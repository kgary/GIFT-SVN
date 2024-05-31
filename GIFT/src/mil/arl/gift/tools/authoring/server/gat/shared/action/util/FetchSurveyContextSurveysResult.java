/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.List;

import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Gets a list of surveys from a survey context.
 * 
 * @author bzahid
 */
public class FetchSurveyContextSurveysResult extends GatServiceResult {
	
	/** The list of surveys from a survey context. */
	private List<SurveyContextSurvey> surveys;
	
	/**
	 * Class constructor for serialization.
	 */
	public FetchSurveyContextSurveysResult() {
		super();
	}
	
	/**
	 * Initializes a new fetch surveys result.
	 * @param surveys A list of surveys from a survey context.
	 */
	public FetchSurveyContextSurveysResult(List<SurveyContextSurvey> surveys) {
		super();
		this.surveys = surveys;
	}
	
	/**
	 * Gets the surveys from a survey context.
	 * 
	 * @return a list of surveys.
	 */
	public List<SurveyContextSurvey> getSurveys() {
		return surveys;
	}
	
	/**
	 * Set the surveys from a survey context.
	 * 
	 * @param list a list of surveys.
	 */
	public void setSurveys(List<SurveyContextSurvey> list) {
		this.surveys = list;
	}
}
