/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.common.gwt.server.websocket.ServerWebSocketHandler;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;


/**
 * The BrowserWebSession class is responsible for managing the connection between the web browser and the server.
 * It is responsible for receiving/sending messages via the websocket interface.
 * 
 * A UserWebSession can have multiple BrowserWebSessions, which means a user can belong to more than one browser sessions.
 * In terms of messaging, as messages are received from a browserwebsession to be processed, it is passed to the parent userwebsession.
 * Conversely, when a browserwebsession receives a message from the server to send to the client, 
 * then it sends it via the websocket interface.  The wesocket interface allows for bi-directional communication between the client
 * and server without the need of long polling.  
 * 
 * The gwt-rpc methods can still be used (see TutorUserInterfaceServiceImpl.java for more examples), but in cases where bi-directional
 * communication is needed, the websocket interface is available.
 * 
 *
 * @author jleonard
 */
public class BrowserWebSession extends AbstractWebSession implements ServerWebSocketHandler {

    /**
     * instance of the logger
     */
    private static Logger logger = LoggerFactory.getLogger(BrowserWebSession.class);

    private String sessionKey = UUID.randomUUID().toString();

    private final String userSessionKey;
    
    /** information about the client browser */
    private final WebClientInformation clientInfo;

    /** An instance of the web socket associated with the browser web session. */
    AbstractServerWebSocket webSocket = null;
        
    /**
     * Constructor
     *
     * @param userSessionKey The ID of the user session associated with this browser session
     * @param clientInfo information about the client browser
     */
    public BrowserWebSession(String userSessionKey, WebClientInformation clientInfo) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("new browser session created: " + userSessionKey);
        }
        
        if(userSessionKey == null){
            throw new IllegalArgumentException("The user session key can't be null.");
        }
        this.userSessionKey = userSessionKey;
        
        if(clientInfo == null){
            throw new IllegalArgumentException("The client info can't be null.");
        }
        this.clientInfo = clientInfo;
    }
    
    /**
     * Gets the the browser session key
     *
     * @return String The browser session key
     */
    public String getBrowserSessionKey() {
        return sessionKey;
    }

    /**
     * Returns the user session for this browser session
     *
     * @return The user session key for this browser session
     */
    public String getUserSessionKey() {
        return userSessionKey;
    }
    
    /**
     * Returns information about the client for this browser session.
     * 
     * @return WebClientInformation
     */
    public WebClientInformation getClientInformation(){
        return clientInfo;
    }
    
    /**
     * Accessor to get the web socket associated with the session.
     * 
     * @return The websocket associated with the session (can be null if the socket doesn't exist yet).
     */
    public AbstractServerWebSocket getWebSocket() {
        return this.webSocket;
    }


    /**
     * Set the web socket associated with the session. 
     * 
     * @param webSocket The websocket that belongs to the session. 
     */
    public void setWebSocket(AbstractServerWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    protected synchronized void onSessionStopping() {
    }

    @Override
    protected synchronized void onSessionEnding() {
       
    }

    @Override
    protected void onSessionEnded() {
    }

    /**
     * Sends a websocket message to the web client.  The message is serialized via the websocket
     * and sent to the client.
     * 
     * @param message The message to send to the client.
     */
    public void sendWebSocketMessage(AbstractWebSocketMessage message) {
        
        if (webSocket != null) {
            
            // Send the message to the client via the websocket.  
            webSocket.send(message);
        } else {
            logger.warn("Unable to apply message because the web socket is null.");
        }
       
    }

	@Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        sb.append("[BrowserWebSession: ");
        sb.append("userSessionKey = ").append(getUserSessionKey());
        sb.append(", browserSession = ").append(getBrowserSessionKey());
        sb.append(", status = ").append(getSessionStatus());
        sb.append(", clientInfo = ").append(getClientInformation());

        sb.append("]");
        return sb.toString();
       
    }

    @Override 
    public void endSession(){
    	super.endSession();
    }    

    @Override
    public void onSocketClosed(AbstractServerWebSocket socket) {
        endSession();
    }

    @Override
    public void onSocketOpened(AbstractServerWebSocket socket) {
        
        if(this.webSocket == null || !this.webSocket.equals(socket)) {
            
            // A new web socket connection has been established for this session. This can happen upon re-connecting to the server.
            setWebSocket(socket);
        }
    }

    @Override
    protected void onSessionStopped() {
        // do nothing.
        
    }

}
