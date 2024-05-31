/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import mil.arl.gift.sensor.impl.AbstractSensor;

/**
 * A SensorDataEvent is a container for sensor data produced from a sensor.
 * 
 * @author mhoffman
 *
 */
public class SensorDataEvent {

	/** the sensor which produced this event */
	private AbstractSensor sensor;
	
	/** the data the sensor produced */
	private AbstractSensorData data;

	/**
	 * Class constructor 
	 * 
	 * @param sensor the sensor which produced this event 
	 * @param data the data the sensor produced
	 */
	public SensorDataEvent(AbstractSensor sensor, AbstractSensorData data){
		this.sensor = sensor;
		this.data = data;
	}	
	
	/**
	 * Return the sensor which produced the data
	 * 
	 * @return AbstractSensor
	 */
	public AbstractSensor getSensor() {
		return sensor;
	}

	/**
	 * Return the data the sensor produced
	 * 
	 * @return AbstractSensorData
	 */
	public AbstractSensorData getData() {
		return data;
	}
	

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SensorDataEvent: ");
        sb.append(" ").append(getSensor());
        sb.append(", ").append(getData());
        sb.append("]");        
     
        return sb.toString();
    }
}
