/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for unlocking a Conversation.
 * 
 * @author bzahid
 */
public class UnlockConversation implements Action<GatServiceResult> {

	/**
	 * Path of the file being unlocked.
	 */
	private String path;

    /** The user name. */
	private String userName;
	
	/** The unique identifier of the browser making the request */
	private String browserSessionKey;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public UnlockConversation() {
    }
    
    /**
     * Parameterized Constructor.
     *
     * @param path Path to be unlocked.
     */
    public UnlockConversation(String path) {
    	this.path = path;
    }

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path for this action.
	 * 
	 * @param path path for this action.
	 */
	public void setPath(String path) {
		this.path = path;
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
	   
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[UnlockConversation: path=");
        builder.append(path);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", browserSessionKey=");
        builder.append(browserSessionKey);
        builder.append("]");
        return builder.toString();
    }
}
