/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.RunState;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.Component;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.PermissionUpdateHandler;
import mil.arl.gift.tools.dashboard.shared.messages.TaskStateCache;

/**
 * A panel capable of displaying {@link TaskPerformanceState a task's performance state data} and updating it visually in real-time
 * as new data is provided. This widget will display both the task's own performance state data as well as the states of all of that 
 * tasks' sub concepts.
 *
 * @author nroberts
 */
public class TaskDataPanel extends Composite implements ActiveSessionChangeHandler, PermissionUpdateHandler, TaskDataProvider {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TaskDataPanel.class.getName());
    
    /** The default view to use to display task state data when the user has not explicitly chosen a view */
    private static final ViewMode DEFAULT_VIEW = ViewMode.LIST;
    
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

    /** The UiBinder that combines the ui.xml with this java class */
    private static TaskDataPanelUiBinder uiBinder = GWT.create(TaskDataPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface TaskDataPanelUiBinder extends UiBinder<Widget, TaskDataPanel> {
    }
    
    /**
     * An enumation of views that this widget is capable of switching between in order to show
     * task state data to the user in different visual arrangements
     * 
     * @author nroberts
     */
    public static enum ViewMode{
        
        /** A view that displays a list of a task's leaf concept performance states. This view effectively hides
         *  intermediate concepts' performance states from the user. */
        LIST,
        
        /** A view that displays a hierarchy of a tasks's concept performance states. This can be used to view
         *  intermediate concepts' performance states. */
        TREE
    }
    
    @UiField
    protected SimplePanel viewContainer;

    /** The active session provider instance */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();
    
    /** The task state data currently being represented by this panel */
    private TaskPerformanceState currentState;

    /** The current knowledge session */
    private final AbstractKnowledgeSession knowledgeSession;
    
    /** 
     * This task's cached state information as recorded by the server. Used to retrieve lifetime state information not included in
     * each individual learner state update from the domain. 
     */
    private TaskStateCache cachedState;
    
    /**
     * Current set of enumerated components that should be hidden from the user
     * due to the current enumerated mode the game master is in
     */
    private Set<Component> disallowedComponents = new HashSet<>();
    
    /** The view currently being used to display the task state data */
    private TaskDataView currentView;
    
    /**
     * Creates a new panel displaying the given state data for a task
     *
     * @param state the state data for the task to represent
     * @param cachedState optional data surrounding the task's cached state on
     *        the server
     * @param knowledgeSession the current knowledge session. Can't be null.
     */
    public TaskDataPanel(TaskPerformanceState state, TaskStateCache cachedState, final AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        this.knowledgeSession = knowledgeSession;
        
        setViewMode(DEFAULT_VIEW);

        updateState(state, cachedState);

        /* Subscribe to the data providers */
        subscribe();
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Note: this must be done after binding */

        /* Subscribe to the active session changes */
        activeSessionProvider.addHandler(this);

        /* Subscribe to permission changes */
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
     * Redraw this panel using the last state received.
     * 
     * @param updateStateOnRedraw flag determining whether to update the concept
     *        states with the current state.
     * @param allowAlertSound flag determining whether the alert sounds are
     *        allowed. Even if this is true, other criteria might prevent the
     *        sound from being played.
     * @return an enumerated type of sound that needs to be played for this task
     *         and any descendant concepts where
     *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
     *         return null;
     */
    public AssessmentSoundType redraw(final boolean updateStateOnRedraw, boolean allowAlertSound, boolean assessmentChanged){
        return currentView.redraw(updateStateOnRedraw, allowAlertSound, assessmentChanged);
    }

    /**
     * Updates this panel's labels to reflect the attribute data provided by the given task state
     * and updates the list of concepts to match its underling concept states.
     *
     * @param state the task state from which to derive attribute data and concept states
     * @param cachedState optional data surrounding the task's cached state on the server
     * @return an enumerated type of sound that needs to be played for this task and any descendant
     * concepts where {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
     * return null;
     */
    public AssessmentSoundType updateState(TaskPerformanceState state, TaskStateCache cachedState) {
        final PerformanceStateAttribute stateAttr = state.getState();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateState(" + state + ")");
        }

        final TaskPerformanceState oldState = getCurrentState();
        boolean firstState = oldState == null;
        boolean newState = oldState == null || oldState.getState().getShortTermTimestamp() != stateAttr.getShortTermTimestamp();
        this.currentState = state;
        this.cachedState = cachedState;
        
        /* Update the assessment source if a new evaluator is provided or if the
         * short term assessment has changed */
        final boolean assessmentChanged = oldState == null || oldState.getState() == null
                || oldState.getState().getShortTerm() == null
                || !oldState.getState().getShortTerm().equals(stateAttr.getShortTerm());

        // allow alerts such as poor performance sounds to be played
        //... unless this is the first time the task state was provided or the same task state that has already been provided
        //    basically don't want to keep replaying alerts when the user is just opening the assessment panel where this task is drawn
        final boolean updateState = !firstState && newState;
        final boolean allowAlertSound = updateState && assessmentChanged;
        
        return redraw(updateState, allowAlertSound, assessmentChanged);
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
    
    @Override
    public TaskPerformanceState getCurrentState() {
        return currentState;
    }
    
    /**
     * Refreshes any widget that has a timestamp or something else that needs frequent updating.
     */
    public void refreshTimerWidgets() {
        currentView.refreshTimerWidgets();
    }
    
    /**
     * Update the visibility of the status control icon
     */
    private void updateStatusControlIconVisibility() {
        
        final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        boolean running = RunState.RUNNING.equals(activeSessionProvider.getRunState(dsId));

        boolean disallowed = disallowedComponents.contains(Component.TASK_START_END);
        
        currentView.updateStatusControlIconVisibility(running && !disallowed);
    }

    @Override
    public void permissionUpdate(Set<Component> disallowedComponents) {
        
        this.disallowedComponents = disallowedComponents;
        
        updateStatusControlIconVisibility();
    }

    @Override
    public void sessionAdded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }
        
        /* Update UI components */
        updateStatusControlIconVisibility();
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
        updateStatusControlIconVisibility();
    }
    
    @Override
    public void setViewMode(ViewMode view) {
        
        TaskDataView newView;
        
        switch(view) {
            case TREE:
                newView = new TaskDataTreeView(this);
                break;
            default:
                newView = new TaskDataListView(this);
        }
        
        viewContainer.setWidget(newView);
        currentView = newView;
        
        currentView.redraw(false, false, true);
        
        updateStatusControlIconVisibility();
        
        onViewChanged();
    }

    @Override
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    @Override
    public TaskStateCache getCachedState() {
        return cachedState;
    }

    /**
     * Updates the visual state of the task data shown by this widget to match the provided filter
     * 
     * @return whether all of the task's concepts have been filtered out.
     */
    public boolean applyConceptFilter() {
        return currentView.applyConceptFilter();
    }
    
    /**
     * Handles when the view within this data panel is changed. Can be used to execute special logic upon
     * switching views.
     */
    public void onViewChanged() {
        //do nothing by default
    }
    
    /**
     * Attempts to find a widget representing the given performance node within this widget and,
     * if such a widget is found, scrolls that widget into view so that the performance node's data
     * is visible
     * 
     * @param performanceNodePath the path to the performance node to scroll into view. Cannot be null.
     */
    public void scrollNodeIntoView(PerformanceNodePath performanceNodePath) {
        
        if(performanceNodePath == null) {
            throw new IllegalArgumentException("The path of the node to scroll into view cannot be null");
        }
        
        if(currentState != null 
                && currentState.getState() != null) {
            
            currentView.scrollNodeIntoView(performanceNodePath);
        }
    }
}
