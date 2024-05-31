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
 * A response to a free response / fill in the blank question
 *
 * @author jleonard
 */
public class FreeResponseQuestionResponse extends AbstractQuestionResponse {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public FreeResponseQuestionResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question the response is for
     * @param responses The responses to this question
     */
    public FreeResponseQuestionResponse(FillInTheBlankSurveyQuestion surveyQuestion, List<QuestionResponseElement> responses) {
        super(surveyQuestion, responses);
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question this is a response for
     * @param surveyPageResponseId The ID of the survey page response this is in
     * @param responses The responses to this question
     */
    public FreeResponseQuestionResponse(FillInTheBlankSurveyQuestion surveyQuestion, int surveyPageResponseId, List<QuestionResponseElement> responses) {
        super(surveyQuestion, surveyPageResponseId, responses);
    }

    @Override
    public FillInTheBlankSurveyQuestion getSurveyQuestion() {

        return (FillInTheBlankSurveyQuestion) super.getSurveyQuestion();
    }

    /**
     * Gets the scored points for the response field.
     * 
     * @param responseFieldIndex the index of the response field.
     * @return the scored value.
     */
    private double getEarnedPointsForResponseField(int responseFieldIndex) {
        double responseWeight = 0;

        if (getSurveyQuestion() != null && getSurveyQuestion().getReplyWeights() != null) {
            List<List<List<Double>>> replyWeights = getSurveyQuestion().getReplyWeights().getReplyWeights();
            if (responseFieldIndex + 1 > replyWeights.size()) {
                return responseWeight;
            }

            Double replyNumber = null;
            // look at the question's responses and gather score attributes
            for (List<Double> rowWeights : replyWeights.get(responseFieldIndex)) {
                
                if (rowWeights.isEmpty()) {
                    continue;
                }
                
                Double scoreValue = rowWeights.get(0);
                if (rowWeights.size() == 1) {
                    // this is the default condition
                    // always 0 for free text responses
                    // authored score for catch-all numeric responses (defaults to 0)
                    responseWeight += scoreValue;
                    break;
                } else {
                    // numeric response, only need to parse value one time
                    if (replyNumber == null) {
                        try {
                            replyNumber = Double.valueOf(getResponses().get(responseFieldIndex).getText());
                        } catch (@SuppressWarnings("unused") Exception e) {
                            continue;
                        }
                    }

                    // check if it is a single value or a range
                    if (rowWeights.size() == 2 && Double.compare(replyNumber, rowWeights.get(1)) == 0) {
                        responseWeight += scoreValue;
                        break;
                    } else if (rowWeights.size() == 3 && rowWeights.get(1) <= replyNumber && replyNumber <= rowWeights.get(2)) {
                        responseWeight += scoreValue;
                        break;
                    }
                }
            }
        }

        return responseWeight;
    }

    /**
     * Gets the total points for this response
     *
     * @return double The total points for this response
     */
    public double getEarnedPoints() {

        double responseWeight = 0;

        if (getSurveyQuestion() != null && getSurveyQuestion().getReplyWeights() != null) {
            List<List<List<Double>>> replyWeights = getSurveyQuestion().getReplyWeights().getReplyWeights();
            for (int i = 0; i < getResponses().size(); i++) {
                if (i + 1 > replyWeights.size()) {
                    break;
                }

                responseWeight += getEarnedPointsForResponseField(i);
            }
        }

        return responseWeight;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[FreeResponseQuestionResponse: ");
        sb.append("element = ").append(super.toString());
        sb.append(", earned points = ").append(getEarnedPoints());
        sb.append("]");

        return sb.toString();
    }
}
