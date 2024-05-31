/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for retrieving surveys from a survey context
 * 
 * @author bzahid
 */
public class FetchSurveyContextSurveys implements Action<FetchSurveyContextSurveysResult>{

	/** The id of the survey context to fetch surveys from. */
	private int surveyContextId;
	
	/** The username of the user for whom the request is being made */
	private String username;
	
	/**
	 * Class constructor for serialization.
	 */
	public FetchSurveyContextSurveys() {
		
	}
	
	/**
	 * Initializes a new action to fetch surveys for a given survey context id.
	 * 
	 * @param surveyContextId  The id of the survey context to fetch surveys from.
	 */
	public FetchSurveyContextSurveys(int surveyContextId) {
		this.surveyContextId = surveyContextId;
	}
	
	/**
	 * Initializes a new action to fetch surveys for a given survey context id for a given user.
	 * 
	 * @param surveyContextId  The id of the survey context to fetch surveys from.
	 * @param username The username of the user for whom the request is being made
	 */
	public FetchSurveyContextSurveys(int surveyContextId, String username) {
		this.surveyContextId = surveyContextId;
		this.username = username;
	}
	
	/** Gets the survey context id. 
	 * @return the survey context id. */
	public int getSurveyContextId() {
		return surveyContextId;
	}
	
	/**
	 * Sets the survey context id to retrieve surveys for.
	 * 
	 * @param surveyContextId The survey context id.
	 */
	public void setSurveyContextId(int surveyContextId) {
		this.surveyContextId = surveyContextId;
	}
	
	/**
	 * Gets the username of the user for whom the request is being made
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Sets the username of the user for whom the request is being made
	 * 
	 * @param the username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchSurveyContextSurveys: ");
        sb.append("surveyContextId = ").append(surveyContextId);
        sb.append(", username = ").append(username);
        sb.append("]");

        return sb.toString();
    } 
}
