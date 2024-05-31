/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;

/**
 * An interface that describes an object that can handle a message update in order
 * to send it to the client.
 * 
 * @author nroberts
 */
public interface MessageUpdateHandler {

    /**
     * Handles the given message update
     * 
     * @param update the update to handle. Cannot be null.
     */
    public void onMessage(AbstractMessageUpdate update);
}
