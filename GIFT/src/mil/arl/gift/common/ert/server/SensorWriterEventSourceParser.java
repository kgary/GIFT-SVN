/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.event.AbstractEvent;
import mil.arl.gift.common.ert.event.SensorWriterEvent;

/**
 * This class is responsible for converting sensor writer entries into events for the ERT.
 * A sensor writer file is an output file of the sensor module that contains sensor raw or filtered data.
 * 
 * @author mhoffman
 *
 */
public class SensorWriterEventSourceParser extends AbstractEventSourceParser {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SensorWriterEventSourceParser.class);
    private static boolean isDebug = logger.isDebugEnabled();
    
    /**
     * the columns for a bookmark event
     */
    private static List<EventReportColumn> defaultColumns;
    
    static{        
        defaultColumns = new ArrayList<EventReportColumn>();
        defaultColumns.add(new EventReportColumn(EventReportColumn.DS_TIME_COL_DISPLAY_NAME, EventReportColumn.DS_TIME_COL_NAME));
        defaultColumns.add(new EventReportColumn(EventReportColumn.EVENT_TYPE_COL_DISPLAY_NAME, EventReportColumn.EVENT_TYPE_COL_NAME));
    }
    
    private static final String SENSOR_WRITER_EVENT_NAME = "SensorWriterData";
    private static final String SENSOR_WRITER_EVENT_DISPLAY_NAME = "Sensor Writer Data";
    private static final String SENSOR_WRITER_EVENT_DESCRIPTION = "Sensor Data (filtered or unfiltered) written to a file created by a sensor writer.";
    private static List<EventType> typesOfEvents = new ArrayList<EventType>(1);
    
    /** contains the list of sensor writer events from the input file */
    private List<AbstractEvent> events = new ArrayList<AbstractEvent>();
    
    /** the sensor data source file */
    private File file;
    
    /**
     * Class constructor - parses the sensor data file.
     * 
     * @param file a sensor data file
     */
    public SensorWriterEventSourceParser(File file){
        
        if(file == null){
            throw new IllegalArgumentException("The file can't be null.");
        }
        this.file = file;
        
        parse();
    }
    
    /**
     * Parse the sensor data file and gather the header information.
     * 
     * @return boolean - whether the file was successfully parsed and needed information gathered
     */
    private boolean parse(){
        
        ICsvMapReader reader = null;
        try {
            reader = new CsvMapReader(new FileReader(file), CsvPreference.EXCEL_PREFERENCE);
            
            String[] header = reader.getCSVHeader(false);
            
            logger.info("found header of size "+header.length+" in "+file.getName());
            if(isDebug){
                StringBuffer sb = new StringBuffer();
                for(String element : header){
                    sb.append(element).append(", ");
                }
                logger.info("Header content: "+sb.toString());
            }
            
            Map<String, EventReportColumn> eventColumns = new HashMap<String, EventReportColumn>();
            
            Map<String, String> row;
            //analyze each row under the header
            while(( row = reader.read(header)) != null){
                
                //gather attributes (not time)
                Map<String, String> attributes = new HashMap<String, String>();
                for(String name : row.keySet()){
                    
                    if(!name.equals(SensorAttributeNameEnum.TIME.toString())){
                        attributes.put(name, row.get(name));
                    }
                }
                
                SensorWriterEvent event = new SensorWriterEvent(Double.valueOf(row.get(SensorAttributeNameEnum.TIME.toString())), attributes);
                logger.info("Adding event = "+event);
                
                events.add(event);
                
                //add the specific event columns, uniquely by name - don't want duplicate column names
                for(EventReportColumn column : event.getColumns()){                    
                    eventColumns.put(column.getColumnName(), column);
                }
            }
            
            List<EventReportColumn> sensorWriterColumns = new ArrayList<EventReportColumn>(eventColumns.values());         
            typesOfEvents.add(new EventType(SENSOR_WRITER_EVENT_NAME, SENSOR_WRITER_EVENT_DISPLAY_NAME, SENSOR_WRITER_EVENT_DESCRIPTION, sensorWriterColumns));
            
            logger.info("Found "+events.size()+" sensor writer events");

            
        } catch (Exception e) {
            logger.error("Caught exception while parsing sensor data event file of "+file.getName(), e);
            return false;
        } finally{
            
            //try to close the reader
            if(reader != null){
                try {
                    reader.close();
                } catch (@SuppressWarnings("unused") IOException e) {
                    //nothing to do...
                }
            }
        }
        
        return true;
    }

    @Override
    public List<EventType> getTypesOfEvents() {
        return typesOfEvents;
    }

    @Override
    public List<AbstractEvent> getEventsByType(EventType eventType) {
        
        List<AbstractEvent> typeEvents = null;
        
        if(typesOfEvents.contains(eventType)){
            typeEvents = events;
        }
        
        return typeEvents;
    }

    @Override
    public List<EventReportColumn> getDefaultColumns() {
        return defaultColumns;
    }
    
    /**
     * Return whether or not the file provided is a sensor writer file.
     * A sensor writer file is an output file of the sensor module that contains sensor raw or filtered data.
     * 
     * @param file the file to check
     * @return boolean
     */
    public static boolean isSensorWriterFile(File file){
        
        boolean checkPassed = true;
        try {
            ICsvMapReader reader = new CsvMapReader(new FileReader(file), CsvPreference.EXCEL_PREFERENCE);
            
            String[] header = reader.getCSVHeader(false);
            
            if(header == null || header.length <= 1){
                checkPassed = false;
            }else if(header.length > 1){
                //there must be 2 or more columns in a sensor writer csv file
                checkPassed = true;
            }
            
        } catch (IOException e) {
            logger.error("Caught exception while parsing sensor writer event source file of "+file.getName(), e);
            checkPassed = false;
        }
        
        logger.info("Check of file named = "+file.getName()+" to be a sensor writer event source file resulted in "+checkPassed);
        
        return checkPassed;
    }

    @Override
    public Integer getParticipantId() {
        return null;
    }

}
