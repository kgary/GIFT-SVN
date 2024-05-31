/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.PowerPointState;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition assesses whether the learner has dwelled too long on a particular PowerPoint slide.  
 * One use case example is when a learner might be day-dreaming on a particular slide or not on pace with
 * the time allocated for the show, the learner may need an instructional strategy to help bring them back on track.
 * 
 * @author mhoffman
 *
 */
public class PowerPointOverDwellCondition extends AbstractPowerPointDwellCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(PowerPointOverDwellCondition.class);
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "PowerPointOverDwell.GIFT Domain condition description.html"), "Powerpoint Overdwell");
    
    /**
     * Default constructor - required for authoring logic
     */
    public PowerPointOverDwellCondition(){
        
    }
        
    /**
     * Class constructor - configure condition with input from domain knowledge
     * 
     * @param input configuration parameters for this condition
     */
    public PowerPointOverDwellCondition(generated.dkf.PowerPointDwellCondition input){
        super(input);

    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {
        
        AssessmentLevelEnum level = null;
        
        MessageTypeEnum mType = message.getMessageType();
        
        if(mType == MessageTypeEnum.POWERPOINT_STATE){            
            
            PowerPointState state = (PowerPointState)message.getPayload();
            if(logger.isInfoEnabled()){
                logger.info("Received "+mType+" message with state = "+state+", checking against dwell assessment logic");
            }
            
            if(state.getErrorMessage() != null){
                logger.error("Received an error message from the PowerPoint state of "+state);
                return false;
            }
            
            double timeInSeconds = getSlideTime(state.getSlideIndex());
            
            //schedule timer task
            slideTimer.schedule(timeInSeconds, state.getSlideIndex());
            
            //always at expectation until the time has been reached for a slide
            level = AssessmentLevelEnum.AT_EXPECTATION;
            assessmentExplanation = null;
            
            updateAssessment(level);
            
            lastState = state;
            return true;
            
        }else if(mType == MessageTypeEnum.STOP_FREEZE){
            
            //signal that this condition is completed because the show has finished.
            this.conditionCompleted();
        }
        
        return false;
    }
    
    @Override
    protected void timeExpired(int slideIndex){
        
        //time was allowed to expire, condition has been violated
        if(logger.isInfoEnabled()){
            logger.info("Over-Dwell time reached on slide index of "+slideIndex);
        }
        timeViolation(slideIndex);
        updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
        
        // set the explanation for this below expectation assessment 
        double slideTimeInSeconds = getSlideTime(slideIndex);
        StringBuffer sb = new StringBuffer();
        int index = slideIndex + 1;
        sb.append("Over ").append(slideTimeInSeconds).append(" seconds was spent on slide ").append(index);
        assessmentExplanation = sb.toString();
        
        sendAssessmentEvent();
    }
    
    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean canComplete() {
        return true;
    }
}
