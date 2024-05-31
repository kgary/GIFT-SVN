/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.io.Serializable;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * The CustomAlignmentPropertySetWidget is responsible for displaying the controls that
 * allow the author to customize the layout of the answer sets for certain questions.
 * 
 * @author nblomberg
 *
 */
public class CustomAlignmentPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(CustomAlignmentPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, CustomAlignmentPropertySetWidget> {
	}
	
	@UiField
	protected CheckBox customizeAlignmentBox;
	
	@UiField
	protected Collapse customizeAlignmentCollapse;
	
	@UiField
	protected TextBox columnWidthBox;
	
	@UiField
	protected TextBox leftMarginBox;
	
	private Integer lastWidthValue = null;
	private Integer lastMarginValue = null;

	/**
	 * Constructor (default)
	 * @param propertySet - The property set for the widget.
	 * 
	 * @param listener - The listener that will handle changes to the properties.
	 */
    public CustomAlignmentPropertySetWidget(CustomAlignmentPropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    	    
	    customizeAlignmentBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				CustomAlignmentPropertySet props = (CustomAlignmentPropertySet) propSet;
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, event.getValue());
				
				if(customizeAlignmentBox.getValue()) {
					customizeAlignmentCollapse.show();
					
				} else {
					customizeAlignmentCollapse.hide();
				}
				
				propListener.onPropertySetChange(propSet);
			}
	    });
	    
	    columnWidthBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				CustomAlignmentPropertySet props = (CustomAlignmentPropertySet) propSet;
				
				if(event.getValue() != null && !event.getValue().isEmpty()){
					
					try{
						
						Integer width = Integer.valueOf(event.getValue());
					
						props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY, width);
						lastWidthValue = width;
						
					} catch(@SuppressWarnings("unused") Exception e){
						
						WarningDialog.error("Invalid value", "Please enter a number for the column width or leave the field blank.");
						
						columnWidthBox.setValue(lastWidthValue != null ? lastWidthValue.toString() : null); //reset the width box
						
						return;
					}
					
				} else {
					
					props.getProperties().removeProperty(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY);
					lastWidthValue = null;
				}			
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    leftMarginBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				CustomAlignmentPropertySet props = (CustomAlignmentPropertySet) propSet;
				
				if(event.getValue() != null && !event.getValue().isEmpty()){
					
					try{
						
						Integer margin = Integer.valueOf(event.getValue());
					
						props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY, margin);
						lastMarginValue = margin;
						
					} catch(@SuppressWarnings("unused") Exception e){
						
						WarningDialog.error("Invalid value", "Please enter a number for the left margin or leave the field blank.");
						
						leftMarginBox.setValue(lastMarginValue != null ? lastMarginValue.toString() : null); //reset the margin box
						
						return;
					}
					
				} else {
					
					props.getProperties().removeProperty(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
					lastMarginValue = null;
				}			
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    
	    if(propertySet != null){
	    	Boolean useCustomAlignment = false;
	    		    	
	    	Serializable width = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY);
	    	
	    	if(width != null && width instanceof Integer){
	    		columnWidthBox.setValue(((Integer) width).toString());
	    		
	    	} else {
	    		columnWidthBox.setValue(null);
	    	}
	    	
	    	Serializable margin = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
	    	
	    	if(margin != null && margin instanceof Integer){
	    		leftMarginBox.setValue(((Integer) margin).toString());
	    		
	    	} else {
	    		leftMarginBox.setValue(null);
	    	}
	    	
	    	if(propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT) != null){
	    		useCustomAlignment = propertySet.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, false);
	    	} else if(width != null || margin != null) {
	    		propertySet.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, true);
	    		useCustomAlignment = true;
	    	}
	    	
	    	if(useCustomAlignment){
	    		customizeAlignmentBox.setValue(true);
	    		customizeAlignmentCollapse.show();
	    	} else {
	    		customizeAlignmentBox.setValue(false);
	    		customizeAlignmentCollapse.hide();
	    	}

	    	propListener.onPropertySetChange(propSet);
	    }
	}

}
