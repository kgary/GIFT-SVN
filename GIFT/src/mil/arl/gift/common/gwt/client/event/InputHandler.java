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
 * Handler for {@link InputEvent} events.
 * 
 * @author nroberts
 */
public interface InputHandler extends EventHandler{
    
    /**
     * Called when a native input event (i.e. "oninput") is fired
     * 
     * @param event the {@link InputEvent} that was fired
     */
    void onInput(InputEvent event);
}
