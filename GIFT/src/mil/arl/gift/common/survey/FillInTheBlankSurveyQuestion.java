/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a fill in the blank survey question
 *
 * @author jleonard
 */
public class FillInTheBlankSurveyQuestion extends AbstractSurveyQuestion<FillInTheBlankQuestion> implements Serializable {
    
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(FillInTheBlankSurveyQuestion.class.getName());
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public FillInTheBlankSurveyQuestion() {
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
    public FillInTheBlankSurveyQuestion(int id, int surveyPageId, FillInTheBlankQuestion question, SurveyItemProperties properties) {
        super(id, surveyPageId, question, properties);
    }
    
    /**
     * Sets the weights associated with each of the reply options
     *
     * @param replyWeights The weights associate with each of the reply options
     */
    public void setReplyWeights(FreeResponseReplyWeights replyWeights) {
        getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, replyWeights);
    }
    
    /**
     * Gets the list of weights associated with each of the reply options
     *
     * @return FreeResponseReplyWeights The weights associated with each of the reply
     * options.  Can be null.
     */
    public FreeResponseReplyWeights getReplyWeights() {
        if((FreeResponseReplyWeights) getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null){
            return (FreeResponseReplyWeights) getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
        } else {
            return (FreeResponseReplyWeights) getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
        }
    }

    @Override
    public double getHighestPossibleScore() {

        if (getReplyWeights() == null) {
//            if (logger.isLoggable(Level.INFO)) {
//                logger.info("Fill in the blank survey question had null reply weights so defaulting to 0");
//            }
            
            return 0;
        }

        return SurveyScorerUtil.getHighestScoreFreeResponse(getReplyWeights().getReplyWeights());
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[FillInTheBlankSurveyQuestion: ");
        sb.append("element = ").append(super.toString());
        sb.append(", highest possible score = ").append(getHighestPossibleScore());
        sb.append(", reply weights = ").append(getReplyWeights());
        sb.append("]");

        return sb.toString();
    }
}
