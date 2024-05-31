/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.IntermediateConceptAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.AssessmentDiff;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff.PerformanceStateAttrFields;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class is responsible for classifying a change in state for a task.
 *
 * @author mhoffman
 *
 */
public class TaskPerformanceStateClassifier extends AbstractClassifier {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TaskPerformanceStateClassifier.class);

    /** the learner's task performance state */
    private TaskPerformanceState state;

    /** the last assessment provided to the classifier */
    private TaskAssessment prevTaskAssessment;

    /**
     * The set of attribute fields that have changed when updating assessments.
     * This is here to be reused each time a comparison is done so only 1 Set is
     * being allocated to memory.
     */
    private final Set<PerformanceStateAttrFields> diffAttrFields = new HashSet<>();

    /**
     * Default - initialize the classifier
     */
    public TaskPerformanceStateClassifier() {
        initialize();
    }

    /**
     * Return the learner task performance state
     *
     * @return the state for this classifier. Won't be null but can be empty.
     */
    @Override
    public TaskPerformanceState getState() {
        return state;
    }

    /**
     * Return the last task assessment data that was received by this classifier
     *
     * @return the previous task assessment that was provided to this classifier.  Can be null.
     */
    @Override
    public TaskAssessment getCurrentData() {
       return prevTaskAssessment;
    }

    @Override
    public boolean updateState(TaskAssessment newTaskAssessment) {

        //TODO: there should be a concept state classifier too, for now just pass the changes through

        if (newTaskAssessment == null) {
            logger.error("The state data provided is null, therefore can't handle this update");
            return false;
        }

        /* updated state received */
        state.setContainsObservedAssessmentCondition(newTaskAssessment.isContainsObservedAssessmentCondition());
        state.setDifficulty(newTaskAssessment.getDifficulty());
        state.setDifficultyReason(newTaskAssessment.getDifficultyReason());
        state.setStress(newTaskAssessment.getStress());
        state.setStressReason(newTaskAssessment.getStressReason());
        List<ConceptPerformanceState> existingConceptStates = state.getConcepts();

        //
        // compare new assessment to previous
        //

        /* check the task assessment */
        boolean changed = updateTaskAssessment(newTaskAssessment, prevTaskAssessment);

        /* check the concept assessment(s) */
        final List<ConceptAssessment> newConceptAssessments = newTaskAssessment.getConceptAssessments();
        final List<ConceptAssessment> oldConceptAssessments = prevTaskAssessment != null
                ? prevTaskAssessment.getConceptAssessments()
                : new ArrayList<>();

        /* check concepts that are not being assessed */
        for (ConceptAssessment oldConceptAssessment : oldConceptAssessments) {

            final UUID oldAssConceptCourseNodeId = oldConceptAssessment.getCourseNodeId();
            final int oldAssConceptId = oldConceptAssessment.getNodeId();

            /* If no longer being assessed, remove it */
            boolean oldConceptIsBeingAssessed = false;
            for(ConceptAssessment newConceptAssessment : newConceptAssessments){
                if(newConceptAssessment.getCourseNodeId().equals(oldAssConceptCourseNodeId)){
                    oldConceptIsBeingAssessed = true;
            }
            }
            if (!oldConceptIsBeingAssessed) {
                existingConceptStates.removeIf(state -> state.getState().getNodeId() == oldAssConceptId);
            }
        }

        /* check concepts that are still being assessed */
        for (ConceptAssessment newConceptAssessment : newConceptAssessments) {
            changed |= handleConceptAssessmentState(newConceptAssessment, existingConceptStates);
        }

        /* finally update assessment just copy it for now... */
        prevTaskAssessment = newTaskAssessment;

        return changed;
    }

    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return null;
    }

    /**
     * Create the concept performance state for a newly discovered concept assessment.
     * This method handles recursively going down any intermediate concept hierarchies.
     *
     * @param assessment - the new concept assessment to create a concept performance state object from.
     * @return ConceptPerformanceState
     */
    private ConceptPerformanceState buildConceptPerformanceState(AbstractAssessment assessment){

        PerformanceStateAttribute attribute =
                new PerformanceStateAttribute(assessment.getName(), assessment.getNodeId(), assessment.getCourseNodeId().toString(),
                        assessment.getAssessmentLevel(), System.currentTimeMillis(),
                        AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(),
                        assessment.getAssessmentLevel(), System.currentTimeMillis());

        /* Set metric values. Assessment is set in the constructor. */
        attribute.setConfidence(assessment.getConfidence(), true);
        attribute.setCompetence(assessment.getCompetence(), true);
        attribute.setPriority(assessment.getPriority(), true);
        attribute.setTrend(assessment.getTrend(), true);

        /* Set hold values */
        attribute.setAssessmentHold(assessment.isAssessmentHold());
        attribute.setConfidenceHold(assessment.isConfidenceHold());
        attribute.setCompetenceHold(assessment.isCompetenceHold());
        attribute.setPriorityHold(assessment.isPriorityHold());
        attribute.setTrendHold(assessment.isTrendHold());

        /* Set additional values */
        attribute.setEvaluator(assessment.getEvaluator());
        attribute.setNodeStateEnum(assessment.getNodeStateEnum());
        attribute.setObserverComment(assessment.getObserverComment());
        attribute.setObserverMedia(assessment.getObserverMedia());
        attribute.setAuthoritativeResource(assessment.getAuthoritativeResource());
        attribute.setAssessmentExplanation(assessment.getAssessmentExplanation());
        attribute.setAssessedTeamOrgEntities(assessment.getAssessedTeamOrgEntities());
        attribute.setScenarioSupportNode(assessment.isScenarioSupportNode());
        attribute.setPerformanceAssessmentTime(assessment.getTime());
        
        // if this was an observed state update, set the observation started time
        if(StringUtils.isNotBlank(assessment.getEvaluator())){
            attribute.setObservationStartedTime(assessment.getTime());
        }

        ConceptPerformanceState cPerfState;
        if(assessment instanceof IntermediateConceptAssessment){

            //have to add any sub-concepts as well
            IntermediateConceptAssessment iConceptAssessment = (IntermediateConceptAssessment)assessment;
            List<ConceptPerformanceState> conceptMap = new ArrayList<>(iConceptAssessment.getConceptAssessments().size());
            for(ConceptAssessment subconceptAssessment : iConceptAssessment.getConceptAssessments()){
                cPerfState = buildConceptPerformanceState(subconceptAssessment);
                conceptMap.add(cPerfState);
            }

            cPerfState = new IntermediateConceptPerformanceState(attribute, conceptMap);

        }else{
            cPerfState = new ConceptPerformanceState(attribute);
        }

        cPerfState.setContainsObservedAssessmentCondition(assessment.isContainsObservedAssessmentCondition());
        return cPerfState;
    }

    /**
     * Return the concept performance state from the collection with the matching id
     *
     * @param conceptPerfStates the collection to find the appropriate concept in.  If null or empty, null is returned.
     * @param conceptNodeId the id of a concept to find in the collection.  If null, null is returned.
     * @return can be null if not found in the provided collection
     */
    private ConceptPerformanceState getConceptAssessment(List<ConceptPerformanceState> conceptPerfStates, Integer conceptNodeId){

        if(CollectionUtils.isEmpty(conceptPerfStates)){
            return null;
        }else if(conceptNodeId == null){
            return null;
        }

        for(ConceptPerformanceState conceptAssessment : conceptPerfStates){
            if(conceptAssessment.getState().getNodeId() == conceptNodeId){
                return conceptAssessment;
            }
        }

        return null;
    }

    /**
     * Update the learner state course concept performance information from the
     * new concept assessment provided.
     *
     * @param newConceptAssessment a new concept performance assessment which
     *        may contain subconcept assessments as well
     * @param existingConceptStates collection of concept states from the current learner
     *        performance state information, used to update a change in learner
     *        state for a concept.
     * @return whether one or more concepts have a change in assessment
     */
    private boolean handleConceptAssessmentState(ConceptAssessment newConceptAssessment,
            List<ConceptPerformanceState> existingConceptStates) {
        final ConceptPerformanceState existingConceptState = getConceptAssessment(existingConceptStates,
                newConceptAssessment.getNodeId());

        /* A concept performance state has not been created for this concept
         * assessment so build a new one */
        if (existingConceptState == null) {

            ConceptPerformanceState cPerfState = buildConceptPerformanceState(newConceptAssessment);

            /* TODO: eventually the classifier probably shouldn't populate the
             * predicted value which is currently the predictors job */
            existingConceptStates.add(cPerfState);
            return true;
        }

        /* Get the existing performance state attributes */
        final PerformanceStateAttribute psa = existingConceptState.getState();

        /* An existing concept state was found. Check for differences in state
         * values. Reuse the diffAttrFields Set so we aren't recreating a new
         * HashSet each time. */
        diffAttrFields.clear();
        boolean changed = AssessmentDiff.performDiff(psa, newConceptAssessment, diffAttrFields, true);

        /* Update the observed assessment condition flag. This does not trigger
         * a change by itself. */
        existingConceptState
                .setContainsObservedAssessmentCondition(newConceptAssessment.isContainsObservedAssessmentCondition());

        /* Ignore any holds if this was a manual update (evaluator exists). */
        final boolean ignoreHold = StringUtils.isNotBlank(newConceptAssessment.getEvaluator());

        /* Update changed fields */
        for (PerformanceStateAttrFields changedField : diffAttrFields) {
            switch (changedField) {
            case AUTHORATATIVE_RESOURCE:
                psa.setAuthoritativeResource(newConceptAssessment.getAuthoritativeResource());
                break;
            case PREDICTED:
                /* TODO: eventually the classifier probably shouldn't populate
                 * the predicted value which is currently the predictors job */
                psa.updatePredicted(newConceptAssessment.getAssessmentLevel());
                break;
            case PRIORITY:
                psa.setPriority(newConceptAssessment.getPriority(), ignoreHold);
                break;
            case CONFIDENCE:
                psa.setConfidence(newConceptAssessment.getConfidence(), ignoreHold);
                break;
            case COMPETENCE:
                psa.setCompetence(newConceptAssessment.getCompetence(), ignoreHold);
                break;
            case TREND:
                psa.setTrend(newConceptAssessment.getTrend(), ignoreHold);
                break;
            case NODE_STATE:
                psa.setNodeStateEnum(newConceptAssessment.getNodeStateEnum());
                break;
            case ASSESSMENT_HOLD:
                psa.setAssessmentHold(newConceptAssessment.isAssessmentHold());
                break;
            case PRIORITY_HOLD:
                psa.setPriorityHold(newConceptAssessment.isPriorityHold());
                break;
            case CONFIDENCE_HOLD:
                psa.setConfidenceHold(newConceptAssessment.isConfidenceHold());
                break;
            case COMPETENCE_HOLD:
                psa.setCompetenceHold(newConceptAssessment.isCompetenceHold());
                break;
            case TREND_HOLD:
                psa.setTrendHold(newConceptAssessment.isTrendHold());
                break;
            case ASSESSMENT_EXPLANATION:
                psa.setAssessmentExplanation(newConceptAssessment.getAssessmentExplanation());
                break;
            case ASSESSED_TEAM_ORG_ENTITIES:
                psa.setAssessedTeamOrgEntities(newConceptAssessment.getAssessedTeamOrgEntities());
                break;
            case OBSERVER_MEDIA:
                psa.setObserverMedia(newConceptAssessment.getObserverMedia());
                break;
            case SHORT_TERM: /* Intentional drop-through */
            case PERFORMANCE_ASSESSMENT_TIMESTAMP:
                /* Update the assessment value if it changed or if the
                 * timestamps are different. The assessment value might not have
                 * changed but GIFT supports assessing the same value back to
                 * back. */
                psa.updateShortTerm(newConceptAssessment.getAssessmentLevel(), ignoreHold);
                break;
            case EVALUATOR: /* Intentional drop-through */
            case OBSERVER_COMMENT:
                /* Handled below if something changed */
                break;
            case LONG_TERM: /* Intentional drop-through */
            case SHORT_TERM_TIMESTAMP: /* Intentional drop-through */
            case LONG_TERM_TIMESTAMP: /* Intentional drop-through */
            case PREDICTED_TIMESTAMP:
                /* Not supported with concept assessments */
                break;
            default:
                logger.warn("Assessment changed value for unhandled property '" + changedField + "'.");
            }
        }

        /* Finished with the diffAttrFields Set. Clear for next time. */
        diffAttrFields.clear();

        /* Check sub-concepts if this is an intermediate concept */
        if (newConceptAssessment instanceof IntermediateConceptAssessment) {
            final List<ConceptPerformanceState> intermediateConceptStates = ((IntermediateConceptPerformanceState) existingConceptState)
                    .getConcepts();
            for (ConceptAssessment newSubconceptAssessment : ((IntermediateConceptAssessment) newConceptAssessment)
                    .getConceptAssessments()) {
                changed |= handleConceptAssessmentState(newSubconceptAssessment, intermediateConceptStates);
            }
        }

        /* Update these fields only if something has changed already */
        if (changed) {
            psa.setEvaluator(newConceptAssessment.getEvaluator());
            psa.setObserverComment(newConceptAssessment.getObserverComment());
            psa.setPerformanceAssessmentTime(newConceptAssessment.getTime());

            /* If this was an observed state update, set the observation started
             * time */
            if (StringUtils.isNotBlank(newConceptAssessment.getEvaluator())) {
                psa.setObservationStartedTime(newConceptAssessment.getTime());
            }
        }

        return changed;
    }

    /**
     * Update the task performance state when a change in assessment is found
     *
     * @param newTAss the incoming new performance assessment for a task. Can't be null
     * @param prevTAss the previous performance assessment for a task.  Can be null if this is the first incoming performance
     * assessment for this task.
     * @return boolean - true iff the task assessment has changed assessment levels
     */
    private boolean updateTaskAssessment(TaskAssessment newTAss, TaskAssessment prevTAss){
        if (newTAss == null) {
            throw new IllegalArgumentException("The parameter 'newTAss' cannot be null.");
        }

        boolean changed = false;

        // Update/Create the state if any of three conditions are met:
        // 1. This is the first task assessment received in this class
        // 2. The classified state has not been set and needs to be set
        // 3. There was a change in assessment
        if (prevTAss == null || state.getState() == null || isTaskAssessmentValuesDifferent(newTAss, prevTAss)) {
            changed = true;

            final boolean ignoreHold = StringUtils.isNotBlank(newTAss.getEvaluator());
            PerformanceStateAttribute tState = state.getState();
            if (tState == null) {
                tState = new PerformanceStateAttribute(newTAss.getName(), newTAss.getNodeId(),
                        newTAss.getCourseNodeId().toString(), newTAss.getAssessmentLevel(), System.currentTimeMillis(),
                        AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), newTAss.getAssessmentLevel(),
                        System.currentTimeMillis());

                state.updateState(tState);
            } else {
                tState.updateShortTerm(newTAss.getAssessmentLevel(), ignoreHold);
            }

            /* Set metric values. The assessment is set in the constructor. */
            tState.setConfidence(newTAss.getConfidence(), ignoreHold);
            tState.setCompetence(newTAss.getCompetence(), ignoreHold);
            tState.setPriority(newTAss.getPriority(), ignoreHold);
            tState.setTrend(newTAss.getTrend(), ignoreHold);

            /* Set hold values */
            tState.setAssessmentHold(newTAss.isAssessmentHold());
            tState.setConfidenceHold(newTAss.isConfidenceHold());
            tState.setCompetenceHold(newTAss.isCompetenceHold());
            tState.setPriorityHold(newTAss.isPriorityHold());
            tState.setTrendHold(newTAss.isTrendHold());

            /* Set additional values */
            tState.setEvaluator(newTAss.getEvaluator());
            tState.setNodeStateEnum(newTAss.getNodeStateEnum());
            tState.setObserverComment(newTAss.getObserverComment());
            tState.setAuthoritativeResource(newTAss.getAuthoritativeResource());
            tState.setObserverMedia(newTAss.getObserverMedia());
            tState.setAssessmentExplanation(newTAss.getAssessmentExplanation());
            tState.setScenarioSupportNode(newTAss.isScenarioSupportNode());
            tState.setPerformanceAssessmentTime(newTAss.getTime());

            // if this was an observed state update, set the observation started time
            if(StringUtils.isNotBlank(newTAss.getEvaluator())){
                tState.setObservationStartedTime(newTAss.getTime());
            }
        }

        return changed;
    }

    /**
     * Checks if the two task assessments contain different class member values.
     * This is different from {@link TaskAssessment#equals(Object)} because we
     * do not want to compare child concepts.
     *
     * @param newTAss the new task assessment
     * @param prevTAss the previous task assessment
     * @return true if the new task assessment has different values than the
     *         previous for select variables; false if they are the same.
     */
    private boolean isTaskAssessmentValuesDifferent(TaskAssessment newTAss, TaskAssessment prevTAss) {
        /* Passing in a null Set because we don't care about which fields are
         * different. We do care about timestamps, so passing in a true flag. */
        return AssessmentDiff.performDiff(newTAss, prevTAss, null, true);
    }
    
    @Override
    public void knowledgeSessionCompleted(){
        initialize();
    }
    
    @Override
    public void knowledgeSessionStarted(){
        initialize();
    }
    
    @Override
    public void domainSessionStarted(){
        initialize();
    }
    
    /**
     * Reset the state being tracked by this classifier.  This is useful to maintain
     * task performance state tracking unique to each knowledge session (DKF).  
     * E.g. don't want the pedagogical module using task/concept state from the end of the last
     * DKF execution which could be in this course or a previous course since GIFT keeps the last
     * learner state in memory for so many hours after a course ends for a user.  Also don't want learner
     * state details to show in game master timeline for a previous knowledge session.
     */
    private void initialize(){
        state = new TaskPerformanceState();
        prevTaskAssessment = null;
    }

    @Override
    public String toString(){

        StringBuilder sb = new StringBuilder();
        sb.append("[TaskPerformanceStateClassifier: ");
        sb.append(super.toString());

        if(state != null){
            sb.append("\nassessment = ").append(state);
        }

        sb.append("]");
        return sb.toString();
    }

}
