/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.usersession.UserSessionCompositeKey;

/**
 * Stores the state of users in the system and maintains a timeout for each for
 * determining when a user goes offline/nonactive.
 *
 * @author jleonard
 */
public class UserStatusModel {

    private static Logger logger = LoggerFactory.getLogger(UserStatusModel.class);

    private final Map<UserSessionCompositeKey, UserSession> users = new HashMap<>();

    private final List<UserStatusListener> listeners = new ArrayList<>();

    /**
     * Returns if a user is online
     *
     * @param userId The ID of the user
     * @return boolean True if the user is online
     */
    public synchronized boolean containsUser(final UserSessionCompositeKey key) {

        return users.containsKey(key);
    }

    /**
     * Add an active user session.
     *
     * @param userSession information about a user session to add
     */
    public synchronized void addUserSession(final UserSession userSession) {

        UserSessionCompositeKey key = new UserSessionCompositeKey(userSession);
        if (!containsUser(key)) {

            
            users.put(key, userSession);

            logger.info("Added user session of " + userSession);

            for (UserStatusListener listener : listeners) {

                try {
                    listener.userStatusAdded(userSession);
                } catch (Exception e) {
                    logger.error("Caught exception from misbehaving listener " + listener, e);
                }
            }
        }

    }

    /**
     * Add a list of active user sessions.
     *
     * @param userSessions collection of user sessions to add
     */
    public synchronized void addUserSessions(final List<UserSession> userSessions) {

        for (UserSession us : userSessions) {
            addUserSession(us);
        }
    }

    /**
     * Remove a list of active user sessions.
     *
     * @param userIds unique user ids to remove
     */
    public synchronized void removeUserSessions(final List<UserSession> userIds) {
        
        if(userIds == null){
            return;
        }

        for (UserSession userId : userIds) {
            removeUserSession(userId);
        }
    }

    /**
     * Private method to remove an inactive user session based on the composite key.
     * External callers should not need to call this directly.
     *
     * @param userId unique user id to remove
     */
    private synchronized void removeUserSession(final UserSessionCompositeKey key) {

        if (containsUser(key)) {

            UserSession us = users.get(key);
            users.remove(key);

            for (UserStatusListener listener : listeners) {

                try {
                    listener.userStatusRemoved(us);
                } catch (Exception e) {
                    logger.error("Caught exception from misbehaving listener " + listener, e);
                }
            }
        }
    }

    /**
     * 
     * Remove an inactive user session.
     * 
     * Function to remove a user session from the map so external callers do not
     * need to build a composite key to do the removal. Instead the user session
     * object to remove can be passed in directly.
     * 
     * @param user
     */
    public synchronized void removeUserSession(final UserSession user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        
        UserSessionCompositeKey key = new UserSessionCompositeKey(user);
        removeUserSession(key);
    }

    /**
     * Adds a listener interested in user status updates
     *
     * @param listener The interested listener
     */
    public synchronized void addListener(final UserStatusListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener no longer interested in user status updates
     *
     * @param listener The uninterested listener
     */
    public synchronized void removeListener(final UserStatusListener listener) {
        if (listeners != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }
}
