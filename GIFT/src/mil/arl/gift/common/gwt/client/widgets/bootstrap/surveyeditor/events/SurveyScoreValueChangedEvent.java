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
 * The SurveyScoreValueChangedEvent signals that a score value for a question or item
 * in a survey could have changed (or a propery was updated that affected the score).  
 * 
 * This event is used to let the Survey Panel retotal the point values of the questions.
 * 
 * @author nblomberg
 *
 */
public class SurveyScoreValueChangedEvent extends GenericEvent {


    /**
     * Constructor (default)
     */
	public SurveyScoreValueChangedEvent() {
	   
	}

}
