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
 * Action that converts a PowerPoint file into a series of images
 * 
 * @author bzahid
 */
public class CreateSlideShow implements Action<CreateSlideShowResult> {
	
	private String pptFilePath;
	
	private String courseFolderPath;
	
	private String courseObjectName;
	
	private String username;
	
	private String browserSessionKey;
	
	private boolean copyExistingSlideShow;
	
	private boolean replaceExisting;
	
	/**
	 * Class constructor for serialization
	 */
	public CreateSlideShow() {
		
	}

	/**
	 * Sets the path to the course folder
	 * 
	 * @param courseFolderPath The path to the course folder
	 */
	public void setCourseFolderPath(String courseFolderPath) {
		this.courseFolderPath = courseFolderPath;
	}
	
	/**
	 * Gets the path to the course folder
	 * 
	 * @return The path to the course folder
	 */
	public String getCourseFolderPath() {
		return courseFolderPath;
	}
	
	/**
	 * Sets the path to the PowerPoint file
	 * 
	 * @param pptFilePath The path to the PowerPoint file
	 */
	public void setPptFilePath(String pptFilePath) {
		this.pptFilePath = pptFilePath;
	}
	
	/**
	 * Gets the path to the PowerPoint file
	 * 
	 * @return The path to the PowerPoint file
	 */
	public String getPptFilePath() {
		return pptFilePath;
	}
	
	/**
	 * Sets the name of the lesson material course object. This name will be used to create a sub-folder 
	 * in the "Slide Shows" folder for the PowerPoint images.
	 * 
	 * @param courseObjectName The name of the course object.
	 */
	public void setCourseObjectName(String courseObjectName) {
		this.courseObjectName = courseObjectName;
	}
	
	/**
	 * Gets the name of the lesson material course object
	 * 
	 * @return The name of the lesson material course object
	 */
	public String getCourseObjectName() {
		return courseObjectName;
	}
	
	/**
	 * Sets the user name 
	 * 
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Gets the user name
	 * 
	 * @return The username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Gets the unique identifier of the browser making the request
	 * @return the value of the browser session key, can be null
	 */
	public String getBrowserSessionKey() {
        return browserSessionKey;
    }
	
	/**
	 * Sets the unique identifier of the browser making the request
	 * @param browserSessionKey the new value of the browser session key, can be null
	 */
	public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }
	
	/**
	 * Sets whether or not an existing slide show is being copied
	 * 
	 * @param copyExisting true if an existing slide show is being copied, false if a powerpoint file is being copied
	 */
	public void setCopyExistingSlideShow(boolean copyExisting) {
		this.copyExistingSlideShow = copyExisting;
	}
	
	/**
	 * Gets whether or not an existing slide show is being copied
	 * 
	 * @return true if an existing slide show is being copied, false if a powerpoint file is being copied
	 */
	public boolean shouldCopyExistingSlideShow() {
		return copyExistingSlideShow;
	}
	
	/**
	 * Sets whether or not the existing Slide Show folder should be replaced with the new one.
	 * 
	 * @param replaceExisting true if the folder should be replaced, false otherwise
	 */
	public void setReplaceExisting(boolean replaceExisting) {
		this.replaceExisting = replaceExisting;
	}
	
	/**
	 * Gets whether or not the existing Slide Show folder should be replaced with the new one.
	 * 
	 * @return true if the folder should be replaced, false otherwise
	 */
	public boolean shouldReplaceExisting() {
		return replaceExisting;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[CreateSlideShow: ");
        sb.append("pptFilePath = ").append(pptFilePath);
        sb.append(", courseFolderPath = ").append(courseFolderPath);
        sb.append(", courseObjectName = ").append(courseObjectName);
        sb.append(", username = ").append(username);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append(", copyExistingSlideShow = ").append(copyExistingSlideShow);
        sb.append(", replaceExisting = ").append(replaceExisting);
        sb.append("]");

        return sb.toString();
    } 
}
