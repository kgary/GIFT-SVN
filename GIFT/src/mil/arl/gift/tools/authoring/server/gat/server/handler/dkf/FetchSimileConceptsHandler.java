/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import java.util.List;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchSimileConcepts;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchSimileConceptsResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * The Class FetchSimileConceptsHandler.
 */
public class FetchSimileConceptsHandler implements ActionHandler<FetchSimileConcepts, FetchSimileConceptsResult> {

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public FetchSimileConceptsResult execute(
			FetchSimileConcepts action, ExecutionContext context)
					throws ActionException {
	    
	    long start = System.currentTimeMillis();
		FetchSimileConceptsResult result = new FetchSimileConceptsResult();
		
		if(action.getConfigurationFilePath() != null){
			
			String userName = action.getUserName();

			try {
		         FileTreeModel directoryModel = ServicesManager.getInstance().getFileServices().getRootTree(userName);
		            
		        FileTreeModel fileModel = directoryModel.getModelFromRelativePath(action.getConfigurationFilePath());
				List<String> concepts = ServicesManager.getInstance().getFileServices().getSIMILEConcepts(userName, fileModel);
				result.addConcepts(concepts);
				
			} catch (Exception e) {
				result.setSuccess(false);
				result.setErrorMsg(e.toString());
			}
		
		} else {
			result.setSuccess(false);
			result.setErrorMsg("The configuration file name cannot be null");
		}
		MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.FetchSimileConcepts", start);
		return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<FetchSimileConcepts> getActionType() {
		return FetchSimileConcepts.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(FetchSimileConcepts arg0,
			FetchSimileConceptsResult arg1, ExecutionContext arg2)
			throws ActionException {
		
	}

}
