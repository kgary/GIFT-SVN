/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionsOverallAssessmentTypes;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionsOverallAssessmentTypesResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class FetchConditionsOverallAssessmentTypesHandler that retrieves information about the overall assessments each condition supports
 */
public class FetchConditionsOverallAssessmentTypesHandler implements ActionHandler<FetchConditionsOverallAssessmentTypes, FetchConditionsOverallAssessmentTypesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchConditionsOverallAssessmentTypesHandler.class);

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public FetchConditionsOverallAssessmentTypesResult execute(
            FetchConditionsOverallAssessmentTypes action, ExecutionContext context)
                    throws ActionException {
        long start = System.currentTimeMillis();
        
        if(logger.isInfoEnabled()){
            logger.info("execute()");
        }
        
        FetchConditionsOverallAssessmentTypesResult result = null;
        
        try {            

            Map<String, Set<String>> overallAssessmentTypesConditionsMap = DomainKnowledgeUtil.getOverallAssessmentTypesConditionsMap();
        	result = new FetchConditionsOverallAssessmentTypesResult(overallAssessmentTypesConditionsMap);
            
        } catch (Exception e) {  
            logger.error("Caught exception while getting the conditions overall assessment type information.", e);
            result = new FetchConditionsOverallAssessmentTypesResult();
            result.setSuccess(false);
            result.setErrorMsg("Failed to get the conditions overall assessment type information.");
            result.setErrorDetails(e.toString());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }
        MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.FetchConditionsOverallAssessmentTypes", start);
        return result;
    }


    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchConditionsOverallAssessmentTypes> getActionType() {
        return FetchConditionsOverallAssessmentTypes.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public void rollback(FetchConditionsOverallAssessmentTypes arg0,
            FetchConditionsOverallAssessmentTypesResult arg1, ExecutionContext arg2)
                    throws ActionException {
    }
}
