/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.survey.SurveyResponse;

/**
 * This interface is used to provide the assessment results of responses to a survey
 *
 * @author mhoffman
 */
public interface SurveyResponseAssessmentListener {

    /**
     * Notification that a survey has been completed and the responses have been assessed
     *
     * @param surveyResponse - the survey responses contents
     * @param assessment - the resulting assessment of the responses
     */
    void surveyCompleted(SurveyResponse surveyResponse, AssessmentLevelEnum assessment);

}
