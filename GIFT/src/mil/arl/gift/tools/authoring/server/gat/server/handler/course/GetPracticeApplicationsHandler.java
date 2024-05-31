/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.LessonMaterialList;
import generated.course.TrainingApplicationWrapper;
import generated.metadata.Metadata;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.metadata.MetadataSearchResult;
import mil.arl.gift.common.metadata.MetadataSearchResult.QuadrantResultSet;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.QuadrantRequest;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetPracticeApplications;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetPracticeApplicationsResult;
import mil.arl.gift.tools.authoring.server.gat.shared.model.course.PracticeApplicationObject;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * 
 * This class handles requests to get the list of applications for a Merrill's Practice quadrant in a course.
 * 
 * @author nroberts
 *
 */
public class GetPracticeApplicationsHandler implements ActionHandler<GetPracticeApplications, GetPracticeApplicationsResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GetPracticeApplicationsHandler.class);

    @Override
	public Class<GetPracticeApplications> getActionType() {
        return GetPracticeApplications.class;
    }

    @Override
	public GetPracticeApplicationsResult execute(GetPracticeApplications action, ExecutionContext context) throws ActionException {
        
        long start = System.currentTimeMillis();
		String userName = action.getUsername();
		
		GetPracticeApplicationsResult response = null;
    	
    	try{
    		
    		String courseFolderPath = action.getCourseFilePath();
    		   		
    		Map<MerrillQuadrantEnum, QuadrantRequest> quadrantToRequest = new HashMap<>();
            QuadrantRequest quadrantRequest = new QuadrantRequest(MerrillQuadrantEnum.PRACTICE, false, action.getRequiredConcepts(), action.getOtherCourseConcepts());
            quadrantToRequest.put(MerrillQuadrantEnum.PRACTICE, quadrantRequest);

            MetadataSearchResult metadataSearchResult = ServicesManager.getInstance().getFileServices()
    			.getMetadataContentFileTree(userName, courseFolderPath, quadrantToRequest);
            
            QuadrantResultSet quadrantResultSet = metadataSearchResult.getResultsForQuadrant(MerrillQuadrantEnum.PRACTICE);
    		
    		List<PracticeApplicationObject> applications = new ArrayList<PracticeApplicationObject>();
    		if(quadrantResultSet != null){
    		    buildPracticeApplicationObjects(userName, courseFolderPath, true, applications, quadrantResultSet.getMetadataRefs());
    		    
    		}
    		
    		response = new GetPracticeApplicationsResult();
    		response.setSuccess(true);
    		response.setPracticeApplications(applications);

        } catch (Exception thrown){
        	
            logger.error("Caught exception while getting practice applications for " + action.getCourseFilePath(), thrown);
            	
            response = new GetPracticeApplicationsResult();
            response.setSuccess(false);
            response.setErrorMsg("Failed to retrieve the practice metadata for the concepts selected.");
            response.setErrorDetails("An exception was caught that reads:\n"+thrown.getMessage());
            response.setErrorStackTrace(DetailedException.getFullStackTrace(thrown));
        }

    	MetricsSenderSingleton.getInstance().endTrackingRpc("course.GetMerillQuadrantFiles", start);
    	
    	return response;
    }

    @Override
	public void rollback( GetPracticeApplications action, GetPracticeApplicationsResult result, ExecutionContext context ) throws ActionException {
    }
    
    /**
     * Convert the metadata information into Practice application objects that can be sent back to the client
     * for displaying metadata to the author.
     * 
     * @param userName information used to authenticate the request when parsing metadata files. Can't be null.
     * @param courseFolderPath where the metadata files reside
     * @param isRequiredCourseConceptSearch whether the metadata must contain only required concepts and not extraneous concepts
     * @param applications the list that will be populated by this method so metadata about the practice applications can be returned to the client
     * @param metadataToContent the metadata that was found to pass the search criteria.  If null or empty this method does nothing.  
     */
    private void buildPracticeApplicationObjects(String userName, String courseFolderPath, 
            boolean isRequiredCourseConceptSearch, List<PracticeApplicationObject> applications, 
            Map<String, MetadataWrapper> metadataToContent){
        
        if(metadataToContent == null){
            return;
        }
        
        //maintain a mapping from each training app ref file we come across to the object created for it so we don't end up making duplicate objects for the same file
        Map<String, PracticeApplicationObject> refToObject = new HashMap<String, PracticeApplicationObject>();
        
        for(String metadataFilePath : metadataToContent.keySet()){
            
            try{
                MetadataWrapper metadataWrapper = metadataToContent.get(metadataFilePath);
                
                generated.metadata.Metadata metadata = metadataWrapper.getMetadata();
                
                if(metadata == null){
                    
                    //unmarshall the metadata file so we can check its contents
                    UnmarshalledFile unmarshalled = ServicesManager.getInstance().getFileServices().unmarshalFile(userName, metadataFilePath);
                
                    if(unmarshalled.getUnmarshalled() != null){
                        
                        if(unmarshalled.getUnmarshalled() instanceof Metadata){
                            
                                metadata = (Metadata) unmarshalled.getUnmarshalled();
                        }
                    }
                }
                        
                if(metadata != null){
                        
                    //check to see if this metadata file contains a training app reference
                    if(metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp &&
                        ((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue() != null){
                        
                            String taRef = ((generated.metadata.Metadata.TrainingApp)metadata.getContent()).getValue();
                        
                            if(refToObject.containsKey(taRef)){
                            
                                //we already created an object for this training app ref, so just update the existing object to include this metadata
                                PracticeApplicationObject existing = refToObject.get(taRef);
                                
                                if(existing.getMetadataFilesMap() == null){
                                    
                                    //this shouldn't happen, but handle it anyway
                                    existing.setMetadataFilesMap(new HashMap<String, generated.metadata.Metadata>());
                                }
                                
                                existing.getMetadataFilesMap().put(metadataFilePath, metadata);
                                
                            } else {
                                
                                //we haven't created an object for this training app ref, so we need to add it
                                PracticeApplicationObject toAdd = new PracticeApplicationObject();
                                
                                String trainingAppRefFile = courseFolderPath + Constants.FORWARD_SLASH + taRef;
                                                                
                                try{
                                    
                                    //if it does, unmarshall that training app reference so we can gather information about it
                                    UnmarshalledFile unmarshalledRef = ServicesManager.getInstance().getFileServices().unmarshalFile(userName, trainingAppRefFile);
                                    
                                    if(unmarshalledRef.getUnmarshalled() != null){
                                        
                                        if(unmarshalledRef.getUnmarshalled() instanceof TrainingApplicationWrapper){
                                            
                                            TrainingApplicationWrapper wrapper = (TrainingApplicationWrapper) unmarshalledRef.getUnmarshalled();
                                            
                                            //gather data from the training app ref
                                            toAdd.setTrainingApplication(new TrainingAppCourseObjectWrapper(wrapper.getTrainingApplication(), 
                                                    ServicesManager.getInstance().getFileServices().getContentType(wrapper.getTrainingApplication())));
                                            
                                        } else {
                                            
                                            throw new IllegalArgumentException("The unmarshalled practice application object is not a valid "
                                                    + "training application object.");
                                        }
                                        
                                    } else {
                                        
                                        throw new NullPointerException("The unmarshalled practice application cannot be null.");
                                    }
                                    
                                } catch(DetailedException e){
                                    
                                    toAdd.setTrainingApplication(new TrainingAppCourseObjectWrapper(metadataWrapper.getDisplayName(), e, null));                   
                                    
                                } catch(Exception e){
                                    
                                    toAdd.setTrainingApplication(
                                        new TrainingAppCourseObjectWrapper(metadataWrapper.getDisplayName(), 
                                            new DetailedException(
                                                    "Failed to retrieve this practice application because an error occurred while parsing its file.", 
                                                    e.toString(), 
                                                    e
                                            ),
                                            null
                                    ));   
                                }
                                
                                if(toAdd.getMetadataFilesMap() == null){
                                    toAdd.setMetadataFilesMap(new HashMap<String, generated.metadata.Metadata>());
                                }
                                
                                toAdd.getMetadataFilesMap().put(metadataFilePath, metadata);
                                
                                toAdd.setContainsOnlyRequiredConcepts(isRequiredCourseConceptSearch);
                                
                                //add the training app reference to the result
                                applications.add(toAdd);
                                
                                //add a mapping for this training app ref so we don't generate another object if we run into another metadata file referencing it
                                refToObject.put(taRef, toAdd);
                            }
                    } 
                    // check to see if this metadata file contains a lesson material reference
                    else if (metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial
                            && ((generated.metadata.Metadata.LessonMaterial) metadata.getContent()).getValue() != null) {
                        String lmRef = ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue();
                                          
                        if(refToObject.containsKey(lmRef)){
                        
                            //we already created an object for this lesson material ref, so just update the existing object to include this metadata
                            PracticeApplicationObject existing = refToObject.get(lmRef);
                            
                            if(existing.getMetadataFilesMap() == null){
                                
                                //this shouldn't happen, but handle it anyway
                                existing.setMetadataFilesMap(new HashMap<String, generated.metadata.Metadata>());
                            }
                
                            existing.getMetadataFilesMap().put(metadataFilePath, metadata);
                            
                        } else {
                            
                            //we haven't created an object for this lesson material ref, so we need to add it
                            PracticeApplicationObject toAdd = new PracticeApplicationObject();
                            
                            String lessonMaterialRefFile = courseFolderPath + Constants.FORWARD_SLASH + lmRef;
                                                            
                            try{
                                
                                //if it does, unmarshall that training app reference so we can gather information about it
                                UnmarshalledFile unmarshalledRef = ServicesManager.getInstance().getFileServices().unmarshalFile(userName, lessonMaterialRefFile);
                                
                                if(unmarshalledRef.getUnmarshalled() != null){
                                    
                                    if(unmarshalledRef.getUnmarshalled() instanceof LessonMaterialList){
                                        
                                        LessonMaterialList lmList = (LessonMaterialList) unmarshalledRef.getUnmarshalled();
                                        toAdd.setLessonMaterial(lmList);
                                        
                                    } else {
                                        
                                        throw new IllegalArgumentException("The unmarshalled practice application object is not a valid "
                                                + "lesson material object.");
                                    }
                                    
                                } else {
                                    
                                    throw new NullPointerException("The unmarshalled practice application cannot be null.");
                                }
                                
                            } catch(DetailedException e){
                                
                                toAdd.setTrainingApplication(new TrainingAppCourseObjectWrapper(metadataWrapper.getDisplayName(), e, null));                   
                                
            } catch(Exception e){                   
                                
                                toAdd.setTrainingApplication(
                                    new TrainingAppCourseObjectWrapper(metadataWrapper.getDisplayName(), 
                                        new DetailedException(
                                                "Failed to retrieve this practice application because an error occurred while parsing its file.", 
                                                e.toString(), 
                                                e
                                        ),
                                        null
                                ));   
                            }
                            
                            if(toAdd.getMetadataFilesMap() == null){
                                toAdd.setMetadataFilesMap(new HashMap<String, generated.metadata.Metadata>());
                            }
                            
                            toAdd.getMetadataFilesMap().put(metadataFilePath, metadata);
                            
                            toAdd.setContainsOnlyRequiredConcepts(isRequiredCourseConceptSearch);
                            
                            //add the practice application reference to the result
                            applications.add(toAdd);
                            
                            //add a mapping for this practice app ref so we don't generate another object if we run into another metadata file referencing it
                            refToObject.put(lmRef, toAdd);
                        }
                    }
                }                    
                
            } catch(Exception e){                   
                logger.error("An exception occurred while parsing '" + metadataFilePath + "' to see if it contained a reference to a practice application", e);
            }
        }
    }
}