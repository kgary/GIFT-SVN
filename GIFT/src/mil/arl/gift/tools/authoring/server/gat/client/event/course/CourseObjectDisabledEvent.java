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
 * An event fired whenever a course object is marked as disabled or enabled
 * 
 * @author sharrison
 */
public class CourseObjectDisabledEvent extends GenericEvent {
	
	private Serializable courseObject;

	/**
	 * Instantiates an event fired whenever a course object is marked as disabled or enabled
	 * 
	 * @param courseObject the course object being marked as disabled or enabled
	 */
	public CourseObjectDisabledEvent(Serializable courseObject){
		this.courseObject = courseObject;
	}

	/**
	 * Gets the course object being marked as disabled or enabled
	 * 
	 * @return the course object being marked as disabled or enabled
	 */
	public Serializable getCourseObject() {
		return courseObject;
	}
}
