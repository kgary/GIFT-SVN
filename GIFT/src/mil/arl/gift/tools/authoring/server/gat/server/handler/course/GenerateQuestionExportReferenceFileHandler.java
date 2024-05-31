/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateQuestionExportReferenceFile;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GenerateQuestionExportReferenceFileHandler.
 */
public class GenerateQuestionExportReferenceFileHandler implements ActionHandler<GenerateQuestionExportReferenceFile, GenericGatServiceResult<Void>>{

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(GenerateQuestionExportReferenceFileHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GenericGatServiceResult<Void> execute(GenerateQuestionExportReferenceFile action, ExecutionContext ctx)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    logger.info("execute()");
	    
	    AbstractQuestion question = action.getQuestion();
	    String targetFilename = action.getTargetFilename();
		String userName = action.getUserName();
	    
	    try{			
			ServicesManager.getInstance().getFileServices().marshalToFile(userName, question, targetFilename, null);
	    } catch(Exception e){
	    	
	    	logger.error("Unable to generate metadata file for '" + targetFilename + "'.", e);
	    	
    		MetricsSenderSingleton.getInstance().endTrackingRpc("course.GenerateQuestionExportReferenceFile", start);
    		return new GenericGatServiceResult<Void>(new FailureResponse<Void>(new DetailedException(
    				"Unable to generate metadata file for '" + targetFilename, 
    				e.toString(), 
    				e
    		)));
	    }
	    
	    //Return success!
		MetricsSenderSingleton.getInstance().endTrackingRpc("course.GenerateQuestionExportReferenceFile", start);
		return new GenericGatServiceResult<Void>(new SuccessfulResponse<Void>());
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<GenerateQuestionExportReferenceFile> getActionType() {
		return GenerateQuestionExportReferenceFile.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(GenerateQuestionExportReferenceFile arg0, GenericGatServiceResult<Void> arg1,
			ExecutionContext arg2) throws DispatchException {		
	}
}
