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
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;

/**
 * The CommonPropertySet contains properties that are common to many (but not all) questions such as
 * help string, if the question is required, etc.
 * 
 * @author nblomberg
 *
 */
public class CommonPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CommonPropertySet.class.getName());
   
    /** the survey question associated with these properties (optional) */
    private AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion;

	/**
	 * Set default property values
	 * @param isScored whether the survey question associated with these common survey question properties
	 * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public CommonPropertySet(boolean isScored) {
	    super();
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED, false);
	    properties.setPropertyValue(SurveyPropertyKeyEnum.TAG, "");
	    properties.setPropertyValue(SurveyPropertyKeyEnum.HELP_STRING, "");
	    properties.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID, "");
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.CAN_QUESTION_HAVE_SCORING, isScored);
	}

	/**
	 * Set the survey question associated with these properties.
	 * 
	 * @param surveyQuestion can be null.
	 */
	public void setSurveyQuestion(AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion){
	    this.surveyQuestion = surveyQuestion;
	}
	
	/**
	 * Return the survey question associated with these properties.
	 * 
	 * @return can be null if not set.
	 */
	public AbstractSurveyQuestion<? extends AbstractQuestion> getSurveyQuestion(){
	    return surveyQuestion;
	}

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.TAG) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.TAG, props.getPropertyValue(SurveyPropertyKeyEnum.TAG));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.HELP_STRING) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.HELP_STRING, props.getPropertyValue(SurveyPropertyKeyEnum.HELP_STRING));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID) != null){
            properties.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID, props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID));
        }
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.ALLOW_PARTIAL_CREDIT) != null) {
            properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.ALLOW_PARTIAL_CREDIT, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.ALLOW_PARTIAL_CREDIT));
    	}
    }

}
