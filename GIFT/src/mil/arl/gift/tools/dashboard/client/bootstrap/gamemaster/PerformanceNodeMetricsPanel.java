/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.FormatterCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.SafeHtmlUtils;
import mil.arl.gift.common.gwt.client.util.TeamsUtil;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamPicker;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.RunState;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingPriority;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.Component;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.PermissionUpdateHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;

/**
 * A panel capable of displaying {@link AbstractPerformanceState a performance node's state data}
 * that allows the game master to make changes to said state and push said changes to GIFT in order
 * to apply them to the knowledge session.
 *
 * @author nroberts
 */
public class PerformanceNodeMetricsPanel extends ValidationComposite
        implements ActiveSessionChangeHandler, PermissionUpdateHandler {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PerformanceNodeMetricsPanel.class.getName());

    /**
     * The number of instances of this widget that have been created. Used to ensure that the
     * assessment level radio buttons are unique for each instance (so that selecting a radio button
     * in one instance doesn't affect the radio buttons in other instances).
     */
    private static int instanceCount = 0;

    /** The UiBinder that combines the ui.xml with this java class */
    private static PerformanceNodeMetricsPanelUiBinder uiBinder = GWT.create(PerformanceNodeMetricsPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface PerformanceNodeMetricsPanelUiBinder extends UiBinder<Widget, PerformanceNodeMetricsPanel> {
    }

    /** The active session provider instance */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /**
     * Create a remote service proxy to talk to the server-side dashboard
     * service.
     */
    private static final DashboardServiceAsync dashboardService = UiManager.getInstance().getDashboardService();

    /** The collapse used to show/hide this widget as its parent passes state data into it */
    @UiField
    protected Collapse collapse;

    /**
     * The button that will apply the game master's changes to the performance state to the
     * knowledge session when clicked
     */
    @UiField
    protected Button applyButton;

    /** The button that cancels the game master's changes and closes this widget when clicked */
    @UiField
    protected Button closeButton;

    /**
     * The radio button used to set the performance state's assessment level to Below Expectation
     */
    @UiField(provided = true)
    protected RadioButton belowExpectationButton;

    /** The radio button used to set the performance state's assessment level to At Expectation */
    @UiField(provided = true)
    protected RadioButton atExpectationButton;

    /**
     * The radio button used to set the performance state's assessment level to Above Expectation
     */
    @UiField(provided = true)
    protected RadioButton aboveExpectationButton;

    /**
     * The tooltip telling the user how to lock and unlock the performance state's assessment level
     */
    @UiField
    protected ManagedTooltip lockAssessmentTooltip;

    /** The button used to lock and unlock the performance state's assessment level */
    @UiField
    protected Button lockAssessmentButton;

    /** The header for the {@link #advancedMetricsCollapse} */
    @UiField
    protected PanelHeader advancedMetricsHeader;

    /** Icon showing if the {@link #advancedMetricsCollapse} is open or closed */
    @UiField
    protected Icon advancedMetricsHeaderIcon;

    /** The collapse containing the advanced metrics */
    @UiField
    protected Collapse advancedMetricsCollapse;

    /** The slider used to set the performance state's confidence level */
    @UiField
    protected Slider confidenceSlider;

    /**
     * The tooltip telling the user how to lock and unlock the performance state's confidence level
     */
    @UiField
    protected ManagedTooltip lockConfidenceTooltip;

    /** The button used to lock and unlock the performance state's confidence level */
    @UiField
    protected Button lockConfidenceButton;

    /** The slider used to set the performance state's competence level */
    @UiField
    protected Slider competenceSlider;

    /**
     * The tooltip telling the user how to lock and unlock the performance state's competence level
     */
    @UiField
    protected ManagedTooltip lockCompetenceTooltip;

    /** The button used to lock and unlock the performance state's competence level */
    @UiField
    protected Button lockCompetenceButton;

    /** The slider used to set the performance state's trend level */
    @UiField
    protected Slider trendSlider;

    /**
     * The tooltip telling the user how to lock and unlock the performance state's trend level
     */
    @UiField
    protected ManagedTooltip lockTrendTooltip;

    /** The button used to lock and unlock the performance state's trend level */
    @UiField
    protected Button lockTrendButton;

    /** The number spinner used to set the performance state's priority level */
    @UiField
    protected NumberSpinner prioritySpinner;

    /**
     * The tooltip telling the user how to lock and unlock the performance state's priority level
     */
    @UiField
    protected ManagedTooltip lockPriorityTooltip;

    /** The button used to lock and unlock the performance state's priority level */
    @UiField
    protected Button lockPriorityButton;

    /** The panel containing the team picker */
    @UiField
    protected FlowPanel teamPickerPanel;

    /** The team picker */
    private final TeamPicker teamPicker;

    /**
     * A flag indicating if {@link #teamPicker} is set to a user supplied value
     * that does not match the state of the backing data.
     */
    private boolean isTeamPickerDirty = false;

    /** The text area for the user to input additional information about the entered metrics */
    @UiField
    protected TextArea descriptionTextArea;
    
    /** A wrapper around the description area used to create and save recordings  */
    @UiField
    protected RecordingBooth recorder;

    /** The label displaying the task's last assessment time */
    @UiField
    protected Label conceptLastAssessmentTime;
    
    /** the panel containing the task difficulty reason elements */
    @UiField
    protected FlowPanel difficultyReasonPanel;
    
    /** the panel contain the task stress reason elements */
    @UiField
    protected FlowPanel stressReasonPanel;
    
    /** the task difficulty reason label */
    @UiField
    protected Label difficultyReason;
    
    /** the task stress reason label */
    @UiField
    protected Label stressReason;

    /** The performance node state data currently being represented by this panel */
    private AbstractPerformanceState currentState;

    /** the time (epoch) at which this performance node metrics panel was open for editing */
    private long currentEditTimestamp;

    /** The current knowledge session */
    private final AbstractKnowledgeSession knowledgeSession;

    /** The list of callback to execute when the apply button is clicked */
    private final List<ApplyCallback> applyCallbacks = new ArrayList<>();

    /**
     * The default list of team org entries to set into the {@link #teamPicker}
     * on reset
     */
    private List<String> defaultTeamOrgEntries = null;

    /**
     * current set of enumerated components that should be hidden from the user
     * due to the current enumerated mode the game master is in
     */
    private Set<Component> disallowedComponents = new HashSet<>();

    /** Validation widget for the team picker */
    private final WidgetValidationStatus teamPickerValidation;

    /** The callback for when the apply button is clicked */
    interface ApplyCallback {
        /**
         * Executes some logic whenever the apply button is clicked
         *
         * @param updateRequest the request that was sent when the apply button
         *        was clicked.
         */
        void onApply(EvaluatorUpdateRequest updateRequest);
    }

    /**
     * Creates a new panel that can display and modify performance node state data
     * @param knowledgeSession the current knowledge session. Can't be null.
     */
    public PerformanceNodeMetricsPanel(AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        belowExpectationButton = new RadioButton("metricsPanelAssessment-" + instanceCount);
        atExpectationButton = new RadioButton("metricsPanelAssessment-" + instanceCount);
        aboveExpectationButton = new RadioButton("metricsPanelAssessment-" + instanceCount);
        instanceCount++;

        final Team teamStructure = knowledgeSession.getTeamStructure();
        if (teamStructure == null || teamStructure.getNumberOfTeamMembers() == 0) {
            teamPicker = null;
            teamPickerValidation = new WidgetValidationStatus(
                    "If you are seeing this error message, something has gone wrong.");
        } else {
            generated.dkf.Team dkfTeam = TeamsUtil.convertToDkfTeam(teamStructure);
            teamPicker = new TeamPicker(dkfTeam, true);
            teamPicker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
                @Override
                public void onValueChange(ValueChangeEvent<List<String>> event) {
                    requestValidation(teamPickerValidation);
                    isTeamPickerDirty = true;
                }
            });
            teamPickerValidation = new WidgetValidationStatus(teamPicker.getTextBoxRef(),
                    "The team picker must be populated.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        this.knowledgeSession = knowledgeSession;

        /* If there is a team, add the picker to the panel; remove the panel if
         * there isn't */
        if (teamPicker == null) {
            teamPickerPanel.removeFromParent();
        } else {
            teamPickerPanel.add(teamPicker);
        }

        advancedMetricsHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("advancedMetricsHeader.onClick(" + event.toDebugString() + ")");
                }

                if (advancedMetricsCollapse.isShown()) {
                    advancedMetricsCollapse.hide();
                    advancedMetricsHeaderIcon.setType(IconType.CARET_RIGHT);
                } else {
                    advancedMetricsCollapse.show();
                    advancedMetricsHeaderIcon.setType(IconType.CARET_DOWN);
                }
            }
        }, ClickEvent.getType());

        AssessmentLevelIcon assessmentIcon = new AssessmentLevelIcon();

        assessmentIcon.setAssessmentLevel(AssessmentLevelEnum.BELOW_EXPECTATION, false);
        belowExpectationButton.setHTML(assessmentIcon.getElement().getString());

        assessmentIcon.setAssessmentLevel(AssessmentLevelEnum.AT_EXPECTATION, false);
        atExpectationButton.setHTML(assessmentIcon.getElement().getString());

        assessmentIcon.setAssessmentLevel(AssessmentLevelEnum.ABOVE_EXPECTATION, false);
        aboveExpectationButton.setHTML(assessmentIcon.getElement().getString());

        manageClickEvents(belowExpectationButton);
        manageClickEvents(atExpectationButton);
        manageClickEvents(aboveExpectationButton);

        setAssessmentLocked(false);
        setConfidenceLocked(false);
        setCompetenceLocked(false);
        setTrendLocked(false);
        setPriorityLocked(false);

        FormatterCallback<Double> metricSliderFormatter = new FormatterCallback<Double>() {
            @Override
            public String formatTooltip(final Double value) {
                return value + "%";
            }
        };

        confidenceSlider.setFormatter(metricSliderFormatter);
        competenceSlider.setFormatter(metricSliderFormatter);
        trendSlider.setFormatter(metricSliderFormatter);

        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                /* need to prevent parent panel from receiving this click so it
                 * doesn't immediately try to re-open this panel */
                event.stopPropagation();

                isTeamPickerDirty = false;
                setMetricsState(null);
                requestValidation(teamPickerValidation);
            }
        });

        applyButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                /* need to prevent parent panel from receiving this click so it
                 * doesn't immediately try to re-open this panel */
                event.stopPropagation();

                /* Can't submit with an invalid team picker */
                if (!teamPickerValidation.isValid()) {
                    return;
                }

                applyEnteredMetrics();
            }
        });

        lockAssessmentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                event.stopPropagation();

                setAssessmentLocked(!isAssessmentLocked());

                // animate changing the lock button
                String animationStyle = Animate.animate(lockAssessmentButton, Animation.FLIP_IN_X);
                Animate.removeAnimationOnEnd(lockAssessmentButton, animationStyle);
            }
        });

        lockConfidenceButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                event.stopPropagation();

                setConfidenceLocked(!isConfidenceLocked());

                // animate changing the lock button
                String animationStyle = Animate.animate(lockConfidenceButton, Animation.FLIP_IN_X);
                Animate.removeAnimationOnEnd(lockConfidenceButton, animationStyle);
            }
        });

        lockCompetenceButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                event.stopPropagation();

                setCompetenceLocked(!isCompetenceLocked());

                // animate changing the lock button
                String animationStyle = Animate.animate(lockCompetenceButton, Animation.FLIP_IN_X);
                Animate.removeAnimationOnEnd(lockCompetenceButton, animationStyle);
            }
        });

        lockTrendButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                event.stopPropagation();

                setTrendLocked(!isTrendLocked());

                // animate changing the lock button
                String animationStyle = Animate.animate(lockTrendButton, Animation.FLIP_IN_X);
                Animate.removeAnimationOnEnd(lockTrendButton, animationStyle);
            }
        });

        lockPriorityButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                event.stopPropagation();

                setPriorityLocked(!isPriorityLocked());

                // animate changing the lock button
                String animationStyle = Animate.animate(lockPriorityButton, Animation.FLIP_IN_X);
                Animate.removeAnimationOnEnd(lockPriorityButton, animationStyle);
            }
        });

        /* Subscribe to any providers */
        subscribe();

        initValidationComposite(new ValidationWidget(this));
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Note: this must be done after binding */

        /* Subscribe to the active session changes */
        activeSessionProvider.addHandler(this);

        /* Subscribe to the permissions provider */
        PermissionsProvider.getInstance().addHandler(this);
    }

    /**
     * Unsubscribe from all providers. This should only be done before the panel
     * is destroyed.
     */
    private void unsubscribe() {
        /* Remove handlers */
        activeSessionProvider.removeHandler(this);
        PermissionsProvider.getInstance().removeHandler(this);
    }

    /**
     * Add a callback to be executed whenever the apply button is clicked.
     *
     * @param callback the callback to execute. Can't be null.
     */
    public void addApplyHandler(ApplyCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        applyCallbacks.add(callback);
    }

    /**
     * Adds a {@link ClickHandler} to the {@link #closeButton}.
     *
     * @param handler The {@link ClickHandler} to subscribe to
     *        {@link #closeButton}. Can't be null.
     * @return The {@link HandlerRegistration} that can be used to cancel the
     *         event subscription. Can't be null.
     */
    public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
        return closeButton.addClickHandler(handler);
    }

    /**
     * Applies the changes that the game master user has made to the performance
     * node state through the UI by sending them to GIFT to be pushed to the
     * appropriate domain knowledge session.
     */
    private void applyEnteredMetrics() {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Applying entered performance node metrics");
        }

        if (currentState == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Unable to apply entered performance node metrics because current state is null");
            }
            return;
        }

        /* Create an update request using the data entered by the user into the
         * UI */
        final EvaluatorUpdateRequest updateRequest = new EvaluatorUpdateRequest(currentState.getState().getName(),
                BsGameMasterPanel.getGameMasterUserName(), currentEditTimestamp);

        /* Update request with assessment metrics */
        AssessmentLevelEnum assessment;
        if (aboveExpectationButton.isActive()) {
            assessment = AssessmentLevelEnum.ABOVE_EXPECTATION;
        } else if (atExpectationButton.isActive()) {
            assessment = AssessmentLevelEnum.AT_EXPECTATION;
        } else if (belowExpectationButton.isActive()) {
            assessment = AssessmentLevelEnum.BELOW_EXPECTATION;
        } else {
            assessment = null;
        }
        updateRequest.setPerformanceMetric(assessment);
        updateRequest.setAssessmentHold(isAssessmentLocked());

        /* Update request with confidence metrics */
        final Float confidence = convertSliderToState(confidenceSlider.getValue());
        if (confidence != null) {
            updateRequest.setConfidenceMetric(confidence.floatValue());
        }
        updateRequest.setConfidenceHold(isConfidenceLocked());

        /* Update request with competence metrics */
        final Float competence = convertSliderToState(competenceSlider.getValue());
        if (competence != null) {
            updateRequest.setCompetenceMetric(competence.floatValue());
        }
        updateRequest.setCompetenceHold(isCompetenceLocked());

        /* Update request with priority metrics */
        final Integer priority = prioritySpinner.getValue();
        updateRequest.setPriorityMetric(priority != 0 ? priority : null);
        updateRequest.setPriorityHold(isPriorityLocked());

        /* Update request with trend metrics */
        final Float trend = convertSliderToState(trendSlider.getValue());
        if (trend != null) {
            updateRequest.setTrendMetric(trend.floatValue());
        }
        updateRequest.setTrendHold(isTrendLocked());

        /* Update request with the optional reason */
        final String reason = descriptionTextArea.getText();
        if (StringUtils.isNotBlank(reason)) {
            updateRequest.setReason(reason);
        }

        /* Update request with the optional team org entities */
        if (teamPicker != null) {
            Map<String, AssessmentLevelEnum> memberAssessments = new HashMap<>();

            AssessmentLevelEnum nullSafeAssessment = assessment != null ? assessment : AssessmentLevelEnum.UNKNOWN;
            for (String selectedMember : teamPicker.getSelectedTeamMembers()) {
                memberAssessments.put(selectedMember, nullSafeAssessment);
            }

            if (CollectionUtils.isNotEmpty(memberAssessments)) {
                updateRequest.setTeamOrgEntities(memberAssessments);
            }
        }

        //handle when the update request has been fully processed
        AsyncCallback<String> onRequestProcessed = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                
                UiManager.getInstance().displayErrorDialog(
                        "Unable to Save Recording", 
                        "Your recording could not be saved to the server due to an unforeseen problem, so your "
                        + "changes have not been applied.<br/><br/>You can still apply the rest of your changes "
                        + "if you remove your recording using the button next to it.", null);
            }

            @Override
            public void onSuccess(String savedRecording) {
                
                if(StringUtils.isNotBlank(savedRecording)) {
                    
                    //modify the request if a recording was saved to ensure its reference is written
                    updateRequest.setMediaFile(savedRecording);
                }
                
                sendRequest();
            }
            
            /**
             * Sends out the built performance node metrics on an update request
             */
            private void sendRequest() {
                
                /* Send the update request to GIFT */
                isTeamPickerDirty = false;

                if (knowledgeSession.inPastSessionMode()) {
                    /* Create patch for changes and apply to timeline */
                    
                    ConfirmationDialogCallback confirmCallback = new ConfirmationDialogCallback() {
                        
                        private void applyPatch(boolean applyToFutureStates) {
                            
                            LoadingDialogProvider.getInstance().startLoading(LoadingType.TIMELINE_REFRESH, LoadingPriority.HIGH, 
                                    "Applying Assessments",
                                    "Please wait while the updated assessments are rendered onto the timeline...");
                            
                            Long timestamp = TimelineProvider.getInstance().getPlaybackTime();
                            dashboardService.createLogPatchForEvaluatorUpdate(
                                    BrowserSession.getInstance().getBrowserSessionKey(),
                                    BsGameMasterPanel.getGameMasterUserName(), timestamp,
                                    !Dashboard.getInstance().getSettings().isApplyChangesAtPlayhead(), updateRequest, applyToFutureStates,
                                    new AsyncCallback<GenericRpcResponse<String>>() {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            logger.warning("Failed to create patch file because " + caught.getMessage());
                                            
                                            TimelineProvider.getInstance().reloadTimeline();
                                        }

                                        @Override
                                        public void onSuccess(GenericRpcResponse<String> result) {
                                            
                                            if(result.getContent() != null) {
                                                if (logger.isLoggable(Level.INFO)) {
                                                    logger.info("Successfully wrote patch file for evaluator update.");
                                                }
    
                                                /* Update log metadata patch file name */
                                                RegisteredSessionProvider.getInstance().updateLogPatchFile(result.getContent());
                                            }
                                            
                                            if(!result.getWasSuccessful()) {
                                                
                                                boolean isLrsError = result.getContent() != null;
                                                String title = isLrsError 
                                                        ? "Unable to publish assessment to external system"
                                                        : "Failed to save assessment";
                                                
                                                UiManager.getInstance().displayDetailedErrorDialog(
                                                        title,
                                                        result.getException().getReason(), 
                                                        result.getException().getDetails(),
                                                        result.getException().getErrorStackTrace(),
                                                        null);
                                            }
                                            
                                            TimelineProvider.getInstance().reloadTimeline();
                                        }
                                    });
                        }
                        
                        @Override
                        public void onDecline() {
                            applyPatch(false);
                        }
                        
                        @Override
                        public void onAccept() {
                            applyPatch(true);
                        }
                    };
                    
                    promptApplyIfNeeded(updateRequest, currentState.getState(), confirmCallback);
                    
                } else {
                    /* Send live message */
                    BrowserSession.getInstance()
                            .sendWebSocketMessage(new DashboardMessage(updateRequest, knowledgeSession));
                }

                /* Notify listeners that the apply has been clicked */
                for (ApplyCallback callback : applyCallbacks) {
                    callback.onApply(updateRequest);
                }

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Finished applying entered performance node metrics");
                }
                
                setMetricsState(null);
            }
        };
        
        if(!recorder.hasRecording()) {
            
            //if there's no recording to save, push the update request immediately
            onRequestProcessed.onSuccess(null);
            
        } else {
            
            /*
             * If possible, gather data about this session's host domain session to pass to the recorder servlet.
             * This will ensure that recordings associated with a domain session are saved to that domain session's
             * output folder.
             */
            SessionMember host = knowledgeSession.getHostSessionMember();
            RecorderParams params = new RecorderParams();
            
            if(host != null) {
                params.setUserId(host.getUserSession().getUserId())
                    .setDomainSessionId(host.getDomainSessionId())
                    .setExperimentId(host.getUserSession().getExperimentId());
            }
            
            //if the user made a recording, need to save it first and process the update request before sending it
            recorder.saveRecording(params, onRequestProcessed);
        }
    }

    /**
     * Converts the slider value to the state value
     *
     * @param sliderValue the slider value to convert
     * @return the converted state value
     */
    private float convertSliderToState(Double sliderValue) {
        return Float.valueOf(String.valueOf(sliderValue.intValue() / 100.0));
    }

    /**
     * Converts the state value to the slider value
     *
     * @param stateValue the state value to convert
     * @return the converted slider value
     */
    private Double convertStateToSlider(float stateValue) {
        return (double) Math.round(stateValue * 100.0);
    }

    /**
     * Disable click events on the button when the button is disabled. This is
     * to work around the issue with GWT Bootstrap CheckBoxButton class
     * setEnabled() method which doesn't actually do what it's supposed to. It
     * visually disables the element, but it doesn't actually stop it from
     * receiving click events.
     *
     * @param button the button to apply custom click handling to when the
     *        button is disabled
     */
    private void manageClickEvents(final RadioButton button) {

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!button.isEnabled()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });

        button.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                requestValidation(teamPickerValidation);
            }
        });
    }

    /**
     * Sets the performance node state that the game master user should be able
     * to modify metrics for. If not null, this panel will update its UI
     * components to match the given performance state. If null, this panel will
     * be hidden.
     *
     * @param state the performance node state whose metrics should be shown, or
     *        null, if no metrics should be shown
     */
    public void setMetricsState(AbstractPerformanceState state) {

        this.currentState = state;

        if (state != null) {

            currentEditTimestamp = System.currentTimeMillis();

            AssessmentLevelEnum assessment = null;

            if (state.getState() != null) {
                assessment = state.getState().getShortTerm();
            }

            /* If it hasn't been activated yet, ignore it's current state
             * value */
            final boolean ignoreState = PerformanceNodeStateEnum.UNACTIVATED
                    .equals(state.getState().getNodeStateEnum());

            boolean isAboveExpectation = !ignoreState && AssessmentLevelEnum.ABOVE_EXPECTATION.equals(assessment);
            boolean isAtExpectation = !ignoreState && !isAboveExpectation && AssessmentLevelEnum.AT_EXPECTATION.equals(assessment);
            boolean isBelowExpectation = !ignoreState && !isAtExpectation && AssessmentLevelEnum.BELOW_EXPECTATION.equals(assessment);

            aboveExpectationButton.setActive(isAboveExpectation);
            atExpectationButton.setActive(isAtExpectation);
            belowExpectationButton.setActive(isBelowExpectation);

            confidenceSlider.setValue(convertStateToSlider(state.getState().getConfidence()));
            competenceSlider.setValue(convertStateToSlider(state.getState().getCompetence()));
            trendSlider.setValue(convertStateToSlider(state.getState().getTrend()));
            final Integer priority = state.getState().getPriority();
            prioritySpinner.setValue(priority == null || priority < 1 ? 0 : priority);
            
            if(state instanceof TaskPerformanceState) {
                TaskPerformanceState tState = (TaskPerformanceState)state;
                if(StringUtils.isNotBlank(tState.getDifficultyReason())){
                    difficultyReason.setText(tState.getDifficultyReason());
                    difficultyReasonPanel.setVisible(true);
                }else {
                    difficultyReasonPanel.setVisible(false);
                }
                
                if(StringUtils.isNotBlank(tState.getStressReason())){
                    stressReason.setText(tState.getStressReason());
                    stressReasonPanel.setVisible(true);
                }else {
                    stressReasonPanel.setVisible(false);
                }
            }else {
                // only for tasks, not concepts
                difficultyReasonPanel.setVisible(false);
                stressReasonPanel.setVisible(false);
            }

            setAssessmentLocked(state.getState().isAssessmentHold());
            setConfidenceLocked(state.getState().isConfidenceHold());
            setCompetenceLocked(state.getState().isCompetenceHold());
            setTrendLocked(state.getState().isTrendHold());
            setPriorityLocked(state.getState().isPriorityHold());

            if (teamPicker != null && !isTeamPickerDirty) {
                teamPicker.setValue(defaultTeamOrgEntries, false);
                requestValidation(teamPickerValidation);
            }

            descriptionTextArea.clear();
            recorder.resetRecording();

            refreshTimerWidgets();

            collapse.show();

        } else {
            collapse.hide();
            
            currentEditTimestamp = 0; // reset
        }
    }

    /**
     * Gets the performance node state whose metrics are being displayed to the user, or null if no metrics have been provided
     * and this widget is hidden.
     *
     * @return the current performance node state, or null if no such state is available.
     */
    public AbstractPerformanceState getMetricsState() {
        return currentState;
    }

    /**
     * Sets whether or not the assessment level for this performance state should be locked and visually adjusts the lock button accordingly
     *
     * @param locked whether the assessment level should be locked
     */
    private void setAssessmentLocked(boolean locked) {
        lockAssessmentButton.setActive(locked);
        lockAssessmentButton.setType(locked ? ButtonType.PRIMARY : ButtonType.DEFAULT);
        lockAssessmentButton.setIcon(locked ? IconType.LOCK : IconType.UNLOCK_ALT);
        lockAssessmentTooltip.setTitle(locked
                ? "This assessment has been locked so that it cannot be modified by the GIFT system automatically.<br/><br/>Click "
                        + "to unlock this assessment so that GIFT can change it."
                : "This assessment is unlocked and can be modified by the GIFT system automatically.<br/><br/>Click to lock this "
                        + "assessment so that GIFT cannot change it.");
    }

    /**
     * Gets whether or not the assessment level for this performance state should be locked
     *
     * @return whether the assessment level should be locked
     */
    private boolean isAssessmentLocked() {
        return lockAssessmentButton.isActive();
    }

    /**
     * Sets whether or not the confidence level for this performance state should be locked and
     * visually adjusts the lock button accordingly
     *
     * @param locked whether the confidence level should be locked
     */
    private void setConfidenceLocked(boolean locked) {
        lockConfidenceButton.setActive(locked);
        lockConfidenceButton.setType(locked ? ButtonType.PRIMARY : ButtonType.DEFAULT);
        lockConfidenceButton.setIcon(locked ? IconType.LOCK : IconType.UNLOCK_ALT);
        lockConfidenceTooltip.setTitle(locked
                ? "This confidence has been locked so that it cannot be modified by the GIFT system automatically.<br/><br/>Click "
                        + "to unlock this confidence so that GIFT can change it."
                : "This confidence is unlocked and can be modified by the GIFT system automatically.<br/><br/>Click to lock this "
                        + "confidence so that GIFT cannot change it.");
    }

    /**
     * Gets whether or not the confidence level for this performance state should be locked
     *
     * @return whether the confidence level should be locked
     */
    private boolean isConfidenceLocked() {
        return lockConfidenceButton.isActive();
    }

    /**
     * Sets whether or not the competence level for this performance state should be locked and
     * visually adjusts the lock button accordingly
     *
     * @param locked whether the competence level should be locked
     */
    private void setCompetenceLocked(boolean locked) {
        lockCompetenceButton.setActive(locked);
        lockCompetenceButton.setType(locked ? ButtonType.PRIMARY : ButtonType.DEFAULT);
        lockCompetenceButton.setIcon(locked ? IconType.LOCK : IconType.UNLOCK_ALT);
        lockCompetenceTooltip.setTitle(locked
                ? "This competence has been locked so that it cannot be modified by the GIFT system automatically.<br/><br/>Click "
                        + "to unlock this competence so that GIFT can change it."
                : "This competence is unlocked and can be modified by the GIFT system automatically.<br/><br/>Click to lock this "
                        + "competence so that GIFT cannot change it.");
    }

    /**
     * Gets whether or not the competence level for this performance state should be locked
     *
     * @return whether the competence level should be locked
     */
    private boolean isCompetenceLocked() {
        return lockCompetenceButton.isActive();
    }

    /**
     * Sets whether or not the trend level for this performance state should be locked and visually
     * adjusts the lock button accordingly
     *
     * @param locked whether the trend level should be locked
     */
    private void setTrendLocked(boolean locked) {
        lockTrendButton.setActive(locked);
        lockTrendButton.setType(locked ? ButtonType.PRIMARY : ButtonType.DEFAULT);
        lockTrendButton.setIcon(locked ? IconType.LOCK : IconType.UNLOCK_ALT);
        lockTrendTooltip.setTitle(locked
                ? "This trend has been locked so that it cannot be modified by the GIFT system automatically.<br/><br/>Click "
                        + "to unlock this trend so that GIFT can change it."
                : "This trend is unlocked and can be modified by the GIFT system automatically.<br/><br/>Click to lock this "
                        + "trend so that GIFT cannot change it.");
    }

    /**
     * Gets whether or not the trend level for this performance state should be locked
     *
     * @return whether the trend level should be locked
     */
    private boolean isTrendLocked() {
        return lockTrendButton.isActive();
    }

    /**
     * Sets whether or not the priority level for this performance state should be locked and
     * visually adjusts the lock button accordingly
     *
     * @param locked whether the priority level should be locked
     */
    private void setPriorityLocked(boolean locked) {
        lockPriorityButton.setActive(locked);
        lockPriorityButton.setType(locked ? ButtonType.PRIMARY : ButtonType.DEFAULT);
        lockPriorityButton.setIcon(locked ? IconType.LOCK : IconType.UNLOCK_ALT);
        lockPriorityTooltip.setTitle(locked
                ? "This priority has been locked so that it cannot be modified by the GIFT system automatically.<br/><br/>Click "
                        + "to unlock this priority so that GIFT can change it."
                : "This priority is unlocked and can be modified by the GIFT system automatically.<br/><br/>Click to lock this "
                        + "priority so that GIFT cannot change it.");
    }

    /**
     * Gets whether or not the priority level for this performance state should be locked
     *
     * @return whether the priority level should be locked
     */
    private boolean isPriorityLocked() {
        return lockPriorityButton.isActive();
    }

    /**
     * Refreshes any widget that has a timestamp or something else that needs frequent updating.
     */
    public void refreshTimerWidgets() {
        updateConceptLastAssessmentTimeLabel();
    }

    /**
     * Updates the last assessment time label with the latest timestamp. Does nothing if the
     * {@link #currentState} is null.
     */
    private void updateConceptLastAssessmentTimeLabel() {
        if (currentState == null) {
            return;
        }

        long lastAssessmentTime = currentState.getState().getShortTermTimestamp();

        long timeDiff;
        if (knowledgeSession.inPastSessionMode()) {
            /* Get timeline current play time */
            timeDiff = TimelineProvider.getInstance().getPlaybackTime() - lastAssessmentTime;
        } else {
            /* Get current time */
            timeDiff = System.currentTimeMillis() - lastAssessmentTime;
        }

        int seconds = ((Long) (timeDiff / 1000)).intValue();
        String timeDisplay = FormattedTimeBox.getDisplayText(seconds, true);

        StringBuilder sb = new StringBuilder();
        // show both the time and elapsed time
        sb.append(TaskDataPanel.getFormattedTimestamp(lastAssessmentTime));
        sb.append(" (").append(timeDisplay).append(")");

        conceptLastAssessmentTime.setText(sb.toString());
    }

    /**
     * Set the default team organization entries.
     *
     * @param teamOrgEntries the team organization entries to use as a default
     *        selection.
     */
    public void setDefaultTeamOrgEntries(Set<String> teamOrgEntries) {
        defaultTeamOrgEntries = new ArrayList<>(teamOrgEntries);
        if (teamPicker != null && !isTeamPickerDirty) {
            teamPicker.setValue(defaultTeamOrgEntries, false);
            requestValidation(teamPickerValidation);
        }
    }

    /**
     * Update the visibility of the apply button
     */
    public void updateApplyButtonVisibility() {
        final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        boolean running = RunState.RUNNING.equals(activeSessionProvider.getRunState(dsId));

        boolean disallowed = disallowedComponents.contains(Component.EVALUATOR_UPDATE);

        applyButton.setVisible(running && !disallowed);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(teamPickerValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (teamPickerValidation.equals(validationStatus)) {
            /*- Picker is valid if:
             * 1. Team picker is not visible
             * 2. The node has no team refs
             * 3. The current state is null
             */
            if (teamPicker == null || CollectionUtils.isEmpty(defaultTeamOrgEntries) || currentState == null
                    || currentState.getState() == null) {
                teamPickerValidation.setValid();
                return;
            }

            AssessmentLevelEnum oldAssessment = currentState.getState().getShortTerm();
            AssessmentLevelEnum newAssessment;
            if (aboveExpectationButton.isActive()) {
                newAssessment = AssessmentLevelEnum.ABOVE_EXPECTATION;
            } else if (atExpectationButton.isActive()) {
                newAssessment = AssessmentLevelEnum.AT_EXPECTATION;
            } else if (belowExpectationButton.isActive()) {
                newAssessment = AssessmentLevelEnum.BELOW_EXPECTATION;
            } else {
                newAssessment = null;
            }

            boolean stateChanged;
            if (oldAssessment == null || AssessmentLevelEnum.UNKNOWN.equals(oldAssessment)) {
                boolean isNewUnknown = newAssessment == null || AssessmentLevelEnum.UNKNOWN.equals(newAssessment);
                stateChanged = !isNewUnknown;
            } else {
                stateChanged = !oldAssessment.equals(newAssessment);
            }

            /*- Team picker is valid if:
             * 1. The state didn't change
             * 2. The state did change and at least one team member is selected
             */
            teamPickerValidation.setValidity(!stateChanged || !teamPicker.getSelectedTeamMembers().isEmpty());
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        /* None */
    }

    @Override
    protected void fireDirtyEvent(Serializable sourceObject) {
        /* Nothing to fire */
    }

    @Override
    public void permissionUpdate(Set<Component> disallowedComponents) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Setting disallowedComponents to " + disallowedComponents);
        }

        this.disallowedComponents = disallowedComponents;

        /* Update UI components */
        updateApplyButtonVisibility();
    }

    @Override
    public void sessionAdded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }

        /* Update UI components */
        updateApplyButtonVisibility();
    }

    @Override
    public void sessionEnded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }

        /* If the session represented by this panel is no longer active, then
         * unsubscribe from all providers because this widget will be removed */
        if (!activeSessionProvider.isActiveSession(knowledgeSession.getHostSessionMember().getDomainSessionId())) {
            unsubscribe();
        }

        /* Update UI components */
        updateApplyButtonVisibility();
    }

    /**
     * Performs checks on the update request and prompts the user for guidance.
     * <br/>
     * 1. If the given update request is before the task/concept has been
     * activated prompt the user to find out if they want to move the playhead
     * to the start of the task/concept and apply the patch there. <br/>
     * 2. If the request changes a performance node's lock state, then this
     * method will display a prompt to the user asking if they want to apply the
     * lock to future messages.
     * 
     * @param updateRequest the update request to apply. Cannot be null.
     * @param state the performance state used to check the current activation
     *        and lock state. Cannot be null.
     * @param confirmCallback the callback that should be used to handle the
     *        user's response. Cannot be null.
     */
    public static void promptApplyIfNeeded(EvaluatorUpdateRequest updateRequest, PerformanceStateAttribute state,
            ConfirmationDialogCallback confirmCallback) {
        if (updateRequest == null) {
            throw new IllegalArgumentException("The parameter 'updateRequest' cannot be null.");
        } else if (state == null) {
            throw new IllegalArgumentException("The parameter 'state' cannot be null.");
        } else if (confirmCallback == null) {
            throw new IllegalArgumentException("The parameter 'confirmCallback' cannot be null.");
        }

        final Command lockCheckCmd = new Command() {
            @Override
            public void execute() {
                if (updateRequest.isAssessmentHold() && updateRequest.isAssessmentHold() != state.isAssessmentHold()) {

            String lock = updateRequest.isAssessmentHold() ? "Lock" : "Unlock";
            String name = state.getName();

            StringBuilder sb = new StringBuilder();
            sb.append("Would you like to ").append(lock.toLowerCase()).append(" the current assessment for the remainder of the session timeline?<br/><br/>");
            
            if(updateRequest.isAssessmentHold()) {
                sb.append("This will change all future assessments for <b>").append(name).append("</b> to the current assessment value, similar to how ").append(
                    "locking works during active sessions. ");
            }

            /* If metric values have changed, ask the user */
                    UiManager.getInstance().displayConfirmDialog(lock + " Assessment for Future States?", sb.toString(),
                    confirmCallback);

        } else {
            confirmCallback.onDecline();
        }
    }
        };

        /* First check if the state is activated */
        final boolean hasActivated = !PerformanceNodeStateEnum.UNACTIVATED.equals(state.getNodeStateEnum());

        if (hasActivated) {
            lockCheckCmd.execute();
        } else {
            /* If the state hasn't been activated yet, ask the user to cancel or
             * update the playhead to when the state is active and apply the
             * update there. */

            UiManager.getInstance().displayConfirmDialog("Inactive State Detected",
                    "You have attempted to make a change before "
                            + SafeHtmlUtils.bold(updateRequest.getNodeName()).asString()
                            + " has been activated. Do you wish to cancel or move the playhead to when it activates and apply the update there?",
                    "Move and Apply", "Cancel", new ConfirmationDialogCallback() {

                        @Override
                        public void onDecline() {
                            /* Cancel - Do nothing */
                        }

                        @Override
                        public void onAccept() {
                            UiManager.getInstance().getDashboardService().jumpToActivationStart(
                                    BrowserSession.getInstance().getBrowserSessionKey(), updateRequest.getNodeName(),
                                    new AsyncCallback<GenericRpcResponse<Long>>() {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            logger.warning("Failed to retrieve the starting learner state for "
                                                    + updateRequest.getNodeName() + " because " + caught.getMessage());
                                        }

                                        @Override
                                        public void onSuccess(GenericRpcResponse<Long> result) {
                                            final Long startState = result.getContent();
                                            if (startState != null) {
                                                TimelineProvider.getInstance().setPlaybackTime(startState);
                                                lockCheckCmd.execute();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }
}
