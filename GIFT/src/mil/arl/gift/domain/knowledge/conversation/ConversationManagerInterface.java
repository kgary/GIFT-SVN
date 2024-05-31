/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import mil.arl.gift.common.ChatLog;

/**
 * This interface is for all conversation model managers.
 * 
 * @author mhoffman
 *
 */
public interface ConversationManagerInterface {

    /**
     * Update the underlying conversational model with the latest learner input to the conversation.
     * 
     * @param chatLog contains information about the chat thus far including the last learner's input to the chat
     */
    public void addUserResponse(ChatLog chatLog);
    
    /**
     * Start the conversation associated with the unique conversation id.
     * 
     * @param chatId a unique identifier of a conversation in this domain module instance.
     */
    public void startConversation(int chatId);
    
    /**
     * Stop the conversation associated with the unique conversation id.
     * 
     * @param chatId a unique identifier of a conversation in this domain module instance.
     */
    public void stopConversation(int chatId);
    
    /**
     * Stop all conversations known to this manager instance.
     */
    public void stopAllConversations();
}
