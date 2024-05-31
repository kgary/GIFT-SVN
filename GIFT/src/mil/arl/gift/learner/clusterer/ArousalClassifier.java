/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.sensor.AbstractSensorData;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.ArousalLevelEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.survey.score.AbstractScale;

/**
 * This class is responsible for classifying the sensor data it receives in order
 * to determine what the current learner state attribute value is for arousal.
 * 
 * @author mhoffman
 *
 */
public class ArousalClassifier extends AbstractClassifier {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ArousalClassifier.class);

    /** the learner's current arousal state */
	private LearnerStateAttribute state;
	
	private FilteredSensorData lastSensorData;
	
	private static final SensorAttributeNameEnum SENSOR_ATTRIBUTE = SensorAttributeNameEnum.HUMIDITY;
    private static final LearnerStateAttributeNameEnum STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.AROUSAL; 
	
    private static final AbstractEnum LOW_VALUE = ArousalLevelEnum.LOW;
    private static final AbstractEnum MED_VALUE = ArousalLevelEnum.MEDIUM;
    private static final AbstractEnum HIGH_VALUE = ArousalLevelEnum.HIGH;
    private static final AbstractEnum UKNOWN_VALUE = ArousalLevelEnum.UNKNOWN;
	
	@Override
	public LearnerStateAttribute getState(){
		return state;
	}
	
	@Override
	public Object getCurrentData(){
		return lastSensorData;
	}
	
	@Override
	public boolean updateState(AbstractSensorData data){
		
        boolean updated = false;
        
        if(data instanceof FilteredSensorData){
            
            lastSensorData = (FilteredSensorData)data;
            
            //determine if the current state needs to change
            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> normalizedValues = sensorTranslator.getValues(((FilteredSensorData)data).getAttributeValues());
            
            //get humidity normalized value
            AbstractSensorAttributeValue value = normalizedValues.get(SENSOR_ATTRIBUTE);
            if(value != null && value.isNumber()){
                
                Double humidityValue = value.getNumber().doubleValue();
                
                AbstractEnum aLevel = LOW_VALUE;
                if(humidityValue > 0.7){
                    aLevel = HIGH_VALUE;
                }else if(humidityValue > 0.5){
                    aLevel = MED_VALUE;
                }
                
                //update state (if necessary)
                if(state == null || aLevel != state.getShortTerm()){

                    state = new LearnerStateAttribute(STATE_ATTRIBUTE, aLevel, UKNOWN_VALUE, UKNOWN_VALUE);
                    updated = true;
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Current State was updated to "+state+" because of humidity value of "+humidityValue);
                    }
                }
            }
        }
        
        return updated;
	}	

    @Override
    public boolean updateState(AbstractScale surveyResult) {
        
        if(surveyResult.getAttribute() == STATE_ATTRIBUTE){
            
            state = new LearnerStateAttribute(STATE_ATTRIBUTE, surveyResult.getValue(), UKNOWN_VALUE, UKNOWN_VALUE);  
            
            if(logger.isInfoEnabled()){
                logger.info("Current State was updated to "+state+" because of survey score result of "+surveyResult);
            }
            
            return true;            
        }
        
        return false;
    }    

    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return STATE_ATTRIBUTE;
    }
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[ArousalClassifier:");
	    sb.append(" current state = ").append(getState());
	    sb.append(", last data = ").append(getCurrentData());
	    sb.append("]");
		return sb.toString();
	}

	
}
