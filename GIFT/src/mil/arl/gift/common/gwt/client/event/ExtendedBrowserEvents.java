/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.event;

/**
 * Constant strings representing browser events that aren't listed in GWT's
 * built-in {@link com.google.gwt.dom.client.BrowserEvents BrowserEvents} class.
 * 
 * @author nroberts
 */
public class ExtendedBrowserEvents {

    /**
     * The 'input' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/Events/input)
     */
    public static final String INPUT = "input";

    /**
     * The 'seeked' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement/seeked_event)
     */
    public static final String SEEKED = "seeked";

    /**
     * The 'seeking' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement/seeking_event)
     */
    public static final String SEEKING = "seeking";

    /**
     * The standard 'fullscreenchange' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Document/fullscreenchange_event
     * and https://www.w3schools.com/JSREF/event_fullscreenchange.asp)
     */
    public static final String FULLSCREEN_CHANGE = "fullscreenchange";

    /**
     * The 'fullscreenchange' event type for Firefox (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Document/fullscreenchange_event
     * and https://www.w3schools.com/JSREF/event_fullscreenchange.asp)
     */
    public static final String MOZ_FULLSCREEN_CHANGE = "mozfullscreenchange";

    /**
     * The 'fullscreenchange' event type for Chrome, Safari, and Opera (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Document/fullscreenchange_event
     * and https://www.w3schools.com/JSREF/event_fullscreenchange.asp)
     */
    public static final String WEBKIT_FULLSCREEN_CHANGE = "webkitfullscreenchange";

    /**
     * The 'fullscreenchange' event type for IE/Edge (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Document/fullscreenchange_event
     * and https://www.w3schools.com/JSREF/event_fullscreenchange.asp)
     */
    public static final String MS_FULLSCREEN_CHANGE = "msfullscreenchange";
    
    /**
     * The 'pointerdown' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events#event_types_and_global_event_handlers)
     */
    public static final String POINTER_DOWN = "pointerdown";
    
    /**
     * The 'pointerup' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events#event_types_and_global_event_handlers)
     */
    public static final String POINTER_UP = "pointerup";
    
    /**
     * The 'pointerout' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events#event_types_and_global_event_handlers)
     */
    public static final String POINTER_OUT = "pointerout";
    
    /**
     * The 'pointermove' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events#event_types_and_global_event_handlers)
     */
    public static final String POINTER_MOVE = "pointermove";
    
    /**
     * The 'pointerover' event type (see
     * https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events#event_types_and_global_event_handlers)
     */
    public static final String POINTER_OVER = "pointerover";
}
