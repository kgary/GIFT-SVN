/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.json.JSONCodec;

/**
 * A simple payload consisting of a single string
 *
 * @author jleonard
 */
public class EmbeddedStringPayloadJSON implements JSONCodec {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedStringPayloadJSON.class);

    /** message attribute names */
    private static final String STRING_PAYLOAD_PROPERTY = "StringPayload";

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        String string = null;

        try {
            string = (String) jsonObj.get(STRING_PAYLOAD_PROPERTY);

        } catch (Exception e) {
            logger.error("Caught exception while creating a string payload from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

        if (string == null) {

            throw new MessageDecodeException(this.getClass().getName(), "There is no string payload");
        }

        return string;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, Object payload) {

        String string = (String) payload;
        jsonObj.put(STRING_PAYLOAD_PROPERTY, string);
    }
}
