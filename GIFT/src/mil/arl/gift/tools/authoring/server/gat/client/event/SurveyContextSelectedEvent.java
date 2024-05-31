/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event;

import mil.arl.gift.common.survey.SurveyContext;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * The Class SurveyContextChangedEvent.
 */
public class SurveyContextSelectedEvent extends GenericEvent {

	/** The survey context that was selected*/
	private SurveyContext surveyContext;
	
	/**
	 * Instantiates a new survey context selected event.
	 *
	 * @param newSurveyContext the new survey context
	 */
	public SurveyContextSelectedEvent(SurveyContext newSurveyContext) {
		this.surveyContext = newSurveyContext;
	}
	
	/**
	 * Gets the survey context.
	 *
	 * @return the survey context
	 */
	public SurveyContext getSurveyContext() {
		return this.surveyContext;
	}
}
