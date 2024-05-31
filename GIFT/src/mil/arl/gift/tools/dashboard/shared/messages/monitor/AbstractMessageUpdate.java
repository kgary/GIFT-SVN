/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

/**
 * A message intended to be handled by a message panel on the client
 * 
 * @author nroberts
 */
public abstract class AbstractMessageUpdate implements WebMonitorUpdate {

    private static final long serialVersionUID = 1L;
    
    /** 
     * The domain session ID of the panel the update is intended for, or null
     * if this update is for a system message 
     */
    private Integer domainSessionId;
    
    /**
     * Default constructor required for RPC serialization
     */
    protected AbstractMessageUpdate() {}

    /**
     * Creates a new update for the message panel watching the given session
     * 
     * @param domainSessionId the domain session ID of the panel the update is intended for, or null
     * if this update is for a system message 
     */
    protected AbstractMessageUpdate(Integer domainSessionId) {
        this();
        this.domainSessionId = domainSessionId;
    }
    
    /**
     * Gets the domain session ID of the session this message is associated with
     * 
     * @return the domain session ID of the panel the update is intended for, or null
     * if this update is for a system message 
     */
    public Integer getDomainSessionId() {
        return domainSessionId;
    }
}
