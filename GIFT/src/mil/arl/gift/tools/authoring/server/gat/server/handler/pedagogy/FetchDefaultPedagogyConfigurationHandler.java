/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.pedagogy;
import java.io.File;
import java.io.FileInputStream;
import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.ped.PedagogicalModuleProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy.FetchDefaultPedagogyConfiguration;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.ped.EMAP;


/**
 * A handler that fetches the default pedagogical configuration template object for clients that request it
 */ 
public class FetchDefaultPedagogyConfigurationHandler implements ActionHandler<FetchDefaultPedagogyConfiguration, GenericGatServiceResult<EMAP>> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchDefaultPedagogyConfigurationHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchDefaultPedagogyConfiguration> getActionType() {
        return FetchDefaultPedagogyConfiguration.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized GenericGatServiceResult<EMAP> execute(FetchDefaultPedagogyConfiguration action, ExecutionContext context) {
        long start = System.currentTimeMillis();
        logger.debug("execute()");

        GenericRpcResponse<EMAP> result = null;
        
        try{
	        File pedFile = new File(PedagogicalModuleProperties.getInstance().getEMAPConfigFileName());
	        
	        UnmarshalledFile uFile = AbstractSchemaHandler.parseAndValidate(
	        		new FileInputStream(pedFile), 
	        		FileType.EMAP_PEDAGOGICAL_CONFIGURATION, 
	        		true
	        );
	        EMAP jaxbObject = (EMAP) uFile.getUnmarshalled();
	        
	        result = new SuccessfulResponse<EMAP>(jaxbObject); 
	        
        } catch(DetailedException e){
        	
        	result = new FailureResponse<>(e);
        	
        } catch(Exception e){
        	
        	result = new FailureResponse<>(new DetailedException(
        			"Failed to retrieve default pedagogical configuration template.", 
        			e.toString(), 
        			e
        	));
        }
        
        MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchDefaultPedagogyConfiguration", start);
        
        return new GenericGatServiceResult<>(result);
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback( FetchDefaultPedagogyConfiguration action, GenericGatServiceResult<EMAP> result, ExecutionContext context ) 
            throws ActionException {

    }
}
