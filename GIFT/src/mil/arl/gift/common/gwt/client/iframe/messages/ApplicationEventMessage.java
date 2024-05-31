/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */

package mil.arl.gift.common.gwt.client.iframe.messages;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;

/**
 * A message that passes event information between GIFT and a mobile application embedding it
 * 
 * @author nroberts
 */
public class ApplicationEventMessage extends AbstractIFrameMessage {
    
    /** A label used to encode the event this message is passing information for */
    public final String LABEL_EVENT_MESSAGE = "EVENT_MESSAGE";
    
    /** A string containing the event information being passed */
    private String eventMessage;
    
    /**
     * Creates a new message notifying GIFT of an event
     */
    public ApplicationEventMessage() {
        setMsgType(IFrameMessageType.APPLICATION_EVENT);
    }
    
    /**
     * Creates a new message notifying GIFT of an event with the specified information
     * 
     * @param eventMessage a string containing the event information being passed
     */
    public ApplicationEventMessage(String eventMessage) {
        this();
        
        this.eventMessage = eventMessage;
    }

    @Override
    public void encode(JSONObject obj) {
        obj.put(LABEL_EVENT_MESSAGE, new JSONString(eventMessage));
    }

    @Override
    public void decode(JSONObject obj) {
        
        JSONValue valEvent = obj.get(LABEL_EVENT_MESSAGE);
        
        if (valEvent != null && valEvent.isString() != null) {
            
            eventMessage = valEvent.isString().stringValue(); 
        }
    }

    
    /**
     * Gets the string containing the event information being passed
     * 
     * @return the string containing the event information being passed
     */
    public String getEventMessage() {
        return eventMessage;
    }

    /**
     * Sets the string containing the event information being passed
     * 
     * @param eventMessage the string containing the event information being passed
     */
    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append("[ApplicationEventMessage eventMessage='")
                .append(eventMessage)
                .append("']")
                .toString();
    }
}
