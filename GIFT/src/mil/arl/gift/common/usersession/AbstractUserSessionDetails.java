/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.usersession;

import java.io.Serializable;

/**
 * The AbstractUserSessionDetails can be used in a UserSession object to 
 * provide additional details of the user session based on the type of session.
 * The abstract class here is the base class that all session detail type classes
 * should extend from.  
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractUserSessionDetails implements Serializable{

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** Each user session details that are made in the future, should provide a global user id
     * that identifies the user in the user session. This id represents a unique id in the
     * globaluser UMS database table which indicates a unique id for a user in the gift instance.
     */
    private Integer globalUserId;
    
    /**
     * Constructor (default)
     */
    public AbstractUserSessionDetails() {
        
    }
    
    public AbstractUserSessionDetails(Integer globalUserId) {
        
        if (globalUserId == null) {
            throw new IllegalArgumentException("The global user id cannot be null.");
        }
        
        this.globalUserId = globalUserId;
    }
    
    /**
     * @return the globalUserId
     */
    public Integer getGlobalUserId() {
        return globalUserId;
    }

    /**
     * @param globalUserId the globalUserId to set
     */
    public void setGlobalUserId(Integer globalUserId) {
        this.globalUserId = globalUserId;
    }
    
    @Override
    public abstract String toString();
}
