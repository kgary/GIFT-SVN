/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.event;

/**
 * Represents a native fullscreen change event for Chrome, Safari, and Opera
 * (see https://www.w3schools.com/JSREF/event_fullscreenchange.asp).
 * 
 * @author sharrison
 */
public class WebkitFullscreenChangeEvent extends FullscreenChangeEvent {

    /** The type of the event to sink */
    private static final Type<FullscreenChangeHandler> TYPE = new Type<FullscreenChangeHandler>(
            ExtendedBrowserEvents.WEBKIT_FULLSCREEN_CHANGE, new WebkitFullscreenChangeEvent());

    /**
     * Gets the event type associated with input events.
     * 
     * @return the handler type
     */
    public static Type<FullscreenChangeHandler> getType() {
        return TYPE;
    }

    /**
     * Protected constructor, use
     * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
     * to fire input events.
     */
    protected WebkitFullscreenChangeEvent() {
    }

    @Override
    public Type<FullscreenChangeHandler> getAssociatedType() {
        return TYPE;
    }
}
