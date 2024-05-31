/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets;

import mil.arl.gift.tools.authoring.server.gat.shared.wrap.TrainingApplicationObject;

/**
 * Wraps a course object to allow for a 'nullable' value.
 *
 * @author sharrison
 *
 */
public class CourseObjectWrapper {
    /** The object that is being wrapped. */
    private TrainingApplicationObject courseObject = null;

    /**
     * Creates a wrapper without an object to wrap.
     */
    public CourseObjectWrapper() {

    }

    /**
     * Creates a wrapper around a given course object.
     *
     * @param courseObject The course object to wrap.
     */
    public CourseObjectWrapper(TrainingApplicationObject courseObject) {
        setCourseObject(courseObject);
    }

    /**
     * Sets the wrapped course object.
     *
     * @param courseObject The new course object to wrap. Can be null.
     */
    public void setCourseObject(final TrainingApplicationObject courseObject) {
        this.courseObject = courseObject;
    }

    /**
     * Gets the wrapped course object.
     *
     * @return The wrapped course object. Can be null.
     */
    public TrainingApplicationObject getCourseObject() {
        return courseObject;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[CourseObjectWrapper: ");
        sb.append("course object = ").append(getCourseObject());
        sb.append("]");
        return sb.toString();
    }
}