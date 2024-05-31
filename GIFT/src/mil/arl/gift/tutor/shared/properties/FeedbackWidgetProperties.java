/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.TutorUserInterfaceFeedback;

/**
 * Properties for the feedback widget
 *
 * @author jleonard
 */
public class FeedbackWidgetProperties {

    private final static String HAS_NEW_FEEDBACK_PROPERTY = "HAS_NEW_FEEDBACK_PROPERTY";

    private final static String FEEDBACK_PROPERTY = "FEEDBACK";

    private final static String UPDATE_COUNT = "UPDATE_COUNT";    
    
    /** the key to the property for whether older feedback in the feedback widget 
     * should be styled differently than new feedback */
    private final static String OLD_FEEDBACK_STYLE_ENABLED = "OLD_FEEDBACK_STYLE_ENABLED";
    
    /**
     * Gets if there is new feedback being displayed
     *
     * @param properties The widget properties
     * @return boolean If there is new feedback being displayed
     */
    public static boolean hasNewFeedback(WidgetProperties properties) {
        Boolean value = properties.getBooleanPropertyValue(HAS_NEW_FEEDBACK_PROPERTY);
        return value != null ? value : false;
    }

    /**
     * Sets if there is new feedback being displayed
     *
     * @param properties The widget properties
     * @param newFeedbackCount If there is new feedback being displayed
     */
    public static void setHasNewFeedback(WidgetProperties properties, boolean newFeedbackCount) {
        properties.setPropertyValue(HAS_NEW_FEEDBACK_PROPERTY, newFeedbackCount);
    }

    /**
     * Gets the feedback to display in the widget
     *
     * @param properties The widget properties
     * @return ArrayList<String> The feedback to display
     */
    @SuppressWarnings(value = "unchecked")
    public static ArrayList<TutorUserInterfaceFeedback> getFeedback(WidgetProperties properties) {
        return (ArrayList<TutorUserInterfaceFeedback>) properties.getPropertyValue(FEEDBACK_PROPERTY);
    }

    /**
     * Sets the feedback to display in the widget
     *
     * @param properties The widget properties
     * @param feedback The feedback to display
     */
    public static void setFeedback(WidgetProperties properties, List<TutorUserInterfaceFeedback> feedback) {
        properties.setPropertyValue(FEEDBACK_PROPERTY, new ArrayList<TutorUserInterfaceFeedback>(feedback));
    }
    
    /**
     * Gets the number of available updates.
     *
     * @param properties The widget properties
     * @return the number of updates available.
     */
    public static int getUpdateCount(WidgetProperties properties) {
        return (properties.getIntegerPropertyValue(UPDATE_COUNT) == null) ? 0 : properties.getIntegerPropertyValue(UPDATE_COUNT);
    }

    /**
     * Sets the number of available updates.
     *
     * @param properties The widget properties
     * @param updateCount the number of updates available.
     */
    public static void setUpdateCount(WidgetProperties properties, int updateCount) {
        properties.setPropertyValue(UPDATE_COUNT, updateCount);
    }
    
    
    /**
     * Set whether the old feedback style should be different than the new feedback style in the
     * feedback widget.
     * @param enabled true if the style should be different
     */
    public static void setOldFeedbackStyleEnabled(WidgetProperties properties, boolean enabled){
        properties.setPropertyValue(OLD_FEEDBACK_STYLE_ENABLED, enabled);
    }
    
    /**
     * Return whether the old feedback style should be different than the new feedback style in the
     * feedback widget.
     * @return true if the style should be different.  Default is false.
     */
    public static boolean getOldFeedbackStyleEnabled(WidgetProperties properties){
        Boolean enabled = properties.getBooleanPropertyValue(OLD_FEEDBACK_STYLE_ENABLED);
        return enabled != null ? enabled : false;
    }
}
