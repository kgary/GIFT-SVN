/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.survey.SurveyResponse;

/**
 * This interface is used to provide the results of a survey
 *
 * @author mhoffman
 */
public interface SurveyResultListener {

    /**
     * Notification that a survey has been completed
     *
     * @param surveyResponse - the survey result contents
     */
    void surveyCompleted(SurveyResponse surveyResponse);
}
