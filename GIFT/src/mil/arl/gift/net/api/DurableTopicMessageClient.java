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
import javax.jms.Topic;

import org.apache.activemq.ActiveMQMessageConsumer;

/**
 * A durable topic connection to an ActiveMQ message broker.
 *
 * @author jleonard
 */
public class DurableTopicMessageClient extends MessageClient {

    private final String subscriptionName;

    /**
     * Class constructor - set the connection URL to a specified value
     *
     * @param url The URL of the message broker to connect to. If null
     *        the default will be used.
     * @param subjectName The name of the JMS subject to interact with
     * @param subscriptionName the subscription name.
     */
    public DurableTopicMessageClient(String url, String subjectName, String subscriptionName) {
        super(url, subjectName, true);
        this.subscriptionName = subscriptionName;
    }

    public String getSubscriptionName() {

        return subscriptionName;
    }

    @Override
    protected void completeConnection() throws JMSException {

        Topic destTopic = getBrokerSession().createTopic(getSubjectName());

        MessageProducer brokerProducer = getBrokerSession().createProducer(destTopic);
        brokerProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        setMessageProducer(brokerProducer);

        if (isConsumer()) {

            ActiveMQMessageConsumer brokerConsumer = (ActiveMQMessageConsumer) getBrokerSession().createDurableSubscriber(destTopic, subscriptionName);
            setMessageConsumer(brokerConsumer);
        }
    }
}
