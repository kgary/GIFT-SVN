/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server.messagehandler;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.module.TutorModuleStatus;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.embedded.message.codec.EmbeddedAppMessageEncoder;
import mil.arl.gift.tutor.server.DomainWebState;
import mil.arl.gift.tutor.server.TutorModule;
import mil.arl.gift.tutor.server.UserWebSession;

/**
 * Handler for incoming embedded application messages into the tutor server
 *
 * @author nblomberg
 *
 */
public class EmbeddedAppMessageHandler  {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedAppMessageHandler.class);

    /**
     * Processes the incoming embedded application message which typically involves sending the message
     * from the tutor module to the domain module via ActiveMQ.
     *
     * @param userWebSession The parent user web session that owns the incoming message
     * @param message The incoming message that is received from the embedded application message
     */
    public void processIncomingEmbeddedAppMessage(UserWebSession userWebSession, String message) {
        try {
            //Creates the payload for the message from the JSON string
            Object payload = EmbeddedAppMessageEncoder.decodeForGift(message);

            DomainWebState domainSession = userWebSession.getDomainWebState();
            if(domainSession != null) {
                TutorModuleStatus status = TutorModule.getInstance().getTutorModuleStatus(domainSession.getDomainSessionId());
                if(status != null) {
                    String tutorTopic = status.getTopicName();

                    //Send the message and its payload through the appropriate channel
                    if(payload instanceof ACK) {
                        //Get the message to reply to
                        Message pendingMessage = userWebSession.dequeueMessagePendingReply();
                        if(pendingMessage != null) {
                            //Send the reply and remove the message to reply to from the browser session
                            TutorModule.getInstance().sendReply(
                                    pendingMessage,
                                    payload,
                                    MessageTypeEnum.PROCESSED_ACK);
                        } else {
                            logger.error("Unable to send ACK because there is no message to reply to for the user web session, " + this);
                        }
                    } else {
                        //If the shutdown process has begun then only acks should be sent to the domain module
                        if(userWebSession.getEmbeddedApplicationSentStopFreeze() || userWebSession.getEmbeddedApplicationReceivedSimanStop()) {
                            return;
                        }

                        try{
                            // Get the message type
                            MessageTypeEnum messageType = EmbeddedAppMessageEncoder.getDecodedMessageType(payload);

                            //If the message is a StopFreeze, the shutdown sequence has begun
                            if(messageType == MessageTypeEnum.STOP_FREEZE) {
                                userWebSession.setEmbeddedApplicationSentStopFreeze(true);
                            }

                            if (messageType != null) {
                                TutorModule.getInstance().sendEmbeddedApplicationMessageToDomainSession(
                                        tutorTopic, 
                                        payload, 
                                        userWebSession.getUserSessionInfo(), 
                                        domainSession.getDomainSessionId(), 
                                        domainSession.getExperimentId(), 
                                        messageType);
                            }
                        }catch(Exception e){
                            logger.error("A message type for the message being sent from the embedded training app could not be determined.", e);
                        }
                    }
                } else {
                    logger.error("There is no status for the domain session " + domainSession + " therefore there is no tutor topic to send the embedded app message to.");
                }
            } else {
                logger.error("Unable to find a domain session web session for user: " + this);
            }
        } catch(@SuppressWarnings("unused") ParseException parseEx) {
            logger.error("The message being sent from the embedded training app to the EmbeddedAppTopic was malformed: " + message);
        }
    }




}
