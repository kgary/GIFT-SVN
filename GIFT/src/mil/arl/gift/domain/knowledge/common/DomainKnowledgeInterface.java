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
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;

/**
 * This interface is used by domain knowledge for notifications on domain knowledge actions or events
 * 
 * @author mhoffman
 *
 */
public interface DomainKnowledgeInterface {
    
    /**
     * Notification that the domain has started
     */
    public void domainStarted();

    /**
     * Notification that the domain has ended
     * @param status information about why the domain knowledge session is ending.
     */
    public void domainEnded(LessonCompletedStatusType status);
    
    /**
     * Notification that a survey needs to be presented to the learner.
     * 
     * @param surveyAssessment Contains information about the survey to present.
     * @param surveyResultListener used to provide the results of a survey  
     */
    public void displayDuringLessonSurvey(AbstractSurveyLessonAssessment surveyAssessment, SurveyResultListener surveyResultListener);
    
    /**
     * Notification that a performance assessment was created
     * 
     * @param performanceAssessment - the performance assessment that was created
     */
    public void performanceAssessmentCreated(PerformanceAssessment performanceAssessment);
    
    /**
     * The lesson assessment is requesting additional information from the training application.  Examples:
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
     * @param information - contains information about the action to take.  
     * {@link ActionsResponse}
     * {@link AssessmentStrategy}
     * {@link ApplicationMessage}
     * {@link ConversationTreeAction}
     * {@link ConversationTreeActions}
     */
    public void handleDomainActionWithLearner(DomainAssessmentContent information);
    
    /**
     * Notification that the domain had a fatal error and it needs to be handled.
     * 
     * @param reason user friendly message describing why the session is ending
     * @param details a more developer friendly message describing why the session is ending
     */
    public void fatalError(String reason, String details);
    
    /**
     * Return information on all the members that are part of this running session.
     *  
     * @return could return one of the following <br/>
     * 1. a single member for sessions that only contain a single playable team member
     * in the team organization<br/>
     * 2. a single member for sessions that only contain a single player in a multi-player
     * possible team organization<br/>
     * 3. multiple members for a session that supports multi-player team organization<br/>
     * 4. null if there are no members found
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
    
    /**
     * Set the collection of all messages being played back for this domain knowledge session.
     * @param playbackMessages optional collection of messages being played back.  Can be null or empty if this
     * is not a playback session.
     */
    void setPlaybackMessages(List<MessageManager> playbackMessages);
}
