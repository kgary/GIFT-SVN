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
import java.util.List;

/**
 * A class that contains the basic metadata of an {@link AbstractQuestionResponse}
 * without containing any references to the related {@link AbstractQuestion} itself.
 * Acts as a lightweight version of the {@link AbstractQuestionResponse} class that 
 * can be used in many circumstances.
 * @see AbstractQuestionResponse
 * @author tflowers
 *
 */
public class AbstractQuestionResponseMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int surveyPageResponseId;
    private int surveyQuestionId;
    private List<QuestionResponseElementMetadata> responses = new ArrayList<QuestionResponseElementMetadata>();
    private List<Integer> optionOrder;
    
    /**
     * Empty constructor required for deserialization by the JSON codec
     */
    public AbstractQuestionResponseMetadata() {
        
    }
    
    /**
     * Constructor that populates the metadata fields from the data contained 
     * within the given AbstractQuestionResponse
     * @param response the AbstractQuestionResponse to pull metadata from
     */
    public AbstractQuestionResponseMetadata(AbstractQuestionResponse response) {
        setSurveyPageResponseId(response.getSurveyPageResponseId());
        setSurveyQuestionId(response.getSurveyQuestion().getId());
        for(QuestionResponseElement e : response.getResponses()) {
            responses.add(new QuestionResponseElementMetadata(e));
        }
        setOptionOrder(response.getOptionOrder());
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
     * Getter for the list of responses
     * @return the list containing the QuestionResponseElementMetadata, can be null
     */
    public List<QuestionResponseElementMetadata> getResponses() {
        return responses;
    }
    
    /**
     * Setter for the list of responses
     * @param responses the new value of the list of responses, can't be null
     */
    public void setResponses(List<QuestionResponseElementMetadata> responses) {
        if(responses == null) {
            throw new IllegalArgumentException("responses can't be null");
        }
        
        this.responses = responses;
    }

    /**
     * Getter for the survey question id
     * @return the value of the survey question id
     */
    public int getSurveyQuestionId() {
        return surveyQuestionId;
    }

    /**
     * Setter for the survey question id
     * @param surveyQuestionId the new value of the survey question id
     */
    public void setSurveyQuestionId(int surveyQuestionId) {
        this.surveyQuestionId = surveyQuestionId;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("[AbstractQuestionResponseMetadata: ")
                .append("surveyPageResponseId=").append(getSurveyPageResponseId())
                .append(", responses=").append(getResponses())
                .append(", surveyQuestionId=").append(getSurveyQuestionId()).append("]")
                .append(", optionOrder = ").append(getOptionOrder())
                .toString();
    }

    /**
     * Gets the order that the question's listed options were shown in
     * 
     * @return the order that the question's listed options were shown in
     */
    public List<Integer> getOptionOrder() {
        return optionOrder;
    }

    /**
     * Sets the order that the question's listed options were shown in
     * 
     * @param options the order that the question's listed options were shown in
     */
    public void setOptionOrder(List<Integer> options) {
        this.optionOrder = options;
    }
}