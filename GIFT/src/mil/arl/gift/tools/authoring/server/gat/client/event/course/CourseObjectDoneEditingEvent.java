/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event fired whenever the last course object is done being edited 
 * 
 * @author cpadilla
 *
 */
public class CourseObjectDoneEditingEvent extends GenericEvent {

	/**
	 * Instantiates an event fired whenever a course object is done being edited
	 * 
	 * @param courseObject the course object that was edited
	 */
	public CourseObjectDoneEditingEvent(){
	}
}
