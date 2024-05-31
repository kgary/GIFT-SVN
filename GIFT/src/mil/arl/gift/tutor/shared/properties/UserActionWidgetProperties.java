/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.util.ArrayList;

import mil.arl.gift.tutor.shared.ScenarioControls;
import mil.arl.gift.tutor.shared.UserAction;

/**
 * Properties for the user action widget
 *
 * @author jleonard
 */
public class UserActionWidgetProperties {

    private final static String USER_ACTIONS_PROPERTY = "USER_ACTIONS";

    private final static String USER_ACTIONS_TAKEN_PROPERTY = "USER_ACTIONS_TAKEN";

    private final static String USE_PREVIOUS_ACTIONS = "USE_PREVIOUS_ACTIONS";
    
    private final static String SCENARIO_CONTROLS_PROPERTY = "SCENARIO_CONTROLS";
    
    /**
     * Gets the user actions the user can take
     *
     * @param properties The properties for a user action widget
     * @return ArrayList<UserAction> The user actions the user can take
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<UserAction> getUserActions(WidgetProperties properties) {
        return (ArrayList<UserAction>) properties.getPropertyValue(USER_ACTIONS_PROPERTY);
    }

    /**
     * Sets the user actions the user can take
     *
     * @param properties The properties for a user action widget
     * @param options The user actions the user can take
     */
    public static void setUserActions(WidgetProperties properties, ArrayList<UserAction> options) {
        properties.setPropertyValue(USER_ACTIONS_PROPERTY, options);
    }

    /**
     * Gets the user actions the user has taken
     *
     * @param properties The properties for a user action widget
     * @return ArrayList<UserAction> The user actions that have been taken
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<UserAction> getUserActionsTaken(WidgetProperties properties) {
        return (ArrayList<UserAction>) properties.getPropertyValue(USER_ACTIONS_TAKEN_PROPERTY);
    }

    /**
     * Sets the user actions the user has taken
     *
     * @param properties The properties for a user action widget
     * @param options The user actions that have been taken
     */
    public static void setUserActionsTaken(WidgetProperties properties, ArrayList<UserAction> options) {
        properties.setPropertyValue(USER_ACTIONS_TAKEN_PROPERTY, options);
    }
    
    /**
     * Gets whether or not the previous user actions should be displayed
     * 
     * @param properties The properties for the user action widget
     * @return True if the previous user actions should be reused, false otherwise
     */
    public static boolean shouldUsePreviousActions(WidgetProperties properties) {
    	Boolean usePreviousActions = properties.getBooleanPropertyValue(USE_PREVIOUS_ACTIONS);
    	return usePreviousActions == null ? false : usePreviousActions;
    }
    
    /**
     * Sets whether or not the previous user actions should be displayed
     * 
     * @param properties The properties for the user action widget
     * @param usePreviousActions True if the previous user actions should be reused, false otherwise
     */
    public static void setShouldUsePreviousActions(WidgetProperties properties, boolean usePreviousActions) {
    	properties.setPropertyValue(USE_PREVIOUS_ACTIONS, usePreviousActions);
    }
    
    /**
     * Gets the scenario controls to make available
     *
     * @param properties The properties for a user action widget
     * @return ArrayList<UserAction> The user actions the user can take
     */
    public static ScenarioControls getScenarioControls(WidgetProperties properties) {
        
        if(properties.getPropertyValue(SCENARIO_CONTROLS_PROPERTY) != null) {
            return (ScenarioControls) properties.getPropertyValue(SCENARIO_CONTROLS_PROPERTY);
            
        } else {
            return null;
        }
    }

    /**
     * Sets the scenario controls to make available
     *
     * @param properties The properties for a user action widget
     * @param controls The scenario controls to make available
     */
    public static void setScenarioControls(WidgetProperties properties, ScenarioControls controls) {
        properties.setPropertyValue(SCENARIO_CONTROLS_PROPERTY, controls);
    }
}
