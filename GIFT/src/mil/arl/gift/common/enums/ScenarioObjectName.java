/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

/**
 * This class contains definitions of all scenario object names. Used to prevent
 * needing to modify course transition names across multiple files. 
 * 
 * @author nroberts
 */
public enum ScenarioObjectName {
		
	    /** The display name for a {@link generated.dkf.Task Task} */
		TASK("Task"),

	    /** The display name for a {@link generated.dkf.Concept Concept} */
		CONCEPT("Concept"),
		
	    /** The display name for a {@link generated.dkf.Condition Condition} */
		CONDITION("Condition"),
		
	    /** The display name for a {@link generated.dkf.Actions.StateTransitions.StateTransition StateTransition} */
		STATE_TRANSITION("State Transition"),

	    /** The display name for a {@link generated.dkf.Strategy Strategy} */
        STRATEGY("Strategy"),

	    /** The display name for a {@link generated.dkf.PlacesOfInterest PlacesOfInterest} */
        PLACES_OF_INTEREST("Places of Interest"),

	    /** The display name for an {@link generated.dkf.Scenario.EndTriggers EndTriggers} */
        SCENARIO_END_TRIGGERS("End Triggers"),
        
        /** The display name for an {@link generated.dkf.TeamOrganization TeamOrganization} */
        TEAM_ORGANIZATION("Team Organization"),
        
        /** The display name for an {@link generated.dkf.Team Team} */
        TEAM("Team Member"),
        
        /** The display name for an {@link generated.dkf.TeamMember TeamMember} */
        TEAM_MEMBER("Team Member"),

	    /** The display name for a {@link generated.dkf.LearnerAction LearnerAction} */
        LEARNER_ACTIONS("Learner Actions"),

	    /** The display name for a {@link generated.dkf.Scenario Scenario}*/
        MISCELLANEOUS("Miscellaneous");
		
	    /** A user friendly name of the scenario object */
		private final String displayName;
		
		/**
		 * Constructor.
		 * 
		 * @param displayName A user friendly name of the scenario object
		 */
		private ScenarioObjectName(String displayName) {
			this.displayName = displayName;
		}
		
		/**
		 * Gets the scenario object display name
		 * 
		 * @return A user friendly name of the scenario object
		 */
		public String getDisplayName() {
			return displayName;
		}
}
