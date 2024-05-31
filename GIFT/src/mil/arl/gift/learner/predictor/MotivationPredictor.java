/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.predictor;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.LowMediumHighLevelEnum;
import mil.arl.gift.learner.clusterer.AbstractClassifier;

/**
 * This class predicts the next learner motiviation state
 * 
 * @author mhoffman
 *
 */
public class MotivationPredictor extends AbstractThreeStatePredictor {

    private static final LearnerStateAttributeNameEnum DEFAULT_STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.MOTIVATION; 
    
    /** the values to use for each state of the three state classification */
    private static final AbstractEnum DEFAULT_LOW_VALUE = LowMediumHighLevelEnum.LOW;
    private static final AbstractEnum DEFAULT_MED_VALUE = LowMediumHighLevelEnum.MEDIUM;
    private static final AbstractEnum DEFAULT_HIGH_VALUE = LowMediumHighLevelEnum.HIGH;
    private static final AbstractEnum DEFAULT_UNKNOWN_VALUE = LowMediumHighLevelEnum.UNKNOWN;
    
    /**
     * Class constructor 
     * 
     * @param classifier - the classifier feeding this predictor
     */
    public MotivationPredictor(AbstractClassifier classifier){
        super(DEFAULT_STATE_ATTRIBUTE, DEFAULT_LOW_VALUE, DEFAULT_MED_VALUE, DEFAULT_HIGH_VALUE, DEFAULT_UNKNOWN_VALUE, classifier);
        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[MotivationPredictor: ").append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
