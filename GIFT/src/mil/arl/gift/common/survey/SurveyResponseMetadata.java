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

import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;

/**
 * A class that contains the metadata for a {@link SurveyResponse}
 * without saving a reference to the {@link SurveyResponse} or 
 * the {@link Survey}
 * @see SurveyResponse
 * @author tflowers
 *
 */
public class SurveyResponseMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int surveyResponseId;
    private int surveyId;
    private String surveyName;
    private String giftKey;
    private int surveyContextId;
    private Date surveyStartTime;
    private Date surveyEndTime;
    private boolean hasIdealAnswer;
    private List<SurveyPageResponseMetadata> surveyPageResponses = new ArrayList<SurveyPageResponseMetadata>();
    
    /** the type of survey that elicited this response */
    private SurveyTypeEnum surveyType;
    
    /**
     * Included only to make the type GWT serializable
     */
    public SurveyResponseMetadata() {
        
    }
    
    /**
     * Constructs metadata out of a survey response
     * @param response the response to extract metadata from, 
     * cannot be null
     */
    public SurveyResponseMetadata(SurveyResponse response) {
        this();
        if(response == null) {
            throw new IllegalArgumentException("The response can't be null");
        }
        
        setSurveyResponseId(response.getSurveyResponseId());
        setSurveyId(response.getSurveyId());
        setSurveyName(response.getSurveyName());
        setGiftKey(response.getGiftKey());
        setSurveyContextId(response.getSurveyContextId());
        setSurveyStartTime(response.getSurveyStartTime());
        setSurveyEndTime(response.getSurveyEndTime());
        setHasIdealAnswer(response.getHasFillInTheBlankQuestionWithIdealAnswer());
        setSurveyType(response.getSurveyType());
        for(SurveyPageResponse pageResponse : response.getSurveyPageResponses()) {
            surveyPageResponses.add(new SurveyPageResponseMetadata(pageResponse));
        }
    }
    
    /**
     * gets the survey response id
     * @return the value of the survey response id
     */
    public int getSurveyResponseId() {
        return surveyResponseId;
    }
    
    /**
     * sets the survey response id
     * @param surveyResponseId the new value of the survey response id
     */
    public void setSurveyResponseId(int surveyResponseId) {
        this.surveyResponseId = surveyResponseId;
    }
    
    /**
     * gets the id of the survey that was being responded to
     * @return the value of the survey id
     */
    public int getSurveyId() {
        return surveyId;
    }
    

    /**
     * sets the id of the survey that was being responded to
     * @return the new value of the survey id
     */
    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }
    
    /**
     * gets the name of the survey that was being responded to
     * @return the value of the survey name, can be null
     */
    public String getSurveyName() {
        return surveyName;
    }

    /**
     * sets the name of the survey that was being responded to
     * @return the new value of the survey name, can be null
     */
    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }
    
    /**
     * gets the unique identifier for the survey that was taken
     * @return the value of the gift key, can be null
     */
    public String getGiftKey() {
        return giftKey;
    }
    
    /**
     * sets the unique identifier for the survey that was taken
     * @return the new value of the gift key, can be null
     */
    public void setGiftKey(String giftKey) {
        this.giftKey = giftKey;
    }
    
    /**
     * gets the id of the survey context which the survey which was taken
     * belongs to
     * @return the value of the survey context
     */
    public int getSurveyContextId() {
        return surveyContextId;
    }
    
    /**
     * sets the id of the survey context which the survey which was taken
     * belongs to
     * @return the new value of the survey context
     */
    public void setSurveyContextId(int surveyContextId) {
        this.surveyContextId = surveyContextId;
    }
    
    /**
     * gets the time that the survey was started by the survey taker
     * @return the value of the survey start time, can be null
     */
    public Date getSurveyStartTime() {
        return surveyStartTime;
    }
    
    /**
     * sets the time that the survey was started by the survey taker
     * @return the new value of the survey start time, can be null
     */
    public void setSurveyStartTime(Date surveyStartTime) {
        this.surveyStartTime = surveyStartTime;
    }
    
    /**
     * gets the time that the survey was completed by the survey taker
     * @return the value of the survey end time, can be null
     */
    public Date getSurveyEndTime() {
        return surveyEndTime;
    }
    
    /**
     * sets the time that the survey was completed by the survey taker
     * @return the new value of the survey end time, can be null
     */
    public void setSurveyEndTime(Date surveyEndTime) {
        this.surveyEndTime = surveyEndTime;
    }
    
    /**
     * gets whether or not the survey that this response metadata was 
     * responding to had a fill in the blank question with an ideal answer
     * @return true if the survey had a fill in the blank question with an ideal answer, false otherwise
     */
    public boolean getHasIdealAnswer() {
        return hasIdealAnswer;
    }
    
    /**
     * sets whether or not the survey that this response metadata was 
     * responding to had a fill in the blank question with an ideal answer
     * @param hasIdealAnswer the new value for the hasFillInTheBlankQuestionWithIdealAnswer flag
     */
    public void setHasIdealAnswer(boolean hasIdealAnswer) {
        this.hasIdealAnswer = hasIdealAnswer;
    }

    /**
     * gets the list of the survey page response metadata that compose this 
     * survey response metadata object
     * @return the list containing the survey page response metadata, can be null
     */
    public List<SurveyPageResponseMetadata> getSurveyPageResponses() {
        return surveyPageResponses;
    }

    /**
     * sets the list of the survey page response metadata that compose this
     * survey response metadata object
     * @param surveyPageResponses the new list of survey page response metadata, can't be null
     */
    public void setSurveyPageResponses(List<SurveyPageResponseMetadata> surveyPageResponses) {
        if(surveyPageResponses == null) {
            throw new IllegalArgumentException("surveyPageResponses can't be null");
        }
        
        this.surveyPageResponses = surveyPageResponses;
    }

    /**
     * Return the type of survey this survey response is for.
     * 
     * @return the survey type.
     */
    public SurveyTypeEnum getSurveyType() {
        return surveyType;
    }

    /**
     * Set the type of survey this survey response is for.
     * 
     * @param surveyType the survey type.  Can't be null.
     */
    public void setSurveyType(SurveyTypeEnum surveyType) {
        if(surveyType == null){
            throw new IllegalArgumentException("The survey type can't be null.");
        }
        this.surveyType = surveyType;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[SurveyResponseMetadata: ")
            .append("surveyResponseId = ").append(getSurveyResponseId())
            .append(", surveyId = ").append(getSurveyId())
            .append(", surveyName = ").append(getSurveyName())
            .append(", getGiftKey = ").append(getGiftKey())
            .append(", surveyType = ").append(getSurveyType())
            .append(", surveyContextId = ").append(getSurveyContextId())
            .append(", surveyStartTime = ").append(getSurveyStartTime())
            .append(", surveyEndTime = ").append(getSurveyEndTime())
            .append(", hasIdealAnswer = ").append(getHasIdealAnswer())
            .append(", surveyPageResponses = ").append(getSurveyPageResponses())
            .append("]").toString();
    }
    
    
}