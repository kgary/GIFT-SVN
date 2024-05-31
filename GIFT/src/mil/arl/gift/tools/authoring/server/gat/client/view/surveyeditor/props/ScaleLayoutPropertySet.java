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
 * The ScaleLayoutPropertySet contains the properties that allow for changing the style
 * of the rating scale bar.
 * 
 * @author nblomberg
 *
 */
public class ScaleLayoutPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(ScaleLayoutPropertySet.class.getName());
   
    /** Boolean to indicate if the image  should be displayed. */
    public boolean displayImage = false;

	/**
	 * Constructor (default)
	 */
	public ScaleLayoutPropertySet() {
	    super();

	    setUseBarLayout(false);
	}
	
	public boolean getUseBarLayout(){
		return properties.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT, false);
	}
	
	public void setUseBarLayout(boolean useBar){
		 properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT, useBar);
	}

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT));
    	}
    }

}
