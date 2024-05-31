/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.Strategy;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.util.StringUtils;

/**
 * A set of cached data surrounding a specific processed strategy
 * 
 * @author sharrison
 */
@SuppressWarnings("serial")
public class ProcessedStrategyCache implements Serializable {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ProcessedStrategyCache.class.getName());

    /** The strategy that was processed */
    private Strategy strategy;

    /** The list of strategy activities that were approved */
    private List<Serializable> approvedActivities;

    /** The current knowledge session */
    private AbstractKnowledgeSession knowledgeSession;

    /** The time the strategy was processed */
    private long timePerformed;

    /** The user that processed the strategy */
    private String userPerformed;

    /**
     * Required for GWT serialization
     */
    private ProcessedStrategyCache() {
    }

    /**
     * Constructor.
     *
     * @param strategy the strategy that was processed. Can't be null.
     * @param approvedActivities the collection of strategy activities that were
     *        approved. Can't be null. Can be empty if no strategy activities
     *        were approved. If empty, the strategy is assumed to have been
     *        denied.
     * @param knowledgeSession the current knowledge session. Can't be null.
     * @param timePerformed the time the strategy was processed. Must be positive.
     * @param userPerformed the user that processed the strategy. Can't be
     *        empty.
     */
    public ProcessedStrategyCache(Strategy strategy, Collection<Serializable> approvedActivities,
            AbstractKnowledgeSession knowledgeSession, long timePerformed, String userPerformed) {
        this();
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(".ctor(");
            List<Object> params = Arrays.<Object>asList(strategy, approvedActivities, timePerformed, userPerformed);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        } else if (approvedActivities == null) {
            throw new IllegalArgumentException("The parameter 'approvedActivities' cannot be null.");
        } else if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        } else if (timePerformed <= 0) {
            throw new IllegalArgumentException("The parameter 'timePerformed' must be positive.");
        } else if (userPerformed != null && userPerformed.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'userPerformed' cannot be empty.");
        }

        this.strategy = strategy;
        this.approvedActivities = new ArrayList<>(approvedActivities);
        this.knowledgeSession = knowledgeSession;
        this.timePerformed = timePerformed;
        this.userPerformed = userPerformed;
    }

    /**
     * The {@link Strategy} represented by this {@link ProcessedStrategyCache}.
     *
     * @return The {@link #strategy}. Can't be null.
     */
    public Strategy getStrategy() {
        return strategy;
    }

    /**
     * The {@link List} of activities that were approved from the
     * {@link #strategy}.
     *
     * @return The {@link #approvedActivities}. Can't be null. An empty
     *         collection indicates that none of the activities were approved.
     */
    public List<Serializable> getApprovedActivities() {
        return approvedActivities;
    }

    /**
     * The knowledge session where this {@link #strategy} was processed.
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
        sb.append("[ProcessedStrategyCache: ");
        sb.append("strategy = ").append(strategy);
        sb.append(", approvedActivities = ").append(approvedActivities);
        sb.append(", knowlege session = ").append(knowledgeSession);
        sb.append(", timePerformed = ").append(timePerformed);
        sb.append(", userPerformed = ").append(userPerformed);
        sb.append("]");
        return sb.toString();
    }
}
