/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.LessonMaterialList;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.AbstractAfterActionReviewEvent;
import mil.arl.gift.common.AbstractDisplayContentTutorRequest;
import mil.arl.gift.common.AfterActionReviewCourseEvent;
import mil.arl.gift.common.AfterActionReviewRemediationEvent;
import mil.arl.gift.common.AfterActionReviewSurveyEvent;
import mil.arl.gift.common.ApplyStrategyLearnerAction;
import mil.arl.gift.common.AssessMyLocationTutorAction;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.DisplaySurveyTutorRequest;
import mil.arl.gift.common.DisplayTextAction;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.common.ExplosiveHazardSpotReport;
import mil.arl.gift.common.NineLineReport;
import mil.arl.gift.common.PaceCountEnded;
import mil.arl.gift.common.PaceCountStarted;
import mil.arl.gift.common.RadioUsed;
import mil.arl.gift.common.SpotReport;
import mil.arl.gift.common.TutorMeLearnerTutorAction;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.score.AbstractAnswerScore;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.AbstractScaleScore;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore.ConceptOverallDetails;
import mil.arl.gift.common.survey.score.SurveyFeedbackScorer;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.LoadProgress;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.tutor.shared.AbstractAction;
import mil.arl.gift.tutor.shared.AbstractWidgetAction;
import mil.arl.gift.tutor.shared.ActionTypeEnum;
import mil.arl.gift.tutor.shared.AfterActionReviewDetailsNode;
import mil.arl.gift.tutor.shared.CloseAction;
import mil.arl.gift.tutor.shared.DialogInstance;
import mil.arl.gift.tutor.shared.DialogTypeEnum;
import mil.arl.gift.tutor.shared.DisplayDialogAction;
import mil.arl.gift.tutor.shared.DisplayWidgetAction;
import mil.arl.gift.tutor.shared.LessonStatusAction;
import mil.arl.gift.tutor.shared.LessonStatusAction.LessonStatus;
import mil.arl.gift.tutor.shared.ScenarioControls;
import mil.arl.gift.tutor.shared.StartCourseAction;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.UserAction;
import mil.arl.gift.tutor.shared.UserActionIconEnum;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetLocationEnum;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.AfterActionReviewWidgetProperties;
import mil.arl.gift.tutor.shared.properties.AvatarContainerWidgetProperties;
import mil.arl.gift.tutor.shared.properties.ChatWindowWidgetProperties;
import mil.arl.gift.tutor.shared.properties.CourseInitInstructionsWidgetProperties;
import mil.arl.gift.tutor.shared.properties.DisplayContentWidgetProperties;
import mil.arl.gift.tutor.shared.properties.DisplayMediaCollectionWidgetProperties;
import mil.arl.gift.tutor.shared.properties.DisplayMediaWidgetProperties;
import mil.arl.gift.tutor.shared.properties.DisplayMessageWidgetProperties;
import mil.arl.gift.tutor.shared.properties.FeedbackWidgetProperties;
import mil.arl.gift.tutor.shared.properties.SurveyWidgetProperties;
import mil.arl.gift.tutor.shared.properties.UserActionWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * The DomainWebState class represents the state of the user's domain session as
 * represented to the Tutor Web Client.  This class holds the state of the tutor web client.
 * Unlike other UI's where the client may manage it's own state, here the Tutor drives
 * and maintains the state of the domain and presents it to the client.
 *
 * By doing this, it allows functionality such as resuming the session from another browser.
 * In terms of relationship, the UserWebSession manages the DomainWebState in a 1 to 1 relationship.
 * No matter how many browsers may be opened, the user can only have on domain web state across all browsers.
 *
 * The logic in this class primarily deals with rendering and displaying widgets and managing the state of the widgets if requested
 * by the client.  The two primary widgets are the Article and Footer widgets.  Additionally it can send dialog messages
 * to the client.
 *
 *
 * @author jleonard
 */
public class DomainWebState  {

    /** The message text that is shown when the character is not responding */
    private static final DisplayTextAction CHARACTER_NOT_RESPONDING_MESSAGE = new DisplayTextAction(
            "<span style='color: red'>(Character is not responding)</span>");

    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(DomainWebState.class);

    /** information about the user session this web session is associated with */
    private final UserSession userSession;

    /** The parent user web session that owns the domain web state. */
    private final UserWebSession userWebSession;

    /** The id of the domain session being represented. */
    private final int domainSessionId;

    /**
     * The id of the experiment if this {@link DomainWebState} is for an
     * experiment. Otherwise, it is null
     */
    private final String experimentId;

    /** the selected domain runtime id which is the path to the course used when taking the course */
    private final String domainRuntimeId;

    /** the selected domain source id which is the path to the authored course */
    private final String domainSourceId;

    private final LinkedList<TutorUserInterfaceFeedback> feedback = new LinkedList<>();

    private boolean hasNewFeedback = false;

    /** The lesson status of the domain session's currently running training application */
    private LessonStatus lessonStatus = LessonStatus.INACTIVE;

    private boolean tutorActionsAvailable = false;

    private final Map<String, ActionListener> idToActionListener = new HashMap<>();

    /** a map to keep track of which conversations need to be updated. */
    private final Map<Integer, ActionListener> chatIdToActionListener = new HashMap<>();

    /** everything above the footer including a possible character */
    private WidgetInstance articleWidget = null;

    /** footer is below the article.  It is not always displayed.  It contains items like user actions during a training application (lesson) */
    private WidgetInstance footerWidget = null;

    /**
     * flag used to determine if the session's character is idle or not -
     * defaulting to false and forcing the client to notify the server when the
     * character is loaded and ready
     */
    private Boolean characterIdle = Boolean.FALSE;

    /** The object used to synchronize access to {@link #characterIdle} */
    private final Object characterIdleLock = new Object();

    /**
     * The flag used to indicate whether or not the character is responding. The
     * value is initialized to null to indicate an indeterminate state. The
     * value should only be changed once and must be changed to a non-null
     * value.
     */
    private boolean characterEnabled = true;

    /** The object used to synchronize access to {@link #characterEnabled} */
    private final Object characterEnabledLock = new Object();

    /**
     * The {@link Timer} used to determine if the character is actually
     * processing messages sent to it.
     */
    private Timer characterEnabledTimer = null;

    /**
     * FIFO queue of chat update requests, waiting for the character to be idle
     * before delivering them
     */
    private ArrayList<Serializable> chatUpdateQueue = new ArrayList<>();

    /** mapping of unique conversation id to the active chat request - used to update specific conversations when active for a user */
    private Map<Integer, ActiveChatWindowRequest> chatIdToChatRequest = new HashMap<>();

    /**
     * FIFO queue of chat update requests, waiting for the character to be idle
     * before delivering them
     */
    private ArrayDeque<TutorUserInterfaceFeedback> feedbackUpdateQueue = new ArrayDeque<>();

    private static boolean fullScreen = true;
    
    /** listeners to notified when the tutor character is idle */
    private List<CharacterIdleListener> characterIdleListeners = new ArrayList<>(0);

    /**
     * Constructor
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      this web session is associated with
     * @param domainSessionId The domain session ID
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param experimentId the unique experiment id.  Can be null if the domain session is not part of an experiment.
     */
    public DomainWebState(UserWebSession userWebSession, UserSession userSession, int domainSessionId, String domainRuntimeId, String domainSourceId, String experimentId) {
        this.userWebSession = userWebSession;
        this.userSession = userSession;
        this.domainSessionId = domainSessionId;
        this.domainRuntimeId = domainRuntimeId;
        this.domainSourceId = domainSourceId;
        this.experimentId = experimentId;
    }

    /**
     * Return the user session information for this web session instance.
     *
     * @return UserSession
     */
    public UserSession getUserSession(){
        return userSession;
    }

    /**
     * Gets the domain session ID of this session
     *
     * @return int the domain session ID of this session
     */
    public int getDomainSessionId() {
        return domainSessionId;
    }

    /**
     * Gets the selected domain runtime id which is the path to the course used when taking the course
     *
     * @return string
     */
    public String getDomainRuntimeId() {
        return domainRuntimeId;
    }

    /**
     * Gets the the selected domain source id which is the path to the authored course
     *
     * @return string
     */
    public String getDomainSourceId(){
        return domainSourceId;
    }

    /**
     * Return the experiment id for this domain session message
     *
     * @return the unique experiment id.  Will be null if the domain session is not part of an experiment.
     */
    public String getExperimentId(){
        return experimentId;
    }

    /**
     * Add an action listener for when actions happen to a widget instance
     *
     * @param id The id of the widget instance
     * @param listener The action listener
     */
    private final void registerActionListener(String id, ActionListener listener) {
        idToActionListener.put(id, listener);
    }

    /**
     * Add an action listener for a conversation widget instance
     *
     * @param id The unique chat id of the conversation.
     * @param listener The action listener
     */
    private final void registerChatActionListener(int id, ActionListener listener) {
        chatIdToActionListener.put(id, listener);
    }

    /**
     * Sets the status of the tutor actions widget. Used to determine whether or not
     * chat updates should be queued.
     *
     * @param isAvailable True if the user has tutor actions available, false otherwise.
     */
    public void setTutorActionsAvailable(boolean isAvailable) {
    	tutorActionsAvailable = isAvailable;
    }

    /**
     * Processes an action.  This typically updates the widget instance/state of the
     * domain session web state.
     *
     * @param action The action to be processed.
     */
    protected void processAction(AbstractAction action) {
        if (logger.isDebugEnabled()) {
            logger.debug("processAction() called with action: " + action);
        }

        if (action.getActionType() == ActionTypeEnum.DISPLAY_WIDGET) {
            applyDisplayWidgetAction((DisplayWidgetAction) action);
        } else if (action.getActionType() == ActionTypeEnum.DISPLAY_DIALOG) {
            DisplayDialogAction displayDialog = (DisplayDialogAction) action;
            if (displayDialog.getDialog() != null) {
                if (logger.isInfoEnabled()) {
                    logger.info(toString() + ": Displaying dialog " + displayDialog.getDialog());
                }
            } else {
                logger.error(toString() + ": Got a display dialog action with no dialog.");
            }
        } else if (action.getActionType() == ActionTypeEnum.AVATAR_IDLE) {
            if(logger.isInfoEnabled()){
                logger.info("Setting character to idle while processing the action " + action + ".");
            }
            characterIdleNotification();
        } else if (action.getActionType() == ActionTypeEnum.AVATAR_BUSY) {
            if(logger.isInfoEnabled()){
                logger.info("Setting character to busy while processing the action " + action + ".");
            }
            characterBusyNotification();
        } else if (action instanceof AbstractWidgetAction) {
            applyWidgetAction((AbstractWidgetAction) action);
        } else if (action.getActionType() == ActionTypeEnum.START_COURSE) {
            // do nothing.
        } else if (action.getActionType() == ActionTypeEnum.INIT_APP) {
            // do nothing. No domain state needs to be saved from initializing the embedded application
        } else if (action.getActionType() == ActionTypeEnum.SEND_APP_MESSASGE) {
            // do nothing. No state needs to be saved from sending a message to the embedded application
        } else if (action.getActionType() == ActionTypeEnum.LESSON_STATUS) {
            // do nothing. No state needs to be saved from the embedded training application being started or finished
        } else if (action.getActionType() == ActionTypeEnum.PRELOAD_AVATAR) {
            /* do nothing. No state needs to be saved from preloading the
             * character */
        } else {
            logger.error(toString() + ": Could not process action: " + action);
        }
    }

    /**
     * Applies a display widget action to the session
     *
     * @param action The display widget action to apply
     */
    private final void applyDisplayWidgetAction(DisplayWidgetAction action) {
        if (action.getWidgetInstance() != null) {
            if(logger.isInfoEnabled()){
                logger.info("Displaying page of \n" + action+"\n for "+toString());
            }
            if (action.getDisplayLocation() == WidgetLocationEnum.ARTICLE) {
                articleWidget = action.getWidgetInstance();
            } else if (action.getDisplayLocation() == WidgetLocationEnum.FOOTER) {
                if(logger.isInfoEnabled()){
                    logger.info("Instantiating the footer widget for "+toString());
                }
                footerWidget = action.getWidgetInstance();
            }
        }
    }

    /**
     * Applies a widget action to the widgets in this session
     *
     * @param widgetAction The widget action to apply
     */
    private final void applyWidgetAction(AbstractWidgetAction widgetAction) {
        if (articleWidget != null && articleWidget.getWidgetId().equals(widgetAction.getWidgetId())) {
        	setFullScreen(articleWidget.getWidgetProperties().getIsFullscreen());
            articleWidget.applyAction(widgetAction);
            if (widgetAction.getActionType() == ActionTypeEnum.CLOSE) {

                if(!lessonStatus.isLessonInactive()
                    && DisplayMediaCollectionWidgetProperties.getMediaHtmlList(articleWidget.getWidgetProperties()) != null) {

                    // if the user has just completed a mid-lesson media collection, return them to the user action widget,
                    // just like mid-lesson surveys
                    WidgetProperties widgetProperties = new WidgetProperties();
                    widgetProperties.setShouldUpdate(true);
                    widgetProperties.setIsFullscreen(true);
                    UserActionWidgetProperties.setShouldUsePreviousActions(widgetProperties, true);
                    displayWidget(WidgetTypeEnum.USER_ACTION_WIDGET, widgetProperties);
                }

                articleWidget = null;

                /* the close action indicates that this course transition is
                 * over and is being closed, therefore any queued character
                 * actions need to be cleared and the domain session character
                 * can be considered idle. This will enable the character action
                 * logic to be in a state where it can handle new character
                 * action requests. */
                if(logger.isInfoEnabled()){
                    logger.info("Clearing character actions applying close action for client widget.");
                }
                clearCharacterActions();

            }
        } else if (footerWidget != null && footerWidget.getWidgetId().equals(widgetAction.getWidgetId())) {
            footerWidget.applyAction(widgetAction);
            if (widgetAction.getActionType() == ActionTypeEnum.CLOSE) {

                if(logger.isInfoEnabled()){
                    logger.info("Clearing the footer widget.");
                }
                footerWidget = null;
            }
        }

        if (widgetAction.getWidgetId() != null) {
        	ActionListener listener = null;

        	if(widgetAction instanceof SubmitAction) {
        		WidgetProperties properties = ((SubmitAction)widgetAction).getProperties();
        		listener = chatIdToActionListener.get(ChatWindowWidgetProperties.getChatId(properties));

        		if(SurveyWidgetProperties.getAnswers(properties) != null && !lessonStatus.isLessonInactive()) {

        		    //if the user has just submitted a mid-lesson survey, return them to the user action widget
        			WidgetProperties widgetProperties = new WidgetProperties();
        			widgetProperties.setShouldUpdate(true);
        			widgetProperties.setIsFullscreen(true);
        			UserActionWidgetProperties.setShouldUsePreviousActions(widgetProperties, true);
        			displayWidget(WidgetTypeEnum.USER_ACTION_WIDGET, widgetProperties);
        		}
        	}

        	if(listener == null) {
        		listener = idToActionListener.get(widgetAction.getWidgetId());
        	}

            if (listener != null) {
                if (logger.isInfoEnabled()) {
                    logger.info(toString() + ": Applying " + widgetAction);
                }
                listener.onAction(widgetAction);
            }
        }
    }

    /**
     * Notifies the domain session that the lesson is initializing
     */
    public void lessonInitializing() {

        lessonStatus = LessonStatus.INITIALIZING;

        doServerAction(new LessonStatusAction(lessonStatus));
    }

    /**
     * Notifies the domain session that the lesson has started
     */
    public void lessonStarted() {

        lessonStatus = LessonStatus.ACTIVE;

        doServerAction(new LessonStatusAction(lessonStatus));
    }

    /**
     * Notifies the domain session that the lesson has finished.
     */
    public void lessonCompleted() {
        lessonStatus = LessonStatus.INACTIVE;

        if(footerWidget != null){
            doServerAction(new CloseAction(footerWidget.getWidgetId()));
        }

        doServerAction(new LessonStatusAction(lessonStatus));
    }

    /**
     * Notify the client that the current course object has been completed by
     * sending a close action to the client. This is necessary so that widgets
     * like the TutorActionsWidget can clean up by releasing the article
     * container as well as the character data thereby allowing subsequent
     * course objects to create new characters.
     */
    public void courseObjectCompleted(){

        if(articleWidget != null){
            doServerAction(new CloseAction(articleWidget.getWidgetId()));

            /* necessary so the tutor can queue any incoming conversation
             * request from the domain module while the tutor is reseting the
             * character data - this can happen when external app course object
             * is followed by a conversation tree course object */
            characterBusyNotification(false);
        }
    }

    /**
     * Notifies the tui that the course is ready to be started.
     */
    public void signalCourseReady() {
        // This action should only be handled when the tui is in embedded mode.
        doServerAction(new StartCourseAction());
    }

    /**
     * Display a web page in the session
     *
     * @param pageType The type of web page to display
     * @param properties The properties of the web page to display
     * @param location The location to display the widget
     * @param listener The action listener of the web page
     * @return String The web page ID
     */
    public String displayWidget(WidgetTypeEnum pageType, WidgetProperties properties, WidgetLocationEnum location, ActionListener listener) {
        String widgetId = displayWidget(pageType, properties, location);

        if (listener != null) {
        	if(pageType == WidgetTypeEnum.CHAT_WINDOW_WIDGET ||
        			AvatarContainerWidgetProperties.getWidgetType(properties) == WidgetTypeEnum.CHAT_WINDOW_WIDGET) {
        		registerChatActionListener(ChatWindowWidgetProperties.getChatId(properties), listener);
        	}

        	registerActionListener(widgetId, listener);
        }
        return widgetId;
    }

    /**
     * Display some text in the session
     *
     * @param displayContentRequest contains the content to display as guidance
     * @param listener The action listener of the page
     */
    public void displayArticleContent(AbstractDisplayContentTutorRequest displayContentRequest, ActionListener listener) {

        WidgetProperties properties = new WidgetProperties();
        properties.setIsFullscreen(displayContentRequest.isFullscreen());
        DisplayContentWidgetProperties.setParameters(properties, displayContentRequest);

        WidgetTypeEnum widgetType;
        if(displayContentRequest instanceof DisplayMessageTutorRequest) {
        	widgetType = WidgetTypeEnum.MESSAGE_WIDGET;

        	if(displayContentRequest == DisplayMessageTutorRequest.EMPTY_REQUEST) {
            	setTutorActionsAvailable(false);
            }

        } else {
        	widgetType = WidgetTypeEnum.MEDIA_WIDGET;
        }

        String pageId = displayWidget(widgetType, properties, WidgetLocationEnum.ARTICLE, listener);

        if (displayContentRequest.getDisplayDuration() > 0) {
        	//automatically remove the guidance page after the duration
        	Timer hideTextTimer = new Timer("hideTextTimer");
        	hideTextTimer.schedule(new CloseWidgetTimerTask(this, pageId), displayContentRequest.getDisplayDuration());
        }
    }

    /**
     * Display a survey in the session
     *
     * @param request The request to display a survey on the tutor.  Contains the survey to present.
     * @param listener The action listener of the page
     */
    public void displaySurvey(DisplaySurveyTutorRequest request, ActionListener listener) {

        WidgetProperties properties = new WidgetProperties();
        properties.setIsFullscreen(request.useFullscreen());
        properties.setHasFocus(true);
        
        SurveyWidgetProperties.setSurvey(properties, request.getSurvey());
        displayWidget(WidgetTypeEnum.SURVEY_WIDGET, properties, WidgetLocationEnum.ARTICLE, listener);
    }

    /**
     * Apply the survey response created by the learner answering the survey through
     * the training application to the currently displayed survey in the tutor.
     *
     * @param surveyResponse contains the answered questions
     */
    public void applyExternalSurveyResponse(SurveyResponse surveyResponse){

        if(surveyResponse == null){
            throw new IllegalArgumentException("The survey response can't be null.");
        }

        //handle survey response
        WidgetProperties properties = new WidgetProperties();
        properties.setShouldUpdate(true);
        SurveyWidgetProperties.setAnswers(properties, surveyResponse);
        displayWidget(WidgetTypeEnum.SURVEY_WIDGET, properties, WidgetLocationEnum.ARTICLE, null);
    }

    /**
     * Apply the survey submit created by the learner through the training application
     * to the currently displayed survey in the tutor.
     */
    public void applyExternalSurveySubmit(){

        WidgetProperties properties = new WidgetProperties();
        properties.setShouldUpdate(true);
        SurveyWidgetProperties.setShouldSubmitSurveyPage(properties, true);
        displayWidget(WidgetTypeEnum.SURVEY_WIDGET, properties, WidgetLocationEnum.ARTICLE, null);
    }

    /**
     * Apply the progress notification of loading content in the training application.
     *
     * @param loadProgress contains the load progress information
     */
    public void applyLoadProgress(GenericJSONState loadProgress){

        WidgetProperties properties = new WidgetProperties();
        properties.setShouldUpdate(true);
        properties.setIsFullscreen(true);
        DisplayMediaWidgetProperties.setLoadProgress(properties, loadProgress.getIntById(LoadProgress.TASK_PROGRESS_KEY));
        displayWidget(WidgetTypeEnum.MEDIA_WIDGET, properties, WidgetLocationEnum.ARTICLE, null);
    }

    /**
     * Display a media collection in the session
     *
     * @param mediaHtmlList The HTML to embed in the page with the media
     * @param listener The action listener of the page
     * @param assessment optional assessment logic to execute while the media collection is being shown. Can be null, if no such
     * assessment logic is needed.
     */
    public void displayMediaCollection(List<MediaHtml> mediaHtmlList, ActionListener listener, LessonMaterialList.Assessment assessment) {
        WidgetProperties widgetProperties = new WidgetProperties();
        widgetProperties.setIsFullscreen(true);
        widgetProperties.setHasFocus(true);

        DisplayMediaCollectionWidgetProperties.setMediaHtmlList(widgetProperties, new ArrayList<>(mediaHtmlList));
        DisplayMediaCollectionWidgetProperties.setAssessment(widgetProperties, assessment);

        displayWidget(WidgetTypeEnum.MEDIA_COLLECTION_WIDGET, widgetProperties, WidgetLocationEnum.ARTICLE, listener);
    }

    /**
     * Displays the actions the user can take in the client
     *
     * @param actions The actions the user can take
     * @param controls The scenario controls to make available
     */
    void displayLearnerActions(final boolean fullscreen, List<LearnerAction> actions, generated.dkf.ScenarioControls controls) {

        ArrayList<UserAction> userActions = new ArrayList<>();
        for (LearnerAction action : actions) {

            if (action.getType().equals(LearnerActionEnumType.EXPLOSIVE_HAZARD_SPOT_REPORT)
                    || action.getType().equals(LearnerActionEnumType.NINE_LINE_REPORT)
                    || action.getType().equals(LearnerActionEnumType.SPOT_REPORT)) {

                userActions.add(new UserAction(action,
                                UserActionIconEnum.REPORT_ICON));

            } else if (action.getType().equals(LearnerActionEnumType.RADIO)) {

                userActions.add(new UserAction(action,
                                UserActionIconEnum.RADIO_ICON));

            } else if (action.getType().equals(LearnerActionEnumType.START_PACE_COUNT)) {

                userActions.add(new UserAction(action,
                                UserActionIconEnum.PACE_COUNT_START_ICON));

            } else if (action.getType().equals(LearnerActionEnumType.END_PACE_COUNT)) {

                userActions.add(new UserAction(action,
                                UserActionIconEnum.PACE_COUNT_END_ICON));

            } else if (action.getType().equals(LearnerActionEnumType.TUTOR_ME)){

            	UserAction userAction = new UserAction(action,
            	                            UserActionIconEnum.TUTOR_ME_ICON);
            	userActions.add(userAction);
            	
            } else if(action.getType().equals(LearnerActionEnumType.APPLY_STRATEGY)){
                
                UserAction userAction = new UserAction(action,
                                            UserActionIconEnum.APPLY_STRATEGY_ICON);
                userActions.add(userAction);
                

            } else {

                userActions.add(new UserAction(action,
                                    null));
            }
        }

        ScenarioControls scenarioControls = new ScenarioControls(controls == null || controls.getPreventManualStop() == null);

        displayUserActions(userActions, scenarioControls, new ActionListener() {
            @Override
            public void onAction(AbstractAction action) {

                if (action.getActionType() == ActionTypeEnum.SUBMIT) {

                    SubmitAction submitAction = (SubmitAction) action;
                    WidgetProperties properties = submitAction.getProperties();
                    properties.setIsFullscreen(fullscreen);
                    List<UserAction> userActionsTaken = UserActionWidgetProperties.getUserActionsTaken(properties);
                    if (userActionsTaken != null) {

                        handleUserActions(userActionsTaken);
                    }
                }
            }
        });
    }

    /**
     * Display an after action review in the page
     *
     * @param title the authorable title of this structured review.  Won't be null or empty.
     * @param fullscreen whether or not to display the AAR in full screen mode
     * @param events The events to display in the AAR
     * @param listener The action listener of the page
     */
    public void displayAfterActionReview(String title, boolean fullscreen, List<AbstractAfterActionReviewEvent> events, ActionListener listener) {

    	 WidgetProperties properties = new WidgetProperties();
         properties.setHasFocus(true);
         properties.setIsFullscreen(fullscreen);

        //this is what is shown in the 'summary' section of the AAR
        StringBuilder overviewBuilder = new StringBuilder();
        overviewBuilder.append("<ul>");

        //the list of items to be shown to the user in the AAR
        ArrayList<AfterActionReviewDetailsNode> detailsList = new ArrayList<>();

        for (AbstractAfterActionReviewEvent event : events) {

            //this is the details for this event
            StringBuilder detailsBuilder = new StringBuilder();

            if (event instanceof AfterActionReviewCourseEvent) {
                //a course event is a scored lesson (i.e. training application DKF w/ scoring rules)

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding structured review course event of:\n" + event);
                }
                AfterActionReviewCourseEvent courseEvent = (AfterActionReviewCourseEvent) event;

                detailsList.add(new AfterActionReviewDetailsNode(courseEvent.getScore()));

            } else if (event instanceof AfterActionReviewSurveyEvent) {
                //a survey was given in the course, show the questions and answers

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding survey response event of:\n" + event);
                }
                AfterActionReviewSurveyEvent surveyEvent = (AfterActionReviewSurveyEvent) event;

                if(!surveyEvent.getSurveyScores().isEmpty()){
                    //add details of the survey score

	                overviewBuilder.append("'").append(surveyEvent.getSurveyResponseMetadata().getSurveyName()).append("' Survey was scored. ").append("<br/>");

	                detailsBuilder.append("'").append(surveyEvent.getSurveyResponseMetadata().getSurveyName()).append("' survey scores:").append("<br/><ul>");

                    //handle any score results for the survey
                    for(ScoreInterface score : surveyEvent.getSurveyScores()) {

                        if(score instanceof AbstractAnswerScore) {

                            AbstractAnswerScore answerScore = (AbstractAnswerScore)score;

                            detailsBuilder.append("<li>Answer Score: ").append(answerScore.getTotalEarnedPoints()).append(" / ").append(answerScore.getHighestPossiblePoints()).append("</li>");

                        } else if (score instanceof AbstractScaleScore) {

                            AbstractScaleScore scaleScore = (AbstractScaleScore)score;

                            //don't add a section title (e.g. 'Scale Scores:') if there are no additional information to provide
                            if(!scaleScore.getScales().isEmpty()){

                                detailsBuilder.append("<li>").append("Scale Scores:").append("<ul>");

                                for(AbstractScale pair : scaleScore.getScales()) {

                                    detailsBuilder.append("<li>").append(pair.getAttribute().getDisplayName()).append(": ").append(pair.getRawValue()).append("</li>");
                                }

                                detailsBuilder.append("</ul></li>");
                            }

                        } else if (score instanceof SurveyConceptAssessmentScore){

                            SurveyConceptAssessmentScore conceptScore = (SurveyConceptAssessmentScore)score;

                            if(!conceptScore.getConceptDetails().isEmpty()){

                                detailsBuilder.append("<li>").append("Per Concept:").append("<ul>");

                                for(String concept : conceptScore.getConceptDetails().keySet()){

                                    ConceptOverallDetails details = conceptScore.getConceptDetails().get(concept);
                                    int correct = details.getCorrectQuestions().size();
                                    int incorrect = details.getIncorrectQuestions().size();
                                    int total = correct + incorrect;
                                    detailsBuilder.append("<li>").append(concept).append(" : ").append(correct).append(" of ").append(total).append("</li>");
                                }

                                detailsBuilder.append("</ul></li>");
                            }

                        }else if(score instanceof SurveyFeedbackScorer){

                            detailsBuilder.append("<li>").append("Feedback available").append("</li>");

                        } else{
                            detailsBuilder.append("<li><font color=\"red\">UNHANDLED SURVEY SCORE TYPE</font></li>");
                            logger.error("Unable to create AAR section for unhandled survey score type of "+score+".");
                        }
                    }//end for

                    detailsBuilder.append("</ul>");

                    detailsList.add(new AfterActionReviewDetailsNode(detailsBuilder.toString(), surveyEvent.getSurveyResponseMetadata()));

                }else if(surveyEvent.getSurveyResponseMetadata().getHasIdealAnswer()){

                	overviewBuilder.append("'").append(surveyEvent.getSurveyResponseMetadata().getSurveyName()).append("' Survey was evaluated. ").append("<br/>");

	                detailsBuilder.append("<div style='padding-bottom: 5px;'>'").append(surveyEvent.getSurveyResponseMetadata().getSurveyName()).append("' survey review: ").append("</div>");

	                detailsList.add(new AfterActionReviewDetailsNode(detailsBuilder.toString(), surveyEvent.getSurveyResponseMetadata()));
                }

            }else if(event instanceof AfterActionReviewRemediationEvent){
                //remediation will be or has been given in the course, show the details

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding remediation event of:\n" + event);
                }
                AfterActionReviewRemediationEvent remediationEvent = (AfterActionReviewRemediationEvent)event;

                overviewBuilder.append("<li>Remediation needed on concepts</li>");

                detailsBuilder.append("Remediation needed on <ul>");
                for(String remediationInfo : remediationEvent.getRemediationInfo()){

                    detailsBuilder.append("<li>").append(remediationInfo).append("</li>");
                }

                detailsBuilder.append("</ul>");

                detailsList.add(new AfterActionReviewDetailsNode(detailsBuilder.toString()));
            }else{

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding UNHANDLED event of:\n" + event);
                }
                detailsBuilder.append("<li><font color=\"red\">UNHANDLED EVENT TYPE</font></li>");
                logger.error("Unable to create AAR section for unhandled event type of "+event+".");

                detailsList.add(new AfterActionReviewDetailsNode(detailsBuilder.toString()));
            }
        }//end for

        overviewBuilder.append("</ul>");

        AfterActionReviewWidgetProperties.setOverview(properties, overviewBuilder.toString());
        AfterActionReviewWidgetProperties.setDetails(properties, detailsList);
        AfterActionReviewWidgetProperties.setTitle(properties, title);

        displayWidget(WidgetTypeEnum.AAR_WIDGET, properties, WidgetLocationEnum.ARTICLE, listener);
    }

    /**
     * Displays the user action widget in the Learner Actions tab of the Feedback Widget.
     *
     * @param actions The actions that the user can do
     * @param controls The scenario controls to make available
     * @param listener The listener for actions done in the widget
     */
    public void displayUserActions(ArrayList<UserAction> actions, ScenarioControls controls, ActionListener listener) {

        /* Since the client-side widget used to show the user actions is a
         * singleton, it is never truly closed, which means its character won't
         * get cleaned up by a close action. This can cause problems if the user
         * actions widget is removed from the page while the character is
         * speaking, since the character idle notification will never be sent.
         * To prevent the character from getting stuck, we need to reset the
         * server-side character state whenever the user actions widget is
         * shown. */
        if(logger.isInfoEnabled()){
            logger.info("Clearing character actions because requesting client to display user actions widget.");
        }
        clearCharacterActions();

        WidgetProperties properties = new WidgetProperties();
        properties.setIsFullscreen(true);
        UserActionWidgetProperties.setUserActions(properties, actions);
        UserActionWidgetProperties.setScenarioControls(properties, controls);

        displayWidget(WidgetTypeEnum.USER_ACTION_WIDGET, properties, WidgetLocationEnum.ARTICLE, listener);
    }

    /**
     * Displays learner feedback in the article of the web page
     *
     * @return true if the feedback was sent to the client, false if it was queued.
     */
    public boolean displayArticleFeedback() {

        synchronized (feedbackUpdateQueue) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding empty feedback update request to queue.");
            }
            feedbackUpdateQueue.add(new TutorUserInterfaceFeedback());
        }

        if (isCharacterIdle()) {
            // Applies the first update request in the queue
            return applyNextFeedbackUpdate();
        } else {
            /* Otherwise, the update will be applied when the Character idle
             * notification is received */
            if (logger.isInfoEnabled()) {
                logger.info(
                        "The character is not idle right now therefore can't immediately apply the feedback update request.");
            }

            return false;
        }
    }

    /**
     * Removes all queued character actions for this domain session.
     */
    private void clearCharacterActions(){

        synchronized (chatUpdateQueue) {
            chatUpdateQueue.clear();
        }

        synchronized (feedbackUpdateQueue) {
        	feedbackUpdateQueue.clear();
        }
    }

    /**
     * Display learner feedback in the article of the web page with new feedback
     * displayed
     *
     * @param feedback contains the feedback to display
     * @return true if the feedback was sent to the client.  False is returned if the feedback was queued.
     */
    public boolean displayArticleFeedback(TutorUserInterfaceFeedback feedback) {

        if (feedback == null) {
            throw new IllegalArgumentException("The feedback can't be null.");
        }

        synchronized (feedbackUpdateQueue) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding display feedback request to queue.\n" + feedback);
            }
            feedbackUpdateQueue.add(feedback);
        }

        synchronized (characterIdleLock) {
            if (isCharacterIdle()) {
            	// Applies the first update request in the queue
                return applyNextFeedbackUpdate();
            }else{
                // Otherwise, the first update will be applied when the
                // character idle notification is received
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "The character is not idle right now therefore can't immediately apply the feedback request.");
                }
                return false;
            }
        }
    }

    /**
     * Displays feedback to the user
     *
     * @param hasNewFeedback If there is new feedback since the last time
     * feedback was shown
     * @param feedback The history of feedback displayed to the user
     */
    public void notifyFeedbackWidget(int updateCount) {

        WidgetProperties properties = new WidgetProperties();
        properties.setShouldUpdate(true);

        AvatarContainerWidgetProperties.setWidgetType(properties, WidgetTypeEnum.FEEDBACK_WIDGET);
        FeedbackWidgetProperties.setHasNewFeedback(properties, hasNewFeedback);
        FeedbackWidgetProperties.setFeedback(properties, feedback);
        FeedbackWidgetProperties.setUpdateCount(properties, updateCount);
        FeedbackWidgetProperties.setOldFeedbackStyleEnabled(properties, TutorModuleProperties.getInstance().getOldFeedbackDifferentStyle());

        displayWidget(WidgetTypeEnum.AVATAR_CONTAINER_WIDGET, properties, WidgetLocationEnum.ARTICLE);
    }

    /**
     * Displays a chat window as an article, this initializes the chat window
     *
     * @param request The chat window request
     * @param listener The listener for actions in the chat window
     */
    public void displayArticleChatWindow(final DisplayChatWindowRequest request, final ActionListener listener) {

        final ActiveChatWindowRequest activeChatWindow = new ActiveChatWindowRequest(this, request, new ActionListener() {
            @Override
            public void onAction(AbstractAction action) {
                if (action.getActionType() == ActionTypeEnum.CLOSE) {
                    chatIdToChatRequest.remove(request.getChatId());
                }

                if (listener != null) {
                    listener.onAction(action);
                }
            }
        });

        chatIdToChatRequest.put(request.getChatId(), activeChatWindow);

        activeChatWindow.displayWidget(request.getChatId());
    }

    /**
     * Displays a chat window as an article, this initializes the chat window
     *
     * @param request The chat window request
     * @param listener The listener for actions in the chat window
     */
    public void notifyArticleChatWindow(int chatId, int updateCount) {

        ActiveChatWindowRequest activeChatWindow = chatIdToChatRequest.get(chatId);
        if(activeChatWindow != null){
            activeChatWindow.displayUpdateNotification(chatId, updateCount);
        }
    }

    /**
     * Notification that the character is currently idle for this session. This
     * will cause the next character action to happen (if any).
     */
    public void characterIdleNotification() {

        /* We should test to see if the character itself is responsive. If it
         * isn't we should disable the wait for character to be idle logic since
         * the character will never tell us its idle. */
        synchronized (characterEnabledLock) {
            /* Cancels any currently running timer */
            if (characterEnabledTimer != null) {
                characterEnabledTimer.cancel();
                characterEnabledTimer = null;
            }
            characterEnabled = true;
        }

        synchronized (characterIdleLock) {
            long time = System.currentTimeMillis();
            if(logger.isInfoEnabled()){
                logger.info("Notified that character is idle (" + time + ") for session " + this);
            }
            characterIdle = true;

            //
            // Notify any listeners that the character is now idle
            //
            synchronized(characterIdleListeners){

                Iterator<CharacterIdleListener> itr = characterIdleListeners.iterator();
                while(itr.hasNext()){

                    try{
                        CharacterIdleListener listener = itr.next();
                        listener.idleNotification();

                        //remove the listener if desired
                        if(listener.shouldRemoveListener()){
                            itr.remove();
                        }
                    }catch(Throwable t){
                        logger.error("Caught error from misbehaving character idle listener.", t);
                    }
                }
            }

            if(UpdateQueueManager.getInstance().isFeedbackActive(userSession.getUserId())) {
            	applyNextFeedbackUpdate();
            } else {
            	applyNextChatUpdate();
            }

        }
    }

    /**
     * Begins a timer for measuring if the character is processing feedback
     * messages.
     */
    private void startCharacterTimeout() {

        synchronized (characterEnabledLock) {

            /* Cancels any currently running timer */
            if (characterEnabledTimer != null) {
                characterEnabledTimer.cancel();
                characterEnabledTimer = null;
            }

            final int CHARACTER_ENABLED_TIMEOUT = TutorModuleProperties.getInstance().getCharacterDetectionTimeout();

            /* Starts the timer */
            characterEnabledTimer = new Timer("Character Enabled Timeout Timer");
            characterEnabledTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    synchronized (characterEnabledLock) {
                        characterEnabled = false;

                        if (logger.isWarnEnabled()) {
                            final String msg = String.format(
                                    "The character did not process a feedback request from domain session %d within %d milliseconds for user session %s",
                                    domainSessionId, CHARACTER_ENABLED_TIMEOUT, userSession.toString());
                            logger.warn(msg);
                        }

                        sendCharacterUnresponsiveMessage();
                    }
                }
            }, CHARACTER_ENABLED_TIMEOUT);
        }
    }

    /**
     * Sends a feedback message to the client informing it that the character is
     * not responding to the feedback delivery request.
     */
    private void sendCharacterUnresponsiveMessage() {
        TutorUserInterfaceFeedback feedback = new TutorUserInterfaceFeedback();
        feedback.setDisplayTextAction(CHARACTER_NOT_RESPONDING_MESSAGE);
        displayArticleFeedback(feedback);
    }

    /**
     * Notification that the character is currently busy for this session
     */
    public void characterBusyNotification(){
        characterBusyNotification(true);
    }
    
    /**
     * Notification that the character is currently busy for this session
     * 
     * @param whether to time out if the character takes too long to say the message. Setting
     * this to false can be useful if the avatar's state needs to be changed without actually
     * making the avatar speak, such as when switching article widgets.
     */
    public void characterBusyNotification(boolean startTimeout){

        synchronized (characterIdleLock) {
            if (isCharacterIdle()) {
                if(CharacterServerService.getInstance().isOnline()) {
                    long time = System.currentTimeMillis();
                    if(logger.isInfoEnabled()){
                        logger.info("Setting character to busy (" + time + ") for " + this);
                    }
                    characterIdle = false;
                    
                    if(startTimeout) {
                        startCharacterTimeout();
                    }
                }
            }
        }
    }

    /**
     * Determines whether or not the character is idle based on whether or not
     * the character is enabled and whether or not the character is currently
     * delivering feedback.
     *
     * @return True if the character has been determined to be disabled or the
     *         character is currently not speaking, false otherwise.
     */
    public boolean isCharacterIdle() {
        synchronized (characterIdleLock) {
            synchronized (characterEnabledLock) {
                return !characterEnabled || characterIdle;
            }
        }
    }

    /**
     * Add a listener that wants to be notified as soon as the character in the tutor client
     * is idle.
     *
     * @param listener instance to callback once the tutor server knows the
     * character is idle.
     */
    public void addCharacterIdleListener(CharacterIdleListener listener){

        if(listener == null){
            return;
        }

        synchronized(characterIdleListeners){
            characterIdleListeners.add(listener);
        }
    }

    /**
     * Applies the next character action (e.g. feedback, conversation) to the
     * TUI if there are any queued.
     */
    private void applyNextChatUpdate(){

    	synchronized (chatUpdateQueue) {

    		DisplayChatWindowUpdateRequest update = null;
    		int activeChat = -1;

    		if(tutorActionsAvailable) {
	    		// Make sure the update is for the active chat widget
	    		activeChat = UpdateQueueManager.getInstance().getActiveChatId(userSession.getUserId());

	    		for(Serializable item : chatUpdateQueue) {
	    			if(item != null) {
	    				if(((DisplayChatWindowUpdateRequest)item).getChatId() == activeChat) {
	    					update = (DisplayChatWindowUpdateRequest)item;
	    					break;
	    				}
	    			}
	    		}

    		} else if(!chatUpdateQueue.isEmpty()) {
    			update = (DisplayChatWindowUpdateRequest) chatUpdateQueue.get(0);

    		}

    		if(update != null){

                if (logger.isInfoEnabled()) {
                    logger.info("Applying next character update of " + update);
                }
    			chatUpdateQueue.remove(update);

    			ActiveChatWindowRequest activeChatWindow = chatIdToChatRequest.get(update.getChatId());
    	        if(activeChatWindow != null){

        			//sets properties and attributes of the chat update
        			activeChatWindow.handleUpdate(update);

        			//applies the update on the client
        			activeChatWindow.displayWidget(update.getChatId());
    	        }
            } else if (logger.isInfoEnabled()) {
                logger.info("No updates for the active chat with id " + activeChat + ".  TutorActionsAvailable = " + tutorActionsAvailable);
            }
    	}
    }

    /**
     * Checks for the next available chat update. If an update is available, it will be applied.
     */
    public void checkForChatUpdates() {
        if (isCharacterIdle()) {
            applyNextChatUpdate();
    	}
    }

    /**
     * Applies the next character action (e.g. feedback, conversation) to the
     * TUI if there are any queued.
     *
     * @return true if the feedback was sent to the client. False is returned if
     *         the feedback was queued.
     */
    private boolean applyNextFeedbackUpdate(){

    	synchronized (feedbackUpdateQueue) {

            TutorUserInterfaceFeedback feedbackUpdate = feedbackUpdateQueue.poll();

            if (feedbackUpdate == null) {
                return false;
            }

            if (logger.isInfoEnabled()) {
                logger.info("Applying next character update of " + feedbackUpdate);
            }

            /* check if this is an empty update by determining if the update is
             * valid or not */
            boolean valid = true;
            try {
                feedbackUpdate.validate();
            } catch (@SuppressWarnings("unused") Exception e) {
                valid = false;
            }

            synchronized (feedback) {
                if (valid) {
                    /* queue the feedback accordingly */

                    if (feedbackUpdate.getClearTextAction() != null) {
                        /* The current feedback message signifies that all
                         * previous feedback should be removed from the TUI. */
                        this.feedback.clear();
                    }

                    this.feedback.add(feedbackUpdate);
                    hasNewFeedback = true;

                    return displayArticleFeedback();

                } else {

                    /* display any remaining feedback, this is used when the
                     * widget is closing */
                    if (logger.isInfoEnabled()) {
                        logger.info("Displaying next character update of " + feedbackUpdate);
                    }

                    if (feedback.isEmpty()) {
                        displayArticleFeedback(hasNewFeedback, feedback);
                    } else {
                        if (feedback.getFirst().getDisplayAvatarAction() instanceof DisplayTextToSpeechAvatarAction) {
                            /* If the feedback isn't empty, the character should
                             * be busy */
                            characterBusyNotification();
                        }
                        displayChatWidget(true, Arrays.asList(feedback.poll()));
                    }

                    hasNewFeedback = false;
                    return true;
                }
            }
    	}
    }

    /**
     * Displays a chat window as an article, this updates the chat window
     *
     * @param update The chat window update request.  Can't be null.
     */
    public void displayArticleChatWindow(DisplayChatWindowUpdateRequest update) {

        if(update == null){
            throw new IllegalArgumentException("The update can't be null.");
        }

        ActiveChatWindowRequest activeChatWindow = chatIdToChatRequest.get(update.getChatId());

        if(activeChatWindow == null){
            throw new DetailedException("Unable to apply the chat window update because the chat was not properly started.",
                    "The following chat window update request could not be applied.  Make sure an initial chat window request message was sent that would have started that widget.\n"+update, null);
        }

        synchronized (chatUpdateQueue) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding chat window update request to queue.\n" + update);
            }
            chatUpdateQueue.add(update);
        }

        synchronized (characterIdleLock) {

            if (isCharacterIdle()) {
                applyNextChatUpdate();
            } else if (logger.isInfoEnabled()) {
                logger.info(
                        "The character is not idle right now therefore can't immediately apply the chat window update request.");
            }
        }

    }

    /**
     * Displays the connection status while connecting to and configuring the Gateway module.
     *
     * @param request the request to display the connection status while connecting to and configuring the Gateway module
     * @param listener The action listener of the page
     */
    public void displayCourseInitInstructions(DisplayCourseInitInstructionsRequest request, ActionListener listener) {

    	WidgetProperties properties = new WidgetProperties();
    	properties.setIsFullscreen(true);
    	properties.setHasFocus(true);
    	properties.setShouldUpdate(true);

    	CourseInitInstructionsWidgetProperties.setDisplayCourseInitInstructionsRequest(properties, request);
    	displayWidget(WidgetTypeEnum.COURSE_INIT_INSTRUCTIONS_WIDGET, properties, WidgetLocationEnum.ARTICLE, listener);
    }

    /**
     * Display a dialog in the web page
     *
     * @param instance The instance of a dialog to display
     */
    public void displayDialog(DialogInstance instance) {
        doServerAction(new DisplayDialogAction(instance));
    }

    /**
     * Display a dialog in the web page
     *
     * @param type The type of dialog to display
     * @param title The title of the dialog to display
     * @param message The message of the dialog to display
     */
    public void displayDialog(DialogTypeEnum type, String title, String message) {
        displayDialog(new DialogInstance(UUID.randomUUID().toString(), type, title, message));
    }

    /**
     * Display a widget in the web page
     *
     * @param pageType The type of web page to display
     */
    public void displayWidget(WidgetTypeEnum pageType) {
        displayArticle(new WidgetInstance(UUID.randomUUID().toString(), pageType));
    }

    /**
     * Display a widget in the web page
     *
     * @param pageType The type of web page to display
     * @param properties The properties of the web page to display
     */
    public void displayWidget(WidgetTypeEnum pageType, WidgetProperties properties) {
        displayArticle(new WidgetInstance(UUID.randomUUID().toString(), pageType, properties));
    }

    /**
     * Displays a widget in the web page
     *
     * @param pageType The type of web page to display
     * @param properties The properties of the web page to display
     * @param location an enumerated location of where to display the widget
     * @return String the unique id of the widget created
     */
    public String displayWidget(WidgetTypeEnum pageType, WidgetProperties properties, WidgetLocationEnum location) {
        WidgetInstance instance = new WidgetInstance(UUID.randomUUID().toString(), pageType, properties);
        if (location == WidgetLocationEnum.ARTICLE) {
            displayArticle(instance);
        } else if (location == WidgetLocationEnum.FOOTER) {
            displayFooter(instance);
        }
        return instance.getWidgetId();
    }

    /**
     * Display a widget in the article of the web page
     *
     * @param instance The widget instance to display
     */
    public void displayArticle(WidgetInstance instance) {
        doServerAction(new DisplayWidgetAction(instance, WidgetLocationEnum.ARTICLE));
    }

    /**
     * Display a widget in the footer of the web page
     *
     * @param instance The widget instance to display
     */
    public void displayFooter(WidgetInstance instance) {
        doServerAction(new DisplayWidgetAction(instance, WidgetLocationEnum.FOOTER));
    }

    /**
     * Displays feedback to the user
     *
     * @param hasNewFeedback If there is new feedback since the last time
     * feedback was shown
     * @param feedback The history of feedback displayed to the user
     */
    public void displayArticleFeedback(boolean hasNewFeedback, List<TutorUserInterfaceFeedback> feedback) {
        WidgetProperties properties = new WidgetProperties();
        properties.setIsFullscreen(fullScreen);

        FeedbackWidgetProperties.setHasNewFeedback(properties, hasNewFeedback);
        FeedbackWidgetProperties.setFeedback(properties, feedback);
        FeedbackWidgetProperties.setOldFeedbackStyleEnabled(properties, TutorModuleProperties.getInstance().getOldFeedbackDifferentStyle());

        displayWidget(WidgetTypeEnum.FEEDBACK_WIDGET, properties, WidgetLocationEnum.ARTICLE);
    }

    /**
     * Displays feedback to the user
     *
     * @param hasNewFeedback If there is new feedback since the last time
     * feedback was shown
     * @param feedback The history of feedback displayed to the user
     */
    public void displayChatWidget(boolean hasNewFeedback, List<TutorUserInterfaceFeedback> feedback) {

        WidgetProperties properties = new WidgetProperties();
        properties.setShouldUpdate(true);

        AvatarContainerWidgetProperties.setWidgetType(properties, WidgetTypeEnum.FEEDBACK_WIDGET);
        FeedbackWidgetProperties.setHasNewFeedback(properties, hasNewFeedback);
        FeedbackWidgetProperties.setFeedback(properties, feedback);
        FeedbackWidgetProperties.setOldFeedbackStyleEnabled(properties, TutorModuleProperties.getInstance().getOldFeedbackDifferentStyle());

        displayWidget(WidgetTypeEnum.AVATAR_CONTAINER_WIDGET, properties, WidgetLocationEnum.ARTICLE);
    }

    /**
     * Displays feedback to the user when there is no new feedback
     *
     * @param feedback The history of feedback displayed to the user
     */
    public void displayArticleFeedback(ArrayList<TutorUserInterfaceFeedback> feedback) {
        displayArticleFeedback(false, feedback);
    }

    /**
     * Display the GIFT logo in the session
     */
    public void displayLogo() {
        displayWidget(WidgetTypeEnum.LOGO_WIDGET);
    }

    /**
     * Displays some text in the footer
     *
     * @param message The text to display in the footer
     */
    public void displayFooterText(String message) {
        WidgetProperties properties = new WidgetProperties();
        DisplayMessageWidgetProperties.setParameters(properties, DisplayMessageTutorRequest.createTextRequest(null, message));
        DisplayMessageWidgetProperties.setHasContinueButton(properties, false);
        displayWidget(WidgetTypeEnum.FOOTER_TEXT_WIDGET, properties, WidgetLocationEnum.FOOTER);
    }

    /**
     * Gets if the lesson is still active
     *
     * @return boolean If the lesson is still active
     */
    public boolean isLessonActive() {

        return LessonStatus.ACTIVE.equals(lessonStatus);
    }
    
    /**
     * Gets if the lesson is still inactive
     *
     * @return boolean If the lesson is still inactive
     */
    public boolean isLessonInactive() {

        return LessonStatus.INACTIVE.equals(lessonStatus);
    }

    private void handleUserActions(List<UserAction> userActions) {

        for (UserAction userAction : userActions) {

            if (logger.isInfoEnabled()) {
                logger.info("User submitted an action: " + userAction.getDisplayString());
            }
            MessageCollectionCallback callback = new MessageCollectionCallback() {
                @Override
                public void success() {
                    // Do nothing
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {
                    StringBuilder response = new StringBuilder();
                    response.append("NACK received from ");
                    response.append(msg.getSenderAddress());
                    response.append(": ");
                    response.append(((NACK) msg.getPayload()).getErrorMessage());

                    displayDialog(DialogTypeEnum.ERROR_DIALOG, "Submit Report Error", response.toString());
                }

                @Override
                public void failure(String why) {
                    StringBuilder response = new StringBuilder();
                    response.append("Failure: ");
                    response.append(why);

                    displayDialog(DialogTypeEnum.ERROR_DIALOG, "Submit Report Error", response.toString());
                }
            };

            if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.EXPLOSIVE_HAZARD_SPOT_REPORT)) {

                TutorModule.getInstance().sendUserActionTaken(new ExplosiveHazardSpotReport(userAction.getLearnerAction()), this, callback);

            } else if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.NINE_LINE_REPORT)) {

                TutorModule.getInstance().sendUserActionTaken(new NineLineReport(userAction.getLearnerAction()), this, callback);

            } else if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.SPOT_REPORT)) {

                TutorModule.getInstance().sendUserActionTaken(new SpotReport(userAction.getLearnerAction()), this, callback);

            } else if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.RADIO)) {

                TutorModule.getInstance().sendUserActionTaken(new RadioUsed(userAction.getLearnerAction()), this, callback);

            } else if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.START_PACE_COUNT)) {

                TutorModule.getInstance().sendUserActionTaken(new PaceCountStarted(userAction.getLearnerAction()), this, callback);

            } else if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.END_PACE_COUNT)) {

                TutorModule.getInstance().sendUserActionTaken(new PaceCountEnded(userAction.getLearnerAction()), this, callback);

            } else if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.TUTOR_ME)) {

                TutorModule.getInstance().sendUserActionTaken(new TutorMeLearnerTutorAction(userAction.getLearnerAction()), this, callback);

            } else if (LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.ASSESS_MY_LOCATION)) {

                TutorModule.getInstance().sendUserActionTaken(new AssessMyLocationTutorAction(userAction.getLearnerAction()), this, callback);
            } else if(LearnerActionEnumType.fromValue(userAction.getValue()).equals(LearnerActionEnumType.APPLY_STRATEGY)){
                
                TutorModule.getInstance().sendUserActionTaken(new ApplyStrategyLearnerAction(userAction.getLearnerAction()), this, callback);
            }
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DomainSessionWebSession: ");
        sb.append("id = ").append(domainSessionId);
        sb.append(", experimentId = ").append(getExperimentId());
        sb.append(", runtimeId = ").append(domainRuntimeId);
        sb.append(", sourceId = ").append(domainSourceId);
        sb.append(", user = ").append(userSession);
        sb.append(", lessonStatus = ").append(lessonStatus);
        sb.append("]");
        return sb.toString();

    }

    /**
     * Sends a close action to any active action listeners waiting for a widget to be closed.
     * <br/><br/>
     * This method is intended to address an issue where the Tutor receives a request to display a widget but never replies to the request
     * if the browser is closed before the client can send a close action to the server. By calling this method, the server can ensure
     * that any message handlers waiting for widgets be closed will be able to proceed as they normally would, even though the client may
     * be unavailable to display the widget.
     * <br/><br/>
     * This method should only really be called during cleanup logic to make sure that threads don't get stuck waiting for a widget that
     * will never show.
     */
    public void closeWidgetListeners(){

    	if(idToActionListener != null){

    		for(ActionListener listener : idToActionListener.values()){

    			if(listener != null){
    				listener.onAction(new CloseAction());
    			}
    		}
    	}

    	if(chatIdToActionListener != null){

    		for(ActionListener listener : chatIdToActionListener.values()){

    			if(listener != null){
    				listener.onAction(new CloseAction());
    			}
    		}
    	}
    }

    public static void setFullScreen(boolean isFullScreen) {
        fullScreen = isFullScreen;
    }

    /**
     * Performs a server driven action.
     *
     * @param action The action to do
     */
    public final void doServerAction(AbstractAction action) {

        if (userWebSession != null) {
            userWebSession.handleServerAction(action);
        } else {
            logger.error("The user web session is null.  Unhandled action: " + action);
        }
    }

    /**
     * Notifies the browser web session that a domain web state was created.  This initializes
     * the client web browser to display the current article/footer widget.
     *
     * @param browserSession The browser web session to be updated.
     */
    public void notifyBrowserWebSessionOfNewDomainWebState(TutorBrowserWebSession browserSession) {
        browserSession.sendWebSocketMessage(new DisplayWidgetAction(articleWidget, WidgetLocationEnum.ARTICLE));
        browserSession.sendWebSocketMessage(new DisplayWidgetAction(footerWidget, WidgetLocationEnum.FOOTER));
    }

    /**
     * Resumes a browser web session by sending the current state of the article / footer widget.
     *
     * @param browserSession The browser web session to be updated.
     */
    public void resumeBrowserSession(TutorBrowserWebSession browserSession) {

        if(articleWidget != null && articleWidget.getWidgetType() == WidgetTypeEnum.AVATAR_CONTAINER_WIDGET) {

            /* if the current article widget is an character container, only
             * show new feedback messages so that old ones aren't redundantly
             * pushed to the client, since the client may still be displaying
             * them */
            FeedbackWidgetProperties.setHasNewFeedback(articleWidget.getWidgetProperties(), hasNewFeedback);
            FeedbackWidgetProperties.setFeedback(articleWidget.getWidgetProperties(), feedback);
        }

        browserSession.sendWebSocketMessage(new DisplayWidgetAction(articleWidget, WidgetLocationEnum.ARTICLE));
        browserSession.sendWebSocketMessage(new DisplayWidgetAction(footerWidget, WidgetLocationEnum.FOOTER));

    }

    /**
     * Handler for when the domain session is closed.
     */
    public void onDomainSessionClosed() {
        // Don't display the footer message in experiment mode.
        if (getExperimentId() == null) {
            displayFooterText("The domain session has ended.");
        }

        //clean up chat history for this domain session
        ActiveChatWindowRequest.domainSessionClosing(getDomainSessionId());
    }


    /**
     * Used for notification that the tutor client character is idle.
     *
     * @author mhoffman
     *
     */
    public interface CharacterIdleListener{

        /**
         * Notification that the character is idle
         */
        public void idleNotification();

        /**
         * Whether this listener should be removed from the list of listeners
         * that will be notified the next time the character is idle.  This is called
         * after the idleNotification is called.
         *
         * @return true if this listener instance only wants to be notified once
         * of the character being idle.
         */
        public boolean shouldRemoveListener();
    }
}
