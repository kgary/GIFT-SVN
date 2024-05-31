/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;

/**
 * The payload class for a GET_SURVEY_REPLY message
 * @author tflowers
 *
 */
public class SurveyGiftData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Survey survey;
    private String giftKey;
    
    /**
     * No arg constructor for GWT Serialization purposes
     */
    private SurveyGiftData() {
        
    }
    
    /**
     * Constructs an instance of SurveyGiftData with the 
     * given giftKey and survey
     * @param giftKey the giftKey identifying the survey
     * @param survey the survey that the other data represents
     */
    public SurveyGiftData(String giftKey, Survey survey) {
        this();
        setGiftKey(giftKey);
        setSurvey(survey);
    }

    /**
     * Getter for the survey
     * @return the value of the survey, can't be null
     */
    public Survey getSurvey() {
        return survey;
    }

    /**
     * Setter for the survey
     * @param survey the new value of the survey, can't be null
     */
    private void setSurvey(Survey survey) {
        if(survey == null) {
            throw new IllegalArgumentException("survey can't be null");
        }
        
        this.survey = survey;
    }

    /**
     * Getter for gift key
     * @return the value of the gift key, can't be null
     */
    public String getGiftKey() {
        return giftKey;
    }

    /**
     * Setter for the gift key field
     * @param giftKey the new value for the gift key, can't be null
     */
    private void setGiftKey(String giftKey) {
        if(giftKey == null) {
            throw new IllegalArgumentException("giftKey can't be null");
        }
        
        this.giftKey = giftKey;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[SurveyGiftData: ")
                .append("survey=").append(getSurvey())
                .append(", giftKey=").append(getGiftKey())
                .append("]").toString();
    }
}
