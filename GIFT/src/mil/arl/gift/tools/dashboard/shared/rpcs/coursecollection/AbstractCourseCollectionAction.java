/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection;

import static mil.arl.gift.common.util.StringUtils.isBlank;

import java.io.Serializable;

/**
 * Represents an RPC action that is invoked to edit an existing
 * {@link CourseCollection}.
 *
 * @author tflowers
 *
 */
public abstract class AbstractCourseCollectionAction implements Serializable {

    /** The version of the class used by the serialization logic */
    private static final long serialVersionUID = 1L;

    /** The name of the user requesting for the action to be executed. */
    private String username;

    /** The id of the collection that is being operated upon */
    private String collectionId;

    /**
     * No argument constructor for GWT serialization.
     */
    protected AbstractCourseCollectionAction() {
    }

    /**
     * Constructs an {@link AbstractCourseCollectionAction} that is being
     * performed by a specified user on a specified collection.
     *
     * @param username The name of the user who is performing the action. Can't
     *        be blank.
     * @param collectionId The id of the collection upon which the action is
     *        being performed. Can't be blank.
     */
    public AbstractCourseCollectionAction(String username, String collectionId) {
        this();
        setUsername(username);
        setCollectionId(collectionId);
    }

    /**
     * Getter for the name of the user who is performing the action.
     *
     * @return The {@link String} value of {@link #username}. Can't be blank.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for the name of the user who is performing the action.
     *
     * @param username The new {@link String} value of {@link #username}. Can't
     *        be blank.
     */
    private void setUsername(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        this.username = username;
    }

    /**
     * Getter for the id of the collection upon which the action is operating.
     *
     * @return The {@link String} value of {@link #collectionId}. Can't be null.
     */
    public String getCollectionId() {
        return collectionId;
    }

    /**
     * Setter for the id of the collection upon which the action is operating.
     *
     * @param collectionId The new {@link String} value of
     *        {@link #collectionId}. Can't be null.
     */
    private void setCollectionId(String collectionId) {
        if (collectionId == null) {
            throw new IllegalArgumentException("The parameter 'collectionId' cannot be null.");
        }

        this.collectionId = collectionId;
    }
}
