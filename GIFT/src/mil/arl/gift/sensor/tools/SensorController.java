/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.tools;

/**
 * This class is responsible for maintaining the value of a single sensor attribute.  
 * An example use case is the self assessment sensor.  This class allows for various controls
 * of the sensor attribute value such as increase, decrease and steady the rate of change in the value.
 *  
 * @author jleonard
 */
public class SensorController {
    
    /** interface used to notify when the sensor rate changes */
    private SensorControllerInterface sensorControllerInterface = null;
    
    /** the current sensor value */
    private double sensorValue = 0.0;
    
    /** whether the sensor controller is still alive */
    private boolean alive = true;

    /**
     * Class constructor - set the controller interface used for event notification
     * 
     * @param controllerInterface interface used to notify when the sensor rate changes 
     */
    public SensorController(SensorControllerInterface controllerInterface) {
        sensorControllerInterface = controllerInterface;
    }

    public void increaseRate() {
        sensorControllerInterface.modifySensorChangeRate(1);
    }

    public void steadyRate() {
        sensorControllerInterface.modifySensorChangeRate(0);
    }

    public void decreaseRate() {
        sensorControllerInterface.modifySensorChangeRate(-1);
    }
    
    /**
     * Signals that the sensor controller should be terminated
     */
    public void kill(){
        alive = false;
    }

    /**
     * Return the current sensor value
     * 
     * @return double
     */
    public double getSensorValue() {
        return sensorValue;
    }
    
    /**
     * Return whether the sensor controller is still alive
     *  
     * @return boolean
     */
    public boolean isAlive(){
        return alive;
    }

    public void updateSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }

}
