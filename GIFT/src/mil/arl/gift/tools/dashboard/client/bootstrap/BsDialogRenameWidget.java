/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;



/**
 * Bootstrap modal used to prompt the user to enter text.
 *
 * @author bzahid
 */
public class BsDialogRenameWidget extends AbstractBsWidget implements HasValue<String> {
    
    private static Logger logger = Logger.getLogger(BsDialogRenameWidget.class.getName()); 
    
    private static BsDialogRenameWidgetUiBinder uiBinder = GWT.create(BsDialogRenameWidgetUiBinder.class);

    
    @UiField
    Modal dialogModal;
       
    @UiField
    Paragraph dialogMessage;
    
    @UiField
	Paragraph invalidMessage;
    
    @UiField
    TextBox textbox;
    
    @UiField
    Button okButton;
    
    @UiField
    Button cancelButton;
    
    @UiField
    Text ctrlTitle;

    interface BsDialogRenameWidgetUiBinder extends UiBinder<Widget, BsDialogRenameWidget> {
    }

    HandlerRegistration acceptHandler = null;
    
    HandlerRegistration changeHandler = null;
    
    HandlerRegistration declineHandler = null;
    
    /** Used to indicate if the dialog is shown or not */
    private boolean isShown = false;
    
    /** The title of the dialog */
    private String dialogTitle = "";
    /** The message of the dialog */
    private String dialogMessageData = "";
    
    /**
     * Constructor
     */
    public BsDialogRenameWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        
        // use enter to quickly apply the rename 
        textbox.addKeyUpHandler(new KeyUpHandler() {
            
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    okButton.click();
                }
            }
        });
    }
        
    /**
     * Sets the data for the dialog (but does not explicitly show the dialog).
     * 
     * @param title - The title of the dialog (cannot be null)
     * @param message - The message of the dialog (cannot be null)
     * @param placeholderText - Placeholder text to display in the textbox. This can be null if not needed.
     * @param confirmLabel - The confirm button text. If null, the button text will be set to "Rename"
     * @param valueChangeHandler - Handler to execute when the confirm button is clicked.
     */
    public void setData(String title, String message, String placeholderText, String confirmLabel, final ValueChangeHandler<String> valueChangeHandler) {
        
        dialogTitle = title;
        dialogMessageData = message;
        dialogMessage.setHTML(message);
        invalidMessage.setVisible(false);
        
        // Set the title for the dialog.  Add a little spacing here, because the icon will be to the left of the text.
        ctrlTitle.setText(" " + title);
        
        // Clear any existing button handler that may exist.
        clearButtonHandlers();
        
        if(placeholderText != null) {
        	textbox.setText(placeholderText);	
        }        
        
        if(confirmLabel != null) {
        	okButton.setText(confirmLabel);
        }
        
        changeHandler = addValueChangeHandler(valueChangeHandler);
        acceptHandler = okButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            	if(textbox.getValue() != null && !textbox.getValue().trim().isEmpty()) {
	                logger.fine("Click event occurred: " + event);
	                isShown = false;
	                ValueChangeEvent.fire(BsDialogRenameWidget.this, textbox.getValue().trim());
	                clearButtonHandlers();
					hide();
            	}
            }
        });
        declineHandler = cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				 logger.info("Decline clicked: " + event);
	                clearButtonHandlers();
	                hide();
			}
        	
        });
        
    }
    
    /**
     * Sets the data for the dialog (but does not explicitly show the dialog). 
     * The dialog will not be hidden when the confirm button is clicked.
     * 
     * @param title - The title of the dialog (cannot be null)
     * @param message - The message of the dialog (cannot be null)
     * @param placeholderText - Placeholder text to display in the textbox. This can be null if not needed.
     * @param confirmLabel - The confirm button text. If null, the button text will be set to "Rename"
     * @param valueChangeHandler - Handler to execute when the confirm button is clicked.
     */
    public void setData(String title, String message, String confirmLabel, final ValueChangeHandler<String> valueChangeHandler) {
        
        dialogTitle = title;
        dialogMessageData = message;
        dialogMessage.setHTML(message);
        invalidMessage.setVisible(false);
        
        // Set the title for the dialog.  Add a little spacing here, because the icon will be to the left of the text.
        ctrlTitle.setText(" " + title);
        
        textbox.clear();
        
        // Clear any existing button handler that may exist.
        clearButtonHandlers();
        
        if(confirmLabel != null) {
        	okButton.setText(confirmLabel);
        }
        
        changeHandler = addValueChangeHandler(valueChangeHandler);
        acceptHandler = okButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            	if(textbox.getValue() != null && !textbox.getValue().trim().isEmpty()) {
	                isShown = false;
	                ValueChangeEvent.fire(BsDialogRenameWidget.this, textbox.getValue().trim());
            	}
            }
        });
        declineHandler = cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				 logger.info("Decline clicked: " + event);
	                clearButtonHandlers();
	                hide();
			}
        	
        });
    }
    
    /**
     * Changes the text of the current modal
     * 
     * @param title The new modal title
     * @param message The new message
     */
    public void changeText(String title, String message) {
		dialogTitle = title;
		dialogMessageData = message;
				
		textbox.clear();
		invalidMessage.setVisible(false);
		dialogModal.setTitle(title);
		dialogMessage.setHTML(message);
	}
    
    /**
     * Shows an error message above the rename textbox
     * @param message The error message
     */
    public void showInvalidMessage(String message) {
		invalidMessage.setHTML(message);
		invalidMessage.setVisible(true);
	}
    
    public String getText() {
    	return textbox.getValue();
    }
    
    
    /**
     * Removes any handlers associated with the accept button.
     * This is needed so that there is only "one click handler" registered
     * at any given time.  
     */
    private void clearButtonHandlers() {
        
        logger.fine("clearing button handler");
        // Remove any previous handlers.
        if (acceptHandler != null) {
            logger.fine("removing accept handler");
            acceptHandler.removeHandler();
        }
        if (declineHandler != null) {
            logger.fine("removing decline handler");
            declineHandler.removeHandler();
        }
        if(changeHandler != null){
        	logger.fine("removing change handler");
        	changeHandler.removeHandler();
        }
        
    }
    
    
    /**
     * Accessor to show the modal dialog.
     */
    public void show() {
        dialogModal.show();
        isShown = true;
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

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
		return addHandler(valueChangeHandler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		return textbox.getValue();
	}

	@Override
	public void setValue(String value) {
		textbox.setValue(value);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		textbox.setValue(value);
		if(fireEvents) {
			ValueChangeEvent.fire(this, value);
		}
	}
    
    
    

    
}
