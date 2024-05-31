/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a fill in the blank question
 *
 * @author jleonard
 */
public class FillInTheBlankQuestion extends AbstractQuestion implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public FillInTheBlankQuestion() {
    }

    /**
     * Constructor
     *
     * @param questionId The ID of the question
     * @param text The text of the question
     * @param properties The properties of the question
     * @param categories The categories the question is in
     * @param visibleToUserNames User names that can see the question
     * @param editableToUsernames User names that can edit the question
     */
    public FillInTheBlankQuestion(int questionId, String text, SurveyItemProperties properties, Collection<String> categories, Collection<String> visibleToUserNames, Collection<String> editableToUsernames) {
        super(questionId, text, properties, categories, visibleToUserNames, editableToUsernames);
    }

    /**
     * Constructor
     *
     * Copies an existing question and gives it a new ID
     *
     * @param question The question to copy
     * @param newId The new ID of the question
     */
    public FillInTheBlankQuestion(AbstractQuestion question, int newId) {
        super(question, newId);
    }

    /**
     * Constructor
     *
     * Copies an existing question and gives it new text
     *
     * @param question The question to copy
     * @param newText The new text
     */
    public FillInTheBlankQuestion(AbstractQuestion question, String newText) {
        super(question, newText);
    }

    /**
     * Returns if the answer field for the question is a text box
     *
     * @return boolean If the answer field for the question is a text box
     */
    public boolean isAnswerFieldTextBox() {

        Boolean value = getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY);
        return value != null ? value : false;
    }

    /**
     * Sets if the answer field for the question is a text box
     *
     * @param value If the answer field for the question is a text box
     */
    public void setIsAnswerFieldTextBox(boolean value) {

        getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY, value);
    }

    /**
     * Gets the list of weights associated with each of the reply options. Can be null.
     *
     * @return FreeResponseReplyWeights The weights associated with each of the reply options
     */
    public FreeResponseReplyWeights getReplyWeights() {
        return (FreeResponseReplyWeights) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
    }

    @Override
    public double getHighestPossibleScore() {
        double highestPossiblePoints = 0.0;

        if (getReplyWeights() != null) {
            highestPossiblePoints = SurveyScorerUtil.getHighestScoreFreeResponse(getReplyWeights().getReplyWeights());
        }

        return highestPossiblePoints;
    }

    /**
     * Gets the response field types (e.g. Numeric, Free Text, etc...) for the question
     *
     * @return List<String> the list of response field types
     */
    public List<String> getResponseFieldTypes() {

        // retrieve types list from properties
        return SurveyItemProperties.decodeListString((String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES),
                Constants.COMMA);
    }

    /**
     * Gets the response field labels for the question.
     *
     * @return List<String> the list of response field labels
     */
    public List<String> getResponseFieldLabels() {

        // retrieve labels list from properties
        return SurveyItemProperties.decodeListString((String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS),
                Constants.PIPE);
    }
    
    /**
     * Returns the list of response field left aligned booleans
     * 
     * @return the list of response field booleans indicating if the label is left aligned
     */
    public List<String> getResponseFieldLeftAligned() {
        return SurveyItemProperties.decodeListString((String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED),
                Constants.COMMA);
    }

    /**
     * Gets the number of response fields per line for the question. Can be null if the property doesn't exist.
     *
     * @return Integer the list of response field labels
     */
    public Integer getNumberResponseFieldsPerLine() {

        // retrieve the number of response fields per line from properties
        return getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE);
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[FillInTheBlankQuestion: ");
        sb.append("element = ").append(super.toString());
        sb.append(", highest possible score = ").append(getHighestPossibleScore());
        sb.append(", number of response fields per line = ").append(getNumberResponseFieldsPerLine());
        sb.append(", is answer field text box = ").append(isAnswerFieldTextBox());
        sb.append(", response field types = ").append(getResponseFieldTypes());
        sb.append(", reply weights = ").append(getReplyWeights());
        sb.append(", response field labels = ").append(getResponseFieldLabels());
        sb.append("]");

        return sb.toString();
    }
}
