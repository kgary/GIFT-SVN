/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.gamemaster;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tools.dashboard.shared.messages.TaskStateCache;

/**
 * A cache that stores the last received {@link Message} of given
 * {@link MessageTypeEnum MessageTypes}
 *
 * @author tflowers
 *
 */
public class MessageCache {

    /** The types of messages that should be cached */
    private static final Collection<MessageTypeEnum> WHITE_LIST = new HashSet<>();

    /** The lookup for the last received message of a type. */
    private final Map<MessageTypeEnum, DomainSessionMessageInterface> messageLookup = new HashMap<>();

    /* The complete list of all message source event ids to their sender
     * addresses so that duplicate messages can be identified and potentially
     * filtered out */
    private final HashSet<SimpleEntry<Integer, String>> duplicateFilter = new HashSet<SimpleEntry<Integer, String>>();

    /** Maps domain session ids to the domain sessions cache of task state. */
    private ConcurrentHashMap<Integer, TaskStateCache> cachedTaskStates = new ConcurrentHashMap<>();

    /**
     * Builds a message cache that caches all processed messages.
     */
    public MessageCache() {
        this(MessageTypeEnum.VALUES());
    }

    /**
     * Builds a message cache that caches messages of specified types.
     *
     * @param whiteList The {@link Collection} of {@link MessageTypeEnum} to
     *        cache. Can't be null or empty.
     */
    public MessageCache(Collection<? extends MessageTypeEnum> whiteList) {
        if (whiteList == null || whiteList.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'whiteList' cannot be null or empty.");
        }

        WHITE_LIST.addAll(whiteList);
    }

    /**
     * Caches a messages payload if necessary.
     *
     * @param message The {@link mil.arl.gift.net.api.message.DomainSessionMessage} to cache. Can't be null.
     * @param isPastSessionMode whether the session is being shown in past session mode (false if active session).
     */
    public void processMessage(DomainSessionMessageInterface message, boolean isPastSessionMode) {
        if (message == null) {
            throw new IllegalArgumentException("The parameter 'message' cannot be null.");
        }

        /* Add the message to the duplicate filter so we can identify if any
         * other messages are a duplicate of this one */
        final SimpleEntry<Integer, String> key = new SimpleEntry<Integer, String>(message.getSourceEventId(),
                message.getSenderAddress());
        duplicateFilter.add(key);

        if (WHITE_LIST.contains(message.getMessageType())) {

            /* If in 'PAST' playback mode, check if the incoming message is
             * before any cached messages. If yes, remove the cached message
             * (and if the cached message is a learner state, then also clear
             * the cached tasks state). */
            if (isPastSessionMode) {
                Iterator<Entry<MessageTypeEnum, DomainSessionMessageInterface>> entryItr = messageLookup.entrySet().iterator();
                while (entryItr.hasNext()) {
                    Entry<MessageTypeEnum, DomainSessionMessageInterface> next = entryItr.next();
                    if (next.getValue().getTimeStamp() > message.getTimeStamp()) {
                        if (next.getKey().equals(MessageTypeEnum.LEARNER_STATE)) {
                            cachedTaskStates.clear();
                        }

                        entryItr.remove();
                    }
                }
            }

            if(MessageTypeEnum.LEARNER_STATE.equals(message.getMessageType()) && message.getPayload() instanceof LearnerState) {

                //need to perform additional caching to track the state of this domain session's tasks
                processTaskStates(message.getDomainSessionId(), (LearnerState) message.getPayload());
            }

            messageLookup.put(message.getMessageType(), message);
        }
    }

    /**
     * Checks if the given message has already been processed.
     * 
     * @param message the message to check. Can't be null.
     * @return true if the message is a duplicate; false otherwise.
     */
    public boolean isDuplicate(DomainSessionMessageInterface message) {
        if (message == null) {
            throw new IllegalArgumentException("The parameter 'message' cannot be null.");
        }

        final int sourceEventId = message.getSourceEventId();

        /* Cannot be a duplicate if there is no id */
        if (sourceEventId == Message.ID_NOT_AVAILABLE) {
            return false;
        }

        final SimpleEntry<Integer, String> key = new SimpleEntry<Integer, String>(sourceEventId,
                message.getSenderAddress());
        return duplicateFilter.contains(key);
    }

    /**
     * Processes the current states of all the tasks represented by the given learner state and caches information surrounding their
     * behavior throughout the lifetime of the session, such as how long they were active and when they last became active.
     *
     * @param domainSessionId the ID of the domain session message that the learner state was retrieved from
     * @param learnerState the learner state whose task information should be processed.
     */
    private void processTaskStates(int domainSessionId, LearnerState learnerState) {

        DomainSessionMessageInterface lastLearnerStateMessage = getMessage(MessageTypeEnum.LEARNER_STATE);
        LearnerState lastLearnerState = null;

        if(lastLearnerStateMessage != null && lastLearnerStateMessage.getPayload() instanceof LearnerState) {

            //get the previous learner state for comparison, if possible
            lastLearnerState = (LearnerState) lastLearnerStateMessage.getPayload();
        }

        PerformanceState performance = learnerState.getPerformance();

        for (Integer taskId : performance.getTasks().keySet()) {

            TaskStateCache cacheState = cachedTaskStates.get(taskId);
            if(cacheState == null) {

                //this task has no cached state information yet, so create some
                cacheState = new TaskStateCache();
                cachedTaskStates.put(taskId, cacheState);
            }

            TaskPerformanceState taskState = performance.getTasks().get(taskId);
            TaskPerformanceState lastTaskState = null;

            if(lastLearnerState != null) {
                lastTaskState = lastLearnerState.getPerformance().getTasks().get(taskId);
            }

            if(PerformanceNodeStateEnum.ACTIVE.equals(taskState.getState().getNodeStateEnum())) {

                if(lastTaskState == null || !PerformanceNodeStateEnum.ACTIVE.equals(lastTaskState.getState().getNodeStateEnum())) {
                    synchronized(cacheState) {

                        //task has become active, so record the time it became active
                        cacheState.setLastActiveTimestamp(taskState.getState().getShortTermTimestamp());
                    }
                }

            } else {

                if(lastTaskState != null && PerformanceNodeStateEnum.ACTIVE.equals(lastTaskState.getState().getNodeStateEnum())) {
                    synchronized(cacheState) {

                        //task became inactive, so calculate how long it was active
                        cacheState.addCumulativeActiveTime(taskState.getState().getShortTermTimestamp() - cacheState.getLastActiveTimestamp());
                    }
                }
            }
        }
    }

    /**
     * Gets the last received message of a given type.
     *
     * @param type The {@link MessageTypeEnum} of the message to retrieve. Can't
     *        be null.
     * @return The {@link Message} of the given type that was last received. Can
     *         be null if no {@link Message} of that type has been cached.
     */
    public DomainSessionMessageInterface getMessage(MessageTypeEnum type) {
        if (type == null) {
            throw new IllegalArgumentException("The parameter 'type' cannot be null.");
        }

        return messageLookup.get(type);
    }

    /**
     * Get the timestamp of the most recent message.
     * 
     * @return the most recent message timestamp. Can be null if no messages are
     *         cached.
     */
    public Long getLatestMessageTimestamp() {
        Long latestTimestamp = null;

        for (DomainSessionMessageInterface message : messageLookup.values()) {
            if (latestTimestamp == null || message.getTimeStamp() > latestTimestamp) {
                latestTimestamp = message.getTimeStamp();
            }
        }

        return latestTimestamp;
    }

    /**
     * Gets the cached state information maintained by this message cache for its domain knowledge tasks
     *
     * @return the cached task state information
     */
    public HashMap<Integer, TaskStateCache> getCachedTaskStates(){
        return new HashMap<>(cachedTaskStates);
    }
}
