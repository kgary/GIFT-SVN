/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.ArrayList;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.common.util.CourseUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchInteropImplementations;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchInteropImplementationsResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FetchInteropImplementationsHandler.
 */
public class FetchInteropImplementationsHandler implements ActionHandler<FetchInteropImplementations, FetchInteropImplementationsResult> {
    
    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchInteropImplementationsHandler.class);

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public FetchInteropImplementationsResult execute(
			FetchInteropImplementations action, ExecutionContext ctx)
					throws DispatchException {
	    long start = System.currentTimeMillis();
	    
        FetchInteropImplementationsResult result = new FetchInteropImplementationsResult();

	    //Get the list of implementations.
	    ArrayList<String> interopImplementations = null;
	    try {			
	    	interopImplementations = CourseUtil.getInteropImplementations();
		} catch ( DetailedException e ) {
			logger.error("Caught exception while fetching interop implementations.", e);		
			result.setSuccess(false);
			result.setErrorMsg(e.getReason());
			result.setErrorDetails(e.getDetails());
			result.setErrorStackTrace(e.getErrorStackTrace());
		} catch ( Throwable t ) {
			logger.error("Caught throwable while fetching interop implementations.", t);
			result.setSuccess(false);
			result.setErrorMsg(t.getLocalizedMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(t));
		}	

	    result.setInteropImplementations(interopImplementations);
	    
	    MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchInteropImplementations", start);
	    return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<FetchInteropImplementations> getActionType() {
		return FetchInteropImplementations.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(FetchInteropImplementations arg0,
			FetchInteropImplementationsResult arg1, ExecutionContext arg2)
			throws DispatchException {
		// TODO Auto-generated method stub		
	}
}
