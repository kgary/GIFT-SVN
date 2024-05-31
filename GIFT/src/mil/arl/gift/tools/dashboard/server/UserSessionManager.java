/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.server.SessionStatusListener;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.metrics.MetricsSender;
import mil.arl.gift.tools.services.ServicesManager;

/**
 * Singleton class for managing user web sessions on the dashboard jetty servlet.
 *
 * @author nblomberg
 *
 */
public class UserSessionManager {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(UserSessionManager.class);

    /** mapping of unique UserWebSession key to the user web session */
    private final Map<String, UserWebSession> userIdToUserSessionMap = new ConcurrentHashMap<>();

    /** mapping of unique usernames to userweb session.  LTI users are not put into this map because LTI users don't have usernames. */
    private final Map<String, UserWebSession> userNameToUserSessionMap = new ConcurrentHashMap<>();

    /** mapping of browser session ID to BrowserSession.  This should contain all active browser sessions. */
    private final Map<String, BrowserWebSession> sessionIdToBrowserSessionMap = new ConcurrentHashMap<>();

    /** Mapping of lti user sessions, this can't be stored in the userNameToSessionMap, because LTI sessions don't have usernames.
     *  Non-LTI user sessions are not stored in this map */
    private final Map<String, UserWebSession> ltiUserToUserSessionMap = new ConcurrentHashMap<>();

    /**
     * The metrics sender is responsible for sending metrics of the number of
     * sessions
     */
    private final static MetricsSender metrics = new MetricsSender("dashboard");

    /** Tracks the number of active browser sessions on the server */
    private final static AtomicInteger currentSessions = new AtomicInteger();

    /**
     * Timer which is called to update the metrics sender with the current
     * session value
     */
    private static Timer updateSessionCounter = new Timer("updateSessionCounter");

    /** Delay of when the session timer is started in milliseconds */
    private static final int SESSIONTIMER_INITIALDELAY_MS = 1000;

    /**
     * Interval in which the session timer is repeatedly called in milliseconds
     */
    private static final int SESSIONTIMER_INTERVAL_MS = 5000;

    private final String LTI_ID_DELIMITER = ":";

    /** Singleton instance of the user session manager. */
    private static UserSessionManager instance = null;

    /**
     * Listeners interested in status changes of a session
     */
    private final Set<BrowserSessionListener> statusListeners = new HashSet<>();

    /**
     * Private constructor used to enforce the singleton pattern
     */
    private UserSessionManager() {
        currentSessions.set(0);

        metrics.startSending();

        /* We need to put the session counter on a timer, because the
         * MetricsSender cannot be accessed on the sessionDestroyed thread. So
         * we will just pump the metrics sender manually with the current
         * session value. We cannot update the metrics sender on another thread
         * (such as when sessionDestroyed is called) because the thread doesn't
         * have the metrics sender in proper context. */
        updateSessionCounter.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                // Update the counter with the current value.
                metrics.updateMetricCounter("currentSessions", currentSessions.get());
            }

        }, SESSIONTIMER_INITIALDELAY_MS, SESSIONTIMER_INTERVAL_MS);
    }

    /**
     * Creates the instance of the singleton class.  This should be done once during initialization.
     */
    public static void createInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
    }

    /**
     * Gets the instance of the singleton.  Can return null, the caller must call 'createInstance' prior
     * to trying to access the manager.
     *
     * @return Instance of the singleton class, can be null if the singleton has not been created.
     */
    public static UserSessionManager getInstance() {
        if (instance == null) {
            logger.error("Singleton instance is null.  Please make sure to call createInstance() during initialization prior to using the class.");
        }

        return instance;
    }

    /**
     * Removes the user web session
     *
     * @param session The dashboard user session to be removed.
     */
    public void removeUserWebSession(UserWebSession session) {

        if (session == null) {
            logger.error("removeUserWebSession() called, but the session is null.");
            return;
        }

        if(logger.isDebugEnabled()){
            logger.debug("removeUserWebSession(): User(" + session.getUserSessionKey() + ")");
        }
        userIdToUserSessionMap.remove(session.getUserSessionKey());

        DashboardHttpSessionData sessionData = session.getUserSessionInfo();
        if (sessionData != null) {
            if (sessionData instanceof LtiUserSession) {
                LtiUserSession ltiSession = (LtiUserSession)sessionData;
                ltiUserToUserSessionMap.remove(getLtiUniqueId(ltiSession));
            } else {
                userNameToUserSessionMap.remove(sessionData.getUserName());
            }
        } else {
            logger.error("removeUserWebSession() called, but the user session info is null.");
        }

        if(logger.isDebugEnabled()){
            logger.debug("removeUserWebSession():  user session size=" + userIdToUserSessionMap.size());
        }

    }

    /**
     * Gets the browser session for a given browser session key
     *
     * @param browserSessionKey The browser session key
     * @return BrowserSession The browser session
     */
    public BrowserWebSession getBrowserSession(String browserSessionKey) {
        return sessionIdToBrowserSessionMap.get(browserSessionKey);
    }

    public Iterable<String> getBrowserSessionKeys() {
        return sessionIdToBrowserSessionMap.keySet();
    }

    /**
     * Creates a user session
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      to associate a web sesison with
     * @return UserSession The created user session
     */
    public UserWebSession createUserSession(final DashboardHttpSessionData userSession, String clientAddress) {

        final UserWebSession session = new UserWebSession(userSession, userSession.getUserSessionId());

        // Map the sessions by user session id &
        userIdToUserSessionMap.put(session.getUserSessionKey(), session);

        // LTI user sessions are put into an additional separate map because LTI users don't have a username.
        // For other sessions, map the sessions by username.
        if (userSession instanceof LtiUserSession) {
            LtiUserSession ltiSession = (LtiUserSession)userSession;
            ltiUserToUserSessionMap.put(getLtiUniqueId(ltiSession), session);
        } else {
            userNameToUserSessionMap.put(userSession.getUserName(), session);
        }

        if(logger.isDebugEnabled()){
            logger.debug("createUserSession():  Adding user session: " + session.getUserSessionKey()+ "user session size= " + userIdToUserSessionMap.size());
        }

        session.addStatusListener(new SessionStatusListener() {

            @Override
            public void onStop() {
                if(logger.isDebugEnabled()){
                    logger.debug("Removing user session from collection because the session has stopped.  "+userSession);
                }

                userIdToUserSessionMap.remove(session.getUserSessionKey());

                if (userSession instanceof LtiUserSession) {
                    LtiUserSession ltiSession = (LtiUserSession)userSession;
                    String uniqueKey = getLtiUniqueId(ltiSession);
                    ltiUserToUserSessionMap.remove(uniqueKey);
                } else {
                    userNameToUserSessionMap.remove(userSession.getUserName());
                }
            }

            @Override
            public void onEnd() {
            }
        });
        return session;
    }

    /**
     * Gets the user web session by username.  This is only valid for non LTI user sessions since
     * LTI users don't have a username.
     *
     * @param userName Username to lookup
     * @return The session if found, null otherwise.
     */
    public UserWebSession getUserWebSessionByUserName(String userName) {
        return userNameToUserSessionMap.get(userName);
    }

    /**
     * Creates a browser session for a user session
     *
     * @param userSession The user session to create a browser session
     * @param clientAddress The client address for this create session request
     * @return BrowserSession The created browser session
     */
    public BrowserWebSession createBrowserSession(UserWebSession userSession, String clientAddress) {

        WebClientInformation clientInfo = new WebClientInformation();
        clientInfo.setClientAddress(clientAddress);

        final BrowserWebSession browserSession = new DashboardBrowserWebSession(this, userSession.getUserSessionKey(), clientInfo);

        if(logger.isDebugEnabled()){
            logger.debug("createBrowserSession for user(" + userSession + ", browserSession: " + browserSession);
        }
        sessionIdToBrowserSessionMap.put(browserSession.getBrowserSessionKey(), browserSession);
        currentSessions.getAndSet(sessionIdToBrowserSessionMap.size());
        userSession.addBrowserSession(browserSession);

        return browserSession;
    }

    /**
     * Gets the user session for a given user session key
     *
     * @param userSessionKey The user session key
     * @return UserSession The user web session
     */
    public UserWebSession getUserSession(final String userSessionKey) {
        UserWebSession userSession = userIdToUserSessionMap.get(userSessionKey);
        return userSession;
    }

    /**
     * Gets the user web session for a given browser key
     *
     * @param browserSessionKey The unique identifier of the browser session for
     *        which to fetc the {@link UserWebSession}
     * @return The user web session
     */
    public UserWebSession getUserSessionByBrowserKey(final String browserSessionKey ) {
        UserWebSession userWebSession = null;
        BrowserWebSession browserSession = sessionIdToBrowserSessionMap.get(browserSessionKey);
        if(browserSession != null) {
            userWebSession = userIdToUserSessionMap.get(browserSession.getUserSessionKey());
        } else {
            logger.warn("BrowserWebSession does not exist for key " + browserSessionKey);
        }

        return userWebSession;
    }

    /**
     * Send given websocket message to all user sessions
     * @param message message to send
     */
    public void broadcastWebsocketeMsgToUsers(AbstractWebSocketMessage message) {
        for (UserWebSession session : userIdToUserSessionMap.values()) {
            session.broadcastWebSocketMsgToBrowsers(message);
        }
    }

    /**
     * Adds a listener for the browsersession.
     *
     * @param listener The listener to be notified of browser session changes.
     */
    public void addStatusListener(BrowserSessionListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " - addStatusListener() " + listener);
        }

        synchronized (statusListeners) {
            statusListeners.add(listener);
        }
    }

    /**
     * Removes a listener for the browsersession changes.
     *
     * @param listener The listener to be notified of browser session changes.
     */
    public void removeStatusListener(BrowserSessionListener listener) {

        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " - removeStatusListener() " + listener);
        }
        synchronized (statusListeners) {
            statusListeners.remove(listener);
        }
    }

    /**
     * Removes a browser session from the session manager.
     *
     * @param session The browser session to be removed.  Cannot be null.
     */
    public void removeBrowserWebSession(BrowserWebSession session) {

        if (session == null) {
            logger.error("removeBrowserWebSession() called with null session.");
            return;
        }

        String browserId = session.getBrowserSessionKey();
        if(logger.isDebugEnabled()){
            logger.debug("removing session: " + browserId);
        }

        // Notify any listeners that the browser session is ending and being cleaned up.
        synchronized (statusListeners) {
            for (BrowserSessionListener listener : statusListeners) {
                listener.onBrowserSessionEnding(session);
            }
        }

        UserWebSession userWebSession = userIdToUserSessionMap.get(session.getUserSessionKey());
        if (userWebSession != null) {
            userWebSession.removeBrowserSession(session);

            if (!userWebSession.hasBrowserSessions()) {
                // In the future we may want some sort of notification/close session logic here to notify
                // any subscribers that the user session is ending.  For now the dashboard does not use it, so
                // we can simply clean it up.
                removeUserWebSession(userWebSession);
            }
            
            //unlock any file resources that are currently locked by the browser session being removed
            ServicesManager.getInstance().getFileServices().unlockAllFiles(userWebSession.getUserSessionInfo().getUserName(), browserId);
        }

        sessionIdToBrowserSessionMap.remove(browserId);
        currentSessions.getAndSet(sessionIdToBrowserSessionMap.size());
    }

    /**
     * Get a unique id based on LTI consumer key and consumer id.
     * For LTI user sessions, we want to store a unique id based on the consumer key/consumer id internally.
     *
     * @param consumerKey The LTI consumer key
     * @param consumerId The LTI consumer id
     * @return String A unique id based on the consumer key and consumer id.
     */
    private String getLtiUniqueId(String consumerKey, String consumerId) {
        return consumerKey + LTI_ID_DELIMITER + consumerId;
    }

    /**
     * Get a unique id based on the LtiUserSession data
     * For LTI user sessions, we want to store a unique id based on the consumer key/consumer id internally.
     *
     * @param ltiSession
     * @return
     */
    private String getLtiUniqueId(LtiUserSession ltiSession) {
        return getLtiUniqueId(ltiSession.getConsumerKey(), ltiSession.getConsumerId());
    }

    /**
     * Gets the LtiUserSession data for a consumer key and consumer id (if it exists).
     *
     * @param consumerKey The consumer key to lookup
     * @param consumerId The consumer id to lookup
     * @return The LtiUserSession data if found (null otherwise).
     */
    public LtiUserSession getLtiUser(String consumerKey, String consumerId) {

        LtiUserSession data = null;
        String uniqueId = getLtiUniqueId(consumerKey, consumerId);

        UserWebSession userSession = ltiUserToUserSessionMap.get(uniqueId);
        if (userSession != null) {
            DashboardHttpSessionData sessionInfo = userSession.getUserSessionInfo();
            if (sessionInfo != null && sessionInfo instanceof LtiUserSession) {
                data = (LtiUserSession)sessionInfo;
            }

        }

        return data;

    }

    /**
     * Gets the UserWebSession data for an LTI User session based on the consumerKey
     * and consumerId.  Can return null if not found.
     *
     * @param consumerKey The consumerKey to lookup
     * @param consumerId The consumerId to lookup
     * @return UserWebSession The user web session data if found.  Null if not found.
     */
    public UserWebSession getLtiUserWebSession(String consumerKey, String consumerId) {
        String uniqueId = getLtiUniqueId(consumerKey, consumerId);
        UserWebSession userSession = ltiUserToUserSessionMap.get(uniqueId);
        return userSession;
    }

}
