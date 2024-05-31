/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchCourseHistory;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchCourseHistoryResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Retrieve the course history information for the course specified.
 * 
 * @author mhoffman
 *
 */
public class FetchCourseHistoryHandler implements ActionHandler<FetchCourseHistory, FetchCourseHistoryResult> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchCourseHistoryHandler.class);
    
    private static final String METRICS_TAG = "course.FetchCourseHistory";
    
	@Override
	public FetchCourseHistoryResult execute(FetchCourseHistory action,
			ExecutionContext arg1) throws DispatchException {
	    
	    long start = System.currentTimeMillis();
		AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
		DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
	    FetchCourseHistoryResult result = new FetchCourseHistoryResult();

	    if(action.getCourseFolderPath() != null){
    	    try {
    
    	        Date lastModifiedDate = fileServices.getCourseFolderLastModified(action.getCourseFolderPath(), action.getUserName());
    	        if(lastModifiedDate != null){
    	            result.setCourseFolderLastModifiedDate(lastModifiedDate);
    	            result.setSuccess(true);
    	        }
    		    
    	    } catch (Exception e) {
    	    	logger.warn("Unable to retrieve the course folder last modified date", e);
    	    	result.setErrorMsg("Failed to retrieve the course folder last modified date for '"+action.getCourseFolderPath()+"'.");
    	    	result.setErrorDetails("An exception was thrown while retrieving the last modified date.  The error reads:\n"+e.getMessage());
    	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
    	    	result.setSuccess(false);
    	    }    	    
	    }
	    
	    if(action.getSurveyContextId() != null){
	        
	        try{
	            
	            Date lastModifiedDate = dbServices.getSurveyContextLastModifiedDate(action.getSurveyContextId(), action.getUserName());
	            if(lastModifiedDate != null){
	                result.setSurveyContextLastModifiedDate(lastModifiedDate);
	                result.setSuccess(true);
	            }
            } catch (Exception e) {
                logger.warn("Unable to retrieve the survey context last modified date", e);
                result.setErrorMsg("Failed to retrieve the survey context last modified date for '"+action.getCourseFolderPath()+"'.");
                result.setErrorDetails("An exception was thrown while retrieving the last modified date.  The error reads:\n"+e.getMessage());
                result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                result.setSuccess(false);
            } 
	    }

	    
	    if(logger.isDebugEnabled()){
	        logger.debug("FetchContentAddressHandler result = " + result);
	    }
	    
	    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
	    
		return result;
	}

	@Override
	public Class<FetchCourseHistory> getActionType() {
		return FetchCourseHistory.class;
	}

	@Override
	public void rollback(FetchCourseHistory arg0,
			FetchCourseHistoryResult arg1, ExecutionContext arg2)
			throws DispatchException {}

}
