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
 * Event to signal when the survey is requested to be saved.
 * 
 * @author nblomberg
 *
 */
public class SurveySaveEvent extends GenericEvent {

    /** Optional boolean to indicate if the editor should close after the save event. */
    private boolean closeAfterSave = false;
    
	/**
	 * Constructor
	 */
	public SurveySaveEvent() {
		
	}
	
	public SurveySaveEvent(boolean closeAfterSave) {
	    this.closeAfterSave = closeAfterSave;
	}
	
	/**
	 * Accessor to return if the event is signalling to close the editor after the save.
	 * 
	 * @return True if the editor should close after the save, false otherwise.
	 */
	public boolean getCloseAfterSave() {
	    return this.closeAfterSave;
	}


}
