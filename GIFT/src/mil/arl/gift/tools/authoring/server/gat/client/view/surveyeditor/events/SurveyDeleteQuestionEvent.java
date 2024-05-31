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
 * Event that is called to signal that a question should be deleted.
 * The question id signals which question should be deleted.
 * 
 * @author nblomberg
 *
 */
public class SurveyDeleteQuestionEvent extends GenericEvent {
	
	/** The widget id of the page that contains the question. */
	private SurveyWidgetId pageId;
	
	/** The widget id of the question that will be deleted. */
	private SurveyWidgetId questionId;
	
	/**
	 * Constructor
	 * 
	 * @param pageId - The widget id of the page that contains the question..
	 * @param questionId - The widget id of the question that will be deleted.
	 */
	public SurveyDeleteQuestionEvent(SurveyWidgetId pageId, SurveyWidgetId questionId) {
		setPageWidgetId(pageId);
		setQuestionId(questionId);
	}

    /**
     * Accessor to get the page widget id containing the question.
     * 
     * @return SurveyWidgetId - The widget id of the page that contains the question.
     */
    public SurveyWidgetId getPageWidgetId() {
        return pageId;
    }

    /**
     * Accessor to set the widget id of the page that contains the question.
     * 
     * @param pageId - the page id that contains the question.
     */
    public void setPageWidgetId(SurveyWidgetId pageId) {
        this.pageId = pageId;
    }

    /**
     * Accessor to get the question widget id that will be deleted.
     * 
     * @return SurveyWidgetId - The widget id of the question that will be deleted.
     */
    public SurveyWidgetId getQuestionId() {
        return questionId;
    }

    /**
     * Accessor to set the question widget id that will be deleted.
     * 
     * @param questionId - The widget id of the question that will be deleted.
     */
    public void setQuestionId(SurveyWidgetId questionId) {
        this.questionId = questionId;
    }
}
