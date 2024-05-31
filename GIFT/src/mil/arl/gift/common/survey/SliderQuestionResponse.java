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
 * A response to a slider question
 *
 * @author jleonard
 */
public class SliderQuestionResponse extends AbstractQuestionResponse {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SliderQuestionResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question the response is for
     * @param responses The responses to this question
     */
    public SliderQuestionResponse(SliderSurveyQuestion surveyQuestion, List<QuestionResponseElement> responses) {
        super(surveyQuestion, responses);
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question this is a response for
     * @param surveyPageResponseId The ID of the survey page response this is in
     * @param responses The responses to this question
     */
    public SliderQuestionResponse(SliderSurveyQuestion surveyQuestion, int surveyPageResponseId, List<QuestionResponseElement> responses) {
        super(surveyQuestion, surveyPageResponseId, responses);
    }

    @Override
    public SliderSurveyQuestion getSurveyQuestion() {

        return (SliderSurveyQuestion) super.getSurveyQuestion();
    }

    /**
     * Gets the total points for this response
     *
     * @return double The total points for this response
     */
    public double getPoints() {

        List<QuestionResponseElement> responses = getResponses();

        if (responses != null && !responses.isEmpty()) {
            
            String pointsText = getResponses().get(0).getText();

            double points;
            try {
                points = Double.parseDouble(pointsText);
            } catch (@SuppressWarnings("unused") Exception e) {
                // if pointsText is not a number, default to 0
                points = 0;
            }
            
            return points;
            
        } else {

            return 0;
        }
    }
}
