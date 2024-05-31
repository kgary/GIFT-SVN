/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner;

import generated.learner.Input;
import generated.learner.Sensor;
import generated.learner.TrainingAppState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;

/**
 * Contains all of the mappings that define what parameters of a Learner
 * State Interpreter can be paired with each other. More specifically:
 * 
 * 1.) Each translator is allowed to be paired with a subset of all possible
 * data sources (Sensors and Training Applications).
 * 2.) The learner state of the interpreter determines which classifiers are
 * allowable.
 * 3.) The learner state of the interpreter determines which predictors are
 * allowable.
 * 
 * @author elafave
 *
 */
public class LearnerConfigurationMaps {

	static private LearnerConfigurationMaps instance;
	
	private HashMap<TranslatorTypeEnum, ArrayList<SensorTypeEnum>> translatorTypeToSensorTypesMap = new HashMap<TranslatorTypeEnum, ArrayList<SensorTypeEnum>>();

	private HashMap<TranslatorTypeEnum, ArrayList<MessageTypeEnum>> translatorTypeToTrainingApplicationTypesMap = new HashMap<TranslatorTypeEnum, ArrayList<MessageTypeEnum>>();
	
	private HashMap<LearnerStateAttributeNameEnum, ArrayList<ClassifierTypeEnum>> learnerStateToClassifierTypesMap = new HashMap<LearnerStateAttributeNameEnum, ArrayList<ClassifierTypeEnum>>();
	
	private HashMap<LearnerStateAttributeNameEnum, ArrayList<PredictorTypeEnum>> learnerStateToPredictorTypesMap = new HashMap<LearnerStateAttributeNameEnum, ArrayList<PredictorTypeEnum>>();
	
	static public LearnerConfigurationMaps getInstance() {
		if(instance == null) {
			instance = new LearnerConfigurationMaps();
		}
		return instance;
	}
	
	private LearnerConfigurationMaps() {
		populateTranslatorTypeToSensorTypesMap();
		populateTranslatorTypeToApplicationTypesMap();
		populateLearnerStateMaps();
	}
	
	/**
	 * Examines the Learner State to Classifier map to determine which Learner
	 * States you can create a Learner State Interpreter for.
	 * 
	 * @return The set of Learner States that a Learner State Interpreter can
	 * be created for.
	 */
	public Set<LearnerStateAttributeNameEnum> getLearnerStates() {
		return learnerStateToClassifierTypesMap.keySet();
	}
	
	/**
	 * Examines the Translator to Sensor Type map to determine which sensors we
	 * support.
	 * @return All of the Sensors that can be used in a Learner Configuration.
	 */
	public HashSet<SensorTypeEnum> getSensorTypes() {
		HashSet<SensorTypeEnum> sensorTypes = new HashSet<SensorTypeEnum>();
		
		Set<TranslatorTypeEnum> keys = translatorTypeToSensorTypesMap.keySet();
		for(TranslatorTypeEnum key : keys) {
			ArrayList<SensorTypeEnum> value = translatorTypeToSensorTypesMap.get(key);
			sensorTypes.addAll(value);
		}
		
		return sensorTypes;
	}
	
	/**
	 * Examines the Translator to Training Application Type map to determine
	 * which training applications we support.
	 * @return All of the Training Applications that can be used in a Learner
	 * Configuration.
	 */
	public HashSet<MessageTypeEnum> getTrainingApplicationTypes() {
		HashSet<MessageTypeEnum> trainingApplicationTypes = new HashSet<MessageTypeEnum>();
		
		Set<TranslatorTypeEnum> keys = translatorTypeToTrainingApplicationTypesMap.keySet();
		for(TranslatorTypeEnum key : keys) {
			ArrayList<MessageTypeEnum> value = translatorTypeToTrainingApplicationTypesMap.get(key);
			trainingApplicationTypes.addAll(value);
		}
		
		return trainingApplicationTypes;
	}
	
	/**
	 * Examines the Translator to Data Source maps to determine which
	 * translators support all of the supplied data sources.
	 * @param dataSources Collection of data sources, each element must be a
	 * Sensor or TrainingAppState.
	 * @return Collection of Translators that can support all of the supplied
	 * data sources.
	 */
	public ArrayList<TranslatorTypeEnum> getTranslatorTypes(List<Serializable> dataSources) {
		//Extract the collection of sensor types and training applications
		//these data sources represent.
		ArrayList<SensorTypeEnum> sensorTypes = new ArrayList<SensorTypeEnum>();
		ArrayList<MessageTypeEnum> trainingApps = new ArrayList<MessageTypeEnum>();
		for(Serializable dataSource : dataSources) {
			if(dataSource instanceof Sensor) {
				String value = ((Sensor)dataSource).getType();
				SensorTypeEnum sensorTypeEnum = SensorTypeEnum.valueOf(value);
				sensorTypes.add(sensorTypeEnum);
			} else if(dataSource instanceof TrainingAppState) {
				String value = ((TrainingAppState)dataSource).getType();
				MessageTypeEnum trainingApplication = MessageTypeEnum.valueOf(value);
				trainingApps.add(trainingApplication);
			}
		}
		
		//Determine which translator types support every data source supplied.
		ArrayList<TranslatorTypeEnum> acceptableValues = new ArrayList<TranslatorTypeEnum>();
		TranslatorTypeEnum[] allTranslatorTypes = TranslatorTypeEnum.values();
		for(TranslatorTypeEnum translatorType : allTranslatorTypes) {
			if(doesTranslatorTypeSupportDataSources(translatorType, sensorTypes, trainingApps)) {
				acceptableValues.add(translatorType);
			}
		}
		
		return acceptableValues;
	}
	
	/**
	 * Examines the Translator to Data Source maps to determine if the supplied
	 * translator can support the supplied data sources.
	 * @param translatorType Translator in question.
	 * @param sensorTypes Sensors the Translator would need to support.
	 * @param trainingApps Training Applications the Translator would need to 
	 * support.
	 * @return True if the supplied Translator supports the supplied data
	 * sources.
	 */
	public boolean doesTranslatorTypeSupportDataSources(TranslatorTypeEnum translatorType, List<SensorTypeEnum> sensorTypes, List<MessageTypeEnum> trainingApps) {
		for(SensorTypeEnum sensorType : sensorTypes) {
			if(!translatorTypeToSensorTypesMap.get(translatorType).contains(sensorType)) {
				return false;
			}
		}
		
		for(MessageTypeEnum trainingApp : trainingApps) {
			if(!translatorTypeToTrainingApplicationTypesMap.get(translatorType).contains(trainingApp)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Examines the Learner State to Predictor map to determine which
	 * Predictors work with the supplied Learner State.
	 * @param learnerState Learner State of the returned Predictors.
	 * @return Collection of Predictors that work with the supplied Learner State.
	 */
	public ArrayList<PredictorTypeEnum> getPredictorTypes(LearnerStateAttributeNameEnum learnerState) {
		return learnerStateToPredictorTypesMap.get(learnerState);
	}
	
	/**
	 * Examines the Learner State to Predictor to determine which Learner
	 * State the supplied Predictor works with and then to determine all of the
	 * Predictors that work with that Learner State.
	 * @param predictorType Predictor that works with a certain Learner State.
	 * @return Collection of Predictors that work with that same Learner State.
	 */
	public ArrayList<PredictorTypeEnum> getPredictorTypes(PredictorTypeEnum predictorType) {
		Set<LearnerStateAttributeNameEnum> learnerStates = learnerStateToPredictorTypesMap.keySet();
		for(LearnerStateAttributeNameEnum learnerState : learnerStates) {
			ArrayList<PredictorTypeEnum> predictorTypes = learnerStateToPredictorTypesMap.get(learnerState);
			if(predictorTypes.contains(predictorType)) {
				return predictorTypes;
			}
		}
		return null;
	}
	
	/**
	 * Examines the Learner State to Classifier map to determine which
	 * Classifiers work with the supplied Learner State.
	 * @param learnerState Learner State of the returned Classifiers.
	 * @return Collection of Classifiers that work with the supplied Learner State.
	 */
	public ArrayList<ClassifierTypeEnum> getClassifierTypes(LearnerStateAttributeNameEnum learnerState) {
		return learnerStateToClassifierTypesMap.get(learnerState);
	}
	
	/**
	 * Examines the Learner State to Classifier to determine which Learner
	 * State the supplied Classifier works with and then to determine all of the
	 * Classifiers that work with that Learner State.
	 * @param classifierType Classifier that works with a certain Learner State.
	 * @return Collection of Classifiers that work with that same Learner State.
	 */
	public ArrayList<ClassifierTypeEnum> getClassifierTypes(ClassifierTypeEnum classifierType) {
		Set<LearnerStateAttributeNameEnum> learnerStates = learnerStateToClassifierTypesMap.keySet();
		for(LearnerStateAttributeNameEnum learnerState : learnerStates) {
			ArrayList<ClassifierTypeEnum> classifierTypes = learnerStateToClassifierTypesMap.get(learnerState);
			if(classifierTypes.contains(classifierType)) {
				return classifierTypes;
			}
		}
		return null;
	}
	
	/**
	 * Uses the supplied interpreter's classifier and predictor along with the
	 * Learner State to Classifier and Learner State to Predictor maps to
	 * perform a reverse lookup to determine which Learner State the supplied
	 * interpreter is for.
	 * 
	 * NOTE: Ideally in the future the Input class will have a Learner State
	 * attribute, making this reverse lookup unnecessary.
	 * 
	 * NOTE: This requires that the Learner State to Classifier and Learner
	 * State to Predictor maps to yeild the same Learner State, otherwise
	 * null will be returned.
	 * 
	 * @param interpreter Learner State Interpreter whose Learner State needs
	 * to be derived based on its Classifier and Predictor.
	 * @return The Learner State associated with the supplied Learner State
	 * Interpreter.
	 */
	public LearnerStateAttributeNameEnum getLearnerState(Input interpreter) {
		String classifierImpl = interpreter.getClassifier().getClassifierImpl();
		ClassifierTypeEnum classifierType = ClassifierTypeEnum.fromClassifierImpl(classifierImpl);
		LearnerStateAttributeNameEnum learnerStateBasedOnClassifier = getLearnerState(classifierType);

		String predictorImpl = interpreter.getPredictor().getPredictorImpl();
		PredictorTypeEnum predictorType = PredictorTypeEnum.fromPredictorImpl(predictorImpl);
		LearnerStateAttributeNameEnum learnerStateBasedOnPredictor = getLearnerState(predictorType);
		
		if(learnerStateBasedOnClassifier == null ||
				learnerStateBasedOnPredictor == null ||
				learnerStateBasedOnClassifier != learnerStateBasedOnPredictor) {
			return null;
		} else {
			return learnerStateBasedOnClassifier;
		}
	}
	
	/**
	 * Uses the supplied Classifier and the Learner State to Classifier map to
	 * perform a reverse lookup to determine which Learner State the Classifier
	 * works with.
	 * 
	 * @param classifierType Classifier whose Learner State needs to be
	 * derived.
	 * @return The Learner State associated with the supplied Classifier.
	 */
	public LearnerStateAttributeNameEnum getLearnerState(ClassifierTypeEnum classifierType) {
		Set<LearnerStateAttributeNameEnum> learnerStates = learnerStateToClassifierTypesMap.keySet();
		for(LearnerStateAttributeNameEnum learnerState : learnerStates) {
			if(learnerStateToClassifierTypesMap.get(learnerState).contains(classifierType)) {
				return learnerState;
			}
		}

		return null;
	}
	
	/**
	 * Uses the supplied Predictor and the Learner State to Predictor map to
	 * perform a reverse lookup to determine which Learner State the Predictor
	 * works with.
	 * 
	 * @param predictorType Predictor whose Learner State needs to be
	 * derived.
	 * @return The Learner State associated with the supplied Predictor.
	 */
	public LearnerStateAttributeNameEnum getLearnerState(PredictorTypeEnum predictorType) {
		Set<LearnerStateAttributeNameEnum> learnerStates = learnerStateToPredictorTypesMap.keySet();
		for(LearnerStateAttributeNameEnum learnerState : learnerStates) {
			if(learnerStateToPredictorTypesMap.get(learnerState).contains(predictorType)) {
				return learnerState;
			}
		}
		
		return null;
	}

	private void populateTranslatorTypeToSensorTypesMap() {
		ArrayList<SensorTypeEnum> sensorTypes;
		
		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.DEFAULT mappings.
		///////////////////////////////////////////////////////////////////////
		sensorTypes = new ArrayList<SensorTypeEnum>();
		sensorTypes.addAll(SensorTypeEnum.VALUES());
		translatorTypeToSensorTypesMap.put(TranslatorTypeEnum.DEFAULT, sensorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.MOUSE mappings.
		///////////////////////////////////////////////////////////////////////
		sensorTypes = new ArrayList<SensorTypeEnum>();
		sensorTypes.add(SensorTypeEnum.MOUSE_TH);
		sensorTypes.add(SensorTypeEnum.MOUSE_TH_SURROGATE);
		translatorTypeToSensorTypesMap.put(TranslatorTypeEnum.MOUSE, sensorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.SELF_ASSESSMENT mappings.
		///////////////////////////////////////////////////////////////////////
		sensorTypes = new ArrayList<SensorTypeEnum>();
		sensorTypes.add(SensorTypeEnum.SELF_ASSESSMENT);
		translatorTypeToSensorTypesMap.put(TranslatorTypeEnum.SELF_ASSESSMENT, sensorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.SINE_WAVE mappings.
		///////////////////////////////////////////////////////////////////////
		sensorTypes = new ArrayList<SensorTypeEnum>();
		sensorTypes.add(SensorTypeEnum.SINE_WAVE);
		translatorTypeToSensorTypesMap.put(TranslatorTypeEnum.SINE_WAVE, sensorTypes);
	}
	
	private void populateTranslatorTypeToApplicationTypesMap() {
		ArrayList<MessageTypeEnum> trainingApps;

		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.DEFAULT mappings.
		///////////////////////////////////////////////////////////////////////
		trainingApps = new ArrayList<MessageTypeEnum>();
		trainingApps.add(MessageTypeEnum.COLLISION);
		trainingApps.add(MessageTypeEnum.DETONATION);
		trainingApps.add(MessageTypeEnum.ENTITY_STATE);
		trainingApps.add(MessageTypeEnum.GENERIC_JSON_STATE);
		trainingApps.add(MessageTypeEnum.POWERPOINT_STATE);
		trainingApps.add(MessageTypeEnum.RIFLE_SHOT_MESSAGE);
		trainingApps.add(MessageTypeEnum.SIMPLE_EXAMPLE_STATE);
		trainingApps.add(MessageTypeEnum.WEAPON_FIRE);
		translatorTypeToTrainingApplicationTypesMap.put(TranslatorTypeEnum.DEFAULT, trainingApps);

		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.MOUSE mappings.
		///////////////////////////////////////////////////////////////////////
		trainingApps = new ArrayList<MessageTypeEnum>();
		translatorTypeToTrainingApplicationTypesMap.put(TranslatorTypeEnum.MOUSE, trainingApps);

		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.SELF_ASSESSMENT mappings.
		///////////////////////////////////////////////////////////////////////
		trainingApps = new ArrayList<MessageTypeEnum>();
		translatorTypeToTrainingApplicationTypesMap.put(TranslatorTypeEnum.SELF_ASSESSMENT, trainingApps);

		///////////////////////////////////////////////////////////////////////
		//Set up the TranslatorTypeEnum.SINE_WAVE mappings.
		///////////////////////////////////////////////////////////////////////
		trainingApps = new ArrayList<MessageTypeEnum>();
		translatorTypeToTrainingApplicationTypesMap.put(TranslatorTypeEnum.SINE_WAVE, trainingApps);
	}
	
	private void populateLearnerStateMaps() {
		ArrayList<ClassifierTypeEnum> classifierTypes;
		ArrayList<PredictorTypeEnum> predictorTypes;
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.ANXIOUS mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.ANXIOUS);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.GENERIC_PREDICTOR);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.ANXIOUS, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.ANXIOUS, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.AROUSAL mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.AROUSAL);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.AROUSAL);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.AROUSAL, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.AROUSAL, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.ENG_CONCENTRATION mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.ENGAGEMENT_CONCENTRATION);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.GENERIC_TEMPORAL_PREDICTOR);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.ENG_CONCENTRATION, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.ENG_CONCENTRATION, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.ENGAGEMENT mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.ENGAGEMENT_TWO_STATE);
		classifierTypes.add(ClassifierTypeEnum.ENGAGEMENT_THREE_STATE);
		
		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.ENGAGEMENT_TWO_STATE);
		predictorTypes.add(PredictorTypeEnum.ENGAGEMENT_THREE_STATE);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.ENGAGEMENT, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.ENGAGEMENT, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.KNOWLEDGE mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.EXPERTISE);
		classifierTypes.add(ClassifierTypeEnum.FUZZY_ART);
		classifierTypes.add(ClassifierTypeEnum.KNOWLEDGE);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.EXPERTISE);
		predictorTypes.add(PredictorTypeEnum.KNOWLEDGE);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.KNOWLEDGE, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.KNOWLEDGE, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.MOTIVATION mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.MOTIVATION);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.MOTIVATION);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.MOTIVATION, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.MOTIVATION, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.SKILL mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.SKILL);
		classifierTypes.add(ClassifierTypeEnum.GENERIC);
		classifierTypes.add(ClassifierTypeEnum.GENERIC_THREE_STATE);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.SKILL);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.SKILL, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.SKILL, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.OFFTASK mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.TASK_PERFORMANCE_STATE);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.TASK_PERFORMANCE_STATE);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.OFFTASK, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.OFFTASK, predictorTypes);
		
		///////////////////////////////////////////////////////////////////////
		//Set up the LearnerStateAttributeNameEnum.UNDERSTANDING mappings.
		///////////////////////////////////////////////////////////////////////
		classifierTypes = new ArrayList<ClassifierTypeEnum>();
		classifierTypes.add(ClassifierTypeEnum.UNDERSTANDING);

		predictorTypes = new ArrayList<PredictorTypeEnum>();
		predictorTypes.add(PredictorTypeEnum.UNDERSTANDING);
		
		learnerStateToClassifierTypesMap.put(LearnerStateAttributeNameEnum.UNDERSTANDING, classifierTypes);
		learnerStateToPredictorTypesMap.put(LearnerStateAttributeNameEnum.UNDERSTANDING, predictorTypes);
	}
}
