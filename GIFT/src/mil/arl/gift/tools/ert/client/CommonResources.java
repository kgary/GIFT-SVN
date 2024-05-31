/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.FileSelectionCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionWidgetUtil;

/**
 * A helper class that provides methods for doing common functions in the SAS
 *
 * @author jleonard
 */
public class CommonResources {
    
    private static Logger logger = Logger.getLogger(CommonResources.class.getName());

    /**
     * Displays a dialog with some text and a close button
     *
     * @param title The title of the dialog box
     * @param message The message to display
     * @param isError If the dialog is an error dialog, if true the message will
     * be highlighted in red
     */
    public static void displayDialog(String title, String message, boolean isError) {
        final DialogBox dialogBox = new DialogBox();
        
        dialogBox.addAttachHandler(new AttachEvent.Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if(event.isAttached()) {
                    
                    if(dialogBox.getOffsetWidth() > 500) {
                        
                        dialogBox.setWidth("500px");
                    }
                }
            }
        });
        
        dialogBox.setText(title);
        VerticalPanel contents = new VerticalPanel();
        contents.setSpacing(5);
        HTML messageHtml = new HTML(message);
        if (isError) {
            messageHtml.addStyleName("serverResponseLabelError");
        }
        contents.add(messageHtml);
        Button closeButton = new Button("Close");
        closeButton.setWidth("100px");
        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                dialogBox.hide();
            }
        });
        contents.add(closeButton);
        contents.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_CENTER);
        dialogBox.setWidget(contents);
        dialogBox.center();
        dialogBox.show();
    }

    /**
     * Display a dialog with some text
     *
     * @param title The title of the dialog box
     * @param message The message to display
     */
    public static void displayDialog(String title, String message) {
        displayDialog(title, message, false);
    }

    /**
     * Display an error in a dialog and logs the error with GWT
     *
     * @param type The type of error
     * @param action The action that was occurring during the error
     * @param why Why the error occurred
     */
    public static void displayErrorDialog(String type, String action, String why) {
        String message = "There was an '" + type + "' while '" + action + "'";
        if (why != null) {
            message = message.concat(": " + why);
        }
        logger.severe(message);
        displayDialog(type, message, true);
    }

    /**
     * Displays a error dialog with an error caught by a throwable
     * 
     * @param type The type of error
     * @param action The action that was occurring during the error 
     * @param caught The throwable thrown indicating the error
     */
    public static void displayErrorDialog(String type, String action, Throwable caught) {
        
        String message = "There was an '" + type + "' while '" + action + "'";
        
        if (caught != null) {
            
            message = message.concat(": " + caught.getClass() + ", " + caught.getMessage());
        }
        
        logger.log(Level.SEVERE, message, caught);
        displayDialog(type, message, true);
    }

    /**
     * Display an error in a dialog and logs the error with GWT
     *
     * @param type The type of error
     * @param action The action that was occurring during the error
     */
    public static void displayErrorDialog(String type, String action) {
        displayErrorDialog(type, action, (String) null);
    }

    /**
     * Display an RPC error in a dialog and logs the error with GWT
     *
     * @param action The action that was occurring during the error
     * @param caught The exception that was thrown from the failure
     */
    public static void displayRPCErrorDialog(String action, Throwable caught) {
        String message = "There was an RPC error while '" + action + "'";
        logger.log(Level.SEVERE, message, caught);
        displayDialog("RPC Error", message, true);
    }

    /**
     * Displays a dialog with a widget in it
     *
     * @param title The title of the dialog
     * @param widget The widget to put in the dialog
     * @param showCloseButton If a button to close the dialog should be displayed
     * @return DialogBox The dialog box with a widget in it
     */
    public static DialogBox displayWidgetContainerDialog(String title, Widget widget, boolean showCloseButton) {
        
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(title);
        VerticalPanel container = new VerticalPanel();
        container.setWidth("100%");
        container.setSpacing(5);
        container.add(widget);
        if (showCloseButton) {
            
            Button closeButton = new Button("Close");
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    dialogBox.hide();
                }
            });
            container.add(closeButton);
            container.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
        }
        
        dialogBox.setWidget(container);
        dialogBox.center();
        dialogBox.show();
        return dialogBox;
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
     * Creates a dialog box for user to confirm or deny an action
     *
     * @param title The title of the dialog box
     * @param message The message of the dialog box
     * @param callback The callback when the dialog box is responded to
     * @return DialogBox A dialog box for the user to confirm or deny an action
     */
    public static DialogBox displayConfirmationDialog(String title, String message, final ConfirmationDialogCallback callback) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(title);
        VerticalPanel contents = new VerticalPanel();
        contents.setSpacing(5);
        contents.add(new HTML(message));
        Button yesButton = new Button("Yes");
        yesButton.setWidth("100px");
        yesButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (callback != null) {
                    callback.onAccept();
                }
                dialogBox.hide();
            }
        });
        Button noButton = new Button("No");
        noButton.setWidth("100px");
        noButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (callback != null) {
                    callback.onDecline();
                }
                dialogBox.hide();
            }
        });
        HorizontalPanel yesNoPanel = new HorizontalPanel();
        yesNoPanel.setWidth("100%");
        yesNoPanel.setSpacing(5);
        yesNoPanel.add(noButton);
        yesNoPanel.setCellHorizontalAlignment(noButton, HasHorizontalAlignment.ALIGN_RIGHT);
        yesNoPanel.add(yesButton);
        contents.add(yesNoPanel);
        dialogBox.add(contents);

        dialogBox.center();
        dialogBox.show();
        return dialogBox;
    }

    /**
     * An interface for getting the input to a text input dialog, if there is
     * any
     */
    public interface TextInputDialogCallback {

        /**
         * Called when the user has finishing input
         *
         * @param input The string the user has input
         */
        public void onInput(String input);
    }

    /**
     * Display a dialog for the user to input text
     *
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param defaultText The text already in the text field
     * @param callback Callback for when the user has finished input
     * @return DialogBox The dialog for the use to input text
     */
    public static DialogBox displayTextInputDialog(String title, String message, String defaultText, final TextInputDialogCallback callback) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(title);
        VerticalPanel contents = new VerticalPanel();
        contents.setSpacing(5);
        contents.add(new HTML(message));
        final TextBox textInputBox = new TextBox();
        textInputBox.setWidth("100%");
        if (defaultText != null) {
            textInputBox.setText(defaultText);
        }
        textInputBox.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    if (callback != null) {
                        callback.onInput(textInputBox.getText());
                    }
                    dialogBox.hide();
                }
            }
        });
        contents.add(textInputBox);
        Button yesButton = new Button("Submit");
        yesButton.setWidth("100px");
        yesButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (callback != null) {
                    callback.onInput(textInputBox.getText());
                }
                dialogBox.hide();
            }
        });
        Button noButton = new Button("Cancel");
        noButton.setWidth("100px");
        noButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        HorizontalPanel yesNoPanel = new HorizontalPanel();
        yesNoPanel.setWidth("100%");
        yesNoPanel.setSpacing(5);
        yesNoPanel.add(noButton);
        yesNoPanel.setCellHorizontalAlignment(noButton, HasHorizontalAlignment.ALIGN_RIGHT);
        yesNoPanel.add(yesButton);
        contents.add(yesNoPanel);
        dialogBox.add(contents);

        dialogBox.center();
        dialogBox.show();

        textInputBox.setFocus(true);

        return dialogBox;
    }

    /**
     * Display a dialog for the user to input text
     *
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param callback Callback for when the user has finished input
     * @return DialogBox The dialog for the use to input text
     */
    public static DialogBox displayTextInputDialog(String title, String message, final TextInputDialogCallback callback) {
        return displayTextInputDialog(title, message, null, callback);
    }

    /**
     * Displays a dialog for uploading a file to a server
     *
     * @param title The title of the dialog
     * @param message the message of the dialog
     * @param postUrl The URL to HTTP POST the file to
     * @param callback The callback for when the file is uploaded.  Can be null if the success or failure of the upload
     * isn't important to the caller of this method.
     * @return DialogBox A dialog for uploading a file to a server
     */
    public static DialogBox displayFileUploadDialog(String title, String message, String postUrl, final FileSelectionCallback callback) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(title);
        final FormPanel form = new FormPanel();
        form.setAction(postUrl);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        VerticalPanel contents = new VerticalPanel();
        contents.setSpacing(5);
        contents.add(new HTML(message));


        final FileUpload upload = new FileUpload();
        upload.setName("uploadFormElement");
        contents.add(upload);

        Button yesButton = new Button("Submit");
        yesButton.setWidth("100px");
        yesButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                form.submit();
            }
        });
        Button noButton = new Button("Cancel");
        noButton.setWidth("100px");
        noButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        HorizontalPanel yesNoPanel = new HorizontalPanel();
        yesNoPanel.setWidth("100%");
        yesNoPanel.setSpacing(5);
        yesNoPanel.add(noButton);
        yesNoPanel.setCellHorizontalAlignment(noButton, HasHorizontalAlignment.ALIGN_RIGHT);
        yesNoPanel.add(yesButton);
        contents.add(yesNoPanel);
        form.setWidget(contents);
        dialogBox.add(form);

        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                
                FileSelectionWidgetUtil.handleFileUploadResponse(event.getResults(), callback);
            }
        });

        dialogBox.center();
        dialogBox.show();
        return dialogBox;
    }

    /**
     * Displays a dialog to indicate an operation is in progress
     *
     * @param title The title of the dialog
     * @param message The message of what is going on
     * @return DialogBox A dialog to indicate an operation is in progress
     */
    public static DialogBox displayProgressDialog(String title, String message) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(title);
        VerticalPanel contents = new VerticalPanel();
        contents.setSpacing(5);
        contents.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        contents.add(new Image("images/loading.gif"));
        contents.add(new HTML(message));
        dialogBox.add(contents);

        dialogBox.center();
        dialogBox.show();
        return dialogBox;
    }
}
