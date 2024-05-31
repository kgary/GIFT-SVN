/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Class with static methods only to encode and decode the packets used for the remote launch capability.
 * 
 * @author cragusa
 *
 */
public class RemoteMessageUtil {
    
    private static Logger logger = LoggerFactory.getLogger(RemoteMessageUtil.class);
    
    /**Size of buffer used to receive incoming packets. WARNING: This may need to be increased if messages get larger */
    public static final int RECV_BUFFER_SIZE = 128;
    
    /**The header use for all remote launch messages including heartbeats */
    private static final String GIFT_REMOTE_MSG_HEADER = "gift_remote";   
    
    /**Delimeter to use for encoding and decoding messages */
    private static final char DELIMETER = '|';
    
    /** Number of tokens expected in header of message */
    private static final int HEADER_TOKEN_COUNT = 2;
    
    /** Count of one to reflect the payload token count when the payload is treated as a single token */
    private static final int SINGLE_PAYLOAD_TOKEN = 1;
    
    //private static final int MSG_HDR_INDEX      = 0;
    /** Index of the message type value */
    private static final int MSG_TYPE_INDEX     = 1;
    
    /** Index of the payload value */
    private static final int MSG_PAYLOAD_INDEX  = 2; //not header
       
    /** Private constructor to prevent instantiation*/
    private RemoteMessageUtil() {}
    
    
    /**
     * Encodes an EncodableDecodable object to a String.
     * 
     * @param encodable object to encode
     * @return the string containing the encoded information
     * @throws IOException if there was a problem encoding the object
     */
    public static String encode(EncodableDecodable encodable) throws IOException {        
        
        //header
        StringBuffer buffer = new StringBuffer();        
        buffer.append(GIFT_REMOTE_MSG_HEADER);
        buffer.append(DELIMETER);
        buffer.append(encodable.getMessageType());
        
        //payload
        buffer.append(DELIMETER);
        encodable.encode(buffer, DELIMETER);
        
        if(logger.isDebugEnabled()) {
            
            logger.debug("encodable was encoded as follows: " + buffer.toString());
        }
        
        return buffer.toString();
    }
       
    
    /**
     * Decodes a string to an EncodableDecodable object.
     * @param string the string to decode
     * @return an EncodableDecodable object or else null if the string is not recognized.
     * @throws IOException if there was a problem decoding the string
     */
    public static EncodableDecodable decode(String string) throws IOException{
        
        if( !string.startsWith(GIFT_REMOTE_MSG_HEADER) ) {   
            
            return null;
        }
        
        String[] tokens = string.split("\\" + DELIMETER, HEADER_TOKEN_COUNT + SINGLE_PAYLOAD_TOKEN);  
        
        EncodableDecodable decodable = null;
        
        //TODO: make sure i get three tokens!
        //TODO: check that I get a valid type!
        RemoteMessageType type = RemoteMessageType.valueOf(tokens[MSG_TYPE_INDEX]);
        
        if( type == RemoteMessageType.HOST_INFO) { 
            
            decodable = new HostInfo(); 
        }
        else if( type == RemoteMessageType.LAUNCH_COMMAND) {
            decodable = new LaunchCommand();
        }
        
        if(decodable != null) {
            decodable.decode(tokens[MSG_PAYLOAD_INDEX], DELIMETER);
            return decodable;
        }
        else { 
            
            return null;
        }
    }
}

