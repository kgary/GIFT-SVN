/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import mil.arl.gift.common.gwt.client.survey.SurveyWidget;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.SurveyResponseMetadata;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.SurveyWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * This instance of a SurveyWidget allows the tutor to update the survey being presented
 * by showing survey response from an external source like a training application.
 * 
 * @author mhoffman
 *
 */
public class TutorSurveyWidget extends SurveyWidget implements IsUpdateableWidget{
    
    /**
     * Constructor, creates a widget for answering a survey
     *
     * @param survey The survey to display
     * @param previewMode If the survey is being previewed
     * @param isDebug whether the tutor is in debug mode.  This allows for logic such as color coding survey choices based on scoring
     */
    public TutorSurveyWidget(Survey survey, boolean previewMode, boolean isDebug) {
        this(survey, previewMode, isDebug, null);
    }

    /**
     * Constructor, creates a widget for reviewing the answer to a page
     *
     * @param survey The survey to display
     * @param previewMode If the survey is being previewed
     * @param isDebug whether the tutor is in debug mode.  This allows for logic such as color coding survey choices based on scoring
     * @param surveyResponseMetadata the optional response metadata to a survey represented by this widget
     */
    public TutorSurveyWidget(Survey survey, final boolean previewMode, final boolean isDebug, SurveyResponseMetadata surveyResponseMetadata) {
        super(survey, previewMode, isDebug, surveyResponseMetadata);
    }
    
    @Override
    public void update(WidgetInstance instance) {
        
        WidgetProperties widgetProperties = instance.getWidgetProperties();
        SurveyResponse surveyResponse = SurveyWidgetProperties.getAnswers(widgetProperties);
        if(surveyResponse != null){
            applyExternalSurveyResponse(surveyResponse);
        }
        
        if(SurveyWidgetProperties.shouldSubmitSurveyPage(widgetProperties)){
            applyExternalSurveySubmit();
        }
    }

	@Override
	public WidgetTypeEnum getWidgetType() {
		return WidgetTypeEnum.SURVEY_WIDGET;
	}
}
