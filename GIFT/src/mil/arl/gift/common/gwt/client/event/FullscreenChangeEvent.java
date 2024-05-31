/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.event;

import com.google.gwt.event.dom.client.DomEvent;

/**
 * Represents a native standard fullscreen change event (see
 * https://developer.mozilla.org/en-US/docs/Web/API/Document/fullscreenchange_event).
 * 
 * @author sharrison
 */
public class FullscreenChangeEvent extends DomEvent<FullscreenChangeHandler> {

    /** The type of the event to sink */
    private static final Type<FullscreenChangeHandler> TYPE = new Type<FullscreenChangeHandler>(
            ExtendedBrowserEvents.FULLSCREEN_CHANGE, new FullscreenChangeEvent());

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
    protected FullscreenChangeEvent() {
    }

    @Override
    public Type<FullscreenChangeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FullscreenChangeHandler handler) {
        handler.onFullscreenChange(this);
    }
}
