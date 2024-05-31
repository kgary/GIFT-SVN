/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.difficulty;

import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.ProxyTaskAssessment;

/**
 * The default difficulty metric calculation simply adds the provided difficulty value being applied to the task's
 * current difficulty value.
 * 
 * @author mhoffman
 *
 */
public class DefaultDifficultyMetric implements DifficultyMetricInterface {

    @Override
    public boolean setDifficulty(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, 
            String strategyName, Double strategyDifficulty) {
        
        if(node instanceof Task){
            return setTaskDifficulty((Task)node, assessmentProxy, strategyName, strategyDifficulty);
        }
        
        // difficulty only set on Task
        return false;
    }
    
    /**
     * Calculate the new task difficulty value using the newly added difficulty value.
     * @param task the task needing a difficulty value calculation.  Can't be null.
     * @param assessmentProxy the task assessment proxy where assessments are managed during dkf execution.  Can't be null.
     * @param strategyName name of the strategy being applied that is causing this task's difficulty value to be recalculated.
     * Can't be null or empty.
     * @param strategyDifficulty that strategy's difficulty value to apply to this task.  If null, nothing happens.
     * @return true if the task difficulty level changed
     */
    private boolean setTaskDifficulty(Task task, AssessmentProxy assessmentProxy, String strategyName, Double strategyDifficulty) {
        
        if(strategyDifficulty == null || strategyDifficulty == 0.0) {
            // no change in task difficulty value
            return false;
        }
        
        ProxyTaskAssessment taskAssessment = task.getAssessment();
        
        if(taskAssessment.getDifficulty() == null) {
            taskAssessment.setDifficulty(strategyDifficulty);
        }else {
            taskAssessment.setDifficulty(taskAssessment.getDifficulty() + strategyDifficulty);
        }
        
        String difficultyReason = taskAssessment.getDifficultyReason();
        if(difficultyReason == null) {
            difficultyReason = new String();
        }
        
        StringBuilder difficultyReasonSB = new StringBuilder(difficultyReason);
        if(!difficultyReason.isEmpty()) {
            // space between each sentence
            difficultyReasonSB.append(" "); 
        }
        difficultyReasonSB.append("'").append(strategyName).append("' applied ").append(strategyDifficulty).append(" difficulty.");
        taskAssessment.setDifficultyReason(difficultyReasonSB.toString());

        return true;
    }

}
