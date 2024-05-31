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
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;


/**
 * The display dialog message is used to handle messages from the tui
 * and send them to the dashboard.  
 * It contains the relevant data needed to display a dialog for the user.
 * 
 * @author nblomberg
 *
 */
public class SetCookieMessage extends AbstractIFrameMessage  {
    
    private final String KEY = "KEY";
    private final String VALUE = "VALUE";
 
    private String cookieKey = "";
    
    // Type of dialog (error, info, etc).
    private String cookieValue = "";

    /**
     * Constructor 
     * Sets the type of message for this class.
     */
    public SetCookieMessage() {
        setMsgType(IFrameMessageType.GAT_SET_COOKIE);
    }
    
    /**
     * Constructor
     * Sets the message parameters as well as the type of message
     * for this class.
     * 
     * @param title - The title for the dialog
     * @param key - The message for the dialog.
     * @param value - The type of dialog (error, info, etc).
     */
    public SetCookieMessage(String key, String value) {
        setMsgType(IFrameMessageType.GAT_SET_COOKIE);
        
        cookieKey = key;
        cookieValue = value;        
    }

    /**
     * Accessor to get the dialog message.
     * 
     * @return String - The dialog message.
     */
    public String getCookieKey() {
        return cookieKey;
    }

    /**
     * Accessor to get the dialog type
     * 
     * @return String - The dialog type.
     */
    public String getCookieValue() {
        return cookieValue;
    }

    @Override
    public void encode(JSONObject obj) {
        
        obj.put(KEY, new JSONString(cookieKey));
        obj.put(VALUE, new JSONString(cookieValue));
    }

    @Override
    public void decode(JSONObject obj) {
        
       JSONValue jsonKey = obj.get(KEY);
       JSONValue jsonValue = obj.get(VALUE);
       
       if (jsonKey != null && jsonKey.isString() != null) {
           
           cookieKey = jsonKey.isString().stringValue(); 
       }
       
       if (jsonValue != null && jsonValue.isString() != null) {
           
           cookieValue = jsonValue.isString().stringValue(); 
       }
       
    }
}
