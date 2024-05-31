/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;

import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for locking a Conversation Tree file.
 * 
 * @author bzahid
 */
public class LockConversation implements Action<LockFileResult> {

	/**
	 * Path of the Conversation being locked.
	 */
	private String domainRelativeFilePath;
	
	/** The user name. */
	private String userName;
	
	/** The unique identifier for the browser which is making the request */
	private String browserSessionKey;

	/**
     * No-arg constructor. Needed for serialization.
     */
    public LockConversation() {
    }

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getRelativePath() {
		return domainRelativeFilePath;
	}

	/**
	 * Sets the path for this action.
	 * 
	 * @param domainRelativeFilePath path for this action.
	 */
	public void setRelativePath(String domainRelativeFilePath) {
		this.domainRelativeFilePath = domainRelativeFilePath;
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
	 * Gets the unique identifier for the browser which is making the request
	 * @return the value of the browser session key, can be null
	 */
	public String getBrowserSessionKey() {
        return browserSessionKey;
    }
	
	/**
	 * Sets the unique identifier for the browser which is making the request
	 * @param browserSessionKey the new value of the browser session key, can be null
	 */
	public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[LockConversation: ");
        sb.append("domainRelativeFilePath = ").append(domainRelativeFilePath);
        sb.append(", userName = ").append(userName);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append("]");

        return sb.toString();
    } 
}
