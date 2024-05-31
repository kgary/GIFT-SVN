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

import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataCollection;
import mil.arl.gift.sensor.SensorDataEvent;


/**
 * This class is responsible for filtering sensor data and producing a filtered event.
 * 
 * @author mhoffman
 *
 */
public class GenericSensorFilter extends AbstractSensorFilter {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GenericSensorFilter.class);
	
	/** how often a filter message is sent */
	private static final double DEFAULT_INTERVAL_MSEC = 1000;
	
	/** number of milliseconds since the last filter data was sent through as a new filter data event */
	private double timeLastSent;
	
	/**
	 * Empty constructor
	 */
	public GenericSensorFilter(){
		
	}
	
	@Override
	public void filterSensorFilterData(SensorFilterEvent sensorFilterEvent){
		//nothing to do yet...
	}
	
	@Override
    public void start(long domainSessionStartTime) throws Exception{
        
	    logger.info("Start called for "+this);
	    super.start(domainSessionStartTime);
	    timeLastSent = Double.NEGATIVE_INFINITY; 
	}
	
	@Override
	public void filterSensorData(SensorDataEvent sensorDataEvent){
		
		//TODO: the filter time checking could be on its own thread but then does it always send
		//      the latest value for all sensor attributes even if an attribute hasn't been given a value recently?
		if((sensorDataEvent.getData().getElapsedTime() - timeLastSent) > DEFAULT_INTERVAL_MSEC){
			
			AbstractSensor sensor = sensorDataEvent.getSensor();
				
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
        sb.append("[GenericSensorFilter:");
        sb.append(super.toString());
        sb.append(", time last sent = ").append(timeLastSent);
        sb.append("]");
        
        return sb.toString();
    }
	
}
