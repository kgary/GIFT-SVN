/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import generated.course.LessonMaterialList;

import java.util.ArrayList;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ConvertLessonMaterialFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ConvertLessonMaterialFilesResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class handles requests to convert LessonMaterialFiles to LessonMaterialLists
 * 
 * @author nroberts
 *
 */
public class ConvertLessonMaterialFilesHandler implements ActionHandler<ConvertLessonMaterialFiles, ConvertLessonMaterialFilesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(ConvertLessonMaterialFilesHandler.class);

    @Override
	public Class<ConvertLessonMaterialFiles> getActionType() {
        return ConvertLessonMaterialFiles.class;
    }

    @Override
	public ConvertLessonMaterialFilesResult execute(ConvertLessonMaterialFiles action, ExecutionContext context) throws ActionException {
        
        long start = System.currentTimeMillis();
		String userName = action.getUsername();
    	ConvertLessonMaterialFilesResult result = new ConvertLessonMaterialFilesResult();
    	try{
    		LessonMaterialList convertedList = new LessonMaterialList();
    		
    		for(String file : action.getFiles().getFile()){
    			
        		String lmFile = action.getCourseFolderPath() + Constants.FORWARD_SLASH + file;
        		
        		UnmarshalledFile unmarshallFile = ServicesManager.getInstance().getFileServices().unmarshalFile(userName, lmFile);
        		LessonMaterialList unmarshalled = (LessonMaterialList) unmarshallFile.getUnmarshalled();

        		if(unmarshalled.getMedia() != null){
        			convertedList.getMedia().addAll(unmarshalled.getMedia());
        		}
    		}	
    		
    		result.setList(convertedList);
    		result.setSuccess(true);

        } catch (Exception thrown){
        	
            logger.error("Caught exception while converting a lesson material file to a lesson material list.", thrown);
            result.setSuccess(false);
            
            if(thrown instanceof DetailedException){
            	
            	DetailedException exception = (DetailedException) thrown;
            	
            	result.setErrorMsg(exception.getReason());
            	result.setErrorDetails(exception.getDetails());
            	result.setErrorStackTrace(exception.getErrorStackTrace());
            	
            } else {
            	
	            result.setErrorMsg("An error occurred while converting a lesson material file to a lesson material list.");
	            result.setErrorDetails(thrown.toString());
	            
	            ArrayList<String> stackTrace = new ArrayList<String>();
				
				if(thrown.getStackTrace() != null){
					for(StackTraceElement e : thrown.getStackTrace()){
						stackTrace.add(e.toString());
					}
				}
				
				result.setErrorStackTrace(stackTrace);
            }
        }

    	MetricsSenderSingleton.getInstance().endTrackingRpc("course.GetMerillQuadrantFiles", start);
    	
    	return result;
    }

    @Override
	public void rollback( ConvertLessonMaterialFiles action, ConvertLessonMaterialFilesResult result, ExecutionContext context ) throws ActionException {
    }
}