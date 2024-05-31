/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;

/**
 * The IFrameMessageListener interface defines a 'handleMessage' method that can be used
 * to respond to messages via cross-domain communication (via iframes).
 *   
 * The messagelistener objects can be registered with the IFrameMessageHandler classes and 
 * when messages are received, the 'handleMessage' method will be called.
 *  
 * @author nblomberg
 *
 */
public interface IFrameMessageListener {
    
    /**
     * Handler for the message (in string format).
     * 
     * @param message - The message that has been received (in string format).
     * @return - true if the message was handled, false otherwise.
     */
    boolean handleMessage(AbstractIFrameMessage message);
    

    
}