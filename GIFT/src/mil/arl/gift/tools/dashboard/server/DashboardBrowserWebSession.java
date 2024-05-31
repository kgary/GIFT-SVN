/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.aar.LogFilePlaybackService;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.server.SessionStatusListener;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.tools.dashboard.server.messagehandlers.MessageHandlerInterface;
import mil.arl.gift.tools.dashboard.server.webmonitor.WebMonitorService;


/**
 * A dashboard browser web session that has the ability to process incoming
 * ActiveMQ messages.
 *
 * @author nblomberg
 *
 */
public class DashboardBrowserWebSession extends BrowserWebSession implements SessionStatusListener {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(DashboardBrowserWebSession.class);

    /** An optional handler for handling ActiveMQ messages. */
    private MessageHandlerInterface messageHandler = null;

    /**
     * Allow some time before cleaning up the session (in case of page refresh,
     * the websocket will close, but will be recreated and in that case, the
     * browser session will still be valid, just with a new websocket).
     */
    private final long SESSION_CLEANUP_TIMER_MS = DashboardProperties.getInstance().getWebSocketRefreshTimerMs();

    /** Timer used to cancel the browser session. */
    private Timer cancelSessionTimer = null;

    /** Instance to the parent user session manager class that is managing the sessions. */
    private UserSessionManager userSessionMgr = null;

    /**
     * Indicates whether or not the client is automatically applying all
     * suggested strategies. <b>NOTE:</b> Value should only be changed using
     * {@link #setAutoModeEnabled(boolean)}
     */
    private boolean isAutoModeEnabled = true;

    /**
     * The {@link Object} that is used to synchronize access to
     * {@link #isAutoModeEnabled}.
     */
    private final Object autoModeMonitor = new Object();

    /** The knowledge session this session is currently listening to. */
    private AbstractKnowledgeSession knowledgeSession = null;

    /** The currently active {@link LogFilePlaybackService}. */
    private LogFilePlaybackService activePlaybackService = null;
    
    /** The web monitor service registered to this session */
    private WebMonitorService monitorService = null;

    /** The {@link LogMetadata} of the log currently being played. */
    private LogMetadata logMetadata;

    /**
     * Constructor - default
     */
    public DashboardBrowserWebSession(UserSessionManager userSessionManager, String userSessionKey, WebClientInformation clientInfo) {
       super(userSessionKey, clientInfo);

       userSessionMgr = userSessionManager;
       addStatusListener(this);
    }

    /**
     * Handle incoming ActiveMQ messages for this browser session.
     *
     * @param domainMsg The {@link DomainSessionMessageInterface} to handle.
     *        Can't be null.
     * @return True if the message was handled while in auto mode, false
     *         otherwise.
     */
    public boolean handleMessage(DomainSessionMessageInterface domainMsg) {
        if (logger.isTraceEnabled()) {
            logger.trace("handleMessage(" + domainMsg + ")");
        }

        synchronized (autoModeMonitor) {
            if (messageHandler != null) {
                messageHandler.handleMessage(this, domainMsg);
            }

            return isAutoModeEnabled;
        }
    }

    /**
     * Get the ActiveMQ message handler for the session (can be null)
     *
     * @return The ActiveMQ message handler for the session (can be null)
     */
    public MessageHandlerInterface getMessageHandler() {
        return messageHandler;
    }

    /**
     * Set the ActiveMQ message handler for the session. Use null to indicate
     * there is no ActiveMQ message handler needed for this session.
     *
     * @param messageHandler Sets the ActiveMQ message handler for the session.
     *        Can be null to indicate the session doesn't handle ActiveMQ
     *        messages.
     */
    public void setMessageHandler(MessageHandlerInterface messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Getter for the knowledgeSession.
     *
     * @return The value of {@link #knowledgeSession}.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    /**
     * Setter for the knowledgeSession.
     *
     * @param knowledgeSession The new value of {@link #knowledgeSession}.
     * @return The previous value of {@link #knowledgeSession}.
     */
    public AbstractKnowledgeSession setKnowledgeSession(AbstractKnowledgeSession knowledgeSession) {
        AbstractKnowledgeSession oldKnowledgeSession = this.knowledgeSession;
        this.knowledgeSession = knowledgeSession;
        return oldKnowledgeSession;
    }

    /**
     * Getter for the activePlaybackService.
     *
     * @return The value of {@link #activePlaybackService}.  Can be null.
     */
    public LogFilePlaybackService getActivePlaybackService() {
        return activePlaybackService;
    }
    
    /**
     * Setter for the activePlaybackService.
     *
     * @param activePlaybackService The new value of
     *        {@link #activePlaybackService}.
     * @return The previous value of {@link #activePlaybackService}.
     */
    public LogFilePlaybackService setActivePlaybackService(LogFilePlaybackService activePlaybackService) {
        LogFilePlaybackService oldService = this.activePlaybackService;
        this.activePlaybackService = activePlaybackService;
        return oldService;
    }
    
    /**
     * Sets the web monitor service associated with this session. When a session has a monitor
     * service, it can use it to perform monitoring operations like listening for updates
     * from GIFT's modules
     * 
     * @param monitorService the monitor service to assign to this session. Can be null.
     * @return the previous monitor service that was already assigned to this session, if any.
     * Can be null.
     */
    public WebMonitorService setMonitorService(WebMonitorService monitorService) {
        WebMonitorService oldService = this.monitorService;
        this.monitorService = monitorService;
        return oldService;
    }
    
    /**
     * Gets the web monitor service associated with this session. When a session has a monitor
     * service, it can use it to perform monitoring operations like listening for updates
     * from GIFT's modules
     * 
     * @return the web monitor service. Can be null, if this session has not registered
     * to use the web monitor service.
     */
    public WebMonitorService getMonitorService() {
        return monitorService;
    }

    /**
     * Getter for the logMetadata.
     *
     * @return The value of {@link #logMetadata}.
     */
    public LogMetadata getLogMetadata() {
        return logMetadata;
    }

    /**
     * Setter for the logMetadata.
     *
     * @param logMetadata The new value of {@link #logMetadata}.
     * @return The previous value of {@link #logMetadata}.
     */
    public LogMetadata setLogMetadata(LogMetadata logMetadata) {
        LogMetadata oldLogMetadata = this.logMetadata;
        this.logMetadata = logMetadata;
        return oldLogMetadata;
    }

    @Override
    public void setWebSocket(AbstractServerWebSocket webSocket) {

        super.setWebSocket(webSocket);

        // Cancel any session cleanup timer.
        if (cancelSessionTimer != null) {
            logger.debug("setWebSocket() called -- cancelSessionTimer has been canceled.");
            cancelSessionTimer.cancel();
            cancelSessionTimer = null;
        }
    }

    /**
     * Setter for the flag indicating if this {@link DashboardBrowserWebSession}
     * is in auto mode or not. Properly synchronizes the update of the
     * {@link #isAutoModeEnabled} variable.
     *
     * @param isAutoModeEnabled The new value of {@link #isAutoModeEnabled}.
     */
    public void setAutoModeEnabled(boolean isAutoModeEnabled) {
        synchronized (autoModeMonitor) {
            this.isAutoModeEnabled = isAutoModeEnabled;
        }
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[DashboardBrowserWebSession: ");
        sb.append("browserwebSession = ").append(super.toString());
        sb.append("]");
        return sb.toString();

    }

    @Override
    public void onStop() {
        // Do nothing

    }


    @Override
    public void onEnd() {
        if(logger.isDebugEnabled()){
            logger.debug("Browser session is ending, scheduling cleanup timer.");
        }

        /* We don't want to close the browser session immediately, since on a
         * page refresh, the socket will be reestablished. Create a timer to see
         * if the socket connection is reestablished within the proper amount of
         * time before ending the session. */
        cancelSessionTimer = new Timer("Remove Dashboard Browser Session Timer");

        cancelSessionTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                if(logger.isDebugEnabled()){
                    logger.debug("Browser session: " + this + " has ended");
                }
                userSessionMgr.removeBrowserWebSession(DashboardBrowserWebSession.this);

            }


        }, SESSION_CLEANUP_TIMER_MS);

    }
}
