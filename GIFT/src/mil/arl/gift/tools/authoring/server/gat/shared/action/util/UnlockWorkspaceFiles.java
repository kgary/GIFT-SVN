/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for locking a group of workspace files.
 * 
 * @author nroberts
 */
public class UnlockWorkspaceFiles implements Action<GatServiceResult> {

	private List<String> pathsToUnlock;
	
	/** The user name. */
	private String userName;
	
	/** The unique identifier of the browser making the request */
    private String browserSessionKey;

	/**
     * No-arg constructor. Needed for serialization.
     */
    public UnlockWorkspaceFiles() {
    }

	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * @param userName User name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
     * Gets the unique identifier of the browser making the request
     * @return the value of the browser session key
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }
    
    /**
     * Sets the unique identifier of the browser making the request
     * @param browserSessionKey the new value of the browser session key
     */
    public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

	/**
	 * Gets the list of paths for the files to unlock
	 * 
	 * @return the list of paths
	 */
	public List<String> getPathsToUnlock() {
		return pathsToUnlock;
	}

	/**
	 * Sets the list of paths for the files to unlock
	 * 
	 * @param pathsToUnlock the list of paths
	 */
	public void setPathsToUnlock(List<String> pathsToUnlock) {
		this.pathsToUnlock = pathsToUnlock;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[UnlockWorkspaceFiles: pathsToUnlock=");
        builder.append(pathsToUnlock);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", browserSessionKey=");
        builder.append(browserSessionKey);
        builder.append("]");
        return builder.toString();
    }
	
	
}
