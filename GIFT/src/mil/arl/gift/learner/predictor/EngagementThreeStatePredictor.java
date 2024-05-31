/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.predictor;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.EngagementLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.learner.clusterer.AbstractClassifier;

/**
 * This class predicts the next learner engagement state and classifies the state into
 * one of three possible engagement states.
 * 
 * @author mhoffman
 *
 */
public class EngagementThreeStatePredictor extends AbstractThreeStatePredictor {

    private static final LearnerStateAttributeNameEnum DEFAULT_STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.ENGAGEMENT; 
    
    /** the values to use for each state of the three state classification */
    private static final AbstractEnum DEFAULT_LOW_VALUE = EngagementLevelEnum.LOW;
    private static final AbstractEnum DEFAULT_MED_VALUE = EngagementLevelEnum.MEDIUM;
    private static final AbstractEnum DEFAULT_HIGH_VALUE = EngagementLevelEnum.HIGH;
    private static final AbstractEnum DEFAULT_UNKNOWN_VALUE = EngagementLevelEnum.UNKNOWN;
    
	/**
	 * Class constructor
	 * 
	 * @param classifier - the classifier feeding this predictor
	 */
	public EngagementThreeStatePredictor(AbstractClassifier classifier){
		super(DEFAULT_STATE_ATTRIBUTE, DEFAULT_LOW_VALUE, DEFAULT_MED_VALUE, DEFAULT_HIGH_VALUE, DEFAULT_UNKNOWN_VALUE, classifier);
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[EngagementThreeStatePredictor:");
	    sb.append(" ").append(super.toString());
	    sb.append("]");
		return sb.toString();
	}
}
