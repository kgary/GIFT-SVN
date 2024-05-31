/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;

/**
 * The DashboardAsyncMessage is a conversion of the gwt rpc DashboardServiceImpl.java file
 * This message converts a gwt-rpc into a websocket message.  The message contains a request for the server to apply
 * an action based on something that was done on the client.  The server should process the action, and then send a response back
 * to indicate if the DashboardAsyncMessage succeeded or failed. This message is sent from the web client to the server via the 
 * dashboard websocket.
 * 
 * @author nblomberg
 *
 */
@SuppressWarnings("serial")
public class DashboardAsyncMessage extends AbstractAsyncWebSocketMessage implements Serializable {
    
    /** The browser session sending the message. */
    private String browserSessionKey;
    
    /** 
     * Constructor - default (Needed for gwt serialization).
     */
    public DashboardAsyncMessage() {
    }
    
    /**
     * Constructor 
     * @param browserSessionKey The browser session that is sending the message.
     * @param action The action to be applied on the server.
     */
    public DashboardAsyncMessage(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
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
        
        sb.append("[DashboardAsyncMessage: ");
        sb.append("browserSessionKey = ").append(getBrowserSessionKey());
        sb.append(", ").append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
