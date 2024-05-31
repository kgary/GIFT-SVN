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
import mil.arl.gift.domain.knowledge.common.EntranceAssessment;
import mil.arl.gift.domain.knowledge.common.SequentialSegment;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks whether an entity has entered a specific area along a line segment.
 * 
 * @author mhoffman
 *
 */
public class EnterAreaCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EnterAreaCondition.class);
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.UNKNOWN;
    
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
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "EnterArea.GIFT Domain condition description.html"), "Entered Area");    
    
    /** list of possible entrances for an area of interest */
    private List<EntranceAssessment> entrances;
    
    /** the current entrance the learner is at and possible about to enter */
    private EntranceAssessment currentEntrance;
    
    private generated.dkf.EnterAreaCondition enterAreaInput = null;
    
    /** Used to evaluate the condition based on current coordinates. */
    private Point3d cachedPoint = new Point3d();
    
    /**
     * Default constructor - required for authoring logic
     */
    public EnterAreaCondition(){
        
    }
    
    /**
     * Class constructor
     * 
     * @param entrances the entrances to populate this condition with
     */
    public EnterAreaCondition(List<EntranceAssessment> entrances){
        
        this.entrances = entrances;
        
        updateAssessment(DEFAULT_ASSESSMENT);
    } 
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param enterArea - dkf content for this condition
     */
    public EnterAreaCondition(generated.dkf.EnterAreaCondition enterArea){
        
        this.entrances = new ArrayList<EntranceAssessment>(enterArea.getEntrance().size());
        
        this.enterAreaInput = enterArea;

        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager){
        super.setPlacesOfInterestManager(placesOfInterestManager);  
        
        if(enterAreaInput != null){
            
            for(generated.dkf.Entrance entrance : enterAreaInput.getEntrance()){
                EntranceAssessment entranceAssessment = new EntranceAssessment(entrance, placesOfInterestManager);
                entrances.add(entranceAssessment);
            }
            
            if(enterAreaInput.getTeamMemberRef() != null){
                setTeamMembersBeingAssessed(new generated.dkf.TeamMemberRefs());
                getTeamMembersBeingAssessed().getTeamMemberRef().add(enterAreaInput.getTeamMemberRef());
            }
        }
    }

    @Override
    public boolean handleTrainingAppGameState(Message message){
        
        //really only interested in entity state messages
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            
            EntityState entityState = (EntityState)message.getPayload();
            
            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if(teamMember == null){
                return false;
            }            
            
            AssessmentLevelEnum level = null;
            
            //determine if reached inside location of current entrance
            if(currentEntrance != null){
                
                SequentialSegment segment = currentEntrance.getSequentialSegment();                
                if(reachedLocation(segment.getEndLocation(), segment.getEndPointProximity(), entityState)){
                    //made it too the inside location (end point) of current entrance segment that was previously entered (start point)
                    level = currentEntrance.getAssessment();
                    setAssessmentExplanation(currentEntrance.getSequentialSegment().getName());
                    
                    if(logger.isDebugEnabled()) {
                        logger.debug("Entered area by reaching location = "+segment.getEndLocation());
                    }
                    
                    //reset entrance as this one has been entered
                    currentEntrance = null;   
                }
            }            

            
            if(level != null){
                
                if(level == AssessmentLevelEnum.BELOW_EXPECTATION){
                    //count number of times entering the wrong area
                    scoringEventStarted();
                }
                
                updateAssessment(level);
                return true;
                
            }else{
                //determine if reached any outside entrance
                for(EntranceAssessment entrance : entrances){
                    
                    SequentialSegment segment = entrance.getSequentialSegment();
                    if(reachedLocation(segment.getStartLocation(), segment.getStartPointProximity(), entityState)){
                        currentEntrance = entrance;
                        break;
                    }
                }
            }
            
        } else if(message.getMessageType() == MessageTypeEnum.GEOLOCATION){
            
            Geolocation geolocation = (Geolocation)message.getPayload();      
            
            AssessmentLevelEnum level = null;
            
            //determine if reached inside location of current entrance
            if(currentEntrance != null){
                
                SequentialSegment segment = currentEntrance.getSequentialSegment();
                CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), cachedPoint);
                if(cachedPoint.distance(segment.getEndLocation()) <= segment.getEndPointProximity()){
                    //made it too the inside location (end point) of current entrance segment that was previously entered (start point)
                    
                    level = currentEntrance.getAssessment();
                    setAssessmentExplanation(currentEntrance.getSequentialSegment().getName());
                    
                    if(logger.isDebugEnabled()) {
                        logger.debug("Entered area by reaching location = "+segment.getEndLocation());
                    }
                    
                    //reset entrance as this one has been entered
                    currentEntrance = null;                    
                }
            }            

        
            if(level != null){
                updateAssessment(level);
                return true;
                
            }else{
                //determine if reached any outside entrance
                for(EntranceAssessment entrance : entrances){
                    
                    SequentialSegment segment = entrance.getSequentialSegment();
                    CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), cachedPoint);
                    if(cachedPoint.distance(segment.getStartLocation()) <= segment.getStartPointProximity()){
                        currentEntrance = entrance;
                        break;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Set the assessment explanation for this condition.
     * 
     * @param entranceName the name of the entrance that was just entered by the learner.  Can be null, in which
     * case the assessment explanation is set to null.
     */
    private void setAssessmentExplanation(String entranceName){
        
        if(entranceName == null){
            assessmentExplanation = null;
        }else{
            StringBuffer sb = new StringBuffer();
            sb.append("Used the entrance named '").append(entranceName).append("'.");
            assessmentExplanation = sb.toString();
        }
        
    }
    
    /**
     * Return whether the entity location has reached the point specified.
     * 
     * @param segmentStartOrEnd the authored point of a segment to check the entity state against. Can't be null.
     * Note: when the third value of this point is 0 a 2D check is performed.
     * @param pointProximity how close does the entity state location need to be to the point.  Should be greater than 0.
     * @param entityState the current entity location to use for this check. Can't be null.
     * @return true if the entity location has reached the point specified.
     */
    private boolean reachedLocation(Point3d segmentStartOrEnd, double pointProximity, EntityState entityState){
        
        boolean reached = false;
        if(segmentStartOrEnd.getZ() == 0){
            //perform 2D calculation - this is here to handle the situation where the location authored was
            //                         created using gift wrap for Unity training application.  In this case
            //                         the third value (elevation) in the Unity AGL coordinate will always be zero.
            //                         However when running the Unity scenario, the entity state messages can
            //                         contain non-zero elevation values.
            double distance2D = Math.sqrt(Math.pow(segmentStartOrEnd.getX() - entityState.getLocation().getX(), 2) + Math.pow(segmentStartOrEnd.getY() - entityState.getLocation().getY(), 2));
            if(distance2D <= pointProximity){
                reached = true;
            }
        }else if(entityState.getLocation().distance(segmentStartOrEnd) <= pointProximity){
            //perform 3D calculation            
            reached = true;                 
        }
        
        return reached;
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
        sb.append("[EnterAreaCondition: ");
        sb.append(super.toString());
        sb.append(", currentEntrance = ").append(currentEntrance);
        sb.append("]");
        
        return sb.toString();
    }
}
