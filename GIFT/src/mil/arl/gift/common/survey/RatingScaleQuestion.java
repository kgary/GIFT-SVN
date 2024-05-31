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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;
import mil.arl.gift.common.util.StringUtils;

/**
 * Represents a rating scale question
 *
 * @author jleonard
 */
public class RatingScaleQuestion extends AbstractQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public RatingScaleQuestion() {
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
    public RatingScaleQuestion(int questionId, String text, SurveyItemProperties properties, Collection<String> categories, Collection<String> visibleToUserNames, Collection<String> editableToUsernames) {
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
    public RatingScaleQuestion(AbstractQuestion question, int newId) {
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
    public RatingScaleQuestion(AbstractQuestion question, String newText) {
        super(question, newText);
    }

    /**
     * Gets the option list for the columns of the question
     *
     * @return GwtOptionList The option list for the columns of the question
     */
    public OptionList getReplyOptionSet() {

        return (OptionList) getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
    }

    /**
     * Sets the option list for the columns of the question
     *
     * @param replyOptionSet The option list for the columns of the question
     */
    public void setReplyOptionSet(OptionList replyOptionSet) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, replyOptionSet);
    }

    /**
     * Gets the URI of the image to display on the scale
     *
     * @return String The URI of the image to display on the scale
     */
    public String getScaleImageUri() {

        return (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY);
    }

    /**
     * Sets the URI of the image to display on the scale
     *
     * @param uri The URI of the image to display on the scale
     */
    public void setScaleImageUri(String uri) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY, uri);
    }

    /**
     * Removes the URI of the image to display on the scale
     */
    public void removeScaleImageUri() {
        getProperties().removeProperty(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY);
        getProperties().removeProperty(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY);
    }

    /**
     * Gets what the width of the displayed image should be
     *
     * @return Integer The width of the displayed image should be
     */
    public int getScaleImageWidth() {

        return getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY) != null
                ? getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY)
                : 0;
    }

    /**
     * Set what the width of the displayed image should be
     *
     * The aspect ratio is preserved when it is displayed
     *
     * @param width The width of the displayed image should be
     */
    public void setScaleImageWidth(int width) {

        if (width > 0) {

            getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY, width);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY);
        }
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

        return (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY);
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
     * Returns if labels should be displayed on the scale
     *
     * @return boolean If labels should be displayed on the scale
     */
    public boolean displayScaleLabels() {

        return getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY, false);
    }

    /**
     * Sets if labels should be displayed on the scale
     *
     * @param displayScaleLabels If labels should be displayed on the scale
     */
    public void setDisplayScaleLabels(boolean displayScaleLabels) {

        if (displayScaleLabels) {

            getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY, true);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY);
        }
    }

    /**
     * Gets the text to display on the left side of the rating scale
     *
     * @return String The text to display on the left side of the rating scale
     */
    public String getLeftExtremeLabel() {

        return (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
    }

    /**
     * Sets the text to display on the left side of the rating scale
     *
     * @param value The text to display on the left side of the rating scale
     */
    public void setLeftExtremeLabel(String value) {

        if (value != null && !value.isEmpty()) {

            getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, value);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
        }
    }

    /**
     * Gets the text to display on the right side of the rating scale
     *
     * @return String The text to display on the right side of the rating scale
     */
    public String getRightExtremeLabel() {

        return (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY);
    }

    /**
     * Sets the text to display on the right side of the rating scale
     *
     * @param value The text to display on the right side of the rating scale
     */
    public void setRightExtremeLabel(String value) {

        if (value != null && !value.isEmpty()) {

            getProperties().setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, value);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY);
        }
    }

    /**
     * Returns if the reply option labels should be hidden
     *
     * @return boolean If the reply option labels should be hidden
     */
    public boolean hideReplyOptionLabels() {

        return getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY, false);
    }
    
    /**
     * Gets if the multiple choice question has a correct answer(s)
     *
     * @return boolean If the question has a correct answer(s)
     */
	public boolean hasCorrectAnswers() {
        if(getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null && !SurveyItemProperties.decodeDoubleListString((String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS)).isEmpty() &&
        		Collections.max(SurveyItemProperties.decodeDoubleListString((String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS))) != 0.0){
        	return true;
        }
        return false;
    }

    /**
     * Sets if the reply option labels should be hidden
     *
     * @param hideReplyLabels If the reply option labels should be hidden
     */
    public void setHideReplyOptionLabels(boolean hideReplyLabels) {

        if (hideReplyLabels) {

            getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY, true);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY);

        }
    }

    /**
     * Returns if the question has been customized
     *
     * @return boolean If the question has been customized
     */
    public boolean isCustomized() {

        return getProperties().hasProperty(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY)  || getProperties().hasProperty(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
    }

    /**
     * Returns if the bar layout should be used when rendering
     *
     * @return boolean If the bar layout should be used when rendering
     */
    public boolean usesBarLayout() {

        boolean useBarLayout = false;
        
        if (getProperties().hasProperty(SurveyPropertyKeyEnum.USE_BAR_LAYOUT)) {
            useBarLayout = getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT);
        }    
        return useBarLayout;

    }

    /**
     * Sets if the bar layout should be used when rendering
     *
     * @param useBarLayout If the bar layout should be used when rendering
     */
    public void setUsesBarLayout(boolean useBarLayout) {

        if (useBarLayout) {

            getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT, true);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.USE_BAR_LAYOUT);
        }
    }

    /**
     * Gets the labels to display between the extreme end labels
     *
     * @return List<String> The list of labels to display between the extreme
     * end labels
     */
    public List<String> getMidScaleLabels() {

        String labelListString = (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY);

        if (labelListString != null) {

            List<String> labelList = SurveyItemProperties.decodeListString(labelListString);

            return labelList;

        } else {

            return null;
        }
    }

    /**
     * Sets the labels to display between the extreme end labels
     *
     * @param labelList The list of labels to display between the extreme end
     * labels
     */
    public void setMidScaleLabels(List<String> labelList) {

        if (labelList != null && !labelList.isEmpty()) {

            String labelListString = SurveyItemProperties.encodeListString(labelList);

            getProperties().setPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY, labelListString);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY);
        }
    }
    
    /**
     * Gets the list of weights associated with each of the reply options
     *
     * @return List<Double> The weights associated with each of the reply
     * options
     */
    public List<Double> getReplyWeights() {

        String replyWeightsListString = (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);

        if (replyWeightsListString != null && !replyWeightsListString.isEmpty()) {

            List<Double> replyWeights = SurveyItemProperties.decodeDoubleListString(replyWeightsListString);
            
            if (replyWeights.size() != (getReplyOptionSet() != null ? getReplyOptionSet().getListOptions().size() : 0)) {
                
                return null;
            }

            return replyWeights;

        } else {

            return null;
        }
    }
    
    @Override
    public double getHighestPossibleScore() {

        List<Double> weights = getReplyWeights();

        if (weights != null) {

            double highestPossiblePoints = SurveyScorerUtil.getHighestScoreRatingScale(weights);

            return highestPossiblePoints;

        } else {

            throw new DetailedException("Unable to calculate scores because the rating scale question has no answer points.", "The answer weights are null.", null);
        }
    }
    
    @Override
    public Set<String> getAllAssociatedImages() {
        
        Set<String> images = super.getAllAssociatedImages();
        
        String scaleImage = getScaleImageUri();
        if(StringUtils.isNotBlank(scaleImage)) {
            images.add(scaleImage);
        }
        
        return images;
    }
}
