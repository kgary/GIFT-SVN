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
 * A message used to control a mobile application when GIFT is embedded inside of it
 * 
 * @author nroberts
 */
public class ControlApplicationMessage extends AbstractIFrameMessage {
    
    /** A label used to encode the control this message is invoking */
    public final String LABEL_CONTROL_MESSAGE = "CONTROL_MESSAGE";
    
    /** A string denoting the control being invoked */
    private String controlMessage;
    
    /**
     * Creates a new message to control a mobile application
     */
    public ControlApplicationMessage() {
        setMsgType(IFrameMessageType.CONTROL_APPLICATION);
    }
    
    /**
     * Creates a new message invoking the given control
     * 
     * @param controlMessage a string denoting the control to invoke
     */
    public ControlApplicationMessage(String controlMessage) {
        this();
        
        this.controlMessage = controlMessage;
    }

    @Override
    public void encode(JSONObject obj) {
        obj.put(LABEL_CONTROL_MESSAGE, new JSONString(controlMessage));
    }

    @Override
    public void decode(JSONObject obj) {
        
        JSONValue valControl = obj.get(LABEL_CONTROL_MESSAGE);
        
        if (valControl != null && valControl.isString() != null) {
            
            controlMessage = valControl.isString().stringValue(); 
        }
    }

    /**
     * Gets the string denoting the control being invoked
     * 
     * @return the string denoting the control being invoked
     */
    public String getControlMessage() {
        return controlMessage;
    }

    /**
     * Sets the string denoting the control being invoked
     * 
     * @param controlMessage the string denoting the control being invoked
     */
    public void setControlMessage(String controlMessage) {
        this.controlMessage = controlMessage;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[ControlApplicationMessage controlMessage='")
                .append(controlMessage)
                .append("']")
                .toString();
    }
}
