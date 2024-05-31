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
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.embedded.message.EmbeddedStopFreeze;
import mil.arl.gift.net.json.JSONCodec;

/**
 * This is the stop/freeze message
 *
 * @author mhoffman
 *
 */
public class EmbeddedStopFreezeJSON implements JSONCodec {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedStopFreezeJSON.class);

    /** message attribute names */
    private static final String REAL_TIME   = "realWorldTime";
    private static final String REASON      = "reason";
    private static final String FROZEN_BEH  = "frozenBehavior";
    private static final String REQUEST_ID  = "requestID";

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException{
        long realWorldTime, requestID;
        int reason, frozenBehavior;

        try{
            realWorldTime = (Long)jsonObj.get(REAL_TIME);
            reason = ((Long)jsonObj.get(REASON)).intValue();
            frozenBehavior = ((Long)jsonObj.get(FROZEN_BEH)).intValue();
            requestID = (Long)jsonObj.get(REQUEST_ID);

        }catch(Exception e){
            logger.error("caught exception while creating "+this.getClass().getName()+" from "+jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

        return new EmbeddedStopFreeze(realWorldTime, reason, frozenBehavior, requestID);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) throws MessageEncodeException{
        EmbeddedStopFreeze stopFreeze = (EmbeddedStopFreeze) payload;

        jsonObj.put(REAL_TIME, stopFreeze.getRealWorldTime());
        jsonObj.put(REASON, stopFreeze.getReason());
        jsonObj.put(FROZEN_BEH, stopFreeze.getFrozenBehavior());
        jsonObj.put(REQUEST_ID, stopFreeze.getRequestID());
    }

}
