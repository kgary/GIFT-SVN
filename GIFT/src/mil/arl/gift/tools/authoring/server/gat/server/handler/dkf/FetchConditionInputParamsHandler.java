/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionInputParams;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionInputParamsResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler that retrieves Condition Implementation Input type 
 * parameters for a specified Condition Implementation.
 * 
 * @author bzahid
 */
public class FetchConditionInputParamsHandler implements ActionHandler<FetchConditionInputParams, FetchConditionInputParamsResult> {

    private static final String METRICS_TAG = "dkf.FetchConditionInputParams";
	@Override
	public FetchConditionInputParamsResult execute(FetchConditionInputParams action, ExecutionContext context) throws DispatchException {
		
	    long start = System.currentTimeMillis();
	    
		FetchConditionInputParamsResult result = new FetchConditionInputParamsResult();
		result.setInputParams(DomainKnowledgeUtil.getValidConditionInputParams(action.getConditionImpl()));
		result.setSuccess(true);
		
		MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
		return result;
	}

	@Override
	public Class<FetchConditionInputParams> getActionType() {
		return FetchConditionInputParams.class;
	}

	@Override
	public void rollback(FetchConditionInputParams action, FetchConditionInputParamsResult result, ExecutionContext context) 
			throws DispatchException {
		// nothing to roll back		
	}

}
