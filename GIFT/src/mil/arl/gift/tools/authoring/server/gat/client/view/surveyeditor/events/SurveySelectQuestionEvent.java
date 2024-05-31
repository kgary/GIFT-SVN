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
 * Event that is called to signal that a question should be selected.
 * The question id signals which question should be selected.
 * 
 * @author nblomberg
 *
 */
public class SurveySelectQuestionEvent extends GenericEvent {
	
	/** The widget id of the page that contains the question. */
	private SurveyWidgetId pageId;
	
	/** The widget id of the question that will be selected. */
	private SurveyWidgetId questionId;
	
	/** Whether or not any previously selected questions should remain selected */
	private boolean keepPreviousSelection = false;
	
	/**
	 * Constructor
	 * 
	 * @param pageId - The widget id of the page that contains the question..
	 * @param questionId - The widget id of the question that will be selected.
	 */
	public SurveySelectQuestionEvent(SurveyWidgetId pageId, SurveyWidgetId questionId) {
		setPageWidgetId(pageId);
		setQuestionId(questionId);
	}
	
	/**
	 * Constructor
	 * 
	 * @param pageId - The widget id of the page that contains the question..
	 * @param questionId - The widget id of the question that will be selected.
	 * @param keepPreviousSelection - Whether or not to all previously selected questions to remain selected
	 */
	public SurveySelectQuestionEvent(SurveyWidgetId pageId, SurveyWidgetId questionId, boolean keepPreviousSelection) {
		this(pageId, questionId);
		
		this.keepPreviousSelection = keepPreviousSelection;
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
     * Accessor to get the question widget id that will be selected.
     * 
     * @return SurveyWidgetId - The widget id of the question that will be selected.
     */
    public SurveyWidgetId getQuestionId() {
        return questionId;
    }

    /**
     * Accessor to set the question widget id that will be selected.
     * 
     * @param questionId - The widget id of the question that will be selected.
     */
    public void setQuestionId(SurveyWidgetId questionId) {
        this.questionId = questionId;
    }

    /**
     * Gets whether or not to all previously selected questions to remain selected
     * 
     * @return whether or not to all previously selected questions to remain selected
     */
	public boolean shouldKeepPreviousSelection() {
		return keepPreviousSelection;
	}
	
	
	/**
	 * Sets whether or not to all previously selected questions to remain selected
	 * 
	 * @param shouldKeep whether or not to all previously selected questions to remain selected
	 */
	public void setKeepPreviousSelection(boolean shouldKeep){
		this.keepPreviousSelection = shouldKeep;
	}
	
	@Override
	public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[SurveySelectQuestionEvent: ");
	    sb.append("surveyWidgetId = ").append(pageId);
	    sb.append(", questionId = ").append(questionId);
	    sb.append(", keepPreviousSelection = ").append(keepPreviousSelection);
	    sb.append("]");
	    return sb.toString();
	}
}
