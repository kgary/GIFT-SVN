/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

/**
 * This is the base class for sensor data internal to the Sensor Module.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractSensorData {

    /** total number of milliseconds since the start of the domain session */
    private long elapsedTime;
    
    /**
     * Class constructor
     * 
     * @param elapsedTime total number of milliseconds since the start of the domain session
     */
    public AbstractSensorData(long elapsedTime){
        this.elapsedTime = elapsedTime;
    }
    
    /**
     * Return the total number of milliseconds since the start of the domain session
     * 
     * @return long - elapsed time in seconds
     */
    public long getElapsedTime() {
        return elapsedTime;
    }
}
