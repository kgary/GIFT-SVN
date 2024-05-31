/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

/**
 * This interface is used to notify listeners about message client events.
 *
 * @author mhoffman
 */
public interface MessageClientConnectionListener {

    /**
     * The message client connection was opened
     *
     * @param client The client that opened a connection
     */
    void connectionOpened(MessageClient client);

    /**
     * The message client connection was lost
     *
     * @param client The client that lost a connection
     */
    void onConnectionLost(MessageClient client);

    /**
     * The message client connection has closed.
     *
     * @param client The client that closed a connection
     */
    void connectionClosed(MessageClient client);
}