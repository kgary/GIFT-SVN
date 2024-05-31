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
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExperimentResponse;

/**
 * A widget used to represent an experiment and update its data
 * 
 * @author nroberts
 */
public class ExperimentWidget extends Composite {
    
    /** instance of the logger */
    private static Logger logger = Logger.getLogger(ExperimentWidget.class.getName());
    
    private static ExperimentWidgetUiBinder uiBinder = GWT
            .create(ExperimentWidgetUiBinder.class);
    
    /** the experiment can still have data collected */
    private final static String ACTIVE_STATUS = "Active";
    
    /** the experiment can't have data collected right now but can be made active again */
    private final static String PAUSED_STATUS = "Paused";
    
    /** the experiment is closed and can no longer have data collected */
    private final static String ENDED_STATUS = "Ended";
    
    /** placeholder text for an experiment with no name */
    private final static String NO_NAME = "<NAME UNAVAILABLE>";
    
    /** placeholder text for an experiment with no description */
    private final static String NO_DESCRIPTION = "<No description available>";
    
    /** placeholder text for an experiment with no course reference */
    private final static String NO_COURSE = "<COURSE UNAVAILABLE>";
    
    /** tooltip text for why an experiment is ended if the course is no longer accessible */
    private final static String ENDED_REASON_TEXT = "The course is no longer published because the original course for this published course has been deleted.";
    
    interface ExperimentWidgetUiBinder extends
    UiBinder<Widget, ExperimentWidget> {
        
    }
    
    /** the panel containing the experiment UI components such as the name and description */
    @UiField
    protected Widget container;
    
    /** the header of the panel which is the only thing shown when the panel is collapsed */
    @UiField
    protected FocusPanel headerPanel;
    
    /** contains the status button, build report buttons */
    @UiField
    protected FocusPanel footerPanel;
    
    /** icon used to indicate if the header is collapsed */
    @UiField
    protected Icon headerCollapseIcon;
    
    /** icon used to indicate if the footer is collapsed */
    @UiField
    protected Icon footerCollapseIcon;
    
    /** the panel that shows the content of the container */
    @UiField
    protected DeckPanel contentDeck;
    
    /** the empty panel to show when collapsed */
    @UiField
    protected Widget noContentPanel;
    
    /** the non-empty panel to shown when expanded (e.g. name, description) */
    @UiField
    protected Widget contentPanel;
    
    /** used to pause the experiment (i.e. pause data collection) */
    @UiField
    protected Button pauseButton;
    
    /** used to resume the experiment (i.e. resume data collection) */
    @UiField
    protected Button resumeButton;
    
    /** the panel containing the buttons */
    @UiField
    protected DeckPanel buttonDeck;
    
    @UiField 
    protected Widget activeButtonPanel;
    
    /** the panel where the pause button is */
    @UiField
    protected Widget pausedButtonPanel;
    
    /** the panel where the ended button is */
    @UiField
    protected Widget endedButtonPanel;
    
    /** current status of the experiment (e.g. Active) */
    @UiField
    protected InlineLabel status;
    
    /** contains the username of owner of this experiment */
    @UiField
    protected InlineLabel ownerUsername;
    
    /** static text that gives context to the owner username label */
    @UiField
    protected HTML ownerLabel;
    
    /** authored name of the experiment */
    @UiField
    protected Label experimentName;
    
    /** authored description for the experiment */
    @UiField
    protected Paragraph descriptionText;
    
    /** the panel containing the {@link #endedReasonText} */
    @UiField
    protected FlowPanel endedReasonPanel;

    /** the reason why the experiment is in the ended status*/
    @UiField
    protected Paragraph endedReasonText;
    
    /** the URL of the experiment */
    @UiField
    protected Label urlLabel;

    /** The icon for editing the course */
    @UiField
    protected Icon editCourseIcon;

    /** the course folder path for this experiment */
    @UiField
    protected HasText courseText;
    
    /** used to delete the experiment instance */
    @UiField
    protected Button deleteButton;    
    
    /** tooltip for the delete button */
    @UiField
    protected ManagedTooltip deleteButtonTooltip;
    
    /** used to export the course behind this experiment instance */
    @UiField
    protected Button exportCourseButton;
    
    /** used to export the raw data for this experiment instance */
    @UiField
    protected Button exportSubjectDataButton;
    
    /** used to pause the experiment and export the raw data for this experiment */
    @UiField
    protected Button exportSubjectDataButtonPaused;
    
    /** used to end the experiment and export the raw data for this experiment */
    @UiField
    protected Button exportSubjectDataButtonEnded;
    
    /** used to validate the course behind this experiment */
    @UiField
    protected Button validateCourseButton;
    
    /** used to edit the metadata for this experiment (e.g. description) */
    @UiField
    protected Button editButton;
    
    /** shows the number of participants that have started this experiment */
    @UiField
    protected HasText numberOfAttempts;
    
    /** shows the timestamp for when the last participant started this experiment */
    @UiField
    protected HasText latestAttempt;
    
    /** used to refresh the display of the metadata of this experiment */
    @UiField
    protected Button refreshButton;
    
    /** the panel where the RPC status is shown */
    @UiField
    protected Widget rpcStatusPanel;
    
    /** shows any transition status values (e.g. Pausing) */
    @UiField
    protected HasText rpcStatusLabel;
    
    /** a loading indicator used when building this widget */
    @UiField
    protected BsLoadingIcon rpcLoadingIcon;
    
    /** opens the report generation panel */
    @UiField
    protected Button buildButton;
    
    /** used to start the report generation process */
    @UiField
    protected Button buildButtonEnded;
    
    /** a double action button that pauses the experiment and opens the report generation panel */
    @UiField
    protected Button pauseAndBuildButton;
    
    /** represents the type of data collection instance (e.g. LTI) */
    @UiField
    protected Icon dataSetTypeIcon;
    
    /** shows the URL that participants can use to take the experiment */
    @UiField
    protected HTML urlTitle;

    /** The button to copy the URL */
    @UiField
    protected Button copyUrlButton;

    /** the panel with experiment information */
    @UiField 
    protected Widget generalInfoPanel;
    
    /** shows information about an LTI instance */
    @UiField
    protected DataCollectionLtiPropertiesPanel ltiInfoPanel;
    
    /** where generation info panel is shown */
    @UiField
    protected DeckPanel infoDeck;
    
    /** shows information about the experiment */
    @UiField
    protected RadioButton generalInfoButton;
    
    /** display LTI information */
    @UiField
    protected RadioButton ltiInfoButton;
    
    /** used by LTI only */
    @UiField
    protected FlowPanel buttonGroupPanel;
    
    /** used to share this experiment instance with other gift users */
    @UiField
    Button ctrlSharePublishedCourse;

    /** info about the data collection instance (e.g. experiment) used to populate the panels in this widget */
    private DataCollectionItem dataCollectionItem = null;
    
    /** The source course the experiment is pointing to */
    private DomainOption experimentSourceCourse = null;

    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    
    /** the widget containing all experiment instances including this widget */
    ExperimentToolWidget parentWidget;
    
    /**
     * whether a request has been made to get more course details from the server, the request
     * maybe still pending, has returned to client, the return maybe an error or a success.
     * This prevents asking the server multiple times.
     */
    private boolean hasAskedForCourseDetails = false;
    
    /**
     * whether the user has, at a minimum, permissions to view the course in the course creator
     */
    private boolean hasCourseViewPermissions = false;
    
    /**
     * Callback used when there is an error on the server with the experiment represented by this widget.  
     * The callback will reload the published course page in an effort to remove any deleted published courses
     * since the list was retrieved last.
     */
    private DialogCallback errorDialogCallback = new DialogCallback() {
        
        @Override
        public void onAccept() {
            // reload the page which should cause any deleted data collection items to be removed from the UI
            UiManager.getInstance().displayScreen(ScreenEnum.MY_RESEARCH);            
        }
    };
    
    /**
     * Callback used when there is an error on the server with the experiment represented by this widget.  
     * The callback will reload the published course page in an effort to remove any deleted published courses
     * since the list was retrieved last.
     */
    private ModalDialogCallback errorModalDialogCallback = new ModalDialogCallback() {
        
        @Override
        public void onClose() {
            // reload the page which should cause any deleted data collection items to be removed from the UI
            UiManager.getInstance().displayScreen(ScreenEnum.MY_RESEARCH);               
        }
    };

    /**
     * Creates a new experiment widget representing the given experiment
     * 
     * @param originalExperiment the experiment to start representing
     * @param parent the parent widget containing the experiment
     */
    public ExperimentWidget(final DataCollectionItem originalExperiment, ExperimentToolWidget parent) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ExperimentWidget(" + originalExperiment + ")");
        }

        initWidget(uiBinder.createAndBindUi(this));        

        parentWidget = parent;
        
        if (parentWidget == null) {
            throw new IllegalArgumentException("Parent widget cannot be null.");
        }
        
        if(originalExperiment == null){
            throw new IllegalArgumentException("Data collection item cannot be null.");
        }
        
        rpcLoadingIcon.startLoading();
        
        setContentVisible(false);
        
        infoDeck.showWidget(infoDeck.getWidgetIndex(generalInfoPanel));
        
        setExperiment(originalExperiment);
        
        final ExperimentWidget thisWidget = this;
        editButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();
                
                parentWidget.showEditDialog(dataCollectionItem, thisWidget); 
            }
        });
                                
        deleteButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(!canDelete()){
                    // a course tile (course data) published course can only be deleted when
                    // the status is ended    
                    String publishCourseName = "this published course instance";
                    if(dataCollectionItem != null && StringUtils.isNotBlank(dataCollectionItem.getName())){
                        publishCourseName = "'" + dataCollectionItem.getName() + "'";
                    }
                    UiManager.getInstance().displayInfoDialog("Unable to Delete", "You can't delete "+publishCourseName+" because you either don't have permissions or the status is not "+ExperimentStatus.ENDED+".");
                    return;
                }
                
                event.stopPropagation();
                
                StringBuilder sb = new StringBuilder();
                String item = dataCollectionItem.getName() != null ? ("<b>" + dataCollectionItem.getName() + "</b>") : "this published course";
                
                sb.append("Are you sure you want to permanently delete ").append(item).append("?<br/>")
                        .append("<br/>")
                        .append("You will not be able to retrieve this published course's data after it has been deleted");
                
                if(UiManager.getInstance().getUseCloudLoginPage()){
                    
                    sb.append(".<br/><br/><b>Note:</b> Any data gathered by this published course will be backed up for 30 days ")
                            .append("after it is deleted.");
                } else {
                    sb.append(", and any data collected by this published course will be lost during the deletion process.");
                }
                
                if (!dataCollectionItem.isLegacyExperiment()) {
                    sb.append("<br/><br/>").append(
                            "This will <b>NOT</b> delete the original course that was used to create this published course. The original course can be deleted through the <b>\"Take a Course\"</b> page.");
                }

                UiManager.getInstance().displayConfirmDialog(
                        "Delete Published Course", 
                        sb.toString(),
                        "Yes, delete this published course",
                        "Cancel", new ConfirmationDialogCallback() {
                            
                            @Override
                            public void onDecline() {
                                //Nothing to do
                            }
                            
                            @Override
                            public void onAccept() {     
                                
                                parentWidget.showDeleteDialog(dataCollectionItem, thisWidget);                           
                            }
                        });
            }
        });
        
        
        
        exportCourseButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                if(!hasCourseViewPermissions) {
                    // prevent hacking the DOM to expose or use the export course button when you shouldn't be able too 
                    logger.warning("Unable to export the course because it doesn't appear this user has permission to view the course content.");
                    return;
                }
                
                parentWidget.showStartExportCourseDialog(dataCollectionItem,  thisWidget);
            }
        });
        
        
        ClickHandler exportHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();

                parentWidget.showStartExportSubjectDialog(dataCollectionItem, thisWidget);
            }
        };
        
        exportSubjectDataButton.addClickHandler(exportHandler);
        exportSubjectDataButtonPaused.addClickHandler(exportHandler);
        exportSubjectDataButtonEnded.addClickHandler(exportHandler);

        validateCourseButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();
                
                dashboardService.validateExperimentCourse(UiManager.getInstance().getUserName(), dataCollectionItem.getId(), new AsyncCallback<DetailedRpcResponse>() {
                    
                    @Override
                    public void onSuccess(DetailedRpcResponse response) {
                        
                        if(response.isSuccess()){
                            
                            UiManager.getInstance().displayInfoDialog(
                                    "Course Validated", 
                                    "The course used by <b>" + (dataCollectionItem.getName() != null ? dataCollectionItem.getName() : "this published course") + "</b>"
                                            + " has been successfully validated. No problems were found with the course."
                                    );
                            
                        } else {
                            
                            if(response.getErrorMessage() == null){
                                UiManager.getInstance().displayErrorDialog("Validate Course Failed", 
                                        "An error occurred while validating the course.\n\nIf you lack the permissions necessary to fix the course validation error, please contact the owner ("+dataCollectionItem.getAuthorUsername()+")",
                                        errorDialogCallback);
                                
                            } else {
                                
                                if(response.getErrorDetails() == null){
                                    UiManager.getInstance().displayErrorDialog("Validate Course Failed",
                                            "An error occurred while validating the course: " + response.getErrorMessage() +
                                                "\n\nIf you lack the permissions necessary to fix the course validation error, please contact the owner ("+dataCollectionItem.getAuthorUsername()+").",
                                                errorDialogCallback);
                                    
                                } else {
                                    UiManager.getInstance().displayDetailedErrorDialog(
                                            "Validate Course Failed", 
                                            "An error occurred while validating the course: " + response.getErrorMessage() + 
                                                "\n\nIf you lack the permissions necessary to fix the course validation error, please contact the owner ("+dataCollectionItem.getAuthorUsername()+")", 
                                            response.getErrorDetails(), 
                                            response.getErrorStackTrace(), 
                                            null,
                                            errorModalDialogCallback);
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable thrown) {
                        
                        UiManager.getInstance().displayErrorDialog(
                                "Validate Course Failed", 
                                "An error occurred while validating the course: " + thrown.toString() + "\n\nIf you lack the permissions necessary to fix the course validation error, please contact the owner ("+dataCollectionItem.getAuthorUsername()+").", 
                                errorDialogCallback);
                        
                        
                    }
                });
            }
        });
        
        refreshButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();
                
                dashboardService.getExperiment(UiManager.getInstance().getUserName(), dataCollectionItem.getId(), new AsyncCallback<ExperimentResponse>() {
                    
                    @Override
                    public void onSuccess(final ExperimentResponse response) {
                        
                        if(response.isSuccess() && response.getExperiment() != null){        
                            
                            setExperiment(response.getExperiment());                                
                        
                        } else {
                            
                            if(response.getErrorMessage() == null){
                                UiManager.getInstance().displayErrorDialog("Refresh Published Course Failed", "An error occurred while fetching the results of the published course from the server.", errorDialogCallback);
                                
                            } else {
                                
                                if(response.getErrorDetails() == null){
                                    UiManager.getInstance().displayErrorDialog("Refresh Published Course Failed", "An error occurred while fetching the results of the published course from the server: " + response.getErrorMessage(), errorDialogCallback);
                                    
                                } else {
                                    UiManager.getInstance().displayDetailedErrorDialog(
                                            "Refresh Published Course Failed", 
                                            "An error occurred while fetching the results of the published course from the server: " + response.getErrorMessage(), 
                                            response.getErrorDetails(), 
                                            response.getErrorStackTrace(),
                                            null,
                                            errorModalDialogCallback);
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable e) {                            
                        UiManager.getInstance().displayErrorDialog("Refresh Published Course Failed", "An error occurred while fetching the results of the published course from the server: " + e.toString() +".", errorDialogCallback);
                    }
                });                            
            }
        });
        
        headerPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                toggleContentVisible();
            }
        });
        
        footerPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                toggleContentVisible();
            }
        });
        
        resumeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(dataCollectionItem != null){
                    
                        rpcStatusLabel.setText("Resuming...");                        
                        rpcStatusPanel.setVisible(true);

                        dashboardService.setExperimentActive(UiManager.getInstance().getUserName(), dataCollectionItem.getId(), true, new AsyncCallback<ExperimentResponse>() {
                            
                            @Override
                            public void onSuccess(ExperimentResponse response) {
                                
                                rpcStatusPanel.setVisible(false);
                                
                                if(response.isSuccess() && response.getExperiment() != null){        
                                    
                                    setExperiment(response.getExperiment());                                
                                
                                } else {
                                    
                                    if(response.getErrorMessage() == null){
                                        UiManager.getInstance().displayErrorDialog("Resume Published Course Failed", "An error occurred while resuming the published course.", errorDialogCallback);
                                        
                                    } else {
                                        
                                        if(response.getErrorDetails() == null){
                                            UiManager.getInstance().displayErrorDialog("Resume Published Course Failed", "An error occurred while resuming the published course: " + response.getErrorMessage(), errorDialogCallback);
                                            
                                        } else {
                                            UiManager.getInstance().displayDetailedErrorDialog(
                                                    "Resume Published Course Failed", 
                                                    "An error occurred while resuming the published course: " + response.getErrorMessage(), 
                                                    response.getErrorDetails(), 
                                                    response.getErrorStackTrace(), 
                                                    null,
                                                    errorModalDialogCallback);
                                        }
                                    }
                                }
                            }
                            
                            @Override
                            public void onFailure(Throwable e) {
                                
                                rpcStatusPanel.setVisible(false);
                                
                                UiManager.getInstance().displayErrorDialog("Resume Published Course Failed", "An error occurred while resuming the published course: " + e.toString() +".", errorDialogCallback);
                            }
                        });
                        
                    }
                }
        });
        
        pauseButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(dataCollectionItem != null){
                    
                    rpcStatusLabel.setText("Pausing...");                        
                    rpcStatusPanel.setVisible(true);

                    dashboardService.setExperimentActive(UiManager.getInstance().getUserName(), dataCollectionItem.getId(), false, new AsyncCallback<ExperimentResponse>() {
                        
                        @Override
                        public void onSuccess(ExperimentResponse response) {
                            
                            rpcStatusPanel.setVisible(false);
                            
                            if(response.isSuccess() && response.getExperiment() != null){        
                                
                                setExperiment(response.getExperiment());                                
                            
                            } else {
                                
                                if(response.getErrorMessage() == null){
                                    UiManager.getInstance().displayErrorDialog("Pause Published Course Failed", "An error occurred while pausing the published course.", errorDialogCallback);
                                    
                                } else {
                                    
                                    if(response.getErrorDetails() == null){
                                        UiManager.getInstance().displayErrorDialog("Pause Published Course Failed", "An error occurred while pausing the published course: " + response.getErrorMessage(), errorDialogCallback);
                                        
                                    } else {
                                        UiManager.getInstance().displayDetailedErrorDialog(
                                                "Pause Published Course Failed", 
                                                "An error occurred while pausing the published course: " + response.getErrorMessage(), 
                                                response.getErrorDetails(), 
                                                response.getErrorStackTrace(), 
                                                null,
                                                errorModalDialogCallback);
                                    }
                                }
                            }
                        }
                        
                        @Override
                        public void onFailure(Throwable e) {
                            
                            rpcStatusPanel.setVisible(false);
                            
                            UiManager.getInstance().displayErrorDialog("Pause Published Course Failed", "An error occurred while pausing the published course: " + e.toString() +".", errorDialogCallback);
                        }
                    });
                    
                }
            }
        });
        
        ClickHandler buildHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(dataCollectionItem != null){
                    
                    parentWidget.showBuildReportDialog(dataCollectionItem);
                }
            }
        };
        
        buildButton.addClickHandler(buildHandler);
        buildButtonEnded.addClickHandler(buildHandler);
        
        pauseAndBuildButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(dataCollectionItem != null){
                    
                    rpcStatusLabel.setText("Pausing...");                        
                    rpcStatusPanel.setVisible(true);

                    dashboardService.setExperimentActive(UiManager.getInstance().getUserName(), dataCollectionItem.getId(), false, new AsyncCallback<ExperimentResponse>() {
                        
                        @Override
                        public void onSuccess(ExperimentResponse response) {
                            
                            rpcStatusPanel.setVisible(false);
                            
                            if(response.isSuccess() && response.getExperiment() != null){        
                                
                                setExperiment(response.getExperiment());
                                
                                parentWidget.showBuildReportDialog(dataCollectionItem);
                            
                            } else {
                                
                                if(response.getErrorMessage() == null){
                                    UiManager.getInstance().displayErrorDialog("Pause Published Course Failed", "An error occurred while pausing the published course.", errorDialogCallback);
                                    
                                } else {
                                    
                                    if(response.getErrorDetails() == null){
                                        UiManager.getInstance().displayErrorDialog("Pause Published Course Failed", "An error occurred while pausing the published course: " + response.getErrorMessage(), errorDialogCallback);
                                        
                                    } else {
                                        UiManager.getInstance().displayDetailedErrorDialog(
                                                "Pause Published Course Failed", 
                                                "An error occurred while pausing the published course: " + response.getErrorMessage(), 
                                                response.getErrorDetails(), 
                                                response.getErrorStackTrace(),
                                                null,
                                                errorModalDialogCallback);
                                    }
                                }
                            }
                        }
                        
                        @Override
                        public void onFailure(Throwable e) {
                            
                            rpcStatusPanel.setVisible(false);
                            
                            UiManager.getInstance().displayErrorDialog("Pause Published Course Failed", "An error occurred while pausing the published course: " + e.toString() +".", errorDialogCallback);
                        }
                    });
                    
                }
            }
        });
        
        generalInfoButton.setActive(true);
        generalInfoButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                infoDeck.showWidget(infoDeck.getWidgetIndex(generalInfoPanel));                
            }            
        });
        
        ltiInfoButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                infoDeck.showWidget(infoDeck.getWidgetIndex(ltiInfoPanel));                
            }            
        });
        
        ctrlSharePublishedCourse.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                //don't force the panel for the widget to open (if the panel is collapsed)
                event.stopPropagation();
                
                JsniUtility.trackEvent( "Share Published Course" );
                sharePublishedCourse();                
            }            
        });

        editCourseIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(!hasCourseViewPermissions) {
                    logger.warning("Unable to edit the course because it doesn't appear this user has permission to view the course content.");
                    return;
                }else if (experimentSourceCourse != null) {
                    UiManager.getInstance().showEditCourse(experimentSourceCourse);
                } else {
                    logger.severe("Failed to edit course '" + dataCollectionItem.getSourceCourseId()
                            + "' because the source course is null.");
                }
            }
        });
    } 
    
    /**
     * Attempts to share the published course with another user.
     */
    private void sharePublishedCourse() {
        
        // retrieve the latest experiments information to see the current saved permissions.  This is necessary
        // because both owners and manager roles can administer permissions to other users.
        dashboardService.getExperiment(UiManager.getInstance().getUserName(), dataCollectionItem.getId(), new AsyncCallback<ExperimentResponse>() {
            
            @Override
            public void onSuccess(final ExperimentResponse response) {
                
                if(response.isSuccess() && response.getExperiment() != null){        
                    
                    setExperiment(response.getExperiment()); 
                    UiManager.getInstance().displayShareDialog(dataCollectionItem);
                
                } else {
                    
                    if(response.getErrorMessage() == null){
                        UiManager.getInstance().displayErrorDialog("Refresh Published Course Failed", "An error occurred while fetching the results of the published course from the server.", errorDialogCallback);
                        
                    } else {
                        
                        if(response.getErrorDetails() == null){
                            UiManager.getInstance().displayErrorDialog("Refresh Published Course Failed", "An error occurred while fetching the results of the published course from the server: " + response.getErrorMessage(), errorDialogCallback);
                            
                        } else {
                            UiManager.getInstance().displayDetailedErrorDialog(
                                    "Refresh Published Course Failed", 
                                    "An error occurred while fetching the results of the published course from the server: " + response.getErrorMessage(), 
                                    response.getErrorDetails(), 
                                    response.getErrorStackTrace(),
                                    null,
                                    errorModalDialogCallback);
                        }
                    }                    
                }

            }
            
            @Override
            public void onFailure(Throwable e) {                            
                UiManager.getInstance().displayErrorDialog("Refresh Published Course Failed", "An error occurred while fetching the results of the published course from the server: " + e.toString() +".", errorDialogCallback);
            }
        }); 
    }
    
    /**
     * Return the data collection item information for this experiment widget.
     * @return can be null if not set yet.
     */
    public DataCollectionItem getExperiment(){
        return dataCollectionItem;
    }
    
    
    /**
     * Sets the experiment this widget represents
     * 
     * @param experiment the experiment to represent
     */
    public void setExperiment(final DataCollectionItem experiment) {
        
        this.dataCollectionItem = experiment;

        /* Reset value. Will be repopulated by rpc call later */
        this.experimentSourceCourse = null;

        if(experiment.getName() != null && !experiment.getName().isEmpty()){
            experimentName.setText(experiment.getName());
            
        } else {
            experimentName.setText(NO_NAME);
        }
        
        if(experiment.getDescription() != null && !experiment.getDescription().isEmpty()){
            descriptionText.setText(experiment.getDescription());
            
        } else {
            descriptionText.setText(NO_DESCRIPTION);
        }
        
        
        endedReasonPanel.setVisible(false);
        ExperimentStatus expStatus = experiment.getStatus();
       
        if (expStatus == null) {
           
        } else {
            if(expStatus.equals(ExperimentStatus.RUNNING)){
                
                status.setText(ACTIVE_STATUS);
                
                buttonDeck.showWidget(buttonDeck.getWidgetIndex(activeButtonPanel));
                
                container.removeStyleName("experimentEndedPanel");
                container.removeStyleName("experimentPausedPanel");
                container.addStyleName("experimentActivePanel");
                
            } else if (expStatus.equals(ExperimentStatus.PAUSED)){
                
                status.setText(PAUSED_STATUS);
                
                buttonDeck.showWidget(buttonDeck.getWidgetIndex(pausedButtonPanel));
               
                container.removeStyleName("experimentActivePanel");
                container.removeStyleName("experimentEndedPanel");
                container.addStyleName("experimentPausedPanel"); 
            } else if (expStatus.equals(ExperimentStatus.ENDED)) {
                status.setText(ENDED_STATUS);
                
                endedReasonPanel.setVisible(true);
                endedReasonText.setText(ENDED_REASON_TEXT);
                exportCourseButton.setEnabled(false);
                validateCourseButton.setEnabled(false);
                
                buttonDeck.showWidget(buttonDeck.getWidgetIndex(endedButtonPanel));
                
                container.removeStyleName("experimentActivePanel");
                container.removeStyleName("experimentPausedPanel");
                container.addStyleName("experimentEndedPanel"); 
            }
        }
        
        String owner = experiment.getAuthorUsername();
        if(owner.equalsIgnoreCase(UiManager.getInstance().getUserName())) {
            // this user owns this item
            ownerLabel.setVisible(false);
            ownerUsername.setVisible(false);
        }else {
            // someone else owns this item show the owner label
            ownerLabel.setVisible(true);
            ownerUsername.setVisible(true);
            
            ownerUsername.setText(owner);
        }
        

        final boolean hasCourseFolder = StringUtils.isNotBlank(experiment.getCourseFolder());
        courseText.setText(hasCourseFolder ? experiment.getCourseFolder() : NO_COURSE);

        // ask the server for additional course details which can be used to further customize widgets
        getCourseDetails();

        // Set defaults.
        setUrlVisibility(false);
        buttonGroupPanel.setVisible(false);
        deleteButton.setEnabled(true);
        deleteButtonTooltip.setTitle("Delete published instance and collected data.");
        
        if (experiment.isDataSetType(DataSetType.EXPERIMENT)) {
            updateExperimentAttempts(experiment);  
            urlLabel.setText(experiment.getUrl());
            setUrlVisibility(true);
        } else if (experiment.isDataSetType(DataSetType.LTI)) {
            updateLtiResultAttempts(experiment);
            buttonGroupPanel.setVisible(true);
            ltiInfoPanel.init(experiment.getSourceCourseId(), experiment.getId());
        } else if(experiment.isDataSetType(DataSetType.COURSE_DATA)){
            updateExperimentAttempts(experiment);
            setUrlVisibility(false); 
            
            if(!experiment.getStatus().equals(ExperimentStatus.ENDED)){
                // user must delete the underlying source course before being allowed to delete this auto created
                // publish course instance
                deleteButtonTooltip.setTitle("Delete the course on the 'Take a Course' page to enable.");
                deleteButton.setEnabled(canDelete());
            }
        } else {
            logger.severe("Unsupported published course type found when trying to get the results of the published course: " + experiment);
        }
        
        // Update the published course type icon.
        updateDataSetTypeIcon(experiment);
        
        DataCollectionUserRole userRole = getUserRole();
        
        applyPermissions(userRole);
    }
    
    /**
     * Query the server for the course details that can be used to further customize this widget
     */
    private void getCourseDetails() {
        
        if(!hasAskedForCourseDetails && dataCollectionItem != null) {
            dashboardService.getCourseById(UiManager.getInstance().getSessionId(), dataCollectionItem.getSourceCourseId(),
                    new AsyncCallback<GenericRpcResponse<DomainOption>>() {
                        @Override
                        public void onSuccess(GenericRpcResponse<DomainOption> response) {
                            if (response.getWasSuccessful() && response.getContent() != null) {
                                experimentSourceCourse = response.getContent();
                                logger.info("setting edit course icon visiblity to "+experimentSourceCourse.isDomainIdReadable()+" based on readability of the experiment '"+dataCollectionItem.getName()+"' course '" + dataCollectionItem.getSourceCourseId() + "'.");
    
                                hasCourseViewPermissions = experimentSourceCourse.isDomainIdReadable();
                                
                                // update widgets with additional information obtained
                                DataCollectionUserRole userRole = getUserRole();
                                applyPermissions(userRole);
                                
                                editCourseIcon.setVisible(hasCourseViewPermissions);
                            } else {
                                logger.info("hiding edit course icon for experiment '"+dataCollectionItem.getName()+"' because the course '" + dataCollectionItem.getSourceCourseId() + "' could not be retrieved on the server");
                                editCourseIcon.setVisible(false);
                            }
                        }
    
                        @Override
                        public void onFailure(Throwable t) {
                            editCourseIcon.setVisible(false);
                            
                            final boolean hasCourseFolder = StringUtils.isNotBlank(dataCollectionItem.getCourseFolder());
                            if(!(hasCourseFolder && !dataCollectionItem.isLegacyExperiment())) {
                                // only an error if NOT a legacy experiment type, i.e. newer experiments reference the workspace course folder and therefore this would be an error in those instances
                                logger.severe("Failed to retrieve course '" + dataCollectionItem.getSourceCourseId() + "' for experiment '"+dataCollectionItem.getName()+"' because "
                                        + t.getMessage());
                            }
                        }
                    });
            hasAskedForCourseDetails = true;
        }
    }
    
    /**
     * Return the user role in this data collection item.
     * @return the user role found in the permissions for this data collection item.  Null can be returned if
     * the {@link #dataCollectionItem} was not set, it doesn't have permissions or the user is not in the permissions.
     */
    private DataCollectionUserRole getUserRole(){
        
        DataCollectionUserRole userRole = null;
        if(dataCollectionItem != null &&
                dataCollectionItem.getPermissions() != null){
            for(DataCollectionPermission permission : dataCollectionItem.getPermissions()){
                
                if(StringUtils.equalsIgnoreCase(permission.getUsername(), UiManager.getInstance().getUserName())){
                    userRole = permission.getDataCollectionUserRole();
                    break;
                }
            }
        }
        
        return userRole;
    }
    
    /**
     * Return whether the user has permissions to delete this published course instance.
     * @return true if the user can delete the publish course instance.  False otherwise.  False
     * if the {@link #dataCollectionItem} is not set.
     */
    private boolean canDelete(){
        
        boolean isCourseData = dataCollectionItem != null && 
                dataCollectionItem.isDataSetType(DataSetType.COURSE_DATA);
        boolean isEndedCourseData = isCourseData && dataCollectionItem.getStatus().equals(ExperimentStatus.ENDED);
        return  (!isCourseData || isEndedCourseData) &&
                hasPermissions(deleteButton, getUserRole());
    }
    
    /**
     * Set various UI component visibility based on the user role.
     * 
     * @param userRole the role to use.  If null then every widget will be hidden.
     */
    private void applyPermissions(DataCollectionUserRole userRole){
        
        ctrlSharePublishedCourse.setVisible(hasPermissions(ctrlSharePublishedCourse, userRole));
        exportCourseButton.setVisible(hasPermissions(exportCourseButton, userRole));
        editButton.setVisible(hasPermissions(editButton, userRole));
        exportSubjectDataButton.setVisible(hasPermissions(exportSubjectDataButton, userRole));
        exportSubjectDataButtonEnded.setVisible(hasPermissions(exportSubjectDataButtonEnded, userRole));
        exportSubjectDataButtonPaused.setVisible(hasPermissions(exportSubjectDataButtonPaused, userRole));
        deleteButton.setVisible(hasPermissions(deleteButton, userRole));
    }
    
    /**
     * Return true if the user role should have access to the widget provided.
     * 
     * @param widget the widget to check permissions on.  If null, returns false.
     * @param userRole the role to check permissions for.  If null, returns false.
     * @return true if the user role has permissions to use the widget.  False otherwise.
     */
    private boolean hasPermissions(Widget widget, DataCollectionUserRole userRole){
        
        if(widget == null || userRole == null){
            return false;
        }
        
        if(widget == ctrlSharePublishedCourse){
            return userRole == DataCollectionUserRole.OWNER || userRole == DataCollectionUserRole.MANAGER;
        }else if(widget == exportCourseButton){
            // if a manager role, you must also have view permissions on the course
            return userRole == DataCollectionUserRole.OWNER || 
                    (userRole == DataCollectionUserRole.MANAGER && hasCourseViewPermissions);
        }else if(widget == editButton){
            return userRole == DataCollectionUserRole.OWNER || userRole == DataCollectionUserRole.MANAGER;
        }else if(widget == exportCourseButton || widget == exportSubjectDataButton || widget == exportSubjectDataButtonEnded ||
                widget == exportSubjectDataButtonPaused){
            //all the export buttons
            return userRole == DataCollectionUserRole.OWNER || userRole == DataCollectionUserRole.MANAGER;
        }else if(widget == deleteButton){
            //only owner can delete experiment
            return userRole == DataCollectionUserRole.OWNER;
        }else {
            return false;
        }
    }
    
    /**
     * Sets the visibility of the URL widgets (the text box and the label).
     * 
     * @param visible True to show the url widgets, false to hide them.
     */
    private void setUrlVisibility(boolean visible) {
        urlLabel.setVisible(visible);
        urlTitle.setVisible(visible);
        copyUrlButton.setVisible(visible);
    }
    
    /**
     * Updates the published course type icon that is used to show the 'type' of the published course that was created.
     * If the type is not supported an error is logged and the icon is hidden.
     * 
     * @param experiment The experiment object to check the published course type for.
     */
    private void updateDataSetTypeIcon(DataCollectionItem experiment) {
        if (experiment.isDataSetType(DataSetType.EXPERIMENT)) {
            dataSetTypeIcon.setType(IconType.FLASK);
        } else if (experiment.isDataSetType(DataSetType.LTI)) {
            dataSetTypeIcon.setType(IconType.PLUG);
        } else if(experiment.isDataSetType(DataSetType.COURSE_DATA)){
            dataSetTypeIcon.setType(IconType.BOOK);
        } else {
            logger.severe("Unsupported published course type found of: " + experiment.getDataSetType());
            dataSetTypeIcon.setType(IconType.QUESTION);
        }
    }
    
    /**
     * Updates the experiment attempts if the experiment is of type 'experiment' and has subject
     * data.
     * 
     * @param experiment The experiment data used to get the subject data from.
     */
    private void updateExperimentAttempts(DataCollectionItem experiment) {
        if (experiment.getSubjectSize() != 0) {

            numberOfAttempts.setText(Long.toString(experiment.getSubjectSize()));

            if (experiment.getSubjectLastAttemptedDate() != null) {
                latestAttempt.setText(DateTimeFormat.getFormat("h:mm:ss a EEEE, MMMM dd, yyyy").format(experiment.getSubjectLastAttemptedDate()));
            } else {
                latestAttempt.setText("N/A");
            }
        } else {
            numberOfAttempts.setText("None");
            latestAttempt.setText("N/A");
        }
    }
    
    
    /**
     * Updates the 'attempts' UI based on the experiment type if it is an lti type of published
     * course.
     * 
     * @param experiment The experiment containing the lti result data to update the UI with.
     */
    private void updateLtiResultAttempts(DataCollectionItem experiment) {
        if (experiment.getLtiResultSize() != 0) {

            numberOfAttempts.setText(Long.toString(experiment.getLtiResultSize()));

            if (experiment.getLtiResultLastAttemptedDate() != null) {
                latestAttempt.setText(DateTimeFormat.getFormat("h:mm:ss a EEEE, MMMM dd, yyyy").format(experiment.getLtiResultLastAttemptedDate()));
            } else {
                latestAttempt.setText("N/A");
            }
        } else {
            numberOfAttempts.setText("None");
            latestAttempt.setText("N/A");
        }
    }

    /**
     * Toggles whether or not this experiment's content should be visible
     */
    private void toggleContentVisible(){
        
        setContentVisible(!isContentShowing());    
    }
    
    /**
     * Show any published courses that reference the course specified. This currently expand the panel(s) for any
     * matching published courses found.  
     * @param domainOption the course to search for in the published courses.  If null this method does nothing.
     * @param dataSetType optional data set type to ignore when searching published course types.  E.g. ignore
     * experiment published courses.
     * @return true if this experiment widget was shown because it was a match.
     */
    public boolean expandPublishCourses(DomainOption domainOption, DataSetType dataSetType){
        
        DataCollectionItem dataCollectionItem = getExperiment();
        if(dataSetType != null){
            if(!dataSetType.equals(dataCollectionItem.getDataSetType())){
                // does not match the filter
                return false;
            }
        }
        
        if(domainOption.getCourseFolderPath().equalsIgnoreCase(dataCollectionItem.getCourseFolder())){
            setContentVisible(true);
            return true;
        }
        
        return false;
    }
    
    /**
     * Sets whether or not this experiment's content should be visible
     * 
     * @param visible true to show the content; false otherwise
     */
    public void setContentVisible(boolean visible){
                
        if(visible){
            
            contentDeck.showWidget(contentDeck.getWidgetIndex(contentPanel));
            
            headerCollapseIcon.setType(IconType.CHEVRON_CIRCLE_DOWN);
            footerCollapseIcon.setType(IconType.ANGLE_DOUBLE_UP);    
            
        } else {
            
            contentDeck.showWidget(contentDeck.getWidgetIndex(noContentPanel));
            
            headerCollapseIcon.setType(IconType.CHEVRON_CIRCLE_RIGHT);
            footerCollapseIcon.setType(IconType.ANGLE_DOUBLE_DOWN);
        }
    }
    
    /**
     * Gets whether or not this experiment's content is showing
     * 
     * @return whether or not this experiment's content is showing
     */
      private boolean isContentShowing(){
        return contentDeck.getVisibleWidget() == contentDeck.getWidgetIndex(contentPanel);
    } 
    
    /**
     * Perform action on url copy button click
     * 
     * @param event the click event
     */
    @UiHandler("copyUrlButton")
    protected void onCopyUrlButton(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onCopyUrlButton(" + event.toDebugString() + ")");
        }

        event.stopPropagation();

        JsniUtility.copyTextToClipboard(urlLabel.getElement());
    }
}
