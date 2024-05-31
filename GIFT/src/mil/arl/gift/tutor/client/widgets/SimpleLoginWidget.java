/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.CreateUserDialogBox;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.TutorUserWebInterface;
import mil.arl.gift.tutor.shared.FieldVerifier;

/**
 * Widget to login the user
 *
 * @author jleonard
 */
public class SimpleLoginWidget extends Composite {
    
    /** The logger for this class */
    private static Logger logger = Logger.getLogger(SimpleLoginWidget.class.getName());

    private static LoginWidgetUiBinder uiBinder = GWT.create(LoginWidgetUiBinder.class);

    @UiField
    TextBox userIdTextBox;

    @UiField
    Button signInButton;

    @UiField
    Button createNewUserButton;

    @UiField
    FlowPanel errorPanel;

    @UiField
    InlineLabel errorLabel;

    @UiField
    Image loadingImage;

    @UiField
    FlowPanel loginPanel;

    @UiField
    FlowPanel loginContainerPanel;
    
    @UiField
    FlowPanel landingPageMessageContainerPanel;
    
    @UiField
    InlineLabel landingPageMessageLabel; 
    
    @UiField
    Image logoImage;

    /**
     * For handling when the login button is pressed
     */
    private final ClickHandler signInButtonClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            sendUserIdToServer();
        }
    };

    /**
     * For handling when the keyboard is used when the user ID text field has
     * focus
     */
    private final KeyUpHandler userIdTextBoxKeyUpHandler = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                sendUserIdToServer();
            }
        }
    };

    /**
     * For handling when the create user button is pressed
     */
    private final ClickHandler createUserButtonClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            DialogBox createUserDialog = new CreateUserDialogBox();
            // This is disabled until GWT is upgraded to version 2.5
            //createUserDialog.setAnimationEnabled(true);
            createUserDialog.center();
        }
    };

    interface LoginWidgetUiBinder extends UiBinder<Widget, SimpleLoginWidget> {
    }

    /**
     * Constructor
     */
    public SimpleLoginWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        History.newItem(TutorUserWebInterface.SIMPLE_LOGIN_PAGE_TAG, false);

        signInButton.addClickHandler(signInButtonClickHandler);

        userIdTextBox.addKeyUpHandler(userIdTextBoxKeyUpHandler);

        createNewUserButton.addClickHandler(createUserButtonClickHandler);

        userIdTextBox.setFocus(true);        
        
        Document.stopLoadingProgress();
        
        //
        // Show any landing page message
        //
        BrowserSession.getLandingPageMessage(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable arg0) {
                // nothing to do 
                landingPageMessageContainerPanel.setVisible(false);
            }

            @Override
            public void onSuccess(String message) {

                if(message != null && !message.isEmpty()){
                    //show the message
                    
                    landingPageMessageContainerPanel.setVisible(true);
                    landingPageMessageLabel.setText(message);
                }else{
                    landingPageMessageContainerPanel.setVisible(false);
                }
            }
        });
        
        // Show the configurable logo
        BrowserSession.getLogoUrl(new AsyncCallback<String>() {
            
            @Override
            public void onSuccess(String logoUrl) {
                logoImage.setUrl(logoUrl);
                logoImage.setVisible(true); 
            }
            
            @Override
            public void onFailure(Throwable t) {
                logger.log(Level.SEVERE, "Failed to retrieve the logo Url", t);
                logoImage.setVisible(false);                
            }
        });
        
        //show non-tested browser dialog
        if(JsniUtility.isIEBrowser()){
            Document.getInstance().displayDialog("Unsupported browser type", 
                        "GIFT has not been thoroughly tested using this browser version.</br></br>Please use a modern version of Chrome, Firefox or Edge for the best experience.");
        }
    }

    /**
     * Send the name from the nameField to the server and wait for a response.
     */
    private void sendUserIdToServer() {
        hideError();
        loadingImage.setVisible(true);
        // First, we validate the input.
        String textToServer = userIdTextBox.getText();
        if (!FieldVerifier.isValidUserId(textToServer, true)) {
            displayError("Must be a numeric value");
            loadingImage.setVisible(false);
            return;
        }

        final int userId = Integer.valueOf(textToServer);

        // Then, we send the input to the server.
        signInButton.setEnabled(false);

        BrowserSession.loginUser(userId, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                displayError("There was a RPC failure. Try again.");
                signInButton.setEnabled(true);
                loadingImage.setVisible(false);
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if (!result.isSuccess()) {
                    displayError("Could not login: " + result.getResponse());
                }
                signInButton.setEnabled(true);
                loadingImage.setVisible(false);
            }
        });
    }

    /**
     * Display an error for the user to see
     *
     * @param error
     */
    private void displayError(String error) {
        hideError();
        errorLabel.setText(error);
        errorPanel.setVisible(true);
        int errorPanelHeight = errorPanel.getOffsetHeight();
        int newHeight = errorPanelHeight + 220;
        loginContainerPanel.setHeight(newHeight + "px");
        loginPanel.setHeight(newHeight + "px");
    }

    /**
     * Hides an error from the user's view
     */
    private void hideError() {
        if (errorPanel.isVisible()) {
            errorPanel.setVisible(false);
            loginPanel.setHeight(220 + "px");
            loginContainerPanel.setHeight(220 + "px");
        }
    }
    
}
