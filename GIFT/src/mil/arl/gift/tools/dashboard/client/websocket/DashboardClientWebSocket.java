/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.logging.client.LogConfiguration;

import mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket;
import mil.arl.gift.common.gwt.client.websocket.ClientWebSocketHandler;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;
import mil.arl.gift.tools.dashboard.client.BrowserSession;


/**
 * The DashboardClientWebSocket is a client side web socket used by the dashboard to establish
 * a bi-directional communication to the server and can be used to send server notifications
 * to the client browsers.  It is used alongside gwt rpc and is not intended to replace it.
 * 
 * @author nblomberg
 *
 */
public class DashboardClientWebSocket extends AbstractClientWebSocket {
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(DashboardClientWebSocket.class.getName());

    /** The browser session that owns the web socket. */
    private BrowserSession parentSession;

    /** 
     * Constructor
     * @param url The full url to the websocket (should start with "ws://" or "wss://").
     * @param parent The browser session that owns the web socket (cannot be null).
     */
    public DashboardClientWebSocket(String url, ClientWebSocketHandler handler, BrowserSession parent) {
        
        super(url, handler);

        if (parent == null) {
            throw new IllegalArgumentException("The browser session cannot be null.");
        }
        
        parentSession = parent;
    }
    
    @Override
    public void onReceiveMessage(AbstractWebSocketMessage message) {
        
        if (message == null) {
            return;
        }

        parentSession.onReceiveMessage(message);
    }

    @Override
    public void onReceiveMessageAsync(final AbstractAsyncWebSocketMessage message, final AsyncMessageResponse response) {
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("onReceiveMessageAsync(): " + message);
        }

        if (message == null) {
            return;
        }
        
        // Do nothing here yet.
        
    }
}