/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.websocket.messages;

import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;
import mil.arl.gift.tutor.shared.AbstractAction;

/**
 * The DoActionAsyncMessage is a conversion of the gwt rpc DoAction method from the TutorUserServiceImpl.java file
 * This message converts a gwt-rpc into a websocket message.  The message contains a request for the server to apply
 * an action based on something that was done on the client.  The server should process the action, and then send a response back
 * to indicate if the DoActionAsyncMessage succeeded or failed. This message is sent from the web client to the server via the 
 * tutor websocket.
 * 
 * @author nblomberg
 *
 */
public class DoActionAsyncMessage extends AbstractAsyncWebSocketMessage {

    /* The tutor action that corresponds to the message. */
    private AbstractAction action;
    
    /** The browser session sending the message. */
    private String browserSessionKey;
    
    /** 
     * Constructor - default (Needed for gwt serialization).
     */
    public DoActionAsyncMessage() {
        
    }
    
    /**
     * Constructor 
     * @param browserSessionKey The browser session that is sending the message.
     * @param action The action to be applied on the server.
     */
    public DoActionAsyncMessage(String browserSessionKey, AbstractAction action) {
        setBrowserSessionKey(browserSessionKey);
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

    /**
     * @return the browserSessionKey
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }


    /**
     * @param browserSessionKey the browserSessionKey to set
     */
    public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[DoActionMessage: ");
        sb.append("browserSessionKey = ").append(getBrowserSessionKey());
        sb.append(", action = ").append(action);
        sb.append(", ").append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
