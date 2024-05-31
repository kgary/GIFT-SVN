/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.AssessmentUpdateEventType;
import mil.arl.gift.domain.knowledge.common.metric.assessment.DefaultPerformanceMetric;
import mil.arl.gift.domain.knowledge.common.metric.assessment.PerformanceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.competence.CompetenceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.competence.DefaultCompetenceMetric;
import mil.arl.gift.domain.knowledge.common.metric.confidence.ConfidenceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.confidence.DefaultConfidenceMetric;
import mil.arl.gift.domain.knowledge.common.metric.grade.DefaultGradeMetric;
import mil.arl.gift.domain.knowledge.common.metric.grade.GradeMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.priority.DefaultPriorityMetric;
import mil.arl.gift.domain.knowledge.common.metric.priority.PriorityMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.trend.DefaultTrendMetric;
import mil.arl.gift.domain.knowledge.common.metric.trend.TrendMetricInterface;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerRequestInterface;
import mil.arl.gift.net.api.message.Message;

/**
 * This is the base class for performance assessment node classes (i.e. Task,
 * Concept).
 *
 * @author mhoffman
 *
 */
public abstract class AbstractPerformanceAssessmentNode {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractPerformanceAssessmentNode.class);

    /** the node name */
    private String name;

    /** 
     * Whether this assessment node is actively being assessed 
     * Currently a task node is active if:
     * 1) the task has no start triggers
     * - OR -
     * 2) one of the task's start triggers was fired
     * 
     * - AND -
     * the task is not finished.
     * 
     * A concept node is active if the task ancestor is active. 
     */
    protected Boolean active = false;

    /** 
     * flag indicating the node has finished being assessed. 
     * Note: the assessment node can be finished and then not finished because it was re-activated
     */
    protected boolean finished = false;
    
    /**
     * flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view. 
     */
    private boolean scenarioSupportNode = false;

    /** scenario (i.e. DKF) unique id of this node */
    private int nodeId;
    
    /** course node unique id (generated, not user provided) */
    protected UUID courseNodeId;   

    /** list of assessment options beyond condition assessing */
    private List<AbstractLessonAssessment> assessments;

    /** the last survey assessment result for this node */
    protected AssessmentLevelEnum latestSurveyAssessment = AssessmentLevelEnum.UNKNOWN;
    
    /** listeners interested in being notified of survey responses that are assessed.  These surveys are presented as additional assessment for this node. */
    private List<SurveyResponseAssessmentListener> childSurveyResponseAssessmentListeners = new ArrayList<>();
    
    /** maintains the assessments for all performance nodes in a course instance for a single user */
    protected AssessmentProxy assessmentProxy = null;
    
    /**
     * used to determine performance metrics about this performance node
     */
    protected PerformanceMetricInterface performanceMetric;
    
    /**
     * used to determine confidence metrics about this performance node
     */
    protected ConfidenceMetricInterface confidenceMetric;
    
    /**
     * used to determine competence metrics about this performance node
     */
    protected CompetenceMetricInterface competenceMetric;
    
    /**
     * used to determine trend metrics about this performance node
     */
    protected TrendMetricInterface trendMetric;
    
    /**
     * used to determine priority metrics about this performance node
     */
    protected PriorityMetricInterface priorityMetric;
    
    /** used to determine the overall assessment grade for this node */
    protected GradeMetricInterface gradeMetric;
    
    /** the minimum confidence value need to affect this nodes performance assessment value */
    public static final double MIN_AFFECT_CONFIDENCE = 0.80;
    
    /** 
     * mapping of child concept or condition course unique id to assessment arguments (e.g. weights)
     * Can be null if the children don't have any arguments.
     */
    protected Map<UUID, PerformanceMetricArguments> childAssessmentWeights = null;

    /**
     * Class constructor - set attributes
     *
     * @param nodeId - unique node id for this performance assessment node
     * @param name - display name of this performance assessment node
     * @param assessments - additional assessments (e.g. survey assessment) for
     * this node
     */
    public AbstractPerformanceAssessmentNode(int nodeId, String name, List<AbstractLessonAssessment> assessments) {
        setNodeId(nodeId);
        setName(name);
        setAssessments(assessments);
    }
    
    /**
     * Set the algorithm used to calculate the performance assessment for this node.
     * 
     * @param performanceMetric the instance to use, can't be null.
     */
    public void setPerformanceMetric(PerformanceMetricInterface performanceMetric){
        
        if(performanceMetric == null){
            throw new IllegalArgumentException("The performance metric instance can't be null.");
        }
        
        this.performanceMetric = performanceMetric;
        performanceMetric.setMetricArgsForChildConceptOrCondition(getChildConceptOrConditionPerformanceMetricArgs());
    }
    
    /**
     * Calculate and possibly update the performance assessment metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @return whether the performance assessment value was changed for this node
     */
    protected boolean calculatePerformanceMetric(){
        
        if(performanceMetric == null){
            setPerformanceMetric(new DefaultPerformanceMetric());
            performanceMetric.setMetricArgsForChildConceptOrCondition(getChildConceptOrConditionPerformanceMetricArgs());
        }
        
        try{
            return performanceMetric.setPerformance(this, assessmentProxy, null);
        }catch(Throwable t){
            logger.error("The performance metric implementation class of "+performanceMetric+" causes an error when calculating the performance for the node '"+getName()+"'.", t);
            return false;
        }
    }
    
    /**
     * Return the mapping of child concept or condition unique course id to the authored performance metric arguments
     * for those objects.  Can be null, empty and have null values for an id.
     * @return the mapping of performance metric arguments.
     */
    protected abstract Map<UUID, PerformanceMetricArguments> getChildConceptOrConditionPerformanceMetricArgs();
    
    /**
     * Set the algorithm used to calculate the confidence for this node.
     * 
     * @param confidenceMetric the instance to use, can't be null.
     */
    public void setConfidenceMetric(ConfidenceMetricInterface confidenceMetric){
        
        if(confidenceMetric == null){
            throw new IllegalArgumentException("The confidence metric instance can't be null.");
        }
        
        this.confidenceMetric = confidenceMetric;
    }
    
    /**
     * Calculate and possibly update the confidence metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @return whether the confidence value was changed for this node
     */
    protected boolean calculateConfidenceMetric(){
        
        if(confidenceMetric == null){
            confidenceMetric = new DefaultConfidenceMetric();
        }
        
        try{
            return confidenceMetric.setConfidence(this, assessmentProxy, null);
        }catch(Throwable t){
            logger.error("The confidence metric implementation class of "+confidenceMetric+" causes an error when calculating the confidence for the node '"+getName()+"'.", t);
            return false;
        }
    }
    
    /**
     * Set the algorithm used to calculate the competence for this node.
     * 
     * @param competenceMetric the instance to use, can't be null.
     */
    public void setCompetenceMetric(CompetenceMetricInterface competenceMetric){
        
        if(competenceMetric == null){
            throw new IllegalArgumentException("The competence metric instance can't be null.");
        }
        
        this.competenceMetric = competenceMetric;
    }
    
    /**
     * Calculate and possibly update the competence metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @return whether the competence value was changed for this node
     */
    protected boolean calculateCompetenceMetric(){
        
        if(competenceMetric == null){
            competenceMetric = new DefaultCompetenceMetric();
        }
        
        try{
            return competenceMetric.setCompetence(this, assessmentProxy, null);
        }catch(Throwable t){
            logger.error("The competence metric implementation class of "+competenceMetric+" causes an error when calculating the competence for the node '"+getName()+"'.", t);
            return false;
        }
    }
    
    /**
     * Set the algorithm used to calculate the assessment trend for this node.
     * 
     * @param trendMetric the instance to use, can't be null.
     */
    public void setTrendMetric(TrendMetricInterface trendMetric){
        
        if(trendMetric == null){
            throw new IllegalArgumentException("The trend metric instance can't be null.");
        }
        
        this.trendMetric = trendMetric;
    }
    
    /**
     * Calculate and possibly update the assessment trend metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @return whether the trend value was changed for this node
     */
    protected boolean calculateTrendMetric(){
        
        if(trendMetric == null){
            trendMetric = new DefaultTrendMetric();
        }
        
        try{
            return trendMetric.setTrend(this, assessmentProxy, null);
        }catch(Throwable t){
            logger.error("The trend metric implementation class of "+trendMetric+" causes an error when calculating the trend for the node '"+getName()+"'.", t);
            return false;
        }
    }
    
    /**
     * Set the algorithm used to calculate the priority for this node.
     * 
     * @param priorityMetric the instance to use, can't be null.
     */
    public void setPriorityMetric(PriorityMetricInterface priorityMetric){
        
        if(priorityMetric == null){
            throw new IllegalArgumentException("The priority metric instance can't be null.");
        }
        
        this.priorityMetric = priorityMetric;
    }
    
    /**
     * Calculate and possibly update the priority metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @return whether the priority value was changed for this node
     */
    protected boolean calculatePriorityMetric(){
        
        if(priorityMetric == null){
            priorityMetric = new DefaultPriorityMetric();
        }
        
        try{
            return priorityMetric.setPriority(this, assessmentProxy, null);
        }catch(Throwable t){
            logger.error("The priority metric implementation class of "+priorityMetric+" causes an error when calculating the priority for the node '"+getName()+"'.", t);
            return false;
        }
    }
    
    /**
     * Set the algorithm used to calculate the grade (overall assessment) for this node.
     * 
     * @param gradeMetric the instance to use, can't be null.
     */
    public void setGradeMetric(GradeMetricInterface gradeMetric){
        
        if(gradeMetric == null){
            throw new IllegalArgumentException("The grade metric instance can't be null.");
        }
        
        this.gradeMetric = gradeMetric;
        gradeMetric.setMetricArgsForChildConceptOrCondition(getChildConceptOrConditionPerformanceMetricArgs());
    }
    
    /**
     * Calculate and possibly update the grade metric for this node using
     * the node's algorithm.  If an algorithm is not set the default will be used.
     * 
     * @return whether the grade value was changed for this node
     */
    protected void calculateGradeMetric(GradedScoreNode gradeNode){
        
        if(gradeMetric == null){
            setGradeMetric(new DefaultGradeMetric());
        }
        
        try{
            gradeMetric.updateGrade(this, gradeNode);
        }catch(Throwable t){
            logger.error("The grade metric implementation class of "+gradeMetric+" causes an error when calculating the grade for the node '"+getName()+"'.", t);
        }
    }
    
    private void setNodeId(int nodeId){
        
        if(nodeId < 0){
            throw new IllegalArgumentException("The node id must be a positive number.  Can't use provided value of "+nodeId);
        }
        
        this.nodeId = nodeId;
    }
    
    private void setName(String name){
        
        if(name == null){
            throw new IllegalArgumentException("The name can't be null");
        }
        
        this.name = name;
    }
    
    private void setAssessments(List<AbstractLessonAssessment> assessments){
        
        if(assessments == null){
            assessments = new ArrayList<>();
        }
        
        this.assessments = assessments;
    }

    /**
     * Return the name of the node
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Return the scenario (i.e. DKF) unique node id
     *
     * @return int
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Return the collection of assessments used to further assess this
     * performance node beyond metric assessing.
     *
     * @return List<AbstractLessonAssessment>
     */
    public List<AbstractLessonAssessment> getAssessments() {
        return assessments;
    }    

    /**
     * Return whether this assessment node is actively being assessed</br> 
     * </br>
     * Currently a task node is active if:</br>
     * 1) the task has no start triggers</br>
     * - OR -</br>
     * 2) one of the task's start triggers was fired</br>
     * - AND -</br>
     * the task is NOT finished.</br>
     * </br>
     * A concept node is active if the task ancestor is active. 
     *
     * @return boolean
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Return whether or not the node has finished being assessed. 
     * Note: the assessment node can be finished and then not finished because it was re-activated
     *
     * @return boolean
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Add a new listener that is interested in receiving notification of when a survey response is received
     * and assessed.  The survey was presented as part of additional assessment for this node.
     * 
     * @param listener the new listener to add to the collection of listeners
     */
    public void addChildSurveyResponseAssessmentListener(SurveyResponseAssessmentListener listener){
        
        if(!childSurveyResponseAssessmentListeners.contains(listener)){
            childSurveyResponseAssessmentListeners.add(listener);
        }
    }
    
    /**
     * Remove the listener from the collection of listeners.  The listener will no longer receive notifications
     * of survey responses that are assessed for this node.
     * 
     * @param listener the listener to remove from the collection of listeners
     */
    public void removeChildSurveyResponseAssessmentListener(SurveyResponseAssessmentListener listener){
        childSurveyResponseAssessmentListeners.remove(listener);
    }

    /**
     * Service the performance assessment request
     *
     * @param strategyHandlerRequestInterface The strategy handler to handle the
     * request
     */
    public void handlePerformanceAssessmentRequest(StrategyHandlerRequestInterface strategyHandlerRequestInterface) {

        for (AbstractLessonAssessment assessment : assessments) {

            //TODO: for now just choose first one, then bail out
            if (assessment instanceof GIFTSurveyLessonAssessment) {
                
                if(logger.isInfoEnabled()){
                    logger.info("Handling Performance Assessment request of type survey lesson assessment - "+assessment);
                }

                //give survey
                strategyHandlerRequestInterface.displayDuringLessonSurvey((AbstractSurveyLessonAssessment) assessment, new SurveyResultHandler((AbstractSurveyLessonAssessment) assessment));
                break;

            }else if(assessment instanceof ConditionLessonAssessment){
                
                if(logger.isInfoEnabled()){
                    logger.info("Handling Performance Assessment request of type Condition Lesson Assessment - "+assessment);
                }
                
                //notify all conditions of this performance assessment node
                if(this instanceof ConditionLessonAssessmentHandlerInterface){
                    ((ConditionLessonAssessmentHandlerInterface)this).assessConditions();
                    break;
                }
                
            }else{

                //ERROR
                logger.error("Found unhandled assessment option of " + assessment + " for " + this);
            }
        }
    }

    /**
     * Assess the survey results using the assessment logic of this node
     *
     * @param surveyResponse - survey results from a learner
     */
    public void handleSurveyResults(SurveyResponse surveyResponse) {
        surveyAssessmentCompleted(surveyResponse, assessments.toArray(new AbstractLessonAssessment[assessments.size()]));
    }
    
    /**
     * Handle the assessments of a conversation between the learner and GIFT.
     * This will update this node's performance assessment value if the concept being assessed
     * matches this node's concept and the assessment confidence is high enough to warrant an update.
     * 
     * @param assessments contains concept assessment information for choices the learner has made in the conversation.
     * @return true iff the conversation assessment updated the concept's assessment value
     */
    public abstract boolean handleConversationAssessment(List<ConversationAssessment> assessments);
    
    /**
     * Handle the assessments of a conversation between the learner and GIFT.
     * This will update this node's performance assessment value if the concept being assessed
     * matches this node's concept and the assessment confidence is high enough to warrant an update.
     * 
     * @param perfNodeCurrentAssessment the current performance assessment of this node implementation
     * @param assessments contains concept assessment information for choices the learner has made in the conversation.
     * @return true iff the conversation assessment updated the concept's assessment value
     */
    protected boolean handleConversationAssessment(AbstractAssessment perfNodeCurrentAssessment, List<ConversationAssessment> assessments){
        
        for(ConversationAssessment assessment : assessments){
            
            if(assessment.getConcept().equals(getName())){
                //found matching node name, update performance assessment
                
                if(assessment.getConfidence() >= MIN_AFFECT_CONFIDENCE){
                    AbstractAssessment currentNodeAssessment = assessmentProxy.get(courseNodeId);
                    currentNodeAssessment.updateAssessment(assessment.getAssessmentLevel());
                    currentNodeAssessment.updateConfidence(assessment.getConfidence());
                    fireAssessmentUpdate(false, AssessmentUpdateEventType.CONCEPT_ASYNC_UPDATED);
                    return true;
                }                
                
                break;
            }
        }
        
        return false;
    }

    /**
     * A survey response has been give for a survey that is suppose to be assessing this
     * performance node (task/concept).  Calculate the resulting assessment based on the responses
     * given and apply it to this node.
     *
     * @param surveyResponse contains the responses to a survey from a learner.  If null then this method
     * does nothing.
     * @param lessonAssessments one or more assessment rules to use on the survey response in order to determine
     * the assessment to apply to this node.  If empty then this method does nothing.
     */
    private void surveyAssessmentCompleted(SurveyResponse surveyResponse, AbstractLessonAssessment ... lessonAssessments) {
        
        if(surveyResponse == null){
            return;
        }else if(lessonAssessments.length == 0){
            return;
        }
        
        // Calculate the assessment level
        // 
        AssessmentLevelEnum newAssessment = AssessmentLevelEnum.UNKNOWN;
        for (AbstractLessonAssessment assessment : lessonAssessments) {

            if (assessment instanceof AbstractSurveyLessonAssessment) {

                newAssessment = ((AbstractSurveyLessonAssessment) assessment).getAssessment(surveyResponse);

                //TODO: for now don't calculate an aggregate score if there are more than one assessment scores here
                break;

            } else {

                //ERROR
                logger.error("Found unhandled assessment option of " + assessment + " for " + this);
            }
        }

        if(logger.isDebugEnabled()){
            logger.debug("Survey assessment resulted in " + newAssessment + " for " + this + " based on survey responses to survey named "+surveyResponse.getSurveyName());
        }
        latestSurveyAssessment = newAssessment;

        //don't listen to the assessment if it resulted with unknown
        if (newAssessment != null && newAssessment != AssessmentLevelEnum.UNKNOWN) {

            AbstractAssessment currentNodeAssessment = assessmentProxy.get(courseNodeId);
            currentNodeAssessment.updateAssessment(newAssessment);
            
            StringBuilder sb = new StringBuilder();
            sb.append("The '").append(surveyResponse.getSurveyName()).append("' survey resulted in a ").append(newAssessment.getDisplayName()).append(" assessment.");
            addAssessmentExplanation(sb.toString());
            fireAssessmentUpdate(true, AssessmentUpdateEventType.SURVEY_ASSESSMENT);
        }
    }
    
    /**
     * Return the current concept assessment for this node
     * 
     * @return the current assessment of this node.  Will be null if {@link #cleanup()} was called.
     */
    public abstract AbstractAssessment getAssessment();
    
    /**
     * Build an updated performance assessment using the metrics provided within the request.
     *
     * @param request the request containing the updated metrics for a specific task or concept.
     */
    public abstract void evaluatorUpdateRequestReceived(EvaluatorUpdateRequest request);
    
    /**
     * Updates the provided assessment metrics with the values in the request.
     *
     * @param request the request containing the new metric values.
     */
    protected void updatePerformanceAssessmentMetrics(EvaluatorUpdateRequest request) {
        
        AbstractAssessment assessment = getAssessment();
        if(assessment == null){
            return;
        }

        final Map<String, AssessmentLevelEnum> teamOrgEntities = request.getTeamOrgEntities();
        boolean hasTeamMembers = CollectionUtils.isNotEmpty(teamOrgEntities);

        /* If the request has team org entities, then add them to the
         * assessment */
        if (hasTeamMembers) {
            assessment.addAssessedTeamOrgEntries(teamOrgEntities);
        }

        // update performance metric
        final AssessmentLevelEnum newMetric = request.getPerformanceMetric();
        if (newMetric != null) {
            assessment.updateAssessment(newMetric, true);

            /* If the request has no team org entities specified, then apply the
             * assessment update to all previously known entities */
            if (!hasTeamMembers) {
                final Map<String, AssessmentLevelEnum> assessedTeamOrgEntities = assessment
                        .getAssessedTeamOrgEntities();
                for (String entity : assessedTeamOrgEntities.keySet()) {
                    assessedTeamOrgEntities.put(entity, newMetric);
                }
            }
        }

        assessment.setObserverComment(request.getReason());
        assessment.setObserverMedia(request.getMediaFile());
        assessment.setObservationStartedTime(request.getTimestamp());

        // update competence metric
        if (request.getCompetenceMetric() != null) {
            assessment.updateCompetence(request.getCompetenceMetric(), true);
        }

        // update confidence metric
        if (request.getConfidenceMetric() != null) {
            assessment.updateConfidence(request.getConfidenceMetric(), true);
        }

        // update priority metric
        if (request.getPriorityMetric() != null) {
            assessment.updatePriority(request.getPriorityMetric(), true);
        }

        // update trend metric
        if (request.getTrendMetric() != null) {
            assessment.updateTrend(request.getTrendMetric(), true);
        }

        // update the user that updated the metrics
        if (request.getEvaluator() != null) {
            assessment.setEvaluator(request.getEvaluator());
        }

        /* Update the hold states */
        if(request.isAssessmentHold() != null) {
            assessment.setAssessmentHold(request.isAssessmentHold());
        }

        if(request.isPriorityHold() != null) {
            assessment.setPriorityHold(request.isPriorityHold());
        }

        if(request.isConfidenceHold() != null) {
            assessment.setConfidenceHold(request.isConfidenceHold());
        }

        if(request.isCompetenceHold() != null) {
            assessment.setCompetenceHold(request.isCompetenceHold());
        }

        if(request.isTrendHold() != null) {
            assessment.setTrendHold(request.isTrendHold());
        }
        
        /* Set assessment explanation value */
        if (StringUtils.isNotBlank(request.getReason())) {
            /* Use the optional bookmark value provided by the observer */
            assessment.addAssessmentExplanation(request.getReason());
        } else if (hasTeamMembers) {
            /* Create assessment explanation based on team org members
             * selected */
            StringBuilder sb = new StringBuilder("[");
            StringUtils.join(", ", teamOrgEntities.keySet(), sb);
            sb.append("] ").append(teamOrgEntities.size() == 1 ? "has" : "have").append(" been assessed.");
            assessment.addAssessmentExplanation(sb.toString());
        }
    }
    
    /**
     * Add the assessment explanation to the set of explanations for the Task/Concept 
     * 
     * @param assessmentExplanation shouldn't be null or empty.
     */
    protected abstract void addAssessmentExplanation(String assessmentExplanation);
    
    /**
     * Notify the assessment proxy that this node's assessment value(s) have been updated
     * 
     * @param notifyParentNode whether this method call should notify the parent performance node of
     * it's change in assessment level.  The case for false is when the caller to this method will
     * handle notifying the parent performance node.
     * @param eventType the enumerated type of event that caused this assessment update, can't be null.
     */
    protected abstract void fireAssessmentUpdate(boolean notifyParentNode, AssessmentUpdateEventType eventType);

    /**
     * Process the training application game state message received.  The message usually comes
     * from the Tutor or the Gateway modules.
     *
     * @param message - the training application game state message to handle
     * @return AbstractAssessment - an assessment created as a result of the game state message</br>
     * Notes:</br>
     *        i. Return null to indicate that the assessment value hasn't changed
     *           from the last reported value.  This is due to the child performance assessment 
     *           node(s) (i.e. Concepts) or Condition(s) not reporting any changes in their assessment value(s).</br>
     *       ii. Return Above/At/Below as a result of child performance assessment node(s) (i.e. Concepts)
     *           or Condition(s) analyzing the message and updating their assessment value(s).  It is acceptable
     *           to return the same value back to back as an indication that the first value is independent
     *           of the second value. For example, every time a user presses 'button A' a Below assessment value
     *           is returned for a Condition.  Therefore the first and second time that 'button A' is pressed
     *           are different events but with the same back-to-back reported assessment value. 
     */
    public abstract AbstractAssessment handleTrainingAppGameState(Message message);

    /**
     * Calculate the current assessment for this node
     *
     * @param eventType - the enumerated type of event causing this assessment update to be called.  Can't be null.
     * @return boolean - whether or not the concept assessment has been updated
     */
    protected abstract boolean updateAssessment(AssessmentUpdateEventType eventType);

    /**
     * Whether or not this node contains scoring rules that can provide an overall
     * assessment of this node once the lesson is finished (not necessarily completed).
     * 
     * @return boolean
     */
    public abstract boolean hasScoringRules();
    
    /**
     * The situation that needed this performance node to be assessed has ended.
     * Time to cleanup.
     */
    protected abstract void assessmentEnded();

    /**
     * Return the flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view.
     * @return default is false
     */
    public boolean isScenarioSupportNode() {
        return scenarioSupportNode;
    }

    /**
     * Set the flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view.<br/>
     * Note: protected because the Task/Concept constructors create the performance assessment objects which
     * need this value as well and there is currently no other logic updating those objects with this value.
     * @param scenarioSupportNode true if this task/concept should be hidden from the OC
     */
    protected void setScenarioSupportNode(boolean scenarioSupportNode) {
        this.scenarioSupportNode = scenarioSupportNode;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(" id = ").append(getNodeId());
        sb.append(", Name = ").append(getName());
        sb.append(", active = ").append(active);
        sb.append(", finished = ").append(finished);
        sb.append(", scenarioSupport = ").append(isScenarioSupportNode());
        sb.append(", performanceMetric = ").append(performanceMetric);
        sb.append(", confidenceMetric = ").append(confidenceMetric);
        sb.append(", competenceMetric = ").append(competenceMetric);
        sb.append(", trendMetric = ").append(trendMetric);
        sb.append(", priorityMetric = ").append(priorityMetric);
        sb.append("]");

        return sb.toString();
    }

    /**
     * Used to handle the results of a survey lesson assessment for which this
     * node was responsible for giving to the learner.
     *
     * @author mhoffman
     *
     */
    protected class SurveyResultHandler implements SurveyResultListener {

        /** the domain assessment knowledge for a survey */
        private AbstractSurveyLessonAssessment assessment;

        /**
         * Class constructor - set attribute
         *
         * @param assessment the domain assessment knowledge for a survey 
         */
        public SurveyResultHandler(AbstractSurveyLessonAssessment assessment) {
            this.assessment = assessment;
        }

        @Override
        public void surveyCompleted(SurveyResponse surveyResponse) {
            surveyAssessmentCompleted(surveyResponse, assessment);
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("[SurveyResultHandler: ");
            sb.append("assessment = ").append(assessment);
            sb.append("]");

            return sb.toString();
        }
    }
}
