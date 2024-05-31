/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.Set;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.server.survey.SurveyExportFileUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurvey;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurveyContextResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.survey.Surveys.SurveyDeleteConstraintTracker;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler that deletes a survey from a survey context
 * 
 * @author nroberts
 */
public class DeleteSurveyHandler implements ActionHandler<DeleteSurvey, DeleteSurveyContextResult>{

    private static final String METRICS_TAG = "util.DeleteSurvey";
    
	@Override
	public DeleteSurveyContextResult execute(DeleteSurvey action, ExecutionContext context) 
			throws DispatchException {
		
	    long start = System.currentTimeMillis();       
		DeleteSurveyContextResult result = new DeleteSurveyContextResult();
		
		int surveyContextId = action.getSurveyContextId();
		String giftKey = action.getSurveyKey();
		String surveyCourseFolderPath = action.getSurveyCourseFolderPath();
		String username = action.getUsername();
		
		Survey survey = null;
		/* Survey is from a survey export file. Remove it from the file instead of the database. */
		if (StringUtils.isNotBlank(surveyCourseFolderPath)) {
            try {
                AbstractFolderProxy surveyCourseFolderFile = ServicesManager.getInstance().getFileServices()
                        .getFolder(surveyCourseFolderPath, null);
                SurveyExportFileUtil.deleteSurveyFromExportFile(surveyCourseFolderFile, giftKey, username);

                // deletion has completed, so return a successful result
                result.setSuccess(true);
                MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
                return result;
            } catch (Exception e) {
                //Report that deletion failed on a survey
                result.setSuccess(false);
                result.setErrorMsg("The survey failed to delete.");
                result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);  
                return result;
            }
		} else if(surveyContextId > 0){
		    //deleting a survey in a single survey context
		    survey = Surveys.getSurveyContextSurvey(surveyContextId, giftKey);
		}else if(action.getSurveyId() > 0){
		    //deleting a survey across all survey contexts
		    survey = Surveys.getSurvey(action.getSurveyId());
		}else{
		    //error - cant determine survey identifier
            result.setSuccess(false);
            result.setErrorMsg("Failed to find the survey to delete because neither the survey context id or survey id was provided.");
            MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);  
            return result;
		}
			
		boolean canRemoveSurvey = false, canRemoveResponsesFromSpecifiedContext = false;
		
		//First check if user has bypass flag set to true in common properties and is in Desktop Deployment mode
		if(action.getBypassPermissionCheck()) {
			canRemoveSurvey = true;
			canRemoveResponsesFromSpecifiedContext = true;
		}else {
			boolean canEditSurvey = survey.getEditableToUserNames() != null && !survey.getEditableToUserNames().isEmpty();
			
			//if there are permissions, check to see if user belongs to user name permission list, and set value accordingly
			//otherwise, set allowed user to false. 
			if(canEditSurvey){				
				canRemoveSurvey = action.getUsername() != null  && survey.getEditableToUserNames().contains(action.getUsername());
						
			} else {
				canRemoveSurvey = false;
			}
			
			if(surveyContextId > 0){
			    canRemoveResponsesFromSpecifiedContext = Surveys.isSurveyContextEditable(surveyContextId, action.getUsername());
			}
		}

						
        if(action.shouldDeleteResponses()){
            
            if(canRemoveResponsesFromSpecifiedContext){               
                //delete the learner responses for this survey if the user specifies to do so and has survey context permission to do so
                
                try{
                    Surveys.deleteSurveyResponses(surveyContextId, survey.getId(), action.getUsername());
                }catch(Exception e){
                    
                    //Report that deletion failed on a survey
                    result.setSuccess(false);
                    result.setErrorMsg("One or more learner responses could not be deleted. The most likely reason "
                            + "is that another user may be using these resources for an ongoing operation.");
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);  
                    return result;
                }
            }else if(action.getSurveyId() > 0){
                //delete survey context responses for all survey context based on write permissions for each survey context 
                //this survey is in
                
                try{
                    Surveys.removeSurveyFromSurveyContexts(action.getSurveyId(), action.getUsername(), null);
                }catch(Exception e){
                    
                    //Report that deletion failed on a survey
                    result.setSuccess(false);
                    result.setErrorMsg("One or more learner responses could not be deleted. The most likely reason "
                            + "is that another user may be using these resources for an ongoing operation.");
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);  
                    return result;
                }
            }
        }            
        
        //check to see if this survey has any learner responses for this survey context
        boolean hasSurveyResponses;
        if(surveyContextId > 0){
            hasSurveyResponses = Surveys.hasSurveyResponses(surveyContextId, survey.getId());
        }else{
            hasSurveyResponses = Surveys.hasSurveyResponses(action.getSurveyId());
        }
        
        //check to see if this survey is used outside of this survey context
        //Note: calling this even if the survey context id is not a positive number to determine if there are any
        //      survey contexts with this survey
        boolean isUsedInOtherContexts = Surveys.isSurveyInAnotherSurveyContext(surveyContextId, survey.getId());
        
        if(!hasSurveyResponses){
            //don't enter this logic if there are still responses to allow the user to explicitly agree to removing
            //survey responses
                                            
            if(!isUsedInOtherContexts && canRemoveSurvey){                  
                //delete this survey if it isn't being used by another survey context and permission is granted
                
                try{
                    // keep track of survey items that are referenced across questions/surveys in order
                    // to make sure those items can actually be deleted w/o causing a foreign key constraint
                    SurveyDeleteConstraintTracker tracker = new SurveyDeleteConstraintTracker();
                    
                    Surveys.deleteSurveyAndQuestions(survey.getId(), action.getUsername(), null, tracker);
                    
                    // now deal with the constraints ---
                    // now that the surveys have been removed, see if any of the collected items 
                    // (e.g. option lists) can be removed.  The idea being if there are no more references
                    // remaining after deleting all the survey items this thread is deleting than the
                    // collected item (e.g. option list) is safe to remove.  Hibernate apparently can't handle our complicated
                    // tables, even in a single transaction.
                    for(Integer dbOptionListId : tracker.optionListIdToOtherQuestionIds.keySet()){
                        
                        Set<Integer> questionIds = tracker.optionListIdToOtherQuestionIds.get(dbOptionListId);
                        if(questionIds == null || questionIds.isEmpty()){
                            //no more questions still reference this option list, safe to delete the option list
                            //Note: don't want tracker here since we know it will be safe to delete by this point
                            Surveys.deleteOptionList(dbOptionListId, action.getUsername(), null, null);
                        }
                    }
                    
                }catch(Exception e){
                    
                    //Report that deletion failed on a survey
                    result.setSuccess(false);
                    result.setErrorMsg("The most likely reason is that another user may be "
                            + "using these resources for an ongoing operation.");
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start); 
                    return result;
                }
                
            } else if(surveyContextId > 0){
                //delete the survey context survey reference to that survey (but not the survey itself)
            
                try{
                    //Delete the reference to this survey from the survey context. This should happen
                    //even if the survey is being used in another survey context.
                    Surveys.deleteSurveyContextSurvey(giftKey, surveyContextId, action.getUsername());
                    
                } catch(Exception e){
                    
                    result.setSuccess(false);
                    result.setErrorMsg("This survey could not be removed from this course's survey context. The most likely reason "
                            + "is that the survey has been removed already.");
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start); 
                    return result;
                }
            }else if(isUsedInOtherContexts && canRemoveSurvey){
                //the user has write permissions to the survey but possibly not all the survey contexts
                //There are no responses to this survey in any survey context at this point.
                
                try{
                    // keep track of survey items that are referenced across questions/surveys in order
                    // to make sure those items can actually be deleted w/o causing a foreign key constraint
                    SurveyDeleteConstraintTracker tracker = new SurveyDeleteConstraintTracker();
                    
                    Surveys.deleteSurvey(action.getSurveyId(), action.getUsername(), null, tracker);
                    
                    // now deal with the constraints ---
                    // now that the surveys have been removed, see if any of the collected items 
                    // (e.g. option lists) can be removed.  The idea being if there are no more references
                    // remaining after deleting all the survey items this thread is deleting than the
                    // collected item (e.g. option list) is safe to remove.  Hibernate apparently can't handle our complicated
                    // tables, even in a single transaction.
                    for(Integer dbOptionListId : tracker.optionListIdToOtherQuestionIds.keySet()){
                        
                        Set<Integer> questionIds = tracker.optionListIdToOtherQuestionIds.get(dbOptionListId);
                        if(questionIds == null || questionIds.isEmpty()){
                            //no more questions still reference this option list, safe to delete the option list
                            Surveys.deleteOptionList(dbOptionListId, action.getUsername(), null, null);
                        }
                    }
                    
                }catch(Exception e){
                    
                    result.setSuccess(false);
                    result.setErrorMsg("This survey could not be removed from all courses it might be referenced in.  Try deleting references to this survey in your course(s) first.  "+
                            "If you don't have permissions to a course that is using this survey you will not be able to delete the survey.");
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start); 
                    return result;
                }
                
            }else{
                //failure
                result.setSuccess(false);
                result.setErrorMsg("This survey could not be removed because you don't have write permissions.");
                MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start); 
                return result;
            }
            
            //deletion has completed, so return a successful result
            result.setSuccess(true);
            MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start); 
            return result;
            
        } else {
            
            //Report survey dependencies back to the user
            result.setSuccess(false);
            result.setHadSurveyResponses(true);
            result.setErrorMsg("This survey has recorded responses "
                    + "from learners who have taken this course. These responses need to be removed "
                    + "before the survey can be deleted.  If you don't have permission to delete the responses for another course you will not be able to delete this survey.");
            MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start); 
            return result;
        }
	}

	@Override
	public Class<DeleteSurvey> getActionType() {
		return DeleteSurvey.class;
	}

	@Override
	public void rollback(DeleteSurvey action, DeleteSurveyContextResult result, ExecutionContext context)
			throws DispatchException {
		// Nothing to do
	}
}
