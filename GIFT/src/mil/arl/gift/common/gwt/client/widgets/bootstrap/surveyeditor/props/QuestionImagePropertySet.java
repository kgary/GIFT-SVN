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
import mil.arl.gift.common.survey.SurveyItemProperties;


/**
 * The QuestionImagePropertySet is the set of properties relating to the image
 * that can be set near the text of a survey question.
 * 
 * @author nblomberg
 *
 */
public class QuestionImagePropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(QuestionImagePropertySet.class.getName());

    /** The default value for the width property. */
    public static final Integer DEFAULT_WIDTH = 100;
	/**
	 * Constructor (default)
	 */
	public QuestionImagePropertySet() {
	    super();
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, false);
	    properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, 0);
	    properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, DEFAULT_WIDTH);
	}

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
        
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE));
    	}
    	
    	if(props.hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY) && props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY) != null){
            properties.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY));
            if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE) == null){
                properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, true);
            }
        }
    	
    	if(props.hasProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY) && props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY));
    		if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE) == null){
    			properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE, true);
    		}
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY) != null){
    		properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, props.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY));
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY) != null){
    		properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, props.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY));
    	}
    }

}
