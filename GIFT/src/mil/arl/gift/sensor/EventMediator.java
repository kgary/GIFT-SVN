/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Event Mediator is responsible for notifying listeners of events.  It offers a threaded
 * queue implementation for enqueue and dequeue operations.
 * 
 * @author mhoffman
 *
 */
public class EventMediator extends Thread {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EventMediator.class);
    
    private static final String MEDIATOR_SUFFIX = ":EVENT_MEDIATOR";
    
    /** queue where events are placed and serviced*/
    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(15000);
    
    /** classes that want to be notified of events from this mediator */
    private List<EventMediatorListener> listeners = new ArrayList<EventMediatorListener>();

    /**
     * The name given to the mediator. This will be displayed in the log when
     * debug information is outputted.
     */
    private String mediatorName;
    
    /**
     * Class constructor
     * 
     * @param mediatorName The display name given to the mediator.
     */
    public EventMediator(String mediatorName){
        super(mediatorName + MEDIATOR_SUFFIX);
        this.mediatorName = mediatorName;
    }
    
    /**
     * Add a listener 
     * 
     * @param listener a new listener to add
     */
    public void addListener(EventMediatorListener listener){
        
        if(listener == null){
            throw new IllegalArgumentException("The listener can't be null.");
        }
        listeners.add(listener);
    }
    
    /**
     * Remove a listener
     * 
     * @param listener the listener to remove
     * @return boolean whether the list contained the specified element 
     */
    public boolean removeListener(EventMediatorListener listener){
        return listeners.remove(listener);
    }
    
    @Override
    public void run() {

        logger.debug("Starting event processing on meditator: "
            + mediatorName);

        while (true) {
            try {
                
                //Note: blocking call
                Object event = queue.take();

                for(EventMediatorListener listener : listeners){
                    
                    try{
                        listener.notify(event);
                    }catch(Exception e){
                        logger.error("Caught exception from misbehaving event listener of "+listener, e);
                    }
                }
                
            } catch (Throwable t) {

                logger.error("Caught Throwable while notifying the event listeners", t);

            }
        }
    }
    
    /**
     * Add an event to the queue for processing
     * 
     * @param event - event to queue up
     */
    public void enqueue(Object event) {

        try {
            queue.add(event);
        } catch (IllegalStateException ise) {

            logger.error("exception trying to add to external queue! event discarded.", ise);
            logger.error("the queue may be full, should not happen.");
        }

    }
    
    /**
     * Clears the event queue.
     */
    public void clearEventQueue() {

        try {
            queue.clear();

        } catch (Exception e) {
            logger.error("Exception caught: ", e);
        }
    }
}
