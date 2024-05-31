/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for unlocking a course.
 * 
 * @author elafave
 */
public class UnlockCourse implements Action<GatServiceResult> {

	/**
	 * Path of the course being unlocked.
	 */
	private String domainRelativeCoursePath;

    /** The user name. */
	private String userName;
	
	/** The unique identifier for the browser that is making the request */
	private String browserSessionKey;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public UnlockCourse() {
    }

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getRelativePath() {
		return domainRelativeCoursePath;
	}

	/**
	 * Sets the path for this action.
	 * 
	 * @param domainRelativeCoursePath path for this action.
	 */
	public void setRelativePath(String domainRelativeCoursePath) {
		this.domainRelativeCoursePath = domainRelativeCoursePath;
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
	 * Gets the unique identifier for the browser making the request
	 * @return the value of the browser session key
	 */
	public String getBrowserSessionKey() {
        return browserSessionKey;
    }
	
	/**
     * Sets the unique identifier for the browser making the request
     * @param browserSessionKey the new value of the browser session key
     */
	public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }
	   
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[UnlockCourse: domainRelativeCoursePath=");
        builder.append(domainRelativeCoursePath);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", browserSessionKey=");
        builder.append(browserSessionKey);
        builder.append("]");
        return builder.toString();
    }
}
