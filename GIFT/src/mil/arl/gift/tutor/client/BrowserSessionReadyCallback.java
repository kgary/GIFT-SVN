/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;


/**
 * BrowserSessionReadyCallback allows for callback method to be executed based on if the web socket could be opened properly for
 * the browser session or not.  
 * 
 * @author nblomberg
 *
 */
public interface BrowserSessionReadyCallback  {
   

    /**
     * Called when the session was created successfully.
     */
    void onSessionSuccess();
    

    /**
     * Called when the session was not created successfully.
     */
    void onSessionFailure();
    
   
}
