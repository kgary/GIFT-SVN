/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.AbstractReport;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.NineLineReport;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks whether a 9-Line report was completed 
 * 
 * @author mhoffman
 *
 */
public class NineLineReportCondition extends AbstractReportCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(NineLineReportCondition.class);
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "NineLineReport.GIFT Domain condition description.html"), "9-Line Report");

    /** the learner actions needed to be shown to the learner for this condition to assess the learner */
    private static final Set<generated.dkf.LearnerActionEnumType> LEARNER_ACTIONS = new HashSet<>(1);
    static{
        LEARNER_ACTIONS.add(generated.dkf.LearnerActionEnumType.NINE_LINE_REPORT);
    }
    
    /**
     * Default constructor - required for authoring logic
     */
    public NineLineReportCondition(){

    }    
    
    /**
     * Class constructor 
     * 
     * @param nineLine - configuration parameter for this condition
     */
    public NineLineReportCondition(generated.dkf.NineLineReportCondition nineLine){

        if(nineLine.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(nineLine.getTeamMemberRefs());
        }
    }  

    @Override
    public boolean handleTrainingAppGameState(Message message){
        
        if(super.handleTrainingAppGameState(message)){
            AssessmentLevelEnum level = getAssessment();
            
            if(level == AssessmentLevelEnum.AT_EXPECTATION){
                // received correct report, update score for success and mark that condition is completed
                // Note: at this point we know:
                // 1. the message is a learner action
                // 2. the action was performed by a team member being assessed by this condition
                // 3. the member was removed from the violators collection
                // 4. the assessment explanation was updated
                // 5. the condition assessment attribute was updated
                
                LearnerTutorAction action = (LearnerTutorAction)message.getPayload();
                AbstractLearnerTutorAction actionData = action.getAction();
                TeamMember<?> teamMember = actionData.getTeamMember();
                
                if(logger.isDebugEnabled()){
                    logger.debug("Learner action received was for this condition.");
                }
                scoringEventStarted(teamMember);
                
                conditionCompleted();               
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    protected boolean isCorrectReport(AbstractReport report){
        
        return report instanceof NineLineReport;
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
    public boolean canComplete() {
        return true;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[NineLineReportCondition: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
