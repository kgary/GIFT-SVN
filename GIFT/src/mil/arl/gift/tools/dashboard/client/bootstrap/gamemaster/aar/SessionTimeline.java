/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.UnorderedList;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Audio;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.Feedback;
import generated.dkf.Feedback.File;
import generated.dkf.ImageProperties;
import generated.dkf.InstructionalIntervention;
import generated.dkf.LessonMaterialList;
import generated.dkf.Media;
import generated.dkf.MediaSemantics;
import generated.dkf.Message;
import generated.dkf.MidLessonMedia;
import generated.dkf.PDFProperties;
import generated.dkf.PerformanceAssessment;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.SlideShowProperties;
import generated.dkf.Strategy;
import generated.dkf.WebpageProperties;
import generated.dkf.YoutubeVideoProperties;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.strategy.StrategyStateUpdate;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.SafeHtmlUtils;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.BookmarkSelector;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.BsGameMasterPanel;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.CourseConceptProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.IntervalClickHandler;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.PerformanceNodePath;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.PerformanceNodeSelector;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.StrategySelector;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.TextAreaDialog;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.TimelineNavigator;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSetting;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSetting.VolumeChangeHandler;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.SessionIntervalStatus.IntervalType;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.TimelineChart.Formatter;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.TimelineChart.PlaybackHandler;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter.OverallAssessmentDialog;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.RunState;
import mil.arl.gift.tools.dashboard.client.gamemaster.BookmarkProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingPriority;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.StrategyProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.AssessmentDisplayMode;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.SummativeAssessmentChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider.TimelineChangeHandler;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.rpcs.SessionTimelineInfo;


/**
 * A widget that displays a domain session's history as an interactive timeline
 *
 * @author nroberts
 */
public class SessionTimeline extends Composite
        implements RequiresResize, ActiveSessionChangeHandler, TimelineChangeHandler, VolumeChangeHandler, TimelineNavigator, SummativeAssessmentChangeHandler {

    /** 
     * A unique ID identifying the timeline event used to show strategies in the timeline. This ID
     * should not match any performance node IDs in the timeline. 
     */
    private static final int STRATEGIES_EVENT_ID = -1;
    
    /** 
     * A unique ID identifying the timeline event used to show global bookmarks in the timeline. This ID
     * should not match any performance node IDs in the timeline. 
     */
    private static final int GLOBAL_BOOKMARKS_EVENT_ID = -2;
    
    /**
     * amount of time in milliseconds to use when creating an event interval for an event that would normally have no duration
     * when it comes to being visualized in the timeline (e.g. a change in task assessment that also ended the task)
     */
    private static final long ARTIFICIAL_DURATION_MS = 1000l;

    /** A tooltip used to indicate what happens when the user clicks a strategy in the timeline */
    private static final String STRATEGY_CLICK_TOOLTIP = "view scenario inject details";
    
    /** A tooltip used to indicate what happens when the user clicks a global bookmark in the timeline */
    private static final String GLOBAL_BOOKMARK_CLICK_TOOLTIP = "view note details";

    /** The format to use when displaying dates in the playback time label */
    private static final DateTimeFormat PLAYBACK_TIME_LABEL_FORMAT = DateTimeFormat
            .getFormat("H:mm:ss EEEE, MMMM dd, yyyy");

    /**
     * The text to append to the tooltip to indicate that a patch was applied
     */
    private static final String PATCH_TOOLTIP = "(custom change)";

    /** Text for context menu option to play audio */
    private static final String PLAY_AUDIO = "Play Audio";

    /** Text for context menu option to stop audio */
    private static final String STOP_AUDIO = "Stop Audio";
    
    /** The time threshold associated with the current playhead position, in milliseconds. If the user 
     * clicks a task/concept event interval near the playhead within this threshold, then instead of moving 
     * the playhead, the click will show the current assessment associated with that event interval */
    @SuppressWarnings("unused")
    private static final int PLAYHEAD_CLICK_THRESHOLD_MS = 3000;

    /** A logger used to write informative messages to the browser console */
    private static final Logger logger = Logger.getLogger(SessionTimeline.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SessionTimelineUiBinder uiBinder = GWT.create(SessionTimelineUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SessionTimelineUiBinder extends UiBinder<Widget, SessionTimeline> {
    }

    /** The active session provider instance */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /** The registered session provider instance */
    private final RegisteredSessionProvider registeredSessionProvider = RegisteredSessionProvider.getInstance();

    /** The component loading provider instance */
    private final LoadingDialogProvider componentLoadingProvider = LoadingDialogProvider.getInstance();
    
    /** The names of all the performance nodes that match course concepts */
    private Set<String> courseConceptNodes = new HashSet<>();

    /** Interface to allow CSS style name access */
    protected interface Style extends CssResource {

        /**
         * Gets the CSS class name to use for event intervals representing when
         * performance nodes are patched
         *
         * @return the class name
         */
        public String intervalPatch();

        /**
         * Gets the CSS class name to use for event intervals representing when
         * performance nodes are active AND at expectation
         *
         * @return the class name
         */
        public String intervalAtExpectation();
        
        /**
         * Gets the CSS class name to use for event intervals representing when
         * performance nodes are active AND above expectation
         *
         * @return the class name
         */
        public String intervalAboveExpectation();
        
        /**
         * Gets the CSS class name to use for event intervals representing when performance nodes are
         * active but their assessments are unknown
         *
         * @return the class name
         */
        public String intervalUnknown();

        /**
         * Gets the CSS class name to use for event intervals representing when performance nodes are
         * active AND below expectation
         *
         * @return the class name
         */
        public String intervalBelowExpectation();
       
        /**
         * Gets the CSS class name used for event intervals representing bookmarks
         * 
         * @return the class name
         */
        public String intervalBookmark();
        
        /**
         * Gets the CSS class name used for event intervals representing strategies
         * 
         * @return the class name
         */
        public String intervalStrategy();
        
        /**
         * Gets the CSS class name used for event intervals representing observer evaluations
         * 
         * @return the class name
         */
        public String intervalObserverEval();
        
        /**
         * Gets the CSS class name to use for timeline events representing tasks
         *
         * @return the class name
         */
        public String taskEvent();
        
        /**
         * Gets the CSS class name to use for timeline events representing concepts
         *
         * @return the class name
         */
        public String conceptEvent();
    }

    /** A set of styles associated with this widget */
    @UiField
    protected Style style;

    /** A chart used to render session history as a timeline */
    @UiField
    protected TimelineChart timelineChart;

    /** The history of events (i.e. the timeline) to render to the timeline chart */
    private TimelineHistory timelineHistory = null;
    
    /** The element being used to control the playback of the current session's captured audio, if such audio exists */
    private AudioElement capturedAudioElement = null;

    /** The element that is used to play bookmark audio */
    private AudioElement bookmarkAudioElement = null;

    /**
     * Create a remote service proxy to talk to the server-side dashboard
     * service.
     */
    private static final DashboardServiceAsync dashboardService = UiManager.getInstance().getDashboardService();

    /**
     * Handles when the user performs interactions that change the timeline's
     * playback
     */
    private final PlaybackHandler playbackHandler;

    /** The selector to use to select/deselect strategies from the timeline */
    private StrategySelector strategySelector;
    
    /** The selector to use to select/deselect bookmarks from the timeline */
    private BookmarkSelector bookmarkSelector;
    
    /** The selector to use to select/deselect performance nodes from the timeline */
    private PerformanceNodeSelector perfNodeSelector;

    /** The event used to display strategies in the timeline */
    private TimelineEvent strategiesEvent;
    
    /** The event used to display global bookmarks in the timeline */
    private TimelineEvent globalBookmarksEvent;
    
    /** 
     * The last global bookmark that was rendered to the timeline. Used to avoid creating
     * multiple timeline events for the same bookmark message
     */
    private String lastGlobalBookmark;

    /** Show only these tasks in the timeline */
    private Set<Integer> showOnlyTaskIds = new HashSet<>();
    
    /** The latest timeline info that was received from the server via reload. Used to repopulate the UI after a refresh. */
    private SessionTimelineInfo timelineInfo = null;
    
    /** The last event interval that was clicked on */
    @SuppressWarnings("unused")
    private EventInterval lastClickedInterval = null;

    /** The previous vertical scroll position of the chart before it was reloaded. Used to preserve the position of the scroll
     * bars (if any, when the timeline is reloaded) */
    private Integer chartScroll;
    
    /** A mapping from each unique performance node name to its summative assessment score */
    private Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment = new HashMap<>();

    /** Whether playback was paused temporarily to refresh the UI. Used to resume the playback afterward. */
    private boolean resumeWhenReloadFinished = false;

    /**
     * Creates a new domain session session timeline
     * 
     * @param playbackHandler Registers the given handler so that it can handle
     *        when the user performs interactions that change the timeline's
     *        playback. Can be null.
     */
    public SessionTimeline(final PlaybackHandler playbackHandler) {
        initWidget(uiBinder.createAndBindUi(this));

        this.playbackHandler = playbackHandler;

        timelineChart.setFormatter(new Formatter() {

            @Override
            public String getCssClass(EventInterval interval) {

                SessionIntervalStatus status = interval.getStatus().<SessionIntervalStatus>cast();
                
                Set<String> styleClass = new HashSet<>();
                switch(status.getType().getBaseType()) {

                    case AT_EXPECTATION:
                        styleClass.add(style.intervalAtExpectation());
                        break;
                        
                    case ABOVE_EXPECTATION:
                        styleClass.add(style.intervalAboveExpectation());
                        break;

                    case BELOW_EXPECTATION:
                        styleClass.add(style.intervalBelowExpectation());
                        break;
                        
                    case UNKNOWN:
                        styleClass.add(style.intervalUnknown());
                        break;
                        
                    case BOOKMARK:
                        styleClass.add(style.intervalBookmark());
                        break;
                        
                    case OBSERVER_EVALUATION:
                        styleClass.add(style.intervalObserverEval());
                        break;
                        
                    case STRATEGY:
                        styleClass.add(style.intervalStrategy());
                        break;
				default:
					break;
                }
                if (interval.isPatched()) {
                    styleClass.add(style.intervalPatch());
                }

                return StringUtils.join(" ", styleClass);
            }

            @Override
            public String getCssClass(TimelineEvent event) {
                
                if(event.getParent() != null) {
                    return style.conceptEvent();
                }
                
                return style.taskEvent();
            }

            @Override
            public String getIcon(EventInterval interval) {
                
                SessionIntervalStatus status = interval.getStatus().<SessionIntervalStatus>cast();
                
                /* Use the icon corresponding to the interval type. If the immediate type
                 * does not have an icon, use the icon of the closest parent type. */
                String icon = null;
                IntervalType currType = status.getType();
                do {
                    icon = currType.getDisplayIcon();
                    currType = currType.getParentType();
                    
                } while(icon == null && currType != null);
                
                return icon;
            }
            
            @Override
            public Boolean shouldCenterIcon(EventInterval interval) {
                
                SessionIntervalStatus status = interval.getStatus().<SessionIntervalStatus>cast();
                
                return status.getType().shouldCenterIcon();
            }
            
        }).setPlaybackHandler(new PlaybackHandler() {
            
            @Override
            public void onSeek(final long dateMillis) {
                seekTo(dateMillis);
            }

            @Override
            public void onPlay(long dateMillis) {
                sendStartPlayback();
            }

            @Override
            public void onPause(final long dateMillis) {
                
                sendPausePlayback(playbackHandler, dateMillis);
            }
        });

        timelineChart.setHistory(timelineHistory);
        
        /* Subscribe to the providers */
        subscribe();

        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                timelineChart.redraw(); //redraw to resize the chart
            }
        });
            }
    
    @Override
    public void seekTo(long seekTimeMs) {

        /* Make sure the seek time is within the bounds of the session */
        if (registeredSessionProvider.hasLogMetadata()) {
            final LogMetadata logMetadata = registeredSessionProvider.getLogMetadata();
            if (seekTimeMs < logMetadata.getStartTime()) {
                seekTimeMs = logMetadata.getStartTime();
            } else if (seekTimeMs > logMetadata.getEndTime()) {
                seekTimeMs = logMetadata.getEndTime();
            }
        }

        final long dateMillis = seekTimeMs;

        /* Notify the server to set the session playback to the selected
         * time */
        dashboardService.setSessionPlaybackTime(
                BrowserSession.getInstance().getBrowserSessionKey(),
                dateMillis,
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {

                        if (result.getWasSuccessful()) {
                            if (playbackHandler != null) {
                                playbackHandler.onSeek(dateMillis);
                            }
                            
                            // Scroll the date specified into view
                            timelineChart.scrollToShow(dateMillis);

                            /* If an ended playback successfully seeks;
                             * re-activate it */
                            if (registeredSessionProvider.hasLogMetadata()) {
                                final LogMetadata logMetadata = registeredSessionProvider.getLogMetadata();
                                final AbstractKnowledgeSession session = logMetadata.getSession();
                                final int dsId = session.getHostSessionMember().getDomainSessionId();
                                if (RunState.PLAYBACK_ENDED.equals(activeSessionProvider.getRunState(dsId))) {
                                    activeSessionProvider.addActiveSession(session);
                                    timelineChart.setPlayPauseButtonEnabled(true);
                                }
                            }
                            
                            if(capturedAudioElement != null) {
                                
                                //sync the captured audio to the current playback time (converted to seconds)
                                capturedAudioElement.setCurrentTime(timelineChart.getRelativePlaybackTime()/1000);  
                                
                                if(!timelineChart.isPaused() && !capturedAudioElement.hasEnded()) {
                                    
                                    //resume playing audio if the timeline is not paused and the audio has not finished playing
                                    capturedAudioElement.play();
                                }
                            }
                            
                        } else {
                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Set Playback Time",
                                    result.getException());
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        UiManager.getInstance().displayErrorDialog(
                                "Failed to Set Playback Time", 
                                "An error occurred while setting the session playback time: " + caught.toString(), 
                                null);
                    }
                });
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        
        //show the timeline's tooltips in this widget's container so they aren't cut off by the smaller timeline area
        timelineChart.setTooltipContainer(getParent().getElement());
    }
    
    @Override
    protected void onDetach() {
        super.onDetach();

        if(capturedAudioElement != null) {
            capturedAudioElement.pause();
            capturedAudioElement = null;
        }
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to the playhead changes. */
        TimelineProvider.getInstance().addManagedHandler(this);

        /* Subscribe to the filter */
        activeSessionProvider.addManagedHandler(this);
        
        /* Subscribe to the volume setting for the past session audio */
        Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().addManagedHandler(this);
        
        /* Subscribe to receive summative assessment changes */
        SummativeAssessmentProvider.getInstance().addManagedHandler(this);
    }

    @Override
    public void onResize() {
        timelineChart.redraw(); //redraw to resize the chart
    }

    /**
     * Send the start playback message to the server.
     */
    public void sendStartPlayback() {
        /* Notify the server to start the playback */
        dashboardService.startSessionPlayback(
                BrowserSession.getInstance().getBrowserSessionKey(), new AsyncCallback<GenericRpcResponse<Void>>() {

                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {

                        if (!result.getWasSuccessful()) {
                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Start Playback",
                                    result.getException());
                        } else {
                            //check the current session to see if it has any captured audio that needs to played alongside it
                            if (registeredSessionProvider.hasLogMetadata() 
                                    && registeredSessionProvider.getLogMetadata().getSession().getObserverControls() != null
                                    && registeredSessionProvider.getLogMetadata().getSession().getObserverControls().getCapturedAudioPath() != null) {
                               
                                // the relative path from the session specific output folder
                                String capturedAudioPath = registeredSessionProvider.getLogMetadata().getSession().getObserverControls().getCapturedAudioPath();
                                
                                if(capturedAudioElement == null) {
                                    final LogMetadata logMetadata = registeredSessionProvider.getLogMetadata();
                                    final String logFile = logMetadata.getLogFile();
                                    FileTreeModel logFileModel = FileTreeModel.createFromRawPath(logFile);
                                    FileTreeModel videoModel = FileTreeModel
                                            .createFromRawPath(logFileModel.getParentTreeModel().getRelativePathFromRoot()
                                                    + Constants.FORWARD_SLASH + capturedAudioPath);
                                
                                    //start playing back the captured audio
                                    capturedAudioElement = BsGameMasterPanel.playCapturedAudio(
                                            videoModel.getRelativePathFromRoot());
                                    capturedAudioElement.setCurrentTime(timelineChart.getRelativePlaybackTime()/1000);  
                                        
                                } else if(!capturedAudioElement.hasEnded()){
                                    
                                    //start playing the existing captured audio, unless it has reached the end of its playback
                                    capturedAudioElement.play();
                                }
                                
                            } else if(capturedAudioElement != null) {
                                
                                //if this session does not have captured audio, clean up any existing audio elements
                                capturedAudioElement.pause();
                                capturedAudioElement = null;
                            }
                            
                            TimelineProvider.getInstance().play();
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {

                        UiManager.getInstance().displayErrorDialog("Failed to Start Playback",
                                "An error occurred while starting the session playback: " + caught.toString(), null);
                    }
                });
    }
    
    /**
     * Refreshes the rendered timeline to reflect the underlying session data
     */
    public void refresh() {
        refresh(false);
    }

    /**
     * Refreshes the rendered timeline to reflect the underlying session data and, optionally,
     * attempts to preserve the visual state of any events currently in the timeline.
     * 
     * @param preserveVisualState whether to preserve the visual states of any events that are
     * still in the timeline after the refresh. This can be useful for keeping an event expanded
     * or collapsed after the timeline is refreshed.
     */
    public void refresh(final boolean preserveVisualState) {
        
        if(timelineInfo == null) {
            return;
        }

        /* defer the rendering logic for a bit so the loading dialog has time to
         * show */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                /* Broke the implementation out into a separate method so the
                 * entire code block wasn't double-indented */
                refreshImpl(preserveVisualState);
            }
        });
    }

    /**
     * The implementation for the refresh method.
     * 
     * @param preserveVisualState whether to preserve the visual states of any
     *        events that are still in the timeline after the refresh. This can
     *        be useful for keeping an event expanded or collapsed after the
     *        timeline is refreshed.
     */
    private void refreshImpl(final boolean preserveVisualState) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Building timeline from received learner states");
        }
        
        if(!TimelineProvider.getInstance().isPaused()) {
            
            resumeWhenReloadFinished = true;
            
            /* If the session is currently playing back, pause it to reduce the rendering and server load. 
             * The user can't actually see the session playing back during this period, 
             * so it makes sense to pause it.*/
            sendPausePlayback(playbackHandler, TimelineProvider.getInstance().getPlaybackTime());
            
            timelineChart.pause();
        }
        
        final long startTime = System.currentTimeMillis();
        
        /* If visual state is being preserved, save the chart's current vertical scroll position */
        if(preserveVisualState && chartScroll == null) {
            chartScroll = timelineChart.getScrollTop(); 
        }

        componentLoadingProvider.startLoading(LoadingType.TIMELINE_REFRESH, LoadingPriority.HIGH, "Drawing Timeline",
                "Please wait while the session data is rendered onto the timeline...");

        final boolean isNewSession = timelineHistory == null;
        final TimelineHistory oldHistory = preserveVisualState ? timelineHistory : null;
        final LogMetadata logMeta = registeredSessionProvider.getLogMetadata();
        final Integer domainSessionId = logMeta == null ? null
                : logMeta.getSession().getHostSessionMember().getDomainSessionId();
        timelineHistory = null;

        /* Callback to be executed with the timeline is finished building */
        final AsyncCallback<Long> timelineBuiltCallback = new AsyncCallback<Long>() {
            @Override
            public void onFailure(Throwable t) {
                logger.severe("Failed to build the timeline because " + t);
                componentLoadingProvider.loadingComplete(LoadingType.TIMELINE_REFRESH);
            }

            @Override
            public void onSuccess(Long firstPerformanceStateTime) {
                /* Populates the chart with timeline data */
                onTimelineBuilt(isNewSession, domainSessionId, firstPerformanceStateTime);
                componentLoadingProvider.loadingComplete(LoadingType.TIMELINE_REFRESH);
            }
        };

        /* Nothing to build, assume complete. */
        if (logMeta == null || domainSessionId == null) {
            timelineBuiltCallback.onSuccess(null);
            return;
        }

        if (isNewSession) {
            /* need to set the start time so that other logic (e.g. strategy
             * history bubble labels) can use it to before the playback starts
             * and the first message is received */
            TimelineProvider.getInstance().setPlaybackTime(logMeta.getStartTime());
        }

        /* Create history */
        timelineHistory = TimelineHistory.create(logMeta.getStartTime(), logMeta.getEndTime());

        globalBookmarksEvent = null;
        lastGlobalBookmark = null;
        courseConceptNodes.clear();
        Long firstPerformanceStateTime = null;
        final TreeMap<Long, LearnerState> learnerStates = timelineInfo.getLearnerStates();
        final Map<Integer, PerfNodeChange> perfNodeToLastChange = new HashMap<>();

        /* calculate task sort order based on when the task starts the first
         * time this requires a look at all learner states provided by the
         * server and doesn't consider if the task becomes active for a
         * subsequent time after being not active. */
        final Set<Integer> taskIdsSorted = new LinkedHashSet<>();
        /*
         * keep track of tasks that were never activated so they too can be added to the timeline, if only
         * as just a series of hierarchy labels
         */
        final Set<Integer> unactivatedTasks = new HashSet<>();
        for (Long timestamp : learnerStates.keySet()) {
            final LearnerState state = timelineInfo.getLearnerStates().get(timestamp);
            if (state.getPerformance() == null) {
                continue;
            }

            Map<Integer, TaskPerformanceState> taskStates = state.getPerformance().getTasks();

            /* look at the tasks w/in this performance assessment */
            for (Integer taskId : taskStates.keySet()) {

                if (taskIdsSorted.contains(taskId)) {
                    /* the task was already found to be started at some point in
                     * a previous learner state */
                    continue;
                }

                TaskPerformanceState taskState = taskStates.get(taskId);
                if (taskState.getState().getNodeStateEnum() == PerformanceNodeStateEnum.ACTIVE) {
                    // this should be the first time this task was ever active
                    taskIdsSorted.add(taskId);
                    // the task is activated at least once, therefore it should be removed from the set of never activated tasks
                    unactivatedTasks.remove(taskId);
                }else if(taskState.getState().getNodeStateEnum() == PerformanceNodeStateEnum.UNACTIVATED){
                    // this task has still never been activated
                    // Note: that the check at the beginning of the for loop and the continue loop call means
                    // we don't have to check if the taskIdsSorted doesn't contain the task id, i.e. we won't
                    // accidently add a task as unactivated after it was marked as activated
                    unactivatedTasks.add(taskId);
                }
                
                /* When looking through the task states for the first time, figure out what performance nodes
                 * are course concepts that should appear in the summative mode */
                courseConceptNodes.addAll(gatherCourseConcepts(taskState));
            }

            if (firstPerformanceStateTime == null && !taskIdsSorted.isEmpty()) {
                /* This is the first learner state containing the tasks, so
                 * store it in case it is needed later */
                firstPerformanceStateTime = timestamp;
            }
        }
        
        /*
         * Add those tasks that never were activated in order to ensure they show up in the timeline at
         * least as labels
         */
        taskIdsSorted.addAll(unactivatedTasks);

        /* Now that we have the first performance state time (even if null);
         * finalize it to be used in the callback */
        final Long firstPerfStateTime = firstPerformanceStateTime;

        final AsyncCallback<Boolean> learnerStatesProcessedCallback = new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable t) {
                timelineBuiltCallback.onFailure(t);
            }

            @Override
            public void onSuccess(Boolean value) {
                onLearnerStatesProcessed(isNewSession, domainSessionId, perfNodeToLastChange, logMeta, oldHistory);
                timelineBuiltCallback.onSuccess(firstPerfStateTime);
            }
        };

        /* Iterate through every learner state to determine the history for each
         * task in the session */
        final Iterator<Entry<Long, LearnerState>> learnerStatesItr = learnerStates.entrySet().iterator();

        /* Use the scheduler because this can be a very intensive and
         * long-running process. The commands in this queue are invoked many
         * times in rapid succession and are then deferred to allow the browser
         * to process its event queue. */
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            @Override
            public boolean execute() {
                /* Check if we have completed the list */
                if (!learnerStatesItr.hasNext()) {
                    learnerStatesProcessedCallback.onSuccess(true);
                    
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Finished refreshing timeline in " + (System.currentTimeMillis() - startTime) + "ms");
                    }
                    
                    if(resumeWhenReloadFinished) {
                        
                        resumeWhenReloadFinished = false;
                        
                        /* We suspended a playback for the reload, so need to resume it now that it is done */
                        sendStartPlayback();
                        timelineChart.play();
                        TimelineProvider.getInstance().play();
                    }
                    
                    return false;
                }

                /* Grab next learner state */
                final Entry<Long, LearnerState> entry = learnerStatesItr.next();
                final Long timestamp = entry.getKey();
                final LearnerState state = entry.getValue();
                if (state.getPerformance() == null) {
                    /* Can't process this learner state; skip to the next one */
                    return true;
                }

                final String globalBookmarkEvaluator = state.getPerformance().getEvaluator();
                final String globalBookmarkComment = state.getPerformance().getObserverComment();
                final String globalBookmarkMedia = state.getPerformance().getObserverMedia();

                addGlobalBookmarkToTimeline(timestamp, globalBookmarkComment, globalBookmarkMedia,
                        globalBookmarkEvaluator, false);

                if (isNewSession && (globalBookmarkComment != null || globalBookmarkMedia != null)) {

                    /* if this is a new session, notify any listeners when
                     * bookmarks are found */
                    BookmarkProvider.getInstance().addBookmark(domainSessionId, globalBookmarkEvaluator, timestamp,
                            globalBookmarkComment, globalBookmarkMedia, false);
                }

                final Map<Integer, TaskPerformanceState> taskStates = state.getPerformance().getTasks();
                final Set<String> patchedAttributes = new HashSet<>();
                if (timelineInfo.getPatchedLearnerStatePerformances().containsKey(timestamp)) {
                    patchedAttributes.addAll(timelineInfo.getPatchedLearnerStatePerformances().get(timestamp));
                }

                /* get the state of each task when this learner state was
                 * applied using the sorted task ids */
                final boolean showSingularTask = showOnlyTaskIds != null && showOnlyTaskIds.size() == 1;
                for (Integer taskId : taskIdsSorted) {
                    if (CollectionUtils.isNotEmpty(showOnlyTaskIds) && !showOnlyTaskIds.contains(taskId)) {
                        continue;
                    }

                    final TaskPerformanceState taskState = taskStates.get(taskId);
                    if (taskState == null) {
                        /* just a safety check since it could be possible one
                         * day that not all tasks are in all learner states */
                        continue;
                    } else if (taskState.getState() != null
                            && CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME.equals(taskState.getState().getName())) {
                        /* hide the course concept task that is artificially
                         * added to track course concepts across course objects.
                         * Currently, during a real time assessment (DKF) the
                         * course concepts task is not updated so the timeline
                         * would just show empty lines with no data. */
                        continue;
                    }
                    
                    if(SummativeAssessmentProvider.getInstance().getDisplayMode() == AssessmentDisplayMode.SUMMATIVE 
                            && !isCourseConcept(taskState.getState().getName())) {
                        
                        /* Do not show non-course concepts when displaying summative assessments */
                        continue;
                    }
                    
                    boolean taskActive = taskState.getState() != null
                            && PerformanceNodeStateEnum.ACTIVE.equals(taskState.getState().getNodeStateEnum());

                    boolean isTaskPatched = taskState.getState() != null
                            && patchedAttributes.contains(taskState.getState().getName());

                    /* create/update the timeline event corresponding to this
                     * task */
                    TimelineEvent taskEvent = addPerfStateToTimeline(timestamp, taskState.getState(), taskActive,
                            isTaskPatched, perfNodeToLastChange, taskState);

                    if (taskEvent == null) {
                        continue;
                    } else if (showSingularTask) {
                        taskEvent.setCollapsed(false);
                    }

                    /* add the task's concepts to the timeline */
                    addConceptStatesToTimeline(timestamp, taskState.getConcepts(), taskActive, patchedAttributes,
                            perfNodeToLastChange, taskEvent, taskState);
                }

                /* Move on to the next learner state */
                return true;
            }
        });
    }

    /**
     * Populates the chart with timeline data. This should only be called after
     * the timeline is finished building.
     * 
     * @param isNewSession true if this is a new session being built; false
     *        otherwise.
     * @param domainSessionId the optional domain session id.
     * @param firstPerformanceStateTime the optional time for the first valid
     *        performance state.
     */
    private void onTimelineBuilt(boolean isNewSession, Integer domainSessionId, Long firstPerformanceStateTime) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Finished building timeline. Populating chart with timeline data.");
        }

        /* push the task timeline data into the chart */
        boolean startPlayback = true;
        if (domainSessionId != null) {
            final RunState runState = ActiveSessionProvider.getInstance().getRunState(domainSessionId);
            if (RunState.PLAYBACK_ENDED.equals(runState)) {
                startPlayback = false;
            }
        }

        timelineChart.setHistory(timelineHistory);
        if (isNewSession) {
            timelineChart.setPlayPauseButtonEnabled(true);
            timelineChart.restart().pause();
            timelineChart.showInitialTooltips();
            if (firstPerformanceStateTime != null) {
                seekTo(firstPerformanceStateTime);
            }
        } else if (startPlayback) {
            timelineChart.setPlayPauseButtonEnabled(true);

            if (!timelineChart.isPaused()) {
                timelineChart.restart().play();
                TimelineProvider.getInstance().reset();
                sendStartPlayback();
                
            } else {
                
                /* Attempt to move the playhead to the current playback time position. This is only needed if playback is
                 * NOT being started, since playing back automatically updates the playhead position.
                 * 
                 * If this is not done, then the playhead will move ahead to the end of the timeline whenever
                 * the timeline is cleared, since clearing the timeline shifts its start/end times (since doing so
                 * unloads the current history and its stat/end times) */
                try {
                    timelineChart.seek(TimelineProvider.getInstance().getPlaybackTime());
                    
                } catch(Exception e) {
                    logger.severe("Failed to seek timeline playhead position to current playback time." + e);
                }
            }
        }
        
        if(chartScroll != null) {
            
            final int scroll = chartScroll;
            
            /* Need to defer re-applying the vertical scroll position. 
             * 
             * If we do not do this, then the vertical scrolling ends up triggering a redraw that can interfere with 
             * the horizontal scrolling that's triggered by seekTo(long). This causes a problem where changing an 
             * assessment while the horizontal scrollbar is visible and the playhead is near the end of the timeline
             * will cause the playhead to be scrolled out of view. 
             */
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                
                @Override
                public void execute() {
                    /* Apply the preserved vertical scroll position to the chart */
                    timelineChart.setScrollTop(scroll);
                }
            });
            
            chartScroll = null;
        }
    }

    /**
     * Populates the chart with timeline data. This should only be called after
     * the timeline is finished building.
     * 
     * @param isNewSession true if this is a new session being built; false
     *        otherwise.
     * @param domainSessionId the optional domain session id.
     * @param perfNodeToLastChange the map of performance id nodes to their last
     *        update change. Can't be null.
     * @param logMeta the log metadata. Can't be null.
     * @param oldHistory the previous timeline history.
     */
    private void onLearnerStatesProcessed(boolean isNewSession, Integer domainSessionId,
            Map<Integer, PerfNodeChange> perfNodeToLastChange, LogMetadata logMeta, TimelineHistory oldHistory) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Finished processing the learner states for the timeline.");
        }

        /* if any tasks are still active after all of the session's learner
         * states have been processed, extend those tasks' timeline events to
         * the end of the session */
        for (PerfNodeChange lastChange : perfNodeToLastChange.values()) {
            if (lastChange.getStatus() != null && lastChange.getLatestInterval() != null) {
                lastChange.getLatestInterval().setEndDate(JsDate.create(logMeta.getEndTime()));
            }
        }

        /* Add any applied strategies to the timeline */
        strategiesEvent = null;
        for (Long timestamp : timelineInfo.getStrategies().keySet()) {

            StrategyStateUpdate strategies = timelineInfo.getStrategies().get(timestamp);

            if (strategiesEvent == null) {

                /* create an event for the strategies if none exists yet */
                strategiesEvent = timelineHistory.addEvent(STRATEGIES_EVENT_ID, "Scenario Injects Acted Upon", true);
            }

            if (isNewSession) {

                /* Notify other panels when a past session with applied
                 * strategies is being loaded for the first time */
                StrategyProvider.getInstance().addAppliedStrategies(strategies.getAppliedStrategies(), domainSessionId,
                        strategies.getEvaluator(), timestamp);
            }

            Collection<Strategy> stratsCollection = StrategyProvider
                    .aggregateStrategies(strategies.getAppliedStrategies());

            /* Add an event for each set of strategies at the appropriate
             * timestamp */
            for (Strategy strat : stratsCollection) {
                addStrategiesToTimeline(timestamp, strat, strategies.getEvaluator());
            }
        }

        if (oldHistory != null) {
            /* need to preserve the visual state from the previous timeline */
            oldHistory.applyCollapseState(timelineHistory);
        }
    }

    /**
     * Reloads all of the timeline-related data in the session and refreshes the rendered timeline 
     * to reflect the underlying session data and, optionally, attempts to preserve the visual state 
     * of any events currently in the timeline.
     */
    public void reload() {
        reload(false);
    }
    
    /**
     * Reloads all of the timeline-related data in the session and refreshes the rendered timeline 
     * to reflect the underlying session data and, optionally, attempts to preserve the visual state 
     * of any events currently in the timeline.
     * 
     * @param preserveVisualState whether to preserve the visual states of any events that are
     * still in the timeline after the refresh. This can be useful for keeping an event expanded
     * or collapsed after the timeline is refreshed.
     */
    public void reload(final boolean preserveVisualState) {
        componentLoadingProvider.startLoading(LoadingType.TIMELINE_RELOAD, "Loading Timeline",
                "Please wait while the session data is loaded from the server");
        
        /* If visual state is being preserved, save the chart's current vertical scroll position */
        if(preserveVisualState) {
            chartScroll = timelineChart.getScrollTop();
        }

        timelineChart.clear();
        
        if(capturedAudioElement != null) {
            capturedAudioElement.pause();
        }

        //request the learner states needed to build the timeline from the server
        final String bsk = BrowserSession.getInstance().getBrowserSessionKey();
        dashboardService.fetchLearnerStatesForSession(bsk,
                new AsyncCallback<GenericRpcResponse<SessionTimelineInfo>>() {

            @Override
            public void onSuccess(final GenericRpcResponse<SessionTimelineInfo> result) {

                if(result == null) {
                    componentLoadingProvider.loadingComplete(LoadingType.TIMELINE_RELOAD);
                    
                    UiManager.getInstance().displayErrorDialog(
                            "Failed to Build Timeline", 
                            "An unknown error was encountered while building the session timeline.", 
                            null);
                    return;

                } else if(!result.getWasSuccessful()){
                    componentLoadingProvider.loadingComplete(LoadingType.TIMELINE_RELOAD);
                    
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Failed to Build Timeline", 
                            result.getException());
                    return;

                }
                
                //update the stored data for this session
                timelineInfo = result.getContent();
                
                /* Clear any summative assessments that were previously shown */
                perfNodeNameToSummativeAssessment.clear();
                
                /* See if there are summative assessments to load and, if so, gather them */
                if(timelineInfo.getScenarioInfo() != null) {
                    
                    if(timelineInfo.getScenarioInfo().getCurrentScore() != null) {
                        gatherSummativeAssessments(timelineInfo.getScenarioInfo().getCurrentScore());
                    }
                    
                    if(timelineInfo.getScenarioInfo().getCourseConcepts() != null) {
                        
                        /* Populate the course concepts */
                        CourseConceptProvider.get().updateCourseConcepts(timelineInfo.getScenarioInfo().getCourseConcepts());
                    }
                }
                
                if(!timelineInfo.getLearnerStates().isEmpty() 
                        && registeredSessionProvider.getLogMetadata() != null 
                        && registeredSessionProvider.getLogMetadata().getSession() != null) {
                    
                    /* 
                     * #5597 - For past sessions specifically, StatePane can get into a bad state if the timestamp of the LessonStarted
                     * message just so happens to have a nearby learner state that doesn't have any tasks in it. Such learner states are
                     * valid since they are used to reset the current learner information across modules, but they can cause the loading
                     * dialog in StatePane to get stuck permanently waiting for task info.
                     * 
                     * The logic below acts as a backup to handle this this edge case. If the timeline contains any learner states that 
                     * have task information in them, we can provide the first of those to StatePane instead to initialize it.
                     */
                    
                    LearnerState firstStateWithTasks = null;
                    long searchStart = System.currentTimeMillis();
                    for(Long timestamp : timelineInfo.getLearnerStates().keySet()) {
                        LearnerState state = timelineInfo.getLearnerStates().get(timestamp);
                        
                        if(state != null && state.getPerformance() != null && !state.getPerformance().getTasks().isEmpty()) {
                            
                            /* This learner state has the list of tasks, so load it */
                            firstStateWithTasks = state;
                            break;
                            
                        } else if(System.currentTimeMillis() - searchStart > 5000) {
                            
                            /* If searching for the first learner state with the task information takes too long, then abort the search. 
                             * We don't want to leave the learner stuck waiting for this to complete since it's just a backup operation */
                            break;
                        }
                    }
                    
                    if(firstStateWithTasks != null) {
                        
                        /* Pass the first learner state with task information to the StatePane to initialize it */
                        KnowledgeSessionState initState = new KnowledgeSessionState(firstStateWithTasks);
                        SessionStateProvider.getInstance().sessionStateUpdate(initState, registeredSessionProvider.getLogMetadata().getSession().getHostSessionMember().getDomainSessionId());
                    
                    } else {
                        
                        /* This shouldn't be possible unless something is seriously wrong with the log file, so log it if it does somehow occur */
                        logger.severe("Failed to find any learner states containing task information. This may cause issues loading some components");
                    }
                }
                
                SummativeAssessmentProvider.getInstance().setSummativeAssessments(perfNodeNameToSummativeAssessment);
                
                //refresh the UI to reflect the new data
                refresh(preserveVisualState);
                componentLoadingProvider.loadingComplete(LoadingType.TIMELINE_RELOAD);
            }

            @Override
            public void onFailure(Throwable caught) {
                failure(caught);
            }

            /**
             * The actions to perform on a failed reload.
             * 
             * @param caught the throwable exception that caused the failure.
             */
            private void failure(Throwable caught) {
                componentLoadingProvider.loadingComplete(LoadingType.TIMELINE_RELOAD);

                timelineHistory = null;
                
                UiManager.getInstance().displayErrorDialog(
                        "Failed to Build Timeline", 
                        "An error occurred while building the session timeline: " + caught.toString(), 
                        null);

                timelineChart.setHistory(timelineHistory);
            }
        });
    }

    /**
     * Adds the given set of strategies to apply to the timeline at the given timestamp
     * 
     * @param timestamp the timestamp where the applied strategies should be placed in the timeline. Cannot be null.
     * @param strategy the set of strategies being applied that need an event created. Cannot be null.
     */
    private void addStrategiesToTimeline(final Long timestamp, final Strategy strategy, String evaluator) {
           
       // Default to a generic icon for one or more strategies
       IntervalType type = IntervalType.STRATEGY;
       if(strategy.getStrategyActivities() != null && strategy.getStrategyActivities().size() == 1) {
           
           // Only one strategy was applied, so show a more-specific icon for it
           Serializable activity = strategy.getStrategyActivities().get(0);
           
           if(activity instanceof InstructionalIntervention) {
               
               type = IntervalType.FEEDBACK;
               Feedback feedback = ((InstructionalIntervention) activity).getFeedback();
               
               Serializable presentation = feedback.getFeedbackPresentation();
               if(presentation instanceof Message) {
                   type = IntervalType.PRESENT_MESSAGE;
                   
               } else if(presentation instanceof Audio) {
                   type = IntervalType.PLAY_AUDIO;
                   
               } else if(presentation instanceof MediaSemantics) {
                   type = IntervalType.AVATAR_SCRIPT;
                   
               } else if(presentation instanceof File) {
                   type = IntervalType.FEEDBACK_LOCAL_WEBPAGE;
               }
           
            } else if(activity instanceof ScenarioAdaptation) {
                
                type = IntervalType.MODIFY_SCENARIO;
                ScenarioAdaptation adaptation = (ScenarioAdaptation) activity;
                
                Serializable envType = adaptation.getEnvironmentAdaptation().getType();
                if(envType instanceof EnvironmentAdaptation.Overcast) {
                    type = IntervalType.OVERCAST;
                    
                } else if(envType instanceof EnvironmentAdaptation.Fog) {
                    type = IntervalType.FOG;
                    
                } else if(envType instanceof EnvironmentAdaptation.Rain) {
                    type = IntervalType.RAIN;
                    
                } else if(envType instanceof EnvironmentAdaptation.TimeOfDay) {
                    type = IntervalType.TIME_OF_DAY;
                    
                } else if(envType instanceof EnvironmentAdaptation.CreateActors) {
                    type = IntervalType.CREATE_ACTORS;
                    
                } else if(envType instanceof EnvironmentAdaptation.RemoveActors) {
                    type = IntervalType.REMOVE_ACTORS;
                    
                } else if(envType instanceof EnvironmentAdaptation.CreateBreadcrumbs) {
                    type = IntervalType.BREADCRUMBS;
                    
                } else if(envType instanceof EnvironmentAdaptation.RemoveBreadcrumbs) {
                    type = IntervalType.REMOVE_BREADCRUMBS;
                    
                } else if(envType instanceof EnvironmentAdaptation.HighlightObjects) {
                    type = IntervalType.HIGHLIGHT;
                    
                } else if(envType instanceof EnvironmentAdaptation.RemoveHighlightOnObjects) {
                    type = IntervalType.REMOVE_HIGHLIGHT;
                    
                } else if(envType instanceof EnvironmentAdaptation.Teleport) {
                    type = IntervalType.TELEPORT;
                    
                } else if(envType instanceof EnvironmentAdaptation.FatigueRecovery) {
                    type = IntervalType.FATIGUE_RECOVERY;
                    
                } else if(envType instanceof EnvironmentAdaptation.Endurance) {
                    type = IntervalType.ENDURANCE;
                    
                } else if(envType instanceof EnvironmentAdaptation.Script) {
                    type = IntervalType.SCRIPT;
                }
                
            } else if(activity instanceof MidLessonMedia) {
                
                type = IntervalType.PRESENT_MEDIA;
                LessonMaterialList material = ((MidLessonMedia) activity).getLessonMaterialList();
                
                if(material.getMedia().size() == 1) {
                    for(Media media : material.getMedia()) {
                        
                        Serializable mediaType = media.getMediaTypeProperties();
                        if(mediaType instanceof PDFProperties) {
                            type = IntervalType.PDF;
                            
                        } else if(mediaType instanceof WebpageProperties) {
                            type = IntervalType.MEDIA_LOCAL_WEBPAGE;
                            
                        } else if(mediaType instanceof YoutubeVideoProperties) {
                            type = IntervalType.YOUTUBE_VIDEO;
                            
                        } else if(mediaType instanceof ImageProperties) {
                            type = IntervalType.LOCAL_IMAGE;
                            
                        } else if(mediaType instanceof SlideShowProperties) {
                            type = IntervalType.SLIDE_SHOW;
                        }
                    }
                }
                
            } else if(activity instanceof PerformanceAssessment) {
                type = IntervalType.PRESENT_SURVEY;
            }
       }
       
       // Create an event interval for this set of strategies
       EventInterval interval = strategiesEvent.addInterval(timestamp, timestamp, SessionIntervalStatus.create(type));
       interval.setClickFunction(new IntervalClickHandler() {
           
           @Override
           public void onClick(Element context, EventInterval d, int index, long clickTime) {
               
               if(strategySelector != null) {
                   strategySelector.selectStrategy(strategy.getName(), timestamp);
               }
               
               //make the playhead jump to the timestamp of this strategy
               seekTo(timestamp);
           }
       });
       
       /* Display a tooltip when the mouse hovers over the interval. If the
        * strategy was applied by an OC, show the OC's name */
       if(evaluator != null) {
           
           interval.setTooltip(buildHtmlTooltip(
                   "Applied by " + evaluator, 
                   STRATEGY_CLICK_TOOLTIP));
           
       } else {
           interval.setTooltip(STRATEGY_CLICK_TOOLTIP);
       }
   }
    
    /**
     * Adds the given global bookmark to the timeline at the given timestamp
     * 
     * @param timestamp the timestamp of the bookmark. Cannot be null.
     * @param comment the comment text of the bookmark. Can be null.
     * @param media the location of the media file associated with the bookmark, if any. Can be null.
     * @param evaluator the observer controller that made the bookmark, if any. Can be null.
     * @param isPatched whether the given global bookmark has been patched into a past session log.
     */
    private void addGlobalBookmarkToTimeline(final Long timestamp, String comment, String media, String evaluator, boolean isPatched) {
        
        if(timestamp == null) {
            throw new IllegalArgumentException("The timestamp for a global bookmark timeline event cannot be null");
        }
        
        boolean hasMedia = media != null;
        final String nextBookmark = hasMedia ? media : comment;
        
        if(nextBookmark == null || Objects.equals(lastGlobalBookmark, nextBookmark)) {
            return;
        }
        
        if(globalBookmarksEvent == null) {
            
            //create an event for the strategies if none exists yet
            globalBookmarksEvent = timelineHistory.addEvent(GLOBAL_BOOKMARKS_EVENT_ID, "Notes", true);
            lastGlobalBookmark = null;
        }
        
        EventInterval interval = globalBookmarksEvent.addInterval(timestamp, timestamp, SessionIntervalStatus.create(IntervalType.BOOKMARK));
        
        if(hasMedia) {

           interval.setTooltip(GLOBAL_BOOKMARK_CLICK_TOOLTIP);

        } else {
            
            //display this bookmark's text in a tooltip
            final String tooltip = StringUtils.isNotBlank(nextBookmark)
                    ? nextBookmark + (isPatched ? " " + PATCH_TOOLTIP : "")
                    : isPatched ? PATCH_TOOLTIP : null;
                    
            interval.setTooltip(buildHtmlTooltip(tooltip, GLOBAL_BOOKMARK_CLICK_TOOLTIP));
        }
        
        interval.setClickFunction(new IntervalClickHandler() {
            
            @Override
            public void onClick(Element context, EventInterval d, int index, long clickTime) {
                
                if(bookmarkSelector != null) {
                    bookmarkSelector.selectBookmark(timestamp);
                }
                
                //make the playhead jump to the timestamp of this bookmark
                seekTo(timestamp);
            }
        });
        
        lastGlobalBookmark = nextBookmark;
    }
    
    /**
     * Parses performance information from the provided concept states and, if needed, updates the
     * timeline so that it accurately reflects said state information. {@link TimelineEvent}s will
     * be generated for the concepts associated with the given states if they do not yet exist, 
     * even if the concept's performance state does not change. If a concept's state does change and
     * that node is active, then an {@link EventInterval} will be added to represent how the state changed 
     * in the time since the last performance update.
     * <br/><br/>
     * This method mainly differs from {@link #addPerfStateToTimeline(Long, PerformanceStateAttribute, boolean, boolean, Map)} 
     * in that it takes in a <i>list</i> of concepts rather than just one and also recursively calls itself for <i>all</i> 
     * subconcepts that are found within the provided conceps. This is used to build timeline events for an
     * entire concept hierarchy rather than just individual concepts.
     * 
     * @param timestamp the timestamp of the performance update. Used to build the event interval when needed. Cannot be null.
     * @param concepts the concept states to evaluate. If null, this method will do nothing.
     * @param taskActive whether the concepts' parent task is active
     * @param patchedAttributes the performance attributes that have been patched in by the user. Cannot be null.
     * @param perfNodeToLastChange a mapping from each performance node to the last change that was derived from. Cannot be null.
     * one of its performance states. Used to build the event interval when needed. Cannot be null.
     * @param parentEvent the parent event that the events for the given concepts should be placed under. Can be null.
     * @param parentTaskState the task performance state that ultimately acts as the parent for this node. Cannot be null.
     */
    private void addConceptStatesToTimeline(final Long timestamp, List<ConceptPerformanceState> concepts, 
            boolean taskActive, Set<String> patchedAttributes, 
            Map<Integer, PerfNodeChange> perfNodeToLastChange, TimelineEvent parentEvent,
            TaskPerformanceState parentTaskState) {
        
        if(concepts == null) {
            return;
        }
        
        for(ConceptPerformanceState concept : concepts) {
            final PerformanceStateAttribute cStateAttr = concept.getState();

            boolean isConceptPatched = cStateAttr != null && patchedAttributes.contains(cStateAttr.getName());
            boolean isIntermediateConcept = concept instanceof IntermediateConceptPerformanceState;
            
            if(SummativeAssessmentProvider.getInstance().getDisplayMode() == AssessmentDisplayMode.SUMMATIVE 
                    && !isCourseConcept(concept.getState().getName())) {
                
                /* Do not show non-course concepts when displaying summative assessments */
                continue;
            }
            
            if(Dashboard.getInstance().getSettings().isShowOcOnly() 
                    && !concept.isContainsObservedAssessmentCondition()) {
                
                //if the setting is enabled, hide concepts that do not require an OC assessment
                // Note: this also means that no descendants have Observed conditions
                continue;
            }
            
            boolean hasPreviousState = cStateAttr != null && perfNodeToLastChange.get(cStateAttr.getNodeId()) != null;
            boolean isPoorPerforming = getDisplayedAssessment(concept.getState()).isPoorPerforming();

            if(Dashboard.getInstance().getSettings().isHideGoodAutoAssessments() &&
                    !hasPreviousState &&
                    !concept.isContainsObservedAssessmentCondition() && 
                    !isPoorPerforming &&
                    !isIntermediateConcept){
                    
                // good automated assessments should be hidden unless:
                // 1. the concept was shown before with a poor performance, i.e. if before below expectation was shown and then At expectation is skipped the concept will show Below forever
                // 2. the concept is an intermediate concept and has children that may need to be drawn because they are poor performing (but the roll up results in good performing - maybe possible)
                //    The logic below will remove this intermediate concept if no descendants are drawn.
                continue;
            }
            
            if(isIntermediateConcept && 
                    !willChildStateBeRendered(((IntermediateConceptPerformanceState) concept).getConcepts(), 
                            perfNodeToLastChange)){
                // will any children be added to this intermediate concept, if not than skip this intermediate concept
                continue;
            }

            //create/update the timeline event corresponding to this concept
            TimelineEvent conceptEvent = addPerfStateToTimeline(timestamp, 
                    cStateAttr, 
                    taskActive,
                    isConceptPatched,
                    perfNodeToLastChange,
                    parentTaskState);
            
            if(conceptEvent == null){
                continue;
            }
            
            conceptEvent.setParent(parentEvent);
            
            if(isIntermediateConcept) {
                addConceptStatesToTimeline(timestamp, ((IntermediateConceptPerformanceState) concept).getConcepts(), 
                        taskActive, patchedAttributes, perfNodeToLastChange, conceptEvent, parentTaskState);
            }
        }
    }
        
    /**
     * Return true if any of the provided concepts will be rendered in the timeline based on current settings
     * and the attributes of the concept performance state.  This will also check all descendant concepts if any
     * of the concepts are intermediate nodes.
     * @param concepts contains one or more concepts that are child to a task or an intermediate concept. If
     * null or empty, this method returns false.
     * @param perfNodeToLastChange contains the last performance node state of concepts.  Used to compare if the
     * last state is different than the current state for a concept.
     * @return true if there is at least one child or descendant concept that would be rendered if this collection
     * of concepts is given to the logic to render concepts based on the current concept state(s) and settings.
     */
    private boolean willChildStateBeRendered(final List<ConceptPerformanceState> concepts, 
            Map<Integer, PerfNodeChange> perfNodeToLastChange){
        
        boolean foundOne = false;
        if(CollectionUtils.isEmpty(concepts)){
            return foundOne;
        }
        
        for(ConceptPerformanceState concept : concepts) {
            final PerformanceStateAttribute perfState = concept.getState();
        
            if (perfState == null) {
                continue;
            }else if(perfState.isScenarioSupportNode() && !Dashboard.getInstance().getSettings().isShowScenarioSupport()){
                continue;
            }
            
            boolean isIntermediateConcept = concept instanceof IntermediateConceptPerformanceState;
            
            if(Dashboard.getInstance().getSettings().isShowOcOnly() 
                    && !concept.isContainsObservedAssessmentCondition()) {
                
                //if the setting is enabled, hide concepts that do not require an OC assessment
                // Note: this also means that no descendants have Observed conditions
                continue;
            }
            
            boolean hasPreviousState = perfNodeToLastChange != null && perfNodeToLastChange.get(perfState.getNodeId()) != null;
            boolean isPoorPerforming = getDisplayedAssessment(concept.getState()).isPoorPerforming();

            if(Dashboard.getInstance().getSettings().isHideGoodAutoAssessments() &&
                    !hasPreviousState &&
                    !concept.isContainsObservedAssessmentCondition() && 
                    !isPoorPerforming &&
                    !isIntermediateConcept){
                    
                // good automated assessments should be hidden unless:
                // 1. the concept was shown before with a poor performance, i.e. if before below expectation was shown and then At expectation is skipped the concept will show Below forever
                // 2. the concept is an intermediate concept and has children that may need to be drawn because they are poor performing (but the roll up results in good performing - maybe possible)
                //    The logic below will remove this intermediate concept if no descendants are drawn.
                continue;
            }
            
            if(concept instanceof IntermediateConceptPerformanceState){
                if(!willChildStateBeRendered(((IntermediateConceptPerformanceState) concept).getConcepts(), 
                        perfNodeToLastChange)){
                    continue;
                }
            }
            
            foundOne = true;
            break;
        }
        
        return foundOne;
    }

    /**
     * Parses performance information from the provided performance state and, if needed, updates the
     * timeline so that it accurately reflects said performance information. A {@link TimelineEvent} will
     * be generated for the performance node associated with the given state if one does not exist, 
     * even if the node's performance state does not change. If a performance node's state does change and
     * that node is active, then an {@link EventInterval} will be added to represent how the state changed 
     * in the time since the last performance update.
     * 
     * @param timestamp the timestamp of the performance update. Used to build the event interval when needed.
     * @param perfState the performance state to evaluate. If null, this method will do nothing and return null.
     * @param taskActive whether the performance node's parent task is active
     * @param isPatched true if this performance state has been patched; false otherwise.
     * @param perfNodeToLastChange a mapping from each performance node to the last change that was derived from
     * one of its performance states. Used to build the event interval when needed. Cannot be null.
     * @param parentTaskState parentTaskState the task performance state that ultimately acts as 
     * the parent for this node. Cannot be null.
     * @return the timeline event that was created for the performance node associated with the given state, or
     * the existing timeline event, if such an event was already added to the timeline. Can be null.
     */
    protected TimelineEvent addPerfStateToTimeline(final Long timestamp, final PerformanceStateAttribute perfState,
            boolean taskActive, boolean isPatched, Map<Integer, PerfNodeChange> perfNodeToLastChange, 
            final TaskPerformanceState parentTaskState) {
        if (perfState == null) {
            return null;
        }else if(perfState.isScenarioSupportNode() && !Dashboard.getInstance().getSettings().isShowScenarioSupport()){
            return null;
        }

        final TimelineEvent timelineEvent = timelineHistory.addEvent(perfState.getNodeId(), perfState.getName());

        //determine what task data has changed since the last learner state
        PerfNodeChange lastChange = perfNodeToLastChange.get(perfState.getNodeId());
        // was the last status active and the current status not active
        boolean changedToFinished = !taskActive && lastChange != null && lastChange.getStatus() != null;
        boolean changedAssessment = getAssesssmentChanged(lastChange, perfState);
        boolean needsArtificialInterval = changedAssessment && changedToFinished;
        PerfNodeChange currentChange = new PerfNodeChange(timestamp, perfState, taskActive || isPatched || needsArtificialInterval);

        EventInterval lastInterval = lastChange != null ? lastChange.getLatestInterval() : null;
        IntervalType lastStatus = lastChange != null ? lastChange.getStatus() : null;

        if(lastInterval != null && lastStatus != null) {
            
            //update the end date of the last interval generated for this performance node
            lastInterval.setEndDate(JsDate.create(currentChange.getDateMillis()));
        }
        
        final EventInterval nextInterval;
        
        String tooltip = null;
        Set<String> assessmentExplanation = null;
        
        if(currentChange.getStatus() == null) {
            
            //don't generate an interval for this state, since its performance node is inactive
            nextInterval = null;
            
        } else if(lastChange != null 
                && !changedAssessment
                && Objects.equals(lastChange.getStatus(), currentChange.getStatus())
                && lastInterval != null) {
            
            //continue using the previous interval for this state, since its timestamp has not changed
            nextInterval = lastInterval;

            /* Display evaluator information for this interval if it is not already shown */
            if (StringUtils.isBlank(lastInterval.getTooltip())
                    || !Objects.equals(lastInterval.getTooltip(), getTooltipPrefix(perfState))) {
                
                if (StringUtils.isNotBlank(perfState.getEvaluator())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Evaluated by ").append(perfState.getEvaluator());
                    if (isPatched) {
                        sb.append(" ").append(PATCH_TOOLTIP);
                    }

                    tooltip = sb.toString();
                }
                
                assessmentExplanation = perfState.getAssessmentExplanation();
            }

            
        } else {
            
            //create a new interval if this performance node's status has changed
            nextInterval = timelineEvent.addInterval(
                    currentChange.getDateMillis(), 
                    needsArtificialInterval ? (currentChange.getDateMillis() + ARTIFICIAL_DURATION_MS) : currentChange.getDateMillis(), 
                    SessionIntervalStatus.create(currentChange.getStatus()));
            nextInterval.setIsPatched(isPatched);

            if (StringUtils.isNotBlank(perfState.getEvaluator())) {
                StringBuilder sb = new StringBuilder();
                sb.append("Evaluated by ").append(perfState.getEvaluator());
                if (isPatched) {
                    sb.append(" ").append(PATCH_TOOLTIP);
                }

                tooltip = sb.toString();
            }
            
            assessmentExplanation = perfState.getAssessmentExplanation();
            
            nextInterval.setClickFunction(new IntervalClickHandler() {
                
                /**
                 * This can be used to determine whether the click logic should propagate up the DOM nesting.
                 * We have been going through different iterations of logic here to determine things like:
                 * - when should the playhead move
                 * - when should the assessment panel open
                 * - should just the task/concept assessment cell being clicked be shown.
                 * For reference and past logic used by is now gone, refer to #5062, #4804
                 */ 
                @Override
                public boolean shouldPropagate(Element context, EventInterval interval, int index, long timestamp) {                        
                    return true;
                }
                
                @Override
                public void onClick(Element context, EventInterval interval, int index, long timestamp) {
                        
                    /* If propagation was stopped, then the playhead will not be moved, so this click handler 
                     * should show the task/concept in the assessment panel instead of moving the playhead*/
                    if(perfNodeSelector != null) {
                        
                        switch(SummativeAssessmentProvider.getInstance().getDisplayMode()) {
                            case FORMATIVE:
                                
                                /* Show the formative assessment in the assessment panel to allow
                                 * the OC to edit the formative assessments*/
                                int perfNodeId = perfState.getNodeId();
                                
                                /* Build a path to the performance node that was clicked by following its
                                 * chain of parents */
                                PerformanceNodePath path = new PerformanceNodePath(perfNodeId);
                                
                                PerformanceNodePath currParent = path;
                                TimelineEvent currEvent = nextInterval.getEvent();
                                
                                while(currEvent.getParent() != null) {
                                    currEvent = currEvent.getParent();
                                    currParent.setParent(new PerformanceNodePath(currEvent.getId()));
                                    currParent = currParent.getParent();
                                }
                                
                                /* Notify listeners that a node was selected */
                                perfNodeSelector.selectPerformanceNode(path, timestamp);
                                break;
                                
                            case SUMMATIVE:
                                
                                /* Display the summative assessment dialog to allow the OC to edit
                                 * the summative assessments */
                                if (registeredSessionProvider.hasLogMetadata()) {
                                    final AbstractKnowledgeSession session = registeredSessionProvider
                                            .getLogMetadata().getSession();
                                    
                                    OverallAssessmentDialog.get().load(parentTaskState, session);
                                }
                                break;
                                
                            default:
                                logger.severe("A click on a performance state interval was unhandled because the current "
                                        + SummativeAssessmentProvider.getInstance().getDisplayMode() + "display mode does not handle it.");
                        }
                    }
                    
                    lastClickedInterval = interval;
                }
            });
        }
        
        if(nextInterval != null) {
            if(assessmentExplanation != null) {
                
                /* Display a tooltip that shows an explanation of the current assessment and its evaluator */
                nextInterval.setTooltip(
                        buildHtmlTooltip(getTooltipPrefix(perfState), tooltip, StringUtils.join("\n\n", assessmentExplanation)));
                
            } else {
                
                /* Display the bare minimum tooltip that just shows the basic formative/summative assessment */
                nextInterval.setTooltip(buildHtmlTooltip(getTooltipPrefix(perfState)));
            }
        }

        //track the last event interval that was updated, if applicable
        currentChange.setLatestInterval(nextInterval);
        
        String lastBookmark = lastChange != null ? lastChange.getLatestBookmark() : null;
        boolean hasMedia = perfState.getObserverMedia() != null;
        final String nextBookmark = hasMedia ? perfState.getObserverMedia() : perfState.getObserverComment();

        String lastObserver = lastChange != null ? lastChange.getLatestObserver() : null;
        String nextObserver = perfState.getEvaluator();

        final PopupPanel contextMenu = new PopupPanel();
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
        htmlBuilder.append(SafeHtmlUtils.bold("Name: ")).appendEscaped(perfState.getName());
        htmlBuilder.appendHtmlConstant("<br/>");
        htmlBuilder.append(SafeHtmlUtils.bold("Time: "))
                .appendEscaped(PLAYBACK_TIME_LABEL_FORMAT.format(new Date(timestamp)));
        if (StringUtils.isNotBlank(perfState.getEvaluator())) {
            htmlBuilder.appendHtmlConstant("<br/>");
            htmlBuilder.append(SafeHtmlUtils.bold("Evaluator: ")).appendEscaped(perfState.getEvaluator());
        }

        final TextAreaDialog editTextDialog = new TextAreaDialog("Edit Text", htmlBuilder.toSafeHtml(), "Update");

        UnorderedList choiceMenuList = new UnorderedList();
        choiceMenuList.setStyleName(Styles.DROPDOWN_MENU);
        choiceMenuList.getElement().getStyle().setProperty("display", "block");

        final AnchorListItem editEventItem = new AnchorListItem("Edit");
        editEventItem.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent mouseEvent) {
                contextMenu.hide();
                editTextDialog.center();
            }
        }, MouseDownEvent.getType());

        AnchorListItem deleteEventItem = new AnchorListItem("Delete");
        final AnchorListItem playMedia = new AnchorListItem(PLAY_AUDIO);
        final Timer timer = new Timer() {
            @Override
            public void run() {
                if (bookmarkAudioElement == null || bookmarkAudioElement.hasEnded()) {
                    cancel();
                    bookmarkAudioElement = null;
                    playMedia.setText(PLAY_AUDIO);
                }
            }
        };
        playMedia.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                contextMenu.hide();
                if (bookmarkAudioElement == null) {
                    bookmarkAudioElement = JsniUtility.playAudio(GWT.getHostPageBaseURL() + nextBookmark, Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().getVolume());
                    playMedia.setText(STOP_AUDIO);
                    timer.scheduleRepeating(1000);
                } else {
                    bookmarkAudioElement.pause();
                    bookmarkAudioElement = null;
                    playMedia.setText(PLAY_AUDIO);
                    timer.cancel();
                }
            }
        }, MouseDownEvent.getType());

        choiceMenuList.add(editEventItem);
        choiceMenuList.add(playMedia);
        choiceMenuList.add(new Divider());
        choiceMenuList.add(deleteEventItem);
        contextMenu.add(choiceMenuList);
        contextMenu.setAutoHideEnabled(true);
        contextMenu.getElement().getStyle().setProperty("border", "none");
        contextMenu.getElement().getStyle().setProperty("background", "none");

        /* Hide the context menu if the mouse leaves the menu for 1 second or
         * more */
        final Timer hideContextMenuTimer = new Timer() {
            @Override
            public void run() {
                if (contextMenu.isShowing()) {
                    contextMenu.hide();
                }
            }
        };

        contextMenu.addDomHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                /* Mouse is on the context menu; stop the hide menu timer */
                if (hideContextMenuTimer.isRunning()) {
                    hideContextMenuTimer.cancel();
                }
            }
        }, MouseMoveEvent.getType());
        contextMenu.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                /* If they don't return to the menu in 1 second, hide it */
                hideContextMenuTimer.schedule(1000);
            }
        }, MouseOutEvent.getType());

        final EventInterval newEventInterval;
        if (nextInterval != null && StringUtils.isNotBlank(perfState.getEvaluator())
                && (!StringUtils.equals(lastBookmark, nextBookmark)
                        || !StringUtils.equals(lastObserver, nextObserver))) {

            /* If a new interval or new bookmark was created or a new user made
             * an evaluation, then create an event interval to represent the
             * observer evaluation */
            newEventInterval = timelineEvent.addInterval(timestamp, timestamp,
                    SessionIntervalStatus.create(IntervalType.OBSERVER_EVALUATION));
            newEventInterval.setIsPatched(isPatched);

            if(hasMedia) {
                
                //if this bookmark has an associated media file, allow the user to play it by clicking the bookmark
                newEventInterval.setClickFunction(new IntervalClickHandler() {
                    
                    @Override
                    public void onClick(Element context, EventInterval d, int index, long timestamp) {
                        editEventItem.setVisible(false);
                        playMedia.setVisible(true);

                        /* Position the context menu at the location of the
                         * mouse */
                        showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), contextMenu);
                    }
                });
                
                newEventInterval.setTooltip("Click to play recording");
                
            } else {
                newEventInterval.setClickFunction(new IntervalClickHandler() {
                    
                    @Override
                    public void onClick(Element context, EventInterval interval, int index, long timestamp) {
                        editEventItem.setVisible(true);
                        playMedia.setVisible(false);

                        editTextDialog.setCaption("Edit Observer Evaluation");
                        String tooltip = interval.getTooltip();
                        if (StringUtils.isNotBlank(tooltip)) {
                            tooltip = tooltip.replace(PATCH_TOOLTIP, "").trim();
                        }
                        editTextDialog.setValue(tooltip);

                        /* Position the context menu at the location of the
                         * mouse */
                        showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), contextMenu);
                    }
                });

                //display this bookmark's text in a tooltip
                final String bookmarkTooltip = StringUtils.isNotBlank(nextBookmark)
                        ? nextBookmark + (isPatched ? " " + PATCH_TOOLTIP : "")
                        : isPatched ? PATCH_TOOLTIP : null;
                newEventInterval.setTooltip(buildHtmlTooltip(bookmarkTooltip));
            }

            currentChange.setLatestBookmark(nextBookmark);
            currentChange.setLatestObserver(nextObserver);

        } else if(StringUtils.isNotBlank(nextBookmark) && !Objects.equals(lastBookmark, nextBookmark)){

            //if a new bookmark is found in the performance state, create an event interval for it
            newEventInterval = timelineEvent.addInterval(timestamp, timestamp,
                    SessionIntervalStatus.create(IntervalType.BOOKMARK));
            newEventInterval.setIsPatched(isPatched);

            if(hasMedia) {

                //if this bookmark has an associated media file, allow the user to play it by clicking the bookmark
                newEventInterval.setClickFunction(new IntervalClickHandler() {
                    
                    @Override
                    public void onClick(Element context, EventInterval d, int index, long timestamp) {
                        editEventItem.setVisible(false);
                        playMedia.setVisible(true);

                        /* Position the context menu at the location of the
                         * mouse */
                        showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), contextMenu);
                    }
                });

                newEventInterval.setTooltip("Click to play recording");

            } else {
                newEventInterval.setClickFunction(new IntervalClickHandler() {
                    
                    @Override
                    public void onClick(Element context, EventInterval interval, int index, long timestamp) {
                        editEventItem.setVisible(true);
                        playMedia.setVisible(false);

                        editTextDialog.setCaption("Edit Bookmark");
                        String tooltip = interval.getTooltip();
                        if (StringUtils.isNotBlank(tooltip)) {
                            tooltip = tooltip.replace(PATCH_TOOLTIP, "").trim();
                        }
                        editTextDialog.setValue(tooltip);

                        /* Position the context menu at the location of the
                         * mouse */
                        showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), contextMenu);
                    }
                });

                //display this bookmark's text in a tooltip
                final String bookmarkTooltip = StringUtils.isNotBlank(nextBookmark)
                        ? nextBookmark + (isPatched ? " " + PATCH_TOOLTIP : "")
                        : isPatched ? PATCH_TOOLTIP : null;
                newEventInterval.setTooltip(buildHtmlTooltip(bookmarkTooltip));
            }

            currentChange.setLatestBookmark(nextBookmark);
            currentChange.setLatestObserver(lastObserver);

        } else {
            newEventInterval = null;
            //don't create a new event interval for a bookmark that has already been given one
            currentChange.setLatestBookmark(lastBookmark);
            currentChange.setLatestObserver(lastObserver);
        }

        /* Persist change to the event interval text */
        editTextDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String newText = event.getValue();
                final String oldText = perfState.getObserverComment();

                Set<String> assessmentExplanation = perfState.getAssessmentExplanation();
                if (CollectionUtils.isNotEmpty(assessmentExplanation)) {
                    assessmentExplanation.remove(oldText);
                }

                if (StringUtils.isBlank(newText)) {
                    perfState.setObserverComment(null);
                    perfState.setEvaluator(null);
                } else {
                    perfState.setObserverComment(newText);
                    if (assessmentExplanation == null) {
                        assessmentExplanation = new HashSet<String>();
                        perfState.setAssessmentExplanation(assessmentExplanation);
                    }
                    assessmentExplanation.add(newText);
                    perfState.setEvaluator(BsGameMasterPanel.getGameMasterUserName());
                }
                
                LoadingDialogProvider.getInstance().startLoading(LoadingType.TIMELINE_REFRESH, LoadingPriority.HIGH, 
                        "Applying Edits",
                        "Please wait while your edits are rendered onto the timeline...");

                dashboardService.editLogPatchForPerformanceStateAttribute(
                        BrowserSession.getInstance().getBrowserSessionKey(), BsGameMasterPanel.getGameMasterUserName(),
                        timestamp, perfState, new AsyncCallback<GenericRpcResponse<String>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                logger.warning("Failed to create patch file because " + caught.getMessage());
                                
                                TimelineProvider.getInstance().reloadTimeline();
                            }

                            @Override
                            public void onSuccess(GenericRpcResponse<String> result) {
                                
                                if(result.getContent() != null) {
                                    if (logger.isLoggable(Level.INFO)) {
                                        logger.info(
                                                "Successfully wrote patch file for performance state attribute update.");
                                    }
    
                                    /* Update log metadata patch file name */
                                    registeredSessionProvider.updateLogPatchFile(result.getContent());
    
                                    if (newEventInterval != null) {
                                        newEventInterval.setIsPatched(true);
                                        if (StringUtils.isBlank(perfState.getObserverComment())) {
                                            removeEventInterval(newEventInterval, timelineEvent);
                                        } else {
                                            final String tooltip = StringUtils.isNotBlank(perfState.getObserverComment())
                                                    ? perfState.getObserverComment() + " " + PATCH_TOOLTIP
                                                    : null;
                                            newEventInterval.setTooltip(buildHtmlTooltip(tooltip));
                                        }
                                    }
    
                                    if (nextInterval != null) {
                                        nextInterval.setIsPatched(true);
                                        final String evaluatedStr = StringUtils.isNotBlank(perfState.getEvaluator())
                                                ? "Evaluated by " + perfState.getEvaluator() + " " + PATCH_TOOLTIP
                                                : null;
                                        nextInterval.setTooltip(buildHtmlTooltip(evaluatedStr,
                                                StringUtils.join("\n\n", perfState.getAssessmentExplanation())));
                                    }
                                } 
                                
                                if(!result.getWasSuccessful()) {
                                    
                                    boolean isLrsError = result.getContent() != null;
                                    String title = isLrsError 
                                            ? "Unable to publish assessment to external system"
                                            : "Failed to save edited assessment";
                                    
                                    UiManager.getInstance().displayDetailedErrorDialog(
                                            title,
                                            result.getException().getReason(), 
                                            result.getException().getDetails(),
                                            result.getException().getErrorStackTrace(),
                                            null);
                                }
                                
                                TimelineProvider.getInstance().reloadTimeline();
                            }
                        });
            }
        });

        deleteEventItem.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent mouseEvent) {
                contextMenu.hide();

                String type = "comment";
                if (newEventInterval != null && newEventInterval.getStatus() instanceof SessionIntervalStatus) {
                    SessionIntervalStatus intervalStatus = (SessionIntervalStatus) newEventInterval.getStatus();
                    switch (intervalStatus.getType()) {
                    case BOOKMARK:
                        type = "Bookmark";
                        break;
                    case OBSERVER_EVALUATION:
                        type = "Observer Evaluation";
                        break;
                    default:
                        /* do nothing */
                    }
                }

                final String content = "Are you sure you want to delete this " + type + " from " + perfState.getName()
                        + "? This will remove it from the timeline.<br/><span style=\"font-size: 12px;\">Note: If this is an original "
                        + type + " then it can be recovered by removing all changes.</span>";

                UiManager.getInstance().displayConfirmDialog("Delete " + type + "?", content,
                        "Delete", "Cancel", new ConfirmationDialogCallback() {
                            @Override
                            public void onDecline() {
                                /* Do nothing */
                            }

                            @Override
                            public void onAccept() {
                                if (bookmarkAudioElement != null) {
                                    timer.cancel();
                                    bookmarkAudioElement.pause();
                                    bookmarkAudioElement = null;
                                }

                                dashboardService.removeLogPatchForAttribute(
                                        BrowserSession.getInstance().getBrowserSessionKey(),
                                        BsGameMasterPanel.getGameMasterUserName(), timestamp, perfState,
                                        new AsyncCallback<GenericRpcResponse<String>>() {
                                            @Override
                                            public void onFailure(Throwable caught) {
                                                logger.warning(
                                                        "Failed to create patch file because " + caught.getMessage());
                                            }

                                            @Override
                                            public void onSuccess(GenericRpcResponse<String> result) {
                                                if (logger.isLoggable(Level.INFO)) {
                                                    logger.info(
                                                            "Successfully wrote patch file for performance state attribute deletion.");
                                                }

                                                /* Update log metadata patch
                                                 * file name */
                                                registeredSessionProvider.updateLogPatchFile(result.getContent());

                                                if (newEventInterval != null) {
                                                    removeEventInterval(newEventInterval, timelineEvent);
                                                }

                                                reload(true);
                                            }
                                        });
                            }
                        });
            }
        }, MouseDownEvent.getType());

        //track the last changes that were gathered from this node's performance state
        perfNodeToLastChange.put(perfState.getNodeId(), currentChange);
        
        return timelineEvent;
    }

    /**
     * Remove the event interval from the timeline chart.
     * 
     * @param parentTimelineEvent the timeline event that contains the event
     *        interval to remove.
     * @param eventInterval the event interval to remove.
     * @return true if the event interval was found and removed successfully;
     *         false otherwise.
     */
    private boolean removeEventInterval(EventInterval eventInterval, TimelineEvent parentTimelineEvent) {
        Array<EventInterval> teIntervals = parentTimelineEvent.getIntervals();
        int foundIndex = -1;
        for (int i = 0; i < teIntervals.length(); i++) {
            EventInterval teInterval = teIntervals.get(i);
            if (teInterval.equals(eventInterval)) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex != -1) {
            teIntervals.splice(foundIndex, 1);
            timelineChart.removeEventInterval(eventInterval);
            return true;
        }

        return false;
    }

    /**
     * Shows the given popup panel at the given coordinates and repositions it
     * if necessary to keep it from going outside the client's viewable area.
     * 
     * @param x the preferred x position that the popup panel should be shown at
     * @param y the preferred y position that the popup panel should be shown at
     * @param popup the popup panel to show
     */
    private void showWithinViewport(int x, int y, PopupPanel popup) {

        popup.setPopupPosition(x, y);
        popup.show();

        // We need to try to keep the context menu from going off the page;
        // otherwise, scrollbars can appear next to the map.
        // We don't want this to happen because it interfere's with the map's
        // zooming controls.
        int top = popup.getPopupTop();
        int left = popup.getPopupLeft();

        int maxHeight = Window.getClientHeight() - top - 1;
        int maxWidth = Window.getClientWidth() - left;

        int verticalDisplacement = 0;
        int horizontalDisplacement = 0;

        if (popup.getOffsetHeight() + 5 + popup.getWidget().getOffsetHeight() > maxHeight) {

            verticalDisplacement = maxHeight - (popup.getOffsetHeight() + 5 + popup.getWidget().getOffsetHeight());
        }

        if (popup.getWidget().getOffsetWidth() > maxWidth) {

            horizontalDisplacement = maxWidth - (popup.getWidget().getOffsetWidth());
        }

        popup.setPopupPosition(left + horizontalDisplacement, top + verticalDisplacement);
    }

    @Override
    public void onPlayheadMoved(long playbackTime) {
        
        //synchronize the timeline chart with the server's playback time as the server passes updates
        timelineChart.seek(playbackTime);
        
//        if(capturedAudioElement != null) {
//            
//            double relPlayTime = getRelativePlaybackTime();
//            if(Math.abs(capturedAudioElement.getCurrentTime()*MILLIS_PER_SECOND - relPlayTime) > MILLIS_PER_SECOND){
//                
//                /*
//                 * If the captured audio's current playback time is more than 1s out of sync with the timeline's
//                 * playback, then re-sync the audio to the actual playback time.
//                 * 
//                 * We unfortunately can't just resync the audio with every call to this method, since the audio's
//                 * current time is only tracked in seconds and not every browser provides millisecond-level precision.
//                 */
//                capturedAudioElement.setCurrentTime(relPlayTime/MILLIS_PER_SECOND); 
//            }
//        }
    }

    @Override
    public void reloadTimeline() {
        reload(true);
    }

    @Override
    public void showTasks(Set<Integer> taskIds) {
        showOnlyTaskIds.clear();
        if (taskIds != null) {
            showOnlyTaskIds.addAll(taskIds);
        }
        refresh();
    }

    /**
     * Builds an HTML-safe string from the given series of lines to be displayed in a tooltip within the timeline
     * 
     * @param lines a series of strings to be placed in each line of the tooltip. If null or empty, null will be returned.
     * @return the built HTML-safe string. Can be null.
     */
    private String buildHtmlTooltip(String... lines) {
        
        if(lines == null || lines.length < 1) {
            return null;
        }
        
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        
        for(int i = 0; i < lines.length; i++) {
            
            String line = lines[i];
            
            if(StringUtils.isBlank(line)) {
                continue;
            }
            
            sb.appendEscapedLines(line);
            
            if(i < lines.length - 1) {
                sb.appendHtmlConstant("<br/><br/>");
            }
        }

        String htmlTooltip = sb.toSafeHtml().asString();
        return StringUtils.isNotBlank(htmlTooltip) ? htmlTooltip : null;
    }

    /**
     * A change to a performance node's state in the timeline
     *
     * @author nroberts
     */
    private class PerfNodeChange{

        /** The date of the change in milliseconds */
        private long dateMillis;

        /** The performance node's status at the moment of the change */
        private IntervalType status = null;

        /** The event interval used to represent one of this performance node's changes on the timeline */
        private EventInterval latestInterval = null;
        
        /** The latest of this performance node's bookmarks that was added to the timeline */
        private String latestBookmark = null;

        /** The latest observer user that evaluated this performance node in the timeline */
        private String latestObserver = null;
        
        /** The short term timestamp of the current status */
        private Long shortTermTimestamp = null;

        /**
         * Creates a new performance node change reflecting the given date and the given performance state
         *
         * @param dateMillis the date of the change
         * @param state the performance state of the performance node at said date. Cannot be null.
         * @param taskActive whether the performance node's parent task is active
         */
        public PerfNodeChange(long dateMillis, PerformanceStateAttribute state, boolean taskActive) {

            if(state == null) {
                throw new IllegalArgumentException("The performance state to create a performance node change from cannot be null");
            }

            this.dateMillis = dateMillis;
            if(taskActive) {
                
                AssessmentLevelEnum assessment = getDisplayedAssessment(state);

                if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(assessment)) {
                    status = IntervalType.BELOW_EXPECTATION; //performance node is active and below expectation

                } else if(AssessmentLevelEnum.UNKNOWN.equals(assessment)){
                    status = IntervalType.UNKNOWN;
                    
                } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(assessment)) {
                    status = IntervalType.AT_EXPECTATION; //performance node is active and at expectation
                } else if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(assessment)) {
                    status = IntervalType.ABOVE_EXPECTATION; //performance node is active and above expectation
                }else {
                    //default - assessment is probably null
                    status = IntervalType.AT_EXPECTATION; //performance node is active and not set
                }
                
                shortTermTimestamp = state.getShortTermTimestamp();
            }
        }

        /**
         * Gets the date of the change in milliseconds
         *
         * @return the change date
         */
        public long getDateMillis() {
            return dateMillis;
        }

        /**
         * Gets the performance node's status at the moment of this change
         *
         * @return the performance node's status. If null, then the performance 
         * node was inactive at the moment of this change.
         */
        public IntervalType getStatus() {
            return status;
        }

        /**
         * Gets the latest event interval used to represent one of this performance 
         * node's changes on the timeline
         *
         * @return the latest event interval. Can be null if this change hasn't been added to the timeline.
         */
        public EventInterval getLatestInterval() {
            return latestInterval;
        }

        /**
         * Sets the latest event interval used to represent one of this 
         * performance node's changes on the timeline
         *
         * @param event the latest event interval. Can be null.
         */
        public void setLatestInterval(EventInterval event) {
            this.latestInterval = event;
        }

        /**
         * Gets the latest of this performance node's bookmarks that was added to the timeline
         * 
         * @return the latest bookmark. Can be null.
         */
        public String getLatestBookmark() {
            return latestBookmark;
        }

        /**
         * Sets the latest of this performance node's bookmarks that was added to the timeline
         * 
         * @param latestBookmark the latest bookmark. Can be null.
         */
        public void setLatestBookmark(String latestBookmark) {
            this.latestBookmark = latestBookmark;
        }
        
        /**
         * Gets the latest observer user that evaluated this performance node in the timeline
         * 
         * @return the latest observer. Can be null.
         */
        public String getLatestObserver() {
            return latestObserver;
        }
        
        /**
         * Sets the latest observer user that evaluated this performance node in the timeline
         * 
         * @param latestObserver the latest observer. Can be null.
         */
        public void setLatestObserver(String latestObserver) {
            this.latestObserver = latestObserver;
        }

        /**
         * Gets the short term timestamp of the current status
         * 
         * @return the timestamp. Can be null.
         */
        public Long getShortTermTimestamp() {
            return shortTermTimestamp;
        }
    }

    @Override
    public void sessionAdded(AbstractKnowledgeSession knowledgeSession) {
        /* Do nothing */
    }

    @Override
    public void sessionEnded(AbstractKnowledgeSession knowledgeSession) {
        final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        if (RunState.PLAYBACK_ENDED.equals(activeSessionProvider.getRunState(dsId))) {
            
            if(timelineChart.isLoopEnabled()){
                
                // tell the server to start playing back from the beginning of the log
                dashboardService.setSessionPlaybackTime(
                        BrowserSession.getInstance().getBrowserSessionKey(),
                        (long) timelineHistory.getStartDate().getTime(),
                        new AsyncCallback<GenericRpcResponse<Void>>() {
                            
                            @Override
                            public void onSuccess(GenericRpcResponse<Void> result) {
                                
                                if (result.getWasSuccessful()) {

                                    if (registeredSessionProvider.hasLogMetadata()) {
                                        final AbstractKnowledgeSession session = registeredSessionProvider
                                                .getLogMetadata().getSession();
                                        if (RunState.PLAYBACK_ENDED
                                                .equals(activeSessionProvider.getRunState(dsId))) {
                                            activeSessionProvider.addActiveSession(session);
                                        }
                                    }
                                    
                                    sendStartPlayback();
                                    
                                    if(capturedAudioElement != null) {
                                        capturedAudioElement.setCurrentTime(0);
                                    }
                                }else{
                                    UiManager.getInstance().displayDetailedErrorDialog("Failed to loop playback",
                                            result.getException());
                                }
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                UiManager.getInstance().displayErrorDialog(
                                        "Failed to loop playback", 
                                        "An error occurred while setting the session playback time: " + caught.toString(), 
                                        null);
                            }
                            
                        });

            }else{
                timelineChart.pause();
                TimelineProvider.getInstance().pause();
                timelineChart.setPlayPauseButtonEnabled(false);
                
                if(capturedAudioElement != null) {
                    capturedAudioElement.pause();
                }
                if (playbackHandler != null) {
                    playbackHandler.onPause(0);
                }
            }
        } else {
            timelineHistory = null;
            timelineChart.clear();
            timelineChart.restart();
            timelineChart.pause();
            TimelineProvider.getInstance().reset();
            
            if(capturedAudioElement != null) {
                capturedAudioElement.pause();
                capturedAudioElement.setCurrentTime(0);
            }
            if (playbackHandler != null) {
                playbackHandler.onSeek(0);
                playbackHandler.onPause(0);
            }
        }
    }
    
    /**
     * Notify the session timeline that playback has been terminated
     */
    public void terminatePlayback(){
        timelineChart.terminatePlayback();
    }

    /**
     * Return whether or not the timeline is paused.
     * 
     * @return true if the timeline is paused; false otherwise.
     */
    public boolean isPaused() {
        return timelineChart.isPaused();
    }
    
    /**
     * Sets the UI elements that should act as the controls for the timeline
     * 
     * @param controls the control elements. Can be null, if the timeline should
     * not allow the learner to control its playback.
     */
    public void setTimelineControls(TimelineControls controls) {
        controls.setSummativeButtonImpl(new Command() {
            
            @Override
            public void execute() {
                
                /* Toggle between summative and formative mode */
                SummativeAssessmentProvider.getInstance().setDisplayMode(SummativeAssessmentProvider.getInstance().getDisplayMode() == AssessmentDisplayMode.SUMMATIVE 
                        ? AssessmentDisplayMode.FORMATIVE
                        : AssessmentDisplayMode.SUMMATIVE);
            }
        });
        
        timelineChart.setControls(controls);
    }
    
    /**
     * Sets the selector that will be used to select/deselect strategies to show their details
     * 
     * @param selector the strategy selector. Can be null, if strategies should not be selected/deselected.
     */
    public void setStrategySelector(StrategySelector selector) {
        this.strategySelector = selector;
    }
    
    /**
     * Sets the selector that will be used to allow the timeline to select/deselect bookmarks to show their details
     * 
     * @param selector the bookmark selector. Can be null, if nothing should happen when the timeline tries to
     * selector/deselect a bookmark.
     */
    public void setBookmarkSelector(BookmarkSelector selector) {
        this.bookmarkSelector = selector;
    }
    
    /**
     * Sets the selector that will be used to allow the timeline to select/deselect performance nodes to show their details
     * 
     * @param selector the performance node selector. Can be null, if nothing should happen when the timeline tries to
     * selector/deselect a performance node.
     */
    public void setPerfNodeSelector(PerformanceNodeSelector selector) {
        this.perfNodeSelector = selector;
    }

    @Override
    public void onVolumeChange(VolumeSetting setting) {
        
        if(Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().equals(setting)) {
            if(capturedAudioElement != null){
                
                //if the volume setting for past session audio is changed, update the appropriate audio element
                capturedAudioElement.setVolume(Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().getVolume());
                capturedAudioElement.setMuted(Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().isMuted());
            }
        }
    }
    
    /**
     * Traverses the hierarchy of score notes starting at the given node to populate
     * a mapping from each performance node to its assessment level score. This is
     * needed to quickly look up performance nodes' summative scores.
     * 
     * @param score the score node to start from. If null, no mappings will be added.
     */
    private void gatherSummativeAssessments(AbstractScoreNode score) {
        
        if(score instanceof GradedScoreNode) {
            perfNodeNameToSummativeAssessment.put(score.getName(), score.getAssessment());
        
            for(AbstractScoreNode childScore : ((GradedScoreNode) score).getChildren()) {
                gatherSummativeAssessments(childScore);
            }
        }
    }
    
    /**
     * Gets the assessment level that should be displayed for the given performance node state.
     * This will vary depending on which assessment display mode is currently in use..
     * 
     * @param perfNode the performance node state to get the assessment from. Cannot be null.
     * @return the assessment level to display. Can be null if no assessment can be determined.
     */
    private AssessmentLevelEnum getDisplayedAssessment(PerformanceStateAttribute perfNode) {
        switch(SummativeAssessmentProvider.getInstance().getDisplayMode()) {
        
            case FORMATIVE:
                
                /* Display the formative assessment from this state*/
                return perfNode.getShortTerm();
            
            case SUMMATIVE:
                
                /* Display the summative assessment score for this state's associated performance node*/
                AssessmentLevelEnum assessment = SummativeAssessmentProvider.getInstance().getSummativeAssessments().get(perfNode.getName());
                if(assessment == null) {
                    
                    /* This node does not yet have a summative score */
                    assessment = AssessmentLevelEnum.UNKNOWN;
                }
                
                return assessment;
                
            default:
                logger.severe("Unable to get assessment level for " + perfNode);
                return null;
        }
    }
    
    /**
     * Determines whether there should be a visible change in the assessment level shown between the two states.
     * If true is returned, a break should be shown between the two states to indicate they are separate
     * assessments. If false is returned, the two states should be shown as one continuous assessment.
     * 
     * @param lastChange the last change to compare the current state against. Cannot be null.
     * @param perfState the current state whose assessment needs to be compared. Cannot be null.
     * @return false if the assessment has changed or not.
     */
    private boolean getAssesssmentChanged(PerfNodeChange lastChange, PerformanceStateAttribute perfState) {
        
        switch(SummativeAssessmentProvider.getInstance().getDisplayMode()) {
            case FORMATIVE:
                
                /* For formative assessments, consider the assessments changed if their timestamps are different, even
                 * if the actual assessment level has not changed*/
                return lastChange != null && !Objects.equals(lastChange.getShortTermTimestamp(), perfState.getShortTermTimestamp());
                
            case SUMMATIVE:
                
                /* Summative assessments do not change over time, so they should span across updates continuously */
                return false;
                
            default:
                logger.severe("Could not determine if assessment changed based on current assessment mode");
                return false;
        }
    }
    
    /**
     * Gets whether the performance node with the given name is part of the course
     * concept hierarchy
     * 
     * @param conceptName the name of the node to check. Can be null, though null will always return false.
     * @return whether the node is part of the course hierarchy
     */
    private boolean isCourseConcept(String conceptName) {
        return courseConceptNodes.contains(conceptName);
    }
    
    /**
     * Parses the given task state to build a hierarchy of all the course concepts
     * found within it
     * 
     * @param task the task to parse for course concepts. If null, no course concepts
     * will be gathered.
     * @return @return all of the performance nodes underneath this task that were idenditied
     * as part of the course concept hierarchy
     */
    private Set<String> gatherCourseConcepts(TaskPerformanceState task) {
        
        Set<String> courseConcepts = new HashSet<>();
        
        boolean isCourseConcept = CourseConceptProvider.get().isCourseConcept(task.getState().getName());  
        
        for(ConceptPerformanceState child : task.getConcepts()) {
            courseConcepts.addAll(gatherCourseConcepts(child, isCourseConcept));
        }
        
        if(isCourseConcept || !courseConcepts.isEmpty()) {
            courseConcepts.add(task.getState().getName());
        }
        
        return courseConcepts;
    }
    
    /**
     * Parses the given concept state to build a hierarchy of all the course concepts
     * found within it
     * 
     * @param concept the concept to parse for course concepts. If null, no course concepts
     * will be gathered.
     * @param isParentCourseConcept whether the parent node is a course concept. If so,
     * this concept will be added to the course concept hierarchy.
     * @return all of the performance nodes underneath this task that were identified
     * as part of the course concept hierarchy
     */
    private Set<String> gatherCourseConcepts(ConceptPerformanceState concept, boolean isParentCourseConcept) {
        
        Set<String> courseConcepts = new HashSet<>();
        
        if(concept == null) {
            return courseConcepts;
        }
        
        boolean isCourseConcept = CourseConceptProvider.get().isCourseConcept(concept.getState().getName());

        if(concept instanceof IntermediateConceptPerformanceState) {
            for(ConceptPerformanceState child : ((IntermediateConceptPerformanceState) concept).getConcepts()) {
                courseConcepts.addAll(gatherCourseConcepts(child, isCourseConcept || isParentCourseConcept));
            }
        }
          
        if(isCourseConcept || isParentCourseConcept || !courseConcepts.isEmpty()) {
            courseConcepts.add(concept.getState().getName());
        }
        
        return courseConcepts;
    }
    
    /**
     * Gets the text that should always appear at the beginning of a tooltip representing
     * the given performance node state. This will vary depending on whether the user is
     * viewing summative or formative assessments
     * 
     * @param perfNode the state of the performance node that the tooltip represents. Cannot be null.
     * @return the prefix text for the tooltip. Will not be null.
     */
    private String getTooltipPrefix(PerformanceStateAttribute perfNode) {
        AssessmentLevelEnum assessment = getDisplayedAssessment(perfNode);
        if(assessment == null) {
            assessment = AssessmentLevelEnum.UNKNOWN;
        }
        
        return SummativeAssessmentProvider.getInstance().getDisplayMode().getDisplayName() + 
                ": " + assessment.getDisplayName();
    }

    @Override
    public void onSummativeAssessmentsChanged(Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDisplayModeChanged(AssessmentDisplayMode displayMode) {
        refresh();
    }

    /**
     * Sends the server a request to stop the current playback
     * 
     * @param playbackHandler a handler that will be informed if the stop was successful. Can be null
     * @param dateMillis the timeline date at which to pause
     */
    private void sendPausePlayback(final PlaybackHandler playbackHandler, final long dateMillis) {
        if(capturedAudioElement != null) {
            capturedAudioElement.pause();
        }
        
        //notify the server to stop the playback
        dashboardService.stopSessionPlayback(
                BrowserSession.getInstance().getBrowserSessionKey(),
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {
                        
                        if(!result.getWasSuccessful()) {
                            UiManager.getInstance().displayDetailedErrorDialog(
                                    "Failed to Stop Playback", 
                                    result.getException());
                        } else if (playbackHandler != null) {
                            playbackHandler.onPause(dateMillis);
                            TimelineProvider.getInstance().pause();
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        
                        UiManager.getInstance().displayErrorDialog(
                                "Failed to Stop Playback", 
                                "An error occurred while starting the session playback: " + caught.toString(), 
                                null);
                    }
                });
    }
}
