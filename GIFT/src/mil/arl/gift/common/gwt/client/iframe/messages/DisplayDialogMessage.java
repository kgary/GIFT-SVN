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
public class DisplayDialogMessage extends AbstractIFrameMessage  {
    
    private final String LABEL_TITLE = "DIALOG_TITLE";
    private final String LABEL_MESSAGE = "DIALOG_MESSAGE";
    private final String LABEL_TYPE = "DIALOG_TYPE";
    private final String LABEL_DETAILS = "DIAOG_DETAILS";
   
    // Title of the dialog
    private String dialogTitle = "";
    
    // Message for the dialog
    private String dialogMessage = "";
    
    // Type of dialog (error, info, etc).
    private String dialogType = "";
    
    // (Optional) the details about the error or information.
    private String dialogDetails;


    /**
     * Constructor 
     * Sets the type of message for this class.
     */
    public DisplayDialogMessage() {
        setMsgType(IFrameMessageType.DISPLAY_DIALOG);
    }
    
    /**
     * Constructor
     * Sets the message parameters as well as the type of message
     * for this class.
     * 
     * @param title - The title for the dialog
     * @param message - The message for the dialog.
     * @param type - The type of dialog (error, info, etc).
     */
    public DisplayDialogMessage(String title, String message, String type) {
        setMsgType(IFrameMessageType.DISPLAY_DIALOG);
        
        dialogTitle = title;
        dialogMessage = message;
        dialogType = type;
        dialogDetails = null;
        
    }
    
    /**
     * Constructor
     * @param title The title for the dialog
     * @param message The message for the dialog.
     * @param details The details (optional) of the error or information.
     * @param type The type of dialog (error, info, etc).
     */
    public DisplayDialogMessage(String title, String message, String details, String type) {
        setMsgType(IFrameMessageType.DISPLAY_DIALOG);
        
        dialogTitle = title;
        dialogMessage = message;
        dialogType = type;
        dialogDetails = details;
        
    }

    /**
     * Accessor to get the dialog title.
     * @return String - The dialog title.
     */
    public String getDialogTitle() {
        return dialogTitle;
    }

    /**
     * Accessor to get the dialog message.
     * @return String - The dialog message.
     */
    public String getDialogMessage() {
        return dialogMessage;
    }

    /**
     * Accessor to get the dialog type
     * @return String - The dialog type.
     */
    public String getDialogType() {
        return dialogType;
    }
    
    /**
     * Accessor to get the dialog message.
     * @return String - The dialog message.
     */
    public void setDialogDetails(String details) {
        dialogDetails = details;
    }

    /**
     * Accessor to get the dialog type
     * @return String - The dialog type.
     */
    public String getDialogDetails() {
        return dialogDetails;
    }

    @Override
    public void encode(JSONObject obj) {
        
        obj.put(LABEL_TITLE, new JSONString(dialogTitle));
        obj.put(LABEL_MESSAGE, new JSONString(dialogMessage));
        obj.put(LABEL_TYPE, new JSONString(dialogType));
        
        if (dialogDetails != null && !dialogDetails.isEmpty()) {
            obj.put(LABEL_DETAILS, new JSONString(dialogDetails));
        }
        
    }



    @Override
    public void decode(JSONObject obj) {
        
       JSONValue valTitle = obj.get(LABEL_TITLE);
       JSONValue valMessage = obj.get(LABEL_MESSAGE);
       JSONValue valType = obj.get(LABEL_TYPE);
       JSONValue valDetails = obj.get(LABEL_DETAILS);
       
       if (valTitle != null && valTitle.isString() != null) {
           
           dialogTitle = valTitle.isString().stringValue(); 
       }
       
       if (valMessage != null && valMessage.isString() != null) {
           
           dialogMessage = valMessage.isString().stringValue(); 
       }
       
       if (valType != null && valType.isString() != null) {
           
           dialogType = valType.isString().stringValue(); 
       }
       
       if (valDetails != null && valDetails.isString() != null) {
           dialogDetails = valDetails.isString().stringValue(); 
       }
        
    }
}
