/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.util.ArrayList;
import java.util.List;

/**
 * A response for a multiple choice question
 *
 * @author jleonard
 */
public class MultipleChoiceQuestionResponse extends AbstractQuestionResponse {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public MultipleChoiceQuestionResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question the response is for
     * @param responses The responses to this question
     */
    public MultipleChoiceQuestionResponse(MultipleChoiceSurveyQuestion surveyQuestion, List<QuestionResponseElement> responses) {
        super(surveyQuestion, responses);
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question this is a response for
     * @param surveyPageResponseId The ID of the survey page response this is in
     * @param responses The responses to this question
     */
    public MultipleChoiceQuestionResponse(MultipleChoiceSurveyQuestion surveyQuestion, int surveyPageResponseId, List<QuestionResponseElement> responses) {
        super(surveyQuestion, surveyPageResponseId, responses);
    }
    
    @Override
    public MultipleChoiceSurveyQuestion getSurveyQuestion() {
        
        return (MultipleChoiceSurveyQuestion) super.getSurveyQuestion();
    }

    /**
     * Gets the total points for this response
     * 
     * @return The total points for this response
     */
    public double getPoints() {

        //calculate weight of responses provided - Note: since this is a multiple choice question, the choices are considered
        //                                               grouped for the purpose of scoring.
        double responseWeight = 0;
        List<Double> replyWeights = getSurveyQuestion().getReplyWeights();
        if(replyWeights != null){
            for (QuestionResponseElement responseElement : getResponses()) {
    
                //look at the question's responses and gather score attributes
                String replyText = responseElement.getText();
                if (replyText == null) {
                    continue;
                }
                responseWeight += replyWeights.get(responseElement.getColumnIndex());
            }
        }
        
        return responseWeight;
    }
    
    /**
     * Return the list of external application object ids for the choices made for this
     * multiple choice question.
     * 
     * @return the list of object ids associated with the multiple choice question response(s).  Can be empty but 
     * not null.  The size may not equal the number of responses if a response is not associated with an object id.
     * @throws Exception if there was a problem finding the index of a response choice
     */
    public List<String> getResponseObjectIds() throws Exception{
        
        MultipleChoiceSurveyQuestion surveyQuestion = getSurveyQuestion();
        
        List<String> objectIds = new ArrayList<>(0);
        
        List<String> questionChoices = surveyQuestion.getQuestionChoices();
        List<String> questionChoiceObjectIds = surveyQuestion.getQuestionChoicesObjectIds();
        
        //find the index of the choice for the response in order to use that index to get the object id for that choice
        List<QuestionResponseElement> responses = getResponses();
        for(QuestionResponseElement response : responses){
            
            String answerText = response.getText();
            int choiceIndex = questionChoices.indexOf(answerText);
            if(choiceIndex == -1){
                throw new Exception("Failed to retrieve the training application object id for the question response text of '"+answerText+"' because that text doesn't correspond to a valid choice for the multiple choice question.");                
            }else if(choiceIndex >= questionChoiceObjectIds.size()){
                throw new Exception("Failed to retrieve the training application object id for the question response text of '"+answerText+"' because there is a mismatch between the number of question choices and the number of training application object ids.");
            }
            
            String objectId = questionChoiceObjectIds.get(choiceIndex);
            if(!objectId.isEmpty()){
                //possible the choice isn't associated with a training application object, in which case
                //don't add it to the list
                objectIds.add(objectId);
            }
            
            
        }//end for
        
        return objectIds;
    }
}
