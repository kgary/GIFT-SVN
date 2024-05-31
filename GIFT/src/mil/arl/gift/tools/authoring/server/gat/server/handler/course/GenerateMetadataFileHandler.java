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

import generated.metadata.Metadata;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.domain.knowledge.metadata.MetadataSchemaHandler;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateMetadataFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateMetadataFileResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * The Class GenerateMetadataFileHandler.
 */
public class GenerateMetadataFileHandler implements ActionHandler<GenerateMetadataFile, GenerateMetadataFileResult>{

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GenerateMetadataFileHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GenerateMetadataFileResult execute(GenerateMetadataFile action, ExecutionContext ctx)
			throws DispatchException {
	    long start = System.currentTimeMillis();
		logger.info("execute()");
	    
	    //Update the version of the metadata.
	    Metadata metadata = action.getMetadata();
	    String currentVersion = metadata.getVersion();
  		String schemaVersion = CommonUtil.getSchemaVersion(AbstractSchemaHandler.METADATA_SCHEMA_FILE);
  		String newVersion = CommonUtil.generateVersionAttribute(currentVersion, schemaVersion);
  		metadata.setVersion(newVersion);
  		boolean copyToSubfolder = false;
  		
	    //If the Metadata references a file that does NOT live in the same
	    //folder that we're going to write the Metadata file then we have a
	    //problem. We'll have to copy the referenced file to that directory.
	    //Then we'll have to update the Metadata object to have a
	    //domain-relative path to the newly copied file.
    	String userName = action.getUserName();
	    String metadataFilePath = action.getTargetFilename();
        String simpleReferencePath = null;
        if(metadata.getContent() instanceof generated.metadata.Metadata.Simple){
            simpleReferencePath = ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue();
        }
        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
	    if(simpleReferencePath != null) {
	    	//Get a FileTreeModel object for the file we're copying and for the
	    	//directory we're copying to.
	        
	        FileTreeModel metadataDirectory, simpleReferenceFile;
	        try{
	        	
	            metadataDirectory = FileTreeModel.createFromRawPath(metadataFilePath).getParentTreeModel();
	            simpleReferenceFile = metadataDirectory.getModelFromRelativePath(simpleReferencePath);
            } catch (DetailedException e) {
                logger.error("Unable to get metadata file tree for '" + metadataFilePath, e);
                
                GenerateMetadataFileResult result = new GenerateMetadataFileResult();
                result.setSuccess(false);
                result.setErrorMsg("Unable to get metadata file tree for '" + metadataFilePath + "'. Reason: " + e.toString());
                return result;
            }
	        
	  		//If the metadata file and the referenced file aren't in the same
		    //directory then we'll take corrective action.
	  		String metadataDirectoryPath = metadataDirectory.getRelativePathFromRoot();
		    String simpleReferenceDirectoryPath = simpleReferenceFile.getParentTreeModel().getRelativePathFromRoot();
		    
		    // If the file referenced is in a subfolder within the same directory, don't make a copy
		    copyToSubfolder = simpleReferenceDirectoryPath.startsWith(metadataDirectoryPath);
		    
		    if(!copyToSubfolder) {
		    	//Copy the referenced file to the directory we're going to
		    	//write the metadata file to.
		    	try {
					String newSimpleReferencePath = fileServices.copyWorkspaceFile(userName, simpleReferenceFile.getRelativePathFromRoot(), metadataDirectory.getRelativePathFromRoot(), NameCollisionResolutionBehavior.FAIL_ON_COLLISION, null);
                    ((generated.metadata.Metadata.Simple)metadata.getContent()).setValue(newSimpleReferencePath);
				} catch (IllegalArgumentException | DetailedException e) {
					logger.error("Unable to generate metadata file for '" + metadataFilePath, e);
			    	
			    	GenerateMetadataFileResult result = new GenerateMetadataFileResult();
					result.setSuccess(false);
		    		result.setErrorMsg("Unable to generate metadata file for '" + metadataFilePath + "'. Reason: " + e.toString());
		    		return result;
				}
		    }
	    }
	    
	    //Now that we've got the grunt work done, we can go about saving the
	    //metadata file.
	    String metadataFile;
	    try{
			
			if(copyToSubfolder) {
				// copy the metadata to the same subfolder as the content file
				
				String path = metadataFilePath.substring(0, metadataFilePath.lastIndexOf("/") + 1);
				
				if(simpleReferencePath != null){
				    path += simpleReferencePath.substring(0, simpleReferencePath.lastIndexOf("/") + 1);
				}
				
				path += metadataFilePath.substring(metadataFilePath.lastIndexOf("/") + 1);
				
				metadataFile = path;
				
			} else {
				metadataFile = metadataFilePath;
			}
			
			fileServices.marshalToFile(userName, metadata, metadataFile, null);
	    
	    } catch(Exception e){
	    	
	    	logger.error("Unable to generate metadata file for '" + metadataFilePath, e);
	    	
	    	GenerateMetadataFileResult result = new GenerateMetadataFileResult();
			result.setSuccess(false);
    		result.setErrorMsg("Unable to generate metadata file for '" + metadataFilePath + "'. Reason: " + e.toString());
    		return result;
	    }
	    
	    ContentTypeEnum contentType = null;
	    try{
	        AbstractFolderProxy courseFolder = fileServices.getCourseFolder(metadataFilePath, userName);
	        contentType = fileServices.getContentType(userName, courseFolder, metadata);
	    }catch(Exception e){
	        logger.error("Failed to determine the content type for the metadata file being created at '"+metadataFile+"' for "+userName, e);
	    }
	    
	    //Return success!
        String displayName = MetadataSchemaHandler.getDisplayName(metadata);
	    MetadataWrapper metadataWrapper = 
	            new MetadataWrapper(metadataFile, displayName, MetadataSchemaHandler.isRemediationOnly(metadata), false, contentType);
		GenerateMetadataFileResult result = new GenerateMetadataFileResult(metadataWrapper);
		
		MetricsSenderSingleton.getInstance().endTrackingRpc("course.GenerateMetadataFile", start);
		return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<GenerateMetadataFile> getActionType() {
		return GenerateMetadataFile.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(GenerateMetadataFile arg0, GenerateMetadataFileResult arg1,
			ExecutionContext arg2) throws DispatchException {		
	}
}
