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
 * An event fired whenever a course object is renamed
 * 
 * @author nroberts
 */
public class CourseObjectRenamedEvent extends GenericEvent {
	
    /** the course object that was renamed */
	private Serializable courseObject;
	
	/** the previous name of the course object. */
	private String oldName;

	/**
	 * Instantiates an event fired whenever a course object is being renamed
	 * 
	 * @param oldName the previous name of the course object.
	 * @param courseObject the course object that was renamed
	 */
	public CourseObjectRenamedEvent(String oldName, Serializable courseObject){
		this.oldName = oldName;
	    this.courseObject = courseObject;
	}

	/**
	 * Gets the course object being renamed
	 * 
	 * @return the course object that was renamed
	 */
	public Serializable getCourseObject() {
		return courseObject;
	}
	
	/**
	 * Return the previous name of the course object.
	 * 
	 * @return the previous name
	 */
	public String getPreviousName(){
	    return oldName;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseObjectRenamedEvent: courseObject = ");
        builder.append(courseObject);
        builder.append(", oldName = ");
        builder.append(oldName);
        builder.append("]");
        return builder.toString();
    }
	
	
}
