/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.websocket;

import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The AbstractWebSocketServlet class is the base class that all other web socket servlet 
 * classes should extend from.  It provides basic functionality for handling the socket connection/creation and is
 * meant to make it easier for sub classes to focus on implementation specific logic.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractWebSocketServlet extends WebSocketServlet {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(AbstractWebSocketServlet.class.getName());
   
    /** Default serialization id */
    private static final long serialVersionUID = 1L;
    
    /** The handler used to help connecting the web socket during an incoming request. */
    private WebSocketCreator connectHandler = null;
    
    /**
     * Constructor - default.
     */
    public AbstractWebSocketServlet() {
        
        if(logger.isInfoEnabled()) {
            logger.info("constructor called.");
        }
    }
    
    /**
     * Gets the web socket connect handler that will be used by the servlet.  
     * 
     * @return The websocketconnecthandler that will be used byt he servlet.
     */
    protected abstract WebSocketCreator getWebSocketConnectHandler();

    @Override
    public void configure(WebSocketServletFactory factory) {
        
        connectHandler = getWebSocketConnectHandler();
        
        if (connectHandler == null) {
            throw new IllegalArgumentException("The WebSocketConnectHandler cannot be null.");
        }
        
        factory.setCreator(connectHandler);
    }   
}
