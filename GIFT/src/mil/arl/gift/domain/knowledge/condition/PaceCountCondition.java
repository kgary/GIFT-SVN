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
import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.PaceCountEnded;
import mil.arl.gift.common.PaceCountStarted;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.net.api.message.Message;

/**
 * A condition that waits for the learner to start tracking their traveling pace and performs an assessment once
 * they stop tracking their pace based on the total distance traveled.
 * 
 * @author nroberts
 */
public class PaceCountCondition extends AbstractCondition {
    
    private static Logger logger = LoggerFactory.getLogger(PaceCountCondition.class);
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.UNKNOWN;
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.GEOLOCATION);
        simulationInterests.add(MessageTypeEnum.LEARNER_TUTOR_ACTION);
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.CompletionTime.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = 
            new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "PaceCount.GIFT Domain condition description.html"), "Pace Count");
    
    /** the learner actions needed to be shown to the learner for this condition to assess the learner */
    private static final Set<generated.dkf.LearnerActionEnumType> LEARNER_ACTIONS = new HashSet<>(2);
    static{
        LEARNER_ACTIONS.add(generated.dkf.LearnerActionEnumType.START_PACE_COUNT);
        LEARNER_ACTIONS.add(generated.dkf.LearnerActionEnumType.END_PACE_COUNT);
    }
    
    /** 
     * The total cumulative distance that the learner has traveled since the start of this condition. This expresses the total
     * number of meters that the learner has traveled on their way to the destination, rather than their absolute displacement 
     * from their starting position.
     */
    private volatile double cumulativeDistanceTraveled = 0d;
    
    /** The learner's last received geolocation. Used to calculate the distance to the next received geolocation. */
    private volatile Point3d lastLocation = null;
    
    /** The input that defines the distance that the course author expects the learner to travel and the threshold for that distance. */
    private generated.dkf.PaceCountCondition paceCountInput;
    
    /** Whether or not this condition is currently accumulating the learner's traveled distance */
    private boolean isCountingPace = false;
    
    /** Whether or not the learner has reached the threshold necessary for a positive assessment*/
    private boolean hasReachedThreshold = false;
    
    /** Used to evaluate the condition based on current coordinates. */
    private Point3d cachedPoint = new Point3d();
    
    /**
     * Default constructor - required for authoring logic
     */
    public PaceCountCondition(){
        super(DEFAULT_ASSESSMENT);
    }
    
    /**
     * Constructor with input that that defines the distance that the course author expects the 
     * learner to travel and the threshold for that distance
     * 
     * @param input some input that defines the distance that the course author expects the 
     * learner to travel and the threshold for that distance
     */
    public PaceCountCondition(generated.dkf.PaceCountCondition input){
        this();
        
        this.paceCountInput = input;
        
        if(input.getTeamMemberRef() != null){
            setTeamMembersBeingAssessed(new generated.dkf.TeamMemberRefs());
            getTeamMembersBeingAssessed().getTeamMemberRef().add(input.getTeamMemberRef());
        }
    }


    @Override
    public boolean handleTrainingAppGameState(Message message) {
        
        if(message.getPayload() instanceof Geolocation && isCountingPace){
            
            Geolocation geolocation = (Geolocation) message.getPayload();
            
            // Update the cached point coordinates
            if (geolocation.getCoordinates() != null) {
                CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), cachedPoint);
            }
            
            if(lastLocation == null) {
                
                if(logger.isInfoEnabled()) {
                    logger.info("Learner's pace count starting location is " + geolocation);
                }
                
                //set the learner's starting location
                CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), lastLocation);
                
            } else if(geolocation.getCoordinates() != null 
                    && lastLocation != null
                    && !lastLocation.equals(cachedPoint)) {

                /*
                 * Calculate the distance between the last location received and the current one, then add the 
                 * result to the cumulative distance traveled.
                 */
                cumulativeDistanceTraveled += lastLocation.distance(cachedPoint);
                
                //update the last location received
                lastLocation = cachedPoint;
            }
            
            if(logger.isDebugEnabled()) {
                logger.debug("Current pace count location is " + geolocation
                        + ". Cumulative distance traveled: " + cumulativeDistanceTraveled + " meters.");
            }
            
            double expectedDistance = paceCountInput.getExpectedDistance();
            double distanceThreshold = paceCountInput.getDistanceThreshold();
            
            if(!hasReachedThreshold &&
                    (expectedDistance - distanceThreshold) <= cumulativeDistanceTraveled 
                        && cumulativeDistanceTraveled <= (expectedDistance + distanceThreshold)) {
                
                hasReachedThreshold = true;
                
                /*
                 * For testing purposes, allow authors to detect when the learner has walked within positive
                 * assessment range by re-throwing the AT_EXPECTATION assessment
                 */
                updateAssessment(AssessmentLevelEnum.AT_EXPECTATION);
                
                return true;
            }
            
        } else if(message.getPayload() instanceof EntityState && isCountingPace){
            
            EntityState state = (EntityState) message.getPayload();
            
            TeamMember<?> teamMember = isConditionAssessedTeamMember(state.getEntityID());
            if (teamMember == null) {
                return false;
            }
            
            Point3d location = state.getLocation();
            
            if(lastLocation == null) {
                
                if(logger.isInfoEnabled()) {
                    logger.info("Learner's pace count starting location is " + state);
                }
                
                //set the learner's starting location
                lastLocation = location;
                
            } else if(location != null 
                    && lastLocation != null 
                    && !lastLocation.equals(location)) {
                      
                //calculate the distance between the last location received and the current one
                double distance = lastLocation.distance(location);
                
                cumulativeDistanceTraveled += distance;
            
                /*
                 * Update the last location received.
                 * 
                 * Nick: We NEED to call Point3d.clone() here to store the current coordinates for later, otherwise, 
                 * lastLocation's coordinates will be updated in real-time by the dead reckoning timer thread used by
                 * mil.arl.gift.domain.EntityTable.
                 */
                lastLocation = (Point3d) location.clone(); 
            }
            
            if(logger.isDebugEnabled()) {
                logger.debug("Current pace count location is " + state
                        + ". Cumulative distance traveled: " + cumulativeDistanceTraveled + " meters.");
            }
            
            double expectedDistance = paceCountInput.getExpectedDistance();
            double distanceThreshold = paceCountInput.getDistanceThreshold();
            
            if(!hasReachedThreshold &&
                    ((expectedDistance - distanceThreshold) <= cumulativeDistanceTraveled 
                        && cumulativeDistanceTraveled <= (expectedDistance + distanceThreshold))) {
                
                hasReachedThreshold = true;
                
                /*
                 * For testing purposes, allow authors to detect when the learner has walked within positive
                 * assessment range by re-throwing the AT_EXPECTATION assessment
                 */
                updateAssessment(AssessmentLevelEnum.AT_EXPECTATION);
                
                return true;
            }
            
        } else if(message.getMessageType() == MessageTypeEnum.LEARNER_TUTOR_ACTION){
            
            LearnerTutorAction action = (LearnerTutorAction)message.getPayload();
            AbstractLearnerTutorAction actionData = action.getAction();
            
            if(actionData instanceof PaceCountStarted){
                
                isCountingPace = true;
                
                cumulativeDistanceTraveled = 0d;
                hasReachedThreshold = false;
                
                if(logger.isInfoEnabled()) {
                    logger.info("Learner has started pace count.");
                }
            
            } else if(actionData instanceof PaceCountEnded && isCountingPace) {
                
                isCountingPace = false;
                
                if(logger.isInfoEnabled()) {
                    logger.info("Learner has ended pace count. Distance traveled is " + cumulativeDistanceTraveled + " meters.");
                }
                
                //assess whether or not the learner traveled the expected distance
                double expectedDistance = paceCountInput.getExpectedDistance();
                double distanceThreshold = paceCountInput.getDistanceThreshold();
                
                if((expectedDistance - distanceThreshold) <= cumulativeDistanceTraveled
                        && cumulativeDistanceTraveled <= (expectedDistance + distanceThreshold)) {
                    
                    if(logger.isInfoEnabled()) {
                        logger.info("Learner has satisfied the pace count criteria.");
                    }
                    
                    updateAssessment(AssessmentLevelEnum.ABOVE_EXPECTATION);
                    
                } else {
                    
                    if(logger.isInfoEnabled()) {
                        logger.info("Learner has failed to satisfy the pace count criteria.");
                    }
                    
                    updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
                }
                
                conditionCompleted();
                
                return true;
            }
        }
        
        return false;
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }
    
    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return LEARNER_ACTIONS;
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
        sb.append("[PaceCountCondition: ");
        sb.append(super.toString());
        sb.append(", isCountingPace = ").append(isCountingPace);
        sb.append(", cumulativeDistanceTraveled = ").append(cumulativeDistanceTraveled);
        sb.append(", lastLocation = ").append(lastLocation);
        sb.append(", hasReachedThreshold = ").append(hasReachedThreshold);
        sb.append("]");
        
        return sb.toString();
    }

}
