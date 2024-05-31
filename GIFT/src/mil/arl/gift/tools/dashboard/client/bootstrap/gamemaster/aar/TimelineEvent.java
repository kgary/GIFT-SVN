/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import com.github.gwtd3.api.arrays.Array;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

/**
 * An event in a {@link TimelineHistory} that represents something that happened during that history. How
 * frequently an event occurs and how long each occurrence lasts is determined by the {@link EventInterval}s
 * that are associated with said event.
 * 
 * @author nroberts
 */
public class TimelineEvent extends JavaScriptObject{

    /** Default no-arg constructor required for classes extending JavaScriptObject */
    protected TimelineEvent() {}

    /**
     * Gets the name of the event
     * 
     * @return the name. Will not be null.
     */
    final public native String getName()/*-{
        return this.name;
    }-*/;
    
    /**
     * Gets the ID used to uniquely identify this event in its history
     * 
     * @return the unique ID of this event. Will not be null.
     */
    final public native int getId()/*-{
        return this.id;
    }-*/;

    /**
     * Gets the overarching parent event that this event is part of, if such an event exists
     * 
     * @return the parent event. Can be null.
     */
    final public native TimelineEvent getParent()/*-{
        return this.parent;
    }-*/;

    /**
     * Sets the overaching parent event that this event is part of.
     * <br/><br/>
     * <i>Note:</i> This event's {@link #isFixed()} state will be changed to match
     * that of it's topmost parent. 
     * 
     * @param event the event to make this event's parent. Can be null.
     */
    final public native void setParent(TimelineEvent event)/*-{
        
        //update this event's parent
        var oldParent = this.parent;
        this.parent = event;
        
        if(oldParent !== this.parent){
            
            //remove this event from its previous parent's sub events, if needed
            if(oldParent != null){
                var index = oldParent.subEvents.indexOf(5);
                if (index > -1) {
                  oldParent.subEvents.splice(index, 1);
                }
            }
            
            //add this event to its new parent's sub events, if needed
            if(this.parent != null){
                this.parent.subEvents.push(this);
            }
        }
        
    }-*/;

    /**
     * Gets the intervals associated with this event that determine when it occurred
     * and how long each occurence lasted
     * 
     * @return the intervals. Cannot be null. Can be empty;
     */
    final public native Array<EventInterval> getIntervals()/*-{
        return this.intervals;
    }-*/;

    /**
     * Adds a new interval with the given start date, end date, and status to this event.
     * 
     * @param startDate the date that the interval should start, in epoch time
     * @param endDate the the date that the interval should end, in epoch time
     * @param status the status of this event during this interval. Can be null.
     * @return the added interval. Will not be null.
     */
    final public EventInterval addInterval(long startDate, long endDate, JavaScriptObject status) {
        return addIntervalJs(JsDate.create(startDate), JsDate.create(endDate), status);
    }

    //Native implementation of addInterval(...)
    final private native EventInterval addIntervalJs(JsDate startDate, JsDate endDate, JavaScriptObject status)/*-{
        
        var interval = {
            event : this,
            startDate : startDate,
            endDate : endDate,
            status : status
        }
        
        this.intervals.push(interval);
        
        return interval;
    }-*/;
    
    /**
     * Gets the sub-events that are part of this event
     * 
     * @return the sub-events. Cannot be null, but can be empty.
     */
    final public native Array<TimelineEvent> getSubEvents()/*-{
        return this.subEvents;
    }-*/;
    
    /**
     * Sets whether or not this event should be collapsed so that its sub-events are hidden
     * 
     * @param collapsed whether to collapse this event and hide its sub-events
     */
    final public native void setCollapsed(boolean collapsed)/*-{
        this.collapsed = collapsed;
    }-*/;
    
    /**
     * Gets whether or not this event is collapsed so that its sub-events are hidden
     * 
     * @return whether this event is collapsed
     */
    final public native boolean isCollapsed()/*-{
        return this.collapsed;
    }-*/;
    
    /**
     * Gets whether or not this event is fixed (i.e. non-scrolling). If this event
     * has a parent, then this will be determined by whether it's topmost parent is fixed.
     * 
     * @return whether this event is fixed
     */
    final public native boolean isFixed()/*-{
        
        var currEvent = this;
        while(currEvent.parent != null){
            currEvent = currEvent.parent;
        }
        
        return currEvent.fixed;
    }-*/;

}
