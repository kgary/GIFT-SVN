/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.server;

import java.io.File;

import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.server.AbstractEventSourceParser;
import mil.arl.gift.common.ert.server.BookmarkEventSourceParser;
import mil.arl.gift.common.ert.server.MessageLogEventSourceParser;
import mil.arl.gift.common.ert.server.SensorWriterEventSourceParser;

/**
 * The Event Source Util provides useful helper methods for handling event sources.
 * 
 * @author mhoffman
 *
 */
public class EventSourceUtil {
    
    /**
     * Return a new instance of an event source parser based on the type of file provided.
     * 
     * @param file the file to get a parser for.  Can't be null and must exist.
     * @param reportProperties contains configuration parameters and other properties for an event report.  Can't be null.
     * @return AbstractEventSourceParser - new instance using the file provided.
     * @throws Exception if there was a severe problem parsing the event file
     */
    public static AbstractEventSourceParser getEventParser(File file, ReportProperties reportProperties) throws Exception{
        
        AbstractEventSourceParser parser = null;
        
        if(isMessageLog(file)){
            parser = new MessageLogEventSourceParser(file, reportProperties);
        }else if(isSensorWriter(file)){
            parser = new SensorWriterEventSourceParser(file);
        }else if(isBookmarkLog(file)){
            parser = new BookmarkEventSourceParser(file);            
        }
        
        return parser;
    }
    
    /**
     * Return whether or not the file provided is a message log file.
     * 
     * @param file the file to check
     * @return true if the fils is a message log file
     */
    public static boolean isMessageLog(File file){
        return MessageLogEventSourceParser.isMessageLog(file);        
    }
    
    /**
     * Return whether or not the file provided is a bookmark file.
     * 
     * @param file the file to check
     * @return true if the file is a Monitor bookmark file
     */
    public static boolean isBookmarkLog(File file){
        return BookmarkEventSourceParser.isBookmarkLog(file);
    }
    
    /**
     * Return whether or not the file provided is a sensor writer file.
     * A sensor writer file is an output file of the sensor module that contains sensor raw or filtered data.
     * 
     * @param file the file to check 
     * @return true if the file is a sensor writer file
     */
    public static boolean isSensorWriter(File file){
        return SensorWriterEventSourceParser.isSensorWriterFile(file);
    }

    private EventSourceUtil() {
    }
}
