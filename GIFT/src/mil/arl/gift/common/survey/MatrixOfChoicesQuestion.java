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
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a matrix of choices question
 *
 * @author jleonard
 */
public class MatrixOfChoicesQuestion extends AbstractQuestion implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public MatrixOfChoicesQuestion() {
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
    public MatrixOfChoicesQuestion(int questionId, String text, SurveyItemProperties properties, Collection<String> categories, Collection<String> visibleToUserNames, Collection<String> editableToUsernames) {
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
    public MatrixOfChoicesQuestion(AbstractQuestion question, int newId) {
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
    public MatrixOfChoicesQuestion(AbstractQuestion question, String newText) {
        super(question, newText);
    }

    /**
     * Gets the option list for the columns of the question
     *
     * @return GwtOptionList The option list for the columns of the question
     */
    public OptionList getColumnOptions() {

        return (OptionList) getProperties().getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
    }

    /**
     * Sets the option list for the columns of the question
     *
     * @param columnOptions The option list for the columns of the question
     */
    public void setColumnOptions(OptionList columnOptions) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, columnOptions);
    }

    /**
     * Gets the option list for the rows of the question
     *
     * @return GwtOptionList The option list for the rows of the question
     */
    public OptionList getRowOptions() {

        return (OptionList) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
    }

    /**
     * Sets the option list for the rows of the question
     *
     * @param rowOptions The option list for the rows of the question
     */
    public void setRowOptions(OptionList rowOptions) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, rowOptions);
    }
    
    /**
     * returns whether or not the question uses custom alignment for the answers. This property
     * is not set for older surveys, so default to true if not set so the old logic still works. 
     * 
     * @return Whether or not to use custom alignment, true if not set.
     */
    public Boolean getUseCustomAlignment(){
    	return getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, true);
    }

    /**
     * Gets the width, in pixels, of the columns
     *
     * @return String The width, in pixels, of the columns
     */
    public String getColumnWidth() {

        return (String)getProperties().getPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY);
    }

    /**
     * Sets the width, in pixels, of the columns
     *
     * @param width The width, in pixels, of the columns
     */
    public void setColumnWidth(String width) {
        getProperties().setPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY, width);
    }

    /**
     * Removes the column width property
     */
    public void removeColumnWidth() {

        getProperties().removeProperty(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY);
    }

    /**
     * Gets the left margin, in pixels, of the question
     *
     * @return String The left margin, in pixels, of the question
     */
    public String getLeftMargin() {

        return (String)getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
    }

    /**
     * Sets the left margin, in pixels, of the question
     *
     * @param margin The left margin, in pixels, of the question
     */
    public void setLeftMargin(String margin) {
        getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY, margin);
    }

    /**
     * Removes the left margin property
     */
    public void removeLeftMargin() {

        getProperties().removeProperty(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
    }
    
    /**
     * Returns if the question has been customized
     *
     * @return boolean If the question has been customized
     */
    public boolean isCustomized() {

        return getProperties().hasProperty(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY) || getProperties().hasProperty(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
    }
    
    /**
     * Gets the list of weights associated with each of the reply options
     *
     * @return List<Double> The weights associated with each of the reply
     * options
     */
    public MatrixOfChoicesReplyWeights getReplyWeights() {
        return (MatrixOfChoicesReplyWeights) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
    }

    @Override
    public double getHighestPossibleScore() {

        double highestPossiblePoints = 0.0;
        
        if (getReplyWeights() != null) {

            List<List<Double>> replyWeights = getReplyWeights().getReplyWeights();

            highestPossiblePoints = SurveyScorerUtil.getHighestScoreMatrixOfChoice(replyWeights);
        }
        
        return highestPossiblePoints;
    }
}
