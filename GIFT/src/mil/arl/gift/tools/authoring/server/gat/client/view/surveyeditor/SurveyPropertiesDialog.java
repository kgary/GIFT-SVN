/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * Dialog used to select properties for the current survey, such as
 * hide survey name, hide number of questions, and display in full screen
 * 
 * @author wpearigen
 * 
 */
public class SurveyPropertiesDialog extends ModalDialogBox{
	
    private static Logger logger = Logger.getLogger(SurveyPropertiesDialog.class.getName());
    
    private static SurveyPropertiesDialogUiBinder uiBinder = GWT
            .create(SurveyPropertiesDialogUiBinder.class);
    
    interface SurveyPropertiesDialogUiBinder extends
    UiBinder<Widget, SurveyPropertiesDialog> {
    }
    
	@UiField
	protected CheckBox hideSurveyNameCheckBox;
	
	@UiField
	protected CheckBox hideNumQuestionsCheckBox;
	
	
	
	@UiHandler("hideSurveyNameCheckBox")
	void onHideSurveyNameClick(ClickEvent event) {
	    logger.info("onHideSurveyNameClick()");
	    
	    surveyProperties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_NAME, hideSurveyNameCheckBox.getValue());
	}
	
	@UiHandler("hideNumQuestionsCheckBox")
    void onHideNumQuestionsClick(ClickEvent event) {
        logger.info("onHideNumQuestionsClick()");
        
        surveyProperties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, hideNumQuestionsCheckBox.getValue());
    }

	// The survey properties that this dialog is responsible for.
	SurveyProperties surveyProperties = null;
	
	public SurveyPropertiesDialog(){
		setWidget(uiBinder.createAndBindUi(this));
		setAnimationEnabled(true);
		setGlassEnabled(true);
	}
	
	/**
	 * Gets whether or not the hide survey name check box is currently checked
	 * 
	 * @return true if checked, false otherwise
	 */
	public Boolean getHideSurveyNameCheckBoxValue() {
		return hideSurveyNameCheckBox.getValue();
	}
	
	/**
	 * Gets whether or not the hide number of questions check box is currently checked
	 * 
	 * @return true if checked, false otherwise
	 */
	public Boolean getHideNumQuestionsCheckBoxValue() {
		return hideNumQuestionsCheckBox.getValue();
	}
	
	/**
	 * Sets whether or not the checkbox is checked
	 * 
	 * @param value the value the checkbox is set to
	 */
	public void setHideSurveyNameCheckBoxValue(Boolean value){
		this.hideSurveyNameCheckBox.setValue(value);
	}
	
	/**
	 * Sets whether or not the checkbox is checked
	 * 
	 * @param value the value the checkbox is set to
	 */
	public void setHideNumQuestionsNameCheckBoxValue(Boolean value){
		this.hideNumQuestionsCheckBox.setValue(value);
	}


    /**
     * Sets the UI widgets based on the values of the underlying survey properties.
     * 
     * @param props - The survey properties.
     */
    public void setSurveyProperties(SurveyProperties props) {
        
        surveyProperties = props;
        
        defaultPropertyValues();

        refreshUiFromPropertyValues(); 
    }
    
    /**
     * Sets whether the Survey Properties are enabled for editing
     * 
     * @param enabled - Determines if survey properties are editable
     */
    public void setEnabled(Boolean enabled) {
        this.hideSurveyNameCheckBox.setEnabled(enabled);
        this.hideNumQuestionsCheckBox.setEnabled(enabled);
    }
    
    /**
     * Refreshes the UI checkboxes with the current values from the survey properties.
     */
    private void refreshUiFromPropertyValues() {
        setHideNumQuestionsNameCheckBoxValue(surveyProperties.getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, false));
        setHideSurveyNameCheckBoxValue(surveyProperties.getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_NAME, false));

    }
    
    
    /**
     * Defaults the property values used by the survey editor for properties that are not yet set. 
     * If the property already has a value, then it is not changed.
     */
    private void defaultPropertyValues() {
        
        defaultBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_NAME, false);
        defaultBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, false);
    }
    
    /**
     * Defaults the property value if the property is not already set.
     * 
     * @param propKey - the property to check for.
     * @param value - The value to set the property to if it is not already set.
     */
    private void defaultBooleanPropertyValue(SurveyPropertyKeyEnum propKey, boolean value) {
        if (propKey != null && !surveyProperties.hasProperty(propKey)) {
            surveyProperties.setBooleanPropertyValue(propKey, value);
        }
    }
}
