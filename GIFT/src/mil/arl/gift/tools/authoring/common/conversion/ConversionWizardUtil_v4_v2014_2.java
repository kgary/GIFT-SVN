/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import generated.v4.dkf.Assessments.Survey;
import generated.v4.dkf.Nvpair;
import generated.v4.learner.Producers;
import generated.v5.learner.Inputs;
import generated.v5.learner.Producer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;

import org.xml.sax.SAXException;

/**
 * This class provides logic to migrate GIFT v4.0 XML files to GIFT v5.0 XML files using the appropriate schemas and generated
 * classes.  
 * 
 * Note: the only changes in XML schemas between these two version is at the DKF, Course, learner action and lesson material levels.
 * 
 * @author mhoffman
 * @author mzellars
 *
 */
public class ConversionWizardUtil_v4_v2014_2 extends AbstractConversionWizardUtil {

     /** course schema info */
    public static final File PREV_COURSE_FOLDER = new File("data"+File.separator+"conversionWizard"+File.separator+"v4"+File.separator+"domain"+File.separator+"course");
    public static final File PREV_COURSE_SCHEMA_FILE = new File(PREV_COURSE_FOLDER.getAbsolutePath()+File.separator+"course.xsd");
    public static final Class<?> COURSE_ROOT = generated.v4.course.Course.class;
    
    /** dkf schema info */
    public static final File PREV_DKF_SCHEMA_FILE = new File("data"+File.separator+"conversionWizard"+File.separator+"v4"+File.separator+"domain"+File.separator+"dkf"+File.separator+"dkf.xsd");
    public static final Class<?> DKF_ROOT = generated.v4.dkf.Scenario.class;
    
    /** learner config schema info */
    public static final File PREV_LEARNERCONFIG_FOLDER = new File("data"+File.separator+"conversionWizard"+File.separator+"v4"+File.separator+"learner");
    public static final File PREV_LEARNERCONFIG_SCHEMA_FILE = new File(PREV_LEARNERCONFIG_FOLDER.getAbsolutePath()+File.separator+"learnerConfig.xsd");
    public static final Class<?> LEARNERCONFIG_ROOT = generated.v4.learner.LearnerConfiguration.class;
    
    /** pedagogical config schema info */
    public static final File PREV_PEDCONFIG_FOLDER = new File("data"+File.separator+"conversionWizard"+File.separator+"v4"+File.separator+"ped");
    public static final File PREV_PEDCONFIG_SCHEMA_FILE = new File(PREV_PEDCONFIG_FOLDER.getAbsolutePath()+File.separator+"eM2AP.xsd");
    public static final Class<?> PEDCONFIG_ROOT = generated.v4.ped.EM2AP.class;
    
    /**
     * Auto-generate a GIFT v4.0 dkf object with every element/attribute instantiated.
     *  
     * @return generated.v4.dkf.Scenario - new 4.0 dkf object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v4.dkf.Scenario createScenario() throws Exception{
        Node rootNode = new Node();
        Object obj = createFullInstance(DKF_ROOT, rootNode);
        return (generated.v4.dkf.Scenario)obj;
    }

    @Override
    public UnmarshalledFile convertCourse(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(courseFile, getPreviousCourseSchemaFile(), getPreviousCourseSchemaRoot(), failOnFirstSchemaError);
        generated.v4.course.Course v4Course = (generated.v4.course.Course)uFile.getUnmarshalled();
        
        // Convert the version 4 course to the newest version and return it
        return convertCourse(v4Course, showCompletionDialog);  
    }
    
    
    /**
     * Convert the previous course schema object to a newer version of the course schema.
     * 
     * @param v4Course - the course schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return the new course
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertCourse(generated.v4.course.Course v4Course, boolean showCompletionDialog) throws IllegalArgumentException{
        
        generated.v5.course.Course newCourse = new generated.v5.course.Course();       
        
        //
        // copy over contents from old object to new object
        //    
        newCourse.setName(v4Course.getName());
        newCourse.setVersion(v4Course.getVersion());
        newCourse.setDescription(v4Course.getDescription());
        newCourse.setSurveyContext(v4Course.getSurveyContext());
   
        if (v4Course.getExclude() != null) {
            newCourse.setExclude(generated.v5.course.BooleanEnum.fromValue(v4Course.getExclude().toString().toLowerCase()));
        }
        
        //Concepts not converted in this version :(
        
        //TRANSITIONS
        generated.v5.course.Transitions newTransitions = convertTransitions(v4Course.getTransitions());
        newCourse.setTransitions(newTransitions);

        // Continue the conversion with the next Util
        ConversionWizardUtil_v2014_2_v2014_3X util = new ConversionWizardUtil_v2014_2_v2014_3X();
        util.setConversionIssueList(conversionIssueList);
        return util.convertCourse(newCourse, showCompletionDialog);
    }
    
    
    /**
     * Convert a Transitions course element.
     * 
     * @param transitions - the transitions element to migrate to a newer version
     * @return generated.v5.course.Transitions - the converted transitions element
     */
    private static generated.v5.course.Transitions convertTransitions(generated.v4.course.Transitions transitions) throws IllegalArgumentException {

        generated.v5.course.Transitions newTransitions = new generated.v5.course.Transitions();
        
        for(Object transitionObj : transitions.getTransitionType()){
            
            if(transitionObj instanceof generated.v4.course.Guidance){
                
                generated.v4.course.Guidance guidance = (generated.v4.course.Guidance)transitionObj;
                generated.v5.course.Guidance newGuidance = convertGuidance(guidance);
                
                newTransitions.getTransitionType().add(newGuidance);
                
            }else if(transitionObj instanceof generated.v4.course.PresentSurvey){
                
                generated.v4.course.PresentSurvey presentSurvey = (generated.v4.course.PresentSurvey)transitionObj;
                generated.v5.course.PresentSurvey newPresentSurvey = convertPresentSurvey(presentSurvey);

                newTransitions.getTransitionType().add(newPresentSurvey);
                
            }else if(transitionObj instanceof generated.v4.course.AAR){
                
                generated.v4.course.AAR aar = (generated.v4.course.AAR)transitionObj;               
                generated.v5.course.AAR newAAR = new generated.v5.course.AAR();
                
                newAAR.setTransitionName(aar.getTransitionName());
                
                newTransitions.getTransitionType().add(newAAR);
                
            }else if(transitionObj instanceof generated.v4.course.TrainingApplication){
                
                generated.v4.course.TrainingApplication trainApp = (generated.v4.course.TrainingApplication)transitionObj;
                generated.v5.course.TrainingApplication newTrainApp = new generated.v5.course.TrainingApplication();
              
                newTrainApp.setTransitionName(trainApp.getTransitionName());
                
                generated.v5.course.DkfRef newDkfRef = new generated.v5.course.DkfRef();
                newDkfRef.setFile(trainApp.getDkfRef().getFile());
                newTrainApp.setDkfRef(newDkfRef);
                
                newTrainApp.setFinishedWhen(trainApp.getFinishedWhen());
                
                if(trainApp.getGuidance() != null){
                    
                    generated.v5.course.Guidance newGuidance = convertGuidance(trainApp.getGuidance());
                    newTrainApp.setGuidance(newGuidance);
                }
                
                if (trainApp.getOptions() != null) {
                    
                    generated.v4.course.TrainingApplication.Options options = trainApp.getOptions();
                    generated.v5.course.TrainingApplication.Options newOptions = new generated.v5.course.TrainingApplication.Options();
                    
                    if (options.getDisableInstInterImpl() != null) {
                        
                        newOptions.setDisableInstInterImpl(generated.v5.course.BooleanEnum.fromValue(options.getDisableInstInterImpl().toString().toLowerCase()));
                    }
                    
                    if (options.getShowAvatarInitially() != null) {
                        
                        generated.v5.course.ShowAvatarInitially newShowAvatarInitially = new generated.v5.course.ShowAvatarInitially();
                        
                        if (options.getShowAvatarInitially().getAvatarChoice() != null) {
                            
                            generated.v4.course.ShowAvatarInitially.MediaSemantics mediaSematics = options.getShowAvatarInitially().getAvatarChoice(); 
                            generated.v5.course.ShowAvatarInitially.MediaSemantics newMediaSematics = new generated.v5.course.ShowAvatarInitially.MediaSemantics();
                            
                            newMediaSematics.setAvatar(mediaSematics.getAvatar());                          
                            newShowAvatarInitially.setAvatarChoice(newMediaSematics);
                        }
                    
                        newOptions.setShowAvatarInitially(newShowAvatarInitially);
                    }
                    
                    newTrainApp.setOptions(newOptions);
                }
                
                generated.v5.course.Interops newInterops = new generated.v5.course.Interops();               
                newTrainApp.setInterops(newInterops);
                
                for(generated.v4.course.Interop interop : trainApp.getInterops().getInterop()){
                    
                    generated.v5.course.Interop newInterop = new generated.v5.course.Interop();
                    newInterop.setInteropImpl(interop.getInteropImpl());
                    
                    newInterop.setInteropInputs(new generated.v5.course.InteropInputs());
                    
                    Object interopObj = interop.getInteropInputs().getInteropInput();
                    if(interopObj instanceof generated.v4.course.VBS2InteropInputs){
                        
                        generated.v4.course.VBS2InteropInputs vbs2 = (generated.v4.course.VBS2InteropInputs)interopObj;
                        generated.v5.course.VBS2InteropInputs newVbs2 = new generated.v5.course.VBS2InteropInputs();
                        
                        generated.v5.course.VBS2InteropInputs.LoadArgs newLoadArgs = new generated.v5.course.VBS2InteropInputs.LoadArgs();
                        newLoadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                        newVbs2.setLoadArgs(newLoadArgs);
                        
                        newInterop.getInteropInputs().setInteropInput(newVbs2);
                        
                    }else if(interopObj instanceof generated.v4.course.DISInteropInputs){
                        
                        generated.v5.course.DISInteropInputs newDIS = new generated.v5.course.DISInteropInputs();
                        newDIS.setLoadArgs(new generated.v5.course.DISInteropInputs.LoadArgs());
                        
                        newInterop.getInteropInputs().setInteropInput(newDIS);
                        
                    }else if(interopObj instanceof generated.v4.course.PowerPointInteropInputs){
                        
                        generated.v4.course.PowerPointInteropInputs ppt = (generated.v4.course.PowerPointInteropInputs)interopObj;
                        generated.v5.course.PowerPointInteropInputs newPPT = new generated.v5.course.PowerPointInteropInputs();
                        
                        newPPT.setLoadArgs(new generated.v5.course.PowerPointInteropInputs.LoadArgs());
                        
                        newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                        
                        newInterop.getInteropInputs().setInteropInput(newPPT);
                        
                    }else if(interopObj instanceof generated.v4.course.TC3InteropInputs){
                        
                        generated.v4.course.TC3InteropInputs tc3 = (generated.v4.course.TC3InteropInputs)interopObj;
                        generated.v5.course.TC3InteropInputs newTC3 = new generated.v5.course.TC3InteropInputs();
                        
                        newTC3.setLoadArgs(new generated.v5.course.TC3InteropInputs.LoadArgs());
                        
                        newTC3.getLoadArgs().setScenarioName(tc3.getLoadArgs().getScenarioName());
                        
                        newInterop.getInteropInputs().setInteropInput(newTC3);
                        
                    }else if(interopObj instanceof generated.v4.course.SCATTInteropInputs){
                    
                        generated.v5.course.SCATTInteropInputs newSCAT = new generated.v5.course.SCATTInteropInputs();              
                        newSCAT.setLoadArgs(new generated.v5.course.SCATTInteropInputs.LoadArgs());
                        
                        newInterop.getInteropInputs().setInteropInput(newSCAT);
                        
                    }else if(interopObj instanceof generated.v4.course.CustomInteropInputs){
                        
                        generated.v4.course.CustomInteropInputs custom = (generated.v4.course.CustomInteropInputs)interopObj;
                        generated.v5.course.CustomInteropInputs newCustom = new generated.v5.course.CustomInteropInputs();
                        
                        newCustom.setLoadArgs(new generated.v5.course.CustomInteropInputs.LoadArgs());
                        
                        for(generated.v4.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                            generated.v5.course.Nvpair newPair = new generated.v5.course.Nvpair();
                            newPair.setName(pair.getName());
                            newPair.setValue(pair.getValue());
                            newCustom.getLoadArgs().getNvpair().add(newPair);
                        }
                        
                        newInterop.getInteropInputs().setInteropInput(newCustom);
                        
                    }else{
                        throw new IllegalArgumentException("Found unhandled interop input type of "+interopObj);
                    }
                    
                    newTrainApp.getInterops().getInterop().add(newInterop);
                }
                
                newTransitions.getTransitionType().add(newTrainApp);
                
            }else if(transitionObj instanceof generated.v4.course.LessonMaterial){
                
                generated.v4.course.LessonMaterial lessonMaterial = (generated.v4.course.LessonMaterial)transitionObj;
                generated.v5.course.LessonMaterial newLessonMaterial = new generated.v5.course.LessonMaterial();
                
                newLessonMaterial.setTransitionName(lessonMaterial.getTransitionName());
                
                if(lessonMaterial.getLessonMaterialList() != null){
                    generated.v5.course.LessonMaterialList newLessonMaterialList = new generated.v5.course.LessonMaterialList();

                    for(generated.v4.course.Media media : lessonMaterial.getLessonMaterialList().getMedia()){
                        
                        generated.v5.course.Media newMedia = new generated.v5.course.Media();
                        newMedia.setName(media.getName());
                        newMedia.setUri(media.getUri());
                        
                        Object mediaType = media.getMediaTypeProperties();
                        if(mediaType instanceof generated.v4.course.PDFProperties){
                            newMedia.setMediaTypeProperties(new generated.v5.course.PDFProperties());
                            
                        }else if(mediaType instanceof generated.v4.course.WebpageProperties){
                            newMedia.setMediaTypeProperties(new generated.v5.course.WebpageProperties());
                            
                        }else if(mediaType instanceof generated.v4.course.YoutubeVideoProperties){
                            
                            generated.v4.course.YoutubeVideoProperties uTubeProp = (generated.v4.course.YoutubeVideoProperties)mediaType;
                            generated.v5.course.YoutubeVideoProperties newUTubeProp = new generated.v5.course.YoutubeVideoProperties();
                            
                            if (uTubeProp.getAllowFullScreen() != null) {
                                newUTubeProp.setAllowFullScreen(generated.v5.course.BooleanEnum.fromValue(uTubeProp.getAllowFullScreen().toString().toLowerCase()));
                            }
                            if (uTubeProp.getAllowAutoPlay() != null) {
                                newUTubeProp.setAllowAutoPlay(generated.v5.course.BooleanEnum.fromValue(uTubeProp.getAllowAutoPlay().toString().toLowerCase()));
                            }
                            
                            if(uTubeProp.getSize() != null){
                                generated.v5.course.Size newSize = new generated.v5.course.Size();
                                newSize.setHeight(uTubeProp.getSize().getHeight());
                                newSize.setWidth(uTubeProp.getSize().getWidth());
                                newUTubeProp.setSize(newSize);
                            }

                            newMedia.setMediaTypeProperties(newUTubeProp);
                            
                        }else if(mediaType instanceof generated.v4.course.ImageProperties){
                            newMedia.setMediaTypeProperties(new generated.v5.course.ImageProperties());
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled media type of "+mediaType);
                        }
                        
                        newLessonMaterialList.getMedia().add(newMedia);
                    }
                    
                    newLessonMaterial.setLessonMaterialList(newLessonMaterialList);
                }

                if(lessonMaterial.getLessonMaterialFiles() != null){
                    
                    generated.v5.course.LessonMaterialFiles newFiles = new generated.v5.course.LessonMaterialFiles();
                    for(String file : lessonMaterial.getLessonMaterialFiles().getFile()){
                        newFiles.getFile().add(file);
                    }                    
                    
                    newLessonMaterial.setLessonMaterialFiles(newFiles);                 
                }
                
                newTransitions.getTransitionType().add(newLessonMaterial);
                
            }else if(transitionObj instanceof generated.v4.course.MerrillsBranchPoint){
                
                generated.v4.course.MerrillsBranchPoint mBranchPoint = (generated.v4.course.MerrillsBranchPoint)transitionObj;          
                generated.v5.course.MerrillsBranchPoint newMBranchPoint = new generated.v5.course.MerrillsBranchPoint();
                
                newMBranchPoint.setTransitionName(mBranchPoint.getTransitionName());
                
                generated.v5.course.MerrillsBranchPoint.Quadrants newQuadrants = convertQuadrants(mBranchPoint.getQuadrants());             
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
     * @return generated.v5.course.MerrillsBranchPoint.Quadrants - the converted quadrants element
     */
    private static generated.v5.course.MerrillsBranchPoint.Quadrants convertQuadrants(generated.v4.course.MerrillsBranchPoint.Quadrants quadrants) throws IllegalArgumentException {
        
        generated.v5.course.MerrillsBranchPoint.Quadrants newQuadrants = new generated.v5.course.MerrillsBranchPoint.Quadrants();
        
        for (Object quadrant : quadrants.getContent()) {
            if (quadrant instanceof generated.v4.course.Rule) {
            
                newQuadrants.getContent().add(new generated.v5.course.Rule());
                
            }else if (quadrant instanceof generated.v4.course.Practice) {
                
                newQuadrants.getContent().add(new generated.v5.course.Practice());
                
            }else if (quadrant instanceof generated.v4.course.Recall) {
                
                newQuadrants.getContent().add(new generated.v5.course.Recall());
                
            }else if (quadrant instanceof generated.v4.course.Example) {
                
                newQuadrants.getContent().add(new generated.v5.course.Example());
                
            }else if (quadrant instanceof generated.v4.course.Transitions) {
                
                generated.v4.course.Transitions transitions = (generated.v4.course.Transitions)quadrant;
                generated.v5.course.Transitions newTransitions = convertTransitions(transitions);
                
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
     * @return generated.v5.course.Guidance - the converted guidance element
     */
    private static generated.v5.course.Guidance convertGuidance(generated.v4.course.Guidance guidance) throws IllegalArgumentException{
        
        generated.v5.course.Guidance newGuidance = new generated.v5.course.Guidance();
        
        newGuidance.setDisplayTime(guidance.getDisplayTime());
        newGuidance.setTransitionName(guidance.getTransitionName());
        
        if (guidance.getFullScreen() != null) {
            newGuidance.setFullScreen(generated.v5.course.BooleanEnum.fromValue(guidance.getFullScreen().toString().toLowerCase()));
        }
        
        Object guidanceChoice = guidance.getGuidanceChoice();
        if (guidanceChoice instanceof generated.v4.course.Guidance.Message) {

            generated.v4.course.Guidance.Message message = (generated.v4.course.Guidance.Message)guidanceChoice;
            
            generated.v5.course.Guidance.Message newMessage = new generated.v5.course.Guidance.Message();
            newMessage.setContent(message.getContent());
            
            newGuidance.setGuidanceChoice(newMessage);
            
        }else if (guidanceChoice instanceof generated.v4.course.Guidance.File) {
            
            generated.v4.course.Guidance.File file = (generated.v4.course.Guidance.File)guidanceChoice;
            
            generated.v5.course.Guidance.File newFile = new generated.v5.course.Guidance.File();
            
            newFile.setHTML(file.getHTML());
            
            newGuidance.setGuidanceChoice(newFile);
            
        }else if (guidanceChoice instanceof generated.v4.course.Guidance.URL) {
            
            generated.v4.course.Guidance.URL url = (generated.v4.course.Guidance.URL)guidanceChoice;
            
            generated.v5.course.Guidance.URL newURL = new generated.v5.course.Guidance.URL();
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
     * @return generated.v5.course.PresentSurvey - the converted PresentSurvey element
     */    
    private static generated.v5.course.PresentSurvey convertPresentSurvey(generated.v4.course.PresentSurvey presentSurvey) throws IllegalArgumentException{
        
        generated.v5.course.PresentSurvey newPresentSurvey = new generated.v5.course.PresentSurvey();
        
        Object surveyChoice = presentSurvey.getSurveyChoice();
        if (surveyChoice instanceof generated.v4.course.AutoTutorSession) {
            
            generated.v4.course.AutoTutorSession autoTutorSession = (generated.v4.course.AutoTutorSession)surveyChoice;
            
            generated.v5.course.AutoTutorSession newAutoTutorSession = new generated.v5.course.AutoTutorSession();
            
            generated.v5.course.DkfRef newDkfRef = new generated.v5.course.DkfRef();
            
            newDkfRef.setFile(autoTutorSession.getDkfRef().getFile());
            newAutoTutorSession.setDkfRef(newDkfRef);
            
            newPresentSurvey.setSurveyChoice(newAutoTutorSession);
            
        }else if (surveyChoice instanceof String) {
            
            newPresentSurvey.setSurveyChoice((String)surveyChoice);
        
        }else {
            throw new IllegalArgumentException("Found unhandled survey choice type of "+surveyChoice);
        }

        return newPresentSurvey;
    }   

    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(), failOnFirstSchemaError);
        generated.v4.dkf.Scenario v4Scenario = (generated.v4.dkf.Scenario)uFile.getUnmarshalled();

        return convertScenario(v4Scenario, showCompletionDialog);
    }
    
    /**
     * Convert the previous scenario schema object to a newer version of the scenario schema.
     * 
     * @param v4Scenario - the scenario schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return the new scenario
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertScenario(generated.v4.dkf.Scenario v4Scenario, boolean showCompletionDialog) throws IllegalArgumentException {
        
        generated.v5.dkf.Scenario newScenario = new generated.v5.dkf.Scenario();
         
        //
        // copy over contents from old object to new object
        //
        newScenario.setDescription(v4Scenario.getDescription());
        newScenario.setName(v4Scenario.getName());
        
        //
        //Learner Id
        //
        if(v4Scenario.getLearnerId() != null){
            generated.v5.dkf.LearnerId newLearnerId = new generated.v5.dkf.LearnerId();
            generated.v5.dkf.StartLocation newStartLocation = new generated.v5.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(v4Scenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }
        
        //
        //Resources
        //
        generated.v5.dkf.Resources newResources = new generated.v5.dkf.Resources();
        newResources.setSurveyContext(v4Scenario.getResources().getSurveyContext());
        
        generated.v5.dkf.AvailableLearnerActions newALA = new generated.v5.dkf.AvailableLearnerActions();
        
        if(v4Scenario.getResources().getAvailableLearnerActions() != null){
            
            generated.v4.dkf.AvailableLearnerActions ala = v4Scenario.getResources().getAvailableLearnerActions();
            if(ala.getLearnerActionsFiles() != null){
                generated.v5.dkf.LearnerActionsFiles newLAF = new generated.v5.dkf.LearnerActionsFiles();
                for(String filename : ala.getLearnerActionsFiles().getFile()){
                    newLAF.getFile().add(filename);
                }
                
                newALA.setLearnerActionsFiles(newLAF);
            }
            
            if(ala.getLearnerActionsList() != null){
                
                generated.v5.dkf.LearnerActionsList newLAL = new generated.v5.dkf.LearnerActionsList();
                for(generated.v4.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()){
                    
                    generated.v5.dkf.LearnerAction newAction = new generated.v5.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    newAction.setType(generated.v5.dkf.LearnerActionEnumType.fromValue(action.getType().value()));
                    newLAL.getLearnerAction().add(newAction);
                }
                newALA.setLearnerActionsList(newLAL);
            }
        
            newResources.setAvailableLearnerActions(newALA);
        }        
        
        newScenario.setResources(newResources);
        
        //
        //Assessment
        //
        generated.v5.dkf.Assessment newAssessment = new generated.v5.dkf.Assessment();
        if(v4Scenario.getAssessment() != null){
            
            generated.v4.dkf.Assessment assessment = v4Scenario.getAssessment();
            
            //
            // Objects
            //
            generated.v5.dkf.Objects newObjects = new generated.v5.dkf.Objects();
            if(assessment.getObjects() != null){
                
                if(assessment.getObjects().getWaypoints() != null){
                    
                    generated.v5.dkf.Waypoints newWaypoints = new generated.v5.dkf.Waypoints();
                    
                    generated.v4.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for(generated.v4.dkf.Waypoint waypoint : waypoints.getWaypoint()){
                        
                        generated.v5.dkf.Waypoint newWaypoint = new generated.v5.dkf.Waypoint();
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
            generated.v5.dkf.Tasks newTasks = new generated.v5.dkf.Tasks();
            if(assessment.getTasks() != null){
                
                for(generated.v4.dkf.Task task : assessment.getTasks().getTask()){
                    
                    generated.v5.dkf.Task newTask = new generated.v5.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());
                    
                    // start triggers
                    if(task.getStartTriggers() != null){
                        generated.v5.dkf.StartTriggers newStartTriggers = new generated.v5.dkf.StartTriggers();
                        newStartTriggers.getTriggers().addAll(convertTriggers( task.getStartTriggers().getTriggers()));
                        newTask.setStartTriggers(newStartTriggers);
                    }
                    
                    // end triggers
                    if(task.getEndTriggers() != null){
                        generated.v5.dkf.EndTriggers newEndTriggers = new generated.v5.dkf.EndTriggers();
                        newEndTriggers.getTriggers().addAll(convertTriggers( task.getEndTriggers().getTriggers()));
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
        if(v4Scenario.getActions() != null){
            
            generated.v4.dkf.Actions actions = v4Scenario.getActions();
            generated.v5.dkf.Actions newActions = new generated.v5.dkf.Actions();
            
            //instructional strategies
            if(actions.getInstructionalStrategies() != null){
                
                generated.v4.dkf.Actions.InstructionalStrategies iStrategies = actions.getInstructionalStrategies();
                generated.v5.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v5.dkf.Actions.InstructionalStrategies();
                
                for(generated.v4.dkf.Strategy strategy : iStrategies.getStrategy()){
                    
                    generated.v5.dkf.Strategy newStrategy = new generated.v5.dkf.Strategy();
                    newStrategy.setName(strategy.getName());
                    
                    Object strategyType = strategy.getStrategyType();//getValueAttribute();
                    if(strategyType instanceof generated.v4.dkf.PerformanceAssessment){
                        
                        generated.v4.dkf.PerformanceAssessment perfAss = (generated.v4.dkf.PerformanceAssessment)strategyType;
                        
                        generated.v5.dkf.PerformanceAssessment newPerfAss = new generated.v5.dkf.PerformanceAssessment();
                        newPerfAss.setNodeId(perfAss.getNodeId());
                        newPerfAss.setStrategyHandler(convertStrategyHandler(perfAss.getStrategyHandler()));
                        
                        newStrategy.setStrategyType(newPerfAss);
                        
                    }else if(strategyType instanceof generated.v4.dkf.InstructionalIntervention){
                        
                        generated.v4.dkf.InstructionalIntervention iIntervention = (generated.v4.dkf.InstructionalIntervention)strategyType;
                        
                        generated.v5.dkf.InstructionalIntervention newIIntervention = new generated.v5.dkf.InstructionalIntervention();
                        newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));

                        //only have a feedback choice in this version
                        for(generated.v4.dkf.Feedback feedback : iIntervention.getInterventionTypes()){
                            
                            generated.v5.dkf.Feedback newFeedback = new generated.v5.dkf.Feedback();
                            
                            if (feedback.getFeedbackPresentation() instanceof generated.v4.dkf.Feedback.Message) {
                                
                                generated.v4.dkf.Feedback.Message message = (generated.v4.dkf.Feedback.Message) feedback.getFeedbackPresentation();
                                
                                generated.v5.dkf.Feedback.Message newMessage = new generated.v5.dkf.Feedback.Message();
                                newMessage.setContent(message.getContent());
                                
                                newFeedback.setFeedbackPresentation(newMessage);
                        
                            }
                            else if (feedback.getFeedbackPresentation() instanceof generated.v4.dkf.Audio) {
                                
                                generated.v4.dkf.Audio audio = (generated.v4.dkf.Audio) feedback.getFeedbackPresentation();
                                
                                generated.v5.dkf.Audio newAudio = new generated.v5.dkf.Audio();
                                
                                // An audio object requires a .mp3 file but does not require a .ogg file
                                newAudio.setMP3File(audio.getMP3File());
                                
                                if (audio.getOGGFile() != null) {
                                    newAudio.setOGGFile(audio.getOGGFile());
                                }
                                
                                newFeedback.setFeedbackPresentation(newAudio);
                                
                            }
                            
                            else if (feedback.getFeedbackPresentation() instanceof generated.v4.dkf.MediaSemantics) {
                                
                                generated.v4.dkf.MediaSemantics semantics = (generated.v4.dkf.MediaSemantics) feedback.getFeedbackPresentation();
                                
                                generated.v5.dkf.MediaSemantics newSemantics = new generated.v5.dkf.MediaSemantics();
                                
                                // A MediaSematic file requires an avatar and a key name property.
                                newSemantics.setAvatar(semantics.getAvatar());
                                newSemantics.setKeyName(semantics.getKeyName());
                                
                                newFeedback.setFeedbackPresentation(newSemantics);
                            }
            
                            newIIntervention.getInterventionTypes().add(newFeedback);
                        }
                       
                        newStrategy.setStrategyType(newIIntervention);
                        
                    }else if(strategyType instanceof generated.v4.dkf.ScenarioAdaptation){
                        
                        generated.v4.dkf.ScenarioAdaptation adaptation = (generated.v4.dkf.ScenarioAdaptation)strategyType;
                        
                        generated.v5.dkf.ScenarioAdaptation newAdaptation = new generated.v5.dkf.ScenarioAdaptation();
                        newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));
                        
                        //only have environment adaptation in this version
                        for(generated.v4.dkf.EnvironmentAdaptation eAdapt : adaptation.getAdaptationTypes()){
                            
                            generated.v5.dkf.EnvironmentAdaptation newEAdapt = new generated.v5.dkf.EnvironmentAdaptation();
                            
                            generated.v5.dkf.EnvironmentAdaptation.Pair newPair = new generated.v5.dkf.EnvironmentAdaptation.Pair();
                            newPair.setType(eAdapt.getPair().getType());
                            newPair.setValue(eAdapt.getPair().getValue());
                            newEAdapt.setPair(newPair);
                            
                            newAdaptation.getAdaptationTypes().add(newEAdapt);
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
               
                generated.v4.dkf.Actions.StateTransitions sTransitions = actions.getStateTransitions();
                generated.v5.dkf.Actions.StateTransitions newSTransitions = new generated.v5.dkf.Actions.StateTransitions();
                
                for(generated.v4.dkf.Actions.StateTransitions.StateTransition sTransition : sTransitions.getStateTransition()){
                    
                    generated.v5.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v5.dkf.Actions.StateTransitions.StateTransition();
                    
                    generated.v5.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v5.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();
                    
                    //State type
                    for (Object stateType : sTransition.getLogicalExpression().getStateType()) {
                        if(stateType instanceof generated.v4.dkf.LearnerStateTransitionEnum){
    
                            generated.v4.dkf.LearnerStateTransitionEnum stateEnum = (generated.v4.dkf.LearnerStateTransitionEnum)stateType;
                            
                            generated.v5.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v5.dkf.LearnerStateTransitionEnum();
                            learnerStateTrans.setAttribute(stateEnum.getAttribute());
                            learnerStateTrans.setPrevious(stateEnum.getPrevious());
                            learnerStateTrans.setCurrent(stateEnum.getCurrent());                           
                                                        
                            if(stateEnum.getCurrent().equals("Any")) {
                                learnerStateTrans.setCurrent(null);
                            }
                            
                            if(stateEnum.getPrevious().equals("Any")) {
                                learnerStateTrans.setPrevious(null);
                            }
                            
                            newLogicalExpression.getStateType().add(learnerStateTrans);
                            
                        }else if(stateType instanceof generated.v4.dkf.PerformanceNode){
                            
                            generated.v4.dkf.PerformanceNode perfNode = (generated.v4.dkf.PerformanceNode)stateType;
                            
                            generated.v5.dkf.PerformanceNode newPerfNode = new generated.v5.dkf.PerformanceNode();
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
                    generated.v5.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v5.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    for(generated.v4.dkf.StrategyRef strategyRef : sTransition.getStrategyChoices().getStrategyRef()){
                        
                        generated.v5.dkf.StrategyRef newStrategyRef = new generated.v5.dkf.StrategyRef();
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
        
        ConversionWizardUtil_v2014_2_v2014_3X util = new ConversionWizardUtil_v2014_2_v2014_3X();
        util.setConversionIssueList(conversionIssueList);
        return util.convertScenario(newScenario, showCompletionDialog);
    
    }
    
    
    /**
     * Convert an entities object to a new entities object.
     * 
     * @param entities - the object to convert
     * @return generated.v5.dkf.Entities - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v5.dkf.Entities convertEntities(generated.v4.dkf.Entities entities) throws IllegalArgumentException{
        
        generated.v5.dkf.Entities newEntities = new generated.v5.dkf.Entities();
        for(generated.v4.dkf.StartLocation location : entities.getStartLocation()){
            
            generated.v5.dkf.StartLocation newLocation = new generated.v5.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }
        
        return newEntities;
    }
    
    /**
     * Convert a path object into a new path object.
     * 
     * @param path - the object to convert
     * @return generated.v5.dkf.Path - the new object
     */
    private static generated.v5.dkf.Path convertPath(generated.v4.dkf.Path path){
        
        generated.v5.dkf.Path newPath = new generated.v5.dkf.Path();
        for(generated.v4.dkf.Segment segment : path.getSegment()){
            
            generated.v5.dkf.Segment newSegment = new generated.v5.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());
            
            generated.v5.dkf.Start start = new generated.v5.dkf.Start();
            start.setWaypoint(segment.getStart().getWaypoint());
            newSegment.setStart(start);
            
            generated.v5.dkf.End end = new generated.v5.dkf.End();
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
     * @return generated.v5.dkf.Checkpoint - the new object
     */
    private static generated.v5.dkf.Checkpoint convertCheckpoint(generated.v4.dkf.Checkpoint checkpoint){
        
        generated.v5.dkf.Checkpoint newCheckpoint = new generated.v5.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setWaypoint(checkpoint.getWaypoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());
        
        return newCheckpoint;
    }
    
    /**
     * Convert an evaluators object into a new evaluators object.
     * 
     * @param evaluators - the object to convert
     * @return generated.v5.dkf.Evaluators - the new object
     */
    private static generated.v5.dkf.Evaluators convertEvaluators(generated.v4.dkf.Evaluators evaluators){
        
        generated.v5.dkf.Evaluators newEvaluators = new generated.v5.dkf.Evaluators();
        for(generated.v4.dkf.Evaluator evaluator : evaluators.getEvaluator()){
            
            generated.v5.dkf.Evaluator newEvaluator = new generated.v5.dkf.Evaluator();
            newEvaluator.setAssessment(evaluator.getAssessment());
            newEvaluator.setValue(evaluator.getValue());                                            
            newEvaluator.setOperator(evaluator.getOperator());
            
            newEvaluators.getEvaluator().add(newEvaluator);
        }
        
        return newEvaluators;
    }
    
    
    /**
     * Convert an assessment object into a new assessment object.
     * 
     * @param assessments - the assessment object to convert
     * @return generated.v5.dkf.Assessments - the new assessment object
     */
    private static generated.v5.dkf.Assessments convertAssessments(generated.v4.dkf.Assessments assessments){
        
        generated.v5.dkf.Assessments newAssessments = new generated.v5.dkf.Assessments();
        
        List<generated.v4.dkf.Assessments.Survey> surveys = new ArrayList<generated.v4.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
            if (assessmentType instanceof generated.v4.dkf.Assessments.Survey) {
                surveys.add((Survey) assessmentType);
            }
        }
        for(generated.v4.dkf.Assessments.Survey survey : surveys){
            
            generated.v5.dkf.Assessments.Survey newSurvey = new generated.v5.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());
            
            generated.v5.dkf.Questions newQuestions = new generated.v5.dkf.Questions();
            for(generated.v4.dkf.Question question : survey.getQuestions().getQuestion()){
                
                generated.v5.dkf.Question newQuestion = new generated.v5.dkf.Question();
                newQuestion.setKey(question.getKey());
                
                for(generated.v4.dkf.Reply reply : question.getReply()){
                    
                    generated.v5.dkf.Reply newReply = new generated.v5.dkf.Reply();
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
     * Convert a collection of trigger objects (start or end triggers) into the new schema version.
     * 
     * @param triggerObjects - collection of trigger objects to convert
     * @return List<Object> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<Serializable> convertTriggers(List<Object> triggerObjects) throws IllegalArgumentException{
        
        List<Serializable> newTriggerObjects = new ArrayList<>();
        for(Object triggerObj : triggerObjects){
            
            if(triggerObj instanceof generated.v4.dkf.EntityLocation){
                
                generated.v4.dkf.EntityLocation entityLocation = (generated.v4.dkf.EntityLocation)triggerObj;
                generated.v5.dkf.EntityLocation newEntityLocation = new generated.v5.dkf.EntityLocation();
                
                generated.v5.dkf.StartLocation startLocation = new generated.v5.dkf.StartLocation();
                startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
                newEntityLocation.setStartLocation(startLocation);
                
                generated.v5.dkf.TriggerLocation triggerLocation = new generated.v5.dkf.TriggerLocation();
                triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
                newEntityLocation.setTriggerLocation(triggerLocation);
                
                newTriggerObjects.add(newEntityLocation);
                
            }else if(triggerObj instanceof generated.v4.dkf.LearnerLocation){
                
                generated.v4.dkf.LearnerLocation learnerLocation = (generated.v4.dkf.LearnerLocation)triggerObj;
                generated.v5.dkf.LearnerLocation newLearnerLocation = new generated.v5.dkf.LearnerLocation();
                
                newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
                
                newTriggerObjects.add(newLearnerLocation);
                
            }else if(triggerObj instanceof generated.v4.dkf.ConceptEnded){
                
                generated.v4.dkf.ConceptEnded conceptEnded = (generated.v4.dkf.ConceptEnded)triggerObj;
                generated.v5.dkf.ConceptEnded newConceptEnded = new generated.v5.dkf.ConceptEnded();
                
                newConceptEnded.setNodeId(conceptEnded.getNodeId());
                
                newTriggerObjects.add(newConceptEnded);
                
            }else if(triggerObj instanceof generated.v4.dkf.ChildConceptEnded){
                
                generated.v4.dkf.ChildConceptEnded childConceptEnded = (generated.v4.dkf.ChildConceptEnded)triggerObj;
                generated.v5.dkf.ChildConceptEnded newChildConceptEnded = new generated.v5.dkf.ChildConceptEnded();
                
                newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());
                
                newTriggerObjects.add(newChildConceptEnded);
                
            }else{
                throw new IllegalArgumentException("Found unhandled trigger type of "+triggerObj);
            }
        }
        
        return newTriggerObjects;
    }
    
    /**
     * Convert a coordinate object into the latest schema version.
     * 
     * @param coordinate - v4.0 coordinate object to convert
     * @return generated.v5.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException
     */
    private static generated.v5.dkf.Coordinate convertCoordinate(generated.v4.dkf.Coordinate coordinate) throws IllegalArgumentException{
        
        generated.v5.dkf.Coordinate newCoord = new generated.v5.dkf.Coordinate();
        
        Object coordType = coordinate.getType();
        if(coordType instanceof generated.v4.dkf.GCC){
            
            generated.v4.dkf.GCC gcc = (generated.v4.dkf.GCC)coordType;
            generated.v5.dkf.GCC newGCC = new generated.v5.dkf.GCC();
            
            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());
            
            newCoord.setType(newGCC);
            
        }else if(coordType instanceof generated.v4.dkf.GDC){
           // generated.v4.
            generated.v4.dkf.GDC gdc = (generated.v4.dkf.GDC)coordType;
            generated.v5.dkf.GDC newGDC = new generated.v5.dkf.GDC();
            
            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());
            
            newCoord.setType(newGDC);
            
        }else if(coordType instanceof generated.v4.dkf.VBS2AGL){
            
            generated.v4.dkf.VBS2AGL agl = (generated.v4.dkf.VBS2AGL)coordType;
            generated.v5.dkf.VBS2AGL newAGL = new generated.v5.dkf.VBS2AGL();
            
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
     * @return generated.v5.dkf.StrategyHandler - the new object
     */
    private static generated.v5.dkf.StrategyHandler convertStrategyHandler(generated.v4.dkf.StrategyHandler handler){
        
        generated.v5.dkf.StrategyHandler newHandler = new generated.v5.dkf.StrategyHandler();
        newHandler.setImpl(handler.getImpl());
        
        if(handler.getParams() != null) {
            
            generated.v5.dkf.StrategyHandler.Params newParams = new generated.v5.dkf.StrategyHandler.Params();
            generated.v5.dkf.Nvpair nvpair = new generated.v5.dkf.Nvpair();
            
            nvpair.setName(handler.getParams().getNvpair().get(0).getName());
            nvpair.setValue(handler.getParams().getNvpair().get(0).getValue());
            
            newParams.getNvpair().add(nvpair);
            newHandler.setParams(newParams);
        }
        
        return newHandler;
    }
    
    /**
     * Convert a concepts object to a new version of the concepts object.
     * 
     * @param concepts - the object to convert
     * @return generated.v5.dkf.Concepts - the new object
     * @throws IllegalArgumentException
     */
    private generated.v5.dkf.Concepts convertConcepts(generated.v4.dkf.Concepts concepts) throws IllegalArgumentException{

        generated.v5.dkf.Concepts newConcepts = new generated.v5.dkf.Concepts();
        for(generated.v4.dkf.Concept concept : concepts.getConcept()){
            
            generated.v5.dkf.Concept newConcept = new generated.v5.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());
            
            if (concept.getAssessments() != null) {
                newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }
            
            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if(conditionsOrConcepts instanceof generated.v4.dkf.Concepts){
                //nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v4.dkf.Concepts)conditionsOrConcepts));
                
            }else if(conditionsOrConcepts instanceof generated.v4.dkf.Conditions){
                
                generated.v5.dkf.Conditions newConditions = new generated.v5.dkf.Conditions();
                
                generated.v4.dkf.Conditions conditions = (generated.v4.dkf.Conditions)conditionsOrConcepts;
                
                for(generated.v4.dkf.Condition condition : conditions.getCondition()){

                    generated.v5.dkf.Condition newCondition = new generated.v5.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());                        
                    
                    if(condition.getDefault() != null){
                        generated.v5.dkf.Default newDefault = new generated.v5.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }                            
                    
                    //Input
                    generated.v5.dkf.Input newInput = new generated.v5.dkf.Input();
                    if(condition.getInput() != null){
                        
                        Object inputType = condition.getInput().getType();
                        
                        if(inputType instanceof generated.v4.dkf.ApplicationCompletedCondition){
                            
                            generated.v4.dkf.ApplicationCompletedCondition conditionInput = (generated.v4.dkf.ApplicationCompletedCondition)inputType;
                            
                            generated.v5.dkf.ApplicationCompletedCondition newConditionInput = new generated.v5.dkf.ApplicationCompletedCondition();
                                
                            if (conditionInput.getIdealCompletionDurtaion() != null) {
                                newConditionInput.setIdealCompletionDuration(conditionInput.getIdealCompletionDurtaion());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if (inputType instanceof generated.v4.dkf.AutoTutorConditionInput){

                            generated.v4.dkf.AutoTutorConditionInput conditionInput = (generated.v4.dkf.AutoTutorConditionInput)inputType;
                            
                            generated.v5.dkf.AutoTutorConditionInput newConditionInput = new generated.v5.dkf.AutoTutorConditionInput();
                            
                            if (conditionInput.getScript() != null) {
                                
                                if (conditionInput.getScript() instanceof generated.v4.dkf.AutoTutorConditionInput.ATLocalSKO) {
                                    
                                    // Version 5 is currently no longer supporting the ATLocalSKO child.
                                    // Inform the user that this data was unable to be migrated.
                                    
                                    conversionIssueList.addIssue("Auto Tutor Local Shared Knowledge File Element (ATLocalSKO)");
                                    
                                }else if (conditionInput.getScript() instanceof generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO) {
                                    
                                    generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO atRemoteSKO = (generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO) conditionInput.getScript();
                                    
                                    generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO newATRemoteSKO = new generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO();
                                    
                                    generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO.URL url = atRemoteSKO.getURL();
                                    
                                    generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO.URL newURL = new generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO.URL();
                                    newURL.setAddress(url.getAddress());
                                    newATRemoteSKO.setURL(newURL);
                                    
                                    newConditionInput.setScript(newATRemoteSKO);
                                    
                                }
                                // Note: AutoTutorConditionInput.LocalSKO was added in version 5 and will need to be handled in the next conversion wizard (5_6).
                                else {
                                    throw new IllegalArgumentException("Found unhandled AutoTutorConditionInput script type of "+conditionInput.getScript());
                                }

                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v4.dkf.AvoidLocationCondition){
                            
                            generated.v4.dkf.AvoidLocationCondition conditionInput = (generated.v4.dkf.AvoidLocationCondition)inputType;
                            
                            generated.v5.dkf.AvoidLocationCondition newConditionInput = new generated.v5.dkf.AvoidLocationCondition();
                            
                            if(conditionInput.getWaypointRef() != null){                                    
                                newConditionInput.setWaypointRef(convertWaypointRef(conditionInput.getWaypointRef()));
                            }
                                                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v4.dkf.CheckpointPaceCondition){

                            generated.v4.dkf.CheckpointPaceCondition conditionInput = (generated.v4.dkf.CheckpointPaceCondition)inputType;
                            
                            generated.v5.dkf.CheckpointPaceCondition newConditionInput = new generated.v5.dkf.CheckpointPaceCondition();
                            for(generated.v4.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);
                                                            
                        }else if(inputType instanceof generated.v4.dkf.CheckpointProgressCondition){

                            generated.v4.dkf.CheckpointProgressCondition conditionInput = (generated.v4.dkf.CheckpointProgressCondition)inputType;
                            
                            generated.v5.dkf.CheckpointProgressCondition newConditionInput = new generated.v5.dkf.CheckpointProgressCondition();
                            for(generated.v4.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);                                
                            
                        }else if(inputType instanceof generated.v4.dkf.CorridorBoundaryCondition){
                            
                            generated.v4.dkf.CorridorBoundaryCondition conditionInput = (generated.v4.dkf.CorridorBoundaryCondition)inputType;
                            
                            generated.v5.dkf.CorridorBoundaryCondition newConditionInput = new generated.v5.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.CorridorPostureCondition){

                            generated.v4.dkf.CorridorPostureCondition conditionInput = (generated.v4.dkf.CorridorPostureCondition)inputType;
                            
                            generated.v5.dkf.CorridorPostureCondition newConditionInput = new generated.v5.dkf.CorridorPostureCondition();
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                

                            generated.v5.dkf.Postures postures = new generated.v5.dkf.Postures();
                            /*
                            for(generated.v4.dkf.PostureEnumType posture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(generated.v5.dkf.PostureEnumType.fromValue(posture.value()));
                            }
                            */
                            for(String strPosture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.EliminateHostilesCondition){

                            generated.v4.dkf.EliminateHostilesCondition conditionInput = (generated.v4.dkf.EliminateHostilesCondition)inputType;
                            
                            generated.v5.dkf.EliminateHostilesCondition newConditionInput = new generated.v5.dkf.EliminateHostilesCondition();
                             
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.EnterAreaCondition){

                            generated.v4.dkf.EnterAreaCondition conditionInput = (generated.v4.dkf.EnterAreaCondition)inputType;
                            
                            generated.v5.dkf.EnterAreaCondition newConditionInput = new generated.v5.dkf.EnterAreaCondition();
                            
                            for(generated.v4.dkf.Entrance entrance : conditionInput.getEntrance()){
                                
                                generated.v5.dkf.Entrance newEntrance = new generated.v5.dkf.Entrance();
                                
                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());
                                
                                generated.v5.dkf.Inside newInside = new generated.v5.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setWaypoint(entrance.getInside().getWaypoint());
                                newEntrance.setInside(newInside);
                                
                                generated.v5.dkf.Outside newOutside = new generated.v5.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setWaypoint(entrance.getOutside().getWaypoint());
                                newEntrance.setOutside(newOutside);
                                
                                newConditionInput.getEntrance().add(newEntrance);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.ExplosiveHazardSpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v4.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v4.dkf.ExplosiveHazardSpotReportCondition)inputType;
                            
                            generated.v5.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v5.dkf.ExplosiveHazardSpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.GenericConditionInput) {
                            
                            generated.v4.dkf.GenericConditionInput conditionInput = (generated.v4.dkf.GenericConditionInput)inputType;
                            
                            generated.v5.dkf.GenericConditionInput newConditionInput = new generated.v5.dkf.GenericConditionInput();
                            
                            if(conditionInput.getNvpair() != null){                                    
                                for (Nvpair nvPair : conditionInput.getNvpair()) {
                                    newConditionInput.getNvpair().add(convertNvpair(nvPair));
                                }
                            }
                                                            
                            newInput.setType(newConditionInput);
                        
                        }else if(inputType instanceof generated.v4.dkf.IdentifyPOIsCondition){

                            generated.v4.dkf.IdentifyPOIsCondition conditionInput = (generated.v4.dkf.IdentifyPOIsCondition)inputType;
                            
                            generated.v5.dkf.IdentifyPOIsCondition newConditionInput = new generated.v5.dkf.IdentifyPOIsCondition();
                            
                            if(conditionInput.getPois() != null){
                                
                                generated.v5.dkf.Pois pois = new generated.v5.dkf.Pois();
                                for(generated.v4.dkf.WaypointRef waypointRef : conditionInput.getPois().getWaypointRef()){
                                    pois.getWaypointRef().add(convertWaypointRef(waypointRef));
                                }
                                
                                newConditionInput.setPois(pois);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.LifeformTargetAccuracyCondition){

                            generated.v4.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v4.dkf.LifeformTargetAccuracyCondition)inputType;
                            
                            generated.v5.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v5.dkf.LifeformTargetAccuracyCondition();
                            
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v4.dkf.MarksmanshipPrecisionCondition) {
                        
                            generated.v4.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v4.dkf.MarksmanshipPrecisionCondition)inputType;
                            
                            generated.v5.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v5.dkf.MarksmanshipPrecisionCondition();
                            
                            if(conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if (inputType instanceof generated.v4.dkf.MarksmanshipSessionCompleteCondition) {
                        
                            generated.v4.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v4.dkf.MarksmanshipSessionCompleteCondition)inputType;
                            
                            generated.v5.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v5.dkf.MarksmanshipSessionCompleteCondition();
                            
                            if(conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }
                            
                            newInput.setType(newConditionInput);
                        
                        }else if(inputType instanceof generated.v4.dkf.NineLineReportCondition){

                            @SuppressWarnings("unused")
                            generated.v4.dkf.NineLineReportCondition conditionInput = (generated.v4.dkf.NineLineReportCondition)inputType;
                            
                            generated.v5.dkf.NineLineReportCondition newConditionInput = new generated.v5.dkf.NineLineReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.NumberOfShotsFiredCondition){
                         
                            generated.v4.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v4.dkf.NumberOfShotsFiredCondition)inputType;
                            
                            generated.v5.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v5.dkf.NumberOfShotsFiredCondition();
                                                            
                            if (conditionInput.getExpectedNumberOfShots() != null) {
                                newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }
                    
                            newInput.setType(newConditionInput);
                    
                        }else if(inputType instanceof generated.v4.dkf.PowerPointDwellCondition){

                            generated.v4.dkf.PowerPointDwellCondition conditionInput = (generated.v4.dkf.PowerPointDwellCondition)inputType;
                            
                            generated.v5.dkf.PowerPointDwellCondition newConditionInput = new generated.v5.dkf.PowerPointDwellCondition();
                            
                            generated.v5.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v5.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);
                            
                            generated.v5.dkf.PowerPointDwellCondition.Slides slides = new generated.v5.dkf.PowerPointDwellCondition.Slides();
                            for(generated.v4.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput.getSlides().getSlide()){
                                
                                generated.v5.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v5.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());
                                
                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.RulesOfEngagementCondition){

                            generated.v4.dkf.RulesOfEngagementCondition conditionInput = (generated.v4.dkf.RulesOfEngagementCondition)inputType;
                            
                            generated.v5.dkf.RulesOfEngagementCondition newConditionInput = new generated.v5.dkf.RulesOfEngagementCondition();
                            generated.v5.dkf.Wcs newWCS = new generated.v5.dkf.Wcs();
                            newWCS.setValue(generated.v5.dkf.WeaponControlStatusEnum.fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v4.dkf.SIMILEConditionInput){
                        
                            generated.v4.dkf.SIMILEConditionInput conditionInput = (generated.v4.dkf.SIMILEConditionInput)inputType;
                            
                            generated.v5.dkf.SIMILEConditionInput newConditionInput = new generated.v5.dkf.SIMILEConditionInput();
                        
                            if (conditionInput.getConditionKey() != null) {
                                newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }
                            
                            if (conditionInput.getConfigurationFile() != null) {
                                newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v4.dkf.SpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v4.dkf.SpotReportCondition conditionInput = (generated.v4.dkf.SpotReportCondition)inputType;
                            
                            generated.v5.dkf.SpotReportCondition newConditionInput = new generated.v5.dkf.SpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v4.dkf.UseRadioCondition){
                            
                            @SuppressWarnings("unused")
                            generated.v4.dkf.UseRadioCondition conditionInput = (generated.v4.dkf.UseRadioCondition)inputType;
                            
                            generated.v5.dkf.UseRadioCondition newConditionInput = new generated.v5.dkf.UseRadioCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled condition input type of "+inputType);
                        }

                    }
                    newCondition.setInput(newInput);
                    
                    //Scoring
                    generated.v5.dkf.Scoring newScoring = new generated.v5.dkf.Scoring();
                    if(condition.getScoring() != null){
                        // Only add the scoring element if it has children. 
                        // As of version 5, there cannot be a scoring element with no children
                        if (!condition.getScoring().getType().isEmpty()) {
                            
                            for(Object scoringType : condition.getScoring().getType()){
                                
                                if(scoringType instanceof generated.v4.dkf.Count){
                                    
                                    generated.v4.dkf.Count count = (generated.v4.dkf.Count)scoringType;
                                    
                                    generated.v5.dkf.Count newCount = new generated.v5.dkf.Count();                                    
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v5.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
                                    
                                    if(count.getEvaluators() != null){                                        
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newCount);
                                    
                                }else if(scoringType instanceof generated.v4.dkf.CompletionTime){
                                    
                                    generated.v4.dkf.CompletionTime complTime = (generated.v4.dkf.CompletionTime)scoringType;
                                    
                                    generated.v5.dkf.CompletionTime newComplTime = new generated.v5.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(generated.v5.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if(complTime.getEvaluators() != null){                                        
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newComplTime);
                                    
                                }else if(scoringType instanceof generated.v4.dkf.ViolationTime){
                                    
                                    generated.v4.dkf.ViolationTime violationTime = (generated.v4.dkf.ViolationTime)scoringType;
                                    
                                    generated.v5.dkf.ViolationTime newViolationTime = new generated.v5.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(generated.v5.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
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
     * @return generated.v5.dkf.Nvpair - the new object
     */
    private static generated.v5.dkf.Nvpair convertNvpair(generated.v4.dkf.Nvpair nvPair) {
        
        generated.v5.dkf.Nvpair newNvpair = new generated.v5.dkf.Nvpair();
        newNvpair.setName(nvPair.getName());
        newNvpair.setValue(nvPair.getValue());
        
        return newNvpair;
    }
    
    /**
     * Convert a waypointref object to a new waypointref object.
     * 
     * @param waypointRef - the object to convert
     * @return generated.v5.dkf.WaypointRef - the new object
     */
    private static generated.v5.dkf.WaypointRef convertWaypointRef(generated.v4.dkf.WaypointRef waypointRef){
        
        generated.v5.dkf.WaypointRef newWaypoint = new generated.v5.dkf.WaypointRef();
        newWaypoint.setValue(waypointRef.getValue());
        newWaypoint.setDistance(waypointRef.getDistance());
        
        return newWaypoint;
    }
    
    @Override
    public File getPreviousDKFSchemaFile() {
        return PREV_DKF_SCHEMA_FILE;
    }
    
    @Override
    public Class<?> getPreviousDKFSchemaRoot() {
        return DKF_ROOT;
    }
    
    @Override
    public File getPreviousCourseSchemaFile() {
        return PREV_COURSE_SCHEMA_FILE;
    }
    
    @Override
    public Class<?> getPreviousCourseSchemaRoot() {
        return COURSE_ROOT;
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
            generated.v4.dkf.Scenario scenario = createScenario();
            
            System.out.println("Scenario generated successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Begin the conversion of a v4.0 file to v5.0 by parsing the old file for the learner config
     * schema object and convert it to a newer version.
     */
    @Override
    public UnmarshalledFile convertLearnerConfiguration(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(courseFile, PREV_LEARNERCONFIG_SCHEMA_FILE, LEARNERCONFIG_ROOT, failOnFirstSchemaError);
        generated.v4.learner.LearnerConfiguration v4LearnerConfig = (generated.v4.learner.LearnerConfiguration)uFile.getUnmarshalled();
        
        
        return convertLearnerConfiguration(v4LearnerConfig, showCompletionDialog);
    }
    
    /**
     * Convert the previous learner schema object to a newer version of the course schema.
     * 
     * @param v4LearnerConfig - current version of the config file 
     * @param showCompletionDialog - show the completion dialog
     * @return the learner config
     */
    public UnmarshalledFile convertLearnerConfiguration(generated.v4.learner.LearnerConfiguration v4LearnerConfig, boolean showCompletionDialog) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        generated.v5.learner.LearnerConfiguration v5LearnerConfig = new generated.v5.learner.LearnerConfiguration();
        v5LearnerConfig.setVersion(v4LearnerConfig.getVersion());
        v5LearnerConfig.setInputs(convertInputs(v4LearnerConfig.getInputs()));
        
        
        ConversionWizardUtil_v2014_2_v2014_3X util = new ConversionWizardUtil_v2014_2_v2014_3X();
        util.setConversionIssueList(conversionIssueList);
        return util.convertLearnerConfiguration(v5LearnerConfig, showCompletionDialog);
    }

    /**
     * Takes all the inputs to a learner config file and individually converts them to the next version
     * @param v4Inputs - the previous version of Inputs
     * @return - the next version up of inputs
     */
    private Inputs convertInputs(generated.v4.learner.Inputs inputs) {
        Inputs v5Inputs = new Inputs();
        int index = 0;
        for(generated.v4.learner.Input input : inputs.getInput()){
            v5Inputs.getInput().add(new generated.v5.learner.Input());
            
            //create the classifier
            generated.v5.learner.Classifier classifier = new generated.v5.learner.Classifier(); 
            classifier.setClassifierImpl(input.getClassifier().getClassifierImpl());
            if(input.getClassifier().getProperties() != null){
                classifier.setProperties(convertProperties(input.getClassifier().getProperties()));
            }
            v5Inputs.getInput().get(index).setClassifier(classifier);
            
            //create the Predictor
            generated.v5.learner.Predictor predictor = new generated.v5.learner.Predictor();
            predictor.setPredictorImpl(input.getPredictor().getPredictorImpl());
            v5Inputs.getInput().get(index).setPredictor(predictor);
            
            //create and set the producers
            v5Inputs.getInput().get(index).setProducers(convertProducers(input.getProducers()));
            
            //create the Translator
            generated.v5.learner.Translator translator = new generated.v5.learner.Translator();
            translator.setTranslatorImpl(input.getTranslator().getTranslatorImpl());
            v5Inputs.getInput().get(index).setTranslator(translator);
            
            
            index++;
        }
        
        
        return v5Inputs;
    }
    

    /**
     * Helper method to convert the producers in a learner config file to the next version
     * 
     * @param v4producers - current version learner config producers
     * @return - v5 producers
     */
    private generated.v5.learner.Producers convertProducers(Producers producers) {
        generated.v5.learner.Producers v5Producers = new generated.v5.learner.Producers();
        int index = 0;
        
        
        for(generated.v4.learner.Producer producer : producers.getProducer()){
            v5Producers.getProducer().add(new Producer());
            v5Producers.getProducer().get(index).setSensorType(producer.getSensorType());
            index++;
        }
        return v5Producers;
    }

    /**
     * Converts the Properties of a classifier from v4 to v5 (The schemas did not change
     * between these 2 version so this simply copies the content of v4 properties
     * into the new v5 properties)
     * 
     * @param v4Properties - properties of the v4 classifier
     * @return the same properties only in v5
     */
    private generated.v5.learner.Properties convertProperties(generated.v4.learner.Properties v4Properties){
        generated.v5.learner.Properties v5Properties = new generated.v5.learner.Properties();
        int index = 0;
        
        for(generated.v4.learner.Property v4Property : v4Properties.getProperty()){
            v5Properties.getProperty().add(new generated.v5.learner.Property());
            v5Properties.getProperty().get(index).setName(v4Property.getName());
            v5Properties.getProperty().get(index).setValue(v4Property.getValue());
            index++;
        }
        
        return v5Properties;
    }
    
    /**
     * Begin the conversion of a v4.0 file to v5.0 by parsing the old file for the pedagogical config
     * schema object and convert it to a newer version.
     */
    @Override
    public UnmarshalledFile convertEMAPConfiguration(FileProxy pedConfigFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(pedConfigFile, PREV_PEDCONFIG_SCHEMA_FILE, PEDCONFIG_ROOT, failOnFirstSchemaError);
        generated.v4.ped.EM2AP v4PedConfig = (generated.v4.ped.EM2AP)uFile.getUnmarshalled();
        
        return convertPedagogicalConfiguration(v4PedConfig, showCompletionDialog);
    }

    /**
     * Convert the previous pedagogical schema object to a newer version of the course schema.
     * 
     * @param v4PedConfig - current version of the config file 
     * @param showCompletionDialog - show the completion dialog
     * @return the sensor config
     */
    public UnmarshalledFile convertPedagogicalConfiguration(generated.v4.ped.EM2AP v4PedConfig, boolean showCompletionDialog) {
        generated.v5.ped.EMAP v5PedConfig = new generated.v5.ped.EMAP();
        
        v5PedConfig.setExample(convertExample(v4PedConfig.getExample()));
        v5PedConfig.setPractice(convertPractice(v4PedConfig.getPractice()));
        v5PedConfig.setRecall(convertRecall(v4PedConfig.getRecall()));
        v5PedConfig.setRule(convertRule(v4PedConfig.getRule()));
        v5PedConfig.setVersion(v4PedConfig.getVersion());
        
        ConversionWizardUtil_v2014_2_v2014_3X util = new ConversionWizardUtil_v2014_2_v2014_3X();
        util.setConversionIssueList(conversionIssueList);
        return util.convertPedagogicalConfiguration(v5PedConfig, showCompletionDialog);
    }

    /**
     * Helper function to convert the Rules of a pedagogy config to the next version
     * @param v4Rule - current version of the rules 
     * @return v5 Rules
     */
    private generated.v5.ped.Rule convertRule(generated.v4.ped.Rule v4Rule) {
        generated.v5.ped.Rule newRule = new generated.v5.ped.Rule();
        generated.v5.ped.Attributes newAttributes = new generated.v5.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v4.ped.Attribute attribute : v4Rule.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.v5.ped.Attribute());
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
     * @param v4Recall - current version of the recall 
     * @return v5 Recall
     */
    private generated.v5.ped.Recall convertRecall(generated.v4.ped.Recall v4Recall) {
        generated.v5.ped.Recall newRecall = new generated.v5.ped.Recall();
        generated.v5.ped.Attributes newAttributes = new generated.v5.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v4.ped.Attribute attribute : v4Recall.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.v5.ped.Attribute());
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
     * @param v4Practice - current version of the practice 
     * @return v5 Practice
     */
    private generated.v5.ped.Practice convertPractice(generated.v4.ped.Practice v4Practice) {
        generated.v5.ped.Practice newPractice = new generated.v5.ped.Practice();
        generated.v5.ped.Attributes newAttributes = new generated.v5.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v4.ped.Attribute attribute : v4Practice.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.v5.ped.Attribute());
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
     * @param v4Example - current version of the example 
     * @return v5 Example
     */
    private generated.v5.ped.Example convertExample(generated.v4.ped.Example v4Example) {
        generated.v5.ped.Example newExample = new generated.v5.ped.Example();
        generated.v5.ped.Attributes newAttributes = new generated.v5.ped.Attributes();
        int index = 0;
        
        //sets the attributes for the example
        for(generated.v4.ped.Attribute attribute : v4Example.getAttributes().getAttribute()){
            newAttributes.getAttribute().add(new generated.v5.ped.Attribute());
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
     * @param v4metadataAttributes - current version's metadata
     * @return v5 metadata
     */
    private generated.v5.ped.MetadataAttributes convertMetaDataAttributes(generated.v4.ped.MetadataAttributes v4metadataAttributes) {
        generated.v5.ped.MetadataAttributes newMetaAttributes = new generated.v5.ped.MetadataAttributes(); 
        int index = 0;
        
        for(generated.v4.ped.MetadataAttribute attribute : v4metadataAttributes.getMetadataAttribute()){
            newMetaAttributes.getMetadataAttribute().add(new generated.v5.ped.MetadataAttribute());
            newMetaAttributes.getMetadataAttribute().get(index).setValue(attribute.getValue());
            if(attribute.getIsQuadrantSpecific() != null){
                newMetaAttributes.getMetadataAttribute().get(index).setIsQuadrantSpecific(generated.v5.ped.BooleanEnum.fromValue(attribute.getIsQuadrantSpecific().toString().toLowerCase()));
            }
            index++;
        }
        
        return newMetaAttributes;
    }
}