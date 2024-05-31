/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for locking a DKF.
 * 
 * @author elafave
 */
public class LockDkf implements Action<LockFileResult> {

	/**
	 * Path of the DKF being locked.
	 */
	private String domainRelativeDkfPath;
	
	/**
	 * If true it indicates that we're trying to require the lock which will
	 * fail if the file is already locked. If it is true it indicates that
	 * we're trying to renew the lock that we already hold.
	 */
	private boolean acquisition = true;
	
	/** The user name. */
	private String userName;
	
	/** The unique identifier for the browser that is making the request */
	private String browserSessionKey;

	/**
     * No-arg constructor. Needed for serialization.
     */
    public LockDkf() {
    }

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getRelativePath() {
		return domainRelativeDkfPath;
	}

	/**
	 * Sets the path for this action.
	 * 
	 * @param domainRelativeDkfPath path for this action.
	 */
	public void setRelativePath(String domainRelativeDkfPath) {
		this.domainRelativeDkfPath = domainRelativeDkfPath;
	}

	/**
	 * Is the lock request trying to acquire the lock that it hasn't yet
	 * acquired.
	 * @return True if this is the first time we're requesting the lock,
	 * false if we're simply renewing our lock on the file.
	 */
    public boolean isAcquisition() {
		return acquisition;
	}

    /**
     * Sets the acquisition status of the lock request.
     * @param acquisition True if we're trying to acquire the lock, false
     * if we're renewing the lock we've already acquired.
     */
    public void setAcquisition(boolean acquisition) {
		this.acquisition = acquisition;
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
	 * @return the value of the browser session key
	 */
	public String getBrowserSessionKey() {
        return browserSessionKey;
    }
	
	/**
	 * Sets the unique identifier for the browser which is making the request
	 * @param browserSessionKey the new value of the browser session key
	 */
	public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[LockDkf: ");
        sb.append("domainRelativeDkfPath = ").append(domainRelativeDkfPath);
        sb.append(", acquisition = ").append(acquisition);
        sb.append(", userName = ").append(userName);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append("]");

        return sb.toString();
    } 
}
