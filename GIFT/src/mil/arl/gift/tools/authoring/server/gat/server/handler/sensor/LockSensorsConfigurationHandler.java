/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.CourseFileAccessDetails;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.sensor.LockSensorsConfiguration;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handles request to lock a sensors configuration so other users can't edit it.
 */
public class LockSensorsConfigurationHandler implements ActionHandler<LockSensorsConfiguration, LockFileResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(LockSensorsConfigurationHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public LockFileResult execute(LockSensorsConfiguration action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    logger.info("execute()");
	    
		String userName = action.getUserName();
		String browserSessionKey = action.getBrowserSessionKey();
		String relativePath = action.getRelativePath();
	    
        LockFileResult result = new LockFileResult();
        
        try{            
            CourseFileAccessDetails details = ServicesManager.getInstance().getFileServices().lockFile(userName,
                    browserSessionKey, relativePath, false);

            result.setCourseFileAccessDetails(details);
            result.setSuccess(true);

	    }catch (Exception e){
	        logger.error("Caught exception while trying to lock file '"+relativePath+"'.", e);
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while attempting to lock'" + relativePath + "'");
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    	return result;
	    }
	    MetricsSenderSingleton.getInstance().endTrackingRpc("sensor.LockSensorsConfiguration", start);
        return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<LockSensorsConfiguration> getActionType() {
		return LockSensorsConfiguration.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(LockSensorsConfiguration lockSensorsConfiguration, LockFileResult result,
			ExecutionContext context) throws DispatchException {
		// TODO Auto-generated method stub
	}
}
