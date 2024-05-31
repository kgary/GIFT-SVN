/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.gwt.shared.websocket.CloseEventCodes;
import mil.arl.gift.common.gwt.shared.websocket.MessageResponseHandler;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage.MessageSource;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractServerWebSocket is the server side web socket implementation that
 * all other web socket classes should extend from.  This class uses the Jetty 7 
 * implementation of websockets.  
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractServerWebSocket implements WebSocketListener{
    
    /** The timeout (in milliseconds) in which web socket connections can be idle without being cleaned up */
    private static final int IDLE_TIMEOUT = 30000;
    
    /** Value for the maximum text message size to send over the websocket */
    private static final int MAX_TEXT_MESSAGE_SIZE = 100000;
    
    /** 
     * The byte data that is sent with ping messages. The exact data that is passed doesn't really matter, but it
     * should be identifiable and consistent, since the data sent back from the accompanying pong message should match.
     */
    public static byte[] PING_DATA = "--PINGPONG--".getBytes();

    /** The number of milliseconds to wait between sending ping messages to the client. Used to avoid flooding the client with pings. */
    private static final long PING_DELAY_MILLIS = 1000;

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(AbstractServerWebSocket.class.getName());
    
    private static final Timer pingTimer = new Timer("Web Socket Ping Timer", true);

    /** The connection that is used to send data to the client (or receive data from the client). */
    private Session connection;
    
    /** Handler used to respond to changes when the socket is opened or closed. */
    private ServerWebSocketHandler handler;
    
    /** Flag used to indicated if a null connection was closed or has not yet been opened. */
    private boolean hasClosed = false;
    
    /**
     * The server message id.  Each message on the server has a unique id.  The message id is guaranteed to be unique
     * for each web socket.  The server message ids may collide with client message ids, so the source of the message (CLIENT or SERVER)
     * must be checked to determine the origin of the message.  This also currently assumes each web socket will have a 1:1 mapping to the server and client.
     * In the future, if a websocket is going to send messages to multiple clients, this will need to be changed.  
     * 
     */
    public static AtomicInteger currentMessageId = new AtomicInteger(AbstractWebSocketMessage.INVALID_MESSAGE_ID);

    /**
     * Map containing a list of message ids with a message response handler.  This allows async messages to be replied back to
     * once the client is done processing the async message.
     */
    private ConcurrentHashMap<Integer, MessageResponseHandler> asyncMessageHandlerMap = new ConcurrentHashMap<Integer, MessageResponseHandler>();
    
    /** 
     * A task that periodically sends ping messages to this web socket's client to keep it's TCP connection alive and detect if
     * it is no longer responsive. If the client does not send an accompanying pong message after {@link #MAX_UNHANDLED_PING_COUNT}
     * ping attempts have been made, then the client will be considered disconnected and their web socket connection will be closed.
     */
    private final TimerTask pingTask = new TimerTask() {
        
        @Override
        public void run() {
            
            /* Ping the client to see if it is still responsive and keep the connection alive. If the client
             * does not respond within the idle timeout, then the server framework will automatically
             * terminate the websocket connection and end the browser session */
            ping();
        }
    };

    /** 
     * Whether this socket should wait until each message has been handled by the client before 
     * sending the next message. This can be helpful for contexts like Game Master where a lot of
     * updates need to be pushed by the server and then rendered by the client.
     */
    private boolean throttleToMatchClient = false;
    
    /**
     * Constructor 
     */
    public AbstractServerWebSocket() {

    }
    
    /**
     * Constructor 
     * 
     * @param handler The handler used to respond to the socket changes (such as opened/closed).  Cannot be null.
     */
    public AbstractServerWebSocket(ServerWebSocketHandler handler) {
        
        setSocketHandler(handler);
    }
    
    /**
     * Sets the handler that should be notified when the web socket changes, such as by opening or closing. If this socket is already 
     * open or closed, then the handler will also be notified appropriately to reflect this socket's current state.
     * 
     * @param handler The handler used to respond to the socket changes (such as opened/closed). If null is passed in, the current
     * handler will be removed from this socket and will no longer respond to its changes, which can be useful when passing an existing
     * handler to a new socket upon reconnecting to the server.
     */
    public void setSocketHandler(ServerWebSocketHandler handler){
    	 
         this.handler = handler;
         
         if(handler != null) {
             
             if(this.connection != null){
            	 handler.onSocketOpened(this);
            	 
             } else if(this.hasClosed){
            	 handler.onSocketClosed(this);
             }
         }
    }
    
    /**
     * Gets the handler that is notified when the web socket changes, such as by opening or closing.
     * 
     * @return the handler for this web socket
     */
    public ServerWebSocketHandler getSocketHandler() {
        return this.handler;
    }
    
    @Override
    public void onWebSocketClose(int code, String message) {
        
        if(logger.isInfoEnabled()){
            logger.info("onClose() called with code(" + code + "), message(" + message + ")");
        }
        
        if(CloseEventCodes.isErroneousClosure(code)) {
            logger.warn("A websocket connection was closed unexpectedly.\nReason: Code " 
                    +  code + " - " + message + "\nDetails: " + CloseEventCodes.getClosureDescription(code));
        }
        
        if (connection != null) {
            connection = null;
            
            if(handler != null){
            	handler.onSocketClosed(this);
            }
            
            hasClosed = true;
        }
        
        //stop periodically pinging the client, since it is no longer connected
        suspendHeartbeat();
    }
    @Override
    public void onWebSocketConnect(Session connection) {

        if(logger.isInfoEnabled()){
            logger.info("onOpen() called with connection: " + connection);
        }
        
        // Set the internal connection.
        if (connection != null) {
            this.connection = connection;
            
            if(handler != null){
            	handler.onSocketOpened(this);
            }
             
            /* Set a timeout for how long websocket connections can remain idle (i.e. not send/receive
             * any messages) before their connections are closed.
             * 
             * In Jetty 7.6.2, we were using a value of -1 for this to avoid a timeout altogether, but as
             * of Jetty 9, there is no way to disable the timeout timer. Using a negative value now throws 
             * an error. That said, preventing the idle timeout is no longer necessary now that a pinging 
             * mechanism has been added to our sockets, since the ping/pong frames sent every second 
             * will keep the idle timeout from occurring. */
            this.connection.getPolicy().setIdleTimeout(IDLE_TIMEOUT);
            
            this.connection.getPolicy().setMaxTextMessageSize(MAX_TEXT_MESSAGE_SIZE);
            
            //start periodically pinging the client to keep its TCP connection alive and determine if it becomes unresponsive
            pingTimer.scheduleAtFixedRate(pingTask, PING_DELAY_MILLIS, PING_DELAY_MILLIS);
        }        
    }

    @Override
    public void onWebSocketText(String msg) {
        
        if(logger.isInfoEnabled()){
            logger.info("onMessage() received: " + msg);
        }

        
        if (connection != null) {
            AbstractWebSocketMessage message = WebSocketServerSerializationManager.getInstance().deserializeMessage(msg);

            if (message != null) {
                
                if (message instanceof AsyncMessageResponse) {
                    AsyncMessageResponse asyncResponse = ((AsyncMessageResponse) message);
                    Integer originId = asyncResponse.getOriginId();
                    
                    
                    MessageResponseHandler handler = asyncMessageHandlerMap.get(originId);
                    if (handler != null) {
                        handler.onResponse(asyncResponse);
                        
                        asyncMessageHandlerMap.remove(originId);
                    } else {
                        logger.error("An async message response was received: " + message + ", but there was no message handler found with that response id.");
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
            logger.warn("The websocket received a message, but the connection is null.  Message will be ignored: " + msg);
        }
    }
    
    /**
     * Sends a ping message (see {@link #PING_OPCODE}) control frame to this web socket's client. If the client is still connected to
     * this web socket and is responsive, then its web socket protocol will automatically send back an accompanying pong message 
     * containing the same data that was sent in the original ping message.
     * <br/><br/>
     * Ping messages can be sent periodically to keep the TCP connection between the client and server's web sockets alive and to
     * detect when either the client or the server has stop responding to web socket traffic, which often indicates an unexpected
     * disconnect from one or both sides of the TCP connection.
     */
    private void ping() {
        
        if(connection != null) {
            
            try {
                connection.getRemote().sendPing(ByteBuffer.wrap(PING_DATA));
                
            } catch (IOException e) {
                logger.warn("Exception caught while pinging a web socket's client.", e);
            }
            
        } else {
            logger.warn("Unable to ping client due to missing frame connection.");
        }
    }
    
    public void close() {
        if (connection != null) {
            
            if(logger.isInfoEnabled()){
                logger.info("closing connection.");
            }
            connection.close();
        }
    }

    /**
     * Internal method to send a serialized message to the client.  The serialization method being used
     * is gwt serialization, and it is expected that the message (in String format) is already serialized
     * via the gwt serialization logic.
     * 
     * @param msg The message to be sent to the web client.
     */
    private void sendMessage(String msg) {
        if (connection != null && msg != null) {
            try {
                
                long sendTime = System.currentTimeMillis();
                
                if(!throttleToMatchClient ) {
                    
                    /* If the message is sent not being throttled, just do a basic send to the remote endpoint */
                    connection.getRemote().sendString(msg);
                    
                } else {
                    
                    /* If the message is being throttled to match the client, need to suspend this thread until 
                     * the client resolves the request, or else we can't ensure synchronization. 
                     * 
                     * This addresses #5506 */
                    synchronized(connection) { 
                        
                        /* Use a return blocker to ensure we suspend sending more messages until after the client 
                         * handles this message. This throttles the send rate to match the client's receive rate.*/
                        final AsyncReturnBlocker<Throwable> returnCallback = new AsyncReturnBlocker<>();
                        
                        /* Use a callback to determine when the write finishes */
                        WriteCallback writeCallback = new WriteCallback() {
                            
                            @Override
                            public void writeSuccess() {
                                returnCallback.setReturnValue(null);
                            }
                            
                            @Override
                            public void writeFailed(Throwable failure) {
                                returnCallback.setReturnValue(failure);
                            }
                        };
                        
                        /* Send to the remote endpoint with the write callback */
                        connection.getRemote().sendString(msg, writeCallback);
                        
                        /* Wait for the result. If the result is an exception, throw it */
                        Throwable returnError = returnCallback.getReturnValueOrTimeout();
                        if(returnError != null) {
                            throw returnError;
                        }
                    }
                    
                    long writeTime = System.currentTimeMillis() - sendTime;
                    if(writeTime > IDLE_TIMEOUT) {
                        logger.warn("Detected abnormally long socket write time of " + writeTime + ", which may indicate a"
                                + "slow client. Offending socket message: " + msg);
                    }
                }
            
            } catch (Throwable e) {
                logger.error("Exception caught sending a message to the client.", e);
            }
                
        } else if(hasClosed) {
            
            if(logger.isDebugEnabled()){
                logger.debug("Unable to send a message to the client because the connection to the client has been closed.  This message will not be sent to the client:\n" + msg);
            }
        }else{
            logger.warn("Unable to send a message to the client because the connection to the client has not been established yet.  This message will not be sent to the client:\n" + msg);

        }
        
    }
    
    /**
     * Sends a message to the web client.  This method sends a message to the client with no need for a response.
     * The message must be an instance of the AbstractWebSocketMessage class.
     * 
     * @param message The message to be sent to the web client.  Cannot be null.
     */
    public void send(AbstractWebSocketMessage message) {
        send(message, null);
    }
    
    /**
     * Sends a message to the client via the websocket connection.  This method can be used to send
     * an AbstractAsyncWebSocket message where a response is needed.  If an async message is needed,
     * it should extend off of AbstractAsyncWebSocketMessage and a message response handler should be passed in.
     * 
     * @param message The message to send to the server.  Cannot be null.
     * @param handler (optional) A response handler that MUST be set if sending an async message.
     */
    public void send(AbstractWebSocketMessage message, MessageResponseHandler handler) {
        if (!validateMessage(message)) {
            return;
        }
        
        if (message instanceof AbstractAsyncWebSocketMessage && handler == null) {
            logger.error("Unable to send message: " + message + ". If sending an async message, a response handler is required.");
            return;
        }
        
        if (!(message instanceof AbstractAsyncWebSocketMessage) && handler != null) {
            logger.error("Unable to send message: " + message + ". A response handler was passed in, but the message is not an async message.");
            return;
        }
        
        setMessageId(message);
        
        // If the message is an async message, then add a handler for when the response is received.
        if (message instanceof AbstractAsyncWebSocketMessage && handler != null) {
            AbstractAsyncWebSocketMessage asyncMessage = (AbstractAsyncWebSocketMessage)message;
            addMessageHandler(asyncMessage, handler);
        }
        
        
        serializeAndSendRawMessage(message);
    }
    
    /**
     * Sets the message id of the message.
     * 
     * @param message The message to set the id for.  Cannot be null.
     */
    private void setMessageId(AbstractWebSocketMessage message) {
        if (message != null && message.getMessageId() == AbstractWebSocketMessage.INVALID_MESSAGE_ID) {
            
            message.setMessageId(currentMessageId.incrementAndGet());
        }
    }
    
    /**
     * Serializes the message using GWT serialization and sends the raw message via the websocket.
     * 
     * @param message The message to be sent.  Cannot be null.
     */
    private void serializeAndSendRawMessage(AbstractWebSocketMessage message) {
        
        if (message == null) {
            return;
        }
        
        String data = WebSocketServerSerializationManager.getInstance().serializeMessage(message);
        
        if (data != null) {
            sendMessage(data);
        } 
    }
    
    /**
     * Validates a message before sending it.  
     * 
     * @param message The message to be validated.
     * @return True if the message is valid to be sent.  False otherwise.
     */
    private boolean validateMessage(AbstractWebSocketMessage message) {
        
        // Ignore null messages. 
        if (message == null) {
            return false;
        }
        
        if (message.getSource() != MessageSource.SERVER) {
            logger.error("Unable to send message: " + message + ".  Only messages created on the server can be sent to the client.");
            return false;
        }
        
        return true;
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
        asyncMessageHandlerMap.put(message.getMessageId(), handler);
        
    }
    
    

    /**
     * Handler for receiving a web socket message (where no response is needed to the client).
     * This is called after deserialization of the raw data. 
     *  
     * @param message The message that was received by the server.  The parameter cannot be null.
     */
    public abstract void onReceiveMessage(AbstractWebSocketMessage message);
    
    /**
     * Handler for receiving an asynchronous web socket message. This is called after deserialization
     * of the raw message data.  This method should be used when the client
     * requires a response back for an async message.  IMPORTANT - It is up to the implementer
     * to call 'send(response);' after the message has been processed to send the 
     * response back to the client.
     * 
     * @param message The async message that is to be processed by the server.
     * @param response The response that MUST be sent back to the client once the client processes the message.
     * It is critical that the implementer sends the 'response' parameter back to the server via the send(response) method.
     * The implementer should process the message, fill in the response with the appropriate details (such as success/failure, messages
     * and/or optional message response data).  Once the response is filled in, the implementer should call send(response) to send
     * the response back to the client.
     */
    public abstract void onReceiveMessageAsync(AbstractAsyncWebSocketMessage message, AsyncMessageResponse response);
    
    /**
     * Suspends the heartbeat mechanism that this socket uses to check that its associated client is still alive via web socket. 
     * Calling this method basically tells this socket to stop pinging its client periodically, which can be used to avoid unnecessary
     * work when the client is known to be disconnected but the server socket needs to be kept alive, such as when handling a timeout.
     */
    public void suspendHeartbeat() {
        
        //stop pinging the client to suspend the heartbeat mechanism
        pingTask.cancel();
        pingTimer.purge();
    }
    
    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
        /* Nothing to do. This socket does not handle raw binary messages. */
    }
    
    @Override
    public void onWebSocketError(Throwable error) {
        logger.error("An unexpected error was thrown from a websocket connection", error);
    }
    
    /**
     * Sets whether this socket should throttle sending matches to match the rate at which the client
     * receives them
     * 
     * @param throttleToMatchClient whether to throttle messages.
     */
    protected void setThrottleToMatchClient(boolean throttleToMatchClient) {
        this.throttleToMatchClient = throttleToMatchClient;
    }
}
