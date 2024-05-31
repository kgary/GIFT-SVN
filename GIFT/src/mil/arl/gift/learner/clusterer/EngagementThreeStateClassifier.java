/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.EngagementLevelEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This class is responsible for classifying the sensor data it receives in order
 * to determine what the current learner state attribute value is for engagement.
 * 
 * Zones:
 * 
 *           1.0  ***************************************
 *           					Level 3
 *           
 *                 ************ Level 2 Threshold *******
 *                 
 *                 				Level 2
 *                 
 *                 ************ Level 1 Threshold *******
 *                 
 *                 				Level 1
 *                 
 *            0.0  **************************************
 * 
 * @author mhoffman
 *
 */
public class EngagementThreeStateClassifier extends AbstractThreeStateClassifier{
	
    private static final SensorAttributeNameEnum DEFAULT_SENSOR_ATTRIBUTE = SensorAttributeNameEnum.ENGAGEMENT;
    private static final LearnerStateAttributeNameEnum DEFAULT_STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.ENGAGEMENT; 
    
    /** the values to use for each state of the three state classification */
    private static final AbstractEnum DEFAULT_LOW_VALUE = EngagementLevelEnum.LOW;
    private static final AbstractEnum DEFAULT_MED_VALUE = EngagementLevelEnum.MEDIUM;
    private static final AbstractEnum DEFAULT_HIGH_VALUE = EngagementLevelEnum.HIGH;
    private static final AbstractEnum DEFAULT_UKNOWN_VALUE = EngagementLevelEnum.UNKNOWN;
	
	/**
	 * Default Constructor
	 */
	public EngagementThreeStateClassifier(){
	    super(DEFAULT_SENSOR_ATTRIBUTE, DEFAULT_STATE_ATTRIBUTE, DEFAULT_LOW_VALUE, DEFAULT_MED_VALUE, DEFAULT_HIGH_VALUE, DEFAULT_UKNOWN_VALUE);
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[EngagementThreeStateClassifier:");
	    sb.append(super.toString());
	    sb.append("]");
		return sb.toString();
	}
}
