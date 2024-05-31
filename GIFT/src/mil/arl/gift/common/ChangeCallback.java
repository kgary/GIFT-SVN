/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * A callback that executes logic when a change occurs
 * 
 * @author sharrison
 * @param <T> The type of element that, if changed, will fire the callback.
 */
public interface ChangeCallback<T> {
    /**
     * Executes some logic when a change occurs
     * 
     * @param newValue the new value. Can be null.
     * @param oldValue the old value. Can be null.
     */
    public void onChange(T newValue, T oldValue);
}
