/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import generated.course.LessonMaterialList;
import generated.course.SlideShowProperties;
import generated.course.TrainingApplicationWrapper;
import generated.metadata.Metadata;

import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.DeleteMetadata;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class handles requests to delete metadata files
 * 
 * @author nroberts
 *
 */
public class DeleteMetadataHandler implements ActionHandler<DeleteMetadata, GenericGatServiceResult<Void>> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(DeleteMetadataHandler.class);

    @Override
	public Class<DeleteMetadata> getActionType() {
        return DeleteMetadata.class;
    }

    @Override
	public GenericGatServiceResult<Void> execute(DeleteMetadata action, ExecutionContext context) throws ActionException {
        
        long start = System.currentTimeMillis();
		String userName = action.getUsername();
		
		GenericRpcResponse<Void> response = null;
    	
    	try{
    		String courseFolderPath = action.getCourseFilePath();
			String metadataFilePath = action.getMetadataFilePath();
    		
    		if(ServicesManager.getInstance().getFileServices().fileExists(action.getUsername(), metadataFilePath, false)){
    			
    			if(action.isDeepDelete()){
        			
    				//parse the metadata file so we can delete any files it references
    				UnmarshalledFile unmarshalled = ServicesManager.getInstance().getFileServices().unmarshalFile(
    						action.getUsername(), 
    						metadataFilePath
    				);
    				
    				if(unmarshalled.getUnmarshalled() != null 
    						&& unmarshalled.getUnmarshalled() instanceof Metadata){
    						
    					Metadata metadata = (Metadata) unmarshalled.getUnmarshalled();
    					
    					//check to see if this metadata file contains a training app reference
                        if(metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp &&
                                ((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue() != null){
        		    			
                            String taRef = ((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue();
    		    			
                            String trainingAppRefFile = courseFolderPath + Constants.FORWARD_SLASH + taRef;
                            
    		    			if(ServicesManager.getInstance().getFileServices().fileExists(action.getUsername(), trainingAppRefFile, false)){
    		    				
    		    				//if it does, unmarshall that training app reference so we can gather information about it
    			    			UnmarshalledFile unmarshalledRef = ServicesManager.getInstance().getFileServices().unmarshalFile(
    			    					userName, 
    			    					courseFolderPath + Constants.FORWARD_SLASH + taRef
    			    			);
    			    			
    			    			if(unmarshalledRef.getUnmarshalled() != null 
    			    					&& unmarshalledRef.getUnmarshalled() instanceof TrainingApplicationWrapper){
    			    					
    			    				TrainingApplicationWrapper wrapper = (TrainingApplicationWrapper) unmarshalledRef.getUnmarshalled();
    			    				
    			    				if(wrapper.getTrainingApplication() != null 
    			    						&& wrapper.getTrainingApplication().getDkfRef() != null
    			    						&& wrapper.getTrainingApplication().getDkfRef().getFile() != null){
    			    					
    			    					String dkfRef = wrapper.getTrainingApplication().getDkfRef().getFile();
    			    					
    			    					String dkfRefFile = courseFolderPath + Constants.FORWARD_SLASH + dkfRef;
    			    					
    			    					if(ServicesManager.getInstance().getFileServices().fileExists(action.getUsername(), dkfRefFile, false)){
    			    					
    				    					//delete the DKF file
    				    					boolean deleted = ServicesManager.getInstance().getFileServices().deleteFile(
    						    					action.getUsername(), 
    						    					action.getBrowserSessionKey(),
    						    					dkfRefFile, 
    						    					null
    						    			);
    						    			
    						    			if(!deleted){
    						    				
    						    				throw new DetailedException(
    						                			"An problem occurred while attempting to delete '" + dkfRef + "'", 
    						                			"Failed to delete '" + dkfRef + "'. The file may be locked by another user "
    						                					+ "for an ongoing operation.", 
    						                			null
    						                	);	
    						    			}	
    			    					}
    			    				}
    			    			} 
    			    			
    			    			//delete the training app reference file
    			    			boolean deleted = ServicesManager.getInstance().getFileServices().deleteFile(
    			    					action.getUsername(), 
    			    					action.getBrowserSessionKey(),
    			    					trainingAppRefFile, 
    			    					null
    			    			);
    			    			
    			    			if(!deleted){
    			    				
    			    				throw new DetailedException(
    			                			"An problem occurred while attempting to delete '" + taRef + "'", 
    			                			"Failed to delete '" + taRef + "'. The file may be locked by another user "
    			                					+ "for an ongoing operation.", 
    			                			null
    			                	);	
    			    			}	
    		    			}
    					}
    					
    					//check to see if this metadata file contains a lesson material reference
                        if(metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial &&
                                ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue() != null){    
        		    			
                            String lmRef = ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue();					

                            String lessonMaterialRefFile = courseFolderPath + Constants.FORWARD_SLASH + lmRef;
    		    			UnmarshalledFile unmarshalledRef = 
    		    			        ServicesManager.getInstance().getFileServices().unmarshalFile(action.getUsername(), courseFolderPath + Constants.FORWARD_SLASH + lmRef);

    		    			//if the media content is a slide show, the slide show images need to be deleted.
    		    			if(unmarshalledRef.getUnmarshalled() != null && unmarshalledRef.getUnmarshalled() instanceof LessonMaterialList){
    		    				LessonMaterialList lmList = (LessonMaterialList) unmarshalledRef.getUnmarshalled();

    		    				if(lmList.getMedia() != null && !lmList.getMedia().isEmpty()) {
    		    					if(lmList.getMedia().get(0).getMediaTypeProperties() instanceof SlideShowProperties) {
    		    						String slideShowPath = courseFolderPath + Constants.FORWARD_SLASH + lmList.getMedia().get(0).getUri();
    		    						FileTreeModel slideShowFolder = FileTreeModel.createFromRawPath(slideShowPath);
    		    						if(slideShowFolder != null) {
    		    							slideShowFolder = slideShowFolder.getParentTreeModel();

    		    							ServicesManager.getInstance().getFileServices().deleteFile(
    		    									action.getUsername(), 
    		    									action.getBrowserSessionKey(),
    		    									slideShowFolder.getRelativePathFromRoot(), 
    		    									null);
    		    						}
    		    					}
    		    				}
    		    			}

    		    			
    		    			if(ServicesManager.getInstance().getFileServices().fileExists(action.getUsername(),lessonMaterialRefFile, false)){			
    			    			
    			    			//delete the training app reference file
    			    			boolean deleted = ServicesManager.getInstance().getFileServices().deleteFile(
    			    					action.getUsername(), 
    			    					action.getBrowserSessionKey(),
    			    					lessonMaterialRefFile, 
    			    					null
    			    			);
    			    			
    			    			if(!deleted){
    			    				
    			    				throw new DetailedException(
    			                			"An problem occurred while attempting to delete '" + lmRef + "'", 
    			                			"Failed to delete '" + lmRef + "'. The file may be locked by another user "
    			                					+ "for an ongoing operation.", 
    			                			null
    			                	);	
    			    			}	
    		    			}
    					}	
    				}
        		}
			
	    		//delete the metadata file
				boolean deleted = ServicesManager.getInstance().getFileServices().deleteFile(
						action.getUsername(), 
						action.getBrowserSessionKey(),
						metadataFilePath, 
						null
				);
				
				if(!deleted){
					
					throw new DetailedException(
	            			"An problem occurred while attempting to delete '" + action.getMetadataFilePath() + "'", 
	            			"Failed to delete '" + action.getMetadataFilePath() + "'. The file may be locked by another user "
	            					+ "for an ongoing operation.", 
	            			null
	            	);
					
				}
    		}
    		
    		response = new SuccessfulResponse<Void>();

        } catch (Exception thrown){
        	
            logger.error("Caught exception while deleting a metadata file for " + action.getCourseFilePath(), thrown);
            
            if(thrown instanceof DetailedException){
            	
            	response = new FailureResponse<Void>((DetailedException) thrown);
            	
            } else {
            	
            	response = new FailureResponse<Void>(new DetailedException(
            			"An error occurred while attempting to delete a metadata file.", 
            			thrown.toString(), 
            			thrown
            	));           	
            }
        }

    	MetricsSenderSingleton.getInstance().endTrackingRpc("course.GetMerillQuadrantFiles", start);
    	
    	return new GenericGatServiceResult<Void>(response);
    }

    @Override
	public void rollback( DeleteMetadata action, GenericGatServiceResult<Void> result, ExecutionContext context ) throws ActionException {
    }
}