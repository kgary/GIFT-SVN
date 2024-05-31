/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.constants.IconType;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.LearnerAction;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Scenario;
import generated.dkf.Scenario.EndTriggers;
import generated.dkf.Strategy;
import generated.dkf.Task;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import generated.dkf.TeamOrganization;
import mil.arl.gift.common.enums.ScenarioObjectName;
import mil.arl.gift.common.util.StringUtils;

/**
 * A class used to perform common operations with scenario elements, such as retrieving display names to be used in user interfaces.
 * 
 * @author nroberts
 */
public class ScenarioElementUtil {
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ScenarioElementUtil.class.getName());

    /**
     * An object containing data related to displaying scenario element information
     * 
     * @author nroberts
     */
    public static class ElementDisplayData{
        
        /** The element's display name */
        private ScenarioObjectName name;
        
        /** The element's associated icon */
        private IconType icon;
        
        /**
         * Creates a new set of display data with the given display name and icon
         * 
         * @param name the display name for this display data
         * @param icon the icon for this display data
         */
        public ElementDisplayData(ScenarioObjectName name, IconType icon) {
            this.name = name;
            this.icon = icon;
        }
        
        /**
         * Gets the display name
         * 
         * @return the display name
         */
        public ScenarioObjectName getName(){
            return name;
        }
        
        /**
         * Gets the display icon
         * 
         * @return the display icon
         */
        public IconType getIcon() {
            return icon;
        }
    }

    /** A mapping from each course element type to its associated display name */
    private final static Map<Class<?>, ElementDisplayData> ELEMENT_CLASS_TO_DISPLAY_NAME = new HashMap<>();
    
    /** The placeholder name for Serializable objects whose names are loaded later. */
    private static final String PLACEHOLDER_NAME = "Unknown";
    
    static{
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(Task.class, new ElementDisplayData(
                ScenarioObjectName.TASK, 
                IconType.GAVEL)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(Concept.class, new ElementDisplayData(
                ScenarioObjectName.CONCEPT, 
                IconType.LIGHTBULB_O)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(Condition.class, new ElementDisplayData(
                ScenarioObjectName.CONDITION, 
                IconType.COGS)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(StateTransition.class, new ElementDisplayData(
                ScenarioObjectName.STATE_TRANSITION, 
                IconType.BELL)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(Strategy.class, new ElementDisplayData(
                ScenarioObjectName.STRATEGY, 
                IconType.BOLT)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(PlacesOfInterest.class, new ElementDisplayData(
                ScenarioObjectName.PLACES_OF_INTEREST,
                IconType.MAP_PIN)
        );

        ELEMENT_CLASS_TO_DISPLAY_NAME.put(EndTriggers.class, new ElementDisplayData(
                ScenarioObjectName.SCENARIO_END_TRIGGERS,
                IconType.BAN)
        );

        ELEMENT_CLASS_TO_DISPLAY_NAME.put(AvailableLearnerActions.class, new ElementDisplayData(
                ScenarioObjectName.LEARNER_ACTIONS,
                IconType.TASKS)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(LearnerAction.class, new ElementDisplayData(
                ScenarioObjectName.LEARNER_ACTIONS,
                IconType.TASKS)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(TeamOrganization.class, new ElementDisplayData(
                ScenarioObjectName.TEAM_ORGANIZATION,
                IconType.USERS)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(Team.class, new ElementDisplayData(
                ScenarioObjectName.TEAM,
                IconType.USERS)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(TeamMember.class, new ElementDisplayData(
                ScenarioObjectName.TEAM_MEMBER,
                IconType.USER)
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(Scenario.class, new ElementDisplayData(
                ScenarioObjectName.MISCELLANEOUS,
                IconType.ELLIPSIS_H)
        );
    }
    
    /**
     * Gets the scenario for the given course element type
     * 
     * @param scenarioElementClass the scenario element type
     * @return the scenario corresponding to the type
     */
    public static IconType getIconFromType(Class<?> scenarioElementClass) {
        
        ElementDisplayData name = ELEMENT_CLASS_TO_DISPLAY_NAME.get(scenarioElementClass);
        
        if (name == null) {
            throw new IllegalArgumentException("getIconFromType() cannot find the class '"+scenarioElementClass.getSimpleName()+"'.");            
        }
        
        return name.getIcon();
    }
    
    /**
     * Gets the icon for the given scenario element's type
     * 
     * @param scenarioElement the scenario element from which to get the type
     * @return the icon corresponding to the scenario element's type
     */
    public static IconType getTypeIcon(Serializable scenarioElement) {
        return getIconFromType(scenarioElement.getClass());
    }
    
    /**
     * Gets the name that has been authored for the given scenario object
     * 
     * @param scenarioObject the scenario object
     * @return the name that was authored for the given object.  Can be null or empty because for some
     * objects based on how they are defined in the DKF schema. E.g. state transition names are optional.
     * @throws IllegalArgumentException if the object given as a scenario object
     *         is not a known scenario object.
     */
    public static String getObjectName(Serializable scenarioObject) throws IllegalArgumentException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getObjectName(" + scenarioObject + ")");
        }
        
        if (scenarioObject instanceof Task) {
            return ((Task) scenarioObject).getName();
        } else if (scenarioObject instanceof Concept) {
            return ((Concept) scenarioObject).getName();
        } else if (scenarioObject instanceof Condition) {
            /* use a placeholder name for now, once the condition tab is built the condition display
             * name will be retrieved and applied to the name label(s). */
            return PLACEHOLDER_NAME;
        } else if (scenarioObject instanceof StateTransition) {
            return ((StateTransition) scenarioObject).getName();
        } else if (scenarioObject instanceof Strategy) {
            return ((Strategy) scenarioObject).getName();
        } else if (scenarioObject instanceof PlacesOfInterest) {
            return ScenarioObjectName.PLACES_OF_INTEREST.getDisplayName();
        } else if (scenarioObject instanceof AvailableLearnerActions) {
            return ScenarioObjectName.LEARNER_ACTIONS.getDisplayName();
        } else if(scenarioObject instanceof LearnerAction){
            // don't display the learner action display name on the editor tab since all learner actions
            // are currently under the generic learner actions tab
            return ScenarioObjectName.LEARNER_ACTIONS.getDisplayName();
        } else if (scenarioObject instanceof Scenario.EndTriggers) {
            return ScenarioObjectName.SCENARIO_END_TRIGGERS.getDisplayName();
        } else if (scenarioObject instanceof TeamOrganization) {
            return ScenarioObjectName.TEAM_ORGANIZATION.getDisplayName();
        } else if (scenarioObject instanceof Team) {
            return ((Team) scenarioObject).getName();
        } else if (scenarioObject instanceof TeamMember) {
            return ((TeamMember) scenarioObject).getName();
        } else if (scenarioObject instanceof Scenario) {
            return ScenarioObjectName.MISCELLANEOUS.getDisplayName();
        } else {
            String message = "There was an error getting the name of the real time assessment object";
            String details = (scenarioObject == null ? "A real time assessment object in the course is null." : 
                    "The real time assessment object (type: " + scenarioObject.getClass() +") is not handled.");
            
            throw new IllegalArgumentException(message + " " + details);
        }
    }

    /**
     * Sets the name of the Object from the Serializable object
     *
     * @param object the Serializable object to cast
     * @param name the name to set the Object to
     * @throws UnsupportedOperationException if the Serializable cannot be cast
     *         to a Scenario Object
     */
    public static void setObjectName(Serializable object, String name)
            throws UnsupportedOperationException {

        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Cannot set a blank scenario object name.");
        }

        // ensure a trimmed name
        name = name.trim();
        if (object instanceof Task) {
            ((Task) object).setName(name);
        } else if (object instanceof Concept) {
            ((Concept) object).setName(name);
        } else if (object instanceof StateTransition) {
            ((StateTransition) object).setName(name);
        } else if (object instanceof Strategy) {
            ((Strategy) object).setName(name);
        } else if (object instanceof Team) {
            ((Team) object).setName(name);
        } else if (object instanceof TeamMember) {
            ((TeamMember) object).setName(name);
        } else {
            throw new UnsupportedOperationException(
                    "Failed to set the name of object: " + object + ". Unrecognized type.");
        }
    }

    /**
     * Determines if the provided scenario object is an expected assessment object.
     * 
     * @param scenarioObject the object to test.
     * @return true if the object is an assessment object; false otherwise.
     */
    public static boolean isObjectAnAssessmentObject(Serializable scenarioObject) {
        try {
            // use this method because it will check all the instanceofs.
            getObjectName(scenarioObject);
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}