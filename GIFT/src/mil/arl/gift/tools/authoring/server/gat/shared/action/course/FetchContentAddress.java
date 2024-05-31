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
 * Used to request the URL of a piece of content in a course folder.
 * 
 * @author mhoffman
 *
 */
public class FetchContentAddress implements Action<FetchContentAddressResult> {
	
    /** 
     * the workspace path to the course folder the content file is located in 
     *  e.g. jsmith/my first course, Public/Hemorrhage Control
     */
	private String courseFolderPath;
	
	/**
	 * the course folder relative path to the file
	 * e.g. bucket controls.png
	 */
	private String contentFilePath;
	
	/**
	 * used to determine if the user has read access to the file
	 */
	private String userName = "";
	
    /**
     * No-arg constructor. Needed for serialization.
     */
    public FetchContentAddress() {
    }
    
    /**
     * Set the attributes of this request.
     * 
     * @param courseFolderPath  the workspace path to the course folder the content file is located in  Can't be null or empty. 
     * e.g. jsmith/my first course, Public/Hemorrhage Control
     * @param contentFilePath the course folder relative path to the file  Can't be null or empty.
     * e.g. bucket controls.png
     * @param username  used to determine if the user has read access to the file.  Can't be null or empty.
     */
    public FetchContentAddress(String courseFolderPath, String contentFilePath, String username){
        
        setCourseFolderPath(courseFolderPath);
        setContentFilePath(contentFilePath);
        setUserName(username);
    }
    
    private void setContentFilePath(String path){
        
        if(path == null || path.isEmpty()){
            throw new IllegalArgumentException("The path to the content file can't be null or empty.");
        }
    	this.contentFilePath = path;
    }
    
    /**
     * Return the course folder relative path to the file
     *  
     * @return won't be null or empty.
     */
    public String getContentFilePath(){
    	return contentFilePath;
    }

    private void setUserName(String name) {
        
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }
        userName = name;        
    }
    
    /**
     * Return the username used to determine if the user has read access to the file.
     * 
     * @return won't be null or empty.
     */
    public String getUserName(){
        return userName;
    }

    private void setCourseFolderPath(String courseFolderPath) {
        
        if(courseFolderPath == null || courseFolderPath.isEmpty()){
            throw new IllegalArgumentException("The path to the course folder can't be null or empty.");
        }
		this.courseFolderPath = courseFolderPath;
	}
	
    /**
     * Return the workspace path to the course folder the content file is located in
     * 
     * @return won't be null or empty.
     */
	public String getCourseFolderPath() {
		return courseFolderPath;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchContentAddress: courseFolderPath=");
        builder.append(courseFolderPath);
        builder.append(", contentFilePath=");
        builder.append(contentFilePath);
        builder.append(", userName=");
        builder.append(userName);
        builder.append("]");
        return builder.toString();
    }
}
