/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.util.Map;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.sensor.AbstractSensorData;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.learner.clusterer.data.AbstractSensorTranslator;

/**
 * This class classifies sensor data in a learner state attribute
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractClassifier {
	
	//TODO: once we get more clarification on classifiers there could be more abstraction to combine sensors and performance assessment
	//TODO: this could be a map of translators for the classifier, mapped by input type (e.g. sensor type)
    protected AbstractSensorTranslator sensorTranslator;
    
    /**
     * Set the sensor translator used by this classifier
     * 
     * @param translator - the translator responsible for normalizing sensor data
     */
    public void setTranslator(AbstractSensorTranslator translator){
    	sensorTranslator = translator;
    }
    
    /**
     * Return the sensor translator used by this classifier
     * 
     * @return AbstractSensorTranslator
     */
    public AbstractSensorTranslator getTranslator(){
    	return sensorTranslator;
    }
    
    /**
     * Retrieve the attributes for this class using the property values provided.
     * 
     * @param properties - properties to configure this classifier with
     * @throws ConfigurationException - thrown if a severe error happens when trying to apply the properties to this classifier
     */
    public void configureByProperties(Map<String, String> properties) throws ConfigurationException{
        
    }

	/**
	 * Return the classifier's learner state object
	 * 
	 * @return the classifier's learner state object, a subset of a learner state (e.g. cognitive attribute such as Knowledge or Skill).
	 * Can be null if the classifier hasn't updated its state attribute value yet.
	 */
	public abstract Object getState();
	
	/**
	 * Return the learner state attribute type being classified.
	 * 
	 * @return the enumerated learner state attribute this classifier is updating values for.  Shouldn't be null.
	 */
	public abstract LearnerStateAttributeNameEnum getAttribute();
	
	/**
	 * Return the classifier's current data
	 * 
	 * @return the data used to classify learner state attribute (e.g. AbstractAssessment, double, FilteredSensorData)
	 */
	public abstract Object getCurrentData();
	
	/**
	 * Update the learner state by classifying the provided sensor data
	 * 
	 * @param sensorData - incoming sensor data to process using this classifier
	 * @return boolean - whether the learner state was changed
	 */
	public boolean updateState(AbstractSensorData sensorData){
	    return false;
	}
	
	/**
     * Update the learner state by classifying the provided performance assessment data
     * 
     * @param performanceAssessment - incoming performance assessment data to process using this classifier
     * @return boolean - whether the learner state was changed
     */
	public boolean updateState(TaskAssessment performanceAssessment){
	    return false;
	}
	
	/**
     * Update the learner state by classifying the provided survey score data
     * 
     * @param surveyResult - incoming survey results to process using this classifier
     * @return boolean - whether the learner state was changed
     */
	public boolean updateState(AbstractScale surveyResult){
	    return false;
	}
	
	/**
	 * Update the learner state by classifying the provided Learner Record
	 * 
	 * @param courseRecord information about the learners execution of a course
	 * @return boolean - whether the learner state was changed
	 */
	public boolean updateState(LMSCourseRecord courseRecord){
	    return false;
	}
	
	/**
     * Update the learner state by classifying the provided graded score node
     * 
     * @param gradedScoreNode incoming graded score node to process using this classifier
     * @return boolean - whether the learner state was changed
     */
    public boolean updateState(GradedScoreNode gradedScoreNode){
        return false;
    }
	
	/**
	 * Update the learner state by classifying the provided Training Application state.
	 * 
	 * @param trainingAppState a game state message payload
	 * @return boolean - whether the learner state was changed
	 */
	public boolean updateState(TrainingAppState trainingAppState){
	    return false;
	}
	
	/**
	 * Notification that a new domain session has started
	 */
	public void domainSessionStarted(){
	    
	}
	
	/**
	 * Notification that a new knowledge session (DKF) has started
	 */
	public void knowledgeSessionStarted(){
	    
	}
	
	/**
     * Notification that a knowledge session (DKF) has completed
     */
    public void knowledgeSessionCompleted(){
        
    }
    
    /**
     * Set the course concepts used in the current course.
     * @param courseConcepts can be null and can contain no course concepts.
     */
    public void setCourseConcepts(generated.course.Concepts courseConcepts) {
        // nothing to do
    }
	
	@Override
	public String toString(){
	    
	    if(sensorTranslator != null){
	        return "sensorTranslator = "+sensorTranslator;
	    }else{
	        return Constants.EMPTY;
	    }
	}
}
