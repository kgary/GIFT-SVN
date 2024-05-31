/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.util.StringUtils;

/**
 * A set of cached data surrounding a specific processed bookmark
 * 
 * @author sharrison
 */
@SuppressWarnings("serial")
public class ProcessedBookmarkCache implements Serializable {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ProcessedBookmarkCache.class.getName());

    private String text;

    private String media;

    /** The current knowledge session */
    private AbstractKnowledgeSession knowledgeSession;

    /** The time the bookmark was processed */
    private long timePerformed;

    /** The user that processed the bookmark */
    private String userPerformed;

    /**
     * Required for GWT serialization
     */
    private ProcessedBookmarkCache() {
    }

    /**
     * Constructor.
     *
     * @param text the text of the bookmark that was processed. Can be null.
     * @param media the media URL of the bookmark that was processed. Can be null.
     * @param knowledgeSession the current knowledge session. Can't be null.
     * @param timePerformed the time the bookmark was processed. Must be positive.
     * @param userPerformed the user that processed the bookmark. Can't be
     *        empty.
     */
    public ProcessedBookmarkCache(String text, String media,
            AbstractKnowledgeSession knowledgeSession, long timePerformed, String userPerformed) {
        this();
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(".ctor(");
            List<Object> params = Arrays.<Object>asList(text, media, timePerformed, userPerformed);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        } else if (timePerformed <= 0) {
            throw new IllegalArgumentException("The parameter 'timePerformed' must be positive.");
        } else if (userPerformed != null && userPerformed.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'userPerformed' cannot be empty.");
        }

        this.text = text;
        this.media = media;
        this.knowledgeSession = knowledgeSession;
        this.timePerformed = timePerformed;
        this.userPerformed = userPerformed;
    }

    /**
     * Gets the text of the bookmark that was processed
     * 
     * @return the text. Can be null.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the media URL of the bookmark that was processed
     * 
     * @return the media. Can be null.
     */
    public String getMedia() {
        return media;
    }

    /**
     * The knowledge session where this bookmark was processed.
     * 
     * @return the knowledge session. Can't be null.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    /**
     * The time at which the approval occurred.
     *
     * @return The {@link #timePerformed}.
     */
    public long getTimePerformed() {
        return timePerformed;
    }

    /**
     * @return The {@link #userPerformed}. A null value indicates it was
     *         automatically approved.
     */
    public String getUserPerformed() {
        return userPerformed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ProcessedBookmarkCache: ");
        sb.append("text = ").append(text);
        sb.append(", media = ").append(media);
        sb.append(", knowlege session = ").append(knowledgeSession);
        sb.append(", timePerformed = ").append(timePerformed);
        sb.append(", userPerformed = ").append(userPerformed);
        sb.append("]");
        return sb.toString();
    }
}
