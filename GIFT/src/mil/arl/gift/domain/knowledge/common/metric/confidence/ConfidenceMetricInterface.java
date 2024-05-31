/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.confidence;

import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;

/**
 * Used to set the confidence of a performance node (i.e. task/concept)
 * 
 * Note: an empty constructor is needed to instantiate an implementation class
 * 
 * @author mhoffman
 *
 */
public interface ConfidenceMetricInterface {

    /**
     * Determine and possibly set the confidence of the node provided.
     * 
     * @param node the node to calculate and possibly set the confidence value for
     * @param assessmentProxy the proxy for the node's assessment values such as confidence
     * @param holdValue whether to hold the value and not let GIFT change it without external over-ride
     * If null the value will remain the same.
     * @return true if the confidence was changed on the node
     */
    public boolean setConfidence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue);
}
