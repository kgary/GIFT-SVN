/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.assessment;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.ProxyTaskAssessment;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;

/**
 * This is the default performance assessment metric algorithm for performance nodes.  It
 * determines the assessment level for a parent based on the children assessment value(s).
 * Currently this can apply authored weights to the children's assessments.  The parent assessment
 * calculation follows a 4.0 grading scale where Below Expectation is under a 2.0 (F->C), At expectation
 * is 2-3.33 (C->B+) and Above is 3.33-4.0 (B+->A).
 *
 * @author mhoffman
 *
 */
public class DefaultPerformanceMetric implements PerformanceMetricInterface {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DefaultPerformanceMetric.class);

    /** arbitrary point values to give children assessment values in order to calculate the parents assessment */
    private static final double ABOVE_EXPECTATION_SCORE = 4;  //equivalent to A grade
    private static final int AT_EXPECTATION_SCORE = 2;  //equivalent to C grade
    private static final int BELOW_EXPECTATION_SCORE = 0; //equivalent to F grade
    
    /** the average score must be under a 2.0 to get a Below expectation result (e.g. under a C grade) */
    private static final int BELOW_EXPECTATION_UPPER_THRESHOLD = 2; 
    
    /** 
     * the average score must be under a 3.33 (and greater than or equal to 2.0) to get an At expectation result (e.g. C to B grade) 
     * This infers that greater than or equal to 3.33 is Above Expectation
     * */
    private static final double AT_EXPECTATION_UPPER_THRESHOLD = 3.33; 
    
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
    public boolean setPerformance(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {

        if(node instanceof IntermediateConcept){
            return setIntermediateConceptPerformance(node, assessmentProxy, holdValue);
        }else if(node instanceof Task){
            return setTaskPerformance(node, assessmentProxy, holdValue);
        }else{
            return setConceptPerformance(node, assessmentProxy, holdValue);
        }

    }

    /**
     * Set the task assessment value based on the assessments of the direct child concepts under this task.
     * @param node the task performance node that will have its child concepts considered for roll up assessment
     * @param assessmentProxy where to change this task's assessment value
     * @param holdValue whether to hold the value and not let GIFT change it without external over-ride.
     * When null the value is not changed from the current value.
     * @return true if the performance assessment was changed on the node
     */
    private boolean setTaskPerformance(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue){

        double aggregateAssessmentLevelScore = 0;

        Task task = (Task)node;
        ProxyTaskAssessment taskAssessment = task.getAssessment();

        if (taskAssessment == null) {
            return true;
        }

        //
        // Calculate aggregate assessment level for this concept
        //
        
        // pre-process to determine if weights have been defined for all child concepts
        // If any child concept weight is not defined, an even weight distribution will be used.
        boolean usingWeights = true;
        for(UUID conceptAssessmentId : taskAssessment.getConcepts()){
            double weight = getWeight(conceptAssessmentId);
            if(weight == NO_WEIGHT){
                usingWeights = false;
                break;
            }
        }
        
        // fall back to even weight across the children in case weights have not been defined
        double evenWeight = 1.0 / taskAssessment.getConcepts().size();
        // the total weight used in the current aggregate calculation.  Can be less than 1
        // if a child has Unknown assessment (that child's weight will be subtracted).
        double weightToConsider = 1.0;
        
        for(UUID conceptAssessmentId : taskAssessment.getConcepts()){

            //get the concept's assessment from the proxy
            AbstractAssessment cAssessment = assessmentProxy.get(conceptAssessmentId);
            AssessmentLevelEnum level = cAssessment.getAssessmentLevel();
            
            double weight;
            if(usingWeights){
                weight = getWeight(cAssessment.getCourseNodeId());
            }else{
                weight = evenWeight;
            }

            if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += ABOVE_EXPECTATION_SCORE * weight;
            }else if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += BELOW_EXPECTATION_SCORE * weight;
            } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += AT_EXPECTATION_SCORE * weight;
            }else{
                weightToConsider -= weight;  // don't count this concept because it has unknown assessment
            }

        }

        // update current assessment value
        AssessmentLevelEnum newLevel = getAssessmentFromScore(weightToConsider, aggregateAssessmentLevelScore);        

        String oldAssessment = taskAssessment.toString();

        //always apply the performance assessment value even if the same as before to allow
        //assessments of the same value to occur back to back.  A check should be made in Task/Concept.java for whether
        //an update is warranted based on updated assessments being activated
        taskAssessment.updateAssessment(newLevel);

        if(holdValue != null){
            taskAssessment.setAssessmentHold(holdValue);
        }

        if(logger.isDebugEnabled()){
            StringBuffer sb = new StringBuffer("Task ");
            sb.append(task.getName()).append(" assessment changed from\n").append(oldAssessment).append(", to\n");
            sb.append(newLevel).append("  (score = ").append(aggregateAssessmentLevelScore).append(")");
            logger.debug(sb.toString());
        }

        //(see comment a few lines up)
        return true;
    }
    
    /**
     * Using the total score of nodes with assessments at a single level (not normalized) and the total weight
     * of those nodes with assessments (minus the weights for those w/ Unknown assessment), determine the
     * assessment level based on the rubric defined at the top of this class.</br>     * 
     * Test Cases:</br>
     * (#4831) - Task w/ 4 child concepts, (20,33.3,33.3,0,13.3%); Varied assessment on first 2.
     * @param weightToConsider - total weight of the nodes at a specific level, will be between 0 and 1.0 inclusive.  Will
     * be less than 1.0 if any of the nodes have an Unknown assessment.  In that case that nodes weight will be subtracted.
     * This is used to normalize the aggregate score and to scale the rubric based how much scoring is available.  Will be rounded to 2 decimal places.
     * @param aggregateAssessmentLevelScore the total score of all non-Unknown nodes at a single level in the task structure.
     * Will be rounded to 2 decimal places.
     * @return an assessment level calculated from the total points and weight of those points.  Will be unknown
     * if the total weights is less than 1% (0.01).
     */
    public static AssessmentLevelEnum getAssessmentFromScore(double weightToConsider, double aggregateAssessmentLevelScore){
        
        AssessmentLevelEnum newLevel = null;
        
        // round to 2 decimal places to avoid precision issues introduced with calculations (e.g. 1.999999997 -> 2.0)
        // Note: can't just call Math.round(weight)/1.0, e.g. 1.98997, we would want 1.98 which the following code
        // would deliver but Math.round first would give us 2.0.  The following code considers the first two decimal place values.
        weightToConsider = Math.round(weightToConsider*100.0)/100.0;
        aggregateAssessmentLevelScore = Math.round(aggregateAssessmentLevelScore*100.0)/100.0;
        
        // normalize to the weights considered in the total score
        // This accounts for remove weight from Unknown assessments in order to not
        // negatively score this aggregate assessment due to Unknown child assessments.
        // Note: instead of using BigDecimal for precision and determining if close enough to
        //       zero here, use the smallest possible defined weight of 1% (0.01).
        if(weightToConsider >= 0.01){
            aggregateAssessmentLevelScore /= weightToConsider;
            
            if(aggregateAssessmentLevelScore < (BELOW_EXPECTATION_UPPER_THRESHOLD * weightToConsider)){
                newLevel = AssessmentLevelEnum.BELOW_EXPECTATION;
            }else if(aggregateAssessmentLevelScore >= (AT_EXPECTATION_UPPER_THRESHOLD * weightToConsider)){
                newLevel = AssessmentLevelEnum.ABOVE_EXPECTATION;
            }else{
                // fall back on At instead of Above - feels safer than giving out Above assessments
                newLevel = AssessmentLevelEnum.AT_EXPECTATION;
            }
        }else{
            newLevel = AssessmentLevelEnum.UNKNOWN;
        }
        
        return newLevel;
    }

    /**
     * Set the intermediate concept assessment value based on the assessments of the direct child concepts under this intermediate concept.
     * @param node the intermediate concept performance node that will have its child concepts considered for roll up assessment
     * @param assessmentProxy where to change this intermediate concept's assessment value
     * @param holdValue whether to hold the value and not let GIFT change it without external over-ride.
     * When null the value is not changed from the current value.
     * @return true if the performance assessment was changed on the node
     */
    private boolean setIntermediateConceptPerformance(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue){

        double aggregateAssessmentLevelScore = 0;

        IntermediateConcept intermediateConcept = (IntermediateConcept)node;
        if (intermediateConcept.getAssessment() == null) {
            return true;
        }

        //
        // Calculate aggregate assessment level score for this intermediate concept
        //
        
        // pre-process to determine if weights have been defined for all child concepts
        // If any child concept weight is not defined, an even weight distribution will be used.
        boolean usingWeights = true;
        for(Concept concept : intermediateConcept.getConcepts()){
            double weight = getWeight(concept.getAssessment().getCourseNodeId());
            if(weight == NO_WEIGHT){
                usingWeights = false;
                break;
            }
        }
        
        // fall back to even weight across the children in case weights have not been defined
        double evenWeight = 1.0 / intermediateConcept.getConcepts().size();
        // the total weight used in the current aggregate calculation.  Can be less than 1
        // if a child has Unknown assessment (that child's weight will be subtracted).
        double weightToConsider = 1.0;
        
        for(Concept concept : intermediateConcept.getConcepts()){
            AbstractAssessment assessment = concept.getAssessment();
            AssessmentLevelEnum level = assessment.getAssessmentLevel();
            
            double weight;
            if(usingWeights){
                weight = getWeight(assessment.getCourseNodeId());
            }else{
                weight = evenWeight;
            }

            if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += ABOVE_EXPECTATION_SCORE * weight;
            }else if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += BELOW_EXPECTATION_SCORE * weight;
            } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += AT_EXPECTATION_SCORE * weight;
            }else{
                weightToConsider -= weight;  // don't count this concept because it has unknown assessment
            }
        }

        // update current assessment value
        AssessmentLevelEnum newLevel = getAssessmentFromScore(weightToConsider, aggregateAssessmentLevelScore);        

        String oldAssessment = intermediateConcept.toString();

        //always apply the performance assessment value even if the same as before to allow
        //assessments of the same value to occur back to back.  A check should be made in Task/Concept.java for whether
        //an update is warranted based on updated assessments being activated
        intermediateConcept.getAssessment().updateAssessment(newLevel);

        if(holdValue != null){
            intermediateConcept.getAssessment().setAssessmentHold(holdValue);
        }

        if(logger.isDebugEnabled()){
            StringBuffer sb = new StringBuffer("Subconcept ");
            sb.append(intermediateConcept.getName()).append(" assessment changed from\n").append(oldAssessment).append(", to\n");
            sb.append(newLevel).append("  (score = ").append(aggregateAssessmentLevelScore).append(")");
            logger.debug(sb.toString());
        }

        //(see comment a few lines up)
        return true;
    }

    /**
     * Set the concept assessment value based on the assessments of the direct child conditions under this concept.
     * @param node the concept performance node that will have its child conditions considered for roll up assessment
     * @param assessmentProxy where to change this concept's assessment value
     * @param holdValue whether to hold the value and not let GIFT change it without external over-ride.
     * When null the value is not changed from the current value.
     * @return true if the performance assessment was changed on the node
     */
    private boolean setConceptPerformance(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue){

        double aggregateAssessmentLevelScore = 0;

        Concept concept = (Concept)node;

        if(concept.getAssessment() == null) {
            return true;
        }

        //
        // Calculate aggregate assessment level score for this concept
        //
        
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
        
        concept.getAssessment().setAssessmentExplanation(null);  //clear to get the latest from the conditions
        Map<String, AssessmentLevelEnum> overallLearnerAssessment = new HashMap<>();

        // fall back to even weight across the children in case weights have not been defined
        double evenWeight = 1.0 / concept.getConditions().size();
        // the total weight used in the current aggregate calculation.  Can be less than 1
        // if a child has Unknown assessment (that child's weight will be subtracted).
        double weightToConsider = 1.0;
        
        for(AbstractCondition condition : concept.getConditions()){
            final AssessmentLevelEnum level = condition.getAssessment();

            final Set<String> violators = condition.getViolatorTeamOrgEntries();
            final boolean hasViolators = CollectionUtils.isNotEmpty(violators);

            Map<String, AssessmentLevelEnum> conditionLearnerAssessment = new HashMap<>();

            /* Set the base assessment for all learners */
            if (condition.getTeamOrgRefs() != null) {
                /* If violators exist then the base is unknown because only the
                 * violators have a known assessment at this point.
                 * 
                 * If no violators exist, then everyone has the condition
                 * assessment. */
                final AssessmentLevelEnum baseLevel = hasViolators ? AssessmentLevelEnum.UNKNOWN : level;
                for (String learner : condition.getTeamOrgRefs().getTeamMemberRef()) {
                    conditionLearnerAssessment.put(learner, baseLevel);
                }
            }

            /* Update the assessment for the violators */
            if (hasViolators) {
                for (String learner : violators) {
                    conditionLearnerAssessment.put(learner, level);
                }
            }

            /*- Only update the overall learner assessment if the new assessment
             * has a higher priority than the currently evaluated assessment.
             * 
             * Assessment Priority Level: BELOW > AT > ABOVE > UNKNOWN > null */
            for (Entry<String, AssessmentLevelEnum> entry : conditionLearnerAssessment.entrySet()) {
                final String learner = entry.getKey();
                final AssessmentLevelEnum assessment = entry.getValue();

                final AssessmentLevelEnum oldAssessment = overallLearnerAssessment.get(learner);
                if (oldAssessment == null || oldAssessment.getValue() > assessment.getValue()) {
                    overallLearnerAssessment.put(learner, assessment);
                }
            }
            
            double weight;
            if(usingWeights){
                weight = getWeight(condition.getId());
            }else{
                weight = evenWeight;
            }

            if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += ABOVE_EXPECTATION_SCORE * weight;
            }else if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += BELOW_EXPECTATION_SCORE * weight;
            } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(level)){
                aggregateAssessmentLevelScore += AT_EXPECTATION_SCORE * weight;
            }else{
                weightToConsider -= weight;  // don't count this concept because it has unknown assessment
            }
            
            String assessmentExplanation = condition.getAssessmentExplanation();
            concept.getAssessment().addAssessmentExplanation(assessmentExplanation);
        }
        concept.getAssessment().addAssessedTeamOrgEntries(overallLearnerAssessment);

        // update current assessment value
        AssessmentLevelEnum newLevel = getAssessmentFromScore(weightToConsider, aggregateAssessmentLevelScore);        

        String oldAssessment = concept.getAssessment().toString();

        //always apply the performance assessment value even if the same as before to allow
        //assessments of the same value to occur back to back.  A check should be made in Task/Concept.java for whether
        //an update is warranted based on updated assessments being activated
        concept.getAssessment().updateAssessment(newLevel);

        if(holdValue != null){
            concept.getAssessment().setAssessmentHold(holdValue);
        }

        if(logger.isDebugEnabled()){
            StringBuffer sb = new StringBuffer("Concept ");
            sb.append(concept.getName()).append(" assessment changed from\n").append(oldAssessment).append(", to\n");
            sb.append(newLevel).append("  (score = ").append(aggregateAssessmentLevelScore).append(")");
            logger.debug(sb.toString());
        }

        //(see comment a few lines up)
        return true;
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
    private double getWeight(UUID childConceptOrConditionId){
        
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
        return "[DefaultPerformanceMetric]";
    }
}
