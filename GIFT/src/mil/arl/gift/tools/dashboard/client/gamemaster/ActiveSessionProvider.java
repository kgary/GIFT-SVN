/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;

/**
 * A singleton class that handles changes to the active sessions.
 * 
 * @author sharrison
 */
public class ActiveSessionProvider extends AbstractProvider<ActiveSessionChangeHandler> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ActiveSessionProvider.class.getName());

    /** The instance of the class */
    private static ActiveSessionProvider instance = null;

    /** The collection of active knowledge sessions */
    private Map<Integer, SessionWrapper> knowledgeSessionMap = new HashMap<>();

    /**
     * The states for active sessions. Live action sessions can only be Running
     * (if ended it is permanently removed); whereas playback can be Running or
     * Ended.
     * 
     * @author sharrison
     */
    public enum RunState {
        /** Currently being run */
        RUNNING,

        /**
         * Playback only! Finished running, but can potentially start up again.
         */
        PLAYBACK_ENDED;
    }

    /**
     * Singleton constructor
     */
    private ActiveSessionProvider() {
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
    public static ActiveSessionProvider getInstance() {
        if (instance == null) {
            instance = new ActiveSessionProvider();
        }

        return instance;
    }

    /**
     * Checks if the domain session id maps to an active knowledge session.
     * 
     * @param domainSessionId the id of the domain session to check.
     * @return true if there is an active knowledge session for the domain
     *         session id; false otherwise.
     */
    public boolean isActiveSession(int domainSessionId) {
        return knowledgeSessionMap.containsKey(domainSessionId);
    }

    /**
     * Return the run state of the session with the given id.
     * 
     * @param domainSessionId the id of the domain session to check.
     * @return the {@link RunState} of the session. Can be null if the session
     *         is not found.
     */
    public RunState getRunState(int domainSessionId) {
        SessionWrapper wrapper = knowledgeSessionMap.get(domainSessionId);
        return wrapper == null ? null : wrapper.getRunState();
    }

    /**
     * Get the complete list of active knowledge sessions.
     * 
     * @return the list of active knowledge sessions.
     */
    public Set<AbstractKnowledgeSession> getActiveKnowledgeSessions() {
        Set<AbstractKnowledgeSession> sessions = new HashSet<>();

        for (SessionWrapper wrapper : knowledgeSessionMap.values()) {
            sessions.add(wrapper.getKnowledgeSession());
        }

        return sessions;
    }

    /**
     * Get the active knowledge session that is mapped to the provided domain
     * session id.
     * 
     * @param domainSessionId the id of the domain session to lookup.
     * @return the active knowledge session if it exists; null if it does't
     *         exist.
     */
    public AbstractKnowledgeSession getActiveSessionFromDomainSessionId(int domainSessionId) {
        SessionWrapper wrapper = knowledgeSessionMap.get(domainSessionId);
        return wrapper == null ? null : wrapper.getKnowledgeSession();
    }

    /**
     * Add the provided knowledge session to the active session list.
     * 
     * @param knowledgeSession the session to add. Can't be null.
     */
    public void addActiveSession(final AbstractKnowledgeSession knowledgeSession) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addActiveSession(" + knowledgeSession + ")");
        }

        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        int hostDomainSessionId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        final SessionWrapper wrapper = knowledgeSessionMap.get(hostDomainSessionId);
        if (wrapper != null) {
            if (wrapper.getRunState().equals(RunState.RUNNING)) {
                /* Map already contains a running knowledge session for this
                 * domain */
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Already added. Skipping.");
                }
                return;
            }

            /* Set the run state to 'running'. */
            wrapper.setRunState(RunState.RUNNING);
        } else {
            /* Create new */
            knowledgeSessionMap.put(hostDomainSessionId, new SessionWrapper(knowledgeSession, RunState.RUNNING));
        }

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<ActiveSessionChangeHandler>() {
            @Override
            public void execute(ActiveSessionChangeHandler handler) {
                handler.sessionAdded(knowledgeSession);
            }
        });
    }

    /**
     * Ends the provided knowledge session.
     * 
     * @param domainSessionId the knowledge session domain id.
     */
    public void endActiveSession(int domainSessionId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("endActiveSession(" + domainSessionId + ")");
        }

        final SessionWrapper wrapper = knowledgeSessionMap.get(domainSessionId);
        if (wrapper == null) {
            /* Map doesn't contain a knowledge session for this domain */
            return;
        }

        final AbstractKnowledgeSession knowledgeSession = wrapper.getKnowledgeSession();
        if (knowledgeSession.inPastSessionMode()) {
            wrapper.setRunState(RunState.PLAYBACK_ENDED);
        } else {
            knowledgeSessionMap.remove(domainSessionId);
        }

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<ActiveSessionChangeHandler>() {
            @Override
            public void execute(ActiveSessionChangeHandler handler) {
                handler.sessionEnded(knowledgeSession);
            }
        });
    }

    /**
     * Terminate playback session.
     * 
     * @param domainSessionId the knowledge session domain id.
     */
    public void terminatePlayback(int domainSessionId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("terminatePlayback(" + domainSessionId + ")");
        }

        final SessionWrapper wrapper = knowledgeSessionMap.get(domainSessionId);
        if (wrapper == null) {
            /* Map doesn't contain a knowledge session for this domain */
            return;
        }

        final AbstractKnowledgeSession knowledgeSession = wrapper.getKnowledgeSession();
        if (!knowledgeSession.inPastSessionMode()) {
            throw new IllegalArgumentException("The knowledge session must be a 'Past' session type.");
        }

        knowledgeSessionMap.remove(domainSessionId);

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<ActiveSessionChangeHandler>() {
            @Override
            public void execute(ActiveSessionChangeHandler handler) {
                handler.sessionEnded(knowledgeSession);
            }
        });
    }

    /**
     * Clear all active sessions
     */
    public void clearActiveSessions() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("clearActiveSessions()");
        }

        /* Remove each session */
        Map<Integer, SessionWrapper> mapCopy = new HashMap<>(knowledgeSessionMap);
        for (Entry<Integer, SessionWrapper> entry : mapCopy.entrySet()) {
            if (entry.getValue().getKnowledgeSession().inPastSessionMode()) {
                terminatePlayback(entry.getKey());
            } else {
                endActiveSession(entry.getKey());
            }
        }
    }

    /**
     * Handler for listening to changes in the active knowledge session.
     * 
     * @author sharrison
     */
    public interface ActiveSessionChangeHandler {
        /**
         * Session has become active.
         * 
         * @param knowledgeSession the active session.
         */
        void sessionAdded(AbstractKnowledgeSession knowledgeSession);

        /**
         * Session has become inactive.
         * 
         * @param knowledgeSession the inactive session.
         */
        void sessionEnded(AbstractKnowledgeSession knowledgeSession);
    }

    /**
     * Wrapper class to bundle the knowledge session with other values.
     * 
     * @author sharrison
     */
    private class SessionWrapper {
        /** The knowledge session */
        AbstractKnowledgeSession knowledgeSession;

        /** The state as to whether or not the knowledge session is running */
        RunState runState;

        /**
         * Constructor.
         * 
         * @param knowledgeSession the knowledge session to monitor. Can't be
         *        null.
         * @param runState the state of the session. Can't be null.
         */
        public SessionWrapper(AbstractKnowledgeSession knowledgeSession, RunState runState) {
            if (knowledgeSession == null) {
                throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
            }

            this.knowledgeSession = knowledgeSession;
            setRunState(runState);
        }

        /**
         * Get the knowledge session
         * 
         * @return the knowledge session.
         */
        public AbstractKnowledgeSession getKnowledgeSession() {
            return knowledgeSession;
        }

        /**
         * Get the current {@link RunState} of the session.
         * 
         * @return the {@link RunState}.
         */
        public RunState getRunState() {
            return runState;
        }

        /**
         * Set the current {@link RunState} of the session.
         * 
         * @param runState the {@link RunState}. Can't be null.
         */
        public void setRunState(RunState runState) {
            if (runState == null) {
                throw new IllegalArgumentException("The parameter 'runState' cannot be null.");
            }

            this.runState = runState;
        }
    }
}
