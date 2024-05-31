/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.common.survey.score.TotalScorer;

/**
 * The properties of a survey
 *
 * @author jleonard
 */
public class SurveyProperties extends SurveyItemProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SurveyProperties() {
        super(new HashMap<SurveyPropertyKeyEnum, Serializable>());
    }

    /**
     * Constructor
     *
     * @param properties The map of property keys to their values of the survey
     */
    public SurveyProperties(Map<SurveyPropertyKeyEnum, Serializable> properties) {
        super(properties);
    }

    /**
     * Gets if the survey name should be hidden
     *
     * @return boolean If the survey name should be hidden
     */
    public boolean getHideSurveyName() {

        return getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_NAME, false);
    }

    /**
     * Sets if the survey name should be hidden
     *
     * @param hide If the survey name should be hidden
     */
    public void setHideSurveyName(boolean hide) {

        setBooleanPropertyValueRemoveFalse(SurveyPropertyKeyEnum.HIDE_SURVEY_NAME, hide);
    }

    /**
     * Gets if the survey question numbers should be hidden
     *
     * @return boolean If the question numbers should be hidden
     */
    public boolean getHideSurveyQuestionNumbers() {

        return getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, false);
    }

    /**
     * Sets if the question numbers should be hidden
     *
     * @param hide If the question numbers should be hidden
     */
    public void setHideSurveyQuestionNumbers(boolean hide) {

        setBooleanPropertyValueRemoveFalse(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, hide);
    }

    /**
     * Gets if the page numbers should be hidden
     *
     * @return boolean If the page numbers should be hidden
     */
    public boolean getHideSurveyPageNumbers() {

        return getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_PAGE_NUMBERS, false);
    }

    /**
     * Sets if the page numbers should be hidden
     *
     * @param hide If the page numbers should be hidden
     */
    public void setHideSurveyPageNumbers(boolean hide) {

        setBooleanPropertyValueRemoveFalse(SurveyPropertyKeyEnum.HIDE_SURVEY_PAGE_NUMBERS, hide);
    }

    /**
     * Gets the text that should be on a button that advances the survey by one
     * page
     *
     * @return String The text that should be on the next page button
     */
    public String getNextPageButtonLabel() {

        return (String) getPropertyValue(SurveyPropertyKeyEnum.SURVEY_NEXT_PAGE_BUTTON_LABEL);
    }

    /**
     * Sets the text that should be on a button that advances the survey by one
     * page
     *
     * @param label The text that should be on the next page button
     */
    public void setNextPageButtonLabel(String label) {

        setPropertyValue(SurveyPropertyKeyEnum.SURVEY_NEXT_PAGE_BUTTON_LABEL, label);
    }

    /**
     * Gets the text that should be on the button that submits the survey
     *
     * @return String The text that should be on the submit survey button
     */
    public String getCompleteSurveyButtonLabel() {

        return (String) getPropertyValue(SurveyPropertyKeyEnum.SURVEY_COMPLETE_SURVEY_BUTTON_LABEL);
    }

    /**
     * Sets the text that should be on the button that submits the survey
     *
     * @param label The text that should be on the submit survey button
     */
    public void setCompleteSurveyButtonLabel(String label) {

        setPropertyValue(SurveyPropertyKeyEnum.SURVEY_COMPLETE_SURVEY_BUTTON_LABEL, label);
    }

    /**
     * Gets if the user can go back to a previously completed page
     *
     * @return boolean If the user can go back to a previously completed page
     */
    public boolean getCanGoBackPages() {

        return getBooleanPropertyValue(SurveyPropertyKeyEnum.SURVEY_GO_BACK_ENABLED, false);
    }

    /**
     * Sets if the user can go back to a previously completed page
     *
     * @param back If the user can go back to a previously completed page
     */
    public void setCanGoBackPages(boolean back) {

        setBooleanPropertyValueRemoveFalse(SurveyPropertyKeyEnum.SURVEY_GO_BACK_ENABLED, back);
    }
    
    /**
     * Returns the survey type for this survey based on the survey property {@link #SurveyPropertyKeyEnum.SURVEY_TYPE}
     * 
     * @return null if the property is not set, otherwise the enumerated survey type
     */
    public SurveyTypeEnum getSurveyType() {
        
        String typeStr = (String) getPropertyValue(SurveyPropertyKeyEnum.SURVEY_TYPE);
        if(typeStr != null){
            return SurveyTypeEnum.valueOf(typeStr);
        }
        
        return null;
    }
    
    /**
     * Returns the survey type for this survey.  If the survey type property is not set than the 
     * type of survey is determined by a series of checks and then the survey type property is set
     * to that returned value.  Survey Type property is usually not known on surveys
     * that existed prior to survey type being a property.
     * @param surveyName the name of the survey, used to determine if the survey is a question bank survey because
     * the name ends with {@link mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GENERATED_SURVEY_NAME}.
     * If null this check is not performed.
     * @return the survey type either pre-existing as the survey type property value or determined by logic in
     * this method and then set as the survey type property.  Won't be null.
     */
    public SurveyTypeEnum getSurveyTypePropertyOrSetIt(String surveyName) {
        
        SurveyTypeEnum surveyType = getSurveyType();
        if(surveyType != null) {
            return surveyType;
        }
        
        //the survey is a generated survey from the question bank (survey) for this course
        if(surveyName != null && surveyName.endsWith(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GENERATED_SURVEY_NAME)){
            surveyType = SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK;
        }else if(hasProperty(SurveyPropertyKeyEnum.SCORERS)){
            if(getSurveyScorer().getAttributeScorers().size() == 1){

                Iterator<AttributeScorerProperties> itr = getSurveyScorer().getAttributeScorers().iterator();
                if(itr.next().getAttributeType().equals(LearnerStateAttributeNameEnum.KNOWLEDGE)){
                    surveyType = SurveyTypeEnum.ASSESSLEARNER_STATIC;
                }
            }
            
            if(surveyType == null) {
                surveyType = SurveyTypeEnum.COLLECTINFO_SCORED;
            }
        }

        if(surveyType == null) {
            surveyType = SurveyTypeEnum.COLLECTINFO_NOTSCORED;
        }
        
        setSurveyType(surveyType);
        
        return surveyType;
    }
    
    /**
     * Gather the set of learner state attributes scored in this survey.
     * @return can return an empty set (but not null).
     */
    public Set<LearnerStateAttributeNameEnum> getScoredAttributes(){
        
        Set<LearnerStateAttributeNameEnum> learnerStateAttrs = new HashSet<>();
        if(hasProperty(SurveyPropertyKeyEnum.SCORERS)){
            
            SurveyScorer sScorer = getSurveyScorer();
            if(sScorer != null) {
                
                if(sScorer.getAttributeScorers() != null) {
                    for(AttributeScorerProperties aScorerProperties : sScorer.getAttributeScorers()) {
                        learnerStateAttrs.add(aScorerProperties.getAttributeType());
                    }
                }
                
                TotalScorer tScorer = sScorer.getTotalScorer();
                if(tScorer != null && tScorer.getAttributeScorers() != null) {
                    for(AttributeScorerProperties aScorerProperties : tScorer.getAttributeScorers()) {
                        learnerStateAttrs.add(aScorerProperties.getAttributeType());
                    }
                }
            }
        }
        
        return learnerStateAttrs;
    }
    
    /**
     * Sets the survey type enumeration value.
     * 
     * @param type - The survey type enumeration.  Can't be null.
     */
    public void setSurveyType(SurveyTypeEnum type) {
        
        if(type == null){
            throw new IllegalArgumentException("The survey type can't be null.");
        }
        
        setPropertyValue(SurveyPropertyKeyEnum.SURVEY_TYPE, type.toString());
    }
    
    /**
     * Gets the survey scorer enumeration value (as a SurveyScorer).
     * 
     * @return survey scorer information for this survey. Can be null.
     */
    public SurveyScorer getSurveyScorer(){
    	return (SurveyScorer) getPropertyValue(SurveyPropertyKeyEnum.SCORERS);
    }
    
    /**
     * Sets the survey scorer attributes and info
     */
    public void setSurveyScorer(SurveyScorer scorer){
    	setPropertyValue(SurveyPropertyKeyEnum.SCORERS, scorer);
    }
}
