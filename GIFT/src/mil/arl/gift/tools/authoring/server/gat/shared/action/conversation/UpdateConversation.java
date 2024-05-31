/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for retrieving a conversation update from the server.
 * 
 * @author bzahid
 */
public class UpdateConversation implements Action<UpdateConversationResult>{

	/**
	 * The text submitted by the user
	 */
	private String userText;
	
	/**
	 * The chat id
	 */
	private int chatId = -1;
	
	/** 
	 * A string representation of the conversation properties 
	 */
	private String conversationJSONStr;
	
	/** 
	 * A string representation of the conversation tree 
	 */
	private String conversationTreeJSONStr;
	
	/**
	 * No arg constructor required for serialization.
	 */
	public UpdateConversation() {
	}

	/**
	 * Sets the user text.
	 * 
	 * @param userText The text entered by the user.
	 */
	public void setUserText(String userText){
		this.userText = userText;
	}
	
	/**
	 * Gets the user text.
	 * 
	 * @return The text entered by the user. Can be null.
	 */
	public String getUserText() {
		return userText;
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
	
	/**
	 * Gets the string representation of the conversation properties.
	 * 
	 * @return the string representation of the conversation properties.
	 */
	public String getConversationJSONStr() {
		return conversationJSONStr;
	}

	/**
	 * Sets the string representation of the conversation properties.
	 * 
	 * @param conversationJSONStr the string representation of the conversation properties.
	 */
	public void setConversationJSONStr(String conversationJSONStr) {
		this.conversationJSONStr = conversationJSONStr;
	}
	
	/**
	 * Gets the string representation of the conversation tree.
	 * 
	 * @return  the string representation of the conversation tree.
	 */
	public String getConversationTreeJSONStr() {
		return conversationTreeJSONStr;
	}
	
	/**
	 * Sets the string representation of the conversation tree.
	 * 
	 * @param conversationJSONStr the string representation of the conversation tree.
	 */
	public void setConversationTreeJSONStr(String conversationTreeJSONStr) {
		this.conversationTreeJSONStr = conversationTreeJSONStr;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[UpdateConversation: ");
        sb.append("userText = ").append(userText);
        sb.append(", chatId = ").append(chatId);
        sb.append(", conversationJSONStr = ").append(conversationJSONStr);
        sb.append(", conversationTreeJSONStr = ").append(conversationTreeJSONStr);
        sb.append("]");

        return sb.toString();
    } 
}
