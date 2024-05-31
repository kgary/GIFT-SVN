/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;

import generated.conversation.Conversation;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for retrieving a JSON conversation tree from a Conversation Tree file.
 * 
 * @author bzahid
 */
public class FetchConversationTreeJSON implements Action<FetchConversationTreeJSONResult>{

	/** The conversation file */
	private Conversation conversation;
	
	/**
	 * No arg constructor required for serialization.
	 */
	public FetchConversationTreeJSON() {
	}

	/**
	 * Gets the conversation file. 
	 * 
	 * @return the conversation file.
	 */
	public Conversation getConversation() {
		return conversation;
	}

	/**
	 * Sets the conversation file.
	 * 
	 * @param conversation The conversation file.
	 */
	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchConversationTreeJSON: ");
        sb.append("conversation = ").append(conversation.toString());
        sb.append("]");

        return sb.toString();
    } 
}
