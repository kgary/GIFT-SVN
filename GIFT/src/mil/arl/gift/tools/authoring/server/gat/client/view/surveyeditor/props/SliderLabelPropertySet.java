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
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;

/**
 * The SliderLabelPropertySet contains the properties that allow for customization
 * of the label text for the slider question.
 * 
 * @author nblomberg
 *
 */
public class SliderLabelPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SliderLabelPropertySet.class.getName());

	/**
	 * Constructor (default)
	 */
	public SliderLabelPropertySet() {
	    super();
	    
	    setHiddenPropertySet(true);
	    
	    properties.setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, "");
        properties.setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, "");
	}

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
        
        if (props.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY));
        }
        
        if (props.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY));
        }
        
    }

}
