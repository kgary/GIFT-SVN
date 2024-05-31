/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import mil.arl.gift.common.DisplayCourseInitInstructionsRequest;

/**
 * Properties for course initialization instructions widget.
 * 
 * @author nroberts
 */
public class CourseInitInstructionsWidgetProperties {
	
	/** Property used to get and set the display course initialization instructions request */
	private static final String DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST_PROPERTY = "DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST_PROPERTY";
	
	/**
	 * Gets the display course initialization instructions request from the specified widget properties
	 * 
	 * @param properties the widget properties from which to get the request
	 * @return the display course initialization instructions request
	 */
	public static DisplayCourseInitInstructionsRequest getDisplayCourseInitInstructionsRequest(WidgetProperties properties){
		return (DisplayCourseInitInstructionsRequest) properties.getPropertyValue(DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST_PROPERTY);
	}
	
	/**
	 * Sets the display course initialization instructions request for the specified widget properties.
	 * 
	 * @param properties the widget properties to which the request should be assigned
	 * @param request the display course initialization instructions request
	 */
	public static void setDisplayCourseInitInstructionsRequest(WidgetProperties properties, DisplayCourseInitInstructionsRequest request){
		properties.setPropertyValue(DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST_PROPERTY, request);
	}
}
