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
import java.util.Date;
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
import mil.arl.gift.domain.knowledge.common.Checkpoint;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks the pace of an entity through checkpoints.
 * 
 * @author mhoffman
 *
 */
public class CheckpointPaceCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CheckpointPaceCondition.class);
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;
    
    /** used for conversion between seconds and milliseconds */
    private static final Long MILLISECOND_CONVERSION = 1000L;
    
    /** how close the location has to be to a waypoint for it to be reached */
    private static final double CHECKPOINT_RADIUS = 3.0;
    
    /** Used to evaluate the condition based on current coordinates. */
    private Point3d cachedPoint = new Point3d();
    
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
        overallAssessmentTypes.add(generated.dkf.CompletionTime.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "CheckpointPace.GIFT Domain condition description.html"), "Checkpoint Pace");
    
    /** the checkpoints for this condition */
    private List<Checkpoint> checkpoints;
    
    /** the index of the next checkpoint to reach*/
    private int currentCheckpointIndex = 0;
    
    private double checkpointDeltaTime = 0.0;
    
    private generated.dkf.CheckpointPaceCondition checkpointPaceInput = null;
    
    /**
     * Default constructor - required for authoring logic
     */
    public CheckpointPaceCondition(){
        
    }

    /**
     * Class constructor - set attributes
     * 
     * @param checkpoints - collection of checkpoints to reach
     */
    public CheckpointPaceCondition(List<Checkpoint> checkpoints){
        
        this.checkpoints = checkpoints;
        
        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param checkpointPace - dkf content for this condition
     */
    public CheckpointPaceCondition(generated.dkf.CheckpointPaceCondition checkpointPace){
        
        this.checkpoints = new ArrayList<Checkpoint>(checkpointPace.getCheckpoint().size());
        this.checkpointPaceInput = checkpointPace;
        
        if(checkpointPaceInput.getTeamMemberRef() != null){
            setTeamMembersBeingAssessed(new generated.dkf.TeamMemberRefs());
            getTeamMembersBeingAssessed().getTeamMemberRef().add(checkpointPaceInput.getTeamMemberRef());
        }

        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager){
        super.setPlacesOfInterestManager(placesOfInterestManager);  
        
        if(checkpointPaceInput != null){
            for(generated.dkf.Checkpoint checkpoint : checkpointPaceInput.getCheckpoint()){
                Checkpoint giftCheckpoint = new Checkpoint(checkpoint, placesOfInterestManager);
                checkpoints.add(giftCheckpoint);
            }        
        }
    }
    
    @Override
    public boolean handleTrainingAppGameState(Message message){
              
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            
            EntityState entityState = (EntityState)message.getPayload();
            
            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if(teamMember == null){
                return false;
            }
            
            if(currentCheckpointIndex >= checkpoints.size()){
                //no more checkpoints to check, maintain last assessment
                return false;
            }
            
            AssessmentLevelEnum level = evaluateCondition(entityState.getLocation());   
            
            //update assessment if it was calculated
            if(level != null){
                updateAssessment(level);
            }
            
            //After assessment level has been updated...
            if(currentCheckpointIndex >= checkpoints.size()){
                //no more checkpoints to check, condition is completed
                conditionCompleted();
            }
            
            return level != null;
            
        }else if(message.getMessageType() == MessageTypeEnum.GEOLOCATION){
            
            Geolocation geolocation = (Geolocation)message.getPayload();
            
            if(currentCheckpointIndex >= checkpoints.size()){
                //no more checkpoints to check, maintain last assessment
                return false;
            }
            
            CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), cachedPoint);
            AssessmentLevelEnum level = evaluateCondition(cachedPoint);   
            
            //update assessment if it was calculated
            if(level != null){
                updateAssessment(level);
            }
            
            //After assessment level has been updated...
            if(currentCheckpointIndex >= checkpoints.size()){
                //no more checkpoints to check, condition is completed
                conditionCompleted();
            }
            
            return level != null;
        }
        
        return false;
    }
    
    /**
     * Return whether the current learner location provided has reached the 
     * next checkpoint location in the list of ordered checkpoints.
     * 
     * @param currentLocation the current learner location to compare against the condition's locations.
     * @return the evaluated assessment level based on the learner's current location and the list of checkpoints.  
     * If the learner is ahead/on-time of checkpoint schedule, return At Expectation
     * If the learner is behind the checkpoint schedule, return Below Expectation
     */
    private AssessmentLevelEnum evaluateCondition(Point3d currentLocation){
        
        if(currentLocation == null){
            return null;
        }
        
        AssessmentLevelEnum level = null;
        
        Checkpoint currCheckpoint = checkpoints.get(currentCheckpointIndex);
        
        Point3d checkpointLocation = currCheckpoint.getPoint();
        
        if(reachedLocation(checkpointLocation, CHECKPOINT_RADIUS, currentLocation)){
            //reached checkpoint
            
            //
            //assess based on pace time
            //
            Date now = new Date();
            double elapsedSec = (now.getTime() - initializedAtTime.getTime())/MILLISECOND_CONVERSION;
            double currentCheckpointDelta = getCheckpointDelta(elapsedSec, currCheckpoint);
            
            if(currentCheckpointDelta > 0 || getAssessment() == AssessmentLevelEnum.BELOW_EXPECTATION){
                //either currently behind pace or previously behind pace
                
                if(currentCheckpointDelta < checkpointDeltaTime){
                    //pace is increasing 
                    
                    if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                        //not performing At Expectation previously, therefore the level has changed
                        
                        level = AssessmentLevelEnum.AT_EXPECTATION;
                        
                        assessmentExplanation = null;
                        scoringEventEnded();
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("Pace is increasing because previous checkpoint time delta = "+checkpointDeltaTime+" and current checkpoint delta = "+currentCheckpointDelta);
                        }
                    }
                    
                }else if(getAssessment() != AssessmentLevelEnum.BELOW_EXPECTATION){
                    //pace is decreasing or remaining the same or currently behind pace on first checkpoint
                    //AND was not performing Below Expectation previously, therefore the level has changed
                    level = AssessmentLevelEnum.BELOW_EXPECTATION;
                    
                    StringBuffer sb = new StringBuffer();
                    sb.append("Failed to reach '").append(currCheckpoint.getPoint().getName()).append("' within ").append(currCheckpoint.getAtTime()).append(" seconds.");
                    assessmentExplanation = sb.toString();
                    
                    scoringEventStarted();
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("Currently behind pace because previous checkpoint time delta = "+checkpointDeltaTime+" and current checkpoint delta = "+currentCheckpointDelta);
                    }

                }
                
            }else if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                //either on time or ahead of schedule AND was not performing At Expectation previously, therefore the level has changed
                level = AssessmentLevelEnum.AT_EXPECTATION;
                assessmentExplanation = null;
                
                if(logger.isDebugEnabled()){
                    logger.debug("On-Pace because previous checkpoint time delta = "+checkpointDeltaTime+" and current checkpoint delta = "+currentCheckpointDelta);
                }

            }
            checkpointDeltaTime = currentCheckpointDelta;
            currentCheckpointIndex++;

        }
        
        return level;
    }
    
    /**
     * Return whether the entity location has reached the point specified.
     *
     * @param candidatePt the authored point to check the entity state against. Can't be null.
     * Note: when the third value of this point is 0 a 2D check is performed.
     * @param pointProximity how close does the entity state location need to be to the point.  Should be greater than 0.
     * @param currentEntityStateLocation the current entity location to use for this check. Can't be null.
     * @return true if the entity location has reached the point specified.
     */
    private boolean reachedLocation(Point3d candidatePt, double pointProximity, Point3d currentEntityStateLocation){

        boolean reached = false;
        if(candidatePt.getZ() == 0){
            //perform 2D calculation - this is here to handle the situation where the location authored was
            //                         created using gift wrap for Unity training application.  In this case
            //                         the third value (elevation) in the Unity AGL coordinate will always be zero.
            //                         However when running the Unity scenario, the entity state messages can
            //                         contain non-zero elevation values.
            double distance = Math.sqrt(Math.pow(candidatePt.getX() - currentEntityStateLocation.getX(), 2) + Math.pow(candidatePt.getY() - currentEntityStateLocation.getY(), 2));

            if(distance <= pointProximity){                

                if(logger.isDebugEnabled()){
                    logger.debug("Reached checkpoint: "+checkpoints.get(currentCheckpointIndex)+" at distance "+distance);
                }
                
                reached = true;
            }
        }else{
            
            //perform 3D calculation
            double distance = currentEntityStateLocation.distance(candidatePt);
            
            if(distance <= pointProximity){
                
                if(logger.isDebugEnabled()){
                    logger.debug("Reached checkpoint: "+checkpoints.get(currentCheckpointIndex)+" at distance "+distance);
                }
                
                reached = true;
            }
        }

        return reached;
    }
    
    /**
     * Calculate the delta time between the elapsed seconds provided and the checkpoint time.
     * A positive number is the amount of delta time the elapsed time is higher than the checkpoint window time.
     * 
     * @param elapsedSec - amount of elapsed simulation time
     * @param checkpoint - the current checkpoint to reach next
     * @return double - delta time in seconds
     */
    private double getCheckpointDelta(double elapsedSec, Checkpoint checkpoint){
        
        double delta = elapsedSec - checkpoint.getAtTime();
        if(delta <= 0){
            //reached checkpoint ahead of or at time
            return delta;
            
        }else if(delta < checkpoint.getWindow()){
            //reached checkpoint within window
            return 0.0;
            
        }else{
            //a positive number, amount of delta time after the checkpoint window time
            return delta - checkpoint.getWindow();
        }
        
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
        return true;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CheckpointPaceCondition: ");
        sb.append(super.toString());
        sb.append(", currentCheckpointIndex = ").append(currentCheckpointIndex);
        sb.append(", checkpointDeltaTime = ").append(checkpointDeltaTime);
        sb.append("]");
        
        return sb.toString();
    }

}
