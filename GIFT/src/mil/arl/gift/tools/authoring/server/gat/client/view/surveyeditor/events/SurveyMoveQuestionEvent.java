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
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyPageWidget.MoveOrderEnum;

/**
 * Event that is called to signal that a question should be moved in the list of questions for a page.
 * The question id signals which question should be moved.
 * The MoveOrderEnum controls the direction that the question should be moved (up or down).
 * 
 * @author nblomberg
 *
 */
public class SurveyMoveQuestionEvent extends GenericEvent {

    /** The order that the page should be moved in the list. */
	private MoveOrderEnum pageOrder;
	
	/** The pageId that that contains the question. */
	private SurveyWidgetId pageId;
	
	/** The questionId that should be moved. */
	private SurveyWidgetId questionId;
	
	/**
	 * Constructor
	 * 
	 * @param pId - The id of the page that contains the question to be moved.
	 * @param qId - The id of the question that will be moved.
	 * @param order - The order the question should be moved (up or down).
	 */
	public SurveyMoveQuestionEvent(SurveyWidgetId pageId, SurveyWidgetId questionId, MoveOrderEnum order) {
		setPageWidgetId(pageId);
		setQuestionId(questionId);
		setPageOrder(order);
	}

	/**
	 * Accessor to get the page order.
	 * 
	 * @return PageOrderEnum - The order the page should be moved (up or down) in the list.
	 */
    public MoveOrderEnum getPageOrder() {
        return pageOrder;
    }

    /**
     * Accessor to set the order the page should be moved in the list (up or down).
     * @param pageOrder - The order the page should be moved in the list (up or down).
     */
    public void setPageOrder(MoveOrderEnum pageOrder) {
        this.pageOrder = pageOrder;
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
     * Accessor to get the question widget id of the question that will be moved.
     * 
     * @return SurveyWidgetId - The id of the question that will be moved.
     */
    public SurveyWidgetId getQuestionId() {
        return questionId;
    }

    /**
     * Accessor to set the widget id of the question that will be moved.
     * 
     * @param questionId - The widget id of the question that will be moved.
     */
    public void setQuestionId(SurveyWidgetId questionId) {
        this.questionId = questionId;
    }


}
