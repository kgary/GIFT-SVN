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
 * An event indicating the conversation tree editor was selected.
 * 
 * @author bzahid
 */
public class TreeSelectedEvent extends GenericEvent {
    
    private Conversation conversation;

	/**
	 * Creates a new event indicating that the tree editor was selected
	 *
	 * @param conversation the conversation that was loaded
	 */
    public TreeSelectedEvent(Conversation conversation) {
        super();
        this.conversation = conversation;
    }

    /**
     * @return the conversation
     */
    public Conversation getConversation() {
        return conversation;
    }    
}
