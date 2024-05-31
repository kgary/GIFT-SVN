/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

import java.util.Set;

/**
 * An update that lets the client know that the domain sessions being watched has changed
 * 
 * @author nroberts
 */
public class MessageWatchedDomainSessionUpdate extends AbstractMessageUpdate {

    private static final long serialVersionUID = 1L;
    
    /** The new watchedDomainSessions to display */
    private Set<Integer> watchedDomainSessions;
    
    @SuppressWarnings("unused")
    private MessageWatchedDomainSessionUpdate() {
        super();
    }
    
    /**
     * Creates a new update that tells a client that its watched domain sessions have changed
     * 
     * @param watchedDomainSessions the domain sessions being watched. Cannot be null but can be empty.
     */
    public MessageWatchedDomainSessionUpdate(Set<Integer> messages) {
        super(null);
        
        if(messages == null ) {
            throw new IllegalArgumentException("The watched domain sessions cannot be null");
        }
        
        this.watchedDomainSessions = messages;
    }

    /**
     * Gets the IDs of the domain sessions being watched
     * 
     * @return the domain sessions being watched. Cannot be null but can be empty.
     */
    public Set<Integer> getWatchedDomainSessions() {
        return watchedDomainSessions;
    }
}
