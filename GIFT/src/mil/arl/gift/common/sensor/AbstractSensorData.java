/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.sensor;

import mil.arl.gift.common.enums.SensorTypeEnum;

/**
 * This is the base class for sensor data shared between GIFT modules.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractSensorData {

    /** the name of the sensor producing the data that is being filtered */
    private String sensorName;
    
    /** the type of sensor producing the data being filtered */
    private SensorTypeEnum sensorType;
    
    /** total number of milliseconds from the domain session start */
    private long elapsedTime;
    
    /**
     * Class constructor - set attributes
     * 
     * @param sensorName the name of the sensor producing the data that is being filtered
     * @param sensorType the type of sensor producing the data being filtered
     * @param elapsedTime total number of milliseconds from the domain session start
     */
    public AbstractSensorData(String sensorName, SensorTypeEnum sensorType, long elapsedTime){
        
        setSensorName(sensorName);
        setSensorType(sensorType);
        setElapsedTime(elapsedTime);
    }
    
    private void setSensorName(String sensorName){
        
        if(sensorName == null){
            throw new IllegalArgumentException("The sensor name can't be null.");
        }
        
        this.sensorName = sensorName;
    }
    
    /**
     * Return the name of the sensor producing the data that is being filtered
     *  
     * @return String
     */
    public String getSensorName(){
        return sensorName;
    }
    
    private void setSensorType(SensorTypeEnum sensorType){
        
        if(sensorType == null){
            throw new IllegalArgumentException("The sensor type can't be null.");
        }
        
        this.sensorType = sensorType;
    }
    
    /**
     * Return the sensor type of the sensor producing filtered data
     * 
     * @return SensorTypeEnum
     */
    public SensorTypeEnum getSensorType(){
        return sensorType;
    }
    
    private void setElapsedTime(long elapsedTime){
        
        if(elapsedTime < 0){
            throw new IllegalArgumentException("The elapsed time value of "+elapsedTime+" must be non-negative.");
        }
        
        this.elapsedTime = elapsedTime;
    }
    
    /**
     * Return the total number of milliseconds from the start of the domain session
     * 
     * @return long
     */
    public long getElapsedTime(){
        return elapsedTime;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append(", SensorName = ").append(getSensorName());
        sb.append(", SensorType = ").append(getSensorType());
        sb.append(", elapsedTime = ").append(getElapsedTime());        
        return sb.toString();
    }

}
