/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import generated.dkf.PowerPointDwellCondition.Slides.Slide;

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
 * This condition assesses whether the learner has dwelled long enough on a particular PowerPoint slide.  
 * One use case example is when a particular slide has useful information that either needs to be read or have multimedia
 * played and the learner should stay on that slide for minimum amount of time.
 * 
 * @author mhoffman
 *
 */
public class PowerPointUnderDwellCondition extends AbstractPowerPointDwellCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(PowerPointUnderDwellCondition.class);
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "PowerPointUnderDwell.GIFT Domain condition description.html"), "PowerPoint Underdwell");
    
    /**
     * Default constructor - required for authoring logic
     */
    public PowerPointUnderDwellCondition(){
        
    }
    
    /**
     * Class constructor - configure condition with input from domain knowledge
     * 
     * @param input configuration parameters for this condition
     */
    public PowerPointUnderDwellCondition(generated.dkf.PowerPointDwellCondition input){
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
            
            double timeInSeconds;
            int slideIndex = state.getSlideIndex();
            Slide slideInfo = slideIndexMap.get(slideIndex);
            if(slideInfo != null){
                timeInSeconds = slideInfo.getTimeInSeconds();
            }else{
                timeInSeconds = input.getDefault().getTimeInSeconds();                
            }
            
            //schedule timer task
            int previousSlideIndex = slideTimer.getSlideIndex();
            if(slideTimer.schedule(timeInSeconds, slideIndex)){              
                //a previous slide timer task was canceled, meaning the learner violated the condition
                
                level = handleViolation(previousSlideIndex);
                
            }else{
            
                if(logger.isInfoEnabled()){
                    logger.info("User successfully dwelled enough on slide "+previousSlideIndex+".");
                }
                level = AssessmentLevelEnum.AT_EXPECTATION;
                assessmentExplanation = null;
            }
            
            lastState = state;
            
            if(level != null){
                updateAssessment(level);
                return true;
            }            

        }else if(mType == MessageTypeEnum.STOP_FREEZE){
            
            if(logger.isInfoEnabled()){
                logger.info("Received "+mType+" message , checking if all slides were visited");
            }
            
            level = handlePowerPointEnd();

            if(level != null){
                return true;
            }           
        }
        
        return false;
    }
    
    /**
     * Handle when an underdwell rule is violated for a slide.
     * 
     * @param violatedSlideIndex the slide index (1 based) that was not visited long enough by the learner.
     * @return AssessmentLevelEnum.BelowExpectation
     */
    private AssessmentLevelEnum handleViolation(int violatedSlideIndex){
        
        double slideTimeInSeconds = getSlideTime(violatedSlideIndex);

        if(logger.isInfoEnabled()){
            logger.info("Violated under dwell time value of "+slideTimeInSeconds+" on slide index of "+violatedSlideIndex+".");
        }
        timeViolation(violatedSlideIndex);
        
        // set the explanation for this below expectation assessment 
        StringBuffer sb = new StringBuffer();
        int index = violatedSlideIndex + 1;
        sb.append("Under ").append(slideTimeInSeconds).append(" seconds was spent on slide ").append(index);
        assessmentExplanation = sb.toString();
        
        return AssessmentLevelEnum.BELOW_EXPECTATION;
    }
    
    /**
     * Handles when the powerpoint show has ended.  Essentially assess whether this condition
     * can be assessed having left the show.
     * @return the condition assessment given the previous state and ending the show.  Will return
     * null if the last powerpoint state was not known.
     */
    private AssessmentLevelEnum handlePowerPointEnd(){
        
        // it is possible that both a STOP_FREEZE received message and the task ending could enter this method
        // but we don't want to handle both cases, just one.
        if(hasCompleted()){
            return null;
        }
        
        AssessmentLevelEnum level = null;
        
        //check if all slides were dwelled upon for the appropriate amount of time by checking
        //if the last known slide is the last slide in the show.  If it wasn't the last slide,
        //add a score event for each remaining slide because they weren't visited.
        
        if(lastState != null){
            
            //TODO: its possible the slideshow allowed the learner to jump around in the order of the slides, need to check
            //      that all specified dwell times were achieved.
            
            int slideCount = lastState.getSlideCount();
            int lastSlideIndex = lastState.getSlideIndex();
            
            if(lastSlideIndex < slideCount){
                //the last slide visited before the stop-freeze wasn't the last slide in the show,
                //for now this is an indication that the slides after the last slide weren't visited
                
                for(int i = lastSlideIndex+1; i <= slideCount; i++){
                    timeViolation(i);
                }
                
                if(logger.isInfoEnabled()){
                    logger.info("Violated under dwell time value of all remaining slides after index of "+lastSlideIndex+".");
                }
                level = AssessmentLevelEnum.BELOW_EXPECTATION;
                assessmentExplanation = "Violated under dwell time value of all remaining slides after index of "+(lastSlideIndex+1)+".";
                updateAssessment(level);
                
            }else if(lastSlideIndex == slideCount){
                // the last slide was being shown when PowerPoint ended, check the last slide for a violation
                
                if(slideTimer.isScheduled()){ 
                    // there is a timer still running for this last slide
                    level = handleViolation(lastSlideIndex);
                }
            }
            
        }else{
            logger.warn("Unable to determine if all slides were visited because the last PowerPoint state is null");
        }
        
        //signal that this condition is completed because the show has finished.
        this.conditionCompleted();
        
        return level;
    }

    /**
     * The task that this condition is under is being stopped, therefore this condition will no longer
     * receive training app messages.  Check all remaining slides for violation.  This can also be a way
     * to catch if the STOP_FREEZE message was not received for some reason.
     */
    @Override
    public void stop(){
        super.stop();
        
        AssessmentLevelEnum level = handlePowerPointEnd();
        if(level != null){
            conditionActionInterface.conditionAssessmentCreated(this);
        }
    }
    
    @Override
    protected void timeExpired(int slideIndex){
        
        //time was allowed to expire, success
        if(logger.isInfoEnabled()){
            logger.info("Dwell time succeeded on slide index of "+slideIndex);
        }
        updateAssessment(AssessmentLevelEnum.AT_EXPECTATION);
        assessmentExplanation = null;
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
