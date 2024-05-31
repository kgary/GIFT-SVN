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
 * The CustomAlignmentPropertySet contains properties that allow for tweaking custom alignments for 
 * the answers in a question.
 * 
 * @author nblomberg
 *
 */
public class CustomAlignmentPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CustomAlignmentPropertySet.class.getName());
    
    /** The default column width for custom alignment property */
    public static final int DEFAULT_COLUMN_WIDTH = 50;
   

	/**
	 * Constructor (default)
	 */
	public CustomAlignmentPropertySet() {
	    super();	    
	}


    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT));
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY) != null){
    		properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY, props.getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY));
    	}
    	if(props.getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY) != null){
    		properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY, props.getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY));
    	}
    }

}
