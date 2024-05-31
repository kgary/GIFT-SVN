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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.AGL;
import generated.dkf.ATRemoteSKO;
import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.ApplicationCompletedCondition;
import generated.dkf.Area;
import generated.dkf.AreaRef;
import generated.dkf.Assessments;
import generated.dkf.AssignedSectorCondition;
import generated.dkf.Audio;
import generated.dkf.AutoTutorConditionInput;
import generated.dkf.AutoTutorSKO;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.Checkpoint;
import generated.dkf.CheckpointPaceCondition;
import generated.dkf.CheckpointProgressCondition;
import generated.dkf.ChildConceptEnded;
import generated.dkf.CompletionTime;
import generated.dkf.Concept;
import generated.dkf.ConceptAssessment;
import generated.dkf.ConceptEnded;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Conversation;
import generated.dkf.ConversationTreeFile;
import generated.dkf.Coordinate;
import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.CorridorPostureCondition;
import generated.dkf.Count;
import generated.dkf.DetectObjectsCondition;
import generated.dkf.EliminateHostilesCondition;
import generated.dkf.EngageTargetsCondition;
import generated.dkf.EnterAreaCondition;
import generated.dkf.EntityLocation;
import generated.dkf.EntityLocation.EntityId;
import generated.dkf.EntityLocation.EntityId.TeamMemberRef;
import generated.dkf.EnvironmentAdaptation.HighlightObjects;
import generated.dkf.Entrance;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.Evaluator;
import generated.dkf.Evaluators;
import generated.dkf.ExplosiveHazardSpotReportCondition;
import generated.dkf.ExternalAttributeEnumType;
import generated.dkf.Feedback;
import generated.dkf.FireTeamRateOfFireCondition;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.GenericConditionInput;
import generated.dkf.HaltConditionInput;
import generated.dkf.HasMovedExcavatorComponentInput;
import generated.dkf.HasMovedExcavatorComponentInput.Component;
import generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional;
import generated.dkf.HealthConditionInput;
import generated.dkf.IdentifyPOIsCondition;
import generated.dkf.ImageProperties;
import generated.dkf.InTutor;
import generated.dkf.Inside;
import generated.dkf.InstructionalIntervention;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.LearnerActionReference;
import generated.dkf.LearnerActionsFiles;
import generated.dkf.LearnerActionsList;
import generated.dkf.LearnerId;
import generated.dkf.LearnerLocation;
import generated.dkf.LearnerStateTransitionEnum;
import generated.dkf.LessonMaterialList.Assessment;
import generated.dkf.LessonMaterialList.Assessment.UnderDwell;
import generated.dkf.LifeformTargetAccuracyCondition;
import generated.dkf.LocalSKO;
import generated.dkf.LtiProperties;
import generated.dkf.MarksmanshipPrecisionCondition;
import generated.dkf.MarksmanshipSessionCompleteCondition;
import generated.dkf.Media;
import generated.dkf.MediaSemantics;
import generated.dkf.Message;
import generated.dkf.Message.Delivery;
import generated.dkf.MidLessonMedia;
import generated.dkf.MuzzleFlaggingCondition;
import generated.dkf.NegligentDischargeCondition;
import generated.dkf.NineLineReportCondition;
import generated.dkf.NoConditionInput;
import generated.dkf.NumberOfShotsFiredCondition;
import generated.dkf.Nvpair;
import generated.dkf.ObservedAssessmentCondition;
import generated.dkf.Outside;
import generated.dkf.OverDwell;
import generated.dkf.OverDwell.Duration.DurationPercent;
import generated.dkf.PDFProperties;
import generated.dkf.PaceCountCondition;
import generated.dkf.Path;
import generated.dkf.PathRef;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceNode;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Point;
import generated.dkf.PointRef;
import generated.dkf.PowerPointDwellCondition;
import generated.dkf.PowerPointDwellCondition.Default;
import generated.dkf.PowerPointDwellCondition.Slides;
import generated.dkf.PowerPointDwellCondition.Slides.Slide;
import generated.dkf.RealTimeAssessmentRules;
import generated.dkf.RequestExternalAttributeCondition;
import generated.dkf.RulesOfEngagementCondition;
import generated.dkf.SIMILEConditionInput;
import generated.dkf.Scenario;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.ScenarioStarted;
import generated.dkf.Segment;
import generated.dkf.SlideShowProperties;
import generated.dkf.SpacingCondition;
import generated.dkf.SpacingCondition.SpacingPair;
import generated.dkf.SpeedLimitCondition;
import generated.dkf.SpotReportCondition;
import generated.dkf.StartLocation;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.StrategyRef;
import generated.dkf.Task;
import generated.dkf.TaskEnded;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import generated.dkf.TeamMemberRefs;
import generated.dkf.TeamOrganization;
import generated.dkf.TeamRef;
import generated.dkf.TimerConditionInput;
import generated.dkf.TriggerLocation;
import generated.dkf.TutorMeParams;
import generated.dkf.UseRadioCondition;
import generated.dkf.ViolationTime;
import generated.dkf.WebpageProperties;
import generated.dkf.YoutubeVideoProperties;
import generated.dkf.VideoProperties;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.util.TeamsUtil;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * Class to validate objects.
 *
 * @author sharrison
 */
public class ScenarioValidatorUtility {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioValidatorUtility.class.getName());

    /**
     * The cache that maintains validity for each object and will call this
     * class to update its state
     */
    private final ScenarioValidationCache parentCache;

    /**
     * The validator utility to validate the DKF items.
     *
     * @param parentCache the cache that maintains validity for each object and
     *        will call this class to update its state
     */
    public ScenarioValidatorUtility(ScenarioValidationCache parentCache) {
        if (parentCache == null) {
            throw new IllegalArgumentException("The parameter 'parentCache' cannot be null.");
        }

        this.parentCache = parentCache;
    }

    /**
     * Validates an object and its children if it has any.
     *
     * @param validationObject the object to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public String validateObject(Serializable validationObject) {
        return validateObject(validationObject, true);
    }

    /**
     * Validates an object.
     *
     * @param validationObject the object to validate. Can't be null.
     * @param validateChildren true to include the object's children in the
     *        validation process; false to exclude the children.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public String validateObject(Serializable validationObject, boolean validateChildren) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(validationObject, validateChildren);
            logger.fine("validateObject(" + join(", ", params) + ")");
        }

        if (validationObject == null) {
            throw new IllegalArgumentException("The parameter 'validationObject' cannot be null.");
        }
        
        // check if validationObject is a scenario object
        if (validationObject instanceof Task) {
            Task task = (Task) validationObject;
            return validateTask(task, validateChildren);

        } else if (validationObject instanceof Concept) {
            Concept concept = (Concept) validationObject;
            return validateConcept(concept, validateChildren);

        } else if (validationObject instanceof Condition) {
            Condition condition = (Condition) validationObject;
            return validateCondition(condition);

        } else if (validationObject instanceof Strategy) {
            Strategy strategy = (Strategy) validationObject;
            return validateStrategy(strategy);

        } else if (validationObject instanceof StateTransition) {
            StateTransition stateTransition = (StateTransition) validationObject;
            return validateStateTransition(stateTransition);
        }

        // check if validationObject is a condition type
        else if (validationObject instanceof ApplicationCompletedCondition) {
            ApplicationCompletedCondition condition = (ApplicationCompletedCondition) validationObject;
            return validateApplicationCompletedCondition(condition);
            
        }else if(validationObject instanceof AssignedSectorCondition) {
            AssignedSectorCondition condition = (AssignedSectorCondition) validationObject;
            return validateAssignedSectorCondition(condition);

        } else if (validationObject instanceof AutoTutorConditionInput) {
            AutoTutorConditionInput condition = (AutoTutorConditionInput) validationObject;
            return validateAutoTutorConditionInput(condition);

        } else if (validationObject instanceof AvoidLocationCondition) {
            AvoidLocationCondition condition = (AvoidLocationCondition) validationObject;
            return validateAvoidLocationCondition(condition);

        } else if (validationObject instanceof CheckpointPaceCondition) {
            CheckpointPaceCondition condition = (CheckpointPaceCondition) validationObject;
            return validateCheckpointPaceCondition(condition);

        } else if (validationObject instanceof CheckpointProgressCondition) {
            CheckpointProgressCondition condition = (CheckpointProgressCondition) validationObject;
            return validateCheckpointProgressCondition(condition);

        } else if (validationObject instanceof CorridorBoundaryCondition) {
            CorridorBoundaryCondition condition = (CorridorBoundaryCondition) validationObject;
            return validateCorridorBoundaryCondition(condition);

        } else if (validationObject instanceof CorridorPostureCondition) {
            CorridorPostureCondition condition = (CorridorPostureCondition) validationObject;
            return validateCorridorPostureCondition(condition);
            
        } else if (validationObject instanceof DetectObjectsCondition){
            DetectObjectsCondition condition = (DetectObjectsCondition) validationObject;
            return validateDetectObjectsCondition(condition);

        } else if (validationObject instanceof EliminateHostilesCondition) {
            EliminateHostilesCondition condition = (EliminateHostilesCondition) validationObject;
            return validateEliminateHostilesCondition(condition);
            
        }else if(validationObject instanceof EngageTargetsCondition) {
            EngageTargetsCondition condition = (EngageTargetsCondition) validationObject;
            return validateEngageTargetsCondition(condition);

        } else if (validationObject instanceof EnterAreaCondition) {
            EnterAreaCondition condition = (EnterAreaCondition) validationObject;
            return validateEnterAreaCondition(condition);

        } else if (validationObject instanceof ExplosiveHazardSpotReportCondition) {
            ExplosiveHazardSpotReportCondition condition = (ExplosiveHazardSpotReportCondition) validationObject;
            return validateExplosiveHazardSpotReportCondition(condition);
            
        } else if(validationObject instanceof FireTeamRateOfFireCondition){
            FireTeamRateOfFireCondition condition = (FireTeamRateOfFireCondition) validationObject;
            return validateFireTeamRateOfFireCondition(condition);

        } else if (validationObject instanceof GenericConditionInput) {
            GenericConditionInput condition = (GenericConditionInput) validationObject;
            return validateGenericConditionInput(condition);

        } else if (validationObject instanceof HaltConditionInput) {
            HaltConditionInput condition = (HaltConditionInput) validationObject;
            return validateHaltCondition(condition);

        } else if (validationObject instanceof HealthConditionInput) {
            HealthConditionInput condition = (HealthConditionInput) validationObject;
            return validateHealthCondition(condition);

        } else if (validationObject instanceof HasMovedExcavatorComponentInput) {
            HasMovedExcavatorComponentInput condition = (HasMovedExcavatorComponentInput) validationObject;
            return validateHasMovedExcavatorComponentInput(condition);

        } else if (validationObject instanceof IdentifyPOIsCondition) {
            IdentifyPOIsCondition condition = (IdentifyPOIsCondition) validationObject;
            return validateIdentifyPOIsCondition(condition);

        } else if (validationObject instanceof LifeformTargetAccuracyCondition) {
            LifeformTargetAccuracyCondition condition = (LifeformTargetAccuracyCondition) validationObject;
            return validateLifeformTargetAccuracyCondition(condition);

        } else if (validationObject instanceof MarksmanshipPrecisionCondition) {
            MarksmanshipPrecisionCondition condition = (MarksmanshipPrecisionCondition) validationObject;
            return validateMarksmanshipPrecisionCondition(condition);

        } else if (validationObject instanceof MarksmanshipSessionCompleteCondition) {
            MarksmanshipSessionCompleteCondition condition = (MarksmanshipSessionCompleteCondition) validationObject;
            return validateMarksmanshipSessionCompleteCondition(condition);

        } else if (validationObject instanceof MuzzleFlaggingCondition) {
            MuzzleFlaggingCondition condition = (MuzzleFlaggingCondition) validationObject;
            return validateMuzzleFlaggingCondition(condition);
            
        } else if (validationObject instanceof NegligentDischargeCondition) {
            NegligentDischargeCondition condition = (NegligentDischargeCondition) validationObject;
            return validateNegligentDischargeCondition(condition);

        } else if (validationObject instanceof NineLineReportCondition) {
            NineLineReportCondition condition = (NineLineReportCondition) validationObject;
            return validateNineLineReportCondition(condition);

        } else if (validationObject instanceof NoConditionInput) {
            NoConditionInput condition = (NoConditionInput) validationObject;
            return validateNoConditionInput(condition);

        } else if (validationObject instanceof NumberOfShotsFiredCondition) {
            NumberOfShotsFiredCondition condition = (NumberOfShotsFiredCondition) validationObject;
            return validateNumberOfShotsFiredCondition(condition);

        } else if (validationObject instanceof ObservedAssessmentCondition) {
            ObservedAssessmentCondition condition = (ObservedAssessmentCondition) validationObject;
            return validateObservedAssessmentCondition(condition);

        } else if (validationObject instanceof PaceCountCondition) {
            PaceCountCondition condition = (PaceCountCondition) validationObject;
            return validatePaceCountCondition(condition);

        } else if (validationObject instanceof PowerPointDwellCondition) {
            PowerPointDwellCondition condition = (PowerPointDwellCondition) validationObject;
            return validatePowerPointDwellCondition(condition);
            
        } else if(validationObject instanceof RequestExternalAttributeCondition){
            RequestExternalAttributeCondition condition = (RequestExternalAttributeCondition) validationObject;
            return validateRequestExternalAttributeCondition(condition);

        } else if (validationObject instanceof RulesOfEngagementCondition) {
            RulesOfEngagementCondition condition = (RulesOfEngagementCondition) validationObject;
            return validateRulesOfEngagementCondition(condition);

        } else if (validationObject instanceof SIMILEConditionInput) {
            SIMILEConditionInput condition = (SIMILEConditionInput) validationObject;
            return validateSIMILEConditionInput(condition);

        } else if (validationObject instanceof SpacingCondition) {
            SpacingCondition condition = (SpacingCondition) validationObject;
            return validateSpacingConditionInput(condition);

        } else if (validationObject instanceof SpeedLimitCondition) {
            SpeedLimitCondition condition = (SpeedLimitCondition) validationObject;
            return validateSpeedLimitConditionInput(condition);

        } else if (validationObject instanceof SpotReportCondition) {
            SpotReportCondition condition = (SpotReportCondition) validationObject;
            return validateSpotReportCondition(condition);

        } else if (validationObject instanceof TimerConditionInput) {
            TimerConditionInput condition = (TimerConditionInput) validationObject;
            return validateTimerConditionInput(condition);

        } else if (validationObject instanceof UseRadioCondition) {
            UseRadioCondition condition = (UseRadioCondition) validationObject;
            return validateUseRadioCondition(condition);
        }

        // check if validationObject is a scenario property object
        else if (validationObject instanceof LearnerId) {
            LearnerId learnerId = (LearnerId) validationObject;
            return validateLearnerId(learnerId);

        } else if (validationObject instanceof Scenario.EndTriggers) {
            Scenario.EndTriggers endTriggers = (Scenario.EndTriggers) validationObject;
            return validateScenarioEndTriggers(endTriggers);

        } else if (validationObject instanceof AvailableLearnerActions) {
            AvailableLearnerActions learnerActions = (AvailableLearnerActions) validationObject;
            return validateAvailableLearnerActions(learnerActions);
            
        }else if(validationObject instanceof LearnerAction){
            LearnerAction learnerAction = (LearnerAction)validationObject;
            return validateLearnerAction(learnerAction);

        } else if (validationObject instanceof PlacesOfInterest) {
            PlacesOfInterest placesOfInterest = (PlacesOfInterest) validationObject;
            return validatePlacesOfInterest(placesOfInterest);

        } else if (validationObject instanceof TeamOrganization) {
            TeamOrganization organization = (TeamOrganization) validationObject;
            return validateTeamOrganization(organization);

        } else if (validationObject instanceof Team) {
            Team team = (Team) validationObject;
            return validateTeam(team);

        } else if (validationObject instanceof TeamMember) {
            TeamMember teamMember = (TeamMember) validationObject;
            return validateTeamMember(teamMember);

        } else if (validationObject instanceof Scenario) {
            // miscellaneous panel - nothing to validate in here
            return null;
        }

        throw new IllegalArgumentException("The parameter 'validationObject' is of an unknown type '"
                + validationObject.getClass().getSimpleName() + "'. Cannot validate.");
    }

    //////////////////////////////////////////////////////////
    /////////// SCENARIO OBJECT VALIDATION METHODS ///////////
    //////////////////////////////////////////////////////////

    /**
     * Validates a {@link Task} scenario object.
     *
     * @param task the {@link Task} to validate. Can't be null.
     * @param validateChildren true to include the {@link Task task's} children
     *        in the validation process; false to exclude the children.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateTask(Task task, boolean validateChildren) {
        if (task == null) {
            throw new IllegalArgumentException("The parameter 'task' cannot be null.");
        }

        // must have name
        if (StringUtils.isBlank(task.getName())) {
            return "Task is missing a name";
        }

        // must have node id >= 0
        if (task.getNodeId() == null || task.getNodeId().intValue() < 0) {
            return "Task node id is null or less than 0";
        }

        // must have at least 1 child
        if (task.getConcepts() == null || task.getConcepts().getConcept().isEmpty()) {
            return "Task must have at least 1 child";
        } else if (validateChildren) {
            for (Concept childConcept : task.getConcepts().getConcept()) {
                if (!parentCache.isValid(childConcept)) {
                    return "Concept " + childConcept.getName() + " returned invalid from the cache";
                }
            }
        }

        // start triggers is optional, but if it exists, must have at least 1
        // start trigger
        if (task.getStartTriggers() != null) {

            if (task.getStartTriggers().getTrigger().isEmpty()) {
                /* Tasks must have at least 1 start trigger if StartTriggers is
                 * not null. This would normally return an invalid state, but
                 * the SaveDkfHandler will reset this back to null on save so
                 * for the sake of user-validation, we don't need to mark this
                 * Task as invalid. */
            }

            // validate start triggers
            boolean foundScenarioStarted = false;
            for (generated.dkf.StartTriggers.Trigger trigger : task.getStartTriggers().getTrigger()) {
                if (trigger.getTriggerType() instanceof ScenarioStarted) {
                    // can only have 1 scenario started task start trigger per
                    // task

                    if (foundScenarioStarted) {
                        return "More than one scenario started trigger type";
                    }
                    foundScenarioStarted = true;
                }

                String msg = validateStartTrigger(trigger);
                if (msg != null) {
                    return msg;
                }
            }
        }

        // must have at least 1 end trigger
        if (task.getEndTriggers() == null || task.getEndTriggers().getTrigger().isEmpty()) {
            return "Task must have at least 1 end trigger";
        }

        // validate end triggers
        for (generated.dkf.EndTriggers.Trigger trigger : task.getEndTriggers().getTrigger()) {
            String msg = validateEndTrigger(trigger);
            if (msg != null) {
                return msg;
            }
        }

        // if task has survey assessment, make sure one is selected
        if (task.getAssessments() != null) {
            for (Serializable assessmentType : task.getAssessments().getAssessmentTypes()) {
                if (assessmentType instanceof Assessments.Survey) {
                    Assessments.Survey survey = (Assessments.Survey) assessmentType;
                    if (StringUtils.isBlank(survey.getGIFTSurveyKey())) {
                        return "Task is missing the GIFT survey key";
                    }
                }
            }
        }
        
        // if child concept assessment roll up rules are defined
        // total weight must be 1.0
        double total = 0.0;
        boolean wasDefined = false;
        for(Concept concept : task.getConcepts().getConcept()){
            generated.dkf.PerformanceMetricArguments args = concept.getPerformanceMetricArguments();
            if(args != null){
                wasDefined = true;
                total += args.getWeight();
            }
        }
        
        if(wasDefined && (total < 0.999 || total > 1.001)){
            return "The total weights for this task's children doesn't equal 1.0";
        }

        return null;
    }

    /**
     * Validates {@link generated.dkf.StartTriggers.Trigger}.
     *
     * @param trigger the {@link generated.dkf.StartTriggers.Trigger} to
     *        validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateStartTrigger(generated.dkf.StartTriggers.Trigger trigger) {
        // optional message
        if (trigger.getTriggerMessage() != null) {
            generated.dkf.Strategy strategy = trigger.getTriggerMessage().getStrategy();
            if (strategy != null) {
                String msg = validateStrategy(strategy);
                if (msg != null) {
                    return msg;
                }
            }
        }

        // optional delay
        if (trigger.getTriggerDelay() != null) {
            // if it exists, must have value >= 0
            if (trigger.getTriggerDelay().doubleValue() < 0) {
                return "Trigger delay is less than 0";
            }
        }

        // validate trigger type
        String msg = validateTriggerType(trigger.getTriggerType());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates {@link generated.dkf.EndTriggers.Trigger}.
     *
     * @param trigger the {@link generated.dkf.EndTriggers.Trigger} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateEndTrigger(generated.dkf.EndTriggers.Trigger trigger) {
        // optional strategy
        if (trigger.getMessage() != null) {
            generated.dkf.Strategy strategy = trigger.getMessage().getStrategy();
            if (strategy != null) {
                String msg = validateStrategy(strategy);
                if (msg != null) {
                    return msg;
                }
            }
        }

        // optional delay
        if (trigger.getTriggerDelay() != null) {
            // if it exists, must have value >= 0
            if (trigger.getTriggerDelay().doubleValue() < 0) {
                return "Trigger delay is less than 0";
            }
        }

        // validate trigger type
        String msg = validateTriggerType(trigger.getTriggerType());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link Concept} scenario object.
     *
     * @param concept the {@link Concept} to validate. Can't be null.
     * @param validateChildren true to include the {@link Concept concept's}
     *        children in the validation process; false to exclude the children.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateConcept(Concept concept, boolean validateChildren) {
        if (concept == null) {
            throw new IllegalArgumentException("The parameter 'concept' cannot be null.");
        }

        // must have name
        if (StringUtils.isBlank(concept.getName())) {
            return "Concept is missing a name";
        }

        // must have node id >= 0
        if (concept.getNodeId() == null || concept.getNodeId().intValue() < 0) {
            return "Concept node id is null or less than 0";
        }

        // optional priority
        if (concept.getPriority() != null) {
            // must have priority >= 1
            if (concept.getPriority().intValue() < 1) {
                return "Concept priority is less than 1";
            }
        }

        // must have at least 1 child
        if (concept.getConditionsOrConcepts() == null) {
            return "Concept must have at least 1 child";
        }

        if (concept.getConditionsOrConcepts() instanceof Concepts) {
            Concepts concepts = (Concepts) concept.getConditionsOrConcepts();
            if (concepts.getConcept().isEmpty()) {
                return "Concept must have at least 1 concept child";
            }    
            
            // if child concept assessment roll up rules are defined
            // total weight must be 1.0
            double total = 0.0;
            boolean wasDefined = false;
            for(Concept childConcept : concepts.getConcept()){
                generated.dkf.PerformanceMetricArguments args = childConcept.getPerformanceMetricArguments();
                if(args != null){
                    wasDefined = true;
                    total += args.getWeight();
                }
            }
            
            if(wasDefined && (total < 0.999 || total > 1.001)){
                return "The total weights for this intermediate concept's children doesn't equal 1.0";
            }

            if (validateChildren) {
                for (Concept childConcept : concepts.getConcept()) {
                    if (!parentCache.isValid(childConcept)) {
                        return "Concept " + childConcept.getName() + " returned invalid from the cache";
                    }
                }
            }
        } else if (concept.getConditionsOrConcepts() instanceof Conditions) {
            Conditions conditions = (Conditions) concept.getConditionsOrConcepts();
            if (conditions.getCondition().isEmpty()) {
                return "Concept must have at least 1 condition child";
            }
            
            // if child concept assessment roll up rules are defined
            // total weight must be 1.0
            double total = 0.0;
            boolean wasDefined = false;
            for(Condition condition : conditions.getCondition()){
                generated.dkf.PerformanceMetricArguments args = condition.getPerformanceMetricArguments();
                if(args != null){
                    wasDefined = true;
                    total += args.getWeight();
                }
            }
            
            if(wasDefined && (total < 0.999 || total > 1.001)){
                return "The total weights for this concept's children doesn't equal 1.0";
            }

            if (validateChildren) {
                for (Condition childCondition : conditions.getCondition()) {
                    if (!parentCache.isValid(childCondition)) {
                        return "Condition " + childCondition.getConditionImpl() + " returned invalid from the cache";
                    }
                }
            }
        }
        

        // if concept has survey assessment, make sure one is selected
        if (concept.getAssessments() != null) {
            for (Serializable assessmentType : concept.getAssessments().getAssessmentTypes()) {
                if (assessmentType instanceof Assessments.Survey) {
                    Assessments.Survey survey = (Assessments.Survey) assessmentType;
                    if (StringUtils.isBlank(survey.getGIFTSurveyKey())) {
                        return "Concept is missing the GIFT survey key";
                    }
                }
            }
        }

        return null;
    }

    /**
     * Validates a {@link Condition} scenario object.
     *
     * @param condition the {@link Condition} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateCondition(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }

        // needs to have a condition impl
        if (StringUtils.isBlank(condition.getConditionImpl())) {
            return "Condition does not have a condition impl";
        }

        // scoring is optional
        if (condition.getScoring() != null) {
            // if scoring exists, it needs to have at least 1 rule.
            if (condition.getScoring().getType().isEmpty()) {
                return "Condition does not have any scoring rules";
            }

            // each rule needs at least 1 evaluator
            for (Serializable scoringType : condition.getScoring().getType()) {
                if (scoringType instanceof Count) {
                    Count count = (Count) scoringType;
                    String msg = validateScoringRule(count);
                    if (msg != null) {
                        return msg;
                    }
                } else if (scoringType instanceof CompletionTime) {
                    CompletionTime completionTime = (CompletionTime) scoringType;
                    String msg = validateScoringRule(completionTime);
                    if (msg != null) {
                        return msg;
                    }
                } else if (scoringType instanceof ViolationTime) {
                    ViolationTime violationTime = (ViolationTime) scoringType;
                    String msg = validateScoringRule(violationTime);
                    if (msg != null) {
                        return msg;
                    }
                } else {
                    // null or unknown scoring rule type
                    return "Null or unknown scoring rule type";
                }
            }
        }

        // default is optional, but if it exists, it needs to have an assessment
        if (condition.getDefault() != null && StringUtils.isBlank(condition.getDefault().getAssessment())) {
            return "Condition default is missing an assessment";
        }

        // condition input and type are required
        if (condition.getInput() == null || condition.getInput().getType() == null) {
            return "Condition is missing an input or input type";
        }

        // check the validity of the condition input type. Force revalidation.
        if (!parentCache.isValid(condition.getInput().getType(), true)) {
            return "Condition input type returned invalid from the cache";
        }

        return null;
    }

    /**
     * Validates a {@link Strategy} scenario object.
     *
     * @param strategy the {@link Strategy} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateStrategy(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        // must have name
        if (StringUtils.isBlank(strategy.getName())) {
            return "Strategy is missing a name";
        }

        // must have type
        for (Serializable activity : strategy.getStrategyActivities()) {
            String msg = validateStrategyActivity(activity);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link InstructionalIntervention}, {@link MidLessonMedia},
     * {@link PerformanceAssessment}, or {@link ScenarioAdaptation}.
     *
     * @param activity the {@link InstructionalIntervention},
     *        {@link MidLessonMedia}, {@link PerformanceAssessment}, or
     *        {@link ScenarioAdaptation} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateStrategyActivity(Serializable activity) {
        if (activity == null) {
            throw new IllegalArgumentException("The parameter 'activity' cannot be null.");
        }

        // find type and validate
        if (activity instanceof InstructionalIntervention) {
            InstructionalIntervention instructionalIntervention = (InstructionalIntervention) activity;
            String msg = validateInstructionalIntervention(instructionalIntervention);
            if (msg != null) {
                return msg;
            }

        } else if (activity instanceof MidLessonMedia) {
            MidLessonMedia midLessonMedia = (MidLessonMedia) activity;
            String msg = validateMidLessonMedia(midLessonMedia);
            if (msg != null) {
                return msg;
            }

        } else if (activity instanceof PerformanceAssessment) {
            PerformanceAssessment performanceAssessment = (PerformanceAssessment) activity;
            String msg = validatePerformanceAssessment(performanceAssessment);
            if (msg != null) {
                return msg;
            }

        } else if (activity instanceof ScenarioAdaptation) {
            ScenarioAdaptation scenarioAdaptation = (ScenarioAdaptation) activity;
            String msg = validateScenarioAdaptation(scenarioAdaptation);
            if (msg != null) {
                return msg;
            }
        } else {
            return "Unexpected strategy activity type: " + activity;
        }

        return null;
    }

    /**
     * Validates an {@link InstructionalIntervention}.
     *
     * @param instructionalIntervention the {@link InstructionalIntervention} to
     *        validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateInstructionalIntervention(InstructionalIntervention instructionalIntervention) {
        if (instructionalIntervention == null) {
            throw new IllegalArgumentException("The parameter 'instructionalIntervention' cannot be null.");
        }

        // must have strategy handler
        if (instructionalIntervention.getStrategyHandler() == null) {
            return "Strategy handler is null";
        }

        // strategy handler must have impl
        if (StringUtils.isBlank(instructionalIntervention.getStrategyHandler().getImpl())) {
            return "Strategy handler impl is blank";
        }

        // optional delay, but must be >= .01 if it exists
        if (instructionalIntervention.getDelayAfterStrategy() != null) {
            if (instructionalIntervention.getDelayAfterStrategy().getDuration() == null
                    || instructionalIntervention.getDelayAfterStrategy().getDuration().doubleValue() < 0.01) {
                return "Delay duration is null or less than 0.01";
            }
        }

        // must have feedback (or doNothingTactic)
        Feedback feedback = instructionalIntervention.getFeedback();
        if (feedback == null) {
            return "No feedback";
        }

        /* Make sure any team member refs are valid */
        String msg = validateTeamMembersExist(feedback.getTeamRef());
        if (msg != null) {
            return msg;
        }

        // feedback must have presentation type
        Serializable feedbackPresentation = feedback.getFeedbackPresentation();
        if (feedbackPresentation == null) {
            return "Feedback does not have a presentation type";
        }

        if (feedbackPresentation instanceof Message) {
            Message message = (Message) feedbackPresentation;

            // message content must be at least 2 characters
            if (StringUtils.isBlank(message.getContent()) || message.getContent().length() < 2) {
                return "Feedback message is less than 2 characters";
            }

        } else if (feedbackPresentation instanceof Audio) {
            Audio audio = (Audio) feedbackPresentation;

            // optional OGG file, but must be at least 5 characters if it
            // exists
            if (audio.getOGGFile() != null && audio.getOGGFile().length() < 5) {
                return "OGG file length is less than 5 characters";
            }

            // MP3 file must be at least 5 characters
            if (StringUtils.isBlank(audio.getMP3File()) || audio.getMP3File().length() < 5) {
                return "MP3 file length is less than 5 characters";
            }

        } else if (feedbackPresentation instanceof MediaSemantics) {
            MediaSemantics media = (MediaSemantics) feedbackPresentation;

            // avatar must be at least 6 characters
            if (StringUtils.isBlank(media.getAvatar()) || media.getAvatar().length() < 6) {
                return "Avatar file length is less than 6 characters";
            }

            // key name must be at least 5 characters
            if (StringUtils.isBlank(media.getKeyName()) || media.getKeyName().length() < 5) {
                return "Media key name is less than 5 characters";
            }

            // optional Message, but content must be at least 2 characters
            // if it exists
            if (media.getMessage() != null && StringUtils.isNotBlank(media.getMessage().getContent())
                    && media.getMessage().getContent().length() < 2) {
                return "Media message is less than 2 characters";
            }

        } else if (feedbackPresentation instanceof Feedback.File) {
            Feedback.File file = (Feedback.File) feedbackPresentation;

            // file must be at least 6 characters
            if (StringUtils.isBlank(file.getHTML()) || file.getHTML().length() < 6) {
                return "Feedback file length is less than 6 characters";
            }
        }

        return null;
    }

    /**
     * Validates a {@link MidLessonMedia}.
     *
     * @param midLessonMedia the {@link MidLessonMedia} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateMidLessonMedia(MidLessonMedia midLessonMedia) {
        if (midLessonMedia == null) {
            throw new IllegalArgumentException("The parameter 'midLessonMedia' cannot be null.");
        }

        // must have strategy handler
        if (midLessonMedia.getStrategyHandler() == null) {
            return "Strategy handler is null";
        }

        // strategy handler must have impl
        if (StringUtils.isBlank(midLessonMedia.getStrategyHandler().getImpl())) {
            return "Strategy handler impl is blank";
        }

        // optional delay, but must be >= .01 if it exists
        if (midLessonMedia.getDelayAfterStrategy() != null) {
            if (midLessonMedia.getDelayAfterStrategy().getDuration() == null
                    || midLessonMedia.getDelayAfterStrategy().getDuration().doubleValue() < 0.01) {
                return "Delay duration is null or less than 0.01";
            }
        }

        if (midLessonMedia.getLessonMaterialList() != null) {

            // must have at least 1 media
            if (midLessonMedia.getLessonMaterialList().getMedia().isEmpty()) {
                return "Lesson material list is empty";
            }

            for (Media media : midLessonMedia.getLessonMaterialList().getMedia()) {
                String msg = validateMedia(media);
                if (msg != null) {
                    return msg;
                }
            }

            // assessment is optional
            Assessment assessment = midLessonMedia.getLessonMaterialList().getAssessment();
            if (assessment != null) {
                UnderDwell underdwell = assessment.getUnderDwell();
                OverDwell overdwell = assessment.getOverDwell();

                // underdwell is optional, but if it exists, must have duration
                // and feedback
                if (underdwell != null) {
                    if (underdwell.getDuration() == null || underdwell.getDuration().intValue() < 0) {
                        return "Underdwell duration is null or less than 0";
                    }

                    if (StringUtils.isBlank(underdwell.getFeedback())) {
                        return "Underdwell is missing feedback";
                    }
                }

                // overdwell is optional, but if it exists, must have duration
                if (overdwell != null) {
                    if (overdwell.getDuration() == null) {
                        return "Overdwell duration is null";
                    }

                    // must have duration type
                    Serializable durationType = overdwell.getDuration().getType();
                    if (durationType == null) {
                        return "Overdwell duration type is null";
                    }

                    if (durationType instanceof DurationPercent) {
                        DurationPercent durationPercent = (DurationPercent) durationType;

                        // must have duration time, must have time that is >= 0
                        if (durationPercent.getTime() == null || durationPercent.getTime().intValue() < 0) {
                            return "Duration time is null or less than 0";
                        }

                        // must have duration percent, must have percent that is
                        // >= 0
                        if (durationPercent.getPercent() == null || durationPercent.getPercent().intValue() < 0) {
                            return "Duration percent is null or less than 0";
                        }
                    } else if (durationType instanceof BigInteger) {
                        BigInteger durationTime = (BigInteger) durationType;
                        if (durationTime.intValue() < 0) {
                            return "Duration time is less than 0";
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Validates a {@link Media}.
     *
     * @param media the {@link Media} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateMedia(Media media) {
        // must have a name with at least 1 character
        if (StringUtils.isBlank(media.getName())) {
            return "Media is missing a name";
        }

        // must have a URI with at least 3 characters
        if (StringUtils.isBlank(media.getUri()) || media.getUri().length() < 3) {
            return "Media URI is less than 3 characters";
        }

        Serializable mediaTypeProperty = media.getMediaTypeProperties();

        // must have a type property
        if (mediaTypeProperty == null) {
            return "Media type property is null";
        }

        if (mediaTypeProperty instanceof PDFProperties) {
            // no additional validation needed
        } else if (mediaTypeProperty instanceof WebpageProperties) {
            // no additional validation needed
        } else if (mediaTypeProperty instanceof YoutubeVideoProperties) {
            YoutubeVideoProperties property = (YoutubeVideoProperties) mediaTypeProperty;

            // size is optional, but it must have height and width if it
            // exists
            if (property.getSize() != null) {
                if (property.getSize().getHeight() == null || property.getSize().getWidth() == null) {
                    return "Youtube height or width is null";
                }
            }
        } else if (mediaTypeProperty instanceof ImageProperties) {
            // no additional validation needed
        } else if (mediaTypeProperty instanceof VideoProperties) {
            // no additional validation needed
        } else if (mediaTypeProperty instanceof SlideShowProperties) {
            SlideShowProperties property = (SlideShowProperties) mediaTypeProperty;

            // display previous slide and keep continue button values
            // must exist
            if (property.getDisplayPreviousSlideButton() == null || property.getKeepContinueButton() == null) {
                return "Slideshow button is null";
            }
        } else if (mediaTypeProperty instanceof LtiProperties) {
            LtiProperties property = (LtiProperties) mediaTypeProperty;

            // LTI identifier must exist
            if (StringUtils.isBlank(property.getLtiIdentifier())) {
                return "Lti identifier is blank";
            }

            // allow score must exist
            if (property.getAllowScore() == null) {
                return "Allow score is null";
            }

            /* optional custom parameters, but if it exists and has NV pairs,
             * the pairs must be populated */
            if (property.getCustomParameters() != null && !property.getCustomParameters().getNvpair().isEmpty()) {
                for (Nvpair paramPair : property.getCustomParameters().getNvpair()) {
                    if (StringUtils.isBlank(paramPair.getName()) || StringUtils.isBlank(paramPair.getValue())) {
                        return "LTI parameter pair is missing a name or value";
                    }
                }
            }

            // optional slider minimum value, but if it exists, must be
            // >= 0
            if (property.getSliderMinValue() != null && property.getSliderMinValue().intValue() < 0) {
                return "Slider min value is less than 0";
            }

            // optional slider maximum value, but if it exists, must be
            // >= 0
            if (property.getSliderMaxValue() != null && property.getSliderMaxValue().intValue() < 0) {
                return "Slider max value is less than 0";
            }

            // display mode must exist
            if (property.getDisplayMode() == null) {
                return "Display mode is null";
            }

            // concepts is optional, but if it exists each concept must
            // not be blank
            if (property.getLtiConcepts() != null) {
                for (String concept : property.getLtiConcepts().getConcepts()) {
                    if (StringUtils.isBlank(concept)) {
                        return "LTI concept is blank";
                    }
                }
            }
        }

        return null;
    }

    /**
     * Validates a {@link PerformanceAssessment}.
     *
     * @param performanceAssessment the {@link PerformanceAssessment} to
     *        validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validatePerformanceAssessment(PerformanceAssessment performanceAssessment) {
        if (performanceAssessment == null) {
            throw new IllegalArgumentException("The parameter 'performanceAssessment' cannot be null.");
        }

        // must have strategy handler
        if (performanceAssessment.getStrategyHandler() == null) {
            return "Strategy handler is null";
        }

        // strategy handler must have impl
        if (StringUtils.isBlank(performanceAssessment.getStrategyHandler().getImpl())) {
            return "Strategy handler impl is blank";
        }

        // optional delay, but must be >= .01 if it exists
        if (performanceAssessment.getDelayAfterStrategy() != null) {
            if (performanceAssessment.getDelayAfterStrategy().getDuration() == null
                    || performanceAssessment.getDelayAfterStrategy().getDuration().doubleValue() < 0.01) {
                return "Delay duration is null or less than 0.01";
            }
        }

        Serializable assessmentType = performanceAssessment.getAssessmentType();

        // assessment type must exist
        if (assessmentType == null) {
            return "Assessment type is null";
        }

        if (assessmentType instanceof Conversation) {
            Conversation conversation = (Conversation) assessmentType;

            Serializable conversationType = conversation.getType();

            if (conversationType instanceof ConversationTreeFile) {
                ConversationTreeFile treeFile = (ConversationTreeFile) conversationType;

                // tree file must have name that is at least 18 characters
                if (StringUtils.isBlank(treeFile.getName()) || treeFile.getName().length() < 18) {
                    return "Tree file must be at least 18 characters";
                }
            } else if (conversationType instanceof AutoTutorSKO) {
                AutoTutorSKO autoTutorSKO = (AutoTutorSKO) conversationType;

                // validate auto tutor SKO
                String msg = validateAutoTutorSKO(autoTutorSKO);
                if (msg != null) {
                    return msg;
                }
            } else {
                // conversation type must exist
                return "Conversation type '" + conversationType + "' is null or unknown";
            }
        } else if (assessmentType instanceof PerformanceAssessment.PerformanceNode) {
            PerformanceAssessment.PerformanceNode node = (PerformanceAssessment.PerformanceNode) assessmentType;

            // node id must exist and be >= 0
            if (node.getNodeId() == null || node.getNodeId().intValue() < 0) {
                return "Performance node id is null or less than 0";
            }
        }

        return null;
    }

    /**
     * Validates a {@link ScenarioAdaptation}.
     *
     * @param scenarioAdaptation the {@link ScenarioAdaptation} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateScenarioAdaptation(ScenarioAdaptation scenarioAdaptation) {
        if (scenarioAdaptation == null) {
            throw new IllegalArgumentException("The parameter 'scenarioAdaptation' cannot be null.");
        }

        // must have strategy handler
        if (scenarioAdaptation.getStrategyHandler() == null) {
            return "Strategy handler is null";
        }

        // strategy handler must have impl
        if (StringUtils.isBlank(scenarioAdaptation.getStrategyHandler().getImpl())) {
            return "Strategy handler impl is blank";
        }

        // optional delay, but must be >= .01 if it exists
        if (scenarioAdaptation.getDelayAfterStrategy() != null) {
            if (scenarioAdaptation.getDelayAfterStrategy().getDuration() == null
                    || scenarioAdaptation.getDelayAfterStrategy().getDuration().doubleValue() < 0.01) {
                return "Delay duration is null or less than 0.01";
            }
        }

        EnvironmentAdaptation adaptationType = scenarioAdaptation.getEnvironmentAdaptation();
        if (adaptationType != null) {
            EnvironmentAdaptation envAdaptation = adaptationType;
            Serializable envAdaptType = envAdaptation.getType();

            // must have pair
            if (envAdaptType == null) {
                return "Environment adaptation type is null";
            }

            if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.Fog) {

                generated.dkf.EnvironmentAdaptation.Fog type = (generated.dkf.EnvironmentAdaptation.Fog) envAdaptType;
                if (type.getDensity() == null) {
                    return "Fog density is not set";
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay) {

                generated.dkf.EnvironmentAdaptation.TimeOfDay type = (generated.dkf.EnvironmentAdaptation.TimeOfDay) envAdaptType;
                if (type.getType() == null) {
                    return "Time of day is not set";
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.Overcast) {

                generated.dkf.EnvironmentAdaptation.Overcast type = (generated.dkf.EnvironmentAdaptation.Overcast) envAdaptType;
                if (type.getValue() == null) {
                    return "Overcast value is not set";
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.Rain) {

                generated.dkf.EnvironmentAdaptation.Rain type = (generated.dkf.EnvironmentAdaptation.Rain) envAdaptType;
                if (type.getValue() == null) {
                    return "Rain value is not set";
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.CreateActors) {

                generated.dkf.EnvironmentAdaptation.CreateActors type = (generated.dkf.EnvironmentAdaptation.CreateActors) envAdaptType;
                if (type.getSide() == null) {
                    return "The side is not set";
                } else if (type.getType() == null) {
                    return "The type is not set";
                } else if (type.getCoordinate() == null || type.getCoordinate().getType() == null) {
                    return "The location is not set";
                }
                
                boolean actorNameIsGood = StringUtils.isBlank(type.getActorName()) || type.getActorName().matches("^[a-zA-Z0-9]*$");
                if(!actorNameIsGood) {
                    return "The actor name contains a non-alphanumeric character";
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.Endurance) {

                generated.dkf.EnvironmentAdaptation.Endurance type = (generated.dkf.EnvironmentAdaptation.Endurance) envAdaptType;
                if (type.getValue() == null) {
                    return "The endurance value is not set";
                }

                if (type.getTeamMemberRef() != null) {
                    String name = type.getTeamMemberRef().getValue();
                    boolean hasMember = TeamsUtil.hasTeamOrTeamMemberWithName(name,
                            ScenarioClientUtility.getTeamOrganizationTeam());
                    if (!hasMember) {
                        return "The member {" + name + "} does not exist in the team organization.";
                    }
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.FatigueRecovery) {

                generated.dkf.EnvironmentAdaptation.FatigueRecovery type = (generated.dkf.EnvironmentAdaptation.FatigueRecovery) envAdaptType;
                if (type.getRate() == null) {
                    return "The recovery rate is not set";
                }

                if (type.getTeamMemberRef() != null) {
                    String name = type.getTeamMemberRef().getValue();
                    boolean hasMember = TeamsUtil.hasTeamOrTeamMemberWithName(name,
                            ScenarioClientUtility.getTeamOrganizationTeam());
                    if (!hasMember) {
                        return "The member {" + name + "} does not exist in the team organization.";
                    }
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.RemoveActors) {

                generated.dkf.EnvironmentAdaptation.RemoveActors type = (generated.dkf.EnvironmentAdaptation.RemoveActors) envAdaptType;

                Serializable singleType = type.getType();
                    if (singleType instanceof String) {
                        String actorName = (String) singleType;
                        if (StringUtils.isBlank(actorName)) {
                            return "The actor name cannot be blank";
                        }
                    }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.Teleport) {

                generated.dkf.EnvironmentAdaptation.Teleport type = (generated.dkf.EnvironmentAdaptation.Teleport) envAdaptType;
                if (type.getCoordinate() == null || type.getCoordinate().getType() == null) {
                    return "The location is not set";
                }

                String msg = validateCoordinate(type.getCoordinate());
                if (msg != null) {
                    return msg;
                }

                if (type.getTeamMemberRef() != null) {
                    String name = type.getTeamMemberRef().getValue();
                    boolean hasMember = TeamsUtil.hasTeamOrTeamMemberWithName(name,
                            ScenarioClientUtility.getTeamOrganizationTeam());
                    if (!hasMember) {
                        return "The member {" + name + "} does not exist in the team organization.";
                    }
                }

            } else if (envAdaptType instanceof generated.dkf.EnvironmentAdaptation.Script) {
                generated.dkf.EnvironmentAdaptation.Script type = (generated.dkf.EnvironmentAdaptation.Script) envAdaptType;
                if (StringUtils.isBlank(type.getValue())) {
                    return "The content of the scenario adaptation script is missing";
                }
                
            } else if(envAdaptType instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects){
                generated.dkf.EnvironmentAdaptation.HighlightObjects type = (generated.dkf.EnvironmentAdaptation.HighlightObjects)envAdaptType;
                
                if(StringUtils.isBlank(type.getName())){
                    return "The highlight name is missing";
                }
                
                Serializable highlightType = type.getType();
                if(highlightType == null){
                    return "The type of object to highlight is not set";
                }else if(highlightType instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.TeamMemberRef){
                    generated.dkf.EnvironmentAdaptation.HighlightObjects.TeamMemberRef memberRef = (generated.dkf.EnvironmentAdaptation.HighlightObjects.TeamMemberRef)highlightType;
                    if(StringUtils.isBlank(memberRef.getValue())){
                        return "The team member to highlight is missing";
                    }
                    
                    /* Make sure any team member refs are valid */
                    Set<String> invalidMembers = TeamsUtil.findInvalidMembers(Arrays.asList(memberRef.getValue()),
                            ScenarioClientUtility.getTeamOrganizationTeam());
                    if (CollectionUtils.isNotEmpty(invalidMembers)) {
                        return "The team member to highlight '"+memberRef.getValue()+"' does not exist in the team organization.";
                    }
                }else if(highlightType instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo){
                    generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = (generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo)highlightType;
                    String placesOfInterestRef = locationInfo.getPlaceOfInterestRef();
                    Serializable poi = ScenarioClientUtility.getPlaceOfInterestWithName(placesOfInterestRef);
                    if(poi == null){
                        return "The place of interest named '"+placesOfInterestRef+" does not exist. Please select an existing place of interest.";
                    }else if(poi instanceof Area || poi instanceof Path){
                        // only point supported
                        return "The place of interest named '"+placesOfInterestRef+"' is not a point.";
                    }
                    
                    // for VBS the coordinates in the POI need to be AGL
                    TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
                    boolean isVbs = taType == TrainingApplicationEnum.VBS;
                    if(isVbs){
                        
                        if(poi instanceof Point){
                            Point point = (Point)poi;
                            Coordinate coordinate = point.getCoordinate();
                            if(!(coordinate.getType() instanceof AGL)){
                                return "The place of interest named '"+placesOfInterestRef+"' must use the AGL coordinate type for VBS scripting to work.  Either use a different place of interest or change the coordinate type.";
                            }
                        } 
                    }
                }
                
            }else if(envAdaptType instanceof generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects){
                
                generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects type = (generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects)envAdaptType;
                
                InstructionalStrategies strategies = ScenarioClientUtility.getStrategies();
                boolean isExistingHighlight = false;
                if (strategies != null) {
                    for (Strategy strategy : strategies.getStrategy()) {
                        
                        for(Serializable activity : strategy.getStrategyActivities()){
                            
                            if(activity instanceof ScenarioAdaptation){
                                
                                ScenarioAdaptation scenarioAdapt = (ScenarioAdaptation)activity;
                                EnvironmentAdaptation environmentAdapt = scenarioAdapt.getEnvironmentAdaptation();
                                Serializable environmentAdaptType = environmentAdapt.getType();
                                if(environmentAdaptType instanceof HighlightObjects){
                                    HighlightObjects highlight = (HighlightObjects)environmentAdaptType;
                                    if(StringUtils.equals(highlight.getName(), type.getHighlightName())){
                                        isExistingHighlight = true;
                                        break;
                                    }

                                }
                            }
                        }
                        
                        if(isExistingHighlight){
                            break;
                        }

                    }

                }
                
                if(!isExistingHighlight){
                    return "The highlight name '"+type.getHighlightName()+"' is not an existing highlight object name.";
                }
                
            }else if(envAdaptType instanceof generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs){
                
                generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs createBreadcrumbs = (generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs)envAdaptType;
                generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = createBreadcrumbs.getLocationInfo();
                String placesOfInterestRef = locationInfo.getPlaceOfInterestRef();
                Serializable poi = ScenarioClientUtility.getPlaceOfInterestWithName(placesOfInterestRef);
                if(poi == null){
                    return "The place of interest named '"+placesOfInterestRef+" does not exist. Please select an existing place of interest.";
                }else if(poi instanceof Area){
                    // only point and path supported
                    return "The place of interest named '"+placesOfInterestRef+"' is not a point or path.";
                }
                
                // for VBS the coordinates in the POI need to be AGL
                TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
                boolean isVbs = taType == TrainingApplicationEnum.VBS;
                if(isVbs){
                    
                    if(poi instanceof Point){
                        Point point = (Point)poi;
                        Coordinate coordinate = point.getCoordinate();
                        if(!(coordinate.getType() instanceof AGL)){
                            return "The place of interest named '"+placesOfInterestRef+"' must use the AGL coordinate type for VBS scripting to work.  Either use a different place of interest or change the coordinate type.";
                        }
                    }else if(poi instanceof Path){
                        Path path = (Path)poi;
                        List<Segment> segments = path.getSegment();
                        if(segments.isEmpty()){
                            return "The place of interest named '"+placesOfInterestRef+"' must contain at least two points.";
                        }else{
                            // just check the first segments coordinate type
                            Segment firstSegment = segments.get(0);
                            Coordinate firstCoord = firstSegment.getStart().getCoordinate();
                            if(!(firstCoord.getType() instanceof AGL)){
                                return "The place of interest named '"+placesOfInterestRef+"' must use the AGL coordinate type for VBS scripting to work.  Either use a different place of interest or change the coordinate type.";
                            }
                        }
                    }
                }
                
                if(createBreadcrumbs.getTeamMemberRef().isEmpty()){
                    return "Please select one or more team members to see the bread crumb(s).";
                }
                
            }else if(envAdaptType instanceof generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs){
                    
                generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs removeBreadcrumbs = (generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs)envAdaptType;
                if(removeBreadcrumbs.getTeamMemberRef().isEmpty()){
                    return "Please select one or more team members to remove bread crumb(s) from.";
                }
            } else {
                return "Unsupported environment adaptation type -"+envAdaptType;
            }

        }

        return null;
    }

    /**
     * Validates a {@link StateTransition} scenario object.
     *
     * @param stateTransition the {@link StateTransition} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateStateTransition(StateTransition stateTransition) {
        if (stateTransition == null) {
            throw new IllegalArgumentException("The parameter 'stateTransition' cannot be null.");
        }

        // optional name
        if (stateTransition.getName() != null) {
            // must be at least 1 character
            if (StringUtils.isBlank(stateTransition.getName())) {
                return "State Transition name must be at least 1 character";
            }
        }
        // needs to have at least 1 state type
        if (stateTransition.getLogicalExpression() == null
                || stateTransition.getLogicalExpression().getStateType().isEmpty()) {
            return "Logical expression is null or it has no state type";
        }

        Set<Object> stateTypes = new HashSet<>();
        for (Serializable stateType : stateTransition.getLogicalExpression().getStateType()) {
            if (stateType instanceof LearnerStateTransitionEnum) {
                LearnerStateTransitionEnum learnerState = (LearnerStateTransitionEnum) stateType;
                String msg = validateLearnerStateTransitionEnum(learnerState);
                if (msg != null) {
                    return msg;
                }
                
                if(stateTypes.contains(learnerState.getAttribute())) {
                    return "Found duplicate learner state attribute of '"+learnerState.getAttribute()+"'";
                }
                stateTypes.add(learnerState.getAttribute());
                
            } else if (stateType instanceof PerformanceNode) {
                PerformanceNode node = (PerformanceNode) stateType;
                String msg = validatePerformanceNode(node);
                if (msg != null) {
                    return msg;
                }
                
                if(stateTypes.contains(node.getNodeId())){
                    return "Found duplicate task/concept of '"+node.getName()+"'";
            }
                stateTypes.add(node.getNodeId());
        }
        }

        // needs to have at least 1 strategy
        if (stateTransition.getStrategyChoices() == null
                || stateTransition.getStrategyChoices().getStrategies().isEmpty()) {
            return "Strategy choices is null or it has no strategies";
        }

        for (StrategyRef strategyRef : stateTransition.getStrategyChoices().getStrategies()) {
            if (StringUtils.isBlank(strategyRef.getName())) {
                return "Malformed strategy reference name: '" + strategyRef.getName() + "'";
            }
        }

        return null;
    }

    /**
     * Validates a {@link LearnerStateTransitionEnum}.
     *
     * @param learnerState the {@link LearnerStateTransitionEnum} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateLearnerStateTransitionEnum(LearnerStateTransitionEnum learnerState) {
        if (learnerState == null) {
            throw new IllegalArgumentException("The parameter 'learnerState' cannot be null.");
        }

        if (StringUtils.isBlank(learnerState.getPrevious()) && StringUtils.isBlank(learnerState.getCurrent())) {
            return "Logical Expression '" + learnerState.getAttribute()
                    + "' cannot have both previous and current values as null";
        }
        
        if(learnerState.getAttribute() != null) {
            try {
                LearnerStateAttributeNameEnum name = LearnerStateAttributeNameEnum.valueOf(learnerState.getAttribute());
                if(name.isExclusiveToConcepts()) {
                    if(StringUtils.isBlank(learnerState.getConcept())) {
                        return "The learner state '"+name.getDisplayName()+"' cannot have a null course concept";
                    }
                }
            }catch(@SuppressWarnings("unused") Exception e) {
                logger.warning("Found unhandled learner state attribute name of '"+learnerState.getAttribute()+"'.");
            }
        }

        return null;
    }

    /**
     * Validates a {@link PerformanceNode}.
     *
     * @param node the {@link PerformanceNode} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validatePerformanceNode(PerformanceNode node) {
        if (node == null) {
            throw new IllegalArgumentException("The parameter 'node' cannot be null.");
        }

        if (StringUtils.isBlank(node.getPrevious()) && StringUtils.isBlank(node.getCurrent())) {
            return "Logical Expression '" + node.getName() + "' cannot have both previous and current values as null";
        }

        return null;
    }

    ////////////////////////////////////////////////////
    /////////// CONDITION VALIDATION METHODS ///////////
    ////////////////////////////////////////////////////

    /**
     * Validates a {@link ApplicationCompletedCondition} condition.
     *
     * @param applicationCompleted the {@link ApplicationCompletedCondition} to
     *        validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateApplicationCompletedCondition(ApplicationCompletedCondition applicationCompleted) {

        // ideal completion duration is optional
        if (applicationCompleted.getIdealCompletionDuration() != null) {
            // if ideal completion duration exists, it must be at least 8
            // characters
            if (applicationCompleted.getIdealCompletionDuration().length() < 8) {
                return "Ideal completion duration must be at least 8 characters";
            }
        }

        return null;
    }

    /**
     * Validates a {@link AutoTutorConditionInput} condition.
     *
     * @param autoTutorInput the {@link AutoTutorConditionInput} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateAutoTutorConditionInput(AutoTutorConditionInput autoTutorInput) {

        // ideal completion duration is optional
        if (autoTutorInput.getAutoTutorSKO() == null) {
            return "Auto Tutor SKO is null";
        }

        String msg = validateAutoTutorSKO(autoTutorInput.getAutoTutorSKO());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link AvoidLocationCondition} condition.
     *
     * @param avoidLocation the {@link AvoidLocationCondition} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateAvoidLocationCondition(AvoidLocationCondition avoidLocation) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validateAvoidLocationCondition(" + avoidLocation + ")");
        }

        if (avoidLocation == null) {
            throw new IllegalArgumentException("The parameter 'avoidLocation' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (avoidLocation.getTeamMemberRefs() == null
                    || avoidLocation.getTeamMemberRefs().getTeamMemberRef().isEmpty()) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(avoidLocation.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        // must have point
        if (avoidLocation.getPointRef() == null) {
            return "Point reference is null";
        }

        if (avoidLocation.getPointRef().isEmpty() && avoidLocation.getAreaRef().isEmpty()) {
            return "The list of places to avoid is empty";
        }

        // validate point ref
        for (PointRef pointRef : avoidLocation.getPointRef()) {
            msg = validatePointRef(pointRef, ScenarioClientUtility.getDisallowedCoordinateTypes(avoidLocation.getClass()));
            if (msg != null) {
                return msg;
            }
        }

        // validate area ref
        for (AreaRef areaRef : avoidLocation.getAreaRef()) {
            msg = validateAreaRef(areaRef);
            if (msg != null) {
                return msg;
            }
        }

        // optional real time assessment rules
        if (avoidLocation.getRealTimeAssessmentRules() != null) {
            msg = validateRealTimeAssessmentRules(avoidLocation.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        if (Boolean.TRUE.equals(avoidLocation.isRequireLearnerAction())) {
            /* There must be an assess my location learner action for this
             * condition to be valid. Don't need to check that the
             * AvoidLocationCondition exists since we are inside that condition
             * already (passing false into the method). */
            msg = validateAvoidLocationLearnerActionDependency(ScenarioClientUtility.getUnmodifiableLearnerActionList(),
                    false);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link CheckpointPaceCondition} condition.
     *
     * @param checkpointPace the {@link CheckpointPaceCondition} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateCheckpointPaceCondition(CheckpointPaceCondition checkpointPace) {
        if (checkpointPace == null) {
            throw new IllegalArgumentException("The parameter 'checkpointPace' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (checkpointPace.getTeamMemberRef() == null) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure team member ref is valid */
        if (checkpointPace.getTeamMemberRef() != null) {
            boolean valid = TeamsUtil.hasTeamOrTeamMemberWithName(checkpointPace.getTeamMemberRef(),
                    ScenarioClientUtility.getTeamOrganizationTeam());
            if (!valid) {
                return checkpointPace.getTeamMemberRef() + " does not exist in the team organization.";
            }
        }

        List<Checkpoint> checkpointList = checkpointPace.getCheckpoint();

        // must have at least 1 checkpoint
        if (checkpointList.isEmpty()) {
            return "There are no checkpoints";
        }

        for (Checkpoint checkpoint : checkpointList) {
            // validate checkpoint
            String msg = validateCheckpoint(checkpoint);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link CheckpointProgressCondition} condition.
     *
     * @param checkpointProgress the {@link CheckpointProgressCondition} to
     *        validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateCheckpointProgressCondition(CheckpointProgressCondition checkpointProgress) {
        if (checkpointProgress == null) {
            throw new IllegalArgumentException("The parameter 'checkpointProgress' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (checkpointProgress.getTeamMemberRef() == null) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure team member ref is valid */
        if (checkpointProgress.getTeamMemberRef() != null) {
            boolean valid = TeamsUtil.hasTeamOrTeamMemberWithName(checkpointProgress.getTeamMemberRef(),
                    ScenarioClientUtility.getTeamOrganizationTeam());
            if (!valid) {
                return checkpointProgress.getTeamMemberRef() + " does not exist in the team organization.";
            }
        }

        List<Checkpoint> checkpointList = checkpointProgress.getCheckpoint();

        // must have at least 1 checkpoint
        if (checkpointList.isEmpty()) {
            return "There are no checkpoints";
        }

        for (Checkpoint checkpoint : checkpointList) {
            // validate checkpoint
            String msg = validateCheckpoint(checkpoint);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link CorridorBoundaryCondition} condition.
     *
     * @param corridorBoundary the {@link CorridorBoundaryCondition} to
     *        validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateCorridorBoundaryCondition(CorridorBoundaryCondition corridorBoundary) {
        if (corridorBoundary == null) {
            throw new IllegalArgumentException("The parameter 'corridorBoundary' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (corridorBoundary.getTeamMemberRefs() == null
                    || corridorBoundary.getTeamMemberRefs().getTeamMemberRef().isEmpty()) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(corridorBoundary.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        // must have buffer width that is >= 0
        if (corridorBoundary.getBufferWidthPercent() == null
                || corridorBoundary.getBufferWidthPercent().doubleValue() < 0) {
            return "Buffer width percent is null or less than 0";
        }

        msg = validatePathRef(corridorBoundary.getPathRef());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link CorridorPostureCondition} condition.
     *
     * @param corridorPosture the {@link CorridorPostureCondition} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateCorridorPostureCondition(CorridorPostureCondition corridorPosture) {
        if (corridorPosture == null) {
            throw new IllegalArgumentException("The parameter 'corridorPosture' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (corridorPosture.getTeamMemberRefs() == null
                    || corridorPosture.getTeamMemberRefs().getTeamMemberRef().isEmpty()) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(corridorPosture.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        // must have posture with at least 1 posture
        if (corridorPosture.getPostures() == null || corridorPosture.getPostures().getPosture().isEmpty()) {
            return "Postures must have at least 1 posture";
        }

        for (String posture : corridorPosture.getPostures().getPosture()) {
            // must have posture
            if (StringUtils.isBlank(posture)) {
                return "Posture is blank";
            }
        }

        // must have path with at least 1 segment
        msg = validatePathRef(corridorPosture.getPathRef());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link EliminateHostilesCondition} condition.
     *
     * @param eliminateHostiles the {@link EliminateHostilesCondition} to
     *        validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateEliminateHostilesCondition(EliminateHostilesCondition eliminateHostiles) {
        if (eliminateHostiles == null) {
            throw new IllegalArgumentException("The parameter 'eliminateHostiles' cannot be null.");
        }

        // must have entities that contain at least 1 start location
        if (eliminateHostiles.getEntities() == null
                || eliminateHostiles.getEntities().getTeamMemberRef().isEmpty()
                && eliminateHostiles.getEntities().getStartLocation().isEmpty()) {
            return "Entities must contain at least 1 start location or team member reference";
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(eliminateHostiles.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        for (StartLocation startLocation : eliminateHostiles.getEntities().getStartLocation()) {
            // validate start location
            msg = validateStartLocation(startLocation);
            if (msg != null) {
                return msg;
            }
        }

        // optional real time assessment rules
        if (eliminateHostiles.getRealTimeAssessmentRules() != null) {
            msg = validateRealTimeAssessmentRules(eliminateHostiles.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link EnterAreaCondition} condition.
     *
     * @param enterArea the {@link EnterAreaCondition} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateEnterAreaCondition(EnterAreaCondition enterArea) {
        if (enterArea == null) {
            throw new IllegalArgumentException("The parameter 'enterArea' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (enterArea.getTeamMemberRef() == null) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure team member ref is valid */
        if (enterArea.getTeamMemberRef() != null) {
            boolean valid = TeamsUtil.hasTeamOrTeamMemberWithName(enterArea.getTeamMemberRef(),
                    ScenarioClientUtility.getTeamOrganizationTeam());
            if (!valid) {
                return enterArea.getTeamMemberRef() + " does not exist in the team organization.";
            }
        }

        // must have at least 1 location
        if (enterArea.getEntrance().isEmpty()) {
            return "Entrance must have at least 1 location";
        }

        for (Entrance entrance : enterArea.getEntrance()) {
            String msg = validateEntrance(entrance);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates an {@link Entrance}.
     *
     * @param entrance the {@link Entrance} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateEntrance(Entrance entrance) {
        // must have name
        if (StringUtils.isBlank(entrance.getName())) {
            return "Missing entrance name";
        }

        // must have assessment
        if (StringUtils.isBlank(entrance.getAssessment())) {
            return "Assessment is blank";
        }

        Inside inside = entrance.getInside();
        Outside outside = entrance.getOutside();

        // must have inside and outside
        if (inside == null || outside == null) {
            return "Inside or Outside is null";
        }

        // must have inside's point and proximity
        if (StringUtils.isBlank(inside.getPoint()) || inside.getProximity() == null) {
            return "Inside is missing point or proximity";
        }

        // must have outside's point and proximity
        if (StringUtils.isBlank(outside.getPoint()) || outside.getProximity() == null) {
            return "Outside is missing point or proximity";
        }

        return null;
    }

    /**
     * Validates a {@link ExplosiveHazardSpotReportCondition} condition.
     *
     * @param explosiveHazardSpotReport the
     *        {@link ExplosiveHazardSpotReportCondition} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateExplosiveHazardSpotReportCondition(
            ExplosiveHazardSpotReportCondition explosiveHazardSpotReport) {
        if (explosiveHazardSpotReport == null) {
            throw new IllegalArgumentException("The parameter 'explosiveHazardSpotReport' cannot be null.");
        }

        String msg = validateConditionTeamMemberRefs(explosiveHazardSpotReport.getTeamMemberRefs(), true);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates the provided teamMemberRefs object against the team
     * organization by making sure if there is a team member in the team
     * organization than the provided teamMemberRefs must have at least one
     * entry. Also checks to see if any team members do not exist in the team
     * organization.
     *
     * @param teamMemberRefs the list of team members to validate
     * @param required true if the condition requires at least one team member
     *        ref to exist; false if it is optional.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateConditionTeamMemberRefs(TeamMemberRefs teamMemberRefs, boolean required) {
        if (required && ScenarioClientUtility.getAnyTeamMemberName() != null) {
            /* There is at least 1 team member, there must be at least 1 team
             * member picked */
            if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef().isEmpty()) {
                return "A team member must be specified.";
            }
        }

        String msg = validateTeamMembersExist(teamMemberRefs);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link GenericConditionInput} condition.
     *
     * @param genericInput the {@link GenericConditionInput} to validate. Can't
     *        be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateGenericConditionInput(GenericConditionInput genericInput) {
        if (genericInput == null) {
            throw new IllegalArgumentException("The parameter 'genericInput' cannot be null.");
        }

        // must have at least 1 parameter pair
        if (genericInput.getNvpair().isEmpty()) {
            return "Generic param must have at least 1 parameter pair";
        }

        // each nv pair must have a name and value
        Set<String> uniqueNames = new HashSet<>();
        for (Nvpair nvPair : genericInput.getNvpair()) {
            String pairName = nvPair.getName();
            if (StringUtils.isBlank(pairName) || StringUtils.isBlank(nvPair.getValue())) {
                return "Generic param is missing the name or value";
            } else if (uniqueNames.contains(pairName)) {
                return "Generic param has duplciate name '" + pairName + "'";
            }
            uniqueNames.add(pairName);
        }

        return null;
    }

    /**
     * Validates a {@link HaltConditionInput} condition.
     *
     * @param haltCondition the {@link HaltConditionInput} to validate. Can't be
     *        null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateHaltCondition(HaltConditionInput haltCondition) {
        if (haltCondition == null) {
            throw new IllegalArgumentException("The parameter 'haltCondition' cannot be null.");
        }

        // optional real time assessment rules
        if (haltCondition.getRealTimeAssessmentRules() != null) {
            String msg = validateRealTimeAssessmentRules(haltCondition.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            final TeamMemberRefs teamMemberRefs = haltCondition.getTeamMemberRefs();
            if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef() == null
                    || teamMemberRefs.getTeamMemberRef().isEmpty()) {
                return "Halt condition requires at least one team member";
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(haltCondition.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link HealthConditionInput} condition.
     *
     * @param healthCondition the {@link HealthConditionInput} to validate. Can't be
     *        null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateHealthCondition(HealthConditionInput healthCondition) {
        if (healthCondition == null) {
            throw new IllegalArgumentException("The parameter 'healthCondition' cannot be null.");
        }

        // optional real time assessment rules
        if (healthCondition.getRealTimeAssessmentRules() != null) {
            String msg = validateRealTimeAssessmentRules(healthCondition.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            final TeamMemberRefs teamMemberRefs = healthCondition.getTeamMemberRefs();
            if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef() == null
                    || teamMemberRefs.getTeamMemberRef().isEmpty()) {
                return "Health condition requires at least one team member";
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(healthCondition.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link HasMovedExcavatorComponentInput} condition.
     *
     * @param hasMovedExcavatorInput the {@link HasMovedExcavatorComponentInput}
     *        to validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateHasMovedExcavatorComponentInput(HasMovedExcavatorComponentInput hasMovedExcavatorInput) {
        if (hasMovedExcavatorInput == null) {
            throw new IllegalArgumentException("The parameter 'hasMovedExcavatorComponentInput' cannot be null.");
        }

        // must have 1-4 components
        int componentSize = hasMovedExcavatorInput.getComponent().size();
        if (componentSize < 1 || componentSize > 4) {
            return "Must have between 1-4 components";
        }

        for (Component component : hasMovedExcavatorInput.getComponent()) {
            // each component must have a component type
            if (component.getComponentType() == null) {
                return "Component type is null";
            }

            // optional direction type
            Serializable directionType = component.getDirectionType();
            if (directionType != null) {
                // any direction must be >= 0
                if (directionType instanceof Double) {
                    Double anyDirection = (Double) directionType;
                    if (anyDirection < 0) {
                        return "Any direction is less than 0";
                    }
                }

                // bidirectional must have negative and positive rotation >= 0
                if (directionType instanceof Bidirectional) {
                    Bidirectional biDirectional = (Bidirectional) directionType;
                    if (biDirectional.getNegativeRotation() < 0 || biDirectional.getPositiveRotation() < 0) {
                        return "Negative or Positive rotation is less than 0";
                    }
                }
            }
        }

        // max assessments is optional, but if it exists, must be >= 0
        if (hasMovedExcavatorInput.getMaxAssessments() != null) {
            if (hasMovedExcavatorInput.getMaxAssessments().intValue() < 0) {
                return "Max assessments is less than 0";
            }
        }

        return null;
    }

    /**
     * Validates a {@link IdentifyPOIsCondition} condition.
     *
     * @param identifyPOIs the {@link IdentifyPOIsCondition} to validate. Can't
     *        be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateIdentifyPOIsCondition(IdentifyPOIsCondition identifyPOIs) {
        if (identifyPOIs == null) {
            throw new IllegalArgumentException("The parameter 'identifyPOIs' cannot be null.");
        } else if (ScenarioClientUtility.isPlayback()) {
            return "The IdentifyPOIsCondition requires a running training application in order to assess properly but this course object is configured for log file playback only.";
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (identifyPOIs.getTeamMemberRefs() == null
                    || identifyPOIs.getTeamMemberRefs().getTeamMemberRef().isEmpty()) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(identifyPOIs.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        // must have pois with at least 1 point
        if (identifyPOIs.getPois() == null || identifyPOIs.getPois().getPointRef().isEmpty()) {
            return "POIs must have at least 1 point";
        }

        for (PointRef pointRef : identifyPOIs.getPois().getPointRef()) {
            // validate each point ref
            msg = validatePointRef(pointRef, ScenarioClientUtility.getDisallowedCoordinateTypes(identifyPOIs.getClass()));
            if (msg != null) {
                return msg;
            }
        }

        // optional real time assessment rules
        if (identifyPOIs.getRealTimeAssessmentRules() != null) {
            msg = validateRealTimeAssessmentRules(identifyPOIs.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link LifeformTargetAccuracyCondition} condition.
     *
     * @param lifeformTargetAccuracy the {@link LifeformTargetAccuracyCondition}
     *        to validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateLifeformTargetAccuracyCondition(LifeformTargetAccuracyCondition lifeformTargetAccuracy) {
        if (lifeformTargetAccuracy == null) {
            throw new IllegalArgumentException("The parameter 'lifeformTargetAccuracy' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (lifeformTargetAccuracy.getTeamMemberRefs() == null
                    || lifeformTargetAccuracy.getTeamMemberRefs().getTeamMemberRef().isEmpty()) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(lifeformTargetAccuracy.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        // must have entities with at least 1 start location
        if (lifeformTargetAccuracy.getEntities() == null
                || lifeformTargetAccuracy.getEntities().getTeamMemberRef().isEmpty()
                && lifeformTargetAccuracy.getEntities().getStartLocation().isEmpty()) {
            return "Entities must have at least 1 start location";
        }

        for (StartLocation startLocation : lifeformTargetAccuracy.getEntities().getStartLocation()) {
            // validate each start location
            msg = validateStartLocation(startLocation);
            if (msg != null) {
                return msg;
            }
        }

        // optional real time assessment rules
        if (lifeformTargetAccuracy.getRealTimeAssessmentRules() != null) {
            msg = validateRealTimeAssessmentRules(lifeformTargetAccuracy.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link MarksmanshipPrecisionCondition} condition.
     *
     * @param marksmanshipPrecision the {@link MarksmanshipPrecisionCondition}
     *        to validate. Can't be null. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateMarksmanshipPrecisionCondition(MarksmanshipPrecisionCondition marksmanshipPrecision) {
        if (marksmanshipPrecision == null) {
            throw new IllegalArgumentException("The parameter 'marksmanshipPrecision' cannot be null.");
        }

        // optional expected number of shots
        if (marksmanshipPrecision.getExpectedNumberOfShots() != null) {
            // if it exists, must have at least 1 character
            if (StringUtils.isBlank(marksmanshipPrecision.getExpectedNumberOfShots())) {
                return "Missing expected number of shots";
            }
        }

        return null;
    }

    /**
     * Validates a {@link MarksmanshipSessionCompleteCondition} condition.
     *
     * @param marksmanshipSessionComplete the
     *        {@link MarksmanshipSessionCompleteCondition} to validate.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateMarksmanshipSessionCompleteCondition(
            MarksmanshipSessionCompleteCondition marksmanshipSessionComplete) {
        if (marksmanshipSessionComplete == null) {
            throw new IllegalArgumentException("The parameter 'marksmanshipSessionComplete' cannot be null.");
        }

        // optional expected number of shots
        if (marksmanshipSessionComplete.getExpectedNumberOfShots() != null) {
            // if it exists, must have at least 1 character
            if (StringUtils.isBlank(marksmanshipSessionComplete.getExpectedNumberOfShots())) {
                return "Missing expected number of shots";
            }
        }

        return null;
    }
    
    /**
     * Validates a {@link DetectObjectsCondition} condition.
     *
     * @param detectObjects The {@link DetectObjectsCondition} to validate.
     * @return An error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateDetectObjectsCondition(DetectObjectsCondition detectObjects){
        if (detectObjects == null) {
            throw new IllegalArgumentException("The parameter 'detectObjects' cannot be null.");
        }

        final Integer fovAngle = detectObjects.getFieldOfView();
        final Integer orientAngle = detectObjects.getOrientAngle();
        if(fovAngle == null && orientAngle != null){
            return "The field of view angle must be specified if the orient angle is specified.";
        }else if(fovAngle != null && orientAngle == null){
            return "The orient angle must be specified if the field of view angle is specified.";
        }else if(fovAngle != null && orientAngle != null){
            
            if(orientAngle > fovAngle){
                return "The field of view angle must be greater than the orient angle";
            }
        }
        
        if (fovAngle != null && (fovAngle < 1 || fovAngle > 360)) {
            return "The field of view angle must be between 1 and 360 (inclusive).";
        }
            
        if (orientAngle != null && (orientAngle < 1 || orientAngle > 360)) {
            return "The orient angle must be between 1 and 360 (inclusive).";
        }
        
        final BigDecimal aboveTime = detectObjects.getAboveExpectationUpperBound();
        final BigDecimal atTime = detectObjects.getAtExpectationUpperBound();
        if(aboveTime == null && atTime != null){
            return "The At Expectation time must be specified if the Above Expectation time is specified";
        }else if(aboveTime != null && atTime == null){
            return "The Above Expectation time must be specified if the At Expectation time is specified";
        }else if(aboveTime != null && atTime != null){
            
            if(aboveTime.doubleValue() >= atTime.doubleValue()){
                return "The Above Expectation time must be less than the At Expectation time";
            }else if(aboveTime.doubleValue() < 0){
                return "The Above Expectation time can't be negative";
            }else if(atTime.doubleValue() < 0){
                return "The At Expectation time can't be negative";
            }
        }
        
        final BigInteger maxDistance = detectObjects.getViewMaxDistance();
        if(maxDistance != null && maxDistance.compareTo(BigInteger.ZERO) <= 0){
            return "The max distance must be greater than zero.";
        }

        final TeamMemberRefs teamMemberRefs = detectObjects.getTeamMemberRefs();
        if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef().size() < 1) {
            return "At least one team member must be assessed";
        }

        final String validateTeamMembersExist = validateTeamMembersExist(teamMemberRefs);
        if (validateTeamMembersExist != null) {
            return validateTeamMembersExist;
        }
        
        final generated.dkf.DetectObjectsCondition.ObjectsToDetect objectsToDetect = detectObjects.getObjectsToDetect();
        if(objectsToDetect == null){
            return "There are no objects to detect";
        }
        
        List<Serializable> objectsToDetectList = objectsToDetect.getTeamMemberRefOrPointRef();
        for(Serializable obj : objectsToDetectList){
            
            if(obj instanceof TeamMemberRef){
                final String validateTargetTeamMemberExist = validateTeamMemberExist((TeamMemberRef) obj);
                if (validateTargetTeamMemberExist != null) {
                    return validateTargetTeamMemberExist;
                }
            }else if(obj instanceof PointRef){
                final String validatePointRef = validatePointRef((PointRef) obj, ScenarioClientUtility.getDisallowedCoordinateTypes(detectObjects.getClass()));
                if(validatePointRef != null){
                    return validatePointRef;
                }
            }
        }
        
        
        return null;
    }
    
    /**
     * Validates a {@link NegligentDischargeCondition} condition.
     *
     * @param condition The {@link NegligentDischargeCondition} to validate.
     * @return An error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateNegligentDischargeCondition(NegligentDischargeCondition condition){
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }
        
        final Integer weaponConeAngle = condition.getWeaponConeAngle();
        if (weaponConeAngle < 1 || weaponConeAngle > 360) {
            return "The weapon cone angle must be between 1 and 360 (inclusive).";
        }
        
        final BigInteger maxDistance = condition.getWeaponConeMaxDistance();
        if(maxDistance != null && maxDistance.compareTo(BigInteger.ZERO) <= 0){
            return "The max distance must be greater than zero.";
        }

        final TeamMemberRefs teamMemberRefs = condition.getTeamMemberRefs();
        if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef().size() < 1) {
            return "At least one team member must be assessed";
        }

        final String validateTeamMembersExist = validateTeamMembersExist(teamMemberRefs);
        if (validateTeamMembersExist != null) {
            return validateTeamMembersExist;
        }
        
        final generated.dkf.NegligentDischargeCondition.TargetsToAvoid targetsToAvoid = condition.getTargetsToAvoid();
        if(targetsToAvoid == null){
            return "There are no objects to avoid";
        }
        
        List<Serializable> targetObjects = targetsToAvoid.getTeamMemberRefOrPointRef();
        for(Serializable targetObj : targetObjects){
            
            if(targetObj instanceof TeamMemberRef){
                final String validateTargetTeamMemberExist = validateTeamMemberExist((TeamMemberRef) targetObj);
                if (validateTargetTeamMemberExist != null) {
                    return validateTargetTeamMemberExist;
                }
            }else if(targetObj instanceof PointRef){
                final String validatePointRef = validatePointRef((PointRef) targetObj, ScenarioClientUtility.getDisallowedCoordinateTypes(condition.getClass()));
                if(validatePointRef != null){
                    return validatePointRef;
                }
            }
        } 
        
        // optional real time assessment rules
        if (condition.getRealTimeAssessmentRules() != null) {
            String msg = validateRealTimeAssessmentRules(condition.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }
    
    /**
     * Validates a {@link EngageTargetsCondition} condition.
     *
     * @param engageTargets The {@link EngageTargetsCondition} to validate.
     * @return An error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateEngageTargetsCondition(EngageTargetsCondition engageTargets){
        if (engageTargets == null) {
            throw new IllegalArgumentException("The parameter 'engageTargets' cannot be null.");
        }
        
        final Integer weaponConeAngle = engageTargets.getWeaponConeAngle();
        if (weaponConeAngle < 1 || weaponConeAngle > 360) {
            return "The weapon cone angle must be between 1 and 360 (inclusive).";
        }
        
        final BigInteger maxDistance = engageTargets.getWeaponConeMaxDistance();
        if(maxDistance != null && maxDistance.compareTo(BigInteger.ZERO) <= 0){
            return "The max distance must be greater than zero.";
        }

        final TeamMemberRefs teamMemberRefs = engageTargets.getTeamMemberRefs();
        if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef().size() < 1) {
            return "At least one team member must be assessed";
        }

        final String validateTeamMembersExist = validateTeamMembersExist(teamMemberRefs);
        if (validateTeamMembersExist != null) {
            return validateTeamMembersExist;
        }
        
        final generated.dkf.EngageTargetsCondition.TargetsToEngage targetsToEngage = engageTargets.getTargetsToEngage();
        if(targetsToEngage == null){
            return "There are no targets to engage";
        }
        
        List<Serializable> targetObjects = targetsToEngage.getTeamMemberRefOrPointRef();
        for(Serializable targetObj : targetObjects){
            
            if(targetObj instanceof TeamMemberRef){
                final String validateTargetTeamMemberExist = validateTeamMemberExist((TeamMemberRef) targetObj);
                if (validateTargetTeamMemberExist != null) {
                    return validateTargetTeamMemberExist;
                }
            }else if(targetObj instanceof PointRef){
                final String validatePointRef = validatePointRef((PointRef) targetObj, ScenarioClientUtility.getDisallowedCoordinateTypes(engageTargets.getClass()));
                if(validatePointRef != null){
                    return validatePointRef;
                }
            }
        }  
        
        final BigDecimal aboveTime = engageTargets.getAboveExpectationUpperBound();
        final BigDecimal atTime = engageTargets.getAtExpectationUpperBound();
        if(aboveTime == null && atTime != null){
            return "The At Expectation time must be specified if the Above Expectation time is specified";
        }else if(aboveTime != null && atTime == null){
            return "The Above Expectation time must be specified if the At Expectation time is specified";
        }else if(aboveTime != null && atTime != null){
            
            if(aboveTime.doubleValue() >= atTime.doubleValue()){
                return "The Above Expectation time must be less than the At Expectation time";
            }else if(aboveTime.doubleValue() < 0){
                return "The Above Expectation time can't be negative";
            }else if(atTime.doubleValue() < 0){
                return "The At Expectation time can't be negative";
            }
        }

        return null;        
    }    
    
    /**
     * Validates a {@link FireTeamRateOfFireCondition} condition.
     *
     * @param assignedSector The {@link FireTeamRateOfFireCondition} to validate.
     * @return An error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateFireTeamRateOfFireCondition(FireTeamRateOfFireCondition rateOfFire){
        if (rateOfFire == null) {
            throw new IllegalArgumentException("The parameter 'rateOfFire' cannot be null.");
        }
        
        BigDecimal atUpperBound = rateOfFire.getAtExpectationUpperBound();
        BigDecimal belowUpperBound = rateOfFire.getBelowExpectationUpperBound();
        if(atUpperBound == null){
            return "The upper bound for At Expectation is not set.";
        }else if(belowUpperBound == null){
            return "The upper bound for Below Expectation is not set.";
        }else if(atUpperBound.doubleValue() < belowUpperBound.doubleValue()){
            return "The below expectation upper bound is greater than the at expectation upper bound.";
        }

        final TeamMemberRefs teamMemberRefs = rateOfFire.getTeamMemberRefs();
        if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef().size() < 1) {
            return "At least one team member must be assessed";
        }

        final String validateTeamMembersExist = validateTeamMembersExist(teamMemberRefs);
        if (validateTeamMembersExist != null) {
            return validateTeamMembersExist;
        }

        return null;
    }
    
    /**
     * Validates a {@link AssignedSectorCondition} condition.
     *
     * @param assignedSector The {@link AssignedSectorCondition} to validate.
     * @return An error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateAssignedSectorCondition(AssignedSectorCondition assignedSector){
        if (assignedSector == null) {
            throw new IllegalArgumentException("The parameter 'assignedSector' cannot be null.");
        }

        final BigDecimal maxAngle = assignedSector.getMaxAngleFromCenter();
        if (maxAngle == null) {
            return "The max angle from center is required.";
        }

        if (maxAngle.compareTo(BigDecimal.ZERO) <= 0 || maxAngle.compareTo(BigDecimal.valueOf(180)) >= 0) {
            return "The max angle from center must be between 0 and 180 (exclusive).";
        }
        
        PointRef pointRef = assignedSector.getPointRef();
        if(pointRef == null){
            return "Select the center point of the assigned sector.";
        }
        
        String pointRefValidation = validatePointRef(pointRef, ScenarioClientUtility.getDisallowedCoordinateTypes(assignedSector.getClass()));
        if(StringUtils.isNotBlank(pointRefValidation)){
            return "The selected center point of the assigned sector isn't valid because "+pointRefValidation;
        }
        
        final BigDecimal freeLookDuration = assignedSector.getFreeLookDuration();
        if(freeLookDuration != null && freeLookDuration.compareTo(BigDecimal.ZERO) < 0){
            return "The free look duration must be equal to or greater than zero.";
        }

        final TeamMemberRefs teamMemberRefs = assignedSector.getTeamMemberRefs();
        if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef().size() < 1) {
            return "At least one team member must be assessed";
        }

        final String validateTeamMembersExist = validateTeamMembersExist(teamMemberRefs);
        if (validateTeamMembersExist != null) {
            return validateTeamMembersExist;
        }

        return null;
        
    }

    /**
     * Validates a {@link MuzzleFlaggingCondition} condition.
     *
     * @param muzzleFlagging The {@link MuzzleFlaggingCondition} to validate.
     * @return An error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateMuzzleFlaggingCondition(MuzzleFlaggingCondition muzzleFlagging) {
        if (muzzleFlagging == null) {
            throw new IllegalArgumentException("The parameter 'muzzleFlagging' cannot be null.");
        }

        final BigDecimal maxAngle = muzzleFlagging.getMaxAngle();
        if (maxAngle == null) {
            return "The max angle is required.";
        }

        if (maxAngle.compareTo(BigDecimal.ZERO) <= 0 || maxAngle.compareTo(BigDecimal.valueOf(180)) >= 0) {
            return "The max angle must be between 0 and 180 (exclusive).";
        }

        final BigDecimal maxDistance = muzzleFlagging.getMaxDistance();
        if (maxDistance != null && maxDistance.compareTo(BigDecimal.ZERO) <= 0) {
            return "The max distance is optional but must be greater than 0.";
        }

        final TeamMemberRefs teamMemberRefs = muzzleFlagging.getTeamMemberRefs();
        if (teamMemberRefs == null || teamMemberRefs.getTeamMemberRef().size() < 2) {
            return "At least two team members must be assessed";
        }

        final String validateTeamMembersExist = validateTeamMembersExist(teamMemberRefs);
        if (validateTeamMembersExist != null) {
            return validateTeamMembersExist;
        }

        return null;
    }

    /**
     * Validates a {@link NineLineReportCondition} condition.
     *
     * @param nineLineReport the {@link NineLineReportCondition} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateNineLineReportCondition(NineLineReportCondition nineLineReport) {
        if (nineLineReport == null) {
            throw new IllegalArgumentException("The parameter 'nineLineReport' cannot be null.");
        }

        String msg = validateConditionTeamMemberRefs(nineLineReport.getTeamMemberRefs(), true);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link NoConditionInput} condition.
     *
     * @param noConditionInput the {@link NoConditionInput} to validate. Can't
     *        be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateNoConditionInput(NoConditionInput noConditionInput) {
        if (noConditionInput == null) {
            throw new IllegalArgumentException("The parameter 'noConditionInput' cannot be null.");
        }

        // nothing to validate

        return null;
    }

    /**
     * Validates a {@link NumberOfShotsFiredCondition} condition.
     *
     * @param numberOfShotsFired the {@link NumberOfShotsFiredCondition} to
     *        validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateNumberOfShotsFiredCondition(NumberOfShotsFiredCondition numberOfShotsFired) {
        if (numberOfShotsFired == null) {
            throw new IllegalArgumentException("The parameter 'numberOfShotsFired' cannot be null.");
        }

        // optional expected number of shots
        if (numberOfShotsFired.getExpectedNumberOfShots() != null) {
            // if it exists, must have at least 1 character
            if (StringUtils.isBlank(numberOfShotsFired.getExpectedNumberOfShots())) {
                return "Missing expected number of shots";
            }
        }

        return null;
    }

    /**
     * Validates a {@link ObservedAssessmentCondition} condition.
     *
     * @param observedAssessment the {@link ObservedAssessmentCondition} to
     *        validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateObservedAssessmentCondition(ObservedAssessmentCondition observedAssessment) {
        if (observedAssessment == null) {
            throw new IllegalArgumentException("The parameter 'observedAssessment' cannot be null.");
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(observedAssessment.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link PaceCountCondition} condition.
     *
     * @param paceCount the {@link PaceCountCondition} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validatePaceCountCondition(PaceCountCondition paceCount) {
        if (paceCount == null) {
            throw new IllegalArgumentException("The parameter 'paceCount' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (paceCount.getTeamMemberRef() == null) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure team member ref is valid */
        if (paceCount.getTeamMemberRef() != null) {
            boolean valid = TeamsUtil.hasTeamOrTeamMemberWithName(paceCount.getTeamMemberRef(),
                    ScenarioClientUtility.getTeamOrganizationTeam());
            if (!valid) {
                return paceCount.getTeamMemberRef() + " does not exist in the team organization.";
            }
        }

        // expected distance must be greater than 0
        if (paceCount.getExpectedDistance() <= 0) {
            return "Expected distance is less than or equal to 0";
        }

        /* There must be both start and end pace count learner action for this
         * condition to be valid. Don't need to check that the
         * PaceCountCondition exists since we are inside that condition already
         * (passing false into the method). */
        String msg = validatePaceCountLearnerActionDependency(ScenarioClientUtility.getUnmodifiableLearnerActionList(),
                false);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link PowerPointDwellCondition} condition.
     *
     * @param powerPointDwell the {@link PowerPointDwellCondition} to validate.
     *        Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validatePowerPointDwellCondition(PowerPointDwellCondition powerPointDwell) {
        if (powerPointDwell == null) {
            throw new IllegalArgumentException("The parameter 'powerPointDwell' cannot be null.");
        }

        Default pptDefault = powerPointDwell.getDefault();
        Slides slides = powerPointDwell.getSlides();

        // must have default and slides
        if (pptDefault == null || slides == null) {
            return "Missing default or slides";
        }

        // default time in seconds must be >= 0
        if (pptDefault.getTimeInSeconds() < 0) {
            return "Default time is less than 0";
        }

        // empty means no duplicates
        boolean hasDuplicateSlideIndex = false;
        boolean hasNonZeroValue = pptDefault.getTimeInSeconds() != 0;

        Set<BigInteger> slideIndices = new HashSet<>();
        for (Slide slide : slides.getSlide()) {
            // slide time in seconds must be >= 0
            if (slide.getTimeInSeconds() < 0) {
                return "Slide time is less than 0";
            }

            // check for nonzero value
            hasNonZeroValue |= slide.getTimeInSeconds() != 0;

            // slide index must be >= 1
            if (slide.getIndex() == null || slide.getIndex().intValue() < 1) {
                return "Slide index is null or less than 1";
            }

            // check for duplicate slide index; populate set with index
            hasDuplicateSlideIndex |= slideIndices.contains(slide.getIndex());
            slideIndices.add(slide.getIndex());
        }

        // at least 1 slide must have a nonzero value
        if (!hasNonZeroValue) {
            return "Must have at least one slide with a nonzero value";
        }

        // cannot have duplicate slide indices
        if (hasDuplicateSlideIndex) {
            return "Cannot have duplicate slide indices";
        }

        return null;
    }
    
    /**
     * Validates a {@link RequestExternalAttributeCondition} condition.
     * 
     * @param requestExternalAttribute the {@link RequestExternalAttributeCondition} to 
     *          validate.  Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateRequestExternalAttributeCondition(RequestExternalAttributeCondition requestExternalAttribute){
        if (requestExternalAttribute == null) {
            throw new IllegalArgumentException("The parameter 'requestExternalAttribute' cannot be null.");
        }
        
        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(requestExternalAttribute.getTeamMemberRefs(), true);
        if (msg != null) {
            return msg;
        }
        
        ExternalAttributeEnumType type = requestExternalAttribute.getAttributeType();
        if(type == null){
            return "The attribute type is null";
        }
        
        // the weapon state type doesn't require an attribute name
        String attrName = requestExternalAttribute.getAttributeName();
        if(type != ExternalAttributeEnumType.WEAPON_STATE && StringUtils.isBlank(attrName)){
            return "The attribute name is blank";
        }
        
        return null;
    }

    /**
     * Validates a {@link RulesOfEngagementCondition} condition.
     *
     * @param rulesOfEngagement the {@link RulesOfEngagementCondition} to
     *        validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateRulesOfEngagementCondition(RulesOfEngagementCondition rulesOfEngagement) {
        if (rulesOfEngagement == null) {
            throw new IllegalArgumentException("The parameter 'rulesOfEngagement' cannot be null.");
        }

        /* this isn't in the schema, but this condition requires a learner start
         * location */
        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (rulesOfEngagement.getTeamMemberRefs() == null
                    || rulesOfEngagement.getTeamMemberRefs().getTeamMemberRef().isEmpty()) {
                if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
                    return "No team member reference or learner start location has been provided.";
                }
            }
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(rulesOfEngagement.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        // must have wcs
        if (rulesOfEngagement.getWcs() == null) {
            return "WCS is null";
        }

        // must have value for wcs
        if (rulesOfEngagement.getWcs().getValue() == null) {
            return "WCS value is null";
        }

        // optional real time assessment rules
        if (rulesOfEngagement.getRealTimeAssessmentRules() != null) {
            msg = validateRealTimeAssessmentRules(rulesOfEngagement.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link SIMILEConditionInput} condition.
     *
     * @param simileInput the {@link SIMILEConditionInput} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateSIMILEConditionInput(SIMILEConditionInput simileInput) {
        if (simileInput == null) {
            throw new IllegalArgumentException("The parameter 'simileInput' cannot be null.");
        }

        if (ScenarioClientUtility.isJRE64Bit()) {
            return "The SIMILE condition does not support a 64-bit JRE.";
        }

        // must have configuration file with at least 5 characters
        if (StringUtils.isBlank(simileInput.getConfigurationFile())
                || simileInput.getConfigurationFile().length() < 5) {
            return "Configuration file must be at least 5 characters";
        }

        // must have condition key
        if (StringUtils.isBlank(simileInput.getConditionKey())) {
            return "Missing condition key";
        }

        return null;
    }

    /**
     * Validates a {@link SpacingCondition} condition.
     *
     * @param spacing the {@link SpacingCondition} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateSpacingConditionInput(SpacingCondition spacing) {

        if (spacing == null) {
            throw new IllegalArgumentException("The parameter 'spacing' cannot be null.");
        }

        if (spacing.getSpacingPair().isEmpty()) {
            return "At least pair of learners to maintain spacing must be specified.";
        }

        for (SpacingPair pair : spacing.getSpacingPair()) {
            String msg = validateSpacingPair(pair);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link SpacingPair}.
     *
     * @param pair the {@link SpacingPair} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateSpacingPair(SpacingPair pair) {
        final Team team = ScenarioClientUtility.getTeamOrganizationTeam();

        if (pair.getFirstObject() == null || StringUtils.isBlank(pair.getFirstObject().getTeamMemberRef())) {
            return "Missing first object's team member reference";
        }

        /* Make sure first team member ref is valid */
        if (!TeamsUtil.hasTeamOrTeamMemberWithName(pair.getFirstObject().getTeamMemberRef(), team)) {
            return pair.getFirstObject().getTeamMemberRef() + " does not exist in the team organization.";
        }

        if (pair.getSecondObject() == null || StringUtils.isBlank(pair.getSecondObject().getTeamMemberRef())) {
            return "Missing second object's team member reference";
        }

        /* Make sure second team member ref is valid */
        if (!TeamsUtil.hasTeamOrTeamMemberWithName(pair.getSecondObject().getTeamMemberRef(), team)) {
            return pair.getSecondObject().getTeamMemberRef() + " does not exist in the team organization.";
        }

        if (pair.getIdeal() == null) {
            return "Missing ideal spacing range";
        }

        if (pair.getIdeal().getIdealMinSpacing() == null) {
            return "Missing ideal minimum spacing";
        }

        if (pair.getIdeal().getIdealMaxSpacing() == null) {
            return "Missing ideal maximum spacing";
        }

        if (pair.getAcceptable() == null) {
            return "Missing ideal spacing range";
        }

        if (pair.getAcceptable().getAcceptableMinSpacing() == null) {
            return "Missing acceptable minimum spacing";
        }

        if (pair.getAcceptable().getAcceptableMaxSpacing() == null) {
            return "Missing acceptable maximum spacing";
        }

        return null;
    }

    /**
     * Validates a {@link SpeedLimitCondition} condition.
     *
     * @param speedLimit the {@link SpeedLimitCondition} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateSpeedLimitConditionInput(SpeedLimitCondition speedLimit) {
        if (speedLimit == null) {
            throw new IllegalArgumentException("The parameter 'speedLimit' cannot be null.");
        }

        if (speedLimit.getSpeedLimit() == null || speedLimit.getSpeedLimit().compareTo(BigDecimal.ZERO) <= 0) {
            return "A speed of greater than zero must be specified.";
        }

        if (speedLimit.getTeamMemberRef() == null) {
            return "The team member the speed limit applies to must be specified";
        }

        /* Make sure team member ref is valid */
        boolean valid = TeamsUtil.hasTeamOrTeamMemberWithName(speedLimit.getTeamMemberRef(),
                ScenarioClientUtility.getTeamOrganizationTeam());
        if (!valid) {
            return speedLimit.getTeamMemberRef() + " does not exist in the team organization.";
        }
        
        // optional real time assessment rules
        if (speedLimit.getRealTimeAssessmentRules() != null) {
            String msg = validateRealTimeAssessmentRules(speedLimit.getRealTimeAssessmentRules());
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates a {@link SpotReportCondition} condition.
     *
     * @param spotReport the {@link SpotReportCondition} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateSpotReportCondition(SpotReportCondition spotReport) {
        if (spotReport == null) {
            throw new IllegalArgumentException("The parameter 'spotReport' cannot be null.");
        }

        String msg = validateConditionTeamMemberRefs(spotReport.getTeamMemberRefs(), true);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link TimerConditionInput} condition.
     *
     * @param timerInput the {@link TimerConditionInput} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateTimerConditionInput(TimerConditionInput timerInput) {
        if (timerInput == null) {
            throw new IllegalArgumentException("The parameter 'timerInput' cannot be null.");
        }

        // must have repeatable flag
        if (timerInput.getRepeatable() == null) {
            return "Repeatable flag is null";
        }

        // must have interval that is > 0
        if (timerInput.getInterval() == null || timerInput.getInterval().doubleValue() <= 0) {
            return "Interval is null or less than or equal to 0";
        }

        /* Make sure any team member refs are valid */
        String msg = validateConditionTeamMemberRefs(timerInput.getTeamMemberRefs(), false);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link UseRadioCondition} condition.
     *
     * @param useRadio the {@link UseRadioCondition} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    private String validateUseRadioCondition(UseRadioCondition useRadio) {
        if (useRadio == null) {
            throw new IllegalArgumentException("The parameter 'useRadio' cannot be null.");
        }

        String msg = validateConditionTeamMemberRefs(useRadio.getTeamMemberRefs(), true);
        if (msg != null) {
            return msg;
        }

        return null;
    }

    ////////////////////////////////////////////////////////////
    /////////// SCENARIO PROPERTY VALIDATION METHODS ///////////
    ////////////////////////////////////////////////////////////

    /**
     * Validates a {@link LearnerId}.
     *
     * @param learnerId the {@link LearnerId} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateLearnerId(LearnerId learnerId) {
        if (learnerId == null) {
            throw new IllegalArgumentException("The parameter 'learnerId' cannot be null.");
        }

        Serializable learnerIdType = learnerId.getType();
        if (learnerIdType == null) {
            return "Learner Id is missing information (e.g. learner location)";
        } else if (learnerIdType instanceof generated.dkf.StartLocation) {
            return validateStartLocation((generated.dkf.StartLocation) learnerIdType);
        } else if (learnerIdType instanceof String) {
            if (StringUtils.isBlank((String) learnerIdType)) {
                return "Learner Id entity marking is missing";
            } else {
                return null;
            }
        } else {
            return "Learner Id has a value that is not being checked";
        }

    }

    /**
     * Validates the {@link generated.dkf.Scenario.EndTriggers end triggers}.
     *
     * @param endTriggers the {@link generated.dkf.Scenario.EndTriggers end
     *        triggers} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateScenarioEndTriggers(Scenario.EndTriggers endTriggers) {
        if (endTriggers == null) {
            throw new IllegalArgumentException("The parameter 'endTriggers' cannot be null.");
        }

        for (Scenario.EndTriggers.Trigger trigger : endTriggers.getTrigger()) {
            String msg = validateScenarioEndTrigger(trigger);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates {@link generated.dkf.Scenario.EndTriggers.Trigger}.
     *
     * @param trigger the {@link generated.dkf.Scenario.EndTriggers.Trigger} to
     *        validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateScenarioEndTrigger(Scenario.EndTriggers.Trigger trigger) {
        // optional strategy
        if (trigger.getMessage() != null) {
            generated.dkf.Strategy strategy = trigger.getMessage().getStrategy();
            if (strategy != null) {
                String msg = validateStrategy(strategy);
                if (msg != null) {
                    return msg;
                }
            }
        }

        // optional delay
        if (trigger.getTriggerDelay() != null) {
            // if it exists, must have value >= 0
            if (trigger.getTriggerDelay().doubleValue() < 0) {
                return "Trigger delay is less than 0";
            }
        }

        // validate trigger type
        String msg = validateTriggerType(trigger.getTriggerType());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates an {@link AvailableLearnerActions}.
     *
     * @param availableLearnerActions the {@link AvailableLearnerActions} to
     *        validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateAvailableLearnerActions(AvailableLearnerActions availableLearnerActions) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validateAvailableLearnerActions(" + availableLearnerActions + ")");
        }

        if (availableLearnerActions == null) {
            throw new IllegalArgumentException("The parameter 'availableLearnerActions' cannot be null.");
        }

        LearnerActionsFiles learnerActionsFiles = availableLearnerActions.getLearnerActionsFiles();
        LearnerActionsList learnerActionsList = availableLearnerActions.getLearnerActionsList();

        // optional learner actions files
        if (learnerActionsFiles != null) {
            // if it exists, each file must have at least 5 characters
            for (String file : learnerActionsFiles.getFile()) {
                if (StringUtils.isBlank(file) || file.length() < 5) {
                    return "File must be at least 5 characters";
                }
            }
        }

        // optional learner actions
        if (learnerActionsList != null) {

            final List<LearnerAction> learnerActions = learnerActionsList.getLearnerAction();
            String msg = validatePaceCountLearnerActionDependency(learnerActions, true);
            if (msg != null) {
                return msg;
            }

            msg = validateAvoidLocationLearnerActionDependency(learnerActions, true);

            /* If it exists, validate each learner action */
            for (LearnerAction learnerAction : learnerActions) {
                msg = validateLearnerAction(learnerAction);
                if (msg != null) {
                    return msg;
                }
            }
        }

        return null;
    }

    /**
     * Validate the dependency between the {@link PaceCountCondition} and the
     * learner actions {@link LearnerActionEnumType#START_PACE_COUNT start pace
     * count} and {@link LearnerActionEnumType#END_PACE_COUNT end pace count}.
     * If one exists, all three should exist.
     *
     * @param learnerActions the learner actions to search for
     *        {@link LearnerActionEnumType#START_PACE_COUNT start pace count}
     *        and {@link LearnerActionEnumType#END_PACE_COUNT end pace count}
     * @param verifyPaceCountConditionExists true to check if the
     *        {@link PaceCountCondition} exists in the scenario; false to assume
     *        it exists and to solely search the learner actions.
     * @return an error message if the dependency check fails validation
     *         describing what caused it to fail; null if it passes validation.
     */
    public static String validatePaceCountLearnerActionDependency(List<LearnerAction> learnerActions,
            boolean verifyPaceCountConditionExists) {

        boolean hasPaceCountCondition = true;
        if (verifyPaceCountConditionExists) {
            hasPaceCountCondition = false;
            for (Condition condition : ScenarioClientUtility.getUnmodifiableConditionList()) {
                if (condition.getInput() != null && condition.getInput().getType() instanceof PaceCountCondition) {
                    hasPaceCountCondition = true;
                    break;
                }
            }
        }

        boolean hasPaceCountStart = false, hasPaceCountEnd = false;
        for (LearnerAction learnerAction : learnerActions) {
            /* Check if this learner action is a Start or Stop Pace Count
             * learner action */
            hasPaceCountStart |= learnerAction.getType() == LearnerActionEnumType.START_PACE_COUNT;
            hasPaceCountEnd |= learnerAction.getType() == LearnerActionEnumType.END_PACE_COUNT;

            if (hasPaceCountStart && hasPaceCountEnd) {
                /* Found both, don't need to keep searching */
                break;
            }
        }

        if (hasPaceCountCondition && (!hasPaceCountStart || !hasPaceCountEnd)) {
            /* Has pace count condition but doesn't have both pace count learner
             * actions */
            StringBuilder sb = new StringBuilder("The Pace Count Condition is missing ");
            if (hasPaceCountStart || hasPaceCountEnd) {
                sb.append(hasPaceCountStart ? "an End Pace Count " : "a Start Pace Count ");
            } else {
                sb.append("a Start Pace Count and an End Pace Count ");
            }

            return sb.append("learner action.").toString();
        } else if (!hasPaceCountCondition && (hasPaceCountStart || hasPaceCountEnd)) {
            /* Doesn't have pace count condition but has one or both learner
             * actions */
            StringBuilder sb = new StringBuilder("The Scenario is missing ");
            if (hasPaceCountStart ^ hasPaceCountEnd) {
                sb.append(hasPaceCountStart ? "an End Pace Count " : "a Start Pace Count ")
                        .append(" learner action and ");
            }

            sb.append("a Pace Count Condition.");
            return sb.toString();
        }

        return null;
    }

    /**
     * Validate the dependency between the {@link AvoidLocationCondition} and
     * the learner action {@link LearnerActionEnumType#ASSESS_MY_LOCATION assess
     * my location}. If one exists, both should exist.
     *
     * @param learnerActions the learner actions to search for
     *        {@link LearnerActionEnumType#ASSESS_MY_LOCATION assess my
     *        location}
     * @param verifyAvoidLocationConditionExists true to check if the
     *        {@link AvoidLocationCondition} exists in the scenario; false to
     *        assume it exists and to solely search the learner actions.
     * @return an error message if the dependency check fails validation
     *         describing what caused it to fail; null if it passes validation.
     */
    public static String validateAvoidLocationLearnerActionDependency(List<LearnerAction> learnerActions,
            boolean verifyAvoidLocationConditionExists) {

        boolean hasAvoidLocationCondition = true;
        if (verifyAvoidLocationConditionExists) {
            hasAvoidLocationCondition = false;
            for (Condition condition : ScenarioClientUtility.getUnmodifiableConditionList()) {
                if (condition.getInput() != null && condition.getInput().getType() instanceof AvoidLocationCondition) {
                    AvoidLocationCondition avoidCondition = (AvoidLocationCondition) condition.getInput().getType();
                    if (Boolean.TRUE.equals(avoidCondition.isRequireLearnerAction())) {
                        hasAvoidLocationCondition = true;
                        break;
                    }
                }
            }
        }

        boolean hasAssessLocation = false;
        for (LearnerAction learnerAction : learnerActions) {
            /* Check if this learner action is an AssessLocation learner
             * action */
            if (learnerAction.getType() == LearnerActionEnumType.ASSESS_MY_LOCATION) {
                hasAssessLocation = true;
                break;
            }
        }

        if (hasAvoidLocationCondition && !hasAssessLocation) {
            /* Has an Avoid Location Condition that requires a learner action
             * that isn't present */
            return "The Avoid Location Condition is missing an Assess My Location learner action";
        } else if (!hasAvoidLocationCondition && hasAssessLocation) {
            /* Has an Assess My Location action without an Avoid Location
             * Condition that requires it */
            return "The Scenario is missing an Avoid Location Condition that requires the learner to determine when"
                    + " they have reached a location.";
        }

        return null;
    }

    /**
     * Validates a {@link LearnerAction}.
     *
     * @param learnerAction the {@link LearnerAction} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateLearnerAction(LearnerAction learnerAction) {
        // must have type
        if (learnerAction.getType() == null) {
            return "Learner action type is null";
        }

        // must have display name
        if (StringUtils.isBlank(learnerAction.getDisplayName())) {
            return "Learner action is missing a display name";
        }

        /* optional description, but if it exists, must have at least 1
         * character */
        String description = learnerAction.getDescription();
        if (description != null && StringUtils.isBlank(description)) {
            return "Description must have at least 1 character";
        }

        // optional params, but if it exists, must be valid
        Serializable actionParams = learnerAction.getLearnerActionParams();
        if(actionParams instanceof generated.dkf.TutorMeParams){
            TutorMeParams params = (generated.dkf.TutorMeParams)actionParams;
            String msg = validateTutorMeParams(params);
            if (msg != null) {
                return msg;
            }

        }else if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
            generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
            if (StringUtils.isBlank(strategyRef.getName())) {
                return "Malformed strategy reference name: '" + strategyRef.getName() + "'";
            } else{
                boolean isExistingStrategy = false;
                for(generated.dkf.Strategy strategy : ScenarioClientUtility.getStrategies().getStrategy()){
                    if(StringUtils.equals(strategyRef.getName(), strategy.getName())){
                        isExistingStrategy = true;
                        break;
                    }
                }
                
                if(!isExistingStrategy){
                    return "The strategy '"+strategyRef.getName()+"' is not an existing strategy.";
                }

            }
        }

        return null;
    }

    /**
     * Validates places of interest list.
     *
     * @param placesOfInterest the {@link PlacesOfInterest} to validate. Can't
     *        be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validatePlacesOfInterest(PlacesOfInterest placesOfInterest) {
        if (placesOfInterest == null) {
            throw new IllegalArgumentException("The parameter 'placesOfInterest' cannot be null.");
        }

        for (Serializable poi : placesOfInterest.getPointOrPathOrArea()) {
            String msg = validatePlaceOfInterest(poi);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }
    
    /**
     * Return whether the place of interest contains the disallowed coordinates.
     * @param poi the place of interest to check for the disallowed coordinates.
     * @param disallowedTypes the coordinate type enums to check for in the place of interest.  Can be null or empty.
     * @return false if the place of interest is null, the disallowed types is null or empty, the place of interest
     * doesn't use the disallowed coordinate types.  True otherwise.
     */
    public static boolean containsDisallowedCoordinate(Serializable poi, CoordinateType...disallowedTypes){
        
        if(poi == null){
            return false;
        }else if(poi instanceof generated.dkf.Point){
            return containsDisallowedCoordinate((generated.dkf.Point)poi, disallowedTypes);
        }else if(poi instanceof generated.dkf.Path){
            return containsDisallowedCoordinate((generated.dkf.Path)poi, disallowedTypes);
        }else if(poi instanceof generated.dkf.Area){
            return containsDisallowedCoordinate((generated.dkf.Area)poi, disallowedTypes);
        }else{
           return false;
        }
    }
    
    /**
     * Return whether the area's coordinates are of one of the disallowed types.
     * 
     * @param area contains the coordinates to check.
     * @param disallowedTypes the disallowed types of coordinates to check against.  Can be null.
     * @return false if the area is null, the disallowed types is null or empty, or the area's
     * coordinates are not of the disallowed type(s).  True otherwise.
     */
    public static boolean containsDisallowedCoordinate(Area area, CoordinateType... disallowedTypes){
        
        if(area == null || disallowedTypes == null){
            return false;
        }
        
        for(Coordinate coordinate : area.getCoordinate()){
            
            if(containsDisallowedCoordinate(coordinate, disallowedTypes)){
                return true;
            }
        }
     
        return false;
    }
    
    /**
     * Return whether the path's coordinates are of one of the disallowed types.
     * 
     * @param path contains the coordinates to check.
     * @param disallowedTypes the disallowed types of coordinates to check against.  Can be null.
     * @return false if the path is null, the disallowed types is null or empty, or the path's
     * coordinates are not of the disallowed type(s).  True otherwise.
     */
    public static boolean containsDisallowedCoordinate(Path path, CoordinateType... disallowedTypes){
        
        if(path == null || disallowedTypes == null){
            return false;
        }
        
        for(Segment segment : path.getSegment()){
            
            generated.dkf.Segment.Start start = segment.getStart();
            generated.dkf.Segment.End end = segment.getEnd();
            
            if(start != null && containsDisallowedCoordinate(start.getCoordinate(), disallowedTypes)){
                return true;
            }
            
            if(end != null && containsDisallowedCoordinate(end.getCoordinate(), disallowedTypes)){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Return whether the point's coordinate is of one of the disallowed types.
     * 
     * @param point contains the coordinate to check.
     * @param disallowedTypes the disallowed types of coordinates to check against.  Can be null.
     * @return false if the point is null, the disallowed types is null or empty, or the point's
     * coordinate is not of the disallowed type(s).  True otherwise.
     */
    public static boolean containsDisallowedCoordinate(Point point, CoordinateType... disallowedTypes){
        
        if(point == null || disallowedTypes == null){
            return false;
        }
        
        if(point.getCoordinate() == null){
            return false;
        }
        
        return containsDisallowedCoordinate(point.getCoordinate(), disallowedTypes);
    }
    
    /**
     * Return whether the coordinate is of one of the disallowed types.
     * @param coordinate the coordinate to check the type of
     * @param disallowedTypes the disallowed types of coordinates to check against.  Can be null.
     * @return false if the coordinate is null, the disallowed types is null or empty, or the 
     * coordinate is not of the disallowed type(s).  True otherwise.
     */
    public static boolean containsDisallowedCoordinate(Coordinate coordinate, CoordinateType... disallowedTypes){
        
        if(coordinate == null || disallowedTypes == null){
            return false;
        }
        
        for(CoordinateType type : disallowedTypes){
            
            if(coordinate.getType() instanceof generated.dkf.AGL &&
                    type == CoordinateType.AGL){
                return true;
            }else if(coordinate.getType() instanceof generated.dkf.GCC &&
                    type == CoordinateType.GCC){
                return true;
            }else if(coordinate.getType() instanceof generated.dkf.GDC &&
                    type == CoordinateType.GDC){
                return true;
            }
        }
        
        return false;
    }

    /**
     * Validates a place of interest which should be a {@link Point},
     * {@link Path}, or {@link Area}.
     *
     * @param placeOfInterest the place of interest to validate. Can't be null.
     * @param disallowedTypes the enumerated coordinate types the place of interest's coordinate(s) can't be. Can be null or empty.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validatePlaceOfInterest(Serializable placeOfInterest, CoordinateType...disallowedTypes) {
        if (placeOfInterest instanceof Point) {
            Point point = (Point) placeOfInterest;
            String msg = validatePoint(point, disallowedTypes);
            if (msg != null) {
                return msg;
            }
        } else if (placeOfInterest instanceof Path) {
            Path path = (Path) placeOfInterest;
            String msg = validatePath(path);
            if (msg != null) {
                return msg;
            }
        } else if (placeOfInterest instanceof Area) {
            Area area = (Area) placeOfInterest;
            String msg = validateArea(area);
            if (msg != null) {
                return msg;
            }
        } else {
            throw new IllegalArgumentException(
                    "The parameter 'placeOfInterest' is of an unknown type: " + placeOfInterest);
        }

        return null;
    }

    /**
     * Validates a team organization.
     *
     * @param organization the {@link TeamOrganization} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateTeamOrganization(TeamOrganization organization) {
        if (organization == null) {
            throw new IllegalArgumentException("The parameter 'organization' cannot be null.");
        }

        final Team rootTeam = organization.getTeam();
        if (rootTeam == null) {
            return "The root team is null";
        }

        /* Must have at least one team member */
        if (ScenarioClientUtility.getAnyTeamMemberName(rootTeam) == null) {
            return "The root team does not contain any team member";
        }

        return validateTeam(rootTeam);
    }

    /**
     * Validates a team.
     *
     * @param team the {@link Team} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateTeam(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("The parameter 'team' cannot be null.");
        }

        if (StringUtils.isBlank(team.getName())) {
            return "Team's name is null";
        }

        for (Serializable unit : team.getTeamOrTeamMember()) {

            if (unit instanceof Team) {

                String validation = validateTeam((Team) unit);

                if (validation != null) {
                    return validation;
                }

            } else if (unit instanceof TeamMember) {

                String validation = validateTeamMember((TeamMember) unit);

                if (validation != null) {
                    return validation;
                }

            } else {
                return "Team unit is using an invalid type: " + unit;
            }
        }

        return null;
    }

    /**
     * Validates a team member.
     *
     * @param member the {@link TeamMember} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateTeamMember(TeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException("The parameter 'member' cannot be null.");
        }

        if (StringUtils.isBlank(member.getName())) {
            return "Team member's name is null";
        }

        if (member.getLearnerId() == null) {
            return "Team member's learner ID is null";
        }

        return validateLearnerId(member.getLearnerId());
    }

    ////////////////////////////////////////////////////////
    /////////// COMMON OBJECT VALIDATION METHODS ///////////
    ////////////////////////////////////////////////////////

    /**
     * Validates an {@link AutoTutorSKO}.
     *
     * @param autoTutorSKO the {@link AutoTutorSKO} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateAutoTutorSKO(AutoTutorSKO autoTutorSKO) {
        if (autoTutorSKO == null) {
            throw new IllegalArgumentException("The parameter 'autoTutorSKO' cannot be null.");
        }

        Serializable script = autoTutorSKO.getScript();

        // must have script
        if (script == null) {
            return "Script is null";
        }

        if (script instanceof LocalSKO) {
            LocalSKO localSko = (LocalSKO) script;

            // must have file that is at least 5 characters
            if (StringUtils.isBlank(localSko.getFile()) || localSko.getFile().length() < 5) {
                return "SKO file must be at least 5 characters";
            }
        } else if (script instanceof ATRemoteSKO) {
            ATRemoteSKO remoteSko = (ATRemoteSKO) script;

            // must have URL that is at least 4 characters
            if (remoteSko.getURL() == null || StringUtils.isBlank(remoteSko.getURL().getAddress())
                    || remoteSko.getURL().getAddress().length() < 4) {
                return "Remote SKO URL must be at least 4 characters";
            }
        }

        return null;
    }

    /**
     * Validates a {@link Point}.
     *
     * @param point the {@link Point} to validate. Can't be null.
     * @param disallowedTypes the enumerated coordinate types the point's coordinate can't be. Can be null or empty.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validatePoint(Point point, CoordinateType...disallowedTypes) {
        /* Must have name */
        if (StringUtils.isBlank(ScenarioClientUtility.getPlaceOfInterestName(point))) {
            return "Point is missing a name";
        }

        /* Must have a valid coordinate */
        if (point.getCoordinate() == null) {
            return "Point coordinate is null";
        }

        String msg = validateCoordinate(point.getCoordinate());
        if (msg != null) {
            return msg;
        }
        
        if(containsDisallowedCoordinate(point, disallowedTypes)){
            return "Point coordinate can't be any of "+disallowedTypes;
        }

        return null;
    }

    /**
     * Validates a {@link PointRef}.
     *
     * @param pointRef the {@link PointRef} to validate. Can't be null.
     * @param disallowedTypes optional array of enumerated coordinate types that are not allowed
     * to be the coordinate type of the provided point.  Can be null or empty.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     *         Possible values:
     *         Point distance is less than 0
     *         Point is missing a value
     *         The point reference name doesn't refer to an existing point
     */
    public static String validatePointRef(PointRef pointRef, CoordinateType... disallowedTypes) {
        if (pointRef == null) {
            throw new IllegalArgumentException("The parameter 'pointRef' cannot be null.");
        }

        // distance is optional, if it exists it must have distance that is >= 0
        if (pointRef.getDistance() != null && pointRef.getDistance().doubleValue() < 0) {
            return "Point distance is less than 0";
        }

        // must have point value
        if (StringUtils.isBlank(pointRef.getValue())) {
            return "Point is missing a value";
        }

        // point ref name must reference a point
        Serializable poi = ScenarioClientUtility.getPlaceOfInterestWithName(pointRef.getValue());
        if(poi == null ||
                !(poi instanceof Point)){
            return "The point reference name doesn't refer to an existing point";
        }
        
        // check against disallowed types of coordinates
        Point point = (Point)poi;
        if(disallowedTypes != null){
            
            if(point.getCoordinate() == null){
                return "The point '"+point.getName()+"' has no coordinates defined";
            }
            
            for(CoordinateType type : disallowedTypes){
                
                if(point.getCoordinate().getType() instanceof generated.dkf.AGL &&
                        type == CoordinateType.AGL){
                    return "Point uses AGL coordinates which are not allowed";
                }else if(point.getCoordinate().getType() instanceof generated.dkf.GCC &&
                        type == CoordinateType.GCC){
                    return "Point uses GCC coordinates which are not allowed";
                }else if(point.getCoordinate().getType() instanceof generated.dkf.GDC &&
                        type == CoordinateType.GDC){
                    return "Point uses GDC coordinates which are not allowed";
                }
            }
        }

        return null;
    }

    /**
     * Validates a {@link Path}.
     *
     * @param path the {@link Path} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validatePath(Path path) {
        /* Must have name */
        if (StringUtils.isBlank(ScenarioClientUtility.getPlaceOfInterestName(path))) {
            return "Path is missing a name";
        }

        if (path.getSegment().size() == 0) {
            return "Path must contain at least two points";
        }

        for (int index = 0; index < path.getSegment().size(); index++) {
            Segment segment = path.getSegment().get(index);
            String result = validateSegment(segment);
            if (result != null) {
                result = "Segment " + index + ": " + result;
                return result;
            }
        }

        return null;
    }

    /**
     * Validates a {@link PathRef}.
     *
     * @param pathRef the {@link PathRef} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validatePathRef(PathRef pathRef) {
        if (pathRef == null) {
            return "The path reference cannot be null.";
        }

        // must have point value
        if (StringUtils.isBlank(pathRef.getValue())) {
            return "Path is missing a value";
        }

        // path ref name must reference an path
        boolean found = false;
        for (Serializable poi : ScenarioClientUtility.getUnmodifiablePlacesOfInterestList()) {

            String name = ScenarioClientUtility.getPlaceOfInterestName(poi);
            if (StringUtils.equals(name, pathRef.getValue())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return "The path reference name doesn't refer to an existing path";
        }

        return null;
    }

    /**
     * Validates an {@link Area}.
     *
     * @param area the {@link Area} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateArea(Area area) {
        /* Must have name */
        if (StringUtils.isBlank(ScenarioClientUtility.getPlaceOfInterestName(area))) {
            return "Area is missing a name";
        }

        if (area.getCoordinate().size() < 3) {
            return "Area must have three or more points";
        }

        for (int index = 0; index < area.getCoordinate().size(); index++) {
            Coordinate coordinate = area.getCoordinate().get(index);

            // must have valid coordinate
            if (coordinate == null) {
                return "Point " + index + " coordinate is null";
            }

            String msg = validateCoordinate(coordinate);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates an {@link AreaRef}.
     *
     * @param areaRef the {@link AreaRef} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateAreaRef(AreaRef areaRef) {
        if (areaRef == null) {
            throw new IllegalArgumentException("The parameter 'areaRef' cannot be null.");
        }

        // must have point value
        if (StringUtils.isBlank(areaRef.getValue())) {
            return "Area is missing a value";
        }

        // area ref name must reference an area
        boolean found = false;
        for (Serializable poi : ScenarioClientUtility.getUnmodifiablePlacesOfInterestList()) {

            String name = ScenarioClientUtility.getPlaceOfInterestName(poi);
            if (StringUtils.equals(name, areaRef.getValue())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return "The area reference name doesn't refer to an existing area";
        }

        return null;
    }

    /**
     * Validates a {@link Checkpoint}.
     *
     * @param checkpoint the {@link Checkpoint} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateCheckpoint(Checkpoint checkpoint) {
        if (checkpoint == null) {
            throw new IllegalArgumentException("The parameter 'checkpoint' cannot be null.");
        }

        // must have at time
        if (StringUtils.isBlank(checkpoint.getAtTime())) {
            return "Checkpoint is missing an At Time";
        }

        // must have point
        if (StringUtils.isBlank(checkpoint.getPoint())) {
            return "Checkpoint is missing a point";
        }

        // must have window of time
        if (checkpoint.getWindowOfTime() == null) {
            return "Checkpoint's window of time is null";
        }

        return null;
    }

    /**
     * Validates a {@link Segment}.
     *
     * @param segment the {@link Segment} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateSegment(Segment segment) {
        if (segment == null) {
            throw new IllegalArgumentException("The parameter 'segment' cannot be null.");
        }

        // must have name
        if (StringUtils.isBlank(segment.getName())) {
            return "Segment is missing a name";
        }

        // must have width that is >= .01
        if (segment.getWidth() == null || segment.getWidth().doubleValue() < 0.01) {
            return "Segment width is null or less than 0.01";
        }

        // optional buffer width percent, must be >= 0 if it exists
        if (segment.getBufferWidthPercent() != null) {
            if (segment.getBufferWidthPercent().doubleValue() < 0) {
                return "Segment buffer width percent is less than 0";
            }
        }

        // must have start
        if (segment.getStart() == null || segment.getStart().getCoordinate() == null) {
            return "Segment start is null or contains no coordinate";
        }

        // must have end
        if (segment.getEnd() == null || segment.getEnd().getCoordinate() == null) {
            return "Segment end is null or contains no coordinate";
        }

        /* Validate the start coordinate */
        String msg = validateCoordinate(segment.getStart().getCoordinate());
        if (msg != null) {
            return msg;
        }

        /* Validate the end coordinate */
        msg = validateCoordinate(segment.getEnd().getCoordinate());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link StartLocation}.
     *
     * @param startLocation the {@link StartLocation} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateStartLocation(StartLocation startLocation) {
        if (startLocation == null) {
            throw new IllegalArgumentException("The parameter 'startLocation' cannot be null.");
        }

        // must have coordinate
        if (startLocation.getCoordinate() == null) {
            return "Start location coordinate is null";
        }

        // validate coordinate
        String msg = validateCoordinate(startLocation.getCoordinate());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link Coordinate}.
     *
     * @param coordinate the {@link Coordinate} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateCoordinate(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("The parameter 'coordinate' cannot be null.");
        }

        Serializable coordinateType = coordinate.getType();

        // must have coordinate
        if (coordinateType == null) {
            return "Coordinate type is null";
        }

        if (coordinateType instanceof GCC) {
            GCC gcc = (GCC) coordinateType;
            if (gcc.getX() == null || gcc.getY() == null || gcc.getZ() == null) {
                return "GCC contains a null value";
            }
        } else if (coordinateType instanceof GDC) {
            GDC gdc = (GDC) coordinateType;
            if (gdc.getLongitude() == null || gdc.getLatitude() == null || gdc.getElevation() == null) {
                return "GDC contains a null value";
            }
        } else if (coordinateType instanceof AGL) {
            AGL agl = (AGL) coordinateType;
            if (agl.getX() == null || agl.getY() == null || agl.getElevation() == null) {
                return "AGL contains a null value";
            }
        }

        return null;
    }

    /**
     * Validates a {@link TutorMeParams}.
     *
     * @param tutorMeParams the {@link TutorMeParams} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateTutorMeParams(TutorMeParams tutorMeParams) {
        if (tutorMeParams == null) {
            throw new IllegalArgumentException("The parameter 'tutorMeParams' cannot be null.");
        }

        Serializable config = tutorMeParams.getConfiguration();
        if (config instanceof ConversationTreeFile) {
            ConversationTreeFile treeFile = (ConversationTreeFile) config;

            // tree file must have name that is at least 18 characters
            if (StringUtils.isBlank(treeFile.getName()) || treeFile.getName().length() < 18) {
                return "Tree file name must be at least 18 characters";
            }
        } else if (config instanceof AutoTutorSKO) {
            AutoTutorSKO autoTutorSKO = (AutoTutorSKO) config;

            // validate auto tutor SKO
            String msg = validateAutoTutorSKO(autoTutorSKO);
            if (msg != null) {
                return msg;
            }
        } else {
            // unknown config type
            return "Configuration is null or unknown";
        }

        return null;
    }

    /**
     * Validates an {@link Evaluator}.
     *
     * @param evaluator the {@link Evaluator} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateEvaluator(Evaluator evaluator) {
        if (evaluator == null) {
            throw new IllegalArgumentException("The parameter 'evaluator' cannot be null.");
        }

        // must have assessment, operator, and value
        if (StringUtils.isBlank(evaluator.getAssessment()) || StringUtils.isBlank(evaluator.getOperator())
                || StringUtils.isBlank(evaluator.getValue())) {
            return "Evaluator is missing an assessment, operator, or value";
        }

        return null;
    }

    /**
     * Validates a trigger type.
     *
     * @param triggerType the trigger type to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateTriggerType(Serializable triggerType) {
        if (triggerType == null) {
            throw new IllegalArgumentException("The parameter 'triggerType' cannot be null.");
        }

        // find type and validate
        String msg;
        if (triggerType instanceof EntityLocation) {
            msg = validateEntityLocation((EntityLocation) triggerType);

        } else if (triggerType instanceof LearnerLocation) {
            msg = validateLearnerLocation((LearnerLocation) triggerType);

        } else if (triggerType instanceof ConceptEnded) {
            msg = validateConceptEnded((ConceptEnded) triggerType);

        } else if (triggerType instanceof ChildConceptEnded) {
            msg = validateChildConceptEnded((ChildConceptEnded) triggerType);

        } else if (triggerType instanceof TaskEnded) {
            msg = validateTaskEnded((TaskEnded) triggerType);

        } else if (triggerType instanceof ConceptAssessment) {
            msg = validateConceptAssessment((ConceptAssessment) triggerType);

        } else if (triggerType instanceof LearnerActionReference) {
            msg = validateLearnerActionReference((LearnerActionReference) triggerType);

        } else if (triggerType instanceof ScenarioStarted) {
            // nothing to validate at this level
            msg = null;
            
        } else if(triggerType instanceof StrategyApplied){            
            msg = validateStrategyApplied((StrategyApplied)triggerType);

        } else {
            // null or unknown type
            msg = "Trigger type '" + triggerType + "' is unknown";
        }

        return msg;
    }
    
    /**
     * Validates an {@link StrategyApplied}
     * 
     * @param strategyApplied the {@link StrategyApplied} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateStrategyApplied(StrategyApplied strategyApplied){
        if(strategyApplied == null){
            throw new IllegalArgumentException("The parameter 'strategyApplied' cannot be null.");
        }
        
        if(ScenarioClientUtility.getStrategyWithName(strategyApplied.getStrategyName()) == null){
            return "The strategy '"+strategyApplied.getStrategyName()+"' is not a strategy that exists.";
        }
        
        return null;
    }

    /**
     * Validates an {@link EntityLocation}.
     *
     * @param entityLocation the {@link EntityLocation} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateEntityLocation(EntityLocation entityLocation) {
        if (entityLocation == null) {
            throw new IllegalArgumentException("The parameter 'entityLocation' cannot be null.");
        }

        EntityId entityId = entityLocation.getEntityId();
        Serializable entityIdType = entityId.getTeamMemberRefOrLearnerId();
        if (entityIdType instanceof LearnerId) {
            LearnerId learnerId = (LearnerId) entityIdType;
            if (learnerId.getType() instanceof StartLocation) {
                StartLocation startLocation = (StartLocation) learnerId.getType();

                // must have start location and trigger location
                if (startLocation == null) {
                    return "Start location is null";
                }

                // must have valid start location
                String msg = validateStartLocation(startLocation);
                if (msg != null) {
                    return msg;
                }

            } else if (learnerId.getType() instanceof String) {
                String entityMarking = (String) learnerId.getType();

                if (StringUtils.isBlank(entityMarking)) {
                    return "Entity Marking is not provided";
                }
            } else {
                throw new IllegalArgumentException("Found unhandled learner id type of " + learnerId.getType());
            }
        } else if (entityIdType instanceof TeamMemberRef) {
            final TeamMemberRef teamMemberRef = (TeamMemberRef) entityIdType;
            String entityMarking = teamMemberRef.getValue();
            if (StringUtils.isBlank(entityMarking)) {
                return "Entity Marking is not provided";
            }

            boolean hasMember = TeamsUtil.hasTeamOrTeamMemberWithName(entityMarking,
                    ScenarioClientUtility.getTeamOrganizationTeam());
            if (!hasMember) {
                return "The member {" + entityMarking + "} does not exist in the team organization.";
            }
        } else {
            return "Unhandled entity Id type";
        }

        TriggerLocation triggerLocation = entityLocation.getTriggerLocation();

        // must have trigger location
        if (triggerLocation == null) {
            return "Trigger location is null";
        }

        // must have valid trigger location coordinate OR valid place of interest reference
        if(triggerLocation.getCoordinate() != null) {
        String msg = validateCoordinate(triggerLocation.getCoordinate());
        if (msg != null) {
            return msg;
        }
        }else if(triggerLocation.getPointRef() != null) {
            String msg = validatePointRef(triggerLocation.getPointRef());
            if(msg != null) {
                return msg;
            }
        }else {
            return "Missing trigger location value";
        }

        return null;
    }

    /**
     * Validates a {@link LearnerLocation}.
     *
     * @param learnerLocation the {@link LearnerLocation} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateLearnerLocation(LearnerLocation learnerLocation) {
        if (learnerLocation == null) {
            throw new IllegalArgumentException("The parameter 'learnerLocation' cannot be null.");
        }

        /* this isn't in the schema, but the learner location requires a learner
         * start location */
        if (ScenarioClientUtility.isLearnerStartLocationNeeded()) {
            return "Learner start location is null";
        }

        // must have learner location coordinate
        if (learnerLocation.getCoordinate() == null) {
            return "Missing learner location coordinate";
        }

        String msg = validateCoordinate(learnerLocation.getCoordinate());
        if (msg != null) {
            return msg;
        }

        return null;
    }

    /**
     * Validates a {@link ConceptEnded}.
     *
     * @param conceptEnded the {@link ConceptEnded} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateConceptEnded(ConceptEnded conceptEnded) {
        if (conceptEnded == null) {
            throw new IllegalArgumentException("The parameter 'conceptEnded' cannot be null.");
        }

        // must have node id that is >= 0
        BigInteger conceptId = conceptEnded.getNodeId();
        if (conceptId == null || conceptId.intValue() < 0) {
            return "Concept node id is null or less than 0";
        }

        // all conditions must complete in order for this concept to end
        Concept concept = ScenarioClientUtility.getConceptWithId(conceptId);
        if (concept != null) {
            boolean canComplete = ScenarioClientUtility
                    .hasOnlyConditionsThatCanComplete(concept.getConditionsOrConcepts());
            if (!canComplete) {
                return "A task trigger will never activate because a concept contains a condition that will never end.";
            }
        }

        return null;
    }

    /**
     * Validates a {@link ChildConceptEnded}.
     *
     * @param childConceptEnded the {@link ChildConceptEnded} to validate. Can't
     *        be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateChildConceptEnded(ChildConceptEnded childConceptEnded) {
        if (childConceptEnded == null) {
            throw new IllegalArgumentException("The parameter 'conceptEnded' cannot be null.");
        }

        // must have node id that is >= 0
        BigInteger conceptId = childConceptEnded.getNodeId();
        if (conceptId == null || conceptId.intValue() < 0) {
            return "Child concept node id is null or less than 0";
        }

        // all conditions must complete in order for this concept to end
        Concept concept = ScenarioClientUtility.getConceptWithId(conceptId);
        if (concept != null) {
            boolean canComplete = ScenarioClientUtility
                    .hasOnlyConditionsThatCanComplete(concept.getConditionsOrConcepts());
            if (!canComplete) {
                return "A task trigger will never activate because a concept contains a condition that will never end.";
            }
        }

        return null;
    }

    /**
     * Validates a {@link TaskEnded}.
     *
     * @param taskEnded the {@link TaskEnded} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateTaskEnded(TaskEnded taskEnded) {
        if (taskEnded == null) {
            throw new IllegalArgumentException("The parameter 'taskEnded' cannot be null.");
        }

        // must have node id that is >= 0
        if (taskEnded.getNodeId() == null || taskEnded.getNodeId().intValue() < 0) {
            return "Task node id is null or less than 0";
        }

        return null;
    }

    /**
     * Validates a {@link ConceptAssessment}.
     *
     * @param conceptAssessment the {@link ConceptAssessment} to validate. Can't
     *        be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateConceptAssessment(ConceptAssessment conceptAssessment) {
        if (conceptAssessment == null) {
            throw new IllegalArgumentException("The parameter 'conceptAssessment' cannot be null.");
        }
        // must have concept with value >= 1
        if (conceptAssessment.getConcept() == null || conceptAssessment.getConcept().intValue() < 1) {
            return "Assessment concept is null or less than 1";
        }

        // must have concept assessment result
        if (StringUtils.isBlank(conceptAssessment.getResult())) {
            return "Assessment is missing result";
        }

        return null;
    }

    /**
     * Validates a {@link LearnerActionReference}.
     *
     * @param learnerActionReference the {@link LearnerActionReference} to
     *        validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateLearnerActionReference(LearnerActionReference learnerActionReference) {
        if (learnerActionReference == null) {
            throw new IllegalArgumentException("The parameter 'learnerActionReference' cannot be null.");
        }

        // must have learner action ref name
        if (StringUtils.isBlank(learnerActionReference.getName())) {
            return "Learner action reference is missing a name";
        }

        // learner action ref name must reference a learner action
        boolean foundLearnerAction = false;
        for (LearnerAction actions : ScenarioClientUtility.getUnmodifiableLearnerActionList()) {
            if (StringUtils.equals(actions.getDisplayName(), learnerActionReference.getName())) {
                foundLearnerAction = true;
                break;
            }
        }

        if (!foundLearnerAction) {
            return "The learner action reference name doesn't refer to an existing learner action";
        }

        return null;
    }

    /**
     * Validates a {@link Delivery}.
     *
     * @param delivery the {@link Delivery} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateDelivery(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("The parameter 'delivery' cannot be null.");
        }

        // optional in training application
        if (delivery.getInTrainingApplication() != null) {
            if (delivery.getInTrainingApplication().getEnabled() == null) {
                return "In training application delivery is missing an enabled flag";
            }
        }

        // optional in tutor
        InTutor inTutor = delivery.getInTutor();
        if (inTutor != null) {
            if (StringUtils.isBlank(inTutor.getMessagePresentation())
                    || StringUtils.isBlank(inTutor.getTextEnhancement())) {
                return "In tutor is missing message presentation or text enhancement";
            }
        }

        return null;
    }

    /**
     * Validates {@link RealTimeAssessmentRules}.
     *
     * @param rtaRules the {@link RealTimeAssessmentRules} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateRealTimeAssessmentRules(RealTimeAssessmentRules rtaRules) {
        if (rtaRules == null) {
            throw new IllegalArgumentException("The parameter 'rtaRules' cannot be null.");
        }

        final Count count = rtaRules.getCount();
        final ViolationTime violationTime = rtaRules.getViolationTime();

        // must have either count or violation time
        if (count == null && violationTime == null) {
            return "The real time assessment rules is empty";
        }

        // optional count
        if (count != null) {
            String msg = validateScoringRule(count);
            if (msg != null) {
                return msg;
            }
        }

        // optional violation time
        if (violationTime != null) {
            String msg = validateScoringRule(violationTime);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates {@link Count}.
     *
     * @param count the {@link Count} to validate. Can't be null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateScoringRule(Count count) {
        if (count == null) {
            throw new IllegalArgumentException("The parameter 'count' cannot be null.");
        }

        // count needs a name
        if (StringUtils.isBlank(count.getName())) {
            return "Scoring rule 'count' is missing a name";
        }

        // count needs a unit
        if (count.getUnits() == null) {
            return "Scoring rule 'count' is missing a unit";
        }

        // must have at least 1 evaluator
        Evaluators evaluators = count.getEvaluators();
        if (evaluators == null || evaluators.getEvaluator().isEmpty()) {
            return "Scoring rule 'count' must have at least 1 evaluator";
        }

        // each evaluator must be valid
        for (Evaluator evaluator : evaluators.getEvaluator()) {
            String msg = validateEvaluator(evaluator);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates {@link CompletionTime}.
     *
     * @param completionTime the {@link CompletionTime} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateScoringRule(CompletionTime completionTime) {
        if (completionTime == null) {
            throw new IllegalArgumentException("The parameter 'completionTime' cannot be null.");
        }

        // completionTime needs a name
        if (StringUtils.isBlank(completionTime.getName())) {
            return "Scoring rule 'completionTime' is missing a name";
        }

        // completionTime needs a unit
        if (completionTime.getUnits() == null) {
            return "Scoring rule 'completionTime' is missing a unit";
        }

        // must have at least 1 evaluator
        Evaluators evaluators = completionTime.getEvaluators();
        if (evaluators == null || evaluators.getEvaluator().isEmpty()) {
            return "Scoring rule 'completionTime' must have at least 1 evaluator";
        }

        // each evaluator must be valid
        for (Evaluator evaluator : evaluators.getEvaluator()) {
            String msg = validateEvaluator(evaluator);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates {@link ViolationTime}.
     *
     * @param violationTime the {@link ViolationTime} to validate. Can't be
     *        null.
     * @return an error message if the object fails validation describing what
     *         caused it to fail; null if it passes validation.
     */
    public static String validateScoringRule(ViolationTime violationTime) {
        if (violationTime == null) {
            throw new IllegalArgumentException("The parameter 'violationTime' cannot be null.");
        }

        // completionTime needs a name
        if (StringUtils.isBlank(violationTime.getName())) {
            return "Scoring rule 'violationTime' is missing a name";
        }

        // completionTime needs a unit
        if (violationTime.getUnits() == null) {
            return "Scoring rule 'violationTime' is missing a unit";
        }

        // must have at least 1 evaluator
        Evaluators evaluators = violationTime.getEvaluators();
        if (evaluators == null || evaluators.getEvaluator().isEmpty()) {
            return "Scoring rule 'violationTime' must have at least 1 evaluator";
        }

        // each evaluator must be valid
        for (Evaluator evaluator : evaluators.getEvaluator()) {
            String msg = validateEvaluator(evaluator);
            if (msg != null) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Validates that all team members exist within the team organization.
     *
     * @param teamMemberRefs the team members to find in the team organization.
     * @return an error message if the one or more team member does not exist;
     *         null if all team members exist.
     */
    public static String validateTeamMembersExist(TeamMemberRefs teamMemberRefs) {
        /* Nothing to check, no error */
        if (teamMemberRefs == null || CollectionUtils.isEmpty(teamMemberRefs.getTeamMemberRef())) {
            return null;
        }

        Set<String> invalidMembers = TeamsUtil.findInvalidMembers(teamMemberRefs.getTeamMemberRef(),
                ScenarioClientUtility.getTeamOrganizationTeam());
        if (CollectionUtils.isNotEmpty(invalidMembers)) {
            return "The members {" + join(", ", invalidMembers) + "} do not exist in the team organization.";
        }

        return null;
    }

    /**
     * Validates that all team members exist within the team organization.
     *
     * @param teamMemberRefs the team members to find in the team organization.
     * @return an error message if the one or more team member does not exist;
     *         null if all team members exist.
     */
    public static String validateTeamMembersExist(Collection<TeamRef> teamMemberRefs) {
        /* Nothing to check, no error */
        if (CollectionUtils.isEmpty(teamMemberRefs)) {
            return null;
        }

        Set<String> memberNames = new HashSet<>();
        for (TeamRef ref : teamMemberRefs) {
            memberNames.add(ref.getValue());
        }

        Set<String> invalidMembers = TeamsUtil.findInvalidMembers(memberNames,
                ScenarioClientUtility.getTeamOrganizationTeam());
        if (CollectionUtils.isNotEmpty(invalidMembers)) {
            return "The members {" + join(", ", invalidMembers) + "} do not exist in the team organization.";
        }

        return null;
    }
    
    /**
     * Validates that the team member exist within the team organization.
     * 
     * @param teamMemberRef the team member to find in the team organization.
     * @return an error message if the team member does not exist;  null if the team member
     * ref is null or the team member exist.
     */
    public static String validateTeamMemberExist(TeamMemberRef teamMemberRef){
        
        if(teamMemberRef == null){
            return null;
        }
        
        Set<String> memberNames = new HashSet<>();
        memberNames.add(teamMemberRef.getValue());
        
        Set<String> invalidMembers = TeamsUtil.findInvalidMembers(memberNames,
                ScenarioClientUtility.getTeamOrganizationTeam());
        if (CollectionUtils.isNotEmpty(invalidMembers)) {
            return "The member {" + join(", ", invalidMembers) + "} does not exist in the team organization.";
        }

        return null;
    }
}