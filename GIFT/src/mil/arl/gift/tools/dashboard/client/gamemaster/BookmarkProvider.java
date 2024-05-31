/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.BookmarkProvider.BookmarkHandler;

/**
 * A singleton class that handles incoming bookmarks.
 * 
 * @author sharrison
 */
public class BookmarkProvider extends AbstractProvider<BookmarkHandler> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(BookmarkProvider.class.getName());

    /** The instance of the class */
    private static BookmarkProvider instance = null;

    /**
     * Singleton constructor
     */
    private BookmarkProvider() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     * 
     * @return the instance to the provider singleton object.
     */
    public static BookmarkProvider getInstance() {
        if (instance == null) {
            instance = new BookmarkProvider();
        }

        return instance;
    }

    /**
     * Notify all listeners that the given bookmark has been received and needs to be handled
     * 
     * @param domainSessionId the knowledge session domain id.
     * @param evaluator The username of the person who created the bookmark. Can be null.
     * @param msgTimestamp The timestamp of the message that contained the bookmark.
     * @param comment The text comment of the bookmark. Can be null.
     * @param media the URL of the bookmark's associated media, if any. Can be null.
     * @param isPatched whether the bookmark has been patched into a past session
     */
    public void addBookmark(final int domainSessionId,
            final String evaluator, final long msgTimestamp, final String comment, final String media, final boolean isPatched) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("addBoomark(");
            List<Object> params = Arrays.<Object>asList(comment, media, domainSessionId, evaluator, msgTimestamp);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        /* Check if the session pushing the update is whitelisted */
        if (!RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {
            return;
        }

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<BookmarkHandler>() {
            @Override
            public void execute(BookmarkHandler handler) {
                handler.addBookmark(domainSessionId, evaluator, msgTimestamp, comment, media, isPatched);
            }
        });
    }

    /**
     * Handler for listening for incoming bookmarks.
     * 
     * @author sharrison
     */
    public interface BookmarkHandler {
        
        /**
         * Handles when the given bookmark has been received and needs to be handled
         * 
         * @param domainSessionId the knowledge session domain id.
         * @param evaluator The username of the person who created the bookmark. Can be null.
         * @param msgTimestamp The timestamp of the message that contained the bookmark.
         * @param comment The text comment of the bookmark. Can be null.
         * @param media the URL of the bookmark's associated media, if any. Can be null.
         * @param isPatched whether the bookmark has been patched into a past session
         */
        void addBookmark(int domainSessionId,
                String evaluator, long msgTimestamp, String comment, String media, boolean isPatched);
    }
}
