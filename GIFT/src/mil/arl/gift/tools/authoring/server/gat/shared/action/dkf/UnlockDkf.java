/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for unlocking a DKF.
 * 
 * @author elafave
 */
public class UnlockDkf implements Action<GatServiceResult> {

	/**
	 * Path of the DKF being unlocked.
	 */
	private String path;
	
	/** The user name. */
	private String userName;
	
	/** The unique identifier of the browser making the request */
    private String browserSessionKey;

    /**
     * No-arg constructor. Needed for serialization.
     */
    @SuppressWarnings("unused")
    private UnlockDkf() {
    }
    
    /**
     * Parameterized Constructor.
     *
     * @param path Path to be unlocked.  Can't be null or empty.
     */
    public UnlockDkf(String path) {       
        setPath(path);
    }

	/**
	 * Gets the path.
	 *
	 * @return the path. Won't be null or empty.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path for this action.
	 * 
	 * @param path path for this action. Can't be null or empty.
	 */
	public void setPath(String path) {
	    
        if(StringUtils.isBlank(path)){
            throw new IllegalArgumentException("The path can't be null or empty.");
        }
        
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
	 * @param userName User name. Can't be null or empty.
	 */
	public void setUserName(String userName) {
	    
	    if(StringUtils.isBlank(userName)){
            throw new IllegalArgumentException("The userName can't be null or empty.");
        }
	    
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
     * @param browserSessionKey the new value of the browser session key. Can't be null or empty.
     */
    public void setBrowserSessionKey(String browserSessionKey) {
        
        if(StringUtils.isBlank(browserSessionKey)){
            throw new IllegalArgumentException("The browserSessionKey can't be null or empty.");
        }
        
        this.browserSessionKey = browserSessionKey;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[UnlockDkf: path = ");
        builder.append(path);
        builder.append(", userName = ");
        builder.append(userName);
        builder.append(", browserSessionKey = ");
        builder.append(browserSessionKey);
        builder.append("]");
        return builder.toString();
    }
	
	
}
