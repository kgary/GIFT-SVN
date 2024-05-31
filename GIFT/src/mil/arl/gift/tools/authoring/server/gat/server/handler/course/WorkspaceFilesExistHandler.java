/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.FilePath;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFilesExist;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.WorkspaceFilesExistResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handles the execution of the WorkspaceFilesExist action. Checks for the existence of a list of file paths relative
 * to the workspace folder and returns success based on whether or not the file exists.
 * @author cpadilla
 *
 */
public class WorkspaceFilesExistHandler implements ActionHandler<WorkspaceFilesExist, WorkspaceFilesExistResult> {
    
    private static final String METRICS_TAG = "course.WorkspaceFilesExist";
    private static final Logger logger = LoggerFactory.getLogger(WorkspaceFilesExistHandler.class);
    
    /**
     * Constructor
     */
    public WorkspaceFilesExistHandler() {
        super();
    }

    @Override
    public WorkspaceFilesExistResult execute(WorkspaceFilesExist action, ExecutionContext context) throws DispatchException {
        long start = System.currentTimeMillis();
        if (logger.isInfoEnabled()) {
            logger.info("WorkspaceFilesExistHandler execute(). Files: " + action.getFilePathList().size());
        }
        
        WorkspaceFilesExistResult result = new WorkspaceFilesExistResult();
        
        //Gets the desired file to check for
        List<FilePath> filePathList = action.getFilePathList();

        result.setSuccess(true);

        for (FilePath fPath : filePathList) {
        	
            boolean valid = false;

            try {

            	//verify that the path points to a valid file or external URI
                String userName = action.getUsername();
                AbstractFolderProxy courseDirectory = ServicesManager.getInstance().getFileServices().getCourseFolder(action.getRelativePath(), userName);

                UriUtil.validateUri(fPath.getFileName(), courseDirectory, UriUtil.getInternetStatus());
                
                valid = true;
                
            } catch (Exception e){
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to validate Uri: " + e.getClass() + "\n" + e.getMessage());
                }
                result.setSuccess(false);
                result.setErrorMsg(result.getErrorMsg() + "Failed to validate Uri '" + fPath.getFileName() + "'.\n");
                result.addFileResult(fPath, false, "Failed to validate Uri '" + fPath.getFileName() + "'.");
            }

            if(valid) {
                if (logger.isInfoEnabled()) {
                    logger.info("The uri " + fPath.getFileName() + "exists");
                }
                result.addFileResult(fPath, true);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("The file '" + fPath.getFileName() + "' is a directory.");
                }
                result.setSuccess(false);
                result.setErrorMsg(result.getErrorMsg() + "The file '" + fPath.getFileName() + "' is a directory. The URI cannot reference a directory.\n");
                result.addFileResult(fPath, false, "The file '" + fPath.getFileName() + "' is a directory. The URI cannot reference a directory.");
            }

        }
        
        MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);

        return result;
    }

    @Override
    public Class<WorkspaceFilesExist> getActionType() {
        return WorkspaceFilesExist.class;
    }

    @Override
    public void rollback(WorkspaceFilesExist arg0, WorkspaceFilesExistResult arg1, ExecutionContext arg2)
            throws DispatchException {
        
    }
    
    
}

