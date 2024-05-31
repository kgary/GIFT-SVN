/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import generated.v3.dkf.Nvpair;
import generated.v4.learner.Producers;
import generated.v4.learner.Properties;
import generated.v4.ped.Example;
import generated.v4.ped.Practice;
import generated.v4.ped.Recall;
import generated.v4.ped.Rule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;

import org.xml.sax.SAXException;

/**
 * This class provides logic to migrate GIFT v3.0 XML files to GIFT v4.0 XML files using the appropriate schemas and generated
 * classes.  
 * 
 * Note: No changes yet.
 * 
 * @author mhoffman
 *
 */
public class ConversionWizardUtil_v3_4 extends AbstractConversionWizardUtil {

    /** course schema info */
    public static final File PREV_COURSE_FOLDER = new File("data"+File.separator+"conversionWizard"+File.separator+"v3"+File.separator+"domain"+File.separator+"course");
    public static final File PREV_COURSE_SCHEMA_FILE = new File(PREV_COURSE_FOLDER.getAbsolutePath()+File.separator+"course.xsd");
    public static final Class<?> COURSE_ROOT = generated.v3.course.Course.class;
    
    /** dkf schema info */
    public static final File PREV_DKF_FOLDER = new File("data"+File.separator+"conversionWizard"+File.separator+"v3"+File.separator+"domain"+File.separator+"dkf");
    public static final File PREV_DKF_SCHEMA_FILE = new File(PREV_DKF_FOLDER.getAbsolutePath()+File.separator+"dkf.xsd");
    public static final Class<?> DKF_ROOT = generated.v3.dkf.Scenario.class;
    
    /** learner config schema info */
    public static final File PREV_LEARNERCONFIG_FOLDER = new File("data"+File.separator+"conversionWizard"+File.separator+"v3"+File.separator+"learner");
    public static final File PREV_LEARNERCONFIG_SCHEMA_FILE = new File(PREV_LEARNERCONFIG_FOLDER.getAbsolutePath()+File.separator+"learnerConfig.xsd");
    public static final Class<?> LEARNERCONFIG_ROOT = generated.v3.learner.LearnerConfiguration.class;
    
    /** pedagogical config schema info */
    public static final File PREV_PEDCONFIG_FOLDER = new File("data"+File.separator+"conversionWizard"+File.separator+"v3"+File.separator+"ped");
    public static final File PREV_PEDCONFIG_SCHEMA_FILE = new File(PREV_PEDCONFIG_FOLDER.getAbsolutePath()+File.separator+"eMAP.xsd");
    public static final Class<?> PEDCONFIG_ROOT = generated.v3.ped.EMAP.class;
       
    /**
     * Auto-generate a GIFT v3.0 course object with every element/attribute instantiated.
     *  
     * @return generated.course.Course - new 3.0 course object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v3.course.Course createCourse() throws Exception{
        Node rootNode = new Node();
        Object obj = createFullInstance(COURSE_ROOT, rootNode);
        return (generated.v3.course.Course)obj;
    }
    
    /**
     * Auto-generate a GIFT v3.0 dkf object with every element/attribute instantiated.
     *  
     * @return generated.dkf.Scenario - new 3.0 dkf object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v3.dkf.Scenario createScenario() throws Exception{
        Node rootNode = new Node();
        Object obj = createFullInstance(DKF_ROOT, rootNode);
        return (generated.v3.dkf.Scenario)obj;
    }
    
    @Override
    public UnmarshalledFile convertCourse(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(courseFile, getPreviousCourseSchemaFile(), getPreviousCourseSchemaRoot(), failOnFirstSchemaError);
    	generated.v3.course.Course v3Course = (generated.v3.course.Course)uFile.getUnmarshalled();
    	
    	// Convert the version 3 course to the newest version and return it
    	return convertCourse(v3Course, showCompletionDialog);
    }
    
    
    /**
     * Convert the previous course schema object to a newer version of the course schema.
     * 
     * @param v3Course - the course schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertCourse(generated.v3.course.Course v3Course, boolean showCompletionDialog) throws IllegalArgumentException{
		
    	generated.v4.course.Course newCourse = new generated.v4.course.Course();       
        
        //
        // copy over contents from old object to new object
        //    
        newCourse.setName(v3Course.getName());
        newCourse.setVersion(v3Course.getVersion());
        newCourse.setDescription(v3Course.getDescription());
        newCourse.setSurveyContext(v3Course.getSurveyContext());
   
        if (v3Course.getExclude() != null) {
        	newCourse.setExclude(generated.v4.course.BooleanEnum.fromValue(v3Course.getExclude().toString().toLowerCase()));
        }
        
        //TRANSITIONS
        generated.v4.course.Transitions newTransitions = convertTransitions(v3Course.getTransitions());
        newCourse.setTransitions(newTransitions);

    	// Continue the conversion with the next Util
        ConversionWizardUtil_v4_v2014_2 util = new ConversionWizardUtil_v4_v2014_2();
        util.setConversionIssueList(conversionIssueList);
        return util.convertCourse(newCourse, showCompletionDialog);
    }
    
    /**
     * Begin the conversion of a v3.0 file to v4.0 by parsing the old file for the learner config
     * schema object and convert it to a newer version.
     * 
     */
    @Override
    public UnmarshalledFile convertLearnerConfiguration(FileProxy learnerConfigFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(learnerConfigFile, PREV_LEARNERCONFIG_SCHEMA_FILE, LEARNERCONFIG_ROOT, failOnFirstSchemaError);
    	generated.v3.learner.LearnerConfiguration v3LearnerConfig = (generated.v3.learner.LearnerConfiguration)uFile.getUnmarshalled();
    	
        return convertLearnerConfiguration(v3LearnerConfig, showCompletionDialog);
    }
    
    /**
     * Convert the previous learner config schema object to a newer version of the course schema.
     * 
     * @param v3LearnerConfig - the current version of the config file 
     * @param showCompletionDialog - show the completeion dialog when done
     * @return - a call to the next version conversion wizard
     * @throws IOException -
     * @throws JAXBException -
     * @throws FileNotFoundException -
     * @throws SAXException -
     * @throws IllegalArgumentException - 
     */
	public UnmarshalledFile convertLearnerConfiguration(generated.v3.learner.LearnerConfiguration v3LearnerConfig, boolean showCompletionDialog) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	generated.v4.learner.LearnerConfiguration v4LearnerConfig = new generated.v4.learner.LearnerConfiguration();

    	v4LearnerConfig.setVersion(v3LearnerConfig.getVersion());
    	v4LearnerConfig.setInputs(convertInputs(v3LearnerConfig.getInputs()));
    	
    	
    	ConversionWizardUtil_v4_v2014_2 util = new ConversionWizardUtil_v4_v2014_2();
        util.setConversionIssueList(conversionIssueList);
    	return util.convertLearnerConfiguration(v4LearnerConfig, showCompletionDialog);
    }
    
	/**
	 * Takes all the inputs to a learner config file and individually converts them to the next version
	 * @param v3Inputs - the previous version of Inputs
	 * @return - the next version up of inputs
	 */
    private generated.v4.learner.Inputs convertInputs(generated.v3.learner.Inputs v3Inputs) {
    	generated.v4.learner.Inputs v4Inputs = new generated.v4.learner.Inputs();
		int index = 0;
		for(generated.v3.learner.Input input : v3Inputs.getInput()){
			v4Inputs.getInput().add(new generated.v4.learner.Input());
			
			//create the classifier
			generated.v4.learner.Classifier classifier = new generated.v4.learner.Classifier(); 
			classifier.setClassifierImpl(input.getClassifier().getClassifierImpl());
			if(input.getClassifier().getProperties() != null){
				classifier.setProperties(convertProperties(input.getClassifier().getProperties()));
			}
			v4Inputs.getInput().get(index).setClassifier(classifier);
			
			//create the Predictor
			generated.v4.learner.Predictor predictor = new generated.v4.learner.Predictor();
			predictor.setPredictorImpl(input.getPredictor().getPredictorImpl());
			v4Inputs.getInput().get(index).setPredictor(predictor);
			
			//create and set the producers
			if(input.getProducers() != null){
				v4Inputs.getInput().get(index).setProducers(convertProducers(input.getProducers()));
			}
			
			//create the Translator
			generated.v4.learner.Translator translator = new generated.v4.learner.Translator();
			translator.setTranslatorImpl(input.getTranslator().getTranslatorImpl());
			v4Inputs.getInput().get(index).setTranslator(translator);
			
			
			index++;
		}
		
		
		return v4Inputs;
	}

    /**
     * Helper method to convert the producers in a learner config file to the next version
     * 
     * @param v3producers - current version learner config producers
     * @return - v4 producers
     */
	private Producers convertProducers(generated.v3.learner.Producers v3producers) {
		generated.v4.learner.Producers v4Producers = new generated.v4.learner.Producers();
		int index = 0;
		
		
		for(generated.v3.learner.Producer producer : v3producers.getProducer()){
			v4Producers.getProducer().add(new generated.v4.learner.Producer());
			v4Producers.getProducer().get(index).setSensorType(producer.getSensorType().value());
			index++;
		}
		return v4Producers;
	}

	/**
	 * Helper method to convert the producers in a learner config file to the next version
	 * 
	 * @param v3Properties - current version learner config properties
	 * @return - v4 properties
	 */
	private Properties convertProperties(generated.v3.learner.Properties v3Properties) {
		generated.v4.learner.Properties v4Properties = new generated.v4.learner.Properties();
		int index = 0;
		
		for(generated.v3.learner.Property v3Property : v3Properties.getProperty()){
			v4Properties.getProperty().add(new generated.v4.learner.Property());
			v4Properties.getProperty().get(index).setName(v3Property.getName());
			v4Properties.getProperty().get(index).setValue(v3Property.getValue());
			index++;
		}
		
		return v4Properties;
	}

	private static generated.v4.course.Transitions convertTransitions(generated.v3.course.Transitions transitions) throws IllegalArgumentException {
    	
    	generated.v4.course.Transitions newTransitions = new generated.v4.course.Transitions();
    	
        for(Object transitionObj : transitions.getTransitionType()){
            
            if(transitionObj instanceof generated.v3.course.Guidance){
                
                generated.v3.course.Guidance guidance = (generated.v3.course.Guidance)transitionObj;
                generated.v4.course.Guidance newGuidance = convertGuidance(guidance);
                
                newTransitions.getTransitionType().add(newGuidance);
                
            }else if(transitionObj instanceof generated.v3.course.PresentSurvey){
                
                generated.v3.course.PresentSurvey presentSurvey = (generated.v3.course.PresentSurvey)transitionObj;
                generated.v4.course.PresentSurvey newPresentSurvey = convertPresentSurvey(presentSurvey);

                newTransitions.getTransitionType().add(newPresentSurvey);
                
            }else if(transitionObj instanceof generated.v3.course.AAR){
                
            	generated.v3.course.AAR aar = (generated.v3.course.AAR)transitionObj;           	
            	generated.v4.course.AAR newAAR = new generated.v4.course.AAR();
            	
            	newAAR.setTransitionName(aar.getTransitionName());
            	
                newTransitions.getTransitionType().add(newAAR);
                
            }else if(transitionObj instanceof generated.v3.course.TrainingApplication){
                
                generated.v3.course.TrainingApplication trainApp = (generated.v3.course.TrainingApplication)transitionObj;
                generated.v4.course.TrainingApplication newTrainApp = new generated.v4.course.TrainingApplication();
              
                newTrainApp.setTransitionName(trainApp.getTransitionName());
                
                generated.v4.course.DkfRef newDkfRef = new generated.v4.course.DkfRef();
                newDkfRef.setFile(trainApp.getDkfRef().getFile());
                newTrainApp.setDkfRef(newDkfRef);
                
                newTrainApp.setFinishedWhen(trainApp.getFinishedWhen().value());
                
                if(trainApp.getGuidance() != null){
                    
                    generated.v4.course.Guidance newGuidance = convertGuidance(trainApp.getGuidance());
                    newTrainApp.setGuidance(newGuidance);
                }
                
                if (trainApp.getOptions() != null) {
                	
	                generated.v3.course.TrainingApplication.Options options = trainApp.getOptions();
	                generated.v4.course.TrainingApplication.Options newOptions = new generated.v4.course.TrainingApplication.Options();
	                
	                if (options.getDisableInstInterImpl() != null) {
	                	
	                	newOptions.setDisableInstInterImpl(generated.v4.course.BooleanEnum.fromValue(options.getDisableInstInterImpl().toString().toLowerCase()));
	                }
	                
	                if (options.getShowAvatarInitially() != null) {
	                	
	                	generated.v4.course.ShowAvatarInitially newShowAvatarInitially = new generated.v4.course.ShowAvatarInitially();
	                	
	                	if (options.getShowAvatarInitially().getAvatarChoice() != null) {
	                		
		                	generated.v3.course.ShowAvatarInitially.MediaSemantics mediaSematics = options.getShowAvatarInitially().getAvatarChoice(); 
		                	generated.v4.course.ShowAvatarInitially.MediaSemantics newMediaSematics = new generated.v4.course.ShowAvatarInitially.MediaSemantics();
		                	
		                	newMediaSematics.setAvatar(mediaSematics.getAvatar());		                	
		                	newShowAvatarInitially.setAvatarChoice(newMediaSematics);
	                	}
	               	
	                	newOptions.setShowAvatarInitially(newShowAvatarInitially);
	                }
	                
	                newTrainApp.setOptions(newOptions);
                }
                
                generated.v4.course.Interops newInterops = new generated.v4.course.Interops();
                newTrainApp.setInterops(newInterops);
                
                for(generated.v3.course.Interop interop : trainApp.getInterops().getInterop()){
                    
                    generated.v4.course.Interop newInterop = new generated.v4.course.Interop();
                    newInterop.setInteropImpl(interop.getInteropImpl());
                    
                    newInterop.setInteropInputs(new generated.v4.course.InteropInputs());
                    
                    Object interopObj = interop.getInteropInputs().getInteropInput();
                    if(interopObj instanceof generated.v3.course.VBS2InteropInputs){
                        
                        generated.v3.course.VBS2InteropInputs vbs2 = (generated.v3.course.VBS2InteropInputs)interopObj;
                        generated.v4.course.VBS2InteropInputs newVbs2 = new generated.v4.course.VBS2InteropInputs();
                        
                        generated.v4.course.VBS2InteropInputs.LoadArgs newLoadArgs = new generated.v4.course.VBS2InteropInputs.LoadArgs();
                        newLoadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                        newVbs2.setLoadArgs(newLoadArgs);
                        
                        newInterop.getInteropInputs().setInteropInput(newVbs2);
                        
                    }else if(interopObj instanceof generated.v3.course.DISInteropInputs){
                        
                        generated.v4.course.DISInteropInputs newDIS = new generated.v4.course.DISInteropInputs();
                        newDIS.setLoadArgs(new generated.v4.course.DISInteropInputs.LoadArgs());
                        
                        newInterop.getInteropInputs().setInteropInput(newDIS);
                        
                    }else if(interopObj instanceof generated.v3.course.PowerPointInteropInputs){
                        
                        generated.v3.course.PowerPointInteropInputs ppt = (generated.v3.course.PowerPointInteropInputs)interopObj;
                        generated.v4.course.PowerPointInteropInputs newPPT = new generated.v4.course.PowerPointInteropInputs();
                        
                        newPPT.setLoadArgs(new generated.v4.course.PowerPointInteropInputs.LoadArgs());
                        
                        newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                        
                        newInterop.getInteropInputs().setInteropInput(newPPT);
                        
                    }else if(interopObj instanceof generated.v3.course.TC3InteropInputs){
                    	
                    	generated.v3.course.TC3InteropInputs tc3 = (generated.v3.course.TC3InteropInputs)interopObj;
                    	generated.v4.course.TC3InteropInputs newTC3 = new generated.v4.course.TC3InteropInputs();
                    	
                    	newTC3.setLoadArgs(new generated.v4.course.TC3InteropInputs.LoadArgs());
                    	
                    	newTC3.getLoadArgs().setScenarioName(tc3.getLoadArgs().getScenarioName());
                    	
                    	newInterop.getInteropInputs().setInteropInput(newTC3);
                    	
                    }else if(interopObj instanceof generated.v3.course.SCATTInteropInputs){
                    
                    	generated.v4.course.SCATTInteropInputs newSCAT = new generated.v4.course.SCATTInteropInputs();           	
                    	newSCAT.setLoadArgs(new generated.v4.course.SCATTInteropInputs.LoadArgs());
                    	
                    	newInterop.getInteropInputs().setInteropInput(newSCAT);
                    	
                	}else if(interopObj instanceof generated.v3.course.CustomInteropInputs){
                        
                        generated.v3.course.CustomInteropInputs custom = (generated.v3.course.CustomInteropInputs)interopObj;
                        generated.v4.course.CustomInteropInputs newCustom = new generated.v4.course.CustomInteropInputs();
                        
                        newCustom.setLoadArgs(new generated.v4.course.CustomInteropInputs.LoadArgs());
                        
                        for(generated.v3.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                            generated.v4.course.Nvpair newPair = new generated.v4.course.Nvpair();
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
                
            }else if(transitionObj instanceof generated.v3.course.LessonMaterial){
                
                generated.v3.course.LessonMaterial lessonMaterial = (generated.v3.course.LessonMaterial)transitionObj;
                generated.v4.course.LessonMaterial newLessonMaterial = new generated.v4.course.LessonMaterial();
                
                newLessonMaterial.setTransitionName(lessonMaterial.getTransitionName());
                
                if(lessonMaterial.getLessonMaterialList() != null){
                    generated.v4.course.LessonMaterialList newLessonMaterialList = new generated.v4.course.LessonMaterialList();

                    for(generated.v3.course.Media media : lessonMaterial.getLessonMaterialList().getMedia()){
                        
                        generated.v4.course.Media newMedia = new generated.v4.course.Media();
                        newMedia.setName(media.getName());
                        newMedia.setUri(media.getUri());
                        
                        Object mediaType = media.getMediaTypeProperties();
                        if(mediaType instanceof generated.v3.course.PDFProperties){
                            newMedia.setMediaTypeProperties(new generated.v4.course.PDFProperties());
                            
                        }else if(mediaType instanceof generated.v3.course.WebpageProperties){
                            newMedia.setMediaTypeProperties(new generated.v4.course.WebpageProperties());
                            
                        }else if(mediaType instanceof generated.v3.course.YoutubeVideoProperties){
                            
                            generated.v3.course.YoutubeVideoProperties uTubeProp = (generated.v3.course.YoutubeVideoProperties)mediaType;
                            generated.v4.course.YoutubeVideoProperties newUTubeProp = new generated.v4.course.YoutubeVideoProperties();
                            
                            if(uTubeProp.isAllowFullScreen() != null){
                                newUTubeProp.setAllowFullScreen(generated.v4.course.BooleanEnum.fromValue(uTubeProp.isAllowFullScreen().toString().toLowerCase()));
                            }
                            
                            if(uTubeProp.getSize() != null){
                                generated.v4.course.Size newSize = new generated.v4.course.Size();
                                newSize.setHeight(uTubeProp.getSize().getHeight());
                                newSize.setWidth(uTubeProp.getSize().getWidth());
                                newUTubeProp.setSize(newSize);
                            }

                            newMedia.setMediaTypeProperties(newUTubeProp);
                            
                        }else if(mediaType instanceof generated.v3.course.ImageProperties){
                            newMedia.setMediaTypeProperties(new generated.v4.course.ImageProperties());
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled media type of "+mediaType);
                        }
                        
                        newLessonMaterialList.getMedia().add(newMedia);
                    }
                    
                    newLessonMaterial.setLessonMaterialList(newLessonMaterialList);
                }

                if(lessonMaterial.getLessonMaterialFiles() != null){
                    
                    generated.v4.course.LessonMaterialFiles newFiles = new generated.v4.course.LessonMaterialFiles();
                    for(String file : lessonMaterial.getLessonMaterialFiles().getFile()){
                        newFiles.getFile().add(file);
                    }                    
                    
                    newLessonMaterial.setLessonMaterialFiles(newFiles);                 
                }
                
                newTransitions.getTransitionType().add(newLessonMaterial);
                
            }else if(transitionObj instanceof generated.v3.course.MerrillsBranchPoint){
            	
            	generated.v3.course.MerrillsBranchPoint mBranchPoint = (generated.v3.course.MerrillsBranchPoint)transitionObj;      	
            	generated.v4.course.MerrillsBranchPoint newMBranchPoint = new generated.v4.course.MerrillsBranchPoint();
            	
            	newMBranchPoint.setTransitionName(mBranchPoint.getTransitionName());
            	
            	generated.v4.course.MerrillsBranchPoint.Quadrants newQuadrants = convertQuadrants(mBranchPoint.getQuadrants());
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
     * @return generated.v3.course.MerrillsBranchPoint.Quadrants - the converted quadrants element
     */
    private static generated.v4.course.MerrillsBranchPoint.Quadrants convertQuadrants(generated.v3.course.MerrillsBranchPoint.Quadrants quadrants) throws IllegalArgumentException {
    	
    	generated.v4.course.MerrillsBranchPoint.Quadrants newQuadrants = new generated.v4.course.MerrillsBranchPoint.Quadrants();
    	
    	for (Object quadrant : quadrants.getContent()) {
    		if (quadrant instanceof generated.v3.course.Rule) {
    		
    			newQuadrants.getContent().add(new generated.v4.course.Rule());
    			
    		}else if (quadrant instanceof generated.v3.course.Practice) {
    			
    			newQuadrants.getContent().add(new generated.v4.course.Practice());
    			
    		}else if (quadrant instanceof generated.v3.course.Recall) {
    			
    			newQuadrants.getContent().add(new generated.v4.course.Recall());
    			
    		}else if (quadrant instanceof generated.v3.course.Example) {
    			
    			newQuadrants.getContent().add(new generated.v4.course.Example());
    			
    		}else if (quadrant instanceof generated.v3.course.Transitions) {
    			
    			generated.v3.course.Transitions transitions = (generated.v3.course.Transitions)quadrant;
    			generated.v4.course.Transitions newTransitions = convertTransitions(transitions);
    			
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
     * @return generated.v4.course.Guidance - the converted guidance element
     */
    private static generated.v4.course.Guidance convertGuidance(generated.v3.course.Guidance guidance) throws IllegalArgumentException{
        
        generated.v4.course.Guidance newGuidance = new generated.v4.course.Guidance();
        
       	newGuidance.setDisplayTime(guidance.getDisplayTime());
       	newGuidance.setTransitionName(guidance.getTransitionName());
        
        if (guidance.getFullScreen() != null) {
        	newGuidance.setFullScreen(generated.v4.course.BooleanEnum.fromValue(guidance.getFullScreen().toString().toLowerCase()));
        }
        
        Object guidanceChoice = guidance.getGuidanceChoice();
        if (guidanceChoice instanceof generated.v3.course.Guidance.Message) {

        	generated.v3.course.Guidance.Message message = (generated.v3.course.Guidance.Message)guidanceChoice;
        	
            generated.v4.course.Guidance.Message newMessage = new generated.v4.course.Guidance.Message();
            newMessage.setContent(message.getContent());
            
            newGuidance.setGuidanceChoice(newMessage);
        	
        }else if (guidanceChoice instanceof generated.v3.course.Guidance.File) {
        	
        	generated.v3.course.Guidance.File file = (generated.v3.course.Guidance.File)guidanceChoice;
        	
        	generated.v4.course.Guidance.File newFile = new generated.v4.course.Guidance.File();
        	newFile.setHTML(file.getHTML());
        	
        	newGuidance.setGuidanceChoice(newFile);
        	
        }else if (guidanceChoice instanceof generated.v3.course.Guidance.URL) {
        	
        	generated.v3.course.Guidance.URL url = (generated.v3.course.Guidance.URL)guidanceChoice;
        	
        	generated.v4.course.Guidance.URL newURL = new generated.v4.course.Guidance.URL();
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
     * @return generated.v4.course.PresentSurvey - the converted PresentSurvey element
     */    
    private static generated.v4.course.PresentSurvey convertPresentSurvey(generated.v3.course.PresentSurvey presentSurvey) throws IllegalArgumentException{
    	
    	generated.v4.course.PresentSurvey newPresentSurvey = new generated.v4.course.PresentSurvey();
    	
    	Object surveyChoice = presentSurvey.getSurveyChoice();
    	if (surveyChoice instanceof generated.v3.course.AutoTutorSession) {
    		
    		generated.v3.course.AutoTutorSession autoTutorSession = (generated.v3.course.AutoTutorSession)surveyChoice;
    		
    		generated.v4.course.AutoTutorSession newAutoTutorSession = new generated.v4.course.AutoTutorSession();
    		
    		generated.v4.course.DkfRef newDkfRef = new generated.v4.course.DkfRef();
    		
    		newDkfRef.setFile(autoTutorSession.getDkfRef().getFile());
    		newAutoTutorSession.setDkfRef(newDkfRef);
    		
    		newPresentSurvey.setSurveyChoice(newAutoTutorSession);
    		
    	}else if (surveyChoice instanceof String) {
    		
    		newPresentSurvey.setSurveyChoice(surveyChoice);
    	
    	}else {
    		throw new IllegalArgumentException("Found unhandled survey choice type of "+surveyChoice);
    	}

    	return newPresentSurvey;
    }   
    
    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{

    	UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(), failOnFirstSchemaError);
    	generated.v3.dkf.Scenario v3Scenario = (generated.v3.dkf.Scenario)uFile.getUnmarshalled();
		
    	// Convert the version 3 scenario to the newest version and return it
    	return convertScenario(v3Scenario, showCompletionDialog);
    	
    }
    
    /**
     * Convert the previous scenario schema object to a newer version of the scenario schema.
     * 
     * @param v3Scenario - the scenario schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new scenario
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertScenario(generated.v3.dkf.Scenario v3Scenario, boolean showCompletionDialog) throws IllegalArgumentException {
    	
    	generated.v4.dkf.Scenario newScenario = new generated.v4.dkf.Scenario();

    	//
        // copy over contents from old object to new object
        //
    	newScenario.setDescription(v3Scenario.getDescription());
        newScenario.setName(v3Scenario.getName());
         
        //
        //Learner Id
        //
        if(v3Scenario.getLearnerId() != null){
            generated.v4.dkf.LearnerId newLearnerId = new generated.v4.dkf.LearnerId();
            generated.v4.dkf.StartLocation newStartLocation = new generated.v4.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(v3Scenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }
        
        //
        //Resources
        //
        generated.v4.dkf.Resources newResources = new generated.v4.dkf.Resources();
        newResources.setSurveyContext(v3Scenario.getResources().getSurveyContext());
        
        generated.v4.dkf.AvailableLearnerActions newALA = new generated.v4.dkf.AvailableLearnerActions();
        
        if(v3Scenario.getResources().getAvailableLearnerActions() != null){
            
            generated.v3.dkf.AvailableLearnerActions ala = v3Scenario.getResources().getAvailableLearnerActions();
            if(ala.getLearnerActionsFiles() != null){
                generated.v4.dkf.LearnerActionsFiles newLAF = new generated.v4.dkf.LearnerActionsFiles();
                for(String filename : ala.getLearnerActionsFiles().getFile()){
                    newLAF.getFile().add(filename);
                }
                
                newALA.setLearnerActionsFiles(newLAF);
            }
            
            if(ala.getLearnerActionsList() != null){
                
                generated.v4.dkf.LearnerActionsList newLAL = new generated.v4.dkf.LearnerActionsList();
                for(generated.v3.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()){
                    
                    generated.v4.dkf.LearnerAction newAction = new generated.v4.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    newAction.setType(generated.v4.dkf.LearnerActionEnumType.fromValue(action.getType().value()));
                    newLAL.getLearnerAction().add(newAction);
                }
                newALA.setLearnerActionsList(newLAL);
            }
        
            newResources.setAvailableLearnerActions(newALA);
        }        
        
        newScenario.setResources(newResources);
        
        //
        // Assessment
        //
        generated.v4.dkf.Assessment newAssessment = new generated.v4.dkf.Assessment();
        if(v3Scenario.getAssessment() != null){
            
            generated.v3.dkf.Assessment assessment = v3Scenario.getAssessment();
            
            //
            // Objects
            //
            generated.v4.dkf.Objects newObjects = new generated.v4.dkf.Objects();
            if(assessment.getObjects() != null){
                
                if(assessment.getObjects().getWaypoints() != null){
                    
                    generated.v4.dkf.Waypoints newWaypoints = new generated.v4.dkf.Waypoints();
                    
                    generated.v3.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for(generated.v3.dkf.Waypoint waypoint : waypoints.getWaypoint()){
                        
                        generated.v4.dkf.Waypoint newWaypoint = new generated.v4.dkf.Waypoint();
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
            generated.v4.dkf.Tasks newTasks = new generated.v4.dkf.Tasks();
            if(assessment.getTasks() != null){
                
                for(generated.v3.dkf.Task task : assessment.getTasks().getTask()){
                    
                    generated.v4.dkf.Task newTask = new generated.v4.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());
                    
                    // start triggers
                    if(task.getStartTriggers() != null){
                        generated.v4.dkf.StartTriggers newStartTriggers = new generated.v4.dkf.StartTriggers();
                        newStartTriggers.getTriggers().addAll(convertTriggers( task.getStartTriggers().getTriggers()));
                        newTask.setStartTriggers(newStartTriggers);
                    }
                    
                    // end triggers
                    if(task.getEndTriggers() != null){
                        generated.v4.dkf.EndTriggers newEndTriggers = new generated.v4.dkf.EndTriggers();
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
        if(v3Scenario.getActions() != null){
            
            generated.v3.dkf.Actions actions = v3Scenario.getActions();
            generated.v4.dkf.Actions newActions = new generated.v4.dkf.Actions();
            
            //instructional strategies
            if(actions.getInstructionalStrategies() != null){
                
                generated.v3.dkf.Actions.InstructionalStrategies iStrategies = actions.getInstructionalStrategies();
                generated.v4.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v4.dkf.Actions.InstructionalStrategies();
                
                for(generated.v3.dkf.Strategy strategy : iStrategies.getStrategy()){
                    
                    generated.v4.dkf.Strategy newStrategy = new generated.v4.dkf.Strategy();
                    newStrategy.setName(strategy.getName());
                    
                    Object strategyType = strategy.getStrategyType();
                    if(strategyType instanceof generated.v3.dkf.PerformanceAssessment){
                        
                        generated.v3.dkf.PerformanceAssessment perfAss = (generated.v3.dkf.PerformanceAssessment)strategyType;
                        
                        generated.v4.dkf.PerformanceAssessment newPerfAss = new generated.v4.dkf.PerformanceAssessment();
                        newPerfAss.setNodeId(perfAss.getNodeId());
                        newPerfAss.setStrategyHandler(convertStrategyHandler(perfAss.getStrategyHandler()));
                        
                        newStrategy.setStrategyType(newPerfAss);
                        
                    }else if(strategyType instanceof generated.v3.dkf.InstructionalIntervention){
                    	 generated.v3.dkf.InstructionalIntervention iIntervention = (generated.v3.dkf.InstructionalIntervention)strategyType;
                         
                         generated.v4.dkf.InstructionalIntervention newIIntervention = new generated.v4.dkf.InstructionalIntervention();
                         newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));
                         
                         //only have a feedback choice in this version 
                         for(generated.v3.dkf.Feedback feedback : iIntervention.getInterventionTypes()){
                             
                             generated.v4.dkf.Feedback newFeedback = new generated.v4.dkf.Feedback();
                             
                             if (feedback.getFeedbackPresentation() instanceof generated.v3.dkf.Feedback.Message) {
                             	
                             	generated.v4.dkf.Feedback.Message newMessage = new generated.v4.dkf.Feedback.Message();
                             	generated.v3.dkf.Feedback.Message oldMessage = (generated.v3.dkf.Feedback.Message) feedback.getFeedbackPresentation();
                             	newMessage.setContent(oldMessage.getContent());
                             	
                             	newFeedback.setFeedbackPresentation(newMessage);
                        	
                             }
                             else if (feedback.getFeedbackPresentation() instanceof generated.v3.dkf.Audio) {
                            	 
                             	generated.v4.dkf.Audio newAudio = new generated.v4.dkf.Audio();
                             	generated.v3.dkf.Audio oldAudio = (generated.v3.dkf.Audio) feedback.getFeedbackPresentation();
                             	
                             	// An audio object requires a .mp3 file but does not require a .ogg file
                             	newAudio.setMP3File(oldAudio.getMP3File());
                             	
                             	if (oldAudio.getOGGFile() != null) {
                             		newAudio.setOGGFile(oldAudio.getOGGFile());
                             	}
                             	
                             	newFeedback.setFeedbackPresentation(newAudio);
                             	
                             }
                             
                             else if (feedback.getFeedbackPresentation() instanceof generated.v3.dkf.MediaSemantics) {
                             	
                             	generated.v4.dkf.MediaSemantics newSemantics = new generated.v4.dkf.MediaSemantics();
                             	generated.v3.dkf.MediaSemantics oldSemantics = (generated.v3.dkf.MediaSemantics) feedback.getFeedbackPresentation();
                             	
                             	// A MediaSematic file requires an avatar and a key name property.
                             	newSemantics.setAvatar(oldSemantics.getAvatar());
                             	newSemantics.setKeyName(oldSemantics.getKeyName());
                             	
                             	newFeedback.setFeedbackPresentation(newSemantics);
                             }
             
                             newIIntervention.getInterventionTypes().add(newFeedback);
                         }
                         
                         newStrategy.setStrategyType(newIIntervention);
                    	
                    }else if(strategyType instanceof generated.v3.dkf.ScenarioAdaptation){
                        
                        generated.v3.dkf.ScenarioAdaptation adaptation = (generated.v3.dkf.ScenarioAdaptation)strategyType;
                        
                        generated.v4.dkf.ScenarioAdaptation newAdaptation = new generated.v4.dkf.ScenarioAdaptation();
                        newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));
                        
                        //only have environment adaptation in this version
                        for(generated.v3.dkf.EnvironmentAdaptation eAdapt : adaptation.getAdaptationTypes()){
                            
                            generated.v4.dkf.EnvironmentAdaptation newEAdapt = new generated.v4.dkf.EnvironmentAdaptation();
                            
                            generated.v4.dkf.EnvironmentAdaptation.Pair newPair = new generated.v4.dkf.EnvironmentAdaptation.Pair();
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
               
                generated.v3.dkf.Actions.StateTransitions sTransitions = actions.getStateTransitions();
                generated.v4.dkf.Actions.StateTransitions newSTransitions = new generated.v4.dkf.Actions.StateTransitions();
                
                for(generated.v3.dkf.Actions.StateTransitions.StateTransition sTransition : sTransitions.getStateTransition()){
                    
                    generated.v4.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v4.dkf.Actions.StateTransitions.StateTransition();
                    
                    generated.v4.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v4.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();
                    
                    //State type
                    for (Object stateType : sTransition.getLogicalExpression().getStateType()) {
                    	
	                    if(stateType instanceof generated.v3.dkf.LearnerStateTransitionEnum){
	                        
	                        generated.v3.dkf.LearnerStateTransitionEnum stateEnum = (generated.v3.dkf.LearnerStateTransitionEnum)stateType;
	                        
	                        generated.v4.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v4.dkf.LearnerStateTransitionEnum();
	                        learnerStateTrans.setAttribute(stateEnum.getAttribute());
	                        learnerStateTrans.setCurrent(stateEnum.getCurrent());
	                        learnerStateTrans.setPrevious(stateEnum.getPrevious());
	                        
	                        newLogicalExpression.getStateType().add(learnerStateTrans);
	                        
	                    }else if(stateType instanceof generated.v3.dkf.PerformanceNode){
	                        
	                        generated.v3.dkf.PerformanceNode perfNode = (generated.v3.dkf.PerformanceNode)stateType;
	                        
	                        generated.v4.dkf.PerformanceNode newPerfNode = new generated.v4.dkf.PerformanceNode();
	                        newPerfNode.setName(perfNode.getName());
	                        newPerfNode.setNodeId(perfNode.getNodeId());
	                        newPerfNode.setCurrent(perfNode.getCurrent().value());
	                        newPerfNode.setPrevious(perfNode.getPrevious().value());
	                        
	                        newLogicalExpression.getStateType().add(newPerfNode);
	                        
	                    }else{
	                        throw new IllegalArgumentException("Found unhandled action's state transition state type of "+stateType);
	                    }
                    }
                    
                    newSTransition.setLogicalExpression(newLogicalExpression);
                    
                    //Strategy Choices
                    generated.v4.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v4.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    for(generated.v3.dkf.StrategyRef strategyRef : sTransition.getStrategyChoices().getStrategyRef()){
                        
                        generated.v4.dkf.StrategyRef newStrategyRef = new generated.v4.dkf.StrategyRef();
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
       
        // Continue the conversion with the next Util
        ConversionWizardUtil_v4_v2014_2 nextConverter = new ConversionWizardUtil_v4_v2014_2();
        nextConverter.setConversionIssueList(conversionIssueList);
    	return nextConverter.convertScenario(newScenario, showCompletionDialog);
    }
    
    /**
     * Convert a strategy handler object to a new version of the strategy handler object.
     * 
     * @param handler - the object to convert
     * @return generated.v4.dkf.StrategyHandler - the new object
     */
    private static generated.v4.dkf.StrategyHandler convertStrategyHandler(generated.v3.dkf.StrategyHandler handler){
        
        generated.v4.dkf.StrategyHandler newHandler = new generated.v4.dkf.StrategyHandler();
        
        if(handler.getParams() != null) {
        	
        	generated.v4.dkf.StrategyHandler.Params newParams = new generated.v4.dkf.StrategyHandler.Params();
        	generated.v4.dkf.Nvpair nvpair = new generated.v4.dkf.Nvpair();
        	
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
     * @return generated.v3.dkf.Concepts - the new object
     * @throws Exception
     */
    private static generated.v4.dkf.Concepts convertConcepts(generated.v3.dkf.Concepts concepts) throws IllegalArgumentException{
        
        generated.v4.dkf.Concepts newConcepts = new generated.v4.dkf.Concepts();
        for(generated.v3.dkf.Concept concept : concepts.getConcept()){
            
            generated.v4.dkf.Concept newConcept = new generated.v4.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());
            
            if (concept.getAssessments() != null) {
            	newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }
            
            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if(conditionsOrConcepts instanceof generated.v3.dkf.Concepts){
                //nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v3.dkf.Concepts)conditionsOrConcepts));
                
            }else if(conditionsOrConcepts instanceof generated.v3.dkf.Conditions){

                generated.v4.dkf.Conditions newConditions = new generated.v4.dkf.Conditions();
                
                generated.v3.dkf.Conditions conditions = (generated.v3.dkf.Conditions)conditionsOrConcepts;
                
                for(generated.v3.dkf.Condition condition : conditions.getCondition()){
                	
                    generated.v4.dkf.Condition newCondition = new generated.v4.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());                        
                    
                    if(condition.getDefault() != null){
                        generated.v4.dkf.Default newDefault = new generated.v4.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment().value());
                        newCondition.setDefault(newDefault);
                    }                            
 
                    //Input
                    generated.v4.dkf.Input newInput = new generated.v4.dkf.Input();
                    if(condition.getInput() != null){
     
                        Object inputType = condition.getInput().getType();
                        if(inputType instanceof generated.v3.dkf.ApplicationCompletedCondition){
                  
                            generated.v3.dkf.ApplicationCompletedCondition conditionInput = (generated.v3.dkf.ApplicationCompletedCondition)inputType;

                            generated.v4.dkf.ApplicationCompletedCondition newConditionInput = new generated.v4.dkf.ApplicationCompletedCondition();
                                                            
                            if (conditionInput.getIdealCompletionDurtaion() != null) {
                            	newConditionInput.setIdealCompletionDurtaion(conditionInput.getIdealCompletionDurtaion());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if (inputType instanceof generated.v3.dkf.AutoTutorConditionInput){
                        	
                        	generated.v3.dkf.AutoTutorConditionInput conditionInput = (generated.v3.dkf.AutoTutorConditionInput)inputType;
                            
                            generated.v4.dkf.AutoTutorConditionInput newConditionInput = new generated.v4.dkf.AutoTutorConditionInput();
                            
                            if (conditionInput.getScript() != null) {
                            	if (conditionInput.getScript() instanceof generated.v3.dkf.AutoTutorConditionInput.ATLocalSKO) {
                            		
                            		generated.v3.dkf.AutoTutorConditionInput.ATLocalSKO atLocalSKO = (generated.v3.dkf.AutoTutorConditionInput.ATLocalSKO) conditionInput.getScript();
                            		
                            		generated.v4.dkf.AutoTutorConditionInput.ATLocalSKO newATLocalSKO = new generated.v4.dkf.AutoTutorConditionInput.ATLocalSKO();
                            		newATLocalSKO.setScriptName(atLocalSKO.getScriptName());
                            		
                            		newConditionInput.setScript(newATLocalSKO);
                            		
                            	}else if (conditionInput.getScript() instanceof generated.v3.dkf.AutoTutorConditionInput.ATRemoteSKO) {
                            		
                            		generated.v3.dkf.AutoTutorConditionInput.ATRemoteSKO atRemoteSKO = (generated.v3.dkf.AutoTutorConditionInput.ATRemoteSKO) conditionInput.getScript();
                            		
                            		generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO newATRemoteSKO = new generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO();
                            		
                            		generated.v3.dkf.AutoTutorConditionInput.ATRemoteSKO.URL url = atRemoteSKO.getURL();
                            		
                            		generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO.URL newURL = new generated.v4.dkf.AutoTutorConditionInput.ATRemoteSKO.URL();
                            		newURL.setAddress(url.getAddress());
                            		newATRemoteSKO.setURL(newURL);
                            		
                            		newConditionInput.setScript(newATRemoteSKO);
                            		
                            	}else {
                            		throw new IllegalArgumentException("Found unhandled AutoTutorConditionInput script type of "+conditionInput.getScript());
                            	}

                            }
                            
                            newInput.setType(newConditionInput);
                        	
                        }else if(inputType instanceof generated.v3.dkf.AvoidLocationCondition){
                            
                            generated.v3.dkf.AvoidLocationCondition conditionInput = (generated.v3.dkf.AvoidLocationCondition)inputType;
                            
                            generated.v4.dkf.AvoidLocationCondition newConditionInput = new generated.v4.dkf.AvoidLocationCondition();
                            
                            if(conditionInput.getWaypointRef() != null){                                    
                                newConditionInput.setWaypointRef(convertWaypointRef(conditionInput.getWaypointRef()));
                            }
                                                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v3.dkf.CheckpointPaceCondition){

                            generated.v3.dkf.CheckpointPaceCondition conditionInput = (generated.v3.dkf.CheckpointPaceCondition)inputType;
                            
                            generated.v4.dkf.CheckpointPaceCondition newConditionInput = new generated.v4.dkf.CheckpointPaceCondition();
                            for(generated.v3.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);
                                                            
                        }else if(inputType instanceof generated.v3.dkf.CheckpointProgressCondition){

                            generated.v3.dkf.CheckpointProgressCondition conditionInput = (generated.v3.dkf.CheckpointProgressCondition)inputType;
                            
                            generated.v4.dkf.CheckpointProgressCondition newConditionInput = new generated.v4.dkf.CheckpointProgressCondition();
                            for(generated.v3.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);                                
                            
                        }else if(inputType instanceof generated.v3.dkf.CorridorBoundaryCondition){
                            
                            generated.v3.dkf.CorridorBoundaryCondition conditionInput = (generated.v3.dkf.CorridorBoundaryCondition)inputType;
                            
                            generated.v4.dkf.CorridorBoundaryCondition newConditionInput = new generated.v4.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.CorridorPostureCondition){

                            generated.v3.dkf.CorridorPostureCondition conditionInput = (generated.v3.dkf.CorridorPostureCondition)inputType;
                            
                            generated.v4.dkf.CorridorPostureCondition newConditionInput = new generated.v4.dkf.CorridorPostureCondition();
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                

                            generated.v4.dkf.Postures postures = new generated.v4.dkf.Postures();
                            
                            for(generated.v3.dkf.PostureEnumType posture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(posture.value());
                            }
                            newConditionInput.setPostures(postures);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.EliminateHostilesCondition){

                            generated.v3.dkf.EliminateHostilesCondition conditionInput = (generated.v3.dkf.EliminateHostilesCondition)inputType;
                            
                            generated.v4.dkf.EliminateHostilesCondition newConditionInput = new generated.v4.dkf.EliminateHostilesCondition();
                             
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.EnterAreaCondition){

                            generated.v3.dkf.EnterAreaCondition conditionInput = (generated.v3.dkf.EnterAreaCondition)inputType;
                            
                            generated.v4.dkf.EnterAreaCondition newConditionInput = new generated.v4.dkf.EnterAreaCondition();
                            
                            for(generated.v3.dkf.Entrance entrance : conditionInput.getEntrance()){
                                
                                generated.v4.dkf.Entrance newEntrance = new generated.v4.dkf.Entrance();
                                
                                newEntrance.setAssessment(entrance.getAssessment().value());
                                newEntrance.setName(entrance.getName());
                                
                                generated.v4.dkf.Inside newInside = new generated.v4.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setWaypoint(entrance.getInside().getWaypoint());
                                newEntrance.setInside(newInside);
                                
                                generated.v4.dkf.Outside newOutside = new generated.v4.dkf.Outside();
                                newOutside.setProximity(entrance.getInside().getProximity());
                                newOutside.setWaypoint(entrance.getInside().getWaypoint());
                                newEntrance.setOutside(newOutside);
                                
                                newConditionInput.getEntrance().add(newEntrance);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.ExplosiveHazardSpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v3.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v3.dkf.ExplosiveHazardSpotReportCondition)inputType;
                            
                            generated.v4.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v4.dkf.ExplosiveHazardSpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.GenericConditionInput) {
                        
                        	generated.v3.dkf.GenericConditionInput conditionInput = (generated.v3.dkf.GenericConditionInput)inputType;
                            
                            generated.v4.dkf.GenericConditionInput newConditionInput = new generated.v4.dkf.GenericConditionInput();
                            
                            if(conditionInput.getNvpair() != null){                                    
                                for (Nvpair nvPair : conditionInput.getNvpair()) {
                                	newConditionInput.getNvpair().add(convertNvpair(nvPair));
                                }
                            }
                                                            
                            newInput.setType(newConditionInput);
                            
                    	}else if(inputType instanceof generated.v3.dkf.IdentifyPOIsCondition){

                            generated.v3.dkf.IdentifyPOIsCondition conditionInput = (generated.v3.dkf.IdentifyPOIsCondition)inputType;
                            
                            generated.v4.dkf.IdentifyPOIsCondition newConditionInput = new generated.v4.dkf.IdentifyPOIsCondition();
                            
                            if(conditionInput.getPois() != null){
                                
                                generated.v4.dkf.Pois pois = new generated.v4.dkf.Pois();
                                for(generated.v3.dkf.WaypointRef waypointRef : conditionInput.getPois().getWaypointRef()){
                                    pois.getWaypointRef().add(convertWaypointRef(waypointRef));
                                }
                                
                                newConditionInput.setPois(pois);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.LifeformTargetAccuracyCondition){

                            generated.v3.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v3.dkf.LifeformTargetAccuracyCondition)inputType;
                            
                            generated.v4.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v4.dkf.LifeformTargetAccuracyCondition();
                            
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v3.dkf.MarksmanshipPrecisionCondition) {
                        
                        	generated.v3.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v3.dkf.MarksmanshipPrecisionCondition)inputType;
                        	
                        	generated.v4.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v4.dkf.MarksmanshipPrecisionCondition();
                        	
                        	if(conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                        	
                        	newInput.setType(newConditionInput);
                        	
                    	}else if (inputType instanceof generated.v3.dkf.MarksmanshipSessionCompleteCondition) {
                        
                        	generated.v3.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v3.dkf.MarksmanshipSessionCompleteCondition)inputType;
                        	
                        	generated.v4.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v4.dkf.MarksmanshipSessionCompleteCondition();
                        	
                        	if(conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                        	
                        	newInput.setType(newConditionInput);
                        
                    	}else if(inputType instanceof generated.v3.dkf.NineLineReportCondition){

                            @SuppressWarnings("unused")
                            generated.v3.dkf.NineLineReportCondition conditionInput = (generated.v3.dkf.NineLineReportCondition)inputType;
                            
                            generated.v4.dkf.NineLineReportCondition newConditionInput = new generated.v4.dkf.NineLineReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.NumberOfShotsFiredCondition){
                        	 
                        	generated.v3.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v3.dkf.NumberOfShotsFiredCondition)inputType;
                             
                            generated.v4.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v4.dkf.NumberOfShotsFiredCondition();
                                                             
                            if (conditionInput.getExpectedNumberOfShots() != null) {
                            	newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                            }
                    	
                            newInput.setType(newConditionInput);
                    	
                    	}else if(inputType instanceof generated.v3.dkf.PowerPointDwellCondition){

                            generated.v3.dkf.PowerPointDwellCondition conditionInput = (generated.v3.dkf.PowerPointDwellCondition)inputType;
                            
                            generated.v4.dkf.PowerPointDwellCondition newConditionInput = new generated.v4.dkf.PowerPointDwellCondition();
                            
                            generated.v4.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v4.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);
                            
                            generated.v4.dkf.PowerPointDwellCondition.Slides slides = new generated.v4.dkf.PowerPointDwellCondition.Slides();
                            for(generated.v3.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput.getSlides().getSlide()){
                                
                                generated.v4.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v4.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());
                                
                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.RulesOfEngagementCondition){

                            generated.v3.dkf.RulesOfEngagementCondition conditionInput = (generated.v3.dkf.RulesOfEngagementCondition)inputType;
                            
                            generated.v4.dkf.RulesOfEngagementCondition newConditionInput = new generated.v4.dkf.RulesOfEngagementCondition();
                            generated.v4.dkf.Wcs newWCS = new generated.v4.dkf.Wcs();
                            newWCS.setValue(generated.v4.dkf.WeaponControlStatusEnum.fromValue(conditionInput.getWcs().getValue()));
                            newConditionInput.setWcs(newWCS);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v3.dkf.SIMILEConditionInput){
                        
                        	generated.v3.dkf.SIMILEConditionInput conditionInput = (generated.v3.dkf.SIMILEConditionInput)inputType;
                            
                            generated.v4.dkf.SIMILEConditionInput newConditionInput = new generated.v4.dkf.SIMILEConditionInput();
                        
                            if (conditionInput.getConditionKey() != null) {
                            	newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }
                            
                            if (conditionInput.getConfigurationFile() != null) {
                            	newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                    	}else if(inputType instanceof generated.v3.dkf.SpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v3.dkf.SpotReportCondition conditionInput = (generated.v3.dkf.SpotReportCondition)inputType;
                            
                            generated.v4.dkf.SpotReportCondition newConditionInput = new generated.v4.dkf.SpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v3.dkf.UseRadioCondition){
                            
                            @SuppressWarnings("unused")
                            generated.v3.dkf.UseRadioCondition conditionInput = (generated.v3.dkf.UseRadioCondition)inputType;
                            
                            generated.v4.dkf.UseRadioCondition newConditionInput = new generated.v4.dkf.UseRadioCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled condition input type of "+inputType);
                        }

                    }
                    newCondition.setInput(newInput);
                    
                    //Scoring
                    generated.v4.dkf.Scoring newScoring = new generated.v4.dkf.Scoring();
                    if(condition.getScoring() != null){
                        
                        for(Object scoringType : condition.getScoring().getType()){
                            
                            if(scoringType instanceof generated.v3.dkf.Count){
                                
                                generated.v3.dkf.Count count = (generated.v3.dkf.Count)scoringType;
                                
                                generated.v4.dkf.Count newCount = new generated.v4.dkf.Count();                                    
                                newCount.setName(count.getName());
                                newCount.setUnits(generated.v4.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
                                
                                if(count.getEvaluators() != null){                                        
                                    newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                }
                                
                                newScoring.getType().add(newCount);
                                
                            }else if(scoringType instanceof generated.v3.dkf.CompletionTime){
                                
                                generated.v3.dkf.CompletionTime complTime = (generated.v3.dkf.CompletionTime)scoringType;
                                
                                generated.v4.dkf.CompletionTime newComplTime = new generated.v4.dkf.CompletionTime();
                                newComplTime.setName(complTime.getName());
                                newComplTime.setUnits(generated.v4.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                if(complTime.getEvaluators() != null){                                        
                                    newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                }
                                
                                newScoring.getType().add(newComplTime);
                                
                            }else if(scoringType instanceof generated.v3.dkf.ViolationTime){
                                
                                generated.v3.dkf.ViolationTime violationTime = (generated.v3.dkf.ViolationTime)scoringType;
                                
                                generated.v4.dkf.ViolationTime newViolationTime = new generated.v4.dkf.ViolationTime();
                                newViolationTime.setName(violationTime.getName());
                                newViolationTime.setUnits(generated.v4.dkf.UnitsEnumType.fromValue(violationTime.getUnits()));
                                
                                if(violationTime.getEvaluators() != null){                                        
                                    newViolationTime.setEvaluators(convertEvaluators(violationTime.getEvaluators()));
                                }
                                
                                newScoring.getType().add(newViolationTime);
                                
                            }else{
                                throw new IllegalArgumentException("Found unhandled scoring type of "+scoringType);
                            }
                        }
                    }
                    newCondition.setScoring(newScoring);

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
     * @return generated.v4.dkf.Nvpair - the new object
     */
    private static generated.v4.dkf.Nvpair convertNvpair(generated.v3.dkf.Nvpair nvPair) {
    	
    	generated.v4.dkf.Nvpair newNvpair = new generated.v4.dkf.Nvpair();
    	newNvpair.setName(nvPair.getName());
    	newNvpair.setValue(nvPair.getValue());
    	
    	return newNvpair;
    }
    
    /**
     * Convert a waypointref object to a new waypointref object.
     * 
     * @param waypointRef - the object to convert
     * @return generated.v4.dkf.WaypointRef - the new object
     */
    private static generated.v4.dkf.WaypointRef convertWaypointRef(generated.v3.dkf.WaypointRef waypointRef){
        
        generated.v4.dkf.WaypointRef newWaypoint = new generated.v4.dkf.WaypointRef();
        newWaypoint.setValue(waypointRef.getValue());
        newWaypoint.setDistance(waypointRef.getDistance());
        
        return newWaypoint;
    }
    
    /**
     * Convert an entities object to a new entities object.
     * 
     * @param entities - the object to convert
     * @return generated.v4.dkf.Entities - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v4.dkf.Entities convertEntities(generated.v3.dkf.Entities entities) throws IllegalArgumentException{
        
        generated.v4.dkf.Entities newEntities = new generated.v4.dkf.Entities();
        for(generated.v3.dkf.StartLocation location : entities.getStartLocation()){
            
            generated.v4.dkf.StartLocation newLocation = new generated.v4.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }
        
        return newEntities;
    }
    
    /**
     * Convert a path object into a new path object.
     * 
     * @param path - the object to convert
     * @return generated.v4.dkf.Path - the new object
     */
    private static generated.v4.dkf.Path convertPath(generated.v3.dkf.Path path){
        
        generated.v4.dkf.Path newPath = new generated.v4.dkf.Path();
        for(generated.v3.dkf.Segment segment : path.getSegment()){
            
            generated.v4.dkf.Segment newSegment = new generated.v4.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());
            
            generated.v4.dkf.Start start = new generated.v4.dkf.Start();
            start.setWaypoint(segment.getStart().getWaypoint());
            newSegment.setStart(start);
            
            generated.v4.dkf.End end = new generated.v4.dkf.End();
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
     * @return generated.v4.dkf.Checkpoint - the new object
     */
    private static generated.v4.dkf.Checkpoint convertCheckpoint(generated.v3.dkf.Checkpoint checkpoint){
        
        generated.v4.dkf.Checkpoint newCheckpoint = new generated.v4.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setWaypoint(checkpoint.getWaypoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());
        
        return newCheckpoint;
    }
    
    /**
     * Convert an evaluators object into a new evaluators object.
     * 
     * @param evaluators - the object to convert
     * @return generated.dkf.Evaluators - the new object
     */
    private static generated.v4.dkf.Evaluators convertEvaluators(generated.v3.dkf.Evaluators evaluators){
        
        generated.v4.dkf.Evaluators newEvaluators = new generated.v4.dkf.Evaluators();
        for(generated.v3.dkf.Evaluator evaluator : evaluators.getEvaluator()){
            
            generated.v4.dkf.Evaluator newEvaluator = new generated.v4.dkf.Evaluator();
            newEvaluator.setAssessment(evaluator.getAssessment().value());
            newEvaluator.setValue(evaluator.getValue());                                            
            newEvaluator.setOperator(evaluator.getOperator().value());
            
            newEvaluators.getEvaluator().add(newEvaluator);
        }
        
        return newEvaluators;
    }
    
    /**
     * Convert an assessment object into a new assessment object.
     * 
     * @param assessments - the assessment object to convert
     * @return generated.v4.dkf.Assessments - the new assessment object
     */
    private static generated.v4.dkf.Assessments convertAssessments(generated.v3.dkf.Assessments assessments){
        
        generated.v4.dkf.Assessments newAssessments = new generated.v4.dkf.Assessments();
        
        List<generated.v3.dkf.Assessments.Survey> surveys = new ArrayList<generated.v3.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
        	if (assessmentType instanceof generated.v3.dkf.Assessments.Survey) {
        		surveys.add((generated.v3.dkf.Assessments.Survey) assessmentType);
        	}
        }
        
        for(generated.v3.dkf.Assessments.Survey survey : surveys){
            
            generated.v4.dkf.Assessments.Survey newSurvey = new generated.v4.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());
            
            generated.v4.dkf.Questions newQuestions = new generated.v4.dkf.Questions();
            for(generated.v3.dkf.Question question : survey.getQuestions().getQuestion()){
                
                generated.v4.dkf.Question newQuestion = new generated.v4.dkf.Question();
                newQuestion.setKey(question.getKey());
                
                for(generated.v3.dkf.Reply reply : question.getReply()){
                    
                    generated.v4.dkf.Reply newReply = new generated.v4.dkf.Reply();
                    newReply.setKey(reply.getKey());
                    newReply.setResult(reply.getResult().value());
                    
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
    private static List<Object> convertTriggers(List<Object> triggerObjects) throws IllegalArgumentException{
        
        List<Object> newTriggerObjects = new ArrayList<>();
        for(Object triggerObj : triggerObjects){
            
            if(triggerObj instanceof generated.v3.dkf.EntityLocation){
                
                generated.v3.dkf.EntityLocation entityLocation = (generated.v3.dkf.EntityLocation)triggerObj;
                generated.v4.dkf.EntityLocation newEntityLocation = new generated.v4.dkf.EntityLocation();
                
                generated.v4.dkf.StartLocation startLocation = new generated.v4.dkf.StartLocation();
                startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
                newEntityLocation.setStartLocation(startLocation);
                
                generated.v4.dkf.TriggerLocation triggerLocation = new generated.v4.dkf.TriggerLocation();
                triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
                newEntityLocation.setTriggerLocation(triggerLocation);
                
                newTriggerObjects.add(newEntityLocation);
                
            }else if(triggerObj instanceof generated.v3.dkf.LearnerLocation){
                
                generated.v3.dkf.LearnerLocation learnerLocation = (generated.v3.dkf.LearnerLocation)triggerObj;
                generated.v4.dkf.LearnerLocation newLearnerLocation = new generated.v4.dkf.LearnerLocation();
                
                newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
                
                newTriggerObjects.add(newLearnerLocation);
                
            }else if(triggerObj instanceof generated.v3.dkf.ConceptEnded){
                
                generated.v3.dkf.ConceptEnded conceptEnded = (generated.v3.dkf.ConceptEnded)triggerObj;
                generated.v4.dkf.ConceptEnded newConceptEnded = new generated.v4.dkf.ConceptEnded();
                
                newConceptEnded.setNodeId(conceptEnded.getNodeId());
                
                newTriggerObjects.add(newConceptEnded);
                
            }else if(triggerObj instanceof generated.v3.dkf.ChildConceptEnded){
            	
            	generated.v3.dkf.ChildConceptEnded childConceptEnded = (generated.v3.dkf.ChildConceptEnded)triggerObj;
            	generated.v4.dkf.ChildConceptEnded newChildConceptEnded = new generated.v4.dkf.ChildConceptEnded();
            	
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
     * @return generated.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException
     */
    private static generated.v4.dkf.Coordinate convertCoordinate(generated.v3.dkf.Coordinate coordinate) throws IllegalArgumentException{
        
        generated.v4.dkf.Coordinate newCoord = new generated.v4.dkf.Coordinate();
        
        Object coordType = coordinate.getType();
        if(coordType instanceof generated.v3.dkf.GCC){
            
            generated.v3.dkf.GCC gcc = (generated.v3.dkf.GCC)coordType;
            generated.v4.dkf.GCC newGCC = new generated.v4.dkf.GCC();
            
            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());
            
            newCoord.setType(newGCC);
            
        }else if(coordType instanceof generated.v3.dkf.GDC){
           // generated.v4.
            generated.v3.dkf.GDC gdc = (generated.v3.dkf.GDC)coordType;
            generated.v4.dkf.GDC newGDC = new generated.v4.dkf.GDC();
            
            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());
            
            newCoord.setType(newGDC);
            
        }else if(coordType instanceof generated.v3.dkf.VBS2AGL){
            
            generated.v3.dkf.VBS2AGL agl = (generated.v3.dkf.VBS2AGL)coordType;
            generated.v4.dkf.VBS2AGL newAGL = new generated.v4.dkf.VBS2AGL();
            
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
     * Begin the conversion of a v3.0 file to v4.0 by parsing the old file for the pedagogical config
     * schema object and convert it to a newer version.
     */
    @Override
	public UnmarshalledFile convertEMAPConfiguration(FileProxy pedConfigFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(pedConfigFile, PREV_PEDCONFIG_SCHEMA_FILE, PEDCONFIG_ROOT, failOnFirstSchemaError);
    	generated.v3.ped.EMAP v3PedConfig = (generated.v3.ped.EMAP)uFile.getUnmarshalled();
    	
        return convertPedagogicalConfiguration(v3PedConfig, showCompletionDialog);
    }

    /**
     * Convert the previous pedagogical schema object to a newer version of the course schema.
     * 
     * @param v3PedConfig - current version of the config file 
     * @param showCompletionDialog - show the completion dialog
     * @return - a call to the next version of conversion wizards
     */
    public UnmarshalledFile convertPedagogicalConfiguration(generated.v3.ped.EMAP v3PedConfig, boolean showCompletionDialog) {
    	generated.v4.ped.EM2AP v4PedConfig = new generated.v4.ped.EM2AP();
    	
    	v4PedConfig.setExample(convertExample(v3PedConfig.getExample()));
    	v4PedConfig.setPractice(convertPractice(v3PedConfig.getPractice()));
    	v4PedConfig.setRecall(convertRecall(v3PedConfig.getRecall()));
    	v4PedConfig.setRule(convertRule(v3PedConfig.getRule()));
    	v4PedConfig.setVersion(v3PedConfig.getVersion());
    	
    	ConversionWizardUtil_v4_v2014_2 util = new ConversionWizardUtil_v4_v2014_2();
        util.setConversionIssueList(conversionIssueList);
    	return util.convertPedagogicalConfiguration(v4PedConfig, showCompletionDialog);
	}

    /**
     * Helper function to convert the Rules of a pedagogy config to the next version
     * @param v3Rule - current version of the rules 
     * @return v4 Rules
     */
	private Rule convertRule(generated.v3.ped.Rule v3Rule) {
		generated.v4.ped.Rule newRule = new generated.v4.ped.Rule();
		generated.v4.ped.Attributes newAttributes = new generated.v4.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v3.ped.Attribute attribute : v3Rule.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v4.ped.Attribute());
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
	 * @param v3Recall - current recall version
	 * @return v4 Recall
	 */
	private Recall convertRecall(generated.v3.ped.Recall v3Recall) {
		generated.v4.ped.Recall newRecall = new generated.v4.ped.Recall();
		generated.v4.ped.Attributes newAttributes = new generated.v4.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v3.ped.Attribute attribute : v3Recall.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v4.ped.Attribute());
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
	 * @param v3Practice - current Practice version
	 * @return v4 Practice
	 */
	private Practice convertPractice(generated.v3.ped.Practice v3Practice) {
		generated.v4.ped.Practice newPractice = new generated.v4.ped.Practice();
		generated.v4.ped.Attributes newAttributes = new generated.v4.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v3.ped.Attribute attribute : v3Practice.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v4.ped.Attribute());
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
	 * @param v3Example - current version's Example
	 * @return v4 Example
	 */
	private Example convertExample(generated.v3.ped.Example v3Example) {
		generated.v4.ped.Example newExample = new generated.v4.ped.Example();
		generated.v4.ped.Attributes newAttributes = new generated.v4.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v3.ped.Attribute attribute : v3Example.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v4.ped.Attribute());
			newAttributes.getAttribute().get(index).setType(attribute.getType());
			newAttributes.getAttribute().get(index).setValue(attribute.getValue());
			newAttributes.getAttribute().get(index).setMetadataAttributes(convertMetaDataAttributes(attribute.getMetadataAttributes()));
			index++;
		}
		newExample.setAttributes(newAttributes);
		
		
		return newExample;
	}

	/**
	 * each element to a pedagogy config contains metadata attributes, and can all use this method to convert them
	 * @param v3metadataAttributes - current version's metadata
	 * @return v4 metadata
	 */
	private generated.v4.ped.MetadataAttributes convertMetaDataAttributes(generated.v3.ped.MetadataAttributes v3metadataAttributes) {
		generated.v4.ped.MetadataAttributes newMetaAttributes = new generated.v4.ped.MetadataAttributes(); 
		int index = 0;
		
		for(generated.v3.ped.MetadataAttribute attribute : v3metadataAttributes.getMetadataAttribute()){
			newMetaAttributes.getMetadataAttribute().add(new generated.v4.ped.MetadataAttribute());
			newMetaAttributes.getMetadataAttribute().get(index).setValue(attribute.getValue());
			if(attribute.getIsQuadrantSpecific() != null){
				newMetaAttributes.getMetadataAttribute().get(index).setIsQuadrantSpecific(generated.v4.ped.BooleanEnum.fromValue(attribute.getIsQuadrantSpecific().toString().toLowerCase()));
			}
			index++;
		}
		
		return newMetaAttributes;
	}

	@Override
    public File getPreviousDKFSchemaFile(){
        return PREV_DKF_SCHEMA_FILE;
    }
    
    @Override
    public File getPreviousCourseSchemaFile() {
    	return PREV_COURSE_SCHEMA_FILE;
    }
   
    @Override
    public Class<?> getPreviousDKFSchemaRoot() {
    	return DKF_ROOT;
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
    	/*
        
        try {
            System.out.println("Starting...");
            
            generated.course.Course course = createCourse();
            File courseFile = new File(PREV_COURSE_FOLDER+File.separator+"AllElementsPopulated_v3.course.xml");
            AbstractSchemaHandler.writeToFile(course, COURSE_ROOT, courseFile, PREV_COURSE_SCHEMA_FILE);
            
            System.out.println("Course generated successfully to file named "+courseFile+".");
            
            generated.dkf.Scenario scenario = createScenario();
            File dkf = new File(PREV_DKF_FOLDER+File.separator+"AllElementsPopulated_v3.dkf.xml");
            AbstractSchemaHandler.writeToFile(scenario, DKF_ROOT, dkf, PREV_DKF_SCHEMA_FILE);
            
            System.out.println("Scenario generated successfully to file named "+courseFile+".");
            
            System.out.println("Good-bye.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
