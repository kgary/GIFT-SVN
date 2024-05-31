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

import mil.arl.gift.learner.clusterer.AbstractClassifier;

/**
 * This class predicts the next learner arousal state
 * 
 * @author mhoffman
 *
 */
public class ArousalPredictor extends AbstractTemporalPredictor {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ArousalPredictor.class);
    private static boolean isDebug = logger.isDebugEnabled();
	
	/**
	 * Define the thresholds for increasing or decreasing slope classification
	 */
	protected static final double INCREASING_SLOPE_MIN = 0.017;
	protected static final double DECREASING_SLOPE_MIN = -0.017;
	
	/** the previous update's arousal information */
	protected double prevArousalValue = -1.0;
	protected double prevElapsedTime = -1.0;
	
	public ArousalPredictor(AbstractClassifier classifier){
		super(classifier);
	}
	
	@Override
	public boolean updateState(double elapsedTime){
		return updateState(classifier.getCurrentData(), elapsedTime);
	}
	
	/**
	 * Update the next learner state by calculating the slope of the previous and current values.
	 * 
	 * @param currentClassifierData
	 * @param elapsedTime
	 * @return boolean - whether the next learner state was updated
	 */
	private boolean updateState(Object currentClassifierData, double elapsedTime){
		
        boolean updated = false;
        
        if(isDebug){
            logger.debug("Received update with data = "+currentClassifierData);
        }
        //TODO: create prediction logic here
        
        return updated;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[").append(this.getClass().getName()).append(":");
	    sb.append(" classifier = ").append(getClassifier());
	    sb.append("]");
		return sb.toString();
	}
}
