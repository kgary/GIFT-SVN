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
 * Handler for {@link PointerDownEvent} events.
 * 
 * @author nroberts
 */
public interface PointerDownHandler extends EventHandler{
    
    /**
     * Called when a native pointer down event (i.e. "onpointerdown") is fired
     * 
     * @param event the {@link PointerDownEvent} that was fired
     */
    void onPointerDown(PointerDownEvent event);
}
