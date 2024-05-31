/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Paragraph;



/**
 * Bootstrap widget used to display informational dialogs to the user.  These are
 * a simple modal class that can be dismissed by the user.  An optional callback
 * can be passed in to be used to respond to the dialog confirmation.  
 * The modal can be an informational type of modal versus
 * an error type of modal.
 *
 * @author nblomberg
 */
public class BsDialogWidget extends AbstractBsWidget {
    
    private static Logger logger = Logger.getLogger(BsDialogWidget.class.getName()); 
    
    private static BsDialogWidgetUiBinder uiBinder = GWT.create(BsDialogWidgetUiBinder.class);

    // Used to specify how to show the dialog.
    public enum DialogType {
        DIALOG_INFO,
        DIALOG_ERROR
    }
    
    @UiField
    Modal dialogModal;
   
    
    @UiField
    Paragraph dialogMessage;
    
    @UiField
    Paragraph dialogAdditionalDetails;
    
    @UiField
    Icon infoIcon;
    
    @UiField
    Icon warningIcon;
    

    @UiField
    Button okButton;
    
    @UiField
    Label ctrlTitle;

    interface BsDialogWidgetUiBinder extends UiBinder<Widget, BsDialogWidget> {
    }

    HandlerRegistration acceptHandler = null;
    
    /** Used to indicate if the dialog is shown or not */
    private boolean isShown = false;
    
    /** The type of dialog this widget represents */
    private DialogType dialogType = DialogType.DIALOG_INFO;
    /** The title of the dialog */
    private String dialogTitle = "";
    /** The message of the dialog */
    private String dialogMessageData = "";
    /** Any additional message for the dialog */
    private String dialogAdditionalMessage = "";
    
    /**
     * Constructor
     */
    public BsDialogWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        dialogModal.getElement().getFirstChildElement().getStyle().setProperty("height", "85%");
        Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
					@Override
		            public void execute() {
						BsDialogWidget.this.onResize();
					}
				});
			}
        });
    }
    
    /**
     * A callback interface for how the user responds to the dialog
     */
    public interface DialogCallback {

        /**
         * Called when the dialog is accepted by the user
         */
        public void onAccept();
    }
    
    
    /**
     * Sets the data for the dialog (but does not explicitly show the dialog).
     * 
     * @param type - The type of dialog to display (eg. informational vs error)
     * @param title - The title of the dialog (cannot be null).  The title should be straight forward and to the point.
     * Longer titles will not fit on the current dialogs width.
     * @param message - The message of the dialog (cannot be null).  HTML format is allowed.  '\n' will be converted into '<br>'
     * @param additionalMsg - An optional advanced or additional detail message that provided.  This can be null if not needed.
     * HTML format is allowed.  '\n' will be converted into '<br>'
     */
    public void setData(DialogType type, String title, String message, String additionalMsg, final DialogCallback callback) {
        
        message = message.replaceAll("(\\r|\\n|\\r\\n)+", "<br>");
        
        dialogType = type;
        dialogTitle = title;
        dialogMessageData = message;
        dialogAdditionalDetails.setVisible(false);

        dialogMessage.setHTML(message);
        
        // Set the title for the dialog.  Add a little spacing here, because the icon will be to the left of the text.
        ctrlTitle.setText(" " + title);
        
        // Only show the additional details if there are any to show.
        if (additionalMsg != null && 
                additionalMsg != "") {
                additionalMsg = additionalMsg.replaceAll("(\\r|\\n|\\r\\n)+", "<br>");
                dialogAdditionalDetails.setHTML(additionalMsg);
                dialogAdditionalDetails.setVisible(true);
        }
        
        dialogAdditionalMessage = additionalMsg;

        // Setup the icons for the dialog.  
        infoIcon.setVisible(false);
        warningIcon.setVisible(false);
 
        if (type == DialogType.DIALOG_INFO) {
            
            infoIcon.setVisible(true);
            
        } else if (type == DialogType.DIALOG_ERROR) {
            warningIcon.setVisible(true);
        }
        
        // Clear any existing button handler that may exist.
        clearButtonHandler();
        
        acceptHandler = okButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logger.fine("Click event occurred: " + event);
                isShown = false;
                clearButtonHandler();
                if (callback != null) {
                    callback.onAccept();
                }
            }
        });
        
    }
    
    
    /**
     * Removes any handlers associated with the accept button.
     * This is needed so that there is only "one click handler" registered
     * at any given time.  
     */
    private void clearButtonHandler() {
        
        logger.fine("clearing button handler");
        // Remove any previous handlers.
        if (acceptHandler != null) {
            logger.fine("removing accept handler");
            acceptHandler.removeHandler();
        }
        
    }
    
    
    /**
     * Accessor to show the modal dialog.
     */
    public void show() {
        dialogModal.show();
        isShown = true;
        onResize();
    }
    
    /**
     * Accessor to hide the modal dialog.
     */
    public void hide() {
        dialogModal.hide();
        isShown = false;
    }
    
    
    /**
     * Accessor to determine if the modal is being shown.
     * 
     * @return true if the modal is being shown, false otherwise.
     */
    public boolean isModalShown() {
        return isShown;
    }


    /**
     * Determines if the dialog is considered the 'same' by checking to see if the underlying
     * dialog data (type, title, description, additionalMsg) are the same.
     * 
     * @param type - the type of dialog (DIALOG_INFO, DIALOG_ERROR) to check to against.
     * @param title - string to check if it is the same as the current dialog title.
     * @param description - string to check if it is the same as the current dialog message.
     * @param additionalMsg - string to check if it is the same as the current dialog additional message.
     * @return true if the dialog has the same title & description, false otherwise.
     */
    public boolean isSameDialog(DialogType type, String title, String description, String additionalMsg) {
        
        boolean isSameDialog = false;
        
        if (this.dialogType == type &&
            this.dialogTitle.compareTo(title) == 0 &&
            this.dialogMessageData.compareTo(description) == 0 &&
            this.dialogAdditionalMessage.compareTo(additionalMsg) == 0) {
            isSameDialog = true;
        }
        
        return isSameDialog;
    }
    
    /**
     * Resizes the dialog based on the browser height
     */
    private void onResize() {
		int pad = 150;
		int height = dialogModal.getElement().getFirstChildElement().getOffsetHeight();
		dialogMessage.getElement().getStyle().setProperty("maxHeight", (height - pad) + "px");
	}
}
