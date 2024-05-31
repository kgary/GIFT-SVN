/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;



import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.WebDeviceUtils;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.gwt.shared.ExperimentParameters;
import mil.arl.gift.common.gwt.shared.LtiParameters;
import mil.arl.gift.common.io.ExperimentUrlManager;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.CourseEndingInterface;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.HistoryManager;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.UiManager.EndCourseReason;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;

/**
 * The BsCourseRuntimeWidget embeds the TUI interface into an iframe and opens up cross-domain communication between the 
 * dashboard host and the tui host.  The widget will show a 'loading' panel until a message is received from the tui
 * indicating that the course has loaded.
 *
 * @author nblomberg
 */
public class BsCourseRuntimeWidget extends AbstractBsWidget implements CourseEndingInterface {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(BsCourseRuntimeWidget.class.getName());
    
    private static BsCourseRuntimeWidgetUiBinder uiBinder = GWT.create(BsCourseRuntimeWidgetUiBinder.class);
    
    /** Default time in seconds to allow the tutor to start a course before the dashboard terminates the session */
    private static final int DEFAULT_TUTOR_START_COURSE_TIMEOUT = 10;
    
    private static final String URL_PARAM_DELIM = "&";
    private static final String EQUALS = "=";
    
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String BORDER = "border";
    private static final String DISPLAY = "display";  
    private static final String ALLOW   = "allow";
    
    private static final String DEFAULT_WIDTH = "100%";
    private static final String DEFAULT_HEIGHT = "100%";
    private static final String DEFAULT_BORDER = "none";
    private static final String DEFAULT_DISPLAY = "block";
    private static final String AUTOPLAY = "autoplay";
    
    private final String RUNTIME_EXCEPTION_MSG = "The course was not able to be started due to an exception that was caught trying to start the course.";
    private final String RUNTIME_CANNOT_CONNECT_MSG = "Failure connecting to the GIFT runtime engine.";
    private final String RUNTIME_INVALID_COURSEDATA = "Could not start the course due to invalid course data.";
    private final String RUNTIME_ERROR_TITLE = "UNABLE TO START COURSE";
    private final String LOADING_MESSAGE = "The course is loading. Please wait.";
    private final String ENDING_MESSAGE = "The course is ending. Please wait.";
    
    @UiField
    Container ctrlLoadPanel;
    
    @UiField
    BsLoadingIcon ctrlLoadIcon;
    
    @UiField
    Container iFrameCtrl;
    
    @UiField
    SimplePanel frameContainer;
    
    @UiField
    Heading waitMessage;
    
    
    /**
     * Callback class that is used to handle errors from the tui.  In all cases, if we get an error 
     * from the tui, we want to return the users back to the "My Course" panel.
     * 
     */
    protected DialogCallback courseErrorCallback = new DialogCallback() {

        @Override
        public void onAccept() {
            UiManager.getInstance().displayScreen(ScreenEnum.MYCOURSES);
            
        }
        
    };
    
    interface BsCourseRuntimeWidgetUiBinder extends UiBinder<Widget, BsCourseRuntimeWidget> {
    }
    
    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    protected final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    /**
     * Constructor for the widget.
     * 
     * @param loadCourseParameters - contains parameters needed to load a GIFT course
     * @param offlineMode - true if the dashboard is in offline mode, false otherwise.
     * @param tutorStartCourseTimeoutProperty the number of seconds before the dashboard times out when the tutor is starting
     * a course. Can be null, in which case a local default value will be used.
     */
    public BsCourseRuntimeWidget(Object loadCourseParameters, final boolean offlineMode, final String tutorStartCourseTimeoutProperty) {
        
        initWidget(uiBinder.createAndBindUi(this));
        
        waitMessage.setText(LOADING_MESSAGE);
        if (loadCourseParameters != null  && loadCourseParameters instanceof LoadCourseParameters) {
            
            final LoadCourseParameters loadCourseParams = (LoadCourseParameters)loadCourseParameters;
            final String courseRuntimeId = loadCourseParams.getCourseRuntimeId();
            final String courseSourceId = loadCourseParams.getCourseSourceId();
            
            ctrlLoadPanel.setVisible(true);
            ctrlLoadIcon.startLoading();
            
            iFrameCtrl.setVisible(false);
            
            UiManager.getInstance().fillToBottomOfViewport(frameContainer);
            
            // Get the tui origin url.
            final String tuiOrigin = UiManager.getInstance().getTuiOrigin();
            //error message used when the tutorURL needs to be edited in common.properties
            final String tutorURLCheck = "The course cannot be started because a connection to the Tutor could not be established. "
                    + "<br><br>First try the course again. If that doesn't work than it maybe that the Tutor URL is not set correctly. If you have access to the GIFT configuration files then please try the following: "
                    + "<br><br>1) Open <i>GIFT\\config\\common.properties</i> for editing."
                    + "<br>2) Check the value of the 'TutorURL' property which should be the IP address of the computer running the Tutor module."
                    + "<br><br>Current TutorURL on GIFT host = " + tuiOrigin
                    + (tuiOrigin.contains("localhost") ? "<br><br>An example of a TutorURL with IP address is: http://38.1.53.40:8090/tutor<br><br>"
                            + "The last resort is to restart GIFT (if possible) and try the course again as GIFT is most likely in a bad state at this point.</html>" : "");
          
            // First we need to "ping" the server to ensure that it can be reached.  If not, we return an error.
            String iFramePingUrl = tuiOrigin + "/?" + DocumentUtil.EMBED_PARAMETER + "=true&"
                    + DocumentUtil.PINGONLY_PARAMETER + "=true";
            iFramePingUrl = UriUtils.encode(iFramePingUrl);
            dashboardService.isUrlReachable(iFramePingUrl, new AsyncCallback<RpcResponse>() {

                @Override
                public void onFailure(Throwable t) {
                    ctrlLoadIcon.stopLoading();
                    ctrlLoadPanel.setVisible(false);
                    logger.severe(RUNTIME_EXCEPTION_MSG + " Exception was: " + t.getMessage());                 
                    
                    if (isExperimentOrLti()) {
                        // EXPERIMENT OR LTI MODE
                        UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, new DashboardErrorWidget.ErrorMessage(
                                RUNTIME_ERROR_TITLE, RUNTIME_EXCEPTION_MSG + " Exception was: " + t.getMessage(), null
                        ));
                        
                    } else {
                        // NORMAL MODE
                        UiManager.getInstance().displayErrorDialog(RUNTIME_ERROR_TITLE, RUNTIME_EXCEPTION_MSG + " Exception was: " + t.getMessage(), courseErrorCallback);
                    }
                }

                @Override
                public void onSuccess(RpcResponse result) {
                    
                    if (result.isSuccess()) {
                        
                        // Reset the flag to indicate the the tui is ready.
                        UiManager.getInstance().setTuiReady(false);
                        
                        // Construct the iFrameUri from the dashboard parameters.
                        final String tuiFrameId = UiManager.getInstance().getTuiFrameId();
                        String candidateUserName = UiManager.getInstance().getUserName(); //candidate because this value might be the 'login as' username
                        String userPassword = UiManager.getInstance().getUserPassword();
                        final Integer userId = UiManager.getInstance().getUserId();
                        String actualUserName = UiManager.getInstance().getActualUserName();
                        
                        //determine the real user name of the user starting a course
                        String userName;
                        if(actualUserName != null){
                            userName = actualUserName;  //this user is using a 'login as' username, actual user's username is stored in this other attribute
                        }else{
                            userName = candidateUserName;
                        }
                        
                        // Encode username & password since they may contain special characters which will be decoded by the TutorUserWebInterface.
                        userName = URL.encodeQueryString(userName);
                        
                        /* When using SSO, the password will be null, so skip encoding if SSO is being used */
                        if(userPassword != null) {
                            userPassword = URL.encodeQueryString(userPassword);
                        }
                        
                        candidateUserName = URL.encodeQueryString(candidateUserName);  //it maybe used again below
                        
                        Boolean offlineModeBool = offlineMode;
                        // Note we construct the url here to be formatted properly for html (eg "&amp;" tags) 
                        StringBuffer iFrameUrl = new StringBuffer();
                        iFrameUrl.append(tuiOrigin).append("/?").append(DocumentUtil.EMBED_PARAMETER).append(EQUALS).append("true")
                            .append(URL_PARAM_DELIM).append(DocumentUtil.OFFLINE_PARAMETER).append(EQUALS).append(offlineModeBool.toString()) 
                            .append(URL_PARAM_DELIM).append(DocumentUtil.USERID_PARAMETER).append(EQUALS).append(userId.toString()) 
                            .append(URL_PARAM_DELIM).append(DocumentUtil.USERNAME_PARAMETER).append(EQUALS).append(userName); 
                        
                        /* When using SSO, the password will be null, so don't add the password parameter if SSO is being used */
                        if(userPassword != null) {
                            iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.PASSWORD_PARAMETER).append(EQUALS).append(userPassword);
                        }  
                            
                        iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.COURSE_RUNTIME_ID_PARAMETER).append(EQUALS).append(
                                    courseRuntimeId != null ? URL.encodeQueryString(courseRuntimeId) : null)
                            .append(URL_PARAM_DELIM).append(DocumentUtil.COURSE_SOURCE_ID_PARAMETER).append(EQUALS).append(
                                    courseSourceId != null ? URL.encodeQueryString(courseSourceId) : null)
                            .append(URL_PARAM_DELIM).append(DocumentUtil.DEBUG_PARAMETER).append(EQUALS).append(UiManager.getInstance().isDebugMode());
                        
                        String mobileAppDetails = WebDeviceUtils.getMobileAppEmbeddedURLParam();
                        if(mobileAppDetails != null) {
                            
                            //if the Dashboard is embedded in the GIFT mobile app, tell the TUI via URL parameter
                            iFrameUrl.append(URL_PARAM_DELIM).append(mobileAppDetails);
                        }
                        
                        //make sure to add the 'login as' username so the tutor knows that it should assume that user's identity after authenticating
                        //the actual user that is logged in and taking the course
                        if(actualUserName != null){
                            iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.LOGIN_AS_USERNAME_PARAMETER).append(EQUALS).append(candidateUserName);
                        }
                        
                        /* Can be an experiment or LTI, but not both */
                        if (loadCourseParams.getExperimentParameters() != null) {
                            ExperimentParameters expParams = loadCourseParams.getExperimentParameters();
                            iFrameUrl.append(URL_PARAM_DELIM).append(ExperimentUrlManager.EXPERIMENT_ID_URL_PARAMETER)
                                    .append(EQUALS).append(URL.encodeQueryString(expParams.getExperimentId()));

                            if (StringUtils.isNotBlank(expParams.getReturnUrl())) {
                                iFrameUrl.append(URL_PARAM_DELIM).append(ExperimentUrlManager.RETURN_URL_PARAMETER)
                                        .append(EQUALS).append(URL.encodeQueryString(expParams.getReturnUrl()));
                            }
                            if (StringUtils.isNotBlank(expParams.getUserSessionId())) {
                                HistoryManager.getInstance().addHistory(
                                        HistoryManager.buildUserSessionHistoryTag(expParams.getUserSessionId()));

                                iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.USER_SESSION_ID_PARAMETER)
                                        .append(EQUALS).append(URL.encodeQueryString(expParams.getUserSessionId()));
                            }
                        } else if (loadCourseParams.getLtiParameters() != null) {

                            LtiParameters ltiParams = loadCourseParams.getLtiParameters();
                            // Add the parameters for the lti launch
                            iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.LTI_CONSUMER_KEY).append(EQUALS)
                                .append(URL.encodeQueryString(ltiParams.getConsumerKey()))
                                .append(URL_PARAM_DELIM).append(DocumentUtil.LTI_CONSUMER_ID).append(EQUALS).append(URL.encodeQueryString(ltiParams.getConsumerId()));
                            
                            final String ltiDataSetId = ltiParams.getDataSetId();
                            if (ltiDataSetId != null && !ltiDataSetId.isEmpty()) {
                                iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.LTI_DATA_SET_ID).append(EQUALS).append(URL.encodeQueryString(ltiDataSetId));
                            }
                            
                            // These parameters are optional.  Both parameters must be passed in if 
                            // GIFT will provide a score back to the Tool Consumer.
                            final String serviceUrl = ltiParams.getLisServiceUrl();
                            final String sourcedid = ltiParams.getLisSourcedid();
                            
                            if (serviceUrl != null && !serviceUrl.isEmpty() && 
                                    sourcedid != null && !sourcedid.isEmpty()) {
                                iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.LTI_OUTCOME_SERVICE_URL).append(EQUALS).append(URL.encodeQueryString(serviceUrl));
                                iFrameUrl.append(URL_PARAM_DELIM).append(DocumentUtil.LTI_OUTCOME_SERVICE_SOURCEDID).append(EQUALS).append(URL.encodeQueryString(sourcedid));
                            }
                        }

                        Frame tuiFrame = new Frame();
                              
                        tuiFrame.getElement().getStyle().setProperty(WIDTH, DEFAULT_WIDTH);
                        tuiFrame.getElement().getStyle().setProperty(HEIGHT, DEFAULT_HEIGHT);
                        tuiFrame.getElement().getStyle().setProperty(BORDER, DEFAULT_BORDER);
                        tuiFrame.getElement().getStyle().setProperty(DISPLAY, DEFAULT_DISPLAY);
                        tuiFrame.getElement().setAttribute(ALLOW, AUTOPLAY);
                        tuiFrame.getElement().setId(tuiFrameId);
                        tuiFrame.getElement().setAttribute("allowfullscreen", "");
                        
                        frameContainer.setWidget(tuiFrame);

                        //at the end of the timer, if the iFrameCtrl is still not visible, this is most likely cause by
                        //"localhost" being used in the tutorURL while trying to connect from a different computer. 
                        //We should present the same error as if an incorrect URL was used. 
                        final Timer tutorUrlTimer = new Timer() {

                            @Override
                            public void run() {
                                logger.info("Timer expired...checking if tui is ready: " + UiManager.getInstance().isTuiReady());
                                if(!UiManager.getInstance().isTuiReady()){
                                    ctrlLoadIcon.stopLoading();
                                    ctrlLoadPanel.setVisible(false);
                                    logger.warning(RUNTIME_CANNOT_CONNECT_MSG + " Due to a timeout, could be caused by connecting to a host machine with 'localhost' in the host's tutorURL");
                                    
                                    if (isExperimentOrLti()) {
                                        // EXPERIMENT OR LTI MODE
                                        UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, new DashboardErrorWidget.ErrorMessage(
                                                RUNTIME_ERROR_TITLE, tutorURLCheck, null
                                        ));
                                        
                                    } else {
                                        // NORMAL MODE
                                        UiManager.getInstance().displayErrorDialog(RUNTIME_ERROR_TITLE, tutorURLCheck, courseErrorCallback);
                                    }
                                }
                            }
                        };                        
                        
                        int tutorStartCourseTimeout = DEFAULT_TUTOR_START_COURSE_TIMEOUT;
                        if(tutorStartCourseTimeoutProperty != null){
                            try {
                                tutorStartCourseTimeout = Integer.parseInt(tutorStartCourseTimeoutProperty);
                            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                                //use local default value 
                            }
                        }

                        final int finalTutorStartCourseTimeout = tutorStartCourseTimeout;
                        tuiFrame.addLoadHandler(new LoadHandler() {

                            @Override
                            public void onLoad(LoadEvent event) {
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine("onLoad(" + event.toDebugString() + ")");
                                }

                                tutorUrlTimer.schedule(finalTutorStartCourseTimeout * 1000);
                        UiManager.getInstance().setTuiIFrameTimer(tutorUrlTimer);
                            }
                        });
                        
                        tuiFrame.setUrl(iFrameUrl.toString());

                    } else {
                        ctrlLoadIcon.stopLoading();
                        ctrlLoadPanel.setVisible(false);
                        logger.warning(RUNTIME_CANNOT_CONNECT_MSG + " Response is: " + result);
                        
                        if (isExperimentOrLti()) {
                            // EXPERIMENT OR LTI MODE
                            UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, new DashboardErrorWidget.ErrorMessage(
                                    RUNTIME_ERROR_TITLE, tutorURLCheck, null
                            ));
                            
                        } else {
                            // NORMAL MODE
                            UiManager.getInstance().displayErrorDialog(RUNTIME_ERROR_TITLE, tutorURLCheck, courseErrorCallback);
                        }
                        
                    }
                    
                }
                
            });
            
        } else {
            logger.severe(RUNTIME_INVALID_COURSEDATA + " load course parameters is: " + loadCourseParameters);
            
            if (isExperimentOrLti()) {
                // EXPERIMENT OR LTI MODE
                UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, new DashboardErrorWidget.ErrorMessage(
                        RUNTIME_ERROR_TITLE, RUNTIME_INVALID_COURSEDATA, null
                ));
                
            } else {
                // NORMAL MODE
                UiManager.getInstance().displayErrorDialog(RUNTIME_ERROR_TITLE, RUNTIME_INVALID_COURSEDATA, courseErrorCallback);
            }
        }

    } 

    /**
     * Checks if the experiment id or LTI consumer key is located as a window
     * parameter.
     *
     * @return true if the window parameters contain the experiment id or LTI
     *         consumer key.
     */
    private boolean isExperimentOrLti() {
        final String experimentId = Window.Location.getParameter(ExperimentUrlManager.EXPERIMENT_ID_URL_PARAMETER);
        final String consumerKey = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_KEY);

        return StringUtils.isNotBlank(experimentId) || StringUtils.isNotBlank(consumerKey);
    }

    /**
     * Set the visibility of the iFrame (can be used to hide until it is completed loading the course.
     * 
     * @param visible - true if the iframe is shown, false to hide the iframe.
     */
    public void setIFrameVisibility(boolean visible) {
        
        iFrameCtrl.setVisible(visible);
        
        if (visible) {
            UiManager.getInstance().cancelTutorIFrameTimer();
            ctrlLoadIcon.stopLoading();
            ctrlLoadPanel.setVisible(false);
        } 
    }


    @Override
    public void handleCourseEnding() {
        logger.info("handleCourseEnding()");
        UiManager.getInstance().cancelTutorIFrameTimer();

        setIFrameVisibility(false);
        
        waitMessage.setText(ENDING_MESSAGE);
        ctrlLoadPanel.setVisible(true);
        ctrlLoadIcon.startLoading();
        
    }
    

    @Override
    public void handleCourseEndingTimerExpired(EndCourseReason reason) {
        // The screen will be unloaded by the UIManager, so there's nothing to do here.
        logger.info("handleCourseEndingTimerExpired(): " + reason);
    }


    @Override
    public void handleCourseEnded(EndCourseReason reason) {
        // The screen will be unloaded by the UIManager, so there's nothing to do here.
        logger.info("handleCourseEnded(): " + reason);
    }
    
    /*
     * Nick: This method exists specifically to address a longstanding issue with all of our frame elements and
     * Internet Explorer 9, 10, and 11. The gist of the problem is that IE doesn't properly clean up a frame's elements after it is 
     * removed from the DOM. Because of this, if the user edits a field in a frame and then removes the frame from the DOM, IE's 
     * focus stays on the text box they edited, preventing ANY other text fields throughout the entire interface from being edited. 
     * 
     * To prevent this from happening, I'm using a 2 step approach:
     * 1) Remove the frame's inner HTML from the DOM
     *      - This causes the DOM to remove the textbox the user was editing
     * 2) Change the frame's URL so that it's current DOM is unloaded
     *      - This ensures that the frame's old DOM doesn't keep have the page's focus
     * 
     * All of these steps together seem to handle all cases of this problem occurring.
     * 
     * For more info on this problem, check out 
     * http://stackoverflow.com/questions/19150008/ie-9-and-ie-10-cannot-enter-text-into-input-text-boxes-from-time-to-time
     */
    public void unlockFrameTextBoxes(){
        
        Widget widget = frameContainer.getWidget();
        
        if (widget != null && widget instanceof Frame) {
            
            Frame frame = (Frame)widget;
            
            //need to reset URL and inner HTML to reset the iframe's state
            frame.getElement().setInnerHTML("");
            frame.setUrl("about:blank");
            
            //need to detach and reattach the iframe to unload its memory
            frameContainer.remove(widget);
            frameContainer.setWidget(widget);
        }
    }
   
    @Override
    public void onPreDetach() {
        unlockFrameTextBoxes();
    }
    
    /**
     * Return true if the tui iframe is visible.  False otherwise.
     * 
     * @return True if the tui iframe is visible.  False otherwise.
     */
    public boolean getIFrameVisibility() {
        
        return iFrameCtrl.isVisible();
    }
}
