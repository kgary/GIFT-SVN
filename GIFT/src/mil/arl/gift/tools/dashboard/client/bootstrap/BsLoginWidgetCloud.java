/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.SuggestBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.SessionStorage;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.websocket.ResumeSessionHandler;
import mil.arl.gift.tools.dashboard.shared.FieldVerifier;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.GetUsernamesResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.LoginResponse;


/**
 * The BsLoginWidgetCloud is the 'cloud' version of the login widget for GIFT.  This login page
 * should only be displayed if GIFT is running in the 'cloud'.  It is similar to the normal login page with
 * the following main differences:
 *   1)  Has different branding / styling to communicate that the cloud version is in development.
 *   2)  Forces users to accept the terms & conditions (EULA) for GIFT before logging in.
 *   3)  Doesn't support offline mode at all (since this is cloud version, online is expected).
 *   
 *
 * @author nblomberg
 */
public class BsLoginWidgetCloud extends AbstractBsWidget {

    private static BootstrapLoginWidgetUiBinder uiBinder = GWT.create(BootstrapLoginWidgetUiBinder.class);

    
    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
   
    //Note: 'provided' is needed to make the SuggestBox actually show suggestions
    //      due to using the UI builder to build this widget
    @UiField (provided = true)
    SuggestBox userNameTextBox;
    
    @UiField
    ListBox userNameListBox;

    @UiField
    Input userPasswordTextBox;
    
    @UiField 
    BsLoadingIcon ctrlLoadIcon;
    
    @UiField
    Button signInButton;
    
    @UiField
    Anchor forgotPasswordLink;
    
    @UiField
    Anchor createAccountLink;
    
    @UiField
    Anchor backToHomeLink;
    
    @UiField
    Alert errorAlert;
    
    @UiField
    Paragraph errorText;
    
    @UiField
    Alert serverInfoAlert;
    
    @UiField
    Alert ieCompatibilityAlert;
    
    @UiField
    Paragraph serverInfoText;
    
    @UiField
    Paragraph offlineLabel;
    
    @UiField
    Paragraph createAccountLabel;
    
    @UiField
    InlineCheckBox checkboxEula;
    
    @UiField
    Anchor forumLink;
    
    @UiField
    Anchor releaseNotesLink;
    
    @UiField
    Anchor eulaLink;    
    
    /** contains the organization image which is displayed in the top right of the page (e.g. customer logo) */
    @UiField
    Paragraph organizationImageParagraph;
    
    /** the organization image displayed in the top right of the page (e.g. customer logo) */
    @UiField
    Image organizationImage;
    
    @UiField
    Anchor dismissNote;
    
    @UiField
    TextBox loginAsUserNameTextBox;
    
    /** used to set the background color/image of the entire login page */
    @UiField
    Container backgroundContainer;
	
    private HandlerRegistration onlineLogin;
    
    private String COOKIE_COMPATIBILITYNOTE = "Dashboard_CompatibilityNote";
    private String COOKIE_COMPATIBILITYNOTE_VALUE = "true";
    
    /** 
     * the value of the debug mode flag found in the URL of the login page (i.e. '?debug=1')
     * This is a request to login as another user which may not succeed.  If the debug flag
     * isn't in the URL or isn't in the appropriate location of the URL the value will be false.
     */
    private boolean debugModeRequested = false;
    
    /**
     * used for Username suggestion cookie logic 
     */
    private final MultiWordSuggestOracle usernameOracle = new MultiWordSuggestOracle();
    private static long forgetMeIn = 1000 * 60 * 60 * 24 * 365 * 30; //30 years
    private static final String USERNAME_COOKIE_KEY = "gift-usernames";
    
    private static Logger logger = Logger.getLogger(BsLoginWidgetCloud.class.getName());
    
    interface BootstrapLoginWidgetUiBinder extends UiBinder<Widget, BsLoginWidgetCloud> {
    }
    
    /**
     * Constructor
     */
    public BsLoginWidgetCloud() {
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("BsLoginWidget() constructor called");
        }
        
        // When the login page loads, clear out any existing session token.
        SessionStorage.removeItem(SessionStorage.DASHBOARD_SESSIONID_TOKEN);
    	
        //update the username suggestions with usernames from cookies
        //Note: this has to be done before initWidget is called
        updateUsernameSuggestions();
        userNameTextBox = new SuggestBox(usernameOracle);
    	
        initWidget(uiBinder.createAndBindUi(this));
        
        // Close any existing websocket.
        // This could be open if the user has signed out and is returning to the login screen (for example).
        if (BrowserSession.getInstance() != null) {
            BrowserSession.getInstance().closeWebSocket();
        }
        
        hideServerInfoAlert();
        
        initServerProperties();

        // Default controls to 'off'/'hidden' as needed.
        stopLoadIcon();
        resetInputFields();
        errorAlert.setVisible(false);
        offlineLabel.setVisible(false);
        
        // Register the Enter key on the input fields so that the user will attempt login if enter is pressed.
        userNameTextBox.addKeyUpHandler(attemptLoginUsernameKeyHandler);
        userPasswordTextBox.addKeyUpHandler(attemptLoginPasswordKeyHandler);
        
        forgotPasswordLink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://gifttutoring.org/account/lost_password", "_blank", "");
            }
        });
        
        releaseNotesLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                ServerProperties props = Dashboard.getInstance().getServerProperties();
                if(props != null){
                    String documentationName = props.getDocumentationToken();
                    if(documentationName != null){                    
                        Window.open("https://gifttutoring.org/projects/gift/wiki/Release_Notes_"+documentationName, "_blank", "");
                    }else{
                        //show static documents page as a backup
                        Window.open("https://gifttutoring.org/projects/gift/wiki/Documentation", "_blank", "");
                    }
                }else{
                    //show static documents page as a backup
                    Window.open("https://gifttutoring.org/projects/gift/wiki/Documentation", "_blank", "");
                }
            }
        });
        
        eulaLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://gifttutoring.org/projects/gift/wiki/Gift_eula", "_blank", "");
            }
        });
        
        forumLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://gifttutoring.org/projects/gift/boards/6", "_blank", "");
            }
        });
        
        createAccountLink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://gifttutoring.org/account/register", "_blank", "");
            }
        });
        
        backToHomeLink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://gifttutoring.org/", "_blank", "");
            }
        });
        
        // this handler registration is needed to remove the click handler
        // in case the user logs in in offline mode
        onlineLogin = signInButton.addClickHandler(signInButtonClickHandler);
        
        resetInputFields(); 
        
        
        // The compatibility alert text should only show in IE browsers.  We're getting
        // the agent here to detect if we're in an IE setting.  In the future we would
        // like to autodetect if the font can be displayed rather than have the user see
        // the compatibility alert in IE browsers.
        // This compatibility alert informs the users about the issue with font-awesome/glyph icons
        // needing the "Font Download" setting to be "enabled" in IE browsers.   
        ieCompatibilityAlert.setVisible(false);
        String userAgent = Window.Navigator.getUserAgent();
        logger.info("Window agent: " + userAgent);
        
        String compatibilityNote = Cookies.getCookie(COOKIE_COMPATIBILITYNOTE);
        // Only show the alert if we're in IE browser and if the cookie to dismiss the note is note set.
        if (userAgent.contains("Trident") && (compatibilityNote == null || compatibilityNote.isEmpty())) {
            ieCompatibilityAlert.setVisible(true);
        }
        
        
       
        dismissNote.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent e) {
                
                // Add a cookie to track if the user sees the compatibility note.  If the cookie is not set,
                // the user will see the note (if they're using IE browser), otherwise if the cookie is set,
                // we don't display the compatibility note.
                // Set expiration to be a year out.  
                final long DURATION = 1000 * 60 * 60 * 24 * 365;
                Date expires = new Date(System.currentTimeMillis() + DURATION);
                Cookies.setCookie(COOKIE_COMPATIBILITYNOTE, COOKIE_COMPATIBILITYNOTE_VALUE, expires, null, "/", false);
                
                ieCompatibilityAlert.setVisible(false);
                
            }
            
        });
        
        // This is to fix ticket: https://gifttutoring.org/issues/1818
        // In some cases, the text boxes can lose focus (possibly IE only issue) when reloading the page, we want to 
        // force the focus to get reset (without actually leaving the focus on the text box) by
        // using scheduled deferred here and turn the focus on/off on one of the text boxes.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute () {
                // Reset the focus by turning it on/off.  We don't want to leave the focus on our text box for now to allow
                // the user to see the 'username' text in the box when the page initially loads.
                userNameTextBox.setFocus(true);
                userNameTextBox.setFocus(false);                
                
                //show non-tested browser dialog
                if(JsniUtility.isIEBrowser()){
                    UiManager.getInstance().displayErrorDialog("Unsupported browser type", 
                            "GIFT has not been thoroughly tested using this browser version.</br></br>Please use a modern version of Chrome, Firefox or Edge for the best experience.", null);
                }
            }
        });
        
        // determine if page is in debug mode
        final String debugFlag = Window.Location.getParameter(DocumentUtil.DEBUG_PARAMETER);
        if(debugFlag != null && debugFlag.equals(DocumentUtil.DEBUG_ENABLED_VALUE)){
            logger.info("The dashboard client is requesting to enter debug mode.");
            debugModeRequested = true;
        }
         
        loginAsUserNameTextBox.setVisible(debugModeRequested);        
    }
    
    /**
     * Return whether the specified username is already in the GIFT username cookie
     * 
     * @param newUsername the username to check
     * @return boolean if the username was found
     */
    private boolean cookieContainsUsername(String newUsername){
        
        boolean contains = false;
        String users = Cookies.getCookie(USERNAME_COOKIE_KEY);
        if (users != null){
            
            for (String username : users.split(":")){
                
                if (username.equals(newUsername)){
                    contains= true;
                }
            }
        }
        
        return contains;
    }
    
    /**
     * Add a username to the GIFT username cookies entry
     * 
     * @param username the username to add
     */
    private void addUsernameToCookie(String username){
        
        String users = Cookies.getCookie(USERNAME_COOKIE_KEY);
        if (users != null){
            Cookies.setCookie(USERNAME_COOKIE_KEY, users+":"+username, new Date(new Date().getTime() + forgetMeIn));
        }else{
            Cookies.setCookie(USERNAME_COOKIE_KEY, username, new Date(new Date().getTime() + forgetMeIn));
        }
    }
    
    /**
     * Return the GIFT TUI usernames found in the cookies
     * 
     * @return List<String> list of usernames successfully used and saved in the cookies
     */
    private List<String> getUsernames(){
        
        List<String> usernames = new ArrayList<String>();
        String users = Cookies.getCookie(USERNAME_COOKIE_KEY);
        if (users != null){
            
            for (String username : users.split(":")){
                usernames.add(username);
            }
        }
        return usernames;
    }
    
    /**
     * Update the suggestions for the username field based on the username
     * values found in the cookies.
     */
    private void updateUsernameSuggestions(){
        
        List<String> usernames = getUsernames();
        usernameOracle.setDefaultSuggestionsFromText(usernames);
        for (String username : usernames){
            usernameOracle.add(username);
        }

    }
    
    /**
     * Initializes the server properties that are used by this widget.
     */
    private void initServerProperties() {
        ServerProperties props = Dashboard.getInstance().getServerProperties();
        
        
        if (props != null) {
            String landingMessage = props.getPropertyValue(ServerProperties.LANDINGPAGE_MESSAGE);
            if (!landingMessage.isEmpty()) {
                logger.fine("Displaying landing page message: " + landingMessage);
                serverInfoText.setHTML(landingMessage);
                serverInfoAlert.setVisible(true);
            } else {
                logger.fine("Not displaying the landing page message.  Landing page message from server is empty.");
            }
            
            String backgroundImage = props.getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
            backgroundContainer.getElement().getStyle().setBackgroundImage("url('"+backgroundImage+"')");
            RootPanel.get().getElement().getStyle().setBackgroundImage("url('"+backgroundImage+"')");

            String orgImage = props.getPropertyValue(ServerProperties.ORGANIZATION_IMAGE);
            if(StringUtils.isNotBlank(orgImage)){
                organizationImage.setUrl(orgImage);
            }else{
                organizationImageParagraph.setVisible(false);
            }
            
        } else {
            logger.fine("Not displaying the landing page message. Properties are null.");
        }
    }
    
    /**
     * Configures the ui to allow offline login
     */
    private void useOfflineMode() {
    	
    	logger.fine("Using offline mode");
    	
    	// prepare the ui
    	
    	signInButton.setEnabled(true);
        stopLoadIcon();
    	
    	backToHomeLink.setVisible(false);
    	userNameTextBox.setVisible(false);
    	createAccountLink.setVisible(false);
    	forgotPasswordLink.setVisible(false);
    	createAccountLabel.setVisible(false);
    	userPasswordTextBox.setVisible(false);
    	
    	offlineLabel.setVisible(true);
    	userNameListBox.setVisible(true);
    	UiManager.getInstance().setOfflineMode(true);
    	    	
    	displayError("To use GIFT in Offline Mode, please select a registered username.");
    	
    	// Login differently and populate list of usernames

    	onlineLogin.removeHandler();
    	
    	signInButton.addClickHandler(new ClickHandler() {

    		@Override
    		public void onClick(ClickEvent clickevent) {
    			attemptOfflineLogin();
    		}
    	});

    	dashboardService.getUsernames(new AsyncCallback<GetUsernamesResponse>() {

    		@Override
    		public void onFailure(Throwable cause) {
    			logger.severe("Failed to retrieve usernames: " + cause);
    		}

    		@Override
    		public void onSuccess(GetUsernamesResponse result) {
    			if(result != null) {
    				for(String username : result.getUsernamesList()) {
    					userNameListBox.addItem(username);
    				}
    			}
    		}
    	});	
    }
    
    /**
     * Displays an error message in the error panel at the top of the login widget.
     * 
     * @param errorMsg - Error message to be displayed (should not be null or empty string).
     */
    private void displayError(String errorMsg) {
        errorAlert.setVisible(true); 
        errorText.setHTML(errorMsg);
    }
    
    /**
     * Resets the input fields to the defaults.
     */
    private void resetInputFields() {
        
        userNameTextBox.setText("");
        userPasswordTextBox.setText("");
        loginAsUserNameTextBox.setText("");
    }
    
    /**
     * Send the name from the nameField to the server and wait for a response.
     */
    private void attemptLogin() {
        
        startLoadIcon();

        // First, we validate the inputs.
        String userName = userNameTextBox.getText();
        String password = userPasswordTextBox.getText();
        String loginAsUserName = loginAsUserNameTextBox.getText();

        // check userName. If valid, convert to lowercase and trim the empty
        // spaces; return if invalid
        if (!FieldVerifier.isValidName(userName)) {
            if (userName == null || userName.trim().isEmpty()) {
                displayError("Please provide a username");
            } else {
                displayError("Incorrect username or password");
            }

            stopLoadIcon();
            return;
        } else {
            userName = userName.toLowerCase().trim();
        }

        // check password. If valid, trim the empty spaces; return if invalid
        if (password == null || password.trim().isEmpty()) {
            displayError("Please provide a password");
            stopLoadIcon();
            return;
        } else {
            password = password.trim();
        }

        // check login as userName. If valid, convert to lowercase and trim the
        // empty spaces; return if invalid
        if (debugModeRequested) {
            if (!FieldVerifier.isValidName(loginAsUserName)) {
                if (loginAsUserName == null || loginAsUserName.trim().isEmpty()) {
                    displayError("Please provide a 'login as' username");
                } else {
                    displayError("Incorrect 'login as' username");
                }

                stopLoadIcon();
                return;
            } else {
                loginAsUserName = loginAsUserName.toLowerCase().trim();
            }
        }

        // Ensure the user accepts the EULA
        if (!checkboxEula.getValue()) {
            displayError("Please accept the terms of the GIFT EULA in order to login.");
            stopLoadIcon();
            return;
        }

        // Then, we send the input to the server.
        signInButton.setEnabled(false);

        // in case the document was edited and the user attempted to insert a
        // login as username when they shouldn't be doing so
        if (!debugModeRequested) {
            loginAsUserName = null;
        } else {
            // a warning is useful so it will be obvious in the log
            logger.warning("Attempting to login " + userName + " as " + loginAsUserName);
        }

        dashboardService.loginUser(userName, password, loginAsUserName, new AsyncCallback<LoginResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                displayError("There was a problem communicating with the server. Please check your internet connection and try again.");
            }

            @Override
            public void onSuccess(LoginResponse result) {
            	
                if (!result.isSuccess()) {
                    displayError("Could not login: " + result.getResponse());
                    
                    
                    /**
                     * Do not show the offline dialog for the cloud version of the login screen.
                    if(!result.isOnline()) {
                    	showOfflineDialog();
                    }
                    */
                    
                } else {
                    //add username to cookies for suggestion upon next login
                    String username = userNameTextBox.getValue();
                    if (username !=null && !username.equals("") && !cookieContainsUsername(username.toLowerCase())){
                        addUsernameToCookie(username.toLowerCase());
                        updateUsernameSuggestions();
                    }
                    
                    initSession(result);
                }
                
                reset();
            }
        });
        
    }
    
    /**
     * Send the name from the nameField to the server and wait for a response.
     */
    private void attemptOfflineLogin() {
        
        startLoadIcon();
                
        // First, we validate the inputs.
        final String userName = userNameListBox.getItemText(userNameListBox.getSelectedIndex());
        
        if (!FieldVerifier.isValidName(userName)) {
            
            if(userName == null || userName.isEmpty()){
            	// must be a registered user
                displayError("Please provide a username");
            } else {
            	displayError("Invalid username");
            }
            
            stopLoadIcon();
            return;
            
        }

        // Then, we send the input to the server.
        signInButton.setEnabled(false);
        
        dashboardService.getUserId(userName, new AsyncCallback<RpcResponse>() {

			@Override
			public void onFailure(Throwable throwable) {
				displayError("The username provided is not registered. To register,"
						+ " connect to the internet and visit <a href=\"https://gifttutoring.org\" target=\"_blank\">gifttutoring.org</a>");
				reset();
				return;
			}

			@Override
			public void onSuccess(RpcResponse result) {
				// Needed to log the user into the tui

				UiManager.getInstance().setUserId(Integer.parseInt(result.getResponse()));	

				dashboardService.loginUserOffline(userName, new AsyncCallback<LoginResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						displayError("There was a problem communicating with the Tutor server. Please try to login again.");
						reset();
					}

					@Override
					public void onSuccess(LoginResponse result) {
						if (!result.isSuccess()) {
							displayError("Could not login: " + result.getResponse());
						} else {
							initSession(result);
						}

						reset();
					}
				});
			}
        });       
    }
    
    /**
     * After a successful login, show the my courses dashboard webpage.
     */
    private void initSession(LoginResponse result) {
    	
        // Store the browser session id as a cookie.
    	String sessionId = result.getBrowserSessionId();
    	       
        SessionStorage.putItem(SessionStorage.DASHBOARD_SESSIONID_TOKEN, sessionId);

        UiManager.getInstance().setSessionId(sessionId);
        
        //$TODO$ nblomberg
        // This is TEMPORARY -- remove before production release.  For security reasons, we need to have a token
        // or Single Sign On id here rather than passing around the username/password on the client.
        UiManager.getInstance().setUserName(result.getUserName());
        UiManager.getInstance().setUserPassword(result.getUserPass());
        
        if(debugModeRequested){     
            //when using 'login as' we don't want to loose the real user's username because that will be
            //needed when the tutor authenticates (again) before running a course
            UiManager.getInstance().setActualUserName(userNameTextBox.getText());
            
            //debug mode entered successfully
            UiManager.getInstance().setDebugMode(true);
        }

        /* Before transitioning to the screen, we now want to establish a
         * websocket connection with the server as well by creating a browser
         * session on the client that contains a websocket. */
        ResumeSessionHandler resumeHandler = new ResumeSessionHandler(ScreenEnum.MYCOURSES);
        BrowserSession.createBrowserSession(sessionId, sessionId, resumeHandler);
    }
    
    /** 
     * Resets the UI fields
     */
    private void reset(){
        signInButton.setEnabled(true);
        resetInputFields();
        stopLoadIcon();
    }    
    
    /**
     * Hides the server message from the user.
     */
    private void hideServerInfoAlert() {
        serverInfoAlert.setVisible(false);
        serverInfoText.setText("");
    }
    
    /**
     * For handling when the login button is pressed
     */
    private final ClickHandler signInButtonClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            attemptLogin();
        }
    };
    
    /**
     * For handling when the enter key is pressed on the username input box.
     * 
     */
    private final KeyUpHandler attemptLoginUsernameKeyHandler = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            	
                attemptLogin();
            }
            
        }

        
    };
    
    
    /**
     * For handling when the enter key is pressed on the password input box.
     */
    private final KeyUpHandler attemptLoginPasswordKeyHandler = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                
                attemptLogin();
            }
        }
    };
            
    /**
     * Starts the loading icon spinning and makes it visible.
     */
    private void startLoadIcon() {
        errorAlert.setVisible(false);
    	ctrlLoadIcon.startLoading();
    }

    /**
     * Stops the loading icon from spinning and hides it.
     */
    private void stopLoadIcon() {
        ctrlLoadIcon.stopLoading();
    }
    
    /**
     * Displays the dialog that appears when the user attempts
     * to login without an internet connection
     */
    @SuppressWarnings("unused")
    private void showOfflineDialog() {
    	
    	String msg = "<h4>It appears you are not connected to the internet. Would you like to use GIFT in Offline Mode?</h4>";
    	
    	ConfirmationDialogCallback callback = new ConfirmationDialogCallback() {

            @Override
            public void onDecline() {
            	//errorAlert.setVisible(false);
            	signInButton.setEnabled(true);
            }

            @Override
            public void onAccept() {
            	errorAlert.setVisible(false);
            	useOfflineMode();
            }
    	};
    	
		UiManager.getInstance().displayConfirmDialog("Unable to Connect", msg, "Go Offline", "Try Again", callback );
    }
    
    @Override
    protected void onDetach() {
        logger.info("onDetach() called");
        try {
            super.onDetach();
        } catch (Exception e) {
            logger.severe("BsLoginWidgetCloud::onDetach() caught an exception while trying to detach: " + e.getMessage());
        }
    }
}
