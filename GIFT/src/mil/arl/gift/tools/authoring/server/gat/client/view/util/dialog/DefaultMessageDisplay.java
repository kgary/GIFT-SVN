/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;

/**
 * This class creates reusable instances of {@link DisplaysMessage} required for file selection/upload dialogs 
 * 
 * @author bzahid
 */
public class DefaultMessageDisplay {
    
    private static Logger logger = Logger.getLogger(DefaultMessageDisplay.class.getName());
	
	/**
	 * Displays info, error, and warning messages accordingly
	 */
	public static DisplaysMessage includeAllMessages = new DisplaysMessage() {

		@Override
		public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
			try{
			    WarningDialog.info(title, text, callback);
			
			} catch(Exception e) {
			    logger.log(Level.SEVERE, "Unable to display info message. An unexpected error occurred", e);
			}
		}

		@Override
		public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
			try { 
			    WarningDialog.error(title, text, callback);

            } catch(Exception e) {
                logger.log(Level.SEVERE, "Unable to display error message. An unexpected error occurred", e);
            }
		}

		@Override
		public void showDetailedErrorMessage(String reason, String details, List<String> stackTrace, ModalDialogCallback callback) {
			ErrorDetailsDialog error = new ErrorDetailsDialog(reason, details, stackTrace);
			error.setCallback(callback);
            error.setText("Error");
            error.center();
		}

		@Override
		public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
		    try {
		        WarningDialog.warning(title, text, callback);

            } catch(Exception e) {
                logger.log(Level.SEVERE, "Unable to display warning message. An unexpected error occurred", e);
            }
		}
		
	};
	
	/**
	 * Displays warning and error messages accordingly
	 */
	public static DisplaysMessage ignoreInfoMessage = new DisplaysMessage() {

		@Override
		public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
			// Do nothing
		}

		@Override
		public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
			 WarningDialog.error(title, text, callback);
		}

		@Override
		public void showDetailedErrorMessage(String reason, String details, List<String> stackTrace, ModalDialogCallback callback) {
			ErrorDetailsDialog error = new ErrorDetailsDialog(reason, details, stackTrace);
			error.setCallback(callback);
            error.setText("Error");
            error.center();
		}

		@Override
		public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
			WarningDialog.warning(title, text, callback);
		}
		
	};

}
