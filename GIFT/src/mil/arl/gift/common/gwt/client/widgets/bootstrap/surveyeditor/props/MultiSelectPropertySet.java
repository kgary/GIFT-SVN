/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;

import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.SurveyItemProperties;


/**
 * The MultiSelectPropertySet class contains the set of properties that correspond to
 * if a radio group should be displayed as a single select radio group or a checkbox
 * (multi select).  If multi select is set, then the user can specify the max and/or min
 * value that can be selected.
 * 
 * @author nblomberg
 *
 */
public class MultiSelectPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(MultiSelectPropertySet.class.getName());


	/**
	 * Constructor (default)
	 */
	public MultiSelectPropertySet() {
	    super();
	    
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED,  false);
	    properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY, 0);
	    properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY, 0);
	}
	
	/**
	 * Gets the value of the max selections allowed property.
	 * 
	 * @return Integer - Gets the value of the max selections allowed property.
	 */
	public Integer getMaxSelectionsAllowed() {
	    return properties.getIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY);
	    
	}
	
	/**
     * Sets the value of the max selections allowed property.
     * 
     * @param maxSelect - Sets the value of the max selections allowed property.
     */
    public void setMaxSelectionsAllowed(Integer maxSelect) {
        properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY, maxSelect);
    }
	
	/**
     * Gets the value of the min selections allowed property.
     * 
     * @return Integer - Gets the value of the min selections allowed property.
     */
	public Integer getMinSelectionsAllowed() {
	    return properties.getIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY);
	}
	
	/**
     * Sets the value of the min selections allowed property.
     * 
     * @param minSelect - Sets the value of the min selections allowed property.
     */
    public void setMinSelectionsAllowed(Integer minSelect) {
        properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY, minSelect);
    }
	
	/**
     * Gets the value of the multi select property.   Multi select means that multiple items
     * can be selected from the list of choices for the question.  Typically multi select is
     * displayed as a group of checkbox items whereas single select is displayed as a radio
     * button group.
     * 
     * @return Boolean - True if multi select is enabled, false otherwise.
     */
	public Boolean getMultiSelectEnabled() {
	    return properties.getBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED);
	}

	/**
     * Sets the value of the multi select property.  Multi select means that multiple items
     * can be selected from the list of choices for the question.  Typically multi select is
     * displayed as a group of checkbox items whereas single select is displayed as a radio
     * button group.
     * 
     * @param isEnabled - True if multi select should be enabled, false otherwise.
     */
    public void setMultiSelectEnabled(Boolean isEnabled) {
        properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED, isEnabled);
        
    }
    
    /**
     * Gets the option list with the reply choices for the question
     *
     * @return OptionList The option list with the reply choices for the question
     */
    public OptionList getReplyOptionSet() {
        if(properties.hasProperty(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY)) {
        	return (OptionList) properties.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
        }
        
        return null;
    }

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED));
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY) != null){
    		properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY, props.getIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY));
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY) != null){
    		properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY, props.getIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY));
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY) != null && 
    			props.getIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY) != null &&
    			props.getBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED) == null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED, true);
    	}
       	if(props.hasProperty(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY)){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY));
    	}
    }

}
