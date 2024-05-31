/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for stopping a conversation on the server.
 * 
 * @author bzahid
 */
public class EndConversation implements Action<GatServiceResult>{

	/**
	 * The chat id
	 */
	private int chatId = -1;
			
	/**
	 * No arg constructor required for serialization.
	 */
	public EndConversation() {
	}
	
	/**
	 * Sets the chat id.
	 * 
	 * @param chatId the chat id
	 */
	public void setChatId(int chatId){
		this.chatId = chatId;
	}
	
	/**
	 * Gets the chat id
	 * 
	 * @return The chat id.
	 */
	public int getChatId() {
		return chatId;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[EndConversation: ");
        sb.append("chatId = ").append(chatId);
        sb.append("]");

        return sb.toString();
    } 
}
