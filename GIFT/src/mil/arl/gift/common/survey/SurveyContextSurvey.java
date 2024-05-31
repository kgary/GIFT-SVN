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
 * A survey in a survey context
 *
 * @author jleonard
 */
public class SurveyContextSurvey implements Serializable, Comparable<SurveyContextSurvey> {
    
    private static final long serialVersionUID = 1L;

    private int surveyContextId;

    private String key;

    private Survey survey;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SurveyContextSurvey() {
    }

    /**
     * Constructor
     *
     * @param id The ID of the survey context this survey is in
     * @param key The key of the survey in the survey context
     * @param survey The survey in the survey context
     */
    public SurveyContextSurvey(int id, String key, Survey survey) {

        this.surveyContextId = id;
        this.key = key;
        this.survey = survey;
    }

    /**
     * Gets the ID of the survey context survey
     *
     * @return int The ID of the survey context survey
     */
    public int getSurveyContextId() {
        return surveyContextId;
    }

    /**
     * Sets the ID of the survey context survey
     *
     * @param surveyContextId The ID of the survey context survey
     */
    public void setSurveyContextId(int surveyContextId) {
        this.surveyContextId = surveyContextId;
    }

    /**
     * Gets the key for the survey in the survey context
     *
     * @return String The key for the survey in the survey context
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key for the survey in the survey context
     *
     * @param key The key for the survey in the survey context
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the survey of the survey context survey
     *
     * @return GwtSurvey The survey of the survey context survey
     */
    public Survey getSurvey() {
        return survey;
    }

    /**
     * Sets the survey of the survey context survey
     *
     * @param survey The survey of the survey context survey
     */
    public void setSurvey(Survey survey) {
        this.survey = survey;
    }
    
    /**
     * Compares this survey context survey with another survey context survey.
     * 
     * Required by Comparable to exist and be public.
     * 
     * @param surveyContextSurvey The survey context survey to be compared to
     * @return Integer A value of -1 or 1 indicating whether this survey context survey's key is lexicographically less than, equal to, or greater than that of the compared survey context survey
     */
    @Override
    public int compareTo(SurveyContextSurvey surveyContextSurvey) {
    	return this.getKey().compareTo(surveyContextSurvey.getKey());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[SurveyContextSurvey: surveyContextId=");
        builder.append(surveyContextId);
        builder.append(", key=");
        builder.append(key);
        builder.append(", survey=");
        if(survey != null){
            builder.append(survey.getName()).append(" (id ").append(survey.getId()).append(")");
        }else{
            builder.append("null");
        }
        builder.append("]");
        return builder.toString();
    }
    
    
}