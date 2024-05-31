/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.conversation;

import generated.conversation.Conversation;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event indicating that a conversation was loaded.
 *
 * @author bzahid
 */
public class ConversationLoadedEvent extends GenericEvent {

	/** The conversation. */
	Conversation conversation;
	
	/**
	 * Creates a new event indicating that a conversation was loaded.
	 *
	 * @param conversation the conversation that was loaded
	 */
	public ConversationLoadedEvent(Conversation conversation){
		this.conversation = conversation;
	}
	
	/**
	 * Gets the conversation that was loaded.
	 *
	 * @return the conversation that was loaded
	 */
	public Conversation getConversation(){
		return conversation;
	}
}
