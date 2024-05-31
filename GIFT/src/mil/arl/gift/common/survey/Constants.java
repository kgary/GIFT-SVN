/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

/**
 * This class contains common, constant fields relating to GIFT surveys.
 * 
 * @author mhoffman
 *
 */
public class Constants {
    
    /** 
     * this is the GIFT Key used in a survey context which has concept related questions added
     * to it.  Those questions are placed in a survey associated with this key and that survey
     * represents a question bank for this survey context.
     * 
     * Note: this must match mil.arl.gift.tools.sas.shared.GwtSurveySystemProperties.java KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY class attribute value
     */
    public static final String KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY = "Knowledge Assessment Question Bank";
    
    /**
     * this is the survey name to use for concept based knowledge assessment generated surveys.  
     * These surveys are usually created during Merrill's branch point course elements or as part
     * of a pre-test of knowledge as a course level survey element.  
     * All of the generated surveys will have the same name across all users and all courses.
     */
    public static final String KNOWLEDGE_ASSESSMENT_GENERATED_SURVEY_NAME = "Knowledge Assessment Survey";
    
    /**
     * this is a regular expression describing the GIFT KEY used in a survey context which 
     * has concept related questions that have been used in concept surveys. 
     * Concept surveys are associated with the key described by this regular expression. 
     * 
     * Note: 
     * 1) this must match mil.arl.gift.tools.sas.shared.GwtSurveySystemProperties.java KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX class attribute value
     * 2) this must match the logic used when dynamically creating the survey in mil.arl.gift.ums.db.survey.Surveys.getConceptSurveys method.
     */
    public static final String KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX = 
    		KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + " : [0-9]+";
    
    /** 
     * the default permissions to set when a database entry has no visible or editable usernames set. 
     * This is used for decoding JSON objects of previous GIFT versions which have no visible or editable usernames properties.
     */
    public static final String VISIBILITY_WILDCARD = "*";
    public static final String EDITABLE_WILDCARD = "*";
    
    /**
     * Private constructor 
     */
    private Constants(){  }
}
