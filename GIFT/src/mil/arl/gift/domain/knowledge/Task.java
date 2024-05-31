/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.domain.DomainKnowledgeManager;
import mil.arl.gift.domain.knowledge.common.AbstractLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractTrigger;
import mil.arl.gift.domain.knowledge.common.AssessmentProxyManager;
import mil.arl.gift.domain.knowledge.common.ConceptActionInterface;
import mil.arl.gift.domain.knowledge.common.ConceptLessonAssessmentHandlerInterface;
import mil.arl.gift.domain.knowledge.common.EndTriggerInformation;
import mil.arl.gift.domain.knowledge.common.ManualTrigger;
import mil.arl.gift.domain.knowledge.common.ProxyTaskAssessment;
import mil.arl.gift.domain.knowledge.common.ScenarioStartTrigger;
import mil.arl.gift.domain.knowledge.common.TaskActionInterface;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.AssessmentUpdateEventType;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.TaskAssessmentEvent;
import mil.arl.gift.domain.knowledge.common.metric.difficulty.DefaultDifficultyMetric;
import mil.arl.gift.domain.knowledge.common.metric.difficulty.DifficultyMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.stress.DefaultStressMetric;
import mil.arl.gift.domain.knowledge.common.metric.stress.StressMetricInterface;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationVarsHandler;
import mil.arl.gift.domain.knowledge.strategy.StrategyAppliedEvent;
import mil.arl.gift.net.api.message.Message;

/**
 * This class represents a domain scenario task which contains concepts that will be
 * assessed.
 * 
 * @author mhoffman
 *
 */
public class Task extends AbstractPerformanceAssessmentNode implements ConceptLessonAssessmentHandlerInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Task.class);
    
    /** the start triggers for this task */
    private List<AbstractTrigger> startTriggers;

    /** the end triggers for this task */
    private List<AbstractTrigger> endTriggers;
    
    /** the concepts directly under this task */
    private List<Concept> concepts;
    
    /** list of assessment options beyond concept assessing */
    List<AbstractLessonAssessment> assessments;

    /** mapping of GIFT message type to container of interested concepts of that message type */
    private Map<MessageTypeEnum, List<Concept>> simulationMessageToListeners = new HashMap<MessageTypeEnum, List<Concept>>();
    
    /** current task assessment */
    private ProxyTaskAssessment currentTaskAssessment;
    
    /** this is the interface which needs to be called to notify the scenario of task actions (e.g. the task has started) */
    private TaskActionInterface taskActionInterface;
    
    /** handles the concepts' concept actions*/
    private ConceptActionHandler conceptActionHandler = new ConceptActionHandler();
    
    /** flag used to indicate if the task should be check for completion */
    private boolean needCompletionCheck = false;
    
    /** if not null, then it is the current task's concept that is handling a simulation message */
    private Concept currentConceptWithSimMsg = null;
    
    /**
     * This timer is used to handle delaying the ending of this task.  Delays are often used for allowing 
     * feedback/instruction to be shown with enough time before being removed/replaced when the next course object is loaded.
     */
    private Timer delayedTaskEndTimer = null;
    
    /**
     * This timer is used to handle delaying the starting of this task.  Delays are often used to allow
     * other events to finish or even being before this task should start being assessed.  
     */
    private Timer delayedTaskStartTimer = null;
    
    /**
     * the logic used to calculate the current difficulty information for this task.  Will
     * most likely be set the first time difficulty is needed to be calculated.
     */
    private DifficultyMetricInterface difficultyMetric;
    
    /**
     * the logic used to calculate the current stress information for this task.  Will
     * most likely be set the first time stress id needed to be calculated (e.g. a strategy is applied while this task is active)
     */
    private StressMetricInterface stressMetric;
    
    /**
     * Class constructor - set attributes
     * 
     * @param nodeId - the unique performance node id of this task
     * @param name - the name of this task
     * @param startTriggers - list of triggers that can start this task.  Can be empty but not null.
     * @param endTriggers - list of triggers that can end this task.  Can't be empty.
     * @param concepts - the concepts under this task
     * @param assessments - the assessments associated with this task that can assess this task
     * @param scenarioSupport flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view.
     * @param initDifficulty the optional initial difficulty of this task.  Usually a value that is authored in the DKF.  
     * Can be null.
     * @param initStress the optional initial stress of this task.  Usually a value that is authored in the DKF.
     * Can be null.
     */
    public Task(int nodeId, String name, List<AbstractTrigger> startTriggers, List<AbstractTrigger> endTriggers, List<Concept> concepts,
            List<AbstractLessonAssessment> assessments, boolean scenarioSupport, Double initDifficulty, Double initStress){
        super(nodeId, name, assessments);
        
        if(startTriggers == null){
            throw new IllegalArgumentException("The start triggers collection can't be null.");
        }else if(CollectionUtils.isEmpty(endTriggers)){
            throw new IllegalArgumentException("The end triggers collection can't be empty.");
        }

        this.startTriggers = startTriggers;
        this.endTriggers = endTriggers; 
        
        validateStartTriggers();
        
        this.concepts = concepts;
        
        setScenarioSupportNode(scenarioSupport);
        
        //build concept assessment container
        List<UUID> conceptAssessmentIds = new ArrayList<>();
        boolean conceptHasObservedAssessment = false;
        for(Concept concept : concepts){
            if (concept.getAssessment().isContainsObservedAssessmentCondition()) {
                conceptHasObservedAssessment = true;
            }
            conceptAssessmentIds.add(concept.getAssessment().getCourseNodeId());
        }
        
        currentTaskAssessment = new ProxyTaskAssessment(name, AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), conceptAssessmentIds, nodeId, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
        currentTaskAssessment.setContainsObservedAssessmentCondition(conceptHasObservedAssessment);
        currentTaskAssessment.setScenarioSupportNode(scenarioSupport);
        currentTaskAssessment.setDifficulty(initDifficulty);
        currentTaskAssessment.setStress(initStress);
        courseNodeId = currentTaskAssessment.getCourseNodeId();
    }
    
    /**
     * Validate the start triggers for this task.
     * 
     * @throws DetailedException if there was a validation issue
     */
    private void validateStartTriggers() throws DetailedException{
        
        if(startTriggers == null){
            return;
        }
        
        boolean foundScenarioStartTrigger = false;
        for(AbstractTrigger trigger : startTriggers){
            
            if(trigger instanceof ScenarioStartTrigger){
                
                if(foundScenarioStartTrigger){
                    // this is a second scenario start trigger which isn't supported
                    throw new DetailedException("Found more than one scenario start trigger in the task '"+getName()+"'.",
                            "A task can only support up to one scenario start trigger.  Please remove additional scenario start triggers on this task.", null);
                }
                
                foundScenarioStartTrigger = true;
            }
        }
        
    }
    
    /**
     * Load the task.
     * Set the task action interface needed to communicate to the scenario.
     * 
     * @param taskActionInterface used to communicate task level events
     * @return boolean If the task was loaded 
     */
    public boolean load(TaskActionInterface taskActionInterface){

        boolean loaded = false;
        
        if(taskActionInterface != null){
            
            this.taskActionInterface = taskActionInterface;
            
            loaded = true;
        }

        return loaded;
    }
    
    @Override
    public boolean handleConversationAssessment(List<ConversationAssessment> assessments){
        
        boolean changed = super.handleConversationAssessment(currentTaskAssessment, assessments);
        
        for(Concept concept : concepts){
            changed |= concept.handleConversationAssessment(assessments);
        }
        
        return changed;
    }
    
    @Override
    public void evaluatorUpdateRequestReceived(EvaluatorUpdateRequest request) {
        
        /* update the assessment metrics */
        updatePerformanceAssessmentMetrics(request);
        if (request.getState() == PerformanceNodeStateEnum.ACTIVE) {
            handleManualStart();
        } else if (request.getState() == PerformanceNodeStateEnum.FINISHED) {
            handleManualEnd(getName() + " ended by an observer request");
        }

        /* assessment was updated so notify parents */
        fireAssessmentUpdate(true, AssessmentUpdateEventType.TASK_EVALUATOR_UPDATE);
    }
    
    @Override
    public AbstractAssessment handleTrainingAppGameState(Message message){
        
        //synchronized because the task can be deactivated by another thread
        synchronized(active){
                       
            if(!active){
                //check if the task should be activated based on the start trigger(s)
                
                if (shouldStart(message)){
                    activate();
                }                
            }
            
            //check to see if the task has been activated during this method
            //TODO: instead of using recursion or some other more sophisticated means to give the incoming message
            //      to concepts after being task activation, just use another 'if' - as this class evolves this will become something greater
            if(active && !finished){
                //this task is currently active
                                
                //DEBUG
                //Date start = new Date();
                
                //if not, provide the message to the active concepts that are interested in the particular message type
                List<Concept> interestedConcepts = simulationMessageToListeners.get(message.getMessageType());
                
                //collection used to capture concept assessments that changed
                List<Concept> conceptAssessmentChanges = null;
                
                List<EndTriggerInformation> taskShouldEndInfos = null;
                if(interestedConcepts != null){
                    for(Concept concept : interestedConcepts){
                        
                        //only provided the message to active concepts
                        if(concept.isActive()){
                            currentConceptWithSimMsg = concept;
                            
                            ConceptAssessment cAssessment = (ConceptAssessment)concept.handleTrainingAppGameState(message);
                            
                            //handle any resulting assessment from the simulation message
                            if(cAssessment != null){
                                
                                //reset survey assessment in order to use concept assessment(s)
                                latestSurveyAssessment = AssessmentLevelEnum.UNKNOWN;
                                
                                EndTriggerInformation taskShouldEndInfo = shouldEnd(concept, false);
                                if(taskShouldEndInfo != null){
                                    // capture all trigger events for processing after this for loop
                                    // to allow all concepts to receive the game state message for assessment
                                    
                                    if(taskShouldEndInfos == null){
                                        taskShouldEndInfos = new ArrayList<>();
                                    }
                                    taskShouldEndInfos.add(taskShouldEndInfo); 
                                }
                                
                                // capture all concept assessment changes for scenario class notification after this for loop
                                // finishes to allow all concepts to receive the game state message for assessment
                                if(conceptAssessmentChanges == null){
                                    conceptAssessmentChanges = new ArrayList<>();
                                }
                                conceptAssessmentChanges.add(concept);
                            }
                        }
                    }//end for
                }//end if
                
                //reset
                currentConceptWithSimMsg = null;
                
                if(needCompletionCheck && !finished){
                    //a concept has finished while handling the simulation message and it didn't activate any end triggers
                    
                    //reset
                    needCompletionCheck = false;
                    
                    //check if all concepts have finished
                    if(isCompleted()){
                        finished = true;
                    }
                }
                
                //DEBUG
                //System.out.println("concept check time = "+ (new Date().getTime() - start.getTime()));
                
                //check if the task should be deactivated based on one of it's end triggers  
                //NOTE: this happens after the concepts get the simulation message for when a concept needs a message
                //      that could also end the task
                if(!finished){
                    
                    //check if the incoming game state message does satisfies this tasks' end triggers
                    EndTriggerInformation taskShouldEndInfo = shouldEnd(message);
                    if(taskShouldEndInfo != null){
                        
                        if(taskShouldEndInfos == null){
                            taskShouldEndInfos = new ArrayList<>();
                        }
                        taskShouldEndInfos.add(taskShouldEndInfo); 
                    }
                    
                    if(taskShouldEndInfos != null){
                        //one or more end triggers have evaluated to true
                        
                        for(EndTriggerInformation triggerInfo : taskShouldEndInfos){
                            handleEndTriggerInformation(triggerInfo);    
                        }
                    }
                        
                }else{
                    //all the concepts have finished 
                    unload();
                }

                
                //when there are concept assessments updates, a task assessment update can be returned
                if(conceptAssessmentChanges != null){                    
                    
                    //notify parent scenario node of change in concept assessment 
                    for(Concept changedConcept : conceptAssessmentChanges){
                        taskActionInterface.conceptAssessmentCreated(this, changedConcept);
                    }

                	if(logger.isDebugEnabled()){
                    	logger.debug("Calculating and updating Task assessment for task named "+getName()+" because a game state message caused one or more concepts to updated it's assessment");
                    }
                	
                    calculateTaskAssessment(AssessmentUpdateEventType.CONCEPT_SYNC_UPDATED);
                    return currentTaskAssessment;
                }
                
            }//end if
            
        }//end sync
        
        return null;
    }
    
    /**
     * This method should be called to manually start the task without any other
     * trigger having been fired.
     */
    public void handleManualStart() {
        activate();
    }

    /**
     * Handle a start trigger that just evaluated to true.
     * This could mean delaying the activation of this task and/or displaying a message to the learner (e.g. task instructions)
     *
     * @param startTrigger the start trigger that is being activated because its rule evaluated to true.  Can contain
     * a message to show to the learner.  Can't be null.
     * @return true iff the start trigger is immediately activating this task, false if there is an authored delay.
     */
    private boolean handleStartTriggerInformation(final AbstractTrigger startTrigger){
        
        boolean activatedHere = false;
        if(startTrigger.getTriggerDelay() > 0){  
            //schedule the activation for sometime in the future
            
            if(logger.isInfoEnabled()){
                logger.info("The task will be starting in "+startTrigger.getTriggerDelay()+" seconds because the start trigger: " + startTrigger + " was fired.\n" + this);
            }
            delayedTaskStart(startTrigger);
            
        }else{  
            if(logger.isInfoEnabled()){
                logger.info("The task will be starting because the start trigger: " + startTrigger + " was fired.\n"+ this);
            }
            activatedHere = true;
            
            if(!startTrigger.getDomainActions().getStrategy().getStrategyActivities().isEmpty()){
                //there is a message to display to the user about the triggered event
                taskActionInterface.handleDomainActionWithLearner(startTrigger.getDomainActions());     
            }
        }
        
        return activatedHere;
    }
    
    /**
     * This method should be called to manually end the task without any other
     * trigger having been fired.
     * 
     * @param reason - short explanation that can be used for display purposes. Can't be null or empty.
     */
    public void handleManualEnd(String reason) {
        handleEndTriggerInformation(new EndTriggerInformation(new ManualTrigger(reason)));
    }

    /**
     * Handle presenting a message to the user about the trigger being fired.
     * For example if the learner's actor dies in the training application it is useful to display 
     * a message long enough for the learner to read it before ending the scenario.
     * 
     * @param endTaskTriggerInfo contains trigger information that is causing the end of this task.  In addition
     * any message to present to the learner.  Can't be null.
     */
    private void handleEndTriggerInformation(final EndTriggerInformation endTaskTriggerInfo){ 
        
        if(!isActive()){
            return;
        }
        
        if(!endTaskTriggerInfo.getDomainActions().getStrategy().getStrategyActivities().isEmpty()){
            //there is a message to display to the user about the triggered event
                
            taskActionInterface.handleDomainActionWithLearner(endTaskTriggerInfo.getDomainActions());    
        }
            
        if(endTaskTriggerInfo.getDelay() == null || endTaskTriggerInfo.getDelay() <= 0){
            //finish right away                
            
            //ignore future simulation messages                                
            finished = true;
            
            if(endTaskTriggerInfo.getTrigger().isScenarioEndingTrigger()){
                taskActionInterface.fatalError("A scenario level end trigger was activated.", 
                        "The task named "+getName()+" fired an end trigger that has been set to also end the scenario.");
            }else{
                unload();
            }
        }else{
            //wait some amount of time to finish                                
                
            TimerTask task = new TimerTask() {
                
                @Override
                public void run() {                        
                    
                    //ignore future simulation messages                                
                    finished = true;
                    
                    if(endTaskTriggerInfo.getTrigger().isScenarioEndingTrigger()){
                        taskActionInterface.fatalError("A scenario level end trigger was activated.", 
                                "The task named "+getName()+" fired an end trigger that has been set to also end the scenario (after a "+endTaskTriggerInfo.getDelay()+" sec delay).");
                    }else{
                        unload();
                    }                    
                }
            };
            
            if(delayedTaskEndTimer == null){
                delayedTaskEndTimer = new Timer(getName() + " delayed Task end timer");
            }
            
            delayedTaskEndTimer.schedule(task, (long) (endTaskTriggerInfo.getDelay() * 1000));

        }

    }
    
    /***************************  Start/End Checks *****************************/
    
    /**
     * Return whether or not this task should start based on its start trigger(s)
     * 
     * @param message a message to provide to this task's start triggers 
     * @return whether the caller to this method should activate this task
     */
    private boolean shouldStart(Message message){        
               
        if (startTriggers.isEmpty()) {
            
            if(logger.isInfoEnabled()){
                logger.info("Task + " + this + " has no start triggers");
            }
            
            return true;
        } else if(!isActive()){
            for(AbstractTrigger trigger : startTriggers) {                
                
                try{
                    if (trigger.shouldActivate(message)) {
                        
                        return handleStartTriggerInformation(trigger);
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task start trigger of "+trigger+" when receiving the message\n"+message+"\nfor this task of "+this+".", e);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Return whether or not this task should start based on its start trigger(s)
     * 
     * @param concept a concept to provide to this task's start triggers
     * @return whether the caller to this method should activate this task
     */
    private boolean shouldStart(Concept concept){
        
        if(!isActive()){
            for (AbstractTrigger trigger : startTriggers) {
                
                try{
                    if (trigger.shouldActivate(concept)) {
                        
                        return handleStartTriggerInformation(trigger);
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task start trigger of "+trigger+" when receiving an update from the concept\n"+concept+"\nfor this task of "+this+".", e);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Return whether or not this task should start based on its start trigger(s), specifically
     * those triggers looking at when a strategy is applied.
     * @param strategyName unique name of a strategy
     * @return whether the caller to this method should activate this task
     */
    private boolean shouldStart(String strategyName){
        
        if(!isActive()){
            for (AbstractTrigger trigger : startTriggers) {
                
                try{
                    if (trigger.shouldActivate(strategyName)) {
                        
                        return handleStartTriggerInformation(trigger);
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task start trigger of "+trigger+" when receiving a notification that a strategy was applied - '"+strategyName+"' for the task of "+this+".", e);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Return whether or not this task should start based on its start trigger(s)
     * 
     * @param task a task to provide to this task's start triggers
     * @return whether the caller to this method should activate this task
     */
    private boolean shouldStart(Task task){
    	
        if(!isActive()){
        	for (AbstractTrigger trigger : startTriggers) {
                
                try{
                    if (trigger.shouldActivate(task, this)) {
                        return handleStartTriggerInformation(trigger);
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task start trigger of "+trigger+" when receiving an update from the task\n"+task+"\nfor this task of "+this+".", e);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check whether or not this task should end based on its stop trigger(s) using the message parameter provided.
     * 
     * @param message a message to provide to this task's end triggers
     * @return information about the trigger that is ending this task.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(Message message){
        
        if(isActive()){
            for(AbstractTrigger trigger : endTriggers){
                
                try{
                    if(trigger.shouldActivate(message)){
                        
                        if(logger.isInfoEnabled()){
                            logger.info("The task: "+this+" is finished because the end trigger: "+trigger+" was activated");
                        }
                        
                        EndTriggerInformation info = new EndTriggerInformation(trigger);                        
                        return info;
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task end trigger of "+trigger+".", e);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check whether or not this task should end based on its stop trigger(s) using the concept provided.
     * 
     * @param concept a concept to provide to this task's end triggers
     * @param isCurrentConcept whether or not the concept being checked is the current child concept with the
     * current training app state message (see handleTrainingAppGameState(Message) method)
     * @return information about the trigger that is ending this task.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(Concept concept, boolean isCurrentConcept){
        
        if(isActive()){
            for(AbstractTrigger trigger : endTriggers){
                
                try{
                    if(trigger.shouldActivate(concept)){
                        
                        if(logger.isInfoEnabled()){
                            logger.info("The task: "+this+" is finished because the end trigger: "+trigger+" was activated");
                        }
                        
                        EndTriggerInformation info = new EndTriggerInformation(trigger);
                        return info;
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task end trigger of "+trigger+".", e);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check whether or not this task should end based on the fact that the strategy with the given name was applied.
     * 
     * @param strategyName the unique name of a strategy that was applied.
     * @return information about the trigger that is ending this task.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(String strategyName){
        
        if(isActive()){
            for(AbstractTrigger trigger : endTriggers){
                
                try{
                    if(trigger.shouldActivate(strategyName)){
                        
                        if(logger.isInfoEnabled()){
                            logger.info("The task: "+this+" is finished because the end trigger: "+trigger+" was activated");
                        }
                        
                        EndTriggerInformation info = new EndTriggerInformation(trigger);
                        return info;
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task end trigger of "+trigger+".", e);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check whether or not this task should end based on its stop trigger(s) using another task provided.
     * 
     * @param task the task that is being checked
     * @return EndTriggerInformation information about the trigger that is ending this task.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(Task task){
    	
        if(isActive()){
        	for(AbstractTrigger trigger : endTriggers){
        		
        	    try{
            		if(trigger.shouldActivate(task, this)){
            		    
            		    if(logger.isInfoEnabled()){
            		        logger.info("The task: "+this+" is finished because the end trigger: "+trigger+" was activated");
            		    }
            			
            		    EndTriggerInformation info = new EndTriggerInformation(trigger);                        
                        return info;
            		}
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving task end trigger of "+trigger+".", e);
                }
        	}
        }
    	
    	return null;
    }
    
    /**
     * Schedule a delay to starting this task based on the trigger's delay value.  Then
     * call the logic normally executed when a start task trigger fires.
     * 
     * @param startTrigger the trigger being fired based on logic checked else-where
     */
    private void delayedTaskStart(final AbstractTrigger startTrigger){
        
        if(!isActive()){
            TimerTask task = new TimerTask() {
                
                @Override
                public void run() {
                    
                    if(!startTrigger.getDomainActions().getStrategy().getStrategyActivities().isEmpty()){
                        //there is a message to display to the user about the triggered event
                        
                        taskActionInterface.handleDomainActionWithLearner(startTrigger.getDomainActions());     
                    }
                    
                    activate();                     
                }
            };
            
            if(delayedTaskStartTimer == null){
                delayedTaskStartTimer = new Timer(getName() + " delayed Task start timer");
            }
            
            delayedTaskStartTimer.schedule(task, (long) (startTrigger.getTriggerDelay() * 1000));
        }
    }
    
    
    /*************************** (end) Start/End Checks *****************************/
    
    /**
     * The task has been started, activate all concepts
     */
    private void activate(){ 
        
        if(!active){
            
            active = true;
			finished = false;
			currentTaskAssessment.setNodeStateEnum(PerformanceNodeStateEnum.ACTIVE);
			
            if(delayedTaskStartTimer != null){
                delayedTaskStartTimer.cancel(); // the task is now active, cancel any scheduled start task triggers that 
                                                // evaluated to true in the past but have yet to fire due to an authored delay time
            }
        
            if(logger.isInfoEnabled()){
                logger.info("Activating Task named "+getName());
            }
            
            //notify scenario that this task has started
            taskActionInterface.taskStarted(this);
            
            for(Concept concept : concepts){
                concept.start();
            }
            
            //send out initial performance state
            updateAssessment(AssessmentUpdateEventType.TASK_ACTIVATED);

        }

    }
    
    /**
     * Force the task to activate it's concepts before receiving a single simulation message.
     * This is useful for when you need tasks/concepts to start logic before a training application game state
     * message arrives but most likely after the lesson has began.
     */
    public void start(){
        
        if (startTriggers.isEmpty()) {
            //start this task if it has no start triggers, meaning the task can only be activated immediately upon the start of the lesson
            //and not by a training application game state message or other type of start trigger logic
            
            if(logger.isInfoEnabled()){
                logger.info("Starting " + this + " because it has no start triggers");
            }
            activate();
            
        }else if(shouldStart(this)){
            // this is useful for checking ScenarioStartTriggers which need to know when
            // the scenario has started.
            
            if(logger.isInfoEnabled()){
                logger.info("Starting " + this + " because of the current state of this task.");
            }
            activate();
        }

    }
    
    /**
     * Update the task assessment by analyzing it's concept assessments
     * @param eventType the enumerated type of event that is causing this task assessment to be calculated,
     * can't be null.
     */
    private void calculateTaskAssessment(AssessmentUpdateEventType eventType){
        
        //Note: don't over-ride a survey assessment if it has been calculated until a concept changes it's assessment
        if(currentTaskAssessment != null && latestSurveyAssessment == AssessmentLevelEnum.UNKNOWN){

            // synchronize and then check for null to handle a race condition where cleanup method
            // could be called on another thread which would set the assessment value to null
            synchronized(currentTaskAssessment){
                if(currentTaskAssessment == null){
                    return;
                }
                
                if(logger.isDebugEnabled()){
                    logger.debug("Calculating metrics for '"+getName()+"'.");
                }

                /* If the metrics are being updated by the system, then the
                 * evaluator is null */
                currentTaskAssessment.setEvaluator(null);

                calculatePerformanceMetric();
                calculateConfidenceMetric();
                calculateCompetenceMetric();
                calculateTrendMetric();
                calculatePriorityMetric();
                
                fireAssessmentUpdate(false, eventType);
            }
        }
    }
    
    /**
     * Calculate and possibly update the difficulty metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @param strategyName name of the strategy being applied that is causing this task's difficulty value to be recalculated.
     * Can't be null or empty.
     * @param strategyDifficulty that strategy's difficulty value to apply to this task.  If null, nothing happens.
     * @return whether the priority value was changed for this node
     */
    private boolean calculateDifficultyMetric(String strategyName, Double strategyDifficulty){
        
        if(difficultyMetric == null){
            difficultyMetric = new DefaultDifficultyMetric();
        }
        
        try{
            return difficultyMetric.setDifficulty(this, assessmentProxy, strategyName, strategyDifficulty);
        }catch(Throwable t){
            logger.error("The difficulty metric implementation class of "+difficultyMetric+" caused an error when calculating the difficulty for the task"
                    +getName()+"'.  The strategy being applied is '"+strategyName+" with difficulty value of "+strategyDifficulty, t);
            return false;
        }
    }
    
    /**
     * Calculate and possibly update the stress metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @param strategyName name of the strategy being applied that is causing this task's stress value to be recalculated.
     * Can't be null or empty.
     * @param strategyStress that strategy's stress value to apply to this task.  If null, nothing happens.
     * @return whether the priority value was changed for this node
     */
    private boolean calculateStressMetric(String strategyName, Double strategyStress){
        
        if(stressMetric == null){
            stressMetric = new DefaultStressMetric();
        }
        
        try{
            return stressMetric.setStress(this, assessmentProxy, strategyName, strategyStress);
        }catch(Throwable t){
            logger.error("The stress metric implementation class of "+stressMetric+" caused an error when calculating the stress for the task '"
                    +getName()+"'.  The strategy being applied is '"+strategyName+" with stress value of "+strategyStress, t);
            return false;
        }
    }
    
    @Override
    protected Map<UUID, PerformanceMetricArguments> getChildConceptOrConditionPerformanceMetricArgs() {
        
        if(childAssessmentWeights == null){
            // gather child concepts
            childAssessmentWeights = new HashMap<>();
            for(Concept concept : concepts){
                childAssessmentWeights.put(concept.getAssessment().getCourseNodeId(), concept.getPerformanceArguments());
            }
        }
        return childAssessmentWeights;
    }
    
    @Override
    public boolean hasScoringRules(){        

        boolean hasScorers = false;
        
        //check this task's concepts
        for(Concept concept : concepts){
            
            hasScorers |= concept.hasScoringRules();
            
            if(hasScorers){
                break;
            }
        }
        
        return hasScorers;
    }
    
    @Override
    protected boolean updateAssessment(AssessmentUpdateEventType eventType){
        
        calculateTaskAssessment(eventType);
        taskActionInterface.taskAssessmentCreated(new TaskAssessmentEvent(this, currentTaskAssessment, eventType));
        return true;
    }
    
    /**
     * Return whether the task's concepts have completed or not.
     * The concepts can only be completed if the task is running or has
     * run as some point (i.e. this task is finished)
     * 
     * @return boolean
     */
    public boolean isCompleted(){
        
        boolean done = true;
        for(Concept aConcept : concepts){
            
            if(!aConcept.isFinished()){
                done = false;
                break;
            }
        }
        
        return done;
    }
    
    /**
     * Handle the notification that a concept has been updated.  This could be that a concept
     * has ended or its assessment has been changed.  This method can:<br/>
     * 1. check if one of this task's end triggers has activated because of this concept<br/>
     * 2. deactivate this task if appropriate<br/>
     * 3. check if one of this task's start triggers has activated because of this concept
     * 
     * @param concept the concept that has changed in some way
     */
    public void conceptUpdatedNotification(Concept concept){
        
        if(concept == currentConceptWithSimMsg){
            //this is the current concept being given a simulation message and that concept
            //has completed.  Delay the task isCompleted check until all concepts have had a chance
            //to receive the simulation message
            
            needCompletionCheck = true;
            
            //
            // check end triggers for task completion
            //
            
            //IMPORTANT: if this logic changes make sure to update the "delayedTaskEndOnConcept(final AbstractTrigger trigger, final boolean isCurrentConcept)"
            //logic to match!
            EndTriggerInformation info = shouldEnd(concept, true);
            if(info != null){
                handleEndTriggerInformation(info);
            }
            
            
        }else if(isActive() && isCompleted()){
            //the concept is completed on a different thread than the handle simulation message method calls,
            //no need to wait, notify the parent scenario that this task has finished because all it's concepts are finished
        
            if(logger.isDebugEnabled()){
                logger.debug("All concepts have finished for task "+getName()+", therefore the task is finished");
            }
            
            deactivate();
            
        } else if (shouldStart(concept)) {
            
            // if the update to this concept was a start trigger for this task, activate this task
            activate();
            
        }else if(isActive()){
            
            EndTriggerInformation info = shouldEnd(concept, false);
            if(info != null){
                //the concept is completed on a different thread than the handle simulation message method calls,
                //no need to wait, notify the parent scenario that this task has finished because of an end trigger
                
                //IMPORTANT: if this logic changes make sure to update the "delayedTaskEndOnConcept(final AbstractTrigger trigger, final boolean isCurrentConcept)"
                //logic to match!
                handleEndTriggerInformation(info);
            }
            

        }
    }

    /**
     * A concept has finished.  Determine if the task is finished by checking it's concepts
     * 
     * @param concept the concept that has ended
     */
    public void conceptEndedNotification(Concept concept){        
        conceptUpdatedNotification(concept);
    }
    
    /**
     * Notification that the strategy with the name was just applied.  Determine if this 
     * task should start or stop based on the defined triggers.
     * 
     * @param event contains information about the strategy that was applied.  Can't be null.
     */
    public void appliedStrategyNotification(StrategyAppliedEvent event){
        
        // determine if the strategy being applied effects this task.
        // Currently it applies to the task if the strategy is applied while the task is active.
        boolean strategyApplies = false;
        if(this.getAssessment() != null && event.getTasksAppliedToo() != null) {
            strategyApplies = CollectionUtils.containsIgnoreCase(event.getTasksAppliedToo(), this.getAssessment().getCourseNodeId().toString());
        }
        
        if (shouldStart(event.getStrategyName())) {
            // Determine if the task should start based on its start triggers
            activate();

        } else if (isActive()) {  
            // Determine if the task should end based on its end triggers
            
            // when not task(s) are specified, apply to all active tasks
            strategyApplies |= CollectionUtils.isEmpty(event.getTasksAppliedToo());

            EndTriggerInformation info = shouldEnd(event.getStrategyName());
            if(info != null){
                
                //IMPORTANT: changes made in this if condition should be copied in 
                //"delayedTaskEndOnTask(final AbstractTrigger trigger)" method                
                handleEndTriggerInformation(info);
            }       
        }
        
        if(strategyApplies) {
            // the strategy applies to this task, update stress then difficulty information for this task
            
            boolean changed = false; 
            
            // update task stress value based on incoming strategy stress applied
            changed |= calculateStressMetric(event.getStrategyName(), event.getStrategyAppliedStress());
            
            // update task difficulty value based on incoming strategy difficulty applied 
            changed |= calculateDifficultyMetric(event.getStrategyName(), event.getStrategyAppliedDifficulty());
            
            if(changed) {
                // notify the Scenario object parent to this task so that a performance assessment
                // message will be sent with the new values
                fireAssessmentUpdate(true, AssessmentUpdateEventType.STRATEGY_APPLIES_TO_TASK); 
            }
        }
    }

    /**
     * Determines whether or not a task should be deactivated.
     * 
     * @param task the task that has ended
     */
    public void taskEndedNotification(Task task){
    	
        if (shouldStart(task)) {
            // Determine if the task should start based on its start triggers
            activate();

        } else if (isActive()) {  
            // Determine if the task should end based on its end triggers

            EndTriggerInformation info = shouldEnd(task);
            if(info != null){
                
                //IMPORTANT: changes made in this if condition should be copied in 
                //"delayedTaskEndOnTask(final AbstractTrigger trigger)" method                
                handleEndTriggerInformation(info);
            }   	
    	}
    }
    
    /**
     * Notify the scenario that a concept of this task has ended.
     * 
     * @param concept a descendant concept of this task that has ended its assessment logic
     * because it's child conditions have completed/ended.
     */
    private void notifyScenarioConceptEnded(Concept concept){
        taskActionInterface.conceptEnded(this, concept);
    }
    
    /**
     * Notify @link {@link Scenario} that a descendant concept to this task has updated its assessment.
     * 
     * @param concept a descendant concept of this task, doesn't have to be a direct child.
     */
    private void notifyScenarioConceptAssessment(Concept concept){
        taskActionInterface.conceptAssessmentCreated(this, concept);
    }
    
    /**
     * Method used by external classes to deactivate the task.
     */
    public synchronized void deactivate(){
        
        if(isActive()){
            
            //synchronized because the task can be receiving simulation messages on another thread
            synchronized(active){
                unload();
            }
        }
    }
    
    /**
     * Set the variable handler used to handle retrieval of assessment variables that have 
     * been provided during course execution.
     * 
     * @param varsHandler can't be null.
     */
    public void setVarsHandler(VariablesHandler varsHandler){
        
        for(Concept concept : concepts){
            concept.setVarsHandler(varsHandler);
        }
    }
    
    /**
     * Initialize this task.
     */
    public void initialize(){
        
        if(logger.isInfoEnabled()){
            logger.info("initializing task: "+this);
        }
        
        //
        // Initialize task's concepts
        //
        for(Concept concept : concepts){
            
            Set<MessageTypeEnum> simInterests = new HashSet<MessageTypeEnum>();
            concept.initialize(simInterests, conceptActionHandler);
            
            //update map with concept's simulation message interests
            for(MessageTypeEnum type : simInterests){
                
                List<Concept> concepts = simulationMessageToListeners.get(type);
                if(concepts == null){
                    concepts = new ArrayList<Concept>();
                    simulationMessageToListeners.put(type, concepts);
                }
                
                concepts.add(concept);
            }
        }
        
        //
        // Initialize task's start triggers
        //
        for(AbstractTrigger trigger : startTriggers){
            trigger.initialize();
        }
        
        //
        // Initialize task's end triggers
        //
        for(AbstractTrigger trigger : endTriggers){
            trigger.initialize();
        }
        
        assessmentProxy = AssessmentProxyManager.getInstance().getAssessmentProxy(this);
        if(assessmentProxy == null){
            throw new RuntimeException("The assessment proxy was not found for the task\n"+this);
        }
        if(logger.isInfoEnabled()){
            logger.info("Set assessment proxy for task '"+getName()+"'.");
        }
        fireAssessmentUpdate(false, AssessmentUpdateEventType.TASK_INITIALIZED);
    }
    
    @Override
    protected void fireAssessmentUpdate(boolean notifyParentNode, AssessmentUpdateEventType eventType){ 
        
        if(assessmentProxy != null){
            assessmentProxy.fireAssessmentUpdate(currentTaskAssessment);
        }
        
        if(notifyParentNode){
            taskActionInterface.taskAssessmentCreated(new TaskAssessmentEvent(this, currentTaskAssessment, eventType));
        }
    
    }
    
    /**
     * Unload this task by deactivating it and removing interest in simulation messages.
     */
    private void unload(){
        
        if(delayedTaskEndTimer != null){
            delayedTaskEndTimer.cancel();  // the task is now inactive, cancel any scheduled end task triggers that 
                                           // evaluated to true in the past but have yet to fire due to an authored delay time
        }
        
        if(delayedTaskStartTimer != null){
            delayedTaskStartTimer.cancel(); // the task is now inactive, cancel any scheduled start task triggers that 
                                            // evaluated to true in the past but have yet to fire due to an authored delay time
        }
        
        if(isActive()){
            
            active = false;
            finished = true;
            currentTaskAssessment.setNodeStateEnum(PerformanceNodeStateEnum.FINISHED);
            
            for(Concept concept : concepts){
                
                if(concept.isActive()){
                    concept.stop();
                }
            }
            
            if(logger.isInfoEnabled()){
                logger.info("De-activating task named "+getName()+".");
            }
            
            taskActionInterface.taskEnded(this);
        }

    }
    
    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes. 
     */
    public void cleanup(){
        
        for(Concept concept : concepts){
            concept.cleanup();
        }
        
        AssessmentProxyManager.getInstance().unregisterNode(this);
        
        // synchronize to make sure the task metrics aren't being updated right now
        // since that logic sets the calculated values in this object
        synchronized (currentTaskAssessment) {
            currentTaskAssessment = null;
        }
        
        concepts.clear();
        conceptActionHandler = null;
        taskActionInterface = null;
    }
    
    /**
     * Return the list of triggers that can start this task
     * 
     * @return List<AbstractTrigger>
     */
    public List<AbstractTrigger> getStartTriggers() {
        return startTriggers;
    }

    /**
     * Return the list of triggers that can end this task
     * 
     * @return List<AbstractTrigger>
     */
    public List<AbstractTrigger> getEndTriggers() {
        return endTriggers;
    }

    /**
     * Return the concepts associated with this domain scenario task.
     * 
     * @return collection of concepts directly under this task.
     */
    public List<Concept> getConcepts() {
        return concepts;
    }

    /**
     * Get the overall assessment score for this Task.  This should be called
     * at the end of the real time assessment.
     *
     * @return new score node with a score calculated from the child overall assessment scores.  Can be
     * null if the descendants have no overall assessment scores.
     */
    public GradedScoreNode getScore(){
        
        TaskScoreNode node = new TaskScoreNode(getName());
        node.setPerformanceNodeId(getNodeId());
        node.setDifficulty(currentTaskAssessment.getDifficulty());
        node.setDifficultyReason(currentTaskAssessment.getDifficultyReason());
        node.setStress(currentTaskAssessment.getStress());
        node.setStressReason(currentTaskAssessment.getStressReason());
        calculateGradeMetric(node);
   
        //if the task's concepts have no scorer nodes below it, than this task node doesn't need to be added 
        if(node.isLeaf()){
            return null;
        }else{
            return node;
        }
    }
        
    @Override
    public void assessConcepts() {
        
        if(logger.isInfoEnabled()){
            logger.info("Assessing concepts for "+this);
        }

        for(Concept concept : concepts){
            concept.assessConditions();
        }
    }
    
    
    /**
     * Return the current task assessment for this task
     * 
     * @return the current task assessment.  Can be null if {@link #cleanup()} was already called.
     */
    @Override
    public ProxyTaskAssessment getAssessment(){
        return currentTaskAssessment;
    }
    
    @Override
    protected void addAssessmentExplanation(String assessmentExplanation){
        currentTaskAssessment.addAssessmentExplanation(assessmentExplanation);
    }
    
    @Override
    protected void assessmentEnded() {
        
        for(Concept concept : concepts){
            concept.assessmentEnded();
        }
    }
        
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Task: ");
        sb.append(super.toString());
        
        sb.append(", Start triggers = {");        
        for(AbstractTrigger trigger : getStartTriggers()){
            sb.append(trigger).append(", ");
        }
        sb.append("}");
        
        sb.append(", End Triggers = {");
        for(AbstractTrigger trigger : getEndTriggers()){
            sb.append(trigger).append(", ");
        }
        sb.append("}");
        
        sb.append(", Concepts = ");
        for(Concept concept : concepts){
            sb.append(concept).append(", ");
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    

    
    /**
     * This is the implementation for the concept action interface which handles the
     * concepts' actions.
     * 
     * @author mhoffman
     *
     */
    private class ConceptActionHandler implements ConceptActionInterface{
        
        @Override
        public void conceptStarted(Concept concept){
            //nothing to do...
        }

        @Override
        public void conceptEnded(Concept concept){
            conceptEndedNotification(concept);            

            notifyScenarioConceptEnded(concept);
        }
        
        @Override
        public void conceptAssessmentCreated(Concept concept, ConceptAssessment conceptAssessment){
            
            if(conceptAssessment != null){  
                
                //reset survey assessment in order to use concept assessment(s)
                latestSurveyAssessment = AssessmentLevelEnum.UNKNOWN;
                
                if(logger.isDebugEnabled()){
                	logger.debug("Updating Task assessment for task named "+getName()+" because the Concept "+concept.getName()+" updated it's assessment to "+conceptAssessment);
                }
                
                if(getConcepts().contains(concept)){
                    // only update this task assessment if a child (not lower descendant) concept changed assessment
                    updateAssessment(AssessmentUpdateEventType.CONCEPT_ASYNC_UPDATED);
                }
                
                //needed for the concept assessment trigger logic
                conceptUpdatedNotification(concept);
                
                notifyScenarioConceptAssessment(concept);
            }
        }

        @Override
        public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
            taskActionInterface.trainingApplicationRequest(infoRequest);
        }

        @Override
        public void handleDomainActionWithLearner(DomainAssessmentContent action) {
            taskActionInterface.handleDomainActionWithLearner(action);            
        }

        @Override
        public void fatalError(String reason, String details) {
            taskActionInterface.fatalError(reason, details);            
        }

        @Override
        public void displayDuringLessonSurvey(
                AbstractSurveyLessonAssessment surveyAssessment,
                SurveyResultListener surveyResultListener) {
            taskActionInterface.displayDuringLessonSurvey(surveyAssessment, surveyResultListener);            
        }

        @Override
        public SessionMembers getSessionMembers() {
            return taskActionInterface.getSessionMembers();
        }

        @Override
        public List<MessageManager> getPlaybackMessages() {
            return taskActionInterface.getPlaybackMessages();
        }
		}

}
