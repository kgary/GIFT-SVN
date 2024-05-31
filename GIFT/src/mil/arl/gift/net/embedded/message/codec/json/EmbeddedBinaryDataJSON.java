/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message.codec.json;

import java.util.Base64;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedBinaryData;
import mil.arl.gift.net.json.JSONCodec;

/**
 * This class is used to wrap a DIS Packet that is binary encoded into a JSONObject.
 * The binary data is encoded as base64 string before being written into the JSONObject.
 * Conversely, the binary data is decoded from the string using base64 decoder.
 *
 * When sending the binary data, care must be taken that the actual binary data is written
 * in Big Endian order.
 *
 * @author nblomberg
 *
 */
public class EmbeddedBinaryDataJSON implements JSONCodec {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedBinaryDataJSON.class);


    /** JSON string keys */
    /** An indicator of the type of binary data */
    private static final String DATA_TYPE = "DATA_TYPE";
    /** The actual binary data (blob) that is encoded as JSON string */
    private static final String BINARY_DATA = "BINARY_DATA";


    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        try{
            // Get the type of binary data.
            String dataTypeStr = (String) jsonObj.get(DATA_TYPE);
            EmbeddedBinaryData.BinaryDataType dataType = EmbeddedBinaryData.BinaryDataType.valueOf(dataTypeStr);

            // Get the actual binary data (blob) as a base64 encoded string
            String binaryPacketStr = (String) jsonObj.get(BINARY_DATA);
            byte[] data = Base64.getDecoder().decode(binaryPacketStr);

            return new EmbeddedBinaryData(dataType, data);

        }catch(Exception e){
            logger.error("caught exception while creating "+this.getClass().getName()+" from "+jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {

        EmbeddedBinaryData binaryPacketData = (EmbeddedBinaryData) payload;

        jsonObj.put(DATA_TYPE,  binaryPacketData.getDataType());
        // Encode the binary data as a base64 encoded string.
        jsonObj.put(BINARY_DATA, binaryPacketData.encodeAsString());

    }

}
