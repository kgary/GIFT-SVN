/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message.codec.json;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.json.JSONCodec;

/**
 * A payload consisting of a number array specifying a pattern of vibrations and pauses, in milliseconds
 *
 * @author nroberts
 */
public class EmbeddedVibrateDeviceJSON implements JSONCodec {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedVibrateDeviceJSON.class);

    /** message attribute names */
    private static final String PATTERN_ARRAY_PROPERTY = "PatternArray";

    @SuppressWarnings("unchecked")
    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        try {
            JSONArray patternArray = (JSONArray) jsonObj.get(PATTERN_ARRAY_PROPERTY);

            return new ArrayList<Integer>(patternArray);

        } catch (Exception e) {
            logger.error("Caught exception while creating a pattern array from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, Object payload) {

        List<Integer> pattern = (List<Integer>) payload;

        JSONArray patternArray = new JSONArray();
        patternArray.addAll(pattern);

        jsonObj.put(PATTERN_ARRAY_PROPERTY, patternArray);
    }
}
