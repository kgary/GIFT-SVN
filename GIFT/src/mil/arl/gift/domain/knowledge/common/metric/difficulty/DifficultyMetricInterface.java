/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.difficulty;

import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;

/**
 * Used to set the difficulty of a performance node (i.e. task)
 * 
 * Note: an empty constructor is needed to instantiate an implementation class
 * 
 * @author mhoffman
 *
 */
public interface DifficultyMetricInterface {

    /**
     * Determine and possibly set the difficulty of the node provided.
     * 
     * @param node the node to calculate and possibly set the difficulty value for. Currently only tasks are supported.
     * @param assessmentProxy the proxy for the node's assessment values such as difficulty
     * @param strategyName name of the strategy being applied that is causing this task's difficulty value to be recalculated.
     * Can't be null or empty.
     * @param strategyDifficulty that strategy's difficulty value to apply to this task.  If null, nothing happens.
     * @return true if the difficulty was changed on the node
     */
    public boolean setDifficulty(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, 
            String strategyName, Double strategyDifficulty);
}
