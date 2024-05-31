/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.io.Serializable;
import java.util.List;

/**
 * This inner class contains information about an end trigger that fired causing
 * a task/scenario to end.
 * 
 * @author mhoffman
 *
 */
public class EndTriggerInformation{
	
    /** the name of the strategy with the end trigger activities to execute */
    private static final String STRATEGY_NAME = "End Trigger strategy";
    
    /** contains a collection of actions to apply when this trigger evaluates to true.  */
    private final generated.dkf.Strategy strategy = new generated.dkf.Strategy();
    
    /** wrapper around the strategy, needed to abstract the different requests the domain assessment logic can make */
    private final AssessmentStrategy domainActions = new AssessmentStrategy(strategy);
    
    /** the trigger that fired */
    private AbstractTrigger trigger;
    
    /** how long to display the message (or nothing) before ending the task/scenario */
    private Float delaySeconds;
    
    /**
     * Set the trigger causing the task/scenario to end.
     * 
     * @param trigger can't be null.
     */
    public EndTriggerInformation(AbstractTrigger trigger){
        setTrigger(trigger);
        setDelay(trigger.getTriggerDelay());
        addDomainActions(trigger.getDomainActions().getStrategy().getStrategyActivities());
        
        strategy.setName(STRATEGY_NAME);
    }

    /**
     * Return the collection of actions to apply when the trigger is evaluated to true.
     * This could have been created by the course author or programmatically.</br>
     * for Start Triggers:  These actions will be shown after the delay value duration.  
     * This is useful for showing a message that contains instructions for a task.</br>
     * for End Triggers: These actions will be shown before the delay value duration.
     * This is useful for showing a message before the next course object is shown.</br>
     * 
     * @return can be empty but not null.
     */
    public AssessmentStrategy getDomainActions(){
        return domainActions;
    }
    
    /**
     * Add the action to the collection of actions to apply when the trigger is evaluated to true.
     * This could have been created by the course author or programmatically.</br>
     * for Start Triggers:  These actions will be shown after the delay value duration.  
     * This is useful for showing a message that contains instructions for a task.</br>
     * for End Triggers: These actions will be shown before the delay value duration.
     * This is useful for showing a message before the next course object is shown.</br>
     * 
     * @param domainAction the action to add, if null than nothing happens
     */
    public void addDomainAction(Serializable domainAction){
        
        if(domainAction == null){
            return;
        }
        
        domainActions.getStrategy().getStrategyActivities().add(domainAction);
    }
    
    /**
     * Add the actions to the collection of actions to apply when the trigger is evaluated to true.
     * This could have been created by the course author or programmatically.</br>
     * for Start Triggers:  These actions will be shown after the delay value duration.  
     * This is useful for showing a message that contains instructions for a task.</br>
     * for End Triggers: These actions will be shown before the delay value duration.
     * This is useful for showing a message before the next course object is shown.</br>
     * 
     * @param domainActions the actions to add, if null or empty than nothing happens
     */
    public void addDomainActions(List<Serializable> domainActions){
        
        if(domainActions == null){
            return;
        }
        
        for(Serializable domainAction : domainActions){
            addDomainAction(domainAction);
        }
    }

    /**
     * Return the trigger that is causing the task/scenario to end.
     * 
     * @return won't be null
     */
    public AbstractTrigger getTrigger() {
        return trigger;
    }

    private void setTrigger(AbstractTrigger trigger) {
        
        if(trigger == null){
            throw new IllegalArgumentException("The trigger can't be null.");
        }
        this.trigger = trigger;
    }
    
    /**
     * Set how long to wait before ending the task/scenario.
     * This can be useful if you want to display the message and give the user enough time to read it 
     * before possibly ending the scenario, starting another task, displaying other feedback, etc.
     * 
     * @param delaySeconds amount of seconds to delay.  Must be a positive number.
     */
    private void setDelay(Float delaySeconds){
        
        if(delaySeconds != null && delaySeconds < 0){
            throw new IllegalArgumentException("The delay seconds can't be negative.");
        }
        
        this.delaySeconds = delaySeconds;
    }
    
    /**
     * Return the delay amount in seconds to delay this task/scenario from ending.
     * 
     * @return amount of seconds to delay.  Can be null or a positive number.
     */
    public Float getDelay(){
        return this.delaySeconds;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EndTriggerInformation: ");
        
        sb.append("trigger = ").append(getTrigger());
        
        sb.append(", strategy = [");
        sb.append(" name = ").append(domainActions.getStrategy().getName());
        sb.append(", size = ").append(domainActions.getStrategy().getStrategyActivities().size());
        sb.append("]");
        
        if(delaySeconds != null){
            sb.append(", delay (sec) = ").append(getDelay());
        }
        
        sb.append("]");
        return sb.toString();
    }
    
}
