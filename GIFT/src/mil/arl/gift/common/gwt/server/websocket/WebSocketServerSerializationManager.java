/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.websocket;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;

import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;

/**
 * The server side serialization manager which is used to serialize web socket messages before sending to the client.  Conversely,
 * it is responsible for deserialization of messages that are received by the client.  The goal here is to provide a standard
 * serialization scheme for any current/future websocket implementation used by GIFT.  Leveraging the gwt serialization allows re-use
 * of classes that were used by the gwt-rpc framework.  
 * 
 * The messages are serialized using the GWT serialization logic.  This logic is adapted from a talk/presentation at GWTcon 2014
 * about using gwt serialization with websockets.  
 * - https://www.slideshare.net/gwtcon/gwt20-websocket20and20data20serialization
 * 
 * @author nblomberg
 *
 */
public class WebSocketServerSerializationManager  {

    /** The instnace of the logger. */
    private static Logger logger = LoggerFactory.getLogger(WebSocketServerSerializationManager.class.getName());
   
    /** Instance of the web socket serialization manager (singleton) */
    private static WebSocketServerSerializationManager instance = null;
   
    /**
     * Constructor - private (for singleton purposes).
     */
    private WebSocketServerSerializationManager() {
        logger.info("WebSocketServerSerializationManager()");
    }
    
    /**
     * Get an instance of the singleton object.  The instance will be
     * created if it doesn't yet exist.
     * 
     * @return Instance of the web socket server serialization manager.  
     */
    public static WebSocketServerSerializationManager getInstance() {
        
        if (instance == null) {
            // Create the instance if it doesn't exist yet.
            instance = new WebSocketServerSerializationManager();
        }
        
        return instance;
    }
    
    /**
     * Deserializes the data from the websocket into an AbstractWebSocketMessage.  The gwt serialization
     * logic is used to deserialize the data.
     * 
     * @param data The data to be deserialized.  
     * @return AbstractWebSocketMessage The websocket message that was deserialized.  Null is returned if the data could not be deserialized. 
     */
    public AbstractWebSocketMessage deserializeMessage(String data) {
        AbstractWebSocketMessage message = null;
        try {
            ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(Thread.currentThread().getContextClassLoader(), 
                    new CustomSerializationPolicyPovider());
            
            streamReader.prepareToRead(data);
            
            message = (AbstractWebSocketMessage)streamReader.readObject();
            
        } catch (Exception e) {
            logger.error("Error deserializing webspclet message.", e);
        }
        
        return message;
    }
    
    /**
     * Serializes the AbstractWebSocketMessage into a text/string object that will be sent onto
     * the websocket (text based websocket).   The gwt serialization logic is used to serialize
     * the data.
     * 
     * @param message The AbstractWebSocketMessage to be serialized.
     * @return String the serialized message data.  Null is returned if there was an error during serialization.
     */
    public String serializeMessage(AbstractWebSocketMessage message) {
        String data = null;
        try {
            ServerSerializationStreamWriter streamWriter = 
                    new ServerSerializationStreamWriter(new SimpleSerializationPolicy());
            streamWriter.writeObject(message);
            data = streamWriter.toString();
        } catch (Exception e) {
            logger.error("Error serializing websocket message.", e);
        }
        
        return data;
    }
    

    /**
     * The CustomSerializationPolicyProvider used by the web socket implementation.  This class is adapted from the
     * GWTcon 2014 presentation here:  https://www.slideshare.net/gwtcon/gwt20-websocket20and20data20serialization
     * @author nblomberg
     *
     */
    private class CustomSerializationPolicyPovider implements SerializationPolicyProvider {

        @Override
        public SerializationPolicy getSerializationPolicy(String arg0, String arg1) {

            return new SimpleSerializationPolicy();
        }
        
    }
    
    /**
     * The SerializationPolicy class that is used by the web socket implementation.  This class is adapted from
     * the GWTcon 2014 presentation here:  https://www.slideshare.net/gwtcon/gwt20-websocket20and20data20serialization
     * 
     * @author nblomberg
     *
     */
    private class SimpleSerializationPolicy extends SerializationPolicy {

        @Override
        public boolean shouldDeserializeFields(Class<?> clazz) {
            return isSerializable(clazz);
        }

        @Override
        public boolean shouldSerializeFields(Class<?> clazz) {
            return isSerializable(clazz);
        }

        @Override
        public void validateDeserialize(Class<?> arg0) throws SerializationException {
            // Nothing to do here yet.
            
        }

        @Override
        public void validateSerialize(Class<?> arg0) throws SerializationException {
            // Nothing to do here yet.
            
        }
        
        /**
         * Determines if the class is serializaable.
         * 
         * @param clazz The class to check against.
         * @return True if the class is serializable, false otherwise.
         */
        public boolean isSerializable(Class<?> clazz) {
            boolean isSerializable = false;
            if (clazz != null) {
                if (clazz.isPrimitive() ||
                     Serializable.class.isAssignableFrom(clazz) ||
                     IsSerializable.class.isAssignableFrom(clazz)) {
                    isSerializable = true;
                }
            }
            
            return isSerializable;
        }
        
    }
}
