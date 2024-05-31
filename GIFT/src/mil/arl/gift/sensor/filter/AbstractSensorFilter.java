/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.sensor.AbstractEventProducer;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.SensorDataEventListener;
import mil.arl.gift.sensor.SensorManager;
import mil.arl.gift.sensor.impl.AbstractSensor;

/**
 * This abstract class is used by sensor filter classes to receive and filter
 * sensor data.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractSensorFilter extends AbstractEventProducer implements SensorDataEventListener, SensorFilterEventListener{

    /** filter properties */
    public static final String NAME = "name";
    
    /** name of the sensor filter */
    private String name;
    
	/**
	 * Class constructor - default
	 */
	public AbstractSensorFilter(){
		
	}
	
	@Override
	public void sensorDataEvent(SensorDataEvent sensorDataEvent){
		filterSensorData(sensorDataEvent);
	}
	
	/**
	 * Filter the sensor data
	 * 
	 * @param sensorDataEvent - sensor data
	 */
	abstract void filterSensorData(SensorDataEvent sensorDataEvent);
	
	@Override
	public void sensorFilterEvent(long elapsedTime, SensorFilterEvent sensorFilterEvent){
		filterSensorFilterData(sensorFilterEvent);
	}
	
	/**
	 * Set the name of the sensor filter
	 * 
	 * @param name a display name for the filter
	 */
	public void setName(String name){
	    
        if(name == null){
            throw new IllegalArgumentException("the sensor filter name is null");
        }
        
        this.name = name;
	}
    
    /**
     * Handle the sensor data by creating a filtered data event from it
     * 
     * @param sData the sensor data to filter
     * @param sensor where the sensor data came from
     */
    protected void handleSensorDataEvent(SensorData sData, AbstractSensor sensor){
        
        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attrToValue = new HashMap<>(sData.getSensorAttributeToValue());
        
        //send filter event
        SensorFilterData fData = new SensorFilterData(attrToValue, sData.getElapsedTime());
        SensorManager.getInstance().createSensorFilterDataEvent(this, fData, sensor);
    }
	
	/**
	 * Filter the filter data
	 * 
	 * @param sensorFilterEvent - sensor filter data
	 */
	abstract void filterSensorFilterData(SensorFilterEvent sensorFilterEvent);
	
    /**
     * Return the filter name
     * 
     * @return String
     */
    public String getFilterName(){
        return name;
    }
	
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AbstractSensorFilter:");
        sb.append(super.toString());
        sb.append(", name = ").append(getFilterName());
        sb.append("]");
        
        return sb.toString();
    }
}
