/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

/**
 * An interface used to allow callers to navigate the current session time throughout
 * a knowledge session's timeline
 * 
 * @author nroberts
 *
 */
public interface TimelineNavigator {

    /**
     * Initiates a server request to move the current session playback time to the given date. This will
     * consequently update any client-side widgets that are listening to the session playback.
     * 
     * @param dateMillis the data to move the playback time to, in milliseconds
     */
    public void seekTo(long timestamp);
}
