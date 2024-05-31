/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteCourseSurveyReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurveyContextResult;
import mil.arl.gift.ums.db.survey.Surveys;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler for deleting a course's reference to a survey by removing the survey context survey row in the survey db.
 * This doesn't delete the underlying survey nor any survey responses.
 * 
 * @author mhoffman
 *
 */
public class DeleteCourseSurveyReferenceHandler implements ActionHandler<DeleteCourseSurveyReference, DeleteSurveyContextResult> {

    @Override
    public DeleteSurveyContextResult execute(DeleteCourseSurveyReference action, ExecutionContext context)
            throws DispatchException {
        
        int surveyContextId = action.getSurveyContextId();
        String giftKey = action.getSurveyKey();
        String username = action.getUsername();
        
        DeleteSurveyContextResult result = new DeleteSurveyContextResult();
        result.setSuccess(true);
        try{
            Surveys.deleteSurveyContextSurvey(giftKey, surveyContextId, username);            
        }catch(DetailedException e){
            
            result.setSuccess(false);
            result.setErrorMsg("Failed to remove the reference to the survey from the set of surveys for this course because "+ e.getReason());
            result.setErrorDetails(e.getDetails());
            result.setErrorStackTrace(e.getErrorStackTrace());
            
        }catch(Exception e){
            
            result.setSuccess(false);
            result.setErrorMsg("Failed to remove the reference to the survey from the set of surveys for this course.");
            result.setErrorDetails("The server had an error:\n"+e.toString());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }

        return result;
    }

    @Override
    public Class<DeleteCourseSurveyReference> getActionType() {
        return DeleteCourseSurveyReference.class;
    }

    @Override
    public void rollback(DeleteCourseSurveyReference arg0, DeleteSurveyContextResult arg1, ExecutionContext arg2)
            throws DispatchException {
        // nothing to do        
    }

}
