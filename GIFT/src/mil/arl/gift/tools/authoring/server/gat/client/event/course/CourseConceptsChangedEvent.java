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
 * An event fired whenever a course's concepts are changed
 * 
 * @author nroberts
 */
public class CourseConceptsChangedEvent extends GenericEvent {
	
	private Course course;

	/**
	 * Instantiates an event fired whenever a course's concepts are changed
	 * 
	 * @param course the course whose concepts were changed
	 */
	public CourseConceptsChangedEvent(Course course){
		this.course = course;
	}

	/**
	 * Gets the course whose concepts were changed
	 * 
	 * @return the the cours
	 */
	public Course getCourse() {
		return course;
	}
}
