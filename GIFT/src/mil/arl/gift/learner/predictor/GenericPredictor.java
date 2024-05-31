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
 * This is the simplest predictor that merely uses the state values set by the classifier.
 *  
 * @author mhoffman
 *
 */
public class GenericPredictor extends AbstractBasePredictor {

    /**
     * Class constructor - set attribute(s).
     * 
     * @param classifier - the classifier feeding this predictor.
     */
    public GenericPredictor(AbstractClassifier classifier) {
        super(classifier);

    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[GenericPredictor:");
        sb.append(" ").append(super.toString());
        sb.append("]");
        return sb.toString();
    }

}
