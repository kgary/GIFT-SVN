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
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextById;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextByIdResult;
import mil.arl.gift.ums.db.survey.Surveys;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler that retrieves a survey context with the specified id.
 * 
 * @author bzahid
 */
public class FetchSurveyContextByIdHandler implements ActionHandler<FetchSurveyContextById, FetchSurveyContextByIdResult>{

    private static final String METRICS_TAG = "util.FetchSurveyContextById";
    
	@Override
	public FetchSurveyContextByIdResult execute(FetchSurveyContextById action, ExecutionContext context) 
			throws DispatchException {
		
	    long start = System.currentTimeMillis();   
		FetchSurveyContextByIdResult result = new FetchSurveyContextByIdResult();
		
		SurveyContext surveyContext;
		try{
		    surveyContext = Surveys.getSurveyContext(action.getSurveyContextId());
        }catch(Exception e){
            result.setSuccess(false);
            result.setErrorMsg("Failed to retrieve the survey for the course because there was a problem retrieving it.");
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            
            MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);  
            return result;
        }
		
		if(surveyContext != null) {
			result.setSurveyContext(surveyContext);
			result.setSuccess(true);
		} else {
			result.setSuccess(false);
			result.setErrorMsg("The survey context with id " + action.getSurveyContextId() + " was not found.");
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
		return result;
	}

	@Override
	public Class<FetchSurveyContextById> getActionType() {
		return FetchSurveyContextById.class;
	}

	@Override
	public void rollback(FetchSurveyContextById action, FetchSurveyContextByIdResult result, ExecutionContext context)
			throws DispatchException {
		// Nothing to do
	}

}
