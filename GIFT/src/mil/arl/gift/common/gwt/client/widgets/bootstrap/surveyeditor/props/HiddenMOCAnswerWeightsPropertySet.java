/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;


/**
 * The HiddenMOCAnswerWeightsPropertySet represents the weights associated with each
 * answer and the scoring attributes associated with them for Matrix of Choices Questions only
 * 
 * @author wpearigen
 *
 */
public class HiddenMOCAnswerWeightsPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(HiddenMOCAnswerWeightsPropertySet.class.getName());


	/**
	 * Constructor (default)
	 */
	public HiddenMOCAnswerWeightsPropertySet() {
	    super();
	    List<List<Double>> list = new ArrayList<List<Double>>();
	    
	    properties.setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, new MatrixOfChoicesReplyWeights(list));
	    properties.setPropertyValue(SurveyPropertyKeyEnum.SCORERS, new QuestionScorer(false, new HashSet<AttributeScorerProperties>()));
	    
	    setHiddenPropertySet(true);
	}


    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.SCORERS) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.SCORERS, props.getPropertyValue(SurveyPropertyKeyEnum.SCORERS));
    	}
    }

}
