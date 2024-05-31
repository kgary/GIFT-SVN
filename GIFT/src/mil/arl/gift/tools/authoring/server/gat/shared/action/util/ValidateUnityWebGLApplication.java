/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action that validates Unity WebGL application files
 * 
 * @author nroberts
 */
public class ValidateUnityWebGLApplication implements Action<GatServiceResult>{

	/** The path to the Unity application's index.html file */
	private String filePath;
	
	/** The user name */
	private String userName;
	
    /**
     * Class constructor.
     */
	public ValidateUnityWebGLApplication() {
	}
	
	/**
	 * Creates an action to validate Unity WebGL application files
	 * 
	 * @param userName The user name
	 * @param filePath The path to the Unity application's index.html file
	 * @param updateInvalidFiles Whether or not to update any invalid files.
	 */
	public ValidateUnityWebGLApplication(String userName, String filePath) {
		this.userName = userName;
		this.filePath = filePath;
	}

	/**
	 * Retrieves the path to the Unity application's index.html file
	 * 
	 * @return the path to the Unity application's index.html file
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Sets the path to the Unity application's index.html file
	 * 
	 * @param filePath the path to the Unity application's index.html file
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
        sb.append("[ValidateUnityWebGLApplication: ");
        sb.append("userName = ").append(userName);
        sb.append(", filePath = ").append(filePath);
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
