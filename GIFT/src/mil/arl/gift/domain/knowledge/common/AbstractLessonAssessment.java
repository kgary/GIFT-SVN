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
 * This is the base class for lesson assessments (e.g. survey lesson assessment for a concept).
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractLessonAssessment {

    /** 
     * arbitrary scores that can be used to give a score for each survey response 
     * based on authored response:assessment pairs in the DKF 
     */
    protected static final double ABOVE_EXPECTATION_SCORE = 2;
    protected static final int AT_EXPECTATION_SCORE = 1;
    protected static final int BELOW_EXPECTATION_SCORE = -2;
    protected static final double UNKNOWN_SCORE = 0.25;
    
    /**
     * Return the assessment level associated with the learner providing the
     * given replies for a particular survey lesson assessment.
     *
     * @param surveyResult - response to a survey
     * @return AssessmentLevelEnum
     */
    public abstract AssessmentLevelEnum getAssessment(Object surveyResult);
}
