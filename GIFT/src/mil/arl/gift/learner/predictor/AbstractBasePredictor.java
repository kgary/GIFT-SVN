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
 * This abstract class is the base class for predictors.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractBasePredictor {
	
    protected AbstractClassifier classifier;

	/**
	 * Class constructor 
	 * 
	 * @param classifier - the classifier associated with this predictor
	 */
	public AbstractBasePredictor(AbstractClassifier classifier){
		this.classifier = classifier;
	}
	
	/**
	 * Return the classifier associated with this predictor
	 * 
	 * @return AbstractClassifier
	 */
	public AbstractClassifier getClassifier(){

		return classifier;
	}
	
	@Override
	public String toString(){
	    return "classifier = "+classifier;
	}

}
