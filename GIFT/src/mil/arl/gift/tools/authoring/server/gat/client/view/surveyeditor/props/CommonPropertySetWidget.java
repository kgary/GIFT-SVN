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
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The CommonPropertySetWidget is responsible for displaying the controls that
 * allow the author to set common properties such as if the question is required,
 * help string for the question, and a tag.
 * 
 * @author nblomberg
 *
 */
public class CommonPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(CommonPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, CommonPropertySetWidget> {
	}
	
	@UiField
	protected CheckBox isRequiredBox;
	
	@UiField
	protected TextBox helpStringBox;
	
	@UiField
	protected TextBox tagBox;
	
	@UiField
	protected Label surveyQuestionId;
	
	/** panel that contains the partial credit checkbox and help button */
	@UiField
	protected HorizontalPanel partialCreditPanel;
	
	/** used to manage the partial credit boolean property */
	@UiField
	protected CheckBox isPartialCreditBox;

	/**
	 * Constructor (default)
	 * @param propertySet - The property set for the widget.
	 * 
	 * @param listener - The listener that will handle changes to the properties.
	 */
    public CommonPropertySetWidget(CommonPropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    isRequiredBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
					
				CommonPropertySet props = (CommonPropertySet) propSet;
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED, event.getValue());
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    isPartialCreditBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                    
                CommonPropertySet props = (CommonPropertySet) propSet;
                props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.ALLOW_PARTIAL_CREDIT, event.getValue());
                
                propListener.onPropertySetChange(propSet);
            }
        });

	    helpStringBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
					
				CommonPropertySet props = (CommonPropertySet) propSet;
				props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.HELP_STRING, helpStringBox.getValue());
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    // necessary to make sure all characters are saved even if something like loading another question causes
	    // this widget to reload the help value for the next question
	    helpStringBox.addKeyUpHandler(new KeyUpHandler() {
            
            @Override
            public void onKeyUp(KeyUpEvent event) {
                
                CommonPropertySet props = (CommonPropertySet) propSet;
                props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.HELP_STRING, helpStringBox.getValue());
                
                propListener.onPropertySetChange(propSet);
            }
        });
	    
	    tagBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
					
				CommonPropertySet props = (CommonPropertySet) propSet;
				props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.TAG, tagBox.getValue());
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    // necessary to make sure all characters are saved even if something like loading another question causes
        // this widget to reload the tag value for the next question
	    tagBox.addKeyUpHandler(new KeyUpHandler() {
            
            @Override
            public void onKeyUp(KeyUpEvent event) {
                
                CommonPropertySet props = (CommonPropertySet) propSet;
                props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.TAG, tagBox.getValue());
                
                propListener.onPropertySetChange(propSet);
            }
        });
	    
	    if(propertySet != null){
	    	
	    	Serializable isRequired = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED);
	    	
	    	if(isRequired != null && isRequired instanceof Boolean){
	    		isRequiredBox.setValue((Boolean) isRequired);
	    		
	    	} else {
	    		isRequiredBox.setValue(null);
	    	}
	    	
	    	Serializable isPartialCredit = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.ALLOW_PARTIAL_CREDIT);
	    	
            if(isPartialCredit != null && isRequired instanceof Boolean){
                isPartialCreditBox.setValue((Boolean) isRequired);
                
            } else {
                // default to checked, partial credit allowed - due to older questions not having this property (#5308)
                isPartialCreditBox.setValue(true);
            }
	    	
	    	Serializable helpText = propertySet.getPropertyValue(SurveyPropertyKeyEnum.HELP_STRING);
	    	
	    	if(helpText != null && helpText instanceof String){
	    		helpStringBox.setValue((String) helpText);
	    		
	    	} else {
	    		helpStringBox.setValue(null);
	    	}
	    	
	    	Serializable tag = propertySet.getPropertyValue(SurveyPropertyKeyEnum.TAG);
	    	
	    	if(tag != null && tag instanceof String){
	    		tagBox.setValue((String) tag);
	    		
	    	} else {
	    		tagBox.setValue(null);
	    	}
	    	
	    	propListener.onPropertySetChange(propSet);
	    	
	    	Boolean canHaveScoring = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.CAN_QUESTION_HAVE_SCORING);
	    	boolean hidePartialCreditPanel = canHaveScoring != null && 
	    	        !canHaveScoring;
	    	
	    	if(propertySet.getSurveyQuestion() != null){
	    	    surveyQuestionId.setText(String.valueOf(propertySet.getSurveyQuestion().getId()));
	    	}else{
	    	    surveyQuestionId.setText("Unknown");
	    	}
	    	
	    	// hide the panel if the survey question doesn't support scoring
	    	if(hidePartialCreditPanel) {
	    	    partialCreditPanel.setVisible(false);
	    	}
	    }
	}

}
