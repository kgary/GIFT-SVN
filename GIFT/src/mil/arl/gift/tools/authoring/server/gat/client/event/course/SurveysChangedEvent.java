/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPicker;

/**
 * An event fired whenever a course's surveys are changed
 * 
 * @author nroberts
 */
public class SurveysChangedEvent extends GenericEvent {
	
	/** The survey picker that fired the event */
	private SurveyPicker surveySource;
	
	/** whether the author selected the 'Use Original' check box on the select survey dialog */
	private boolean useOriginal;
	
	/**
	 * Instantiates an event fired whenever a course's surveys are changed
	 * 
	 * @param source the survey picker that fired the event
	 * @param useOriginal whether the author selected the 'Use Original' check box on the select survey dialog
	 */
	public SurveysChangedEvent(SurveyPicker surveySource, boolean useOriginal){
		this.surveySource = surveySource;
		this.useOriginal = useOriginal;
	}

	public SurveyPicker getSurveySource() {
		return surveySource;
	}
	
	/**
	 * Return whether the author selected the 'Use Original' check box on the select survey dialog
	 * 
	 * @return the 'use original' check box value
	 */
	public boolean useOriginal(){
	    return useOriginal;
	}
}
