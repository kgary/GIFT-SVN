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
 * The Sensor File Created event happens when a sensor file has been created.
 * 
 * @author mhoffman
 *
 */
public class SensorFileCreated {

    /** the sensor file name */
    private String fileName;

    /** the type of sensor causing information to be written to the file */
    private SensorTypeEnum sensorType;

    /**
     * Class constructor
     * 
     * @param fileName The name of the file created
     * @param sensorType The sensor type that will be populated the sensor file
     */
    public SensorFileCreated(String fileName, SensorTypeEnum sensorType){
        this.fileName = fileName;
        this.sensorType = sensorType;
    }

    /**
     * Return the sensor file name 
     * 
     * @return String
     */
    public String getFileName(){
        return fileName;
    }

    /**
     * Return the type of sensor causing information to be written to this file
     * 
     * @return SensorTypeEnum
     */
    public SensorTypeEnum getSensorType(){
        return sensorType;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[SensorFileCreated: ");
        sb.append("File Name = ").append(getFileName());
        sb.append(", Sensor Type = ").append(getSensorType());
        sb.append("]");

        return sb.toString();
    }
}
