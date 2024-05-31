/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFileExists;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handles the execution of the WorkspaceFileExists action. Checks for the existence of a file path relative
 * to the workspace folder and returns success based on whether or not the file exists.
 * @author tflowers
 *
 */
public class WorkspaceFileExistsHandler implements ActionHandler<WorkspaceFileExists, GatServiceResult> {

    private static final String METRICS_TAG = "course.WorkspaceFileExists";
    
    @Override
    public GatServiceResult execute(WorkspaceFileExists action, ExecutionContext context) throws DispatchException {
        
        long start = System.currentTimeMillis();
        
        GatServiceResult result = new GatServiceResult();
        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        
        //Gets the desired file to check for
        FileTreeModel root = fileServices.getRootTree(action.getUsername());
        root.detatchFromParent();
        FileTreeModel file = root.getModelFromRelativePath(action.getFilePath(), false);
        
        //Checks for the files existence
        if(file != null && !file.isDirectory()) {
            result.setSuccess(true);
        } else {
            String filePath = root.getRelativePathFromRoot() + "/" + action.getFilePath();
            result.setSuccess(false);
            
            if(file == null) {
                result.setErrorMsg("The file '" + filePath + "' does not exist.");
                result.setErrorDetails(filePath + " doesn't exist.");
            } else {
                result.setErrorMsg("The file '" + filePath + "' is a directory. The URI cannot reference a directory.");
                result.setErrorDetails(filePath + "' is a directory.\nfile.isDirectory() = " + file.isDirectory());
            }
        }
        
        MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
        
        return result;
    }

    @Override
    public Class<WorkspaceFileExists> getActionType() {
        return WorkspaceFileExists.class;
    }

    @Override
    public void rollback(WorkspaceFileExists action, GatServiceResult result, ExecutionContext context) throws DispatchException {
        // TODO Auto-generated method stub
        
    }

}
