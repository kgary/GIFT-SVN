/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import com.google.gwt.dom.client.Element;

import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.EventInterval;

/**
 * A handler that is capable of processing a click event for an {@link EventInterval}
 * 
 * @author nroberts
 */
public abstract class IntervalClickHandler{
    
    /**
     * Gets whether the click event should be allowed to propagate to any parent elements. This is mainly
     * used to prevent the default timeline click behavior from being executed when an event interval
     * is clicked.
     * 
     * @param context
     * @param interval
     * @param index
     * @param timestamp
     * @return whether the click event should propagate to parent elements. Defaults to false.
     */
    public boolean shouldPropagate(Element context, EventInterval interval, int index, long timestamp) {
        return false;
    }

    /**
     * Handles a click event on the given {@link Element} is associated with the given {@link EventInterval}
     * 
     * @param context the element that was clicked. Will not be null.
     * @param interval the event interval that the element represents
     * @param index the index of the data item within the D3 context. Generally, don't mess with this unless
     * you understand what it means within the D3 engine.
     * @param timestamp the exact timestamp value of the X coordinate where the click occured within 
     * the timeline chart. Will be somewhere between the start and end dates of the interval.
     */
    public abstract void onClick(Element context, EventInterval interval, int index, long timestamp);
}
