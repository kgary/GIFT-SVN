/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

import java.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.net.api.message.codec.json.MessageJSONCodec;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;

/**
 * This class contains logic to decode raw messages into a GIFT message based upon various encoding schemes.
 * 
 * @author mhoffman
 *
 */
public class MessageUtil {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MessageUtil.class);

    /**
     * Private constructor - for now there is no need to instantiate this class
     */
    private MessageUtil(){}
    
    /**
     * Decode the message string based on the encoding type.
     * 
     * @param str - the message string
     * @param encodingType - the type of encoding used on the message
     * @return Message - the message created using the string
     * @throws MessageDecodeException if there was a problem decoding the string into a GIFT message
     * @throws DetailedException if there was a problem parsing the message string based on the encoding type provided
     */
    public static Message getMessageFromString(String str, MessageEncodingTypeEnum encodingType) throws MessageDecodeException, DetailedException {

        Message msg = null;
        
        if(encodingType == MessageEncodingTypeEnum.JSON){
            
            JSONObject jsonObj;
            try{
                jsonObj = (JSONObject) (JSONValue.parse(str));
            }catch(Exception e){
                throw new DetailedException("There was a problem decoding a message string using the "+encodingType+" encoding type.", "The message string that was not parsable was:\n"+str, e);
            }

            if (jsonObj != null) {

                msg = MessageJSONCodec.decode(jsonObj);

            } else {
                logger.error("JSON was unable to parse string message of " + str);
            }
        } else if (encodingType == MessageEncodingTypeEnum.BINARY) {
            try {
                /* Base64 encoding schemes are commonly used when there is a
                 * need to encode binary data that needs be stored and
                 * transferred over media that are designed to deal with textual
                 * data. This is to ensure that the data remains intact without
                 * modification during transport. */
                final byte[] bytes = Base64.getDecoder().decode(str);
                ProtobufMessage protobufMessage = ProtobufMessage.parseFrom(bytes);
                msg = new ProtobufMessageProtoCodec().convert(protobufMessage);
            } catch (InvalidProtocolBufferException e) {
                logger.error("There was a problem parsing the data as a protobuf message.", e);
                throw new DetailedException("There was a problem parsing the data as a protobuf message.",
                        "Parsing the protobuf message caused an InvalidProtocolBufferException.", e);
            }
        } else {
            logger.error("There is no support for decoding message of encoding type "+encodingType+", therefore the message = "+str+" will not be handled");
        }
        
        if(msg == null){
            throw new MessageDecodeException("MessageUtil", "Unable to decode message, check log for more details");
        }

        return msg;
    }

}
