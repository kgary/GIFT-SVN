/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * Contains information about a condition that has additional logic to assess itself for
 * a performance assessment node.
 * 
 * @author mhoffman
 *
 */
public class ConditionLessonAssessment extends AbstractLessonAssessment {

    @Override
    public AssessmentLevelEnum getAssessment(Object surveyResult) {
        return null;
    }

    //TODO: in the future this will probably contain information about a specific condition that can be assessed for a concept
}
