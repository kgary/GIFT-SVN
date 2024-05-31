/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyScale;
import mil.arl.gift.common.survey.score.SurveyScaleScore;
import mil.arl.gift.common.survey.score.SurveyScorerManager;

/**
 * Contains the information needed to assess a survey lesson assessment based on the
 * replies given by a learner.
 * 
 * @author mhoffman
 *
 */
public class GIFTSurveyLessonAssessment extends AbstractSurveyLessonAssessment {
    
    /** logger instance */
    private static Logger logger = LoggerFactory.getLogger(GIFTSurveyLessonAssessment.class);

    /** unique survey context gift key of a survey */
    private String giftKey;
    
    /** list of question assessments for this survey */
    private Map<Integer, QuestionAssessment> questionIdToAssessment;
    
    /**
     * Class constructor - set attributes
     * 
     * @param giftKey -  unique survey context gift key of a survey associated with this assessment
     * @param questionAssessments list of question assessments for this survey 
     */
    public GIFTSurveyLessonAssessment(String giftKey, List<QuestionAssessment> questionAssessments){
        
        if(giftKey == null){
            throw new IllegalArgumentException("The gift key can't be null.");
        }
        this.giftKey = giftKey;
        
        if(questionAssessments == null){
            throw new IllegalArgumentException("The question assessments collection can't be null.");
        }
        
        this.questionIdToAssessment = new HashMap<Integer, QuestionAssessment>(questionAssessments.size());
        for(QuestionAssessment assessment : questionAssessments){
            questionIdToAssessment.put(assessment.getQuestionId(), assessment);
        }
    }
    
    /**
     * Class constructor - set attributes from generated class's object content
     * 
     * @param survey - dkf content for a survey assessment
     */
    public GIFTSurveyLessonAssessment(generated.dkf.Assessments.Survey survey){
        
        this.giftKey = survey.getGIFTSurveyKey();
        
        boolean hasQuestions = survey.getQuestions() != null;
        
        this.questionIdToAssessment = new HashMap<Integer, QuestionAssessment>(
                hasQuestions ? survey.getQuestions().getQuestion().size() : 0);
        
        if (hasQuestions) {
            for (generated.dkf.Question question : survey.getQuestions().getQuestion()) {
                QuestionAssessment questionAssessment = new QuestionAssessment(question);
                questionIdToAssessment.put(questionAssessment.getQuestionId(), questionAssessment);
            }
        }
    }
    
    /**
     * Return the unique survey context gift key of the survey related to this assessment.
     * 
     * @return String - GIFT survey key from the survey database
     */
    public String getGiftKey(){
        return giftKey;
    }
    
    /**
     * Return the collection of question assessments for this survey assessment.
     * 
     * @return Collection<QuestionAssessment>
     */
    public Collection<QuestionAssessment> getQuestionAssessments(){
        return questionIdToAssessment.values();
    }
    
    @Override
    public AssessmentLevelEnum getAssessment(Object surveyResult) {
        
        if(surveyResult == null){
            throw new IllegalArgumentException("The survey response can't be null.");
        }else if(!(surveyResult instanceof SurveyResponse)){
            throw new IllegalArgumentException("Received unhandled survey result object of "+surveyResult);
        }

        AssessmentLevelEnum assessment = AssessmentLevelEnum.UNKNOWN;
        AssessmentLevelEnum qAssessment;

        SurveyResponse surveyResponse = (SurveyResponse) surveyResult;
        
        boolean assessmentHandled = false;
        
        if(surveyResponse.getSurveyType() == SurveyTypeEnum.ASSESSLEARNER_STATIC
                && surveyResponse.getSurveyScorerModel() != null) {
            
            //assess knowledge assessment surveys according to their own scoring rules
            List<ScoreInterface> scores = SurveyScorerManager.getScores(surveyResponse);
            
            for(ScoreInterface score : scores) {
                
                if(score instanceof SurveyScaleScore) {
                    
                    List<AbstractScale> scales =((SurveyScaleScore) score).getScales();
                    
                    for(AbstractScale scale : scales) {
                        
                        if(scale instanceof SurveyScale) {
                            
                            SurveyScale surveyScale = (SurveyScale) scale;
                            
                            if(LearnerStateAttributeNameEnum.KNOWLEDGE.equals(surveyScale.getAttribute())
                                    && surveyScale.getValue() instanceof ExpertiseLevelEnum){
                                
                                //if the learner's expertise was evaluated, use their expertise level to determine their assessment level
                                ExpertiseLevelEnum expertise = (ExpertiseLevelEnum) surveyScale.getValue();
                                
                                if (expertise.equals(ExpertiseLevelEnum.NOVICE)) {
                                    assessment = AssessmentLevelEnum.BELOW_EXPECTATION;
                                    
                                } else if (expertise.equals(ExpertiseLevelEnum.JOURNEYMAN)) {
                                    assessment = AssessmentLevelEnum.AT_EXPECTATION;
                                    
                                } else if (expertise.equals(ExpertiseLevelEnum.EXPERT)) {
                                    assessment = AssessmentLevelEnum.ABOVE_EXPECTATION;
                                }
                                
                                if(assessment != AssessmentLevelEnum.UNKNOWN) {
                                    assessmentHandled = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if(assessmentHandled) {
                    break;
                }
            }
        }
        
        //if an assessment couldn't be obtained from the survey's knowledge scoring rules, fall back to the legacy DKF scoring logic
        if(!assessmentHandled) {
        
            //TODO: for now use simple +/- scoring
            int score = 0;
    
            int responseId;
            for (SurveyPageResponse surveyPageResponse : ((SurveyResponse) surveyResult).getSurveyPageResponses()) {
    
                for (AbstractQuestionResponse response : surveyPageResponse.getQuestionResponses()) {
    
                    AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = response.getSurveyQuestion();
                                    
                    QuestionAssessment questionAssessment = questionIdToAssessment.get(surveyQuestion.getId());
    
                    if (questionAssessment != null) {
    
                        for (QuestionResponseElement responseElement : response.getResponses()) {
    
                            responseId = responseElement.getQuestionResponseId();
    
                            //TODO: currently the response data only contains the response string to allow for fill in the blank response,
                            // Therefore since only multiple choice single select survey assessments are currently supported, the response
                            // id must be searched for using the response's text
                            if (responseId == 0) {
    
                                if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {
    
                                    MultipleChoiceSurveyQuestion multipleChoiceQuestion = (MultipleChoiceSurveyQuestion) surveyQuestion;
    
                                    if (multipleChoiceQuestion.getQuestion().getReplyOptionSet() != null) {
    
                                        for (ListOption replyOption : multipleChoiceQuestion.getQuestion().getReplyOptionSet().getListOptions()) {
    
                                            if (replyOption.getText().equals(responseElement.getText())) {
    
                                                responseId = replyOption.getId();
                                                break;
                                            }
                                        }
                                    }
                                }
    
                                if (responseId == 0) {
                                    logger.error("Unable to find the response id for reply text = " + responseElement.getText() + " from " + response);
                                    continue;
                                }
                            }
                            /////////////////////////////////////
    
                            qAssessment = questionAssessment.getAssessment(responseId);
    
                            if (qAssessment == AssessmentLevelEnum.BELOW_EXPECTATION) {
                                score -= 2;
                            } else if (qAssessment == AssessmentLevelEnum.AT_EXPECTATION) {
                                score++;
                            } else if (qAssessment == AssessmentLevelEnum.ABOVE_EXPECTATION) {
                                score += 2;
                            }
                        }
                    }
                }
            }
    
            logger.debug("Resulting score of survey is " + score);
    
            if (score < 0) {
                assessment = AssessmentLevelEnum.BELOW_EXPECTATION;
            } else if (score > (ABOVE_EXPECTATION_SCORE * questionIdToAssessment.size() * 0.75)) {
                //just came up with an arbitrary relationship of above expectation score and the number of metrics in this concept
                assessment = AssessmentLevelEnum.ABOVE_EXPECTATION;
            } else if (score > (AT_EXPECTATION_SCORE * questionIdToAssessment.size() * 0.75)) {
                //just came up with an arbitrary relationship of above expectation score and the number of metrics in this concept
                assessment = AssessmentLevelEnum.AT_EXPECTATION;
            }
        }

        return assessment;
    }
    
    /**
     * Return the string representation of this class
     * 
     * @return String - the string representation of this class
     */
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyLessonAssessment: ");
        sb.append("giftKey = ").append(getGiftKey());        
        sb.append("]");
        
        return sb.toString();
    }
}
