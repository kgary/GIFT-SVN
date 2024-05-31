/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.writer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.ComplexAttributeFieldManager;
import mil.arl.gift.common.sensor.ComplexAttributeFieldManager.ComplexAttributeField;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerAttribute;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;
import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataCollection;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.filter.SensorFilterData;
import mil.arl.gift.sensor.filter.SensorFilterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for writing sensor and/or filtered data to a file using a string delimited encoding.
 * 
 * @author mhoffman
 *
 */
public class GenericSensorDelimitedWriter extends AbstractFileWriter{

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GenericSensorDelimitedWriter.class);
    private static boolean isDebugEnabled = logger.isDebugEnabled();
	
    /** format used for attribute values */
    private static final DecimalFormat attributeFormat = new DecimalFormat("#.##");
	protected static final String DELIM 	= ",";
	
	/** parameters to configure this writer */
	protected generated.sensor.GenericSensorDelimitedWriter configuration;
	
	/** used to replace the delimiter character contained within a sensor data value to prevent false delimiters */
	private static final String DEFAULT_DELIM_REPLACEMENT = "|";
	protected String delimReplacement = DEFAULT_DELIM_REPLACEMENT;
	
	/**
	 * Class constructor - configure using the writer configuration input for this writer
	 * 
     * @param configuration parameters to configure this writer
     * @param eventProducerInformation attributes for which data will be written for 
	 */
	public GenericSensorDelimitedWriter(generated.sensor.GenericSensorDelimitedWriter configuration, EventProducerInformation eventProducerInformation){
		super(configuration.getFilePrefix(), CSV_EXTENSION, eventProducerInformation);
		
		this.configuration = configuration;
		
		if(configuration.getDatumDelimReplacementChar() != null){
		    delimReplacement = configuration.getDatumDelimReplacementChar();
		}
		
		if(configuration != null && configuration.getDirectoryToWrite() != null) {
		    logger.warn("A sensor writer contains a legacy configuration to write to '" + configuration.getDirectoryToWrite() + "'. The "
		            + "configured directory will be ignored, and sensor output from this writer will instead be written to '"
		            + PackageUtil.getDomainSessions() + "'.");
		 }
	}
	
	/**
	 * Class constructor -  configure using the parameters provided for this writer
	 * 
	 * @param prefix - the file name prefix to use for the file being created and written too
     * @param fileExtension - the type of file extension to use
     * @param eventProducerInformation - attributes for which data will be written for
	 */
	public GenericSensorDelimitedWriter(String prefix, String fileExtension, EventProducerInformation eventProducerInformation){
	    super(prefix, fileExtension, eventProducerInformation);
	    
	}
	
	/**
	 * Get the format to use for formatting decimals when writing sensor data attribute values.
	 * 
	 * @return DecimalFormat the format to use for this writer instance
	 */ 
	protected DecimalFormat getAttributeFormat(){
	    return attributeFormat;
	}
	
	@Override
	protected void write(SensorFilterEvent sensorFilterEvent) throws IOException{		

		SensorFilterData data = sensorFilterEvent.getData();
		
		if(!haveWrittenSomething){	    
		    writeHeader(eventProducerInformation);
		}
		
		writeAttributes(data.getElapsedTime(), data.getSensorFilterAttributeToValue());
		
		//force written contents to file
		flushWriter();
		
		if(isDebugEnabled){
		    logger.debug("Wrote "+sensorFilterEvent+" to file "+getFileName());
		}
	}
	
    @Override
    protected void write(SensorDataEvent sensorDataEvent) throws IOException{      
    
        AbstractSensorData data = sensorDataEvent.getData();

        if(!haveWrittenSomething){      
            writeHeader(eventProducerInformation);
        }     
        
        if(data instanceof SensorDataCollection){

            for (SensorData sData : ((SensorDataCollection) data).getSensorDatas()) {
                writeAttributes(data.getElapsedTime(), sData.getSensorAttributeToValue());
            }
            
        }else if(data instanceof SensorData){
            writeAttributes(data.getElapsedTime(), ((SensorData)data).getSensorAttributeToValue());

        }else{
            logger.error("Unable to write data of type "+data);
        }

        
        //force written contents to file
        flushWriter();
        
        if(isDebugEnabled){
            logger.debug("Wrote "+sensorDataEvent+" to file "+getFileName());
        }
    }
	
	/**
	 * Write the attribute values.
	 * 
	 * @param time - time stamp to show for this line written to disk.
	 * @param values - contains the sensor attribute value.
	 * @throws IOException if there was a problem retrieving a value to write
	 */
	protected void writeAttributes(long time, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> values) throws IOException{
        
        if (!values.isEmpty()) {

            StringBuffer line = new StringBuffer();
            line.append(TimeUtil.formatTimeRelative(0, time));

            //write the attribute(s) values
            for(EventProducerAttribute attribute : eventProducerInformation.getAttributes()){
                
                if(values.containsKey(attribute.getName())){
                  
                    AbstractSensorAttributeValue value = values.get(attribute.getName());
                    if (value.isNumber()) {
                        //write the number
                        line.append(DELIM).append(attributeFormat.format(value.getNumber()));

                    } else {
                        //the value is not a simple number, check for non-number (e.g. String) and/or complex attribute objects (e.g. Tuple3d)
                        
                        //check if it is a complex attribute data type
                        List<ComplexAttributeField> attributeFields = ComplexAttributeFieldManager.getInstance().getFieldsForAttributeClass(attribute.getType());
                        if(attributeFields != null){
                            //write each field to a specific column
                            
                            for(ComplexAttributeField attributeField : attributeFields){
                                
                                try {
                                    
                                    Object returnValue = attributeField.getMethod().invoke(value, (Object[])null);
                                    if(returnValue instanceof Number){
                                        //the returnValue is a number, format it and add it to the line in its own column
                                        line.append(DELIM).append(attributeFormat.format(returnValue));
                                    }else{
                                        //the returnValue is not a number
                                        //just call the toString on the object, after all this is a generic writer
                                        //Note: have to replace the DELIM in the data string to prevent this single value from appearing as multiple entries in the output.
                                        line.append(DELIM).append(returnValue.toString().replace(DELIM, delimReplacement));
                                    }
                                    
                                } catch (Exception e) {
                                    logger.error("Caught exception while trying to retrieve value from getter method of "+attributeField.getMethod().getName()+" in attribute value object of "+value+".", e);
                                    throw new IOException("Unable to retrieve complex attribute value from getter method", e);
                                } 
                            }
                            
                        }else{
                            //the value is not a number
                            //just call the toDataString on the object, after all this is a generic writer and the value implementation class hasn't
                            //provided specific information on how to retrieve its stored value
                            //Note: have to replace the DELIM in the data string to prevent this single value from appearing as multiple entries in the output.
                            line.append(DELIM).append(value.toDataString().replace(DELIM, delimReplacement));
                        }
                    }

                } else {
                    //write empty entry
                    
                    List<ComplexAttributeField> attributeFields = ComplexAttributeFieldManager.getInstance().getFieldsForAttributeClass(attribute.getType());
                    if(attributeFields != null && !attributeFields.isEmpty()){
                        //write empty entry for each field, since the attribute have no values in this event
                        
                        for(int i = 0; i < attributeFields.size(); i++){
                            line.append(DELIM);
                        }
                        
                    }else{
                        line.append(DELIM);
                    }
                }
            }

            line.append(EOL);
            write(line.toString());
            
        } //end if
    }
    
    /**
     * Write a header of the type of data the event producer can produce to the writer
     * 
     * @param eventProducerInformation contains information about the event producer including the possible data attributes it can produce
     */
    protected void writeHeader(EventProducerInformation eventProducerInformation){
        
        ComplexAttributeFieldManager complexAttrMgr = ComplexAttributeFieldManager.getInstance();
        
        logger.info("Using the following information to help write the header: "+complexAttrMgr+".");
        
        StringBuffer line = new StringBuffer();
        line.append(SensorAttributeNameEnum.TIME.toString());
        
        for(EventProducerAttribute attribute : eventProducerInformation.getAttributes()){
            SensorAttributeNameEnum name = attribute.getName();            
            
            List<ComplexAttributeField> attributeFields = complexAttrMgr.getFieldsForAttributeClass(attribute.getType());
            if(attributeFields != null && !attributeFields.isEmpty()){
                //this attribute is complex and needs it fields represented in one or more columns
                
                for(int i = 0; i < attributeFields.size(); i++){
                    ComplexAttributeField attributeField = attributeFields.get(i);
                    line.append(DELIM).append(name).append("_").append(attributeField.getLabel());
                }
                
            }else{
                line.append(DELIM).append(name);
            }
        }
        
        line.append(EOL);
        write(line.toString());
        
        logger.debug("Wrote header line = "+line.toString());
    }
}
