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
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteExportFile;
import mil.arl.gift.tools.services.file.ExportManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * An action handler that deletes an exported file.
 * 
 * @author bzahid
 */
public class DeleteExportFileHandler implements ActionHandler<DeleteExportFile, GatServiceResult> {

	/**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(DeleteExportFileHandler.class);
	
	@Override
	public GatServiceResult execute(DeleteExportFile action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();		
		logger.debug("execute() with action: " + action);
		
		GatServiceResult result = new GatServiceResult();
		
		logger.trace("deleteExportFile() called for user: " + action.getUserName());
		
		result.setSuccess(ExportManager.getInstance().deleteExportFile(new DownloadableFileRef(action.getDownloadUrl(), action.getLocationOnServer())));
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.DeleteExportFile", start);	
		return result;		
	}

	@Override
	public Class<DeleteExportFile> getActionType() {
		return DeleteExportFile.class;
	}

	@Override
	public void rollback(DeleteExportFile action, GatServiceResult result,
			ExecutionContext context) throws DispatchException {
	}

}
