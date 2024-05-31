/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.user.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DeploymentModeException;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.net.nuxeo.NuxeoInterface;
import mil.arl.gift.net.nuxeo.NuxeoInterface.UserEntityType;
import mil.arl.gift.tools.services.file.FileServices;
import mil.arl.gift.tools.services.user.UserServicesInterface;
import mil.arl.gift.ums.db.UMSDatabaseException;

/**
 * This class contains GIFT user services for server deployment.  Server deployment user services
 * are reliant on not only the UMS database but also whatever content management system has been 
 * integrated with GIFT.
 * 
 * @author mhoffman
 *
 */
public class ServerUserServices implements UserServicesInterface {
    
    /** logger instance */
    private static final Logger logger = LoggerFactory.getLogger(ServerUserServices.class);
    
    /** used to access server file system */
    private static final NuxeoInterface nuxeoInterface = FileServices.nuxeoInterface;
    
    @Override
    public boolean loginUser(String username, boolean createGIFTUser) throws DeploymentModeException, UMSDatabaseException, ProhibitedUserException, IOException{

        boolean userExists = nuxeoInterface.userExists(username);
        if(!userExists && createGIFTUser){
            //create GIFT+nuxeo user
            
            if(CommonProperties.getProhibitedNames().contains(username.toLowerCase())){
                //username is prohibited
                throw new ProhibitedUserException("Unable to create a new user in the database for username of '"+username+"' because that username is prohibited.");
            }
            
            UserEntityType.Properties props = new UserEntityType.Properties(username, username, username, username, username, username);
            UserEntityType user = new UserEntityType(props);
            userExists = nuxeoInterface.createNewUser(user);
            
            if (!userExists) {
                logger.error("Unable to successfully create the new user in Nuxeo.");
            }
        }
        
        // If the user exists but the user workspace somehow no longer exists, then recreate it.
        boolean userWorkspaceExists = nuxeoInterface.documentExists(null, username, true, username);
        if (!userWorkspaceExists && createGIFTUser) {
            UserEntityType.Properties props = new UserEntityType.Properties(username, username, username, username, username, username);
            UserEntityType user = new UserEntityType(props);
            
            if (nuxeoInterface.createUserWorkspace(user) != null) {
                userWorkspaceExists = true;
            } else {
                logger.error("Unable to create the user workspace for user: " + username);
            }
        } 
        
        return userExists && userWorkspaceExists;
    }

}
