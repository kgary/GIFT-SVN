/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveys;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveysResult;
import mil.arl.gift.ums.db.survey.Surveys;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler to retrieve survey context surveys.
 * 
 * @author bzahid
 */
public class FetchSurveyContextSurveysHandler implements ActionHandler<FetchSurveyContextSurveys, FetchSurveyContextSurveysResult> {

    private static final String METRICS_TAG = "util.FetchSurveyContextSurveys";
    
	@Override
	public FetchSurveyContextSurveysResult execute(FetchSurveyContextSurveys action, ExecutionContext context)
			throws DispatchException {
		
	    long start = System.currentTimeMillis();
	    
		FetchSurveyContextSurveysResult result = new FetchSurveyContextSurveysResult();
		
		try {
			
			if(action.getUsername() != null){
				
				List<SurveyContextSurvey> surveys = new ArrayList<SurveyContextSurvey>();
				
				for(SurveyContextSurvey survey : Surveys.getSurveyContextSurveys(action.getSurveyContextId())){
					
					if(survey.getSurvey() != null 
							&&  (survey.getSurvey().getVisibleToUserNames() == null 
	    						|| survey.getSurvey().getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)
	    						|| survey.getSurvey().getVisibleToUserNames().contains(action.getUsername()))){
    					
    					surveys.add(survey);
    				}
				}
				
				result.setSurveys(surveys);
			
			} else {
				result.setSurveys(Surveys.getSurveyContextSurveys(action.getSurveyContextId()));
			}
			result.setSuccess(true);
			
		} catch(Exception e) {
			result.setErrorMsg("There was an error retrieving surveys from the survey context with id " + action.getSurveyContextId());
			result.setErrorDetails(e.toString());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
			result.setSuccess(false);
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
		
		return result;
	}

	@Override
	public Class<FetchSurveyContextSurveys> getActionType() {
		return FetchSurveyContextSurveys.class;
	}

	@Override
	public void rollback(FetchSurveyContextSurveys arg0, FetchSurveyContextSurveysResult arg1,
			ExecutionContext arg2) throws DispatchException {
		// nothing to do
	}

}
