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
 * Displays a notification message in the dashboard using the bootstrap Notify class.
 * The intent is to allow the embedded tutor to pass data to the dashboard to display
 * a notification (via Bootstrap Notify element) rather than a dialog that must be
 * interacted with by the user.
 * 
 * It does not support ALL types of notify settings that are available, but allows
 * setting of title, message and icon.  Additional support could be added later if 
 * needed.
 * 
 * @author nblomberg
 *
 */
public class DisplayNotificationMessage extends AbstractIFrameMessage  {
    
    /** JSON label to encode the title. */
    private final String LABEL_TITLE = "TITLE";
    /** JSON label to encode the message. */
    private final String LABEL_MESSAGE = "MESSAGE";
    /** JSON lable to encode the icon type. */
    private final String LABEL_TYPE = "ICON_TYPE";
   
    // (Optional) title of the notification.  Can be empty to indicate no title.
    private String title = "";
    
    // Messag
    private String message = "";
    
    /* Type of the icon (optional).  Can be empty to indicate no icon.  For serialization
     * purposes, this is the css style name of the bootstrap icon, not the bootstrap icon itself.
     */
    private String iconType = "";

    /**
     * Constructor 
     * Sets the type of message for this class.
     */
    public DisplayNotificationMessage() {
        setMsgType(IFrameMessageType.DISPLAY_NOTIFICATION);
    }
    
    /**
     * Constructor
     * Sets the message parameters as well as the type of message
     * for this class.
     * 
     * @param title - The title for the notification (can be empty to indicate no title).
     * @param message - The message for the notification.
     * @param type - The type of icon (this represents the css name of the icon in bootstrap).  
     * It can be empty to indicate no icon.
     */
    public DisplayNotificationMessage(String title, String message, String type) {
        setMsgType(IFrameMessageType.DISPLAY_NOTIFICATION);
        
        this.title = title;
        this.message = message;
        iconType = type;
        
    }
    
    /**
     * Constructor
     * @param title The title for the dialog
     * @param message The message for the dialog.
     * @param details The details (optional) of the error or information.
     * @param type The type of dialog (error, info, etc).
     */
    public DisplayNotificationMessage(String title, String message, String details, String type) {
        setMsgType(IFrameMessageType.DISPLAY_NOTIFICATION);
        
        this.title = title;
        this.message = message;
        iconType = type;
        
    }

    /**
     * Accessor to get the dialog title.
     * @return String - The dialog title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Accessor to get the dialog message.
     * @return String - The dialog message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Accessor to get the dialog type
     * @return String - The dialog type.
     */
    public String getIconType() {
        return iconType;
    }
    

    @Override
    public void encode(JSONObject obj) {
        
        obj.put(LABEL_TITLE, new JSONString(title));
        obj.put(LABEL_MESSAGE, new JSONString(message));
        obj.put(LABEL_TYPE, new JSONString(iconType));
    }



    @Override
    public void decode(JSONObject obj) {
        
       JSONValue valTitle = obj.get(LABEL_TITLE);
       JSONValue valMessage = obj.get(LABEL_MESSAGE);
       JSONValue valType = obj.get(LABEL_TYPE);
       
       if (valTitle != null && valTitle.isString() != null) {
           
           title = valTitle.isString().stringValue(); 
       }
       
       if (valMessage != null && valMessage.isString() != null) {
           
           message = valMessage.isString().stringValue(); 
       }
       
       if (valType != null && valType.isString() != null) {
           
           iconType = valType.isString().stringValue(); 
       }
       

    }
}
