/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import mil.arl.gift.sensor.impl.AbstractSensor;

/**
 * A SensorFilterEvent is a container for sensor filter data produced from a sensor filter.
 * 
 * @author mhoffman
 *
 */
public class SensorFilterEvent{

	/** the filter which produced the data */
	private AbstractSensorFilter filter;
	
	/** the filtered data */
	private SensorFilterData data;
	
	/** the sensor producing data which is being filtered */
	private AbstractSensor sensor;

	/**
	 * Class constructor 
	 * 
	 * @param filter - the filter which produced the data
	 * @param data - the filtered data
	 * @param sensor the sensor producing data which is being filtered
	 */
	public SensorFilterEvent(AbstractSensorFilter filter, SensorFilterData data, AbstractSensor sensor){
		this.filter = filter;
		this.data = data;
		this.sensor = sensor;
	}	
	
	/**
	 * Return the filter which produced the data 
	 * 
	 * @return AbstractSensorFilter
	 */
	public AbstractSensorFilter getFilter() {
		return filter;
	}

	/**
	 * Return the filtered data
	 * 
	 * @return SensorFilterData
	 */
	public SensorFilterData getData() {
		return data;
	}
	
	/**
	 * Return the sensor producing data which is being filtered
	 * 
	 * @return AbstractSensor
	 */
	public AbstractSensor getSensor(){
		return sensor;
	}
	
	@Override
	public String toString(){
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append("[SensorFilterEvent: ");
	    sb.append("sensor = ").append(sensor);
	    sb.append(", filter = ").append(filter);
	    sb.append(", data = ").append(data);
	    sb.append("]");
	    return sb.toString();
	}
}
