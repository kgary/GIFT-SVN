/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.util.Collections;
import java.util.Map;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;

/**
 * This class contains sensor data at a single point in time.  It represents
 * a single snapshot of the sensor and its attributes.
 * 
 * @author mhoffman
 *
 */
public class SensorData extends AbstractSensorData {

    /** map of sensor attribute to value */ 
    private Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue;

	/**
     * Class constructor - populate attributes
     * 
     * @param sensorAttributeToValue  map of sensor attribute to value 
     * @param elapsedTime - total number of milliseconds since the start of the domain session
     */
    public SensorData(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue, long elapsedTime){
        super(elapsedTime);
        
        this.sensorAttributeToValue = Collections.unmodifiableMap(sensorAttributeToValue);
    }

    /**
     * Return the map of sensor attribute to value for the sensor at the elapsed time
     * 
     * @return Map<SensorAttributeNameEnum, AbstractSensorAttributeValue>
     */
	public Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> getSensorAttributeToValue() {
		return sensorAttributeToValue;
	}
	
	@Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SensorData: ");
        sb.append(" elapsed time = ").append(TimeUtil.formatTimeSystemLog(getElapsedTime()));
        sb.append(" AttrValue pairs = {");
        
        for(SensorAttributeNameEnum attr : getSensorAttributeToValue().keySet()){
            sb.append(attr).append(":").append(getSensorAttributeToValue().get(attr)).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
