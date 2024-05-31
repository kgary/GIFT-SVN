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
 * Action for deleting a survey context survey, i.e. a reference to a survey for a specific course.
 * This will not delete the survey or it's responses.
 * 
 * @author mhoffman
 *
 */
public class DeleteCourseSurveyReference implements Action<DeleteSurveyContextResult> {

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
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private DeleteCourseSurveyReference(){}
    
    /**
     * Creates an action to delete the survey context survey in the survey context.
     * In other words, remove the reference to a survey for a course.
     * 
     * @param username the username for the user making the request. Used to check permissions when deleting 
     * the survey and survey context references to that survey.  Can't be null or empty.
     * @param surveyKey the GIFT key of the survey to delete.  Can't be null or empty.
     * @param surveyContextId The ID of the survey context to delete from.  Can't be null. Must be a valid survey id in the database.  A value less than
     * or equal to zero will cause an illegalArgumentException.
     */
    public DeleteCourseSurveyReference(String username, String surveyKey, BigInteger surveyContextId) {
        setUsername(username);
        setSurveyKey(surveyKey);
        setSurveyContextId(surveyContextId.intValue());
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
     * @return the username.  Wont be null or empty.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for the user making the request. Used to check permissions when a new survey context is deleted.
     * 
     * @param username the username.  Can't be null or empty.
     */
    private void setUsername(String username) {
        
        if(username == null || username.isEmpty()){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }
        this.username = username;
    }

    /**
     * Gets the GIFT key for the survey to delete
     * 
     * @return the GIFT key for the survey to delete. Will not be null or empty.
     */
    public String getSurveyKey() {
        return surveyKey;
    }

    /**
     * Sets the GIFT key for the survey to delete
     * 
     * @param surveyKey the GIFT key for the survey to delete.  Can't be null or empty.
     */
    private void setSurveyKey(String surveyKey) {
        this.surveyKey = surveyKey;
    }
    
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DeleteCourseSurveyReference: ");
        sb.append("username = ").append(username);
        sb.append(", surveyContextId = ").append(surveyContextId);
        sb.append(", surveyKey = ").append(surveyKey);
        
        sb.append("]");

        return sb.toString();
    } 
}
