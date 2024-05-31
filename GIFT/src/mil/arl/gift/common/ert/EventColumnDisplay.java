/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the display values for an event and it's columns.
 * 
 * @author mhoffman
 *
 */
public class EventColumnDisplay implements Serializable{
    
	private static final long serialVersionUID = 1L;

	/** whether this event type is enabled or not */
    private Boolean isEnabled = Boolean.FALSE;
    
    /** map detailing which sub-columns of the event are enabled */
    private Map<EventReportColumn, Boolean> columnEnabledMap = new HashMap<EventReportColumn, Boolean>();
    
    /** the event type information for the event represented by the column*/
    private EventType eventType;
    
    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public EventColumnDisplay(){            
    }
    
    /**
     * Class constructor - set attributes to default values using event type information provided
     * 
     * @param eventType - the event type information for the event represented by the column
     */
    public EventColumnDisplay(EventType eventType){
        this.eventType = eventType;
        init(eventType);
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param eventType - the event type information for the event represented by the column
     * @param isEnabled - whether the column is currently enabled to be included in a generated report
     * @param columnEnabledMap - map detailing which sub-columns of the event are enabled 
     */
    public EventColumnDisplay(EventType eventType, Boolean isEnabled, Map<EventReportColumn, Boolean> columnEnabledMap){
        this.isEnabled = isEnabled;
        
        if(eventType == null){
            throw new IllegalArgumentException("The event type can't be null");
        }
        this.eventType = eventType;
        
        if(columnEnabledMap == null){
            throw new IllegalArgumentException("The column map can't be null");
        }
        this.columnEnabledMap = columnEnabledMap;
    }
    
    /**
     * Initialize this class using the data from the event type 
     * 
     * @param eType
     */
    private void init(EventType eType){
        
        for(EventReportColumn col : eType.getEventColumns()){
            columnEnabledMap.put(col, false);
        }
    }
    
    /**
     * Return the column display map
     * 
     * @return Map<EventReportColumn, Boolean> - unmodifiable copy of the map
     */ 
    public Map<EventReportColumn, Boolean> getColumnMap(){
        return Collections.unmodifiableMap(columnEnabledMap);
    }
    
    public EventType getEventType(){
        return eventType;
    }
    
    /**
     * Return whether this event column is enabled or not.
     * 
     * @return boolean
     */
    public Boolean isEnabled(){
        return isEnabled;
    }
    
    /**
     * Set whether this event column is enabled or not.  In addition it will change the
     * events sub-column's enabled setting to the same provided value.
     * 
     * @param enabled - set whether the column is enabled or not
     */
    public void setEnabled(boolean enabled){
        this.isEnabled = enabled;
        
        for(EventReportColumn col : columnEnabledMap.keySet()){
            columnEnabledMap.put(col, enabled);
        }
    }
    
    /**
     * Return whether the specified column of this event is enabled or not.
     * 
     * @param column - the column to check for
     * @return boolean - the enabled setting value for that column
     */
    public boolean isEnabled(EventReportColumn column){
        
        if(columnEnabledMap.containsKey(column)){
            return columnEnabledMap.get(column);
        }
        
        return false;
    }
    
    /**
     * Update the enabled setting of a column of this event.
     * 
     * @param column - the column to update the enable setting for
     * @param enabled - the new enabled value to use
     * @return boolean - whether the update operation succeeded.
     */
    public boolean setEnabled(EventReportColumn column, boolean enabled){
        
        if(columnEnabledMap.containsKey(column)){
            columnEnabledMap.put(column, enabled);
            return true;
        }
        
        return false;
    }
    
    /**
     * Return the list of selected columns.
     * 
     * @return List<EventReportColumn>
     */
    public List<EventReportColumn> getSelectedColumns() {

        List<EventReportColumn> eventReportColumns = new ArrayList<EventReportColumn>();

        for (EventReportColumn column : columnEnabledMap.keySet()) {

            if (columnEnabledMap.get(column)) {

                eventReportColumns.add(column);
            }
        }

        return eventReportColumns;
    }
    
    /**
     * Return the list of not selected columns.
     * 
     * @return List<EventReportColumn>
     */
    public List<EventReportColumn> getNotSelectedColumns() {

        List<EventReportColumn> eventReportColumns = new ArrayList<EventReportColumn>();

        for (EventReportColumn column : columnEnabledMap.keySet()) {

            if (!columnEnabledMap.get(column)) {

                eventReportColumns.add(column);
            }
        }

        return eventReportColumns;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EventColumnDisplay: ");
        sb.append("isEnabled = ").append(isEnabled());
        sb.append(", eventType = ").append(getEventType());
        sb.append("]");
        
        return sb.toString();
    }
}
