/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import generated.course.Course;
import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event fired whenever a course is loaded.
 * 
 * @author nroberts
 */
public class CourseLoadedEvent extends GenericEvent {
	
	private Course course;

	/**
	 * Instantiates an event fired whenever a course is loaded.
	 * 
	 * @param course the course that was loaded
	 */
	public CourseLoadedEvent(Course course){
		this.course = course;
	}

	/**
	 * Gets the course overview that was clicked on.
	 * 
	 * @return the overview
	 */
	public Course getCourse() {
		return course;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseLoadedEvent: ]");
        return builder.toString();
    }
	
	
}
