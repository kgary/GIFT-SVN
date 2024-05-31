/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;

/**
 * A callback that handles the information retrieved from a conversation update.
 * 
 * @author bzahid
 */
public interface ConversationUpdateCallback {

	/**
	 * Notifies the caller with the conversation information.
	 * 
	 * @param chatId The chat id
	 * @param tutorText A list of messages to be displayed to the user. Can be null.
	 * @param choices A list of choices to display to the user. Can be null.
	 * @param endConversation Whether or not the conversation is over.
	 */
	void notify(int chatId, List<String> tutorText, List<String> choices, boolean endConversation);
	
	/**
	 * Notifies the caller that the update has failed
	 * 
	 * @param result The result containing information about the cause.
	 */
	void failure(UpdateConversationResult result);	
}
