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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import mil.arl.gift.common.course.dkf.team.TeamOrganization;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.StringUtils;

/**
 * Responsible for converting 2019-1 GIFT version XML files to 2020-1 versions when applicable, i.e.
 * if a particular schema changes enough to warrant a conversion process.
 *
 * @author cpadilla
 *
 */
public class ConversionWizardUtil_v2019_1_v2020_1 extends AbstractConversionWizardUtil {

    //////////////////////////////////////////////////////////////
    /////////// DON'T REMOVE THE ITEMS IN THIS SECTION ///////////
    //////////////////////////////////////////////////////////////

    /** The new version number */
    private static final String VERSION_NUMBER = "10.0.1";

    @Override
    public String getConvertedVersionNumber() {
        return VERSION_NUMBER;
    }

    /********* PREVIOUS SCHEMA FILES *********/

    /** Path to the specific version folder */
    private static final String versionPathPrefix = StringUtils.join(File.separator,
            Arrays.asList("data", "conversionWizard", "v2019_1"));

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
    public static final Class<?> PREV_COURSE_ROOT = generated.v9.course.Course.class;

    /** Previous DKF schema root */
    public static final Class<?> PREV_DKF_ROOT = generated.v9.dkf.Scenario.class;

    /** Previous metadata schema root */
    public static final Class<?> PREV_METADATA_ROOT = generated.v9.metadata.Metadata.class;

    /** Previous learner schema root */
    public static final Class<?> PREV_LEARNER_ROOT = generated.v9.learner.LearnerConfiguration.class;

    /** Previous pedagogical schema root */
    public static final Class<?> PREV_PEDAGOGICAL_ROOT = generated.v9.ped.EMAP.class;

    /** Previous sensor schema root */
    public static final Class<?> PREV_SENSOR_ROOT = generated.v9.sensor.SensorsConfiguration.class;

    /** Previous training App schema root */
    public static final Class<?> PREV_TRAINING_APP_ROOT = generated.v9.course.TrainingApplicationWrapper.class;

    /** Previous conversation schema root */
    public static final Class<?> PREV_CONVERSATION_ROOT = generated.v9.conversation.Conversation.class;

    /** Previous lesson Material schema root */
    public static final Class<?> PREV_LESSON_MATERIAL_ROOT = generated.v9.course.LessonMaterialList.class;

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
    
    private generated.v10.dkf.TeamMember newTeamOfOneMember;

    /******************* CONVERT DKF *******************/

    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError)
            throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {

        UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(),
                failOnFirstSchemaError);
        generated.v9.dkf.Scenario prevScenario = (generated.v9.dkf.Scenario) uFile.getUnmarshalled();

        return convertScenario(prevScenario, showCompletionDialog);
    }

    @Override
    public UnmarshalledFile convertScenario(Serializable prevScenarioObj, boolean showCompletionDialog)
            throws IllegalArgumentException {
        generated.v9.dkf.Scenario prevScenario = (generated.v9.dkf.Scenario) prevScenarioObj;
        generated.v10.dkf.Scenario newScenario = new generated.v10.dkf.Scenario();

        newScenario.setDescription(prevScenario.getDescription());
        newScenario.setName(prevScenario.getName());
        newScenario.setVersion(VERSION_NUMBER);

        // Learner Id
        generated.v9.dkf.LearnerId learnerId = prevScenario.getLearnerId();
        if (learnerId != null) {
            generated.v10.dkf.LearnerId newLearnerId = new generated.v10.dkf.LearnerId();

            generated.v9.dkf.StartLocation learnerType = learnerId.getType();
            generated.v10.dkf.StartLocation newStartLocation = new generated.v10.dkf.StartLocation();
            newStartLocation.setCoordinate(learnerType != null ? convertCoordinate(learnerType.getCoordinate()) : new generated.v10.dkf.Coordinate());
            newLearnerId.setType(newStartLocation);

            generated.v10.dkf.TeamMember teamMember = new generated.v10.dkf.TeamMember();
            teamMember.setLearnerId(newLearnerId);
            teamMember.setName(TeamOrganization.DEFAULT_LEARNER_TEAM_MEMBER_NAME);
            teamMember.setPlayable(true);

            generated.v10.dkf.Team team = new generated.v10.dkf.Team();
            team.setName(TeamOrganization.DEFAULT_TEAM_OF_ONE_TEAM_NAME);
            team.getTeamOrTeamMember().add(teamMember);
            generated.v10.dkf.TeamOrganization teamOrganization = new generated.v10.dkf.TeamOrganization();
            teamOrganization.setTeam(team);
            
            // save reference for later conversions
            newTeamOfOneMember = teamMember;
            
            newScenario.setTeamOrganization(teamOrganization);
        }

        // Resources
        generated.v10.dkf.Resources newResources = new generated.v10.dkf.Resources();
        newResources.setSurveyContext(prevScenario.getResources().getSurveyContext());

        generated.v10.dkf.AvailableLearnerActions newALA = new generated.v10.dkf.AvailableLearnerActions();

        if (prevScenario.getResources().getAvailableLearnerActions() != null) {

            generated.v9.dkf.AvailableLearnerActions ala = prevScenario.getResources().getAvailableLearnerActions();
            if (ala.getLearnerActionsFiles() != null) {
                generated.v10.dkf.LearnerActionsFiles newLAF = new generated.v10.dkf.LearnerActionsFiles();
                for (String filename : ala.getLearnerActionsFiles().getFile()) {
                    newLAF.getFile().add(filename);
                }

                newALA.setLearnerActionsFiles(newLAF);
            }

            if (ala.getLearnerActionsList() != null) {

                generated.v10.dkf.LearnerActionsList newLAL = new generated.v10.dkf.LearnerActionsList();
                for (generated.v9.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()) {

                    generated.v10.dkf.LearnerAction newAction = new generated.v10.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());

                    generated.v10.dkf.LearnerActionEnumType actionType;
                    try {
                        actionType = generated.v10.dkf.LearnerActionEnumType.fromValue(action.getType().value());
                    } catch (@SuppressWarnings("unused") Exception e) {
                        throw new UnsupportedOperationException("The learner action type '" + action.getType()
                                + "' is unknown. Since this is a required field, the import can not continue.");
                    }
                    newAction.setType(actionType);
                    newAction.setDescription(action.getDescription());

                    if (action.getLearnerActionParams() != null) {

                        generated.v9.dkf.TutorMeParams tutorMeParams = action.getLearnerActionParams();
                        generated.v10.dkf.TutorMeParams newTutorMeParams = new generated.v10.dkf.TutorMeParams();
                        if (tutorMeParams.getConfiguration() instanceof generated.v9.dkf.ConversationTreeFile) {

                            generated.v9.dkf.ConversationTreeFile convTreeFile = (generated.v9.dkf.ConversationTreeFile) tutorMeParams
                                    .getConfiguration();
                            generated.v10.dkf.ConversationTreeFile newConvTreeFile = new generated.v10.dkf.ConversationTreeFile();
                            newConvTreeFile.setName(convTreeFile.getName());

                            newTutorMeParams.setConfiguration(newConvTreeFile);

                        } else if (tutorMeParams.getConfiguration() instanceof generated.v9.dkf.AutoTutorSKO) {

                            generated.v9.dkf.AutoTutorSKO atSKO = (generated.v9.dkf.AutoTutorSKO) tutorMeParams
                                    .getConfiguration();
                            generated.v10.dkf.AutoTutorSKO newATSKO = convertAutoTutorSKO(atSKO);

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
            generated.v9.dkf.ScenarioControls scenarioControls = prevScenario.getResources().getScenarioControls();
            generated.v10.dkf.ScenarioControls newScenarioControls = new generated.v10.dkf.ScenarioControls();

            if (scenarioControls.getPreventManualStop() != null) {
                newScenarioControls.setPreventManualStop(new generated.v10.dkf.PreventManualStop());
            }

            newResources.setScenarioControls(newScenarioControls);
        }


        newScenario.setResources(newResources);

        // End Triggers
        generated.v10.dkf.Scenario.EndTriggers newScenarioEndTriggers = new generated.v10.dkf.Scenario.EndTriggers();

        if (prevScenario.getEndTriggers() != null) {
            newScenarioEndTriggers.getTrigger()
                    .addAll(convertScenarioEndTriggers(prevScenario.getEndTriggers().getTrigger()));
            newScenario.setEndTriggers(newScenarioEndTriggers);
        }

        // Assessment
        generated.v10.dkf.Assessment newAssessment = new generated.v10.dkf.Assessment();
        if (prevScenario.getAssessment() != null) {

            generated.v9.dkf.Assessment assessment = prevScenario.getAssessment();

            // Objects
            generated.v10.dkf.Objects newObjects = new generated.v10.dkf.Objects();
            if (assessment.getObjects() != null) {

                if (assessment.getObjects().getPlacesOfInterest() != null) {

                    generated.v10.dkf.PlacesOfInterest newPlacesOfInterest = new generated.v10.dkf.PlacesOfInterest();
                    generated.v9.dkf.PlacesOfInterest placesOfInterest = assessment.getObjects().getPlacesOfInterest();

                    for (Object pointOrPathOrArea : placesOfInterest.getPointOrPathOrArea())
                    {
                        if (pointOrPathOrArea instanceof generated.v9.dkf.Point) {
                            generated.v9.dkf.Point point = (generated.v9.dkf.Point) pointOrPathOrArea;

                            generated.v10.dkf.Point newPoint = new generated.v10.dkf.Point();
                            newPoint.setName(point.getName());
                            newPoint.setCoordinate(convertCoordinate(point.getCoordinate()));
                            newPoint.setColorHexRGBA(point.getColorHexRGBA());

                            newPlacesOfInterest.getPointOrPathOrArea().add(newPoint);
                        } else if (pointOrPathOrArea instanceof generated.v9.dkf.Path) {
                            generated.v9.dkf.Path path = (generated.v9.dkf.Path) pointOrPathOrArea;
                            
                            generated.v10.dkf.Path newPath = new generated.v10.dkf.Path();
                            newPath.setName(path.getName());
                            newPath.setColorHexRGBA(path.getColorHexRGBA());
                            for (generated.v9.dkf.Segment segment : path.getSegment()) {
                                generated.v10.dkf.Segment newSegment = new generated.v10.dkf.Segment();
                                newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
                                newSegment.setName(segment.getName());
                                newSegment.setWidth(segment.getWidth());

                                generated.v10.dkf.Segment.Start start = new generated.v10.dkf.Segment.Start();
                                start.setCoordinate(convertCoordinate(segment.getStart().getCoordinate()));
                                newSegment.setStart(start);
                                generated.v10.dkf.Segment.End end = new generated.v10.dkf.Segment.End();
                                end.setCoordinate(convertCoordinate(segment.getEnd().getCoordinate()));
                                newSegment.setEnd(end);
                                newPath.getSegment().add(newSegment);
                            }

                            newPlacesOfInterest.getPointOrPathOrArea().add(newPath);
                        } else if (pointOrPathOrArea instanceof generated.v9.dkf.Area) {
                            generated.v9.dkf.Area area = (generated.v9.dkf.Area) pointOrPathOrArea;
                            generated.v10.dkf.Area newArea = new generated.v10.dkf.Area();
                            newArea.setName(area.getName());
                            newArea.setColorHexRGBA(area.getColorHexRGBA());

                            for (generated.v9.dkf.Coordinate coordinate : area.getCoordinate()) {
                                newArea.getCoordinate().add(convertCoordinate(coordinate));
                            }
                            
                            newPlacesOfInterest.getPointOrPathOrArea().add(newArea);
                        } else {
                            throw new IllegalArgumentException("Found unhandled place of interest type of " + pointOrPathOrArea);
                        }
                    }

                    newObjects.setPlacesOfInterest(newPlacesOfInterest);
                }
            }
            newAssessment.setObjects(newObjects);

            // Tasks
            generated.v10.dkf.Tasks newTasks = new generated.v10.dkf.Tasks();
            if (assessment.getTasks() != null) {

                for (generated.v9.dkf.Task task : assessment.getTasks().getTask()) {

                    generated.v10.dkf.Task newTask = new generated.v10.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());

                    // Start Triggers
                    if (task.getStartTriggers() != null) {
                        generated.v10.dkf.StartTriggers newStartTriggers = new generated.v10.dkf.StartTriggers();
                        newStartTriggers.getTrigger()
                                .addAll(convertStartTriggers(task.getStartTriggers().getTrigger()));
                        newTask.setStartTriggers(newStartTriggers);
                    }

                    // End Triggers
                    generated.v10.dkf.EndTriggers newEndTriggers = new generated.v10.dkf.EndTriggers();
                    newEndTriggers.getTrigger().addAll(convertEndTriggers(task.getEndTriggers().getTrigger()));
                    newTask.setEndTriggers(newEndTriggers);

                    // Concepts
                    newTask.setConcepts(convertConcepts(task.getConcepts(), newObjects));

                    // Assessments
                    if (task.getAssessments() != null) {
                        newTask.setAssessments(convertAssessments(task.getAssessments()));
                    }

                    // Competence Metric
                    if (task.getCompetenceMetric() != null) {
                        generated.v10.dkf.CompetenceMetric newCompetenceMetric = new generated.v10.dkf.CompetenceMetric();
                        newCompetenceMetric.setCompetenceMetricImpl(task.getCompetenceMetric().getCompetenceMetricImpl());
                        newTask.setCompetenceMetric(newCompetenceMetric);
                    }
                    
                    // Performance Metric
                    if (task.getPerformanceMetric() != null) {
                        generated.v10.dkf.PerformanceMetric newPerformanceMetric = new generated.v10.dkf.PerformanceMetric();
                        newPerformanceMetric.setPerformanceMetricImpl(task.getPerformanceMetric().getPerformanceMetricImpl());
                        newTask.setPerformanceMetric(newPerformanceMetric);
                    }
                    
                    // Confidence Metric
                    if (task.getConfidenceMetric() != null) {
                        generated.v10.dkf.ConfidenceMetric newConfidenceMetric = new generated.v10.dkf.ConfidenceMetric();
                        newConfidenceMetric.setConfidenceMetricImpl(task.getConfidenceMetric().getConfidenceMetricImpl());
                        newTask.setConfidenceMetric(newConfidenceMetric);
                    }
                    
                    // Trend Metric
                    if (task.getTrendMetric() != null) {
                        generated.v10.dkf.TrendMetric newTrendMetric = new generated.v10.dkf.TrendMetric();
                        newTrendMetric.setTrendMetricImpl(task.getTrendMetric().getTrendMetricImpl());
                        newTask.setTrendMetric(newTrendMetric);
                    }
                    
                    // Priority Metric
                    if (task.getPriorityMetric() != null) {
                        generated.v10.dkf.PriorityMetric newPriorityMetric = new generated.v10.dkf.PriorityMetric();
                        newPriorityMetric.setPriorityMetricImpl(task.getPriorityMetric().getPriorityMetricImpl());
                        newTask.setPriorityMetric(newPriorityMetric);
                    }

                    newTasks.getTask().add(newTask);

                }

            }

            newAssessment.setTasks(newTasks);

        }

        newScenario.setAssessment(newAssessment);
        
        // Actions
        if (prevScenario.getActions() != null) {
            generated.v9.dkf.Actions actions = prevScenario.getActions();
            generated.v10.dkf.Actions newActions = new generated.v10.dkf.Actions();
            
            if (actions.getInstructionalStrategies() != null) {
                generated.v9.dkf.Actions.InstructionalStrategies instructionalStrategies = actions.getInstructionalStrategies();
                generated.v10.dkf.Actions.InstructionalStrategies newInstructionalStrategies = new generated.v10.dkf.Actions.InstructionalStrategies();

                for(generated.v9.dkf.Strategy strategy : instructionalStrategies.getStrategy()) {
                    newInstructionalStrategies.getStrategy().add(convertStrategy(strategy));
                }
                
                newActions.setInstructionalStrategies(newInstructionalStrategies);
            }
            
            if (actions.getStateTransitions() != null) {
                generated.v9.dkf.Actions.StateTransitions stateTransitions = actions.getStateTransitions();
                generated.v10.dkf.Actions.StateTransitions newStateTransitions = new generated.v10.dkf.Actions.StateTransitions();

                for (generated.v9.dkf.Actions.StateTransitions.StateTransition stateTransition : stateTransitions.getStateTransition()) {
                    generated.v10.dkf.Actions.StateTransitions.StateTransition newStateTransition = new generated.v10.dkf.Actions.StateTransitions.StateTransition();
                    newStateTransition.setName(stateTransition.getName());

                    generated.v9.dkf.Actions.StateTransitions.StateTransition.LogicalExpression logicalExpression = stateTransition.getLogicalExpression();
                    generated.v10.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v10.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();

                    for (Object stateType : logicalExpression.getStateType()) {
                        if (stateType instanceof generated.v9.dkf.LearnerStateTransitionEnum) {
                            generated.v9.dkf.LearnerStateTransitionEnum learnerStateTransitionEnum = (generated.v9.dkf.LearnerStateTransitionEnum) stateType;
                            generated.v10.dkf.LearnerStateTransitionEnum newLearnerStateTransitionEnum = new generated.v10.dkf.LearnerStateTransitionEnum();

                            newLearnerStateTransitionEnum.setAttribute(learnerStateTransitionEnum.getAttribute());
                            newLearnerStateTransitionEnum.setCurrent(learnerStateTransitionEnum.getCurrent());
                            newLearnerStateTransitionEnum.setPrevious(learnerStateTransitionEnum.getPrevious());

                            newLogicalExpression.getStateType().add(newLearnerStateTransitionEnum);
                        } else if (stateType instanceof generated.v9.dkf.PerformanceNode) {
                            generated.v9.dkf.PerformanceNode performanceNode = (generated.v9.dkf.PerformanceNode) stateType;
                            generated.v10.dkf.PerformanceNode newPerformanceNode = new generated.v10.dkf.PerformanceNode();

                            newPerformanceNode.setCurrent(performanceNode.getCurrent());
                            newPerformanceNode.setName(performanceNode.getName());
                            newPerformanceNode.setNodeId(performanceNode.getNodeId());
                            newPerformanceNode.setPrevious(performanceNode.getPrevious());
                            
                            newLogicalExpression.getStateType().add(newPerformanceNode);
                        } else {
                            throw new IllegalArgumentException("Found unhandled action's state transition state type of " + stateType);
                        }
                    }

                    newStateTransition.setLogicalExpression(newLogicalExpression);
                    
                    generated.v9.dkf.Actions.StateTransitions.StateTransition.StrategyChoices strategyChoices = stateTransition.getStrategyChoices();
                    generated.v10.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v10.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    
                    for (generated.v9.dkf.StrategyRef strategyRef : strategyChoices.getStrategies()) {
                        generated.v10.dkf.StrategyRef newRef = new generated.v10.dkf.StrategyRef();
                        newRef.setName(strategyRef.getName());
                        newStrategyChoices.getStrategies().add(newRef);
                    }
                    
                    newStateTransition.setStrategyChoices(newStrategyChoices);

                    newStateTransitions.getStateTransition().add(newStateTransition);
                }
            
                newActions.setStateTransitions(newStateTransitions);
            }

            newScenario.setActions(newActions);
        }

        return super.convertScenario(newScenario, showCompletionDialog);
    }

    /**
     * Converts a performance assessment
     * 
     * @param perfAssessment the performance assessment to convert
     * @return the next version of the performance assessment
     */
    private generated.v10.dkf.PerformanceAssessment convertPerformanceAssessment(
            generated.v9.dkf.PerformanceAssessment perfAssessment) {
        generated.v10.dkf.PerformanceAssessment newPerfAssessment = new generated.v10.dkf.PerformanceAssessment();

        if (perfAssessment.getAssessmentType() instanceof generated.v9.dkf.PerformanceAssessment.PerformanceNode) {
            generated.v10.dkf.PerformanceAssessment.PerformanceNode newPerfAssNode = new generated.v10.dkf.PerformanceAssessment.PerformanceNode();

            newPerfAssNode.setNodeId(
                    ((generated.v9.dkf.PerformanceAssessment.PerformanceNode) perfAssessment.getAssessmentType())
                            .getNodeId());
            newPerfAssessment.setAssessmentType(newPerfAssNode);
        } else {
            generated.v9.dkf.Conversation prevConv = (generated.v9.dkf.Conversation) perfAssessment.getAssessmentType();

            generated.v10.dkf.Conversation newConv = new generated.v10.dkf.Conversation();
            if (prevConv.getType() instanceof generated.v9.dkf.ConversationTreeFile) {

                generated.v10.dkf.ConversationTreeFile newTreeFile = new generated.v10.dkf.ConversationTreeFile();
                newTreeFile.setName(((generated.v9.dkf.ConversationTreeFile) prevConv.getType()).getName());

                newConv.setType(newTreeFile);
            } else {

                generated.v9.dkf.AutoTutorSKO prevATSKO = (generated.v9.dkf.AutoTutorSKO) prevConv.getType();

                generated.v10.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevATSKO);

                newConv.setType(newAutoTutorSKO);
            }

            newPerfAssessment.setAssessmentType(newConv);
        }

        newPerfAssessment.setStrategyHandler(convertStrategyHandler(perfAssessment.getStrategyHandler()));
        if (perfAssessment.getDelayAfterStrategy() != null) {
            generated.v10.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.v10.dkf.DelayAfterStrategy();
            newDelayAfterStrategy.setDuration(perfAssessment.getDelayAfterStrategy().getDuration());
            newPerfAssessment.setDelayAfterStrategy(newDelayAfterStrategy);
        }
        return newPerfAssessment;
    }

    /**
     * Converts an environment adaptation
     * 
     * @param envAdaptation the environment adaptation to convert
     * @return the next version of the environment adaptation. Can return null if the adaptation is no longer supported.
     */
    private generated.v10.dkf.EnvironmentAdaptation convertEnvironmentAdaptation(
            generated.v9.dkf.EnvironmentAdaptation envAdaptation) {
        generated.v10.dkf.EnvironmentAdaptation newEnvAdapt = new generated.v10.dkf.EnvironmentAdaptation();

        Object type = envAdaptation.getType();

        if(type instanceof generated.v9.dkf.EnvironmentAdaptation.Overcast){
            generated.v9.dkf.EnvironmentAdaptation.Overcast overcast = (generated.v9.dkf.EnvironmentAdaptation.Overcast) type;
            generated.v10.dkf.EnvironmentAdaptation.Overcast newOvercast = new generated.v10.dkf.EnvironmentAdaptation.Overcast();
            newOvercast.setValue(overcast.getValue());
            newEnvAdapt.setType(newOvercast);
            
        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.Fog){
            
            generated.v9.dkf.EnvironmentAdaptation.Fog fog = (generated.v9.dkf.EnvironmentAdaptation.Fog) type;
            generated.v10.dkf.EnvironmentAdaptation.Fog newFog = new generated.v10.dkf.EnvironmentAdaptation.Fog();
            newFog.setDensity(fog.getDensity());
            if (fog.getColor() != null) {
                generated.v10.dkf.EnvironmentAdaptation.Fog.Color newFogColor = new generated.v10.dkf.EnvironmentAdaptation.Fog.Color();
                newFogColor.setRed(fog.getColor().getRed());
                newFogColor.setGreen(fog.getColor().getGreen());
                newFogColor.setBlue(fog.getColor().getBlue());
                newFog.setColor(newFogColor);
            }
            newEnvAdapt.setType(newFog);
            
        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.Rain){
            
            generated.v9.dkf.EnvironmentAdaptation.Rain rain = (generated.v9.dkf.EnvironmentAdaptation.Rain) type;
            generated.v10.dkf.EnvironmentAdaptation.Rain newRain = new generated.v10.dkf.EnvironmentAdaptation.Rain();
            newRain.setValue(rain.getValue());
            if (rain.getScenarioAdaptationDuration() != null) {
                newRain.setScenarioAdaptationDuration(rain.getScenarioAdaptationDuration());
            }
            newEnvAdapt.setType(newRain);
            
        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.TimeOfDay){
            
            generated.v9.dkf.EnvironmentAdaptation.TimeOfDay tod = (generated.v9.dkf.EnvironmentAdaptation.TimeOfDay) type;
            generated.v10.dkf.EnvironmentAdaptation.TimeOfDay newTod = new generated.v10.dkf.EnvironmentAdaptation.TimeOfDay();
            if (tod.getType() instanceof generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Dawn) {
                newTod.setType(new generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Dawn());  
                newEnvAdapt.setType(newTod);
            } else if (tod.getType() instanceof generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Dusk) {
                newTod.setType(new generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Dusk());  
                newEnvAdapt.setType(newTod);
            } else if (tod.getType() instanceof generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Midday) {
                newTod.setType(new generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Midday());  
                newEnvAdapt.setType(newTod);
            } else if (tod.getType() instanceof generated.v9.dkf.EnvironmentAdaptation.TimeOfDay.Midnight) {
                newTod.setType(new generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Midnight());  
                newEnvAdapt.setType(newTod);
            } else {
                throw new IllegalArgumentException("Found unhandled Time of Day type of '" + type + "'.");
            }
            
        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.CreateActors){

            generated.v9.dkf.EnvironmentAdaptation.CreateActors createActors = (generated.v9.dkf.EnvironmentAdaptation.CreateActors) type;
            generated.v10.dkf.EnvironmentAdaptation.CreateActors newCreateActors = new generated.v10.dkf.EnvironmentAdaptation.CreateActors();
            newCreateActors.setCoordinate(convertCoordinate(createActors.getCoordinate()));
            newCreateActors.setType(createActors.getType());
            if (createActors.getSide().getType() instanceof generated.v9.dkf.EnvironmentAdaptation.CreateActors.Side.Civilian) {
                generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side newSide = new generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side();
                newSide.setType(new generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side.Civilian());
                newCreateActors.setSide(newSide);
            } else if (createActors.getSide().getType() instanceof generated.v9.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor) {
                generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side newSide = new generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side();
                newSide.setType(new generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor());
                newCreateActors.setSide(newSide);
            } else if (createActors.getSide().getType() instanceof generated.v9.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor) {
                generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side newSide = new generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side();
                newSide.setType(new generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor());
                newCreateActors.setSide(newSide);
            } else {
                throw new IllegalArgumentException("Found unhandled Create Actor Side type of '" + createActors.getSide().getType() + "'.");
            }
            newEnvAdapt.setType(newCreateActors);

        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.RemoveActors){

            generated.v9.dkf.EnvironmentAdaptation.RemoveActors removeActors = (generated.v9.dkf.EnvironmentAdaptation.RemoveActors) type;
            generated.v10.dkf.EnvironmentAdaptation.RemoveActors newRemoveActors = new generated.v10.dkf.EnvironmentAdaptation.RemoveActors();
            for (Object actorType : removeActors.getType()){
                if (actorType instanceof String) {
                    newRemoveActors.getType().add((String) actorType);
                } else if (actorType instanceof generated.v9.dkf.EnvironmentAdaptation.RemoveActors.Location) {
                    generated.v9.dkf.EnvironmentAdaptation.RemoveActors.Location actorLocation = (generated.v9.dkf.EnvironmentAdaptation.RemoveActors.Location) actorType;
                    generated.v10.dkf.EnvironmentAdaptation.RemoveActors.Location newActorLocation = new generated.v10.dkf.EnvironmentAdaptation.RemoveActors.Location();
                    newActorLocation.setCoordinate(convertCoordinate(actorLocation.getCoordinate()));
                    newRemoveActors.getType().add(newActorLocation);
                } else {
                    throw new IllegalArgumentException("Found unhandled Remove Actor type of '" + actorType + "'.");
                }
            }
            
            newEnvAdapt.setType(newRemoveActors);

        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.TeleportLearner){
            generated.v9.dkf.EnvironmentAdaptation.TeleportLearner teleportLearner = (generated.v9.dkf.EnvironmentAdaptation.TeleportLearner) type;
            generated.v10.dkf.EnvironmentAdaptation.Teleport newTeleport = new generated.v10.dkf.EnvironmentAdaptation.Teleport();
            newTeleport.setCoordinate(convertCoordinate(teleportLearner.getCoordinate()));
            if (teleportLearner.getHeading() != null) {
                generated.v10.dkf.EnvironmentAdaptation.Teleport.Heading newHeading = new generated.v10.dkf.EnvironmentAdaptation.Teleport.Heading();
                newHeading.setValue(teleportLearner.getHeading().getValue());
                newTeleport.setHeading(newHeading);
            }
            
            newEnvAdapt.setType(newTeleport);
            
        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.FatigueRecovery){
            generated.v9.dkf.EnvironmentAdaptation.FatigueRecovery fatigueRecovery = (generated.v9.dkf.EnvironmentAdaptation.FatigueRecovery) type;
            generated.v10.dkf.EnvironmentAdaptation.FatigueRecovery newFatigueRecovery = new generated.v10.dkf.EnvironmentAdaptation.FatigueRecovery();
            newFatigueRecovery.setRate(fatigueRecovery.getRate());
            generated.v10.dkf.EnvironmentAdaptation.FatigueRecovery.TeamMemberRef newTeamMemberRef = new generated.v10.dkf.EnvironmentAdaptation.FatigueRecovery.TeamMemberRef();
            if (newTeamOfOneMember != null) {
                newTeamMemberRef.setValue(newTeamOfOneMember.getName());
            } else {
                throw new IllegalArgumentException("Found invalid schema; Fatigue Recovery environment adaptation cannot exist without a learnerId.");
            }
            newTeamMemberRef.setEntityMarking("New Entity Marker");
            newFatigueRecovery.setTeamMemberRef(newTeamMemberRef);
            
            newEnvAdapt.setType(newFatigueRecovery);

        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.Endurance){
            generated.v9.dkf.EnvironmentAdaptation.Endurance endurance = (generated.v9.dkf.EnvironmentAdaptation.Endurance) type;
            generated.v10.dkf.EnvironmentAdaptation.Endurance newEndurance = new generated.v10.dkf.EnvironmentAdaptation.Endurance();
            newEndurance.setValue(endurance.getValue());
            generated.v10.dkf.EnvironmentAdaptation.Endurance.TeamMemberRef newTeamMemberRef = new generated.v10.dkf.EnvironmentAdaptation.Endurance.TeamMemberRef();
            if (newTeamOfOneMember != null) {
                newTeamMemberRef.setValue(newTeamOfOneMember.getName());
            } else {
                throw new IllegalArgumentException("Found invalid schema; Endurance environment adaptation cannot exist without a learnerId.");
            }
            newTeamMemberRef.setEntityMarking("New Entity Marker");
            newEndurance.setTeamMemberRef(newTeamMemberRef);

            newEnvAdapt.setType(newEndurance);

        } else if(type instanceof generated.v9.dkf.EnvironmentAdaptation.Script){
            generated.v9.dkf.EnvironmentAdaptation.Script script = (generated.v9.dkf.EnvironmentAdaptation.Script) type;
            generated.v10.dkf.EnvironmentAdaptation.Script newScript = new generated.v10.dkf.EnvironmentAdaptation.Script();
            newScript.setValue(script.getValue());

            newEnvAdapt.setType(newScript);

        }

        return newEnvAdapt.getType() != null ? newEnvAdapt : null;
    }

    /**
     * Converts a feedback
     * 
     * @param feedback the feedback to convert
     * @return the next version of the feedback
     */
    private generated.v10.dkf.Feedback convertFeedback(generated.v9.dkf.Feedback feedback) {
        generated.v10.dkf.Feedback newFeedback = new generated.v10.dkf.Feedback();

        if (feedback.getFeedbackPresentation() instanceof generated.v9.dkf.Message) {

            generated.v9.dkf.Message message = (generated.v9.dkf.Message) feedback.getFeedbackPresentation();
            generated.v10.dkf.Message feedbackMsg = convertMessage(message);

            newFeedback.setFeedbackPresentation(feedbackMsg);
        } else if (feedback.getFeedbackPresentation() instanceof generated.v9.dkf.Audio) {

            generated.v9.dkf.Audio audio = (generated.v9.dkf.Audio) feedback.getFeedbackPresentation();

            generated.v10.dkf.Audio newAudio = new generated.v10.dkf.Audio();

            // An audio object requires a .mp3 file but does not require a .ogg file
            newAudio.setMP3File(audio.getMP3File());

            if (audio.getOGGFile() != null) {
                newAudio.setOGGFile(audio.getOGGFile());
            }

            newFeedback.setFeedbackPresentation(newAudio);
        } else if (feedback.getFeedbackPresentation() instanceof generated.v9.dkf.MediaSemantics) {

            generated.v9.dkf.MediaSemantics semantics = (generated.v9.dkf.MediaSemantics) feedback
                    .getFeedbackPresentation();

            generated.v10.dkf.MediaSemantics newSemantics = new generated.v10.dkf.MediaSemantics();

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
        newFeedback.setFeedbackDuration(feedback.getFeedbackDuration());
        return newFeedback;
    }

    /**
     * Converts a mid lesson media
     * 
     * @param midLessonMedia the mid lesson media to convert
     * @return the next version of the mid lesson media
     */
    private generated.v10.dkf.MidLessonMedia convertMidLessonMedia(generated.v9.dkf.MidLessonMedia midLessonMedia) {
        generated.v10.dkf.MidLessonMedia newMidLessonMedia = new generated.v10.dkf.MidLessonMedia();

        newMidLessonMedia.setStrategyHandler(convertStrategyHandler(midLessonMedia.getStrategyHandler()));
        if (midLessonMedia.getDelayAfterStrategy() != null) {
            generated.v10.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.v10.dkf.DelayAfterStrategy();
            newDelayAfterStrategy.setDuration(midLessonMedia.getDelayAfterStrategy().getDuration());
            newMidLessonMedia.setDelayAfterStrategy(newDelayAfterStrategy);
        }

        final generated.v9.dkf.LessonMaterialList lessonMaterialList = midLessonMedia.getLessonMaterialList();
        if (lessonMaterialList == null) {
            // gone as far as we can
            return newMidLessonMedia;
        }

        generated.v10.dkf.LessonMaterialList newLessonMaterialList = new generated.v10.dkf.LessonMaterialList();

        generated.v9.dkf.LessonMaterialList.Assessment assessment = lessonMaterialList.getAssessment();
        if (assessment != null) {
            generated.v10.dkf.LessonMaterialList.Assessment newAssessment = new generated.v10.dkf.LessonMaterialList.Assessment();

            final generated.v9.dkf.OverDwell overDwell = assessment.getOverDwell();
            if (overDwell != null) {
                generated.v10.dkf.OverDwell newOverDwell = new generated.v10.dkf.OverDwell();
                newOverDwell.setFeedback(overDwell.getFeedback());

                final generated.v9.dkf.OverDwell.Duration duration = overDwell.getDuration();
                if (duration != null) {
                    generated.v10.dkf.OverDwell.Duration newDuration = new generated.v10.dkf.OverDwell.Duration();

                    final Serializable durationType = duration.getType();
                    if (durationType instanceof BigInteger) {
                        BigInteger durationTime = (BigInteger) durationType;
                        newDuration.setType(durationTime);
                    } else if (durationType instanceof generated.v9.dkf.OverDwell.Duration.DurationPercent) {
                        generated.v9.dkf.OverDwell.Duration.DurationPercent durationPercent = (generated.v9.dkf.OverDwell.Duration.DurationPercent) durationType;
                        generated.v10.dkf.OverDwell.Duration.DurationPercent newDurationPercent = new generated.v10.dkf.OverDwell.Duration.DurationPercent();
                        newDurationPercent.setPercent(durationPercent.getPercent());
                        newDurationPercent.setTime(durationPercent.getTime());

                        newDuration.setType(newDurationPercent);
                    }

                    newOverDwell.setDuration(newDuration);
                }

                newAssessment.setOverDwell(newOverDwell);
            }

            final generated.v9.dkf.LessonMaterialList.Assessment.UnderDwell underDwell = assessment.getUnderDwell();
            if (underDwell != null) {
                generated.v10.dkf.LessonMaterialList.Assessment.UnderDwell newUnderDwell = new generated.v10.dkf.LessonMaterialList.Assessment.UnderDwell();
                newUnderDwell.setFeedback(underDwell.getFeedback());
                newUnderDwell.setDuration(underDwell.getDuration());

                newAssessment.setUnderDwell(newUnderDwell);
            }

            newLessonMaterialList.setAssessment(newAssessment);
        }

        newLessonMaterialList.setIsCollection(convertBooleanEnum(lessonMaterialList.getIsCollection()));

        for (generated.v9.dkf.Media media : lessonMaterialList.getMedia()) {
            generated.v10.dkf.Media newMedia = new generated.v10.dkf.Media();

            newMedia.setName(media.getName());
            newMedia.setMessage(media.getMessage());
            newMedia.setUri(media.getUri());

            Serializable mediaProperties = media.getMediaTypeProperties();
            if (mediaProperties instanceof generated.v9.dkf.PDFProperties) {
                newMedia.setMediaTypeProperties(new generated.v10.dkf.PDFProperties());

            } else if (mediaProperties instanceof generated.v9.dkf.WebpageProperties) {
                newMedia.setMediaTypeProperties(new generated.v10.dkf.WebpageProperties());

            } else if (mediaProperties instanceof generated.v9.dkf.YoutubeVideoProperties) {
                generated.v9.dkf.YoutubeVideoProperties properties = (generated.v9.dkf.YoutubeVideoProperties) mediaProperties;
                newMedia.setMediaTypeProperties(convertYoutubeVideoProperties(properties));

            } else if (mediaProperties instanceof generated.v9.dkf.ImageProperties) {
                newMedia.setMediaTypeProperties(new generated.v10.dkf.ImageProperties());

            } else if (mediaProperties instanceof generated.v9.dkf.SlideShowProperties) {
                generated.v9.dkf.SlideShowProperties properties = (generated.v9.dkf.SlideShowProperties) mediaProperties;
                generated.v10.dkf.SlideShowProperties newProperties = new generated.v10.dkf.SlideShowProperties();
                newProperties
                        .setDisplayPreviousSlideButton(convertBooleanEnum(properties.getDisplayPreviousSlideButton()));
                newProperties.setKeepContinueButton(convertBooleanEnum(properties.getKeepContinueButton()));
                newProperties.getSlideRelativePath().addAll(properties.getSlideRelativePath());
                newMedia.setMediaTypeProperties(newProperties);

            } else if (mediaProperties instanceof generated.v9.dkf.LtiProperties) {
                generated.v9.dkf.LtiProperties properties = (generated.v9.dkf.LtiProperties) mediaProperties;
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
    private generated.v10.dkf.YoutubeVideoProperties convertYoutubeVideoProperties(
            generated.v9.dkf.YoutubeVideoProperties properties) {
        generated.v10.dkf.YoutubeVideoProperties newProperties = new generated.v10.dkf.YoutubeVideoProperties();

        newProperties.setAllowAutoPlay(convertBooleanEnum(properties.getAllowAutoPlay()));

        newProperties.setAllowFullScreen(convertBooleanEnum(properties.getAllowFullScreen()));

        final generated.v9.dkf.Size size = properties.getSize();
        if (size != null) {
            generated.v10.dkf.Size newSize = new generated.v10.dkf.Size();
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
    private generated.v10.dkf.LtiProperties convertLtiProperties(generated.v9.dkf.LtiProperties properties) {
        generated.v10.dkf.LtiProperties newProperties = new generated.v10.dkf.LtiProperties();

        newProperties.setAllowScore(convertBooleanEnum(properties.getAllowScore()));
        newProperties.setIsKnowledge(convertBooleanEnum(properties.getIsKnowledge()));
        newProperties.setSliderMaxValue(properties.getSliderMaxValue());
        newProperties.setSliderMinValue(properties.getSliderMinValue());
        newProperties.setLtiIdentifier(properties.getLtiIdentifier());

        if (properties.getDisplayMode() != null) {
            newProperties.setDisplayMode(generated.v10.dkf.DisplayModeEnum.fromValue(properties.getDisplayMode().value()));
        }

        if (properties.getCustomParameters() != null) {
            generated.v10.dkf.CustomParameters newCustomParameters = new generated.v10.dkf.CustomParameters();
            for (generated.v9.dkf.Nvpair nvPair : properties.getCustomParameters().getNvpair()) {
                newCustomParameters.getNvpair().add(convertNvpair(nvPair));
            }
            newProperties.setCustomParameters(newCustomParameters);
        }

        if (properties.getLtiConcepts() != null) {
            generated.v10.dkf.LtiConcepts newLtiConcepts = new generated.v10.dkf.LtiConcepts();
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
    private generated.v10.dkf.Strategy convertStrategy(generated.v9.dkf.Strategy oldStrategy) {
        generated.v10.dkf.Strategy newStrategy = new generated.v10.dkf.Strategy();
        newStrategy.setName(oldStrategy.getName());
        
        for (Object strategyActivity : oldStrategy.getStrategyActivities()) {
            if (strategyActivity instanceof generated.v9.dkf.InstructionalIntervention) {

                generated.v9.dkf.InstructionalIntervention iIntervention = (generated.v9.dkf.InstructionalIntervention) strategyActivity;
                generated.v10.dkf.InstructionalIntervention newIIntervention = new generated.v10.dkf.InstructionalIntervention();

                if (iIntervention.getDelayAfterStrategy() != null) {
                    generated.v10.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.v10.dkf.DelayAfterStrategy();
                    newDelayAfterStrategy.setDuration(iIntervention.getDelayAfterStrategy().getDuration());
                    newIIntervention.setDelayAfterStrategy(newDelayAfterStrategy);
                }
                newIIntervention.setFeedback(convertFeedback(iIntervention.getFeedback()));
                newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));
                
                newStrategy.getStrategyActivities().add(newIIntervention);

            } else if (strategyActivity instanceof generated.v9.dkf.MidLessonMedia) {

                newStrategy.getStrategyActivities().add(convertMidLessonMedia((generated.v9.dkf.MidLessonMedia) strategyActivity));

            } else if (strategyActivity instanceof generated.v9.dkf.PerformanceAssessment) {

                newStrategy.getStrategyActivities().add(convertPerformanceAssessment((generated.v9.dkf.PerformanceAssessment) strategyActivity));

            } else if (strategyActivity instanceof generated.v9.dkf.ScenarioAdaptation) {
                generated.v9.dkf.ScenarioAdaptation scenarioAdaptation = (generated.v9.dkf.ScenarioAdaptation) strategyActivity;
                generated.v10.dkf.ScenarioAdaptation newScenarioAdaptation = new generated.v10.dkf.ScenarioAdaptation();
                if (scenarioAdaptation.getDelayAfterStrategy() != null) {
                    generated.v9.dkf.DelayAfterStrategy delayAfterStrategy = scenarioAdaptation.getDelayAfterStrategy();
                    generated.v10.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.v10.dkf.DelayAfterStrategy();
                    newDelayAfterStrategy.setDuration(delayAfterStrategy.getDuration());
                    newScenarioAdaptation.setDelayAfterStrategy(newDelayAfterStrategy);
                }
                newScenarioAdaptation.setEnvironmentAdaptation(convertEnvironmentAdaptation(scenarioAdaptation.getEnvironmentAdaptation()));
                newScenarioAdaptation.setStrategyHandler(convertStrategyHandler(scenarioAdaptation.getStrategyHandler()));

                newStrategy.getStrategyActivities().add(newScenarioAdaptation);

            } else if (strategyActivity instanceof generated.v9.dkf.DoNothingInstStrategy) {
                generated.v9.dkf.DoNothingInstStrategy doNothingInstStrategy = (generated.v9.dkf.DoNothingInstStrategy) strategyActivity;
                generated.v10.dkf.DoNothingInstStrategy newDoNothingInstStrategy = new generated.v10.dkf.DoNothingInstStrategy();
                if (doNothingInstStrategy.getDelayAfterStrategy() != null) {
                    generated.v10.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.v10.dkf.DelayAfterStrategy();
                    newDelayAfterStrategy.setDuration(doNothingInstStrategy.getDelayAfterStrategy().getDuration());
                    newDoNothingInstStrategy.setDelayAfterStrategy(newDelayAfterStrategy);
                }
                if (doNothingInstStrategy.getStrategyHandler() != null) {
                    newDoNothingInstStrategy.setStrategyHandler(convertStrategyHandler(doNothingInstStrategy.getStrategyHandler()));
                }
            } else {
                throw new IllegalArgumentException("Found unhandled strategy activty type of " + strategyActivity);
            }
        }
        
        return newStrategy;
    }

    /**
     * Convert a previous AutoTutor SKO element to the newer schema version.
     *
     * @param prevATSKO the previous element to convert its content to the newer element. If null
     *        this returns null.
     * @return the new AutoTutor SKO element for the new schema version.
     */
    private generated.v10.dkf.AutoTutorSKO convertAutoTutorSKO(generated.v9.dkf.AutoTutorSKO prevATSKO) {

        if (prevATSKO == null) {
            return null;
        }

        generated.v10.dkf.AutoTutorSKO newAutoTutorSKO = new generated.v10.dkf.AutoTutorSKO();
        if (prevATSKO.getScript() instanceof generated.v9.dkf.LocalSKO) {

            generated.v9.dkf.LocalSKO localSKO = (generated.v9.dkf.LocalSKO) prevATSKO.getScript();
            generated.v10.dkf.LocalSKO newLocalSKO = new generated.v10.dkf.LocalSKO();

            newLocalSKO.setFile(localSKO.getFile());
            newAutoTutorSKO.setScript(newLocalSKO);

        } else if (prevATSKO.getScript() instanceof generated.v9.dkf.ATRemoteSKO) {
            generated.v9.dkf.ATRemoteSKO prevLocalSKO = (generated.v9.dkf.ATRemoteSKO) prevATSKO.getScript();

            generated.v10.dkf.ATRemoteSKO.URL newURL = new generated.v10.dkf.ATRemoteSKO.URL();
            newURL.setAddress(prevLocalSKO.getURL().getAddress());

            generated.v10.dkf.ATRemoteSKO newATRemoteSKO = new generated.v10.dkf.ATRemoteSKO();
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
    private generated.v10.dkf.Message convertMessage(generated.v9.dkf.Message message) {
        generated.v10.dkf.Message newMessage = new generated.v10.dkf.Message();
        newMessage.setContent(message.getContent());

        if (message.getDelivery() != null) {
            generated.v10.dkf.Message.Delivery newDelivery = new generated.v10.dkf.Message.Delivery();

            if (message.getDelivery().getInTrainingApplication() != null) {
                generated.v10.dkf.Message.Delivery.InTrainingApplication newInTrainingApp = new generated.v10.dkf.Message.Delivery.InTrainingApplication();
                newInTrainingApp.setEnabled(generated.v10.dkf.BooleanEnum.fromValue(
                        message.getDelivery().getInTrainingApplication().getEnabled().toString().toLowerCase()));
                if (message.getDelivery().getInTrainingApplication().getMobileOption() != null) {
                    generated.v9.dkf.Message.Delivery.InTrainingApplication.MobileOption mobileOption = message.getDelivery().getInTrainingApplication().getMobileOption();
                    generated.v10.dkf.Message.Delivery.InTrainingApplication.MobileOption newMobileOption = new generated.v10.dkf.Message.Delivery.InTrainingApplication.MobileOption();
                    newMobileOption.setVibrate(mobileOption.isVibrate());
                    newInTrainingApp.setMobileOption(newMobileOption);
                }
                newDelivery.setInTrainingApplication(newInTrainingApp);
            }
            if (message.getDelivery().getInTutor() != null) {
                generated.v10.dkf.InTutor newInTutor = new generated.v10.dkf.InTutor();
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
     * @return generated.v9.dkf.Entities - the new object
     * @throws IllegalArgumentException if the start location coordinate type is unknown
     */
    private generated.v10.dkf.Entities convertEntities(generated.v9.dkf.Entities entities)
            throws IllegalArgumentException {

        generated.v10.dkf.Entities newEntities = new generated.v10.dkf.Entities();
        for (generated.v9.dkf.StartLocation location : entities.getStartLocation()) {

            generated.v10.dkf.StartLocation newLocation = new generated.v10.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }

        return newEntities;
    }

    /**
     * Convert a checkpoint object into a new checkpoint object.
     *
     * @param checkpoint - the object to convert
     * @return generated.v9.dkf.Checkpoint - the new object
     */
    private generated.v10.dkf.Checkpoint convertCheckpoint(generated.v9.dkf.Checkpoint checkpoint) {

        generated.v10.dkf.Checkpoint newCheckpoint = new generated.v10.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setPoint(checkpoint.getPoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());

        return newCheckpoint;
    }

    /**
     * Convert an evaluators object into a new evaluators object.
     *
     * @param evaluators - the object to convert
     * @return the new object
     */
    private generated.v10.dkf.Evaluators convertEvaluators(generated.v9.dkf.Evaluators evaluators) {

        generated.v10.dkf.Evaluators newEvaluators = new generated.v10.dkf.Evaluators();
        for (generated.v9.dkf.Evaluator evaluator : evaluators.getEvaluator()) {

            generated.v10.dkf.Evaluator newEvaluator = new generated.v10.dkf.Evaluator();
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
    private List<generated.v10.dkf.HasMovedExcavatorComponentInput.Component> convertComponents(
            List<generated.v9.dkf.HasMovedExcavatorComponentInput.Component> oldCompList) {

        List<generated.v10.dkf.HasMovedExcavatorComponentInput.Component> componentList = new ArrayList<generated.v10.dkf.HasMovedExcavatorComponentInput.Component>();

        for (generated.v9.dkf.HasMovedExcavatorComponentInput.Component oldComp : oldCompList) {
            generated.v10.dkf.HasMovedExcavatorComponentInput.Component newComp = new generated.v10.dkf.HasMovedExcavatorComponentInput.Component();
            newComp.setComponentType(
                    generated.v10.dkf.ExcavatorComponentEnum.fromValue(oldComp.getComponentType().value()));

            if (oldComp
                    .getDirectionType() instanceof generated.v9.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional) {
                generated.v9.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional oldBiDirectional = (generated.v9.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional) oldComp
                        .getDirectionType();
                generated.v10.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional newBiDirectional = new generated.v10.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional();
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
    private generated.v10.dkf.Assessments convertAssessments(generated.v9.dkf.Assessments assessments) {

        generated.v10.dkf.Assessments newAssessments = new generated.v10.dkf.Assessments();

        List<generated.v9.dkf.Assessments.Survey> surveys = new ArrayList<generated.v9.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
            if (assessmentType instanceof generated.v9.dkf.Assessments.Survey) {
                surveys.add((generated.v9.dkf.Assessments.Survey) assessmentType);
            }
        }

        for (generated.v9.dkf.Assessments.Survey survey : surveys) {
            generated.v10.dkf.Assessments.Survey newSurvey = new generated.v10.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());

            final generated.v9.dkf.Questions oldQuestions = survey.getQuestions();
            if (oldQuestions != null && !oldQuestions.getQuestion().isEmpty()) {
                generated.v10.dkf.Questions newQuestions = new generated.v10.dkf.Questions();
                for (generated.v9.dkf.Question oldQuestion : oldQuestions.getQuestion()) {

                    generated.v10.dkf.Question newQuestion = new generated.v10.dkf.Question();
                    newQuestion.setKey(oldQuestion.getKey());

                    for (generated.v9.dkf.Reply oldReply : oldQuestion.getReply()) {

                        generated.v10.dkf.Reply newReply = new generated.v10.dkf.Reply();
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
     * @return List<generated.v10.dkf.StartTriggers.Trigger> - converted trigger objects (same size as
     *         triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private List<generated.v10.dkf.StartTriggers.Trigger> convertStartTriggers(
            List<generated.v9.dkf.StartTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.v10.dkf.StartTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v9.dkf.StartTriggers.Trigger triggerObj : list) {

            generated.v10.dkf.StartTriggers.Trigger trigger = new generated.v10.dkf.StartTriggers.Trigger();
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
     * @return List<generated.v10.dkf.EndTriggers.Trigger> - converted trigger objects (same size as
     *         triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private List<generated.v10.dkf.EndTriggers.Trigger> convertEndTriggers(
            List<generated.v9.dkf.EndTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.v10.dkf.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v9.dkf.EndTriggers.Trigger triggerObj : list) {

            generated.v10.dkf.EndTriggers.Trigger trigger = new generated.v10.dkf.EndTriggers.Trigger();
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
     * @return List<generated.v10.dkf.Scenario.EndTriggers.Trigger> - converted trigger objects (same
     *         size as triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private List<generated.v10.dkf.Scenario.EndTriggers.Trigger> convertScenarioEndTriggers(
            List<generated.v9.dkf.Scenario.EndTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.v10.dkf.Scenario.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        int i = 1;
        for (generated.v9.dkf.Scenario.EndTriggers.Trigger triggerObj : list) {

            generated.v10.dkf.Scenario.EndTriggers.Trigger trigger = new generated.v10.dkf.Scenario.EndTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj.getTriggerType()));
            trigger.setTriggerDelay(triggerObj.getTriggerDelay());
            
            if(StringUtils.isNotBlank(triggerObj.getMessage())){
                generated.v10.dkf.Strategy strategy = new generated.v10.dkf.Strategy();
                strategy.setName("scenario end trigger strategy "+i);
                i++;
                generated.v10.dkf.Feedback feedback = new generated.v10.dkf.Feedback();
                feedback.setAffectiveFeedbackType(triggerObj.getMessage());
                generated.v10.dkf.InstructionalIntervention instructionalIntervention = new generated.v10.dkf.InstructionalIntervention();
                instructionalIntervention.setFeedback(feedback);
                strategy.getStrategyActivities().add(instructionalIntervention);
                generated.v10.dkf.Scenario.EndTriggers.Trigger.Message message = new generated.v10.dkf.Scenario.EndTriggers.Trigger.Message();
                message.setStrategy(strategy);
                trigger.setMessage(message);
            }

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
    private Serializable convertTrigger(Serializable triggerObj) throws IllegalArgumentException {

        if (triggerObj instanceof generated.v9.dkf.EntityLocation) {

            generated.v9.dkf.EntityLocation entityLocation = (generated.v9.dkf.EntityLocation) triggerObj;
            generated.v10.dkf.EntityLocation newEntityLocation = new generated.v10.dkf.EntityLocation();

            generated.v10.dkf.StartLocation startLocation = new generated.v10.dkf.StartLocation();
            if (entityLocation.getStartLocation() != null) {
                startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
            }

            generated.v10.dkf.EntityLocation.EntityId entityId = new generated.v10.dkf.EntityLocation.EntityId();
            generated.v10.dkf.LearnerId learnerId = new generated.v10.dkf.LearnerId();
            learnerId.setType(startLocation);
            entityId.setTeamMemberRefOrLearnerId(learnerId);
            newEntityLocation.setEntityId(entityId);

            generated.v10.dkf.TriggerLocation triggerLocation = new generated.v10.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);

            return newEntityLocation;
        } else if (triggerObj instanceof generated.v9.dkf.LearnerLocation) {
            // convert to EntityLocation
            generated.v9.dkf.LearnerLocation learnerLocation = (generated.v9.dkf.LearnerLocation) triggerObj;
            generated.v10.dkf.EntityLocation newEntityLocation = new generated.v10.dkf.EntityLocation();

            generated.v10.dkf.EntityLocation.EntityId entityId = new generated.v10.dkf.EntityLocation.EntityId();
            generated.v10.dkf.LearnerId learnerId = new generated.v10.dkf.LearnerId();
            if (newTeamOfOneMember != null) {
                if (newTeamOfOneMember.getLearnerId().getType() instanceof generated.v10.dkf.StartLocation) {
                    generated.v10.dkf.StartLocation startLocation = (generated.v10.dkf.StartLocation) newTeamOfOneMember.getLearnerId().getType();
                    learnerId.setType(startLocation);
                } else {
                    throw new IllegalArgumentException("Found unhandled LearnerId type of " + newTeamOfOneMember.getLearnerId().getType());
                }
            } else {
                throw new IllegalArgumentException("Found invalid schema; LearnerLocation trigger cannot exist without a learnerId.");
            }
            
            entityId.setTeamMemberRefOrLearnerId(learnerId);
            newEntityLocation.setEntityId(entityId);

            generated.v10.dkf.TriggerLocation triggerLocation = new generated.v10.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);

            return newEntityLocation;
        } else if (triggerObj instanceof generated.v9.dkf.ConceptEnded) {

            generated.v9.dkf.ConceptEnded conceptEnded = (generated.v9.dkf.ConceptEnded) triggerObj;
            generated.v10.dkf.ConceptEnded newConceptEnded = new generated.v10.dkf.ConceptEnded();

            newConceptEnded.setNodeId(conceptEnded.getNodeId());

            return newConceptEnded;
        } else if (triggerObj instanceof generated.v9.dkf.ChildConceptEnded) {

            generated.v9.dkf.ChildConceptEnded childConceptEnded = (generated.v9.dkf.ChildConceptEnded) triggerObj;
            generated.v10.dkf.ChildConceptEnded newChildConceptEnded = new generated.v10.dkf.ChildConceptEnded();

            newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());

            return newChildConceptEnded;
        } else if (triggerObj instanceof generated.v9.dkf.TaskEnded) {

            generated.v9.dkf.TaskEnded taskEnded = (generated.v9.dkf.TaskEnded) triggerObj;
            generated.v10.dkf.TaskEnded newTaskEnded = new generated.v10.dkf.TaskEnded();

            newTaskEnded.setNodeId(taskEnded.getNodeId());

            return newTaskEnded;
        } else if (triggerObj instanceof generated.v9.dkf.ConceptAssessment) {

            generated.v9.dkf.ConceptAssessment conceptAssessment = (generated.v9.dkf.ConceptAssessment) triggerObj;
            generated.v10.dkf.ConceptAssessment newConceptAssessment = new generated.v10.dkf.ConceptAssessment();

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
     * @return generated.v10.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException if the coordinate is of an unknown type
     */
    private generated.v10.dkf.Coordinate convertCoordinate(generated.v9.dkf.Coordinate coordinate)
            throws IllegalArgumentException {

        generated.v10.dkf.Coordinate newCoord = new generated.v10.dkf.Coordinate();

        Object coordType = coordinate.getType();
        if (coordType instanceof generated.v9.dkf.GCC) {

            generated.v9.dkf.GCC gcc = (generated.v9.dkf.GCC) coordType;
            generated.v10.dkf.GCC newGCC = new generated.v10.dkf.GCC();

            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());

            newCoord.setType(newGCC);

        } else if (coordType instanceof generated.v9.dkf.GDC) {
            // generated.
            generated.v9.dkf.GDC gdc = (generated.v9.dkf.GDC) coordType;
            generated.v10.dkf.GDC newGDC = new generated.v10.dkf.GDC();

            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());

            newCoord.setType(newGDC);

        } else if (coordType instanceof generated.v9.dkf.AGL) {

            generated.v9.dkf.AGL agl = (generated.v9.dkf.AGL) coordType;
            generated.v10.dkf.AGL newAGL = new generated.v10.dkf.AGL();

            newAGL.setX(agl.getX());
            newAGL.setY(agl.getY());
            newAGL.setElevation(agl.getElevation());

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
     * @return generated.v10.dkf.StrategyHandler - the new object
     * @throws IllegalArgumentException if the handler is null
     */
    private generated.v10.dkf.StrategyHandler convertStrategyHandler(generated.v9.dkf.StrategyHandler handler) {

        if (handler == null) {
            throw new IllegalArgumentException("Found null StrategyHandler but it is required.");
        }

        generated.v10.dkf.StrategyHandler newHandler = new generated.v10.dkf.StrategyHandler();

        if (handler.getParams() != null) {

            generated.v10.dkf.StrategyHandler.Params newParams = new generated.v10.dkf.StrategyHandler.Params();

            for (generated.v9.dkf.Nvpair nvpair : handler.getParams().getNvpair()) {
                generated.v10.dkf.Nvpair newNvpair = new generated.v10.dkf.Nvpair();
                newNvpair.setName(nvpair.getName());
                newNvpair.setValue(nvpair.getValue());
                newParams.getNvpair().add(newNvpair);
            }

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
     * @return generated.v10.dkf.Concepts - the new object
     * @throws IllegalArgumentException if the concept contains an entity with an unknown coordinate
     *         type
     */
    private generated.v10.dkf.Concepts convertConcepts(generated.v9.dkf.Concepts concepts, generated.v10.dkf.Objects scenarioObjects) throws IllegalArgumentException{

        // TODO check that all fields from v9 are copied over
        generated.v10.dkf.Concepts newConcepts = new generated.v10.dkf.Concepts();
        for (generated.v9.dkf.Concept concept : concepts.getConcept()) {

            generated.v10.dkf.Concept newConcept = new generated.v10.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());

            if (concept.getAssessments() != null) {
                newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }

            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if (conditionsOrConcepts instanceof generated.v9.dkf.Concepts) {
                // nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v9.dkf.Concepts)conditionsOrConcepts, scenarioObjects) );

            } else if (conditionsOrConcepts instanceof generated.v9.dkf.Conditions) {

                generated.v10.dkf.Conditions newConditions = new generated.v10.dkf.Conditions();

                generated.v9.dkf.Conditions conditions = (generated.v9.dkf.Conditions) conditionsOrConcepts;

                for (generated.v9.dkf.Condition condition : conditions.getCondition()) {

                    generated.v10.dkf.Condition newCondition = new generated.v10.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());

                    if (condition.getDefault() != null) {
                        generated.v10.dkf.Default newDefault = new generated.v10.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }

                    // Input
                    generated.v10.dkf.Input newInput = new generated.v10.dkf.Input();
                    if (condition.getInput() != null) {

                        Object inputType = condition.getInput().getType();

                        if (inputType == null) {
                            // nothing to do right now

                        } else if (inputType instanceof generated.v9.dkf.ApplicationCompletedCondition) {

                            generated.v9.dkf.ApplicationCompletedCondition conditionInput = (generated.v9.dkf.ApplicationCompletedCondition) inputType;

                            generated.v10.dkf.ApplicationCompletedCondition newConditionInput = new generated.v10.dkf.ApplicationCompletedCondition();

                            if (conditionInput.getIdealCompletionDuration() != null) {
                                newConditionInput
                                        .setIdealCompletionDuration(conditionInput.getIdealCompletionDuration());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.AutoTutorConditionInput) {

                            generated.v9.dkf.AutoTutorConditionInput conditionInput = (generated.v9.dkf.AutoTutorConditionInput) inputType;

                            generated.v10.dkf.AutoTutorConditionInput newConditionInput = new generated.v10.dkf.AutoTutorConditionInput();

                            if (conditionInput.getAutoTutorSKO() != null) {

                                generated.v9.dkf.AutoTutorSKO prevAutoTutorSKO = conditionInput.getAutoTutorSKO();
                                generated.v10.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevAutoTutorSKO);
                                newConditionInput.setAutoTutorSKO(newAutoTutorSKO);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.AvoidLocationCondition) {

                            generated.v9.dkf.AvoidLocationCondition conditionInput = (generated.v9.dkf.AvoidLocationCondition) inputType;

                            generated.v10.dkf.AvoidLocationCondition newConditionInput = new generated.v10.dkf.AvoidLocationCondition();

                            if (conditionInput.getRealTimeAssessmentRules() != null) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }

                            if (conditionInput.getPointRef() != null) {
                                for(generated.v9.dkf.PointRef pointRef : conditionInput.getPointRef()) {
                                    newConditionInput.getPointRef().add(convertPointRef(pointRef));
                                }
                            }

                            if (conditionInput.getAreaRef() != null) {
                                for(generated.v9.dkf.AreaRef areaRef : conditionInput.getAreaRef()) {
                                    newConditionInput.getAreaRef().add(convertAreaRef(areaRef));
                                }
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.CheckpointPaceCondition) {

                            generated.v9.dkf.CheckpointPaceCondition conditionInput = (generated.v9.dkf.CheckpointPaceCondition) inputType;

                            generated.v10.dkf.CheckpointPaceCondition newConditionInput = new generated.v10.dkf.CheckpointPaceCondition();
                            for (generated.v9.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()) {

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                newConditionInput.setTeamMemberRef(newTeamOfOneMember.getName());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.CheckpointProgressCondition) {

                            generated.v9.dkf.CheckpointProgressCondition conditionInput = (generated.v9.dkf.CheckpointProgressCondition) inputType;

                            generated.v10.dkf.CheckpointProgressCondition newConditionInput = new generated.v10.dkf.CheckpointProgressCondition();
                            for (generated.v9.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()) {

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                newConditionInput.setTeamMemberRef(newTeamOfOneMember.getName());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.CorridorBoundaryCondition) {

                            generated.v9.dkf.CorridorBoundaryCondition conditionInput = (generated.v9.dkf.CorridorBoundaryCondition) inputType;

                            generated.v10.dkf.CorridorBoundaryCondition newConditionInput = new generated.v10.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            generated.v10.dkf.PathRef newPathRef = new generated.v10.dkf.PathRef();
                            newPathRef.setValue(conditionInput.getPathRef().getValue());
                            newConditionInput.setPathRef(newPathRef);
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.CorridorPostureCondition) {

                            generated.v9.dkf.CorridorPostureCondition conditionInput = (generated.v9.dkf.CorridorPostureCondition) inputType;

                            generated.v10.dkf.CorridorPostureCondition newConditionInput = new generated.v10.dkf.CorridorPostureCondition();
                            generated.v10.dkf.PathRef newPathRef = new generated.v10.dkf.PathRef();
                            newPathRef.setValue(conditionInput.getPathRef().getValue());
                            newConditionInput.setPathRef(newPathRef);

                            generated.v10.dkf.Postures postures = new generated.v10.dkf.Postures();
                            for (String strPosture : conditionInput.getPostures().getPosture()) {
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.EliminateHostilesCondition) {

                            generated.v9.dkf.EliminateHostilesCondition conditionInput = (generated.v9.dkf.EliminateHostilesCondition) inputType;

                            generated.v10.dkf.EliminateHostilesCondition newConditionInput = new generated.v10.dkf.EliminateHostilesCondition();

                            if (conditionInput.getEntities() != null) {
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            if (conditionInput.getRealTimeAssessmentRules() != null) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.EnterAreaCondition) {

                            generated.v9.dkf.EnterAreaCondition conditionInput = (generated.v9.dkf.EnterAreaCondition) inputType;

                            generated.v10.dkf.EnterAreaCondition newConditionInput = new generated.v10.dkf.EnterAreaCondition();

                            for (generated.v9.dkf.Entrance entrance : conditionInput.getEntrance()) {

                                generated.v10.dkf.Entrance newEntrance = new generated.v10.dkf.Entrance();

                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());

                                generated.v10.dkf.Inside newInside = new generated.v10.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setPoint(entrance.getInside().getPoint());
                                newEntrance.setInside(newInside);

                                generated.v10.dkf.Outside newOutside = new generated.v10.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setPoint(entrance.getOutside().getPoint());
                                newEntrance.setOutside(newOutside);

                                newConditionInput.getEntrance().add(newEntrance);
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                newConditionInput.setTeamMemberRef(newTeamOfOneMember.getName());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.ExplosiveHazardSpotReportCondition) {

                            @SuppressWarnings("unused")
                            generated.v9.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v9.dkf.ExplosiveHazardSpotReportCondition) inputType;

                            generated.v10.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v10.dkf.ExplosiveHazardSpotReportCondition();
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.GenericConditionInput) {

                            generated.v9.dkf.GenericConditionInput conditionInput = (generated.v9.dkf.GenericConditionInput) inputType;

                            generated.v10.dkf.GenericConditionInput newConditionInput = new generated.v10.dkf.GenericConditionInput();

                            if (conditionInput.getNvpair() != null) {
                                for (generated.v9.dkf.Nvpair nvPair : conditionInput.getNvpair()) {
                                    newConditionInput.getNvpair().add(convertNvpair(nvPair));
                                }
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.HasMovedExcavatorComponentInput) {

                            generated.v9.dkf.HasMovedExcavatorComponentInput conditionInput = (generated.v9.dkf.HasMovedExcavatorComponentInput) inputType;
                            generated.v10.dkf.HasMovedExcavatorComponentInput newConditionInput = new generated.v10.dkf.HasMovedExcavatorComponentInput();

                            newConditionInput.getComponent().addAll(convertComponents(conditionInput.getComponent()));
                            newConditionInput.setMaxAssessments(conditionInput.getMaxAssessments());

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.IdentifyPOIsCondition) {

                            generated.v9.dkf.IdentifyPOIsCondition conditionInput = (generated.v9.dkf.IdentifyPOIsCondition) inputType;

                            generated.v10.dkf.IdentifyPOIsCondition newConditionInput = new generated.v10.dkf.IdentifyPOIsCondition();

                            if (conditionInput.getPois() != null) {

                                generated.v10.dkf.Pois pois = new generated.v10.dkf.Pois();
                                for (generated.v9.dkf.PointRef pointRef : conditionInput.getPois()
                                        .getPointRef()) {
                                    pois.getPointRef().add(convertPointRef(pointRef));
                                }

                                newConditionInput.setPois(pois);
                            }

                            if (conditionInput.getRealTimeAssessmentRules() != null ) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.LifeformTargetAccuracyCondition) {

                            generated.v9.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v9.dkf.LifeformTargetAccuracyCondition) inputType;

                            generated.v10.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v10.dkf.LifeformTargetAccuracyCondition();

                            if (conditionInput.getEntities() != null) {
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            if (conditionInput.getRealTimeAssessmentRules() != null ) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.MarksmanshipPrecisionCondition) {

                            generated.v9.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v9.dkf.MarksmanshipPrecisionCondition) inputType;

                            generated.v10.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v10.dkf.MarksmanshipPrecisionCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.MarksmanshipSessionCompleteCondition) {

                            generated.v9.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v9.dkf.MarksmanshipSessionCompleteCondition) inputType;

                            generated.v10.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v10.dkf.MarksmanshipSessionCompleteCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.NineLineReportCondition) {

                            @SuppressWarnings("unused")
                            generated.v9.dkf.NineLineReportCondition conditionInput = (generated.v9.dkf.NineLineReportCondition) inputType;

                            generated.v10.dkf.NineLineReportCondition newConditionInput = new generated.v10.dkf.NineLineReportCondition();
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.NoConditionInput) {
                            newInput.setType(new generated.v10.dkf.NoConditionInput());

                        } else if (inputType instanceof generated.v9.dkf.NumberOfShotsFiredCondition) {

                            generated.v9.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v9.dkf.NumberOfShotsFiredCondition) inputType;

                            generated.v10.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v10.dkf.NumberOfShotsFiredCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.PowerPointDwellCondition) {

                            generated.v9.dkf.PowerPointDwellCondition conditionInput = (generated.v9.dkf.PowerPointDwellCondition) inputType;

                            generated.v10.dkf.PowerPointDwellCondition newConditionInput = new generated.v10.dkf.PowerPointDwellCondition();

                            generated.v10.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v10.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);

                            generated.v10.dkf.PowerPointDwellCondition.Slides slides = new generated.v10.dkf.PowerPointDwellCondition.Slides();
                            for (generated.v9.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput
                                    .getSlides().getSlide()) {

                                generated.v10.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v10.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());

                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.RulesOfEngagementCondition) {

                            generated.v9.dkf.RulesOfEngagementCondition conditionInput = (generated.v9.dkf.RulesOfEngagementCondition) inputType;

                            generated.v10.dkf.RulesOfEngagementCondition newConditionInput = new generated.v10.dkf.RulesOfEngagementCondition();
                            generated.v10.dkf.Wcs newWCS = new generated.v10.dkf.Wcs();
                            newWCS.setValue(generated.v10.dkf.WeaponControlStatusEnum
                                    .fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);

                            if (conditionInput.getRealTimeAssessmentRules() != null ) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.SIMILEConditionInput) {

                            generated.v9.dkf.SIMILEConditionInput conditionInput = (generated.v9.dkf.SIMILEConditionInput) inputType;

                            generated.v10.dkf.SIMILEConditionInput newConditionInput = new generated.v10.dkf.SIMILEConditionInput();

                            if (conditionInput.getConditionKey() != null) {
                                newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }

                            if (conditionInput.getConfigurationFile() != null) {
                                newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.SpotReportCondition) {

                            @SuppressWarnings("unused")
                            generated.v9.dkf.SpotReportCondition conditionInput = (generated.v9.dkf.SpotReportCondition) inputType;

                            generated.v10.dkf.SpotReportCondition newConditionInput = new generated.v10.dkf.SpotReportCondition();
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.TimerConditionInput) {

                            generated.v9.dkf.TimerConditionInput conditionInput = (generated.v9.dkf.TimerConditionInput) inputType;

                            generated.v10.dkf.TimerConditionInput newConditionInput = new generated.v10.dkf.TimerConditionInput();

                            newConditionInput.setRepeatable(generated.v10.dkf.BooleanEnum
                                    .fromValue(conditionInput.getRepeatable().toString().toLowerCase()));
                            newConditionInput.setInterval(conditionInput.getInterval());
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.UseRadioCondition) {

                            @SuppressWarnings("unused")
                            generated.v9.dkf.UseRadioCondition conditionInput = (generated.v9.dkf.UseRadioCondition) inputType;

                            generated.v10.dkf.UseRadioCondition newConditionInput = new generated.v10.dkf.UseRadioCondition();
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                generated.v10.dkf.TeamMemberRefs teamMemberRefs = new generated.v10.dkf.TeamMemberRefs();
                                teamMemberRefs.getTeamMemberRef().add(newTeamOfOneMember.getName());
                                newConditionInput.setTeamMemberRefs(teamMemberRefs);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v9.dkf.PaceCountCondition) {
                            
                            generated.v9.dkf.PaceCountCondition paceCountCondition = (generated.v9.dkf.PaceCountCondition) inputType;

                            generated.v10.dkf.PaceCountCondition newPaceCountCondition = new generated.v10.dkf.PaceCountCondition();
                            
                            newPaceCountCondition.setDistanceThreshold(paceCountCondition.getDistanceThreshold());
                            newPaceCountCondition.setExpectedDistance(paceCountCondition.getExpectedDistance());
                            // newly added during v10
                            newPaceCountCondition.setTeamMemberRef(null);
                            
                            // set the single team member as the one being assessed against this condition
                            if (newTeamOfOneMember != null) {
                                newPaceCountCondition.setTeamMemberRef(newTeamOfOneMember.getName());
                            }
                            
                        } else {
                            throw new IllegalArgumentException("Found unhandled condition input type of " + inputType);
                        }

                    }
                    newCondition.setInput(newInput);

                    // Scoring
                    generated.v10.dkf.Scoring newScoring = new generated.v10.dkf.Scoring();
                    if (condition.getScoring() != null) {
                        // Only add the scoring element if it has children.
                        // As of version 5, there cannot be a scoring element with no children
                        if (!condition.getScoring().getType().isEmpty()) {

                            for (Object scoringType : condition.getScoring().getType()) {

                                if (scoringType instanceof generated.v9.dkf.Count) {

                                    generated.v9.dkf.Count count = (generated.v9.dkf.Count) scoringType;

                                    generated.v10.dkf.Count newCount = new generated.v10.dkf.Count();
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v10.dkf.UnitsEnumType.fromValue(count.getUnits().value()));

                                    if (count.getEvaluators() != null) {
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }

                                    newScoring.getType().add(newCount);

                                } else if (scoringType instanceof generated.v9.dkf.CompletionTime) {

                                    generated.v9.dkf.CompletionTime complTime = (generated.v9.dkf.CompletionTime) scoringType;

                                    generated.v10.dkf.CompletionTime newComplTime = new generated.v10.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(
                                            generated.v10.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if (complTime.getEvaluators() != null) {
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }

                                    newScoring.getType().add(newComplTime);

                                } else if (scoringType instanceof generated.v9.dkf.ViolationTime) {

                                    generated.v9.dkf.ViolationTime violationTime = (generated.v9.dkf.ViolationTime) scoringType;

                                    generated.v10.dkf.ViolationTime newViolationTime = new generated.v10.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(
                                            generated.v10.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
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

            } else {
                throw new IllegalArgumentException("Found unhandled subconcept node type of " + conditionsOrConcepts);
            }

            if (concept.getPerformanceMetric() != null) {
                generated.v10.dkf.PerformanceMetric newPerformanceMetric = new generated.v10.dkf.PerformanceMetric();
                newPerformanceMetric.setPerformanceMetricImpl(concept.getPerformanceMetric().getPerformanceMetricImpl());
                newConcept.setPerformanceMetric(newPerformanceMetric);
            }
            
            if (concept.getConfidenceMetric() != null) {
                generated.v10.dkf.ConfidenceMetric newConfidenceMetric = new generated.v10.dkf.ConfidenceMetric();
                newConfidenceMetric.setConfidenceMetricImpl(concept.getConfidenceMetric().getConfidenceMetricImpl());
                newConcept.setConfidenceMetric(newConfidenceMetric);
            }

            if (concept.getCompetenceMetric() != null) {
                generated.v10.dkf.CompetenceMetric newCompetenceMetric = new generated.v10.dkf.CompetenceMetric();
                newCompetenceMetric.setCompetenceMetricImpl(concept.getCompetenceMetric().getCompetenceMetricImpl());
                newConcept.setCompetenceMetric(newCompetenceMetric);
            }

            if (concept.getTrendMetric() != null) {
                generated.v10.dkf.TrendMetric newTrendMetric = new generated.v10.dkf.TrendMetric();
                newTrendMetric.setTrendMetricImpl(concept.getTrendMetric().getTrendMetricImpl());
                newConcept.setTrendMetric(newTrendMetric);
            }

            if (concept.getPriorityMetric() != null) {
                generated.v10.dkf.PriorityMetric newPriorityMetric = new generated.v10.dkf.PriorityMetric();
                newPriorityMetric.setPriorityMetricImpl(concept.getPriorityMetric().getPriorityMetricImpl());
                newConcept.setPriorityMetric(newPriorityMetric);
            }

            if (concept.getPriority() != null) {
                newConcept.setPriority(concept.getPriority());
            }

            newConcepts.getConcept().add(newConcept);

        }

        return newConcepts;
    }

    /**
     * Convert a Real Time Assessment Rules object to a new Real Time Assessment Rules
     * 
     * @param realTimeAssessmentRules - the object to convert
     * @return the new object
     */
    private generated.v10.dkf.RealTimeAssessmentRules convertRealTimeAssessmentRules(
            generated.v9.dkf.RealTimeAssessmentRules realTimeAssessmentRules) {
        generated.v10.dkf.RealTimeAssessmentRules newRealTimeAssessmentRules = new generated.v10.dkf.RealTimeAssessmentRules();
        
        if (realTimeAssessmentRules.getCount() != null) {
            generated.v9.dkf.Count count = realTimeAssessmentRules.getCount();
            generated.v10.dkf.Count newCount = new generated.v10.dkf.Count();
            if (count.getEvaluators() != null) {
                newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
            }
            newCount.setName(count.getName());
            newCount.setUnits(generated.v10.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
            newRealTimeAssessmentRules.setCount(newCount);
        }
        
        if (realTimeAssessmentRules.getViolationTime() != null) {
            generated.v9.dkf.ViolationTime violationTime = realTimeAssessmentRules.getViolationTime();
            generated.v10.dkf.ViolationTime newViolationTime = new generated.v10.dkf.ViolationTime();
            if (violationTime.getEvaluators() != null) {
                newViolationTime.setEvaluators(convertEvaluators(violationTime.getEvaluators()));
            }
            newViolationTime.setName(violationTime.getName());
            newViolationTime.setUnits(generated.v10.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
            newRealTimeAssessmentRules.setViolationTime(newViolationTime);
        }
        return newRealTimeAssessmentRules;
    }

    /**
     * Convert a Nvpair object to a new Nvpair object.
     *
     * @param nvPair - the object to convert
     * @return the new object
     */
    private generated.v10.dkf.Nvpair convertNvpair(generated.v9.dkf.Nvpair nvPair) {

        generated.v10.dkf.Nvpair newNvpair = new generated.v10.dkf.Nvpair();
        newNvpair.setName(nvPair.getName());
        newNvpair.setValue(nvPair.getValue());

        return newNvpair;
    }

    /**
     * Convert a PointRef object to a new PointRef object.
     *
     * @param waypointRef - the object to convert
     * @return the new object
     */
    private generated.v10.dkf.PointRef convertPointRef(generated.v9.dkf.PointRef pointRef){

        generated.v10.dkf.PointRef newPoint = new generated.v10.dkf.PointRef();
        newPoint.setValue(pointRef.getValue());
        newPoint.setDistance(pointRef.getDistance());

        return newPoint;
    }

    /**
     * Convert an AreaRef object to a new AreaRef
     * @param AreaRef
     * @return
     */
    private generated.v10.dkf.AreaRef convertAreaRef(generated.v9.dkf.AreaRef areaRef) {

        generated.v10.dkf.AreaRef newAreaRef = new generated.v10.dkf.AreaRef();
        newAreaRef.setValue(areaRef.getValue());

        return newAreaRef;
    }

    /**
     * Converts a boolean enum to the next version
     * 
     * @param booleanEnum the boolean enum to convert
     * @return the converted boolean enum
     */
    private generated.v10.dkf.BooleanEnum convertBooleanEnum(generated.v9.dkf.BooleanEnum booleanEnum) {
        if (booleanEnum == null) {
            return null;
        }

        return generated.v9.dkf.BooleanEnum.TRUE.equals(booleanEnum) ? generated.v10.dkf.BooleanEnum.TRUE
                : generated.v10.dkf.BooleanEnum.FALSE;
    }

    /******************* END CONVERT DKF *******************/
}
