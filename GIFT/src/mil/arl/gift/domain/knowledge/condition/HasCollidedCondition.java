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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.Collision;
import mil.arl.gift.net.api.message.Message;

/**
 * Assesses whether a collision message was received.
 * 
 * @author mhoffman
 *
 */
public class HasCollidedCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(HasCollidedCondition.class);
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.UNKNOWN;
    
    /** the amount of time that must elapse to consider the collision to be a new collision event */
    private static final Long TIME_BETWEEN_COLLISIONS = 1000L;
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.COLLISION);
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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "HasCollided.GIFT Domain condition description.html"), "Detect Collision");
    
    /** 
     * the time of the last notified collision, used to determine if the collision is a new event or
     * part of the same collision event
     */
    private Long lastCollisionTime = System.currentTimeMillis();
    
    /**
     * Default constructor - required for authoring logic
     */
    public HasCollidedCondition(){
        super(DEFAULT_ASSESSMENT);
    }

    /**
     * @deprecated GenericConditionInput has been replaced in favor of NoConditionInput
     * when the condition input is not used. Use {@link #HasCollidedCondition(generated.dkf.NoConditionInput)}
     * instead. 
     * 
     * @param input some input parameters (nothing supported yet)
     */
    @Deprecated
    public HasCollidedCondition(generated.dkf.GenericConditionInput input){
        this();
    }
    
    /**
     * Constructor with input parameters
     * 
     * @param input some input parameters (nothing supported yet)
     */
    public HasCollidedCondition(generated.dkf.NoConditionInput input){
        this();
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }


    @Override
    public boolean handleTrainingAppGameState(Message message) {
        
        if(message.getPayload() instanceof Collision){
            
            AssessmentLevelEnum assessment = null;
            
            //treat a block of collision messages as the same event
            if((lastCollisionTime + TIME_BETWEEN_COLLISIONS) < message.getTimeStamp()){
            
                //for now don't analyze the collision information
                assessment = AssessmentLevelEnum.AT_EXPECTATION;
                
                if(logger.isDebugEnabled()){
                    logger.debug("Received collision of "+message.getPayload()+".");
                }
            }
            
            lastCollisionTime = message.getTimeStamp();
            
            if(assessment != null){
                
                //count the number of collisions
                scoringEventStarted();
                
                updateAssessment(assessment);
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
        sb.append("[HasCollidedCondition: ");
        sb.append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
