/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.survey.Survey;

/**
 * The result of querying for a survey
 *
 * @author jleonard
 */
public class SurveyReturnResult extends RpcResponse {

    private Survey survey;

    /**
     * Default Constructor
     *
     * Required to exist and be public for IsSerializable
     */
    public SurveyReturnResult() {
    }

    /**
     * Constructor
     *
     * @param survey The survey to return
     */
    public SurveyReturnResult(Survey survey) {
        this.survey = survey;
    }

    /**
     * Gets the survey that was returned
     *
     * @return Survey The survey that was returned
     */
    public Survey getSurvey() {

        return survey;
    }

}
