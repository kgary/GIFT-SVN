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
import java.util.Date;
import java.util.List;

/**
 * An abstract response to a survey question
 *
 * @author jleonard
 */
public abstract class AbstractQuestionResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private int surveyPageResponseId;
    private AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion;
    private List<QuestionResponseElement> responseElements = new ArrayList<QuestionResponseElement>();
    
    /** The IDs of the options that were presented with the question, in the order that they were shown to the learner */
    private List<Integer> optionOrder;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public AbstractQuestionResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question the response is for
     * @param responses collection of responses to the question
     */
    public AbstractQuestionResponse(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, List<QuestionResponseElement> responses) {

        this.surveyQuestion = surveyQuestion;
        this.responseElements.addAll(responses);
    }

    /**
     * Constructor
     *
     * @param surveyQuestion The survey question this is a response for
     * @param surveyPageResponseId The ID of the survey page response this is in
     * @param responses collection of responses to the question
     */
    public AbstractQuestionResponse(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, int surveyPageResponseId, List<QuestionResponseElement> responses) {
        this(surveyQuestion, responses);

        this.surveyPageResponseId = surveyPageResponseId;
    }
    
    /**
     * Returns the most recent time an answer was provided for this question.  If there
     * is more than one answer (e.g. multiple choice multiple select) than all choices are
     * looked at.
     * 
     * @return the most recent answer time.  Will be null if no answer was given.
     */
    public Date getMostRecentAnswerTime(){
        
        Date recent = null;
        for(QuestionResponseElement response : responseElements){
            
            if(response != null && (recent == null || response.getAnswerTime().compareTo(recent) > 0)){
                recent = response.getAnswerTime();
            }
        }
        
        return recent;
    }

    /**
     * Gets the survey question the response is for
     *
     * @return GwtSurveyQuestion The survey question the response is for
     */
    public AbstractSurveyQuestion<? extends AbstractQuestion> getSurveyQuestion() {

        return surveyQuestion;
    }

    /**
     * Sets the survey question the response is for
     *
     * @param surveyQuestion The survey question the response is for
     */
    public void setSurveyQuestion(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion) {

        this.surveyQuestion = surveyQuestion;
    }

    /**
     * Gets the ID of the survey page this response is in
     *
     * @return int The ID of the survey page this response is in
     */
    public int getSurveyPageResponseId() {

        return surveyPageResponseId;
    }

    /**
     * Sets the ID of the survey page this response is in
     *
     * @param surveyPageResponseId The ID of the survey page this response is in
     */
    public void setSurveyPageResponseId(int surveyPageResponseId) {

        this.surveyPageResponseId = surveyPageResponseId;
    }

    /**
     * Gets the responses to the question
     *
     * @return List<QuestionResponseElement> The responses to the question
     */
    public List<QuestionResponseElement> getResponses() {

        return responseElements;
    }
    
    /**
     * Gets the order that the question's listed options were shown in
     * 
     * @return the order that the question's listed options were shown in. Can return null.
     */
    public List<Integer> getOptionOrder() {
        return optionOrder;
    }

    /**
     * Sets the order that the question's listed options were shown in
     * 
     * @param options the order that the question's listed options were shown in. Can be null.
     */
    public void setOptions(List<Integer> options) {
        this.optionOrder = options;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("survey question = ").append(getSurveyQuestion());
        sb.append(", survey page response id = ").append(getSurveyPageResponseId());
        sb.append(", responses = [").append(getSurveyPageResponseId());
        for (QuestionResponseElement responseElem : responseElements) {

            sb.append(responseElem).append(", ");
        }
        sb.append("]");
        sb.append(", optionOrder = ").append(getOptionOrder());
        return sb.toString();
    }

    /**
     * Creates a response for a survey question
     *
     * @param surveyQuestion The survey question
     * @param responses The responses to the question
     * @return QuestionResponse The response to the survey question
     */
    public static AbstractQuestionResponse createResponse(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, List<QuestionResponseElement> responses) {

        if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {

            return new FreeResponseQuestionResponse((FillInTheBlankSurveyQuestion) surveyQuestion, responses);

        } else if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {

            return new MultipleChoiceQuestionResponse((MultipleChoiceSurveyQuestion) surveyQuestion, responses);

        } else if (surveyQuestion instanceof RatingScaleSurveyQuestion) {

            return new RatingScaleQuestionResponse((RatingScaleSurveyQuestion) surveyQuestion, responses);

        } else if (surveyQuestion instanceof MatrixOfChoicesSurveyQuestion) {

            return new MatrixOfChoicesQuestionResponse((MatrixOfChoicesSurveyQuestion) surveyQuestion, responses);

        } else if (surveyQuestion instanceof SliderSurveyQuestion) {

            return new SliderQuestionResponse((SliderSurveyQuestion) surveyQuestion, responses);

        } else if (surveyQuestion != null) {

            throw new IllegalArgumentException("Unknown question type: " + surveyQuestion.getClass().getName());

        } else {

            throw new NullPointerException("Cannot create the response to a null survey question");
        }
    }

    /**
     * Creates a response for a survey question in an existing survey page
     * response
     *
     * @param surveyQuestion The survey question
     * @param surveyPageResponseId The survey page response ID
     * @param responses The responses to the question
     * @return QuestionResponse The response to the survey question
     */
    public static AbstractQuestionResponse createResponse(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, int surveyPageResponseId, List<QuestionResponseElement> responses) {

        if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {

            return new FreeResponseQuestionResponse((FillInTheBlankSurveyQuestion) surveyQuestion, surveyPageResponseId, responses);

        } else if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {

            return new MultipleChoiceQuestionResponse((MultipleChoiceSurveyQuestion) surveyQuestion, surveyPageResponseId, responses);

        } else if (surveyQuestion instanceof RatingScaleSurveyQuestion) {

            return new RatingScaleQuestionResponse((RatingScaleSurveyQuestion) surveyQuestion, surveyPageResponseId, responses);

        } else if (surveyQuestion instanceof MatrixOfChoicesSurveyQuestion) {

            return new MatrixOfChoicesQuestionResponse((MatrixOfChoicesSurveyQuestion) surveyQuestion, surveyPageResponseId, responses);

        } else if (surveyQuestion instanceof SliderSurveyQuestion) {

            return new SliderQuestionResponse((SliderSurveyQuestion) surveyQuestion, surveyPageResponseId, responses);

        } else if (surveyQuestion != null) {

            throw new IllegalArgumentException("Unknown question type: " + surveyQuestion.getClass().getName());

        } else {

            throw new NullPointerException("Cannot create the response to a null survey question");
        }
    }
}
