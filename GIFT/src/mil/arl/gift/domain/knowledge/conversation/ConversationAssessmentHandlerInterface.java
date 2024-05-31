/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import java.util.List;

import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;

/**
 * This interface is used by implementations that can handle conversation assessment results
 * by applying them to performance assessment knowledge.
 * 
 * @author mhoffman
 *
 */
public interface ConversationAssessmentHandlerInterface {

    /**
     * Handle the assessments of a conversation between the learner and GIFT.
     * This will update performance node's assessment value if the assessment confidence 
     * is high enough to warrant an update.
     * 
     * @param assessments contains concept assessment information for choices the learner has made in the conversation.
     * Can't be null or empty.
     */
    public void assessPerformanceFromConversation(List<ConversationAssessment> assessments);
}
