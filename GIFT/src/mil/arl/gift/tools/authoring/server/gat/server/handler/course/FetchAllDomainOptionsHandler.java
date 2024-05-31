/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchAllDomainOptions;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchAllDomainOptionsResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

public class FetchAllDomainOptionsHandler implements ActionHandler<FetchAllDomainOptions, FetchAllDomainOptionsResult> {

	private static Logger logger = LoggerFactory.getLogger(FetchAllDomainOptionsHandler.class);
	
	@Override
	public FetchAllDomainOptionsResult execute(FetchAllDomainOptions action,
			ExecutionContext context) throws DispatchException {
		
	    long start = System.currentTimeMillis();
		logger.debug("execute() with action: " + action);
		
		CourseOptionsWrapper options = new CourseOptionsWrapper();
		FetchAllDomainOptionsResult result = new FetchAllDomainOptionsResult();
		
		try {
			ServicesManager.getInstance().getFileServices().getCourses(action.getUserName(), options, false, null);
			result.setDomainOptionsList(options.domainOptions.values());
			result.setSuccess(true);
			
		} catch (Throwable e) {
			logger.error("Caught exception while getting domain options for user " + action.getUserName(), e);
			result.setErrorMsg("Caught exception while getting domain options: " + result.getErrorMsg());
			result.setSuccess(false);
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc("course.FetchAllDomainOptions", start);
		return result;
	}

	/* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
	@Override
	public Class<FetchAllDomainOptions> getActionType() {
		return FetchAllDomainOptions.class;
	}

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
	@Override
	public void rollback(FetchAllDomainOptions action, FetchAllDomainOptionsResult result,
			ExecutionContext context) throws DispatchException {
	}

}
