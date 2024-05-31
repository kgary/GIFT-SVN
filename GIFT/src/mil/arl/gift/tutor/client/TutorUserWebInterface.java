/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

import mil.arl.gift.common.gwt.client.BrowserProperties;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.gwt.shared.LtiParameters;
import mil.arl.gift.common.io.ExperimentUrlManager;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.widgets.IsRemovableTuiWidget;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;

/**
 * The entry point for the Tutor Web Interface
 */
public class TutorUserWebInterface implements EntryPoint, ValueChangeHandler<String> {

    
    private static Logger logger = Logger.getLogger(TutorUserWebInterface.class.getName());
    
    public final static String LOGIN_PAGE_TAG = "login";
    public final static String SIMPLE_LOGIN_PAGE_TAG = "login.simple";
    public final static String SELECT_DOMAIN_TAG = "selectdomain";
    public final static String DOMAIN_SESSION_PAGE_TAG = "domain";
    public final static String EXPERIMENT_PAGE_TAG = "experiment";
    public final static String USER_SESSION_ID = ".userSessionId=";

    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";

    /**
     * Indicates the method in which the tutor should launch courses.
     * 
     * @author nblomberg
     *
     */
    public enum LaunchMode {
        NORMAL_DASHBOARD_LAUNCH,   // User is requesting to launch a course from the dashboard (normal flow).
        OFFLINE_DASHBOARD_LAUNCH,  // User is requesting to launch a course from the dashboard, but is not connected to an external network.
        PING_ONLY_LAUNCH,          // User is requesting to see if the tutor URL is reachable (just pinging the tutor only).
        LTI_LAUNCH,                // User is requesting to launch a course via the LTI launch flow.
        EXPERIMENT_LAUNCH,         // User is requesting to launch an experiment.
        TUI_SIMPLE_LAUNCH          // User is requesting to launch the tutor in Simple Mode, which means the Tutor should login the user 
                                   // and have the user select the domain/course within the tutor.  (There is no dashboard login for the user).
                                   // The Simple Mode was the legacy/original way users logged into GIFT prior to the dashboard interface.
    }
    
    /* boolean to indicate if the Tui is running in 'embedded' mode.  Currently this is used
     * for the dashboard which will embed the tui in an 'iframe'.  It defaults to false.
     */
    private static boolean embeddedMode = false;

    /** Used for experiments */
    private static String experimentId = null;

    /** The path to the experiment folder */
    private static String experimentCoursePath = null;

    /** The URL to return to after the experiment ends */
    private static String experimentReturnUrl = null;

    /** whether the tutor is in debug mode.  This allows for logic such as color coding survey choices based on scoring */
    private static boolean debugMode = false;
    
    /** Indicates how the tutor should launch courses or initialize it's first web pages. */
    private static LaunchMode launchMode;
       
    /*
     * This is the listener to handle receiving of cross-domain messages from the dashboard (parent) while the
     * tui is running in an iframe within the dashboard.  It is only used while the Tui is in embedded mode. 
     */
    private EmbeddedIFrameListener listener = new EmbeddedIFrameListener();

    /**
     * Generates an error string for server errors
     *
     * @param action The action being done at the time of the error
     * @return String The error string
     */
    public static String generateServerError(String action) {
        return "While '" + action.toLowerCase() + "': " + SERVER_ERROR;
    }
    
    @Override
    public void onModuleLoad() {
        
        // This is the main entry point of the TUI client application.
        
        History.addValueChangeHandler(this);
        
        logger.info("tutoruserwebinterface onModuleLoad()");

        // Default to 'full screen mode'.  Other transition objects may set the document to sidebar mode,
        // but that will be transition dependent.
        Document.getInstance().fullScreenMode();       

        // parse any parameters that we're looking for.
        // The Dashboard can launch the TUI in 'embedded' mode now and the url for that will look something like:
        // http://localhost:8090/tutor?embed=true&username=john&password=pass&courseId=SomeCourseName
        // If the TUI receives this URL, we parse it for any parameters to see if it should be launched in embedded mode.
        final String embedMode = Window.Location.getParameter(DocumentUtil.EMBED_PARAMETER);
        experimentId = Window.Location.getParameter(ExperimentUrlManager.EXPERIMENT_ID_URL_PARAMETER);
        
        final String userPass = Window.Location.getParameter(DocumentUtil.PASSWORD_PARAMETER);
        final String userName = Window.Location.getParameter(DocumentUtil.USERNAME_PARAMETER);
        final String loginAsUsername = Window.Location.getParameter(DocumentUtil.LOGIN_AS_USERNAME_PARAMETER);
        final String userId   = Window.Location.getParameter(DocumentUtil.USERID_PARAMETER);
        final String courseRuntimeId = Window.Location.getParameter(DocumentUtil.COURSE_RUNTIME_ID_PARAMETER);
        final String courseSourceId = Window.Location.getParameter(DocumentUtil.COURSE_SOURCE_ID_PARAMETER);
        final String pingOnly = Window.Location.getParameter(DocumentUtil.PINGONLY_PARAMETER);
        final String offlineMode = Window.Location.getParameter(DocumentUtil.OFFLINE_PARAMETER);
        final String debugModeParam = Window.Location.getParameter(DocumentUtil.DEBUG_PARAMETER);
        final String userSessionId = Window.Location.getParameter(DocumentUtil.USER_SESSION_ID_PARAMETER);

        if(debugModeParam != null && Boolean.valueOf(debugModeParam)){
            logger.info("The tutor client is entering debug mode.");
            debugMode = true;
        }

        // Determine how the TUI is to be launched based on the settings.
        launchMode = initLaunchMode(embedMode, pingOnly, offlineMode);
        
        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("Tutor is initialized with launch mode: " + launchMode);
        }

        /* Reset experiment properties */
        if (launchMode == LaunchMode.EXPERIMENT_LAUNCH) {
            /* Can be null if using a legacy experiment or if accessing the
             * experiment through the tutor directly instead of dashboard */
            experimentCoursePath = courseRuntimeId;

            /* Can be null if the experiment is not part of a collection */
            experimentReturnUrl = Window.Location.getParameter(ExperimentUrlManager.RETURN_URL_PARAMETER);

            /* experimentId was set above */
        } else {
            logger.info("Resetting experiment values since we are not launching an experiment");

            /* Ensure values are nulled out */
            experimentCoursePath = null;
            experimentReturnUrl = null;
            experimentId = null;
        }

        // Set if the tui is running in embedded mode (meaning that it is embedded into the 'dashboard' as an iframe).
        try {
            embeddedMode = initEmbeddedMode();
        } catch (IllegalArgumentException e) {
            Document.getInstance().displayError("Initialization Error", e.getMessage());
            return;
        }

        switch (launchMode) {
        case NORMAL_DASHBOARD_LAUNCH:
            retrievePropertiesAndLaunchNormalCourse(userName, userPass, loginAsUsername, courseRuntimeId, courseSourceId);        
            break;
        case EXPERIMENT_LAUNCH:
            if (StringUtils.isNotBlank(userSessionId)) {
                History.newItem(buildUserSessionHistoryTag(userSessionId), false);
            }
            retrievePropertiesAndLaunchExperiment();
            break;
        case LTI_LAUNCH:
            retrievePropertiesAndLaunchLtiCourse(courseRuntimeId, courseSourceId);
            break;
        case OFFLINE_DASHBOARD_LAUNCH:
            retrievePropertiesAndLaunchOfflineCourse(userName, userId, courseRuntimeId, courseSourceId);
            break;
        case TUI_SIMPLE_LAUNCH:
            retrievePropertiesAndLaunchSimpleLoginPage();
            break;
        case PING_ONLY_LAUNCH:
            // This simply returns, and does nothing.
            // Ping only is used to ensure that the server is reachable (without doing any work yet).  
            // The url for "pingonly" (needs embedded set to true as well) looks like this:
            // http://localhost:8090/tutor?embed=true&pingOnly=true
            break;
        default:
            logger.severe("The TUI could not be loaded because the launch mode of (" + launchMode +"), is not supported.");
            Document.getInstance().displayError("Invalid Launch Mode", "The TUI could not be loaded because the launch mode of (" + launchMode +"), is not supported.");
        }
        
        
        
    }
    
    /**
     * Accessor to retrieve the launch mode flag.  This flag indicates how the tutor
     * should launch courses.  Can be null if it has not been set yet.
     * @return LaunchMode The current mode that the tutor should launch courses.
     */
    public LaunchMode getLaunchMode() {
        return launchMode;
    }
    
    /**
     * Inits the embedded mode flag which indicates the tutor is being run by the dashboard and is embedded into the webpage
     * as an iframe.  This must be called after the launch mode is determined since it depends on those settings.
     * @return
     * @throws IllegalArgumentException
     */
    private boolean initEmbeddedMode() throws IllegalArgumentException {
        boolean embedded = false;
        
        if (getLaunchMode() == null) {
            logger.severe("Unable to init embedded mode, launch mode must be determined first.");
            throw new IllegalArgumentException("Unable to init embedded mode, launch mode must be determined first, launch mode currently is null.");
        }

        switch (getLaunchMode()) {
        case TUI_SIMPLE_LAUNCH:
            embedded = false;
            break;
        case EXPERIMENT_LAUNCH:
            /* If a course path for the experiment exists, then it is embedded
             * in the dashboard */
            embedded = StringUtils.isNotBlank(getExperimentCoursePath());
            break;
        default:
            embedded = true;
        }

       return embedded;
    }

    /**
     * Determine the launch mode of the tutor.
     * 
     * @param embedMode - Flag to indicate if the tutor is embedded into the
     *        dashboard as an iframe.
     * @param pingOnly - Flag to indicate if the tutor is being 'pinged' only to
     *        see if the URL is reachable.
     * @param offlineMode - Flag to indiate of the tutor is in 'offline' mode
     *        meaning there is no external network available.
     * @return the {@link LaunchMode} type
     */
    private LaunchMode initLaunchMode(String embedMode, String pingOnly, String offlineMode) {

        /* DO NOT change the order of the if statements. They are in a precise
         * order to prevent duplicate checks. */

        final boolean isEmbedded = StringUtils.equalsIgnoreCase(embedMode, "true");
        final boolean isPingOnly = StringUtils.equalsIgnoreCase(pingOnly, "true");

        /* Check if the launch is actually just a ping, this should always be the first check */
        if (isEmbedded && isPingOnly) {
            /* This is a ping only check to see if the tutor is reachable. */
            return LaunchMode.PING_ONLY_LAUNCH;
        }

        /* If the launch if for an experiment, do not care about being embedded
         * or not. */
        if (isExperiment()) {
            /* This is an experiment launch */
            return LaunchMode.EXPERIMENT_LAUNCH;
        }

        /* Check if it is embedded. If it isn't, we can only launch the simple
         * TUI */
        if (!isEmbedded) {
            /* This means the tutor launches to a simple login page (this is the
             * legacy way the tutor was logged into prior to the dashboard. It
             * still can be used for research for logging in users by id on a
             * local network or other cases. */
            return LaunchMode.TUI_SIMPLE_LAUNCH;
        }

        /* Check if offline */
        if (StringUtils.equalsIgnoreCase(offlineMode, "true")) {
            /* This is an offline course launch (course launched in offline mode
             * - not connected to the network). This means user authentication
             * needs to be 'bypassed' since the GIFT Portal could not be reached
             * to login users. */
            return LaunchMode.OFFLINE_DASHBOARD_LAUNCH;
        }

        /* Check if LTI */
        final String consumerKey = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_KEY);
        final String consumerId = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_ID);
        if (StringUtils.isNotBlank(consumerKey) || StringUtils.isNotBlank(consumerId)) {
            /* This is an LTI launch (a course is being launched via LTI). */
            return LaunchMode.LTI_LAUNCH;
        }

        /* This is a normal course launch (course launched from the
         * dashboard). */
        logger.info("Setting launchMode to " + LaunchMode.NORMAL_DASHBOARD_LAUNCH + " because - embedMode = "
                + embedMode + ", pingOnly = " + pingOnly + ", offlineMode = " + offlineMode);
        return LaunchMode.NORMAL_DASHBOARD_LAUNCH;
    }

    /**
     * Retrieves the server properties and launch an LTI course.
     * 
     * @param courseRuntimeId The course runtime id for LTI.
     * @param courseSourceId The course source id for LTI.
     */
    private void retrievePropertiesAndLaunchLtiCourse(final String courseRuntimeId, final String courseSourceId) {
        final String consumerKey = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_KEY);
        final String consumerId = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_ID);
        // The data set id is optional and can be null or empty.
        final String dataSetId = Window.Location.getParameter(DocumentUtil.LTI_DATA_SET_ID);
        
        final String serviceUrl = Window.Location.getParameter(DocumentUtil.LTI_OUTCOME_SERVICE_URL);
        final String sourcedid = Window.Location.getParameter(DocumentUtil.LTI_OUTCOME_SERVICE_SOURCEDID);
        
        final LtiParameters params = new LtiParameters(consumerKey, consumerId, courseSourceId, dataSetId, serviceUrl, sourcedid);
        
        if (consumerKey == null || consumerKey.isEmpty()) {
            logger.severe("The course could not be started because the lti consumer key was not specified.");
            Document.getInstance().displayError("Invalid LTI Launch", "The course could not be started because the lti consumer key was not specified.");
            return;
        }
        
        if (consumerId == null || consumerId.isEmpty()) {
            logger.severe("The course could not be started because the lti consumer id was not specified.");
            Document.getInstance().displayError("Invalid LTI Launch", "The course could not be started because the lti consumer id was not specified.");
            return;
        }
        BrowserSession.retrieveServerProperties(new AsyncCallback<Boolean>() {
            
            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Unable to retrieve server properties", caught);
            }

            @Override
            public void onSuccess(Boolean isValid) {
                
                
                setupEmbeddedIFrameHandler();
                
                // Embedded mode should auto launch the course.
                ltiAutoLaunchCourse(params, courseRuntimeId);
                
                Window.setTitle(BrowserSession.getInstance().getWindowTitle());
            }
        });  
    }
    
    /**
     * Retrieve the server properties and launch a normal course meaning a normal launch from a user logged into the dashboard
     * and selecting and starting a course to run.
     * 
     * @param userName The username of the user logged into the dashboard.
     * @param userPass The password (encrypted) of the user logged into the dashboard.
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param courseRuntimeId The runtime id of the course selected.
     * @param courseSourceId The source course id of the course selected.
     */
    private void retrievePropertiesAndLaunchNormalCourse(final String userName, final String userPass, final String loginAsUserName,
                    final String courseRuntimeId, final String courseSourceId) {
        BrowserSession.retrieveServerProperties(new AsyncCallback<Boolean>() {
            
            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Unable to retrieve server properties", caught);
            }

            @Override
            public void onSuccess(Boolean isValid) {
                
                setupEmbeddedIFrameHandler();
                
                // Embedded mode should auto launch the course.
                autoLaunchCourse(userName, userPass, loginAsUserName, courseRuntimeId, courseSourceId);
                
                Window.setTitle(BrowserSession.getInstance().getWindowTitle());
            }
        });   
    }
    
    /**
     * Retrieve the server properties and launch a course in 'offline' mode.  This is used if there is no external network connection,
     * where GIFT can be running in offline mode.  The user can log into the dashboard (via a user id) and start a course.
     * 
     * @param username The name of the user that will be used for login & authentication.
     * @param userId The id of the user that will be used for login & authentication.
     * @param courseRuntimeId The runtime id of the course selected.
     * @param courseSourceId The source course id of the course selected.
     */
    private void retrievePropertiesAndLaunchOfflineCourse(final String username, final String userId, final String courseRuntimeId, final String courseSourceId) {
        BrowserSession.retrieveServerProperties(new AsyncCallback<Boolean>() {
            
            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Unable to retrieve server properties", caught);
            }

            @Override
            public void onSuccess(Boolean isValid) {
                
                setupEmbeddedIFrameHandler();
                
                // Embedded mode should auto launch the course.
                autoLaunchOfflineCourse(username, Integer.parseInt(userId), courseRuntimeId, courseSourceId);
                
                Window.setTitle(BrowserSession.getInstance().getWindowTitle());
            }
        });       
    }
    
    /**
     * Retrieve the server properties and load the simple loging page (for
     * Simple deployment mode of GIFT). This is used when the user logs directly
     * into the TUI. There is no dashboard running in the Gift Admin Server and
     * this is the legacy method of logging users in to run experiments. This
     * was the original method of logging into GIFT prior to when the dashboard
     * was implemented. In this flow the user sees the simple login page of the
     * TUI and from the simple login page, the user transitions to the
     * "SelectDomainWidget" which is the legacy screen for selecting courses
     * directly from the TUI.
     * 
     * This is still supported by GIFT, but has largely been replaced by GIFT's
     * Desktop/Server modes where the user signs into the 'Dashboard' interface
     * instead of directly to the TUI.
     */
    private void retrievePropertiesAndLaunchExperiment() {

        //
        // Retrieve server properties, then continue on to display the
        // appropriate webpage
        //
        BrowserSession.retrieveServerProperties(new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Unable to retrieve server properties", caught);
            }

            @Override
            public void onSuccess(Boolean isValid) {

                setupEmbeddedIFrameHandler();

                createInactiveSession();

                Window.setTitle(BrowserSession.getInstance().getWindowTitle());
            }
        });
    }

    /**
     * Retrieve the server properties and load the simple loging page (for Simple deployment mode of GIFT).
     * This is used when the user logs directly into the TUI.  There is no dashboard running in the Gift Admin
     * Server and this is the legacy method of logging users in to run experiments.  This was the original method
     * of logging into GIFT prior to when the dashboard was implemented.  In this flow the user sees the simple login
     * page of the TUI and from the simple login page, the user transitions to the "SelectDomainWidget" which is the legacy
     * screen for selecting courses directly from the TUI.  
     * 
     * This is still supported by GIFT, but has largely been replaced by GIFT's Desktop/Server modes where the user signs
     * into the 'Dashboard' interface instead of directly to the TUI.
     */
    private void retrievePropertiesAndLaunchSimpleLoginPage() {
        
        setupEmbeddedIFrameHandler();
        
        //
        // Retrieve server properties, then continue on to display the appropriate webpage
        //
        BrowserSession.retrieveServerProperties(new AsyncCallback<Boolean>() {
            
            @Override
            public void onFailure(Throwable caught) {
                Document.getInstance().displayRPCError("Unable to retrieve server properties", caught);
            }

            @Override
            public void onSuccess(Boolean isValid) {
                
                if (isValid) {
                    
                    String userSessionKey = BrowserProperties.getInstance().getUserSessionKey();
                    if (userSessionKey != null && !userSessionKey.isEmpty()) {
                        logger.fine("Using user session ID in cookie: " + userSessionKey);
                        BrowserSession.resumeUserSession(userSessionKey, new AsyncCallback<Boolean>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                createInactiveSession();
                                Document.getInstance().displayRPCError("Resuming a user session", caught);
                            }

                            @Override
                            public void onSuccess(Boolean isValid) {
                                
                                if (isValid) {
                                    
                                    logger.info("resumeUserSession returned success. ");
                                    String currentToken = History.getToken();
                                    if (currentToken.isEmpty()) {
                                        displayPage(SELECT_DOMAIN_TAG);
                                    } else {
                                        boolean handled = displayPage(currentToken);
                                        if (!handled) {
                                            displayPage(SELECT_DOMAIN_TAG);
                                        }
                                    }
                                } else {
                                    createInactiveSession();
                                }
                            }
                        });
                    } else {
                        createInactiveSession();
                    }
                    
                }else{
                    Document.getInstance().displayError("Retrieving Server Properties", "Failed to retrieve server properties");    
                }
                
                Window.setTitle(BrowserSession.getInstance().getWindowTitle());
            }
        });
    }
    
    /**
     * Sets up the embedded iframe handler and signals that the tui is ready.
     */
    private void setupEmbeddedIFrameHandler() {
        logger.info("setupEmbeddedIFrameHandler()");
        // Setup the iframe child message handler (allows 2 way communication from tui and dashboard).
        IFrameMessageHandlerChild.getInstance().init();
        IFrameMessageHandlerChild.getInstance().addMessageListener(listener);
        
        IFrameSimpleMessage tuiReadyMsg = new IFrameSimpleMessage(IFrameMessageType.TUI_READY);
        IFrameMessageHandlerChild.getInstance().sendMessage(tuiReadyMsg);
    }

    public void createInactiveSession() {
        BrowserSession.invalidate();
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        
        logger.fine("TUI history change requested to: " + event.getValue());
        
        if(Document.getInstance().getArticleWidget() instanceof IsRemovableTuiWidget) {
            
            //if necessary, notify the current article widget that it is being removed by the user, not GIFT
            ((IsRemovableTuiWidget) Document.getInstance().getArticleWidget()).onRemoval(false);
        }
        
        displayPage(event.getValue());
    }

    /**
     * Displays a page in the browser
     *
     * @param tag The tag of the page to display
     * @return boolean If any action was taken
     */
    public boolean displayPage(String tag) {

        if (LogConfiguration.loggingIsEnabled(Level.INFO)) {
            logger.info("displayPage: " + tag);
        }
        
        
        BrowserSession instance = BrowserSession.getInstance();
        if (instance != null) {
            if (tag.equalsIgnoreCase(SELECT_DOMAIN_TAG)) {
                instance.displaySelectDomainWidget();
                return true;
            } else if (tag.equalsIgnoreCase(DOMAIN_SESSION_PAGE_TAG)) {
                instance.resumeDomainSession();
                return true;
            } else if (tag.equalsIgnoreCase(EXPERIMENT_PAGE_TAG) && isExperiment()) {
                instance.displayExperimentWelcomeWidget();
                return true;
            } else {
                return false;
            }
        } else {
            if (tag.equalsIgnoreCase(SIMPLE_LOGIN_PAGE_TAG)) {
                try {                   
                    //BrowserSession.getInstance().doAction(new CloseAction(), null);
                    Document.getInstance().setArticleWidget(WidgetFactory.createWidgetType(WidgetTypeEnum.SIMPLE_LOGIN_WIDGET));
                } catch (@SuppressWarnings("unused") Exception ex) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Auto launches the course for an lti user.
     * @param params LTI parameters used to login and start the course.
     * @param courseRuntimeId The path of the runtime course to be loaded.
     */
    private void ltiAutoLaunchCourse(LtiParameters params, String courseRuntimeId)  {
        logger.info("ltiAutoLaunchCourse() called with params: " + params + ", course=" + courseRuntimeId);
        
        BrowserSession.ltiLoginAndStartCourse(params, courseRuntimeId, new AsyncCallback<RpcResponse>() {
            
            @Override
            public void onFailure(Throwable caught) {
                logger.severe("ltiAutoLaunchCourse caught failure: " + caught.getMessage());
                if(caught instanceof StatusCodeException && ((StatusCodeException)caught).getStatusCode() == 0) {
                    /* HTTPStatusEvent with a status code of 0 are reported by FireFox 
                     * whenever the browser page is refreshed, triggering this onFailure method.
                     * Temporary solution is to catch it and ignore it for now. 
                     * (https://gifttutoring.org/issues/1856) */
                } else {
                    logger.severe("Server exception occurred when starting the course: " + caught.toString());

                    if(BrowserSession.getInstance().isServerDeploymentMode()){
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course (server deployment mode). Please try again to see if the problem persists."+
                        "If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the browser developer console for more error details</li>"+
                                  "<li>If this is the first course taken on your GIFT server and you have access to the GIFT configuration files,"+
                                  "make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", caught.toString());
                    }else{
                        
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course. There are several things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the browser developer console for more error details</li>"+
                                "<li>Check the tutor and domain module logs for useful errors</li>"+
                                  "<li>Make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", caught.toString());

                    }
                }
            }
            
            @Override
            public void onSuccess(RpcResponse result) {
                
                logger.info("ltiAutoLaunchCourse returned: " + result);
                if (result.isSuccess()) {
                    // course should load.
                } else {
                    logger.severe("Server failure occurred when starting the course -"+result);

                    
                    StringBuilder error = new StringBuilder("<b>Server Response:</b><br/>");
                    String response = result.getResponse() == null ? "" : ("<br/>Error details: " + result.getResponse());
                    String information = result.getAdditionalInformation() == null ? "" : ("<br/>" + result.getAdditionalInformation());
                    
                    error.append(response);
                    error.append(information);
                    
                    if(BrowserSession.getInstance().isServerDeploymentMode()){
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course (server deployment mode). Please try again to see if the problem persists."+
                        "If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the browser developer console for more error details</li>"+
                                  "<li>If this is the first course taken on your GIFT server and you have access to the GIFT configuration files,"+
                                  "make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", error.toString());
                    }else{
                        
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course. Please try again to see if the problem persists."+
                        "If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the browser developer console for more error details</li>"+
                                "<li>Check the tutor and domain module logs for useful errors</li>"+
                                  "<li>Make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", error.toString());

                    }
                }
                
            }
        });

    }
    
    
    /**
     * This method is used when the TUI is in embedded mode and is used to 'auto start' the course by logging in the user,
     * getting the course listing, and selecting a course.  It bypasses the UI needed to achieve this.
     * 
     * @param userName - The name of the user that will be used for login & authentication.
     * @param password - The password of the user that will be used for login & authentication.
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     */
    private void autoLaunchCourse(String userName, String password, String loginAsUserName, String courseRuntimeId, String courseSourceId)  {
        
        logger.info("autoLaunchCourse() called.");
        BrowserSession.startOrResumeCourse(userName, password, loginAsUserName, courseRuntimeId, courseSourceId, new AsyncCallback<RpcResponse>() {
            
            @Override
            public void onFailure(Throwable caught) {
                logger.info("autoLaunchCourse onFailure(): " + caught);
                if(caught instanceof StatusCodeException && ((StatusCodeException)caught).getStatusCode() == 0) {
                    /* HTTPStatusEvent with a status code of 0 are reported by FireFox 
                     * whenever the browser page is refreshed, triggering this onFailure method.
                     * Temporary solution is to catch it and ignore it for now. 
                     * (https://gifttutoring.org/issues/1856) */
                } else {
                    logger.severe("Server exception occurred when starting the course: " + caught.toString());

                    if(BrowserSession.getInstance().isServerDeploymentMode()){
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course (server deployment mode). Please try again to see if the problem persists."+
                        "<br/><br/>If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                                  "<li>If this is the first course taken on your GIFT server and you have access to the GIFT configuration files,"+
                                  "make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", caught.toString());
                    }else{
                        
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course. There are several things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                                "<li>Check the tutor and domain module logs for useful errors</li>"+
                                  "<li>Make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", caught.toString());

                    }
                }
            }
            
            @Override
            public void onSuccess(RpcResponse result) {
                
                logger.info("autoLaunchCourse onSuccess(): " + result);
                
                if (result == null) {
                    logger.severe("Server failure occurred when starting the course -"+result);
                    Document.getInstance().displayErrorDialog("Unable to start course", 
                            "The Tutor server threw an exception when trying to start the course. Please try again to see if the problem persists."+
                    "<br/><br/>If it happens again there are a few things you can check "
                            + "to help determine the cause:<br/>"+
                              "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                            "<li>Check the tutor and domain module logs for useful errors</li>"+
                              "<li>Make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", "A null response was returned from the server.");
                    return;
                }
                
                
                if (result.isSuccess()) {
                    // course should load.
                } else {
                    logger.severe("Server failure occurred when starting the course -"+result);

                    
                    StringBuilder error = new StringBuilder("<b>Server Response:</b><br/>");
                    String response = result.getResponse() == null ? "" : ("<br/>Error details: " + result.getResponse());
                    String information = result.getAdditionalInformation() == null ? "" : ("<br/>" + result.getAdditionalInformation());
                    
                    error.append(response);
                    error.append(information);
                    
                    if(BrowserSession.getInstance().isServerDeploymentMode()){
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course (server deployment mode). Please try again to see if the problem persists."+
                        "<br/><br/>If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                                  "<li>If this is the first course taken on your GIFT server and you have access to the GIFT configuration files,"+
                                  "make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", error.toString());
                    }else{
                        
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course. Please try again to see if the problem persists."+
                        "<br/><br/>If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                                "<li>Check the tutor and domain module logs for useful errors</li>"+
                                  "<li>Make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", error.toString());

                    }
                }
                
            }
        });

    }
    
    /**
     * This method is used when the TUI is in embedded mode and is used to 'auto start' the course by logging in the user,
     * getting the course listing, and selecting a course.  It bypasses the UI needed to achieve this.
     * 
     * @param username The name of the user that will be used for login & authentication.
     * @param userId The id of the user that will be used for login & authentication.
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     */
    private void autoLaunchOfflineCourse(String username, int userId, String courseRuntimeId, String courseSourceId)  {
        
        BrowserSession.loginAndStartOfflineCourse(username, userId, courseRuntimeId, courseSourceId, new AsyncCallback<RpcResponse>() {
            
            @Override
            public void onFailure(Throwable t) {
                
                logger.severe("Server exception occurred when starting the course (offline): " + t.toString());

                if(BrowserSession.getInstance().isServerDeploymentMode()){
                    Document.getInstance().displayErrorDialog("Unable to start course", 
                            "The Tutor server threw an exception when trying to start the course (offline - server deployment mode). Please try again to see if the problem persists."+
                    "<br/><br/>If it happens again there are a few things you can check "
                            + "to help determine the cause:<br/>"+
                              "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                              "<li>If this is the first course taken on your GIFT server and you have access to the GIFT configuration files,"+
                              "make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", t.toString());
                }else{
                    
                    Document.getInstance().displayErrorDialog("Unable to start course", 
                            "The Tutor server threw an exception when trying to start the course (offline). There are several things you can check "
                            + "to help determine the cause:<br/>"+
                              "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                            "<li>Check the tutor and domain module logs for useful errors</li>"+
                              "<li>Make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", t.toString());

                }

            }
            
            @Override
            public void onSuccess(RpcResponse result) {
                
                if (result.isSuccess()) {
                    
                    // course should load.
                    IFrameSimpleMessage courseStartingMsg = new IFrameSimpleMessage(IFrameMessageType.COURSE_STARTING);
                    IFrameMessageHandlerChild.getInstance().sendMessage(courseStartingMsg);
                    
                } else {
                    logger.severe("Server failure occurred when starting the course -"+result);

                    
                    StringBuilder error = new StringBuilder("<b>Server Response:</b><br/>");
                    String response = result.getResponse() == null ? "" : ("<br/>Error details: " + result.getResponse());
                    String information = result.getAdditionalInformation() == null ? "" : ("<br/>" + result.getAdditionalInformation());
                    
                    error.append(response);
                    error.append(information);
                    
                    if(BrowserSession.getInstance().isServerDeploymentMode()){
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course (offline - server deployment mode). Please try again to see if the problem persists."+
                        "<br/><br/>If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                                  "<li>If this is the first course taken on your GIFT server and you have access to the GIFT configuration files,"+
                                  "make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", error.toString());
                    }else{
                        
                        Document.getInstance().displayErrorDialog("Unable to start course", 
                                "The Tutor server threw an exception when trying to start the course (offline). Please try again to see if the problem persists."+
                        "<br/><br/>If it happens again there are a few things you can check "
                                + "to help determine the cause:<br/>"+
                                  "<ol><li>Check the error details below and the browser developer console for more information.</li>"+
                                "<li>Check the tutor and domain module logs for useful errors</li>"+
                                  "<li>Make sure your TutorURL property is set correctly in GIFT/config/common.properties</li></ol>", error.toString());

                    }
                }
                
            }
        });

    }

    /**
     * Gets the ID of the experiment being run, if applicable
     * 
     * @return the ID of the experiment being run. Null, if no experiment is
     *         being run.
     */
    public static String getExperimentId() {
        return experimentId;
    }

    /**
     * Gets the runtime course folder of the experiment being run, if applicable
     * 
     * @return the runtime folder of the experiment being run. Null, if no
     *         experiment is being run.
     */
    public static String getExperimentCoursePath() {
        return experimentCoursePath;
    }

    /**
     * Gets the experiment's return URL, if applicable.
     * 
     * @return the URL to return to after the experiment ends. Null, if the
     *         experiment does not have a specific return URL.
     */
    public static String getExperimentReturnUrl() {
        return experimentReturnUrl;
    }

    /**
     * Check if an experiment is being run.
     * 
     * @return true if an experiment is being run; false otherwise.
     */
    public static boolean isExperiment() {
        return StringUtils.isNotBlank(getExperimentId());
    }

    /**
     * Check if the application is embedded.
     * 
     * @return true if it is embedded; false otherwise.
     */
    public static boolean isEmbedded() {
        return embeddedMode;
    }

    /**
     * Return whether the tutor is in debug mode.  This allows for logic such as color coding survey choices based on scoring.
     * 
     * @return false by default
     */
    public static boolean isDebugMode(){
        return debugMode;
    }

    /**
     * Builds the user session history tag with the provided user session key.
     * 
     * @param userSessionKey the key to use while building the use session
     *        history tag
     * @return the user session history tag
     */
    public static String buildUserSessionHistoryTag(String userSessionKey) {
        return DOMAIN_SESSION_PAGE_TAG + USER_SESSION_ID + userSessionKey;
    }
}

