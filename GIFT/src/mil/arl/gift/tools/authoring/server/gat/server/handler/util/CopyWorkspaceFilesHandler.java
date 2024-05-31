/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.server.FileOperationsManager;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A handler for a set of workspace files to new locations
 * 
 * @author nroberts
 *
 */
public class CopyWorkspaceFilesHandler implements ActionHandler<CopyWorkspaceFiles, CopyWorkspaceFilesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(CopyWorkspaceFilesHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
	public Class<CopyWorkspaceFiles> getActionType() {
        return CopyWorkspaceFiles.class;
    }


    @Override
    public CopyWorkspaceFilesResult execute(CopyWorkspaceFiles action, ExecutionContext context)
            throws ActionException {
        long start = System.currentTimeMillis();
        logger.debug("execute() with action: " + action);
        
        if(action.shouldAppendTimestamp()) {
        	for(String key : action.getSourcePathsToTargetPaths().keySet()) {
        		// Append "- Copy" to the filename
        		
        		String targetPath = action.getSourcePathsToTargetPaths().get(key);
        		int extensionIndex = targetPath.lastIndexOf(".", targetPath.lastIndexOf(".") - 1);
        		String extension = targetPath.substring(extensionIndex);
        		
        		targetPath = targetPath.substring(0, extensionIndex) + " - Copy" + extension;
        		
        		AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        		FileTreeModel file = fileServices.getRootTree(action.getUsername()).getModelFromRelativePath(targetPath, false);
        		
        		if(file != null) {
        			// If a copy of this file already exists, append a timestamp to the filename
        			
        			String format = "MM-dd-yyyy_HH-mm-ss";
            		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        			String timestamp = dateFormat.format(new Date());
        			        			
        			if(key.contains(" - Copy ")) {
        				if(dateFormat.parse(key, new ParsePosition(extensionIndex - format.length())) != null) {
        					// Prevent naming the file "name - Copy Timestamp - Copy Timestamp"
        					extensionIndex -= format.length();
        				}
        				
        				targetPath = key.substring(0, extensionIndex) + timestamp + extension;
        				
        			} else {
        				
        				targetPath = key.substring(0, extensionIndex) + " - Copy " + timestamp + extension;
        			}
        			
        		}
        		
        		action.getSourcePathsToTargetPaths().put(key, targetPath);
        	}
        }
        
        CopyWorkspaceFilesResult result = FileOperationsManager.getInstance().copyWorkspaceFiles(
        		action.getUsername(), 
        		action.getBrowserSessionKey(),
        		action.getSourcePathsToTargetPaths(), 
        		action.isOverwriteExisting(), 
        		action.isCleanUpOnFailure());
		
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.CopyWorkspaceFiles", start);
		return result;
       
    }

    @Override
    public void rollback(CopyWorkspaceFiles action, CopyWorkspaceFilesResult result,
            ExecutionContext context) throws ActionException {
        
        
    }
}
