/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.server.AbstractWebSession;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInfoCacheData.DomainSessionKey;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.EndSessionRequest;

/**
 * A UserWebSession represents the user's web presence in the dashboard.
 * A user web session can have multiple browser sessions (eg, a user
 * can have multiple browsers opened at the same time.
 *
 *
 * So a UserWebSession has a 1 to many relationship to BrowserWebSessions.
 *
 * This class is responsible primarily for handling messages from the WebMonitorModule for a specific
 * user and updating the the browsers associated with the user.
 * Conversely, it is responsible for handling messages received by a browser client and passing
 * those back to the WebMonitorModule (and/or other browser clients that are associated for the user).
 *
 *
 * @author nblomberg
 */
public class UserWebSession extends AbstractWebSession {

    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(UserWebSession.class);

    /** A reference to the singleton instance of {@link UserSessionManager} */
    private static final UserSessionManager userSessionManager = UserSessionManager.getInstance();

    private String sessionKey;

    /** information about the user session this web session is associated with */
    private final DashboardHttpSessionData userSessionInfo;

    /**
     * A user web session can have multiple browser sessions associated with it.
     * This means that the user can have multiple browsers, but each browser session is associated
     * with only 1 user.  This is a 1 to many relationship.
     */
    private final Set<BrowserWebSession> browserSessions = new HashSet<>();

    /**
     * Creates a new user web session
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      this web session is associated with
     */
    public UserWebSession(DashboardHttpSessionData userSession, String userSessionKey) {
        userSessionInfo = userSession;
        sessionKey = userSession.getUserSessionId();

    }

    /**
     * Gets the user session data associated with the user web session. This can
     * be LTI specific data, or contain information about username depending on
     * the type of user.
     *
     * @return The {@link DashboardHttpSessionData} value of
     *         {@link #userSessionInfo}.
     */
    public DashboardHttpSessionData getUserSessionInfo() {
        return userSessionInfo;
    }

    /**
     * Returns if the user web session has any connected browser sessions.
     * @return True if there are one or more browser sessions associated with the user.  False otherwise.
     */
    public boolean hasBrowserSessions() {
        return !browserSessions.isEmpty();
    }

    /**
     * Gets the key of this user session;
     *
     * @return String The key of the user session
     */
    public String getUserSessionKey() {
        return sessionKey;
    }

    @Override
    protected void onSessionStopping() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSessionStopped() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSessionEnding() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSessionEnded() {
        // TODO Auto-generated method stub

    }

    /**
     * Adds a browser session that is part of this user session
     *
     * @param session The browser session that is part of this user session
     */
    public void addBrowserSession(BrowserWebSession session) {
        if(logger.isDebugEnabled()){
            logger.debug("addBrowserSession(): User(" + this.getUserSessionKey() + "), Browser (" + session.getBrowserSessionKey() + ")");
        }
        synchronized (browserSessions) {
            browserSessions.add(session);
        }
        if(logger.isDebugEnabled()){
            logger.debug("addBrowserSession(): browsersession size=" + browserSessions.size());
        }
    }

    /**
     * Removes a browser session that is part of this user session
     *
     * @param session The browser session that is part of this user session
     */
    public void removeBrowserSession(BrowserWebSession session) {
        logger.debug("removeBrowserSession(): User(" + this.getUserSessionKey() + "), Browser (" + session.getBrowserSessionKey() + ")");
        synchronized (browserSessions) {
            browserSessions.remove(session);
        }

        if(logger.isDebugEnabled()){
            logger.debug("removeBrowserSession(): browsersession size=" + browserSessions.size());
        }
    }

    /**
     * Send given message to all browsers
     * @param message message to send
     */
    public void broadcastWebSocketMsgToBrowsers(AbstractWebSocketMessage message) {

        for (BrowserWebSession session : browserSessions) {
            session.sendWebSocketMessage(message);
        }
    }

    /**
     * Processes an incoming message from the web client.
     *
     * @param message The message received
     * @param browserSessionKey The unique identifier of the browser that
     *        produced the message to handle.
     * @return The {@link RpcResponse} that was produced in response to the
     *         received message. Can't be null.
     */
    public final RpcResponse handleClientMessage(AbstractWebSocketMessage message, String browserSessionKey) {

        RpcResponse response = null;

        try{
            final DashboardMessage dashboardMessage = (DashboardMessage) message;
            final BrowserWebSession browserSession = userSessionManager.getBrowserSession(browserSessionKey);
            if (browserSession instanceof DashboardBrowserWebSession) {
                DashboardBrowserWebSession dashSession = (DashboardBrowserWebSession) browserSession;
                final DomainSessionKey key = new DomainSessionKey(dashSession.getKnowledgeSession());

                /* send the payload to the appropriate domain session */
                Object payload = dashboardMessage.getPayload();
                if (payload instanceof EvaluatorUpdateRequest) {
                    final EvaluatorUpdateRequest updateRequest = (EvaluatorUpdateRequest) payload;
                    WebMonitorModule.getInstance().sendEvaluatorUpdateRequest(updateRequest, key);
                } else if (payload instanceof ApplyStrategies) {
                    final ApplyStrategies applyStrategies = (ApplyStrategies) payload;
                    WebMonitorModule.getInstance().sendApplyStrategies(applyStrategies, key);
                } else if (payload instanceof EndSessionRequest) {
                    final EndSessionRequest endSessionRequest = (EndSessionRequest) payload;
                    WebMonitorModule.getInstance().sendEndSessionRequest(endSessionRequest, key);
                }

                response = new RpcResponse(sessionKey, browserSessionKey, true, "success");
            } else {
                response = new RpcResponse(sessionKey, browserSessionKey, false, "No DashboardBrowserWebSession found");
            }

        }catch(Throwable t){
            logger.error("Exception caught in handleClientMessage with the message: " + t.getMessage()+".  The message is "+message+" for session "+sessionKey, t);
            response = new RpcResponse(sessionKey, browserSessionKey, false, "An exception thrown when trying to apply the message on the server.");
            response.setAdditionalInformation("The error reads:\n"+t.getMessage());
        }

        return response;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[UserWebSession: ");
        sb.append("dashboardSessionInfo = ").append(userSessionInfo);
        sb.append(", userSessionId = ").append(sessionKey);

        sb.append("]");
        return sb.toString();
    }

}
