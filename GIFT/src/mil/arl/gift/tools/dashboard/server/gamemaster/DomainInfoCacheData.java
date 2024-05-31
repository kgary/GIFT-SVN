/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.gamemaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedBookmarkCache;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedStrategyCache;

/**
 * Wrapper class to contain the cache data for each domain session id.
 *
 * @author sharrison
 */
public class DomainInfoCacheData {
    /**
     * Uniquely identifies a domain session.
     *
     * @author tflowers
     *
     */
    public static class DomainSessionKey {

        /** The id of the domain session being described. */
        private final int domainSessionId;

        /**
         * The unique numeric identifier of the user who owns the domain
         * session. This id is only globally unique when {@link #experimentId}
         * is null. If {@link #experimentId} is non-null, then it must be used
         * as well when identifying the user.
         */
        private final int userId;

        /**
         * The unique identifier of the experiment to which this domain session
         * belongs. Can be null if this domain session key does not belong to an
         * experiment.
         */
        private final String experimentId;

        /**
         * The name of the user who owns the domain session. Can be null for
         * experiment users.
         */
        private final String username;
        
        /** The unique ID of the service being used to play back this session, if this session is being played back */
        private final String playbackId;

        /**
         * Constructs a {@link DomainSessionKey} identifying the domain session
         * from which a provided {@link DomainSessionMessageInterface} came.
         *
         * @param domainMsg The {@link DomainSessionMessageInterface} that came
         *        from the domain session for which a {@link DomainSessionKey}
         *        should be constructed. Can't be null.
         */
        public DomainSessionKey(DomainSessionMessageInterface domainMsg) {
            if (domainMsg == null) {
                throw new IllegalArgumentException("The parameter 'domainMsg' cannot be null.");
            }

            this.domainSessionId = domainMsg.getDomainSessionId();
            this.username = domainMsg.getUsername();
            this.experimentId = domainMsg.getExperimentId();
            this.userId = domainMsg.getUserId();
            
            if(domainMsg instanceof DomainSessionMessageEntry) {
                this.playbackId = ((DomainSessionMessageEntry) domainMsg).getPlaybackId();
                
            } else {
                this.playbackId = null;
            }
        }

        /**
         * Constructs a {@link DomainSessionKey} identifying the domain session
         * which the provided {@link AbstractKnowledgeSession} is part of.
         *
         * @param knowledgeSession The {@link AbstractKnowledgeSession} that belongs to the
         *        domain session for which a {@link DomainSessionKey} should be
         *        constructed. Can't be null.
         */
        public DomainSessionKey(AbstractKnowledgeSession knowledgeSession) {
            
            if (knowledgeSession == null) {
                throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
            }
            
            SessionMember sessionMember = knowledgeSession.getHostSessionMember();

            this.domainSessionId = sessionMember.getDomainSessionId();
            this.experimentId = sessionMember.getUserSession().getExperimentId();
            this.userId = sessionMember.getUserSession().getUserId();
            this.username = sessionMember.getSessionMembership().getUsername();
            this.playbackId = knowledgeSession.getPlaybackId();
        }

        /**
         * Getter for the domainSessionId.
         *
         * @return The value of {@link #domainSessionId}.
         */
        public int getDomainSessionId() {
            return domainSessionId;
        }

        /**
         * Getter for the userId.
         *
         * @return The value of {@link #userId}.
         */
        public int getUserId() {
            return userId;
        }

        /**
         * Getter for the experimentId.
         *
         * @return The value of {@link #experimentId}.
         */
        public String getExperimentId() {
            return experimentId;
        }

        /**
         * Getter for the username.
         *
         * @return The value of {@link #username}.
         */
        public String getUsername() {
            return username;
        }

        @Override
        public int hashCode() {
            if (experimentId == null) {
                return Objects.hash(domainSessionId, username, playbackId);
            } else {
                return Objects.hash(experimentId, userId, playbackId);
            }
        }

        @Override
        public boolean equals(Object obj) {
            /* Do a null check and a type check */
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            final DomainSessionKey other = (DomainSessionKey) obj;

            /* If no experiment ID is provided, just use the domain session id
             * and username to test for equality. Otherwise, use the experiment
             * ID and user id to check for equality. */
            if (experimentId == null) {
                return Objects.equals(domainSessionId, other.domainSessionId)
                        && Objects.equals(username, other.username)
                        && Objects.equals(playbackId, other.playbackId);
            } else {
                return Objects.equals(experimentId, other.experimentId)
                        && Objects.equals(userId, other.userId)
                        && Objects.equals(playbackId, other.playbackId);
            }
        }

        @Override
        public String toString() {
            return new StringBuilder("[DomainSessionKey: ")
                    .append("domainSessionId = ").append(domainSessionId)
                    .append(", experimentId = ").append(experimentId)
                    .append(", userId = ").append(userId)
                    .append(", username = ").append(username)
                    .append(", playbackId = ").append(playbackId)
                    .append(']').toString();
        }
    }

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(DomainInfoCacheData.class);

    /** The cache containing the messages for the domain session */
    private final MessageCache messageCache = new MessageCache(MESSAGE_TYPE_WHITELIST);

    /** The domain session's ActiveMQ address */
    private String activeMQAddress;

    /** The domain session's User Session */
    private UserSession userSession;

    /** The domain session's cached processed strategies */
    private final List<ProcessedStrategyCache> processedStrategiesCache = new ArrayList<>();
    
    /** The domain session's cached processed bookmarks */
    private final List<ProcessedBookmarkCache> processedBookmarkCache = new ArrayList<>();

    /** The domain session's active knowledge session */
    private AbstractKnowledgeSession knowledgeSession;

    /**
     * The list of callbacks waiting for a response to the knowledge session
     * fetch
     */
    private final List<CompletableFuture<AbstractKnowledgeSession>> fetchSessionCallbacks = new ArrayList<>();

    /** Map a domain session id to its cache data */
    private static final Map<DomainSessionKey, DomainInfoCacheData> domainSessionToCacheData = new HashMap<>();

    /** Message whitelist */
    private static final List<MessageTypeEnum> MESSAGE_TYPE_WHITELIST = Arrays
            .asList(MessageTypeEnum.INITIALIZE_PEDAGOGICAL_MODEL_REQUEST, MessageTypeEnum.LEARNER_STATE);

    /**
     * Constructor
     */
    public DomainInfoCacheData() {
        if (logger.isTraceEnabled()) {
            logger.trace(".ctor()");
        }
    }

    /**
     * Get the cache data for the provided domain session. It will be created if
     * it doesn't already exist.
     *
     * @param key The unique identifier of the domain session.
     * @return the domain session's cache data. Will never be null.
     */
    public static DomainInfoCacheData getInstance(DomainSessionKey key) {
        /* If a cache has not yet been created, create it */
        DomainInfoCacheData cacheData = domainSessionToCacheData.get(key);
        if (cacheData == null) {
            cacheData = new DomainInfoCacheData();
            domainSessionToCacheData.put(key, cacheData);
        }

        return cacheData;
    }

    /**
     * Iterates over every {@link DomainInfoCacheData}.
     *
     * @return An {@link Iterator} of every {@link DomainInfoCacheData}. Can't
     *         be null.
     */
    public static Iterator<DomainInfoCacheData> iterator() {
        return domainSessionToCacheData.values().iterator();
    }

    /**
     * Erases all data for a given domain session id.
     *
     * @param key The unique identifier of the domain session whose data should
     *        be erased.
     */
    public static void removeInstance(DomainSessionKey key) {
        domainSessionToCacheData.remove(key);
    }

    /**
     * Retrieve the message cache for the domain session.
     *
     * @return the {@link MessageCache}. Will never be null.
     */
    public MessageCache getMessageCache() {
        return messageCache;
    }

    /**
     * Retrieve the ActiveMQ address for the domain session.
     *
     * @return the ActiveMQ address. Can be null if it was never set.
     */
    public String getActiveMQAddress() {
        return activeMQAddress;
    }

    /**
     * Set the ActiveMQ address for the domain session. Can only be set once.
     *
     * @param activeMQAddress the ActiveMQ address.
     */
    public void setActiveMQAddress(String activeMQAddress) {
        if (this.activeMQAddress != null) {
            throw new IllegalArgumentException("The domain session ActiveMQ address can only be set once.");
        }

        this.activeMQAddress = activeMQAddress;
    }

    /**
     * Retrieve the {@link UserSession} for the domain session.
     *
     * @return the user session. Can be null if it was never set.
     */
    public UserSession getUserSession() {
        return userSession;
    }

    /**
     * Set the {@link UserSession} for the domain session. Can only be set once.
     *
     * @param userSession the user session.
     */
    public void setUserSession(UserSession userSession) {
        if (this.userSession != null) {
            throw new IllegalArgumentException("The domain session user session can only be set once.");
        }

        this.userSession = userSession;
    }

    /**
     * Retrieve the list of {@link ProcessedStrategyCache cached processed
     * strategies} for the domain session.
     *
     * @return the processed strategies. Will never be null.
     */
    public List<ProcessedStrategyCache> getProcessedStrategiesCache() {
        return processedStrategiesCache;
    }
    
    /**
     * Retrieve the list of {@link ProcessedBookmarkCache cached processed
     * bookmarks} for the domain session.
     *
     * @return the processed bookmarks. Will never be null.
     */
    public List<ProcessedBookmarkCache> getProcessedBookmarkCache() {
        return processedBookmarkCache;
    }

    /**
     * Retrieve the {@link AbstractKnowledgeSession} for the domain session.
     *
     * @return the knowledge session. Can be null if it was never set.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    /**
     * Set the {@link AbstractKnowledgeSession} for the domain session.
     *
     * @param knowledgeSession the knowledge session to set. Can be null to
     *        clear the cached session associated with this domain session.
     */
    public void setKnowledgeSession(AbstractKnowledgeSession knowledgeSession) {
        this.knowledgeSession = knowledgeSession;
    }

    /**
     * Retrieve the list of callbacks that are waiting for the knowledge session
     * to be fetched.
     *
     * @return the list of callbacks. Will never be null.
     */
    public List<CompletableFuture<AbstractKnowledgeSession>> getFetchSessionCallbacks() {
        return fetchSessionCallbacks;
    }
}
