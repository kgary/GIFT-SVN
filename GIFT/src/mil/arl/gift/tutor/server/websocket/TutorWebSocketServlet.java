/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.common.gwt.server.websocket.AbstractWebSocketServlet;
import mil.arl.gift.common.gwt.server.websocket.ServerWebSocketHandler;
import mil.arl.gift.common.gwt.shared.websocket.CloseEventCodes;
import mil.arl.gift.tutor.server.TutorBrowserWebSession;
import mil.arl.gift.tutor.server.TutorModule;
import mil.arl.gift.tutor.shared.websocket.TutorWebSocketUtils;

import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The tutor web socket servlet is responsible for managing web socket connections with the tutor clients.  The 
 * servlet also maps a web socket for each browser web session.  
 * 
 * @author nblomberg
 *
 */
public class TutorWebSocketServlet extends AbstractWebSocketServlet implements WebSocketCreator {

    private static Logger logger = LoggerFactory.getLogger(TutorWebSocketServlet.class.getName());
    
    private static final long serialVersionUID = 1L;
    
    /** A mapping from the web socket ID for each client to the appropriate web socket */
    private static final Map<String, TutorServerWebSocket> ID_TO_SOCKET = new HashMap<>();
    
    /** A timer used to allow web sockets to time out after they disconnect */
    private static final Timer timeoutTimer = new Timer("Tutor Web Socket Timeout", true);
    
    @Override
    public void init() throws ServletException {
    	
    	if(logger.isInfoEnabled()){
    		logger.info("init() called");
    	}
    	
        super.init();
        
        //ensure an instance of TutorModule is created if it somehow hasn't been already
        TutorModule.getInstance();
    }
    
    public TutorWebSocketServlet() {
    	
    	if(logger.isInfoEnabled()){
    		logger.info("constructor called.");
    	}
    }

    /**
     * Creates a web socket based on an incoming web socket request. 
     * <br/><br/>
     * The given request must provide a UUID in its {@link #SOCKET_ID} parameter 
     * that will be used to create and uniquely identify a web socket for the client that made the request. This UUID will later be used 
     * while constructing the client's {@link mil.arl.gift.tutor.server.BrowserWebSession} in order to identify the web socket that should
     * be used to handle messages for the client's brower session.
     * <br/><br/>
     * If the request does not provide a UUID in its {@link #SOCKET_ID} parameter, then no web socket will be created, and this method will return null.
     * <br/><br/>
     * @param req The incoming HTTP websocket request. This request will be upgraded to an actual websocket
     * connection by the web socket factory.
     * @param resp The server's respose to the websocket request. Information about the upgraded websocket
     * connection will be returned to the client to allow websocket communication to begin.
     * @return The created web socket.
     */
    @Override
    public AbstractServerWebSocket createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        
        /* 
         * Disable web socket extensions in the response, such as permessage-deflate. Such extensions are supported 
         * by Jetty 9.4 but are not well supported by Windows IIS. If Jetty tries to respond to a web socket upgrade
         * request that uses such extensions, IIS will throw a 502 error because it doesn't know how to interpret the
         * web socket extensions in the header for the server response. Since IIS acts as a proxy for the Jetty
         * server, this ends up causing the websocket connection to be closed immediately since the request to
         * upgrade the HTTP connection to WS could not be fulfilled.
         */
        resp.setExtensions(new ArrayList<ExtensionConfig>());
    	
    	final String socketId = req.getHttpServletRequest().getParameter(TutorWebSocketUtils.SOCKET_ID_PARAM);
    	
        if (socketId != null && !socketId.isEmpty()){
        	
        	//create the web socket for this client
        	TutorServerWebSocket webSocket = new TutorServerWebSocket(){
        	    
        	    /**
        	     * A wrapper for this instance's super onClose method. Used to continue handle web socket closure normally
        	     * after a socket's connection has timed out.
        	     * 
        	     * @param code the status code for the close event
        	     * @param message the close event message
        	     */
        	    private void superOnWebSocketClose(int code, String message) {
        	        super.onWebSocketClose(code, message);
        	    }
        		
        		@Override
        		public void onWebSocketClose(final int code, final String message) {
        		    
        		    final TutorServerWebSocket thisSocket = this;
        		    
        		    //define a task to handle cleaning up the socket and its associated sessions
        		    TimerTask closeTask = new TimerTask() {
                        
                        @Override
                        public void run() {
                            
                            superOnWebSocketClose(code, message);
                            
                            synchronized (ID_TO_SOCKET) {
                                if(thisSocket.equals(ID_TO_SOCKET.get(socketId))){
                                    
                                    //remove the socket's mapping once the socket has closed, but only if its socket ID isn't being used by a new socket
                                    ID_TO_SOCKET.remove(socketId);
                                }
                            }
                            
                            if(logger.isInfoEnabled()){
                                logger.info("Web socket removed for client with socket ID of " + socketId);
                            }
                        }
                    };
                    
                    
                    Integer timeout = null;
        		    
        		    if(getSocketHandler() != null && getSocketHandler() instanceof TutorBrowserWebSession) {
        		        
        		        if(CloseEventCodes.isErroneousClosure(code)) {
        		            
        		            // If a client experiences an unexpected disconnect, allow them
                            // to reconnect and continue their course before a certain amount of time passes
        		            if(((TutorBrowserWebSession) getSocketHandler()).getClientInformation().isMobile()) {
        		                timeout = TutorWebSocketUtils.MOBILE_SOCKET_TIMEOUT_MS;
        		                
        		            } else {
        		                timeout = TutorWebSocketUtils.DEFAULT_SOCKET_TIMEOUT_MS;
        		            }
        		        }
        		    }
        		    
        		    if(timeout != null) {
        		        
        		        //prevent this socket from making futile attempts to reach the disconnected client while it is timing out
        		        suspendHeartbeat();
        		        
        		        //delay handling the close event until the appropriate timeout period has passed
        		        timeoutTimer.schedule(closeTask, timeout);
        		        
        		    } else {
        		        closeTask.run();
        		    }
        		}
        	};
        	
        	//keep a mapping from this client to its socket so we can identify it later
            synchronized (ID_TO_SOCKET) {
                
                TutorServerWebSocket existingSocket = ID_TO_SOCKET.get(socketId);
                
                ID_TO_SOCKET.put(socketId, webSocket);
                
                if(existingSocket != null) {
                    
                    //an existing client is reconnecting with a new socket, so reuse the old socket's handler if possible
                    ServerWebSocketHandler existingHandler = existingSocket.getSocketHandler();
                    
                    if(existingHandler != null) {
                        
                        if(logger.isInfoEnabled()) {
                            logger.info("Handing off existing socket handler to new socket, since the client has likely reconnected.");
                        }
                        
                        webSocket.setSocketHandler(existingHandler);
                        existingSocket.setSocketHandler(null);
                    }
                }
			}
        	
        	if(logger.isInfoEnabled()){
        		logger.info("Web socket created for client with socket ID of " + socketId);
        	}
        	
        	return webSocket;
        	
        } else {
        	return null;
        }
    }
    
    /**
     * Gets the web socket that should be used with the client with the given web socket ID. The ID for a client's web socket is determined by the
     * {@link #SOCKET_ID} parameter provided by the URL that the client used to establish the web socket connection. If the client never established
     * a web socket connection or if the given socket ID doesn't correspond to any clients that have connected thus far, then this method will
     * return null.
     * 
     * @param socketId the web socket ID of the client whose web socket is being requested
     * @return the appropriate web socket for the client with the given web socket ID. Can be null if no web socket was established using
     * the given web socket ID.
     */
    public static TutorServerWebSocket getWebSocket(String socketId){
    	return ID_TO_SOCKET.get(socketId);
    }

    @Override
    protected WebSocketCreator getWebSocketConnectHandler() {
        return this;
    }

}
