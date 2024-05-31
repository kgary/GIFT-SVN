/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;

/**
 * Represents a slider survey question
 *
 * @author jleonard
 */
public class SliderSurveyQuestion extends AbstractSurveyQuestion<SliderQuestion> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SliderSurveyQuestion() {
        super();
    }

    /**
     * Constructor
     *
     * @param id The ID of the survey question
     * @param surveyPageId The ID of the survey page this survey question is in
     * @param question The question of this survey question
     * @param properties The properties of the survey question
     */
    public SliderSurveyQuestion(int id, int surveyPageId, SliderQuestion question, SurveyItemProperties properties) {
        super(id, surveyPageId, question, properties);
    }
    
    /**
     * Sets the range of the values returned by the slider
     * 
     * @param bounds The bounds of the values returned by the slider
     */
    public void setSliderRange(SliderRange bounds) {
        
        getProperties().setPropertyValue(SurveyPropertyKeyEnum.RANGE, bounds);
    }
    
    /**
     * Removes the custom range for the slider
     */
    public void removeSliderRange() {
        
        getProperties().removeProperty(SurveyPropertyKeyEnum.RANGE);
    }
    
    /**
     * Gets the range of the values returned by the slider, if null, the range
     * is 0 and 100
     *
     * @return SliderValueBounds The range of the values returned by the slider
     */
    public SliderRange getSliderRange() {
        return (SliderRange)getPropertyValue(SurveyPropertyKeyEnum.RANGE);
    }

    @Override
    public double getHighestPossibleScore() {

        return SurveyScorerUtil.getHighestScoreSlider(getSliderRange());
    }
}
