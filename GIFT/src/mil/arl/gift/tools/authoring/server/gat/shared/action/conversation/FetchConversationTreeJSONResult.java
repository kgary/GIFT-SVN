/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A result containing a JSON string representation of 
 * the conversation tree from a Conversation Tree file.
 * 
 * @author bzahid
 */
public class FetchConversationTreeJSONResult extends GatServiceResult {

	/** A JSON string representation of the conversation tree */
	private String conversationTreeJSON;
	
	/**
	 * Instantiates a new result
	 */
	public FetchConversationTreeJSONResult() {
		super();
	}

	/**
	 * Gets the string representation of the conversation tree.
	 * 
	 * @return the conversationTreeJSON
	 */
	public String getConversationTreeJSON() {
		return conversationTreeJSON;
	}

	/**
	 * Sets the string representation of the conversation tree.
	 * 
	 * @param conversationTreeJSON the string representation of the conversation tree.
	 */
	public void setConversationTreeJSON(String conversationTreeJSON) {
		this.conversationTreeJSON = conversationTreeJSON;
	}
	
}
