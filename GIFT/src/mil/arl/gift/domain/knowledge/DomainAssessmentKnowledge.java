/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.AssessmentProxyManager;
import mil.arl.gift.domain.knowledge.common.DomainKnowledgeInterface;
import mil.arl.gift.domain.knowledge.common.ProxyPerformanceAssessment;
import mil.arl.gift.domain.knowledge.common.ScenarioActionInterface;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.strategy.StrategyAppliedEvent;
import mil.arl.gift.net.api.message.Message;

//TODO: its possible this class could be removed and domain knowledge manager would have scenario

/**
 * This class contains the information contained within a domain knowledge file.
 * 
 * @author mhoffman
 *
 */
public class DomainAssessmentKnowledge {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DomainAssessmentKnowledge.class);

    /** the domain scenario */
    private Scenario scenario;
        
    private AssessmentProxy assessmentProxy;
    
    /** handler for scenario actions */
    private ScenarioActionImpl scenarioActionInterface = null;
        
    /**
     * Class constructor
     * 
     * @param scenario - the scenario information for this domain.  Can't be null.
     */
    public DomainAssessmentKnowledge(Scenario scenario){
        
        if(scenario == null){
            throw new IllegalArgumentException("The scenario can't be null.");
        }
        this.scenario = scenario;
    }
    
    /**
     * Register all of the performance assessments for the performance assessment nodes in the scenario
     * object with the assessment proxy specified.  This is needed to allow the various nodes to update
     * the appropriate proxy with the latest assessment values.
     * 
     * @param assessmentProxy manages the assessment values for this domain knowledge
     * @throws Exception if there was a severe problem registering the nodes of this scenario.
     */
    public void registerNodes(AssessmentProxy assessmentProxy) throws Exception{
        
        this.assessmentProxy = assessmentProxy;
        
        AssessmentProxyManager proxyMgr = AssessmentProxyManager.getInstance();
        for(AbstractPerformanceAssessmentNode node : scenario.getPerformanceNodes().values()){
            proxyMgr.registerNode(node, assessmentProxy);
        }
    }
    
    /**
     * Load the domain scenario.  Should be called before {@link #start()}.
     * 
     * @param domainKnowledgeInterface used to communicate assessment events
     * @return boolean - whether or not the assessment engine(s) successfully initialized
     */
    public boolean load(final DomainKnowledgeInterface domainKnowledgeInterface){
        
        if(domainKnowledgeInterface == null){
            throw new IllegalArgumentException("the domain knowledge interface is null");
        } else if(scenarioActionInterface != null){
            throw new RuntimeException("The assessment knowledge has already been loaded.");
        }
       
        //handler for scenario actions
        scenarioActionInterface = new ScenarioActionImpl(domainKnowledgeInterface, assessmentProxy);
        
        if(scenario.initialize(scenarioActionInterface)){
            if(logger.isInfoEnabled()){
                logger.info("Scenario has been initialized"); 
            }
        }else{
            //raise an error higher up because the domain session scenario is now corrupt/incomplete
            logger.error("Failed to initialize scenario");
            domainKnowledgeInterface.fatalError("Failed to initialize the domain knowledge", "There was a problem initializing the DKF");
            return false;
        }
        
        return true;
    }
    
    /**
     * Terminate the domain scenario
     * @param status information about why the domain assessment knowlege session is ending.
     */
    public void terminate(LessonCompletedStatusType status){
        
        if(logger.isInfoEnabled()){
            logger.info("The domain assessment knowledge has been ordered to terminate");
        }
        
        if(scenario != null){
            scenario.terminate(status);
        }
    }
    
    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes. 
     */
    public void cleanup(){
        
        if(scenarioActionInterface != null){
            scenarioActionInterface.cleanup();
            scenarioActionInterface = null;
        }
        
        if(scenario != null){
            scenario.cleanup();
            scenario = null;
        }
    }
    
    /**
     * Notification that the strategy with the name was just applied.  Can be used to notify
     * domain knowledge task triggers.
     * 
     * @param event contains information about the strategy that was applied.  Can't be null.
     */
    public void appliedStrategyNotification(StrategyAppliedEvent event){
     
        if(scenario == null){
            throw new NullPointerException("The scenario object is null so the scenario can't be notified that a strategy was applied.");
        }
        
        scenario.appliedStrategyNotification(event);
    }
    
    /**
     * Request that the scenario be assessed.  This can be useful for causing the performance node hierarchy
     * to produce some initial assessments.
     */
    public void assessScenario(){
        
        if(scenario == null){
            throw new NullPointerException("The scenario object is null so the scenario can't be assessed.");
        }
        scenario.assessTasks();
    }
    
    /**
     * Start the scenario before receiving a single training application message.  Should be called
     * after {@link #load(DomainKnowledgeInterface)}.
     */
    public void start(){
        if(logger.isInfoEnabled()){
            logger.info("Starting domain assessment knowledge.");
        }
        
        if(scenario == null){
            throw new NullPointerException("The scenario object is null so the scenario can't be started.");
        }
        scenario.start();
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
    public PerformanceAssessment trainingAppGameStateMessageReceived(Message message){
        
        if(scenario == null){
            throw new NullPointerException("The scenario object is null so the scenario can't handle the training app game state message:\n"+message);
        }
        
        ProxyPerformanceAssessment assessment = scenario.trainingAppGameStateMessageReceived(message);
        if(assessment != null){
            return assessmentProxy.generatePerformanceAssessment(assessment);
        }else{
            return null;
        }
    }

    /**
     * Build an updated performance assessment using the metrics provided within the request.
     * 
     * @param request the request containing the updated metrics for a specific task or concept.
     */
    public void evaluatorUpdateRequestReceived(EvaluatorUpdateRequest request) {
        
        if(scenario == null){
            throw new NullPointerException("The scenario object is null so the scenario can't handle evaluator update requests:\n"+request);
        }
        scenario.evaluatorUpdateRequestReceived(request);
    }

    /**
     * Handle the assessments of a conversation between the learner and GIFT.
     * This will update performance node's assessment value if the concept being assessed
     * matches the node's concept and the assessment confidence is high enough to warrant an update.
     * 
     * @param assessments contains concept assessment information for choices the learner has made in the conversation.
     */
    public void handleConversationAssessment(List<ConversationAssessment> assessments){
        
        if(scenario == null){
            throw new NullPointerException("The scenario object is null so the scenario can't handle conversation assessments.");
        }
        scenario.handleConversationAssessment(assessments);
    }
    
    /**
     * Return the domain scenario instance
     * 
     * @return Scenario contains the real time assessment information for a particular training app course object.
     * Can be null if the cleanup method was called.
     */
    public Scenario getScenario(){
        return scenario;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(this.getClass().getSimpleName()).append(": ");
        sb.append(" Scenario = ").append(getScenario());
        
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * Handles notifications of domain knowledge events such as performance assessments and scenario completion.
     * 
     * @author mhoffman
     *
     */
    private static class ScenarioActionImpl implements ScenarioActionInterface{
        
        /** used to communicate events caused in the domain knowledge to BaseDomainSession */
        private DomainKnowledgeInterface domainKnowledgeInterface;
        
        /** contains assessments for the tasks/concepts in the scenario, used for performance assessment messages */
        private AssessmentProxy assessmentProxy;
        
        /**
         * Set attributes
         * 
         * @param domainKnowledgeInterface used to communicate events caused in the domain knowledge to BaseDomainSession.  Can't be null.
         * @param assessmentProxy contains assessments for the tasks/concepts in the scenario, used for performance assessment messages.  Can't be null.
         */
        public ScenarioActionImpl(DomainKnowledgeInterface domainKnowledgeInterface, AssessmentProxy assessmentProxy){
            
            if(domainKnowledgeInterface == null){
                throw new IllegalArgumentException("The domain knowledge interface can't be null");
            }else if(assessmentProxy == null){
                throw new IllegalArgumentException("The assessment proxy can't be null");
            }
            this.domainKnowledgeInterface = domainKnowledgeInterface;
            this.assessmentProxy = assessmentProxy;
        }
        
        /**
         * This instance will no longer be used.  Release references to objects that were created
         * outside of this class, used by inner classes and are inner classes. 
         */
        public void cleanup(){
            domainKnowledgeInterface = null;
        }
        
        @Override
        public void scenarioStarted() {
            domainKnowledgeInterface.domainStarted();
        }
        
        @Override
        public void scenarioEnded(LessonCompletedStatusType status) {
            domainKnowledgeInterface.domainEnded(status);  
        }
        
        @Override
        public void performanceAssessmentUpdated(
                ProxyPerformanceAssessment performanceAssessment) {
            
            PerformanceAssessment newPerformanceAssessment = assessmentProxy.generatePerformanceAssessment(performanceAssessment);
            domainKnowledgeInterface.performanceAssessmentCreated(newPerformanceAssessment);
        }

        @Override
        public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
            domainKnowledgeInterface.trainingApplicationRequest(infoRequest);                
        }

        @Override
        public void handleDomainActionWithLearner(DomainAssessmentContent action) {
            domainKnowledgeInterface.handleDomainActionWithLearner(action);
        }

        @Override
        public void fatalError(String reason, String details) {
            domainKnowledgeInterface.fatalError(reason, details);                
        }

        @Override
        public void displayDuringLessonSurvey(
                AbstractSurveyLessonAssessment surveyAssessment,
                SurveyResultListener surveyResultListener) {
            domainKnowledgeInterface.displayDuringLessonSurvey(surveyAssessment, surveyResultListener);                
        }

        @Override
        public SessionMembers getSessionMembers() {
            return domainKnowledgeInterface.getSessionMembers();
        }

        @Override
        public List<MessageManager> getPlaybackMessages() {
            return domainKnowledgeInterface.getPlaybackMessages();
        }
    }
}
