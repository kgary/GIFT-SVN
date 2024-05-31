/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner;

/**
 * Each enum represents a "type" of Predictor that can be used in a
 * LearnerConfiguration. This is particularly useful for end users because it
 * allows them to identify a Predictor using the enum's displayName rather than
 * the PredictorImpl that it maps to.
 * @author elafave
 *
 */
public enum PredictorTypeEnum {

	AROUSAL("Arousal Predictor", "learner.predictor.ArousalPredictor"),
	ENGAGEMENT_TWO_STATE("Engagement Two State Predictor", "learner.predictor.EngagementTwoStatePredictor"),
	ENGAGEMENT_THREE_STATE("Engagement Three State Predictor", "learner.predictor.EngagementThreeStatePredictor"),
	EXPERTISE("Expertise Predictor", "learner.predictor.ExpertisePredictor"),
	GENERIC_PREDICTOR("Generic Predictor", "learner.predictor.GenericPredictor"),
	GENERIC_TEMPORAL_PREDICTOR("Generic Predictor", "learner.predictor.GenericTemporalPredictor"),
	KNOWLEDGE("Knowledge Predictor", "learner.predictor.KnowledgePredictor"),
	MOTIVATION("Knowledge Predictor", "learner.predictor.MotivationPredictor"),
	SKILL("Skill Predictor", "learner.predictor.SkillPredictor"),
	TASK_PERFORMANCE_STATE("Task Performance State Predictor", "learner.predictor.TaskPerformanceStatePredictor"),
	UNDERSTANDING("Understanding Predictor", "learner.predictor.UnderstandingPredictor");
	
	private final String displayName;
	
	private final String predictorImpl;

    private PredictorTypeEnum(String displayName, String predictorImpl){
        this.displayName = displayName;
        this.predictorImpl = predictorImpl;
    }
    
    /**
     * 
     * @return A human-friendly string that identifies the PredictorTypeEnum.
     */
    public String getDisplayName() {
    	return displayName;
    }
    
    /**
     * 
     * @return The predictor impl used by the software in LearnerConfiguration.
     */
    public String getPredictorImpl() {
    	return predictorImpl;
    }
    
    /**
     * Performs a reverse look up based on the supplied predictorImpl to find
     * the PredictorTypeEnum with that implementation.
     * @param predictorImpl Predictor Implementation of the
     * PredictorTypeEnum to be returned.
     * @return PredictorTypeEnum with the supplied predictorImpl or NULL if
     * one couldn't be found.
     */
    static public PredictorTypeEnum fromPredictorImpl(String predictorImpl) {
    	PredictorTypeEnum[] values = PredictorTypeEnum.values();
    	for(PredictorTypeEnum predictorTypeEnum : values) {
    		if(predictorTypeEnum.getPredictorImpl().equals(predictorImpl)) {
    			return predictorTypeEnum;
    		}
    	}
    	return null;
    }
}
