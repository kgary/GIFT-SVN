/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPickerQuestionBank;

/**
 * An event fired whenever a course's question bank is changed
 * 
 * @author nroberts
 */
public class QuestionBankChangedEvent extends GenericEvent {
	
	/** The survey picker that fired the event */
	private SurveyPickerQuestionBank surveySource;
	
	/**
	 * Instantiates an event fired whenever a course's question bank is changed
	 * 
	 * @param source the survey picker that fired the event
	 */
	public QuestionBankChangedEvent(SurveyPickerQuestionBank surveySource){
		this.surveySource = surveySource;
	}

	public SurveyPickerQuestionBank getSurveySource() {
		return surveySource;
	}
}
