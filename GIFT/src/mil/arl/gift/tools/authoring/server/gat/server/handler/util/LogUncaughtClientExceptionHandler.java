/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.LogUncaughtClientException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.LogUncaughtClientExceptionResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LogUncaughtClientExceptionHandler.
 */
public class LogUncaughtClientExceptionHandler implements ActionHandler<LogUncaughtClientException, LogUncaughtClientExceptionResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(LogUncaughtClientExceptionHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public LogUncaughtClientExceptionResult execute(
			LogUncaughtClientException action, ExecutionContext ctx)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    logger.info("execute()");
	    
	    String logEntry = action.getLogEntry();
	    logger.warn(logEntry);
	    MetricsSenderSingleton.getInstance().endTrackingRpc("util.LogUncaughtClientException", start);
		return new LogUncaughtClientExceptionResult();
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<LogUncaughtClientException> getActionType() {
		return LogUncaughtClientException.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(LogUncaughtClientException arg0,
			LogUncaughtClientExceptionResult arg1, ExecutionContext arg2)
			throws DispatchException {		
	}

}
