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

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.ta.state.Geolocation;

/**
 * This class represents a geo location event that can be included in an ERT report.  It has the logic to
 * convert a geo location object into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class GeolocationEvent extends DomainSessionEvent {
    
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String ACCURACY = "gps accuracy";
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();

    /**
     * Class constructor - set attributes and parse learner state
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param state - the learner state content for the event
     */
    public GeolocationEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, Geolocation geolocation){
        super(MessageTypeEnum.GEOLOCATION.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(geolocation);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    /**
     * Parse the geo location event into columns.
     * 
     * @param geolocation event to parse columns
     */
    private void parseEvent(Geolocation geolocation){
        
        EventReportColumn latitudeCol = new EventReportColumn(LATITUDE);
        columns.add(latitudeCol);
        cells.add(new Cell(String.valueOf(geolocation.getCoordinates().getLatitude()), latitudeCol));
        
        EventReportColumn longitudeCol = new EventReportColumn(LONGITUDE);
        columns.add(longitudeCol);
        cells.add(new Cell(String.valueOf(geolocation.getCoordinates().getLongitude()), longitudeCol));
        
        EventReportColumn accuracyCol = new EventReportColumn(ACCURACY);
        columns.add(accuracyCol);
        cells.add(new Cell(String.valueOf(geolocation.getAccuracy()), accuracyCol));
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[GeolocationEvent: ");
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
