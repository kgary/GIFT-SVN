/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.event.util;

import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.event.FullscreenChangeEvent;
import mil.arl.gift.common.gwt.client.event.FullscreenChangeHandler;
import mil.arl.gift.common.gwt.client.event.MozFullscreenChangeEvent;
import mil.arl.gift.common.gwt.client.event.MsFullscreenChangeEvent;
import mil.arl.gift.common.gwt.client.event.WebkitFullscreenChangeEvent;

/**
 * Utility for adding listeners for cross-browser support.
 * 
 * @author sharrison
 */
public class CrossBrowserEventUtil {
    /**
     * Add a fullscreen change listener to the widget.
     * 
     * @param w the widget to add the fullscreen change listener to. Can't be
     *        null.
     * @param handler the handler to listen for fullscreen changes. Can't be
     *        null.
     */
    public static void addFullscreenChangeListener(Widget w, FullscreenChangeHandler handler) {
        w.addDomHandler(handler, FullscreenChangeEvent.getType());
        w.addDomHandler(handler, MozFullscreenChangeEvent.getType());
        w.addDomHandler(handler, WebkitFullscreenChangeEvent.getType());
        w.addDomHandler(handler, MsFullscreenChangeEvent.getType());
    }
}
