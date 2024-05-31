/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.validation;

/**
 * A callback that executes logic when the validation status changed.
 * 
 * @author sharrison
 */
public interface ValidationStatusChangedCallback {
    /**
     * Executes some logic when the validation status is changed.
     * 
     * @param isValid true if the validation status changed to being valid;
     *        false if it changed to being invalid.
     * @param fireEvents true to force the change callback to fire; false will
     *        only fire the change callback if the validity changes.
     */
    public void changedValidity(boolean isValid, boolean fireEvents);
}
