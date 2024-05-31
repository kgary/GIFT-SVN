/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.ModalHiddenEvent;
import org.gwtbootstrap3.client.shared.event.ModalHiddenHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;

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
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;



/**
 * Widget used to display a confirmation dialog (Yes/No choice) to the user.  After the choice is made,
 * a callback is made signaling which choice the user selected.
 *
 * @author nblomberg
 */
public class BsDialogConfirmWidget extends AbstractBsWidget {

    private static Logger logger = Logger.getLogger(BsDialogConfirmWidget.class.getName());
    
    private static BsDialogConfirmWidgetUiBinder uiBinder = GWT.create(BsDialogConfirmWidgetUiBinder.class);
    
    private final String CONFIRM_LABEL_DEFAULT = "Yes";
    private final String DECLINE_LABEL_DEFAULT = "No";
    
    
    /** Used to indicate if the dialog is shown or not */
    private boolean isShown = false;
    
    /** The title of the dialog */
    private String dialogTitle = "";
    /** The message of the dialog */
    private String dialogMessageData = "";

    
    @UiField
    Modal confirmModal;
   
    
    @UiField
    Paragraph dialogMessage;

    @UiField
    Button declineButton;
    
    @UiField
    Button confirmButton;
    
    @UiField
    Text ctrlTitle;
    
    // Registered handler for the confirm button.  This interface is used to clear the handlers once we are done with the button actions.
    HandlerRegistration confirmHandler = null;
    
    // Registered handler for the decline button.  This interface is used to clear the handlers once we are done with the button actions.
    HandlerRegistration declineHandler = null;

    /** Handler for the modal hidden action */
    private com.google.web.bindery.event.shared.HandlerRegistration modalHiddenHandler = null;

    interface BsDialogConfirmWidgetUiBinder extends UiBinder<Widget, BsDialogConfirmWidget> {
    }
    
    
    /**
     * A callback interface for how the user responds to a confirmation dialog
     */
    public interface ConfirmationDialogCallback {

        /**
         * Called when the dialog is declined by the user
         */
        public void onDecline();

        /**
         * Called when the dialog is accepted by the user
         */
        public void onAccept();
    }

    /**
     * Constructor
     */
    public BsDialogConfirmWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        confirmModal.getElement().getFirstChildElement().getStyle().setProperty("height", "85%");
        Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
					@Override
		            public void execute() {
						BsDialogConfirmWidget.this.onResize();
					}
				});
			}
        });
        declineButton.setText(DECLINE_LABEL_DEFAULT);
        confirmButton.setText(CONFIRM_LABEL_DEFAULT);
    }
    
    /**
     * Sets the data for the confirmation dialog.  This is a dialog with a 'yes/'no' option.  A callback is made 
     * after the user picks one of 'yes' or 'no'.  This function only sets the data of the dialog, but does not
     * display or hide it.
     * @param title - The title for the dialog (cannot be null).
     * @param message - The message for the dialog (cannot be null).
     * @param confirmLabel - (optional) The title of the confirm action button.  If null is specified, then the default title "Yes" is used.
     * @param declineLabel - (optional) The title of the decline action button.  If null is specified, then the default title "No" is used.  
     * @param callback - The callback to be made when the user select yes or no (cannot be null).
     */
    public void setData(String title, String message, String confirmLabel, String declineLabel, final ConfirmationDialogCallback callback) {
        
        dialogTitle = title;
        dialogMessageData = message;
        
        // Set the text for the dialog.
        //confirmModal.setTitle(title);
        dialogMessage.setHTML(message);
        
        // Add a spacing here for the icon, which is immediately to the left of the title.  
        ctrlTitle.setText(" " + title);
        
        if (confirmLabel != null && !confirmLabel.isEmpty()) {
            confirmButton.setText(confirmLabel);
        } else {
           confirmButton.setText(CONFIRM_LABEL_DEFAULT);
        }
        
        if (declineLabel != null && !declineLabel.isEmpty()) {
            declineButton.setText(declineLabel);
        } else {
            declineButton.setText(DECLINE_LABEL_DEFAULT);
        }
        
        
        // Safety check to clear any button handlers that could exist.  If these are NOT cleared
        // then the effect is that multiple callbacks/click events can be fired, which is not what we want.
        clearButtonHandlers();
        
        
        // Setup click handlers for the buttons.
        declineHandler = declineButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logger.info("Decline clicked: " + event);
                clearButtonHandlers();
                hide(new ModalDialogCallback() {
                    @Override
                    public void onClose() {
                        if (callback != null) {
                            callback.onDecline();
                        }
                    }
                });
            }
        });
        
        
        confirmHandler = confirmButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logger.info("Confirm clicked: " + event);
                clearButtonHandlers();
                hide(new ModalDialogCallback() {
                    @Override
                    public void onClose() {
                        if (callback != null) {
                            callback.onAccept();
                        }
                    }
                });
            }
        });
    }
    
    
    /**
     * Sets the data for the confirmation dialog.  This is a dialog with a 'yes/'no' option.  A callback is made 
     * after the user picks one of 'yes' or 'no'.  This function only sets the data of the dialog, but does not
     * display or hide it.
     * @param title - The title for the dialog (cannot be null).
     * @param message - The message for the dialog (cannot be null).
     * @param callback - The callback to be made when the user select yes or no (cannot be null).
     */
    public void setData(String title, String message, final ConfirmationDialogCallback callback) {
              
        setData(title, message, CONFIRM_LABEL_DEFAULT, DECLINE_LABEL_DEFAULT, callback);
    }
    
    
    /**
     * Clear the confirm and decline button 'onclick' handlers.  
     * This is required so that we don't continually add 'handlers' to the button
     * and trigger multiple 'onclick' events with a single button press.  
     */
    private void clearButtonHandlers() {
        
        logger.fine("clearing button handlers");
        // Remove any previous handlers.
        if (confirmHandler != null) {
            logger.fine("removing confirmation handler");
            confirmHandler.removeHandler();
            confirmHandler = null;
        }
        
        if (declineHandler != null) {
            logger.fine("removing decline handler");
            declineHandler.removeHandler();
            declineHandler = null;
        }
    }
    
    
    /**
     * Accessor to show the modal dialog.
     */
    public void show() {
        confirmModal.show();
        isShown = true;
        onResize();
    }
    
    /**
     * Accessor to hide the modal dialog.
     */
    public void hide() {
        hide(null);
    }

    /**
     * Accessor to hide the modal dialog.
     * 
     * @param callback callback to be notified when the dialog has finished
     *        closing. Can be null.
     */
    public void hide(final ModalDialogCallback callback) {
        if (!isModalShown()) {
            if (callback != null) {
                callback.onClose();
            }
            return;
        }

        /* Reset hidden handler */
        if (modalHiddenHandler != null) {
            modalHiddenHandler.removeHandler();
        }

        if (callback != null) {
            modalHiddenHandler = confirmModal.addHiddenHandler(new ModalHiddenHandler() {
                @Override
                public void onHidden(ModalHiddenEvent evt) {
                    callback.onClose();
                }
            });
        }

        isShown = false;
        confirmModal.hide();
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
     * Determines if the dialog is already displaying the same title & message.
     * 
     * @param title - string to check if it is the same as the current dialog title.
     * @param description - string to check if it is the same as the current dialog message.
     * @return true if the dialog has the same title & description, false otherwise.
     */
    public boolean isSameDialog(String title, String description) {
        
        boolean isSameDialog = false;
        
        if (this.dialogTitle.compareTo(title) == 0 &&
            this.dialogMessageData.compareTo(description) == 0) {
            isSameDialog = true;
        }
        
        return isSameDialog;
    }

    /**
     * Resizes the dialog based on the browser height
     */
    private void onResize() {
		int pad = 150;
		int height = confirmModal.getElement().getFirstChildElement().getOffsetHeight();
		dialogMessage.getElement().getStyle().setProperty("maxHeight", (height - pad) + "px");
	}
}
