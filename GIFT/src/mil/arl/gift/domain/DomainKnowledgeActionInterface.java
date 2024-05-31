/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;

/**
 * This interface is used by domain knowledge manager to notify domain session when domain knowledge actions or events have happened.
 * 
 * @author jleonard
 */
public interface DomainKnowledgeActionInterface {
    
    /**
     * Notification that the domain knowledge scenario has started
     */
    public void scenarioStarted();
    
    /**
     * Notification that the domain knowledge scenario has completed
     * @param lessonCompleted information about why the scenario is ending.
     */
    public void scenarioCompleted(LessonCompleted lessonCompleted);
    
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
     * @param action - contains information about the action to take.  
     * {@link ActionsResponse}
     * {@link AssessmentStrategy}
     * {@link ApplicationMessage}
     * {@link ConversationTreeAction}
     * {@link ConversationTreeActions}
     */
    public void handleDomainActionWithLearner(DomainAssessmentContent action);
    
    /**
     * Notification that the assessment knowledge had a fatal error and it needs to be handled.
     * 
     * @param reason user friendly message describing why the session is ending
     * @param details a more developer friendly message describing why the session is ending
     */
    public void fatalError(String reason, String details);
}
