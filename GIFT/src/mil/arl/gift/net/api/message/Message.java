/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.TimeUtil;

/**
 * This is the base class for a message.  It contains the minimal header information for GiFT.  
 * It also contains the payload to be encoded/decoded.
 * 
 * @author mhoffman
 *
 */
public class Message {    
    
    /** the default reply to sequence number value */
    public static final int UNKNOWN_REPLY_ID = -1;
    
    /** the value of a sourceEventId if the message was generated before implementation of sourceEventIds */
    public static final int ID_NOT_AVAILABLE = -1;

    /** the message type */
    private MessageTypeEnum messageType;

    /** the date/time this message was created */
    private long timeStamp;

    /** the enumerated type of module sending the message */
    private ModuleTypeEnum senderModuleType;

    /** the module's name sending the message */
    private String senderModuleName;

    /** the module's queue name sending the message */
    private String senderQueueName;

    /** the name of the destination queue the message is being sent too */
    private String destinationQueueName;

    /** 
     * the message's sequence number 
     * Note: sequence numbers are unique to an instance of the sender, not across a GIFT session. 
     *       i.e. all GIFT message senders will use the same seqNum value at some point in a GIFT session.
     */
    private int seqNum;
    
    /**
     * the message's sourceEventId. This id is unique to the sender and shared between multiple messages
     * sent from the same sender and is used to distinguish duplicate messages in the ERT.
     */
    private int sourceEventId;

    /** contains the class information (e.g. Module Status), and is the purpose of this message's existence */
    private Object payload;
    
    /** 
     * whether or not this message is expecting an ACK upon successful handling 
     * (beyond just decoding the message contents) at the destination 
     */
    private boolean needsHandlingResponse;
    
    /** the sequence number of a message this message is a reply too */
    private int replyToSeqNum = UNKNOWN_REPLY_ID;

    /**
     * Class constructor - creates a new message.
     * This constructor is used for creating a new message to send. 
     * 
     * @param messageType - the type of message
     * @param sourceEventId - the message source's event id
     * @param senderModuleName - the module's name sending the message
     * @param senderAddress - the address of the sender
     * @param senderModuleType - type of module sending the message
     * @param destinationAddress - the destination for the message
     * @param payload - the payload of the message (i.e. not the header)
     * @param needsACK - whether or not this message is expecting an ACK upon successful decoding at the destination
     */
    public Message(MessageTypeEnum messageType, int sourceEventId, String senderModuleName, 
            String senderAddress, ModuleTypeEnum senderModuleType, String destinationAddress, Object payload, boolean needsACK){
        
        this(messageType, SequenceNumberGenerator.nextSeqNumber(), sourceEventId, System.currentTimeMillis(), senderModuleName, senderAddress, senderModuleType, destinationAddress, payload, needsACK);
    }
    
    /**
     * Class constructor - used when the time stamp and sequence number have already been determined.
     * 
     * @param messageType - the type of message
     * @param seqNum - the sequence number created by the sender
     * @param sourceEventId - the event id created by the sender
     * @param timeStamp - the time at which the message was originally created
     * @param senderModuleName the module's name sending the message
     * @param senderAddress - the address of the sender
     * @param senderModuleType - type of module sending the message
     * @param destinationAddress - the destination for the message
     * @param payload - the payload of the message (i.e. not the header)
     * @param needsACK - whether or not this message is expecting an ACK upon successful decoding at the destination
     */
    public Message(MessageTypeEnum messageType, int seqNum, int sourceEventId, long timeStamp, String senderModuleName, 
            String senderAddress, ModuleTypeEnum senderModuleType, String destinationAddress, Object payload, boolean needsACK){
        this.messageType = messageType;
        this.seqNum = seqNum;
        this.sourceEventId = sourceEventId;
        this.timeStamp = timeStamp;
        this.senderModuleName = senderModuleName;
        this.senderModuleType = senderModuleType;
        this.senderQueueName = senderAddress;
        this.destinationQueueName = destinationAddress; 
        this.payload = payload;
        this.needsHandlingResponse = needsACK;
    }
    
    /**
     * Return the sequence number of the message this message is replying too
     * 
     * @return int - the sequence number of the message being replied too.  If negative, than this is not a reply to a message.
     */
    public int getReplyToSequenceNumber(){
        return replyToSeqNum;
    }
    
    /**
     * Set the sequence number of the message this message is replying too
     * 
     * @param sequenceNumber - the sequence number of the message being replied too.
     */
    public void setReplyToSequenceNumber(int sequenceNumber){
        replyToSeqNum = sequenceNumber;
    }
    
    /**
     * whether or not this message is expecting an ACK upon successful handling 
     * (beyond just decoding the message contents) at the destination 
     * 
     * @return boolean true if the message sender is waiting for a response that indicates
     * the message was received and handled
     */
    public boolean needsHandlingResponse(){
        return needsHandlingResponse;
    }
    
    /**
     * Return the payload of this message.  The payload contains the class information 
     * (e.g. Module Status), and is the purpose of this message's existence.
     * 
     * @return the contents of the message (not the header)
     */
    public Object getPayload(){
        return payload;
    }


    /**
     * Return the date/time this message was created
     * 
     * @return Date - the date/time this message was created
     */
    public long getTimeStamp(){
        return timeStamp;
    }

    /**
     * Set the timestamp for this message.
     * 
     * @param timestamp the timestamp to set.
     */
    public void setTimeStamp(long timestamp) {
        this.timeStamp = timestamp;
    }

    /**
     * Return the message type
     * 
     * @return MessageTypeEnum - the message type
     */
    public MessageTypeEnum getMessageType(){
        return messageType;
    }

    /**
     * Return the enumerated type of module sending the message
     * 
     * @return ModuleTypeEnum
     */
    public ModuleTypeEnum getSenderModuleType() {
        return senderModuleType;
    }

    /**
     * Return the name of the module sending the message 
     * 
     * @return String
     */
    public String getSenderModuleName() {
        return senderModuleName;
    }

    /**
     * Return the queue name of the module receiving the message
     * 
     * @return String
     */
    public String getDestinationQueueName(){
        return destinationQueueName;
    }

    /**
     * Return the message senders location for which to send reply messages too
     * 
     * @return String
     */
    public String getSenderAddress(){
        return senderQueueName;
    }

    /**
     * Return the message's sequence number
     * Refer to the class attributes comments for more information.
     * 
     * @return int the sequence number for this message
     */
    public int getSequenceNumber(){
        return seqNum;
    }

    /**
     * Return the message's source event Id
     * Refer to the class attributes comments for more information.
     * 
     * @return int the sequence number for this message
     */
    public int getSourceEventId(){
        return sourceEventId;
    }
    
    /**
     * Return whether this is a reply Message or not
     * 
     * @return boolean
     */
    public boolean isReplyMessage(){
        return replyToSeqNum != UNKNOWN_REPLY_ID;
    }
    
    /**
     * Creates a deep copy of this message, however the sequence number is incremented
     * 
     * @return Message - the copied message
     */
    public Message copy(){
        
        Message message = new Message(this.getMessageType(), this.sourceEventId, this.getSenderModuleName(), this.getSenderAddress(), 
                this.getSenderModuleType(), this.getDestinationQueueName(), this.getPayload(), this.needsHandlingResponse());
        
        if(message.isReplyMessage()){
            message.setReplyToSequenceNumber(getReplyToSequenceNumber());
        }
        
        return message;
    }
    
    /**
     * Creates a deep copy of this message, however using the new destination address and a sequence number is incremented
     * 
     * @param newDestinationAddress - address to use in creating a clone of this message. 
     * @return Message - the copied message
     */
    public Message copyNewDestination(String newDestinationAddress){
        
        destinationQueueName = newDestinationAddress;
        
        Message message = new Message(this.getMessageType(), this.sourceEventId, this.getSenderModuleName(), this.getSenderAddress(), 
                this.getSenderModuleType(), this.getDestinationQueueName(), this.getPayload(), this.needsHandlingResponse());
        
        if(isReplyMessage()){
            message.setReplyToSequenceNumber(getReplyToSequenceNumber());
        }
        
        return message;
    }


    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[Message: ");
        sb.append("type = ").append(getMessageType());
        sb.append(", source event id = ").append(getSourceEventId());
        sb.append(", seq # = ").append(getSequenceNumber());
        
        if(isReplyMessage()){
            sb.append(", replyToSeqNum = ").append(getReplyToSequenceNumber());  
        }
        
        sb.append(", time = ").append(TimeUtil.formatTimeSystemLog(getTimeStamp()));
        sb.append(", sender type = ").append(getSenderModuleType());
        sb.append(", sender name = ").append(getSenderModuleName());
        sb.append(", sender address = ").append(getSenderAddress());
        sb.append(", destination address = ").append(getDestinationQueueName());
        sb.append(", payload = ").append(getPayload());
        sb.append(", needs response = ").append(needsHandlingResponse());
        sb.append("]");

        return sb.toString();
    }   
}
