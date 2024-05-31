/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.net.api.message.Message;

/**
 * This trigger is used to determine if the training application ended the scenario prematurely due
 * to an error.  It does this by looking at the StopFreeze message for one of the following reason
 * types:
 *       3   System Failure
 *       6   Stop for reset
 *       7   Stop for restart
 * 
 * @author mhoffman
 *
 */
public class TrainingAppUnexpectedStoppedTrigger extends AbstractTrigger {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TrainingAppUnexpectedStoppedTrigger.class);
    
    private static final String REASON = "The training application (or scenario) has closed unexpectedly.";
    
    /** the name of the strategy with the feedback message */
    private static final String STRATEGY_NAME = "Training application (or scenario) closed feedback";
    
    /** build the object used to present this message to the learner in the tutor */
    private static final AssessmentStrategy DOMAIN_ACTIONS;
    private static final generated.dkf.Strategy strategy = new generated.dkf.Strategy();    
    static{
        
        final generated.dkf.InstructionalIntervention instructionalIntervention = new generated.dkf.InstructionalIntervention();
        final generated.dkf.Feedback feedback = new generated.dkf.Feedback();
        final generated.dkf.Message message = new generated.dkf.Message();
        message.setContent(REASON);
        feedback.setFeedbackPresentation(message);
        instructionalIntervention.setFeedback(feedback);    
        strategy.setName(STRATEGY_NAME);
        strategy.getStrategyActivities().add(instructionalIntervention);
        DOMAIN_ACTIONS = new AssessmentStrategy(strategy);
    }
    
    /** default amount of time to wait before firing this trigger */
    private static final float DEFAULT_TRIGGER_DELAY = 5.0f;
    
    /**
     * Class constructor
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty. 
     */
    public TrainingAppUnexpectedStoppedTrigger(String triggerName){
        super(triggerName, DEFAULT_TRIGGER_DELAY);

    }
    
    @Override
    public boolean shouldActivate(Message message) {
        
        if(message.getMessageType() == MessageTypeEnum.STOP_FREEZE){
            StopFreeze stop = (StopFreeze)message.getPayload();

            switch (stop.getReason()){
                case StopFreeze.SYSTEM_FAIL:
                case StopFreeze.STOP_FOR_RESET:
                case StopFreeze.STOP_FOR_RESTART:
                    logger.info("Trigger has been activated because the StopFreeze message of "+stop+" has the appropriate reason.");
                    return true;
            }

        }
        
        return false;
    }
    
    @Override
    public boolean isScenarioEndingTrigger(){
        return true;
    }
    
    @Override
    public AssessmentStrategy getDomainActions(){
        return DOMAIN_ACTIONS;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[TrainingAppUnexpectedStoppedTrigger: "); 
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }

}
