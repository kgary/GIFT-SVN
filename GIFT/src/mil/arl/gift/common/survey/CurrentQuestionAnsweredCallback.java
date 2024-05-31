/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

/**
 * Used for notification that the learner answered a question.
 * 
 * @author mhoffman
 *
 */
public interface CurrentQuestionAnsweredCallback{
    
    /**
     * Notification that a survey question was answered by the learner.
     * 
     * @param response contains the survey question response
     */
    public void questionAnswered(AbstractQuestionResponse response);
}