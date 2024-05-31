/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This class provides information on a learner state attribute (e.g. Affective state such as Arousal)
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class LearnerStateAttribute extends AbstractLearnerStateAttribute {
    
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    protected LearnerStateAttribute() {}
    
    /**
     * Class constructor
     * 
     * @param attribute - the state attribute
     * @param shortTerm - the short term value for the state attribute
     * @param shortTermTimestamp the time in milliseconds at which the short term state value was set
     * @param longTerm - the long term value for the state attribute
     * @param longTermTimestamp the time in milliseconds at which the long term state value was set
     * @param predicted - the predicted value for the state attribute
     * @param predictedTimestamp the time in milliseconds at which the predicted state value was set
     */
    public LearnerStateAttribute(LearnerStateAttributeNameEnum attribute, 
            AbstractEnum shortTerm, long shortTermTimestamp,
            AbstractEnum longTerm, long longTermTimestamp,
            AbstractEnum predicted, long predictedTimestamp){
        super(attribute, shortTerm, shortTermTimestamp, longTerm, longTermTimestamp, predicted, predictedTimestamp);

    }
	
	/**
	 * Class constructor
	 * 
     * @param attribute - the state attribute
     * @param shortTerm - the short term value for the state attribute
     * @param longTerm - the long term value for the state attribute
     * @param predicted - the predicted value for the state attribute
	 */
	public LearnerStateAttribute(LearnerStateAttributeNameEnum attribute, AbstractEnum shortTerm, AbstractEnum longTerm, AbstractEnum predicted){
		super(attribute, shortTerm, longTerm, predicted);

	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
        sb.append("[LearnerStateAttribute: ");
        sb.append(super.toString());
		sb.append("]");
		
		return sb.toString();
	}
}
