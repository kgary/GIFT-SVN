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

import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.net.api.message.Message;

/**
 * This trigger is used to determine if the learner entity is dead.
 * 
 * @author mhoffman
 *
 */
public class KKILLTrigger extends AbstractTrigger {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KKILLTrigger.class);
    
    /** this is the messages that is shown to the learner in the TUI */
    private static final String REASON = "You have died, therefore this lesson is over.";
    
    /** the name of the strategy with the feedback message */
    private static final String STRATEGY_NAME = "Learner died feedback";
    
    /** build the object used to present a message to the learner in the tutor */
    private static final AssessmentStrategy DOMAIN_ACTIONS;
    private static final generated.dkf.Strategy strategy = new generated.dkf.Strategy();    
    static{
        
        final generated.dkf.InstructionalIntervention instructionalIntervention = new generated.dkf.InstructionalIntervention();
        final generated.dkf.Feedback feedback = new generated.dkf.Feedback();
        final generated.dkf.Message message = new generated.dkf.Message();
        message.setContent(REASON);
        feedback.setFeedbackPresentation(message);
        instructionalIntervention.setFeedback(feedback);  
        generated.dkf.StrategyHandler strategyHandler = new generated.dkf.StrategyHandler();
        strategyHandler.setImpl("domain.knowledge.strategy.DefaultStrategyHandler");
        instructionalIntervention.setStrategyHandler(strategyHandler);
        strategy.setName(STRATEGY_NAME);
        strategy.getStrategyActivities().add(instructionalIntervention);
        DOMAIN_ACTIONS = new AssessmentStrategy(strategy);
    }
    
    /** 
     * how many seconds before the scenario will end and the next course object should be shown, 
     * you want to give enough time to read the reason message 
     */
    private static final float DELAY = 10.f;
    
    /** only allow this trigger to fire once, otherwise the feedback is delivered a lot */
    private boolean hasFired = false;
    
    /**
     * Class constructor
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     */
    public KKILLTrigger(String triggerName){
        super(triggerName);
    }
    
    @Override
    public boolean shouldActivate(Message message) {
        
        // only fire once
        if(hasFired){
            return false;
        }
        
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            EntityState es = (EntityState)message.getPayload();
            
            TeamMember<?> triggerTeamMember = getTriggerTeamMember();
            if(triggerTeamMember == null){
                // needs the learner team member only
                setTriggersTeamMember(getTeamOrganization() != null ? getTeamOrganization().getLearnerTeamMember() : null);
            }
            
            //only check if the entity state message describes the learner
            if(!isTriggerTeamMember(es.getEntityID())){
                return false;
            }
            
            if(es.getAppearance().getDamage() != DamageEnum.HEALTHY){

                if(logger.isInfoEnabled()){
                    logger.info("Trigger has been activated because entity's ("+getTriggerTeamMember()+") health is = "+es.getAppearance().getDamage());
                }
                hasFired = true;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public AssessmentStrategy getDomainActions(){
        return DOMAIN_ACTIONS;
    }
    
    @Override
    public float getTriggerDelay(){
        return DELAY;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[KKillTrigger: "); 
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }

}
