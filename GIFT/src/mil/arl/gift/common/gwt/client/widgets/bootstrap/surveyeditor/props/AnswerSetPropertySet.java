/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.io.Constants;
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
public class AnswerSetPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(AnswerSetPropertySet.class.getName());


	/**
	 * Constructor (default)
	 */
	public AnswerSetPropertySet() {
	    super();
	    
	    properties.setPropertyValue(SurveyPropertyKeyEnum.REPLY_FEEDBACK, "");
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ANSWER_SET, false);
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE, false);
	    
	    OptionList optionList = new OptionList();
	    optionList.setIsShared(false);
	    optionList.setListOptions(new ArrayList<ListOption>());
	    properties.setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, optionList);
	}

	public void setReplyFeedbacks(List<String> feedbacks){
    	properties.setPropertyValue(SurveyPropertyKeyEnum.REPLY_FEEDBACK, SurveyItemProperties.encodeListString(feedbacks, Constants.PIPE));
    }
    
    public List<String> getReplyFeedbacks(){
    	return SurveyItemProperties.decodeListString((String) properties.getPropertyValue(SurveyPropertyKeyEnum.REPLY_FEEDBACK), Constants.PIPE);
    }	
	
    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
        Boolean useExistingAnswerSet = props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ANSWER_SET);
        
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY) != null && props.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY) instanceof OptionList){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY));
    		OptionList optionList = (OptionList) props.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
    		// if the option list is shared, useExistingAnswerSet property should always be true
    		if(optionList.getIsShared()){
    			useExistingAnswerSet = true;
    		}
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
    	}
    	
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.REPLY_FEEDBACK) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.REPLY_FEEDBACK, props.getPropertyValue(SurveyPropertyKeyEnum.REPLY_FEEDBACK));
    	}
    	
    	// if the USE_EXISTING_ANSWER_SET property has a value or if option list is shared, update the properties with the new value
    	if(useExistingAnswerSet != null){
            properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ANSWER_SET, useExistingAnswerSet);
        }
    }

}
