/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

import mil.arl.gift.common.enums.MessageEncodingTypeEnum;

/**
 * Interface for processing raw messages received from the message broker.
 *
 * @author jleonard
 */
public interface RawMessageHandler
{
    /**
     * Callback for when a message is received from the message broker.
     *
     * @param msg The message to be handled.
     * @param encodingType - enumerated encoding type for this message
     * @return boolean - was the message processed
     */
    boolean processMessage(String msg, MessageEncodingTypeEnum encodingType);
}
