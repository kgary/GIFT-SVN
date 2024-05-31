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
 * Handler for {@link FullscreenChangeEvent} events.
 * 
 * @author sharrison
 */
public interface FullscreenChangeHandler extends EventHandler {

    /**
     * Called when a native fullscreen change event (i.e. "onFullscreenChange")
     * is fired.
     * 
     * @param event the {@link FullscreenChangeEvent} that was fired.
     */
    void onFullscreenChange(FullscreenChangeEvent event);
}
