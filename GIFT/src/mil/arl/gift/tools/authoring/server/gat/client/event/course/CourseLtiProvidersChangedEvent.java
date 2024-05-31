/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import generated.course.Course;

/**
 * An event fired whenever a course's LTI providers are changed
 * 
 * @author sharrison
 */
public class CourseLtiProvidersChangedEvent extends GenericEvent {
	
    /** The course object that contains the modified LTI providers */
	private Course course;

	/**
	 * Instantiates an event fired whenever a course's LTI providers are changed
	 * 
	 * @param course the course whose LTI providers were changed
	 */
	public CourseLtiProvidersChangedEvent(Course course){
		this.course = course;
	}

	/**
	 * Gets the course whose LTI providers were changed
	 * 
	 * @return the course
	 */
	public Course getCourse() {
		return course;
	}
}
