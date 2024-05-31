/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection;

import mil.arl.gift.common.experiment.DataCollectionItem;

/**
 * An {@link AbstractCourseCollectionAction} which requests that a new
 * {@link DataCollectionItem} be added to a {@link CourseCollection}.
 *
 * @author tflowers
 *
 */
public class AddCourseAction extends AbstractCourseCollectionAction {

    /** The version of the class used by serialization logic */
    private static final long serialVersionUID = 1L;

    /** The course that should be added to the collection. */
    private DataCollectionItem newCourse;

    /**
     * No argument constructor for GWT serialization.
     */
    @SuppressWarnings("unused")
    private AddCourseAction() {
    }

    /**
     * Constructs an {@link AddCourseAction} that adds a
     * {@link DataCollectionItem} to a specific {@link CourseCollection}.
     *
     * @param username The name of the user requesting the action be performed.
     *        Used for authentication purposes. Can't be blank.
     * @param collectionId The id of the {@link CourseCollection} to which the
     *        {@link DataCollectionItem} item should be added. Can't be blank.
     * @param newCourse The {@link DataCollectionItem} to add to the specified
     *        {@link CourseCollection}. The value should not yet exist in the
     *        database. Can't be null.
     */
    public AddCourseAction(String username, String collectionId, DataCollectionItem newCourse) {
        super(username, collectionId);
        setNewCourse(newCourse);
    }

    /**
     * Getter for the {@link DataCollectionItem} which is to be added to the
     * specified {@link CourseCollection}.
     *
     * @return The {@link DataCollectionItem} value of {@link #newCourse}. Can't
     *         be null.
     */
    public DataCollectionItem getNewCourse() {
        return newCourse;
    }

    /**
     * Setter for the {@link DataCollectionItem} which is to be added to the
     * specified {@link CourseCollection}.
     *
     * @param newCourse The new {@link DataCollectionItem} value of
     *        {@link #newCourse}. Can't be null.
     */
    private void setNewCourse(DataCollectionItem newCourse) {
        if (newCourse == null) {
            throw new IllegalArgumentException("The parameter 'newCourse' cannot be null.");
        }

        this.newCourse = newCourse;
    }
}
