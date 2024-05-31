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

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.survey.FreeResponseReplyWeights;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;

/**
 * The HiddenMFRAnswerWeightsPropertySet represents the weights associated with each answer and the
 * scoring attributes associated with them for Multiple Free Response questions only
 * 
 * @author sharrison
 *
 */
public class HiddenMFRAnswerWeightsPropertySet extends AbstractPropertySet {

    /**
     * Constructor (default)
     */
    public HiddenMFRAnswerWeightsPropertySet() {
        super();

        properties.setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, new FreeResponseReplyWeights(new ArrayList<List<List<Double>>>()));
        properties.setPropertyValue(SurveyPropertyKeyEnum.SCORERS, new QuestionScorer(false, new HashSet<AttributeScorerProperties>()));

        setHiddenPropertySet(true);
    }

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
        if (props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, props.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
        }
        if (props.getPropertyValue(SurveyPropertyKeyEnum.SCORERS) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.SCORERS, props.getPropertyValue(SurveyPropertyKeyEnum.SCORERS));
        }
    }
}
