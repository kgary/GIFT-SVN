/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider.SessionStateUpdateHandler;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;

/**
 * A singleton class that manages knowledge session state updates.
 * 
 * @author sharrison
 */
public class SessionStateProvider extends AbstractProvider<SessionStateUpdateHandler> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionStateProvider.class.getName());

    /** The instance of this class */
    private static SessionStateProvider instance = null;
    
    /** The last set of tasks that listeners were requested to show */
    private Set<Integer> shownTaskIds = null;

    /**
     * Singleton constructor
     */
    private SessionStateProvider() {
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
    public static SessionStateProvider getInstance() {
        if (instance == null) {
            instance = new SessionStateProvider();
        }

        return instance;
    }

    /**
     * The knowledge session state has been updated.
     * 
     * @param state contains data about the knowledge session state.
     * @param domainSessionId the knowledge session domain id.
     */
    public void sessionStateUpdate(final KnowledgeSessionState state, final int domainSessionId) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("sessionStateUpdate(");
            List<Object> params = Arrays.<Object>asList(state, domainSessionId);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (state == null) {
            throw new IllegalArgumentException("The parameter 'state' cannot be null.");
        }

        /* Check if the session pushing the knowledge session update is
         * whitelisted */
        if (RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {

            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<SessionStateUpdateHandler>() {
                @Override
                public void execute(SessionStateUpdateHandler handler) {
                    handler.sessionStateUpdate(state, domainSessionId);
                }
            });
        }
    }
    
    /**
     * Notifies listeners to explicitly display the details of the given tasks, effectively selecting it
     * 
     * @param taskIds the IDs of the tasks whose details should be displayed. 
     * If null or empty, no tasks should be explicitly displayed.
     */
    public void showTasks(final Set<Integer> taskIds) {
        
        shownTaskIds = taskIds;
        
        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<SessionStateUpdateHandler>() {
            @Override
            public void execute(SessionStateUpdateHandler handler) {
                handler.showTasks(taskIds);
            }
        });
    }  

    /**
     * Gets the last set of tasks that listeners were requested to show
     * 
     * @return the shown tasks. Can be null or empty.
     */
    public Set<Integer> getShownTaskIds() {
        return shownTaskIds;
    }

    /**
     * Handler for listening to knowledge session state updates.
     * 
     * @author sharrison
     */
    public interface SessionStateUpdateHandler {
        /**
         * The knowledge session state has been updated.
         * 
         * @param state contains data about the knowledge session state. Can't
         *        be null.
         * @param domainSessionId the domain session id for the knowledge
         *        session.
         */
        void sessionStateUpdate(KnowledgeSessionState state, int domainSessionId);

        /**
         * Handles when the details of the given tasks should be displayed
         * 
         * @param taskIds the IDs of the tasks whose details should be displayed. 
         * If null or empty, no tasks should be explicitly displayed.
         */
        void showTasks(Set<Integer> taskIds);
    }
}
