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

import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.sensor.UnfilteredSensorData;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;

/**
 * This class represents a sensor data event that can be included in an ERT report.  It has the logic to
 * convert a sensor data object into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class SensorDataEvent extends DomainSessionEvent {

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Class constructor - set attributes and parse sensor data event.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param sensorData contains the sensor data
     */
    public SensorDataEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, UnfilteredSensorData sensorData) {
        super(MessageTypeEnum.SENSOR_DATA.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(sensorData);
    }
    
    /**
     * Gather information on the columns and cell content for the content of the sensor data
     * 
     * @param sensorData contains the sensor data
     */
    private void parseEvent(UnfilteredSensorData sensorData){
        
        for(SensorAttributeNameEnum name : sensorData.getAttributeValues().keySet()){
            
            EventReportColumn col = new EventReportColumn(name.getName(), name.getName());
            columns.add(col);
            cells.add(new Cell(sensorData.getAttributeValues().get(name).toString(), col));
        }
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SensorDataEvent: ");
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
