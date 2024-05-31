/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import java.util.Date;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.sensor.AbstractEventProducer;
import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorManager;
import mil.arl.gift.sensor.SensorModule;
import mil.arl.gift.sensor.filter.AbstractSensorFilter;
import mil.arl.gift.sensor.tools.ModuleUserInterfaceEventListener;
import mil.arl.gift.sensor.tools.ModuleUserInterfaceMgr;

/**
 * This is the base class for all sensors.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractSensor extends AbstractEventProducer{
    
    private static final Double DEFAULT_INTERVAL = 1.0;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractSensor.class);

	/** the sensor name */
	private String name;
	
	/** the sensor tick interval (seconds), i.e. how long the sensor sleeps between checks */
	private Double interval = DEFAULT_INTERVAL;
	
	/** the time when this sensor started ticking */
	private Date startTime;
	
	/** the sensor type */
	private SensorTypeEnum sensorType;

	/** the current sensor state */
	protected volatile SensorStateEnum sensorState = SensorStateEnum.READY;
	
	/** the sensor's filter (optional) */
	private AbstractSensorFilter filter = null;	
    
    /** values used for capturing input from the user via command prompt or module user interface window */
    protected Object userInputMonitor = new Object();
    protected String userInput = null;
	
	/** 
	 * instance of the user interface manager used to communicate messages to the user of the sensor module 
	 * The interface that presents the message is dependant on the way the sensor module is started (learner mode vs power user mode)
	 */
	protected ModuleUserInterfaceMgr systemUserInterface = ModuleUserInterfaceMgr.getInstance();
	
	/**
	 * Class constructor - parameters are not yet known
     * 
     * @param sensorName - display name of this sensor, can't be null.
     * @param sensorType The type of sensor. can't be null or empty.
     */
	public AbstractSensor(String sensorName, SensorTypeEnum sensorType){
	    setSensorName(sensorName);
        setSensorType(sensorType);
	}
	
	/**
	 * Class constructor - set common attributes
	 * 
	 * @param sensorName - display name of this sensor, can't be null.
	 * @param interval - the sensor tick interval (seconds), i.e. how long the sensor sleeps between checks.  Must be a positive value.
	 * @param sensorType - the type of sensor. can't be null.
	 */
	public AbstractSensor(String sensorName, double interval, SensorTypeEnum sensorType){
		setSensorName(sensorName);
		setSensorInterval(interval);
		setSensorType(sensorType);
		sensorState = SensorStateEnum.READY;
	}
	
	/**
	 * Test the sensor.  This might involve instantiating the connection the the sensor's hardware, in addition
	 * to reading data from the sensor to make sure it is active and ready to be used.
     * 
     * @return boolean If the sensor is active and ready to use
     */
	public abstract boolean test();
	
	/**
	 * Return the sensor name
	 * 
	 * @return the authored name of this sensor instance.
	 */
	public String getSensorName() {
		return name;
	}
	
	/**
	 * Set the sensor name
	 * 
	 * @param name - the display name, can't be null or empty.
	 */
	private void setSensorName(String name){
	    
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("the sensor name is null or empty");
        }
        
	    this.name = name;
	}
	
	/**
	 * Set the interval at which the sensor is checked
	 * 
	 * @param interval amount of time in seconds between checking for sensor data.  Must be a positive number.
	 */
	public void setSensorInterval(double interval){
	    
	    if(interval < 0){
            throw new IllegalArgumentException("the sensor interval cant be less than zero");
	    }
	    
	    this.interval = interval;
	}

	/**
	 * Return how long the sensor should sleep between checks
	 * 
	 * @return double - sensor tick interval (sec)
	 */
	public Double getSensorInterval() {
		return interval;
	}
	
	/**
	 * Return the sensor start time
	 * 
	 * @return Date
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Set this sensor's type.
	 * 
	 * @param sensorType can't be null.
	 */
	private void setSensorType(SensorTypeEnum sensorType){
	    
	    if(sensorType == null){
	        throw new IllegalArgumentException("the sensor type can't be null.");
	    }
	    
	    this.sensorType = sensorType;
	}

	/**
	 * Return the sensor type
	 * 
	 * @return SensorTypeEnum
	 */
	public SensorTypeEnum getSensorType() {
		return sensorType;
	}
	
	/**
	 * Return the sensor state
	 * 
	 * @return SensorStateEnum - sensor state
	 */
	public SensorStateEnum getSensorState() {
		return sensorState;
	}
	
	/**
	 * Return the sensor's filter instance
	 * Note: this can be null since a filter is not required
	 * 
	 * @return AbstractSensorFilter
	 */
	public AbstractSensorFilter getSensorFilter(){
	    return filter;
	}
	
	/**
	 * Set the sensor's filter instance
	 * 
	 * @param filter - the sensor filter to use
	 */
	public void setSensorFilter(AbstractSensorFilter filter){
	    this.filter = filter;
	}
    
    @Override
	public void start(long domainSessionStartTime) throws Exception{	    
	    super.start(domainSessionStartTime);
	}
	
    @Override
	public void stop(){	    
	    super.stop();
	}	
    
    /**
     * provide a sensor data event to the filter and writer instances
     * 
     * @param data - sensor data
     */
    protected void sendDataEvent(AbstractSensorData data){
        SensorManager.getInstance().createSensorDataEvent(this, data);
    }
    
    @Override
    protected void writerFileCreated(String fileName){
        SensorModule sModule = SensorManager.getInstance().getProducerSensorModule(this);
        sModule.sensorDataFileCreatedNotification(fileName, this);
    }

    /**
     * Create a new sensor status.
     *
     * @param message - the status message
     */
    protected void createSensorStatus(String message) {
        SensorManager.getInstance().createSensorStatus(false, this, getSensorName(), getSensorType(), message);
    }

    /**
     * Create a new sensor error.
     *
     * @param message - the error message
     */
    protected void createSensorError(String message) {
        SensorManager.getInstance().createSensorStatus(true, this, getSensorName(), getSensorType(), message);
    }
    
    
    /**
     * Display the specified message to the user in the appropriate interface as controlled by the sensor module.
     * 
     * @param message the content to display to the user
     * @param eventListener a callback used to provide responses from the user to the message.  Can be null if no notification is warranted.
     */
    protected void displayMessageToUser(String message, ModuleUserInterfaceEventListener eventListener){
        
        try {
            systemUserInterface.displayMessage(getSensorName(), message, eventListener);
        } catch (Exception e) {
            logger.error("Caught exception while trying to display message of "+message+" for "+getSensorName()+".", e);
            JOptionPane.showMessageDialog(null, message, 
                    getSensorName() + " - Failed to display message", 
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Failed to properly display message");
        }        
    }
    
    /**
     * Sets the user provided input from the appropriate user interface and notifies any waiting 
     * threads that a response has been received.
     * 
     * @param input the input provided by the user.  Can be empty or null.
     */
    protected void setUserInput(String input){
        logger.info("Received user input text of '"+input+"'.");
        this.userInput = input;
        
        synchronized(userInputMonitor){
            logger.info("Notifying that user input was received and set");
            userInputMonitor.notifyAll();
        }
    }
	
	@Override
	public String toString(){
		
	    StringBuilder sb = new StringBuilder();
	    sb.append("name = ").append(getSensorName());
	    sb.append(", interval = ").append(getSensorInterval());
	    sb.append(", state = ").append(getSensorState());
	    sb.append(", filter = ").append(getSensorFilter());
		return sb.toString();
	}
}
