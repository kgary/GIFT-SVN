/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;


/**
 * The ExistingSessionResponse contains the 'existing session' data for a user
 * if the user already has a session on the server.  The existing session data contains
 * the screen state of the user that can be used to restore the client to a particular screen in 
 * a given state.
 * 
 * @author nblomberg
 *
 */
public class ExistingSessionResponse extends RpcResponse {

    
    private ScreenEnum screenState;
    
    // $TODO$ nblomberg -- This is in here so we can pass the data and authenticate with the TUI!
    // Remove for security reasons.  We don't want to pass around the username/password, but will
    // need to until we have support for single sign on between dashboard & other tools & tui.
    private String userName;
    private String userPass;
    
    /**
     * Whether or not this user logged in without authentication because GIFT was offline
     */
    private boolean isOffline;
    
    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public ExistingSessionResponse() {
    }
    
    /**
     * An rpc that is used to get the existing session information of a user (if it exists).
     * 
     * @param userSessionId - The user session id from the server.
     * @param browserSessionId - The browser session id from the server.
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param state - The screen state that the server found (can be invalid).
     * @param offline - Whether or not this user logged in without authentication because GIFT was offline
     */
    public ExistingSessionResponse(String userSessionId, String browserSessionId, boolean success, String response, String username, String password, ScreenEnum state, boolean offline) {
       
        super(userSessionId, browserSessionId, success, response);
        this.screenState = state;
        this.userName = username;
        this.userPass = password;
        this.isOffline = offline;
    }
    
    /**
     * Accessor to set the screen state of the response
     * @param state - screen state.  Should not be null.
     */
    public void setScreenState(ScreenEnum state) {
        screenState = state;
    }
    
    /**
     * Accessor to get the screen state of the response.
     * @return ScreenEnum - The screen state of the response (should not be null).
     */
    public ScreenEnum getScreenState() {
        return screenState;
    }
    
    /**
     * Accessor to the get username of the response.
     * @return String - The username of the response.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Accessor to the get user password of the response.
     * @return String - The user password  of the response.
     */
    public String getUserPass() {
        return userPass;
    }

    /**
     * Gets whether or not this user logged in without authentication because GIFT was offline
     * 
     * @return whether or not this user logged in without authentication because GIFT was offline
     */
    public boolean isOffline() {
        return isOffline;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ExistingSessionResponse: screenState=");
        builder.append(screenState);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", isOffline=");
        builder.append(isOffline);
        builder.append("]");
        return builder.toString();
    }
   
    
}
