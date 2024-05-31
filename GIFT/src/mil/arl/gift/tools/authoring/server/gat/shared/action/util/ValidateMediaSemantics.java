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
 * Action that validates media semantics files
 * 
 * @author bzahid
 */
public class ValidateMediaSemantics implements Action<ValidateMediaSemanticsResult>{

	/** The path to the avatar html file */
	private String filePath;
	
	/** The user name */
	private String userName;
	
	/** Whether or not to update any invalid files */
	private boolean updateInvalidFiles = false;
	
    /**
     * Class constructor.
     */
	public ValidateMediaSemantics() {
	}
	
	/**
	 * Creates an action to validate media semantics files.
	 * 
	 * @param userName The user name
	 * @param filePath The path to the avatar html file
	 * @param updateInvalidFiles Whether or not to update any invalid files.
	 */
	public ValidateMediaSemantics(String userName, String filePath, boolean updateInvalidFiles) {
		this.userName = userName;
		this.filePath = filePath;
		this.updateInvalidFiles = updateInvalidFiles;
	}

	/**
	 * Retrieves the avatar file path.
	 * 
	 * @return the path to the avatar html file.
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Sets the avatar file path.
	 * 
	 * @param filePath path to the avatar html file.
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Returns whether or not invalid files should be updated.
	 *
	 * @return true if invalid files should be updated, false otherwise.
	 */
	public boolean shouldUpdateInvalidFiles() {
		return updateInvalidFiles;
	}

	/**
	 * Sets whether or not invalid files should be updated.
	 *
	 * @param updateInvalidFiles true if invalid files should be updated, false otherwise.
	 */
	public void setUpdateInvalidFiles(boolean updateInvalidFiles) {
		this.updateInvalidFiles = updateInvalidFiles;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param userName The user name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Gets the user name.
	 * 
	 * @return The user name.
	 */
	public String getUserName() {
		return userName;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ValidateMediaSemantics: ");
        sb.append("filePath = ").append(userName);
        sb.append(", userName = ").append(userName);
        sb.append(", updateInvalidFiles = ").append(updateInvalidFiles);
        sb.append("]");

        return sb.toString();
    } 
}
