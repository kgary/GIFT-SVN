/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared.websocket;

import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;


/**
 * The MessageResponseHandler is used to handle the response of an async message that is sent via websockets.
 * 
 * @author nblomberg
 *
 */
public interface MessageResponseHandler {

   /**
    * Handler for the response of an async websocket message.  
    * 
    * @param response Response containing the result of an async websocket message.
    */
   public void onResponse(AsyncMessageResponse response);
   
}
