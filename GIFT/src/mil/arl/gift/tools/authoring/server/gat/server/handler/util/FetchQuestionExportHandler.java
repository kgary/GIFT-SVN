/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;
import java.io.Serializable;

import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchQuestionExport;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ActionHandler for actions of type {@link FetchQuestionExport}
 *
 */ 
public class FetchQuestionExportHandler implements ActionHandler<FetchQuestionExport, GenericGatServiceResult<AbstractQuestion>> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchQuestionExportHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchQuestionExport> getActionType() {
        return FetchQuestionExport.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized GenericGatServiceResult<AbstractQuestion> execute(FetchQuestionExport action, ExecutionContext context) {
        long start = System.currentTimeMillis();
        logger.debug("execute()");

        ///////////////////////////////////////////////////////////////////////
        //Rudimentary error checking
        ///////////////////////////////////////////////////////////////////////
        
        //Quick null check because of a hack in the DKF presenter.
		String userName = action.getUserName();
        String relativePath = action.getRelativePath();
        if(relativePath == null) {
        	
        	return new GenericGatServiceResult<AbstractQuestion>(new FailureResponse<AbstractQuestion>(
        			new DetailedException(
        					"An error occurred while loading the file from the server.", 
        					"Can't parse a question export when supplied with an empty or undefined path.", 
        					null
        			)
        	));
        }

        //Make sure we support parsing this extension.
        boolean isSupportedFile = false;
        if(relativePath.endsWith(FileUtil.QUESTION_EXPORT_SUFFIX)) {
        	isSupportedFile = true;
        }
        if(!isSupportedFile) {
        	
        	return new GenericGatServiceResult<AbstractQuestion>(new FailureResponse<AbstractQuestion>(
        			new DetailedException(
        					"The file '" + relativePath + "' has a file extension that is not authorable and cannot be loaded into this context.", 
        					"This context can only author files with the following extesions: " 
        		        			+ FileUtil.QUESTION_EXPORT_SUFFIX + ".", 
        					null
        			)
        	));
        }

		///////////////////////////////////////////////////////////////////////
		//Handling the normal load case.
		///////////////////////////////////////////////////////////////////////
        UnmarshalledFile loadedFile = null;
        
        try{
        	loadedFile = ServicesManager.getInstance().getFileServices().unmarshalFile(userName, relativePath);
        	
        } catch(Exception e){
        	
        	logger.error("An error occurred while unmarshalling a question export at '" + relativePath + "'.", e);
        	
            if(e instanceof DetailedException){
            	
            	return new GenericGatServiceResult<AbstractQuestion>(new FailureResponse<AbstractQuestion>(
            			(DetailedException) e
            	));
            	
            } else {
            	
            	return new GenericGatServiceResult<AbstractQuestion>(new FailureResponse<AbstractQuestion>(
            			new DetailedException(
            					"There was a problem reading data from the file at '" + relativePath + "' on the server. "
            		           			+ "Please check the file path to make sure it exists.",
            					e.toString(), 
            					e
            			)
            	));
            }
        }
        
        Serializable jaxbObject = loadedFile.getUnmarshalled();
        
        if(jaxbObject == null) {
           	
           	return new GenericGatServiceResult<AbstractQuestion>(new FailureResponse<AbstractQuestion>(
        			new DetailedException(
        					"An error occurred while returning '" + relativePath + "' from the server."
        		           			+ "Please check the file path to make sure it exists.",
        		           	"Failed to unmarshal the JAXB object at '" + relativePath + "'.", 
        					null
        			)
        	));
           	
        } else if(jaxbObject instanceof AbstractQuestion){
        	
            MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchQuestionExport", start);
            
            return new GenericGatServiceResult<>(new SuccessfulResponse<AbstractQuestion>((AbstractQuestion) jaxbObject));
            
        } else {
        	
        	return new GenericGatServiceResult<AbstractQuestion>(new FailureResponse<AbstractQuestion>(
        			new DetailedException(
        					"An error occurred while returning '" + relativePath + "' from the server."
        		           			+ "Please check the file path to make sure it is correct.",
        		           	"The course object found at '" + relativePath + "' is not a question.", 
        					null
        			)
        	));
        }
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback( FetchQuestionExport action, GenericGatServiceResult<AbstractQuestion> result, ExecutionContext context ) 
            throws ActionException {

    }
}
