/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * The TrustedLtiConsumer class represents the data for an LTI Tool Consumer that is
 * trusted to access GIFT (as an LTI Tool Provider).  Each LTI Tool Consumer that is registered
 * with GIFT must have a unique consumer key.  This class currently is serialized by GSON encoder
 * and must comply with the formats / expectations of the GSON encoder.  Eventually this class
 * may represent data from a database (such as Hibernate).
 * 
 * @author nblomberg
 *
 */
public class TrustedLtiConsumer {  
    
    /** A unique token reserved to concatenate the consumer name with the consumer id to create a unique string representation
     *  of the lti user.
     */
    public final static String RESERVED_TOKEN = "___";
    
    /** The internal name used to identify the consumer. This must be unique per Tool Consumer. */
    private String name;
    /** The consumer key that must be unique per Tool Consumer.  This typically is a UUID that is provided by the Tool Provider
     *  to uniquely identify the consumer.  
     */
    private String consumerKey;
    /** The shared secret that will be used to identify the incoming lti request from the consumer.
     *  The shared secret is typically a UUID that is provided by the Tool Provider and should be unique per consumer and per environment
     *  such as development or production. 
     */
    private String consumerSharedSecret;
    
    /** A static value to be used as a unique id that will be used for the lti course runtime folder name. */
    private static AtomicInteger runtimeUniqueId = new AtomicInteger();

    /** Constructor 
     * 
     * @param name - The name of the consumer.
     * @param key - The unique key used to identify the consumer.  Typically this is a UUID that is generated for the consumer by the Tool Provider.
     * @param secret - The secret (also a UUID) that is used to authenticate requests from the Tool Consumer. 
     * @throws Exception
     */
    public TrustedLtiConsumer(String name, String key, String secret) throws IllegalArgumentException {        
        
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Consumer key cannot be null or empty.");
        }
        
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Consumer secret cannot be null or emtpy.");
        }
        
        setName(name);
        setConsumerKey(key);
        setConsumerSharedSecret(secret);
       
    }

    /** 
     * Gets the name of the consumer.
     * 
     * @return String the name of the consumer.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the consumer.
     * @param name the name of the consumer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the key for the consumer.  The key is typically created by the Tool Provider to
     * uniquely identify the consumer.
     * 
     * @return String the key (UUID) that uniquely identifies the consumer. 
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * Sets the key for the consumer. The key is typically created by the Tool Provider to
     * uniquelyl identify the consumer.
     * @param consumerKey the key (UUID) that uniquely identifies the consumer. 
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * Gets the consumer shared secret.  The secret is typically a UUID created by the Tool Provider
     * that should be unique per consumer and per environment (development, production).
     * 
     * @return String the shared secret (UUID) that is used to authenticate requests from a Tool Consumer.
     */
    public String getConsumerSharedSecret() {
        return consumerSharedSecret;
    }
    
    /**
     * Static utility method to create a unique string composite value consisting of the internal consumer name field
     * along with the consumer id.  This is used in several places such as a unique name that can be used in both the
     * giftuser table -- username and lmsusername fields.   It also is used to generate a unique value for the lti user
     * in the gift runtime folder (which uses a username to copy the courses into).
     * 
     * @param consumer The trusted lti user object containing the consumer name.
     * @param consumerId The consumer id of the lti user.
     * @return Composite string value consisting of the internal consumer name field along with the consumer id for an lti user.
     *  An example would be in the format '[consumer name]___[consumer id]' where the identifier '___' is used as a token to 
     *  concatenate the values.
     */
    public static String getInternalUniqueConsumerId(TrustedLtiConsumer consumer, String consumerId) {
        
        String uniqueId = "";
        
        if (consumer == null || consumer.getName() == null || consumer.getName().isEmpty()) {
           throw new IllegalArgumentException("The consumer parameter cannot be null and the name must not be null or empty.");
        }
        
        if (consumerId == null || consumerId.isEmpty()) {
            throw new IllegalArgumentException("The consumerId cannot be null and the name must not be empty.");
        }
        uniqueId = consumer.getName() + TrustedLtiConsumer.RESERVED_TOKEN + consumerId;
        return uniqueId;
    }
    
    /**
     * Static method to creates a unique folder name that will be created at the root runtime folder level.  The folder name
     * is in the form [consumerName]___[runtimeId], where runtimeId is an integer value.  The integer value
     * that is assigned is reset each server boot, so the folder names will and can recycle once the server
     * is rebooted.  However during the life that the server is running, a unique id is generated for each
     * lti launch request.  
     * 
     * This method is used so that the runtime folder name is kept smaller so that the maximum character
     * path limit is not exceeded when accessing courses (which could occur if the consumer GUIDs would be 
     * used).
     * 
     * @param consumer The trusted lti consumer data to use as the prefix for the folder name.
     * @return String a unique runtime folder name consisting of the lti consumer name as the prefix, plus a unique integer id.
     * 
     */
    public static String createUniqueRuntimeFolderName(TrustedLtiConsumer consumer) {
        
        String uniqueId = "";
        
        if (consumer == null || consumer.getName() == null || consumer.getName().isEmpty()) {
           throw new IllegalArgumentException("The consumer parameter cannot be null and the name must not be null or empty.");
        }
        
       
        uniqueId = consumer.getName() + TrustedLtiConsumer.RESERVED_TOKEN + runtimeUniqueId.incrementAndGet();
        return uniqueId;
    }

    /**
     * Sets the consumer shared secret.  The secret is typically a UUID created by the Tool Provider
     * that should be unique per consumer and per environment (development, production).
     * 
     * @param consumerSharedSecret the shared secret (UUID) that is used to authenticate requests from a Tool Consumer.
     */
    public void setConsumerSharedSecret(String consumerSharedSecret) {
        this.consumerSharedSecret = consumerSharedSecret;
    }
    
    
    @Override
    public String toString() {
        return "Consumer [name=" + getName() + ", key=" + getConsumerKey() + ", shared secret=" + getConsumerSharedSecret() + "]";
    }
      
    
    

}
