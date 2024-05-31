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
 * This class represents a Sensor Status event.
 *
 * @author mhoffman
 */
public class SensorStatus {

    /** the name of the sensor producing the data that is being filtered */
    private String sensorName;

    /** the enumerated type of sensor producing the raw values for the filter */
    private SensorTypeEnum sensorType;

    /** the message content */
    private String message;

    /**
     * Class constructor
     *
     * @param sensorName - the name of the sensor reporting the status
     * @param sensorType - the type of sensor reporting the status
     * @param message - the status message
     */
    public SensorStatus(String sensorName, SensorTypeEnum sensorType, String message) {

        setSensorName(sensorName);
        setSensorType(sensorType);
        setMessage(message);
    }

    private void setMessage(String message) {

        if (message == null) {

            throw new IllegalArgumentException("The message can't be null");
        }

        this.message = message;
    }

    private void setSensorName(String sensorName) {

        if (sensorName == null) {

            throw new IllegalArgumentException("The sensor name can't be null");
        }

        this.sensorName = sensorName;
    }

    private void setSensorType(SensorTypeEnum sensorType) {

        if (sensorType == null) {

            throw new IllegalArgumentException("The sensor type can't be null");
        }

        this.sensorType = sensorType;
    }

    /**
     * Returns if this message is reporting an error
     *
     * @return boolean If this message is reporting an error
     */
    public boolean isErrorMessage() {

        return false;
    }

    /**
     * Gets the name of sensor reporting the status
     *
     * @return String The name of sensor reporting the status
     */
    public String getSensorName() {

        return sensorName;
    }

    /**
     * Gets the type of sensor reporting the status
     *
     * @return SensorTypeEnum The type of sensor reporting the status
     */
    public SensorTypeEnum getSensorType() {

        return sensorType;
    }

    /**
     * Gets the status message
     *
     * @return String The status message
     */
    public String getMessage() {

        return message;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SensorStatus: ");
        sb.append(", sensor name = ").append(sensorName);
        sb.append(", sensor type = ").append(sensorType);
        sb.append(", message = ").append(message);
        sb.append("]");

        return sb.toString();
    }
}
