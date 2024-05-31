/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;

/**
 * The Class WarningDialog.
 */
public class WarningDialog extends ModalDialogBox {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(WarningDialog.class.getName());
	
	/** The Constant INFO_IMAGE. */
	private static final Image INFO_IMAGE  = new Image(GatClientBundle.INSTANCE.info_image());  
	
	/** The Constant ALERT_IMAGE. */
	private static final Image ALERT_IMAGE = new Image(GatClientBundle.INSTANCE.alert_image());
	
	/** The Constant WARN_IMAGE. */
	private static final Image WARN_IMAGE  = new Image(GatClientBundle.INSTANCE.warn_image());
	
	/** The Constant ERROR_IMAGE. */
	private static final Image ERROR_IMAGE = new Image(GatClientBundle.INSTANCE.error_image());
	
	/** The Constant INFORMATION String */
	private static final String INFORMATION = "Information";
	
	/** The Constant ALERT String */
	private static final String ALERT = "Alert";
	
	/** The Constant Warning String */
	private static final String WARNING = "Warning";
	
	/** The Constant ERROR String */
	private static final String ERROR = "Error";
		
	/** The Constant INFO_DIALOG. */
	private static final WarningDialog INFO_DIALOG  = new WarningDialog(INFORMATION, INFO_IMAGE);
	
	/** The Constant ALERT_DIALOG. */
	private static final WarningDialog ALERT_DIALOG = new WarningDialog(ALERT, ALERT_IMAGE);
	
	/** The Constant WARN_DIALOG. */
	private static final WarningDialog WARN_DIALOG  = new WarningDialog(WARNING, WARN_IMAGE);
	
	/** The Constant ERROR_DIALOG. */
	private static final WarningDialog ERROR_DIALOG = new WarningDialog(ERROR, ERROR_IMAGE);	
	
	@UiField
	protected HTML htmlMessage;
	
	@UiField
	protected SimplePanel iconContainer;
	
	private static WarningDialogUiBinder uiBinder = GWT
			.create(WarningDialogUiBinder.class);

	interface WarningDialogUiBinder extends UiBinder<Widget, WarningDialog> {
	}
	
	
	
	/**
	 * Instantiates a new warning dialog.
	 *
	 * @param title the title of the dialog.  Can't be null or empty.
	 * @param image the image to show in the dialog.  Can't be null.
	 */
	private WarningDialog(String title, Image image) {
		
		super();
		
		if(title == null || title.isEmpty()){
		    throw new IllegalArgumentException("The title can't be null or empty.");
		}else if(image == null){
		    throw new IllegalArgumentException("The image can't be null.");
		}
		
		setWidget(uiBinder.createAndBindUi(this));
		
		setText(title);
		
		image.getElement().getStyle().setProperty("paddingRight", "15px");
		image.getElement().getStyle().setProperty("box-sizing", "content-box");
		
		iconContainer.add(image);
		
		setGlassEnabled(true);
		setCloseable(true);
	}		
	

	/**
	 * Sets the content.
	 *
	 * @param htmlMessage the new content.  Can't be null.
	 */
	private void setContent(String htmlMessage) {
	    
	    if(htmlMessage == null){
	        htmlMessage = "--Oops.  This is not useful.  The message is null.--";
	    }
	    
		this.htmlMessage.setHTML(htmlMessage);
		this.htmlMessage.getElement().getStyle().setProperty("fontFamily", "Arial");
		this.htmlMessage.getElement().getStyle().setProperty("fontSize", "14px");
	}
	
	/**
	 * Displays an alert dialog, just like WarningDialog.alert, but allows custom caption label</br>
     * Note: this will not hold onto the calling thread.  Use the version of this method with callback
     * to be notified of when the dialog is closed.
	 * 
	 * @param caption Title for the warning dialog (if left blank or null, will use default value for type of dialog)
	 * @param message html message for the body of the dialog
	 */
	public static void alert(String title, String message){				
		createDialog(ALERT_DIALOG, title, message, null);
	}
	
	/**
     * Displays an alert dialog, just like WarningDialog.alert, but allows custom caption label</br>
     * Note: this will not hold onto the calling thread.
     * 
     * @param title Title for the warning dialog. Can't be null or empty.
     * @param message html message for the body of the dialog.  Can't be null or empty.
     * @param callback - (optional) The callback that is called when the modal dialog is closed. Can be null.
     */
    public static void alert(String title, String message, ModalDialogCallback callback){               
        createDialog(ALERT_DIALOG, title, message, callback);
    }
	
	/**
	 * Displays a warning dialog, just like WarningDialog.warning, but allows custom caption label</br>
     * Note: this will not hold onto the calling thread.  Use the version of this method with callback
     * to be notified of when the dialog is closed. 
	 * 
	 * @param message html message for the body of the dialog. Can't be null or empty.
	 * @param message html message for the body of the dialog.  Can't be null or empty.
	 */
	public static void warning(String title, String message){
		createDialog(WARN_DIALOG, title, message, null);
	}
	
	/**
     * Displays a warning dialog, just like WarningDialog.warning, but allows custom caption label</br>
     * Note: this will not hold onto the calling thread.
     * 
     * @param title Title for the warning dialog. Can't be null or empty.
     * @param message html message for the body of the dialog. Can't be null or empty.
     * @param callback - (optional) The callback that is called when the modal dialog is closed.
     */
    public static void warning(String title, String message, ModalDialogCallback callback){
        createDialog(WARN_DIALOG, title, message, callback);
    }
    
    /**
     * Displays an error dialog, just like WarningDialog.error, but allows custom caption label</br>
     * Note: this will not hold onto the calling thread.
     * 
     * @param title Title for the warning dialog. Can't be null or empty.
     * @param message html message for the body of the dialog. Can't be null or empty.
     * @param callback - (optional) The callback that is called when the modal dialog is closed.
     */
    public static void error(String title, String message, ModalDialogCallback callback){
        createDialog(ERROR_DIALOG, title, message, callback);
    }
	
	/**
	 * Displays an error dialog, just like WarningDialog.error, but allows custom caption label</br>
	 * Note: this will not hold onto the calling thread.  Use the version of this method with callback
	 * to be notified of when the dialog is closed.
	 * 
	 * @param title Title for the warning dialog. Can't be null or empty.
	 * @param message html message for the body of the dialog. Can't be null or empty.
	 */
	public static void error(String title, String message){
		createDialog(ERROR_DIALOG, title, message, null);
	}
	
	/**
	 * Displays an info dialog, just like WarningDialog.info, but allows custom caption label
     * Note: this will not hold onto the calling thread.  Use the version of this method with callback
     * to be notified of when the dialog is closed.
	 * 
	 * @param title Title for the warning dialog. Can't be null or empty.
	 * @param message html message for the body of the dialog. Can't be null or empty.
	 */
	public static void info(String title, String message){
		createDialog(INFO_DIALOG, title, message, null);
	}
	
	/**
     * Displays an info dialog, just like WarningDialog.info, but allows custom caption label</br>
     * Note: this will not hold onto the calling thread.
     * 
     * @param title Title for the warning dialog. Can't be null or empty.
     * @param message html message for the body of the dialog. Can't be null or empty.
     * @param callback - (optional) The callback that is called when the modal dialog is closed.
     */
    public static void info(String title, String message, ModalDialogCallback callback){
        createDialog(INFO_DIALOG, title, message, callback);
    }

    /**
     * Hides all instances of the warning dialog
     */
    public static void hideAll() {
        /* Need to defer this action because a dialog might be performing an
         * animation */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                INFO_DIALOG.hide();
                ALERT_DIALOG.hide();
                WARN_DIALOG.hide();
                ERROR_DIALOG.hide();
            }
        });
    }

	/**
	 * Method used to set all the information for each type of WarningDialog. Will set title to default if left null
	 * or empty or custom text if desired. Will use the default icon based on the type of dialog used (e.g. error, warning)
	 * All WarningDialogs come to this method for setup.
	 * 
	 * @param dialog The type of dialog (warning, error, etc)
	 * @param title The desired title. Can't be null or empty.
	 * @param message html message for the body of the dialog. Can't be null or empty.
	 */
	private static void createDialog(final WarningDialog dialog, String title, String message, ModalDialogCallback callback){
	    dialog.setCallback(callback);
	    
        if(title == null || title.isEmpty()){
            logger.severe("TODO: Missing title for warningdialog createdialog method call with message: "+message);
            title = "--Oops.  This is not useful.  The title is null/empty--";
        }else if(message == null || message.isEmpty()){
            logger.severe("TODO: Missing message for warningdialog createdialog method call with title: "+title);
            message = "--Oops.  This is not useful.  The message is null/empty--";
        }
	    
		if(title == null || title.isEmpty()){
			if(dialog.equals(ALERT_DIALOG)){
				dialog.setText(ALERT);
			}
			else if(dialog.equals(WARN_DIALOG)){
				dialog.setText(WARNING);
			}
			else if(dialog.equals(INFO_DIALOG)){
				dialog.setText(INFORMATION);
			}
			else if(dialog.equals(ERROR_DIALOG)){
				dialog.setText(ERROR);
			}
		}
		else{
			dialog.setText(title);
		}
		dialog.setContent(message);
		
        //need to schedule a command to execute after content is attached
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            
            @Override
            public void execute() {
                dialog.center();
            }
        });
	}
	
	/**
	 * Used to show warning that file name contains illegal characters.
	 * Used across all the editors/presenters.
	 * Will halt the thread of the caller until closed. 
	 * 
	 * @param validationMsg - the message about the illegal characters being used
	 */
	public static void illegalCharactersWarning(String validationMsg){
		createDialog(WARN_DIALOG, "Illegal Characters in File Name", validationMsg, null);
	}
	
	
}
