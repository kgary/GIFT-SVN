/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.UsersStateProvider.UsersStatusChangeHandler;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.UsersStatusUpdate;

/**
 * A provider that provides user session status information to any registered handlers
 * 
 * @author nroberts
 */
public class UsersStateProvider extends AbstractProvider<UsersStatusChangeHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(UsersStateProvider.class.getName());

    /** The instance of this class */
    private static UsersStateProvider instance = null;

    /** A mapping from each user session ID to its associated session */
    private Map<Integer, UserSession> userSessions = new HashMap<>();
    
    /** A mapping from each active domain session ID to its associated session */
    private Map<Integer, DomainSession> domainSessions = new HashMap<>();

    /**
     * Singleton constructor
     */
    private UsersStateProvider() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     * 
     * @return the instance to the provider singleton object.
     */
    public static UsersStateProvider getInstance() {
        if (instance == null) {
            instance = new UsersStateProvider();
        }

        return instance;
    }

    /**
     * Updates the user statuses stored by this provider and sends and notifies all
     * registered handlers that said statuses have changed
     * 
     * @param update the update containing the user statuses. Cannot be null.
     */
    public void setUsersStatus(final UsersStatusUpdate update) {
        boolean changed = !this.userSessions.equals(update.getUserSessions()) 
                || !this.domainSessions.equals(update.getActiveDomainSessions());
        this.userSessions.clear();
        this.domainSessions.clear();
        this.userSessions.putAll(update.getUserSessions());
        this.domainSessions.putAll(update.getActiveDomainSessions());

        if (changed) {
            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<UsersStatusChangeHandler>() {
                @Override
                public void execute(UsersStatusChangeHandler handler) {
                    handler.onUsersChanged(userSessions, domainSessions);
                }
            });
        }
    }
    
    /**
     * Gets the user statuses stored by this provider, i.e. a mapping from each user ID
     * to the session associated with it
     * 
     * @return the current user statuses. Will not be null, but can be empty.
     */
    public Map<Integer, UserSession> getUserSessions(){
        return userSessions;
    }
    
    /**
     * Gets the active domain session statuses stored by this provider, i.e. a mapping from each session ID
     * to the session associated with it
     * 
     * @return the current active domain session statuses. Will not be null, but can be empty.
     */
    public Map<Integer, DomainSession> getActiveDomainSessions(){
        return domainSessions;
    }

    /**
     * A handler used to handle user session status updates
     * 
     * @author nroberts
     */
    public interface UsersStatusChangeHandler {
        
        /**
         * Notifies this listener that the given user sessions statuses changed
         * 
         * @param userSessions the user session statuses. Cannot be null.
         * @param domainSessions the active domain session statuses. Cannot be null.
         */
        void onUsersChanged(Map<Integer, UserSession> userSessions, Map<Integer, DomainSession> domainSessions);
    }
}
