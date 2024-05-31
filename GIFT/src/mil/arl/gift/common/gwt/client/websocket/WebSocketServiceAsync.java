/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.websocket;


import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;

/**
 * The async service that is used for leveraging the GWT serialization logic for web socket application.
 * This logic was derived from a presentation at GWTcon 2014 for using gwt serialization with web sockets:
 *  - https://www.slideshare.net/gwtcon/gwt20-websocket20and20data20serialization
 *  
 * @author nblomberg
 *
 */
public interface WebSocketServiceAsync {
    
    /** This method isn't used directly, but the class is required to leverage the gwt rpc serialization logic. */
	void getMessage(AbstractWebSocketMessage message, AsyncCallback<AbstractWebSocketMessage> callback)
	    throws IllegalArgumentException;
	
}
