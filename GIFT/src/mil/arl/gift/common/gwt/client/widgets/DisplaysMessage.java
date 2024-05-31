/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import java.util.List;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;

/**
 * An object capable of displaying messages to the user
 * 
 * @author nroberts
 */
public interface DisplaysMessage {

	/**
	 * Shows an informative message to the user.
	 * Note: this will not hold onto the calling thread.
	 * 
	 * @param title the dialog title. Can't be null or empty.
	 * @param text the message's text. Can't be null or empty.
	 * @param callback used to be notified of dialog events (e.g. closing).  Can be null.
	 */
	public void showInfoMessage(String title, String text, ModalDialogCallback callback);
	
	/**
	 * Shows an error message to the user
	 * Note: this will not hold onto the calling thread.
	 * 
	 * @param title the dialog title. Can't be null or empty.
	 * @param text the message's text. Can't be null or empty.
	 * @param callback used to be notified of dialog events (e.g. closing).  Can be null.
	 */
	public void showErrorMessage(String title, String text, ModalDialogCallback callback);
	
	/**
	 * Shows a detailed error message to the user
	 * Note: this will not hold onto the calling thread.
	 * 
	 * @param reason A user friendly reason for this error. Can't be null or empty.
	 * @param details The error details. Can't be null or empty.
	 * @param stackTrace The error stack trace. Can be null.
	 * @param callback used to be notified of dialog events (e.g. closing).  Can be null.
	 */
	public void showDetailedErrorMessage(String reason, String details, List<String> stackTrace, ModalDialogCallback callback);
	
	/**
	 * Shows a warning message to the user
	 * Note: this will not hold onto the calling thread.
	 * 
	 * @param title the dialog title. Can't be null or empty.
	 * @param text the message's text. Can't be null or empty.
	 * @param callback used to be notified of dialog events (e.g. closing).  Can be null.
	 */
	public void showWarningMessage(String title, String text, ModalDialogCallback callback);
}
