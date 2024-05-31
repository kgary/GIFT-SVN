/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractReport;
import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.Message;

/**
 * This is the base class for report conditions 
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractReportCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractReportCondition.class);
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.LEARNER_TUTOR_ACTION);
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
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.UNKNOWN;

    /**
     * Class constructor
     */
    public AbstractReportCondition(){        
        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    @Override
    public boolean handleTrainingAppGameState(Message message){
                
        //only interested in report completed actions 
        if(message.getMessageType() == MessageTypeEnum.LEARNER_TUTOR_ACTION) {
            
            AssessmentLevelEnum level = null;
            
            LearnerTutorAction actionMessage = (LearnerTutorAction)message.getPayload();
            AbstractLearnerTutorAction actionData = actionMessage.getAction();
            
            if(actionData instanceof AbstractReport) {
                
                if(logger.isDebugEnabled()){
                    logger.debug("Received message of "+actionData);
                }
                
                TeamMember<?> teamMember = actionData.getTeamMember();
                if(teamMember == null || (teamMember != null && isConditionAssessedTeamMember(teamMember.getEntityIdentifier()) != null)){
                    //the learner action is relevant to this condition because:
                    // 1. the action doesn't include team member info
                    // 2. this condition didn't specify team members
                    // 3. this condition specified team members and the action was taken by one of those team members

                    if(isCorrectReport((AbstractReport)actionData)){
                        //this game state message indicates that the correct report has activated
                        
                        removeViolator(teamMember == null ? null : teamMember.getEntityIdentifier());
                        
                        level = AssessmentLevelEnum.AT_EXPECTATION;
                        if(logger.isDebugEnabled()){
                            logger.debug("The correct report type was completed: Spot Report, resulting in a positive assessment");
                        }
                        
                    }else{
                        //this game state message indicates that the incorrect report has activated
                        
                        addViolator(teamMember, teamMember == null ? null : teamMember.getEntityIdentifier());
                        
                        level = AssessmentLevelEnum.BELOW_EXPECTATION;
                        if(logger.isDebugEnabled()){
                            logger.debug("The incorrect report type was completed: "+actionData+" resulting in a negative assessment");
                        }
                    }
                    
                    setAssessmentExplanation();
                }

                
                if(level != null){
                    updateAssessment(level);
                    return true;
                }
            }
        }
        
        return false;
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
            assessmentExplanationBuilder.append("{");
            StringUtils.join(TEAM_MEMBER_STRINGIFIER_DELIM, violators, TEAM_MEMBER_STRINGIFIER, assessmentExplanationBuilder);

            assessmentExplanationBuilder.append("} used the wrong learner action");   
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
    }
    
    /**
     * Determine if the provided report type is the correct type for this condition
     * 
     * @return whether the report provides is the correct report for the condition
     */
    protected abstract boolean isCorrectReport(AbstractReport report);
    
    @Override
    public List<MessageTypeEnum> getSimulationInterests(){
        return simulationInterests;
    }    

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }

}
