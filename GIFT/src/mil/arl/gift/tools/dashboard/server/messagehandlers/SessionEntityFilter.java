/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.messagehandlers;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.tools.dashboard.server.UserSessionManager;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInfoCacheData.DomainSessionKey;

/**
 * This class serves as a way of filtering and regulating the frequency of
 * {@link EntityState} messages that are sent the client.
 *
 * @author tflowers
 *
 */
public class SessionEntityFilter {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SessionEntityFilter.class);

    /**
     * Maps a browser session key to its dedicated {@link SessionEntityFilter}
     * instance.
     */
    private final static Map<String, SessionEntityFilter> browserSessionKeyToInstance = new ConcurrentHashMap<>();

    /** Maps the domain session id to the filter for the session */
    private final ConcurrentHashMap<DomainSessionKey, DomainSessionEntityFilter> domainSessionIdToFilter = new ConcurrentHashMap<>();

    /**
     * {@link BrowserWebSession} for which {@link EntityState} messages are
     * filtered by this object.
     */
    private final BrowserWebSession browserSession;

    /**
     * A private constructor that is used to control when a
     * {@link SessionEntityFilter} is constructed.
     *
     * @param browserSessionKey The unique identifier of the
     *        {@link BrowserWebSession} for which this object will filter
     *        {@link EntityState} messages. Can't be null.
     */
    private SessionEntityFilter(String browserSessionKey) {
        if (browserSessionKey == null) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be null.");
        }

        browserSession = UserSessionManager.getInstance().getBrowserSession(browserSessionKey);
    }

    /**
     * Gets the dedicated instance of a {@link SessionEntityFilter} for a
     * specified browser session.
     *
     * @param browserSessionKey The unique identifier of the browser for which
     *        the {@link SessionEntityFilter} is being fetched.
     * @return The {@link SessionEntityFilter} for the specified browsers. Can't
     *         be null.
     */
    public static SessionEntityFilter getInstance(String browserSessionKey) {
        return browserSessionKeyToInstance.computeIfAbsent(browserSessionKey, key -> new SessionEntityFilter(key));
    }

    /**
     * Removes the
     *
     * @param browserSessionKey The unique identifier of the browser session
     *        whose {@link SessionEntityFilter} should be removed from the map.
     */
    public static void destroyInstance(String browserSessionKey) {
        final SessionEntityFilter sef = browserSessionKeyToInstance.remove(browserSessionKey);
        Iterator<Map.Entry<DomainSessionKey, DomainSessionEntityFilter>> iter = sef.domainSessionIdToFilter.entrySet()
                .iterator();

        while (iter.hasNext()) {
            iter.next().getValue().destroy();
            iter.remove();
        }
    }

    /**
     * Creates a {@link DomainSessionEntityFilter} within this
     * {@link SessionEntityFilter}.
     *
     * @param ks The {@link AbstractKnowledgeSession} for which to create the
     *        {@link DomainSessionEntityFilter}.
     * @return The {@link DomainSessionEntityFilter} that was created or the
     *         existing one if it was previously created already.
     */
    public DomainSessionEntityFilter createDomainSessionFilter(AbstractKnowledgeSession ks) {
        if (logger.isTraceEnabled()) {
            logger.trace("createDomainSessionFilter(" + ks + ")");
        }

        /* If the filter for this domain session id already exists, return
         * it. */
        final DomainSessionKey domainSessionKey = new DomainSessionKey(ks);
        final AbstractServerWebSocket ws = browserSession.getWebSocket();
        return domainSessionIdToFilter.computeIfAbsent(domainSessionKey, id -> {
            if (logger.isInfoEnabled()) {
                logger.info("Creating a "+DomainSessionEntityFilter.class.getSimpleName()+" for domain session "+domainSessionKey);
            }

            return new DomainSessionEntityFilter(id, ws);
        });
    }

    /**
     * Fetches the {@link DomainSessionEntityFilter} for a specified domain
     * session id.
     *
     * @param domainSessionKey The unique key identifying the domain session for
     *        which to fetch the {@link DomainSessionEntityFilter}.
     * @return The associated {@link DomainSessionEntityFilter}. Can be null if
     *         none yet exists for the specified domain session.
     */
    public DomainSessionEntityFilter getDomainSessionFilter(DomainSessionKey domainSessionKey) {
        return domainSessionIdToFilter.get(domainSessionKey);
    }

    /**
     * Destroys the existing {@link DomainSessionEntityFilter} for the specified
     * domain session.
     *
     * @param domainSessionKey The unique key identifying the domain session for
     *        which to destroy the {@link DomainSessionEntityFilter}.
     */
    public void destroyDomainSessionFilter(DomainSessionKey domainSessionKey) {
        if (logger.isTraceEnabled()) {
            logger.trace("destroyDomainSessionFilter(" + domainSessionKey + ")");
        }

        DomainSessionEntityFilter dsef = domainSessionIdToFilter.remove(domainSessionKey);
        dsef.destroy();
    }
}