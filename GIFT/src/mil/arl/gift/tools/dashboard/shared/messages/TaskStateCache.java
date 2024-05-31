/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

/**
 * A set of cached data surrounding a specific task's state
 * 
 * @author nroberts
 */
@SuppressWarnings("serial")
public class TaskStateCache implements Serializable{
    
    /** 
     * The timestamp representing the last time this task became active. If this task is still active, this timestamp can 
     * be used to calculate how long it has currently be active
     */
    private long lastActiveTimestamp = 0;
    
    /** 
     * The cumulative time this task was active before being deactivated. To get the total time this task has been active, 
     * add this value with the amount of time passed since {@link #lastActiveTimestamp}.
     */
    private long cumulativeActiveTime = 0;
    
    /**
     * Creates a new set of cached data surrounding a specific task's state
     */
    public TaskStateCache() {}

    /**
     * Gets the timestamp representing the last time this task became active. If this task is still active, this timestamp can 
     * be used to calculate how long it has currently be active
     * 
     * @return the last time this task became active
     */
    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    /**
     * Sets the timestamp representing the last time this task became active. If this task is still active, this timestamp can 
     * be used to calculate how long it has currently be active
     * 
     * @param lastActiveTimestamp the last time this task became active
     */
    public void setLastActiveTimestamp(long lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    /**
     * Gets the cumulative time this task was active before being deactivated. To get the total time this task has been active, 
     * add this value with the amount of time passed since {@link #getLastActiveTimestamp()}.
     * 
     * @return the cumulative time this task was active
     */
    public long getCumulativeActiveTime() {
        return cumulativeActiveTime;
    }

    /**
     * Adds the given number of milliseconds to the cumulative time this task was active before being deactivated. To get 
     * the total time this task has been active, add this value with the amount of time passed 
     * since {@link #getLastActiveTimestamp()}.
     * 
     * @param millis the number of milliseconds to add to this task's cumulative active time
     */
    public void addCumulativeActiveTime(long millis) {
        this.cumulativeActiveTime += millis;
    }
    
    
}