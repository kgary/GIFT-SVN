/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

import mil.arl.gift.common.usersession.AbstractUserSessionDetails;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.common.util.CompareUtil;

/**
 * This class contains high level information about a user session
 * 
 * @author mhoffman
 *
 */
public class UserSession implements Serializable{

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** the learner's unique user id */
    private int userId;    
    
    /** (optional) the unique experiment id for the session [if the session is for an experiment] */
    private String experimentId;
    
    /** a user name for the user (optional) */
    private String username;    
    
    /** Stores the type of user session. Defaults to normal GIFT_USER type for backwards compatibility reasons. */
    private UserSessionType sessionType = UserSessionType.GIFT_USER;
    
    /** Additional details about the user session which can be provided based on the type of user session. Can be null. */
    private AbstractUserSessionDetails sessionDetails;
    
    /**
     * Required for GWT serialization
     */
    protected UserSession(){}

    /**
     * Class constructor - set attributes
     * 
     * @param userId - the user id for this domain session
     */
    public UserSession(int userId){
        this.userId = userId;
    }

    /**
     * Retrieve the user id for this domain session
     * 
     * @return the user id
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set the username for this user session instance
     * 
     * @param username a username for a user. Can be null because a user session can be anonymous (e.g. Experiment user)
     */
    public void setUsername(String username){
        this.username = username;
    }
    
    /**
     * Return the username for the user
     * 
     * @return can be null because a user session can be anonymous (e.g. Experiment user)
     */
    public String getUsername(){
        return username;
    }      

    /**
     * Set the optional experiment id for this domain session.
     * 
     * @param experimentId the unique id of the experiment being run in the
     *        session.
     */
    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    /**
     * Return the experiment id for this session message
     * 
     * @return the unique experiment id. Will be null if the session is not part
     *         of an experiment.
     */
    public String getExperimentId() {
        return experimentId;
    }

    /**
     * @return the sessionType
     */
    public UserSessionType getSessionType() {
        return sessionType;
    }

    /**
     * @param sessionType the sessionType to set
     */
    public void setSessionType(UserSessionType sessionType) {
        this.sessionType = sessionType;
    }

    /**
     * @return the sessionDetails
     */
    public AbstractUserSessionDetails getSessionDetails() {
        return sessionDetails;
    }

    /**
     * @param sessionDetails the sessionDetails to set
     */
    public void setSessionDetails(AbstractUserSessionDetails sessionDetails) {
        this.sessionDetails = sessionDetails;
    }
    
    /**
     * Helper method to retrieve the global user id from a user session (if it exists).
     * 
     * @return The global user id for the user session (if it exists).  Null if the session does not have a global user id.  
     */
    public Integer getGlobalUserId() {
        
        if (sessionDetails != null && sessionDetails instanceof LtiUserSessionDetails) {
            LtiUserSessionDetails details = (LtiUserSessionDetails)sessionDetails;
            return details.getGlobalUserId();
        }
        
        return null;
    }
    
    /**
     * Helper function to compare the user session type compared to this user session.
     * 
     * @param sessionType The type of user session to compare the user session to.
     * @return True if the types are the same, false otherwise.
     */
    public boolean isSessionType(UserSessionType sessionType) {
        return CompareUtil.equalsNullSafe(this.getSessionType(), sessionType);
    }

    @Override
    public boolean equals(Object that){ 
        
        if(that != null && that instanceof UserSession){
            
            UserSession otherSession = (UserSession)that;
            
            if(CompareUtil.equalsNullSafe(this.getUserId(), otherSession.getUserId()) &&
                    CompareUtil.equalsNullSafe(this.getExperimentId(), otherSession.getExperimentId()) &&
                    CompareUtil.equalsNullSafe(this.getGlobalUserId(), otherSession.getGlobalUserId()) &&
                    CompareUtil.equalsNullSafe(this.getSessionType(), otherSession.getSessionType())) {
                        return true;
            }

        }
        
        return false;
    }
    
    
    
    @Override
    public int hashCode() {

        int hashCode = 0;

        hashCode |= userId << 0;
        hashCode |= experimentId != null ? experimentId.hashCode() << 4 : 0;
        hashCode |= getGlobalUserId() != null ? getGlobalUserId().hashCode() << 6 : 0;

        return hashCode;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[UserSession: ");
        sb.append("userId = ").append(getUserId());
        sb.append(", username = ").append(getUsername());
        sb.append(", experimentId = ").append(getExperimentId());
        sb.append(", sessionType = ").append(getSessionType());
        sb.append(", sessionDetails = ").append(getSessionDetails());
        sb.append("]");
        
        return sb.toString();
    }
}
