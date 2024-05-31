/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.authentication;

import javax.servlet.ServletRequest;

import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;

/**
 * This interface is used for authentication purposes in GIFT.
 * 
 * @author mhoffman
 *
 */
public interface UserAuthenticationInterface {

    /**
     * Check if the provided credentials match a known user account.
     * 
     * @param username the user name to lookup  
     * @param password the password to check against that user name's password
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null. 
     * @param request the server request that called this method. Cannot be null, since it is needed by some authenticators.
     * @param result the authentication result where user metadata and validation success is tracked. Cannot be null.
     * @return null if a valid user otherwise the reason for failed authentication
     * @throws UserAuthenticationException if there was a problem validating the credentials
     * @throws Exception capture all other un-handled/unknown exceptions were the authentication logic doesn't provide a specific explanation/hint
     */
    public void isValidUser(String username, String password, String loginAsUserName, ServletRequest request, UserAuthResult result) throws UserAuthenticationException, Exception;
    
    /**
     * Return the name of the authentication implementation.  This can be used
     * to display to the user on the TUI, for GIFT logging, etc.
     * 
     * @return String a display name for this authentication implementation.
     */
    public String getName();
    
    /**
     * Return a description of the authentication implementation.  This can be used
     * to display to the user on the TUI, etc.
     * 
     * @return String a description of the authentication implementation
     */
    public String getDescription();
    
    /**
     * Return whether this authentication logic is enabled, i.e. it can be used to filter
     * access to this GIFT instance.
     * 
     * @param the server request that called this method. Cannot be null, since it is needed by some authenticators.
     * @return true if the isValidUser method can be used to authenticate users.  This is normally
     * false when the implementation doesn't have parameters to filter user on such as an empty white list of usernames.
     */
    public boolean isEnabled(ServletRequest request);

    /**
     * Logs out the user associated with the given server request. This is mainly used to support authentications that 
     * require GIFT to make an external request to sign out the user rather than managing the login state itself, such
     * as when GIFT users must sign in with SSO.
     * 
     * @param request the server request that called this method. Cannot be null.
     * @return null if the user was successfully signed out or an error string if an an error occurre while logging out.
     */
    public String logOutUser(ServletRequest request);
}
