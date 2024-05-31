/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.enums;

/**
 * This class contains definitions of all course transition names. Used to prevent
 * needing to modify course transition names across multiple files. 
 * 
 * @author bzahid
 */
public class CourseObjectNames {
		
	public enum CourseObjectName {
	
		SURVEY("Survey"),
		METADATA("Metadata"),
		GUIDANCE("Information"),
		AAR("Structured Review"),
		LESSON_MATERIAL("Media"),
		ADAPTIVE_COURSEFLOW("Adaptive Courseflow"),
		DKF("Real-Time Assessment"),
		SENSOR_CONFIG("Sensor Configuration"),
		LEARNER_CONFIG("Learner Configuration"),
		PED_CONFIG("Pedagogical Configuration"),
		CONVERSATION_TREE("Conversation Tree"),
		TRAINING_APPLICATION("External Application"),
		AUTHORED_BRANCH("Authored Branch");
		
		private final String displayName;
		
		private CourseObjectName(String displayName) {
			this.displayName = displayName;
		}
		
		/**
		 * Gets the course object display name
		 * 
		 * @return A user friendly name of the course object
		 */
		public String getDisplayName() {
			return displayName;
		}
	}
	
}
