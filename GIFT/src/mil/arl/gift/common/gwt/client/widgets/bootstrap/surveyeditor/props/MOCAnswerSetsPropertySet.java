/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;

import java.util.ArrayList;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.SurveyItemProperties;


/**
 * The AnswerSetPropertySet class controls the properties that allow the user to select
 * an existing set of answers rather than use custom answer sets.
 * 
 * @author nblomberg
 *
 */
public class MOCAnswerSetsPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(AnswerSetPropertySet.class.getName());


	/**
	 * Constructor (default)
	 */
	public MOCAnswerSetsPropertySet() {
	    super();
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET, false);
	    OptionList rowOptionList = new OptionList();
	    rowOptionList.setIsShared(false);
	    rowOptionList.setListOptions(new ArrayList<ListOption>());
	    properties.setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, rowOptionList);
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET, false);
	    OptionList columnOptionList = new OptionList();
	    columnOptionList.setIsShared(false);
	    columnOptionList.setListOptions(new ArrayList<ListOption>());
	    properties.setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, columnOptionList);
	}


    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET));
    	}
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY) != null && props.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY) instanceof OptionList){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY));
    		OptionList optionList = (OptionList) props.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
    		if(optionList.getIsShared()){
    			properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET, true);
    		} else{
    			properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET, false);
    		}
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY) != null && props.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY) instanceof OptionList){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY));
    		OptionList optionList = (OptionList) props.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
    		if(optionList.getIsShared()){
    			properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET, true);
    		} else{
    			properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET, false);
    		}
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null) {
    		properties.setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
    	}
    }

}
