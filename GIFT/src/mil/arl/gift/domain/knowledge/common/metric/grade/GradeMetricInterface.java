/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.grade;

import java.util.Map;
import java.util.UUID;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;

/**
 * Used to calculate the overall assessment grade for a particular performance assessment node (i.e. task or concept).
 * 
 * @author mhoffman
 *
 */
public interface GradeMetricInterface {
    
    /**
     * Update the grade for the given assessment node based on the assessment node's child overall assessment values.
     * @param assessmentNode the assessment node to analyze the child overall assessment values for. Can't be null.
     * @param gradeNode where to place the new calculated grade for this assessment node.  Can't be null.
     */
    public void updateGrade(AbstractPerformanceAssessmentNode assessmentNode, GradedScoreNode gradeNode);
    
    /**
     * Set the mapping of child concept or condition unique course id to the authored performance metric arguments
     * for those objects.  Can be null, empty and have null values for an id.
     * @param childConceptOrConditionArgsMap the mapping of performance metric arguments.
     */
    public void setMetricArgsForChildConceptOrCondition(Map<UUID, PerformanceMetricArguments> childConceptOrConditionArgsMap);
}
