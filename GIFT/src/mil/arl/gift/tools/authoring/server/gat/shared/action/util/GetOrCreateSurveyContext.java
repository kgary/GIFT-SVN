/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.math.BigInteger;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for retrieving a survey context with a specified id. If no such survey context exists, a new one will be created.
 * 
 * @author nroberts
 */
public class GetOrCreateSurveyContext implements Action<GetOrCreateSurveyContextResult>{
	
	/**
	 * The username for the user making the request. Used to assign permissions when a new survey context is made.
	 */
	private String username;

	/**
	 * The id of the survey context to retrieve.
	 */
	private Integer surveyContextId;
	
	/**
	 * The name of the course that the survey context is being retrieved for. Used for display purposes only.
	 */
	private String courseName;
	
	/**
	 * Class constructor.
	 */
	public GetOrCreateSurveyContext() {
		
	}
	
	/**
	 * Creates an action to get or create a survey context.
	 * 
	 * @param username the username for the user making the request. Used to assign permissions when a new survey context is made.
	 * @param courseName the name of the course that the survey context is being retrieved for. Used for display purposes only.
	 * @param surveyContextId The id of the survey context to get.
	 */
	public GetOrCreateSurveyContext(String username, String courseName, BigInteger surveyContextId) {
		
		this.setUsername(username);
		
		this.setCourseName(courseName);
		
		if(surveyContextId != null){
			this.surveyContextId = surveyContextId.intValue();
			
		} else {
			this.surveyContextId = null;
		}
	}

	/**
	 * Gets the survey context id.
	 * 
	 * @return the id of the survey context.
	 */
	public Integer getSurveyContextId() {
		return surveyContextId;
	}

	/**
	 * Sets the survey context id.
	 * 
	 * @param surveyContextId the id of the survey context.
	 */
	public void setSurveyContextId(Integer surveyContextId) {
		this.surveyContextId = surveyContextId;
	}

	/**
	 * Gets the name of the course that the survey context is being retrieved for. Used for display purposes only.
	 * 
	 * @return the name of the course
	 */
	public String getCourseName() {
		return courseName;
	}

	/**
	 * Sets the name of the course that the survey context is being retrieved for. Used for display purposes only.
	 * 
	 * @param courseName the name of the course
	 */
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	/**
	 * Gets the username for the user making the request. Used to assign permissions when a new survey context is made.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username for the user making the request. Used to assign permissions when a new survey context is made.
	 * 
	 * @param username the username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GetOrCreateSurveyContext: ");
        sb.append("username = ").append(username);
        sb.append(", surveyContextId = ").append(surveyContextId);
        sb.append(", courseName = ").append(courseName);
        sb.append("]");

        return sb.toString();
    } 
}
