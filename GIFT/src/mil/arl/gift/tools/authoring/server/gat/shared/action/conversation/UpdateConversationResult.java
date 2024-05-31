/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A result containing the conversation update from the server.
 * 
 * @author bzahid
 */
public class UpdateConversationResult extends GatServiceResult {

	/**
	 * The chat id
	 */
	private int chatId;
	
	/**
	 * The text entries to display.
	 */
	private List<String> tutorText;
	
	/**
	 * A list of choices that should be presented to the user.
	 */
	private List<String> choices;
	
	/**
	 * Whether or not the conversation has ended.
	 */
	private boolean endConversation;
	
	/**
     * No-arg constructor. Needed for serialization.
     */
    public UpdateConversationResult() {
    }

	/**
	 * Gets the chat id
	 * 
	 * @return the chat id
	 */
	public int getChatId() {
		return chatId;
	}

	/**
	 * Sets the chat id
	 * 
	 * @param chatId The chat id to set
	 */
	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	/**
	 * Gets the next text entries in the conversation tree.
	 * 
	 * @return the next text entries in the conversation tree.
	 */
	public List<String> getTutorText() {
		return tutorText;
	}

	/**
	 * Sets the text entries to display to the user.
	 * 
	 * @param tutorText The next text to display to the user.
	 */
	public void setTutorText(List<String> tutorText) {
		this.tutorText = tutorText;
	}

	/**
	 * Gets a list of choices that should be presented to the user.
	 * 
	 * @return a list of choices. Can be null.
	 */
	public List<String> getChoices() {
		return choices;
	}

	/**
	 * Sets a list of choices that should be presented to the user.
	 * 
	 * @param choices The choices to set.
	 */
	public void setChoices(List<String> choices) {
		this.choices = choices;
	}

	/**
	 * Gets whether or not the conversation has ended.
	 * 
	 * @return true if the conversation is over, false otherwise.
	 */
	public boolean endConversation() {
		return endConversation;
	}

	/**
	 * Sets whether or not the conversation has ended.
	 * 
	 * @param endConversation true if the conversation is over, false otherwise.
	 */
	public void setEndConversation(boolean endConversation) {
		this.endConversation = endConversation;
	}

	
}
