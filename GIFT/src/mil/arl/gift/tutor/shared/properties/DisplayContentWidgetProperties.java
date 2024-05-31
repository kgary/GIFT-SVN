/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.AbstractDisplayContentTutorRequest;
import mil.arl.gift.common.DisplayMediaTutorRequest;
import mil.arl.gift.common.DisplayMessageTutorRequest;

/**
 *
 * @author jleonard
 */
public class DisplayContentWidgetProperties implements IsSerializable {
    
    /* Property names */
    private final static String MESSAGE_PARAMETERS = "MESSAGE_PARAMETERS"; 
    private final static String MEDIA_PARAMETERS = "MEDIA_PARAMETERS"; 
    private final static String HAS_CONTINUE_BUTTON_PROPERTY = "HAS_CONTINUE_BUTTON_PROPERTY";
    private final static String DISPLAY_DURATION_PROPERTY = "DISPLAY_DURATION_PROPERTY";

    /**
     * Gets the text message to display in the widget
     *
     * @param properties The properties for a display text widget
     * @return String The text message to display
     */
    public static DisplayMessageTutorRequest getMessageParameters(WidgetProperties properties) {

        return (DisplayMessageTutorRequest) properties.getPropertyValue(MESSAGE_PARAMETERS);
    }

    /**
     * Gets the text message to display in the widget
     *
     * @param properties The properties for a display text widget
     * @return String The text message to display
     */
    public static DisplayMediaTutorRequest getMediaParameters(WidgetProperties properties) {

        return (DisplayMediaTutorRequest) properties.getPropertyValue(MEDIA_PARAMETERS);
    }
    
    /**
     * Sets the text message to display in the widget
     *
     * @param properties The properties for a display text widget
     * @param parameters guidance parameters containing information about the guidance content
     */
    public static void setParameters(WidgetProperties properties, AbstractDisplayContentTutorRequest parameters) {
    	
    	
    	if(parameters instanceof DisplayMediaTutorRequest) {
    		properties.setPropertyValue(MEDIA_PARAMETERS, parameters);
    		
    	} else {
    		properties.setPropertyValue(MESSAGE_PARAMETERS, parameters);
    		if(parameters == DisplayMessageTutorRequest.EMPTY_REQUEST) {
            	DisplayMessageWidgetProperties.setEmptyRequest(properties, true);
            	DisplayMessageWidgetProperties.setHasContinueButton(properties, false);
            	properties.setIsFullscreen(true);
    		}
    	}
    	
    	if (parameters.getDisplayDuration() > 0 || parameters.isWhileTrainingAppLoads()) {
    		DisplayContentWidgetProperties.setHasContinueButton(properties, false);
		}
    	
    	DisplayContentWidgetProperties.setDisplayDuration(properties, parameters.getDisplayDuration());
    }
    
    /**
     * Sets if the widget closes at user's discretion with the press of a
     * 'Continue' button
     *
     * @param properties The properties for a display text widget
     * @param hasContinueButton If the widget closes at user's discretion
     */
    public static void setHasContinueButton(WidgetProperties properties, boolean hasContinueButton) {
        properties.setPropertyValue(HAS_CONTINUE_BUTTON_PROPERTY, hasContinueButton);
    }
    
    /**
     * Gets if the widget closes at user's discretion with the press of a
     * 'Continue' button
     *
     * @param properties The properties for a display text widget
     * @return boolean If the widget closes at user's discretion
     */
    public static boolean getHasContinueButton(WidgetProperties properties) {
        Boolean hasContinueButton = properties.getBooleanPropertyValue(HAS_CONTINUE_BUTTON_PROPERTY);
        return hasContinueButton == null ? true : hasContinueButton;
    }
    
    /**
     * Sets how long the widget should be displayed before closing automatically
     * 
     * @param properties The properties for a display text widget
     * @param displayDuration How long the widget should be displayed
     */
    public static void setDisplayDuration(WidgetProperties properties, int displayDuration) {
        properties.setPropertyValue(DISPLAY_DURATION_PROPERTY, displayDuration);
    }
    
    /**
     * Gets how long the widget will be displayed before it closes automatically
     * 
     * @param properties The properties for a display text widget
     * @return How long the widget will be displayed
     */
    public static int getDisplayDuration(WidgetProperties properties) {
        Integer displayDuration = properties.getIntegerPropertyValue(DISPLAY_DURATION_PROPERTY);
        return displayDuration == null ? 0 : displayDuration;
    }
    
    protected DisplayContentWidgetProperties() {
    }
}
