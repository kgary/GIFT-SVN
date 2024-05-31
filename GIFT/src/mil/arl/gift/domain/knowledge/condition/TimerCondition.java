/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition is used as a timer.  It doesn't check training app game state messages.
 * The time based assessment can be repeated. 
 * 
 * @author mhoffman
 */
public class TimerCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TimerCondition.class);
    
    private static final AssessmentLevelEnum ORIGINAL_ASSESSMENT = AssessmentLevelEnum.UNKNOWN;
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.SIMAN);
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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "Timer.GIFT Domain condition description.html"), "Timer");

    /** the input for this condition */
    private generated.dkf.TimerConditionInput timeInput;
    
    /** used to convert seconds to milliseconds */
    private static final BigDecimal conversion = new BigDecimal(1000.0);
    
    /**
     * Default constructor - required for authoring logic
     */
    public TimerCondition(){
        super(ORIGINAL_ASSESSMENT);
    }

    /**
     * Class constructor
     * 
     * @param timeInput the input for this condition including the interval to use for the timer (seconds)
     */
    public TimerCondition(generated.dkf.TimerConditionInput timeInput){
        this();
        
        this.timeInput = timeInput;
        if(timeInput.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(timeInput.getTeamMemberRefs());
        }
    }
    
    // Don't require the timer condition to specify team members even if there is a
    // team org defined.  For this condition the team member refs is provided in case the timer
    // has some direct relationship to a team members assessments.  More often than not 
    // timers are used to sequence dkf events (e.g. instructions) and not directly related to assessments.
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }
    
    private void setDelay(){
        
        BigDecimal ms = timeInput.getInterval().multiply(conversion);
        long interval = ms.longValue();
        
        if(logger.isInfoEnabled()){
            logger.info("Setting reset assessment delay to "+interval+" ms.");
        }
        setResetAssessmentDelay(interval);
    }
    
    @Override
    public void start(){
        super.start();
        
        //set the assessment timer
        setDelay();
        
        //update the assessment to the default (also schedules this timer)
        //Note: if updateAssessment method stops calling scheduleAssessmentReset than it will need to be called here explicitly
        updateAssessment(ORIGINAL_ASSESSMENT);
        if(conditionActionInterface != null){
            conditionActionInterface.conditionAssessmentCreated(this);
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Started timer for "+this);
        }
    }
    
    @Override
    protected void resetAssessmentEvent(){
        
        if(logger.isInfoEnabled()){
            logger.info("Notified of a assessment reset based time event.");
        }
        
        //cancel the timer if this is not repeatable
        if(timeInput.getRepeatable() == generated.dkf.BooleanEnum.FALSE){
            if(logger.isInfoEnabled()){
                logger.info("Stopping the reset assessment timer event from happening again because this timer condition input has isRepeatable set to false.");
            }
            setResetAssessmentDelay(null);
        }
        
        // set team org violators (if provided)
        if(getTeamMembersBeingAssessed() != null){
            for(String teamMemberRef : getTeamMembersBeingAssessed().getTeamMemberRef()){
                TeamMember<?> teamMember = getTeamMember(teamMemberRef);
                if (teamMember != null) {
                    addViolator(teamMember, teamMember.getEntityIdentifier());
                }
            }
            
            setAssessmentExplanation();
        }
        
        //count number of times the timer expired
        scoringEventStarted();
        
        //set a new assessment to update too (could be the same as the current assessment, especially if
        //this timer is repeating)
        updateAssessment(AssessmentLevelEnum.AT_EXPECTATION);
        
        //notification event - if the same assessment the timestamp might be the only thing updated for the parent concept
        if(conditionActionInterface != null){
            conditionActionInterface.conditionAssessmentCreated(this);
        }
        
        //notify that this condition is completed if it is not repeatable
        if(timeInput.getRepeatable() == generated.dkf.BooleanEnum.FALSE){
        	conditionCompleted();
        }
    }
 
    /**
     * Set the condition's assessment explanation based on the team members being assessed on this condition
     * and are currently violating the condition parameters.
     * @return true if the assessment explanation value for this condition changed during this method.
     */
    private boolean setAssessmentExplanation(){

        //update assessment explanation
        Set<TeamMember<?>> violators = buildViolatorsInfo();
        boolean changed = false;
        if(violators.isEmpty()){
            changed = assessmentExplanation != null;
            assessmentExplanation = null;
        }else{
            StringBuilder assessmentExplanationBuilder = new StringBuilder();
            Iterator<TeamMember<?>> itr = violators.iterator();
            assessmentExplanationBuilder.append("{");
            int foundTeamMembers = 0;
            while(itr.hasNext()){
                TeamMember<?> violator = itr.next();
                
                if(violator != null){
                    foundTeamMembers++;
                    assessmentExplanationBuilder.append(violator.getName());
                    if(itr.hasNext()){
                        assessmentExplanationBuilder.append(", ");
                    }
                }
            }
            
            if(foundTeamMembers > 0){
                assessmentExplanationBuilder.append("} ").append(foundTeamMembers == 1 ? "was" : "were").append(" assessed because the time expired after ").append(timeInput.getInterval()).append(" seconds."); 
            }else{
                assessmentExplanationBuilder.setLength(0);  // clear
                assessmentExplanationBuilder.append("The timer expired after ").append(timeInput.getInterval()).append(" seconds.");
            }
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
    }
 

    @Override
    public boolean handleTrainingAppGameState(Message message){        
        return false;
    }
    
    @Override
    public List<MessageTypeEnum> getSimulationInterests(){
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
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[TimerCondition: ");
        sb.append(super.toString());
        sb.append(", repeatable = ").append(timeInput.getRepeatable() == generated.dkf.BooleanEnum.TRUE);
        sb.append(", interval = ").append(timeInput.getInterval()).append(" ms");
        sb.append("]");
        
        return sb.toString();
    }
    
}
