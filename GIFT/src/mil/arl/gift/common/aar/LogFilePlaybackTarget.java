/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import mil.arl.gift.net.api.message.Message;

/**
 * Interface used for handling the playback of a log file.
 * 
 * @author mhoffman
 *
 */
public interface LogFilePlaybackTarget {
    
    /**
     * Handle the playback of the given message.
     * 
     * @param msg a message from the log file to handle playback of.  Won't be null.
     */
    void handlePlayedbackMessage(Message msg);
}
