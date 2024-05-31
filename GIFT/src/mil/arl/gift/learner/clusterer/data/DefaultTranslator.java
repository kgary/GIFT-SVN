/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer.data;


import java.util.Map;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;

/**
 * This class is responsible for passing sensor data through without normalizing.
 * 
 * @author mhoffman
 *
 */
public class DefaultTranslator extends AbstractNormalizeTranslator {
	
	/**
	 * Class constructor
	 */
	public DefaultTranslator(){
		
	}
	
	@Override
    public Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> getValues(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attributeToValue){        
        return attributeToValue;
    }

}
