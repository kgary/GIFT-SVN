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
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.domain.knowledge.common.ConditionActionInterface;
import mil.arl.gift.domain.knowledge.common.SurveyResponseAssessmentListener;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition is used to apply the results of a survey score to the parent concept.
 * 
 * @author mhoffman
 *
 */
public class SimpleSurveyAssessmentCondition extends AbstractCondition {
    
    private static Logger logger = LoggerFactory.getLogger(SimpleSurveyAssessmentCondition.class);
    
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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "SimpleSurveyAssessment.GIFT Domain condition description.html"), "Survey Assessment");    

    
    /**
     * required for authoring tools
     */
    public SimpleSurveyAssessmentCondition(){
        
    }

    /**
     * @deprecated GenericConditionInput has been replaced in favor of NoConditionInput
     * when the condition input is not used. Use {@link #SimpleSurveyAssessmentCondition(generated.dkf.NoConditionInput)}
     * instead.
     * 
     * @param input not used
     */
    @Deprecated
    public SimpleSurveyAssessmentCondition(generated.dkf.GenericConditionInput input){
        
    }

    /**
     * Constructor with input parameters
     * 
     * @param input not used
     */
    public SimpleSurveyAssessmentCondition(generated.dkf.NoConditionInput input){
        
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }
    
    @Override
    public void initialize(ConditionActionInterface conditionActionInterface) {
        super.initialize(conditionActionInterface);

        conditionActionInterface.addSurveyResponseAssessmentListener(new SurveyResponseAssessmentListener() {
            @Override
            public void surveyCompleted(SurveyResponse surveyResponse, AssessmentLevelEnum assessment) {

                logger.debug("Survey Completed Assessment: " + assessment);
                
                updateAssessment(assessment);
                
                if (assessment == AssessmentLevelEnum.AT_EXPECTATION || assessment == AssessmentLevelEnum.ABOVE_EXPECTATION) {
                    
                    //count the number of times At/Above expectation happened
                    scoringEventStarted();
                    
                    conditionCompleted();
                }
            }
        });
    }
    
    @Override
    public boolean handleTrainingAppGameState(Message message) {
        return false;
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }
    
    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }
    
    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }
    
    @Override
    public boolean canComplete() {
        return true;
    }

}
