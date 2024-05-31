/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition assesses the completion of the training application.  One use case example is when a PowerPoint
 * show is presented, this condition is interested in when the show has been completed.
 * 
 * @author mhoffman
 *
 */
public class ApplicationCompletedCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ApplicationCompletedCondition.class);
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.START_RESUME);
        simulationInterests.add(MessageTypeEnum.STOP_FREEZE);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.CompletionTime.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = 
            new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "ApplicationCompleted.GIFT Domain condition description.html"), "Application Completed");
    
    private Thread assessmentThread = null; 
    
    private long duration = -1;
    private boolean stopping = false;
    
    /**
     * Default constructor - required for authoring logic
     */
    public ApplicationCompletedCondition(){
        
    }
    
    /**
     * Class constructor
     * 
     * @param input - domain knowledge input configuration params
     */
    public ApplicationCompletedCondition(generated.dkf.ApplicationCompletedCondition input){
        
        String durationStr = input.getIdealCompletionDuration();
        if(durationStr != null){
            
            Date timestamp = null;
            try{
                timestamp = DomainDKFHandler.atTime_df.convertStringToDate(durationStr);
            }catch(Exception e){
                logger.error("Caught exception while trying to parse duration value of "+durationStr, e);
                throw new IllegalArgumentException("Caught exception while parsing time stamp, check log for more details", e);
            }
            
            duration = timestamp.getTime();
        }
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {
        
        AssessmentLevelEnum level = null;
        
        MessageTypeEnum mType = message.getMessageType();
        if(mType == MessageTypeEnum.STOP_FREEZE){
            
            StopFreeze stopFreeze = (StopFreeze) message.getPayload();
            
            if(stopFreeze.isPaused()){
                logger.info("Received "+message+", application has been paused.");
                
                //pause thread, keep track of amount of time paused (if assessing amount of time application has been running)
                if(assessmentThread != null){
                    
                    synchronized(assessmentThread){
                        logger.info("Making assessment thread wait until the training application has resumed from this pause");
                        assessmentThread.interrupt();
                    }
                    
                }
                
            }else{
                logger.info("Received "+message+", application has stopped. Condition completed.");                
                
                if(assessmentThread != null){
                    stopping = true;
                    assessmentThread.interrupt();
                }
            
                level = AssessmentLevelEnum.AT_EXPECTATION;
                updateAssessment(level);
                
                //update score for success
                scoringEventStarted();
                
                conditionCompleted();
                
                return true;
            }
            
        }else if(mType == MessageTypeEnum.START_RESUME){
            
            logger.info("Received "+message+", application has started or resumed from pause.");
            
            //if assessing amount of time then check the assessment thread
            if(duration > 0){
            
                //if timer thread already exists, 
                //this is a resume message because the thread is created only once on the first START_RESUME
                if(assessmentThread != null){
                    
                    logger.info("Releasing wait on assessment thread");
                    
                    synchronized(assessmentThread){
                        assessmentThread.notifyAll();
                    }
                    
                }else{
                    
                    logger.info("Creating application completed assessment thread.");
                    
                    //otherwise this is a start message, therefore create the thread
                    assessmentThread = new Thread("Application Completed Timer"){
                        
                        @Override
                        public void run(){
                            
                            Date start = new Date();
                            boolean interrupted = false;
                            
                            do{
                                try {
                                    interrupted = false;
                                    logger.debug("Starting ideal duration timer at "+start+".  Going to sleep for "+duration/1000.0+" seconds.");
                                    sleep(duration);
                                } catch (@SuppressWarnings("unused") InterruptedException e) {
                                    interrupted = true;
                                }            
                                                                
                                Date end = new Date();
                                
                                if(!stopping && !interrupted){
                                    //the training app is currently not stopping and the ideal duration has expired, change assessment
                                    
                                    logger.info("The ideal duration timer has expired after "+(end.getTime()-start.getTime())/1000.0+" seconds and the training application is still runnning, " +
                                    		"therefore changing assessment level");
                                    
                                    updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
                                    sendAssessmentEvent();
                                    
                                }else if(!stopping && interrupted){
                                    //represents a pause in the training application
                                    
                                    duration = duration - (end.getTime() - start.getTime());
                                    logger.info("Ideal duration timer was stopped with "+duration/1000.0+" seconds remaining because the training application was paused.  Waiting for application to resume...");
                                    
                                    //wait for application to resume from the pause...
                                    synchronized(this){
                                        try {
                                            this.wait();
                                        } catch (InterruptedException e) {
                                            logger.error("Caught exception while waiting for training application to resume from pause. Not sure what the state of this condition will be now.", e);
                                        }
                                    }
                                    
                                    logger.info("Application has resumed.  Setting duration to remaining time left of "+duration/1000.0+" seconds.");
                                }
                            
                                //continue when: training app paused [stopping=false, interrupted=true]
                                //stop when: training app stopped [stopping=true, interrupted=true], duration expired [stopping=false, interrupted=false]
                            }while(!stopping && interrupted);
                            
                            logger.debug("Stoping ideal duration timer at "+new Date());
                        }
                    };
                    
                    assessmentThread.start();
                }
                
            }//end if duration
        }//end if-else
        
        return false;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }
    
    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }
    
    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }
    
    @Override
    public boolean canComplete() {
        return true;
    }

}
