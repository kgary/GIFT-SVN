/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.user.desktop;

import java.io.File;

import mil.arl.gift.common.DeploymentModeException;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.tools.services.user.UserServicesInterface;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.table.DbUser;

/**
 * This class contains GIFT user services for desktop deployment.  Desktop deployment user services
 * are reliant on the UMS database.
 * 
 * @author mhoffman
 *
 */
public class DesktopUserServices implements UserServicesInterface {
    
    private static final File LOCAL_WORKSPACE_DIRECTORY = new File(CommonProperties.getInstance().getWorkspaceDirectory());  
    
    @Override
    public boolean loginUser(String username, boolean createGIFTUser) throws DeploymentModeException, UMSDatabaseException, ProhibitedUserException{

        //check if user exists
        DbUser dbUser = UMSDatabaseManager.getInstance().getUserByUsername(username, true);
        if(dbUser == null){
            throw new UMSDatabaseException("Failed to retrieve the user's information for user name of "+username+".");
        }
        
        //create workspace folder
        File usersWorkspaceFolder = new File(LOCAL_WORKSPACE_DIRECTORY + File.separator + username);
        if(!usersWorkspaceFolder.exists()){
            usersWorkspaceFolder.mkdir();
        }
        
        return true;

    }

}
