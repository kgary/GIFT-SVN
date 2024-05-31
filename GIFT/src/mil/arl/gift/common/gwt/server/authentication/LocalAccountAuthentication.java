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
import java.util.Properties;

import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;

/**
 * This class will authenticate a user against a predefined set of user account credentials defined in a local
 * configuration file
 * 
 * @author nroberts
 */
public class LocalAccountAuthentication implements UserAuthenticationInterface {
    
    /** The logger used to log messages to output files */
    private static Logger logger = LoggerFactory.getLogger(LocalAccountAuthentication.class);
    
    /** the name of this authentication interface */
    private static String NAME = "Local Account Authentication";
    
    /** the description of this authentication interface */
    private static String DESCRIPTION = "Authenticates against credentials defined in the local Dashboard configuration";
    
    /** error message used when provided credentials don't pass authentication */
    private static final String CREDENTIALS_FAILED_MSG = "Incorrect username or password. Your credentials were not found in GIFT's local account configuration.";
    
    /** The configuration file containing the credentials for locally-defined user accounts */
    private static File LOCAL_ACCOUNT_FILE = new File(PackageUtil.getConfiguration() + File.separator + "tools" + File.separator + "dashboard" + File.separator + "LocalAccounts.txt");
    
    /** The singleton instance of this class */
    private static LocalAccountAuthentication instance = null;
    
    /** The last time the local account configuration file was modified */
    private long lastModified = Long.MIN_VALUE;
    
    /** The properties that define the local user accounts that have been configured  */
    private Properties localAccounts = new Properties();
    
    /**
     * The default constructor used to construct the singleton instance of this class
     */
    private LocalAccountAuthentication() { }

    @Override
    public void isValidUser(String username, String password, String loginAsUserName, ServletRequest request, UserAuthResult authResult)
            throws UserAuthenticationException, Exception {
        
        if(LOCAL_ACCOUNT_FILE.exists()){
            
            synchronized (localAccounts) {
                
                updateAccounts();           
                
                //check if the password entered matches the password defined for this user in the local account configuration
                String validPass = localAccounts.getProperty(username);
                if(validPass != null && validPass.equals(password)){
                    return;
                }else{
                    
                    authResult.setLoginErrMsg(CREDENTIALS_FAILED_MSG);
                    return;
                }
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
            updateAccounts();
            return !localAccounts.isEmpty();
        } catch (@SuppressWarnings("unused") Exception e) {
            //not interested
        }
        
        return false;
    }
    
    /**
     * Updates the stored set of user account credentials based on the local account configuration file. If the
     * file doesn't exist, is empty, cannot be read, then the set of user account credentials will be empty.
     * 
     * @throws Exception if a problem occurs while reading the local account configuration file.
     */
    private void updateAccounts() throws Exception{
        
        if(LOCAL_ACCOUNT_FILE.exists()){
        
            synchronized (localAccounts) {
                
                //has the file been modified since last check
                if(lastModified < LOCAL_ACCOUNT_FILE.lastModified()){
                    //read latest file
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Reading local account authentication file.");
                    }
                    
                    localAccounts.clear();
                    try(FileInputStream fileInputStream = new FileInputStream(LOCAL_ACCOUNT_FILE)){
                        localAccounts.load(fileInputStream);
                    }
                    
                    lastModified = LOCAL_ACCOUNT_FILE.lastModified();
                } 
            }
            
        } else if(!localAccounts.isEmpty()){
            //the file may have been deleted, make sure to clear any existing accounts
            
            synchronized (localAccounts) {
                localAccounts.clear();
            }
        }
    }

    /**
     * Gets the singleton instance of this class
     * 
     * @return the singleton instance. Will not be null.
     */
    public synchronized static LocalAccountAuthentication getInstance() {
        
        if(instance == null) {
            instance = new LocalAccountAuthentication();
        }
        
        return instance;
    }
    
    @Override
    public String logOutUser(ServletRequest request) {
        /* No special logic needed for logout */
        return null;
    }
}
