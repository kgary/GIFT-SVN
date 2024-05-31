/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;

/**
 * The minimum subset of data from a message that is needed to uniquely
 * identify it and display it in the web monitor. This is sent to the client
 * instead of the message itself both to save on memory and to allow messages
 * that can't be converted to JavaScript to be displayed.
 * 
 * @author nroberts
 */
public class MessageEntryMetadata implements IsSerializable {

    /** The message's message type */
    private MessageTypeEnum type;
    
    /** The timestamp at which the message was sent */
    private long timestamp;
    
    /** The message's unique sequence number in its sender */
    private int sequenceNumber;
    
    /** The message's unique sequence number that it is replying to */
    private int replyToSequenceNumber;
    
    /** The address of the endpoint that sent the message */
    private String sender;
    
    /** the enumerated type of module sending the message */
    private ModuleTypeEnum senderModuleType;
    
    /** the module's sender address sending the message */
    private String senderAddress;
    
    /** the name of the destination queue address the message is being sent to */
    private String destinationAddress;

    /** 
     * whether or not this message is expecting an ACK upon successful handling 
     * (beyond just decoding the message contents) at the destination 
     */
    private boolean needsACK;
    
    /**
     * the message's sourceEventId. This id is unique to the sender and shared between multiple messages
     * sent from the same sender and is used to distinguish duplicate messages in the ERT.
     */
    private int sourceEventId;
    
    /**
     * Default no-arg constructor required for RPC serialization
     */
    private MessageEntryMetadata() {}

    /**
     * Creates a new set of message metadata from the given data 
     * 
     * @param type the message's message type. Cannot be null.
     * @param timestamp the timestamp at which the message was sent. Cannot be null.
     * @param sequenceNumber the message's unique sequence number in its sender. Cannot be null.
     * @param sender the address of the endpoint that sent the message. Cannot be null.
     */
    public MessageEntryMetadata(MessageTypeEnum type, long timestamp, int sequenceNumber, String sender) {
        this();
        this.type = type;
        this.timestamp = timestamp;
        this.sequenceNumber = sequenceNumber;
        this.sender = sender;
    }
    
    /**
     * Sets the message's reply to sequence number
     * 
     * @param replyToSequenceNumber the reply to sequence number
     */
    public void setReplyToSequenceNumber(int replyToSequenceNumber) {
        this.replyToSequenceNumber = replyToSequenceNumber;
    }
    
    /**
     * Sets the sender module type 
     * 
     * @param senderModuleType the module type 
     */
    public void setSenderModuleType(ModuleTypeEnum senderModuleType) {
        this.senderModuleType = senderModuleType;
    }
    
    /**
     * Sets the message's sender address
     * 
     * @param senderAddress the sender address
     */
    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }
    
    /**
     * Sets the message's destination address
     * 
     * @param destinationAddress the destination address
     */
    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
    
    /**
     * Sets the value indicating if the message needs a handling response
     * 
     * @param needsACK the value to set
     */
    public void setNeedsACK(boolean needsACK) {
        this.needsACK = needsACK;
    }
    
    /**
     * Sets the message's source event id
     * 
     * @param sourceEventId the source event id
     */
    public void setSourceEventId(int sourceEventId) {
        this.sourceEventId = sourceEventId;
    }
    

    /**
     * Gets the message's message type
     * 
     * @return the message type. Cannot be null.
     */
    public MessageTypeEnum getType() {
        return type;
    }

    /**
     * Gets the timestamp at which the message was sent
     * 
     * @return the timestamp. Cannot be null.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the message's unique sequence number in its sender
     * 
     * @return the sequence number. Cannot be null.
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Gets the address of the endpoint that sent the message 
     * 
     * @return the sender address. Cannot be null.
     */
    public String getSender() {
        return sender;
    }
    
    /**
     * Gets the message's sender module type
     * 
     * @return the sender module type. Cannot be null.
     */
    public ModuleTypeEnum getSenderModuleType() {
        return senderModuleType;
    }
    
    /**
     * Gets the address of the message 
     * 
     * @return the sender address. Cannot be null.
     */
    public String getSenderAddress() {
        return senderAddress;
    }
    
    /**
     * Gets the destination address of the message
     *  
     * @return the destination address. Cannot be null.
     */
    public String getDestinationAddress() {
        return destinationAddress;
    }
    
    /**
     * Gets the value indicating whether the message needs a handling response
     * 
     * @return the needs ACK flag. Cannot be null.
     */
    public boolean isNeedsACK() {
        return needsACK;
    }
    
    /**
     * Gets the message's source event id
     * 
     * @return the source event id. Cannot be null.
     */
    public int getSourceEventId() {
        return sourceEventId;
    }
    
    /**
     * Gets the message's reply to sequence number
     * 
     * @return the message's reply to sequence number. Cannot be null.
     */
    public int getReplyToSequenceNumber() {
        return replyToSequenceNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        result = prime * result + sequenceNumber;
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageEntryMetadata other = (MessageEntryMetadata) obj;
        if (sender == null) {
            if (other.sender != null)
                return false;
        } else if (!sender.equals(other.sender))
            return false;
        if (sequenceNumber != other.sequenceNumber)
            return false;
        if (timestamp != other.timestamp)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
    
    
    
}
