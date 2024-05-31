/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.io.Serializable;

import mil.arl.gift.common.DisplayMessageTutorRequest;

/**
 * Handles logic for setting Widget Properties specifically for a message Widget instance.
 * @author jleonard
 */
public class DisplayMessageWidgetProperties extends DisplayContentWidgetProperties implements Serializable {
    
	private static final long serialVersionUID = 1L;
	/* Property names */
    private final static String IS_EMPTY_REQUEST = "IS_EMPTY_REQUEST"; 
    
    /**
     * Gets the text message to display in the widget
     *
     * @param properties The properties for a display text widget
     * @return String The text message to display
     */
    public static DisplayMessageTutorRequest getParameters(WidgetProperties properties) {
        return getMessageParameters(properties);
    }
    
    /**
     * Sets whether or not the guidance request is empty. If the request is empty, the TUI will be cleared.
     * 
     * @param properties The properties for the display text widget
     * @param isEmptyRequest true if the DisplayGuidanceTutorRequest is an empty request, false otherwise
     */
    public static void setEmptyRequest(WidgetProperties properties, boolean isEmptyRequest) {
    	properties.setPropertyValue(IS_EMPTY_REQUEST, isEmptyRequest);
    }
    
    /**
     * Gets whether or not the guidance request is empty.
     * 
     * @param properties The properties for the display text widget
     * @return true if the DisplayGuidanceTutorRequest is an empty request, false otherwise
     */
    public static Boolean isEmptyRequest(WidgetProperties properties) {
    	 Boolean isEmptyRequest = properties.getBooleanPropertyValue(IS_EMPTY_REQUEST);
         return isEmptyRequest == null ? false : isEmptyRequest;
    }

    private DisplayMessageWidgetProperties() {
    	super();
    }
}
