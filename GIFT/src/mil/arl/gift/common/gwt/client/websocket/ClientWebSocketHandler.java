/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.websocket;

import com.google.gwt.core.client.JavaScriptException;

/**
 * ClientWebSocketHandler allows for implementers to perform necessary work when the socket is open, closed, etc.
 * 
 * @author nblomberg
 *
 */
public interface ClientWebSocketHandler  {
   

    /**
     * Called when the socket is closed.
     */
    void onSocketClosed();
    

    /**
     * Called when the socket is opened and ready to send/receive messages.
     */
    void onSocketOpened();
    
    
    /** 
     * Called when the socket throws a javascript exception (typically during initialization)
     * 
     * @param ex The javascript exception that was thrown
     */
    void onJavaScriptException(JavaScriptException ex);
}
