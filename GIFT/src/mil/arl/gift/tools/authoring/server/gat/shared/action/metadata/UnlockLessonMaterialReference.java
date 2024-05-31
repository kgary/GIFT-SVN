/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.metadata;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for unlocking a TrainingApplicationReference.
 * 
 * @author nroberts
 */
public class UnlockLessonMaterialReference implements Action<GatServiceResult> {

	/**
	 * Path of the file being unlocked.
	 */
	private String domainRelativePath;
	
	/** The user name. */
	private String userName;
	
	/** The unique identifier of the browser that is making the request */
	private String browserSessionKey;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public UnlockLessonMaterialReference() {
    }

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getRelativePath() {
		return domainRelativePath;
	}

	/**
	 * Sets the path for this action.
	 * 
	 * @param domainRelativePath path for this action.
	 */
	public void setRelativePath(String domainRelativePath) {
		this.domainRelativePath = domainRelativePath;
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
	 * Gets the unique identifier of the browser that is making the request
	 * @return the value of the browser session key
	 */
	public String getBrowserSessionKey() {
	    return browserSessionKey;
	}
	
	/**
	 * Sets the unique identifier of the browser that is making the request
	 * @param browserSessionKey the new value of the browser session key
	 */
	public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[UnlockLessonMaterialReference: ");
        sb.append("domainRelativePath = ").append(domainRelativePath);
        sb.append(", userName = ").append(userName);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append("]");

        return sb.toString();
    } 
}
