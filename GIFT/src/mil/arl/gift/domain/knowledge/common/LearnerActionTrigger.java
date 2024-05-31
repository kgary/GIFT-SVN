/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.api.message.Message;

/**
 * A real time assessment trigger that can be used by scenario and tasks to activate/deactivate
 * when a learner uses a learner action in the tutor.
 * 
 * @author mhoffman
 *
 */
public class LearnerActionTrigger extends AbstractTrigger {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerActionTrigger.class);
    
    /** the authored learner action information */
    private generated.dkf.LearnerAction learnerAction;
    
    /**
     * Set attribute
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param learnerAction the authored learner action information to associate with this
     * trigger.  Can't be null.
     */
    public LearnerActionTrigger(String triggerName, generated.dkf.LearnerAction learnerAction) {
        super(triggerName);
        setLearnerAction(learnerAction);        
    }
    
    /**
     * Set learner action.
     * 
     * @param learnerAction the authored learner action information to associate with this
     * trigger.  Can't be null.
     */
    private void setLearnerAction(generated.dkf.LearnerAction learnerAction){
        
        if(learnerAction == null){
            throw new IllegalArgumentException("The learner action can't be null.");
        }
        
        this.learnerAction = learnerAction;
    }
    
    @Override
    public boolean shouldActivate(Message message){
        
        boolean match = false;
        if(message != null && message.getMessageType() == MessageTypeEnum.LEARNER_TUTOR_ACTION){
            
            LearnerTutorAction action = (LearnerTutorAction)message.getPayload();
            AbstractLearnerTutorAction actionData = action.getAction();
            generated.dkf.LearnerAction learnerAction = actionData.getLearnerAction();
            if(learnerAction != null){
            
                //match parameters
                if(this.learnerAction.getDisplayName().equals(learnerAction.getDisplayName()) && this.learnerAction.getType().equals(learnerAction.getType())){
                    match = true;
                    if(logger.isDebugEnabled()){
                        logger.debug("Activating "+this+" because learner action was used.");
                    }
                }else{
                    if(logger.isDebugEnabled()){
                        logger.debug("A different learner action was received with name of '"+learnerAction.getDisplayName()+"' and type "+learnerAction.getType()+
                                " when the learner action to match is named '"+this.learnerAction.getDisplayName()+"' with type "+this.learnerAction.getType());
                    }
                }
            }
        }
        
        return match;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LearnerActionTrigger: ");
        sb.append(super.toString());  
        sb.append(", learnerAction = {");
        
        //manual toString of learnerAction
        sb.append("displayName = ").append(learnerAction.getDisplayName());
        sb.append(", type = ").append(learnerAction.getType());
        if(learnerAction.getLearnerActionParams() != null){
            
            Serializable actionParams = learnerAction.getLearnerActionParams();
            if(actionParams instanceof generated.dkf.TutorMeParams){
                sb.append(", tutorMeParams = ");
                generated.dkf.TutorMeParams tutorMeParams = (generated.dkf.TutorMeParams)actionParams;
                if(tutorMeParams.getConfiguration() instanceof generated.dkf.ConversationTreeFile){
                    sb.append(" [conv. tree] ").append(((generated.dkf.ConversationTreeFile)tutorMeParams.getConfiguration()).getName());
                }else if(tutorMeParams.getConfiguration() instanceof generated.dkf.AutoTutorSKO){
                    sb.append(" [AutoTutor] ").append(((generated.dkf.AutoTutorSKO)tutorMeParams.getConfiguration()).getScript());
                }
            }else if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                sb.append(", strategyReference = ").append(strategyRef.getName());
            }
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
