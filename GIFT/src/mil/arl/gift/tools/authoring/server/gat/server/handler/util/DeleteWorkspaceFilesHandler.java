/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.server.FileOperationsManager;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The handler for deleting workspace files from the server
 * 
 * @author nroberts
 *
 */
public class DeleteWorkspaceFilesHandler implements ActionHandler<DeleteWorkspaceFiles, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(DeleteWorkspaceFilesHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
	public Class<DeleteWorkspaceFiles> getActionType() {
        return DeleteWorkspaceFiles.class;
    }


    @Override
    public GatServiceResult execute(DeleteWorkspaceFiles action, ExecutionContext context)
            throws ActionException {
    	
        long start = System.currentTimeMillis();
        logger.debug("execute() with action: " + action);
        
        GatServiceResult result = FileOperationsManager.getInstance().deleteWorkspaceFiles(action.getUsername(), action.getBrowserSessionKey(), action.getWorkspacePaths());       
        MetricsSenderSingleton.getInstance().endTrackingRpc("util.DeleteWorkspaceFiles", start);		
        
        return result;
       
    }

    @Override
    public void rollback(DeleteWorkspaceFiles action, GatServiceResult result,
            ExecutionContext context) throws ActionException {
        
        
    }
}
