/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import java.io.Serializable;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event fired whenever a course object is opened for editing
 * 
 * @author nroberts
 */
public class CourseObjectOpenedForEditingEvent extends GenericEvent {
	
	private Serializable courseObject;

	/**
	 * Instantiates an event fired whenever a course object is opened for editing
	 * 
	 * @param courseObject the course object that was opened
	 */
	public CourseObjectOpenedForEditingEvent(Serializable courseObject){
		this.courseObject = courseObject;
	}

	/**
	 * Gets the course object that was opened
	 * 
	 * @return the course object that was opened
	 */
	public Serializable getCourseObject() {
		return courseObject;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseObjectOpenedForEditingEvent: courseObject = ");
        builder.append(courseObject);
        builder.append("]");
        return builder.toString();
    }
	
	
}
