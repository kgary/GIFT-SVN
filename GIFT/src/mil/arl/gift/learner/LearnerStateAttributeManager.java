/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.learner.clusterer.AbstractClassifier;
import mil.arl.gift.learner.predictor.AbstractBasePredictor;
import mil.arl.gift.learner.predictor.AbstractPredictor;
import mil.arl.gift.learner.predictor.AbstractTemporalPredictor;

/**
 * The learner state attribute manager is responsible for managing the classification and prediction of a
 * learner state attribute current and next values.
 * 
 * @author mhoffman
 *
 */
public class LearnerStateAttributeManager {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerStateAttributeManager.class);
    
    /** the learner state attribute (e.g. Motivation) being tracked in this manager */
    private LearnerStateAttributeNameEnum attribute;

    /** classifies the current learner state */
    private AbstractClassifier classifier;
    
    /** predicts the next learner state */
    private AbstractBasePredictor predictor;

    /**
     * Class constructor
     * 
     * @param attribute - the learner state attribute (e.g. Motivation) being tracked in this manager.  Can be null if the classifier and
     * predictors are being used for performance assessment learner state and not cognitive or affective learner state.
     * @param classifier - classifier to use for a learner state attribute.  Can't be null.
     * @param predictor - predictor to use for a learner state attribute. Can't be null.
     */
	public LearnerStateAttributeManager(LearnerStateAttributeNameEnum attribute, AbstractClassifier classifier, AbstractBasePredictor predictor){
	    
	    this.attribute = attribute;
	    
	    if(classifier == null){
	        throw new IllegalArgumentException("The classifier can't be null.");
	    }
		this.classifier = classifier;
		
        if(predictor == null){
            throw new IllegalArgumentException("The predictor can't be null.");
        }
		this.predictor = predictor;
	}
	
	/**
	 * Update the learner's state information based upon the new sensor data.
	 * 
	 * @param data - data to classifier and predict on for learner state
	 * @return boolean - whether the state attribute value changed with the provided data
	 */
	public boolean updateState(Object data){
		
		boolean currentStateChanged = false;
		boolean nextStateChanged = false;
		
		//
		// Apply data to classification and prediction logic
		//
		if(data instanceof FilteredSensorData){
			
		    currentStateChanged = classifier.updateState((FilteredSensorData)data);
		    
			if(predictor instanceof AbstractTemporalPredictor){
				
				nextStateChanged = ((AbstractTemporalPredictor)predictor).updateState(((FilteredSensorData)data).getElapsedTime());
				logger.info("Provided filtered sensor data for prediction");

			}else{
				logger.error("Only a temporal predictor is supported for filtered sensor data -> predictor = "+predictor+", FilteredSensorData = "+data);
			}
			
		}else if(data instanceof TaskAssessment){
		    
		    currentStateChanged = classifier.updateState((TaskAssessment)data);
			
			if(predictor instanceof AbstractPredictor){
				nextStateChanged = ((AbstractPredictor)predictor).updateState();
				logger.info("Provided a concept assessment for prediction");

			}else{
				logger.error("The wrong predictor was provided for concept assessment data -> predictor = "+predictor+", TaskAssessment = "+data);
			}
			
		}else if(data instanceof AbstractScale){
		    //survey score scale	
		    
		    currentStateChanged = classifier.updateState((AbstractScale)data);
		    
		}else if(data instanceof LMSCourseRecord){
		    //a new LMS course record
		    
		    currentStateChanged = classifier.updateState((LMSCourseRecord)data);
		    
		}else if(data instanceof TrainingAppState){
		    //a new Training App game state
		    
		    currentStateChanged = classifier.updateState((TrainingAppState)data);
		
		}else if(data instanceof GradedScoreNode){
            //a new Graded Score Node
            
            currentStateChanged = classifier.updateState((GradedScoreNode)data);
        
        }else{
		
			logger.error("Received unhandled data = "+data+" when updating state.");
		}
		
		
		return currentStateChanged || nextStateChanged;
	}
	
    /**
     * Notification that a new domain session has started
     */
    public void domainSessionStarted(){        
        classifier.domainSessionStarted();
    }
    
    /**
     * Notification that a new knowledge session (DKF) has started
     */
    public void knowledgeSessionStarted(){
        classifier.knowledgeSessionStarted();
    }
    
    /**
     * Notification that a knowledge session (DKF) has completed
     */
    public void knowledgeSessionCompleted(){
        classifier.knowledgeSessionCompleted();
    }
    
	/**
	 * Return the current learner state
	 * 
	 * @return contains the classifier state object.  Can be null if the classifier hasn't created a state update yet.
	 */
	public Object getState(){
		return classifier.getState();
	}
	
	/**
	 * Return the learner state attribute value being managed here.
	 * 
	 * @return the enumerated learner state attribute this manager is managing state for.  Shouldn't be null.
	 */
	public LearnerStateAttributeNameEnum getAttribute(){
	    return classifier.getAttribute();
	}
	
	/**
     * Set the course concepts used in the current course.
     * @param courseConcepts can be null and can contain no course concepts.
	 */
	public void setCourseConcepts(generated.course.Concepts courseConcepts) {
	    classifier.setCourseConcepts(courseConcepts);
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[LearnerStateAttributeManager:");
	    sb.append(" attribute = ").append(attribute);
	    sb.append(", classifier = ").append(classifier);
	    sb.append(", predictor = ").append(predictor);
	    sb.append("]");
		
		return sb.toString();
	}
}
