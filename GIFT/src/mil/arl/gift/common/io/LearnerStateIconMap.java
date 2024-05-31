/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.util.HashMap;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This class is used to provide learner state tree nodes with custom icons.  
 * 
 * @author mzellars
 */
public class LearnerStateIconMap {
	
    /** the folder where most gift images are located */
	private static final String IMAGES_FOLDER = "/mil/arl/gift/common/images/";
	
	/** the folder where learner state icons are located  */
	private static final String ICONS_FOLDER = IMAGES_FOLDER + "icons/";
	
	/** the icon to use as the default when one is not mapped */
	private static final String DEFAULT_ICON = ICONS_FOLDER + "placeholder.png";
	
	private static final String TASK = "Task";
	private static final String CONCEPT = "Concept";
	
	/** the map of icons */
	private static HashMap<Object, String> iconMap;
	
	/** force callers to use provided static methods */
	private LearnerStateIconMap() {}
	
	static {
		// Store references to all images that are used to represent a learner's state
		iconMap = new HashMap<Object, String>();
		
		// Add task and concept image references to map
		iconMap.put("Task", IMAGES_FOLDER + "gavel-solid.png");
		iconMap.put("Concept", IMAGES_FOLDER + "lightbulb-regular.png");
		
		// Learner state attribute references
		iconMap.put(LearnerStateAttributeNameEnum.ENGAGEMENT, ICONS_FOLDER + "engagement.png");
		iconMap.put(LearnerStateAttributeNameEnum.UNDERSTANDING, ICONS_FOLDER + "understanding.png");
		iconMap.put(LearnerStateAttributeNameEnum.AROUSAL, ICONS_FOLDER + "arousal.png");
		iconMap.put(LearnerStateAttributeNameEnum.LT_EXCITEMENT, ICONS_FOLDER + "lt_excitement.png");
		iconMap.put(LearnerStateAttributeNameEnum.ST_EXCITEMENT, ICONS_FOLDER + "st_excitement.png");
		iconMap.put(LearnerStateAttributeNameEnum.MEDITATION, ICONS_FOLDER + "meditation.png");
		iconMap.put(LearnerStateAttributeNameEnum.FRUSTRATION, ICONS_FOLDER + "frustration.png");
		
		
		// Learner states needed for rapid miner integration task.
        iconMap.put(LearnerStateAttributeNameEnum.ANXIOUS, ICONS_FOLDER + "anxious.png");
        iconMap.put(LearnerStateAttributeNameEnum.BORED, ICONS_FOLDER + "bored.png");   
        iconMap.put(LearnerStateAttributeNameEnum.CONFUSED, ICONS_FOLDER + "confused.png");
        iconMap.put(LearnerStateAttributeNameEnum.ENG_CONCENTRATION, ICONS_FOLDER + "engagement.png");
        iconMap.put(LearnerStateAttributeNameEnum.OFFTASK, ICONS_FOLDER + "offtask.png");
        iconMap.put(LearnerStateAttributeNameEnum.SURPRISED, ICONS_FOLDER + "surprised.png");
        
		// The following icons are driven by the eMAP (Pedagogical Model)
		iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION, ICONS_FOLDER + "motivation.png");
		iconMap.put(LearnerStateAttributeNameEnum.PRIOR_KNOWLEDGE, ICONS_FOLDER + "prior_knowledge.png");	
		iconMap.put(LearnerStateAttributeNameEnum.KNOWLEDGE, ICONS_FOLDER + "knowledge.png");
		iconMap.put(LearnerStateAttributeNameEnum.SKILL, ICONS_FOLDER + "skill.png");
		iconMap.put(LearnerStateAttributeNameEnum.SOCIO_ECONOMIC_STATUS, ICONS_FOLDER + "socio_economic_status.png");
		iconMap.put(LearnerStateAttributeNameEnum.LOCUS_OF_CONTROL, ICONS_FOLDER + "locus_of_control.png");
		iconMap.put(LearnerStateAttributeNameEnum.LEARNER_ABILITY, ICONS_FOLDER + "learner_ability.png");
		iconMap.put(LearnerStateAttributeNameEnum.GENERAL_INTELLIGENCE, ICONS_FOLDER + "general_intelligence.png");
		iconMap.put(LearnerStateAttributeNameEnum.LEARNING_STYLE, ICONS_FOLDER + "learning_style.png");
		iconMap.put(LearnerStateAttributeNameEnum.SELF_REGULATORY_ABILITY, ICONS_FOLDER + "self_regulatory_ability.png");
		iconMap.put(LearnerStateAttributeNameEnum.GRIT, ICONS_FOLDER + "grit.png");
		iconMap.put(LearnerStateAttributeNameEnum.SELF_EFFICACY, ICONS_FOLDER + "self-efficacy.png");
		iconMap.put(LearnerStateAttributeNameEnum.GOAL_ORIENTATION, ICONS_FOLDER + "goal_orientation.png");
		
		// The following icons are drive by the motivational assessment tool (MAT) two surveys from IST research
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_LEARNER_DRIVEN, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_CHALLENGE, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_LOSS_OF_EFFORT, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_FREQUENCY, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_FEAR_FREEZE_FLIGHT, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_COMPETITION, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_RELATEDNESS, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_SOCIAL_LINK, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_PUNISHMENT, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_BREAKS, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_FEEDBACK, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_TIME_DURING_LEARNING, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_ENERGIZER, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_SENSOR, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_LOGICAL_CONSEQUENCE, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_ACK_DIGITAL, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_LOW_AND_HIGH_VALUE, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_TIME_AFTER_LEARNING, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_HOBBY, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_ACTIVITY, ICONS_FOLDER + "motivation.png");
        iconMap.put(LearnerStateAttributeNameEnum.MOTIVATION_GOAL_ORIENTATION, ICONS_FOLDER + "motivation.png");
	}
	
	/**
	 * Used to obtain the path to the image used to represent a learner state
	 * 
	 * @param nodeEnum the LearnerStateAttributeNameEnum of the node that is requesting an image
	 * @return the path to the image mapped to the learner state.  A default image will be returned if not found in the map.
	 */
	public static String getEnumerationIconPath(LearnerStateAttributeNameEnum nodeEnum) {

		if (iconMap.containsKey(nodeEnum)) {
			return iconMap.get(nodeEnum);
		}
		
		// Couldn't find an image reference
		return DEFAULT_ICON;
	}
	
	/**
	 * Used to obtain the path to the image used to represent a task node.
	 * 
	 * @return the path to the image
	 */
	public static final String getTaskIconPath() {
		
		return iconMap.get(TASK);
	}

	/**
	 * Used to obtain the path to the image used to represent a concept node.
	 * 
	 * @return the path to the image
	 */
	public static final String getConceptIconPath() {
		
		return iconMap.get(CONCEPT);
	}
}
