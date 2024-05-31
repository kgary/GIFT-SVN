/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ApplicationCompletedCondition;
import generated.dkf.AssignedSectorCondition;
import generated.dkf.AutoTutorConditionInput;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.CheckpointPaceCondition;
import generated.dkf.CheckpointProgressCondition;
import generated.dkf.Condition;
import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.CorridorPostureCondition;
import generated.dkf.DetectObjectsCondition;
import generated.dkf.EliminateHostilesCondition;
import generated.dkf.EngageTargetsCondition;
import generated.dkf.EnterAreaCondition;
import generated.dkf.ExplosiveHazardSpotReportCondition;
import generated.dkf.FireTeamRateOfFireCondition;
import generated.dkf.GenericConditionInput;
import generated.dkf.HaltConditionInput;
import generated.dkf.HasMovedExcavatorComponentInput;
import generated.dkf.HealthConditionInput;
import generated.dkf.IdentifyPOIsCondition;
import generated.dkf.Input;
import generated.dkf.LifeformTargetAccuracyCondition;
import generated.dkf.MarksmanshipPrecisionCondition;
import generated.dkf.MarksmanshipSessionCompleteCondition;
import generated.dkf.MuzzleFlaggingCondition;
import generated.dkf.NegligentDischargeCondition;
import generated.dkf.NineLineReportCondition;
import generated.dkf.NoConditionInput;
import generated.dkf.NumberOfShotsFiredCondition;
import generated.dkf.ObservedAssessmentCondition;
import generated.dkf.PaceCountCondition;
import generated.dkf.PowerPointDwellCondition;
import generated.dkf.RequestExternalAttributeCondition;
import generated.dkf.RulesOfEngagementCondition;
import generated.dkf.SIMILEConditionInput;
import generated.dkf.SpacingCondition;
import generated.dkf.SpeedLimitCondition;
import generated.dkf.SpotReportCondition;
import generated.dkf.TimerConditionInput;
import generated.dkf.UseRadioCondition;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.ConditionInputCache.ConditionType;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ConditionEditorWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionInputParams;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionInputParamsResult;
import net.customware.gwt.dispatch.client.DispatchAsync;

/**
 * The panel that allows the user to select an input type for the condition and
 * edit the parameters of that input type.
 */
public class ConditionPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ConditionPanel.class.getName());

    /** Defines the interface for combining the java class with the ui.xml */
    interface ConditionPanelUiBinder extends UiBinder<Widget, ConditionPanel> {
    }

    /** Combines this java class with the ui.xml */
    private static ConditionPanelUiBinder uiBinder = GWT.create(ConditionPanelUiBinder.class);

    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);

    /** The panel to switch between the selection and condition panels */
    @UiField
    protected DeckPanel deckPanel;

    /** The widget for selecting a Condition */
    @UiField(provided = true)
    protected final ConditionSelectionPanel selectionPanel = new ConditionSelectionPanel(
            ScenarioClientUtility.getTrainingAppType());

    /** The panel containing the selected condition data */
    @UiField
    protected FlowPanel conditionPanel;

    /**
     * Allows the user to select the type of input for the condition to consume.
     */
    @UiField
    protected Select inputTypeSelect;

    /**
     * The panel that contains the {@link ConditionInputPanel} that is specific
     * to a specific type.
     */
    @UiField
    protected SimplePanel inputEditorPanel;

    /** The current condition which is being edited. */
    private Condition condition = null;

    /**
     * Stores each of the inputs the user is editing in the cache so a user's
     * authoring is not lost when switching inputs
     */
    private final ConditionInputCache conditionInputCache = new ConditionInputCache();

    /** The condition editor wrapper */
    private final ConditionEditorWrapper conditionEditorWrapper;

    /**
     * Flag to indicate if the panel is being populated with existing data. Used to prevent the
     * dirty event from firing.
     */
    private boolean isPopulatingPanel = false;

    /**
     * The command that gets executed when {@link #updateInputChoices(String, Command)} completes.
     */
    private final Command updateInputChoicesFinishedCommand = new Command() {
        @Override
        public void execute() {
            if (isPopulatingPanel) {
                validateAll();
                isPopulatingPanel = false;
            } else {
                validateAllAndFireDirtyEvent(condition);
            }
        }
    };

    /** The container for showing validation messages for the task not having children. */
    private final ModelValidationStatus conditionImplValidationStatus = new ModelValidationStatus("A condition type must be selected.") {
        @Override
        protected void fireJumpToEvent(Serializable modelObject) {
            ScenarioEventUtility.fireJumpToEvent(modelObject);
        }
    };

    /**
     * Creates the panel used to edit the {@link Condition}
     */
    public ConditionPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        conditionImplValidationStatus.setAdditionalInstructions(
                "To add a condition type, click the button next to the desired condition. If you do not know which condition to choose, select the condition row to see a description.");

        selectionPanel.addValueChangeHandler(new ValueChangeHandler<ConditionInfo>() {

            @Override
            public void onValueChange(ValueChangeEvent<ConditionInfo> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("selectionPanel.onValueChange(" + event.getValue() + ")");
                }

                String oldConditionImpl = condition.getConditionImpl();
                String oldName = StringUtils.isBlank(oldConditionImpl) ? "Unknown"
                        : oldConditionImpl.substring(oldConditionImpl.lastIndexOf('.') + 1);

                /* Updates the name on the tab and within the outline */
                String conditionClassName = event.getValue().getConditionClass();
                String newName = event.getValue().getDisplayName();
                ScenarioEventUtility.fireRenameEvent(condition, oldName, newName);

                /* Sets the condition implementation and presents the specific
                 * editor */
                condition.setConditionImpl(conditionClassName);
                refresh();
                deckPanel.showWidget(deckPanel.getWidgetIndex(conditionPanel));
            }
        });

        conditionEditorWrapper = new ConditionEditorWrapper(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("conditionPanel.backButton.onClick(" + event + ")");
                }
                conditionEditorWrapper.clearValidations();
                deckPanel.showWidget(deckPanel.getWidgetIndex(selectionPanel));
            }
        });

        inputEditorPanel.setWidget(conditionEditorWrapper);

        // needs to be called last
        initValidationComposite(validations);
    }

    /**
     * Updates the UI to edit the given condition
     *
     * @param condition The condition for the {@link ConditionPanel} to edit. Can't be null.
     */
    public void edit(final Condition condition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + condition + ")");
        }

        /* Ensure that the condition is not null */
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }

        /* Save a reference to the condition that is now being edited so that the users actions will
         * appropriately populate it. */
        this.condition = condition;

        /* If there is no input, supply one */
        if (condition.getInput() == null) {
            condition.setInput(new Input());
        }

        String impl = condition.getConditionImpl();

        if (StringUtils.isBlank(impl)) {
            deckPanel.showWidget(deckPanel.getWidgetIndex(selectionPanel));
            validateAll();

            // we've gone as far as we can without an impl
            return;
        }

        /* Updates the cache with the supplied input if one already exists within the condition. */
        Serializable inputType = condition.getInput().getType();
        if (inputType != null) {
            ConditionType conditionType = ConditionType.getTypeFromString(impl);
            conditionInputCache.put(conditionType, inputType);
            deckPanel.showWidget(deckPanel.getWidgetIndex(conditionPanel));
        }

        /* The panel is being populated from existing data. Mark flag as true to prevent a dirty
         * event from being fired. */
        isPopulatingPanel = true;

        // wait until after the input type has been populated to edit the wrapper
        updateInputChoices(condition.getConditionImpl(), updateInputChoicesFinishedCommand);
    }

    /**
     * Updates the {@link ConditionPanel} UI based on the current state of the
     * underlying condition.
     */
    public void refresh() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refresh()");
        }

        if (condition == null) {
            return;
        }

        // clear condition scoring rules and advanced options
        condition.setScoring(null);
        condition.setDefault(null);

        // not populating on refresh
        isPopulatingPanel = false;

        // wait until after the input type has been populated to edit the wrapper
        updateInputChoices(condition.getConditionImpl(), updateInputChoicesFinishedCommand);
    }
    
    /**
     * A concept changed into a course concept or is no longer a course concept.
     * @param concept the concept that changed.  Shouldn't be null.
     */
    public void onCourseConceptChanged(generated.dkf.Concept concept){
        conditionEditorWrapper.refreshOverallAssessmentPanel();
    }

    /**
     * Handles when the user selects a new input type.
     *
     * @param event The event containing the new input type that the user has
     *        selected.
     */
    @UiHandler("inputTypeSelect")
    protected void onInputChanged(ValueChangeEvent<String> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onInputChanged(" + event.getValue() + ")");
        }

        ConditionType implType = ConditionType.getTypeFromString(condition.getConditionImpl());
        String inputTypeName = event.getValue();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Impl type: '" + implType + "'. Condition cache = " + conditionInputCache);
        }

        switch (implType) {
        case APPLICATION_COMPLETED_CONDITION:
            ApplicationCompletedCondition appCompletedCondition = (ApplicationCompletedCondition)conditionInputCache.get(implType, inputTypeName);
            ApplicationCompletedConditionEditorImpl appCompletedPanel = new ApplicationCompletedConditionEditorImpl();
            showInputPanel(appCompletedPanel, appCompletedCondition);
            break;
        case ASSIGNED_SECTOR_CONDITION:
            AssignedSectorCondition assignedSectorCondition = (AssignedSectorCondition) conditionInputCache.get(implType, inputTypeName);
            AssignedSectorConditionEditor assignedSectorPanel = new AssignedSectorConditionEditor();
            showInputPanel(assignedSectorPanel, assignedSectorCondition);
            break;
        case AUTO_TUTOR_CONDITION:
            AutoTutorConditionInput autoTutorCondition = (AutoTutorConditionInput)conditionInputCache.get(implType, inputTypeName);
            AutoTutorConditionInputEditorImpl autoTutorConditionPanel = new AutoTutorConditionInputEditorImpl();
            showInputPanel(autoTutorConditionPanel, autoTutorCondition);
            break;
        case AVOID_LOCATION_CONDITION:
            AvoidLocationCondition avoidLocation = (AvoidLocationCondition) conditionInputCache.get(implType, inputTypeName);
            AvoidLocationConditionEditorImpl avoidLocationPanel = new AvoidLocationConditionEditorImpl();
            showInputPanel(avoidLocationPanel, avoidLocation);
            break;
        case CHECKPOINT_PACE_CONDITION:
            CheckpointPaceCondition checkpointPaceCondition = (CheckpointPaceCondition) conditionInputCache.get(implType, inputTypeName);
            CheckpointPaceConditionEditorImpl checkpoinPacePanel = new CheckpointPaceConditionEditorImpl();
            showInputPanel(checkpoinPacePanel, checkpointPaceCondition);
            break;
        case CHECKPOINT_PROGRESS_CONDITION:
            CheckpointProgressCondition checkpointProgress = (CheckpointProgressCondition)conditionInputCache.get(implType, inputTypeName);
            CheckpointProgressConditionEditorImpl checkpointProgressPanel = new CheckpointProgressConditionEditorImpl();
            showInputPanel(checkpointProgressPanel, checkpointProgress);
            break;
        case CORRIDOR_BOUNDARY_CONDITION:
            CorridorBoundaryCondition corridorBoundary = (CorridorBoundaryCondition)conditionInputCache.get(implType, inputTypeName);
            CorridorBoundaryConditionEditorImpl corridorBoundaryPanel = new CorridorBoundaryConditionEditorImpl();
            showInputPanel(corridorBoundaryPanel, corridorBoundary);
            break;
        case CORRIDOR_POSTURE_CONDITION:
            CorridorPostureCondition corridorPosture = (CorridorPostureCondition) conditionInputCache.get(implType, inputTypeName);
            CorridorPostureConditionEditorImpl corridorPosturePanel = new CorridorPostureConditionEditorImpl();
            showInputPanel(corridorPosturePanel, corridorPosture);
            break;
        case DETECT_OBJECTS_CONDITION:
            DetectObjectsCondition detectObjects = (DetectObjectsCondition)conditionInputCache.get(implType, inputTypeName);
            DetectObjectsConditionEditor detectObjectsPanel = new DetectObjectsConditionEditor();
            showInputPanel(detectObjectsPanel, detectObjects);
            break;
        case ELIMINATE_HOSTILES_CONDITION:
            EliminateHostilesConditionEditorImpl eliminateHostilesPanel = new EliminateHostilesConditionEditorImpl();
            EliminateHostilesCondition eliminateHostiles = (EliminateHostilesCondition) conditionInputCache.get(implType, inputTypeName);
            showInputPanel(eliminateHostilesPanel, eliminateHostiles);
            break;
        case ENGAGE_TARGETS_CONDITION:
            EngageTargetsConditionEditor engageTargetsPanel = new EngageTargetsConditionEditor();
            EngageTargetsCondition engageTargets = (EngageTargetsCondition) conditionInputCache.get(implType, inputTypeName);
            showInputPanel(engageTargetsPanel, engageTargets);
            break;
        case ENTER_AREA_CONDITION:
            EnterAreaCondition enterArea = (EnterAreaCondition) conditionInputCache.get(implType, inputTypeName);
            EnterAreaConditionEditorImpl enterAreaPanel = new EnterAreaConditionEditorImpl();
            showInputPanel(enterAreaPanel, enterArea);
            break;
        case EXPLOSIVE_HAZARD_SPOT_REPORT_CONDITION:
            ExplosiveHazardSpotReportCondition explosiveHazardSpotReport = (ExplosiveHazardSpotReportCondition)conditionInputCache.get(implType, inputTypeName);
            ExplosiveHazardSpotReportConditionInputEditorImpl explosiveHazardSpotReportConditionPanel = new ExplosiveHazardSpotReportConditionInputEditorImpl();
            showInputPanel(explosiveHazardSpotReportConditionPanel, explosiveHazardSpotReport);
            break;
        case FIRE_TEAM_RATE_OF_FIRE_CONDITION:
            FireTeamRateOfFireCondition fireTeamRateOfFire = (FireTeamRateOfFireCondition)conditionInputCache.get(implType, inputTypeName);
            FireTeamRateOfFireConditionEditor fireTeamRateOfFireConditionPanel = new FireTeamRateOfFireConditionEditor();
            showInputPanel(fireTeamRateOfFireConditionPanel, fireTeamRateOfFire);
            break;
        case HALT_CONDITION:
            HaltConditionInput haltCondition = (HaltConditionInput) conditionInputCache.get(implType, inputTypeName);
            HaltConditionEditorImpl haltConditionPanel = new HaltConditionEditorImpl();
            showInputPanel(haltConditionPanel, haltCondition);
            break;
        case HEALTH_CONDITION:
            HealthConditionInput healthCondition = (HealthConditionInput) conditionInputCache.get(implType, inputTypeName);
            HealthConditionEditor healthConditionPanel = new HealthConditionEditor();
            showInputPanel(healthConditionPanel, healthCondition);
            break;
        case HAS_COLLIDED_CONDITION:
            // No editor for this condition
            setInputWithoutPanel(new NoConditionInput());
            break;
        case HAS_MOVED_EXCAVATOR_COMPONENT_CONDITION:
            HasMovedExcavatorComponentInput excavatorCondition = (HasMovedExcavatorComponentInput) conditionInputCache.get(implType, inputTypeName);
            HasMovedExcavatorComponentConditionEditorImpl excavatorConditionPanel = new HasMovedExcavatorComponentConditionEditorImpl();
            showInputPanel(excavatorConditionPanel, excavatorCondition);
            break;
        case IDENTIFY_POIS_CONDITION:
            IdentifyPOIsCondition identifyPOIs = (IdentifyPOIsCondition) conditionInputCache.get(implType, inputTypeName);
            IdentifyPoisConditionEditorImpl identifyPOIsPanel = new IdentifyPoisConditionEditorImpl();
            showInputPanel(identifyPOIsPanel, identifyPOIs);
            break;
        case LIFEFORM_TARGET_ACCURACY_CONDITION:
            LifeformTargetAccuracyCondition lifeformTargetAccuracy = (LifeformTargetAccuracyCondition) conditionInputCache.get(implType, inputTypeName);
            LifeformTargetAccuracyConditionEditorImpl lifeformTargetAccuracyPanel = new LifeformTargetAccuracyConditionEditorImpl();
            showInputPanel(lifeformTargetAccuracyPanel, lifeformTargetAccuracy);
            break;
        case MARKSMANSHIP_PRECISION_CONDITION:
            MarksmanshipPrecisionCondition marksmanshipPrecision = (MarksmanshipPrecisionCondition) conditionInputCache.get(implType, inputTypeName);
            MarksmanshipPrecisionConditionEditorImpl marksmanshipPrecisionConditionEditorImpl = new MarksmanshipPrecisionConditionEditorImpl();
            showInputPanel(marksmanshipPrecisionConditionEditorImpl, marksmanshipPrecision);
            break;
        case MARKSMANSHIP_SESSION_COMPLETE_CONDITION:
            MarksmanshipSessionCompleteCondition marksmanshipSessionComplete = (MarksmanshipSessionCompleteCondition)conditionInputCache.get(implType, inputTypeName);
            MarksmanshipSessionCompleteConditionEditorImpl marksmanshipSessionCompletePanel = new MarksmanshipSessionCompleteConditionEditorImpl();
            showInputPanel(marksmanshipSessionCompletePanel, marksmanshipSessionComplete);
            break;
        case MUZZLE_FLAGGING_CONDITION:
            MuzzleFlaggingCondition muzzleFlaggingCondition = (MuzzleFlaggingCondition) conditionInputCache.get(implType, inputTypeName);
            MuzzleFlaggingConditionEditor muzzleFlaggingPanel = new MuzzleFlaggingConditionEditor();
            showInputPanel(muzzleFlaggingPanel, muzzleFlaggingCondition);
            break;
        case NEGLIGENT_DISCHARGE_CONDITION:
            NegligentDischargeCondition negligentDischargeCondition = (NegligentDischargeCondition) conditionInputCache.get(implType, inputTypeName);
            NegligentDischargeConditionEditor negligentDischargePanel = new NegligentDischargeConditionEditor();
            showInputPanel(negligentDischargePanel, negligentDischargeCondition);
            break;
        case NINE_LINE_REPORT_CONDITION:
            NineLineReportCondition nineLineReport = (NineLineReportCondition) conditionInputCache.get(implType, inputTypeName);
            NineLineReportConditionInputEditorImpl nineLineReportConditionPanel = new NineLineReportConditionInputEditorImpl();
            showInputPanel(nineLineReportConditionPanel, nineLineReport);
            break;
        case NUMBER_OF_SHOTS_FIRED_CONDITION:
            NumberOfShotsFiredCondition numberOfShotsFired = (NumberOfShotsFiredCondition) conditionInputCache.get(implType, inputTypeName);
            NumberOfShotsFiredConditionEditorImpl numberOfShotsFiredPanel = new NumberOfShotsFiredConditionEditorImpl();
            showInputPanel(numberOfShotsFiredPanel, numberOfShotsFired);
            break;
        case OBSERVED_ASSESSMENT_CONDITION:
            ObservedAssessmentCondition observedAssessmentCondition = (ObservedAssessmentCondition) conditionInputCache.get(implType, inputTypeName);
            ObservedAssessmentConditionInputEditorImpl observedAssessmentPanel = new ObservedAssessmentConditionInputEditorImpl();
            showInputPanel(observedAssessmentPanel, observedAssessmentCondition);
            break;
        case PACE_COUNT_CONDITION:
            PaceCountCondition paceCount = (PaceCountCondition) conditionInputCache.get(implType, inputTypeName);
            PaceCountConditionEditorImpl paceCountPanel = new PaceCountConditionEditorImpl();
            showInputPanel(paceCountPanel, paceCount);
            break;
        case PPT_OVER_DWELL_CONDITION:
        case PPT_UNDER_DWELL_CONDITION:
            PowerPointDwellCondition powerPoint = (PowerPointDwellCondition) conditionInputCache.get(implType, inputTypeName);
            PowerPointDwellConditionEditorImpl powerPointPanel = new PowerPointDwellConditionEditorImpl();
            if (implType == ConditionType.PPT_OVER_DWELL_CONDITION) {
                powerPointPanel.setItemListEditorDescription("Maximum time that the learner should spend on each slide:");
            } else if (implType == ConditionType.PPT_UNDER_DWELL_CONDITION) {
                powerPointPanel.setItemListEditorDescription("Minimum time that the learner should spend on each slide:");
            }
            showInputPanel(powerPointPanel, powerPoint);
            break;
        case REQUEST_EXTERNAL_ATTRIBUTE_CONDITION:
            RequestExternalAttributeCondition requestExternal = (RequestExternalAttributeCondition)conditionInputCache.get(implType,  inputTypeName);
            RequestExternalAttributeConditionEditor requestEditor = new RequestExternalAttributeConditionEditor();
            showInputPanel(requestEditor, requestExternal);
            break;
        case RULES_OF_ENGAGEMENT_CONDITION:
            RulesOfEngagementCondition rulesOfEngagement = (RulesOfEngagementCondition)conditionInputCache.get(implType, inputTypeName);
            RulesOfEngagementConditionEditorImpl rulesOfEngagementPanel = new RulesOfEngagementConditionEditorImpl();
            showInputPanel(rulesOfEngagementPanel, rulesOfEngagement);
            break;
        case SIMILE_CONDITION:
            SIMILEConditionInput simileConditionInput = (SIMILEConditionInput) conditionInputCache.get(implType, inputTypeName);
            SimileConditionInputEditorImpl simileConditionPanel = new SimileConditionInputEditorImpl();
            showInputPanel(simileConditionPanel, simileConditionInput);
            break;
        case SIMPLE_SURVEY_ASSESSMENT_CONDITION:
            // No editor for this condition
            setInputWithoutPanel(new NoConditionInput());
            break;
        case SPACING_CONDITION:
            SpacingCondition spacingCondition = (SpacingCondition) conditionInputCache.get(implType,
                    inputTypeName);
            SpacingConditionInputEditor spacingPanel = new SpacingConditionInputEditor();
            showInputPanel(spacingPanel, spacingCondition);
            break;
        case SPEED_LIMIT_CONDITION:
            SpeedLimitCondition speedLimitCondition = (SpeedLimitCondition) conditionInputCache.get(implType,
                    inputTypeName);
            SpeedLimitConditionInputPanel speedLimitPanel = new SpeedLimitConditionInputPanel();
            showInputPanel(speedLimitPanel, speedLimitCondition);
            break;
        case SPOT_REPORT_CONDITION:
            SpotReportCondition spotReportCondition = (SpotReportCondition) conditionInputCache.get(implType, inputTypeName);
            SpotReportConditionInputEditorImpl spotReportConditionPanel = new SpotReportConditionInputEditorImpl();
            showInputPanel(spotReportConditionPanel, spotReportCondition);
            break;
        case STRING_MATCHING_EXAMPLE_CONDITION:
            GenericConditionInput stringMatching = (GenericConditionInput) conditionInputCache.get(implType, inputTypeName);
            GenericConditionInputEditorImpl stringMatchingPanel = new GenericConditionInputEditorImpl();
            showInputPanel(stringMatchingPanel, stringMatching);
            break;
        case USE_RADIO_CONDITION:
            UseRadioCondition useRadio = (UseRadioCondition) conditionInputCache.get(implType, inputTypeName);
            UseRadioConditionInputEditorImpl useRadioConditionPanel = new UseRadioConditionInputEditorImpl();
            showInputPanel(useRadioConditionPanel, useRadio);
            break;
        case TIMER_CONDITION:
            TimerConditionInput timerCondition = (TimerConditionInput) conditionInputCache.get(implType, inputTypeName);
            TimerConditionInputEditorImpl timerPanel = new TimerConditionInputEditorImpl();
            showInputPanel(timerPanel, timerCondition);
            break;
        default:
            logger.warning("Unknown Condition type selected. Using empty panel.");
            Condition condition = (Condition) conditionInputCache.get(implType, inputTypeName);
            setInputWithoutPanel(condition);
            break;
        }
    }

    /**
     * Updates the choice of condition inputs based on the provided condition impl.
     *
     * @param conditionImpl The condition impl that the choice of inputs should be based on. Can't
     *        be null.
     * @param command the command to execute when the condition input has been populated. Can't be null.
     */
    private void updateInputChoices(String conditionImpl, final Command command) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(conditionImpl, command);
            logger.fine("updateInputChoices(" + StringUtils.join(", ", params) + ")");
        }

        if (StringUtils.isBlank(conditionImpl)) {
            return;
        } else if (command == null) {
            throw new IllegalArgumentException("The parameter 'command' cannot be null.");
        }

        /* Fetches the input types that are compatible with this condition
         * implementation */
        FetchConditionInputParams action = new FetchConditionInputParams(conditionImpl);
        DispatchAsync dispatch = SharedResources.getInstance().getDispatchService();
        dispatch.execute(action, new AsyncCallback<FetchConditionInputParamsResult>() {

            @Override
            public void onFailure(Throwable caught) {
                String errMsg = new StringBuilder()
                        .append("There was an issue while fetching the input types for '")
                        .append(condition.getConditionImpl())
                        .append('\'')
                        .toString();
                logger.log(Level.SEVERE, errMsg, caught);

                // nothing else to do but continue
                command.execute();
            }

            @Override
            public void onSuccess(FetchConditionInputParamsResult result) {
                if (!result.isSuccess()) {
                    String errMsg = new StringBuilder()
                            .append("The server reported a problem, while fetching the input types for '")
                            .append(condition.getConditionImpl())
                            .append("'.\n")
                            .append(result.getErrorMsg()).append("\n")
                            .append(result.getErrorDetails()).append("\n")
                            .append(result.getErrorStackTrace())
                            .toString();
                    logger.severe(errMsg);

                    // nothing else to do but continue
                    command.execute();
                    return;
                }

                populateInputChoices(result.getInputParams());

                // input choices have been populated, now continue
                command.execute();
            }
        });
    }

    /**
     * Updates the choices contained within {@link #inputTypeSelect} to reflect
     * the currently selected condition implementation within
     * {@link Condition#getConditionImpl()}
     *
     * @param inputs The collection of input types to allow the user to select
     */
    private void populateInputChoices(List<String> inputs) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateInputChoices(" + inputs + ")");
        }

        inputTypeSelect.clear();
        if (inputs == null) {
            return;
        }

        inputTypeSelect.setVisible(inputs.size() > 1);

        /* Builds each input type option within the input type Select control */
        for (String input : inputs) {
            Option option = new Option();
            option.setText(input);
            option.setValue(input);
            inputTypeSelect.add(option);
        }

        inputTypeSelect.refresh();

        if (!inputs.isEmpty()) {
            inputTypeSelect.setValue(inputs.get(0));
            ValueChangeEvent.fire(inputTypeSelect, inputs.get(0));
        }
    }

    /**
     * Shows the provided {@link ConditionInputPanel} and configures it to edit a given condition
     * input.
     *
     * @param panel The {@link ConditionInputPanel} to display to the user. Can't be null.
     * @param input The condition input to edit with the provided {@link ConditionInputPanel}. Can't
     *        be null.
     */
    private <T extends Serializable> void showInputPanel(ConditionInputPanel<T> panel, T input) {
        if (logger.isLoggable(Level.FINE)) {
            // don't want to log panel. Floods the log.
            logger.fine("showInputPanel(" + input + ")");
        }

        if (panel == null) {
            throw new IllegalArgumentException("The parameter 'panel' cannot be null.");
        } else if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }

        if (condition.getInput() == null) {
            condition.setInput(new Input());
        }
        condition.getInput().setType(input);

        /* The AvailableLearnerActions should be marked dirty since its validity
         * is dependent on what condition types are present in the Scenario. */
        AvailableLearnerActions availableLearnerActions = ScenarioClientUtility.getAvailableLearnerActions();
        if (availableLearnerActions != null) {
            ScenarioEventUtility.fireDirtyEditorEvent(availableLearnerActions);
        }

        /* condition input changed, so update waypoint references because we don't want to maintain
         * references to the old input panel */
        ScenarioClientUtility.gatherPlacesOfInterestReferences();

        // must set panel into wrapper before we edit.
        conditionEditorWrapper.setConditionPanel(panel);
        conditionEditorWrapper.edit(condition);

        // perform this after we populate the wrapper
        panel.edit(input, condition);
    }

    /**
     * Shows the panel that has no inputs and set the condition to use the provided condition input.
     *
     * @param input The condition input to use with the {@link #condition condition} being edited.
     *        Can't be null.
     */
    private void setInputWithoutPanel(Serializable input) {
        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setInputWithoutPanel(" + input + ")");
        }

        if (condition.getInput() == null) {
            condition.setInput(new Input());
        }
        condition.getInput().setType(input);

        /* The AvailableLearnerActions should be marked dirty since its validity
         * is dependent on what condition types are present in the Scenario. */
        AvailableLearnerActions availableLearnerActions = ScenarioClientUtility.getAvailableLearnerActions();
        if (availableLearnerActions != null) {
            ScenarioEventUtility.fireDirtyEditorEvent(availableLearnerActions);
        }

        conditionEditorWrapper.setConditionPanel(new NoInputsConditionEditorImpl());
        conditionEditorWrapper.edit(condition);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(conditionImplValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (conditionImplValidationStatus.equals(validationStatus)) {
            conditionImplValidationStatus.setValidity(StringUtils.isNotBlank(condition.getConditionImpl()));
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(conditionEditorWrapper);
    }
}