/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import mil.arl.gift.common.gwt.server.BrowserWebSession;

/**
 * A listener for when browser sessions are removed
 *
 * @author nblomberg
 */
public interface BrowserSessionListener {
    
    /*
     * Called when the session being listened to is ended
     */
    void onBrowserSessionEnding(BrowserWebSession webSession);
    
}
