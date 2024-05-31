/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Result containing a survey context.
 * 
 * @author bzahid
 */
public class FetchSurveyContextByIdResult extends GatServiceResult {
	
	/** The survey context. */
	private SurveyContext surveyContext;
	
	/**
	 * Class constructor.
	 */
	public FetchSurveyContextByIdResult() {
		super();
	}

	/**
	 * Gets the survey context.
	 * 
	 * @return the survey context.
	 */
	public SurveyContext getSurveyContext() {
		return surveyContext;
	}

	/**
	 * Sets the survey context.
	 * 
	 * @param surveyContext the survey context to set.
	 */
	public void setSurveyContext(SurveyContext surveyContext) {
		this.surveyContext = surveyContext;
	}
	
	
}
