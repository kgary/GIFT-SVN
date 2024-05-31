/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyResponse;

/**
 * Properties for the survey widget
 *
 * @author jleonard
 */
public class SurveyWidgetProperties {

    private final static String SURVEY_QUESTIONS_PROPERTY = "SURVEY_QUESTIONS";

    private final static String SURVEY_ANSWERS_PROPERTY = "SURVEY_ANSWERS";
    
    private final static String INTERMEDIATE_QUESTION_ANSWERS_PROPERTY = "QUESTION_ANSWERS";
    
    private final static String SUBMIT_SURVEY_PAGE_REQUEST = "SUBMIT_SURVEY_PAGE_REQUEST";
    
    private SurveyWidgetProperties() { }
    
    /**
     * Gets the survey to display
     *
     * @param properties The properties for a survey widget
     * @return The survey to display
     */
    public static Survey getSurvey(WidgetProperties properties) {
        return (Survey) properties.getPropertyValue(SURVEY_QUESTIONS_PROPERTY);
    }

    /**
     * Sets the survey to display
     *
     * @param properties The properties for a survey widget
     * @param survey The survey to display
     */
    public static void setSurvey(WidgetProperties properties, Survey survey) {
        properties.setPropertyValue(SURVEY_QUESTIONS_PROPERTY, survey);
    }

    /**
     * Gets the response to the survey
     *
     * @param properties The properties for a survey widget
     * @return The response to the survey
     */
    public static SurveyResponse getAnswers(WidgetProperties properties) {
        return (SurveyResponse) properties.getPropertyValue(SURVEY_ANSWERS_PROPERTY);
    }

    /**
     * Sets the response to the survey
     *
     * @param properties The properties for a survey widget
     * @param surveyResponse The response to the survey
     */
    public static void setAnswers(WidgetProperties properties, SurveyResponse surveyResponse) {
        properties.setPropertyValue(SURVEY_ANSWERS_PROPERTY, surveyResponse);
    }
    
    /**
     * Gets the response to the current survey question
     *
     * @param properties The properties for a survey widget
     * @return The response to the survey
     */
    public static AbstractQuestionResponse getCurrentQuestionAnswers(WidgetProperties properties) {
        return (AbstractQuestionResponse) properties.getPropertyValue(INTERMEDIATE_QUESTION_ANSWERS_PROPERTY);
    }

    /**
     * Sets the response to the current survey question
     *
     * @param properties The properties for a survey widget
     * @param The response to the current question
     */
    public static void setCurrentQuestionAnswers(WidgetProperties properties, AbstractQuestionResponse questionResponse) {
        properties.setPropertyValue(INTERMEDIATE_QUESTION_ANSWERS_PROPERTY, questionResponse);
    }
    
    /**
     * Gets whether the survey page should be submitted
     *
     * @param properties The properties for a survey widget
     * @return true if the survey page should be submitted, false otherwise.  False will also be returned
     * if the property is not set.
     */
    public static boolean shouldSubmitSurveyPage(WidgetProperties properties) {
        Boolean value = properties.getBooleanPropertyValue(SUBMIT_SURVEY_PAGE_REQUEST);
        return value == null ? false : value;
    }

    /**
     * Sets whether the survey page should be submitted
     *
     * @param properties The properties for a survey widget
     * @param whether the survey page should be submitted
     */
    public static void setShouldSubmitSurveyPage(WidgetProperties properties, boolean submitSurveyPageRequest) {
        properties.setPropertyValue(SUBMIT_SURVEY_PAGE_REQUEST, submitSurveyPageRequest);
    }

}
