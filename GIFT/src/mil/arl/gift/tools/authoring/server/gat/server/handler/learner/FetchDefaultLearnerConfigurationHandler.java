/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.learner;
import java.io.File;
import java.io.FileInputStream;
import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.learner.LearnerModuleProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.FetchDefaultLearnerConfiguration;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.learner.LearnerConfiguration;


/**
 * A handler that fetches the default learner configuration template object for clients that request it
 */ 
public class FetchDefaultLearnerConfigurationHandler implements ActionHandler<FetchDefaultLearnerConfiguration, GenericGatServiceResult<LearnerConfiguration>> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchDefaultLearnerConfigurationHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchDefaultLearnerConfiguration> getActionType() {
        return FetchDefaultLearnerConfiguration.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized GenericGatServiceResult<LearnerConfiguration> execute(FetchDefaultLearnerConfiguration action, ExecutionContext context) {
        long start = System.currentTimeMillis();
        logger.debug("execute()");

        GenericRpcResponse<LearnerConfiguration> result = null;
        
        try{
	        File learnerFile = new File(LearnerModuleProperties.getInstance().getLearnerConfigurationFilename());
	        
	        UnmarshalledFile uFile = AbstractSchemaHandler.parseAndValidate(
	        		new FileInputStream(learnerFile), 
	        		FileType.LEARNER_CONFIGURATION, 
	        		true
	        );
	        LearnerConfiguration jaxbObject = (LearnerConfiguration)uFile.getUnmarshalled();
	        
	        result = new SuccessfulResponse<LearnerConfiguration>(jaxbObject); 
	        
        } catch(DetailedException e){
        	
        	result = new FailureResponse<>(e);
        	
        } catch(Exception e){
        	
        	result = new FailureResponse<>(new DetailedException(
        			"Failed to retrieve default learner configuration template.", 
        			e.toString(), 
        			e
        	));
        }
        
        MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchDefaultLearnerConfiguration", start);
        
        return new GenericGatServiceResult<>(result);
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback( FetchDefaultLearnerConfiguration action, GenericGatServiceResult<LearnerConfiguration> result, ExecutionContext context ) 
            throws ActionException {

    }
}
