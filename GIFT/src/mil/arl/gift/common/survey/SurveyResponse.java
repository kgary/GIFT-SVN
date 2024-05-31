/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.common.ta.state.TrainingAppState;

/**
 * An instance of a completed survey
 *
 * @author jleonard
 */
public class SurveyResponse implements Serializable, TrainingAppState {
    
    /**
     * Generates a duplicate SurveyResponse that has the same values as the 
     * supplied SurveyResponse
     * @param toCopy The SurveyResponse to copy
     * @return A shallow copy of the SurveyResponse toCopy
     */
    public static SurveyResponse createShallowCopy(SurveyResponse toCopy) {
        SurveyResponse copy = new SurveyResponse();
        
        copy.hasFillInTheBlankQuestionWithIdealAnswer = toCopy.hasFillInTheBlankQuestionWithIdealAnswer;
        copy.surveyContextId = toCopy.surveyContextId;
        copy.surveyEndTime = toCopy.surveyEndTime;
        copy.surveyId = toCopy.surveyId;
        copy.surveyName = toCopy.surveyName;
        copy.surveyType = toCopy.surveyType;
        copy.giftKey = toCopy.giftKey;
        copy.surveyPageResponses = toCopy.surveyPageResponses;
        copy.surveyResponseId = toCopy.surveyResponseId;
        copy.surveyScorer = toCopy.surveyScorer;
        copy.surveyStartTime = toCopy.surveyStartTime;
        
        return copy;
    }
    
    private static final long serialVersionUID = 1L;

    private int surveyResponseId;

    private int surveyId;
    
    private String surveyName;

    private String giftKey;
    
    private int surveyContextId;

    private Date surveyStartTime;

    private Date surveyEndTime;

    private List<SurveyPageResponse> surveyPageResponses = new ArrayList<SurveyPageResponse>();
    
    private SurveyScorer surveyScorer;
    
    private boolean hasFillInTheBlankQuestionWithIdealAnswer;
    
    private SurveyTypeEnum surveyType = SurveyTypeEnum.COLLECTINFO_NOTSCORED;
    
    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SurveyResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyResponseId The ID of the survey response
     * @param survey The survey this response is for
     * @param surveyContextId The ID of the survey context the survey is in
     * @param surveyStartTime The time the survey was started
     * @param surveyEndTime The time the survey was completed
     * @param surveyPageResponses The responses to the survey pages
     */
    public SurveyResponse(int surveyResponseId, Survey survey, int surveyContextId, Date surveyStartTime, Date surveyEndTime, List<SurveyPageResponse> surveyPageResponses) {

        setSurveyResponseId(surveyResponseId);
        setSurveyContextId(surveyContextId);
        setSurveyStartTime(surveyStartTime);
        setSurveyEndTime(surveyEndTime);
        setSurveyPageResponses(surveyPageResponses);
        setSurvey(survey);
    }

    /**
     * Gets the ID of the survey response
     *
     * @return int The ID of the survey response
     */
    public int getSurveyResponseId() {
        return surveyResponseId;
    }

    /**
     * Sets the ID of the survey response
     *
     * @param surveyResponseId The ID of the survey response
     */
    public void setSurveyResponseId(int surveyResponseId) {
        this.surveyResponseId = surveyResponseId;
    }

    /**
     * Acts as a "meta-setter" for multiple properties which represent 
     * metadata describing the survey that is being responded to. These 
     * properties include: hasFillInTheBlankQuestionWithIdealAnswer, 
     * surveyId, surveyName, surveyScorerModel, surveyType
     *
     * @param survey The survey that this response is for
     */
    public void setSurvey(Survey survey) {
        setHasFillInTheBlankQuestionWithIdealAnswer(false);
        for(SurveyPage page : survey.getPages()){
    		for(AbstractSurveyElement element: page.getElements()){
    			
    			if(element instanceof FillInTheBlankSurveyQuestion 
    					&& ((FillInTheBlankSurveyQuestion)element).getQuestion().getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)){
    				setHasFillInTheBlankQuestionWithIdealAnswer(true);
    			}
    		}
    	}
        
        setSurveyId(survey.getId());
        setSurveyName(survey.getName());
        setSurveyType(survey.getSurveyType());
        setSurveyScorerModel(survey.getScorerModel());
    }
    
    /**
     * Gets the id of the survey that was responded to
     * @return the value of the survey id
     */
    public int getSurveyId() {
        return surveyId;
    }
    
    /**
     * Sets the id of the survey that was responded to
     * @param surveyId the new value to set the survey id to
     */
    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }
    
    /**
     * Gets the name of the survey that was responded to
     * @return the value of the survey name
     */
    public String getSurveyName() {
        return surveyName;
    }
    
    /**
     * Sets the name of the survey that was responded to
     * @param surveyName the new value to set the survey name to
     */
    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }
    
    /**
     * Gets the type of the survey that was responded to. If not set explicitly, the survey type will default 
     * to {@link SurveyTypeEnum#COLLECTINFO_NOTSCORED}.
     * 
     * @return the value of the survey type
     */
    public SurveyTypeEnum getSurveyType() {
        return surveyType;
    }
    
    /**
     * Sets the type of the survey that was responded to. If the provided type is null, then a default type
     * of {@link SurveyTypeEnum#COLLECTINFO_NOTSCORED} will be used.
     * 
     * @param surveyType the new value to set the survey type to
     */
    public void setSurveyType(SurveyTypeEnum surveyType) {
        
        if(surveyType != null) {
            this.surveyType = surveyType;
            
        } else {
            this.surveyType = SurveyTypeEnum.COLLECTINFO_NOTSCORED;
        }
    }
    
    /**
     * Gets the gift key of the survey that was responded to
     * @return the value of the gift key
     */
    public String getGiftKey() {
        return giftKey;
    }
    
    /**
     * Sets the gift key of the survey that was responded to
     * @param giftKey the new value to set the gift key to
     */
    public void setGiftKey(String giftKey) {
        this.giftKey = giftKey;
    }
    
    /**
     * Gets the survey scorer model of the survey that was responded to
     * @return the value of the survey scorer model
     */
    public SurveyScorer getSurveyScorerModel() {
        return surveyScorer;
    }
    
    /**
     * Sets the survey scorer of the survey that was responded to
     * @param surveyScorer the new value to set the survey scorer model to
     */
    public void setSurveyScorerModel(SurveyScorer surveyScorer) {
        this.surveyScorer = surveyScorer;
    }

    /**
     * Gets the ID of the survey context this was done in
     *
     * @return int The ID of the survey context this was done in
     */
    public int getSurveyContextId() {
        return surveyContextId;
    }

    /**
     * Sets the ID of the survey context this was done in
     *
     * @param surveyContextId The ID of the survey context for this was done in
     */
    public void setSurveyContextId(int surveyContextId) {
        this.surveyContextId = surveyContextId;
    }

    /**
     * Gets the time this survey was started
     *
     * @return Date The time this survey was started
     */
    public Date getSurveyStartTime() {
        return surveyStartTime;
    }

    /**
     * Sets the time this survey was started
     *
     * @param surveyStartTime The time this survey was started
     */
    public void setSurveyStartTime(Date surveyStartTime) {
        this.surveyStartTime = surveyStartTime;
    }

    /**
     * Gets the time this survey was completed
     *
     * @return Date The time this survey was completed
     */
    public Date getSurveyEndTime() {
        return surveyEndTime;
    }

    /**
     * Sets the time this survey was completed
     *
     * @param surveyEndTime The time this survey was completed
     */
    public void setSurveyEndTime(Date surveyEndTime) {
        this.surveyEndTime = surveyEndTime;
    }

    /**
     * Gets the surveyPageResponses to the questions in the survey completed
     *
     * @return List<GwtSurveyPageResponse> The surveyPageResponses to the
     * questions in the survey completed
     */
    public List<SurveyPageResponse> getSurveyPageResponses() {
        return surveyPageResponses;
    }

    /**
     * Sets the surveyPageResponses to the questions in the survey completed
     *
     * @param surveyPageResponses The surveyPageResponses to the questions in
     * the survey completed
     */
    public void setSurveyPageResponses(List<SurveyPageResponse> surveyPageResponses) {
        this.surveyPageResponses = surveyPageResponses;
    }
    
    /**
     * Returns whether or not the survey this response is for contains any fill-in-the-blank questions with ideal answers that 
     * should be shown in an AAR
     * 
     * @return true, if the survey contains at least one fill-in-the-blank-question with an ideal answer. Otherwise, returns false;
     */
    public boolean getHasFillInTheBlankQuestionWithIdealAnswer(){
    	return hasFillInTheBlankQuestionWithIdealAnswer;	
    }
    
    /**
     * Sets whether or not the survey has a fill in the blank question with an ideal answer. NOTE: This method 
     * should only be used internally within the class and by the JSON codec responsible for deserialization of 
     * this class
     * @param hasFillInTheBlankQuestionWithIdealAnswer the new value indicating whether or not the survey which 
     * this object in is response to has a fill in the blank question with an ideal answer
     */
    public void setHasFillInTheBlankQuestionWithIdealAnswer(boolean hasFillInTheBlankQuestionWithIdealAnswer) {
        this.hasFillInTheBlankQuestionWithIdealAnswer = hasFillInTheBlankQuestionWithIdealAnswer;
    }
    
    /**
     * Return whether the survey response at least one question response that has feedback authored for it.
     * 
     * @return true if there is at least one question response in the survey with feedback
     */
    public boolean hasFeedbackForResponses(){
        
        for(SurveyPageResponse surveyPageResponse : surveyPageResponses){
            
            for(AbstractQuestionResponse questionResponse : surveyPageResponse.getQuestionResponses()){
                
                List<QuestionResponseElement> questionResponseElements = questionResponse.getResponses();
                if(questionResponseElements == null || questionResponseElements.isEmpty()){
                    //no response to this question
                    continue;
                }
                
                AbstractSurveyQuestion<?> surveyQuestion = questionResponse.getSurveyQuestion();
                
                if(!(surveyQuestion instanceof MultipleChoiceSurveyQuestion)){
                    //only multiple choice questions have feedback right now
                    continue;
                }
                
                List<String> feedbacks = ((MultipleChoiceSurveyQuestion)surveyQuestion).getReplyFeedbacks();
                if(feedbacks == null || feedbacks.isEmpty()){
                    //no feedback authored for this question
                    continue;
                }
                
                //check if the learner's response have feedback authored for it   
                for (QuestionResponseElement responseElement : questionResponseElements) {

                    int index = 0;

                    for (ListOption replyOption : ((MultipleChoiceSurveyQuestion)surveyQuestion).getQuestion().getReplyOptionSet().getListOptions()) {

                        if (responseElement.getText() != null && responseElement.getText().equals(replyOption.getText())) {
                            //found the multiple choice question choice by matching text to the question response

                            if(feedbacks != null && index < feedbacks.size()){
                                
                                String feedback = feedbacks.get(index);
                                if(feedback != null && !feedback.isEmpty()){
                                    //found an authored feedback string
                                    return true;
                                }
                            }
                                    
                        }
                    }
                }

            }

        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return this.getSurveyResponseId(); //guaranteed to be unique
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        } else if (o instanceof SurveyResponse) {
            return this.getSurveyResponseId() == ((SurveyResponse) o).getSurveyResponseId();
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyResponse: ");
        sb.append("id = ").append(getSurveyResponseId());
        sb.append(", survey context id = ").append(getSurveyContextId());
        sb.append(", start time = ").append(getSurveyStartTime());
        sb.append(", end time = ").append(getSurveyEndTime());
        sb.append(", has fill in the blank question with ideal answer = ").append(getHasFillInTheBlankQuestionWithIdealAnswer());
        sb.append(", gift key = ").append(getGiftKey());
        sb.append(", survey id = ").append(getSurveyId());
        sb.append(", suvey name = ").append(getSurveyName());
        sb.append(", suvey type = ").append(getSurveyType());
        sb.append(", survey scorer = ").append(getSurveyScorerModel());
        
        sb.append(", page responses = {");
        for (SurveyPageResponse response : getSurveyPageResponses()) {
            sb.append(response).append(", ");
        }
        sb.append("}");

        sb.append("]");
        return sb.toString();
    }
}
