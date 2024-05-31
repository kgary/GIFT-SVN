/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.MultiSelectPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.IntegerBox;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * The MultiSelectPropertySetWidget class is responsible for displaying the
 * MultiSelectPropertySet values to the screen and allowing for the properties to be
 * set / unset as needed.
 * 
 * @author nblomberg
 *
 */
public class MultiSelectPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(MultiSelectPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, MultiSelectPropertySetWidget> {
	}

	
	@UiField
	CheckBox multiSelect;
	
	@UiField
	IntegerBox minSelections;
	
	@UiField
	IntegerBox maxSelections;
	
	@UiField
	protected Collapse multiSelectCollapse;
	
	/**
	 * Constructor (default)
	 * @param msPropSet 
	 */
    public MultiSelectPropertySetWidget(final MultiSelectPropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));

	   	    
	    multiSelect.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                MultiSelectPropertySet msProps = (MultiSelectPropertySet)propSet;
                msProps.setMultiSelectEnabled(multiSelect.getValue());
                
                if(propertySet.getReplyOptionSet() != null && propertySet.getReplyOptionSet().getListOptions() != null) {
                	// Only enable the text boxes if there are responses available
                	
                	setTextBoxControlsEnabled(!propertySet.getReplyOptionSet().getListOptions().isEmpty());
                	
                } else {
                	setTextBoxControlsEnabled(multiSelect.getValue());
                }
                                
                if(multiSelect.getValue()) {
                	multiSelectCollapse.show();
                	
                } else {
                	multiSelectCollapse.hide();
                	msProps.getProperties().removeProperty(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY);
                	msProps.getProperties().removeProperty(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY);
                }
                
                
                notifyPropertySetChanged();
            }
	        
	    });
	    
	    // Handler used to save the property for Min Selections Allowed.
	    minSelections.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                
                logger.info("minSelections onBlur()");
                
                // Save back the property if it contains a valid value.  Invalid values
                // should not be saved to the property set.
                if (minSelections.getValue() != null) {
                    MultiSelectPropertySet msProps = (MultiSelectPropertySet)propSet;
                    msProps.setMinSelectionsAllowed(minSelections.getValue());
                    notifyPropertySetChanged();
                }
            }
	        
	    });
	    
	    // Handler used to save the property for the Max Selections Allowed
	    maxSelections.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                logger.info("maxSelections onBlur()");
                
                // Save back the property if it contains a valid value.  Invalid values 
                // should not be saved to the property set.
                if (maxSelections.getValue() != null) {
                    MultiSelectPropertySet msProps = (MultiSelectPropertySet)propSet;
                    msProps.setMaxSelectionsAllowed(maxSelections.getValue());
                    notifyPropertySetChanged();
                }
                
            }
            
        });
	    
	    // Set validation to occur when the blur event happens.
	    // the order is (validate first, then blur event happens).
	    maxSelections.setValidateOnBlur(true);
	    minSelections.setValidateOnBlur(true);
	    
	    // Create a validator for the min selections allowed.
	    Validator<Integer> minValidator = new Validator<Integer>() {

            @Override
            public int getPriority() {
                
                return Priority.HIGHEST;
            }

            @Override
            public List<EditorError> validate(Editor<Integer> editor,
                    Integer value) {
                logger.info("min validate called");
                
                ArrayList<EditorError> result = new ArrayList<EditorError>();
                
                if (value == null) {
                    result.add(new BasicEditorError(minSelections, value, "Field must be a numeric value."));
                    
                }else if(value < 0){
                	result.add(new BasicEditorError(minSelections, value, "Value can not be negative"));
                	
                }else if(maxSelections.getValue() != null && value > maxSelections.getValue()){
                	result.add(new BasicEditorError(minSelections, value, "Minimum Value cannot be greater than Maximum Value"));
                	
                }else if(maxSelections.getValue() == null && value > 0){
                	result.add(new BasicEditorError(minSelections, value, "Minimum Value cannot be greater than Maximum Value"));
                
                } else if(propertySet.getReplyOptionSet() != null 
                    		&& propertySet.getReplyOptionSet().getListOptions() != null
                    		&& value > propertySet.getReplyOptionSet().getListOptions().size()){
                	
                    result.add(new BasicEditorError(minSelections, value, "Minimum Value cannot be greater than the number of choices"));
                }
                
                if (result.size() == 0) {
                    minSelections.setValue(value);
                } else {
                	revertMinSelectionsBox();
                }
                
                return result;
            }
	        
	    };
	    
	    // Create a validator for the max selections allowed.
	    Validator<Integer> maxValidator = new Validator<Integer>() {

            @Override
            public int getPriority() {
                
                return Priority.HIGHEST;
            }

            @Override
            public List<EditorError> validate(Editor<Integer> editor,
                    Integer value) {
                logger.info("max validate called");
                
                ArrayList<EditorError> result = new ArrayList<EditorError>();
                
                if (value == null) {
                    result.add(new BasicEditorError(maxSelections, value, "Field must be a numeric value."));
                    
                } else if(value <= 0){
                	result.add(new BasicEditorError(maxSelections, value, "Value must be greater than 0"));
                	
                } else if(minSelections.getValue() != null && value < minSelections.getValue()){
                	result.add(new BasicEditorError(maxSelections, value, "Maximum Value must be greater than Minimum Value"));
                	
                } else if(propertySet.getReplyOptionSet() != null 
                		&& propertySet.getReplyOptionSet().getListOptions() != null
                		&& value > propertySet.getReplyOptionSet().getListOptions().size()){
            	
                	result.add(new BasicEditorError(maxSelections, value, "Maximum Value cannot be greater than the number of choices"));
                	
                } else {
                	minSelections.validate();
                }
                
                if (result.size() == 0) {
                    maxSelections.setValue(value);
                } else{
                	revertMaxSelectionsBox();
                }
                
                return result;
            }
            
        };
	    
	    maxSelections.addValidator(maxValidator);
	    minSelections.addValidator(minValidator);
	    	    
	    MultiSelectPropertySet msProps = (MultiSelectPropertySet)propSet;
	    multiSelect.setValue(msProps.getMultiSelectEnabled());
	    setTextBoxControlsEnabled(msProps.getMultiSelectEnabled());
	    
	    if(msProps.getMultiSelectEnabled()) {
	    	multiSelectCollapse.show();
	    	minSelections.setValue(msProps.getMinSelectionsAllowed());
		    maxSelections.setValue(msProps.getMaxSelectionsAllowed());
	    }
	    
	    if(propertySet.getReplyOptionSet() != null && propertySet.getReplyOptionSet().getListOptions() != null) {
	    	onResponseChanged(propertySet.getReplyOptionSet().getListOptions().size());
	    }
	    
	    notifyPropertySetChanged();
    }
	
    /**
     * Validates the minimum and maximum selections allowed against the total number of 
     * reply options available.
     * 
     * @param totalResponses The total number of replies for the current question.
     */
    public void onResponseChanged(int totalResponses) {
    	
    	MultiSelectPropertySet msProps = (MultiSelectPropertySet)propSet;
    	Integer min = msProps.getMinSelectionsAllowed();
    	Integer max = msProps.getMaxSelectionsAllowed();
    	
    	if (max == null || max > totalResponses) {
    		max = totalResponses;
    		msProps.setMaxSelectionsAllowed(totalResponses);
    	}
    	
    	if(min == null){
    		min = 0;
    		msProps.setMinSelectionsAllowed(min);
    		
    	} else if (min > max) {
    		min = max;
    		msProps.setMinSelectionsAllowed(min);
    	}

    	minSelections.setValue(msProps.getMinSelectionsAllowed());
	    maxSelections.setValue(msProps.getMaxSelectionsAllowed());
	    
	    setTextBoxControlsEnabled(totalResponses != 0);
	    
	    notifyPropertySetChanged();
    }
    
	/**
	 *  Sets if the text box controls (min & max selections) are enabled or disabled.
	 *  
	 * @param enabled - True to enable the text box items, false to disable them.
	 */
	private void setTextBoxControlsEnabled(boolean enabled) {
	    minSelections.setEnabled(enabled);
        maxSelections.setEnabled(enabled);
	}
	
	private void revertMinSelectionsBox() {
		MultiSelectPropertySet multiSelectProps = (MultiSelectPropertySet) propSet;
		minSelections.setValue(multiSelectProps.getMinSelectionsAllowed());
	}
	
	private void revertMaxSelectionsBox() {
		MultiSelectPropertySet multiSelectProps = (MultiSelectPropertySet) propSet;
		maxSelections.setValue(multiSelectProps.getMaxSelectionsAllowed());
	}

    

}
