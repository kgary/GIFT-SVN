/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorData;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.survey.score.AbstractScale;

/**
 * This is a very generic and simple classifier for the learner model.  It basically passes through any incoming data
 * that needs to be classified.  If the data already has state attribute value it will be used, otherwise there is no way
 * to generically classify data as of now.
 * 
 * @author mhoffman
 *
 */
public class GenericClassifier extends AbstractClassifier {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GenericClassifier.class);
    
    /** the name of the state attribute being classified */
    private LearnerStateAttributeNameEnum stateAttribute;
    
    /** the learner's attribute state */
    protected LearnerStateAttribute state;
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param stateAttribute - the state attribute being assigned a value
     */
    public GenericClassifier(LearnerStateAttributeNameEnum stateAttribute){
        
        if(stateAttribute == null){
            throw new IllegalArgumentException("The state attribute can't be null.");
        }
        this.stateAttribute = stateAttribute;
    }

    @Override
    public Object getState() {
        return state;
    }

    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return stateAttribute;
    }

    @Override
    public Object getCurrentData() {
        return null;
    }

    @Override
    public boolean updateState(AbstractSensorData sensorData) {
        
        //there is no generic way to classify sensor data, therefore no logic goes here
        
        return false;
    }

    @Override
    public boolean updateState(TaskAssessment performanceAssessment) {
        
        //there is no generic way to classify performance assessment data, therefore no logic goes here
        
        return false;
    }

    @Override
    public boolean updateState(AbstractScale surveyResult) {
        
        if(surveyResult.getAttribute() == stateAttribute){
            
            state = new LearnerStateAttribute(
                    stateAttribute, 
                    surveyResult.getValue(), 
                    surveyResult.getTimeStamp().getTime(), 
                    surveyResult.getValue(), 
                    surveyResult.getTimeStamp().getTime(), 
                    surveyResult.getValue(), 
                    surveyResult.getTimeStamp().getTime());
            
            if(logger.isInfoEnabled()){
                logger.info("Current State was updated to "+state+" because of survey score result of "+surveyResult);
            }
            
            return true;            
        }
        
        return false;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[GenericClassifier: ");
        sb.append(" state = ").append(getState());
        sb.append(", value = ").append(getCurrentData());
        sb.append("]");
        return sb.toString();
    }

}
