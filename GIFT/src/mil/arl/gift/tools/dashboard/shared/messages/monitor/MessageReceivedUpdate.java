/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

/**
 * An update that lets the client know that a new message has been received and needs to be displayed
 * 
 * @author nroberts
 */
public class MessageReceivedUpdate extends AbstractMessageUpdate{

    private static final long serialVersionUID = 1L;
    
    /** The metadata needed to display the message */
    private MessageEntryMetadata message;
    
    @SuppressWarnings("unused")
    private MessageReceivedUpdate() {
        super();
    }

    /**
     * Creates an update that tells the message panel watching the domain session with the given ID
     * that the given message has been received and needs to be displayed
     * 
     * @param domainSessionId the ID of the domain session this update is a part of. Cannot be null.
     * @param message the metadata needed to display the message. Cannot be null.
     */
    public MessageReceivedUpdate(Integer domainSessionId, MessageEntryMetadata message) {
        super(domainSessionId);
        this.message = message;
    }

    /**
     * Gets message to display
     * 
     * @return the metadata needed to display the message. Will not be null.
     */
    public MessageEntryMetadata getMessage() {
        return message;
    }
}
