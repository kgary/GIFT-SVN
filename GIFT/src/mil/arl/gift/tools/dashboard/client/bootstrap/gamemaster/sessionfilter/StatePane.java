/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityFilterProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityStatusProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityStatusProvider.EntityStatusChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider.RegisteredSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider.SessionStateUpdateHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.AssessmentDisplayMode;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.SummativeAssessmentChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.EndSessionRequest;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Status;

/**
 * The game master state pane contains the mission details, team structure, and
 * team member objectives, equipment, and health status details.
 * 
 * @author sharrison
 */
public class StatePane extends Composite
        implements RegisteredSessionChangeHandler, EntityStatusChangeHandler, SessionStateUpdateHandler, SummativeAssessmentChangeHandler {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StatePane.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static StatePaneUiBinder uiBinder = GWT.create(StatePaneUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StatePaneUiBinder extends UiBinder<Widget, StatePane> {
    }

    /** The section for the mission information */
    @UiField
    protected StatePaneSection<String> missionSection;

    /** The section for the playable team structure */
    @UiField(provided = true)
    protected StatePaneSection<AbstractTeamUnit> teamsRolesSection = new StatePaneSection<>("Teams and Roles",
            new ChangeCallback<AbstractTeamUnit>() {
                @Override
                public void onChange(AbstractTeamUnit newValue, AbstractTeamUnit oldValue) {
                    /* Rebuild the panels with the new team unit */
                    buildObjectivesPanel();
                    buildEquipmentPanel();
                    buildHealthPanel();

                    /* Exit early */
                    if (newValue == null || session == null) {
                        objectivesSection.openCollapse(false);
                        return;
                    }

                    Set<String> members;
                    if (newValue instanceof Team) {
                        members = ((Team) newValue).getTeamMemberNames();
                    } else {
                        /* Add single member */
                        members = new HashSet<>();
                        members.add(newValue.getName());
                    }
                    
                    // expose the objectives panel when a team org entry is selected to help show the objectives
                    // for that entry.  A more sophisticated approach might be needed if the user collapsed the objectives
                    // panel because they don't want to see it and this logic keeps opening it again.
                    objectivesSection.openCollapse(true);

                    EntityFilterProvider.getInstance()
                            .entitySelected(session.getHostSessionMember().getDomainSessionId(), members);
                }
            });

    /** The section for the mission objectives */
    @UiField(provided = true)
    protected StatePaneSection<TaskPerformanceState> objectivesSection = new StatePaneSection<>("Objectives", true,
            new ChangeCallback<TaskPerformanceState>() {
                @Override
                public void onChange(TaskPerformanceState newValue, TaskPerformanceState oldValue) {
                    sendSelectedObjectives();
                }
            });

    /** The section for the equipment status */
    @UiField
    protected StatePaneSection<AbstractTeamUnit> equipmentSection;

    /** The section for the member health status */
    @UiField
    protected StatePaneSection<AbstractTeamUnit> healthSection;

    /** The registered session provider */
    private final RegisteredSessionProvider registeredSessionProvider = RegisteredSessionProvider.getInstance();

    /**
     * The first valid states to use for determining which team members are
     * being assessed
     */
    private List<TaskPerformanceState> firstValidStates;

    /** The most recent state update to be received */
    private KnowledgeSessionState mostRecentState;

    /**
     * This is here so we aren't creating a new set each time the objective
     * selection changes
     */
    private final Set<Integer> objectivesToSend = new HashSet<>();

    /** The confirmation prompt for ending the session */
    private ModalDialogBox endSessionConfirmation = null;

    /** 
     * A callback used to play back a session when the appropriate button is clicked. 
     * Used to begin playing back a session after it ends.
     */
    private PlayBackSessionCallback playBackSessionCallback;

    /**
     * The knowledge session used for this panel. This should only be set on
     * {@link #registeredSessionChanged(AbstractKnowledgeSession, AbstractKnowledgeSession)}
     */
    private AbstractKnowledgeSession session;
    
    /**
     * Enumerated search results for checking if a specific team/team-member(s) are in a performance
     * state hierarchy.
     * 
     * @author mhoffman
     *
     */
    private enum TeamUnitSearchResultEnum{
        /*
         * ORDER MATTERS!!!! - the last entry is the highest priority
         */
        NO_MEMBERS,  // when there are no team members at that performance state or any descendant level
        OTHER_MEMBERS_ONLY,  // when there are team members at that performance state or descendant level and the specific team member(s) are not there
        FOUND_SPECIFIC_MEMBER // when the specified team member(s) are at that performance state or descendant level
    }

    /** Constructor */
    public StatePane() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        objectivesSection.setComparator(new Comparator<TaskPerformanceState>() {
            @Override
            public int compare(TaskPerformanceState o1, TaskPerformanceState o2) {
                /* Check if either object is null. Non-nulls appear before
                 * nulls. */
                if (o1 == null) {
                    return o2 != null ? 1 : 0;
                } else if (o2 == null) {
                    return -1;
                }

                final PerformanceStateAttribute o1State = o1.getState();
                final PerformanceStateAttribute o2State = o2.getState();

                /* Check if either object is null. Non-nulls appear before
                 * nulls. */
                if (o1State == null) {
                    return o2State != null ? 1 : 0;
                } else if (o2State == null) {
                    return -1;
                }

                return Integer.compare(o1State.getNodeId(), o2State.getNodeId());
            }
        });

        /* Subscribe to the data providers */
        subscribe();
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to registered session changes */
        registeredSessionProvider.addManagedHandler(this);

        /* Subscribe to entity status changes */
        EntityStatusProvider.getInstance().addManagedHandler(this);

        /* Subscribe to the knowledge session state updates */
        SessionStateProvider.getInstance().addHandler(this);
        
        /* Subscribe to get summative assessment updates */
        SummativeAssessmentProvider.getInstance().addHandler(this);
    }

    /** Build the mission section panel */
    private void buildMissionPanel() {
        missionSection.clear();

        if (session == null) {
            return;
        }

        /* Scenario name is always first */
        missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.SCENARIO_NAME))
                .appendEscaped(session.getNameOfSession()).toSafeHtml(), StatePaneConstants.SCENARIO_NAME);

        /* Add mission details if possible */
        if (session.getMission() != null) {
            final Mission mission = session.getMission();
            if (StringUtils.isNotBlank(mission.getSource())) {
                missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.SOURCE))
                        .appendEscaped(mission.getSource()).toSafeHtml(), StatePaneConstants.SOURCE);
            }

            if (StringUtils.isNotBlank(mission.getMET())) {
                missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.MET))
                        .appendEscaped(mission.getMET()).toSafeHtml(), StatePaneConstants.MET);
            }

            if (StringUtils.isNotBlank(mission.getTask())) {
                missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.TASK))
                        .appendEscaped(mission.getTask()).toSafeHtml(), StatePaneConstants.TASK);
            }

            if (StringUtils.isNotBlank(mission.getSituation())) {
                missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.SITUATION))
                        .appendEscaped(mission.getSituation()).toSafeHtml(), StatePaneConstants.SITUATION);
            }

            if (StringUtils.isNotBlank(mission.getGoals())) {
                missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.GOALS))
                        .appendEscaped(mission.getGoals()).toSafeHtml(), StatePaneConstants.GOALS);
            }

            if (StringUtils.isNotBlank(mission.getCondition())) {
                missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.CONDITION))
                        .appendEscaped(mission.getCondition()).toSafeHtml(), StatePaneConstants.CONDITION);
            }

            if (StringUtils.isNotBlank(mission.getROE())) {
                missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.ROE))
                        .appendEscaped(mission.getROE()).toSafeHtml(), StatePaneConstants.ROE);
            }

            if (StringUtils.isNotBlank(mission.getThreatWarning())) {
                missionSection.add(
                        new SafeHtmlBuilder().append(bold(StatePaneConstants.THREAT_WARNING))
                                .appendEscaped(mission.getThreatWarning()).toSafeHtml(),
                        StatePaneConstants.THREAT_WARNING);
            }

            if (StringUtils.isNotBlank(mission.getWeaponStatus())) {
                missionSection.add(
                        new SafeHtmlBuilder().append(bold(StatePaneConstants.WEAPON_STATUS))
                                .appendEscaped(mission.getWeaponStatus()).toSafeHtml(),
                        StatePaneConstants.WEAPON_STATUS);
            }

            if (StringUtils.isNotBlank(mission.getWeaponPosture())) {
                missionSection.add(
                        new SafeHtmlBuilder().append(bold(StatePaneConstants.WEAPON_POSTURE))
                                .appendEscaped(mission.getWeaponPosture()).toSafeHtml(),
                        StatePaneConstants.WEAPON_POSTURE);
            }
        }

        /* Create the advanced section */
        final List<ListGroupItem> advancedItems = new ArrayList<>();

        final Icon caretIcon = new Icon(IconType.CARET_RIGHT);
        caretIcon.getElement().getStyle().setPosition(Position.ABSOLUTE);
        caretIcon.getElement().getStyle().setLeft(4, Unit.PX);
        caretIcon.getElement().getStyle().setTop(27, Unit.PCT);

        final ListGroupItem advancedHeader = missionSection.add(bold("Advanced Details"), "mission_advanced");
        advancedHeader.insert(caretIcon, 0);
        advancedHeader.getElement().getStyle().setCursor(Cursor.POINTER);
        advancedHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final boolean expand = IconType.CARET_RIGHT.equals(caretIcon.getType());

                caretIcon.setType(expand ? IconType.CARET_DOWN : IconType.CARET_RIGHT);
                for (ListGroupItem subItem : advancedItems) {
                    subItem.getElement().getStyle().setDisplay(expand ? Display.BLOCK : Display.NONE);
                }
            }
        }, ClickEvent.getType());

        advancedItems.add(missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.HOST_USERNAME))
                .appendEscaped(session.getHostSessionMember().getSessionMembership().getUsername()).toSafeHtml(),
                StatePaneConstants.HOST_USERNAME));

        if (session.getSessionStartTime() > 0) {
            Date start = new Date(session.getSessionStartTime());
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.append(bold(StatePaneConstants.TIME))
                    .appendHtmlConstant(StatePaneConstants.FULL_DATE_FORMAT.format(start));
            if (session.getSessionEndTime() > session.getSessionStartTime()) {
                /* Concatenate end time */
                sb.appendHtmlConstant(Constants.SPACE).appendHtmlConstant("-").appendHtmlConstant(Constants.SPACE)
                        .appendHtmlConstant(
                                StatePaneConstants.FULL_DATE_FORMAT.format(new Date(session.getSessionEndTime())));

                /* Concatenate duration */
                String duration = FormattedTimeBox.getDisplayText(
                        (int) (session.getSessionEndTime() - session.getSessionStartTime()) / 1000, true);
                sb.appendHtmlConstant(Constants.SPACE).appendHtmlConstant(Constants.HTML_NEWLINE)
                        .append(bold(StatePaneConstants.DURATION)).appendHtmlConstant(duration);
            }

            advancedItems.add(missionSection.add(sb.toSafeHtml(), StatePaneConstants.TIME));
        }

        advancedItems.add(missionSection.add(
                new SafeHtmlBuilder().append(bold(StatePaneConstants.SESSION_COURSE_ID))
                        .appendEscaped(session.getCourseRuntimeId()).toSafeHtml(),
                StatePaneConstants.SESSION_COURSE_ID));
        advancedItems.add(missionSection.add(
                new SafeHtmlBuilder().append(bold(StatePaneConstants.SESSION_SOURCE_COURSE_ID))
                        .appendEscaped(session.getCourseSourceId()).toSafeHtml(),
                StatePaneConstants.SESSION_SOURCE_COURSE_ID));

        if (session instanceof TeamKnowledgeSession) {
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            TeamKnowledgeSession teamSession = (TeamKnowledgeSession) session;
            /* Note: add 1 to include the host in the count since the host is
             * not currently in the list of joined members */
            sb.append(bold(StatePaneConstants.SESSION_PARTICIPANTS))
                    .appendHtmlConstant(Integer.toString(teamSession.getJoinedMembers().size() + 1))
                    .appendHtmlConstant(" out of ")
                    .appendHtmlConstant(Integer.toString(teamSession.getTotalPossibleTeamMembers()));

            advancedItems.add(missionSection.add(sb.toSafeHtml(), StatePaneConstants.SESSION_PARTICIPANTS));
        }

        advancedItems
                .add(missionSection.add(new SafeHtmlBuilder().append(bold(StatePaneConstants.SESSION_HOST_SESSION_ID))
                        .appendEscaped(Integer.toString(session.getHostSessionMember().getDomainSessionId()))
                        .toSafeHtml(), StatePaneConstants.SESSION_HOST_SESSION_ID));

        LogMetadata logMetadata = registeredSessionProvider.getLogMetadata();
        if (logMetadata != null) {
            advancedItems.add(missionSection.add(
                    new SafeHtmlBuilder().append(bold(StatePaneConstants.SESSION_LOG_FILE))
                            .appendEscaped(logMetadata.getLogFile()).toSafeHtml(),
                    StatePaneConstants.SESSION_LOG_FILE));
        }

        if (StringUtils.isNotBlank(session.getScenarioDescription())) {
            advancedItems.add(missionSection.add(
                    new SafeHtmlBuilder().append(bold(StatePaneConstants.SESSION_DESCRIPTION))
                            .appendEscaped(session.getScenarioDescription()).toSafeHtml(),
                    StatePaneConstants.SESSION_DESCRIPTION));
        }

        /* Indent advanced items and hide them by default */
        final double indentSize = StatePaneConstants.BASE_INDENT + 1;
        for (ListGroupItem subItem : advancedItems) {
            subItem.getElement().getStyle().setPaddingLeft(indentSize, Unit.EM);
            subItem.getElement().getStyle().setDisplay(Display.NONE);
        }
        
        missionSection.getElement().getStyle().setFontSize(12, Unit.PX);
    }

    /**
     * Build the teams and roles section panel.
     * 
     * @param team the team to traverse to build the team panel.
     * @param hidingRootNode true if the root team was not added to the list.
     */
    private void buildTeamPanel(final Team team, boolean hidingRootNode) {
        /* Clear the section, so we can build the panel from scratch. Make sure
         * to only do this when the provided team is the root because this is a
         * recursive method. */
        boolean shouldHideTeamLevelInstance = false;
        if (team != null && team.getParentTeam() == null) {
            teamsRolesSection.clear();
            
            // since this is the root team, hide by default until proven it has a team member child
            // which means this team is most likely something that should be shown as an element in the hierarchy.
            shouldHideTeamLevelInstance = true;
            for (final AbstractTeamUnit unit : team.getUnits()) {
                if (unit instanceof TeamMember<?>) {
                    shouldHideTeamLevelInstance = false;
                    break;
                }
            }
        }

        if (team == null || !team.hasPlayableTeamMember()) {
            return;
        }

        /* Get the team depth for the units to calculate the padding size.  Offset the depth by 1 if the root team was not added. */
        double paddingSize = team.getTeamDepth() - (hidingRootNode ? 1 : 0) + StatePaneConstants.BASE_INDENT;

        if(!shouldHideTeamLevelInstance){
            /* Add this team since it has playable members (and is not the hidden root team) */
            final ListGroupItem addedTeamItem = teamsRolesSection.add(SafeHtmlUtils.fromString(team.getName()), team);
            if (addedTeamItem != null) {
                /* Increment padding size for the children */
                addedTeamItem.getElement().getStyle().setPaddingLeft(paddingSize++, Unit.EM);
            }
        }

        /* Add the team units to the list if it is or contains a playable
         * member */
        for (final AbstractTeamUnit unit : team.getUnits()) {
            if (unit instanceof Team) {
                buildTeamPanel((Team) unit, shouldHideTeamLevelInstance);
            } else if (unit instanceof TeamMember<?>) {
                if (!((TeamMember<?>) unit).isPlayable()) {
                    continue;
                }

                ListGroupItem addedItem = teamsRolesSection.add(SafeHtmlUtils.fromString(unit.getName()), unit);
                if (addedItem != null && team.getTeamDepth() != 0) {
                    addedItem.getElement().getStyle().setPaddingLeft(paddingSize, Unit.EM);
                }
            }
        }
    }

    /** Build the objectives section panel */
    private void buildObjectivesPanel() {
        objectivesSection.clear();
        int objectiveSize;
        if (session == null) {
            objectivesSection.setEmptyLabelText("There are no objectives");
            return;
        } else if (firstValidStates == null) {
            objectivesSection.setEmptyLabelText("Waiting for a real-time assessment...");
            return;
        }

        final boolean hasTeamMembers = !teamsRolesSection.getIdentifiers().isEmpty();
        final boolean showScenarioSupport = Dashboard.getInstance().getSettings().isShowScenarioSupport();
        for (final TaskPerformanceState task : firstValidStates) {
            /* Skip scenario support tasks if the setting is set to false */
            if (task.getState().isScenarioSupportNode() && !showScenarioSupport) {
                continue;
            }

            /* If selected unit is being assessed or there are no team members,
             * add the objective */
            if (!hasTeamMembers || checkAssessedUnit(task) != TeamUnitSearchResultEnum.OTHER_MEMBERS_ONLY) {
                
                ClickHandler onOverallAssessmentClicked = null;
                if (session.inPastSessionMode()) {
                    
                    onOverallAssessmentClicked = new ClickHandler() {
                        
                        @Override
                        public void onClick(ClickEvent event) {
                            
                            /* Allow the OC to assign overall assessments to this objective's conditions */
                            event.stopPropagation();
                            OverallAssessmentDialog.get().load(task, session);
                        }
                    };
                }
                
                objectivesSection.add(new ObjectiveItem(task, onOverallAssessmentClicked), task);
            }
        }
        objectiveSize = objectivesSection.size();
        if (hasTeamMembers && teamsRolesSection.getSelectedItem() == null) {
            objectivesSection.setEmptyLabelText("Please select a team member");
        } else if (objectiveSize == 0) {
            StringBuilder sb = new StringBuilder("There are no objectives");
            if (teamsRolesSection.getSelectedItem() != null) {
                sb.append(" for '").append(teamsRolesSection.getSelectedItem().getName()).append("'");
            }
            objectivesSection.setEmptyLabelText(sb.toString());
        } 
        
        updateObjectiveAssessments();

        /* Send the objectives that are being shown */
        sendSelectedObjectives();
    }

    /** Apply the current settings filters */
    public void applySettingsFilter() {

        /* Get current selection, rebuild objectives panel, and the re-set the
         * selection if possible */
        final TaskPerformanceState selectedObjective = objectivesSection.getSelectedItem();
        buildObjectivesPanel();
        /* If non-null, this will re-select the objective */
        objectivesSection.setSelectedItem(selectedObjective);
    }

    /**
     * Checks if the selected team unit from the {@link #teamsRolesSection} is
     * being assessed in any task or concept.
     * 
     * @param perfState the performance state to check.
     * @return the enumerated search result for the performance state provided. This is recursively called
     * by this method.  Null will not be returned.  
     */
    private TeamUnitSearchResultEnum checkAssessedUnit(AbstractPerformanceState perfState) {
        final AbstractTeamUnit selectedTeamUnit = teamsRolesSection.getSelectedItem();
        if (perfState == null || selectedTeamUnit == null) {
            return TeamUnitSearchResultEnum.NO_MEMBERS;
        }

        final PerformanceStateAttribute stateAttr = perfState.getState();
        TeamUnitSearchResultEnum result = TeamUnitSearchResultEnum.NO_MEMBERS;
        if (stateAttr != null) {
            
            if (selectedTeamUnit instanceof Team) {
                Team team = (Team) selectedTeamUnit;
                if (stateAttr.getAssessedTeamOrgEntities().keySet().containsAll(team.getPlayableTeamMemberNames())) {
                    return TeamUnitSearchResultEnum.FOUND_SPECIFIC_MEMBER;
                }
            } else if (stateAttr.getAssessedTeamOrgEntities().containsKey(selectedTeamUnit.getName())) {
                return TeamUnitSearchResultEnum.FOUND_SPECIFIC_MEMBER;
            } else if (perfState instanceof ConceptPerformanceState){
                
                if(stateAttr.getAssessedTeamOrgEntities().isEmpty()) {
                
                    /* The concept does not assess any specific team units, 
                     * so act as if it is assessing all of them */
                    return TeamUnitSearchResultEnum.NO_MEMBERS;
                }else{
                    /*
                     * is assessing members just not the ones we are looking for now
                     */
                    return TeamUnitSearchResultEnum.OTHER_MEMBERS_ONLY;
                }
            }
        }

        if (perfState instanceof TaskPerformanceState) {
            TaskPerformanceState taskState = (TaskPerformanceState) perfState;
            for (ConceptPerformanceState conceptState : taskState.getConcepts()) {
                
                TeamUnitSearchResultEnum candidateResult = checkAssessedUnit(conceptState);
                if(candidateResult.ordinal() > result.ordinal()){
                    // found a better result than the current result
                    result = candidateResult;
                }
                
                if(result == TeamUnitSearchResultEnum.FOUND_SPECIFIC_MEMBER){
                    // can't get any better result than this
                    return result;
                }
            }
        } else if (perfState instanceof IntermediateConceptPerformanceState) {
            IntermediateConceptPerformanceState icState = (IntermediateConceptPerformanceState) perfState;
            for (ConceptPerformanceState conceptState : icState.getConcepts()) {
                
                TeamUnitSearchResultEnum candidateResult = checkAssessedUnit(conceptState);
                if(candidateResult.ordinal() > result.ordinal()){
                    // found a better result than the current result
                    result = candidateResult;
                }
                
                if(result == TeamUnitSearchResultEnum.FOUND_SPECIFIC_MEMBER){
                    // can't get any better result than this
                    return result;
                }
            }
        }

        return result;
    }

    /** Build the equipment section panel */
    private void buildEquipmentPanel() {
        equipmentSection.clear();
    }

    /** Build the health section panel */
    private void buildHealthPanel() {
        healthSection.clear();
    }

    /**
     * Sends the selected objectives to the provider for anyone that is
     * listening.
     */
    private void sendSelectedObjectives() {
        if (firstValidStates == null) {
            return;
        }
        
        logger.info("sendSelectedObjects");

        objectivesToSend.clear();

        final TaskPerformanceState selectedValue = objectivesSection.getSelectedItem();
        if (selectedValue != null && selectedValue.getState() != null) {
            objectivesToSend.add(selectedValue.getState().getNodeId());
        } else {
            /* Show all visible */
            for (TaskPerformanceState state : objectivesSection.getIdentifiers()) {
                if (state.getState() != null) {
                    objectivesToSend.add(state.getState().getNodeId());
                }
            }
        }

        /* Only send to the timeline if in PAST mode */
        if (session.inPastSessionMode()) {
            LoadingDialogProvider.getInstance().startLoading(LoadingType.TIMELINE_REFRESH, "Updating Selection",
                    "Updating the timeline with the current selection...");
            TimelineProvider.getInstance().showTasks(objectivesToSend);
        }
        LoadingDialogProvider.getInstance().startLoading(LoadingType.SESSION_DATA_PANEL, "Updating Selection",
                "Updating the assessments with the current selection...");
        SessionStateProvider.getInstance().showTasks(objectivesToSend);
    }

    @Override
    public void registeredSessionChanged(final AbstractKnowledgeSession newSession,
            final AbstractKnowledgeSession oldSession) {
        if (newSession == null) {
            if (!oldSession.inPastSessionMode()) {
                
                FlowPanel endedPanel = new FlowPanel();
                
                /* Overlay the text on the right-side of the header */
                endedPanel.getElement().getStyle().setProperty("display", "flex");
                endedPanel.getElement().getStyle().setProperty("alignItems", "center");
                endedPanel.setHeight("100%");
                
                final Label label = new Label("(ended)");
                
                endedPanel.add(label);
                
                //add a button to let the user play back the session after it has ended
                Button pastButton = new Button(null, IconType.EXTERNAL_LINK, new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        event.stopPropagation();
                        
                        UiManager.getInstance().displayConfirmDialog("Open Session for Playback?", 
                                "Are you sure you want to open this session for playback?"
                                + "<br/><br/>If you select Yes, then all of the session data that is currently "
                                + "being displayed will be reloaded in order to build a timeline of events.",
                                new ConfirmationDialogCallback() {
                            
                            @Override
                            public void onDecline() {
                                // Nothing to do
                            }
                            
                            @Override
                            public void onAccept() {
                                onPlaybackSession(oldSession);
                                
                                //reset the header
                                missionSection.addHeaderWidget(null, true);
                            }
                        });
                    }
                });
                
                pastButton.addStyleName("gmEndSessionJumpBtn");
                
                final ManagedTooltip pastButtonTooltip =  ManagedTooltip.attachTooltip(
                        pastButton, "This session has ended. Click here to play back the session.");
                pastButtonTooltip.setPlacement(Placement.RIGHT);
                        
                endedPanel.add(pastButtonTooltip);
                
                missionSection.addHeaderWidget(endedPanel, true);
                
                //show the tooltip automatically and hide it after a few seconds
                pastButtonTooltip.show();
                new Timer() {

                    @Override
                    public void run() {
                        pastButtonTooltip.hide();
                    }
                    
                }.schedule(3000);
            }
            return;
        }

        if (endSessionConfirmation != null) {
            endSessionConfirmation.hide();
        }

        /* Reset panel with the new session */
        firstValidStates = null;
        mostRecentState = null;
        objectivesToSend.clear();
        session = newSession;

        /* Start loading the state pane. Loading is complete once the first
         * learner state has been received. */
        LoadingDialogProvider.getInstance().startLoading(LoadingType.STATE_PANE, "Loading Session",
                "Please wait while the session data is loaded...");

        buildMissionPanel();
        buildTeamPanel(newSession == null ? null : newSession.getTeamStructure(), false);

        /* Reset panel state */
        missionSection.openCollapse(false);
        teamsRolesSection.openCollapse(true);
        objectivesSection.openCollapse(false);
        equipmentSection.openCollapse(false);
        healthSection.openCollapse(false);

        if (!newSession.inPastSessionMode()) {
            
            FlowPanel iconPanel = new FlowPanel();
            
            /* Overlay the text on the right-side of the header */
            iconPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
            iconPanel.getElement().getStyle().setTop(0, Unit.PX);
            iconPanel.getElement().getStyle().setRight(0, Unit.PX);
            iconPanel.getElement().getStyle().setProperty("display", "flex");
            iconPanel.getElement().getStyle().setProperty("alignItems", "center");
            iconPanel.setHeight("100%");
            
            final Icon icon = new Icon(IconType.CIRCLE_O_NOTCH);
            icon.setSpin(true);
            /* Overlay the icon on the right-side of the header */
            icon.getElement().getStyle().setMargin(5, Unit.PX);
            ManagedTooltip.attachTooltip(icon, "This session is active. Click here to stop the session.");
            icon.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation();

                    if (endSessionConfirmation == null) {
                        /* Display a prompt asking the user if they want to end
                         * the selected session */
                        endSessionConfirmation = new ModalDialogBox();
                        endSessionConfirmation.setGlassEnabled(true);
                        endSessionConfirmation.setText("End Session?");

                        Button yesButton = new Button("Yes");
                        yesButton.setType(ButtonType.PRIMARY);
                        yesButton.addClickHandler(new ClickHandler() {

                            @Override
                            public void onClick(ClickEvent event) {
                                String username = UiManager.getInstance().getUserName();
                                EndSessionRequest msgPayload = new EndSessionRequest(username);
                                BrowserSession.getInstance()
                                        .sendWebSocketMessage(new DashboardMessage(msgPayload, newSession));
                                endSessionConfirmation.hide();
                            }
                        });
                        endSessionConfirmation.setFooterWidget(yesButton);

                        endSessionConfirmation.setCloseable(true);
                        endSessionConfirmation.getCloseButton().setText("No");
                        endSessionConfirmation.getCloseButton().setType(ButtonType.DANGER);
                    }

                    endSessionConfirmation.setWidget(new HTML(
                            "Are you sure you want to terminate <b>" + newSession.getNameOfSession() + "</b>?"));

                    endSessionConfirmation.center();
                }
            });
            iconPanel.add(icon);
            missionSection.addHeaderWidget(iconPanel, true);
        }
    }

    @Override
    public void logPatchFileChanged(String logPatchFileName) {
        /* Must be in playback mode to care */
        if (!registeredSessionProvider.hasLogMetadata()) {
            return;
        }

        /* TODO: Handle log patch update. Does anything need to happen in this
         * panel? Refresh all sections except mission? */
    }

    @Override
    public void entityStatusChange(int domainSessionId, String teamRole, Status newStatus, Status oldStatus) {
        /* Ignore if the registered session doesn't have a team */
        if (StringUtils.isBlank(teamRole) || !registeredSessionProvider.hasRegisteredSession()
                || registeredSessionProvider.getRegisteredSession().getTeamStructure() == null) {
            return;
        }

        final AbstractTeamUnit teamUnit = registeredSessionProvider.getRegisteredSession().getTeamStructure()
                .getTeamElement(teamRole);
        teamsRolesSection.updateItemAssessmentState(teamUnit, newStatus);
    }

    @Override
    public void sessionStateUpdate(KnowledgeSessionState state, int domainSessionId) {
        /* Process the update if the specified session is equal to this panel's
         * session */
        if (registeredSessionProvider.hasRegisteredSession() && registeredSessionProvider.getRegisteredSession()
                .getHostSessionMember().getDomainSessionId() != domainSessionId) {
            return;
        } else if (state.getLearnerState() == null || state.getLearnerState().getPerformance() == null
                || state.getLearnerState().getPerformance().getTasks().isEmpty()) {
            /* Nothing to process */
            return;
        }

        mostRecentState = state;

        if (firstValidStates == null) {
            final List<TaskPerformanceState> tasks = new ArrayList<>(
                    state.getLearnerState().getPerformance().getTasks().values());

            Iterator<TaskPerformanceState> taskItr = tasks.iterator();
            while (taskItr.hasNext()) {
                TaskPerformanceState tps = taskItr.next();
                if (tps.getState() == null
                        || CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME.equals(tps.getState().getName())) {
                    taskItr.remove();
                }
            }

            /* Can't proceed if no tasks remain */
            if (tasks.isEmpty()) {
                return;
            }

            /* Sort by name since task list is unreliable in a map. Ideally we
             * would like to sort by time like in the timeline, but I didn't
             * want to do another blocking server call here to get all the
             * learner states. Would be nice to 'share' the server call results
             * from the timeline. */
            Collections.sort(tasks, new Comparator<TaskPerformanceState>() {
                @Override
                public int compare(TaskPerformanceState o1, TaskPerformanceState o2) {
                    return o1.getState().getName().compareTo(o2.getState().getName());
                }
            });
            firstValidStates = tasks;

            buildObjectivesPanel();
            LoadingDialogProvider.getInstance().loadingComplete(LoadingType.STATE_PANE);
        } else {
            updateObjectiveAssessments();
        }
    }
    
    /**
     * When the session fails to load successfully this method can be used to clean up 
     * any logic managed in this class when the session was starting to be loaded.
     * E.g. removing the loading dialog. #4972
     */
    public void sessionUnloadedUnexpectedly(){        
        LoadingDialogProvider.getInstance().loadingComplete(LoadingType.STATE_PANE);
    }

    /**
     * Updates the objective assessment states based on the most recent
     * {@link KnowledgeSessionState} received.
     */
    private void updateObjectiveAssessments() {
        if (mostRecentState == null) {
            return;
        }

        /* Update objective states */
        for (TaskPerformanceState task : mostRecentState.getLearnerState().getPerformance().getTasks().values()) {
            if (task.getState() == null) {
                continue;
            }
            
            AssessmentLevelEnum assessment;

            if (session.inPastSessionMode() 
                    && SummativeAssessmentProvider.getInstance().getDisplayMode() == AssessmentDisplayMode.SUMMATIVE) {
                
                /* If this is a past session and the timeline is set to show summative assessments, make sure the colors of 
                 * the objectives match the assessments in the timeline */
                assessment = AssessmentLevelEnum.UNKNOWN;
                
                Map<String, AssessmentLevelEnum> summativeAssessments = SummativeAssessmentProvider.getInstance().getSummativeAssessments();
                if(summativeAssessments != null) {
                    
                    AssessmentLevelEnum summativeAssessment = summativeAssessments.get(task.getState().getName());
                    if(summativeAssessment != null) {
                        assessment = summativeAssessment;
                    }
                }
                
            } else {
                
                /* Otherwise, use formative assessments for the objective colors */
                assessment = task.getState().getShortTerm();
            }
            
            objectivesSection.updateItemAssessmentState(task, assessment);
        }
    }

    @Override
    public void showTasks(Set<Integer> taskIds) {
        /* Nothing to do */
    }
    
    /**
     * Sets the callback that will be used to play back a session if the user chooses
     * to do so after said session has ended
     * 
     * @param callback the callback to use. Can be null, if nothing should happen when
     * the user chooses to play back the session.
     */
    public void setPlayBackSessionCallback(PlayBackSessionCallback callback) {
        this.playBackSessionCallback = callback;
    }
    
    /**
     * Invokes the appropriate handlers when the user has specified that they want to
     * play back the given session
     * 
     * @param session the session to play back. Can be null.
     */
    private void onPlaybackSession(AbstractKnowledgeSession session) {
        if(this.playBackSessionCallback != null) {
            this.playBackSessionCallback.onPlayBackSession(session);
        }
    }
    
    @Override
    public void onSummativeAssessmentsChanged(Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment) {
        updateObjectiveAssessments();
    }

    @Override
    public void onDisplayModeChanged(AssessmentDisplayMode displayMode) {
        updateObjectiveAssessments();
    }
    
    /**
     * A callback used to invoke logic needed to play back a session
     * 
     * @author nroberts
     */
    public interface PlayBackSessionCallback{
        
        /**
         * Begins playing back the given session
         * 
         * @param session the session to play back. Can be null, if no session
         * should be played back
         */
        public void onPlayBackSession(AbstractKnowledgeSession session);
    }
}
