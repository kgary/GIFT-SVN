/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.iframe.IFrameMessageParser;
import mil.arl.gift.common.gwt.client.iframe.IFrameOrigin;
import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;


/**
 * The IFrameMessageHandlerParent class is responsible for handling cross-domain
 * messages (via iframes) within a single document.  More documentation regarding this
 * has been placed on the wiki at:  
 * https://gifttutoring.org/projects/gift/wiki/GIFT_Developer_Guide_(Private)#Gwt-CloudDashboard-38-Cross-Domain-communication
 * 
 * The "Parent" signifies that this class should be created at the 'parent' document level and will be able to
 * communicate to a child iFrame.  This uses JSNI and Javascript postMessage() to send the messages to the child iFrame
 * as well as JSNI and the Javascript 'addEventListener()' methods to listen for messages from the child iFrame.  
 * 
 * Listeners can be added to the IFrameMessageHandlerParent class such that when a message from the child iFrame is received,
 * the listeners will be notified via the 'handleMessage()' function. 
 * 
 * For now this is a singleton object that should be instantiated and created on the Gwt onModuleLoad().  The order should be
 * that in the gwt onModuleLoad(), IFrameMessageHandlerParent.getInstance().init() should be called which registers the 
 * the Javascript event listener methods.  Currently the IFrameMessageHandlerParent class supports communication with multiple
 * child iframes.
 * 
 * @author nblomberg
 *
 */
public class IFrameMessageHandlerParent extends AbstractIFrameMessageHandler {

    private static Logger logger = Logger.getLogger(IFrameMessageHandlerParent.class.getName());
    private static IFrameMessageHandlerParent instance = null;
    
    private String targetOrigin = "";
    private String iFrameId = "";
    
    HashMap<String, IFrameOrigin> originMap = null;
    JsArrayString origins = null;

    
    /**
     * Default Constructor
     *
     */
    private IFrameMessageHandlerParent() {
        logger.info("Creating IFrameMessageHandlerParent instance.");
        
        origins = JsArrayString.createArray().cast();
        originMap = new HashMap<String, IFrameOrigin>();
    }
    
    /**
     * Singleton access to the class.  If the instance isn't created
     * it will be created the first time.
     * 
     * @return IFrameMessageHandlerParent - instance of the singleton class.
     */
    public static IFrameMessageHandlerParent getInstance() {
        if (instance == null) {
            instance = new IFrameMessageHandlerParent();
        }
        
        return instance;
    }
    
    /**
     * Handles initialization of the class.  The origin needs to be set properly to the 
     * target (child) iFrame host.  The origin should be formatted similar to "http://wwww.something.com:8888"
     * and needs to match the child iFrame host & port.  The child iFrame must be given an 'id'   
     * that is specified here and must match the 'id' of the child iFrame as it's created in order for the 
     * communication to work properly.
     * 
     * During initialization the Javascript event listener (handler) is established.  This method is expected
     * to be called during the gwt onModuleLoad() class. 
     * 
     * @param originList - array of child frames that the parent will need to communicate with.  There should be at least one entry in the array.
     */
    public void init(ArrayList<IFrameOrigin> originList) {
        
        if (!initialized) {
            
            if (originList != null && !originList.isEmpty()) {
               
                
                for (int x=0; x < originList.size(); x++) {
                    
                    IFrameOrigin origin = originList.get(x);
                    
                    originMap.put(origin.getOriginKey(), origin);
                    
                    
                    origins.push(origin.getOriginUrl());
                }

                setupJsHandler(this, origins);
                
                
                // indicate that the handler has been properly initialized.
                initialized = true;
            }
            else {
                logger.severe("The IFrameMessageHandlerParent is being initialized with an invalid origin list");
            }
        } else {
            logger.severe("The IFrameMessageHandlerParent has already been initialized.  Initialization should only happen once.");
        }
        
        
        
    }
    
    
    /**
     * Sends a message (via JSNI) to the child iFrame via the HTML 5 postMessage() command.
     * 
     * @param msg - The message (string format) to send to the child iFrame.
     * @param childKey - The key of the IFrameOrigin that specifies what child that the message should be sent to.
     */
    public void sendMessage(AbstractIFrameMessage msg, String childKey) {

        if (originMap.containsKey(childKey)) {
            
            IFrameOrigin child = originMap.get(childKey);
            JSONObject jsonObj = IFrameMessageParser.encode(msg);

            logger.fine("Sending Message: " + jsonObj.toString());

            sendMessage(jsonObj.toString(), child.getOriginUrl(), child.getIFrameId());
            
        } else {
            logger.severe("Unable to send message to child.  Cannot find child in the originMap: " +  childKey);
        }
        
        
    }
    
    /**
     * Receives a message (via JSNI) from the child iFrame via the javascript
     * addEventListener method.
     * 
     * The received messages are broadcast to any listeners that are found.
     * 
     * @param msg - Message received from the child Iframe (in String format).
     */
    private void receiveMessage(String msg) {
 
        logger.fine("Receiving message: " + msg);
        
        JSONValue jsonVal = JSONParser.parseStrict(msg);
        if (jsonVal != null && jsonVal.isObject() != null) {
            JSONObject obj = jsonVal.isObject();
            
            AbstractIFrameMessage message = IFrameMessageParser.decode(obj);
            
            if (message != null) {
                for (IFrameMessageListener listener : messageListeners) {
                    listener.handleMessage(message);
                }
            } else {
                logger.warning("Decoding error occurred. Message is null: " + msg);
            }
            
        } else {
            logger.warning("Message received, but will not be handled.  Json message is null or is not an object: " + msg);
        }
        
    }
    
    /**
     * 
     * JSNI method to setup the event listener to listen for child iFrame messages.
     * Any messages that are received (string format) are sent to the Java method (receiveMessage()) for handling
     * within Java code.
     * 
     * For more information on JSNI see: http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html
     * 
     * @param parent - An instance to the IFrameMessageHandlerParent class.
     * @param origin - The target (child) origin.  Should be in format of "http://www.something.com:8888"
     */
    private native void setupJsHandler(IFrameMessageHandlerParent parent, JsArrayString origins) /*-{
    
        function receiveMessage(event) {
    

          // Check the origin for security purposes.
          if (origins.indexOf(event.origin) == -1) {
            // console.log("Received an event from an unsupported origin: " + event.origin);
            return;
          }
          
          var message = event.data;
          // console.log("IFrameMessageHandlerParent received: " + message);
          parent.@mil.arl.gift.common.gwt.client.IFrameMessageHandlerParent::receiveMessage(Ljava/lang/String;)(message);
        }
        
        // Add the event listener to the window.
        $wnd.addEventListener("message", receiveMessage, false);
        // console.log("IFrameMessageHandlerParent eventListener is registered.");
        
        
    }-*/;
    
    /**
     * JSNI method to send a message via postMessage() to a child iFrame.
     * 
     * For more information on JSNI see: http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html
     * 
     * @param message - String formatted message to send to the child iFrame.
     * @param origin - The target (child) origin host & port. Should be in the form of "http://www.something.com:8888"
     * @param iFrameId - The child iFrame id.
     */
    private native void sendMessage(String message, String origin, String iFrameId)/*-{
    
    // targetOrigin should be in the form of "http://www.something.com:8888"
    var targetOrigin = origin; 
    
    // console.log("targetOrigin (" + targetOrigin + ") sending message: " + message);
    
    var iframe = $wnd.document.getElementById(iFrameId).contentWindow;
    iframe.postMessage(message, targetOrigin);
    
    
    
}-*/;
    

    
}
