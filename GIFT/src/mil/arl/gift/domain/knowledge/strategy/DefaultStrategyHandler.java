/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.strategy;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.Audio;
import generated.dkf.BooleanEnum;
import generated.dkf.Conversation;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.Feedback;
import generated.dkf.InTutor;
import generated.dkf.MediaSemantics;
import generated.dkf.Message;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.DisplayHTMLFeedbackAction;
import mil.arl.gift.common.DisplayScriptedAvatarAction;
import mil.arl.gift.common.DisplayTextAction;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.common.PlayAudioAction;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.DoNothingStrategy;
import mil.arl.gift.common.course.strategy.InstructionalInterventionStrategy;
import mil.arl.gift.common.course.strategy.MidLessonMediaStrategy;
import mil.arl.gift.common.course.strategy.ScenarioAdaptationStrategy;
import mil.arl.gift.common.io.CharacterServiceUtil;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;

/**
 * This class is responsible for selecting a strategy instance to implement from a Pedagogical requested strategy
 * during a domain session.
 *
 * @author mhoffman
 *
 */
public class DefaultStrategyHandler implements StrategyHandlerInterface {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DefaultStrategyHandler.class);

    private static final String DEFAULT_CONVERSATION_NAME = "Tutor Initiated";

    /** 
     * having a private static instantiated 'instance' variable is required for StrategyHandlerInterface implementations
     * This 'instance' is used in @see {@link AbstractPedagogicalRequestHandler#getHandler(String)}
     */
    @SuppressWarnings("unused")
    private static final StrategyHandlerInterface instance = new DefaultStrategyHandler();

    private static final DesktopFolderProxy domainFolder = new DesktopFolderProxy(new File(DomainModuleProperties.getInstance().getDomainDirectory()));

    @Override
    public void handleScenarioAdaptation(ScenarioAdaptationStrategy scenarioAdaptation,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface) {

        EnvironmentAdaptation environmentAdaptation = scenarioAdaptation.getType();
        strategyHandlerRequestInterface.handleScenarioAdaptation(environmentAdaptation, scenarioAdaptation.getStress());
    }

    @Override
    public void handleInstructionalIntervention(InstructionalInterventionStrategy instructionalInterv,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface, DesktopFolderProxy courseFolder) {

        Feedback feedback = instructionalInterv.getFeedback();

        if (feedback.getFeedbackPresentation() instanceof Message) {
            //
            // handle feedback message
            //

            Message message = (Message) feedback.getFeedbackPresentation();
            
            // ignore messages if intended for the controller only
            if (message.getDelivery() != null && message.getDelivery().getToObserverController() != null && CollectionUtils.isEmpty(feedback.getTeamRef())) {
                return;
            }

            /* Create an action to handle message text in some fashion (e.g. TUI
             * display text, TUI avatar text to speech, in Training App) */
            DisplayTextAction textAction = new DisplayTextAction(message.getContent());
            TutorUserInterfaceFeedback tuiFeedback = new TutorUserInterfaceFeedback(textAction, null,
                    new DisplayTextToSpeechAvatarAction(message.getContent()), null, null);

            //
            // Handle delivery settings
            //
            handleMessageDelivery(message, textAction, tuiFeedback, strategyHandlerRequestInterface);

        } else if (feedback.getFeedbackPresentation() instanceof Audio) {
            //
            // Handle playing audio
            //

            Audio tuiAudio = (Audio) feedback.getFeedbackPresentation();

            // ignore messages if intended for the controller only
            if (tuiAudio.getToObserverController() != null && (feedback.getTeamRef() == null || feedback.getTeamRef().isEmpty())) {
                return;
            }

            /* The expected behavior right now is to not have an avatar
             * displayed when audio is played */
            TutorUserInterfaceFeedback tuiFeedback = new TutorUserInterfaceFeedback(null,
                    new PlayAudioAction(tuiAudio.getMP3File(), tuiAudio.getOGGFile()), null, null, null);

            strategyHandlerRequestInterface.handleFeedbackUsingTUI(tuiFeedback, null);

        } else if (feedback.getFeedbackPresentation() instanceof MediaSemantics) {
            //
            // Handle a media semantics character action
            //

            MediaSemantics tuiAvatar = (MediaSemantics) feedback.getFeedbackPresentation();

            /* Generate the avatar file relative to the domain module server */
            String runtimeCourseFolderRelativePath = FileFinderUtil
                    .getRelativePath(domainFolder.getFolder(), courseFolder.getFolder());
            AvatarData domainAvatar = CharacterServiceUtil
                    .generateCharacter(runtimeCourseFolderRelativePath, tuiAvatar.getAvatar());

            /* The expected behavior right now is to display the avatar as well
             * as handle any optionally authored text */
            TutorUserInterfaceFeedback tuiFeedback = new TutorUserInterfaceFeedback(null, null,
                    new DisplayScriptedAvatarAction(domainAvatar, tuiAvatar.getKeyName()), null, null);

            //
            // Handle delivery settings
            //
            if (tuiAvatar.getMessage() != null) {

                Message message = tuiAvatar.getMessage();

                /* Create an action to handle message text in some fashion (e.g.
                 * TUI display text, TUI avatar text to speech, in Training
                 * App) */
                DisplayTextAction textAction = new DisplayTextAction(message.getContent());
                tuiFeedback.setDisplayTextAction(textAction);

                //
                // Handle delivery settings
                //
                handleMessageDelivery(message, textAction, tuiFeedback, strategyHandlerRequestInterface);
            } else {
                strategyHandlerRequestInterface.handleFeedbackUsingTUI(tuiFeedback, null);
            }

        } else if (feedback.getFeedbackPresentation() instanceof generated.dkf.Feedback.File) {

            String networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress();
            generated.dkf.Feedback.File fbFile = (generated.dkf.Feedback.File) feedback.getFeedbackPresentation();
            String filename = fbFile.getHTML().trim();

            /* need to add course folder path from Domain folder to get domain
             * hosted path to HTML file */
            String runtimeCourseFolderRelativePath = FileFinderUtil
                    .getRelativePath(domainFolder.getFolder(), courseFolder.getFolder());
            String fileUrl = runtimeCourseFolderRelativePath + Constants.FORWARD_SLASH + filename;

            /* firefox browsers do not automatically replace backslashes,
             * resulting in invalid urls */
            fileUrl = UriUtil.makeURICompliant(fileUrl);

            fileUrl = networkURL + Constants.FORWARD_SLASH + fileUrl;

            TutorUserInterfaceFeedback tuiFeedback = new TutorUserInterfaceFeedback(null, null, null, null,
                    new DisplayHTMLFeedbackAction(fileUrl));

            strategyHandlerRequestInterface.handleFeedbackUsingTUI(tuiFeedback, null);

        } else {

            logger.error("Received unhandled instructional intervention request of "
                    + feedback.getFeedbackPresentation());
        }
    }

    @Override
    public void handleMidLessonMedia(MidLessonMediaStrategy media,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface, DesktopFolderProxy courseFolder) {

        strategyHandlerRequestInterface.handleMediaUsingTUI(media.getMediaList());
    }

    /**
     * Process the message delivery settings by updating the appropriate attributes in the
     * text action as well handling how the message is delivered (i.e. TUI, Training App).
     *
     * @param tuiMessage the authored message information including content and delivery settings
     * @param textAction the action containing the message text to be handled
     * @param tuiFeedback the container for the various types of actions that need to take place to handle the feedback
     * (i.e. display text, play audio, text to speech)
     * @param strategyHandlerRequestInterface the interface responsible for executing the feedback request (i.e. send
     * the appropriate information to the module that will handle presenting the feedback)
     */
    private void handleMessageDelivery(Message tuiMessage, DisplayTextAction textAction,
    		TutorUserInterfaceFeedback tuiFeedback, StrategyHandlerRequestInterface strategyHandlerRequestInterface){

        //
        // Handle delivery settings
        //
        if(tuiMessage.getDelivery() != null){

            if(tuiMessage.getDelivery().getInTutor() != null){
                //delivery feedback through TUI

                InTutor inTutor = tuiMessage.getDelivery().getInTutor();
                textAction.setDeliverySettings(inTutor);
                strategyHandlerRequestInterface.handleFeedbackUsingTUI(tuiFeedback, null);
            }

            //check whether to send the feedback to the training application
            //Note: this maybe in addition to sending it to the TUI
            if(tuiMessage.getDelivery().getInTrainingApplication() != null &&
                    tuiMessage.getDelivery().getInTrainingApplication().getEnabled() == BooleanEnum.TRUE){

                //send feedback message to Gateway which may/may-not support presenting the feedback
                strategyHandlerRequestInterface.handleFeedbackUsingTrainingApp(tuiMessage);

            }else if(tuiMessage.getDelivery().getInTutor() == null){
                /* make sure feedback is at least delivered through the TUI In
                 * this case the delivery object is present but contains no
                 * information */

                strategyHandlerRequestInterface.handleFeedbackUsingTUI(tuiFeedback, null);
            }

        }else{
            strategyHandlerRequestInterface.handleFeedbackUsingTUI(tuiFeedback, null);
        }
    }

    @Override
    public void handleRequestForPerformanceAssessment(
            AbstractPerformanceAssessmentNode node,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface) {

        node.handlePerformanceAssessmentRequest(strategyHandlerRequestInterface);
    }


    @Override
    public void handleRequestForPerformanceAssessment(
            Conversation conversation,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface) {

        strategyHandlerRequestInterface.handleConversationRequest(DEFAULT_CONVERSATION_NAME, conversation);
    }


    @Override
    public void handleBranchAdaptation(
            BranchAdaptationStrategy branchAdaptation,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface) {

        //this is not handled by the default strategy handler but inherently by the CourseManager
    }

    @Override
    public void handleDoNothing(DoNothingStrategy doNothingStrategy) {
        //nothing to do

    }
}