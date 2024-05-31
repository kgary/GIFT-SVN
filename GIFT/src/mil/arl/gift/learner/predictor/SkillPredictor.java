/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.predictor;

import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.learner.clusterer.AbstractClassifier;
import mil.arl.gift.learner.clusterer.SkillClassifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class predicts the next learner state for Skill.
 * 
 * @author mhoffman
 *
 */
public class SkillPredictor extends AbstractPredictor {

	/** instance of the logger */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(SkillPredictor.class);
	
	/**
	 * Class constructor
	 * 
	 * @param classifier - the classifier feeding this predictor
	 */
	public SkillPredictor(AbstractClassifier classifier){
		super(classifier);
	}
	
    @Override
	public boolean updateState(){
		
		boolean updated = false;
		
		//TODO: use the classifier's value(s) to detect trends in order to set the predicted value of the learner state
		@SuppressWarnings("unused")
        LMSCourseRecord courseRecord = (LMSCourseRecord)((SkillClassifier)classifier).getCurrentData();	
		
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
