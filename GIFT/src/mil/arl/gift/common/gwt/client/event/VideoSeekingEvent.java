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
 * Represents a native seeking event (see
 * https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement/seeking_event).
 * 
 * @author sharrison
 */
public class VideoSeekingEvent extends DomEvent<VideoSeekHandler> {

    /** The type of the event to sink */
    private static final Type<VideoSeekHandler> TYPE = new Type<VideoSeekHandler>(ExtendedBrowserEvents.SEEKING,
            new VideoSeekingEvent());

    /**
     * Gets the event type associated with input events.
     * 
     * @return the handler type
     */
    public static Type<VideoSeekHandler> getType() {
        return TYPE;
    }

    /**
     * Protected constructor, use
     * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
     * to fire input events.
     */
    protected VideoSeekingEvent() {
    }

    @Override
    public Type<VideoSeekHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(VideoSeekHandler handler) {
        handler.onSeeking(this);
    }
}
