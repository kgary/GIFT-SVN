/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.List;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.UnlockWorkspaceFiles;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles request to unlock files so other users can edit them.
 */
public class UnlockWorkspaceFilesHandler implements ActionHandler<UnlockWorkspaceFiles, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(UnlockWorkspaceFilesHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GatServiceResult execute(UnlockWorkspaceFiles action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    
	    if(logger.isInfoEnabled()){
	        logger.info("Attempting to unlock multiple workspace files: "+action);
	    }
	    
		String userName = action.getUserName();
		String browserSessionKey = action.getBrowserSessionKey();
		
		List<String> pathsToUnlock = action.getPathsToUnlock();
	    
	    GatServiceResult result = new GatServiceResult();
	    
	    try{
	    	        
	        for(String path : pathsToUnlock){
		        
		        try {
			        ServicesManager.getInstance().getFileServices().unlockFile(userName, browserSessionKey, path);
		        } catch(@SuppressWarnings("unused") Exception e){
		        	
		        }
	        }
	        
	        result.setSuccess(true);
	        
	    } catch (Exception e){
	        logger.error("Caught exception while trying to lock files for user " + userName  + ".", e);
	        
	    	result.setSuccess(false);
	    	result.setErrorMsg(e.toString());
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    	return result;
	    }
	    MetricsSenderSingleton.getInstance().endTrackingRpc("util.UnlockWorkspaceFiles", start);
        return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<UnlockWorkspaceFiles> getActionType() {
		return UnlockWorkspaceFiles.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(UnlockWorkspaceFiles lockDkf, GatServiceResult result,
			ExecutionContext context) throws DispatchException {
		// TODO Auto-generated method stub
	}
}
