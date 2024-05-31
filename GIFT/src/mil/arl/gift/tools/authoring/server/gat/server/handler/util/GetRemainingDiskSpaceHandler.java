/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetRemainingDiskSpace;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetRemainingDiskSpaceResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * A handler that retrieves the remaining disk space for a given user.
 * 
 * @author bzahid
 */
public class GetRemainingDiskSpaceHandler implements ActionHandler<GetRemainingDiskSpace, GetRemainingDiskSpaceResult> {

    private static final String METRICS_TAG = "util.GetRemainingDiskSpace";
    
	@Override
	public GetRemainingDiskSpaceResult execute(GetRemainingDiskSpace action, ExecutionContext context) throws DispatchException {
		
	    long start = System.currentTimeMillis();
	    
		GetRemainingDiskSpaceResult result = new GetRemainingDiskSpaceResult();
		
		try {
			
			long diskSpace = ServicesManager.getInstance().getFileServices().getRemainingWorkspacesQuota(action.getUserName());
			result.setDiskSpace((diskSpace == Long.MAX_VALUE) ? "Unlimited" : diskSpace + " MB");			
			result.setSuccess(true);
			
		} catch (DetailedException e) {
			
			result.setSuccess(false);
			result.setErrorMsg(e.getReason());
			result.setErrorDetails(e.getDetails());
			result.setErrorStackTrace(e.getErrorStackTrace());
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
		
		return result;
	}

	@Override
	public Class<GetRemainingDiskSpace> getActionType() {
		return GetRemainingDiskSpace.class;
	}

	@Override
	public void rollback(GetRemainingDiskSpace action,
			GetRemainingDiskSpaceResult result, ExecutionContext context)
			throws DispatchException {
		// Nothing to roll back		
	}
	
	
}
