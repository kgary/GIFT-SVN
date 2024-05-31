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

import mil.arl.gift.common.course.CourseFileAccessDetails;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockCourse;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handles request to lock the course so other users can't edit it.
 */
public class LockCourseHandler implements ActionHandler<LockCourse, LockFileResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(LockCourseHandler.class);
    
    /**
     * The key that identifies the metric for the time taken to lock a course on
     * the server
     */
    private static final String LOCK_COURSE_METRIC = "course.LockCourse";
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public LockFileResult execute(LockCourse action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    
	    if(logger.isInfoEnabled()){
	        logger.info("Attempting to lock course :" +action);
	    }
		
		String userName = action.getUserName();
		String browserSessionKey = action.getBrowserSessionKey();
		String relativePath = action.getPath();
        boolean initialAcquisition = action.getInitialAcquistion();
	    
		LockFileResult result = new LockFileResult();
	    
	    try{	    	
            CourseFileAccessDetails details = ServicesManager.getInstance().getFileServices().lockFile(userName,
                    browserSessionKey, relativePath, initialAcquisition);

            result.setCourseFileAccessDetails(details);
            result.setSuccess(true);

        } catch(Exception e){
            logger.error("Caught exception while trying to lock file '"+relativePath+"'.", e);
        	String msg = "An error occurred while trying to lock course file: " + relativePath;

        	result.setErrorMsg(msg);
            result.setErrorDetails("The error is "+ e.getMessage() != null ? e.getMessage() : e.toString());
        	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        	result.setSuccess(false);
        }
	    
	    MetricsSenderSingleton.getInstance().endTrackingRpc(LOCK_COURSE_METRIC, start);
	    
        return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<LockCourse> getActionType() {
		return LockCourse.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(LockCourse lockCourse, LockFileResult result,
			ExecutionContext context) throws DispatchException {

	}
}
