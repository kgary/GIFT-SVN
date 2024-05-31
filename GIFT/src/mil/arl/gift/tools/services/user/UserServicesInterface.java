/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.user;

import java.io.IOException;

import mil.arl.gift.common.DeploymentModeException;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.ums.db.UMSDatabaseException;

/**
 * This class contains GIFT user services to abstract the logic involved with 
 * the various deployment modes of GIFT.
 * 
 * @author mhoffman
 *
 */
public interface UserServicesInterface {

    /**
     * Logs the specified user into GIFT.  Depending on the parameters the user maybe created in the system.
     * 
     * @param username the username to login
     * @param createGIFTUser whether or not the user should be created if they don't already exist in the system
     * @return whether the user was logged in and created if needed and requested
     * @throws DeploymentModeException if the GIFT deployment mode doesn't support this operation
     * @throws UMSDatabaseException if there was a problem interacting with the GIFT UMS database
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     * @throws IOException if there was a problem creating the new user
     */
    public boolean loginUser(String username, boolean createGIFTUser) 
            throws DeploymentModeException, UMSDatabaseException, ProhibitedUserException, IOException;
}
