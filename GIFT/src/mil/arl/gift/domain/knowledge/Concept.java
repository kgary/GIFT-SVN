/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.domain.DomainKnowledgeManager;
import mil.arl.gift.domain.knowledge.common.AbstractLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AssessmentProxyManager;
import mil.arl.gift.domain.knowledge.common.ConceptActionInterface;
import mil.arl.gift.domain.knowledge.common.ConditionActionInterface;
import mil.arl.gift.domain.knowledge.common.ConditionLessonAssessmentHandlerInterface;
import mil.arl.gift.domain.knowledge.common.SurveyResponseAssessmentListener;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.AssessmentUpdateEventType;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.net.api.message.Message;

/**
 * This class contains information about a concept that can be assessed.
 * 
 * @author mhoffman
 *
 */
public class Concept extends AbstractPerformanceAssessmentNode 
        implements ConditionLessonAssessmentHandlerInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Concept.class);
    
    /** the conditions and current assessments for those conditions 
     * Note: using a map is useful for retrieving the collection of assessment levels at once and allowing the map to change
     *       while processing over that container
     */
    private Map<AbstractCondition, AssessmentLevelEnum> conditionToAssessment;
    
    /** 
     * list of conditions provided to this concept 
     * - useful for iterating over the condition in the order that was authored 
     */
    private List<AbstractCondition> conditionsList;
    
    /** the current assessment of this concept */   
    protected ConceptAssessment currentAssessment;
    
    /** this is the interface which needs to be called to notify the task of concept actions (e.g. the concept has an assessment) */
    protected ConceptActionInterface conceptActionInterface;
    
    /** handles the conditions' condition actions */
    private ConditionActionHandler conditionActionHandler = new ConditionActionHandler();
    
    /** flag used to indicate if the concept should be check for completion */
    private boolean needCompletionCheck = false;
    
    /** if not null, then it is the current concept's condition that is handling a simulation message */
    private AbstractCondition currentConditionWithSimMsg = null;
    
    /** The optional arguments to be passed into the algorithm that handles this concept's performance metrics */
    private PerformanceMetricArguments performanceArguments;
    
    /** whether this DKF concept is also a course concept */
    private boolean isCourseConcept = false;    
    
    /** whether this concept has an ancestor intermediate concept that is a course concept */
    private boolean hasAncestorCourseConcept = false;
        
    /**
     * Class constructor  - set attributes and calculate initial assessment
     * 
     * @param nodeId - the unique node id for this performance assessment node
     * @param name - the display name of this concept
     * @param conditions - the conditions associated with this concept
     * @param assessments - additional assessments (e.g. survey assessment) for this concept
     * @param initialPriority - the priority of the concept (optional, can be null)
     * @param scenarioSupport flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view. 
     */
    public Concept(int nodeId, String name, List<AbstractCondition> conditions, List<AbstractLessonAssessment> assessments, BigInteger initialPriority, boolean scenarioSupport){
        super(nodeId, name, assessments);
        
        conditionsList = conditions;
        
        this.setScenarioSupportNode(scenarioSupport);

        conditionToAssessment = new HashMap<AbstractCondition, AssessmentLevelEnum>(conditions.size());
        Map<String, AssessmentLevelEnum> teamOrgEntries = new HashMap<>();
        boolean hasObservedAssessmentChild = false;
        for(AbstractCondition condition : conditions){
            if (condition instanceof ObservedAssessmentCondition) {
                hasObservedAssessmentChild = true;
            }
            conditionToAssessment.put(condition, condition.getAssessment());
            
            //validating that the condition class will register for interest in one or more simulation messages
            List<MessageTypeEnum> simInterest = condition.getSimulationInterests();
            if(simInterest == null || simInterest.isEmpty()){
                throw new ConfigurationException("Found incomplete condition class of '"+condition+"' in concept named '"+name+"'.",
                        "The condition class of "+condition+" is not properly registering for notification of simulation messages. " +
                		"Therefore it will never be called upon during a lesson.  Please look at the condition's class method named 'getSimulationInterets' for more information.",
                		null);
            }
            
            generated.dkf.TeamMemberRefs teamOrgRefs = condition.getTeamOrgRefs();
            if (teamOrgRefs != null) {
                for (String memberRef : teamOrgRefs.getTeamMemberRef()) {
                    teamOrgEntries.put(memberRef, AssessmentLevelEnum.UNKNOWN);
                }
            }
        }
        
        currentAssessment = new ConceptAssessment(name, AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), nodeId, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
        currentAssessment.setContainsObservedAssessmentCondition(hasObservedAssessmentChild);
        currentAssessment.setScenarioSupportNode(scenarioSupport);
        
        // set the priority (which is optional).  
        // NOTE: perhaps this should use the Priority Metric class in AbstractPerformanceAssessmentNode
        if (initialPriority != null) {
            currentAssessment.updatePriority(initialPriority.intValue());
        }
        
        currentAssessment.addAssessedTeamOrgEntries(teamOrgEntries);

        courseNodeId = currentAssessment.getCourseNodeId();
        
        //force an analysis of this concept's conditions default assessments
        updateAssessment(AssessmentUpdateEventType.CONCEPT_CREATED);
    }
    
    /**
     * Return this concept's conditions.
     * Note: care should be taken when using the returned collection due to the frequency at which conditions may change, etc.
     * 
     * @return Set<AbstractCondition>
     */
    public Set<AbstractCondition> getConditions(){
        return conditionToAssessment.keySet();
    }
    
    /**
     * Return the current concept assessment for this concept
     * 
     * @return the current assessment of this concept.  Will be null if {@link #cleanup()} was called.
     */
    @Override
    public ConceptAssessment getAssessment(){
        return currentAssessment;
    }
    
    @Override
    protected void addAssessmentExplanation(String assessmentExplanation){
        currentAssessment.addAssessmentExplanation(assessmentExplanation);
    }

    /**
     * Method that is executed when the concept's assessment is updated externally (not from the
     * condition assessments).
     */
    public void assessmentUpdatedExternally() {
        for (AbstractCondition condition : conditionsList) {
            condition.assessmentUpdatedExternally();
        }
    }

    /**
     * Initialize the concept and populate the simulation message interests of this concept.
     * 
     * @param simInterests - the collection of simulation message interests for this condition's parent. Can't be null.
     * @param conceptActionInterface - the interface used by this concept to communicate with the task. Can't be null.
     */
    public void initialize(Set<MessageTypeEnum> simInterests, ConceptActionInterface conceptActionInterface){
        
        if(logger.isInfoEnabled()){
            logger.info("Initializing Concept named "+getName());
        }
        
        this.conceptActionInterface = conceptActionInterface;
        
        for(AbstractCondition condition : conditionsList){
            condition.initialize(conditionActionHandler);
            
            List<MessageTypeEnum> simInterest = condition.getSimulationInterests();            
            simInterests.addAll(simInterest);
        }
        
        assessmentProxy = AssessmentProxyManager.getInstance().getAssessmentProxy(this);
        fireAssessmentUpdate(false, AssessmentUpdateEventType.CONCEPT_INITIALIZED);
        
    }
    
    /**
     * Set the variable handler used to handle retrieval of assessment variables that have 
     * been provided during course execution.
     * 
     * @param varsHandler can't be null.
     */
    public void setVarsHandler(VariablesHandler varsHandler){
        
        for(AbstractCondition condition : conditionsList){
            condition.setVarsHandler(varsHandler);
        }
    }
    
    /**
     * Start the concept.  This means the task has been (or is being) started.
     */
    public void start(){        

        if(logger.isInfoEnabled()){
            logger.info("Starting Concept named '"+getName()+"'.");
        }
        
        active = true;
        finished = false;
        if(currentAssessment != null){
            currentAssessment.setNodeStateEnum(PerformanceNodeStateEnum.ACTIVE);
        }
        
        for(AbstractCondition condition : conditionsList){
            condition.start();
        }
    }
    
    /**
     * Stop the concept.  This means the task is being stopped.
     */
    public void stop(){
        
        if(logger.isInfoEnabled()){
            logger.info("Stopping Concept named '"+getName()+'.');
        }
        
        for(AbstractCondition condition : conditionsList){
            condition.stop();
        }
        
        active = false;
        if(currentAssessment != null){
            currentAssessment.setNodeStateEnum(PerformanceNodeStateEnum.DEACTIVATED);
        }
    }
    
    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes. 
     */
    public void cleanup(){
        
        active = false;
        
        AssessmentProxyManager.getInstance().unregisterNode(this);
        
        // synchronize to make sure the concept metrics aren't being updated right now
        // since that logic sets the calculated values in this object
        synchronized (currentAssessment) {
            currentAssessment = null;
        }
        
        conditionActionHandler = null;
        conceptActionInterface = null;
        conditionsList.clear();
        conditionToAssessment.clear();
    }
    
    @Override
    public boolean handleConversationAssessment(List<ConversationAssessment> assessments){
        
        if(currentAssessment == null){
            return false;
        }
        
        boolean changed = super.handleConversationAssessment(currentAssessment, assessments);
        
        if(changed){
            //provide to concept's conditions
            for(AbstractCondition condition : conditionsList){
                condition.handleConversationAssessment(assessments);
            }
        }
        
        return changed;
    }
    
    @Override
    public void evaluatorUpdateRequestReceived(EvaluatorUpdateRequest request) {
        
        /* update the assessment metrics */
        updatePerformanceAssessmentMetrics(request);

        /* assessment was updated manually so notify parents and child conditions */
        fireAssessmentUpdate(true, AssessmentUpdateEventType.CONCEPT_EVALUATOR_UPDATE);
        assessmentUpdatedExternally();
    }
    
    @Override
    public AbstractAssessment handleTrainingAppGameState(Message message){
        
        boolean assessmentChanged = false;
        
        for(AbstractCondition condition : conditionsList){
            
            currentConditionWithSimMsg = condition;
            
            try{                
                if(condition.handleTrainingAppGameState(message)){
                    //update condition's assessment
                    
                    assessmentChanged = true;

                    if(logger.isDebugEnabled()){
                        logger.debug("Updating condition's known assessment of "+condition+" from "+conditionToAssessment.get(condition)+
                                " to "+condition.getAssessment()+" because of a simulation message");
                    }
                    conditionToAssessment.put(condition, condition.getAssessment());
                    
                    //TESTING - this logic can be used to test priority values by changing the priority each time
                    //          the concept falls below expectation which can show dynamically changing priorities in GIFT.
                    //          Example: (init) priority = 1 -> 10-1 = 9 -> 10-9 = 1 -> and so on...
                    /*
                    if(level == AssessmentLevelEnum.BELOW_EXPECTATION && this.getPriority() != null){
                        
                        Integer oldPriority = this.getPriority();
                        this.setPriority(10-oldPriority);
                    }
                    */
                    
                    //reset survey assessment in order to use condition assessment(s)
                    latestSurveyAssessment = AssessmentLevelEnum.UNKNOWN;
                }
            }catch(Exception e){
                logger.error("Caught exception from misbehaving condition class of "+condition+".  Continuing with handling training app game state message of\n"+message+".", e);
                continue;
            }
            

        }
        
        //reset
        currentConditionWithSimMsg = null;
        
        //if this concept assessment was updated, the assessment will be returned
        boolean returnAssessment = false;
        if(assessmentChanged && updateAssessment(AssessmentUpdateEventType.CONDITION_SYNC_UPDATED)){
        	returnAssessment = true;
        }
        
        if(needCompletionCheck){
            
            //reset
            needCompletionCheck = false;
            
            if(isCompleted()){
                finished = true;
                if(currentAssessment != null){
                    currentAssessment.setNodeStateEnum(PerformanceNodeStateEnum.FINISHED);
                }
                conceptActionInterface.conceptEnded(this);
            }
        }
        
        //if this concept assessment was updated, return the assessment
        if(returnAssessment){
            return currentAssessment;
        }else {
        	return null;
        }

    }
    
    @Override
    public boolean hasScoringRules(){        

        boolean hasScorers = false;
        
        //check this concept's conditions
        for(AbstractCondition condition : conditionsList){
            
            hasScorers |= condition.hasScorers();
            
            if(hasScorers){
                break;
            }
        }
        
        return hasScorers;
    }
    
    @Override
    protected synchronized boolean updateAssessment(AssessmentUpdateEventType eventType){
        
        //don't over-ride a survey assessment if it has been calculated until a condition changes it's assessment
        if(currentAssessment != null && latestSurveyAssessment == AssessmentLevelEnum.UNKNOWN){

            // synchronize and then check for null to handle a race condition where cleanup method
            // could be called on another thread which would set the assessment value to null
            synchronized(currentAssessment){
                if (currentAssessment == null) {
                    return false;
                }

                /* If the metrics are being updated by the system, then the
                 * evaluator is null */
                currentAssessment.setEvaluator(null);

                calculatePerformanceMetric();
                calculateConfidenceMetric();
                calculateCompetenceMetric();
                calculateTrendMetric();
                calculatePriorityMetric();
            
                fireAssessmentUpdate(false, eventType);
            }
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void fireAssessmentUpdate(boolean notifyParentNode, AssessmentUpdateEventType eventType){ 
        
        if(assessmentProxy != null){
            assessmentProxy.fireAssessmentUpdate(currentAssessment);
        }
    
        //the interface can be null during initialization, right before the task provides a value for the interface
        if(conceptActionInterface != null && notifyParentNode){
        	//this concept's assessment level changed, notify this concept's parent performance node  
            conceptActionInterface.conceptAssessmentCreated(this, currentAssessment);
        }
    }
    
    /**
     * Handle the condition assessment update event by updating the condition's assessment of that condition
     * and notifying the condition's concept.
     * 
     * @param condition the child condition whose assessment is being updated
     */
    private void handleConditionUpdate(AbstractCondition condition){
        
        //reset survey assessment in order to use condition assessment(s)
        latestSurveyAssessment = AssessmentLevelEnum.UNKNOWN;
        
        AssessmentLevelEnum assessment = condition.getAssessment();
        
        if(assessment != null){
            //update this concept's knowledge of the condition's assessment
            
            if(logger.isDebugEnabled()){
                logger.debug("Updating out-of-game-state-sequence condition's known assessment of "+condition+".");
            }
            
            conditionToAssessment.put(condition, assessment);
            
            if(updateAssessment(AssessmentUpdateEventType.CONDITION_ASYNC_UPDATED) && currentAssessment != null){
            	//this concept's assessment level changed because of the change in the condition's assessment level,
            	//notify this concept's parent performance node                
                conceptActionInterface.conceptAssessmentCreated(this, currentAssessment);
            }
        }
        
    }
    
    /**
     * Return whether the concept's conditions have completed or not.
     * 
     * @return boolean
     */
    public boolean isCompleted(){
        
        //check if other conditions have finished
        boolean conceptCompleted = true;
        for(AbstractCondition aCondition : conditionsList){
            
            if(!aCondition.hasCompleted()){
                conceptCompleted = false;
                break;
            }
        }  
        
        return conceptCompleted;
    }
    
    /**
     * Handle the condition completing event by checking if this condition is now completed.  If so
     * notify the concept.
     * 
     * @param condition - the condition that just completed
     */
    private synchronized void handleConditionCompleted(AbstractCondition condition){     
        
        if(currentConditionWithSimMsg == condition){
            //this is the current condition being given a simulation message and that condition
            //has completed.  Delay the concept isCompleted check until all conditions have had a chance
            //to receive the simulation message
            
            needCompletionCheck = true;
            
        }else if(isCompleted()){
            //the condition is completed on a different thread than the handle simulation message method calls,
            //no need to wait, notify the parent concept that this condition has finished
            
            if(logger.isDebugEnabled()){
                logger.debug("All conditions have finished for concept "+getName()+", therefore the concept is finished");
            }
            finished = true;
            
            if(currentAssessment != null){
                currentAssessment.setNodeStateEnum(PerformanceNodeStateEnum.FINISHED);
            }
            conceptActionInterface.conceptEnded(this);
        }
    }
    
    /**
     * Get the overall assessment score for this concept.  This should be called
     * at the end of the real time assessment.  
     *
     * @param courseConceptDescendant true if this DKF concept is a descendant of another DKF concept that
     * is also a course concept.  
     * @return new score node with a score calculated from the child overall assessment scores.  Can be
     * null if the descendants have no overall assessment scores.  Will also be null if this concept
     * is not a course concept or not a descendant of a course concept.
     */
    public GradedScoreNode getScore(boolean courseConceptDescendant){
        
        if(!courseConceptDescendant && !isCourseConcept()){
            // only course concepts or descendants of course concepts can have overall assessments
            return null;
        }
        
        GradedScoreNode node = new GradedScoreNode(getName());
        node.setPerformanceNodeId(getNodeId());
        calculateGradeMetric(node);
        
        //if the concept's conditions have no scorer nodes below it, than this concept node doesn't need to be added 
        if(node.isLeaf()){
            return null;   
        }else{
            return node;
        }
    }
    
    @Override
    public void assessConditions() {
        
        if(logger.isInfoEnabled()){
            logger.info("Assessing conditions for "+this);
        }

        for(AbstractCondition condition : conditionsList){
            condition.assessCondition();
        }
    }

    @Override
    protected void assessmentEnded() {
        //nothing to do
    }
    
    @Override
    protected Map<UUID, PerformanceMetricArguments> getChildConceptOrConditionPerformanceMetricArgs() {

        if(childAssessmentWeights == null){
            // collect conditions
            childAssessmentWeights = new HashMap<>();
            for(AbstractCondition condition : conditionsList){
                childAssessmentWeights.put(condition.getId(), condition.getPerformanceArguments());
            }
        }
        return childAssessmentWeights;
    }
    
    /**
     * Gets the arguments to be passed into the algorithm that handles this concept's performance metrics
     * 
     * @return the performance metric algorithm arguments. Can be null.
     */
    public PerformanceMetricArguments getPerformanceArguments() {
        return performanceArguments;
    }

    /**
     * Sets the arguments to be passed into the algorithm that handles this concept's performance metrics
     * 
     * @param performanceArguments the performance metric algorithm arguments. Can be null.
     */
    public void setPerformanceArguments(PerformanceMetricArguments performanceArguments) {
        this.performanceArguments = performanceArguments;
    }
    
    /**
     * Return whether this DKF concept is a course concept.
     * @return true if this DKF concept is also a course concept. Default is false.
     */
    public boolean isCourseConcept() {
        return isCourseConcept;
    }

    /**
     * Set whether this DKF concept is a course concept.
     * @param isCourseConcept true if this DKF concept is also a course concept
     */
    public void setCourseConcept(boolean isCourseConcept) {
        this.isCourseConcept = isCourseConcept;
    }    
    
    /**
     * Return whether this concept has an ancestor intermediate concept that is a course concept
     * @return default is false
     */
    public boolean hasAncestorCourseConcept(){
        return hasAncestorCourseConcept;        
    }
    
    /**
     * Return whether this concept has the id provided.
     * @param conceptDomainKnowledgeId the DKF performance node id to check for in this concept
     * @return true if a match is found.
     */
    public boolean containsConceptDomainKnowledgeId(int conceptDomainKnowledgeId) {        
        return conceptDomainKnowledgeId == this.getNodeId();
    }
    
    /**
     * Set whether this concept has an ancestor intermediate concept that is a course concept
     * @param hasAncestorCourseConcept true if an ancestor to this concept is a course concept
     */
    protected void setHasAncestorCourseConcept(boolean hasAncestorCourseConcept){
        this.hasAncestorCourseConcept = hasAncestorCourseConcept;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Concept: ");
        sb.append(super.toString());
        sb.append(", courseConcept = ").append(isCourseConcept);
        sb.append(", ancestor course concept = ").append(hasAncestorCourseConcept);

        sb.append(", Conditions = ");
        for(AbstractCondition condition : conditionsList){
            sb.append(condition).append(", ");
        }
        
        sb.append("]");
        
        return sb.toString();
    }

    /**
     * This is the implementation for the condition action interface which handles the
     * conditions' actions.
     * 
     * @author mhoffman
     *
     */
    private class ConditionActionHandler implements ConditionActionInterface{
        
        @Override
        public void conditionAssessmentCreated(AbstractCondition condition){
            handleConditionUpdate(condition);
        }
        
        @Override
        public void conditionCompleted(AbstractCondition condition){
            handleConditionCompleted(condition);
        }

        @Override
        public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
            conceptActionInterface.trainingApplicationRequest(infoRequest);           
        }

        @Override
        public void handleDomainActionWithLearner(DomainAssessmentContent action) {
            conceptActionInterface.handleDomainActionWithLearner(action);
        }

        @Override
        public void fatalError(String reason, String details) {
            conceptActionInterface.fatalError(reason, details);
        }

        @Override
        public void addSurveyResponseAssessmentListener(SurveyResponseAssessmentListener listener) {
            addChildSurveyResponseAssessmentListener(listener);
        }

        @Override
        public void displayDuringLessonSurvey(
                AbstractSurveyLessonAssessment surveyAssessment,
                SurveyResultListener surveyResultListener) {
            conceptActionInterface.displayDuringLessonSurvey(surveyAssessment, surveyResultListener);            
        }
        
        @Override
        public void setConceptPriority(Integer priority) {
            currentAssessment.updatePriority(priority);
        }

        @Override
        public SessionMembers getSessionMembers() {
            return conceptActionInterface.getSessionMembers();
        }

        @Override
        public List<MessageManager> getPlaybackMessages() {
            return conceptActionInterface.getPlaybackMessages();
        }

		}

    }
