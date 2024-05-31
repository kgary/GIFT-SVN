/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.LtiProperties;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.gwt.client.BrowserProperties;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.WebDeviceUtils;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.EndCourseMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.HistoryItemMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.websocket.ClientWebSocketHandler;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.shared.AbstractCourseLaunchParameters;
import mil.arl.gift.common.gwt.shared.LtiParameters;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.gwt.shared.websocket.MessageResponseHandler;
import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;
import mil.arl.gift.common.gwt.shared.websocket.responsedata.AbstractMessageResponseData;
import mil.arl.gift.common.gwt.shared.websocket.responsedata.RpcResponseData;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.websocket.TutorClientWebSocket;
import mil.arl.gift.tutor.client.widgets.AvatarContainer;
import mil.arl.gift.tutor.client.widgets.DisplayMediaWidget;
import mil.arl.gift.tutor.client.widgets.ErrorPageWidget;
import mil.arl.gift.tutor.client.widgets.ExperimentCompleteWidget;
import mil.arl.gift.tutor.client.widgets.ExperimentWelcomeWidget;
import mil.arl.gift.tutor.client.widgets.IsUpdateableWidget;
import mil.arl.gift.tutor.client.widgets.SelectDomainWidget;
import mil.arl.gift.tutor.client.widgets.SimpleLoginWidget;
import mil.arl.gift.tutor.client.widgets.TutorActionsWidget;
import mil.arl.gift.tutor.client.widgets.UserActionAvatarContainer;
import mil.arl.gift.tutor.shared.AbstractAction;
import mil.arl.gift.tutor.shared.AbstractWidgetAction;
import mil.arl.gift.tutor.shared.ActionTypeEnum;
import mil.arl.gift.tutor.shared.AvatarStatusAction;
import mil.arl.gift.tutor.shared.ClientProperties;
import mil.arl.gift.tutor.shared.DeactivateAction;
import mil.arl.gift.tutor.shared.DialogInstance;
import mil.arl.gift.tutor.shared.DisplayDialogAction;
import mil.arl.gift.tutor.shared.DisplayWidgetAction;
import mil.arl.gift.tutor.shared.InitTrainingAppAction;
import mil.arl.gift.tutor.shared.KnowledgeSessionsUpdated;
import mil.arl.gift.tutor.shared.LessonStatusAction;
import mil.arl.gift.tutor.shared.LessonStatusAction.LessonStatus;
import mil.arl.gift.tutor.shared.PreloadAvatarAction;
import mil.arl.gift.tutor.shared.SynchronizeClientStateAction;
import mil.arl.gift.tutor.shared.TrainingAppMessageAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetLocationEnum;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.data.CourseListResponse;
import mil.arl.gift.tutor.shared.properties.DisplayMediaWidgetProperties;
import mil.arl.gift.tutor.shared.properties.DisplayMessageWidgetProperties;
import mil.arl.gift.tutor.shared.properties.ExperimentWidgetProperties;
import mil.arl.gift.tutor.shared.properties.SelectDomainWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;
import mil.arl.gift.tutor.shared.websocket.TutorWebSocketUtils;
import mil.arl.gift.tutor.shared.websocket.messages.DoActionAsyncMessage;

/**
 * Represents a session of the browser. This is a singleton.
 *
 * @author jleonard
 */
public class BrowserSession implements ClientWebSocketHandler {

    /** The logger for this class */
    private static Logger logger = Logger.getLogger(BrowserSession.class.getName());

    /** The connection to the TWS RPC service */
    private static final TutorUserInterfaceServiceAsync tutorUserInterfaceService = GWT.create(TutorUserInterfaceService.class);

    /** The location of the HTML file to use as the GIFT tutor simple login unavailable page (e.g. when GIFT is in server deployment mode)  */
    private static final String UNAVAILABLE_PAGE_URL = "Unavailable.html";

    /** The HTML file to show for Experiment/LTI landing pages when this GIFT instance has restricted access (e.g. white list authentication is enabled) */
    private static final String RESTRICTED_ACCESS_PAGE_URL = "RestrictedAccess.html";

    /** The HTML file to show for Simple Login landing page when this GIFT instance has restricted access (e.g. white list authentication is enabled) */
    private static final String SIMPLE_LOGIN_RESTRICTED_ACCESS_PAGE_URL = "SimpleLoginRestrictedAccess.html";

    /** The key for the user session */
    private final String userSessionKey;

    /** The key for the browser session */
    private final String browserSessionKey;

    /** The unique GIFT user id */
    private int userId = -1;

    /** The widget to display */
    private WidgetInstance articleWidget = null;

    /** The footer widget to display */
    private WidgetInstance footerWidget = null;

    /** The type of login process */
    private static WidgetTypeEnum loginType = WidgetTypeEnum.SIMPLE_LOGIN_WIDGET;

    /** Flag to indicate if the domain session is listening or not */
    private boolean domainSessionListening = false;

    /**
     * properties needed by the client and provided by the server
     * Note: can be null if the properties were not retrieved
     */
    private static ServerProperties serverProperties;

    /** The instance of the browser session */
    private static BrowserSession instance;

    /** The cached domain id (this is the id of the course that is being run by the user) */
    private String cachedDomainId = "";

    /** Timer used the check the course start progress. */
    private Timer checkCourseProgress = null;

    /** Interval (in milliseconds) between each request to check the course progress. */
    private static final int CHECK_COURSE_INTERVAL_MS = 1000;

    /** The websocket used for this browser session. */
    static TutorClientWebSocket webSocket = null;

    /** Optional callback class that can be executed once the browser session is ready. */
    private BrowserSessionReadyCallback readyCallback = null;

    /** Optional listener to listen for knowledge session updates. */
    private KnowledgeSessionListener knowledgeSessionListener = null;

    /** The dialog used to tell the user when this client is reconnecting to the server after a disconnect */
    private static ModalDialogBox reconnectDialog = null;

    /**
     * The time that this browser session began attempting to reconnect to the server. Used to determine if
     * the client has taken too long to reconnect to the server and has likely lost its server session as a result.
     */
    private static Long reconnectStartTime = null;

    /** Whether this client is attempting to resynchronize its state with the server after a disconnect */
    private static boolean isResynchronizing = false;

    /**
     * Gets the instance of the browser session
     *
     * @return BrowserSession The current instance of the browser session.
     */
    public static BrowserSession getInstance() {
        logger.fine("instance = " + instance);
        return instance;
    }

    /**
     * Constructor
     *
     * @param userSessionKey The user session that belongs to the browser.
     * @param browserSessionKey The browser session ID
     * @param loginType The type of login
     * @param readyCallback (optional) Callback used to signal back when the browser session is ready (ie once the socket is opened).
     */
    private BrowserSession(String userSessionKey, String browserSessionKey, WidgetTypeEnum loginType, final BrowserSessionReadyCallback readyCallback) {
        logger.info("Starting browser session.");
        this.userSessionKey = userSessionKey;
        this.browserSessionKey = browserSessionKey;
        BrowserSession.loginType = loginType;
        BrowserProperties.getInstance().setUserSessionKey(userSessionKey);

        exposeNativeFunctions();

        this.readyCallback = readyCallback;

        logger.info("Finished starting browser session.");
    }

    /**
     * Creates the global browser session instance to be used throughout the client window and attaches it as a handler to
     * the web socket currently assigned to this client. This method implicitly calls {@link BrowserSession}'s constructor and should
     * generally be preferred over calling the constructor directly, since it will immediately assign the created browser session as
     * the global instance and set up the web socket to use it, allowing the web socket to handle messages as soon as possible.
     *
     * @param userSessionKey The user session that belongs to the browser.
     * @param browserSessionKey The browser session ID
     * @param loginType The type of login
     * @param readyCallback (optional) Callback used to signal back when the browser session is ready (ie once the socket is opened).
     */
    private static void createInstanceAndAttachSocket(String userSessionKey, String browserSessionKey, WidgetTypeEnum loginType, final BrowserSessionReadyCallback readyCallback) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("createInstanceAndAttachSocket(");
            List<Object> params = Arrays.<Object>asList(userSessionKey, browserSessionKey, loginType);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        instance = new BrowserSession(userSessionKey, browserSessionKey, loginType, readyCallback);

        try{

            if(webSocket != null){

                //assign this browser session to the appropriate web socket created for this client's server-side HTTP session
                webSocket.setBrowserSession(instance);

                if(webSocket.areActionsDesynched()) {

                    //if this browser session missed some actions from its web socket, synchronize its state with the server
                    instance.sendActionToServer(new SynchronizeClientStateAction(), null);
                    webSocket.setActionsDesynched(false);
                }

            } else {
                logger.severe("Failed to fully start the browser session instance because its web socket has not been initialized.");
                readyCallback.onSessionFailure();
            }

        }catch(Exception e){
            logger.severe("Failed to fully start the browser session instance because an error occurred while initializing the web socket:\n" + e);
            readyCallback.onSessionFailure();
        }
    }

    /**
     * Return the current login widget type for this session
     *
     * @return WidgetTypeEnum the enumerated type
     */
    public static WidgetTypeEnum getLoginType(){
        return loginType;
    }

    /**
     * Updates the browser session's login type with the type given.
     *
     * @param loginType the type of login process.
     */
    public static void setLoginType(WidgetTypeEnum loginType){
        BrowserSession.loginType = loginType;
    }

    /**
     * Gets the key of the user session for this browser session
     *
     * @return String The key of the user session for this browser session
     */
    public String getUserSessionKey() {
        return userSessionKey;
    }

    /**
     * Gets the key this browser session
     *
     * @return String The key of this browser session
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }

    /**
     * Registers a knowledge session listener to the browser session.  There is only one
     * listener allowed at a time.  A UI panel can register to listen for knowledge session
     * updates.
     *
     * @param listener The listener to receive knowledge session updates.
     */
    public void setKnowledgeSessionListener(KnowledgeSessionListener listener) {
        knowledgeSessionListener = listener;
    }

    /**
     * Checks to make sure the browser session is valid on the server
     *
     * @param callback Callback for if the browser session is valid on the
     * server
     */
    public void isValidSession(final AsyncCallback<Boolean> callback) {
        if (browserSessionKey != null) {
            tutorUserInterfaceService.isValidBrowserSessionKey(browserSessionKey, callback);
        } else {
            callback.onSuccess(Boolean.FALSE);
        }
    }

    /**
     * Logs out the user associated with this browser session
     *
     * @param callback Callback for when the server responds
     */
    public void logout(final AsyncCallback<RpcResponse> callback) {
        tutorUserInterfaceService.userLogout(browserSessionKey, new LogoutCallback(callback));
    }

    /**
     * Gets the name of the active domain session associate with the browser
     * session
     *
     * If there is no active domain session, the returned value will be null
     *
     * @param callback Callback with the active domain session name when the
     * server responds
     */
    public void getActiveDomainSessionName(final AsyncCallback<String> callback) {
        tutorUserInterfaceService.getActiveDomainSessionName(browserSessionKey, callback);
    }

    /**
     * Gets the domain options from the domain module connected to the server
     *
     * @param callback Callback for when the server responds with the domain
     * options
     */
    public void getDomainOptions(final AsyncCallback<CourseListResponse> callback) {
        tutorUserInterfaceService.getDomainOptions(browserSessionKey, callback);
    }

    /**
     * Gets the string value for the window title property
     * @return if the server properties have not been retrieved yet, it will always
     * return null. Otherwise it will return the value as it is stored within the properties
     */
    public String getWindowTitle() {
        return serverProperties != null ? serverProperties.getWindowTitle() : null;
    }
    
    /**
     * Gets the string value for the background image URL.
     * @return if the server properties have not been retrieved yet, it will always
     * return null.  Otherwise it will return the value as it is stored within the properties
     */
    public String getBackgroundUrl(){
        return serverProperties != null ? serverProperties.getPropertyValue(ServerProperties.BACKGROUND_IMAGE) : null;
    }
    
    /**
     * Gets the string value for the Logo URL.
     * @return if the server properties have not been retrieved yet, it will always
     * return null.  Otherwise it will return the values as it is stored within the properties.  Use
     * {@link #getLogoUrl(AsyncCallback)} for retrieving server properties with a callback.
     */
    public String getLogoUrl(){
        return serverProperties != null ? serverProperties.getPropertyValue(ServerProperties.LOGO) : null;
    }

    /**
     * Return whether the deployment mode of GIFT is in server mode.
     *
     * @return true if GIFT is configured for server deployment mode
     */
    public boolean isServerDeploymentMode(){
        return serverProperties != null && serverProperties.isServerMode();
    }


    /**
     * Return the url to the tutor web socket service.
     *
     * @return if the server properties have not been retrieved yet, it will always
     * return null. Otherwise it will return the value as it is stored within the properties
     */
    public static String getTutorWebSocketUrl() {
        return serverProperties != null ? serverProperties.getTutorWebSocketUrl() : null;
    }

    /**
     * Exposes a javascript function to be used by the GIFT Media Semantics Avatar html
     * to notify GIFT that the avatar is idle.
     */
    public native void exposeNativeFunctions()/*-{

		var that = this;

		$wnd.notifyGIFT = $entry(function() {
			that.@mil.arl.gift.tutor.client.BrowserSession::idleAvatarNotification()();
		});

		$wnd.addEventListener("message", function(e) {

			var avatarFrame = @mil.arl.gift.tutor.client.widgets.AvatarContainer::getAvatarFrameElement()();

			if (avatarFrame	&& event.source === avatarFrame.contentWindow) {

				//only notify GIFT that the avatar is idle upon receiving a message from the avatar
				$wnd.notifyGIFT();
			}

		}, false);
    }-*/;

    /**
     * Notify the server that the currently displayed avatar is idle and ready to receive
     * request to speak from the server.
     * Note: this method is called by the native javascript function {@link #exposeNativeFunctions()}
     * to indicate that the avatar is idle.
     */
    public void idleAvatarNotification() {
        AvatarStatusAction idleAction = new AvatarStatusAction(true);
        sendActionToServer(idleAction, null);
    }

    /**
     * Notify the server that the currently displayed avatar (or avatar that is being loaded) is busy
     * and NOT ready to receive request to speak from the server.  The server should queue these requests
     * until the avatar is idle and the client notifies the server that the avatar is idle.
     */
    public void busyAvatarNotification(){
        AvatarStatusAction idleAction = new AvatarStatusAction(false);
        sendActionToServer(idleAction, null);
    }

    /**
     * Determines whether or not the avatar is idle
     *
     * @param callback The callback to execute when the avatar status is retrieved
     */
    public void isAvatarIdle(AsyncCallback<Boolean> callback) {
        tutorUserInterfaceService.isAvatarIdle(instance.browserSessionKey, callback);
    }

    /**
     * Displays a widget as an article of the web page
     *
     * @param instance The widget to display
     */
    private void displayArticleWidget(WidgetInstance instance) {
        try {
            if (instance != null) {
                if (TutorUserWebInterface.isExperiment() && instance.getWidgetType() == WidgetTypeEnum.SELECT_DOMAIN_WIDGET) {
                    logger.severe("receiving a select domain widget request when in experiment mode!");
                    return;
                }
                Widget widgetInstance = null;

                boolean shouldResize = true;

                widgetInstance = Document.getInstance().getArticleWidget();

                //detect if this client is currently attempting to resynchronize with the server and act accordingly
                boolean resynchronizingNow = isResynchronizing;

                if(resynchronizingNow) {
                    isResynchronizing = false;
                }

                if (instance.getWidgetProperties().getShouldUpdate()
                        && widgetInstance instanceof IsUpdateableWidget
                        && ((IsUpdateableWidget) widgetInstance).getWidgetType() == instance.getWidgetType()) {

                    // if an updateable widget is being shown as the main article, update the existing article widget
                    ((IsUpdateableWidget) widgetInstance).update(instance);

                    //only resize the widget the first time it is shown, not with every update
                    shouldResize = false;

                } else if (instance.getWidgetProperties().getShouldUpdate()
                        && UserActionAvatarContainer.getInstance().getLoadingOverlayWidget() instanceof IsUpdateableWidget
                        && ((IsUpdateableWidget) UserActionAvatarContainer.getInstance().getLoadingOverlayWidget()).getWidgetType()
                            == instance.getWidgetType()) {

                    // if an updateable widget is being shown as the loading message for a training application,
                    // update the existing loading message widget
                    ((IsUpdateableWidget) UserActionAvatarContainer.getInstance().getLoadingOverlayWidget()).update(instance);

                    //need to return early here, since we aren't actually changing the article widget but are instead updating
                    //the loading widget inside of it
                    return;

                } else if(resynchronizingNow
                        && articleWidget != null
                        && articleWidget.getWidgetId() != null
                        && articleWidget.getWidgetId().equals(instance.getWidgetId())) {

                    // if the client is just now resychronizing with the server after a disconnect and is still showing the
                    // widget with the same ID that the server says it should be, then don't change the current widget
                    return;

                } else {

                    //hide the course header if it is showing in case the new widget doesn't use it
                    Document.getInstance().hideCourseHeader();

                    //we are not updating an existing widget, so create a new widget instance
                    widgetInstance = WidgetFactory.createWidgetInstance(instance);
                }

                if (widgetInstance != null) {
                    WidgetProperties properties = instance.getWidgetProperties();

                    if(LessonStatus.INITIALIZING.equals(UserActionAvatarContainer.getInstance().getLessonStatus())
                            && (WidgetTypeEnum.MEDIA_WIDGET.equals(instance.getWidgetType()) || WidgetTypeEnum.MESSAGE_WIDGET.equals(instance.getWidgetType()))
                            && !DisplayMessageWidgetProperties.getHasContinueButton(instance.getWidgetProperties())) {

                        //if a loading message is being shown while a training application is initializing, display it inside the avatar container
                        UserActionAvatarContainer.getInstance().setLoadingOverlay(instance, widgetInstance);

                        return;
                    }

                    if(shouldResize){
                        // Determine if the widget should be displayed in full screen mode or sidebar mode.1
                        if (properties != null && properties.getIsFullscreen()) {
                            Document.getInstance().fullScreenMode();
                        } else if(widgetInstance instanceof SelectDomainWidget || widgetInstance instanceof SimpleLoginWidget){

                            Document.getInstance().fullScreenMode();

                            //set the width of this widget to 50% of the total width of the parent element, the Article document
                            //When the article document is set to full screen mode, this means the widget is 50% of the screen.
                            widgetInstance.getElement().getStyle().setProperty("width", "50%");

                        } else {
                            // Default to sidebar mode for each widget instance.
                            Document.getInstance().sideBarMode();
                        }
                    }

                    if (instance.getWidgetType() == WidgetTypeEnum.SELECT_DOMAIN_WIDGET) {
                        if(TutorUserWebInterface.isEmbedded()){
                            // When the course ends, only the GIFT Logo will be displayed unless the user is not running in
                            // embedded mode. This prevents the Select Course list from being shown when the Tutor is
                            // embedded in the dashboard
                            return;
                        }

                        if(footerWidget != null && userId != -1) {
                            displayFooterText("Logged in as user " + userId);
                        }
                        domainSessionListening = false;

                    } else if(instance.getWidgetType() == WidgetTypeEnum.COURSE_INIT_INSTRUCTIONS_WIDGET){
                         domainSessionListening = true;
                         if(TutorUserWebInterface.isExperiment()) {
                             addHistoryTag(TutorUserWebInterface.buildUserSessionHistoryTag(userSessionKey));
                         } else {
                             addHistoryTag(TutorUserWebInterface.DOMAIN_SESSION_PAGE_TAG);
                         }

                         if (TutorUserWebInterface.isEmbedded()) {

                             // Create and embed a new message type.
                             IFrameSimpleMessage startSession = new IFrameSimpleMessage(IFrameMessageType.COURSE_READY);
                             IFrameMessageHandlerChild.getInstance().sendMessage(startSession);
                         }
                    } else if(instance.getWidgetType() == WidgetTypeEnum.SIMPLE_LOGIN_WIDGET){

                        if(TutorUserWebInterface.isEmbedded()){
                            // When the course ends, only the GIFT Logo will be displayed unless the user is not running in
                            // embedded mode. This prevents the Select Course list from being shown when the Tutor is
                            // embedded in the dashboard
                            return;
                        }
                    } else if (Document.getInstance().getArticleWidget() instanceof AvatarContainer
                            && instance.getWidgetType() == WidgetTypeEnum.AVATAR_CONTAINER_WIDGET) {
                        // If the avatar container is already showing, don't reset the article widget, otherwise the Avatar flashes.
                        articleWidget = instance;
                        return;
                    } else if (Document.getInstance().getArticleWidget() instanceof DisplayMediaWidget
                            && instance.getWidgetType() == WidgetTypeEnum.MEDIA_WIDGET
                            && DisplayMediaWidgetProperties.getLoadProgress(instance.getWidgetProperties()) != null) {
                        // If the guidance widget is already showing, don't reset the article widget, otherwise the Avatar flashes.
                        articleWidget = instance;
                        return;
                    }

                    Document.getInstance().showLoading(false);
                    
                    if(properties != null && properties.getBackgroundImagePath() != null){
                        Document.getInstance().setBackground(properties.getBackgroundImagePath());
                    }

                    if (properties != null && properties.getHasFocus()) {
                        Document.getInstance().setArticleWidget(widgetInstance, properties.getHasFocus());
                    } else {
                        Document.getInstance().setArticleWidget(widgetInstance);
                    }

                    articleWidget = instance;

                } else {
                    articleWidget = null;
                }

            } else {
                Document.getInstance().setArticleWidget(WidgetFactory.createWidgetType(WidgetTypeEnum.LOGO_WIDGET));
            }
        } catch (Exception e) {
            Document.getInstance().displayError("Displaying an article widget", "The client caught an exception", e);
        }
    }

    /**
     * Helper function to add history tags.
     *
     * @param tag - the new history tag to be added.  (should not be null).
     */
    private void addHistoryTag(String tag) {

        /* Add history for the tui if it is an experiment or not embedded */
        final boolean isExperiment = TutorUserWebInterface.isExperiment();
        if (isExperiment || !TutorUserWebInterface.isEmbedded()) {
            History.newItem(tag, false);

            /* Need to add the history to dashboard also */
            if (isExperiment) {
                HistoryItemMessage historyMsg = new HistoryItemMessage(tag);
                IFrameMessageHandlerChild.getInstance().sendMessage(historyMsg);
    }
        }
    }

    /**
     * Displays a widget in the footer of the web page
     *
     * @param instance The widget to display
     */
    private void displayFooterWidget(WidgetInstance instance) {
        try {
            if (instance != null) {
                Widget widgetInstance = WidgetFactory.createWidgetInstance(instance);
                if (widgetInstance != null) {
                    Document.getInstance().setFooterWidget(widgetInstance);
                    footerWidget = instance;
                }
            } else {
                Document.getInstance().setFooterWidget(null);
            }
        } catch (Exception e) {
            Document.getInstance().displayError("Displaying a footer widget", "The client caught an exception", e);
        }
    }

    /**
     * Displays some text in the footer of the web page
     *
     * @param message The text to display
     */
    private void displayFooterText(String message) {
        WidgetInstance widgetInstance = new WidgetInstance(null, WidgetTypeEnum.FOOTER_TEXT_WIDGET);
        WidgetProperties properties = widgetInstance.getWidgetProperties();
        DisplayMessageWidgetProperties.setParameters(properties, DisplayMessageTutorRequest.createTextRequest(null, message));
        DisplayMessageWidgetProperties.setHasContinueButton(properties, false);
        displayFooterWidget(widgetInstance);
    }

    /**
     * Displays a dialog box in the browser
     *
     * @param instance The dialog box to display
     */
    private void displayDialog(DialogInstance instance) {
        if (instance.getType().isError()) {
            Document.getInstance().displayErrorDialog(instance.getTitle(), instance.getMessage(), null);
        } else {
            Document.getInstance().displayDialog(instance.getTitle(), instance.getMessage());
        }
    }

    /**
     * Return the landing page message to use in the tutor client.
     *
     * @param callback used to notify the method caller of when the landing page message has been retrieved from the server.
     * Note: the message can be null or empty string
     */
    public static void getLandingPageMessage(final AsyncCallback<String> callback){

        if(serverProperties == null){
            // retrieve the properties from the server
            tutorUserInterfaceService.getServerProperties(new AsyncCallback<ServerProperties>() {
    
                @Override
                public void onFailure(Throwable arg0) {
                    callback.onFailure(arg0);
                }
    
                @Override
                public void onSuccess(ServerProperties serverProperties) {
    
                    if(serverProperties != null){
                        callback.onSuccess(serverProperties.getLandingPageMessage());
                    }else{
                        callback.onSuccess(null);
                    }
                }
    
            });
        }else{
            // don't query the server for the properties again
            callback.onSuccess(serverProperties.getLandingPageMessage());
        }
    }
    
    /**
     * Return the logo Url to use in the tutor client.
     *
     * @param callback used to notify the method caller of when the logo Url has been retrieved from the server.
     * Note: the value can be null or empty string
     */
    public static void getLogoUrl(final AsyncCallback<String> callback){

        if(serverProperties == null){
            // retrieve the properties from the server
            tutorUserInterfaceService.getServerProperties(new AsyncCallback<ServerProperties>() {
    
                @Override
                public void onFailure(Throwable arg0) {
                    callback.onFailure(arg0);
                }
    
                @Override
                public void onSuccess(ServerProperties serverProperties) {
    
                    if(serverProperties != null){
                        callback.onSuccess(serverProperties.getPropertyValue(ServerProperties.LOGO));
                    }else{
                        callback.onSuccess(null);
                    }
                }
    
            });
        }else{
            // don't query the server for the properties again
            callback.onSuccess(serverProperties.getPropertyValue(ServerProperties.LOGO));
        }
    }

    /**
     * Selects a domain for the user to run
     *
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param callback Callback for when the server responds
     */
    public void userSelectDomain(String domainRuntimeId, String domainSourceId, final AsyncCallback<RpcResponse> callback) {
        tutorUserInterfaceService.userStartDomainSession(browserSessionKey, domainRuntimeId, domainSourceId, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                if(caught instanceof StatusCodeException && ((StatusCodeException)caught).getStatusCode() == 0) {
                    /* HTTPStatusEvent with a status code of 0 are reported by FireFox
                     * whenever the browser page is refreshed, triggering this onFailure method.
                     * Temporary solution is to catch it and ignore it for now.
                     * (https://gifttutoring.org/issues/1856) */
                } else {
                    Document.getInstance().displayRPCError("Starting a course", caught);
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if (result == null || !result.isSuccess()) {
                    if(result == null) {
                        result = new RpcResponse(null, null, false, "The action timed out on the server.");
                    }
                    Document.getInstance().displayError("Starting a course", result.getResponse(), result.getAdditionalInformation());
                } else {
                    domainSessionListening = true;
                    addHistoryTag(TutorUserWebInterface.DOMAIN_SESSION_PAGE_TAG);
                }
                callback.onSuccess(result);
            }
        });
    }

    /**
     * Sends an action to the server
     *
     * @param action The action for the server to preform. Can't be null.
     * @param callback Callback for when the server responds. Can be null if no callback is needed.
     */
    public void sendActionToServer(AbstractAction action, final AsyncCallback<RpcResponse> callback) {

        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("sendActionToServer() called with action: " + action);
        }

        DoActionAsyncMessage message = new DoActionAsyncMessage(browserSessionKey, action);

        webSocket.send(message, new MessageResponseHandler() {

            @Override
            public void onResponse(AsyncMessageResponse response) {
                logger.info("DoAction response received from server: " + response);
                // For the RpcResponseData, we only care about the rpcResponse.  This was for backwards compatibility
                // to use the Gwt-rpc response data/code.  The actual AsyncMessageResponse just wraps the old gwt rpcResponse.
                AbstractMessageResponseData responseData = response.getResponseData();

                if (responseData != null && responseData instanceof RpcResponseData) {
                    RpcResponse result = ((RpcResponseData)responseData).getRpcResponse();

                    if (result != null ) {
                        if (result.isSuccess()) {
                            if (callback != null) {
                                callback.onSuccess(result);
                            }
                        } else {

                            if(result.getAdditionalInformation() != null){
                                Document.getInstance().displayError("Applying an action on the server", result.getResponse(), result.getAdditionalInformation());
                            }else{
                                Document.getInstance().displayError("Applying an action on the server", result.getResponse());
                            }
                        }
                    }

                } else {
                    callback.onSuccess(null);
                }

            }

        });
    }

    /**
     * Displays the domain selection widget
     */
    public void displaySelectDomainWidget() {
        if (domainSessionListening) {
            getActiveDomainSessionName(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                    displayArticleWidget(new WidgetInstance(WidgetTypeEnum.SELECT_DOMAIN_WIDGET));
                }

                @Override
                public void onSuccess(String result) {
                    if (result != null) {
                        suspendDomainSession();
                        WidgetProperties properties = new WidgetProperties();
                        SelectDomainWidgetProperties.setActiveDomainSessionName(properties, result);
                        SelectDomainWidgetProperties.setReturnToDomainSessionOnExit(properties, true);
                        displayArticleWidget(new WidgetInstance(WidgetTypeEnum.RESUME_SESSION_WIDGET, properties));
                    } else {
                        displayArticleWidget(new WidgetInstance(WidgetTypeEnum.SELECT_DOMAIN_WIDGET));
                    }
                }
            });

        } else {
            displayArticleWidget(new WidgetInstance(WidgetTypeEnum.SELECT_DOMAIN_WIDGET));
        }
    }

    /**
     * Displays the domain selection widget
     */
    public void displayExperimentWelcomeWidget() {

         WidgetProperties properties = new WidgetProperties();
         properties.setIsFullscreen(true);

         ExperimentWidgetProperties.setExperimentId(properties, TutorUserWebInterface.getExperimentId());

         /*
          * Nick: setting a property here to let the experiment welcome widget know when the back button has been pressed. For now,
          * this works here since this method is only called from TutorUserWebInterface when a browser session exists, which can
          * only happen if the back button is pressed (refreshing the page loses the browser session). If this method is called elsewhere
          * this property might need to be set before this method is called and passed in instead.
          */
         ExperimentWidgetProperties.setBackButtonPressed(properties, true);

         if (domainSessionListening) {
            suspendDomainSession();
        }

         displayArticleWidget(new WidgetInstance(WidgetTypeEnum.EXPERIMENT_WELCOME_WIDGET, properties));
    }

    /**
     * Displays the experiment complete widget
     */
    public static void displayExperimentCompleteWidget() {

        WidgetProperties properties = new WidgetProperties();
        properties.setIsFullscreen(true);

        ExperimentCompleteWidget completeWidget =
                new ExperimentCompleteWidget(new WidgetInstance(WidgetTypeEnum.EXPERIMENT_COMPLETE_WIDGET, properties));

        Document.getInstance().fullScreenMode();

        Document.getInstance().setArticleWidget(completeWidget);
    }

    /**
     * Brings the browser session into the domain session
     */
    public void resumeDomainSession() {
        logger.info("Resuming Domain Session" );
        tutorUserInterfaceService.userResumeDomainSessionUpdates(browserSessionKey, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                
                if (TutorUserWebInterface.isEmbedded() && TutorUserWebInterface.isExperiment()) {
                    
                    /*
                     * If resuming the session failed for an experiment user, display an error dialog and return them
                     * to the home page of the experiment so they can run the course again
                     */
                    displayDialogAndLogOut("Your course could not be resumed because an error was encountered while "
                            + "attempting to continue from where the course left off. You will be returned to the "
                            + "beginning of the course.<br/><br/>Details: " + caught.toString());
                    
                } else {
                
                    //if resuming the session failed for a non-experiment user, simply show the error normally
                    Document.getInstance().displayRPCError("Resuming course", caught);
                    History.newItem(TutorUserWebInterface.SELECT_DOMAIN_TAG);
                }
            }

            @Override
            public void onSuccess(RpcResponse result) {
                
                if (!result.isSuccess()) {
                    
                    if (TutorUserWebInterface.isEmbedded() && TutorUserWebInterface.isExperiment()) {
                        
                        /*
                         * If resuming the session failed for an experiment user, display an error dialog and return them
                         * to the home page of the experiment so they can run the course again
                         */
                        displayDialogAndLogOut("Your course could not be resumed because of the following problem: " 
                            + result.getResponse() + "<br/><br/>You will be returned to the beginning of the course.");
                        
                    } else {
                        
                        //if resuming the session failed for a non-experiment user, simply show error normally
                        Document.getInstance().displayError("Resuming course", "The action failed on the server", result.getResponse());
                        History.newItem(TutorUserWebInterface.SELECT_DOMAIN_TAG);
                    }
                    
                } else {
                    
                    //if resuming the session is successful, begin listening to the new session
                    domainSessionListening = true;
                    if(TutorUserWebInterface.isExperiment()) {
                        addHistoryTag(TutorUserWebInterface.buildUserSessionHistoryTag(result.getUserSessionId()));
                    } else {
                        addHistoryTag(TutorUserWebInterface.DOMAIN_SESSION_PAGE_TAG);
                    }
                }
            }
            
            /**
             * Displays a dialog containing the given message to the user while simultaneously ending their domain session
             * and logging out. When the user closes the dialog, they will be returned to the starting point of 
             * a new session.
             * 
             * @param message the message to display
             */
            private void displayDialogAndLogOut(String message) {
                
                //display a dialog containing the given message
                ModalDialogBox leaveDialog = new ModalDialogBox();
                leaveDialog.setGlassEnabled(true);
                leaveDialog.setText("Unable to Resume Course");
                leaveDialog.getElement().getStyle().setProperty("max-width", "700px");

                FlowPanel messagePanel = new FlowPanel();

                HTML messageHtml = new HTML(message);
                messageHtml.getElement().getStyle().setProperty("margin-bottom", "10px");
                messagePanel.add(messageHtml);

                leaveDialog.setWidget(messagePanel);
                
                leaveDialog.setCloseable(true);
                leaveDialog.getCloseButton().addClickHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        if (TutorUserWebInterface.isEmbedded() && TutorUserWebInterface.isExperiment()) {

                            //need to tell the dashboard that the course has ended and return it to the appropriate page
                            EndCourseMessage endCourseMessage = new EndCourseMessage();
                            
                            String experimentReturnUrl = TutorUserWebInterface.getExperimentReturnUrl();
                            
                            if (StringUtils.isNotBlank(experimentReturnUrl)) {
                                
                                //if the dashboard provided a return URL for a course collection, return to that URL
                                endCourseMessage.setExperimentReturnUrl(experimentReturnUrl);
                                
                            } else {
                                
                                //if this tutor window is running an experiment embedded in the dashboard, reload 
                                //the dashboard to return it to the experiment start page
                                endCourseMessage.setShouldReload(true);
                            }
                            
                            IFrameMessageHandlerChild.getInstance().sendMessage(endCourseMessage);
                        }
                    }
                });
            
                leaveDialog.center();
                
                //end the user's session on the server and log them out so they can start fresh
                userEndDomainSessionAndLogout(new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.severe("Failed to log out user after failing to resume a domain session. Reason: " 
                                + caught.toString());
                    }

                    @Override
                    public void onSuccess(RpcResponse result) {
                        
                        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                            logger.info("Successfully logged out user after failing to resume a domain session.");
                        }
                    }
                });
            }
        });
    }

    /**
     * Takes the browser session out of the domain session
     */
    public void suspendDomainSession() {
        tutorUserInterfaceService.userPauseDomainSessionUpdates(browserSessionKey, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Suspending course", caught);
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if (!result.isSuccess()) {
                    Document.getInstance().displayError("Suspending course", result.getResponse());
                } else {
                    domainSessionListening = false;
                }
            }
        });
    }

    /**
     * End the active domain session of the user session this browser session is
     * in
     */
    public void endDomainSession() {
        tutorUserInterfaceService.userEndDomainSession(browserSessionKey, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Ending course", caught);
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if (result == null || !result.isSuccess()) {
                    Document.getInstance().displayError("Ending course", "The action failed on the server.",
                            result == null ? "The action timed out on the server." : result.getResponse());

                    if(!TutorUserWebInterface.isExperiment()){
                        //Force the user to return to the course selection page instead of possibly having them stuck with a TUI
                        //that can't go anywhere because something happened in closing the domain session.
                        displaySelectDomainWidget();

                    } else {
                        displayExperimentCompleteWidget();
                    }

                } else {
                    resumeDomainSession();
                }
            }
        });


    }

    /**
     * Applies an action from the server to the browser session
     *
     * @param action The action to apply
     */
    public void receiveActionFromServer(AbstractAction action) {

        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("receiveActionFromServer() called with action: " + action);
        }


        if (action.getActionType() == ActionTypeEnum.DISPLAY_WIDGET) {

            DisplayWidgetAction displayPage = (DisplayWidgetAction) action;

            if(logger.isLoggable(Level.FINE)){
                logger.fine("Action type is DISPLAY_WIDGET: " + displayPage.toString());
            }

            if (displayPage.getWidgetInstance() != null) {
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Displaying widget: " + displayPage.getWidgetInstance().getWidgetType() + " in " + displayPage.getDisplayLocation());
                }
            }
            if (displayPage.getDisplayLocation() == WidgetLocationEnum.ARTICLE) {
                displayArticleWidget(displayPage.getWidgetInstance());
            } else if (displayPage.getDisplayLocation() == WidgetLocationEnum.FOOTER && loginType != WidgetTypeEnum.BLANK_WIDGET) {
                displayFooterWidget(displayPage.getWidgetInstance());
            }

        } else if (action.getActionType() == ActionTypeEnum.DISPLAY_DIALOG) {
            DisplayDialogAction displayDialog = (DisplayDialogAction) action;
            displayDialog(displayDialog.getDialog());
        } else if(action.getActionType() == ActionTypeEnum.INIT_APP) {
            InitTrainingAppAction initAppAction = (InitTrainingAppAction) action;

            //Navigates to the first URL in the collection. Multiple apps are not yet supported.
            String url = initAppAction.getUrls().get(0);
            UserActionAvatarContainer.getInstance().loadWebAppliation(url);
        } else if(action.getActionType() == ActionTypeEnum.SEND_APP_MESSASGE) {
            TrainingAppMessageAction postMessageAction = (TrainingAppMessageAction) action;
            String appMessage = postMessageAction.getMessage();
            UserActionAvatarContainer.getInstance().sendMessageToWebApplication(appMessage);

        } else if (action.getActionType() == ActionTypeEnum.DEACTIVATE) {
            DeactivateAction deactivateAction = (DeactivateAction) action;
            if(logger.isLoggable(Level.FINE)){
                logger.fine("Deactivating because: " + deactivateAction.getMessage());
            }
            invalidate();

            // The course has been abnormally terminated.  Once case this can happen is if
            // the user logs in from another web browser while the course is running.
            // In this case, we want to end the course and return the user to the dashboard
            // if we are running in embedded mode.
            sendEndCourseMessageToDashboard();

        } else if (action.getActionType() == ActionTypeEnum.END_COURSE) {
            // If we're in embedded mode (and not an experiment), once the domain session ends, we go back to the course selection screen.
            // We need to log out the user and communicate that the domain session has ended to the parent.
            if (TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment()) {
                // logout user and send a message back to the parent window.
                this.logout(new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable t) {
                        sendEndCourseMessageToDashboard();

                    }

                    @Override
                    public void onSuccess(RpcResponse result) {
                        sendEndCourseMessageToDashboard();
                    }

                });

            }

        } else if (action.getActionType() == ActionTypeEnum.START_COURSE) {

            if (TutorUserWebInterface.isEmbedded()) {

                // Create and embed a new message type.
                IFrameSimpleMessage startSession = new IFrameSimpleMessage(IFrameMessageType.COURSE_READY);
                IFrameMessageHandlerChild.getInstance().sendMessage(startSession);
            }

        } else if (action.getActionType() == ActionTypeEnum.LESSON_STATUS) {

            LessonStatusAction statusAction = (LessonStatusAction) action;

            // If the user begins or ends a training application lesson, update the avatar used with training apps so that
            // it knows to maintain its state when mid-lesson activities (such as surveys) are handled.
            UserActionAvatarContainer.getInstance().setLessonStatus(statusAction.getLessonStatus());


        } else if (action.getActionType() == ActionTypeEnum.KNOWLEDGE_SESSIONS_UPDATED) {

            KnowledgeSessionsUpdated knowledgeSessions = (KnowledgeSessionsUpdated)action;
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Received action from the server: " + knowledgeSessions);
            }

            // Pass any knowledge session updates to any listener (if active).
            if (knowledgeSessionListener != null) {
                knowledgeSessionListener.onKnowledgeSessionUpdated(knowledgeSessions.getKnowledgeSessions());
            }


        } else if (action.getActionType() == ActionTypeEnum.PRELOAD_AVATAR) {

            PreloadAvatarAction preloadAction = (PreloadAvatarAction) action;

            if(preloadAction.getAvatarData() != null) {
                AvatarContainer.loadAvatar(preloadAction.getAvatarData());

            } else {
                logger.warning("Server requested to preload an avatar, but no avatar data was received.");
            }


        } else if (action instanceof AbstractWidgetAction) {

            AbstractWidgetAction widgetAction = (AbstractWidgetAction) action;
            if (articleWidget != null && widgetAction.getWidgetId().equals(articleWidget.getWidgetId())) {

                if (widgetAction.getActionType() == ActionTypeEnum.CLOSE) {
                    try {

                        if(articleWidget.getWidgetProperties().getIsFullscreen()) {
                            Document.getInstance().fullScreenMode();
                        }

                        if(logger.isLoggable(Level.INFO)){
                            logger.info("The server sent a close action on the currently displayed article "+articleWidget.getWidgetId()+". Clearing the article widget.  This can cause onUnload GWT methods to be called.");
                        }

                        // clear the article widget - this is needed by TutorActionsWidget in order to call the onUnloaded method which then
                        // removes the avatar so the avatar can be reloaded for the next course object widget.  Its the avatar reload that causes
                        // an avatar idle notification to be sent to the tutor server which allows queued messages to be delivered to the client.
                        Document.getInstance().setArticleWidget(null);

                        // clear the tutor actions widget (a singleton) so that it will be recreated for the next course object that needs it
                        // Not doing this will currently cause on going conversations to appear in subsequent tutor action widget
                        // displays for those course objects.
                        TutorActionsWidget.reset();

                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "There was an error creating a logo widget", e);
                    }
                } else {
                    articleWidget.applyAction(widgetAction);
                }

            } else if (footerWidget != null && widgetAction.getWidgetId().equals(footerWidget.getWidgetId())) {

                //the footer widget matches the ID of the widget action, so apply the action to the footer widget
                if (widgetAction.getActionType() == ActionTypeEnum.CLOSE) {
                    Document.getInstance().setFooterWidget(null);
                } else {
                    footerWidget.applyAction(widgetAction);
                }

            } else if(UserActionAvatarContainer.getInstance().getLoadingOverlay() != null
                    && widgetAction.getWidgetId().equals(UserActionAvatarContainer.getInstance().getLoadingOverlay().getWidgetId())){

                // the loading message widget for a training application matches the ID of the widget action, so
                // apply the action to the loading message widget
                if (widgetAction.getActionType() == ActionTypeEnum.CLOSE) {
                    UserActionAvatarContainer.getInstance().setLoadingOverlay(null, null);

                } else {
                    UserActionAvatarContainer.getInstance().getLoadingOverlay().applyAction(widgetAction);
                }
            }
        }
    }


    /**
     * Sends an END_COURSE message to the dashboard (if the tutor is running in embedded mode).
     */
    private void sendEndCourseMessageToDashboard() {
        // If we're in embedded mode (and not in an experiment), once the domain session ends, we go back to the course selection screen.
        // We need to log out the user and communicate that the domain session has ended to the parent.
        // Don't send the message back if the user has explicitly stopped the course (outside of the TUI -- either by
        // signing out of the dashboard or by pressing the 'stop course' button in the dashboard).  In these cases, we
        // don't need to message back to the Dashboard.
        if (TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment()) {
            EndCourseMessage endSession = new EndCourseMessage();
            endSession.setDomainId(getCachedDomainId());
            IFrameMessageHandlerChild.getInstance().sendMessage(endSession);
        }

    }

    /**
     * Create a new user, creating a browser session upon success
     *
     * @param isMale Is the user to be created a male
     * @param lmsUsername The LMS username of the user to be created
     * @param callback Callback for when the server responds
     */
    public static void createNewUser(boolean isMale, String lmsUsername, final AsyncCallback<RpcResponse> callback) {

        tutorUserInterfaceService.createNewUser(
                isMale,
                lmsUsername,
                new ClientProperties(serverProperties.getWebSocketId(), WebDeviceUtils.getMobileAppProperties()),
                new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final RpcResponse result) {
                if (result.isSuccess()) {

                    BrowserSessionReadyCallback browserReadyCallback = new BrowserSessionReadyCallback() {

                        @Override
                        public void onSessionSuccess() {
                            getInstance().displaySelectDomainWidget();

                            if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                                logger.info("browser session created, user:" + result.getUserSessionId() + ", browserSession: " + result.getBrowserSessionId());
                            }

                            callback.onSuccess(result);
                        }

                        @Override
                        public void onSessionFailure() {

                            callback.onSuccess(new RpcResponse (null, null, false, "Failed to open a web socket connection to the server."));
                        }

                    };

                    createInstanceAndAttachSocket(result.getUserSessionId(), result.getBrowserSessionId(),
                            WidgetTypeEnum.SIMPLE_LOGIN_WIDGET, browserReadyCallback);

                } else {
                    callback.onSuccess(result);
                }

            }
        });
    }

    /**
     * Used to login in user and start a course.  This is useful for things like the GIFT dashboard
     * that want to bypass the course selection page in the TUI.
     *
     * @param username the GIFT user name to login
     * @param password the user's password, used to authenticate the user on the server
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param callback used to notify the caller of the request's response
     */
    public static void loginAndStartCourse(final String username, final String password, final String loginAsUserName, final String domainRuntimeId, final String domainSourceId, final AsyncCallback<RpcResponse> callback){

        logger.info("Logging in with username of " + username+" and starting course identified by '"+domainRuntimeId+"'.");

        final Command loginCommand = new Command() {

            @Override
            public void execute() {

                /*
                 * Nick 5/19/2015: Splitting the call to login and start the course into two sequential RPC calls. Originally, the code here made a
                 * single RPC call to login the user and start the course; however, since a BrowserSession would not be created until the RPC call
                 * returned and since the RPC call wouldn't return until the course was started, the TUI would become stuck when trying to start
                 * courses requiring the Gateway module while running in server mode. The BrowserSession would not start until the course started, and
                 * the course would not start until the course intialization screen was passed, which requires that the BrowserSession be started. To
                 * fix this, the BrowserSession is now started immediately after the login, allowing the course initialization screen to be shown so
                 * the course can eventually be started.
                 */

        tutorUserInterfaceService.userLogin(
                username,
                password,
                loginAsUserName,
                new ClientProperties(serverProperties.getWebSocketId(), WebDeviceUtils.getMobileAppProperties()),
                new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable e) {
                        callback.onFailure(e);
                    }

                    @Override
                    public void onSuccess(final RpcResponse result) {

                        if(result != null && result.isSuccess()){

                            WidgetTypeEnum type = WidgetTypeEnum.BLANK_WIDGET;

                            logger.info("Logged in with username " + username + " with user session ID " + result.getUserSessionId() + " and browser session ID" + result.getBrowserSessionId());

                            BrowserSessionReadyCallback browserReadyCallback = new BrowserSessionReadyCallback() {

                                @Override
                                public void onSessionSuccess() {
                                    logger.info("Browser Session ready");
                                    if(instance == null){
                                        logger.info("Instance is SOMEHOW null");
                                    }
                                    // Initialize data that the browser session needs when a course is started.
                                    getInstance().initCourseStartedData(domainRuntimeId);

                                    logger.info("startCourse request called.");
                                    final String browserSessionId = result.getBrowserSessionId();
                                    instance.startCourseAsync(browserSessionId, domainRuntimeId, domainSourceId, null, callback);

                                }

                                @Override
                                public void onSessionFailure() {
                                    callback.onSuccess(new RpcResponse(null, null, false, "Failed to open a websocket connection to the server."));

                                }

                            };


                            createInstanceAndAttachSocket(result.getUserSessionId(), result.getBrowserSessionId(), type, browserReadyCallback);

                        } else {

                            RpcResponse failedResult = result;
                            if(failedResult == null) {
                                failedResult = new RpcResponse(null, null, false, "The action timed out on the server.");
                            }

                            logger.severe("Could not login: " + failedResult.getResponse());
                            callback.onSuccess(failedResult);

                        }
                    }

                });
            }
        };

        if(instance != null){

            /* If a browser session is currently running, we want to log out to completely clean it up before we log in and start the next session.
             * If we don't do this, then we open up the possibility that, with just the right timing, the previous browser session's ending messages
             * may potentially end up getting sent AFTER the second browser session is started on the client (since the logic that handles ending
             * the previous browser session is handled on a separate thread on the server). This can cause the second browser session to get into
             * a bad state if the messages come in while it is still initializing, which will often end up prompting the course to end prematurely.
             *
             * If we force the log out now instead of waiting for the previous browser session's domain session to end (which is what normally
             * triggers the log out in embedded mode), then we avoid this problem, since the ending messages will always get sent out before
             * the second browser session is started.
             */

            logger.info("Previous browser session detected. Attempting to log out previous session before login attempt.");

            final String endedSessionKey = instance.browserSessionKey;

            instance.logout(new AsyncCallback<RpcResponse>() {

                @Override
                public void onFailure(Throwable caught) {

                    logger.warning("Failed to log out browser session " + endedSessionKey + ". Attempting to login anyway. Failure reason:\n" + caught);

                    loginCommand.execute();
                }

                @Override
                public void onSuccess(RpcResponse result) {

                    if(result == null){
                        logger.warning("Failed to log out browser session " + endedSessionKey + ". Attempting to login anyway. Failure reason:\n"
                                + "An invalid response was received from the log out attempt.");

                    } else if(result.getResponse() != null){
                        logger.warning("Failed to log out browser session " + endedSessionKey + ". Attempting to login anyway. Failure reason:\n"
                                + result.getResponse());
                    }

                    loginCommand.execute();
                }
            });

        } else {

            // if no browser session is currently running, simply log in normally
            loginCommand.execute();
        }
    }

    /**
     * Internal method to commonize functionality used to start a course asyncronously.
     *
     * @param browserSessionId the browser session id making the request
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param launchParams additional parameters that may be needed to start the course or provide runtime information to the course.
     * @param callback The callback that will be called when a response is received.
     */
    private void startCourseAsync(final String browserSessionId, final String domainRuntimeId, final String domainSourceId, AbstractCourseLaunchParameters launchParams, final AsyncCallback<RpcResponse> callback) {
        logger.info("BrowserSession.startCourse() called.");
        tutorUserInterfaceService.startCourse(browserSessionId, domainRuntimeId, domainSourceId, launchParams, new AsyncCallback<RpcResponse>() {

            @Override
            public void onSuccess(RpcResponse response) {


                logger.info("startCourse onSuccess(): " + response);
                if (instance != null) {
                    instance.checkCourseProgress = new Timer(){

                        @Override
                        public void run() {
                            logger.info("Checking course start progress.");
                            if(instance != null && instance.browserSessionKey != null){

                                tutorUserInterfaceService.checkStartCourseStatus(browserSessionId, domainRuntimeId, domainSourceId, new AsyncCallback<RpcResponse>() {

                                    @Override
                                    public void onSuccess(RpcResponse result) {

                                        if (result != null) {
                                            logger.info("checkStartCourseStatus returned result: " + result);
                                            instance.finishCourseStarted();
                                            callback.onSuccess(result);
                                        } else {
                                            logger.info("Course is not started yet....checking again.");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Throwable thrown) {
                                        logger.warning("A problem while calling the checkStartCourseStatus request: " + thrown.toString());
                                        callback.onFailure(thrown);
                                    }
                                });
                            } else {
                                logger.warning("Failed to reset subject response timeout.");
                            }
                        }
                    };

                    instance.checkCourseProgress.scheduleRepeating(CHECK_COURSE_INTERVAL_MS);
                    instance.checkCourseProgress.run();

                    logger.info("startCourse timer started");
                }

                IFrameSimpleMessage courseStartingMsg = new IFrameSimpleMessage(IFrameMessageType.COURSE_STARTING);
                IFrameMessageHandlerChild.getInstance().sendMessage(courseStartingMsg);

                // Do not call callback.onSuccess() here.
                // The callback is called later on once the server finishes setting up the course.
            }

            @Override
            public void onFailure(Throwable e) {

                logger.severe("startCourse failure() " + e.getMessage());
                callback.onFailure(e);
            }
        });
    }

    /**
     * Method to perform logic once the course has been started by the server.
     */
    private void finishCourseStarted() {
        instance.checkCourseProgress.cancel();
        instance.checkCourseProgress = null;
        instance.domainSessionListening = true;
        getInstance().addHistoryTag(TutorUserWebInterface.DOMAIN_SESSION_PAGE_TAG);
    }

    /**
     * This method detects when a webpage has been refreshed due to the IE timeout (the default timeout value is 60 minutes).
     * If a domain session for the given username is running, the session will be resumed. Otherwise, a new session will start.
     *
     * @param username The username to create or retrieve a session for
     * @param password The password to login the user
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param callback The callback to execute when the operation is complete
     */
    public static void startOrResumeCourse(final String username, final String password, final String loginAsUserName, final String domainRuntimeId, final String domainSourceId, final AsyncCallback<RpcResponse> callback) {

        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("startOrResumeCourse called: " + domainRuntimeId + ", for user: " + username);
        }
        // Check if there is an existing session
        tutorUserInterfaceService.getExistingUserSession(username, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable t) {
                logger.warning("Check for existing user session failed: " + t);
                loginAndStartCourse(username, password, loginAsUserName, domainRuntimeId, domainSourceId, callback);
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if(result != null && result.isSuccess()) {

                    logger.info("Found an existing user session for "+username+" with user session id "+result.getUserSessionId()+".  Notfying the server to resume user session.");

                    // if there is an existing session, get the browser session for it
                    resumeUserSession(result.getUserSessionId(), new AsyncCallback<Boolean>() {

                        @Override
                        public void onFailure(Throwable t) {
                            logger.warning("Failed to resume user session: " + t);
                        }

                        @Override
                        public void onSuccess(Boolean success) {
                            if(success) {
                                // make sure the browser session has a matching domain id
                                instance.getActiveDomainSessionName(new AsyncCallback<String>() {

                                    @Override
                                    public void onFailure(Throwable t) {
                                        logger.warning("Unable to get active domain session name: " + t);
                                    }

                                    @Override
                                    public void onSuccess(String result) {
                                        logger.warning("Active Domain Session Name = " + result);
                                        // resume the matching domain session
                                        if(result != null && result.equals(domainRuntimeId)) {
                                            instance.resumeDomainSession();
                                        } else {
                                            // end the previous domain session and start a new one
                                            loginAndStartCourse(username, password, loginAsUserName, domainRuntimeId, domainSourceId, callback);
                                        }
                                    }

                                });
                            } else {
                                loginAndStartCourse(username, password, loginAsUserName, domainRuntimeId, domainSourceId, callback);
                            }
                        }

                    });
                } else {
                    loginAndStartCourse(username, password, loginAsUserName, domainRuntimeId, domainSourceId, callback);
                }
            }

        });
    }

    /**
     * Initializes the course started data each time the browser session starts the course.
     *
     * @param domainId - The runtime course id of the course that is being started.
     */
    private void initCourseStartedData(String domainId) {
        // Cache off the domain id.
        getInstance().setCachedDomainId(domainId);
    }

    /**
     * Set the cached domain id that is valid once a user starts a course.  The domain id
     * is the string value containing the runtime domain id of the course that the user has started
     * @param domainId - String value containing the runtime domain id of the course the user is executing.  Cannot be null, but can be an empty string if there is no course started.
     */
    private void setCachedDomainId(String domainId) {

        logger.fine("domain id = " + domainId);
        cachedDomainId = domainId;
    }

    /**
     * Retrieve the cached domain id of the course that the user has started.
     *
     * @return String - The domain id of the course that the user has started.  Can return an empty string if there is no course started.
     */
    private String getCachedDomainId() {
        return cachedDomainId;
    }

    /**
     * Used to login in user and start a course.  This is useful for things like the GIFT dashboard
     * that want to bypass the course selection page in the TUI.
     *
     * @param username the GIFT user name to login. Should match the username of the supplied userId.
     * @param userId the unique GIFT user id to login. Should match the userId of the supplied username.
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param callback used to notify the caller of the request's response
     */
    public static void loginAndStartOfflineCourse(final String username, final int userId, final String domainRuntimeId, final String domainSourceId, final AsyncCallback<RpcResponse> callback){

        if(logger.isLoggable(Level.INFO)) {
            logger.info("Logging in (offline) with username of " + userId +" and starting course identified by '"+domainRuntimeId+"'.");
        }

        tutorUserInterfaceService.offlineUserLoginAndSelectDomain(
                username,
                userId,
                domainRuntimeId,
                domainSourceId,
                new ClientProperties(serverProperties.getWebSocketId(), WebDeviceUtils.getMobileAppProperties()),
                new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.WARNING, "Could not login", caught);
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final RpcResponse result) {

                if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                    logger.info("offlineUserLoginAndSelectDomain rpc returned result: " + result);
                }

                if (result.isSuccess()) {
                    WidgetTypeEnum type = loginType;

                    if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                        logger.info("Logged in with username " + userId + " with user session ID " + result.getUserSessionId() + " and browser session ID" + result.getBrowserSessionId());
                    }

                    BrowserSessionReadyCallback browserReadyCallback = new BrowserSessionReadyCallback() {

                        @Override
                        public void onSessionSuccess() {
                            // Initialize data that the browser session needs when a course is started.
                            getInstance().initCourseStartedData(domainRuntimeId);

                            instance.domainSessionListening = true;
                            getInstance().addHistoryTag(TutorUserWebInterface.DOMAIN_SESSION_PAGE_TAG);

                            callback.onSuccess(result);

                        }

                        @Override
                        public void onSessionFailure() {
                            callback.onSuccess(new RpcResponse(null, null, false, "Failed to open a websocket connection to the server."));

                        }
                    };

                    createInstanceAndAttachSocket(result.getUserSessionId(), result.getBrowserSessionId(), type, browserReadyCallback);



                } else {
                    logger.severe("Could not login: " + result.getResponse());
                    callback.onSuccess(result);
                }

            }
        });
    }

    /**
     * Used to login in an LTI Tool Consumer user and start a course.  This bypasses the tui login and
     * domain selection screen and starts the course.
     *
     * @param params The LTI parameters containing the information needed to start an LTI course.
     * @param courseRuntimeId The path of the runtime course to be loaded.
     * @param callback used to notify the caller of the request's response
     */
    public static void ltiLoginAndStartCourse(final LtiParameters params, final String courseRuntimeId, final AsyncCallback<RpcResponse> callback){

        logger.info("Logging in and starting course for lti params " + params +", and starting course identified by '"+courseRuntimeId+"'.");


        final String consumerKey = params.getConsumerKey();
        final String consumerId = params.getConsumerId();
        final String dataSetId = params.getDataSetId();
        final String courseSourceId = params.getCourseId();

        // This was changed to 2 calls for similar reasons to the normal loginAndStartCourse() method.
        /*
         * Nick 5/19/2015: Splitting the call to login and start the course into two sequential RPC calls. Originally, the code here made a
         * single RPC call to login the user and start the course; however, since a BrowserSession would not be created until the RPC call
         * returned and since the RPC call wouldn't return until the course was started, the TUI would become stuck when trying to start
         * courses requiring the Gateway module while running in server mode. The BrowserSession would not start until the course started, and
         * the course would not start until the course initialization screen was passed, which requires that the BrowserSession be started. To
         * fix this, the BrowserSession is now started immediately after the login, allowing the course initialization screen to be shown so
         * the course can eventually be started.
         */
        tutorUserInterfaceService.ltiUserLogin(
                consumerKey,
                consumerId,
                dataSetId,
                new ClientProperties(serverProperties.getWebSocketId(), WebDeviceUtils.getMobileAppProperties()),
                new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                logger.warning("ltiUserLogin onFailure(): " + caught.getMessage());
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final RpcResponse result) {

                if (result.isSuccess()) {

                    if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                        logger.info("Logged in lti user with consumerKey " + consumerKey + " with user session ID " + result.getUserSessionId() + " and browser session ID" + result.getBrowserSessionId());
                    }

                    BrowserSessionReadyCallback browserReadyCallback = new BrowserSessionReadyCallback() {

                        @Override
                        public void onSessionSuccess() {
                            // Initialize data that the browser session needs when a course is started.
                            getInstance().initCourseStartedData(courseRuntimeId);
                            instance.startCourseAsync(result.getBrowserSessionId(), courseRuntimeId, courseSourceId, params, callback);

                        }

                        @Override
                        public void onSessionFailure() {
                            callback.onSuccess(new RpcResponse(null, null, false, "Failed to open a websocket connection to the server."));

                        }

                    };

                    createInstanceAndAttachSocket(result.getUserSessionId(), result.getBrowserSessionId(), WidgetTypeEnum.BLANK_WIDGET, browserReadyCallback);


                } else {

                    // Just return the result back to the callback.
                    callback.onSuccess(result);
                }
            }
        });
    }

    /**
     * Login using the specified user ID, creating a browser session upon
     * success
     *
     * @param userId The user ID to login as
     * @param callback Callback for when the server responds
     */
    public static void loginUser(final int userId, final AsyncCallback<RpcResponse> callback) {
        logger.info("Logging in with user ID " + userId);
        tutorUserInterfaceService.userLogin(
                userId,
                new ClientProperties(serverProperties.getWebSocketId(), WebDeviceUtils.getMobileAppProperties()),
                new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Could not login", caught);
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final RpcResponse result) {
                if (result != null && result.isSuccess()) {
                    if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                        logger.info("Logged in with user ID " + userId + " with user session ID " + result.getUserSessionId() + " and browser session ID" + result.getBrowserSessionId());
                    }

                    BrowserSessionReadyCallback browserReadyCallback = new BrowserSessionReadyCallback() {

                        @Override
                        public void onSessionSuccess() {
                            getInstance().displaySelectDomainWidget();
                            getInstance().displayFooterText("Logged in as user " + userId);
                            getInstance().userId = userId;
                            callback.onSuccess(result);

                        }

                        @Override
                        public void onSessionFailure() {
                            callback.onSuccess(new RpcResponse(null, null, false, "A websocket connection was not successfully created to the server."));

                        }

                    };


                    try {
                        createInstanceAndAttachSocket(result.getUserSessionId(), result.getBrowserSessionId(),
                                WidgetTypeEnum.SIMPLE_LOGIN_WIDGET, browserReadyCallback);
                    } catch(Throwable t) {
                        callback.onFailure(t);
                    }
                } else {
                    RpcResponse failedResult = result;
                    if(failedResult == null) {
                        failedResult = new RpcResponse(null, null, false, "The action timed out on the server.");
                    }
                    logger.severe("Could not login: " + failedResult.getResponse());
                    callback.onSuccess(failedResult);
                }

            }
        });
    }

    /**
     * Resumes a user session if it is still active on the server
     *
     * @param userSessionKey The user session key of the user session to resume
     * @param callback If the action was successful or not
     */
    public static void resumeUserSession(final String userSessionKey, final AsyncCallback<Boolean> callback) {
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("resumeUserSession()");
        }

        //prevent experiments from being started if the gift instance has restricted access at the moment
        if(BrowserSession.serverProperties.hasRestrictedAccess()){
            logger.warning("Unable to resume user session because this GIFT instance has restricted user access.");
            callback.onSuccess(false);
        }

        tutorUserInterfaceService.createBrowserSession(
                userSessionKey,
                new ClientProperties(serverProperties.getWebSocketId(), WebDeviceUtils.getMobileAppProperties()),
                new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(RpcResponse result) {
                final boolean success = result != null && result.isSuccess();
                if (result != null && success) {
                    if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                        logger.info("Resumed an existing user session with user session ID " + result.getUserSessionId() + " and browser session ID" + result.getBrowserSessionId());
                    }

                    BrowserSessionReadyCallback browserReadyCallback = new BrowserSessionReadyCallback() {

                        @Override
                        public void onSessionSuccess() {
                            // Call the callback.
                            callback.onSuccess(success);

                        }

                        @Override
                        public void onSessionFailure() {
                            logger.severe("Failed to establish a websocket connection to the server.");
                            callback.onSuccess(false);

                        }
                    };

                    WidgetTypeEnum loginType = WidgetTypeEnum.SIMPLE_LOGIN_WIDGET;

                    if(TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment()){
                        loginType = WidgetTypeEnum.BLANK_WIDGET;
                    }

                    createInstanceAndAttachSocket(result.getUserSessionId(), result.getBrowserSessionId(),
                            loginType, browserReadyCallback);
                } else {
                   logger.severe("User session ID is invalid, could not get a browser session ID");
                   callback.onSuccess(success);
                }

            }
        });
    }

    /**
     * Request the properties for the client from the server and then store them in this class for later use
     *
     * @param callback used to call back when the server has responded.
     */
    public static void retrieveServerProperties(final AsyncCallback<Boolean> callback){
        
        AsyncCallback<ServerProperties> receivedProperties = new AsyncCallback<ServerProperties>() {

            @Override
            public void onSuccess(ServerProperties properties) {
                logger.info("set server properties\n"+properties);
                BrowserSession.serverProperties = properties;
                postPropertiesReceived(callback);                
            }

            @Override
            public void onFailure(Throwable throwable) {
                
                if(WebDeviceUtils.isMobileAppEmbedded()) {
                    
                    /* Try connecting again in case the network dropped temporarily */
                    retrieveServerProperties(callback);
                    
                } else {
                    Document.getInstance().displayDialogInDashboard("Retrieving Server Properties", "Failed to retrieve server properties", throwable.toString());
                    callback.onFailure(throwable);
                }
            }
        };

        if(serverProperties == null){
            // retrieve them from the server
            tutorUserInterfaceService.getServerProperties(receivedProperties);
        }else{
            // apply the properties that have already been received from the server in the past, just in case 
            postPropertiesReceived(callback);
        }
    }
    
    /**
     * Apply the properties received from the server.
     * 
     * @param callback notify the caller that the server properties have been applied
     */
    private static void postPropertiesReceived(final AsyncCallback<Boolean> callback){
        
        // set the background
        String backgroundUrl = serverProperties.getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
        Document.getInstance().setBackground(backgroundUrl);
        
        
        if(webSocket == null){
            establishWebSocketConnection(callback);

        } else {
            callback.onSuccess(true);
        }
    }

    /**
     * Creates a web socket for this client and establishes its connection to the server. If no {@link BrowserSession} instance
     * is currently available, the created web socket will be given a temporary handler until the session is created. If a
     * If a {@link BrowserSession} instance is available (such as when reconnecting), then it will be used as the handler for
     * the web socket.
     * <br/><br/>
     * Note that this method will invoke the provided callback's failure case if it is called before
     * the server properties have been loaded.
     *
     * @param callback a callback used to handle the result of establishing the web socket connection (i.e.
     * whether the connection was established successfully or not.
     */
    private static void establishWebSocketConnection(final AsyncCallback<Boolean> callback) {

        try{

            if(serverProperties == null) {
                throw new IllegalArgumentException("Failed to establish web socket connection because the server "
                        + "properties have not yet been loaded.");
            }

            // Use the Tutor's host page URL and web socket servlet to build a URL for the web socket connection
            //
            // NOTE: By creating the web socket URL from the host page URL, we ensure that both the web socket connection
            // is established on the same HTTP session used to handle RPC requests to the host. This is needed in order to
            // allow the web socket to be retrieved when creating the server-side browser session during a RPC call
            String tutorWsServlet = getTutorWebSocketUrl();
            String tutorUrl = GWT.getHostPageBaseURL();

            String scheme = "ws";

            if(serverProperties.shouldUseHttps()){
                scheme = "wss"; //if the Tutor is using HTTPs, we also need to use a secure ws connection
            }

            String webSocketUrl = scheme + tutorUrl.substring(tutorUrl.indexOf("://")) + tutorWsServlet + "?"
                    + TutorWebSocketUtils.SOCKET_ID_PARAM + "=" + serverProperties.getWebSocketId();

            if(webSocket != null) {

                //remove the handler for the existing web socket, since it is being replaced
                webSocket.setBrowserSession(null);
            }

            //open a web socket connection to the server
            webSocket = new TutorClientWebSocket(webSocketUrl);

            if(instance == null) {

                // This is used to ensure that the socket is open before we try creating the web browser session.
                // It is a temporary handler since the browser session implements the handler and will overwrite this one.
                webSocket.setSocketHandler(new ClientWebSocketHandler() {

                    @Override
                    public void onSocketClosed() {
                        callback.onFailure(new DetailedException("Temporary socket handler received socket closed message.",
                                "Temporary socket handler received socket closed message.", null));
                    }

                    @Override
                    public void onSocketOpened() {
                        callback.onSuccess(true);
                    }

                    @Override
                    public void onJavaScriptException(JavaScriptException ex) {
                        callback.onFailure(new DetailedException("Temporary socket handler received javascript exception",
                                ex.getDescription(), null));

                    }
                });

            } else {

                // The existing browser session is attempting to re-establish a web socket connection, so continue using
                // it as the handler for the new web socket
                webSocket.setBrowserSession(instance);
            }

        } catch (JavaScriptException jsEx) {
            
            if(WebDeviceUtils.isMobileAppEmbedded()) {
                
                /* Try connecting again in case the network dropped temporarily */
                establishWebSocketConnection(callback);
                
            } else {
                
                // Catch the websocket javascript exception.
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                    .append("Unable to establish websocket due to the JavaScriptException: ").append(jsEx.getName()).append("<br/>")
                    .append("<br/>")
                    .append("If running in Internet Explorer this might be fixed by doing the following: <br/>")
                    .append("1.) Navigate to 'Internet options'<br/>")
                    .append("2.) Click the 'Security' tab<br/>")
                    .append("3.) Click 'Local intranet'<br/>")
                    .append("4.) Click 'Sites'<br/>")
                    .append("5.) Uncheck 'Include all local (intranet) sites not listed in other zones'<br/>")
                    .append("<br/>")
                    .append("If you're unable to change these settings, you can also try changing the IP address in the address bar (e.g. 10.0.0.1).<br/>")
                    .append("Replacing the IP address with localhost may fix the issue but this replacement may result in undefined behavior.");

                Document.getInstance().displayDialogInDashboard("WebSocket Error", 
                        "An unexpected error occurred while settting up the websocket connection", 
                        stringBuilder.toString());
                callback.onFailure(jsEx);
            }
            

        }  catch(Exception e){
            logger.severe("Unable to establish web socket connection. Reason: " + e);
            
            
            if(WebDeviceUtils.isMobileAppEmbedded()) {
                
                /* Try connecting again in case the network dropped temporarily */
                establishWebSocketConnection(callback);
                
            } else {
                callback.onFailure(e);
            }
        }
    }

    /**
     * Invalidates the current browser session.
     * Note this will also cause the login web page to be shown.
     *
     * @return BrowserSession always null
     */
    public static BrowserSession invalidate() {

        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("BrowserSession invalidate() called.");

            if(instance != null){
                 logger.info("BrowserSession invalidate() called for " + instance.browserSessionKey);
            } else {
                 logger.info("BrowserSession invalidate() called for null.");
            }
        }

        final String experimentId = TutorUserWebInterface.getExperimentId();
        if(StringUtils.isNotBlank(experimentId)){
            final String experimentCoursePath = TutorUserWebInterface.getExperimentCoursePath();

            instance = null;
            BrowserProperties.getInstance().setUserSessionKey(null);

            try {

                if(Document.getInstance().getArticleWidget() instanceof ErrorPageWidget ||
                   Document.getInstance().getArticleWidget() instanceof ExperimentCompleteWidget){
                    //if an error page is showing or experiment complete widget, don't replace it with the start page
                    return instance;
                }

                //prevent experiments from being started if the gift instance has restricted access at the moment
                if(BrowserSession.serverProperties.hasRestrictedAccess()){
                    logger.warning("This GIFT instance has restricted user access.  Please contact your GIFT admin for access.");
                    Frame unavailableFrame = new Frame();
                    unavailableFrame.getElement().getStyle().setProperty("border", "none");
                    
                    String backgroundUrl = BrowserSession.serverProperties.getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
                    unavailableFrame.getElement().getStyle().setBackgroundImage("linear-gradient(transparent, rgba(255,0,0,0.6)), url('"+backgroundUrl+"')");
                    unavailableFrame.setHeight("100%");
                    unavailableFrame.setWidth("100%");
                    unavailableFrame.setUrl(RESTRICTED_ACCESS_PAGE_URL);

                    RootLayoutPanel.get().clear();
                    RootLayoutPanel.get().add(unavailableFrame);

                    return instance;
                }

                final WidgetProperties properties = new WidgetProperties();
                properties.setIsFullscreen(true);

                ExperimentWidgetProperties.setExperimentId(properties, experimentId);
                ExperimentWidgetProperties.setExperimentFolder(properties, experimentCoursePath);

                if(History.getToken().contains(TutorUserWebInterface.USER_SESSION_ID)) {
                    String userSessionId = History.getToken();
                    userSessionId = userSessionId.substring(userSessionId.indexOf(TutorUserWebInterface.USER_SESSION_ID));
                    userSessionId = userSessionId.substring(userSessionId.indexOf("=") + 1);

                    resumeUserSession(userSessionId, new AsyncCallback<Boolean>() {

                        @Override
                        public void onFailure(Throwable arg0) {
                            ExperimentWelcomeWidget startWidget = new ExperimentWelcomeWidget(new WidgetInstance(WidgetTypeEnum.EXPERIMENT_WELCOME_WIDGET, properties));

                            Document.getInstance().fullScreenMode();

                            Document.getInstance().setArticleWidget(startWidget, true);
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            if (!result && TutorUserWebInterface.isEmbedded()) {
                                final EndCourseMessage endMsg = new EndCourseMessage();
                                /* If a return URL was provided, add it to the
                                 * end course message so the message handler
                                 * knows to redirect the session to that URL
                                 * instead of the default page. */
                                if (StringUtils.isNotBlank(TutorUserWebInterface.getExperimentReturnUrl())) {
                                    endMsg.setExperimentReturnUrl(TutorUserWebInterface.getExperimentReturnUrl());
                                }
                                IFrameMessageHandlerChild.getInstance().sendMessage(endMsg);
                            }

                            ExperimentWelcomeWidget startWidget = new ExperimentWelcomeWidget(new WidgetInstance(WidgetTypeEnum.EXPERIMENT_WELCOME_WIDGET, properties));

                            Document.getInstance().fullScreenMode();

                            Document.getInstance().setArticleWidget(startWidget, true);
                        }

                    });
                } else {
                    ExperimentWelcomeWidget startWidget = new ExperimentWelcomeWidget(new WidgetInstance(WidgetTypeEnum.EXPERIMENT_WELCOME_WIDGET, properties));

                    Document.getInstance().fullScreenMode();

                    Document.getInstance().setArticleWidget(startWidget, true);

                }

            } catch (Exception ex) {

                Document.getInstance().displayError("Creating an Inactive Experiment Session", "Exception Caught", ex);
            }

            return instance;

        } else {


            //Document.getInstance().displayDialog("invalidate", "serverProperties -> "+ serverProperties.isExperimentMode());
            if(instance != null){
                loginType = BrowserSession.getLoginType();
            } else if (TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment()){
                loginType = WidgetTypeEnum.LOGO_WIDGET;
            } else {
                loginType = WidgetTypeEnum.SIMPLE_LOGIN_WIDGET;
            }

            instance = null;
            BrowserProperties.getInstance().setUserSessionKey(null);

            try {

                if(BrowserSession.serverProperties.isServerMode() && loginType != WidgetTypeEnum.BLANK_WIDGET) {

                    Frame unavailableFrame = new Frame();
                    unavailableFrame.getElement().getStyle().setProperty("border", "none");
                    String backgroundUrl = BrowserSession.serverProperties.getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
                    unavailableFrame.getElement().getStyle().setBackgroundImage("linear-gradient(transparent, rgba(255,0,0,0.6)), url('"+backgroundUrl+"')");
                    unavailableFrame.setHeight("100%");
                    unavailableFrame.setWidth("100%");
                    unavailableFrame.setUrl(UNAVAILABLE_PAGE_URL);

                    RootLayoutPanel.get().clear();
                    RootLayoutPanel.get().add(unavailableFrame);

                }else if(loginType == WidgetTypeEnum.SIMPLE_LOGIN_WIDGET && BrowserSession.serverProperties.hasRestrictedAccess()){
                    //prevent experiments from being started if the gift instance has restricted access at the moment

                    logger.warning("This GIFT instance has restricted user access.  Please contact your GIFT admin for access.");
                    Frame unavailableFrame = new Frame();
                    unavailableFrame.getElement().getStyle().setProperty("border", "none");
                    String backgroundUrl = BrowserSession.serverProperties.getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
                    unavailableFrame.getElement().getStyle().setBackgroundImage("linear-gradient(transparent, rgba(255,0,0,0.6)), url('"+backgroundUrl+"')");
                    unavailableFrame.setHeight("100%");
                    unavailableFrame.setWidth("100%");
                    unavailableFrame.setUrl(SIMPLE_LOGIN_RESTRICTED_ACCESS_PAGE_URL);

                    RootLayoutPanel.get().clear();
                    RootLayoutPanel.get().add(unavailableFrame);

                } else {
                    Document.getInstance().setArticleWidget(WidgetFactory.createWidgetType(loginType));
                }

            } catch (Exception ex) {

                Document.getInstance().displayError("invalidating the browser session in the tutor client", "an exception was thrown when trying to show the appropriate page", ex);
            }

            logger.info("Finished invalidating browser session.");

            return instance;
        }
    }


    /**
     * Contains a callback used when logging out.
     */
    private static class LogoutCallback implements AsyncCallback<RpcResponse> {

        /** The callback used when logging out */
        private final AsyncCallback<RpcResponse> callback;

        /**
         * Constructor.
         *
         * @param callback the callback used when logging out.
         */
        public LogoutCallback(AsyncCallback<RpcResponse> callback) {
            this.callback = callback;
        }

        @Override
        public void onFailure(Throwable caught) {
            callback.onFailure(caught);
        }

        @Override
        public void onSuccess(RpcResponse result) {
            callback.onSuccess(result);
        }
    }

    /**
     * Ends the user domain session and logs out the user.
     *
     * @param callback - used to call back when the server has responded.
     */
    public void userEndDomainSessionAndLogout( AsyncCallback<RpcResponse> callback) {
        tutorUserInterfaceService.userEndDomainSessionAndLogout(browserSessionKey, new LogoutCallback(callback));

    }

    /**
     * Detects whether or not a resource is reachable from the given URL
     *
     * @param url the resource's URL
     * @param callback a callback invoked once it has been determined whether or not the resource is reachable
     */
    public void isUrlResourceReachable(String url, AsyncCallback<RpcResponse> callback){
        tutorUserInterfaceService.isUrlResourceReachable(url, callback);
    }

    /**
     * Starts the course corresponding to the experiment with the given ID
     *
     * @param experimentId the ID of the experiment
     * @param experimentFolder the folder of the experiment course relative to
     *        the runtime experiment folder.
     * @param callback Callback for when the server responds
     */
    public static void startExperimentCourse(final String experimentId, final String experimentFolder, final AsyncCallback<RpcResponse> callback) {

        tutorUserInterfaceService.startExperimentCourse(
                experimentId,
                experimentFolder,
                new ClientProperties(serverProperties.getWebSocketId(), WebDeviceUtils.getMobileAppProperties()),
                new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                
                if(WebDeviceUtils.isMobileAppEmbedded()) {
                    
                    /* Try connecting again in case the network dropped temporarily */
                    startExperimentCourse(experimentId, experimentFolder, callback);
                    
                } else {
                    callback.onFailure(caught);
                }
                
            }

            @Override
            public void onSuccess(final RpcResponse result) {

                if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
                    logger.info("startExperimentCourse() - browser session created");
                }

                BrowserSessionReadyCallback browserReadyCallback = new BrowserSessionReadyCallback() {

                    @Override
                    public void onSessionSuccess() {
                        instance.domainSessionListening = true;
                        getInstance().addHistoryTag(TutorUserWebInterface.buildUserSessionHistoryTag(result.getUserSessionId()));

                        callback.onSuccess(result);

                    }

                    @Override
                    public void onSessionFailure() {
                        callback.onSuccess(new RpcResponse (null, null, false, "Failed to open a web socket connection to the server."));
                    }

                };

                createInstanceAndAttachSocket(result.getUserSessionId(), result.getBrowserSessionId(), WidgetTypeEnum.EXPERIMENT_WELCOME_WIDGET, browserReadyCallback);

                if (TutorUserWebInterface.isEmbedded()) {
                    IFrameSimpleMessage courseStartingMsg = new IFrameSimpleMessage(IFrameMessageType.COURSE_STARTING);
                    IFrameMessageHandlerChild.getInstance().sendMessage(courseStartingMsg);
            }
            }
        });
    }

    /**
     * Sets the status of the feedback widget on the server. Used to determine whether
     * or not feedback updates will be queued or pushed directly to the client.
     *
     * @param isActive Whether or not the feedback widget is active.
     * @param callback The callback to execute after the status update.
     */
    public static void setFeedbackWidgetIsActive(boolean isActive, AsyncCallback<RpcResponse> callback) {
        if(instance != null) {
            tutorUserInterfaceService.setFeedbackWidgetIsActive(instance.browserSessionKey, isActive, callback);
        }
    }

    /**
     * Sets the id of the active conversation widget on the server. Used to determine whether
     * or not chat updates will be queued or pushed directly to the client.
     *
     * @param chatId The unique id of the conversation.
     * @param callback The callback to execute after the status update.
     */
    public static void setActiveConversationWidget(int chatId, AsyncCallback<RpcResponse> callback) {
        if(instance != null) {
            tutorUserInterfaceService.setActiveConversationWidget(instance.browserSessionKey, chatId, callback);
        }
    }

    /**
     * Sets the status of the conversation widget on the server to inactive. Used to determine
     * whether or not chat updates will be queued or pushed directly to the client.
     *
     * @param inactiveConversationId The unique id of the conversation (created by the server) that the client would like to notify
     * the server about being idle.  An inactive conversation means that the server should NOT deliver updates to
     * the chat until it becomes active again.  The client would need to notify the server if the conversation
     * becomes active again.
     */
    public static void setConversationWidgetInactive(int inactiveConversationId) {
        if(instance != null) {
            tutorUserInterfaceService.setInactiveConversationWidget(inactiveConversationId, instance.browserSessionKey, new AsyncCallback<RpcResponse>() {

                @Override
                public void onFailure(Throwable thrown) {
                    logger.warning("Failed to change the chat widget state on the server: " + thrown.toString());
                }

                @Override
                public void onSuccess(RpcResponse result) {
                    // Nothing to do
                }

            });
        }
    }

    /**
     * Pulls updates from the server for the active chat widget.
     *
     * @param callback The callback to execute once the update has been handled.
     */
    public static void dequeueChatWidgetUpdate(AsyncCallback<RpcResponse> callback) {
        if (instance != null) {
            tutorUserInterfaceService.dequeueChatWidgetUpdate(instance.browserSessionKey, callback);
        }

    }

    /**
     * Pulls updates from the server for the active feedback widget.
     *
     * @param callback The callback to execute once the update has been handled. The response success value will be true
     * when a feedback was sent to the client as a result of this request to dequeue.  False is an indication
     * that the feedback is most likely queued because the server considers the avatar to be idle.
     */
    public static void dequeueFeedbackWidgetUpdate(AsyncCallback<RpcResponse> callback) {
        if (instance != null) {
            tutorUserInterfaceService.dequeueFeedbackWidgetUpdate(instance.browserSessionKey, callback);
        }

    }

    /**
     * Disconnects the websocket from the server (closes it).  This can be used in cases such as experiment mode
     * when the course is completed to disconnect the client from the server.
     *
     * Typically this is not needed to be called since the server usually disconnects closes the socket when the course is done.
     */
    public void disconnectWebSocket() {
        cleanupWebSocket();
    }

    /**
     * Sets the status of the tutor actions widget on the server to active or inactive.
     *
     * @param isAvailable True if the tutor actions widget is available on the client, false otherwise.
     */
    public static void setTutorActionsAvailable(boolean isAvailable) {
        if(instance != null) {
            tutorUserInterfaceService.setTutorActionsAvailable(instance.browserSessionKey, isAvailable, new AsyncCallback<RpcResponse>() {

                @Override
                public void onFailure(Throwable thrown) {
                    logger.warning("Failed to update the Tutor Actions Widget status on the server: " + thrown);
                }

                @Override
                public void onSuccess(RpcResponse response) {
                    // nothing to do
                }

            });
        }
    }

    /**
     * Sends the embedded app state message.
     *
     * @param message the message to send.
     */
    public void sendEmbeddedAppState(String message) {
        tutorUserInterfaceService.sendEmbeddedAppState(message, browserSessionKey, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable arg0) {
                //TODO: Log failure
            }

            @Override
            public void onSuccess(RpcResponse arg0) {
                // nothing to do
            }
        });
    }

    /**
     * Notifies the Domain that the learner wants to manually stop the current training application scenario
     */
    public void stopTrainingAppScenario() {

        tutorUserInterfaceService.stopTrainingAppScenario(browserSessionKey, new AsyncCallback<GenericRpcResponse<Void>>() {

            @Override
            public void onSuccess(GenericRpcResponse<Void> result) {

               if(!result.getWasSuccessful()) {

                   if(result.getException() != null) {
                       Document.getInstance().displayErrorDialog(
                               null,
                               result.getException().getMessage(),
                               result.getException().getDetails());
                   }
               }
            }

            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Finishing Scenario", caught);
            }
        });
    }

    /**
     * Gets the survey with the specified key in the specified context
     * @param surveyContextId the survey context to query for the specified survey
     * @param giftKey the survey key that identifies the survey to return
     * @param callback the callback to invoke when the survey is returned
     */
    public void getSurvey(int surveyContextId, String giftKey, AsyncCallback<SurveyGiftData> callback) {
        tutorUserInterfaceService.getSurvey(browserSessionKey, surveyContextId, giftKey, callback);
    }

    /**
     * Builds the encrypted OAuth URL that will be used to send the request to the LTI provider.
     *
     * @param rawUrl the raw media url before it has been protected by OAuth.
     * @param properties The LtiProperties associated with the content
     * @param callback the callback used to handle the response or catch any failures.
     */
    public void buildOAuthLtiUrl(String rawUrl, LtiProperties properties, AsyncCallback<RpcResponse> callback) {
        tutorUserInterfaceService.buildOAuthLtiUrl(rawUrl, properties, browserSessionKey, callback);
    }

    /**
     * Cleans up the websocket from the browser session.
     */
    private void cleanupWebSocket() {

        if (readyCallback != null) {
            readyCallback.onSessionFailure();
            readyCallback = null;
        }

        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }
    }

    /**
     * Attempts to re-establish this browser session's connection with the server by creating a new web socket and re-attempting
     * to connect to it. This method will continually attempt to recreate the web socket connection every few seconds and will
     * resume running the course normally once the connection has been re-established. While the web socket is reconnecting, a
     * dialog will be displayed to block user input and let the user know that the client is reconnecting.
     */
    private void reconnectWebSocket() {

        if(logger.isLoggable(Level.INFO)) {
            logger.info("The web socket connection has been closed unexpectedly. Attempting to re-establish socket connection.");
        }

        //the web socket connection was closed unexpectedly for a mobile device, so prepare to reconnect
        final AsyncCallback<Boolean> connectCallback = new AsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("The web socket connection has been re-established.");
                }
            }

            @Override
            public void onFailure(Throwable caught) {

                logger.warning("Failed to re-establish web socket connection. " + caught.toString());

                //attempt to reconnect again if this attempt fails
                reconnectWebSocket();
            }
        };

        //handle when the connection is opened so that this client can re-sync with the server
        readyCallback = new BrowserSessionReadyCallback() {

            @Override
            public void onSessionSuccess() {

                readyCallback = null;
                
                int timeout = WebDeviceUtils.isMobileAppEmbedded() 
                        ? TutorWebSocketUtils.MOBILE_SOCKET_TIMEOUT_MS 
                        : TutorWebSocketUtils.DEFAULT_SOCKET_TIMEOUT_MS; 

                if(System.currentTimeMillis() - reconnectStartTime < timeout) {

                    if(logger.isLoggable(Level.INFO)) {
                        logger.info("The re-established web socket connection has been opened. Attempting to sync with server.");
                    }

                    reconnectStartTime = null;
                    isResynchronizing = true;

                    // actions may have been missed while the connection was being re-established, so sync up with the server
                    instance.sendActionToServer(new SynchronizeClientStateAction(), null);
                    webSocket.setActionsDesynched(false);

                } else {
                    
                    // the connection was re-established after this client's sessions on the server were cleaned up, so leave
                    // the current session after displaying an informative dialog, since it is no longer valid
                    final ModalDialogBox leaveDialog = new ModalDialogBox();
                    leaveDialog.setGlassEnabled(true);
                    leaveDialog.setText("Course has ended");

                    FlowPanel messagePanel = new FlowPanel();

                    HTML message = new HTML("Your session was closed after not being connected to the tutor for an extended period of time."
                            + "<br/><br/>Now that you are back, feel free to start the course over again or choose a new course to take.");
                    message.getElement().getStyle().setProperty("margin-bottom", "10px");
                    messagePanel.add(message);

                    leaveDialog.setWidget(messagePanel);
                    
                    final Button closeButton = new Button("Close");
                    closeButton.setType(ButtonType.PRIMARY);
                    closeButton.addClickHandler(new ClickHandler() {
                        
                        @Override
                        public void onClick(ClickEvent event) {
                            
                            closeButton.setEnabled(false);
                            closeButton.setIcon(IconType.COG);
                            closeButton.setIconSpin(true);
                            
                            //leave the current session once the dialog's close button is pressed
                            String experimentReturnUrl = TutorUserWebInterface.getExperimentReturnUrl();

                            if (TutorUserWebInterface.isEmbedded()) {
                                
                                //if this tutor window is embedded in the dashboard, we need to tell the dashboard where to go next
                                //(by default, ending the course will return the dashboard to the Take a Course page)
                                EndCourseMessage endCourseMessage = new EndCourseMessage();
                                
                                if (StringUtils.isNotBlank(experimentReturnUrl)) {
                                    
                                    //if the dashboard provided a return URL for a course collection, return to that URL
                                    endCourseMessage.setExperimentReturnUrl(experimentReturnUrl);
                                    
                                } else if(TutorUserWebInterface.isExperiment()) {
                                    
                                    //if this tutor window is running an experiment embedded in the dashboard, reload 
                                    //the dashboard to return it to the experiment start page
                                    endCourseMessage.setShouldReload(true);
                                }
                                
                                IFrameMessageHandlerChild.getInstance().sendMessage(endCourseMessage);
                                
                            } else {
                                
                                //return this tutor window to the login page
                                invalidate();
                                
                                leaveDialog.hide(); //hide the dialog so the user can interact with the TUI again
                            }
                        }
                    });
                    leaveDialog.setFooterWidget(closeButton);
                
                    leaveDialog.center();
                }

                if(reconnectDialog != null) {

                    //hide the reconnect dialog so that the learner can interact with the page again
                    reconnectDialog.hide();
                }
            }

            @Override
            public void onSessionFailure() {

                readyCallback = null;

                connectCallback.onFailure(new Exception("The browser session was not ready"));
            }
        };

        if(reconnectDialog == null) {

            //create a dialog telling the learner that the TUI is attempting to reconnect to the server
            reconnectDialog = new ModalDialogBox();
            reconnectDialog.setGlassEnabled(true);
            reconnectDialog.setCloseable(false);
            reconnectDialog.setText("Reconnecting");

            FlowPanel messagePanel = new FlowPanel();
            messagePanel.getElement().getStyle().setProperty("text-align", "center");

            HTML message = new HTML("The connection with the server has been interrupted. Attempting to reconnect.");
            message.getElement().getStyle().setProperty("margin-bottom", "10px");
            messagePanel.add(message);

            BsLoadingIcon reconnectIcon = new BsLoadingIcon();
            reconnectIcon.setSize(IconSize.TIMES4);
            reconnectIcon.startLoading();
            messagePanel.add(reconnectIcon);

            reconnectDialog.setWidget(messagePanel);
        }

        //display a dialog while the connection is being re-established to prevent learner interaction that could cause
        //errors while the server connection is down.
        reconnectDialog.center();

        if(reconnectStartTime == null) {

            //track when this browser session starts attempting to reconnect so we can determine how long it takes
            reconnectStartTime = System.currentTimeMillis();
        }

        //attempt to re-establish the web socket connection
        establishWebSocketConnection(connectCallback);
    }

    @Override
    public void onSocketClosed() {

        if(webSocket != null) {

            //the web socket has not yet been cleaned up, so see if it needs to clean up or reconnect
            if(webSocket.isClientNormalCloseRequest()) {

                //if the current device is non-mobile OR if the current web socket is closing normally, clean up the current web socket
                cleanupWebSocket();

            } else {

                //if the client is closing abnormally, attempt to reconnect a web socket with the server
                reconnectWebSocket();
            }
        }
    }

    @Override
    public void onSocketOpened() {

        // Execute any delayed logic (if specified) once the web socket is opened.
        if (readyCallback != null) {
            readyCallback.onSessionSuccess();
            readyCallback = null;
        }

    }

    @Override
    public void onJavaScriptException(JavaScriptException ex) {
        // no implementation needed here, this is already handled in the tutor where the websocket is constructed.
    }

}
