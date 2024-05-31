/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

/**
 * This interface is used by all conversation models.
 * 
 * @author mhoffman
 *
 */
public interface ConversationModelInterface {

    /**
     * Start the conversation.  Should only be called once.
     */
    public void start();
    
    /**
     * Stop the conversation.  The conversation should cleanup any resources
     * it no longer needs like threads or socket connections.
     */
    public void stop();
    
    /**
     * The underlying conversation model should deliver the next conversation actions
     * to the appropriate interface (e.g. show the next tutor conversation element like a 
     * question).
     */
    public void deliverNextActions();
}
