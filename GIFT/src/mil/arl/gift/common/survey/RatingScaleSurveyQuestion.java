/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a rating scale survey question
 *
 * @author jleonard
 */
public class RatingScaleSurveyQuestion extends AbstractSurveyQuestion<RatingScaleQuestion> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public RatingScaleSurveyQuestion() {
        super();
    }

    /**
     * Constructor
     *
     * @param id The ID of the survey question
     * @param surveyPageId The ID of the survey page this survey question is in
     * @param question The question of this survey question
     * @param properties The properties of the survey question
     */
    public RatingScaleSurveyQuestion(int id, int surveyPageId, RatingScaleQuestion question, SurveyItemProperties properties) {
        super(id, surveyPageId, question, properties);
    }

    /**
     * Gets if the multiple choice question has a correct answer(s)
     *
     * @return boolean If the question has a correct answer(s)
     */
	public boolean hasCorrectAnswers() {
        if(getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null &&  !SurveyItemProperties.decodeDoubleListString((String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS)).isEmpty() &&
        		Collections.max(SurveyItemProperties.decodeDoubleListString((String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS))) != 0.0){
        	return true;
        }if(getQuestion().hasCorrectAnswers()){
        	return true;
        }
        return false;
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
     * Gets the list of weights associated with each of the reply options
     *
     * @return List<Double> The weights associated with each of the reply
     * options
     */
    public List<Double> getReplyWeights() {

        String replyWeightsListString = getStringPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);

        if (replyWeightsListString != null && !replyWeightsListString.isEmpty()) {

            List<Double> replyWeights = SurveyItemProperties.decodeDoubleListString(replyWeightsListString);
            
            if (replyWeights.size() != (getQuestion().getReplyOptionSet() != null ? getQuestion().getReplyOptionSet().getListOptions().size() : 0)) {
            	
            	return null;
            }

            return replyWeights;

        } else {

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

    @Override
    public double getHighestPossibleScore() {

        List<Double> weights = getReplyWeights();

        if (weights != null) {

            double highestPossiblePoints = SurveyScorerUtil.getHighestScoreRatingScale(weights);

            return highestPossiblePoints;

        } else {
            return 0.0;
        }
    }
}
