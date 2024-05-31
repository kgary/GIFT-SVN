/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import mil.arl.gift.net.api.message.Message;

/**
 * A callback interface for notifying the events of a message collection
 *
 * @author jleonard
 */
public interface MessageCollectionCallback {
    /**
     * All messages were responded to successfully.
     */
    void success();

    /**
     * A message was responded to with a Non-NACK/ACK message
     * 
     * Note: make sure to keep the timing of the logic for this method to a
     * minimum in order to not hold onto the decoded message received queue
     * thread.
     * 
     * @param msg The message received
     */
    void received(Message msg);

    /**
     * A message was responded to with a NACK message Note: make sure to keep
     * the timing of the logic for this method to a minimum in order to not hold
     * onto the decoded message received queue thread.
     * 
     * @param msg The NACK message received
     */
    void failure(Message msg);

    /**
     * Some failure occurred within the collection (ie. timeout) Note: make sure
     * to keep the timing of the logic for this method to a minimum in order to
     * not hold onto the decoded message received queue thread.
     * 
     * @param why The reason for failure
     */
    void failure(String why);
}
