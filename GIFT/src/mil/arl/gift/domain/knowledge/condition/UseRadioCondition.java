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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.RadioUsed;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks whether the learner used the radio.
 * 
 * @author mhoffman
 */
public class UseRadioCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(UseRadioCondition.class);
    
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
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "UseRadio.GIFT Domain condition description.html"), "Use Radio");
    
    /** the learner actions needed to be shown to the learner for this condition to assess the learner */
    private static final Set<generated.dkf.LearnerActionEnumType> LEARNER_ACTIONS = new HashSet<>(1);
    static{
        LEARNER_ACTIONS.add(generated.dkf.LearnerActionEnumType.RADIO);
    }
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.UNKNOWN;
    
    /**
     * Default constructor - required for authoring logic
     */
    public UseRadioCondition(){
        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param useRadio - dkf content for this condition
     */
    public UseRadioCondition(generated.dkf.UseRadioCondition useRadio){
        
        if(useRadio.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(useRadio.getTeamMemberRefs());
        }
        
        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    @Override
    public boolean handleTrainingAppGameState(Message message) {
        
        //only interested in use radio actions 
        if(message.getMessageType() == MessageTypeEnum.LEARNER_TUTOR_ACTION){
            
            LearnerTutorAction action = (LearnerTutorAction)message.getPayload();
            AbstractLearnerTutorAction actionData = action.getAction();
            TeamMember<?> teamMember = actionData.getTeamMember();
            
            if(teamMember == null || (teamMember != null && isConditionAssessedTeamMember(teamMember.getEntityIdentifier()) != null)){
                //the learner action is relevant to this condition because:
                // 1. the action doesn't include team member info
                // 2. this condition didn't specify team members
                // 3. this condition specified team members and the action was taken by one of those team members
            
                AssessmentLevelEnum level = null;
                if(actionData instanceof RadioUsed){
                    
                    removeViolator(teamMember == null ? null : teamMember.getEntityIdentifier());
                    
                    level = AssessmentLevelEnum.AT_EXPECTATION;                    
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("The radio was used at the appropriate time in assessment, therefore setting an expectation of "+level);
                    }
                    
                    //update score for success
                    scoringEventStarted(teamMember);
                    
                    conditionCompleted();
                                   
                }else{                    
                    //this game state message indicates that the incorrect learner action has activated
                    
                    addViolator(teamMember, teamMember == null ? null : teamMember.getEntityIdentifier());
                    
                    level = AssessmentLevelEnum.BELOW_EXPECTATION;
                    if(logger.isDebugEnabled()){
                        logger.debug("Another learner action of "+actionData+" was used when waiting for the radio to be used during the current assessmentct resulting in a negative assessment");
                    }
                }
                
                setAssessmentExplanation();
                
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
            
            assessmentExplanationBuilder.append("} used a different learner action besides the radio.");   
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
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
        return LEARNER_ACTIONS;
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
        sb.append("[UseRadioCondition: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
