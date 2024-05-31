/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An event that was triggered on a list of items. Contains the action that was performed and the
 * list of items affected.
 * 
 * @author sharrison
 * 
 * @param <T> The type of elements contained within the list which is being edited.
 */
public class ListChangedEvent<T> {

    /**
     * A list of actions that can be performed on a list.
     * 
     * @author sharrison
     */
    public enum ListAction {
        /** Add an item to a list */
        ADD,
        /** Remove an item from a list */
        REMOVE,
        /** Edit an item in a list */
        EDIT,
        /** Reorder the items in a list */
        REORDER
    }

    /** The {@link ListAction} that was performed on a list */
    private ListAction actionPerformed;

    /** The list of item that were affected by the {@link #actionPerformed} */
    private List<T> affectedItems;

    /**
     * Constructs an event for when a list is changed.
     * 
     * @param action The {@link ListAction} that was performed on a list. Can't be null.
     * @param affectedItem The item that was affected by the {@link #actionPerformed}.<br>
     *        Note: for {@link ListAction#REORDER} use {@link #ListChangedEvent(ListAction, List)}.
     *        Can't be null.
     */
    public ListChangedEvent(ListAction action, T affectedItem) {
        if (action == null) {
            throw new IllegalArgumentException("The parameter 'action' cannot be null.");
        } else if (affectedItem == null) {
            throw new IllegalArgumentException("The parameter 'affectedItem' cannot be null.");
        }

        this.actionPerformed = action;
        this.affectedItems = Arrays.asList(affectedItem);
    }

    /**
     * Constructs an event for when a list is changed.
     * 
     * @param action The {@link ListAction} that was performed on a list. Can't be null.
     * @param affectedItems The list of item that were affected by the {@link #actionPerformed}.<br>
     *        Note: for {@link ListAction#REORDER} this list should contain the complete reordered
     *        list. Can't be null.
     */
    public ListChangedEvent(ListAction action, List<T> affectedItems) {
        if (action == null) {
            throw new IllegalArgumentException("The parameter 'action' cannot be null.");
        } else if (affectedItems == null || affectedItems.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'affectedItem' cannot be null or empty.");
        }

        this.actionPerformed = action;
        this.affectedItems = affectedItems;
    }

    /**
     * The {@link ListAction} that was performed on a list
     * 
     * @return the {@link ListAction}. Can't be null.
     */
    public ListAction getActionPerformed() {
        return actionPerformed;
    }

    /**
     * The list of item that were affected by the {@link #actionPerformed}.
     * 
     * @return the unmodifiable list of affected items. Can't be null or empty. <br>
     *         Note: for {@link ListAction#REORDER} this list should contain the complete reordered
     *         list.
     */
    public List<T> getAffectedItems() {
        return Collections.unmodifiableList(affectedItems);
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[ListChangedEvent: ")
                .append("actionPerformed = ").append(actionPerformed)
                .append(", affectedItems = ").append(affectedItems)
                .append("]").toString();
    }
}
