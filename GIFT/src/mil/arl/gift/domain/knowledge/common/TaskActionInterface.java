/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.List;

import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.TriggerEventManager.TaskAssessmentEvent;

/**
 * This interface is used by tasks to notify the scenario when task actions or events have happened.
 * 
 * @author mhoffman
 *
 */
public interface TaskActionInterface {
    
    /**
     * Notification that a task has started
     * 
     * @param task - the task that started
     */
    public void taskStarted(Task task);

    /**
     * Notification that a task has ended
     * 
     * @param task - the task that ended
     */
    public void taskEnded(Task task);
    
    /**
     * Notification that a task assessment has been created
     * 
     * @param taskAssessmentEvent - contains details about the task assessment event, can't be null.
     */
    public void taskAssessmentCreated(TaskAssessmentEvent taskAssessmentEvent);    
    
    /**
     * Notification that a concept has started
     * 
     * @param ancestorTask - the task containing the concept that has started
     * @param concept - the concept that started
     */
    public void conceptStarted(Task ancestorTask, Concept concept);

    /**
     * Notification that a concept has ended
     * 
     * @param ancestorTask - the task containing the concept that has ended
     * @param concept - the concept that ended
     */
    public void conceptEnded(Task ancestorTask, Concept concept);
    
    /**
     * Notification that a concept assessment has been created
     * 
     * @param ancestorTask the task containing the concept whose assessment was updated
     * @param concept the concept whose assessment was updated
     */
    public void conceptAssessmentCreated(Task ancestorTask, Concept concept);
    
    /**
     * A task is requesting additional information from the training application.  Examples:
     * 1. line of sight (LoS) queries
     * 2. weapon state 
     * 
     * @param infoRequest contains details needed by the training application to retrieve the appropriate information for the condition.
     */
    public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest);
    
    /**
     * The domain assessment model is requesting some type of interaction with the learner (e.g. show information
     * as feedback, lesson material, conversation update).
     * 
     * @param action - contains information about the action to take.  
     * {@link ActionsResponse}
     * {@link AssessmentStrategy}
     * {@link ApplicationMessage}
     * {@link ConversationTreeAction}
     * {@link ConversationTreeActions}
     */
    public void handleDomainActionWithLearner(DomainAssessmentContent action);
    
    /**
     * Notification that a survey needs to be presented to the learner.
     * 
     * @param surveyAssessment Contains information about the survey to present.
     * @param surveyResultListener used to provide the results of a survey  
     */
    public void displayDuringLessonSurvey(AbstractSurveyLessonAssessment surveyAssessment, SurveyResultListener surveyResultListener);
    
    /**
     * Notification that the task had a fatal error and it needs to be handled.
     * 
     * @param reason user friendly message describing why the session is ending
     * @param details a more developer friendly message describing why the session is ending
     */
    public void fatalError(String reason, String details);
    
    /**
     * Return information on all the members that are part of this running session.
     *  
     * @return could contain a single member for sessions that only contain a single playable team member
     * in the team organization, a single member for sessions that only contain a single player in a multi-player
     * possible team organization, or multiple members for a session that supports multi-player team organization.
     */
    public SessionMembers getSessionMembers();
    
    /**
     * Return the list of all the messages being played back for a domain knowledge session.
     * @return optional list of playback messages that if populated contain all of the messages
     * that are about to be played back in actual recorded time sequence as part of this 
     * domain knowledge part of the course.  Can be null or empty.</br>
     * In the future this should be a stream that is not directly associated with
     * the collection of messages being played back.  Until then, callers should
     * NOT manipulate this collection.
     */
    public List<MessageManager> getPlaybackMessages();
    
}
