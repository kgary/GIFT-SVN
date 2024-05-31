/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.authentication;

import java.io.File;

import javax.servlet.ServletRequest;

import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;

/**
 * Checks whether the user logging in has the appropriate GIFT role to be able to login as another GIFT user.
 * 
 * @author mhoffman
 *
 */
public class LoginAsUserAuth extends GIFTPortalUserAuthentication {
    
    private static String NAME = "Login As User Authentication";
    private static String DESCRIPTION = "Determines whether a gifttutoring.org user's account has a role matchine one of the roles found in the 'login as' authentication file.";

    private static LoginAsUserAuth instance = null;
    
    private static final String ROLE_LIST_FAILED_MSG = "Unable to login as another GIFT user because your GIFT account does not have the appropriate role.";
    
    /** optional configuration files with gift portal roles allowed to pass authentication */
    private static File ROLE_LIST_FILE = new File("config" + File.separator + "tools" + File.separator + "dashboard" + File.separator + "GIFTPortalLoginAsAllowedRoles.txt");
       
    /**
     * Return the singleton instance of this class.
     * 
     * @return LoginAsUserAuth the singleton instance
     */
    public static synchronized LoginAsUserAuth getInstance(){
        
        if(instance == null){
            instance = new LoginAsUserAuth();
        }
        
        return instance;
    }
    
    /**
     * Private empty constructor
     */
    private LoginAsUserAuth(){        
        super();
    }
    
    @Override
    public void isValidUser(String username, String password, String loginAsUserName, ServletRequest request, UserAuthResult authResult) throws UserAuthenticationException, Exception{
        
        if(loginAsUserName == null || loginAsUserName.isEmpty()){
            return;
        }else{
            super.isValidUser(username, password, loginAsUserName, request, authResult);
        }
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
    protected String getRoleListFailedMessage(){
        return ROLE_LIST_FAILED_MSG;
    }
    
    @Override
    protected File getRoleListFile(){
        return ROLE_LIST_FILE;
    }
    
    @Override
    protected boolean getDefaultValidRoleDecision(){
        return false;
    }
    
    @Override
    public boolean isEnabled(ServletRequest request) {

        try {
            updateRolesList();
            return !roleNames.isEmpty();
        } catch (@SuppressWarnings("unused") Exception e) {
            //not interested
        }
            
        
        return false;
    }
}
