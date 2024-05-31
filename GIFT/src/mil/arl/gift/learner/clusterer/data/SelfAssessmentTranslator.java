/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer.data;


import mil.arl.gift.common.enums.SensorAttributeNameEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for normalizing the self assessment sensor data.
 * 
 * @author mhoffman
 *
 */
public class SelfAssessmentTranslator extends AbstractNormalizeTranslator {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SelfAssessmentTranslator.class);
	
	private static double MIN_VALUE = 0.0;
	private static double MAX_VALUE = 1.0;
	
	/**
	 * Class constructor
	 */
	public SelfAssessmentTranslator(){
		init();
	}

	/**
	 * Initialize the translator
	 */
	private void init(){
		
		//
		// Populate a container with the sensor's attributes value ranges
		//
		ranges.put(SensorAttributeNameEnum.ENGAGEMENT, new AttributeRange(MIN_VALUE, MAX_VALUE));
		
		logger.info("Populated ranges container: "+ranges);
	}

}
