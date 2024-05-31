/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CourseFilesExist;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CourseFilesExistResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * ActionHandler for actions of type CourseFilesExist.
 */
public class CourseFilesExistHandler implements ActionHandler<CourseFilesExist, CourseFilesExistResult> {

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(CourseFilesExistHandler.class);

    @Override
    public Class<CourseFilesExist> getActionType() {
        return CourseFilesExist.class;
    }

    @Override
    public void rollback(CourseFilesExist action, CourseFilesExistResult result, ExecutionContext context)
            throws DispatchException {
    }

    @Override
    public synchronized CourseFilesExistResult execute(CourseFilesExist action, ExecutionContext context)
            throws ActionException {
        if (logger.isTraceEnabled()) {
            logger.trace("CourseFilesExistResult.execute()");
        }

        long start = System.currentTimeMillis();

        final String courseFolderPath = action.getCourseFolderPath();
        final String username = action.getUsername();

        CourseFilesExistResult result = new CourseFilesExistResult();
        try {
            final AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
            for (String fileToCheck : action.getCourseFiles()) {
                final String filePath = courseFolderPath + File.separator + fileToCheck;
                boolean exists = fileServices.fileExists(username, filePath, true)
                        || fileServices.fileExists(username, filePath, false);
                result.addFileResult(fileToCheck, exists);
            }
        } catch (Exception e) {
            logger.error("Caught exception while trying to see if the files exist within the course folder.", e);
            result.setSuccess(false);
            result.setErrorMsg("Failed checking the files to see if they exist within the course folder.");
        }

        MetricsSenderSingleton.getInstance().endTrackingRpc("util.CourseFilesExist", start);
        return result;
    }
}
