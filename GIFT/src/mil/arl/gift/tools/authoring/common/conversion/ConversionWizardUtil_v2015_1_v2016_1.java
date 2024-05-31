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
 * This class provides logic to migrate GIFT v2014-3X XML files to GIFT v2015-1 XML files using the appropriate schemas and generated
 * classes.  
 * 
 * Note: the only changes in XML schemas between these two version is at the DKF and Course levels.
 * 
 * @author bzahid
 *
 */
public class ConversionWizardUtil_v2015_1_v2016_1 extends AbstractConversionWizardUtil {

    //////////////////////////////////////////////////////////////
    /////////// DON'T REMOVE THE ITEMS IN THIS SECTION ///////////
    //////////////////////////////////////////////////////////////

    /** The new version number */
    private static final String VERSION_NUMBER = "7.0.1";

    @Override
    public String getConvertedVersionNumber() {
        return VERSION_NUMBER;
    }

    /********* PREVIOUS SCHEMA FILES *********/

    /** Path to the specific version folder */
    private static final String versionPathPrefix = StringUtils.join(File.separator,
            Arrays.asList("data", "conversionWizard", "v2015_1"));

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
    public File getPreviousLessonMaterialRefSchemaFile() {
        return PREV_LESSON_MATERIAL_SCHEMA_FILE;
    }

    /********* PREVIOUS SCHEMA ROOTS *********/

    /** Course schema root */
    public static final Class<?> PREV_COURSE_ROOT = generated.v6.course.Course.class;

    /** DKF schema root */
    public static final Class<?> PREV_DKF_ROOT = generated.v6.dkf.Scenario.class;

    /** Metadata schema root */
    public static final Class<?> PREV_METADATA_ROOT = generated.v6.metadata.Metadata.class;

    /** Learner schema root */
    public static final Class<?> PREV_LEARNER_ROOT = generated.v6.learner.LearnerConfiguration.class;

    /** Pedagogical schema root */
    public static final Class<?> PREV_PEDAGOGICAL_ROOT = generated.v6.ped.EMAP.class;

    /** Sensor schema root */
    public static final Class<?> PREV_SENSOR_ROOT = generated.v6.sensor.SensorsConfiguration.class;

    /** Training App schema root */
    public static final Class<?> PREV_TRAINING_APP_ROOT = generated.v6.course.TrainingApplicationWrapper.class;

    /** Lesson Material schema root */
    public static final Class<?> PREV_LESSON_MATERIAL_ROOT = generated.v6.course.LessonMaterialList.class;

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
    public Class<?> getPreviousLessonMaterialRefSchemaRoot() {
        return PREV_LESSON_MATERIAL_ROOT;
    }

    //////////////////////////////////////
    /////////// END OF SECTION ///////////
    //////////////////////////////////////

    private static final String OLD_VBS_INTEROP = "gateway.interop.vbs2plugin.VBS2PluginInterface";
    private static final String NEW_VBS_INTEROP = "gateway.interop.vbsplugin.VBSPluginInterface";
    
    /**
     * Auto-generate a GIFT v6.0 dkf object with every element/attribute instantiated.
     *  
     * @return generated.v6.dkf.Scenario - new 5.0 dkf object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v6.dkf.Scenario createScenario() throws Exception{
                
        Node rootNode = new Node();
        Object obj = createFullInstance(PREV_DKF_ROOT, rootNode);
        return (generated.v6.dkf.Scenario)obj;
    }
    
    @Override
    public UnmarshalledFile convertCourse(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(courseFile, getPreviousCourseSchemaFile(), getPreviousCourseSchemaRoot(), failOnFirstSchemaError);
        generated.v6.course.Course v6Course = (generated.v6.course.Course)uFile.getUnmarshalled();
        
        // Convert the version 5 course to the newest version and return it
        return convertCourse(v6Course, showCompletionDialog);  
    }

    /**
     * Convert the previous course schema object to a newer version of the course schema.
     * 
     * @param v6Course - the course schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return the new course
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertCourse(generated.v6.course.Course v6Course, boolean showCompletionDialog) throws IllegalArgumentException{
        
        generated.course.Course newCourse = new generated.course.Course();       
        
        //
        // copy over contents from old object to new object
        //    
        newCourse.setName(v6Course.getName());
        newCourse.setVersion(VERSION_NUMBER);
        newCourse.setDescription(v6Course.getDescription());
        newCourse.setSurveyContext(v6Course.getSurveyContext());
   
        if (v6Course.getExclude() != null) {
            newCourse.setExclude(generated.course.BooleanEnum.fromValue(v6Course.getExclude().toString().toLowerCase()));
        }
        
        if(v6Course.getConcepts() != null) {
            //CONCEPTS
            
            generated.course.Concepts newConcepts = convertConcepts(v6Course.getConcepts());
            newCourse.setConcepts(newConcepts);
        }
        
        //TRANSITIONS
        generated.course.Transitions newTransitions = convertTransitions(v6Course.getTransitions());
        newCourse.setTransitions(newTransitions);

        return super.convertCourse(newCourse, showCompletionDialog);
    }
    
    /**
     * Begin the conversion of a v6.1 file to current version by parsing the old file for the learner config
     * schema object and convert it to a newer version.
     */
    @Override
    public UnmarshalledFile convertLearnerConfiguration(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(courseFile, PREV_LEARNER_SCHEMA_FILE, PREV_LEARNER_ROOT, failOnFirstSchemaError);
        generated.v6.learner.LearnerConfiguration newLearnerConfig = (generated.v6.learner.LearnerConfiguration)uFile.getUnmarshalled();        
        
        return convertLearnerConfiguration(newLearnerConfig, showCompletionDialog);
    }
    
    /**
     * Convert the previous learner schema object to a newer version of the course schema.
     * 
     * @param v6LearnerConfig - version of the config file to be converted 
     * @param showCompletionDialog - show the completion dialog
     * @return - the freshly converted config file
     */
    public UnmarshalledFile convertLearnerConfiguration(generated.v6.learner.LearnerConfiguration v6LearnerConfig, boolean showCompletionDialog) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        generated.learner.LearnerConfiguration newLearnerConfig = new generated.learner.LearnerConfiguration();
        newLearnerConfig.setVersion(VERSION_NUMBER);
        newLearnerConfig.setInputs(convertInputs(v6LearnerConfig.getInputs()));

        return super.convertLearnerConfiguration(newLearnerConfig, showCompletionDialog);
    }

    /**
     * Takes all the inputs to a learner config file and individually converts them to the next version
     * @param v6Inputs - the previous version of Inputs
     * @return - the next version up of inputs
     */
    private generated.learner.Inputs convertInputs(generated.v6.learner.Inputs inputs) {
        generated.learner.Inputs newInputs = new generated.learner.Inputs();
        int index = 0;
        
        for(generated.v6.learner.Input input : inputs.getInput()){
            newInputs.getInput().add(new generated.learner.Input());
            
            //create the classifier
            generated.learner.Classifier classifier = new generated.learner.Classifier(); 
            classifier.setClassifierImpl(input.getClassifier().getClassifierImpl());
            if(input.getClassifier().getProperties() != null){
                classifier.setProperties(convertProperties(input.getClassifier().getProperties()));
            }
            newInputs.getInput().get(index).setClassifier(classifier);
            
            //create the Predictor
            generated.learner.Predictor predictor = new generated.learner.Predictor();
            predictor.setPredictorImpl(input.getPredictor().getPredictorImpl());
            newInputs.getInput().get(index).setPredictor(predictor);
            
            //create and set the producers
            if(input.getProducers() != null){
                newInputs.getInput().get(index).setProducers(convertProducers(input.getProducers()));
            }
            
            //create the Translator
            generated.learner.Translator translator = new generated.learner.Translator();
            translator.setTranslatorImpl(input.getTranslator().getTranslatorImpl());
            newInputs.getInput().get(index).setTranslator(translator);
            
            
            index++;
        }
        
        
        return newInputs;
    }
    
    /**
     * Helper method to convert the producers in a learner config file to the next version
     * 
     * @param v6producers - current version learner config producers
     * @return - v6.0 producers
     */
    private generated.learner.Producers convertProducers(generated.v6.learner.Producers v6Producers) {
        generated.learner.Producers newProducers = new generated.learner.Producers();
        int index = 0;
        
        
        for(Serializable producer : v6Producers.getProducerType()){
            if(producer instanceof generated.v6.learner.Sensor){
                generated.v6.learner.Sensor sensor = (generated.v6.learner.Sensor) producer;
                if(sensor.getType() != null && !sensor.getType().isEmpty()){
                    newProducers.getProducerType().add(new generated.learner.Sensor());
                    ((generated.learner.Sensor) newProducers.getProducerType().get(index)).setType(sensor.getType());
                }
                index++;
            } else if (producer instanceof generated.v6.learner.TrainingAppState){
                generated.v6.learner.TrainingAppState traningAppState = (generated.v6.learner.TrainingAppState) producer;
                if(traningAppState.getType() != null && !traningAppState.getType().isEmpty()){
                    newProducers.getProducerType().add(new generated.learner.TrainingAppState());
                    ((generated.learner.TrainingAppState) newProducers.getProducerType().get(index)).setType(traningAppState.getType());
                }
                index++;
            } else {
                throw new IllegalArgumentException("found unhandled producer of type " + producer);
            }
        }
        return newProducers;
    }

    /**
     * Converts the Properties of a classifier from v6 to v6 
     * 
     * @param v6Properties - properties of the v6 classifier
     * @return the same properties only in v6
     */
    private generated.learner.Properties convertProperties(generated.v6.learner.Properties v6Properties){
        generated.learner.Properties newProperties = new generated.learner.Properties();
        int index = 0;
        
        for(generated.v6.learner.Property v6Property : v6Properties.getProperty()){
            newProperties.getProperty().add(new generated.learner.Property());
            newProperties.getProperty().get(index).setName(v6Property.getName());
            newProperties.getProperty().get(index).setValue(v6Property.getValue());
            index++;
        }
        
        return newProperties;
    }
    
    /**
     * Convert a Transitions course element.
     * 
     * @param transitions - the transitions element to migrate to a newer version
     * @return generated.course.Transitions - the converted transitions element
     * @throws IllegalArgumentException
     */
    private static generated.course.Transitions convertTransitions(generated.v6.course.Transitions transitions) throws IllegalArgumentException {

        generated.course.Transitions newTransitions = new generated.course.Transitions();
        
        for(Object transitionObj : transitions.getTransitionType()){
            
            if(transitionObj instanceof generated.v6.course.Guidance){
                
                generated.v6.course.Guidance guidance = (generated.v6.course.Guidance)transitionObj;
                generated.course.Guidance newGuidance = convertGuidance(guidance, null);
                
                newTransitions.getTransitionType().add(newGuidance);
                
            }else if(transitionObj instanceof generated.v6.course.PresentSurvey){
                
                generated.v6.course.PresentSurvey presentSurvey = (generated.v6.course.PresentSurvey)transitionObj;
                generated.course.PresentSurvey newPresentSurvey = convertPresentSurvey(presentSurvey);
                
                newPresentSurvey.setTransitionName(presentSurvey.getTransitionName());
                
                newTransitions.getTransitionType().add(newPresentSurvey);
                
            }else if(transitionObj instanceof generated.v6.course.AAR){
                
                generated.v6.course.AAR aar = (generated.v6.course.AAR)transitionObj;               
                generated.course.AAR newAAR = new generated.course.AAR();
                
                newAAR.setTransitionName(aar.getTransitionName());
                
                if(aar.getFullScreen() != null){
                    newAAR.setFullScreen(generated.course.BooleanEnum.fromValue(aar.getFullScreen().toString().toLowerCase()));
                }
                
                newTransitions.getTransitionType().add(newAAR);
                
            }else if(transitionObj instanceof generated.v6.course.TrainingApplication){
                
                generated.v6.course.TrainingApplication trainApp = (generated.v6.course.TrainingApplication)transitionObj;
                generated.course.TrainingApplication newTrainApp = convertTrainingApplication(trainApp);
                
                
                newTransitions.getTransitionType().add(newTrainApp);
                
            }else if(transitionObj instanceof generated.v6.course.LessonMaterial){
                
                generated.v6.course.LessonMaterial lessonMaterial = (generated.v6.course.LessonMaterial)transitionObj;
                generated.course.LessonMaterial newLessonMaterial = new generated.course.LessonMaterial();
                
                newLessonMaterial.setTransitionName(lessonMaterial.getTransitionName());
                
                if(lessonMaterial.getLessonMaterialList() != null){
                    generated.course.LessonMaterialList newLessonMaterialList = new generated.course.LessonMaterialList();

                    for(generated.v6.course.Media media : lessonMaterial.getLessonMaterialList().getMedia()){
                        
                        generated.course.Media newMedia = new generated.course.Media();
                        newMedia.setName(media.getName());
                        newMedia.setUri(media.getUri());
                        
                        Object mediaType = media.getMediaTypeProperties();
                        if(mediaType instanceof generated.v6.course.PDFProperties){
                            newMedia.setMediaTypeProperties(new generated.course.PDFProperties());
                            
                        }else if(mediaType instanceof generated.v6.course.WebpageProperties){
                            newMedia.setMediaTypeProperties(new generated.course.WebpageProperties());
                            
                        }else if(mediaType instanceof generated.v6.course.YoutubeVideoProperties){
                            
                            generated.v6.course.YoutubeVideoProperties uTubeProp = (generated.v6.course.YoutubeVideoProperties)mediaType;
                            generated.course.YoutubeVideoProperties newUTubeProp = new generated.course.YoutubeVideoProperties();
                            
                            if (uTubeProp.getAllowFullScreen() != null) {
                                newUTubeProp.setAllowFullScreen(generated.course.BooleanEnum.fromValue(uTubeProp.getAllowFullScreen().toString().toLowerCase()));
                            }
                            if (uTubeProp.getAllowAutoPlay() != null) {
                                newUTubeProp.setAllowAutoPlay(generated.course.BooleanEnum.fromValue(uTubeProp.getAllowAutoPlay().toString().toLowerCase()));
                            }

                            newMedia.setMediaTypeProperties(newUTubeProp);
                            
                        }else if(mediaType instanceof generated.v6.course.ImageProperties){
                            newMedia.setMediaTypeProperties(new generated.course.ImageProperties());
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled media type of "+mediaType);
                        }
                        
                        newLessonMaterialList.getMedia().add(newMedia);
                    }
                    
                    newLessonMaterial.setLessonMaterialList(newLessonMaterialList);
                }

                if(lessonMaterial.getLessonMaterialFiles() != null){
                    
                    generated.course.LessonMaterialFiles newFiles = new generated.course.LessonMaterialFiles();
                    for(String file : lessonMaterial.getLessonMaterialFiles().getFile()){
                        newFiles.getFile().add(file);
                    }                    
                    
                    newLessonMaterial.setLessonMaterialFiles(newFiles);                 
                }
                
                newTransitions.getTransitionType().add(newLessonMaterial);
                
            }else if(transitionObj instanceof generated.v6.course.MerrillsBranchPoint){
                
                generated.v6.course.MerrillsBranchPoint mBranchPoint = (generated.v6.course.MerrillsBranchPoint)transitionObj;          
                generated.course.MerrillsBranchPoint newMBranchPoint = new generated.course.MerrillsBranchPoint();
                
                newMBranchPoint.setTransitionName(mBranchPoint.getTransitionName());
                
                generated.course.MerrillsBranchPoint.Concepts newConcepts = new generated.course.MerrillsBranchPoint.Concepts();
                newConcepts.getConcept().addAll(mBranchPoint.getConcepts().getConcept());
                newMBranchPoint.setConcepts(newConcepts);
                
                generated.course.MerrillsBranchPoint.Quadrants newQuadrants = convertQuadrants(mBranchPoint.getQuadrants(), newMBranchPoint.getTransitionName());               
                newMBranchPoint.setQuadrants(newQuadrants);
                
                newTransitions.getTransitionType().add(newMBranchPoint);
                
            }else{
                throw new IllegalArgumentException("Found unhandled transition type of "+transitionObj);
            }
        }
        
        return newTransitions;
        
    }
    
    
    /**
     * Convert a Quadrants course element.
     * 
     * @param quadrants - the quadrants element to migrate to a newer version
     * @param parentTransitionName - used to generate a name for a guidance transition within a MBP transition. Can be null.
     * @return generated.course.MerrillsBranchPoint.Quadrants - the converted quadrants element
     */
    private static generated.course.MerrillsBranchPoint.Quadrants convertQuadrants(generated.v6.course.MerrillsBranchPoint.Quadrants quadrants, String parentTransitionName) throws IllegalArgumentException {
        
        generated.course.MerrillsBranchPoint.Quadrants newQuadrants = new generated.course.MerrillsBranchPoint.Quadrants();
        
        for (Object quadrant : quadrants.getContent()) {
            if (quadrant instanceof generated.v6.course.Rule) {
            
                newQuadrants.getContent().add(new generated.course.Rule());
                
            }else if (quadrant instanceof generated.v6.course.Practice) {
                
                generated.v6.course.Practice practice = (generated.v6.course.Practice)quadrant;
                generated.course.Practice newPractice = new generated.course.Practice();
                generated.course.Practice.PracticeConcepts newConcept = new generated.course.Practice.PracticeConcepts();
                
                newConcept.getCourseConcept().addAll(practice.getPracticeConcepts().getCourseConcept());
                
                newPractice.setPracticeConcepts(newConcept);
                newPractice.setAllowedAttempts(practice.getAllowedAttempts());
                
                newQuadrants.getContent().add(newPractice);
                
            }else if (quadrant instanceof generated.v6.course.Recall) {
                
                generated.v6.course.Recall recall = (generated.v6.course.Recall)quadrant;
                
                generated.course.Recall newRecall = new generated.course.Recall();
                
                // Convert QuestionTypes and AssessmentRules
                generated.course.Recall.PresentSurvey survey = new generated.course.Recall.PresentSurvey();
                survey.setSurveyChoice(new generated.course.Recall.PresentSurvey.ConceptSurvey());
                
                survey.getSurveyChoice().getConceptQuestions().addAll(
                        convertConceptQuestions(recall.getPresentSurvey().getSurveyChoice().getConceptQuestions()));
                
                survey.getSurveyChoice().setGIFTSurveyKey(recall.getPresentSurvey().getSurveyChoice().getGIFTSurveyKey());
                
                newRecall.setPresentSurvey(survey);
                
                // Copy allowed attempts
                newRecall.setAllowedAttempts(recall.getAllowedAttempts());
                
                if(recall.getPresentSurvey().getFullScreen() != null) {
                    // Copy fullScreen
                    
                    newRecall.getPresentSurvey().setFullScreen(generated.course.BooleanEnum.fromValue(
                            recall.getPresentSurvey().getFullScreen().toString().toLowerCase()));
                }
                
                newQuadrants.getContent().add(newRecall);
                
            }else if (quadrant instanceof generated.v6.course.Example) {
                
                newQuadrants.getContent().add(new generated.course.Example());
                
            }else if (quadrant instanceof generated.v6.course.Transitions) {
                
                generated.v6.course.Transitions transitions = (generated.v6.course.Transitions)quadrant;
                generated.course.Transitions newTransitions = convertTransitions(transitions);
                
                //Cycle back through MBP transitions to verify they have names, if not
                //generated a name based on the parent transition name
                for(Object transition : newTransitions.getTransitionType()){
                    if(transition instanceof generated.course.Guidance){
                        if(((generated.course.Guidance) transition).getTransitionName() == null){
                            ((generated.course.Guidance) transition).setTransitionName(parentTransitionName + " - Guidance");
                        }
                    }
                }
                newQuadrants.getContent().add(newTransitions);
                            
            }else {
                throw new IllegalArgumentException("Found unhandled transition type of "+quadrant);
            }
        }
        
        return newQuadrants;
    }
        
    
    /**
     * Convert a Guidance course element.
     * 
     * @param guidance - the guidance element to migrate to a newer version
     * @param generatedName - a transition name based on the parent transition, can be null
     * @return generated.course.Guidance - the converted guidance element
     * @throws IllegalArgumentException
     */
    private static generated.course.Guidance convertGuidance(generated.v6.course.Guidance guidance, String generatedName) throws IllegalArgumentException{
        
        generated.course.Guidance newGuidance = new generated.course.Guidance();
        
        newGuidance.setDisplayTime(guidance.getDisplayTime());
        
        if(guidance.getTransitionName() != null || generatedName != null){
            newGuidance.setTransitionName(guidance.getTransitionName() == null ? generatedName : guidance.getTransitionName());
        }
        
        if (guidance.getFullScreen() != null) {
            newGuidance.setFullScreen(generated.course.BooleanEnum.fromValue(guidance.getFullScreen().toString().toLowerCase()));
        }
        
        Object guidanceChoice = guidance.getGuidanceChoice();
        if (guidanceChoice instanceof generated.v6.course.Guidance.Message) {

            generated.v6.course.Guidance.Message message = (generated.v6.course.Guidance.Message)guidanceChoice;
            
            generated.course.Guidance.Message newMessage = new generated.course.Guidance.Message();
            newMessage.setContent(message.getContent());
            
            newGuidance.setGuidanceChoice(newMessage);
            
        }else if (guidanceChoice instanceof generated.v6.course.Guidance.File) {
            
            generated.v6.course.Guidance.File file = (generated.v6.course.Guidance.File)guidanceChoice;
            
            generated.course.Guidance.File newFile = new generated.course.Guidance.File();
            
            newFile.setHTML(file.getHTML());
            newFile.setMessage(file.getMessage());
            
            newGuidance.setGuidanceChoice(newFile);
            
        }else if (guidanceChoice instanceof generated.v6.course.Guidance.URL) {
            
            generated.v6.course.Guidance.URL url = (generated.v6.course.Guidance.URL)guidanceChoice;
            
            generated.course.Guidance.URL newURL = new generated.course.Guidance.URL();
            newURL.setAddress(url.getAddress());
            newURL.setMessage(url.getMessage());
            
            newGuidance.setGuidanceChoice(newURL);
            
        }else {
            throw new IllegalArgumentException("Found unhandled guidance choice type of "+guidanceChoice);
        }
        
        return newGuidance;
    }
    
    
    /**
     * Convert a PresentSurvey course element.
     * 
     * @param presentSurvey - the PresentSurvey element to migrate to a newer version
     * @return generated.course.PresentSurvey - the converted PresentSurvey element
     */    
    private static generated.course.PresentSurvey convertPresentSurvey(generated.v6.course.PresentSurvey presentSurvey) throws IllegalArgumentException{
        
        generated.course.PresentSurvey newPresentSurvey = new generated.course.PresentSurvey();
        
        if(presentSurvey.getFullScreen() != null){
            newPresentSurvey.setFullScreen(generated.course.BooleanEnum.fromValue(
                    presentSurvey.getFullScreen().toString().toLowerCase()));
        }
        if(presentSurvey.getShowInAAR() != null){
            newPresentSurvey.setShowInAAR(generated.course.BooleanEnum.fromValue(
                    presentSurvey.getShowInAAR().toString().toLowerCase()));
        }
        
        Object surveyChoice = presentSurvey.getSurveyChoice();
        if (surveyChoice instanceof generated.v6.course.AutoTutorSession) {
            
            generated.v6.course.AutoTutorSession autoTutorSession = (generated.v6.course.AutoTutorSession)surveyChoice;
            
            generated.course.AutoTutorSession newAutoTutorSession = new generated.course.AutoTutorSession();
            generated.course.Conversation newConversation = new generated.course.Conversation();
            
            generated.course.DkfRef newDkfRef = new generated.course.DkfRef();
            
            newDkfRef.setFile(autoTutorSession.getDkfRef().getFile());
            newAutoTutorSession.setAutoTutorConfiguration(newDkfRef);
            
            newConversation.setType(newAutoTutorSession);
            
            newPresentSurvey.setSurveyChoice(newConversation);
            
        }else if (surveyChoice instanceof generated.v6.course.PresentSurvey.ConceptSurvey ) {
            
            generated.v6.course.PresentSurvey.ConceptSurvey conceptSurvey = (generated.v6.course.PresentSurvey.ConceptSurvey)surveyChoice;
            
            generated.course.PresentSurvey.ConceptSurvey newConceptSurvey = new generated.course.PresentSurvey.ConceptSurvey();
            
            newConceptSurvey.setGIFTSurveyKey(conceptSurvey.getGIFTSurveyKey());
            
            if(conceptSurvey.getFullScreen() != null){
                newConceptSurvey.setFullScreen(generated.course.BooleanEnum.fromValue(
                        conceptSurvey.getFullScreen().toString().toLowerCase()));
            }
            
            if(conceptSurvey.getSkipConceptsByExamination() != null){
                newConceptSurvey.setSkipConceptsByExamination(generated.course.BooleanEnum.fromValue(
                        conceptSurvey.getSkipConceptsByExamination().toString().toLowerCase()));
            }
            
            newConceptSurvey.getConceptQuestions().addAll(convertConceptQuestions(conceptSurvey.getConceptQuestions()));
            
            newPresentSurvey.setSurveyChoice(newConceptSurvey);
            
        }else if (surveyChoice instanceof String) {
            
            newPresentSurvey.setSurveyChoice((String)surveyChoice);
        
        }else {
            throw new IllegalArgumentException("Found unhandled survey choice type of "+surveyChoice);
        }

        return newPresentSurvey;
    }   
    
    /**
     * Converts course concepts within a list or hierarchy
     * 
     * @param concepts - the v6.1 course concepts
     * @return the converted v6.0 course concepts
     */
    private generated.course.Concepts convertConcepts(generated.v6.course.Concepts concepts) {
        
        generated.course.Concepts newConcepts = new generated.course.Concepts();
        
        Object list = concepts.getListOrHierarchy();
        
        if(list instanceof generated.v6.course.Concepts.List) {
            
            generated.course.Concepts.List newList = new generated.course.Concepts.List();
            
            for(generated.v6.course.Concepts.List.Concept c : ((generated.v6.course.Concepts.List) list).getConcept()) {
                
                generated.course.Concepts.List.Concept concept = new generated.course.Concepts.List.Concept();
                
                concept.setName(c.getName());
                newList.getConcept().add(concept);
            }
            
            newConcepts.setListOrHierarchy(newList);
            
        } else {
            
            generated.v6.course.Concepts.Hierarchy hierarchy = (generated.v6.course.Concepts.Hierarchy)list;
            generated.course.Concepts.Hierarchy newList = new generated.course.Concepts.Hierarchy();
            generated.course.ConceptNode newSadness = new generated.course.ConceptNode();
            generated.v6.course.ConceptNode sadness = hierarchy.getConceptNode();
                        
            newSadness.setName(sadness.getName());      
            
            while(sadness.getConceptNode() != null) {
                
                generated.course.ConceptNode newNode = new generated.course.ConceptNode();
                newNode.setName(sadness.getName());
                
                newSadness.getConceptNode().add(newNode);
            }
            
            newList.setConceptNode(newSadness);
            newConcepts.setListOrHierarchy(newList);
        }
        
        return newConcepts;
    }    
    
    /** 
     * Converts course concept questions 
     * 
     * @param conceptQuestions - the list of v6.1 conceptQuestions
     * @return the converted list of v6.0 conceptQuestions
     */
    private static List<generated.course.ConceptQuestions> convertConceptQuestions(List<generated.v6.course.ConceptQuestions> conceptQuestions) {
        
        List<generated.course.ConceptQuestions> newConceptQuestions = new ArrayList<generated.course.ConceptQuestions>();
        
        for(generated.v6.course.ConceptQuestions q : conceptQuestions) {
            
            generated.course.ConceptQuestions newQuestion = new generated.course.ConceptQuestions();
            generated.course.ConceptQuestions.QuestionTypes newTypes = new generated.course.ConceptQuestions.QuestionTypes();
            generated.course.ConceptQuestions.AssessmentRules newRules = new generated.course.ConceptQuestions.AssessmentRules();
            generated.course.ConceptQuestions.AssessmentRules.AtExpectation atRule = new generated.course.ConceptQuestions.AssessmentRules.AtExpectation();
            generated.course.ConceptQuestions.AssessmentRules.AboveExpectation overRule = new generated.course.ConceptQuestions.AssessmentRules.AboveExpectation();
            generated.course.ConceptQuestions.AssessmentRules.BelowExpectation underRule = new generated.course.ConceptQuestions.AssessmentRules.BelowExpectation();
            
            newTypes.setEasy(q.getQuestionTypes().getEasy());
            newTypes.setHard(q.getQuestionTypes().getHard());
            newTypes.setMedium(q.getQuestionTypes().getMedium());
            
            atRule.setNumberCorrect(q.getAssessmentRules().getAtExpectation().getNumberCorrect());
            overRule.setNumberCorrect(q.getAssessmentRules().getAboveExpectation().getNumberCorrect());
            underRule.setNumberCorrect(q.getAssessmentRules().getBelowExpectation().getNumberCorrect());
            
            newRules.setAtExpectation(atRule);
            newRules.setAboveExpectation(overRule);
            newRules.setBelowExpectation(underRule);
            
            newQuestion.setName(q.getName());
            newQuestion.setQuestionTypes(newTypes);
            newQuestion.setAssessmentRules(newRules);
            
            newConceptQuestions.add(newQuestion);
        }
        
        return newConceptQuestions;
    }
    
    
    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(), failOnFirstSchemaError);
        generated.v6.dkf.Scenario v6Scenario = (generated.v6.dkf.Scenario)uFile.getUnmarshalled();

        return convertScenario(v6Scenario, showCompletionDialog);
    }
    
    /**
     * Convert the previous scenario schema object to a newer version of the scenario schema.
     * 
     * @param v6Scenario - the scenario schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return the new scenario
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertScenario(generated.v6.dkf.Scenario v6Scenario, boolean showCompletionDialog) throws IllegalArgumentException {
            
        generated.v7.dkf.Scenario newScenario = new generated.v7.dkf.Scenario();
         
        //
        // copy over contents from old object to new object
        //
        newScenario.setDescription(v6Scenario.getDescription());
        newScenario.setName(v6Scenario.getName());
        newScenario.setVersion(VERSION_NUMBER);
        
        //
        //Learner Id
        //
        if(v6Scenario.getLearnerId() != null){
            generated.v7.dkf.LearnerId newLearnerId = new generated.v7.dkf.LearnerId();
            generated.v7.dkf.StartLocation newStartLocation = new generated.v7.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(v6Scenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }
        
        //
        //Resources
        //
        generated.v7.dkf.Resources newResources = new generated.v7.dkf.Resources();
        newResources.setSurveyContext(v6Scenario.getResources().getSurveyContext());
        
        generated.v7.dkf.AvailableLearnerActions newALA = new generated.v7.dkf.AvailableLearnerActions();
        
        if(v6Scenario.getResources().getAvailableLearnerActions() != null){
            
            generated.v6.dkf.AvailableLearnerActions ala = v6Scenario.getResources().getAvailableLearnerActions();
            if(ala.getLearnerActionsFiles() != null){
                generated.v7.dkf.LearnerActionsFiles newLAF = new generated.v7.dkf.LearnerActionsFiles();
                for(String filename : ala.getLearnerActionsFiles().getFile()){
                    newLAF.getFile().add(filename);
                }
                
                newALA.setLearnerActionsFiles(newLAF);
            }
            
            if(ala.getLearnerActionsList() != null){
                
                generated.v7.dkf.LearnerActionsList newLAL = new generated.v7.dkf.LearnerActionsList();
                for(generated.v6.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()){
                    
                    generated.v7.dkf.LearnerAction newAction = new generated.v7.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    newAction.setType(generated.v7.dkf.LearnerActionEnumType.fromValue(action.getType().value()));
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
        generated.v7.dkf.Scenario.EndTriggers newScenarioEndTriggers = new generated.v7.dkf.Scenario.EndTriggers();
        
        if(v6Scenario.getEndTriggers() != null) {
            newScenarioEndTriggers.getTrigger().addAll(convertScenarioEndTriggers( v6Scenario.getEndTriggers().getTrigger()));
            newScenario.setEndTriggers(newScenarioEndTriggers);
        }
        
        //
        //Assessment
        //
        generated.v7.dkf.Assessment newAssessment = new generated.v7.dkf.Assessment();
        if(v6Scenario.getAssessment() != null){
            
            generated.v6.dkf.Assessment assessment = v6Scenario.getAssessment();
            
            //
            // Objects
            //
            generated.v7.dkf.Objects newObjects = new generated.v7.dkf.Objects();
            if(assessment.getObjects() != null){
                
                if(assessment.getObjects().getWaypoints() != null){
                    
                    generated.v7.dkf.Waypoints newWaypoints = new generated.v7.dkf.Waypoints();
                    
                    generated.v6.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for(generated.v6.dkf.Waypoint waypoint : waypoints.getWaypoint()){
                        
                        generated.v7.dkf.Waypoint newWaypoint = new generated.v7.dkf.Waypoint();
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
            generated.v7.dkf.Tasks newTasks = new generated.v7.dkf.Tasks();
            if(assessment.getTasks() != null){
                
                for(generated.v6.dkf.Task task : assessment.getTasks().getTask()){
                    
                    generated.v7.dkf.Task newTask = new generated.v7.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());
                    
                    // start triggers
                    if(task.getStartTriggers() != null){
                        generated.v7.dkf.StartTriggers newStartTriggers = new generated.v7.dkf.StartTriggers();
                        newStartTriggers.getTrigger().addAll(convertStartTriggers( task.getStartTriggers().getTrigger()));
                        newTask.setStartTriggers(newStartTriggers);
                    }
                    
                    // end triggers
                    if(task.getEndTriggers() != null){
                        generated.v7.dkf.EndTriggers newEndTriggers = new generated.v7.dkf.EndTriggers();
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
        if(v6Scenario.getActions() != null){
            
            generated.v6.dkf.Actions actions = v6Scenario.getActions();
            generated.v7.dkf.Actions newActions = new generated.v7.dkf.Actions();
            
            //instructional strategies
            if(actions.getInstructionalStrategies() != null){
                
                generated.v6.dkf.Actions.InstructionalStrategies iStrategies = actions.getInstructionalStrategies();
                generated.v7.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v7.dkf.Actions.InstructionalStrategies();
                
                for(generated.v6.dkf.Strategy strategy : iStrategies.getStrategy()){
                    
                    generated.v7.dkf.Strategy newStrategy = new generated.v7.dkf.Strategy();
                    newStrategy.setName(strategy.getName());
                    
                    Object strategyType = strategy.getStrategyType();//getValueAttribute();
                    if(strategyType instanceof generated.v6.dkf.PerformanceAssessment){
                        
                        generated.v6.dkf.PerformanceAssessment perfAss = (generated.v6.dkf.PerformanceAssessment)strategyType;
                        
                        generated.v7.dkf.PerformanceAssessment newPerfAss = new generated.v7.dkf.PerformanceAssessment();
                        generated.v7.dkf.PerformanceAssessment.PerformanceNode newPerfAssNode = new generated.v7.dkf.PerformanceAssessment.PerformanceNode();
                        
                        newPerfAssNode.setNodeId(perfAss.getNodeId());
                        newPerfAss.setAssessmentType(newPerfAssNode);
                        
                        newPerfAss.setStrategyHandler(convertStrategyHandler(perfAss.getStrategyHandler()));
                        
                        newStrategy.setStrategyType(newPerfAss);
                        
                    }else if(strategyType instanceof generated.v6.dkf.InstructionalIntervention){
                        
                        generated.v6.dkf.InstructionalIntervention iIntervention = (generated.v6.dkf.InstructionalIntervention)strategyType;
                        
                        generated.v7.dkf.InstructionalIntervention newIIntervention = new generated.v7.dkf.InstructionalIntervention();
                        newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));

                        //only have a feedback choice in this version
                        for(Serializable serFeedback : iIntervention.getInterventionTypes()){
                            generated.v6.dkf.Feedback feedback = null;
                            if(serFeedback instanceof generated.v6.dkf.Feedback){
                                feedback = (generated.v6.dkf.Feedback) serFeedback;
                            
                                generated.v7.dkf.Feedback newFeedback = new generated.v7.dkf.Feedback();
                                
                                if (feedback.getFeedbackPresentation() instanceof generated.v6.dkf.Message) {
           
                                    generated.v6.dkf.Message message = (generated.v6.dkf.Message) feedback.getFeedbackPresentation();
                                    generated.v7.dkf.Message feedbackMsg = convertMessage(message);
                                    
                                    newFeedback.setFeedbackPresentation(feedbackMsg);
                                    
                                }
                                else if (feedback.getFeedbackPresentation() instanceof generated.v6.dkf.Audio) {
                                    
                                    generated.v6.dkf.Audio audio = (generated.v6.dkf.Audio) feedback.getFeedbackPresentation();
                                    
                                    generated.v7.dkf.Audio newAudio = new generated.v7.dkf.Audio();
                                    
                                    // An audio object requires a .mp3 file but does not require a .ogg file
                                    newAudio.setMP3File(audio.getMP3File());
                                    
                                    if (audio.getOGGFile() != null) {
                                        newAudio.setOGGFile(audio.getOGGFile());
                                    }
                                    
                                    newFeedback.setFeedbackPresentation(newAudio);
                                    
                                }
                                
                                else if (feedback.getFeedbackPresentation() instanceof generated.v6.dkf.MediaSemantics) {
                                    
                                    generated.v6.dkf.MediaSemantics semantics = (generated.v6.dkf.MediaSemantics) feedback.getFeedbackPresentation();
                                    
                                    generated.v7.dkf.MediaSemantics newSemantics = new generated.v7.dkf.MediaSemantics();
                                    
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
                                
                                newStrategy.setStrategyType(newIIntervention);
                            } else {
                                throw new IllegalArgumentException("Found unhandled feedback type of " + serFeedback);
                            }
                        }
                        
                    }else if(strategyType instanceof generated.v6.dkf.ScenarioAdaptation){
                        
                        generated.v6.dkf.ScenarioAdaptation adaptation = (generated.v6.dkf.ScenarioAdaptation)strategyType;
                        
                        generated.v7.dkf.ScenarioAdaptation newAdaptation = new generated.v7.dkf.ScenarioAdaptation();
                        newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));
                        
                        //only have environment adaptation in this version
                        for(Serializable serEAdapt : adaptation.getAdaptationTypes()){
                            generated.v6.dkf.EnvironmentAdaptation eAdapt = null;
                            if(serEAdapt instanceof generated.v6.dkf.EnvironmentAdaptation){
                                eAdapt = (generated.v6.dkf.EnvironmentAdaptation) serEAdapt;
                            
                                generated.v7.dkf.EnvironmentAdaptation newEAdapt = new generated.v7.dkf.EnvironmentAdaptation();
                                
                                generated.v7.dkf.EnvironmentAdaptation.Pair newPair = new generated.v7.dkf.EnvironmentAdaptation.Pair();
                                newPair.setType(eAdapt.getPair().getType());
                                newPair.setValue(eAdapt.getPair().getValue());
                                newEAdapt.setPair(newPair);
                                
                                newAdaptation.getAdaptationTypes().add(newEAdapt);
                            } else {
                                throw new IllegalArgumentException("Found unhandled Environment Adaption type of " + serEAdapt);
                            }
                        }
                        
                        newStrategy.setStrategyType(newAdaptation);
                        
                    }else{
                        throw new IllegalArgumentException("Found unhandled strategy type of "+strategyType);
                    }
                    
                    
                    newIStrategies.getStrategy().add(newStrategy);
                }
                
                newActions.setInstructionalStrategies(newIStrategies);
            }

            //State transitions
            if(actions.getStateTransitions() != null){
               
                generated.v6.dkf.Actions.StateTransitions sTransitions = actions.getStateTransitions();
                generated.v7.dkf.Actions.StateTransitions newSTransitions = new generated.v7.dkf.Actions.StateTransitions();
                
                for(generated.v6.dkf.Actions.StateTransitions.StateTransition sTransition : sTransitions.getStateTransition()){
                    
                    generated.v7.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v7.dkf.Actions.StateTransitions.StateTransition();
                    
                    generated.v7.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v7.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();
                    
                    newSTransition.setName(sTransition.getName());
                    
                    //State type
                    for (Object stateType : sTransition.getLogicalExpression().getStateType()) {
                        if(stateType instanceof generated.v6.dkf.LearnerStateTransitionEnum){
    
                            generated.v6.dkf.LearnerStateTransitionEnum stateEnum = (generated.v6.dkf.LearnerStateTransitionEnum)stateType;
                            
                            generated.v7.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v7.dkf.LearnerStateTransitionEnum();
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
                            
                        }else if(stateType instanceof generated.v6.dkf.PerformanceNode){
                            
                            generated.v6.dkf.PerformanceNode perfNode = (generated.v6.dkf.PerformanceNode)stateType;
                            
                            generated.v7.dkf.PerformanceNode newPerfNode = new generated.v7.dkf.PerformanceNode();
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
                    generated.v7.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v7.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    for(generated.v6.dkf.StrategyRef strategyRef : sTransition.getStrategyChoices().getStrategyRef()){
                        
                        generated.v7.dkf.StrategyRef newStrategyRef = new generated.v7.dkf.StrategyRef();
                        newStrategyRef.setName(strategyRef.getName());
                        
                        newStrategyChoices.getStrategyRef().add(newStrategyRef);
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
     * Convert a Message object for scenarios
     * 
     * @param message - the message to convert
     * @return a new Message object
     */
    private generated.v7.dkf.Message convertMessage(generated.v6.dkf.Message message) {
        generated.v7.dkf.Message newMessage = new generated.v7.dkf.Message();
        newMessage.setContent(message.getContent());
        
        if(message.getDelivery() != null){
            generated.v7.dkf.Message.Delivery newDelivery = new generated.v7.dkf.Message.Delivery();
            
            if(message.getDelivery().getInTrainingApplication() != null){
                generated.v7.dkf.Message.Delivery.InTrainingApplication newInTrainingApp = new generated.v7.dkf.Message.Delivery.InTrainingApplication();
                newInTrainingApp.setEnabled(generated.v7.dkf.BooleanEnum.fromValue(message.getDelivery().getInTrainingApplication().getEnabled().toString().toLowerCase()));
                newDelivery.setInTrainingApplication(newInTrainingApp);
            }
            if(message.getDelivery().getInTutor() != null){
                generated.v7.dkf.InTutor newInTutor = new generated.v7.dkf.InTutor();
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
     * @return generated.v3.dkf.Entities - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v7.dkf.Entities convertEntities(generated.v6.dkf.Entities entities) throws IllegalArgumentException{
        
        generated.v7.dkf.Entities newEntities = new generated.v7.dkf.Entities();
        for(generated.v6.dkf.StartLocation location : entities.getStartLocation()){
            
            generated.v7.dkf.StartLocation newLocation = new generated.v7.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }
        
        return newEntities;
    }
    
    /**
     * Convert a path object into a new path object.
     * 
     * @param path - the object to convert
     * @return generated.v3.dkf.Path - the new object
     */
    private static generated.v7.dkf.Path convertPath(generated.v6.dkf.Path path){
        
        generated.v7.dkf.Path newPath = new generated.v7.dkf.Path();
        for(generated.v6.dkf.Segment segment : path.getSegment()){
            
            generated.v7.dkf.Segment newSegment = new generated.v7.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());
            
            generated.v7.dkf.Start start = new generated.v7.dkf.Start();
            start.setWaypoint(segment.getStart().getWaypoint());
            newSegment.setStart(start);
            
            generated.v7.dkf.End end = new generated.v7.dkf.End();
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
     * @return generated.v3.dkf.Checkpoint - the new object
     */
    private static generated.v7.dkf.Checkpoint convertCheckpoint(generated.v6.dkf.Checkpoint checkpoint){
        
        generated.v7.dkf.Checkpoint newCheckpoint = new generated.v7.dkf.Checkpoint();
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
    private static generated.v7.dkf.Evaluators convertEvaluators(generated.v6.dkf.Evaluators evaluators){
        
        generated.v7.dkf.Evaluators newEvaluators = new generated.v7.dkf.Evaluators();
        for(generated.v6.dkf.Evaluator evaluator : evaluators.getEvaluator()){
            
            generated.v7.dkf.Evaluator newEvaluator = new generated.v7.dkf.Evaluator();
            newEvaluator.setAssessment(evaluator.getAssessment());
            newEvaluator.setValue(evaluator.getValue());                                            
            newEvaluator.setOperator(evaluator.getOperator());
            
            newEvaluators.getEvaluator().add(newEvaluator);
        }
        
        return newEvaluators;
    }
    

    /**
     * Converts v6.1 excavator component inputs
     * 
     * @param oldCompList - the v6.1 component list
     * @return the new v6.0 component list
     */
    private static List<generated.v7.dkf.HasMovedExcavatorComponentInput.Component> convertComponents(
            List<generated.v6.dkf.HasMovedExcavatorComponentInput.Component> oldCompList) {
        
        List<generated.v7.dkf.HasMovedExcavatorComponentInput.Component> componentList = 
                new ArrayList<generated.v7.dkf.HasMovedExcavatorComponentInput.Component>();
        
        for(generated.v6.dkf.HasMovedExcavatorComponentInput.Component oldComp : oldCompList) {
             generated.v7.dkf.HasMovedExcavatorComponentInput.Component newComp = new generated.v7.dkf.HasMovedExcavatorComponentInput.Component();
             newComp.setComponentType(generated.v7.dkf.ExcavatorComponentEnum.fromValue(oldComp.getComponentType().value()));
             
             if(oldComp.getDirectionType() instanceof generated.v6.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional){
                 generated.v6.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional oldBiDirectional = (generated.v6.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional)oldComp.getDirectionType();
                 generated.v7.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional newBiDirectional = new generated.v7.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional();
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
    private static generated.v7.dkf.Assessments convertAssessments(generated.v6.dkf.Assessments assessments){
        
        generated.v7.dkf.Assessments newAssessments = new generated.v7.dkf.Assessments();
        
        List<generated.v6.dkf.Assessments.Survey> surveys = new ArrayList<generated.v6.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
            if (assessmentType instanceof generated.v6.dkf.Assessments.Survey) {
                surveys.add((generated.v6.dkf.Assessments.Survey) assessmentType);
            }
        }
        for(generated.v6.dkf.Assessments.Survey survey : surveys){
            
            generated.v7.dkf.Assessments.Survey newSurvey = new generated.v7.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());
            
            generated.v7.dkf.Questions newQuestions = new generated.v7.dkf.Questions();
            for(generated.v6.dkf.Question question : survey.getQuestions().getQuestion()){
                
                generated.v7.dkf.Question newQuestion = new generated.v7.dkf.Question();
                newQuestion.setKey(question.getKey());
                
                for(generated.v6.dkf.Reply reply : question.getReply()){
                    
                    generated.v7.dkf.Reply newReply = new generated.v7.dkf.Reply();
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
     * @return List<generated.v7.dkf.StartTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v7.dkf.StartTriggers.Trigger> convertStartTriggers(List<generated.v6.dkf.StartTriggers.Trigger> list) throws IllegalArgumentException{
        
        List<generated.v7.dkf.StartTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(generated.v6.dkf.StartTriggers.Trigger triggerObj : list){
            
            generated.v7.dkf.StartTriggers.Trigger trigger = new generated.v7.dkf.StartTriggers.Trigger();
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
     * @return List<generated.v7.dkf.EndTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v7.dkf.EndTriggers.Trigger> convertEndTriggers(List<generated.v6.dkf.EndTriggers.Trigger> list) throws IllegalArgumentException{
        
        List<generated.v7.dkf.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(generated.v6.dkf.EndTriggers.Trigger triggerObj : list){
            
            generated.v7.dkf.EndTriggers.Trigger trigger = new generated.v7.dkf.EndTriggers.Trigger();
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
     * @return List<generated.v7.dkf.Scenario.EndTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v7.dkf.Scenario.EndTriggers.Trigger> convertScenarioEndTriggers(List<generated.v6.dkf.Scenario.EndTriggers.Trigger> list) throws IllegalArgumentException{
        
        List<generated.v7.dkf.Scenario.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(generated.v6.dkf.Scenario.EndTriggers.Trigger triggerObj : list){
            
            generated.v7.dkf.Scenario.EndTriggers.Trigger trigger = new generated.v7.dkf.Scenario.EndTriggers.Trigger();
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

        if(triggerObj instanceof generated.v6.dkf.EntityLocation){
            
            generated.v6.dkf.EntityLocation entityLocation = (generated.v6.dkf.EntityLocation)triggerObj;
            generated.v7.dkf.EntityLocation newEntityLocation = new generated.v7.dkf.EntityLocation();
            
            generated.v7.dkf.StartLocation startLocation = new generated.v7.dkf.StartLocation();
            startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
            newEntityLocation.setStartLocation(startLocation);
            
            generated.v7.dkf.TriggerLocation triggerLocation = new generated.v7.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);
            
            return newEntityLocation;
            
            
        }else if(triggerObj instanceof generated.v6.dkf.LearnerLocation){
            
            generated.v6.dkf.LearnerLocation learnerLocation = (generated.v6.dkf.LearnerLocation)triggerObj;
            generated.v7.dkf.LearnerLocation newLearnerLocation = new generated.v7.dkf.LearnerLocation();
            
            newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
            
            return newLearnerLocation;
            
            
        }else if(triggerObj instanceof generated.v6.dkf.ConceptEnded){
            
            generated.v6.dkf.ConceptEnded conceptEnded = (generated.v6.dkf.ConceptEnded)triggerObj;
            generated.v7.dkf.ConceptEnded newConceptEnded = new generated.v7.dkf.ConceptEnded();
            
            newConceptEnded.setNodeId(conceptEnded.getNodeId());
            
            return newConceptEnded;
            
            
        }else if(triggerObj instanceof generated.v6.dkf.ChildConceptEnded){
            
            generated.v6.dkf.ChildConceptEnded childConceptEnded = (generated.v6.dkf.ChildConceptEnded)triggerObj;
            generated.v7.dkf.ChildConceptEnded newChildConceptEnded = new generated.v7.dkf.ChildConceptEnded();
            
            newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());
            
            return newChildConceptEnded;
            
            
        } else if(triggerObj instanceof generated.v6.dkf.TaskEnded){
            
            generated.v6.dkf.TaskEnded taskEnded = (generated.v6.dkf.TaskEnded)triggerObj;
            generated.v7.dkf.TaskEnded newTaskEnded = new generated.v7.dkf.TaskEnded();
            
            newTaskEnded.setNodeId(taskEnded.getNodeId());
            
            return newTaskEnded;
            
            
        } else if(triggerObj instanceof generated.v6.dkf.ConceptAssessment){
            
            generated.v6.dkf.ConceptAssessment conceptAssessment = (generated.v6.dkf.ConceptAssessment)triggerObj;
            generated.v7.dkf.ConceptAssessment newConceptAssessment = new generated.v7.dkf.ConceptAssessment();
            
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
     * @param coordinate - v6.0 coordinate object to convert
     * @return generated.v7.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException
     */
    private static generated.v7.dkf.Coordinate convertCoordinate(generated.v6.dkf.Coordinate coordinate) throws IllegalArgumentException{
        
        generated.v7.dkf.Coordinate newCoord = new generated.v7.dkf.Coordinate();
        
        Object coordType = coordinate.getType();
        if(coordType instanceof generated.v6.dkf.GCC){
            
            generated.v6.dkf.GCC gcc = (generated.v6.dkf.GCC)coordType;
            generated.v7.dkf.GCC newGCC = new generated.v7.dkf.GCC();
            
            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());
            
            newCoord.setType(newGCC);
            
        }else if(coordType instanceof generated.v6.dkf.GDC){
           // generated.v6.
            generated.v6.dkf.GDC gdc = (generated.v6.dkf.GDC)coordType;
            generated.v7.dkf.GDC newGDC = new generated.v7.dkf.GDC();
            
            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());
            
            newCoord.setType(newGDC);
            
        }else if(coordType instanceof generated.v6.dkf.VBSAGL){
            
            generated.v6.dkf.VBSAGL agl = (generated.v6.dkf.VBSAGL)coordType;
            generated.v7.dkf.VBSAGL newAGL = new generated.v7.dkf.VBSAGL();
            
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
     * @return generated.v7.dkf.StrategyHandler - the new object
     */
    private static generated.v7.dkf.StrategyHandler convertStrategyHandler(generated.v6.dkf.StrategyHandler handler){
        
        generated.v7.dkf.StrategyHandler newHandler = new generated.v7.dkf.StrategyHandler();
        
        if(handler.getParams() != null) {
            
            generated.v7.dkf.StrategyHandler.Params newParams = new generated.v7.dkf.StrategyHandler.Params();
            generated.v7.dkf.Nvpair nvpair = new generated.v7.dkf.Nvpair();
            
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
     * @return generated.v7.dkf.Concepts - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v7.dkf.Concepts convertConcepts(generated.v6.dkf.Concepts concepts) throws IllegalArgumentException{

        generated.v7.dkf.Concepts newConcepts = new generated.v7.dkf.Concepts();
        for(generated.v6.dkf.Concept concept : concepts.getConcept()){
            
            generated.v7.dkf.Concept newConcept = new generated.v7.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());
            
            if (concept.getAssessments() != null) {
                newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }
            
            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if(conditionsOrConcepts instanceof generated.v6.dkf.Concepts){
                //nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v6.dkf.Concepts)conditionsOrConcepts));
                
            }else if(conditionsOrConcepts instanceof generated.v6.dkf.Conditions){
                
                generated.v7.dkf.Conditions newConditions = new generated.v7.dkf.Conditions();
                
                generated.v6.dkf.Conditions conditions = (generated.v6.dkf.Conditions)conditionsOrConcepts;
                
                for(generated.v6.dkf.Condition condition : conditions.getCondition()){

                    generated.v7.dkf.Condition newCondition = new generated.v7.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());                        
                    
                    if(condition.getDefault() != null){
                        generated.v7.dkf.Default newDefault = new generated.v7.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }                            
                    
                    //Input
                    generated.v7.dkf.Input newInput = new generated.v7.dkf.Input();
                    if(condition.getInput() != null){
                        
                        Object inputType = condition.getInput().getType();
                        
                        if(inputType instanceof generated.v6.dkf.ApplicationCompletedCondition){
                            
                            generated.v6.dkf.ApplicationCompletedCondition conditionInput = (generated.v6.dkf.ApplicationCompletedCondition)inputType;
                            
                            generated.v7.dkf.ApplicationCompletedCondition newConditionInput = new generated.v7.dkf.ApplicationCompletedCondition();
                                
                            if (conditionInput.getIdealCompletionDuration() != null) {
                                newConditionInput.setIdealCompletionDuration(conditionInput.getIdealCompletionDuration());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if (inputType instanceof generated.v6.dkf.AutoTutorConditionInput){

                            generated.v6.dkf.AutoTutorConditionInput conditionInput = (generated.v6.dkf.AutoTutorConditionInput)inputType;
                            
                            generated.v7.dkf.AutoTutorConditionInput newConditionInput = new generated.v7.dkf.AutoTutorConditionInput();

                            if (conditionInput.getScript() != null) {
                                
                                if (conditionInput.getScript() instanceof generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO) {
                                    
                                    generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO atRemoteSKO = (generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO) conditionInput.getScript();
                                    
                                    generated.v7.dkf.ATRemoteSKO newATRemoteSKO = new generated.v7.dkf.ATRemoteSKO();
                                    generated.v7.dkf.AutoTutorSKO newAutoTutorSKO= new generated.v7.dkf.AutoTutorSKO();
                                    
                                    generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO.URL url = atRemoteSKO.getURL();
                                    
                                    generated.v7.dkf.ATRemoteSKO.URL newURL = new generated.v7.dkf.ATRemoteSKO.URL();
                                    newURL.setAddress(url.getAddress());
                                    newATRemoteSKO.setURL(newURL);
                                    newAutoTutorSKO.setScript(newATRemoteSKO);
                                    
                                    newConditionInput.setAutoTutorSKO(newAutoTutorSKO);
                                    
                                } else if (conditionInput.getScript() instanceof generated.v6.dkf.AutoTutorConditionInput.LocalSKO) {

                                    generated.v6.dkf.AutoTutorConditionInput.LocalSKO localSKO = (generated.v6.dkf.AutoTutorConditionInput.LocalSKO) conditionInput.getScript();
                                    generated.v7.dkf.LocalSKO newLocalSKO = new generated.v7.dkf.LocalSKO();
                                    generated.v7.dkf.AutoTutorSKO newAutoTutorSKO= new generated.v7.dkf.AutoTutorSKO();
                                    
                                    newLocalSKO.setFile(localSKO.getFile());
                                    newAutoTutorSKO.setScript(newLocalSKO);
                                    newConditionInput.setAutoTutorSKO(newAutoTutorSKO);
                                    
                                } else {
                                    throw new IllegalArgumentException("Found unhandled AutoTutorConditionInput script type of "+conditionInput.getScript());
                                }

                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v6.dkf.AvoidLocationCondition){
                            
                            generated.v6.dkf.AvoidLocationCondition conditionInput = (generated.v6.dkf.AvoidLocationCondition)inputType;
                            
                            generated.v7.dkf.AvoidLocationCondition newConditionInput = new generated.v7.dkf.AvoidLocationCondition();
                            
                            if(conditionInput.getWaypointRef() != null){                                    
                                newConditionInput.setWaypointRef(convertWaypointRef(conditionInput.getWaypointRef()));
                            }
                                                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v6.dkf.CheckpointPaceCondition){

                            generated.v6.dkf.CheckpointPaceCondition conditionInput = (generated.v6.dkf.CheckpointPaceCondition)inputType;
                            
                            generated.v7.dkf.CheckpointPaceCondition newConditionInput = new generated.v7.dkf.CheckpointPaceCondition();
                            for(generated.v6.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);
                                                            
                        }else if(inputType instanceof generated.v6.dkf.CheckpointProgressCondition){

                            generated.v6.dkf.CheckpointProgressCondition conditionInput = (generated.v6.dkf.CheckpointProgressCondition)inputType;
                            
                            generated.v7.dkf.CheckpointProgressCondition newConditionInput = new generated.v7.dkf.CheckpointProgressCondition();
                            for(generated.v6.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);                                
                            
                        }else if(inputType instanceof generated.v6.dkf.CorridorBoundaryCondition){
                            
                            generated.v6.dkf.CorridorBoundaryCondition conditionInput = (generated.v6.dkf.CorridorBoundaryCondition)inputType;
                            
                            generated.v7.dkf.CorridorBoundaryCondition newConditionInput = new generated.v7.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.CorridorPostureCondition){

                            generated.v6.dkf.CorridorPostureCondition conditionInput = (generated.v6.dkf.CorridorPostureCondition)inputType;
                            
                            generated.v7.dkf.CorridorPostureCondition newConditionInput = new generated.v7.dkf.CorridorPostureCondition();
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                

                            generated.v7.dkf.Postures postures = new generated.v7.dkf.Postures();
                            /*
                            for(generated.v6.dkf.PostureEnumType posture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(generated.v7.dkf.PostureEnumType.fromValue(posture.value()));
                            }
                            */
                            for(String strPosture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.EliminateHostilesCondition){

                            generated.v6.dkf.EliminateHostilesCondition conditionInput = (generated.v6.dkf.EliminateHostilesCondition)inputType;
                            
                            generated.v7.dkf.EliminateHostilesCondition newConditionInput = new generated.v7.dkf.EliminateHostilesCondition();
                             
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.EnterAreaCondition){

                            generated.v6.dkf.EnterAreaCondition conditionInput = (generated.v6.dkf.EnterAreaCondition)inputType;
                            
                            generated.v7.dkf.EnterAreaCondition newConditionInput = new generated.v7.dkf.EnterAreaCondition();
                            
                            for(generated.v6.dkf.Entrance entrance : conditionInput.getEntrance()){
                                
                                generated.v7.dkf.Entrance newEntrance = new generated.v7.dkf.Entrance();
                                
                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());
                                
                                generated.v7.dkf.Inside newInside = new generated.v7.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setWaypoint(entrance.getInside().getWaypoint());
                                newEntrance.setInside(newInside);
                                
                                generated.v7.dkf.Outside newOutside = new generated.v7.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setWaypoint(entrance.getOutside().getWaypoint());
                                newEntrance.setOutside(newOutside);
                                
                                newConditionInput.getEntrance().add(newEntrance);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.ExplosiveHazardSpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v6.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v6.dkf.ExplosiveHazardSpotReportCondition)inputType;
                            
                            generated.v7.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v7.dkf.ExplosiveHazardSpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.GenericConditionInput) {
                            
                            generated.v6.dkf.GenericConditionInput conditionInput = (generated.v6.dkf.GenericConditionInput)inputType;
                            
                            generated.v7.dkf.GenericConditionInput newConditionInput = new generated.v7.dkf.GenericConditionInput();
                            
                            if(conditionInput.getNvpair() != null){                                    
                                for (generated.v6.dkf.Nvpair nvPair : conditionInput.getNvpair()) {
                                    newConditionInput.getNvpair().add(convertNvpair(nvPair));
                                }
                            }
                                                            
                            newInput.setType(newConditionInput);
                        
                        }else if(inputType instanceof generated.v6.dkf.HasMovedExcavatorComponentInput){

                            generated.v6.dkf.HasMovedExcavatorComponentInput conditionInput = (generated.v6.dkf.HasMovedExcavatorComponentInput)inputType;
                            generated.v7.dkf.HasMovedExcavatorComponentInput newConditionInput = new generated.v7.dkf.HasMovedExcavatorComponentInput();
                            
                            newConditionInput.getComponent().addAll(convertComponents(conditionInput.getComponent()));
                            newConditionInput.setMaxAssessments(conditionInput.getMaxAssessments());
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.IdentifyPOIsCondition){

                            generated.v6.dkf.IdentifyPOIsCondition conditionInput = (generated.v6.dkf.IdentifyPOIsCondition)inputType;
                            
                            generated.v7.dkf.IdentifyPOIsCondition newConditionInput = new generated.v7.dkf.IdentifyPOIsCondition();
                            
                            if(conditionInput.getPois() != null){
                                
                                generated.v7.dkf.Pois pois = new generated.v7.dkf.Pois();
                                for(generated.v6.dkf.WaypointRef waypointRef : conditionInput.getPois().getWaypointRef()){
                                    pois.getWaypointRef().add(convertWaypointRef(waypointRef));
                                }
                                
                                newConditionInput.setPois(pois);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.LifeformTargetAccuracyCondition){

                            generated.v6.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v6.dkf.LifeformTargetAccuracyCondition)inputType;
                            
                            generated.v7.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v7.dkf.LifeformTargetAccuracyCondition();
                            
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v6.dkf.MarksmanshipPrecisionCondition) {
                        
                            generated.v6.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v6.dkf.MarksmanshipPrecisionCondition)inputType;
                            
                            generated.v7.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v7.dkf.MarksmanshipPrecisionCondition();
                            
                            if(conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if (inputType instanceof generated.v6.dkf.MarksmanshipSessionCompleteCondition) {
                        
                            generated.v6.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v6.dkf.MarksmanshipSessionCompleteCondition)inputType;
                            
                            generated.v7.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v7.dkf.MarksmanshipSessionCompleteCondition();
                            
                            if(conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }
                            
                            newInput.setType(newConditionInput);
                        
                        }else if(inputType instanceof generated.v6.dkf.NineLineReportCondition){

                            @SuppressWarnings("unused")
                            generated.v6.dkf.NineLineReportCondition conditionInput = (generated.v6.dkf.NineLineReportCondition)inputType;
                            
                            generated.v7.dkf.NineLineReportCondition newConditionInput = new generated.v7.dkf.NineLineReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.NumberOfShotsFiredCondition){
                         
                            generated.v6.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v6.dkf.NumberOfShotsFiredCondition)inputType;
                            
                            generated.v7.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v7.dkf.NumberOfShotsFiredCondition();
                                                            
                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }
                    
                            newInput.setType(newConditionInput);
                    
                        }else if(inputType instanceof generated.v6.dkf.PowerPointDwellCondition){

                            generated.v6.dkf.PowerPointDwellCondition conditionInput = (generated.v6.dkf.PowerPointDwellCondition)inputType;
                            
                            generated.v7.dkf.PowerPointDwellCondition newConditionInput = new generated.v7.dkf.PowerPointDwellCondition();
                            
                            generated.v7.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v7.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);
                            
                            generated.v7.dkf.PowerPointDwellCondition.Slides slides = new generated.v7.dkf.PowerPointDwellCondition.Slides();
                            for(generated.v6.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput.getSlides().getSlide()){
                                
                                generated.v7.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v7.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());
                                
                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.RulesOfEngagementCondition){

                            generated.v6.dkf.RulesOfEngagementCondition conditionInput = (generated.v6.dkf.RulesOfEngagementCondition)inputType;
                            
                            generated.v7.dkf.RulesOfEngagementCondition newConditionInput = new generated.v7.dkf.RulesOfEngagementCondition();
                            generated.v7.dkf.Wcs newWCS = new generated.v7.dkf.Wcs();
                            newWCS.setValue(generated.v7.dkf.WeaponControlStatusEnum.fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v6.dkf.SIMILEConditionInput){
                        
                            generated.v6.dkf.SIMILEConditionInput conditionInput = (generated.v6.dkf.SIMILEConditionInput)inputType;
                            
                            generated.v7.dkf.SIMILEConditionInput newConditionInput = new generated.v7.dkf.SIMILEConditionInput();
                        
                            if (conditionInput.getConditionKey() != null) {
                                newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }
                            
                            if (conditionInput.getConfigurationFile() != null) {
                                newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v6.dkf.SpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v6.dkf.SpotReportCondition conditionInput = (generated.v6.dkf.SpotReportCondition)inputType;
                            
                            generated.v7.dkf.SpotReportCondition newConditionInput = new generated.v7.dkf.SpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.TimerConditionInput){

                            generated.v6.dkf.TimerConditionInput conditionInput = (generated.v6.dkf.TimerConditionInput)inputType;
                            
                            generated.v7.dkf.TimerConditionInput newConditionInput = new generated.v7.dkf.TimerConditionInput();
                            
                            newConditionInput.setRepeatable(generated.v7.dkf.BooleanEnum.fromValue(conditionInput.getRepeatable().toString().toLowerCase()));
                            newConditionInput.setInterval(conditionInput.getInterval());
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v6.dkf.UseRadioCondition){
                            
                            @SuppressWarnings("unused")
                            generated.v6.dkf.UseRadioCondition conditionInput = (generated.v6.dkf.UseRadioCondition)inputType;
                            
                            generated.v7.dkf.UseRadioCondition newConditionInput = new generated.v7.dkf.UseRadioCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled condition input type of "+inputType);
                        }

                    }
                    newCondition.setInput(newInput);
                    
                    //Scoring
                    generated.v7.dkf.Scoring newScoring = new generated.v7.dkf.Scoring();
                    if(condition.getScoring() != null){
                        // Only add the scoring element if it has children. 
                        // As of version 5, there cannot be a scoring element with no children
                        if (!condition.getScoring().getType().isEmpty()) {
                            
                            for(Object scoringType : condition.getScoring().getType()){
                                
                                if(scoringType instanceof generated.v6.dkf.Count){
                                    
                                    generated.v6.dkf.Count count = (generated.v6.dkf.Count)scoringType;
                                    
                                    generated.v7.dkf.Count newCount = new generated.v7.dkf.Count();                                    
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v7.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
                                    
                                    if(count.getEvaluators() != null){                                        
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newCount);
                                    
                                }else if(scoringType instanceof generated.v6.dkf.CompletionTime){
                                    
                                    generated.v6.dkf.CompletionTime complTime = (generated.v6.dkf.CompletionTime)scoringType;
                                    
                                    generated.v7.dkf.CompletionTime newComplTime = new generated.v7.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(generated.v7.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if(complTime.getEvaluators() != null){                                        
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newComplTime);
                                    
                                }else if(scoringType instanceof generated.v6.dkf.ViolationTime){
                                    
                                    generated.v6.dkf.ViolationTime violationTime = (generated.v6.dkf.ViolationTime)scoringType;
                                    
                                    generated.v7.dkf.ViolationTime newViolationTime = new generated.v7.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(generated.v7.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
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
    private static generated.v7.dkf.Nvpair convertNvpair(generated.v6.dkf.Nvpair nvPair) {
        
        generated.v7.dkf.Nvpair newNvpair = new generated.v7.dkf.Nvpair();
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
    private static generated.v7.dkf.WaypointRef convertWaypointRef(generated.v6.dkf.WaypointRef waypointRef){
        
        generated.v7.dkf.WaypointRef newWaypoint = new generated.v7.dkf.WaypointRef();
        newWaypoint.setValue(waypointRef.getValue());
        newWaypoint.setDistance(waypointRef.getDistance());
        
        return newWaypoint;
    }
    
    /**
     * Begin the conversion of a v6 file to the current version by parsing the old file for the pedagogical config
     * schema object and convert it to a newer version.
     */
    @Override
    public UnmarshalledFile convertEMAPConfiguration(FileProxy pedConfigFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(pedConfigFile, PREV_PEDAGOGICAL_SCHEMA_FILE, PREV_PEDAGOGICAL_ROOT, failOnFirstSchemaError);
        generated.v6.ped.EMAP v6PedConfig = (generated.v6.ped.EMAP)uFile.getUnmarshalled();
        
        return convertPedagogicalConfiguration(v6PedConfig, showCompletionDialog);
    }

    /**
     * Convert the previous pedagogical schema object to the current version of the course schema.
     * 
     * @param v6PedConfig - version of the config file to be converted 
     * @param showCompletionDialog - show the completion dialog
     * @return - the freshly converted config file
     */
    public UnmarshalledFile convertPedagogicalConfiguration(generated.v6.ped.EMAP v6PedConfig, boolean showCompletionDialog) {
        generated.ped.EMAP newPedConfig = new generated.ped.EMAP();
        
        newPedConfig.setExample(convertExample(v6PedConfig.getExample()));
        newPedConfig.setPractice(convertPractice(v6PedConfig.getPractice()));
        newPedConfig.setRecall(convertRecall(v6PedConfig.getRecall()));
        newPedConfig.setRule(convertRule(v6PedConfig.getRule()));
        newPedConfig.setVersion(VERSION_NUMBER);
        
        return super.convertEMAPConfiguration(newPedConfig, showCompletionDialog);
    }
    
    /**
     * Helper function to convert the Rules of a pedagogy config to the next version
     * @param v6Rule - current version of the rules 
     * @return current version Rules
     */
    private generated.ped.Rule convertRule(generated.v6.ped.Rule v6Rule) {
        generated.ped.Rule newRule = new generated.ped.Rule();
        generated.ped.Attributes newAttributes = new generated.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v6.ped.Attribute attribute : v6Rule.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.ped.Attribute());
            newAttributes.getAttribute().get(index).setType(attribute.getType());
            newAttributes.getAttribute().get(index).setValue(attribute.getValue());
            newAttributes.getAttribute().get(index).setMetadataAttributes(convertMetaDataAttributes(attribute.getMetadataAttributes()));
            index++;
        }
        newRule.setAttributes(newAttributes);
        
        
        return newRule;
    }

    /**
     * Helper function to convert the Recall of a pedagogy config to the next version
     * @param v6Rule - current version of the recall 
     * @return current version Recall
     */
    private generated.ped.Recall convertRecall(generated.v6.ped.Recall v6Recall) {
        generated.ped.Recall newRecall = new generated.ped.Recall();
        generated.ped.Attributes newAttributes = new generated.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v6.ped.Attribute attribute : v6Recall.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.ped.Attribute());
            newAttributes.getAttribute().get(index).setType(attribute.getType());
            newAttributes.getAttribute().get(index).setValue(attribute.getValue());
            newAttributes.getAttribute().get(index).setMetadataAttributes(convertMetaDataAttributes(attribute.getMetadataAttributes()));
            index++;
        }
        newRecall.setAttributes(newAttributes);
        
        
        return newRecall;
    }

    /**
     * Helper function to convert the Practice of a pedagogy config to the next version
     * @param v6Practice - current version of the practice 
     * @return current version Practice
     */
    private generated.ped.Practice convertPractice(generated.v6.ped.Practice v6Practice) {
        generated.ped.Practice newPractice = new generated.ped.Practice();
        generated.ped.Attributes newAttributes = new generated.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v6.ped.Attribute attribute : v6Practice.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.ped.Attribute());
            newAttributes.getAttribute().get(index).setType(attribute.getType());
            newAttributes.getAttribute().get(index).setValue(attribute.getValue());
            newAttributes.getAttribute().get(index).setMetadataAttributes(convertMetaDataAttributes(attribute.getMetadataAttributes()));
            index++;
        }
        newPractice.setAttributes(newAttributes);
        
        
        return newPractice;
    }

    /**
     * Helper function to convert the Example of a pedagogy config to the next version
     * @param v6Example - current version of the example
     * @return current version example
     */
    private generated.ped.Example convertExample(generated.v6.ped.Example v6Example) {
        generated.ped.Example newExample = new generated.ped.Example();
        generated.ped.Attributes newAttributes = new generated.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v6.ped.Attribute attribute : v6Example.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.ped.Attribute());
            newAttributes.getAttribute().get(index).setType(attribute.getType());
            newAttributes.getAttribute().get(index).setValue(attribute.getValue());
            newAttributes.getAttribute().get(index).setMetadataAttributes(convertMetaDataAttributes(attribute.getMetadataAttributes()));
            index++;
        }
        newExample.setAttributes(newAttributes);
        
        
        return newExample;
    }

    /**
     * Each element to a pedagogy config contains metadata attributes, and can all use this method to convert them
     * @param v6metadataAttributes - current version's metadata
     * @return current version metadata
     */
    private generated.ped.MetadataAttributes convertMetaDataAttributes(generated.v6.ped.MetadataAttributes v6metadataAttributes) {
        generated.ped.MetadataAttributes newMetaAttributes = new generated.ped.MetadataAttributes();
        int index = 0;
        
        for(generated.v6.ped.MetadataAttribute attribute : v6metadataAttributes.getMetadataAttribute()){
            newMetaAttributes.getMetadataAttribute().add(new generated.ped.MetadataAttribute());
            newMetaAttributes.getMetadataAttribute().get(index).setValue(attribute.getValue());
            //Quadrant Specific attributes no longer exists as of version 2014_3X and are added to
            //issue list in previous wizard, no longer need to re add or handle these attributes
//          if(attribute.getIsQuadrantSpecific() != null){
//              conversionIssueList.addIssue("Quadrant Specific Element");
//              conversionIssueList.put("Quadrant Specific Element", new HashMap<String, Integer>(1));
//          }
            index++;
        }
        
        return newMetaAttributes;
    }
    
    @Override
    public UnmarshalledFile convertTrainingApplicationRef(FileProxy trainingAppFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(trainingAppFile, getPreviousCourseSchemaFile(), getPreviousTrainingAppSchemaRoot(), failOnFirstSchemaError);
        generated.v6.course.TrainingApplicationWrapper v6trainingApp = (generated.v6.course.TrainingApplicationWrapper)uFile.getUnmarshalled();
        
        // Convert the version 5 course to the newest version and return it
        return convertTrainingApplicationRef(v6trainingApp, showCompletionDialog);  
    }
    
    /**
     * Convert the previous TrainingAppRef schema object to a newer version of the TrainingAppRef schema.
     * 
     * @param v6trainingAppRef - the TrainingAppRef schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return the new course
     */
    public UnmarshalledFile convertTrainingApplicationRef(generated.v6.course.TrainingApplicationWrapper v6trainingAppRef, boolean showCompletionDialog){
        generated.course.TrainingApplicationWrapper newTrainingAppRef = new generated.course.TrainingApplicationWrapper();
        newTrainingAppRef.setDescription(v6trainingAppRef.getDescription());
        newTrainingAppRef.setVersion(VERSION_NUMBER);
        newTrainingAppRef.setTrainingApplication(convertTrainingApplication(v6trainingAppRef.getTrainingApplication()));

        return super.convertTrainingApplicationRef(newTrainingAppRef, showCompletionDialog);
    }
    
    private static generated.course.TrainingApplication convertTrainingApplication(generated.v6.course.TrainingApplication trainApp){
        generated.course.TrainingApplication newTrainApp = new generated.course.TrainingApplication();
      
        newTrainApp.setTransitionName(trainApp.getTransitionName());
        
        generated.course.DkfRef newDkfRef = new generated.course.DkfRef();
        newDkfRef.setFile(trainApp.getDkfRef().getFile());
        newTrainApp.setDkfRef(newDkfRef);
        
        newTrainApp.setFinishedWhen(trainApp.getFinishedWhen());
        
        if(trainApp.getGuidance() != null){
            
            generated.course.Guidance newGuidance;
            
            //Some transitions, including training application guidance, are now required to have names.
            //If the prev version's name is null, then create a name from the parent training application and append
            //" - Guidance". Otherwise, use the previous version's name. For example, if a v2015_1 course has a training
            //application transition called "PPT slideshow" and the transition contains a guidance with no name, 
            //the new version of the training app transition's guidance will be named "PPT slideshow - Guidance"
            if(trainApp.getGuidance().getTransitionName() == null){
                newGuidance = convertGuidance(trainApp.getGuidance(), trainApp.getTransitionName() + " - Guidance");
            } else {
                newGuidance = convertGuidance(trainApp.getGuidance(), null);
            }
            newTrainApp.setGuidance(newGuidance);
        }
        
        if (trainApp.getOptions() != null) {
            
            newTrainApp.setOptions(convertOptions(trainApp.getOptions()));
            
        }
        
        generated.course.Interops newInterops = new generated.course.Interops();               
        newTrainApp.setInterops(newInterops);
        
        for(generated.v6.course.Interop interop : trainApp.getInterops().getInterop()){
            
            generated.course.Interop newInterop = new generated.course.Interop();
            
            // Check to make sure the old vbs interop isn't being referenced since it has been replaced
            if(interop.getInteropImpl().equals(OLD_VBS_INTEROP)) {
                newInterop.setInteropImpl(NEW_VBS_INTEROP);
            } else {
                newInterop.setInteropImpl(interop.getInteropImpl());
            }
                                
            newInterop.setInteropInputs(new generated.course.InteropInputs());
            
            Object interopObj = interop.getInteropInputs().getInteropInput();
            if(interopObj instanceof generated.v6.course.VBSInteropInputs){
                
                generated.v6.course.VBSInteropInputs vbs2 = (generated.v6.course.VBSInteropInputs)interopObj;
                generated.course.VBSInteropInputs newVbs = new generated.course.VBSInteropInputs();
                
                generated.course.VBSInteropInputs.LoadArgs newLoadArgs = new generated.course.VBSInteropInputs.LoadArgs();
                newLoadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                newVbs.setLoadArgs(newLoadArgs);
                
                newInterop.getInteropInputs().setInteropInput(newVbs);
                
            }else if(interopObj instanceof generated.v6.course.DISInteropInputs){
                
                generated.course.DISInteropInputs newDIS = new generated.course.DISInteropInputs();
                newDIS.setLoadArgs(new generated.course.DISInteropInputs.LoadArgs());
                
                newInterop.getInteropInputs().setInteropInput(newDIS);
                
            }else if(interopObj instanceof generated.v6.course.PowerPointInteropInputs){
                
                generated.v6.course.PowerPointInteropInputs ppt = (generated.v6.course.PowerPointInteropInputs)interopObj;
                generated.course.PowerPointInteropInputs newPPT = new generated.course.PowerPointInteropInputs();
                
                newPPT.setLoadArgs(new generated.course.PowerPointInteropInputs.LoadArgs());
                
                newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                
                newInterop.getInteropInputs().setInteropInput(newPPT);
                
            }else if(interopObj instanceof generated.v6.course.TC3InteropInputs){
                
                generated.v6.course.TC3InteropInputs tc3 = (generated.v6.course.TC3InteropInputs)interopObj;
                generated.course.TC3InteropInputs newTC3 = new generated.course.TC3InteropInputs();
                
                newTC3.setLoadArgs(new generated.course.TC3InteropInputs.LoadArgs());
                
                newTC3.getLoadArgs().setScenarioName(tc3.getLoadArgs().getScenarioName());
                
                newInterop.getInteropInputs().setInteropInput(newTC3);
                
            }else if(interopObj instanceof generated.v6.course.SCATTInteropInputs){
            
                generated.course.SCATTInteropInputs newSCAT = new generated.course.SCATTInteropInputs();            
                newSCAT.setLoadArgs(new generated.course.SCATTInteropInputs.LoadArgs());
                
                newInterop.getInteropInputs().setInteropInput(newSCAT);
                
            }else if(interopObj instanceof generated.v6.course.CustomInteropInputs){
                
                generated.v6.course.CustomInteropInputs custom = (generated.v6.course.CustomInteropInputs)interopObj;
                generated.course.CustomInteropInputs newCustom = new generated.course.CustomInteropInputs();
                
                newCustom.setLoadArgs(new generated.course.CustomInteropInputs.LoadArgs());
                
                for(generated.v6.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                    generated.course.Nvpair newPair = new generated.course.Nvpair();
                    newPair.setName(pair.getName());
                    newPair.setValue(pair.getValue());
                    newCustom.getLoadArgs().getNvpair().add(newPair);
                }
                
                newInterop.getInteropInputs().setInteropInput(newCustom);
                
                
            }else if(interopObj instanceof generated.v6.course.DETestbedInteropInputs){
                
                generated.v6.course.DETestbedInteropInputs deTestbed = (generated.v6.course.DETestbedInteropInputs)interopObj;
                generated.course.DETestbedInteropInputs newDeTestbed = new generated.course.DETestbedInteropInputs();
                
                generated.course.DETestbedInteropInputs.LoadArgs args = new generated.course.DETestbedInteropInputs.LoadArgs();
                args.setScenarioName(deTestbed.getLoadArgs().getScenarioName());
                newDeTestbed.setLoadArgs(args);
                
                newInterop.getInteropInputs().setInteropInput(newDeTestbed);
                
            } else if(interopObj instanceof generated.v6.course.SimpleExampleTAInteropInputs){
                generated.v6.course.SimpleExampleTAInteropInputs TAInputs = (generated.v6.course.SimpleExampleTAInteropInputs) interopObj;
                generated.course.SimpleExampleTAInteropInputs newTAInputs = new generated.course.SimpleExampleTAInteropInputs();
                generated.course.SimpleExampleTAInteropInputs.LoadArgs args = new generated.course.SimpleExampleTAInteropInputs.LoadArgs();
                args.setScenarioName(TAInputs.getLoadArgs().getScenarioName());
                
                newTAInputs.setLoadArgs(args);
                newInterop.getInteropInputs().setInteropInput(newTAInputs);
            } else {
                throw new IllegalArgumentException("Found unhandled interop input type of "+interopObj);
            }
            
            newTrainApp.getInterops().getInterop().add(newInterop);
        }
        
        return newTrainApp;
        
    
    }
    
    @Override
    public UnmarshalledFile convertMetadata(FileProxy metadataFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(metadataFile, getPreviousMetadataSchemaFile(), getPreviousMetadataSchemaRoot(), failOnFirstSchemaError);
        generated.v6.metadata.Metadata v6metadata = (generated.v6.metadata.Metadata)uFile.getUnmarshalled();
        
        // Convert the version 6 metadata to the newest version and return it
        return convertMetadata(v6metadata, showCompletionDialog);  
    }
    
    /**
     * Convert the previous metadata schema object to a newer version of the metadata schema.
     * 
     * @param oldMetadata - the metadata schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     */
    public UnmarshalledFile convertMetadata(generated.v6.metadata.Metadata oldMetadata, boolean showCompletionDialog){
        generated.v7.metadata.Metadata newMetadata = new generated.v7.metadata.Metadata();
        
        newMetadata.setVersion(VERSION_NUMBER);
        newMetadata.setMerrillQuadrant(oldMetadata.getMerrillQuadrant());
        newMetadata.setSimpleRef(oldMetadata.getSimpleRef());
        newMetadata.setTrainingAppRef(oldMetadata.getTrainingAppRef());
        newMetadata.setConcepts(convertConcepts(oldMetadata.getConcepts()));
        
        ConversionWizardUtil_v2016_1_v2018_1 util = new ConversionWizardUtil_v2016_1_v2018_1();
        util.setConversionIssueList(conversionIssueList);

        return super.convertMetadata(newMetadata, showCompletionDialog);
    }
    
    private static generated.v7.metadata.Metadata.Concepts convertConcepts(generated.v6.metadata.Metadata.Concepts oldConcepts) {
        generated.v7.metadata.Metadata.Concepts newConcepts = new generated.v7.metadata.Metadata.Concepts();
        
        for(generated.v6.metadata.Concept concept: oldConcepts.getConcept()){
            generated.v7.metadata.Concept newConcept = new generated.v7.metadata.Concept();
            newConcept.setAttributes(convertMetaDataAttributes(concept.getAttributes()));
            newConcept.setName(concept.getName());
            newConcepts.getConcept().add(newConcept);
        }
        
        return newConcepts;
    }
    
    private static generated.v7.metadata.Attributes convertMetaDataAttributes(generated.v6.metadata.Attributes oldAttributes) {
        generated.v7.metadata.Attributes newMetaAttributes = new generated.v7.metadata.Attributes();
        int index = 0;
        
        for(generated.v6.metadata.Attribute attribute : oldAttributes.getAttribute()){
            newMetaAttributes.getAttribute().add(new generated.v7.metadata.Attribute());
            newMetaAttributes.getAttribute().get(index).setValue(attribute.getValue());
            //Quadrant Specific attributes no longer exists as of version 2014_3X and are added to
            //issue list in previous wizard, no longer need to re add or handle these attributes
//          if(attribute.getIsQuadrantSpecific() != null){
//              conversionIssueList.addIssue("Quadrant Specific Element");
//              conversionIssueList.put("Quadrant Specific Element", new HashMap<String, Integer>(1));
//          }
            index++;
        }
        
        return newMetaAttributes;
    }

    private static generated.course.TrainingApplication.Options convertOptions(generated.v6.course.TrainingApplication.Options oldOptions){
            generated.course.TrainingApplication.Options newOptions = new generated.course.TrainingApplication.Options();
            
            if (oldOptions.getDisableInstInterImpl() != null) {
                
                newOptions.setDisableInstInterImpl(generated.course.BooleanEnum.fromValue(oldOptions.getDisableInstInterImpl().toString().toLowerCase()));
            }
            
            if (oldOptions.getShowAvatarInitially() != null) {
                
                generated.course.ShowAvatarInitially newShowAvatarInitially = new generated.course.ShowAvatarInitially();
                
                if (oldOptions.getShowAvatarInitially().getAvatarChoice() != null) {
                    
                    generated.v6.course.ShowAvatarInitially.MediaSemantics mediaSematics = oldOptions.getShowAvatarInitially().getAvatarChoice(); 
                    generated.course.ShowAvatarInitially.MediaSemantics newMediaSematics = new generated.course.ShowAvatarInitially.MediaSemantics();
                    
                    newMediaSematics.setAvatar(mediaSematics.getAvatar());                          
                    newShowAvatarInitially.setAvatarChoice(newMediaSematics);
                }
            
                newOptions.setShowAvatarInitially(newShowAvatarInitially);
            }
            
            return newOptions;
    }
    
    /**
     * Used to test logic in this class.
     * 
     * @param args - unused
     */
    public static void main(String[] args){
        
        try {
            System.out.println("Starting...");
            
            @SuppressWarnings("unused")
            generated.v6.dkf.Scenario scenario = createScenario();
            
            System.out.println("Scenario generated successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
