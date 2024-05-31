/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner;

/**
 * Each enum represents a "type" of Classifier that can be used in a
 * LearnerConfiguration. This is particularly useful for end users because it
 * allows them to identify a Classifier using the enum's displayName rather than
 * the ClassifierImpl that it maps to.
 * @author elafave
 *
 */
public enum ClassifierTypeEnum {

	ANXIOUS("Anxious Classifier", "learner.clusterer.AnxiousClassifier"),
	AROUSAL("Arousal Classifier", "learner.clusterer.ArousalClassifier"),
	ENGAGEMENT_CONCENTRATION("Engagement Concentration Classifier", "learner.clusterer.EngConcentrationClassifier"),
	ENGAGEMENT_THREE_STATE("Engagement Three State Classifier", "learner.clusterer.EngagementThreeStateClassifier"),
	ENGAGEMENT_TWO_STATE("Engagement Two State Classifier", "learner.clusterer.EngagementTwoStateClassifier"),
	EXPERTISE("Expertise Classifier", "learner.clusterer.ExpertiseClassifier"),
	FUZZY_ART("Fuzzy ART Classifier", "learner.clusterer.FuzzyARTClassifier"),
	GENERIC("Generic Classifier", "learner.clusterer.GenericClassifier"),
	GENERIC_THREE_STATE("Generic Three State Classifier", "learner.clusterer.GenericThreeStateClassifier"),
	KNOWLEDGE("Knowledge Classifier", "learner.clusterer.KnowledgeClassifier"),
	MOTIVATION("Motivation Classifier", "learner.clusterer.MotivationClassifier"),
	SKILL("Skill Classifier", "learner.clusterer.SkillClassifier"),
	TASK_PERFORMANCE_STATE("Task Performance State Classifier", "learner.clusterer.TaskPerformanceStateClassifier"),
	UNDERSTANDING("Understanding Classifier", "learner.clusterer.UnderstandingClassifier");
	
	private final String displayName;
	
	private final String classifierImpl;

    private ClassifierTypeEnum(String displayName, String classifierImpl){
        this.displayName = displayName;
        this.classifierImpl = classifierImpl;
    }
    
    /**
     * 
     * @return A human-friendly string that identifies the ClassifierTypeEnum.
     */
    public String getDisplayName() {
    	return displayName;
    }
    
    /**
     * 
     * @return The ClassifierTypeEnum used by the software in LearnerConfiguration.
     */
    public String getClassifierImpl() {
    	return classifierImpl;
    }
    
    /**
     * Performs a reverse look up based on the supplied classifierImpl to find
     * the ClassifierTypeEnum with that implementation.
     * @param classifierImpl Classifier Implementation of the
     * ClassifierTypeEnum to be returned.
     * @return ClassifierTypeEnum with the supplied classifierImpl or NULL if
     * one couldn't be found.
     */
    static public ClassifierTypeEnum fromClassifierImpl(String classifierImpl) {
    	ClassifierTypeEnum[] values = ClassifierTypeEnum.values();
    	for(ClassifierTypeEnum classifierTypeEnum : values) {
    		if(classifierTypeEnum.getClassifierImpl().equals(classifierImpl)) {
    			return classifierTypeEnum;
    		}
    	}
    	return null;
    }
}
