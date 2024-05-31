/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.EndConversation;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler to stop a conversation.
 * 
 * @author bzahid
 */
public class EndConversationHandler implements ActionHandler<EndConversation, GatServiceResult>{

    private static final String METRICS_TAG = "conversation.EndConversation";
    
	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(EndConversationHandler.class);

	@Override
	public GatServiceResult execute(EndConversation action, ExecutionContext context) throws DispatchException {
		
		long start = System.currentTimeMillis();
		logger.info("execute()");

		GatServiceResult result = new GatServiceResult();
		try {			
			
			ConversationUpdateManager.endConversation(action.getChatId());
			result.setSuccess(true);
		} catch (Exception e) {	
			
			result.setSuccess(false);
			result.setErrorMsg("Failed to stop the conversation.");
			result.setErrorDetails(e.toString());
		}
		
        MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
		return result;
	}

	@Override
	public Class<EndConversation> getActionType() {
		return EndConversation.class;
	}

	@Override
	public void rollback(EndConversation action, GatServiceResult result, ExecutionContext context) throws DispatchException {
		// nothing to rollback
	}

}
