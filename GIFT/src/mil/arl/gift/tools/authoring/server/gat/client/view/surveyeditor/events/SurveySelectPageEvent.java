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
 * Event sent when a survey page should be selected.  The pageId specifies
 * which page widget should be selected.
 * 
 * @author nblomberg
 *
 */
public class SurveySelectPageEvent extends GenericEvent {

	/** The page id that should be selected. */
    private SurveyWidgetId widgetId = null;
    
    /** 
     * Constructor
     * 
     * @param id - The page id to be selected.
     */
	public SurveySelectPageEvent(SurveyWidgetId id) {
	    widgetId = id;
	}
	
	/**
	 * Accessor to retrieve the pageId.
	 * 
	 * @return SurveyWidgetId - The widget id for the event.
	 */
	public SurveyWidgetId getPageId() {
	    return widgetId;
	}


}
