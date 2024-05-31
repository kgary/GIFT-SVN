/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.websocket.messages;

import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.tutor.shared.AbstractAction;


/**
 * The TutorActionMessage is the websocket message used to send tutor
 * actions between the server and client.
 * 
 * @author nblomberg
 *
 */
public class TutorActionMessage extends AbstractWebSocketMessage {

    /* The tutor action that corresponds to the message. */
    private AbstractAction action;
    
    /** 
     * Constructor - default
     */
    public TutorActionMessage() {
        
    }
    

    /**
     * Constructor 
     * 
     * @param action The action that corresponds to the message. 
     */
    public TutorActionMessage(AbstractAction action) {
        setAction(action);
    }

    /**
     * Sets the tutor action for this message. 
     * 
     * @param action The tutor action to be set. 
     */
    public void setAction(AbstractAction action) {
        this.action = action;
    }
    
    /**
     * Gets the tutor action for this message. 
     * 
     * @return The tutor action for the message.  Can be null.
     */
    public AbstractAction getAction() {
        return this.action;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[TutorActionMessage: ");
        sb.append("action = ").append(action);
        sb.append(", ").append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
