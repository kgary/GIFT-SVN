/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyWidgetId;

/**
 * Event that is called to signal that the selected questions should be copied to the location of another question.
 * The question id signals which question the selected questions should be copied to.
 * 
 * @author nblomberg
 *
 */
public class SurveyCopyQuestionEvent extends GenericEvent {
	
	/** The pageId that that contains the question. */
	private SurveyWidgetId pageId;
	
	/** The questionId that should be moved. */
	private SurveyWidgetId questionId;
	
	/**
	 * Constructor
	 * 
	 * @param pId - The id of the page that contains the question the selected questions should be copied to.
	 * @param qId - The id of the question the selected questions should be copied to.
	 */
	public SurveyCopyQuestionEvent(SurveyWidgetId pageId, SurveyWidgetId questionId) {
		setPageWidgetId(pageId);
		setQuestionId(questionId);
	}

    /**
     * Accessor to get the page widget id that contains the question.
     * 
     * @return SurveyWidgetId - The widget id of the page that contains the question.
     */
    public SurveyWidgetId getPageWidgetId() {
        return pageId;
    }

    /**
     * Accessor to set the page id.
     * 
     * @param pageId - the page id that contains the question.
     */
    public void setPageWidgetId(SurveyWidgetId pageId) {
        this.pageId = pageId;
    }

    /**
     * Accessor to get the question widget id of the question the selected questions should be copied to.
     * 
     * @return SurveyWidgetId - The id of the question the selected questions should be copied to.
     */
    public SurveyWidgetId getQuestionId() {
        return questionId;
    }

    /**
     * Accessor to set the widget id of the question the selected questions should be copied to.
     * 
     * @param questionId - The widget id of the question the selected questions should be copied to.
     */
    public void setQuestionId(SurveyWidgetId questionId) {
        this.questionId = questionId;
    }


}
