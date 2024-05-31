/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.common.gwt.server.websocket.ServerWebSocketHandler;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;

/**
 * A web socket used by the dashboard server that is used to push websocket messages to the web client.  It can also
 * receive web socket messages from the client.
 * 
 * @author nblomberg
 *
 */
public class DashboardServerWebSocket extends AbstractServerWebSocket {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(DashboardServerWebSocket.class.getName());

    /** 
     * Constructor - default
     */
    public DashboardServerWebSocket(ServerWebSocketHandler handler) {
        super(handler);
        logger.debug("DashboardServerWebSocket()");
        
        /* Need to ensure Game Master updates aren't sent too quickly for the client to handle,
         * since these updates are sent frequently and need to be handled by the UI */
        setThrottleToMatchClient(true);
    }

    @Override
    public void onReceiveMessage(AbstractWebSocketMessage message) {
        
        DashboardMessage dashboardMessage = (DashboardMessage) message;
        String browserSessionKey = dashboardMessage.getBrowserSessionKey();
        
        UserWebSession session = UserSessionManager.getInstance().getUserSessionByBrowserKey(browserSessionKey);
        
        if (session != null) {
            
            //forward the received message to the appropriate user session so that it can be handled appropriately
            session.handleClientMessage(dashboardMessage, browserSessionKey);
            
        } else {
            logger.warn("onReceiveMessageAsync session = null");
        }

    }

    @Override
    public void onReceiveMessageAsync(AbstractAsyncWebSocketMessage message, final AsyncMessageResponse response) {
        logger.error("onReceiveMessageAsync() - Received an async websocket message from the client, but there is no handler implemented yet.");

    }
   
}
