/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;

/**
 * This class represents a sensor writer event that can be included in an ERT report.  It has the logic to
 * convert sensor attributes from a sensor writer file into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class SensorWriterEvent extends DomainSessionEvent {

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Class constructor - set attributes and parse sensor data event.
     * 
     * @param time - elapsed domain session time at which this event occurred
     * @param attributes - name:value pairing of sensor writer events for a sensor
     */
    public SensorWriterEvent(double time, Map<String, String> attributes) {
        super(MessageTypeEnum.SENSOR_DATA.toString(), time, -1, -1);
        
        parseEvent(attributes);
    }
    
    /**
     * Gather information on the columns and cell content for the content of the sensor data
     * 
     * @param attributes - name:value pairing of sensor writer events for a sensor
     */
    private void parseEvent(Map<String, String> attributes){
        
        for(String name : attributes.keySet()){
            
            EventReportColumn col = new EventReportColumn(name, name);
            columns.add(col);
            cells.add(new Cell(attributes.get(name), col));
        }
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SensorWriterEvent: ");
        sb.append(super.toString());
        
        sb.append(", columns = {");
        for(EventReportColumn column : columns){
            sb.append(column.toString()).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }

}
