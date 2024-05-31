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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.PostureEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.domain.knowledge.common.CorridorFinder;
import mil.arl.gift.domain.knowledge.common.CorridorSegment;
import mil.arl.gift.domain.knowledge.common.Path;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.common.Segment;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks the posture of an entity in a corridor.
 *
 * @author mhoffman
 *
 */
public class CorridorPostureCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CorridorPostureCondition.class);

    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "CorridorPosture.GIFT Domain condition description.html"), "Check posture along path");


    /** the valid postures for this condition */
    private List<PostureEnum> postures;

    /** helps find the current corridor segment for this condition */
    private CorridorFinder corridorFinder;

    private generated.dkf.CorridorPostureCondition corridorPostureInput = null;

    /**
     * Default constructor - required for authoring logic
     */
    public CorridorPostureCondition(){

    }

    /**
     * Class constructor
     *
     * @param segments the segments to populate this condition with
     * @param postures the corresponding postures for each segment in segments
     */
    public CorridorPostureCondition(List<CorridorSegment> segments, List<PostureEnum> postures){
        corridorFinder = new CorridorFinder(segments);
        this.postures = postures;
    }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param corridorPosture - dkf content for this condition
     */
    public CorridorPostureCondition(generated.dkf.CorridorPostureCondition corridorPosture){

        this.corridorPostureInput = corridorPosture;

        postures = new ArrayList<PostureEnum>(corridorPosture.getPostures().getPosture().size());
        for(String postureEnumName : corridorPosture.getPostures().getPosture()){
            postures.add(PostureEnum.valueOf(postureEnumName));
        }
        
        if(corridorPostureInput.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(corridorPostureInput.getTeamMemberRefs());
        }
    }

    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager){
        super.setPlacesOfInterestManager(placesOfInterestManager);

        if(corridorPostureInput != null){

            PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(corridorPostureInput.getPathRef().getValue());
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
    public boolean handleTrainingAppGameState(Message message){

        //really only interested in entity state messages
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){

            EntityState es = (EntityState)message.getPayload();

            if(logger.isDebugEnabled()){
                logger.debug("Received message "+message.toString());
            }

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(es.getEntityID());
            if(teamMember != null){

                //capture change in assessment
                AssessmentLevelEnum level = null;

                Segment currentSegment = corridorFinder.getSegment(es.getLocation());
                if(currentSegment != null){
                    //currently in the posture corridor, check posture

                    if(postures.contains(es.getAppearance().getPosture())){
                        //in the posture corridor and in the appropriate posture
                        
                        if(logger.isTraceEnabled()){
                            logger.trace(teamMember.getName() + " location is within corridor and posture is correct");
                        }
                        
                        // this team member is not violating
                        removeViolator(es.getEntityID());
                        level = handleSuccess(teamMember);

                        if(getViolatorSize() == 0){
                            // the current entity for this entity state message is not violating this condition, nor
                            // is any other entity at the moment
                            
                            // to stop any duration timers for all assessed team members in this condition
                            handleSuccess();  
                        }

                    }else{

                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName() + " location is within corridor and posture of "+es.getAppearance().getPosture()+" is NOT correct");
                        }
                        
                        // calling this repeatedly does nothing
                        addViolator(teamMember, es.getEntityID());
                        level = handleViolation(teamMember);

                    }
                }else{
                    //not in posture corridor, don't care about posture

                    removeViolator(es.getEntityID());
                    level = handleSuccess(teamMember);
                    
                    if(getViolatorSize() == 0){
                        // the current entity for this entity state message is not violating this condition, nor
                        // is any other entity at the moment
                        handleSuccess();                        
                    }

                }

                if(level != null){
                    setAssessmentExplanation();
                    updateAssessment(level);
                    return true;
                }

            }//end if on entityId
        }

        return false;
    }
    
    /**
     * Set the condition's assessment explanation based on the team members being assessed on this condition
     * and are currently violating the condition parameters.
     * @return true if the assessment explanation value for this condition changed during this method.
     */
    private boolean setAssessmentExplanation(){
        
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
            while(itr.hasNext()){
                TeamMember<?> violator = itr.next();
                assessmentExplanationBuilder.append(violator.getName());
                if(itr.hasNext()){
                    assessmentExplanationBuilder.append(", ");
                }
            }
            
            if(violators.size() > 1){
                assessmentExplanationBuilder.append("} are on the path but not in one of the following postures: ").append(postures); 
            }else{
                assessmentExplanationBuilder.append("} is on the path but not in one of the following postures: ").append(postures);   
            }
            
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
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
            setAssessmentExplanation();
            updateAssessment(level);
            if(conditionActionInterface != null){
                conditionActionInterface.conditionAssessmentCreated(this);
            }
        }
    }
    
    /**
     * Handle the case where one or more of the team members are in the corridor with the wrong posture at this moment.
     *
     * @param teamMembers the team members violating this condition.  
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleViolation(TeamMember<?>... teamMembers){

        AssessmentLevelEnum level = null;

        if(isScoringEventActive(teamMembers)){
            //make sure the level hasn't changed due to an ongoing time of violation

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null && getAssessment() != authoredLevel){
                //only set the level if the assessment is different than the current assessment to indicate
                //a new assessment has taken place and needs to be communicated throughout gift
                level = authoredLevel;
            }
        }else{
            //this is a new event, not a continuation of an ongoing event

            scoringEventStarted(teamMembers);

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null){
                //one of the authored assessment rules has been satisfied
                level = authoredLevel;
            }else{
                if(getAssessment() != AssessmentLevelEnum.BELOW_EXPECTATION){
                    //not currently violating this condition, therefore treat this as a new violation
                    level = AssessmentLevelEnum.BELOW_EXPECTATION;
                }
            }
        }

        return level;
    }
    
    /**
     * Handle the case where one or more of the team members are in the corridor with the correct posture at this moment.
     *
     * @param teamMembersNotViolating The {@link TeamMember<?>}s who are
     * not violating this condition. Use no value to update the group assessment
     * of team members being assessed by this condition instance.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleSuccess(TeamMember<?>... teamMembersNotViolating){

        AssessmentLevelEnum level = null;

        //its ok to call this repeatedly w/o starting an event
        scoringEventEnded(teamMembersNotViolating);

        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){
            //one of the authored assessment rules has been satisfied

            if(getAssessment() != authoredLevel){
                //this is a new assessment, don't want to keep sending old, non-changed assessment
                level = authoredLevel;
            }
        }else if(getViolatorSize() == 0){
            //no authored assessment rules AND no current violators

            if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                //found to be far enough away from the location AND was not previously, therefore the level has changed

                level = AssessmentLevelEnum.AT_EXPECTATION;
            }
        }

        return level;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests(){
        return simulationInterests;
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
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
        sb.append("[CorridorPostureCondition: ");
        sb.append(super.toString());
        sb.append(", corridorFinder = ").append(corridorFinder);
        sb.append(", postures = ").append(postures);
        sb.append("]");

        return sb.toString();
    }

}
