/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.socket;

/**
 * Used to notify the listener of application states received by GIFT from an external training application.
 * 
 * @author mhoffman
 *
 */
public interface ApplicationStateListener {

    /**
     * A new training application state message was received.
     * 
     * @param state contains the characters read from the network connection.
     */
    public void handleApplicationState(String state);
}
