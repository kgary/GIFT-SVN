/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.grade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.metric.assessment.DefaultPerformanceMetric;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;

/**
 * This is an extension of {@link DefaultGradeMetric} that scores overall assessments for
 * conditions using a set of assessments that were manually specified by an observer
 * controller.
 * 
 * @author nroberts
 */
public class ObserverControllerGradeMetric extends DefaultGradeMetric {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ObserverControllerGradeMetric.class);
    
    /**
     * used to check whether a weight was specified for a concept or condition.
     */
    private static final double NO_WEIGHT = -1.0;

    /** The condition assessments that were manually specified by an observer controller */
    private Map<Integer, List<ScoreNodeUpdate>> ocConditionAssessments;
    
    /** 
     * A mapping from each leaf concept performance node ID to its existing score, should one exist.
     * Used to identify if a concept that has not changed already has a score that should be preserved.
     */
    private Map<Integer, GradedScoreNode> existingConceptAssessments;
    
    /**
     * Creates a new grade metric that scores condition overall assessments using the given
     * set of assessments provided by an observer controller
     * 
     * @param conceptToConditionAssessments the assessments provided by the observer controller.
     * Cannot be null.
     * @param existingConceptAssessments a mapping from each leaf concept performance node ID to its existing score.
     * Cannot be null, but can be empty.
     */
    public ObserverControllerGradeMetric(Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, Map<Integer, GradedScoreNode> existingConceptAssessments) {
        
        if(conceptToConditionAssessments == null) {
            throw new IllegalArgumentException("The provided OC assessments cannot be null");
        }
        
        this.ocConditionAssessments = conceptToConditionAssessments;
        
        if(existingConceptAssessments == null) {
            throw new IllegalArgumentException("The existing concept assessments cannot be null");
        }
        
        this.existingConceptAssessments = existingConceptAssessments;
    }
    
    @Override
    protected void updateTaskGrade(Task task, GradedScoreNode taskGrade){
        
        double aggregateGradeLevelScore = 0;
        
        //
        // Calculate aggregate grade level for this concept
        //
        
        // pre-process to determine if weights have been defined for all child concepts
        // If any child concept weight is not defined, an even weight distribution will be used.
        boolean usingWeights = true;
        for(Concept concept : task.getConcepts()){
            PerformanceMetricArguments arg = concept.getPerformanceArguments();
            usingWeights = arg != null && arg.getWeight() != NO_WEIGHT;
            if(!usingWeights){
                break;
            }
        }
        
        // fall back to even weight across the children in case weights have not been defined
        double evenWeight = 1.0 / task.getConcepts().size();
        // the total weight used in the current aggregate calculation.  Can be less than 1
        // if a child has Unknown grade (that child's weight will be subtracted).
        double weightToConsider = 1.0;
        
        for(Concept concept : task.getConcepts()){
            
            double weight;
            if(usingWeights){
                weight = getWeight(concept.getAssessment().getCourseNodeId());
            }else{
                weight = evenWeight;
            }
            
            concept.setGradeMetric(new ObserverControllerGradeMetric(ocConditionAssessments, existingConceptAssessments));
            
            GradedScoreNode conceptGrade = getNewOrExistingScore(concept, concept.isCourseConcept());
            if(conceptGrade == null){
                weightToConsider -= weight;  // don't count this concept because it has has no scoring grade
                continue;
            }
            taskGrade.addChild(conceptGrade);  // make sure to add concept score to parent task
            AssessmentLevelEnum conceptGradeEnum = conceptGrade.getAssessment();

            if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(conceptGradeEnum)){
                aggregateGradeLevelScore += ABOVE_EXPECTATION_SCORE * weight;
            }else if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(conceptGradeEnum)){
                aggregateGradeLevelScore += BELOW_EXPECTATION_SCORE * weight;
            } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(conceptGradeEnum)){
                aggregateGradeLevelScore += AT_EXPECTATION_SCORE * weight;
            }else{
                weightToConsider -= weight;  // don't count this concept because it has unknown assessment
            }
        }
        
        //
        // update grade value of concept
        //
        AssessmentLevelEnum grade = DefaultPerformanceMetric.getAssessmentFromScore(weightToConsider, aggregateGradeLevelScore);
        
        taskGrade.updateAssessment(grade);
        
        if(logger.isDebugEnabled()){
            StringBuffer sb = new StringBuffer("Task ");
            sb.append(task.getName()).append(" grade set to ").append(grade).append("\n");
            sb.append("  (score = ").append(aggregateGradeLevelScore).append(")");
            logger.debug(sb.toString());
        }
    }
    
    @Override
    protected void updateConceptGrade(Concept concept, GradedScoreNode conceptGrade){
        
        double aggregateGradeLevelScore = 0;
        
        // pre-process to determine if weights have been defined for all child conditions
        // If any child condition weight is not defined, an even weight distribution will be used.
        boolean usingWeights = true;
        for(AbstractCondition condition : concept.getConditions()){
            double weight = getWeight(condition.getId());
            if(weight == NO_WEIGHT){
                usingWeights = false;
                break;
            }
        }
        
        // fall back to even weight across the children in case weights have not been defined
        double evenConditionWeight = 1.0 / concept.getConditions().size();
        // the total weight used in the current aggregate calculation.  Can be less than 1
        // if a child has Unknown grade (that child's weight will be subtracted).
        double conditionWeightToConsider = 1.0;
        
        List<ScoreNodeUpdate> ocAssessment = ocConditionAssessments.get(concept.getNodeId());
        
        int index = 0;
        for(AbstractCondition condition : concept.getConditions()){
            
            double weight;
            if(usingWeights){
                weight = getWeight(condition.getId());
            }else{
                weight = evenConditionWeight;
            }
            
            ScoreNodeUpdate ocAssessmentLevel = null;
            
            if(ocAssessment != null) {
                ocAssessmentLevel = ocAssessment.get(index);
            }
            
            List<RawScoreNode> conditionScores = new ArrayList<>();
            condition.getScores(conditionScores, ocAssessmentLevel);
            
            if(conditionScores.isEmpty()){
                conditionWeightToConsider -= weight;  // don't count this concept because it has has no scoring grade
                continue;
            }
            
            // make sure to add all the conditions scores to this parent concept
            for(RawScoreNode rawScoreNode : conditionScores){
                conceptGrade.addChild(rawScoreNode);
            }
            
            // even weights for all scores within this single condition
            double evenConditionScoresWeight = 1.0 / conditionScores.size();

            for(RawScoreNode conditionScore : conditionScores){
                
                final AssessmentLevelEnum childAssessment = conditionScore.getAssessment(); 
                
                // a condition can have multiple overall assessment scores, we don't have weights at this
                // level of assessment.  Only at the condition as a single object.  Therefore need
                // to calculate the set of scores for a single condition based on pass/fail and take
                // the lowest grade.
                
                if(childAssessment.equals(AssessmentLevelEnum.ABOVE_EXPECTATION)){
                    aggregateGradeLevelScore += ABOVE_EXPECTATION_SCORE * evenConditionScoresWeight;
                }else if(childAssessment.equals(AssessmentLevelEnum.BELOW_EXPECTATION)){
                    aggregateGradeLevelScore += BELOW_EXPECTATION_SCORE * evenConditionScoresWeight;
                } else if(childAssessment.equals(AssessmentLevelEnum.AT_EXPECTATION)){
                    aggregateGradeLevelScore += AT_EXPECTATION_SCORE * evenConditionScoresWeight;
                }else{
                    conditionWeightToConsider -= weight;  // don't count this concept because it has unknown assessment
                }

            }
            
            index++;
        }
        
        //
        // update grade value of concept
        //
        AssessmentLevelEnum grade = DefaultPerformanceMetric.getAssessmentFromScore(conditionWeightToConsider, aggregateGradeLevelScore);
        
        conceptGrade.updateAssessment(grade);
        
        if(logger.isDebugEnabled()){
            StringBuffer sb = new StringBuffer("Concept ");
            sb.append(concept.getName()).append(" grade set to ").append(grade).append("\n");
            sb.append("  (score = ").append(aggregateGradeLevelScore).append(")");
            logger.debug(sb.toString());
        }
    }
    
    @Override
    protected void updateIntermediateConceptGrade(IntermediateConcept intermediateConcept, GradedScoreNode intermediateConceptGrade){
        
        double aggregateGradeLevelScore = 0;
        
        //
        // Calculate aggregate grade for this concept
        //
        
        // pre-process to determine if weights have been defined for all child concepts
        // If any child concept weight is not defined, an even weight distribution will be used.
        boolean usingWeights = true;
        for(Concept concept : intermediateConcept.getConcepts()){
            PerformanceMetricArguments arg = concept.getPerformanceArguments();
            usingWeights = arg != null && arg.getWeight() != NO_WEIGHT;
            if(!usingWeights){
                break;
            }
        }
        
        // fall back to even weight across the children in case weights have not been defined
        double evenWeight = 1.0 / intermediateConcept.getConcepts().size();
        // the total weight used in the current aggregate calculation.  Can be less than 1
        // if a child has Unknown grade (that child's weight will be subtracted).
        double weightToConsider = 1.0;
        
        for(Concept concept : intermediateConcept.getConcepts()){

            double weight;
            if(usingWeights){
                weight = getWeight(concept.getAssessment().getCourseNodeId());
            }else{
                weight = evenWeight;
            }
            
            // this 'intermediateConcept' and any concept that is an ancestor to this 'intermediateConcept' is an ancestor
            // of this 'concept'
            concept.setGradeMetric(new ObserverControllerGradeMetric(ocConditionAssessments, existingConceptAssessments));
            
            GradedScoreNode conceptGrade = getNewOrExistingScore(concept, intermediateConcept.hasAncestorCourseConcept() || intermediateConcept.isCourseConcept());
            if(conceptGrade == null){
                weightToConsider -= weight;  // don't count this concept because it has has no scoring grade
                continue;
            }
            intermediateConceptGrade.addChild(conceptGrade);  // make sure to add concept score to parent intermediate concept
            AssessmentLevelEnum conceptGradeEnum = conceptGrade.getAssessment();
            
            if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(conceptGradeEnum)){
                aggregateGradeLevelScore += ABOVE_EXPECTATION_SCORE * weight;
            }else if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(conceptGradeEnum)){
                aggregateGradeLevelScore += BELOW_EXPECTATION_SCORE * weight;
            } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(conceptGradeEnum)){
                aggregateGradeLevelScore += AT_EXPECTATION_SCORE * weight;
            }else{
                weightToConsider -= weight;  // don't count this concept because it has unknown assessment
            }
        }
        
        //
        // update grade value of concept
        //
        AssessmentLevelEnum grade = DefaultPerformanceMetric.getAssessmentFromScore(weightToConsider, aggregateGradeLevelScore);
        
        intermediateConceptGrade.updateAssessment(grade);
        
        if(logger.isDebugEnabled()){
            StringBuffer sb = new StringBuffer("IntermediateConcept ");
            sb.append(intermediateConcept.getName()).append(" grade set to ").append(grade).append("\n");
            sb.append("  (score = ").append(aggregateGradeLevelScore).append(")");
            logger.debug(sb.toString());
        }
    }
    
    /**
     * Determines whether the given concept should be given a new score or should preserve its existing
     * score and then returns whatever score was chosen.<br/><br/>
     * 
     * If the concept was explicitly given a score by the OC, it will <i>always</i> generate a new score.<br/><br/>
     * 
     * If a concept was not scored by the OC, a lookup will be done to see if a leaf concept score matching the
     * concept was already published. If so, the existing published score will be used. If not, a new score will be
     * calculated.
     * 
     * @param concept the concept to get a score for. Cannot be null.
     * @param courseConceptDescendant whether the concept or one of its parents is a course concept
     * @return the calculated score. Can be null if the concept is not a course concept.
     */
    private GradedScoreNode getNewOrExistingScore(Concept concept, boolean courseConceptDescendant) {
        
        List<ScoreNodeUpdate> ocAssessment = ocConditionAssessments.get(concept.getNodeId());
        
        if(ocAssessment == null || ocAssessment.isEmpty()) {
            
            /* If the OC did not provide an assessment for this concept, attempt to maintain
             * whatever existing assessment the concept has, if any */
            GradedScoreNode existingAssessment = existingConceptAssessments.get(concept.getNodeId());
            if(existingAssessment != null) {
                return existingAssessment;
            }
        }
        
        return concept.getScore(courseConceptDescendant);
    }

    @Override
    public String toString(){

        return "[ObserverControllerGradeMetric]";
    }

}
