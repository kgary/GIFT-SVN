/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.List;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a matrix of choices survey question
 *
 * @author jleonard
 */
public class MatrixOfChoicesSurveyQuestion extends AbstractSurveyQuestion<MatrixOfChoicesQuestion> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public MatrixOfChoicesSurveyQuestion() {
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
    public MatrixOfChoicesSurveyQuestion(int id, int surveyPageId, MatrixOfChoicesQuestion question, SurveyItemProperties properties) {
        super(id, surveyPageId, question, properties);
    }

    /**
     * Sets the weights associated with each of the reply options
     *
     * @param replyWeights The weights associate with each of the reply options
     */
    public void setReplyWeights(MatrixOfChoicesReplyWeights replyWeights) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, replyWeights);
    }

    /**
     * Gets the list of weights associated with each of the reply options
     *
     * @return List<Double> The weights associated with each of the reply
     * options
     */
    public MatrixOfChoicesReplyWeights getReplyWeights() {
    	if((MatrixOfChoicesReplyWeights) getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null){
    		return (MatrixOfChoicesReplyWeights) getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
    	} else {
    		return (MatrixOfChoicesReplyWeights) getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
    	}
    }

    @Override
    public double getHighestPossibleScore() {

        if (getReplyWeights() != null) {

            List<List<Double>> replyWeights = getReplyWeights().getReplyWeights();

            double highestPossiblePoints = SurveyScorerUtil.getHighestScoreMatrixOfChoice(replyWeights);

            
            return highestPossiblePoints;

        } else {

            throw new IllegalArgumentException("The answer weights are null");
        }
    }
}
