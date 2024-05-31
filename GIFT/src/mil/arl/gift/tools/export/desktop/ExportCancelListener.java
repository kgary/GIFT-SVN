/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

/**
 * Listens for cancel event from the ExportThread.
 * 
 * @author cdettmering
 */
public interface ExportCancelListener {
	
	/**
	 * Called when the export has finished cancel operations. Use this callback
	 * to react to an export cancel.
	 */
	public void onCancel();
}
