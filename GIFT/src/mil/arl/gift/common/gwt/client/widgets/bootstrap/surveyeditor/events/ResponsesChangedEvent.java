/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * Event sent to update the number of responses for the current 
 * multiple choice question.
 */
public class ResponsesChangedEvent extends GenericEvent {

	/** The total number of responses for the current question */
    private int totalResponses = 0;

    /**
     * Constructor (default)
     * @param totalResponses The total number of responses available for the current question
     */
    public ResponsesChangedEvent(int totalResponses) {
       this.totalResponses = totalResponses;
    }	
    
    /**
     * Gets the total number of responses for the current question.
     * @return the total number of responses for the current question.
     */
    public int getTotalResponses() {
    	return totalResponses;
    }

}
