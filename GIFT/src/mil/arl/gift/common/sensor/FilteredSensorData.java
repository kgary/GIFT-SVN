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
 * This class contains filtered sensor data.
 * 
 * @author mhoffman
 *
 */
public class FilteredSensorData extends AbstractSensorData{

    /** the name of the filter producing the data */
    private String filterName;    
    
    /** map of sensor filter attribute to value */
    private Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorFilterAttributeToValue;
    
    /**
     * Class constructor - set attributes
     * 
     * @param filterName the name of the filter producing the data 
     * @param sensorName the name of the sensor producing the data that is being filtered
     * @param sensorType the type of sensor producing the data being filtered
     * @param elapsedTime total number of milliseconds from the domain session start
     * @param sensorFilterAttributeToValue map of sensor filter attribute to value
     */
    public FilteredSensorData(String filterName, String sensorName, SensorTypeEnum sensorType, long elapsedTime, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorFilterAttributeToValue){
        super(sensorName, sensorType, elapsedTime);
        
        this.filterName = filterName;
        this.sensorFilterAttributeToValue = sensorFilterAttributeToValue;
    }
    
    /**
     * Return the name of the filter producing the data
     * 
     * @return String
     */
    public String getFilterName(){
        return filterName;
    }
    
    /**
     * Return the map of sensor filter attribute to value
     * 
     * @return Map<SensorAttributeNameEnum, AbstractSensorAttributeValue>
     */
    public Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> getAttributeValues(){
        return sensorFilterAttributeToValue;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[FilteredSensorData: ");
        sb.append("filterName = ").append(getFilterName());
        sb.append(", ").append(super.toString());
        
        sb.append(", Attributes = {");
        for(SensorAttributeNameEnum name : sensorFilterAttributeToValue.keySet()){
            sb.append("\n").append(name).append("=").append(sensorFilterAttributeToValue.get(name)).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
