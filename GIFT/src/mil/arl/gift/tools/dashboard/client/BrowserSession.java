/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.EndKnowledgeSessionRequest;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.course.strategy.StrategyStateUpdate;
import mil.arl.gift.common.gwt.client.BrowserProperties;
import mil.arl.gift.common.gwt.client.BrowserSessionHandler;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.WebDeviceUtils;
import mil.arl.gift.common.gwt.client.websocket.ClientWebSocketHandler;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.gwt.shared.websocket.MessageResponseHandler;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleMessageProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleStateProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.UsersStateProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.BookmarkProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.GeolocationProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.StrategyProvider;
import mil.arl.gift.tools.dashboard.client.websocket.DashboardClientWebSocket;
import mil.arl.gift.tools.dashboard.shared.messages.AddKnowledgeSession;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardAsyncMessage;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.DetonationUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.EntityStateUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.InitializationMessage;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedBookmarkCache;
import mil.arl.gift.tools.dashboard.shared.messages.RemoveEntityMessage;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.ModuleStatusUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.UsersStatusUpdate;


/**
 * Represents a session of the browser. This is a singleton.
 *
 * @author nblomberg
 */
public class BrowserSession implements ClientWebSocketHandler {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(BrowserSession.class.getName());

    /**
     * Create a remote service proxy to talk to the server-side dashboard
     * service.
     */
    @SuppressWarnings("unused")
    private final DashboardServiceAsync dashboardService = GWT.create(DashboardService.class);

    private final String userSessionKey;

    private final String browserSessionKey;

    /**
     * properties needed by the client and provided by the server
     * Note: can be null if the properties were not retrieved
     */
    private static ServerProperties serverProperties;

    /** The instance of the browser session */
    private static BrowserSession instance;

    /** The websocket used for this browser session. */
    private DashboardClientWebSocket webSocket = null;

    /** Reference to the handler for browser session events.  */
    private BrowserSessionHandler handler = null;

    /**
     * Gets the instance of the browser session
     *
     * @return BrowserSession The current instance of the browser session.
     */
    public static BrowserSession getInstance() {
        logger.fine("instance = " + instance);
        return instance;
    }

    /**
     * Sets the server properties
     */
    public static void setServerProperties(ServerProperties serverPropertiesIn) {
        serverProperties = serverPropertiesIn;
    }

    /**
     * Constructor
     *
     * @param userSessionKey The user session that belongs to the browser.
     * @param browserSessionKey The browser session ID
     * @param bsh (optional) Browser session handler to listen for key events such as browser session failure.
     */
    private BrowserSession(String userSessionKey, String browserSessionKey,  BrowserSessionHandler bsh) {
        this.userSessionKey = userSessionKey;
        this.browserSessionKey = browserSessionKey;
        BrowserProperties.getInstance().setUserSessionKey(userSessionKey);

        handler = bsh;

        // Get the suburl for where the websocket url is.
        String wsSubUrl = serverProperties.getDashboardWebSocketUrl();
        // Use the current url in the webbrowser as the starting point to determine the websocket url.
        String currentUrl = GWT.getHostPageBaseURL();

        String scheme = "ws";

        if(serverProperties.shouldUseHttps()){
            scheme = "wss"; //if the dashboard is using HTTPs, we also need to use a secure ws connection
        }

        // Construct the full websocket url.
        // An example should be something like: ws://192.168.0.1:8080/dashboard/websocket?browserSessionId=1dcc000b-1b0d-4131-b88a-614686fb88a7
        String webSocketUrl = scheme + currentUrl.substring(currentUrl.indexOf("://")) + wsSubUrl;

        logger.info("Creating websocket: " + webSocketUrl + "?browserSessionId=" + browserSessionKey);
        webSocket = new DashboardClientWebSocket(webSocketUrl + "?browserSessionId=" + browserSessionKey, this, this);


    }

    public void closeWebSocket() {


        logger.info("Client is requesting to close the web socket.");

        // Just cleanup the websocket, don't call the handler.
        cleanupWebSocket();
    }

    /**
     * Cleans up the websocket from the browser session.
     */
    private void cleanupWebSocket() {
        if (webSocket != null) {
            webSocket.close();
        }
    }

    @Override
    public void onSocketClosed() {
        logger.info("onSocketClosed()");
        
        if(!WebDeviceUtils.isMobileAppEmbedded()) {
            
            // Clean up the web socket connection for non-mobile devices.
            // Mobile devices should ignore this logic in case the TUI re-establishes its web socket connection during a course.
            cleanupWebSocket();
    
            if (handler != null) {
    
                if (webSocket != null) {
                    logger.info("isClientNormalCloseRequest = " + webSocket.isClientNormalCloseRequest());
                }
    
                if (webSocket == null) {
                   handler.onWebSocketClosed();
                } else {
                    // If the websocket is closing normally (from a client side request to close), then
                    // don't call onWebSocketClosed() since it is not an error.
                    if (!webSocket.isClientNormalCloseRequest()) {
                        handler.onWebSocketClosed();
                    }
                }
            }
    
            if (webSocket != null) {
                webSocket = null;
            }
        }
    }

    @Override
    public void onSocketOpened() {
        // Execute any delayed logic (if specified) once the web socket is opened.
        if (handler != null) {
            handler.onBrowserSessionReady();
        }

    }

    @Override
    public void onJavaScriptException(JavaScriptException ex) {

        // Invalidate the webSocket on a javascript exception error.
        webSocket = null;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("Unable to establish websocket due to the JavaScriptException: ").append(ex.getDescription()).append("<br/>")
            .append("<br/>")
            .append("Check the value of the WebSocketUrl property in dashboard.properties file to ensure it is configured correctly.<br/>")
            .append("You can also try changing the IP address in the address bar (e.g. 10.0.0.1).<br/>")
            .append("Replacing the IP address with localhost may fix the issue but this replacement may result in undefined behavior.");

        if (handler != null) {
            handler.onWebSocketJsExceptionError(ex);
        }

    }

    /**
     * Resumes a user session if it is still active on the server
     *
     * @param userSessionKey The user session key of the user session to resume
     * @param bsh (optional) Optional callback for listening for key browser session events.
     */
    public static void createBrowserSession(final String userSessionKey, final String browserSessionKey, final BrowserSessionHandler bsh) {

        instance = new BrowserSession(userSessionKey, browserSessionKey, bsh);

    }

    /**
     * Process the given websocket message
     * @param message the message received
     */
    public void onReceiveMessage(AbstractWebSocketMessage message) {

        if(message != null) {

            if(message instanceof DashboardMessage) {

                DashboardMessage dashMsg = (DashboardMessage) message;
                final Serializable payload = dashMsg.getPayload();
                final int domainSessionId = dashMsg.getDomainSessionId();
                final long msgTimestamp = dashMsg.getTimestamp();

                final AbstractKnowledgeSession knowledgeSession = dashMsg.getKnowledgeSession();
                if (knowledgeSession != null && knowledgeSession.inPastSessionMode()) {
                    TimelineProvider.getInstance().setPlaybackTime(msgTimestamp);
                }

                if (payload instanceof AddKnowledgeSession) {
                    AddKnowledgeSession addSessionMsg = (AddKnowledgeSession) payload;
                    ActiveSessionProvider.getInstance().addActiveSession(addSessionMsg.getKnowledgeSession());
                } else if (payload instanceof InitializationMessage) {
                    InitializationMessage initMessage = (InitializationMessage) payload;
                    StrategyProvider.getInstance().setPresetStrategies(initMessage.getAssociatedStrategies(), initMessage.getUnAssociatedStrategies(), domainSessionId);
                    SessionStateProvider.getInstance().sessionStateUpdate(initMessage.getLastLearnerState(), domainSessionId);
                    
                } else if (payload instanceof KnowledgeSessionState) {
                    
                    KnowledgeSessionState state = (KnowledgeSessionState) payload;
                    SessionStateProvider.getInstance().sessionStateUpdate(state, domainSessionId);
                    
                    if(state.getLearnerState() != null && state.getLearnerState().getPerformance() != null
                            && knowledgeSession != null && !knowledgeSession.inPastSessionMode()) {
                        
                        PerformanceState perfState = state.getLearnerState().getPerformance();
                        
                        /* if this is an active session, we need to notify the appropriate locations when
                         * global bookmarks are processed as part of a learner state */
                        if(perfState.getObserverComment() != null || perfState.getObserverMedia() != null) {
                            
                            BookmarkProvider.getInstance().addBookmark(domainSessionId, perfState.getEvaluator(), 
                                    msgTimestamp, perfState.getObserverComment(), perfState.getObserverMedia(), false);
                            
                            /* Cache bookmarks as they are received */
                            final ProcessedBookmarkCache cache = new ProcessedBookmarkCache(perfState.getObserverComment(),
                                    perfState.getObserverMedia(), knowledgeSession, msgTimestamp, perfState.getEvaluator());

                            /* Send to cache */
                            UiManager.getInstance().getDashboardService().cacheProcessedBookmark(BrowserSession.getInstance().getBrowserSessionKey(), cache,
                                    new AsyncCallback<GenericRpcResponse<Void>>() {
                                        @Override
                                        public void onSuccess(GenericRpcResponse<Void> response) {
                                            // do nothing
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {
                                            throw new DetailedException("Caching the processed bookmark failed.",
                                                    "Failed to cache the bookmark " + cache + " because " + t.getMessage(), t);
                                        }
                                    });
                        }
                    }
                    
                } else if (payload instanceof StrategyStateUpdate) {
                    
                    StrategyStateUpdate strategyStateUpdate = (StrategyStateUpdate) payload;
                    
                    if(knowledgeSession != null && !knowledgeSession.inPastSessionMode()) {
                        
                        /* Only add strategies when they are received if this is not a past session. Past sessions already 
                         * populate the strategies when they are loaded, so adding them here would duplicate them. */
                        StrategyProvider.getInstance().addSuggestedStrategy(strategyStateUpdate.getPendingStrategies(),
                                domainSessionId, strategyStateUpdate.getEvaluator(), msgTimestamp);
                        StrategyProvider.getInstance().addAppliedStrategies(strategyStateUpdate.getAppliedStrategies(),
                                domainSessionId, strategyStateUpdate.getEvaluator(), msgTimestamp);
                    }
                } else if (payload instanceof ExecuteOCStrategy) {
                    ExecuteOCStrategy ocStrategy = (ExecuteOCStrategy) payload;
                    StrategyProvider.getInstance().executeOcStrategy(ocStrategy, domainSessionId, msgTimestamp);
                } else if (payload instanceof EntityStateUpdate) {
                    GeolocationProvider.getInstance().entityLocationUpdate((EntityStateUpdate) payload);
                } else if (payload instanceof DetonationUpdate) {
                    GeolocationProvider.getInstance().detonationLocationUpdate((DetonationUpdate) payload);
                } else if (payload instanceof RemoveEntityMessage){
                    GeolocationProvider.getInstance().removeEntityRequest((RemoveEntityMessage) payload);
                } else if (payload instanceof EndKnowledgeSessionRequest) {
                    ActiveSessionProvider.getInstance().endActiveSession(domainSessionId);
                    
                } else if (payload instanceof ModuleStatusUpdate) {
                    ModuleStateProvider.getInstance().setModuleStatuses(((ModuleStatusUpdate) payload).getModuleToQueueNames());
                    
                } else if (payload instanceof UsersStatusUpdate) {
                    UsersStateProvider.getInstance().setUsersStatus((UsersStatusUpdate) payload);
                    
                } else if (payload instanceof AbstractMessageUpdate) {
                    ModuleMessageProvider.getInstance().update((AbstractMessageUpdate) payload);
                    
                } else {
                    logger.severe("Received unhandled dashboard message: " + message.toString());
                }

            } else {
                logger.warning("Received unhandled web socket message: " + message.toString());
            }
        }
    }

    /**
     * Sends a websocket message to the web client (where a response is not needed).  The message is serialized via the websocket
     * and sent to the client.
     *
     * @param message The message to send to the client.  The message is 1 way (no response is returned).
     */
    public void sendWebSocketMessage(DashboardMessage message) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("sendWebSocketMessage(" + message + ")");
        }

        if (webSocket != null) {

            // Send the message to the client via the websocket.
            message.setBrowserSessionKey(browserSessionKey);
            webSocket.send(message);
        } else {
            logger.warning("Unable to send the message because the web socket is null.");
        }

    }

    /**
     * Used to send an async message to the server via websockets (where a response is needed).
     *
     * @param message The message to send.
     * @param messageHandler The handler for the message when a response is returned from the server.
     */
    public void sendAsyncWebSocketMessage(DashboardAsyncMessage message, MessageResponseHandler messageHandler) {
        if (webSocket != null) {

            // Send the message to the client via the websocket.
            message.setBrowserSessionKey(browserSessionKey);
            webSocket.send(message, messageHandler);
        } else {
            logger.warning("Unable to send the message because the web socket is null.");
        }
    }

    /**
     * Retrieves the key for the current user session
     *
     * @return the user session key
     */
    public String getUserSessionKey() {
        return userSessionKey;
    }

    /**
     * Retrieves the key for the current browser session
     *
     * @return the browser session key
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }
}
