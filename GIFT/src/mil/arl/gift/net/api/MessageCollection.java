/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.PayloadDecodeException;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;

/**
 * Sends out a single message to multiple destinations and then handles whether
 * all messages were responded to successfully, responded to with a NACK, some
 * were responded to with something else to with some or if some were not
 * responded to at all.
 *
 * @author jleonard
 * 
 */
public class MessageCollection implements MessageClientConnectionListener {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MessageCollection.class);

    /**
     * The default timeout for ACK
     */
    //NOTE (MH 1/28/13): After adding an after-ACK queue in MessageClient I was able to run an 
    //           Explicit Feedback course with this value at 100.
    public static final int ACK_TIMEOUT = CommonProperties.getInstance().getMessageAckTimeoutMs();

    /**
     * A simple data structure for storing where a message was sent and if it
     * had been replied to yet.
     */
    private class Pair {

        public Pair(String dest) {
            messageDestination = dest;
            repliedTo = false;
            ackReceived = false;
        }
        public String messageDestination;

        public volatile boolean repliedTo;

        public volatile boolean ackReceived;
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder("[Pair: ");
            sb.append("repliedTo = ").append(repliedTo);
            sb.append(", ackReceived = ").append(ackReceived);
            sb.append(", messageDestination = ").append(messageDestination);
            sb.append("]");
            return sb.toString();
        }

    }
    /**
     * If the message collection is still active
     */
    private volatile boolean isActive = true;

    /**
     * Maps sequence numbers of a message to it's destination and if it has been
     * responded to yet
     */
    private final Map<Integer, Pair> messageStatus = new HashMap<>();

    /**
     * The callback for when something happens with the message collection ie.
     * all sequence numbers responded to, an NACK or non ACK message received,
     * the collection times out.  Can be null.
     */
    private final MessageCollectionCallback callback;

    /**
     * The ACK timeout timer Note: this creates a new thread
     */
    private ReschedulableTimer ackTimer;

    /**
     * The original message being sent
     */
    private final Message message;
    
    /**
     * A unique Id for each message collection
     */
    private int sourceEventId;

    /**
     * The set of recipients for the message being sent
     */
    private Set<MessageClient> clients;
    
    /** 
     * A locking mechanism used to ensure that conflicting callback 
     * operations are not called simultaneously, such as if a connection
     * is closed while the callback is handling a received message.
     */
    private Object lock = new Object();
    
    /**
     * (optional) used to notify when this message collection is finished, either
     * by success or failure.
     */
    private MessageCollectionFinishedCallback finishedCallback;

    /**
     * Constructor - set and check attributes
     *
     * @param sourceEventId The unique id for the message collection
     * @param message The message to send
     * @param clients The set of destinations to send the message
     * @param callback The callback for events on the message collection. Can be null.
     * @param finishedCallback (optional) used to notify when this message collection is finished, either
     * by success or failure.
     */
    public MessageCollection(int sourceEventId, Message message, Set<MessageClient> clients, MessageCollectionCallback callback, MessageCollectionFinishedCallback finishedCallback) {
        this.sourceEventId = sourceEventId;
        this.callback = callback;
        this.finishedCallback = finishedCallback;

        if (clients == null || clients.isEmpty()) {
            throw new IllegalArgumentException("The set of message clients can't be emtpy");
        } else {

            this.clients = clients;
        }

        if (message == null) {
            throw new IllegalArgumentException("the message can't be null");
        } else {
            this.message = message;
        }
    }
    
    @Override
    public int hashCode(){
        return sourceEventId;
    }
    
    @Override
    public boolean equals(Object otherMessageCollection){
        return otherMessageCollection != null && otherMessageCollection instanceof MessageCollection &&
                this.sourceEventId == ((MessageCollection)otherMessageCollection).sourceEventId;
    }

    /**
     * Returns if the message collection has been completed
     *
     * @return The message collection's status
     */
    public boolean isFinished() {
        return !isActive;
    }
    
    /**
     * Send the message to the provided destinations
     *
     * @param encodingType - the encoding schema to use to encode the message(s)
     * to send.
     * @return boolean - whether or not the message(s) were sent successfully.
     */
    public boolean send(MessageEncodingTypeEnum encodingType) {

        boolean handled = true;
        
        //
        // Determine whether or not the message content should be logged.  This is meant to help
        // prevent to much logging for certain routine messages.
        //
        if(logger.isDebugEnabled()){
            //at this point we know a detailed level of logging has been set, but is it debug or trace level?
            
            if(message.getMessageType() == MessageTypeEnum.MODULE_STATUS || message.getMessageType() == MessageTypeEnum.GATEWAY_MODULE_STATUS){
                //filter out Module status messages if log4j trace level is not enabled
                
                if(logger.isTraceEnabled()){
                    logger.trace("Sending original message of "+message+" to "+clients.size()+" destinations from collection " + this);
                }
            }else{
                logger.debug("Sending original message of "+message+" to "+clients.size()+" destinations from collection " + this);
            }
        }
        
        // Synchronize to make sure that conflicting operations are not called simultaneously
        // Here we want to make sure all messages are sent and the ACK timer started before handling
        // a possibly quick ACK response which will cancel the ACK timer
        // Note: don't acquire lock unless the message collection is still active
        if(isActive){
            synchronized(lock){
    
                for (MessageClient client : clients) {
        
                    final Message clientMessage = message.copyNewDestination(client.getSubjectName());
        
                    final int clientMsgSeqNum = clientMessage.getSequenceNumber();
                    messageStatus.put(clientMsgSeqNum, new Pair(client.getSubjectName()));
        
                    if (callback != null) {
                        //TODO: need to remove the listener as well
                        client.addConnectionStatusListener(this);
                    }

                    handled &= sendMessage(client, clientMessage, encodingType);
                }//end for
        
                if (clients.isEmpty()) {
                    logger.error("There are no clients to send the message to: " + message);
                } else {
        
                    if (callback != null && isActive) {
        
                        //Start the ACK Timer now that all the messages have been sent
                        if (ackTimer == null) {
        
                            ackTimer = new ReschedulableTimer("MessageCollectionACKReceived-Timer", ACK_TIMEOUT, message);
                        }
        
                        ackTimer.schedule();
                    }
                }
            }//end lock
        }

        return handled;
    }

    /**
     * Send the {@link Message}
     * 
     * @param client the client to send the message
     * @param clientMessage the message to send
     * @param encodingType the type of encoding that the message is using. This
     *        is not used for protobuf messages.
     * @return true if the message was sent; false otherwise.
     */
    private boolean sendMessage(MessageClient client, Message clientMessage,
            MessageEncodingTypeEnum encodingType) {
        //
        // Determine whether or not the message content should be logged.
        // This is meant to help
        // prevent to much logging for certain routine messages.
        //
        if (logger.isDebugEnabled()) {
            // at this point we know a detailed level of logging has been
            // set, but is it debug or trace level?

            if (message.getMessageType() == MessageTypeEnum.MODULE_STATUS
                    || message.getMessageType() == MessageTypeEnum.GATEWAY_MODULE_STATUS) {
                // filter out Module status messages if log4j trace level is
                // not enabled

                if (logger.isTraceEnabled()) {
                    logger.trace("Sending " + clientMessage + " to " + client);
                }
            } else {
                logger.debug("Sending " + clientMessage + " to " + client);
            }
        }

        if (MessageEncodingTypeEnum.JSON.equals(encodingType)) {
            throw new UnsupportedOperationException("JSON message types are no longer supported.");
        } else if (MessageEncodingTypeEnum.BINARY.equals(encodingType)) {
            return client.sendMessage(new ProtobufMessageProtoCodec().map(clientMessage));
        } else {
            logger.error("Unknown encoding type '" + encodingType + "' found. Unable to send the message.");
        }

        return false;
    }

    /**
     * Stops the received timer. Note: this should only be done after a ACK has
     * been received from each recipient.
     */
    private void receivedACK() {

        if (ackTimer != null) {
            ackTimer.cancel();
            ackTimer = null;

            if (logger.isDebugEnabled()) {
                logger.info("All messages have been received at their destinations for " + this);
            }
        }else{
            
            if(logger.isDebugEnabled()){
                logger.debug("The ACK timer is null for\n"+this);
            }
        }
    }

    /**
     * Deactivates the collection
     */
    private void finish() {

        isActive = false;
        
        //added this null check after gift user posted a null ptr exception in GIFT v4.0 - https://gifttutoring.org/boards/5/topics/346?r=349
        //This is the first time the error was ever seen and I can only assume based on the code that an ACK msg
        //was received before the ACK timer was initialized.  In this scenario, the ACK timer might be scheduled after this line but
        //the only affect will be a warning msg in the log file.
        if(ackTimer != null){
            ackTimer.cancel();
            ackTimer = null;
        }
        
        // remove the connection status listener from all the clients when this is finished.
        if (clients != null) {
            for (MessageClient client : clients) {
                client.removeConnectionStatusListener(this);
            }
        }
        
        if(finishedCallback != null){
            try{
                finishedCallback.messageCollectionFinished(sourceEventId);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving message collection finished callback implementation.", e);
            }
        }
    }

    /**
     * Handles a message to see if it is a reply to a message of the collection
     *
     * @param msg The message received
     * @return Boolean If the message collection handled this message
     */
    public boolean receive(Message msg) {
        
        // Synchronize to make sure that conflicting operations are not called simultaneously
        // Note: don't acquire lock unless the message collection is still active
        if(isActive){
        	synchronized(lock){
        		
        	    //once the lock is acquired, make sure still the message collection is still active
    	        if (isActive && msg.isReplyMessage()) {
    	
    	            // Check to see if the reply-to sequence number is one of the 
    	            // collection's sequence numbers
    	            Pair pair = messageStatus.get(msg.getReplyToSequenceNumber());
    	            if (pair != null) {
    	
    	                // If the message is a NACK, deactivate the collection and
    	                // notify the callback of the failure
    	                if (msg.getMessageType() == MessageTypeEnum.NACK || msg.getMessageType() == MessageTypeEnum.PROCESSED_NACK) {
    	                    finish();
    	                    handleNACK(msg);
    	                    return true;
    	
    	                } else if (msg.getMessageType() == MessageTypeEnum.ACK) {
    	                    // The message was successfully received and decoded
    	
    	                    pair.ackReceived = true;
    	
    	                    // Check all the message mappings to see if everyone has
    	                    // been replied to
    	                    for (Pair i : messageStatus.values()) {
    	                        if (!i.ackReceived) {
    	                            
    	                            if(logger.isDebugEnabled()){
    	                                logger.debug("This is the message collection for the following reply but still waiting for a reply to "+i+"\n"+msg+"\n"+this);
    	                            }
    	                            return true;
    	                        }
    	                    }
    	
    	                    // If so, the collection has been fully received
    	                    receivedACK();
    	                    return true;
    	
    	                } else {
    	                    
    	                    // Everything else, the sequence number is considered
    	                    // replied to
    	                    pair.repliedTo = true;
    	
    	                    // If the message is not an ACK, let the callback handle it
    	                    if (msg.getMessageType() != MessageTypeEnum.PROCESSED_ACK && callback != null) {
    	                        try{
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Message collection " + this + " received message " + msg);
                                    }

    	                            callback.received(msg);
    	                        }catch(Exception e){
    	                            logger.error("Caught exception from mis-behaving message callback "+callback, e);
    	                        }
    	                    }
    	
    	                    // Check all the message mappings to see if everyone has
    	                    // been replied to
    	                    for (Pair i : messageStatus.values()) {
    	                        if (!i.repliedTo) {
    	                            logger.debug("Still waiting for a reply from "+i.messageDestination);
    	                            return true;
    	                        }
    	                    }
    	
    	                    // If so, finish the collection and notify the callback of
    	                    // success
    	                    finish();
    	                    if (callback != null) {
    	
    	                        //(#1302) - threading this call here to allow the callback success method implementations
    	                        //to take as long as they want handling this event.  The longest logic currently is
    	                        //in dealing with the domain options request after receiving the LMS data reply.
                                Thread successThread = new Thread(new Runnable() {
                                    
                                    @Override
                                    public void run() {
                                        
                                        try{
                                            if(logger.isDebugEnabled()) {
                                                logger.debug("Message collection "+ MessageCollection.this + " succeeded with the response " + msg);
                                            }
                                            callback.success(); 
                                        }catch(Exception e){
                                            logger.error("Caught exception from mis-behaving message callback "+callback, e);
                                            String exceptionMsg = e.getMessage() != null ? e.getMessage() : e.getLocalizedMessage();
                                            if(exceptionMsg == null){
                                                callback.failure("Caught exception from mis-behaving message callback "+callback+". \nSee the appropriate log running with this Message Collection instance for more information.");
                                            }else{
                                                callback.failure("Caught exception from mis-behaving message callback "+callback+". \nSee the appropriate log running with this Message Collection instance for more information.\nERROR = "+exceptionMsg);
                                            }
                                        }
                                    }
                                }, "Msg "+message.getSequenceNumber()+" success callback");
                                
                                successThread.start();                              
                                
                            }
                            return true;
                        }
                    }else{
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("The following message was not intended for this message collection:\n"+msg+"\n"+this);
                        }
                    }
                }
            }
        }

        // It wasn't meant for this collection, so we didn't handle it
        return false;
    }
    
    /**
     * Handles a failure to see if it is a reply to a message of the collection
     *
     * @param payloadException - exception to handle
     * @return boolean If the failure was handled
     */
    public boolean receiveFailure(PayloadDecodeException payloadException) {
        
        // Synchronize to make sure that conflicting operations are not called simultaneously
        // Note: don't acquire lock unless the message collection is still active
        if(isActive){
            synchronized(lock){
                
                //once the lock is acquired, make sure still the message collection is still active
                if (isActive) {
        
                    Pair pair = messageStatus.get(payloadException.getIncompleteMessage().getReplyToSequenceNumber());
        
                    if (pair != null) {
        
                        StringBuilder builder = new StringBuilder();
                        builder.append("There was a failure for the message collection, got a PayloadDecodeException for sequence number ID ").append(payloadException.getIncompleteMessage().getReplyToSequenceNumber());
        
                        if (callback != null) {
        
                            // thread the callback in order to release the 'lock'
                            Thread failureCallbackThread = new Thread(new Runnable() {
                                
                                @Override
                                public void run() {
                                    try {       
                                        callback.failure(builder.toString());                
                                    } catch (Exception e) {     
                                        logger.error("Caught exception from mis-behaving message callback on receive failure: " + callback+"\n"+payloadException, e);
                                    }                          
                                }
                            }, "Msg Collection connection lost");
                            
                            failureCallbackThread.start();  
        
                        } else {        
                            logger.error(builder.toString(), payloadException);
                        }                       
                        
                        finish();
        
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Handle receiving a NACK from a recipient of the message sent by this
     * collection.
     *
     * @param msg - the NACK message
     */
    private void handleNACK(Message msg) {
        if (callback == null) {
            NACK nack = (NACK) msg.getPayload();
            StringBuilder builder = new StringBuilder();
            builder.append("There was a failure for a message collection, got a NACK: ").append(nack.getErrorMessage()).append(" for sequence number ID ").append(msg.getReplyToSequenceNumber());
            logger.error(builder.toString());

        } else {
            try{
                callback.failure(msg);
            }catch(Exception e){
                logger.error("Caught exception from mis-behaving message callback "+callback, e);
            }
        }
    }

    /**
     * Handle receiving an timeout (either ACK or customer reply timing) because
     * a recipient of the message sent by this collection didn't respond.
     *
     * @param msg - the timeout message
     */
    private void handleTimeout(String msg) {
        if (callback == null) {

            StringBuilder builder = new StringBuilder();
            builder.append("There was a failure for a message collection: ").append(msg).append(". Message collection had sequence numbers: ");

            for (Integer i : messageStatus.keySet()) {

                Pair pair = messageStatus.get(i);

                if (pair != null) {

                    builder.append(i).append(" to ").append(pair.messageDestination).append(", ");
                }
            }

            logger.error(builder.toString());

        } else {
            
            try{
                callback.failure(msg);
            }catch(Exception e){
                logger.error("Caught exception from mis-behaving message callback "+callback, e);
            }
            
        }

        finish();
    }

    @Override
    public void connectionOpened(MessageClient client) {
        // Do nothing
    }

    @Override
    public void onConnectionLost(MessageClient client) {

        //TODO: Determine if the response eventually coming back should be handled
        if (clients.contains(client)) {
            
            // Synchronize to make sure that conflicting operations are not called simultaneously
            // Note: don't acquire lock unless the message collection is still active
            if(isActive){
                synchronized(lock){
                    
                    //once the lock is acquired, make sure still the message collection is still active
                    if (isActive) {                 
                        if(callback != null) {
                            
                            // thread the callback in order to release the 'lock'
                            Thread failureCallbackThread = new Thread(new Runnable() {
                                
                                @Override
                                public void run() {
                                    try{                        
                                        callback.failure("The connection has timed out for " + client);
                                    }catch(Exception e){
                                        logger.error("Caught exception from mis-behaving message callback when notifying it of connection lost: "+callback, e);
                                    }                            
                                }
                            }, "Msg Collection connection lost");
                            
                            failureCallbackThread.start();                          
                        }
                    }
                }
            }
            
            finish();
        }
    }

    @Override
    public void connectionClosed(final MessageClient client) {

        if (clients.contains(client)) {
            
            // Synchronize to make sure that conflicting operations are not called simultaneously
            // Note: don't acquire lock unless the message collection is still active
            if(isActive){
                synchronized(lock){
                    
                    //once the lock is acquired, make sure still the message collection is still active
                    if (isActive) {
                        
                        // thread the callback in order to release the 'lock'
                        Thread failureCallbackThread = new Thread(new Runnable() {
                            
                            @Override
                            public void run() {
                                try{                        
                                    callback.failure("The connection has timed out for " + client);
                                }catch(Exception e){
                                    logger.error("Caught exception from mis-behaving message callback when notifying it of connection closed: "+callback, e);
                                }                            
                            }
                        }, "Msg Collection connection closed");
                        
                        failureCallbackThread.start();
                    }
                }
            }
            
            finish();
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[MessageCollection: ");
        sb.append("active = ").append(isActive);
        sb.append(", sourceEventId = ").append(sourceEventId);
        sb.append(", ackTimer = ").append(ackTimer != null);
        sb.append(" message = ").append(message);
        sb.append("]");

        return sb.toString();
    }

    /**
     * This class is responsible for scheduling or rescheduling a timer task
     * without having to create a new thread.
     *
     * @author mhoffman
     *
     */
    private class ReschedulableTimer extends Timer {

        private TimeoutTimerTask timerTask = null;

        /** amount of time (milliseconds) to wait for a response */
        private final int delay;
        
        /** the sent message whose response is being timed */
        private Message sentMessage;

        /**
         * Class constructor - set the thread name
         *
         * @param name - thread name to use for this timer
         * @param delay - amount of time (milliseconds) to wait for a response
         * @param message - the sent message whose response is being timed
         */
        public ReschedulableTimer(String name, int delay, Message message) {
            super(name);
            this.delay = delay;
            this.sentMessage = message;
        }

        /**
         * Schedule a timer task with the given delay
         */
        public void schedule() {

            if (timerTask == null) {
                timerTask = new TimeoutTimerTask(sentMessage);
                schedule(timerTask, delay);
            } else {
                reschedule();
            }
        }

        /**
         * Reschedules the timer task with the given delay
         *
         * @param delay
         */
        private void reschedule() {
            timerTask.cancel();
            timerTask = new TimeoutTimerTask(message);
            schedule(timerTask, delay);
        }
    }

    /**
     * A TimerTask that calls failure on the callback if all messages are not
     * responded to within the timeout duration.
     */
    private class TimeoutTimerTask extends TimerTask {
        
        /** the sent message whose response is being timed */
        private Message sentMessage;
        
        /**
         * Class constructor
         * 
         * @param message - the sent message whose response is being timed
         */
        public TimeoutTimerTask(Message message){
            this.sentMessage = message;
        }

        @Override
        public void run() {
            
            try{
            
                if (isActive) {
    
                    isActive = false;
    
                    StringBuilder builder = new StringBuilder();
    
                    boolean allRepliedTo = true;
    
                    for (Integer i : messageStatus.keySet()) {
    
                        Pair pair = messageStatus.get(i);
    
                        if (!pair.repliedTo) {
    
                            allRepliedTo = false;
    
                            builder.append(pair.messageDestination).append(" did not respond, ");
                        }
                    }
                    
                    builder.append("to message ").append(this.sentMessage);
    
                    if (!allRepliedTo) {
    
                        String failString = builder.toString();
                        logger.error("Message has timed out because: " + failString);
                        handleTimeout(failString);
    
                    } else {
    
                        if (callback != null) {
    
                            try{
                                callback.success();
                            }catch(Exception e){
                                logger.error("Caught exception from mis-behaving message callback "+callback, e);
                            }
                        }
    
                        logger.warn("TimeoutTimerTask run() was executed even though all messages were replied to.");
                    }
                } else {
                    
                    logger.warn("TimeoutTimerTask run() was executed even though the message collection is no longer active.");
                }
            }catch(Throwable t){
                logger.error("Caught exception while waiting for all responses to a message.", t);
            }
        }
    }
    
    /**
     * Interface used to notify when the message collection has finished, either successfully
     * or through a failure of some kind.
     * 
     * @author mhoffman
     *
     */
    public interface MessageCollectionFinishedCallback{
        
        /**
         * Notification that the message collection has finished.  Finishing could be
         * either through a successful or failure case.
         * 
         * @param sourceEventId the unique id of a source message for a particular module that had
         * a message collection created to manage the life cycle of that original message (which could
         * have been sent to multiple recipients).  This can be used to uniquely identify the message
         * collection among message collections in a module.
         */
        public void messageCollectionFinished(int sourceEventId);
    }
}
