/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.tools;

/**
 * This interface is used to notify the listener of user input that is provided via the module's user interface,
 * which could be the command prompt (power user mode) or a custom window (learner mode).
 * 
 * @author mhoffman
 *
 */
public interface ModuleUserInterfaceEventListener {

    /**
     * Notification that the user has entered text into the user interface provided.
     * 
     * @param text the text entered.  Can be empty or null.
     */
    public void textEntered(String text);
    
    /**
     * Notification that an error occurred while trying to retrieve the user's provided text.
     * 
     * @param message information about the issue
     */
    public void errorOccurred(String message);
}
