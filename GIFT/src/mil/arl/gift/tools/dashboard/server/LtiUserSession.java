/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

/**
 * A class which will contain a unique id of the usersession for Lti Tool Consumer 'users'.
 * Unlike a normal 'UserSession' class which typically represents a logged in user, the 'LtiUserSession'
 * is used to represent sessions for anonymous Lti Tool Consumer users.
 * This data will be stored in the httpSession data on the server.
 * The client will get the session/browser ids.
 *   
 * @author nblomberg
 *
 */
public class LtiUserSession extends DashboardHttpSessionData {

    /** The consumer key of the lti user. */
    private String consumerKey = "";
    /** The consumer id of the lti user. */
    private String consumerId = "";
    
    /**
     * Constructor
     * 
     * @param consumerKey The consumer key of the lti user.
     * @param consumerId The consumer id of the lti user.
     */
    public LtiUserSession(String consumerKey, String consumerId) {
        // The LtiUserSession does not use the username/password fields so set those to null.
        super(null, null);
        
        setConsumerKey(consumerKey);
        setConsumerId(consumerId);
    }

    /**
     * Accessor to get the consumer key.
     * 
     * @return
     */
    public String getConsumerKey() {
        return consumerKey;
    }
    
    /**
     * Accessor to set the consumer key
     * 
     * @param consumerKey
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }
    
    /**
     * Accessor to get the consumer id.
     * @return
     */
    public String getConsumerId() {
        return consumerId;
    }
    
    /**
     * Accessor to set the consumer id
     * @param consumerId
     */
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }
    
    @Override
    public String toString() {
        return "LtiUserSession [sessionId=" + getUserSessionId()
                + ", consumerKey="
                + getConsumerKey() + ", consumerId=" 
                + getConsumerId() + "]";
    }
    
}
