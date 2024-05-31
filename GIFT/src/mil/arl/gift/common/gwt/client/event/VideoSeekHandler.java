/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link VideoSeekedEvent} and {@link VideoSeekingEvent} events.
 * 
 * @author sharrison
 */
public interface VideoSeekHandler extends EventHandler {

    /**
     * Called when a native seeked event (i.e. "onSeeked") is fired.
     * 
     * @param event the {@link VideoSeekedEvent} that was fired.
     */
    void onSeeked(VideoSeekedEvent event);

    /**
     * Called when a native seeking event (i.e. "onSeeking") is fired.
     * 
     * @param event the {@link VideoSeekedEvent} that was fired.
     */
    void onSeeking(VideoSeekingEvent event);
}
