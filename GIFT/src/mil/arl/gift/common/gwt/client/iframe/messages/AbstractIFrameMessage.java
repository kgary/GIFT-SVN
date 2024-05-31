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
 * The abstract iframe message class encapsulates the core message functionality
 * that all derived classes will need.  It includes the message type along with
 * functionality for each child class to provide an encoding / decoding mechanism.
 * 
 * We are using Gwt JSON types in these classes (JSONObject, JSONValue).  Refer to:
 * http://www.gwtproject.org/javadoc/latest/com/google/gwt/json/client/JSONValue.html
 * for more information on those classes.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractIFrameMessage  {

    // The core type for this message.  Must be specified for all messages. 
    private IFrameMessageType msgType = IFrameMessageType.INVALID;
    
    /**
     * Accessor to return the message type for this message.
     * @return IFrameMessageType - The iframe message enum (type) for this message.  Must not be null.
     */
    public IFrameMessageType getMsgType() {
        return msgType;
    }
    
    /**
     * Accessor to set the message type for this message.
     * @param msgType - The IFrameMessageType enum for this message.  Must not be null.
     */
    public void setMsgType(IFrameMessageType msgType) {
        this.msgType = msgType;
    }
    
    /**
     * Function to encode the message into an existing JSONObject that is passed in.
     * The object can embed any necessary data into the JSONObject.  The JSONObject
     * will be serialized and sent via 'cross-domain' communication using iframes as
     * a json string.
     * 
     * @param obj - Required (must not be null).  The JSONObject to encode the message into.
     */
    public abstract void encode(JSONObject obj);
    
    /**
     * Function to decode the message from an existing JSONObject that is passed in.
     * The object can decode any required data from the JSONObject that was previously
     * encoded.  Decoding here doesn't return any value as it's expected that the message
     * instance will use the data from the JSONObject to restore it's own internal state.
     * 
     * @param obj - Required (must not be null).  The JSONObject to decode the message from.
     * 
     */
    public abstract void decode(JSONObject obj);
   
    
}
