/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a multiple choice question
 *
 * @author jleonard
 */
public class MultipleChoiceQuestion extends AbstractQuestion implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public MultipleChoiceQuestion() {
    }

    /**
     * Constructor
     *
     * @param questionId The ID of the question.  Provide a value less than 1 if this is a new question.
     * @param text The text of the question
     * @param properties The properties of the question. Can't be null.
     * @param categories The categories the question is in.  Can be null or empty.
     * @param visibleToUserNames User names that can see the question.  Can be null or empty.
     * @param editableToUsernames User names that can edit the question.  Can be null or empty.
     */
    public MultipleChoiceQuestion(int questionId, String text, SurveyItemProperties properties, Collection<String> categories, Collection<String> visibleToUserNames, Collection<String> editableToUsernames) {
        super(questionId, text, properties, categories, visibleToUserNames, editableToUsernames);
    }

    /**
     * Constructor
     *
     * Copies an existing question and gives it a new ID
     *
     * @param question The question to copy
     * @param id The new ID of the question
     */
    public MultipleChoiceQuestion(AbstractQuestion question, int id) {
        super(question, id);
    }

    /**
     * Constructor
     *
     * Copies an existing question and gives it new text
     *
     * @param question The question to copy
     * @param newText The new text
     */
    public MultipleChoiceQuestion(AbstractQuestion question, String newText) {
        super(question, newText);
    }

    /**
     * Gets the option list for the reply choices of the question
     *
     * @return GwtOptionList The option list for the reply choices of the
     * question
     */
    public OptionList getReplyOptionSet() {
        
        return (OptionList) getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
    }

    /**
     * Sets the option list for the reply choices of the question
     *
     * @param replyOptionSet The option list for the reply choices of the
     * question
     */
    public void setReplyOptionSet(OptionList replyOptionSet) {
        
        if (replyOptionSet != null) {
            
            getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, replyOptionSet);
            
        } else {
            
            getProperties().removeProperty(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
        }
    }

    /**
     * Gets the number of minimum selections required for an answer
     *
     * @return Integer The number minimum selections required, null if there is
     * no minimum
     */
    public Integer getMinimumSelectionsRequired() {
        
        return getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY);
    }
    
    /**
     * Determines if the multi select enabled property is set to true or false.  This will return false
     * if the property is not set.   Use the hasMultiSelectEnabledProperty() function to determine if the 
     * property exists first.
     * 
     * @return True if the multi select enabled property is set, false otherwise.
     */
    public Boolean getIsMultiSelectEnabled() {
        
        Boolean isMultiSelectEnabled = false;
        
        if (getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED) != null) {
            isMultiSelectEnabled = getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED);
        }
        
        return isMultiSelectEnabled;
    }
    
    /**
     * Determines if the multi select property exists for this question.
     * 
     * @return - True if the multi select property exists, false otherwise.
     */
    public Boolean hasMultiSelectEnabledProperty() {
        Boolean hasProperty = false;
        
        if (getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED) != null) {
            hasProperty = true;
        }
        
        return hasProperty;
    }

    /**
     * Sets the number of minimum selections required for an answer
     *
     * @param selections The number of minimum selections required, null or 0 if
     * there is no minimum
     */
    public void setMinimumSelectionsRequired(Integer selections) {
        
        if (selections != null && selections > 0) {
            
            getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY, selections);
            
        } else {
            
            getProperties().removeProperty(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY);
        }
    }

    /**
     * Gets the maximum number of selections allowed for an answer
     *
     * @return Integer The maximum number of selections allowed for an answer. If an value of zero or less
     * was stored for the property than either the number of choices will be returned to maximize the possible
     * selections allowed or the value for the minimum number of selections required will be returned.  This is to prevent
     * issues when taking a survey question where multi select is enabled.
     */
    public Integer getMaximumSelectionsAllowed() {
        
        Integer value = getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY);
        if(value != null && value <= 0 && getIsMultiSelectEnabled()){
            
            if(getReplyOptionSet() != null && getReplyOptionSet().getListOptions() != null){
                return getReplyOptionSet().getListOptions().size();
            }else if(getMinimumSelectionsRequired() != null){
                return getMinimumSelectionsRequired();
            }
        }
        
        return value;
    }

    /**
     * Sets the maximum number of selections allowed for an answer
     *
     * @param selections The maximum number of selections allowed for an answer,
     * null or 0 if there is no maximum
     */
    public void setMaximumSelectionsAllowed(Integer selections) {
        
        if (selections != null && selections > 0) {
            
            getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY, selections);
            
        } else {
            
            getProperties().removeProperty(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY);
        }
    }

    /**
     * Gets if the multiple choice question has a correct answer(s).  A correct answer means that there is a way
     * to get a positive score if you answer this question.
     *
     * @return boolean If the question has a correct answer(s)
     */
	public boolean hasCorrectAnswers() {
	    
	    double highestScore = getHighestPossibleScore();
	    return highestScore > 0.0;
    }

    /**
     * Sets the weights associated with each of the reply options
     *
     * @param replyWeights The weights associate with each of the reply options
     */
    public void setReplyWeights(List<Double> replyWeights) {
        
        String replyWeightsListString = SurveyItemProperties.encodeDoubleListString(replyWeights);
        
        getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, replyWeightsListString);
    }

    /**
     * Gets the list of weights associate with each of the reply options.<br/>
     * If the reply weights are authored but are a different size than the question choices, 
     * the number of weights will be manipulated in this method to match the number of choices
     * (the end of the list is manipulated, items being added or removed as needed). 
     *
     * @return The weights associated with each of the reply
     * options.  Can be null.
     */
    public List<Double> getReplyWeights() {

        String replyWeightsListString = (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);

        if (replyWeightsListString != null) {

            List<Double> replyWeights = SurveyItemProperties.decodeDoubleListString(replyWeightsListString);
            int replyOptionSize = getReplyOptionSet() != null ? getReplyOptionSet().getListOptions().size() : 0;
            
            if (replyWeights.size() != replyOptionSize) {
                // sanity check - the reply weights are not in sync with the number of choices in the question.  Make the number of weights match
                //                the number of choices to prevent issues down stream.
                
                if(replyWeights.size() > replyOptionSize){
                    //remove weights at end of list
                    int numToRemove = replyWeights.size() - replyOptionSize;
                    for(int cnt = 0; cnt < numToRemove; cnt++){
                        replyWeights.remove(replyWeights.size() - 1);
                    }
                }else{
                    //add 0.0 weights to the end of the list
                    int numToAdd = replyOptionSize - replyWeights.size();
                    for(int cnt = 0; cnt < numToAdd; cnt++){
                        replyWeights.add(0.0);
                    }
                }
            }

            return replyWeights;
            
        } else {

            return null;
        }
    }

    /**
     * Removes the weights associated with each of the reply options
     */
    public void removeReplyWeights() {
        
        getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
    }

    @Override
    public double getHighestPossibleScore() {
        
        List<Double> weights = getReplyWeights();
        
        double highestPossiblePoints = 0.0;
        
        if(weights != null) {
            // If there is no maximum selections, then the default number of max selections is one
            int maxSelections = (getMaximumSelectionsAllowed() != null && getMaximumSelectionsAllowed() != 0) ? getMaximumSelectionsAllowed() : 1;
            
            // if there is no minimum selections, then the default number is zero
            int minSelections = getMinimumSelectionsRequired() != null ? getMinimumSelectionsRequired() : 0;

            try {
            	highestPossiblePoints = SurveyScorerUtil.getHighestScoreMultipleChoice(weights,  minSelections, maxSelections);
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            	setMaximumSelectionsAllowed(1);
            	setMinimumSelectionsRequired(0);
            	highestPossiblePoints = SurveyScorerUtil.getHighestScoreMultipleChoice(weights,  0, 1);
            }
        } 
        
        return highestPossiblePoints;
    }

}
