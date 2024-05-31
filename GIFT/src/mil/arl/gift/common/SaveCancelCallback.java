/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * A callback that executes logic for when the user chooses to save or cancel.
 * 
 * @author sharrison
 */
public interface SaveCancelCallback {
    /**
     * Callback used when the user chooses to save.
     */
    void save();

    /**
     * Callback used when the user chooses to cancel.
     */
    void cancel();
}
