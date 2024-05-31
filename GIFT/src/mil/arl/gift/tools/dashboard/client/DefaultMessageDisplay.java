/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;

/**
 * This class creates reusable instances of {@link DisplaysMessage} required for file selection/upload dialogs 
 * Note: this was started by copying the Gat version of this class with the same name.
 * @author mhoffman
 *
 */
public class DefaultMessageDisplay {
    
    private static Logger logger = Logger.getLogger(DefaultMessageDisplay.class.getName());

    /**
     * Displays info, error, and warning messages accordingly
     */
    public static DisplaysMessage includeAllMessages = new DisplaysMessage() {

        @Override
        public void showInfoMessage(String title, String text, final ModalDialogCallback callback) {
            try{
                UiManager.getInstance().displayInfoDialog(title, text, new DialogCallback() {
                    
                    @Override
                    public void onAccept() {
                        callback.onClose();                        
                    }
                });
            
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Unable to display info message. An unexpected error occurred", e);
            }
        }

        @Override
        public void showErrorMessage(String title, String text, final ModalDialogCallback callback) {
            try { 
                UiManager.getInstance().displayErrorDialog(title, text, new DialogCallback() {
                    
                    @Override
                    public void onAccept() {
                        callback.onClose();                         
                    }
                });

            } catch(Exception e) {
                logger.log(Level.SEVERE, "Unable to display error message. An unexpected error occurred", e);
            }
        }

        @Override
        public void showDetailedErrorMessage(String reason, String details, List<String> stackTrace, final ModalDialogCallback callback) {
            UiManager.getInstance().displayDetailedErrorDialog("Error", reason, details, stackTrace, null, callback);
        }

        @Override
        public void showWarningMessage(String title, String text, final ModalDialogCallback callback) {
            try {
                UiManager.getInstance().displayInfoDialog(title, text, new DialogCallback() {
                    
                    @Override
                    public void onAccept() {
                        callback.onClose();                        
                    }
                });

            } catch(Exception e) {
                logger.log(Level.SEVERE, "Unable to display warning message. An unexpected error occurred", e);
            }
        }
        
    };
}
