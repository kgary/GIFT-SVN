/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.core.Value;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

/**
 * A history of events that have taken place over a period of time (i.e. a timeline). The course of each 
 * {@link TimelineEvent} is portrayed by the {@link EventInterval}s that are associated with it.
 * 
 * @author nroberts
 */
public class TimelineHistory extends JavaScriptObject {
    
    /** Default no-arg constructor required for classes extending JavaScriptObject */
    protected TimelineHistory() {}
    
    /**
     * Creates a new history with no events (i.e. an empty timeline) that spans the given time frame
     * 
     * @param startDate the start date of the history time frame
     * @param endDate the end date of the history time frame
     * @return the created timeline history. Will not be null.
     */
    final public static TimelineHistory create(long startDate, long endDate) {
        return create(JsDate.create(startDate), JsDate.create(endDate));
    }
    
    /**
     * Creates a new history with no events (i.e. an empty timeline) that spans the given time frame
     * 
     * @param startDate the start date of the history timeframe. Cannot be null.
     * @param endDate the end date of the history timeframe. Cannot be null.
     * @return the created timeline history. Will not be null.
     */
    final public static TimelineHistory create(JsDate startDate, JsDate endDate) {
        
        if(startDate == null) {
            throw new IllegalArgumentException("A timeline history's starting date cannot be null");
        }
        
        if(endDate == null) {
            throw new IllegalArgumentException("A timeline history's ending date cannot be null");
        }
        
        return createJs(startDate, endDate);
    }

    //native implementation of create(JsDate, JsDate)
    final private static native TimelineHistory createJs(JsDate startDate, JsDate endDate)/*-{
        
        var history = {
            
            //a mapping from each event name to its associated event
            nameToEvent : {},
            
            //the events that take place in this timeline
            events : [],
            
            //the date that the timeline begins at
            startDate : startDate,
            
            //the date that the timeline ends at
            endDate : endDate
        }
        
        //a function that gets the event with the given name
        history.getEvent = function(eventName){
            return history.nameToEvent[eventName];
        }
        
        return history;
    }-*/;

    /**
     * Gets the event in the timeline with the given name
     * 
     * @param eventName the name of the event to get
     * @return the event with the given name
     */
    final public native TimelineEvent getEvent(String eventName)/*-{
        return this.getEvent(eventName);
    }-*/;
    
    /**
     * Creates an event with the given name and adds it to the timeline. If an existing event with 
     * the given name has already been added to the timeline, then that event will be returned
     * and no new event will be created.
     * 
     * @param id the ID that uniquely identifies the event being added
     * @param eventName the name of the event to add. Cannot be null.
     * @return the added event, or the existing event with the given name. Will not be null.
     */
    final public TimelineEvent addEvent(int id, String eventName){
        return addEvent(id, eventName, false);
    }

    
    /**
     * Creates an event with the given name and adds it to the timeline. If an existing event with 
     * the given name has already been added to the timeline, then that event will be returned
     * and no new event will be created.
     * 
     * @param id the ID that uniquely identifies the event being added
     * @param eventName the name of the event to add. Cannot be null.
     * @param isFixed whether the event should be fixed (i.e. non-scrolling). <i/>Note:</i> this can be
     * overridden if the event is later provided with a parent, since the topmost parent's fixed
     * state takes precedence.
     * @return the added event, or the existing event with the given name. Will not be null.
     */
    final public native TimelineEvent addEvent(int id, String eventName, boolean isFixed)/*-{
        
        var event = this.getEvent(eventName);
        if(event){
            
            //an event with this name has already been added, so don't add it again
            return event;
        }
        
        event = {
            id : id,
            name : eventName,
            intervals : [],
            subEvents : [],
            collapsed : true,
            fixed : isFixed
        }
        
        //map the event's name to the event itself using a property accessor, which also ensures uniqueness
        this.nameToEvent[eventName] = event;
        
        this.events.push(event);
        
        return event;
    }-*/;
    
    /**
     * Gets all of the events that take place in this timeline history
     * 
     * @return the events. Will not be null. Can be empty.
     */
    final public native Array<TimelineEvent> getEvents()/*-{
        return this.events;
    }-*/;
    
    /**
     * Gets the start date of the history time frame
     * 
     * @return the start date. Will not be null.
     */
    final public native JsDate getStartDate()/*-{
        return this.startDate;
    }-*/;
    
    /**
     * Gets the end date of the history time frame
     * 
     * @return the end date. Will not be null.
     */
    final public native JsDate getEndDate()/*-{
        return this.endDate;
    }-*/;
    
    /**
     * Applies the collapse state of any events in this timeline history to the history that's provided.
     * This can be used to keep events expanded or collapsed after replacing the history in a timeline chart,
     * since it effectively preserves the expanded/collapsed state across histories.
     * <br/><br/>
     * Note: In order for an event's expanded/collapsed state to properly carry over to the new history, the
     * new history must have an event with the same ID.
     * 
     * @param newHistory the new history to apply this history's event collapse states to. Cannot be null.
     */
    final public void applyCollapseState(TimelineHistory newHistory) {
        
        if(newHistory == null) {
            throw new IllegalArgumentException("The history that collapse states should be applied to cannot be null.");
        }
        
        //get the unique IDs of all of the events that are currently expanded in this history
        List<Integer> expandedEventNames = new ArrayList<>();
        for(Value value : getEvents().asIterable()) {
            TimelineEvent event = value.as();
            if(!event.isCollapsed()) {
                expandedEventNames.add(event.getId());
            }
        }
        
        //iterate over the events in the new history and expand any events that have matching IDs
        for(Value value : newHistory.getEvents().asIterable()) {
            TimelineEvent event = value.as();
            event.setCollapsed(!expandedEventNames.contains(event.getId()));
        }
    }
    
    /**
     * Gets the next available event ID that is not currently in use. Can be useful for injecting new events after
     * a group of existing events in a timeline history.
     * 
     * @return the next available event ID. If no events are yet present, this will default to 0.
     */
    public final Integer getNextEventId() {
        
        int largestId = 0;
        for(Value value : getEvents().asIterable()) {
            
            TimelineEvent event = value.as();
            if(largestId < event.getId()) {
                largestId = event.getId();
            }
        }
        
        return ++largestId;
    }
}
