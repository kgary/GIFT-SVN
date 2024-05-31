/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.PerformanceNodeMetricsPanel.ApplyCallback;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider;

/**
 * A panel capable of displaying {@link ConceptPerformanceState a concept's performance state data} and updating it visually in real-time
 * as new data is provided.
 *
 * @author nroberts
 */
public class LeafConceptDataPanel extends Composite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LeafConceptDataPanel.class.getName());

    /** The style name that should be applied to concepts that require the user to provide an observed assessment */
    private static final String OBSERVED_ASSESSMENT_STYLE_NAME = "nodeObservedAssessment";
    
    /** the style name that should be applied to concepts that are below expectation */
    private static final String BELOW_EXPECTATION_STYLE_NAME = "nodeBelowExpectationAssessment";

    /** The UiBinder that combines the ui.xml with this java class */
    private static ConceptDataPanelUiBinder uiBinder = GWT.create(ConceptDataPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ConceptDataPanelUiBinder extends UiBinder<Widget, LeafConceptDataPanel> {
    }

    /**
     * Interface for the style names used in the ui.xml template of the
     * {@link ConceptDataPanel}.
     *
     * @author tflowers
     *
     */
    interface Style extends CssResource {
        /**
         * Gets the name of the style applied to the
         * {@link ConceptDataPanel#conceptDescriptionPanel} when the
         * {@link ConceptDataPanel#metricsPanel} is expanded.
         *
         * @return The name of the style.
         */
        String expanded();
    }

    /** The instance of the {@link Style} interface. */
    @UiField
    protected Style style;

    /** The panel containing the concept's description (i.e. name, assessment level, etc.)*/
    @UiField
    protected Widget conceptDescriptionPanel;

    /** The label representing the concept's name*/
    @UiField
    protected Label conceptNameText;

    /** The icon to show the history log */
    @UiField
    protected Icon historyLogIcon;

    /** The overlay popup containing the history log */
    @UiField
    protected OverlayPopup historyLogOverlay;

    /** The panel containing the log information */
    @UiField
    protected FlowPanel historyLog;

    /** the panel containing the labels for the assessment source */
    @UiField
    protected FlowPanel assessmentSourcePanel;

    /** The label displaying the task's assessment source */
    @UiField
    protected Label conceptAssessmentSource;

    /** The icon representing this concept's assessment level */
    @UiField
    protected AssessmentLevelIcon conceptAssessmentIcon;

    /** The panel used to show and allow the user to modify the metrics of the concept's current state*/
    @UiField (provided = true)
    protected PerformanceNodeMetricsPanel metricsPanel;    
    
    /** Where assessment explanation is stored */
    @UiField
    protected HTML assessmentExplanation;
    
    /** The panel where the assessment explanation content is on */
    @UiField
    protected FlowPanel assessmentExplanationPanel;
    
    /** The panel used to display any recorded audio and allow it to be played back */
    @UiField
    protected AudioPlayer recordingPlayer;
    
    /** The header of this panel */
    @UiField
    protected Widget descriptionHeader;

    /** The concept state data currently being represented by this panel */
    private ConceptPerformanceState currentState;
    
    /** Whether the metric panel is being shown or not */
    private boolean isExpanded = false;
    
    /** The current knowledge session */
    private AbstractKnowledgeSession knowledgeSession;
    
    /**
     * Creates a new panel displaying the given state data for a concept
     *
     * @param state the state data for the concept to represent
     * @param knowledgeSession the current knowledge session. Can't be null.
     */
    public LeafConceptDataPanel(ConceptPerformanceState state, AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }
        
        this.knowledgeSession = knowledgeSession;

        metricsPanel = new PerformanceNodeMetricsPanel(this.knowledgeSession);
        initWidget(uiBinder.createAndBindUi(this));

        addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(!isExpanded) {
                    setMetricsPanelVisible(true);
                }
            }

        }, ClickEvent.getType());
        
        descriptionHeader.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(isExpanded) {
                    event.stopPropagation();
                    setMetricsPanelVisible(false);
                }
            }

        }, ClickEvent.getType());

        historyLogIcon.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                historyLogOverlay.show();
            }
        }, ClickEvent.getType());

        metricsPanel.addApplyHandler(new ApplyCallback() {
            @Override
            public void onApply(EvaluatorUpdateRequest updateRequest) {
                setMetricsPanelVisible(false);
                if (StringUtils.isNotBlank(updateRequest.getReason())) {
                    logReason(SafeHtmlUtils.fromString(updateRequest.getReason()));
                }
            }
        });

        metricsPanel.addCloseClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setMetricsPanelVisible(false);
            }
        });

        updateState(state);

        /* (STEVEN) TODO: currently we do not want to show the history log.
         * Temporarily remove the icon from the parent so that it cannot be
         * shown. */
        historyLogIcon.removeFromParent();
    }

    /**
     * Sets whether or no the metrics panel should be visible to the user
     * 
     * @param visible whether the panel should be visible
     */
    void setMetricsPanelVisible(boolean visible) {
        setMetricsPanelVisible(visible, false);
    }
    
    /**
     * Sets whether or no the metrics panel should be visible to the user
     * and, optionally, refreshes the data displayed by it
     * 
     * @param visible whether the panel should be visible
     * @param refresh whether to refresh any data that is currently
     * being displayed by the metrics panel
     */
    void setMetricsPanelVisible(boolean visible, boolean refresh) {
        
        if(visible) {
            
            if(metricsPanel.getMetricsState() == null || refresh) {

                //show the metrics panel if it is not already showing
                metricsPanel.setMetricsState(getCurrentState());

                /* Mark the conceptDescriptionPanel as being expanded */
                conceptDescriptionPanel.addStyleName(style.expanded());
                
                isExpanded = true;
            }
            
        } else {
            
            metricsPanel.setMetricsState(null);
            
            conceptDescriptionPanel.removeStyleName(style.expanded());
    
            isExpanded = false;
        }
        
        redrawDetails();
    }
    
    /**
     * Redraw this panel using the last state received.
     * 
     * @return an enumerated type of sound that needs to be played for this
     *         concept and any descendant concepts where
     *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent.
     *         Can return null;
     * 
     * @param allowAlertSound flag determining whether the alert sounds are
     *        allowed. Even if this is true, other criteria might prevent the
     *        sound from being played.
     */
    public AssessmentSoundType redraw(boolean allowAlertSound){
                
        if(currentState == null){
            return null;
        }
        
        AssessmentSoundType requestedSoundType = null;
        
        final PerformanceStateAttribute stateAttr = currentState.getState();
        conceptNameText.setText(stateAttr.getName());
        
        redrawDetails();

        metricsPanel.setDefaultTeamOrgEntries(stateAttr.getAssessedTeamOrgEntities().keySet());

        if (currentState.isContainsObservedAssessmentCondition()
                && AssessmentLevelEnum.UNKNOWN.equals(stateAttr.getShortTerm())) {
            /* If this concept requires an observed assessment and its current
             * assessment level is unknown, change its styling so the user's
             * attention is drawn toward it. */
            
            if(Dashboard.getInstance().getSettings().isHideOCAssessmentVisual()){
                conceptDescriptionPanel.removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
            }else{
                conceptDescriptionPanel.addStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
            }

        } else {
            /* Otherwise, use the default styling for concepts */
            conceptDescriptionPanel.removeStyleName(OBSERVED_ASSESSMENT_STYLE_NAME);
        }
        
        /*- Determine if an alert sound should be played by meeting ALL these criteria:
         * 1. Alerts are allowed for this redraw method call
         * 2. The concept is active 
         * 3. The assessment didn't come from this client's user during ACTIVE session
         *    (i.e. don't play sound right after user provides an assessment)
         * 4. It is not a support node or support nodes are visible
         */
        if (allowAlertSound) {
            final boolean isThisUsersEvaluation = StringUtils.equalsIgnoreCase(stateAttr.getEvaluator(),
                    UiManager.getInstance().getUserName());
            final boolean otherClientOrPlaybackMode = !isThisUsersEvaluation || PermissionsProvider.getInstance()
                    .getCurrentMode().equals(PermissionsProvider.Mode.PAST_SESSION_PLAYBACK);
            final boolean isConceptActive = stateAttr.getNodeStateEnum().equals(PerformanceNodeStateEnum.ACTIVE);
            final boolean passesSupportNodeFilter = !stateAttr.isScenarioSupportNode()
                    || Dashboard.getInstance().getSettings().isShowScenarioSupport();
            final boolean isAutoAssessment = !currentState.isContainsObservedAssessmentCondition();
            final boolean isGoodAutoAssessment = isAutoAssessment && 
                    !stateAttr.getShortTerm().isPoorPerforming();
            final boolean passesAutoAssessFilter = !isAutoAssessment || !(Dashboard.getInstance().getSettings().isHideGoodAutoAssessments() &&
                    isGoodAutoAssessment);

            allowAlertSound &= isConceptActive && otherClientOrPlaybackMode && passesSupportNodeFilter && passesAutoAssessFilter;
        }

        /* If it hasn't been activated yet, ignore it's current state value */
        final boolean ignoreState = PerformanceNodeStateEnum.UNACTIVATED.equals(stateAttr.getNodeStateEnum());

        if(!ignoreState && stateAttr.getShortTerm().equals(AssessmentLevelEnum.BELOW_EXPECTATION)){
            // if the concept is below expectation change its styling so the user's attention is drawn toward it
            // Note: this takes higher precedence than the OBSERVED_ASSESSMENT_STYLE_NAME applied above.
            
            if(Dashboard.getInstance().getSettings().isHidePoorAssessmentVisual()){
                conceptDescriptionPanel.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }else{
                conceptDescriptionPanel.addStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }
            
            /* For below expectation, verify the poor assessment sound is not
             * muted */ 
            if (allowAlertSound && !Dashboard.VolumeSettings.POOR_ASSESSMENT_SOUND.getSetting().isMuted()) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("requesting play beep for "+conceptNameText.getText()+" concept poor perf");
                }

                requestedSoundType = AssessmentSoundType.POOR_ASSESSMENT;
            }
        } else {
            // use the default styling for the concepts
            conceptDescriptionPanel.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);

            /* Only play the good assessment sound if At or Above expectation
             * and verify the good assessment sound is not muted */
            if (allowAlertSound && !Dashboard.VolumeSettings.GOOD_ASSESSMENT_SOUND.getSetting().isMuted()
                    && (stateAttr.getShortTerm().equals(AssessmentLevelEnum.AT_EXPECTATION)
                            || stateAttr.getShortTerm().equals(AssessmentLevelEnum.ABOVE_EXPECTATION))) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("requesting play beep for " + stateAttr.getName() + " concept good perf");
                }

                requestedSoundType = AssessmentSoundType.GOOD_ASSESSMENT;
            }
        }

        return requestedSoundType;
    }

    /**
     * Updates this panel's labels to reflect the attribute data provided by the given concept state
     *
     * @param state the concept state from which to derive attribute data
     * @return an enumerated type of sound that needs to be played for this concept and any descendant
     * concepts where {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
     * return null;
     */
    public AssessmentSoundType updateState(ConceptPerformanceState state) {

        final ConceptPerformanceState oldState = getCurrentState();
        boolean firstState = oldState == null;
        boolean newState = oldState == null || oldState.getState().getShortTermTimestamp() != state.getState().getShortTermTimestamp();
        this.currentState = state;

        /* Update the assessment source if a new evaluator is provided or if the
         * short term assessment has changed */
        final boolean assessmentChanged = oldState == null || oldState.getState() == null
                || oldState.getState().getShortTerm() == null
                || !oldState.getState().getShortTerm().equals(state.getState().getShortTerm());

        refreshTimerWidgets();

        // allow alerts such as poor performance sounds to be played
        //... unless this is the first time the concept state was provided or the same concept that has already been provided
        //    basically don't want to keep replaying alerts when the user is just opening the assessment panel where this concept is drawn
        final boolean allowAlertSound = !firstState && newState && assessmentChanged;
        return redraw(allowAlertSound);
    }

    /**
     * Gets the task state data that this panel is currently displaying
     *
     * @return the current task state
     */
    public ConceptPerformanceState getCurrentState() {
        return currentState;
    }

    /**
     * Refreshes any widget that has a timestamp or something else that needs frequent updating.
     */
    public void refreshTimerWidgets() {
        
        if(isExpanded){
            // update the assessment time label if currently displayed
            metricsPanel.refreshTimerWidgets();
        }
    }

    /**
     * Adds the reason log message to the log panel.
     * 
     * @param logMessage the reason message to add.
     */
    public void logReason(SafeHtml logMessage) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendEscaped(TaskDataPanel.getFormattedTimestamp(System.currentTimeMillis())).appendEscaped(" | ")
                .append(logMessage);
        final BubbleLabel bubbleLabel = new BubbleLabel(sb.toSafeHtml());
        bubbleLabel.getElement().getStyle().setProperty("margin", "0px 0px 4px 0px");
        bubbleLabel.getElement().getStyle().setBackgroundColor("#f6fafd");
        historyLog.add(bubbleLabel);
        historyLog.getElement().setScrollTop(historyLog.getElement().getScrollHeight());
        historyLogIcon.setVisible(true);
    }

    /**
     * Clears the log.
     */
    public void clearLog() {
        historyLog.clear();
    }
    
    /**
     * Redraws the detailed information outside of the metrics panel. This information is will be hidden
     * if this concept has not been expanded by the user.
     */
    private void redrawDetails() {
        
        final PerformanceStateAttribute stateAttr = currentState.getState();
        
        final String evaluator = stateAttr.getEvaluator();
        conceptAssessmentSource.setText(evaluator);
        
        assessmentSourcePanel.setVisible(isExpanded && StringUtils.isNotBlank(evaluator));

        /* If it hasn't been activated yet, ignore it's current state value */
        final boolean ignoreState = PerformanceNodeStateEnum.UNACTIVATED.equals(stateAttr.getNodeStateEnum());

        conceptAssessmentIcon.setAssessmentLevel((isExpanded && !ignoreState) ? stateAttr.getShortTerm() : AssessmentLevelEnum.UNKNOWN);
        
        boolean showExplanationPanel = false;
        
        if(currentState.getState().getAssessmentExplanation() != null){
            
            showExplanationPanel = true;
            String value = StringUtils.join("<br/>", currentState.getState().getAssessmentExplanation());
            assessmentExplanation.setHTML(value);
            
        } else {
            assessmentExplanation.setText(null);
        }
        
        if(currentState.getState().getObserverMedia() != null){
            showExplanationPanel = true;
            recordingPlayer.setUrl(currentState.getState().getObserverMedia());
            
        } else {
            recordingPlayer.setUrl(null);
        }
        
        assessmentExplanationPanel.setVisible(isExpanded && showExplanationPanel);
    }
}
