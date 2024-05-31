/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

/**
 * A callback that executes logic when an operation is cancelled
 * 
 * @author nroberts
 */
public interface CancelCallback {
	
	/**
	 * Executes some logic when an operation is cancelled
	 */
	public void onCancel();
}
