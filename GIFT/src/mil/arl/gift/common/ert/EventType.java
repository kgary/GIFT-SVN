/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a type of event (e.g. Learner state message) from an event source (e.g. domain session log file).
 * 
 * @author jleonard
 */
public class EventType implements Serializable, Comparable<EventType> {
	
	private static final long serialVersionUID = 1L;

	/** value for generating event type unique ids   */
    private static int Event_ID = 0;
    
    /**
     * Return the next unique event type id
     * 
     * @return int
     */
    public static synchronized int getNextEventID(){
        return Event_ID++;
    }
    
    /** unique id of this event type */
    private int eventId;
    
    /** the name of this event type - should be unique amongst events */
    private String name;
    
    /** a friendly display name of this event type - doesn't have to be unique but should be */
    private String displayName;
    
    /** a helpful description of the event type */
    private String description;
    
    /** container of all possible columns for this type of event */
    private List<EventReportColumn> eventColumns = new ArrayList<EventReportColumn>();
    
    /**
     * Default Constructor
     * 
     * Required by IsSerializable to exist
     */
    @SuppressWarnings("unused")
    private EventType() {
        
    }
    
    /**
     * Class constructor - set attributes and generate unique id.
     * 
     * @param name - the name of this event type. can't be null or empty. 
     * @param displayName - the display name of this event type.  can't be null or empty.
     * @param description - a helpful description of the event type.  Can be null.
     */
    public EventType(String name, String displayName, String description) {
        this.eventId = getNextEventID();
        
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The name can't be null or empty.");
        }else if(displayName == null || displayName.isEmpty()){
            throw new IllegalArgumentException("The display name can't be null or empty.");
        }
        
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Class contructor - set attributes and generate unique id.
     * 
     * @param name - the name of this event type. can't be null or empty. 
     * @param displayName - the display name of this event type.  can't be null or empty.
     * @param description - a helpful description of the event type.  Can be null.
     * @param eventColumns container of all possible columns for this type of event
     */
    public EventType(String name, String displayName, String description, List<EventReportColumn> eventColumns) {
        this(name, displayName, description);
        this.eventColumns = eventColumns;
    }
    
    /**
     * Return the unique id of this event type
     * 
     * @return int - unique id of this event type
     */
    public int getId() {
        return eventId;
    }
    
    /**
     * Return the name of this event type
     * 
     * @return String. will not be null or empty.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return the display name of this event type
     * 
     * @return String.  will not be null or empty.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Return the helpful description of the event type.  
     * 
     * @return String the even type description. Can be null.
     */
    public String getDescription(){
        return description;
    }
    
    /**
     * Return the list of all possible columns for this type of event
     * 
     * @return List<EventReportColumn>
     */
    public List<EventReportColumn> getEventColumns(){
        return eventColumns;
    }
    
    @Override
    public int compareTo(EventType other) {             
        return this.getName().compareTo(other.getName());
    }
    
    @Override
    public boolean equals(Object otherEventType){
        
        boolean equals = false;
        //MH 2/1/13 - not sure why an id comparison was done here because it prevents a scenario where 2 domain session message
        //            logs are selected as inputs and they both contain a matching event type which should be the same event type
        //            on the ERT but with both sets of event columns (if there are any for that event)
        if(otherEventType instanceof EventType &&
                /*((EventType)otherEventType).getId() == this.getId() &&*/ ((EventType)otherEventType).getName().equals(this.getName())){
            equals = true;
        }
        
        return equals;
    }
    
    @Override
    public int hashCode() {     

        int hashCode = 0;
        
//        hashCode |= getId() << 2;
        hashCode |= name.hashCode();
                
        return hashCode;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EventType: ");
        sb.append("id = ").append(getId());
        sb.append(", name = ").append(getName());
        sb.append(", displayName = ").append(getDisplayName());
        sb.append(", description = ").append(getDescription());
     
        sb.append(", columns = {");
        for(EventReportColumn column : eventColumns){
            sb.append(column.toString()).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
