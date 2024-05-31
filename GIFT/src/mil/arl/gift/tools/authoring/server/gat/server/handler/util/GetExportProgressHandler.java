/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportProgress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgressResult;
import mil.arl.gift.tools.services.file.ExportManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * A handler that gets the progress of an ongoing export task
 * 
 * @author bzahid
 */
public class GetExportProgressHandler implements ActionHandler<GetExportProgress, GetProgressResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GetExportProgressHandler.class);

	@Override
	public GetProgressResult execute(GetExportProgress action,
			ExecutionContext context) throws DispatchException {
	    long start = System.currentTimeMillis();
		logger.debug("execute() with action: " + action);
		
		GetProgressResult result = new GetProgressResult(); 
		String username = action.getUserName();
		
		try {
			logger.trace("getImportProgress called with user: " + username + ".");
        	
        	ProgressIndicator progress = ExportManager.getInstance().getExportProgress(username);
        	
        	if(progress != null){
        		result.setSuccess(true);
        		result.setProgress(progress);
        		
        	} else {
        		result.setSuccess(false);
        		result.setErrorMsg("Could not find any exports in progress for this user.");
        	}
			
		} catch(Exception e) {
			logger.error("GetExportProgressHandler - Caught exception while getting export progress for " + username + ".", e);
            result.setSuccess(false);
            result.setErrorMsg("Could not get import progress for " + username + ". " + e.getMessage());
		}
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.GetExportProgress", start);
		return result;
	}

	@Override
	public Class<GetExportProgress> getActionType() {
		return GetExportProgress.class;
	}

	@Override
	public void rollback(GetExportProgress action, GetProgressResult result,
			ExecutionContext context) throws DispatchException {
	}

}
