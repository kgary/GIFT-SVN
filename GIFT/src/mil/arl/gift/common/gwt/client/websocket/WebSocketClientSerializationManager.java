
/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.websocket;


import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;

/**
 * The WebSocketClientSerializationManager is responsible for serializing/deserializing messages on the client from the
 * websocket stream.  The gwt serialization logic is used to serialize and deserialize the messages.  This method allows
 * existing objects to be reused (that were used for gwt-rpc).  This class is based on a presentation that was done
 * at GWTcon 2014:  https://www.slideshare.net/gwtcon/gwt20-websocket20and20data20serialization
 * 
 * The class is a singleton object that is intended to be used across all websockets.
 * 
 * @author nblomberg
 *
 */
public class WebSocketClientSerializationManager  {
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(WebSocketClientSerializationManager.class.getName());
    
    /** The gwt serialization factory used to serialize the data. */
    private SerializationStreamFactory factory = (SerializationStreamFactory) GWT.create(WebSocketService.class);
    
    /** Instance of the serialization manager (singleton object). */
    private static WebSocketClientSerializationManager instance = null;
    
    /**
     * Constructor (private)
     */
    private WebSocketClientSerializationManager() {
        
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("TutorSerializationManager()");
        }
    }
    
    /**
     * Returns the instance to the singleton object.  If the instance doesn't exist
     * it will be created.
     * 
     * @return The instance to the serialization manager object.
     */
    static public WebSocketClientSerializationManager getInstance() {
        if (instance == null) {
            instance = new WebSocketClientSerializationManager();
        }
        
        return instance;
    }
    
    /**
     * Serialize the message. The gwt serialization logic is used to convert the object.  The object 
     * must follow the rules for gwt serialization to be able to be serialized.
     * 
     * @param message The message to be serialized. 
     * @return String the serialized message.  Null is returned if there was an error.
     */
	public String serializeMessage(AbstractWebSocketMessage message) {
	    String data = null;
	    try {
	       
	        SerializationStreamWriter writer = factory.createStreamWriter();
	        writer.writeObject(message);
	        data = writer.toString();
	    } catch (Exception e) {
	        logger.severe("Exception caught serializing message of " + message + "\n" + e);
	    }
	    
	    return data;
	}

	/**
	 * Deserialize the message.  The gwt serialization logic is used to deserialize the data.  The object
	 * must follow the rules for gwt serialization to be able to be deserialized.  
	 * @param data
	 * @return
	 */
	public AbstractWebSocketMessage deserializeMessage(String data) {
	    AbstractWebSocketMessage message = null;
	    try {
	        final SerializationStreamReader streamReader = factory.createStreamReader(data);
	        
	        message = (AbstractWebSocketMessage)streamReader.readObject();
	    } catch (Exception e) {
	        logger.log(Level.SEVERE, "Exception caught deserializing message of\n" + data + ".", e);
	    }
	    
	    return message;
	}
	
	
	
}
