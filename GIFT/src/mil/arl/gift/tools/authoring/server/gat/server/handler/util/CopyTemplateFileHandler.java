/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.io.File;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyTemplateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;


/**
 * A handler to copy template files to the specified course folder
 * 
 * @author nroberts
 *
 */
public class CopyTemplateFileHandler implements ActionHandler<CopyTemplateFile, CopyWorkspaceFilesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(CopyTemplateFileHandler.class);
    
    /** The name of the templates folder */
    private static final String TEMPLATE_DIR = CommonProperties.WORKSPACE + File.separator + "templates";
    
    private static final String COPY_TEMPLATE_METRIC = "util.CopyTemplateFile";
    
    /** The name of the dkf file to use as a template */
    private static final String DKF_TEMPLATE = "simplest.dkf.xml";
    
    /** the name of the default conversation tree to use */
    private static final String CONVERSATION_TREE_TEMPLATE = "default.conversationTree.xml";
    
    @Override
	public Class<CopyTemplateFile> getActionType() {
        return CopyTemplateFile.class;
    }


    @Override
    public CopyWorkspaceFilesResult execute(CopyTemplateFile action, ExecutionContext context)
            throws ActionException {
        long start = System.currentTimeMillis();
        
        if(logger.isDebugEnabled()){
            logger.debug("execute() with action: " + action);
        }
        
        CopyWorkspaceFilesResult result = new CopyWorkspaceFilesResult();
        
        try{
            if(action.getFileExtension().equals(AbstractSchemaHandler.DKF_FILE_EXTENSION)) {
            
            	// Get the path to the template file
            	String templatePath = TEMPLATE_DIR + File.separator + DKF_TEMPLATE;
            	
            	// Get the target file path with a UUID
            	StringBuilder sb = new StringBuilder(action.getTargetName());
            	if (action.isAppendUUIDToFilename()) {
            	    sb.append("_").append(UUID.randomUUID());
            	}
            	sb.append(action.getFileExtension());
            	String fileName = action.getCourseFolderName() + File.separator + sb.toString();        	
            	
            	// Copy the template
            	try{
            	    String copiedPath = ServicesManager.getInstance().getFileServices().copyDomainFile(action.getUsername(), templatePath, fileName, NameCollisionResolutionBehavior.FAIL_ON_COLLISION, null);
            	    FileTreeModel copiedModel = FileTreeModel.createFromRawPath(copiedPath);
            	    
            	    result.setSuccess(true);
            	    result.addCopiedFile(copiedModel);

            	}catch(Exception e){
                    result.setErrorMsg("Failed to copy the dkf template file due to a server error.");
                    result.setErrorDetails("The dkf template file '"+templatePath+"' could not be copied to '"+fileName+"'.");
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                    result.setSuccess(false);
            	} 
            }else if(action.getFileExtension().equals(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)) {
                
                // Get the path to the template file
                String templatePath = TEMPLATE_DIR + File.separator + CONVERSATION_TREE_TEMPLATE;
                
                // Get the target file path with a UUID
                String fileName = action.getTargetName() + "_" + UUID.randomUUID() + action.getFileExtension();
                fileName = action.getCourseFolderName() + File.separator + fileName;            
                
                // Copy the template
                try{
                    String copiedPath = ServicesManager.getInstance().getFileServices().copyDomainFile(action.getUsername(), templatePath, fileName, NameCollisionResolutionBehavior.FAIL_ON_COLLISION, null);
                    FileTreeModel copiedModel = FileTreeModel.createFromRawPath(copiedPath);
                    
                    result.setSuccess(true);
                    result.addCopiedFile(copiedModel);

                }catch(Exception e){
                    result.setErrorMsg("Failed to copy the conversation tree template file due to a server error.");
                    result.setErrorDetails("The conversation tree template file '"+templatePath+"' could not be copied to '"+fileName+"'.");
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                    result.setSuccess(false);
                } 
            }else{
                result.setSuccess(false);
                result.setErrorMsg("Failed to copy the template file due to a server error.");
                result.setErrorDetails("The template file extension provided is not supported: "+action);
            }
        }catch(Exception e){
            result.setErrorMsg("Failed to copy the template file due to a server error.");
            result.setErrorDetails("The template file trying to be copied was based on "+action);
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            result.setSuccess(false);
        }
        
		MetricsSenderSingleton.getInstance().endTrackingRpc(COPY_TEMPLATE_METRIC, start);
		return result;       
    }

    @Override
    public void rollback(CopyTemplateFile action, CopyWorkspaceFilesResult result,
            ExecutionContext context) throws ActionException {
    }
}
