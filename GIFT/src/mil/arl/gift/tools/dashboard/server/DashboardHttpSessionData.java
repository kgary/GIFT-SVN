/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.util.UUID;


/**
 * A DashboardHttpSessionData class which will contain a unique id of the usersession,
 * along with other needed user data. This data will be stored in the httpSession data on the server.
 * The client will get the session/browser ids, but the internal data such as username/password are to 
 * be kept on the server.
 *   
 * @author nblomberg
 *
 */
public class DashboardHttpSessionData {
    

    /** A unique identifier that is used to identify the user session.  This id is passed
     * back to the client so that the client can send this id in future rpcs to identify which client is requesting
     * information.  This id is temporary to the life of the server and the session.  
     */
    private final String sessionId = UUID.randomUUID().toString();
        
    /** The name of the user being logged in.  This can be optional (in the case for lti user sessions) if there is
     * no name for the user.
     */
    private String userName = "";
    /** The password for the user being logged in.  This can be optional (in the case for lti user sessions) if there is
     * no logged in user name.
     */
    private String userPass = "";
    
    /**
     * Whether or not this user logged in without authentication because GIFT was offline
     */
    private boolean isOffline = false;
    
    /**
     * Constructor 
     * @param user Name of the user (can be empty string if a non-logged in user session such as lti users).
     * @param offline Whether or not this user logged in without authentication because GIFT was offline.
     */
    public DashboardHttpSessionData(String user, boolean offline) {
        this(user);
        isOffline = offline;
    }
    
    /**
     * Constructor 
     * @param user Name of the user (can be empty string if a non-logged in user session such as lti users).
     * @param password Password of the user (can be an empty string if a non-logged in user session such as lti users).
     */
    public DashboardHttpSessionData(String user, String password) {
        userName = user;
        userPass = password;
    }
    
    /** 
     * Constructor Used for offline user sessions where there is no password required.
     * 
     * @param user Name of the user 
     */
    public DashboardHttpSessionData(String user) {
        userName = user;
    }
    
    /**
     * Accessor to get the user session id.  This is a temporary id (temporary to the life of the server runtime instance
     * and/or the life of the session.
     * 
     * @return
     */
    public String getUserSessionId() {
        return sessionId;
    }
            
    /**
     * Accessor to get the user name of the user.  This can be an empty string if there is no logged in user such
     * as an lti user.
     * @return
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Accessor to get the user password.  This can be an empty string if there is no logged in user such as
     * an lti user.
     * @return
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
        
        StringBuilder sb = new StringBuilder();
        sb.append("[DashboardHttpSessionData: ");
        sb.append("sessionId = ").append(sessionId);
        sb.append(", username = ").append(userName);
        sb.append(", offline = ").append(isOffline);
        sb.append("]");
        return sb.toString();
    }
    
}
