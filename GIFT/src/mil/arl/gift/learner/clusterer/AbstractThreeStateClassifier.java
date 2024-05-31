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

import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorData;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.survey.score.AbstractScale;

/**
 * This class is responsible for classifying the sensor data it receives in order
 * to determine what the current learner state attribute value is for a particular learner state attribute.
 * 
 * Zones:
 * 
 *           1.0  ***************************************
 *           					Level 3
 *           
 *                 ************ Level 2 Threshold *******
 *                 
 *                 				Level 2
 *                 
 *                 ************ Level 1 Threshold *******
 *                 
 *                 				Level 1
 *                 
 *            0.0  **************************************
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractThreeStateClassifier extends AbstractClassifier{

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractThreeStateClassifier.class);
    
    /** where the 3 states are separated on a 0 to 1 scale */
    protected static final double DEFAULT_LOW_MED_THRESHOLD = 0.33;
    protected static final double DEFAULT_MED_HIGH_THRESHOLD = 0.66;
		
	/** the classification thresholds */
	private double levelOneThreshold = DEFAULT_LOW_MED_THRESHOLD;
	private double levelTwoThreshold = DEFAULT_MED_HIGH_THRESHOLD;
	
	/** current sensor attribute value */
	private double currentValue;
	
	/** the learner's attribute state */
    protected LearnerStateAttribute state;
    
    /** inputs from the implementation class (see constructor) */
    private SensorAttributeNameEnum sensorAttribute;
    private LearnerStateAttributeNameEnum stateAttribute; 
    private AbstractEnum lowLevelValue;
    private AbstractEnum mediumLevelValue;
    private AbstractEnum highLevelValue;
    private AbstractEnum unknownLevelValue;
	
    /**
     * Class Constructor - set attributes
     * 
     * @param sensorAttribute - the sensor attribute whose values are being classified
     * @param stateAttribute - the state attribute being assigned a value
     * @param lowLevelValue - the state attribute value for when the sensor attribute value is considered in the low state
     * @param mediumLevelValue - the state attribute value for when the sensor attribute value is considered in the medium state
     * @param highLevelValue - the state attribute value for when the sensor attribute value is considered in the high state
     * @param unknownLevelValue - the state attribute value for when the sensor attribute value is considered in the unknown state
     */
	public AbstractThreeStateClassifier(SensorAttributeNameEnum sensorAttribute, LearnerStateAttributeNameEnum stateAttribute, 
	        AbstractEnum lowLevelValue, AbstractEnum mediumLevelValue, AbstractEnum highLevelValue, AbstractEnum unknownLevelValue){
	    this(stateAttribute, lowLevelValue, mediumLevelValue, highLevelValue, unknownLevelValue);
	
	    if(sensorAttribute == null){
	        throw new IllegalArgumentException("The sensor attribute can't be null.");
	    }
	    
	    this.sensorAttribute = sensorAttribute;
	}
	
    /**
     * Class Constructor - set attributes
     * 
     * @param stateAttribute - the state attribute being assigned a value
     * @param lowLevelValue - the state attribute value for when the sensor attribute value is considered in the low state
     * @param mediumLevelValue - the state attribute value for when the sensor attribute value is considered in the medium state
     * @param highLevelValue - the state attribute value for when the sensor attribute value is considered in the high state
     * @param unknownLevelValue - the state attribute value for when the sensor attribute value is considered in the unknown state
     */
    public AbstractThreeStateClassifier(LearnerStateAttributeNameEnum stateAttribute, 
            AbstractEnum lowLevelValue, AbstractEnum mediumLevelValue, AbstractEnum highLevelValue, AbstractEnum unknownLevelValue){
    
        if(stateAttribute == null){
            throw new IllegalArgumentException("The state attribute can't be null.");
        }
        this.stateAttribute = stateAttribute;
        
        if(lowLevelValue == null){
            throw new IllegalArgumentException("The low level value enum can't be null.");
        }
        this.lowLevelValue = lowLevelValue;
        
        if(mediumLevelValue == null){
            throw new IllegalArgumentException("The medium level value enum can't be null.");
        }
        this.mediumLevelValue = mediumLevelValue;
        
        if(highLevelValue == null){
            throw new IllegalArgumentException("The high level value enum can't be null.");
        }
        this.highLevelValue = highLevelValue;
        
        if(unknownLevelValue == null){
            throw new IllegalArgumentException("The unknown level value enum can't be null.");
        }
        this.unknownLevelValue = unknownLevelValue;
    }
	
    @Override
    public LearnerStateAttribute getState(){
        return state;
    }
    
    @Override
    public Object getCurrentData(){
        return currentValue;
    }
	
	@Override
	public boolean updateState(AbstractSensorData data){
		
		boolean updated = false;
		
		if(data instanceof FilteredSensorData){
			
			//determine if the current state needs to change
			Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> normalizedValues = sensorTranslator.getValues(((FilteredSensorData)data).getAttributeValues());
			
			//get sensor attribute normalized value
			Double value = normalizedValues.get(sensorAttribute).getNumber().doubleValue();
			if(value != null){
				
				//update local value
			    currentValue = value;
				
				AbstractEnum level;
				
				//determine attribute value level
				if(value < levelOneThreshold){
				    level = lowLevelValue;
				}else if(value < levelTwoThreshold){
				    level = mediumLevelValue;
				}else{
				    level = highLevelValue;
				}
				
				//update state (if necessary)
				if(state == null || !level.equals(state.getShortTerm())){

				    state = new LearnerStateAttribute(stateAttribute, level, unknownLevelValue, unknownLevelValue);
					updated = true;
					
					logger.info("Current State was updated to "+state+" because of attribute value of "+value);
				}
			}
		}
		
		return updated;
	}

    @Override
    public boolean updateState(AbstractScale surveyResult) {
        
        if(surveyResult.getAttribute() == stateAttribute){
            
            state = new LearnerStateAttribute(stateAttribute, surveyResult.getValue(), unknownLevelValue, unknownLevelValue);            
            logger.info("Current State was updated to "+state+" because of survey score result of "+surveyResult);
            
            return true;            
        }
        
        return false;
    }
    
    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return stateAttribute;
    }
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append(" state = ").append(getState());
	    sb.append(", value = ").append(getCurrentData());
		return sb.toString();
	}
}
