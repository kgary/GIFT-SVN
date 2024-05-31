/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.experiment.CourseCollection;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerParent;
import mil.arl.gift.common.gwt.client.IFrameMessageListener;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.iframe.IFrameOrigin;
import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.ApplicationEventMessage;
import mil.arl.gift.common.gwt.client.widgets.AbstractWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsPageLoadErrorWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.shared.ExperimentParameters;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.io.ExperimentUrlManager;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsCourseHeaderWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsCourseRuntimeWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogRenameWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogSharedPermissionsWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogType;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsHeaderWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsLoginWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsLoginWidgetCloud;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsMyCoursesDashWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsMyStatsWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsMyToolsWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.DashboardErrorWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.ErrorDetailsDialogWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.ExperimentToolWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.ExperimentWelcomePageWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.LoadCourseParameters;
import mil.arl.gift.tools.dashboard.client.bootstrap.LtiConsumerEndWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.LtiConsumerStartWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.LtiCourseRuntimeWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.coursecollection.CourseCollectionPage;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.BsGameMasterPanel;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.WebMonitorPanel;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.WebMonitorPanel.MonitorMode;
import mil.arl.gift.tools.dashboard.client.websocket.ResumeSessionHandler;
import mil.arl.gift.tools.dashboard.shared.PageLoadError;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.LoginResponse;


/**
 * The UiManager Singleton class is responsible for controlling the requests to transition to different screens, as well
 * as determining the layout and building the overall rootpanel with the widgets needed to display a screen.  Additionally
 * the UiManager handles setting up the cross-domain browser communication via iFrames.
 *
 * It is expected that this class be initialized/created at some point during the module entry point (onLoad()) method.
 *
 * @author nblomberg
 *
 */
public class UiManager {

    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(UiManager.class.getName());

    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);

    /** instance of the dashboard singleton object. */
    private static UiManager instance = null;

    public static final String INVALID_SESSION = "";
    /** The client user session id */
    private String sessionId = INVALID_SESSION;
    
    /** 
     * A number of pixels to subtract from any window resize operations to account for issues with subpixels. 
     * On some displays, we've seen issues where the main widget in this UiManager takes up a single extra pixel due to
     * rounding errors when the total screen size has a decimal number of pixels. This can cause the UI to repeatedly 
     * "bounce" because the resize events use integers and repeatedly round up and down to the next closest integers.
     * 
     * Subtracting enough pixels to adjust for rounding issues helps deal with this. This number should generally be
     * kept as low as possible without being 0 to avoid leaving too much of a gap at the bottom of the UI.
     */
    final int SUBPIXEL_RESIZE_ADJUSTMENT = 1;

    /** The client username */
    private String userName = "";
    /** The client password */
    private String userPass = "";

    /**
     * whether the dashboard is in debug mode which allows for logic such as validate all course
     * tiles, color code survey responses based on scoring.
     */
    private boolean debugMode = false;

    private String actualUserName = null;
    /** The client id */
    private int userId = 0;

    /** name of the currently course being taken in the TUI, as loaded by the dashboard.  Can be null.*/
    private String currentCourseName;

    /** The origin of the tui. */
    private String tuiOrigin = "";

    /** The iFrame id that is assigned to the tui. */
    private final String tuiFrameId = "__tuiFrame";

    /** The origin of the gat. */
    private String gatOrigin = "";

    /** The iFrame id that is assigned to the gat. */
    private final String gatFrameId = "__gatFrame";

    private AbstractWidget subPanel = null;
    private AbstractWidget dashNavbar = null;

    private BsHeaderWidget navBar = null;
    private BsDialogWidget dialogWidget = null;
    private BsDialogRenameWidget renameWidget = null;
    private BsDialogConfirmWidget confirmWidget = null;
    private BsDialogSharedPermissionsWidget sharedPermissionsWidget = null;
    private ErrorDetailsDialogWidget detailedErrorWidget = null;

    private TuiMessageListener tuiListener = null;

    /** A message listener used to handle window events from an iframe created by a surrounding application (i.e. the mobile app) */
    private IFrameMessageListener applicationFrameListener = new IFrameMessageListener() {

        @Override
        public boolean handleMessage(AbstractIFrameMessage message) {

            if (message instanceof ApplicationEventMessage) {

                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Passing app event message to tutor frame : " + message.toString());
                }

                //if the mobile application is notifying the TUI of an event, pass the message through the dashboard
                IFrameMessageHandlerParent.getInstance().sendMessage(message, ChildIFrameConsts.TUI_IFRAME_KEY);
            }

            return true;
        }
    };

    private boolean offlineMode = false;

    /** Flag to indicate that the embedded tui is ready, which means the user can start the course. */
    private boolean tuiReady = false;

    /**
     * Timer checks if the course instruction init page has loaded properly,
     * which is used to determine whether the Tutor client is not responding to
     * the Dashboard's request to start a course. This can be used to detect
     * scenarios such as when the Tutor URL is not configured correctly which
     * can cause the course starting screen to remain indefinitely
     */
    private Timer tuiIFrameTimer = null;

    /** Enums for the reasons that a course is ended */
    public enum EndCourseReason {
        /** User stopped the course. */
        USER_STOPPED,
        /** User signed out */
        USER_SIGNEDOUT,
        /** The TUI ended the course (default reason) */
        COURSE_COMPLETED,
     }

    /** Cached reason that a course was ended. */
    private EndCourseReason endCourseReason = EndCourseReason.COURSE_COMPLETED;

    /** Store the current state of the UiManager in terms of what screen/mode it is in. */
    private ScreenEnum currentState = ScreenEnum.INVALID;

    /**
     * Timer to wait for the TUI to send the END_COURSE message.
     * A sort of handshake happens between the dashboard and tui when the user gracefully, but prematurely,
     * ends a course.  First the dashboard starts to clean up, then sends a notification to the tui.  Second the TUI
     * replies with a message which then triggers additional clean up logic on the dashboard.  This timer is
     * to make sure the TUI sends its message and if it doesn't the dashboard isn't waiting forever.
     */
    private Timer endCourseTimer = null;

    /** The deployment mode */
    private DeploymentModeEnum deploymentMode;

    /** Time (in milliseconds) to wait for the TUI to close and send the END_COURSE message.  If the message is not received in this amount of time, the dashboard will forcefully close the course. */
    private static final int ENDCOURSETIMER_MS = 10000;

    /** properties from the server, available after init method is called */
    private ServerProperties serverProperties = null;

    @SuppressWarnings("unused")
    private static final String LEAVE_COURSE_RUNTIME_WARNING = "You are about to leave the course before it is completed.  If you leave the page, all progress in the course will be lost.";
    private static final String LEAVE_GAT_WARNING = "You are about to leave the course authoring tool.  If you have any unsaved changes, please save them first.";

    // The window closing handler (to respond to page reloads/window closing).
    private Window.ClosingHandler windowClosingHandler = new Window.ClosingHandler() {

        @Override
        public void onWindowClosing(ClosingEvent event) {


            event = getInstance().showLeavePageWarning(event);

        }

    };

    /**
     * Singleton constructor
     */
    private UiManager() {

        logger.fine("Creating ui manager interface.");

        subPanel = null;
        dashNavbar = null;

        // The navbar instance remains throughout the logged in session.  It should
        // not be recreated each screen transition.
        navBar = new BsHeaderWidget();


        dialogWidget = new BsDialogWidget();
        renameWidget = new BsDialogRenameWidget();
        confirmWidget = new BsDialogConfirmWidget();
        detailedErrorWidget = new ErrorDetailsDialogWidget();
        sharedPermissionsWidget = new BsDialogSharedPermissionsWidget();

        // Clear the document, but don't build anything yet.
        RootPanel document = RootPanel.get();
        document.clear();

        // set default visibility of the navbar.
        navBar.setVisible(false);

        tuiReady = false;
    }



    /**
     * Initializes the UIManager based on the properties that are sent from the server.
     * For example, the tui host origin is configured from the server, and is sent to the client.
     *
     * @param properties - The properties retrieved from the server.
     */
    public void init(ServerProperties properties) {

        this.serverProperties = properties;

        sharedPermissionsWidget.init(serverProperties);

        tuiOrigin = properties.getPropertyValue(ServerProperties.TUI_URL);

        if (tuiOrigin == null || tuiOrigin.isEmpty()) {
            logger.severe("TUI Origin could not be found in the server properties.  IFrame communication with the TUI will fail.");
        } else {
            logger.fine("UiManager will connect to TUI at: " + tuiOrigin);
        }

        gatOrigin = GWT.getHostPageBaseURL();

        if (gatOrigin == null || gatOrigin.isEmpty()) {
            logger.severe("GAT Origin could not be found in the server properties.  IFrame communication with the GAT will fail.");
        } else {
            logger.fine("UiManager will connect to GAT at: " + gatOrigin);
        }

        deploymentMode = properties.getDeploymentMode();

        // adjust the game master visibility accordingly
        navBar.updateHeaderConfiguration();

        ArrayList<IFrameOrigin> originList = new ArrayList<IFrameOrigin>();

        IFrameOrigin tuiIFrame = new IFrameOrigin();
        tuiIFrame.setOriginKey(ChildIFrameConsts.TUI_IFRAME_KEY);
        tuiIFrame.setOriginUrlFromFullUrl(tuiOrigin);
        tuiIFrame.setIFrameId(tuiFrameId);

        IFrameOrigin gatIFrame = new IFrameOrigin();
        gatIFrame.setOriginKey(ChildIFrameConsts.GAT_IFRAME_KEY);
        gatIFrame.setOriginUrlFromFullUrl(gatOrigin);
        gatIFrame.setIFrameId(gatFrameId);

        originList.add(tuiIFrame);
        originList.add(gatIFrame);

        IFrameMessageHandlerParent.getInstance().init(originList); //handle messages from the TUI and GAT
        IFrameMessageHandlerChild.getInstance().init(); //handle messages from the mobile app

        // Create an instance of the history manager.
        HistoryManager.getInstance();

        // Add the window closing handler.
        Window.addWindowClosingHandler(windowClosingHandler);

        Window.setTitle(properties.getPropertyValue(ServerProperties.WINDOW_TITLE));

        // Prefetch any known images ahead of time.
        preFetchDefaultImages();
    }

    /**
     * Accessor to retrieve the server side property of wheter the cloud login page should be used
     * instead of the normal login page.  This flag should only be set for the cloud GIFT server configuration.
     *
     * @return - true if the cloud login page should be shown, false otherwise.
     */
    public boolean getUseCloudLoginPage() {
        boolean useCloudLoginPage = false;
        String useCloudLoginValue = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.USE_CLOUDLOGINPAGE);
        logger.fine("useCloudLoginPage = " + useCloudLoginValue);
        if (!useCloudLoginValue.isEmpty()) {
            useCloudLoginPage = Boolean.parseBoolean(useCloudLoginValue);
        }

        return useCloudLoginPage;
    }

    /**
     * Function to allow prefetching of common images that the dashboard may need to display in the future.
     *
     */
    private void preFetchDefaultImages() {

        Image.prefetch(DomainOption.COURSE_DEFAULT_IMAGE);
        Image.prefetch(DomainOption.COURSE_TYPE_RECOMMENDED);
        Image.prefetch(DomainOption.COURSE_TYPE_REFRESHER);
        Image.prefetch(DomainOption.COURSE_TYPE_INVALID);
        Image.prefetch(DomainOption.COURSE_TYPE_WARNING);
        Image.prefetch(DomainOption.FILE_NOT_FOUND_IMAGE);

        if(serverProperties != null){
            String loginBackgroundImg = serverProperties.getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
            if(StringUtils.isNotBlank(loginBackgroundImg)){
                Image.prefetch(loginBackgroundImg);
            }
        }
    }

    /**
     * Accessor to the dashboard singleton object.  If it doesn't exist yet
     * it will be created.
     * @return Dashboard - instance to the dashboard singleton object.
     */
    public static UiManager getInstance() {
        if (instance == null) {
            instance = new UiManager();
        }

        return instance;

    }
    
    /**
     * Gets whether or not GIFT's current lesson level is set to Real-Time Assessment (RTA)
     *  
     * @return whether RTA is set as GIFT's current lesson level
     */
    public boolean isRtaLessonLevel() {
        if (LessonLevelEnum.RTA.equals(serverProperties.getLessonLevel())) {
            return true;
        }
        
        return false;
    }

    /**
     * Accessor to set the browser session id of the client.
     * @param id - The browser session id.
     */
    public void setSessionId(String id) {
        logger.fine("Setting session id: " + id);
        sessionId = id;
    }

    /**
     * Accessor to retrieve the browser session id of the client.
     * @return String - return the browser session id of the client.  Can be an empty string.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Accessor to retrieve the user name that is logged in on the client.
     * @return String - the name of the logged in user.  Can be an empty string.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Accessor to retrieve the read only user constant
     * @return String - the read only user constant set in common.properties
     */
    public String getReadOnlyUser() {
        return serverProperties.getReadOnlyUser();
    }
    
    /**
     * Gets the maximum number of messages that should be displayed at a time
     * by the web monitor. Default value is 1000. 
     * @return the maximum message buffer display size
     */
    public int getMessageDisplayBufferSize() {
        return serverProperties.getMessageDisplayBufferSize();
    }

    /**
     * Return whether the dashboard is in debug mode which allows for logic such as validate all course
     * tiles, color code survey responses based on scoring.
     *
     * @return true if in debug mode
     */
    public boolean isDebugMode(){
        return debugMode;
    }

    /**
     * Accessor to retrieve the user name that is logged in on the client. When using 'login as'
     * this is the username of the profile being accessed (e.g. Admin A logins as Person B; actual
     * username is B).
     *
     * @return String - the name of the logged in user. Can be an empty string.
     */
    public String getActualUserName() {
        return actualUserName;
    }

    /**
     * Accessor to retrieve the user password that is logged on the client.
     * @return String - the password for the user.
     */
    public String getUserPassword() {
        return userPass;
    }

    /**
     * Accessor to retrieve the user id that is logged on the client.
     * @return the user id
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Accessor to retrieve the tui origin used for cross-domain communication.
     *
     * @return String - the tui origin (should be in form of: "http://www.something.com:8888")
     */
    public String getTuiOrigin() {
        return tuiOrigin;
    }

    /**
     * Accessor to retreive the tui iFrame id that is used for cross-domain communication.
     *
     * @return String - the tui iFrame id.
     */
    public String getTuiFrameId() {
        return tuiFrameId;
    }

    /**
     * Accessor to retreive the gat iFrame id that is used for cross-domain communication.
     *
     * @return String - the gat iFrame id.
     */
    public String getGatFrameId() {
        return gatFrameId;
    }

    /**
     * Accessor to set the name of the logged in user on the client.
     *
     * @param name - Name of the user that is logged in.
     */
    public void setUserName(String name) {
        userName = name;
    }

    /**
     * Set whether the dashboard is in debug mode which allows for logic such as validate all course
     * tiles, color code survey responses based on scoring.
     *
     * @param debug true if the user successfully entered the dashboard in debug mode
     */
    public void setDebugMode(boolean debug){
        this.debugMode = debug;

        // adjust the game master visibility accordingly
        navBar.updateHeaderConfiguration();
    }

    /**
     * Accessor to set the name of the logged in user on the client.
     *
     * @param name - Name of the user that is logged in. When using 'login as' this is the username
     *        of the profile being accessed (e.g. Admin A logins as Person B; actual username is B).
     */
    public void setActualUserName(String name) {
        actualUserName = name;
    }

    /**
     * Sets the user password that was retrieved from the server.
     *
     * @param password - Password of the user that was retreived from the server.
     */
    public void setUserPassword(String password) {
        userPass = password;
    }

    /**
     * Sets the user id that was retrieved from the server.
     *
     * @param id - id of the user that was retrieved from the server.
     */
    public void setUserId(int id) {
        userId = id;
    }

    /**
     * Set the name of the currently running course for this client.
     *
     * @param courseName can be null (but not empty) if not running a course.
     */
    public void setCurrentCourseName(String courseName){

        if(courseName != null && courseName.isEmpty()){
            throw new IllegalArgumentException("The course name can't be empty.");
        }

        this.currentCourseName = courseName;
    }

    /**
     * Get the name of the currently running course in the tutor frame.
     *
     * @return the course name.  Can be null (but not empty) if not running a course.
     */
    public String getCurrentCourseName(){
        return currentCourseName;
    }

    /**
     * Sets offline mode for the user session
     *
     * @param offline - whether the user is offline or not
     */
    public void setOfflineMode(boolean offline) {
        offlineMode = offline;
    }

    /**
     * Switches the UI to a new 'screen'.  In order to do this the server is called
     * with the new state first, and if that succeeds, we populate the client UI.  if the
     * state cannot be updated on the server, the user is returned to the loginscreen.
     *
     * @param state - The screen enum that should be transitioned to.
     * @param data - (optional) Any optional data that may need to be sent to the screen during the transition.
     */
    public void displayScreen(ScreenEnum state, Object data) {

       logger.fine("Request to transition to screen: " + state);

       ScreenAsyncCallback callback = new ScreenAsyncCallback();
       callback.setScreenState(state);
       callback.setScreenData(data);

       if (state == ScreenEnum.LOGIN) {
           // Login screen should always show, we don't need to update the screen state on the server for the login page.
           displayLoginScreen();
       } else if (state == ScreenEnum.PAGELOAD_ERROR){
           internalDisplayScreen(state, data);
       } else if (state == ScreenEnum.LTI_CONSUMER_START_PAGE){
           internalDisplayScreen(state, data);
       }  else if (state == ScreenEnum.LTI_CONSUMER_END_PAGE){
           internalDisplayScreen(state, data);
       }  else if (state == ScreenEnum.DASHBOARD_ERROR_PAGE){
           internalDisplayScreen(state, data);
       } else if (state == ScreenEnum.LTI_COURSE_RUNTIME){
           internalDisplayScreen(state, data);
        } else if (state == ScreenEnum.COURSE_COLLECTION) {
            internalDisplayScreen(state, data);
        } else if (state == ScreenEnum.EXPERIMENT_WELCOME_PAGE) {
            internalDisplayScreen(state, data);
        } else if (state == ScreenEnum.EXPERIMENT_RUNTIME) {
            internalDisplayScreen(state, data);
        } else {

           // Call onDetaching() method to signal that the old screens are going to be unloaded.  This gives
           // the ui an opportunity to cancel any outstanding rpcs to allow a quicker transition.
           notifyPreDetach();
           logger.fine("Calling updatestate with session id: " + UiManager.getInstance().getSessionId());
           dashboardService.updateUserState(UiManager.getInstance().getSessionId(),  state, callback);
       }
    }

    /**
     * Notifies the existing UI elements that they will be detached.
     */
    private void notifyPreDetach() {
        logger.info("notifyPreDetach()");

        if (dashNavbar != null) {
            dashNavbar.onPreDetach();
        }

        if (subPanel != null) {
            subPanel.onPreDetach();
        }

    }

    /**
     * Switches the UI to a new 'screen'.  In order to do this the server is called
     * with the new state first, and if that succeeds, we populate the client UI.  if the
     * state cannot be updated on the server, the user is returned to the loginscreen.
     *
     * @param state - The screen enum that should be transitioned to.
     */
    public void displayScreen(ScreenEnum state) {
        displayScreen(state, "");
    }

    /**
     * Locks all of the navigation tabs. This will prevent the user from navigation away from the current screen.
     */
    public void lockNavTabs(){
        navBar.disable();
    }

    /**
     * Unlocks the navigation tabs
     */
    public void unlockNavTabs() {
        navBar.enable();
    }

    /**
     * Get the current screen state of the UiManager.
     *
     * @return ScreenEnum The current screen state of the UiManager.  Can be invalid during initial page loading.
     */
    public ScreenEnum getScreenState() {
        return currentState;
    }

    /**
     * An internal function used to display a screen.  This is expected to be called after
     * the server has been called to update the screen state and returned with a response.
     *
     * @param state - The screen enum that should be transitioned to.
     * @param data - (optional) Any optional data that may need to be sent to the screen during the transition.
     */
    private void internalDisplayScreen(ScreenEnum state, Object data) {
        currentState = state;
        switch (state) {
         case LOGIN:
             displayLoginScreen();
             break;
         case MYCOURSES:
             displayMyCoursesScreen();
             break;
         case LEARNER_PROFILE:
             displayMyStatsScreen();
             break;
         case COURSE_CREATOR:
             displayMyToolsScreen(data);
             break;
         case MY_RESEARCH:
             displayMyExperimentsScreen(data);
             break;
         case COURSE_RUNTIME:
             displayCourseRuntimeScreen(data);
             break;
         case PAGELOAD_ERROR:
             displayPageLoadErrorScreen(data);
             break;
         case LTI_CONSUMER_START_PAGE:
             displayLtiConsumerStartScreen(data);
             break;
         case LTI_CONSUMER_END_PAGE:
             displayLtiConsumerEndScreen(data);
             break;
         case DASHBOARD_ERROR_PAGE:
             displayErrorScreen(data);
             break;
         case LTI_COURSE_RUNTIME:
             displayLtiCourseRuntimeScreen(data);
             break;
         case GAME_MASTER_ACTIVE:
             displayGameMasterPanelScreen(data, true);
             break;
         case GAME_MASTER_PAST:
             displayGameMasterPanelScreen(data, false);
             break;
         case WEB_MONITOR_STATUS:
             displayWebMonitorScreen(data, MonitorMode.STATUS);
             break;
         case WEB_MONITOR_MESSAGE:
             displayWebMonitorScreen(data, MonitorMode.MESSAGE);
             break;
        case COURSE_COLLECTION:
            displayCourseCollectionScreen(data);
             break;
         case EXPERIMENT_WELCOME_PAGE:
             displayExperimentWelcomePage(data);
             break;
         case EXPERIMENT_RUNTIME:
             displayExperimentRuntimeScreen(data);
             break;
         default:
             currentState = ScreenEnum.LOGIN;
             logger.severe("Unhandled request to go to screen " + state + ". Displaying the login screen instead.");
             displayLoginScreen();
             break;
         }


    }

    /**
     * Shows or hides the embedded tui (course runtime) by setting the visibility of the iframe.
     *
     * @param show whether or not to show the embedded tui
     */
    public void showEmbeddedTui(boolean show) {
        if (subPanel instanceof BsCourseRuntimeWidget) {
            ((BsCourseRuntimeWidget) subPanel).setIFrameVisibility(show);
        }
    }

    /**
     * Accessor to determine if the embedded tui is showing.
     *
     * @return True if the embedded tui is showing, false otherwise.
     */
    public boolean isEmbeddedTuiShowing() {
        boolean isShowing = false;

        if (subPanel instanceof BsCourseRuntimeWidget) {
            isShowing = ((BsCourseRuntimeWidget) subPanel).getIFrameVisibility();
        }

        return isShowing;
    }

    /**
     * Start the course ending processes.  Notify any necessary widget that the course is ending
     * and schedule the timer to wait for the TUI to send the END_COURSE message.
     *
     * @param reason - Reason that the course is ending.
     */
    public void onCourseEnding(EndCourseReason reason) {

        logger.fine("onCourseEnding called with reason:" + reason);
        endCourseReason = reason;

        if (dashNavbar instanceof BsCourseHeaderWidget) {
            ((BsCourseHeaderWidget) dashNavbar).handleCourseEnding();
        }

        // We need to display a 'please wait' message since we wait for the TUI
        // to shutdown properly.  We also need to start a hard timer in the event that the TUI
        // doesn't respond.

        if (subPanel instanceof BsCourseRuntimeWidget) {
            ((BsCourseRuntimeWidget) subPanel).handleCourseEnding();
        }

        cancelTutorIFrameTimer();

        scheduleEndCourseTimer(reason);
    }

    /**
     * Return whether the end course timer is currently running.
     *
     * @return true if the end course timer is running
     */
    public boolean isEndCourseTimerRunning(){
        return endCourseTimer != null && endCourseTimer.isRunning();
    }

    /**
     * The experiment has ended. Clean up timers.
     */
    public void onExperimentEnded() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onExperimentEnded()");
        }

        /* Cancel the end course timer */
        if (endCourseTimer != null) {
            endCourseTimer.cancel();

            endCourseTimer = null;
        }

        cancelTutorIFrameTimer();
    }

    /**
     * The course has ended.  The TUI has sent the END_COURSE message.  In this case,
     * the UIManager will notify any required widget with the handleCourseEnded method that
     * the course has ended.  The endcourse timer is also cancelled since we got the message
     * successfully from the TUI.
     */
    public void onCourseEnded() {

        logger.fine("onCourseEnded called with reason:" + endCourseReason);

        // Cancel the end course timer.
        if (endCourseTimer != null) {
            endCourseTimer.cancel();

            endCourseTimer = null;
        }

        cancelTutorIFrameTimer();


        if (dashNavbar != null && dashNavbar instanceof BsCourseHeaderWidget) {
            ((BsCourseHeaderWidget) dashNavbar).handleCourseEnded(endCourseReason);
        }

        // We need to display a 'please wait' message since we wait for the TUI
        // to shutdown properly.  We also need to start a hard timer in the event that the TUI
        // doesn't respond.

        if (subPanel != null && subPanel instanceof BsCourseRuntimeWidget) {
            ((BsCourseRuntimeWidget) subPanel).handleCourseEnded(endCourseReason);
        }

        setCurrentCourseName(null);

        resetEndCourseReason();
    }


    /**
     * Reset the cached end course reason.  This should be called after the end course
     * process has completed.  Currently the value is reset back to the default reason.
     */
    private void resetEndCourseReason() {
        endCourseReason = EndCourseReason.COURSE_COMPLETED;
    }

    /**
     * Schedule the end course timer.
     * This timer is to make sure the TUI sends its message to the dashboard indicating that the tutor module
     * is ready to end the course.  If the TUI never sends the message this timer makes sure the dashboard
     * isn't stuck forever, waiting to end a course.
     *
     * @param reason - The reason that the course is ending.
     */
    private void scheduleEndCourseTimer(final EndCourseReason reason) {


        endCourseTimer = new Timer() {

            @Override
            public void run() {

                logger.fine("scheduleEndCourseTimer expired with endcourse reason:" + reason);

                if (dashNavbar instanceof BsCourseHeaderWidget) {
                    ((BsCourseHeaderWidget) dashNavbar).handleCourseEndingTimerExpired(reason);
                }

                // We need to display a 'please wait' message since we wait for the TUI
                // to shutdown properly.  We also need to start a hard timer in the event that the TUI
                // doesn't respond.

                if (subPanel instanceof BsCourseRuntimeWidget) {
                    ((BsCourseRuntimeWidget) subPanel).handleCourseEndingTimerExpired(reason);
                }

                setCurrentCourseName(null);

                resetEndCourseReason();

            }


        };

        endCourseTimer.schedule(ENDCOURSETIMER_MS);

    }

    /**
     * Displays an error dialog with a title & message. A callback can be passed in to allow for handling
     * once the selection is confirmed.
     *
     * @param title - The title of the dialog.
     * @param description - The message for the dialog.
     * @param callback - (Optional) callback that can be passed in to handle when the dialog button is clicked/dismissed.
     */
    public void displayErrorDialog(String title, String description, DialogCallback callback) {
        displayDialog(DialogType.DIALOG_ERROR, title, description, callback);
    }

    /**
     * Displays the dialog with a title & message. The type of dialog can be specified as well (error or info).
     *
     * @param type - The type of dialog (error or info) to display.
     * @param title - The title of the dialog.
     * @param description - The message for the dialog.
     * @param callback - (Optional) callback that can be passed in to handle when the dialog button is clicked/dismissed.
     */
    public void displayDialog(final DialogType type, final String title, final String description, final DialogCallback callback) {
        logger.info("displaying dialog with title: " + title);


        // The TUI can send a duplicate display dialog message at nearly the same time, which causes an issue in displaying the modal dialog
        // For now we will ignore a request to show a dialog if the same dialog is already being displayed.  In the future this could
        // be made more robust by having a type of 'modal manager' class which could manage things like trying to display two dialogs at nearly
        // the same time.
        if (dialogWidget.isModalShown() && dialogWidget.isSameDialog(type, title, description, "")) {
            logger.warning("Ignoring request to display the dialog.  It appears the same dialog is already being shown.");
            return;
        }

        closeDialogs();

        dialogWidget.setData(type, title,  description,  "", callback);
        dialogWidget.show();
    }

    /**
     * Displays an informational dialog to the user.
     *
     * @param title - The title for the informational dialog.
     * @param description - The description/message for the dialog.
     */
    public void displayInfoDialog(String title, String description) {

        displayInfoDialog(title, description, null);
    }

    /**
     * Displays an informational dialog to the user.
     *
     * @param title - The title for the informational dialog.
     * @param description - The description/message for the dialog.
     * @param callback - (Optional) callback that can be passed in to handle when the dialog button is clicked/dismissed.
     */
    public void displayInfoDialog(String title, String description, final DialogCallback callback) {

        displayDialog(DialogType.DIALOG_INFO, title, description, callback);
    }

    /**
     * Displays a detailed error dialog with the specified information
     *
     * @param title - The title of the dialog.
     * @param errorReason - The reason message for the dialog.
     * @param errorDetails - A detailed explanation of the error
     * @param errorStackTrace - A list of stack trace messages
     * @param courseName - the name of the course the error happened in.   Can be null but not empty.
     */
    public void displayDetailedErrorDialog(final String title, final String errorReason, final String errorDetails, final List<String> errorStackTrace, String courseName) {

       displayDetailedErrorDialog(title, errorReason, errorDetails, errorStackTrace, courseName, null);
    }

    /**
     * Displays a detailed error dialog with the specified information
     *
     * @param title - The title of the dialog.
     * @param errorReason - The reason message for the dialog.
     * @param errorDetails - A detailed explanation of the error
     * @param errorStackTrace - A list of stack trace messages
     * @param courseName - the name of the course the error happened in.  Can be null but not empty.
     * @param callback - An optional callback that can be used to perform action after the close button is clicked.
     */
    public void displayDetailedErrorDialog(final String title, final String errorReason, final String errorDetails, final List<String> errorStackTrace, String courseName, final ModalDialogCallback callback) {
        logger.info("displaying detailed error dialog with title: " + title);


        // The TUI can send a duplicate display dialog message at nearly the same time, which causes an issue in displaying the modal dialog
        // For now we will ignore a request to show a dialog if the same dialog is already being displayed.  In the future this could
        // be made more robust by having a type of 'modal manager' class which could manage things like trying to display two dialogs at nearly
        // the same time.
        if (isDetailedErrorWidgetShowing() && detailedErrorWidget.isSameDialog(title, errorReason, errorDetails)) {
            logger.warning("Ignoring request to display the dialog.  It appears the same dialog is already being shown.");
            return;
        }


        closeDialogs();

        detailedErrorWidget.setData(title, errorReason, new ErrorDetails(errorDetails, errorStackTrace));
        detailedErrorWidget.setCourseName(courseName);
        detailedErrorWidget.setCallback(new ModalDialogCallback() {
            
            @Override
            public void onClose() {
                if(callback != null){
                    callback.onClose();
                }
                
                /* Clear out the detailed error dialog's callback. If this is not done, then hiding said widget
                 * when another dialog is being shown (such as a confirmation dialog) will re-invoke the close
                 * callback, which is not what we want */
                detailedErrorWidget.setCallback(null);
            }
        });
        detailedErrorWidget.center();

    }

    /**
     * Displays a detailed error dialog with the given detailed exception wrapper. This is handy for displaying the results of
     * RPC calls using {@link mil.arl.gift.common.gwt.client.GenericRpcResponse GenericRpcResponse}.
     *
     * @param title - The title of the dialog.
     * @param exceptionWrapper - A client-safe wrapper around a detailed exception.
     */
    public void displayDetailedErrorDialog(final String title, DetailedExceptionSerializedWrapper exceptionWrapper) {

        displayDetailedErrorDialog(title, exceptionWrapper.getReason(), exceptionWrapper.getDetails(),exceptionWrapper.getErrorStackTrace(), null);

    }


    /**
     * Displays a confirmational dialog.  This dialog allows for a 'yes'/'no' option for
     * the user to select.  A callback can be passed in to handle each of the choices
     * that the user may take.
     *
     * @param title - Title for the dialog.
     * @param description - Description/message that will be displayed in the dialog to the user.
     * @param callback - (optional) A callback that will be triggered for the action that is taken by the user.
     */
    public void displayConfirmDialog(String title, String description, ConfirmationDialogCallback callback) {

        displayConfirmDialog(title, description, null, null, callback);
    }

    /**
     * Displays a confirmational dialog with custom labels for the 'confirm'/'decline' buttons.  A callback
     * can be passed in to allow for taking action when the buttons are clicked.
     *
     * @param title - Title for the dialog.
     * @param description - Description/message that will be displayed in the dialog to the user.
     * @param confirmLabel - (optional) The label for the 'confirm' button.  If null is specified the default of "Yes" is used.
     * @param declineLabel - (optional) The label for the 'decline' button.  If null is specified the default of "No" is used.
     * @param callback - (optional) A callback that will be triggered for the action that is taken by the user.
     */
    public void displayConfirmDialog(final String title, final String description, final String confirmLabel, final String declineLabel, final ConfirmationDialogCallback callback) {
        logger.info("displaying confirmation dialog with title: " + title);


        // The TUI can send a duplicate display dialog message at nearly the same time, which causes an issue in displaying the modal dialog
        // For now we will ignore a request to show a dialog if the same dialog is already being displayed.  In the future this could
        // be made more robust by having a type of 'modal manager' class which could manage things like trying to display two dialogs at nearly
        // the same time.
        if (confirmWidget.isModalShown() && confirmWidget.isSameDialog(title, description)) {
            logger.warning("Ignoring request to display the confirmation dialog.  It appears the same dialog is already being shown.");
            return;
        }

        closeDialogs();


        confirmWidget.setData(title,  description, confirmLabel, declineLabel, callback);
        confirmWidget.show();
    }

    /**
     * Displays a confirmational dialog with custom labels for the 'confirm'/'decline' buttons.  A callback
     * can be passed in to allow for taking action when the buttons are clicked.
     *
     * @param title - Title for the dialog.
     * @param description - Description/message that will be displayed in the dialog to the user.
     * @param placeholderText - Placeholder text to display in the textbox. This can be null if not needed.
     * @param confirmLabel - (optional) The label for the 'confirm' button.  If null is specified the default of "Yes" is used.
     * @param callback - (optional) A callback that will be triggered for the action that is taken by the user.
     */
    public void displayRenameDialog(final String title, final String description, final String placeholderText, final String confirmLabel, final ValueChangeHandler<String> callback) {
        logger.info("displaying confirmation dialog with title: " + title);


        // The TUI can send a duplicate display dialog message at nearly the same time, which causes an issue in displaying the modal dialog
        // For now we will ignore a request to show a dialog if the same dialog is already being displayed.  In the future this could
        // be made more robust by having a type of 'modal manager' class which could manage things like trying to display two dialogs at nearly
        // the same time.
        if (renameWidget.isModalShown() && renameWidget.isSameDialog(title, description)) {
            logger.warning("Ignoring request to display the confirmation dialog.  It appears the same dialog is already being shown.");
            return;
        }

        closeDialogs();


        renameWidget.setData(title,  description, placeholderText, confirmLabel, callback);
        renameWidget.show();
    }

    /**
     * Displays a dialog showing the users that have access to the course. This dialog can only be
     * accessed from the owner of the course. The owner will have the option to add/edit/remove the
     * shared users.
     *
     * @param courseData the course data
     */
    public void displayShareDialog(DomainOption courseData) {
        logger.info("displaying share course dialog");


        // The TUI can send a duplicate display dialog message at nearly the same time, which causes an issue in displaying the modal dialog
        // For now we will ignore a request to show a dialog if the same dialog is already being displayed.  In the future this could
        // be made more robust by having a type of 'modal manager' class which could manage things like trying to display two dialogs at nearly
        // the same time.
        if (sharedPermissionsWidget.isModalShown()) {
            logger.warning("Ignoring request to display the share course dialog.  It appears the same dialog is already being shown.");
            return;
        }

        closeDialogs();
        sharedPermissionsWidget.setData(courseData);
        sharedPermissionsWidget.show();
    }

    /**
     * Displays a dialog showing the users that have access to the published course. This dialog can only be
     * accessed from the owner of the published course. The owner will have the option to add/edit/remove the
     * shared users.
     *
     * @param dataCollectionItem the data collection (published course) data
     */
    public void displayShareDialog(DataCollectionItem dataCollectionItem) {
        logger.info("displaying share published course dialog");


        // The TUI can send a duplicate display dialog message at nearly the same time, which causes an issue in displaying the modal dialog
        // For now we will ignore a request to show a dialog if the same dialog is already being displayed.  In the future this could
        // be made more robust by having a type of 'modal manager' class which could manage things like trying to display two dialogs at nearly
        // the same time.
        if (sharedPermissionsWidget.isModalShown()) {
            logger.warning("Ignoring request to display the share published course dialog.  It appears the same dialog is already being shown.");
            return;
        }

        closeDialogs();

        sharedPermissionsWidget.setData(dataCollectionItem);
        sharedPermissionsWidget.show();
    }

    /**
     * Closes any dialogs that may be opened.
     */
    private void closeDialogs() {
        dialogWidget.hide();
        renameWidget.hide();
        confirmWidget.hide();
        detailedErrorWidget.hide();
        sharedPermissionsWidget.hideAll();
    }

    /**
     * The page load error screen is only used in the event that the server properties cannot be retrieved.
     * In this case we don't even want the user to login since the client may not have all the information
     * required for the dashboard to run properly.
     */
    private void displayPageLoadErrorScreen(Object data) {


        // Hide the navbar.
        navBar.setVisible(false);
        dashNavbar = navBar;

        String errorTitle = "";
        String errorMessage = "";
        if (data != null && data instanceof PageLoadError) {
            PageLoadError pageLoadObj = (PageLoadError)data;

            errorTitle = pageLoadObj.getErrorTitle();
            errorMessage = pageLoadObj.getErrorMessage();

        }

        subPanel = new BsPageLoadErrorWidget(errorTitle, errorMessage);

        rebuildRootPanel();

    }

    /**
     * Displays the login screen
     */
    private void displayLoginScreen() {

        // Set the url to the login url.
        HistoryManager.getInstance().addHistory(HistoryManager.LOGIN);

        // We don't need to update the state here, since the login screen is
        // the default for a non logged in user.
        navBar.setVisible(false);
        dashNavbar = navBar;
        
        if(serverProperties.shouldUseSSOLogin()) {
            
            /* 
             * If the server is configured to use SSO to authenticate users, then we need to skip
             * GIFT's normal login page but still perform the same server-side logic that would be
             * performed if the user had signed, since it's needed to let them proceed to the rest 
             * of the webpage.
             * 
             * The null values passed to the login request below are intentional, since using SSO
             * won't pass the user's username to this client.
             */
            dashboardService.loginUser(null, null, null, new AsyncCallback<LoginResponse>() {

                @Override
                public void onFailure(Throwable caught) {
                    //This will prevent offline login
                    displayErrorScreen(new DetailedExceptionSerializedWrapper(new DetailedException(
                            "There was a problem communicating with the server. Please check your internet connection and try again.", 
                            "The server threw an error while fulfilling the request.", 
                            caught
                    )));
                }

                @Override
                public void onSuccess(LoginResponse result) {
                    
                    if (!result.isSuccess()) {
                        
                        displayErrorScreen(new DetailedExceptionSerializedWrapper(new DetailedException(
                                "There was a problem communicating with the server. Please check your internet connection and try again.", 
                                "The server threw an error while fulfilling the request.", 
                                null
                        )));
                        
                    } else {                    
                        //add username to cookies for suggestion upon next login
                        String username = result.getUserName();
                        BsLoginWidget.addUsernameToCookie(username);
                        
                        /* Initialize the browser session */
                        String sessionId = result.getUserSessionId();
                        String browserSessionId = result.getBrowserSessionId();
                                
                        SessionStorage.putItem(SessionStorage.DASHBOARD_SESSIONID_TOKEN, browserSessionId);

                        UiManager.getInstance().setSessionId(browserSessionId);

                        UiManager.getInstance().setUserName(result.getUserName());
                        UiManager.getInstance().setUserPassword(result.getUserPass());
                        
                        if(result.isAutoDebug()){
                            //when using 'login as' we don't want to loose the real user's username because that will be
                            //needed when the tutor authenticates (again) before running a course
                            UiManager.getInstance().setActualUserName(username);
                            
                            //debug mode entered successfully
                            UiManager.getInstance().setDebugMode(true);
                        }
                        
                        // Before transitioning to the screen, we now want to establish a websocket connection with the server as well
                        // by creating a browser session on the client that contains a websocket.
                        ResumeSessionHandler resumeHandler = new ResumeSessionHandler(ScreenEnum.MYCOURSES);
                        BrowserSession.createBrowserSession(sessionId, browserSessionId,  resumeHandler);
                    }
                }
            });
            
        } else {

            // Show the login page for cloud version if it's enabled.  Otherwise, default to the
            // normal login page version.
            boolean useCloudLogin = getUseCloudLoginPage();
            if (useCloudLogin) {
                logger.info("Displaying cloud login page.");
                subPanel = new BsLoginWidgetCloud();
    
            } else {
                logger.info("Displaying login page.");
                subPanel = new BsLoginWidget();
    
            }
        }


        rebuildRootPanel();
    }



    /**
     * Displays the my courses screen.
     *
     */
    private void displayMyCoursesScreen() {


        // Set the url to the mycourses url.
        HistoryManager.getInstance().addHistory(HistoryManager.MYCOURSES);

        navBar.updateUserState(UiManager.getInstance().getUserName(),  BsHeaderWidget.UserLoginState.STATE_LOGGEDIN);
        navBar.setSelectedNavBarItem(ScreenEnum.MYCOURSES);
        navBar.setMinimizeIconVisible(false);
        navBar.setVisible(true);

        dashNavbar = navBar;
        subPanel = new BsMyCoursesDashWidget();

        rebuildRootPanel();
    }

    /**
     * Displays the My Tools Screen
     *
     */
    private void displayMyToolsScreen(Object data) {

        // Set the url to the mytools url.
        HistoryManager.getInstance().addHistory(HistoryManager.MYTOOLS);

        logger.fine("Calling updatestate with session id: " + UiManager.getInstance().getSessionId());
        navBar.updateUserState(UiManager.getInstance().getUserName(),  BsHeaderWidget.UserLoginState.STATE_LOGGEDIN);
        navBar.setSelectedNavBarItem(ScreenEnum.COURSE_CREATOR);
        navBar.setMinimizeIconVisible(false);
        navBar.setVisible(true);

        dashNavbar = navBar;

        BsMyToolsWidget toolsWidget = new BsMyToolsWidget();
        toolsWidget.updateMyToolsPanel(data);
        subPanel = toolsWidget;

        rebuildRootPanel();
    }

    /**
     * Displays the Publish Course Screen
     * @param data optional information about a specific course to use to automatically show published courses
     * that reference that course.  Currently supported values are of type @link {@link DomainOption}.
     */
    private void displayMyExperimentsScreen(Object data) {

        // Block the URLs used to reach the pages we hide when LessonLevel is set to RTA
        if(isRtaLessonLevel()) {
            displayLoginScreen();
        } else {
            
         // Set the url to the mycourses url.
            HistoryManager.getInstance().addHistory(HistoryManager.MYEXPERIMENTS);

            navBar.updateUserState(UiManager.getInstance().getUserName(),  BsHeaderWidget.UserLoginState.STATE_LOGGEDIN);
            navBar.setSelectedNavBarItem(ScreenEnum.MY_RESEARCH);
            navBar.setMinimizeIconVisible(false);
            navBar.setVisible(true);

            dashNavbar = navBar;
            subPanel = new ExperimentToolWidget();

            if(data instanceof DomainOption){
                ((ExperimentToolWidget)subPanel).expandPublishCourses((DomainOption)data, null);
            }

            rebuildRootPanel();
        }
    }

    /**
     * Displays the My Stats Screen
     *
     */
    private void displayMyStatsScreen() {

        // Block the URLs used to reach the pages we hide when LessonLevel is set to RTA
        if(isRtaLessonLevel()) {
            displayLoginScreen();
        } else {
            
         // Set the url to the mytools url.
            HistoryManager.getInstance().addHistory(HistoryManager.MYSTATS);

            navBar.updateUserState(UiManager.getInstance().getUserName(),  BsHeaderWidget.UserLoginState.STATE_LOGGEDIN);
            navBar.setSelectedNavBarItem(ScreenEnum.LEARNER_PROFILE);
            navBar.setMinimizeIconVisible(false);
            navBar.setVisible(true);

            dashNavbar = navBar;
            subPanel = new BsMyStatsWidget();

            rebuildRootPanel();
        }
    }


    /**
     * Displays the course runtime screen (which should be the TUI embedded as an iframe).
     *
     */
    private void displayCourseRuntimeScreen(Object data) {

        logger.info("Displaying course runtime screen.");

        // Set the url to the course runtime url.
        HistoryManager.getInstance().addHistory(HistoryManager.COURSE_RUNTIME);

        navBar.updateUserState(UiManager.getInstance().getUserName(),  BsHeaderWidget.UserLoginState.STATE_LOGGEDIN);
        navBar.setSelectedNavBarItem(ScreenEnum.MYCOURSES);
        navBar.setMinimizeIconVisible(false);
        navBar.setVisible(true);

        dashNavbar = new BsCourseHeaderWidget(UiManager.getInstance().getUserName(), data);
        subPanel = new BsCourseRuntimeWidget(data, offlineMode, serverProperties.getTutorCourseStartTimeout());

        rebuildRootPanel();

        if (tuiListener != null) {
            IFrameMessageHandlerParent.getInstance().removeMessageListener(tuiListener);
        }
        tuiListener = new TuiMessageListener();
        IFrameMessageHandlerParent.getInstance().addMessageListener(tuiListener);

        //start listening to messages from the mobile app
        IFrameMessageHandlerChild.getInstance().addMessageListener(applicationFrameListener);
    }


    /**
     * Displays the course runtime screen for an lti course launch request.
     * This should be the TUI embedded as an iframe.
     *
     * The navbar header is also hidden since the lti users are not logged into
     * the gift dashboard in the normal sense.  The lti user is treated as an
     * anonymous external user.
     *
     * @param data The screen initialization parameters.
     */
    private void displayLtiCourseRuntimeScreen(Object data) {

        // Set the url to the course runtime url.
        HistoryManager.getInstance().addHistory(HistoryManager.COURSE_RUNTIME);

        navBar.setVisible(false);
        // When running in lti mode there is no navbar.
        dashNavbar = null;

        subPanel = new LtiCourseRuntimeWidget(data, offlineMode, serverProperties);

        rebuildRootPanel();

        if (tuiListener != null) {
            IFrameMessageHandlerParent.getInstance().removeMessageListener(tuiListener);
        }
        tuiListener = new LtiTuiMessageListener();
        IFrameMessageHandlerParent.getInstance().addMessageListener(tuiListener);

        //start listening to messages from the mobile app
        IFrameMessageHandlerChild.getInstance().addMessageListener(applicationFrameListener);
    }

    /**
     * Displays the game master panel screen.
     *
     * @param data The screen initialization parameters.
     * @param showActiveSessions true to show the active sessions; false to show
     *        the past sessions.
     */
    private void displayGameMasterPanelScreen(Object data, boolean showActiveSessions) {

        if((getDeploymentMode() == DeploymentModeEnum.SERVER && !isDebugMode())){
            // bump user to login screen because they can't access game master panel in server, non-debug mode
            displayLoginScreen();
        }else{
            // Set the url to the game master url.
            HistoryManager.getInstance().addHistory(HistoryManager.GAME_MASTER);

            navBar.updateUserState(UiManager.getInstance().getUserName(),  BsHeaderWidget.UserLoginState.STATE_LOGGEDIN);
            navBar.setSelectedNavBarItem(showActiveSessions ? ScreenEnum.GAME_MASTER_ACTIVE : ScreenEnum.GAME_MASTER_PAST);
            navBar.setMinimizeIconVisible(true);
            navBar.setVisible(true);

            dashNavbar = navBar;
            subPanel = new BsGameMasterPanel(showActiveSessions);

            rebuildRootPanel();
        }
    }
    
    /**
     * Displays the web monitor screen.
     *
     * @param data The screen initialization parameters.
     * @param mode the mode to show the web monitor in. Cannot be null.
     */
    private void displayWebMonitorScreen(Object data, WebMonitorPanel.MonitorMode mode) {

        if((getDeploymentMode() == DeploymentModeEnum.SERVER && !isDebugMode())){
            // bump user to login screen because they can't access the web monitor in server, non-debug mode
            displayLoginScreen();
            
        }else{
            
            if(mode == null) {
                throw new IllegalArgumentException("The web monitor mode to display cannot be null");
            }
            
            navBar.updateUserState(UiManager.getInstance().getUserName(), BsHeaderWidget.UserLoginState.STATE_LOGGEDIN);
            navBar.setSelectedNavBarItem(mode.getScreen());
            navBar.setVisible(true);

            dashNavbar = navBar;
            subPanel = new WebMonitorPanel(mode);

            rebuildRootPanel();
        }
    }

    /**
     * Displays the course collection to the user that matches the id in the
     * data parameter.
     *
     * @param data The {@link String} representation of the course group id.
     *        Can't be null.
     */
    private void displayCourseCollectionScreen(Object data) {

        final String courseCollectionId = (String) data;
        dashboardService.getCourseCollection(courseCollectionId, new AsyncCallback<CourseCollection>() {

            @Override
            public void onSuccess(CourseCollection result) {
                subPanel = new CourseCollectionPage(result);
                rebuildRootPanel();
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE,
                        "There was a problem fetching the courses in the collection '" + courseCollectionId + "'.",
                        caught);
            }
        });
    }

    /**
     * Displays the experiment welcome page.
     *
     * @param data The screen initialization parameters.  Should be of type String.
     */
    private void displayExperimentWelcomePage(Object data) {
        HistoryManager.getInstance().addHistory(HistoryManager.EXPERIMENT_START);
        
        // When running in experiment mode there is no navbar.
        navBar.setVisible(false);
        dashNavbar = null;

        logger.info("Displaying Experiment Welcome page.");
        subPanel = new ExperimentWelcomePageWidget(data);

        rebuildRootPanel();
    }

    /**
     * Displays the experiment welcome page.
     *
     * @param data The screen initialization parameters.  Should be of type String.
     */
    private void displayExperimentRuntimeScreen(Object data) {

        // When running in experiment mode there is no navbar.
        navBar.setVisible(false);
        dashNavbar = null;

        subPanel = new BsCourseRuntimeWidget(data, offlineMode, serverProperties.getTutorCourseStartTimeout());

        rebuildRootPanel();

        if (tuiListener != null) {
            IFrameMessageHandlerParent.getInstance().removeMessageListener(tuiListener);
        }
        tuiListener = new ExperimentTuiMessageListener();
        IFrameMessageHandlerParent.getInstance().addMessageListener(tuiListener);

        //start listening to messages from the mobile app
        IFrameMessageHandlerChild.getInstance().addMessageListener(applicationFrameListener);
    }

    /**
     * Rebuilds the root panel with the new sub panel.
     */
    private void rebuildRootPanel() {

        // Reset the tui listener.
        if (tuiListener != null) {
            IFrameMessageHandlerParent.getInstance().removeMessageListener(tuiListener);
            tuiListener = null;
        }

        //stop listening to messages from the mobile app
        IFrameMessageHandlerChild.getInstance().removeMessageListener(applicationFrameListener);

        RootPanel document = RootPanel.get();
        document.clear();

        // The navbar can be optional (one example is for lti launches).
        if (dashNavbar != null) {
            document.add(dashNavbar);
        }

        document.add(subPanel);
        // We add the modal dialog widgets always to the root panel (they remain hidden until needed).
        document.add(dialogWidget);
        document.add(renameWidget);
        document.add(confirmWidget);
        document.add(sharedPermissionsWidget);
        document.add(sharedPermissionsWidget.getAddSharedCoursePermissionsDialogWidget());
        document.add(sharedPermissionsWidget.getAddSharedPublishedCoursePermissionsDialogWidget());
        document.add(sharedPermissionsWidget.getDeleteSharedPermissionsDialogWidget());
    }


    /**
     * ScreenAsyncCallback class stores the screen state & screen data that can
     * be used after the callback has returned a success or failure.
     *
     * @author nblomberg
     *
     */
    private class ScreenAsyncCallback implements AsyncCallback<RpcResponse> {

        /**
         * Stores the screen state that is needed for the callback once it has
         * completed.
         */
        private ScreenEnum screenState = ScreenEnum.INVALID;
        /**
         * Stores the screen data (optional) that is needed for the callback
         * once it has completed.
         */
        private Object screenData = null;

        /**
         * Accessor to set the screen state for the callback;
         * @param screenState
         */
        public void setScreenState(ScreenEnum screenState) {
            this.screenState = screenState;
        }


        /**
         * Accessor to set the screenData (optional) for the callback.
         *
         * @param screenData
         */
        public void setScreenData(Object screenData) {
            this.screenData = screenData;
        }

        @Override
        public void onFailure(Throwable caught) {
            displayLoginScreen();
        }

        @Override
        public void onSuccess(RpcResponse result) {

            if (result.isSuccess()) {
                logger.fine("updateUserState succeeded. screenstate = "+screenState+", screenData = "+screenData);
                internalDisplayScreen(screenState, screenData);
            } else {
                displayLoginScreen();
            }
        }

    }

    /**
     * Determines whether or not the user is using GIFT in offline mode
     * @return true of the user is in offline mode, false otherwise
     */
    public boolean getOfflineMode() {
        return offlineMode;
    }

     /**
     * Gets the deployment mode
     *
     * @return the deployment mode
     */
    public DeploymentModeEnum getDeploymentMode() {
        return deploymentMode;
    }


    /**
     * Function to show the course details panel (valid only if the user is on the mycourses dashboard widget).
     * @param courseData - The course data that will be  used to populate the course details screen.  Cannot be null.
     */
    public void showCourseDetails(DomainOption courseData) {

        if (subPanel instanceof BsMyCoursesDashWidget) {
            BsMyCoursesDashWidget myCourses = (BsMyCoursesDashWidget) subPanel;

            myCourses.displayCourseDetails(courseData);
        }

    }

    /**
     * Function to start the logic to take a course.
     *
     * @param courseData the course data that will be used to find the course and start the course.  Cannot be null.
     * @throws Exception if the course data is null
     */
    public void takeACourse(DomainOption courseData) throws Exception{
        
        if(courseData == null){
            throw new Exception("The course data is null");
        }

        if (subPanel instanceof BsMyCoursesDashWidget) {
            BsMyCoursesDashWidget myCourses = (BsMyCoursesDashWidget) subPanel;

            myCourses.takeACourse(courseData);
        }

    }


    /**
     * Modifies the height of the specified UI element so that it takes up the remaining viewable space below it. This method will also attach
     * a resize handler to the viewport so that the element's height will dynamically change to fit the new viewport dimensions when the browser
     * window is resized.
     *
     * Ideally, this method should only be called once on a single element to avoid assigning multiple resize handlers that all do the same thing.
     *
     * @param element the UI element to fill to the bottom of the viewport
     * @throws IllegalArgumentException if the UI element passed in is null
     */
    public void fillToBottomOfViewport(final Widget element) throws IllegalArgumentException{

        if(element == null){
            throw new IllegalArgumentException("The element used to fill the remainder of the viewport cannot be null");
        }

        //fill to current viewport height
        element.setHeight(Window.getClientHeight() - element.getAbsoluteTop() - SUBPIXEL_RESIZE_ADJUSTMENT + "px");

        //periodically refresh height to deal with moving elements
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            @Override
            public boolean execute() {

                    element.setHeight(Window.getClientHeight() - element.getAbsoluteTop() - SUBPIXEL_RESIZE_ADJUSTMENT + "px");

                    return true;
            }
        }, 100);

        //fill to new viewport height when viewport is resized
        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                element.setHeight(event.getHeight() - element.getAbsoluteTop() - SUBPIXEL_RESIZE_ADJUSTMENT + "px");
            }
        });
    }

    /**
     * Accessor to determine if the course details panel is open (while on the mycourses tab).
     *
     * @return - True if the course details panel is open/visible.  Returns false otherwise.
     */
    public boolean isCourseDetailsVisible() {

        boolean isVisible = false;
        if (subPanel instanceof BsMyCoursesDashWidget) {
            BsMyCoursesDashWidget myCourses = (BsMyCoursesDashWidget) subPanel;

            isVisible = myCourses.isCourseDetailsVisible();

        }

        return isVisible;

    }

    /**
     * Closes the course details panel (on the my courses tab).
     */
    public void closeCourseDetails() {

        // Only try to close the course details panel if the my courses widget is active.
        if (subPanel instanceof BsMyCoursesDashWidget) {
            BsMyCoursesDashWidget myCourses = (BsMyCoursesDashWidget) subPanel;

            myCourses.closeCourseDetails();
        }

    }


    /**
     * Shows the stop course dialog (if the course runtime is open).
     */
    public void showStopCourseDialog() {
        if (dashNavbar instanceof BsCourseHeaderWidget) {
            BsCourseHeaderWidget courseHeader = (BsCourseHeaderWidget) dashNavbar;

            courseHeader.showStopCourseDialog();
        }

    }


    /**
     * Returns true if the course runtime is displayed to the user.
     *
     * @return - True if the user is in the course runtime, false otherwise.
     */
    public boolean isCourseRuntimeActive() {

        boolean isActive = false;
        if (subPanel instanceof BsCourseRuntimeWidget) {
            isActive = true;
        }

        return isActive;
    }

    /**
     * Cancel the timer (if running) that is waiting for the Tutor User Interface to
     * be shown in the iframe of the dashboard.  If the timer is not canceled and an error
     * happens when starting a course the error dialog shown to the user will be automatically
     * removed once the timer fires.
     */
    public void cancelTutorIFrameTimer(){

        if (tuiIFrameTimer != null && tuiIFrameTimer.isRunning()) {
            logger.info("cancelTutorIFrameTimer()");
            tuiIFrameTimer.cancel();
        }
    }

    /**
     * Helper function to determine if a 'leave page' warning should be presented
     * to the user during an 'on window close' event.
     *
     * @param event - The window closing event for the current close event.
     * @return Window.ClosingEvent - The window closing event (if the message is modified, then the user will be presented with a leave page warning). If the leave page
     *                               warning should not be displayed, then the event message is not set.
     */
    private Window.ClosingEvent showLeavePageWarning(Window.ClosingEvent event) {


        // Display a leave page warning if the course runtime is active (but not in an error state).
        if (isCourseRuntimeActive() && !isDetailedErrorWidgetShowing()) {
            // For the LTI effort, the leave site message is not desired.
            // event.setMessage(LEAVE_COURSE_RUNTIME_WARNING);
        } else if (isGatAuthoringActive()) {
            event.setMessage(LEAVE_GAT_WARNING);
        }


        return event;

    }

    /**
     * Returns true if the detailed error widget is showing.
     *
     * @return True if the detailed erorr widget is showing. False otherwise.
     */
    private boolean isDetailedErrorWidgetShowing() {
        logger.info("isDetailedErrorWidgetShowing = " + detailedErrorWidget.isShowing());
        return detailedErrorWidget.isShowing();
    }

    /**
     * Function to determine if the gat authoring tool is visible/active and has open files that are not read-only.
     *
     * @return - true if the gat authoring tool is visible/active and has files open that are not read-only.  False otherwise.
     */
    public boolean isGatAuthoringActive() {

        boolean isActive = false;
        if (subPanel instanceof BsMyToolsWidget) {

            BsMyToolsWidget panel = (BsMyToolsWidget) subPanel;


            isActive = panel.isGatAuthoringToolActive();
        }

        return isActive;

    }

    /**
     * Accessor to show the Edit Course (Gat) page in the my tools panel.
     *
     * @param courseData - The course data that the gat should be displayed in.  If null a new course will be created.
     */
    public void showEditCourse(DomainOption courseData) {
        displayScreen(ScreenEnum.COURSE_CREATOR, courseData);
    }



    /**
     * Displays the leave gat dialog.  A screen enum is provided
     * to indicate which screen the user will be taken to if they confirm to leave the gat.
     *
     * @param screenId - The screen id that the user will be taken to if they confirm to leave the gat tool.
     */
    public void showLeaveGatDialog(ScreenEnum screenId) {

        if (subPanel instanceof BsMyToolsWidget) {

            BsMyToolsWidget panel = (BsMyToolsWidget) subPanel;
            panel.showLeaveGatDialog(screenId);
        }

    }

    /**
     * Redirect back to the tutor experiment with the appropriate settings for
     * when the back button is pressed.
     * 
     * @param isExperimentActive true if the experiment is currently running;
     *        false otherwise.
     */
    public void browserBackClickedInExperiment(boolean isExperimentActive) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("experimentBackButtonPressed(" + isExperimentActive + ")");
        }

        /* Get the experiment id from the URL parameter map */
        final String experimentId = Window.Location.getParameter(ExperimentUrlManager.EXPERIMENT_ID_URL_PARAMETER);
        if (StringUtils.isBlank(experimentId)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Performing no action on browser 'back' because no experiment id was available.");
            }
            return;
        }

        /* Get the return url from the URL parameter map (can be null) */
        final String returnUrl = Window.Location.getParameter(ExperimentUrlManager.RETURN_URL_PARAMETER);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Return URL is '" + returnUrl + "'");
        }

        final ExperimentParameters experimentParameters = new ExperimentParameters(experimentId, returnUrl);

        if (isExperimentActive) {
            final String historyToken = History.getToken();
            if (StringUtils.isBlank(historyToken) || !historyToken.contains(HistoryManager.USER_SESSION_ID_TAG)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(
                            "Performing no action on browser 'back' because history token didn't contain the necessary user session id. Token: '"
                                    + historyToken + "'");
                }
                return;
            }

            /* Parse user session id */
            String userSessionId = historyToken.substring(historyToken.indexOf(HistoryManager.USER_SESSION_ID_TAG));
            userSessionId = userSessionId.substring(userSessionId.indexOf("=") + 1);

            /* Build the experiment parameters */
            experimentParameters.setUserSessionId(userSessionId);

            /* Build the load course parameters */
            LoadCourseParameters loadCourseParameters = new LoadCourseParameters();
            loadCourseParameters.setExperimentParameters(experimentParameters);

            /* Run the experiment with the parameters. Should display a
             * resume/exit page to the user. */
            UiManager.getInstance().displayScreen(ScreenEnum.EXPERIMENT_RUNTIME, loadCourseParameters);

        } else if (StringUtils.isNotBlank(returnUrl)) {
            /* The back button was clicked while the experiment was inactive.
             * The experiment has a return URL so go there. */
            Window.Location.replace(returnUrl);

        } else {
            /* The back button was clicked while the experiment was inactive. No
             * return URL was specified so redirect the user to the welcome
             * page. */
            UiManager.getInstance().displayScreen(ScreenEnum.EXPERIMENT_WELCOME_PAGE, experimentParameters);
        }
    }

    /**
     * Displays the lti consumer start screen.
     *
     * @param data The screen initialization parameters.  Should be of type LtiConsumerStartWidgetParams.
     */
    private void displayLtiConsumerStartScreen(Object data) {
        // Set the url to the lti consumer start page.
        HistoryManager.getInstance().addHistory(HistoryManager.LTICONSUMERSTART);

        // We don't need to update the state here, since the login screen is
        // the default for a non logged in user.
        navBar.setVisible(false);
        // When running in lti mode there is no navbar.
        dashNavbar = null;


        logger.info("Displaying Lti Consumer Start page.");
        subPanel = new LtiConsumerStartWidget(data);

        rebuildRootPanel();
    }

    /**
     * Displays the Error screen.
     *
     * @param data The screen initialization parameters.  Should be of type {@link DetailedExceptionSerializedWrapper}}.
     */
    private void displayErrorScreen(Object data) {

        // We don't need to update the state here, since the login screen is
        // the default for a non logged in user.
        navBar.setVisible(false);
        dashNavbar = navBar;

        logger.info("Displaying Dashboard Error page.");
        subPanel = new DashboardErrorWidget(data, serverProperties.getPropertyValue(ServerProperties.BACKGROUND_IMAGE));

        rebuildRootPanel();
    }

    /**
     * Displays the lti consumer end screen.
     *
     * @param data The screen initialization parameters (if any).  Can be null.
     */
    private void displayLtiConsumerEndScreen(Object data) {
        // Set the url to the lti consumer end page.
       HistoryManager.getInstance().addHistory(HistoryManager.LTICONSUMEREND);


       // We don't need to update the state here, since the login screen is
       // the default for a non logged in user.
       navBar.setVisible(false);
       // When running in lti mode there is no navbar.
       dashNavbar = null;


       logger.info("Displaying Lti Consumer End page.");
       subPanel = new LtiConsumerEndWidget(data);

       rebuildRootPanel();
   }
    
    /**
     * Set the flag indicating that the tui is ready.  By ready it means the embedded
     * tui has the course ready to be displayed.
     *
     * @param ready True to indicate that the tui is ready, false otherwise.
     */
    public void setTuiReady (boolean ready) {
        logger.info("setTuiReady: " + ready);
        tuiReady = ready;
    }

    /**
     * Accessor to determine if the tui is ready.  By ready it means the embedded
     * tui has the course ready to be displayed.
     * @return True if the tui is ready, false otherwise.
     */
    public boolean isTuiReady() {
        return tuiReady;
    }

    /**
     * Sets the tui iFrame timer.  There should only be one of these timers any previous
     * running timer needs to be cancelled before a new one is started.
     *
     * @param timer The timer to set.  Can be null.
     */
    public void setTuiIFrameTimer(Timer timer) {
        if (tuiIFrameTimer != null) {
            tuiIFrameTimer.cancel();
        }
        tuiIFrameTimer = timer;
    }

    /**
     * Updates the permission for one or more users on a course.
     *
     * @param permissions the permission for one or more users.
     * @param courseData the course data.
     * @param callback used to notify the caller of failure or success
     */
    public void updatePermissions(Set<DomainOptionPermissions> permissions, DomainOption courseData, AsyncCallback<RpcResponse> callback) {
        logger.info("Contacting server to update course permissions on "+courseData);
        dashboardService.updateCourseUserPermissions(permissions, courseData,
                UiManager.getInstance().getSessionId(), callback);
    }

    /**
     * Updates the permission for one or more users on a published course.
     *
     * @param permissions the permission for one or more users.  Can't be null.
     * @param courseData the course data.  Can't be null and must have a valid id value.
     * @param callback used to notify the caller of failure or success.  Can't be null.
     */
    public void updatePermissions(Set<DataCollectionPermission> permissions, DataCollectionItem dataCollectionItem, AsyncCallback<RpcResponse> callback) {
        logger.info("Contacting server to update published course permissions on "+dataCollectionItem);
        dashboardService.updatePublishedCourseUserPermissions(permissions, dataCollectionItem.getId(),
                UiManager.getInstance().getSessionId(), callback);
    }
    
    /**
     * Updates the permissions for one or more users on the given course.  The
     * details of which users to add comes from a previously uploaded csv file.
     * @param filePath the path to the csv file with user permissions to add to the course.  Assumes the path is relative
     * to the Domain directory.  E.g. temp/c9e27c56-f705-41fb-9d2c-07654f03ddac/permissionsTest.csv
     * @param dataCollectionItem contains information about the published course which permissions are changing
     * @param browserSessionId the browser session id from the client making the request
     * @param callback used to notify the caller of the updated DomainOption with the current permissions for that course.  Used to refresh the client UI permissions table.
     * @throws IllegalArgumentException if any of the arguments are not properly provided.
     */
    public void addCourseUserPermissionsFromFile(String filePath, DomainOption courseData, AsyncCallback<GenericRpcResponse<DomainOption>> callback) throws IllegalArgumentException{
        dashboardService.addCourseUserPermissionsFromFile(filePath, courseData, UiManager.getInstance().getSessionId(), callback);        
    }
    
    /**
     * Updates the permissions for one or more users on the given published course.  The
     * details of which users to add comes from a previously uploaded csv file.
     * @param filePath the path to the csv file with user permissions to add to the published course.  Assumes the path is relative
     * to the Domain directory.  E.g. temp/c9e27c56-f705-41fb-9d2c-07654f03ddac/permissionsTest.csv
     * @param dataCollectionItem contains information about the published course which permissions are changing
     * @param browserSessionId the browser session id from the client making the request
     * @param callback used to notify the caller of the updated DataCollectionItem with the current permissions for that published course.  Used to refresh the client UI permissions table.
     * @throws IllegalArgumentException if any of the arguments are not properly provided.
     */
    public void addPublishedCourseUserPermissionsFromFile(String filePath, DataCollectionItem dataCollectionItem, AsyncCallback<GenericRpcResponse<DataCollectionItem>> callback) throws IllegalArgumentException{
        dashboardService.addPublishedCourseUserPermissionsFromFile(filePath, dataCollectionItem, UiManager.getInstance().getSessionId(), callback);        
    }
    
    /**
     * Gets the RPC service used to send requests to the Dashboard server
     * 
     * @return the Dashboard RPC service
     */
    public DashboardServiceAsync getDashboardService() {
        return dashboardService;
    }

    /**
     * Displays a notification (via the Bootstrap Notify class).  This is a simple accessor to
     * display an optional title, message, and optional icon if needed.
     *
     * @param title The title of the notification (optional).  Can be an empty string to indicate no title.
     * @param message The message for the notification.  Cannot be empty or null.
     * @param iconType The bootstrap icon to display (optional).  Can be null to indicate to show no icon.
     */
    public void displayNotifyMessage(String title, String message, IconType iconType) {

        if (iconType != null) {
            Notify.notify(title, message, iconType);
        } else{
            Notify.notify(title, message);
        }
    }
    
    /**
     * Gets the Google Maps API key that has been assigned to the server, if such an API key exists
     * 
     * @return the API key to use when interacting with Google Maps
     */
    public String getGoogleMapsAPIKey() {
        return serverProperties.getPropertyValue(ServerProperties.GOOGLE_MAPS_API_KEY);
    }
}

