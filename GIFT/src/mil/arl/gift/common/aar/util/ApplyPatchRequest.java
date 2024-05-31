/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

import java.util.Set;

import mil.arl.gift.common.state.PerformanceStateAttribute;

/**
 * The request parameters to apply a PerformanceStateAttribute patch into the
 * log messages
 * 
 * @author sharrison
 */
public class ApplyPatchRequest {
    
    /** The performance states that contain the patched fields for each changed attribute */
    private final Set<PerformanceStateAttribute> patchedPerformanceState;

    /** The timestamp of the patch */
    private final long timestamp;

    /**
     * Flag indicating if the patch should apply to the entire message span or
     * to start at the provided timestamp
     */
    private boolean updateEntireSpan = true;

    /**
     * Constructor
     * 
     * @param patchedPerformanceState the performance states that contain the 
     *        patched fields for each changed attribute. Can't be null or empty.
     * @param timestamp the time the patch was applied.
     */
    public ApplyPatchRequest(Set<PerformanceStateAttribute> patchedPerformanceState, long timestamp) {
        if (patchedPerformanceState == null || patchedPerformanceState.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'patchedPerformanceState' cannot be null or empty.");
        }

        this.patchedPerformanceState = patchedPerformanceState;
        this.timestamp = timestamp;
    }

    /**
     * Get the performance states that contain the patched changes for each changed attribute
     * 
     * @return the patched performance states. Will never be null or empty.
     */
    public Set<PerformanceStateAttribute> getPatchedPerformanceState() {
        return patchedPerformanceState;
    }

    /**
     * The time the patch was applied.
     * 
     * @return the timestamp for the patch.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the flag indicating if the patch should apply to the entire message
     * span or to start at the provided timestamp.
     * 
     * @param updateEntireSpan true to apply to the entire message span (whole
     *        rectangle in the timeline); false to apply it at the timestamp
     *        location, if no message exists at this location, one will be
     *        created.
     */
    public void setUpdateEntireSpan(boolean updateEntireSpan) {
        this.updateEntireSpan = updateEntireSpan;
    }

    /**
     * Get the flag indicating if the patch should apply to the entire message
     * span or to start at the provided timestamp.
     * 
     * @return true to apply to the entire message span (whole rectangle in the
     *         timeline); false to apply it at the timestamp location.
     */
    public boolean isUpdateEntireSpan() {
        return updateEntireSpan;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ApplyPatchRequest patchedPerformanceState =");
        builder.append(patchedPerformanceState);
        builder.append(", timestamp =");
        builder.append(timestamp);
        builder.append(", updateEntireSpan =");
        builder.append(updateEntireSpan);
        builder.append("]");
        return builder.toString();
    }
    
    
}
