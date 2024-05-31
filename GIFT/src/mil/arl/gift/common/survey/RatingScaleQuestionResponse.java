/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.util.List;

/**
 * A response to a rating scale question
 *
 * @author jleonard
 */
public class RatingScaleQuestionResponse extends AbstractQuestionResponse {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public RatingScaleQuestionResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question the response is for
     * @param responses The responses to this question
     */
    public RatingScaleQuestionResponse(RatingScaleSurveyQuestion surveyQuestion, List<QuestionResponseElement> responses) {
        super(surveyQuestion, responses);
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question this is a response for
     * @param surveyPageResponseId The ID of the survey page response this is in
     * @param responses The responses to this question
     */
    public RatingScaleQuestionResponse(RatingScaleSurveyQuestion surveyQuestion, int surveyPageResponseId, List<QuestionResponseElement> responses) {
        super(surveyQuestion, surveyPageResponseId, responses);
    }

    @Override
    public RatingScaleSurveyQuestion getSurveyQuestion() {

        return (RatingScaleSurveyQuestion) super.getSurveyQuestion();
    }

    /**
     * Gets the total points for this response
     *
     * @return double The total points for this response
     */
    public double getPoints() {

        //calculate weight of responses provided - Note: since this is a multiple choice question, the choices are considered
        //                                               grouped for the purpose of scoring.
        double responseWeight = 0;
        for (QuestionResponseElement responseElement : getResponses()) {

            //look at the question's responses and gather score attributes
            String replyText = responseElement.getText();
            if (replyText == null) {
                continue;
            }

            responseWeight += getSurveyQuestion().getReplyWeights().get(responseElement.getColumnIndex());
        }

        return responseWeight;
    }
}
