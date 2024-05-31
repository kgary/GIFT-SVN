/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.math.BigInteger;
import java.util.ArrayList;
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
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.domain.DomainKnowledgeManager;
import mil.arl.gift.domain.knowledge.common.AbstractLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AssessmentProxyManager;
import mil.arl.gift.domain.knowledge.common.ConceptActionInterface;
import mil.arl.gift.domain.knowledge.common.ProxyIntermediateConceptAssessment;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.AssessmentUpdateEventType;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.net.api.message.Message;

/**
 * A intermediate concept is a concept that allows for infinite nesting of concepts, therefore allowing the performance node
 * tree to be as complex as the DKF author wishes.  A intermediate concept has no metrics like a regular concept.
 * 
 * @author mhoffman
 *
 */
public class IntermediateConcept extends Concept {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(IntermediateConcept.class);
    
    /** list of concepts associated (i.e. children) of this intermediate concept */
    private List<Concept> concepts;

    /** if not null, then it is the current concept's condition that is handling a simulation message */
    private Concept currentConceptWithSimMsg = null;

    /** flag used to indicate if the concept should be check for completion */
    private boolean needCompletionCheck = false;
    
    /** flag used to indicate if this concept has a descendant concept that is a course concept.  Null if this check has not been performed. */
    private Boolean containsDescendantCourseConcepts = null;

    /**
     * Class constructor - set attributes
     * 
     * @param nodeId - the unique node id of this concept
     * @param name - the display name of this concept
     * @param concepts - the sub-concepts associated with this concept
     * @param assessments - additional assessments (e.g. survey assessment) for this concept
     * @param initialPriority - the initial priority for the concept (optional, can be null)
     * @param scenarioSupport flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view.
     */
    public IntermediateConcept(int nodeId, String name, List<Concept> concepts, List<AbstractLessonAssessment> assessments, 
            BigInteger initialPriority, boolean scenarioSupport) {
        super(nodeId, name, new ArrayList<AbstractCondition>(0), assessments, initialPriority, scenarioSupport);
       
        setConcepts(concepts);
        
        List<UUID> subconcepts = new ArrayList<>(concepts.size());
        for(Concept concept : concepts){
            subconcepts.add(concept.getAssessment().getCourseNodeId());
        }
                
        currentAssessment = new ProxyIntermediateConceptAssessment(name, AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), nodeId, subconcepts, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
        courseNodeId = currentAssessment.getCourseNodeId();
        
        //force an analysis of this concept's conditions default assessments
        updateAssessment(AssessmentUpdateEventType.CONCEPT_CREATED);
    }
    
    /**
     * Return the list of concepts associated (i.e. children) with this intermediate concept.
     * 
     * @return List<Concept> - the collection of subconcepts
     */
    public List<Concept> getConcepts(){
        return concepts;
    }
    
    /**
     * Return whether this concept or any descendant concepts has the id provided.
     * @param conceptDomainKnowledgeId the DKF performance node id to check for in this concept
     * @return true if a match is found.
     */
    @Override
    public boolean containsConceptDomainKnowledgeId(int conceptDomainKnowledgeId) {        
        
        boolean found = false;
        if(conceptDomainKnowledgeId == this.getNodeId()) {
            found = true;
        }else {
            for(Concept subconcept : concepts) {
                if(subconcept.containsConceptDomainKnowledgeId(conceptDomainKnowledgeId)) {
                    found = true;
                    break;
                }
            }
        }
        
        return found;
    }
    
    @Override
    public GradedScoreNode getScore(boolean courseConceptDescendant){        
        return super.getScore(courseConceptDescendant || isCourseConcept() || hasDescendantCourseConcept());
    }
    
    /**
     * Return whether this intermediate DKF concept has a descendant DKF concept that is a course concept.
     * @return true if one descendant course concept was found.
     */
    public boolean hasDescendantCourseConcept(){
        
        if(containsDescendantCourseConcepts == null){
            // this check has not been performed yet
            
            containsDescendantCourseConcepts = Boolean.FALSE;
            
            for(Concept concept : concepts){
                
                if(concept.isCourseConcept()){
                    containsDescendantCourseConcepts = Boolean.TRUE;
                    break;
                }
                
                if(concept instanceof IntermediateConcept){
                    containsDescendantCourseConcepts = ((IntermediateConcept)concept).hasDescendantCourseConcept();
                    if(containsDescendantCourseConcepts){
                        break;
                    }
                }
            }

        }
        
        return containsDescendantCourseConcepts;
    }
    
    /**
     * Set the list of concepts associated (i.e. children) with this intermediate concept.
     * 
     * @param concepts
     */
    private void setConcepts(List<Concept> concepts){
        
        if(concepts == null || concepts.isEmpty()){
            throw new IllegalArgumentException("The list of concepts for a intermediate concept must contain at least one concept");
        }
        
        this.concepts = concepts;
    }
    
    @Override
    public void initialize(Set<MessageTypeEnum> simInterests, ConceptActionInterface conceptActionInterface){
        
        if(logger.isInfoEnabled()){
            logger.info("Initializing Concept named "+getName());
        }
        
        this.conceptActionInterface = conceptActionInterface;
        
        ConceptActionInterface intermediateConceptActionInterface = new ConceptActionHandler();
        
        for(Concept concept : concepts){
            concept.initialize(simInterests, intermediateConceptActionInterface);
        }
        
        assessmentProxy = AssessmentProxyManager.getInstance().getAssessmentProxy(this);
        fireAssessmentUpdate(false, AssessmentUpdateEventType.CONCEPT_INITIALIZED);
        
        active = true;
    }
    
    @Override
    public void start(){

        super.start();
        
        for(Concept concept : concepts){
            concept.start();
        }
    }
    
    @Override
    public void stop(){
        
        for(Concept concept : concepts){
            concept.stop();
        }        
        
        // called after notifying concepts because the Concept.stop() sets the status 
        // attributes after notifying the conditions
        super.stop();

    }
    
    @Override
    public void cleanup(){
        
        AssessmentProxyManager.getInstance().unregisterNode(this);
        
        // synchronize to make sure the concept metrics aren't being updated right now
        // since that logic sets the calculated values in this object
        synchronized (currentAssessment) {
            currentAssessment = null;
        }
        
        for(Concept concept : concepts){
            concept.cleanup();
        }
        
        concepts.clear();
    }
    
    @Override
    public AbstractAssessment handleTrainingAppGameState(Message message){
               
        //provide the training app game state message to the various concepts
        boolean assessmentChanged = false;
        for(Concept concept : concepts){
            currentConceptWithSimMsg = concept;
            AbstractAssessment assessment = concept.handleTrainingAppGameState(message);
            if(assessment != null){
                assessmentChanged = true;
                if(conceptActionInterface != null){
                    //notify the parent to this intermediate concept that a grand child concept has changed
                    conceptActionInterface.conceptAssessmentCreated(concept, (ConceptAssessment) assessment);
                }
            }
        }
        
        // reset
        currentConceptWithSimMsg = null;

        //if this concept assessment was updated, return the assessment
        boolean returnAssessment = false;
        if(assessmentChanged && updateAssessment(AssessmentUpdateEventType.CONCEPT_SYNC_UPDATED)){
            returnAssessment = true;
        }
        
        if (needCompletionCheck) {
            // reset
            needCompletionCheck = false;

            if (isCompleted()) {
                finished = true;
                if (currentAssessment != null) {
                    currentAssessment.setNodeStateEnum(PerformanceNodeStateEnum.FINISHED);
                }

                if (conceptActionInterface != null) {
                    conceptActionInterface.conceptEnded(this);
                }
            }
        }

        // if this concept assessment was updated, return the assessment
        if (returnAssessment) {
            return currentAssessment;
        } else {
        return null;
    }
    }
    
    @Override
    protected synchronized boolean updateAssessment(AssessmentUpdateEventType eventType){
        
        // currentAssessment could be null when called on another thread which would set the assessment value to null
        // concepts can be null if this method is called by the constructor of Concept.java
        if (currentAssessment == null || concepts == null) {
            return false;
        }

        // synchronize and then check for null to handle a race condition where cleanup method
        // could be called on another thread which would set the assessment value to null
        synchronized(currentAssessment){
            if (currentAssessment == null) {
                return false;
            }

            /* If the metrics are being updated by the system, then the
             * evaluator is null */
            currentAssessment.setEvaluator(null);

            boolean changed = false;
            changed |= calculatePerformanceMetric();
            changed |= calculateConfidenceMetric();
            changed |= calculateCompetenceMetric();
            changed |= calculateTrendMetric();
            changed |= calculatePriorityMetric();
            
            if(changed){
                fireAssessmentUpdate(false, eventType);        
                return true;
            }
        }
        
        return false;
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
        
        //check this intermediation concept's concepts
        for(Concept concept : concepts){
            
            hasScorers |= concept.hasScoringRules();
            
            if(hasScorers){
                break;
            }
        }
        
        return hasScorers;
    }
    
    /**
     * Populates the foundCourseConcepts set with the set of course concepts
     * that are under this intermediate concept level and matches the filter
     * criteria provided.
     * 
     * @param searchFilter the filter criteria to apply to the concepts under
     *        this intermediate concept level.
     * @param foundCourseConcepts the set of course concepts found to be a match
     *        by string comparison from the course concepts set.
     * @param hasAncestorCourseConcepts whether this intermediate concept has an ancestor intermediate concept that is a course concept
     */
    public void getCourseConcepts(CourseConceptSearchFilter searchFilter, Set<String> foundCourseConcepts, boolean hasAncestorCourseConcepts) {
        if (searchFilter == null) {
            throw new IllegalArgumentException("The parameter 'searchFilter' cannot be null.");
        } else if (CollectionUtils.isEmpty(searchFilter.getCourseConceptNames())) {
            return;
        } else if (foundCourseConcepts == null) {
            throw new IllegalArgumentException("The found course concepts set is null.");
        }

        for (Concept concept : concepts) {
            if (searchFilter.applyFilter(concept)) {
                // the 'concept', a sub-concept to this intermediate concept instance, in this iteration is a course concept
                concept.setCourseConcept(true);
                foundCourseConcepts.add(concept.getName().toLowerCase());
            }
            
            // the child concept has a course concept ancestor if this intermediate concept is a course concept OR
            // an ancestor to this intermediate concept, another intermediate concept, is a course concept
            concept.setHasAncestorCourseConcept(hasAncestorCourseConcepts || concept.isCourseConcept());

            if (concept instanceof IntermediateConcept) {
                ((IntermediateConcept) concept).getCourseConcepts(searchFilter, foundCourseConcepts, hasAncestorCourseConcepts || concept.isCourseConcept());
            }
        }
    }
    
    /**
     * Handle when a child concept to this concept has changed.  This method will:<br/>
     * 1. attempt to update this Intermediate concept's state information<br/>
     * 2. notify the parent to this Intermediate concept (a task or another intermediate concept object) of 
     *  the concept provided <br/>
     * 3. if this Intermediate concept state was updated as a result, notify the parent to this Intermediate 
     * concept (a task or another intermediate concept object)<br/>
     * 
     * @param childConcept the child concept to this concept that has changed in some way (e.g. new assessment event)
     * @param childConceptAssessment contains the assessment information about the child concept
     */
    private void childConceptAssessmentUpdate(Concept childConcept, ConceptAssessment childConceptAssessment){
        
        // update this parent concept based on the update to the child concept
        boolean intermediateConceptUpdated = updateAssessment(AssessmentUpdateEventType.CONCEPT_ASYNC_UPDATED);
        
        if(conceptActionInterface != null){
            // FIRST: notify the parent to this parent of the child concept that changed
            conceptActionInterface.conceptAssessmentCreated(childConcept, childConceptAssessment);
            
            if(intermediateConceptUpdated){
                // SECOND: notify the parent that this parent concept also changed
                // Note: not calling Concept.fireAssessmentUpdate(true) here because updateAssessment() above just called
                //       it with false.
                conceptActionInterface.conceptAssessmentCreated(this, currentAssessment);
            }
        }
    }

    @Override
    public boolean isCompleted() {
        // check if other concepts have finished
        for (Concept child : getConcepts()) {
            if (!child.isFinished()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Handle the concept completing event by checking if this concept is now
     * completed. If so notify the parent.
     * 
     * @param concept - the concept that just completed
     */
    private synchronized void handleConceptCompleted(Concept concept) {

        if (currentConceptWithSimMsg == concept) {
            /* this is the current concept being given a simulation message and
             * that concept has completed. Delay the intermediate concept
             * isCompleted check until all concepts have had a chance to receive
             * the simulation message. */

            needCompletionCheck = true;

        } else if (isCompleted()) {
            /* the concept is completed on a different thread than the handle
             * simulation message method calls, no need to wait, notify the
             * parent concept that this concept has finished */

            if (logger.isDebugEnabled()) {
                logger.debug("All sub-concepts have finished for intermediate concept " + getName()
                        + ", therefore the intermediate concept is finished.");
            }
            finished = true;

            if (currentAssessment != null) {
                currentAssessment.setNodeStateEnum(PerformanceNodeStateEnum.FINISHED);
            }

            if (conceptActionInterface != null) {
                conceptActionInterface.conceptEnded(this);
            }
        }
    }
    
    @Override
    public void setVarsHandler(VariablesHandler varsHandler){
        
        for(Concept subConcept : concepts) {
            subConcept.setVarsHandler(varsHandler);
        }
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[IntermediateConcept: ");
        sb.append(super.toString());

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
        public void conceptStarted(Concept concept) {
            if(conceptActionInterface != null){
                conceptActionInterface.conceptStarted(concept);
            }
        }

        @Override
        public void conceptEnded(Concept concept) {
            handleConceptCompleted(concept);
        }

        @Override
        public void displayDuringLessonSurvey(AbstractSurveyLessonAssessment surveyAssessment,
                SurveyResultListener surveyResultListener) {
            
            if(conceptActionInterface != null){
                conceptActionInterface.displayDuringLessonSurvey(surveyAssessment, surveyResultListener);
            }
        }

        @Override
        public void conceptAssessmentCreated(Concept concept, ConceptAssessment conceptAssessment) {
            childConceptAssessmentUpdate(concept, conceptAssessment);
        }

        @Override
        public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
            if(conceptActionInterface != null){
                conceptActionInterface.trainingApplicationRequest(infoRequest);
            }
        }

        @Override
        public void handleDomainActionWithLearner(DomainAssessmentContent action) {
            if(conceptActionInterface != null){
                conceptActionInterface.handleDomainActionWithLearner(action);
            }            
        }

        @Override
        public void fatalError(String reason, String details) {

            if(conceptActionInterface != null){
                conceptActionInterface.fatalError(reason, details);
            }
        }

        @Override
        public SessionMembers getSessionMembers() {
            if (conceptActionInterface != null) {
                return conceptActionInterface.getSessionMembers();
            }
        
            return null;
        }

        @Override
        public List<MessageManager> getPlaybackMessages() {
            return conceptActionInterface.getPlaybackMessages();
        }
    }

}
