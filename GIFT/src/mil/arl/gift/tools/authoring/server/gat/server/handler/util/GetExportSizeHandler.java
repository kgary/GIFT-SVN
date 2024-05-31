/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportSize;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportSizeResult;
import mil.arl.gift.tools.services.file.ExportManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

public class GetExportSizeHandler implements ActionHandler<GetExportSize, GetExportSizeResult> {

	 /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GetExportSizeHandler.class);
	
	@Override
	public GetExportSizeResult execute(GetExportSize action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();		
		GetExportSizeResult result = new GetExportSizeResult();
		
		String username = action.getUserName();
		List<DomainOption> selectedCourses = action.getSelectedDomainOptions();
		
		if(selectedCourses == null || selectedCourses.isEmpty()) {
			throw new IllegalArgumentException("The selected courses list cannot be null or empty.");
		}
		
		try {
			logger.trace("GetExportSize called for user: " + username);
						
			result.setExportSize(ExportManager.getInstance().getExportSize(username, selectedCourses));
			result.setSuccess(true);
			
		} catch(Exception e) {
			logger.error("GetExportSizeHandler - Caught exception while trying to retrieve the export size.", e);
			
			result.setSuccess(false);
			result.setErrorMsg("Caught exception while trying to calculate the export size: " + e.getMessage());

		}
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.GetExportSize", start);
		return result;
	}

	@Override
	public Class<GetExportSize> getActionType() {
		return GetExportSize.class;
	}

	@Override
	public void rollback(GetExportSize action, GetExportSizeResult result,
			ExecutionContext context) throws DispatchException {
	}

}
