/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.sensor.SensorError;
import mil.arl.gift.common.sensor.SensorFileCreated;
import mil.arl.gift.common.sensor.SensorStatus;
import mil.arl.gift.common.sensor.UnfilteredSensorData;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.sensor.filter.AbstractSensorFilter;
import mil.arl.gift.sensor.filter.SensorFilterEvent;
import mil.arl.gift.sensor.filter.SensorFilterEventListener;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.tools.ModuleUserInterfaceMgr;

/**
 * This class is the Sensor Module which is responsible for a sensor.
 * Upon receiving sensor data or sensor filter data events, a message will be created
 * with the event's information and sent over the network.
 * 
 * @author mhoffman
 *
 */
public class SensorModule extends AbstractModule implements SensorDataEventListener, SensorFilterEventListener  {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SensorModule.class);
    
    private static final String SENSOR_MODULE_PREFIX = "SensorModule:";
    
	static{
		//use sensor log4j
		PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/sensor/sensor.log4j.properties");
	}
	
	/** the list of event producers (sensors and filters) for this sensor module */
	private List<AbstractEventProducer> producers = new ArrayList<AbstractEventProducer>();
    
    /** the sensors/filters in this module are associated with this learner's unique user session info */
    private UserSession userSession = null;
    
    /** the current domain session for the user */
    private Integer domainSessionId = null;
    
    /** the current experiment ID */
    private String experimentID = null;
    
    /** container for sensor's last status message {key: sensor name, value: last status message} */
    private Map<String, SensorStatusInstance> sensorToLastError = new HashMap<>();
    
    /** convert seconds to milliseconds one time and reuse */
    private static final long MIN_MS_BTW_ERROR_MSG = (long) (SensorModuleProperties.getInstance().getMinSecBtwErrors() * 1000);
    		
	/**
	 * Class constructor 
	 */
	public SensorModule(){
		super(SENSOR_MODULE_PREFIX, SubjectUtil.SENSOR_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr, SubjectUtil.SENSOR_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, SensorModuleProperties.getInstance());
		
		init();
	}
	
	@Override
    protected int getMaxNumberOfAllocations(){
        return 1;
    }
	
	@Override
    public void updateAllocationStatus(UserSession userSession){
	    
	    //
	    // (If no sensors) The sensor module can be allocated to unlimited users
	    // (If 1 or more sensors) The sensor module can be allocated to 1 user.  Here are the checks for that case:
	    //   1. is there a user session for this instance?
	    //   2. does the user session match the incoming request?
	    //
	    boolean allocated = this.getUserId() != null && 
	            userSession != null && 
	            this.getUserId() != userSession.getUserId() && 
	            SensorManager.getInstance().getNumberOfSensors() != 0;
	    if(logger.isInfoEnabled()){
	        logger.info("Updating allocation status fully allocated value to "+allocated+" based on current user id of "+this.getUserId()+" and requesting allocation for user of "+userSession+".");
	    }
	    allocationStatus.setFullyAllocated(allocated);
    }
	
	@Override
	public void allocateToUser(UserSession userSession) throws Exception{
	    
	    if(SensorManager.getInstance().getNumberOfSensors() != 0){
	        setUserSessionInfo(userSession);
	    }else{
	        if(logger.isInfoEnabled()){
	            logger.info("Skipping sensor module allocation to user "+userSession+" because the sensor module has no sensors.");
	        }
	    }
	}
	
	/**
	 * Return the user id for the sensors/filters in this module that are associated with a learner
	 * 
	 * @return Integer
	 */
	public Integer getUserId(){
		return userSession != null ? userSession.getUserId() : null;
	}
	
	/**
	 * Return information about the user session this sensor module is currently working with.
	 * 
	 * @return UserSession - information about the user session.  Can be null if this sensor module is
	 *                 not currently allocated for use in a user session.
	 */
	public UserSession getUserSessionInfo(){
	    return userSession;
	}
	
	/**
	 * Set the user information for the sensors/filters in this module that are associated with a learner
	 * 
	 * @param userSession can be null to indicate this module is no longer assigned to a learner
	 * @throws OperationNotSupportedException 
	 */
	private void setUserSessionInfo(UserSession userSession) throws OperationNotSupportedException{
	    
	    if(this.getUserId() != null && userSession != null && this.getUserId() == userSession.getUserId()){
	        //no-change in the user this module is allocated too already
	        return;
	    }
	    
	    if(userSession != null && this.getUserId() != null){
	        throw new OperationNotSupportedException("Can't change the user id for the sensor module from "+this.getUserId()+" to "+userSession+" without closing the domain session first");
	    }
	    
	    if(logger.isInfoEnabled()){
	        logger.info("Changing user id from "+this.getUserId()+" to "+userSession);
	    }
	    this.userSession = userSession;
	}
	
	
	
	/**
	 * Return the domain session id for the sensors/filters in this module
	 * 
	 * @return int
	 */
	public int getDomainSessionId(){
		return domainSessionId;
	}
	
	/**
	 * Set the domain session id for the sensors/filters in this module
	 * 
	 * @param domainSessionId
	 */
	private void setDomainSessionId(Integer domainSessionId){
	    this.domainSessionId = domainSessionId;
	}
	
	/**
	 * Set domain session information for testing purposes
	 * 
	 * @param userId - the user id to use for the sensors/filters in this module
	 * @param domainSessionId - the domain session id to use for the sensors/filters in this module
	 * @throws OperationNotSupportedException - if there was an issue setting the ids
	 */
	public void setTestIds(int userId, int domainSessionId) throws OperationNotSupportedException{
		setUserSessionInfo(new UserSession(userId));
		setDomainSessionId(domainSessionId);
	}
	
	/**
	 * Perform initialization logic for the module
	 */
    @Override
	public void init(){
		
		//create client to send sensor status too
		createSubjectTopicClient(SubjectUtil.SENSOR_DISCOVERY_TOPIC, false);
		
		//create client to send any sensor statuses too
		createSubjectTopicClient(SubjectUtil.SENSOR_STATUS_TOPIC, false);
		
		//start the module heartbeat
		initializeHeartbeat();

	}
	

    @Override
    protected void handleMessage(Message message) {

    	MessageTypeEnum type = message.getMessageType();
		if(type == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST){
            handleInitializeDomainSessionRequest(message);
			
		}else if(type == MessageTypeEnum.START_DOMAIN_SESSION){
            handleStartDomainSession(message);
			
		}else if(type == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST){
            handleCloseDomainSession(message);

        } else if (type == MessageTypeEnum.KILL_MODULE) {
            handleKillModuleMessage(message);
			
		} else {

            logger.error(getModuleName() + " received unhandled message:" + message);
            
            if(message.needsHandlingResponse()) {
                this.sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "The module did not know how to handle the message"), MessageTypeEnum.PROCESSED_NACK);
            }
        }
        
	}
    
    /**
     * Notify sensors and filters to stop processing data 
     */
    private void stopProducers(boolean dispose){
        
        if(logger.isInfoEnabled()){
            logger.info("Stopping event producers");
        }
        
        //synchronize producers in case a producer is being added/started right now
        synchronized (producers) {

            for(AbstractEventProducer eProducer : producers){
                
                try{
                    eProducer.stop();
                    
                    if(dispose){
                        eProducer.dispose();
                    }
                }catch(Exception e){
                    logger.error("While trying to stop event producers, caught exception from mis-behaving event producer = "+eProducer, e);
                }
            }
        }
    }
	
	/**
	 * Handle the close domain session request message by ...
	 * 
	 * @param message - the close domain session request message
	 */
	private void handleCloseDomainSession(Message message){
		
	    if(logger.isInfoEnabled()){
	        logger.info(getModuleName() + " received message: " + message);
	    }
        
        stopProducers(false);            
        
        try{
            if(getUserId() != null){
                releaseDomainSessionModules(userSession);
            }
            
            //reset
            setUserSessionInfo(null);
            setDomainSessionId(null);
            setExperimentID(null);
            
        }catch(Exception e){
            logger.error("Caught exception while trying to close the domain session", e);
        }
		
        //send ACK
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);       
	}
	
	/**
	 * Handle the start domain session request message by ...
	 * 
	 * @param message - the start domain session request message
	 */
	private void handleStartDomainSession(Message message){
		
	    if(logger.isInfoEnabled()){
	        logger.info(getModuleName() + " received message: " + message.toString());
	    }
        
        //let UMS know that a sensor data file was created
        //TODO: design better mechanism to link sensor instance, sensor module, filter(s), writer(s)
        //      Maybe there needs to be a more direct link between sensor, filter and writer instead of allowing
        //      a compose-able system as it is now.
        
        try{
            
            //synchronize producers in case a producer is being added/removed while attempting to start producers
            synchronized (producers){
                
                //notify event producers that a domain session is being started
                for(AbstractEventProducer eProducer : producers){
                    
                    try{
                        if(logger.isInfoEnabled()){
                            logger.info("Starting event producer of "+eProducer+".");
                        }
                        eProducer.start(message.getTimeStamp());
                    }catch(Exception e){
                        throw new Exception("While trying to start domain session, caught exception from mis-behaving event producer = "+eProducer+".", e);
                    }
                }
            }
            
            //send ACK
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            
        }catch(Exception e){
            logger.error("Caught exception while trying to start a domain session", e);
            sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Unable to succesfully start all event producers in the sensor module.  Check the sensor module log for more information."), MessageTypeEnum.PROCESSED_NACK);
        }
	}
	
	/**
	 * Handle the initialize domain session request message by starting the sensor(s)
	 * and replying to the request.
	 * 
	 * @param message - the initialize domain session request message
	 */
	private void handleInitializeDomainSessionRequest(Message message){
		
	    if(logger.isInfoEnabled()){
	        logger.info(getModuleName() + " received message: " + message.toString());
	    }

		//get current user id and domain session id
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        try{
            //MH 11/6: Setting the user id is part of the module allocation process
//            setUserId(domainSessionMessage.getUserId());
            setDomainSessionId(domainSessionMessage.getDomainSessionId());
            setExperimentID(domainSessionMessage.getExperimentId());
            
            //send ACK
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            
        }catch(Exception e){
            logger.error("Caught exception while trying to set the domain session information", e);
            
            sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Unable to initialize the sensor module for the "+
                    "domain session because there was an exception thrown when trying to set the domain session information"), MessageTypeEnum.PROCESSED_NACK);
        }

	}

    /**
     * Handles the message to kill this module
     *
     * @param message - The Kill Module message
     */
    private void handleKillModuleMessage(Message message) {
        Thread killModule = new Thread("Kill Module"){
            @Override
            public void run() {
                killModule();
            }            
        };
        killModule.start();
    }
    
    /**
     * Add the event producer to the container of event producers for this sensor module
     * 
     * @param producer - an event producer (e.g. sensor, filter) instance
     */
    private void addEventProducer(AbstractEventProducer producer){
        if(logger.isInfoEnabled()){
            logger.info("Adding event producer of "+producer+" to collection of known producers.");
        }
        producers.add(producer);
    }
    
    /**
     * Remove the event producer from the container of event producers for this sensor module
     * 
     * @param producer - an event producer (e.g. sensor, filter) instance
     */
    private void removeEventProducer(AbstractEventProducer producer){
        
        synchronized (producers) {
            if(logger.isInfoEnabled()){
                logger.info("Removing event producer of "+producer+" to collection of known producers.");
            }
            producers.remove(producer);
        }
    }
    
    /**
     * Add the collection of event producers to this sensor module
     * 
     * @param eProducers - the collection of event producers
     */
    public void addEventProducers(Collection<AbstractEventProducer> eProducers){
        
        //synchronize producers in case a producer is being removed/started while adding these producers
        synchronized (producers){
            
            for(AbstractEventProducer producer : eProducers){
                
                if(producer instanceof AbstractSensor){
                    
                    try{
                        addSensor((AbstractSensor) producer);
                        
                        ((AbstractSensor)producer).test();                
                        
                    }catch(Exception e){
                        logger.error("Caught exception while adding event producer of "+producer+", therefore it won't be used and the sensor module is not fully initialized.", e);
                        removeSensor((AbstractSensor) producer);
                        
                        sendSensorStatus(true, ((AbstractSensor)producer).getSensorName(), ((AbstractSensor)producer).getSensorType(), "Sensor initialize test failed");
                    }
                    
                }else if(producer instanceof AbstractSensorFilter){
                    addFilter((AbstractSensorFilter) producer);
                }else{
                    throw new RuntimeException("Received unhandled event producer of "+producer);
                }
            } 
        }
 
    }
    
    /**
     * Add a sensor to this sensor module
     * 
     * @param sensor - add a new sensor to this module
     */
    private void addSensor(AbstractSensor sensor){
        addEventProducer(sensor);
        
        //register for interest in events
        SensorManager.getInstance().addEventProducerListener(this, sensor);
    }
    
    /**
     * Remove a sensor to this sensor module
     * 
     * @param sensor - sensor to remove from this module
     */
    public void removeSensor(AbstractSensor sensor){
        removeEventProducer(sensor);
        
        //register for interest in events
        SensorManager.getInstance().removeEventProducerListener(sensor);
    }
    
    /**
     * Add a filter to this sensor module
     * 
     * @param filter - add a new filter to this module
     */
    private void addFilter(AbstractSensorFilter filter){
        addEventProducer(filter);

        //register for interest in events
        SensorManager.getInstance().addEventProducerListener(this, filter);
    }
	
	/**
	 * This is the callback method for when a Sensor Filter has produced data.
	 * Create a sensor filter message and send it out over the network.
	 * 
	 * @param elapsedTime - the elapsed domain session time
	 * @param sensorFilterEvent - the sensor's filter data event containing filtered data to send
	 */
    @Override
	public void sensorFilterEvent(long elapsedTime, SensorFilterEvent sensorFilterEvent){
			
		//don't send message if not in a domain session
		if(domainSessionId == null){
			return;
		}

		FilteredSensorData fSensorData = 
		        new FilteredSensorData(sensorFilterEvent.getFilter().getFilterName(), sensorFilterEvent.getSensor().getSensorName(), sensorFilterEvent.getSensor().getSensorType(), sensorFilterEvent.getData().getElapsedTime(), sensorFilterEvent.getData().getSensorFilterAttributeToValue());

		sendDomainSessionMessage(fSensorData, getUserSessionInfo(), domainSessionId, MessageTypeEnum.SENSOR_FILTER_DATA, null);
    }
    
    /**
     * Send a new sensor status message to GIFT.
     * Note: Callers of this method should refrain from sending messages to frequently, especially when the 
     * content is the same.  Ignoring this constraint will add unwanted overhead on other parts of GIFT.
     * 
     * @param isError If the status is an error
     * @param sensorName - the name of the sensor reporting the status
     * @param sensorType - the type of sensor reporting the status
     * @param message - the status message
     */
    public void sendSensorStatus(boolean isError, String sensorName, SensorTypeEnum sensorType, String message) {

        if (sensorName == null || sensorType == null || message == null) {
            return;
        }

        //filter out misbehaving sensor statuses reporting the same status to frequently
        boolean sendMsg = true;
        SensorStatusInstance sStatus = sensorToLastError.get(sensorName);
        if (sStatus == null) {
            sStatus = new SensorStatusInstance(message);
            sensorToLastError.put(sensorName, sStatus);
        } else {

            //check if this is a new status message
            if (sStatus.getMessage().equals(message)
                    && (new Date().getTime() - sStatus.getTime().getTime()) < MIN_MS_BTW_ERROR_MSG) {
                sendMsg = false;

            } else {
                //update for latest status message
                sStatus.update(message);
            }
        }

        if (sendMsg) {
            //send the status to the sensor status topic for all interested parties to receive
            if (haveSubjectClient(SubjectUtil.SENSOR_STATUS_TOPIC)) {

                SensorStatus sensorStatus;
                
                if (isError) {
                    
                    sensorStatus = new SensorError(sensorName, sensorType, message);
                    
                } else {
                    
                    sensorStatus = new SensorStatus(sensorName, sensorType, message);
                }
                
                if (getUserSessionInfo() == null) {
                    //sensor status happened before being assigned to a user
                    sendMessage(SubjectUtil.SENSOR_STATUS_TOPIC, sensorStatus, MessageTypeEnum.SENSOR_STATUS, null);
                } else {
                    sendUserSessionMessage(SubjectUtil.SENSOR_STATUS_TOPIC, sensorStatus, getUserSessionInfo(), MessageTypeEnum.SENSOR_STATUS, null);
                }
            }
        }
    }
	
	/**
	 * This is the callback method for when a Sensor has produced data.
	 * Create a sensor data message and send it out over the network.
	 * 
	 * @param sensorDataEvent - the sensor's data event containing sensor data to send
	 */
    @Override
	public void sensorDataEvent(SensorDataEvent sensorDataEvent){
			
		//don't send message if not in a domain session
		if(domainSessionId == null){
			return;
		}
		
		//Send message for each sensor data
		AbstractSensorData sData = sensorDataEvent.getData();
		if(sData instanceof SensorData) {
			UnfilteredSensorData sMsg = 
					new UnfilteredSensorData(sensorDataEvent.getSensor().getSensorName(),
											 sensorDataEvent.getSensor().getSensorType(),
											 sensorDataEvent.getData().getElapsedTime(),
											 ((SensorData)sData).getSensorAttributeToValue());
              //send it to the learner module
              if(haveSubjectClient(userSession, ModuleTypeEnum.LEARNER_MODULE)){
                sendDomainSessionMessage(sMsg, getUserSessionInfo(), domainSessionId, MessageTypeEnum.SENSOR_DATA, null);
              }
		}else{
		    logger.error("Unable to handle sending sensor data type of "+sData);
		}

	}
	
	/**
	 * Send a message to the UMS module notifying it that a sensor data file was created
	 * 
     * @param fileName - the sensor data file name
     * @param sensor  The sensor the data file was created for
	 */
	public void sensorDataFileCreatedNotification(String fileName, AbstractSensor sensor){
        
        if(fileName == null){
        	logger.error("The file name can't be null for a sensor data file, therefore it will be missing in the UMS database entry for this domain session");
        }else{
        	//send message to UMS
        	SensorFileCreated sensorFileCreated = new SensorFileCreated(fileName, sensor.getSensorType());
        	sendDomainSessionMessage(sensorFileCreated, getUserSessionInfo(), getDomainSessionId(), MessageTypeEnum.SENSOR_FILE_CREATED, null);
        }
	}
	
	/**
	 * Return the list of event producer class(es) for this sensor event listener
	 * 
	 * @return List<AbstractEventProducer> - the classes producing data for this listener to consume
	 */
	public List<AbstractEventProducer> getProducers(){
		return producers;
	}
	
	/**
	 * Return the module type
	 * 
	 * @return ModuleTypeEnum
	 */
    @Override
	public ModuleTypeEnum getModuleType(){
		return ModuleTypeEnum.SENSOR_MODULE;
	}
	
	/**
	 * Create and send a module status message over the network.
	 */
    @Override
	public void sendModuleStatus(){
		sendMessage(SubjectUtil.SENSOR_DISCOVERY_TOPIC, moduleStatus, MessageTypeEnum.MODULE_STATUS, null);
	}

    @Override
    protected void cleanup() {
        super.cleanup();
        
        if(logger.isInfoEnabled()){
            logger.info("stopping producers...");
        }
        stopProducers(true);
        
        if(logger.isInfoEnabled()){
            logger.info("cleaning up module user interface manager...");
        }
        ModuleUserInterfaceMgr.getInstance().cleanup();
        
        if(logger.isInfoEnabled()){
            logger.info("Finished cleaning up sensor module.");
        }
    }
	
    @Override
	public String toString(){
		
        StringBuffer sb = new StringBuffer();
        sb.append("[SensorModule: ");
        sb.append(" name = ").append(getModuleName());
        sb.append(", type = ").append(getModuleType());
        sb.append(", user id = ").append(getUserId());
		
		if(!producers.isEmpty()){
		    sb.append(", producers = ");
		    for(AbstractEventProducer eProducer : producers){
		        sb.append(eProducer).append(", ");
		    }
		}
		
		sb.append("]");
		
		return sb.toString();
	}
    
    /**
     * Wrapper for a sensor status instance including the sensor status message and the
     * time stamp for when that status was reported.
     * 
     * @author mhoffman
     *
     */
    private class SensorStatusInstance{
        
        /** the time at which this status was reported */
        private Date time;
        
        /** the status message content */
        private String message;
        
        public SensorStatusInstance(String message){
            update(message);
        }
        
        public void update(String message){
            this.message = message;
            this.time = new Date();
        }
        
        public Date getTime(){
            return time;
        }
        
        public String getMessage(){
            return message;
        }
    }
	
	/**
     * Used to run the sensor module
     * 
     * @param args - launch module arguments
     */
    public static void main(String[] args) {
    	ModuleModeEnum mode = checkModuleMode(args);
        SensorModuleProperties.getInstance().setCommandLineArgs(args);   
        
        SensorModule sModule = null;
        try{
            SensorsConfigFileHandler configHandler = null;
            String sensorConfigFilename = SensorModuleProperties.getInstance().getSensorConfigFile();
            if(sensorConfigFilename != null){
                FileProxy defaultConfigurationFile = new FileProxy(new File(sensorConfigFilename));
                System.out.println("Reading "+defaultConfigurationFile);
                configHandler = new SensorsConfigFileHandler(defaultConfigurationFile);
                
            }else{                
                logger.warn("There is no sensor configuration file property value, therefore the sensor module will not be configured with any sensors.  To change this configuration, update the "+SensorModuleProperties.SENSOR_CONFIG_FILE+" property in "+SensorModuleProperties.PROPERTIES_FILE+" to point to the appropriate sensor configuration XML file.");
                System.out.println("\nWarning: This sensor module is configured to run with no sensors. To change this configuration, update the "+SensorModuleProperties.SENSOR_CONFIG_FILE+" property in "+SensorModuleProperties.PROPERTIES_FILE+" to point to the appropriate sensor configuration XML file.\n");
            } 
            
            sModule = new SensorModule();             
            sModule.setModuleMode(mode);
            if(configHandler != null){
                Collection<AbstractEventProducer> producers = configHandler.getEventProducers();
                sModule.addEventProducers(producers);
                
                if(sModule.getProducers().size() != producers.size()){
                    //something went wrong
                    throw new Exception("Not all of the producers were created, check the sensor module log for more details.");
                }
            }

            sModule.showModuleStartedPrompt();
            
            //stop event producers, cleanup threads and GUIs, etc.
            sModule.cleanup();

        }catch(Throwable t){
            logger.error("Caught exception while running Sensor module.", t);
            
            try{
                if(mode == ModuleModeEnum.LEARNER_MODE){
                    //this mode doesn't have a console to print too
                    
                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null,
                                    "The Sensor Module had a severe error.\nThe error reads:\n"+t.getMessage()+"\n\nFor more information check the latest sensor module log file for more information.",
                                    "Sensor Module Error",
                                    JOptionPane.ERROR_MESSAGE);                        
                        }
                    });
    
                }else{
                    System.err.println("The sensor module threw an exception.");
                    t.printStackTrace();
                    
                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null,
                                    "The Sensor Module had a severe error.\nThe error reads:\n"+t.getMessage()+"\n\nFor more information check the sensor module log file and the console window for more information.",
                                    "Sensor Module Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            }catch(@SuppressWarnings("unused") Exception e){
                //what do we do now, there was a problem just showing a dialog to the user
            }
            
            //stop event producers, cleanup threads and GUIs, etc.
            if(sModule != null){
                sModule.cleanup();
            }
            
            //this allows the SPL to be launched again w/o having to wait for SPL logic to timeout
            showModuleUnexpectedExitPrompt(mode);
        }       
        
    	if(mode == ModuleModeEnum.POWER_USER_MODE) {
            System.out.println("Good-bye");
            //kill any threads
            System.exit(0);
    	}
    }

    /**
     * Return the experiment id for the sensors/filters in this module.
     * experimentID can be null
     * 
     * @return String
     */
    public String getExperimentID() {
        return experimentID;
    }

    /**
     * Set the experiment id for the sensors/filters in this module
     * 
     * @param experimentID can be null
     */
    public void setExperimentID(String experimentID) {
        this.experimentID = experimentID;
    }
}
