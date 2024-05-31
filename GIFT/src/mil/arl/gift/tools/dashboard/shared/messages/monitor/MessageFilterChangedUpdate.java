/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

import java.util.List;

/**
 * An update that lets the client know that the list of displayed messages has changed
 * due to updating the filter
 * 
 * @author nroberts
 */
public class MessageFilterChangedUpdate extends AbstractMessageUpdate {

    private static final long serialVersionUID = 1L;
    
    /** The new messages to display */
    private List<MessageEntryMetadata> messages;
    
    @SuppressWarnings("unused")
    private MessageFilterChangedUpdate() {
        super();
    }
    
    /**
     * Creates a new update that tells the message panel watching the domain session with the given ID
     * that the given messages should be displayed based on the changed filter.
     * 
     * @param domainSessionId the ID of the domain session whose panel this update is intended for.
     * @param messages the new messages to display after changing the filter. Cannot be null but can be empty.
     */
    public MessageFilterChangedUpdate(Integer domainSessionId, List<MessageEntryMetadata> messages) {
        super(domainSessionId);
        
        if(messages == null ) {
            throw new IllegalArgumentException("The messages to display cannot be null");
        }
        
        this.messages = messages;
    }

    /**
     * Gets the messages to display after changing the filter
     * 
     * @return the new messages to display after changing the filter. Cannot be null but can be empty.
     */
    public List<MessageEntryMetadata> getMessages() {
        return messages;
    }
}
