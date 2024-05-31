/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.iframe.messages;

import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;

import com.google.gwt.json.client.JSONObject;

/**
 * The IFrameSimpleMessage class represents a simple message of only
 * a 'type' with no message data.  The constructor can take in the message
 * type which can be sent via cross-domain communication.
 * 
 * Note that the encode/decode methods are empty since only the message
 * type is used for the 'simple' messages.  The message type is already
 * encoded & decoded by the IFrameMessageParser.  
 * 
 * @author nblomberg
 *
 */
public class IFrameSimpleMessage extends AbstractIFrameMessage  {
    
    /**
     * Constructor - Takes an iframe message type and sets the
     * base class message type.
     * 
     * @param type - The type of iframe message for this class.
     */
    public IFrameSimpleMessage(IFrameMessageType type) {
        setMsgType(type);
    }

    @Override
    public void encode(JSONObject obj) {
        
        // There is no object specific data to encode for this class.
    }



    @Override
    public void decode(JSONObject obj) {
        
       // There is no object specific data to decode for this class.
        
    }
}
