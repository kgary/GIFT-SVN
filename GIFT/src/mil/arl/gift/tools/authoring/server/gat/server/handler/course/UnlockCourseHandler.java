/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnlockCourse;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles request to unlock the course so other users can edit it.
 */
public class UnlockCourseHandler implements ActionHandler<UnlockCourse, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(UnlockCourseHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GatServiceResult execute(UnlockCourse action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    
	    if(logger.isInfoEnabled()){
	        logger.info("Attempting to unlock course : "+action);
	    }
	    
	    String relativePath = action.getRelativePath();
	    String userName = action.getUserName();
	    String browserSessionKey = action.getBrowserSessionKey();
	    
	    GatServiceResult result = new GatServiceResult();
	    try {
	    	ServicesManager.getInstance().getFileServices().unlockFile(userName, browserSessionKey, relativePath);
	    } catch(Exception e){
	        logger.error("Caught exception while trying to unlock file '"+relativePath+"'.", e);
	        
        	String msg = "An error occurred while trying to unlock course file: " + relativePath;
            
        	result.setErrorMsg(msg);
        	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        	result.setSuccess(false);
        }
        
	    MetricsSenderSingleton.getInstance().endTrackingRpc("course.UnlockCourse", start);
        return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<UnlockCourse> getActionType() {
		return UnlockCourse.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(UnlockCourse unlockCourse, GatServiceResult result,
			ExecutionContext context) throws DispatchException {
		// TODO Auto-generated method stub
	}
}
