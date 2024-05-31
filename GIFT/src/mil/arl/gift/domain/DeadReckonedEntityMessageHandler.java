/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import mil.arl.gift.net.api.message.Message;


/**
 * Defines the interface required by classes wanting callbacks with entity state information containing dead-reckoned entity location. 
 * 
 * @author cragusa
 */
public interface DeadReckonedEntityMessageHandler {

    /**
     * Callback method to receive dead reckoned entity information.
     * @param message a Message instance. Although not strictly required by the method signature, the message type of message should be MessageTypeEnum.ENTITY_STATE.
     */
    void handleDeadReckonedEntityMessage(Message message);
}
