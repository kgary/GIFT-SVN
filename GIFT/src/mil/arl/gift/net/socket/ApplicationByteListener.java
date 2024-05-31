/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.socket;

/**
 * Used to notify the listener of incoming byte data received by GIFT from an external training application.
 * 
 * @author mcambata
 *
 */
public interface ApplicationByteListener {

    /**
     * New byte data was received.
     * 
     * @param byteInput contains the byte array read from the network connection.
     */
    public void handleApplicationByteInput(byte[] byteInput);
}
