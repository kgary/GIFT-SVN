/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;

import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.command.ActiveMQDestination;

import com.google.protobuf.AbstractMessage;

public class ProtobufQueueMessageClient<T extends AbstractMessage> extends ProtobufMessageClient<T> {

    public ProtobufQueueMessageClient(String connectionUrl, String subjectName, boolean pruneOldMessages,
            T messageTemplate, T errorMessageTemplate, T ackTemplate) {
        super(connectionUrl, subjectName, pruneOldMessages, DestinationType.QUEUE, messageTemplate,
                errorMessageTemplate, ackTemplate);
    }

    @Override
    protected void completeConnection() throws JMSException {

        activeMQDestination = (ActiveMQDestination) getBrokerSession().createQueue(getSubjectName());

        MessageProducer brokerProducer = getBrokerSession().createProducer(activeMQDestination);
        brokerProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        setMessageProducer(brokerProducer);

        if (isConsumer()) {

            ActiveMQMessageConsumer brokerConsumer = (ActiveMQMessageConsumer) getBrokerSession()
                    .createConsumer(activeMQDestination);
            setMessageConsumer(brokerConsumer);
        }
    }
}
