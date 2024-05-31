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
 * Represents a native input event (see https://developer.mozilla.org/en-US/docs/Web/Events/input).
 * 
 * @author nroberts
 */
public class InputEvent extends HumanInputEvent<InputHandler> {
    
    private static final Type<InputHandler> TYPE = new Type<InputHandler>(
            ExtendedBrowserEvents.INPUT, 
            new InputEvent()
    );
    
    /**
     * Gets the event type associated with input events.
     * 
     * @return the handler type
     */
    public static Type<InputHandler> getType() {
        return TYPE;
    }
    
    /**
     * Protected constructor, use
     * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
     * to fire input events.
     */
    protected InputEvent() {
    }

    @Override
    public Type<InputHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(InputHandler handler) {
        handler.onInput(this);
    }

}
