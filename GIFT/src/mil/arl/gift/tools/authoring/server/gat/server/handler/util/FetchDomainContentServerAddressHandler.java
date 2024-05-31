/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchDomainContentServerAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchDomainContentServerAddressResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * A handler that retrieves the domain content server address.
 * 
 * @author bzahid
 */
public class FetchDomainContentServerAddressHandler implements ActionHandler<FetchDomainContentServerAddress, FetchDomainContentServerAddressResult> {

	@Override
	public FetchDomainContentServerAddressResult execute(
			FetchDomainContentServerAddress action, ExecutionContext context)
			throws DispatchException {
		
	    long start = System.currentTimeMillis();
	    
	    FetchDomainContentServerAddressResult result = new FetchDomainContentServerAddressResult(DomainModuleProperties.getInstance().getDomainContentServerAddress());
	    
	    MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchDomainContentServerAddress", start);
	    
	    return result;
		
	}

	@Override
	public Class<FetchDomainContentServerAddress> getActionType() {
		return FetchDomainContentServerAddress.class;
	}

	@Override
	public void rollback(FetchDomainContentServerAddress action,
			FetchDomainContentServerAddressResult result, ExecutionContext context)
			throws DispatchException {
		// nothing to rollback
	}

}
