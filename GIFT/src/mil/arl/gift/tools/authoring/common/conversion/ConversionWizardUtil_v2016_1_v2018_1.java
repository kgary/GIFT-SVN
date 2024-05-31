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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.StringUtils;

/**
 * Responsible for converting 2016-1 GIFT version XML files to 2017-1 versions when applicable, i.e.
 * if a particular schema changes enough to warrant a conversion process.
 *
 * @author mhoffman
 *
 */
public class ConversionWizardUtil_v2016_1_v2018_1 extends AbstractConversionWizardUtil {

    //////////////////////////////////////////////////////////////
    /////////// DON'T REMOVE THE ITEMS IN THIS SECTION ///////////
    //////////////////////////////////////////////////////////////

    /** The new version number */
    private static final String VERSION_NUMBER = "8.0.1";

    @Override
    public String getConvertedVersionNumber() {
        return VERSION_NUMBER;
    }

    /********* PREVIOUS SCHEMA FILES *********/

    /** Path to the specific version folder */
    private static final String versionPathPrefix = StringUtils.join(File.separator,
            Arrays.asList("data", "conversionWizard", "v2016_1"));

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

    /** Course schema root */
    public static final Class<?> PREV_COURSE_ROOT = generated.v7.course.Course.class;

    /** DKF schema root */
    public static final Class<?> PREV_DKF_ROOT = generated.v7.dkf.Scenario.class;

    /** Metadata schema root */
    public static final Class<?> PREV_METADATA_ROOT = generated.v7.metadata.Metadata.class;

    /** Learner schema root */
    public static final Class<?> PREV_LEARNER_ROOT = generated.v7.learner.LearnerConfiguration.class;

    /** Pedagogical schema root */
    public static final Class<?> PREV_PEDAGOGICAL_ROOT = generated.v7.ped.EMAP.class;

    /** Sensor schema root */
    public static final Class<?> PREV_SENSOR_ROOT = generated.v7.sensor.SensorsConfiguration.class;

    /** Training App schema root */
    public static final Class<?> PREV_TRAINING_APP_ROOT = generated.v7.course.TrainingApplicationWrapper.class;

    /** Conversation schema root */
    public static final Class<?> PREV_CONVERSATION_ROOT = generated.v7.conversation.Conversation.class;

    /** Lesson Material schema root */
    public static final Class<?> PREV_LESSON_MATERIAL_ROOT = generated.v7.course.LessonMaterialList.class;

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

    @Override
    public UnmarshalledFile convertMetadata(FileProxy metadataFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {

        UnmarshalledFile uFile = parseFile(metadataFile, getPreviousMetadataSchemaFile(), getPreviousMetadataSchemaRoot(), failOnFirstSchemaError);
        generated.v7.metadata.Metadata v7metadata = (generated.v7.metadata.Metadata)uFile.getUnmarshalled();

        // Convert the version 7 metadata to the newest version and return it
        return convertMetadata(v7metadata, showCompletionDialog);
    }

    @Override
    public UnmarshalledFile convertMetadata(Serializable oldMetadataObj, boolean showCompletionDialog){
        generated.v7.metadata.Metadata oldMetadata = (generated.v7.metadata.Metadata) oldMetadataObj;
        generated.v8.metadata.Metadata newMetadata = new generated.v8.metadata.Metadata();

        newMetadata.setVersion(VERSION_NUMBER);

        newMetadata.setDisplayName(oldMetadata.getDisplayName());

        //Change Metadata.MerrillQuadrant to Metadata.presentAt.MerrillQuadrant
        if(oldMetadata.getMerrillQuadrant() != null){

            generated.v8.metadata.PresentAt presentAt = new generated.v8.metadata.PresentAt();
            newMetadata.setPresentAt(presentAt);

            presentAt.setMerrillQuadrant(oldMetadata.getMerrillQuadrant());
        }

        //Change flat references to Simple, Training App, URL and Lesson Material to new objects
        if(oldMetadata.getSimpleRef() != null){

            generated.v8.metadata.Metadata.Simple simple = new generated.v8.metadata.Metadata.Simple();
            simple.setValue(oldMetadata.getSimpleRef());
            newMetadata.setContent(simple);

        }else if(oldMetadata.getTrainingAppRef() != null){

            generated.v8.metadata.Metadata.TrainingApp tApp = new generated.v8.metadata.Metadata.TrainingApp();
            tApp.setValue(oldMetadata.getTrainingAppRef());
            newMetadata.setContent(tApp);

        }else if(oldMetadata.getURL() != null){

            generated.v8.metadata.Metadata.URL url = new generated.v8.metadata.Metadata.URL();
            url.setValue(oldMetadata.getURL());
            newMetadata.setContent(url);

        }else if(oldMetadata.getLessonMaterialRef() != null){

            generated.v8.metadata.Metadata.LessonMaterial lessonMaterial = new generated.v8.metadata.Metadata.LessonMaterial();
            lessonMaterial.setValue(oldMetadata.getLessonMaterialRef());
            newMetadata.setContent(lessonMaterial);
        }

        newMetadata.setConcepts(convertConcepts(oldMetadata.getConcepts()));

        return super.convertMetadata(newMetadata, showCompletionDialog);
    }

    private static generated.v8.metadata.Metadata.Concepts convertConcepts(generated.v7.metadata.Metadata.Concepts oldConcepts) {
        generated.v8.metadata.Metadata.Concepts newConcepts = new generated.v8.metadata.Metadata.Concepts();

        for(generated.v7.metadata.Concept concept: oldConcepts.getConcept()){
            generated.v8.metadata.Concept newConcept = new generated.v8.metadata.Concept();

            //set concept content type to Passive and copy metadata attributes into child element
            if(concept.getAttributes() != null){

                generated.v8.metadata.ActivityType contentType = new generated.v8.metadata.ActivityType();
                newConcept.setActivityType(contentType);

                generated.v8.metadata.ActivityType.Passive passive = new generated.v8.metadata.ActivityType.Passive();
                contentType.setType(passive);

                passive.setAttributes(convertMetaDataAttributes(concept.getAttributes()));

                newConcept.setActivityType(contentType);
            }

            newConcept.setName(concept.getName());
            newConcepts.getConcept().add(newConcept);
        }

        return newConcepts;
    }

    private static generated.v8.metadata.Attributes convertMetaDataAttributes(generated.v7.metadata.Attributes oldAttributes) {
        generated.v8.metadata.Attributes newMetaAttributes = new generated.v8.metadata.Attributes();
        int index = 0;

        for(generated.v7.metadata.Attribute attribute : oldAttributes.getAttribute()){
            newMetaAttributes.getAttribute().add(new generated.v8.metadata.Attribute());
            newMetaAttributes.getAttribute().get(index).setValue(attribute.getValue());

            index++;
        }

        return newMetaAttributes;
    }

    //////////////////////////////////////////////// CONVERT DKF /////////////////////////////////////////////////////////////

    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{

        UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(), failOnFirstSchemaError);
        generated.v7.dkf.Scenario prevScenario = (generated.v7.dkf.Scenario)uFile.getUnmarshalled();

        return convertScenario(prevScenario, showCompletionDialog);
    }

    @Override
    public UnmarshalledFile convertScenario(Serializable prevScenarioObj, boolean showCompletionDialog) throws IllegalArgumentException {
        generated.v7.dkf.Scenario prevScenario = (generated.v7.dkf.Scenario) prevScenarioObj;
        generated.v8.dkf.Scenario newScenario = new generated.v8.dkf.Scenario();

        //
        // copy over contents from old object to new object
        //
        newScenario.setDescription(prevScenario.getDescription());
        newScenario.setName(prevScenario.getName());
        newScenario.setVersion(VERSION_NUMBER);

        //
        //Learner Id
        //
        if(prevScenario.getLearnerId() != null){
            generated.v8.dkf.LearnerId newLearnerId = new generated.v8.dkf.LearnerId();
            generated.v8.dkf.StartLocation newStartLocation = new generated.v8.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(prevScenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }

        //
        //Resources
        //
        generated.v8.dkf.Resources newResources = new generated.v8.dkf.Resources();
        newResources.setSurveyContext(prevScenario.getResources().getSurveyContext());

        generated.v8.dkf.AvailableLearnerActions newALA = new generated.v8.dkf.AvailableLearnerActions();

        if(prevScenario.getResources().getAvailableLearnerActions() != null){

            generated.v7.dkf.AvailableLearnerActions ala = prevScenario.getResources().getAvailableLearnerActions();
            if(ala.getLearnerActionsFiles() != null){
                generated.v8.dkf.LearnerActionsFiles newLAF = new generated.v8.dkf.LearnerActionsFiles();
                for(String filename : ala.getLearnerActionsFiles().getFile()){
                    newLAF.getFile().add(filename);
                }

                newALA.setLearnerActionsFiles(newLAF);
            }

            if(ala.getLearnerActionsList() != null){

                generated.v8.dkf.LearnerActionsList newLAL = new generated.v8.dkf.LearnerActionsList();
                for(generated.v7.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()){

                    generated.v8.dkf.LearnerAction newAction = new generated.v8.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    newAction.setType(generated.v8.dkf.LearnerActionEnumType.fromValue(action.getType().value()));
                    newAction.setDescription(action.getDescription());

                    if(action.getLearnerActionParams() != null){

                        generated.v7.dkf.TutorMeParams tutorMeParams = action.getLearnerActionParams();
                        generated.v8.dkf.TutorMeParams newTutorMeParams = new generated.v8.dkf.TutorMeParams();
                        if(tutorMeParams.getConfiguration() instanceof generated.v7.dkf.ConversationTreeFile){

                            generated.v7.dkf.ConversationTreeFile convTreeFile = (generated.v7.dkf.ConversationTreeFile)tutorMeParams.getConfiguration();
                            generated.v8.dkf.ConversationTreeFile newConvTreeFile = new generated.v8.dkf.ConversationTreeFile();
                            newConvTreeFile.setName(convTreeFile.getName());

                            newTutorMeParams.setConfiguration(newConvTreeFile);

                        }else if(tutorMeParams.getConfiguration() instanceof generated.v7.dkf.AutoTutorSKO){

                            generated.v7.dkf.AutoTutorSKO atSKO = (generated.v7.dkf.AutoTutorSKO)tutorMeParams.getConfiguration();
                            generated.v8.dkf.AutoTutorSKO newATSKO = convertAutoTutorSKO(atSKO);

                            newTutorMeParams.setConfiguration(newATSKO);

                        }else{
                            //unhandled tutor me params
                            throw new IllegalArgumentException("Found unhandled tutor me params type of "+tutorMeParams+" in learner action '"+action.getDisplayName()+"'.");
                        }

                        newAction.setLearnerActionParams(newTutorMeParams);
                    }

                    newLAL.getLearnerAction().add(newAction);
                }
                newALA.setLearnerActionsList(newLAL);
            }

            newResources.setAvailableLearnerActions(newALA);
        }

        newScenario.setResources(newResources);

        //
        //End Triggers
        //
        generated.v8.dkf.Scenario.EndTriggers newScenarioEndTriggers = new generated.v8.dkf.Scenario.EndTriggers();

        if(prevScenario.getEndTriggers() != null) {
            newScenarioEndTriggers.getTrigger().addAll(convertScenarioEndTriggers( prevScenario.getEndTriggers().getTrigger()));
            newScenario.setEndTriggers(newScenarioEndTriggers);
        }

        //
        //Assessment
        //
        generated.v8.dkf.Assessment newAssessment = new generated.v8.dkf.Assessment();
        if(prevScenario.getAssessment() != null){

            generated.v7.dkf.Assessment assessment = prevScenario.getAssessment();

            //
            // Objects
            //
            generated.v8.dkf.Objects newObjects = new generated.v8.dkf.Objects();
            if(assessment.getObjects() != null){

                if(assessment.getObjects().getWaypoints() != null){

                    generated.v8.dkf.Waypoints newWaypoints = new generated.v8.dkf.Waypoints();

                    generated.v7.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for(generated.v7.dkf.Waypoint waypoint : waypoints.getWaypoint()){

                        generated.v8.dkf.Waypoint newWaypoint = new generated.v8.dkf.Waypoint();
                        newWaypoint.setName(waypoint.getName());
                        newWaypoint.setCoordinate(convertCoordinate(waypoint.getCoordinate()));

                        newWaypoints.getWaypoint().add(newWaypoint);
                    }

                    newObjects.setWaypoints(newWaypoints);
                }
            }
            newAssessment.setObjects(newObjects);

            //
            // Tasks
            //
            generated.v8.dkf.Tasks newTasks = new generated.v8.dkf.Tasks();
            if(assessment.getTasks() != null){

                for(generated.v7.dkf.Task task : assessment.getTasks().getTask()){

                    generated.v8.dkf.Task newTask = new generated.v8.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());

                    // start triggers
                    if(task.getStartTriggers() != null){
                        generated.v8.dkf.StartTriggers newStartTriggers = new generated.v8.dkf.StartTriggers();
                        newStartTriggers.getTrigger().addAll(convertStartTriggers( task.getStartTriggers().getTrigger()));
                        newTask.setStartTriggers(newStartTriggers);
                    }

                    // end triggers
                    if(task.getEndTriggers() != null){
                        generated.v8.dkf.EndTriggers newEndTriggers = new generated.v8.dkf.EndTriggers();
                        newEndTriggers.getTrigger().addAll(convertEndTriggers( task.getEndTriggers().getTrigger()));
                        newTask.setEndTriggers(newEndTriggers);
                    }

                    // Concepts
                    if(task.getConcepts() != null){
                        newTask.setConcepts(convertConcepts(task.getConcepts()));
                    }

                    // Assessments
                    if(task.getAssessments() != null){
                        newTask.setAssessments(convertAssessments(task.getAssessments()));
                    }

                    newTasks.getTask().add(newTask);
                }

            }//end task if

            newAssessment.setTasks(newTasks);

        } //end assessment if

        newScenario.setAssessment(newAssessment);

        //
        //Actions
        //
        if(prevScenario.getActions() != null){

            generated.v7.dkf.Actions actions = prevScenario.getActions();
            generated.v8.dkf.Actions newActions = new generated.v8.dkf.Actions();

            //instructional strategies
            if(actions.getInstructionalStrategies() != null){

                generated.v7.dkf.Actions.InstructionalStrategies iStrategies = actions.getInstructionalStrategies();
                generated.v8.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v8.dkf.Actions.InstructionalStrategies();

                for(generated.v7.dkf.Strategy strategy : iStrategies.getStrategy()){

                    generated.v8.dkf.Strategy newStrategy = new generated.v8.dkf.Strategy();
                    newStrategy.setName(strategy.getName());

                    Object strategyType = strategy.getStrategyType();//getValueAttribute();
                    if(strategyType instanceof generated.v7.dkf.PerformanceAssessment){

                        generated.v7.dkf.PerformanceAssessment perfAss = (generated.v7.dkf.PerformanceAssessment)strategyType;

                        generated.v8.dkf.PerformanceAssessment newPerfAss = new generated.v8.dkf.PerformanceAssessment();

                        if(perfAss.getAssessmentType() instanceof generated.v7.dkf.PerformanceAssessment.PerformanceNode){
                            generated.v8.dkf.PerformanceAssessment.PerformanceNode newPerfAssNode = new generated.v8.dkf.PerformanceAssessment.PerformanceNode();

                            newPerfAssNode.setNodeId(((generated.v7.dkf.PerformanceAssessment.PerformanceNode)perfAss.getAssessmentType()).getNodeId());
                            newPerfAss.setAssessmentType(newPerfAssNode);
                        }else{
                            generated.v7.dkf.Conversation prevConv = (generated.v7.dkf.Conversation)perfAss.getAssessmentType();

                            generated.v8.dkf.Conversation newConv = new generated.v8.dkf.Conversation();
                            if(prevConv.getType() instanceof generated.v7.dkf.ConversationTreeFile){

                                generated.v8.dkf.ConversationTreeFile newTreeFile = new generated.v8.dkf.ConversationTreeFile();
                                newTreeFile.setName(((generated.v7.dkf.ConversationTreeFile)prevConv.getType()).getName());

                                newConv.setType(newTreeFile);
                            }else{

                                generated.v7.dkf.AutoTutorSKO prevATSKO = (generated.v7.dkf.AutoTutorSKO)prevConv.getType();

                                generated.v8.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevATSKO);

                                newConv.setType(newAutoTutorSKO);
                            }

                            newPerfAss.setAssessmentType(newConv);
                        }

                        newPerfAss.setStrategyHandler(convertStrategyHandler(perfAss.getStrategyHandler()));

                        newStrategy.setStrategyType(newPerfAss);

                    }else if(strategyType instanceof generated.v7.dkf.InstructionalIntervention){

                        generated.v7.dkf.InstructionalIntervention iIntervention = (generated.v7.dkf.InstructionalIntervention)strategyType;

                        generated.v8.dkf.InstructionalIntervention newIIntervention = new generated.v8.dkf.InstructionalIntervention();
                        newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));

                        /* Handle each intervention type within the
                         * InstructionalIntervention. Each type is either a
                         * Feedback or a DoNothingTactic. */
                        for(Serializable serFeedback : iIntervention.getInterventionTypes()){
                            generated.v7.dkf.Feedback feedback = null;
                            if(serFeedback instanceof generated.v7.dkf.Feedback){
                                feedback = (generated.v7.dkf.Feedback) serFeedback;

                                generated.v8.dkf.Feedback newFeedback = new generated.v8.dkf.Feedback();

                                if (feedback.getFeedbackPresentation() instanceof generated.v7.dkf.Message) {

                                    generated.v7.dkf.Message message = (generated.v7.dkf.Message) feedback.getFeedbackPresentation();
                                    generated.v8.dkf.Message feedbackMsg = convertMessage(message);

                                    newFeedback.setFeedbackPresentation(feedbackMsg);

                                }
                                else if (feedback.getFeedbackPresentation() instanceof generated.v7.dkf.Audio) {

                                    generated.v7.dkf.Audio audio = (generated.v7.dkf.Audio) feedback.getFeedbackPresentation();

                                    generated.v8.dkf.Audio newAudio = new generated.v8.dkf.Audio();

                                    // An audio object requires a .mp3 file but does not require a .ogg file
                                    newAudio.setMP3File(audio.getMP3File());

                                    if (audio.getOGGFile() != null) {
                                        newAudio.setOGGFile(audio.getOGGFile());
                                    }

                                    newFeedback.setFeedbackPresentation(newAudio);

                                }

                                else if (feedback.getFeedbackPresentation() instanceof generated.v7.dkf.MediaSemantics) {

                                    generated.v7.dkf.MediaSemantics semantics = (generated.v7.dkf.MediaSemantics) feedback.getFeedbackPresentation();

                                    generated.v8.dkf.MediaSemantics newSemantics = new generated.v8.dkf.MediaSemantics();

                                    // A MediaSematic file requires an avatar and a key name property.
                                    newSemantics.setAvatar(semantics.getAvatar());
                                    newSemantics.setKeyName(semantics.getKeyName());

                                    if(semantics.getMessage() != null){
                                        newSemantics.setMessage(convertMessage(semantics.getMessage()));
                                    }

                                    newFeedback.setFeedbackPresentation(newSemantics);
                                }

                                newFeedback.setAffectiveFeedbackType(feedback.getAffectiveFeedbackType());
                                newFeedback.setFeedbackSpecificityType(feedback.getFeedbackSpecificityType());

                                newIIntervention.getInterventionTypes().add(newFeedback);

                            } else if (serFeedback instanceof generated.v7.dkf.DoNothingTactic) {
                                newIIntervention.getInterventionTypes().add(new generated.v8.dkf.DoNothingTactic());
                            } else {
                                throw new IllegalArgumentException("Found unhandled feedback type of " + serFeedback);
                            }
                        }
                        newStrategy.setStrategyType(newIIntervention);

                    }else if(strategyType instanceof generated.v7.dkf.ScenarioAdaptation){

                        generated.v7.dkf.ScenarioAdaptation adaptation = (generated.v7.dkf.ScenarioAdaptation)strategyType;

                        generated.v8.dkf.ScenarioAdaptation newAdaptation = new generated.v8.dkf.ScenarioAdaptation();
                        newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));

                        /* Handle each intervention type within the
                         * ScenarioAdaptation. Each type is either an
                         * EnvironmentAdaptation or a DoNothingTactic. */
                        for(Serializable serEAdapt : adaptation.getAdaptationTypes()){
                            generated.v7.dkf.EnvironmentAdaptation eAdapt = null;
                            if(serEAdapt instanceof generated.v7.dkf.EnvironmentAdaptation){
                                eAdapt = (generated.v7.dkf.EnvironmentAdaptation) serEAdapt;

                                generated.v8.dkf.EnvironmentAdaptation newEAdapt = new generated.v8.dkf.EnvironmentAdaptation();

                                generated.v8.dkf.EnvironmentAdaptation.Pair newPair = new generated.v8.dkf.EnvironmentAdaptation.Pair();
                                newPair.setType(eAdapt.getPair().getType());
                                newPair.setValue(eAdapt.getPair().getValue());
                                newEAdapt.setPair(newPair);

                                newAdaptation.getAdaptationTypes().add(newEAdapt);
                            } else if (serEAdapt instanceof generated.v7.dkf.DoNothingTactic) {
                                newAdaptation.getAdaptationTypes().add(new generated.v8.dkf.DoNothingTactic());
                            } else {
                                throw new IllegalArgumentException("Found unhandled Environment Adaption type of " + serEAdapt);
                            }
                        }

                        newStrategy.setStrategyType(newAdaptation);

                    } else if (strategyType instanceof generated.v7.dkf.DoNothingInstStrategy) {
                        newStrategy.setStrategyType(new generated.v8.dkf.DoNothingInstStrategy());
                    } else {
                        throw new IllegalArgumentException("Found unhandled strategy type of "+strategyType);
                    }


                    newIStrategies.getStrategy().add(newStrategy);
                }

                newActions.setInstructionalStrategies(newIStrategies);
            }

            //State transitions
            if(actions.getStateTransitions() != null){

                generated.v7.dkf.Actions.StateTransitions sTransitions = actions.getStateTransitions();
                generated.v8.dkf.Actions.StateTransitions newSTransitions = new generated.v8.dkf.Actions.StateTransitions();

                for(generated.v7.dkf.Actions.StateTransitions.StateTransition sTransition : sTransitions.getStateTransition()){

                    generated.v8.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v8.dkf.Actions.StateTransitions.StateTransition();

                    generated.v8.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v8.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();

                    newSTransition.setName(sTransition.getName());

                    //State type
                    for (Object stateType : sTransition.getLogicalExpression().getStateType()) {
                        if(stateType instanceof generated.v7.dkf.LearnerStateTransitionEnum){

                            generated.v7.dkf.LearnerStateTransitionEnum stateEnum = (generated.v7.dkf.LearnerStateTransitionEnum)stateType;

                            generated.v8.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v8.dkf.LearnerStateTransitionEnum();
                            learnerStateTrans.setAttribute(stateEnum.getAttribute());
                            learnerStateTrans.setPrevious(stateEnum.getPrevious());
                            learnerStateTrans.setCurrent(stateEnum.getCurrent());

                            if(stateEnum.getCurrent() != null && stateEnum.getCurrent().equals("Any")) {
                                learnerStateTrans.setCurrent(null);
                            }

                            if(stateEnum.getPrevious() != null && stateEnum.getPrevious().equals("Any")) {
                                learnerStateTrans.setPrevious(null);
                            }

                            newLogicalExpression.getStateType().add(learnerStateTrans);

                        }else if(stateType instanceof generated.v7.dkf.PerformanceNode){

                            generated.v7.dkf.PerformanceNode perfNode = (generated.v7.dkf.PerformanceNode)stateType;

                            generated.v8.dkf.PerformanceNode newPerfNode = new generated.v8.dkf.PerformanceNode();
                            newPerfNode.setName(perfNode.getName());
                            newPerfNode.setNodeId(perfNode.getNodeId());
                            newPerfNode.setCurrent(perfNode.getCurrent());
                            newPerfNode.setPrevious(perfNode.getPrevious());

                            newLogicalExpression.getStateType().add(newPerfNode);

                        }else{
                            throw new IllegalArgumentException("Found unhandled action's state transition state type of "+stateType);
                        }
                    }

                    newSTransition.setLogicalExpression(newLogicalExpression);

                    //Strategy Choices
                    generated.v8.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v8.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    for(generated.v7.dkf.StrategyRef strategyRef : sTransition.getStrategyChoices().getStrategyRef()){

                        generated.v8.dkf.StrategyRef newStrategyRef = new generated.v8.dkf.StrategyRef();
                        newStrategyRef.setName(strategyRef.getName());

                        newStrategyChoices.getStrategies().add(newStrategyRef);
                    }
                    newSTransition.setStrategyChoices(newStrategyChoices);

                    newSTransitions.getStateTransition().add(newSTransition);
                }

                newActions.setStateTransitions(newSTransitions);
            }

            newScenario.setActions(newActions);
        }

        return super.convertScenario(newScenario, showCompletionDialog);
    }

    /**
     * Convert a previous AutoTutor SKO element to the newer schema version.
     *
     * @param prevATSKO the previous element to convert its content to the newer element.  If null this returns null.
     * @return the new AutoTutor SKO element for the new schema version.
     */
    private static generated.v8.dkf.AutoTutorSKO convertAutoTutorSKO(generated.v7.dkf.AutoTutorSKO prevATSKO){

        if(prevATSKO == null){
            return null;
        }

        generated.v8.dkf.AutoTutorSKO newAutoTutorSKO = new generated.v8.dkf.AutoTutorSKO();
        if(prevATSKO.getScript() instanceof generated.v7.dkf.LocalSKO){

            generated.v7.dkf.LocalSKO localSKO = (generated.v7.dkf.LocalSKO) prevATSKO.getScript();
            generated.v8.dkf.LocalSKO newLocalSKO = new generated.v8.dkf.LocalSKO();

            newLocalSKO.setFile(localSKO.getFile());
            newAutoTutorSKO.setScript(newLocalSKO);

        }else if(prevATSKO.getScript() instanceof generated.v7.dkf.ATRemoteSKO) {
            generated.v7.dkf.ATRemoteSKO prevLocalSKO = (generated.v7.dkf.ATRemoteSKO)prevATSKO.getScript();

            generated.v8.dkf.ATRemoteSKO.URL newURL = new generated.v8.dkf.ATRemoteSKO.URL();
            newURL.setAddress(prevLocalSKO.getURL().getAddress());

            generated.v8.dkf.ATRemoteSKO newATRemoteSKO = new generated.v8.dkf.ATRemoteSKO();
            newATRemoteSKO.setURL(newURL);
            newAutoTutorSKO.setScript(newATRemoteSKO);

        } else {
            throw new IllegalArgumentException("Found unhandled AutoTutor script reference type of "+prevATSKO.getScript());
        }

        return newAutoTutorSKO;
    }


    /**
     * Convert a Message object for scenarios
     *
     * @param message - the message to convert
     * @return a new Message object
     */
    private generated.v8.dkf.Message convertMessage(generated.v7.dkf.Message message) {
        generated.v8.dkf.Message newMessage = new generated.v8.dkf.Message();
        newMessage.setContent(message.getContent());

        if(message.getDelivery() != null){
            generated.v8.dkf.Message.Delivery newDelivery = new generated.v8.dkf.Message.Delivery();

            if(message.getDelivery().getInTrainingApplication() != null){
                generated.v8.dkf.Message.Delivery.InTrainingApplication newInTrainingApp = new generated.v8.dkf.Message.Delivery.InTrainingApplication();
                newInTrainingApp.setEnabled(generated.v8.dkf.BooleanEnum.fromValue(message.getDelivery().getInTrainingApplication().getEnabled().toString().toLowerCase()));
                newDelivery.setInTrainingApplication(newInTrainingApp);
            }
            if(message.getDelivery().getInTutor() != null){
                generated.v8.dkf.InTutor newInTutor = new generated.v8.dkf.InTutor();
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
     * @return generated.v7.dkf.Entities - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v8.dkf.Entities convertEntities(generated.v7.dkf.Entities entities) throws IllegalArgumentException{

        generated.v8.dkf.Entities newEntities = new generated.v8.dkf.Entities();
        for(generated.v7.dkf.StartLocation location : entities.getStartLocation()){

            generated.v8.dkf.StartLocation newLocation = new generated.v8.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }

        return newEntities;
    }

    /**
     * Convert a path object into a new path object.
     *
     * @param path - the object to convert
     * @return generated.v7.dkf.Path - the new object
     */
    private static generated.v8.dkf.Path convertPath(generated.v7.dkf.Path path){

        generated.v8.dkf.Path newPath = new generated.v8.dkf.Path();
        for(generated.v7.dkf.Segment segment : path.getSegment()){

            generated.v8.dkf.Segment newSegment = new generated.v8.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());

            generated.v8.dkf.Start start = new generated.v8.dkf.Start();
            start.setWaypoint(segment.getStart().getWaypoint());
            newSegment.setStart(start);

            generated.v8.dkf.End end = new generated.v8.dkf.End();
            end.setWaypoint(segment.getEnd().getWaypoint());
            newSegment.setEnd(end);

            newPath.getSegment().add(newSegment);
        }

        return newPath;
    }

    /**
     * Convert a checkpoint object into a new checkpoint object.
     *
     * @param checkpoint - the object to convert
     * @return generated.v7.dkf.Checkpoint - the new object
     */
    private static generated.v8.dkf.Checkpoint convertCheckpoint(generated.v7.dkf.Checkpoint checkpoint){

        generated.v8.dkf.Checkpoint newCheckpoint = new generated.v8.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setWaypoint(checkpoint.getWaypoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());

        return newCheckpoint;
    }

    /**
     * Convert an evaluators object into a new evaluators object.
     *
     * @param evaluators - the object to convert
     * @return the new object
     */
    private static generated.v8.dkf.Evaluators convertEvaluators(generated.v7.dkf.Evaluators evaluators){

        generated.v8.dkf.Evaluators newEvaluators = new generated.v8.dkf.Evaluators();
        for(generated.v7.dkf.Evaluator evaluator : evaluators.getEvaluator()){

            generated.v8.dkf.Evaluator newEvaluator = new generated.v8.dkf.Evaluator();
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
    private static List<generated.v8.dkf.HasMovedExcavatorComponentInput.Component> convertComponents(
            List<generated.v7.dkf.HasMovedExcavatorComponentInput.Component> oldCompList) {

        List<generated.v8.dkf.HasMovedExcavatorComponentInput.Component> componentList =
                new ArrayList<generated.v8.dkf.HasMovedExcavatorComponentInput.Component>();

        for(generated.v7.dkf.HasMovedExcavatorComponentInput.Component oldComp : oldCompList) {
             generated.v8.dkf.HasMovedExcavatorComponentInput.Component newComp = new generated.v8.dkf.HasMovedExcavatorComponentInput.Component();
             newComp.setComponentType(generated.v8.dkf.ExcavatorComponentEnum.fromValue(oldComp.getComponentType().value()));

             if(oldComp.getDirectionType() instanceof generated.v7.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional){
                 generated.v7.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional oldBiDirectional = (generated.v7.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional)oldComp.getDirectionType();
                 generated.v8.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional newBiDirectional = new generated.v8.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional();
                 newBiDirectional.setNegativeRotation(oldBiDirectional.getNegativeRotation());
                 newBiDirectional.setPositiveRotation(oldBiDirectional.getPositiveRotation());
                 newComp.setDirectionType(newBiDirectional);
             }else{
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
    private static generated.v8.dkf.Assessments convertAssessments(generated.v7.dkf.Assessments assessments){

        generated.v8.dkf.Assessments newAssessments = new generated.v8.dkf.Assessments();

        List<generated.v7.dkf.Assessments.Survey> surveys = new ArrayList<generated.v7.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
            if (assessmentType instanceof generated.v7.dkf.Assessments.Survey) {
                surveys.add((generated.v7.dkf.Assessments.Survey) assessmentType);
            }
        }
        for(generated.v7.dkf.Assessments.Survey survey : surveys){

            generated.v8.dkf.Assessments.Survey newSurvey = new generated.v8.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());

            generated.v8.dkf.Questions newQuestions = new generated.v8.dkf.Questions();
            for(generated.v7.dkf.Question question : survey.getQuestions().getQuestion()){

                generated.v8.dkf.Question newQuestion = new generated.v8.dkf.Question();
                newQuestion.setKey(question.getKey());

                for(generated.v7.dkf.Reply reply : question.getReply()){

                    generated.v8.dkf.Reply newReply = new generated.v8.dkf.Reply();
                    newReply.setKey(reply.getKey());
                    newReply.setResult(reply.getResult());

                    newQuestion.getReply().add(newReply);
                }

                newQuestions.getQuestion().add(newQuestion);
            }

            newSurvey.setQuestions(newQuestions);

            newAssessments.getAssessmentTypes().add(newSurvey);
        }


        return newAssessments;
    }

    /**
     * Convert a collection of start trigger objects into the new schema version.
     *
     * @param list - collection of trigger objects to convert
     * @return List<generated.v8.dkf.StartTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v8.dkf.StartTriggers.Trigger> convertStartTriggers(List<generated.v7.dkf.StartTriggers.Trigger> list) throws IllegalArgumentException{

        List<generated.v8.dkf.StartTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(generated.v7.dkf.StartTriggers.Trigger triggerObj : list){

            generated.v8.dkf.StartTriggers.Trigger trigger = new generated.v8.dkf.StartTriggers.Trigger();
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
     * @return List<generated.v8.dkf.EndTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v8.dkf.EndTriggers.Trigger> convertEndTriggers(List<generated.v7.dkf.EndTriggers.Trigger> list) throws IllegalArgumentException{

        List<generated.v8.dkf.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(generated.v7.dkf.EndTriggers.Trigger triggerObj : list){

            generated.v8.dkf.EndTriggers.Trigger trigger = new generated.v8.dkf.EndTriggers.Trigger();
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
     * @return List<generated.v8.dkf.Scenario.EndTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v8.dkf.Scenario.EndTriggers.Trigger> convertScenarioEndTriggers(List<generated.v7.dkf.Scenario.EndTriggers.Trigger> list) throws IllegalArgumentException{

        List<generated.v8.dkf.Scenario.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(generated.v7.dkf.Scenario.EndTriggers.Trigger triggerObj : list){

            generated.v8.dkf.Scenario.EndTriggers.Trigger trigger = new generated.v8.dkf.Scenario.EndTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj.getTriggerType()));
            trigger.setTriggerDelay(triggerObj.getTriggerDelay());

            newTriggerObjects.add(trigger);
        }

        return newTriggerObjects;
    }


    /**
     * Convert a collection of trigger objects (start or end triggers) into the new schema version.
     *
     * @param list - collection of trigger objects to convert
     * @return List<Object> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static Serializable convertTrigger(Serializable triggerObj) throws IllegalArgumentException{

        if(triggerObj instanceof generated.v7.dkf.EntityLocation){

            generated.v7.dkf.EntityLocation entityLocation = (generated.v7.dkf.EntityLocation)triggerObj;
            generated.v8.dkf.EntityLocation newEntityLocation = new generated.v8.dkf.EntityLocation();

            generated.v8.dkf.StartLocation startLocation = new generated.v8.dkf.StartLocation();
            startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
            newEntityLocation.setStartLocation(startLocation);

            generated.v8.dkf.TriggerLocation triggerLocation = new generated.v8.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);

            return newEntityLocation;


        }else if(triggerObj instanceof generated.v7.dkf.LearnerLocation){

            generated.v7.dkf.LearnerLocation learnerLocation = (generated.v7.dkf.LearnerLocation)triggerObj;
            generated.v8.dkf.LearnerLocation newLearnerLocation = new generated.v8.dkf.LearnerLocation();

            newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));

            return newLearnerLocation;


        }else if(triggerObj instanceof generated.v7.dkf.ConceptEnded){

            generated.v7.dkf.ConceptEnded conceptEnded = (generated.v7.dkf.ConceptEnded)triggerObj;
            generated.v8.dkf.ConceptEnded newConceptEnded = new generated.v8.dkf.ConceptEnded();

            newConceptEnded.setNodeId(conceptEnded.getNodeId());

            return newConceptEnded;


        }else if(triggerObj instanceof generated.v7.dkf.ChildConceptEnded){

            generated.v7.dkf.ChildConceptEnded childConceptEnded = (generated.v7.dkf.ChildConceptEnded)triggerObj;
            generated.v8.dkf.ChildConceptEnded newChildConceptEnded = new generated.v8.dkf.ChildConceptEnded();

            newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());

            return newChildConceptEnded;


        } else if(triggerObj instanceof generated.v7.dkf.TaskEnded){

            generated.v7.dkf.TaskEnded taskEnded = (generated.v7.dkf.TaskEnded)triggerObj;
            generated.v8.dkf.TaskEnded newTaskEnded = new generated.v8.dkf.TaskEnded();

            newTaskEnded.setNodeId(taskEnded.getNodeId());

            return newTaskEnded;


        } else if(triggerObj instanceof generated.v7.dkf.ConceptAssessment){

            generated.v7.dkf.ConceptAssessment conceptAssessment = (generated.v7.dkf.ConceptAssessment)triggerObj;
            generated.v8.dkf.ConceptAssessment newConceptAssessment = new generated.v8.dkf.ConceptAssessment();

            newConceptAssessment.setResult(conceptAssessment.getResult());
            newConceptAssessment.setConcept(conceptAssessment.getConcept());

            return newConceptAssessment;


        } else{
            throw new IllegalArgumentException("Found unhandled trigger type of "+triggerObj);
        }

    }

    /**
     * Convert a coordinate object into the latest schema version.
     *
     * @param coordinate - coordinate object to convert
     * @return generated.v8.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException
     */
    private static generated.v8.dkf.Coordinate convertCoordinate(generated.v7.dkf.Coordinate coordinate) throws IllegalArgumentException{

        generated.v8.dkf.Coordinate newCoord = new generated.v8.dkf.Coordinate();

        Object coordType = coordinate.getType();
        if(coordType instanceof generated.v7.dkf.GCC){

            generated.v7.dkf.GCC gcc = (generated.v7.dkf.GCC)coordType;
            generated.v8.dkf.GCC newGCC = new generated.v8.dkf.GCC();

            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());

            newCoord.setType(newGCC);

        }else if(coordType instanceof generated.v7.dkf.GDC){
           // generated.
            generated.v7.dkf.GDC gdc = (generated.v7.dkf.GDC)coordType;
            generated.v8.dkf.GDC newGDC = new generated.v8.dkf.GDC();

            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());

            newCoord.setType(newGDC);

        }else if(coordType instanceof generated.v7.dkf.VBSAGL){

            generated.v7.dkf.VBSAGL agl = (generated.v7.dkf.VBSAGL)coordType;
            generated.v8.dkf.VBSAGL newAGL = new generated.v8.dkf.VBSAGL();

            newAGL.setX(agl.getX());
            newAGL.setY(agl.getY());
            newAGL.setZ(agl.getZ());

            newCoord.setType(newAGL);

        }else{
            throw new IllegalArgumentException("Found unhandled coordinate type of "+coordType);
        }

        return newCoord;
    }

    /**
     * Convert a strategy handler object to a new version of the strategy handler object.
     *
     * @param handler - the object to convert
     * @return generated.v8.dkf.StrategyHandler - the new object
     */
    private static generated.v8.dkf.StrategyHandler convertStrategyHandler(generated.v7.dkf.StrategyHandler handler) {

        generated.v8.dkf.StrategyHandler newHandler = new generated.v8.dkf.StrategyHandler();

        if(handler.getParams() != null) {

            generated.v8.dkf.StrategyHandler.Params newParams = new generated.v8.dkf.StrategyHandler.Params();
            generated.v8.dkf.Nvpair nvpair = new generated.v8.dkf.Nvpair();

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
     * @return generated.v8.dkf.Concepts - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v8.dkf.Concepts convertConcepts(generated.v7.dkf.Concepts concepts) throws IllegalArgumentException{

        generated.v8.dkf.Concepts newConcepts = new generated.v8.dkf.Concepts();
        for(generated.v7.dkf.Concept concept : concepts.getConcept()){

            generated.v8.dkf.Concept newConcept = new generated.v8.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());

            if (concept.getAssessments() != null) {
                newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }

            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if(conditionsOrConcepts instanceof generated.v7.dkf.Concepts){
                //nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v7.dkf.Concepts)conditionsOrConcepts));

            }else if(conditionsOrConcepts instanceof generated.v7.dkf.Conditions){

                generated.v8.dkf.Conditions newConditions = new generated.v8.dkf.Conditions();

                generated.v7.dkf.Conditions conditions = (generated.v7.dkf.Conditions)conditionsOrConcepts;

                for(generated.v7.dkf.Condition condition : conditions.getCondition()){

                    generated.v8.dkf.Condition newCondition = new generated.v8.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());

                    if(condition.getDefault() != null){
                        generated.v8.dkf.Default newDefault = new generated.v8.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }

                    //Input
                    generated.v8.dkf.Input newInput = new generated.v8.dkf.Input();
                    if(condition.getInput() != null){

                        Object inputType = condition.getInput().getType();

                        if(inputType == null){
                            //nothing to do right now

                        }else if(inputType instanceof generated.v7.dkf.ApplicationCompletedCondition){

                            generated.v7.dkf.ApplicationCompletedCondition conditionInput = (generated.v7.dkf.ApplicationCompletedCondition)inputType;

                            generated.v8.dkf.ApplicationCompletedCondition newConditionInput = new generated.v8.dkf.ApplicationCompletedCondition();

                            if (conditionInput.getIdealCompletionDuration() != null) {
                                newConditionInput.setIdealCompletionDuration(conditionInput.getIdealCompletionDuration());
                            }

                            newInput.setType(newConditionInput);

                        }else if (inputType instanceof generated.v7.dkf.AutoTutorConditionInput){

                            generated.v7.dkf.AutoTutorConditionInput conditionInput = (generated.v7.dkf.AutoTutorConditionInput)inputType;

                            generated.v8.dkf.AutoTutorConditionInput newConditionInput = new generated.v8.dkf.AutoTutorConditionInput();

                            if (conditionInput.getAutoTutorSKO() != null) {

                                generated.v7.dkf.AutoTutorSKO prevAutoTutorSKO = conditionInput.getAutoTutorSKO();
                                generated.v8.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevAutoTutorSKO);
                                newConditionInput.setAutoTutorSKO(newAutoTutorSKO);
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.AvoidLocationCondition){

                            generated.v7.dkf.AvoidLocationCondition conditionInput = (generated.v7.dkf.AvoidLocationCondition)inputType;

                            generated.v8.dkf.AvoidLocationCondition newConditionInput = new generated.v8.dkf.AvoidLocationCondition();

                            if(conditionInput.getWaypointRef() != null){
                                newConditionInput.setWaypointRef(convertWaypointRef(conditionInput.getWaypointRef()));
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.CheckpointPaceCondition){

                            generated.v7.dkf.CheckpointPaceCondition conditionInput = (generated.v7.dkf.CheckpointPaceCondition)inputType;

                            generated.v8.dkf.CheckpointPaceCondition newConditionInput = new generated.v8.dkf.CheckpointPaceCondition();
                            for(generated.v7.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.CheckpointProgressCondition){

                            generated.v7.dkf.CheckpointProgressCondition conditionInput = (generated.v7.dkf.CheckpointProgressCondition)inputType;

                            generated.v8.dkf.CheckpointProgressCondition newConditionInput = new generated.v8.dkf.CheckpointProgressCondition();
                            for(generated.v7.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.CorridorBoundaryCondition){

                            generated.v7.dkf.CorridorBoundaryCondition conditionInput = (generated.v7.dkf.CorridorBoundaryCondition)inputType;

                            generated.v8.dkf.CorridorBoundaryCondition newConditionInput = new generated.v8.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.CorridorPostureCondition){

                            generated.v7.dkf.CorridorPostureCondition conditionInput = (generated.v7.dkf.CorridorPostureCondition)inputType;

                            generated.v8.dkf.CorridorPostureCondition newConditionInput = new generated.v8.dkf.CorridorPostureCondition();
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));

                            generated.v8.dkf.Postures postures = new generated.v8.dkf.Postures();
                            /*
                            for(generated.v7.dkf.PostureEnumType posture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(generated.v8.dkf.PostureEnumType.fromValue(posture.value()));
                            }
                            */
                            for(String strPosture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.EliminateHostilesCondition){

                            generated.v7.dkf.EliminateHostilesCondition conditionInput = (generated.v7.dkf.EliminateHostilesCondition)inputType;

                            generated.v8.dkf.EliminateHostilesCondition newConditionInput = new generated.v8.dkf.EliminateHostilesCondition();

                            if(conditionInput.getEntities() != null){
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.EnterAreaCondition){

                            generated.v7.dkf.EnterAreaCondition conditionInput = (generated.v7.dkf.EnterAreaCondition)inputType;

                            generated.v8.dkf.EnterAreaCondition newConditionInput = new generated.v8.dkf.EnterAreaCondition();

                            for(generated.v7.dkf.Entrance entrance : conditionInput.getEntrance()){

                                generated.v8.dkf.Entrance newEntrance = new generated.v8.dkf.Entrance();

                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());

                                generated.v8.dkf.Inside newInside = new generated.v8.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setWaypoint(entrance.getInside().getWaypoint());
                                newEntrance.setInside(newInside);

                                generated.v8.dkf.Outside newOutside = new generated.v8.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setWaypoint(entrance.getOutside().getWaypoint());
                                newEntrance.setOutside(newOutside);

                                newConditionInput.getEntrance().add(newEntrance);
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.ExplosiveHazardSpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v7.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v7.dkf.ExplosiveHazardSpotReportCondition)inputType;

                            generated.v8.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v8.dkf.ExplosiveHazardSpotReportCondition();

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.GenericConditionInput) {

                            generated.v7.dkf.GenericConditionInput conditionInput = (generated.v7.dkf.GenericConditionInput)inputType;

                            generated.v8.dkf.GenericConditionInput newConditionInput = new generated.v8.dkf.GenericConditionInput();

                            if(conditionInput.getNvpair() != null){
                                for (generated.v7.dkf.Nvpair nvPair : conditionInput.getNvpair()) {
                                    newConditionInput.getNvpair().add(convertNvpair(nvPair));
                                }
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.HasMovedExcavatorComponentInput){

                            generated.v7.dkf.HasMovedExcavatorComponentInput conditionInput = (generated.v7.dkf.HasMovedExcavatorComponentInput)inputType;
                            generated.v8.dkf.HasMovedExcavatorComponentInput newConditionInput = new generated.v8.dkf.HasMovedExcavatorComponentInput();

                            newConditionInput.getComponent().addAll(convertComponents(conditionInput.getComponent()));
                            newConditionInput.setMaxAssessments(conditionInput.getMaxAssessments());

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.IdentifyPOIsCondition){

                            generated.v7.dkf.IdentifyPOIsCondition conditionInput = (generated.v7.dkf.IdentifyPOIsCondition)inputType;

                            generated.v8.dkf.IdentifyPOIsCondition newConditionInput = new generated.v8.dkf.IdentifyPOIsCondition();

                            if(conditionInput.getPois() != null){

                                generated.v8.dkf.Pois pois = new generated.v8.dkf.Pois();
                                for(generated.v7.dkf.WaypointRef waypointRef : conditionInput.getPois().getWaypointRef()){
                                    pois.getWaypointRef().add(convertWaypointRef(waypointRef));
                                }

                                newConditionInput.setPois(pois);
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.LifeformTargetAccuracyCondition){

                            generated.v7.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v7.dkf.LifeformTargetAccuracyCondition)inputType;

                            generated.v8.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v8.dkf.LifeformTargetAccuracyCondition();

                            if(conditionInput.getEntities() != null){
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            newInput.setType(newConditionInput);

                        }else if (inputType instanceof generated.v7.dkf.MarksmanshipPrecisionCondition) {

                            generated.v7.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v7.dkf.MarksmanshipPrecisionCondition)inputType;

                            generated.v8.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v8.dkf.MarksmanshipPrecisionCondition();

                            if(conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        }else if (inputType instanceof generated.v7.dkf.MarksmanshipSessionCompleteCondition) {

                            generated.v7.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v7.dkf.MarksmanshipSessionCompleteCondition)inputType;

                            generated.v8.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v8.dkf.MarksmanshipSessionCompleteCondition();

                            if(conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.NineLineReportCondition){

                            @SuppressWarnings("unused")
                            generated.v7.dkf.NineLineReportCondition conditionInput = (generated.v7.dkf.NineLineReportCondition)inputType;

                            generated.v8.dkf.NineLineReportCondition newConditionInput = new generated.v8.dkf.NineLineReportCondition();

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.NumberOfShotsFiredCondition){

                            generated.v7.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v7.dkf.NumberOfShotsFiredCondition)inputType;

                            generated.v8.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v8.dkf.NumberOfShotsFiredCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.PowerPointDwellCondition){

                            generated.v7.dkf.PowerPointDwellCondition conditionInput = (generated.v7.dkf.PowerPointDwellCondition)inputType;

                            generated.v8.dkf.PowerPointDwellCondition newConditionInput = new generated.v8.dkf.PowerPointDwellCondition();

                            generated.v8.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v8.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);

                            generated.v8.dkf.PowerPointDwellCondition.Slides slides = new generated.v8.dkf.PowerPointDwellCondition.Slides();
                            for(generated.v7.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput.getSlides().getSlide()){

                                generated.v8.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v8.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());

                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.RulesOfEngagementCondition){

                            generated.v7.dkf.RulesOfEngagementCondition conditionInput = (generated.v7.dkf.RulesOfEngagementCondition)inputType;

                            generated.v8.dkf.RulesOfEngagementCondition newConditionInput = new generated.v8.dkf.RulesOfEngagementCondition();
                            generated.v8.dkf.Wcs newWCS = new generated.v8.dkf.Wcs();
                            newWCS.setValue(generated.v8.dkf.WeaponControlStatusEnum.fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);

                            newInput.setType(newConditionInput);

                        }else if (inputType instanceof generated.v7.dkf.SIMILEConditionInput){

                            generated.v7.dkf.SIMILEConditionInput conditionInput = (generated.v7.dkf.SIMILEConditionInput)inputType;

                            generated.v8.dkf.SIMILEConditionInput newConditionInput = new generated.v8.dkf.SIMILEConditionInput();

                            if (conditionInput.getConditionKey() != null) {
                                newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }

                            if (conditionInput.getConfigurationFile() != null) {
                                newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.SpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v7.dkf.SpotReportCondition conditionInput = (generated.v7.dkf.SpotReportCondition)inputType;

                            generated.v8.dkf.SpotReportCondition newConditionInput = new generated.v8.dkf.SpotReportCondition();

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.TimerConditionInput){

                            generated.v7.dkf.TimerConditionInput conditionInput = (generated.v7.dkf.TimerConditionInput)inputType;

                            generated.v8.dkf.TimerConditionInput newConditionInput = new generated.v8.dkf.TimerConditionInput();

                            newConditionInput.setRepeatable(generated.v8.dkf.BooleanEnum.fromValue(conditionInput.getRepeatable().toString().toLowerCase()));
                            newConditionInput.setInterval(conditionInput.getInterval());

                            newInput.setType(newConditionInput);

                        }else if(inputType instanceof generated.v7.dkf.UseRadioCondition){

                            @SuppressWarnings("unused")
                            generated.v7.dkf.UseRadioCondition conditionInput = (generated.v7.dkf.UseRadioCondition)inputType;

                            generated.v8.dkf.UseRadioCondition newConditionInput = new generated.v8.dkf.UseRadioCondition();

                            newInput.setType(newConditionInput);

                        }else{
                            throw new IllegalArgumentException("Found unhandled condition input type of "+inputType);
                        }

                    }
                    newCondition.setInput(newInput);

                    //Scoring
                    generated.v8.dkf.Scoring newScoring = new generated.v8.dkf.Scoring();
                    if(condition.getScoring() != null){
                        // Only add the scoring element if it has children.
                        // As of version 5, there cannot be a scoring element with no children
                        if (!condition.getScoring().getType().isEmpty()) {

                            for(Object scoringType : condition.getScoring().getType()){

                                if(scoringType instanceof generated.v7.dkf.Count){

                                    generated.v7.dkf.Count count = (generated.v7.dkf.Count)scoringType;

                                    generated.v8.dkf.Count newCount = new generated.v8.dkf.Count();
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v8.dkf.UnitsEnumType.fromValue(count.getUnits().value()));

                                    if(count.getEvaluators() != null){
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }

                                    newScoring.getType().add(newCount);

                                }else if(scoringType instanceof generated.v7.dkf.CompletionTime){

                                    generated.v7.dkf.CompletionTime complTime = (generated.v7.dkf.CompletionTime)scoringType;

                                    generated.v8.dkf.CompletionTime newComplTime = new generated.v8.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(generated.v8.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if(complTime.getEvaluators() != null){
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }

                                    newScoring.getType().add(newComplTime);

                                }else if(scoringType instanceof generated.v7.dkf.ViolationTime){

                                    generated.v7.dkf.ViolationTime violationTime = (generated.v7.dkf.ViolationTime)scoringType;

                                    generated.v8.dkf.ViolationTime newViolationTime = new generated.v8.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(generated.v8.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
                                    if(violationTime.getEvaluators() != null){
                                        newViolationTime.setEvaluators(convertEvaluators(violationTime.getEvaluators()));
                                    }

                                    newScoring.getType().add(newViolationTime);

                                }else{
                                    throw new IllegalArgumentException("Found unhandled scoring type of "+scoringType);
                                }
                            }

                            newCondition.setScoring(newScoring);
                        }
                    }

                    newConditions.getCondition().add(newCondition);
                }

                newConcept.setConditionsOrConcepts(newConditions);

            }else{
                throw new IllegalArgumentException("Found unhandled subconcept node type of "+conditionsOrConcepts);
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
    private static generated.v8.dkf.Nvpair convertNvpair(generated.v7.dkf.Nvpair nvPair) {

        generated.v8.dkf.Nvpair newNvpair = new generated.v8.dkf.Nvpair();
        newNvpair.setName(nvPair.getName());
        newNvpair.setValue(nvPair.getValue());

        return newNvpair;
    }

    /**
     * Convert a waypointref object to a new waypointref object.
     *
     * @param waypointRef - the object to convert
     * @return the new object
     */
    private static generated.v8.dkf.WaypointRef convertWaypointRef(generated.v7.dkf.WaypointRef waypointRef){

        generated.v8.dkf.WaypointRef newWaypoint = new generated.v8.dkf.WaypointRef();
        newWaypoint.setValue(waypointRef.getValue());
        newWaypoint.setDistance(waypointRef.getDistance());

        return newWaypoint;
    }



    //////////////////////////////////////////////// END CONVERT DKF ////////////////////////////////////////////////////////
}
