/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.iframe;

import java.util.logging.Logger;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.ApplicationEventMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.ControlApplicationMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.DisplayDialogMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.DisplayNotificationMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.EndCourseMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.HistoryItemMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.SetCookieMessage;


/**
 * The IFrameMessageParser class is responsible for parsing messages into Gwt
 * JSONObjects and decoding them back into AbstractIFrameMessages.
 * 
 * At a high level, a JSONObject is created that holds the "message type" and 
 * some optional "message data" which could be message specific data that is 
 * required beyond just a simple type.  Classes can be created that embed additional
 * information into the 'message data' parameter if required.
 * 
 * Note that for JSON encoding, the gwt client JSON classes are used since
 * this code lives on the client.  For more inforamtion about these classes, 
 * refer to:  http://www.gwtproject.org/javadoc/latest/com/google/gwt/json/client/JSONValue.html
 * 
 * @author nblomberg
 *
 */
public class IFrameMessageParser  {

    private static Logger logger = Logger.getLogger(IFrameMessageParser.class.getName());
    
    // The JSON label/tag that is used to identify the message type.
    private static final String MESSAGE_TYPE_LABEL = "MESSAGE_TYPE";
    
    // The JSON label/tag that is used to identify the message data.
    private static final String MESSAGE_DATA_LABEL = "MESSAGE_DATA";

    
    /**
     * Static function to encode an AbstractIFrameMessage into a gwt JSONObject.  
     * 
     * @param msg - An iframe message to encode (required - must not be null).
     * @return JSONObject - The JSONObject that the message is encoded into.
     */
    public static JSONObject encode(AbstractIFrameMessage msg) {
        
        JSONObject obj = new JSONObject();
        
        // Encode the message type
        JSONString messageType = new JSONString(msg.getMsgType().name());
        obj.put(MESSAGE_TYPE_LABEL,messageType);
        
        // Encode the message data.
        JSONObject data = new JSONObject();
        msg.encode(data);
        
        obj.put(MESSAGE_DATA_LABEL, data);
        
        return obj;
    }



    /** 
     * Static function to decode a gwt JSONObject into an AbstractIFrameMessage.
     * 
     * @param obj - (required, must not be null).  The gwt JSONObject to decode the message from.
     * @return AbstractIFrameMessage - The iframe message that has been decoded from the json data.  Can return null if an error occurs.
     */
    public static AbstractIFrameMessage decode(JSONObject obj) {
        
        JSONString type = (JSONString)(obj.get(MESSAGE_TYPE_LABEL));
        
        AbstractIFrameMessage msg = null;
        
        if (type != null) {
            
           JSONObject data = (JSONObject)(obj.get(MESSAGE_DATA_LABEL));
           
           if (data != null) {
               IFrameMessageType  messageType = IFrameMessageType.valueOf(type.stringValue());
               
               switch (messageType) {
               
                case END_COURSE:
                    msg = new EndCourseMessage();
                    msg.decode(data);
                    break;
                
                case DISPLAY_DIALOG:
                    msg = new DisplayDialogMessage();
                    msg.decode(data);
                    break;
                case DISPLAY_NOTIFICATION:
                    msg = new DisplayNotificationMessage();
                    msg.decode(data);
                    break;
                case GAT_SET_COOKIE:
                    msg = new SetCookieMessage();
                    msg.decode(data);
                    break;
                    
                case APPLICATION_EVENT:
                    msg = new ApplicationEventMessage();
                    msg.decode(data);
                    break;
                    
                case CONTROL_APPLICATION:
                    msg = new ControlApplicationMessage();
                    msg.decode(data);
                    break;
                    
                case ADD_HISTORY_ITEM:
                    msg = new HistoryItemMessage();
                    msg.decode(data);
                    break;
                
                 // intentional fallthroughs (these messages are all simple message type)
                case CLOSE_GAT_FILES:
                case STOP_COURSE:
                case COURSE_READY:
                case GAT_FILES_CLOSED:
                case GAT_FILES_OPEN:
                case GO_TO_DASHBOARD:
                case GAT_LOADED:
                case WRAP_OPEN:
                case WRAP_CLOSED:
                case COURSE_STARTING:
                case TUI_READY:
                    msg = new IFrameSimpleMessage(messageType);
                    break;
                default:
                    logger.warning("Unable to decode json object, unhandled message type: " + messageType);
                    break;
               }
           } else {
               logger.warning("Unable to decode json object, message data is null. " + obj.toString());
           }
        } else {
            logger.warning("Unable to decode json object, message type is null. " + obj.toString());
        }
        
        
        return msg;
        
    }
}
