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

import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;
import mil.arl.gift.common.io.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will authenticate a user against a list of users.
 * 
 * @author mhoffman
 *
 */
public class WhiteListUserAuth implements UserAuthenticationInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(WhiteListUserAuth.class);
    
    private static final String NAME = "White List Authentication";
    private static final String DESCRIPTION = "Authenticates against usernames in the white list file";
    
    private static final String FAILED_MSG = "This GIFT instance currently has restricted access.  Your username is not on the list.";
    
    private static final String USERS = "USERS";
    
    private static File WHITE_LIST_FILE = new File("config" + File.separator + "tools" + File.separator + "dashboard" + File.separator + "WhiteListUsers.txt");

    private long lastModified = Long.MIN_VALUE;
    
    private Set<String> usernames = new HashSet<String>();  
    
    /** singleton instance of this class */
    private static WhiteListUserAuth instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return WhiteListUserAuth the singleton instance
     */
    public static synchronized WhiteListUserAuth getInstance(){
        
        if(instance == null){
            instance = new WhiteListUserAuth();
        }
        
        return instance;
    }
    
    /**
     * Private empty constructor
     */
    private WhiteListUserAuth(){        
        
    }
    
    @Override
    public void isValidUser(String username, String password, String loginAsUserName, ServletRequest request, UserAuthResult authResult)
            throws UserAuthenticationException, Exception {

        if(WHITE_LIST_FILE.exists()){
            
            synchronized (usernames) {
                
                updateWhiteList();           
                
                //check for the username
                if(usernames.isEmpty() || usernames.contains(username)){
                    return;
                }else{
                    if(logger.isInfoEnabled()){
                        logger.info("User '"+username+"' failed authentication against "+NAME);
                    }
                    authResult.setLoginErrMsg(FAILED_MSG);
                    return;
                }
            }
   
        }
    }
    
    /**
     * Updates the list of users from the white list file.  The file will only be read if it 
     * exists and has been modified since the last read of the file.
     * 
     * @throws Exception if there was a problem reading from the white list file
     */
    private void updateWhiteList() throws Exception{
        
        if(WHITE_LIST_FILE.exists()){
        
            synchronized (usernames) {
                
                //has the file been modified since last check
                if(lastModified < WHITE_LIST_FILE.lastModified()){
                    //read latest file
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Reading White List authentication file.");
                    }
                    
                    usernames.clear();
                    
                    Properties props = new Properties();
                    props.load(new FileInputStream(WHITE_LIST_FILE));
                    
                    String value = props.getProperty(USERS);
                    if(value != null && value.length() > 0){
                        String[] users = value.split(Constants.COMMA);
                        usernames.addAll(Arrays.asList(users));
                    }
                    
                    lastModified = WHITE_LIST_FILE.lastModified();
                } 
            }
        }else if(!usernames.isEmpty()){
            //the file may have been deleted, make sure to clear any existing usernames
            
            synchronized (usernames) {
                usernames.clear();
            }
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
    public boolean isEnabled(ServletRequest request) {
        
        try {
            updateWhiteList();
            return !usernames.isEmpty();
        } catch (@SuppressWarnings("unused") Exception e) {
            //not interested
        }
        
        return false;
    }
    
    @Override
    public String logOutUser(ServletRequest request) {
        /* No special logic needed for logout */
        return null;
    }

}
