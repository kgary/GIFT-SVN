/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.sensor.filter.AbstractSensorFilter;
import mil.arl.gift.sensor.filter.SensorFilterData;
import mil.arl.gift.sensor.filter.SensorFilterEvent;
import mil.arl.gift.sensor.impl.AbstractSensor;


/**
 * The sensor manager is responsible for managing publish/subscribe sensor events.
 * 
 * @author mhoffman
 *
 */
public class SensorManager {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SensorManager.class);
    
	/**
	 * container of event producers (sensors, filters) to the sensor module responsible for it
	 */    
    private Map<AbstractEventProducer, SensorModule> eventProducerToModule = new HashMap<AbstractEventProducer, SensorModule>();
    
	/** the singleton instance of this class */
	private static SensorManager instance = null;
	
	/** container of event producers (sensors, filters) to the event mediator responsible for threading events */
	private Map<AbstractEventProducer, EventMediator> eventMediators = new HashMap<AbstractEventProducer, EventMediator>();
	
	/**
	 * Return the singleton instance of this class
	 * 
	 * @return SensorManager - the singleton instance of this class
	 */
	public static SensorManager getInstance(){
		
		if(instance == null){
			instance = new SensorManager();
		}
		
		return instance;
	}
	
	
	/**
	 * Class constructor
	 */
	private SensorManager(){

	}
	
	/**
	 * Add a mapping of event producer to sensor module
	 * 
	 * @param module - the sensor module containing the event producer provided
	 * @param eProducer - the event producer to map
	 */
	public void addEventProducerListener(SensorModule module, AbstractEventProducer eProducer){
	    eventProducerToModule.put(eProducer, module);
	}
	
	/**
     * Remove a mapping of event producer to sensor module
     * 
     * @param eProducer - the listener to remove
     */
    public void removeEventProducerListener(AbstractEventProducer eProducer){
        eventProducerToModule.remove(eProducer);
    }
	
	/**
	 * Return the sensor module for the given event producer
	 * 
	 * @param eProducer - the event producer to retrieve the sensor module instance of
	 * @return SensorModule - the sensor module for the event producer
	 */
	public SensorModule getProducerSensorModule(AbstractEventProducer eProducer){
	    return eventProducerToModule.get(eProducer);
	}
	
    /**
     * Return the number of sensors managed by this class.
     * 
     * @return int the number of sensors
     */
    public int getNumberOfSensors(){
        
        int cnt = 0;
        
        //synchronize producers in case a producer is being removed/started while adding these producers
        synchronized (eventProducerToModule){
            
            for(AbstractEventProducer producer : eventProducerToModule.keySet()){
                
                if(producer instanceof AbstractSensor){
                    cnt++;
                }
            }
        }
        
        return cnt;
    }
	
	/**
	 * Create a sensor data event from the data produced by the sensor.
	 * 
	 * @param sensor - sensor producing the data event
	 * @param data - the data the sensor produced
	 */
	public void createSensorDataEvent(AbstractSensor sensor, AbstractSensorData data){
		
	      EventMediator mediator = eventMediators.get(sensor);
          if(mediator == null){
              //create new mediator instance to handle this sensor's events
              
              mediator = new EventMediator(sensor.getSensorName());
              mediator.addListener(new SensorEventMediatorListener());
              mediator.start();
              
              eventMediators.put(sensor, mediator);
          }
         
          SensorDataEvent event = new SensorDataEvent(sensor, data);
          try {
              mediator.enqueue(event);
          } catch (Exception ie) {
              logger
                  .error(mediator.getName()
                      + " - Event: "
                      + event
                      + " failed to add to the eventQueue (interrupted).  Event dropped.", ie);
          }
          
	}
	
	/**
	 * Notify listeners of sensor data event
	 * 
	 * @param event - the sensor data event to notify listeners about
	 */
	private void fireNotificationSensorData(SensorDataEvent event){
	            
        if(event.getSensor().getSensorFilter() != null){
            
            try{
                event.getSensor().getSensorFilter().sensorDataEvent(event);
            }catch(Exception e){
                logger.error("Caught exception from mis-behaving sensor filter = "+event.getSensor().getSensorFilter(), e);
                createSensorStatus(true, event.getSensor(), event.getSensor().getSensorName(), event.getSensor().getSensorType(), "Unable to filter raw sensor data for sensor named "+event.getSensor().getSensorName());
            }
        }
        
        if(event.getSensor().getEventWriter() != null){
            
            try{
                event.getSensor().getEventWriter().sensorDataEvent(event);
            }catch(Exception e){
                logger.error("Caught exception from mis-behaving sensor event writer = "+event.getSensor().getEventWriter(), e);
                createSensorStatus(true, event.getSensor(), event.getSensor().getSensorName(), event.getSensor().getSensorType(), "Unable to write raw sensor data for sensor named "+event.getSensor().getSensorName());
            }
        }
        
        if(event.getSensor().shouldDistributeExternally()){
            SensorModule module = eventProducerToModule.get(event.getSensor());
            
            if(module != null){
                try{
                    module.sensorDataEvent(event);
                }catch(Exception e){
                    logger.error("Caught exception while trying to send raw sensor data externally.", e);
                    createSensorStatus(true, event.getSensor(), event.getSensor().getSensorName(), event.getSensor().getSensorType(), "Unable to send raw sensor data for sensor named "+event.getSensor().getSensorName());
                }
            }else{
                logger.error("Unable to find a sensor module for "+event.getSensor()+", therefore the sensor data event will not be sent externally");
                createSensorStatus(true, event.getSensor(), event.getSensor().getSensorName(), event.getSensor().getSensorType(), "Unable to send raw sensor data for sensor named "+event.getSensor().getSensorName());
            }
        }

	}
	
	/**
	 * Create a sensor filter data event from the filter data produced by the filter.
	 * 
	 * @param filter - filter producing the data event
	 * @param data - the data the filter produced
     * @param sensor - the sensor sending the event to send
	 */
	public void createSensorFilterDataEvent(AbstractSensorFilter filter, SensorFilterData data, AbstractSensor sensor){
				
	    EventMediator mediator = eventMediators.get(filter);
	    if(mediator == null){
	        //create new mediator to handle this filter's events
	        
	        mediator = new EventMediator(filter.getFilterName());
	        mediator.addListener(new FilterEventMediatorListener());
	        mediator.start();
	        
	        eventMediators.put(filter, mediator);
	    }
	   
        SensorFilterEvent event = new SensorFilterEvent(filter, data, sensor);
        try {
            mediator.enqueue(event);
        } catch (Exception ie) {
            logger
                .error(mediator.getName()
                    + " - Event: "
                    + event
                    + " failed to add to the eventQueue (interrupted).  Event dropped.", ie);
        }

	}
	
	/**
	 * Notify listeners of sensor filter data event
	 * 
	 * @param filter - the filter producing the data event
	 * @param data - the data the filter produced
	 */
	private void fireNotificationSensorFilterData(SensorFilterEvent event){	    
	    
        if(event.getFilter().getEventWriter() != null){        	
            
            try{
                event.getFilter().getEventWriter().sensorFilterEvent(event.getData().getElapsedTime(), event);
            }catch(Exception e){
                logger.error("Caught exception from mis-behaving sensor event writer = "+event.getSensor().getEventWriter(), e); 
            }
        }
        
        if(event.getFilter().shouldDistributeExternally()){
            
            SensorModule sModule = eventProducerToModule.get(event.getFilter());
            
            if(sModule != null){
                try{
                    sModule.sensorFilterEvent(event.getData().getElapsedTime(), event);
                }catch(Exception e){
                    logger.error("Caught exception while trying to send filtered sensor data externally.", e);
                    createSensorStatus(true, event.getSensor(), event.getSensor().getSensorName(), event.getSensor().getSensorType(), "Unable to send filtered sensor data for sensor named "+event.getSensor().getSensorName());
                }
            }else{
                logger.error("Unable to find a sensor module for "+event.getFilter()+", therefore the sensor filter event will not be sent externally");
                createSensorStatus(true, event.getSensor(), event.getSensor().getSensorName(), event.getSensor().getSensorType(), "Unable to send filtered sensor data for sensor named "+event.getSensor().getSensorName());
            }
        }

	}
	
    /**
     * Create a new sensor status message.
     *
     * @param isError If the status is an error
     * @param sensor The sensor reporting the status
     * @param sensorName - the name of the sensor reporting the status
     * @param sensorType - the type of sensor reporting the status
     * @param message - the status message
     */
    public void createSensorStatus(boolean isError, AbstractSensor sensor, String sensorName, SensorTypeEnum sensorType, String message) {

        SensorModule module = eventProducerToModule.get(sensor);

        if (module != null) {

            module.sendSensorStatus(isError, sensorName, sensorType, message);

        } else {

            logger.error("Unable to find a sensor module for " + sensor + ", therefore the sensor status event will not be sent externally");
        }
    }
	
	/**
	 * Handle filter events from the mediator
	 * 
	 * @author mhoffman
	 *
	 */
	private class FilterEventMediatorListener implements EventMediatorListener{

        @Override
        public void notify(Object event) {
            
            fireNotificationSensorFilterData((SensorFilterEvent)event);            
        }
	    
	}
	
	/**
	 * Handle sensor events from the mediator
	 * 
	 * @author mhoffman
	 *
	 */
    private class SensorEventMediatorListener implements EventMediatorListener{

        @Override
        public void notify(Object event) {
            
            fireNotificationSensorData((SensorDataEvent)event);            
        }
        
    }
}
