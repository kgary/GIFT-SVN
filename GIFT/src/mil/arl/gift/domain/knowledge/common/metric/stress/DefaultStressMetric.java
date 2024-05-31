/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.stress;

import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.ProxyTaskAssessment;

/**
 * The default stress metric calculation simply adds the provided stress value being applied to the task's
 * current stress value.
 * @author mhoffman
 *
 */
public class DefaultStressMetric implements StressMetricInterface {

    @Override
    public boolean setStress(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy,
            String strategyName, Double additionalStress) {
        
        if(node instanceof Task){            
            return setTaskStress((Task)node, assessmentProxy, strategyName, additionalStress);
        }
        
        // stress only set on Task
        return false;
    }

    /**
     * Calculate the new task stress value using the newly added stress value.
     * @param task the task needing the stress value updated.  Can't be null.
     * @param assessmentProxy the task assessment proxy where assessments are managed during dkf execution.  Can't be null.
     * @param strategyName the name of the strategy being applied.  Useful for auto generating a reason message on
     * why the stress value changed.  Can't be null.
     * @param additionalStress the stress added because the strategy was applied to this task.  If null or zero,
     * this method just returns false.
     * @return true if the task stress value changed
     */
    private boolean setTaskStress(Task task, AssessmentProxy assessmentProxy,
            String strategyName, Double additionalStress) {
        
        if(additionalStress == null || additionalStress == 0.0) {
            // no change in task stress value
            return false;
        }
        
        ProxyTaskAssessment taskAssessment = task.getAssessment();
        
        if(taskAssessment.getStress() == null) {
            taskAssessment.setStress(additionalStress);
        }else {
            taskAssessment.setStress(taskAssessment.getStress() + additionalStress);
        }
        
        String stressReason = taskAssessment.getStressReason();
        if(stressReason == null) {
            stressReason = new String();
        }
        
        StringBuilder stressReasonSB = new StringBuilder(stressReason);
        if(!stressReason.isEmpty()) {
            // space between each sentence
            stressReasonSB.append(" "); 
        }
        stressReasonSB.append("'").append(strategyName).append("' applied ").append(additionalStress).append(" stress.");
        taskAssessment.setStressReason(stressReasonSB.toString());
        
        return true;
    }
    
}
