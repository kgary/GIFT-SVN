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

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;


/**
 * The default event representation for events that will be used in an event report.
 * This class stores the content/information on an event for a row in the report.
 * 
 * @author mhoffman
 *
 */
public class DefaultEvent extends AbstractEvent {

    /** information about the event */
    private String content;
    
    /** contains columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param name the name of the event 
     * @param time time at which the event occurred
     * @param content information about the event
     */
    public DefaultEvent(String name, long time, String content){
        super(name, time);
        this.content = content;
        
        cells.add(new Cell(content, EventReportColumn.CONTENT_COLUMN));
    }  
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DefaultEvent: ");
        sb.append(super.toString());
        
        sb.append(", columns = {");
        for(EventReportColumn column : columns){
            sb.append(column.toString()).append(", ");
        }
        sb.append("}");
        
        if(content != null){
            sb.append(", content = ").append(content);
        }
        
        sb.append("]");
        return sb.toString();
    }
}
