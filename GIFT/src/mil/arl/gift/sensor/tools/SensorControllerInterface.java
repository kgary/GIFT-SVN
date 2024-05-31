/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.tools;

/**
 * This interface should be implemented by sensor classes which have the ability
 * for external interaction such as changing the sensors rate of change in value
 * 
 * @author jleonard
 */
public interface SensorControllerInterface {

    /**
     * Modify the sensor's rate of change
     * 
     * @param rate how often to update the sensor value
     */
    void modifySensorChangeRate(int rate);
}
