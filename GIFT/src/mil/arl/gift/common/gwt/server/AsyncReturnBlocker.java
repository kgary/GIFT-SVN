/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

/**
 * Class for blocking a return until a value is set, for async operations
 *
 * @param <T> The return type
 */
public class AsyncReturnBlocker<T> {

    /**
     * Whether or not a value has been set for this return blocker.
     */
    private boolean handled = false;
    
    /**
     * The amount of time to wait for a return value to be set.
     */
    private static final int TIMEOUT = 600000; // 10 minutes
    
    /**
     * The return value.
     */
    private T returnValue = null;
    
    /**
     * Sets the value of the return.
     *
     * @param returnValue The value of the return to be set to
     */
    public void setReturnValue(T returnValue) {
        this.returnValue = returnValue;
        handled = true;
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * Gets the value of the return, or waits 10 minutes for the return value to be set.
     *
     * @return T The return value or null if the timeout is reached.
     */
    public T getReturnValueOrTimeout() { 
        if(!handled) {
            synchronized (this) {
                try {
                    this.wait(TIMEOUT);
                } catch (@SuppressWarnings("unused") InterruptedException ex) {
                }
            }
        }
        
        return returnValue;
    }
    
    /**
     * Waits indefinitely for the return value.
     * 
     * @return The return value, or null if a timeout was reached.
     */
    public T getReturnValue() {
    	
    	while(!handled) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (@SuppressWarnings("unused") InterruptedException ex) {
                }
            }
        }
    	
        return returnValue;
    }
    
}
