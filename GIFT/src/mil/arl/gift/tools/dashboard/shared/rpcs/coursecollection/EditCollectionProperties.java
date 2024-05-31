/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection;

import static mil.arl.gift.common.util.StringUtils.isBlank;

/**
 * The {@link AbstractCourseCollectionAction} that is used to update properties
 * for a {@link CourseCollection} such as name and description.
 *
 * @author tflowers
 *
 */
public class EditCollectionProperties extends AbstractCourseCollectionAction {

    /** The version used by the serialization logic */
    private static final long serialVersionUID = 1L;

    /** The new name to apply to the {@link CourseCollection} */
    private String name;

    /** The new description to apply to the {@link CourseCollection} */
    private String description;

    /**
     * No argument constructor for GWT serialization.
     */
    @SuppressWarnings("unused")
    private EditCollectionProperties() {
    }

    /**
     * Constructs a new {@link EditCollectionProperties} with all the necessary
     * parameters.
     *
     * @param username The name of the user performing the action. Used for
     *        validation purposes. Can't be null.
     * @param collectionId The id of the {@link CourseCollection} upon which
     *        this action is being performed.
     * @param name The new name of the {@link CourseCollection}. Can't be blank.
     * @param description The new description of the {@link CourseCollection}.
     *        Can be null to indicate the description should be erased.
     */
    public EditCollectionProperties(String username, String collectionId, String name, String description) {
        super(username, collectionId);
        setName(name);
        setDescription(description);
    }

    /**
     * Getter for the new name of the {@link CourseCollection}.
     *
     * @return The {@link String} value of {@link #name}. Can't be blank.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the new name of the {@link CourseCollection}.
     *
     * @param name The new {@link String} value of {@link #name}. Can't be
     *        blank.
     */
    private void setName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("The parameter 'name' cannot be blank.");
        }

        this.name = name;
    }

    /**
     * Getter for the new description of the {@link CourseCollection}.
     *
     * @return The {@link String} value of {@link #description}. Can be null.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for the new description of the {@link CourseCollection}.
     *
     * @param description The new {@link String} value of {@link #description}.
     *        Can be null.
     */
    private void setDescription(String description) {
        this.description = description;
    }
}