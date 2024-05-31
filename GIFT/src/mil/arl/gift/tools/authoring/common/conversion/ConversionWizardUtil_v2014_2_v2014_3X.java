/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import generated.v5.dkf.Assessments.Survey;
import generated.v5.dkf.Nvpair;

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
 * This class provides logic to migrate GIFT v5.0 XML files to GIFT v6.0 XML files using the appropriate schemas and generated
 * classes.  
 * 
 * Note: the only changes in XML schemas between these two version is at the DKF and Course levels.
 * 
 * @author bzahid
 *
 */
public class ConversionWizardUtil_v2014_2_v2014_3X extends AbstractConversionWizardUtil {
	
	/** course schema info */
    public static final File PREV_COURSE_SCHEMA_FILE = new File(
    		"data"+File.separator+"conversionWizard"+File.separator+"v5"+File.separator+"domain"+File.separator+"course"+File.separator+"course.xsd");
    public static final Class<?> COURSE_ROOT = generated.v5.course.Course.class;
	
    /** dkf schema info */
	public static final File PREV_DKF_SCHEMA_FILE = new File(
			"data"+File.separator+"conversionWizard"+File.separator+"v5"+File.separator+"domain"+File.separator+"dkf"+File.separator+"dkf.xsd");
    public static final Class<?> DKF_ROOT = generated.v5.dkf.Scenario.class;
    
    /** learner config schema info */
    public static final File PREV_LEARNERCONFIG_SCHEMA_FILE = new File(
			"data"+File.separator+"conversionWizard"+File.separator+"v5"+File.separator+"learner"+File.separator+"learnerConfig.xsd");
    public static final Class<?> LEARNERCONFIG_ROOT = generated.v5.learner.LearnerConfiguration.class;
    
    /** pedagogical config schema info */
    public static final File PREV_PEDCONFIG_SCHEMA_FILE = new File(
			"data"+File.separator+"conversionWizard"+File.separator+"v5"+File.separator+"ped"+File.separator+"eMAP.xsd");
    public static final Class<?> PEDCONFIG_ROOT = generated.v5.ped.EMAP.class;
    
    /** metadata schema info */
    public static final File PREV_METADATA_SCHEMA_FILE = new File(
            "data"+File.separator+"conversionWizard"+File.separator+"v5"+File.separator+"domain"+File.separator+"metadata"+File.separator+"metadata.xsd");
    public static final Class<?> METADATA_ROOT = generated.v5.metadata.Metadata.class;
    
    public static final Class<?> TRAINING_APP_ELEMENT_ROOT = generated.v5.course.TrainingApplicationWrapper.class;
    
    
    
    /**
     * Auto-generate a GIFT v5.0 dkf object with every element/attribute instantiated.
     *  
     * @return generated.v5.dkf.Scenario - new 5.0 dkf object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v5.dkf.Scenario createScenario() throws Exception{
    	    	
        Node rootNode = new Node();
        Object obj = createFullInstance(DKF_ROOT, rootNode);
        return (generated.v5.dkf.Scenario)obj;
    }
    
    @Override
    public UnmarshalledFile convertCourse(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile unmarshalledFile = parseFile(courseFile, getPreviousCourseSchemaFile(), getPreviousCourseSchemaRoot(), failOnFirstSchemaError);
    	generated.v5.course.Course v5Course = (generated.v5.course.Course)unmarshalledFile.getUnmarshalled();
    	
    	// Convert the version 5 course to the newest version and return it
    	return convertCourse(v5Course, showCompletionDialog);  
    }
    
    
    /**
     * Convert the previous course schema object to a newer version of the course schema.
     * 
     * @param v5Course - the course schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertCourse(generated.v5.course.Course v5Course, boolean showCompletionDialog) throws IllegalArgumentException{
		
    	generated.v5_1.course.Course newCourse = new generated.v5_1.course.Course();       
 
        //
        // copy over contents from old object to new object
        //    
        newCourse.setName(v5Course.getName());
        newCourse.setVersion(v5Course.getVersion());
        newCourse.setDescription(v5Course.getDescription());
        newCourse.setSurveyContext(v5Course.getSurveyContext());
   
        if (v5Course.getExclude() != null) {
        	newCourse.setExclude(generated.v5_1.course.BooleanEnum.fromValue(v5Course.getExclude().toString().toLowerCase()));
        }        
        
        if(v5Course.getConcepts() != null) {
        	//CONCEPTS
        	
        	generated.v5_1.course.Concepts newConcepts = convertConcepts(v5Course.getConcepts());
        	newCourse.setConcepts(newConcepts);
        }
    	
        //TRANSITIONS
        generated.v5_1.course.Transitions newTransitions = convertTransitions(v5Course.getTransitions());
        newCourse.setTransitions(newTransitions);
        
        ConversionWizardUtil_v2014_3X_v2015_1 util = new ConversionWizardUtil_v2014_3X_v2015_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertCourse(newCourse, showCompletionDialog);
    }
    
    /**
     * Begin the conversion of a v5.0 file to v5.1 by parsing the old file for the learner config
     * schema object and convert it to a newer version.
     */
    @Override
    public UnmarshalledFile convertLearnerConfiguration(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(courseFile, PREV_LEARNERCONFIG_SCHEMA_FILE, LEARNERCONFIG_ROOT, failOnFirstSchemaError);
    	generated.v5.learner.LearnerConfiguration v5LearnerConfig = (generated.v5.learner.LearnerConfiguration)uFile.getUnmarshalled();    	
    	
        return convertLearnerConfiguration(v5LearnerConfig, showCompletionDialog);
    }
    
    /**
     * Convert the previous learner schema object to a newer version of the course schema.
     * 
     * @param v5LearnerConfig - current version of the config file 
     * @param showCompletionDialog - show the completion dialog
     * @return - a call to the next version of conversion wizards
     */
	public UnmarshalledFile convertLearnerConfiguration(generated.v5.learner.LearnerConfiguration v5LearnerConfig, boolean showCompletionDialog) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	generated.v5_1.learner.LearnerConfiguration v5_1LearnerConfig = new generated.v5_1.learner.LearnerConfiguration();
    	v5_1LearnerConfig.setVersion(v5LearnerConfig.getVersion());
    	v5_1LearnerConfig.setInputs(convertInputs(v5LearnerConfig.getInputs()));
    	
    	
    	ConversionWizardUtil_v2014_3X_v2015_1 util = new ConversionWizardUtil_v2014_3X_v2015_1();
        util.setConversionIssueList(conversionIssueList);
    	return util.convertLearnerConfiguration(v5_1LearnerConfig, showCompletionDialog);
    }

	/**
	 * Takes all the inputs to a learner config file and individually converts them to the next version
	 * @param v5Inputs - the previous version of Inputs
	 * @return - the next version up of inputs
	 */
	private generated.v5_1.learner.Inputs convertInputs(generated.v5.learner.Inputs inputs) {
		generated.v5_1.learner.Inputs v5_1Inputs = new generated.v5_1.learner.Inputs();
		int index = 0;
		for(generated.v5.learner.Input input : inputs.getInput()){
			v5_1Inputs.getInput().add(new generated.v5_1.learner.Input());
			
			//create the classifier
			generated.v5_1.learner.Classifier classifier = new generated.v5_1.learner.Classifier(); 
			classifier.setClassifierImpl(input.getClassifier().getClassifierImpl());
			if(input.getClassifier().getProperties() != null){
				classifier.setProperties(convertProperties(input.getClassifier().getProperties()));
			}
			v5_1Inputs.getInput().get(index).setClassifier(classifier);
			
			//create the Predictor
			generated.v5_1.learner.Predictor predictor = new generated.v5_1.learner.Predictor();
			predictor.setPredictorImpl(input.getPredictor().getPredictorImpl());
			v5_1Inputs.getInput().get(index).setPredictor(predictor);
			
			//create and set the producers
			if(input.getProducers() != null){
				v5_1Inputs.getInput().get(index).setProducers(convertProducers(input.getProducers()));
			}
			
			//create the Translator
			generated.v5_1.learner.Translator translator = new generated.v5_1.learner.Translator();
			translator.setTranslatorImpl(input.getTranslator().getTranslatorImpl());
			v5_1Inputs.getInput().get(index).setTranslator(translator);
			
			
			index++;
		}
		
		
		return v5_1Inputs;
	}
	
	/**
     * Helper method to convert the producers in a learner config file to the next version
     * 
     * @param v5producers - current version learner config producers
     * @return - v5.1 producers
     */
	private generated.v5_1.learner.Producers convertProducers(generated.v5.learner.Producers producers) {
		generated.v5_1.learner.Producers v5Producers = new generated.v5_1.learner.Producers();
		int index = 0;
		
		
		for(generated.v5.learner.Producer producer : producers.getProducer()){
			v5Producers.getProducer().add(new generated.v5_1.learner.Producer());
			v5Producers.getProducer().get(index).setSensorType(producer.getSensorType());
			index++;
		}
		return v5Producers;
	}

	/**
	 * Converts the Properties of a classifier from v5 to v5_1 
	 * 
	 * @param v5Properties - properties of the v5 classifier
	 * @return the same properties only in v5_1
	 */
	private generated.v5_1.learner.Properties convertProperties(generated.v5.learner.Properties v5Properties){
		generated.v5_1.learner.Properties v5_1Properties = new generated.v5_1.learner.Properties();
		int index = 0;
		
		for(generated.v5.learner.Property v5Property : v5Properties.getProperty()){
			v5_1Properties.getProperty().add(new generated.v5_1.learner.Property());
			v5_1Properties.getProperty().get(index).setName(v5Property.getName());
			v5_1Properties.getProperty().get(index).setValue(v5Property.getValue());
			index++;
		}
		
		return v5_1Properties;
	}
    
    /**
     * Convert a Transitions course element.
     * 
     * @param transitions - the transitions element to migrate to a newer version
     * @return generated.v5_1.course.Transitions - the converted transitions element
     * @throws IllegalArgumentException
     */
    private static generated.v5_1.course.Transitions convertTransitions(generated.v5.course.Transitions transitions) throws IllegalArgumentException {

    	generated.v5_1.course.Transitions newTransitions = new generated.v5_1.course.Transitions();
    	
        for(Object transitionObj : transitions.getTransitionType()){
            
            if(transitionObj instanceof generated.v5.course.Guidance){
                
                generated.v5.course.Guidance guidance = (generated.v5.course.Guidance)transitionObj;
                generated.v5_1.course.Guidance newGuidance = convertGuidance(guidance);
                
                newTransitions.getTransitionType().add(newGuidance);
                
            }else if(transitionObj instanceof generated.v5.course.PresentSurvey){
                
                generated.v5.course.PresentSurvey presentSurvey = (generated.v5.course.PresentSurvey)transitionObj;
                generated.v5_1.course.PresentSurvey newPresentSurvey = convertPresentSurvey(presentSurvey);

                newTransitions.getTransitionType().add(newPresentSurvey);
                
            }else if(transitionObj instanceof generated.v5.course.AAR){
                
            	generated.v5.course.AAR aar = (generated.v5.course.AAR)transitionObj;           	
            	generated.v5_1.course.AAR newAAR = new generated.v5_1.course.AAR();
            	
            	newAAR.setTransitionName(aar.getTransitionName());
            	
                newTransitions.getTransitionType().add(newAAR);
                
            }else if(transitionObj instanceof generated.v5.course.TrainingApplication){
                
                generated.v5.course.TrainingApplication trainApp = (generated.v5.course.TrainingApplication)transitionObj;
                generated.v5_1.course.TrainingApplication newTrainApp = new generated.v5_1.course.TrainingApplication();
              
                newTrainApp.setTransitionName(trainApp.getTransitionName());
                
                generated.v5_1.course.DkfRef newDkfRef = new generated.v5_1.course.DkfRef();
                newDkfRef.setFile(trainApp.getDkfRef().getFile());
                newTrainApp.setDkfRef(newDkfRef);
                
                newTrainApp.setFinishedWhen(trainApp.getFinishedWhen());
                
                if(trainApp.getGuidance() != null){
                    
                    generated.v5_1.course.Guidance newGuidance = convertGuidance(trainApp.getGuidance());
                    newTrainApp.setGuidance(newGuidance);
                }
                
                if (trainApp.getOptions() != null) {
                	
	                generated.v5.course.TrainingApplication.Options options = trainApp.getOptions();
	                generated.v5_1.course.TrainingApplication.Options newOptions = new generated.v5_1.course.TrainingApplication.Options();
	                
	                if (options.getDisableInstInterImpl() != null) {
	                	
	                	newOptions.setDisableInstInterImpl(generated.v5_1.course.BooleanEnum.fromValue(options.getDisableInstInterImpl().toString().toLowerCase()));
	                }
	                
	                if (options.getShowAvatarInitially() != null) {
	                	
	                	generated.v5_1.course.ShowAvatarInitially newShowAvatarInitially = new generated.v5_1.course.ShowAvatarInitially();
	                	
	                	if (options.getShowAvatarInitially().getAvatarChoice() != null) {
	                		
		                	generated.v5.course.ShowAvatarInitially.MediaSemantics mediaSematics = options.getShowAvatarInitially().getAvatarChoice(); 
		                	generated.v5_1.course.ShowAvatarInitially.MediaSemantics newMediaSematics = new generated.v5_1.course.ShowAvatarInitially.MediaSemantics();
		                	
		                	newMediaSematics.setAvatar(mediaSematics.getAvatar());		                	
		                	newShowAvatarInitially.setAvatarChoice(newMediaSematics);
	                	}
	               	
	                	newOptions.setShowAvatarInitially(newShowAvatarInitially);
	                }
	                
	                newTrainApp.setOptions(newOptions);
                }
                
                generated.v5_1.course.Interops newInterops = new generated.v5_1.course.Interops();               
                newTrainApp.setInterops(newInterops);
                
                for(generated.v5.course.Interop interop : trainApp.getInterops().getInterop()){
                    
                    generated.v5_1.course.Interop newInterop = new generated.v5_1.course.Interop();
                    newInterop.setInteropImpl(interop.getInteropImpl());
                    
                    newInterop.setInteropInputs(new generated.v5_1.course.InteropInputs());
                    
                    Object interopObj = interop.getInteropInputs().getInteropInput();
                    if(interopObj instanceof generated.v5.course.VBS2InteropInputs){
                        
                        generated.v5.course.VBS2InteropInputs vbs2 = (generated.v5.course.VBS2InteropInputs)interopObj;
                        generated.v5_1.course.VBS2InteropInputs newVbs = new generated.v5_1.course.VBS2InteropInputs();
                        
                        generated.v5_1.course.VBS2InteropInputs.LoadArgs newLoadArgs = new generated.v5_1.course.VBS2InteropInputs.LoadArgs();
                        newLoadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                        newVbs.setLoadArgs(newLoadArgs);
                        
                        newInterop.getInteropInputs().setInteropInput(newVbs);
                        
                    }else if(interopObj instanceof generated.v5.course.DISInteropInputs){
                        
                        generated.v5_1.course.DISInteropInputs newDIS = new generated.v5_1.course.DISInteropInputs();
                        newDIS.setLoadArgs(new generated.v5_1.course.DISInteropInputs.LoadArgs());
                        
                        newInterop.getInteropInputs().setInteropInput(newDIS);
                        
                    }else if(interopObj instanceof generated.v5.course.PowerPointInteropInputs){
                        
                        generated.v5.course.PowerPointInteropInputs ppt = (generated.v5.course.PowerPointInteropInputs)interopObj;
                        generated.v5_1.course.PowerPointInteropInputs newPPT = new generated.v5_1.course.PowerPointInteropInputs();
                        
                        newPPT.setLoadArgs(new generated.v5_1.course.PowerPointInteropInputs.LoadArgs());
                        
                        newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                        
                        newInterop.getInteropInputs().setInteropInput(newPPT);
                        
                    }else if(interopObj instanceof generated.v5.course.TC3InteropInputs){
                    	
                    	generated.v5.course.TC3InteropInputs tc3 = (generated.v5.course.TC3InteropInputs)interopObj;
                    	generated.v5_1.course.TC3InteropInputs newTC3 = new generated.v5_1.course.TC3InteropInputs();
                    	
                    	newTC3.setLoadArgs(new generated.v5_1.course.TC3InteropInputs.LoadArgs());
                    	
                    	newTC3.getLoadArgs().setScenarioName(tc3.getLoadArgs().getScenarioName());
                    	
                    	newInterop.getInteropInputs().setInteropInput(newTC3);
                    	
                    }else if(interopObj instanceof generated.v5.course.SCATTInteropInputs){
                    
                    	generated.v5_1.course.SCATTInteropInputs newSCAT = new generated.v5_1.course.SCATTInteropInputs();           	
                    	newSCAT.setLoadArgs(new generated.v5_1.course.SCATTInteropInputs.LoadArgs());
                    	
                    	newInterop.getInteropInputs().setInteropInput(newSCAT);
                    	
                	}else if(interopObj instanceof generated.v5.course.CustomInteropInputs){
                        
                        generated.v5.course.CustomInteropInputs custom = (generated.v5.course.CustomInteropInputs)interopObj;
                        generated.v5_1.course.CustomInteropInputs newCustom = new generated.v5_1.course.CustomInteropInputs();
                        
                        newCustom.setLoadArgs(new generated.v5_1.course.CustomInteropInputs.LoadArgs());
                        
                        for(generated.v5.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                            generated.v5_1.course.Nvpair newPair = new generated.v5_1.course.Nvpair();
                            newPair.setName(pair.getName());
                            newPair.setValue(pair.getValue());
                            newCustom.getLoadArgs().getNvpair().add(newPair);
                        }
                        
                        newInterop.getInteropInputs().setInteropInput(newCustom);
                        
                        
                	}else if(interopObj instanceof generated.v5.course.DETestbedInteropInputs){
                	    
                	    generated.v5.course.DETestbedInteropInputs deTestbed = (generated.v5.course.DETestbedInteropInputs)interopObj;
                	    generated.v5_1.course.DETestbedInteropInputs newDeTestbed = new generated.v5_1.course.DETestbedInteropInputs();
                	    
                	    generated.v5_1.course.DETestbedInteropInputs.LoadArgs args = new generated.v5_1.course.DETestbedInteropInputs.LoadArgs();
                	    args.setScenarioName(deTestbed.getLoadArgs().getScenarioName());
                	    newDeTestbed.setLoadArgs(args);
                        
                        newInterop.getInteropInputs().setInteropInput(newDeTestbed);
                        
                    } else if(interopObj instanceof generated.v5.course.SimpleExampleTAInteropInputs){
                        generated.v5.course.SimpleExampleTAInteropInputs TAInputs = (generated.v5.course.SimpleExampleTAInteropInputs) interopObj;
                        generated.v5_1.course.SimpleExampleTAInteropInputs newTAInputs = new generated.v5_1.course.SimpleExampleTAInteropInputs();
                        generated.v5_1.course.SimpleExampleTAInteropInputs.LoadArgs args = new generated.v5_1.course.SimpleExampleTAInteropInputs.LoadArgs();
                        args.setScenarioName(TAInputs.getLoadArgs().getScenarioName());
                        
                        newTAInputs.setLoadArgs(args);
                        newInterop.getInteropInputs().setInteropInput(newTAInputs);
                    } else {
                        throw new IllegalArgumentException("Found unhandled interop input type of "+interopObj);
                    }
                    
                    newTrainApp.getInterops().getInterop().add(newInterop);
                }
                
                newTransitions.getTransitionType().add(newTrainApp);
                
            }else if(transitionObj instanceof generated.v5.course.LessonMaterial){
                
                generated.v5.course.LessonMaterial lessonMaterial = (generated.v5.course.LessonMaterial)transitionObj;
                generated.v5_1.course.LessonMaterial newLessonMaterial = new generated.v5_1.course.LessonMaterial();
                
                newLessonMaterial.setTransitionName(lessonMaterial.getTransitionName());
                
                if(lessonMaterial.getLessonMaterialList() != null){
                    generated.v5_1.course.LessonMaterialList newLessonMaterialList = new generated.v5_1.course.LessonMaterialList();

                    for(generated.v5.course.Media media : lessonMaterial.getLessonMaterialList().getMedia()){
                        
                        generated.v5_1.course.Media newMedia = new generated.v5_1.course.Media();
                        newMedia.setName(media.getName());
                        newMedia.setUri(media.getUri());
                        
                        Object mediaType = media.getMediaTypeProperties();
                        if(mediaType instanceof generated.v5.course.PDFProperties){
                            newMedia.setMediaTypeProperties(new generated.v5_1.course.PDFProperties());
                            
                        }else if(mediaType instanceof generated.v5.course.WebpageProperties){
                            newMedia.setMediaTypeProperties(new generated.v5_1.course.WebpageProperties());
                            
                        }else if(mediaType instanceof generated.v5.course.YoutubeVideoProperties){
                            
                            generated.v5.course.YoutubeVideoProperties uTubeProp = (generated.v5.course.YoutubeVideoProperties)mediaType;
                            generated.v5_1.course.YoutubeVideoProperties newUTubeProp = new generated.v5_1.course.YoutubeVideoProperties();
                            
                            if (uTubeProp.getAllowFullScreen() != null) {
                            	newUTubeProp.setAllowFullScreen(generated.v5_1.course.BooleanEnum.fromValue(uTubeProp.getAllowFullScreen().toString().toLowerCase()));
                            }
                            if (uTubeProp.getAllowAutoPlay() != null) {
                            	newUTubeProp.setAllowAutoPlay(generated.v5_1.course.BooleanEnum.fromValue(uTubeProp.getAllowAutoPlay().toString().toLowerCase()));
                            }
                            
                            if(uTubeProp.getSize() != null){
                                generated.v5_1.course.Size newSize = new generated.v5_1.course.Size();
                                newSize.setHeight(uTubeProp.getSize().getHeight());
                                newSize.setWidth(uTubeProp.getSize().getWidth());
                                newUTubeProp.setSize(newSize);
                            }

                            newMedia.setMediaTypeProperties(newUTubeProp);
                            
                        }else if(mediaType instanceof generated.v5.course.ImageProperties){
                            newMedia.setMediaTypeProperties(new generated.v5_1.course.ImageProperties());
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled media type of "+mediaType);
                        }
                        
                        newLessonMaterialList.getMedia().add(newMedia);
                    }
                    
                    newLessonMaterial.setLessonMaterialList(newLessonMaterialList);
                }

                if(lessonMaterial.getLessonMaterialFiles() != null){
                    
                    generated.v5_1.course.LessonMaterialFiles newFiles = new generated.v5_1.course.LessonMaterialFiles();
                    for(String file : lessonMaterial.getLessonMaterialFiles().getFile()){
                        newFiles.getFile().add(file);
                    }                    
                    
                    newLessonMaterial.setLessonMaterialFiles(newFiles);                 
                }
                
                newTransitions.getTransitionType().add(newLessonMaterial);
                
            }else if(transitionObj instanceof generated.v5.course.MerrillsBranchPoint){
            	
            	generated.v5.course.MerrillsBranchPoint mBranchPoint = (generated.v5.course.MerrillsBranchPoint)transitionObj;      	
            	generated.v5_1.course.MerrillsBranchPoint newMBranchPoint = new generated.v5_1.course.MerrillsBranchPoint();
            	
            	newMBranchPoint.setTransitionName(mBranchPoint.getTransitionName());
            	
            	generated.v5_1.course.MerrillsBranchPoint.Concepts newConcepts = new generated.v5_1.course.MerrillsBranchPoint.Concepts();
            	newConcepts.getConcept().addAll(mBranchPoint.getConcepts().getConcept());
            	newMBranchPoint.setConcepts(newConcepts);
            	
            	generated.v5_1.course.MerrillsBranchPoint.Quadrants newQuadrants = convertQuadrants(mBranchPoint.getQuadrants());            	
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
     * @return generated.v5_1.course.MerrillsBranchPoint.Quadrants - the converted quadrants element
     */
    private static generated.v5_1.course.MerrillsBranchPoint.Quadrants convertQuadrants(generated.v5.course.MerrillsBranchPoint.Quadrants quadrants) throws IllegalArgumentException {
    	
    	generated.v5_1.course.MerrillsBranchPoint.Quadrants newQuadrants = new generated.v5_1.course.MerrillsBranchPoint.Quadrants();
    	
    	for (Object quadrant : quadrants.getContent()) {
    		if (quadrant instanceof generated.v5.course.Rule) {
    		
    			newQuadrants.getContent().add(new generated.v5_1.course.Rule());
    			
    		}else if (quadrant instanceof generated.v5.course.Practice) {
    			
    			generated.v5.course.Practice practice = (generated.v5.course.Practice)quadrant;
    			generated.v5_1.course.Practice newPractice = new generated.v5_1.course.Practice();
    			generated.v5_1.course.Practice.PracticeConcepts newConcept = new generated.v5_1.course.Practice.PracticeConcepts();
    		    			
    			newConcept.getCourseConcept().addAll(practice.getPracticeConcepts().getCourseConcept());
    		    				
    			newPractice.setPracticeConcepts(newConcept);
    			newPractice.setAllowedAttempts(practice.getAllowedAttempts());
    			
    			newQuadrants.getContent().add(newPractice);
    			
    		}else if (quadrant instanceof generated.v5.course.Recall) {
    			
    			generated.v5.course.Recall recall = (generated.v5.course.Recall)quadrant;
    			
    			generated.v5_1.course.Recall newRecall = new generated.v5_1.course.Recall();
    			
    			// Convert QuestionTypes and AssessmentRules
    			generated.v5_1.course.Recall.PresentSurvey survey = new generated.v5_1.course.Recall.PresentSurvey();
    			
    			survey.setSurveyChoice(new generated.v5_1.course.Recall.PresentSurvey.ConceptSurvey());
    			
    			survey.getSurveyChoice().getConceptQuestions().addAll(
    					convertConceptQuestions(recall.getPresentSurvey().getSurveyChoice().getConceptQuestions()));
    			
    			survey.getSurveyChoice().setGIFTSurveyKey(recall.getPresentSurvey().getSurveyChoice().getGIFTSurveyKey());
    			
    			newRecall.setPresentSurvey(survey);
    			    			
    			// Copy allowed attempts
    			newRecall.setAllowedAttempts(recall.getAllowedAttempts());
    			
    			if(recall.getPresentSurvey().getFullScreen() != null) {
	    			// Copy fullScreen
    				
	    			newRecall.getPresentSurvey().setFullScreen(generated.v5_1.course.BooleanEnum.fromValue(
	    					recall.getPresentSurvey().getFullScreen().toString().toLowerCase()));
    			}
    			
    			newQuadrants.getContent().add(newRecall);
    			
    		}else if (quadrant instanceof generated.v5.course.Example) {
    			
    			newQuadrants.getContent().add(new generated.v5_1.course.Example());
    			
    		}else if (quadrant instanceof generated.v5.course.Transitions) {
    			
    			generated.v5.course.Transitions transitions = (generated.v5.course.Transitions)quadrant;
    			generated.v5_1.course.Transitions newTransitions = convertTransitions(transitions);
    			
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
     * @return generated.v5_1.course.Guidance - the converted guidance element
     */
    private static generated.v5_1.course.Guidance convertGuidance(generated.v5.course.Guidance guidance) throws IllegalArgumentException{
        
        generated.v5_1.course.Guidance newGuidance = new generated.v5_1.course.Guidance();
        
       	newGuidance.setDisplayTime(guidance.getDisplayTime());
       	newGuidance.setTransitionName(guidance.getTransitionName());
        
        if (guidance.getFullScreen() != null) {
        	newGuidance.setFullScreen(generated.v5_1.course.BooleanEnum.fromValue(guidance.getFullScreen().toString().toLowerCase()));
        }
        
        Object guidanceChoice = guidance.getGuidanceChoice();
        if (guidanceChoice instanceof generated.v5.course.Guidance.Message) {

        	generated.v5.course.Guidance.Message message = (generated.v5.course.Guidance.Message)guidanceChoice;
        	
            generated.v5_1.course.Guidance.Message newMessage = new generated.v5_1.course.Guidance.Message();
            newMessage.setContent(message.getContent());
            
            newGuidance.setGuidanceChoice(newMessage);
        	
        }else if (guidanceChoice instanceof generated.v5.course.Guidance.File) {
        	
        	generated.v5.course.Guidance.File file = (generated.v5.course.Guidance.File)guidanceChoice;
        	
        	generated.v5_1.course.Guidance.File newFile = new generated.v5_1.course.Guidance.File();
        	
        	newFile.setHTML(file.getHTML());
        	newFile.setMessage(file.getMessage());
        	
        	newGuidance.setGuidanceChoice(newFile);
        	
        }else if (guidanceChoice instanceof generated.v5.course.Guidance.URL) {
        	
        	generated.v5.course.Guidance.URL url = (generated.v5.course.Guidance.URL)guidanceChoice;
        	
        	generated.v5_1.course.Guidance.URL newURL = new generated.v5_1.course.Guidance.URL();
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
     * @return generated.v5_1.course.PresentSurvey - the converted PresentSurvey element
     */    
    private static generated.v5_1.course.PresentSurvey convertPresentSurvey(generated.v5.course.PresentSurvey presentSurvey) throws IllegalArgumentException{
    	
    	generated.v5_1.course.PresentSurvey newPresentSurvey = new generated.v5_1.course.PresentSurvey();
    	
    	Object surveyChoice = presentSurvey.getSurveyChoice();
    	if (surveyChoice instanceof generated.v5.course.AutoTutorSession) {
    		
    		generated.v5.course.AutoTutorSession autoTutorSession = (generated.v5.course.AutoTutorSession)surveyChoice;
    		
    		generated.v5_1.course.AutoTutorSession newAutoTutorSession = new generated.v5_1.course.AutoTutorSession();
    		
    		generated.v5_1.course.DkfRef newDkfRef = new generated.v5_1.course.DkfRef();
    		
    		newDkfRef.setFile(autoTutorSession.getDkfRef().getFile());
    		newAutoTutorSession.setDkfRef(newDkfRef);
    		
    		newPresentSurvey.setSurveyChoice(newAutoTutorSession);
    		
    	}else if (surveyChoice instanceof generated.v5.course.PresentSurvey.ConceptSurvey ) {
    		
    		generated.v5.course.PresentSurvey.ConceptSurvey conceptSurvey = (generated.v5.course.PresentSurvey.ConceptSurvey)surveyChoice;
    		
    		generated.v5_1.course.PresentSurvey.ConceptSurvey newConceptSurvey = new generated.v5_1.course.PresentSurvey.ConceptSurvey();
    		
    		newConceptSurvey.setGIFTSurveyKey(conceptSurvey.getGIFTSurveyKey());
    		
    		newConceptSurvey.setFullScreen(generated.v5_1.course.BooleanEnum.fromValue(
    				conceptSurvey.getFullScreen().toString().toLowerCase()));
    		
    		newConceptSurvey.setSkipConceptsByExamination(generated.v5_1.course.BooleanEnum.fromValue(
    				conceptSurvey.getSkipConceptsByExamination().toString().toLowerCase()));
    		
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
     * @param concepts - the v5 course concepts
     * @return the converted v5.1 course concepts
     */
    private static generated.v5_1.course.Concepts convertConcepts(generated.v5.course.Concepts concepts) {
    	
    	generated.v5_1.course.Concepts newConcepts = new generated.v5_1.course.Concepts();
    	
    	Object list = concepts.getListOrHierarchy();
    	
    	if(list instanceof generated.v5.course.Concepts.List) {
    		
    		generated.v5_1.course.Concepts.List newList = new generated.v5_1.course.Concepts.List();
    		
    		for(generated.v5.course.Concepts.List.Concept c : ((generated.v5.course.Concepts.List) list).getConcept()) {
    				
	    		generated.v5_1.course.Concepts.List.Concept concept = new generated.v5_1.course.Concepts.List.Concept();
	    		
	    		concept.setName(c.getName());
	    		newList.getConcept().add(concept);
    		}
    		
    		newConcepts.setListOrHierarchy(newList);
    		
    	} else {
    		
    		generated.v5.course.Concepts.Hierarchy hierarchy = (generated.v5.course.Concepts.Hierarchy)list;
    		generated.v5_1.course.Concepts.Hierarchy newList = new generated.v5_1.course.Concepts.Hierarchy();
    		generated.v5_1.course.ConceptNode newSadness = new generated.v5_1.course.ConceptNode();
    		generated.v5.course.ConceptNode sadness = hierarchy.getConceptNode();
    		    		
    		newSadness.setName(sadness.getName());		
    		
    		while(sadness.getConceptNode() != null) {
    			
    			generated.v5_1.course.ConceptNode newNode = new generated.v5_1.course.ConceptNode();
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
     * @param conceptQuestions - the list of v5 conceptQuestions
     * @return the converted list of v5.1 conceptQuestions
     */
    private static List<generated.v5_1.course.ConceptQuestions> convertConceptQuestions(List<generated.v5.course.ConceptQuestions> conceptQuestions) {
    	
    	List<generated.v5_1.course.ConceptQuestions> newConceptQuestions = new ArrayList<generated.v5_1.course.ConceptQuestions>();
    	
    	for(generated.v5.course.ConceptQuestions q : conceptQuestions) {
    		
    		generated.v5_1.course.ConceptQuestions newQuestion = new generated.v5_1.course.ConceptQuestions();
    		generated.v5_1.course.ConceptQuestions.QuestionTypes newTypes = new generated.v5_1.course.ConceptQuestions.QuestionTypes();
    		generated.v5_1.course.ConceptQuestions.AssessmentRules newRules = new generated.v5_1.course.ConceptQuestions.AssessmentRules();
    		generated.v5_1.course.ConceptQuestions.AssessmentRules.AtExpectation atRule = new generated.v5_1.course.ConceptQuestions.AssessmentRules.AtExpectation();
    		generated.v5_1.course.ConceptQuestions.AssessmentRules.AboveExpectation overRule = new generated.v5_1.course.ConceptQuestions.AssessmentRules.AboveExpectation();
    		generated.v5_1.course.ConceptQuestions.AssessmentRules.BelowExpectation underRule = new generated.v5_1.course.ConceptQuestions.AssessmentRules.BelowExpectation();
    		
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
    	generated.v5.dkf.Scenario v5Scenario = (generated.v5.dkf.Scenario)uFile.getUnmarshalled();

        return convertScenario(v5Scenario, showCompletionDialog);
    }
    
    /**
     * Convert the previous scenario schema object to a newer version of the scenario schema.
     * 
     * @param v5Scenario - the scenario schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new scenario
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertScenario(generated.v5.dkf.Scenario v5Scenario, boolean showCompletionDialog) throws IllegalArgumentException {
        	
    	generated.v5_1.dkf.Scenario newScenario = new generated.v5_1.dkf.Scenario();
    	 
    	//
        // copy over contents from old object to new object
        //
        newScenario.setDescription(v5Scenario.getDescription());
        newScenario.setName(v5Scenario.getName());
        
        //
        //Learner Id
        //
        if(v5Scenario.getLearnerId() != null){
            generated.v5_1.dkf.LearnerId newLearnerId = new generated.v5_1.dkf.LearnerId();
            generated.v5_1.dkf.StartLocation newStartLocation = new generated.v5_1.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(v5Scenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }
        
        //
        //Resources
        //
        generated.v5_1.dkf.Resources newResources = new generated.v5_1.dkf.Resources();
        newResources.setSurveyContext(v5Scenario.getResources().getSurveyContext());
        
        generated.v5_1.dkf.AvailableLearnerActions newALA = new generated.v5_1.dkf.AvailableLearnerActions();
        
        if(v5Scenario.getResources().getAvailableLearnerActions() != null){
            
            generated.v5.dkf.AvailableLearnerActions ala = v5Scenario.getResources().getAvailableLearnerActions();
            if(ala.getLearnerActionsFiles() != null){
                generated.v5_1.dkf.LearnerActionsFiles newLAF = new generated.v5_1.dkf.LearnerActionsFiles();
                for(String filename : ala.getLearnerActionsFiles().getFile()){
                    newLAF.getFile().add(filename);
                }
                
                newALA.setLearnerActionsFiles(newLAF);
            }
            
            if(ala.getLearnerActionsList() != null){
                
                generated.v5_1.dkf.LearnerActionsList newLAL = new generated.v5_1.dkf.LearnerActionsList();
                for(generated.v5.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()){
                    
                    generated.v5_1.dkf.LearnerAction newAction = new generated.v5_1.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    newAction.setType(generated.v5_1.dkf.LearnerActionEnumType.fromValue(action.getType().value()));
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
        generated.v5_1.dkf.Assessment newAssessment = new generated.v5_1.dkf.Assessment();
        if(v5Scenario.getAssessment() != null){
            
            generated.v5.dkf.Assessment assessment = v5Scenario.getAssessment();
            
            //
            // Objects
            //
            generated.v5_1.dkf.Objects newObjects = new generated.v5_1.dkf.Objects();
            if(assessment.getObjects() != null){
                
                if(assessment.getObjects().getWaypoints() != null){
                    
                    generated.v5_1.dkf.Waypoints newWaypoints = new generated.v5_1.dkf.Waypoints();
                    
                    generated.v5.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for(generated.v5.dkf.Waypoint waypoint : waypoints.getWaypoint()){
                        
                        generated.v5_1.dkf.Waypoint newWaypoint = new generated.v5_1.dkf.Waypoint();
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
            generated.v5_1.dkf.Tasks newTasks = new generated.v5_1.dkf.Tasks();
            if(assessment.getTasks() != null){
                
                for(generated.v5.dkf.Task task : assessment.getTasks().getTask()){
                    
                    generated.v5_1.dkf.Task newTask = new generated.v5_1.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());
                    
                    // start triggers
                    if(task.getStartTriggers() != null){
                        generated.v5_1.dkf.StartTriggers newStartTriggers = new generated.v5_1.dkf.StartTriggers();
                        newStartTriggers.getTriggers().addAll(convertTriggers(task.getStartTriggers().getTriggers()));
                        newTask.setStartTriggers(newStartTriggers);
                    }
                    
                    // end triggers
                    if(task.getEndTriggers() != null){
                        generated.v5_1.dkf.EndTriggers newEndTriggers = new generated.v5_1.dkf.EndTriggers();
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
        if(v5Scenario.getActions() != null){
            
            generated.v5.dkf.Actions actions = v5Scenario.getActions();
            generated.v5_1.dkf.Actions newActions = new generated.v5_1.dkf.Actions();
            
            //instructional strategies
            if(actions.getInstructionalStrategies() != null){
                
                generated.v5.dkf.Actions.InstructionalStrategies iStrategies = actions.getInstructionalStrategies();
                generated.v5_1.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v5_1.dkf.Actions.InstructionalStrategies();
                
                for(generated.v5.dkf.Strategy strategy : iStrategies.getStrategy()){
                    
                    generated.v5_1.dkf.Strategy newStrategy = new generated.v5_1.dkf.Strategy();
                    newStrategy.setName(strategy.getName());
                    
                    Object strategyType = strategy.getStrategyType();//getValueAttribute();
                    if(strategyType instanceof generated.v5.dkf.PerformanceAssessment){
                        
                        generated.v5.dkf.PerformanceAssessment perfAss = (generated.v5.dkf.PerformanceAssessment)strategyType;
                        
                        generated.v5_1.dkf.PerformanceAssessment newPerfAss = new generated.v5_1.dkf.PerformanceAssessment();
                        newPerfAss.setNodeId(perfAss.getNodeId());
                        newPerfAss.setStrategyHandler(convertStrategyHandler(perfAss.getStrategyHandler()));
                        
                        newStrategy.setStrategyType(newPerfAss);
                        
                    }else if(strategyType instanceof generated.v5.dkf.InstructionalIntervention){
                        
                        generated.v5.dkf.InstructionalIntervention iIntervention = (generated.v5.dkf.InstructionalIntervention)strategyType;
                        
                        generated.v5_1.dkf.InstructionalIntervention newIIntervention = new generated.v5_1.dkf.InstructionalIntervention();
                        newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));

                        for(generated.v5.dkf.Feedback feedback : iIntervention.getInterventionTypes()){
                            
                            generated.v5_1.dkf.Feedback newFeedback = new generated.v5_1.dkf.Feedback();
                           
                            if (feedback.getFeedbackPresentation() instanceof generated.v5.dkf.Feedback.Message) {
                            	
                            	generated.v5.dkf.Feedback.Message message = (generated.v5.dkf.Feedback.Message) feedback.getFeedbackPresentation();
                            	generated.v5_1.dkf.Message feedbackMsg = new generated.v5_1.dkf.Message();
                            	
                            	// Copy the message delivery settings
                            	if(message.getDelivery() != null) {
                            		
                            		generated.v5_1.dkf.Message.Delivery delivery = new generated.v5_1.dkf.Message.Delivery();
                            		
                            		if(message.getDelivery().getInTrainingApplication() != null) {
                            		
                            			generated.v5_1.dkf.Message.Delivery.InTrainingApplication newInApp = new generated.v5_1.dkf.Message.Delivery.InTrainingApplication();
                            			newInApp.setEnabled(generated.v5_1.dkf.BooleanEnum.fromValue(message.getDelivery().getInTrainingApplication().getEnabled().toString().toLowerCase()));
                            			delivery.setInTrainingApplication(newInApp);
                            		}
                            		
                            		if(message.getDelivery().getInTutor() != null) {
                            			
                            			generated.v5_1.dkf.InTutor inTutor = new generated.v5_1.dkf.InTutor();
                            			
                            			inTutor.setMessagePresentation(message.getDelivery().getInTutor().getMessagePresentation());
                            			inTutor.setTextEnhancement(message.getDelivery().getInTutor().getTextEnhancement());
                            			
                            			delivery.setInTutor(inTutor);
                            		}
                            		
                            		feedbackMsg.setDelivery(delivery);
                            	}
                            	
                            	feedbackMsg.setContent(message.getContent());
                            	
                            	newFeedback.setFeedbackPresentation(feedbackMsg);

                            }
                            else if (feedback.getFeedbackPresentation() instanceof generated.v5.dkf.Audio) {
                            	
                            	generated.v5.dkf.Audio audio = (generated.v5.dkf.Audio) feedback.getFeedbackPresentation();
                            	
                            	generated.v5_1.dkf.Audio newAudio = new generated.v5_1.dkf.Audio();
                            	
                            	// An audio object requires a .mp3 file but does not require a .ogg file
                            	newAudio.setMP3File(audio.getMP3File());
                            	
                            	if (audio.getOGGFile() != null) {
                            		newAudio.setOGGFile(audio.getOGGFile());
                            	}
                            	
                            	newFeedback.setFeedbackPresentation(newAudio);
                            	
                            }
                            
                            else if (feedback.getFeedbackPresentation() instanceof generated.v5.dkf.MediaSemantics) {
                            	
                            	generated.v5.dkf.MediaSemantics semantics = (generated.v5.dkf.MediaSemantics) feedback.getFeedbackPresentation();
                            	
                            	generated.v5_1.dkf.MediaSemantics newSemantics = new generated.v5_1.dkf.MediaSemantics();
                            	
                            	// A MediaSematic file requires an avatar and a key name property.
                            	newSemantics.setAvatar(semantics.getAvatar());
                            	newSemantics.setKeyName(semantics.getKeyName());
                            	
                            	newFeedback.setFeedbackPresentation(newSemantics);
                            }
            
                            newFeedback.setAffectiveFeedbackType(feedback.getAffectiveFeedbackType());
                            newFeedback.setFeedbackSpecificityType(feedback.getFeedbackSpecificityType());
                            
                            newIIntervention.getInterventionTypes().add(newFeedback);
                        }
                       
                        newStrategy.setStrategyType(newIIntervention);
                        
                    }else if(strategyType instanceof generated.v5.dkf.ScenarioAdaptation){
                        
                        generated.v5.dkf.ScenarioAdaptation adaptation = (generated.v5.dkf.ScenarioAdaptation)strategyType;
                        
                        generated.v5_1.dkf.ScenarioAdaptation newAdaptation = new generated.v5_1.dkf.ScenarioAdaptation();
                        newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));
                        
                        //only have environment adaptation in this version
                        for(generated.v5.dkf.EnvironmentAdaptation eAdapt : adaptation.getAdaptationTypes()){
                            
                            generated.v5_1.dkf.EnvironmentAdaptation newEAdapt = new generated.v5_1.dkf.EnvironmentAdaptation();
                            
                            generated.v5_1.dkf.EnvironmentAdaptation.Pair newPair = new generated.v5_1.dkf.EnvironmentAdaptation.Pair();
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
               
                generated.v5.dkf.Actions.StateTransitions sTransitions = actions.getStateTransitions();
                generated.v5_1.dkf.Actions.StateTransitions newSTransitions = new generated.v5_1.dkf.Actions.StateTransitions();
                
                for(generated.v5.dkf.Actions.StateTransitions.StateTransition sTransition : sTransitions.getStateTransition()){
                    
                    generated.v5_1.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v5_1.dkf.Actions.StateTransitions.StateTransition();
                    
                    generated.v5_1.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v5_1.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();
                    
                    //State type
                    for (Object stateType : sTransition.getLogicalExpression().getStateType()) {
	                    if(stateType instanceof generated.v5.dkf.LearnerStateTransitionEnum){
	
	                        generated.v5.dkf.LearnerStateTransitionEnum stateEnum = (generated.v5.dkf.LearnerStateTransitionEnum)stateType;
	                        
	                        generated.v5_1.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v5_1.dkf.LearnerStateTransitionEnum();
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
	                        
	                    }else if(stateType instanceof generated.v5.dkf.PerformanceNode){
	                        
	                        generated.v5.dkf.PerformanceNode perfNode = (generated.v5.dkf.PerformanceNode)stateType;
	                        
	                        generated.v5_1.dkf.PerformanceNode newPerfNode = new generated.v5_1.dkf.PerformanceNode();
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
                    generated.v5_1.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v5_1.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    for(generated.v5.dkf.StrategyRef strategyRef : sTransition.getStrategyChoices().getStrategyRef()){
                        
                        generated.v5_1.dkf.StrategyRef newStrategyRef = new generated.v5_1.dkf.StrategyRef();
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
            
        ConversionWizardUtil_v2014_3X_v2015_1 util = new ConversionWizardUtil_v2014_3X_v2015_1();
        util.setConversionIssueList(conversionIssueList);
		return util.convertScenario(newScenario, showCompletionDialog);
    	
    }
    
    
    /**
     * Convert an entities object to a new entities object.
     * 
     * @param entities - the object to convert
     * @return generated.v5_1.v3.dkf.Entities - the new object
     * @throws Exception
     */
    private static generated.v5_1.dkf.Entities convertEntities(generated.v5.dkf.Entities entities) throws IllegalArgumentException{
        
        generated.v5_1.dkf.Entities newEntities = new generated.v5_1.dkf.Entities();
        for(generated.v5.dkf.StartLocation location : entities.getStartLocation()){
            
            generated.v5_1.dkf.StartLocation newLocation = new generated.v5_1.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }
        
        return newEntities;
    }
    
    /**
     * Convert a path object into a new path object.
     * 
     * @param path - the object to convert
     * @return generated.v5_1.v3.dkf.Path - the new object
     */
    private static generated.v5_1.dkf.Path convertPath(generated.v5.dkf.Path path){
        
        generated.v5_1.dkf.Path newPath = new generated.v5_1.dkf.Path();
        for(generated.v5.dkf.Segment segment : path.getSegment()){
            
            generated.v5_1.dkf.Segment newSegment = new generated.v5_1.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());
            
            generated.v5_1.dkf.Start start = new generated.v5_1.dkf.Start();
            start.setWaypoint(segment.getStart().getWaypoint());
            newSegment.setStart(start);
            
            generated.v5_1.dkf.End end = new generated.v5_1.dkf.End();
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
     * @return generated.v5_1.v3.dkf.Checkpoint - the new object
     */
    private static generated.v5_1.dkf.Checkpoint convertCheckpoint(generated.v5.dkf.Checkpoint checkpoint){
        
        generated.v5_1.dkf.Checkpoint newCheckpoint = new generated.v5_1.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setWaypoint(checkpoint.getWaypoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());
        
        return newCheckpoint;
    }
    
    /**
     * Convert an evaluators object into a new evaluators object.
     * 
     * @param evaluators - the object to convert
     * @return generated.v5_1.dkf.Evaluators - the new object
     */
    private static generated.v5_1.dkf.Evaluators convertEvaluators(generated.v5.dkf.Evaluators evaluators){
        
        generated.v5_1.dkf.Evaluators newEvaluators = new generated.v5_1.dkf.Evaluators();
        for(generated.v5.dkf.Evaluator evaluator : evaluators.getEvaluator()){
            
            generated.v5_1.dkf.Evaluator newEvaluator = new generated.v5_1.dkf.Evaluator();
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
     * @return generated.v5_1.dkf.Assessments - the new assessment object
     */
    private static generated.v5_1.dkf.Assessments convertAssessments(generated.v5.dkf.Assessments assessments){
        
        generated.v5_1.dkf.Assessments newAssessments = new generated.v5_1.dkf.Assessments();
        
        List<generated.v5.dkf.Assessments.Survey> surveys = new ArrayList<generated.v5.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
        	if (assessmentType instanceof generated.v5.dkf.Assessments.Survey) {
        		surveys.add((Survey) assessmentType);
        	}
        }
        for(generated.v5.dkf.Assessments.Survey survey : surveys){
            
            generated.v5_1.dkf.Assessments.Survey newSurvey = new generated.v5_1.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());
            
            generated.v5_1.dkf.Questions newQuestions = new generated.v5_1.dkf.Questions();
            for(generated.v5.dkf.Question question : survey.getQuestions().getQuestion()){
                
                generated.v5_1.dkf.Question newQuestion = new generated.v5_1.dkf.Question();
                newQuestion.setKey(question.getKey());
                
                for(generated.v5.dkf.Reply reply : question.getReply()){
                    
                    generated.v5_1.dkf.Reply newReply = new generated.v5_1.dkf.Reply();
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
     * @param list - collection of trigger objects to convert
     * @return List<Serializable> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<Serializable> convertTriggers(List<Serializable> list) throws IllegalArgumentException{
        
        List<Serializable> newTriggerObjects = new ArrayList<>();
        for(Object triggerObj : list){
            
            if(triggerObj instanceof generated.v5.dkf.EntityLocation){
                
                generated.v5.dkf.EntityLocation entityLocation = (generated.v5.dkf.EntityLocation)triggerObj;
                generated.v5_1.dkf.EntityLocation newEntityLocation = new generated.v5_1.dkf.EntityLocation();
                
                generated.v5_1.dkf.StartLocation startLocation = new generated.v5_1.dkf.StartLocation();
                startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
                newEntityLocation.setStartLocation(startLocation);
                
                generated.v5_1.dkf.TriggerLocation triggerLocation = new generated.v5_1.dkf.TriggerLocation();
                triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
                newEntityLocation.setTriggerLocation(triggerLocation);
                
                newTriggerObjects.add(newEntityLocation);
                
            }else if(triggerObj instanceof generated.v5.dkf.LearnerLocation){
                
                generated.v5.dkf.LearnerLocation learnerLocation = (generated.v5.dkf.LearnerLocation)triggerObj;
                generated.v5_1.dkf.LearnerLocation newLearnerLocation = new generated.v5_1.dkf.LearnerLocation();
                
                newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
                
                newTriggerObjects.add(newLearnerLocation);
                
            }else if(triggerObj instanceof generated.v5.dkf.ConceptEnded){
                
                generated.v5.dkf.ConceptEnded conceptEnded = (generated.v5.dkf.ConceptEnded)triggerObj;
                generated.v5_1.dkf.ConceptEnded newConceptEnded = new generated.v5_1.dkf.ConceptEnded();
                
                newConceptEnded.setNodeId(conceptEnded.getNodeId());
                
                newTriggerObjects.add(newConceptEnded);
                
            }else if(triggerObj instanceof generated.v5.dkf.ChildConceptEnded){
            	
            	generated.v5.dkf.ChildConceptEnded childConceptEnded = (generated.v5.dkf.ChildConceptEnded)triggerObj;
            	generated.v5_1.dkf.ChildConceptEnded newChildConceptEnded = new generated.v5_1.dkf.ChildConceptEnded();
            	
            	newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());
            	
            	newTriggerObjects.add(newChildConceptEnded);
            	
            } else if(triggerObj instanceof generated.v5.dkf.TaskEnded){
            	
            	generated.v5.dkf.TaskEnded taskEnded = (generated.v5.dkf.TaskEnded)triggerObj;
            	generated.v5_1.dkf.TaskEnded newTaskEnded = new generated.v5_1.dkf.TaskEnded();
            	
            	newTaskEnded.setNodeId(taskEnded.getNodeId());
            	
            	newTriggerObjects.add(newTaskEnded);
            	
            } else if(triggerObj instanceof generated.v5.dkf.ConceptAssessment){
            	
            	generated.v5.dkf.ConceptAssessment conceptAssessment = (generated.v5.dkf.ConceptAssessment)triggerObj;
            	generated.v5_1.dkf.ConceptAssessment newConceptAssessment = new generated.v5_1.dkf.ConceptAssessment();
            	
            	newConceptAssessment.setResult(conceptAssessment.getResult());
            	newConceptAssessment.setConcept(conceptAssessment.getConcept());
            	
            	newTriggerObjects.add(newConceptAssessment);
            	
            } else{
                throw new IllegalArgumentException("Found unhandled trigger type of "+triggerObj);
            }
        }
        
        return newTriggerObjects;
    }
    
    /**
     * Convert a coordinate object into the latest schema version.
     * 
     * @param coordinate - v5.0 coordinate object to convert
     * @return generated.v5_1.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException
     */
    private static generated.v5_1.dkf.Coordinate convertCoordinate(generated.v5.dkf.Coordinate coordinate) throws IllegalArgumentException{
        
        generated.v5_1.dkf.Coordinate newCoord = new generated.v5_1.dkf.Coordinate();
        
        Object coordType = coordinate.getType();
        if(coordType instanceof generated.v5.dkf.GCC){
            
            generated.v5.dkf.GCC gcc = (generated.v5.dkf.GCC)coordType;
            generated.v5_1.dkf.GCC newGCC = new generated.v5_1.dkf.GCC();
            
            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());
            
            newCoord.setType(newGCC);
            
        }else if(coordType instanceof generated.v5.dkf.GDC){
           // generated.v5.
            generated.v5.dkf.GDC gdc = (generated.v5.dkf.GDC)coordType;
            generated.v5_1.dkf.GDC newGDC = new generated.v5_1.dkf.GDC();
            
            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());
            
            newCoord.setType(newGDC);
            
        }else if(coordType instanceof generated.v5.dkf.VBS2AGL){
            
            generated.v5.dkf.VBS2AGL agl = (generated.v5.dkf.VBS2AGL)coordType;
            generated.v5_1.dkf.VBS2AGL newAGL = new generated.v5_1.dkf.VBS2AGL();
            
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
     * @return generated.v5_1.dkf.StrategyHandler - the new object
     */
    private static generated.v5_1.dkf.StrategyHandler convertStrategyHandler(generated.v5.dkf.StrategyHandler handler){
        
        generated.v5_1.dkf.StrategyHandler newHandler = new generated.v5_1.dkf.StrategyHandler();
        
        if(handler.getParams() != null) {
        	
        	generated.v5_1.dkf.StrategyHandler.Params newParams = new generated.v5_1.dkf.StrategyHandler.Params();
        	generated.v5_1.dkf.Nvpair nvpair = new generated.v5_1.dkf.Nvpair();
        	
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
     * @return generated.v5_1.dkf.Concepts - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v5_1.dkf.Concepts convertConcepts(generated.v5.dkf.Concepts concepts) throws IllegalArgumentException{

        generated.v5_1.dkf.Concepts newConcepts = new generated.v5_1.dkf.Concepts();
        for(generated.v5.dkf.Concept concept : concepts.getConcept()){
            
            generated.v5_1.dkf.Concept newConcept = new generated.v5_1.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());
            
            if (concept.getAssessments() != null) {
            	newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }
            
            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if(conditionsOrConcepts instanceof generated.v5.dkf.Concepts){
                //nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v5.dkf.Concepts)conditionsOrConcepts));
                
            }else if(conditionsOrConcepts instanceof generated.v5.dkf.Conditions){
            	
                generated.v5_1.dkf.Conditions newConditions = new generated.v5_1.dkf.Conditions();
                
                generated.v5.dkf.Conditions conditions = (generated.v5.dkf.Conditions)conditionsOrConcepts;
                
                for(generated.v5.dkf.Condition condition : conditions.getCondition()){

                    generated.v5_1.dkf.Condition newCondition = new generated.v5_1.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());                        
                    
                    if(condition.getDefault() != null){
                        generated.v5_1.dkf.Default newDefault = new generated.v5_1.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }                            
                    
                    //Input
                    generated.v5_1.dkf.Input newInput = new generated.v5_1.dkf.Input();
                    if(condition.getInput() != null){
                    	
                        Object inputType = condition.getInput().getType();
                        
                        if(inputType instanceof generated.v5.dkf.ApplicationCompletedCondition){
                            
                            generated.v5.dkf.ApplicationCompletedCondition conditionInput = (generated.v5.dkf.ApplicationCompletedCondition)inputType;
                            
                            generated.v5_1.dkf.ApplicationCompletedCondition newConditionInput = new generated.v5_1.dkf.ApplicationCompletedCondition();
                                
                            if (conditionInput.getIdealCompletionDuration() != null) {
                            	newConditionInput.setIdealCompletionDuration(conditionInput.getIdealCompletionDuration());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if (inputType instanceof generated.v5.dkf.AutoTutorConditionInput){

                        	generated.v5.dkf.AutoTutorConditionInput conditionInput = (generated.v5.dkf.AutoTutorConditionInput)inputType;
                            
                            generated.v5_1.dkf.AutoTutorConditionInput newConditionInput = new generated.v5_1.dkf.AutoTutorConditionInput();

                            if (conditionInput.getScript() != null) {
                            	
                            	if (conditionInput.getScript() instanceof generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO) {
                            		
                            		generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO atRemoteSKO = (generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO) conditionInput.getScript();
                            		
                            		generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO newATRemoteSKO = new generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO();
                            		
                            		generated.v5.dkf.AutoTutorConditionInput.ATRemoteSKO.URL url = atRemoteSKO.getURL();
                            		
                            		generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO.URL newURL = new generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO.URL();
                            		newURL.setAddress(url.getAddress());
                            		newATRemoteSKO.setURL(newURL);
                            		
                            		newConditionInput.setScript(newATRemoteSKO);
                            		
                            	} else if (conditionInput.getScript() instanceof generated.v5.dkf.AutoTutorConditionInput.LocalSKO) {

                            		generated.v5.dkf.AutoTutorConditionInput.LocalSKO localSKO = (generated.v5.dkf.AutoTutorConditionInput.LocalSKO) conditionInput.getScript();
                            		generated.v5_1.dkf.AutoTutorConditionInput.LocalSKO newLocalSKO = new generated.v5_1.dkf.AutoTutorConditionInput.LocalSKO();
                            		
                            		newLocalSKO.setFile(localSKO.getFile());
                            		newConditionInput.setScript(newLocalSKO);
                            		
                            	} else {
                            		throw new IllegalArgumentException("Found unhandled AutoTutorConditionInput script type of "+conditionInput.getScript());
                            	}

                            }
                            
                            newInput.setType(newConditionInput);
                        	
                        }else if(inputType instanceof generated.v5.dkf.AvoidLocationCondition){
                            
                            generated.v5.dkf.AvoidLocationCondition conditionInput = (generated.v5.dkf.AvoidLocationCondition)inputType;
                            
                            generated.v5_1.dkf.AvoidLocationCondition newConditionInput = new generated.v5_1.dkf.AvoidLocationCondition();
                            
                            if(conditionInput.getWaypointRef() != null){                                    
                                newConditionInput.setWaypointRef(convertWaypointRef(conditionInput.getWaypointRef()));
                            }
                                                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v5.dkf.CheckpointPaceCondition){

                            generated.v5.dkf.CheckpointPaceCondition conditionInput = (generated.v5.dkf.CheckpointPaceCondition)inputType;
                            
                            generated.v5_1.dkf.CheckpointPaceCondition newConditionInput = new generated.v5_1.dkf.CheckpointPaceCondition();
                            for(generated.v5.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);
                                                            
                        }else if(inputType instanceof generated.v5.dkf.CheckpointProgressCondition){

                            generated.v5.dkf.CheckpointProgressCondition conditionInput = (generated.v5.dkf.CheckpointProgressCondition)inputType;
                            
                            generated.v5_1.dkf.CheckpointProgressCondition newConditionInput = new generated.v5_1.dkf.CheckpointProgressCondition();
                            for(generated.v5.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);                                
                            
                        }else if(inputType instanceof generated.v5.dkf.CorridorBoundaryCondition){
                            
                            generated.v5.dkf.CorridorBoundaryCondition conditionInput = (generated.v5.dkf.CorridorBoundaryCondition)inputType;
                            
                            generated.v5_1.dkf.CorridorBoundaryCondition newConditionInput = new generated.v5_1.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.CorridorPostureCondition){

                            generated.v5.dkf.CorridorPostureCondition conditionInput = (generated.v5.dkf.CorridorPostureCondition)inputType;
                            
                            generated.v5_1.dkf.CorridorPostureCondition newConditionInput = new generated.v5_1.dkf.CorridorPostureCondition();
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                

                            generated.v5_1.dkf.Postures postures = new generated.v5_1.dkf.Postures();
                            /*
                            for(generated.v5.dkf.PostureEnumType posture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(generated.v5_1.dkf.PostureEnumType.fromValue(posture.value()));
                            }
                            */
                            for(String strPosture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.EliminateHostilesCondition){

                            generated.v5.dkf.EliminateHostilesCondition conditionInput = (generated.v5.dkf.EliminateHostilesCondition)inputType;
                            
                            generated.v5_1.dkf.EliminateHostilesCondition newConditionInput = new generated.v5_1.dkf.EliminateHostilesCondition();
                             
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.EnterAreaCondition){

                            generated.v5.dkf.EnterAreaCondition conditionInput = (generated.v5.dkf.EnterAreaCondition)inputType;
                            
                            generated.v5_1.dkf.EnterAreaCondition newConditionInput = new generated.v5_1.dkf.EnterAreaCondition();
                            
                            for(generated.v5.dkf.Entrance entrance : conditionInput.getEntrance()){
                                
                                generated.v5_1.dkf.Entrance newEntrance = new generated.v5_1.dkf.Entrance();
                                
                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());
                                
                                generated.v5_1.dkf.Inside newInside = new generated.v5_1.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setWaypoint(entrance.getInside().getWaypoint());
                                newEntrance.setInside(newInside);
                                
                                generated.v5_1.dkf.Outside newOutside = new generated.v5_1.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setWaypoint(entrance.getOutside().getWaypoint());
                                newEntrance.setOutside(newOutside);
                                
                                newConditionInput.getEntrance().add(newEntrance);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.ExplosiveHazardSpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v5.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v5.dkf.ExplosiveHazardSpotReportCondition)inputType;
                            
                            generated.v5_1.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v5_1.dkf.ExplosiveHazardSpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.GenericConditionInput) {
                        	
	                        generated.v5.dkf.GenericConditionInput conditionInput = (generated.v5.dkf.GenericConditionInput)inputType;
	                        
	                        generated.v5_1.dkf.GenericConditionInput newConditionInput = new generated.v5_1.dkf.GenericConditionInput();
	                        
	                        if(conditionInput.getNvpair() != null){                                    
	                            for (Nvpair nvPair : conditionInput.getNvpair()) {
	                            	newConditionInput.getNvpair().add(convertNvpair(nvPair));
	                            }
	                        }
	                                                        
	                        newInput.setType(newConditionInput);
                        
                    	}else if(inputType instanceof generated.v5.dkf.HasMovedExcavatorComponentInput){

                    		generated.v5.dkf.HasMovedExcavatorComponentInput conditionInput = (generated.v5.dkf.HasMovedExcavatorComponentInput)inputType;
                    		generated.v5_1.dkf.HasMovedExcavatorComponentInput newConditionInput = new generated.v5_1.dkf.HasMovedExcavatorComponentInput();
                    		
                    		// Element changed in this version. The user will have to set new parameters, 
                    		// but at least copy over the desired component.
                    		generated.v5_1.dkf.HasMovedExcavatorComponentInput.Component c = new generated.v5_1.dkf.HasMovedExcavatorComponentInput.Component();
                    		c.setComponentType(generated.v5_1.dkf.ExcavatorComponentEnum.fromValue(conditionInput.getComponent().toString().toLowerCase()));
                    		
                    		newConditionInput.getComponent().add(c);
                    		
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.IdentifyPOIsCondition){

                            generated.v5.dkf.IdentifyPOIsCondition conditionInput = (generated.v5.dkf.IdentifyPOIsCondition)inputType;
                            
                            generated.v5_1.dkf.IdentifyPOIsCondition newConditionInput = new generated.v5_1.dkf.IdentifyPOIsCondition();
                            
                            if(conditionInput.getPois() != null){
                                
                                generated.v5_1.dkf.Pois pois = new generated.v5_1.dkf.Pois();
                                for(generated.v5.dkf.WaypointRef waypointRef : conditionInput.getPois().getWaypointRef()){
                                    pois.getWaypointRef().add(convertWaypointRef(waypointRef));
                                }
                                
                                newConditionInput.setPois(pois);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.LifeformTargetAccuracyCondition){

                            generated.v5.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v5.dkf.LifeformTargetAccuracyCondition)inputType;
                            
                            generated.v5_1.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v5_1.dkf.LifeformTargetAccuracyCondition();
                            
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v5.dkf.MarksmanshipPrecisionCondition) {
                        
                        	generated.v5.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v5.dkf.MarksmanshipPrecisionCondition)inputType;
                        	
                        	generated.v5_1.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v5_1.dkf.MarksmanshipPrecisionCondition();
                        	
                        	if(conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                        	
                        	newInput.setType(newConditionInput);
                        	
                    	}else if (inputType instanceof generated.v5.dkf.MarksmanshipSessionCompleteCondition) {
                        
                        	generated.v5.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v5.dkf.MarksmanshipSessionCompleteCondition)inputType;
                        	
                        	generated.v5_1.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v5_1.dkf.MarksmanshipSessionCompleteCondition();
                        	
                        	if(conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                        	
                        	newInput.setType(newConditionInput);
                        
                    	}else if(inputType instanceof generated.v5.dkf.NineLineReportCondition){

                            @SuppressWarnings("unused")
                            generated.v5.dkf.NineLineReportCondition conditionInput = (generated.v5.dkf.NineLineReportCondition)inputType;
                            
                            generated.v5_1.dkf.NineLineReportCondition newConditionInput = new generated.v5_1.dkf.NineLineReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.NumberOfShotsFiredCondition){
                       	 
                        	generated.v5.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v5.dkf.NumberOfShotsFiredCondition)inputType;
                            
                        	generated.v5_1.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v5_1.dkf.NumberOfShotsFiredCondition();
                                                            
                        	if (conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                   	
                        	newInput.setType(newConditionInput);
                   	
                        }else if(inputType instanceof generated.v5.dkf.PowerPointDwellCondition){

                            generated.v5.dkf.PowerPointDwellCondition conditionInput = (generated.v5.dkf.PowerPointDwellCondition)inputType;
                            
                            generated.v5_1.dkf.PowerPointDwellCondition newConditionInput = new generated.v5_1.dkf.PowerPointDwellCondition();
                            
                            generated.v5_1.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v5_1.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);
                            
                            generated.v5_1.dkf.PowerPointDwellCondition.Slides slides = new generated.v5_1.dkf.PowerPointDwellCondition.Slides();
                            for(generated.v5.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput.getSlides().getSlide()){
                                
                                generated.v5_1.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v5_1.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());
                                
                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.RulesOfEngagementCondition){

                            generated.v5.dkf.RulesOfEngagementCondition conditionInput = (generated.v5.dkf.RulesOfEngagementCondition)inputType;
                            
                            generated.v5_1.dkf.RulesOfEngagementCondition newConditionInput = new generated.v5_1.dkf.RulesOfEngagementCondition();
                            generated.v5_1.dkf.Wcs newWCS = new generated.v5_1.dkf.Wcs();
                            newWCS.setValue(generated.v5_1.dkf.WeaponControlStatusEnum.fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v5.dkf.SIMILEConditionInput){
                        
                        	generated.v5.dkf.SIMILEConditionInput conditionInput = (generated.v5.dkf.SIMILEConditionInput)inputType;
                            
                            generated.v5_1.dkf.SIMILEConditionInput newConditionInput = new generated.v5_1.dkf.SIMILEConditionInput();
                        
                            if (conditionInput.getConditionKey() != null) {
                            	newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }
                            
                            if (conditionInput.getConfigurationFile() != null) {
                            	newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                    	}else if(inputType instanceof generated.v5.dkf.SpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v5.dkf.SpotReportCondition conditionInput = (generated.v5.dkf.SpotReportCondition)inputType;
                            
                            generated.v5_1.dkf.SpotReportCondition newConditionInput = new generated.v5_1.dkf.SpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.TimerConditionInput){

                            generated.v5.dkf.TimerConditionInput conditionInput = (generated.v5.dkf.TimerConditionInput)inputType;
                            
                            generated.v5_1.dkf.TimerConditionInput newConditionInput = new generated.v5_1.dkf.TimerConditionInput();
                            
                            newConditionInput.setRepeatable(generated.v5_1.dkf.BooleanEnum.fromValue(conditionInput.getRepeatable().toString().toLowerCase()));
                            newConditionInput.setInterval(conditionInput.getInterval());
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5.dkf.UseRadioCondition){
                            
                            @SuppressWarnings("unused")
                            generated.v5.dkf.UseRadioCondition conditionInput = (generated.v5.dkf.UseRadioCondition)inputType;
                            
                            generated.v5_1.dkf.UseRadioCondition newConditionInput = new generated.v5_1.dkf.UseRadioCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled condition input type of "+inputType);
                        }

                    }
                    newCondition.setInput(newInput);
                    
                    //Scoring
                    generated.v5_1.dkf.Scoring newScoring = new generated.v5_1.dkf.Scoring();
                    if(condition.getScoring() != null){
                    	// Only add the scoring element if it has children. 
                    	// As of version 5, there cannot be a scoring element with no children
                    	if (!condition.getScoring().getType().isEmpty()) {
                    		
                        	for(Object scoringType : condition.getScoring().getType()){
                                
                                if(scoringType instanceof generated.v5.dkf.Count){
                                    
                                    generated.v5.dkf.Count count = (generated.v5.dkf.Count)scoringType;
                                    
                                    generated.v5_1.dkf.Count newCount = new generated.v5_1.dkf.Count();                                    
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v5_1.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
                                    
                                    if(count.getEvaluators() != null){                                        
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newCount);
                                    
                                }else if(scoringType instanceof generated.v5.dkf.CompletionTime){
                                    
                                    generated.v5.dkf.CompletionTime complTime = (generated.v5.dkf.CompletionTime)scoringType;
                                    
                                    generated.v5_1.dkf.CompletionTime newComplTime = new generated.v5_1.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(generated.v5_1.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if(complTime.getEvaluators() != null){                                        
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newComplTime);
                                    
                                }else if(scoringType instanceof generated.v5.dkf.ViolationTime){
                                    
                                    generated.v5.dkf.ViolationTime violationTime = (generated.v5.dkf.ViolationTime)scoringType;
                                    
                                    generated.v5_1.dkf.ViolationTime newViolationTime = new generated.v5_1.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(generated.v5_1.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
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
     * @return generated.v5_1.dkf.Nvpair - the new object
     */
    private static generated.v5_1.dkf.Nvpair convertNvpair(generated.v5.dkf.Nvpair nvPair) {
    	
    	generated.v5_1.dkf.Nvpair newNvpair = new generated.v5_1.dkf.Nvpair();
    	newNvpair.setName(nvPair.getName());
    	newNvpair.setValue(nvPair.getValue());
    	
    	return newNvpair;
    }
    
    /**
     * Convert a waypointref object to a new waypointref object.
     * 
     * @param waypointRef - the object to convert
     * @return generated.v5_1.dkf.WaypointRef - the new object
     */
    private static generated.v5_1.dkf.WaypointRef convertWaypointRef(generated.v5.dkf.WaypointRef waypointRef){
        
        generated.v5_1.dkf.WaypointRef newWaypoint = new generated.v5_1.dkf.WaypointRef();
        newWaypoint.setValue(waypointRef.getValue());
        newWaypoint.setDistance(waypointRef.getDistance());
        
        return newWaypoint;
    }
    
    /**
     * Begin the conversion of a v5.0 file to v5.1 by parsing the old file for the pedagogical config
     * schema object and convert it to a newer version.
     */
    @Override
	public UnmarshalledFile convertEMAPConfiguration(FileProxy pedConfigFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(pedConfigFile, PREV_PEDCONFIG_SCHEMA_FILE, PEDCONFIG_ROOT, failOnFirstSchemaError);
    	generated.v5.ped.EMAP v5PedConfig = (generated.v5.ped.EMAP)uFile.getUnmarshalled();
    	
        return convertPedagogicalConfiguration(v5PedConfig, showCompletionDialog);
    }

    /**
     * Convert the previous pedagogical schema object to a newer version of the course schema.
     * 
     * @param v5PedConfig - current version of the config file 
     * @param showCompletionDialog - show the completion dialog
     * @return - a call to the next version of conversion wizards
     */
    public UnmarshalledFile convertPedagogicalConfiguration(generated.v5.ped.EMAP v5PedConfig, boolean showCompletionDialog) {
    	generated.v5_1.ped.EMAP v5_1PedConfig = new generated.v5_1.ped.EMAP();
    	
    	v5_1PedConfig.setExample(convertExample(v5PedConfig.getExample()));
    	v5_1PedConfig.setPractice(convertPractice(v5PedConfig.getPractice()));
    	v5_1PedConfig.setRecall(convertRecall(v5PedConfig.getRecall()));
    	v5_1PedConfig.setRule(convertRule(v5PedConfig.getRule()));
    	v5_1PedConfig.setVersion(v5PedConfig.getVersion());
    	
    	ConversionWizardUtil_v2014_3X_v2015_1 util = new ConversionWizardUtil_v2014_3X_v2015_1();
        util.setConversionIssueList(conversionIssueList);
    	return util.convertPedagogicalConfiguration(v5_1PedConfig, showCompletionDialog);
	}

    /**
     * Helper function to convert the Rules of a pedagogy config to the next version
     * @param v5Rule - current version of the rules 
     * @return v5.1 Rules
     */
	private generated.v5_1.ped.Rule convertRule(generated.v5.ped.Rule v5Rule) {
		generated.v5_1.ped.Rule newRule = new generated.v5_1.ped.Rule();
		generated.v5_1.ped.Attributes newAttributes = new generated.v5_1.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5.ped.Attribute attribute : v5Rule.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v5_1.ped.Attribute());
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
     * @param v5Recall - current version of the recall 
     * @return v5.1 Recall
     */
	private generated.v5_1.ped.Recall convertRecall(generated.v5.ped.Recall v5Recall) {
		generated.v5_1.ped.Recall newRecall = new generated.v5_1.ped.Recall();
		generated.v5_1.ped.Attributes newAttributes = new generated.v5_1.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5.ped.Attribute attribute : v5Recall.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v5_1.ped.Attribute());
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
     * @param v5Practice - current version of the practice 
     * @return v5.1 Practice
     */
	private generated.v5_1.ped.Practice convertPractice(generated.v5.ped.Practice v5Practice) {
		generated.v5_1.ped.Practice newPractice = new generated.v5_1.ped.Practice();
		generated.v5_1.ped.Attributes newAttributes = new generated.v5_1.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5.ped.Attribute attribute : v5Practice.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v5_1.ped.Attribute());
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
     * @param v5Example - current version of the example 
     * @return v5.1 Example
     */
	private generated.v5_1.ped.Example convertExample(generated.v5.ped.Example v5Example) {
		generated.v5_1.ped.Example newExample = new generated.v5_1.ped.Example();
		generated.v5_1.ped.Attributes newAttributes = new generated.v5_1.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5.ped.Attribute attribute : v5Example.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v5_1.ped.Attribute());
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
	 * @param v5metadataAttributes - current version's metadata
	 * @return v5.1 metadata
	 */
	private generated.v5_1.ped.MetadataAttributes convertMetaDataAttributes(generated.v5.ped.MetadataAttributes v5metadataAttributes) {
		generated.v5_1.ped.MetadataAttributes newMetaAttributes = new generated.v5_1.ped.MetadataAttributes(); 
		int index = 0;
		
		for(generated.v5.ped.MetadataAttribute attribute : v5metadataAttributes.getMetadataAttribute()){
			newMetaAttributes.getMetadataAttribute().add(new generated.v5_1.ped.MetadataAttribute());
			newMetaAttributes.getMetadataAttribute().get(index).setValue(attribute.getValue());
			if(attribute.getIsQuadrantSpecific() != null){
				newMetaAttributes.getMetadataAttribute().get(index).setIsQuadrantSpecific(generated.v5_1.ped.BooleanEnum.fromValue(attribute.getIsQuadrantSpecific().toString().toLowerCase()));
			}
			index++;
		}
		
		return newMetaAttributes;
	}

    @Override
    public UnmarshalledFile convertMetadata(FileProxy metadataFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(metadataFile, getPreviousMetadataSchemaFile(), getPreviousMetadataSchemaRoot(), failOnFirstSchemaError);
        generated.v5.metadata.Metadata v5metadata = (generated.v5.metadata.Metadata)uFile.getUnmarshalled();
        
        // Convert the version 5 course to the newest version and return it
        return convertMetadata(v5metadata, showCompletionDialog);  
    }
	
    /**
     * Convert the previous metadata schema object to a newer version of the metadata schema.
     * 
     * @param oldMetadata - the metadata schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
	private UnmarshalledFile convertMetadata(generated.v5.metadata.Metadata oldMetadata, boolean showCompletionDialog){
        generated.v5_1.metadata.Metadata newMetadata = new generated.v5_1.metadata.Metadata();
        
        newMetadata.setVersion(oldMetadata.getVersion());
        newMetadata.setMerrillQuadrant(oldMetadata.getMerrillQuadrant());
        newMetadata.setSimpleRef(oldMetadata.getSimpleRef());
        newMetadata.setTrainingAppRef(oldMetadata.getTrainingAppRef());
        newMetadata.setConcepts(convertConcepts(oldMetadata.getConcepts()));
        
        ConversionWizardUtil_v2014_3X_v2015_1 util = new ConversionWizardUtil_v2014_3X_v2015_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertMetadata(newMetadata, showCompletionDialog);
    }
    
    private static generated.v5_1.metadata.Metadata.Concepts convertConcepts(generated.v5.metadata.Metadata.Concepts oldConcepts) {
        generated.v5_1.metadata.Metadata.Concepts newConcepts = new generated.v5_1.metadata.Metadata.Concepts();
        
        for(generated.v5.metadata.Concept concept: oldConcepts.getConcept()){
            generated.v5_1.metadata.Concept newConcept = new generated.v5_1.metadata.Concept();
            newConcept.setAttributes(convertMetaDataAttributes(concept.getAttributes()));
            newConcept.setName(concept.getName());
            newConcepts.getConcept().add(newConcept);
        }
        
        return newConcepts;
    }
    
    private static generated.v5_1.metadata.Attributes convertMetaDataAttributes(generated.v5.metadata.Attributes oldAttributes) {
        generated.v5_1.metadata.Attributes newMetaAttributes = new generated.v5_1.metadata.Attributes();
        int index = 0;
        
        for(generated.v5.metadata.Attribute attribute : oldAttributes.getAttribute()){
            newMetaAttributes.getAttribute().add(new generated.v5_1.metadata.Attribute());
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
    
    @Override
    public UnmarshalledFile convertTrainingApplicationRef(FileProxy trainingAppFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(trainingAppFile, getPreviousCourseSchemaFile(), getPreviousTrainingAppSchemaRoot(), failOnFirstSchemaError);
        generated.v5.course.TrainingApplicationWrapper v5trainingApp = (generated.v5.course.TrainingApplicationWrapper)uFile.getUnmarshalled();
        
        // Convert the version 5 course to the newest version and return it
        return convertTrainingApplicationRef(v5trainingApp, showCompletionDialog);  
    }
    
    /**
     * Convert the previous TrainingAppRef schema object to a newer version of the TrainingAppRef schema.
     * 
     * @param v5trainingAppRef - the TrainingAppRef schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     */
    public UnmarshalledFile convertTrainingApplicationRef(generated.v5.course.TrainingApplicationWrapper v5trainingAppRef, boolean showCompletionDialog){
        generated.v5_1.course.TrainingApplicationWrapper newTrainingAppRef = new generated.v5_1.course.TrainingApplicationWrapper();
        newTrainingAppRef.setDescription(v5trainingAppRef.getDescription());
        newTrainingAppRef.setVersion(v5trainingAppRef.getVersion());
        newTrainingAppRef.setTrainingApplication(convertTrainingApplication(v5trainingAppRef.getTrainingApplication()));
        
        
        ConversionWizardUtil_v2014_3X_v2015_1 util = new ConversionWizardUtil_v2014_3X_v2015_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertTrainingApplicationRef(newTrainingAppRef, showCompletionDialog);
    }
    
    private static generated.v5_1.course.TrainingApplication convertTrainingApplication(generated.v5.course.TrainingApplication trainApp){
        generated.v5_1.course.TrainingApplication newTrainApp = new generated.v5_1.course.TrainingApplication();
      
        newTrainApp.setTransitionName(trainApp.getTransitionName());
        
        generated.v5_1.course.DkfRef newDkfRef = new generated.v5_1.course.DkfRef();
        newDkfRef.setFile(trainApp.getDkfRef().getFile());
        newTrainApp.setDkfRef(newDkfRef);
        
        newTrainApp.setFinishedWhen(trainApp.getFinishedWhen());
        
        if(trainApp.getGuidance() != null){
            
            generated.v5_1.course.Guidance newGuidance = convertGuidance(trainApp.getGuidance());
            newTrainApp.setGuidance(newGuidance);
        }
        
        if (trainApp.getOptions() != null) {
            
            newTrainApp.setOptions(convertOptions(trainApp.getOptions()));
            
        }
        
        generated.v5_1.course.Interops newInterops = new generated.v5_1.course.Interops();               
        newTrainApp.setInterops(newInterops);
        
        for(generated.v5.course.Interop interop : trainApp.getInterops().getInterop()){
            
            generated.v5_1.course.Interop newInterop = new generated.v5_1.course.Interop();
            
            newInterop.setInteropImpl(interop.getInteropImpl());
                                
            newInterop.setInteropInputs(new generated.v5_1.course.InteropInputs());
            
            Object interopObj = interop.getInteropInputs().getInteropInput();
            if(interopObj instanceof generated.v5.course.VBS2InteropInputs){
                
                generated.v5.course.VBS2InteropInputs vbs2 = (generated.v5.course.VBS2InteropInputs)interopObj;
                generated.v5_1.course.VBS2InteropInputs newVbs = new generated.v5_1.course.VBS2InteropInputs();
                
                generated.v5_1.course.VBS2InteropInputs.LoadArgs newLoadArgs = new generated.v5_1.course.VBS2InteropInputs.LoadArgs();
                newLoadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                newVbs.setLoadArgs(newLoadArgs);
                
                newInterop.getInteropInputs().setInteropInput(newVbs);
                
            }else if(interopObj instanceof generated.v5.course.DISInteropInputs){
                
                generated.v5_1.course.DISInteropInputs newDIS = new generated.v5_1.course.DISInteropInputs();
                newDIS.setLoadArgs(new generated.v5_1.course.DISInteropInputs.LoadArgs());
                
                newInterop.getInteropInputs().setInteropInput(newDIS);
                
            }else if(interopObj instanceof generated.v5.course.PowerPointInteropInputs){
                
                generated.v5.course.PowerPointInteropInputs ppt = (generated.v5.course.PowerPointInteropInputs)interopObj;
                generated.v5_1.course.PowerPointInteropInputs newPPT = new generated.v5_1.course.PowerPointInteropInputs();
                
                newPPT.setLoadArgs(new generated.v5_1.course.PowerPointInteropInputs.LoadArgs());
                
                newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                
                newInterop.getInteropInputs().setInteropInput(newPPT);
                
            }else if(interopObj instanceof generated.v5.course.TC3InteropInputs){
                
                generated.v5.course.TC3InteropInputs tc3 = (generated.v5.course.TC3InteropInputs)interopObj;
                generated.v5_1.course.TC3InteropInputs newTC3 = new generated.v5_1.course.TC3InteropInputs();
                
                newTC3.setLoadArgs(new generated.v5_1.course.TC3InteropInputs.LoadArgs());
                
                newTC3.getLoadArgs().setScenarioName(tc3.getLoadArgs().getScenarioName());
                
                newInterop.getInteropInputs().setInteropInput(newTC3);
                
            }else if(interopObj instanceof generated.v5.course.SCATTInteropInputs){
            
                generated.v5_1.course.SCATTInteropInputs newSCAT = new generated.v5_1.course.SCATTInteropInputs();            
                newSCAT.setLoadArgs(new generated.v5_1.course.SCATTInteropInputs.LoadArgs());
                
                newInterop.getInteropInputs().setInteropInput(newSCAT);
                
            }else if(interopObj instanceof generated.v5.course.CustomInteropInputs){
                
                generated.v5.course.CustomInteropInputs custom = (generated.v5.course.CustomInteropInputs)interopObj;
                generated.v5_1.course.CustomInteropInputs newCustom = new generated.v5_1.course.CustomInteropInputs();
                
                newCustom.setLoadArgs(new generated.v5_1.course.CustomInteropInputs.LoadArgs());
                
                for(generated.v5.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                    generated.v5_1.course.Nvpair newPair = new generated.v5_1.course.Nvpair();
                    newPair.setName(pair.getName());
                    newPair.setValue(pair.getValue());
                    newCustom.getLoadArgs().getNvpair().add(newPair);
                }
                
                newInterop.getInteropInputs().setInteropInput(newCustom);
                
                
            }else if(interopObj instanceof generated.v5.course.DETestbedInteropInputs){
                
                generated.v5.course.DETestbedInteropInputs deTestbed = (generated.v5.course.DETestbedInteropInputs)interopObj;
                generated.v5_1.course.DETestbedInteropInputs newDeTestbed = new generated.v5_1.course.DETestbedInteropInputs();
                
                generated.v5_1.course.DETestbedInteropInputs.LoadArgs args = new generated.v5_1.course.DETestbedInteropInputs.LoadArgs();
                args.setScenarioName(deTestbed.getLoadArgs().getScenarioName());
                newDeTestbed.setLoadArgs(args);
                
                newInterop.getInteropInputs().setInteropInput(newDeTestbed);
                
            } else if(interopObj instanceof generated.v5.course.SimpleExampleTAInteropInputs){
                generated.v5.course.SimpleExampleTAInteropInputs TAInputs = (generated.v5.course.SimpleExampleTAInteropInputs) interopObj;
                generated.v5_1.course.SimpleExampleTAInteropInputs newTAInputs = new generated.v5_1.course.SimpleExampleTAInteropInputs();
                generated.v5_1.course.SimpleExampleTAInteropInputs.LoadArgs args = new generated.v5_1.course.SimpleExampleTAInteropInputs.LoadArgs();
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
    
    private static generated.v5_1.course.TrainingApplication.Options convertOptions(generated.v5.course.TrainingApplication.Options oldOptions){
        generated.v5_1.course.TrainingApplication.Options newOptions = new generated.v5_1.course.TrainingApplication.Options();
        
        if (oldOptions.getDisableInstInterImpl() != null) {
            
            newOptions.setDisableInstInterImpl(generated.v5_1.course.BooleanEnum.fromValue(oldOptions.getDisableInstInterImpl().toString().toLowerCase()));
        }
        
        if (oldOptions.getShowAvatarInitially() != null) {
            
            generated.v5_1.course.ShowAvatarInitially newShowAvatarInitially = new generated.v5_1.course.ShowAvatarInitially();
            
            if (oldOptions.getShowAvatarInitially().getAvatarChoice() != null) {
                
                generated.v5.course.ShowAvatarInitially.MediaSemantics mediaSematics = oldOptions.getShowAvatarInitially().getAvatarChoice(); 
                generated.v5_1.course.ShowAvatarInitially.MediaSemantics newMediaSematics = new generated.v5_1.course.ShowAvatarInitially.MediaSemantics();
                
                newMediaSematics.setAvatar(mediaSematics.getAvatar());                          
                newShowAvatarInitially.setAvatarChoice(newMediaSematics);
            }
        
            newOptions.setShowAvatarInitially(newShowAvatarInitially);
        }
        
        return newOptions;
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
    
    @Override
    public Class<?> getPreviousTrainingAppSchemaRoot() {
        return TRAINING_APP_ELEMENT_ROOT;
    }
    
    @Override
    public File getPreviousMetadataSchemaFile() {
        return PREV_METADATA_SCHEMA_FILE;
    }
    
    @Override
    public Class<?> getPreviousMetadataSchemaRoot() {
        return METADATA_ROOT;
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
            generated.v5.dkf.Scenario scenario = createScenario();
            
            System.out.println("Scenario generated successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
