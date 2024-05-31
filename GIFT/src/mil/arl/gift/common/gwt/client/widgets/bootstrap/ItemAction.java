/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.constants.IconType;

/**
 * Defines an action which can be performed on any of the elements of the given list.
 * 
 * @author tflowers
 *
 * @param <T> The type element that the action is called on.
 */
public interface ItemAction<T> {

    /**
     * Specifies whether or not the action can be performed.
     * 
     * @param item The item the action was called on.
     * 
     * @return True if the action can be performed, false otherwise.
     */
    boolean isEnabled(T item);

    /**
     * Executes the action
     * 
     * @param item The item the action was called on.
     */
    void execute(T item);

    /**
     * Gets the text of the tool tip. Should describe to the user what will happen when the
     * action is invoked.
     * 
     * @param item The item the action was called on.
     * 
     * @return The text of the tool tip. Can be null.
     */
    String getTooltip(T item);

    /**
     * The icon that should be displayed as a representation of the action
     * 
     * @param item The item the action was called on.
     * 
     * @return The icon that visually represents the action.
     */
    IconType getIconType(T item);
}