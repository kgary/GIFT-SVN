/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;

import java.util.logging.Logger;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.survey.SurveyItemProperties;


/**
 * The HiddenAnswerWeightsPropertySet represents the weights associated with each
 * answer and the scoring attributes associated with them
 * 
 * @author wpearigen
 *
 */
public class DifficultyAndConceptsPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(DifficultyAndConceptsPropertySet.class.getName());


	/**
	 * Constructor (default)
	 */
	public DifficultyAndConceptsPropertySet() {
	    super();
	    properties.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, QuestionDifficultyEnum.EASY.getDisplayName());
	    properties.setPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS, "");
	    
	    setHiddenPropertySet(true);
	}


    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS, props.getPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS));
    	}
    }

}
