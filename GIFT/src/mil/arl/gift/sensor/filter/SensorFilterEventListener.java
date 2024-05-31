/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import mil.arl.gift.sensor.SensorEventListener;

/**
 * This interface is used by classes who listen for filter events.
 * 
 * @author mhoffman
 *
 */
public interface SensorFilterEventListener extends SensorEventListener{

	/**
	 * Sensor Filter has produced data
	 * 
	 * @param elapsedTime - elapsed domain session time in milliseconds
	 * @param sensorFilterEvent - the filter event containing filtered sensor data
	 * @throws Exception if there was a severe problem handling the sensor filter event
	 */
	void sensorFilterEvent(long elapsedTime, SensorFilterEvent sensorFilterEvent) throws Exception;
}
