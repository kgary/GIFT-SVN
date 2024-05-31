/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared.websocket.messages;


import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The AbstractWebSocketMessage is the base class which all web socket messages are
 * derived from.  The classes must be compatible with Gwt serialization and must implement
 * the IsSerializable interface.
 * 
 * @author nblomberg
 *
 */
public class AbstractWebSocketMessage implements IsSerializable {


    /** Indicates an invalid message id. */
    public static final int INVALID_MESSAGE_ID = 0;
    
    /** The id of the message.  A message id is guaranteed to be unique for each websocket connection plus within the source
     * of the message (CLIENT or SERVER).  This means that the client ids may collide with the server ids, so the user must be aware
     * if storing the ids in a map not to store the client and server messages into a same list based on id.  
     * 
     * Also the current websocket implementation works for a 1:1 relationship between the browser client and server.  If there is a desire
     * to have multiple clients communicating with each other (ala chat/messaging across multiple clients), then this messaging scheme may 
     * need to be extended to include browser sessions.
     */
    private int messageId = INVALID_MESSAGE_ID;
    
   /**
    * The source of the message (CLIENT or SERVER).
    * 
    * @author nblomberg
    *
    */
    public enum MessageSource {
        CLIENT,
        SERVER,
        INVALID,
    }
    
    /** Indicates where the message is originating from.  Either the client or server. */
    private MessageSource source = MessageSource.INVALID;
    
    /**
     * Constructor - default
     */
    public AbstractWebSocketMessage() {
        
        // Set the source of the message.  
        if (GWT.isClient()) {
            source = MessageSource.CLIENT;
        } else {
            source = MessageSource.SERVER;
        }
    }
    
    /**
     * Sets the message id. 
     * @param messageId The id of the message.
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    
    /** 
     * Accessor to get the message id. 
     * @return The id of the message.
     */
    public int getMessageId() {
        return this.messageId;
    }
    
    /**
     * Accessor to get the source of the message.
     * @return The source of the message.
     */
    public MessageSource getSource() {
        return source;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[AbstractWebSocketMessage: ");
        sb.append("messageId = ").append(messageId);
        sb.append(", messageSource = ").append(source);
        sb.append("]");
        
        return sb.toString();
    }
}
