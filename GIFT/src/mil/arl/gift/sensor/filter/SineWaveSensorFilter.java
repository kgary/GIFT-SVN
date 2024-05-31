/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataCollection;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.impl.SineWaveSensor;


/**
 * This class is responsible for filtering sensor data and producing a filtered event.
 * 
 * @author mhoffman
 *
 */
public class SineWaveSensorFilter extends AbstractSensorFilter {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SineWaveSensorFilter.class);
	
	/** how often a filter message is sent */
	private static final double DEFAULT_INTERVAL_SEC = 1.0;
	
	private double timeLastSent = 0.0;
	
	/**
	 * Empty constructor
	 */
	public SineWaveSensorFilter(){
		
	}
	
	@Override
	public void filterSensorFilterData(SensorFilterEvent sensorFilterEvent){
		//nothing to do yet...
	}
	
	@Override
    public void start(long domainSessionStartTime) throws Exception{
        
	    logger.info("Start called for "+this);
	    super.start(domainSessionStartTime);
	    timeLastSent = 0.0; 
	}
	
	@Override
	public void filterSensorData(SensorDataEvent sensorDataEvent){
		
		//TODO: the filter time checking could be on its own thread but then does it always send
		//      the latest value for all sensor attributes even if an attribute hasn't been given a value recently?
		if((sensorDataEvent.getData().getElapsedTime() - timeLastSent) > DEFAULT_INTERVAL_SEC){
			
			AbstractSensor sensor = sensorDataEvent.getSensor();
			if(sensor instanceof SineWaveSensor){
				
				if(logger.isInfoEnabled()){
					logger.info("Received sensor data event to filter and create a message for - " + sensorDataEvent);
				}
				
				//update time last sent
				timeLastSent = sensorDataEvent.getData().getElapsedTime();
				
	            //convert attributes to object map
	            AbstractSensorData sData = sensorDataEvent.getData();
	            if(sData instanceof SensorDataCollection){
	                
	                for(SensorData sensorData : ((SensorDataCollection)sData).getSensorDatas()){
	                    handleSensorDataEvent(sensorData, sensor);
	                }
	                
	            }else if(sData instanceof SensorData){
	                handleSensorDataEvent((SensorData)sData, sensor);
	            }else{
	                logger.error("Unable to handle sensor data event of type "+sData);
	                return;
	            }

			}else{
			    logger.error("The sine wave sensor filter should only be given sine wave sensor events, not events from "+sensor);
			}
		}
	}
	
    @Override
    protected void writerFileCreated(String fileName){
        //SensorModule sModule = SensorManager.getInstance().getProducerSensorModule(this);
        
        //TODO: as of now our UMS db doesn't support filter data file referencing
    }
	
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SineWaveSensorFilter:");
        sb.append(super.toString());
        sb.append(", time last sent = ").append(timeLastSent);
        sb.append("]");
        
        return sb.toString();
    }
	
}
