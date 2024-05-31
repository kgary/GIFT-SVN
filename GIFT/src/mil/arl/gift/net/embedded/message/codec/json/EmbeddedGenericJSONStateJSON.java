/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message.codec.json;

import java.util.UUID;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedGenericJSONState;
import mil.arl.gift.net.json.JSONCodec;

/**
 * This class is responsible for JSON encoding/decoding a Generic JSON State object.
 *
 * @author mhoffman
 *
 */
public class EmbeddedGenericJSONStateJSON implements JSONCodec {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedGenericJSONStateJSON.class);

    /** The key that specifies the generic JSON object */
    private static final String JSON_OBJECT = "OBJECT";

    /** Message attribute names that are required by the message. */
    private static final String UNIQUE_ID = "messageUniqueId";

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        EmbeddedGenericJSONState state = null;
        try {

            if(jsonObj.containsKey(JSON_OBJECT)){
                JSONObject obj = (JSONObject) jsonObj.get(JSON_OBJECT);
                state = new EmbeddedGenericJSONState(UUID.fromString((String) obj.get(UNIQUE_ID)));
                state.addAll(obj);
            }else{
                //Legacy
                state = new EmbeddedGenericJSONState(UUID.fromString((String) jsonObj.get(UNIQUE_ID)));
                state.addAll(jsonObj);
            }

        } catch (Exception e) {
            logger.error("Caught exception while creating "
                + this.getClass().getName() + " from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(),
                "Exception logged while decoding payload.");
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {
        EmbeddedGenericJSONState state = (EmbeddedGenericJSONState) payload;

    	//place the Generic JSON objects into its own object to separate it from the
    	//GIFT message JSON object values
    	JSONObject obj = new JSONObject();
    	obj.put(UNIQUE_ID, state.getUUID().toString());
    	obj.putAll(state.getJSONObject());

    	jsonObj.put(JSON_OBJECT, obj);
    }
}
