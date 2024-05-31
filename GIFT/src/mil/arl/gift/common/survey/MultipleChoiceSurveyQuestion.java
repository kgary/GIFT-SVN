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
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a multiple choice survey question
 *
 * @author jleonard
 */
public class MultipleChoiceSurveyQuestion extends AbstractSurveyQuestion<MultipleChoiceQuestion> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public MultipleChoiceSurveyQuestion() {
        super();
    }

    /**
     * Constructor
     *
     * @param id The ID of the survey question
     * @param surveyPageId The ID of the survey page this survey question is in
     * @param question The question of this survey question
     * @param properties The properties of the survey question.  Can't be null.
     */
    public MultipleChoiceSurveyQuestion(int id, int surveyPageId, MultipleChoiceQuestion question, SurveyItemProperties properties) {
        super(id, surveyPageId, question, properties);
    }

    /**
     * Gets if the question should randomize its reply options
     *
     * This only applies to multiple choice questions currently
     *
     * @return boolean If the question should randomize its reply options
     */
    public boolean getRandomizeReplyOptions() {

        Boolean randomVal = getBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE);
        
        if (randomVal == null) {
            randomVal = false;
        }
        
        return randomVal;
    }

    /**
     * Sets if the question should randomize its reply options
     *
     * This only applies to multiple choice questions currently
     *
     * @param randomize If the question should randomize its reply options
     */
    public void setRandomizeReplyOption(boolean randomize) {

        getProperties().setBooleanPropertyValueRemoveFalse(SurveyPropertyKeyEnum.RANDOMIZE, randomize);
    }
    
    /**
     * Gets if the multiple choice question has a correct answer(s). A correct answer means that there is a way
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
     * Return the list of question choices feedback.  
     * 
     * @return the list of feedbacks, one for each question choice.  Can be null.  If not null the size will match the number of question choices.
     */
    public List<String> getReplyFeedbacks(){
       
        String feedbackDelimStr = getStringPropertyValue(SurveyPropertyKeyEnum.REPLY_FEEDBACK);

        if(feedbackDelimStr != null){
            String [] tokens = feedbackDelimStr.split("\\"+SurveyPropertyKeyEnum.REPLY_FEEDBACK.getListDelimiter());
            List<String> feedbacks = new ArrayList<>(tokens.length);
            Collections.addAll(feedbacks, tokens);
            
            return feedbacks;
        }else{
            return null;
        }
    }
    
    /**
     * Return the choices for the question.
     * 
     * @return the choices for this question.  Will be null if the property is not found.
     */
    public OptionList getChoices(){

        OptionList optionList = (OptionList)getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                
        return optionList;
    }
    
    /**
     * Return the list of question choices.
     * 
     * @return list of question choices.  Will be null if the property is not found.
     */
    public List<String> getQuestionChoices(){
        
        OptionList optionList = getChoices();
        
        if(optionList != null){
            List<String> choices = new ArrayList<>(optionList.getListOptions().size());
            for(ListOption listOption : optionList.getListOptions()){
                
                choices.add(listOption.getText());
            }
            
            return choices;    
        }else{
            return null;
        }
    }     
    
    /**
     * Return the list of question choices object ids.  
     * 
     * @return the list of object ids, one for each question choice.  The size will match the number of question choices.  Will be null
     * if the property is not found.  An empty object id is valid in the list.
     */
    public List<String> getQuestionChoicesObjectIds(){
        
        SurveyItemProperties properties;
        if(getProperties().hasProperty(SurveyPropertyKeyEnum.REPLY_EXTERNAL_TA_OBJ_ID)){
            properties = getProperties();
        }else{
            properties = getQuestion().getProperties();
        }
        
        List<String> objectIds = properties.getStringListPropertyValue(SurveyPropertyKeyEnum.REPLY_EXTERNAL_TA_OBJ_ID);        
        return objectIds;
    } 
    
    /**
     * Return the list of validated question choices object ids. Validation logic includes checking
     * whether there are duplicate object ids or if the size of the object ids list matches the option list size.
     * Keep in mind that an empty object id is valid therefore a list could contain 4 entries but only 3 object ids.  
     * 
     * @return the list of validated object ids 
     * @throws Exception if there was a validation issue
     */
    public List<String> getAndValidateObjectIds() throws Exception{
        
        List<String> objectIds = getQuestionChoicesObjectIds();
        if(objectIds == null || objectIds.isEmpty()){
            return null;
        }        
                               
        //make sure object ids are unique, otherwise how do we know what the learner choose
        String duplicateId = SurveyItemProperties.findDuplicateEntry(objectIds);                            
        
        if(duplicateId != null){
            //ERROR - found matching object ids
            throw new Exception("Found a duplicate external training application object id ("+duplicateId+") in the multiple choice question (id "+getId()+").  This doesn't make logical sense as there is more than 1 choice that maps to the same object.");
        }
        
        //make sure same number of object ids as question choices
        OptionList optionList = (OptionList) getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
        if(optionList == null){
            //check survey question's question
            optionList = (OptionList) getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
        }
        
        if(objectIds.size() != optionList.getListOptions().size()){
            //ERROR - mismatch of object ids for choice to the number of choices for the question
            throw new Exception("Found a mismatch between the number of external training application object ids ("+objectIds.size()+") to the number of question choices ("+optionList.getListOptions().size()+") in the multiple choice question in the multiple choice question (id "+getId()+").");
        }
        
        return objectIds;
    }

    /**
     * Gets the list of weights associated with each of the reply options.  Uses
     * the survey question's property first and if null, the question's property is used.<br/>
     * If the reply weights are authored but are a different size than the question choices, 
     * the number of weights will be manipulated in this method to match the number of choices
     * (the end of the list is manipulated, items being added or removed as needed). 
     *
     * @return The weights associated with each of the reply
     * options.  Can be null.  
     */
    public List<Double> getReplyWeights() {

        // try to get the reply weights from the survey question properties
        String replyWeightsListString = getStringPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);

        if (replyWeightsListString != null && !replyWeightsListString.isEmpty()) {

            List<Double> replyWeights = SurveyItemProperties.decodeDoubleListString(replyWeightsListString);
            int replyOptionSize = getQuestion().getReplyOptionSet() != null ? getQuestion().getReplyOptionSet().getListOptions().size() : 0;
            
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
            // try to get the reply weights from the question's properties
            
            List<Double> replyWeights = getQuestion().getReplyWeights();

            return replyWeights;
        }
    }

    @Override
    public double getHighestPossibleScore() {
        
        List<Double> weights = getReplyWeights();
        
        double highestPossiblePoints = 0.0;
        
        if(weights != null) {
            // If there is no maximum selections, then the default number of max selections is one
            int maxSelections = (getQuestion().getMaximumSelectionsAllowed() != null && getQuestion().getMaximumSelectionsAllowed() != 0) ? getQuestion().getMaximumSelectionsAllowed() : 1;
            
            // If there is no min selections, then the default number of min selections is zero
            int minSelections = (getQuestion().getMinimumSelectionsRequired() != null && getQuestion().getMinimumSelectionsRequired() != 0) ? getQuestion().getMinimumSelectionsRequired() : 0;

            try {
            	highestPossiblePoints = SurveyScorerUtil.getHighestScoreMultipleChoice(weights,  minSelections, maxSelections);
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            	getQuestion().setMaximumSelectionsAllowed(1);
            	getQuestion().setMinimumSelectionsRequired(0);
            	highestPossiblePoints = SurveyScorerUtil.getHighestScoreMultipleChoice(weights,  0, 1);
            }
        } 
        
        return highestPossiblePoints;
    }
    
    /**
     * Return the question text for the multiple choice question provided.
     * 
     * @return the question text.  Won't be null or empty.
     */
    public String getQuestionText(){
        return getQuestion().getText();
    }
    
    
    /**
     * Change the multiple choice question text.
     * 
     * @param newQuestionText the new question text.  Can't be null or empty.
     */
    public void setQuestionText(String newQuestionText){
        getQuestion().setText(newQuestionText);
    } 
}
