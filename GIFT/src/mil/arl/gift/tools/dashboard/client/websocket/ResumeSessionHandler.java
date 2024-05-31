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

import com.google.gwt.core.client.JavaScriptException;

import mil.arl.gift.common.gwt.client.BrowserSessionHandler;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;


/**
 * A handler for when a browser session is resumed in the dashboard. 
 * 
 * @author nblomberg
 *
 */
public class ResumeSessionHandler implements BrowserSessionHandler {
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(ResumeSessionHandler.class.getName());

    /** The screen that should be transitioned to after the browser session is ready. */
    private ScreenEnum nextScreen;

    /** 
     * Constructor
     *
     * @param nextScreen The screen that should be displayed once the browser is ready.
     */
    public ResumeSessionHandler(ScreenEnum nextScreen) {
        
        this.nextScreen = nextScreen;
        
    }

    @Override
    public void onWebSocketClosed() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onWebSocketClosed");
        }
        
        // Kick the user back to the login screen if they lose connection to the server.
        UiManager.getInstance().displayScreen(ScreenEnum.LOGIN); 
        UiManager.getInstance().displayErrorDialog("Server connection lost", 
                "You have been signed out because the web client has lost its connection to the server.  Please make sure the server is running and can be reached from the web browser.", null);
        
    }



    @Override
    public void onBrowserSessionReady() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onBrowserSessionReady");
        }
        // Web socket and underlying session is created, so proceed to the requested screen.
        UiManager.getInstance().displayScreen(nextScreen);
    }



    @Override
    public void onWebSocketJsExceptionError(JavaScriptException ex) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onJavaScriptExceptionError");
        }
        
        if (UiManager.getInstance().getScreenState() == ScreenEnum.INVALID) {
            // If the webocket was closed abnormally, then kick back to the login screen.
            UiManager.getInstance().displayScreen(ScreenEnum.LOGIN); 
        }
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("Unable to establish websocket due to the JavaScriptException: ").append(ex.getDescription()).append("<br/>")
            .append("<br/>")
            .append("Check the value of the WebSocketUrl property in dashboard.properties file to ensure it is configured correctly.<br/>")
            .append("You can also try changing the IP address in the address bar (e.g. 10.0.0.1).<br/>")
            .append("Replacing the IP address with localhost may fix the issue but this replacement may result in undefined behavior.");
        
        UiManager.getInstance().displayErrorDialog("WebSocket Error", stringBuilder.toString(), null);

    }
    
   
}