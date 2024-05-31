/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.gamemaster;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tools.dashboard.server.WebMonitorModule;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInfoCacheData.DomainSessionKey;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedBookmarkCache;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedStrategyCache;
import mil.arl.gift.tools.dashboard.shared.messages.TaskStateCache;

/**
 * A cache for domain session related information. All cached information is
 * stored by processing {@link Message Messages}.
 *
 * @author tflowers
 *
 */
public class DomainInformationCache {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(DomainInformationCache.class);

    /** A set of the sessions that have established a connection */
    private final Set<Integer> connectedDomainSessions = new HashSet<>();

    /**
     * Process a provided {@link Message} by storing any data that needs to be
     * cached from it.
     *
     * @param msg The {@link Message} to process. Can't be null.
     * @return true if the message is a duplicate; false otherwise.
     */
    public boolean processMessage(Message msg) {
        if (msg == null) {
            throw new IllegalArgumentException("The parameter 'msg' cannot be null.");
        } else if (!(msg instanceof DomainSessionMessageInterface)) {
            return false;
        }

        DomainSessionMessageInterface domainMsg = (DomainSessionMessageInterface) msg;
        final DomainSessionKey key = new DomainSessionKey(domainMsg);

        SessionType sessionType = null;
        boolean isPastSessionMode = false;
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(key);
        if (cacheData.getKnowledgeSession() != null) {
            sessionType = cacheData.getKnowledgeSession().getSessionType();
            isPastSessionMode = cacheData.getKnowledgeSession().inPastSessionMode();
        }

        /* Check if the message is a duplicate. Skip this step if in playback
         * mode. */
        if (!isPastSessionMode && cacheData.getMessageCache().isDuplicate(domainMsg)) {
            return true;
        }

        /* Put the message in the cache */
        cacheData.getMessageCache().processMessage(domainMsg, isPastSessionMode);

        /* If in 'PAST' playback mode, check if the incoming message is
         * before any cached strategies. If yes, remove the cached
         * strategies that are from the 'future'. */
        final List<ProcessedStrategyCache> cache = cacheData.getProcessedStrategiesCache();
        synchronized (cache) {
            if (isPastSessionMode && CollectionUtils.isNotEmpty(cache)) {
                final long msgTime = domainMsg.getTimeStamp();

                /* Iterate in reverse direction since the cache should be
                 * chronological */
                ListIterator<ProcessedStrategyCache> cacheItr = cache.listIterator(cache.size());
                while (cacheItr.hasPrevious()) {
                    if (cacheItr.previous().getTimePerformed() > msgTime) {
                        cacheItr.remove();
                    } else {
                        /* No more 'future' cache items; exit loop */
                        break;
                    }
                }
            }
        }

        /* If the address for the domain session has not yet been saved, save
         * it */
        if (cacheData.getActiveMQAddress() == null && domainMsg.getSenderModuleType() == ModuleTypeEnum.DOMAIN_MODULE) {
            cacheData.setActiveMQAddress(domainMsg.getSenderAddress());
        }

        if (domainMsg instanceof DomainSessionMessage) {
            DomainSessionMessage domainSessionMsg = (DomainSessionMessage) domainMsg;
            /* If the user session for the domain session has not yet been
             * saved, save it */
            if (cacheData.getUserSession() == null) {
                cacheData.setUserSession(domainSessionMsg.getUserSession());
            }

            /* Clear the session cache on lesson started so that it can be
             * retrieved anew from the domain the next time someone needs it.
             * This guarantees that the session contains all necessary
             * information (e.g. joined members). */
            if (msg.getMessageType().equals(MessageTypeEnum.LESSON_STARTED) && SessionType.ACTIVE.equals(sessionType)) {
                cacheData.setKnowledgeSession(null);
            }
        }

        return false;
    }

    /**
     * Erases all data for a given domain session.
     *
     * @param domainSessionKey The unique key identifying the domain session.
     */
    public void dropDomainSessionData(DomainSessionKey domainSessionKey) {
        if (logger.isDebugEnabled()) {
            logger.debug("dropDomainSessionData(" + domainSessionKey + ")");
        }

        DomainInfoCacheData.removeInstance(domainSessionKey);
    }

    /**
     * Gets the ActiveMQ address of a given domain session id.
     *
     * @param domainSessionKey The unique key identifying the domain session for
     *        which to fetch the address.
     * @return The ActiveMQ address
     */
    public String getDomainModuleAddress(DomainSessionKey domainSessionKey) {
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);
        return cacheData.getActiveMQAddress();
    }

    /**
     * Specifies whether or not the Web Monitor has established an ActiveMQ
     * connection to a domain session.
     *
     * @param domainSessionId The id of the domain session for which to set the
     *        connection status.
     * @param isConnected The boolean indicating the connection status. True if
     *        the Web Monitor is connected to the session, false otherwise.
     */
    public void setSessionConnected(int domainSessionId, boolean isConnected) {
        if (isConnected) {
            connectedDomainSessions.add(domainSessionId);
        } else {
            connectedDomainSessions.remove(domainSessionId);
        }
    }

    /**
     * Determines whether or not the Web Monitor has established an ActiveMQ
     * connection for a domain session.
     *
     * @param domainSessionId The id of the domain session for which to test the
     *        connection.
     * @return True if an ActiveMQ connection already exists for the domain
     *         session, false otherwise.
     */
    public boolean isSessionConnected(int domainSessionId) {
        return connectedDomainSessions.contains(domainSessionId);
    }

    /**
     * Determines if any of the domain sessions to which the WebMonitor have
     * established a connection exist at the given ActiveMQ address.
     *
     * @param address The address for which to test a connection. Can't be null.
     * @return True if a connected domain session exists at that address, false
     *         otherwise.
     */
    public boolean isAddressedReferenced(String address) {
        if (address == null) {
            throw new IllegalArgumentException("The parameter 'address' cannot be null.");
        }

        final Iterator<DomainInfoCacheData> iter = DomainInfoCacheData.iterator();
        while (iter.hasNext()) {
            final DomainInfoCacheData cacheData = iter.next();
            if (StringUtils.equals(cacheData.getActiveMQAddress(), address)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the user session for a given domain session id.
     *
     * @param domainSessionKey The unique key identifying the domain session for
     *        which to fetch the user session.
     * @return the user session
     */
    public UserSession getUserSession(DomainSessionKey domainSessionKey) {
        DomainInfoCacheData cacheData = DomainInfoCacheData
                .getInstance(domainSessionKey);
        return cacheData.getUserSession();
    }

    /**
     * Gets the last {@link LearnerState} payload that was received.
     *
     * @param domainSessionKey The unique key identifying the domain session for
     *        which to fetch the {@link LearnerState}.
     * @return The last received {@link LearnerState} payload. Returns null if
     *         one has not yet been received.
     */
    public LearnerState getLastLearnerState(DomainSessionKey domainSessionKey) {
        return (LearnerState) getMessagePayloadForDomainSession(domainSessionKey, MessageTypeEnum.LEARNER_STATE);
    }

    /**
     * Gets the last {@link InitializePedagogicalModelRequest} payload that was
     * received.
     *
     * @param domainSessionKey The unique key identifying the domain session for
     *        which to fetch the {@link InitializePedagogicalModelRequest}.
     * @return The last received {@link InitializePedagogicalModelRequest}
     *         payload. Returns null if one has not yet been received.
     */
    public InitializePedagogicalModelRequest getLastInitializePedagogicalRequest(DomainSessionKey domainSessionKey) {
        return (InitializePedagogicalModelRequest) getMessagePayloadForDomainSession(domainSessionKey,
                MessageTypeEnum.INITIALIZE_PEDAGOGICAL_MODEL_REQUEST);
    }

    /**
     * Gets the currently cached payload for a {@link MessageTypeEnum} within a
     * given domain session.
     *
     * @param key The unique key identifying the domain session for which to
     *        fetch the payload.
     * @param msgType The {@link MessageTypeEnum} of the payload to fetch. Can't
     *        be null.
     * @return The payload that was cached. Can be null if there is no domain
     *         session with the given id or if there is no cached payload of the
     *         given {@link MessageTypeEnum}.
     */
    private Object getMessagePayloadForDomainSession(DomainSessionKey key, MessageTypeEnum msgType) {
        if (msgType == null) {
            throw new IllegalArgumentException("The parameter 'msgType' cannot be null.");
        }

        /* Get cache data */
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(key);
        DomainSessionMessageInterface message = cacheData.getMessageCache().getMessage(msgType);
        return message != null ? message.getPayload() : null;
    }

    /**
     * Gets the cached state information for all of the domain knowledge tasks
     * in the message cached for the domain session with the given ID
     *
     * @param domainSessionKey the unique key identifying the domain session
     * @return the cached task state information
     */
    public HashMap<Integer, TaskStateCache> getCachedTaskStates(DomainSessionKey domainSessionKey) {
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);
        return cacheData.getMessageCache().getCachedTaskStates();
    }

    /**
     * Gets the processed strategies cached for the domain session with the
     * given ID.
     *
     * @param domainSessionKey the unique key identifying the domain session
     * @return an unmodifiable list of cached processed strategies. Can return
     *         null if the domain session id does not contain any cached
     *         strategies.
     */
    public List<ProcessedStrategyCache> getCachedProcessedStrategies(DomainSessionKey domainSessionKey) {
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);
        return Collections.unmodifiableList(cacheData.getProcessedStrategiesCache());
    }
    
    /**
     * Gets the processed bookmarks cached for the domain session with the
     * given ID.
     *
     * @param domainSessionKey the unique key identifying the domain session
     * @return an unmodifiable list of cached processed bookmarks. Can return
     *         null if the domain session id does not contain any cached
     *         bookmarks.
     */
    public List<ProcessedBookmarkCache> getCachedProcessedBookmarks(DomainSessionKey domainSessionKey) {
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);
        return Collections.unmodifiableList(cacheData.getProcessedBookmarkCache());
    }

    /**
     * Caches the processed strategies referenced by the
     * {@link ProcessedStrategyCache} items.
     *
     * @param domainSessionKey The unique key identifying the domain session.
     * @param strategyCache the collection of processed strategies to cache.
     */
    public void cacheProcessedStrategy(DomainSessionKey domainSessionKey, Collection<ProcessedStrategyCache> strategyCache) {
        if (CollectionUtils.isEmpty(strategyCache)) {
            return;
        }

        /* Get cache data */
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);

        final List<ProcessedStrategyCache> cacheList = cacheData.getProcessedStrategiesCache();
        synchronized (cacheList) {
            for (ProcessedStrategyCache cacheItem : strategyCache) {
                if (cacheItem != null) {
                    cacheList.add(cacheItem);
                }
            }
        }
    }
    
    /**
     * Caches the processed bookmarks referenced by the
     * {@link ProcessedBookmarkCache} items.
     *
     * @param domainSessionKey The unique key identifying the domain session.
     * @param bookmarkCache the collection of processed bookmarks to cache.
     */
    public void cacheProcessedBookmark(DomainSessionKey domainSessionKey, Collection<ProcessedBookmarkCache> strategyCache) {
        if (CollectionUtils.isEmpty(strategyCache)) {
            return;
        }

        /* Get cache data */
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);

        final List<ProcessedBookmarkCache> cacheList = cacheData.getProcessedBookmarkCache();
        synchronized (cacheList) {
            for (ProcessedBookmarkCache cacheItem : strategyCache) {
                if (cacheItem != null) {
                    cacheList.add(cacheItem);
                }
            }
        }
    }

    /**
     * Caches the knowledge sessions for the given domain sessions.
     *
     * @param domainToSessionMap the sessions to cache with their associated
     *        domain session id.
     */
    public void cacheKnowledgeSessions(Map<Integer, AbstractKnowledgeSession> domainToSessionMap) {
        for (Entry<Integer, AbstractKnowledgeSession> entry : domainToSessionMap.entrySet()) {
            cacheKnowledgeSession(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Caches the knowledge session for the given domain session.
     *
     * @param domainSessionId the ID of the domain session
     * @param knowledgeSession the session to cache
     */
    public void cacheKnowledgeSession(int domainSessionId, AbstractKnowledgeSession knowledgeSession) {
        final DomainSessionKey key = new DomainSessionKey(knowledgeSession);
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(key);
        cacheData.setKnowledgeSession(knowledgeSession);
    }

    /**
     * Gets a specified cached knowledge session or queries the Domain module
     * for such a knowledge session.
     *
     * @param domainSessionKey The {@link DomainSessionKey} identifying the
     *        domain session to fetch. Can't be null.
     * @return A {@link CompletableFuture} representing the asynchronous
     *         operation. Can't be null.
     */
    public CompletableFuture<AbstractKnowledgeSession> getCachedKnowledgeSession(DomainSessionKey domainSessionKey) {
        CompletableFuture<AbstractKnowledgeSession> future = new CompletableFuture<>();

        /* Get cache data */
        final DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);

        AbstractKnowledgeSession session = cacheData.getKnowledgeSession();
        if (session != null) {
            if (logger.isTraceEnabled()) {
                // this happens frequently so don't use debug or higher
                logger.trace(
                        "getCachedKnowledgeSession found cached session for domainSessionKey: " + domainSessionKey);
            }

            future.complete(session);
        } else {
            final List<CompletableFuture<AbstractKnowledgeSession>> futureList = cacheData.getFetchSessionCallbacks();
            synchronized (futureList) {
                /* If the list is not empty, then the request has already been
                 * sent. Add to the list and wait. */
                if (!futureList.isEmpty()) {
                    futureList.add(future);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding callback to fetch knowledge session for domainSessionKey '"
                                + domainSessionKey + "'; size = " + futureList.size());
                    }

                    return future;
                }

                /* Add to the list and request for the knowledge session */
                futureList.add(future);
                WebMonitorModule.getInstance().requestActiveKnowledgeSessionsFromDomain(new MessageCollectionCallback() {
                    @Override
                    public void success() {
                        AbstractKnowledgeSession session = cacheData.getKnowledgeSession();
                        if (session == null && logger.isDebugEnabled()) {
                            logger.debug(
                                    "requestActiveKnowledgeSessionsFromDomain: Requested active sessions but returned null session for domain session key: "
                                            + domainSessionKey);
                        }

                        /* Remove the list of waiting callbacks now that they
                         * have been processed. */
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "requestActiveKnowledgeSessionsFromDomain: removing entries for domain session key: "
                                            + domainSessionKey + " with size: " + futureList.size());
                        }

                        /* Process all callbacks waiting for the response */
                        synchronized (futureList) {
                            Iterator<CompletableFuture<AbstractKnowledgeSession>> itr = futureList.iterator();
                            while (itr.hasNext()) {
                                CompletableFuture<AbstractKnowledgeSession> waitingFuture = itr.next();
                                waitingFuture.complete(session);
                                itr.remove();
                            }
                        }
                    }

                    @Override
                    public void received(Message msg) {
                    }

                    @Override
                    public void failure(String why) {
                        final String msg = "Failed to retrieve the active knowledge sessions because '" + why + "'.";
                        logger.error("requestActiveKnowledgeSessionsFromDomain: " + msg);

                        /* Process all callbacks waiting for the response */
                        synchronized (futureList) {
                            Iterator<CompletableFuture<AbstractKnowledgeSession>> itr = futureList.iterator();
                            while (itr.hasNext()) {
                                CompletableFuture<AbstractKnowledgeSession> waitingCallback = itr.next();
                                waitingCallback.completeExceptionally(new DetailedException(msg, msg, null));
                                itr.remove();
                            }
                        }
                    }

                    @Override
                    public void failure(Message msg) {
                        final String failureMsg = "Failed to retrieve the active knowledge sessions because '" + msg
                                + "'.";
                        logger.error("requestActiveKnowledgeSessionsFromDomain: " + failureMsg);

                        /* Process all callbacks waiting for the response */
                        synchronized (futureList) {
                            Iterator<CompletableFuture<AbstractKnowledgeSession>> itr = futureList.iterator();
                            while (itr.hasNext()) {
                                CompletableFuture<AbstractKnowledgeSession> waitingCallback = itr.next();
                                waitingCallback
                                        .completeExceptionally(new DetailedException(failureMsg, failureMsg, null));
                                itr.remove();
                            }
                        }
                    }
                });
            }

        }

        return future;
    }

    /**
     * Get the timestamp of the most recent message.
     *
     * @param domainSessionKey the unique key identifying the domain session for
     *        which to fetch the most recent timestamp.
     * @return the most recent message timestamp. Can be null if no messages are
     *         cached.
     */
    public Long getLatestMessageTimestamp(DomainSessionKey domainSessionKey) {
        DomainInfoCacheData cacheData = DomainInfoCacheData.getInstance(domainSessionKey);
        return cacheData.getMessageCache().getLatestMessageTimestamp();
    }
}