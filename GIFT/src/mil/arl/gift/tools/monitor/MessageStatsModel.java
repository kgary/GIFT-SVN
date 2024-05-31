/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.api.message.Message;

/**
 * Model for the statistics of a set of messages sent
 *
 * @author jleonard
 */
public class MessageStatsModel implements MonitorMessageListener {

    private static Logger logger = LoggerFactory.getLogger(MessageStatsModel.class);

    /**
     * Class for representing the statistics of a message
     */
    public class MessageStatistics {

        private final MessageTypeEnum messageType;

        private int messageCount = 0;

        /**
         * Constructor
         *
         * @param messageType The type of message this statistic is for
         */
        public MessageStatistics(MessageTypeEnum messageType) {
            this.messageType = messageType;
        }

        /**
         * Constructor
         *
         * Copies an existing message statistic
         *
         * @param messageStat contains the information to copy
         */
        public MessageStatistics(MessageStatistics messageStat) {
            this(messageStat.getMessageType());
            this.messageCount = messageStat.getMessageCount();
        }

        /**
         * Gets the message type of the statistic
         *
         * @return MessageTypeEnum The message type of the statistics
         */
        public MessageTypeEnum getMessageType() {
            return messageType;
        }

        /**
         * Gets the number of times this message has been received
         *
         * @return int The number of times this message has been received
         */
        public int getMessageCount() {
            return messageCount;
        }

        /**
         * Increments the number of times this message has been received
         */
        public void incrementMessageCount() {
            messageCount += 1;
        }
    }

    /**
     * Interface for listening to updates to the statistics of messages
     *
     * @author jleonard
     */
    public interface MessageStatisticsUpdateListener {

        /**
         * Callback when there is an update to the statistics of messages
         *
         * @param messageStatistics The list of updated message statistics
         */
        public void onMessageStatisticsUpdate(List<MessageStatistics> messageStatistics);
    }

    private class MessageStatisticsComparator implements Comparator<MessageStatistics> {

        @Override
        public int compare(MessageStatistics o1, MessageStatistics o2) {

            return o2.getMessageCount() - o1.getMessageCount();
        }
    }
    private final Map<MessageTypeEnum, MessageStatistics> messageStatisticsMap = new HashMap<MessageTypeEnum, MessageStatistics>();

    private final Set<MessageStatisticsUpdateListener> messageStatisticsUpdateListeners = new HashSet<MessageStatisticsUpdateListener>();

    /**
     * Default Constructor
     */
    public MessageStatsModel() {

        Timer refreshTimer = new Timer("MessageStatsModel-refreshTimer");

        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateMessageStatistics();
            }
        }, 0, 1000);
    }

    @Override
    public void handleMessage(Message msg) {

        synchronized (messageStatisticsMap) {

            MessageStatistics messageStatistic = messageStatisticsMap.get(msg.getMessageType());

            if (messageStatistic != null) {

                messageStatistic.incrementMessageCount();
                messageStatisticsMap.put(msg.getMessageType(), messageStatistic);

            } else {

                messageStatistic = new MessageStatistics(msg.getMessageType());
                messageStatistic.incrementMessageCount();
                messageStatisticsMap.put(msg.getMessageType(), messageStatistic);
            }
        }
    }

    private void updateMessageStatistics() {

        List<MessageStatistics> newMessageStatistics = new ArrayList<MessageStatistics>();

        synchronized (messageStatisticsMap) {

            for (MessageStatistics messageStatistic : messageStatisticsMap.values()) {

                newMessageStatistics.add(new MessageStatistics(messageStatistic));
            }
        }

        Collections.sort(newMessageStatistics, new MessageStatisticsComparator());

        synchronized (messageStatisticsUpdateListeners) {

            for (MessageStatisticsUpdateListener listener : messageStatisticsUpdateListeners) {

                try {

                    listener.onMessageStatisticsUpdate(newMessageStatistics);

                } catch (Exception e) {

                    logger.error("Caught exception from misbehaving listener " + listener, e);
                }
            }
        }
    }

    /**
     * Adds a listener interesting in getting updates on message statistics
     *
     * @param listener The interested listener
     */
    public void addMessageStatisticsUpdateListener(MessageStatisticsUpdateListener listener) {

        synchronized (messageStatisticsUpdateListeners) {

            messageStatisticsUpdateListeners.add(listener);
        }
    }

    /**
     * Removes a listener no longer interested in getting updates on message
     * statistics
     *
     * @param listener The uninterested listener
     */
    public void removeMessageStatisticsUpdateListener(MessageStatisticsUpdateListener listener) {

        synchronized (messageStatisticsUpdateListeners) {

            messageStatisticsUpdateListeners.remove(listener);
        }
    }
}
