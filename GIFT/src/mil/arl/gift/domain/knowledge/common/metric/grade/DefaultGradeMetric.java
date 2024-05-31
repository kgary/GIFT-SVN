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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.metric.assessment.DefaultPerformanceMetric;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;

/**
 * This is the default grade metric algorithm for performance nodes.  It determines
 * the overall assessment grade for a parent based on the grades of the children. Currently
 * the grade for a set of overall assessments for a single condition follows a 4.0 grading 
 * scale where Below Expectation is under a 2.0 (F->C), At expectation is 2-3.33 (C->B+) and 
 * Above is 3.33-4.0 (B+->A).  Then At and Above equate to Pass grade.  
 * From their the roll up uses weights to determine how influential the child grade is to the parent's grade.  
 * The parent grade calculation follows a 1.0 grading scale, where less than 0.5 results in Fail grade.
 *  
 * 
 * @author mhoffman
 *
 */
public class DefaultGradeMetric implements GradeMetricInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DefaultGradeMetric.class);
    
    /** arbitrary point values to give overall assessments in a single condition in order to calculate the condition's average assessment */
    public static final double ABOVE_EXPECTATION_SCORE = 4;  //equivalent to A grade
    public static final int AT_EXPECTATION_SCORE = 2;  //equivalent to C grade
    public static final int BELOW_EXPECTATION_SCORE = 0; //equivalent to F grade
    
    /** the average score must be under a 2.0 to get a Below expectation result (e.g. F to C- grade) */
    public static final double BELOW_EXPECTATION_UPPER_THRESHOLD = 1.7; 
    
    /** 
     * the average score must be under a 3.33 (and greater than or equal to 2.0) to get an At expectation result (e.g. C to B+ grade) 
     * This infers that greater than or equal to 3.33 is Above Expectation
     * */
    public static final double AT_EXPECTATION_UPPER_THRESHOLD = 3.33; 
    
    /**
     * used to check whether a weight was specified for a concept or condition.
     */
    private static final double NO_WEIGHT = -1.0;
    
    /**
     * mapping of task/concept/condition course level unique id to the performance metric argument(s) (e.g. weights).
     * The map can be null.  The performance metric arguments for a task, concept, or condition can be null.
     */
    private Map<UUID, PerformanceMetricArguments> childConceptOrConditionArgsMap;
    
    @Override
    public void setMetricArgsForChildConceptOrCondition(Map<UUID, PerformanceMetricArguments> childConceptOrConditionArgsMap){
        this.childConceptOrConditionArgsMap = childConceptOrConditionArgsMap;
    }

    @Override
    public void updateGrade(AbstractPerformanceAssessmentNode assessmentNode, GradedScoreNode gradeNode) {
        
        if(assessmentNode instanceof IntermediateConcept){
            updateIntermediateConceptGrade((IntermediateConcept)assessmentNode, gradeNode);
        }else if(assessmentNode instanceof Task){
            updateTaskGrade((Task)assessmentNode, gradeNode);
        }else{
            updateConceptGrade((Concept)assessmentNode, gradeNode);
        }
        
    }
    
    /**
     * Update the provided intermediate concept score node with a grade based on the children's grades.
     * 
     * @param intermediateConcept the intermediate concept to set the grade for based on the children's grades
     * @param intermediateConceptGrade where to update the grade for this intermediate concept at
     */
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
            GradedScoreNode conceptGrade = concept.getScore(intermediateConcept.hasAncestorCourseConcept() || intermediateConcept.isCourseConcept());
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
     * Update the provided task score node with a grade based on the children's grades.
     * 
     * @param task the task to set the grade for based on the children's grades
     * @param taskGrade where to update the grade for this task at
     */
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
            
            GradedScoreNode conceptGrade = concept.getScore(false);  // false since the parent to this concept is a task, a task can't be a course concept
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

    /**
     * Update the provided concept score node with a grade based on the children's grades.
     * 
     * @param concept the concept to set the grade for based on the children's grades
     * @param conceptGrade where to update the grade for this concept at
     */
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
        
        for(AbstractCondition condition : concept.getConditions()){
            
            double weight;
            if(usingWeights){
                weight = getWeight(condition.getId());
            }else{
                weight = evenConditionWeight;
            }
            
            List<RawScoreNode> conditionScores = new ArrayList<>();
            condition.getScores(conditionScores);            
            
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
                
                if(childAssessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
                    aggregateGradeLevelScore += ABOVE_EXPECTATION_SCORE * evenConditionScoresWeight;
                }else if(childAssessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                    aggregateGradeLevelScore += BELOW_EXPECTATION_SCORE * evenConditionScoresWeight;
                } else if(childAssessment == AssessmentLevelEnum.AT_EXPECTATION){
                    aggregateGradeLevelScore += AT_EXPECTATION_SCORE * evenConditionScoresWeight;
                }else{
                    conditionWeightToConsider -= weight;  // don't count this concept because it has unknown assessment
                }

            }
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
    
    /**
     * Return the weight value for the concept or condition specified.
     * 
     * @param childConceptOrConditionId the id of the child concept or condition to get authored weight for.
     * @return can return the default of 1.0 for:</br>
     * 1. the {@link #childConceptOrConditionArgsMap} is null</br>
     * 2. the child concept or condition is not in the map</br>
     * 3. the args for that concept or condition are null.
     */
    protected double getWeight(UUID childConceptOrConditionId){
        
        if(childConceptOrConditionArgsMap != null && childConceptOrConditionArgsMap.containsKey(childConceptOrConditionId)){
            PerformanceMetricArguments arg = childConceptOrConditionArgsMap.get(childConceptOrConditionId);
            if(arg != null){
                return arg.getWeight();
            }
        }
        
        return NO_WEIGHT;
    }

    @Override
    public String toString(){

        return "[DefaultGradeMetric]";
    }

}
