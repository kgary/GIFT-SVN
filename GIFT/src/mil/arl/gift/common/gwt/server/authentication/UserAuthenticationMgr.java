/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.authentication;

import javax.servlet.ServletRequest;

/**
 * This class is used to manage the various user authentication logic used by the current GIFT instance.
 * 
 * @author mhoffman
 *
 */
public class UserAuthenticationMgr {

    /**
     * A set of secondary authenticators to validate against after verifying that a user's credentials
     * are correct according to a primary authenticator. Secondary authenticators act as an extra layer
     * of authentication to further limit user access.
     */
    private static final UserAuthenticationInterface[] secondaryAuthenticators = {
            LoginAsUserAuth.getInstance(),
            WhiteListUserAuth.getInstance()
    };
    
    /** singleton instance of this class */
    private static UserAuthenticationMgr instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return UserAuthenticationMgr
     */
    public static synchronized UserAuthenticationMgr getInstance(){
        
        if(instance == null){
            instance = new UserAuthenticationMgr();
        }
        
        return instance;
    }
    
    /**
     * Private empty constructor
     */
    private UserAuthenticationMgr(){        
        
    }
    
    /**
     * Check if the provided credentials match a known user account.
     * 
     * @param username the user name to lookup  
     * @param password the password to check against that user name's password
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param request the server request that called this method. Cannot be null.
     * @return an authorization result indicating if authorization succeeded and providing metadata surrounding the authentication. Will not be null.
     * @throws UserAuthenticationException if there was a problem validating the credentials
     * @throws Exception capture all other un-handled/unknown exceptions were the authentication logic doesn't provide a specific explanation/hint
     */
    public UserAuthResult isValidUser(String username, String password, String loginAsUserName, ServletRequest request) throws UserAuthenticationException, Exception{
        
        //determine the primay authenticator that should be used to authenticate
        UserAuthenticationInterface primaryAuthenticator = null;
        
        if(KeycloakUserAuthentication.getInstance().isEnabled(request)) {
            
            //default to Keycloak authenticator if Keycloak settings are provided
            primaryAuthenticator = KeycloakUserAuthentication.getInstance();
            
        } else if (LocalAccountAuthentication.getInstance().isEnabled(request)) {
            
            //default to the local account authenticator if the local account configuration has entries
            primaryAuthenticator = LocalAccountAuthentication.getInstance();
            
        } else {
            
            //otherwise, default to GIFT Portal authentication
            primaryAuthenticator = GIFTPortalUserAuthentication.getInstance();
        }
        
        //validate the user according to the primary authenticator first
        UserAuthResult authResult = new UserAuthResult();
        primaryAuthenticator.isValidUser(username, password, loginAsUserName, request, authResult);
        if(authResult.getAuthFailedReason() != null){
            return authResult;
        }
        
        //if the user passes the primary authenticator, validate against the secondary authenticators
        for(UserAuthenticationInterface authentication : secondaryAuthenticators){
            
            authentication.isValidUser(username, password, loginAsUserName, request, authResult);
            if(authResult.getAuthFailedReason() != null){
                return authResult;
            }
        }
        
        return authResult;
    }

    /**
     * Whether the primary authenticator supports using single-sign on through an external authentication webpage,
     * rather than relying on GIFT's built-in login page
     * 
     * @param request the server request that called this method. Cannot be null.
     * @return true if the authentication logic supports SSO. This is normally false unless the credentials for
     * a SSO service are provided in GIFT's server configuration files.
     */
    public boolean isSSOSupported(ServletRequest request) {
        
        if(KeycloakUserAuthentication.getInstance().isEnabled(request)) {
            return true;
        }
        
        return false;
    }
    
    /** 
     * Logs out the user associated with the given server request. This is mainly used to support authentications that 
     * require GIFT to make an external request to sign out the user rather than managing the login state itself, such
     * as when GIFT users must sign in with SSO.
     * 
     * @param request the server request that called this method. Cannot be null.
     * @return null if the user was successfully signed out or an error string if an an error occurre while logging out.
     */
    public String logOutUser(ServletRequest request) {
        
        //determine the primay authenticator that should be used to log out
        UserAuthenticationInterface primaryAuthenticator = null;
        
        if(KeycloakUserAuthentication.getInstance().isEnabled(request)) {
            
            //default to Keycloak authenticator if Keycloak settings are provided
            primaryAuthenticator = KeycloakUserAuthentication.getInstance();
            
        } else if (LocalAccountAuthentication.getInstance().isEnabled(request)) {
            
            //default to the local account authenticator if the local account configuration has entries
            primaryAuthenticator = LocalAccountAuthentication.getInstance();
            
        } else {
            
            //otherwise, default to GIFT Portal authentication
            primaryAuthenticator = GIFTPortalUserAuthentication.getInstance();
        }
        
        //log out the user according to the primary authenticator first
        String errorMsg = primaryAuthenticator.logOutUser(request);
        if(errorMsg != null){
            return errorMsg;
        }
        
        //if the user passes the primary authenticator, validate against the secondary authenticators
        for(UserAuthenticationInterface authentication : secondaryAuthenticators){
            
            errorMsg = authentication.logOutUser(request);
            if(errorMsg != null){
                return errorMsg;
            }
        }
        
        return errorMsg;
    }
    
    /**
     * The authentication result that is returned when checking that a user has authorization to access GIFT's web pages
     * 
     * @author nroberts
     */
    public static class UserAuthResult {
        
        /** An error message that was thrown from an unsuccessful attempt to log in */
        private String loginErrMsg;
        
        /** 
         * The name of this user, as recognized by the authentication service. This can be useful
         * for SSO or other situations where the client may not know the username and needs it returned
         * from the user.
         */
        private String username;
        
        /** 
         * Whether the user should automatically be treated as if they signed in using debug mode. This can be
         * useful in conjunction with SSO to automatically grant permission to certain features based on roles. 
         */
        private boolean isAutoDebug = false;

        /**
         * An error message that was thrown from an unsuccessful attempt to log in
         * 
         * @return the error message, if any. Will be null if login was successful.
         */
        public String getAuthFailedReason() {
            return loginErrMsg;
        }

        /**
         * Sets an error message that was thrown from an unsuccessful attempt to log in
         * 
         * @param loginErrMsg the error message. Can be null.
         */
        public void setLoginErrMsg(String loginErrMsg) {
            this.loginErrMsg = loginErrMsg;
        }

        /**
         * Gets the name of this user, as recognized by the authentication service. This can be useful
         * for SSO or other situations where the client may not know the username and needs it returned
         * from the user.
         * 
         * @return the authenticated username. Can be null.
         */
        public String getAuthUsername() {
            return username;
        }

        /**
         * Sets the name of this user, as recognized by the authentication service. This can be useful
         * for SSO or other situations where the client may not know the username and needs it returned
         * from the user.
         * 
         * @param username the authenticated username. Can be null.
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Gets whether the user should automatically be treated as if they signed in using debug mode. This can be
         * useful in conjunction with SSO to automatically grant permission to certain features based on roles. 
         * 
         * @return whether the user should automatically enter debug mode.
         */
        public boolean isAutoDebug() {
            return isAutoDebug;
        }

        /**
         * Sets whether the user should automatically be treated as if they signed in using debug mode. This can be
         * useful in conjunction with SSO to automatically grant permission to certain features based on roles. 
         * 
         * @param isAutoDebug whether the user should automatically enter debug mode.
         */
        public void setAutoDebug(boolean isAutoDebug) {
            this.isAutoDebug = isAutoDebug;
        }
    }
}
