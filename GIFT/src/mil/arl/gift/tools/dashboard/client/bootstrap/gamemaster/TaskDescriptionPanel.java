/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
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
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.PerformanceNodeMetricsPanel.ApplyCallback;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;

/**
 * A that displays {@link TaskPerformanceState a task's performance state data} and updates it visually in real-time
 * as new data is provided. This widget only focuses on the data stored by the task performance state itself and is 
 * not concerned with the states of subconcepts beneath the task.
 *
 * @author nroberts
 */
public class TaskDescriptionPanel extends Composite {
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static TaskDescriptionPanelUiBinder uiBinder = GWT.create(TaskDescriptionPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface TaskDescriptionPanelUiBinder extends UiBinder<Widget, TaskDescriptionPanel> {
    }

    /**
     * Interface for the style names used in the ui.xml template of the
     * {@link TaskDataPanel}.
     *
     * @author tflowers
     *
     */
    interface Style extends CssResource {

        /**
         * Gets the name of the style applied to the
         * {@link TaskDataPanel#taskDescriptionPanel} when the
         * {@link TaskDataPanel#metricsPanel} is expanded.
         *
         * @return The name of the style.
         */
        String expanded();
    }

    /** The instance of the {@link Style} interface. */
    @UiField
    Style style;
    
    /** the style name that should be applied to tasks that are below expectation */
    private static final String BELOW_EXPECTATION_STYLE_NAME = "nodeBelowExpectationAssessment";

    /**
     * Date format used to show the full date (i.e. [HOUR]:[MINUTE]:[SECOND]
     * [AM/PM] [DAY-OF-WEEK], [MONTH] [DAY], [YEAR])
     */
    private static DateTimeFormat FULL_DATE_FORMAT = DateTimeFormat.getFormat("h:mm:ss a EEEE, MMMM d, yyyy");

    /**
     * Date format used to show only the day, month, and year (i.e.
     * [DAY]-[MONTH]-[YEAR]). Used to determine if two timestamps fall on the
     * exact same day.
     */
    private static DateTimeFormat DAY_MONTH_YEAR_FORMAT = DateTimeFormat.getFormat("dd-MM-yyyy");

    /**
     * Date format used to show only the time of day (i.e.
     * [HOUR]:[MINUTE]:[SECOND] [AM/PM])
     */
    private static DateTimeFormat TIME_OF_DAY_FORMAT = DateTimeFormat.getFormat("h:mm:ss a");
    
    /**
     * Number format used to show the task stress/difficulty in 1 decimal precision
     */
    private static NumberFormat STRESS_DIFFICULTY_FORMAT = NumberFormat.getFormat("0.0");

    /** The label displaying the task's name */
    @UiField
    protected Label taskName;

    /** The tooltip attached to {@link #statusControlIcon} */
    @UiField
    protected ManagedTooltip statusControlImageTooltip;
    
    /** the play/pause icon for tasks */
    @UiField
    protected Icon statusControlIcon;

    /** The icon to show the history log */
    @UiField
    protected Icon historyLogIcon;

    /** The overlay popup containing the history log */
    @UiField
    protected OverlayPopup historyLogOverlay;

    /** The panel containing the log information */
    @UiField
    protected FlowPanel historyLog;

    /** The label displaying the task's assessment source */
    @UiField
    protected Label taskAssessmentSource;

    /** The label showing whether or not this task is active */
    @UiField
    protected Label taskActive;
    
    /** the panel that contains task difficulty widgets */
    @UiField
    protected FlowPanel difficultyPanel;
    
    /** the label with the task difficulty value */
    @UiField
    protected Label taskDifficulty;
    
    /** the panel that contains task stress widgets */
    @UiField
    protected FlowPanel stressPanel;
    
    /** the label with the task stress value */
    @UiField
    protected Label taskStress;

    /** The icon representing this task's assessment level */
    @UiField
    protected AssessmentLevelIcon taskAssessmentIcon;

    /** The panel containing the description of the task's current state */
    @UiField
    protected Widget taskDescriptionPanel;

    /** The panel used to show and allow the user to modify the metrics of the task's current state*/
    @UiField (provided = true)
    protected PerformanceNodeMetricsPanel metricsPanel;
    
    /** where assessment explanation is stored */
    @UiField
    protected HTML assessmentExplanation;
    
    /** the panel where the assessment explanation content is on */
    @UiField
    protected FlowPanel assessmentExplanationPanel;
    
    /** The panel used to display any recorded audio and allow it to be played back */
    @UiField
    protected AudioPlayer recordingPlayer;

    /** the panel containing the labels for the assessment source */
    @UiField
    protected FlowPanel assessmentSourcePanel;
    
    /** The panel containing the label showing whether or not this task is active*/
    @UiField
    protected Widget activePanel;
    
    /** The header of this panel */
    @UiField
    protected Widget taskDescriptionHeader;
    
    /** The panel containing the label showing which team roles are targeted by the current assessment */
    @UiField
    protected Widget assessmentTargetPanel;
    
    /** The icon next to the assessment target label */
    @UiField
    protected Icon assessmentTargetIcon;
    
    /** The label showing which team roles are targeted by the current assessment */
    @UiField
    protected Label assessmentTargetLabel;
    
    /** A badge next to the assessment target label indicating how many roles are targeted by the current assessment */
    @UiField
    protected Label assessmentTargetBadgeLabel;

    /** whether the metric panel is being shown or not */
    private boolean isExpanded = false;
    
    /** The popup used to confirm the choice to deactivate the task */
    private ModalDialogBox activeStatusConfirmation = null;

    /** The provider of task assessment information used to populate this widget */
    private TaskDataProvider dataProvider;
    
    /** Whether this panel should display as little data as possible when collapsed */
    private boolean isMinimized;
    
    /**
     * Creates a new panel displaying the given state data for a task
     *
     * @param dataProvider the data provider that the task's state data should be obtained from. Cannot be null.
     */
    public TaskDescriptionPanel(final TaskDataProvider dataProvider) {
        
        if(dataProvider == null) {
            throw new IllegalArgumentException("The data provider cannot be null");
        }
        
        this.dataProvider = dataProvider;

        metricsPanel = new PerformanceNodeMetricsPanel(dataProvider.getKnowledgeSession());
        initWidget(uiBinder.createAndBindUi(this));

        taskDescriptionPanel.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(!isExpanded) {
                    setMetricsPanelVisible(true);
                }
            }

        }, ClickEvent.getType());
        
        taskDescriptionHeader.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(isExpanded) {
                    event.stopPropagation();
                    setMetricsPanelVisible(false);
                }
            }

        }, ClickEvent.getType());
        
        statusControlIcon.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                
                if(activeStatusConfirmation == null) {
                
                    //display a prompt asking the user if they want to start/stop this task
                    activeStatusConfirmation = new ModalDialogBox();
                    activeStatusConfirmation.setGlassEnabled(true);
                    
                    Button yesButton = new Button("Yes");
                    yesButton.setType(ButtonType.PRIMARY);
                    yesButton.addClickHandler(new ClickHandler() {
                        
                        @Override
                        public void onClick(ClickEvent event) {
                            
                            event.stopPropagation();
    
                            PerformanceNodeStateEnum nodeStateEnum = dataProvider.getCurrentState().getState().getNodeStateEnum();
                            final PerformanceNodeStateEnum newValue = nodeStateEnum == PerformanceNodeStateEnum.ACTIVE
                                    ? PerformanceNodeStateEnum.FINISHED
                                    : PerformanceNodeStateEnum.ACTIVE;
    
                            String nodeName = dataProvider.getCurrentState().getState().getName();
                            EvaluatorUpdateRequest updateRequest = new EvaluatorUpdateRequest(nodeName, BsGameMasterPanel.getGameMasterUserName(), System.currentTimeMillis());
                            updateRequest.setState(newValue);
                            BrowserSession.getInstance().sendWebSocketMessage(new DashboardMessage(updateRequest, dataProvider.getKnowledgeSession()));
    
                            activeStatusConfirmation.hide();
                        }
                    });
                    activeStatusConfirmation.setFooterWidget(yesButton);
                    
                    activeStatusConfirmation.setCloseable(true);
                    activeStatusConfirmation.getCloseButton().setText("No");
                    activeStatusConfirmation.getCloseButton().setType(ButtonType.DANGER);
                }
                
                PerformanceNodeStateEnum nodeStateEnum = dataProvider.getCurrentState().getState().getNodeStateEnum();
                final PerformanceNodeStateEnum newValue = nodeStateEnum == PerformanceNodeStateEnum.ACTIVE
                        ? PerformanceNodeStateEnum.FINISHED
                        : PerformanceNodeStateEnum.ACTIVE;

                String promptVerb = newValue == PerformanceNodeStateEnum.ACTIVE ? "start" : "stop";
                String promptText = "Are you sure you want to " + promptVerb + " assessing <b>"
                        + dataProvider.getCurrentState().getState().getName() + "</b>?";

                activeStatusConfirmation.setText((newValue == PerformanceNodeStateEnum.ACTIVE ? "Activate" : "Deactivate") + " Task?");
                activeStatusConfirmation.setWidget(new HTML(promptText));
                
                activeStatusConfirmation.center();
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

        redraw(true);

        /* (STEVEN) TODO: currently we do not want to show the history log.
         * Temporarily remove the icon from the parent so that it cannot be
         * shown. */
        historyLogIcon.removeFromParent();
    }

    /**
     * Update the visibility of the status control icon
     * 
     * @param whether the status control icon should be visible
     */
    public void updateStatusControlIconVisibility(boolean visible) {
        statusControlIcon.setVisible(visible);
    }
    
    /**
     * Redraw this panel using the last state received.
     * 
     * @param assessmentChanged whether the assessment state has just changed
     */
    public void redraw(boolean assessmentChanged){
                
        if(dataProvider.getCurrentState() == null){
            return;
        }
        
        final PerformanceStateAttribute stateAttr = dataProvider.getCurrentState().getState();
        
        redrawDetails();
        
        taskName.setText(stateAttr.getName());
        refreshTimerWidgets();

        /* If it hasn't been activated yet, ignore it's current state
         * value */
        final boolean ignoreState = PerformanceNodeStateEnum.UNACTIVATED
                .equals(stateAttr.getNodeStateEnum());

        if(!ignoreState && stateAttr.getShortTerm().equals(AssessmentLevelEnum.BELOW_EXPECTATION)){            
            // if the task is below expectation change its styling so the user's attention is drawn toward it
            
            if(Dashboard.getInstance().getSettings().isHidePoorAssessmentVisual()){
                taskDescriptionPanel.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }else{
                taskDescriptionPanel.addStyleName(BELOW_EXPECTATION_STYLE_NAME);
            }
        } else {
            // use the default styling for the concepts
            taskDescriptionPanel.removeStyleName(BELOW_EXPECTATION_STYLE_NAME);
        }
        
        boolean isActive = PerformanceNodeStateEnum.ACTIVE.equals(stateAttr.getNodeStateEnum());
        statusControlIcon.setType(isActive ? IconType.PAUSE_CIRCLE_O : IconType.PLAY_CIRCLE_O);
        
        statusControlImageTooltip.setTitle("Click to " + (isActive ? "de" : "") + "activate the task");
        
        redrawAssessmentTargets(stateAttr.getAssessedTeamOrgEntities());
    }

    /**
     * Redraws the area on this panel that indicates what users/team roles were targeted by the
     * last assessment state received
     * 
     * @param assessedLearners the learner that were assessed. If null, the area will be hidden.
     */
    private void redrawAssessmentTargets(Map<String, AssessmentLevelEnum> assessedLearners) {
        
        if(assessedLearners == null) {
            assessmentTargetPanel.setVisible(false);
            return;
        }
        
        //determine which teams/team members were assessed
        List<AbstractTeamUnit> assessedUnits = getTopMostTeamUnits(
                new ArrayList<String>(assessedLearners.keySet()), 
                dataProvider.getKnowledgeSession().getTeamStructure());
        
        //calculate the total number of assessed units
        int numUnits = assessedUnits.size();
        assessmentTargetPanel.setVisible(numUnits != 0);
        assessmentTargetBadgeLabel.setText(Integer.toString(assessedLearners.size()));
        
        if(numUnits == 1) {
            
            //if only one unit was assessed, simply display its name
            AbstractTeamUnit unit = assessedUnits.get(0);
            assessmentTargetIcon.setType(unit instanceof Team ? IconType.USERS : IconType.USER);
            assessmentTargetLabel.setText(unit.getName());
            
        } else {
            
            //if multiple units were assessed, iterate over them and display a combination of their names
            assessmentTargetIcon.setType(IconType.USERS);
            
            List<AbstractTeamUnit> incompleteTeams = new ArrayList<>();
            int displayedUnits = 0;
            StringBuilder sb = new StringBuilder();
            
            for(int i = 0; i < numUnits; i++) {
                
                AbstractTeamUnit unit = assessedUnits.get(i);
                boolean isMember = unit instanceof TeamMember;
                boolean alreadyFoundTeam = false;
                
                //attempt to display this unit's topmost parent
                while(unit.getParentTeam() != null && unit.getParentTeam().getParentTeam() != null) {
                    unit = unit.getParentTeam();
                    
                    if(!incompleteTeams.contains(unit)) {
                        if(isMember) {
                            
                            /* If we encounter a member rather than a team, then we know that not all of
                             * that team's members were assessed, so the team is incomplete*/
                            incompleteTeams.add(unit);
                        }
                        
                    } else {
                        
                        //don't want to display parent teams that were already found
                        alreadyFoundTeam = true;
                        continue;
                    }
                }
                
                if(alreadyFoundTeam) {
                    //don't want to display parent teams that were already found
                    continue;
                }
                
                displayedUnits++;
                
                if(displayedUnits > 1) {
                    sb.append(", ");    
                }
                
                if(displayedUnits > 2) {
                    sb.append("...");
                    break;
                }
                
                sb.append(unit.getName());
                
                //show a - sign next to the names of teams that were not completely assessed
                if(incompleteTeams.contains(unit)) {
                    sb.append("-");
                }
            }
            
            assessmentTargetLabel.setText(sb.toString());
        }
    }

    /**
     * Updates the task active label with the latest timestamp. Does nothing if the
     * {@link #currentState} or {@link #cachedState} is null.
     */
    public void updateTaskActiveLabel() {
        if (dataProvider.getCurrentState() == null || dataProvider.getCachedState() == null) {
            return;
        }

        Long timeDiff = dataProvider.getCachedState().getCumulativeActiveTime();

        if (PerformanceNodeStateEnum.ACTIVE.equals(dataProvider.getCurrentState().getState().getNodeStateEnum())) {
            /* If this task is currently active, need to calculate time since it
             * last became active */
            Long currentTime = dataProvider.getKnowledgeSession().inPastSessionMode()
                    ? TimelineProvider.getInstance().getPlaybackTime()
                    : System.currentTimeMillis();
            timeDiff = timeDiff + currentTime - dataProvider.getCachedState().getLastActiveTimestamp();
        }

        int seconds = ((Long) (timeDiff / 1000)).intValue();
        String timeDisplay = FormattedTimeBox.getDisplayText(seconds, true);

        taskActive.setText(timeDisplay);
    }
    
    /**
     * Update the task difficulty value label with the current task performance state value.
     * Will also hide the difficulty panel if the task difficulty value is null.
     */
    public void updateTaskDifficultyLabel() {
        if (dataProvider.getCurrentState() == null) {
            // hide difficulty panel
            difficultyPanel.setVisible(false);
            return;
        }
        
        TaskPerformanceState tState = dataProvider.getCurrentState();
        Double difficulty = tState.getDifficulty();
        if(difficulty == null) {
            // hide difficulty panel
            difficultyPanel.setVisible(false);
            return;
        }
        
        if(!difficultyPanel.isVisible()) {
            // show difficulty panel
            difficultyPanel.setVisible(true);
        }
        
        taskDifficulty.setText(tState.getDifficultyAsString() + " ("+STRESS_DIFFICULTY_FORMAT.format(tState.getDifficulty())+")");        
    }
    
    /**
     * Update the task stress value label with the current task performance state value.
     * Will also hide the stress panel if the task stress value is null.
     */
    public void updateTaskStressLabel() {        
        if (dataProvider.getCurrentState() == null) {
            //  hide stress panel
            stressPanel.setVisible(false);
            return;
        }
        
        TaskPerformanceState tState = dataProvider.getCurrentState();
        Double stress = tState.getStress();
        if(stress == null) {
            // hide stress panel
            stressPanel.setVisible(false);
            return;
        }
        
        if(!stressPanel.isVisible()) {
            // show stress panel
            stressPanel.setVisible(true);
        }
        
        taskStress.setText(STRESS_DIFFICULTY_FORMAT.format(stress));
    }

    /**
     * Gets a formatted string indicating the time of day (and, if necessary,
     * the date) that the given timestamp falls on. Note that the date of the
     * timestamp will only be displayed if it occurs on a different day than
     * when this method is invoked.
     *
     * @param timestampMillis the timestamp from which to derive the time of day
     *        and date
     * @return a formatted string indicating the time of day. May include the
     *         date if the timestamp occurred on another day.
     */
    static String getFormattedTimestamp(long timestampMillis) {

        Date currentTime = new Date(System.currentTimeMillis());
        Date timestampTime = new Date(timestampMillis);

        if (DAY_MONTH_YEAR_FORMAT.format(currentTime).equals(DAY_MONTH_YEAR_FORMAT.format(timestampTime))) {

            // only return time of day (i.e. hh:mm:ss), since the timestamp
            // falls on today's date
            return TIME_OF_DAY_FORMAT.format(timestampTime);

        } else {

            // return the full date, since the timestamp falls on a different
            // day from today's date
            return FULL_DATE_FORMAT.format(timestampTime);
        }
    }
    
    /**
     * Refreshes any widget that has a timestamp or something else that needs frequent updating.
     */
    public void refreshTimerWidgets() {
        
        if(isExpanded){
            // update the assessment time label if currently displayed
            metricsPanel.refreshTimerWidgets();
        }
        updateTaskActiveLabel();
    }

    /**
     * Adds the reason log message to the log panel.
     * 
     * @param logMessage the reason message to add.
     */
    public void logReason(SafeHtml logMessage) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendEscaped(getFormattedTimestamp(System.currentTimeMillis())).appendEscaped(" | ").append(logMessage);
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
     * Sets whether this panel should display as little data as possible when collapsed
     * 
     * @param minimized whether this panel should be minimized
     */
    public void setMinimized(boolean minimized) {
        this.isMinimized = minimized;
    }
    
    /**
     * Redraws the detailed information outside of the metrics panel. This information will be hidden
     * if this task has not been expanded by the user.
     */
    private void redrawDetails() {
        
        boolean draw = !isMinimized || isExpanded;
        
        final PerformanceStateAttribute stateAttr = dataProvider.getCurrentState().getState();
        
        final String evaluator = stateAttr.getEvaluator();
        taskAssessmentSource.setText(evaluator);
        
        assessmentSourcePanel.setVisible(draw && StringUtils.isNotBlank(evaluator));

        /* If it hasn't been activated yet, ignore it's current state value */
        final boolean ignoreState = PerformanceNodeStateEnum.UNACTIVATED.equals(stateAttr.getNodeStateEnum());

        taskAssessmentIcon.setAssessmentLevel((draw && !ignoreState) ? stateAttr.getShortTerm() : AssessmentLevelEnum.UNKNOWN);
        
        updateTaskDifficultyLabel();
        updateTaskStressLabel();
        
        boolean showExplanationPanel = false;
        
        if(stateAttr.getAssessmentExplanation() != null){
            
            showExplanationPanel = true;
            String value = StringUtils.join("<br/>", stateAttr.getAssessmentExplanation());
            assessmentExplanation.setHTML(value);
            
        } else {
            assessmentExplanation.setText(null);
        }
        
        if(stateAttr.getObserverMedia() != null){
            showExplanationPanel = true;
            recordingPlayer.setUrl(stateAttr.getObserverMedia());
            
        } else {
            recordingPlayer.setUrl(null);
        }
        
        assessmentExplanationPanel.setVisible(draw && showExplanationPanel);
        
        // 12/21 - hiding this panel due to the active time not proven useful by anyone yet.  Plus just added difficulty/stress above.  -->
//        activePanel.setVisible(draw);
    }
    
    /**
     * Sets whether or not the metrics panel should be visible to the user
     * 
     * @param visible whether the panel should be visible
     */
    void setMetricsPanelVisible(boolean visible) {
        
        if(visible) {
            
            if (metricsPanel.getMetricsState() == null) {

                // show the metrics panel if it is not already showing
                metricsPanel.setMetricsState(dataProvider.getCurrentState());

                /* Adjust the styling on the taskDescriptionPanel */
                taskDescriptionPanel.addStyleName(style.expanded());

                isExpanded = true;
            }
            
        } else {
            
            metricsPanel.setMetricsState(null);
            
            taskDescriptionPanel.removeStyleName(style.expanded());
    
            isExpanded = false;
        }
        
        redrawDetails();
    }
    
    /**
     * Searches the given team for teams and team members with the given names
     * and returns the top-most units that are found with those names. If one of the provided
     * names is used by a parent team, then the parent team's
     * children will be removed from the returned list so that only the highest
     * unit in the organization is kept. If the names of all the team members
     * within a parent team are part of the provided list, then only
     * the parent team will be returned.
     * 
     * This method essentially returns the smallest possible list of teams and
     * team members in which the given list of team and team member names
     * can be found
     * 
     * @param names the team and team member names to look for. Can be null.
     * @param team the team within which to look for names. Can be null.
     * @return the top-most teams and team members that were found. Will not be null.
     */
    public static List<AbstractTeamUnit> getTopMostTeamUnits(List<String> names, Team team) {

        List<AbstractTeamUnit> returnList = new ArrayList<>();
        if (team == null || names == null) {
            return returnList;
        }

        /* We do not want to modify the provided list of names so create a copy
         * to perform the actions on */
        List<String> namesCopy = new ArrayList<>(names);

        if (!namesCopy.isEmpty()) {
            Iterator<String> itr = namesCopy.iterator();
            while (itr.hasNext()) {

                String name = itr.next();
                if (StringUtils.equals(name, team.getName())) {

                    /* found one of the names in a team, so stop looking for it
                     * and skip the team's children */
                    returnList.add(team);
                    itr.remove();

                    return returnList;
                }
            }
        }

        /* track whether all of this team's team members match the names being
         * looked for */
        boolean allMembersFound = true;

        if (namesCopy.isEmpty()) {
            allMembersFound = false;
        } else {
            for (AbstractTeamUnit unit : team.getUnits()) {

                if (unit instanceof TeamMember) {
                    boolean nameFound = false;

                    Iterator<String> itr = namesCopy.iterator();
                    while (itr.hasNext()) {

                        String name = itr.next();
                        if (StringUtils.equals(name, unit.getName())) {
                            /* found one of the names in a team member, so stop
                             * looking for it */
                            nameFound = true;
                            returnList.add(unit);
                            itr.remove();
                            break;
                        }
                    }

                    if (!nameFound) {
                        /* this member did not have a name being looked for */
                        allMembersFound = false;
                    }

                } else if (unit instanceof Team) {

                    Team subTeam = (Team) unit;
                    List<AbstractTeamUnit> namesInSubTeam = getTopMostTeamUnits(namesCopy, subTeam);

                    if (namesInSubTeam.size() != 1 || !namesInSubTeam.get(0).equals(subTeam)) {
                        /* at least one member this a sub-team did not have a
                         * name being looked for */
                        allMembersFound = false;
                    }

                    returnList.addAll(namesInSubTeam);
                }
            }

        }

        if (!team.getUnits().isEmpty() && allMembersFound) {
            /* ALL of this team's members had names being looked for, so only
             * return this team's name as a shorthand for them */
            returnList.clear();
            returnList.add(team);
        }

        return returnList;
    }
}
