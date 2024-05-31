/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

/**
 * Interface for processing received GIFT messages
 *
 * @author jleonard
 *
 */
public interface MessageHandler
{
    /**
     * Callback for when a message is received
     *
     * @param message The GIFT message to be handled.
     * @return boolean - was the message processed
     */
    boolean processMessage(Message message);
}
