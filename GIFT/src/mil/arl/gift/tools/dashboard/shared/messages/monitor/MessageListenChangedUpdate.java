/**
 * Copyright Dignitas Technologies, LLC
 *
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */              
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

/**
 * This class represents an update that informs the client that the listening 
 * status for messages has changed.
 * 
 * @author cpolynice
 */
public class MessageListenChangedUpdate extends AbstractMessageUpdate {
    
    private static final long serialVersionUID = 1L;
    
    /** Flag indicating whether the client is listening for messages */
    private boolean listening = true;
    
    @SuppressWarnings("unused")
    private MessageListenChangedUpdate() {
        super();
    }
    
    /**
     * Constructor - creates a new update for the given domain session that lets the client know that
     * the listening status for messages has changed 
     * 
     * @param domainSessionId the ID of the domain session whose panel this update is intended for.
     * @param listening the new value indicating whether the message panel is listening for messages
     */
    public MessageListenChangedUpdate(Integer domainSessionId, boolean listening) {
        super(domainSessionId);
        this.listening = listening;
    }
    
    /**
     * Gets the value to determine listening status of message panel
     * 
     * @return the listening value
     */
    public boolean isListening() {
        return listening;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("[MessageListenChangedUpdate: ");
        sb.append("listening: ").append(listening);
        sb.append("]");
        return sb.toString();
    }
}
