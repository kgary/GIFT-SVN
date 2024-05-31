/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util;

import static mil.arl.gift.common.util.StringUtils.join;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import generated.course.AuthoringSupportElements;
import generated.dkf.AGL;
import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Actions.StateTransitions;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Actions.StateTransitions.StateTransition.LogicalExpression;
import generated.dkf.Actions.StateTransitions.StateTransition.StrategyChoices;
import generated.dkf.Area;
import generated.dkf.AreaRef;
import generated.dkf.AssignedSectorCondition;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.Checkpoint;
import generated.dkf.CheckpointPaceCondition;
import generated.dkf.CheckpointProgressCondition;
import generated.dkf.ChildConceptEnded;
import generated.dkf.Concept;
import generated.dkf.ConceptAssessment;
import generated.dkf.ConceptEnded;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Coordinate;
import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.CorridorPostureCondition;
import generated.dkf.DetectObjectsCondition;
import generated.dkf.DetectObjectsCondition.ObjectsToDetect;
import generated.dkf.EliminateHostilesCondition;
import generated.dkf.EndTriggers;
import generated.dkf.EngageTargetsCondition;
import generated.dkf.EngageTargetsCondition.TargetsToEngage;
import generated.dkf.EnterAreaCondition;
import generated.dkf.EntityLocation;
import generated.dkf.EntityLocation.EntityId;
import generated.dkf.EntityLocation.EntityId.TeamMemberRef;
import generated.dkf.Entrance;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs;
import generated.dkf.EnvironmentAdaptation.Endurance;
import generated.dkf.EnvironmentAdaptation.FatigueRecovery;
import generated.dkf.EnvironmentAdaptation.HighlightObjects;
import generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs;
import generated.dkf.EnvironmentAdaptation.Teleport;
import generated.dkf.ExplosiveHazardSpotReportCondition;
import generated.dkf.FireTeamRateOfFireCondition;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.HaltConditionInput;
import generated.dkf.HealthConditionInput;
import generated.dkf.IdentifyPOIsCondition;
import generated.dkf.Input;
import generated.dkf.InstructionalIntervention;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.LearnerActionReference;
import generated.dkf.LearnerActionsList;
import generated.dkf.LearnerId;
import generated.dkf.LifeformTargetAccuracyCondition;
import generated.dkf.NegligentDischargeCondition;
import generated.dkf.NegligentDischargeCondition.TargetsToAvoid;
import generated.dkf.NineLineReportCondition;
import generated.dkf.ObservedAssessmentCondition;
import generated.dkf.PaceCountCondition;
import generated.dkf.Path;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceNode;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Point;
import generated.dkf.PointRef;
import generated.dkf.Pois;
import generated.dkf.RulesOfEngagementCondition;
import generated.dkf.Scenario;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.SpacingCondition;
import generated.dkf.SpacingCondition.SpacingPair;
import generated.dkf.SpeedLimitCondition;
import generated.dkf.SpotReportCondition;
import generated.dkf.StartLocation;
import generated.dkf.StartTriggers;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.StrategyRef;
import generated.dkf.Task;
import generated.dkf.TaskEnded;
import generated.dkf.Tasks;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import generated.dkf.TeamOrganization;
import generated.dkf.TeamRef;
import generated.dkf.TimerConditionInput;
import generated.dkf.UseRadioCondition;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.common.gwt.client.util.TeamsUtil;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.io.MapTileProperties;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility.ConceptNodeRef;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlacesOfInterestReferencesUpdatedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.TeamReferencesUpdatedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.TeamRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestConditionReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestStrategyReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.TeamReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.concept.ConceptPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.AssessmentRollupWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplDescription;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplDescriptionResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionsOverallAssessmentTypes;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionsOverallAssessmentTypesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchTrainingAppScenarioAdaptations;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchTrainingAppScenarioAdaptationsResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveys;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveysResult;

/**
 * Provides global utilities for the DKF editor. Should not be used outside of the
 * {@link mil.arl.gift.tools.authoring.server.gat.client.view.dkf dkf editor package}
 *
 * @author tflowers
 *
 */
public class ScenarioClientUtility {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioClientUtility.class.getName());

    /** The {@link Scenario} that is currently being edited by the DKF editor */
    private static Scenario scenario = null;

    /** Flag indicating if this DKF is being used for a playback log */
    private static boolean isPlayback = false;
    
    /** Flag indicating if this DKF is being used for remediation content*/
    private static boolean isRemediation = false;

    /**
     * The set of condition classes that require a running training application
     * to complete their assessment
     */
    private static final Set<String> conditionsClassesRequiringTrainingApp;
    static {
        conditionsClassesRequiringTrainingApp = new HashSet<>();
        conditionsClassesRequiringTrainingApp.add("domain.knowledge.condition.IdentifyPOIsCondition");
    }
    
    /**
     * The set of condition classes that require the TUI
     * to complete their assessment
     */
    private static final Set<String> conditionsClassesRequiringTutor;
    static {
        conditionsClassesRequiringTutor = new HashSet<>();
        conditionsClassesRequiringTutor.add("domain.knowledge.condition.SimpleSurveyAssessmentCondition");
    }

    /**
     * The type of training application for which the {@link Scenario} is being authored
     */
    private static TrainingApplicationEnum trainingAppType = null;

    /** The list of surveys retrieved from the course. */
    private static List<SurveyContextSurvey> courseSurveys = null;

    /** Dirty flag for the scenario editor. Will be set to true if anything is changed. */
    private static boolean dirty = false;

    /**
     * A map that associates a training app with a set of conditions used to assess actions in that
     * training app
     */
    private static Map<TrainingApplicationEnum, Set<ConditionInfo>> appToConditions = new HashMap<>();

    /**
     * mapping of training app enum to list of outstanding requests for that training app enum's
     * domain conditions information
     */
    private static Map<TrainingApplicationEnum, List<AsyncCallback<Set<ConditionInfo>>>> trainingAppConditionInfoCallbacksMap = new HashMap<TrainingApplicationEnum, List<AsyncCallback<Set<ConditionInfo>>>>();

    /**
     * a list of conditions information for conditions used to assess enumerated learner actions
     * Note: not all learner actions have condition classes (e.g. tutor me)
     */
    private static List<ConditionInfo> learnerActionConditions = new ArrayList<ConditionInfo>();

    /**
     * a list of outstanding requests for the conditions information for conditions used to assess
     * enumerated learner actions
     */
    private static List<AsyncCallback<List<ConditionInfo>>> learnerActionConditionsCallbacks = new ArrayList<AsyncCallback<List<ConditionInfo>>>();

    /**
     * A map that associates a condition class impl name (e.g.
     * domain.knowledge.condition.ApplicationCompletedCondition) to the information about that
     * condition.
     */
    private static Map<String, ConditionInfo> conditionImplToConditionInfo = new HashMap<>();
        
    /**
     * the list of condition classes that call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * A condition classes extends AbstractCondition.class.  Will be null until the server returns with the response.
     */
    private static Set<String> conditionsThatCanComplete = null;
    
    /**
     * list of callbacks waiting for the server to respond to a request to retrieve the information about
     * conditions that can complete.
     */
    private static List<AsyncCallback<Set<String>>> conditionsThatCanCompleteCallbacks = new ArrayList<>();
    
    /**
     * A map of overall assessment types to the condition classes can populate it its life cycle.<br/>
     * Key: condition class name without the src package prefix {@link PACKAGE_PATH} (e.g. domain.knowledge.condition.EliminateHostilesCondition)<br/>
     * Values: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime} class canonical name value 
     */
    private static Map<String, Set<String>> overallAssessmentTypesConditionsMap = new HashMap<>();
    
    /**
     * A map of training application enums to the supported scenario adaptations for that integrated training application.<br/>
     * Values: are generated classes getCanonicalName (e.g. generated.dkf.EnvironmentAdaptation.Fog).
     */
    private static Map<TrainingApplicationEnum, Set<String>> trainingAppScenarioAdaptationsMap = new HashMap<>();
    
    /**
     * collection of callback waiting for the server to provide this client with the overall assessment map.  
     * This is used so only one server request is made for this client instance.
     */
    private static List<AsyncCallback<Map<String, Set<String>>>> conditionsOverallAssessmentTypesCallbacks = new ArrayList<>();
    
    /**
     * collection of callbacks waiting for the server to provide this client with the training application scenario adaptations map.  
     * This is used so only one server request is made for this client instance.
     */
    private static List<AsyncCallback<Map<TrainingApplicationEnum, Set<String>>>> trainingAppScenarioAdaptationsCallbacks = new ArrayList<>();

    /**
     * Map that contains the condition impl and the callbacks that will be executed when the
     * condition info is fetched
     */
    private static Map<String, List<AsyncCallback<ConditionInfo>>> conditionImplConditionInfoCallbacksMap = new HashMap<String, List<AsyncCallback<ConditionInfo>>>();

    /** A mapping from each place of interest (e.g. point, path, area) to all of the conditions that reference it */
    private static HashMap<String, List<PlaceOfInterestReference>> placeOfInterestReferences = new HashMap<>();

    /** A mapping from each team or team member to all of the strategies that reference it */
    private static HashMap<String, List<TeamReference>> teamReferences = new HashMap<>();

    /** The last performance node ID that was generated. Used to increment performance node IDs so they remain unique. */
    private static BigInteger lastPerformanceNodeId = null;

    /** The interface for invoking RPCs */
    private static final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);

    /** Maintains the validity state of each scenario item */
    private static final ScenarioValidationCache validationCache = new ScenarioValidationCache();

    /** The formatter for numbers to limit to 2 decimal places. */
    private static final NumberFormat GCC_NUM_FORMAT = NumberFormat.getFormat("#.##");

    /** The formatter for numbers to limit to 6 decimal places. */
    private static final NumberFormat GDC_NUM_FORMAT = NumberFormat.getFormat("#.######");

    /** The formatter for numbers to limit to 2 decimal places. */
    private static final NumberFormat AGL_NUM_FORMAT = NumberFormat.getFormat("#.##");

    /** {@link Stringifier} for the GCC coordinate type. Used to pretty print the location. */
    private static final Stringifier<GCC> gccStringifier = new Stringifier<GCC>() {
        @Override
        public String stringify(GCC gcc) {
            String gccX = "X=" + GCC_NUM_FORMAT.format(gcc.getX());
            String gccY = "Y=" + GCC_NUM_FORMAT.format(gcc.getY());
            String gccZ = "Z=" + GCC_NUM_FORMAT.format(gcc.getZ());
            return "(GCC: " + join(", ", Arrays.asList(gccX, gccY, gccZ)) + ")";
        }
    };

    /** {@link Stringifier} for the GDC coordinate type. Used to pretty print the location. */
    private static final Stringifier<GDC> gdcStringifier = new Stringifier<GDC>() {
        @Override
        public String stringify(GDC gdc) {
            String gdcLon = "Lon=" + GDC_NUM_FORMAT.format(gdc.getLongitude());
            String gdcLat = "Lat=" + GDC_NUM_FORMAT.format(gdc.getLatitude());
            String gdcElev = "Elev=" + GDC_NUM_FORMAT.format(gdc.getElevation());
            return "(GDC: " + join(", ", Arrays.asList(gdcLon, gdcLat, gdcElev)) + ")";
        }
    };

    /** {@link Stringifier} for the AGL coordinate type. Used to pretty print the location. */
    private static final Stringifier<AGL> vbsaglStringifier = new Stringifier<AGL>() {
        @Override
        public String stringify(AGL agl) {
            String aglX = "X=" + AGL_NUM_FORMAT.format(agl.getX());
            String aglY = "Y=" + AGL_NUM_FORMAT.format(agl.getY());
            String aglElevation = "Elevation=" + AGL_NUM_FORMAT.format(agl.getElevation());
            return "(AGL: " + join(", ", Arrays.asList(aglX, aglY, aglElevation)) + ")";
        }
    };

    /**
     * Gets the {@link Scenario} that is currently being edited by the DKF editor.
     *
     * @return Returns the {@link Scenario} that is currently being edited. Will not be null after
     *         {@link DkfPresenter#loadDkf} is called
     */
    public static Scenario getScenario() {
        return scenario;
    }

    /**
     * Sets the {@link Scenario} that is currently being edited by the DKF editor. Should only be
     * called from {@link DkfPresenter#loadDkf}
     *
     * @param newScenario The value of the {@link Scenario} that is currently being edited by the
     *        DKF editor. Can not be null.
     * @throws UnsupportedOperationException if the scenario has already been set
     */
    public static void setScenario(Scenario newScenario) {
        if (newScenario == null) {
            throw new IllegalArgumentException("The parameter 'newScenario' can not be null");
        } else if (scenario != null) {
            throw new UnsupportedOperationException("The 'scenario' can't be set a second time");
        }

        scenario = newScenario;

        if(isLearnerIdRequiredByApplication()) {

            //determine if the scenario already has a team organization, and create one if it doesn't
            if(scenario.getTeamOrganization() == null) {
                scenario.setTeamOrganization(new TeamOrganization());

            }

            //determine if the scenario's team organization already has a root team, and create one if it doesn't
            if(scenario.getTeamOrganization().getTeam() == null) {
                scenario.getTeamOrganization().setTeam(new Team());
            }

            //determine if the team organization's root team already has a name, and create one if it doesn't
            if(scenario.getTeamOrganization().getTeam().getName() == null) {
                scenario.getTeamOrganization().getTeam().setName("All Learners");
            }

            //determine if the team organization has any team members, and create a new team member if it
            //doesn't but has a learner ID instead
            if(getAnyTeamMemberName() == null) {

                TeamMember teamMember = generateNewTeamMember();

                LearnerId id = new LearnerId();
                TrainingApplicationEnum ta = getTrainingAppType();
                
                if(TrainingApplicationEnum.VBS.equals(ta) || TrainingApplicationEnum.VR_ENGAGE.equals(ta)) {
                    id.setType("Enter the URN marking for a VBS entity");

                } else {
                    id.setType("Learner");
                }

                teamMember.setLearnerId(id);

                scenario.getTeamOrganization().getTeam().getTeamOrTeamMember().add(teamMember);
            }
        }
        
        //clear the last performance node ID since the new scenario will have new IDs
        lastPerformanceNodeId = null;
    }

    /**
     * Set the flag indicating if this DKF is being used for playback (defaults
     * to false).
     * 
     * @param newPlaybackValue true if the DKF is for playback; false otherwise.
     */
    public static void setIsPlayback(boolean newPlaybackValue) {
        isPlayback = newPlaybackValue;
    }

    /**
     * Flag indicating if this DKF is being used for playback (defaults to
     * false)
     * 
     * @return true if the DKF is for playback; false otherwise.
     */
    public static boolean isPlayback() {
        return isPlayback;
    }

    /**
     * Set the flag indicating if this DKF is being used for interactive remediation content (defaults
     * to false).
     * 
     * @param newRemediationValue true if the DKF is for remediation; false otherwise.
     */
    public static void setIsRemediation(boolean newRemediationValue) {
        isRemediation = newRemediationValue;
    }

    /**
     * Flag indicating if this DKF is being used for interactive remediation content (defaults to
     * false)
     * 
     * @return true if the DKF is for remediation; false otherwise.
     */
    public static boolean isRemediation() {
        return isRemediation;
    }
    
    /**
     * Checks if the provided {@link ConditionInfo} references a condition that
     * requires a running training application to complete its assessment.
     * 
     * @param conditionInfo the {@link ConditionInfo} to check. Can't be null.
     * @return true if the referenced condition requires a running training
     *         application to complete its assessment; false otherwise.
     */
    public static boolean doesConditionRequireTrainingApp(ConditionInfo conditionInfo) {
        if (conditionInfo == null) {
            throw new IllegalArgumentException("The parameter 'conditionInfo' cannot be null.");
        }

        return conditionsClassesRequiringTrainingApp.contains(conditionInfo.getConditionClass());
    }
    
    /**
     * Checks if the provided {@link ConditionInfo} references a condition that
     * requires the TUI to complete its assessment.
     * 
     * @param conditionInfo the {@link ConditionInfo} to check. Can't be null.
     * @return true if the referenced condition requires the TUI to complete its assessment; false otherwise.
     */
    public static boolean doesConditionRequireTutor(ConditionInfo conditionInfo) {
        if (conditionInfo == null) {
            throw new IllegalArgumentException("The parameter 'conditionInfo' cannot be null.");
        }
        
        return conditionsClassesRequiringTutor.contains(conditionInfo.getConditionClass());
    }

    /**
    * Returns the learner's start location coordinate.
    *
    * @return The {@link Coordinate} of the learner's start location or null if it doesn't exist.
    */
   public static Coordinate getLearnerStartLocationCoordinate() {
       TeamOrganization teamOrganization = getScenario().getTeamOrganization();
       if (teamOrganization == null) {
           return null;
       }

       TeamMember teamMember = getLearner(teamOrganization.getTeam());
       if (teamMember != null && teamMember.getLearnerId() != null) {
           Serializable learnerIdType = teamMember.getLearnerId().getType();

           if(learnerIdType != null && learnerIdType instanceof StartLocation){
               StartLocation startLocation = (StartLocation) learnerIdType;

               Coordinate coordinate = startLocation.getCoordinate();
               // if coordinate is null or not of an allowed type
               if (coordinate == null) {
                   return null;
               }

               return coordinate;
           }
       }

       return null;
   }

   /**
    * Recursively search through a {@link Team} for the first learner {@link TeamMember} encountered
    *
    * @param team the team to search through
    * @return the first {@link TeamMember} found
    */
    private static TeamMember getLearner(Team team) {
        TeamMember teamMember;
        if (team != null) {
            for (Serializable teamOrTeamMember : team.getTeamOrTeamMember()) {
                if (teamOrTeamMember instanceof TeamMember) {
                    return (TeamMember) teamOrTeamMember;
                } else if (teamOrTeamMember instanceof Team) {
                    teamMember = getLearner((Team) teamOrTeamMember);
                    if (teamMember != null && teamMember.getLearnerId() != null) {
                        return teamMember;
                    }
                }
            }
        }

        return null;
    }

/**

    /**
     * Retrieve the scenario validation cache.
     *
     * @return the cache of scenario elements' validity.
     */
    public static ScenarioValidationCache getValidationCache() {
        return validationCache;
    }

    /**
     * Retrieves the dirty flag for the DKF scenario editor.
     *
     * @return the dirty flag.True if anything has changed without saving.
     */
    public static boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the dirty flag for the DKF scenario editor to true. Should be called if anything in the
     * editor has changed without being saved.
     */
    public static void setDirty() {
        dirty = true;
    }

    /**
     * Sets the dirty flag for the DKF scenario editor to false. Should only be called on save.
     */
    public static void setClean() {
        dirty = false;
    }

    /**
     * Gets the type of training application for which the {@link Scenario} is being authored.
     *
     * @return The {@link TrainingApplicationEnum} for which the {@link Scenario} is being authored.
     *         Will not be null after
     *         {@link DkfPresenter#start(AcceptsOneWidget, java.util.HashMap)} is called.
     */
    public static TrainingApplicationEnum getTrainingAppType() {
        return trainingAppType;
    }
    
    /**
     * Return whether the currently set training application type supports displaying mid lesson media.
     * @return true by default (including any newly added training application types), false if the type
     * has not been set in this class, false if the type doesn't support mid-lesson media (probably because
     * it can't be paused while using the application).
     */
    public static boolean canTrainingAppUseMidLessonMedia(){
        
        if(trainingAppType == null){
            return false;
        }else if(trainingAppType.equals(TrainingApplicationEnum.HAVEN) || trainingAppType.equals(TrainingApplicationEnum.RIDE)){
            return false;
        }
        
        return true;        
    }
    
    /**
     * Return whether the currently set training application type supports displaying mid lesson survey.
     * @return true by default (including any newly added training application types), false if the type
     * has not been set in this class, false if the type doesn't support mid-lesson survey (probably because
     * it can't be paused while using the application).
     */
    public static boolean canTrainingAppUseMidLessonSurvey(){
        
        if(trainingAppType == null){
            return false;
        }else if(trainingAppType.equals(TrainingApplicationEnum.HAVEN) || trainingAppType.equals(TrainingApplicationEnum.RIDE)){
            return false;
        }
        
        return true;        
    }
    
    /**
     * Return whether the currently set training application type supports displaying mid lesson conversation.
     * @return true by default (including any newly added training application types), false if the type
     * has not been set in this class, false if the type doesn't support mid-lesson conversation (probably because
     * it can't be paused while using the application).
     */
    public static boolean canTrainingAppUseMidLessonConversation(){
        
        if(trainingAppType == null){
            return false;
        }else if(trainingAppType.equals(TrainingApplicationEnum.HAVEN) || trainingAppType.equals(TrainingApplicationEnum.RIDE)){
            return false;
        }
        
        return true;        
    }
    
    /**
     * Return whether the currently set training application type supports modifying the scenario.
     * @return true by default (including any newly added training application types), false if the type
     * has not been set in this class, false if the type doesn't support modifying the scenario.
     */
    public static boolean canTrainingAppUseModifyScenario(){
        
        if(trainingAppType == null){
            return false;
        }else if(trainingAppType == TrainingApplicationEnum.ARES || trainingAppType == TrainingApplicationEnum.MOBILE_DEVICE_EVENTS  
                || trainingAppType == TrainingApplicationEnum.POWERPOINT
                || trainingAppType == TrainingApplicationEnum.SIMPLE_EXAMPLE_TA
                || trainingAppType == TrainingApplicationEnum.SUDOKU
                || trainingAppType == TrainingApplicationEnum.TC3
                || trainingAppType == TrainingApplicationEnum.UNITY_DESKTOP
                || trainingAppType == TrainingApplicationEnum.UNITY_EMBEDDED){
            return false;
        }
        
        return true;        
    }
    
    /**
     * Return the coordinate types that are not allowed for the provided condition given
     * the known current training application type course object being authored.  E.g. VBS
     * produces messages with GCC coordinates, meaning condition classes would need to compare
     * locations encoded in GCC (or something that can be translated to GCC like GDC).
     * @param conditionClass a condition class to find the coordinate types that are not supported.
     * (e.g. generated.dkf.IdentifyPOIsCondition.class)
     * @return null can be returned, otherwise an array of coordinate type enumerations that
     * the condition doesn't support in a given training application.
     */
    public static CoordinateType[] getDisallowedCoordinateTypes(Class<?> conditionClass){
        
        CoordinateType[] GCC_ONLY = {CoordinateType.AGL, CoordinateType.GDC};
        CoordinateType[] AGL_ONLY = {CoordinateType.GCC, CoordinateType.GDC};
        
        TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
        if(conditionClass == null || taType == null){
            return null;
        }else if(conditionClass.equals(generated.dkf.AvoidLocationCondition.class)){
            // VBS integration supports GCC+AGL, but Avoid location condition only works with GCC, GDC
            return taType == TrainingApplicationEnum.VBS ? GCC_ONLY : null; 
        }else if(conditionClass.equals(generated.dkf.AssignedSectorCondition.class)){
            // VBS integration supports GCC+AGL, but assigned sector condition only works with GCC
            return taType == TrainingApplicationEnum.VBS ? GCC_ONLY : null;
        }else if(conditionClass.equals(generated.dkf.DetectObjectsCondition.class)){
            // VBS integration supports GCC+AGL, but detect objects condition only works with GCC
            return taType == TrainingApplicationEnum.VBS ? GCC_ONLY : null;
        }else if(conditionClass.equals(generated.dkf.EngageTargetsCondition.class)){
            // VBS integration supports GCC+AGL, but engage targets condition only works with GCC
            return taType == TrainingApplicationEnum.VBS ? GCC_ONLY : null;
        }else if(conditionClass.equals(generated.dkf.IdentifyPOIsCondition.class)){
            // VBS uses AGL coordinates for Line of sight calls for identify Pois condition
            return taType == TrainingApplicationEnum.VBS ? AGL_ONLY : null;
        }else if(conditionClass.equals(generated.dkf.NegligentDischargeCondition.class)){
            // VBS integration supports GCC+AGL, but engage targets condition only works with GCC
            return taType == TrainingApplicationEnum.VBS ? GCC_ONLY : null;
        }
        
        return null;
    }

    /**
     * Sets the type of training application for which the {@link Scenario} is being authored.
     * Should only be called from {@link DkfPresenter#start(AcceptsOneWidget, java.util.HashMap)}
     *
     * @param taEnum The type of training application. Can't be null.
     * @throws UnsupportedOperationException if the training application type has already been set
     */
    public static void setTrainingAppType(TrainingApplicationEnum taEnum) {
        if (taEnum == null) {
            throw new IllegalArgumentException("The parameter 'taEnum' cannot be null.");
        } else if (trainingAppType != null) {
            throw new UnsupportedOperationException("The trainingAppType can't be set more than once");
        }
        trainingAppType = taEnum;

        // pre load condition info cache for use later when building condition tree items and editor
        // panels
        /* If the training app is excluded (blocked from being selected or
         * shown), default it to the most generic training application possible
         * (Simple Example TA) so that the user is able to see something and can
         * then proceed in choosing whichever makes most sense. */
        getConditionsForTrainingApp(
                ScenarioClientUtility.isTrainingAppExcluded(trainingAppType) ? TrainingApplicationEnum.SIMPLE_EXAMPLE_TA
                        : trainingAppType,
                null);
    }

    /**
     * Checks if the training application should be excluded from loading.
     * 
     * @param taEnum the training application enum to check.
     * @return true if the training application should be excluded.
     */
    public static boolean isTrainingAppExcluded(TrainingApplicationEnum taEnum) {
        /* CUSTOM EXCLUSIONS */
        if (taEnum == TrainingApplicationEnum.SUDOKU) {
            /* Not fully integrated */
            return true;
        } else if (GatClientUtility.getServerProperties() != null
                && GatClientUtility.getServerProperties().getDeploymentMode() == DeploymentModeEnum.SERVER
                && taEnum == TrainingApplicationEnum.TC3) {
            /* Not usable in server mode, also currently doesn't support
             * multiple concurrent users */
            return true;
        } else if (GatClientUtility.isRtaLessonLevel() && (taEnum == TrainingApplicationEnum.UNITY_EMBEDDED
                || taEnum == TrainingApplicationEnum.MOBILE_DEVICE_EVENTS)) {
            /* not usable in RTA mode */
            return true;
        }

        return false;
    }

    /**
     * Checks if the given {@link Condition} is excluded.
     * 
     * @param condition the condition to check.
     * @return the reason for the condition being excluded, if any. Will return
     *         null if the condition is not excluded.
     */
    public static String isConditionExcluded(Condition condition) {
        if (condition == null) {
            return null;
        }

        if (ScenarioClientUtility.isJRE64Bit()) {
            if (StringUtils.equalsIgnoreCase(condition.getConditionImpl(),
                    "domain.knowledge.condition.simile.SIMILEInterfaceCondition")) {
                return "The SIMILE condition does not support a 64-bit JRE.";
            }
        }
        return null;
    }

    /**
     * Checks if the JRE is running in 64-bit.
     * https://docs.oracle.com/javame/config/cdc/cdc-opt-impl/ojmeec/1.1/architecture/html/properties.htm#g1001328
     * 
     * @return true if running in 64-bit; false otherwise.
     */
    public static boolean isJRE64Bit() {
        return GatClientUtility.isJRE64Bit();
    }

    /**
     * Gets the {@link AuthoringSupportElements} for this DKF's training
     * application if they are available.
     *
     * @return Returns the DKF's training application's
     *         {@link AuthoringSupportElements} if available. Otherwise returns
     *         null.
     */
    public static native AuthoringSupportElements getAuthoringSupportElements() /*-{
        var possibleSupportElements = $wnd.parent.authoringSupportElements;
        return possibleSupportElements || null;
    }-*/;

    /**
     * Gets the name of the Scenario Object from the Serializable object. In the case of Conditions,
     * please use getConditionInfoForConditionImpl call back to get the display name.
     *
     * @param scenarioObject the Serializable object to cast
     * @return the name of the Scenario Object
     * @throws IllegalArgumentException if the Serializable cannot be cast to a Scenario Object
     */
    public static String getScenarioObjectName(Serializable scenarioObject) throws IllegalArgumentException {
        return ScenarioElementUtil.getObjectName(scenarioObject);
    }

    /**
     * Sets the name of the Scenario Object from the Serializable object
     *
     * @param scenarioObject the Serializable object to cast
     * @param name the name to set the Scenario Object to
     * @throws UnsupportedOperationException if the Serializable cannot be cast to a Scenario Object
     */
    public static void setScenarioObjectName(Serializable scenarioObject, String name)
            throws UnsupportedOperationException {
        ScenarioElementUtil.setObjectName(scenarioObject, name);
    }

    /**
     * Searches the scenario for the Concept with the given node id.
     *
     * @param nodeId Node ID to look for. If null, will return null.
     * @return Concept with the given Node ID, null if no such concept exists.
     */
    public static Concept getConceptWithId(BigInteger nodeId) {
        if (nodeId == null) {
            return null;
        }

        for (Concept concept : getUnmodifiableConceptList()) {
            if (concept.getNodeId().equals(nodeId)) {
                return concept;
            }
        }
        return null;
    }

    /**
     * Searches the scenario for the Task with the given node id.
     *
     * @param nodeId Node ID to look for. If null, will return null.
     * @return Task with the given Node ID, null if no such task exists.
     */
    public static Task getTaskWithId(BigInteger nodeId) {
        if (nodeId == null) {
            return null;
        }

        for (Task task : getUnmodifiableTaskList()) {
            if (task.getNodeId().equals(nodeId)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Returns the {@link Task} or {@link Concept} that is referenced by the provided node id.
     *
     * @param nodeId the node id that is used to identify a specific {@link Task} or
     *        {@link Concept}.
     * @return the {@link Task} or {@link Concept} with the provided node id. Can be null if the
     *         node id isn't found.
     */
    public static Serializable getTaskOrConceptWithId(BigInteger nodeId) {
        if (nodeId == null) {
            return null;
        }

        Serializable taskOrConcept = ScenarioClientUtility.getTaskWithId(nodeId);
        if (taskOrConcept == null) {
            // if concept is not found, this will return null
            taskOrConcept = ScenarioClientUtility.getConceptWithId(nodeId);
        }

        return taskOrConcept;
    }

    /**
     * Returns the name of a {@link Task} or {@link Concept} that is provided.
     * 
     * @param taskOrConcept a {@link Task} or {@link Concept} to get the name value from.
     * @return the name of the task or concept from the object provided.  If the object is null,
     * null is returned.  A null or empty string can also be returned if that is the value of
     * the name attribute in the object provided.
     */
    public static String getTaskOrConceptName(Serializable taskOrConcept){
        
        if(taskOrConcept == null){
            return null;
        }else if(taskOrConcept instanceof Task){
            return ((Task)taskOrConcept).getName();
        }else if(taskOrConcept instanceof Concept){
            return ((Concept)taskOrConcept).getName();
        }else{
            return null;
        }
    }

    /**
     * Gets the {@link Tasks} object from the scenario.
     *
     * @return the {@link Tasks} from the scenario. If there is no {@link Tasks}, null is returned.
     */
    public static Tasks getTasks() {
        if (scenario == null || scenario.getAssessment() == null) {
            return null;
        }

        return scenario.getAssessment().getTasks();
    }

    /**
     * Gets the full list of scenario tasks in an unmodifiable list.
     *
     * @return all of the tasks within the scenario. An empty list is returned if there are no
     *         tasks.
     */
    public static List<Task> getUnmodifiableTaskList() {
        Tasks tasks = getTasks();
        if (tasks == null) {
            return Collections.unmodifiableList(new ArrayList<Task>());
        }

        return Collections.unmodifiableList(tasks.getTask());
    }

    /**
     * Gets the full list of scenario concepts in an unmodifiable list. This
     * will include all subconcepts as well.
     *
     * @return all of the concepts within the scenario. An empty list is returned if there are no
     *         concepts.
     */
    public static List<Concept> getUnmodifiableConceptList() {
        List<Concept> conceptList = new ArrayList<Concept>();
        for (Task task : getUnmodifiableTaskList()) {
            if (task.getConcepts() != null) {
                for (Concept concept : task.getConcepts().getConcept()) {
                    conceptList.add(concept);
                    conceptList.addAll(getSubConcepts(concept));
                }
            }
        }

        return Collections.unmodifiableList(conceptList);
    }
    
    /**
     * Gets all the subconcepts under the given concept
     *
     * @param concept the concept under which to get subconcepts
     * @return the list of subconcepts
     */
    public static List<Concept> getSubConcepts(Concept concept) {

        List<Concept> subConcepts = new ArrayList<Concept>();

        if (concept.getConditionsOrConcepts() instanceof Concepts) {

            for (Concept subConcept : ((Concepts) concept.getConditionsOrConcepts()).getConcept()) {
                subConcepts.add(subConcept);
                subConcepts.addAll(getSubConcepts(subConcept));
            }
        }

        return subConcepts;
    }

    /**
     * Builds a map of each {@link Task} and {@link Concept}'s node id to name
     *
     * @return a complete map of {@link Task} and {@link Concept} node ids to names
     */
    public static Map<BigInteger, String> getUnmodifiableNodeIdToNameMap() {
        Map<BigInteger, String> nodeIdToNameMap = new HashMap<>();

        for (Task task : getUnmodifiableTaskList()) {
            nodeIdToNameMap.put(task.getNodeId(), task.getName());
        }

        for (Concept concept : getUnmodifiableConceptList()) {
            nodeIdToNameMap.put(concept.getNodeId(), concept.getName());
        }

        return nodeIdToNameMap;
    }

    /**
     * Gets the full list of scenario conditions in an unmodifiable list.
     *
     * @return all of the conditions within the scenario. An empty list is returned if there are no
     *         conditions.
     */
    public static List<Condition> getUnmodifiableConditionList() {
        List<Condition> conditionList = new ArrayList<Condition>();
        for (Concept concept : getUnmodifiableConceptList()) {
            if (concept.getConditionsOrConcepts() instanceof Conditions) {
                Conditions conditions = (Conditions) concept.getConditionsOrConcepts();
                conditionList.addAll(conditions.getCondition());
            }
        }

        return Collections.unmodifiableList(conditionList);
    }

    /**
     * Gets the {@link InstructionalStrategies} object from the scenario.
     *
     * @return the {@link InstructionalStrategies} from the scenario. If there is no
     *         {@link InstructionalStrategies}, null is returned.
     */
    public static InstructionalStrategies getStrategies() {
        if (scenario == null || scenario.getActions() == null) {
            return null;
        }

        return scenario.getActions().getInstructionalStrategies();
    }

    /**
     * Gets the full list of scenario strategies in an unmodifiable list.
     *
     * @return all of the strategies within the scenario. An empty list is returned if there are no
     *         strategies.
     */
    public static List<Strategy> getUnmodifiableStrategyList() {
        InstructionalStrategies strategies = getStrategies();
        if (strategies == null) {
            return Collections.unmodifiableList(new ArrayList<Strategy>());
        }

        return Collections.unmodifiableList(strategies.getStrategy());
    }

    /**
     * Gets the {@link StateTransitions} object from the scenario.
     *
     * @return the {@link StateTransitions} from the scenario. If there is no
     *         {@link StateTransitions}, null is returned.
     */
    public static StateTransitions getStateTransitions() {
        if (scenario == null || scenario.getActions() == null) {
            return null;
        }

        return scenario.getActions().getStateTransitions();
    }

    /**
     * Gets the full list of scenario state transitions in an unmodifiable list.
     *
     * @return all of the state transitions within the scenario. An empty list is returned if there
     *         are no state transitions.
     */
    public static List<StateTransition> getUnmodifiableStateTransitionList() {
        StateTransitions stateTransitions = getStateTransitions();
        if (stateTransitions == null) {
            return Collections.unmodifiableList(new ArrayList<StateTransition>());
        }

        return Collections.unmodifiableList(stateTransitions.getStateTransition());
    }

    /**
     * Gets the {@link PlacesOfInterest} object from the scenario.
     *
     * @return the {@link PlacesOfInterest} from the scenario. If there is no {@link PlacesOfInterest}, null is
     *         returned.
     */
    public static PlacesOfInterest getPlacesOfInterest() {
        if (scenario == null || scenario.getAssessment() == null || scenario.getAssessment().getObjects() == null) {
            return null;
        }

        return scenario.getAssessment().getObjects().getPlacesOfInterest();
    }

    /**
     * Gets the full list of scenario places of interest in an unmodifiable list.
     *
     * @return all of the places of interest within the scenario. An empty list is returned if there are no
     *         places of interest.
     */
    public static List<Serializable> getUnmodifiablePlacesOfInterestList() {
        if (getPlacesOfInterest() == null) {
            return Collections.unmodifiableList(new ArrayList<Serializable>());
        }

        return Collections.unmodifiableList(getPlacesOfInterest().getPointOrPathOrArea());
    }

    /**
     * Gets the {@link AvailableLearnerActions} within the {@link Scenario} if
     * it exists.
     *
     * @return The {@link AvailableLearnerActions} within the {@link Scenario}.
     *         If it does not exist, null is returned.
     */
    public static AvailableLearnerActions getAvailableLearnerActions() {
        if (scenario == null || scenario.getResources() == null) {
            return null;
        }

        return scenario.getResources().getAvailableLearnerActions();
    }

    /**
     * Gets the {@link LearnerActionsList} object from the {@link Scenario}.
     *
     * @return The {@link LearnerActionsList} from the {@link Scenario}. If
     *         there is no {@link LearnerActionsList}, null is returned.
     */
    public static LearnerActionsList getLearnerActions() {
        final AvailableLearnerActions availableLearnerActions = getAvailableLearnerActions();
        if (availableLearnerActions == null) {
            return null;
        }

        return availableLearnerActions.getLearnerActionsList();
    }

    /**
     * Gets the full list of {@link Scenario} learner actions in an unmodifiable
     * list.
     *
     * @return the {@link List} of learner actions within the {@link Scenario}.
     *         An empty {@link List} is returned if there are no learner
     *         actions.
     */
    public static List<LearnerAction> getUnmodifiableLearnerActionList() {
        LearnerActionsList list = getLearnerActions();
        if (list == null) {
            return Collections.unmodifiableList(new ArrayList<LearnerAction>());
        }

        return Collections.unmodifiableList(list.getLearnerAction());
    }

    /**
     * Searches the scenario for the Strategy with the given name.
     *
     * @param strategyName strategy name to look for.
     * @return Strategy with the given name, null if no such strategy exists.
     */
    public static Strategy getStrategyWithName(String strategyName) {
        for (Strategy strategy : getUnmodifiableStrategyList()) {
            if (StringUtils.equals(strategy.getName(), strategyName)) {
                return strategy;
            }
        }
        return null;
    }
    
    
    /**
     * Gets the list of available strategy names..
     *
     * @return the list of strategy names that are available. Cannot be null.
     */
    public static List<String> getAvailableStrategyNames() {

        List<String> availableStrategies = new ArrayList<>();

        InstructionalStrategies strategies = ScenarioClientUtility.getStrategies();

        if (strategies != null) {
            for (Strategy strategy : strategies.getStrategy()) {
                String strategyName = strategy.getName();
                if (StringUtils.isNotBlank(strategyName) && !availableStrategies.contains(strategyName)) {
                    // only add strategy names that are not blank and have not already been added
                    availableStrategies.add(strategyName);
                }
            }

        }

        return availableStrategies;
    }

    /**
     * Finds a place of interest in the {@link Scenario} with the provided name.
     *
     * @param name The name of the place of interest to find.
     * @return The place of interest with the provided name if it exists, otherwise null.
     * {@link Path}, {@link Area}, {@link Point}
     */
    public static Serializable getPlaceOfInterestWithName(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        
        for (Serializable poi : getUnmodifiablePlacesOfInterestList()) {
            if (poi instanceof Point && ((Point)poi).getName().equalsIgnoreCase(name)) {
                return poi;
            }else if (poi instanceof Path && ((Path)poi).getName() != null && ((Path)poi).getName().equalsIgnoreCase(name)) {
                return poi;
            }else if (poi instanceof Area && ((Area)poi).getName().equalsIgnoreCase(name)) {
                return poi;
            }
        }

        return null;
    }

    /**
     * Searches the global team organization for teams and team members with the given names and returns the top-most
     * names that are found. If one of the provided names is used by a parent team, then the names of that parent team's
     * children will be removed from the returned list so that only the highest name in the organization is kept. Provided
     * names that are not used by any teams and team members will also be removed from the returned list.
     * <br/><br/>
     * Note that this method will simply return null if the current training application does not use a team organization
     * (i.e. does not require learner IDs).
     *
     * @param names the team and team member names to look for
     * @return the names of the top-most teams and team members that were found, or null if the current training application
     * does not use a team organization
     */
    public static List<String> getTopMostTeamNames(List<String> names) {

        if(getTeamOrganization() != null) {
            return getTopMostTeamNames(names != null ? new ArrayList<>(names) : null, getTeamOrganization().getTeam());

        } else {
            return null;
        }
    }

    /**
     * Searches the given team for teams and team members with the given names and returns the top-most
     * names that are found. If one of the provided names is used by a parent team, then the names of that parent team's
     * children will be removed from the returned list so that only the highest name in the organization is kept. If the
     * names of all the team members within a parent team are part of the provided list, then only the name of the parent
     * team will be returned. Provided names that are not used by any teams and team members will also be removed from
     * the returned list.
     *
     * This method essentially returns the smallest possible list of team and team member names in which the given list
     * of team and team member names can be found
     *
     * @param names the team and team member names to look for
     * @param team the team within which to look for names
     * @return the names of the top-most teams and team members that were found
     */
    private static List<String> getTopMostTeamNames(List<String> names, Team team) {
        return TeamsUtil.getTopMostTeamNames(names, team);
    }

    /**
     * Finds a {@link LearnerAction} in the {@link Scenario} with the provided
     * name.
     *
     * @param learnerActionName The name of the {@link LearnerAction} to find.
     * @return The {@link LearnerAction} with the provided name if it exists,
     *         otherwise null.
     */
    public static LearnerAction getLearnerActionWithName(String learnerActionName) {
        for (LearnerAction action : getUnmodifiableLearnerActionList()) {
            if (StringUtils.equals(action.getDisplayName(), learnerActionName)) {
                return action;
            }
        }

        return null;
    }

    /**
     * Identifies all of the {@link StateTransition StateTransitions} that directly reference the
     * node id.
     *
     * @param nodeId the node id of a {@link Task} or {@link Concept} that is potentially referenced
     *        by the {@link StateTransition}.
     * @return Every StateTransition that directly references the node id.
     */
    public static HashSet<StateTransition> getStateTransitionsThatReferenceNodeId(BigInteger nodeId) {
        HashSet<StateTransition> referencers = new HashSet<StateTransition>();

        // exit early if null
        if (nodeId == null) {
            return referencers;
        }

        // Find transitions that reference the provided node id
        for (StateTransition transition : getUnmodifiableStateTransitionList()) {
            for (Serializable stateType : transition.getLogicalExpression().getStateType()) {
                if (stateType instanceof PerformanceNode) {
                    PerformanceNode performanceNode = (PerformanceNode) stateType;
                    if (performanceNode.getNodeId().equals(nodeId)) {
                        referencers.add(transition);
                    }
                }
            }
        }

        return referencers;
    }
    
    /**
     * Identifies all of the LearnerActions that reference the strategy.
     * 
     * @param strategyName the name of an instructional strategy to get the learner actions that reference it
     * @return every learner action that references the strategy.  Can be empty but not null.
     */
    public static HashSet<LearnerAction> getLearnerActionsThatReferenceStrategy(String strategyName){
        HashSet<LearnerAction> learnerActionsThatReferenceStrategy = new HashSet<LearnerAction>();

        // exit early if blank
        if (StringUtils.isBlank(strategyName)) {
            return learnerActionsThatReferenceStrategy;
        }
        
        for(LearnerAction learnerAction : getUnmodifiableLearnerActionList()){
            if(LearnerActionEnumType.APPLY_STRATEGY.equals(learnerAction.getType())){
                Serializable actionParams = learnerAction.getLearnerActionParams();
                if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                    generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                    if(StringUtils.equals(strategyName, strategyRef.getName())){
                        learnerActionsThatReferenceStrategy.add(learnerAction);
                    }
                }
            }
        }
        
        return learnerActionsThatReferenceStrategy;

    }

    /**
     * Identifies all of the StateTransitions that reference the strategy.
     *
     * @param strategyName Strategy name potentially referenced by StateTransitions.
     * @return Every StateTransition that references the strategy.  Can be empty but not null.
     */
    public static HashSet<StateTransition> getStateTransitionsThatReferenceStrategy(String strategyName) {
        HashSet<StateTransition> stateTransitionsThatReferenceStrategy = new HashSet<StateTransition>();

        // exit early if blank
        if (StringUtils.isBlank(strategyName)) {
            return stateTransitionsThatReferenceStrategy;
        }

        // Find all state transitions that reference the provided strategy
        for (StateTransition transition : getUnmodifiableStateTransitionList()) {
            for (StrategyRef strategyRef : transition.getStrategyChoices().getStrategies()) {
                if (StringUtils.equals(strategyRef.getName(), strategyName)) {
                        stateTransitionsThatReferenceStrategy.add(transition);
                        break;
                    }
                }
            }

        return stateTransitionsThatReferenceStrategy;
    }
    
    /**
     * Return whether the provided trigger type contains a reference to the strategy.
     * 
     * @param triggerType a trigger to analyze for a reference to the strategy.
     * @param strategyName  the strategy to check for in the trigger.  Returns false if this is null or empty.
     * @return true if the trigger type is {@link StrategyApplied} and contains the strategy name.
     */
    public static boolean doesTriggerTypeContainStrategy(Serializable triggerType, String strategyName){
        
        if(StringUtils.isBlank(strategyName)){
            return false;
        }
        
        if(triggerType instanceof StrategyApplied){
            StrategyApplied sApplied = (StrategyApplied)triggerType;
            return strategyName.equalsIgnoreCase(sApplied.getStrategyName());
        }else{
            return false;
        }
    }
    
    /**
     * Return all {@link Task} found that reference the strategy in task start/end triggers. 
     * @param strategyName the strategy name to find Task triggers that reference it
     * @return contains all the Tasks {@link Task} that have start/end triggers 
     * reference the strategy.  Won't be null but can be empty.
     */
    public static HashSet<Task> getTasksThatReferenceStrategy(String strategyName){
        HashSet<Task> tasksThatReferenceStrategy = new HashSet<Task>();
        
        // exit early if blank
        if (StringUtils.isBlank(strategyName)) {
            return tasksThatReferenceStrategy;
        }
        
        for (Task otherTask : getUnmodifiableTaskList()) {

            boolean added = false;
            if (otherTask.getStartTriggers() != null) {
                for (StartTriggers.Trigger trigger : otherTask.getStartTriggers().getTrigger()) {
                    Serializable triggerType = trigger.getTriggerType();
                    if (doesTriggerTypeContainStrategy(triggerType, strategyName)) {
                        tasksThatReferenceStrategy.add(otherTask);
                        added = true;
                        break;
                    }
                }
            }

            if (!added && otherTask.getEndTriggers() != null) {
                for (EndTriggers.Trigger trigger : otherTask.getEndTriggers().getTrigger()) {
                    Serializable triggerType = trigger.getTriggerType();
                    if (doesTriggerTypeContainStrategy(triggerType, strategyName)) {
                        tasksThatReferenceStrategy.add(otherTask);
                        break;
                    }
                }
            }

        }
        
        return tasksThatReferenceStrategy;
    }

    /**
     * Identifies every {@link Strategy} that references the given node id.
     *
     * @param referencedNodeId the node id of a {@link Task} or {@link Concept} that is potentially
     *        referenced by the {@link Strategy}.
     * @return the set of {@link Strategy strategies} that reference the given node id directly.
     */
    public static HashSet<Strategy> getStrategiesThatReferenceNodeId(BigInteger referencedNodeId) {
        HashSet<Strategy> referencers = new HashSet<Strategy>();

        // exit early if null
        if (referencedNodeId == null) {
            return referencers;
        }

        // Find strategies that reference the task
        for (Strategy strategy : getUnmodifiableStrategyList()) {
            for (Serializable activity : strategy.getStrategyActivities()) {
                if (activity instanceof PerformanceAssessment) {
                    PerformanceAssessment assessment = (PerformanceAssessment) activity;
                if (assessment.getAssessmentType() instanceof PerformanceAssessment.PerformanceNode) {
                    PerformanceAssessment.PerformanceNode node = (PerformanceAssessment.PerformanceNode) assessment
                            .getAssessmentType();
                    if (referencedNodeId.equals(node.getNodeId())) {
                        referencers.add(strategy);
                    }
                }
            }
        }
        }

        return referencers;
    }

    /**
     * Specifies whether or not the DKF editor is in read-only mode.
     *
     * @return True indicates that the DKF editor is in read-only mode and therefore, no editing
     *         should be allowed on any of the widgets. False indicates that the DKF editor is not
     *         in read-only mode and therefore normal editing should be allowed through the widgets.
     */
    public static native boolean isReadOnly() /*-{
        var currentWnd = $wnd;

        while (currentWnd != null && currentWnd != currentWnd.parent) {
            if (currentWnd.readOnly != null) {
                return currentWnd.readOnly;
            } else {
                currentWnd = currentWnd.parent;
            }
        }

        return false;
    }-*/;

    /**
     * Sets whether or not the DKF editor is in read-only mode.
     *
     * @param readOnlyValue True if the DKF editor is in read-only mode and should not allow
     *        editing. False if the DKF editor is not in readonly mode and should allow editing.
     */
    public static native void setReadOnly(boolean readOnlyValue) /*-{
        $wnd.readOnly = readOnlyValue;
    }-*/;

    /**
     * Generates a {@link Task} with a unique name and node id. The generated {@link Task} is
     * <b>NOT</b> inserted into the {@link Scenario}.  The {@link Task} will contain a single {@link Concept}
     * created from {@link #generateNewConcept()}
     *
     * @return the generated {@link Task}.
     */
    public static Task generateNewTask() {
        Task task = new Task();
        task.setName(generateNewTaskName());
        task.setNodeId(generateTaskOrConceptId());
        task.setConcepts(new Concepts());
        
        Concept concept = generateNewConcept();
        task.getConcepts().getConcept().add(concept);

        return task;
    }

    /** New {@link Task} name prefix */
    private final static String NEW_TASK_PREFIX = "New Task ";

    /**
     * Generates a unique {@link Task} name.
     *
     * @return a unique name for {@link Task tasks}.
     */
    private static String generateNewTaskName() {
        int index = 1;
        String newName;
        do {
            newName = NEW_TASK_PREFIX + index++;
        } while (!isTaskOrConceptNameValid(newName));

        return newName;
    }

    /**
     * Generates a {@link Concept} with a unique name and node id. The generated {@link Concept} is
     * <b>NOT</b> inserted into the {@link Scenario}.
     *
     * @return the generated {@link Concept}.
     */
    public static Concept generateNewConcept() {
        Concept concept = new Concept();
        concept.setName(generateNewConceptName());
        concept.setNodeId(generateTaskOrConceptId());
        return concept;
    }

    /** New {@link Concept} name prefix */
    private final static String NEW_CONCEPT_PREFIX = "New Concept ";

    /**
     * Generates a unique {@link Concept} name.
     *
     * @return a unique name for {@link Concept concepts}.
     */
    private static String generateNewConceptName() {
        int index = 1;
        String newName;
        do {
            newName = NEW_CONCEPT_PREFIX + index++;
        } while (!isTaskOrConceptNameValid(newName));

        return newName;
    }

    /**
     * Generates a {@link Strategy} with a unique name. The generated {@link Strategy} is <b>NOT</b>
     * inserted into the {@link Scenario}.
     *
     * @return the generated {@link Strategy}.
     */
    public static Strategy generateNewStrategy() {
        Strategy strategy = new Strategy();
        strategy.setName(generateNewStrategyName());
        return strategy;
    }

    /** New {@link Strategy} name prefix */
    private final static String NEW_STRATEGY_PREFIX = "New Strategy ";

    /**
     * Generates a unique {@link Strategy} name.
     *
     * @return a unique name for {@link Strategy strategies}.
     */
    private static String generateNewStrategyName() {
        int index = 1;
        String newName;
        do {
            newName = NEW_STRATEGY_PREFIX + index++;
        } while (!isStrategyNameValid(newName));

        return newName;
    }

    /**
     * Generates a {@link StateTransition} with a unique name. The generated {@link StateTransition}
     * is <b>NOT</b> inserted into the {@link Scenario}.
     *
     * @return the generated {@link StateTransition}.
     */
    public static StateTransition generateNewStateTransition() {
        StateTransition stateTransition = new StateTransition();
        stateTransition.setName(generateNewStateTransitionName());
        stateTransition.setLogicalExpression(new LogicalExpression());
        stateTransition.setStrategyChoices(new StrategyChoices());
        return stateTransition;
    }

    /** New {@link StateTransition} name prefix */
    private final static String NEW_STATE_TRANSITION_PREFIX = "New State Transition ";

    /**
     * Generates a unique {@link StateTransition} name.
     *
     * @return a unique name for {@link StateTransition state transitions}.
     */
    private static String generateNewStateTransitionName() {
        int index = 1;
        String newName;
        do {
            newName = NEW_STATE_TRANSITION_PREFIX + index++;
        } while (!isStateTransitionNameValid(newName));

        return newName;
    }

    /**
     * Generates a unique ID that can be applied to a task or concept. The algorithm for doing so is
     * to identify the largest task or concept ID in the given scenario and increment by one
     *
     * @return A unique ID that can be applied to a task or concept.
     */
    public static BigInteger generateTaskOrConceptId() {

        if(lastPerformanceNodeId == null) {
            
            //since we haven't generated any IDs yet, get the largest ID from the scenario
        BigInteger largestIdFound = BigInteger.valueOf(0);
        for (Task task : getUnmodifiableTaskList()) {
            // if task node id is bigger, set largest node id to task node id
            if (task.getNodeId().compareTo(largestIdFound) == 1) {
                largestIdFound = task.getNodeId();
            }

            List<Concept> concepts = task.getConcepts().getConcept();
            for (Concept concept : concepts) {
                BigInteger largestConceptId = getLargestId(concept);

                // if concept node id is bigger, set largest node id to concept node id
                if (largestConceptId.compareTo(largestIdFound) == 1) {
                    largestIdFound = largestConceptId;
                }
            }
        }

            lastPerformanceNodeId = largestIdFound;
        }
        
        // add one to the largest node id found to make it unique
        lastPerformanceNodeId = lastPerformanceNodeId.add(BigInteger.ONE);

        return lastPerformanceNodeId;
    }

    /**
     * Examines the Node IDs of the given concept and any concepts nested within it. Whatever ID is
     * the largest is returned.
     *
     * @param concept Concept who should be examined to find the largest ID.
     * @return If concept is null 0 is returned. If concept doesn't have a child concept then the
     *         concept's id is returned. If the concept has a concept child then we return the
     *         largest ID within that nesting.
     */
    private static BigInteger getLargestId(Concept concept) {
        if (concept == null) {
            return BigInteger.valueOf(0);
        }

        BigInteger maxId = concept.getNodeId();

        for (Concept childConcept : getSubConcepts(concept)) {
            BigInteger childMaxId = getLargestId(childConcept);

            if (childMaxId.compareTo(maxId) == 1) {
                maxId = childMaxId;
            }
        }

        return maxId;
    }
    
    /**
     * Examines the Node IDs of the given task and any concepts nested within it. Whatever ID is
     * the largest is returned.
     *
     * @param task Task who should be examined to find the largest ID.
     * @return If task is null 0 is returned. If task doesn't have a child concept then the
     *         task's id is returned. If the task has a concept child then we return the
     *         largest ID within that nesting.
     */
    private static BigInteger getLargestId(Task task) {
        
        if (task == null) {
            return BigInteger.valueOf(0);
        }
        
        BigInteger maxId = task.getNodeId();

        List<Concept> concepts = task.getConcepts().getConcept();
        for (Concept concept : concepts) {
            BigInteger largestConceptId = getLargestId(concept);

            // if concept node id is bigger, set largest node id to concept node id
            if (largestConceptId.compareTo(maxId) == 1) {
                maxId = largestConceptId;
            }
        }

        return maxId;
    }

    /**
     * Generates a {@link Team} with a unique name. The generated {@link Team} is
     * <b>NOT</b> inserted into the {@link Scenario}.
     *
     * @return the generated {@link Team}.
     */
    public static Team generateNewTeam() {
        Team team = new Team();
        team.setName(generateNewTeamName());
        return team;
    }

    /** New {@link Team} name prefix */
    private final static String NEW_TEAM_PREFIX = "Team ";

    /**
     * Generates a unique {@link Team} name.
     *
     * @return a unique name for a {@link Team team}.
     */
    private static String generateNewTeamName() {
        int index = 1;
        String newName;
        do {
            newName = NEW_TEAM_PREFIX + index++;
        } while (!isTeamOrMemberNameValid(newName));

        return newName;
    }

    /** New {@link TeamMember} name prefix */
    private final static String NEW_TEAM_MEMBER_PREFIX = "Learner ";

    /**
     * Generates a {@link TeamMember} with a unique name. The generated {@link TeamMember} is
     * <b>NOT</b> inserted into the {@link Scenario}.
     *
     * @return the generated {@link TeamMember}.
     */
    public static TeamMember generateNewTeamMember() {
        TeamMember team = new TeamMember();
        team.setName(generateNewTeamMemberName());
        return team;
    }

    /**
     * Generates a unique {@link TeamMember} name.
     *
     * @return a unique name for a {@link TeamMember team member}.
     */
    private static String generateNewTeamMemberName() {
        int index = 1;
        String newName;
        do {
            newName = NEW_TEAM_MEMBER_PREFIX + index++;
        } while (!isTeamOrMemberNameValid(newName));

        return newName;
    }

    /**
     * Gets whether or not adding a scenario object with the specified name will cause a naming
     * conflict
     *
     * @param scenarioObject the Serializable object to cast
     * @param name the name to check
     * @return whether or not a name conflict will occur
     * @throws UnsupportedOperationException if the Serializable cannot be cast to a Scenario Object
     */
    public static boolean isScenarioObjectNameValid(Serializable scenarioObject, String name)
            throws UnsupportedOperationException {

        if (scenarioObject instanceof Task || scenarioObject instanceof Concept) {
            return isTaskOrConceptNameValid(name);
        } else if (scenarioObject instanceof StateTransition) {
            return isStateTransitionNameValid(name);
        } else if (scenarioObject instanceof Strategy) {
            return isStrategyNameValid(name);
        } else if (scenarioObject instanceof Team || scenarioObject instanceof TeamMember) {
                return isTeamOrMemberNameValid(name);
        } else {
            throw new UnsupportedOperationException(
                    "Failed to determine if scenario object: " + scenarioObject + " name was valid. Unknown Type.");
        }

    }

    /**
     * Gets whether or not adding a task or concept with the specified name will cause a naming
     * conflict
     *
     * @param name the name to check
     * @return whether or not a name conflict will occur
     */
    public static boolean isTaskOrConceptNameValid(String name) {

        if (StringUtils.isBlank(name)) {
            return false;
        }

        for (Task task : getUnmodifiableTaskList()) {
            if (StringUtils.equals(task.getName(), name)) {
                return false;
            }

            if (task.getConcepts() != null) {
                for (Concept concept : task.getConcepts().getConcept()) {
                    if (!isConceptNameValid(name, concept)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Gets whether or not adding a concept with the specified name will cause a naming conflict
     *
     * @param name the name to check
     * @param concept the concept in which to check for conflicts
     * @return whether or not a name conflict will occur
     */
    private static boolean isConceptNameValid(String name, Concept concept) {

        if (StringUtils.isBlank(name)) {
            return false;
        } else if (concept == null) {
            return true;
        }

        if (StringUtils.equals(name, concept.getName())) {
            return false;
        } else if (concept.getConditionsOrConcepts() instanceof Concepts) {
            Concepts concepts = (Concepts) concept.getConditionsOrConcepts();
            for (Concept subConcept : concepts.getConcept()) {
                if (!isConceptNameValid(name, subConcept)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Gets whether or not adding a team or team member with the specified name will cause a naming
     * conflict
     *
     * @param name the name to check
     * @return whether or not a name conflict will occur
     */
    public static boolean isTeamOrMemberNameValid(String name) {

        if (StringUtils.isBlank(name)) {
            return false;
        }

        if(scenario.getTeamOrganization() != null
                && !isTeamOrMemberNameValid(name, scenario.getTeamOrganization().getTeam())) {

            return false;
        }

        return true;
    }

    /**
     * Gets whether or not adding a team or team member with the specified name will cause a naming conflict
     *
     * @param name the name to check
     * @param team the team in which to check for conflicts
     * @return whether or not a name conflict will occur
     */
    private static boolean isTeamOrMemberNameValid(String name, Team team) {

        if (StringUtils.isBlank(name)) {
            return false;
        } else if (team == null) {
            return true;
        }

        if (StringUtils.equals(name, team.getName())) {
            return false;

        } else {

            List<Serializable> units = team.getTeamOrTeamMember();
            for (Serializable unit : units) {

                if(unit instanceof TeamMember) {

                    if (StringUtils.equals(name, ((TeamMember) unit).getName())) {
                        return false;
                    }

                } else if(unit instanceof Team) {

                    if (!isTeamOrMemberNameValid(name, (Team) unit)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Gets whether or not adding a state transition with the specified name will cause a naming
     * conflict
     *
     * @param name the name to check
     * @return whether or not a name conflict will occur
     */
    public static boolean isStateTransitionNameValid(String name) {

        if (StringUtils.isBlank(name)) {
            return false;
        }

        for (StateTransition transition : getUnmodifiableStateTransitionList()) {
            if (StringUtils.equals(transition.getName(), name)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets whether or not adding a strategy with the specified name will cause a naming conflict
     *
     * @param name the name to check
     * @return whether or not a name conflict will occur
     */
    public static boolean isStrategyNameValid(String name) {

        if (StringUtils.isBlank(name)) {
            return false;
        }

        for (Strategy strategy : getUnmodifiableStrategyList()) {
            if (StringUtils.equals(strategy.getName(), name)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns each of the {@link StateTransition state transitions}, {@link Task}, {@link Scenario.EndTriggers},
     * and {@link LearnerAction} that reference the provided {@link Strategy}.
     *
     * @param strategy The {@link Strategy} for which to find references. Can't be null.
     * @return An {@link Iterable} collection containing each of the {@link StateTransition state
     *         transitions}, {@link Task}, {@link Scenario.EndTriggers}, and {@link LearnerAction} that 
     *         reference the provided {@link Strategy}. Can't be null.
     */
    public static Iterable<Serializable> getReferencesTo(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        // Find all state transitions that reference the provided strategy
        HashSet<StateTransition> stateTransitions = getStateTransitionsThatReferenceStrategy(strategy.getName());
        
        // Find all the learner actions that reference the provided strategy
        HashSet<LearnerAction> learnerActions = getLearnerActionsThatReferenceStrategy(strategy.getName());
        
        // Find all the task with triggers that reference the provided strategy
        HashSet<Task> tasks = getTasksThatReferenceStrategy(strategy.getName());
        
        Set<Serializable> combinedSet = new HashSet<>();
        combinedSet.addAll(stateTransitions);
        combinedSet.addAll(learnerActions);
        combinedSet.addAll(tasks);
        
        
        /* Find references to the action within the Scenario end triggers */
        final generated.dkf.Scenario.EndTriggers endTriggers = scenario.getEndTriggers();
        if (endTriggers != null) {
            for (Scenario.EndTriggers.Trigger trigger : endTriggers.getTrigger()) {
                if (trigger.getTriggerType() instanceof StrategyApplied) {
                    StrategyApplied strategyApplied = (StrategyApplied) trigger.getTriggerType();
                    if (StringUtils.equals(strategyApplied.getStrategyName(), strategy.getName())) {
                        combinedSet.add(endTriggers);
                        break;
                    }
                }
            }
        }
        
        return combinedSet;
    }

    /**
     * Returns each of the {@link StateTransition state transitions} that reference the provided
     * {@link Task}.
     *
     * @param task The {@link Task} for which to find {@link StateTransition state transitions}.
     *        Can't be null.
     * @return An {@link Iterable} collection containing each of the {@link StateTransition state
     *         transitions} that reference the provided {@link Task}. Can't be null.
     */
    public static Iterable<Serializable> getReferencesTo(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("The parameter 'task' cannot be null.");
        }

        List<Serializable> referencers = new ArrayList<>();

        // Find transitions that reference the task
        referencers.addAll(getStateTransitionsThatReferenceNodeId(task.getNodeId()));

        // Find strategies that reference the task
        referencers.addAll(getStrategiesThatReferenceNodeId(task.getNodeId()));

        // Find tasks that reference the task
        for (Task otherTask : getUnmodifiableTaskList()) {
            boolean taskReferences = false;
            if (otherTask.getStartTriggers() != null) {
                for (StartTriggers.Trigger trigger : otherTask.getStartTriggers().getTrigger()) {
                    Serializable triggerType = trigger.getTriggerType();
                    if (doesTriggerTypeContainNodeId(triggerType, task.getNodeId())) {
                        taskReferences = true;
                        break;
                    }
                }
            }

            if (!taskReferences && otherTask.getEndTriggers() != null) {
                for (EndTriggers.Trigger trigger : otherTask.getEndTriggers().getTrigger()) {
                    Serializable triggerType = trigger.getTriggerType();
                    if (doesTriggerTypeContainNodeId(triggerType, task.getNodeId())) {
                        taskReferences = true;
                        break;
                    }
                }
            }

            if (taskReferences) {
                referencers.add(otherTask);
            }
        }

        return referencers;
    }

    /**
     * Returns each of the {@link StateTransition state transitions}, {@link Task tasks}, or
     * {@link Concept concepts} that reference the provided {@link Concept}.
     *
     * @param concept The {@link Concept} for which to find references. Can't be null.
     * @return An {@link Iterable} collection containing each of the {@link StateTransition state
     *         transitions}, {@link Task tasks}, or {@link Concept concepts} that reference the
     *         provided {@link Concept}. Can't be null.
     */
    public static Iterable<Serializable> getReferencesTo(Concept concept) {
        if (concept == null) {
            throw new IllegalArgumentException("The parameter 'concept' cannot be null.");
        }

        List<Serializable> referencers = new ArrayList<>();

        // Find transitions that reference the concept
        referencers.addAll(getStateTransitionsThatReferenceNodeId(concept.getNodeId()));

        // Find strategies that reference the task
        referencers.addAll(getStrategiesThatReferenceNodeId(concept.getNodeId()));

        // Find any Tasks that reference the concept
        boolean conceptParentFound = false;
        for (Task task : getUnmodifiableTaskList()) {
            boolean taskReferences = false;
            if (task.getStartTriggers() != null) {
                for (StartTriggers.Trigger trigger : task.getStartTriggers().getTrigger()) {
                    Serializable triggerType = trigger.getTriggerType();
                    if (doesTriggerTypeContainNodeId(triggerType, concept.getNodeId())) {
                        taskReferences = true;
                        break;
                    }
                }
            }

            if (!taskReferences && task.getEndTriggers() != null) {
                for (EndTriggers.Trigger trigger : task.getEndTriggers().getTrigger()) {
                    Serializable triggerType = trigger.getTriggerType();
                    if (doesTriggerTypeContainNodeId(triggerType, concept.getNodeId())) {
                        taskReferences = true;
                        break;
                    }
                }
            }

            /* the concept can only belong to one task, no point searching once it has been found */
            if (!conceptParentFound && task.getConcepts() != null) {
                for (Concept taskConcept : task.getConcepts().getConcept()) {
                    // Check to see if the task references the concept
                    if (taskConcept.getNodeId().equals(concept.getNodeId())) {
                        taskReferences = true;
                        conceptParentFound = true;
                        break;
                    }
                }
            }

            if (taskReferences) {
                referencers.add(task);
            }
        }

        /* If the concept's parent isn't a Task, search through the Concepts */
        if (!conceptParentFound) {
            for (Concept otherConcept : getUnmodifiableConceptList()) {
                if (otherConcept.getConditionsOrConcepts() instanceof Concepts) {
                    Concepts concepts = (Concepts) otherConcept.getConditionsOrConcepts();
                    for (Concept conceptChild : concepts.getConcept()) {
                        // Check to see if the concept node ids match
                        if (conceptChild.getNodeId().equals(concept.getNodeId())) {
                            referencers.add(otherConcept);
                            break;
                        }
                    }
                }
            }
        }

        return referencers;
    }

    /**
     * Checks if the provided trigger type contains the provided node id.
     *
     * @param triggerType the trigger type to check.
     * @param nodeId the node id to search for.
     * @return true if the trigger type contains the provided node id.
     */
    public static boolean doesTriggerTypeContainNodeId(Serializable triggerType, BigInteger nodeId) {
        if (triggerType == null || nodeId == null) {
            return false;
        }

        if (triggerType instanceof ConceptEnded) {
            ConceptEnded type = (ConceptEnded) triggerType;
            return nodeId.equals(type.getNodeId());
        } else if (triggerType instanceof ChildConceptEnded) {
            ChildConceptEnded type = (ChildConceptEnded) triggerType;
            return nodeId.equals(type.getNodeId());
        } else if (triggerType instanceof TaskEnded) {
            TaskEnded type = (TaskEnded) triggerType;
            return nodeId.equals(type.getNodeId());
        } else if (triggerType instanceof ConceptAssessment) {
            ConceptAssessment type = (ConceptAssessment) triggerType;
            return nodeId.equals(type.getConcept());
        }

        return false;
    }

    /**
     * Returns each of the {@link Concept concepts} that reference the provided {@link Condition}.
     *
     * @param condition The {@link Condition} for which to find {@link Concept concepts}. Can't be
     *        null.
     * @return A {@link Concept} that references the provided {@link Condition}. Can be null if no
     *         {@link Concept} was found.
     */
    public static Concept getReferencesTo(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }

        // Find Concept that references the condition
        for (Concept concept : getUnmodifiableConceptList()) {
            if (concept.getConditionsOrConcepts() instanceof Conditions) {
                Conditions conceptConditions = (Conditions) concept.getConditionsOrConcepts();
                for (Condition childCondition : conceptConditions.getCondition()) {
                    if (childCondition == condition) {
                        return concept;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets all trigger objects that are referring to a provided {@link LearnerAction}.
     *
     * @param action The {@link LearnerAction} for which to find references.
     * @return An {@link Iterable} collection that contains each {@link StartTriggers start
     *         triggers}, {@link EndTriggers end triggers}, or {@link Scenario.EndTriggers scenario
     *         end triggers} that refers the provided action. Can't be null.
     */
    public static Iterable<Serializable> getReferencesTo(LearnerAction action) {
        if (action == null) {
            throw new IllegalArgumentException("The parameter 'action' cannot be null.");
        }

        final String actionDisplayName = action.getDisplayName();
        Collection<Serializable> referencers = new ArrayList<>();

        /* Find references to the action within tasks */
        for (Task task : getUnmodifiableTaskList()) {
            /* Find references within the Task start triggers */
            boolean taskReferences = false;
            if (task.getStartTriggers() != null) {
                for (StartTriggers.Trigger trigger : task.getStartTriggers().getTrigger()) {
                    if (trigger.getTriggerType() instanceof LearnerActionReference) {
                        LearnerActionReference learnerActionRef = (LearnerActionReference) trigger.getTriggerType();
                        if (StringUtils.equals(learnerActionRef.getName(), actionDisplayName)) {
                            taskReferences = true;
                            break;
                        }
                    }
                }
            }

            /* Find references within the Task end triggers */
            if (!taskReferences && task.getEndTriggers() != null) {
                for (EndTriggers.Trigger trigger : task.getEndTriggers().getTrigger()) {
                    if (trigger.getTriggerType() instanceof LearnerActionReference) {
                        LearnerActionReference learnerActionRef = (LearnerActionReference) trigger.getTriggerType();
                        if (StringUtils.equals(learnerActionRef.getName(), actionDisplayName)) {
                            taskReferences = true;
                            break;
                        }
                    }
                }
            }

            if (taskReferences) {
                referencers.add(task);
            }
        }

        /* Find references to the action within the Scenario end triggers */
        final generated.dkf.Scenario.EndTriggers endTriggers = scenario.getEndTriggers();
        if (endTriggers != null) {
            for (Scenario.EndTriggers.Trigger trigger : endTriggers.getTrigger()) {
                if (trigger.getTriggerType() instanceof LearnerActionReference) {
                    LearnerActionReference learnerActionRef = (LearnerActionReference) trigger.getTriggerType();
                    if (StringUtils.equals(learnerActionRef.getName(), actionDisplayName)) {
                        referencers.add(endTriggers);
                        break;
                    }
                }
            }
        }

        return referencers;
    }

    /**
     * Returns the {@link Concept} which is the parent of a conceptToFind.
     *
     * @param conceptToSearch The concept to search within.
     * @param conceptToFind The concept whose parent is being searched for.
     * @return The parent of conceptToFind. If the parent is not found, null is returned.
     */
    private static Concept findConceptParent(Concept conceptToSearch, Concept conceptToFind) {
        if (conceptToSearch == null || !(conceptToSearch.getConditionsOrConcepts() instanceof Concepts)) {
            return null;
        }

        /* Check the children of the current node to determine if the current node is the parent */
        Concepts subConcepts = (Concepts) conceptToSearch.getConditionsOrConcepts();
        for (Concept child : subConcepts.getConcept()) {
            if (child.getNodeId().equals(conceptToFind.getNodeId())) {
                return conceptToSearch;
            }
        }

        // Search another layer down the tree
        for (Concept child : subConcepts.getConcept()) {
            Concept result = findConceptParent(child, conceptToFind);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
    
    /**
     * Retrieve the mapping of training application type to the scenario adaptations supported (see {@link #trainingAppScenarioAdaptationsMap}.  
     * If the map has already been retrieved by this client, the
     * map is returned and the server is not notified.  If there is a pending request to the server
     * for this map already, this callback will be queued and notified once the other request
     * has completed.
     * @param callback used to asynchronously return the map to the caller.
     */
    public static void getTrainingAppScenarioAdaptations(final AsyncCallback<Map<TrainingApplicationEnum, Set<String>>> callback){
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }        
        
        if(!trainingAppScenarioAdaptationsMap.isEmpty()){
            callback.onSuccess(trainingAppScenarioAdaptationsMap);
            return;
        }
        
        // there are already callbacks pending
        if (!trainingAppScenarioAdaptationsCallbacks.isEmpty()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(
                        "there are other callbacks for training app scenario adaptations pending, add this callback to that collection");
            }

            // add the callback to the pending list and return
            trainingAppScenarioAdaptationsCallbacks.add(callback);
            return;
        }

        FetchTrainingAppScenarioAdaptations fetch = new FetchTrainingAppScenarioAdaptations();

        /* there are NO other callbacks pending, add this as the first
         * callback to that collection */
        trainingAppScenarioAdaptationsCallbacks.add(callback);

        SharedResources.getInstance().getDispatchService().execute(fetch,
                new AsyncCallback<FetchTrainingAppScenarioAdaptationsResult>() {

                    @Override
                    public void onFailure(Throwable thrown) {

                        if (!trainingAppScenarioAdaptationsCallbacks.isEmpty()) {
                            Iterator<AsyncCallback<Map<TrainingApplicationEnum, Set<String>>>> itr = trainingAppScenarioAdaptationsCallbacks.iterator();
                            while (itr.hasNext()) {
                                AsyncCallback<Map<TrainingApplicationEnum, Set<String>>> queuedCallback = itr.next();
                                try {
                                    if (queuedCallback != null) {
                                        queuedCallback.onFailure(thrown);
                                    }
                                } catch (Throwable t) {
                                    logger.log(Level.SEVERE, "trainingAppScenarioAdaptationsCallbacks callback had an error, continuing to next callback (if any).", t);
                                }
                                itr.remove();
                            }
                        }
                    }

                    @Override
                    public void onSuccess(FetchTrainingAppScenarioAdaptationsResult result) {

                        if (result == null) {
                            return;
                        }

                        if (result.isSuccess()) {

                            if (result.getTrainingAppScenarioAdaptationsMap() != null) {
                                // update the cache for the next caller
                                trainingAppScenarioAdaptationsMap = result.getTrainingAppScenarioAdaptationsMap();
                            }

                            if (!trainingAppScenarioAdaptationsCallbacks.isEmpty()) {
                                Iterator<AsyncCallback<Map<TrainingApplicationEnum, Set<String>>>> itr = trainingAppScenarioAdaptationsCallbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<Map<TrainingApplicationEnum, Set<String>>> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onSuccess(result.getTrainingAppScenarioAdaptationsMap());
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "trainingAppScenarioAdaptationsCallbacks callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }

                        } else {

                            if (!trainingAppScenarioAdaptationsCallbacks.isEmpty()) {
                                Iterator<AsyncCallback<Map<TrainingApplicationEnum, Set<String>>>> itr = trainingAppScenarioAdaptationsCallbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<Map<TrainingApplicationEnum, Set<String>>> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onFailure(new Exception(
                                                    "The server failed to return the training application scenario adapations because "
                                                            + result.getErrorMsg()));
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "trainingAppScenarioAdaptationsCallbacks callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }
                        }
                    }
                });
        
    }
    
    /**
     * Retrieve the mapping of condition to the collection of overall assessment types the condition 
     * supports for AAR from the server (see {@link #overallAssessmentTypesConditionsMap}.  
     * If the map has already been retrieved by this client, the
     * map is returned and the server is not notified.  If there is a pending request to the server
     * for this map already, this callback will be queued and notified once the other request
     * has completed.
     * @param callback used to asynchronously return the map to the caller.
     */
    public static void getConditionsOverallAssessmentTypes(final AsyncCallback<Map<String, Set<String>>> callback){
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }        
        
        if(!overallAssessmentTypesConditionsMap.isEmpty()){
            callback.onSuccess(overallAssessmentTypesConditionsMap);
            return;
        }
        
        // there are already callbacks pending
        if (!conditionsOverallAssessmentTypesCallbacks.isEmpty()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(
                        "there are other callbacks for this conditionImpl pending, add this callback to that collection");
            }

            // add the callback to the pending list and return
            conditionsOverallAssessmentTypesCallbacks.add(callback);
            return;
        }

        FetchConditionsOverallAssessmentTypes fetch = new FetchConditionsOverallAssessmentTypes();

        /* there are NO other callbacks for this conditionImpl pending, add this as the first
         * callback to that collection */
        conditionsOverallAssessmentTypesCallbacks.add(callback);

        SharedResources.getInstance().getDispatchService().execute(fetch,
                new AsyncCallback<FetchConditionsOverallAssessmentTypesResult>() {

                    @Override
                    public void onFailure(Throwable thrown) {

                        if (!conditionsOverallAssessmentTypesCallbacks.isEmpty()) {
                            Iterator<AsyncCallback<Map<String, Set<String>>>> itr = conditionsOverallAssessmentTypesCallbacks.iterator();
                            while (itr.hasNext()) {
                                AsyncCallback<Map<String, Set<String>>> queuedCallback = itr.next();
                                try {
                                    if (queuedCallback != null) {
                                        queuedCallback.onFailure(thrown);
                                    }
                                } catch (Throwable t) {
                                    logger.log(Level.SEVERE, "conditionsOverallAssessmentTypesCallbacks callback had an error, continuing to next callback (if any).", t);
                                }
                                itr.remove();
                            }
                        }
                    }

                    @Override
                    public void onSuccess(FetchConditionsOverallAssessmentTypesResult result) {

                        if (result == null) {
                            return;
                        }

                        if (result.isSuccess()) {

                            if (result.getOverallAssessmentTypesconditionsMap() != null) {
                                // update the cache for the next caller
                                overallAssessmentTypesConditionsMap = result.getOverallAssessmentTypesconditionsMap();
                            }

                            if (!conditionsOverallAssessmentTypesCallbacks.isEmpty()) {
                                Iterator<AsyncCallback<Map<String, Set<String>>>> itr = conditionsOverallAssessmentTypesCallbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<Map<String, Set<String>>> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onSuccess(result.getOverallAssessmentTypesconditionsMap());
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "conditionsOverallAssessmentTypesCallbacks callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }

                        } else {

                            if (!conditionsOverallAssessmentTypesCallbacks.isEmpty()) {
                                Iterator<AsyncCallback<Map<String, Set<String>>>> itr = conditionsOverallAssessmentTypesCallbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<Map<String, Set<String>>> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onFailure(new Exception(
                                                    "The server failed to return the condition information because "
                                                            + result.getErrorMsg()));
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "conditionsOverallAssessmentTypesCallbacks callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }
                        }
                    }
                });
        
    }
    
    /**
     * Return whether all conditions in the provided parameter can call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * @param conditionsOrConcepts either a {@link generated.dkf.Concepts} or {@link generated.dkf.Conditions} object
     * to check all conditions implementations against the server's list of conditions that can complete.
     * @return true if all conditions found in the given parameters can complete.  Also returns false if the 
     * conditionsOrConcepts is null.
     */
    public static boolean hasOnlyConditionsThatCanComplete(Serializable conditionsOrConcepts){
        
        if(CollectionUtils.isEmpty(conditionsThatCanComplete)){
            return true;
        }else if(conditionsOrConcepts == null){
            return false;
        }
        
        if(conditionsOrConcepts instanceof generated.dkf.Conditions){
            
            for(generated.dkf.Condition condition : ((generated.dkf.Conditions)conditionsOrConcepts).getCondition()){
                
                if(!conditionsThatCanComplete.contains(condition.getConditionImpl())){
                    return false;
                }
            }
            
        }else{
            
            generated.dkf.Concepts concepts = (generated.dkf.Concepts)conditionsOrConcepts;
            for(Concept concept : concepts.getConcept()){
                
                if(!hasOnlyConditionsThatCanComplete(concept.getConditionsOrConcepts())){
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Retrieve the list of condition classes that call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * 
     * @param callback used to notify the caller of the conditions found by the server.  Contains the condition classes 
     * without the "mil.arl.gift" prefix that can call {@link #AbstractConditionconditionCompleted()}.
     */
    public static void getConditionsThatCanComplete(final AsyncCallback<Set<String>> callback){
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }
        
        if (conditionsThatCanComplete != null) {
            callback.onSuccess(conditionsThatCanComplete);
        } else {

            if (!conditionsThatCanCompleteCallbacks.isEmpty()) {
                // there are other callbacks for this request pending, add this callback to
                // that collection
                conditionsThatCanCompleteCallbacks.add(callback);

            } else {

                // there are NO other callbacks for this request pending, add this as the
                // first callback to that collection
                conditionsThatCanCompleteCallbacks.add(callback);

                rpcService.getConditionsThatCanComplete(
                        new AsyncCallback<Set<String>>() {

                            @Override
                            public void onSuccess(Set<String> result) {

                                if (result != null) {
                                    conditionsThatCanComplete = result;
                                }

                                Iterator<AsyncCallback<Set<String>>> itr = conditionsThatCanCompleteCallbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<Set<String>> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onSuccess(result);
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "conditionsThatCanCompleteCallbacks callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {

                                Iterator<AsyncCallback<Set<String>>> itr = conditionsThatCanCompleteCallbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<Set<String>> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onFailure(caught);
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "conditionsThatCanCompleteCallbacks callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }
                        });
            }
        }
    }

    /**
     * Retrieve the condition information for a condition class impl. If not already cached this
     * method will query the server for this information.
     *
     * @param conditionImpl a condition class impl (e.g.
     *        "domain.knowledge.condition.ApplicationCompletedCondition") without the "mil.arl.gift"
     *        prefix to retrieve the condition information for. Can't be blank.
     * @param callback used to notify the caller of the condition information for this condition
     *        class impl. Can't be null.
     */
    public static void getConditionInfoForConditionImpl(final String conditionImpl, final AsyncCallback<ConditionInfo> callback) {
        if (StringUtils.isBlank(conditionImpl)) {
            throw new IllegalArgumentException("The parameter 'conditionImpl' cannot be blank.");
        } else if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        // if it already exists, return the result
        if (conditionImplToConditionInfo.containsKey(conditionImpl)) {
            callback.onSuccess(conditionImplToConditionInfo.get(conditionImpl));
            return;
        }

        List<AsyncCallback<ConditionInfo>> callbacks = conditionImplConditionInfoCallbacksMap.get(conditionImpl);
        if (callbacks == null) {
            callbacks = new ArrayList<AsyncCallback<ConditionInfo>>();
            conditionImplConditionInfoCallbacksMap.put(conditionImpl, callbacks);
        }

        // there are already callbacks pending
        if (!callbacks.isEmpty()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(
                        "there are other callbacks for this conditionImpl pending, add this callback to that collection");
            }

            // add the callback to the pending list and return
            callbacks.add(callback);
            return;
        }

        FetchConditionImplDescription fetch = new FetchConditionImplDescription();
        fetch.setImplClassName(conditionImpl);

        /* there are NO other callbacks for this conditionImpl pending, add this as the first
         * callback to that collection */
        callbacks.add(callback);

        SharedResources.getInstance().getDispatchService().execute(fetch,
                new AsyncCallback<FetchConditionImplDescriptionResult>() {

                    @Override
                    public void onFailure(Throwable thrown) {

                        List<AsyncCallback<ConditionInfo>> callbacks = conditionImplConditionInfoCallbacksMap
                                .get(conditionImpl);
                        if (callbacks != null && !callbacks.isEmpty()) {
                            Iterator<AsyncCallback<ConditionInfo>> itr = callbacks.iterator();
                            while (itr.hasNext()) {
                                AsyncCallback<ConditionInfo> queuedCallback = itr.next();
                                try {
                                    if (queuedCallback != null) {
                                        queuedCallback.onFailure(thrown);
                                    }
                                } catch (Throwable t) {
                                    logger.log(Level.SEVERE, "conditionImplConditionInfoCallbacksMap callback had an error, continuing to next callback (if any).", t);
                                }
                                itr.remove();
                            }
                        }
                    }

                    @Override
                    public void onSuccess(FetchConditionImplDescriptionResult result) {

                        if (result == null) {
                            return;
                        }

                        if (result.isSuccess()) {

                            if (result.getConditionInfo() != null) {
                                // update the cache for the next caller
                                conditionImplToConditionInfo.put(result.getConditionInfo().getConditionClass(),
                                        result.getConditionInfo());
                            }

                            List<AsyncCallback<ConditionInfo>> callbacks = conditionImplConditionInfoCallbacksMap
                                    .get(conditionImpl);
                            if (callbacks != null && !callbacks.isEmpty()) {
                                Iterator<AsyncCallback<ConditionInfo>> itr = callbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<ConditionInfo> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onSuccess(result.getConditionInfo());
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "conditionImplConditionInfoCallbacksMap callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }

                        } else {

                            List<AsyncCallback<ConditionInfo>> callbacks = conditionImplConditionInfoCallbacksMap
                                    .get(conditionImpl);
                            if (callbacks != null && !callbacks.isEmpty()) {
                                Iterator<AsyncCallback<ConditionInfo>> itr = callbacks.iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<ConditionInfo> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onFailure(new Exception(
                                                    "The server failed to return the condition information because "
                                                            + result.getErrorMsg()));
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "conditionImplConditionInfoCallbacksMap callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Retrieve the collection of condition information for all enumerated learner action types. If
     * not already cached this method will query the server for this information.
     *
     * @param callback used to notify the caller of the condition information collection all learner
     *        action types. Can be null.
     */
    public static void getConditionsForLearnerActions(final AsyncCallback<List<ConditionInfo>> callback) {

        if (learnerActionConditions.isEmpty()) {

            if (!learnerActionConditionsCallbacks.isEmpty()) {
                // there are other callbacks for learner actions conditions pending, add this
                // callback to that collection

                if (callback != null) {
                    learnerActionConditionsCallbacks.add(callback);
                }
            } else {

                // there are NO other callbacks for learner actions conditions pending, add this as
                // the first callback to that collection
                // Note: adding null is acceptable as this indicates a server call is pending but
                // the caller doesn't want to be notified.
                learnerActionConditionsCallbacks.add(callback);

                rpcService.getConditionsForLearnerActions(new AsyncCallback<List<ConditionInfo>>() {

                    @Override
                    public void onFailure(Throwable caught) {

                        if (callback != null) {

                            if (learnerActionConditionsCallbacks != null) {
                                Iterator<AsyncCallback<List<ConditionInfo>>> itr = learnerActionConditionsCallbacks
                                        .iterator();
                                while (itr.hasNext()) {
                                    AsyncCallback<List<ConditionInfo>> queuedCallback = itr.next();
                                    try {
                                        if (queuedCallback != null) {
                                            queuedCallback.onFailure(caught);
                                        }
                                    } catch (Throwable t) {
                                        logger.log(Level.SEVERE, "learnerActionConditionsCallbacks callback had an error, continuing to next callback (if any).", t);
                                    }
                                    itr.remove();
                                }
                            }
                        } else {
                            logger.log(Level.SEVERE,
                                    "The server failed to retrieve the condition information for all learner actions",
                                    caught);
                        }
                    }

                    @Override
                    public void onSuccess(List<ConditionInfo> result) {

                        if (result != null) {
                            learnerActionConditions.addAll(result);
                        }

                        if (learnerActionConditionsCallbacks != null) {
                            Iterator<AsyncCallback<List<ConditionInfo>>> itr = learnerActionConditionsCallbacks
                                    .iterator();
                            while (itr.hasNext()) {
                                AsyncCallback<List<ConditionInfo>> queuedCallback = itr.next();
                                try {
                                    if (queuedCallback != null) {
                                        queuedCallback.onSuccess(learnerActionConditions);
                                    }
                                } catch (Throwable t) {
                                    logger.log(Level.SEVERE, "learnerActionConditionsCallbacks callback had an error, continuing to next callback (if any).", t);
                                }
                                itr.remove();
                            }
                        }
                    }
                });
            }
        } else {
            callback.onSuccess(learnerActionConditions);
        }
    }

    /**
     * Retrieve the collection of condition information for a training application. If not already
     * cached this method will query the server for this information.
     *
     * @param trainingAppEnum the training application type to retrieve the collection of condition
     *        information for. If null the callback onFailure is called.
     * @param callback used to notify the caller of the condition information collection for this
     *        training application type. Can be null.
     */
    public static void getConditionsForTrainingApp(final TrainingApplicationEnum trainingAppEnum,
            final AsyncCallback<Set<ConditionInfo>> callback) {

        if (appToConditions.containsKey(trainingAppEnum)) {
            callback.onSuccess(appToConditions.get(trainingAppEnum));
        } else if (trainingAppEnum == null) {
            callback.onFailure(new NullPointerException(
                    "Unable to retrieve the condition information for all conditions of a null training application type."));
        } else {

            List<AsyncCallback<Set<ConditionInfo>>> callbacks = trainingAppConditionInfoCallbacksMap
                    .get(trainingAppEnum);
            if (callbacks == null) {
                callbacks = new ArrayList<AsyncCallback<Set<ConditionInfo>>>();
                trainingAppConditionInfoCallbacksMap.put(trainingAppEnum, callbacks);
            }

            if (!callbacks.isEmpty()) {
                // there are other callbacks for this conditionImpl pending, add this callback to
                // that collection

                if (callback != null) {
                    callbacks.add(callback);
                }
            } else {

                // there are NO other callbacks for this conditionImpl pending, add this as the
                // first callback to that collection
                // Note: adding null is acceptable as this indicates a server call is pending but
                // the caller doesn't want to be notified.
                callbacks.add(callback);

                rpcService.getConditionsForTrainingApplication(trainingAppEnum,
                        new AsyncCallback<Set<ConditionInfo>>() {

                            @Override
                            public void onSuccess(Set<ConditionInfo> result) {

                                if (result != null) {
                                    appToConditions.put(trainingAppEnum, result);

                                    // update condition impl map as well
                                    for (ConditionInfo condInfo : result) {
                                        conditionImplToConditionInfo.put(condInfo.getConditionClass(), condInfo);
                                    }
                                }

                                List<AsyncCallback<Set<ConditionInfo>>> callbacks = trainingAppConditionInfoCallbacksMap
                                        .get(trainingAppEnum);
                                if (callbacks != null) {
                                    Iterator<AsyncCallback<Set<ConditionInfo>>> itr = callbacks.iterator();
                                    while (itr.hasNext()) {
                                        AsyncCallback<Set<ConditionInfo>> queuedCallback = itr.next();
                                        try {
                                            if (queuedCallback != null) {
                                                queuedCallback.onSuccess(result);
                                            }
                                        } catch (Throwable t) {
                                            logger.log(Level.SEVERE, "trainingAppConditionInfoCallbacksMap callback had an error, continuing to next callback (if any).", t);
                                        }
                                        itr.remove();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {

                                if (callback != null) {

                                    List<AsyncCallback<Set<ConditionInfo>>> callbacks = trainingAppConditionInfoCallbacksMap
                                            .get(trainingAppEnum);
                                    if (callbacks != null) {
                                        Iterator<AsyncCallback<Set<ConditionInfo>>> itr = callbacks.iterator();
                                        while (itr.hasNext()) {
                                            AsyncCallback<Set<ConditionInfo>> queuedCallback = itr.next();
                                            try {
                                                if (queuedCallback != null) {
                                                    queuedCallback.onFailure(caught);
                                                }
                                            } catch (Throwable t) {
                                                logger.log(Level.SEVERE, "trainingAppConditionInfoCallbacksMap callback had an error, continuing to next callback (if any).", t);
                                            }
                                            itr.remove();
                                        }
                                    }
                                } else {
                                    logger.log(Level.SEVERE,
                                            "The server failed to retrieve the condition information for all conditions of the training application "
                                                    + trainingAppEnum,
                                            caught);
                                }
                            }
                        });
            }
        }
    }

    /**
     * Retrieves the course surveys. Will return the result in the callback.
     *
     * @param callback the async callback that will catch the result being returned. Can't be null.
     */
    public static void getCourseSurveys(final AsyncCallback<FetchSurveyContextSurveysResult> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        if (courseSurveys == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("getCourseSurveys() - No surveys in memory, fetching survey list.");
            }

            FetchSurveyContextSurveys action = new FetchSurveyContextSurveys();
            if (scenario != null && scenario.getResources() != null
                    && scenario.getResources().getSurveyContext() != null) {
                action.setSurveyContextId(scenario.getResources().getSurveyContext().intValue());
                SharedResources.getInstance().getDispatchService().execute(action,
                        new AsyncCallback<FetchSurveyContextSurveysResult>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                callback.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(FetchSurveyContextSurveysResult result) {
                                courseSurveys = result.getSurveys();
                                callback.onSuccess(result);
                            }
                        });

            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Failed to fetch survey list because scenario survey context was null.");
                }
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("getCourseSurveys() - Returning pre-fetched surveys.");
            }

            FetchSurveyContextSurveysResult result = new FetchSurveyContextSurveysResult();
            result.setSurveys(courseSurveys);
            callback.onSuccess(result);
        }
    }

    /**
     * Gets the survey context being used by the current scenario. If the scenario did not contain a survey context ID when
     * it was loaded, this method will return null.
     *
     * @return the scenario's survey context, or null, if no survey context ID was defined in the loaded scenario
     */
    public static BigInteger getSurveyContextId() {

        if (scenario != null && scenario.getResources() != null) {
            return scenario.getResources().getSurveyContext();
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Failed to fetch survey list because scenario survey context was null.");
            }

            return null;
        }
    }

    /**
     * Finds all of the object referencing places of interest and compiles a mapping of their references
     */
    public static void gatherPlacesOfInterestReferences() {

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Updating places of interest references");
        }

        placeOfInterestReferences.clear();

        for (Serializable poi : ScenarioClientUtility.getUnmodifiablePlacesOfInterestList()) {

            String name = ScenarioClientUtility.getPlaceOfInterestName(poi);

            if(name != null) {
                placeOfInterestReferences.put(name, new ArrayList<PlaceOfInterestReference>());
            }
        }

        // check conditions
        for (Concept concept : ScenarioClientUtility.getUnmodifiableConceptList()) {
            Serializable child = concept.getConditionsOrConcepts();
            if (child instanceof Conditions) {
                List<Condition> conditions = ((Conditions) child).getCondition();
                for (Condition condition : conditions) {
                    gatherWaypointReferences(condition, concept);
                }
            }
        }

        // check strategies - in task triggers
        for(Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {

            if(task.getStartTriggers() != null) {
                for(StartTriggers.Trigger trigger :  task.getStartTriggers().getTrigger()) {
                    if(trigger.getTriggerMessage() != null){                        
                        gatherPlaceOfInterestReferences(trigger.getTriggerMessage().getStrategy());
                    }
                }
                
                for(EndTriggers.Trigger trigger :  task.getEndTriggers().getTrigger()) {
                    if(trigger.getMessage() != null){
                        gatherPlaceOfInterestReferences(trigger.getMessage().getStrategy());
                    }
                }
            }
        }
        
        // check strategies - in strategies list
        for(Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {
            gatherPlaceOfInterestReferences(strategy);
        }

        SharedResources.getInstance().getEventBus().fireEvent(new PlacesOfInterestReferencesUpdatedEvent());

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished updating places of interest references");
        }
    }

    /**
     * Updates the global place of interest references based on the strategy's references, if any.
     * 
     * @param strategy the strategy to check for place of interest references
     */
    private static void gatherPlaceOfInterestReferences(Strategy strategy){
        
        if(strategy == null){
            return;
        }
        
        for(Serializable activity : strategy.getStrategyActivities()) {
            
            if(activity instanceof ScenarioAdaptation) {

                EnvironmentAdaptation adaptation = ((ScenarioAdaptation) activity).getEnvironmentAdaptation();
                if(adaptation.getType() instanceof HighlightObjects){
                    
                    HighlightObjects highlight = (HighlightObjects)adaptation.getType();
                    if(highlight.getType() instanceof EnvironmentAdaptation.HighlightObjects.LocationInfo){
                        
                        EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = 
                                (EnvironmentAdaptation.HighlightObjects.LocationInfo)highlight.getType();
                        
                        String poiRef = locationInfo.getPlaceOfInterestRef();
                        addPlaceOfInterestReference(strategy, poiRef);                        
                    }
                }else if(adaptation.getType() instanceof CreateBreadcrumbs){
                    
                    CreateBreadcrumbs breadcrumbs = (CreateBreadcrumbs)adaptation.getType();
                    generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = breadcrumbs.getLocationInfo();
                    String poiRef = locationInfo.getPlaceOfInterestRef();
                    addPlaceOfInterestReference(strategy, poiRef);

                }
            }
        }

    }

    /**
     * Within the given condition, finds all of the referenced places of interest and compiles a mapping of
     * their references.
     *
     * @param condition the condition with which to search for referenced places of interest. Can't be null.
     * @param parentConcept the concept that is the parent of the provided condition. Can't be null.
     */
    private static void gatherWaypointReferences(Condition condition, Concept parentConcept) {
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        } else if (parentConcept == null) {
            throw new IllegalArgumentException("The parameter 'parentConcept' cannot be null.");
        }

        if (condition.getInput() == null) {
            return;
        }

        Serializable inputType = condition.getInput().getType();
        if (inputType != null) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding " + inputType.getClass().getSimpleName() + " reference");
            }

            if (inputType instanceof AvoidLocationCondition) {

                AvoidLocationCondition avoidLocationCondition = (AvoidLocationCondition) inputType;

                for(PointRef pointRef : avoidLocationCondition.getPointRef()){
                    String ref = pointRef.getValue();
                    addPlaceOfInterestReference(parentConcept, condition, ref);
                }

                for(AreaRef areaRef : avoidLocationCondition.getAreaRef()){
                    String ref = areaRef.getValue();
                    addPlaceOfInterestReference(parentConcept, condition, ref);
                }

            } else if (inputType instanceof IdentifyPOIsCondition) {

                IdentifyPOIsCondition identifyPoisCondition = (IdentifyPOIsCondition) inputType;
                List<PointRef> pointRefs = identifyPoisCondition.getPois().getPointRef();
                for (PointRef pointRef : pointRefs) {

                    String ref = pointRef.getValue();
                    addPlaceOfInterestReference(parentConcept, condition, ref);
                }
            } else if (inputType instanceof CheckpointPaceCondition) {
                CheckpointPaceCondition checkpointPaceCondition = (CheckpointPaceCondition) inputType;
                List<Checkpoint> checkpoints = checkpointPaceCondition.getCheckpoint();
                for (Checkpoint checkpoint : checkpoints) {

                    String ref = checkpoint.getPoint();
                    addPlaceOfInterestReference(parentConcept, condition, ref);
                }
            } else if (inputType instanceof CheckpointProgressCondition) {
                CheckpointProgressCondition checkpointProgressCondition = (CheckpointProgressCondition) inputType;
                List<Checkpoint> checkpoints = checkpointProgressCondition.getCheckpoint();
                for (Checkpoint checkpoint : checkpoints) {

                    String ref = checkpoint.getPoint();
                    addPlaceOfInterestReference(parentConcept, condition, ref);
                }
            } else if (inputType instanceof CorridorBoundaryCondition) {
                CorridorBoundaryCondition corridorBoundaryCondition = (CorridorBoundaryCondition) inputType;
                if(corridorBoundaryCondition.getPathRef() != null) {

                    String ref = corridorBoundaryCondition.getPathRef().getValue();
                    addPlaceOfInterestReference(parentConcept, condition, ref);
                }
            } else if (inputType instanceof CorridorPostureCondition) {
                CorridorPostureCondition corridorPostureCondition = (CorridorPostureCondition) inputType;
                if(corridorPostureCondition.getPathRef() != null) {

                    String ref = corridorPostureCondition.getPathRef().getValue();
                    addPlaceOfInterestReference(parentConcept, condition, ref);
                }
            } else if (inputType instanceof EnterAreaCondition) {
                EnterAreaCondition enterAreaCondition = (EnterAreaCondition) inputType;
                List<Entrance> entrances = enterAreaCondition.getEntrance();
                for (Entrance entrance : entrances) {

                    if (entrance.getInside() != null) {

                        String ref = entrance.getInside().getPoint();
                        addPlaceOfInterestReference(parentConcept, condition, ref);
                    }

                    if (entrance.getOutside() != null) {

                        String ref = entrance.getOutside().getPoint();
                        addPlaceOfInterestReference(parentConcept, condition, ref);
                    }
                }
                
            } else if (inputType instanceof DetectObjectsCondition) {

                DetectObjectsCondition conditionType = (DetectObjectsCondition) inputType;
                
                if(conditionType.getObjectsToDetect() == null) {
                    return;
                }
                
                List<Serializable> refObjs = conditionType.getObjectsToDetect().getTeamMemberRefOrPointRef();
                for (Serializable refObj : refObjs) {

                    if(refObj instanceof PointRef) {
                        String ref = ((PointRef) refObj).getValue();
                        addPlaceOfInterestReference(parentConcept, condition, ref);
                    }
                }
                
            } else if (inputType instanceof EngageTargetsCondition) {

                EngageTargetsCondition conditionType = (EngageTargetsCondition) inputType;
                
                if(conditionType.getTargetsToEngage() == null) {
                    return;
                }
                
                List<Serializable> refObjs = conditionType.getTargetsToEngage().getTeamMemberRefOrPointRef();
                for (Serializable refObj : refObjs) {

                    if(refObj instanceof PointRef) {
                        String ref = ((PointRef) refObj).getValue();
                        addPlaceOfInterestReference(parentConcept, condition, ref);
                    }
                }
                
            } else if (inputType instanceof NegligentDischargeCondition) {

                NegligentDischargeCondition conditionType = (NegligentDischargeCondition) inputType;
                
                if(conditionType.getTargetsToAvoid() == null) {
                    return;
                }
                
                List<Serializable> refObjs = conditionType.getTargetsToAvoid().getTeamMemberRefOrPointRef();
                for (Serializable refObj : refObjs) {

                    if(refObj instanceof PointRef) {
                        String ref = ((PointRef) refObj).getValue();
                        addPlaceOfInterestReference(parentConcept, condition, ref);
                    }
                }
            } else if(inputType instanceof AssignedSectorCondition){
                
                AssignedSectorCondition conditionType = (AssignedSectorCondition)inputType;
                
                if(conditionType.getPointRef() == null){
                    return;
                }
                
                String ref = conditionType.getPointRef().getValue();
                addPlaceOfInterestReference(parentConcept, condition, ref);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Finished adding reference");
            }
        }
    }

    /**
     * Creates a mapping of the given condition to the place of interest it references
     *
     * @param parent the condition's parent concept. Used to display concept names to authors so
     *        they can better identify conditions.
     * @param condition the condition where a reference was found
     * @param reference the referenced place of interest (e.g. point)
     */
    private static void addPlaceOfInterestReference(Concept parent, Condition condition, String reference) {

        if (condition == null) {
            return;
        }

        Serializable referencedWaypoint = getPlaceOfInterestWithName(reference);
        if (referencedWaypoint != null) {

            List<PlaceOfInterestReference> refs = getReferencesTo(referencedWaypoint);
            for (PlaceOfInterestReference ref : refs) {

                if(ref instanceof PlaceOfInterestConditionReference &&
                    condition.equals(((PlaceOfInterestConditionReference)ref).getCondition())) {

                    // this condition already references this place of interest, so increment the number of
                    // references
                    ref.incrementReferences();
                    return;
                }
            }

            // no references to this place of interest have been found in this condition yet, so add a new
            // reference
            refs.add(new PlaceOfInterestConditionReference(parent, condition));
        }
    }

    /**
     * Checks the strategy for the place of interest name provided and increments the global
     * counter for the named place of interest.
     * @param strategy the strategy to check for the place of interest
     * @param reference the name of a place of interest to increment the global reference counter for
     */
    private static void addPlaceOfInterestReference(Strategy strategy, String reference){
        
        if (strategy == null) {
            return;
        }

        Serializable referencedWaypoint = getPlaceOfInterestWithName(reference);
        if (referencedWaypoint != null) {

            List<PlaceOfInterestReference> refs = getReferencesTo(referencedWaypoint);
            for (PlaceOfInterestReference ref : refs) {

                if(ref instanceof PlaceOfInterestStrategyReference &&
                    StringUtils.equals(strategy.getName(), ((PlaceOfInterestStrategyReference)ref).getStrategy().getName())) {
    
                    // this condition already references this place of interest, so increment the number of
                    // references
                    ref.incrementReferences();
                    return;
                }
            }

            // no references to this place of interest have been found in this strategy yet, so add a new
            // reference
            refs.add(new PlaceOfInterestStrategyReference(strategy));
        }
    }

    /**
     * Gets all of the objects referencing the given place of interest
     *
     * @param placeOfInterest the place of interest to find references for
     * @return the list of objects referencing the place of interest. May return null if place of interest references have not been gathered yet,
     * or an empty list if no objects reference the place of interest.
     */
    public static List<PlaceOfInterestReference> getReferencesTo(Serializable placeOfInterest) {
        return placeOfInterestReferences.get(ScenarioClientUtility.getPlaceOfInterestName(placeOfInterest));
    }

    /**
     * Gets all of the strategy activities referencing the given team or team member
     *
     * @param teamName the name of team or team member to find references for
     * @return the list of strategy activities referencing the team or team member. May return null if
     * team references have not been gathered yet, or an empty list if no activities reference the team or team member.
     */
    public static List<TeamReference> getReferencesToTeam(String teamName) {
        return teamReferences.get(teamName);
    }
    
    /**
     * Updates all references to the given place of interest to reflect underlying changes to its data. 
     * <br/><br/>
     * This method is different from {@link #updatePlaceOfInterestReferences(String, String)} because
     * it doesn't actually modify uses of the place of interest's name, but it <i>does</i> make all of 
     * the references dirty, causing them to be revalidated.
     * <br/><br/>
     * This can be useful when an object's validation state is partly dependent on the data inside of a 
     * place of interest. For instance, if a condition's validation state is dependent on a place of interest's
     * coordinate type, then invoking this method will revalidate that condition to reflect the current 
     * coordinate type.
     *
     * @param name the name of the place of interest whose references need to be updated. 
     * If null, this method will do nothing.
     */
    public static void updatePlaceOfInterestReferences(String name) {
        updatePlaceOfInterestReferences(name, null, false);
    }
    
    /**
     * Updates all references to the place of interest with the old name to use the new name instead. This also
     * causes any references to the old name to be made dirty, causing them to be revalidated with the new name.
     *
     * @param oldName the old place of interest name to replace. If null, this method will do nothing.
     * @param newName the new place of interest name to use. Can be null.
     */
    public static void updatePlaceOfInterestReferences(String oldName, String newName) {
        updatePlaceOfInterestReferences(oldName, newName, true);
    }

    /**
     * Updates all references to the place of interest with the name <i>oldName</i>, making them dirty and 
     * causing them to be revalidated. 
     * <br/><br/>
     * If <i>replaceRefs</i> is true and <i>oldName</i> and <i>newName</i> are different, then this method 
     * will also replace all uses of <i>oldName</i> with <i>newName</i> and trigger a {@link PlaceOfInterestRenamedEvent}. 
     * If <i>replaceRefs</i> is false, then <i>newName</i> is ignored and the existing references are simply made dirty
     * so they can be revalidated.
     *
     * @param oldName the old (i.e. existing) place of interest name. If <i>replaceRefs</i> is true, this will be replaced
     * by <i>newName</i>. If null, this method will do nothing.
     * @param newName the new place of interest name to replace the old name with. Will not be used if <i>replaceRefs</i>
     * is false.
     * @param replaceRefs whether to modify existing references to <i>oldName</i> to use <i>newName</i> instead. If false, then
     * the references will simply be made dirty and revalidated without changing any of the references.
     */
    private static void updatePlaceOfInterestReferences(String oldName, String newName, boolean replaceRefs) {
        // if the old name is blank or if the old and new values are the same, do nothing.
        if (replaceRefs && (StringUtils.isBlank(oldName) || StringUtils.equals(oldName, newName))) {
            return;
        }

        Set<Serializable> referencingSchemaObjects = new HashSet<Serializable>();
        for (Concept concept : getUnmodifiableConceptList()) {
            referencingSchemaObjects.addAll(updateWaypointReferences(oldName, newName, concept, replaceRefs));
        }

        //update references in strategies
        for(Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {

            for(Serializable activity : strategy.getStrategyActivities()) {
                
                if(activity instanceof ScenarioAdaptation) {

                    EnvironmentAdaptation adaptation = ((ScenarioAdaptation) activity).getEnvironmentAdaptation();
                    if(adaptation.getType() instanceof HighlightObjects){
                        
                        HighlightObjects highlight = (HighlightObjects)adaptation.getType();
                        if(highlight.getType() instanceof EnvironmentAdaptation.HighlightObjects.LocationInfo){
                            
                            EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = 
                                    (EnvironmentAdaptation.HighlightObjects.LocationInfo)highlight.getType();
                            
                            String poiRef = locationInfo.getPlaceOfInterestRef();
                            if(poiRef != null && StringUtils.equals(poiRef, oldName)) {

                                if(replaceRefs) {
                                    if(StringUtils.isNotBlank(newName)) {
                                        locationInfo.setPlaceOfInterestRef(newName); //replace old name reference with new name
    
                                    } else {
                                        locationInfo.setPlaceOfInterestRef(null); //new name is empty, so remove reference
                                    }
                                }

                                referencingSchemaObjects.add(highlight);
                            }
                        }
                    }else if(adaptation.getType() instanceof CreateBreadcrumbs){
                        
                        CreateBreadcrumbs breadcrumbs = (CreateBreadcrumbs)adaptation.getType();
                        generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = breadcrumbs.getLocationInfo();
                        String poiRef = locationInfo.getPlaceOfInterestRef();
                        if(poiRef != null && StringUtils.equals(poiRef, oldName)) {

                            if(replaceRefs) {
                                if(StringUtils.isNotBlank(newName)) {
                                    locationInfo.setPlaceOfInterestRef(newName); //replace old name reference with new name
    
                                } else {
                                    locationInfo.setPlaceOfInterestRef(null); //new name is empty, so remove reference
                                }
                            }

                            referencingSchemaObjects.add(breadcrumbs);
                        }

                    }
                }
            }
        }

        // references have been updated, so gather the global references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();

        if(replaceRefs) {
        
            // notify listeners that the place of interest was renamed
            SharedResources.getInstance().getEventBus().fireEvent(new PlaceOfInterestRenamedEvent(oldName, newName));
        }

        /* if the new name is blank, we need to revalidate because this will cause validation
         * issues */
        if (StringUtils.isBlank(newName)) {
            for (Serializable validateObj : referencingSchemaObjects) {
                ScenarioEventUtility.fireDirtyEditorEvent(validateObj);
            }
        }
    }

    /**
     * Within the given concept, updates all references to the place of interest with the name <i>oldName</i>, 
     * making them dirty and causing them to be revalidated. 
     * <br/><br/>
     * If <i>replaceRefs</i> is true and <i>oldName</i> and <i>newName</i> are different, then this method 
     * will also replace all uses of <i>oldName</i> with <i>newName</i> and trigger a {@link PlaceOfInterestRenamedEvent}. 
     * If <i>replaceRefs</i> is false, then <i>newName</i> is ignored and the existing references are simply made dirty
     * so they can be revalidated.
     *
     * @param oldName the old (i.e. existing) place of interest name. If <i>replaceRefs</i> is true, this will be replaced
     * by <i>newName</i>. If null, this method will just return an empty set of conditions.
     * @param newName the new place of interest name to replace the old name with. Will not be used if <i>replaceRefs</i>
     * is false.
     * @param concept the concept to check for references. If null, this method will just return an empty set of conditions
     * @param replaceRefs whether to modify existing references to <i>oldName</i> to use <i>newName</i> instead. If false, then
     * the references will simply be made dirty and revalidated without changing any of the references.
     */
    private static Set<Condition> updateWaypointReferences(String oldName, String newName, Concept concept, boolean replaceRefs) {

        if (oldName == null || concept == null) {
            return new HashSet<Condition>();
        }

        Set<Condition> referencedConditions = new HashSet<Condition>();

        Serializable child = concept.getConditionsOrConcepts();
        if (child instanceof Conditions) {

            List<Condition> conditions = ((Conditions) child).getCondition();
            for (Condition condition : conditions) {

                Input conditionInput = condition.getInput();
                if (conditionInput == null) {
                    continue;
                }

                Serializable inputType = conditionInput.getType();
                if (inputType != null) {

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Updating " + inputType.getClass().getSimpleName() + " reference");
                    }

                    if (inputType instanceof AvoidLocationCondition) {
                        AvoidLocationCondition avoidLocationCondition = (AvoidLocationCondition) inputType;
                        List<PointRef> pointRefs = avoidLocationCondition.getPointRef();
                        for(PointRef pointRef : pointRefs){
                            if (pointRef == null) {
                                continue;
                            }

                            String ref = pointRef.getValue();
                            if (StringUtils.equals(oldName, ref)) {
                                if(replaceRefs) {
                                    pointRef.setValue(newName);
                                }
                                referencedConditions.add(condition);
                            }
                        }

                    } else if (inputType instanceof IdentifyPOIsCondition) {
                        IdentifyPOIsCondition identifyPoisCondition = (IdentifyPOIsCondition) inputType;
                        Pois pois = identifyPoisCondition.getPois();
                        if (pois == null) {
                            continue;
                        }
                        List<PointRef> pointRefs = pois.getPointRef();
                        for (PointRef pointRef : pointRefs) {

                            String ref = pointRef.getValue();
                            if (StringUtils.equals(oldName, ref)) {
                                if(replaceRefs) {
                                    pointRef.setValue(newName);
                                }
                                referencedConditions.add(condition);
                            }
                        }
                    } else if (inputType instanceof CheckpointPaceCondition) {
                        CheckpointPaceCondition checkpointPaceCondition = (CheckpointPaceCondition) inputType;
                        List<Checkpoint> checkpoints = checkpointPaceCondition.getCheckpoint();
                        for (Checkpoint checkpoint : checkpoints) {

                            String ref = checkpoint.getPoint();
                            if (StringUtils.equals(oldName, ref)) {
                                if(replaceRefs) {
                                    checkpoint.setPoint(newName);
                                }
                                referencedConditions.add(condition);
                            }
                        }
                    } else if (inputType instanceof CheckpointProgressCondition) {
                        CheckpointProgressCondition checkpointProgressCondition = (CheckpointProgressCondition) inputType;
                        List<Checkpoint> checkpoints = checkpointProgressCondition.getCheckpoint();
                        for (Checkpoint checkpoint : checkpoints) {

                            String ref = checkpoint.getPoint();
                            if (StringUtils.equals(oldName, ref)) {
                                if(replaceRefs) {
                                    checkpoint.setPoint(newName);
                                }
                                referencedConditions.add(condition);
                            }
                        }

                    } else if (inputType instanceof EnterAreaCondition) {
                        EnterAreaCondition enterAreaCondition = (EnterAreaCondition) inputType;
                        List<Entrance> entrances = enterAreaCondition.getEntrance();
                        for (Entrance entrance : entrances) {

                            if (entrance.getInside() != null) {
                                String ref = entrance.getInside().getPoint();
                                if (StringUtils.equals(oldName, ref)) {
                                    if(replaceRefs) {
                                        entrance.getInside().setPoint(newName);
                                    }
                                    referencedConditions.add(condition);
                                }
                            }

                            if (entrance.getOutside() != null) {
                                String ref = entrance.getOutside().getPoint();
                                if (StringUtils.equals(oldName, ref)) {
                                    if(replaceRefs) {
                                        entrance.getOutside().setPoint(newName);
                                    }
                                    referencedConditions.add(condition);
                                }
                            }
                        }
                        
                    } else if (inputType instanceof DetectObjectsCondition) {
                        DetectObjectsCondition conditionType = (DetectObjectsCondition) inputType;
                        ObjectsToDetect refObjs = conditionType.getObjectsToDetect();
                        if (refObjs == null) {
                            continue;
                        }
                        List<Serializable> refs = refObjs.getTeamMemberRefOrPointRef();
                        for (Serializable refObj : refs) {
                            if(refObj instanceof PointRef) {
                                
                                PointRef pointRef = (PointRef) refObj;
                                String ref = pointRef.getValue();
                                if (StringUtils.equals(oldName, ref)) {
                                    if(replaceRefs) {
                                        pointRef.setValue(newName);
                                    }
                                    referencedConditions.add(condition);
                                }
                            }
                        }
                        
                    } else if (inputType instanceof EngageTargetsCondition) {
                        EngageTargetsCondition conditionType = (EngageTargetsCondition) inputType;
                        TargetsToEngage refObjs = conditionType.getTargetsToEngage();
                        if (refObjs == null) {
                            continue;
                        }
                        List<Serializable> refs = refObjs.getTeamMemberRefOrPointRef();
                        for (Serializable refObj : refs) {
                            if(refObj instanceof PointRef) {
                                
                                PointRef pointRef = (PointRef) refObj;
                                String ref = pointRef.getValue();
                                if (StringUtils.equals(oldName, ref)) {
                                    if(replaceRefs) {
                                        pointRef.setValue(newName);
                                    }
                                    referencedConditions.add(condition);
                                }
                            }
                        }
                        
                    } else if (inputType instanceof NegligentDischargeCondition) {
                        NegligentDischargeCondition conditionType = (NegligentDischargeCondition) inputType;
                        TargetsToAvoid refObjs = conditionType.getTargetsToAvoid();
                        if (refObjs == null) {
                            continue;
                        }
                        List<Serializable> refs = refObjs.getTeamMemberRefOrPointRef();
                        for (Serializable refObj : refs) {
                            if(refObj instanceof PointRef) {
                                
                                PointRef pointRef = (PointRef) refObj;
                                String ref = pointRef.getValue();
                                if (StringUtils.equals(oldName, ref)) {
                                    if(replaceRefs) {
                                        pointRef.setValue(newName);
                                    }
                                    referencedConditions.add(condition);
                                }
                            }
                        }
                    } else if(inputType instanceof AssignedSectorCondition){
                        AssignedSectorCondition conditionType = (AssignedSectorCondition)inputType;
                        PointRef pointRef = conditionType.getPointRef();
                        if(pointRef != null){
                            String ref = pointRef.getValue();
                            if (StringUtils.equals(oldName, ref)) {
                                if(replaceRefs) {
                                    pointRef.setValue(newName);
                                }
                                referencedConditions.add(condition);
                            }
                        }
                        
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Finished updating reference");
                    }
                }
            }
        }

        return referencedConditions;
    }

    /**
     * Gets whether the current training application requires at least one learner ID in order to track learners' movements.
     * <i>Not to be confused with {@link #isLearnerStartLocationNeeded()}.</i>
     * <br/><br/>
     * Some training applications, like VBS need learner IDs  in order to determine which entity should be used to track
     * learners' movements. Other training applications, such as the GIFT mobile app, don't have any entities to track other than the
     * learner and, therefore, don't need learner IDs.
     * <br/><br/>
     * This method exists to differentiate between these different types of training applications so that learner IDs
     * are only required for validation when they are absolutely needed to track the learners' movements.
     *
     * @return true if the current training application requires at least one learner ID; false otherwise.
     */
    public static boolean isLearnerIdRequiredByApplication() {
        return TrainingApplicationEnum.UNITY_EMBEDDED.equals(ScenarioClientUtility.getTrainingAppType())
                || TrainingApplicationEnum.VBS.equals(ScenarioClientUtility.getTrainingAppType())
                || TrainingApplicationEnum.VR_ENGAGE.equals(ScenarioClientUtility.getTrainingAppType())
                || TrainingApplicationEnum.UNITY_DESKTOP.equals(ScenarioClientUtility.getTrainingAppType())
                || TrainingApplicationEnum.HAVEN.equals(ScenarioClientUtility.getTrainingAppType())
                || TrainingApplicationEnum.RIDE.equals(ScenarioClientUtility.getTrainingAppType());
    }
    
    /**
     * Return whether the current training application type supports handling feedback messages during
     * the real time assessment.
     *  
     * @return true if the current training application supports displaying feedback to the learner during
     * real time assessment.
     */
    public static boolean doesSupportInTrainingAppFeedback(){
        // the list that doesn't is much smaller
        return !(TrainingApplicationEnum.POWERPOINT.equals(ScenarioClientUtility.getTrainingAppType()) ||
                (TrainingApplicationEnum.ARES.equals(ScenarioClientUtility.getTrainingAppType())));
    }

    /**
     * Gets whether the current training application requires a learner start location in order to track a learner's movements
     * AND that start location has not been authored.
     * <i>Not to be confused with {@link #isLearnerIdRequiredByApplication()}.</i>
     * <br/><br/>
     * This method is basically a shorthand for calling {@link #isLearnerIdRequiredByApplication()} to see if a learner ID
     *  is required and then calling {@link #getLearnerStartLocationCoordinate()} to see if that start location has been authored.
     *
     * @return true if the current trainign application requires a learner start location; false otherwise.
     */
    public static boolean isLearnerStartLocationNeeded() {
        return isLearnerIdRequiredByApplication() && getLearnerStartLocationCoordinate() == null;
    }

    /**
     * Checks whether or not the scenario has the learner actions needed to start and end a pace count and adds those
     * learner actions if they don't exist.
     */
    public static void ensurePaceCountLearnerActionsExist() {

        //detect whether or not we need to add learner actions to start and end the pace count
        boolean hasStartPaceCountAction = false;
        boolean hasEndPaceCountAction = false;

        LearnerActionsList learnerActions = ScenarioClientUtility.getLearnerActions();

        if(learnerActions != null) {

            for(LearnerAction action : learnerActions.getLearnerAction()) {

                if(LearnerActionEnumType.START_PACE_COUNT.equals(action.getType())){

                    hasStartPaceCountAction = true;

                    if(hasEndPaceCountAction) {
                        break; //no need to keep looking, since we found both pace count actions
                    }

                } else if(LearnerActionEnumType.END_PACE_COUNT.equals(action.getType())){

                    hasEndPaceCountAction = true;

                    if(hasStartPaceCountAction) {
                        break; //no need to keep looking, since we found both pace count actions
                    }
                }
            }
        }

        //add a learner action to start the pace count if no such action exists
        if(!hasStartPaceCountAction) {

            LearnerAction newLearnerAction = new LearnerAction();
            newLearnerAction.setDisplayName("Start Pace Count");
            newLearnerAction.setType(LearnerActionEnumType.START_PACE_COUNT);

            ScenarioEventUtility.fireCreateScenarioObjectEvent(newLearnerAction);
        }

        //add a learner action to end the pace count if no such action exists
        if(!hasEndPaceCountAction) {

            LearnerAction newLearnerAction = new LearnerAction();
            newLearnerAction.setDisplayName("End Pace Count");
            newLearnerAction.setType(LearnerActionEnumType.END_PACE_COUNT);

            ScenarioEventUtility.fireCreateScenarioObjectEvent(newLearnerAction);
        }
    }

    /**
     * Checks if the current scenario does not have an 'Assess My Location' learner actions when there are AvoidLocationConditions
     * that need it. If the required learner action doesn't exist, it will be created.
     */
    public static void ensureAssessLocationLearnerActionExists() {

        //detect whether or not we need to add the appropriate learner action
        boolean hasAssessLocationAction = false;

        LearnerActionsList learnerActions = ScenarioClientUtility.getLearnerActions();

        if(learnerActions != null) {

            Iterator<LearnerAction> itr = learnerActions.getLearnerAction().iterator();
            while(itr.hasNext()) {

                LearnerAction action = itr.next();

                if(LearnerActionEnumType.ASSESS_MY_LOCATION.equals(action.getType())){

                    hasAssessLocationAction = true;
                    break;
                }
            }
        }

        if(!hasAssessLocationAction) {

            //if the assess location action should exist, add it when it doesn't
            LearnerAction newLearnerAction = new LearnerAction();
            newLearnerAction.setDisplayName("Assess My Location");
            newLearnerAction.setType(LearnerActionEnumType.ASSESS_MY_LOCATION);

            ScenarioEventUtility.fireCreateScenarioObjectEvent(newLearnerAction);
        }
    }

    /**
     * Checks if the current scenario still has 'Assess My Location' learner actions when there are no AvoidLocationConditions
     * capable of handling them. If it does, then the author will be asked whether they want to delete the learner actions.
     */
    public static void cleanUpAssessLocationLearnerAction() {

        for (Condition c : ScenarioClientUtility.getUnmodifiableConditionList()) {

            if (c.getInput() != null
                    && c.getInput().getType() instanceof AvoidLocationCondition) {

                AvoidLocationCondition alc = (AvoidLocationCondition) c.getInput().getType();

                if(alc.isRequireLearnerAction() != null && alc.isRequireLearnerAction()) {

                    //there are still Avoid Location Conditions that need the learner action, so don't remove it
                    return;
                }
            }
        }

        LearnerActionsList learnerActions = ScenarioClientUtility.getLearnerActions();
        final List<LearnerAction> actionsToCleanUp = new ArrayList<LearnerAction>();

        if(learnerActions != null) {

            for(LearnerAction action : learnerActions.getLearnerAction()) {

                if(LearnerActionEnumType.ASSESS_MY_LOCATION.equals(action.getType())){
                    actionsToCleanUp.add(action);
                }
            }
        }

        if(!actionsToCleanUp.isEmpty()) {

            /* Construct the prompt to present to the user */
            final String msg = "An 'Assess My Location' learner action still exists even though there are no more Avoid Location conditions "
                    + "using it. Would you like to delete this learner action?";

            final String title = "Delete Learner Action(s)?";

            /* Schedule deferred allows the currently shown dialog to be hidden
             * before reshowing it. */
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    OkayCancelDialog.show(title, msg, null, "Delete", "Keep", new OkayCancelCallback() {

                        @Override
                        public void okay() {

                            for (LearnerAction action : actionsToCleanUp) {
                                ScenarioEventUtility.fireDeleteScenarioObjectEvent(action, null);
                            }
                        }

                        @Override
                        public void cancel() {
                            // Nothing to do
                        }
                    });
                }
            });
        }
    }

    /**
     * Converts the coordinate location to an easily readable format.
     *
     * @param coord the coordinates of the entity location.
     * @return the coordinate location in an easily readable format. Returns an empty string if the
     *         coordinate type is unknown.
     */
    public static String prettyPrintCoordinate(Coordinate coord) {

        String prettyPrint = "";

        Serializable type = coord.getType();
        if (type instanceof GCC) {
            prettyPrint = gccStringifier.stringify((GCC) coord.getType());
        } else if (type instanceof GDC) {
            prettyPrint = gdcStringifier.stringify((GDC) coord.getType());
        } else if (type instanceof AGL) {
            prettyPrint = vbsaglStringifier.stringify((AGL) coord.getType());
        }

        return prettyPrint;
    }

    /**
     * Return the name of the place of interest provided.
     *
     * @param placeOfInterest a {@link Point}, {@link Path} or {@link Area}.
     * @return the name value, null if the place of interest type is not supported.
     */
    public static String getPlaceOfInterestName(Serializable placeOfInterest){

        String name = null;
        if(placeOfInterest != null){

            if(placeOfInterest instanceof Point){
                name = ((Point)placeOfInterest).getName();
            }else if(placeOfInterest instanceof Path){
                name = ((Path)placeOfInterest).getName();
            }else if(placeOfInterest instanceof Area){
                name = ((Area)placeOfInterest).getName();
            }
        }
        return name;
    }

    /**
     * Return the color of the place of interest provided.
     *
     * @param placeOfInterest a {@link Point}, {@link Path} or {@link Area}.
     * @return the color value, null if the place of interest type is not supported.
     */
    public static String getPlaceOfInterestColor(Serializable placeOfInterest){

        String color = null;
        if(placeOfInterest != null){

            if(placeOfInterest instanceof Point){
                color = ((Point)placeOfInterest).getColorHexRGBA();
            }else if(placeOfInterest instanceof Path){
                color = ((Path)placeOfInterest).getColorHexRGBA();
            }else if(placeOfInterest instanceof Area){
                color = ((Area)placeOfInterest).getColorHexRGBA();
            }
        }
        return color;
    }

    /**
     * Return true if the collection contains a place of interest with the given name.
     *
     * @param name the name to look for.  If null, false is returned.
     * @param placesOfInterest a collection of {@link Point}, {@link Path} and {@link Area}.
     * @return true if the collection contains the place of interest with the given name
     */
    public static boolean containsPlaceOfInterest(String name, List<Serializable> placesOfInterest){

        if(name == null){
            return false;
        }

        for(Serializable placeOfInterest : placesOfInterest){

            if(placeOfInterest instanceof Point && name.equals(((Point)placeOfInterest).getName())){
                return true;
            }else if(placeOfInterest instanceof Path && name.equals(((Path)placeOfInterest).getName())){
                return true;
            }else if(placeOfInterest instanceof Area && name.equals(((Area)placeOfInterest).getName())){
                return true;
            }
        }

        return false;
    }

    /**
     * Return a full URL to a map image hosted by GIFT.
     *
     * @param tileProperties contains the image path relative to a parent map/scenario folder. (e.g. overlay.png).
     * Can't be null.
     * @param scenarioFolderRelatviePath contains the path to the map/scenario folder.  Can't be null or empty.
     * (e.g. Public/LandNav_Standalone_HD/GIFT_Overlay_Resources/)
     * @return the full URL to access the hosted image file that shows a map/scenario.
     */
    public static String buildMapImageUrl(MapTileProperties tileProperties, String scenarioFolderRelatviePath) {

        if(tileProperties == null){
            throw new IllegalArgumentException("The tileProperties is null");
        }else if(StringUtils.isBlank(scenarioFolderRelatviePath)){
            throw new IllegalArgumentException("The scenario folder relative path is null or blank");
        }

        if (!scenarioFolderRelatviePath.endsWith("/")) {
            scenarioFolderRelatviePath += "/";
        }

        return GWT.getHostPageBaseURL() + "mapImage/" + scenarioFolderRelatviePath + tileProperties.getImageFilePath();
    }

    /**
     * Gets the scenario's team organization, which, if available, will always contain the root team.
     * <br/><br/>
     * Note that a default team organization will only be provided when the current
     * training application requires learner IDs, so this method will return null when the current training application
     * does not require learner IDs unless the DKF already had a team organization when it was loaded. For this reason,
     * this method should typically only be used in code that is accessible when {@link #isLearnerIdRequiredByApplication()}
     *  returns true.
     *
     * @return the scenario's team organization, or null if the current training application does not require learner IDs and
     * did not have a team organization when it was loaded.
     */
    public static TeamOrganization getTeamOrganization() {
        return scenario.getTeamOrganization();
    }

    /**
     * Gets the scenario's team organization's team.
     *
     * @return the organization team if available; null otherwise.
     */
    public static Team getTeamOrganizationTeam() {
        return getTeamOrganizationTeam(false);
    }
    
    /**
     * Gets the scenario's team organization's team.
     * 
     * @param createAndSet true if a new, schema valid, team organization with one learner should be created and added to the scenario
     * data model if a team organization doesn't exist already.
     * @return the organization team if available; if not available and createAndSet is true, than a new, schema valid team organization
     * with one learner will be created and added to the scenario data model, other null is returned.  
     */
    public static Team getTeamOrganizationTeam(boolean createAndSet){

        TeamOrganization teamOrg = getTeamOrganization();
        if(createAndSet && teamOrg == null){
            
            LearnerId learnerId = new LearnerId();
            learnerId.setType("learner");
            
            TeamMember teamMember = new TeamMember();
            teamMember.setName("Learner");
            teamMember.setLearnerId(learnerId);
            
            Team newTeam = generateNewTeam();
            newTeam.getTeamOrTeamMember().add(teamMember);
            
            teamOrg = new TeamOrganization();
            teamOrg.setTeam(newTeam);
            scenario.setTeamOrganization(teamOrg);
        }
        
        return teamOrg != null ? teamOrg.getTeam() : null;
    }

    /**
     * Gets whether or not the given team is the root team of the global team organization (see {@link #getTeamOrganization()}).
     * <br/><br/>
     * Note that this method will simply return false if the current training application does not use a team organization
     * (i.e. does not require learner IDs).
     *
     * @param team the team to check
     * @return true if the given team is the root team of the team organization. False, otherwise.
     */
    public static boolean isRootTeam(Team team) {

        if(getTeamOrganization() != null) {
            return team.equals(getTeamOrganization().getTeam());

        } else {
            return false;
        }
    }

    /**
     * Finds all of the strategies referencing teams and team members from the global team organization
     * and compiles a mapping of their references.
     * <br/><br/>
     * Note that this method will simply do nothing if the current training application does not use a team organization
     * (i.e. does not require learner IDs).
     */
    public static void gatherTeamReferences() {

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Updating team references");
        }

        teamReferences.clear();

        if(getTeamOrganization() != null) {

            for (String name : getAllTeamNames()) {

                if(name != null) {
                    teamReferences.put(name, new ArrayList<TeamReference>());
                }
            }

            //search strategies for team references
            for(Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {

                for(Serializable activity : strategy.getStrategyActivities()) {

                    if(activity instanceof InstructionalIntervention) {

                        InstructionalIntervention intervention = (InstructionalIntervention) activity;
                        if(intervention.getFeedback() != null) {
                            addTeamReferences(strategy, intervention, intervention.getFeedback().getTeamRef());
                        }

                    } else if(activity instanceof ScenarioAdaptation) {

                        EnvironmentAdaptation adaptation = ((ScenarioAdaptation) activity).getEnvironmentAdaptation();

                        if(adaptation.getType() instanceof FatigueRecovery) {

                            FatigueRecovery fatigue = (FatigueRecovery) adaptation.getType();

                            FatigueRecovery.TeamMemberRef memberRef = fatigue.getTeamMemberRef();
                            if(memberRef.getValue() != null) {

                                List<String> teamMemberRef = new ArrayList<>();
                                teamMemberRef.add(memberRef.getValue());

                                addTeamReferences(strategy, activity, teamMemberRef);
                            }

                        } else if(adaptation.getType() instanceof Teleport) {

                            Teleport teleport = (Teleport) adaptation.getType();

                            Teleport.TeamMemberRef memberRef = teleport.getTeamMemberRef();
                            if(memberRef.getValue() != null) {

                                List<String> teamMemberRef = new ArrayList<>();
                                teamMemberRef.add(memberRef.getValue());

                                addTeamReferences(strategy, activity, teamMemberRef);
                            }

                        } else if(adaptation.getType() instanceof Endurance) {

                            Endurance endurance = (Endurance) adaptation.getType();

                            Endurance.TeamMemberRef memberRef = endurance.getTeamMemberRef();
                            if(memberRef.getValue() != null) {

                                List<String> teamMemberRef = new ArrayList<>();
                                teamMemberRef.add(memberRef.getValue());

                                addTeamReferences(strategy, activity, teamMemberRef);
                            }
                        } else if(adaptation.getType() instanceof HighlightObjects){
                            
                            HighlightObjects highlight = (HighlightObjects)adaptation.getType();
                            if(highlight.getType() instanceof HighlightObjects.TeamMemberRef){
                                HighlightObjects.TeamMemberRef memberRef = (HighlightObjects.TeamMemberRef)highlight.getType();
                                if(memberRef.getValue() != null) {
    
                                    List<String> teamMemberRef = new ArrayList<>();
                                    teamMemberRef.add(memberRef.getValue());
    
                                    addTeamReferences(strategy, activity, teamMemberRef);
                        }
                    }
                        } else if(adaptation.getType() instanceof CreateBreadcrumbs){
                            
                            CreateBreadcrumbs breadcrumbs = (CreateBreadcrumbs)adaptation.getType();
                            for(CreateBreadcrumbs.TeamMemberRef memberRef : breadcrumbs.getTeamMemberRef()){

                                if(memberRef.getValue() != null) {
    
                                    List<String> teamMemberRef = new ArrayList<>();
                                    teamMemberRef.add(memberRef.getValue());
    
                                    addTeamReferences(strategy, activity, teamMemberRef);
                }
            }
                        }else if(adaptation.getType() instanceof RemoveBreadcrumbs){

                            RemoveBreadcrumbs removeBreadcrumbs = (RemoveBreadcrumbs)adaptation.getType();
                            for(RemoveBreadcrumbs.TeamMemberRef memberRef : removeBreadcrumbs.getTeamMemberRef()){

                                if(memberRef.getValue() != null) {
    
                                    List<String> teamMemberRef = new ArrayList<>();
                                    teamMemberRef.add(memberRef.getValue());
    
                                    addTeamReferences(strategy, activity, teamMemberRef);
                                }
                            }
                        }
                    }
                }
            }

            //search task triggers for team references
            for(Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {

                if(task.getStartTriggers() != null) {
                    for(StartTriggers.Trigger trigger :  task.getStartTriggers().getTrigger()) {

                        if(trigger.getTriggerType() instanceof EntityLocation) {

                            EntityLocation location = (EntityLocation) trigger.getTriggerType();

                            if(location.getEntityId() != null
                                    && location.getEntityId().getTeamMemberRefOrLearnerId() instanceof EntityId.TeamMemberRef) {

                                TeamMemberRef memberRef = (TeamMemberRef) location.getEntityId().getTeamMemberRefOrLearnerId();
                                if(memberRef.getValue() != null) {

                                    List<String> teamMemberRef = new ArrayList<>();
                                    teamMemberRef.add(memberRef.getValue());

                                    addTeamReferences(task, location, teamMemberRef);
                                }
                            }
                        }
                    }

                    for(EndTriggers.Trigger trigger :  task.getEndTriggers().getTrigger()) {
                        if(trigger.getTriggerType() instanceof EntityLocation) {

                            EntityLocation location = (EntityLocation) trigger.getTriggerType();

                            if(location.getEntityId() != null
                                    && location.getEntityId().getTeamMemberRefOrLearnerId() instanceof EntityId.TeamMemberRef) {

                                TeamMemberRef memberRef = (TeamMemberRef) location.getEntityId().getTeamMemberRefOrLearnerId();
                                if(memberRef.getValue() != null) {

                                    List<String> teamMemberRef = new ArrayList<>();
                                    teamMemberRef.add(memberRef.getValue());

                                    addTeamReferences(task, location, teamMemberRef);
                                }
                            }
                        }
                    }
                }
            }

            //search concepts for team references
            for (Concept concept : ScenarioClientUtility.getUnmodifiableConceptList()) {
                Serializable child = concept.getConditionsOrConcepts();
                if (child instanceof Conditions) {
                    List<Condition> conditions = ((Conditions) child).getCondition();
                    for (Condition condition : conditions) {
                        gatherTeamReferences(condition, concept);
                    }
                }
            }

            SharedResources.getInstance().getEventBus().fireEvent(new TeamReferencesUpdatedEvent());
        }

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished updating team references");
        }
    }

    /**
     * Within the given condition, finds all of the referenced team members and compiles a mapping of
     * their references.
     *
     * @param condition the condition with which to search for referenced team members. Can't be null.
     * @param parentConcept the concept that is the parent of the provided condition. Can't be null.
     */
    private static void gatherTeamReferences(Condition condition, Concept parentConcept) {
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        } else if (parentConcept == null) {
            throw new IllegalArgumentException("The parameter 'parentConcept' cannot be null.");
        }

        if (condition.getInput() == null) {
            return;
        }

        Serializable inputType = condition.getInput().getType();
        if (inputType != null) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding " + inputType.getClass().getSimpleName() + " team reference");
            }

            if (inputType instanceof AvoidLocationCondition) {

                AvoidLocationCondition input = (AvoidLocationCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof CheckpointPaceCondition) {

                CheckpointPaceCondition input = (CheckpointPaceCondition) inputType;

                if(input.getTeamMemberRef() != null) {

                    List<String> teamMemberRef = new ArrayList<>();
                    teamMemberRef.add(input.getTeamMemberRef());

                    addTeamReferences(
                            parentConcept,
                            condition,
                            teamMemberRef);
                }

            } else if (inputType instanceof CorridorBoundaryCondition) {

                CorridorBoundaryCondition input = (CorridorBoundaryCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof CheckpointProgressCondition) {

                CheckpointProgressCondition input = (CheckpointProgressCondition) inputType;

                if(input.getTeamMemberRef() != null) {

                    List<String> teamMemberRef = new ArrayList<>();
                    teamMemberRef.add(input.getTeamMemberRef());

                    addTeamReferences(
                            parentConcept,
                            condition,
                            teamMemberRef);
                }
            } else if (inputType instanceof CorridorPostureCondition) {

                CorridorPostureCondition input = (CorridorPostureCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }
                
            } else if (inputType instanceof DetectObjectsCondition) {

                DetectObjectsCondition input = (DetectObjectsCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof EliminateHostilesCondition) {

                EliminateHostilesCondition input = (EliminateHostilesCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof SpacingCondition) {

                SpacingCondition input = (SpacingCondition) inputType;

                List<String> teamMemberRef = new ArrayList<>();

                for(SpacingPair pair : input.getSpacingPair()) {

                    if(pair.getFirstObject() != null && pair.getFirstObject().getTeamMemberRef() != null) {
                        teamMemberRef.add(pair.getFirstObject().getTeamMemberRef());
                    }

                    if(pair.getSecondObject() != null && pair.getSecondObject().getTeamMemberRef() != null) {
                        teamMemberRef.add(pair.getSecondObject().getTeamMemberRef());
                    }
                }

                if(teamMemberRef.isEmpty()) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            teamMemberRef);
                }

            } else if (inputType instanceof EnterAreaCondition) {

                EnterAreaCondition input = (EnterAreaCondition) inputType;

                if(input.getTeamMemberRef() != null) {

                    List<String> teamMemberRef = new ArrayList<>();
                    teamMemberRef.add(input.getTeamMemberRef());

                    addTeamReferences(
                            parentConcept,
                            condition,
                            teamMemberRef);
                }

            } else if (inputType instanceof ExplosiveHazardSpotReportCondition) {

                ExplosiveHazardSpotReportCondition input = (ExplosiveHazardSpotReportCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof HaltConditionInput) {

                HaltConditionInput input = (HaltConditionInput) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof IdentifyPOIsCondition) {

                IdentifyPOIsCondition input = (IdentifyPOIsCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof LifeformTargetAccuracyCondition) {

                LifeformTargetAccuracyCondition input = (LifeformTargetAccuracyCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }
                
            } else if (inputType instanceof NegligentDischargeCondition) {

                NegligentDischargeCondition input = (NegligentDischargeCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof NineLineReportCondition) {

                NineLineReportCondition input = (NineLineReportCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof RulesOfEngagementCondition) {

                RulesOfEngagementCondition input = (RulesOfEngagementCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof SpeedLimitCondition) {

                SpeedLimitCondition input = (SpeedLimitCondition) inputType;

                if(input.getTeamMemberRef() != null) {

                    List<String> teamMemberRef = new ArrayList<>();
                    teamMemberRef.add(input.getTeamMemberRef());

                    addTeamReferences(
                            parentConcept,
                            condition,
                            teamMemberRef);
                }

            } else if (inputType instanceof SpotReportCondition) {

                SpotReportCondition input = (SpotReportCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if (inputType instanceof UseRadioCondition) {

                UseRadioCondition input = (UseRadioCondition) inputType;

                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }

            } else if(inputType instanceof EngageTargetsCondition){
                
                EngageTargetsCondition input = (EngageTargetsCondition)inputType;
                
                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }
                
            } else if(inputType instanceof FireTeamRateOfFireCondition){
                
                FireTeamRateOfFireCondition input = (FireTeamRateOfFireCondition)inputType;
                
                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }
                
            } else if(inputType instanceof AssignedSectorCondition){
                
                AssignedSectorCondition input = (AssignedSectorCondition)inputType;
                
                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }
                
            } else if(inputType instanceof HealthConditionInput){
                
                HealthConditionInput input = (HealthConditionInput)inputType;
                
                if(input.getTeamMemberRefs() != null) {
                    addTeamReferences(
                            parentConcept,
                            condition,
                            input.getTeamMemberRefs().getTeamMemberRef());
                }
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Finished adding team reference");
            }
        }
    }

    /**
     * Gets all of the team and team member names in the global team organization.
     * <br/><br/>
     * Note that this method will simply return null if the current training application does not use a team organization
     * (i.e. does not require learner IDs).
     *
     * @return all of the team and team member names, or null if the current training application does not use a team organization
     */
    public static List<String> getAllTeamNames() {

        if(getTeamOrganization() != null) {
            return getAllTeamNames(getTeamOrganization().getTeam());

        } else {
            return null;
        }
    }

    /**
     * Gets all of the team and team member names within the given team
     *
     * @param team the team within which to get team and team member names
     * @return all of the team and team member names
     */
    private static List<String> getAllTeamNames(Team team) {

        List<String> names = new ArrayList<>();

        if(team != null) {

            names.add(team.getName());

            for(Serializable unit : team.getTeamOrTeamMember()) {

                if(unit instanceof TeamMember) {
                    names.add(((TeamMember) unit).getName());

                } else if(unit instanceof Team) {
                    names.addAll(getAllTeamNames((Team) unit));
                }
            }
        }

        return names;
    }

    /**
     * Creates a mapping of the given strategy activity to the team it references
     *
     * @param strategy the condition's parent strategy. Used to display strategy names to authors so
     *        they can better identify activities.
     * @param activity the activity where a reference was found
     * @param references the referenced teams
     */
    private static void addTeamReferences(Strategy strategy, Serializable activity, List<TeamRef> references) {

        if (activity == null) {
            return;
        }

        if (references == null) {
            return;
        }

        for(TeamRef ref : references) {

            //update the reference mapping of each team or team member referenced by this activity
            List<TeamReference> teamRefs = getReferencesToTeam(ref.getValue());
            if(teamRefs != null) {

                boolean refMappingExists = false;

                for(TeamReference teamRef : teamRefs) {

                    if (activity.equals(teamRef.getReferenceObject())) {

                        // this activity already references this team or team member, so increment the
                        // number of  references
                        teamRef.incrementReferences();
                        refMappingExists = true;
                        break;
                    }
                }

                if(!refMappingExists) {

                    // no references to this team or team member have been found in this activity yet,
                    // so add a new reference
                    teamRefs.add(new TeamReference(strategy, activity));
                }
            }
        }
    }

    /**
     * Creates a mapping of the given schema object to the team it references
     *
     * @param parent the parent of the reference object. Used to display unique names associated with the reference object's
     *        nearest named parent so that authors can better identify where the referencing object is in the scenario.
     * @param referenceObject the schema object where a reference was found
     * @param references the referenced teams
     */
    private static void addTeamReferences(Serializable parent, Serializable referenceObject, List<String> references) {

        if (referenceObject == null) {
            return;
        }

        if (references == null) {
            return;
        }

        for(String ref : references) {

            //update the reference mapping of each team or team member referenced by this object
            List<TeamReference> teamRefs = getReferencesToTeam(ref);
            if(teamRefs != null) {

                boolean refMappingExists = false;

                for(TeamReference teamRef : teamRefs) {

                    if (referenceObject.equals(teamRef.getReferenceObject())) {

                        // this obhect already references this team or team member, so increment the
                        // number of  references
                        teamRef.incrementReferences();
                        refMappingExists = true;
                        break;
                    }
                }

                if(!refMappingExists) {

                    // no references to this team or team member have been found in this object yet,
                    // so add a new reference
                    teamRefs.add(new TeamReference(parent, referenceObject));
                }
            }
        }
    }

    /**
     * Updates team member entity marking references in different parts of the DKF in memory data model.
     * 
     * @param teamMemberName the name of the team member whose entity marking is being updated.  If null or empty this 
     * method does nothing.
     * @param newEntityMarking the new entity marking for that team member.
     */
    public static void updateTeamMemberEntityMarkingReferences(String teamMemberName, String newEntityMarking){
        
        if(StringUtils.isBlank(teamMemberName)){
            return;
        }
        
        //update references in strategies
        Set<Serializable> referencingSchemaObjects = new HashSet<Serializable>();
        for(Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {
            updateTeamMemberEntityMarkingReferences(strategy, teamMemberName, newEntityMarking, referencingSchemaObjects);            
        }
        
        //update references in task triggers
        for(Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {

            if(task.getStartTriggers() != null) {
                for(StartTriggers.Trigger trigger :  task.getStartTriggers().getTrigger()) {
                    
                    if(trigger.getTriggerMessage() != null){
                        updateTeamMemberEntityMarkingReferences(trigger.getTriggerMessage().getStrategy(), teamMemberName, newEntityMarking, referencingSchemaObjects);
                    }
                }

                for(EndTriggers.Trigger trigger :  task.getEndTriggers().getTrigger()) {
                    
                    if(trigger.getMessage() != null){
                        updateTeamMemberEntityMarkingReferences(trigger.getMessage().getStrategy(), teamMemberName, newEntityMarking, referencingSchemaObjects);
                    }
                }
            }
        }
        
        for (Serializable validateObj : referencingSchemaObjects) {
            ScenarioEventUtility.fireDirtyEditorEvent(validateObj);
        }

    }
    
    /**
     * Updates team member entity marking references in the strategy provided.
     * 
     * @param strategy the strategy to check activities in and update entity marking references if the team member reference is found. If
     * null this method does nothing
     * @param teamMemberName the name of the team member whose entity marking is being updated.  If null or empty this 
     * method does nothing.
     * @param newEntityMarking the new entity marking for that team member.
     * @param referencingSchemaObjects collection of activities that had their entity marking updated.  Useful for creating object dirty events.
     * Can't be null.
     */
    private static void updateTeamMemberEntityMarkingReferences(Strategy strategy, String teamMemberName, String newEntityMarking, Set<Serializable> referencingSchemaObjects){
        
        if(StringUtils.isBlank(teamMemberName)){
            return;
        }else if(strategy == null){
            return;
        }
        
        for(Serializable activity : strategy.getStrategyActivities()) {

            if(activity instanceof ScenarioAdaptation) {

               EnvironmentAdaptation adaptation = ((ScenarioAdaptation) activity).getEnvironmentAdaptation();

               if(adaptation.getType() instanceof FatigueRecovery) {

                   FatigueRecovery fatigue = (FatigueRecovery) adaptation.getType();

                   FatigueRecovery.TeamMemberRef memberRef = fatigue.getTeamMemberRef();

                   if(memberRef != null && StringUtils.equals(memberRef.getValue(), teamMemberName)) {
                       
                       if(StringUtils.isNotBlank(newEntityMarking)) {
                           memberRef.setEntityMarking(newEntityMarking); //replace old name reference with new name

                       } else {
                           memberRef.setEntityMarking(null); //new name is empty, so remove reference
                       }

                       referencingSchemaObjects.add(fatigue);
                   }

               } else if(adaptation.getType() instanceof Teleport) {

                   Teleport teleport = (Teleport) adaptation.getType();

                   Teleport.TeamMemberRef memberRef = teleport.getTeamMemberRef();

                   if(memberRef != null && StringUtils.equals(memberRef.getValue(), teamMemberName)) {

                       if(StringUtils.isNotBlank(newEntityMarking)) {
                           memberRef.setEntityMarking(newEntityMarking); //replace old name reference with new name

                       } else {
                           memberRef.setEntityMarking(null); //new name is empty, so remove reference
                       }

                       referencingSchemaObjects.add(teleport);
                   }

               } else if(adaptation.getType() instanceof Endurance) {

                   Endurance endurance = (Endurance) adaptation.getType();

                   Endurance.TeamMemberRef memberRef = endurance.getTeamMemberRef();

                   if(memberRef != null && StringUtils.equals(memberRef.getValue(), teamMemberName)) {

                       if(StringUtils.isNotBlank(newEntityMarking)) {
                           memberRef.setEntityMarking(newEntityMarking); //replace old name reference with new name

                       } else {
                           memberRef.setEntityMarking(null); //new name is empty, so remove reference
                       }

                       referencingSchemaObjects.add(endurance);
                   }
               } else if(adaptation.getType() instanceof HighlightObjects){
                   
                   HighlightObjects highlight = (HighlightObjects)adaptation.getType();
                   
                   if(highlight.getType() instanceof EnvironmentAdaptation.HighlightObjects.TeamMemberRef){
                       
                       EnvironmentAdaptation.HighlightObjects.TeamMemberRef memberRef = 
                               (EnvironmentAdaptation.HighlightObjects.TeamMemberRef)highlight.getType();
                       
                       if(memberRef != null && StringUtils.equals(memberRef.getValue(), teamMemberName)) {

                           if(StringUtils.isNotBlank(newEntityMarking)) {
                               memberRef.setEntityMarking(newEntityMarking); //replace old name reference with new name

                           } else {
                               memberRef.setEntityMarking(null); //new name is empty, so remove reference
                           }

                           referencingSchemaObjects.add(highlight);
                       }
                   }
               } else if(adaptation.getType() instanceof CreateBreadcrumbs){
                   
                   CreateBreadcrumbs breadcrumbs = (CreateBreadcrumbs)adaptation.getType();
                   
                   for(generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef memberRef : breadcrumbs.getTeamMemberRef()){

                       if(memberRef != null && StringUtils.equals(memberRef.getValue(), teamMemberName)) {

                           if(StringUtils.isNotBlank(newEntityMarking)) {
                               memberRef.setEntityMarking(newEntityMarking); //replace old name reference with new name

                           } else {
                               memberRef.setEntityMarking(null); //new name is empty, so remove reference
                           }

                           referencingSchemaObjects.add(breadcrumbs);
                       }
                   }

               } else if(adaptation.getType() instanceof RemoveBreadcrumbs){
                   
                   RemoveBreadcrumbs removeBreadcrumbs = (RemoveBreadcrumbs)adaptation.getType();
                   
                   for(generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef memberRef : removeBreadcrumbs.getTeamMemberRef()){
                       
                       if(memberRef != null && StringUtils.equals(memberRef.getValue(), teamMemberName)) {

                           if(StringUtils.isNotBlank(newEntityMarking)) {
                               memberRef.setEntityMarking(newEntityMarking); //replace old name reference with new name

                           } else {
                               memberRef.setEntityMarking(null); //new name is empty, so remove reference
                           }

                           referencingSchemaObjects.add(removeBreadcrumbs);
                       }
                   }

               }
           }
       }
    }

    /**
     * Updates all references to the team or team member with the old name to use the new name instead
     *
     * @param oldTeamReference the old team or team member name to replace (not entity marking attribute).  If null or empty this
     * method does nothing.
     * @param newTeamReference the new team or team member name to use (not entity marking attribute).  If the same as oldTeamReference 
     * this method does nothing.
     */
    public static void updateTeamReferences(String oldName, String newName) {

        // if the old name is blank or if the old and new values are the same, do nothing.
        if (StringUtils.isBlank(oldName) || StringUtils.equals(oldName, newName)) {
            return;
        }

        //update references in strategies
        Set<Serializable> referencingSchemaObjects = new HashSet<Serializable>();
        for(Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {
            updateTeamReference(strategy, oldName, newName, referencingSchemaObjects);            
        }

        //update references in task triggers
        for(Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {

            if(task.getStartTriggers() != null) {
                for(StartTriggers.Trigger trigger :  task.getStartTriggers().getTrigger()) {

                    if(trigger.getTriggerType() instanceof EntityLocation) {

                        EntityLocation location = (EntityLocation) trigger.getTriggerType();
                        if(updateTeamReference(location, oldName, newName)){
                            referencingSchemaObjects.add(location);
                        }
                    }
                    
                    if(trigger.getTriggerMessage() != null){
                        updateTeamReference(trigger.getTriggerMessage().getStrategy(), oldName, newName, referencingSchemaObjects);
                    }
                }

                for(EndTriggers.Trigger trigger :  task.getEndTriggers().getTrigger()) {
                    if(trigger.getTriggerType() instanceof EntityLocation) {

                        EntityLocation location = (EntityLocation) trigger.getTriggerType();

                        if(updateTeamReference(location, oldName, newName)){
                            referencingSchemaObjects.add(location);
                        }
                    }
                    
                    if(trigger.getMessage() != null){
                        updateTeamReference(trigger.getMessage().getStrategy(), oldName, newName, referencingSchemaObjects);
                    }
                }
            }
        }

        //update references in conditions
        for (Concept concept : getUnmodifiableConceptList()) {
            referencingSchemaObjects.addAll(updateTeamReferences(oldName, newName, concept));
        }

        // references have been updated, so gather the global references
        ScenarioClientUtility.gatherTeamReferences();

        // notify listeners that the place of interest was renamed
        SharedResources.getInstance().getEventBus().fireEvent(new TeamRenamedEvent(oldName, newName));

        /* if the new name is blank, we need to revalidate because this will cause validation
         * issues */
        if (StringUtils.isBlank(newName)) {

            for (Serializable validateObj : referencingSchemaObjects) {
                ScenarioEventUtility.fireDirtyEditorEvent(validateObj);
            }
        }
    }
    
    /**
     * Updates all references to the team or team member with the old name to use the new name instead in the strategy's 
     * activities
     *
     * @param strategy the strategy to check the activities in and update the team references if the old reference is found.
     * If null this method does nothing.
     * @param oldTeamReference the old team or team member name to replace (not entity marking attribute).  If null or empty this
     * method does nothing.
     * @param newTeamReference the new team or team member name to use (not entity marking attribute).  If the same as oldTeamReference 
     * this method does nothing.
     * @param referencingSchemaObjects collection of activities that had their team reference updated.  Useful for creating object dirty events.
     * Can't be null.
     */
    private static void updateTeamReference(Strategy strategy, String oldTeamReference, String newTeamReference, Set<Serializable> referencingSchemaObjects){
        
        if(strategy == null){
            return;
        }
        
        // if the old name is blank or if the old and new values are the same, do nothing.
        if (StringUtils.isBlank(oldTeamReference) || StringUtils.equals(oldTeamReference, newTeamReference)) {
            return;
        }
        
            for(Serializable activity : strategy.getStrategyActivities()) {

                if(activity instanceof InstructionalIntervention) {

                    InstructionalIntervention intervention = (InstructionalIntervention) activity;
                    if(intervention.getFeedback() != null ) {

                        Iterator<TeamRef> itr = intervention.getFeedback().getTeamRef().iterator();
                        while(itr.hasNext()) {

                            TeamRef ref = itr.next();

                        if(StringUtils.equals(ref.getValue(), oldTeamReference)) {

                            if(StringUtils.isNotBlank(newTeamReference)) {
                                ref.setValue(newTeamReference); //replace old name reference with new name

                                } else {
                                    itr.remove(); //new name is empty, so remove reference
                                }

                                referencingSchemaObjects.add(intervention);
                            }
                        }
                    }

                } else if(activity instanceof ScenarioAdaptation) {

                    EnvironmentAdaptation adaptation = ((ScenarioAdaptation) activity).getEnvironmentAdaptation();

                    if(adaptation.getType() instanceof FatigueRecovery) {

                        FatigueRecovery fatigue = (FatigueRecovery) adaptation.getType();

                        FatigueRecovery.TeamMemberRef memberRef = fatigue.getTeamMemberRef();

                    if(memberRef != null && StringUtils.equals(memberRef.getValue(), oldTeamReference)) {

                        if(StringUtils.isNotBlank(newTeamReference)) {
                            memberRef.setValue(newTeamReference); //replace old name reference with new name

                            } else {
                                memberRef.setValue(null); //new name is empty, so remove reference
                            }

                            referencingSchemaObjects.add(fatigue);
                        }

                    } else if(adaptation.getType() instanceof Teleport) {

                        Teleport teleport = (Teleport) adaptation.getType();

                        Teleport.TeamMemberRef memberRef = teleport.getTeamMemberRef();

                    if(memberRef != null && StringUtils.equals(memberRef.getValue(), oldTeamReference)) {

                        if(StringUtils.isNotBlank(newTeamReference)) {
                            memberRef.setValue(newTeamReference); //replace old name reference with new name

                            } else {
                                memberRef.setValue(null); //new name is empty, so remove reference
                            }

                            referencingSchemaObjects.add(teleport);
                        }

                    } else if(adaptation.getType() instanceof Endurance) {

                        Endurance endurance = (Endurance) adaptation.getType();

                        Endurance.TeamMemberRef memberRef = endurance.getTeamMemberRef();

                    if(memberRef != null && StringUtils.equals(memberRef.getValue(), oldTeamReference)) {

                        if(StringUtils.isNotBlank(newTeamReference)) {
                            memberRef.setValue(newTeamReference); //replace old name reference with new name

                            } else {
                                memberRef.setValue(null); //new name is empty, so remove reference
                            }

                            referencingSchemaObjects.add(endurance);
                        }
                } else if(adaptation.getType() instanceof HighlightObjects){

                    HighlightObjects highlight = (HighlightObjects)adaptation.getType();

                    if(highlight.getType() instanceof EnvironmentAdaptation.HighlightObjects.TeamMemberRef){

                        EnvironmentAdaptation.HighlightObjects.TeamMemberRef memberRef = 
                                (EnvironmentAdaptation.HighlightObjects.TeamMemberRef)highlight.getType();

                        if(memberRef != null && StringUtils.equals(memberRef.getValue(), oldTeamReference)) {

                            if(StringUtils.isNotBlank(newTeamReference)) {
                                memberRef.setValue(newTeamReference); //replace old name reference with new name

                            } else {
                                memberRef.setValue(null); //new name is empty, so remove reference
                            }

                            referencingSchemaObjects.add(highlight);
                        }
                    }
                } else if(adaptation.getType() instanceof CreateBreadcrumbs){

                    CreateBreadcrumbs breadcrumbs = (CreateBreadcrumbs)adaptation.getType();

                    for(generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef memberRef : breadcrumbs.getTeamMemberRef()){

                        if(memberRef != null && StringUtils.equals(memberRef.getValue(), oldTeamReference)) {

                            if(StringUtils.isNotBlank(newTeamReference)) {
                                memberRef.setValue(newTeamReference); //replace old name reference with new name

                                } else {
                                    memberRef.setValue(null); //new name is empty, so remove reference
                                }

                            referencingSchemaObjects.add(breadcrumbs);
                            }
                        }

                } else if(adaptation.getType() instanceof RemoveBreadcrumbs){

                    RemoveBreadcrumbs removeBreadcrumbs = (RemoveBreadcrumbs)adaptation.getType();

                    for(generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef memberRef : removeBreadcrumbs.getTeamMemberRef()){

                        if(memberRef != null && StringUtils.equals(memberRef.getValue(), oldTeamReference)) {

                            if(StringUtils.isNotBlank(newTeamReference)) {
                                memberRef.setValue(newTeamReference); //replace old name reference with new name

                                } else {
                                    memberRef.setValue(null); //new name is empty, so remove reference
                                }

                            referencingSchemaObjects.add(removeBreadcrumbs);
                            }
                        }

                    }
                }
            }
        }

    /**
     * Updates all references to the team or team member with the old name to use the new name instead in the EntityLocation 
     * object.
     *
     * @param location the EntityLocation to check for team member reference and update the team references if the old reference is found.
     * If null this method does nothing and returns false.
     * @param oldTeamReference the old team or team member name to replace (not entity marking attribute).  If null or empty this
     * method does nothing and returns false.
     * @param newTeamReference the new team or team member name to use (not entity marking attribute).  If the same as oldTeamReference 
     * this method does nothing and returns false.
     * @return true if the location object had it's team member reference updated, false otherwise.
     */
    private static boolean updateTeamReference(EntityLocation location, String oldTeamReference, String newTeamReference){
        
        if(location == null){
            return false;
        }

        // if the old name is blank or if the old and new values are the same, do nothing.
        if (StringUtils.isBlank(oldTeamReference) || StringUtils.equals(oldTeamReference, newTeamReference)) {
            return false;
        }

        if(location.getEntityId() != null
                && location.getEntityId().getTeamMemberRefOrLearnerId() instanceof EntityId.TeamMemberRef) {

            TeamMemberRef memberRef = (TeamMemberRef) location.getEntityId().getTeamMemberRefOrLearnerId();

            if(StringUtils.equals(memberRef.getValue(), oldTeamReference)) {

                if(StringUtils.isNotBlank(newTeamReference)) {
                    memberRef.setValue(newTeamReference); //replace old name reference with new name

                } else {
                    memberRef.setValue(null); //new name is empty, so remove reference
            }

                return true;
        }
    }

        return false;
    }

    /**
     * Within the given concept, updates all references to the team or team
     * member with the old name to use the new name instead
     *
     * @param oldName the old team or team member name to replace
     * @param newName the new team or team member name to use
     * @param concept the concept to check for references
     * @return the referenced {@link Condition conditions} that were updated
     *         with the new name
     */
    private static Set<Condition> updateTeamReferences(String oldName, String newName, Concept concept) {

        if (oldName == null || concept == null) {
            return new HashSet<Condition>();
        }

        Set<Condition> referencedConditions = new HashSet<Condition>();

        Serializable child = concept.getConditionsOrConcepts();
        if (child instanceof Conditions) {

            List<Condition> conditions = ((Conditions) child).getCondition();
            for (Condition condition : conditions) {

                Input conditionInput = condition.getInput();
                if (conditionInput == null) {
                    continue;
                }

                Serializable inputType = conditionInput.getType();
                if (inputType != null) {

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Updating " + inputType.getClass().getSimpleName() + " team reference");
                    }

                    if (inputType instanceof AvoidLocationCondition) {

                        AvoidLocationCondition input = (AvoidLocationCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof CheckpointPaceCondition) {

                        CheckpointPaceCondition input = (CheckpointPaceCondition) inputType;

                        if(input.getTeamMemberRef() != null) {

                            if(StringUtils.equals(input.getTeamMemberRef(), oldName)) {
                                input.setTeamMemberRef(newName);
                                referencedConditions.add(condition);
                            }
                        }

                    } else if (inputType instanceof CheckpointProgressCondition) {

                        CheckpointProgressCondition input = (CheckpointProgressCondition) inputType;

                        if(StringUtils.equals(input.getTeamMemberRef(), oldName)) {
                            input.setTeamMemberRef(newName);
                            referencedConditions.add(condition);
                        }

                    } else if (inputType instanceof CorridorBoundaryCondition) {

                        CorridorBoundaryCondition input = (CorridorBoundaryCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof CorridorPostureCondition) {

                        CorridorPostureCondition input = (CorridorPostureCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof EliminateHostilesCondition) {

                        EliminateHostilesCondition input = (EliminateHostilesCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof EnterAreaCondition) {

                        EnterAreaCondition input = (EnterAreaCondition) inputType;

                        if(input.getTeamMemberRef() != null) {

                            if(StringUtils.equals(input.getTeamMemberRef(), oldName)) {
                                input.setTeamMemberRef(newName);
                                referencedConditions.add(condition);
                            }
                        }

                    } else if (inputType instanceof ExplosiveHazardSpotReportCondition) {

                        ExplosiveHazardSpotReportCondition input = (ExplosiveHazardSpotReportCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof HaltConditionInput) {

                        HaltConditionInput input = (HaltConditionInput) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof IdentifyPOIsCondition) {

                        IdentifyPOIsCondition input = (IdentifyPOIsCondition) inputType;

                        if (input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while (index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof LifeformTargetAccuracyCondition) {

                        LifeformTargetAccuracyCondition input = (LifeformTargetAccuracyCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof NineLineReportCondition) {

                        NineLineReportCondition input = (NineLineReportCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof ObservedAssessmentCondition) {

                        ObservedAssessmentCondition input = (ObservedAssessmentCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof PaceCountCondition) {

                        PaceCountCondition input = (PaceCountCondition) inputType;

                        if (input.getTeamMemberRef() != null && StringUtils.equals(input.getTeamMemberRef(), oldName)) {
                            input.setTeamMemberRef(newName);
                            referencedConditions.add(condition);
                        }

                    } else if (inputType instanceof RulesOfEngagementCondition) {

                        RulesOfEngagementCondition input = (RulesOfEngagementCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof SpacingCondition) {

                        SpacingCondition input = (SpacingCondition) inputType;

                        boolean conditionChanged = false;

                        for (SpacingPair pair : input.getSpacingPair()) {

                            if (pair.getFirstObject() != null
                                    && StringUtils.equals(pair.getFirstObject().getTeamMemberRef(), oldName)) {
                                pair.getFirstObject().setTeamMemberRef(newName);
                                conditionChanged = true;
                            }

                            if (pair.getSecondObject() != null
                                    && StringUtils.equals(pair.getSecondObject().getTeamMemberRef(), oldName)) {
                                pair.getSecondObject().setTeamMemberRef(newName);
                                conditionChanged = true;
                            }
                        }

                        if (conditionChanged) {
                            referencedConditions.add(condition);
                        }

                    } else if (inputType instanceof SpeedLimitCondition) {

                        SpeedLimitCondition input = (SpeedLimitCondition) inputType;

                        if(input.getTeamMemberRef() != null) {

                            if(StringUtils.equals(input.getTeamMemberRef(), oldName)) {
                                input.setTeamMemberRef(newName);
                                referencedConditions.add(condition);
                            }
                        }

                    } else if (inputType instanceof SpotReportCondition) {

                        SpotReportCondition input = (SpotReportCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }

                    } else if (inputType instanceof TimerConditionInput) {

                        TimerConditionInput input = (TimerConditionInput) inputType;

                        if (input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while (index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }
                    } else if (inputType instanceof UseRadioCondition) {

                        UseRadioCondition input = (UseRadioCondition) inputType;

                        if(input.getTeamMemberRefs() != null) {

                            int index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);

                            while(index != -1) {

                                input.getTeamMemberRefs().getTeamMemberRef().set(index, newName);
                                referencedConditions.add(condition);

                                index = input.getTeamMemberRefs().getTeamMemberRef().indexOf(oldName);
                            }
                        }
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Finished updating team reference");
                    }
                }
            }
        }

        return referencedConditions;
    }

    /**
     * Gets the names of all the team members that can be found among the teams and team members with the given names.
     * Teams with any of the given names will be deeply searched to see if any of their children are team members
     * or have any child team members.
     * <br/><br/>
     * Note that this method will simply return null if the current training application does not use a team organization
     * (i.e. does not require learner IDs).
     *
     * @param teamNames the team and team member names to search
     * @return the team members found among the teams and team members with the given names, or null if the current
     * training application does not use a team organization
     */
    public static List<String> getTeamMemberNames(List<String> teamNames) {

        if(getTeamOrganization() != null) {
            return getTeamMemberNames(teamNames != null ? new ArrayList<>(teamNames) : null, getTeamOrganization().getTeam(), false);

        } else {
            return null;
        }
    }

    /**
     * Gets the names of all the team members that can be found among the teams and team members with the given names.
     * Teams with any of the given names will be deeply searched to see if any of their children are team members
     * or have any child team members.
     *
     * @param teamNames the team and team member names to search
     * @param team the root team to start with
     * @param hasAncestorWithName whether the root team has any ancestors with one of the given names
     * @return the team members found among the teams and team members with the given names
     */
    private static List<String> getTeamMemberNames(List<String> teamNames, Team team, boolean hasAncestorWithName) {
        return TeamsUtil.getTeamMemberNames(teamNames, team, hasAncestorWithName);
    }

    /**
     * Searches through the team organization and returns the first team member name found within it. Can be used to determine if
     * any team member names are available.
     *
     * @return the first team member name found, or null if no team members are found
     */
    public static String getAnyTeamMemberName() {

        if(getTeamOrganization() != null) {
            return getAnyTeamMemberName(getTeamOrganization().getTeam());
        }

        return null;
    }

    /**
     * Searches through the given team and returns the first team member name found with it.
     *
     * @param team the team within which to search for a team member name
     * @return the first team member name found, or null if no team members are found
     */
    public static String getAnyTeamMemberName(Team team) {
        return TeamsUtil.getAnyTeamMemberName(team);
    }

    /**
     * Gets the team member with the given name in the scenario's team organization
     *
     * @param name the name of the team member to get
     * @return the team member with the given name, or null, if no such team member exists
     */
    public static TeamMember getTeamMemberWithName(String name) {

        if(getTeamOrganization() != null) {
            return getTeamMemberWithName(name, getTeamOrganization().getTeam());
        }

        return null;
    }

    /**
     * Gets the team member with the given name in the given team
     *
     * @param name the name of the team member to get
     * @param team the team within which to look for the team member
     * @return the team member with the given name, or null, if no such team member exists
     */
    private static TeamMember getTeamMemberWithName(String name, Team team) {
        return (TeamMember) TeamsUtil.getTeamOrgEntityWithName(name, team, false);
    }

    /**
     * Gets the team member with the given name in the scenario's team organization
     *
     * @param names the names of the team members to get
     * @return the team member with the given name, or null, if no such team member exists
     */
    public static List<TeamMember> getTeamMembersWithNames(List<String> names) {

        if(getTeamOrganization() != null) {
            return getTeamMembersWithNames(names != null ? new ArrayList<>(names) : null, getTeamOrganization().getTeam());
        }

        return null;
    }

    /**
     * Gets the team member with the given name in the given team
     *
     * @param names the names of the team members to get
     * @param team the team within which to look for the team member
     * @return the team member with the given name, or null, if no such team member exists
     */
    private static List<TeamMember> getTeamMembersWithNames(List<String> names, Team team) {
        return TeamsUtil.getTeamMembersWithNames(names, team);
    }

    /**
     * Gets whether or not there are any team members that use entity markers to identify
     * themselves in the scenario's team organization
     *
     * @return whether any team members use entity markers
     */
    public static boolean hasTeamMemberWithMarker() {

        if(getTeamOrganization() != null) {
            return hasTeamMemberWithMarker(getTeamOrganization().getTeam());
        }

        return false;
    }

    /**
     * Gets whether or not there are any team members that use entity markers to identify
     * themselves in the given team
     *
     * @param team the team to find team members with entity markers inside
     * @return whether any team members use entity markers
     */
    private static boolean hasTeamMemberWithMarker(Team team) {
        return TeamsUtil.hasTeamMemberWithMarker(team);
    }

    /**
     * Finds the parent {@link Team} of the provided {@link Team} or
     * {@link TeamMember}..
     *
     * @param teamOrTeamMember the child {@link Team} or {@link TeamMember} of
     *        the parent to find. Can't be null.
     * @return the parent {@link Team} or null if no parent is found.
     */
    public static Team findTeamParent(Serializable teamOrTeamMember) {
        if (teamOrTeamMember == null) {
            throw new IllegalArgumentException("The parameter 'teamMember' cannot be null.");
        } else if (!(teamOrTeamMember instanceof Team || teamOrTeamMember instanceof TeamMember)) {
            throw new IllegalArgumentException(
                    "The parameter 'teamOrTeamMember' must be of type 'Team' or 'TeamMember'.");
        }

        final TeamOrganization teamOrganization = getTeamOrganization();
        if (teamOrganization == null) {
            return null;
        }

        return findTeamParent(teamOrganization.getTeam(), teamOrTeamMember);
    }

    /**
     * Finds the parent {@link Team} of the provided {@link Team} or
     * {@link TeamMember}.
     *
     * @param teamToSearch the {@link Team} to search for the provided
     *        {@link Team} or {@link TeamMember}. Can't be null.
     * @param teamOrTeamMemberToFind the {@link Team} or {@link TeamMember} to
     *        find in order to retrieve its parent.
     * @return the parent {@link Team} or null if no parent is found.
     */
    private static Team findTeamParent(Team teamToSearch, Serializable teamOrTeamMemberToFind) {
        return TeamsUtil.findTeamParent(teamToSearch, teamOrTeamMemberToFind);
    }
    
    /**
     * Automatically adjusts the performance metric rules for the given parent object's children so that any
     * metric rules that do not yet have a weight associated with them are provided with one based on 
     * how much remaining weight is unused (out of 100%)
     * 
     * @param parent the parent object whose children should be adjusted. Only Tasks and Concepts will
     * be affected by this method. For any other argument, this will function as a no-op.
     */
    public static void adjustChildRollupRules(Serializable parent) {
        AssessmentRollupWidget.adjustChildRollupRules(parent);
    }
    
    /**
     * Creates a scenario concept from the given course concept. Any child course concepts
     * will also be included.
     * 
     * @param courseConcept the course concept to import. If null, null will be returned.
     * @return the imported scenario concept. Can be null.
     */
    public static Concept importConceptFromCourse(ConceptNodeRef courseConcept) {
        
        final Concept concept = new Concept();
        concept.setNodeId(generateTaskOrConceptId());
        
        importConceptFromCourse(concept, courseConcept, new PerfNodeIdGenerator() {
            
            @Override
            public BigInteger generateNodeId() {
                return getLargestId(concept).add(BigInteger.ONE);
            }
        });
        
        return concept;
    }
    
    /**
     * Populates the given scenario concept with data from the given course concept. Child DKF concepts will
     * also be created for any child course concepts.
     * 
     * @param concept the DKF concept to populate. Cannot be null.
     * @param courseConcept the course concept to import. Cannot be null.
     * @param idGenerator a generator for performance node IDs. Cannot be null.
     */
    public static void importConceptFromCourse(final Concept concept, ConceptNodeRef courseConcept, PerfNodeIdGenerator idGenerator) {
        
        if(concept == null) {
            throw new IllegalArgumentException("The DKF concept to import a course concept to cannot be null");
        }
        
        if(courseConcept == null) {
            throw new IllegalArgumentException("The course concept to import cannot be null");
        }
        
        if(idGenerator == null) {
            throw new IllegalArgumentException("The ID generator for the course concepts to import cannot be null");
        }
        
        concept.setName(courseConcept.getName());
        
        if(courseConcept.getNodes() != null && !courseConcept.getNodes().isEmpty()) {
            
            Concepts concepts = new Concepts();
            concept.setConditionsOrConcepts(concepts);
            
            for(ConceptNodeRef node : courseConcept.getNodes()) {
                
                Concept childConcept = new Concept();
                childConcept.setNodeId(idGenerator.generateNodeId());
                concepts.getConcept().add(childConcept);
                
                importConceptFromCourse(childConcept, node, idGenerator);
            }
        }
    }
    
    /**
     * Creates a task from the given course concept. Any child course concepts
     * will also be included.
     * 
     * @param courseConcept the course concept to import. If null, null will be returned.
     * @return the imported task. Can be null.
     */
    public static Task importTaskFromCourse(ConceptNodeRef courseConcept) {
        
        if(courseConcept == null) {
            return null;
        }
        
        BigInteger nextNodeId = generateTaskOrConceptId();
        
        final Task task = new Task();
        task.setName(courseConcept.getName());
        task.setNodeId(nextNodeId);
        
        if(courseConcept.getNodes() != null && !courseConcept.getNodes().isEmpty()) {
            
            Concepts concepts = new Concepts();
            task.setConcepts(concepts);
            
            PerfNodeIdGenerator idGenerator = new PerfNodeIdGenerator() {
                
                @Override
                public BigInteger generateNodeId() {
                    return getLargestId(task).add(BigInteger.ONE);
                }
            };
            
            for(ConceptNodeRef node : courseConcept.getNodes()) {
                
                Concept childConcept = new Concept();
                childConcept.setNodeId(idGenerator.generateNodeId());
                concepts.getConcept().add(childConcept);
                
                importConceptFromCourse(childConcept, node, idGenerator);
            }
        }
        
        return task;
    }
    
    /**
     * A helper class used to generate unique performance node IDs for nodes that are not yet part of
     * the scenario
     * 
     * @author nroberts
     */
    private interface PerfNodeIdGenerator {
        
        /**
         * Generates a unique ID that is not yet in use
         * 
         * @return a unique node ID. Cannot be null.
         */
        public BigInteger generateNodeId();
    }
    
    /**
     * Uses a server call to check if the concept has different condition data than any other concept which references the same
     * xTSP object. If it does, triggers the appearance of a validation error to inform the user.
     * @param conceptToCompare	The Concept being checked. Cannot be null.
     * @param validationStatus 	The ModelValidationStatus to be set, if there is an error. Cannot be null.
     * @param conceptPanel		The ConceptPanel that triggered this validation check. Cannot be null.
     */
    public static void setConceptPanelWarningInCaseOfConflictingExternalSourceDuplicates(Concept conceptToCompare, ModelValidationStatus validationStatus, ConceptPanel conceptPanel) {
    	rpcService.doesConceptHaveConflictingExternalResourceReferences(conceptToCompare, getConceptsWithExternalSourceId(conceptToCompare.getExternalSourceId()), new AsyncCallback<Boolean>() {
    		
    		@Override
    		public void onSuccess(Boolean arg0) {
    			if (arg0.equals(Boolean.TRUE)) {
        			validationStatus.setModelObject(null);
        			validationStatus.setErrorMessage(
	        				"This concept is a duplicate reference to another concept from an external source, but its child conditions are not the same. Please resolve the issues within these conditions by ensuring each has the same child conditions.");
        			validationStatus.setAdditionalInstructions(null);
        			
        			// If the validation status is currently valid, then set it to invalid and have the ConceptPanel request validation again.
        			if (validationStatus.isValid()) {
        				validationStatus.setValidity(false, true);
        				conceptPanel.requestValidationExternally(validationStatus);
        			}
	        	}
    		}
    		
    		@Override
    		public void onFailure(Throwable arg0) {
    			logger.warning("The call to GatRpcService.doesConceptHaveConflictingExternalResourceReferences() failed from ScenarioClientUtility.setConceptPanelWarningInCaseOfConflictingExternalSourceDuplicates().");
    		}
    	});
    }
    
    /** 
     * Gets a list of Concepts which have the specified external source ID.
     * @param targetExternalSourceId a String value for the externalSourceId
     * @return a List<Concept> containing all Concepts with externalSourceIds that match targetExternalSourceId
     */
	public static List<Concept> getConceptsWithExternalSourceId(String targetExternalSourceId) {
		Tasks scenarioTasks = getTasks();
		List<Task> taskList = scenarioTasks.getTask();
		List<Concept> conceptsToReturn = new ArrayList<Concept>();
		
		for (Task currentTask : taskList) {
			List<Concept> conceptList = currentTask.getConcepts().getConcept();
			for (Concept currentConcept : conceptList) {
				conceptsToReturn.addAll(getConceptsWithExternalSourceId(targetExternalSourceId, currentConcept));
			}
		}
		
		return conceptsToReturn;
	}

	/**
	 * A recursive method that gets a list of current and child Concepts which have the specified external source ID.
	 * @param targetExternalSourceId a String value for the externalSourceId
	 * @param currentConcept the current Concept being checked
	 * @return a List<Concept> containing all current and child Concepts with externalSourceIds that match targetExternalSourceId
	 */
	private static Collection<? extends Concept> getConceptsWithExternalSourceId(String targetExternalSourceId,
			Concept currentConcept) {
		
		List<Concept> conceptsToReturn = new ArrayList<Concept>();
		
		String currentExternalSourceId = currentConcept.getExternalSourceId();
		
		if (currentExternalSourceId != null && currentExternalSourceId.equals(targetExternalSourceId)) {
			conceptsToReturn.add(currentConcept);
		}
		
		if (currentConcept.getConditionsOrConcepts() instanceof Concepts) {
			Concepts subConcepts = (Concepts) currentConcept.getConditionsOrConcepts();
			List<Concept> subConceptList = subConcepts.getConcept();
			for (Concept conceptToTraverse : subConceptList) {
				conceptsToReturn.addAll(getConceptsWithExternalSourceId(targetExternalSourceId, conceptToTraverse));
			}
		}
		
		return conceptsToReturn;
	}
}