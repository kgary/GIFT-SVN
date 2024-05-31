/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Used to request the course history information for a course and survey context.
 * 
 * @author mhoffman
 *
 */
public class FetchCourseHistory implements Action<FetchCourseHistoryResult> {
	
    /** 
     * the workspace path to the course folder 
     *  e.g. jsmith/my first course, Public/Hemorrhage Control
     */
	private String courseFolderPath;
	
	/**
	 * used to determine if the user has read access to the file
	 */
	private String userName = "";
	
	/**
	 * the unique survey context id for a course 
	 */
	private Integer surveyContextId = null;
	
    /**
     * No-arg constructor. Needed for serialization.
     */
    public FetchCourseHistory() {
    }
    
    /**
     * Set the attributes of this request.
     * 
     * @param username  used to determine if the user has read access to the file.  Can't be null or empty.
     * @param courseFolderPath  the workspace path to the course folder the content file is located in  Can be null or empty if the
     * survey context id is provided and only the survey context id history is desired. 
     * e.g. jsmith/my first course, Public/Hemorrhage Control
     * @param surveyContextId the unique survey context id for a course.  Can be null if the course folder path is
     * provided and only the course folder history is desired.
     */
    public FetchCourseHistory(String username, String courseFolderPath, Integer surveyContextId){
        
        setUserName(username);
        
        if(courseFolderPath == null || courseFolderPath.isEmpty()){
            
            if(surveyContextId == null){
                throw new IllegalArgumentException("The path to the course folder AND/OR the survey context id must be provided.");
            }
        }
        
        setCourseFolderPath(courseFolderPath);
        setSurveyContextId(surveyContextId);
    }

    private void setUserName(String name) {
        
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }
        userName = name;        
    }
    
    /**
     * Return the username used to determine if the user has read access to the survey and course.
     * 
     * @return won't be null or empty.
     */
    public String getUserName(){
        return userName;
    }

    private void setCourseFolderPath(String courseFolderPath) {
		this.courseFolderPath = courseFolderPath;
	}
	
    /**
     * Return the workspace path to the course folder
     * 
     * @return can be null if the course folder history is not desired and the survey context id is provided.
     */
	public String getCourseFolderPath() {
		return courseFolderPath;
	}
	
	/**
	 * The unique survey context id for a course.
	 * 
	 * @return can be null if the survey context history is not desired and the course folder path is provided.
	 */
    public Integer getSurveyContextId() {
        return surveyContextId;
    }

    private void setSurveyContextId(Integer surveyContextId) {
        this.surveyContextId = surveyContextId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchCourseHistory: courseFolderPath=");
        builder.append(courseFolderPath);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", surveyContextId = ");
        builder.append(surveyContextId);
        builder.append("]");
        return builder.toString();
    }


}
