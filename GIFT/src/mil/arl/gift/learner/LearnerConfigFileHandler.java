/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.learner.Classifier;
import generated.learner.Input;
import generated.learner.LearnerConfiguration;
import generated.learner.Property;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.course.LearnerFileValidationException;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.learner.clusterer.ClassifierConfiguration;

/**
 * This class will parse the learner configuration file which dictates where
 * (i.e. which class) each type of input data (e.g. sensor, performance
 * assessment) is handled in order to produce learner states.
 *
 * @author mhoffman
 */
public class LearnerConfigFileHandler extends AbstractSchemaHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerConfigFileHandler.class);
    
    /** contains information about the learner configuration parsed by this class */
    protected LearnerConfiguration learnerConfiguration;
    
    /** 
     * The following maps should be the same size.  Each input is expected to get a
     * translator, classifier, and predictor.  1 input -> (1 translator, 1 classifier, 1 predictor).
     */
    private Map<String, Class<?>> inputNameToTranslator = new HashMap<>();
    private Map<String, ClassifierConfiguration> inputNameToClassifier = new HashMap<>();
    private Map<String, Class<?>> inputNameToPredictor = new HashMap<>();
    
    /** Maintain a list of configured inputs from the learner configuration file */
    private List<Input> configuredInputs = new ArrayList<>();

    /**
     * Class constructor - set attribute(s)
     *
     * @param learnerConfigXmlContent - the contents of a learner configuration file (can't be null)
     * @param learnerSchemaFile - the learner configuration schema file name. If null, the default schema will be used.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws DetailedException if there was a problem parsing and validating the learner configuration
     */
    public LearnerConfigFileHandler(String learnerConfigXmlContent, File learnerSchemaFile, boolean failOnFirstSchemaError) throws DetailedException {
        super(learnerSchemaFile == null ? LEARNER_SCHEMA_FILE : learnerSchemaFile);
    	
    	if(learnerConfigXmlContent == null) {
    	    throw new IllegalArgumentException("The learner config file can't be null");
    	}
    	
    	try {
        	UnmarshalledFile uFile = parseAndValidate(LearnerConfiguration.class, learnerConfigXmlContent, failOnFirstSchemaError);
        	learnerConfiguration = (LearnerConfiguration) uFile.getUnmarshalled();
        	configureLearnerComponents();
        } catch (Exception e) {
            throw new DetailedException("Failed to parse and validate the learner configuration against the schema and GIFT logic",
                    e.getMessage(),
                    e);
        } 	
    }

    /**
     * Class constructor - set attribute(s)
     *
     * @param learnerConfigFile - the learner configuration file to parse.  Can't be null. 
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws LearnerFileValidationException if there was a problem parsing and validating the learner configuration file
     */
    public LearnerConfigFileHandler(FileProxy learnerConfigFile, boolean failOnFirstSchemaError) throws LearnerFileValidationException {
        super(LEARNER_SCHEMA_FILE);

        if (learnerConfigFile == null) {
            throw new IllegalArgumentException("The learner config file can't be null.");
        }

    	try {
        	UnmarshalledFile uFile = parseAndValidate(LearnerConfiguration.class, learnerConfigFile.getInputStream(), failOnFirstSchemaError);
        	learnerConfiguration = (LearnerConfiguration) uFile.getUnmarshalled();
        	configureLearnerComponents();
        } catch (Throwable e) {
            throw new LearnerFileValidationException("Failed to parse and validate the learner configuration file against the schema and GIFT logic.",
                    e.getMessage(),
                    learnerConfigFile.getFileId(),
                    e);
        } 	
    }
    
    /**
     * Gets the translator class for a specified input in the learnerconfiguration file.
     * 
     * @param inputName - The name of the input, cannot be null.
     * @return Class - returns the translator class for the specified input.  Can return null if the name isn't found.
     */
    public Class<?> getTranslator(String inputName) {
        return inputNameToTranslator.get(inputName);
    }
    
    /**
     * Gets the predictor class for a specified input in the learnerconfiguration file.
     * 
     * @param inputName - The name of the input, cannot be null.
     * @return Class - returns the predictor class for the specified input.  Can return null if the name isn't found.
     */
    public Class<?> getPredictor(String inputName) {
        return inputNameToPredictor.get(inputName);
    }
    
    /**
     * Gets the classifier configuration for a specified input in the learner configuration file.
     * 
     * @param inputName - The name of the input, cannot be null.
     * @return ClassifierConfiguration - returns the classifier configuration class for the specified input.  Can return null if the name isn't found.
     */
    public ClassifierConfiguration getClassifier(String inputName) {
        return inputNameToClassifier.get(inputName);
    }

    /**
     * Read the configuration file contents and generate learner components
     * (e.g. translators, classifiers and predictors)
     *
     * @throws DetailedException Thrown when there is a problem configuring
     * the learner
     */
    public void configureLearnerComponents() throws DetailedException {
        buildLearnerComponents();   
        logger.info("Successfully configured learner components using configuration file.");
    }

    /**
     * Build the learner components by analyzing the content of the learner
     * config.
     * @throws DetailedException if there was a problem building the learner data pipeline
     */
    private void buildLearnerComponents() throws DetailedException{

        for (Input input : learnerConfiguration.getInputs().getInput()) {

            
            // Enforce that the input names are unique.  This is because we store a mapping of input names to 
            // learnerstateattributemanagers (ie pipelines) and each input gets its own unique learnerstateattributemanager.
            // We also store a unique mapping of input name to it's corresponding translator, predictor, and classifier.  
            if (inputNameAlreadyExists(input.getName())) {
            
                throw new ConfigurationException("Input with name (" +input.getName()+ ") has already been configured.",
                        "Input names must be unique, please check the learnerconfiguration.xml for duplicate input names.",
                        null);
            }
            
            ClassifierConfiguration classifierConfig = buildClassifier(input.getClassifier());

            //
            // check that the classes actually exists
            //
            String translatorClassName = input.getTranslator().getTranslatorImpl();
            Class<?> translatorClazz = checkClass(translatorClassName);

            String predictorClassName = input.getPredictor().getPredictorImpl();
            Class<?> predictorClazz = checkClass(predictorClassName);
           
            // This defines the pipeline for the input (translator -> classifier -> predictor)\
            // Each input instantiates it's own pipeline.
            inputNameToTranslator.put(input.getName(),  translatorClazz);
            inputNameToPredictor.put(input.getName(),  predictorClazz);
            inputNameToClassifier.put(input.getName(),  classifierConfig);
            
            // Cache off the list of inputs that are defined in the configuration file.
            configuredInputs.add(input);
            
        }

        if(learnerConfiguration.getInputs().getInput().isEmpty()){
            throw new ConfigurationException("There are no inputs defined in the learner configuration.",
                    "The learner configuration must specify at least 1 data pipeline.", null);
        }
    }
    
    /**
     * Checks the existing inputList
     *
     * @param lConfig - the learner config content
     * @return boolean - if there were no issues and the configuration completed
     * successfully
     */
    private boolean inputNameAlreadyExists(String inputName) {
        
        boolean nameFound = false;
        
        for (int x=0; x < configuredInputs.size(); x++) {
            Input input = configuredInputs.get(x);
            if (inputName.equals(input.getName())) {
                nameFound = true;
                break;
            }
        }
        return nameFound;
    }
    
    
    /**
     * Retrieve a list of the inputs that are defined in the leanerconfiguration file.
     *
     * @return List<Input> - Return list of inputs that are defined in the learnerconfiguration file.
     */
    public List<Input> getInputs() {
        return configuredInputs;
    }

    /**
     * Build a classifier component of the learner
     *
     * @param classifier
     * @return ClassifierConfiguration
     * @throws DetailedException if there was a problem building the classifier
     */
    private ClassifierConfiguration buildClassifier(Classifier classifier) throws DetailedException {

        ClassifierConfiguration config = null;

        if (classifier != null) {

            Map<String, String> properties = new HashMap<String, String>();

            if (classifier.getProperties() != null) {
                for (Property property : classifier.getProperties().getProperty()) {
                    //a property element

                    properties.put(property.getName(), property.getValue());
                }
            }

            String classifierClassName = classifier.getClassifierImpl();
            Class<?> classifierClazz = checkClass(classifierClassName);
            config = new ClassifierConfiguration(classifierClazz, properties);

        } else {
            throw new ConfigurationException("There is no classifier for the input",
                    "The classifier needs to be specified.",
                    null);
        }

        return config;
    }

    /**
     * Check that the class named exists and can be instantiated
     *
     * @param className - the class name to check
     * @return Class<?> - the class for the class name, null if there was an
     * error
     * @throws DetailedException if the class could not be found or the class has no public constructors
     */
    private static Class<?> checkClass(String className) throws DetailedException {

        Class<?> clazz = null;

        try {
            //check that classes exists
            clazz = Class.forName(PackageUtil.getRoot() + "." + className);

            //check that each class can be instantiated
            Constructor<?>[] constructors = clazz.getConstructors();

            if (constructors.length == 0) {
                throw new ConfigurationException("There are no constructors for class named " + className,
                        "In order to use an implementation class in a learner configuration that class must exist.",
                        null);
            }

        } catch (ClassNotFoundException cnfe) {
            throw new DetailedException("Found a non-existant class while parsing input xml element",
                    "The class named "+className+" doesn't exist.",
                    cnfe);
        }

        return clazz;

    }
}
