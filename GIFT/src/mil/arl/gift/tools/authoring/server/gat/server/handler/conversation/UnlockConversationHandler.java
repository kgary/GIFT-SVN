/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UnlockConversation;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles request to unlock the course so other users can edit it.
 */
public class UnlockConversationHandler implements ActionHandler<UnlockConversation, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(UnlockConversationHandler.class);
    
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public GatServiceResult execute(UnlockConversation action, ExecutionContext context)
			throws DispatchException {
	    long start = System.currentTimeMillis();
	    
	    if(logger.isInfoEnabled()){
	        logger.info("Attempting to unlock conversation : "+action);
	    }
	    
	    String relativePath = action.getPath();
	    String userName = action.getUserName();
	    String browserSessionKey = action.getBrowserSessionKey();
	    
	    GatServiceResult result = new GatServiceResult();
	    try{
	    	ServicesManager.getInstance().getFileServices().unlockFile(userName, browserSessionKey, relativePath);
	    } catch(Exception e){
	        logger.error("Caught exception while trying to unlock file '"+relativePath+"'.", e);
	        
        	String msg = "An error occurred while trying to unlock conversation file: " + relativePath;
            
        	result.setErrorMsg(msg);
        	result.setSuccess(false);
        }
        
	    MetricsSenderSingleton.getInstance().endTrackingRpc("conversation.UnlockConversation", start);
        return result;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<UnlockConversation> getActionType() {
		return UnlockConversation.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(UnlockConversation unlockFile, GatServiceResult result,
			ExecutionContext context) throws DispatchException {
		// TODO Auto-generated method stub
	}
}
