/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.websocket;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.logging.client.LogConfiguration;

import mil.arl.gift.common.gwt.shared.websocket.MessageResponseHandler;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage.MessageSource;
import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;


/**
 * The AbstractClientWebSocket class is the base class that other client side
 * web socket implementations can extend from.  It provides the functionality to
 * serialize/deserialize messages on the socket as well wrapping the jsni methods.
 * 
 * This class was adapted from a presentation from GWTcon 2014 on using gwt serialization
 * with websockets:  https://www.slideshare.net/gwtcon/gwt20-websocket20and20data20serialization
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractClientWebSocket {
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(AbstractClientWebSocket.class.getName());
    
    /** The javascript object instance */
    private JavaScriptObject ws;
    
    /** Boolean to indicate if the client requested the socket to close normally. */
    private boolean clientNormalCloseRequest = false;
    
    /* The message id.  Each message on the client has a unique id.  The message id is unique per websocket and per client or server. 
     * One example where the id is used is when determining which message to reply back to (for async messages).  
     */
    private int currentMessageId = AbstractWebSocketMessage.INVALID_MESSAGE_ID;
    
    /**
     * Map containing a list of message ids with a message response handler.  This allows async messages to be replied back to
     * once the client is done processing the async message.
     */
    private HashMap<Integer, MessageResponseHandler> asyncMessageHandlerMap = new HashMap<Integer, MessageResponseHandler>();
    
    /**
     * Application specific socket states.  This can used to track application specific states that
     * the socket may be in.
     * 
     * @author nblomberg
     *
     */
    public enum WebSocketState {
        OPENING,
        OPEN,
        CLOSING,
        CLOSED,
        ERROR,
        INVALID,
    }
    
    
    /**
     * The raw javascript socket 'readyState'.  These enums come from
     * the docs here:  https://developer.mozilla.org/en-US/docs/Web/API/WebSocket#Ready_state_constants
     * 
     * This is the raw state of the socket in javascript. 
     * 
     * @author nblomberg
     *
     */
    public enum RawSocketState {
        CONNECTING(0),
        OPEN(1),
        CLOSING(2),
        CLOSED(3);
       
        /** the javascript socket state. */
        private int state;
        
        /** 
         * Constructor 
         * @param state The javascript socket state value.
         */
        private RawSocketState(int state) {
            this.state = state;
        }
        
        /** 
         * Returns the state value (int) of the enum.
         * 
         * @return The integer value of the enum which maps to a javascript socket state.
         */
        public int getState() {
            return state;
        }
    }
    
    /** The application socket state (different than the raw socket state).  */
    private WebSocketState socketState = WebSocketState.INVALID;
    
    /** The websocket handler to be notified if the socket is closed/opened */
    private ClientWebSocketHandler handler;
    
    
    /**
     * Constructor - Can be used if there is no external websocket handler needed.
     * 
     * @param url The url to the websocket (should start with "ws://" or "wss://").
     */
    public AbstractClientWebSocket(String url) throws JavaScriptException {
        
        try {
            internalCreate(url);
        } catch(JavaScriptException jsEx) {
            // Rethrow the javascript error.
            throw jsEx;
        }
    }
    
    /**
     * Constructor - Can be used to provide an external client websocket handler.
     * 
     * @param url The url to the websocket (should start with "ws://" or "wss://").
     * @param handler The client websocket handler.
     */
    public AbstractClientWebSocket(String url, ClientWebSocketHandler handler) {
        
       
        if (handler == null) {
            throw new IllegalArgumentException("The client web socket handler cannot be null.");
        }
        
        this.handler = handler;

        try {
            internalCreate(url);
        } catch(JavaScriptException jsEx) {
            this.handler.onJavaScriptException(jsEx);
        }
    }
    
    /**
     * Internally create the websocket.  
     * 
     * @param url The url of the websocket
     * @throws JavaScriptException
     */
    private void internalCreate(String url) throws JavaScriptException {
        setState(WebSocketState.OPENING);
        
        try {
            ws = init(url);
        } catch(JavaScriptException jsEx) {
            // If there's an exception here, the underlying websocket is not valid, so invalidate it here.
            setState(WebSocketState.ERROR);
            ws = null;
            // Rethrow the javascript error, which should be handled by the caller.
            throw jsEx;
        }
    }
    
    /**
     * Sets the handler that should be notified when this socket is opened or closed. If this socket's current state is already {@link WebSocketState#OPEN} or
     * {@link WebSocketState#CLOSED}, then the handler will also be notified appropriately to reflect this socket's current state.
     * 
     * @param handler a handler that will listen for the web socket state
     */
    public void setSocketHandler(ClientWebSocketHandler handler){
    	
    	if (handler == null) {
            throw new IllegalArgumentException("The client web socket handler cannot be null.");
        }
    	
    	this.handler = handler;
    	
    	if(socketState != null){
    		
    		if(socketState == WebSocketState.OPEN){
    			handler.onSocketOpened();
    			
    		} else if(socketState == WebSocketState.CLOSED){
    			handler.onSocketClosed();
    		}
    	}
    }

    /**
     * Called when the socket is closed. 
     * 
     * @param event a close event containing information about why the socket was closed
     */
    protected void onClose(CloseEvent event) {
        
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("AbstractClientWebSocket::onClose()");
        }
        
        setState(WebSocketState.CLOSED);
        
        if (handler != null) {
            handler.onSocketClosed();
        }
        
    }
    
    /**
     * Called when the socket connection is opened. 
     */
    protected void onOpen() {
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("AbstractClientWebSocket::onOpen()");
        }
        
        setState(WebSocketState.OPEN);
        
        if (handler != null) {
            handler.onSocketOpened();
        }
    }
    
    /**
     * Called when there was an error on the socket. 
     */
    protected void onError() {
        logger.severe("AbstractClientWebSocket::onError()");
        
        // Per documentation, the onError is followed by connection termination, which is a close event.
        // So the state of error should be followed by an onclose which will set the state to closed.
        setState(WebSocketState.ERROR);
        
    }
    
    /**
     * Sets the application specific socket state.
     * 
     * @param state Sets the state of the socket.
     */
    protected void setState(WebSocketState state) {
        
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("AbstractClientWebSocket::setState() setting socket state to: " + state);
        }
        
        socketState = state;
    }
    
    /**
     * Gets the application specific socket state.  This may differ than the raw socket state. 
     * See the getRawSocketState() method to get the raw javascript socket state value.
     * 
     * @return The application specific socket state.
     */
    public WebSocketState getState() {
        return socketState;
    }
    
    /**
     * Internal method to handle an incoming raw message on the socket (string based).
     * The raw message is then deserialized using gwt serialization logic and the message handler
     * is called to handle the deserialized message.
     * 
     * @param msg The raw message on the socket. 
     */
    private void onMessage(String msg) {
        
        if (canHandleMessage()) {
            AbstractWebSocketMessage message = WebSocketClientSerializationManager.getInstance().deserializeMessage(msg);
            
            if (message != null) {
                
                if (message instanceof AsyncMessageResponse) {
                    AsyncMessageResponse asyncResponse = ((AsyncMessageResponse) message);
                    Integer originId = asyncResponse.getOriginId();
                    
                    
                    MessageResponseHandler handler = asyncMessageHandlerMap.get(originId);
                    if (handler != null) {
                        handler.onResponse(asyncResponse);
                        
                        asyncMessageHandlerMap.remove(originId);
                    } else {
                        logger.severe("An async message response was received: " + message + ", but there was no message handler found with that response id.");
                    }
                } else if (message instanceof AbstractAsyncWebSocketMessage) {
                    
                    final AbstractAsyncWebSocketMessage asyncMessage = (AbstractAsyncWebSocketMessage)message;
                    
                    AsyncMessageResponse response = new AsyncMessageResponse();
                    // Set the origin message id in the response.
                    response.setOriginId(asyncMessage.getMessageId());
                    onReceiveMessageAsync(asyncMessage, response);
                } else {
                    onReceiveMessage(message);
                }
                
            }
        } else {
            logger.severe("Web socket recevied a message: " + msg + ", but is in an improper state: " + getState());
        }
        
        
    }
    
    /**
     * Add the message handler to the map so when a reponse is received the handler can be called.  
     * The id of the message being sent is entered into the map.  When the message response
     * is received, the id of the message origin is checked against the map to get the correct response
     * handler.
     * 
     * @param message The async message to be sent out.  Cannot be null.
     * @param handler The handler for the message when the response is received.  Cannot be null.
     */
    private void addMessageHandler(AbstractAsyncWebSocketMessage message, MessageResponseHandler handler) {
        
        if (message == null || handler == null) {
            return;
        }
        
        asyncMessageHandlerMap.put(message.getMessageId(), handler);
        
    }
    
    /**
     * Helper method to determine if the websocket should process a message (either incoming or outgoing).  
     * 
     * @return True if the message should be handled, false otherwise.
     */
    public boolean canHandleMessage() {
        boolean canHandleMessage = true;
        
        if (getState() == WebSocketState.CLOSED ||
             getState() == WebSocketState.ERROR ||
             getState() == WebSocketState.INVALID) {
            canHandleMessage = false;
        }

        return canHandleMessage;
    }
    
    /**
     * Helper method to determine if the client is requesting the socket to close normally.
     * 
     * @return True if the client is requesting the socket to close normally.
     */
    public boolean isClientNormalCloseRequest() {
        return clientNormalCloseRequest;
    }
    
    /**
     * Closes the socket (from the web client).  This should typically not be needed to be called if 
     * the server is authoritative in closing the socket.
     */
    public void close() {

        WebSocketState curState = getState();
        if (curState != WebSocketState.CLOSED && curState != WebSocketState.CLOSING &&
                curState != WebSocketState.ERROR) {
            clientNormalCloseRequest = true;
            
            if (ws != null) {
                internalClose();   
            } else {
                // Ignore the close request but log an error.
                logger.severe("close is requested, but websocket is null.");
            }
        }
    }
    
    /**
     * Sends a direct message to the server via the websocket connection.  This method sends a message
     * to the server with no need for a response.
     * 
     * @param message The message to send to the server.
     */
    public void send(AbstractWebSocketMessage message) {
        send(message, null);
        
    }
    
    /**
     * Sends a message to the server via the websocket connection.  This method can be used to send
     * an AbstractAsyncWebSocket message where a response is needed.  If an async message is needed,
     * it should extend off of AbstractAsyncWebSocketMessage and a message response handler should be passed in.
     * 
     * @param message The message to send to the server.  Cannot be null.
     * @param handler (optional) A response handler that MUST be set if sending an async message.
     */
    public void send(AbstractWebSocketMessage message, MessageResponseHandler handler) {
        // Ignore null messages.
        if (message == null) {
            return;
        }
        
        if (message.getSource() != MessageSource.CLIENT) {
            logger.severe("Unable to send message: " + message + ".  Only messages created on the client can be sent to the server.");
            return;
        }
        
        if (canHandleMessage()) {
            
            if (message instanceof AbstractAsyncWebSocketMessage && handler == null) {
                logger.severe("Unable to send message: " + message + ". If sending an async message, a response handler is required.");
                return;
            }
            
            if (!(message instanceof AbstractAsyncWebSocketMessage) && handler != null) {
                logger.severe("Unable to send message: " + message + ". A response handler was passed in, but the message is not an async message.");
                return;
            }
            
            if (message.getMessageId() == AbstractWebSocketMessage.INVALID_MESSAGE_ID) {
                currentMessageId++;
                message.setMessageId(currentMessageId);
            }
            
            
            
            if (message instanceof AbstractAsyncWebSocketMessage && handler != null) {
                AbstractAsyncWebSocketMessage asyncMessage = (AbstractAsyncWebSocketMessage)message;
                addMessageHandler(asyncMessage, handler);
            }
            
            String data = WebSocketClientSerializationManager.getInstance().serializeMessage(message);
            
            if (data != null) {
                send(data);
            }
        } else {
            logger.severe("Unable to send websocket message: " + message + ".  The websocket is in an improper state: " + getState());
        }
    }

    /**
     * Gets the raw javascript socket state value (the 'readyState' of the javascript socket).
     * 
     * @return The raw javascript socket state value.
     */
    private RawSocketState getRawSocketState() {

        // Default to closed state.
        RawSocketState foundState = RawSocketState.CLOSED;
        
        int internalState = internalGetRawSocketState();
        for (RawSocketState value : RawSocketState.values()) {
            if (internalState == value.getState()) {
                foundState = value;
                break;
            }
        }
        
        return foundState;
    }
    
    /**
     * Handler for receiving a web socket message (where no response is needed to the server).
     * This is called after deserialization of the raw data. 
     *  
     * @param message The message that was received by the client.  The parameter cannot be null.
     */
    public abstract void onReceiveMessage(AbstractWebSocketMessage message);
    
    /**
     * Handler for receiving an asynchronous web socket message. This is called after deserialization
     * of the raw message data.  This method should be used when the server
     * requires a response back for an async message.  IMPORTANT - It is up to the implementer
     * to call 'send(response);' after the message has been processed to send the 
     * response back to the server.
     * 
     * @param message The async message that is to be processed by the client.
     * @param response The response that MUST be sent back to the server once the client processes the message.
     * It is critical that the implementer sends the 'response' parameter back to the server via the send(response) method.
     * The implementer should process the message, fill in the response with the appropriate details (such as success/failure, messages
     * and/or optional message response data).  Once the response is filled in, the implementer should call send(response) to send
     * the response back to the server.
     */
    public abstract void onReceiveMessageAsync(AbstractAsyncWebSocketMessage message, AsyncMessageResponse response);
    
    /**
     * Native method to send a message via the javascript websocket.
     * 
     * @param message The message (string format) to be sent to the server.
     */
    private native void send(String message) /*-{
      this.@mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket::ws.send(message);
    }-*/; 
    
    
    /**
     * Native method to get the raw javascript socket state (the 'readyState' of the javascript socket).
     * 
     * @return The int value containing the ready state of the javascript socket.  
     */
    private native int internalGetRawSocketState() /*-{
      return this.@mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket::ws.readyState;
    }-*/;
    
    /**
     * Native method to close the javascript socket.
     */
    private native void internalClose() /*-{
      this.@mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket::ws.close();
    }-*/;
    
    
    /**
     * Native method to create a javascript web socket.  This sets up the handlers of the socket.
     * 
     * @param url The url to connect the websocket to.  
     * @return The created javascript websocket object.
     */
    private native JavaScriptObject init(String url) /*-{
        var websocket = new WebSocket(url);
        var wrapper = this;
        websocket.onopen = function(evt) {
          wrapper.@mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket::onOpen()();
        };
        
        websocket.onclose = function(evt) {
            wrapper.@mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket::onClose(Lmil/arl/gift/common/gwt/client/websocket/CloseEvent;)(evt);
        };
        
        websocket.onmessage = function(evt) {
            wrapper.@mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket::onMessage(Ljava/lang/String;)(evt.data);
        };
        
        websocket.onerror = function(evt) {
            wrapper.@mil.arl.gift.common.gwt.client.websocket.AbstractClientWebSocket::onError()();
        };
        
        return websocket;
        
    }-*/;
}