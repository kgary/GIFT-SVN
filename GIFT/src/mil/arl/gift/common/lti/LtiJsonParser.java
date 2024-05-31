/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;


import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import generated.course.LtiProvider;


/** The LtiJsonParser class is responsible for parsing any Lti Json properties in the GIFT configuration file.
 * 
 * @author nblomberg
 *
 */
public class LtiJsonParser {  
    
    /**
     * Custom exception class used when lti parsing exceptions occur.
     * 
     * @author nblomberg
     *
     */
    public class LtiPropertyParseException extends Exception {

        /** Default serialization id. */
        private static final long serialVersionUID = 1L;
        
        /** Constructor
         * 
         * @param message Message for the exception.
         */
        public LtiPropertyParseException(String message) {
            super(message);
        }
    }
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LtiJsonParser.class);

    /** Expected key used in the json object for the trusted consumer array. */
    private static final String TRUSTED_CONSUMER_JSON_KEY = "trusted_consumers";
    
    /** Expected key used in the json object for the trusted provider array. */
    private static final String TRUSTED_PROVIDER_JSON_KEY = "trusted_providers";
    
    /**
     * Constructor - default
     */
    public LtiJsonParser(){        
       
    }
      
    
    /**
     * Parses the trusted consumers list json object.  An example of this json format (for a single entry) is:
     *   { "trusted_consumers": [ { "name" : "testConsumer", "consumerKey":"testUUIDForKey", "consumerSharedSecret":"testUUIDForSecret"} ] }
     * 
     * @param consumerJson JSON formatted string containing the list of trusted consumers.  
     * @return Map containing the list of trusted consumer objects based on the consumer key as a unique key value in the map.
     *         A null map is returned if there is an error parsing the json object.
     */
    public HashMap<String, TrustedLtiConsumer> parseTrustedConsumerListJson(String consumerJson) {
        
        HashMap<String, TrustedLtiConsumer> trustedConsumerMap = new HashMap<String, TrustedLtiConsumer>();

        try {
            JSONObject rootObj = (JSONObject) new JSONParser().parse(consumerJson);
            if (rootObj != null) {
                
                
                JSONArray list = (JSONArray) rootObj.get(TRUSTED_CONSUMER_JSON_KEY);
                
                if (list != null && !list.isEmpty()) {
                    Gson gson = new Gson();

                    for (Object obj : list) {
                        
                        JSONObject consumerObj = (JSONObject) obj;

                        TrustedLtiConsumer consumer = gson.fromJson(consumerObj.toJSONString(), TrustedLtiConsumer.class);
                        
                        if (consumer != null) {
                            TrustedLtiConsumer existingConsumer = trustedConsumerMap.get(consumer.getConsumerKey());
                            if (existingConsumer == null) {
                                
                                if (consumer.getName() == null || consumer.getName().isEmpty()) {
                                    throw new LtiPropertyParseException("Found an invalid consumer name that is null or empty.");
                                }
                                
                                if (consumer.getName().contains(TrustedLtiConsumer.RESERVED_TOKEN)) {
                                    throw new LtiPropertyParseException("Found an invalid consumer name.  The consumer name cannot contain the reserved token: '" + TrustedLtiConsumer.RESERVED_TOKEN + "'");
                                }
                                
                                if (consumer.getConsumerKey() == null || consumer.getConsumerKey().isEmpty()) {
                                    throw new LtiPropertyParseException("Found an invalid consumer key that is null or empty.");
                                }
                                
                                if (consumer.getConsumerSharedSecret() == null || consumer.getConsumerSharedSecret().isEmpty()) {
                                    throw new LtiPropertyParseException("Found an invalid consumer shared secret that is null or empty.");
                                }
                                
                                // Check for duplicate names.  The consumer name must be unique.
                                for (TrustedLtiConsumer lookup : trustedConsumerMap.values()) {
                                    if (lookup.getName().compareTo(consumer.getName()) == 0) {
                                        throw new LtiPropertyParseException("Found a duplicate consumer name: " + consumer.getName() + ".  The consumer name must be unique.");
                                    }
                                }
                                
                                trustedConsumerMap.put(consumer.getConsumerKey(), consumer);
                                
                                logger.info("Adding consumer to map: " + consumer);
                            } else {
                                throw new LtiPropertyParseException("Duplicate consumer key found.  : " + consumer.getConsumerKey());
                            }
                        } else {
                            throw new LtiPropertyParseException("Consumer value was null when parsing the TrustedLtiConsumers property.");
                        }
                       
                    }
                    
                }
            }
        } catch (ParseException e) {
            logger.error("Parse Exception caught while parsing the LTI Consumer json object: ", e);
            trustedConsumerMap = null;
        } catch (LtiPropertyParseException e) {
            logger.error("LtiPropertyParseException caught while parsing the LTI consumer json object: ", e);
            trustedConsumerMap = null;
        } catch (Exception e) {
            logger.error("General exception caught parsing the LTI consumer json object: ", e);
            trustedConsumerMap = null;
        }

        return trustedConsumerMap; 
    }
    
    /**
     * Parses the trusted providers list json object. An example of this json format (for a single
     * entry) is: { "trusted_providers": [ { "name" : "testProvider",
     * "providerKey":"testUUIDForKey", "providerSharedSecret":"testUUIDForSecret"} ] }
     * 
     * @param providerJson JSON formatted string containing the list of trusted providers.
     * @return Map containing the list of trusted provider objects based on the provider key as a
     *         unique key value in the map. A null map is returned if there is an error parsing the
     *         json object.
     */
    public HashMap<String, LtiProvider> parseTrustedProviderListJson(String providerJson) {

        HashMap<String, LtiProvider> trustedProviderMap = new HashMap<String, LtiProvider>();

        try {
            JSONObject rootObj = (JSONObject) new JSONParser().parse(providerJson);
            if (rootObj != null) {

                JSONArray list = (JSONArray) rootObj.get(TRUSTED_PROVIDER_JSON_KEY);

                if (list != null && !list.isEmpty()) {
                    Gson gson = new Gson();

                    for (Object obj : list) {

                        JSONObject providerObj = (JSONObject) obj;

                        LtiProvider provider = gson.fromJson(providerObj.toJSONString(), LtiProvider.class);

                        if (provider != null) {
                            LtiProvider existingProvider = trustedProviderMap.get(provider.getKey());
                            if (existingProvider == null) {

                                if (provider.getIdentifier() == null || provider.getIdentifier().isEmpty()) {
                                    throw new LtiPropertyParseException("Found an invalid provider name that is null or empty.");
                                }

                                if (provider.getKey() == null || provider.getKey().isEmpty()) {
                                    throw new LtiPropertyParseException("Found an invalid provider key that is null or empty.");
                                }

                                if (provider.getSharedSecret() == null || provider.getSharedSecret().isEmpty()) {
                                    throw new LtiPropertyParseException("Found an invalid provider shared secret that is null or empty.");
                                }

                                // Check for duplicate identifiers. The provider identifier must be
                                // unique.
                                for (LtiProvider lookup : trustedProviderMap.values()) {
                                    if (lookup.getIdentifier().compareTo(provider.getIdentifier()) == 0) {
                                        throw new LtiPropertyParseException("Found a duplicate provider identifier: " + provider.getIdentifier()
                                                + ".  The provider name must be unique.");
                                    }
                                }

                                trustedProviderMap.put(provider.getKey(), provider);

                                logger.info("Adding provider to map: " + provider);
                            } else {
                                throw new LtiPropertyParseException("Duplicate provider key found.  : " + provider.getKey());
                            }
                        } else {
                            throw new LtiPropertyParseException("Provider value was null when parsing the LtiProvider property.");
                        }

                    }

                }
            }
        } catch (ParseException e) {
            logger.error("Parse Exception caught while parsing the LTI Provider json object: ", e);
            trustedProviderMap = null;
        } catch (LtiPropertyParseException e) {
            logger.error("LtiPropertyParseException caught while parsing the LTI provider json object: ", e);
            trustedProviderMap = null;
        } catch (Exception e) {
            logger.error("General exception caught parsing the LTI provider json object: ", e);
            trustedProviderMap = null;
        }

        return trustedProviderMap;
    }

}



