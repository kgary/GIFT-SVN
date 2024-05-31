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
import mil.arl.gift.common.ert.server.Row;


/**
 * The base class for events that will be used in an event report.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractEvent {
    
    /** the time at which this event happened */
    private long time;
    
    /** the name of the event */
    private String name;   
    
    /** cell containing the event time value */
    private Cell timeCell = null;
    
    /** report cells containing the content of this event */
    protected List<Cell> cells = new ArrayList<Cell>();
    
    /**
     * Private constructor 
     * 
     * @param name the name of the event 
     */
    public AbstractEvent(String name){
        this.name = name;
        cells.add(new Cell(name, EventReportColumn.EVENT_TYPE_COLUMN));
    }
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param name the name of the event 
     * @param epochTime - time at which the event occurred
     */
    public AbstractEvent(String name, long epochTime){
        this(name);
        
        setTime(epochTime);       
    }
    
    /**
     * Return the time at which this event happened.
     * 
     * @return milliseconds since epoch
     */
    public long getTime(){
        return time;
    }
    
    /**
     * Set the time at which the event occurred
     * 
     * @param time milliseconds since epoch
     */
    public void setTime(long time){
        
        this.time = time;
        if(timeCell != null){
            cells.remove(timeCell);
        }        
        
        timeCell = new Cell(String.valueOf(time), EventReportColumn.TIME_COLUMN);
        cells.add(timeCell);
    }

    /** 
     * Convert the event's information into a row
     * 
     * @return Row
     */
    public Row toRow(){
        
        Row row = new Row();
        row.addCells(cells);        
        return row;
    }
    
    /**
     * Return the list of columns for specific to the content of this event
     * 
     * @return List<EventReportColumn>
     */
    public abstract List<EventReportColumn> getColumns();
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AbstractEvent: ");
        sb.append("name = ").append(name);
        sb.append(", time = ").append(time); 
        
        sb.append(", cells = {");
        for(Cell cell : cells){
            sb.append(cell.toString()).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
