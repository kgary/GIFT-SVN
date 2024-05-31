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
import mil.arl.gift.net.embedded.message.EmbeddedSimpleExampleState;
import mil.arl.gift.net.json.JSONCodec;

public class EmbeddedSimpleExampleStateJSON implements JSONCodec {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedSimpleExampleStateJSON.class);

    /** JSON string keys */
    private static final String VAR = "VAR";

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        try{
            String var = (String) jsonObj.get(VAR);

            return new EmbeddedSimpleExampleState(var);

        }catch(Exception e){
            logger.error("caught exception while creating "+this.getClass().getName()+" from "+jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {

        EmbeddedSimpleExampleState state = (EmbeddedSimpleExampleState) payload;

        jsonObj.put(VAR, state.getVar());

    }

}
