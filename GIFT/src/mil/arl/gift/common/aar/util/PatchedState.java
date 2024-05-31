/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

/**
 * The patch for the payload in a playback log message.
 * 
 * @author sharrison
 */
public abstract class PatchedState {

    /** The time that the patch should be applied */
    private final long time;

    /** A unique identifier for this patch */
    private final String id;

    /**
     * Constructor.
     * 
     * @param time the time that the patch should be applied.
     */
    protected PatchedState(long time) {
        this.time = time;
        this.id = buildUniquePatchKey(time);
    }
    
    /**
     * Creates a new patch at the given time with the given ID. Note that patches with the
     * same ID at the same time will be merged, so if you wish to avoid merging patches, you
     * must ensure that the provided ID is unique.
     * 
     * @param time the time to apply the patch at in the log.
     * @param id the unique ID to assign this patch. Cannot be null.
     */
    protected PatchedState(long time, String id) {
        this.time = time;
        
        if(id == null) {
            throw new IllegalArgumentException("The ID of a patch state cannot be null");
        }
        
        this.id = id;
    }

    /**
     * The time that the patch should be applied.
     * 
     * @return the patch timestamp.
     */
    public long getTime() {
        return time;
    }

    /**
     * Get the unique identifier for this patch.
     * 
     * @return the unique identifier. Will never be null.
     */
    public String getId() {
        return id;
    }

    /**
     * Merge with the provided patch. If there is a conflict,
     * the provided patch will take precedence.
     * 
     * @param newPatch the patch to apply. If null, no changes will be made.
     */
    public abstract void updatePatch(PatchedState newPatch);

    /**
     * Apply this patch to the provided message.
     * 
     * @param toApplyMsg the message to apply the patch to.
     */
    public abstract void applyPatch(MessageManager toApplyMsg);

    

    /**
     * Builds the unique identifier for a {@link PatchedState}.
     * 
     * @param time the time the patch should be applied.
     * @return the unique identifier for the patch. Will never be null.
     */
    public abstract String buildUniquePatchKey(long time);
}