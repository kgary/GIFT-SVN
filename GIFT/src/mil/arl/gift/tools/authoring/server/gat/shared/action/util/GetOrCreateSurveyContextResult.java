/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Result containing a survey context.
 * 
 * @author nroberts
 */
public class GetOrCreateSurveyContextResult extends GatServiceResult {
	
	/** The survey context. */
	private Integer surveyContextId;
	
	/**
	 * Class constructor.
	 */
	public GetOrCreateSurveyContextResult() {
		super();
	}

	/**
	 * Gets the survey context.
	 * 
	 * @return the survey context.
	 */
	public Integer getSurveyContext() {
		return surveyContextId;
	}

	/**
	 * Sets the survey context.
	 * 
	 * @param surveyContext the survey context to set.
	 */
	public void setSurveyContext(Integer surveyContextId) {
		this.surveyContextId = surveyContextId;
	}
	
	
}
