/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.installer;

/**
 * Listens for cancel event from the InstallThread.
 * 
 * @author mhoffman
 */
public interface InstallCancelListener {
	
	/**
	 * Called when the install has finished cancel operations. Use this callback
	 * to react to an install cancel.
	 * 
	 * @param failureMessage an optional message describing that the installer was canceled because of a failure.
	 * Can be null.
	 */
	public void onCancel(String failureMessage);
}
