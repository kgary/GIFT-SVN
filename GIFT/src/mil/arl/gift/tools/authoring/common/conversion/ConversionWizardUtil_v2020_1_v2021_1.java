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

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.StringUtils;

/**
 * Responsible for converting 2020-1 GIFT version XML files to 2021-1 versions when applicable, i.e.
 * if a particular schema changes enough to warrant a conversion process.
 *
 * @author mhoffman
 *
 */
public class ConversionWizardUtil_v2020_1_v2021_1 extends AbstractConversionWizardUtil {

    //////////////////////////////////////////////////////////////
    /////////// DON'T REMOVE THE ITEMS IN THIS SECTION ///////////
    //////////////////////////////////////////////////////////////

    /** The new version number */
    private static final String VERSION_NUMBER = "11.0.1";

    @Override
    public String getConvertedVersionNumber() {
        return VERSION_NUMBER;
    }

    /********* PREVIOUS SCHEMA FILES *********/

    /** Path to the specific version folder */
    private static final String versionPathPrefix = StringUtils.join(File.separator,
            Arrays.asList("data", "conversionWizard", "v2020_1"));

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
    public static final Class<?> PREV_COURSE_ROOT = generated.v10.course.Course.class;

    /** Previous DKF schema root */
    public static final Class<?> PREV_DKF_ROOT = generated.v10.dkf.Scenario.class;

    /** Previous metadata schema root */
    public static final Class<?> PREV_METADATA_ROOT = generated.v10.metadata.Metadata.class;

    /** Previous learner schema root */
    public static final Class<?> PREV_LEARNER_ROOT = generated.v10.learner.LearnerConfiguration.class;

    /** Previous pedagogical schema root */
    public static final Class<?> PREV_PEDAGOGICAL_ROOT = generated.v10.ped.EMAP.class;

    /** Previous sensor schema root */
    public static final Class<?> PREV_SENSOR_ROOT = generated.v10.sensor.SensorsConfiguration.class;

    /** Previous training App schema root */
    public static final Class<?> PREV_TRAINING_APP_ROOT = generated.v10.course.TrainingApplicationWrapper.class;

    /** Previous conversation schema root */
    public static final Class<?> PREV_CONVERSATION_ROOT = generated.v10.conversation.Conversation.class;

    /** Previous lesson Material schema root */
    public static final Class<?> PREV_LESSON_MATERIAL_ROOT = generated.v10.course.LessonMaterialList.class;

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
    
    /******************* Convert Metadata ***************/
    
    @Override
    public UnmarshalledFile convertMetadata(FileProxy metadataFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile uFile = parseFile(metadataFile, getPreviousMetadataSchemaFile(), getPreviousMetadataSchemaRoot(), failOnFirstSchemaError);

        generated.v10.metadata.Metadata prevMetadata = (generated.v10.metadata.Metadata) uFile.getUnmarshalled();
        
        return convertMetadata(prevMetadata, showCompletionDialog);
    }
    
    @Override
    protected UnmarshalledFile convertMetadata(Serializable metadata, boolean showCompletionDialog) {
        generated.v10.metadata.Metadata prevMetadata = (generated.v10.metadata.Metadata)metadata;
        generated.metadata.Metadata newMetadata = new generated.metadata.Metadata();
        
        newMetadata.setVersion(VERSION_NUMBER);
        newMetadata.setDisplayName(prevMetadata.getDisplayName());
        
        generated.v10.metadata.PresentAt presentAt = prevMetadata.getPresentAt();
        generated.metadata.PresentAt newPresentAt = new generated.metadata.PresentAt();
        newPresentAt.setMerrillQuadrant(presentAt.getMerrillQuadrant());
        if(presentAt.getRemediationOnly() != null){
            newPresentAt.setRemediationOnly(generated.metadata.BooleanEnum.valueOf(presentAt.getRemediationOnly().name()));
        }
        
        newMetadata.setPresentAt(newPresentAt);
        
        Serializable content = prevMetadata.getContent();
        if(content instanceof generated.v10.metadata.Metadata.Simple){
            
            generated.v10.metadata.Metadata.Simple prevSimple = (generated.v10.metadata.Metadata.Simple)content;
            generated.metadata.Metadata.Simple simple = new generated.metadata.Metadata.Simple();
            simple.setValue(prevSimple.getValue());
            newMetadata.setContent(simple);
            
        }else if(content instanceof generated.v10.metadata.Metadata.TrainingApp){
            
            generated.v10.metadata.Metadata.TrainingApp prevTApp = (generated.v10.metadata.Metadata.TrainingApp)content;
            generated.metadata.Metadata.TrainingApp tApp = new generated.metadata.Metadata.TrainingApp();
            tApp.setValue(prevTApp.getValue());
            newMetadata.setContent(tApp);
            
        }else if(content instanceof generated.v10.metadata.Metadata.LessonMaterial){
            
            generated.v10.metadata.Metadata.LessonMaterial prevLM = (generated.v10.metadata.Metadata.LessonMaterial)content;
            generated.metadata.Metadata.LessonMaterial lessonMaterial = new generated.metadata.Metadata.LessonMaterial();
            lessonMaterial.setValue(prevLM.getValue());
            newMetadata.setContent(lessonMaterial);
            
        }else if(content instanceof generated.v10.metadata.Metadata.URL){
            
            generated.v10.metadata.Metadata.URL prevURL = (generated.v10.metadata.Metadata.URL)content;
            generated.metadata.Metadata.URL url = new generated.metadata.Metadata.URL();
            url.setValue(prevURL.getValue());
            newMetadata.setContent(url);
            
        }else{
            throw new IllegalArgumentException("Found unhandled metadata content type of "+content);
        }

        newMetadata.setConcepts(convertConcepts(prevMetadata.getConcepts()));
        
        return super.convertMetadata(newMetadata, showCompletionDialog);
    }
    
    private static generated.metadata.Metadata.Concepts convertConcepts(generated.v10.metadata.Metadata.Concepts oldConcepts) {
        generated.metadata.Metadata.Concepts newConcepts = new generated.metadata.Metadata.Concepts();

        for(generated.v10.metadata.Concept concept: oldConcepts.getConcept()){
            generated.metadata.Concept newConcept = new generated.metadata.Concept();
            newConcept.setName(concept.getName());
            
            generated.v10.metadata.ActivityType activityType = concept.getActivityType();
            generated.metadata.ActivityType newActType = new generated.metadata.ActivityType();

            Serializable type = activityType.getType();
            Serializable newType;
            if(type instanceof generated.v10.metadata.ActivityType.Interactive){
                // added 2020-1 v10
                
                @SuppressWarnings("unused")
                generated.v10.metadata.ActivityType.Interactive interactive = (generated.v10.metadata.ActivityType.Interactive)type;
                generated.metadata.ActivityType.Interactive newInteractive = new generated.metadata.ActivityType.Interactive();
                newType = newInteractive;
                
            }else if(type instanceof generated.v10.metadata.ActivityType.Constructive){
                
                @SuppressWarnings("unused")
                generated.v10.metadata.ActivityType.Constructive constructive = (generated.v10.metadata.ActivityType.Constructive)type;
                generated.metadata.ActivityType.Constructive newConstructive = new generated.metadata.ActivityType.Constructive();
                newType = newConstructive;
                
            }else if(type instanceof generated.v10.metadata.ActivityType.Active){
                
                @SuppressWarnings("unused")
                generated.v10.metadata.ActivityType.Active active = (generated.v10.metadata.ActivityType.Active)type;
                generated.metadata.ActivityType.Active newActive = new generated.metadata.ActivityType.Active();
                newType = newActive;
                
            }else if(type instanceof generated.v10.metadata.ActivityType.Passive){
                
                generated.v10.metadata.ActivityType.Passive passive = (generated.v10.metadata.ActivityType.Passive)type;
                generated.metadata.ActivityType.Passive newPassive = new generated.metadata.ActivityType.Passive();
                newPassive.setAttributes(convertMetaDataAttributes(passive.getAttributes()));
                newType = newPassive;
                
            }else{
                throw new IllegalArgumentException("Found unhandled metadata concept activity type of "+type);
            }
            
            newActType.setType(newType);
            newConcept.setActivityType(newActType);

            newConcepts.getConcept().add(newConcept);
        }

        return newConcepts;
    }
    
    private static generated.metadata.Attributes convertMetaDataAttributes(generated.v10.metadata.Attributes oldAttributes) {
        generated.metadata.Attributes newMetaAttributes = new generated.metadata.Attributes();
        int index = 0;

        for(generated.v10.metadata.Attribute attribute : oldAttributes.getAttribute()){
            newMetaAttributes.getAttribute().add(new generated.metadata.Attribute());
            newMetaAttributes.getAttribute().get(index).setValue(attribute.getValue());

            index++;
        }

        return newMetaAttributes;
    }

    /******************* CONVERT DKF *******************/

    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError)
            throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {

        UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(),
                failOnFirstSchemaError);
        generated.v10.dkf.Scenario prevScenario = (generated.v10.dkf.Scenario) uFile.getUnmarshalled();

        return convertScenario(prevScenario, showCompletionDialog);
    }
    
    /**
     * Convert the old team organization object into the new team organization object
     * @param teamOrg the old team org object to convert
     * @return the new team org object
     */
    private generated.dkf.TeamOrganization convertTeamOrganization(generated.v10.dkf.TeamOrganization teamOrg){
        
        generated.dkf.TeamOrganization newTeamOrg = new generated.dkf.TeamOrganization();
        
        generated.v10.dkf.Team rootTeam = teamOrg.getTeam();
        generated.dkf.Team newRootTeam = new generated.dkf.Team();
        newRootTeam.setEchelon(rootTeam.getEchelon());
        newRootTeam.setName(rootTeam.getName());
        
        convertTeam(rootTeam, newRootTeam);
        
        newTeamOrg.setTeam(newRootTeam);
        
        return newTeamOrg;
    }
    
    /**
     * Convert the old team member references into a new team member references object 
     * @param memberRefs the old member references object to convert 
     * @return the new member references object.  If null is provided, null will be returned.
     */
    private generated.dkf.TeamMemberRefs convertTeamMemberRefs(generated.v10.dkf.TeamMemberRefs memberRefs){
        
        if(memberRefs == null){
            return null;
        }
        
        generated.dkf.TeamMemberRefs newMemberRefs = new generated.dkf.TeamMemberRefs();
        newMemberRefs.getTeamMemberRef().addAll(memberRefs.getTeamMemberRef());
        return newMemberRefs;
    }
    
    /**
     * Convert the team provided into a new team object.  Recursively walks the team hierarchy.
     * 
     * @param team the old team to convert into the new team object
     * @param newTeam the new team object
     */
    private void convertTeam(generated.v10.dkf.Team team, generated.dkf.Team newTeam){
        
        for(Serializable teamObject : team.getTeamOrTeamMember()){
            
            if(teamObject instanceof generated.v10.dkf.TeamMember){
                
                generated.v10.dkf.TeamMember teamMember = (generated.v10.dkf.TeamMember)teamObject;
                generated.dkf.TeamMember newTeamMember = new generated.dkf.TeamMember();
                newTeamMember.setLearnerId(convertLearnerId(teamMember.getLearnerId()));
                newTeamMember.setName(teamMember.getName());
                newTeamMember.setPlayable(teamMember.isPlayable());
                
                newTeam.getTeamOrTeamMember().add(newTeamMember);
            }else if(teamObject instanceof generated.v10.dkf.Team){
                
                generated.v10.dkf.Team subTeam = (generated.v10.dkf.Team)teamObject;
                generated.dkf.Team newSubTeam = new generated.dkf.Team();
                newSubTeam.setEchelon(subTeam.getEchelon());
                newSubTeam.setName(subTeam.getName());
                convertTeam(subTeam, newSubTeam);
                
                newTeam.getTeamOrTeamMember().add(newSubTeam);
            }
        }
    }
    
    /**
     * Convert a learner id object
     * @param learnerId the object to convert
     * @return the new learner id object
     */
    private generated.dkf.LearnerId convertLearnerId(generated.v10.dkf.LearnerId learnerId){
        
        generated.dkf.LearnerId newId = new generated.dkf.LearnerId();
        Serializable newType = null;
        if(learnerId.getType() instanceof generated.v10.dkf.StartLocation){
            generated.dkf.StartLocation startLocation = new generated.dkf.StartLocation();
            startLocation.setCoordinate(convertCoordinate(((generated.v10.dkf.StartLocation)learnerId.getType()).getCoordinate()));
            newType = startLocation;
            
        }else if(learnerId.getType() instanceof String){
            newType = learnerId.getType();
        }else{
            throw new IllegalArgumentException("Found unhandled learner id type of "+learnerId.getType());
        }
        
        newId.setType(newType);
        return newId;
    }

    @Override
    public UnmarshalledFile convertScenario(Serializable prevScenarioObj, boolean showCompletionDialog)
            throws IllegalArgumentException {
        generated.v10.dkf.Scenario prevScenario = (generated.v10.dkf.Scenario) prevScenarioObj;
        generated.dkf.Scenario newScenario = new generated.dkf.Scenario();

        newScenario.setDescription(prevScenario.getDescription());
        newScenario.setName(prevScenario.getName());
        newScenario.setVersion(VERSION_NUMBER);

        // Learner Id
        generated.v10.dkf.TeamOrganization teamOrg = prevScenario.getTeamOrganization();
        if (teamOrg != null) {            
            newScenario.setTeamOrganization(convertTeamOrganization(teamOrg));
        }

        // Resources
        generated.dkf.Resources newResources = new generated.dkf.Resources();
        newResources.setSurveyContext(prevScenario.getResources().getSurveyContext());

        generated.dkf.AvailableLearnerActions newALA = new generated.dkf.AvailableLearnerActions();

        if (prevScenario.getResources().getAvailableLearnerActions() != null) {

            generated.v10.dkf.AvailableLearnerActions ala = prevScenario.getResources().getAvailableLearnerActions();
            if (ala.getLearnerActionsFiles() != null) {
                generated.dkf.LearnerActionsFiles newLAF = new generated.dkf.LearnerActionsFiles();
                for (String filename : ala.getLearnerActionsFiles().getFile()) {
                    newLAF.getFile().add(filename);
                }

                newALA.setLearnerActionsFiles(newLAF);
            }

            if (ala.getLearnerActionsList() != null) {

                generated.dkf.LearnerActionsList newLAL = new generated.dkf.LearnerActionsList();
                for (generated.v10.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()) {

                    generated.dkf.LearnerAction newAction = new generated.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    
                    // if the learner action is incomplete, continue on in the conversion.  Don't
                    // bail on the conversion completely which is too harsh for this not widely used element.
                    if(action.getType() == null || action.getType().name() == null) {
                        continue;
                    }

                    generated.dkf.LearnerActionEnumType actionType;
                    try {
                        actionType = generated.dkf.LearnerActionEnumType.valueOf(action.getType().name());
                    } catch (@SuppressWarnings("unused") Exception e) {
                        throw new UnsupportedOperationException("The learner action type '" + action.getType()
                                + "' is unknown. Since this is a required field, the import can not continue.");
                    }
                    newAction.setType(actionType);
                    newAction.setDescription(action.getDescription());

                    if (action.getLearnerActionParams() != null) {

                        generated.v10.dkf.TutorMeParams tutorMeParams = action.getLearnerActionParams();
                        generated.dkf.TutorMeParams newTutorMeParams = new generated.dkf.TutorMeParams();
                        if (tutorMeParams.getConfiguration() instanceof generated.v10.dkf.ConversationTreeFile) {

                            generated.v10.dkf.ConversationTreeFile convTreeFile = (generated.v10.dkf.ConversationTreeFile) tutorMeParams
                                    .getConfiguration();
                            generated.dkf.ConversationTreeFile newConvTreeFile = new generated.dkf.ConversationTreeFile();
                            newConvTreeFile.setName(convTreeFile.getName());

                            newTutorMeParams.setConfiguration(newConvTreeFile);

                        } else if (tutorMeParams.getConfiguration() instanceof generated.v10.dkf.AutoTutorSKO) {

                            generated.v10.dkf.AutoTutorSKO atSKO = (generated.v10.dkf.AutoTutorSKO) tutorMeParams
                                    .getConfiguration();
                            generated.dkf.AutoTutorSKO newATSKO = convertAutoTutorSKO(atSKO);

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
            generated.v10.dkf.ScenarioControls scenarioControls = prevScenario.getResources().getScenarioControls();
            generated.dkf.ScenarioControls newScenarioControls = new generated.dkf.ScenarioControls();

            if (scenarioControls.getPreventManualStop() != null) {
                newScenarioControls.setPreventManualStop(new generated.dkf.PreventManualStop());
            }

            newResources.setScenarioControls(newScenarioControls);
        }
        
        // Added in 2020-1 v10
        if(prevScenario.getResources().getObserverControls() != null){
            generated.v10.dkf.ObserverControls observerControls = prevScenario.getResources().getObserverControls();
            generated.dkf.ObserverControls newObserverControls = new generated.dkf.ObserverControls();
            
            generated.v10.dkf.ObserverControls.Audio audio = observerControls.getAudio();
            generated.dkf.ObserverControls.Audio newAudio = new generated.dkf.ObserverControls.Audio();
            newAudio.setGoodPerformance(audio.getGoodPerformance());
            newAudio.setPoorPerformance(audio.getPoorPerformance());
            newObserverControls.setAudio(newAudio);
            newResources.setObserverControls(newObserverControls);
        }


        newScenario.setResources(newResources);

        // End Triggers
        generated.dkf.Scenario.EndTriggers newScenarioEndTriggers = new generated.dkf.Scenario.EndTriggers();

        if (prevScenario.getEndTriggers() != null) {
            newScenarioEndTriggers.getTrigger()
                    .addAll(convertScenarioEndTriggers(prevScenario.getEndTriggers().getTrigger()));
            newScenario.setEndTriggers(newScenarioEndTriggers);
        }

        // Assessment
        generated.dkf.Assessment newAssessment = new generated.dkf.Assessment();
        if (prevScenario.getAssessment() != null) {

            generated.v10.dkf.Assessment assessment = prevScenario.getAssessment();

            // Objects
            generated.dkf.Objects newObjects = new generated.dkf.Objects();
            if (assessment.getObjects() != null) {

                if (assessment.getObjects().getPlacesOfInterest() != null) {

                    generated.dkf.PlacesOfInterest newPlacesOfInterest = new generated.dkf.PlacesOfInterest();
                    generated.v10.dkf.PlacesOfInterest placesOfInterest = assessment.getObjects().getPlacesOfInterest();

                    for (Object pointOrPathOrArea : placesOfInterest.getPointOrPathOrArea())
                    {
                        if (pointOrPathOrArea instanceof generated.v10.dkf.Point) {
                            generated.v10.dkf.Point point = (generated.v10.dkf.Point) pointOrPathOrArea;

                            generated.dkf.Point newPoint = new generated.dkf.Point();
                            newPoint.setName(point.getName());
                            newPoint.setCoordinate(convertCoordinate(point.getCoordinate()));
                            newPoint.setColorHexRGBA(point.getColorHexRGBA());

                            newPlacesOfInterest.getPointOrPathOrArea().add(newPoint);
                        } else if (pointOrPathOrArea instanceof generated.v10.dkf.Path) {
                            generated.v10.dkf.Path path = (generated.v10.dkf.Path) pointOrPathOrArea;
                            
                            generated.dkf.Path newPath = new generated.dkf.Path();
                            newPath.setName(path.getName());
                            newPath.setColorHexRGBA(path.getColorHexRGBA());
                            for (generated.v10.dkf.Segment segment : path.getSegment()) {
                                generated.dkf.Segment newSegment = new generated.dkf.Segment();
                                newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
                                newSegment.setName(segment.getName());
                                newSegment.setWidth(segment.getWidth());

                                generated.dkf.Segment.Start start = new generated.dkf.Segment.Start();
                                start.setCoordinate(convertCoordinate(segment.getStart().getCoordinate()));
                                newSegment.setStart(start);
                                generated.dkf.Segment.End end = new generated.dkf.Segment.End();
                                end.setCoordinate(convertCoordinate(segment.getEnd().getCoordinate()));
                                newSegment.setEnd(end);
                                newPath.getSegment().add(newSegment);
                            }

                            newPlacesOfInterest.getPointOrPathOrArea().add(newPath);
                        } else if (pointOrPathOrArea instanceof generated.v10.dkf.Area) {
                            generated.v10.dkf.Area area = (generated.v10.dkf.Area) pointOrPathOrArea;
                            generated.dkf.Area newArea = new generated.dkf.Area();
                            newArea.setName(area.getName());
                            newArea.setColorHexRGBA(area.getColorHexRGBA());

                            for (generated.v10.dkf.Coordinate coordinate : area.getCoordinate()) {
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
            generated.dkf.Tasks newTasks = new generated.dkf.Tasks();
            if (assessment.getTasks() != null) {

                for (generated.v10.dkf.Task task : assessment.getTasks().getTask()) {

                    generated.dkf.Task newTask = new generated.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());
                    
                    if(task.isScenarioSupport()){
                        newTask.setScenarioSupport(task.isScenarioSupport());  //added 2020-1 v10
                    }

                    // Start Triggers
                    if (task.getStartTriggers() != null) {
                        generated.dkf.StartTriggers newStartTriggers = new generated.dkf.StartTriggers();
                        newStartTriggers.getTrigger()
                                .addAll(convertStartTriggers(task.getStartTriggers().getTrigger()));
                        newTask.setStartTriggers(newStartTriggers);
                    }

                    // End Triggers
                    generated.dkf.EndTriggers newEndTriggers = new generated.dkf.EndTriggers();
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
                        generated.dkf.CompetenceMetric newCompetenceMetric = new generated.dkf.CompetenceMetric();
                        newCompetenceMetric.setCompetenceMetricImpl(task.getCompetenceMetric().getCompetenceMetricImpl());
                        newTask.setCompetenceMetric(newCompetenceMetric);
                    }
                    
                    // Performance Metric
                    if (task.getPerformanceMetric() != null) {
                        generated.dkf.PerformanceMetric newPerformanceMetric = new generated.dkf.PerformanceMetric();
                        newPerformanceMetric.setPerformanceMetricImpl(task.getPerformanceMetric().getPerformanceMetricImpl());
                        newTask.setPerformanceMetric(newPerformanceMetric);
                    }
                    
                    // Confidence Metric
                    if (task.getConfidenceMetric() != null) {
                        generated.dkf.ConfidenceMetric newConfidenceMetric = new generated.dkf.ConfidenceMetric();
                        newConfidenceMetric.setConfidenceMetricImpl(task.getConfidenceMetric().getConfidenceMetricImpl());
                        newTask.setConfidenceMetric(newConfidenceMetric);
                    }
                    
                    // Trend Metric
                    if (task.getTrendMetric() != null) {
                        generated.dkf.TrendMetric newTrendMetric = new generated.dkf.TrendMetric();
                        newTrendMetric.setTrendMetricImpl(task.getTrendMetric().getTrendMetricImpl());
                        newTask.setTrendMetric(newTrendMetric);
                    }
                    
                    // Priority Metric
                    if (task.getPriorityMetric() != null) {
                        generated.dkf.PriorityMetric newPriorityMetric = new generated.dkf.PriorityMetric();
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
            generated.v10.dkf.Actions actions = prevScenario.getActions();
            generated.dkf.Actions newActions = new generated.dkf.Actions();
            
            if (actions.getInstructionalStrategies() != null) {
                generated.v10.dkf.Actions.InstructionalStrategies instructionalStrategies = actions.getInstructionalStrategies();
                generated.dkf.Actions.InstructionalStrategies newInstructionalStrategies = new generated.dkf.Actions.InstructionalStrategies();

                for(generated.v10.dkf.Strategy strategy : instructionalStrategies.getStrategy()) {
                    newInstructionalStrategies.getStrategy().add(convertStrategy(strategy));
                }
                
                newActions.setInstructionalStrategies(newInstructionalStrategies);
            }
            
            if (actions.getStateTransitions() != null) {
                generated.v10.dkf.Actions.StateTransitions stateTransitions = actions.getStateTransitions();
                generated.dkf.Actions.StateTransitions newStateTransitions = new generated.dkf.Actions.StateTransitions();

                for (generated.v10.dkf.Actions.StateTransitions.StateTransition stateTransition : stateTransitions.getStateTransition()) {
                    generated.dkf.Actions.StateTransitions.StateTransition newStateTransition = new generated.dkf.Actions.StateTransitions.StateTransition();
                    newStateTransition.setName(stateTransition.getName());

                    generated.v10.dkf.Actions.StateTransitions.StateTransition.LogicalExpression logicalExpression = stateTransition.getLogicalExpression();
                    generated.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();

                    for (Object stateType : logicalExpression.getStateType()) {
                        if (stateType instanceof generated.v10.dkf.LearnerStateTransitionEnum) {
                            generated.v10.dkf.LearnerStateTransitionEnum learnerStateTransitionEnum = (generated.v10.dkf.LearnerStateTransitionEnum) stateType;
                            generated.dkf.LearnerStateTransitionEnum newLearnerStateTransitionEnum = new generated.dkf.LearnerStateTransitionEnum();

                            newLearnerStateTransitionEnum.setAttribute(learnerStateTransitionEnum.getAttribute());
                            newLearnerStateTransitionEnum.setCurrent(learnerStateTransitionEnum.getCurrent());
                            newLearnerStateTransitionEnum.setPrevious(learnerStateTransitionEnum.getPrevious());

                            newLogicalExpression.getStateType().add(newLearnerStateTransitionEnum);
                        } else if (stateType instanceof generated.v10.dkf.PerformanceNode) {
                            generated.v10.dkf.PerformanceNode performanceNode = (generated.v10.dkf.PerformanceNode) stateType;
                            generated.dkf.PerformanceNode newPerformanceNode = new generated.dkf.PerformanceNode();

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
                    
                    generated.v10.dkf.Actions.StateTransitions.StateTransition.StrategyChoices strategyChoices = stateTransition.getStrategyChoices();
                    generated.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    
                    for (generated.v10.dkf.StrategyRef strategyRef : strategyChoices.getStrategies()) {
                        generated.dkf.StrategyRef newRef = new generated.dkf.StrategyRef();
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
        
        // Team Org
        if(prevScenario.getTeamOrganization() != null){
            newScenario.setTeamOrganization(convertTeamOrganization(prevScenario.getTeamOrganization()));
        }

        return super.convertScenario(newScenario, showCompletionDialog);
    }

    /**
     * Converts a performance assessment
     * 
     * @param perfAssessment the performance assessment to convert
     * @return the next version of the performance assessment
     */
    private generated.dkf.PerformanceAssessment convertPerformanceAssessment(
            generated.v10.dkf.PerformanceAssessment perfAssessment) {
        generated.dkf.PerformanceAssessment newPerfAssessment = new generated.dkf.PerformanceAssessment();

        if (perfAssessment.getAssessmentType() instanceof generated.v10.dkf.PerformanceAssessment.PerformanceNode) {
            generated.dkf.PerformanceAssessment.PerformanceNode newPerfAssNode = new generated.dkf.PerformanceAssessment.PerformanceNode();

            newPerfAssNode.setNodeId(
                    ((generated.v10.dkf.PerformanceAssessment.PerformanceNode) perfAssessment.getAssessmentType())
                            .getNodeId());
            newPerfAssessment.setAssessmentType(newPerfAssNode);
        } else {
            generated.v10.dkf.Conversation prevConv = (generated.v10.dkf.Conversation) perfAssessment.getAssessmentType();

            generated.dkf.Conversation newConv = new generated.dkf.Conversation();
            if (prevConv.getType() instanceof generated.v10.dkf.ConversationTreeFile) {

                generated.dkf.ConversationTreeFile newTreeFile = new generated.dkf.ConversationTreeFile();
                newTreeFile.setName(((generated.v10.dkf.ConversationTreeFile) prevConv.getType()).getName());

                newConv.setType(newTreeFile);
            } else {

                generated.v10.dkf.AutoTutorSKO prevATSKO = (generated.v10.dkf.AutoTutorSKO) prevConv.getType();

                generated.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevATSKO);

                newConv.setType(newAutoTutorSKO);
            }

            newPerfAssessment.setAssessmentType(newConv);
        }

        newPerfAssessment.setStrategyHandler(convertStrategyHandler(perfAssessment.getStrategyHandler()));
        
        // added 2020-1 v10
        if(perfAssessment.isMandatory() != null){
            newPerfAssessment.setMandatory(perfAssessment.isMandatory());
        }
        
        if (perfAssessment.getDelayAfterStrategy() != null) {
            generated.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.dkf.DelayAfterStrategy();
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
    private generated.dkf.EnvironmentAdaptation convertEnvironmentAdaptation(
            generated.v10.dkf.EnvironmentAdaptation envAdaptation) {
        generated.dkf.EnvironmentAdaptation newEnvAdapt = new generated.dkf.EnvironmentAdaptation();

        Object type = envAdaptation.getType();

        if(type instanceof generated.v10.dkf.EnvironmentAdaptation.Overcast){
            generated.v10.dkf.EnvironmentAdaptation.Overcast overcast = (generated.v10.dkf.EnvironmentAdaptation.Overcast) type;
            generated.dkf.EnvironmentAdaptation.Overcast newOvercast = new generated.dkf.EnvironmentAdaptation.Overcast();
            newOvercast.setValue(overcast.getValue());
            newOvercast.setScenarioAdaptationDuration(overcast.getScenarioAdaptationDuration());
            newEnvAdapt.setType(newOvercast);
            
        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.Fog){
            
            generated.v10.dkf.EnvironmentAdaptation.Fog fog = (generated.v10.dkf.EnvironmentAdaptation.Fog) type;
            generated.dkf.EnvironmentAdaptation.Fog newFog = new generated.dkf.EnvironmentAdaptation.Fog();
            newFog.setDensity(fog.getDensity());
            if (fog.getColor() != null) {
                generated.dkf.EnvironmentAdaptation.Fog.Color newFogColor = new generated.dkf.EnvironmentAdaptation.Fog.Color();
                newFogColor.setRed(fog.getColor().getRed());
                newFogColor.setGreen(fog.getColor().getGreen());
                newFogColor.setBlue(fog.getColor().getBlue());
                newFog.setColor(newFogColor);
            }
            newEnvAdapt.setType(newFog);
            newFog.setScenarioAdaptationDuration(fog.getScenarioAdaptationDuration());
            
        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.Rain){
            
            generated.v10.dkf.EnvironmentAdaptation.Rain rain = (generated.v10.dkf.EnvironmentAdaptation.Rain) type;
            generated.dkf.EnvironmentAdaptation.Rain newRain = new generated.dkf.EnvironmentAdaptation.Rain();
            newRain.setValue(rain.getValue());
            if (rain.getScenarioAdaptationDuration() != null) {
                newRain.setScenarioAdaptationDuration(rain.getScenarioAdaptationDuration());
            }
            newEnvAdapt.setType(newRain);
            
        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.TimeOfDay){
            
            generated.v10.dkf.EnvironmentAdaptation.TimeOfDay tod = (generated.v10.dkf.EnvironmentAdaptation.TimeOfDay) type;
            generated.dkf.EnvironmentAdaptation.TimeOfDay newTod = new generated.dkf.EnvironmentAdaptation.TimeOfDay();
            if (tod.getType() instanceof generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Dawn) {
                newTod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Dawn());  
                newEnvAdapt.setType(newTod);
            } else if (tod.getType() instanceof generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Dusk) {
                newTod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Dusk());  
                newEnvAdapt.setType(newTod);
            } else if (tod.getType() instanceof generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Midday) {
                newTod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Midday());  
                newEnvAdapt.setType(newTod);
            } else if (tod.getType() instanceof generated.v10.dkf.EnvironmentAdaptation.TimeOfDay.Midnight) {
                newTod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Midnight());  
                newEnvAdapt.setType(newTod);
            } else {
                throw new IllegalArgumentException("Found unhandled Time of Day type of '" + type + "'.");
            }
            
        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.CreateActors){

            generated.v10.dkf.EnvironmentAdaptation.CreateActors createActors = (generated.v10.dkf.EnvironmentAdaptation.CreateActors) type;
            generated.dkf.EnvironmentAdaptation.CreateActors newCreateActors = new generated.dkf.EnvironmentAdaptation.CreateActors();
            newCreateActors.setCoordinate(convertCoordinate(createActors.getCoordinate()));
            newCreateActors.setType(createActors.getType());
            if (createActors.getSide().getType() instanceof generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side.Civilian) {
                generated.dkf.EnvironmentAdaptation.CreateActors.Side newSide = new generated.dkf.EnvironmentAdaptation.CreateActors.Side();
                newSide.setType(new generated.dkf.EnvironmentAdaptation.CreateActors.Side.Civilian());
                newCreateActors.setSide(newSide);
            } else if (createActors.getSide().getType() instanceof generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor) {
                generated.dkf.EnvironmentAdaptation.CreateActors.Side newSide = new generated.dkf.EnvironmentAdaptation.CreateActors.Side();
                newSide.setType(new generated.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor());
                newCreateActors.setSide(newSide);
            } else if (createActors.getSide().getType() instanceof generated.v10.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor) {
                generated.dkf.EnvironmentAdaptation.CreateActors.Side newSide = new generated.dkf.EnvironmentAdaptation.CreateActors.Side();
                newSide.setType(new generated.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor());
                newCreateActors.setSide(newSide);
            } else {
                throw new IllegalArgumentException("Found unhandled Create Actor Side type of '" + createActors.getSide().getType() + "'.");
            }
            newEnvAdapt.setType(newCreateActors);

        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.RemoveActors){

            generated.v10.dkf.EnvironmentAdaptation.RemoveActors removeActors = (generated.v10.dkf.EnvironmentAdaptation.RemoveActors) type;
            generated.dkf.EnvironmentAdaptation.RemoveActors newRemoveActors = new generated.dkf.EnvironmentAdaptation.RemoveActors();
            
            // HACK: no longer supporting list of actors to remove as of May 2022.  This should have resulted in a new conversion wizard
            // being created but for now just mess with it here.
            for (Object actorType : removeActors.getType()){
                if (actorType instanceof String) {
                    newRemoveActors.setType((String) actorType);
                } else if (actorType instanceof generated.v10.dkf.EnvironmentAdaptation.RemoveActors.Location) {
                    generated.v10.dkf.EnvironmentAdaptation.RemoveActors.Location actorLocation = (generated.v10.dkf.EnvironmentAdaptation.RemoveActors.Location) actorType;
                    generated.dkf.EnvironmentAdaptation.RemoveActors.Location newActorLocation = new generated.dkf.EnvironmentAdaptation.RemoveActors.Location();
                    newActorLocation.setCoordinate(convertCoordinate(actorLocation.getCoordinate()));
                    newRemoveActors.setType(newActorLocation);
                } else {
                    throw new IllegalArgumentException("Found unhandled Remove Actor type of '" + actorType + "'.");
                }
                
                // only bringing in the first one, see HACK above
                break;
            }
            
            newEnvAdapt.setType(newRemoveActors);

        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.Teleport){
            generated.v10.dkf.EnvironmentAdaptation.Teleport teleportLearner = (generated.v10.dkf.EnvironmentAdaptation.Teleport) type;
            generated.dkf.EnvironmentAdaptation.Teleport newTeleport = new generated.dkf.EnvironmentAdaptation.Teleport();
            newTeleport.setCoordinate(convertCoordinate(teleportLearner.getCoordinate()));
            if (teleportLearner.getHeading() != null) {
                generated.dkf.EnvironmentAdaptation.Teleport.Heading newHeading = new generated.dkf.EnvironmentAdaptation.Teleport.Heading();
                newHeading.setValue(teleportLearner.getHeading().getValue());
                newTeleport.setHeading(newHeading);
            }
            
            // added 2020-1 v10
            if(teleportLearner.getTeamMemberRef() != null){
                generated.dkf.EnvironmentAdaptation.Teleport.TeamMemberRef memberRef = new generated.dkf.EnvironmentAdaptation.Teleport.TeamMemberRef();
                memberRef.setEntityMarking(teleportLearner.getTeamMemberRef().getEntityMarking());
                memberRef.setValue(teleportLearner.getTeamMemberRef().getValue());
                newTeleport.setTeamMemberRef(memberRef);
            }
            
            newEnvAdapt.setType(newTeleport);
            
        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.FatigueRecovery){
            generated.v10.dkf.EnvironmentAdaptation.FatigueRecovery fatigueRecovery = (generated.v10.dkf.EnvironmentAdaptation.FatigueRecovery) type;
            generated.dkf.EnvironmentAdaptation.FatigueRecovery newFatigueRecovery = new generated.dkf.EnvironmentAdaptation.FatigueRecovery();
            newFatigueRecovery.setRate(fatigueRecovery.getRate());
            
            // added 2020-1 v10
            if(fatigueRecovery.getTeamMemberRef() != null){
                generated.dkf.EnvironmentAdaptation.FatigueRecovery.TeamMemberRef memberRef = new generated.dkf.EnvironmentAdaptation.FatigueRecovery.TeamMemberRef();
                memberRef.setEntityMarking(fatigueRecovery.getTeamMemberRef().getEntityMarking());
                memberRef.setValue(fatigueRecovery.getTeamMemberRef().getValue());
                newFatigueRecovery.setTeamMemberRef(memberRef);
            }
            
            newEnvAdapt.setType(newFatigueRecovery);

        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.Endurance){
            generated.v10.dkf.EnvironmentAdaptation.Endurance endurance = (generated.v10.dkf.EnvironmentAdaptation.Endurance) type;
            generated.dkf.EnvironmentAdaptation.Endurance newEndurance = new generated.dkf.EnvironmentAdaptation.Endurance();
            newEndurance.setValue(endurance.getValue());
            
            // added 2020-1 v10
            generated.dkf.EnvironmentAdaptation.Endurance.TeamMemberRef newTeamMemberRef = new generated.dkf.EnvironmentAdaptation.Endurance.TeamMemberRef();
            newTeamMemberRef.setValue(endurance.getTeamMemberRef().getValue());
            newTeamMemberRef.setEntityMarking(endurance.getTeamMemberRef().getEntityMarking());
            newEndurance.setTeamMemberRef(newTeamMemberRef);

            newEnvAdapt.setType(newEndurance);

        } else if(type instanceof generated.v10.dkf.EnvironmentAdaptation.Script){
            generated.v10.dkf.EnvironmentAdaptation.Script script = (generated.v10.dkf.EnvironmentAdaptation.Script) type;
            generated.dkf.EnvironmentAdaptation.Script newScript = new generated.dkf.EnvironmentAdaptation.Script();
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
    private generated.dkf.Feedback convertFeedback(generated.v10.dkf.Feedback feedback) {
        generated.dkf.Feedback newFeedback = new generated.dkf.Feedback();

        if (feedback.getFeedbackPresentation() instanceof generated.v10.dkf.Message) {

            generated.v10.dkf.Message message = (generated.v10.dkf.Message) feedback.getFeedbackPresentation();
            generated.dkf.Message feedbackMsg = convertMessage(message);

            newFeedback.setFeedbackPresentation(feedbackMsg);
        } else if (feedback.getFeedbackPresentation() instanceof generated.v10.dkf.Audio) {

            generated.v10.dkf.Audio audio = (generated.v10.dkf.Audio) feedback.getFeedbackPresentation();

            generated.dkf.Audio newAudio = new generated.dkf.Audio();
            
            // added 2020-1 v10
            if(audio.getToObserverController() != null){
                generated.dkf.ToObserverController newToOC = new generated.dkf.ToObserverController();
                newToOC.setValue(audio.getToObserverController().getValue());
                newAudio.setToObserverController(newToOC);
            }

            // An audio object requires a .mp3 file but does not require a .ogg file
            newAudio.setMP3File(audio.getMP3File());

            if (audio.getOGGFile() != null) {
                newAudio.setOGGFile(audio.getOGGFile());
            }

            newFeedback.setFeedbackPresentation(newAudio);
        } else if (feedback.getFeedbackPresentation() instanceof generated.v10.dkf.MediaSemantics) {

            generated.v10.dkf.MediaSemantics semantics = (generated.v10.dkf.MediaSemantics) feedback
                    .getFeedbackPresentation();

            generated.dkf.MediaSemantics newSemantics = new generated.dkf.MediaSemantics();

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
        
        // Added 2020-1 v10
        if(!feedback.getTeamRef().isEmpty()){
            for(generated.v10.dkf.TeamRef teamRef : feedback.getTeamRef()){
                
                generated.dkf.TeamRef newTeamRef = new generated.dkf.TeamRef();
                newTeamRef.setValue(teamRef.getValue());
                newFeedback.getTeamRef().add(newTeamRef);

            }
        }
        return newFeedback;
    }

    /**
     * Converts a mid lesson media
     * 
     * @param midLessonMedia the mid lesson media to convert
     * @return the next version of the mid lesson media
     */
    private generated.dkf.MidLessonMedia convertMidLessonMedia(generated.v10.dkf.MidLessonMedia midLessonMedia) {
        generated.dkf.MidLessonMedia newMidLessonMedia = new generated.dkf.MidLessonMedia();

        newMidLessonMedia.setStrategyHandler(convertStrategyHandler(midLessonMedia.getStrategyHandler()));
        if (midLessonMedia.getDelayAfterStrategy() != null) {
            generated.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.dkf.DelayAfterStrategy();
            newDelayAfterStrategy.setDuration(midLessonMedia.getDelayAfterStrategy().getDuration());
            newMidLessonMedia.setDelayAfterStrategy(newDelayAfterStrategy);
        }
        
        // added 2020-1 v10
        if(midLessonMedia.isMandatory() != null){
            newMidLessonMedia.setMandatory(midLessonMedia.isMandatory());
        }

        final generated.v10.dkf.LessonMaterialList lessonMaterialList = midLessonMedia.getLessonMaterialList();
        if (lessonMaterialList == null) {
            // gone as far as we can
            return newMidLessonMedia;
        }

        generated.dkf.LessonMaterialList newLessonMaterialList = new generated.dkf.LessonMaterialList();

        generated.v10.dkf.LessonMaterialList.Assessment assessment = lessonMaterialList.getAssessment();
        if (assessment != null) {
            generated.dkf.LessonMaterialList.Assessment newAssessment = new generated.dkf.LessonMaterialList.Assessment();

            final generated.v10.dkf.OverDwell overDwell = assessment.getOverDwell();
            if (overDwell != null) {
                generated.dkf.OverDwell newOverDwell = new generated.dkf.OverDwell();
                newOverDwell.setFeedback(overDwell.getFeedback());

                final generated.v10.dkf.OverDwell.Duration duration = overDwell.getDuration();
                if (duration != null) {
                    generated.dkf.OverDwell.Duration newDuration = new generated.dkf.OverDwell.Duration();

                    final Serializable durationType = duration.getType();
                    if (durationType instanceof BigInteger) {
                        BigInteger durationTime = (BigInteger) durationType;
                        newDuration.setType(durationTime);
                    } else if (durationType instanceof generated.v10.dkf.OverDwell.Duration.DurationPercent) {
                        generated.v10.dkf.OverDwell.Duration.DurationPercent durationPercent = (generated.v10.dkf.OverDwell.Duration.DurationPercent) durationType;
                        generated.dkf.OverDwell.Duration.DurationPercent newDurationPercent = new generated.dkf.OverDwell.Duration.DurationPercent();
                        newDurationPercent.setPercent(durationPercent.getPercent());
                        newDurationPercent.setTime(durationPercent.getTime());

                        newDuration.setType(newDurationPercent);
                    }

                    newOverDwell.setDuration(newDuration);
                }

                newAssessment.setOverDwell(newOverDwell);
            }

            final generated.v10.dkf.LessonMaterialList.Assessment.UnderDwell underDwell = assessment.getUnderDwell();
            if (underDwell != null) {
                generated.dkf.LessonMaterialList.Assessment.UnderDwell newUnderDwell = new generated.dkf.LessonMaterialList.Assessment.UnderDwell();
                newUnderDwell.setFeedback(underDwell.getFeedback());
                newUnderDwell.setDuration(underDwell.getDuration());

                newAssessment.setUnderDwell(newUnderDwell);
            }

            newLessonMaterialList.setAssessment(newAssessment);
        }

        newLessonMaterialList.setIsCollection(convertBooleanEnum(lessonMaterialList.getIsCollection()));

        for (generated.v10.dkf.Media media : lessonMaterialList.getMedia()) {
            generated.dkf.Media newMedia = new generated.dkf.Media();

            newMedia.setName(media.getName());
            newMedia.setMessage(media.getMessage());
            newMedia.setUri(media.getUri());

            Serializable mediaProperties = media.getMediaTypeProperties();
            if (mediaProperties instanceof generated.v10.dkf.PDFProperties) {
                newMedia.setMediaTypeProperties(new generated.dkf.PDFProperties());

            } else if (mediaProperties instanceof generated.v10.dkf.WebpageProperties) {
                newMedia.setMediaTypeProperties(new generated.dkf.WebpageProperties());

            } else if (mediaProperties instanceof generated.v10.dkf.YoutubeVideoProperties) {
                generated.v10.dkf.YoutubeVideoProperties properties = (generated.v10.dkf.YoutubeVideoProperties) mediaProperties;
                newMedia.setMediaTypeProperties(convertYoutubeVideoProperties(properties));

            } else if (mediaProperties instanceof generated.v10.dkf.ImageProperties) {
                newMedia.setMediaTypeProperties(new generated.dkf.ImageProperties());

            } else if (mediaProperties instanceof generated.v10.dkf.SlideShowProperties) {
                generated.v10.dkf.SlideShowProperties properties = (generated.v10.dkf.SlideShowProperties) mediaProperties;
                generated.dkf.SlideShowProperties newProperties = new generated.dkf.SlideShowProperties();
                newProperties
                        .setDisplayPreviousSlideButton(convertBooleanEnum(properties.getDisplayPreviousSlideButton()));
                newProperties.setKeepContinueButton(convertBooleanEnum(properties.getKeepContinueButton()));
                newProperties.getSlideRelativePath().addAll(properties.getSlideRelativePath());
                newMedia.setMediaTypeProperties(newProperties);

            } else if (mediaProperties instanceof generated.v10.dkf.LtiProperties) {
                generated.v10.dkf.LtiProperties properties = (generated.v10.dkf.LtiProperties) mediaProperties;
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
    private generated.dkf.YoutubeVideoProperties convertYoutubeVideoProperties(
            generated.v10.dkf.YoutubeVideoProperties properties) {
        generated.dkf.YoutubeVideoProperties newProperties = new generated.dkf.YoutubeVideoProperties();

        newProperties.setAllowAutoPlay(convertBooleanEnum(properties.getAllowAutoPlay()));

        newProperties.setAllowFullScreen(convertBooleanEnum(properties.getAllowFullScreen()));

        final generated.v10.dkf.Size size = properties.getSize();
        if (size != null) {
            generated.dkf.Size newSize = new generated.dkf.Size();
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
    private generated.dkf.LtiProperties convertLtiProperties(generated.v10.dkf.LtiProperties properties) {
        generated.dkf.LtiProperties newProperties = new generated.dkf.LtiProperties();

        newProperties.setAllowScore(convertBooleanEnum(properties.getAllowScore()));
        newProperties.setIsKnowledge(convertBooleanEnum(properties.getIsKnowledge()));
        newProperties.setSliderMaxValue(properties.getSliderMaxValue());
        newProperties.setSliderMinValue(properties.getSliderMinValue());
        newProperties.setLtiIdentifier(properties.getLtiIdentifier());

        if (properties.getDisplayMode() != null) {
            newProperties.setDisplayMode(generated.dkf.DisplayModeEnum.fromValue(properties.getDisplayMode().value()));
        }

        if (properties.getCustomParameters() != null) {
            generated.dkf.CustomParameters newCustomParameters = new generated.dkf.CustomParameters();
            for (generated.v10.dkf.Nvpair nvPair : properties.getCustomParameters().getNvpair()) {
                newCustomParameters.getNvpair().add(convertNvpair(nvPair));
            }
            newProperties.setCustomParameters(newCustomParameters);
        }

        if (properties.getLtiConcepts() != null) {
            generated.dkf.LtiConcepts newLtiConcepts = new generated.dkf.LtiConcepts();
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
    private generated.dkf.Strategy convertStrategy(generated.v10.dkf.Strategy oldStrategy) {
        generated.dkf.Strategy newStrategy = new generated.dkf.Strategy();
        newStrategy.setName(oldStrategy.getName());
        
        for (Object strategyActivity : oldStrategy.getStrategyActivities()) {
            if (strategyActivity instanceof generated.v10.dkf.InstructionalIntervention) {

                generated.v10.dkf.InstructionalIntervention iIntervention = (generated.v10.dkf.InstructionalIntervention) strategyActivity;
                generated.dkf.InstructionalIntervention newIIntervention = new generated.dkf.InstructionalIntervention();

                if (iIntervention.getDelayAfterStrategy() != null) {
                    generated.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.dkf.DelayAfterStrategy();
                    newDelayAfterStrategy.setDuration(iIntervention.getDelayAfterStrategy().getDuration());
                    newIIntervention.setDelayAfterStrategy(newDelayAfterStrategy);
                }
                newIIntervention.setFeedback(convertFeedback(iIntervention.getFeedback()));
                newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));
                
                // added 2020-1 v10
                if(iIntervention.isMandatory() != null){
                    newIIntervention.setMandatory(iIntervention.isMandatory());
                }
                
                newStrategy.getStrategyActivities().add(newIIntervention);

            } else if (strategyActivity instanceof generated.v10.dkf.MidLessonMedia) {

                newStrategy.getStrategyActivities().add(convertMidLessonMedia((generated.v10.dkf.MidLessonMedia) strategyActivity));

            } else if (strategyActivity instanceof generated.v10.dkf.PerformanceAssessment) {

                newStrategy.getStrategyActivities().add(convertPerformanceAssessment((generated.v10.dkf.PerformanceAssessment) strategyActivity));

            } else if (strategyActivity instanceof generated.v10.dkf.ScenarioAdaptation) {
                generated.v10.dkf.ScenarioAdaptation scenarioAdaptation = (generated.v10.dkf.ScenarioAdaptation) strategyActivity;
                generated.dkf.ScenarioAdaptation newScenarioAdaptation = new generated.dkf.ScenarioAdaptation();
                if (scenarioAdaptation.getDelayAfterStrategy() != null) {
                    generated.v10.dkf.DelayAfterStrategy delayAfterStrategy = scenarioAdaptation.getDelayAfterStrategy();
                    generated.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.dkf.DelayAfterStrategy();
                    newDelayAfterStrategy.setDuration(delayAfterStrategy.getDuration());
                    newScenarioAdaptation.setDelayAfterStrategy(newDelayAfterStrategy);
                }
                newScenarioAdaptation.setEnvironmentAdaptation(convertEnvironmentAdaptation(scenarioAdaptation.getEnvironmentAdaptation()));
                newScenarioAdaptation.setStrategyHandler(convertStrategyHandler(scenarioAdaptation.getStrategyHandler()));
                
                // added 2020-1 v10
                if(scenarioAdaptation.isMandatory() != null){
                    newScenarioAdaptation.setMandatory(scenarioAdaptation.isMandatory());
                }
                
                // added 2020-1 v10
                newScenarioAdaptation.setDescription(scenarioAdaptation.getDescription());

                newStrategy.getStrategyActivities().add(newScenarioAdaptation);

            } else if (strategyActivity instanceof generated.v10.dkf.DoNothingInstStrategy) {
                generated.v10.dkf.DoNothingInstStrategy doNothingInstStrategy = (generated.v10.dkf.DoNothingInstStrategy) strategyActivity;
                generated.dkf.DoNothingInstStrategy newDoNothingInstStrategy = new generated.dkf.DoNothingInstStrategy();
                if (doNothingInstStrategy.getDelayAfterStrategy() != null) {
                    generated.dkf.DelayAfterStrategy newDelayAfterStrategy = new generated.dkf.DelayAfterStrategy();
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
    private generated.dkf.AutoTutorSKO convertAutoTutorSKO(generated.v10.dkf.AutoTutorSKO prevATSKO) {

        if (prevATSKO == null) {
            return null;
        }

        generated.dkf.AutoTutorSKO newAutoTutorSKO = new generated.dkf.AutoTutorSKO();
        if (prevATSKO.getScript() instanceof generated.v10.dkf.LocalSKO) {

            generated.v10.dkf.LocalSKO localSKO = (generated.v10.dkf.LocalSKO) prevATSKO.getScript();
            generated.dkf.LocalSKO newLocalSKO = new generated.dkf.LocalSKO();

            newLocalSKO.setFile(localSKO.getFile());
            newAutoTutorSKO.setScript(newLocalSKO);

        } else if (prevATSKO.getScript() instanceof generated.v10.dkf.ATRemoteSKO) {
            generated.v10.dkf.ATRemoteSKO prevLocalSKO = (generated.v10.dkf.ATRemoteSKO) prevATSKO.getScript();

            generated.dkf.ATRemoteSKO.URL newURL = new generated.dkf.ATRemoteSKO.URL();
            newURL.setAddress(prevLocalSKO.getURL().getAddress());

            generated.dkf.ATRemoteSKO newATRemoteSKO = new generated.dkf.ATRemoteSKO();
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
    private generated.dkf.Message convertMessage(generated.v10.dkf.Message message) {
        generated.dkf.Message newMessage = new generated.dkf.Message();
        newMessage.setContent(message.getContent());

        if (message.getDelivery() != null) {
            generated.dkf.Message.Delivery newDelivery = new generated.dkf.Message.Delivery();

            if (message.getDelivery().getInTrainingApplication() != null) {
                generated.dkf.Message.Delivery.InTrainingApplication newInTrainingApp = new generated.dkf.Message.Delivery.InTrainingApplication();
                newInTrainingApp.setEnabled(generated.dkf.BooleanEnum.fromValue(
                        message.getDelivery().getInTrainingApplication().getEnabled().toString().toLowerCase()));
                if (message.getDelivery().getInTrainingApplication().getMobileOption() != null) {
                    generated.v10.dkf.Message.Delivery.InTrainingApplication.MobileOption mobileOption = message.getDelivery().getInTrainingApplication().getMobileOption();
                    generated.dkf.Message.Delivery.InTrainingApplication.MobileOption newMobileOption = new generated.dkf.Message.Delivery.InTrainingApplication.MobileOption();
                    newMobileOption.setVibrate(mobileOption.isVibrate());
                    newInTrainingApp.setMobileOption(newMobileOption);
                }
                newDelivery.setInTrainingApplication(newInTrainingApp);
            }
            if (message.getDelivery().getInTutor() != null) {
                generated.dkf.InTutor newInTutor = new generated.dkf.InTutor();
                newInTutor.setMessagePresentation(message.getDelivery().getInTutor().getMessagePresentation());
                newInTutor.setTextEnhancement(message.getDelivery().getInTutor().getTextEnhancement());
                newDelivery.setInTutor(newInTutor);
            }
            
            // added 2020-1 v10
            if(message.getDelivery().getToObserverController() != null){
                generated.dkf.ToObserverController newToOC = new generated.dkf.ToObserverController();
                newToOC.setValue(message.getDelivery().getToObserverController().getValue());
                newDelivery.setToObserverController(newToOC);
            }

            newMessage.setDelivery(newDelivery);
        }

        return newMessage;
    }
    
    /**
     * Convert a Message object for scenarios
     *
     * @param message - the message to convert
     * @return a new Message object.  If null is provided, null is returned.
     */
    private generated.dkf.Scenario.EndTriggers.Trigger.Message convertMessage(generated.v10.dkf.Scenario.EndTriggers.Trigger.Message message) {
        
        if(message == null){
            return null;
        }
        
        generated.dkf.Scenario.EndTriggers.Trigger.Message newMessage = new generated.dkf.Scenario.EndTriggers.Trigger.Message();
        newMessage.setStrategy(convertStrategy(message.getStrategy()));

        return newMessage;
    }

    /**
     * Convert an entities object to a new entities object.
     *
     * @param entities - the object to convert
     * @return generated.v10.dkf.Entities - the new object
     * @throws IllegalArgumentException if the start location coordinate type is unknown
     */
    private generated.dkf.Entities convertEntities(generated.v10.dkf.Entities entities)
            throws IllegalArgumentException {

        generated.dkf.Entities newEntities = new generated.dkf.Entities();
        for (generated.v10.dkf.StartLocation location : entities.getStartLocation()) {

            generated.dkf.StartLocation newLocation = new generated.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }
        
        newEntities.getTeamMemberRef().addAll(entities.getTeamMemberRef());

        return newEntities;
    }

    /**
     * Convert a checkpoint object into a new checkpoint object.
     *
     * @param checkpoint - the object to convert
     * @return generated.v10.dkf.Checkpoint - the new object
     */
    private generated.dkf.Checkpoint convertCheckpoint(generated.v10.dkf.Checkpoint checkpoint) {

        generated.dkf.Checkpoint newCheckpoint = new generated.dkf.Checkpoint();
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
    private generated.dkf.Evaluators convertEvaluators(generated.v10.dkf.Evaluators evaluators) {

        generated.dkf.Evaluators newEvaluators = new generated.dkf.Evaluators();
        for (generated.v10.dkf.Evaluator evaluator : evaluators.getEvaluator()) {

            generated.dkf.Evaluator newEvaluator = new generated.dkf.Evaluator();
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
    private List<generated.dkf.HasMovedExcavatorComponentInput.Component> convertComponents(
            List<generated.v10.dkf.HasMovedExcavatorComponentInput.Component> oldCompList) {

        List<generated.dkf.HasMovedExcavatorComponentInput.Component> componentList = new ArrayList<generated.dkf.HasMovedExcavatorComponentInput.Component>();

        for (generated.v10.dkf.HasMovedExcavatorComponentInput.Component oldComp : oldCompList) {
            generated.dkf.HasMovedExcavatorComponentInput.Component newComp = new generated.dkf.HasMovedExcavatorComponentInput.Component();
            newComp.setComponentType(
                    generated.dkf.ExcavatorComponentEnum.fromValue(oldComp.getComponentType().value()));

            if (oldComp
                    .getDirectionType() instanceof generated.v10.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional) {
                generated.v10.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional oldBiDirectional = (generated.v10.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional) oldComp
                        .getDirectionType();
                generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional newBiDirectional = new generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional();
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
    private generated.dkf.Assessments convertAssessments(generated.v10.dkf.Assessments assessments) {

        generated.dkf.Assessments newAssessments = new generated.dkf.Assessments();

        List<generated.v10.dkf.Assessments.Survey> surveys = new ArrayList<generated.v10.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
            if (assessmentType instanceof generated.v10.dkf.Assessments.Survey) {
                surveys.add((generated.v10.dkf.Assessments.Survey) assessmentType);
            }
        }

        for (generated.v10.dkf.Assessments.Survey survey : surveys) {
            generated.dkf.Assessments.Survey newSurvey = new generated.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());

            final generated.v10.dkf.Questions oldQuestions = survey.getQuestions();
            if (oldQuestions != null && !oldQuestions.getQuestion().isEmpty()) {
                generated.dkf.Questions newQuestions = new generated.dkf.Questions();
                for (generated.v10.dkf.Question oldQuestion : oldQuestions.getQuestion()) {

                    generated.dkf.Question newQuestion = new generated.dkf.Question();
                    newQuestion.setKey(oldQuestion.getKey());

                    for (generated.v10.dkf.Reply oldReply : oldQuestion.getReply()) {

                        generated.dkf.Reply newReply = new generated.dkf.Reply();
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
     * @return List<generated.dkf.StartTriggers.Trigger> - converted trigger objects (same size as
     *         triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private List<generated.dkf.StartTriggers.Trigger> convertStartTriggers(
            List<generated.v10.dkf.StartTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.dkf.StartTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v10.dkf.StartTriggers.Trigger triggerObj : list) {

            generated.dkf.StartTriggers.Trigger trigger = new generated.dkf.StartTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj.getTriggerType()));
            trigger.setTriggerDelay(triggerObj.getTriggerDelay());
            
            if(triggerObj.getTriggerMessage() != null){
                generated.dkf.StartTriggers.Trigger.TriggerMessage newTrigMsg = new generated.dkf.StartTriggers.Trigger.TriggerMessage();
                newTrigMsg.setStrategy(convertStrategy(triggerObj.getTriggerMessage().getStrategy()));
                trigger.setTriggerMessage(newTrigMsg);
            }

            newTriggerObjects.add(trigger);
        }

        return newTriggerObjects;
    }

    /**
     * Convert a collection of end trigger objects into the new schema version.
     *
     * @param list - collection of trigger objects to convert
     * @return List<generated.dkf.EndTriggers.Trigger> - converted trigger objects (same size as
     *         triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private List<generated.dkf.EndTriggers.Trigger> convertEndTriggers(
            List<generated.v10.dkf.EndTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.dkf.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v10.dkf.EndTriggers.Trigger triggerObj : list) {

            generated.dkf.EndTriggers.Trigger trigger = new generated.dkf.EndTriggers.Trigger();
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
     * @return List<generated.dkf.Scenario.EndTriggers.Trigger> - converted trigger objects (same
     *         size as triggerObjects collection)
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private List<generated.dkf.Scenario.EndTriggers.Trigger> convertScenarioEndTriggers(
            List<generated.v10.dkf.Scenario.EndTriggers.Trigger> list) throws IllegalArgumentException {

        List<generated.dkf.Scenario.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for (generated.v10.dkf.Scenario.EndTriggers.Trigger triggerObj : list) {

            generated.dkf.Scenario.EndTriggers.Trigger trigger = new generated.dkf.Scenario.EndTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj.getTriggerType()));
            trigger.setTriggerDelay(triggerObj.getTriggerDelay());            
            trigger.setMessage(convertMessage(triggerObj.getMessage()));

            newTriggerObjects.add(trigger);
        }

        return newTriggerObjects;
    }
    
    /**
     * Convert an old entity id into a new entity id object
     * @param entityId the old entity id object to convert
     * @return the new entity id object
     */
    private generated.dkf.EntityLocation.EntityId convertEntityId(generated.v10.dkf.EntityLocation.EntityId entityId){
        

        generated.dkf.EntityLocation.EntityId newEntityId = new generated.dkf.EntityLocation.EntityId();
        
        Serializable entityIdObj = entityId.getTeamMemberRefOrLearnerId();
        Serializable newEntityIdObj;
        if(entityIdObj instanceof generated.v10.dkf.LearnerId){            
            newEntityIdObj = convertLearnerId((generated.v10.dkf.LearnerId)entityIdObj);

        }else if(entityIdObj instanceof generated.v10.dkf.EntityLocation.EntityId.TeamMemberRef){
            
            generated.v10.dkf.EntityLocation.EntityId.TeamMemberRef teamMemberRef = (generated.v10.dkf.EntityLocation.EntityId.TeamMemberRef)entityIdObj;
            generated.dkf.EntityLocation.EntityId.TeamMemberRef newTeamMemberRef = new generated.dkf.EntityLocation.EntityId.TeamMemberRef();
            newTeamMemberRef.setValue(teamMemberRef.getValue());
            newEntityIdObj = newTeamMemberRef;
            
        }else{
            throw new IllegalArgumentException("Found unhandled entity id type of "+ entityIdObj);
        }
        
        newEntityId.setTeamMemberRefOrLearnerId(newEntityIdObj);
        
        return newEntityId;
    }

    /**
     * Convert a collection of trigger objects (start or end triggers) into the new schema version.
     *
     * @param triggerObj the trigger to convert
     * @return the converted trigger object
     * @throws IllegalArgumentException if the trigger contains an unknown coordinate type.
     */
    private Serializable convertTrigger(Serializable triggerObj) throws IllegalArgumentException {

        if (triggerObj instanceof generated.v10.dkf.EntityLocation) {

            generated.v10.dkf.EntityLocation entityLocation = (generated.v10.dkf.EntityLocation) triggerObj;
            generated.dkf.EntityLocation newEntityLocation = new generated.dkf.EntityLocation();

            generated.dkf.StartLocation startLocation = new generated.dkf.StartLocation();
            if (entityLocation.getTriggerLocation() != null) {
                startLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
            }
            
            newEntityLocation.setEntityId(convertEntityId(entityLocation.getEntityId()));

            generated.dkf.TriggerLocation triggerLocation = new generated.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);

            return newEntityLocation;
        } else if (triggerObj instanceof generated.v10.dkf.LearnerLocation) {
            // convert to EntityLocation
            generated.v10.dkf.LearnerLocation learnerLocation = (generated.v10.dkf.LearnerLocation) triggerObj;
            generated.dkf.EntityLocation newEntityLocation = new generated.dkf.EntityLocation();

            generated.dkf.EntityLocation.EntityId entityId = new generated.dkf.EntityLocation.EntityId();
            generated.dkf.LearnerId learnerId = new generated.dkf.LearnerId();
            // current v10 doesn't have String learner id type, just coordinate
            generated.dkf.StartLocation startLocation = new generated.dkf.StartLocation();
            startLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
            learnerId.setType(startLocation);
            
            entityId.setTeamMemberRefOrLearnerId(learnerId);
            newEntityLocation.setEntityId(entityId);

            generated.dkf.TriggerLocation triggerLocation = new generated.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);

            return newEntityLocation;
        } else if (triggerObj instanceof generated.v10.dkf.ConceptEnded) {

            generated.v10.dkf.ConceptEnded conceptEnded = (generated.v10.dkf.ConceptEnded) triggerObj;
            generated.dkf.ConceptEnded newConceptEnded = new generated.dkf.ConceptEnded();

            newConceptEnded.setNodeId(conceptEnded.getNodeId());

            return newConceptEnded;
        } else if (triggerObj instanceof generated.v10.dkf.ChildConceptEnded) {

            generated.v10.dkf.ChildConceptEnded childConceptEnded = (generated.v10.dkf.ChildConceptEnded) triggerObj;
            generated.dkf.ChildConceptEnded newChildConceptEnded = new generated.dkf.ChildConceptEnded();

            newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());

            return newChildConceptEnded;
        } else if (triggerObj instanceof generated.v10.dkf.TaskEnded) {

            generated.v10.dkf.TaskEnded taskEnded = (generated.v10.dkf.TaskEnded) triggerObj;
            generated.dkf.TaskEnded newTaskEnded = new generated.dkf.TaskEnded();

            newTaskEnded.setNodeId(taskEnded.getNodeId());

            return newTaskEnded;
        } else if (triggerObj instanceof generated.v10.dkf.ConceptAssessment) {

            generated.v10.dkf.ConceptAssessment conceptAssessment = (generated.v10.dkf.ConceptAssessment) triggerObj;
            generated.dkf.ConceptAssessment newConceptAssessment = new generated.dkf.ConceptAssessment();

            newConceptAssessment.setResult(conceptAssessment.getResult());
            newConceptAssessment.setConcept(conceptAssessment.getConcept());

            return newConceptAssessment;
            
        } else if(triggerObj instanceof generated.v10.dkf.LearnerActionReference){
            
            generated.v10.dkf.LearnerActionReference actionRef = (generated.v10.dkf.LearnerActionReference)triggerObj;
            generated.dkf.LearnerActionReference newActionRef = new generated.dkf.LearnerActionReference();
            
            newActionRef.setName(actionRef.getName());
            
            return newActionRef;
            
        } else if(triggerObj instanceof generated.v10.dkf.ScenarioStarted){
            
            @SuppressWarnings("unused")
            generated.v10.dkf.ScenarioStarted scenarioStarted = (generated.v10.dkf.ScenarioStarted)triggerObj;
            generated.dkf.ScenarioStarted newScenarioStarted = new generated.dkf.ScenarioStarted();
            
            return newScenarioStarted;
            
        } else {
            throw new IllegalArgumentException("Found unhandled trigger type of " + triggerObj);
        }
    }

    /**
     * Convert a coordinate object into the latest schema version.
     *
     * @param coordinate - coordinate object to convert
     * @return generated.dkf.Coordinate - the new coordinate object.  If null is provided, null is returned.
     * @throws IllegalArgumentException if the coordinate is of an unknown type
     */
    private generated.dkf.Coordinate convertCoordinate(generated.v10.dkf.Coordinate coordinate)
            throws IllegalArgumentException {
        
        if(coordinate == null){
            return null;
        }

        generated.dkf.Coordinate newCoord = new generated.dkf.Coordinate();

        Object coordType = coordinate.getType();
        if (coordType instanceof generated.v10.dkf.GCC) {

            generated.v10.dkf.GCC gcc = (generated.v10.dkf.GCC) coordType;
            generated.dkf.GCC newGCC = new generated.dkf.GCC();

            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());

            newCoord.setType(newGCC);

        } else if (coordType instanceof generated.v10.dkf.GDC) {
            // generated.
            generated.v10.dkf.GDC gdc = (generated.v10.dkf.GDC) coordType;
            generated.dkf.GDC newGDC = new generated.dkf.GDC();

            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());

            newCoord.setType(newGDC);

        } else if (coordType instanceof generated.v10.dkf.AGL) {

            generated.v10.dkf.AGL agl = (generated.v10.dkf.AGL) coordType;
            generated.dkf.AGL newAGL = new generated.dkf.AGL();

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
     * @return generated.dkf.StrategyHandler - the new object
     * @throws IllegalArgumentException if the handler is null
     */
    private generated.dkf.StrategyHandler convertStrategyHandler(generated.v10.dkf.StrategyHandler handler) {

        if (handler == null) {
            throw new IllegalArgumentException("Found null StrategyHandler but it is required.");
        }

        generated.dkf.StrategyHandler newHandler = new generated.dkf.StrategyHandler();

        if (handler.getParams() != null) {

            generated.dkf.StrategyHandler.Params newParams = new generated.dkf.StrategyHandler.Params();

            for (generated.v10.dkf.Nvpair nvpair : handler.getParams().getNvpair()) {
                generated.dkf.Nvpair newNvpair = new generated.dkf.Nvpair();
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
     * @return generated.dkf.Concepts - the new object
     * @throws IllegalArgumentException if the concept contains an entity with an unknown coordinate
     *         type
     */
    private generated.dkf.Concepts convertConcepts(generated.v10.dkf.Concepts concepts, generated.dkf.Objects scenarioObjects) throws IllegalArgumentException{

        generated.dkf.Concepts newConcepts = new generated.dkf.Concepts();
        for (generated.v10.dkf.Concept concept : concepts.getConcept()) {

            generated.dkf.Concept newConcept = new generated.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());
            if(concept.isScenarioSupport()){
                newConcept.setScenarioSupport(concept.isScenarioSupport());  //added 2020-1 v10
            }

            if (concept.getAssessments() != null) {
                newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }

            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if (conditionsOrConcepts instanceof generated.v10.dkf.Concepts) {
                // nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v10.dkf.Concepts)conditionsOrConcepts, scenarioObjects) );

            } else if (conditionsOrConcepts instanceof generated.v10.dkf.Conditions) {

                generated.dkf.Conditions newConditions = new generated.dkf.Conditions();

                generated.v10.dkf.Conditions conditions = (generated.v10.dkf.Conditions) conditionsOrConcepts;

                for (generated.v10.dkf.Condition condition : conditions.getCondition()) {

                    generated.dkf.Condition newCondition = new generated.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());

                    if (condition.getDefault() != null) {
                        generated.dkf.Default newDefault = new generated.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }

                    // Input
                    generated.dkf.Input newInput = new generated.dkf.Input();
                    if (condition.getInput() != null) {

                        Object inputType = condition.getInput().getType();

                        if (inputType == null) {
                            // nothing to do right now

                        } else if (inputType instanceof generated.v10.dkf.ApplicationCompletedCondition) {

                            generated.v10.dkf.ApplicationCompletedCondition conditionInput = (generated.v10.dkf.ApplicationCompletedCondition) inputType;

                            generated.dkf.ApplicationCompletedCondition newConditionInput = new generated.dkf.ApplicationCompletedCondition();

                            if (conditionInput.getIdealCompletionDuration() != null) {
                                newConditionInput
                                        .setIdealCompletionDuration(conditionInput.getIdealCompletionDuration());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.AutoTutorConditionInput) {

                            generated.v10.dkf.AutoTutorConditionInput conditionInput = (generated.v10.dkf.AutoTutorConditionInput) inputType;

                            generated.dkf.AutoTutorConditionInput newConditionInput = new generated.dkf.AutoTutorConditionInput();

                            if (conditionInput.getAutoTutorSKO() != null) {

                                generated.v10.dkf.AutoTutorSKO prevAutoTutorSKO = conditionInput.getAutoTutorSKO();
                                generated.dkf.AutoTutorSKO newAutoTutorSKO = convertAutoTutorSKO(prevAutoTutorSKO);
                                newConditionInput.setAutoTutorSKO(newAutoTutorSKO);
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.AvoidLocationCondition) {

                            generated.v10.dkf.AvoidLocationCondition conditionInput = (generated.v10.dkf.AvoidLocationCondition) inputType;

                            generated.dkf.AvoidLocationCondition newConditionInput = new generated.dkf.AvoidLocationCondition();

                            if (conditionInput.getRealTimeAssessmentRules() != null) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }

                            if (conditionInput.getPointRef() != null) {
                                for(generated.v10.dkf.PointRef pointRef : conditionInput.getPointRef()) {
                                    newConditionInput.getPointRef().add(convertPointRef(pointRef));
                                }
                            }

                            if (conditionInput.getAreaRef() != null) {
                                for(generated.v10.dkf.AreaRef areaRef : conditionInput.getAreaRef()) {
                                    newConditionInput.getAreaRef().add(convertAreaRef(areaRef));
                                }
                            }
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));
                            
                            // added 2020-1 v10
                            if(conditionInput.isRequireLearnerAction() != null){
                                newConditionInput.setRequireLearnerAction(conditionInput.isRequireLearnerAction());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.CheckpointPaceCondition) {

                            generated.v10.dkf.CheckpointPaceCondition conditionInput = (generated.v10.dkf.CheckpointPaceCondition) inputType;

                            generated.dkf.CheckpointPaceCondition newConditionInput = new generated.dkf.CheckpointPaceCondition();
                            for (generated.v10.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()) {

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                            
                            newConditionInput.setTeamMemberRef(conditionInput.getTeamMemberRef());

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.CheckpointProgressCondition) {

                            generated.v10.dkf.CheckpointProgressCondition conditionInput = (generated.v10.dkf.CheckpointProgressCondition) inputType;

                            generated.dkf.CheckpointProgressCondition newConditionInput = new generated.dkf.CheckpointProgressCondition();
                            for (generated.v10.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()) {

                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                            
                            newConditionInput.setTeamMemberRef(conditionInput.getTeamMemberRef());

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.CorridorBoundaryCondition) {

                            generated.v10.dkf.CorridorBoundaryCondition conditionInput = (generated.v10.dkf.CorridorBoundaryCondition) inputType;

                            generated.dkf.CorridorBoundaryCondition newConditionInput = new generated.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            generated.dkf.PathRef newPathRef = new generated.dkf.PathRef();
                            newPathRef.setValue(conditionInput.getPathRef().getValue());
                            newConditionInput.setPathRef(newPathRef);
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.CorridorPostureCondition) {

                            generated.v10.dkf.CorridorPostureCondition conditionInput = (generated.v10.dkf.CorridorPostureCondition) inputType;

                            generated.dkf.CorridorPostureCondition newConditionInput = new generated.dkf.CorridorPostureCondition();
                            generated.dkf.PathRef newPathRef = new generated.dkf.PathRef();
                            newPathRef.setValue(conditionInput.getPathRef().getValue());
                            newConditionInput.setPathRef(newPathRef);

                            generated.dkf.Postures postures = new generated.dkf.Postures();
                            for (String strPosture : conditionInput.getPostures().getPosture()) {
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.EliminateHostilesCondition) {

                            generated.v10.dkf.EliminateHostilesCondition conditionInput = (generated.v10.dkf.EliminateHostilesCondition) inputType;

                            generated.dkf.EliminateHostilesCondition newConditionInput = new generated.dkf.EliminateHostilesCondition();

                            if (conditionInput.getEntities() != null) {
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            if (conditionInput.getRealTimeAssessmentRules() != null) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.EnterAreaCondition) {

                            generated.v10.dkf.EnterAreaCondition conditionInput = (generated.v10.dkf.EnterAreaCondition) inputType;

                            generated.dkf.EnterAreaCondition newConditionInput = new generated.dkf.EnterAreaCondition();

                            for (generated.v10.dkf.Entrance entrance : conditionInput.getEntrance()) {

                                generated.dkf.Entrance newEntrance = new generated.dkf.Entrance();

                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());

                                generated.dkf.Inside newInside = new generated.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setPoint(entrance.getInside().getPoint());
                                newEntrance.setInside(newInside);

                                generated.dkf.Outside newOutside = new generated.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setPoint(entrance.getOutside().getPoint());
                                newEntrance.setOutside(newOutside);

                                newConditionInput.getEntrance().add(newEntrance);
                            }
                            
                            newConditionInput.setTeamMemberRef(conditionInput.getTeamMemberRef());

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.ExplosiveHazardSpotReportCondition) {

                            generated.v10.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v10.dkf.ExplosiveHazardSpotReportCondition) inputType;

                            generated.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.dkf.ExplosiveHazardSpotReportCondition();
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.GenericConditionInput) {

                            generated.v10.dkf.GenericConditionInput conditionInput = (generated.v10.dkf.GenericConditionInput) inputType;

                            generated.dkf.GenericConditionInput newConditionInput = new generated.dkf.GenericConditionInput();

                            if (conditionInput.getNvpair() != null) {
                                for (generated.v10.dkf.Nvpair nvPair : conditionInput.getNvpair()) {
                                    newConditionInput.getNvpair().add(convertNvpair(nvPair));
                                }
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.HasMovedExcavatorComponentInput) {

                            generated.v10.dkf.HasMovedExcavatorComponentInput conditionInput = (generated.v10.dkf.HasMovedExcavatorComponentInput) inputType;
                            generated.dkf.HasMovedExcavatorComponentInput newConditionInput = new generated.dkf.HasMovedExcavatorComponentInput();

                            newConditionInput.getComponent().addAll(convertComponents(conditionInput.getComponent()));
                            newConditionInput.setMaxAssessments(conditionInput.getMaxAssessments());

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.IdentifyPOIsCondition) {

                            generated.v10.dkf.IdentifyPOIsCondition conditionInput = (generated.v10.dkf.IdentifyPOIsCondition) inputType;

                            generated.dkf.IdentifyPOIsCondition newConditionInput = new generated.dkf.IdentifyPOIsCondition();

                            if (conditionInput.getPois() != null) {

                                generated.dkf.Pois pois = new generated.dkf.Pois();
                                for (generated.v10.dkf.PointRef pointRef : conditionInput.getPois()
                                        .getPointRef()) {
                                    pois.getPointRef().add(convertPointRef(pointRef));
                                }

                                newConditionInput.setPois(pois);
                            }

                            if (conditionInput.getRealTimeAssessmentRules() != null ) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.LifeformTargetAccuracyCondition) {

                            generated.v10.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v10.dkf.LifeformTargetAccuracyCondition) inputType;

                            generated.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.dkf.LifeformTargetAccuracyCondition();

                            if (conditionInput.getEntities() != null) {
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }

                            if (conditionInput.getRealTimeAssessmentRules() != null ) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.MarksmanshipPrecisionCondition) {

                            generated.v10.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v10.dkf.MarksmanshipPrecisionCondition) inputType;

                            generated.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.dkf.MarksmanshipPrecisionCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.MarksmanshipSessionCompleteCondition) {

                            generated.v10.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v10.dkf.MarksmanshipSessionCompleteCondition) inputType;

                            generated.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.dkf.MarksmanshipSessionCompleteCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.NineLineReportCondition) {

                            generated.v10.dkf.NineLineReportCondition conditionInput = (generated.v10.dkf.NineLineReportCondition) inputType;

                            generated.dkf.NineLineReportCondition newConditionInput = new generated.dkf.NineLineReportCondition();
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.NoConditionInput) {
                            newInput.setType(new generated.dkf.NoConditionInput());

                        } else if (inputType instanceof generated.v10.dkf.NumberOfShotsFiredCondition) {

                            generated.v10.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v10.dkf.NumberOfShotsFiredCondition) inputType;

                            generated.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.dkf.NumberOfShotsFiredCondition();

                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.PowerPointDwellCondition) {

                            generated.v10.dkf.PowerPointDwellCondition conditionInput = (generated.v10.dkf.PowerPointDwellCondition) inputType;

                            generated.dkf.PowerPointDwellCondition newConditionInput = new generated.dkf.PowerPointDwellCondition();

                            generated.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);

                            generated.dkf.PowerPointDwellCondition.Slides slides = new generated.dkf.PowerPointDwellCondition.Slides();
                            for (generated.v10.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput
                                    .getSlides().getSlide()) {

                                generated.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());

                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.RulesOfEngagementCondition) {

                            generated.v10.dkf.RulesOfEngagementCondition conditionInput = (generated.v10.dkf.RulesOfEngagementCondition) inputType;

                            generated.dkf.RulesOfEngagementCondition newConditionInput = new generated.dkf.RulesOfEngagementCondition();
                            generated.dkf.Wcs newWCS = new generated.dkf.Wcs();
                            newWCS.setValue(generated.dkf.WeaponControlStatusEnum
                                    .fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);

                            if (conditionInput.getRealTimeAssessmentRules() != null ) {
                                newConditionInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            }
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.SIMILEConditionInput) {

                            generated.v10.dkf.SIMILEConditionInput conditionInput = (generated.v10.dkf.SIMILEConditionInput) inputType;

                            generated.dkf.SIMILEConditionInput newConditionInput = new generated.dkf.SIMILEConditionInput();

                            if (conditionInput.getConditionKey() != null) {
                                newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }

                            if (conditionInput.getConfigurationFile() != null) {
                                newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.SpotReportCondition) {

                            generated.v10.dkf.SpotReportCondition conditionInput = (generated.v10.dkf.SpotReportCondition) inputType;

                            generated.dkf.SpotReportCondition newConditionInput = new generated.dkf.SpotReportCondition();
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.TimerConditionInput) {

                            generated.v10.dkf.TimerConditionInput conditionInput = (generated.v10.dkf.TimerConditionInput) inputType;

                            generated.dkf.TimerConditionInput newConditionInput = new generated.dkf.TimerConditionInput();

                            newConditionInput.setRepeatable(generated.dkf.BooleanEnum
                                    .fromValue(conditionInput.getRepeatable().toString().toLowerCase()));
                            newConditionInput.setInterval(conditionInput.getInterval());
                            
                            if(conditionInput.getTeamMemberRefs() != null){
                                newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));
                            }

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.UseRadioCondition) {

                            generated.v10.dkf.UseRadioCondition conditionInput = (generated.v10.dkf.UseRadioCondition) inputType;

                            generated.dkf.UseRadioCondition newConditionInput = new generated.dkf.UseRadioCondition();
                            
                            newConditionInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));

                            newInput.setType(newConditionInput);

                        } else if (inputType instanceof generated.v10.dkf.PaceCountCondition) {
                            
                            generated.v10.dkf.PaceCountCondition paceCountCondition = (generated.v10.dkf.PaceCountCondition) inputType;

                            generated.dkf.PaceCountCondition newPaceCountCondition = new generated.dkf.PaceCountCondition();
                            
                            newPaceCountCondition.setDistanceThreshold(paceCountCondition.getDistanceThreshold());
                            newPaceCountCondition.setExpectedDistance(paceCountCondition.getExpectedDistance());
                            // newly added during v10
                            newPaceCountCondition.setTeamMemberRef(null);
                            
                            newPaceCountCondition.setTeamMemberRef(paceCountCondition.getTeamMemberRef());
                            
                            newInput.setType(newPaceCountCondition);
                            
                        } else if(inputType instanceof generated.v10.dkf.HaltConditionInput){
                            
                            generated.v10.dkf.HaltConditionInput conditionInput = (generated.v10.dkf.HaltConditionInput)inputType;
                            generated.dkf.HaltConditionInput newCondInput = new generated.dkf.HaltConditionInput();
                            
                            newCondInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            newCondInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));
                            
                            newInput.setType(newCondInput);
                            
                        } else if(inputType instanceof generated.v10.dkf.HealthConditionInput){
                            
                            generated.v10.dkf.HealthConditionInput conditionInput = (generated.v10.dkf.HealthConditionInput)inputType;
                            generated.dkf.HealthConditionInput newCondInput = new generated.dkf.HealthConditionInput();
                            
                            newCondInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            newCondInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));
                            
                            newInput.setType(newCondInput);
                            
                        } else if(inputType instanceof generated.v10.dkf.MuzzleFlaggingCondition){
                            
                            generated.v10.dkf.MuzzleFlaggingCondition conditionInput = (generated.v10.dkf.MuzzleFlaggingCondition)inputType;
                            generated.dkf.MuzzleFlaggingCondition newCondInput = new generated.dkf.MuzzleFlaggingCondition();
                            
                            newCondInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            newCondInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));
                            
                            newCondInput.setMaxAngle(conditionInput.getMaxAngle());
                            newCondInput.setMaxDistance(conditionInput.getMaxDistance());
                            
                            newInput.setType(newCondInput);
                            
                        } else if(inputType instanceof generated.v10.dkf.ObservedAssessmentCondition){
                            
                            generated.v10.dkf.ObservedAssessmentCondition conditionInput = (generated.v10.dkf.ObservedAssessmentCondition)inputType;
                            generated.dkf.ObservedAssessmentCondition newCondInput = new generated.dkf.ObservedAssessmentCondition();
                            
                            newCondInput.setTeamMemberRefs(convertTeamMemberRefs(conditionInput.getTeamMemberRefs()));
                            
                            newInput.setType(newCondInput);
                            
                        } else if(inputType instanceof generated.v10.dkf.SpacingCondition){
                            
                            generated.v10.dkf.SpacingCondition conditionInput = (generated.v10.dkf.SpacingCondition)inputType;
                            generated.dkf.SpacingCondition newCondInput = new generated.dkf.SpacingCondition();
                            
                            newCondInput.setMinDurationBeforeViolation(conditionInput.getMinDurationBeforeViolation());
                            
                            for(generated.v10.dkf.SpacingCondition.SpacingPair sPair : conditionInput.getSpacingPair()){
                                generated.dkf.SpacingCondition.SpacingPair newSPair = new generated.dkf.SpacingCondition.SpacingPair();
                                
                                generated.v10.dkf.SpacingCondition.SpacingPair.Acceptable acceptable = sPair.getAcceptable();
                                generated.dkf.SpacingCondition.SpacingPair.Acceptable newAcceptable = new generated.dkf.SpacingCondition.SpacingPair.Acceptable();
                                newAcceptable.setAcceptableMaxSpacing(acceptable.getAcceptableMaxSpacing());
                                newAcceptable.setAcceptableMinSpacing(acceptable.getAcceptableMinSpacing());
                                newSPair.setAcceptable(newAcceptable);
                                
                                generated.v10.dkf.SpacingCondition.SpacingPair.FirstObject firstObj = sPair.getFirstObject();
                                generated.dkf.SpacingCondition.SpacingPair.FirstObject newFirstObj = new generated.dkf.SpacingCondition.SpacingPair.FirstObject();
                                newFirstObj.setTeamMemberRef(firstObj.getTeamMemberRef());
                                newSPair.setFirstObject(newFirstObj);
                                
                                generated.v10.dkf.SpacingCondition.SpacingPair.SecondObject secondObj = sPair.getSecondObject();
                                generated.dkf.SpacingCondition.SpacingPair.SecondObject newSecondObj = new generated.dkf.SpacingCondition.SpacingPair.SecondObject();
                                newSecondObj.setTeamMemberRef(secondObj.getTeamMemberRef());
                                newSPair.setSecondObject(newSecondObj);
                                
                                generated.v10.dkf.SpacingCondition.SpacingPair.Ideal ideal = sPair.getIdeal();
                                generated.dkf.SpacingCondition.SpacingPair.Ideal newIdeal = new generated.dkf.SpacingCondition.SpacingPair.Ideal();
                                newIdeal.setIdealMaxSpacing(ideal.getIdealMaxSpacing());
                                newIdeal.setIdealMinSpacing(ideal.getIdealMinSpacing());
                                newSPair.setIdeal(newIdeal);
                                
                                newCondInput.getSpacingPair().add(newSPair);
                            }
                            
                            newInput.setType(newCondInput);
                            
                        } else if(inputType instanceof generated.v10.dkf.SpeedLimitCondition){
                            
                            generated.v10.dkf.SpeedLimitCondition conditionInput = (generated.v10.dkf.SpeedLimitCondition)inputType;
                            generated.dkf.SpeedLimitCondition newCondInput = new generated.dkf.SpeedLimitCondition();
                            
                            newCondInput.setMinDurationBeforeViolation(conditionInput.getMinDurationBeforeViolation());
                            newCondInput.setMinSpeedLimit(conditionInput.getMinSpeedLimit());
                            newCondInput.setSpeedLimit(conditionInput.getSpeedLimit());
                            
                            newCondInput.setRealTimeAssessmentRules(convertRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules()));
                            newCondInput.setTeamMemberRef(conditionInput.getTeamMemberRef());
                            
                            newInput.setType(newCondInput);
                            
                        } else {
                            throw new IllegalArgumentException("Found unhandled condition input type of " + inputType);
                        }

                    }
                    newCondition.setInput(newInput);

                    // Scoring
                    generated.dkf.Scoring newScoring = new generated.dkf.Scoring();
                    if (condition.getScoring() != null) {
                        // Only add the scoring element if it has children.
                        // As of version 5, there cannot be a scoring element with no children
                        if (!condition.getScoring().getType().isEmpty()) {

                            for (Object scoringType : condition.getScoring().getType()) {

                                if (scoringType instanceof generated.v10.dkf.Count) {

                                    generated.v10.dkf.Count count = (generated.v10.dkf.Count) scoringType;

                                    generated.dkf.Count newCount = new generated.dkf.Count();
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.dkf.UnitsEnumType.fromValue(count.getUnits().value()));

                                    if (count.getEvaluators() != null) {
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }

                                    newScoring.getType().add(newCount);

                                } else if (scoringType instanceof generated.v10.dkf.CompletionTime) {

                                    generated.v10.dkf.CompletionTime complTime = (generated.v10.dkf.CompletionTime) scoringType;

                                    generated.dkf.CompletionTime newComplTime = new generated.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(
                                            generated.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if (complTime.getEvaluators() != null) {
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }

                                    newScoring.getType().add(newComplTime);

                                } else if (scoringType instanceof generated.v10.dkf.ViolationTime) {

                                    generated.v10.dkf.ViolationTime violationTime = (generated.v10.dkf.ViolationTime) scoringType;

                                    generated.dkf.ViolationTime newViolationTime = new generated.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(
                                            generated.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
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
                generated.dkf.PerformanceMetric newPerformanceMetric = new generated.dkf.PerformanceMetric();
                newPerformanceMetric.setPerformanceMetricImpl(concept.getPerformanceMetric().getPerformanceMetricImpl());
                newConcept.setPerformanceMetric(newPerformanceMetric);
            }
            
            if (concept.getConfidenceMetric() != null) {
                generated.dkf.ConfidenceMetric newConfidenceMetric = new generated.dkf.ConfidenceMetric();
                newConfidenceMetric.setConfidenceMetricImpl(concept.getConfidenceMetric().getConfidenceMetricImpl());
                newConcept.setConfidenceMetric(newConfidenceMetric);
            }

            if (concept.getCompetenceMetric() != null) {
                generated.dkf.CompetenceMetric newCompetenceMetric = new generated.dkf.CompetenceMetric();
                newCompetenceMetric.setCompetenceMetricImpl(concept.getCompetenceMetric().getCompetenceMetricImpl());
                newConcept.setCompetenceMetric(newCompetenceMetric);
            }

            if (concept.getTrendMetric() != null) {
                generated.dkf.TrendMetric newTrendMetric = new generated.dkf.TrendMetric();
                newTrendMetric.setTrendMetricImpl(concept.getTrendMetric().getTrendMetricImpl());
                newConcept.setTrendMetric(newTrendMetric);
            }

            if (concept.getPriorityMetric() != null) {
                generated.dkf.PriorityMetric newPriorityMetric = new generated.dkf.PriorityMetric();
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
     * @return the new object.  If null is provided, null is returned.
     */
    private generated.dkf.RealTimeAssessmentRules convertRealTimeAssessmentRules(
            generated.v10.dkf.RealTimeAssessmentRules realTimeAssessmentRules) {
        
        if(realTimeAssessmentRules == null){
            return null;
        }
        
        generated.dkf.RealTimeAssessmentRules newRealTimeAssessmentRules = new generated.dkf.RealTimeAssessmentRules();
        
        if (realTimeAssessmentRules.getCount() != null) {
            generated.v10.dkf.Count count = realTimeAssessmentRules.getCount();
            generated.dkf.Count newCount = new generated.dkf.Count();
            if (count.getEvaluators() != null) {
                newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
            }
            newCount.setName(count.getName());
            newCount.setUnits(generated.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
            newRealTimeAssessmentRules.setCount(newCount);
        }
        
        if (realTimeAssessmentRules.getViolationTime() != null) {
            generated.v10.dkf.ViolationTime violationTime = realTimeAssessmentRules.getViolationTime();
            generated.dkf.ViolationTime newViolationTime = new generated.dkf.ViolationTime();
            if (violationTime.getEvaluators() != null) {
                newViolationTime.setEvaluators(convertEvaluators(violationTime.getEvaluators()));
            }
            newViolationTime.setName(violationTime.getName());
            newViolationTime.setUnits(generated.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
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
    private generated.dkf.Nvpair convertNvpair(generated.v10.dkf.Nvpair nvPair) {

        generated.dkf.Nvpair newNvpair = new generated.dkf.Nvpair();
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
    private generated.dkf.PointRef convertPointRef(generated.v10.dkf.PointRef pointRef){

        generated.dkf.PointRef newPoint = new generated.dkf.PointRef();
        newPoint.setValue(pointRef.getValue());
        newPoint.setDistance(pointRef.getDistance());

        return newPoint;
    }

    /**
     * Convert an AreaRef object to a new AreaRef
     * @param AreaRef
     * @return
     */
    private generated.dkf.AreaRef convertAreaRef(generated.v10.dkf.AreaRef areaRef) {

        generated.dkf.AreaRef newAreaRef = new generated.dkf.AreaRef();
        newAreaRef.setValue(areaRef.getValue());

        return newAreaRef;
    }

    /**
     * Converts a boolean enum to the next version
     * 
     * @param booleanEnum the boolean enum to convert
     * @return the converted boolean enum
     */
    private generated.dkf.BooleanEnum convertBooleanEnum(generated.v10.dkf.BooleanEnum booleanEnum) {
        if (booleanEnum == null) {
            return null;
        }

        return generated.v10.dkf.BooleanEnum.TRUE.equals(booleanEnum) ? generated.dkf.BooleanEnum.TRUE
                : generated.dkf.BooleanEnum.FALSE;
    }

    /******************* END CONVERT DKF *******************/
}
