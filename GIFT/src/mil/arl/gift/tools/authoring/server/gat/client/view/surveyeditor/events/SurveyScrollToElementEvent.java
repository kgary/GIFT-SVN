/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * Event to signal that the survey panel should scroll to a specific element based on the html 'id' tag of the element.
 * This can be used to scroll to question widgets, add survey item dialogs, etc.
 * 
 * @author nblomberg
 *
 */
public class SurveyScrollToElementEvent extends GenericEvent {

    /** The html 'id' tag of the widget. */
    private String elementId = "";
    

    /**
     * Constructor 
     * 
     * @param id - The html 'id' tag of the widget.
     */
	public SurveyScrollToElementEvent(String id) {
	    elementId = id;
	}
	
	/** 
	 * Accessor to get the element id.
	 * 
	 * @return String - The html 'id' tag of the widget.
	 */
	public String getElementId() {
	    return elementId;
	}


}
