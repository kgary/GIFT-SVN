/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import mil.arl.gift.common.ChangeCallback;

/**
 * Class that protects a value from being directly accessed.
 * 
 * @author sharrison
 * @param <T> the type of value
 */
public class ProtectedProperty<T> {
    /** The value to protect */
    private T value;

    /** The callback to execute when the setter is called */
    private final ChangeCallback<T> changeCallback;

    /**
     * Constructor. Uses a null {@link #changeCallback}.
     */
    public ProtectedProperty() {
        this(null);
    }

    /**
     * Constructor
     * 
     * @param changeCallback the callback to execute when the setter is called.
     *        Can be null.
     */
    public ProtectedProperty(ChangeCallback<T> changeCallback) {
        this.changeCallback = changeCallback;
    }

    /**
     * Checks if a non-null value has been set.
     * 
     * @return true if a non-null value has been set; false otherwise.
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * Retrieve the value.
     * 
     * @return the value being protected. Can be null if it was never set.
     */
    public T getValue() {
        return value;
    }

    /**
     * Set the value. Will execute the {@link #changeCallback}.
     * 
     * @param value the value to set. Can be null.
     */
    public void setValue(T value) {
        if (changeCallback != null) {
            changeCallback.onChange(value, this.value);
        }

        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ProtectedProperty: ");
        sb.append("value = ").append(value);
        sb.append("]");
        return sb.toString();
    }
}
