/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;

import java.util.Date;


/**
 * The LtiUserRecord class encapsulates the response data from a valid
 * LtiGetUser request.  
 * 
 * @author nblomberg
 *
 */
public class LtiUserRecord {

    
    /** The unique lti tool consumer user id.  This identifies the user uniquely within each tool consumer. */
    private LtiUserId ltiUserId;
    
    /** The most recent time of the lti launch request for the lti user. */
    private Date ltiTimestamp;
    
    /** The global user id that corresponds to the lti user. */
    private int globalUserId;

    /**
     * Constructor
     * @param ltiUserId The lti user id that is provided by the Tool Consumer.
     * @param globalUserId The global user id that corresponds to the lti user.
     * @param giftUsername The gift user name that corresponds to the lti user.
     * @param epochTimestamp The most recent time of the lti launch request for the lti user.
     */
    public LtiUserRecord(LtiUserId ltiUserId, int globalUserId, long epochTimestamp) { 
        setLtiUserId(ltiUserId);
        setGlobalUserId(globalUserId);
        setLtiTimestamp(new Date(epochTimestamp));
    }

    /**
     * @return the ltiUserId
     */
    public LtiUserId getLtiUserId() {
        return ltiUserId;
    }

    /**
     * @param ltiUserId the ltiUserId to set
     */
    public void setLtiUserId(LtiUserId ltiUserId) {
        this.ltiUserId = ltiUserId;
    }

    /**
     * @return the timestamp
     */
    public Date getLtiTimestamp() {
        return ltiTimestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setLtiTimestamp(Date timestamp) {
        this.ltiTimestamp = timestamp;
    }

    /**
     * @return the globalUserId
     */
    public int getGlobalUserId() {
        return globalUserId;
    }

    /**
     * @param globalUserId the globalUserId to set
     */
    public void setGlobalUserId(int globalUserId) {
        this.globalUserId = globalUserId;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LtiUserRecord: ");
        sb.append("ltiUserId = ").append(getLtiUserId());
        sb.append(", globalUserId = ").append(getGlobalUserId());
        sb.append(", ltiTimestamp = ").append(getLtiTimestamp());
        sb.append("]");
        return sb.toString();
    }
}
