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
 * An event fired whenever a course object needs to be redrawn by widgets outside of its editor.
 * This is helpful for when the course object is changed in some meaningful way that would alter
 * the appearance of the object somehow, such as changing a training application's type.
 * 
 * @author nroberts
 */
public class CourseObjectRedrawEvent extends GenericEvent {
	
    /** the course object that needs to be redrawn */
	private Serializable courseObject;

	/**
	 * Instantiates an event fired whenever a course object needs to be redrawn
	 * 
	 * @param courseObject the course object that needs to be redrawn. Cannot be null.
	 */
	public CourseObjectRedrawEvent(Serializable courseObject){
	    if(courseObject == null) {
	        throw new IllegalArgumentException("The course object to redraw cannot be null.");
	    }
	    
	    this.courseObject = courseObject;
	}

	/**
	 * Gets the course object that needs to be redrawn
	 * 
	 * @return the course object that needs to be redrawn. Will not be null.
	 */
	public Serializable getCourseObject() {
		return courseObject;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseObjectRedrawEvent: courseObject = ");
        builder.append(courseObject);
        builder.append("]");
        return builder.toString();
    }
	
	
}
