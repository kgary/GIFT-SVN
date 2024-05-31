/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;


import java.io.Serializable;

import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.dkf.team.TeamOrganization;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.net.api.message.Message;

/**
 * This base class is by all trigger classes to clearly define the needed methods. 
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractTrigger {
    
    /** 
     * the team member that the trigger is analyzing during runtime.
     * The value can be null if, for example, the training application doesn't need to identify the 
     * learner amongst other players.  It can also be null if the team member has
     * yet to be found based on the identification criteria set in the triggers parameters.
     */
    private TeamMember<?> triggersTeamMember = null;
    
    /**
     * the team organization that contains all team and team members as authored in the real time assessment.
     * The value can be null if, for example, the training application doesn't need to identify the 
     * learner amongst other players.
     */
    private TeamOrganization teamOrganization = null;
    
    /**
     * (optional) seconds to delay the firing of this trigger instance.
     * This is useful for situations like needing to leave feedback visible long enough to 
     * read, therefore you don't want the scenario to end to quickly and clear the feedback message
     * from the screen.
     */
    private float triggerDelay = 0;
    
    /**
     * contains a collection of actions to apply when this trigger evaluates to true.  
     */
    private final generated.dkf.Strategy strategy = new generated.dkf.Strategy();
    
    /**
     * a wrapper around the strategy of actions to apply, needed to abstract the types of requests
     * the domain assessment logic might request from the domain session.
     */
    private final AssessmentStrategy domainActions = new AssessmentStrategy(strategy);
    
    /**
     * Default constructor. Use if there is no trigger delay.
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     */
    public AbstractTrigger(String triggerName){    
        
        if(StringUtils.isBlank(triggerName)){
            throw new IllegalArgumentException("The trigger name can't be null or empty");
        }
        strategy.setName(triggerName);
    }
    
    /**
     * Set the optional trigger delay for this trigger
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty. 
     * @param triggerDelay amount of time in seconds to delay the firing of this trigger.  Must be
     * a positive number.
     */
    public AbstractTrigger(String triggerName, float triggerDelay){ 
    	this(triggerName);
        setTriggerDelay(triggerDelay);
    }
    
    /**
     * Set the team organization that contains all team and team members as authored in the real time assessment.
     * Should only be called once.
     * 
     * @param teamOrganization The value can be null if, for example, the training application doesn't need to identify the 
     * learner amongst other players.
     * @throws RuntimeException when there is a team member reference used by this trigger but is not in the
     * provided team organization provided.
     */
    public void setTeamOrganization(TeamOrganization teamOrganization) throws RuntimeException{
        this.teamOrganization = teamOrganization;
        
        // check strategy team member references
        if(!domainActions.getStrategy().getStrategyActivities().isEmpty()){
            
            for(Serializable strategy : domainActions.getStrategy().getStrategyActivities()){
                
                if(strategy instanceof generated.dkf.InstructionalIntervention){
                    generated.dkf.InstructionalIntervention intervention = (generated.dkf.InstructionalIntervention)strategy;

                    generated.dkf.Feedback feedback = intervention.getFeedback();
                    for(generated.dkf.TeamRef teamMemberRef : feedback.getTeamRef()){
                        
                        if(teamOrganization.getTeamElementByName(teamMemberRef.getValue()) == null){
                            throw new RuntimeException("found a team organization reference of '"+teamMemberRef.getValue()+"' that isn't in the team organization.");
                        }
                    }

                }
            }
            
        }

            
        // check triggers team member references
        if(triggersTeamMember != null){
            
            if(teamOrganization == null){
                throw new RuntimeException("found a team organization reference but the team organization is empty.");
            }            
                
            if(teamOrganization.getTeamElementByName(triggersTeamMember.getName()) == null){
                throw new RuntimeException("found a team organization reference of '"+triggersTeamMember.getName()+"' that isn't in the team organization.");
            }

        }
    }
    
    /**
     * Return the team organization that contains all team and team members as authored in the real time assessment.
     * @return the authored team organization and current identification information for team members.
     */
    protected TeamOrganization getTeamOrganization(){
        return teamOrganization;
    }
    
    /**
     * Return the team member that the trigger is analyzing during runtime. This should be
     * set by the trigger itself once the appropriate team member is found.
     * 
     * @return The value can be null if, for example, the training application doesn't need to identify the 
     * learner amongst other players.  It can also be null if the team member has
     * yet to be found based on the identification criteria set in the triggers parameters.
     */
    protected TeamMember<?> getTriggerTeamMember(){
        return triggersTeamMember;
    }
    
    /**
     * Set team member that the trigger is analyzing during runtime. This should be
     * set by the trigger itself once the appropriate team member is found.  Should only
     * be called once.
     * 
     * @param triggersTeamMember can be null if the team member is not needed or has still
     * not been found.
     */
    protected void setTriggersTeamMember(TeamMember<?> triggersTeamMember){
        this.triggersTeamMember = triggersTeamMember;
    }
    
    /**
     * Whether the provided entity identifier matches with the trigger's known entity identifier.
     * Note: this currently just checks the entity id and not the simulation address information.
     * 
     * @param anEntityIdentifier the identifier information to compare against the identifier known to 
     * be the associated with this trigger.
     * @return true iff the trigger's entity id matches the provided entity identifier entity id value.
     */
    public boolean isTriggerTeamMember(EntityIdentifier anEntityIdentifier){        
        return triggersTeamMember != null && triggersTeamMember.getEntityIdentifier() != null &&
                anEntityIdentifier != null && anEntityIdentifier.getEntityID() == triggersTeamMember.getEntityIdentifier().getEntityID();
    }
    
    /**
     * Whether or not this trigger is a scenario ending trigger when fired
     * 
     * @return boolean
     */
    public boolean isScenarioEndingTrigger(){
        return false;
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
     * Set the trigger delay for this trigger.
     * 
     * @param triggerDelay amount of time in seconds to delay the firing of this trigger.  Must be
     * a positive number.
     */
    public void setTriggerDelay(float triggerDelay){
        
        if(triggerDelay <= 0){
            throw new IllegalArgumentException("The trigger delay of "+triggerDelay+" is not a positive number.");
        }
        
        this.triggerDelay = triggerDelay;
    }
    
    /**
     * Return how long to wait before ending the task.
     * This can be useful if you want to display the message and give the user enough time to read it 
     * before possibly ending the scenario, starting another task, displaying other feedback, etc.
     * 
     * @return must be a positive number (seconds)
     */
    public float getTriggerDelay(){
        return triggerDelay;
    }

    /**
     * Use the message contents to determine if the trigger should be fired or not.
     * 
     * @param message the message to analyze
     * @return true iff the message causes the trigger to be fired
     */
    public boolean shouldActivate(Message message){
        return false;
    }
    
    /**
     * Use the concept to determine if the trigger should be fired or not.
     * 
     * @param concept the concept to analyze
     * @return true iff the concept causes the trigger to be fired
     */
    public boolean shouldActivate(Concept concept){
        return false;
    }
    
    /**
     * Use the strategy being applied that could activate a trigger.
     * @param appliedStratergyName the strategy being applied to check if a trigger is following that strategy
     * @return true iff the strategy causes the trigger to be fired
     */
    public boolean shouldActivate(String appliedStratergyName){
        return false;
    }

    /**
     * Use the tasks to determine if the trigger should be fired or not.
     * 
     * @param changedTask the task that was changed. This task is analyzed to
     *        determine whether or not the 'taskToActivate' should be activated.
     *        Shouldn't be null.
     * @param taskToActivate the task that has the potential to become
     *        activated. Can be null iff the trigger is not being applied to a
     *        {@link Task}.
     * @return true iff the changedTask causes the trigger to be fired
     */
    public boolean shouldActivate(Task changedTask, Task taskToActivate) {
        return false;
    }
    
    /**
     * Initialize the condition's logic because it will now start receiving
     * game state information.
     */    
    public void initialize(){
        // no-op
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("entityId = ").append(getTriggerTeamMember()); 
        sb.append(", triggerDelay = ").append(getTriggerDelay()); 
        sb.append(", message = ").append(getDomainActions());
        sb.append(", teamMember = ").append(getTriggerTeamMember());
        return sb.toString();
    }
}
