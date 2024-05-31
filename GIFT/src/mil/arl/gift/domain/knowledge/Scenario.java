/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.dkf.team.TeamOrganization;
import mil.arl.gift.common.course.dkf.team.TeamUtil;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.ScoreUtil;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.common.ta.state.VariablesState;
import mil.arl.gift.common.ta.state.VariablesState.VariableNumberState;
import mil.arl.gift.common.ta.state.VariablesState.VariableState;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.common.ta.state.VariablesStateResult;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractTrigger;
import mil.arl.gift.domain.knowledge.common.EndTriggerInformation;
import mil.arl.gift.domain.knowledge.common.KKILLTrigger;
import mil.arl.gift.domain.knowledge.common.PerformanceNodeStatusTool;
import mil.arl.gift.domain.knowledge.common.ProxyPerformanceAssessment;
import mil.arl.gift.domain.knowledge.common.ProxyTaskAssessment;
import mil.arl.gift.domain.knowledge.common.ScenarioActionInterface;
import mil.arl.gift.domain.knowledge.common.TaskActionInterface;
import mil.arl.gift.domain.knowledge.common.TaskLessonAssessmentHandlerInterface;
import mil.arl.gift.domain.knowledge.common.TrainingAppUnexpectedStoppedTrigger;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.ConceptAssessmentEvent;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.ConceptEndedEvent;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.ConceptStartedEvent;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.TaskAssessmentEvent;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.TaskEndedEvent;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.TaskStartedEvent;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.TriggerEventHandler;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr.ConditionEntityState;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr.ConditionEntityStateNumberVariable;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr.SessionConditionsBlackboard;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationVarsHandler;
import mil.arl.gift.domain.knowledge.strategy.StrategyAppliedEvent;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;

/**
 * This class contains information about a scenario.
 *
 * @author mhoffman
 *
 */
public class Scenario implements TaskLessonAssessmentHandlerInterface{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Scenario.class);

    /** the scenario name */
    private String name;

    /** The scenario description. Supports HTML. */
    private String description;

    /** the container of tasks for this scenario */
    private List<Task> tasks;

    /**
     * collection of scenario ending triggers
     * Can be null or empty.
     */
    private List<AbstractTrigger> endTriggers;

    /** container of the performance assessment nodes for this scenario */
    private Map<Integer, AbstractPerformanceAssessmentNode> dkfPerformanceNodes;

    /** the optional mission data for this scenario */
    private Mission mission;
    
    /** the various resources available for this scenario */
    private Resources resources;

    /** handles this scenario's tasks actions */
    private TaskActionHandler taskActionHandler;

    /** this is the interface which needs to be called to notify the domain knowledge of scenario actions (e.g. a performance assessment has been created) */
    private ScenarioActionInterface scenarioActionInterface;

    /** contains a link (i.e. by unique id) to the various root level task performance assessment values */
    private ProxyPerformanceAssessment currentAssessment;

    /** (optional) timer used to auto-complete this scenario */
    private Timer autoCompleteTimer;

    /** flag used to indicate if the scenario is still active */
    private boolean active;
    
    /** flag used to indicate if the scenario has finished starting */
    private boolean started;
    
    /** flag used to indicate if an assessment was reported yet during starting. 
     *  Want to make sure an assessment goes out even when no tasks start immediately.  
     *  This helps UIs like the game master become populated. */
    private boolean sentStartAssessment = false;

    /** used to show the status of tasks and concepts for this scenario in a dialog */
    private PerformanceNodeStatusTool statusTool;
    private Object statusToolCreationSemaphore = new Object();

    /**
     * Trigger event handler logic used to process task provided events (e.g. concept ended, task ended)
     * in a threaded queue.
     * Note: this is important to prevent synchronization issues and deadlocks once seen
     */
    private TriggerEventManager triggerEventMgr;
    private TriggerEventHandler triggerEventHandler = new TriggerEventScenarioHandler();
    private final Object triggerEventQueueMutex = new Object();

    /**
     * This thread is used to handle delaying the ending of this scenario.  It is mainly driven by authored logic in the
     * domain assessment knowledge.  It could be used for allowing feedback/instruction to be shown with enough time
     * before being removed/replaced.
     *
     * Note: currently we don't support scheduling multiple scenario end logic, only 1 at a time.  This is important to
     * know in cases where a subsequent event to end the scenario could fire before the currently scheduled scenario ending event
     * but will be ignored in this case.  Future logic will handle this with a more sophisticated scheduler.
     */
    private Thread delayedScenarioEndThread = null;

    /**
     * The root team of this sceanrio's team organization hierarchy.
     * Can be null when identifying learners game state traffic among other entities is not needed
     */
    private TeamOrganization teamOrganization;
    
    /** Point that is cached for performance reasons and is used when looking up team locations */
    private Point3d cachedTeamPoint = new Point3d();
    
    /** instance of the blackboard for the domain session this scenario is running in */
    private SessionConditionsBlackboard blackboard = null;

    /** 
     * A handler used to manage assessment variables, which share information about ongoing
     * automated assessments throughout the knowledge session
     */
	private VariablesHandler varsHandler = new VariablesHandler();

    /**
     * Class constructor - set attributes, load tasks.
     * Note: this constructor should be used when the learner will not be playing an actor in a simulation (e.g. VBS).
     *
     * @param name - display name for this scenario
     * @param description - description of this scenario. Supports HTML.
     * @param tasks - the tasks associated with this scenario
     * @param mission  - the mission data used by this scenario (e.g. source, task, weapon status)
     * @param resources - the resources used by this scenario (e.g. survey context, transitions)
     * @param endTriggers - collection of scenario ending triggers.  Can be null or empty.
     * @param teamOrganization - the team organization. Contains information about
     * how to identify the learner's game state (e.g. entity state) messages amongst other entities in the application.
     * Can be null.
     */
    public Scenario(String name, String description, List<Task> tasks, Mission mission, Resources resources, List<AbstractTrigger> endTriggers,
            TeamOrganization teamOrganization){
        this.name = name;
        this.description = description;
        this.mission = mission;
        this.resources = resources;
        this.endTriggers = endTriggers;
        this.teamOrganization = teamOrganization;

        //
        // add default end triggers
        //
        if(this.endTriggers == null){
            this.endTriggers = new ArrayList<>(2);
        }

        // in case the training application closes unexpectedly
        this.endTriggers.add(new TrainingAppUnexpectedStoppedTrigger(name + " ended unexpectedly trigger"));

        triggerEventMgr = new TriggerEventManager(name, triggerEventHandler);

        taskActionHandler = new TaskActionHandler(triggerEventMgr);

        this.tasks = tasks;
        List<UUID> taskNodeCourseIds = new ArrayList<>();
        dkfPerformanceNodes = new HashMap<Integer, AbstractPerformanceAssessmentNode>(tasks.size()*2);
        for(Task task : tasks){

            if(task.load(taskActionHandler)){
                dkfPerformanceNodes.put(task.getNodeId(), task);
                taskNodeCourseIds.add(task.getAssessment().getCourseNodeId());

                for(AbstractPerformanceAssessmentNode concept : task.getConcepts()){
                    gatherPerformanceNodes((Concept) concept, dkfPerformanceNodes);
                }
                
                task.setVarsHandler(varsHandler);
                
            }else{
                throw new DetailedException("Failed to load the task named "+task.getName(), 
                        "There was an error while loading the task for the scenario named "+name+", therefore the assessment can't continue correctly.", null);
            }
        }

        currentAssessment = new ProxyPerformanceAssessment(taskNodeCourseIds);

        // set the team organization in all triggers for their own use
        if(teamOrganization != null){

            if(endTriggers != null){
                for(AbstractTrigger trigger : endTriggers){
                    
                    try{
                        trigger.setTeamOrganization(teamOrganization);
                    }catch(RuntimeException e){
                        throw new DetailedException("Failed to initialize one of the scenario end triggers.", 
                                "One of the scenario end triggers caused an exception that reads "+e.getMessage(), e);
                    }
                }
            }

            for(Task task : tasks){

                for(AbstractTrigger trigger : task.getStartTriggers()){
                    try{
                        trigger.setTeamOrganization(teamOrganization);
                    }catch(RuntimeException e){
                        throw new DetailedException("Failed to initialize one of the start triggers for the task "+task.getName()+".", 
                                "One of the start triggers for the task '"+task.getName()+"' caused an exception that reads "+e.getMessage(), e);
                    }
                }

                for(AbstractTrigger trigger : task.getEndTriggers()){
                    try{
                        trigger.setTeamOrganization(teamOrganization);
                    }catch(RuntimeException e){
                        throw new DetailedException("Failed to initialize one of the end triggers for the task "+task.getName()+".", 
                                "One of the start triggers for the task '"+task.getName()+"' caused an exception that reads "+e.getMessage(), e);
                    }
                }
            }
        }

    }

    /**
     * Recursively gather all of the performance nodes for the provided concept node.
     *
     * @param concept the concept to gather all descendant concept nodes from
     * @param nodes the map to update with the concepts that are found
     */
    private void gatherPerformanceNodes(Concept concept, Map<Integer, AbstractPerformanceAssessmentNode> nodes) {
        
        dkfPerformanceNodes.put(concept.getNodeId(), concept);
        
        if(concept instanceof IntermediateConcept){
            
            for(Concept subConcept : ((IntermediateConcept) concept).getConcepts()){
                gatherPerformanceNodes(subConcept, nodes);
            }
            
        }else{

            for(AbstractCondition condition : concept.getConditions()){
                condition.setTeamOrganization(teamOrganization);
                
                String invalidMsg = condition.hasValidTeamMemberRefs();
                if(invalidMsg != null){
                    // the condition is not properly configured with team org team member references
                    throw new DetailedException("Failed to properly initialize the concept named "+concept.getName()+"'", 
                            "One of the concept's "+condition.getDescription().getDisplayName()+" condition(s) reported an issue of "+invalidMsg+".  Please provide at least one team member so the condition knows which role to assess in the team organization.", null);
                }
            }
        }
    }

    /**
     * Set the conversation variable handler used to handle retrieval of conversation variables that have
     * been provided during course execution.
     *
     * @param conversationVarHandler can't be null.
     */
    public void setConversationVarsHandler(ConversationVarsHandler conversationVarHandler){
    	
    	varsHandler.setConversationVars(conversationVarHandler);
    }

    /**
     * Initialize the scenario.<br/>
     * Should be called before {@link #start()}.<br/>
     * Note: only the host session should call this method.  This way there is only
     * one running assessment for an individual or collective knowledge assessment session.
     *
     * @param scenarioActionInterface used to communicate scenario level events
     * @return boolean whether the scenario was initialized
     */
    public boolean initialize(ScenarioActionInterface scenarioActionInterface){

        if(scenarioActionInterface == null){
            throw new IllegalArgumentException("the scenario action interface is null");
        }

        this.scenarioActionInterface = scenarioActionInterface;

        taskActionHandler.setScenarioActionInterface(scenarioActionInterface);        

        //initialize task(s)
        for(Task task : tasks){

            task.initialize();
        }        

        active = true;

        return true;
    }

    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes.
     */
    public void cleanup(){

        for(Task task : tasks){
            task.cleanup();
        }

        tasks.clear();
        scenarioActionInterface = null;
        triggerEventHandler = null;
        taskActionHandler = null;

        if(statusTool != null){
            statusTool.dispose();
        }
    }

    /**
     * Start the scenario's tasks.
     * This is another way to start the scenario assessment logic before a training application message
     * has been received.
     *
     * @return boolean whether or not the scenario assessment started
     */
    public boolean start(){ 
        
        if(scenarioActionInterface == null){
            throw new RuntimeException("The scenario action interface was not set.  Make sure Scenario class has been initialized before calling start.");
        }

        // Check if this session is single player (must be done at the start of the assessment not before)
        SessionMembers sessionMembers = scenarioActionInterface.getSessionMembers();
        Map<?,?> members = sessionMembers.getSessionMemberDSIdMap();
        
        boolean isSingleSessionMember = members.size() == 1;        
        if(isSingleSessionMember){
            // there is only one connected session for this dkf, the host. No joiners.

            // Auto add scenario end trigger for when the learner character dies
            // NOTE: for now only artifically add for individual assessment, not teams.
            KKILLTrigger killTrigger = new KKILLTrigger("Learner's character has died.");
            killTrigger.setTeamOrganization(teamOrganization);
            this.endTriggers.add(killTrigger);

            if(teamOrganization != null && teamOrganization.getLearnerTeamMember() == null &&
                    teamOrganization.getRootTeam().getFirstPlayableTeamMember() != null){
                // this happens when the team org only has 1 playable team member position which means there was not
                // any knowledge session update requests for players joining which would have caused the learner team member to be set.
                teamOrganization.setLearnerTeamMember(teamOrganization.getRootTeam().getFirstPlayableTeamMember());
            }
        }

        triggerEventMgr.start();

        //start task(s)
        for(Task task : tasks){
            task.start();
        }

        //REMOVE!!! the domain knowledge shouldn't start the scenario anymore, the TA is responsible for saying it has
        //successfully started
        //scenarioActionInterface.scenarioStarted();

        if(DomainModuleProperties.getInstance().getAutoCompleteScenario() > 0){
            setupAutoCompleteTimer(DomainModuleProperties.getInstance().getAutoCompleteScenario());
        }
        
        // if none of the tasks started and sent an assessment update, send one now.  
        // This helps UIs like the game master become populated.
        if(!sentStartAssessment){
            scenarioActionInterface.performanceAssessmentUpdated(currentAssessment);
            sentStartAssessment = true;
        }

        updateStatusTool();
        
        started = true;

        return true;
    }

    /**
     * Terminate the scenario.  Deactivates all of the scenario's tasks and then notifies the domain assessment knowledge
     * that the scenario has ended.
     * @param status information about why the scenario is ending.
     */
    public void terminate(LessonCompletedStatusType status){

        if(logger.isInfoEnabled()){
            logger.info("The scenario has been ordered to terminate");
        }

        for(Task task : tasks){
            task.deactivate();
        }

        notifyScenarioEnded(status);

        if(statusTool != null){
            statusTool.dispose();
        }
    }

//    /**
//     * Force the tasks to start before receiving a single simulation message.
//     * This is useful for when you need tasks/concepts to start logic before a simulation message arrives but most likely
//     * after the lesson has began.
//     * Note: the learner entity id will be set to null.
//     */
//    public void forceStart(){
//
//        logger.info("Forcing start of "+this);
//
//        populateEntityId(null);
//
//        for(Task task : tasks){
//            task.forceStart();
//        }
//    }

    /**
     * Notification that the event queue is empty which will allow the continued handling of the
     * incoming training application messages logic.
     */
    private void handleDomainKnowledgeEventQueueEmpty(){

        //wake up any awaiting training app message handling that was waiting for the
        //event queue to be empty
        synchronized(triggerEventQueueMutex){
            triggerEventQueueMutex.notifyAll();
        }
    }

    /**
     * Handle the assessments of a conversation between the learner and GIFT.
     * This will update performance node's assessment value if the concept being assessed
     * matches the node's concept and the assessment confidence is high enough to warrant an update.
     *
     * @param assessments contains concept assessment information for choices the learner has made in the conversation.
     */
    public void handleConversationAssessment(List<ConversationAssessment> assessments){

        boolean changed = false;
        for(Task task : tasks){
            changed |= task.handleConversationAssessment(assessments);
        }

        if(changed){
            //create a new performance assessment in order to allow other threads
            //to change the current assessment and not effect the values.
            scenarioActionInterface.performanceAssessmentUpdated(currentAssessment);

            updateStatusTool();
        }
    }

    /**
     * Start a timer which will eventually cause the scenario to finish automatically.
     */
    private void setupAutoCompleteTimer(long time){

        autoCompleteTimer = new Timer("AutoCompleteTime:Scenario-"+name);

        autoCompleteTimer.schedule(new TimerTask(){

            @Override
            public void run() {
                if(logger.isInfoEnabled()){
                    logger.info("The scenario auto-complete timer has fired, terminating scenario");
                }
                terminate(LessonCompletedStatusType.CONTROLLER_ENDED_LESSON);
            }


        }, time * 1000);

        if(logger.isDebugEnabled()){
            logger.debug("Scenario auto-complete timer set to expire in "+time+" seconds");
        }
    }

    /**
     * Process the training application game state message received.  The message usually comes
     * from the Tutor or the Gateway modules.
     *
     * @param message - the training application game state message to handle
     * @return PerformanceAssessment - an assessment created as a result of the game state message
     * Notes:
     *        i. Return null to indicate that the assessment value hasn't changed for the scenario
     *           from the last reported value.  This is due to the scenario's child performance assessment
     *           node(s) (i.e. Tasks) not reporting any changes in their assessment value(s).
     *       ii. Return Above/At/Below as a result of child performance assessment node(s) (i.e. Tasks)
     *           analyzing the message and updating their assessment value(s).  It is acceptable
     *           to return the same value back to back as an indication that the first value is independent
     *           of the second value.
     *           For example, every time a user presses 'button A' a Below assessment value
     *           is returned for a Condition.  Therefore the first and second time that 'button A' is pressed
     *           are different events but with the same back-to-back reported assessment value.
     */
    public ProxyPerformanceAssessment trainingAppGameStateMessageReceived(Message message){

        //wait to handle the next training app state message until all events are processed
        //first, this is to synchronize all domain knowledge events caused by the last training app
        //state message before the next message is handled.
        if(!triggerEventMgr.isEventQueueEmpty()){

            synchronized(triggerEventQueueMutex){
                try {
                    triggerEventQueueMutex.wait();
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting to be notified that the trigger event queue is empty.", e);
                }
            }
        }

        //Nick 2/6/15 - Prevent remaining logic in this method from executing if the scenario is inactive
        if(!active){
            return null;
        }

        boolean updated = false;
        ProxyPerformanceAssessment resultingAssessment = null;

        // capture/update entity identifiers for team members
        //  Note: this is done for every entity state message because with applications like VBS, the entity id
        //       can change when lifeform's mount and then dismount vehicles.
        populateEntityId(message);
        
        // update the black board if necessary based on the message received
        handleBlackboardUpdates(message);

        //synchronized because a task assessment update can affect the current assessment
        synchronized(currentAssessment){

            for(Task task : tasks){

                if(!task.isFinished()){
                    // (currently) a finished task's attributes shouldn't be changed
                    ProxyTaskAssessment tAss = (ProxyTaskAssessment)task.handleTrainingAppGameState(message);

                    if(tAss != null){
                        updated = true;
//                            currentAssessment.updateTaskAssessment(tAss);
                    }

                }
            }

            //when there are task assessments, create a new performance assessment in order to allow other threads
            //to change the current assessment and not effect the values.
            if(updated){
                resultingAssessment = new ProxyPerformanceAssessment(currentAssessment.getTasks());
                updateStatusTool();
            }

        }//end sync

        EndTriggerInformation taskShouldEndInfo = shouldEnd(message);
        if(taskShouldEndInfo != null){
            handleEndTriggerInformation(taskShouldEndInfo);
        }

        return resultingAssessment;
    }

    /**
     * Build an updated performance assessment using the metrics provided within the request.
     *
     * @param request the request containing the updated metrics for a specific task or concept. Request cannot be null.
     */
    public void evaluatorUpdateRequestReceived(EvaluatorUpdateRequest request) {

        synchronized (currentAssessment) {
            final String requestName = request.getNodeName();
            
            //Apply a global bookmark to the assessment, or remove said bookmark if none is in the request
            currentAssessment.setEvaluator(request.getEvaluator());
            currentAssessment.setObserverComment(request.getReason());
            currentAssessment.setObserverMedia(request.getMediaFile());            
            
            if (requestName == null) {
                
                //If there's no request name, then this request is for the whole scenario
                scenarioActionInterface.performanceAssessmentUpdated(currentAssessment);
                
            } else {
                for (Task task : tasks) {
                    if (StringUtils.equalsIgnoreCase(task.getName(), requestName)) {
                        task.evaluatorUpdateRequestReceived(request);
                        break;
                    }
    
                    Concept concept = findConceptByName(requestName, task.getConcepts());
                    if (concept != null) {
                        concept.evaluatorUpdateRequestReceived(request);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Finds the concept (searches descendents) with the provided name. All concepts have a unique
     * name.
     *
     * @param nameToFind the name to find.
     * @param concepts the list of concepts to check.
     * @return the concept with the provided name. Can be null if no concept was
     *         found with the same name.
     */
    private Concept findConceptByName(String nameToFind, Collection<Concept> concepts) {
        if (concepts == null || concepts.isEmpty()) {
            return null;
        }

        for (Concept concept : concepts) {
            if (StringUtils.equalsIgnoreCase(nameToFind, concept.getName())) {
                return concept;
            }

            if (concept instanceof IntermediateConcept) {
                IntermediateConcept intermediateConcept = (IntermediateConcept) concept;
                Concept foundConcept = findConceptByName(nameToFind, intermediateConcept.getConcepts());
                if (foundConcept != null) {
                    return foundConcept;
                }
            }
        }

        return null;
    }
    
    /**
     * Retrieve the task course node ids (UUIDs) for any task that is either directly referenced
     * in the provided set of DKF performance node ids (integers) or contains a descendant concept
     * with the DKF performance node id.
     * @param taskConceptDomainKnowledgeIds contains unique collection of task and concept DKF performance nodes ids to use
     * to gather the task course node ids for.
     * @return a unique set of task course node ids.  Can be smaller size than the provided set if a task was either
     * not found or more than one entry in the provided set was found under the same task. Will return an empty set
     * if the provided set is null or empty.
     */
    public Set<String> getCourseTaskIds(Set<Integer> taskConceptDomainKnowledgeIds){
        
        Set<String> taskIds = new HashSet<>();
        if(CollectionUtils.isNotEmpty(taskConceptDomainKnowledgeIds)) {
            for(Integer taskConceptId : taskConceptDomainKnowledgeIds) {
                for(Task task : tasks) {
                    
                    ProxyTaskAssessment taskAssessment = task.getAssessment();
                    if(taskAssessment != null) {
                        
                        if(taskConceptId.equals(taskAssessment.getNodeId())) {                    
                            // the task is a match
                            taskIds.add(taskAssessment.getCourseNodeId().toString());
                            break;
                        }else {
                            //check the descendant concepts
                            
                            for(Concept concept : task.getConcepts()) {
                                
                                if(concept.containsConceptDomainKnowledgeId(taskConceptId)) {
                                    // the task contains the concept with the id
                                    taskIds.add(taskAssessment.getCourseNodeId().toString());
                                    break;
                                }
                            }
                        }
                    }
                    
                }
            }
        }
        
        return taskIds;
    }

    /**
     * Populates the foundCourseConcepts set with the set of course concepts
     * that are under this scenario's set of task(s) and matches the filter
     * criteria provided.
     * 
     * @param searchFilter the filter criteria to apply to the concepts under
     *        this scenario's set of task(s).
     * @param foundCourseConcepts the set of course concepts found to be a match
     *        by string comparison from the course concepts set.
     */
    public void getCourseConcepts(CourseConceptSearchFilter searchFilter, Set<String> foundCourseConcepts) {
        if (searchFilter == null) {
            throw new IllegalArgumentException("The parameter 'searchFilter' cannot be null.");
        } else if (CollectionUtils.isEmpty(searchFilter.getCourseConceptNames())) {
            return;
        } else if (foundCourseConcepts == null) {
            throw new IllegalArgumentException("The found course concepts set is null.");
        }

        for (Task task : tasks) {
            for (Concept concept : task.getConcepts()) {
                if (searchFilter.applyFilter(concept)) {
                    concept.setCourseConcept(true);
                    foundCourseConcepts.add(concept.getName().toLowerCase());
                }

                if (concept instanceof IntermediateConcept) {
                    ((IntermediateConcept) concept).getCourseConcepts(searchFilter, foundCourseConcepts, concept.isCourseConcept());
                }
            }
        }
    }

    /**
     * Populate the Entity Id of the learner for this Scenario based on the message provided.  If the message doesn't help
     * identify the learner in the training application then false will be returned.
     *
     * @param message - a simulation message
     * @return boolean - whether the learner has been assigned an entity id for the lesson.
     */
    private boolean populateEntityId(Message message){

        if(teamOrganization != null && message != null && message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            // update all team member entity ids
            // This will handle updating entity ids if they have changed

            EntityState es = (EntityState)message.getPayload();
            TeamMember<?> foundTeamMember = TeamUtil.getTeamMemberByEntityState(es, cachedTeamPoint, teamOrganization.getRootTeam());

            if(foundTeamMember != null){
                // a team member was found in the team organization that matches the entity state message
                // information received, set the entity identifier to that team organization member

                if(!es.getEntityID().equals(foundTeamMember.getEntityIdentifier())){
                    // the incoming entity id is different than the last saved entity id for the team member, therefore update it
                    // We have seen this happen in VBS when the learner mounts a vehicle and then dismounts, the learner is given
                    // a new DIS entity id.
                    
                    //make a copy so no other logic can manipulate the entity id
                    EntityIdentifier entityId = new EntityIdentifier(new SimulationAddress(es.getEntityID().getSimulationAddress().getSiteID(),
                            es.getEntityID().getSimulationAddress().getApplicationID()), es.getEntityID().getEntityID());
    
                    //a role corresponding to this learner identification was found, so associate it with the learner's entity
                    entityId.setRoleName(foundTeamMember.getName());
    
                    foundTeamMember.setEntityIdentifier(entityId);
    
                    if(logger.isDebugEnabled()){
                        logger.debug("set team member learner entity id to be: "+entityId+" for "+foundTeamMember);
                    }
                }

            }
        }

        // no learner entity ids were updated
        return false;
    }
    
    /**
     * Update the black board used by this session with any information that can be used from the given message.
     * 
     * @param message the incoming message that might be used to update the Black board.
     */
    private void handleBlackboardUpdates(Message message){
        
        if(blackboard == null){
            
            if(message instanceof DomainSessionMessage){
                blackboard = SessionConditionsBlackboardMgr.getInstance().getSessionBlackboard(((DomainSessionMessage)message).getDomainSessionId());
            }else{
                return;
            }
        }
        
        if(message.getMessageType() == MessageTypeEnum.VARIABLE_STATE_RESULT){
            VariablesStateResult result = (VariablesStateResult)message.getPayload();
            
            VariablesState varsState = result.getVariablesState();
            Map<VARIABLE_TYPE, Map<String, VariableState>> varTypeMap = varsState.getVariableTypeMap();
            for(VARIABLE_TYPE varType : varTypeMap.keySet()){
                
                switch(varType){
                case WEAPON_STATE:
                    Map<String, VariableState> entityToWeaponVar = varTypeMap.get(varType);
                    
                    // update the shared state information for all conditions that might need it
                    for(String entityMarking : entityToWeaponVar.keySet()){
                        ConditionEntityState state = blackboard.getConditionEntityState(entityMarking);
                        state.setWeaponState((WeaponState) entityToWeaponVar.get(entityMarking));
                    }
                    
                    // update that these entities are no longer awaiting weapon state information
                    blackboard.removePendingWeaponStateResultEntities(entityToWeaponVar.keySet());
                    break;
                case ANIMATION_PHASE:
                case VARIABLE:
                    
                    Map<String, VariableState> entityToAnimVar = varTypeMap.get(varType);
                    
                    // update the shared state information for all conditions that might need it
                    for(String entityMarking : entityToAnimVar.keySet()){
                        ConditionEntityState state = blackboard.getConditionEntityState(entityMarking);
                        VariableNumberState variableState = (VariableNumberState) entityToAnimVar.get(entityMarking);
                        
                        ConditionEntityStateNumberVariable conditionVariable = state.getVariable(variableState.getVarName());
                        if(conditionVariable == null){
                            conditionVariable = new ConditionEntityStateNumberVariable(variableState, -1);
                        }
                        state.addVariable(variableState.getVarName(), conditionVariable);
                    }
                    
                    break;
                }
            }

        }
    }

    /**
     * Return whether or not this scenario should end based on its stop trigger(s)
     *
     * @param message a message to provide to this task's end triggers
     * @return information about the trigger that is ending this scenario.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(Message message){

        if(endTriggers != null){
            for(AbstractTrigger trigger : endTriggers){

                try{
                    if(trigger.shouldActivate(message)){

                        if(logger.isInfoEnabled()){
                            logger.info("The scenario: "+this+" is finished because the end trigger: "+trigger+" was activated");
                        }

                        EndTriggerInformation info = new EndTriggerInformation(trigger);
                        return info;
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving scenario end trigger of "+trigger+".", e);
                }
            }
        }

        return null;
    }

    /**
     * Return whether or not this scenario should end based on its stop trigger(s)
     *
     * @param concept a concept to provide to this task's end triggers
     * @return information about the trigger that is ending this scenario.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(Concept concept){

        if(endTriggers != null){
            for(AbstractTrigger trigger : endTriggers){

                try{
                    if(trigger.shouldActivate(concept)){
                        if(logger.isInfoEnabled()){
                            logger.info("The scenario: "+this+" is finished because the end trigger: "+trigger+" was activated");
                        }

                        EndTriggerInformation info = new EndTriggerInformation(trigger);
                        return info;
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving scenario end trigger of "+trigger+".", e);
                }
            }
        }

        return null;
    }

    /**
     * Return whether or not this scenario should end based on its stop trigger(s)
     *
     * @param task the task that is being checked
     * @return information about the trigger that is ending this scenario.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(Task task){

        if(endTriggers != null){
            for(AbstractTrigger trigger : endTriggers){

                try{
                    if(trigger.shouldActivate(task, null)){
                        if(logger.isInfoEnabled()){
                            logger.info("The scenario: "+this+" is finished because the end trigger: "+trigger+" was activated");
                        }

                        EndTriggerInformation info = new EndTriggerInformation(trigger);
                        return info;

                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving scenario end trigger of "+trigger+".", e);
                }
            }
        }

        return null;
    }
    
    /**
     * Check whether or not this scenario should end based on the fact that the strategy with the given name was applied.
     * 
     * @param strategyName the unique name of a strategy that was applied.
     * @return information about the trigger that is ending this scenario.  Can be null if no trigger
     * evaluated to true with the given parameter.
     */
    private EndTriggerInformation shouldEnd(String strategyName){
        
        if(active){
            for(AbstractTrigger trigger : endTriggers){
                
                try{
                    if(trigger.shouldActivate(strategyName)){
                        
                        if(logger.isInfoEnabled()){
                            logger.info("The scenario: "+this+" is finished because the end trigger: "+trigger+" was activated");
                        }
                        
                        EndTriggerInformation info = new EndTriggerInformation(trigger);
                        return info;
                    }
                }catch(Exception e){
                    logger.warn("Caught exception from misbehaving scenario end trigger of "+trigger+".", e);
                }
            }
        }
        
        return null;
    }

    /**
     * Schedule a delay to ending this scenario based on the trigger's delay value.  Then
     * call the logic normally executed when a end scenario trigger fires.
     *
     * @param trigger the trigger being fired based on logic checked else-where
     */
    private void delayedScenarioEnd(final AbstractTrigger trigger){

        if(delayedScenarioEndThread == null || !delayedScenarioEndThread.isAlive()){

            delayedScenarioEndThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        Thread.sleep((long)(trigger.getTriggerDelay() * 1000));
                    } catch (InterruptedException e) {
                        logger.error("The trigger sleep was interrupted unexpectedly.", e);
                    }

                    notifyScenarioEnded(LessonCompletedStatusType.LESSON_RULE);
                }
            });
            delayedScenarioEndThread.start();
        }
    }

    /**
     * Notify tasks that a concept has ended.
     *
     * @param rootTask - the task containing the concept that has ended
     * @param concept - the concept that ended
     */
    private void conceptEndedNotification(Task rootTask, Concept concept){

        for(Task aTask : tasks){

            if(aTask != rootTask){
                aTask.conceptEndedNotification(concept);
            }
        }

        updateStatusTool();

        EndTriggerInformation taskShouldEndInfo = shouldEnd(concept);
        if(taskShouldEndInfo != null){
            handleEndTriggerInformation(taskShouldEndInfo);
        }
    }

    /**
     * Notification that a concept has started.
     *
     * @param rootTask the task containing the concept that has started
     * @param concept the concept that started
     */
    private void conceptStartedNotification(Task rootTask, Concept concept){
        updateStatusTool();
    }

    /**
     * A concept assessment was updated.  Notify the other tasks of this event.
     *
     * @param ancestorTask the task containing the concept whose assessment was updated
     * @param concept the concept whose assessment was updated
     */
    private void conceptAssessmentNotification(Task ancestorTask, Concept concept){

        for(Task aTask : tasks){

            // Notify other tasks that this task has ended
            if(aTask != ancestorTask) {
                aTask.conceptUpdatedNotification(concept);
            }
        }

        EndTriggerInformation taskShouldEndInfo = shouldEnd(concept);
        if(taskShouldEndInfo != null){
            handleEndTriggerInformation(taskShouldEndInfo);
        }
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

        if(!active){
            return;
        }

        if(!endTaskTriggerInfo.getDomainActions().getStrategy().getStrategyActivities().isEmpty()){
            //there is a message to display to the user about the triggered event

            scenarioActionInterface.handleDomainActionWithLearner(endTaskTriggerInfo.getDomainActions());
        }

        if(endTaskTriggerInfo.getDelay() == null || endTaskTriggerInfo.getDelay() <= 0){
            //finish right away

            notifyScenarioEnded(LessonCompletedStatusType.LESSON_RULE);

        }else{
            //wait some amount of time to finish
            delayedScenarioEnd(endTaskTriggerInfo.getTrigger());
        }
    }

    /**
     * The scenario is ending, notify Domain assessment knowledge.
     * @param status information about why the scenario is ending.
     */
    private void notifyScenarioEnded(LessonCompletedStatusType status){

        if(active){
            active = false;

            triggerEventMgr.quit();

            for(Task aTask: tasks){
                aTask.assessmentEnded();
            }

            if(scenarioActionInterface != null){
                scenarioActionInterface.scenarioEnded(status);
            }

            if(autoCompleteTimer != null){
                autoCompleteTimer.cancel();
            }

            if(statusTool != null){
                if(logger.isInfoEnabled()){
                    logger.info("Closing status tool because the scenario has ended.");
                }
                statusTool.dispose();
            }

            /*
             * Nick 2/6/15 - Notify threads waiting on the trigger event queue mutex so that the Gateway Topic thread is allowed to continue if the
             * scenario ends but something keeps the trigger event manager from notifying the thread that the event queue is empty.
             */
            synchronized(triggerEventQueueMutex){
                triggerEventQueueMutex.notifyAll();
            }
        }
    }
    
    /**
     * Notification that the strategy with the name was just applied.  Can be used to notify
     * domain knowledge task triggers.
     * 
     * @param event contains information about the strategy that was applied.  Can't be null.
     */
    public void appliedStrategyNotification(StrategyAppliedEvent event){
        
        if(event == null){
            return;
        }
        
        if(active){
            // the scenario is active
            
            EndTriggerInformation scenarioShouldEndInfo = shouldEnd(event.getStrategyName());
            if(scenarioShouldEndInfo != null){
                handleEndTriggerInformation(scenarioShouldEndInfo);
                return;
            }
            
            for(Task task : tasks){
                
                try{
                    task.appliedStrategyNotification(event);
                }catch(Exception e){
                    logger.error("Caught exception while trying to notify the task that the strategy '"+event.getStrategyName()+"' was applied.\n"+task, e);
                }
            }
        }
        
    }

    /**
     * Create the Status tool and show it in a new dialog.
     * If the tool has already been instantiated, this will do nothing.
     */
    private void createAndShowStatusTool(){

        synchronized(statusToolCreationSemaphore){

            if(statusTool == null && DomainModuleProperties.getInstance().shouldShowNodeStatusTool()){
                statusTool = new PerformanceNodeStatusTool(this, currentAssessment);
                statusTool.setVisible(true);
                if(logger.isInfoEnabled()){
                    logger.info("Created performance node status tool.  It should be visible and populated with DKF nodes now.");
                }
            }
        }

    }

    /**
     * Update the status tool with the latest information for this scenario.
     * If the tool hasn't been instantiated, this will create it and show the dialog.
     */
    private synchronized void updateStatusTool(){

        if(statusTool == null){
            createAndShowStatusTool();
        }else{
            try{
                statusTool.updateTable(this, currentAssessment);
            }catch(Exception e){
                logger.error("Failed to update performance node status tool", e);
            }
        }
    }

    /**
     * A task assessment has been created.  Create a new Performance Assessment and provided it to the
     * domain knowledge for handling.
     *
     * @param task - the task that created the task assessment
     * @param taskAssessment - the task assessment created
     */
    private void taskAssessmentNotification(Task task, ProxyTaskAssessment taskAssessment){

        //create a new performance assessment in order to allow other threads
        //to change the current assessment and not effect the values.
        scenarioActionInterface.performanceAssessmentUpdated(currentAssessment);
        
        if(!started){
            sentStartAssessment = true;
        }

        updateStatusTool();
    }

    /**
     * The task has ended.  Check if this scenario has ended.
     *
     * @param task - the task that ended
     */
    private void taskEndedNotification(Task task){

        if(logger.isInfoEnabled()){
            logger.info("The task "+task+" has finished.  Checking if the scenario's assessment logic is finished.");
        }

        boolean stillActive = false;

        StringBuffer debugBuffer = null;
        if(logger.isDebugEnabled()){
            debugBuffer = new StringBuffer();
            debugBuffer.append("Tasks not finished: {");
        }

        //check if there are any unfinished tasks remaining
        //TODO: there could be a container of active tasks to make this check faster
        for(Task aTask : tasks){

            // Notify other tasks that this task has ended
            if(aTask != task) {
                aTask.taskEndedNotification(task);
            }

            if(!aTask.isFinished()){
                //found unfinished task, therefore scenario is not finished
                if(logger.isInfoEnabled()){
                    logger.info("The task "+aTask+" has yet to finish, therefore the scenario is still active and will continue to be assessed.");
                }
                stillActive = true;

                if(debugBuffer != null){
                    //building the debug message
                    debugBuffer.append(" ").append(aTask.getName()).append(",");
                }
            }
        }

        if(debugBuffer != null){
            debugBuffer.append("}");
            if(logger.isDebugEnabled()){
                logger.debug(debugBuffer.toString());
            }
        }

        updateStatusTool();
        scenarioActionInterface.performanceAssessmentUpdated(currentAssessment);  //send out perf assessment msg with new node state enum value for this task

        if(!stillActive){
            notifyScenarioEnded(LessonCompletedStatusType.LESSON_RULE);
        }else{
            EndTriggerInformation taskShouldEndInfo = shouldEnd(task);
            if(taskShouldEndInfo != null){
                handleEndTriggerInformation(taskShouldEndInfo);
            }
        }
    }

    /**
     * The task has started.
     *
     * @param task - the task that started
     */
    private void taskStartedNotification(Task task){

        updateStatusTool();
    }

    /**
     * Return the scenario name
     *
     * @return String - the scenario name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the scenario description. Supports HTML.
     *
     * @return String - the scenario description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Return the tasks associated with this scenario
     *
     * @return the tasks for this scenario, won't be empty or null.
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Return all the performance assessment nodes associated with this scenario
     *
     * @return Map<Integer, AbstractPerformanceAssessmentNode>
     */
    public Map<Integer, AbstractPerformanceAssessmentNode> getPerformanceNodes(){
        return dkfPerformanceNodes;
    }

    /**
     * Return the mission data available for this scenario
     *
     * @return Mission - Not null.
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * Return the resources available for this scenario
     *
     * @return Resources - Not null.
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * Return the team organization defined in the scenario object.
     *
     * @return the highest level team defined. Can be null if this is an individual scenario. Can be null.
     */
    public Team getRootTeam(){
        return teamOrganization != null ? teamOrganization.getRootTeam() : null;
    }

    /**
     * Return the team member associated with the learner for this assessment.
     *
     * @return can be null if not set yet or not needed
     */
    public TeamMember<?> getLearnerTeamMember(){
        return teamOrganization != null ? teamOrganization.getLearnerTeamMember() : null;
    }

    /**
     * Set the team member information for the learner of this assessment.
     * The team organization must be set before calling this method.
     *
     * @param learnerTeamMember Can't be null. If you wish to unset the learner's team
     * member than you need to provide another team member to set it too.
     */
    public void setLearnerTeamMember(TeamMember<?> learnerTeamMember){

        if(teamOrganization == null){
            throw new RuntimeException("The learner's team member can't be set because the team organization is null");
        }else if(learnerTeamMember == null){
            throw new IllegalArgumentException("The learner team member can't be null");
        }

        teamOrganization.setLearnerTeamMember(learnerTeamMember);
    }

    /**
     * Returns whether this scenario can support multiple concurrent learners for assessment.
     *
     * @return true if this scenario has a team organization with multiple playable learner team member positions.
     */
    public boolean supportsMultipleLearners(){
        return teamOrganization != null && teamOrganization.getRootTeam().getNumberOfPlayableTeamMembers() > 1;
    }

    /**
     * Get the scores for this scenario
     *
     * @return can be null if there is no scoring rules/information
     */
    public GradedScoreNode getScores(){

        /*
         * NOTE: LogFilePlaybackmMessageManager and LogFilePlaybackService rely on assigning the scenario name
         * to the root score node in order to identify which published lesson score messages contain summative
         * assessment scores from the scenario.
         * 
         * If the name assigned to the scenario's root node is changed for whatever reason, then those classes
         * should be changed as well to accomodate the change.
         */
        
        //build the Scenario level score node hierarchy
        GradedScoreNode node = new GradedScoreNode(name);

        for(Task task : tasks){

            //don't include scores for tasks that have never executed (ticket #1259)
            if(task.isActive() || task.isFinished()){
                try{
                    GradedScoreNode child = task.getScore();
                    if(child != null){
                        node.addChild(child);
                    }
                }catch(Exception e){
                    throw new RuntimeException("An error happened while trying to calculate the score for the task '"+task.getName()+"'.", e);
                }
            }
        }

        if(node.isLeaf()){
            //no scoring information was given
            return null;
        }else{
            // don't recalculate the descendant GradedScoreNodes which were just calculated above with task.getScore()
            ScoreUtil.performAssessmentRollup(node, false);
            return node;
        }
    }

    @Override
    public void assessTasks() {

        for(Task task : tasks){
            task.assessConcepts();
        }

    }
    
    /**
     * Clears any observer-specific metadata from the current assessment for this scenario,
     * namely any comments that were added by an observer.
     */
    public void clearObserverMetadata() {
        
        if(currentAssessment == null) {
            return;
        }
        
        currentAssessment.setEvaluator(null);
        currentAssessment.setObserverComment(null);
        currentAssessment.setObserverMedia(null); 
    }
    
    /**
     * Gets the handler used to manage assessment variables, which share information about ongoing
     * automated assessments throughout the knowledge session
     * 
     * @return the variables handler. Will not be null.
     */
    public VariablesHandler getVarsHandler() {
		return varsHandler;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[").append(this.getClass().getSimpleName()).append(": ");
        sb.append(" name = ").append(getName());
        sb.append(", description = ").append(getDescription());

        sb.append(", Tasks = ");
        for(Task task : getTasks()){
            sb.append(task).append(", ");
        }

        sb.append(", Mission = ").append(getMission());
        sb.append(", Resources = ").append(getResources());

        sb.append("]");

        return sb.toString();
    }

    /**
     * This is the implementation for the task action interface which handles the
     * tasks' actions.
     *
     * @author mhoffman
     *
     */
    private static class TaskActionHandler implements TaskActionInterface{

        /** manages scenario start/end triggers */
        private TriggerEventManager triggerEventMgr;

        /** used to notify BaseDomainSession of scenario events (e.g. performance assessments) */
        private ScenarioActionInterface scenarioActionInterface;

        /**
         * Set attributes
         * @param triggerEventMgr manages scenario start/end triggers.  Can't be null.
         */
        public TaskActionHandler(TriggerEventManager triggerEventMgr){

            if(triggerEventMgr == null){
                throw new IllegalArgumentException("The trigger event manager can't be null");
            }
            this.triggerEventMgr = triggerEventMgr;
        }

        /**
         * Set the interface used to notify the BaseDomainSession of scenario events (e.g. performance assessments)
         *
         * @param scenarioActionInterface can't be null
         */
        public void setScenarioActionInterface(ScenarioActionInterface scenarioActionInterface){

            if(scenarioActionInterface == null){
                throw new IllegalArgumentException("The scenario action interface can't be null");
            }
            this.scenarioActionInterface = scenarioActionInterface;
        }

        @Override
        public void taskStarted(Task task){
            triggerEventMgr.addEvent(new TaskStartedEvent(task));
        }

        @Override
        public void taskEnded(Task task){
            triggerEventMgr.addEvent(new TaskEndedEvent(task));
        }

        @Override
        public void taskAssessmentCreated(TaskAssessmentEvent taskAssessmentEvent){

            if (logger.isDebugEnabled()) {
                logger.debug("Scenario's interface has created an out-of-game-state-sequence performance assessment to send out from "+taskAssessmentEvent);
            }

            triggerEventMgr.addEvent(taskAssessmentEvent);
        }

        @Override
        public void conceptStarted(Task ancestorTask, Concept concept) {
            triggerEventMgr.addEvent(new ConceptStartedEvent(ancestorTask, concept));
        }

        @Override
        public void conceptEnded(Task ancestorTask, Concept concept) {
            triggerEventMgr.addEvent(new ConceptEndedEvent(ancestorTask, concept));
        }

        @Override
        public void conceptAssessmentCreated(Task ancestorTask, Concept concept){
            triggerEventMgr.addEvent(new ConceptAssessmentEvent(ancestorTask, concept));
        }

        @Override
        public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
            scenarioActionInterface.trainingApplicationRequest(infoRequest);
        }


        @Override
        public void handleDomainActionWithLearner(DomainAssessmentContent action) {
            if(scenarioActionInterface != null){
                scenarioActionInterface.handleDomainActionWithLearner(action);
            }else{
                logger.error("Unable to display the following information to the learner because the scenario action interface was not set:\n"+action);
            }
        }

        @Override
        public void fatalError(String reason, String details) {
            scenarioActionInterface.fatalError(reason, details);
        }

        @Override
        public void displayDuringLessonSurvey(
                AbstractSurveyLessonAssessment surveyAssessment,
                SurveyResultListener surveyResultListener) {

            if(scenarioActionInterface != null){
                scenarioActionInterface.displayDuringLessonSurvey(surveyAssessment, surveyResultListener);
            }else{
                logger.error("Unable to display the survey to the learner because the scenario action interface was not set:\n"+surveyAssessment);
            }
        }

        @Override
        public SessionMembers getSessionMembers() {
            return scenarioActionInterface.getSessionMembers();
        }

        @Override
        public List<MessageManager> getPlaybackMessages() {
            return scenarioActionInterface.getPlaybackMessages();
        }

		}
        
    /**
     * Used to process trigger events at the scenario level.
     *
     * @author mhoffman
     *
     */
    private class TriggerEventScenarioHandler implements TriggerEventHandler{

        @Override
        public void handleConceptStarted(ConceptStartedEvent event) {
            conceptStartedNotification(event.getAncestorTask(), event.getConcept());
        }

        @Override
        public void handleConceptEnded(ConceptEndedEvent event) {
            conceptEndedNotification(event.getAncestorTask(), event.getConcept());
        }

        @Override
        public void handleConceptAssessment(ConceptAssessmentEvent event) {
            conceptAssessmentNotification(event.getAncestorTask(), event.getConcept());
        }

        @Override
        public void handleTaskStarted(TaskStartedEvent event) {
            taskStartedNotification(event.getTask());
        }

        @Override
        public void handleTaskEnded(TaskEndedEvent event) {
            taskEndedNotification(event.getTask());
        }

        @Override
        public void handleTaskAssessment(TaskAssessmentEvent event) {
            taskAssessmentNotification(event.getTask(), event.getTaskAssessment());
        }

        @Override
        public void eventQueueEmpty() {
            handleDomainKnowledgeEventQueueEmpty();
        }

    }
}
