/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.RpcResponse;


/**
 * The LoginResponse contains the data from a loginUser request from the client.  Currently it contains
 * the userName & password, but eventually should hold some Single Sign On token that can be passed around.
 * 
 * @author nblomberg
 *
 */
public class LoginResponse extends RpcResponse {

    
    // $TODO$ nblomberg -- This is in here so we can pass the data and authenticate with the TUI!
    // Remove for security reasons.  We don't want to pass around the username/password, but will
    // need to until we have support for single sign on between dashboard & other tools & tui.
    private String userName;
    private String userPass;
    private boolean isOnline;
    
    /**
     * Whether the user who was logged in should automatically be given have debug permissions even if they 
     * weren't explicitly requested. This is mainly used to support SSO solutions where users who need access
     * to things like Game Master may not use the debug URL flag.
     */
    private boolean isAutoDebug;
    
    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public LoginResponse() {
    }
    
    /**
     * An rpc that is used to get the existing session information of a user (if it exists).
     * 
     * @param userSessionId - The user session id from the server.
     * @param browserSessionId - The browser session id from the server.
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param state - The screen state that the server found (can be invalid).
     */
    public LoginResponse(String userSessionId, String browserSessionId, boolean success, String response, String userName, String password) {
       
        super(userSessionId, browserSessionId, success, response);
        this.userName = userName;
        this.userPass = password;
    }
    
    /**
     * An rpc that is used to get the existing session information of a user (if it exists).
     * 
     * @param userSessionId - The user session id from the server.
     * @param browserSessionId - The browser session id from the server.
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param state - The screen state that the server found (can be invalid).
     * @param isAutoDebug - Whether the client should automatically enter debug mode.
     */
    public LoginResponse(String userSessionId, String browserSessionId, boolean success, String response, String userName, String password, boolean isOnline, boolean isAutoDebug) {
       
        super(userSessionId, browserSessionId, success, response);
        this.userName = userName;
        this.userPass = password;
        this.isOnline = isOnline;
        this.isAutoDebug = isAutoDebug;
    }

    
    /**
     * Accessor to get the username of the user
     * @return username
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Accessor to get the password of the user
     * @return userPass
     */
    public String getUserPass() {
        return userPass;
    }
    
    /**
     * Accessor to get the online state of the user.
     * @return true if the user is online, false otherwise
     */
    public boolean isOnline() {
        return isOnline;
    }
    
    /**
     * Gets wether the client should automatically enter debug mode upon logging in.
     * 
     * @return whether to use debug mode
     */
    public boolean isAutoDebug() {
        return isAutoDebug;
    }
    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("[LoginResponse: ");
        sb.append("username = ").append(userName);
        sb.append(", isOnline = ").append(isOnline);
        sb.append("]");
        return sb.toString();
    }
    
}
