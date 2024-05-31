/**
 * Copyright Dignitas Technologies, LLC
 *
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */              
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

/**
 * This class represents an update that informs the client that the header 
 * status for the message panel has changed.
 * 
 * @author cpolynice
 */
public class MessageHeaderStatusUpdate extends AbstractMessageUpdate {
    
    private static final long serialVersionUID = 1L;
    
    /** Flag indicating whether the client should display advanced data for messages */
    private boolean advancedHeader = true;
    
    @SuppressWarnings("unused")
    private MessageHeaderStatusUpdate() {
        super();
    }
    
    /**
     * Constructor - creates a new update for the given domain session that lets the client know that
     * the header status for messages has changed 
     * 
     * @param domainSessionId the ID of the domain session whose panel this update is intended for.
     * @param listening the new value indicating whether the message panel should display advanced data
     */
    public MessageHeaderStatusUpdate(Integer domainSessionId, boolean advancedHeader) {
        super(domainSessionId);
        this.advancedHeader = advancedHeader;
    }
    
    /**
     * Gets the value to determine header status of message panel
     * 
     * @return the header status value
     */
    public boolean isAdvancedHeader() {
        return advancedHeader;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("[MessageListenChangedUpdate: ");
        sb.append("advancedHeader: ").append(advancedHeader);
        sb.append("]");
        return sb.toString();
    }
}
