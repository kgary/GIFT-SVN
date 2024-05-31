/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.survey.SurveyResponse;

/**
 * The results of a survey, with the responses to questions in the survey and
 * information about the responses
 *
 * @author jleonard
 *
 */
public class SubmitSurveyResults {

    /**
     * the survey results data
     */
    private SurveyResponse surveyResponse = null;

    /**
     * GIFT survey context key associated with the survey
     */
    private String giftKey;
    
    /**
     * Course name. Used in the LRS to create an activity id which is used as a key for inserted survey results.
     */
    private String courseName;

    /**
     * Class constructor
     *
     * @param giftKey The GIFT key of the survey context associated with the survey. Cannot be null.
     * @param courseName the name of the course. Cannot be null.
     * @param surveyResponse The survey results including the questions and answers. Cannot be null.
     */
    public SubmitSurveyResults(String giftKey, String courseName, SurveyResponse surveyResponse) {
        if (giftKey == null) {
            throw new IllegalArgumentException("gift key cannot be null");
        } else if (courseName == null) {
            throw new IllegalArgumentException("course name cannot be null");
        } else if (surveyResponse == null) {
            throw new IllegalArgumentException("survey response cannot be null");
        }
        
        this.giftKey = giftKey;
        this.courseName = courseName;
        this.surveyResponse = surveyResponse;
    }

    /**
     * Return the survey results data which includes the questions and answers
     *
     * @return SurveyResponse The survey results. Will never be null.
     */
    public SurveyResponse getSurveyResponse() {
        return surveyResponse;
    }

    /**
     * Gets the GIFT key of the survey context associated with the survey
     *
     * @return The GIFT key of the survey context associated with the survey. Will never be null.
     */
    public String getGiftKey() {
        return giftKey;
    }

    /**
     * Gets the course name of the survey
     * 
     * @return the course name. Will never be null.
     */
    public String getCourseName() {
        return courseName;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SubmitSurveyResults: ");
        sb.append("gift key = ").append(getGiftKey());
        sb.append(", course name = ").append(getCourseName());
        sb.append(", survey response = ").append(getSurveyResponse());
        sb.append("]");

        return sb.toString();
    }
}
