/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplDescription;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplDescriptionResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;


/**
 * The Class FetchConditionImplDescriptionHandler that retrieves information about a condition.
 */
public class FetchConditionImplDescriptionHandler implements ActionHandler<FetchConditionImplDescription, FetchConditionImplDescriptionResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchConditionImplDescriptionHandler.class);

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public FetchConditionImplDescriptionResult execute(
            FetchConditionImplDescription action, ExecutionContext context)
                    throws ActionException {
        long start = System.currentTimeMillis();
        
        if(logger.isInfoEnabled()){
            logger.info("execute()");
        }
        
        FetchConditionImplDescriptionResult result = null;
        
        try {            

            ConditionInfo condInfo = DomainKnowledgeUtil.getConditionInfoForConditionImpl(action.getImplClassName());
        	result = new FetchConditionImplDescriptionResult(condInfo);
            
        } catch (Exception e) {  
            logger.error("Caught exception while getting a condition implementation description" 
            		+ (action.getImplClassName() != null ? (" for " + action.getImplClassName() + ".") : "") + ". ", e);
            result = new FetchConditionImplDescriptionResult();
            result.setSuccess(false);
            result.setErrorMsg("Failed to get the condition implementation description, therefore no description will be available.");
            result.setErrorDetails(e.toString());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }
        MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.FetchConditionImplDescription", start);
        return result;
    }


    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchConditionImplDescription> getActionType() {
        return FetchConditionImplDescription.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public void rollback(FetchConditionImplDescription arg0,
            FetchConditionImplDescriptionResult arg1, ExecutionContext arg2)
                    throws ActionException {
    }
}
