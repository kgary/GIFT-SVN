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
 * Action to retrieve non-xml files from the server
 * 
 * @author bzahid
 */
public class FetchMediaFiles implements Action<FetchMediaFilesResult> {

	/** The username */
	private String userName;
	
	/** The path to the current course folder */
	private String courseFolderPath;

	/**
	 * Class constructor. Needed for serialization.
	 */
	public FetchMediaFiles() {
		
	}
	
	/**
	 * Gets the userName 
	 * 
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the userName
	 * 
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the course folder path
	 * 
	 * @return the course folder path
	 */
	public String getCourseFolderPath() {
		return courseFolderPath;
	}

	/**
	 * Sets the course folder path
	 * 
	 * @param courseFolderPath the path to the current course folder
	 */
	public void setCourseFolderPath(String courseFolderPath) {
		this.courseFolderPath = courseFolderPath;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchMediaFiles: ");
        sb.append("userName = ").append(userName);
        sb.append(", courseFolderPath").append(courseFolderPath);
        sb.append("]");

        return sb.toString();
    } 
}
