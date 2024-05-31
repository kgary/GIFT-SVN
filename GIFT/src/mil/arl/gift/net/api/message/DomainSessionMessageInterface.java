/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;

/**
 * Represents the basic interface for a message for a domain session.
 *
 * @author tflowers
 *
 */
public interface DomainSessionMessageInterface {
    /**
     * Getter for the type of the message.
     *
     * @return The {@link MessageTypeEnum} of the message. Can't be null.
     */
    MessageTypeEnum getMessageType();

    /**
     * Return the unique domain session id for the current learner's domain
     * session
     *
     * @return the int value of the domain session id
     */
    int getDomainSessionId();

    /**
     * Getter for the name of the user whose session is sending the message.
     *
     * @return The {@link String} value of the username.
     */
    String getUsername();

    /**
     * Getter for the epoch time stamp of the message.
     *
     * @return The long value of the time stamp of the message.
     */
    long getTimeStamp();

    /**
     * The payload of the domain session message.
     *
     * @return The {@link Object} value of the payload of the message.
     */
    Object getPayload();

    /**
     * Getter for the module type of the module who sent the message.
     *
     * @return The {@link ModuleTypeEnum} value of the type of the sender.
     */
    ModuleTypeEnum getSenderModuleType();

    /**
     * Getter for the address of the module who sent the message.
     *
     * @return The {@link String} value of the sender address.
     */
    String getSenderAddress();

    /**
     * Getter for the id of the user whose session is sending and receiving the
     * message.
     *
     * @return The int value of user's id.
     */
    int getUserId();

    /**
     * Getter for the user session which contains information about the user
     * session (including the unique user id of the learner) the message is
     * associated with.
     * 
     * @return the {@link UserSession}. Can't be null.
     */
    UserSession getUserSession();

    /**
     * Getter for the id of the experiment to which the represented domain
     * session belongs (if the domain session is part of an experiment).
     *
     * @return The unique identifier of the experiment. Can be null if the
     *         domain session is not part of an experiment.
     */
    String getExperimentId();

    /**
     * Return the message's source event Id Refer to the class attributes
     * comments for more information.
     *
     * @return int the sequence number for this message
     */
    int getSourceEventId();
}
