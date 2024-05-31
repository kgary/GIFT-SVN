/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.gamemaster;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.tools.dashboard.shared.messages.EntityStateUpdate;

/**
 * Used to track and manage the frequency of messages.
 *
 * @author mhoffman
 *
 */
public class MessageFrequencyThrottle {

    /**
     * the minimum duration between entity state messages for a single entity
     * (identifier)
     */
    private static final long ENTITY_STATE_MIN_TIME = 250;

    /** singleton instance for this class */
    private static MessageFrequencyThrottle instance = null;

    /**
     * mapping of domain session id to the information about the various messages
     * going through that session.
     */
    private Map<Integer, SessionMessages> dsIdToSessionMessages = new HashMap<>();

    /**
     * private constructor - singleton model
     */
    private MessageFrequencyThrottle(){}

    /**
     * Return the singleton instance.
     *
     * @return the singleton instance of this class
     */
    public static MessageFrequencyThrottle getInstance(){

        if(instance == null){
            instance = new MessageFrequencyThrottle();
        }

        return instance;
    }

    /**
     * Return whether to allow the message to be sent for the domain session based
     * on message filtering protocols (e.g. frequency).
     *
     * @param domainSessionId uniquely identifies a domain session for which the message
     * is trying to be sent for.
     * @param giftMessage the message to analyze if it should be sent.
     * @return true if the message should be allowed to be sent.
     */
    public boolean allow(int domainSessionId, EntityStateUpdate giftMessage) {

        SessionMessages sessionMessages = dsIdToSessionMessages.get(domainSessionId);
        if(sessionMessages == null){
            sessionMessages = new SessionMessages();
            dsIdToSessionMessages.put(domainSessionId, sessionMessages);
        }

        return sessionMessages.allow(giftMessage);
    }

    /**
     * Used to analyze messages for a single domain session.
     *
     * @author mhoffman
     *
     */
    public class SessionMessages{

        /**
         * mapping of entity state entity identifier to the epoch time at which the
         * last entity state message for that entity was allowed to be sent.
         */
        private Map<Integer, Long> lastEntityStateMsg = new HashMap<>();

        /**
         * Return whether to allow the entity state to be sent based on
         * message filtering protocols (e.g. frequency).
         *
         * @param entityState contains information about a specific entity of which needs to be checked
         * against filtering protocols.
         * @return true if it has been at least {@link MessageFrequencyThrottle#ENTITY_STATE_MIN_TIME} milliseconds
         * since the last entity state allowed OR the entity state entity appearance has active set to false.
         */
        private boolean allow(EntityStateUpdate entityState) {

            long now = System.currentTimeMillis();
            Long lastSentTime = lastEntityStateMsg.get(entityState.getSessionEntityId().getEntityId());
            boolean allow = lastSentTime == null || now - lastSentTime > ENTITY_STATE_MIN_TIME
                    || !entityState.isActive();

            if (allow) {
                lastEntityStateMsg.put(entityState.getSessionEntityId().getEntityId(), now);
            }

            return allow;
        }
    }
}
