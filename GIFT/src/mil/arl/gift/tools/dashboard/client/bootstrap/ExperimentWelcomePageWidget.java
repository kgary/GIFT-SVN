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
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.WebDeviceUtils;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.shared.ExperimentParameters;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.LoadExperimentResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ProgressResponse;

/**
 * The welcome landing page for an experiment course.
 * 
 * @author sharrison
 *
 */
public class ExperimentWelcomePageWidget extends AbstractBsWidget {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ExperimentWelcomePageWidget.class.getName());

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ExperimentWelcomePageUiBinder extends UiBinder<Widget, ExperimentWelcomePageWidget> {
    }

    /** The UiBinder that combines the ui.xml with this java class */
    private static ExperimentWelcomePageUiBinder uiBinder = GWT.create(ExperimentWelcomePageUiBinder.class);

    /** The panel containing the start or loading screens */
    @UiField
    protected DeckPanel deckPanel;

    /** Panel showing the start button */
    @UiField
    protected FlowPanel startPanel;

    /** The button to start the experiment */
    @UiField
    protected Button startButton;

    /** The loading panel */
    @UiField
    protected FlowPanel loadPanel;

    /** The loading message to show to the user */
    @UiField
    protected HTML loadingMessage;

    /** The icon to show while the loading page is visible */
    @UiField
    protected BsLoadingIcon loadingIcon;

    /** The progress of loading the experiment */
    @UiField
    protected Progress loadProgress;

    /** The bar that shows the loading progress */
    @UiField
    protected ProgressBar loadProgressBar;
    
    @UiField
    protected FlowPanel mainPanel;
    
    /** the image containing the configurable logo */
    @UiField
    protected Image logoImage;

    /** Create a remote service proxy to talk to the server-side */
    private final DashboardServiceAsync dashboardService = GWT.create(DashboardService.class);

    /** The parameters for loading the experiment */
    private LoadCourseParameters loadCourseParameters = null;

    /**
     * A timer that is used to check the progress of the course load. The timer
     * can be cancelled if the user aborts the load course operation.
     */
    private Timer checkProgressTimer = null;

    /**
     * Timer (in ms) to control how frequently we poll the server to get updates
     * for the load course progress bar.
     */
    private final int CHECK_PROGRESS_MS = 250;

    /** Progress start */
    private final int NO_PERCENT = 0;

    /** Progress end */
    private final int FULL_PERCENT = 100;

    /**
     * Delay (in ms) to allow the user time to read the loading text and before
     * the start button is displayed.
     */
    private final int SHOW_START_DELAY_MS = 1000;

    /** The parameters for the experiment */
    private final ExperimentParameters experimentParameters;

    /**
     * Constructor
     * 
     * @param initParams the parameters used to initialize the page.
     */
    public ExperimentWelcomePageWidget(Object initParams) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ExperimentWelcomePageWidget(" + initParams + ")");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        // set the background
        String backgroundImage = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
        mainPanel.getElement().getStyle().setBackgroundImage("url('"+backgroundImage+"')");
        
        String logoUrl = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.LOGO);
        logoImage.setUrl(logoUrl);

        if (initParams instanceof ExperimentParameters) {
            experimentParameters = (ExperimentParameters) initParams;

            deckPanel.showWidget(deckPanel.getWidgetIndex(startPanel));

            startButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    startLoadingExperiment();
                }
            });
        } else {
            String errorMessage = "Screen initialization error. Expected experiment parameters but got " + initParams;
            showError(new DashboardErrorWidget.ErrorMessage(errorMessage, "", null));
            experimentParameters = null;
            return;
        }
    }

    /**
     * Determines if the loadCourse rpc has returned successfully.
     * 
     * @return True if the loadCourse rpc is completed successfully, false
     *         otherwise.
     */
    private boolean isCourseLoadRpcComplete() {
        return loadCourseParameters != null;
    }

    /**
     * Calls the load experiment rpc to the server. This starts the loading of
     * the course data on the client.
     */
    private void startLoadingExperiment() {

        if (experimentParameters == null || StringUtils.isBlank(experimentParameters.getExperimentId())) {
            String errorMessage = "Experiment could not be started due to invalid experiment parameters.";
            showError(new DashboardErrorWidget.ErrorMessage(errorMessage, "", null));
            return;
        }

        /* Update UI to show loading */
        deckPanel.showWidget(deckPanel.getWidgetIndex(loadPanel));
        loadingIcon.startLoading();
        loadingMessage.setVisible(true);

        loadProgress.setType(ProgressType.STRIPED);
        loadProgressBar.setPercent(NO_PERCENT);
        loadProgress.setActive(true);
        loadProgress.setVisible(true);

        // Note we are caching off the Request object here so that we can abort
        // this rpc if the user wants to. Instead of void, the rpc can return a
        // Result object which can be cancelled via Request.cancel() method.
        // Note that this ltiLoadCourse() rpc is a SYCHRONOUS call on the server
        // and will not return until the course is completely ready.
        // The UI calls the getExperimentLoadProgress() rpc to check the
        // progress while the loadExperiment() rpc is outstanding.
        // Only once the getExperimentLoadProgress() is at 100% and the
        // loadExperiment() rpc has returned, that is when the
        // course is ready to be started.

        /* Generate a UUID to be used to identify the progress indicator */
        final String progressIndicatorId = generateUUID();
        final String experimentId = experimentParameters.getExperimentId();
        dashboardService.loadExperiment(experimentId, progressIndicatorId, new AsyncCallback<LoadExperimentResponse>() {
            @Override
            public void onSuccess(LoadExperimentResponse response) {
                if (response.isSuccess()) {
                    final String courseRuntimeFolder = response.getCourseRuntimeFolder();
                    final String courseSourceFolder = response.getCourseSourceFolder();
                    loadCourseParameters = new LoadCourseParameters(courseRuntimeFolder, courseSourceFolder,
                            courseSourceFolder);
                    loadCourseParameters.setExperimentParameters(experimentParameters);

                    logger.info("Load course returned success.  Runtime course id = " + courseRuntimeFolder
                            + "; Source folder = " + courseSourceFolder);
                } else {

                    stopLoadingExperiment(progressIndicatorId);
                    String errorMsg = "Unable to load the experiment with id: '" + experimentId + "'.";
                    logger.severe(errorMsg);
                    showError(response.getErrorMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                stopLoadingExperiment(progressIndicatorId);
                String errorMsg = "Unable to load the experiment with id: '" + experimentId + "'.";
                logger.severe(errorMsg + " Caught a throwable error: " + t.getMessage());

                if(WebDeviceUtils.isMobileAppEmbedded()) {
                    
                    /* Try connecting again in case the network dropped temporarily */
                    startLoadingExperiment();
                    
                } else {
                    showError(new DashboardErrorWidget.ErrorMessage(
                            "A server error occurred while loading the experiment.&nbsp;&nbsp;"
                                    + "Please make sure the experiment id is correct.&nbsp;&nbsp;"
                                    + "<b>Experiment ID:</b> " + experimentId,
                            "Error Details:&nbsp;&nbsp;" + t.getMessage(), null));
                
            }
            }
        });

        checkCourseLoadProgress(progressIndicatorId);
    }

    /**
     * Sets up the UI to show the proper panels once the loading has completed
     * and the course is ready to be started.
     * 
     * @param progressIndicatorId the unique id of the progress indicator
     */
    private void stopLoadingExperiment(String progressIndicatorId) {

        cancelProgressTimer();
        loadingMessage.setVisible(false);
        loadingIcon.stopLoading();

        // Keep the progress bar visible, but set the type back to normal to
        // show it's done and loaded.
        loadProgress.setType(ProgressType.DEFAULT);
        loadProgress.setActive(false);
        loadProgress.setVisible(false);

        dashboardService.cleanupLoadCourseIndicator(progressIndicatorId, new AsyncCallback<GenericRpcResponse<Void>>() {
            @Override
            public void onSuccess(GenericRpcResponse<Void> arg0) {
                // don't care
            }

            @Override
            public void onFailure(Throwable arg0) {
                // don't care, best effort
            }
        });

        startLoadedCourse();
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
     * Check the server for the progress of the load course operation for this
     * widget.
     * 
     * @param progressIndicatorId the unique id of the progress indicator
     */
    private void checkCourseLoadProgress(final String progressIndicatorId) {
        checkProgressTimer = new Timer() {
            @Override
            public void run() {
                dashboardService.getLoadCourseProgress(progressIndicatorId, new AsyncCallback<ProgressResponse>() {

                    @Override
                    public void onFailure(Throwable t) {
                        logger.severe("Error caught with getting load course progress: " + t.getMessage());
                        stopLoadingExperiment(progressIndicatorId);
                    }

                    @Override
                    public void onSuccess(ProgressResponse result) {

                        if (result.isSuccess()) {

                            ProgressIndicator progress = result.getProgress();

                            logger.fine("Progress is: " + progress);
                            if (progress != null) {

                                loadProgressBar.setPercent(progress.getPercentComplete());
                                ProgressIndicator subProgress = progress.getSubtaskProcessIndicator();

                                if (subProgress != null && subProgress.getTaskDescription() != null
                                        && !subProgress.getTaskDescription().isEmpty()) {
                                    String loadStr = progress.getTaskDescription() + ": "
                                            + subProgress.getTaskDescription();
                                    loadingMessage.setText(loadStr);
                                } else {
                                    loadingMessage.setText(progress.getTaskDescription());
                                }

                                /* Both the load progress should be at 100 and
                                 * the loadCourse rpc should be completed before
                                 * the user can start the course. */
                                if (progress.getPercentComplete() < FULL_PERCENT || !isCourseLoadRpcComplete()) {

                                    logger.info("isCourseLoadRpcComplete=" + isCourseLoadRpcComplete());

                                    /* If somehow the server hasn't returned the
                                     * loadCourse rpc, then update the loading
                                     * message to indicate that the server needs
                                     * to respond. */
                                    if (progress.getPercentComplete() >= FULL_PERCENT && !isCourseLoadRpcComplete()) {
                                        loadingMessage.setText(
                                                "Loading:  Waiting for the server to reply that the course is ready.");
                                    }
                                    /* If we're still getting updates, schedule
                                     * another check to the server. */
                                    checkCourseLoadProgress(progressIndicatorId);
                                } else {

                                    logger.info("load completed, startCourseTimer started.");

                                    /* Change the style of the progress bar to
                                     * show that it's almost done. */
                                    loadProgress.setType(ProgressType.DEFAULT);

                                    /* Add a slight delay to make the UI
                                     * transition look nicer to give the user a
                                     * brief time to read the loading
                                     * message. */
                                    Timer startCourseTimer = new Timer() {
                                        @Override
                                        public void run() {
                                            stopLoadingExperiment(progressIndicatorId);
                                            if (logger.isLoggable(Level.INFO)) {
                                                logger.info("Experiment is loaded: " + experimentParameters.getExperimentId());
                                            }
                                        }

                                    };

                                    startCourseTimer.schedule(SHOW_START_DELAY_MS);
                                }
                            }
                        } else {
                            logger.severe("Rpc error occurred while calling getLoadCourseProgress. Error is: "
                                    + result.getResponse());
                            stopLoadingExperiment(progressIndicatorId);

                            showError(new DashboardErrorWidget.ErrorMessage(
                                    "An error occurred while loading the experiment: " + result.getResponse()
                                            + "<br/><b>Experiment ID:</b> " + experimentParameters.getExperimentId(),
                                    result.getErrorDetails(), result.getErrorStackTrace()));
                        }
                    }
                });
            }
        };

        checkProgressTimer.schedule(CHECK_PROGRESS_MS);
    }

    /**
     * Shows the given error message to the user. If the provided message is
     * null or empty, the error text will be hidden from the user.
     * 
     * @param errorObj the error message
     */
    private void showError(Object errorObj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showError(" + errorObj + ")");
        }

        cancelProgressTimer();
        UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, errorObj);
    }

    /**
     * Starts the loaded course by using the course runtime id.
     */
    private void startLoadedCourse() {

        logger.info("Requesting to start experiment: " + loadCourseParameters);
        UiManager.getInstance().displayScreen(ScreenEnum.EXPERIMENT_RUNTIME, loadCourseParameters);
    }

    /**
     * Generates a UUID to be used to identify the progress indicator.
     * 
     * @return a UUID
     */
    public native static String generateUUID() /*-{
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
            function(c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r
                        : (r & 0x3 | 0x8);
                return v.toString(16);
            });
}-*/;
}
