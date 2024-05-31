/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced;

import generated.sensor.Sensor;

/**
 * A callback used to invoke logic when a sensor's name changes
 * 
 * @author nroberts
 */
public interface SensorNameChangedCallback{
	
	/**
	 * Invokes logic when a sensor's name changes
	 * 
	 * @param sensor the sensor whose name changed
	 */
	public void onSensorNameChanged(Sensor sensor);
}
