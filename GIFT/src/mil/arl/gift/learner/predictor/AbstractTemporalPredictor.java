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
 * This abstract class should be extended by predictors that need temporal information
 * to predict the next learner state for a learner state attribute.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractTemporalPredictor extends AbstractBasePredictor{
	
	/**
	 * Class constructor 
	 * 
	 * @param classifier - set classifier
	 */
	public AbstractTemporalPredictor(AbstractClassifier classifier){
		super(classifier);
	}
	
	/**
	 * Update the next learner state
	 * 
	 * @param elapsedTime - time elapsed since the predictor was started
	 * @return boolean - whether the next learner state was updated
	 */
	public abstract boolean updateState(double elapsedTime);
}
