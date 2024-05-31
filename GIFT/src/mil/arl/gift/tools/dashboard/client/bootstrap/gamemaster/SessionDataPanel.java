/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.HideEvent;
import org.gwtbootstrap3.client.shared.event.HideHandler;
import org.gwtbootstrap3.client.shared.event.ShowEvent;
import org.gwtbootstrap3.client.shared.event.ShowHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.TouchSplitLayoutPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider.SessionStateUpdateHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.AssessmentDisplayMode;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.SummativeAssessmentChangeHandler;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;

/**
 * A widget contains the functionality to manipulate and monitor an individual
 * training session.
 *
 * @author nroberts
 */
public class SessionDataPanel extends Composite implements ActiveSessionChangeHandler, SessionStateUpdateHandler, SummativeAssessmentChangeHandler {

    /**
     * The CSS class used to identify the notification bubble for when a new
     * session is started for the same domain session
     */
    private static final String NEW_SESSION_NOTIFICATION_CLASS = "sessionDataPanel-newSessionNotification";

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionDataPanel.class.getName());

    /** The session details: Host username */
    private static final SafeHtml HOST_USERNAME = bold("Host: ");

    /** The session details: Course ID parameter */
    private static final SafeHtml SESSION_COURSE_ID = bold("Course ID: ");

    /** The session details: Source Course ID parameter */
    private static final SafeHtml SESSION_SOURCE_COURSE_ID = bold("Source Course ID: ");

    /** The session details: Team participants parameter */
    private static final SafeHtml SESSION_PARTICIPANTS = bold("Team Roster: ");

    /** The session details: Host session ID parameter */
    private static final SafeHtml SESSION_HOST_SESSION_ID = bold("Host session ID: ");

    /**
     * Date format used to show the full date (i.e. [HOUR]:[MINUTE] [MONTH]
     * [DAY], [YEAR])
     */
    private static DateTimeFormat FULL_DATE_FORMAT = DateTimeFormat.getFormat("HH:mm MMMM d, yyyy");

    /**
     * The session details: time info for session (start and end - if provided)
     */
    private static final SafeHtml TIME = bold("Time: ");

    /**
     * The session details: duration of the session (if end time is provided)
     */
    private static final SafeHtml DURATION = bold("Duration: ");

    /**
     * The session details: domain session log (optional - only for past session
     * playback)
     */
    private static final SafeHtml SESSION_LOG_FILE = bold("Log: ");

    /** The UiBinder that combines the ui.xml with this java class */
    private static SessionDataPanelUiBinder uiBinder = GWT.create(SessionDataPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SessionDataPanelUiBinder extends UiBinder<Widget, SessionDataPanel> {
    }

    /** The panel that shows the list of active tasks */
    @UiField
    protected FlowPanel activeTasksPanel;

    /** The panel that shows the list of upcoming tasks */
    @UiField
    protected FlowPanel upcomingTasksPanel;

    /** the panel that shows the list of completed tasks */
    @UiField
    protected FlowPanel completedTasksPanel;

    /**
     * The layout panel. The main content is in the center and the east contains
     * the {@link #strategiesPanel}.
     */
    @UiField
    protected TouchSplitLayoutPanel layoutSplitter;

    /** Toggles between the {@link #tasksPanel} and {@link #emptyTasksPanel} */
    @UiField
    protected DeckPanel tasksDeck;

    /**
     * The panel containing the tasks and concepts to be displayed to the user
     */
    @UiField
    protected Widget tasksPanel;

    /** The panel to be shown if the {@link #tasksPanel} is empty */
    @UiField
    protected Widget emptyTasksPanel;

    /** The panel that hides formative assessments when in summative assessments mode*/
    @UiField
    protected Widget formativeHidingPanel;
    
    /** A button used to change back to formative assessments*/
    @UiField
    protected Button formativeButtonChange;
    
    /** Tooltip for the button used to show/hide active tasks */
    @UiField
    protected Tooltip activeTasksTooltip;

    /** Button used to show/hide active tasks */
    @UiField
    protected Button activeTasksButton;

    /**
     * popup to show attached to the active tasks button when landing on this
     * panel
     */
    @UiField
    protected Popover activeTasksButtonPopup;

    /** Collapse panel used to hide active tasks */
    @UiField
    protected Collapse activeTasksCollapse;

    /** contains a placeholder text for the active tasks panel */
    @UiField
    protected HTMLPanel activeTasksPlaceholder;

    /** Tooltip for the button used to show/hide upcoming tasks */
    @UiField
    protected Tooltip upcomingTasksTooltip;

    /** Button used to show/hide upcoming tasks */
    @UiField
    protected Button upcomingTasksButton;

    /**
     * popup to show attached to the upcoming tasks button when landing on this
     * panel
     */
    @UiField
    protected Popover upcomingTasksButtonPopup;

    /** Collapse panel used to hide upcoming tasks */
    @UiField
    protected Collapse upcomingTasksCollapse;

    /** contains a placeholder text for the upcoming tasks panel */
    @UiField
    protected HTMLPanel upcomingTasksPlaceholder;

    /** Tooltip for the button used to show/hide completed tasks */
    @UiField
    protected Tooltip completedTasksTooltip;

    /** Button used to show/hide completed tasks */
    @UiField
    protected Button completedTasksButton;

    /**
     * popup to show attached to the completed tasks button when landing on this
     * panel
     */
    @UiField
    protected Popover completedTasksButtonPopup;

    /** Collapse panel used to hide completed tasks */
    @UiField
    protected Collapse completedTasksCollapse;

    /** contains a placeholder text for the completed tasks panel */
    @UiField
    protected HTMLPanel completedTasksPlaceholder;

    /** The active session provider instance */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /** The knowledge session for this data panel */
    private final AbstractKnowledgeSession knowledgeSession;

    /**
     * A mapping from known task performance node IDs to the data panels used to
     * display their data
     */
    private HashMap<Integer, TaskDataPanel> nodeIdToTaskPanel = new HashMap<>();

    /** Timer to refresh certain display widgets after a set period of time */
    private final Timer refreshTimer = new Timer() {
        @Override
        public void run() {

            /* Notify task data panels to refresh timer widgets */
            for (TaskDataPanel taskPanel : nodeIdToTaskPanel.values()) {
                taskPanel.refreshTimerWidgets();
            }
        }
    };

    /** 30 second delay for the {@link #refreshTimer} */
    private final static int TIMER_DELAY = 30000;

    /** flag used to track whether the upcoming tasks button popup is exposed */
    private boolean popupVisible = false;

    /** The header data for the session */
    private final SessionDataHeader sessionDataHeader;

    /** The tasks that are currently selected for closer inspection, if any */
    private Set<Integer> selectedTasks = new HashSet<>();

    /** Whether this panel is currently set to show the task groupings */
    private boolean isShowingTaskGroupings = false;

    /**
     * The latest knowledge session state that corresponds to the session data
     * currently being displayed
     */
    private KnowledgeSessionState currentState = null;

    /* The knowledge session selector */
    private KnowledgeSessionSelector sessionSelector = null;

    /**
     * The performance node that should be scrolled into view once the
     * performance nodes have been redrawn and are visually ready to be scrolled
     */
    private PerformanceNodePath perfNodeToScroll;

    /** The request to render the UI that is currently being handled */
    private ScheduledCommand currentDisplayRequest;

    /* The message notifying of an active session. */
    private Notify activeSessionNotification;

    public interface KnowledgeSessionSelector {

        /**
         * Handles the response to when a session is selected.
         * 
         * @param session the knowledge session being registered.
         */
        public void onSelectSession(AbstractKnowledgeSession session);
    }

    /**
     * Sets the session selector.
     *
     * @param sessionSelector the session selector object.
     */
    public void setSessionSelector(KnowledgeSessionSelector sessionSelector) {
        this.sessionSelector = sessionSelector;

    }

    /**
     * Creates a new widget that can manipulate and monitor an individual
     * training session
     *
     * @param knowledgeSession the knowledge session used to populate this data
     *        panel.
     */
    public SessionDataPanel(final AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SessionDataPanel(" + knowledgeSession.getNameOfSession() + ")");
        }

        this.knowledgeSession = knowledgeSession;
        sessionDataHeader = new SessionDataHeader(knowledgeSession);

        initWidget(uiBinder.createAndBindUi(this));
        init();

        tasksDeck.showWidget(tasksDeck.getWidgetIndex(emptyTasksPanel));

        activeTasksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if (IconType.CARET_DOWN.equals(activeTasksButton.getIcon())) {
                    activeTasksCollapse.hide();
                    activeTasksButton.setIcon(IconType.CARET_RIGHT);
                    activeTasksTooltip.setTitle("Click to show active tasks");

                } else {
                    activeTasksCollapse.show();
                    activeTasksButton.setIcon(IconType.CARET_DOWN);
                    activeTasksTooltip.setTitle("Click to hide active tasks");
                }
            }
        });

        activeTasksButtonPopup.addShowHandler(new ShowHandler() {

            @Override
            public void onShow(ShowEvent event) {
                popupVisible = true;
            }
        });

        activeTasksButtonPopup.addHideHandler(new HideHandler() {

            @Override
            public void onHide(HideEvent event) {
                popupVisible = false;
            }
        });

        upcomingTasksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if (IconType.CARET_DOWN.equals(upcomingTasksButton.getIcon())) {
                    upcomingTasksCollapse.hide();
                    upcomingTasksButton.setIcon(IconType.CARET_RIGHT);
                    upcomingTasksTooltip.setTitle("Click to show upcoming tasks");

                } else {
                    upcomingTasksCollapse.show();
                    upcomingTasksButton.setIcon(IconType.CARET_DOWN);
                    upcomingTasksTooltip.setTitle("Click to hide upcoming tasks");
                }
            }
        });

        upcomingTasksButtonPopup.addShowHandler(new ShowHandler() {

            @Override
            public void onShow(ShowEvent event) {
                popupVisible = true;
            }
        });

        upcomingTasksButtonPopup.addHideHandler(new HideHandler() {

            @Override
            public void onHide(HideEvent event) {
                popupVisible = false;
            }
        });

        completedTasksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if (IconType.CARET_DOWN.equals(completedTasksButton.getIcon())) {
                    completedTasksCollapse.hide();
                    completedTasksButton.setIcon(IconType.CARET_RIGHT);
                    completedTasksTooltip.setTitle("Click to show completed tasks");

                } else {
                    completedTasksCollapse.show();
                    completedTasksButton.setIcon(IconType.CARET_DOWN);
                    completedTasksTooltip.setTitle("Click to hide completed tasks");
                }
            }
        });

        completedTasksButtonPopup.addShowHandler(new ShowHandler() {

            @Override
            public void onShow(ShowEvent event) {
                popupVisible = true;
            }
        });

        completedTasksButtonPopup.addHideHandler(new HideHandler() {

            @Override
            public void onHide(HideEvent event) {
                popupVisible = false;
            }
        });
        
        formativeButtonChange.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SummativeAssessmentProvider.getInstance().setDisplayMode(AssessmentDisplayMode.FORMATIVE);
            }
        });
        
        //if tasks were selected before this widget was ready, get them
        setSelectedTasks(SessionStateProvider.getInstance().getShownTaskIds());

        /* Subscribe to the data providers */
        subscribe();
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        logger.info("subscribe to providers");
        /* Subscribe to the active session changes */
        activeSessionProvider.addHandler(this);

        /* Subscribe to the knowledge session state updates */
        SessionStateProvider.getInstance().addHandler(this);
        
        /* Subscriber to summative assessments provider*/
        SummativeAssessmentProvider.getInstance().addHandler(this);
    }

    /**
     * Unsubscribe this {@link SessionDataPanel} from all providers. This should
     * only be done before the panel is destroyed.
     */
    public void unsubscribe() {
        /* Remove handlers */
        activeSessionProvider.removeHandler(this);
        SessionStateProvider.getInstance().removeHandler(this);

        if (activeSessionNotification != null) {
            activeSessionNotification.hide();
        }
        SummativeAssessmentProvider.getInstance().removeHandler(this);
    }

    /**
     * Initialize this panel with the {@link AbstractKnowledgeSession}
     */
    private void init() {

        SafeHtmlBuilder sb = new SafeHtmlBuilder();

        sb.append(HOST_USERNAME)
                .appendHtmlConstant(knowledgeSession.getHostSessionMember().getSessionMembership().getUsername());
        sb.appendHtmlConstant("<br/>");
        if (knowledgeSession.getSessionStartTime() > 0) {
            Date start = new Date(knowledgeSession.getSessionStartTime());
            sb.append(TIME).appendHtmlConstant(FULL_DATE_FORMAT.format(start));
            if (knowledgeSession.getSessionEndTime() > knowledgeSession.getSessionStartTime()) {

                // concatenate end time
                sb.appendHtmlConstant(Constants.SPACE).appendHtmlConstant("-").appendHtmlConstant(Constants.SPACE)
                        .appendHtmlConstant(FULL_DATE_FORMAT.format(new Date(knowledgeSession.getSessionEndTime())));

                // concatenate duration
                String duration = FormattedTimeBox.getDisplayText(
                        (int) (knowledgeSession.getSessionEndTime() - knowledgeSession.getSessionStartTime()) / 1000,
                        true);
                sb.appendHtmlConstant(Constants.SPACE).appendHtmlConstant(Constants.SPACE).append(DURATION)
                        .appendHtmlConstant(duration);
            }
            sb.appendHtmlConstant("<br/>");
        }

        sb.append(SESSION_COURSE_ID).appendHtmlConstant(knowledgeSession.getCourseRuntimeId());
        sb.appendHtmlConstant("<br/>");
        sb.append(SESSION_SOURCE_COURSE_ID).appendHtmlConstant(knowledgeSession.getCourseSourceId());
        sb.appendHtmlConstant("<br/>");
        if (knowledgeSession instanceof TeamKnowledgeSession) {
            TeamKnowledgeSession teamSession = (TeamKnowledgeSession) knowledgeSession;
            // Note: add 1 to include the host in the count since the host is
            // not currently in the list of joined members
            sb.append(SESSION_PARTICIPANTS)
                    .appendHtmlConstant(Integer.toString(teamSession.getJoinedMembers().size() + 1))
                    .appendHtmlConstant(" out of ")
                    .appendHtmlConstant(Integer.toString(teamSession.getTotalPossibleTeamMembers()));
            sb.appendHtmlConstant("<br/>");
        }
        sb.append(SESSION_HOST_SESSION_ID)
                .appendHtmlConstant(Integer.toString(knowledgeSession.getHostSessionMember().getDomainSessionId()));

        LogMetadata logMetadata = RegisteredSessionProvider.getInstance().getLogMetadata();
        if (logMetadata != null) {
            sb.appendHtmlConstant("<br/>").append(SESSION_LOG_FILE).appendHtmlConstant(logMetadata.getLogFile());
        }

        /* Restart timer */
        refreshTimer.scheduleRepeating(TIMER_DELAY);
    }

    /**
     * Apply the filter to the task data panels on the page.
     */
    public void applyFilter() {
        boolean showingTask = false, hasCompletedTask = false, hasActiveTask = false, hasUpcomingTask = false;
        for (TaskDataPanel taskDataPanel : nodeIdToTaskPanel.values()) {

            // this task should be shown, now check the descendant concepts
            boolean childConceptShown = taskDataPanel.applyConceptFilter();

            /* If no concepts are visible for a task, hide the task */
            taskDataPanel.setVisible(childConceptShown);

            showingTask |= taskDataPanel.isVisible();

            if (taskDataPanel.isVisible()) {
                // since this task panel is visible, check to see which panel it
                // will be placed in
                // to determine if that placed panel needs a placeholder shown
                // or not

                PerformanceStateAttribute perfState = taskDataPanel.getCurrentState().getState();
                if (PerformanceNodeStateEnum.UNACTIVATED.equals(perfState.getNodeStateEnum())) {
                    hasUpcomingTask = true;
                } else if (PerformanceNodeStateEnum.FINISHED.equals(perfState.getNodeStateEnum())
                        || PerformanceNodeStateEnum.DEACTIVATED.equals(perfState.getNodeStateEnum())) {
                    hasCompletedTask = true;
                } else {
                    hasActiveTask = true;
                }
            }
        }

        final Widget toShow = showingTask ? tasksPanel : emptyTasksPanel;
        if (SummativeAssessmentProvider.getInstance().getDisplayMode() == AssessmentDisplayMode.SUMMATIVE 
                && knowledgeSession.inPastSessionMode()) {
            
            /* If we're viewing a past session in summative mode, hide formative assessments */
            tasksDeck.showWidget(tasksDeck.getWidgetIndex(formativeHidingPanel));
        }
        else {
            tasksDeck.showWidget(tasksDeck.getWidgetIndex(toShow));
        }

        completedTasksPlaceholder.setVisible(!hasCompletedTask);
        activeTasksPlaceholder
                .setVisible(!hasActiveTask && (getOnlyTasksToDisplay() == null || isShowingTaskGroupings));

        // hide the active task placeholder if only selected tasks are visible
        upcomingTasksPlaceholder.setVisible(!hasUpcomingTask);
    }

    /**
     * Gets the knowledge session that this is displaying data for
     *
     * @return the knowledge session whose data is being displayed. Will never
     *         be null.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    /**
     * Updates the internal state data based on the given learner state. This data can later be shown
     * using {@link #updateDisplayedLearnerState()}.
     *
     * @param state the updated learner state. Cannot be null.
     * @param knowledgeSession the knowledge session to apply the learner state. Cannot be null.
     * @return whether the new state that was received contained a new learner state for this session.
     */
    private boolean updateInternalLearnerState(KnowledgeSessionState state, AbstractKnowledgeSession knowledgeSession) {
        if (state == null) {
            throw new IllegalArgumentException("The parameter 'state' cannot be null.");
        } else if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        /* Only update the knowledge state if the specified session is equal to
         * this data panel's session and a learner state is provided. */
        if (!knowledgeSession.equals(this.knowledgeSession) || state.getLearnerState() == null) {
            return false;
        }

        currentState = state;

        return true;
    }

    /**
     * Updates the displayed state data based on the most recent internal learner state. This will visually update
     * the lists of active and upcoming tasks to reflect the performance node assessments and state
     * attributes from the given learner state.
     * 
     * @return an enumerated type of sound that needs to be played for this session
     */
    private AssessmentSoundType updateDisplayedLearnerState() {
        
        KnowledgeSessionState state = currentState;

        PerformanceState performance = state.getLearnerState().getPerformance();

        /* begin updating this task's task panels to match the current state */
        HashMap<Integer, TaskDataPanel> existingTaskPanels = new HashMap<>(nodeIdToTaskPanel);
        nodeIdToTaskPanel.clear();

        AssessmentSoundType requestedSoundType = null;

        /* get the panels corresponding to the tasks found in this state */
        for (Integer taskId : performance.getTasks().keySet()) {

            Set<Integer> onlyTaskToDisplay = getOnlyTasksToDisplay();
            if (onlyTaskToDisplay != null && !onlyTaskToDisplay.contains(taskId)) {

                // prevent any task not in the list from showing
                continue;
            }

            TaskDataPanel taskPanel = existingTaskPanels.remove(taskId);

            if (taskPanel == null) {
                /* a panel does not yet exist for this task, so create one */
                taskPanel = new TaskDataPanel(performance.getTasks().get(taskId), state.getCachedTaskState(taskId),
                        this.knowledgeSession) {

                    @Override
                    public void onViewChanged() {

                        // reapply the filter whenever a task's view is changed
                        logger.info("onViewChanged - applying filter to task data panel(s)");
                        applyFilter();
                    }
                };

            } else {

                try {
                    /* update the existing task panel's state */
                    AssessmentSoundType taskRequestedSoundType = taskPanel
                            .updateState(performance.getTasks().get(taskId), state.getCachedTaskState(taskId));
                    if (AssessmentSoundType.isHigherPriority(taskRequestedSoundType, requestedSoundType)) {
                        // the session requested sound type is higher priority
                        // for this redraw than the current one set for this
                        // redraw
                        requestedSoundType = taskRequestedSoundType;
                    }
                } catch (Exception e) {
                    logger.severe("An error occurred while updating a task's state data: " + e);
                }
            }

            nodeIdToTaskPanel.put(taskId, taskPanel);
        }

        /* remove the panels for any tasks that are not found in the new
         * state */
        for (TaskDataPanel panel : existingTaskPanels.values()) {
            panel.removeFromParent();
        }

        /* sort tasks so that active ones are shown first */
        List<TaskDataPanel> activeTaskPanels = new ArrayList<>(nodeIdToTaskPanel.values());
        List<TaskDataPanel> upcomingTaskPanels = new ArrayList<>();
        List<TaskDataPanel> completedTaskPanels = new ArrayList<>();

        Iterator<TaskDataPanel> itr = activeTaskPanels.iterator();
        while (itr.hasNext()) {

            TaskDataPanel panel = itr.next();

            if (getOnlyTasksToDisplay() != null && !isShowingTaskGroupings) {

                /* if only selected tasks are being displayed and the upcoming
                 * and completed tasks are hidden, ALWAYS show them in the
                 * active task panel */
                continue;
            }

            if (panel.getCurrentState() != null && panel.getCurrentState().getState() != null) {

                PerformanceStateAttribute perfState = panel.getCurrentState().getState();
                if (PerformanceNodeStateEnum.UNACTIVATED.equals(perfState.getNodeStateEnum())) {
                    upcomingTaskPanels.add(panel);
                    itr.remove();
                } else if (PerformanceNodeStateEnum.FINISHED.equals(perfState.getNodeStateEnum())
                        || PerformanceNodeStateEnum.DEACTIVATED.equals(perfState.getNodeStateEnum())) {
                    completedTaskPanels.add(panel);
                    itr.remove();
                }
            }
        }

        updateTaskPanel(activeTaskPanels, activeTasksPanel, activeTasksPlaceholder);
        updateTaskPanel(upcomingTaskPanels, upcomingTasksPanel, upcomingTasksPlaceholder);
        updateTaskPanel(completedTaskPanels, completedTasksPanel, completedTasksPlaceholder);

        if (!nodeIdToTaskPanel.isEmpty() && tasksDeck.getVisibleWidget() != tasksDeck.getWidgetIndex(tasksPanel)) {
            tasksDeck.showWidget(tasksDeck.getWidgetIndex(tasksPanel));

            if (getOnlyTasksToDisplay() == null) {

                /* if multiple tasks should be displayed, show the tooltip for
                 * the upcoming and completed tasks button whenever the task
                 * panel is shown */
                upcomingTasksButtonPopup.show();
                completedTasksButtonPopup.show();
                Timer timer = new Timer() {
                    @Override
                    public void run() {
                        if (popupVisible) {
                            upcomingTasksButtonPopup.hide();
                            completedTasksButtonPopup.hide();
                        }
                    }
                };

                timer.schedule(4000);
            }

        } else if (nodeIdToTaskPanel.isEmpty()
                && tasksDeck.getVisibleWidget() == tasksDeck.getWidgetIndex(tasksPanel)) {
            tasksDeck.showWidget(tasksDeck.getWidgetIndex(emptyTasksPanel));
        }

        // important for the first learner state received when there is a filter
        // that
        // needs to be applied immediately and not just when the user changes
        // the filter state
        applyFilter();

        return requestedSoundType;
    }

    /**
     * Updates the provided flow panel with the state of the task panels
     * provided.
     * 
     * @param taskPanels zero or more panels containing information about tasks
     *        (running state, assessment state, concepts).
     * @param tasksPanel the panel containing the tasks, e.g. this could be the
     *        completed tasks panel.
     * @param placeholder a placeholder component that should contain some
     *        useful label that will be shown if there are no tasks in the
     *        tasksPanel.
     */
    private void updateTaskPanel(List<TaskDataPanel> taskPanels, FlowPanel tasksPanel, HTMLPanel placeholder) {

        for (int i = 0; i < taskPanels.size(); i++) {

            final TaskDataPanel panel = taskPanels.get(i);

            // Sort items via CSS rather than via DOM order. This allows us to
            // alter the order of elements without
            // removing them from the DOM, which maintains their current visual
            // state and avoids re-triggering animations
            Integer oldOrder = null;

            try {
                oldOrder = Integer.valueOf(panel.getElement().getStyle().getProperty("order"));

            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // ignore old order
            }

            panel.getElement().getStyle().setProperty("order", "" + i);

            Boolean animateUp = null;

            if (!tasksPanel.equals(panel.getParent())) {

                tasksPanel.add(panel); // only add new data panels when the
                                       // tasks panel does not yet contain them
                animateUp = false;

            } else if (oldOrder != null) {

                if (i < oldOrder) {
                    animateUp = true;

                } else if (i > oldOrder) {
                    animateUp = false;
                }

            } else {
                animateUp = true;
            }

            if (animateUp != null) {

                if (animateUp) {

                    String animation = Animate.animate(panel, Animation.FADE_OUT);
                    Animate.removeAnimationOnEnd(panel, animation);

                } else {
                    // Adding task to panel OR changing the order of the task
                    // already on the panel
                    String animation = Animate.animate(panel, Animation.FADE_IN);
                    Animate.removeAnimationOnEnd(panel, animation);
                }
            }
        }

        placeholder.setVisible(taskPanels.isEmpty());
    }

    @Override
    public void sessionStateUpdate(KnowledgeSessionState state, int domainSessionId) {
        /* Process the update if the specified session is equal to this panel's
         * session */
        if (!RegisteredSessionProvider.getInstance().hasRegisteredSession()
                || knowledgeSession.getHostSessionMember().getDomainSessionId() != domainSessionId) {
            return;
        }

        /* Null assessment means that entity received a "visual only" state. Do
         * not process further. This was implemented as a quick hack for ARES
         * visualization. */
        if (state.getLearnerState() != null) {
            PerformanceState performance = state.getLearnerState().getPerformance();
            for (TaskPerformanceState perfState : performance.getTasks().values()) {
                for (ConceptPerformanceState c : perfState.getConcepts()) {
                    if (c.getState().getAssessedTeamOrgEntities().containsValue(null)) {
                        return;
                    }
                }
            }
        }

        /* Update this widget's current learner state to match the one provided*/
        boolean needsVisualUpdate = updateInternalLearnerState(state, knowledgeSession);
        if(needsVisualUpdate) {
            
            /* Only perform the visual update if another request to visually update is not already being
             * processed. This ensures the UI doesn't fall behind the most recent request. 
             * 
             * This avoids a problem we've seen where sending a lot of knowledge session states in a short 
             * time period, such as from weapon fire assessments, can make the UI unresponsive */
            if(currentDisplayRequest == null) {
                currentDisplayRequest = new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        
                        try {
                            long startTime = System.currentTimeMillis();
                            AssessmentSoundType requestedSoundType = updateDisplayedLearnerState();
                            
                            if(logger.isLoggable(Level.FINE)) {
                                
                                long renderTime = System.currentTimeMillis() - startTime;
                                logger.fine("Rendered knowledge session state in " + renderTime + "ms");
                            }
                            
                            if(requestedSoundType == AssessmentSoundType.GOOD_ASSESSMENT){
            					BsGameMasterPanel.playGoodPerformanceBeep(knowledgeSession);
                            }else if(requestedSoundType == AssessmentSoundType.POOR_ASSESSMENT){
            					BsGameMasterPanel.playPoorPerformanceBeep(knowledgeSession);
        					}
                            
                        } catch (Exception e) {
                            
                            /* Catch any exceptions to make sure they don't block future rendering */
                            logger.severe("Caught exception while rendering knowledge session state " + e);
    					}
                        
                        currentDisplayRequest = null;
                    }
                };
                
                /* Defer the rendering command so that any server updates that are queued are handled first.
                 * This ensures that the most recent knowledge session state is the one that is rendered. */
                Scheduler.get().scheduleDeferred(currentDisplayRequest);
            }

        }
    }
    
    /**
     * Redraw the tasks panels for this session data panel. The tasks will use
     * the latest received state.
     * 
     * @return an enumerated type of sound that needs to be played
     */
    public AssessmentSoundType redraw() {

        if (Dashboard.getInstance().getSettings().isShowAllTasks() != isShowingTaskGroupings) {

            // need refresh the display to account for the selected tasks, since
            // the option to show all tasks has changed
            return setSelectedTasks(new HashSet<>(selectedTasks));
        }

        AssessmentSoundType requestedSoundType = null;
        for (TaskDataPanel taskDataPanel : nodeIdToTaskPanel.values()) {
            AssessmentSoundType taskRequestedSoundType = taskDataPanel.redraw(false, false, false);

            if (AssessmentSoundType.isHigherPriority(taskRequestedSoundType, requestedSoundType)) {
                // the task requested sound type is higher priority for this
                // redraw than the current one set for this redraw
                requestedSoundType = taskRequestedSoundType;
            }
        }

        scrollNodeIntoView();

        return requestedSoundType;
    }

    /**
     * Return the widget containing the header data.
     *
     * @return the session data header. Won't be null.
     */
    public SessionDataHeader getHeaderPanel() {
        return sessionDataHeader;
    }

    @Override
    public void sessionAdded(final AbstractKnowledgeSession knowledgeSession) {

        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession) && this.knowledgeSession.getHostSessionMember()
                .getDomainSessionId() == knowledgeSession.getHostSessionMember().getDomainSessionId()) {
            
            /* This is another knowledge session for the same domain session, which indicates that the course has moved
             * on to another training application course object. We need let the OC/T Move onto this new session without
             * manually leaving and re-entering Game Master */
            if(Dashboard.getInstance().getSettings().isAutoAdvanceSessions()) {
                
                /* If enabled, automatically advance to the next knowledge session in the course*/
                sessionSelector.onSelectSession(knowledgeSession);
                
            } else {

                /* Otherwise, display a notification to the user that they can click on to move to the next session */
                NotifySettings notifySettings = NotifySettings.newSettings();
                notifySettings.setType(NotifyType.SUCCESS);
                notifySettings.setDelay(0);
                notifySettings.setAnimation(NEW_SESSION_NOTIFICATION_CLASS, null);
    
                if (activeSessionNotification != null) {
                    activeSessionNotification.hide();
                }
    
                /* Tell the user that a new session has been started */
                activeSessionNotification = Notify.notify(
                        "A new session has been started by this course. Click here to start monitoring this session.",
                        notifySettings);
    
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
    
                    @Override
                    public void execute() {
    
                        /* Attach an event lister to the notification to let the
                         * user click it to start listening to the new session */
                        Node notificationNode = JsniUtility.querySelector("." + NEW_SESSION_NOTIFICATION_CLASS);
                        if (notificationNode != null) {
                            Element nodeElement = notificationNode.cast();
                            Event.sinkEvents(nodeElement, Event.ONCLICK);
                            Event.setEventListener(nodeElement, new EventListener() {
    
                                @Override
                                public void onBrowserEvent(Event event) {
                                    if (Event.ONCLICK == event.getTypeInt()) {
                                        sessionSelector.onSelectSession(knowledgeSession);
                                    }
                                }
                            });
                        }
    
                        /* Attach an event listener to the close button so that the
                         * user can close the notification without listening to the
                         * new session */
                        Node closeNode = JsniUtility.querySelector("." + NEW_SESSION_NOTIFICATION_CLASS + " .close");
                        if (closeNode != null) {
                            Element nodeElement = closeNode.cast();
                            Event.sinkEvents(nodeElement, Event.ONCLICK);
                            Event.setEventListener(nodeElement, new EventListener() {
    
                                @Override
                                public void onBrowserEvent(Event event) {
                                    if (Event.ONCLICK == event.getTypeInt()) {
                                        event.stopPropagation();
    
                                        if (activeSessionNotification != null) {
                                            activeSessionNotification.hide();
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }

        /* Restart timer */
        refreshTimer.scheduleRepeating(TIMER_DELAY);
    }

    @Override
    public void sessionEnded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }

        /* Stop refreshing the display */
        refreshTimer.cancel();
    }

    /**
     * Updates this panel's selected tasks to the ones provided. If the selected
     * tasks have changed OR if the setting that controls whether task groupings
     * are shown has been changed, then the tasks displayed by this panel will
     * be changed accordingly.
     * 
     * @param selectedTasks the tasks to select. If this is non-null, then only
     *        the selected tasks will be displayed. Otherwise, all tasks will be
     *        displayed.
     * @return an enumerated type of sound that needs to be played for this
     *         session, if the displayed tasks are actually refreshed
     */
    public AssessmentSoundType setSelectedTasks(Set<Integer> selectedTasks) {
        logger.info("setSelectedTasks");
        LoadingDialogProvider.getInstance().startLoading(LoadingType.SESSION_DATA_PANEL, "Loading Session Assessments",
                "Please wait while the assessments are being rendered...");
        boolean shouldShowGroupings = Dashboard.getInstance().getSettings().isShowAllTasks();

        if (CollectionUtils.equalsIgnoreOrder(this.selectedTasks, selectedTasks)
                && isShowingTaskGroupings == shouldShowGroupings) {
            LoadingDialogProvider.getInstance().loadingComplete(LoadingType.SESSION_DATA_PANEL);
            return null; // selection should not change, so do nothing
        }

        // select the appropriate task
        this.selectedTasks.clear();

        if (selectedTasks != null) {
            this.selectedTasks.addAll(selectedTasks);
        }

        this.isShowingTaskGroupings = shouldShowGroupings;

        if (getOnlyTasksToDisplay() != null && !isShowingTaskGroupings) {

            // hide all non-active task panels
            if (activeTasksCollapse.isHidden()) {
                activeTasksButton.click();
            }

            activeTasksButton.setVisible(false);

            if (completedTasksCollapse.isShown()) {
                completedTasksButton.click();
            }

            completedTasksButton.setVisible(false);

            if (upcomingTasksCollapse.isShown()) {
                upcomingTasksButton.click();
            }

            upcomingTasksButton.setVisible(false);

        } else {

            // display all non-active task panels, but only expand active tasks
            // by default
            if (activeTasksCollapse.isHidden()) {
                activeTasksButton.click();
            }

            activeTasksButton.setVisible(true);

            if (completedTasksCollapse.isShown()) {
                completedTasksButton.click();
            }

            completedTasksButton.setVisible(true);

            if (upcomingTasksCollapse.isShown()) {
                upcomingTasksButton.click();
            }

            upcomingTasksButton.setVisible(true);
        }

        AssessmentSoundType toRetType = null;
        if (currentState != null && knowledgeSession != null) {

            // Refresh all the displayed tasks. If tasks are selected, this will
            // hide all other tasks.
            toRetType = updateDisplayedLearnerState();
        }

        LoadingDialogProvider.getInstance().loadingComplete(LoadingType.SESSION_DATA_PANEL);
        return toRetType;
    }

    @Override
    public void showTasks(Set<Integer> taskIds) {
        setSelectedTasks(taskIds);
    }

    /**
     * If tasks have been selected AND the setting to show all tasks has NOT
     * been enabled, gets the IDs of the tasks that have been selected, since
     * they are the only tasks that should be displayed. <br/>
     * <br/>
     * Otherwise, returns null, which indicates that ALL tasks should be
     * displayed.
     * 
     * @return the IDs of the only tasks that should be displayed, or null if
     *         ALL tasks should be displayed
     */
    private Set<Integer> getOnlyTasksToDisplay() {
        return CollectionUtils.isNotEmpty(selectedTasks) ? selectedTasks : null;
    }

    /**
     * Attempts to find a tree item representing the given performance node
     * within this tree item and, if such a tree item is found, scrolls that
     * tree item into view so that the performance node's data is visible. <br/>
     * <br/>
     * If the performance node is not yet ready to be scrolled into view, then
     * scrolling will be postponed until it is ready
     * 
     * @param performanceNodePath the path to the performance node to scroll
     *        into view. Cannot be null.
     */
    public void scrollNodeIntoView(PerformanceNodePath nodePath) {

        if (nodePath == null) {
            throw new IllegalArgumentException("The path of the node to scroll into view cannot be null");
        }

        this.perfNodeToScroll = nodePath;

        scrollNodeIntoView();
    }

    /**
     * Attempts to find a widget associated with the last performance node that
     * was passed into {@link #scrollNodeIntoView(PerformanceNodePath)} and
     * scroll it into view. <br/>
     * <br/>
     * This method is mainly used to defer the scrolling behavior if the
     * task/concept UI elements are not ready yet.
     */
    private void scrollNodeIntoView() {

        if (perfNodeToScroll == null || nodeIdToTaskPanel.isEmpty() || !isAttached()) {
            return;
        }

        /* Need to wait for the event loop to complete in case task/concept
         * widgets are still being attached */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {

                PerformanceNodePath taskPath = perfNodeToScroll.getPathStart();

                TaskDataPanel taskPanel = nodeIdToTaskPanel.get(taskPath.getNodeId());
                if (taskPanel != null) {

                    taskPanel.getElement().scrollIntoView();

                    if (taskPath.getChild() != null) {
                        taskPanel.scrollNodeIntoView(taskPath.getChild());
                    }
                }

                perfNodeToScroll = null;
            }
        });
    }

    @Override
    public void onSummativeAssessmentsChanged(Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment) {
        // Nothing to do
    }

    @Override
    public void onDisplayModeChanged(AssessmentDisplayMode displayMode) {
        if (displayMode == AssessmentDisplayMode.SUMMATIVE) {
            tasksDeck.showWidget(tasksDeck.getWidgetIndex(formativeHidingPanel));
        
        } else {
            
            if (nodeIdToTaskPanel.isEmpty()) {
                tasksDeck.showWidget(tasksDeck.getWidgetIndex(emptyTasksPanel));
                
            } else {
                tasksDeck.showWidget(tasksDeck.getWidgetIndex(tasksPanel));
            }
        }
    }
    
}
