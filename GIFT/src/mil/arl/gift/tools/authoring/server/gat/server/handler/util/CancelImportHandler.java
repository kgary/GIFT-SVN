/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CancelImport;
import mil.arl.gift.tools.services.file.ImportManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A handler that cancels an ongoing import task
 * 
 * @author nroberts
 */
public class CancelImportHandler implements ActionHandler<CancelImport, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(CancelImportHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
	public Class<CancelImport> getActionType() {
        return CancelImport.class;
    }


    @Override
    public GatServiceResult execute(CancelImport action, ExecutionContext context)
            throws ActionException {
        long start = System.currentTimeMillis();
        logger.debug("execute() with action: " + action);
        GatServiceResult result = new GatServiceResult();
        
        String username = action.getUserName();
        
        try{
        	logger.trace("CancelImport called with user: " + username + ".");
        	
        	ImportManager.getInstance().cancelImport(username);
        	
        	result.setSuccess(true);
            
        } catch (Exception e) {

            logger.error("CancelImport - Caught exception while trying to cancel import for user" + username + ".", e);
            result.setSuccess(false);
            result.setErrorMsg("Could not get import progress for " + username + ".");
        }
       
        MetricsSenderSingleton.getInstance().endTrackingRpc("util.CancelImport", start);
        return result;
       
    }

    @Override
    public void rollback(CancelImport action, GatServiceResult result,
            ExecutionContext context) throws ActionException {
        
        
    }
}
