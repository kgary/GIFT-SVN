/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.json;

import mil.arl.gift.net.api.message.MessageDecodeException;

import org.json.simple.JSONObject;

/**
 * This class should be implemented by all classes which JSON encode/decode payload classes (e.g. Module Status)
 * 
 * @author mhoffman
 */
public interface JSONCodec {

    /**
     * Decode the json encoding of a class
     * 
     * @param jsonObj - the encoding of a class
     * @return Object - the class instance created by decoding the json object
     * @throws MessageDecodeException if there was a problem decoding the json object
     */
    Object decode(JSONObject jsonObj) throws MessageDecodeException;
    
    /**
     * Encode a class in JSON
     * 
     * @param jsonObj - the JSON object to populate
     * @param payload - the class containing attributes to encoded
     */
    void encode(JSONObject jsonObj, Object payload);
}
