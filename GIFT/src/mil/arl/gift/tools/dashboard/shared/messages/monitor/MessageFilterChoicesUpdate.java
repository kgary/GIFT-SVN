/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

import java.util.List;

import mil.arl.gift.common.enums.MessageTypeEnum;

/**
 * An update that lets the client know that the list of message types that can be filtered
 * has changed
 * 
 * @author nroberts
 */
public class MessageFilterChoicesUpdate extends AbstractMessageUpdate {

    private static final long serialVersionUID = 1L;
    
    /** All of the message type choices that can be picked from in the filter */
    private List<MessageTypeEnum> allChoices;
    
    /** The message type choices that the user has selected to filter */
    private List<MessageTypeEnum> selectedChoices;
    
    /**
     * No arg constructor required by GWT serialization
     */
    @SuppressWarnings("unused")
    private MessageFilterChoicesUpdate() {
        super();
    }

    /**
     * Creates a new update that notifies the client when the filter choices have changed
     * 
     * @param domainSessionId the ID of the domain session this update is for. Cannot be null.
     * @param allChoices all of the message type choices that can be picked from in the filter. Cannot be null.
     * @param selectedChoices the message type choices that the user has selected to filter. Cannot be null.
     */
    public MessageFilterChoicesUpdate(Integer domainSessionId, List<MessageTypeEnum> allChoices, List<MessageTypeEnum> selectedChoices) {
        super(domainSessionId);
        
        if(allChoices == null) {
            throw new IllegalArgumentException("The list of total allowed choices cannot be null");
        }
        
        if(selectedChoices == null) {
            throw new IllegalArgumentException("The list of total selected choices cannot be null");
        }
        
        this.allChoices = allChoices;
        this.selectedChoices = selectedChoices;
    }

    /**
     * Gets all of the message type choices that can be picked from in the filter
     * 
     * @return the available choices. Cannot be null.
     */
    public List<MessageTypeEnum> getAllChoices() {
        return allChoices;
    }

    /**
     * Gets the message type choices that the user has selected to filter
     * 
     * @return the selected choices. Cannot be null.
     */
    public List<MessageTypeEnum> getSelectedChoices() {
        return selectedChoices;
    }
    
    
}
