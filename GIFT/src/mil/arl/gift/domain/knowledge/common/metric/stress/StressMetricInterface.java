/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.stress;

import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;

/**
 * Used to set the stress of a performance node (i.e. task)
 * 
 * Note: an empty constructor is needed to instantiate an implementation class
 * 
 * @author mhoffman
 *
 */
public interface StressMetricInterface {

    /**
     * Determine and possibly set the stress of the node provided.
     * 
     * @param node the node to calculate and possibly set the stress value for. Currently only tasks are supported.
     * @param assessmentProxy the proxy for the node's assessment values such as stress
     * @param strategyName name of the strategy being applied that is causing this task's stress value to be recalculated.
     * Can't be null or empty.
     * @param additionalStress the stressor value being added to the node that will influence changing the nodes stress value
     * @return true if the stress was changed on the node
     */
    public boolean setStress(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, 
            String strategyName, Double additionalStress);
}
