/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CreateWorkspaceFolder;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An action handler that creates a folder in the workspace folder
 */ 
public class CreateWorkspaceFolderHandler implements ActionHandler<CreateWorkspaceFolder, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(CreateWorkspaceFolderHandler.class);
    
    @Override
	public Class<CreateWorkspaceFolder> getActionType() {
        return CreateWorkspaceFolder.class;
    }
    
    @Override
    public synchronized GatServiceResult execute( CreateWorkspaceFolder action, ExecutionContext context ) 
            throws ActionException {
        long start = System.currentTimeMillis();
        logger.info("execute()");
		
        GatServiceResult result = new GatServiceResult();     
        
        String username = action.getUsername();
	    String parentWorkspaceLocationPath = action.getParentWorkspaceLocation();
	    String folderName = action.getFolderName();
	    boolean ignoreExistingFolder = action.ignoreExistingFolder();
        
		try{
		    
		    //move the domain file to the target workspace location
		    ServicesManager.getInstance().getFileServices().createFolder(username, parentWorkspaceLocationPath, folderName, ignoreExistingFolder);

		    result.setSuccess(true);		    
    		
        }catch(Exception e){
        	
            logger.error("Caught exception while trying to create a folder named '" + folderName + "' for user named '" + username + "' at " + parentWorkspaceLocationPath + ".", e);
            result.setSuccess(false);
            result.setErrorMsg("Could not create folder '" + folderName + "' at " + parentWorkspaceLocationPath + ". " + e.getMessage() != null ? e.getMessage() : e.toString());
        }        
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.CreateWorkspaceFolder", start);
        return result;
    }
    
    @Override
    public synchronized void rollback( CreateWorkspaceFolder action, GatServiceResult result, ExecutionContext context ) 
            throws ActionException {

    }
}
