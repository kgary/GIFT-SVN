/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

/**
 * This interface is used to notify implementations of sensor data events.
 * 
 * @author mhoffman
 *
 */
public interface SensorDataEventListener extends SensorEventListener {

	/**
	 * Sensor has produced data
	 * 
	 * @param sensorDataEvent an event containing new sensor data
	 * @throws Exception if there was a severe problem handling the sensor event
	 */
	void sensorDataEvent(SensorDataEvent sensorDataEvent) throws Exception;
}
