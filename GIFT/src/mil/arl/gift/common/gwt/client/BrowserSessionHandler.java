/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import com.google.gwt.core.client.JavaScriptException;

/**
 * Handler for events from a BrowserSession.  This interface allows users of a browser session to
 * be notified of any type of events that may be needed such as websocket closing, browser session
 * being created properly, etc.
 * 
 * @author nblomberg
 *
 */
public interface BrowserSessionHandler {
    
    /**
     * Called when the underlying websocket of a browser session is closed.
     */
    void onWebSocketClosed();
    
    /**
     * Called when the the browser session is fully created (client and server)
     * If a websocket is being used, then this typically gets called once the websocket
     * has been established. 
     */
    void onBrowserSessionReady();
    
    /**
     * Called when the underlying web socket throws a javascript exception.  Typically this
     * can occur during the initialization of the websocket.
     * 
     * @param reason The reason of the error.
     */
    void onWebSocketJsExceptionError(JavaScriptException ex);

}
