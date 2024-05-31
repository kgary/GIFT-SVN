/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.util.StringUtils;

/**
 * Class to hold the game master strategy metrics for a given strategy.
 *
 * @author sharrison
 */
public class StrategyMetrics {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyMetrics.class.getName());

    /** The name of the strategy with which these metrics apply */
    private final String strategyName;

    /**
     * The number of times the {@link #strategy} has been recommended to the
     * game master
     */
    private int numRecommended = 0;

    /**
     * The number of times the {@link #strategy} has been approved by the game
     * master
     */
    private int numApproved = 0;

    /**
     * The last known time this {@link #strategy} was approved by the game
     * master
     */
    private Date lastApprovedTime;

    /**
     * The last known game master username that approved this {@link #strategy}
     */
    private String lastApprover;

    /**
     * The list of callbacks to be executed whenever the metric data is changed
     */
    private Collection<ChangeCallback<Void>> changeCallbacks = new HashSet<>();

    /**
     * Constructor.
     *
     * @param strategyName the name of the strategy with which these metrics
     *        apply. Can't be null.
     * @param changeCallback the callback to be executed whenever the metric
     *        data changes. Can be null.
     */
    public StrategyMetrics(String strategyName, ChangeCallback<Void> changeCallback) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("StrategyMetrics(" + strategyName + ")");
        }

        if (StringUtils.isBlank(strategyName)) {
            throw new IllegalArgumentException("The parameter 'strategyName' cannot be blank.");
        }

        this.strategyName = strategyName;
        addChangeCallback(changeCallback);
    }

    /**
     * Retrieves the name of the strategy with which these metrics apply.
     *
     * @return the name of the strategy for these metrics
     */
    public String getStrategyName() {
        return strategyName;
    }

    /**
     * Retrieves the number of times the {@link #strategy} has been recommended
     * to the game master.
     *
     * @return the recommended count
     */
    public int getRecommendedCount() {
        return numRecommended;
    }

    /**
     * Increments the count for the number of times the {@link #strategy} has
     * been recommended to the game master.
     */
    public void incrementRecommendedCount() {
        numRecommended++;

        fireCallbacks();
    }

    /**
     * Retrieves the number of times the {@link #strategy} has been approved by
     * the game master.
     *
     * @return the approved count
     */
    public int getApprovedCount() {
        return numApproved;
    }

    /**
     * Retrieves the last known time this {@link #strategy} was approved by the
     * game master.
     *
     * @return the last known approval time. Can be null if the
     *         {@link #strategy} has not yet been approved.
     */
    public Date getLastApprovedTime() {
        return lastApprovedTime;
    }

    /**
     * The last known game master username that approved this {@link #strategy}.
     *
     * @return the last known approver's username. Can be null if the last
     *         approver was the automated system or if the {@link #strategy} has
     *         not yet been approved.
     */
    public String getLastApprover() {
        return lastApprover;
    }

    /**
     * Increments the number of times the {@link #strategy} has been approved by
     * the game master. This will also update the last known approval time to be
     * the current time.
     *
     * @param approverUsername the name of the game master that approved this
     *        {@link #strategy}. Can be null if it was approved by the system
     *        automatically.
     */
    public void incrementApprovedCount(String approverUsername) {
        numApproved++;
        lastApprovedTime = new Date();
        lastApprover = approverUsername;

        fireCallbacks();
    }

    /**
     * Executes the callbacks' onChange() method.
     */
    private void fireCallbacks() {
        for (ChangeCallback<Void> callback : changeCallbacks) {
            try {
                callback.onChange(null, null);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "There was an exception thrown within a ChangeCallback", t);
            }
        }
    }

    /**
     * Subscribes a {@link ChangeCallback} to this metric to be notified when
     * the data changes.
     *
     * @param callback the callback to be executed whenever the metric data
     *        changes. Can't be null.
     * @return true if the callback was subscribed successfully; false
     *         otherwise.
     */
    public boolean addChangeCallback(ChangeCallback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        return changeCallbacks.add(callback);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[StrategyMetrics: ");
        sb.append("strategyName = ").append(getStrategyName());
        sb.append(", numRecommended = ").append(getRecommendedCount());
        sb.append(", numApproved = ").append(getApprovedCount());
        sb.append(", lastApprovedTime = ").append(getLastApprovedTime());
        sb.append(", lastApprover = ").append(getLastApprover());
        sb.append("]");
        return sb.toString();
    }
}