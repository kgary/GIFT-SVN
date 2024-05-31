/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.ExternalAttributeEnumType;
import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VariableInfo;
import mil.arl.gift.common.ta.state.VariablesStateResult;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition is used to routinely request a variable value from an external application for
 * one or more team members.
 * @author mhoffman
 *
 */
public class RequestExternalAttributeCondition extends AbstractCondition {
    
    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(RequestExternalAttributeCondition.class);
    
    /** how long to wait in milliseconds before requesting another update on the variables
     * Just picked some small amount of time.  For gunnery targets sub-second is needed for table scoring. */
    private static final int REQUEST_DELAY_MS = 300;

    /** The message types that this condition is interested in. */
    private static final List<MessageTypeEnum> simulationInterests = Arrays.asList(MessageTypeEnum.VARIABLE_STATE_RESULT, MessageTypeEnum.ENTITY_STATE);
    
    /** Information about the purpose of this condition */
    private static final ConditionDescription description = new FileDescription(
            Paths.get("docs", "conditions", "RequestExternalAttribute.GIFT Domain condition description.html").toFile(),
            "Request External Attribute");
    
    /** the condition inputs */
    private generated.dkf.RequestExternalAttributeCondition input;
    
    /** used to schedule when to send the next external attribute value request */
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    
    /** representing pending completion of the task of requesting the external attribute
     *  and whose get() method will return null upon completion */
    private ScheduledFuture<?> futureRequest = null;
    
    /**
     * Empty constructor required for authoring logic to work.
     */
    public RequestExternalAttributeCondition() {
        if (logger.isInfoEnabled()) {
            logger.info("RequestExternalAttributeCondition()");
        }
    }
    
    /**
     * Set the condition inputs
     * @param input the condition inputs, can't be null
     */
    public RequestExternalAttributeCondition(generated.dkf.RequestExternalAttributeCondition input){
        super(AssessmentLevelEnum.UNKNOWN);
        if (logger.isInfoEnabled()) {
            logger.info("RequestExternalAttributeCondition(" + input + ")");
        }

        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }
        
        setTeamMembersBeingAssessed(input.getTeamMemberRefs());
        
        this.input = input;
    }
    
    /**
     * Responsible to making the request for the appropriate variables of the appropriate individuals
     */
    private Runnable sendRequest = new Runnable(){
        
        @Override
        public void run(){
            if(input == null){
                return;
            }
            
            try{
                VariablesStateRequest request = new VariablesStateRequest(conditionInstanceID.toString());
                VariableInfo varInfo = new VariableInfo(getAssessedEntityMarkings());
                
                ExternalAttributeEnumType type = input.getAttributeType();
                VARIABLE_TYPE varType = null;
                switch(type){
                case ANIMATION_PHASE:
                    varType = VARIABLE_TYPE.ANIMATION_PHASE;
                    varInfo.setVarName(input.getAttributeName());
                    break;
                case VARIABLE:
                    varType = VARIABLE_TYPE.VARIABLE;
                    varInfo.setVarName(input.getAttributeName());
                    break;
                
                case WEAPON_STATE:
                    // doesn't require a variable name
                    varType = VARIABLE_TYPE.WEAPON_STATE;
                    break;
                }
                
                if(varType == null){
                    return;
                }
                request.setTypeVariable(varType, varInfo);
                conditionActionInterface.trainingApplicationRequest(request);
            }catch(Exception e){
                logger.error("There was a problem while trying to send the request for the value of an external attribute.\n"+this, e);
            }
        }
    };

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        if (logger.isDebugEnabled()) {
            logger.debug("handleTrainingAppGameState(" + message + ")");
        }
        
        if(blackboard == null){
            setBlackboard(message);
        }

        final MessageTypeEnum msgType = message.getMessageType();
        if(msgType == MessageTypeEnum.VARIABLE_STATE_RESULT){
            
            VariablesStateResult result = (VariablesStateResult)message.getPayload(); 
            if(result.getRequestId().equals(conditionInstanceID.toString())){
                
                if(logger.isDebugEnabled()){
                    logger.debug("Received variables state result for this condition instance:\n"+result);
                }
                
                // send another request
                futureRequest = scheduledExecutorService.schedule(sendRequest, REQUEST_DELAY_MS, TimeUnit.MILLISECONDS);
            }
            
        }else if(msgType == MessageTypeEnum.ENTITY_STATE){
            // using the first entity state message to trigger the first request
            // Note: Originally tried to use the AbstractCondition.start method but the VBS plugin socket connection has
            // a timeout when the VBS scenario is loading cause a NACK to be sent from GW to DM and therefore the
            // variable state result message is never received above causing the next variable request message to never be sent.
            
            if(futureRequest == null){
                futureRequest = scheduledExecutorService.schedule(sendRequest, REQUEST_DELAY_MS, TimeUnit.MILLISECONDS);
            }
        }
        
        return false;
    }

    @Override
    public ConditionDescription getDescription() {
        return description;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }

    @Override
    public boolean canComplete() {
        return false;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RequestExternalAttributeCondition: ");        
        sb.append("name = ").append(input.getAttributeName());
        sb.append(", type = ").append(input.getAttributeType().name());
        sb.append(", who =\n").append(getTeamMembersBeingAssessed());
        sb.append(super.toString());
        sb.append(']');
        return sb.toString();
    }
}
