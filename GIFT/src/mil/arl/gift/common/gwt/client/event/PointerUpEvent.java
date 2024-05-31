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
 * pointer up event</a>
 * 
 * @author nroberts
 */
public class PointerUpEvent extends HumanInputEvent<PointerUpHandler> {
    
    private static final Type<PointerUpHandler> TYPE = new Type<PointerUpHandler>(
            ExtendedBrowserEvents.POINTER_UP, 
            new PointerUpEvent()
    );
    
    /**
     * Gets the event type associated with pointer up events.
     * 
     * @return the handler type
     */
    public static Type<PointerUpHandler> getType() {
        return TYPE;
    }
    
    /**
     * Protected constructor, use
     * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
     * to fire pointer up events.
     */
    protected PointerUpEvent() {
    }

    @Override
    public Type<PointerUpHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PointerUpHandler handler) {
        handler.onPointerUp(this);
    }

}
