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
 * A response to a matrix of choices question
 *
 * @author jleonard
 */
public class MatrixOfChoicesQuestionResponse extends AbstractQuestionResponse {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public MatrixOfChoicesQuestionResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question the response is for
     * @param responses The responses to this question
     */
    public MatrixOfChoicesQuestionResponse(MatrixOfChoicesSurveyQuestion surveyQuestion, List<QuestionResponseElement> responses) {
        super(surveyQuestion, responses);
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question this is a response for
     * @param surveyPageResponseId The ID of the survey page response this is in
     * @param responses The responses to this question
     */
    public MatrixOfChoicesQuestionResponse(MatrixOfChoicesSurveyQuestion surveyQuestion, int surveyPageResponseId, List<QuestionResponseElement> responses) {
        super(surveyQuestion, surveyPageResponseId, responses);
    }

    @Override
    public MatrixOfChoicesSurveyQuestion getSurveyQuestion() {

        return (MatrixOfChoicesSurveyQuestion) super.getSurveyQuestion();
    }
    
        /**
     * Gets the total points for this response
     *
     * @return double The total points for this response
     */
    public double getPoints() {

        //calculate weight of responses provided
        double responseWeight = 0;
        for (QuestionResponseElement responseElement : getResponses()) {

            //look at the question's responses and gather score attributes
            ListOption reply = responseElement.getRowChoice();
            
            ListOption columnReply = responseElement.getChoice();
            if (columnReply == null) {
                continue;
            }

            List<ListOption> rowOptions = responseElement.getRowChoices().getListOptions();

            int row;
      
            int col = 0;

            boolean found = false;

            for (row = 0; row < rowOptions.size(); row++) {

                if (reply.getId() == rowOptions.get(row).getId()) {

                    List<ListOption> columnOptions = responseElement.getChoices().getListOptions();

                    for (col = 0; col < columnOptions.size(); col++) {

                        if (columnReply.getId() == columnOptions.get(col).getId()) {

                            found = true;

                            break;
                        }
                    }

                    if (found) {

                        break;
                    }
                }
            }

            if (found) {

                responseWeight += getSurveyQuestion().getReplyWeights().getReplyWeight(row, col);
            }
        }

        return responseWeight;
    }
}
