/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchConversationTreeJSON;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchConversationTreeJSONResult;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.ConversationHelper;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler to retrieve a JSON string representation of the conversation tree from a Conversation Tree file.
 * 
 * @author bzahid
 */
public class FetchConversationTreeJSONHandler implements ActionHandler<FetchConversationTreeJSON, FetchConversationTreeJSONResult>{

	/** The logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchConversationTreeJSONHandler.class);
	
	@Override
	public FetchConversationTreeJSONResult execute(FetchConversationTreeJSON action, ExecutionContext context)
			throws DispatchException {
		 long start = System.currentTimeMillis();
		 logger.info("execute()");

		 FetchConversationTreeJSONResult result = new FetchConversationTreeJSONResult();
		 
        try {
        	JSONObject jsonObj = ConversationUtil.toJSON(action.getConversation());
        	result.setSuccess(true);
        	result.setConversationTreeJSON(jsonObj.getJSONObject(ConversationHelper.TREE_KEY).toString());
        	
        } catch (DetailedException e) {
        	result.setSuccess(false);
        	result.setErrorMsg(e.getReason());
        	result.setErrorDetails(e.getDetails());
        	
        } catch (Exception e) {
        	result.setSuccess(false);
        	result.setErrorMsg("An error occurred while loading the conversation tree.");
        	result.setErrorDetails(e.getMessage());
        }
        
        MetricsSenderSingleton.getInstance().endTrackingRpc("conversation.FetchConversationTreeJSON", start);
		return result;
	}

	@Override
	public Class<FetchConversationTreeJSON> getActionType() {
		return FetchConversationTreeJSON.class;
	}

	@Override
	public void rollback(FetchConversationTreeJSON action, FetchConversationTreeJSONResult result, ExecutionContext context)
			throws DispatchException {
		// nothing to rollback
	}

}
