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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import javax.vecmath.Vector3d;

import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.net.api.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import generated.dkf.TeamMemberRefs;

/**
 * This condition checks whether an entity is obeying a speed limit.
 *
 * @author mhoffman
 *
 */
public class SpeedLimitCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SpeedLimitCondition.class);

    /** the default assessment level for this condition */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;

    /** name of the thread used to prevent thrashing of assessments from a speeder */
    private static final String TIMER_NAME = "Speed limit violation timer";

    /** used to calculate meters per second from miles per hour that is authored */
    private static double metersPerSecondConversion = 2.237;

    /**
     * amount of time in milliseconds between checking a entity's entity state
     * Also used for the amount of between assessment output of this condition if a time was
     * not specified by the author for this condition.
     */
    private static final Long DURATION_BETWEEN_CHECKS = 1000L;

    /**
     * contains the types of GIFT messages this condition needs in order to
     * provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;

    static {
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
    private static final ConditionDescription DESCRIPTION =
            new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "SpeedLimit.GIFT Domain condition description.html"), "Speed Limit");

    /** the authored speed limit, converted to m/s */
    private double speedLimitMS;

    /** (optional) the authored min speed limit, converted to m/s */
    private Double minSpeedLimitMS = null;

    /** how long in milliseconds the speed limit assessment must be maintained before the assessment is reported */
    private int minAssessmentDurationMS = 0;

    /**
     * the timer used when the speed limit is actively being violated, need to know if the violation
     * has been sustained for a certain amount of time as provided by the author
     */
    private SchedulableTimer speedLimitViolationTimer = null;

    /** the timestamp of when the previous geolocation message was received, used to manually calculate speed */
    private long lastGeoLocationTimestamp = -1;

    /** the previous geolocation, used to manually calculate speed */
    private Geolocation lastGeolocation = null;

    /** the input configuration parameters to this condition */
    private generated.dkf.SpeedLimitCondition speedLimitInput;

    /**
     * mapping of unique team member name to the information being tracked for assessment for this condition
     */
    private Map<String, SpeedLimitAssessmentWrapper> teamMemberRefAssessmentMap = new HashMap<>();

    /**
     * the types of violations that can occur for this condition.
     * @author mhoffman
     *
     */
    private enum SPEED_VIOLATION_TYPE{
        NONE,
        MIN_SPEED,
        MAX_SPEED
    }

    /**
     * Default constructor - required for authoring logic
     */
    public SpeedLimitCondition(){ }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param speedLimit - dkf content for this condition
     */
    public SpeedLimitCondition(generated.dkf.SpeedLimitCondition speedLimitInput) {

        this.speedLimitInput = speedLimitInput;
        speedLimitMS = speedLimitInput.getSpeedLimit().doubleValue() / metersPerSecondConversion;
        minAssessmentDurationMS = speedLimitInput.getMinDurationBeforeViolation() != null ? speedLimitInput.getMinDurationBeforeViolation().intValue() * 1000 : DURATION_BETWEEN_CHECKS.intValue();

        if(speedLimitInput.getMinSpeedLimit() != null && speedLimitInput.getMinSpeedLimit().doubleValue() >= 0){
            minSpeedLimitMS =  speedLimitInput.getMinSpeedLimit().doubleValue() / metersPerSecondConversion;
        }

        //save any authored real time assessment rules
        if(speedLimitInput.getRealTimeAssessmentRules() != null){
            addRealTimeAssessmentRules(speedLimitInput.getRealTimeAssessmentRules());
        }

        if(speedLimitInput.getTeamMemberRef() != null){
            setTeamMembersBeingAssessed(new TeamMemberRefs());
            getTeamMembersBeingAssessed().getTeamMemberRef().add(speedLimitInput.getTeamMemberRef());
        }

        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){
            //set the initial assessment to the authored real time assessment value
            updateAssessment(authoredLevel);
        }else{
            updateAssessment(DEFAULT_ASSESSMENT);
        }
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        final int prevViolatorCount = getViolatorSize();
        if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {

            EntityState entityState = (EntityState) message.getPayload();

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if (teamMember == null) {
                return false;
            }

            SpeedLimitAssessmentWrapper assessmentWrapper = teamMemberRefAssessmentMap.get(teamMember.getName());
            if(assessmentWrapper == null){
                assessmentWrapper = new SpeedLimitAssessmentWrapper(teamMember);
                synchronized (teamMemberRefAssessmentMap) {
                    teamMemberRefAssessmentMap.put(teamMember.getName(), assessmentWrapper);
                }
            }

            // update last received entity state
            assessmentWrapper.setEntityState(entityState);

            // check if this entity should be evaluated
            Long lastAssessmentCheck = assessmentWrapper.getLastAssessmentCheck();
            if(lastAssessmentCheck != null && System.currentTimeMillis() - lastAssessmentCheck < DURATION_BETWEEN_CHECKS){
                return false;
            }

            if(speedLimitViolationTimer == null){
                // the timer is not running and the timer is needed in order to determine if the learner has violated the speeding
                // for a long enough time

                speedLimitViolationTimer = new SchedulableTimer(TIMER_NAME);

                // run assessment reporting logic twice as often as the time an assessment must be maintained
                speedLimitViolationTimer.scheduleAtFixedRate(new SpeedLimitViolationTimeTask(), minAssessmentDurationMS / 2, minAssessmentDurationMS / 2);

                if(logger.isDebugEnabled()) {
                    logger.debug("Started violating the speed limit condition.  Starting speed limit violation timer");
                }
            }

            // update the last time this entity was evaluated
            assessmentWrapper.updateLastAssessmentCheck();

            // evaluate and save the assessment for this entity
            SPEED_VIOLATION_TYPE speedLimitViolationType = evaluateCondition(entityState.getLinearVelocity());
            AssessmentLevelEnum level = null;
            if(speedLimitViolationType != SPEED_VIOLATION_TYPE.NONE){
                //found to be above the specified speed limit (or below the optional min speed limit)

                if(speedLimitViolationType == SPEED_VIOLATION_TYPE.MAX_SPEED ||
                        (speedLimitViolationType == SPEED_VIOLATION_TYPE.MIN_SPEED && assessmentWrapper.hasEnteredSpeedBounds())){
                        // violating speed limit OR
                        // violating min speed and was previously above the min speed
                    addViolator(teamMember, entityState.getEntityID());
                    level = handleViolation(teamMember);
                }

            } else{
                //make sure if this entity was in violation that it is no longer in the violation collection
                removeViolator(entityState.getEntityID());

                handleSuccess(teamMember);
                if(getViolatorSize() == 0){
                    // the current entity for this entity state message is not violating this condition, nor
                    // is any other entity at the moment
                    // - don't provide team member to stop any duration timers for all assessed team members in this condition
                    level = handleSuccess();
                }

            }

            // update flag to determine when transition from below min speed to above min speed back to below min speed
            // Note: only want to detect the first time the min speed threshold is crossed from 0, not subsequent times
            if(speedLimitViolationType != SPEED_VIOLATION_TYPE.MIN_SPEED){
                assessmentWrapper.setHasEnteredSpeedBounds(true);
            }


            if (level != null && level != assessmentWrapper.getAssessment() || prevViolatorCount != getViolatorSize()) {
                // don't update the assessment time if the assessment hasn't changed
                assessmentWrapper.updateAssessment(level, speedLimitViolationType);
            }

        } else if (message.getMessageType() == MessageTypeEnum.GEOLOCATION) {

            Geolocation geolocation = (Geolocation) message.getPayload();

            // preventing divide by zero when calculating speed based on distance traveled if
            // the time between geolocation updates is zero.
            if((message.getTimeStamp() - lastGeoLocationTimestamp) == 0){
                return false;
            }

            AssessmentLevelEnum level = null;
            boolean assessmentExplanationChanged = false;

            if (evaluateCondition(message.getTimeStamp(), geolocation)) {
                //found to be above the specified speed limit
                level = handleViolation();

                //update assessment explanation
                assessmentExplanationChanged = setAssessmentExplanation();

            } else{
                level = handleSuccess();

                //update assessment explanation
                assessmentExplanationChanged = setAssessmentExplanation();
            }

            if (level != null) {
                updateAssessment(level);
                return true;
            }else if(assessmentExplanationChanged){
                return true;
            }
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

            StringBuilder assessmentMaxSpeedExplanationBuilder = null;
            StringBuilder assessmentMinSpeedExplanationBuilder = null;

            while(itr.hasNext()){
                TeamMember<?> violator = itr.next();
                SpeedLimitAssessmentWrapper assessmentWrapper;
                synchronized(teamMemberRefAssessmentMap){
                    assessmentWrapper = teamMemberRefAssessmentMap.get(violator.getName());
                }

                if(assessmentWrapper == null){
                    continue;
            }

                if(assessmentWrapper.getSpeedViolationType() == SPEED_VIOLATION_TYPE.MAX_SPEED){

                    if(assessmentMaxSpeedExplanationBuilder == null){
                        // this is the first violator of max speed
                        assessmentMaxSpeedExplanationBuilder = new StringBuilder("{");
                    }else{
                        //there was a previous violator of max speed added to this builder
                        assessmentMaxSpeedExplanationBuilder.append(", ");
                    }

                    assessmentMaxSpeedExplanationBuilder.append(violator.getName());

                }else if(assessmentWrapper.getSpeedViolationType() == SPEED_VIOLATION_TYPE.MIN_SPEED){

                    if(assessmentMinSpeedExplanationBuilder == null){
                        // this is the first violator of min speed
                        assessmentMinSpeedExplanationBuilder = new StringBuilder("{");
                    }else{
                        //there was a previous violator of min speed added to this builder
                        assessmentMinSpeedExplanationBuilder.append(", ");
                    }

                    assessmentMinSpeedExplanationBuilder.append(violator.getName());

                }

            }// end while

            if(assessmentMaxSpeedExplanationBuilder != null){
                assessmentMaxSpeedExplanationBuilder.append("} went over the speed limit of ").append(speedLimitInput.getSpeedLimit()).append(" mph");
                assessmentExplanationBuilder.append(assessmentMaxSpeedExplanationBuilder.toString());
            }

            if(assessmentMinSpeedExplanationBuilder != null){
                assessmentMinSpeedExplanationBuilder.append("} went under the min speed limit of ").append(speedLimitInput.getMinSpeedLimit()).append(" mph");

                if(assessmentMaxSpeedExplanationBuilder != null){
                    assessmentExplanationBuilder.append("\n");
                }

                assessmentExplanationBuilder.append(assessmentMinSpeedExplanationBuilder.toString());
            }

            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }

        return changed;
    }

    @Override
    protected void violatorUpdated(Set<TeamMember<?>> removedViolators){

        if(removedViolators != null && !removedViolators.isEmpty()){
            
            if(getViolatorSize() == 0){
                //no more violators
                // - use no team members to stop any duration timers for all assessed team members in this condition
                AssessmentLevelEnum level = handleSuccess();
                if(level != null){
                    updateAssessment(level);
                    if(conditionActionInterface != null){
                        conditionActionInterface.conditionAssessmentCreated(this);
                    }
                }
            }else{
                handleSuccess(removedViolators.toArray(new TeamMember<?>[removedViolators.size()]));
            }


        }
    }

    /**
     * Handle the case where the learner is below the speed limit.
     *
     * @param teamMembersNotViolating The {@link TeamMember<?>}s who are
     * not violating this condition. Can be empty.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleSuccess(TeamMember<?>...teamMembersNotViolating){

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
        }else{
            //no authored assessment rules

            if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                //found to be under the speed limit AND was not previously, therefore the level has changed

                level = AssessmentLevelEnum.AT_EXPECTATION;
            }
        }

        return level;
    }

    /**
     * Handle the case where the learner is NOT obeying the speed limit at this moment.
     *
     * @param violatingTeamMembers The {@link TeamMember<?>}s who are
     * violating this condition. Can be empty.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleViolation(TeamMember<?>...violatingTeamMembers){

        AssessmentLevelEnum level = null;
        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){

            if(getAssessment() != authoredLevel){
                //only set the level if the assessment is different than the current assessment to indicate
                //a new assessment has taken place and needs to be communicated throughout gift
                level = authoredLevel;
            }
        }else if(getAssessment() != AssessmentLevelEnum.BELOW_EXPECTATION){
            //not currently violating this condition, therefore treat this as a new violation
            level = AssessmentLevelEnum.BELOW_EXPECTATION;
        }

        return level;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
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

    /**
     * Return whether the current learner's speed is above the speed limit for this condition.
     *
     * @param currentLinearVelocity the current learner linear velocity in m/s
     * @return the enumerated type of speed condition violation
     * SPEED_VIOLATION_TYPE.NONE : (any of the following is true)
     * 1. no linear velocity
     * 2. min speed is not defined and not over the speed limit
     * SPEED_VIOLATION_TYPE.MIN_SPEED:
     * 1. min speed is defined and going less than min speed
     * SPEED_VIOLATION_TYPE.MAX_SPEED:
     * 1. going over speed limit
     */
    private SPEED_VIOLATION_TYPE evaluateCondition(Vector3d currentLinearVelocity){

        if(currentLinearVelocity == null){
            return SPEED_VIOLATION_TYPE.NONE;
        }else if(minSpeedLimitMS != null && currentLinearVelocity.length() <= minSpeedLimitMS){
            // violating optional min speed
            return SPEED_VIOLATION_TYPE.MIN_SPEED;
        }else if(currentLinearVelocity.length() > speedLimitMS){
            // violating the speed limit
            return SPEED_VIOLATION_TYPE.MAX_SPEED;
        }

        return SPEED_VIOLATION_TYPE.NONE;
    }

    /**
     * Return whether the current learner's speed is above the speed limit (or below the min
     * speed limit if provided) for this condition
     *
     * @param currentGeolocationTimestamp the timestamp of the current location information to assess,
     * used to determine speed.
     * @param currentGeolocation the current location of the learner, used to determine speed.
     * @return true if the learner's velocity is above the speed limit (or below the min speed limit
     * if provided)
     */
    private boolean evaluateCondition(long currentGeolocationTimestamp, Geolocation currentGeolocation){

        if(lastGeolocation == null){
            lastGeolocation = currentGeolocation;
            lastGeoLocationTimestamp = currentGeolocationTimestamp;
            return false;
        }

        Double speed = currentGeolocation.getSpeed();
        if(speed != null && speed >= 0){
            // as of 4/5/19 the Android version we used for the gift mobile app wasn't providing
            // the speed value.  Once it does we need to determine what the units are and if they need
            // to be converted to m/s for this comparison
            return speed <= minSpeedLimitMS && speed > speedLimitMS;
        }

        //
        // calculate speed based on distance traveled
        //
        double distanceTraveledMeters = CoordinateUtil.getInstance().distance(currentGeolocation.getCoordinates(), lastGeolocation.getCoordinates());
        if(distanceTraveledMeters == 0){
            return false;
        }

        double timeElapsedSeconds = (currentGeolocationTimestamp - lastGeoLocationTimestamp) / 1000.0;
        if(timeElapsedSeconds == 0){
            return false;
        }

        //update for next check
        lastGeolocation = currentGeolocation;
        lastGeoLocationTimestamp = currentGeolocationTimestamp;

        double currentMS = distanceTraveledMeters / timeElapsedSeconds;
        return currentMS <= minSpeedLimitMS && currentMS > speedLimitMS;
    }

    /**
     * Notify the parent concept to this condition that the condition has a new assessment
     * outside the handle training app game state method call (i.e. because the violation timer task fired)
     */
    private void sendAsynchAssessmentNotification(){
        sendAssessmentEvent();
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
    public void stop() {
        super.stop();

        if(speedLimitViolationTimer != null) {

            //clean up any timers that are still active when this condition is stopped
            speedLimitViolationTimer.cancel();
            speedLimitViolationTimer = null;
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SpeedLimitCondition: ");
        sb.append(super.toString());
        sb.append(", speedLimit(m/s) = ").append(speedLimitMS);
        sb.append(", min speedLimit(m/s) = ").append(minSpeedLimitMS);
        sb.append(", minViolationDuration(ms) = ").append(minAssessmentDurationMS);
        sb.append("]");

        return sb.toString();
    }

    /**
     * Used to extend the {@link AssessmentWrapper} by adding speed limit specific
     * attributes that need to be tracked on a per assessed team member basis.
     * 
     * @author mhoffman
     *
     */
    private class SpeedLimitAssessmentWrapper extends AssessmentWrapper{        

        /** the violation type that associates with the assessment value */
        private SPEED_VIOLATION_TYPE speedViolationType;

        /** whether the team member has reached the min speed */
        private boolean hasEnteredSpeedBounds = false;
        
        /**
         * Set attribute
         *
         * @param teamMember the team member being assessed.  Can't be null.
         */
        public SpeedLimitAssessmentWrapper(TeamMember<?> teamMember){
            super(teamMember);
        }
        
        /**
         * Set the current assessment for this team member for this condition.
         *
         * @param assessment can't be null.
         * @param speedViolationType the enumerated type of violation associated with the assessment. Can't be null.
         */
        public void updateAssessment(AssessmentLevelEnum assessment, SPEED_VIOLATION_TYPE speedViolationType){
            super.updateAssessment(assessment);
            
            if(speedViolationType == null){
                throw new IllegalArgumentException("The violation type is null.");
            }

            this.speedViolationType = speedViolationType;
        }        

        /**
         * Return whether the team member has reached the min speed limit (i.e. increased
         * speed to enter the speed condition bounds.
         * @return true if the team members speed has entered the min->max range.
         */
        public boolean hasEnteredSpeedBounds() {
            return hasEnteredSpeedBounds;
        }

        /**
         * Set whether the team member has reached the min speed limit (i.e. increased
         * speed to enter the speed condition bounds.
         * @param hasEnteredSpeedBounds true if the team members speed has entered the min->max range.
         */
        public void setHasEnteredSpeedBounds(boolean hasEnteredSpeedBounds) {
            this.hasEnteredSpeedBounds = hasEnteredSpeedBounds;
        }

        /**
         * Return the violation type that associates with the assessment value
         * @return will be null if {@link #updateAssessment(AssessmentLevelEnum)} has not been called.
         */
        public SPEED_VIOLATION_TYPE getSpeedViolationType(){
            return speedViolationType;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[SpeedLimitAssessmentWrapper: ");
            builder.append(super.toString());
            builder.append(", hasEnteredSpeedBounds = ");
            builder.append(hasEnteredSpeedBounds());
            builder.append(", violationType = ");
            builder.append(getSpeedViolationType());
            builder.append("]");
            return builder.toString();
        }
        
    }
    /**
     * This class is the timer task which runs at the appropriately scheduled date.  It will
     * cause a change in assessment when the speed limit has been violated long enough as set by the author.
     *
     * @author mhoffman
     *
     */
    private class SpeedLimitViolationTimeTask extends TimerTask{

        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("speed limit violation timer task fired.");
            }

            AssessmentLevelEnum level = null;
            Set<TeamMember<?>> newViolators = new HashSet<>();
            synchronized(teamMemberRefAssessmentMap){

                long now = System.currentTimeMillis();

                // check the assessment map for the lowest expectation result
                for(AssessmentWrapper assessmentWrapper : teamMemberRefAssessmentMap.values()){

                    AssessmentLevelEnum assessment = assessmentWrapper.getAssessment();
                    if(assessment == null){
                        continue;
                    }

                    Long assessmentTime = assessmentWrapper.getLastAssessmentTime();
                    if(now - assessmentTime >= minAssessmentDurationMS){
                        // this assessment has been long enough

                        if(assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                            // at any point, this is the lowest assessment possible
                            level = assessment;
                            
                            // is this a new violator
                            if(assessment != assessmentWrapper.getLastAssessment()){
                                newViolators.add(assessmentWrapper.getTeamMember());
                            }
                            break;
                        }else if(level == null){
                            level = assessment;
                        }else if(level == AssessmentLevelEnum.ABOVE_EXPECTATION && assessment == AssessmentLevelEnum.AT_EXPECTATION){
                            // set to lower assessment
                            level = assessment;
                        }
                    }
                }
            }

            if(level == null){
                //unable to determine, could be because no assessment has been taking place long enough
                return;
            }

            //update assessment explanation
            boolean explanationChanged = setAssessmentExplanation();
            
            if(!newViolators.isEmpty()){
                scoringEventStarted(newViolators.toArray(new TeamMember<?>[newViolators.size()]));
            }

            if(level != getAssessment()) {
                if(logger.isDebugEnabled()){
                    logger.debug("changing assessment level to "+level+" from "+getAssessment()+" with explanation of '"+getAssessmentExplanation()+"' for "+this);
                }

                updateAssessment(level);
                sendAsynchAssessmentNotification();
            }else if(explanationChanged){
                sendAsynchAssessmentNotification();
            }

        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder("[SpeedLimitViolationTimeTask: ");
            sb.append(" team members = {");
            for(AssessmentWrapper assessmentWrapper : teamMemberRefAssessmentMap.values()){
                sb.append("(").append(assessmentWrapper.getTeamMember()).append(" : ").append(assessmentWrapper.getAssessment()).append("), ");
        }
            sb.append("}");
            sb.append("]");

            return sb.toString();
    }
}
}
