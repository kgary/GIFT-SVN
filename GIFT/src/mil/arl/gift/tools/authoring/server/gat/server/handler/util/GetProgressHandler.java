/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.authoring.server.gat.server.FileOperationsManager;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgressResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler that retrieves the progress of an ongoing delete task.
 * 
 * @author bzahid
 */
public class GetProgressHandler implements ActionHandler<GetProgress, GetProgressResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GetProgressHandler.class);

    @Override
    public GetProgressResult execute(GetProgress action, ExecutionContext context) throws ActionException {
    	
        long start = System.currentTimeMillis();
        logger.debug("execute() with action: " + action);
        
        ProgressIndicator progress;
        GetProgressResult result = new GetProgressResult();
        String username = action.getUserName();
        
        try{
            logger.trace("getProgress called with user: " + username + ".");
        	
            progress = FileOperationsManager.getInstance().getProgress(action.getTask(), username);
            
        	if(progress != null){
        		result.setSuccess(true);
        		result.setProgress(progress);
        		
        	} else {
        		result.setSuccess(false);
        		result.setErrorMsg("Could not find any file operations in progress for this user.");
        	}
            
        } catch (Exception e) {

            logger.error("GetProgressHandler - Caught exception while getting progress for " + username + ".", e);
            result.setSuccess(false);
            result.setErrorMsg("Could not get progress for " + username + ". " + e.getMessage());
        }
       
        MetricsSenderSingleton.getInstance().endTrackingRpc("util.GetProgress", start);
        return result;
       
    }
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
	public Class<GetProgress> getActionType() {
        return GetProgress.class;
    }

    @Override
    public void rollback(GetProgress action, GetProgressResult result,
            ExecutionContext context) throws ActionException {
        
        
    }
}
