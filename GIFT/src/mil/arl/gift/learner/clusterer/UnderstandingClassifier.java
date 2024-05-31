/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.UnderstandingLevelEnum;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.survey.score.AbstractScale;

/**
 * This class is responsible for classifying the assessment it receives in order
 * to determine what the current learner state attribute value is for understanding.
 * 
 * @author mhoffman
 *
 */
public class UnderstandingClassifier extends AbstractClassifier {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(UnderstandingClassifier.class);

    /** the learner's engagement state */
	private LearnerStateAttribute state;
	
	/** the last assessment provided to this classifier */
	private AbstractAssessment assessment;
	
    private static final LearnerStateAttributeNameEnum STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.UNDERSTANDING; 
    
    private static final AbstractEnum LOW_VALUE = UnderstandingLevelEnum.LOW;
    private static final AbstractEnum MED_VALUE = UnderstandingLevelEnum.MEDIUM;
    private static final AbstractEnum HIGH_VALUE = UnderstandingLevelEnum.HIGH;
    private static final AbstractEnum UKNOWN_VALUE = UnderstandingLevelEnum.UNKNOWN;
	
	/**
	 * Return the  learner understanding state
	 * 
	 * @return LearnerState
	 */
	@Override
	public LearnerStateAttribute getState(){
		return state;
	}
	
	/**
	 * Return the current understanding data
	 * 
	 * @return Object
	 */
	@Override
	public Object getCurrentData(){
		return assessment;
	}
	
	@Override
	public boolean updateState(TaskAssessment data){
		
		boolean updated = false;
				
        logger.info("Received a Performance Assessment update of "+data);
        
        if(state == null || assessment.getAssessmentLevel() != ((AbstractAssessment)data).getAssessmentLevel()){
            
            AbstractEnum uLevel = UKNOWN_VALUE;
            AssessmentLevelEnum currentAssessment = ((AbstractAssessment)data).getAssessmentLevel();
            if(currentAssessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
                uLevel = HIGH_VALUE;
            }else if(currentAssessment == AssessmentLevelEnum.AT_EXPECTATION){
                uLevel = MED_VALUE;
            }else if(currentAssessment != AssessmentLevelEnum.UNKNOWN){
                uLevel = LOW_VALUE;
            }
            
            state = new LearnerStateAttribute(STATE_ATTRIBUTE, uLevel, UKNOWN_VALUE, UKNOWN_VALUE);
            updated = true;
            
            logger.info("The concept assessment update caused a change in the current learner state to "+state);
        }
			
        assessment = data;
		
		return updated;
	}
    
    @Override
    public boolean updateState(AbstractScale surveyResult) {
        
        if(surveyResult.getAttribute() == STATE_ATTRIBUTE){
            
            state = new LearnerStateAttribute(STATE_ATTRIBUTE, surveyResult.getValue(), UKNOWN_VALUE, UKNOWN_VALUE);            
            logger.info("Current State was updated to "+state+" because of survey score result of "+surveyResult);
            
            return true;            
        }
        
        return false;
    }
    
    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return STATE_ATTRIBUTE;
    }
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[UnderstandingClassifier:");
	    sb.append(" state = ").append(getState());
	    sb.append(", previous assessment = ").append(getCurrentData());
	    sb.append("]");
		return sb.toString();
	}
	
}
