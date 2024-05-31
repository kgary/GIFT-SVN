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
 * The property set relating to the setting that controls if the answer field
 * should be displayed as a text box (versus a text area).
 * 
 * @author nblomberg
 *
 */
public class AnswerFieldTextBoxPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(AnswerFieldTextBoxPropertySet.class.getName());


	/**
	 * Constructor (default)
	 */
	public AnswerFieldTextBoxPropertySet() {
	    super();
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY, true);
	    
	    // The answer field text box property set is never shown to the user.
	    setHiddenPropertySet(true);
	}


    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
        if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY) != null){
        	properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY));
        }
    }

}
