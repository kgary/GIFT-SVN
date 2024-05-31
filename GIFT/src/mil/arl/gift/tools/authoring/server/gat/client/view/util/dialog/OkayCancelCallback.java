/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;


/**
 * Interface used for dialog box callbacks.
 * 
 * @author cragusa
 *
 */
public interface OkayCancelCallback {
    
    /**
     * Callback used when the user clicked the Okay button.
     */
	void okay();	
	
	/**
	 * Callback used when the user clicks the Cancel button.
	 */
	void cancel();
}
