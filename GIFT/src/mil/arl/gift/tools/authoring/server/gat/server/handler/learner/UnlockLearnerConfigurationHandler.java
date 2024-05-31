/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.learner;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.UnlockLearnerConfiguration;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles request to unlock the LearnerConfiguration so other users can edit it.
 */
public class UnlockLearnerConfigurationHandler implements ActionHandler<UnlockLearnerConfiguration, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(UnlockLearnerConfigurationHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GatServiceResult execute(UnlockLearnerConfiguration action, ExecutionContext context)
			throws DispatchException {

	    long start = System.currentTimeMillis();
	    
	    if(logger.isDebugEnabled()){
	        logger.info("Attempting to unlock learner configuration file : "+action);
	    }
	    
	    GatServiceResult result = new GatServiceResult();
	    
	    String relativePath = action.getRelativePath();
		String userName = action.getUserName();
		String browserSessionKey = action.getBrowserSessionKey();

	    try {
	    	ServicesManager.getInstance().getFileServices().unlockFile(userName, browserSessionKey, relativePath);
	    } catch (Exception e){
	        logger.error("Caught exception while trying to unlock file '"+relativePath+"'.", e);
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while attempting to unlock '" + relativePath + "'.");
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    	return result;
	    }
        
	    MetricsSenderSingleton.getInstance().endTrackingRpc("learner.UnlockLearnerConfiguration", start);
        return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<UnlockLearnerConfiguration> getActionType() {
		return UnlockLearnerConfiguration.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(UnlockLearnerConfiguration unlockLearnerConfiguration, GatServiceResult result,
			ExecutionContext context) throws DispatchException {
		// TODO Auto-generated method stub
	}
}
