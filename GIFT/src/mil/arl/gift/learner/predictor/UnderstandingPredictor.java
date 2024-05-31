/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.predictor;

import java.util.List;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.UnderstandingLevelEnum;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.learner.clusterer.AbstractClassifier;
import mil.arl.gift.learner.clusterer.UnderstandingClassifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class predicts the next learner state for Understanding.
 * 
 * @author mhoffman
 *
 */
public class UnderstandingPredictor extends AbstractPredictor {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(UnderstandingPredictor.class);
	
	/**
	 * Class constructor
	 * 
	 * @param classifier - the classifier feeding this predictor
	 */
	public UnderstandingPredictor(AbstractClassifier classifier){
		super(classifier);
	}
	
    @Override
	public boolean updateState(){
		
		boolean updated = false;
		
		AbstractAssessment assessment = (AbstractAssessment)((UnderstandingClassifier)classifier).getCurrentData();
		
		if(assessment instanceof TaskAssessment){
		    List<ConceptAssessment> conceptAssessments = ((TaskAssessment)assessment).getConceptAssessments();
	        
	        //analyze concept results for trend
	        UnderstandingLevelEnum nextLevel = null;
	        LearnerStateAttribute state = (LearnerStateAttribute)classifier.getState();
	        UnderstandingLevelEnum currLevel = (UnderstandingLevelEnum)state.getPredicted();
	        
	        if(assessment.getAssessmentLevel() != AssessmentLevelEnum.UNKNOWN){

	            int score = 0, delta;
	            for(ConceptAssessment ass : conceptAssessments){
	                
	                delta = 0; //reset
	                
	                if(ass.getAssessmentLevel() == AssessmentLevelEnum.ABOVE_EXPECTATION){
	                    //trend is positive
	                    delta = 1;
	                }else if(ass.getAssessmentLevel() == AssessmentLevelEnum.BELOW_EXPECTATION){
	                    //trend is negative
	                    delta = -1;
	                }else{
	                    //unanswered or skipped question
	                    continue;
	                }
	                
	                //update sequential score
	                score += delta;
	                
	                if(score >= 3){
	                    //reached the high level
	                    nextLevel = UnderstandingLevelEnum.HIGH;
	                    break;
	                }else if(score <= -3){
	                    //reached the low level
	                    nextLevel = UnderstandingLevelEnum.LOW;
	                    break;
	                }else{
	                    
	                    if(delta == 1 && score <= 0){
	                        //changed from negative trend by answering a question right
	                        break;
	                    }else if(delta == -1 && score >= 0){
	                        //changed from positive trend by answering a question wrong
	                        break;
	                    }
	                }
	            }//end for
	        }
	        
	        if(nextLevel != null && nextLevel != currLevel){
	            //the level has changed (unknown->low, unknown->high, low->high, high->low)
	            currLevel = nextLevel;
	            updated = true;
	            
	            logger.info("A new next learner state was created based on an understanding level change, "+nextLevel);
	        }
	        
		}//end if
		
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
