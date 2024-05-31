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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
 * This condition checks the progress of an entity through checkpoints.
 *
 * @author mhoffman
 *
 */
public class CheckpointProgressCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CheckpointProgressCondition.class);

    /** used for conversion between seconds and milliseconds */
    private static final Long MILLISECOND_CONVERSION = 1000L;

    /** how close the location has to be to a waypoint for it to be reached */
    private static final double CHECKPOINT_RADIUS = 3.0;

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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "CheckpointProgress.GIFT Domain condition description.html"), "Checkpoint Progress");

    /** for scheduling a timer task for creating assessments out of synch with incoming simulation messages */
    private static final String TIMER_NAME = "CheckpointProgressCondition";
    private Timer timer = new Timer(TIMER_NAME);

    /** the checkpoints for this condition */
    private List<Checkpoint> checkpoints;

    /** the index of the next checkpoint to reach*/
    private int currentCheckpointIndex = 0;

    private double distanceFromNextCheckpoint = -1.0;

    /** collection of timer tasks for checkpoints */
    private Map<Checkpoint, CheckpointTimerTask> checkpointTimers;

    private generated.dkf.CheckpointProgressCondition checkpointProgressInput = null;
    
    /** Used to evaluate the condition based on current coordinates. */
    private Point3d cachedPoint = new Point3d();

    /**
     * Default constructor - required for authoring logic
     */
    public CheckpointProgressCondition(){

    }

    /**
     * Class constructor
     *
     * @param checkpoints the checkpoints for this condition
     */
    public CheckpointProgressCondition(List<Checkpoint> checkpoints){
        this.checkpoints = checkpoints;
    }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param checkpointProgress - dkf content for this condition
     */
    public CheckpointProgressCondition(generated.dkf.CheckpointProgressCondition checkpointProgress){

        this.checkpoints = new ArrayList<Checkpoint>(checkpointProgress.getCheckpoint().size());
        this.checkpointProgressInput = checkpointProgress;
        
        if(checkpointProgressInput.getTeamMemberRef() != null){
            setTeamMembersBeingAssessed(new generated.dkf.TeamMemberRefs());
            getTeamMembersBeingAssessed().getTeamMemberRef().add(checkpointProgressInput.getTeamMemberRef());
        }
    }

    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager) {
        super.setPlacesOfInterestManager(placesOfInterestManager);

        if(checkpointProgressInput != null){
            for (generated.dkf.Checkpoint checkpoint : checkpointProgressInput.getCheckpoint()) {
                Checkpoint giftCheckpoint = new Checkpoint(checkpoint, placesOfInterestManager);
                checkpoints.add(giftCheckpoint);
            }
        }
    }

    @Override
    public void start(){
        super.start();

        //create timer tasks for each checkpoint
        checkpointTimers = new HashMap<Checkpoint, CheckpointTimerTask>(checkpoints.size());
        for(Checkpoint checkpoint : checkpoints){

            CheckpointTimerTask task = new CheckpointTimerTask(checkpoint);
            timer.schedule(task, (long)(checkpoint.getAtTime() + checkpoint.getWindow()) * MILLISECOND_CONVERSION);

            checkpointTimers.put(checkpoint, task);
        }
    }

    @Override
    public void stop(){
        super.stop();

        //cancel any schedule events in the timer - has no affect if all the events have already been fired
        timer.cancel();

        if(completedAtTime == null){
            logger.debug(this + " did not complete.  Currently "+distanceFromNextCheckpoint+" away from checkpoint number "+(currentCheckpointIndex+1)+" of "+checkpoints.size()+" checkpoints");
        }

    }

    @Override
    public boolean handleTrainingAppGameState(Message message){

        //this will prevent the condition from being assessed when it is clearly completed
        if(hasCompleted()){
            return false;
        }

        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){

            EntityState entityState = (EntityState)message.getPayload();

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if(teamMember == null){
                return false;
            }

            //capture change in assessment
            AssessmentLevelEnum level = evaluateCondition(entityState.getLocation());

            if(currentCheckpointIndex >= checkpoints.size()){
                //no more checkpoints to check, condition is over
                conditionCompleted();
            }

            if(level != null){
                updateAssessment(level);
                return true;
            }

        }else if(message.getMessageType() == MessageTypeEnum.GEOLOCATION){

            Geolocation geolocation = (Geolocation)message.getPayload();

            CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), cachedPoint);

            //capture change in assessment
            AssessmentLevelEnum level = evaluateCondition(cachedPoint);
            if(currentCheckpointIndex >= checkpoints.size()){
                //no more checkpoints to check, condition is over
                conditionCompleted();
            }

            if(level != null){
                updateAssessment(level);
                return true;
            }
        }

        return false;
    }

    /**
     * Return whether the current learner location provided has reached the
     * next checkpoint location in the list of ordered checkpoints.
     *
     * @param currentLocation the current learner location to compare against the condition's locations.
     * @return the evaluated assessment level based on the learner's current location and the list of checkpoints.
     * If the learner reached the next checkpoint with the allotted time window, return At Expectation
     * If the learner reached the next checkpoint before the time window, return At Expectation
     * Below will be returned outside of this method based on a timer event being fired.
     */
    private AssessmentLevelEnum evaluateCondition(Point3d currentLocation){

        if(currentLocation == null){
            return null;
        }

        AssessmentLevelEnum level = null;

        Checkpoint currCheckpoint = checkpoints.get(currentCheckpointIndex);

        CheckpointTimerTask task = checkpointTimers.get(currCheckpoint);
        if(task.hasFired()){
            //the current checkpoint has been fired, find next, not-fired checkpoint

            do{

                currentCheckpointIndex++;
                if(currentCheckpointIndex >= checkpoints.size()){
                    //no more checkpoints to check, maintain last assessment
                    conditionCompleted();
                    return null;
                }

                currCheckpoint = checkpoints.get(currentCheckpointIndex);
                task = checkpointTimers.get(currCheckpoint);

            }while(task.hasFired());
        }

        Point3d checkpointLocation = currCheckpoint.getPoint();

        if(reachedLocation(checkpointLocation, CHECKPOINT_RADIUS, currentLocation)){
            //reached checkpoint

            //cancel timer task
            task.cancel();

            //assess based on time
            Date now = new Date();
            double elapsedSec = (now.getTime() - initializedAtTime.getTime())/MILLISECOND_CONVERSION;
            if(elapsedSec > currCheckpoint.getAtTime()){
                //reached checkpoint before max expiration time, i.e. within the window

                level = AssessmentLevelEnum.AT_EXPECTATION;
                assessmentExplanation = null;

            }else{
                //reached checkpoint before 'at time'

                StringBuffer sb = new StringBuffer();
                sb.append("Reached '").append(currCheckpoint.getPoint().getName()).append("' well before the accepted ").append(currCheckpoint.getAtTime()).append(" seconds.");
                assessmentExplanation = sb.toString();
                level = AssessmentLevelEnum.ABOVE_EXPECTATION;
            }

            scoringEventEnded();

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
            distanceFromNextCheckpoint = Math.sqrt(Math.pow(candidatePt.getX() - currentEntityStateLocation.getX(), 2) + Math.pow(candidatePt.getY() - currentEntityStateLocation.getY(), 2));

            if(distanceFromNextCheckpoint <= pointProximity){
                
                if(logger.isDebugEnabled()){
                    logger.debug("Reached checkpoint: "+checkpoints.get(currentCheckpointIndex)+" at distance "+distanceFromNextCheckpoint);
                }
                
                reached = true;
            }
        }else{
            
            //perform 3D calculation
            distanceFromNextCheckpoint = currentEntityStateLocation.distance(candidatePt);
            
            if(distanceFromNextCheckpoint <= pointProximity){
                
                if(logger.isDebugEnabled()){
                    logger.debug("Reached checkpoint: "+checkpoints.get(currentCheckpointIndex)+" at distance "+distanceFromNextCheckpoint);
                }
                
                reached = true;
            }
        }

        return reached;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests(){
        return simulationInterests;
    }

    /**
     * The provided checkpoint has expired.  Change the assessment level.
     *
     * @param checkpoint the checkpoint that was not reached by the learner in time.
     */
    private void checkpointExpired(Checkpoint checkpoint){

        if(logger.isDebugEnabled()){
            logger.debug("The checkpoint: "+checkpoint+" has expired");
        }

        scoringEventStarted();

        StringBuffer sb = new StringBuffer();
        sb.append("Failed to reach '").append(checkpoint.getPoint().getName()).append("' within ").append(checkpoint.getAtTime()).append(" seconds.");
        assessmentExplanation = sb.toString();
        
        updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
        if(conditionActionInterface != null){
            conditionActionInterface.conditionAssessmentCreated(this);
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
        sb.append("[CheckpointProgressCondition: ");
        sb.append(super.toString());
        sb.append(", distanceFromNextCheckpoint = ").append(distanceFromNextCheckpoint);
        sb.append(", currentCheckpointIndex = ").append(currentCheckpointIndex);
        sb.append("]");

        return sb.toString();
    }

    /**
     * This class is the timer task which runs at the appropriately schedule date.  It will
     * cause a new assessment of this condition to be created.
     *
     * @author mhoffman
     *
     */
    private class CheckpointTimerTask extends TimerTask{

        private Checkpoint checkpoint;

        /** has this timer task been ran yet */
        private boolean fired = false;

        public CheckpointTimerTask(Checkpoint checkpoint){
            this.checkpoint = checkpoint;
        }

        /**
         * Return whether this task has been fired or not
         *
         * @return boolean
         */
        public boolean hasFired(){
            return fired;
        }

        @Override
        public void run() {
            fired = true;
            checkpointExpired(checkpoint);
        }
    }

}
