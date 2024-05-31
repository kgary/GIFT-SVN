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
 * Class that represents the request to get the lti properties for a course.
 * 
 * @author nblomberg
 *
 */
public class FetchLtiProperties implements Action<FetchLtiPropertiesResult> {
	
 
    /** The relative path to the course. */
	private String courseFilePath;
	
	/**
	 * used to determine if the user has access to the properties
	 */
	private String userName = "";
	
    /**
     * No-arg constructor. Needed for serialization.
     */
    public FetchLtiProperties() {
    }
    

    /**
     * Constructor 
     * 
     * @param courseFilePath The relative path to the course.
     * @param username The name of the user making the request.
     */
    public FetchLtiProperties(String courseFilePath, String username){
        
        setCourseFilePath(courseFilePath);
        setUserName(username);
    }
    
    /**
     * Sets the relative path to the course.
     * 
     * @param path The relative path to the course.
     */
    private void setCourseFilePath(String path){
        
        if(path == null || path.isEmpty()){
            throw new IllegalArgumentException("The path to the course file can't be null or empty.");
        }
    	this.courseFilePath = path;
    }
    
    /**
     * Return the relative path to the course file
     *  
     * @return won't be null or empty.
     */
    public String getCourseFilePath(){
    	return courseFilePath;
    }

    /**
     * Sets the name of the user making the request.
     * 
     * @param name The name of the user making the request.
     */
    private void setUserName(String name) {
        
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }
        userName = name;        
    }
    
    /**
     * Return the username used to determine if the user has access to the lti properties.
     * 
     * @return won't be null or empty.
     */
    public String getUserName(){
        return userName;
    }

   

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchLtiProperties: ");
        builder.append("courseFilePath=");
        builder.append(courseFilePath);
        builder.append(", userName=");
        builder.append(userName);
        builder.append("]");
        return builder.toString();
    }
}
