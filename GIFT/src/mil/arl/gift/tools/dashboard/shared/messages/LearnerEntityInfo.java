/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

/**
 * Information surrounding a learner that is playing a particular entity in a domain knowledge session
 * 
 * @author nroberts
 */
@SuppressWarnings("serial")
public class LearnerEntityInfo implements Serializable {

    /** The ID of the domain session that identifies this learner within the domain knowledge session */
    private int domainSessionId;
    
    /** The learner's user name */
    private String userName;
    
    /**
     * Default, no-argument contructor required for GWT RPC serialization
     */
    protected LearnerEntityInfo() {}
    
    /**
     * Creates a new set of info for the learner with the given domain session ID at the given location
     * 
     * @param domainSessionId the ID of the domain session that identifies this learner within the domain knowledge session. Cannot be null.
     */
    public LearnerEntityInfo(int domainSessionId) {
        this();
        setDomainSessionId(domainSessionId);
    }
    
    /**
     * Creates a new set of info for the learner with the given domain session ID at the given location
     * 
     * @param domainSessionId the ID of the domain session that identifies this learner within the domain knowledge session. Cannot be null.
     * @param username the user name of the learner. Can be null.
     */
    public LearnerEntityInfo(int domainSessionId, String username) {
        this(domainSessionId);
        setUsername(username);
    }
    
    /**
     * Sets the ID of the domain session that identifies this learner within the domain knowledge session
     * 
     * @param domainSessionId the learner's domain session ID
     */
    protected void setDomainSessionId(int domainSessionId) {
        this.domainSessionId = domainSessionId;
    }
    
    /**
     * Gets the ID of the domain session that identifies this learner within the domain knowledge session
     * 
     * @return the learner's domain session ID
     */
    public int getDomainSessionId() {
        return this.domainSessionId;
    }
    
    /**
     * Gets this learner's user name
     * 
     * @return the learner's user name. Can be null.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets this learner's user name
     * 
     * @param userName the learner's user name. Can be null.
     */
    public void setUsername(String userName) {
        this.userName = userName;
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[LearnerEntityInfo: ")
                .append("domainSessionId = ").append(domainSessionId)
                .append(", userName = ").append(userName)
                .append("]").toString();
    }
}
