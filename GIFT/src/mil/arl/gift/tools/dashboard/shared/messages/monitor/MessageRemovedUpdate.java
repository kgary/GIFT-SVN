/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

/**
 * An update that lets the client know that a message has been removed and needs to be hidden
 * 
 * @author nroberts
 */
public class MessageRemovedUpdate extends AbstractMessageUpdate{

    private static final long serialVersionUID = 1L;
    
    /** The metadata needed to hide the message */
    private MessageEntryMetadata message;
    
    @SuppressWarnings("unused")
    private MessageRemovedUpdate() {
        super();
    }

    /**
     * Creates an update that tells the message panel watching the domain session with the given ID
     * that the given message has been removed and needs to be hidden
     * 
     * @param domainSessionId the ID of the domain session the message is a part of. Can be null.
     * @param message the metadata needed to remove the message. Cannot be null.
     */
    public MessageRemovedUpdate(Integer domainSessionId, MessageEntryMetadata message) {
        super(domainSessionId);
        this.message = message;
    }

    /**
     * Gets message to remove
     * 
     * @return the metadata needed to remove the message. Will not be null.
     */
    public MessageEntryMetadata getMessage() {
        return message;
    }
}
