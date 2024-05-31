/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.util.ArrayList;

import mil.arl.gift.tutor.shared.data.GwtDomainOption;

/**
 * Properties for the select domain widget
 *
 * @author jleonard
 */
public class SelectDomainWidgetProperties {

    private final static String ACTIVE_DOMAIN_SESSION_NAME_PROPERTY = "ACTIVE_DOMAIN_SESSION_NAME";

    private final static String RETURN_TO_DOMAIN_SESSION_ON_EXIT_PROPERTY = "RETURN_TO_DOMAIN_SESSION_ON_EXIT";

    private final static String DOMAIN_OPTIONS_PROPERTY = "SELECT_DOMAIN_OPTIONS";

    /**
     * Gets the name of the active domain session, if one exists
     *
     * @param properties The properties for a select domain widget
     * @return String The name of the active domain session, null if there is
     * none
     */
    public static String getActiveDomainSessionName(WidgetProperties properties) {
        return properties.getStringPropertyValue(ACTIVE_DOMAIN_SESSION_NAME_PROPERTY);
    }

    /**
     * Sets the name of the active domain session
     *
     * @param properties The properties for a select domain widget
     * @param activeDomainSessionName The name of the active domain session,
     * null if there is none
     */
    public static void setActiveDomainSessionName(WidgetProperties properties, String activeDomainSessionName) {
        properties.setPropertyValue(ACTIVE_DOMAIN_SESSION_NAME_PROPERTY, activeDomainSessionName);
    }

    /**
     * Gets if the domain session should be displayed if no action is taken in
     * the widget
     *
     * @param properties The properties for a select domain widget
     * @return boolean If the domain session should be displayed if no action is
     * taken
     */
    public static boolean getReturnToDomainSessionOnExit(WidgetProperties properties) {
        Boolean returnTo = properties.getBooleanPropertyValue(RETURN_TO_DOMAIN_SESSION_ON_EXIT_PROPERTY);
        return returnTo != null ? returnTo : false;
    }

    /**
     * Sets if the domain session should be displayed if no action is taken in
     * the widget
     *
     * @param properties The properties for a select domain widget
     * @param returnTo If the domain session should be displayed if no action is
     * taken
     */
    public static void setReturnToDomainSessionOnExit(WidgetProperties properties, boolean returnTo) {
        properties.setPropertyValue(RETURN_TO_DOMAIN_SESSION_ON_EXIT_PROPERTY, returnTo);
    }

    /**
     * Gets the domain options for the user
     *
     * @param properties The properties for a select domain widget
     * @return ArrayList<GwtDomainOption> The list of domain options for the
     * user
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<GwtDomainOption> getOptions(WidgetProperties properties) {
        return (ArrayList<GwtDomainOption>) properties.getPropertyValue(DOMAIN_OPTIONS_PROPERTY);
    }

    /**
     * Sets the domain options for the user
     *
     * @param properties The properties for a select domain widget
     * @param options The list of domain options for the user
     */
    public static void setOptions(WidgetProperties properties, ArrayList<GwtDomainOption> options) {
        properties.setPropertyValue(DOMAIN_OPTIONS_PROPERTY, options);
    }
}
