/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import java.util.HashSet;
import java.util.Set;



/**
 * The AbstractIFrameMessageHandler class is responsible for maintaining a set of message listeners
 * for classes that extend this class.  These message listeners can be used to receive messages
 * between cross-domain communication (via iframes).  
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractIFrameMessageHandler  {

   
    protected Set<IFrameMessageListener> messageListeners = new HashSet<IFrameMessageListener>();
    
    // Boolean to indicate if the message handler has been initialized.  Initialization should only happen once.
    protected boolean initialized = false;
    
    /**
     * Adds a message listener that will handle messages received.
     * When the message listener no longer is interested, then the removeMessageListener
     * function should be called.
     * 
     * @param listener - The message listener to add (should not be null).
     */
    public void addMessageListener(IFrameMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.add(listener);
        }
    }

    /**
     * Removes a message listener that no longer needs to respond to messages.
     * 
     * @param listener - The listener to be removed (should not be null).
     */
    public void removeMessageListener(IFrameMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.remove(listener);
        }
    }
    
}
