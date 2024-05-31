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
 * Event sent to request to open the "add survey item" dialog.  
 * 
 * @author nblomberg
 *
 */
public class SurveyAddSurveyItemEvent extends GenericEvent {

	/** The widget page id that the dialog will be shown in.  Null means that the add survey dialog will be shown on the 'selected page'. */
    private SurveyWidgetId pageId;
    
    /** The question id that the dialog should be shown (before or after).  If null, the dialog will appear at the end of the page. */
    private SurveyWidgetId questionId;
    
    /** The order (before/after) that the dialog will be shown based on the question id.  */
    private InsertOrder insertOrder;
    
    /** Enum defining the supported order types. */
    public enum InsertOrder {
        ORDER_BEFORE,
        ORDER_AFTER
    }
    
    /**
     * Constructor (default)
     * Used to add the 'add survey item' dialog to the selected page at the end of the page.
     * 
     */
    public SurveyAddSurveyItemEvent() {
       setPageId(null);
       setQuestionId(null);
       setInsertOrder(InsertOrder.ORDER_AFTER);
    }
	/** 
	 * Constructor (defaults the insert order to ORDER_AFTER).
	 * 
	 * @param pageId - The page id that the dialog should be shown for.  Null implies that the dialog should be shown for the 'selected page'.
	 * @param questionId - The question id that the dialog will be displayed after.  
	 *                     Null implies that the dialog will be shown at the end of the page.
	 */
    public SurveyAddSurveyItemEvent(SurveyWidgetId pageId, SurveyWidgetId questionId) {
        this(pageId, questionId, InsertOrder.ORDER_AFTER);
    }
    
    /**
     * Constructor
     * @param pageId - The page id that the dialog should be shown for.  Null implies that the dialog should be shown for the 'selected page'.
     * @param questionId - The question id that the dialog will be displayed after.  
     *                     Null implies that the dialog will be shown at the end of the page.
     * @param order - The order that the dialog should appear on the page (before or after the question).
     */
	public SurveyAddSurveyItemEvent(SurveyWidgetId pageId, SurveyWidgetId questionId, InsertOrder order) {
	    setPageId(pageId);
	    setQuestionId(questionId);
	    setInsertOrder(order);
	}

	/**
	 * Accessor to get the question id.
	 * 
	 * @return SurveyWidgetId - the widget id of the question that the dialog should appear (before or after).  Null means that the dialog
	 *                          will be shown at the end of the page.
	 */
    public SurveyWidgetId getQuestionId() {
        return questionId;
    }

    /**
     * Accessor to set the question id.
     * 
     * param questionId - the widget id of the question that the dialog should appear (before or after).  Null means that the dialog
     *                    will be shown at the end of the page.
     */
    public void setQuestionId(SurveyWidgetId questionId) {
        this.questionId = questionId;
    }

    /**
     * Accessor to get the page id.
     * 
     * @return SurveyWidgetId - the widget id of the page that the dialog will be shown for.  Null means that the dialog will be shown 
     *                          on the currently selected page.
     */
    public SurveyWidgetId getPageId() {
        return pageId;
    }

    /**
     * Accessor to set the page id. 
     * 
     * @param pageId - the widget id of the page that the dialog will be shown for.  Null means that the dialog will be shown 
     *                          on the currently selected page.
     */
    public void setPageId(SurveyWidgetId pageId) {
        this.pageId = pageId;
    }

    /**
     * Accessor to get the order that the dialog will be inserted into the page (before or after the specified question).
     * @return InsertOrder - the order that the dialog will be inserted into the page (before or after the specified question).
     */
    public InsertOrder getInsertOrder() {
        return insertOrder;
    }

    /**
     * Accessor to set the order that the dialog will be inserted into the page (before or after the specified question).
     * 
     * @param insertOrder - the order that the dialog will be inserted into the page (before or after the specified question).
     */
    public void setInsertOrder(InsertOrder insertOrder) {
        this.insertOrder = insertOrder;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[SurveyAddSurveyItemEvent: pageId=");
        builder.append(pageId);
        builder.append(", questionId=");
        builder.append(questionId);
        builder.append(", insertOrder=");
        builder.append(insertOrder);
        builder.append("]");
        return builder.toString();
    }

}
