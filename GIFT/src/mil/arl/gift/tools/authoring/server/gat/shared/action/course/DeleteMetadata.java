/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for deleting a metadata file
 * 
 * @author nroberts
 */
public class DeleteMetadata implements Action<GenericGatServiceResult<Void>> {

	/** The name of the user making the request */
	private String username;
	
	/** The unique identifier for the browser making the request */
	private String browserSessionKey;
	
	/** The path to the course folder */
	private String courseFilePath;
	
	/** The path to the metadata file to delete */
	private String metadataFilePath;
	
	/** Whether or not to delete any training app or DKF files referenced by the metadata */
	private boolean isDeepDelete;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public DeleteMetadata() {
    	
    }

    /**
     * Creates a new action for deleting a metadata file
     * 
     * @param username the username of the user making the request
     * @param browserSessionKey the unique identifier of the browser making the request
     * @param courseFilePath the location of the course folder
     * @param metadataFilePath the path to the metadata file to delete
     * @param isDeepDelete whether or not to delete any training app or DKF files referenced by the metadata
     */
    public DeleteMetadata(String username, String browserSessionKey, String courseFilePath, String metadataFilePath, boolean isDeepDelete) {
    	this.username = username;
    	this.browserSessionKey = browserSessionKey;
    	this.courseFilePath = courseFilePath;
    	this.metadataFilePath = metadataFilePath;
    	this.isDeepDelete = isDeepDelete;
    }
    
    /**
     * Gets the name of the user making the request
     * 
     * @return the name of the user making the request
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
	 * Gets the path to the course folder
	 * 
	 * @return the path to the course folder
	 */
	public String getCourseFilePath() {
		return courseFilePath;
	}

	/**
	 * Gets the path to the metadata file to delete
	 * 
	 * @return the path to the metadata file to delete
	 */
	public String getMetadataFilePath() {
		return metadataFilePath;
	}

	/**
	 * Gets whether or not to delete any training app or DKF files referenced by the metadata
	 * 
	 * @return whether or not to delete any training app or DKF files referenced by the metadata
	 */
	public boolean isDeepDelete() {
		return isDeepDelete;
	}
    
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DeleteMetadata: ");
        sb.append("username = ").append(username);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append(", courseFilePath").append(courseFilePath);
        sb.append(", metadataFilePath").append(metadataFilePath);
        sb.append(", isDeepDelete").append(isDeepDelete);
        sb.append("]");

        return sb.toString();
    } 
}
