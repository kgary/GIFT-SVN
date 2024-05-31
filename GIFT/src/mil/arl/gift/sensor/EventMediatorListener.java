/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

/**
 * This interface allows event mediators to notify classes of events
 * 
 * @author mhoffman
 *
 */
public interface EventMediatorListener {

    /**
     * Notification of an event from an event mediator
     * 
     * @param event to process
     */
    void notify(Object event);
}
