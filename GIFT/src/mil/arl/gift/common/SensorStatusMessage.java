/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.enums.SensorTypeEnum;

/**
 * This class represents a Sensor Error event. 
 * 
 * @author mhoffman
 *
 */
public class SensorStatusMessage {
    
    /** the name of the sensor producing the data that is being filtered */
    private String sensorName;

    /** the enumerated type of sensor producing the raw values for the filter */
    private SensorTypeEnum sensorType;
    
    /** the error message content */
    private String message;

    /**
     * Class constructor
     * 
     * @param sensorName - the name of the sensor reporting the error
     * @param sensorType - the type of sensor reporting the error
     * @param message - the error message
     */
    public SensorStatusMessage(String sensorName, SensorTypeEnum sensorType, String message){
        setSensorName(sensorName);
        setSensorType(sensorType);
        setMessage(message);
    }
    
    private void setMessage(String message){
        
        if(message == null){
            throw new IllegalArgumentException("The message can't be null");
        }
        
        this.message = message;
    }
    
    private void setSensorName(String sensorName){
        
        if(sensorName == null){
            throw new IllegalArgumentException("The sensor name can't be null");
        }
        
        this.sensorName = sensorName;
    }
    
    private void setSensorType(SensorTypeEnum sensorType){
        
        if(sensorType == null){
            throw new IllegalArgumentException("The sensor type can't be null");
        }
        
        this.sensorType = sensorType;
    }

    public String getSensorName() {
        return sensorName;
    }

    public SensorTypeEnum getSensorType() {
        return sensorType;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[SensorError: ");
        sb.append(", sensor name = ").append(sensorName);
        sb.append(", sensor type = ").append(sensorType);
        sb.append(", error = ").append(message);
        sb.append("]");

        return sb.toString();
    }
}
