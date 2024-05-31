/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Request;
import org.keycloak.adapters.jetty.core.AbstractKeycloakJettyAuthenticator.KeycloakAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;
import mil.arl.gift.common.io.Constants;

/**
 * This class will authenticate a user against Keycloak accounts.
 * 
 * @author mhoffman
 *
 */
public class KeycloakUserAuthentication implements
        UserAuthenticationInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KeycloakUserAuthentication.class);
    
    private static String NAME = "Keycloak Authentication";
    private static String DESCRIPTION = "Authenticates against a Keycloak server";
    
    /**
     * various authentication failure messages
     */
    private static final String ROLE_LIST_FAILED_MSG = "This Keycloak instance has restricted access.  Your Keycloak user role(s) are not on the list.";
    private static final String UNKNOWN_EXCEPTION_MSG = "An exception happened while trying to login.";
    
    /** optional configuration files with gift portal roles allowed to pass authentication */
    private static final String USER_ROLES = "USER_ROLES";    
    private static final String ADMIN_ROLES = "ADMIN_ROLES";    
    private static File ROLE_LIST_FILE = new File("config" + File.separator + "tools" + File.separator + "gas" + File.separator 
            + "auth" + File.separator + "keycloak" + File.separator + "KeycloakAllowedRoles.txt");

    private long lastModified = Long.MIN_VALUE;
    
    protected Set<String> userRoleNames = new HashSet<String>(); 
    protected Set<String> adminRoleNames = new HashSet<String>(); 
    
    /** singleton instance of this class */
    private static KeycloakUserAuthentication instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return GIFTPortalUserAuthentication
     */
    public static synchronized KeycloakUserAuthentication getInstance(){
        
        if(instance == null){
            instance = new KeycloakUserAuthentication();
        }
        
        return instance;
    }
    
    /**
     * Private empty constructor
     */
    protected KeycloakUserAuthentication(){  }

    @Override
    public void isValidUser(String username, String password, String loginAsUserName, ServletRequest request, UserAuthResult authResult)
            throws UserAuthenticationException, Exception {
        
        try{
            
            /* 
             * Get the Keycloak authenticator from the request. This is possible because GIFT's Keycloak settings
             * are handled by Jetty's web application settings, so Jetty handles the brunt of the work and exposes APIs
             * in the HTTP request to let is query the Keycloak service.
             */
            Request jettyRequest = Request.getBaseRequest(request);
                
            UserAuthentication userAuth = (UserAuthentication) jettyRequest.getAuthentication();
            if(!(userAuth instanceof KeycloakAuthentication)) {
                throw new IllegalStateException("Somehow received authentication details from a non-Keycloak authenticator while requiring Keycloak authentication");
            }
            
            KeycloakAuthentication keycloakAuth = (KeycloakAuthentication) userAuth;
            
            /* Check what level of role permission the user has */
            if(isValidAdminRole(jettyRequest, keycloakAuth)) {
                authResult.setAutoDebug(true);
            
            } else if(isValidUserRole(jettyRequest, keycloakAuth)) {
                authResult.setAutoDebug(false);
            
            } else {
                authResult.setLoginErrMsg(ROLE_LIST_FAILED_MSG);
            }
            
            /* Keycloak auth doesn't receive username from client, so get from keycloak server */
            authResult.setUsername(keycloakAuth.getUserIdentity().getUserPrincipal().getName());
            
        } catch(Throwable t){
            logger.error("Caught exception while trying to authenticate '"+username+"'.", t);
            authResult.setLoginErrMsg(UNKNOWN_EXCEPTION_MSG);
            return;
        }
    }
    
    /**
     * Checks the user's roles for Keycloak against the roles in the file to see if they are a regular user.
     * 
     * @param jettyRequest the request for authentication. Cannot be null.
     * @param keycloakAuth the Keycloak authenticator. Cannot be null.
     * @return true if:
     * 1. the roles file is not found
     * 2. the roles list in the roles file is empty
     * 3. one of the roles provided is in the list of roles in the roles file
     * @throws Exception if there was a problem reading the roles file
     */
    private boolean isValidUserRole(Request jettyRequest, KeycloakAuthentication keycloakAuth) throws Exception{
        
        File roleListFile = getRoleListFile();
        if(roleListFile.exists()){
            
            synchronized (userRoleNames) {
                
                updateRolesLists();            
                
                //check for the role
                boolean validUser = false;
                if(userRoleNames.isEmpty()) {
                    return getDefaultValidRoleDecision();
                } else {
                    for(String roleName : userRoleNames) {
                         if(keycloakAuth.getUserIdentity().isUserInRole(roleName, jettyRequest.getUserIdentityScope())){
                             validUser = true;
                             break;
                         }
                    }
                }
                
                return validUser;
            }
        }
        
        return getDefaultValidRoleDecision();
    }
    
    /**
     * Checks the user's roles for Keycloak against the roles in the file to see if they are an admin.
     * 
     * @param jettyRequest the request for authentication. Cannot be null.
     * @param keycloakAuth the Keycloak authenticator. Cannot be null.
     * @return true if:
     * 1. the roles file is not found
     * 2. the roles list in the roles file is empty
     * 3. one of the roles provided is in the list of roles in the roles file
     * @throws Exception if there was a problem reading the roles file
     */
    private boolean isValidAdminRole(Request jettyRequest, KeycloakAuthentication keycloakAuth) throws Exception{
        
        File roleListFile = getRoleListFile();
        if(roleListFile.exists()){
            
            synchronized (adminRoleNames) {
                
                updateRolesLists();            
                
                //check for the role
                boolean validUser = false;
                if(adminRoleNames.isEmpty()) {
                    return false;
                } else {
                    for(String roleName : adminRoleNames) {
                         if(keycloakAuth.getUserIdentity().isUserInRole(roleName, jettyRequest.getUserIdentityScope())){
                             validUser = true;
                             break;
                         }
                    }
                }
                
                return validUser;
            }
        }
        
        return false;
    }
    
    /**
     * Updates the list of roles from the Role list file.  The file will only be read if it 
     * exists and has been modified since the last read of the file.
     * 
     * @throws Exception if there was a problem reading from the roles list file
     */
    protected void updateRolesLists() throws Exception{
        
        File roleListFile = getRoleListFile();
        if(roleListFile.exists()){
            
            synchronized (userRoleNames) {
                synchronized(adminRoleNames) {
                
                    //has the file been modified since last check
                    if(lastModified < roleListFile.lastModified()){
                        //read latest file
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Reading keycloak role authentication file.");
                        }
                        
                        userRoleNames.clear();
                        adminRoleNames.clear();
                        
                        Properties props = new Properties();
                        props.load(new FileInputStream(roleListFile));
                        
                        String value = props.getProperty(USER_ROLES);
                        if(value != null && value.length() > 0){
                            String[] users = value.split(Constants.COMMA);
                            userRoleNames.addAll(Arrays.asList(users));
                        }
                        
                        value = props.getProperty(ADMIN_ROLES);
                        if(value != null && value.length() > 0){
                            String[] users = value.split(Constants.COMMA);
                            adminRoleNames.addAll(Arrays.asList(users));
                        }
                        
                        lastModified = roleListFile.lastModified();
                    }
                }
            }
        }else if(!userRoleNames.isEmpty() || !adminRoleNames.isEmpty()){
            
            synchronized (userRoleNames) {
                userRoleNames.clear();
            }
            
            synchronized (adminRoleNames) {
                adminRoleNames.clear();
            }
        }
    }
    
    /**
     * Return the file that contains the roles to check against for this authenticator.
     *  
     * @return the file containing one or more GIFT roles to check against.  Can be null or non-existent, in which 
     * case the default valid role decision value will be used (see {@link #getDefaultValidRoleDecision()}).
     */
    protected File getRoleListFile(){
        return ROLE_LIST_FILE;
    }
    
    /**
     * Return the default decision of whether to allow a successful authentication when there are no roles to compare
     * to because either the role file doesn't exist or there are no roles listed.
     * 
     * @return the default decision when there are no roles to check against
     */
    protected boolean getDefaultValidRoleDecision(){
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean isEnabled(ServletRequest request) {
        
        /* Just check if the request has Keycloak authentication assigned to it*/
        Request jettyRequest = Request.getBaseRequest(request);
        
        if(jettyRequest.getAuthentication() instanceof KeycloakAuthentication) {
            return true;
        }
        
        return false;
    }

    @Override
    public String logOutUser(ServletRequest request) {
        try{
        
            /* Logging out through the HTTP session and then refreshing the page is sufficient to kick
             * the user back to the Keycloak login page */
            Request jettyRequest = Request.getBaseRequest(request);
            jettyRequest.logout();
            
            return null;
        
        } catch(Throwable t){
            logger.error("Caught exception while trying to log out", t);
            return "Unable to log out user due to an errror :" + t;
        }
    }
}
