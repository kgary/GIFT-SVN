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

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataCollection;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.impl.AbstractSensor;

/**
 * A filter specialized for use with the Bioharness Sensor
 * 
 * The Bioharness generates multiple streams of data (e.g. heart rate and breathing waveform).
 * 
 * This filter allows the various data streams to be emitted to the GIFT network at differing rates.
 * 
 *  Practical application: 
 *  
 *      heartrate signal when viewed in the GIFT monitor is acceptable at 1 Hz update rate.
 *      
 *      Breathing waveform needs to be sent at a higher frequency (e.g. 5 Hz) to give a smooth curve on the GIFT Monitor.
 *  
 * @author cragusa
 *
 */
public class BioharnessFilter extends AbstractSensorFilter {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(BioharnessFilter.class);
    
    /** static final flag use for performance improvements in logging */
    private static final boolean IS_LOGGER_INFO_ENABLED = logger.isInfoEnabled();
	
	/** how often a filter message is sent */
	private static final double BREATHING_WAVEFORM_PACKET_INTERVAL =  200;
	private static final double GENERAL_PACKET_INTERVAL            = 1000;
	
	/** Elapsed domain session time in milliseconds when the last breathing waveform data was sent through as a new filter data event */
	private double timeLastSentBreathWave;
	
	/** Elapsed domain session time in milliseconds when the last general packet data was sent through as a new filter data event */
	private double timeLastSentGeneral;
		
	@Override
	public void filterSensorFilterData(SensorFilterEvent sensorFilterEvent){
		//nothing to do yet...
	}
	
	@Override
    public void start(long domainSessionStartTime) throws Exception {
		
	    logger.info("Start called for "+this);
	    super.start(domainSessionStartTime);
	    timeLastSentBreathWave = Double.NEGATIVE_INFINITY; 
	    timeLastSentGeneral = Double.NEGATIVE_INFINITY;
	}
	
	/**
	 * Process sensor data events.
	 * 
	 * @param sensorDataEvent The sensor data event to process
	 * @param sensor the sensor from which the data event originated.
	 */
	private void processSensorDataEvent(SensorDataEvent sensorDataEvent, AbstractSensor sensor) {
		
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
        }
		
	}
	
	/**
	 * Specialized handler method that only handles the breathing wave form sensor data events.
	 * 
	 * @param sensorDataEvent the sensor data event already qualified as a breathing waveform type.
	 * @param elapsedTime the elapsed domain session time of the event
	 */
	private void handleBreathingWaveformEvent(SensorDataEvent sensorDataEvent, double elapsedTime) {
		
		//TODO: the filter time checking could be on its own thread but then does it always send
		//      the latest value for all sensor attributes even if an attribute hasn't been given a value recently?
		if((elapsedTime - timeLastSentBreathWave) > BREATHING_WAVEFORM_PACKET_INTERVAL){
			
			AbstractSensor sensor = sensorDataEvent.getSensor();
				
            if(IS_LOGGER_INFO_ENABLED){
            	
                logger.info("Received sensor data event to filter and create a message for - " + sensorDataEvent);
            }
            
            //update time last sent
            timeLastSentBreathWave = elapsedTime;
            
            processSensorDataEvent(sensorDataEvent, sensor);
		}	
	}
	
	/**
	 * Specialized handler method that only handles the breathing wave form sensor data events.
	 * 
	 * @param sensorDataEvent the sensor data event already qualified as a Bioharness general event type.
	 * @param elapsedTime the elapsed domain session time of the event
	 */
	private void handleGeneralEvent(SensorDataEvent sensorDataEvent, double elapsedTime) {
		
		//TODO: the filter time checking could be on its own thread but then does it always send
		//      the latest value for all sensor attributes even if an attribute hasn't been given a value recently?
		if((elapsedTime - timeLastSentGeneral) > GENERAL_PACKET_INTERVAL){
			
			AbstractSensor sensor = sensorDataEvent.getSensor();
				
            if(IS_LOGGER_INFO_ENABLED){
            	
                logger.info("Received sensor data event to filter and create a message for - " + sensorDataEvent);
            }
            
            //update time last sent
            timeLastSentGeneral = elapsedTime;
            
            processSensorDataEvent(sensorDataEvent, sensor);            
		}	
	}
	
	
	@Override
	public void filterSensorData(SensorDataEvent sensorDataEvent){
		
		AbstractSensorData absData = sensorDataEvent.getData();
		
		if(absData instanceof SensorData) {
			
			SensorData sData = (SensorData)absData;
			
			double elapsedTime = sData.getElapsedTime();
			
			if( sData.getSensorAttributeToValue().containsKey(SensorAttributeNameEnum.BREATHING_WAVEFORM_SAMPLE) ) {
				
				handleBreathingWaveformEvent(sensorDataEvent, elapsedTime);
				
			} else if (sData.getSensorAttributeToValue().containsKey(SensorAttributeNameEnum.HEART_RATE) ) {
				
				handleGeneralEvent(sensorDataEvent, elapsedTime);
				
			} else {
				
				//must be a ECG waveform.  Drop it on the floor.
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
        sb.append("[BioharnessFilter:");
        sb.append(super.toString());
        sb.append(", time last sent = ").append(timeLastSentBreathWave);
        sb.append("]");
        
        return sb.toString();
    }

}
