/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

/**
 * A listener for changes in session statuses
 *
 * @author jleonard
 */
public interface SessionStatusListener {
    
    /**
     * Called when the session being listened to is stopped
     */
    void onStop();
    
    /**
     * Called when the session being listened to is ended
     */
    void onEnd();
    
}
