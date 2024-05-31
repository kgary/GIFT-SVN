/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ExportCourses;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ExportCoursesResult;
import mil.arl.gift.tools.services.file.ExportManager;

/**
 * A handler that begins a task to export courses
 * 
 * @author bzahid
 */
public class ExportCoursesHandler implements ActionHandler<ExportCourses, ExportCoursesResult> {

	 /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(ExportCoursesHandler.class);

	@Override
	public ExportCoursesResult execute(ExportCourses action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();
		logger.debug("execute() wuth action: " + action);
		ExportCoursesResult result = new ExportCoursesResult();
		
		String username = action.getUserName();
		List<DomainOption> selectedCourses = action.getSelectedDomainOptions();
			
		if(selectedCourses == null || selectedCourses.isEmpty()) {
			throw new IllegalArgumentException("The selected courses list cannot be null or empty.");
		}
		
		try {
			logger.trace("ExportCourses called for user: " + username);
						
			DownloadableFileRef exportResult = ExportManager.getInstance().export(username, selectedCourses);
			
			result.setLocationOnServer(exportResult.getLocationOnServer());
			result.setDownloadUrl(exportResult.getDownloadUrl());
			result.setSuccess(true);
		
		} catch(DetailedException de){
			result.setSuccess(false);
			result.setErrorMsg(de.getReason());
			result.setErrorDetails(de.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(de));
			
		} catch(Exception e) {
			logger.error("ExportCoursesHandler - Caught exception while trying to export courses.", e);
			
				result.setSuccess(false);
				result.setErrorMsg(e.getMessage());
				result.setErrorDetails(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
				result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
		}
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.ExportCourses", start);
		return result;
	}

	@Override
	public Class<ExportCourses> getActionType() {
		return ExportCourses.class;
	}

	@Override
	public void rollback(ExportCourses action, ExportCoursesResult result,
			ExecutionContext context) throws DispatchException {
	}
    
    
}
