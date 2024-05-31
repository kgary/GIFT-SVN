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
 * Sensor translators must extend this class
 * 
 * Scales the values coming from the sensor.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractSensorTranslator {

	/**
	 * Translate the sensor values
	 * 
	 * @param attributeToValue - sensor attribute name to it's value
	 * @return Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> - translated values
	 */
	public abstract Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> getValues(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attributeToValue);
}
