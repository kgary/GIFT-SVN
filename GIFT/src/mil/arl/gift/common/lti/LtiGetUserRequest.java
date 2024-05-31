/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;

/**
 * The LtiGetUserRequest class encapsulates the data needed to request an lti user record
 * from the UMS ltiuserrecord database table.
 * 
 * @author nblomberg
 *
 */
public class LtiGetUserRequest {

   /** The consumerKey of the lti user to find. */
    private String consumerKey;

    /** The consumerId of the lti user to find. */
    private String consumerId;
    
    /**
     * Class constructor
     * 
     * @param userId the unique GIFT user id of a user wanting to login
     */
    public LtiGetUserRequest(String consumerKey, String consumerId){
        this.setConsumerKey(consumerKey);
        this.setConsumerId(consumerId);
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
     * @param consumerId the consumerId to set
     */
    private void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }



    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LtiGetUserRequest: ");
        sb.append("consumerKey = ").append(getConsumerKey());
        sb.append(", consumerId = ").append(getConsumerId());
        sb.append("]");
        return sb.toString();
    }
}
