/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchFilesByExtension;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchFilesByExtensionResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ActionHandler for actions of type FetchAvatars.
 */ 
public class FetchFilesByExtensionHandler implements ActionHandler<FetchFilesByExtension, FetchFilesByExtensionResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchFilesByExtensionHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
	public Class<FetchFilesByExtension> getActionType() {
        return FetchFilesByExtension.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized FetchFilesByExtensionResult execute( FetchFilesByExtension action, ExecutionContext context ) 
            throws ActionException {
        long start = System.currentTimeMillis();
        logger.info("execute()");
        
		Map<String, List<String>> extensionToFiles = new HashMap<String, List<String>>();
		List<String> extensions = action.getExtensions();
		String userName = action.getUserName();
		
        FetchFilesByExtensionResult result = new FetchFilesByExtensionResult();
		try{
		
    		for(String extension : extensions){
     			
    			FileTreeModel rootDirectory = ServicesManager.getInstance().getFileServices().getFileTree(userName, extension);
    			
    			extensionToFiles.put(extension, rootDirectory.getFileNamesUnderModel());
    		}    		

            result.setFiles(extensionToFiles);
    		
        }catch(Exception e){
            logger.error("Caught exception while trying to get all files with extensions of "+extensions+"'.", e);
            result.setSuccess(false);
            result.setErrorMsg("Could not get files with extensions of '" + extensions + "'.");
        }        
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchFilesByExtension", start);
        return result;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback( FetchFilesByExtension action, FetchFilesByExtensionResult result, ExecutionContext context ) 
            throws ActionException {

    }
}
