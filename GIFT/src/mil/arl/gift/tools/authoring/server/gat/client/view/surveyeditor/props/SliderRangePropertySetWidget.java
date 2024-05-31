/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.survey.SliderRange;

import org.gwtbootstrap3.client.ui.DoubleBox;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.extras.slider.client.ui.base.constants.ScaleType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * The SliderRangePropertySetWidget is responsible for displaying the properties to control
 * the range (min & max) of a slider widget.  This widget is displayed in the properties panel of 
 * the survey editor.
 * 
 * @author nblomberg
 *
 */
public class SliderRangePropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(SliderRangePropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, SliderRangePropertySetWidget> {
	}
	
	@UiField
	DoubleBox minRange;
	
	@UiField
	DoubleBox maxRange;
	
	/* sets by how much the slider should increase or decrease at a time */
	@UiField
	DoubleBox stepSize;

	@UiField
    ValueListBox<ScaleType> scaleType;
	
	@UiHandler("minRange")
	void onBlurMinRange(BlurEvent event) {
	    
	    updatePropertySetMinValue();
	    notifyPropertySetChanged();
	}
	
	@UiHandler("maxRange")
    void onBlurMaxRange(BlurEvent event) {
	    
	    updatePropertySetMaxValue();
	    notifyPropertySetChanged();
    }
	
	/* make sure property is set and update it if user changes the step size */
	@UiHandler("stepSize")
    void onBlurStepSize(BlurEvent event) {
	    
	    updatePropertySetStepSize();
	    notifyPropertySetChanged();
    }

	/**
	 * Constructor (Default)
	 * 
	 * @param propertySet - The property set that holds the values for the widget.
	 * @param listener - The listener that is notified of any changes to the properties.
	 */
    public SliderRangePropertySetWidget(SliderRangePropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    // Set validation to occur when the blur event happens.
        // the order is (validate first, then blur event happens).
	    minRange.setValidateOnBlur(true);
	    maxRange.setValidateOnBlur(true);
	    stepSize.setValidateOnBlur(true);
	    
	    /* Configure the scale type dropdown and add options. */
	    scaleType.setValue(ScaleType.LINEAR, true);
	    scaleType.setAcceptableValues(Arrays.asList(ScaleType.LINEAR, ScaleType.LOGARITHMIC));
	    
        // Create a validator for the min range
        Validator<Double> minValidator = new Validator<Double>() {

            @Override
            public int getPriority() {
                
                return Priority.HIGHEST;
            }

            @Override
            public List<EditorError> validate(Editor<Double> editor,
                    Double value) {
                logger.info("min validate called");
                
                ArrayList<EditorError> result = new ArrayList<EditorError>();
                
                if (value == null) {
                    result.add(new BasicEditorError(minRange, value, "Field must be a numeric value."));
                } else if (scaleType.getValue() == ScaleType.LOGARITHMIC && value < 0) {
                    result.add(new BasicEditorError(minRange, value, "Field should not be negative when dealing with logarithmic scales."));
                } else if (value >= maxRange.getValue()) {
                    result.add(new BasicEditorError(minRange, value, "Field must be less than the Max Slider Value."));
                }
                
                if (result.size() == 0) {
                    minRange.setValue(value);
                } else {
                    // Revert to the original setting.
                    revertMinRangeEditBox();
                    
                    /* This check is necessary in case a negative value was entered for min/max bounds and 
                     * the scale type is LOGARITHMIC. */
                    if (minRange.getValue() < 0) {
                        minRange.setValue(Double.valueOf(0));
                    }
                }
                
                return result;
            }
            
        };
        
        // Create a validator for the max range
        Validator<Double> maxValidator = new Validator<Double>() {

            @Override
            public int getPriority() {
                
                return Priority.HIGHEST;
            }

            @Override
            public List<EditorError> validate(Editor<Double> editor,
                    Double value) {
                logger.info("max validate called");
                
                ArrayList<EditorError> result = new ArrayList<EditorError>();
                
                if (value == null) {
                    result.add(new BasicEditorError(maxRange, value, "Field must be a numeric value."));
                } else if (value <= minRange.getValue()) {
                    result.add(new BasicEditorError(maxRange, value, "Field must be greater than the Min Slider Value."));
                }
                
                if (result.size() == 0) {
                    maxRange.setValue(value);
                } else {
                    // Revert to the original setting.
                    revertMaxRangeEditBox();
                    
                    /* This check is necessary in case a negative value was entered for min/max bounds and 
                     * the scale type is LOGARITHMIC. */
                    if (maxRange.getValue() < 0) {
                        maxRange.setValue(Double.valueOf(0));
                    }
                }
                
                return result;
            }
            
        };
        
        // Create a validator for the step size
        Validator<Double> stepValidator = new Validator<Double>() {

            @Override
            public int getPriority() {
                
                return Priority.HIGHEST;
            }

            @Override
            public List<EditorError> validate(Editor<Double> editor,
                    Double value) {
                logger.info("max validate called");
                
                ArrayList<EditorError> result = new ArrayList<EditorError>();
                
                if (value == null) {
                    result.add(new BasicEditorError(stepSize, value, "Field must be a numeric value."));
                } else if (value <= 0) {
                    result.add(new BasicEditorError(stepSize, value, "Field must be greater than 0."));
                }
                
                if (result.size() == 0) {
                    stepSize.setValue(value);
                } else {
                    // Revert to the original setting.
                    revertStepSizeEditBox();
                }
                
                return result;
            }
            
        };
        
        maxRange.addValidator(maxValidator);
        minRange.addValidator(minValidator);
        stepSize.addValidator(stepValidator);
        
        scaleType.addValueChangeHandler(new ValueChangeHandler<ScaleType>() {

            @Override
            public void onValueChange(ValueChangeEvent<ScaleType> event) {
                updatePropertySetScaleType();
                notifyPropertySetChanged(); 
                minRange.validate(true);
                maxRange.validate(true);
            }
        });
        
        
        if(propertySet != null){
        	
        	Serializable sliderMinValue = propertySet.getSliderMinValue();
        	
	    	if(sliderMinValue != null && sliderMinValue instanceof Double){
	    		minRange.setValue((Double) sliderMinValue);
	    	} else {
	    		minRange.setValue(SliderRangePropertySet.DEFAULT_MIN);
	    	}
	    	
	    	Serializable sliderMaxValue = propertySet.getSliderMaxValue();
	    	
	    	if(sliderMaxValue != null && sliderMaxValue instanceof Double){
	    		maxRange.setValue((Double) sliderMaxValue);
	    	} else {
	    		maxRange.setValue(SliderRangePropertySet.DEFAULT_MAX);
	    	}
	    	/* either set the step value or make it the default value*/
	    	Serializable sliderStepValue = propertySet.getSliderStepValue();
	    	if(sliderStepValue != null && sliderStepValue instanceof Double) {
	    		stepSize.setValue((Double) sliderStepValue);
	    	} else {
	    		stepSize.setValue(SliderRangePropertySet.DEFAULT_STEP_SIZE);
	    	}
	    	
	    	Serializable sliderScaleType = propertySet.getSliderScaleType();
            if(sliderScaleType != null && sliderScaleType instanceof SliderRange.ScaleType) {
                ScaleType type = ((SliderRange.ScaleType) sliderScaleType).equals(SliderRange.ScaleType.LINEAR)
                        ? ScaleType.LINEAR
                        : ScaleType.LOGARITHMIC;
                scaleType.setValue(type);
            } else {
                scaleType.setValue(ScaleType.LINEAR);
            }
        	
	    	propListener.onPropertySetChange(propSet);
        } else {
        	logger.info("property set is null");
        }
	}
    
    /**
     * Reverts the edit box for min range to the previous value.
     */
    void revertMinRangeEditBox() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        minRange.setValue(rangeProps.getSliderMinValue());
    }
    
    /**
     * Reverts the edit box for max range to the previous value.
     */
    void revertMaxRangeEditBox() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        maxRange.setValue(rangeProps.getSliderMaxValue());
    }
    
    /**
     * Reverts the edit box for step size to the previous value.
     */
    void revertStepSizeEditBox() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        stepSize.setValue(rangeProps.getSliderStepValue());
    }
    
    /**
     * Reverts the edit box for scale type to the previous value.
     */
    void revertScaleTypeEditBox() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        scaleType.setValue(rangeProps.getSliderScaleType().equals(SliderRange.ScaleType.LINEAR) ? ScaleType.LINEAR : ScaleType.LOGARITHMIC);
    }
    
    /**
     * Saves the widget edit box for min range to the property set.
     */
    void updatePropertySetMinValue() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        rangeProps.setSliderMinValue(minRange.getValue());
    }
    
    /**
     * Saves the widget edit box for max range to the property set.
     */
    void updatePropertySetMaxValue() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        rangeProps.setSliderMaxValue(maxRange.getValue());
    }
    
    /**
     * Saves the widget edit box for step size to the property set.
     */
    void updatePropertySetStepSize() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        rangeProps.setSliderStepValue(stepSize.getValue());
    }
    
    /**
     * Saves the widget edit box for scale type to the property set.
     */
    void updatePropertySetScaleType() {
        SliderRangePropertySet rangeProps = (SliderRangePropertySet) propSet;
        rangeProps.setSliderScaleType(scaleType.getValue().equals(ScaleType.LINEAR) ? SliderRange.ScaleType.LINEAR : SliderRange.ScaleType.LOGARITHMIC);
    }

}
