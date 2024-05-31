/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.logging.client.LogConfiguration;

import mil.arl.gift.common.gwt.client.WebDeviceUtils;
import mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket;
import mil.arl.gift.common.gwt.client.websocket.CloseEvent;
import mil.arl.gift.common.gwt.shared.websocket.CloseEventCodes;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.shared.websocket.messages.TutorActionMessage;


/**
 * The TutorClientWebSocket is a client side web socket used by the tutor to handle
 * tutor actions from the tutor server.  
 * 
 * @author nblomberg
 *
 */
public class TutorClientWebSocket extends AbstractClientWebSocket {
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(TutorClientWebSocket.class.getName());

    /** The browser session that owns the web socket. */
    private BrowserSession parentSession;
    
    /** Whether or not this client's browser session has missed server actions and fallen out of sync with its server-side session */
    private boolean actionsDesycned = false;

    /** 
     * Constructor
     * @param url The full url to the websocket (should start with "ws://" or "wss://").
     */
    public TutorClientWebSocket(String url) {
        super(url);
    }

    /**
     * Sets the browser session that will us this web socket for client-server communication
     * 
     * @param parent The browser session that this web socket will provide communications for. 
     * Null can be passed in to remove the current browser session as a handler for this socket, which 
     * can be useful when attempting to pass the browser session to another socket after a disconnect.
     */
    public void setBrowserSession(BrowserSession parent){
    	
    	if(parent != null) {
        	setSocketHandler(parent);
    	}
    	
    	parentSession = parent;
    }
    
    @Override
    protected void onError() {
        logger.severe("TutorClientWebSocket::onError() currentState = " + getState());
        
        if(WebDeviceUtils.isMobileAppEmbedded() || parentSession != null) {
            
            /* if this client is inside the GIFT mobile app or if the web socket connection has already been
             * established at least once (i.e. the parent session has been set), then give the client a 
             * chance to reconnect */
            logger.warning("An error in this client's web socket connection was detected. This was likely caused by "
                    + "an unexpected interruption in the connection. Will attempt to reconnect.");
            
        } else if (getState() == WebSocketState.OPENING) {
            
            // The websocket will get closed, but if this hits, then the websocket failed to do an initial connect.
            // Reasons could be such as bad url, url could not be reached, etc.
            // We need to log an error so the tui handles it properly.
            Document.getInstance().displayErrorDialog("Network WebSocket Error", 
                    "Unable to communicate with the Tutor Web Server via websocket protocol. Please check your network connection.",
                    "Connection to the tutor server could not be established via websocket protocol. Verify that the Tutor Module " +
                    "is still running and your computer can access the server via websocket protocol.");
        }
        
        // Per documentation when onError() is called the socket is closed, which calls onClose().  The onClose() method is used
        // to handle the socket for normal closing and during errors.
        super.onError();
    }
    
    @Override
    protected void onClose(CloseEvent event) {
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("TutorClientWebSocket::onClose()");
        }
        
        // Only display an error if the socket is open (eg. not already being shutdown) and if the client did not initiate the close request.
        if ((getState() == WebSocketState.OPEN || getState() == WebSocketState.ERROR) && !isClientNormalCloseRequest()) {
        	
        	Integer code = event.getCode();
        	
        	if(CloseEventCodes.isErroneousClosure(code)){
				
				String reason = CloseEventCodes.getClosureDescription(code);
				
			    String details;
			    
			    if(event.getReason() != null && !event.getReason().isEmpty()){
			    	details = event.getReason();
			    	
			    } else {
			    	details = "Connection to the update server has been lost. Verify that the Tutor Module " +
			                "is still running and your computer is still connected to the Internet.";
			    }
			    
			    logger.severe("The websocket connection was closed unexpectedly.\nReason: Code " 
			    		+  code + " - " + reason + "\nDetails: " + details);
				
                if(!WebDeviceUtils.isMobileAppEmbedded() && parentSession == null) {
                
                    /* if this client is not inside the GIFT mobile app AND the web socket connection has not been
                     * established at least once (i.e. the parent session has not been set), then display an error 
                     * message for the user, since the client will not attempt to reconnect */
                    Document.getInstance().displayErrorDialog(
                            "Network Websocket Error", 
                            reason,
                            details);
                }
			}
        }
        
        super.onClose(event);
        
    }
    
    @Override
    public void onReceiveMessage(AbstractWebSocketMessage message) {        
        
        if (message == null) {
            return;
        }        
        
        if (message instanceof TutorActionMessage) {
            TutorActionMessage tutorAction = (TutorActionMessage)message;
            
            if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                logger.info("applying action from the websocket: " + tutorAction.getAction());
            }
            
            if(parentSession != null) {
                parentSession.receiveActionFromServer(tutorAction.getAction());
            
            } else {
                
                //client has fallen out of sync with its server session, so we'll eventually need to re-synchronize
                setActionsDesynched(true);
                
                logger.warning("Unable to apply action from the web socket because the parent session is not yet ready to receive it: "
                        + tutorAction.getAction());
            }
            
            if(logger.isLoggable(Level.INFO)){
                logger.info("finished handling the server sent message of "+message);
            }
        } 
        
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
    
    /**
     * Gets whether or not this client's browser session has missed server actions and fallen out of sync with its server-side session
     * 
     * @return whether or not this client's browser session has missed server actions and fallen out of sync with its server-side session
     */
    public boolean areActionsDesynched() {
        return actionsDesycned;
    }
    
    /**
     * Sets whether or not this client's browser session has missed server actions and fallen out of sync with its server-side session
     * 
     * @param actionsDesynced whether or not this client's browser session has missed server actions and fallen out of 
     * sync with its server-side session
     */
    public void setActionsDesynched(boolean actionsDesynced) {
        this.actionsDesycned = actionsDesynced;
    }
}