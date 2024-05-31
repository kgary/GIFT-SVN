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
 * 
 * @author mhoffman
 *
 */
public class TaskPerformanceStatePredictor extends AbstractPredictor {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TaskPerformanceStatePredictor.class);
    private static boolean isDebug = logger.isDebugEnabled();
    
    /**
     * Class constructor
     * 
     * @param classifier - the classifier feeding this predictor
     */
    public TaskPerformanceStatePredictor(AbstractClassifier classifier){
        super(classifier);
    }
    
    @Override
    public boolean updateState(){
        
        boolean changed = false;
        
        if(isDebug){
            logger.debug("Received update state call");
        }
        
        //TODO: there should be a concept state predictor, for now just pass the changes through - this logic is done in performance state classifier as well right now
        
        return changed;
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
