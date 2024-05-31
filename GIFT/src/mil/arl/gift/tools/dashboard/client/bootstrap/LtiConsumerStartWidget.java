/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.constants.ProgressType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.shared.LtiParameters;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.ProgressResponse;


/**
 * The LtiConsumerStartWidget is a page that is used as a landing page from an incoming LTI request
 * from an LTI Tool Consumer.  The landing page serves as a place where errors can be displayed and
 * the user can be notified of progress of the launching of the course from the LTI Tool Consumer.
 * 
 * Unlike the login page, the launch process should be seamless for the user to where the course
 * is auto started.
 * 
 * @author nblomberg
 *
 */
public class LtiConsumerStartWidget extends AbstractBsWidget {

    private static Logger logger = Logger.getLogger(LtiConsumerStartWidget.class.getName());
    
    private static LtiConsumerUiBinder uiBinder = GWT.create(LtiConsumerUiBinder.class);
       
    @UiField
    protected HTML loadingMessage;
    
    @UiField
    protected BsLoadingIcon loadingIcon;
    
    @UiField
    protected Button startButton;
    
    @UiField
    ProgressBar loadProgressBar;
    
    @UiField
    Progress loadProgress;
    
    @UiField
    FlowPanel startPanel;
    
    @UiField
    FlowPanel loadPanel;
    
    @UiField
    FlowPanel mainPanel;
    
    /** the image containing the configurable logo */
    @UiField
    protected Image logoImage;
    
    
    interface LtiConsumerUiBinder extends UiBinder<Widget, LtiConsumerStartWidget> {
    }
    
    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);

    LoadCourseParameters loadCourseParameters = null;
    
    /** A timer that is used to check the progress of the course load.  The timer can be cancelled if the user aborts the load course operation. */
    private Timer checkProgressTimer = null;
    
    /** Timer (in ms) to control how frequently we poll the server to get updates for the load course progress bar. */
    private final int CHECK_PROGRESS_MS = 250;
    
    private final int NO_PERCENT = 0;
    private final int FULL_PERCENT = 100;
    
    /** Delay (in ms) to allow the user time to read the loading text and before the start button is displayed. */
    private final int SHOW_START_DELAY_MS = 1000;
    

    /** The source path of the course that maps to the course id.  */
    private String courseSourcePath;
  
    /** The lti parameters used to load and launch the course. */
    private LtiParameters ltiParams;
    
    
    private enum PanelEnum {
        LOAD_PANEL,
        START_PANEL
    }
    
    /**
     * Constructor
     */
    public LtiConsumerStartWidget(Object initParams) {
        
        logger.info("LtiConsumerStartWidget() called.");
        initWidget(uiBinder.createAndBindUi(this));
        
        // set the background
        String backgroundImage = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
        mainPanel.getElement().getStyle().setBackgroundImage("url('"+backgroundImage+"')");

        showError("");
        loadingIcon.stopLoading();
        loadingMessage.setVisible(false);

        showPanel(PanelEnum.LOAD_PANEL);
        
        loadProgress.setType(ProgressType.STRIPED);
        loadProgress.setActive(true);
        loadProgressBar.setPercent(NO_PERCENT);
        loadProgress.setVisible(true);
        
        String logoUrl = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.LOGO);
        logoImage.setUrl(logoUrl);
        
        if (UiManager.getInstance().getSessionId() == UiManager.INVALID_SESSION) {
            
            logger.severe("The Lti user session was not set properly when loading the screen.");
            startButton.setEnabled(false);
            
            String errorMessage = "The Lti User Session was not set properly when loading the screen.";
            showError(new DashboardErrorWidget.ErrorMessage(errorMessage, "", null));
            return;
        } 
        
        if (initParams != null && initParams instanceof LtiParameters) {
            
            ltiParams = (LtiParameters) initParams;
            
            startCourse(ltiParams);
            
            startButton.setEnabled(false);
            
            startButton.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    startLoadedCourse();
                }
            });           
            
        } else {
            String errorMessage = "Screen initialization error.  Expected object of type " + LtiParameters.class.getName();
            showError(new DashboardErrorWidget.ErrorMessage(errorMessage, "", null));
            return;
        }
        
    }
    
    /**
     * Displays the panel.   The widget only shows one panel at a time, which can be the
     * loading panel, the start panel, or the error panel.
     * 
     * @param panel PanelEnum to show on the UI.
     */
    private void showPanel(PanelEnum panel) {
        
        switch (panel) {
        case LOAD_PANEL:
            loadPanel.setVisible(true);
            startPanel.setVisible(false);
            break;
        case START_PANEL:
            loadPanel.setVisible(false);
            startPanel.setVisible(true);
            break;
        }
    }
    
    /**
     * Determines if the loadCourse rpc has returned successfully.
     * 
     * @return True if the loadCourse rpc is completed successfully, false otherwise.
     */
    private boolean isCourseLoadRpcComplete() {
        return loadCourseParameters != null;
    }
    
    /**
     * Starts a course in the TUI from an LTI Tool Consumer.  This doesn't use the normal login with a full username/password, but instead
     * uses the lti authentication parameters such as consumerKey and consumerId to identify the user from the LTI Tool Consumer system.
     * 
     * @param params The LTI parameters used to start the course.
     */
    private void startCourse(LtiParameters params) {
        logger.info("LtiConsumerStartWidget::startCourse() called.");

        String consumerKey = params.getConsumerKey();
        String consumerId = params.getConsumerId();
        String courseId = params.getCourseId();
        
        if (consumerKey == null || consumerKey.isEmpty()) {
            String errorMessage = "Course could not be started due to invalid lti launch parameters.";
            showError(new DashboardErrorWidget.ErrorMessage(errorMessage, "", null));
            return;
        }
        
        if (consumerId == null || consumerId.isEmpty()) {
            String errorMessage = "Course could not be started due to invalid lti launch parameters.";
            showError(new DashboardErrorWidget.ErrorMessage(errorMessage, "", null));
            return;
        }
        
        if (courseId == null || courseId.isEmpty()) {
            String errorMessage = "Course could not be started due to invalid lti launch parameters.";
            showError(new DashboardErrorWidget.ErrorMessage(errorMessage, "", null));
            return;
        }

        // Start the loading indicators.
        startLoading();

        // Convert the courseId into the course source path.
        dashboardService.ltiGetCourseSourcePathFromId(consumerKey, consumerId, courseId, new AsyncCallback<GenericRpcResponse<String>>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe("ltiGetCourseSourcePathFromId: " + t.getMessage());
                
                showError(new DashboardErrorWidget.ErrorMessage("The request to the server failed trying to lookup the course id.  Please verify that the 'course_id' parameter is correct.", 
                        "",
                        null
                        ));
            }

            @Override
            public void onSuccess(GenericRpcResponse<String> result) {
                
                if (result.getWasSuccessful()) {
                    
                    courseSourcePath = result.getContent();
                    startLoadCourseRpc();
                } else {
                    showError(result.getException());
                }
            }
            
        });
    }
    
    
    /**
     * Calls the lti load course rpc to the server.  This starts the loading of the course data on the client.
     */
    private void startLoadCourseRpc() {
        // Note we are caching off the Request object here so that we can abort this rpc if the user wants to.  Instead of void, the rpc can return a
        // Result object which can be cancelled via Request.cancel() method.
        // Note that this ltiLoadCourse() rpc is a SYCHRONOUS call on the server and will not return until the course is completely ready.
        // The UI calls the getLtiCourseLoadProgress() rpc to check the progress while the ltiLoadCourse() rpc is outstanding.
        // Only once the getLtiCourseLoadProgress() is at 100% and the ltiLoadCourse() rpc has returned, that is when the
        // course is ready to be started.
        
        final String consumerKey = ltiParams.getConsumerKey();
        final String consumerId = ltiParams.getConsumerId();
        dashboardService.ltiLoadCourse(consumerKey, consumerId, courseSourcePath, new AsyncCallback<GenericRpcResponse<String>>() {

            @Override
            public void onFailure(Throwable t) {
                
                stopLoading();
                logger.severe("Unable to load the course: " + courseSourcePath + ".  Caught a throwable error: " + t.getMessage());                       
                
                showError(new DashboardErrorWidget.ErrorMessage(
                        "A server error occurred while loading the course.&nbsp;&nbsp;"
                                + "Please make sure the path to the course is correct.&nbsp;&nbsp;"
                                + "<b>Course Path:</b> " + courseSourcePath 
                                , "Error Details:&nbsp;&nbsp;" + t.toString(), 
                                null
                        ));

            }

            @Override
            public void onSuccess(GenericRpcResponse<String> result) {

                
                if (result.getWasSuccessful()) {
                                        
                    String courseRuntimeId = result.getContent();
                    
                    loadCourseParameters = new LoadCourseParameters(courseRuntimeId, courseSourcePath, courseSourcePath);
                    loadCourseParameters.setLtiParameters(ltiParams);

                    logger.info("Load course returned success.  Runtime course id = " + courseRuntimeId);

                } else {
                    
                    stopLoading();
                    String errorMsg = "Unable to load the course: " + courseSourcePath + ".  Server returned an rpc error: " 
                                + result.getException().getReason() + "\n" + result.getException().getDetails();
                    
                    logger.severe(errorMsg);
                    showError(result.getException());
                }
            }                

        });
        
        checkCourseLoadProgress();
    }
    
    /**
     * Sets up the UI to show the right loading UI elements.
     */
    private void startLoading() {
        
        showPanel(PanelEnum.LOAD_PANEL);
        loadingIcon.startLoading();
        loadingMessage.setVisible(true);

        loadProgress.setType(ProgressType.STRIPED);
        loadProgressBar.setPercent(NO_PERCENT);
        loadProgress.setActive(true);
        loadProgress.setVisible(true);
    }
    
    /**
     * Sets up the UI to show the proper panels once the loading has completed and the course is ready to be started.
     */
    private void stopLoading() {
        
        cancelProgressTimer();
        showPanel(PanelEnum.START_PANEL);
        loadingMessage.setVisible(false);
        loadingIcon.stopLoading();

        // Keep the progress bar visible, but set the type back to normal to show it's done and loaded.
        loadProgress.setType(ProgressType.DEFAULT);
        loadProgress.setActive(false);
        loadProgress.setVisible(false);
        
        dashboardService.cleanupLoadCourseIndicator(ltiParams.getConsumerKey(), ltiParams.getConsumerId(), courseSourcePath, new AsyncCallback<GenericRpcResponse<Void>>() {
            
            @Override
            public void onSuccess(GenericRpcResponse<Void> arg0) {
                // don't care                
            }
            
            @Override
            public void onFailure(Throwable arg0) {
                // don't care, best effort                
            }
        });
    }
    
    /**
     * Cancels the progress timer (if it has been started).
     */
    private void cancelProgressTimer() {
        if (checkProgressTimer != null) {
            checkProgressTimer.cancel();
        }
    }
    
    /**
     * Check the server for the progress of the load course operation for this widget.
     */
    private void checkCourseLoadProgress() {
        
        final String courseSourceId = courseSourcePath;
        final String consumerKey = ltiParams.getConsumerKey();
        final String consumerId = ltiParams.getConsumerId();
        logger.fine("Checking load progress for course: " + courseSourceId);
        checkProgressTimer = new Timer() {
            @Override
            public void run() {
                dashboardService.getLoadCourseProgress(consumerKey, consumerId, courseSourceId, new AsyncCallback<ProgressResponse>() {

                    @Override
                    public void onFailure(Throwable t) {
                        logger.severe("Error caught with getting load course progress: " + t.getMessage());
                        stopLoading();
                    }

                    @Override
                    public void onSuccess(ProgressResponse result) {
          
                        if (result.isSuccess()) {

                            ProgressIndicator progress = result.getProgress();
                            
                            logger.fine("Progress is: " + progress);
                            if (progress != null) {
                                
                                loadProgressBar.setPercent(progress.getPercentComplete());
                                ProgressIndicator subProgress = progress.getSubtaskProcessIndicator();
                                
                                if (subProgress != null && subProgress.getTaskDescription() != null && !subProgress.getTaskDescription().isEmpty())
                                {   
                                    String loadStr = progress.getTaskDescription() + ": " + subProgress.getTaskDescription();
                                    loadingMessage.setText(loadStr);
                                } else {
                                    loadingMessage.setText(progress.getTaskDescription());
                                }
                                
                                
                                // Both the load progress should be at 100 and the loadCourse rpc should be completed before
                                // the user can start the course.
                                if (progress.getPercentComplete() < FULL_PERCENT || !isCourseLoadRpcComplete()) {
                                    
                                    logger.info("isCourseLoadRpcComplete=" + isCourseLoadRpcComplete());
                                    
                                    // If somehow the server hasn't returned the loadCourse rpc, then update the loading
                                    // message to indicate that the server needs to respond.
                                    if (progress.getPercentComplete() >= FULL_PERCENT && !isCourseLoadRpcComplete()) {
                                        loadingMessage.setText("Loading:  Waiting for the server to reply that the course is ready.");
                                    }
                                    // If we're still getting updates, schedule another check to the server.
                                    checkCourseLoadProgress();
                                } else {
                                    
                                    logger.info("load completed, startCourseTimer started.");
                                    
                                    // Change the style of the progress bar to show that it's almost done.
                                    loadProgress.setType(ProgressType.DEFAULT);
                                    
                                    // Add a slight delay to make the UI transition look nicer to give the user
                                    // a brief time to read the loading message.
                                    Timer startCourseTimer = new Timer() {

                                        @Override
                                        public void run() {
                                            stopLoading();
                                            logger.info("Course is loaded: " + courseSourceId);
                                            
                                            startButton.setEnabled(true);
                                        }
                                        
                                    };
                                    
                                    startCourseTimer.schedule(SHOW_START_DELAY_MS);
                                    
                                }
                            }
                        } else {
                            logger.severe("Rpc error occurred while calling getLoadCourseProgress. Error is: " + result.getResponse());
                            stopLoading();
                            
                            showError(new DashboardErrorWidget.ErrorMessage(
                                    "An error occurred while loading the course: " + result.getResponse() 
                                    + "<br/><b>Course Path:</b> " + courseSourceId,
                                    result.getAdditionalInformation(), 
                                    result.getErrorStackTrace()
                            ));
                        }
                        
                    }
                    
                });
            }
          };

          checkProgressTimer.schedule(CHECK_PROGRESS_MS);
    }
    
    /**
     * Shows the given error message to the user. If the provided message is null or empty, 
     * the error text will be hidden from the user.
     * 
     * @param text the error message
     */
    private void showError(Object errorObj){

        logger.info("showError()");
        
        cancelProgressTimer();
        UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, errorObj);
    }
    
    /**
     * Starts the loaded course by using the course runtime id.  
     */
    private void startLoadedCourse() {
      
       logger.info("Requesting to start course: " + loadCourseParameters + " for user: " + UiManager.getInstance().getUserId());
       UiManager.getInstance().displayScreen(ScreenEnum.LTI_COURSE_RUNTIME, loadCourseParameters );
    }
}
