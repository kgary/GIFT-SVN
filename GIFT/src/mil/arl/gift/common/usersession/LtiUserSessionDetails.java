/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.usersession;

import java.io.Serializable;

import mil.arl.gift.common.lti.LtiUserId;

/**
 * The LtiUserSessionDetails class provides additional details about the user session
 * that pertain to the LTI specification.  This includes data such as the lti user id, 
 * the data set id (if the session belongs to data collection), and the global user id.
 * 
 * @author nblomberg
 *
 */
public class LtiUserSessionDetails extends AbstractUserSessionDetails implements Serializable{

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** The lti user id of the user. */
    private LtiUserId ltiUserId;
    
    /** The data collection data set id that corresponds to the user session. */
    private String dataSetId;

    
    /**
     * Constructor (default)
     */
    public LtiUserSessionDetails() {
        
    }
    
    /**
     * Constructor 
     * 
     * @param ltiUserId The lti user id that corresponds to the user session.  
     * @param dataSetId The data set id (optional) if the session is to be tracked for data collection.
     * @param globalUserId The global user id that corresponds to the user session. 
     */
    public LtiUserSessionDetails(LtiUserId ltiUserId, String dataSetId, Integer globalUserId) {
        super(globalUserId);
        
        if (ltiUserId == null) {
            throw new IllegalArgumentException("The lti user id cannot be null.");
        }
        
        this.ltiUserId = ltiUserId;
        this.dataSetId = dataSetId;
    }
    
    
    /**
     * @return the ltiuserId
     */
    public LtiUserId getLtiUserId() {
        return ltiUserId;
    }

    /**
     * @param ltiuserId the ltiuserId to set
     */
    public void setLtiUserId(LtiUserId ltiUserId) {
        this.ltiUserId = ltiUserId;
    }
    
    /**
     * @return the dataSetId
     */
    public String getDataSetId() {
        return dataSetId;
    }

    /**
     * @param dataSetId the dataSetId to set
     */
    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[LtiUserSessionDetails: ");
        sb.append(", ltiUserId = ").append(getLtiUserId());
        sb.append(", dataSetId = ").append(getDataSetId());
        sb.append(", globalUserId = ").append(getGlobalUserId());
        sb.append("]");
        
        return sb.toString();
    }

    
    

}
