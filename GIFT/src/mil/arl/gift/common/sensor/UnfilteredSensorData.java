/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.sensor;

import java.util.Map;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;

/**
 * This class contains information produced by raw sensor data.
 * 
 * @author mhoffman
 *
 */
public class UnfilteredSensorData extends AbstractSensorData {

    /** map of sensor attribute to value */
    private Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue;

    /**
     * Class constructor
     * 
     * @param sensorName - the name of the sensor producing the data
     * @param sensorType - the type of sensor producing the data
     * @param elapsedTime - total number of seconds from the sensor's start
     * @param sensorAttributeToValue - map of sensor attribute to value
     */
    public UnfilteredSensorData(String sensorName, SensorTypeEnum sensorType, long elapsedTime, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue){
        super(sensorName, sensorType, elapsedTime);
        
        this.sensorAttributeToValue = sensorAttributeToValue;
    }
    /**
     * Return the map of sensor attribute to value
     * 
     * @return Map<SensorAttributeNameEnum, AbstractSensorAttributeValue>
     */
    public Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> getAttributeValues(){
        return sensorAttributeToValue;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[UnfilteredSensorData: ");
        sb.append(super.toString());
        
        sb.append(", Attributes = {");
        for(SensorAttributeNameEnum name : sensorAttributeToValue.keySet()){
            sb.append("\n").append(name).append("=").append(sensorAttributeToValue.get(name)).append(",");
        }
        sb.append("}");
        
        sb.append("]");

        return sb.toString();
    }
}
