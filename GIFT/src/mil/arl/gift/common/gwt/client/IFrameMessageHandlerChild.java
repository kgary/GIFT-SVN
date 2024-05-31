/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import java.util.logging.Logger;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageParser;
import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;


/**
 * The IFrameMessageHandlerChild class is responsible for handling cross-domain
 * messages (via iframes) within a single document.  More documentation regarding this
 * has been placed on the wiki at:  
 * https://gifttutoring.org/projects/gift/wiki/GIFT_Developer_Guide_(Private)#Gwt-CloudDashboard-38-Cross-Domain-communication
 * 
 * The "Child" signifies that this class should be created as in 'iframe' within a document and will be able to
 * communicate to the parent document.  This uses JSNI and Javascript postMessage() to send the messages to the parent document
 * as well as JSNI and the Javascript 'addEventListener()' methods to listen for messages from parent document.  
 * 
 * Listeners can be added to the IFrameMessageHandlerParent class such that when a message from the parent is received,
 * the listeners will be notified via the 'handleMessage()' function. 
 * 
 * For now this is a singleton object that should be instantiated and created on the Gwt onModuleLoad().  The order should be
 * that in the gwt onModuleLoad(), IFrameMessageHandlerParent.getInstance().init() should be called which registers the 
 * the Javascript event listener methods.
 * 
 * @author nblomberg
 *
 */
public class IFrameMessageHandlerChild extends AbstractIFrameMessageHandler  {

   
    private static Logger logger = Logger.getLogger(IFrameMessageHandlerChild.class.getName());
    
    /** The singleton instance of the IFrameMessageHandlerChild class */
    private static IFrameMessageHandlerChild instance = null;
    
    /**
     * Default Constructor
     */
    private IFrameMessageHandlerChild() {
        logger.info("Creating IFrameMessageHandlerChild instance.");
    }
    

    
    /**
     * Singleton access to the class.  If the instance isn't created
     * it will be created the first time.
     * 
     * @return IFrameMessageHandlerChild - instance of the singleton class.
     */
    public static IFrameMessageHandlerChild getInstance() {
        if (instance == null) {
            instance = new IFrameMessageHandlerChild();
        }
        
        return instance;
    }
    
    /**
     * Handles initialization of the class.  During initialization the Javascript event listener (handler) is established.  
     * This method is expected to be called during the gwt onModuleLoad() class. 
     */
    public void init() {
        
        if (!initialized) {
            setupJsHandler(this);
            
            // indicate that the handler has been properly initialized.
            initialized = true;
        } else {
            logger.severe("The IFrameMessageHandlerChild has already been initialized.  Initialization should only happen once.");
        }
        
    }
    
    
    
    /**
     * Sends a message (via JSNI) to the parent document via the HTML 5 postMessage() command.
     * 
     * @param msg - The message (string format) to send to the parent document.
     */
    public void sendMessage(AbstractIFrameMessage msg) {
        
        JSONObject jsonObj = IFrameMessageParser.encode(msg);
        
        logger.fine("Sending Message: " + jsonObj.toString());
        
        postMessageToParent(jsonObj.toString());
    }

    
    /**
     * Receives a message (via JSNI) from the parent document via the javascript
     * addEventListener method.
     * 
     * The received messages are broadcast to any listeners that are found.
     * 
     * @param msg - Message received from the parent document (in String format).
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
     * JSNI method to setup the event listener to listen for parent document messages
     * Any messages that are received (string format) are sent to the Java method (receiveMessage()) for handling
     * within Java code.
     * 
     * For more information on JSNI see: http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html
     * 
     * @param child - An instance to the IFrameMessageHandlerChild class.
     */
    private native void setupJsHandler(IFrameMessageHandlerChild child) /*-{

        function receiveMessage(event)
        {
        	
        	// We don't want this handler to receive messages from any other iframes within the child iframe, since it 
     		// should only receive messages from its parent. If the source window of a message matches any of this
     		// window's children, then the message should be ignored.
        	for(var i = 0; i < $wnd.frames.length; i++){
          	
	         	if($wnd.frames[i] == event.source){
	         		return;
	          	}
          } 
 
          	var message = event.data;
          	
          	child.@mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild::receiveMessage(Ljava/lang/String;)(message);

        }

        $wnd.addEventListener("message", receiveMessage, false);
    
    }-*/;


    /**
     * JSNI method to send a message via postMessage() to a parent document.
     * 
     * For more information on JSNI see: http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html
     * 
     * @param message - String formatted message to send to the child iFrame.
     */
    private native void postMessageToParent(String message) /*-{
        $wnd.parent.postMessage(message, "*");
        // console.log("Child iFrame sending message: " + message);
    }-*/;
}
