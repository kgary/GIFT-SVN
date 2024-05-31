/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.math.BigInteger;

import mil.arl.gift.common.util.StringUtils;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for deleting a survey from a survey context
 * 
 * @author nroberts
 */
public class DeleteSurvey implements Action<DeleteSurveyContextResult>{
	
	/**
	 * The username for the user making the request. Used to check permissions.
	 */
	private String username;

	/**
	 * The ID of the survey context to delete from
	 */
	private int surveyContextId;
	
	/**
	 * The GIFT key for the survey to delete
	 */
	private String surveyKey;
	
	/**
	 * The unique id of the survey to delete
	 */
	private int surveyId;
	
	/**
	 * Whether any learner responses to the survey context or its elements should also be deleted
	 */
	private boolean deleteResponses = false;
	
	/**
	 * Whether or not to bypass permission checks when deleting the survey
	 */
	private boolean bypassPermissionCheck = false;
	
	/** The path of the directory containing the survey export file that maintains this survey. This should only be populated if coming from GIFT Wrap. */
    private String surveyCourseFolderPath;

	/**
	 * Class constructor.
	 * 
	 * Required for GWT serilization.  DONT USE!!!
	 */
	public DeleteSurvey() {
		
	}
	
	/**
	 * Creates an action to delete the survey across all survey contexts
	 * 
	 * @param username the username for the user making the request. Used to check permissions when deleting the survey and survey context references to that survey.
	 * @param surveyId the unique id of a survey to delete.  Must be a valid survey id in the database.  A value less than
     * or equal to zero will cause an illegalArgumentException.
	 */
	public DeleteSurvey(String username, int surveyId){
	    setUsername(username);
	    setSurveyId(surveyId);
	}
	
	/**
	 * Creates an action to delete the survey in the survey context.
	 * 
	 * @param username the username for the user making the request. Used to check permissions when deleting the survey and survey context references to that survey.
	 * @param surveyKey the GIFT key of the survey to delete
	 * @param surveyContextId The ID of the survey context to delete from.  Can't be null. Must be a valid survey id in the database.  A value less than
     * or equal to zero will cause an illegalArgumentException.
	 */
	public DeleteSurvey(String username, String surveyKey, BigInteger surveyContextId) {
	    setUsername(username);
	    setSurveyKey(surveyKey);
	    setSurveyContextId(surveyContextId.intValue());
	}
	
    /**
     * Creates an action to delete the survey in the survey context from a survey export file.
     * 
     * @param username the username for the user making the request. Used to check permissions when
     *        deleting the survey and survey context references to that survey. Can't be null.
     * @param surveyKey the GIFT key of the survey to delete. Can't be null.
     * @param surveyCourseFolderPath The relative path to the course folder containing the survey
     *        export file. The path should be the subfolder of workspace (e.g.
     *        Public/TrainingAppsLib/...). Can't be null.
     */
    public DeleteSurvey(String username, String surveyKey, String surveyCourseFolderPath) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        } else if (StringUtils.isBlank(surveyKey)) {
            throw new IllegalArgumentException("The parameter 'surveyKey' cannot be blank.");
        } else if (StringUtils.isBlank(surveyCourseFolderPath)) {
            throw new IllegalArgumentException("The parameter 'surveyCourseFolderPath' cannot be blank.");
        }

        setUsername(username);
        setSurveyKey(surveyKey);
        setSurveyCourseFolderPath(surveyCourseFolderPath);
    }

	/**
	 * Creates an action to delete the survey context.
	 * 
	 * @param username the username for the user making the request. Used to check permissions when deleting the survey and survey context references to that survey.
	 * @param surveyContextId The id of the survey context to delete.  Can't be null. Must be a valid survey id in the database.  A value less than
     * or equal to zero will cause an illegalArgumentException.
	 * @param deleteResponses whether any learner responses to the survey context or its elements should also be deleted
	 * @param bypassPermissionCheck whether or not to bypass permission checks
	 */
	public DeleteSurvey(String username, String surveyKey, BigInteger surveyContextId, boolean deleteResponses, boolean bypassPermissionCheck) {
	    this(username, surveyKey, surveyContextId);
		setDeleteResponses(deleteResponses);
		setBypassPermissionCheck(bypassPermissionCheck);
	}
	
	/**
	 * Return the unique id of the survey to delete.
	 * 
	 * @return the unique id of the survey to delete.</br>
	 * Note: this will be zero if the request is to delete the survey in a specific survey context.  In this case the
	 * survey context id will be greater than zero.
	 */
	public int getSurveyId(){
	    return surveyId;
	}
	
	/**
	 * Set the survey id to delete.
	 * 
	 * @param surveyId the unique id of a survey to delete.  Must be a valid survey id in the database.  A value less than
	 * or equal to zero will cause an illegalArgumentException.
	 */
	private void setSurveyId(int surveyId){
	    
	    if(surveyId <= 0){
	        throw new IllegalArgumentException("The survey id of "+surveyId+" is not a valid survey id.");
	    }
	    
	    this.surveyId = surveyId;
	}

	/**
	 * Gets the survey context id.
	 * 
	 * @return the id of the survey context.</br>
	 * Note: this will be zero if the request is to delete the survey across all survey contexts.  In this case the
     * survey id will be greater than zero.
	 */
	public int getSurveyContextId() {
		return surveyContextId;
	}

	/**
	 * Sets the survey context id.
	 * 
	 * @param surveyContextId the id of the survey context. Must be a valid survey id in the database.  A value less than
     * or equal to zero will cause an illegalArgumentException.
	 */
	private void setSurveyContextId(int surveyContextId) {
	    
        if(surveyContextId <= 0){
            throw new IllegalArgumentException("The survey context id of "+surveyContextId+" is not a valid survey id.");
        }
	       
		this.surveyContextId = surveyContextId;
	}
	
	/**
	 * Gets the username for the user making the request. Used to check permissions when a new survey context is deleted.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

    /**
     * Gets the course folder path that contains the survey export file.
     * 
     * @return the survey export file's course folder path. Can be null if never set.
     */
    public String getSurveyCourseFolderPath() {
        return surveyCourseFolderPath;
    }

    /**
	 * Sets the username for the user making the request. Used to check permissions when a new survey context is deleted.
	 * 
	 * @param username the username
	 */
	private void setUsername(String username) {
		this.username = username;
    }

    /**
     * Sets the course folder path for the survey export file.
     * 
     * @param surveyCourseFolderPath the survey export file's course folder path
     */
    public void setSurveyCourseFolderPath(String surveyCourseFolderPath) {
        this.surveyCourseFolderPath = surveyCourseFolderPath;
    }

    /**
	 * Gets whether any learner responses to the survey context or its elements should also be deleted
	 * 
	 * @return whether to delete any responses
	 */
	public boolean shouldDeleteResponses() {
		return deleteResponses;
	}

	/**
	 * Sets whether any learner responses to the survey context or its elements should also be deleted
	 * 
	 * @param deleteResponses whether to delete any responses
	 */
	public void setDeleteResponses(boolean deleteResponses) {
		this.deleteResponses = deleteResponses;
	}

	/**
	 * Gets the GIFT key for the survey to delete
	 * 
	 * @return the GIFT key for the survey to delete</br>
	 * Note: this will be null if the request is to delete the survey across all survey contexts.  In this case the
     * survey id will be greater than zero.
	 */
	public String getSurveyKey() {
		return surveyKey;
	}

	/**
	 * Sets the GIFT key for the survey to delete
	 * 
	 * @param surveyKey the GIFT key for the survey to delete
	 */
	private void setSurveyKey(String surveyKey) {
		this.surveyKey = surveyKey;
	}
	
	/**
	 * gets whether or not to bypass permission checks
	 * 
	 * @return whether or not to bypass permission checks
	 */
	public boolean getBypassPermissionCheck(){
		return bypassPermissionCheck;
	}
	
	/**
	 * Sets whether or not to bypass permission checks
	 * 
	 * @param bypass whether or not to bypass permission checks
	 */
	public void setBypassPermissionCheck(boolean bypass){
		bypassPermissionCheck = bypass;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DeleteSurvey: ");
        sb.append("username = ").append(username);
        
        if(surveyContextId > 0){
            sb.append(", surveyContextId = ").append(surveyContextId);
            sb.append(", surveyKey = ").append(surveyKey);
        }else{
            sb.append(", surveyId = ").append(surveyId);
        }
        
        sb.append(", deleteResponses = ").append(deleteResponses);
        sb.append(", bypassPermissionCheck = ").append(bypassPermissionCheck);
        sb.append(", surveyCourseFolder = ").append(getSurveyCourseFolderPath());
        sb.append("]");

        return sb.toString();
    } 
}
