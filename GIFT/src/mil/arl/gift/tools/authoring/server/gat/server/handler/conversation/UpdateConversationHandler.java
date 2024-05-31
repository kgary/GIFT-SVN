/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler to retrieve a conversation update.
 * 
 * @author bzahid
 */
public class UpdateConversationHandler implements ActionHandler<UpdateConversation, UpdateConversationResult>{

    private static final String METRICS_TAG = "conversation.UpdateConversation";
	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(UpdateConversationHandler.class);

	@Override
	public UpdateConversationResult execute(UpdateConversation action, ExecutionContext context) throws DispatchException {
		
		final long start = System.currentTimeMillis();
		logger.info("execute()");

		final AsyncReturnBlocker<UpdateConversationResult> returnBlocker = new AsyncReturnBlocker<UpdateConversationResult>();
        ConversationUpdateManager.getNextUpdate(action, new ConversationUpdateCallback() {

			@Override
			public void notify(int chatId, List<String> tutorText, List<String> choices, boolean endConversation) {
				
				UpdateConversationResult result = new UpdateConversationResult();
				
				result.setSuccess(true);
				result.setChatId(chatId);
				result.setChoices(choices);
				result.setTutorText(tutorText);
				result.setEndConversation(endConversation);
				
				MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
				returnBlocker.setReturnValue(result);				
			}
			
			@Override
			public void failure(UpdateConversationResult result) {
				
				MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
				returnBlocker.setReturnValue(result);
			}
        	
        });

		return returnBlocker.getReturnValueOrTimeout();
	}

	@Override
	public Class<UpdateConversation> getActionType() {
		return UpdateConversation.class;
	}

	@Override
	public void rollback(UpdateConversation action, UpdateConversationResult result, ExecutionContext context) throws DispatchException {
		// nothing to rollback
	}

}
