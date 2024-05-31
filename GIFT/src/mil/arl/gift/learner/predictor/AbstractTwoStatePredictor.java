/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.learner.clusterer.AbstractClassifier;

/**
 * This class predicts the next learner attribute state and classifies the state into
 * one of two possible learner state attribute value states.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractTwoStatePredictor extends AbstractTemporalPredictor {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractTwoStatePredictor.class);
	
    /**
     * Define the various zone thresholds
     */
    //Two zone prediction
    private static final double HIGH_TO_LOW_TREND_THRESHOLD = 0.67;
    private static final double LOW_TO_HIGH_THRESHOLD = 0.5;
    private static final double LOW_TO_HIGH_TREND_THRESHOLD = 0.33;
        
    /**
     *    High
     *             --------------------------------------- High to Low Trend (0.67)
     * -------------------------- Low to High (0.50)
     *      Low    --------------------------------------- Low to High Trend (0.33)
     *      
     */ 
	
	/**
     * Define the thresholds for increasing or decreasing slope classification
     */
    protected static final double INCREASING_SLOPE_MIN = 0.017;
    protected static final double DECREASING_SLOPE_MIN = -0.017;
	
    /** the learner attribute being predicted upon */
    private LearnerStateAttributeNameEnum stateAttribute; 

    /** various values for the learner state attribute (see constructor) */
    private AbstractEnum lowLevelValue;
    private AbstractEnum highLevelValue;
    private AbstractEnum unknownLevelValue;
	
    /** the previous classifier provided raw value for this attribute */
	private double prevValue;
	
	/** the elapsed time of the previous classification of this attribute */
	private double prevElapsedTime;
	
	/**
	 * Class constructor - set attributes
	 * 
     * @param stateAttribute - the state attribute being assigned a value
     * @param lowLevelValue - the state attribute value for when the sensor attribute value is considered in the low state
     * @param highLevelValue - the state attribute value for when the sensor attribute value is considered in the high state
     * @param unknownLevelValue - the state attribute value for when the sensor attribute value is considered in the unknown state
	 * @param classifier - the classifier feeding this predictor
	 */
	public AbstractTwoStatePredictor(LearnerStateAttributeNameEnum stateAttribute, 
            AbstractEnum lowLevelValue, AbstractEnum highLevelValue, AbstractEnum unknownLevelValue, AbstractClassifier classifier){
		super(classifier);
		
		this.stateAttribute = stateAttribute;
		this.lowLevelValue = lowLevelValue;
		this.highLevelValue = highLevelValue;
		this.unknownLevelValue = unknownLevelValue;
	}
	
    @Override
    public boolean updateState(double elapsedTime){
        return updateState((Double)classifier.getCurrentData(), elapsedTime);
    }
	
	/**
	 * Update the next learner state by calculating the slope of the previous and current values.
	 * 
	 * TODO: For noisy and/or high-frequency updates, we should consider more points than just the current and previous point).
	 * 
	 * @param currentValue the current value from the clusterer
	 * @param elapsedTime amount of time that has elapsed since the last update
	 * @return boolean - whether the next learner state was updated
	 */
	protected boolean updateState(double currentValue, double elapsedTime){
		
		boolean updated = false;
		
		//calculate slope of previous and current state 
		if(prevValue != -1.0){
			
			if(elapsedTime > prevElapsedTime){
				//avoid those annoying divide by zeros...
				
				double slope = (currentValue - prevValue) / (elapsedTime - prevElapsedTime);
				
				LearnerStateAttribute thisState = (LearnerStateAttribute)classifier.getState();
				AbstractEnum currValue = thisState.getPredicted();
				AbstractEnum newValue;

                //
                //determine zone and then trend
                //
                if(slope == 0.0){
                    //unknown
                    newValue = unknownLevelValue;

                    
                }else if(currentValue < LOW_TO_HIGH_TREND_THRESHOLD){
                    //trend doesn't matter
                    
                    newValue = lowLevelValue;
                    
                }else if(currentValue < LOW_TO_HIGH_THRESHOLD){
                    
                    if(slope > INCREASING_SLOPE_MIN){
                        //trending upward
                        newValue = highLevelValue;
                    }else{
                        newValue = lowLevelValue;
                    }
                    
                }else if(currentValue < HIGH_TO_LOW_TREND_THRESHOLD){
                    
                    if(slope < DECREASING_SLOPE_MIN){
                        //trending downward
                        newValue = lowLevelValue;

                    }else{
                        newValue = highLevelValue;

                    }

                }else{
                    //trend doesn't matter
                    newValue = highLevelValue;
                }
				
				if(currValue != newValue){
				    currValue = newValue;
				    thisState.setPredicted(newValue);
					updated = true;
					
					logger.info("The state was changed to "+newValue+" because of "+stateAttribute+" attribute value of "+currentValue+" and slope "+slope);
				}
			}
			
			
		}
		
		prevValue = currentValue;
		prevElapsedTime = elapsedTime;
		
		return updated;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append(super.toString());
		return sb.toString();
	}
}
