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
 * Event sent when a survey page should be deleted.  The pageId specifies
 * which page widget should be deleted.
 * 
 * @author nblomberg
 *
 */
public class SurveyDeletePageEvent extends GenericEvent {

	/** The page id that should be deleted. */
    private SurveyWidgetId widgetId = null;
    
    /** 
     * Constructor
     * 
     * @param id - The page id to be deleted.
     */
	public SurveyDeletePageEvent(SurveyWidgetId wId) {
	    widgetId = wId;
	}
	
	/**
	 * Accessor to retrieve the page widget id.
	 * 
	 * @return SurveyWidgetId - The page widget id for the event.
	 */
	public SurveyWidgetId getWidgetId() {
	    return widgetId;
	}


}
