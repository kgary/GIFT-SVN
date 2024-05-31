/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

/**
 * Interface used for dialog box callbacks
 * @author tflowers
 *
 */
public interface DeleteRemoveCancelCallback {
    
    /** 
     * Callback used when the delete button is clicked 
     * */
    void delete();
    
    /** 
     * Callback used when the remove button is clicked 
     * */
    void remove();
    
    /** 
     * Callback used when the cancel button is clicked 
     * */
    void cancel();
}
