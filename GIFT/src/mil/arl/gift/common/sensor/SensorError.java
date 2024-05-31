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
 * This class represents a Sensor Error event
 *
 * @author jleonard
 */
public class SensorError extends SensorStatus {

    /**
     * Constructor
     *
     * @param sensorName - the name of the sensor reporting the error
     * @param sensorType - the type of sensor reporting the error
     * @param message - the error message
     */
    public SensorError(String sensorName, SensorTypeEnum sensorType, String message) {
        super(sensorName, sensorType, message);
    }
    
    @Override
    public boolean isErrorMessage() {

        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SensorError: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
