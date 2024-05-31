/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModelResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ActionHandler for actions of type GetJAXBObject
 *
 */ 
public class FetchRootDirectoryModelHandler implements ActionHandler<FetchRootDirectoryModel, FetchRootDirectoryModelResult> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchRootDirectoryModelHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchRootDirectoryModel> getActionType() {
        return FetchRootDirectoryModel.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public FetchRootDirectoryModelResult execute(FetchRootDirectoryModel action, ExecutionContext context ) 
            throws ActionException {
        long start = System.currentTimeMillis();
        logger.debug("execute()");
        
        FetchRootDirectoryModelResult result = new FetchRootDirectoryModelResult();
        
        try {

    		String userName = action.getUserName();
        	String[] extensionsToInclude = action.getExtensionsToInclude();
        	
        	if(extensionsToInclude == null || extensionsToInclude.length == 0){
        		
        		//assume we want to use the root folder if no extensions are given
        		result.setDirectoryModel(ServicesManager.getInstance().getFileServices().getRootTree(userName));
        		
        	} else {
        		
        		Set<FileType> fileTypes = new HashSet<FileType>();
        		
        		boolean hasNonSchemaFileType = false;
        		
        		for(String extension : extensionsToInclude){
        			
        			try{
        				
        				fileTypes.add(AbstractSchemaHandler.getFileType(extension));
        				
        			} catch(@SuppressWarnings("unused") IllegalArgumentException e){
        				
        				hasNonSchemaFileType = true;     				
        			}
        		}
        		
        		if(fileTypes.size() > 1 || hasNonSchemaFileType){
        			
        			//assume we want to use the root folder if multiple extensions are given or if an extension with 
        			//no schema (e.g .html, .ogg, etc.) is included
            		result.setDirectoryModel(ServicesManager.getInstance().getFileServices().getFileTree(userName, extensionsToInclude));
        		
        		} else {
        			
        			FileType fileType = (FileType) fileTypes.toArray()[0];
        			
        			result.setDirectoryModel(ServicesManager.getInstance().getFileServices().getFileTree(userName, fileType));
        		}
        	}	
        	
            result.setSuccess(true);
        
        } catch (Exception e) {
        
            result.setSuccess(false);
            result.setErrorMsg(e.getLocalizedMessage());
        }
        
        MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchRootDirectoryModel", start);
                
        return result;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback(FetchRootDirectoryModel action, FetchRootDirectoryModelResult result, ExecutionContext context ) 
            throws ActionException {

    }
}
