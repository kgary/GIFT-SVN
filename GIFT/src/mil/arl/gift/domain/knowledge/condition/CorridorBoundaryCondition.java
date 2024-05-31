/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.domain.knowledge.common.CorridorFinder;
import mil.arl.gift.domain.knowledge.common.CorridorSegment;
import mil.arl.gift.domain.knowledge.common.Path;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.common.Segment;
import mil.arl.gift.net.api.message.Message;

/**
 * This class contains information about a corridor condition that will be assessed based on simulation messages.
 *
 * @author mhoffman
 *
 */
public class CorridorBoundaryCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CorridorBoundaryCondition.class);

    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
        simulationInterests.add(MessageTypeEnum.GEOLOCATION);
    }

    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.ViolationTime.class);
    }

    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "CorridorBoundary.GIFT Domain condition description.html"), "Follow Path");

    /** Used to evaluate the condition based on current coordinates. */
    private Point3d cachedPoint = new Point3d();

    /** used to find the current corridor segment */
    private CorridorFinder corridorFinder;

    /** flag to indicate whether the corridor has been entered at least once */
    private boolean hasEnteredCorridor = false;

    private generated.dkf.CorridorBoundaryCondition corridorBoundaryInput = null;

    /**
     * Default constructor - required for authoring logic
     */
    public CorridorBoundaryCondition(){

    }

    /**
     * Class constructor
     *
     * @param segments the segments to populate this condition with
     */
    public CorridorBoundaryCondition(List<CorridorSegment> segments){
        corridorFinder = new CorridorFinder(segments);
    }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param corridorBoundary - dkf content for this condition
     */
    public CorridorBoundaryCondition(generated.dkf.CorridorBoundaryCondition corridorBoundary){
        this.corridorBoundaryInput = corridorBoundary;
        
        if(corridorBoundaryInput.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(corridorBoundaryInput.getTeamMemberRefs());
        }
    }

    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager){
        super.setPlacesOfInterestManager(placesOfInterestManager);

        if(corridorBoundaryInput != null){

            PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(corridorBoundaryInput.getPathRef().getValue());
            if(poi == null) {
                throw new NullPointerException("The place of interest defining the corridor to follow cannot be null.");
            }

            Path path = (Path) poi;

            List<CorridorSegment> segments = new ArrayList<CorridorSegment>(path.getSegment().size());
            for (Segment segment : path.getSegment()) {
                CorridorSegment giftCorridorSegment = new CorridorSegment(segment, placesOfInterestManager);
                segments.add(giftCorridorSegment);
            }

            corridorFinder = new CorridorFinder(segments);
        }
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests(){
        return simulationInterests;
    }

    @Override
    public boolean handleTrainingAppGameState(Message message){

        //really only interested in entity state messages
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){

            EntityState esm = (EntityState)message.getPayload();

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(esm.getEntityID());
            if(teamMember != null){

                //capture change in assessment
                AssessmentLevelEnum level = null;

                Segment currentSegment = corridorFinder.getSegmentWithBuffer(esm.getLocation());
                if(currentSegment == null){
                    //found to be outside of corridor

                    if(hasEnteredCorridor) {
                        // the entity has been in the corridor but is now outside of it 

                        // update the last time this entity violated 
                        addViolator(teamMember, esm.getEntityID());
                        
                        if(getAssessment() != AssessmentLevelEnum.BELOW_EXPECTATION){
                            // was not performing Below Expectation before - therefore the level has changed and
                            // needs to be communicated
                            level = AssessmentLevelEnum.BELOW_EXPECTATION;
                            setAssessmentExplanation(false);
                            scoringEventStarted(teamMember);
                        }
                    }

                }else if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                    //found to be inside of corridor AND was not performing At Expectation before - therefore the level has changed

                    removeViolator(esm.getEntityID());
                    setAssessmentExplanation(true);
                    
                    if(getViolatorSize() == 0){
                        // the current entity for this entity state message is not violating this condition, nor
                        // is any other entity at the moment
                        level = AssessmentLevelEnum.AT_EXPECTATION;
    
                        if(getAssessment() == AssessmentLevelEnum.BELOW_EXPECTATION){
                            //previously outside of corridor, therefore the violation has ended
    
                            if(logger.isDebugEnabled()) {
                                logger.debug("Detected violation has ended for "+this+", therefore the assessment will change to "+level);
                            }

                        }
                        
                        // to stop any duration timers for all assessed team members in this condition
                        scoringEventEnded(); 
                    }
                    
                    scoringEventEnded(teamMember);

                    //indicate has entered corridor at least once - prevents violations before entering corridor
                    hasEnteredCorridor = true;
                }

                //update assessment if it was calculated
                if(level != null){
                    updateAssessment(level);
                    return true;
                }

            }//end if on entityId

        } else if(message.getMessageType() == MessageTypeEnum.GEOLOCATION){

            Geolocation geolocation = (Geolocation)message.getPayload();

            //capture change in assessment
            AssessmentLevelEnum level = null;
            
            CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), cachedPoint);
            Segment currentSegment = corridorFinder.getSegmentWithBuffer(cachedPoint);
            if(currentSegment == null){
                //found to be outside of corridor

                if(hasEnteredCorridor) {
                    // the entity has been in the corridor but is now outside of it 
                    
                    // update the last time this entity violated 
                    addViolator(null, null);
                    
                    if(getAssessment() != AssessmentLevelEnum.BELOW_EXPECTATION){
                        // was not performing Below Expectation before - therefore the level has changed and
                        // needs to be communicated
                        level = AssessmentLevelEnum.BELOW_EXPECTATION;
                        setAssessmentExplanation(false);
                        scoringEventStarted();
                    }
                }

            }else if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                //found to be inside of corridor AND was not performing At Expectation before - therefore the level has changed

                level = AssessmentLevelEnum.AT_EXPECTATION;
                setAssessmentExplanation(true);

                if(getAssessment() == AssessmentLevelEnum.BELOW_EXPECTATION){
                    //previously outside of corridor, therefore the violation has ended

                    if(logger.isDebugEnabled()) {
                        logger.debug("Detected violation has ended for "+this+", therefore the assessment will change to "+level);
                    }

                    removeViolator(null);
                    scoringEventEnded();
                }

                //indicate has entered corridor at least once - prevents violations before entering corridor
                hasEnteredCorridor = true;
            }

            //update assessment if it was calculated
            if(level != null){
                updateAssessment(level);
                return true;
            }

        }//end if on entityId

        //return the new assessment - if any
        return false;
    }
    
    @Override
    protected void violatorUpdated(Set<TeamMember<?>> removedViolators){
        
        if(removedViolators != null && !removedViolators.isEmpty() && getViolatorSize() == 0){
            // no more violators due to timeout of stale entity state information,
            // not sure what to do here except throw our hands up and assess as unknown.  
            // We can't tell if the entity is still in the corridor or not.  We could
            // maintain the last assessment but that might not even be matching to what
            // is happening in the training application

            AssessmentLevelEnum level = AssessmentLevelEnum.UNKNOWN;
            assessmentExplanation = null;
            updateAssessment(level);
            if(conditionActionInterface != null){
                conditionActionInterface.conditionAssessmentCreated(this);
            }
        }
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }

    /**
     * Set the condition's assessment explanation based on the team members being assessed on this condition
     * and are currently violating the condition parameters.
     * @param insideCorridor whether the explanation represents that one or more of the assessed learners is inside 
     * one or more of the specified path segments.
     * @return true if the assessment explanation value for this condition changed during this method.
     */
    private boolean setAssessmentExplanation(boolean insideCorridor){
        
        //update assessment explanation
        Set<TeamMember<?>> violators = buildViolatorsInfo();
        boolean changed = false;
        if(violators.isEmpty()){
            changed = assessmentExplanation != null;
            assessmentExplanation = null;
        }else{
            StringBuilder assessmentExplanationBuilder = new StringBuilder();
            Iterator<TeamMember<?>> itr = violators.iterator();
            assessmentExplanationBuilder.append("{");
            boolean mobileUser = false;
            while(itr.hasNext()){
                TeamMember<?> violator = itr.next();
                if(violator == null){
                    assessmentExplanationBuilder.setLength(0);  //clear the { character
                    mobileUser = true;
                    break;
                }else{
                    assessmentExplanationBuilder.append(violator.getName());
                    if(itr.hasNext()){
                        assessmentExplanationBuilder.append(", ");
                    }
                }
            }
            
            if(mobileUser){
                
                if(insideCorridor){
                    assessmentExplanationBuilder.append("The learner is inside the path.");
                }else{
                    assessmentExplanationBuilder.append("The learner is outside of the path.");
                }
            }else{
                if(violators.size() > 1){
                    assessmentExplanationBuilder.append("} are outside of the path.");  
                }else{
                    assessmentExplanationBuilder.append("} is outside of the path.");  
                }
            }
            
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
    }

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }
    
    @Override
    public boolean canComplete() {
        return false;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[CorridorBoundaryCondition: ");
        sb.append(super.toString());
        sb.append(", corridorFinder = ").append(corridorFinder);
        sb.append(", hasEnteredcorridor = ").append(hasEnteredCorridor);
        sb.append("]");

        return sb.toString();
    }


}
