/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.util.ArrayList;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMetadataFilesForMerrillQuadrant;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.StringListResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class handles requests to get the list of metadata files for a particular content file for a Merrills quadrant in a course.
 * 
 * @author nroberts
 */
public class GetMetadataFilesForMerrillQuadrantHandler implements ActionHandler<GetMetadataFilesForMerrillQuadrant, StringListResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GetMetadataFilesForMerrillQuadrantHandler.class);

    @Override
	public Class<GetMetadataFilesForMerrillQuadrant> getActionType() {
        return GetMetadataFilesForMerrillQuadrant.class;
    }

    @Override
	public StringListResult execute(GetMetadataFilesForMerrillQuadrant action, ExecutionContext context) throws ActionException {
        
        long start = System.currentTimeMillis();
		String userName = action.getUsername();
    	StringListResult result = new StringListResult();
    	try{
    		FileTreeModel workspaceFileTree = ServicesManager.getInstance().getFileServices().getRootTree(userName);
    		
    		FileTreeModel contentFileTree = workspaceFileTree.getModelFromRelativePath(action.getContentFilePath());
    		
    		result.setStrings(ServicesManager.getInstance().getFileServices()
    			.getMetadataForContent(userName, contentFileTree, MerrillQuadrantEnum.valueOf(action.getQuadrantName())));
    		
    		result.setSuccess(true);

        } catch (Exception thrown){
        	
            logger.error("Caught exception while getting metadata files for " + action.getContentFilePath(), thrown);
            result.setSuccess(false);
            
            if(thrown instanceof DetailedException){
            	
            	DetailedException exception = (DetailedException) thrown;
            	
            	result.setErrorMsg(exception.getReason());
            	result.setErrorDetails(exception.getDetails());
            	result.setErrorStackTrace(exception.getErrorStackTrace());
            	
            } else {
            	
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
	public void rollback( GetMetadataFilesForMerrillQuadrant action, StringListResult result, ExecutionContext context ) throws ActionException {
    }
}