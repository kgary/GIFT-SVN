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
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CancelExport;
import mil.arl.gift.tools.services.file.ExportManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * A handler that cancels an ongoing export task
 * 
 * @author bzahid
 */
public class CancelExportHandler  implements ActionHandler<CancelExport, GatServiceResult> {

	   /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(CancelExportHandler.class);
	
	@Override
	public GatServiceResult execute(CancelExport action, ExecutionContext context)
			throws DispatchException {
		
	    long start = System.currentTimeMillis();
		logger.debug("execute() with action: " + action);
		GatServiceResult result = new GatServiceResult();		
		String username = action.getUserName();
		
		try {
			
			logger.trace("CancelExport called with user: " + username);
			ExportManager.getInstance().cancelExport(username);
			result.setSuccess(true);
			
		} catch (Exception e) {
			
			logger.error("CancelExport - caught exception while trying to cancel"
					+ " course export for user " + username, e);
			
			result.setSuccess(false);
			result.setErrorMsg("Could not get export progress for " + username);
			
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.CancelExport", start);
		return result;
	}

	 /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
	@Override
	public Class<CancelExport> getActionType() {
		return CancelExport.class;
	}

	@Override
	public void rollback(CancelExport action, GatServiceResult result,
			ExecutionContext context) throws DispatchException {
	}

}
