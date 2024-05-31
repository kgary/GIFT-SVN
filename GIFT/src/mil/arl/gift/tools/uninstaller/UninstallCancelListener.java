/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.uninstaller;

/**
 * Listens for cancel event from the UninstallThread.
 * 
 * @author mhoffman
 */
public interface UninstallCancelListener {
	
	/**
	 * Called when the uninstall has finished cancel operations. Use this callback
	 * to react to an uninstall cancel.
	 * 
     * @param failureMessage an optional message describing that the uninstaller was canceled because of a failure.
     * Can be null.
     */
    public void onCancel(String failureMessage);
}
