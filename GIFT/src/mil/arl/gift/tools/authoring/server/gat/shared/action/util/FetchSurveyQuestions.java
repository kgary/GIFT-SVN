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
 * Action for retrieving questions from a survey
 * 
 * @author bzahid
 */
public class FetchSurveyQuestions implements Action<FetchSurveyQuestionsResult> {

	/** The survey context containing the survey. */
	private int surveyContextId;
	
	/** The label of the survey to retrieve. */
	private String giftKey;
	
	/** The username of the user for whom the request is being made */
	private String username;
	
	/**
	 * Class constructor.
	 */
	public FetchSurveyQuestions() {
		
	}
	
	/**
	 * Initializes a new action for retrieving survey questions.
	 * 
	 * @param surveyContextId The survey context containing the survey.
	 * @param giftKey The label of the survey to get questions from 
	 * @param username The username of the user for whom the request is being made
	 */
	public FetchSurveyQuestions(int surveyContextId, String giftKey, String username) {
		this.surveyContextId = surveyContextId;
		this.giftKey = giftKey;
		this.username = username;
	}

	/**
	 * Gets the survey context id.
	 * 
	 * @return The survey context id containing the survey.
	 */
	public int getSurveyContextId() {
		return surveyContextId;
	}

	/**
	 * Sets the survey context id.
	 * 
	 * @param surveyContextId the survey context id containing the survey.
	 */
	public void setSurveyContextId(int surveyContextId) {
		this.surveyContextId = surveyContextId;
	}

	/**
	 * Gets the label of the survey to retrieve.
	 * 
	 * @return The label of the survey to retrieve.
	 */
	public String getGiftKey() {
		return giftKey;
	}

	/**
	 * Sets the label of the survey to retrieve.
	 * 
	 * @param giftKey The label of the survey to retrieve.
	 */
	public void setGiftKey(String giftKey) {
		this.giftKey = giftKey;
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
        sb.append("[FetchSurveyQuestions: ");
        sb.append("surveyContextId = ").append(surveyContextId);
        sb.append(", giftKey = ").append(giftKey);
        sb.append(", username = ").append(username);
        sb.append("]");

        return sb.toString();
    } 
}
