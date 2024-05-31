/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.predictor;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.learner.clusterer.AbstractClassifier;

/**
 * This class predicts the next learner expertise state
 * 
 * @author mhoffman
 *
 */
public class ExpertisePredictor extends AbstractThreeStatePredictor {

    private static final LearnerStateAttributeNameEnum DEFAULT_STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.PRIOR_KNOWLEDGE; 
    
    /** the values to use for each state of the three state classification */
    private static final AbstractEnum DEFAULT_LOW_VALUE = ExpertiseLevelEnum.NOVICE;
    private static final AbstractEnum DEFAULT_MED_VALUE = ExpertiseLevelEnum.JOURNEYMAN;
    private static final AbstractEnum DEFAULT_HIGH_VALUE = ExpertiseLevelEnum.EXPERT;
    private static final AbstractEnum DEFAULT_UNKNOWN_VALUE = ExpertiseLevelEnum.UNKNOWN;
    
    /**
     * Class constructor 
     * 
     * @param classifier - the classifier feeding this predictor
     */
    public ExpertisePredictor(AbstractClassifier classifier){
        super(DEFAULT_STATE_ATTRIBUTE, DEFAULT_LOW_VALUE, DEFAULT_MED_VALUE, DEFAULT_HIGH_VALUE, DEFAULT_UNKNOWN_VALUE, classifier);
        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ExpertisePredictor: ").append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
