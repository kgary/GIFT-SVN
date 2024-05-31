/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.sensor.Filter;
import generated.sensor.Filters;
import generated.sensor.Sensor;
import generated.sensor.Sensors;
import generated.sensor.SensorsConfiguration;
import generated.sensor.Writer;
import generated.sensor.Writers;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.course.SensorFileValidationException;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;
import mil.arl.gift.sensor.filter.AbstractSensorFilter;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.writer.AbstractWriter;

/**
 * This class will parse the sensor configuration file and create a sensor
 * module. The sensor module will contain created instances of sensors, filters
 * and writers which are presented in the file. In addition the relationships
 * between consumers and producers of sensor data will be linked accordingly.
 *
 * @author mhoffman
 *
 */
public class SensorsConfigFileHandler extends AbstractSchemaHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SensorsConfigFileHandler.class);
    
    /** the sensor config file to parse */
    protected FileProxy file;

    //TODO: rework to allow a consumer to have multiple producers (filter consumes filter and sensor data), 
    //      this map only allows for consumer to producer relationship of many to 1.
    /** used as a lookup mechanism for event producer instances */
    private Map<Integer, AbstractEventProducer> consumerIdToEventProducer = new HashMap<Integer, AbstractEventProducer>();

    /** list of event producers (i.e. sensors and filters) for the sensor config
     * being parsed */
    private List<AbstractEventProducer> eventProducers = new ArrayList<AbstractEventProducer>();

    /**
     * Class constructor - set attribute(s)
     *
     * @param sensorConfigFile - the sensor configuration file to parse
     * @throws SensorFileValidationException if there was a problem parsing the configuration file
     */
    public SensorsConfigFileHandler(FileProxy sensorConfigFile) throws SensorFileValidationException {
        super(SENSOR_SCHEMA_FILE);

        if (sensorConfigFile == null) {
            throw new IllegalArgumentException("The sensor config file can't be null");
        }

        file = sensorConfigFile;
        
        configureEventProducers();
    }

    /**
     * Return the name of the sensor configuration file being used to build the
     * sensor module.
     *
     * @return String
     */
    public String getConfigurationFileName() {
        return file.getFileId();
    }
    
    /**
     * Return the list of event producers (i.e. sensors and filters) for the sensor config
     * being parsed
     * 
     * @return List<AbstractEventProducer>
     */
    public List<AbstractEventProducer> getEventProducers(){
        return eventProducers;
    }

    /**
     * Parse and validate the sensor configuration file and create the specified sensors,
     * filters, writers.  This doesn't test the sensors, only checks that the contents are:
     * 1) schema valid
     * 2) appropriate references are authored 
     * 3) sensors, filters and writer implementation classes exist and can be configured
     * 4) that when executing this configuration, every sensor raw data stream is sent either to disk or with a GIFT message
     *
     * @throws SensorFileValidationException Thrown then the sensor configuration file
     * cannot be validated
     */
    private void configureEventProducers() throws SensorFileValidationException {

        SensorsConfiguration sConfig = null;
        try {
            UnmarshalledFile uFile = parseAndValidate(SensorsConfiguration.class, file.getInputStream(), true);
            sConfig = (SensorsConfiguration)uFile.getUnmarshalled();
        } catch (Exception e) {
            throw new SensorFileValidationException("Failed to configure the sensors.", "The sensor configuration file is not schema valid", file.getFileId(), e);
        }

        buildEventProducers(sConfig);
        verifyConfiguration();
        if(logger.isInfoEnabled()){
            logger.info("Successfully configured sensor event producers using configuration file named " + file.getFileId());
        }
    }
    
    /**
     * Test the sensors authored in this configuration file. 
     * This might involve instantiating the connection the the sensor's hardware, in addition to reading data from the 
     * sensor to make sure it is active and ready to be used.
     * Note: if the sensors being tested are not going to be used soon during course execution then the caller should
     * dispose of all event producers so that there aren't issue with accessing the sensor later (e.g. port in use exception).
     * 
     * @throws SensorFileValidationException if there was a problem testing any of the sensors
     */
    public void testSensors() throws SensorFileValidationException{
        
        for(AbstractEventProducer eProducer : eventProducers){
            
            if(eProducer instanceof AbstractSensor){
                
                if(!((AbstractSensor)eProducer).test()){
                    //test failed
                    throw new SensorFileValidationException("Sensor failed test", 
                            ((AbstractSensor)eProducer).getSensorName() + " failed test.", file.getFileId(), null);
                }else{
                    logger.info(((AbstractSensor)eProducer).getSensorName() + " sensor testing completed successfully");  
                }
            }
        }
        
        if(logger.isInfoEnabled()){
            logger.info("All sensor testing completed successfully");
        }
    }
    
    /**
     * Check that every sensor known to this sensor module has its data written to a file
     * and sent over the network.  It could be that a sensor filter is responsible for sending
     * the sensor data over the network.  If a sensor has a filter than the filtered data either
     * needs to be sent over the network.
     * 
     * @param producers - the collection of event producers to validate
     * @throws SensorFileValidationException if there was a problem detected when verifying the configuration
     */
    private void verifyConfiguration() throws SensorFileValidationException{
        
        boolean foundASensor = false;
        
        for(AbstractEventProducer producer : eventProducers){
            
            if(producer instanceof AbstractSensor){
                foundASensor = true;
                
                AbstractSensor sensor = (AbstractSensor)producer;
                
//                //Required: raw data written to file
//                if(sensor.getEventWriter() == null){
//                    passed = false;
//                    logger.error("Sensor Module configuration failed: Found a sensor without an event writer -> "+sensor);
//                    break;
//                }               
                    
                
                if(sensor.getSensorFilter() != null && !sensor.getSensorFilter().shouldDistributeExternally()){  
                    //Required: If sensor has filter, the filter must send network events
                    throw new SensorFileValidationException("Sensor filtered data is not configured to be communicated correctly", 
                            "Sensor filter data must be communicated to other parts of GIFT.  The sensor named '"+sensor.getSensorName()+"' references sensor filter named '"+sensor.getSensorFilter().getFilterName()+"' which is not distributing events externally.", file.getFileId(), null);                    
                
                }else if(sensor.getSensorFilter() == null && !sensor.shouldDistributeExternally()){
                    //Required: If sensor has no filter, the raw data must be sent over the network
                    throw new SensorFileValidationException("Sensor raw data is not configured to be communicated correctly", 
                            "Sensor raw data must be communicated to other parts of GIFT.  The sensor named '"+sensor.getSensorName()+"' has NO sensor filter, therefore it must distribute events externally.", file.getFileId(), null);                    
                }
                

            }
        }
        
        if(!foundASensor){
            throw new SensorFileValidationException("No sensors found", "There must be at least 1 sensor defined in the configuration file.  If you want 0 sensors than don't specify a sensor configuration file.", file.getFileId(), null);
        }
        
    }

    /**
     * Build the event producers by analyzing the content of the sensor config.
     *
     * @param sConfig - the sensor config content
     * @throws SensorFileValidationException if there was a severe problem building the event producers
     */
    private void buildEventProducers(SensorsConfiguration sConfig) throws SensorFileValidationException {

        buildSensors(sConfig.getSensors());
        buildFilters(sConfig.getFilters());
        buildWriters(sConfig.getWriters());
    }

    /**
     * Build the writers associated with this sensor module
     *
     * @param writers - the sensor config list of writers
     * @throws SensorFileValidationException if there was a problem building the sensor writers
     */
    private void buildWriters(Writers writers) throws SensorFileValidationException {

        for (Writer writer : writers.getWriter()) {

            //get event producer this writer wants to consumer data from
            AbstractEventProducer producer = consumerIdToEventProducer.get(writer.getId().intValue());

            if (producer == null) {
                throw new SensorFileValidationException("Unused sensor writer", "The sensor writer named '"+writer.getName()+"' with id '"+writer.getId().intValue()+"' is not used by any sensor or filter.  Every writer must be used at least once.", file.getFileId(), null);
            }

            //
            // Create Instance of writer class
            //
            AbstractWriter giftWriter = null;
            Object inputObj = null;
            String writerClassName = null;
            try {

                //first - the implementation class name
                writerClassName = writer.getWriterImpl();

                //second - the input object, if any
                inputObj = writer.getWriterInput().getType();

                //third - get constructor with input object as only parameter and instantiate writer
                Constructor<?> constructor;
                if (inputObj != null) {
                    
                    try{
                        constructor = Class.forName(PackageUtil.getRoot() + "." + writerClassName).getConstructor(inputObj.getClass(), EventProducerInformation.class);
                        giftWriter = (AbstractWriter) constructor.newInstance(inputObj, producer.getEventProducerInformation());
                    } catch (NoSuchMethodException nsme){
                        throw new SensorFileValidationException("Incorrect class definition for sensor writer.", "A 'No Such Method' Exception was thrown while trying to instantiate writer class: "+writerClassName+"" +
                                ".  The most likely cause of this error is that the class doesn't have a constructor with parameter(s): "+inputObj.getClass(), file.getFileId(), nsme);
                    }
                    
                } else {
                    
                    try{
                        constructor = Class.forName(PackageUtil.getRoot() + "." + writerClassName).getConstructor(EventProducerInformation.class);
                        giftWriter = (AbstractWriter) constructor.newInstance(producer.getEventProducerInformation());
                    } catch (NoSuchMethodException nsme){
                        throw new SensorFileValidationException("Incorrect class definition for sensor writer.", "A 'No Such Method' Exception was thrown while trying to instantiate sensor writer class: "+writerClassName+"" +
                                ".  The most likely cause of this error is that the class doesn't have a constructor with NO parameter(s).", file.getFileId(), nsme);
                    }
                }

                //set producer's writer instance
                producer.setEventWriter(giftWriter);

            } catch (Exception e) {
                throw new SensorFileValidationException("Failed to instantiate the writer named '"+writer.getName()+"' with id "+writer.getId().intValue()+".", "An exception occurred while configuring the writer implementation class.", file.getFileId(), e);
            } catch (Throwable t){
                //catching higher level issues
                logger.error("caught exception while trying to instantiate the writer named '"+writer.getName()+"' because the implementation class with unique sensor configuration file element id = " + writer.getId(), t);
                throw new SensorFileValidationException("Failed to instantiate the the writer named '"+writer.getName()+"' with id '"+writer.getId().intValue()+"'.", 
                        "An error occurred while configuring the writer implementation class.  The message reads:\n"+t.getMessage()+".", file.getFileId(), null);
            }
        }

    }

    /**
     * Build the filters associated with this sensor module
     *
     * @param filters - the sensor config list of filters
     * @throws SensorFileValidationException if there was a problem building the filters
     */
    private void buildFilters(Filters filters) throws SensorFileValidationException{

        for (Filter filter : filters.getFilter()) {

            //get event producer this filter wants to consumer data from
            AbstractEventProducer producer = consumerIdToEventProducer.get(filter.getId().intValue());

            if (producer == null) {
                throw new SensorFileValidationException("Unused sensor filter", "The sensor filter named '"+filter.getName()+"' with id '"+filter.getId().intValue()+"' is not used by any sensor.  Every filter must be used at least once.", file.getFileId(), null);
            }

            //
            // Create Instance of filter class
            //
            AbstractSensorFilter giftFilter = null;
            try {

                //first - the implementation class name
                String filterClassName = filter.getFilterImpl();

                //second - the input object, if any
                Object inputObj = filter.getFilterInput().getType();

                //third - get constructor with input object as only parameter and instantiate filter
                Constructor<?> constructor;
                if (inputObj != null) {
                    
                    try{
                        constructor = Class.forName(PackageUtil.getRoot() + "." + filterClassName).getConstructor(inputObj.getClass());
                        giftFilter = (AbstractSensorFilter) constructor.newInstance(inputObj);
                    } catch (NoSuchMethodException nsme){
                        throw new SensorFileValidationException("Incorrect class definition for sensor filter.", "A 'No Such Method' Exception was thrown while trying to instantiate sensor filter class: "+filterClassName+"" +
                                ".  The most likely cause of this error is that the class doesn't have a constructor with parameter(s): "+inputObj.getClass(), file.getFileId(), nsme);
                    }
                    
                } else {
                    
                    try{
                        constructor = Class.forName(PackageUtil.getRoot() + "." + filterClassName).getConstructor();
                        giftFilter = (AbstractSensorFilter) constructor.newInstance();
                    } catch (NoSuchMethodException nsme){
                        throw new SensorFileValidationException("Incorrect class definition for sensor filter.", "A 'No Such Method' Exception was thrown while trying to instantiate sensor filter class: "+filterClassName+"" +
                                ".  The most likely cause of this error is that the class doesn't have a constructor with NO parameter(s).", file.getFileId(), nsme);
                    }
                }

                giftFilter.setName(filter.getName());
                giftFilter.setDistributeExternally(Boolean.valueOf(filter.getDistributeExternally().value()));
                
                if(giftFilter.getEventProducerInformation() == null || giftFilter.getEventProducerInformation().getAttributes().isEmpty()){
                    //the filter has no attributes of its own, it is relying on the raw sensor data attributes to filter on
                    //Example: the generic sensor filter doesn't add any attributes it merely filters existing/incoming attributes
                    giftFilter.setEventProducerInformation(producer.getEventProducerInformation());
                }

                ((AbstractSensor) producer).setSensorFilter(giftFilter);

                //add to collection being built
                eventProducers.add(giftFilter);
                
            } catch (Exception e) {
                throw new SensorFileValidationException("Failed to instantiate the filter named '"+filter.getName()+"' with id "+filter.getId().intValue()+".", "An exception occurred while configuring the sensor implementation class.", file.getFileId(), e);
            } catch (Throwable t){
                //catching higher level issues
                logger.error("caught exception while trying to instantiate the filter named '"+filter.getName()+"' because the implementation class with unique sensor configuration file element id = " + filter.getId(), t);
                throw new SensorFileValidationException("Failed to instantiate the the filter named '"+filter.getName()+"' with id '"+filter.getId().intValue()+"'.", 
                        "An error occurred while configuring the filter implementation class.  The message reads:\n"+t.getMessage()+".", file.getFileId(), null);
            }

            //
            // Writer is optional
            //            
            if (filter.getWriterInstance() != null) {
                int consumerId = filter.getWriterInstance().intValue();

                addConsumerMapping(consumerId, giftFilter);
            }
        }

    }

    /**
     * Build the sensors associated with this sensor module.
     *
     * @param sensors - the sensor config list of sensors
     * @throws SensorFileValidationException if there was a problem building the sensors
     */
    private void buildSensors(Sensors sensors) throws SensorFileValidationException {

        for (Sensor sensor : sensors.getSensor()) {

            //
            // Create Instance of sensor class
            //
            AbstractSensor giftSensor = null;            

            //first - the implementation class name
            String sensorClassName = sensor.getSensorImpl();
            try {

                //second - the input object, if any
                Serializable inputObj = sensor.getSensorInput().getType();

                //third - get constructor with input object as only parameter and instantiate sensor
                Constructor<?> constructor;
                if (inputObj != null) {
                    constructor = Class.forName(PackageUtil.getRoot() + Constants.PERIOD + sensorClassName).getConstructor(String.class, inputObj.getClass());
                    giftSensor = (AbstractSensor) constructor.newInstance(sensor.getName(), inputObj);
                } else {
                    constructor = Class.forName(PackageUtil.getRoot() + Constants.PERIOD + sensorClassName).getConstructor(String.class);
                    giftSensor = (AbstractSensor) constructor.newInstance(sensor.getName());
                }

                if (sensor.getInterval() != null) {
                    giftSensor.setSensorInterval(sensor.getInterval().doubleValue());
                }

                giftSensor.setDistributeExternally(Boolean.valueOf(sensor.getDistributeExternally().value()));

                //add to collection being built
                eventProducers.add(giftSensor);

            } catch (Exception e) {
                throw new SensorFileValidationException("Failed to instantiate the '"+sensor.getName()+"' sensor", 
                        "An exception occurred while configuring the sensor implementation class of '"+sensorClassName+"'.\n"+
                        "Does that class have two constructors, one with a string parameter and a second with a string parameter plus a Serializable parameter?", file.getFileId(), e);
            } catch (Throwable t){
                //catching higher level issues
                logger.error("caught exception while trying to instantiate '"+sensor.getName()+"' sensor implementation class with unique sensor configuration file element id = " + sensor.getId(), t);
                throw new SensorFileValidationException("Failed to instantiate the '"+sensor.getName()+"' sensor", 
                        "An error occurred while configuring the sensor implementation class.  The message reads:\n"+t.getMessage()+".", file.getFileId(), null);
            }

            //
            // filter and writer are optional
            //

            if (sensor.getFilterInstance() != null) {

                int consumerId = sensor.getFilterInstance().intValue();

                addConsumerMapping(consumerId, giftSensor);
            }

            if (sensor.getWriterInstance() != null) {

                int consumerId = sensor.getWriterInstance().intValue();

                addConsumerMapping(consumerId, giftSensor);
            }
        }
        
        if(sensors.getSensor().isEmpty()){
            throw new SensorFileValidationException("No sensors found", "There must be at least 1 sensor defined in the configuration file.  If you want 0 sensors than don't specify a sensor configuration file.", file.getFileId(), null);
        }

    }

    /**
     * Add the consumer id to event producer relationship to the map for
     * retrieval later.
     *
     * @param consumerId - a consumer unique id
     * @param producer - the producer of events for the consumer with that id
     */
    private void addConsumerMapping(Integer consumerId, AbstractEventProducer producer) {

        if (consumerIdToEventProducer.containsKey(consumerId)) {
            logger.error("consumer id of '" + consumerId + "' is already mapped to event producer = " + producer);
        } else {
            consumerIdToEventProducer.put(consumerId, producer);
        }
    }
}
