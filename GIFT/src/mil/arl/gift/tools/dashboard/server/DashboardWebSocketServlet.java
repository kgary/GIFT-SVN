/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.common.gwt.server.websocket.AbstractWebSocketServlet;

import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The dashboard web socket servlet is responsible for managing web socket connections with the dashboard clients.  The 
 * servlet also maps a web socket for each browser web session.  
 * 
 * @author nblomberg
 *
 */
@WebServlet(name = "DashboardWebSocketServlet", urlPatterns = { "/websocket" })
public class DashboardWebSocketServlet extends AbstractWebSocketServlet implements WebSocketCreator {

    private static Logger logger = LoggerFactory.getLogger(DashboardWebSocketServlet.class);
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    @Override
    public void init() throws ServletException {
        logger.info("init() called");
        super.init();
    }
    
    /**
     * Constructor
     */
    public DashboardWebSocketServlet() {
        
    }

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
        
        DashboardServerWebSocket webSocket = null;

        // Map the RuntimeTool web socket to the browser session.
        String browserSessionId = req.getHttpServletRequest().getParameter("browserSessionId");
        if (browserSessionId != null && !browserSessionId.isEmpty()) {


            BrowserWebSession session = UserSessionManager.getInstance().getBrowserSession(browserSessionId);
            
            if (session != null) {
                // Create a new web socket. 
                webSocket = new DashboardServerWebSocket(session);
                session.setWebSocket(webSocket);
            } else {
                logger.error("Could not find a browser web session for browser session id: " + browserSessionId);
            }
            
        } else {
            logger.error("Required parameter 'browserSessionId' could not be found in the incoming request.");
        }

        return webSocket;
    }

    @Override
    protected WebSocketCreator getWebSocketConnectHandler() {
        return this;
    }
}
