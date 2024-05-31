/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection;

import java.util.List;

import mil.arl.gift.common.experiment.DataCollectionItem;

/**
 * An action that describes how a course should be moved within a
 * {@link CourseCollection}.
 *
 * @author tflowers
 *
 */
public class ReorderAction extends AbstractCourseCollectionAction {

    /** The version used by the serialization logic */
    private static final long serialVersionUID = 1L;

    /** The index of the element within the collection which is being moved */
    private int oldIndex;

    /** The index at which the element being moved should be placed */
    private int newIndex;

    /** The order of the collection of element at the time the action was requested */
    private List<DataCollectionItem> currentOrdering;

    /**
     * No argument constructor for GWT serialization.
     */
    @SuppressWarnings("unused")
    private ReorderAction() {
    }

    /**
     * Constructs a new {@link ReorderAction} that should be executed on the
     * server.
     *
     * @param username The name of the user who is requesting the execution of
     *        the action. Cannot be blank.
     * @param collectionId The id of the collection on which the action is being
     *        executed. Cannot be blank.
     * @param oldIndex The index at which the element to move is located. Cannot
     *        be negative and should be within the bounds of the collection
     *        being modified.
     * @param newIndex The index at which the element being moved should be
     *        placed. Cannot be negative and should be within the bounds of the
     *        collection being modified.
     * @param currentOrdering The {@link List} that describes the current
     *        ordering of elements when the request was created. Can't be null
     *        or empty.
     */
    public ReorderAction(String username, String collectionId, int oldIndex, int newIndex, List<DataCollectionItem> currentOrdering) {
        super(username, collectionId);
        if (oldIndex == newIndex) {
            throw new IllegalArgumentException("The values of oldIndex and newIndex can't be the same.");
        }

        setOldIndex(oldIndex);
        setNewIndex(newIndex);
        setCurrentOrdering(currentOrdering);
    }

    /**
     * Getter for the index at which the element to move is located.
     *
     * @return The int value of {@link #oldIndex}. Cannot be negative.
     */
    public int getOldIndex() {
        return oldIndex;
    }

    /**
     * Setter for the index at which the element to move is located.
     *
     * @param oldIndex The new int value of {@link #oldIndex}. Cannot be
     *        negative.
     */
    private void setOldIndex(int oldIndex) {
        if (oldIndex < 0) {
            throw new IllegalArgumentException("The value of 'oldIndex' cannot be negative.");
        }

        this.oldIndex = oldIndex;
    }

    /**
     * Getter for the index at which the element to move should be placed.
     *
     * @return The int value of {@link #newIndex}. Cannot be negative.
     */
    public int getNewIndex() {
        return newIndex;
    }

    /**
     * Setter for the index at which the element to move should be placed.
     *
     * @param newIndex The new int value of {@link #newIndex}. Cannot be
     *        negative.
     */
    private void setNewIndex(int newIndex) {
        if (newIndex < 0) {
            throw new IllegalArgumentException("The value of 'newIndex' cannot be negative.");
        }

        this.newIndex = newIndex;
    }

    /**
     * Getter for the ordering of the collection when the {@link ReorderAction}
     * was requested.
     *
     * @return The {@link Collection} of {@link DataCollectionItem} value of
     *         {@link #currentOrdering}. Can't be null or empty.
     */
    public List<DataCollectionItem> getCurrentOrdering() {
        return currentOrdering;
    }

    /**
     * Getter for the ordering of the collection when the {@link ReorderAction}
     * was requested.
     *
     * @param currentOrdering The new {@link Collection} of
     *        {@link DataCollectionItem} value of {@link #currentOrdering}.
     *        Can't be null or empty.
     */
    private void setCurrentOrdering(List<DataCollectionItem> currentOrdering) {
        if (currentOrdering == null || currentOrdering.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'currentOrdering' cannot be null or empty.");
        }

        this.currentOrdering = currentOrdering;
    }
}
