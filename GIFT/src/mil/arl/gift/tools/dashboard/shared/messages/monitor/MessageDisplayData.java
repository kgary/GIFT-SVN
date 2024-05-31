/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.UserSession;

/**
 * Data needed to display a message's details in the client
 * 
 * @author nroberts
 */
public class MessageDisplayData implements IsSerializable{
    
    /** The metadata uniquely identifying the message */
    private MessageEntryMetadata metadata;
    
    /** The destination address of the message */
    private String destination;
    
    /** The JSON details of the message */
    private String detailsJson;
    
    /** The user session that this message is a part of */
    private UserSession userSession;
    
    /** A default no-arg constructor required for GWT serialization */
    private MessageDisplayData() {}

    /**
     * Creates a new set of display data for a message
     * 
     * @param metadata the metadata uniquely identifying the message. Cannot be null.
     * @param destination the destination address of the message. Cannot be null.
     * @param detailsJson the JSON details of the message. Cannot be null.
     * @param userSession the user session that this message is a part of. Can be null for
     * system messages.
     */
    public MessageDisplayData(MessageEntryMetadata metadata, String destination, String detailsJson, UserSession userSession) {
        this();
        this.metadata = metadata;
        this.destination = destination;
        this.detailsJson = detailsJson;
        this.userSession = userSession;
    }

    /** 
     * Gets the metadata uniquely identifying the message
     * 
     * @return the metadata. Cannot be null.
     */
    public MessageEntryMetadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the destination address of the message
     * 
     * @return the destination. Cannot be null.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Gets the JSON details of the message 
     * 
     * @return the JSON details. Cannot be null.
     */
    public String getDetailsJson() {
        return detailsJson;
    }

    /**
     * Gets the user session that this message is a part of
     * 
     * @return the user session. Can be null for system messages.
     */
    public UserSession getUserSession() {
        return userSession;
    }
}
