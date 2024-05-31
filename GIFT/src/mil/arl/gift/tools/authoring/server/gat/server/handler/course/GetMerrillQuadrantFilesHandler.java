/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.util.ArrayList;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.metadata.MetadataSearchResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMerrillQuadrantFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMerrillQuadrantFilesResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class handles requests to get the list of files for a particular Merrills quadrant in a course.
 * 
 * @author nroberts
 *
 */
public class GetMerrillQuadrantFilesHandler implements ActionHandler<GetMerrillQuadrantFiles, GetMerrillQuadrantFilesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GetMerrillQuadrantFilesHandler.class);

    @Override
	public Class<GetMerrillQuadrantFiles> getActionType() {
        return GetMerrillQuadrantFiles.class;
    }

    @Override
	public GetMerrillQuadrantFilesResult execute(GetMerrillQuadrantFiles action, ExecutionContext context) throws ActionException {

        long start = System.currentTimeMillis();
        String userName = action.getUsername();
    	GetMerrillQuadrantFilesResult result = new GetMerrillQuadrantFilesResult();
    	try{
    		
    		MetadataSearchResult searchResult = ServicesManager.getInstance().getFileServices().getMetadataContentFileTree(userName, action.getCourseFilePath(), action.getRequests());		
    		result.setSearchResult(searchResult);
    		
    		result.setSuccess(true);

        } catch (Exception thrown){
        	
            logger.error("Caught exception while getting Merrill quadrant files for " + action.getCourseFilePath(), thrown);
            result.setSuccess(false);
            
            if(thrown instanceof DetailedException){
            	
            	DetailedException exception = (DetailedException) thrown;
            	
            	result.setErrorMsg(exception.getReason());
            	result.setErrorDetails(exception.getDetails());
            	result.setErrorStackTrace(exception.getErrorStackTrace());
            	
            } else {
            	
	            result.setErrorMsg("An error occurred while attempting to get the list of content files.");
	            result.setErrorDetails(thrown.toString());
	            
	            ArrayList<String> stackTrace = new ArrayList<String>();
				
				if(thrown.getStackTrace() != null){
					for(StackTraceElement e : thrown.getStackTrace()){
						stackTrace.add(e.toString());
					}
				}
				
				result.setErrorStackTrace(stackTrace);
            }
        }

    	MetricsSenderSingleton.getInstance().endTrackingRpc("course.GetMerillQuadrantFiles", start);
    	
    	return result;
    }

    @Override
	public void rollback( GetMerrillQuadrantFiles action, GetMerrillQuadrantFilesResult result, ExecutionContext context ) throws ActionException {
    }
}