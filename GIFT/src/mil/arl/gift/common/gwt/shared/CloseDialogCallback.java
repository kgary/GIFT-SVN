/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

/**
 * A callback interface used to fire logic when a dialog is closed.
 *  
 * @author bzahid
 */
public interface CloseDialogCallback {
	
	/**
     * Called when the dialog is closed by the user.
     */
    public void onClose();
}
