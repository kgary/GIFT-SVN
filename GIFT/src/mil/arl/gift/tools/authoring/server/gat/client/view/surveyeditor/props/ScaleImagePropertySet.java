/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.survey.SurveyItemProperties;

/**
 * The ScaleImagePropertySet contains the properties to add or customize any image that will be 
 * displayed in the rating scale answer.
 * 
 * @author nblomberg
 *
 */
public class ScaleImagePropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(ScaleImagePropertySet.class.getName());

	/**
	 * Constructor (default)
	 */
	public ScaleImagePropertySet() {
	    super();

	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE, false);
	    properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY, 0);
	}

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE));
    	}
    	if(props.hasProperty(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY) && props.getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY));
    		if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE) == null){
                properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE, true);
            }
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY) != null){
    		properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY, props.getIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY));
    	}
    }

}
