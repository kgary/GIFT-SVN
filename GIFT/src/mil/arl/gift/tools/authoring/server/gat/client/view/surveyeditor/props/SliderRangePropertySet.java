/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.survey.SliderRange;
import mil.arl.gift.common.survey.SliderRange.ScaleType;
import mil.arl.gift.common.survey.SurveyItemProperties;

/**
 * The SliderRangePropertySet contains the properties that allow for a custom
 * range of the slider.
 * 
 * @author nblomberg
 *
 */
public class SliderRangePropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SliderRangePropertySet.class.getName());

    /** The default max value of the slider. */
    static final double DEFAULT_MAX = 100.0;
    
    /** The default min value of the slider. */
    static final double DEFAULT_MIN = 0.0;
    
    /** The default step size of the slider. */
    static final double DEFAULT_STEP_SIZE = 1.0;
    
    /** The default scale type of the slider. */
    static final ScaleType DEFAULT_SCALE_TYPE = ScaleType.LINEAR;
	/**
	 * Constructor (default)
	 */
	public SliderRangePropertySet() {
	    super();
	
	    SliderRange defaultRange = new SliderRange(DEFAULT_MIN, DEFAULT_MAX);
	    defaultRange.setStepSize(DEFAULT_STEP_SIZE);
	    defaultRange.setScaleType(DEFAULT_SCALE_TYPE);
	    
	    properties.setPropertyValue(SurveyPropertyKeyEnum.RANGE, defaultRange);
	}
	
	
	/**
	 * Gets the min value for the slider.
	 * 
	 * @return double - the min value of the slider.
	 */
	public double getSliderMinValue() {
	    
	    double sliderMin = 0;
	    SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
	    sliderMin = range.getMinValue();
	    return sliderMin;
	}
	
	/**
	 * Gets the max value for the slider.
	 * 
	 * @return double - the max value of the slider.
	 */
	public double getSliderMaxValue() {
        
        double sliderMax = 0;
        SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
        sliderMax = range.getMaxValue();
        return sliderMax;
    }
	
	/**
	 * Gets the step value for the slider. Make sure the value is greater than 0 so slider would be able to move
	 * 
	 * @return double - the step value of the slider.
	 */
	public double getSliderStepValue() {
        
	    double sliderStep = 0;
        SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
        sliderStep = range.getStepSize();
        if(sliderStep <= 0)
        {
            sliderStep = 1.0;
        }
        return sliderStep;
    }
	
	/**
     * Gets the scale type for the slider.
     * 
     * @return ScaleType - the scale type value of the slider.
     */
    public ScaleType getSliderScaleType() {
        
        ScaleType type = ScaleType.LINEAR;
        SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
        type = range.getScaleType();
        return type;
    }
	
	
	/**
	 * Sets the slider min value.
	 * 
	 * @param newMin - The min value of the slider.
	 */
	public void setSliderMinValue(double newMin) {
	    SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
	    
	    range.setMinValue(newMin);
	    
	    properties.setPropertyValue(SurveyPropertyKeyEnum.RANGE, range);
    }
	
	/**
	 * Sets the slider max value. 
	 * 
	 * @param newMax - The max value of the slider.
	 */
	public void setSliderMaxValue(double newMax) {
        SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
        
        range.setMaxValue(newMax);
        
        properties.setPropertyValue(SurveyPropertyKeyEnum.RANGE, range);
    }
	
	/**
	 * Gets the step value for the slider. 
	 * 
	 * @return double - the step value of the slider.
	 */
	public void setSliderStepValue(double newStep) {
	    SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);

        range.setStepSize(newStep);
        
        properties.setPropertyValue(SurveyPropertyKeyEnum.RANGE, range);
    }
	
	/**
     * Gets the step value for the slider.
     * 
     * @return double - the step value of the slider.
     */
    public void setSliderScaleType(ScaleType newType) {
        SliderRange range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
        
        range.setScaleType(newType);
        
        properties.setPropertyValue(SurveyPropertyKeyEnum.RANGE, range);
    }


    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
        if (props.getPropertyValue(SurveyPropertyKeyEnum.RANGE) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.RANGE, props.getPropertyValue(SurveyPropertyKeyEnum.RANGE));
        }        
    }


    /**
     * Accessor to get the SliderRange value from the property set.
     * 
     * @return SliderRange the range value for the property set.
     */
    public SliderRange getSliderRange() {
        SliderRange range = null;
        if (properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE) != null) {
            range = (SliderRange) properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE);
        }
        return range;
    }
    
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[SliderRangePropertySet: ");
        sb.append("sliderMin = ").append(getSliderMinValue());
        sb.append(", sliderMax = ").append(getSliderMaxValue());
        sb.append(", sliderStep = ").append(getSliderStepValue());
        
        if(properties.getPropertyValue(SurveyPropertyKeyEnum.RANGE) != null) {
            sb.append(", range = ").append(getSliderRange());
        }
        
        return sb.toString();
    } 

}
