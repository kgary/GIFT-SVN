/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import mil.arl.gift.common.UserSession;

/**
 * For registering with the UserStatusModel to receive a callback when a user
 * goes online or offline.
 *
 * @author jleonard
 */
public interface UserStatusListener {
    
    /**
     * Notify listener that a user has been added
     * 
     * @param userSession - user session info.
     */
    void userStatusAdded(UserSession userSession);
    
    /**
     * Notify listener that a user has been removed
     * 
     * @param userSession - user session info.
     */
    void userStatusRemoved(UserSession userSession);
}
