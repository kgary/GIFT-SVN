/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import generated.course.TrainingApplicationWrapper;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateTrainingAppReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateTrainingAppReferenceFileResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GenerateTrainingAppReferenceFileHandler.
 */
public class GenerateTrainingAppReferenceFileHandler implements ActionHandler<GenerateTrainingAppReferenceFile, GenerateTrainingAppReferenceFileResult>{

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GenerateTrainingAppReferenceFileHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GenerateTrainingAppReferenceFileResult execute(GenerateTrainingAppReferenceFile action, ExecutionContext ctx)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    logger.info("execute()");
	    
	    TrainingApplicationWrapper taWrapper = action.getTrainingAppWrapper();
        String currentVersion = taWrapper.getVersion();
        String schemaVersion = CommonUtil.getSchemaVersion(AbstractSchemaHandler.COURSE_SCHEMA_FILE);
        String newVersion = CommonUtil.generateVersionAttribute(currentVersion, schemaVersion);
        taWrapper.setVersion(newVersion);
	        
	    String targetFilename = action.getTargetFilename();
		String userName = action.getUserName();
	    
	    try{
			ServicesManager.getInstance().getFileServices().marshalToFile(userName, taWrapper, targetFilename, null);
	    } catch(Exception e){
	    	
	    	logger.error("Unable to generate metadata file for '" + targetFilename + "'.", e);
	    	
	    	GenerateTrainingAppReferenceFileResult result = new GenerateTrainingAppReferenceFileResult();
			result.setSuccess(false);
    		result.setErrorMsg("Unable to generate metadata file for '" + targetFilename + "'. Reason: " + e.toString());
    		return result;
	    }
	    
	    //Return success!
		GenerateTrainingAppReferenceFileResult result = new GenerateTrainingAppReferenceFileResult();
		MetricsSenderSingleton.getInstance().endTrackingRpc("course.GenerateTrainingAppReferenceFile", start);
		return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<GenerateTrainingAppReferenceFile> getActionType() {
		return GenerateTrainingAppReferenceFile.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(GenerateTrainingAppReferenceFile arg0, GenerateTrainingAppReferenceFileResult arg1,
			ExecutionContext arg2) throws DispatchException {		
	}
}
