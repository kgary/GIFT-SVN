/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.event;

import com.google.gwt.event.dom.client.HumanInputEvent;

/**
 * Represents a native 
 * <a href='https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events#event_types_and_global_event_handlers'>
 * pointer down event</a>
 * 
 * @author nroberts
 */
public class PointerDownEvent extends HumanInputEvent<PointerDownHandler> {
    
    private static final Type<PointerDownHandler> TYPE = new Type<PointerDownHandler>(
            ExtendedBrowserEvents.POINTER_DOWN, 
            new PointerDownEvent()
    );
    
    /**
     * Gets the event type associated with pointer down events.
     * 
     * @return the handler type
     */
    public static Type<PointerDownHandler> getType() {
        return TYPE;
    }
    
    /**
     * Protected constructor, use
     * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
     * to fire pointer down events.
     */
    protected PointerDownEvent() {
    }

    @Override
    public Type<PointerDownHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PointerDownHandler handler) {
        handler.onPointerDown(this);
    }

}
