/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared.websocket.messages;

/**
 * The AbstractAsyncWebSocketMessage is used to send an async message via the websocket
 * where a response is expected.  All async websocket messages should extend from this class.  
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractAsyncWebSocketMessage extends AbstractWebSocketMessage  {

   
    /** 
     * Constructor - default
     */
    public AbstractAsyncWebSocketMessage() {
        super();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[AbstractAsyncWebSocketMessage: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
