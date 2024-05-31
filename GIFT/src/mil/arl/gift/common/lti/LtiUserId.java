/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;

import java.io.Serializable;


/**
 * The LtiUserId class encapsulates the identifier that makes an LTI user 
 * unique to GIFT (which consists of the consumer key along with the consumer id).  
 * 
 * @author nblomberg
 *
 */
public class LtiUserId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The consumer key of the lti user. */
    private String consumerKey;

    /** The consumer id of the lti user. */
    private String consumerId;
    
    /** Used as a separator token to combine the consumer key and consumer id in a single string token */
    private final static String UNIQUE_SEPARATOR_TOKEN = "___";

    /**
     * Default constructor
     */
    public LtiUserId() {
        
    }
    
    /**
     * Constructor
     * @param consumerKey The consumer key of the lti user.
     * @param consumerId The consumer id of the lti user.
     */
    public LtiUserId(String consumerKey, String consumerId) { 
        setConsumerKey(consumerKey);
        setConsumerId(consumerId);
    }
    
    /**
     * @return the consumerKey
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * @param consumerKey the consumerKey to set
     */
    private void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * @return the consumerId
     */
    public String getConsumerId() {
        return consumerId;
    }
    
    /**
     * @return consumerId the consumerId to set
     */
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }
    
    /** 
     * Utility method to create a composite consumer name (single string) which combines the consumer key 
     * and the consumer id in a single string (separated by a unique token).  Once place this
     * is used is to generate a unique lms username for GIFT.
     * 
     * @param user The LTI user to get a composite consumer name for.
     * @return The combined consumer name (based on the consumer key and consumer id of the lti user).  Can return null.
     */
    public static String getCompositeConsumerName(LtiUserId user) {
        if (user != null) {
            
            StringBuilder sb = new StringBuilder();
            sb.append(user.getConsumerKey());
            sb.append(UNIQUE_SEPARATOR_TOKEN);
            sb.append(user.getConsumerId());
            return sb.toString();
        }
        
        return null;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LtiUserId: ");
        sb.append("consumerKey = ").append(getConsumerKey());
        sb.append(", consumerId = ").append(getConsumerId());
        sb.append("]");
        return sb.toString();
    }
}
