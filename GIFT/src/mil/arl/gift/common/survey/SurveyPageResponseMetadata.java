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

/**
 * A class that contains the basic metadata of an {@link SurveyPageResponse}
 * without containing any references to the related {@link SurveyPage} itself.
 * Acts as a lightweight version of the {@link SurveyPageResponse} class that 
 * can be used in many circumstances.
 * @see SurveyPageResponse
 * @author tflowers
 *
 */
public class SurveyPageResponseMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int surveyPageResponseId;
    private int surveyResponseId;
    private Date startTime;
    private Date endTime;
    private List<AbstractQuestionResponseMetadata> questionMetadata = new ArrayList<AbstractQuestionResponseMetadata>();
    private int surveyPageId;
    
    /**
     * Constructor used during deserialization
     */
    public SurveyPageResponseMetadata() {
        
    }
    
    /**
     * Constructor that populates the metadata fields from the data contained 
     * within the given SurveyPageResponse
     * @param response the SurveyPageResponse to pull metadata from
     */
    public SurveyPageResponseMetadata(SurveyPageResponse response) {
        setSurveyPageResponseId(response.getSurveyPageResponseId());
        setSurveyResponseId(response.getSurveyResponseId());
        setStartTime(response.getStartTime());
        setEndTime(response.getEndTime());
        setSurveyPageId(response.getSurveyPage().getId());
        for(AbstractQuestionResponse qResponse : response.getQuestionResponses()) {
            questionMetadata.add(new AbstractQuestionResponseMetadata(qResponse));
        }
    }
    
    /**
     * Getter for the survey page response id
     * @return the value of the survey page response id
     */
    public int getSurveyPageResponseId() {
        return surveyPageResponseId;
    }

    /**
     * Setter for the survey page response id
     * @param surveyPageResponseId the new value of the survey page response id
     */
    public void setSurveyPageResponseId(int surveyPageResponseId) {
        this.surveyPageResponseId = surveyPageResponseId;
    }

    /**
     * Getter for the survey response id
     * @return the value of the survey response id
     */
    public int getSurveyResponseId() {
        return surveyResponseId;
    }

    /**
     * Setter for the survey response id
     * @param surveyResponseId the new value of the survey response id
     */
    public void setSurveyResponseId(int surveyResponseId) {
        this.surveyResponseId = surveyResponseId;
    }

    /**
     * Getter for the start time of the survey page response
     * @return the value of the start time, can be null
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Setter for the start time of the survey page response
     * @param startTime the new value of the start time, can be null
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Getter for the end time of the survey page response
     * @return the value of the end time, can be null
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Setter for the end time of the survey page response
     * @param endTime the new value of the end time, can be null
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Getter for the list of AbstractQuestionResponseMetadata
     * @return the list containing the abstract question response metadata, can be null
     */
    public List<AbstractQuestionResponseMetadata> getQuestionResponses() {
        return questionMetadata;
    }

    /**
     * Setter for the list of AbstractQuestionResponseMetadata
     * @param questionMetadata the new list containing the abstract question response metadata, can't be null
     */
    public void setQuestionMetadata(List<AbstractQuestionResponseMetadata> questionMetadata) {
        if(questionMetadata == null) {
            throw new IllegalArgumentException("questionMetadata can't be null");
        }
        
        this.questionMetadata = questionMetadata;
    }
    
    /**
     * Getter for the survey page id
     * @return the value of the survey page id
     */
    public int getSurveyPageId() {
        return surveyPageId;
    }
    
    /**
     * Setter for the survey page id
     * @param surveyPageId the new value of the survey page id
     */
    public void setSurveyPageId(int surveyPageId) {
        this.surveyPageId = surveyPageId;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[SurveyPageResponseMetadata: ")
                .append("surveyPageResponseId=").append(getSurveyPageResponseId())
                .append(", surveyResponseId=").append(getSurveyResponseId())
                .append(", startTime=").append(getStartTime())
                .append(", endTime=").append(getEndTime())
                .append(", questionResponses=").append(getQuestionResponses())
                .append(", surveyPageId=").append(getSurveyPageId())
                .append("]").toString();
    }
}