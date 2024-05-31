/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import generated.v5_1.dkf.Assessments.Survey;
import generated.v5_1.dkf.Nvpair;

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
 * This class provides logic to migrate GIFT v2014-3X XML files to GIFT v2015-1 XML files using the appropriate schemas and generated
 * classes.  
 * 
 * Note: the only changes in XML schemas between these two version is at the DKF and Course levels.
 * 
 * @author bzahid
 *
 */
public class ConversionWizardUtil_v2014_3X_v2015_1 extends AbstractConversionWizardUtil {

	/** course schema info */
    public static final File PREV_COURSE_SCHEMA_FILE = new File(
    		"data"+File.separator+"conversionWizard"+File.separator+"v2014_3X"+File.separator+"domain"+File.separator+"course"+File.separator+"course.xsd");
    public static final Class<?> COURSE_ROOT = generated.v5_1.course.Course.class;
	
    /** dkf schema info */
	public static final File PREV_DKF_SCHEMA_FILE = new File(
			"data"+File.separator+"conversionWizard"+File.separator+"v2014_3X"+File.separator+"domain"+File.separator+"dkf"+File.separator+"dkf.xsd");
    public static final Class<?> DKF_ROOT = generated.v5_1.dkf.Scenario.class;
    
    /** learner config schema info */
    public static final File PREV_LEARNERCONFIG_SCHEMA_FILE = new File(
			"data"+File.separator+"conversionWizard"+File.separator+"v2014_3X"+File.separator+"learner"+File.separator+"learnerConfig.xsd");
    public static final Class<?> LEARNERCONFIG_ROOT = generated.v5_1.learner.LearnerConfiguration.class;
    
    /** pedagogical config schema info */
    public static final File PREV_PEDCONFIG_SCHEMA_FILE = new File(
			"data"+File.separator+"conversionWizard"+File.separator+"v2014_3X"+File.separator+"ped"+File.separator+"eMAP.xsd");
    public static final Class<?> PEDCONFIG_ROOT = generated.v5_1.ped.EMAP.class;
    
    public static final Class<?> TRAINING_APP_ELEMENT_ROOT = generated.v5_1.course.TrainingApplicationWrapper.class;
    
    /** metadata schema info */
    public static final File PREV_METADATA_SCHEMA_FILE = new File(
            "data"+File.separator+"conversionWizard"+File.separator+"v2014_3X"+File.separator+"domain"+File.separator+"metadata"+File.separator+"metadata.xsd");
    public static final Class<?> METADATA_ROOT = generated.v5_1.metadata.Metadata.class;
    
    private static final String OLD_VBS_INTEROP = "gateway.interop.vbs2plugin.VBS2PluginInterface";
    private static final String NEW_VBS_INTEROP = "gateway.interop.vbsplugin.VBSPluginInterface";
    
    /**
     * Auto-generate a GIFT v5.0 dkf object with every element/attribute instantiated.
     *  
     * @return generated.v5_1.dkf.Scenario - new 5.0 dkf object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v5_1.dkf.Scenario createScenario() throws Exception{
    	    	
        Node rootNode = new Node();
        Object obj = createFullInstance(DKF_ROOT, rootNode);
        return (generated.v5_1.dkf.Scenario)obj;
    }
    
    @Override
    public UnmarshalledFile convertCourse(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(courseFile, getPreviousCourseSchemaFile(), getPreviousCourseSchemaRoot(), failOnFirstSchemaError);
    	generated.v5_1.course.Course v5Course = (generated.v5_1.course.Course)uFile.getUnmarshalled();
    	
    	// Convert the version 5 course to the newest version and return it
    	return convertCourse(v5Course, showCompletionDialog);  
    }
    
    
    /**
     * Convert the previous course schema object to a newer version of the course schema.
     * 
     * @param v5_1Course - the course schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertCourse(generated.v5_1.course.Course v5_1Course, boolean showCompletionDialog) throws IllegalArgumentException{
		
    	generated.v6.course.Course newCourse = new generated.v6.course.Course();       
        
        //
        // copy over contents from old object to new object
        //    
        newCourse.setName(v5_1Course.getName());
        newCourse.setVersion(v5_1Course.getVersion());
        newCourse.setDescription(v5_1Course.getDescription());
        newCourse.setSurveyContext(v5_1Course.getSurveyContext());
   
        if (v5_1Course.getExclude() != null) {
        	newCourse.setExclude(generated.v6.course.BooleanEnum.fromValue(v5_1Course.getExclude().toString().toLowerCase()));
        }
        
        if(v5_1Course.getConcepts() != null) {
        	//CONCEPTS
        	
        	generated.v6.course.Concepts newConcepts = convertConcepts(v5_1Course.getConcepts());
        	newCourse.setConcepts(newConcepts);
        }
        
        //TRANSITIONS
        generated.v6.course.Transitions newTransitions = convertTransitions(v5_1Course.getTransitions());
        newCourse.setTransitions(newTransitions);

        ConversionWizardUtil_v2015_1_v2016_1 util = new ConversionWizardUtil_v2015_1_v2016_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertCourse(newCourse, showCompletionDialog);
    }
    
    /**
     * Begin the conversion of a v5.1 file to current version by parsing the old file for the learner config
     * schema object and convert it to a newer version.
     */
    @Override
    public UnmarshalledFile convertLearnerConfiguration(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(courseFile, PREV_LEARNERCONFIG_SCHEMA_FILE, LEARNERCONFIG_ROOT, failOnFirstSchemaError);
    	generated.v5_1.learner.LearnerConfiguration newLearnerConfig = (generated.v5_1.learner.LearnerConfiguration)uFile.getUnmarshalled();    	
    	
        return convertLearnerConfiguration(newLearnerConfig, showCompletionDialog);
    }
    
    /**
     * Convert the previous learner schema object to a newer version of the course schema.
     * 
     * @param v5_1LearnerConfig - version of the config file to be converted 
     * @param showCompletionDialog - show the completion dialog
     * @return - the freshly converted config file
     */
	public UnmarshalledFile convertLearnerConfiguration(generated.v5_1.learner.LearnerConfiguration v5_1LearnerConfig, boolean showCompletionDialog) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	generated.v6.learner.LearnerConfiguration newLearnerConfig = new generated.v6.learner.LearnerConfiguration();
    	newLearnerConfig.setVersion(v5_1LearnerConfig.getVersion());
    	newLearnerConfig.setInputs(convertInputs(v5_1LearnerConfig.getInputs()));
    	
    	
    	ConversionWizardUtil_v2015_1_v2016_1 util = new ConversionWizardUtil_v2015_1_v2016_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertLearnerConfiguration(newLearnerConfig, showCompletionDialog);
	}

	/**
	 * Takes all the inputs to a learner config file and individually converts them to the next version
	 * @param v5_1Inputs - the previous version of Inputs
	 * @return - the next version up of inputs
	 */
	private generated.v6.learner.Inputs convertInputs(generated.v5_1.learner.Inputs inputs) {
		generated.v6.learner.Inputs newInputs = new generated.v6.learner.Inputs();
		int index = 0;
		
		for(generated.v5_1.learner.Input input : inputs.getInput()){
			newInputs.getInput().add(new generated.v6.learner.Input());
			
			//create the classifier
			generated.v6.learner.Classifier classifier = new generated.v6.learner.Classifier(); 
			classifier.setClassifierImpl(input.getClassifier().getClassifierImpl());
			if(input.getClassifier().getProperties() != null){
				classifier.setProperties(convertProperties(input.getClassifier().getProperties()));
			}
			newInputs.getInput().get(index).setClassifier(classifier);
			
			//create the Predictor
			generated.v6.learner.Predictor predictor = new generated.v6.learner.Predictor();
			predictor.setPredictorImpl(input.getPredictor().getPredictorImpl());
			newInputs.getInput().get(index).setPredictor(predictor);
			
			//create and set the producers
			if(input.getProducers() != null){
				newInputs.getInput().get(index).setProducers(convertProducers(input.getProducers()));
			}
			
			//create the Translator
			generated.v6.learner.Translator translator = new generated.v6.learner.Translator();
			translator.setTranslatorImpl(input.getTranslator().getTranslatorImpl());
			newInputs.getInput().get(index).setTranslator(translator);
			
			
			index++;
		}
		
		
		return newInputs;
	}
	
	/**
     * Helper method to convert the producers in a learner config file to the next version
     * 
     * @param v5_1producers - current version learner config producers
     * @return - v6.0 producers
     */
	private generated.v6.learner.Producers convertProducers(generated.v5_1.learner.Producers v5_1Producers) {
		generated.v6.learner.Producers newProducers = new generated.v6.learner.Producers();
		int index = 0;
		
		
		for(generated.v5_1.learner.Producer producer : v5_1Producers.getProducer()){
			if(producer.getSensorType() != null && !producer.getSensorType().isEmpty()){
				newProducers.getProducerType().add(new generated.v6.learner.Sensor());
				((generated.v6.learner.Sensor) newProducers.getProducerType().get(index)).setType(producer.getSensorType());
			}
			index++;
		}
		return newProducers;
	}

	/**
	 * Converts the Properties of a classifier from v5_1 to v6 
	 * 
	 * @param v5Properties - properties of the v5 classifier
	 * @return the same properties only in v5_1
	 */
	private generated.v6.learner.Properties convertProperties(generated.v5_1.learner.Properties v5_1Properties){
		generated.v6.learner.Properties newProperties = new generated.v6.learner.Properties();
		int index = 0;
		
		for(generated.v5_1.learner.Property v5_1Property : v5_1Properties.getProperty()){
			newProperties.getProperty().add(new generated.v6.learner.Property());
			newProperties.getProperty().get(index).setName(v5_1Property.getName());
			newProperties.getProperty().get(index).setValue(v5_1Property.getValue());
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
    private static generated.v6.course.Transitions convertTransitions(generated.v5_1.course.Transitions transitions) throws IllegalArgumentException {

    	generated.v6.course.Transitions newTransitions = new generated.v6.course.Transitions();
    	
        for(Object transitionObj : transitions.getTransitionType()){
            
            if(transitionObj instanceof generated.v5_1.course.Guidance){
                
                generated.v5_1.course.Guidance guidance = (generated.v5_1.course.Guidance)transitionObj;
                generated.v6.course.Guidance newGuidance = convertGuidance(guidance);
                
                newTransitions.getTransitionType().add(newGuidance);
                
            }else if(transitionObj instanceof generated.v5_1.course.PresentSurvey){
                
                generated.v5_1.course.PresentSurvey presentSurvey = (generated.v5_1.course.PresentSurvey)transitionObj;
                generated.v6.course.PresentSurvey newPresentSurvey = convertPresentSurvey(presentSurvey);

                newTransitions.getTransitionType().add(newPresentSurvey);
                
            }else if(transitionObj instanceof generated.v5_1.course.AAR){
                
            	generated.v5_1.course.AAR aar = (generated.v5_1.course.AAR)transitionObj;           	
            	generated.v6.course.AAR newAAR = new generated.v6.course.AAR();
            	
            	newAAR.setTransitionName(aar.getTransitionName());
            	
                newTransitions.getTransitionType().add(newAAR);
                
            }else if(transitionObj instanceof generated.v5_1.course.TrainingApplication){
                
                generated.v5_1.course.TrainingApplication trainApp = (generated.v5_1.course.TrainingApplication)transitionObj;
                generated.v6.course.TrainingApplication newTrainApp = new generated.v6.course.TrainingApplication();
              
                newTrainApp.setTransitionName(trainApp.getTransitionName());
                
                generated.v6.course.DkfRef newDkfRef = new generated.v6.course.DkfRef();
                newDkfRef.setFile(trainApp.getDkfRef().getFile());
                newTrainApp.setDkfRef(newDkfRef);
                
                newTrainApp.setFinishedWhen(trainApp.getFinishedWhen());
                
                if(trainApp.getGuidance() != null){
                    
                    generated.v6.course.Guidance newGuidance = convertGuidance(trainApp.getGuidance());
                    newTrainApp.setGuidance(newGuidance);
                }
                
                if (trainApp.getOptions() != null) {
                	
	                generated.v5_1.course.TrainingApplication.Options options = trainApp.getOptions();
	                generated.v6.course.TrainingApplication.Options newOptions = new generated.v6.course.TrainingApplication.Options();
	                
	                if (options.getDisableInstInterImpl() != null) {
	                	
	                	newOptions.setDisableInstInterImpl(generated.v6.course.BooleanEnum.fromValue(options.getDisableInstInterImpl().toString().toLowerCase()));
	                }
	                
	                if (options.getShowAvatarInitially() != null) {
	                	
	                	generated.v6.course.ShowAvatarInitially newShowAvatarInitially = new generated.v6.course.ShowAvatarInitially();
	                	
	                	if (options.getShowAvatarInitially().getAvatarChoice() != null) {
	                		
		                	generated.v5_1.course.ShowAvatarInitially.MediaSemantics mediaSematics = options.getShowAvatarInitially().getAvatarChoice(); 
		                	generated.v6.course.ShowAvatarInitially.MediaSemantics newMediaSematics = new generated.v6.course.ShowAvatarInitially.MediaSemantics();
		                	
		                	newMediaSematics.setAvatar(mediaSematics.getAvatar());		                	
		                	newShowAvatarInitially.setAvatarChoice(newMediaSematics);
	                	}
	               	
	                	newOptions.setShowAvatarInitially(newShowAvatarInitially);
	                }
	                
	                newTrainApp.setOptions(newOptions);
                }
                
                generated.v6.course.Interops newInterops = new generated.v6.course.Interops();               
                newTrainApp.setInterops(newInterops);
                
                for(generated.v5_1.course.Interop interop : trainApp.getInterops().getInterop()){
                    
                    generated.v6.course.Interop newInterop = new generated.v6.course.Interop();
                    
                    // Check to make sure the old vbs interop isn't being referenced since it has been replaced
                    if(interop.getInteropImpl().equals(OLD_VBS_INTEROP)) {
                    	newInterop.setInteropImpl(NEW_VBS_INTEROP);
                    } else {
                    	newInterop.setInteropImpl(interop.getInteropImpl());
                    }
                                        
                    newInterop.setInteropInputs(new generated.v6.course.InteropInputs());
                    
                    Object interopObj = interop.getInteropInputs().getInteropInput();
                    if(interopObj instanceof generated.v5_1.course.VBS2InteropInputs){
                        
                        generated.v5_1.course.VBS2InteropInputs vbs2 = (generated.v5_1.course.VBS2InteropInputs)interopObj;
                        generated.v6.course.VBSInteropInputs newVbs = new generated.v6.course.VBSInteropInputs();
                        
                        generated.v6.course.VBSInteropInputs.LoadArgs newLoadArgs = new generated.v6.course.VBSInteropInputs.LoadArgs();
                        newLoadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                        newVbs.setLoadArgs(newLoadArgs);
                        
                        newInterop.getInteropInputs().setInteropInput(newVbs);
                        
                    }else if(interopObj instanceof generated.v5_1.course.DISInteropInputs){
                        
                        generated.v6.course.DISInteropInputs newDIS = new generated.v6.course.DISInteropInputs();
                        newDIS.setLoadArgs(new generated.v6.course.DISInteropInputs.LoadArgs());
                        
                        newInterop.getInteropInputs().setInteropInput(newDIS);
                        
                    }else if(interopObj instanceof generated.v5_1.course.PowerPointInteropInputs){
                        
                        generated.v5_1.course.PowerPointInteropInputs ppt = (generated.v5_1.course.PowerPointInteropInputs)interopObj;
                        generated.v6.course.PowerPointInteropInputs newPPT = new generated.v6.course.PowerPointInteropInputs();
                        
                        newPPT.setLoadArgs(new generated.v6.course.PowerPointInteropInputs.LoadArgs());
                        
                        newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                        
                        newInterop.getInteropInputs().setInteropInput(newPPT);
                        
                    }else if(interopObj instanceof generated.v5_1.course.TC3InteropInputs){
                    	
                    	generated.v5_1.course.TC3InteropInputs tc3 = (generated.v5_1.course.TC3InteropInputs)interopObj;
                    	generated.v6.course.TC3InteropInputs newTC3 = new generated.v6.course.TC3InteropInputs();
                    	
                    	newTC3.setLoadArgs(new generated.v6.course.TC3InteropInputs.LoadArgs());
                    	
                    	newTC3.getLoadArgs().setScenarioName(tc3.getLoadArgs().getScenarioName());
                    	
                    	newInterop.getInteropInputs().setInteropInput(newTC3);
                    	
                    }else if(interopObj instanceof generated.v5_1.course.SCATTInteropInputs){
                    
                    	generated.v6.course.SCATTInteropInputs newSCAT = new generated.v6.course.SCATTInteropInputs();           	
                    	newSCAT.setLoadArgs(new generated.v6.course.SCATTInteropInputs.LoadArgs());
                    	
                    	newInterop.getInteropInputs().setInteropInput(newSCAT);
                    	
                	}else if(interopObj instanceof generated.v5_1.course.CustomInteropInputs){
                        
                        generated.v5_1.course.CustomInteropInputs custom = (generated.v5_1.course.CustomInteropInputs)interopObj;
                        generated.v6.course.CustomInteropInputs newCustom = new generated.v6.course.CustomInteropInputs();
                        
                        newCustom.setLoadArgs(new generated.v6.course.CustomInteropInputs.LoadArgs());
                        
                        for(generated.v5_1.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                            generated.v6.course.Nvpair newPair = new generated.v6.course.Nvpair();
                            newPair.setName(pair.getName());
                            newPair.setValue(pair.getValue());
                            newCustom.getLoadArgs().getNvpair().add(newPair);
                        }
                        
                        newInterop.getInteropInputs().setInteropInput(newCustom);
                        
                        
                	}else if(interopObj instanceof generated.v5_1.course.DETestbedInteropInputs){
                	    
                	    generated.v5_1.course.DETestbedInteropInputs deTestbed = (generated.v5_1.course.DETestbedInteropInputs)interopObj;
                	    generated.v6.course.DETestbedInteropInputs newDeTestbed = new generated.v6.course.DETestbedInteropInputs();
                	    
                	    generated.v6.course.DETestbedInteropInputs.LoadArgs args = new generated.v6.course.DETestbedInteropInputs.LoadArgs();
                	    args.setScenarioName(deTestbed.getLoadArgs().getScenarioName());
                	    newDeTestbed.setLoadArgs(args);
                        
                        newInterop.getInteropInputs().setInteropInput(newDeTestbed);
                        
                    } else if(interopObj instanceof generated.v5_1.course.SimpleExampleTAInteropInputs){
                        generated.v5_1.course.SimpleExampleTAInteropInputs TAInputs = (generated.v5_1.course.SimpleExampleTAInteropInputs) interopObj;
                        generated.v6.course.SimpleExampleTAInteropInputs newTAInputs = new generated.v6.course.SimpleExampleTAInteropInputs();
                        generated.v6.course.SimpleExampleTAInteropInputs.LoadArgs args = new generated.v6.course.SimpleExampleTAInteropInputs.LoadArgs();
                        args.setScenarioName(TAInputs.getLoadArgs().getScenarioName());
                        
                        newTAInputs.setLoadArgs(args);
                        newInterop.getInteropInputs().setInteropInput(newTAInputs);
                    } else {
                        throw new IllegalArgumentException("Found unhandled interop input type of "+interopObj);
                    }
                    
                    newTrainApp.getInterops().getInterop().add(newInterop);
                }
                
                newTransitions.getTransitionType().add(newTrainApp);
                
            }else if(transitionObj instanceof generated.v5_1.course.LessonMaterial){
                
                generated.v5_1.course.LessonMaterial lessonMaterial = (generated.v5_1.course.LessonMaterial)transitionObj;
                generated.v6.course.LessonMaterial newLessonMaterial = new generated.v6.course.LessonMaterial();
                
                newLessonMaterial.setTransitionName(lessonMaterial.getTransitionName());
                
                if(lessonMaterial.getLessonMaterialList() != null){
                    generated.v6.course.LessonMaterialList newLessonMaterialList = new generated.v6.course.LessonMaterialList();

                    for(generated.v5_1.course.Media media : lessonMaterial.getLessonMaterialList().getMedia()){
                        
                        generated.v6.course.Media newMedia = new generated.v6.course.Media();
                        newMedia.setName(media.getName());
                        newMedia.setUri(media.getUri());
                        
                        Object mediaType = media.getMediaTypeProperties();
                        if(mediaType instanceof generated.v5_1.course.PDFProperties){
                            newMedia.setMediaTypeProperties(new generated.v6.course.PDFProperties());
                            
                        }else if(mediaType instanceof generated.v5_1.course.WebpageProperties){
                            newMedia.setMediaTypeProperties(new generated.v6.course.WebpageProperties());
                            
                        }else if(mediaType instanceof generated.v5_1.course.YoutubeVideoProperties){
                            
                            generated.v5_1.course.YoutubeVideoProperties uTubeProp = (generated.v5_1.course.YoutubeVideoProperties)mediaType;
                            generated.v6.course.YoutubeVideoProperties newUTubeProp = new generated.v6.course.YoutubeVideoProperties();
                            
                            if (uTubeProp.getAllowFullScreen() != null) {
                            	newUTubeProp.setAllowFullScreen(generated.v6.course.BooleanEnum.fromValue(uTubeProp.getAllowFullScreen().toString().toLowerCase()));
                            }
                            if (uTubeProp.getAllowAutoPlay() != null) {
                            	newUTubeProp.setAllowAutoPlay(generated.v6.course.BooleanEnum.fromValue(uTubeProp.getAllowAutoPlay().toString().toLowerCase()));
                            }
                            
                            if(uTubeProp.getSize() != null){
                                generated.v6.course.Size newSize = new generated.v6.course.Size();
                                newSize.setHeight(uTubeProp.getSize().getHeight());
                                newSize.setWidth(uTubeProp.getSize().getWidth());
                                newUTubeProp.setSize(newSize);
                            }

                            newMedia.setMediaTypeProperties(newUTubeProp);
                            
                        }else if(mediaType instanceof generated.v5_1.course.ImageProperties){
                            newMedia.setMediaTypeProperties(new generated.v6.course.ImageProperties());
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled media type of "+mediaType);
                        }
                        
                        newLessonMaterialList.getMedia().add(newMedia);
                    }
                    
                    newLessonMaterial.setLessonMaterialList(newLessonMaterialList);
                }

                if(lessonMaterial.getLessonMaterialFiles() != null){
                    
                    generated.v6.course.LessonMaterialFiles newFiles = new generated.v6.course.LessonMaterialFiles();
                    for(String file : lessonMaterial.getLessonMaterialFiles().getFile()){
                        newFiles.getFile().add(file);
                    }                    
                    
                    newLessonMaterial.setLessonMaterialFiles(newFiles);                 
                }
                
                newTransitions.getTransitionType().add(newLessonMaterial);
                
            }else if(transitionObj instanceof generated.v5_1.course.MerrillsBranchPoint){
            	
            	generated.v5_1.course.MerrillsBranchPoint mBranchPoint = (generated.v5_1.course.MerrillsBranchPoint)transitionObj;      	
            	generated.v6.course.MerrillsBranchPoint newMBranchPoint = new generated.v6.course.MerrillsBranchPoint();
            	
            	newMBranchPoint.setTransitionName(mBranchPoint.getTransitionName());
            	
            	generated.v6.course.MerrillsBranchPoint.Concepts newConcepts = new generated.v6.course.MerrillsBranchPoint.Concepts();
            	newConcepts.getConcept().addAll(mBranchPoint.getConcepts().getConcept());
            	newMBranchPoint.setConcepts(newConcepts);
            	
            	generated.v6.course.MerrillsBranchPoint.Quadrants newQuadrants = convertQuadrants(mBranchPoint.getQuadrants());            	
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
     * @return generated.course.MerrillsBranchPoint.Quadrants - the converted quadrants element
     */
    private static generated.v6.course.MerrillsBranchPoint.Quadrants convertQuadrants(generated.v5_1.course.MerrillsBranchPoint.Quadrants quadrants) throws IllegalArgumentException {
    	
    	generated.v6.course.MerrillsBranchPoint.Quadrants newQuadrants = new generated.v6.course.MerrillsBranchPoint.Quadrants();
    	
    	for (Object quadrant : quadrants.getContent()) {
    		if (quadrant instanceof generated.v5_1.course.Rule) {
    		
    			newQuadrants.getContent().add(new generated.v6.course.Rule());
    			
    		}else if (quadrant instanceof generated.v5_1.course.Practice) {
    			
    			generated.v5_1.course.Practice practice = (generated.v5_1.course.Practice)quadrant;
    			generated.v6.course.Practice newPractice = new generated.v6.course.Practice();
    			generated.v6.course.Practice.PracticeConcepts newConcept = new generated.v6.course.Practice.PracticeConcepts();
    			
    			newConcept.getCourseConcept().addAll(practice.getPracticeConcepts().getCourseConcept());
    			
    			newPractice.setPracticeConcepts(newConcept);
    			newPractice.setAllowedAttempts(practice.getAllowedAttempts());
    			
    			newQuadrants.getContent().add(newPractice);
    			
    		}else if (quadrant instanceof generated.v5_1.course.Recall) {
    			
    			generated.v5_1.course.Recall recall = (generated.v5_1.course.Recall)quadrant;
    			
    			generated.v6.course.Recall newRecall = new generated.v6.course.Recall();
    			
    			// Convert QuestionTypes and AssessmentRules
    			generated.v6.course.Recall.PresentSurvey survey = new generated.v6.course.Recall.PresentSurvey();
    			survey.setSurveyChoice(new generated.v6.course.Recall.PresentSurvey.ConceptSurvey());
    			
    			survey.getSurveyChoice().getConceptQuestions().addAll(
    					convertConceptQuestions(recall.getPresentSurvey().getSurveyChoice().getConceptQuestions()));
    			
    			survey.getSurveyChoice().setGIFTSurveyKey(recall.getPresentSurvey().getSurveyChoice().getGIFTSurveyKey());
    			
    			newRecall.setPresentSurvey(survey);
    			
    			// Copy allowed attempts
    			newRecall.setAllowedAttempts(recall.getAllowedAttempts());
    			
    			if(recall.getPresentSurvey().getFullScreen() != null) {
	    			// Copy fullScreen
    				
	    			newRecall.getPresentSurvey().setFullScreen(generated.v6.course.BooleanEnum.fromValue(
	    					recall.getPresentSurvey().getFullScreen().toString().toLowerCase()));
    			}
    			
    			newQuadrants.getContent().add(newRecall);
    			
    		}else if (quadrant instanceof generated.v5_1.course.Example) {
    			
    			newQuadrants.getContent().add(new generated.v6.course.Example());
    			
    		}else if (quadrant instanceof generated.v5_1.course.Transitions) {
    			
    			generated.v5_1.course.Transitions transitions = (generated.v5_1.course.Transitions)quadrant;
    			generated.v6.course.Transitions newTransitions = convertTransitions(transitions);
    			
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
     * @return generated.course.Guidance - the converted guidance element
     * @throws IllegalArgumentException
     */
    private static generated.v6.course.Guidance convertGuidance(generated.v5_1.course.Guidance guidance) throws IllegalArgumentException{
        
        generated.v6.course.Guidance newGuidance = new generated.v6.course.Guidance();
        
       	newGuidance.setDisplayTime(guidance.getDisplayTime());
       	newGuidance.setTransitionName(guidance.getTransitionName());
        
        if (guidance.getFullScreen() != null) {
        	newGuidance.setFullScreen(generated.v6.course.BooleanEnum.fromValue(guidance.getFullScreen().toString().toLowerCase()));
        }
        
        Object guidanceChoice = guidance.getGuidanceChoice();
        if (guidanceChoice instanceof generated.v5_1.course.Guidance.Message) {

        	generated.v5_1.course.Guidance.Message message = (generated.v5_1.course.Guidance.Message)guidanceChoice;
        	
            generated.v6.course.Guidance.Message newMessage = new generated.v6.course.Guidance.Message();
            newMessage.setContent(message.getContent());
            
            newGuidance.setGuidanceChoice(newMessage);
        	
        }else if (guidanceChoice instanceof generated.v5_1.course.Guidance.File) {
        	
        	generated.v5_1.course.Guidance.File file = (generated.v5_1.course.Guidance.File)guidanceChoice;
        	
        	generated.v6.course.Guidance.File newFile = new generated.v6.course.Guidance.File();
        	
        	newFile.setHTML(file.getHTML());
        	newFile.setMessage(file.getMessage());
        	
        	newGuidance.setGuidanceChoice(newFile);
        	
        }else if (guidanceChoice instanceof generated.v5_1.course.Guidance.URL) {
        	
        	generated.v5_1.course.Guidance.URL url = (generated.v5_1.course.Guidance.URL)guidanceChoice;
        	
        	generated.v6.course.Guidance.URL newURL = new generated.v6.course.Guidance.URL();
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
    private static generated.v6.course.PresentSurvey convertPresentSurvey(generated.v5_1.course.PresentSurvey presentSurvey) throws IllegalArgumentException{
    	
    	generated.v6.course.PresentSurvey newPresentSurvey = new generated.v6.course.PresentSurvey();
    	
    	Object surveyChoice = presentSurvey.getSurveyChoice();
    	if (surveyChoice instanceof generated.v5_1.course.AutoTutorSession) {
    		
    		generated.v5_1.course.AutoTutorSession autoTutorSession = (generated.v5_1.course.AutoTutorSession)surveyChoice;
    		
    		generated.v6.course.AutoTutorSession newAutoTutorSession = new generated.v6.course.AutoTutorSession();
    		
    		generated.v6.course.DkfRef newDkfRef = new generated.v6.course.DkfRef();
    		
    		newDkfRef.setFile(autoTutorSession.getDkfRef().getFile());
    		//MH: had to comment out until new conversion wizard class is created
    		newAutoTutorSession.setDkfRef(newDkfRef);
    		
    		newPresentSurvey.setSurveyChoice(newAutoTutorSession);
    		
    	}else if (surveyChoice instanceof generated.v5_1.course.PresentSurvey.ConceptSurvey ) {
    		
    		generated.v5_1.course.PresentSurvey.ConceptSurvey conceptSurvey = (generated.v5_1.course.PresentSurvey.ConceptSurvey)surveyChoice;
    		
    		generated.v6.course.PresentSurvey.ConceptSurvey newConceptSurvey = new generated.v6.course.PresentSurvey.ConceptSurvey();
    		
    		newConceptSurvey.setGIFTSurveyKey(conceptSurvey.getGIFTSurveyKey());
    		
    		newConceptSurvey.setFullScreen(generated.v6.course.BooleanEnum.fromValue(
    				conceptSurvey.getFullScreen().toString().toLowerCase()));
    		
    		newConceptSurvey.setSkipConceptsByExamination(generated.v6.course.BooleanEnum.fromValue(
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
     * @param concepts - the v5.1 course concepts
     * @return the converted v6.0 course concepts
     */
    private static generated.v6.course.Concepts convertConcepts(generated.v5_1.course.Concepts concepts) {
    	
    	generated.v6.course.Concepts newConcepts = new generated.v6.course.Concepts();
    	
    	Object list = concepts.getListOrHierarchy();
    	
    	if(list instanceof generated.v5_1.course.Concepts.List) {
    		
    		generated.v6.course.Concepts.List newList = new generated.v6.course.Concepts.List();
    		
    		for(generated.v5_1.course.Concepts.List.Concept c : ((generated.v5_1.course.Concepts.List) list).getConcept()) {
    			
	    		generated.v6.course.Concepts.List.Concept concept = new generated.v6.course.Concepts.List.Concept();
	    		
	    		concept.setName(c.getName());
	    		newList.getConcept().add(concept);
    		}
    		
    		newConcepts.setListOrHierarchy(newList);
    		
    	} else {
    		
    		generated.v5_1.course.Concepts.Hierarchy hierarchy = (generated.v5_1.course.Concepts.Hierarchy)list;
    		generated.v6.course.Concepts.Hierarchy newList = new generated.v6.course.Concepts.Hierarchy();
    		generated.v6.course.ConceptNode newSadness = new generated.v6.course.ConceptNode();
    		generated.v5_1.course.ConceptNode sadness = hierarchy.getConceptNode();
    		    		
    		newSadness.setName(sadness.getName());		
    		
    		while(sadness.getConceptNode() != null) {
    			
    			generated.v6.course.ConceptNode newNode = new generated.v6.course.ConceptNode();
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
     * @param conceptQuestions - the list of v5.1 conceptQuestions
     * @return the converted list of v6.0 conceptQuestions
     */
    private static List<generated.v6.course.ConceptQuestions> convertConceptQuestions(List<generated.v5_1.course.ConceptQuestions> conceptQuestions) {
    	
    	List<generated.v6.course.ConceptQuestions> newConceptQuestions = new ArrayList<generated.v6.course.ConceptQuestions>();
    	
    	for(generated.v5_1.course.ConceptQuestions q : conceptQuestions) {
    		
    		generated.v6.course.ConceptQuestions newQuestion = new generated.v6.course.ConceptQuestions();
    		generated.v6.course.ConceptQuestions.QuestionTypes newTypes = new generated.v6.course.ConceptQuestions.QuestionTypes();
    		generated.v6.course.ConceptQuestions.AssessmentRules newRules = new generated.v6.course.ConceptQuestions.AssessmentRules();
    		generated.v6.course.ConceptQuestions.AssessmentRules.AtExpectation atRule = new generated.v6.course.ConceptQuestions.AssessmentRules.AtExpectation();
    		generated.v6.course.ConceptQuestions.AssessmentRules.AboveExpectation overRule = new generated.v6.course.ConceptQuestions.AssessmentRules.AboveExpectation();
    		generated.v6.course.ConceptQuestions.AssessmentRules.BelowExpectation underRule = new generated.v6.course.ConceptQuestions.AssessmentRules.BelowExpectation();
    		
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
    	generated.v5_1.dkf.Scenario v5Scenario = (generated.v5_1.dkf.Scenario)uFile.getUnmarshalled();

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
    public UnmarshalledFile convertScenario(generated.v5_1.dkf.Scenario v5Scenario, boolean showCompletionDialog) throws IllegalArgumentException {
        	
    	generated.v6.dkf.Scenario newScenario = new generated.v6.dkf.Scenario();
    	 
    	//
        // copy over contents from old object to new object
        //
        newScenario.setDescription(v5Scenario.getDescription());
        newScenario.setName(v5Scenario.getName());
        
        //
        //Learner Id
        //
        if(v5Scenario.getLearnerId() != null){
            generated.v6.dkf.LearnerId newLearnerId = new generated.v6.dkf.LearnerId();
            generated.v6.dkf.StartLocation newStartLocation = new generated.v6.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(v5Scenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }
        
        //
        //Resources
        //
        generated.v6.dkf.Resources newResources = new generated.v6.dkf.Resources();
        newResources.setSurveyContext(v5Scenario.getResources().getSurveyContext());
        
        generated.v6.dkf.AvailableLearnerActions newALA = new generated.v6.dkf.AvailableLearnerActions();
        
        if(v5Scenario.getResources().getAvailableLearnerActions() != null){
            
            generated.v5_1.dkf.AvailableLearnerActions ala = v5Scenario.getResources().getAvailableLearnerActions();
            if(ala.getLearnerActionsFiles() != null){
                generated.v6.dkf.LearnerActionsFiles newLAF = new generated.v6.dkf.LearnerActionsFiles();
                for(String filename : ala.getLearnerActionsFiles().getFile()){
                    newLAF.getFile().add(filename);
                }
                
                newALA.setLearnerActionsFiles(newLAF);
            }
            
            if(ala.getLearnerActionsList() != null){
                
                generated.v6.dkf.LearnerActionsList newLAL = new generated.v6.dkf.LearnerActionsList();
                for(generated.v5_1.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()){
                    
                    generated.v6.dkf.LearnerAction newAction = new generated.v6.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    newAction.setType(generated.v6.dkf.LearnerActionEnumType.fromValue(action.getType().value()));
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
        generated.v6.dkf.Scenario.EndTriggers newScenarioEndTriggers = new generated.v6.dkf.Scenario.EndTriggers();
        
        if(v5Scenario.getEndTriggers() != null) {
        	newScenarioEndTriggers.getTrigger().addAll(convertScenarioEndTriggers( v5Scenario.getEndTriggers().getTriggers()));
            newScenario.setEndTriggers(newScenarioEndTriggers);
        }
        
        //
        //Assessment
        //
        generated.v6.dkf.Assessment newAssessment = new generated.v6.dkf.Assessment();
        if(v5Scenario.getAssessment() != null){
            
            generated.v5_1.dkf.Assessment assessment = v5Scenario.getAssessment();
            
            //
            // Objects
            //
            generated.v6.dkf.Objects newObjects = new generated.v6.dkf.Objects();
            if(assessment.getObjects() != null){
                
                if(assessment.getObjects().getWaypoints() != null){
                    
                    generated.v6.dkf.Waypoints newWaypoints = new generated.v6.dkf.Waypoints();
                    
                    generated.v5_1.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for(generated.v5_1.dkf.Waypoint waypoint : waypoints.getWaypoint()){
                        
                        generated.v6.dkf.Waypoint newWaypoint = new generated.v6.dkf.Waypoint();
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
            generated.v6.dkf.Tasks newTasks = new generated.v6.dkf.Tasks();
            if(assessment.getTasks() != null){
                
                for(generated.v5_1.dkf.Task task : assessment.getTasks().getTask()){
                    
                    generated.v6.dkf.Task newTask = new generated.v6.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());
                    
                    // start triggers
                    if(task.getStartTriggers() != null){
                        generated.v6.dkf.StartTriggers newStartTriggers = new generated.v6.dkf.StartTriggers();
                        newStartTriggers.getTrigger().addAll(convertStartTriggers( task.getStartTriggers().getTriggers()));
                        newTask.setStartTriggers(newStartTriggers);
                    }
                    
                    // end triggers
                    if(task.getEndTriggers() != null){
                        generated.v6.dkf.EndTriggers newEndTriggers = new generated.v6.dkf.EndTriggers();
                        newEndTriggers.getTrigger().addAll(convertEndTriggers( task.getEndTriggers().getTriggers()));
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
            
            generated.v5_1.dkf.Actions actions = v5Scenario.getActions();
            generated.v6.dkf.Actions newActions = new generated.v6.dkf.Actions();
            
            //instructional strategies
            if(actions.getInstructionalStrategies() != null){
                
                generated.v5_1.dkf.Actions.InstructionalStrategies iStrategies = actions.getInstructionalStrategies();
                generated.v6.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v6.dkf.Actions.InstructionalStrategies();
                
                for(generated.v5_1.dkf.Strategy strategy : iStrategies.getStrategy()){
                    
                    generated.v6.dkf.Strategy newStrategy = new generated.v6.dkf.Strategy();
                    newStrategy.setName(strategy.getName());
                    
                    Object strategyType = strategy.getStrategyType();//getValueAttribute();
                    if(strategyType instanceof generated.v5_1.dkf.PerformanceAssessment){
                        
                        generated.v5_1.dkf.PerformanceAssessment perfAss = (generated.v5_1.dkf.PerformanceAssessment)strategyType;
                        
                        generated.v6.dkf.PerformanceAssessment newPerfAss = new generated.v6.dkf.PerformanceAssessment();
                        
                        newPerfAss.setNodeId(perfAss.getNodeId());
                        newPerfAss.setStrategyHandler(convertStrategyHandler(perfAss.getStrategyHandler()));
                        
                        newStrategy.setStrategyType(newPerfAss);
                        
                    }else if(strategyType instanceof generated.v5_1.dkf.InstructionalIntervention){
                        
                        generated.v5_1.dkf.InstructionalIntervention iIntervention = (generated.v5_1.dkf.InstructionalIntervention)strategyType;
                        
                        generated.v6.dkf.InstructionalIntervention newIIntervention = new generated.v6.dkf.InstructionalIntervention();
                        newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));

                        //only have a feedback choice in this version
                        for(generated.v5_1.dkf.Feedback feedback : iIntervention.getInterventionTypes()){
                            
                            generated.v6.dkf.Feedback newFeedback = new generated.v6.dkf.Feedback();
                            
                            if (feedback.getFeedbackPresentation() instanceof generated.v5_1.dkf.Message) {
       
                            	generated.v5_1.dkf.Message message = (generated.v5_1.dkf.Message) feedback.getFeedbackPresentation();
                            	generated.v6.dkf.Message feedbackMsg = new generated.v6.dkf.Message();
                            	
                            	// Copy the message delivery settings
                            	if(message.getDelivery() != null) {
                            		
                            		generated.v6.dkf.Message.Delivery delivery = new generated.v6.dkf.Message.Delivery();
                            		
                            		if(message.getDelivery().getInTrainingApplication() != null) {
                            		
                            			generated.v6.dkf.Message.Delivery.InTrainingApplication newInApp = new generated.v6.dkf.Message.Delivery.InTrainingApplication();
                            			newInApp.setEnabled(generated.v6.dkf.BooleanEnum.fromValue(message.getDelivery().getInTrainingApplication().getEnabled().toString().toLowerCase()));
                            		
                            			delivery.setInTrainingApplication(newInApp);
                            		}
                            		
                            		if(message.getDelivery().getInTutor() != null) {
                            			
                            			generated.v6.dkf.InTutor inTutor = new generated.v6.dkf.InTutor();
                            			
                            			inTutor.setMessagePresentation(message.getDelivery().getInTutor().getMessagePresentation());
                            			inTutor.setTextEnhancement(message.getDelivery().getInTutor().getTextEnhancement());
                            			
                            			delivery.setInTutor(inTutor);
                            		}
                            		
                            		feedbackMsg.setDelivery(delivery);
                            	}
                            	
                            	feedbackMsg.setContent(message.getContent());
                            	
                            	newFeedback.setFeedbackPresentation(feedbackMsg);
                            	
                            }
                            else if (feedback.getFeedbackPresentation() instanceof generated.v5_1.dkf.Audio) {
                            	
                            	generated.v5_1.dkf.Audio audio = (generated.v5_1.dkf.Audio) feedback.getFeedbackPresentation();
                            	
                            	generated.v6.dkf.Audio newAudio = new generated.v6.dkf.Audio();
                            	
                            	// An audio object requires a .mp3 file but does not require a .ogg file
                            	newAudio.setMP3File(audio.getMP3File());
                            	
                            	if (audio.getOGGFile() != null) {
                            		newAudio.setOGGFile(audio.getOGGFile());
                            	}
                            	
                            	newFeedback.setFeedbackPresentation(newAudio);
                            	
                            }
                            
                            else if (feedback.getFeedbackPresentation() instanceof generated.v5_1.dkf.MediaSemantics) {
                            	
                            	generated.v5_1.dkf.MediaSemantics semantics = (generated.v5_1.dkf.MediaSemantics) feedback.getFeedbackPresentation();
                            	
                            	generated.v6.dkf.MediaSemantics newSemantics = new generated.v6.dkf.MediaSemantics();
                            	
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
                        
                    }else if(strategyType instanceof generated.v5_1.dkf.ScenarioAdaptation){
                        
                        generated.v5_1.dkf.ScenarioAdaptation adaptation = (generated.v5_1.dkf.ScenarioAdaptation)strategyType;
                        
                        generated.v6.dkf.ScenarioAdaptation newAdaptation = new generated.v6.dkf.ScenarioAdaptation();
                        newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));
                        
                        //only have environment adaptation in this version
                        for(generated.v5_1.dkf.EnvironmentAdaptation eAdapt : adaptation.getAdaptationTypes()){
                            
                            generated.v6.dkf.EnvironmentAdaptation newEAdapt = new generated.v6.dkf.EnvironmentAdaptation();
                            
                            generated.v6.dkf.EnvironmentAdaptation.Pair newPair = new generated.v6.dkf.EnvironmentAdaptation.Pair();
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
               
                generated.v5_1.dkf.Actions.StateTransitions sTransitions = actions.getStateTransitions();
                generated.v6.dkf.Actions.StateTransitions newSTransitions = new generated.v6.dkf.Actions.StateTransitions();
                
                for(generated.v5_1.dkf.Actions.StateTransitions.StateTransition sTransition : sTransitions.getStateTransition()){
                    
                    generated.v6.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v6.dkf.Actions.StateTransitions.StateTransition();
                    
                    generated.v6.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v6.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();
                    
                    newSTransition.setName(sTransition.getName());
                    
                    //State type
                    for (Object stateType : sTransition.getLogicalExpression().getStateType()) {
	                    if(stateType instanceof generated.v5_1.dkf.LearnerStateTransitionEnum){
	
	                        generated.v5_1.dkf.LearnerStateTransitionEnum stateEnum = (generated.v5_1.dkf.LearnerStateTransitionEnum)stateType;
	                        
	                        generated.v6.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v6.dkf.LearnerStateTransitionEnum();
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
	                        
	                    }else if(stateType instanceof generated.v5_1.dkf.PerformanceNode){
	                        
	                        generated.v5_1.dkf.PerformanceNode perfNode = (generated.v5_1.dkf.PerformanceNode)stateType;
	                        
	                        generated.v6.dkf.PerformanceNode newPerfNode = new generated.v6.dkf.PerformanceNode();
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
                    generated.v6.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v6.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    for(generated.v5_1.dkf.StrategyRef strategyRef : sTransition.getStrategyChoices().getStrategyRef()){
                        
                        generated.v6.dkf.StrategyRef newStrategyRef = new generated.v6.dkf.StrategyRef();
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
        
        ConversionWizardUtil_v2015_1_v2016_1 util = new ConversionWizardUtil_v2015_1_v2016_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertScenario(newScenario, showCompletionDialog);
    	
    }
    
    
    /**
     * Convert an entities object to a new entities object.
     * 
     * @param entities - the object to convert
     * @return generated.v3.dkf.Entities - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v6.dkf.Entities convertEntities(generated.v5_1.dkf.Entities entities) throws IllegalArgumentException{
        
        generated.v6.dkf.Entities newEntities = new generated.v6.dkf.Entities();
        for(generated.v5_1.dkf.StartLocation location : entities.getStartLocation()){
            
            generated.v6.dkf.StartLocation newLocation = new generated.v6.dkf.StartLocation();
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
    private static generated.v6.dkf.Path convertPath(generated.v5_1.dkf.Path path){
        
        generated.v6.dkf.Path newPath = new generated.v6.dkf.Path();
        for(generated.v5_1.dkf.Segment segment : path.getSegment()){
            
            generated.v6.dkf.Segment newSegment = new generated.v6.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());
            
            generated.v6.dkf.Start start = new generated.v6.dkf.Start();
            start.setWaypoint(segment.getStart().getWaypoint());
            newSegment.setStart(start);
            
            generated.v6.dkf.End end = new generated.v6.dkf.End();
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
    private static generated.v6.dkf.Checkpoint convertCheckpoint(generated.v5_1.dkf.Checkpoint checkpoint){
        
        generated.v6.dkf.Checkpoint newCheckpoint = new generated.v6.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setWaypoint(checkpoint.getWaypoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());
        
        return newCheckpoint;
    }
    
    /**
     * Convert an evaluators object into a new evaluators object.
     * 
     * @param evaluators - the object to convert
     * @return generated.v6.dkf.Evaluators - the new object
     */
    private static generated.v6.dkf.Evaluators convertEvaluators(generated.v5_1.dkf.Evaluators evaluators){
        
        generated.v6.dkf.Evaluators newEvaluators = new generated.v6.dkf.Evaluators();
        for(generated.v5_1.dkf.Evaluator evaluator : evaluators.getEvaluator()){
            
            generated.v6.dkf.Evaluator newEvaluator = new generated.v6.dkf.Evaluator();
            newEvaluator.setAssessment(evaluator.getAssessment());
            newEvaluator.setValue(evaluator.getValue());                                            
            newEvaluator.setOperator(evaluator.getOperator());
            
            newEvaluators.getEvaluator().add(newEvaluator);
        }
        
        return newEvaluators;
    }
    

    /**
     * Converts v5.1 excavator component inputs
     * 
     * @param oldCompList - the v5.1 component list
     * @return the new v6.0 component list
     */
    private static List<generated.v6.dkf.HasMovedExcavatorComponentInput.Component> convertComponents(
    		List<generated.v5_1.dkf.HasMovedExcavatorComponentInput.Component> oldCompList) {
    	
    	List<generated.v6.dkf.HasMovedExcavatorComponentInput.Component> componentList = 
    			new ArrayList<generated.v6.dkf.HasMovedExcavatorComponentInput.Component>();
    	
    	for(generated.v5_1.dkf.HasMovedExcavatorComponentInput.Component oldComp : oldCompList) {
    		 generated.v6.dkf.HasMovedExcavatorComponentInput.Component newComp = new generated.v6.dkf.HasMovedExcavatorComponentInput.Component();
    		 newComp.setComponentType(generated.v6.dkf.ExcavatorComponentEnum.fromValue(oldComp.getComponentType().value()));
    		 
    		 if(oldComp.getDirectionType() instanceof generated.v5_1.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional){
                 generated.v5_1.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional oldBiDirectional = (generated.v5_1.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional)oldComp.getDirectionType();
                 generated.v6.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional newBiDirectional = new generated.v6.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional();
                 newBiDirectional.setNegativeRotation(oldBiDirectional.getNegativeRotation());
                 newBiDirectional.setPositiveRotation(oldBiDirectional.getPositiveRotation());
                 newComp.setDirectionType(newBiDirectional);
    		 }else{
        		 newComp.setDirectionType(oldComp.getDirectionType());
        		 componentList.add(newComp);
    		 }
    	}
    	
    	return componentList;
    }
        
    
    /**
     * Convert an assessment object into a new assessment object.
     * 
     * @param assessments - the assessment object to convert
     * @return generated.v6.dkf.Assessments - the new assessment object
     */
    private static generated.v6.dkf.Assessments convertAssessments(generated.v5_1.dkf.Assessments assessments){
        
        generated.v6.dkf.Assessments newAssessments = new generated.v6.dkf.Assessments();
        
        List<generated.v5_1.dkf.Assessments.Survey> surveys = new ArrayList<generated.v5_1.dkf.Assessments.Survey>();
        for (Object assessmentType : assessments.getAssessmentTypes()) {
        	if (assessmentType instanceof generated.v5_1.dkf.Assessments.Survey) {
        		surveys.add((Survey) assessmentType);
        	}
        }
        for(generated.v5_1.dkf.Assessments.Survey survey : surveys){
            
            generated.v6.dkf.Assessments.Survey newSurvey = new generated.v6.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());
            
            generated.v6.dkf.Questions newQuestions = new generated.v6.dkf.Questions();
            for(generated.v5_1.dkf.Question question : survey.getQuestions().getQuestion()){
                
                generated.v6.dkf.Question newQuestion = new generated.v6.dkf.Question();
                newQuestion.setKey(question.getKey());
                
                for(generated.v5_1.dkf.Reply reply : question.getReply()){
                    
                    generated.v6.dkf.Reply newReply = new generated.v6.dkf.Reply();
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
     * @return List<generated.v6.dkf.StartTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v6.dkf.StartTriggers.Trigger> convertStartTriggers(List<Serializable> list) throws IllegalArgumentException{
        
        List<generated.v6.dkf.StartTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(Serializable triggerObj : list){
            
            generated.v6.dkf.StartTriggers.Trigger trigger = new generated.v6.dkf.StartTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj));
            
            newTriggerObjects.add(trigger);
        }
        
        return newTriggerObjects;
    }
    
    /**
     * Convert a collection of end trigger objects into the new schema version.
     * 
     * @param list - collection of trigger objects to convert
     * @return List<generated.v6.dkf.EndTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v6.dkf.EndTriggers.Trigger> convertEndTriggers(List<Serializable> list) throws IllegalArgumentException{
        
        List<generated.v6.dkf.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(Serializable triggerObj : list){
            
            generated.v6.dkf.EndTriggers.Trigger trigger = new generated.v6.dkf.EndTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj));
            
            newTriggerObjects.add(trigger);
        }
        
        return newTriggerObjects;
    }
    
    /**
     * Convert a collection of end trigger objects into the new schema version.
     * 
     * @param list - collection of trigger objects to convert
     * @return List<generated.v6.dkf.Scenario.EndTriggers.Trigger> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<generated.v6.dkf.Scenario.EndTriggers.Trigger> convertScenarioEndTriggers(List<Serializable> list) throws IllegalArgumentException{
        
        List<generated.v6.dkf.Scenario.EndTriggers.Trigger> newTriggerObjects = new ArrayList<>();
        for(Serializable triggerObj : list){
            
            generated.v6.dkf.Scenario.EndTriggers.Trigger trigger = new generated.v6.dkf.Scenario.EndTriggers.Trigger();
            trigger.setTriggerType(convertTrigger(triggerObj));
            
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
            
        if(triggerObj instanceof generated.v5_1.dkf.EntityLocation){
            
            generated.v5_1.dkf.EntityLocation entityLocation = (generated.v5_1.dkf.EntityLocation)triggerObj;
            generated.v6.dkf.EntityLocation newEntityLocation = new generated.v6.dkf.EntityLocation();
            
            generated.v6.dkf.StartLocation startLocation = new generated.v6.dkf.StartLocation();
            startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
            newEntityLocation.setStartLocation(startLocation);
            
            generated.v6.dkf.TriggerLocation triggerLocation = new generated.v6.dkf.TriggerLocation();
            triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
            newEntityLocation.setTriggerLocation(triggerLocation);
            
            return newEntityLocation;
                            
        }else if(triggerObj instanceof generated.v5_1.dkf.LearnerLocation){
            
            generated.v5_1.dkf.LearnerLocation learnerLocation = (generated.v5_1.dkf.LearnerLocation)triggerObj;
            generated.v6.dkf.LearnerLocation newLearnerLocation = new generated.v6.dkf.LearnerLocation();
            
            newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
            
            return newLearnerLocation;
            
            
        }else if(triggerObj instanceof generated.v5_1.dkf.ConceptEnded){
            
            generated.v5_1.dkf.ConceptEnded conceptEnded = (generated.v5_1.dkf.ConceptEnded)triggerObj;
            generated.v6.dkf.ConceptEnded newConceptEnded = new generated.v6.dkf.ConceptEnded();
            
            newConceptEnded.setNodeId(conceptEnded.getNodeId());
            
            return newConceptEnded;
            
            
        }else if(triggerObj instanceof generated.v5_1.dkf.ChildConceptEnded){
            
            generated.v5_1.dkf.ChildConceptEnded childConceptEnded = (generated.v5_1.dkf.ChildConceptEnded)triggerObj;
            generated.v6.dkf.ChildConceptEnded newChildConceptEnded = new generated.v6.dkf.ChildConceptEnded();
            
            newChildConceptEnded.setNodeId(childConceptEnded.getNodeId());
            
            return newChildConceptEnded;
            
            
        } else if(triggerObj instanceof generated.v5_1.dkf.TaskEnded){
            
            generated.v5_1.dkf.TaskEnded taskEnded = (generated.v5_1.dkf.TaskEnded)triggerObj;
            generated.v6.dkf.TaskEnded newTaskEnded = new generated.v6.dkf.TaskEnded();
            
            newTaskEnded.setNodeId(taskEnded.getNodeId());
            
            return newTaskEnded;
            
            
        } else if(triggerObj instanceof generated.v5_1.dkf.ConceptAssessment){
            
            generated.v5_1.dkf.ConceptAssessment conceptAssessment = (generated.v5_1.dkf.ConceptAssessment)triggerObj;
            generated.v6.dkf.ConceptAssessment newConceptAssessment = new generated.v6.dkf.ConceptAssessment();
            
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
     * @param coordinate - v5.0 coordinate object to convert
     * @return generated.v6.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException
     */
    private static generated.v6.dkf.Coordinate convertCoordinate(generated.v5_1.dkf.Coordinate coordinate) throws IllegalArgumentException{
        
        generated.v6.dkf.Coordinate newCoord = new generated.v6.dkf.Coordinate();
        
        Object coordType = coordinate.getType();
        if(coordType instanceof generated.v5_1.dkf.GCC){
            
            generated.v5_1.dkf.GCC gcc = (generated.v5_1.dkf.GCC)coordType;
            generated.v6.dkf.GCC newGCC = new generated.v6.dkf.GCC();
            
            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());
            
            newCoord.setType(newGCC);
            
        }else if(coordType instanceof generated.v5_1.dkf.GDC){
           // generated.v5_1.
            generated.v5_1.dkf.GDC gdc = (generated.v5_1.dkf.GDC)coordType;
            generated.v6.dkf.GDC newGDC = new generated.v6.dkf.GDC();
            
            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());
            
            newCoord.setType(newGDC);
            
        }else if(coordType instanceof generated.v5_1.dkf.VBS2AGL){
            
            generated.v5_1.dkf.VBS2AGL agl = (generated.v5_1.dkf.VBS2AGL)coordType;
            generated.v6.dkf.VBSAGL newAGL = new generated.v6.dkf.VBSAGL();
            
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
     * @return generated.v6.dkf.StrategyHandler - the new object
     */
    private static generated.v6.dkf.StrategyHandler convertStrategyHandler(generated.v5_1.dkf.StrategyHandler handler){
        
        generated.v6.dkf.StrategyHandler newHandler = new generated.v6.dkf.StrategyHandler();
        
        if(handler.getParams() != null) {
        	
        	generated.v6.dkf.StrategyHandler.Params newParams = new generated.v6.dkf.StrategyHandler.Params();
        	generated.v6.dkf.Nvpair nvpair = new generated.v6.dkf.Nvpair();
        	
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
     * @return generated.v6.dkf.Concepts - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v6.dkf.Concepts convertConcepts(generated.v5_1.dkf.Concepts concepts) throws IllegalArgumentException{

        generated.v6.dkf.Concepts newConcepts = new generated.v6.dkf.Concepts();
        for(generated.v5_1.dkf.Concept concept : concepts.getConcept()){
            
            generated.v6.dkf.Concept newConcept = new generated.v6.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());
            
            if (concept.getAssessments() != null) {
            	newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            }
            
            Object conditionsOrConcepts = concept.getConditionsOrConcepts();
            if(conditionsOrConcepts instanceof generated.v5_1.dkf.Concepts){
                //nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v5_1.dkf.Concepts)conditionsOrConcepts));
                
            }else if(conditionsOrConcepts instanceof generated.v5_1.dkf.Conditions){
            	
                generated.v6.dkf.Conditions newConditions = new generated.v6.dkf.Conditions();
                
                generated.v5_1.dkf.Conditions conditions = (generated.v5_1.dkf.Conditions)conditionsOrConcepts;
                
                for(generated.v5_1.dkf.Condition condition : conditions.getCondition()){

                    generated.v6.dkf.Condition newCondition = new generated.v6.dkf.Condition();
                    newCondition.setConditionImpl(condition.getConditionImpl());                        
                    
                    if(condition.getDefault() != null){
                        generated.v6.dkf.Default newDefault = new generated.v6.dkf.Default();
                        newDefault.setAssessment(condition.getDefault().getAssessment());
                        newCondition.setDefault(newDefault);
                    }                            
                    
                    //Input
                    generated.v6.dkf.Input newInput = new generated.v6.dkf.Input();
                    if(condition.getInput() != null){
                    	
                        Object inputType = condition.getInput().getType();
                        
                        if(inputType instanceof generated.v5_1.dkf.ApplicationCompletedCondition){
                            
                            generated.v5_1.dkf.ApplicationCompletedCondition conditionInput = (generated.v5_1.dkf.ApplicationCompletedCondition)inputType;
                            
                            generated.v6.dkf.ApplicationCompletedCondition newConditionInput = new generated.v6.dkf.ApplicationCompletedCondition();
                                
                            if (conditionInput.getIdealCompletionDuration() != null) {
                            	newConditionInput.setIdealCompletionDuration(conditionInput.getIdealCompletionDuration());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                        }else if (inputType instanceof generated.v5_1.dkf.AutoTutorConditionInput){

                        	generated.v5_1.dkf.AutoTutorConditionInput conditionInput = (generated.v5_1.dkf.AutoTutorConditionInput)inputType;
                            
                            generated.v6.dkf.AutoTutorConditionInput newConditionInput = new generated.v6.dkf.AutoTutorConditionInput();

                            if (conditionInput.getScript() != null) {
                            	
                                //MH: had to comment until next convesion wizard class is created
                            	if (conditionInput.getScript() instanceof generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO) {
                            		
                            		generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO atRemoteSKO = (generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO) conditionInput.getScript();
                            		
                            		generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO newATRemoteSKO = new generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO();
                            		
                            		generated.v5_1.dkf.AutoTutorConditionInput.ATRemoteSKO.URL url = atRemoteSKO.getURL();
                            		
                            		generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO.URL newURL = new generated.v6.dkf.AutoTutorConditionInput.ATRemoteSKO.URL();
                            		newURL.setAddress(url.getAddress());
                            		newATRemoteSKO.setURL(newURL);
                            		
                            		newConditionInput.setScript(newATRemoteSKO);
                            		
                            	} else if (conditionInput.getScript() instanceof generated.v5_1.dkf.AutoTutorConditionInput.LocalSKO) {

                            		generated.v5_1.dkf.AutoTutorConditionInput.LocalSKO localSKO = (generated.v5_1.dkf.AutoTutorConditionInput.LocalSKO) conditionInput.getScript();
                            		generated.v6.dkf.AutoTutorConditionInput.LocalSKO newLocalSKO = new generated.v6.dkf.AutoTutorConditionInput.LocalSKO();
                            		
                            		newLocalSKO.setFile(localSKO.getFile());
                            		newConditionInput.setScript(newLocalSKO);
                            		
                            	} else {
                            		throw new IllegalArgumentException("Found unhandled AutoTutorConditionInput script type of "+conditionInput.getScript());
                            	}

                            }
                            
                            newInput.setType(newConditionInput);
                        	
                        }else if(inputType instanceof generated.v5_1.dkf.AvoidLocationCondition){
                            
                            generated.v5_1.dkf.AvoidLocationCondition conditionInput = (generated.v5_1.dkf.AvoidLocationCondition)inputType;
                            
                            generated.v6.dkf.AvoidLocationCondition newConditionInput = new generated.v6.dkf.AvoidLocationCondition();
                            
                            if(conditionInput.getWaypointRef() != null){                                    
                                newConditionInput.setWaypointRef(convertWaypointRef(conditionInput.getWaypointRef()));
                            }
                                                            
                            newInput.setType(newConditionInput);
                            
                        }else if(inputType instanceof generated.v5_1.dkf.CheckpointPaceCondition){

                            generated.v5_1.dkf.CheckpointPaceCondition conditionInput = (generated.v5_1.dkf.CheckpointPaceCondition)inputType;
                            
                            generated.v6.dkf.CheckpointPaceCondition newConditionInput = new generated.v6.dkf.CheckpointPaceCondition();
                            for(generated.v5_1.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);
                                                            
                        }else if(inputType instanceof generated.v5_1.dkf.CheckpointProgressCondition){

                            generated.v5_1.dkf.CheckpointProgressCondition conditionInput = (generated.v5_1.dkf.CheckpointProgressCondition)inputType;
                            
                            generated.v6.dkf.CheckpointProgressCondition newConditionInput = new generated.v6.dkf.CheckpointProgressCondition();
                            for(generated.v5_1.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                
                                newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                            }
                                                            
                            newInput.setType(newConditionInput);                                
                            
                        }else if(inputType instanceof generated.v5_1.dkf.CorridorBoundaryCondition){
                            
                            generated.v5_1.dkf.CorridorBoundaryCondition conditionInput = (generated.v5_1.dkf.CorridorBoundaryCondition)inputType;
                            
                            generated.v6.dkf.CorridorBoundaryCondition newConditionInput = new generated.v6.dkf.CorridorBoundaryCondition();
                            newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.CorridorPostureCondition){

                            generated.v5_1.dkf.CorridorPostureCondition conditionInput = (generated.v5_1.dkf.CorridorPostureCondition)inputType;
                            
                            generated.v6.dkf.CorridorPostureCondition newConditionInput = new generated.v6.dkf.CorridorPostureCondition();
                            newConditionInput.setPath(convertPath(conditionInput.getPath()));                                

                            generated.v6.dkf.Postures postures = new generated.v6.dkf.Postures();
                            /*
                            for(generated.v5_1.dkf.PostureEnumType posture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(generated.v6.dkf.PostureEnumType.fromValue(posture.value()));
                            }
                            */
                            for(String strPosture : conditionInput.getPostures().getPosture()){
                                postures.getPosture().add(strPosture);
                            }
                            newConditionInput.setPostures(postures);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.EliminateHostilesCondition){

                            generated.v5_1.dkf.EliminateHostilesCondition conditionInput = (generated.v5_1.dkf.EliminateHostilesCondition)inputType;
                            
                            generated.v6.dkf.EliminateHostilesCondition newConditionInput = new generated.v6.dkf.EliminateHostilesCondition();
                             
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.EnterAreaCondition){

                            generated.v5_1.dkf.EnterAreaCondition conditionInput = (generated.v5_1.dkf.EnterAreaCondition)inputType;
                            
                            generated.v6.dkf.EnterAreaCondition newConditionInput = new generated.v6.dkf.EnterAreaCondition();
                            
                            for(generated.v5_1.dkf.Entrance entrance : conditionInput.getEntrance()){
                                
                                generated.v6.dkf.Entrance newEntrance = new generated.v6.dkf.Entrance();
                                
                                newEntrance.setAssessment(entrance.getAssessment());
                                newEntrance.setName(entrance.getName());
                                
                                generated.v6.dkf.Inside newInside = new generated.v6.dkf.Inside();
                                newInside.setProximity(entrance.getInside().getProximity());
                                newInside.setWaypoint(entrance.getInside().getWaypoint());
                                newEntrance.setInside(newInside);
                                
                                generated.v6.dkf.Outside newOutside = new generated.v6.dkf.Outside();
                                newOutside.setProximity(entrance.getOutside().getProximity());
                                newOutside.setWaypoint(entrance.getOutside().getWaypoint());
                                newEntrance.setOutside(newOutside);
                                
                                newConditionInput.getEntrance().add(newEntrance);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.ExplosiveHazardSpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v5_1.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v5_1.dkf.ExplosiveHazardSpotReportCondition)inputType;
                            
                            generated.v6.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v6.dkf.ExplosiveHazardSpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.GenericConditionInput) {
                        	
	                        generated.v5_1.dkf.GenericConditionInput conditionInput = (generated.v5_1.dkf.GenericConditionInput)inputType;
	                        
	                        generated.v6.dkf.GenericConditionInput newConditionInput = new generated.v6.dkf.GenericConditionInput();
	                        
	                        if(conditionInput.getNvpair() != null){                                    
	                            for (Nvpair nvPair : conditionInput.getNvpair()) {
	                            	newConditionInput.getNvpair().add(convertNvpair(nvPair));
	                            }
	                        }
	                                                        
	                        newInput.setType(newConditionInput);
                        
                    	}else if(inputType instanceof generated.v5_1.dkf.HasMovedExcavatorComponentInput){

                    		generated.v5_1.dkf.HasMovedExcavatorComponentInput conditionInput = (generated.v5_1.dkf.HasMovedExcavatorComponentInput)inputType;
                    		generated.v6.dkf.HasMovedExcavatorComponentInput newConditionInput = new generated.v6.dkf.HasMovedExcavatorComponentInput();
                    		
                    		newConditionInput.getComponent().addAll(convertComponents(conditionInput.getComponent()));
                    		newConditionInput.setMaxAssessments(conditionInput.getMaxAssessments());
                    		
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.IdentifyPOIsCondition){

                            generated.v5_1.dkf.IdentifyPOIsCondition conditionInput = (generated.v5_1.dkf.IdentifyPOIsCondition)inputType;
                            
                            generated.v6.dkf.IdentifyPOIsCondition newConditionInput = new generated.v6.dkf.IdentifyPOIsCondition();
                            
                            if(conditionInput.getPois() != null){
                                
                                generated.v6.dkf.Pois pois = new generated.v6.dkf.Pois();
                                for(generated.v5_1.dkf.WaypointRef waypointRef : conditionInput.getPois().getWaypointRef()){
                                    pois.getWaypointRef().add(convertWaypointRef(waypointRef));
                                }
                                
                                newConditionInput.setPois(pois);
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.LifeformTargetAccuracyCondition){

                            generated.v5_1.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v5_1.dkf.LifeformTargetAccuracyCondition)inputType;
                            
                            generated.v6.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v6.dkf.LifeformTargetAccuracyCondition();
                            
                            if(conditionInput.getEntities() != null){                                    
                                newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                            }
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v5_1.dkf.MarksmanshipPrecisionCondition) {
                        
                        	generated.v5_1.dkf.MarksmanshipPrecisionCondition conditionInput = (generated.v5_1.dkf.MarksmanshipPrecisionCondition)inputType;
                        	
                        	generated.v6.dkf.MarksmanshipPrecisionCondition newConditionInput = new generated.v6.dkf.MarksmanshipPrecisionCondition();
                        	
                        	if(conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                        	
                        	newInput.setType(newConditionInput);
                        	
                    	}else if (inputType instanceof generated.v5_1.dkf.MarksmanshipSessionCompleteCondition) {
                        
                        	generated.v5_1.dkf.MarksmanshipSessionCompleteCondition conditionInput = (generated.v5_1.dkf.MarksmanshipSessionCompleteCondition)inputType;
                        	
                        	generated.v6.dkf.MarksmanshipSessionCompleteCondition newConditionInput = new generated.v6.dkf.MarksmanshipSessionCompleteCondition();
                        	
                        	if(conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                        	
                        	newInput.setType(newConditionInput);
                        
                    	}else if(inputType instanceof generated.v5_1.dkf.NineLineReportCondition){

                            @SuppressWarnings("unused")
                            generated.v5_1.dkf.NineLineReportCondition conditionInput = (generated.v5_1.dkf.NineLineReportCondition)inputType;
                            
                            generated.v6.dkf.NineLineReportCondition newConditionInput = new generated.v6.dkf.NineLineReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.NumberOfShotsFiredCondition){
                       	 
                        	generated.v5_1.dkf.NumberOfShotsFiredCondition conditionInput = (generated.v5_1.dkf.NumberOfShotsFiredCondition)inputType;
                            
                        	generated.v6.dkf.NumberOfShotsFiredCondition newConditionInput = new generated.v6.dkf.NumberOfShotsFiredCondition();
                                                            
                        	if (conditionInput.getExpectedNumberOfShots() != null) {
                        		newConditionInput.setExpectedNumberOfShots(conditionInput.getExpectedNumberOfShots());
                        	}
                   	
                        	newInput.setType(newConditionInput);
                   	
                        }else if(inputType instanceof generated.v5_1.dkf.PowerPointDwellCondition){

                            generated.v5_1.dkf.PowerPointDwellCondition conditionInput = (generated.v5_1.dkf.PowerPointDwellCondition)inputType;
                            
                            generated.v6.dkf.PowerPointDwellCondition newConditionInput = new generated.v6.dkf.PowerPointDwellCondition();
                            
                            generated.v6.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v6.dkf.PowerPointDwellCondition.Default();
                            newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                            newConditionInput.setDefault(newPPTDefault);
                            
                            generated.v6.dkf.PowerPointDwellCondition.Slides slides = new generated.v6.dkf.PowerPointDwellCondition.Slides();
                            for(generated.v5_1.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput.getSlides().getSlide()){
                                
                                generated.v6.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v6.dkf.PowerPointDwellCondition.Slides.Slide();
                                newSlide.setIndex(slide.getIndex());
                                newSlide.setTimeInSeconds(slide.getTimeInSeconds());
                                
                                slides.getSlide().add(newSlide);
                            }
                            newConditionInput.setSlides(slides);
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.RulesOfEngagementCondition){

                            generated.v5_1.dkf.RulesOfEngagementCondition conditionInput = (generated.v5_1.dkf.RulesOfEngagementCondition)inputType;
                            
                            generated.v6.dkf.RulesOfEngagementCondition newConditionInput = new generated.v6.dkf.RulesOfEngagementCondition();
                            generated.v6.dkf.Wcs newWCS = new generated.v6.dkf.Wcs();
                            newWCS.setValue(generated.v6.dkf.WeaponControlStatusEnum.fromValue(conditionInput.getWcs().getValue().value()));
                            newConditionInput.setWcs(newWCS);
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if (inputType instanceof generated.v5_1.dkf.SIMILEConditionInput){
                        
                        	generated.v5_1.dkf.SIMILEConditionInput conditionInput = (generated.v5_1.dkf.SIMILEConditionInput)inputType;
                            
                            generated.v6.dkf.SIMILEConditionInput newConditionInput = new generated.v6.dkf.SIMILEConditionInput();
                        
                            if (conditionInput.getConditionKey() != null) {
                            	newConditionInput.setConditionKey(conditionInput.getConditionKey());
                            }
                            
                            if (conditionInput.getConfigurationFile() != null) {
                            	newConditionInput.setConfigurationFile(conditionInput.getConfigurationFile());
                            }
                            
                            newInput.setType(newConditionInput);
                            
                    	}else if(inputType instanceof generated.v5_1.dkf.SpotReportCondition){

                            @SuppressWarnings("unused")
                            generated.v5_1.dkf.SpotReportCondition conditionInput = (generated.v5_1.dkf.SpotReportCondition)inputType;
                            
                            generated.v6.dkf.SpotReportCondition newConditionInput = new generated.v6.dkf.SpotReportCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.TimerConditionInput){

                            generated.v5_1.dkf.TimerConditionInput conditionInput = (generated.v5_1.dkf.TimerConditionInput)inputType;
                            
                            generated.v6.dkf.TimerConditionInput newConditionInput = new generated.v6.dkf.TimerConditionInput();
                            
                            newConditionInput.setRepeatable(generated.v6.dkf.BooleanEnum.fromValue(conditionInput.getRepeatable().toString().toLowerCase()));
                            newConditionInput.setInterval(conditionInput.getInterval());
                            
                            newInput.setType(newConditionInput);  
                            
                        }else if(inputType instanceof generated.v5_1.dkf.UseRadioCondition){
                            
                            @SuppressWarnings("unused")
                            generated.v5_1.dkf.UseRadioCondition conditionInput = (generated.v5_1.dkf.UseRadioCondition)inputType;
                            
                            generated.v6.dkf.UseRadioCondition newConditionInput = new generated.v6.dkf.UseRadioCondition();
                                                            
                            newInput.setType(newConditionInput);  
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled condition input type of "+inputType);
                        }

                    }
                    newCondition.setInput(newInput);
                    
                    //Scoring
                    generated.v6.dkf.Scoring newScoring = new generated.v6.dkf.Scoring();
                    if(condition.getScoring() != null){
                    	// Only add the scoring element if it has children. 
                    	// As of version 5, there cannot be a scoring element with no children
                    	if (!condition.getScoring().getType().isEmpty()) {
                    		
                        	for(Object scoringType : condition.getScoring().getType()){
                                
                                if(scoringType instanceof generated.v5_1.dkf.Count){
                                    
                                    generated.v5_1.dkf.Count count = (generated.v5_1.dkf.Count)scoringType;
                                    
                                    generated.v6.dkf.Count newCount = new generated.v6.dkf.Count();                                    
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v6.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
                                    
                                    if(count.getEvaluators() != null){                                        
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newCount);
                                    
                                }else if(scoringType instanceof generated.v5_1.dkf.CompletionTime){
                                    
                                    generated.v5_1.dkf.CompletionTime complTime = (generated.v5_1.dkf.CompletionTime)scoringType;
                                    
                                    generated.v6.dkf.CompletionTime newComplTime = new generated.v6.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(generated.v6.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if(complTime.getEvaluators() != null){                                        
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newComplTime);
                                    
                                }else if(scoringType instanceof generated.v5_1.dkf.ViolationTime){
                                    
                                    generated.v5_1.dkf.ViolationTime violationTime = (generated.v5_1.dkf.ViolationTime)scoringType;
                                    
                                    generated.v6.dkf.ViolationTime newViolationTime = new generated.v6.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(generated.v6.dkf.UnitsEnumType.fromValue(violationTime.getUnits().value()));
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
     * @return generated.v6.dkf.Nvpair - the new object
     */
    private static generated.v6.dkf.Nvpair convertNvpair(generated.v5_1.dkf.Nvpair nvPair) {
    	
    	generated.v6.dkf.Nvpair newNvpair = new generated.v6.dkf.Nvpair();
    	newNvpair.setName(nvPair.getName());
    	newNvpair.setValue(nvPair.getValue());
    	
    	return newNvpair;
    }
    
    /**
     * Convert a waypointref object to a new waypointref object.
     * 
     * @param waypointRef - the object to convert
     * @return generated.v6.dkf.WaypointRef - the new object
     */
    private static generated.v6.dkf.WaypointRef convertWaypointRef(generated.v5_1.dkf.WaypointRef waypointRef){
        
        generated.v6.dkf.WaypointRef newWaypoint = new generated.v6.dkf.WaypointRef();
        newWaypoint.setValue(waypointRef.getValue());
        newWaypoint.setDistance(waypointRef.getDistance());
        
        return newWaypoint;
    }
    
    /**
     * Begin the conversion of a v5.1 file to the current version by parsing the old file for the pedagogical config
     * schema object and convert it to a newer version.
     */
    @Override
	public UnmarshalledFile convertEMAPConfiguration(FileProxy pedConfigFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
    	
    	UnmarshalledFile uFile = parseFile(pedConfigFile, PREV_PEDCONFIG_SCHEMA_FILE, PEDCONFIG_ROOT, failOnFirstSchemaError);
    	generated.v5_1.ped.EMAP v5_1PedConfig = (generated.v5_1.ped.EMAP)uFile.getUnmarshalled();
    	
        return convertPedagogicalConfiguration(v5_1PedConfig, showCompletionDialog);
    }

    /**
     * Convert the previous pedagogical schema object to the current version of the course schema.
     * 
     * @param v5_1PedConfig - version of the config file to be converted 
     * @param showCompletionDialog - show the completion dialog
     * @return - the freshly converted config file
     */
    public UnmarshalledFile convertPedagogicalConfiguration(generated.v5_1.ped.EMAP v5_1PedConfig, boolean showCompletionDialog) {
    	generated.v6.ped.EMAP newPedConfig = new generated.v6.ped.EMAP();
    	
    	newPedConfig.setExample(convertExample(v5_1PedConfig.getExample()));
    	newPedConfig.setPractice(convertPractice(v5_1PedConfig.getPractice()));
    	newPedConfig.setRecall(convertRecall(v5_1PedConfig.getRecall()));
    	newPedConfig.setRule(convertRule(v5_1PedConfig.getRule()));
    	newPedConfig.setVersion(v5_1PedConfig.getVersion());
    	
    	ConversionWizardUtil_v2015_1_v2016_1 util = new ConversionWizardUtil_v2015_1_v2016_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertPedagogicalConfiguration(newPedConfig, showCompletionDialog);
	}
    
    /**
     * Helper function to convert the Rules of a pedagogy config to the next version
     * @param v5_1Rule - current version of the rules 
     * @return current version Rules
     */
	private generated.v6.ped.Rule convertRule(generated.v5_1.ped.Rule v5_1Rule) {
		generated.v6.ped.Rule newRule = new generated.v6.ped.Rule();
		generated.v6.ped.Attributes newAttributes = new generated.v6.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5_1.ped.Attribute attribute : v5_1Rule.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v6.ped.Attribute());
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
     * @param v5_1Rule - current version of the recall 
     * @return current version Recall
     */
	private generated.v6.ped.Recall convertRecall(generated.v5_1.ped.Recall v5_1Recall) {
		generated.v6.ped.Recall newRecall = new generated.v6.ped.Recall();
		generated.v6.ped.Attributes newAttributes = new generated.v6.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5_1.ped.Attribute attribute : v5_1Recall.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v6.ped.Attribute());
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
     * @param v5_1Practice - current version of the practice 
     * @return current version Practice
     */
	private generated.v6.ped.Practice convertPractice(generated.v5_1.ped.Practice v5_1Practice) {
		generated.v6.ped.Practice newPractice = new generated.v6.ped.Practice();
		generated.v6.ped.Attributes newAttributes = new generated.v6.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5_1.ped.Attribute attribute : v5_1Practice.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v6.ped.Attribute());
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
     * @param v5_1Example - current version of the example
     * @return current version example
     */
	private generated.v6.ped.Example convertExample(generated.v5_1.ped.Example v5_1Example) {
		generated.v6.ped.Example newExample = new generated.v6.ped.Example();
		generated.v6.ped.Attributes newAttributes = new generated.v6.ped.Attributes();
		int index = 0;
		
		//sets the attributes for the example
		for(generated.v5_1.ped.Attribute attribute : v5_1Example.getAttributes().getAttribute()){
			newAttributes.getAttribute().add(new generated.v6.ped.Attribute());
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
	 * @param v5_1metadataAttributes - current version's metadata
	 * @return current version metadata
	 */
	private generated.v6.ped.MetadataAttributes convertMetaDataAttributes(generated.v5_1.ped.MetadataAttributes v5_1metadataAttributes) {
		generated.v6.ped.MetadataAttributes newMetaAttributes = new generated.v6.ped.MetadataAttributes(); 
		int index = 0;
		
		for(generated.v5_1.ped.MetadataAttribute attribute : v5_1metadataAttributes.getMetadataAttribute()){
			newMetaAttributes.getMetadataAttribute().add(new generated.v6.ped.MetadataAttribute());
			newMetaAttributes.getMetadataAttribute().get(index).setValue(attribute.getValue());
			
			//Quadrant Specific attribute no longer exists in this version, add to issue list
			if(attribute.getIsQuadrantSpecific() != null){
				conversionIssueList.addIssue("Quadrant Specific Element");
			}
			index++;
		}
		
		return newMetaAttributes;
	}
	
    @Override
    public UnmarshalledFile convertMetadata(FileProxy metadataFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException {
        
        UnmarshalledFile uFile = parseFile(metadataFile, getPreviousMetadataSchemaFile(), getPreviousMetadataSchemaRoot(), failOnFirstSchemaError);
        generated.v5_1.metadata.Metadata v5_1metadata = (generated.v5_1.metadata.Metadata)uFile.getUnmarshalled();
        
        // Convert the version 5 course to the newest version and return it
        return convertMetadata(v5_1metadata, showCompletionDialog);  
    }
    
    
    /**
     * Convert the previous metadata schema object to a newer version of the metadata schema.
     * 
     * @param oldMetadata - the metadata schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     */
    public UnmarshalledFile convertMetadata(generated.v5_1.metadata.Metadata oldMetadata, boolean showCompletionDialog){
        generated.v6.metadata.Metadata newMetadata = new generated.v6.metadata.Metadata();
        
        newMetadata.setVersion(oldMetadata.getVersion());
        newMetadata.setMerrillQuadrant(oldMetadata.getMerrillQuadrant());
        newMetadata.setSimpleRef(oldMetadata.getSimpleRef());
        newMetadata.setTrainingAppRef(oldMetadata.getTrainingAppRef());
        newMetadata.setConcepts(convertConcepts(oldMetadata.getConcepts()));
        
        ConversionWizardUtil_v2015_1_v2016_1 util = new ConversionWizardUtil_v2015_1_v2016_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertMetadata(newMetadata, showCompletionDialog);
    }
    
    private static generated.v6.metadata.Metadata.Concepts convertConcepts(generated.v5_1.metadata.Metadata.Concepts oldConcepts) {
        generated.v6.metadata.Metadata.Concepts newConcepts = new generated.v6.metadata.Metadata.Concepts();
        
        for(generated.v5_1.metadata.Concept concept: oldConcepts.getConcept()){
            generated.v6.metadata.Concept newConcept = new generated.v6.metadata.Concept();
            newConcept.setAttributes(convertMetaDataAttributes(concept.getAttributes()));
            newConcept.setName(concept.getName());
            newConcepts.getConcept().add(newConcept);
        }
        
        return newConcepts;
    }
    
    private static generated.v6.metadata.Attributes convertMetaDataAttributes(generated.v5_1.metadata.Attributes oldAttributes) {
        generated.v6.metadata.Attributes newMetaAttributes = new generated.v6.metadata.Attributes();
        int index = 0;
        
        for(generated.v5_1.metadata.Attribute attribute : oldAttributes.getAttribute()){
            newMetaAttributes.getAttribute().add(new generated.v6.metadata.Attribute());
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
        generated.v5_1.course.TrainingApplicationWrapper v5_1trainingApp = (generated.v5_1.course.TrainingApplicationWrapper)uFile.getUnmarshalled();
        
        // Convert the version 5 course to the newest version and return it
        return convertTrainingApplicationRef(v5_1trainingApp, showCompletionDialog);  
    }
	
    /**
     * Convert the previous TrainingAppRef schema object to a newer version of the TrainingAppRef schema.
     * 
     * @param v5_1trainingAppRef - the TrainingAppRef schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     */
	public UnmarshalledFile convertTrainingApplicationRef(generated.v5_1.course.TrainingApplicationWrapper v5_1trainingAppRef, boolean showCompletionDialog){
        generated.v6.course.TrainingApplicationWrapper newTrainingAppRef = new generated.v6.course.TrainingApplicationWrapper();
        newTrainingAppRef.setDescription(v5_1trainingAppRef.getDescription());
        newTrainingAppRef.setVersion(v5_1trainingAppRef.getVersion());
        newTrainingAppRef.setTrainingApplication(convertTrainingApplication(v5_1trainingAppRef.getTrainingApplication()));
        
        
        ConversionWizardUtil_v2015_1_v2016_1 util = new ConversionWizardUtil_v2015_1_v2016_1();
        util.setConversionIssueList(conversionIssueList);
        return util.convertTrainingApplicationRef(newTrainingAppRef, showCompletionDialog);
    }
    
    private static generated.v6.course.TrainingApplication convertTrainingApplication(generated.v5_1.course.TrainingApplication trainApp){
        generated.v6.course.TrainingApplication newTrainApp = new generated.v6.course.TrainingApplication();
      
        newTrainApp.setTransitionName(trainApp.getTransitionName());
        
        generated.v6.course.DkfRef newDkfRef = new generated.v6.course.DkfRef();
        newDkfRef.setFile(trainApp.getDkfRef().getFile());
        newTrainApp.setDkfRef(newDkfRef);
        
        newTrainApp.setFinishedWhen(trainApp.getFinishedWhen());
        
        if(trainApp.getGuidance() != null){
            
            generated.v6.course.Guidance newGuidance = convertGuidance(trainApp.getGuidance());
            newTrainApp.setGuidance(newGuidance);
        }
        
        if (trainApp.getOptions() != null) {
            
            newTrainApp.setOptions(convertOptions(trainApp.getOptions()));
            
        }
        
        generated.v6.course.Interops newInterops = new generated.v6.course.Interops();               
        newTrainApp.setInterops(newInterops);
        
        for(generated.v5_1.course.Interop interop : trainApp.getInterops().getInterop()){
            
            generated.v6.course.Interop newInterop = new generated.v6.course.Interop();
            
            // Check to make sure the old vbs interop isn't being referenced since it has been replaced
            if(interop.getInteropImpl().equals(OLD_VBS_INTEROP)) {
                newInterop.setInteropImpl(NEW_VBS_INTEROP);
            } else {
                newInterop.setInteropImpl(interop.getInteropImpl());
            }
                                
            newInterop.setInteropInputs(new generated.v6.course.InteropInputs());
            
            Object interopObj = interop.getInteropInputs().getInteropInput();
            if(interopObj instanceof generated.v5_1.course.VBS2InteropInputs){
                
                generated.v5_1.course.VBS2InteropInputs vbs2 = (generated.v5_1.course.VBS2InteropInputs)interopObj;
                generated.v6.course.VBSInteropInputs newVbs = new generated.v6.course.VBSInteropInputs();
                
                generated.v6.course.VBSInteropInputs.LoadArgs newLoadArgs = new generated.v6.course.VBSInteropInputs.LoadArgs();
                newLoadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                newVbs.setLoadArgs(newLoadArgs);
                
                newInterop.getInteropInputs().setInteropInput(newVbs);
                
            }else if(interopObj instanceof generated.v5_1.course.DISInteropInputs){
                
                generated.v6.course.DISInteropInputs newDIS = new generated.v6.course.DISInteropInputs();
                newDIS.setLoadArgs(new generated.v6.course.DISInteropInputs.LoadArgs());
                
                newInterop.getInteropInputs().setInteropInput(newDIS);
                
            }else if(interopObj instanceof generated.v5_1.course.PowerPointInteropInputs){
                
                generated.v5_1.course.PowerPointInteropInputs ppt = (generated.v5_1.course.PowerPointInteropInputs)interopObj;
                generated.v6.course.PowerPointInteropInputs newPPT = new generated.v6.course.PowerPointInteropInputs();
                
                newPPT.setLoadArgs(new generated.v6.course.PowerPointInteropInputs.LoadArgs());
                
                newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                
                newInterop.getInteropInputs().setInteropInput(newPPT);
                
            }else if(interopObj instanceof generated.v5_1.course.TC3InteropInputs){
                
                generated.v5_1.course.TC3InteropInputs tc3 = (generated.v5_1.course.TC3InteropInputs)interopObj;
                generated.v6.course.TC3InteropInputs newTC3 = new generated.v6.course.TC3InteropInputs();
                
                newTC3.setLoadArgs(new generated.v6.course.TC3InteropInputs.LoadArgs());
                
                newTC3.getLoadArgs().setScenarioName(tc3.getLoadArgs().getScenarioName());
                
                newInterop.getInteropInputs().setInteropInput(newTC3);
                
            }else if(interopObj instanceof generated.v5_1.course.SCATTInteropInputs){
            
                generated.v6.course.SCATTInteropInputs newSCAT = new generated.v6.course.SCATTInteropInputs();            
                newSCAT.setLoadArgs(new generated.v6.course.SCATTInteropInputs.LoadArgs());
                
                newInterop.getInteropInputs().setInteropInput(newSCAT);
                
            }else if(interopObj instanceof generated.v5_1.course.CustomInteropInputs){
                
                generated.v5_1.course.CustomInteropInputs custom = (generated.v5_1.course.CustomInteropInputs)interopObj;
                generated.v6.course.CustomInteropInputs newCustom = new generated.v6.course.CustomInteropInputs();
                
                newCustom.setLoadArgs(new generated.v6.course.CustomInteropInputs.LoadArgs());
                
                for(generated.v5_1.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                    generated.v6.course.Nvpair newPair = new generated.v6.course.Nvpair();
                    newPair.setName(pair.getName());
                    newPair.setValue(pair.getValue());
                    newCustom.getLoadArgs().getNvpair().add(newPair);
                }
                
                newInterop.getInteropInputs().setInteropInput(newCustom);
                
                
            }else if(interopObj instanceof generated.v5_1.course.DETestbedInteropInputs){
                
                generated.v5_1.course.DETestbedInteropInputs deTestbed = (generated.v5_1.course.DETestbedInteropInputs)interopObj;
                generated.v6.course.DETestbedInteropInputs newDeTestbed = new generated.v6.course.DETestbedInteropInputs();
                
                generated.v6.course.DETestbedInteropInputs.LoadArgs args = new generated.v6.course.DETestbedInteropInputs.LoadArgs();
                args.setScenarioName(deTestbed.getLoadArgs().getScenarioName());
                newDeTestbed.setLoadArgs(args);
                
                newInterop.getInteropInputs().setInteropInput(newDeTestbed);
                
            } else if(interopObj instanceof generated.v5_1.course.SimpleExampleTAInteropInputs){
                generated.v5_1.course.SimpleExampleTAInteropInputs TAInputs = (generated.v5_1.course.SimpleExampleTAInteropInputs) interopObj;
                generated.v6.course.SimpleExampleTAInteropInputs newTAInputs = new generated.v6.course.SimpleExampleTAInteropInputs();
                generated.v6.course.SimpleExampleTAInteropInputs.LoadArgs args = new generated.v6.course.SimpleExampleTAInteropInputs.LoadArgs();
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
    
    private static generated.v6.course.TrainingApplication.Options convertOptions(generated.v5_1.course.TrainingApplication.Options oldOptions){
        generated.v6.course.TrainingApplication.Options newOptions = new generated.v6.course.TrainingApplication.Options();
        
        if (oldOptions.getDisableInstInterImpl() != null) {
            
            newOptions.setDisableInstInterImpl(generated.v6.course.BooleanEnum.fromValue(oldOptions.getDisableInstInterImpl().toString().toLowerCase()));
        }
        
        if (oldOptions.getShowAvatarInitially() != null) {
            
            generated.v6.course.ShowAvatarInitially newShowAvatarInitially = new generated.v6.course.ShowAvatarInitially();
            
            if (oldOptions.getShowAvatarInitially().getAvatarChoice() != null) {
                
                generated.v5_1.course.ShowAvatarInitially.MediaSemantics mediaSematics = oldOptions.getShowAvatarInitially().getAvatarChoice(); 
                generated.v6.course.ShowAvatarInitially.MediaSemantics newMediaSematics = new generated.v6.course.ShowAvatarInitially.MediaSemantics();
                
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
            generated.v5_1.dkf.Scenario scenario = createScenario();
            
            System.out.println("Scenario generated successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
