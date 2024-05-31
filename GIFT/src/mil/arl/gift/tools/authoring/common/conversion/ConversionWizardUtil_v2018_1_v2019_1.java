/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.StringUtils;

/**
 * Responsible for converting 2018-1 GIFT version XML files to 2019-1 versions when applicable, i.e.
 * if a particular schema changes enough to warrant a conversion process.
 *
 * @author sharrison
 *
 */
public class ConversionWizardUtil_v2018_1_v2019_1 extends AbstractConversionWizardUtil {

    //////////////////////////////////////////////////////////////
    /////////// DON'T REMOVE THE ITEMS IN THIS SECTION ///////////
    //////////////////////////////////////////////////////////////

    /** The new version number */
    private static final String VERSION_NUMBER = "9.0.1";

    @Override
    public String getConvertedVersionNumber() {
        return VERSION_NUMBER;
    }

    /********* PREVIOUS SCHEMA FILES *********/

    /** Path to the specific version folder */
    private static final String versionPathPrefix = StringUtils.join(File.separator,
            Arrays.asList("data", "conversionWizard", "v2018_1"));

    /** a name to give paths because they didn't have names before */
    private static final String GENERATED_PATH_NAME = "generated name";

    /** Previous course schema file */
    public static final File PREV_COURSE_SCHEMA_FILE = new File(
            StringUtils.join(File.separator, Arrays.asList(versionPathPrefix, "domain", "course", "course.xsd")));

    /** Previous DKF schema file */
    public static final File PREV_DKF_SCHEMA_FILE = new File(
            StringUtils.join(File.separator, Arrays.asList(versionPathPrefix, "domain", "dkf", "dkf.xsd")));

    /** Previous metadata schema file */
    public static final File PREV_METADATA_SCHEMA_FILE = new File(
            StringUtils.join(File.separator, Arrays.asList(versionPathPrefix, "domain", "metadata", "metadata.xsd")));

    /** Previous learner schema file */
    public static final File PREV_LEARNER_SCHEMA_FILE = new File(
            StringUtils.join(File.separator, Arrays.asList(versionPathPrefix, "learner", "learnerConfig.xsd")));

    /** Previous pedagogical schema file */
    public static final File PREV_PEDAGOGICAL_SCHEMA_FILE = new File(
            StringUtils.join(File.separator, Arrays.asList(versionPathPrefix, "ped", "eMAP.xsd")));

    /** Previous sensor schema file */
    public static final File PREV_SENSOR_SCHEMA_FILE = new File(
            StringUtils.join(File.separator, Arrays.asList(versionPathPrefix, "sensor", "sensorConfig.xsd")));

    /** Previous training app schema file */
    public static final File PREV_TRAINING_APP_SCHEMA_FILE = PREV_COURSE_SCHEMA_FILE;

    /** Previous conversation schema file */
    public static final File PREV_CONVERSATION_SCHEMA_FILE = new File(StringUtils.join(File.separator,
            Arrays.asList(versionPathPrefix, "domain", "conversationTree", "conversationTree.xsd")));

    /** Previous lesson material schema file */
    public static final File PREV_LESSON_MATERIAL_SCHEMA_FILE = new File(StringUtils.join(File.separator,
            Arrays.asList(versionPathPrefix, "domain", "lessonMaterial", "lessonMaterial.xsd")));

    @Override
    public File getPreviousCourseSchemaFile() {
        return PREV_COURSE_SCHEMA_FILE;
    }

    @Override
    public File getPreviousDKFSchemaFile() {
        return PREV_DKF_SCHEMA_FILE;
    }

    @Override
    public File getPreviousMetadataSchemaFile() {
        return PREV_METADATA_SCHEMA_FILE;
    }

    @Override
    public File getPreviousLearnerSchemaFile() {
        return PREV_LEARNER_SCHEMA_FILE;
    }

    @Override
    public File getPreviousEMAPConfigSchemaFile() {
        return PREV_LEARNER_SCHEMA_FILE;
    }

    @Override
    public File getPreviousSensorSchemaFile() {
        return PREV_SENSOR_SCHEMA_FILE;
    }

    @Override
    public File getPreviousTrainingAppSchemaFile() {
        return PREV_TRAINING_APP_SCHEMA_FILE;
    }

    @Override
    public File getPreviousConversationSchemaFile() {
        return PREV_CONVERSATION_SCHEMA_FILE;
    }

    @Override
    public File getPreviousLessonMaterialRefSchemaFile() {
        return PREV_LESSON_MATERIAL_SCHEMA_FILE;
    }

    /********* PREVIOUS SCHEMA ROOTS *********/

    /** Previous course schema root */
    public static final Class<?> PREV_COURSE_ROOT = generated.v8.course.Course.class;

    /** Previous DKF schema root */
    public static final Class<?> PREV_DKF_ROOT = generated.v8.dkf.Scenario.class;

    /** Previous metadata schema root */
    public static final Class<?> PREV_METADATA_ROOT = generated.v8.metadata.Metadata.class;

    /** Previous learner schema root */
    public static final Class<?> PREV_LEARNER_ROOT = generated.v8.learner.LearnerConfiguration.class;

    /** Previous pedagogical schema root */
    public static final Class<?> PREV_PEDAGOGICAL_ROOT = generated.v8.ped.EMAP.class;

    /** Previous sensor schema root */
    public static final Class<?> PREV_SENSOR_ROOT = generated.v8.sensor.SensorsConfiguration.class;

    /** Previous training App schema root */
    public static final Class<?> PREV_TRAINING_APP_ROOT = generated.v8.course.TrainingApplicationWrapper.class;

    /** Previous conversation schema root */
    public static final Class<?> PREV_CONVERSATION_ROOT = generated.v8.conversation.Conversation.class;

    /** Previous lesson Material schema root */
    public static final Class<?> PREV_LESSON_MATERIAL_ROOT = generated.v8.course.LessonMaterialList.class;

    @Override
    public Class<?> getPreviousCourseSchemaRoot() {
        return PREV_COURSE_ROOT;
    }

    @Override
    public Class<?> getPreviousDKFSchemaRoot() {
        return PREV_DKF_ROOT;
    }

    @Override
    public Class<?> getPreviousMetadataSchemaRoot() {
        return PREV_METADATA_ROOT;
    }

    @Override
    public Class<?> getPreviousLearnerSchemaRoot() {
        return PREV_LEARNER_ROOT;
    }

    @Override
    public Class<?> getPreviousEMAPConfigSchemaRoot() {
        return PREV_PEDAGOGICAL_ROOT;
    }

    @Override
    public Class<?> getPreviousSensorSchemaRoot() {
        return PREV_SENSOR_ROOT;
    }

    @Override
    public Class<?> getPreviousTrainingAppSchemaRoot() {
        return PREV_TRAINING_APP_ROOT;
    }

    @Override
    public Class<?> getPreviousConversationSchemaRoot() {
        return PREV_CONVERSATION_ROOT;
    }

    @Override
    public Class<?> getPreviousLessonMaterialRefSchemaRoot() {
        return PREV_LESSON_MATERIAL_ROOT;
    }

    //////////////////////////////////////
    /////////// END OF SECTION ///////////
    //////////////////////////////////////

    /******************* CONVERT DKF *******************/

    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError)
            throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {

        UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(),
                failOnFirstSchemaError);
        generated.v8.dkf.Scenario prevScenario = (generated.v8.dkf.Scenario) uFile.getUnmarshalled();

        return convertScenario(prevScenario, showCompletionDialog);
    }

    @Override
    public UnmarshalledFile convertScenario(Serializable prevScenarioObj, boolean showCompletionDialog)
            throws IllegalArgumentException {
        generated.v8.dkf.Scenario prevScenario = (generated.v8.dkf.Scenario) prevScenarioObj;
        generated.v9.dkf.Scenario newScenario = new generated.v9.dkf.Scenario();

        // copy over contents from old object to new object
        newScenario.setDescription(prevScenario.getDescription());
        newScenario.setName(prevScenario.getName());
        newScenario.setVersion(VERSION_NUMBER);

        // Learner Id
        if (prevScenario.getLearnerId() != null) {
            generated.v9.dkf.LearnerId newLearnerId = new generated.v9.dkf.LearnerId();
            generated.v9.dkf.StartLocation newStartLocation = new generated.v9.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(prevScenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }

        // Resources
        generated.v9.dkf.Resources newResources = new generated.v9.dkf.Resources();
        newResources.setSurveyContext(prevScenario.getResources().getSurveyContext());

        generated.v9.dkf.AvailableLearnerActions newALA = new generated.v9.dkf.AvailableLearnerActions();

        if (prevScenario.getResources().getAvailableLearnerActions() != null) {

            generated.v8.dkf.AvailableLearnerActions ala = prevScenario.getResources().getAvailableLearnerActions();
            if (ala.getLearnerActionsFiles() != null) {
                generated.v9.dkf.LearnerActionsFiles newLAF = new generated.v9.dkf.LearnerActionsFiles();
                for (String filename : ala.getLearnerActionsFiles().getFile()) {
                    newLAF.getFile().add(filename);
                }

                newALA.setLearnerActionsFiles(newLAF);
            }

            if (ala.getLearnerActionsList() != null) {

                generated.v9.dkf.LearnerActionsList newLAL = new generated.v9.dkf.LearnerActionsList();
                for (generated.v8.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()) {

                    generated.v9.dkf.LearnerAction newAction = new generated.v9.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());

                    generated.v9.dkf.LearnerActionEnumType actionType;
                    try {
                        actionType = generated.v9.dkf.LearnerActionEnumType.fromValue(action.getType().value());
                    } catch (@SuppressWarnings("unused") Exception e) {
                        throw new UnsupportedOperationException("The learner action type '" + action.getType()
                                + "' is unknown. Since this is a required field, the import can not continue.");
                    }
                    newAction.setType(actionType);
                    newAction.setDescription(action.getDescription());

                    if (action.getLearnerActionParams() != null) {

                        generated.v8.dkf.TutorMeParams tutorMeParams = action.getLearnerActionParams();
                        generated.v9.dkf.TutorMeParams newTutorMeParams = new generated.v9.dkf.TutorMeParams();
                        if (tutorMeParams.getConfiguration() instanceof generated.v8.dkf.ConversationTreeFile) {

                            generated.v8.dkf.ConversationTreeFile convTreeFile = (generated.v8.dkf.ConversationTreeFile) tutorMeParams
                                    .getConfiguration();
                            generated.v9.dkf.ConversationTreeFile newConvTreeFile = new generated.v9.dkf.ConversationTreeFile();
                            newConvTreeFile.setName(convTreeFile.getName());

                            newTutorMeParams.setConfiguration(newConvTreeFile);

                        } else if (tutorMeParams.getConfiguration() instanceof generated.v8.dkf.AutoTutorSKO) {

                            generated.v8.dkf.AutoTutorSKO atSKO = (generated.v8.dkf.AutoTutorSKO) tutorMeParams
                                    .getConfiguration();
                            generated.v9.dkf.AutoTutorSKO newATSKO = convertAutoTutorSKO(atSKO);

                            newTutorMeParams.setConfiguration(newATSKO);

                        } else {
                            // unhandled tutor me params
                            throw new IllegalArgumentException("Found unhandled tutor me params type of "
                                    + tutorMeParams + " in learner action '" + action.getDisplayName() + "'.");
                        }

                        newAction.setLearnerActionParams(newTutorMeParams);
                    }

                    newLAL.getLearnerAction().add(newAction);
                }

                newALA.setLearnerActionsList(newLAL);
            }

            newResources.setAvailableLearnerActions(newALA);
        }
        
        if (prevScenario.getResources().getScenarioControls() != null) {
            generated.v8.dkf.ScenarioControls scenarioControls = prevScenario.getResources().getScenarioControls();
            generated.v9.dkf.ScenarioControls newScenarioControls = new generated.v9.dkf.ScenarioControls();

            if (scenarioControls.getPreventManualStop() != null) {
                newScenarioControls.setPreventManualStop(new generated.v9.dkf.PreventManualStop());
            }

            newResources.setScenarioControls(newScenarioControls);
        }

        newScenario.setResources(newResources);

        // End Triggers
        generated.v9.dkf.Scenario.EndTriggers newScenarioEndTriggers = new generated.v9.dkf.Scenario.EndTriggers();

        if (prevScenario.getEndTriggers() != null) {
            newScenarioEndTriggers.getTrigger()
                    .addAll(convertScenarioEndTriggers(prevScenario.getEndTriggers().getTrigger()));
            newScenario.setEndTriggers(newScenarioEndTriggers);
        }

        // Assessment
        generated.v9.dkf.Assessment newAssessment = new generated.v9.dkf.Assessment();
        if (prevScenario.getAssessment() != null) {

            generated.v8.dkf.Assessment assessment = prevScenario.getAssessment();

            // Objects
            generated.v9.dkf.Objects newObjects = new generated.v9.dkf.Objects();
            if (assessment.getObjects() != null) {

                if (assessment.getObjects().getWaypoints() != null) {

                    generated.v9.dkf.PlacesOfInterest newPlacesOfInterest = new generated.v9.dkf.PlacesOfInterest();

                    generated.v8.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for (generated.v8.dkf.Waypoint waypoint : waypoints.getWaypoint()) {

                        // Waypoints are now points
                        generated.v9.dkf.Point newPoint = new generated.v9.dkf.Point();
                        newPoint.setName(waypoint.getName());
                        newPoint.setCoordinate(convertCoordinate(waypoint.getCoordinate()));

                        newPlacesOfInterest.getPointOrPathOrArea().add(newPoint);
                    }

                    newObjects.setPlacesOfInterest(newPlacesOfInterest);
                }
            }
            newAssessment.setObjects(newObjects);

            // Tasks
            generated.v9.dkf.Tasks newTasks = new generated.v9.dkf.Tasks();
            if (assessment.getTasks() != null) {

                for (generated.v8.dkf.Task task : assessment.getTasks().getTask()) {

                    generated.v9.dkf.Task newTask = new generated.v9.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());

                    // Start Triggers
                    if (task.getStartTriggers() != null) {
                        generated.v9.dkf.StartTriggers newStartTriggers = new generated.v9.dkf.StartTriggers();
                        newStartTriggers.getTrigger()
                                .addAll(convertStartTriggers(task.getStartTriggers().getTrigger()));
                        newTask.setStartTriggers(newStartTriggers);
                    }

                    // End Triggers
                    if (task.getEndTriggers() != null) {
                        generated.v9.dkf.EndTriggers newEndTriggers = new generated.v9.dkf.EndTriggers();
                        newEndTriggers.getTrigger().addAll(convertEndTriggers(task.getEndTriggers().getTrigger()));
                        newTask.setEndTriggers(newEndTriggers);
                    }

                    // Concepts
                    if (task.getConcepts() != null) {
                        newTask.setConcepts(convertConcepts(task.getConcepts(), newObjects));
                    }

                    // Assessments
                    if (task.getAssessments() != null) {
                        newTask.setAssessments(convertAssessments(task.getAssessments()));
                    }

                    newTasks.getTask().add(newTask);
                }

            }

            newAssessment.setTasks(newTasks);

        }

        newScenario.setAssessment(newAssessment);

        // Actions
        if (prevScenario.getActions() != null) {

            generated.v8.dkf.Actions oldActions = prevScenario.getActions();
            generated.v9.dkf.Actions newActions = new generated.v9.dkf.Actions();

            boolean hasInstructionalStrategies = oldActions.getInstructionalStrategies() != null;

            // maintain order
            LinkedHashMap<List<Serializable>, List<generated.v9.dkf.Strategy>> oldStrategyTypesToNewStrategies = new LinkedHashMap<>();

            // get full list of strategy names (used to find unreferenced strategies later)
            Set<String> unusedStrategies = new HashSet<String>();
            List<generated.v8.dkf.Strategy> oldStrategies = null;
            if (hasInstructionalStrategies) {
                oldStrategies = oldActions.getInstructionalStrategies().getStrategy();
                for (generated.v8.dkf.Strategy strategy : oldStrategies) {
                    unusedStrategies.add(strategy.getName());
                }
            }

            // State transitions
            if (oldActions.getStateTransitions() != null) {

                generated.v8.dkf.Actions.StateTransitions oldSTransitions = oldActions.getStateTransitions();
                generated.v9.dkf.Actions.StateTransitions newSTransitions = new generated.v9.dkf.Actions.StateTransitions();

                int unnamedStateTransitionIndex = 1;
                for (generated.v8.dkf.Actions.StateTransitions.StateTransition oldSTransition : oldSTransitions
                        .getStateTransition()) {
                    if (StringUtils.isBlank(oldSTransition.getName())) {
                        oldSTransition.setName("Unnamed State Transition " + unnamedStateTransitionIndex++);
                    }

                    generated.v9.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v9.dkf.Actions.StateTransitions.StateTransition();
                    newSTransition.setName(oldSTransition.getName());

                    generated.v9.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v9.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();

                    // State type
                    for (Serializable stateType : oldSTransition.getLogicalExpression().getStateType()) {
                        if (stateType instanceof generated.v8.dkf.LearnerStateTransitionEnum) {

                            generated.v8.dkf.LearnerStateTransitionEnum stateEnum = (generated.v8.dkf.LearnerStateTransitionEnum) stateType;

                            generated.v9.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v9.dkf.LearnerStateTransitionEnum();
                            learnerStateTrans.setAttribute(stateEnum.getAttribute());
                            learnerStateTrans.setPrevious(stateEnum.getPrevious());
                            learnerStateTrans.setCurrent(stateEnum.getCurrent());

                            if (stateEnum.getCurrent() != null && (StringUtils.equals(stateEnum.getCurrent(), "Any")
                                    || StringUtils.equals(stateEnum.getCurrent(), "Anything"))) {
                                learnerStateTrans.setCurrent(null);
                            }

                            if (stateEnum.getPrevious() != null && (StringUtils.equals(stateEnum.getPrevious(), "Any")
                                    || StringUtils.equals(stateEnum.getPrevious(), "Anything"))) {
                                learnerStateTrans.setPrevious(null);
                            }

                            newLogicalExpression.getStateType().add(learnerStateTrans);

                        } else if (stateType instanceof generated.v8.dkf.PerformanceNode) {

                            generated.v8.dkf.PerformanceNode perfNode = (generated.v8.dkf.PerformanceNode) stateType;

                            generated.v9.dkf.PerformanceNode newPerfNode = new generated.v9.dkf.PerformanceNode();
                            newPerfNode.setName(perfNode.getName());
                            newPerfNode.setNodeId(perfNode.getNodeId());
                            newPerfNode.setCurrent(perfNode.getCurrent());
                            newPerfNode.setPrevious(perfNode.getPrevious());

                            if (newPerfNode.getCurrent() != null && (StringUtils.equals(newPerfNode.getCurrent(), "Any")
                                    || StringUtils.equals(newPerfNode.getCurrent(), "Anything"))) {
                                newPerfNode.setCurrent(null);
                            }

                            if (newPerfNode.getPrevious() != null
                                    && (StringUtils.equals(newPerfNode.getPrevious(), "Any")
                                            || StringUtils.equals(newPerfNode.getPrevious(), "Anything"))) {
                                newPerfNode.setPrevious(null);
                            }

                            newLogicalExpression.getStateType().add(newPerfNode);

                        } else {
                            throw new IllegalArgumentException(
                                    "Found unhandled action's state transition state type of " + stateType);
                        }
                    }

                    newSTransition.setLogicalExpression(newLogicalExpression);

                    // Strategy Choices
                    generated.v9.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v9.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    if (hasInstructionalStrategies && oldSTransition.getStrategyChoices() != null) {
                        // strategies that have been referenced by the transition
                        LinkedHashMap<String, generated.v8.dkf.Strategy> usedStrategies = new LinkedHashMap<>();

                        // keep track of which strategies will be using the delay
                        generated.v8.dkf.Strategy previousStrategy = null;
                        Map<generated.v8.dkf.Strategy, BigDecimal> applyDelay = new HashMap<>();

                        // find the largest strategy size
                        int largestActionSize = 0;

                        final List<Serializable> strategyTypes = oldSTransition.getStrategyChoices().getStrategies();
                        for (Serializable strategyType : strategyTypes) {

                            if (strategyType instanceof generated.v8.dkf.StrategyRef) {
                                generated.v8.dkf.StrategyRef strategyRef = (generated.v8.dkf.StrategyRef) strategyType;
                                generated.v8.dkf.Strategy strategy = findStrategyByName(oldStrategies,
                                        strategyRef.getName());
                                if (strategy == null) {
                                    continue;
                                }

                                usedStrategies.put(strategy.getName(), strategy);
                                int actionListSize = getStrategyActionSize(strategy);
                                largestActionSize = Math.max(largestActionSize, actionListSize);
                                BigDecimal delay = getStrategyDelay(strategy);
                                if (previousStrategy != null && delay != null) {
                                    if (applyDelay.containsKey(previousStrategy)) {
                                        delay = new BigDecimal(
                                                applyDelay.get(previousStrategy).doubleValue() + delay.doubleValue());
                                    }
                                    applyDelay.put(previousStrategy, delay);
                                }

                                // set strategy as previous if not do nothing
                                if (!(strategy.getStrategyType() instanceof generated.v8.dkf.DoNothingInstStrategy)) {
                                    previousStrategy = strategy;
                                }
                            } else if (strategyType instanceof generated.v8.dkf.DelayBeforeStrategy) {
                                if (previousStrategy != null) {
                                    generated.v8.dkf.DelayBeforeStrategy delayStrategy = (generated.v8.dkf.DelayBeforeStrategy) strategyType;
                                    BigDecimal delay = delayStrategy.getDuration();
                                    if (delay != null) {
                                        if (applyDelay.containsKey(previousStrategy)) {
                                            /* NOTE: Break this up into multiple statements */
                                            delay = new BigDecimal(applyDelay.get(previousStrategy).doubleValue()
                                                    + delay.doubleValue());
                                        }
                                        applyDelay.put(previousStrategy, delay);
                                    }
                                }
                            }
                        }

                        // the new strategies for this state transition
                        List<generated.v9.dkf.Strategy> mergedStrategies = null;

                        // check if the lists are the same as a previous run through
                        /* NOTE: None of the JAXB objects implement equals, so the equals here will
                         * not work as expected. */
                        for (Entry<List<Serializable>, List<generated.v9.dkf.Strategy>> previousStrategyEntry : oldStrategyTypesToNewStrategies.entrySet()) {
                            List<Serializable> previousStrategyTypes = previousStrategyEntry.getKey();
                            List<generated.v9.dkf.Strategy> convertedStrategies = previousStrategyEntry.getValue();

                            // if we previously created an empty strategy, reuse that strategy
                            if (largestActionSize == 0) {
                                if (convertedStrategies.size() == 1 && convertedStrategies.get(0).getStrategyActivities().isEmpty()) {
                                    mergedStrategies = convertedStrategies;
                                    break;
                                }
                                continue;
                            }
                            
                            if (previousStrategyTypes.size() != strategyTypes.size()) {
                                continue;
                            }

                            boolean isDifferent = false;
                            for (int i = 0; i < previousStrategyTypes.size(); i++) {
                                Serializable previousType = previousStrategyTypes.get(i);
                                Serializable type = strategyTypes.get(i);

                                if (previousType instanceof generated.v8.dkf.StrategyRef
                                        && type instanceof generated.v8.dkf.StrategyRef) {
                                    generated.v8.dkf.StrategyRef previousStrategyRef = (generated.v8.dkf.StrategyRef) previousType;
                                    generated.v8.dkf.StrategyRef strategyRef = (generated.v8.dkf.StrategyRef) type;
                                    if (!StringUtils.equalsIgnoreCase(previousStrategyRef.getName(),
                                            strategyRef.getName())) {
                                        isDifferent = true;
                                        break;
                                    }
                                } else if (previousType instanceof generated.v8.dkf.DelayBeforeStrategy
                                        && type instanceof generated.v8.dkf.DelayBeforeStrategy) {
                                    generated.v8.dkf.DelayBeforeStrategy previousDelay = (generated.v8.dkf.DelayBeforeStrategy) previousType;
                                    generated.v8.dkf.DelayBeforeStrategy delay = (generated.v8.dkf.DelayBeforeStrategy) type;
                                    if (previousDelay.getDuration().compareTo(delay.getDuration()) != 0) {
                                        isDifferent = true;
                                        break;
                                    }
                                } else {
                                    isDifferent = true;
                                    break;
                                }
                            }

                            if (isDifferent) {
                                continue;
                            }

                            mergedStrategies = convertedStrategies;
                            break;
                        }

                        // was not previously populated
                        if (mergedStrategies == null) {
                            mergedStrategies = new ArrayList<>();
                            
                            // create a new strategy with no actions if the old state transition had no activities
                            if (largestActionSize == 0) {
                                generated.v9.dkf.Strategy newStrategy = new generated.v9.dkf.Strategy();
                                StringBuilder sb = new StringBuilder(oldSTransition.getName());
                                newStrategy.setName(sb.toString());
                                mergedStrategies.add(newStrategy);
                            } else {
                                for (int i = 0; i < largestActionSize; i++) {
                                    generated.v9.dkf.Strategy newStrategy = new generated.v9.dkf.Strategy();
                                    StringBuilder sb = new StringBuilder(oldSTransition.getName());
                                    /* only add 'stage' to the name if we are generating more than 1 per
                                     * old strategy */
                                    if (largestActionSize > 1) {
                                        int val = i + 1;
                                        sb.append(" - Stage ").append(val);
                                    }
                                    newStrategy.setName(sb.toString());
                                    final List<Serializable> strategyActivities = newStrategy.getStrategyActivities();

                                    for (generated.v8.dkf.Strategy strategy : usedStrategies.values()) {
                                        final BigDecimal delay = applyDelay.get(strategy);
                                        Serializable newAction = retrieveNthElement(strategy, i, delay);
                                        if (newAction == null) {
                                            if (delay != null && !strategyActivities.isEmpty()) {
                                                Serializable previousActivity = strategyActivities
                                                        .get(strategyActivities.size() - 1);
                                                addToStrategyDelay(previousActivity, delay);
                                            }
                                        } else {
                                            strategyActivities.add(newAction);
                                        }
                                    }

                                    mergedStrategies.add(newStrategy);
                                }
                            }
                            
                            oldStrategyTypesToNewStrategies.put(strategyTypes, mergedStrategies);
                        }

                        for (generated.v9.dkf.Strategy newStrategy : mergedStrategies) {
                            generated.v9.dkf.StrategyRef newRef = new generated.v9.dkf.StrategyRef();
                            newRef.setName(newStrategy.getName());
                            newStrategyChoices.getStrategies().add(newRef);
                        }
                    }

                    newSTransition.setStrategyChoices(newStrategyChoices);
                    newSTransitions.getStateTransition().add(newSTransition);
                }

                newActions.setStateTransitions(newSTransitions);
            }

            // instructional strategies
            if (hasInstructionalStrategies) {

                generated.v9.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v9.dkf.Actions.InstructionalStrategies();

                for (Entry<List<Serializable>, List<generated.v9.dkf.Strategy>> oldTypesToNewStrategies : oldStrategyTypesToNewStrategies
                        .entrySet()) {
                    for (Serializable oldType : oldTypesToNewStrategies.getKey()) {
                        if (oldType instanceof generated.v8.dkf.StrategyRef) {
                            generated.v8.dkf.StrategyRef oldStrategyRef = (generated.v8.dkf.StrategyRef) oldType;
                            unusedStrategies.remove(oldStrategyRef.getName());
                        }
                    }
                    newIStrategies.getStrategy().addAll(oldTypesToNewStrategies.getValue());
                }

                int unnamedStrategyIndex = 1;
                for (String unusedRef : unusedStrategies) {
                    generated.v8.dkf.Strategy oldStrategy = findStrategyByName(oldStrategies, unusedRef);

                    if (StringUtils.isBlank(oldStrategy.getName())) {
                        oldStrategy.setName("Unnamed Strategy " + unnamedStrategyIndex++);
                    }
                    newIStrategies.getStrategy().addAll(convertStrategy(oldStrategy));
                }

                newActions.setInstructionalStrategies(newIStrategies);
            }

            newScenario.setActions(newActions);
        }

        return super.convertScenario(newScenario, showCompletionDialog);
    }

    /**
     * Retrieves the element at the provided index within the strategy list of intervention types.
     * 
     * @param strategy the strategy containing the types to traverse.
     * @param index the location of the intervention type to retrieve. If the index is greater than
     *        the number of types, then the last type will be returned. Can't be negative.
     * @param delay optional delay to be executed after the intervention type is performed.
     * @return the intervention type at the location of the index (or last type if the index is
     *         larger than the list). Can be null if the strategy is no longer supported.
     */
    private Serializable retrieveNthElement(generated.v8.dkf.Strategy strategy, int index, BigDecimal delay) {
        if (index < 0) {
            throw new IllegalArgumentException("The parameter 'index' cannot be < 0.");
        }

        Serializable strategyType = strategy.getStrategyType();

        Serializable selectedType;
        if (strategyType instanceof generated.v8.dkf.InstructionalIntervention) {
            generated.v8.dkf.InstructionalIntervention instrIntervention = (generated.v8.dkf.InstructionalIntervention) strategyType;

            generated.v9.dkf.InstructionalIntervention newInstrIntervention = new generated.v9.dkf.InstructionalIntervention();
            if (delay != null) {
                generated.v9.dkf.DelayAfterStrategy delayAfterStrategy = new generated.v9.dkf.DelayAfterStrategy();
                delayAfterStrategy.setDuration(delay);
                newInstrIntervention.setDelayAfterStrategy(delayAfterStrategy);
            }

            newInstrIntervention.setStrategyHandler(convertStrategyHandler(instrIntervention.getStrategyHandler()));

            final List<Serializable> interventionTypes = instrIntervention.getInterventionTypes();
            if (interventionTypes.isEmpty()) {
                return null;
            } else if (index >= interventionTypes.size()) {
                // get the last element in the list
                selectedType = interventionTypes.get(interventionTypes.size() - 1);
            } else {
                selectedType = interventionTypes.get(index);
            }

            if (selectedType instanceof generated.v8.dkf.Feedback) {
                generated.v8.dkf.Feedback feedback = (generated.v8.dkf.Feedback) selectedType;
                generated.v9.dkf.Feedback newFeedback = convertFeedback(feedback);

                newInstrIntervention.setFeedback(newFeedback);
                return newInstrIntervention;
            }

            // unknown or Do Nothing
            return null;
        } else if (strategyType instanceof generated.v8.dkf.MidLessonMedia) {
            generated.v8.dkf.MidLessonMedia midLessonMedia = (generated.v8.dkf.MidLessonMedia) strategyType;
            generated.v9.dkf.MidLessonMedia newMidLessonMedia = convertMidLessonMedia(midLessonMedia);
            if (delay != null) {
                generated.v9.dkf.DelayAfterStrategy delayAfterStrategy = new generated.v9.dkf.DelayAfterStrategy();
                delayAfterStrategy.setDuration(delay);
                newMidLessonMedia.setDelayAfterStrategy(delayAfterStrategy);
            }

            return newMidLessonMedia;
        } else if (strategyType instanceof generated.v8.dkf.PerformanceAssessment) {
            generated.v8.dkf.PerformanceAssessment perfAssessment = (generated.v8.dkf.PerformanceAssessment) strategyType;
            generated.v9.dkf.PerformanceAssessment newPerfAssessment = convertPerformanceAssessment(perfAssessment);
            if (delay != null) {
                generated.v9.dkf.DelayAfterStrategy delayAfterStrategy = new generated.v9.dkf.DelayAfterStrategy();
                delayAfterStrategy.setDuration(delay);
                newPerfAssessment.setDelayAfterStrategy(delayAfterStrategy);
            }

            return newPerfAssessment;
        } else if (strategyType instanceof generated.v8.dkf.ScenarioAdaptation) {
            generated.v8.dkf.ScenarioAdaptation adaptation = (generated.v8.dkf.ScenarioAdaptation) strategyType;
            generated.v9.dkf.ScenarioAdaptation newAdaptation = new generated.v9.dkf.ScenarioAdaptation();
            if (delay != null) {
                generated.v9.dkf.DelayAfterStrategy delayAfterStrategy = new generated.v9.dkf.DelayAfterStrategy();
                delayAfterStrategy.setDuration(delay);
                newAdaptation.setDelayAfterStrategy(delayAfterStrategy);
            }

            newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));

            final List<Serializable> adaptationTypes = adaptation.getAdaptationTypes();
            if (adaptationTypes.isEmpty()) {
                return null;
            } else if (index >= adaptationTypes.size()) {
                // get the last element in the list
                selectedType = adaptationTypes.get(adaptationTypes.size() - 1);
            } else {
                selectedType = adaptationTypes.get(index);
            }

            if (selectedType instanceof generated.v8.dkf.EnvironmentAdaptation) {
                generated.v8.dkf.EnvironmentAdaptation envAdapt = (generated.v8.dkf.EnvironmentAdaptation) selectedType;
                generated.v9.dkf.EnvironmentAdaptation newEnvAdapt = convertEnvironmentAdaptation(envAdapt);
                if(newEnvAdapt != null){
                    newAdaptation.setEnvironmentAdaptation(newEnvAdapt);
                }else{
                    newAdaptation = null;
                }
                return newAdaptation;
            }

            // unknown or Do Nothing
            return null;
        } else if (strategyType instanceof generated.v8.dkf.DoNothingInstStrategy) {
            return null;
        } else {
            throw new UnsupportedOperationException("Unknown strategy type: " + strategyType);
        }
    }

    /**
     * Converts a performance assessment
     * 
     * @param perfAssessment the performance assessment to convert
     * @return the next version of the performance assessment
     */
    private generated.v9.dkf.PerformanceAssessment convertPerformanceAssessment(
            generated.v8.dkf.PerformanceAssessment perfAssessment) {
        generated.v9.dkf.PerformanceAssessment newPerfAssessment = new generated.v9.dkf.PerformanceAssessment();

        if (perfAssessment.getAssessmentType() instanceof generated.v8.dkf.PerformanceAssessment.PerformanceNode) {
            generated.v9.dkf.PerformanceAssessment.PerformanceNode newPerfAssNode = new generated.v9.dkf.PerformanceAssessment.PerformanceNode();

            newPerfAssNode.setNodeId(
                    ((generated.v8.dkf.PerformanceAssessment.PerformanceNode) perfAssessment.getAssessmentType())
                            .getNodeId());
            newPerfAssessment.setAssessmentType(newPerfAssNode);
        } else {
            generated.v8.dkf.Conversation prevConv = (generated.v8.dkf.Conversation) perfAssessment.getAssessmentType();

            generated.v9.dkf.Conversation newConv = new generated.v9.dkf.Conversation();
            if (prevConv.getType() instanceof generated.v8.dkf.ConversationTreeFile) {

                generated.v9.dkf.ConversationTreeFile newTreeFile = new generated.v9.dkf.ConversationTreeFile();
                newTreeFile.setName(((generated.v8.dkf.ConversationTreeFile) prevConv.getType()).getName());

                newConv.setType(newTreeFile);
            } else {

                generated.v8.dkf.AutoTutorSKO prevATSKO = (generated.v8.dkf.AutoTutorSKO) prevConv.getType();

                generated.v9.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevATSKO);

                newConv.setType(newAutoTutorSKO);
            }

            newPerfAssessment.setAssessmentType(newConv);
        }

        newPerfAssessment.setStrategyHandler(convertStrategyHandler(perfAssessment.getStrategyHandler()));
        return newPerfAssessment;
    }

    /**
     * Converts an environment adaptation
     * 
     * @param envAdaptation the environment adaptation to convert
     * @return the next version of the environment adaptation.  Can return null if the adaptation is no longer supported.
     */
    private generated.v9.dkf.EnvironmentAdaptation convertEnvironmentAdaptation(
            generated.v8.dkf.EnvironmentAdaptation envAdaptation) {
        generated.v9.dkf.EnvironmentAdaptation newEnvAdapt = new generated.v9.dkf.EnvironmentAdaptation();

        final generated.v8.dkf.EnvironmentAdaptation.Pair pair = envAdaptation.getPair();
        if (pair != null) {
            
            String typeStr = pair.getType();

            if (StringUtils.equals(typeStr, "EnvironmentControlEnum")) {
                
                String value = pair.getValue();
                if(value.equals("Overcast")){
                    generated.v9.dkf.EnvironmentAdaptation.Overcast overcast = new generated.v9.dkf.EnvironmentAdaptation.Overcast();
                    overcast.setValue(new BigDecimal("0.9"));  // value came from the old GIFT/config/gateway/externalApplications/VBS/vbs.control.properties file
                    newEnvAdapt.setType(overcast);
                    
                }else if(value.equals("FogLevel1")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.Fog fog = new generated.v9.dkf.EnvironmentAdaptation.Fog();
                    fog.setDensity(new BigDecimal("0.1"));  // value came from the old GIFT/config/gateway/externalApplications/VBS/vbs.control.properties file
                    newEnvAdapt.setType(fog);
                    
                }else if(value.equals("FogLevel2")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.Fog fog = new generated.v9.dkf.EnvironmentAdaptation.Fog();
                    fog.setDensity(new BigDecimal("0.4"));  // value came from the old GIFT/config/gateway/externalApplications/VBS/vbs.control.properties file
                    newEnvAdapt.setType(fog);
                    
                }else if(value.equals("FogLevel3")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.Fog fog = new generated.v9.dkf.EnvironmentAdaptation.Fog();
                    fog.setDensity(new BigDecimal("0.6"));  // value came from the old GIFT/config/gateway/externalApplications/VBS/vbs.control.properties file
                    newEnvAdapt.setType(fog);
                    
                }else if(value.equals("FogLevel4")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.Fog fog = new generated.v9.dkf.EnvironmentAdaptation.Fog();
                    fog.setDensity(new BigDecimal("0.9"));  // value came from the old GIFT/config/gateway/externalApplications/VBS/vbs.control.properties file
                    newEnvAdapt.setType(fog);
                    
                }else if(value.equals("Rain")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.Rain rain = new generated.v9.dkf.EnvironmentAdaptation.Rain();
                    rain.setValue(new BigDecimal("0.5"));  // value came from the old GIFT/config/gateway/externalApplications/VBS/vbs.control.properties file
                    newEnvAdapt.setType(rain);
                    
                }else if(value.equals("Clear")){
                    //no longer supported
                    newEnvAdapt = null;
                    
                }else if(value.equals("TimeOfDayDusk")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay();
                    tod.setType(new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Dusk());  
                    newEnvAdapt.setType(tod);
                    
                }else if(value.equals("TimeOfDayDawn")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay();
                    tod.setType(new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Dawn());  
                    newEnvAdapt.setType(tod);
                    
                }else if(value.equals("TimeOfDayMidday")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay();
                    tod.setType(new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Midday());  
                    newEnvAdapt.setType(tod);
                    
                }else if(value.equals("TimeOfDayMidnight")){
                    
                    generated.v9.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay();
                    tod.setType(new generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Midnight());  
                    newEnvAdapt.setType(tod);
                    
                }
            }
        }

        if (newEnvAdapt == null || newEnvAdapt.getType() == null) {
            return null;
        }

        return newEnvAdapt;
    }

    /**
     * Converts a feedback
     * 
     * @param feedback the feedback to convert
     * @return the next version of the feedback
     */
    private generated.v9.dkf.Feedback convertFeedback(generated.v8.dkf.Feedback feedback) {
        generated.v9.dkf.Feedback newFeedback = new generated.v9.dkf.Feedback();

        if (feedback.getFeedbackPresentation() instanceof generated.v8.dkf.Message) {

            generated.v8.dkf.Message message = (generated.v8.dkf.Message) feedback.getFeedbackPresentation();
            generated.v9.dkf.Message feedbackMsg = convertMessage(message);

            newFeedback.setFeedbackPresentation(feedbackMsg);
        } else if (feedback.getFeedbackPresentation() instanceof generated.v8.dkf.Audio) {

            generated.v8.dkf.Audio audio = (generated.v8.dkf.Audio) feedback.getFeedbackPresentation();

            generated.v9.dkf.Audio newAudio = new generated.v9.dkf.Audio();

            // An audio object requires a .mp3 file but does not require a .ogg file
            newAudio.setMP3File(audio.getMP3File());

            if (audio.getOGGFile() != null) {
                newAudio.setOGGFile(audio.getOGGFile());
            }

            newFeedback.setFeedbackPresentation(newAudio);
        } else if (feedback.getFeedbackPresentation() instanceof generated.v8.dkf.MediaSemantics) {

            generated.v8.dkf.MediaSemantics semantics = (generated.v8.dkf.MediaSemantics) feedback
                    .getFeedbackPresentation();

            generated.v9.dkf.MediaSemantics newSemantics = new generated.v9.dkf.MediaSemantics();

            // A MediaSematic file requires an avatar and a key name property.
            newSemantics.setAvatar(semantics.getAvatar());
            newSemantics.setKeyName(semantics.getKeyName());

            if (semantics.getMessage() != null) {
                newSemantics.setMessage(convertMessage(semantics.getMessage()));
            }

            newFeedback.setFeedbackPresentation(newSemantics);
        }

        newFeedback.setAffectiveFeedbackType(feedback.getAffectiveFeedbackType());
        newFeedback.setFeedbackSpecificityType(feedback.getFeedbackSpecificityType());
        return newFeedback;
    }

    /**
     * Converts a mid lesson media
     * 
     * @param midLessonMedia the mid lesson media to convert
     * @return the next version of the mid lesson media
     */
    private generated.v9.dkf.MidLessonMedia convertMidLessonMedia(generated.v8.dkf.MidLessonMedia midLessonMedia) {
        generated.v9.dkf.MidLessonMedia newMidLessonMedia = new generated.v9.dkf.MidLessonMedia();

        newMidLessonMedia.setStrategyHandler(convertStrategyHandler(midLessonMedia.getStrategyHandler()));

        final generated.v8.dkf.LessonMaterialList lessonMaterialList = midLessonMedia.getLessonMaterialList();
        if (lessonMaterialList == null) {
            // gone as far as we can
            return newMidLessonMedia;
        }

        generated.v9.dkf.LessonMaterialList newLessonMaterialList = new generated.v9.dkf.LessonMaterialList();

        generated.v8.dkf.LessonMaterialList.Assessment assessment = lessonMaterialList.getAssessment();
        if (assessment != null) {
            generated.v9.dkf.LessonMaterialList.Assessment newAssessment = new generated.v9.dkf.LessonMaterialList.Assessment();

            final generated.v8.dkf.OverDwell overDwell = assessment.getOverDwell();
            if (overDwell != null) {
                generated.v9.dkf.OverDwell newOverDwell = new generated.v9.dkf.OverDwell();
                newOverDwell.setFeedback(overDwell.getFeedback());

                final generated.v8.dkf.OverDwell.Duration duration = overDwell.getDuration();
                if (duration != null) {
                    generated.v9.dkf.OverDwell.Duration newDuration = new generated.v9.dkf.OverDwell.Duration();

                    final Serializable durationType = duration.getType();
                    if (durationType instanceof BigInteger) {
                        BigInteger durationTime = (BigInteger) durationType;
                        newDuration.setType(durationTime);
                    } else if (durationType instanceof generated.v8.dkf.OverDwell.Duration.DurationPercent) {
                        generated.v8.dkf.OverDwell.Duration.DurationPercent durationPercent = (generated.v8.dkf.OverDwell.Duration.DurationPercent) durationType;
                        generated.v9.dkf.OverDwell.Duration.DurationPercent newDurationPercent = new generated.v9.dkf.OverDwell.Duration.DurationPercent();
                        newDurationPercent.setPercent(durationPercent.getPercent());
                        newDurationPercent.setTime(durationPercent.getTime());

                        newDuration.setType(newDurationPercent);
                    }

                    newOverDwell.setDuration(newDuration);
                }

                newAssessment.setOverDwell(newOverDwell);
            }

            final generated.v8.dkf.LessonMaterialList.Assessment.UnderDwell underDwell = assessment.getUnderDwell();
            if (underDwell != null) {
                generated.v9.dkf.LessonMaterialList.Assessment.UnderDwell newUnderDwell = new generated.v9.dkf.LessonMaterialList.Assessment.UnderDwell();
                newUnderDwell.setFeedback(underDwell.getFeedback());
                newUnderDwell.setDuration(underDwell.getDuration());

                newAssessment.setUnderDwell(newUnderDwell);
            }

            newLessonMaterialList.setAssessment(newAssessment);
        }

        newLessonMaterialList.setIsCollection(convertBooleanEnum(lessonMaterialList.getIsCollection()));

        for (generated.v8.dkf.Media media : lessonMaterialList.getMedia()) {
            generated.v9.dkf.Media newMedia = new generated.v9.dkf.Media();

            newMedia.setName(media.getName());
            newMedia.setMessage(media.getMessage());
            newMedia.setUri(media.getUri());

            Serializable mediaProperties = media.getMediaTypeProperties();
            if (mediaProperties instanceof generated.v8.dkf.PDFProperties) {
                newMedia.setMediaTypeProperties(new generated.v9.dkf.PDFProperties());

            } else if (mediaProperties instanceof generated.v8.dkf.WebpageProperties) {
                newMedia.setMediaTypeProperties(new generated.v9.dkf.WebpageProperties());

            } else if (mediaProperties instanceof generated.v8.dkf.YoutubeVideoProperties) {
                generated.v8.dkf.YoutubeVideoProperties properties = (generated.v8.dkf.YoutubeVideoProperties) mediaProperties;
                newMedia.setMediaTypeProperties(convertYoutubeVideoProperties(properties));

            } else if (mediaProperties instanceof generated.v8.dkf.ImageProperties) {
                newMedia.setMediaTypeProperties(new generated.v9.dkf.ImageProperties());

            } else if (mediaProperties instanceof generated.v8.dkf.SlideShowProperties) {
                generated.v8.dkf.SlideShowProperties properties = (generated.v8.dkf.SlideShowProperties) mediaProperties;
                generated.v9.dkf.SlideShowProperties newProperties = new generated.v9.dkf.SlideShowProperties();
                newProperties
                        .setDisplayPreviousSlideButton(convertBooleanEnum(properties.getDisplayPreviousSlideButton()));
                newProperties.setKeepContinueButton(convertBooleanEnum(properties.getKeepContinueButton()));
                newProperties.getSlideRelativePath().addAll(properties.getSlideRelativePath());
                newMedia.setMediaTypeProperties(newProperties);

            } else if (mediaProperties instanceof generated.v8.dkf.LtiProperties) {
                generated.v8.dkf.LtiProperties properties = (generated.v8.dkf.LtiProperties) mediaProperties;
                newMedia.setMediaTypeProperties(convertLtiProperties(properties));

            } else {
                throw new IllegalArgumentException("Found unhandled media type of " + mediaProperties);
            }

            newLessonMaterialList.getMedia().add(newMedia);
        }

        newMidLessonMedia.setLessonMaterialList(newLessonMaterialList);
        return newMidLessonMedia;
    }

    /**
     * Converts youtube properties
     * 
     * @param properties the youtube properties to convert
     * @return the next version of the youtube properties
     */
    private generated.v9.dkf.YoutubeVideoProperties convertYoutubeVideoProperties(
            generated.v8.dkf.YoutubeVideoProperties properties) {
        generated.v9.dkf.YoutubeVideoProperties newProperties = new generated.v9.dkf.YoutubeVideoProperties();

        newProperties.setAllowAutoPlay(convertBooleanEnum(properties.getAllowAutoPlay()));

        newProperties.setAllowFullScreen(convertBooleanEnum(properties.getAllowFullScreen()));

        final generated.v8.dkf.Size size = properties.getSize();
        if (size != null) {
            generated.v9.dkf.Size newSize = new generated.v9.dkf.Size();
            newSize.setConstrainToScreen(convertBooleanEnum(size.getConstrainToScreen()));

            newSize.setHeight(size.getHeight());
            newSize.setHeightUnits(size.getHeightUnits());
            newSize.setWidth(size.getWidth());
            newSize.setWidthUnits(size.getWidthUnits());

            newProperties.setSize(newSize);
        }

        return newProperties;
    }

    /**
     * Converts LTI properties
     * 
     * @param properties the LTI properties to convert
     * @return the next version of the LTI properties
     */
    private generated.v9.dkf.LtiProperties convertLtiProperties(generated.v8.dkf.LtiProperties properties) {
        generated.v9.dkf.LtiProperties newProperties = new generated.v9.dkf.LtiProperties();

        newProperties.setAllowScore(convertBooleanEnum(properties.getAllowScore()));
        newProperties.setIsKnowledge(convertBooleanEnum(properties.getIsKnowledge()));
        newProperties.setSliderMaxValue(properties.getSliderMaxValue());
        newProperties.setSliderMinValue(properties.getSliderMinValue());
        newProperties.setLtiIdentifier(properties.getLtiIdentifier());

        if (properties.getDisplayMode() != null) {
            newProperties.setDisplayMode(generated.v9.dkf.DisplayModeEnum.fromValue(properties.getDisplayMode().value()));
        }

        if (properties.getCustomParameters() != null) {
            generated.v9.dkf.CustomParameters newCustomParameters = new generated.v9.dkf.CustomParameters();
            for (generated.v8.dkf.Nvpair nvPair : properties.getCustomParameters().getNvpair()) {
                newCustomParameters.getNvpair().add(convertNvpair(nvPair));
            }
            newProperties.setCustomParameters(newCustomParameters);
        }

        if (properties.getLtiConcepts() != null) {
            generated.v9.dkf.LtiConcepts newLtiConcepts = new generated.v9.dkf.LtiConcepts();
            newLtiConcepts.getConcepts().addAll(properties.getLtiConcepts().getConcepts());
            newProperties.setLtiConcepts(newLtiConcepts);
        }

        return newProperties;
    }

    /**
     * Converts a strategy
     * 
     * @param oldStrategy the strategy to convert
     * @return the next version of the strategy
     */
    private List<generated.v9.dkf.Strategy> convertStrategy(generated.v8.dkf.Strategy oldStrategy) {
        List<generated.v9.dkf.Strategy> newStrategies = new ArrayList<>();

        Serializable strategyType = oldStrategy.getStrategyType();
        if (strategyType instanceof generated.v8.dkf.InstructionalIntervention) {
            generated.v8.dkf.InstructionalIntervention iIntervention = (generated.v8.dkf.InstructionalIntervention) strategyType;

            final List<Serializable> interventionTypes = iIntervention.getInterventionTypes();
            for (int i = 0; i < interventionTypes.size(); i++) {
                Serializable interventionType = interventionTypes.get(i);
                if (interventionType instanceof generated.v8.dkf.Feedback) {
                    generated.v8.dkf.Feedback feedback = (generated.v8.dkf.Feedback) interventionType;

                    generated.v9.dkf.Strategy newStrategy = new generated.v9.dkf.Strategy();
                    StringBuilder sb = new StringBuilder(oldStrategy.getName());
                    /* only add 'stage' to the name if we are generating more than 1 per old
                     * strategy */
                    if (interventionTypes.size() > 1) {
                        int val2 = i + 1;
                        sb.append(" - Stage ").append(val2);
                    }
                    newStrategy.setName(sb.toString());

                    generated.v9.dkf.InstructionalIntervention newIIntervention = new generated.v9.dkf.InstructionalIntervention();
                    newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));
                    newIIntervention.setFeedback(convertFeedback(feedback));

                    newStrategy.getStrategyActivities().add(newIIntervention);
                    newStrategies.add(newStrategy);
                }
            }
        } else if (strategyType instanceof generated.v8.dkf.ScenarioAdaptation) {
            generated.v8.dkf.ScenarioAdaptation adaptation = (generated.v8.dkf.ScenarioAdaptation) strategyType;

            final List<Serializable> adaptationTypes = adaptation.getAdaptationTypes();
            for (int i = 0; i < adaptationTypes.size(); i++) {
                Serializable adaptationType = adaptationTypes.get(i);
                if (adaptationType instanceof generated.v8.dkf.EnvironmentAdaptation) {
                    generated.v8.dkf.EnvironmentAdaptation envAdaptation = (generated.v8.dkf.EnvironmentAdaptation) adaptationType;

                    generated.v9.dkf.Strategy newStrategy = new generated.v9.dkf.Strategy();
                    StringBuilder sb = new StringBuilder(oldStrategy.getName());
                    /* only add 'stage' to the name if we are generating more than 1 per old
                     * strategy */
                    if (adaptationTypes.size() > 1) {
                        int val3 = i + 1;
                        sb.append(" - Stage ").append(val3);
                    }
                    newStrategy.setName(sb.toString());

                    generated.v9.dkf.ScenarioAdaptation newAdaptation = new generated.v9.dkf.ScenarioAdaptation();
                    newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));
                    generated.v9.dkf.EnvironmentAdaptation newEnvAdaptation = convertEnvironmentAdaptation(envAdaptation);
                    if(newEnvAdaptation != null){
                        newAdaptation.setEnvironmentAdaptation(newEnvAdaptation);

                        newStrategy.getStrategyActivities().add(newAdaptation);
                        newStrategies.add(newStrategy);
                    }
                }
            }
        } else if (strategyType instanceof generated.v8.dkf.MidLessonMedia) {
            generated.v9.dkf.Strategy newStrategy = new generated.v9.dkf.Strategy();
            newStrategy.setName(oldStrategy.getName());

            generated.v8.dkf.MidLessonMedia midLessonMedia = (generated.v8.dkf.MidLessonMedia) strategyType;
            newStrategy.getStrategyActivities().add(convertMidLessonMedia(midLessonMedia));

            newStrategies.add(newStrategy);

        } else if (strategyType instanceof generated.v8.dkf.PerformanceAssessment) {
            generated.v9.dkf.Strategy newStrategy = new generated.v9.dkf.Strategy();
            newStrategy.setName(oldStrategy.getName());

            generated.v8.dkf.PerformanceAssessment perfAssessment = (generated.v8.dkf.PerformanceAssessment) strategyType;
            newStrategy.getStrategyActivities().add(convertPerformanceAssessment(perfAssessment));

            newStrategies.add(newStrategy);

        } else if (strategyType instanceof generated.v8.dkf.DoNothingInstStrategy) {
            generated.v9.dkf.Strategy newStrategy = new generated.v9.dkf.Strategy();
            newStrategy.setName(oldStrategy.getName());

            newStrategies.add(newStrategy);
        } else {
            throw new IllegalArgumentException("Found unhandled strategy type of " + strategyType);
        }

        return newStrategies;
    }

    /**
     * Finds a strategy within the list that contains the provided name.
     * 
     * @param strategies the list of strategies to check
     * @param strategyName the name to look for
     * @return the strategy with the provided name. Can be null if no strategy is found.
     */
    private generated.v8.dkf.Strategy findStrategyByName(List<generated.v8.dkf.Strategy> strategies,
            String strategyName) {
        for (generated.v8.dkf.Strategy strategy : strategies) {
            if (StringUtils.equals(strategy.getName(), strategyName)) {
                return strategy;
            }
        }

        return null;
    }

    /**
     * Gets the size of the strategy actions from the provided strategy.
     * 
     * @param strategy the strategy containing the actions
     * @return the size of the strategy actions.
     */
    private int getStrategyActionSize(generated.v8.dkf.Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        final Serializable strategyType = strategy.getStrategyType();
        if (strategyType instanceof generated.v8.dkf.InstructionalIntervention) {
            generated.v8.dkf.InstructionalIntervention instrInter = (generated.v8.dkf.InstructionalIntervention) strategyType;
            for (Serializable type : instrInter.getInterventionTypes()) {
                if (!(type instanceof generated.v8.dkf.DoNothingTactic)) {
                    return instrInter.getInterventionTypes().size();
                }
            }
        } else if (strategyType instanceof generated.v8.dkf.ScenarioAdaptation) {
            generated.v8.dkf.ScenarioAdaptation adaptation = (generated.v8.dkf.ScenarioAdaptation) strategyType;
            for (Serializable type : adaptation.getAdaptationTypes()) {
                if (!(type instanceof generated.v8.dkf.DoNothingTactic)) {
                    return adaptation.getAdaptationTypes().size();
                }
            }
        } else if (strategyType instanceof generated.v8.dkf.MidLessonMedia) {
            generated.v8.dkf.MidLessonMedia midLessonMedia = (generated.v8.dkf.MidLessonMedia) strategyType;
            if (midLessonMedia.getLessonMaterialList() != null) {
                return Arrays.asList(midLessonMedia.getLessonMaterialList()).size();
            }
        } else if (strategyType instanceof generated.v8.dkf.PerformanceAssessment) {
            generated.v8.dkf.PerformanceAssessment perfAssessment = (generated.v8.dkf.PerformanceAssessment) strategyType;
            if (perfAssessment.getAssessmentType() != null) {
                return Arrays.asList(perfAssessment.getAssessmentType()).size();
            }
        } else if (strategyType instanceof generated.v8.dkf.DoNothingInstStrategy) {
            // return default 0
        }

        return 0;
    }

    /**
     * Gets the strategy delay from the provided strategy.
     * 
     * @param strategy the strategy containing the delay. Can't be null.
     * @return the decimal value of the delay. Can be null if no delay exists.
     */
    private BigDecimal getStrategyDelay(generated.v8.dkf.Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        BigDecimal delay = null;

        final Serializable strategyType = strategy.getStrategyType();
        if (strategyType instanceof generated.v8.dkf.InstructionalIntervention) {
            generated.v8.dkf.InstructionalIntervention instrInter = (generated.v8.dkf.InstructionalIntervention) strategyType;
            if (instrInter.getDelayBeforeStrategy() != null
                    && instrInter.getDelayBeforeStrategy().getDuration() != null) {
                delay = instrInter.getDelayBeforeStrategy().getDuration();
            }
        } else if (strategyType instanceof generated.v8.dkf.ScenarioAdaptation) {
            generated.v8.dkf.ScenarioAdaptation adaptation = (generated.v8.dkf.ScenarioAdaptation) strategyType;
            if (adaptation.getDelayBeforeStrategy() != null
                    && adaptation.getDelayBeforeStrategy().getDuration() != null) {
                delay = adaptation.getDelayBeforeStrategy().getDuration();
            }
        } else if (strategyType instanceof generated.v8.dkf.MidLessonMedia) {
            generated.v8.dkf.MidLessonMedia midLessonMedia = (generated.v8.dkf.MidLessonMedia) strategyType;
            if (midLessonMedia.getDelayBeforeStrategy() != null
                    && midLessonMedia.getDelayBeforeStrategy().getDuration() != null) {
                delay = midLessonMedia.getDelayBeforeStrategy().getDuration();
            }
        } else if (strategyType instanceof generated.v8.dkf.PerformanceAssessment) {
            generated.v8.dkf.PerformanceAssessment perfAssessment = (generated.v8.dkf.PerformanceAssessment) strategyType;
            if (perfAssessment.getDelayBeforeStrategy() != null
                    && perfAssessment.getDelayBeforeStrategy().getDuration() != null) {
                delay = perfAssessment.getDelayBeforeStrategy().getDuration();
            }
        } else if (strategyType instanceof generated.v8.dkf.DoNothingInstStrategy) {
            generated.v8.dkf.DoNothingInstStrategy doNothing = (generated.v8.dkf.DoNothingInstStrategy) strategyType;
            if (doNothing.getDelayBeforeStrategy() != null
                    && doNothing.getDelayBeforeStrategy().getDuration() != null) {
                delay = doNothing.getDelayBeforeStrategy().getDuration();
            }
        }

        return delay;
    }

    /**
     * Appends the delay to the provided strategy activity.
     * 
     * @param newStrategyType the strategy activity to add the delay.
     * @param delayToAdd the decimal value of the delay to add to the activity.
     */
    private void addToStrategyDelay(Serializable newStrategyType, BigDecimal delayToAdd) {
        if (newStrategyType == null) {
            throw new IllegalArgumentException("The parameter 'newStrategyType' cannot be null.");
        } else if (delayToAdd == null || BigDecimal.ZERO.equals(delayToAdd)) {
            return;
        }

        generated.v9.dkf.DelayAfterStrategy delayAfterStrategy;
        if (newStrategyType instanceof generated.v9.dkf.InstructionalIntervention) {
            generated.v9.dkf.InstructionalIntervention instrInter = (generated.v9.dkf.InstructionalIntervention) newStrategyType;
            delayAfterStrategy = instrInter.getDelayAfterStrategy();
        } else if (newStrategyType instanceof generated.v9.dkf.ScenarioAdaptation) {
            generated.v9.dkf.ScenarioAdaptation adaptation = (generated.v9.dkf.ScenarioAdaptation) newStrategyType;
            delayAfterStrategy = adaptation.getDelayAfterStrategy();
        } else if (newStrategyType instanceof generated.v9.dkf.MidLessonMedia) {
            generated.v9.dkf.MidLessonMedia midLessonMedia = (generated.v9.dkf.MidLessonMedia) newStrategyType;
            delayAfterStrategy = midLessonMedia.getDelayAfterStrategy();
        } else if (newStrategyType instanceof generated.v9.dkf.PerformanceAssessment) {
            generated.v9.dkf.PerformanceAssessment perfAssessment = (generated.v9.dkf.PerformanceAssessment) newStrategyType;
            delayAfterStrategy = perfAssessment.getDelayAfterStrategy();
        } else {
            throw new UnsupportedOperationException("Unknown strategy type '" + newStrategyType + "'");
        }

        if (delayAfterStrategy == null) {
            delayAfterStrategy = new generated.v9.dkf.DelayAfterStrategy();
        }

        final BigDecimal duration = delayAfterStrategy.getDuration();
        if (duration == null) {
            delayAfterStrategy.setDuration(delayToAdd);
        } else {
            delayAfterStrategy.setDuration(new BigDecimal(duration.doubleValue() + delayToAdd.doubleValue()));
        }
    }

    /**
     * Convert a previous AutoTutor SKO element to the newer schema version.
     *
     * @param prevATSKO the previous element to convert its content to the newer element. If null
     *        this returns null.
     * @return the new AutoTutor SKO element for the new schema version.
     */
    private static generated.v9.dkf.AutoTutorSKO convertAutoTutorSKO(generated.v8.dkf.AutoTutorSKO prevATSKO) {

        if (prevATSKO == null) {
            return null;
        }

        generated.v9.dkf.AutoTutorSKO newAutoTutorSKO = new generated.v9.dkf.AutoTutorSKO();
        if (prevATSKO.getScript() instanceof generated.v8.dkf.LocalSKO) {

            generated.v8.dkf.LocalSKO localSKO = (generated.v8.dkf.LocalSKO) prevATSKO.getScript();
            generated.v9.dkf.LocalSKO newLocalSKO = new generated.v9.dkf.LocalSKO();

            newLocalSKO.setFile(localSKO.getFile());
            newAutoTutorSKO.setScript(newLocalSKO);

        } else if (prevATSKO.getScript() instanceof generated.v8.dkf.ATRemoteSKO) {
            generated.v8.dkf.ATRemoteSKO prevLocalSKO = (generated.v8.dkf.ATRemoteSKO) prevATSKO.getScript();

            generated.v9.dkf.ATRemoteSKO.URL newURL = new generated.v9.dkf.ATRemoteSKO.URL();
            newURL.setAddress(prevLocalSKO.getURL().getAddress());

            generated.v9.dkf.ATRemoteSKO newATRemoteSKO = new generated.v9.dkf.ATRemoteSKO();
            newATRemoteSKO.setURL(newURL);
            newAutoTutorSKO.setScript(newATRemoteSKO);

        } else {
            throw new IllegalArgumentException(
                    "Found unhandled AutoTutor script reference type of " + prevATSKO.getScript());
        }

        return newAutoTutorSKO;
    }

    /**
     * Convert a Message object for scenarios
     *
     * @param message - the message to convert
     * @return a new Message object
     */
    private generated.v9.dkf.Message convertMessage(generated.v8.dkf.Message message) {
        generated.v9.dkf.Message newMessage = new generated.v9.dkf.Message();
        newMessage.setContent(message.getContent());

        if (message.getDelivery() != null) {
            generated.v9.dkf.Message.Delivery newDelivery = new generated.v9.dkf.Message.Delivery();

            if (message.getDelivery().getInTrainingApplication() != null) {
                generated.v9.dkf.Message.Delivery.InTrainingApplication newInTrainingApp = new generated.v9.dkf.Message.Delivery.InTrainingApplication();
                newInTrainingApp.setEnabled(generated.v9.dkf.BooleanEnum.fromValue(
                        message.getDelivery().getInTrainingApplication().getEnabled().toString().toLowerCase()));
                newDelivery.setInTrainingApplication(newInTrainingApp);
            }
            if (message.getDelivery().getInTutor() != null) {
                generated.v9.dkf.InTutor newInTutor = new generated.v9.dkf.InTutor();
                newInTutor.setMessagePresentation(message.getDelivery().getInTutor().getMessagePresentation());
                newInTutor.setTextEnhancement(message.getDelivery().getInTutor().getTextEnhancement());
                newDelivery.setInTutor(newInTutor);
            }

            newMessage.setDelivery(newDelivery);
        }

        return newMessage;
    }

    /**
     * Convert an entities object to a new entities object.
     *
     * @param entities - the object to convert
     * @return generated.v8.dkf.Entities - the new object
     * @throws IllegalArgumentException if the start location coordinate type is unknown
     */
    private static generated.v9.dkf.Entities convertEntities(generated.v8.dkf.Entities entities)
            throws IllegalArgumentException {

        generated.v9.dkf.Entities newEntities = new generated.v9.dkf.Entities();
        for (generated.v8.dkf.StartLocation location : entities.getStartLocation()) {

            generated.v9.dkf.StartLocation newLocation = new generated.v9.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }

        return newEntities;
    }

    /**
     * Convert a path object into a new path object.
     *
     * @param path - the object to convert
     * @param scenarioObjects - contains the collection of all places of interest
     * @param pathNamePrefix - a prefix that will be prepended to the name of the path generated by this method
     * @return the new object
     */
    private static generated.v9.dkf.PathRef convertPath(generated.v8.dkf.Path path, generated.v9.dkf.Objects scenarioObjects, String pathNamePrefix){

        generated.v9.dkf.Path newPath = new generated.v9.dkf.Path();
        for (generated.v8.dkf.Segment segment : path.getSegment()) {

            generated.v9.dkf.Segment newSegment = new generated.v9.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());

            generated.v9.dkf.Segment.Start start = new generated.v9.dkf.Segment.Start();
            
            //replace the old waypoint string name reference with the coordinate of that waypoint
            start.setCoordinate(getWaypointCoordinate(segment.getStart().getWaypoint(), scenarioObjects));
            newSegment.setStart(start);

            generated.v9.dkf.Segment.End end = new generated.v9.dkf.Segment.End();
            end.setCoordinate(getWaypointCoordinate(segment.getEnd().getWaypoint(), scenarioObjects));
            newSegment.setEnd(end);

            newPath.getSegment().add(newSegment);
        }

        if(scenarioObjects.getPlacesOfInterest() == null) {
            scenarioObjects.setPlacesOfInterest(new generated.v9.dkf.PlacesOfInterest());
        }

        //create a new path of interest with a unique name
        int i = 1;
        String pathName = GENERATED_PATH_NAME + " " + i;
        
        int index = 0;
        while(index < scenarioObjects.getPlacesOfInterest().getPointOrPathOrArea().size()) {
            
            Serializable placeOfInterest = scenarioObjects.getPlacesOfInterest().getPointOrPathOrArea().get(index);
            
            if((placeOfInterest instanceof generated.v9.dkf.Point && pathName.equals(((generated.v9.dkf.Point) placeOfInterest).getName()))
                    || (placeOfInterest instanceof generated.v9.dkf.Path && pathName.equals(((generated.v9.dkf.Path) placeOfInterest).getName()))
                    || (placeOfInterest instanceof generated.v9.dkf.Area && pathName.equals(((generated.v9.dkf.Area) placeOfInterest).getName()))) {
                
                //if the current name is already being used by another place of interest, increment it
                pathName = pathNamePrefix + " - " + GENERATED_PATH_NAME + " " + ++i;
                
                //return to the beginning of the list and check for uniqueness again
                index = 0;
                
            } else {
                index++;
            }
        }
        
        newPath.setName(pathName);
        
        scenarioObjects.getPlacesOfInterest().getPointOrPathOrArea().add(newPath);
        
        generated.v9.dkf.PathRef ref = new generated.v9.dkf.PathRef();
        ref.setValue(pathName);

        return ref;
    }
    
    /**
     * Return the coordinate of a waypoint (now called point) referenced by the name provided from
     * the scenario's (dkf) list of authored places of interest (new name).
     * 
     * @param waypointName the name of the waypoint to find and return the coordinate object for
     * @param scenarioObjects contains all the places of interest (already up converted)
     * @return the coordinate for the waypoint.  Will be null if the waypoint could not be found.
     */
    private static generated.v9.dkf.Coordinate getWaypointCoordinate(String waypointName, generated.v9.dkf.Objects scenarioObjects){
        
        if(scenarioObjects != null && scenarioObjects.getPlacesOfInterest() != null){
            
            for(Serializable placeOfInterest : scenarioObjects.getPlacesOfInterest().getPointOrPathOrArea()){
                
                if(placeOfInterest instanceof generated.v9.dkf.Point){
                    //found what was originally a 'waypoint'
                    
                    generated.v9.dkf.Point point = (generated.v9.dkf.Point)placeOfInterest;
                    if(point.getName() != null && point.getName().equalsIgnoreCase(waypointName)){
                        //found matching name
                        return point.getCoordinate();
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Convert a checkpoint object into a new checkpoint object.
     *
     * @param checkpoint - the object to convert
     * @return generated.v8.dkf.Checkpoint - the new object
     */
    private static generated.v9.dkf.Checkpoint convertCheckpoint(generated.v8.dkf.Checkpoint checkpoint) {

        generated.v9.dkf.Checkpoint newCheckpoint = new generated.v9.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setPoint(checkpoint.getWaypoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());

        return newCheckpoint;
    }

    /**
     * Convert an evaluators object into a new evaluators object.
     *
     * @param evaluators - the object to convert
     * @return the new object
     */
    private static generated.v9.dkf.Evaluators convertEvaluators(generated.v8.dkf.Evaluators evaluators) {

        generated.v9.dkf.Evaluators newEvaluators = new generated.v9.dkf.Evaluators();
        for (generated.v8.dkf.Evaluator evaluator : evaluators.getEvaluator()) {

            generated.v9.dkf.Evaluator newEvaluator = new generated.v9.dkf.Evaluator();
            newEvaluator.setAssessment(evaluator.getAssessment());
            newEvaluator.setValue(evaluator.getValue());
            newEvaluator.setOperator(evaluator.getOperator());

            newEvaluators.getEvaluator().add(newEvaluator);
        }

        return newEvaluators;
    }

    /**
     * Converts excavator component inputs
     *
     * @param oldCompList - the component list
     * @return the new component list
     */
    private static List<generated.v9.dkf.HasMovedExcavatorComponentInput.Component> convertComponents(
            List<generated.v8.dkf.HasMovedExcavatorComponentInput.Component> oldCompList) {

        List<generated.v9.dkf.HasMovedExcavatorComponentInput.Component> componentList = new ArrayList<generated.v9.dkf.HasMovedExcavatorComponentInput.Component>();

        for (generated.v8.dkf.HasMovedExcavatorComponentInput.Component oldComp : oldCompList) {
            generated.v9.dkf.HasMovedExcavatorComponentInput.Component newComp = new generated.v9.dkf.HasMovedExcavatorComponentInput.Component();
            newComp.setComponentType(
                    generated.v9.dkf.ExcavatorComponentEnum.fromValue(oldComp.getComponentType().value()));

            if (oldComp
                    .getDirectionType() instanceof generated.v8.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional) {
                generated.v8.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional oldBiDirectional = (generated.v8.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional) oldComp
                        .getDirectionType();
                generated.v9.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional newBiDirectional = new generated.v9.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional();
                newBiDirectional.setNegativeRotation(oldBiDirectional.getNegativeRotation());
                newBiDirectional.setPositiveRotation(oldBiDirectional.getPositiveRotation());
                newComp.setDirectionType(newBiDirectional);
            } else {
                newComp.setDirectionType(oldComp.getDirectionType());
            }

            componentList.add(newComp);
        }

        return componentList;
    }

    /**
     * Convert an assessment object into a new assessment object.
     *
     * @param assessments - the assessment object to convert
     * @return the new assessment object
     */
    private static generated.v9.dkf.Assessments convertAssessments(generated.v8.dkf.Assessments assessments) {

        generated.v9.dkf.Assessments newAssessments = new generated.v9.dkf.Assessments();

        List<generated.v8.dkf.Assessments.Survey> surveys = new ArrayList<generated.v8.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
            if (assessmentType instanceof generated.v8.dkf.Assessments.Survey) {
                surveys.add((generated.v8.dkf.Assessments.Survey) assessmentType);
            }
        }

        for (generated.v8.dkf.Assessments.Survey survey : surveys) {
            generated.v9.dkf.Assessments.Survey newSurvey = new generated.v9.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());

            final generated.v8.dkf.Questions oldQuestions = survey.getQuestions();
            if (oldQuestions != null && !oldQuestions.getQuestion().isEmpty()) {
                generated.v9.dkf.Questions newQuestions = new generated.v9.dkf.Questions();
                for (generated.v8.dkf.Question oldQuestion : oldQuestions.getQuestion()) {

                    generated.v9.dkf.Question newQuestion = new generated.v9.dkf.Question();
                    newQuestion.setKey(oldQuestion.getKey());

                    for (generated.v8.dkf.Reply oldReply : oldQuestion.getReply()) {

                        generated.v9.dkf.Reply newReply = new generated.v9.dkf.Reply();
                        newReply.setKey(oldReply.getKey());
                        newReply.setResult(oldReply.getResult());

                        newQuestion.getReply().add(newReply);
                    }

                    newQuestions.getQuestion().add(newQuestion);
                }

                newSurvey.setQuestions(newQuestions);
            }

            newAssessments.getAssessmentTypes().add(newSurvey);
        }

        return newAssessments;
    }

    /**
     * Convert a collection of start trigger objects into the new schema version.
     *
     * @param list - collection of trigger objects to convert
     * @return List<generated.v9.dkf.StartTriggers.Trigger> - converted trigger objects (same size as
     *         triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private static List<generated.v9.dkf.StartTriggers.Trigger> convertStartTriggers(
            List<generated.v8.dkf.StartTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.v9.dkf.StartTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v8.dkf.StartTriggers.Trigger triggerObj : list) {

            generated.v9.dkf.StartTriggers.Trigger trigger = new generated.v9.dkf.StartTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj.getTriggerType()));
            trigger.setTriggerDelay(triggerObj.getTriggerDelay());

            newTriggerObjects.add(trigger);
        }

        return newTriggerObjects;
    }

    /**
     * Convert a collection of end trigger objects into the new schema version.
     *
     * @param list - collection of trigger objects to convert
     * @return List<generated.v9.dkf.EndTriggers.Trigger> - converted trigger objects (same size as
     *         triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private static List<generated.v9.dkf.EndTriggers.Trigger> convertEndTriggers(
            List<generated.v8.dkf.EndTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.v9.dkf.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v8.dkf.EndTriggers.Trigger triggerObj : list) {

            generated.v9.dkf.EndTriggers.Trigger trigger = new generated.v9.dkf.EndTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj.getTriggerType()));
            trigger.setTriggerDelay(triggerObj.getTriggerDelay());

            newTriggerObjects.add(trigger);
        }

        return newTriggerObjects;
    }

    /**
     * Convert a collection of end trigger objects into the new schema version.
     *
     * @param list - collection of trigger objects to convert
     * @return List<generated.v9.dkf.Scenario.EndTriggers.Trigger> - converted trigger objects (same
     *         size as triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private static List<generated.v9.dkf.Scenario.EndTriggers.Trigger> convertScenarioEndTriggers(
            List<generated.v8.dkf.Scenario.EndTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.v9.dkf.Scenario.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v8.dkf.Scenario.EndTriggers.Trigger triggerObj : list) {

            generated.v9.dkf.Scenario.EndTriggers.Trigger trigger = new generated.v9.dkf.Scenario.EndTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj.getTriggerType()));
            trigger.setTriggerDelay(triggerObj.getTriggerDelay());
            trigger.setMessage(triggerObj.getMessage());

            newTriggerObjects.add(trigger);
        }

        return newTriggerObjects;
    }

    /**
     * Convert a collection of trigger objects (start or end triggers) into the new schema version.
     *
     * @param triggerObj the trigger to convert
     * @return the converted trigger object
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private static Serializable convertTrigger(Serializable triggerObj) throws IllegalArgumentException {

        if (triggerObj instanceof generated.v8.dkf.EntityLocation) {

            generated.v8.dkf.EntityLocation entityLocation = (generated.v8.dkf.EntityLocation) triggerObj;
            generated.v9.dkf.EntityLocation newEntityLocation = new generated.v9.dkf.EntityLocation();

            generated.v9.dkf.StartLocation startLocation = new generated.v9.dkf.StartLocation();
            startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
            
            generated.v9.dkf.TriggerLocation triggerLocation = new generated.v9.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);

            return newEntityLocation;
        } else if (triggerObj instanceof generated.v8.dkf.LearnerLocation) {

            generated.v8.dkf.LearnerLocation learnerLocation = (generated.v8.dkf.LearnerLocation) triggerObj;
            generated.v9.dkf.LearnerLocation newLearnerLocation = new generated.v9.dkf.LearnerLocation();

            newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));

            return newLearnerLocation;
        } else if (triggerObj instanceof generated.v8.dkf.ConceptEnded) {

            generated.v8.dkf.ConceptEnded conceptEnded = (generated.v8.dkf.ConceptEnded) triggerObj;
            generated.v9.dkf.ConceptEnded newConceptEnded = new generated.v9.dkf.ConceptEnded();

            newConceptEnded.setNodeId(conceptEnded.getNodeId());

            return newConceptEnded;
        } else if (triggerObj instanceof generated.v8.dkf.ChildConceptEnded) {

            generated.v8.dkf.ChildConceptEnded childConceptEnded = (generated.v8.dkf.ChildConceptEnded) triggerObj;
            generated.v9.dkf.ChildConceptEnded newChildConceptEnded = new generated.v9.dkf.ChildConceptEnded();

            newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());

            return newChildConceptEnded;
        } else if (triggerObj instanceof generated.v8.dkf.TaskEnded) {

            generated.v8.dkf.TaskEnded taskEnded = (generated.v8.dkf.TaskEnded) triggerObj;
            generated.v9.dkf.TaskEnded newTaskEnded = new generated.v9.dkf.TaskEnded();

            newTaskEnded.setNodeId(taskEnded.getNodeId());

            return newTaskEnded;
        } else if (triggerObj instanceof generated.v8.dkf.ConceptAssessment) {

            generated.v8.dkf.ConceptAssessment conceptAssessment = (generated.v8.dkf.ConceptAssessment) triggerObj;
            generated.v9.dkf.ConceptAssessment newConceptAssessment = new generated.v9.dkf.ConceptAssessment();

            newConceptAssessment.setResult(conceptAssessment.getResult());
            newConceptAssessment.setConcept(conceptAssessment.getConcept());

            return newConceptAssessment;
        } else {
            throw new IllegalArgumentException("Found unhandled trigger type of " + triggerObj);
        }
    }

    /**
     * Convert a coordinate object into the latest schema version.
     *
     * @param coordinate - coordinate object to convert
     * @return generated.v9.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException if the coordinate is of an unknown type
     */
    private static generated.v9.dkf.Coordinate convertCoordinate(generated.v8.dkf.Coordinate coordinate)
            throws IllegalArgumentException {

        generated.v9.dkf.Coordinate newCoord = new generated.v9.dkf.Coordinate();

        Object coordType = coordinate.getType();
        if (coordType instanceof generated.v8.dkf.GCC) {

            generated.v8.dkf.GCC gcc = (generated.v8.dkf.GCC) coordType;
            generated.v9.dkf.GCC newGCC = new generated.v9.dkf.GCC();

            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());

            newCoord.setType(newGCC);

        } else if (coordType instanceof generated.v8.dkf.GDC) {
            // generated.
            generated.v8.dkf.GDC gdc = (generated.v8.dkf.GDC) coordType;
            generated.v9.dkf.GDC newGDC = new generated.v9.dkf.GDC();

            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());

            newCoord.setType(newGDC);

        } else if (coordType instanceof generated.v8.dkf.VBSAGL) {

            generated.v8.dkf.VBSAGL agl = (generated.v8.dkf.VBSAGL) coordType;
            generated.v9.dkf.AGL newAGL = new generated.v9.dkf.AGL();

            newAGL.setX(agl.getX());
            newAGL.setY(agl.getY());
            newAGL.setElevation(agl.getZ());

            newCoord.setType(newAGL);

        } else {
            throw new IllegalArgumentException("Found unhandled coordinate type of " + coordType);
        }

        return newCoord;
    }

    /**
     * Convert a strategy handler object to a new version of the strategy handler object.
     *
     * @param handler - the object to convert
     * @return generated.v9.dkf.StrategyHandler - the new object
     */
    private static generated.v9.dkf.StrategyHandler convertStrategyHandler(generated.v8.dkf.StrategyHandler handler) {

        generated.v9.dkf.StrategyHandler newHandler = new generated.v9.dkf.StrategyHandler();

        if (handler.getParams() != null) {

            generated.v9.dkf.StrategyHandler.Params newParams = new generated.v9.dkf.StrategyHandler.Params();
            generated.v9.dkf.Nvpair nvpair = new generated.v9.dkf.Nvpair();

            nvpair.setName(handler.getParams().getNvpair().get(0).getName());
            nvpair.setValue(handler.getParams().getNvpair().get(0).getValue());

            newParams.getNvpair().add(nvpair);
            newHandler.setParams(newParams);
        }

        newHandler.setImpl(handler.getImpl());
        return newHandler;
    }

    /**
     * Convert a concepts object to a new version of the concepts object.
     *
     * @param concepts - the object to convert
     * @param scenarioObjects contains all the places of interest (already up converted)
     * @return generated.v9.dkf.Concepts - the new object
     * @throws IllegalArgumentException if the concept contains an entity with an unknown coordinate
     *         type
     */
    private generated.v9.dkf.Concepts convertConcepts(generated.v8.dkf.Concepts concepts, generated.v9.dkf.Objects scenarioObjects) throws IllegalArgumentException{

        generated.v9.dkf.Concepts newConcepts = new generated.v9.dkf.Concepts();
        for (generated.v8.dkf.Concept concept : concepts.getConcept()) {

            generated.v9.dkf.Concept newConcept = new generated.v9.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());

            if (concept.getAssessments() != null) {
                newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }

            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if (conditionsOrConcepts instanceof generated.v8.dkf.Concepts) {
                // nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v8.dkf.Concepts)conditionsOrConcepts, scenarioObjects) );

            } else if (conditionsOrConcepts instanceof generated.v8.dkf.Conditions) {

                generated.v9.dkf.Conditions newConditions = new generated.v9.dkf.Conditions();

                generated.v8.dkf.Conditions conditions = (generated.v8.dkf.Conditions) conditionsOrConcepts;

                for (generated.v8.dkf.Condition condition : conditions.getCondition()) {

                    generated.v9.dkf.Condition newCondition = new generated.v9.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());

                    if (condition.getDefault() != null) {
                        generated.v9.dkf.Default newDefault = new generated.v9.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }

                    // Input
                    generated.v9.dkf.Input newInput = new generated.v9.dkf.Input();
                    if (condition.getInput() != null) {

                        Object inputType = condition.getInput().getType();

                        if (inputType == null) {
                            // nothing to do right now

                        } else if (inputType instanceof generated.v8.dkf.ApplicationCompletedCondition) {

                            generated.v8.dkf.ApplicationCompletedCondition conditionInput = (generated.v8.dkf.ApplicationCompletedCondition) inputType;

                            generated.v9.dkf.ApplicationCompletedCondition newConditionInput = new generated.v9.dkf.ApplicationCompletedCondition();

                            if (conditionInput.getIdealCompletionDuration() != null) {
                                newConditionInput
                                        .setIdealCompletionDuration(conditionInput.getIdealCompletionDuration());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.AutoTutorConditionInput) {

                            generated.v8.dkf.AutoTutorConditionInput conditionInput = (generated.v8.dkf.AutoTutorConditionInput) inputType;

                            generated.v9.dkf.AutoTutorConditionInput newConditionInput = new generated.v9.dkf.AutoTutorConditionInput();

                            if (conditionInput.getAutoTutorSKO() != null) {

                                generated.v8.dkf.AutoTutorSKO prevAutoTutorSKO = conditionInput.getAutoTutorSKO();
                                generated.v9.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevAutoTutorSKO);
                                newConditionInput.setAutoTutorSKO(newAutoTutorSKO);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.AvoidLocationCondition) {

                            generated.v8.dkf.AvoidLocationCondition conditionInput = (generated.v8.dkf.AvoidLocationCondition) inputType;

                            generated.v9.dkf.AvoidLocationCondition newConditionInput = new generated.v9.dkf.AvoidLocationCondition();

                            if (conditionInput.getWaypointRef() != null) {
                                newConditionInput.getPointRef().add(convertWaypointRef(conditionInput.getWaypointRef()));
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.CheckpointPaceCondition) {

                            generated.v8.dkf.CheckpointPaceCondition conditionInput = (generated.v8.dkf.CheckpointPaceCondition) inputType;

                            generated.v9.dkf.CheckpointPaceCondition newConditionInput = new generated.v9.dkf.CheckpointPaceCondition();
                            for (generated.v8.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()) {

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.CheckpointProgressCondition) {

                            generated.v8.dkf.CheckpointProgressCondition conditionInput = (generated.v8.dkf.CheckpointProgressCondition) inputType;

                            generated.v9.dkf.CheckpointProgressCondition newConditionInput = new generated.v9.dkf.CheckpointProgressCondition();
                            for (generated.v8.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()) {

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.CorridorBoundaryCondition) {

                            generated.v8.dkf.CorridorBoundaryCondition conditionInput = (generated.v8.dkf.CorridorBoundaryCondition) inputType;

                            generated.v9.dkf.CorridorBoundaryCondition newConditionInput = new generated.v9.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            newConditionInput.setPathRef(convertPath(conditionInput.getPath(), scenarioObjects, concept.getName()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.CorridorPostureCondition) {

                            generated.v8.dkf.CorridorPostureCondition conditionInput = (generated.v8.dkf.CorridorPostureCondition) inputType;

                            generated.v9.dkf.CorridorPostureCondition newConditionInput = new generated.v9.dkf.CorridorPostureCondition();
                            newConditionInput.setPathRef(convertPath(conditionInput.getPath(), scenarioObjects, concept.getName()));

                            generated.v9.dkf.Postures postures = new generated.v9.dkf.Postures();
                            for (String strPosture : conditionInput.getPostures().getPosture()) {
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.EliminateHostilesCondition) {

                            generated.v8.dkf.EliminateHostilesCondition conditionInput = (generated.v8.dkf.EliminateHostilesCondition) inputType;

                            generated.v9.dkf.EliminateHostilesCondition newConditionInput = new generated.v9.dkf.EliminateHostilesCondition();

                            if (conditionInput.getEntities() != null) {
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.EnterAreaCondition) {

                            generated.v8.dkf.EnterAreaCondition conditionInput = (generated.v8.dkf.EnterAreaCondition) inputType;

                            generated.v9.dkf.EnterAreaCondition newConditionInput = new generated.v9.dkf.EnterAreaCondition();

                            for (generated.v8.dkf.Entrance entrance : conditionInput.getEntrance()) {

                                generated.v9.dkf.Entrance newEntrance = new generated.v9.dkf.Entrance();

                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());

                                generated.v9.dkf.Inside newInside = new generated.v9.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setPoint(entrance.getInside().getWaypoint());
                                newEntrance.setInside(newInside);

                                generated.v9.dkf.Outside newOutside = new generated.v9.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setPoint(entrance.getOutside().getWaypoint());
                                newEntrance.setOutside(newOutside);

                                newConditionInput.getEntrance().add(newEntrance);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.ExplosiveHazardSpotReportCondition) {

                            @SuppressWarnings("unused")
                            generated.v8.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v8.dkf.ExplosiveHazardSpotReportCondition) inputType;

                            generated.v9.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v9.dkf.ExplosiveHazardSpotReportCondition();

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.GenericConditionInput) {

                            generated.v8.dkf.GenericConditionInput conditionInput = (generated.v8.dkf.GenericConditionInput) inputType;

                            generated.v9.dkf.GenericConditionInput newConditionInput = new generated.v9.dkf.GenericConditionInput();

                            if (conditionInput.getNvpair() != null) {
                                for (generated.v8.dkf.Nvpair nvPair : conditionInput.getNvpair()) {
                                    newConditionInput.getNvpair().add(convertNvpair(nvPair));
                                }
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.HasMovedExcavatorComponentInput) {

                            generated.v8.dkf.HasMovedExcavatorComponentInput conditionInput = (generated.v8.dkf.HasMovedExcavatorComponentInput) inputType;
                            generated.v9.dkf.HasMovedExcavatorComponentInput newConditionInput = new generated.v9.dkf.HasMovedExcavatorComponentInput();

                            newConditionInput.getComponent().addAll(convertComponents(conditionInput.getComponent()));
                            newConditionInput.setMaxAssessments(conditionInput.getMaxAssessments());

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.IdentifyPOIsCondition) {

                            generated.v8.dkf.IdentifyPOIsCondition conditionInput = (generated.v8.dkf.IdentifyPOIsCondition) inputType;

                            generated.v9.dkf.IdentifyPOIsCondition newConditionInput = new generated.v9.dkf.IdentifyPOIsCondition();

                            if (conditionInput.getPois() != null) {

                                generated.v9.dkf.Pois pois = new generated.v9.dkf.Pois();
                                for (generated.v8.dkf.WaypointRef waypointRef : conditionInput.getPois()
                                        .getWaypointRef()) {
                                    pois.getPointRef().add(convertWaypointRef(waypointRef));
                                }

                                newConditionInput.setPois(pois);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.LifeformTargetAccuracyCondition) {

                            generated.v8.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v8.dkf.LifeformTargetAccuracyCondition) inputType;

                            generated.v9.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v9.dkf.LifeformTargetAccuracyCondition();

                            if (conditionInput.getEntities() != null) {
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.MarksmanshipPrecisionCondition) {

                            generated.v8.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v8.dkf.MarksmanshipPrecisionCondition) inputType;

                            generated.v9.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v9.dkf.MarksmanshipPrecisionCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.MarksmanshipSessionCompleteCondition) {

                            generated.v8.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v8.dkf.MarksmanshipSessionCompleteCondition) inputType;

                            generated.v9.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v9.dkf.MarksmanshipSessionCompleteCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.NineLineReportCondition) {

                            @SuppressWarnings("unused")
                            generated.v8.dkf.NineLineReportCondition conditionInput = (generated.v8.dkf.NineLineReportCondition) inputType;

                            generated.v9.dkf.NineLineReportCondition newConditionInput = new generated.v9.dkf.NineLineReportCondition();

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.NoConditionInput) {
                            newInput.setType(new generated.v9.dkf.NoConditionInput());

                        } else if (inputType instanceof generated.v8.dkf.NumberOfShotsFiredCondition) {

                            generated.v8.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v8.dkf.NumberOfShotsFiredCondition) inputType;

                            generated.v9.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v9.dkf.NumberOfShotsFiredCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.PowerPointDwellCondition) {

                            generated.v8.dkf.PowerPointDwellCondition conditionInput = (generated.v8.dkf.PowerPointDwellCondition) inputType;

                            generated.v9.dkf.PowerPointDwellCondition newConditionInput = new generated.v9.dkf.PowerPointDwellCondition();

                            generated.v9.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v9.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);

                            generated.v9.dkf.PowerPointDwellCondition.Slides slides = new generated.v9.dkf.PowerPointDwellCondition.Slides();
                            for (generated.v8.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput
                                    .getSlides().getSlide()) {

                                generated.v9.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v9.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());

                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.RulesOfEngagementCondition) {

                            generated.v8.dkf.RulesOfEngagementCondition conditionInput = (generated.v8.dkf.RulesOfEngagementCondition) inputType;

                            generated.v9.dkf.RulesOfEngagementCondition newConditionInput = new generated.v9.dkf.RulesOfEngagementCondition();
                            generated.v9.dkf.Wcs newWCS = new generated.v9.dkf.Wcs();
                            newWCS.setValue(generated.v9.dkf.WeaponControlStatusEnum
                                    .fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.SIMILEConditionInput) {

                            generated.v8.dkf.SIMILEConditionInput conditionInput = (generated.v8.dkf.SIMILEConditionInput) inputType;

                            generated.v9.dkf.SIMILEConditionInput newConditionInput = new generated.v9.dkf.SIMILEConditionInput();

                            if (conditionInput.getConditionKey() != null) {
                                newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }

                            if (conditionInput.getConfigurationFile() != null) {
                                newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.SpotReportCondition) {

                            @SuppressWarnings("unused")
                            generated.v8.dkf.SpotReportCondition conditionInput = (generated.v8.dkf.SpotReportCondition) inputType;

                            generated.v9.dkf.SpotReportCondition newConditionInput = new generated.v9.dkf.SpotReportCondition();

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.TimerConditionInput) {

                            generated.v8.dkf.TimerConditionInput conditionInput = (generated.v8.dkf.TimerConditionInput) inputType;

                            generated.v9.dkf.TimerConditionInput newConditionInput = new generated.v9.dkf.TimerConditionInput();

                            newConditionInput.setRepeatable(generated.v9.dkf.BooleanEnum
                                    .fromValue(conditionInput.getRepeatable().toString().toLowerCase()));
                            newConditionInput.setInterval(conditionInput.getInterval());

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v8.dkf.UseRadioCondition) {

                            @SuppressWarnings("unused")
                            generated.v8.dkf.UseRadioCondition conditionInput = (generated.v8.dkf.UseRadioCondition) inputType;

                            generated.v9.dkf.UseRadioCondition newConditionInput = new generated.v9.dkf.UseRadioCondition();

                            newInput.setType(newConditionInput);

                        } else {
                            throw new IllegalArgumentException("Found unhandled condition input type of " + inputType);
                        }

                    }
                    newCondition.setInput(newInput);

                    // Scoring
                    generated.v9.dkf.Scoring newScoring = new generated.v9.dkf.Scoring();
                    if (condition.getScoring() != null) {
                        // Only add the scoring element if it has children.
                        // As of version 5, there cannot be a scoring element with no children
                        if (!condition.getScoring().getType().isEmpty()) {

                            for (Object scoringType : condition.getScoring().getType()) {

                                if (scoringType instanceof generated.v8.dkf.Count) {

                                    generated.v8.dkf.Count count = (generated.v8.dkf.Count) scoringType;

                                    generated.v9.dkf.Count newCount = new generated.v9.dkf.Count();
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v9.dkf.UnitsEnumType.fromValue(count.getUnits().value()));

                                    if (count.getEvaluators() != null) {
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }

                                    newScoring.getType().add(newCount);

                                } else if (scoringType instanceof generated.v8.dkf.CompletionTime) {

                                    generated.v8.dkf.CompletionTime complTime = (generated.v8.dkf.CompletionTime) scoringType;

                                    generated.v9.dkf.CompletionTime newComplTime = new generated.v9.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(
                                            generated.v9.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if (complTime.getEvaluators() != null) {
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }

                                    newScoring.getType().add(newComplTime);

                                } else if (scoringType instanceof generated.v8.dkf.ViolationTime) {

                                    generated.v8.dkf.ViolationTime violationTime = (generated.v8.dkf.ViolationTime) scoringType;

                                    generated.v9.dkf.ViolationTime newViolationTime = new generated.v9.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(
                                            generated.v9.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
                                    if (violationTime.getEvaluators() != null) {
                                        newViolationTime
                                                .setEvaluators(convertEvaluators(violationTime.getEvaluators()));
                                    }

                                    newScoring.getType().add(newViolationTime);

                                } else {
                                    throw new IllegalArgumentException(
                                            "Found unhandled scoring type of " + scoringType);
                                }
                            }

                            newCondition.setScoring(newScoring);
                        }
                    }

                    newConditions.getCondition().add(newCondition);
                }

                newConcept.setConditionsOrConcepts(newConditions);
                
                if (concept.getPriority() != null) {
                    newConcept.setPriority(concept.getPriority());
                }

            } else {
                throw new IllegalArgumentException("Found unhandled subconcept node type of " + conditionsOrConcepts);
            }

            newConcepts.getConcept().add(newConcept);

        }

        return newConcepts;
    }

    /**
     * Convert a Nvpair object to a new Nvpair object.
     *
     * @param nvPair - the object to convert
     * @return the new object
     */
    private static generated.v9.dkf.Nvpair convertNvpair(generated.v8.dkf.Nvpair nvPair) {

        generated.v9.dkf.Nvpair newNvpair = new generated.v9.dkf.Nvpair();
        newNvpair.setName(nvPair.getName());
        newNvpair.setValue(nvPair.getValue());

        return newNvpair;
    }

    /**
     * Convert a waypointref object to a new PointRef object.
     *
     * @param waypointRef - the object to convert
     * @return the new object
     */
    private static generated.v9.dkf.PointRef convertWaypointRef(generated.v8.dkf.WaypointRef waypointRef){

        generated.v9.dkf.PointRef newPoint = new generated.v9.dkf.PointRef();
        newPoint.setValue(waypointRef.getValue());
        newPoint.setDistance(waypointRef.getDistance());

        return newPoint;
    }

    /**
     * Converts a boolean enum to the next version
     * 
     * @param booleanEnum the boolean enum to convert
     * @return the converted boolean enum
     */
    private static generated.v9.dkf.BooleanEnum convertBooleanEnum(generated.v8.dkf.BooleanEnum booleanEnum) {
        if (booleanEnum == null) {
            return null;
        }

        return generated.v8.dkf.BooleanEnum.TRUE.equals(booleanEnum) ? generated.v9.dkf.BooleanEnum.TRUE
                : generated.v9.dkf.BooleanEnum.FALSE;
    }

    /******************* END CONVERT DKF *******************/
}
