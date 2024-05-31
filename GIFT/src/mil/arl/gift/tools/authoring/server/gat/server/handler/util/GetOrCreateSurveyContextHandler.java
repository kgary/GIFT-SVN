/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.HashSet;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetOrCreateSurveyContext;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetOrCreateSurveyContextResult;
import mil.arl.gift.ums.db.survey.Surveys;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler that retrieves a survey context with the specified id.
 * 
 * @author nroberts
 */
public class GetOrCreateSurveyContextHandler implements ActionHandler<GetOrCreateSurveyContext, GetOrCreateSurveyContextResult>{

    private static final String METRICS_TAG = "util.GetOrCreateSurveyContext";
    
	@Override
	public GetOrCreateSurveyContextResult execute(GetOrCreateSurveyContext action, ExecutionContext context) 
			throws DispatchException {
		
	    long start = System.currentTimeMillis();
	    
		GetOrCreateSurveyContextResult result = new GetOrCreateSurveyContextResult();
		boolean surveyContextExists = false;
		
		if(action.getSurveyContextId() != null){
			
			//if the caller has defined a survey context to load, to to find it in the database
			surveyContextExists = Surveys.doesSurveyContextExist(action.getSurveyContextId());
		}
		
		if(surveyContextExists) {
			
			//if the caller has defined a survey context AND it is found in the database, return that survey context
			result.setSurveyContext(action.getSurveyContextId());
			result.setSuccess(true);
			
		} else {
			
			//otherwise, create a new survey context
			SurveyContext surveyContext = new SurveyContext();
			
			if(action.getCourseName() != null){
				surveyContext.setName(action.getCourseName());
				
			} else {
				surveyContext.setName("Course Survey Context");
			}
			
			if(action.getUsername() != null){
				
				HashSet<String> editableUsers = new HashSet<String>();			
				editableUsers.add(action.getUsername());
				
				surveyContext.setEditableToUserNames(editableUsers);
				
				HashSet<String> visibleUsers = new HashSet<String>();	
				visibleUsers.add(action.getUsername());
				
				surveyContext.setVisibleToUserNames(visibleUsers);
			}
			
			try{
			    SurveyContext createdContext = Surveys.insertSurveyContext(surveyContext, null);
			    result.setSurveyContext(createdContext.getId());
				result.setSuccess(true);
			
			} catch(Exception e) {
				
				result.setErrorMsg("Failed to find or create a survey context for "
						+ action.getCourseName() != null ? action.getCourseName() : "course" + ".");
				result.setErrorDetails("Attempted to create a new survey context, but an exception was thrown that reads:\n"+e.getMessage());
				result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
				result.setSuccess(false);
			}
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
		
		return result;
	}

	@Override
	public Class<GetOrCreateSurveyContext> getActionType() {
		return GetOrCreateSurveyContext.class;
	}

	@Override
	public void rollback(GetOrCreateSurveyContext action, GetOrCreateSurveyContextResult result, ExecutionContext context)
			throws DispatchException {
		// Nothing to do
	}

}
