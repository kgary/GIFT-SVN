/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.IntervalClickHandler;

/**
 * An occurrence of a {@link TimelineEvent} that takes place over a single interval of time. 
 * Each of an event's intervals will be placed in line with that event's label in the rendered chart.
 * 
 * @author nroberts
 */
public class EventInterval extends JavaScriptObject{

    /** Default no-arg constructor required for classes extending JavaScriptObject */
    protected EventInterval() {}
    
    /**
     * Gets this interval's associated event (i.e. its label in the timeline)
     * 
     * @return the associated event. Will not be null.
     */
    final public native TimelineEvent getEvent()/*-{
        return this.event;
    }-*/;
    
    /**
     * Gets the date when this interval starts
     * 
     * @return the start date. Will not be null.
     */
    final public native JsDate getStartDate()/*-{
        return this.startDate;
    }-*/;

    /**
     * Gets the date when this interval ends
     * 
     * @return the end date. Will not be null.
     */
    final public native JsDate getEndDate()/*-{
        return this.endDate;
    }-*/;
    
    /**
     * Sets the date when this event interval ends
     * 
     * @param endDate the end date. Cannot be null.
     */
    final public native void setEndDate(JsDate endDate)/*-{
        this.endDate = endDate;
    }-*/;
    
    /**
     * Gets the status of the event during this interval
     * 
     * @return the status. Can be null.
     */
    final public native JavaScriptObject getStatus()/*-{
        return this.status;
    }-*/;
    
    /**
     * Sets the HTML to show in a tooltip when the mouse hovers over this interval
     * 
     * @param tooltip the tooltip HTML. Can be null to prevent a tooltip from showing.
     */
    final public native void setTooltip(String tooltip)/*-{
        this.tooltip = tooltip;
    }-*/;

    /**
     * Gets the text to show in a tooltip when the mouse hovers over this interval
     * 
     * @return the tooltip text. Can be null if a tooltip should not be shown.
     */
    final public native String getTooltip()/*-{
        return this.tooltip;
    }-*/;

    /**
     * Sets the flag indicating whether or not this interval represents a
     * patched event.
     * 
     * @param isPatched true if this event has been patched; false otherwise
     *        (defaults to false).
     */
    final public native void setIsPatched(boolean isPatched)/*-{
		this.isPatched = isPatched;
    }-*/;

    /**
     * Retrieves the flag indicating whether or not this interval represents a
     * patched event.
     * 
     * @return true if this event has been patched; false otherwise (defaults to
     *         false).
     */
    final public native boolean isPatched()/*-{
		return this.isPatched;
    }-*/;

    /**
     * Sets the function to execute when the user clicks this interval
     * 
     * @param func the function to execute. Can be null if nothing should happen when the user
     * clicks on this interval.
     */
    final public native void setClickFunction(IntervalClickHandler func)/*-{
        this.clickFunction = func;
    }-*/;
    
    /**
     * Gets the function to execute when the user clicks this interval
     * 
     * @return the function to execute. Can be null if nothing should happen when the user
     * clicks on this interval.
     */
    final public native IntervalClickHandler getClickFunction()/*-{
        return this.clickFunction;
    }-*/;

}
