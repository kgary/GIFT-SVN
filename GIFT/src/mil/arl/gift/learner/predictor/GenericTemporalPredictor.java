/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.predictor;


import mil.arl.gift.learner.clusterer.AbstractClassifier;

/**
 * This is a generic temporal predictor class that can be used
 * for any common temporal predictor logic.  Currently it is used
 * so that pipelines in the learnerconfiguration.xml file can be setup
 * for sensors (like Kinect) and be allowed to specify a predictor that
 * essentially does nothing and makes no prediction.
 * 
 * @author nblomberg
 *
 */
public class GenericTemporalPredictor extends AbstractTemporalPredictor {

	
	public GenericTemporalPredictor(AbstractClassifier classifier){
		super(classifier);
	}
	
	@Override
	public boolean updateState(double elapsedTime){
	    
	    boolean updated = false;

	    // $TODO$ nblomberg
	    // Add logic here if needed to update state, otherwise this
	    // currently does not attempt to predict the next learner state.
	    
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
