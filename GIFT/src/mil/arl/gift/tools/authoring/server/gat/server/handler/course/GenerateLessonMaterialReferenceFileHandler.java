/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import generated.course.LessonMaterialList;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFileResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GenerateLessonMaterialReferenceFileHandler.
 */
public class GenerateLessonMaterialReferenceFileHandler implements ActionHandler<GenerateLessonMaterialReferenceFile, GenerateLessonMaterialReferenceFileResult>{

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GenerateLessonMaterialReferenceFileHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GenerateLessonMaterialReferenceFileResult execute(GenerateLessonMaterialReferenceFile action, ExecutionContext ctx)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    logger.info("execute()");
	    
	    LessonMaterialList lessonMaterialList = action.getLessonMaterialList();
	    String targetFilename = action.getTargetFilename();
		String userName = action.getUserName();
	    
	    try{
			
			ServicesManager.getInstance().getFileServices().marshalToFile(userName, lessonMaterialList, targetFilename, null);
	    } catch(Exception e){
	    	
	    	logger.error("Unable to generate metadata file for '" + targetFilename + "'.", e);
	    	
	    	GenerateLessonMaterialReferenceFileResult result = new GenerateLessonMaterialReferenceFileResult();
			result.setSuccess(false);
    		result.setErrorMsg("Unable to generate metadata file for '" + targetFilename + "'.");
    		result.setErrorDetails("Reason: " + e.toString());
    		result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
    		return result;
	    }
	    
	    //Return success!
		GenerateLessonMaterialReferenceFileResult result = new GenerateLessonMaterialReferenceFileResult();
		MetricsSenderSingleton.getInstance().endTrackingRpc("course.GenerateLessonMaterialReferenceFile", start);
		return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<GenerateLessonMaterialReferenceFile> getActionType() {
		return GenerateLessonMaterialReferenceFile.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(GenerateLessonMaterialReferenceFile arg0, GenerateLessonMaterialReferenceFileResult arg1,
			ExecutionContext arg2) throws DispatchException {		
	}
}
