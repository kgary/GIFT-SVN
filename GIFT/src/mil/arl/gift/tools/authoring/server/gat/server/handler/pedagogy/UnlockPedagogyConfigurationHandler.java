/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.pedagogy;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy.UnlockPedagogyConfiguration;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles request to unlock the PedagogyConfiguration so other users can edit it.
 */
public class UnlockPedagogyConfigurationHandler implements ActionHandler<UnlockPedagogyConfiguration, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(UnlockPedagogyConfigurationHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GatServiceResult execute(UnlockPedagogyConfiguration action, ExecutionContext context)
			throws DispatchException {

	    long start = System.currentTimeMillis();
	    
	    if(logger.isInfoEnabled()){
	        logger.info("Attempting to unlock pedagogy configuration file : "+action);
	    }
	    
	    GatServiceResult result = new GatServiceResult();
	    
	    String relativePath = action.getRelativePath();
		String userName = action.getUserName();
		String browserSessionKey = action.getBrowserSessionKey();

	    try {
	    	ServicesManager.getInstance().getFileServices().unlockFile(userName, browserSessionKey, relativePath);
	    } catch (Exception e){
	        logger.error("Caught exception while trying to lock file '"+relativePath+"'.", e);
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while attempting to unlock '" + relativePath + "'.");
	    	return result;
	    }
        
	    MetricsSenderSingleton.getInstance().endTrackingRpc("ped.UnlockPedagogyConfiguration", start);
        return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<UnlockPedagogyConfiguration> getActionType() {
		return UnlockPedagogyConfiguration.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(UnlockPedagogyConfiguration unlockPedagogyConfiguration, GatServiceResult result,
			ExecutionContext context) throws DispatchException {
		// TODO Auto-generated method stub
	}
}
