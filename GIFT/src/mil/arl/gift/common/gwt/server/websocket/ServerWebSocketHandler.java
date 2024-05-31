/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.websocket;



/**
 * ServerWebSocketHandler allows for implementers to perform necessary work when a socket is opened, closed, etc.
 * 
 * @author nblomberg
 *
 */
public interface ServerWebSocketHandler  {
   

    /**
     * Called when a socket is closed.
     * 
     * @param socket the socket that was closed
     */
    void onSocketClosed(AbstractServerWebSocket socket);
    

    /**
     * Called when the socket is opened and ready to send/receive messages.
     * 
     * @param socket the socket that was opened
     */
    void onSocketOpened(AbstractServerWebSocket socket);
    
   
}
