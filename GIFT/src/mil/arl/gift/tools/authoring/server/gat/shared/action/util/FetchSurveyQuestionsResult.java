/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Result containing the list of survey questions.
 * 
 * @author bzahid
 */
public class FetchSurveyQuestionsResult extends GatServiceResult {

	/**
	 * A list of multiple choice, rating scale, and matrix of choices 
	 * questions from a survey
	 */
	private List<AbstractSurveyQuestion<?>> surveyQuestions = new ArrayList<AbstractSurveyQuestion<?>>();
	
	/**
	 * Class constructor for serialization
	 */
	public FetchSurveyQuestionsResult() {
		super();
	}
	
	/**
	 * Sets the survey questions.
	 * 
	 * @param surveyQuestions A list of multiple choice, rating scale, and
	 * matrix of choices questions from a survey. 
	 */
	public void setSurveyQuestions(List<AbstractSurveyQuestion<?>> surveyQuestions) {
		this.surveyQuestions = surveyQuestions;
	}

	/**
	 * Sets the survey questions.
	 * 
	 * @return A list of multiple choice, rating scale, and
	 * matrix of choices questions from a survey. 
	 */
	public List<AbstractSurveyQuestion<?>> getSurveyQuestions() {
		return surveyQuestions;
	}
}
