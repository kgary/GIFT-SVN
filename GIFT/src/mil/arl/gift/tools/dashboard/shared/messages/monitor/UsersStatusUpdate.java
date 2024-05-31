/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

import java.util.Map;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;

/**
 * An update indicating that the status of one or more user sessions has changed
 * 
 * @author nroberts
 */
public class UsersStatusUpdate implements WebMonitorUpdate {

    private static final long serialVersionUID = 1L;
    
    /** A mapping from each user ID to its associated session */
    private Map<Integer, UserSession> userSessions;
    
    /** 
     * A mapping from each active domain session ID to its associated session. 
     * 
     * Note: Unlike {@link #userSessions}, which can also contain domain sessions,
     * this mapping is a bit more nuanced in the sense that it only lists domain sessions
     * <i>that have active Domain queues handling messages for them</i>.
     */
    private Map<Integer, DomainSession> activeDomainSessions;

    /**
     * A default constructor required for RPC serialization
     */
    private UsersStatusUpdate() {
        
    }

    /**
     * Creates a new users status update
     * 
     * @param userSessions a mapping from each user ID to its associated session. Cannot be null.
     */
    public UsersStatusUpdate(Map<Integer, UserSession> userSessions, Map<Integer, DomainSession> activeDomainSessions) {
        this();
        
        if(userSessions == null) {
            throw new IllegalArgumentException("A users status update must contain a map of user sessions");
        }
        
        if(activeDomainSessions == null) {
            throw new IllegalArgumentException("A users status update must contain a map of active domain sessions");
        }
        
        this.userSessions = userSessions;
        this.activeDomainSessions = activeDomainSessions;
    }

    /**
     * Gets the status of each of the user sessions
     * 
     * @return the user session status. Cannot be null.
     */
    public Map<Integer, UserSession> getUserSessions() {
        return userSessions;
    }
    
    /**
     * Gets the status of each of the active domain sessions
     * 
     * Note: Unlike {@link #getUserSessions()}, which can also contain domain sessions,
     * the mapping returned by this method is a bit more nuanced in the sense that it only lists 
     * domain sessions <i>that have active Domain queues handling messages for them</i>.
     * 
     * @return the active domain session status. Cannot be null.
     */
    public Map<Integer, DomainSession> getActiveDomainSessions() {
        return activeDomainSessions;
    }
}
