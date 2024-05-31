/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.assessment;

import java.util.Map;
import java.util.UUID;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;

/**
 * Used to set the performance assessment of a performance node (i.e. task/concept)
 * 
 * Note: an empty constructor is needed to instantiate an implementation class
 * 
 * @author mhoffman
 *
 */
public interface PerformanceMetricInterface {

    /**
     * Determine and possibly set the performance assessment of the node provided.
     * 
     * @param node the node to calculate and possibly set the performance assessment value for
     * @param assessmentProxy the proxy for the node's assessment values such as performance assessment
     * @param holdValue whether to hold the value and not let GIFT change it without external over-ride.
     * When null the value is not changed from the current value.
     * @return true if the performance assessment was changed on the node
     */
    public boolean setPerformance(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue);
    
    /**
     * Set the mapping of child concept or condition unique course id to the authored performance metric arguments
     * for those objects.  Can be null, empty and have null values for an id.
     * @param childConceptOrConditionArgsMap the mapping of performance metric arguments.
     */
    public void setMetricArgsForChildConceptOrCondition(Map<UUID, PerformanceMetricArguments> childConceptOrConditionArgsMap);
}
