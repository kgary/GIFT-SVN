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
import mil.arl.gift.common.gwt.shared.LtiParameters;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.DashboardErrorWidget.ErrorMessage;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;


/**
 * A handler for when an lti browser session is resumed in the dashboard. 
 * 
 * @author nblomberg
 *
 */
public class ResumeLtiSessionHandler implements BrowserSessionHandler {
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(ResumeLtiSessionHandler.class.getName());

    /** The lti parameters used to launch the course */
    private LtiParameters ltiParams;

    /** 
     * Constructor
     *
     * @param LtiParameters The lti params that are used to load to the next screen.
     */
    public ResumeLtiSessionHandler(LtiParameters ltiParams) {
        
        this.ltiParams = ltiParams;
        
    }

    @Override
    public void onWebSocketClosed() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onWebSocketClosed");
        }
        
        // If the webocket was closed abnormally, then show the error page.
        ErrorMessage params = new ErrorMessage("Lost connection to the server", 
                "The course has ended due to connection being lost to the server.", null);
        
        UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, params);
    }



    @Override
    public void onBrowserSessionReady() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onBrowserSessionReady");
        }

     // session id is invalid, so this will display an error.
        UiManager.getInstance().displayScreen(ScreenEnum.LTI_CONSUMER_START_PAGE, ltiParams);
    }



    @Override
    public void onWebSocketJsExceptionError(JavaScriptException ex) {
        
        logger.severe("onJavaScriptExceptionError(): " + ex);

        // If the webocket was closed abnormally, then show the error page.
        ErrorMessage params = new ErrorMessage("Unable to start course.", "A connection to the server could not be established due to error: " 
                + ex.getDescription(), null);
        
        UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, params);

    }
    
   
}