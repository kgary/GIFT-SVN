/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import generated.course.SlideShowProperties;
import mil.arl.gift.common.DisplayMediaTutorRequest;

/**
 * Wrapper for a widget's properties to get/set display media specific properties
 *
 * @author jleonard
 */
public class DisplayMediaWidgetProperties extends DisplayContentWidgetProperties {

    /* Property names */
    private final static String LOAD_PROGRESS = "LOAD_PROGRESS";
    
    /**
     * Gets the text message to display in the widget
     *
     * @param properties The widget properties
     * @return String The text message to display
     */
    public static DisplayMediaTutorRequest getParameters(WidgetProperties properties) {
        return getMediaParameters(properties);
    }

    /**
     * Gets whether or not the media type is a slide show
     * 
     * @param properties The widget properties
     * @return true if the media is a slide show, false otherwise
     */
    public static boolean isSlideShowItem(WidgetProperties properties) {
    	if(getMediaParameters(properties).getMedia() != null) {
    		return getMediaParameters(properties).getMedia().getMediaTypeProperties() instanceof SlideShowProperties;
    	}
    	
    	return false;
    }
    /**
     * Gets the percent complete of loading content in the training application
     *
     * @param properties The widget properties
     * @return the load progress as a percent of complete
     */
    public static Integer getLoadProgress(WidgetProperties properties) {
        Integer loadProgress = properties.getIntegerPropertyValue(LOAD_PROGRESS);
        return loadProgress;
    }

    /**
     * Sets the percent complete of loading content in the training application
     *
     * @param properties The widget properties
     * @param loadProgress the percent complete of loading content
     */
    public static void setLoadProgress(WidgetProperties properties, int loadProgress) {
        properties.setPropertyValue(LOAD_PROGRESS, loadProgress);
    }

}
