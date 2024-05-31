/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard;

import generated.sensor.Filter;
import generated.sensor.Sensor;
import generated.sensor.Writer;

import java.util.ArrayList;

public interface ISensorBuiltHandler {

	public void onSensorBuilt(Sensor sensor, Filter filter, ArrayList<Writer> writers);
}
