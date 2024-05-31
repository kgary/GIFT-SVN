/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.websocket;

import javax.servlet.http.HttpServletRequest;

/**
 * The WebSocketConnectHandler interface is used to handle creation/connection of the websocket during the initial
 * websocket request.
 * 
 * @author nblomberg
 *
 */
public interface WebSocketConnectHandler  {
   
    /**
     * Creates the web socket based on an incoming websocket request.
     * @param req The incoming http websocket request.
     * @param protocol (unused)
     * @return The created web socket.
     */
    AbstractServerWebSocket createWebSocket(HttpServletRequest req, String protocol);

   
}
