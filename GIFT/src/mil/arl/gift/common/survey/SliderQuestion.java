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

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a slider question
 *
 * @author jleonard
 */
public class SliderQuestion extends AbstractQuestion implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SliderQuestion() {
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
    public SliderQuestion(int questionId, String text, SurveyItemProperties properties, Collection<String> categories, Collection<String> visibleToUserNames, Collection<String> editableToUsernames) {
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
    public SliderQuestion(AbstractQuestion question, int newId) {
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
    public SliderQuestion(AbstractQuestion question, String newText) {
        super(question, newText);
    }

    /**
     * Gets the text to display on the left side of the slider
     *
     * @return String The text to display on the left side of the slider
     */
    public String getLeftLabel() {

        return (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
    }

    /**
     * Sets the text to display on the left side of the slider
     *
     * @param value The text to display on the left side of the slider
     */
    public void setLeftLabel(String value) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, value);
    }

    /**
     * Gets the text to display on the right side of the slider
     *
     * @return String The text to display on the right side of the slider
     */
    public String getRightLabel() {

        return (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY);
    }

    /**
     * Sets the text to display on the right side of the slider
     *
     * @param value The text to display on the right side of the slider
     */
    public void setRightLabel(String value) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, value);
    }
    
    /**
     * Gets the range of the values returned by the slider, if null, the range
     * is 0 and 100
     *
     * @return SliderValueBounds The range of the values returned by the slider
     */
    public SliderRange getSliderRange() {
        return (SliderRange)getProperties().getPropertyValue(SurveyPropertyKeyEnum.RANGE);
    }

    @Override
    public double getHighestPossibleScore() {
        return SurveyScorerUtil.getHighestScoreSlider(getSliderRange());
    }
}
