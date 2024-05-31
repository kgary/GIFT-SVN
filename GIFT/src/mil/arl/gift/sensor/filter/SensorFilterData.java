/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import java.util.Collections;
import java.util.Map;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;

/**
 * This class contains filtered information derived from raw sensor data and filtered data.
 * 
 * @author mhoffman
 *
 */
public class SensorFilterData {

    /** total number of milliseconds since the domain session started */
    private long elapsedTime;
    
    /** map of sensor filter attribute to value */
    private Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorFilterAttributeToValue;

	/**
     * Class constructor - populate attributes
     * 
     * @param sensorFilterAttributeToValue - map of sensor filter attribute to value
     * @param elapsedTime - total number of milliseconds since the domain session started
     */
    public SensorFilterData(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorFilterAttributeToValue, long elapsedTime)
    {
        this.elapsedTime = elapsedTime;
        this.sensorFilterAttributeToValue = Collections.unmodifiableMap(sensorFilterAttributeToValue);
    }   

    /**
     * Return the total number of milliseconds since the domain session started
     * 
     * @return long - elapsed time in seconds
     */
    public long getElapsedTime() {
		return elapsedTime;
	}

    /**
     * Return the map of sensor filter attribute to object for the sensor's filter at the elapsed time
     * 
     * @return Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> 
     */
	public Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> getSensorFilterAttributeToValue() {
		return sensorFilterAttributeToValue;
	}
	
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SensorFilterData: ");
        sb.append(" elapsed time = ").append(TimeUtil.formatTimeSystemLog(getElapsedTime()));
        sb.append(" AttrValue pairs = {");
        
        for(SensorAttributeNameEnum attr : getSensorFilterAttributeToValue().keySet()){
            sb.append(attr).append(":").append(getSensorFilterAttributeToValue().get(attr)).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }

}
