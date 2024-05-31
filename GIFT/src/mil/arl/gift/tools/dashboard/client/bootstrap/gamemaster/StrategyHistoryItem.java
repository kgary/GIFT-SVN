/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.Strategy;
import mil.arl.gift.common.util.StringUtils;

/**
 * Container class for the strategy notifications that were approved or denied. Also maintains
 * additional information (e.g. who and when).
 *
 * @author sharrison
 */
public class StrategyHistoryItem {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyHistoryItem.class.getName());

    /** The strategy that was processed */
    private final Strategy strategy;

    /** The list of strategy activities that were approved */
    private final Collection<Serializable> approvedActivities;

    /** The time the strategy was processed */
    private final long timePerformed;

    /** The user that processed the strategy */
    private final String userPerformed;

    /**
     * Constructor. Uses the current date as the time performed.
     *
     * @param strategy the strategy that was processed. Can't be null.
     * @param approvedActivities the collection of strategy activities that were approved. Can't be
     *        null. Can be empty if no strategy activities were approved. If empty, the strategy is
     *        assumed to have been denied.
     * @param userPerformed the user that processed the strategy. Can't be empty.
     */
    public StrategyHistoryItem(Strategy strategy, Collection<Serializable> approvedActivities, String userPerformed) {
        this(strategy, approvedActivities, new Date().getTime(), userPerformed);
    }

    /**
     * Constructor.
     *
     * @param strategy the strategy that was processed. Can't be null.
     * @param approvedActivities the collection of strategy activities that were
     *        approved. Can't be null. Can be empty if no strategy activities
     *        were approved. If empty, the strategy is assumed to have been
     *        denied.
     * @param timePerformed the time the strategy was processed. Must be positive.
     * @param userPerformed the user that processed the strategy. Can't be
     *        empty.
     */
    public StrategyHistoryItem(Strategy strategy, Collection<Serializable> approvedActivities, long timePerformed,
            String userPerformed) {
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
        } else if (timePerformed <= 0) {
            throw new IllegalArgumentException("The parameter 'timePerformed' must be positive.");
        } else if (userPerformed != null && userPerformed.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'userPerformed' cannot be empty.");
        }

        this.strategy = strategy;
        this.approvedActivities = approvedActivities;
        this.timePerformed = timePerformed;
        this.userPerformed = userPerformed;
    }

    /**
     * The {@link Strategy} represented by this {@link StrategyHistoryItem}.
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
    public Collection<Serializable> getApprovedActivities() {
        return approvedActivities;
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
        sb.append("[StrategyHistoryItem: ");
        sb.append("strategy = ").append(strategy);
        sb.append(", approvedActivities = ").append(approvedActivities);
        sb.append(", timePerformed = ").append(timePerformed);
        sb.append(", userPerformed = ").append(userPerformed);
        sb.append("]");
        return sb.toString();
    }
}
