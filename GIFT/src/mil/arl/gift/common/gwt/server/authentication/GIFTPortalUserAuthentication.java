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
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskadapter.redmineapi.NotAuthorizedException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.RedmineTransportException;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;

import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;
import mil.arl.gift.common.io.Constants;

/**
 * This class will authenticate a user against the GIFT portal accounts.
 * 
 * @author mhoffman
 *
 */
public class GIFTPortalUserAuthentication implements
        UserAuthenticationInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GIFTPortalUserAuthentication.class);
    
    private static String NAME = "GIFT Portal Authentication";
    private static String DESCRIPTION = "Authenticates against user accounts on gifttutoring.org";
    
    /**
     * various authentication failure messages
     */
    private static final String ROLE_LIST_FAILED_MSG = "This GIFT instance has restricted access.  Your GIFT user role(s) are not on the list.";
    private static final String CREDENTIALS_FAILED_MSG = "Incorrect username or password.";
    private static final String UNKNOWN_EXCEPTION_MSG = "An exception happened while trying to login.";
    
    private static String redmineHost = "https://www.gifttutoring.org";
    
    /** gift project name on gift portal */
    private static final String GIFT_PROJECT_NAME = "GIFT";
    
    /** optional configuration files with gift portal roles allowed to pass authentication */
    private static final String ROLES = "ROLES";    
    private static File ROLE_LIST_FILE = new File("config" + File.separator + "tools" + File.separator + "dashboard" + File.separator + "GIFTPortalAllowedRoles.txt");

    private long lastModified = Long.MIN_VALUE;
    
    protected Set<String> roleNames = new HashSet<String>(); 
    
    /** using the Redmine API to query the GIFT Portal for user authentication */
    protected RedmineManager mgr;
    
    /** singleton instance of this class */
    private static GIFTPortalUserAuthentication instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return GIFTPortalUserAuthentication
     */
    public static synchronized GIFTPortalUserAuthentication getInstance(){
        
        if(instance == null){
            instance = new GIFTPortalUserAuthentication();
        }
        
        return instance;
    }
    
    /**
     * Private empty constructor
     */
    protected GIFTPortalUserAuthentication(){  }

    @Override
    public void isValidUser(String username, String password, String loginAsUserName, ServletRequest request, UserAuthResult authResult)
            throws UserAuthenticationException, Exception {
        
        //Currently the issue tracker is broken because a "com.taskadapter.redmineapi.RedmineFormatException: org.json.JSONException: A JSONObject text must begin with '{' at character 1 of"
        //is received with correct credentials of a user in the User role.
        //When using bad credentials a "com.taskadapter.redmineapi.RedmineAuthenticationException: Authorization error. Please check if you provided a valid API access key or Login and Password and REST API service is enabled on the server."
        //is received.
        RedmineManager mgr = null;
        try{
            mgr = RedmineManagerFactory.createWithUserAuth(redmineHost, username, password);
            User user = mgr.getUserManager().getCurrentUser();
            
            //Retrieve more user data this way in order to get access to the user's memberships
            user = mgr.getUserManager().getUserById(user.getId());
            
            //check the user's membership against the roles list
            for(Membership membership : user.getMemberships()){
                
                if(membership.getProject().getName().equals(GIFT_PROJECT_NAME)){
                    //found the GIFT project to check for the user's roles
                    
                    Collection<Role> userRoles = membership.getRoles();
                    if(!isValidRole(username, userRoles)){
                        //user assigned gift project roles aren't in the list of allowed roles
                        authResult.setLoginErrMsg(getRoleListFailedMessage());
                        return;
                    }
                    
                    break;
                }
            }
            
        }catch(RedmineTransportException transportException){
            logger.error("Caught exception while trying to authenticate'"+username+"'.", transportException);            
            throw new UserAuthenticationException("There was a problem authenticating the user given the credentials provided.\n" +
            		"Do you have an internet connection required for this type of authentication?", transportException);
            
        } catch(NotAuthorizedException authException) {
        	// The user is authenticated but not authorized to get the UserManager
            logger.error("Caught exception while trying to authenticate '"+username+"'.", authException);
            authResult.setLoginErrMsg(UNKNOWN_EXCEPTION_MSG);
            return;
        } catch(@SuppressWarnings("unused") RedmineAuthenticationException authenticationException){
            authResult.setLoginErrMsg(CREDENTIALS_FAILED_MSG);
            return;
        } catch(Throwable t){
            logger.error("Caught exception while trying to authenticate '"+username+"'.", t);
            authResult.setLoginErrMsg(UNKNOWN_EXCEPTION_MSG);
            return;
        }
    }
    
    /**
     * Checks the user's roles for the GIFT project against the roles in the file.
     * 
     * @param username the user being authenticated
     * @param userRoles the list of roles for this user in the GIFT portal's 'GIFT' project.
     * @return true if:
     * 1. the roles file is not found
     * 2. the roles list in the roles file is empty
     * 3. one of the roles provided is in the list of roles in the roles file
     * @throws Exception if there was a problem reading the roles file
     */
    protected boolean isValidRole(String username, Collection<Role> userRoles) throws Exception{
        
        File roleListFile = getRoleListFile();
        if(roleListFile.exists()){
            
            synchronized (roleNames) {
                
                updateRolesList();            
                
                //check for the role
                if(roleNames.isEmpty()){
                    return getDefaultValidRoleDecision();
                }else{
                    
                    for(Role role : userRoles){
                        
                        if(roleNames.contains(role.getName())){
                            return true;
                        }
                    }
                    
                    if(logger.isInfoEnabled()){
                        logger.info("User '"+username+"' failed authentication against allowed roles: "+roleNames);
                    }
                    
                    return false;
                }
            }
   
        }
        
        return getDefaultValidRoleDecision();
    }
    
    /**
     * Updates the list of roles from the Role list file.  The file will only be read if it 
     * exists and has been modified since the last read of the file.
     * 
     * @throws Exception if there was a problem reading from the roles list file
     */
    protected void updateRolesList() throws Exception{
        
        File roleListFile = getRoleListFile();
        if(roleListFile.exists()){
            
            synchronized (roleNames) {
                
                //has the file been modified since last check
                if(lastModified < roleListFile.lastModified()){
                    //read latest file
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Reading gift portal role authentication file.");
                    }
                    
                    roleNames.clear();
                    
                    Properties props = new Properties();
                    props.load(new FileInputStream(roleListFile));
                    
                    String value = props.getProperty(ROLES);
                    if(value != null && value.length() > 0){
                        String[] users = value.split(Constants.COMMA);
                        roleNames.addAll(Arrays.asList(users));
                    }
                    
                    lastModified = roleListFile.lastModified();
                }
            }
        }else if(!roleNames.isEmpty()){
            
            synchronized (roleNames) {
                roleNames.clear();
            }
        }
    }
    
    /**
     * Return the message that indicates why a login attempted failed based on the user's assigned roles.
     * 
     * @return the failed message about a lack of role assigned to the user attempting to login
     */
    protected String getRoleListFailedMessage(){
        return ROLE_LIST_FAILED_MSG;
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
        return true;
    }

    @Override
    public String logOutUser(ServletRequest request) {
        /* No special logic needed for logout */
        return null;
    }

}
