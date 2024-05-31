/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.Constants;

/**
 * The OfflineUsers class is used to load the list of offline users that can be used
 * to prepopulate the UMS database with and make available to the offline user login page.
 * 
 * This should only be used in desktop mode.
 * 
 * @author nblomberg
 *
 */
public class OfflineUsers  {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(OfflineUsers.class);
   
    /** Defines the offline user file that holds the list of offline users. */
    private static File OFFLINE_USER_LIST_FILE = new File("config" + File.separator + "tools" + File.separator + 
            "dashboard" + File.separator + "OfflineUsers.txt");
    
    /** List of offline usernames to populate into the db.  Can be empty if the feature is not used. */
    private List<String> usernames = new ArrayList<String>();  
    
    /** Token that represents a comment in the file. */
    private static final String COMMENT_TOKEN = "#";


    /**
     * Constructor
     */
    public OfflineUsers(){        

    }
    
    
    /**
     * Loads the list of offline users from disk and returns the list of usernames if found in the text file.
     * An empty list indicates there is no users found in the file.
     * 
     * @return List of offline usernames to load into the database.  Can be an empty list if there aren't any found.
     */
    public List<String> loadOfflineUsers() {

       DeploymentModeEnum deploymentMode = DashboardProperties.getInstance().getDeploymentMode();

       if(deploymentMode == DeploymentModeEnum.DESKTOP && OFFLINE_USER_LIST_FILE.exists()){
           
           try {                
                String strLine;
    
                try(FileInputStream fstream = new FileInputStream(OFFLINE_USER_LIST_FILE)){
                    try(BufferedReader br = new BufferedReader(new InputStreamReader(fstream))){
                        while ((strLine = br.readLine()) != null) {
                            
                            if (!strLine.startsWith(COMMENT_TOKEN)) {
                                
                                if (validateUsername(strLine)) {
                                    usernames.add(strLine);
                                } else {
                                    logger.error("Unable to add username of: " + strLine + ", because it is not a valid username.");
                                }
                                
                            }
                            
                        }
                    }
                }
                
                
            } catch (FileNotFoundException e) {
                logger.error("Could not find the offline user list file.", e);
            } catch (IOException e) {
                logger.error("Caught IOException while trying to load the offline users.", e );
            } catch (Exception e) {
                logger.error("Caught exception while trying to load the offline users.", e);
            }
           
       }
        return usernames;
        
    }
    
    /**
     * Allows for validation of the username that is read in from the offline user file.  
     * Additional rules could be added later, for now, just checking to ensure the string is lowercase.
     * 
     * @param userLine The line of text representing the username. 
     * @return True if the line is valid for a username, false otherwise.
     */
    private boolean validateUsername(String userLine) {
        
        // Make sure the string is not empty or null.
        if (userLine == null || userLine.isEmpty()) {
            return false;
        }
        
        // Ensure the space is no
        if (userLine.contains(Constants.SPACE)) {
            return false;
        }
        
        // Ensure the string is lowercase. 
        if (userLine.toLowerCase().compareTo(userLine) != 0) {
            return false;
        }
        
        return true;

    }
    
    
    

}
