/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestion;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.RatingScaleQuestion;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyQuestions;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyQuestionsResult;
import mil.arl.gift.ums.db.survey.Surveys;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler to get a list of multiple choice, matrix of choices, and 
 * rating scale questions from a survey in a survey context.
 * 
 * @author bzahid
 */
public class FetchSurveyQuestionsHandler implements ActionHandler<FetchSurveyQuestions, FetchSurveyQuestionsResult> {

    private static final String METRICS_TAG = "util.FetchSurveyQuestions";
    
	@Override
	public FetchSurveyQuestionsResult execute(FetchSurveyQuestions action,
			ExecutionContext context) throws DispatchException {
		
	    long start = System.currentTimeMillis();
	    
		FetchSurveyQuestionsResult result = new FetchSurveyQuestionsResult();
		
		try {
			Survey survey = Surveys.getSurveyContextSurvey(action.getSurveyContextId(), action.getGiftKey());
			
			for(SurveyPage page : survey.getPages()) {
				
				for(AbstractSurveyElement element : page.getElements()) {
					
					if(element instanceof AbstractSurveyQuestion<?>) {
						
						AbstractSurveyQuestion<?> surveyQuestion = ((AbstractSurveyQuestion<?>) element);
						
						AbstractQuestion question = surveyQuestion.getQuestion();
						
						if(question instanceof MultipleChoiceQuestion
								||question instanceof RatingScaleQuestion 
								|| question instanceof MatrixOfChoicesQuestion 
								|| question instanceof FillInTheBlankQuestion) {
							
							if(action.getUsername() != null){
								
								if(question.getVisibleToUserNames() == null 
				    						|| question.getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)
				    						|| question.getVisibleToUserNames().contains(action.getUsername())){
			    					
			    					result.getSurveyQuestions().add(surveyQuestion);
			    				}
								
							} else {
								result.getSurveyQuestions().add(surveyQuestion);
							}
						}
					}
					
				}
			}
		} catch(Exception e) {
			result.setErrorMsg("An error occurred while getting questions from the survey context survey" + action.getGiftKey());
			result.setErrorDetails(e.toString());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
			result.setSuccess(false);
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
		
		return result;
	}

	@Override
	public Class<FetchSurveyQuestions> getActionType() {
		return FetchSurveyQuestions.class;
	}

	@Override
	public void rollback(FetchSurveyQuestions action,
			FetchSurveyQuestionsResult result, ExecutionContext context)
			throws DispatchException {
		// nothing to roll back		
	}

}
