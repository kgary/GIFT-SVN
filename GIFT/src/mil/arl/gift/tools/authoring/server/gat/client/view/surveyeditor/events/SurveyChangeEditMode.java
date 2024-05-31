/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * Event to signal that the editor mode has been changed (switched from writing or scoring mode).
 * 
 * @author nblomberg
 *
 */
public class SurveyChangeEditMode extends GenericEvent {

	

    /** The edit mode of the event (scoring or writing mode). */
    private SurveyEditMode editMode;

    
    /** 
     * 
     * Constructor (default)
     * 
     */
	public SurveyChangeEditMode(SurveyEditMode mode) {
		setEditMode(mode);
	}


	/**
	 * Accessor to get the edit mode of the event.
	 * 
	 * @return SurveyEditMode - The edit mode of the event.
	 */
    public SurveyEditMode getEditMode() {
        return editMode;
    }


    /**
     * Accessor to set the edit mode of the event.
     * 
     * @param editMode - The edit mode that the event will be set to.
     */
    public void setEditMode(SurveyEditMode editMode) {
        this.editMode = editMode;
    }

	

}
