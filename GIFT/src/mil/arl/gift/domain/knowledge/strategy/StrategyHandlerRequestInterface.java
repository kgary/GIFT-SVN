/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.strategy;

import java.util.List;
import java.util.Map;

import generated.dkf.LessonMaterialList;
import generated.dkf.Message;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.net.api.MessageCollectionCallback;

/**
 * This interface is used by Strategy Handlers to make request on its behalf 
 * (e.g. change time of day to midnight in the training application, present survey on TUI)
 * 
 * @author mhoffman
 *
 */
public interface StrategyHandlerRequestInterface {

    /**
     * Notification that feedback needs to presented to the learner through the Tutor User Interface.
     * 
     * @param feedback - the feedback message to present
     * @param showFeedbackHandler - used to notify the caller when the tutor is ready to receive more feedback requests.  When initially
     * loading a character in the tutor this response can take longer.  Can be null if no callback is needed.
     */
    void handleFeedbackUsingTUI(TutorUserInterfaceFeedback feedback, MessageCollectionCallback showFeedbackHandler);
    
    /**
     * Notification that feedback needs to presented to the learner through the Training Application.
     * 
     * @param argument - the information needed by the gateway in order to service the feedback request using the training application.
     */
    void handleFeedbackUsingTrainingApp(Message argument);
    
    /**
     * Notification that media needs to presented to the learner through the Tutor User Interface. Note that presenting 
     * mid-lesson media will pause the pedagogical request that triggered it until the learner finishes viewing all of the
     * media being presented, so any subsequent strategies after the mid-lesson media in the same pedagogical request will
     * only begin executing once the learner has finished the mid-lesson media.<br/>
     * Note: this method will hold onto the calling thread until the mid lesson media presentation is finished, i.e. the 
     * learner selects the continue button.
     * 
     * @param list - the media to present
     */
    void handleMediaUsingTUI(LessonMaterialList list);
    
    /**
     * Apply the scenario adaptation type
     * 
     * @param type - the scenario adaptation info to apply in this scenario adaptation
     * @param strategyStress an optional value of stress associated with this strategy. Can be null.
     * See ProxyTaskAssessment for ranges on stress value.
     */
    void handleScenarioAdaptation(generated.dkf.EnvironmentAdaptation type, Double strategyStress);
    
    /**
     * Notification that a survey needs to be presented to the learner.
     * Note: this method will hold onto the calling thread until the mid lesson survey presentation is finished, i.e. the 
     * learner submits the survey responses.
     * 
     * @param surveyAssessment Contains information about the survey to present.
     * @param surveyResultListener used to provide the results of a survey  
     */
    void displayDuringLessonSurvey(AbstractSurveyLessonAssessment surveyAssessment, SurveyResultListener surveyResultListener);
    
    /**
     * Apply an update to the current chat window.
     * 
     * @param request - the chat window update information
     */
    void handleTutorChatUpdate(DisplayChatWindowUpdateRequest request);
    
    /**
     * Notification that a conversation should be started as a strategy implementation.
     * 
     * @param conversationName the name of the conversation to show to the learner.  Can't be null or empty.
     * @param conversation - information about a conversation to deliver to the learner
     */
    void handleConversationRequest(String conversationName, generated.dkf.Conversation conversation);
    
    /**
     * Sends a {@link AuthorizeStrategiesRequest} for a provided list of
     * {@link StrategyToApply}.
     *
     * @param strategies The {@link List} of {@link StrategyToApply} for which to
     *        request execution.
     * @param domainSession The {@link DomainSession} for which the strategies
     *        are being requested.
     */
    void sendAuthorizeStrategiesRequest(Map<String, List<StrategyToApply>> strategies, DomainSession domainSession);
}
