/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.priority;

import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;

/**
 * Used to set the priority of a performance node (i.e. task/concept)
 * 
 * Note: an empty constructor is needed to instantiate an implementation class
 * 
 * @author mhoffman
 *
 */
public interface PriorityMetricInterface {

    /**
     * Determine and possibly set the priority of the node provided.
     * 
     * @param node the node to calculate and possibly set the priority value for
     * @param assessmentProxy the proxy for the node's assessment values such as priority
     * @param holdValue whether to hold the value and not let GIFT change it without external over-ride
     * When null the value is not changed from the current value.
     * @return true if the priority was changed on the node
     */
    public boolean setPriority(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue);
}
