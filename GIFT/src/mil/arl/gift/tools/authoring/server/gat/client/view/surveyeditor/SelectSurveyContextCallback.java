/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import mil.arl.gift.common.survey.Survey;


/**
 * Callback for when the survey context is selected/saved from the survey editor.
 * 
 * @author nblomberg
 *
 */
public interface SelectSurveyContextCallback {

    /**
     * Callback for when the survey context has been selected from the survey editor.
     * 
     * @param surveyContextKey - The survey context key that was selected.  If no context was selected, this can be null or empty.
     * @param surveyContextId - The survey context object that was saved.  
     * @param survey - The survey object that was saved to the context. Can be null or empty if no context was selected.
     */
    void onSurveyContextSelected(String surveyContextKey, int surveyContextId, Survey survey);

}
