/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.aar.VideoMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.event.PointerDownEvent;
import mil.arl.gift.common.gwt.client.event.PointerDownHandler;
import mil.arl.gift.common.gwt.client.event.PointerUpEvent;
import mil.arl.gift.common.gwt.client.event.PointerUpHandler;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.GIFTSplitLayoutPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.KnowledgeSessionListWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.KnowledgeSessionListWidget.SessionSelectedCallback;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ToggleButton;
import mil.arl.gift.common.gwt.shared.GetActiveKnowledgeSessionsResponse;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.ta.util.ExternalMonitorConfig;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;
import mil.arl.gift.tools.dashboard.client.Dashboard.Settings;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogType;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AddVideoModalDialog.NewVideoCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AddVideoModalDialog.VideoChangedCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.BookmarkCreatorPanel.GestureCommands;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.DisplayPicker.DisplayOptions;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.SessionDataPanel.KnowledgeSessionSelector;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.SessionControlPanel;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.SessionTimeline;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.TimelineChart.PlaybackHandler;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter.SessionsFilterPanel;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter.StatePane;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.Mode;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider.TimelineChangeHandler;
import mil.arl.gift.tools.dashboard.shared.messages.GatewayConnection;

/**
 * The Game Master Panel contains the functionality for a game master to join,
 * manipulate and monitor training sessions.
 *
 * @author nblomberg
 *
 */
public class BsGameMasterPanel extends AbstractBsWidget implements ActiveSessionChangeHandler, TimelineChangeHandler {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(BsGameMasterPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static BsGameMasterPanelUiBinder uiBinder = GWT.create(BsGameMasterPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface BsGameMasterPanelUiBinder extends UiBinder<Widget, BsGameMasterPanel> {
    }

    /**
     * Create a remote service proxy to talk to the server-side dashboard
     * service.
     */
    private static final DashboardServiceAsync dashboardService = UiManager.getInstance().getDashboardService();

    /** The active session provider */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /** The registered session provider */
    private final RegisteredSessionProvider registeredSessionProvider = RegisteredSessionProvider.getInstance();

    /** The component loading provider instance */
    private final LoadingDialogProvider componentLoadingProvider = LoadingDialogProvider.getInstance();

    /** The dashboard settings to control the sound permissions */
    private final Settings dashboardSettings = Dashboard.getInstance().getSettings();

    /** The style name for selected buttons */
    private static final String selectedButtonColorStyle = "giftSelectedButtonColor";

    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * The CSS applied to the main splitter.
         *
         * @return the CSS style name
         */
        String mainSplitter();

        /**
         * The CSS to apply to horizontal draggers to hide them.
         *
         * @return the CSS style name
         */
        String hideHorizontalDraggers();

        /**
         * The CSS for the panel select dropdown widgets.
         * 
         * @return the CSS style name
         */
        String panelSelect();
    }

    /** The style from the ui.xml */
    @UiField
    protected Style style;

    /**
     * The deck panel that allows for switching between active and past session
     * lists and the selected panel
     */
    @UiField
    protected DeckPanel mainDeck;

    /** The split layout panel for the session filter and the session lists */
    @UiField
    protected GIFTSplitLayoutPanel sessionListSplit;

    /**
     * The deck panel that allows for switching between active and past session
     * lists
     */
    @UiField
    protected DeckPanel sessionListDeck;

    /** 
     * The blocker panel that pops up when the user enters the bookmark gesture mode
     */
    @UiField
    protected static BlockerPanel  bookmarkBlockerPanel;
    
    /** The manager used to detect and handle touch gestures when they are enabled*/
    private GestureManager gestureManager;

    /** The widget containing the list of active knowledge sessions */
    @UiField(provided = true)
    protected KnowledgeSessionListWidget activeSessionListWidget = new KnowledgeSessionListWidget(
            new SessionSelectedCallback() {
                @Override
                protected void sessionSelected(final AbstractKnowledgeSession knowledgeSession) {
                    registerActiveSession(knowledgeSession);
                }
                
            }
        ) {

        @Override
        public void addSession(AbstractKnowledgeSession session) {

            // only display sessions in the active session list if they are not
            // past
            if (!session.inPastSessionMode()) {
                super.addSession(session);
            }
        }
    };

    /** The widget containing the list of AAR sessions */
    @UiField(provided = true)
    protected KnowledgeSessionListWidget aarListWidget = new KnowledgeSessionListWidget("Select to replay this session",
            new SessionSelectedCallback() {
                @Override
                protected void sessionSelected(final AbstractKnowledgeSession knowledgeSession) {
                    registerPastSession(knowledgeSession);
                }
            });

    /** show the server side progress of fetching the list of sessions to show the user */
    @UiField
    protected ProgressBarListEntry progressBar;

    /** The filter for the session lists */
    @UiField(provided = true)
    protected SessionsFilterPanel sessionListFilter = new SessionsFilterPanel(this);

    /** The panel to display when an active or past session is selected */
    @UiField
    protected FlowPanel selectedSessionPanel;

    /** The container for the content on the left side */
    @UiField
    protected SimplePanel leftContentPanel;

    /** The container for the content on the right side */
    @UiField
    protected SimplePanel rightContentPanel;

    /** The panel containing the contents of this page */
    @UiField
    protected FlowPanel mainPanel;    
    
    /** the callback for when a video is changed (e.g. name changed, deleted) - update video picker */
    private final VideoChangedCallback videoChangedCallback = new VideoChangedCallback() {
        
        @Override
        public void onUpdated(VideoMetadata videoMetadata) {
          logger.info("Successfully updated the video metadata on the server for "+videoMetadata);
          
          videoPanel.updateVideo(videoMetadata);
          final VideoManager videoManager = videoPanel.getVideoManager(videoMetadata);
          if(videoManager == null){
              logger.warning("Unable to find video manager for "+videoMetadata);
          }
          leftDisplayPicker.updateVideo(videoManager);
          rightDisplayPicker.updateVideo(videoManager);
          
          Notify.notify("Updated the <b>"+videoMetadata.getTitle()+"</b> video properties");
            
        }
        
        @Override
        public void onUpdateFailed(String message, VideoMetadata videoMetadata) {
            UiManager.getInstance().displayErrorDialog("Video Property Update Failed", 
                  "There was a problem on the server when updating the properties for <b>"+videoMetadata.getTitle()+"</b>.\n"+message, null);
        }
        
        @Override
        public void onDeletedFailed(String message, VideoMetadata videoMetadata) {
            UiManager.getInstance().displayErrorDialog("Video Delete Failed", 
                  "There was a problem on the server when deleting <b>"+videoMetadata.getTitle()+"</b>.\n"+message, null);
        }
        
        @Override
        public void onDeleted(VideoMetadata videoMetadata) {

            if (!registeredSessionProvider.hasLogMetadata()) {
                return;
            }
            
            logger.info("Successfully deleted the video/metadata on the server for "+videoMetadata);
            
            Notify.notify("Deleted the <b>"+videoMetadata.getTitle()+"</b> video");

            // Opposite of newVideoCallback.onUploaded method logic
            boolean removedSessionVideoMetadata = registeredSessionProvider.getLogMetadata().getVideoFiles().remove(videoMetadata);
            final VideoManager videoManager = videoPanel.getVideoManager(videoMetadata);
            if(videoManager == null){
                logger.warning("Unable to find video manager for "+videoMetadata);
            }
            leftDisplayPicker.removeVideo(videoManager);
            rightDisplayPicker.removeVideo(videoManager);
            
            // must be done after retrieving and using the video manager above
            boolean removedFromVideoPanel = videoPanel.removeVideo(videoMetadata);

            logger.info("Results of removing video from game master-> Session = "+removedSessionVideoMetadata+", videoPanel = "+removedFromVideoPanel);
        }
    };

    /** The callback for when a new video is uploaded - update video picker */
    private final NewVideoCallback newVideoCallback = new NewVideoCallback() {
        @Override
        public void onUploaded(final VideoMetadata videoMetadata) {
            if (!registeredSessionProvider.hasLogMetadata()) {
                return;
            }

            registeredSessionProvider.getLogMetadata().getVideoFiles().add(videoMetadata);
            videoPanel.addNewVideo(videoMetadata, new Command() {
                @Override
                public void execute() {
                    logger.info("Notified that video has been added to the video panel.  Updating game master UI for new video.\n"+videoMetadata);
                    final VideoManager newManager = videoPanel.getVideoManager(videoMetadata);
                    
                    /* Seek new video to the current time */
                    newManager.seek(TimelineProvider.getInstance().getPlaybackTime());

                    leftDisplayPicker.addNewVideo(newManager);
                    rightDisplayPicker.addNewVideo(newManager);
                }
            }, videoChangedCallback);
        }

        @Override
        public void onUploadFailed(String message) {
            logger.warning("Video failed to upload because: '" + message + "'.");
        }
    };

    /** The panel containing the {@link #leftDisplayPicker} */
    @UiField
    protected FlowPanel leftDisplayPickerPanel;

    /** The display picker for the {@link #leftContentPanel} */
    @UiField(provided = true)
    protected DisplayPicker leftDisplayPicker = new DisplayPicker(new ChangeCallback<Set<VideoManager>>() {
        @Override
        public void onChange(Set<VideoManager> newValue, Set<VideoManager> oldValue) {
            rightDisplayPicker.updateSelectedVideos(newValue);
        }
    }, newVideoCallback);

    /** The panel containing the {@link #rightDisplayPicker} */
    @UiField
    protected FlowPanel rightDisplayPickerPanel;

    /** The display picker for the {@link #rightContentPanel} */
    @UiField(provided = true)
    protected DisplayPicker rightDisplayPicker = new DisplayPicker(new ChangeCallback<Set<VideoManager>>() {
        @Override
        public void onChange(Set<VideoManager> newValue, Set<VideoManager> oldValue) {
            leftDisplayPicker.updateSelectedVideos(newValue);
        }
    }, newVideoCallback);

    /** The button used to show the timeline for a selected past session */
    @UiField
    protected Button timelineButton;
    
    /** button used to show or hide the mission panel */
    @UiField
    protected Button missionButton;

    /** The settings drop down menu */
    @UiField
    protected DropDownMenu settingsMenu;

    /** Option to play/mute all sounds */
    @UiField
    protected InlineHTML muteAllAlerts;

    /** Option to show/hide poor assessment visuals (e.g. red highlight task/concept panel) */
    @UiField
    protected ToggleButton showPoorAssessmentVisual;

    /** Option to show/hide observer controller assessment visuals (e.g. yellow highlight concept panel) */
    @UiField
    protected ToggleButton showOCAssessmentVisual;
    
    /** Option to sort so that controller assessment concepts are shown first */
    @UiField
    protected ToggleButton prioritizeOCAssessment;

    /** Option to show only the observer controller concepts */
    @UiField
    protected ToggleButton showOcOnly;

    /** Option to show all tasks in the assessment panel */
    @UiField
    protected ToggleButton showAllTasks;
    
    /** Option to auto advance all sessions in the assessment panel */
    @UiField
    protected ToggleButton autoAdvanceSessions;

    /** show showing/hiding team org names (i.e. white labels to the lower left of map entity/team icons) */
    @UiField
    protected ToggleButton showTeamOrgName;
    
    /** button used to toggle showing the mini map or not */
    @UiField
    protected ToggleButton showMiniMap;

    /** show showing/hiding scenario support task/concepts */
    @UiField
    protected ToggleButton showScenarioSupport;
    
    /** show showing/hiding concepts with automated assessments that have good assessment at the moment */
    @UiField
    protected ToggleButton hideGoodAutoAssessments;

    /**
     * Apply the assessment changes (patches) where the playhead is. If this is
     * false, the entire assessment will be updated regardless of playhead
     * position.
     */
    @UiField
    protected ToggleButton applyChangesAtPlayhead;

    /** the panel containing options to share playback with external applications */
    @UiField
    protected Widget sharePlaybackPanel;

    /** Option to share playback with ARES */
    @UiField
    protected ToggleButton monitorWithARES;
    
    /** Option to share playback with DIS */
    @UiField
    protected ToggleButton monitorWithDIS;

    /** Menu string to display when all alerts (audio and visual) are not muted */
    private static final String MUTE_ALL_ALERTS = "Suppress All Alerts";

    /** Menu string to display when all alerts (audio and visual) are muted */
    private static final String PLAY_ALL_ALERTS = "Play All Alerts";

    /** The panel containing the data for the selected session */
    @UiField
    protected SimplePanel sessionDataHeaderPanel;

    /** The container panel for the {@link #statePane} */
    @UiField
    protected HTMLPanel filterPanelContainer;

    /** A panel containing controls to filter based on session criteria */
    @UiField
    protected StatePane statePane;
    
    /** A splitter used to divide the timeline from the main splitter an adjust their size */
    @UiField
    protected GIFTSplitLayoutPanel outerSplitter;

    /** A splitter used to divide the panels and adjust their size */
    @UiField
    protected GIFTSplitLayoutPanel mainSplitter;

    /** A button that can be used to disable recording via touch gestures */
    @UiField
    protected Widget exitGestureButton;
    
    /** An icon used to indicate whether recording via touch gestures is currently in progress*/
    @UiField
    protected Icon gestureMicIcon;
    
    /** The sub text shown by the touch gesture UI*/
    @UiField
    protected HTML gestureSubText;
    
    /** A confirmation dialog to use for bookmark gesture operations when game master is in full screen mode. 
     * The normal error dialog is attached to the body element and will be hidden when game master is full
     * screen, so this dialog is used as an alternative. */
    @UiField
    protected BsDialogConfirmWidget gestureConfirmDialog;
    
    /** An error dialog to use for bookmark gesture operations when game master is in full screen mode. 
     * The normal error dialog is attached to the body element and will be hidden when game master is full
     * screen, so this dialog is used as an alternative. */
    @UiField
    protected BsDialogWidget gestureErrorDialog;

    /** The panel containing the aar videos */
    private final SessionVideoPanel videoPanel = new SessionVideoPanel();

    /** The panel containing the sessions map */
    private final SessionsMapPanel mapPanel = new SessionsMapPanel();

    /** The handler for controlling the video player */
    private final PlaybackHandler videoPlaybackHandler = new PlaybackHandler() {
        @Override
        public void onSeek(long dateMillis) {
            videoPanel.seek(dateMillis);
        }

        @Override
        public void onPlay(long dateMillis) {
            /* Nothing to do. Playing is handled by the
             * timelineProvider.onPlayheadMoved. */
        }

        @Override
        public void onPause(long dateMillis) {
            videoPanel.pauseVideos();
        }
    };

    /** The panel container for the session controls (i.e. volume, timeline controls, etc.) */
    @UiField(provided = true)
    protected SessionControlPanel controlPanel = new SessionControlPanel();
    
    /** The panel container for the AAR timeline */
    @UiField(provided = true)
    protected SessionTimeline timelinePanel = new SessionTimeline(videoPlaybackHandler);
    
    /** A slider that controls the size of military symbols */
    @UiField(provided = true)
    protected Slider symbolSizeSlider = new Slider(0.5, 2d, Dashboard.getInstance().getSettings().getMilSymbolScale());

    /** The session data panel for the selected knowledge session */
    private SessionDataPanel sessionDataPanel;
    
    /** The strategies panel for the selected knowledge session */
    private StrategiesPanel strategiesPanel;
    
    /** The bookmark panel for the selected knowledge session */
    private GlobalBookmarkPanel bookmarkPanel;
    
    /** The map of knowledge sessions to their log metadata */
    private final Map<AbstractKnowledgeSession, LogMetadata> sessionToLogMetadataMap = new LinkedHashMap<>();

    /** The configuration containing settings that are shared with external monitor applications */
    private ExternalMonitorConfig externalMonitorConfig = new ExternalMonitorConfig();
    
    /** whether the game master is being used in a touch screen */
    private boolean touchCapable = false;

    /**
     * Constructor
     * 
     * @param showActiveSessions true to show the active sessions; false to show
     *        the past sessions.
     */
    public BsGameMasterPanel(boolean showActiveSessions) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        /* Remove the 'none' option because the left is the 'main' panel that
         * should always be displaying something */
        leftDisplayPicker.removeDisplayOption(DisplayOptions.NONE);
        
        initWidget(uiBinder.createAndBindUi(this));

        gestureManager = new GestureManager(bookmarkBlockerPanel);

        UiManager.getInstance().fillToBottomOfViewport(mainDeck);

        /* Hide timeline and right content panel by default */
        hideTimelinePanel();
        mainSplitter.setWidgetHidden(rightContentPanel, true);
        setMainSplitterDragBarVisibility(false);

        leftDisplayPicker.addValueChangeHandler(new ValueChangeHandler<DisplayOptions>() {
            @Override
            public void onValueChange(ValueChangeEvent<DisplayOptions> event) {
                selectedOption(event.getValue(), leftContentPanel);
                rightDisplayPicker.setDisabledOptions(event.getValue());
            }
        });

        rightDisplayPicker.addValueChangeHandler(new ValueChangeHandler<DisplayOptions>() {
            @Override
            public void onValueChange(ValueChangeEvent<DisplayOptions> event) {
                selectedOption(event.getValue(), rightContentPanel);
                leftDisplayPicker.setDisabledOptions(event.getValue());
            }
        });

        timelineButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* Toggle timeline visibility */
                if (outerSplitter.isHidden(timelinePanel) && registeredSessionProvider.hasRegisteredSession()) {
                    showTimelinePanel();
                } else {
                    hideTimelinePanel();
                }
            }
        });
        
        missionButton.addStyleName(selectedButtonColorStyle);
        missionButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				/* Toggle mission panel visibility */                
                if(filterPanelContainer.isVisible()) {
                	// to not visible
                	filterPanelContainer.setVisible(false);
                	missionButton.removeStyleName(selectedButtonColorStyle);
                }else {
                	// to visible
                	filterPanelContainer.setVisible(true);
                	missionButton.addStyleName(selectedButtonColorStyle);
                }
			}
		});

        mapPanel.setFullScreenTarget(getElement());
        mapPanel.addAlertButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* Zoom map to fit all the alert entities that were just
                 * filtered on */
                mapPanel.resetZoom();
            }
        });
        
        timelinePanel.setStrategySelector(new StrategySelector() {
            
            @Override
            public void selectStrategy(final String name, final Long timestamp) {
                
                /* Check if the panel is already on the left or right side */
                if (!strategiesPanel.isAttached()) {
                    rightDisplayPicker.setValue(DisplayOptions.STRATEGIES, true);
                  
                }
                
                /* Defer opening the strategy history until after the panel is attached,
                 * in case we need to scroll to a strategy (which won't work properly if 
                 * the panel is still being attached and opened) */
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        strategiesPanel.openStrategyHistoryPanel(name, timestamp);
                    }
                
                });
                
            }
        });
        
        timelinePanel.setBookmarkSelector(new BookmarkSelector() {
            
            @Override
            public void selectBookmark(final long timestamp) {
                
                /* Open the global bookmarks in the right side panel */
                if (!bookmarkPanel.isAttached()) {
                    rightDisplayPicker.setValue(DisplayOptions.BOOKMARK, true);
                }
                
                /* Defer opening the bookmark history until after the panel is attached,
                 * in case we need to scroll to a bookmark (which won't work properly if 
                 * the panel is still being attached and opened) */
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        bookmarkPanel.openGlobalBookmarkPanel(timestamp);
                    }
                });
            }
        });
        
        timelinePanel.setPerfNodeSelector(new PerformanceNodeSelector() {
            
            @Override
            public void selectPerformanceNode(final PerformanceNodePath nodePath, long timestamp) {
                
                /* Open the session data in the right side panel */
                if (!sessionDataPanel.isAttached()) {
                    rightDisplayPicker.setValue(DisplayOptions.ASSESSMENTS, true);
                }
                
                final ScheduledCommand command = new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        sessionDataPanel.scrollNodeIntoView(nodePath);
                    }
                };
                
                /* Scroll the selected node into view */
                if(!sessionDataPanel.isAttached()) {
                    
                    /* Need to wait for the session data panel to be attached first */
                    final HandlerRegistration[] registration = new HandlerRegistration[1];
                    registration[0] = sessionDataPanel.addAttachHandler(new AttachEvent.Handler() {
                        
                        @Override
                        public void onAttachOrDetach(AttachEvent event) {
                            
                            if(!event.isAttached()) {
                                return;
                            }
                            
                            /* Defer opening the bookmark history until after the panel is attached,
                             * in case we need to scroll to a bookmark (which won't work properly if 
                             * the panel is still being attached and opened) */
                            Scheduler.get().scheduleDeferred(command);
                            
                            registration[0].removeHandler();
                        }
                    });
                    
                } else {
                    Scheduler.get().scheduleDeferred(command);
                }
            }
        });
        
        exitGestureButton.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                leaveGestureRecordingMode();
            }
        }, ClickEvent.getType());
        
        statePane.setPlayBackSessionCallback(new StatePane.PlayBackSessionCallback() {
            
            @Override
            public void onPlayBackSession(AbstractKnowledgeSession session) {
                
                //if the user chooses to start playing back their last session, auto-load it for them
                autoLoadPastSession(session);
            }
        });

        /* Subscribe to the data providers */
        subscribe();

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                /* Redraw video panel if not going to fullscreen mode */
                if (JsniUtility.getFullscreenElement() == null) {
                    videoPanel.redraw();
                }
            }
        });

        /* Register the browser session with nothing being monitored. This will
         * 'subscribe' to only session life cycle states (e.g. start and end) */
        registerKnowledgeSession(null, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.warning("Failed to register the browser session for life cycle states.");
            }

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    logger.warning("Failed to register the browser session for life cycle states.");
                }
            }
        });

        showKnowledgeSessions(showActiveSessions);
        
        /* Initialize the widgets in the settings section */
        initSettingsWidgets();

        //detect if the server is already connected to an external monitor app and modify the UI as needed
        dashboardService.getGatewayConnection(
                UiManager.getInstance().getSessionId(), new AsyncCallback<GenericRpcResponse<GatewayConnection>>() {

                    @Override
                    public void onSuccess(GenericRpcResponse<GatewayConnection> result) {

                        GatewayConnection gatewayConnection = result.getContent();
                        
                        if(!result.getWasSuccessful() || gatewayConnection == null) {
                            // reset all
                            monitorWithARES.setValue(false);
                            monitorWithDIS.setValue(false);
                            return;
                        }

                        //show monitor applications as enabled/disabled depending on whether the server is connected to them
                        monitorWithARES.setValue(gatewayConnection.getTaTypes().contains(TrainingApplicationEnum.ARES));
                        monitorWithDIS.setValue(gatewayConnection.shouldUseDIS());
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        monitorWithARES.setValue(false);
                        monitorWithDIS.setValue(false);
                    }
                });
        
        if(DeploymentModeEnum.SERVER.equals(UiManager.getInstance().getDeploymentMode())) {
            
            //hide the option to share playback with external applications when in server mode
            sharePlaybackPanel.setVisible(false);
        }
        
        //link the control UI elements in this panel with the appropriate operations in the timeline
        timelinePanel.setTimelineControls(controlPanel.getTimelineControls());
        
        sessionListDeck.addDomHandler(new TouchEndHandler() {
            
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                touchCapable = true;
            }
        }, TouchEndEvent.getType());
        
        /* Change the size of the military symbols as their slider is moved */
        symbolSizeSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                Dashboard.getInstance().getSettings().setMilSymbolScale(event.getValue());
            }
        });
    }
    
    /**
     * Registers the session that is active in Game Master.
     * 
     * @param knowledgeSession the knowledge session being registered.
     */
    protected void registerActiveSession(final AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            return;
        }

        PermissionsProvider.getInstance().permissionUpdate(Mode.ACTIVE_SESSION);

        /* Reset the auto mode on the server back to the default
         * game master is not in charge of approving strategies when
         * in RTA lesson level, the external system connected via
         * the GW module is. */
        StrategiesPanel.sendAutoMode(Dashboard.getInstance().getSettings().isAutoApplyStrategies()
                && Dashboard.getInstance().getServerProperties().getLessonLevel() != LessonLevelEnum.RTA);

        sessionDataPanel = new SessionDataPanel(knowledgeSession);
        sessionDataHeaderPanel.setWidget(sessionDataPanel.getHeaderPanel());
        sessionDataHeaderPanel.setVisible(true);
        sessionDataPanel.getHeaderPanel().getBookmarkCreator().showEnterGestureModeHeaderButton(touchCapable);
        sessionDataPanel.getHeaderPanel().getBookmarkCreator().setRecordGestureCommand(new GestureCommands() {
            
            @Override
            public void record() {
                startRecordGesture();
            }

            @Override
            public void displayDetailedError(String title,
                    DetailedExceptionSerializedWrapper exceptionWrapper) {
                displayGestureDetailedErrorDialog(title, exceptionWrapper);
            }

            @Override
            public void displayError(String title, String description) {
                displayGestureErrorDialog(title, description);
            }
        });
        
        // KnowledgeSessionSelector is needed to handle when a user clicks on the notification to view a new active session.
        sessionDataPanel.setSessionSelector(new KnowledgeSessionSelector() {

            @Override
            public void onSelectSession(AbstractKnowledgeSession session) {
                
                sessionDataPanel.unsubscribe();
                
                registerActiveSession(session);
                
                selectedOption(rightDisplayPicker.getValue(), rightContentPanel);
                rightDisplayPicker.setDisabledOptions(rightDisplayPicker.getValue());
                
                selectedOption(leftDisplayPicker.getValue(), leftContentPanel);
                leftDisplayPicker.setDisabledOptions(rightDisplayPicker.getValue());
                
            }
            
        });
        
        strategiesPanel = new StrategiesPanel(knowledgeSession);
        bookmarkPanel = new GlobalBookmarkPanel(knowledgeSession);

        registerKnowledgeSession(knowledgeSession, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    return;
                }

                leftDisplayPickerPanel.setVisible(true);
                rightDisplayPickerPanel.setVisible(true);

                // 3/2/21 - current requirement is to default to showing just the map on left, nothing on right
                loadPreferredDisplayOptions(DisplayOptions.MAP, null);

                mainDeck.showWidget(mainDeck.getWidgetIndex(selectedSessionPanel));
            }
        });
    }

    /**
     * Attempts to load and display the user's preferred display options and, if none are found, defaults
     * to the display options given.<br/><br/>
     * 
     * Whenever a user selects a display option from either picker, their selection is saved to their browser's 
     * local storage so that the same display option can be automatically selected the next time they enter 
     * Game Master, thereby letting them look at the same views in between sessions without re-selecting them.
     * The default values are only used when a user hasn't made any selections yet, allowing them to act as a fallback.
     * 
     * @param defaultLeft the default display option to use for the left panel if no saved selection is found. If null,
     * the left panel will not be changed.
     * @param defaultRight the default display option to use for the right panel if no saved selection is found. If null,
     * the left panel will not be changed.
     */
    protected void loadPreferredDisplayOptions(DisplayOptions defaultLeft, DisplayOptions defaultRight) {
        
        DisplayOptions leftOption = defaultLeft;
        
        String leftPanelName = Dashboard.getInstance().getSettings().getLeftDisplayPicker();
        if(leftPanelName != null) {
            
            /* Show the user's previous selected panel if there is one*/
            DisplayOptions cachedOption = DisplayOptions.valueOf(leftPanelName);
            if(leftDisplayPicker.hasDisplayOption(cachedOption)) {
                leftOption = cachedOption; 
            }
        }
        
        if(leftOption != null) {
            leftDisplayPicker.setValue(leftOption, true);
        }
        
        DisplayOptions rightOption = defaultRight;
        
        String rightPanelName = Dashboard.getInstance().getSettings().getRightDisplayPicker();
        if(rightPanelName != null) {
            
            /* Show the user's previous selected panel if there is one*/
            DisplayOptions cachedOption = DisplayOptions.valueOf(rightPanelName);
            if(rightDisplayPicker.hasDisplayOption(cachedOption)) {
                rightOption = cachedOption; 
            }
        }
        
        if(rightOption != null) {
            rightDisplayPicker.setValue(rightOption, true);
        }
    }

    private void registerPastSession(final AbstractKnowledgeSession knowledgeSession) {
        
        if (knowledgeSession == null) {
            return;
        }

        PermissionsProvider.getInstance().permissionUpdate(Mode.PAST_SESSION_PLAYBACK);
        
        // #5098 - create these panels before registering the knowledge session to allow the server
        // to send messages to the client that will populate these panels immediately
        sessionDataPanel = new SessionDataPanel(knowledgeSession);
        strategiesPanel = new StrategiesPanel(knowledgeSession);
        bookmarkPanel = new GlobalBookmarkPanel(knowledgeSession);

        registerKnowledgeSession(knowledgeSession, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    return;
                }
                
                if(knowledgeSession.inPastSessionMode()) {
                	autoAdvanceSessions.setVisible(false);
                }
                videoPanel.populate(registeredSessionProvider.getLogMetadata(), videoChangedCallback);
                showTimelinePanel();

                leftDisplayPicker.populateVideos(videoPanel.getVideoManagers());
                rightDisplayPicker.populateVideos(videoPanel.getVideoManagers());

                timelineButton.setVisible(true);
                applyChangesAtPlayhead.setVisible(true);
                leftDisplayPickerPanel.setVisible(true);
                rightDisplayPickerPanel.setVisible(true);

                /* Reset the auto mode on the server back to the
                 * default game master is not in charge of approving
                 * strategies when in RTA lesson level, the external
                 * system connected via the GW module is. */
                StrategiesPanel.sendAutoMode(Dashboard.getInstance().getSettings().isAutoApplyStrategies()
                        && Dashboard.getInstance().getServerProperties().getLessonLevel() != LessonLevelEnum.RTA);

                sessionDataHeaderPanel.setWidget(sessionDataPanel.getHeaderPanel());
                sessionDataHeaderPanel.setVisible(true);
                sessionDataPanel.getHeaderPanel().getBookmarkCreator().setRecordGestureCommand(new GestureCommands() {
                    @Override
                    public void record() {
                        startRecordGesture();
                    }

                    @Override
                    public void displayDetailedError(String title,
                            DetailedExceptionSerializedWrapper exceptionWrapper) {
                        displayGestureDetailedErrorDialog(title, exceptionWrapper);
                    }

                    @Override
                    public void displayError(String title, String description) {
                        displayGestureErrorDialog(title, description);
                    }
                });
                
                strategiesPanel.setTimelineNavigator(timelinePanel);
                
                bookmarkPanel.setTimelineNavigator(timelinePanel);

                // 3/2/21 - current requirement is to default to showing just the map on left, nothing on right
                loadPreferredDisplayOptions(DisplayOptions.MAP, null);
                mainDeck.showWidget(mainDeck.getWidgetIndex(selectedSessionPanel));
            }
        });
    }

    /**
     * Fetches the list of knowledge sessions from the server and shows them once
     * they are ready
     * 
     * @param showActiveSessions whether active sessions should be shown. If false,
     * past sessions will be shown instead.
     */
    private void showKnowledgeSessions(final boolean showActiveSessions) {
        
        if (showActiveSessions) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Show active session list widget");
            }

            sessionListDeck.showWidget(sessionListDeck.getWidgetIndex(activeSessionListWidget));
            mainDeck.showWidget(mainDeck.getWidgetIndex(sessionListSplit));

            /* Poll for active knowledge sessions */
            dashboardService.fetchActiveKnowledgeSessions(new AsyncCallback<GetActiveKnowledgeSessionsResponse>() {
                @Override
                public void onSuccess(GetActiveKnowledgeSessionsResponse response) {
                    if (!response.isSuccess() || response.getKnowledgeSessions() == null) {
                        return;
                    }

                    Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = response.getKnowledgeSessions()
                            .getKnowledgeSessionMap();
                    if (CollectionUtils.isEmpty(knowledgeSessionMap)) {
                        return;
                    }

                    /* Add the sessions to the provider */
                    for (AbstractKnowledgeSession session : knowledgeSessionMap.values()) {
                        activeSessionProvider.addActiveSession(session);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.log(Level.SEVERE, "Error occurred trying to fetch the active knowledge sessions.", t);
                }
            });
        } else {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Show past session list widget");
            }

            /* Kick off a request for the server to collect the past session log
             * file list */
            dashboardService.fetchLogsAvailableForPlayback(UiManager.getInstance().getSessionId(),
                    new AsyncCallback<RpcResponse>() {

                        @Override
                        public void onSuccess(RpcResponse response) {

                            if (response.isSuccess()) {
                                checkFetchAARLogsLoadProgress();
                            } else {

                                UiManager.getInstance().displayDetailedErrorDialog("Failed to retrieve past sessions",
                                        response.getResponse(), response.getAdditionalInformation(),
                                        response.getErrorStackTrace(), null);

                                // show placeholder label
                                finishedFetchAARLogs(null);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            logger.severe(
                                    "Error caught when requesting that the server start to fetch past session log files list : "
                                            + t.getMessage());

                            UiManager.getInstance().displayErrorDialog("Failed to retrieve past sessions",
                                    "There was a server side error of\n" + t.getMessage(), null);

                            // show placeholder label
                            finishedFetchAARLogs(null);
                        }
                    });

            /* Show the progress bar while the server is working */
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Showing progress bar widget");
            }
            progressBar.clear();
            mainDeck.showWidget(mainDeck.getWidgetIndex(progressBar));
        }

    }

    /** Initializes the widgets in the settings section */
    private void initSettingsWidgets() {

        settingsMenu.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        updatePlayAllAlertsOptionLabel();
        muteAllAlerts.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                /* If it is currently muted; toggle to playing all sounds (or
                 * vice-versa). */
                final boolean playAllSounds = dashboardSettings.isAllSoundsVisualsMuted();
                Dashboard.VolumeSettings.ALL_SOUNDS.getSetting().setMuted(!playAllSounds);
                showPoorAssessmentVisual.setValue(playAllSounds, true);
                showOCAssessmentVisual.setValue(playAllSounds, true);
            }
        }, ClickEvent.getType());

        showPoorAssessmentVisual.getPreLabelStyle().setProperty("flex", "1");
        showPoorAssessmentVisual.setValue(!dashboardSettings.isHidePoorAssessmentVisual());
        showPoorAssessmentVisual.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                /* Toggle hide poor assessment visual option */
                boolean hide = !Boolean.TRUE.equals(event.getValue());
                dashboardSettings.setHidePoorAssessmentVisual(hide);
                updatePlayAllAlertsOptionLabel();

                redrawSessionDataPanel();

                setExternalMonitorSetting(ExternalMonitorConfig.Setting.ShowPoorAssessment, !hide);
            }
        });

        showOCAssessmentVisual.getPreLabelStyle().setProperty("flex", "1");
        showOCAssessmentVisual.setValue(!dashboardSettings.isHideOCAssessmentVisual());
        showOCAssessmentVisual.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                /* Toggle hide observer assessment visual option */
                boolean hide = !Boolean.TRUE.equals(event.getValue());
                dashboardSettings.setHideOCAssessmentVisual(hide);
                updatePlayAllAlertsOptionLabel();

                redrawSessionDataPanel();

                setExternalMonitorSetting(ExternalMonitorConfig.Setting.ShowObserverAssessment, !hide);
            }
        });

        prioritizeOCAssessment.setValue(dashboardSettings.isPrioritizeOCAssessment());
        prioritizeOCAssessment.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                /* Toggle hide observer assessment visual option */
                boolean prioritize = Boolean.TRUE.equals(event.getValue());
                dashboardSettings.setPrioritizeOCAssessment(prioritize);

                redrawSessionDataPanel();
            }
        });
        
        showAllTasks.setValue(dashboardSettings.isShowAllTasks());
        showAllTasks.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                /* Toggle show all tasks option */
                boolean showAll = Boolean.TRUE.equals(event.getValue());
                dashboardSettings.setShowAllTasks(showAll);

                redrawSessionDataPanel();
            }
        });
        autoAdvanceSessions.setValue(dashboardSettings.isAutoAdvanceSessions());
        autoAdvanceSessions.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				/* Toggle automatically advance sessions option */
				boolean autoAdvance = Boolean.TRUE.equals(event.getValue());
				dashboardSettings.setAutoAdvanceSessions(autoAdvance);
	
	        	redrawSessionDataPanel();
			}
		});

        showTeamOrgName.setValue(dashboardSettings.isShowTeamOrgName());
        showTeamOrgName.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> valueEvent) {
                showTeamOrgName(valueEvent.getValue());
            }
        });
        
        showMiniMap.setValue(dashboardSettings.isShowMiniMap());
        showMiniMap.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> valueEvent) {
				showMiniMap(valueEvent.getValue());
			}
		});

        showScenarioSupport.setValue(dashboardSettings.isShowScenarioSupport());
        showScenarioSupport.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> valueEvent) {

                dashboardSettings.setShowScenarioSupport(valueEvent.getValue());
                logger.info("Changing show scenario support setting to " + dashboardSettings.isShowScenarioSupport());

                sessionDataPanel.applyFilter();
                
                statePane.applySettingsFilter();
                
                redrawSessionDataPanel();

                if (outerSplitter.isHidden(timelinePanel)) {
                    return;
                }

                timelinePanel.refresh(true);
            }
        });
        
        hideGoodAutoAssessments.setValue(dashboardSettings.isHideGoodAutoAssessments());
        hideGoodAutoAssessments.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> valueEvent) {

                dashboardSettings.setHideGoodAutoAssessments(valueEvent.getValue());
                logger.info("Changing hide good automated assessments setting to " + dashboardSettings.isHideGoodAutoAssessments());

                sessionDataPanel.applyFilter();
                
                statePane.applySettingsFilter();
                
                redrawSessionDataPanel();

                if (outerSplitter.isHidden(timelinePanel)) {
                    return;
                }

                timelinePanel.refresh(true);
            }
        });

        showOcOnly.setValue(dashboardSettings.isShowOcOnly());
        showOcOnly.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean showOnlyOc = Boolean.TRUE.equals(event.getValue());
                dashboardSettings.setShowOcOnly(showOnlyOc);               
                
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Changing show OC only setting to " + dashboardSettings.isShowOcOnly());
                }

                sessionDataPanel.applyFilter();
                timelinePanel.refresh(true);
                
                redrawSessionDataPanel();
            }
        });

        monitorWithARES.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                updateGatewayConnections(true);
            }
        });

        monitorWithDIS.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                updateGatewayConnections(true);
            }
        });

        applyChangesAtPlayhead.setValue(dashboardSettings.isApplyChangesAtPlayhead());
        applyChangesAtPlayhead.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> valueEvent) {
                dashboardSettings.setApplyChangesAtPlayhead(valueEvent.getValue());
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Changing apply changes at playhead setting to "
                            + dashboardSettings.isApplyChangesAtPlayhead());
                }
            }
        });
    }

    /**
     * Populate the parent panel with the selected option's associated widget.
     * 
     * @param selectedOption the selected option.
     * @param parentPanel the parent panel to populate.
     */
    private void selectedOption(DisplayOptions selectedOption, SimplePanel parentPanel) {
        
        /* Save the user's selection to local storage so it can be reloaded when they revisit*/
        if(leftContentPanel.equals(parentPanel)) {
            Dashboard.getInstance().getSettings().setLeftDisplayPicker(selectedOption != null
                    ? selectedOption.name()
                    : DisplayOptions.NONE.name());
            
        } else if(rightContentPanel.equals(parentPanel)){
            Dashboard.getInstance().getSettings().setRightDisplayPicker(selectedOption != null
                    ? selectedOption.name()
                    : DisplayOptions.NONE.name());
        }
        
        if (selectedOption == null || selectedOption == DisplayOptions.NONE) {
            parentPanel.clear();
            mainSplitter.setWidgetHidden(parentPanel, true);
            setMainSplitterDragBarVisibility(false);
            return;
        }

        switch (selectedOption) {
        case MAP:
            parentPanel.setWidget(mapPanel);
            break;
        case ASSESSMENTS:
            parentPanel.setWidget(sessionDataPanel);
            break;
        case VIDEO:
            parentPanel.setWidget(videoPanel);
            break;
        case STRATEGIES:
            parentPanel.setWidget(strategiesPanel);
            break;
        case BOOKMARK:
            parentPanel.setWidget(bookmarkPanel);
            break;
		default:
			break;
        }

        /* Add width to the right panel when it is used for the first time */
        if (parentPanel == rightContentPanel && mainSplitter.getWidgetSize(parentPanel) == 0) {
            int fullSize = this.getElement().getClientWidth() - filterPanelContainer.getElement().getClientWidth();

            /* Take half the remaining space so the left panel is still
             * visible */
            fullSize /= 2;
            mainSplitter.setWidgetSize(parentPanel, fullSize);
        }

        mainSplitter.setWidgetHidden(parentPanel, false);
        setMainSplitterDragBarVisibility(true);
    }

    /**
     * Force a redraw of all session data panels (i.e. task and concept panels) using
     * the last task/concept states the panels received.
     */
    private void redrawSessionDataPanel(){

        AssessmentSoundType requestedSoundType = null;
        AssessmentSoundType sessionRequestedSoundType = sessionDataPanel.redraw();

        if (AssessmentSoundType.isHigherPriority(sessionRequestedSoundType, requestedSoundType)) {
            // the session requested sound type is higher priority for this
            // redraw than the current one set for this redraw
            requestedSoundType = sessionRequestedSoundType;
        }

        if (requestedSoundType == AssessmentSoundType.GOOD_ASSESSMENT) {
            playGoodPerformanceBeep(registeredSessionProvider.getRegisteredSession());

        } else if (requestedSoundType == AssessmentSoundType.POOR_ASSESSMENT) {
            playPoorPerformanceBeep(registeredSessionProvider.getRegisteredSession());
        }
    }

    /**
     * Asks the server for the latest progress on fetching the log sessions list. The progress
     * is displayed to the user on the progress bar.  When the server indicates the request is
     * completed, the session lists is shown.  If the load operation is still
     * on going, this method will recursively call itself.
     */
    private void checkFetchAARLogsLoadProgress() {

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Checking load progress for AAR logs");
        }

        dashboardService.getFetchLogsAvailableForPlaybackProgress(UiManager.getInstance().getSessionId(),
                new AsyncCallback<LoadedProgressIndicator<Collection<LogMetadata>>>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe("Error caught with getting fetch logs available for playback progress: " + t.getMessage());

                UiManager.getInstance().displayErrorDialog("Failed to retrieve past sessions", "There was a server side error of\n"+t.getMessage(), null);

                // show placeholder label
                finishedFetchAARLogs(null);
            }

            @Override
            public void onSuccess(LoadedProgressIndicator<Collection<LogMetadata>> loadProgressResponse) {

                if(loadProgressResponse.getException() != null){

                    UiManager.getInstance().displayDetailedErrorDialog("Failed to retrieve past sessions",
                            loadProgressResponse.getException().getReason(), loadProgressResponse.getException().getDetails(),
                            loadProgressResponse.getException().getErrorStackTrace(), null);

                    // show placeholder label
                    finishedFetchAARLogs(null);

                }else if(loadProgressResponse.isComplete()){
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("AAR fetch logs request load progress has completed");
                    }

                    //if the import has finished, deal with the completed import
                    finishedFetchAARLogs(loadProgressResponse.getPayload());

                } else {
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("AAR fetch logs request load progress continues.."+loadProgressResponse);
                    }

                    //otherwise, check to see if there is progress information
                    progressBar.updateProgress(loadProgressResponse);

                    //schedule another poll for progress 1 second from now
                    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                        @Override
                        public boolean execute() {

                            checkFetchAARLogsLoadProgress();

                            return false;
                        }

                    }, 1000);
                }
            }

        });

    }

    /**
     * Handle the server responding to the request for domain session message logs.
     *
     * @param logMetadatas information about the log files on the server.  If null or empty
     * the session list will show the placeholder label as this is an indication that
     * there are no log files on the server.
     */
    private void finishedFetchAARLogs(Collection<LogMetadata> logMetadatas){

        sessionToLogMetadataMap.clear();
        aarListWidget.clearSessions();

        if (mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(progressBar)) {
            // still showing the progress bar, replace with the AAR list widget
            if (logger.isLoggable(Level.INFO)) {
                logger.info("show aar list widget");
            }

            sessionListDeck.showWidget(sessionListDeck.getWidgetIndex(aarListWidget));
            mainDeck.showWidget(mainDeck.getWidgetIndex(sessionListSplit));
            aarListWidget.scrollToTop();
        }

        progressBar.clear();

        if (logMetadatas != null) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("finishedFetchAARLogs called with " + logMetadatas.size() + " log file(s).");
            }

            for (LogMetadata logMeta : logMetadatas) {
                sessionToLogMetadataMap.put(logMeta.getSession(), logMeta);
            }
        }

        /* Rebuild the session filter for past sessions (this will also
         * deregister applicable active sessions) */
        sessionListFilter.rebuildSessionFilter(!isSessionListInPlaybackMode());
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to the list of active knowledge sessions. */
        activeSessionProvider.addManagedHandler(this);

        /* Subscribe to timeline changes */
        TimelineProvider.getInstance().addManagedHandler(this);
    }

    @Override
    protected void onDetach() {
        /* Unsubscribe from providers */
        if (sessionDataPanel != null) {
            sessionDataPanel.unsubscribe();
        }

        /* Clear all active sessions from the provider on detach */
        activeSessionProvider.clearActiveSessions();

            dashboardService.cleanupGameMaster(BrowserSession.getInstance().getBrowserSessionKey(), 
                    new AsyncCallback<GenericRpcResponse<Void>>() {
                @Override
                public void onSuccess(GenericRpcResponse<Void> result) {
                    /* Nothing to do */
                }
    
                @Override
                public void onFailure(Throwable caught) {
                    if (logger.isLoggable(Level.SEVERE)) {
                        logger.log(Level.SEVERE, "There was a problem cleaning up the game master server-side resources.",
                                caught);
                    }
                }
            });

        /* Remove any video references */
        videoPanel.reset();

        /* Detach this panel */
        super.onDetach();
    }

    /**
     * Gets the map layer that is being used to display map data for the domain session with the given ID
     * 
     * @param domainSessionId the ID of the domain session whose map layer is needed
     * @return the map layer corresponding to the given domain session ID
     */
    public SessionMapLayer getSessionMapLayer(int domainSessionId) {
        return mapPanel.getSessionMapLayer(domainSessionId);
    }

    /**
     * Show the timeline panel.
     */
    private void showTimelinePanel() {
        timelinePanel.reload();
        controlPanel.getTimelineControls().setVisible(true);

        outerSplitter.setWidgetHidden(timelinePanel, false);
        timelineButton.addStyleName(selectedButtonColorStyle);

        outerSplitter.setWidgetSize(timelinePanel, 300);
        outerSplitter.forceLayout();
    }

    /**
     * Hide the timeline panel.
     */
    private void hideTimelinePanel() {
        /* Exit early if already hidden */
        if (outerSplitter.isHidden(timelinePanel)) {
            return;
        }

        controlPanel.getTimelineControls().setVisible(false);
        outerSplitter.setWidgetHidden(timelinePanel, true);
        timelineButton.removeStyleName(selectedButtonColorStyle);
    }

    /**
     * Set the visibility of the {@link #mainSplitter split layout panel's} drag
     * bars.
     *
     * @param show true to show the drag bars; false to hide them.
     */
    private void setMainSplitterDragBarVisibility(boolean show) {
        final String selector = "." + style.mainSplitter() + " > div > div.gwt-SplitLayoutPanel-HDragger";
        NodeList<Node> nodes = JsniUtility.querySelectorAll(mainSplitter.getElement(), selector);

        final String styleName = style.hideHorizontalDraggers();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.getItem(i);
            final Element parentElement = node.getParentElement();
            if (parentElement == null) {
                continue;
            }

            if (show) {
                parentElement.removeClassName(styleName);
            } else {
                parentElement.addClassName(styleName);
            }
        }
    }

    /**
     * Gets the game master's user name.
     *
     * @return The username of the game master.
     */
    public static String getGameMasterUserName() {
        final String actualUser = UiManager.getInstance().getActualUserName();
        return StringUtils.isNotBlank(actualUser) ? actualUser : UiManager.getInstance().getUserName();
    }

    /**
     * Checks if the {@link #sessionListDeck} is showing the playback logs.
     *
     * @return true if the session list is showing playback logs; false if it is
     *         showing active sessions.
     */
    public boolean isSessionListInPlaybackMode() {
        return sessionListDeck.getVisibleWidget() == sessionListDeck.getWidgetIndex(aarListWidget);
    }

    /**
     * Get the sessions that were retrieved for the playback logs.
     *
     * @return mapping of knowledge session to log metadata for that session
     */
    public Map<AbstractKnowledgeSession, LogMetadata> getPlaybackLogSessions() {
        return sessionToLogMetadataMap;
    }

    /**
     * Get the active session list widget.
     *
     * @return the widget showing the list of active sessions.
     */
    public KnowledgeSessionListWidget getActiveSessionListWidget() {
        return activeSessionListWidget;
    }

    /**
     * Get the AAR session list widget.
     *
     * @return the widget showing the list of AAR sessions.
     */
    public KnowledgeSessionListWidget getAarSessionListWidget() {
        return aarListWidget;
    }

    /**
     * Get the height of the content under the header
     *
     * @return the sub-header content height
     */
    public double getSubHeaderContentHeight() {
        return mainPanel.getElement().getClientHeight();
    }

    /**
     * Registers the provided knowledge session with the dashboard service.
     *
     * @param knowledgeSession the knowledge session being registered.
     * @param callback the callback to execute once the register is complete.
     *        Can't be null.
     */
    private void registerKnowledgeSession(final AbstractKnowledgeSession knowledgeSession,
            final AsyncCallback<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        final boolean isPast = knowledgeSession != null && knowledgeSession.inPastSessionMode();

        final AsyncCallback<GenericRpcResponse<Void>> rpcCallback = new AsyncCallback<GenericRpcResponse<Void>>() {
            @Override
            public void onFailure(Throwable caught) {
                registeredSessionProvider.setRegisteredSession(null);

                /* Heard back from server, hide loading dialogs and remove
                 * glass */
                componentLoadingProvider.loadingComplete(LoadingType.REGISTERED_SESSION);
                statePane.sessionUnloadedUnexpectedly();

                StringBuilder sb = new StringBuilder();
                sb.append("There was a problem starting ");
                if (isPast) {
                    sb.append("playback for ");
                }
                sb.append("the knowledge session because ").append(caught.getMessage());

                UiManager.getInstance().displayErrorDialog(isPast ? "Playback Start Error" : "Start Error",
                        sb.toString(), null);
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(GenericRpcResponse<Void> result) {
                /* Heard back from server, hide loading dialog and remove
                 * glass */
                if (result.getWasSuccessful()) {
                    callback.onSuccess(true);
                    componentLoadingProvider.loadingComplete(LoadingType.REGISTERED_SESSION);
                } else {
                    componentLoadingProvider.loadingComplete(LoadingType.REGISTERED_SESSION);
                    statePane.sessionUnloadedUnexpectedly(); // also remove state pane loading dialog since server won't provide status update
                    registeredSessionProvider.setRegisteredSession(null);
                    UiManager.getInstance().displayDetailedErrorDialog(isPast ? "Playback Start Error" : "Start Error",
                            result.getException());
                    callback.onSuccess(false);
                }
            }
        };

        if (isPast) {
            /* Start the loading dialog for all the components we know we will
             * need for playback */
            componentLoadingProvider.startLoading(LoadingType.REGISTERED_SESSION, "Loading Session",
                    "Building playback experience...");

            /* Attempt to start playback of the message on the server */
            LogMetadata metadata = sessionToLogMetadataMap.get(knowledgeSession);

            /* Update the new registered session */
            registeredSessionProvider.setRegisteredSessionByLog(metadata);

            dashboardService.registerKnowledgeSessionPlayback(
                    BrowserSession.getInstance().getBrowserSessionKey(), metadata, rpcCallback);
        } else {
            if (knowledgeSession != null) {
                /* Start the loading dialog for all the components we know we will
                 * need for an active session */
                componentLoadingProvider.startLoading(LoadingType.REGISTERED_SESSION, "Loading Session",
                        "Please wait...");
            }

            /* Update the new registered session */
            registeredSessionProvider.setRegisteredSession(knowledgeSession);

            dashboardService.registerKnowledgeSessionMonitor(
                    BrowserSession.getInstance().getBrowserSessionKey(), knowledgeSession, rpcCallback);
        }
    }

    /**
     * Deregisters the provided knowledge session from the dashboard service.
     *
     * @param knowledgeSession the knowledge session being deregistered.
     * @param callback the callback to execute once the deregister is complete.
     *        Can't be null.
     */
    private void deregisterKnowledgeSession(final AbstractKnowledgeSession knowledgeSession,
            final AsyncCallback<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        } else if (knowledgeSession == null) {
            callback.onSuccess(true);
            return;
        }

        final boolean isPast = knowledgeSession.inPastSessionMode();

        int domainSessionId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        if (registeredSessionProvider.isRegistered(domainSessionId)) {
            registeredSessionProvider.setRegisteredSession(null);
            TimelineProvider.getInstance().reset();
        }

        final AsyncCallback<GenericRpcResponse<Void>> rpcCallback = new AsyncCallback<GenericRpcResponse<Void>>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.warning("Failed to deregister the knowledge session" + (isPast ? " playback" : "") + " because "
                        + caught.getMessage());
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(GenericRpcResponse<Void> result) {
                if (result.getWasSuccessful()) {
                    callback.onSuccess(true);
                } else {
                    logger.warning("Failed to deregister the knowledge session" + (isPast ? " playback" : "") + ".");
                    callback.onSuccess(false);
                }
            }
        };

        if (isPast) {
            dashboardService.deregisterKnowledgeSessionPlayback(
                    BrowserSession.getInstance().getBrowserSessionKey(), rpcCallback);
        } else {
            dashboardService.deregisterKnowledgeSessionMonitor(
                    BrowserSession.getInstance().getBrowserSessionKey(), knowledgeSession, rpcCallback);
        }
    }

    @Override
    public void sessionAdded(final AbstractKnowledgeSession knowledgeSession) {
        /* Nothing to do */
    }

    @Override
    public void sessionEnded(final AbstractKnowledgeSession knowledgeSession) {
        final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        final boolean isActiveSession = activeSessionProvider.isActiveSession(dsId);

        /* Playback can still be active when ended */
        if (isActiveSession) {
            activeSessionListWidget.removeSession(knowledgeSession);
            return;
        }

        /* Check if the ended session is the one selected for the game master
         * panel */
        if (registeredSessionProvider.isRegistered(dsId)) {
            deregisterKnowledgeSession(knowledgeSession, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    /* Nothing to do */
                }

                @Override
                public void onSuccess(Boolean result) {
                    /* Nothing to do */
                }
            });
        }
    }

    /**
     * Update the 'play all alerts' option based on the other sound settings.
     *
     */
    private void updatePlayAllAlertsOptionLabel() {
        if (dashboardSettings.isAllSoundsVisualsMuted()) {
            muteAllAlerts.setHTML(PLAY_ALL_ALERTS + " <i class='fa fa-volume-up' style='font-size:large; padding-left:45px;'/> <i class='fa fa-eye' style='font-size:large; padding-left:5px;'/>");
        } else {
            muteAllAlerts.setHTML(MUTE_ALL_ALERTS + " <i class='fa fa-volume-off' style='font-size:large; padding-left:20px;'/> <i class='fa fa-eye-slash' style='font-size:large; padding-left:5px;'/>");
        }
    }

    /**
     * Redraw the entity and team map icons in order to apply the flag
     * of whether the team org names should be shown. Team org names are the white
     * labels to the lower left of the map icons.
     *
     * @param show true if the team org names should be shown on the map icons.
     */
    private void showTeamOrgName(boolean show) {
        dashboardSettings.setShowTeamOrgName(show);
        mapPanel.redrawDataPoints();
    }
    
    /**
     * Change whether the mini map is shown or hidden.
     * @param show true to show the mini map.
     */
    private void showMiniMap(boolean show){
    	mapPanel.showMiniMap(show);
    }

    /**
     * Requests that the server connect to the current enabled gateway connections and share session data
     * with it so that it can be used as an external system
     *
     * @param showDialog whether or not to show a loading dialog while the server connects/disconnects
     * from the app. Can be used to restrict user input and show a message while waiting for the
     * server call to finish.
     */
    private void updateGatewayConnections(final boolean showDialog) {
        
        boolean useDIS = monitorWithDIS.getValue();
        
        List<TrainingApplicationEnum> taTypes = new ArrayList<>();
        if(monitorWithARES.getValue()){
            taTypes.add(TrainingApplicationEnum.ARES);
            
            // in order to show DIS Entity States in ARES, the DIS gateway connection is needed
            useDIS = true;
        }

        if(showDialog) {

            //show a loading dialog while the server is being contacted to connect/disconnect the application
            if (taTypes.isEmpty() && !useDIS) {
                componentLoadingProvider.startLoading(LoadingType.GATEWAY_CONNECTIONS, "Disconnecting connections",
                        "Please wait while GIFT disconnects from the Gateway connections to stop sharing playback");

            } else {
                componentLoadingProvider.startLoading(LoadingType.GATEWAY_CONNECTIONS, "Connecting",
                        "Please wait while the appropriate Gateway connections are established for sharing playback.");
            }
        }
        
        GatewayConnection gConn = new GatewayConnection(taTypes, useDIS);

        //contact the server to connect to or disconnect from the appropriate application
        dashboardService.setGatewayConnections(
            UiManager.getInstance().getSessionId(),
            gConn, new AsyncCallback<GenericRpcResponse<Void>>() {

                @Override
                public void onSuccess(GenericRpcResponse<Void> result) {

                    componentLoadingProvider.loadingComplete(LoadingType.GATEWAY_CONNECTIONS);

                    if(!result.getWasSuccessful()) {
                        monitorWithARES.setValue(false);
                        monitorWithDIS.setValue(false);
                        UiManager.getInstance().displayDetailedErrorDialog(
                                "Failed to connect to application", result.getException());
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    monitorWithARES.setValue(false);
                    monitorWithDIS.setValue(false);

                    componentLoadingProvider.loadingComplete(LoadingType.GATEWAY_CONNECTIONS);

                    logger.severe("Error caught with setting external monitor app: " + caught.getMessage());

                    UiManager.getInstance().displayErrorDialog(
                            "Failed to connect to application",
                            "There was a server side error of\n"+caught.getMessage(), null);
                }
            });
    }

    /**
     * Adjusts the configuration for external monitor applications that are being shared with so that the given
     * configuration setting is enabled or disabled
     *
     * @param setting the setting in the external monitor configuration to change. If null, no action will
     * be performed.
     * @param enabled whether the given setting should be enabled or disabled.
     */
    private void setExternalMonitorSetting(ExternalMonitorConfig.Setting setting, boolean enabled) {

        if(setting == null) {
            return;
        }

        //adjust the configuration
        if(externalMonitorConfig.set(setting, enabled)) {

            //if the configuration has meaningfully changed, send the updated configuration to the server
            dashboardService.setExternalMonitorConfig(
                    BrowserSession.getInstance().getBrowserSessionKey(),
                    externalMonitorConfig,
                    new AsyncCallback<GenericRpcResponse<Void>>() {

                        @Override
                        public void onSuccess(GenericRpcResponse<Void> result) {

                            if(!result.getWasSuccessful()) {
                                UiManager.getInstance().displayDetailedErrorDialog(
                                        "Failed to adjust settings for connected application", result.getException());
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {

                            logger.severe("Error caught with setting external monitor app: " + caught.getMessage());

                            UiManager.getInstance().displayErrorDialog(
                                    "Failed to adjust settings for connected application",
                                    "There was a server side error of\n"+caught.getMessage(), null);
                        }
            });
        }
    }

    /**
     * Plays a beep indicating that performance within the given session is good. If the session has defined its
     * own custom good performance audio cue, then that audio will be used instead of the default.
     *
     * @param session the session whose performance is being indicated by the beep. Can be null.
     */
    public static void playGoodPerformanceBeep(AbstractKnowledgeSession session) {
        
        double volume = Dashboard.VolumeSettings.GOOD_ASSESSMENT_SOUND.getSetting().getVolume();

        if(session != null
                && session.getObserverControls() != null
                && session.getObserverControls().getGoodPerformanceAudioUrl() != null) {

            String url = Dashboard.getInstance().getServerProperties().getSessionOutputServerPath() +
                    session.getObserverControls().getGoodPerformanceAudioUrl();
            logger.info("using good performance audio path = "+url);
            Dashboard.playBeep(url, volume);

        } else {
            Dashboard.playGoodBeep(volume);
        }
    }

    /**
     * Plays a beep indicating that performance within the given session is good. If the session has defined its
     * own custom poor performance audio cue, then that audio will be used instead of the default.
     *
     * @param session the session whose performance is being indicated by the beep. Can be null.
     */
    public static void playPoorPerformanceBeep(AbstractKnowledgeSession session) {
        
        double volume = Dashboard.VolumeSettings.POOR_ASSESSMENT_SOUND.getSetting().getVolume();

        if(session != null
                && session.getObserverControls() != null
                && session.getObserverControls().getPoorPerformanceAudioUrl() != null) {

            String url = Dashboard.getInstance().getServerProperties().getSessionOutputServerPath() +
                    session.getObserverControls().getPoorPerformanceAudioUrl();
            logger.info("using poor performance audio path = "+url);
            Dashboard.playBeep(url, volume);

        } else {
            Dashboard.playPoorBeep(volume);
        }
    }
    
    /**
     * Plays the file at the given workspace location that contains audio captured alongside a knowledge session. 
     * If no captured audio is found, then no audio will be played.
     * 
     * @param capturedAudioPath the GIFT/output/domainSessions/ path to the file containing the captured audio. 
     * If null, no audio will play. E.g. domainSession697_uId1/beep.mp3
     * @return If successful, provides a handle that can be used to control the played audio. Can be null.
     */
    public static AudioElement playCapturedAudio(final String capturedAudioPath) {
        
        if(capturedAudioPath == null) {
            return null;
        }
        
        final String servletCapturedAudioPath = Dashboard.getInstance().getServerProperties().getSessionOutputServerPath()
                + capturedAudioPath;
        logger.info("using captured audio path = "+servletCapturedAudioPath);
                        
        //play the captured audio
        AudioElement audio = JsniUtility.playAudio(servletCapturedAudioPath, Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().getVolume());
        if(audio != null){
            audio.setMuted(Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().isMuted());
        }else{
            logger.warning("failed to find audio element for "+servletCapturedAudioPath);
        }
        
        return audio;
    }

    @Override
    public void onPlayheadMoved(long playbackTime) {
        /* Do nothing if not a playback or video files don't exist */
        final LogMetadata logMetadata = registeredSessionProvider.getLogMetadata();
        if (logMetadata == null || !videoPanel.hasVideos()) {
            return;
        }

        videoPanel.updateCurrentTime(playbackTime, timelinePanel.isPaused());
    }

    @Override
    public void reloadTimeline() {
        /* Nothing to do */
    }

    @Override
    public void showTasks(Set<Integer> taskIds) {
        
        // 3/2/21 - current requirement is to show the assessment panel on right when an objective is picked.  This logic
        //          will do that and if there is only 1 objective in the list it will show the assessment panel as well.
        //          If any other panel is in the right, this will not change it right now.  That requirement could change.
        if(taskIds != null && taskIds.size() == 1 &&
                rightDisplayPicker.getValue() == DisplayOptions.NONE){
            rightDisplayPicker.setValue(DisplayOptions.ASSESSMENTS, true);
        }
    }
    
    /**
     * Automatically performs the UI operations needed to load the past session version of the
     * given session and begin playing it back. This can be used to allow users to instantly jump
     * from an active session to its past session version after it has ended.
     * 
     * @param session the session whose past session log should be loaded. Can be either an active
     * session or a past session. If null, no session will be auto-loaded, but the list of past
     * sessions will be shown.
     */
    public void autoLoadPastSession(final AbstractKnowledgeSession session) {

        componentLoadingProvider.startLoading(LoadingType.FETCH_PLACKBACK_LOG, "Loading Session Playback",
                "Please wait while the session's playback data is gathered...");

        dashboardService.fetchLogForPlayback(UiManager.getInstance().getSessionId(), session, 
                new AsyncCallback<GenericRpcResponse<LogMetadata>>() {
            
            @Override
            public void onSuccess(GenericRpcResponse<LogMetadata> result) {
                
                if(!result.getWasSuccessful()) {
                    
                    componentLoadingProvider.loadingComplete(LoadingType.FETCH_PLACKBACK_LOG);
                    
                    UiManager.getInstance().displayDetailedErrorDialog("Failed to retrieve past sessions",
                            result.getException().getReason(), result.getException().getDetails(),
                            result.getException().getErrorStackTrace(), null);
                    
                    return;
                }
                
                LogMetadata logMeta = result.getContent();
                
                sessionToLogMetadataMap.put(logMeta.getSession(), logMeta);

                //automatically register the session that was chosen to auto load
                registerPastSession(logMeta.getSession());
                componentLoadingProvider.loadingComplete(LoadingType.FETCH_PLACKBACK_LOG);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                
                componentLoadingProvider.loadingComplete(LoadingType.FETCH_PLACKBACK_LOG);
                
                logger.severe(
                        "Error caught when requesting that the server start to fetch past session log for " 
                                + session + " : " + caught.getMessage());

                UiManager.getInstance().displayErrorDialog("Failed to retrieve past session",
                        "There was a server side error of\n" + caught.getMessage(), null);
            }
        });
    }
   
   /**
    * Stops recording using touch gestures and saves the recording if one is in progress
    */
   public void stopRecording() {
       
       sessionDataPanel.getHeaderPanel().getBookmarkCreator().setRecording(false);
       
       gestureManager.setMode(GestureMode.DEFAULT);
   }
   
    /**
     * Starts recording using touch gestures and saves when recording is stopped
     */
    public void startRecording() {
        
        sessionDataPanel.getHeaderPanel().getBookmarkCreator().setRecording(true);
    }
    
    /** 
     * Prompts the user asking if they want to cancel recording via touch gestures
     */
    public void cancelGestureRecording() {
      displayGestureConfirmDialog("Cancel Recording", "Are you sure you want to cancel this recording?", "Yes", "No", new ConfirmationDialogCallback() {

         @Override
         public void onDecline() {
             bookmarkBlockerPanel.block();
             
         }

         @Override
         public void onAccept() {
             MediaInputUtil.cancelRecording();
             gestureManager.setMode(GestureMode.DEFAULT);
         }
          
      });  
    }
    
    /** 
     * Prompts the user asking if they want to leave the touch gesture interface for recording
     */
    public void leaveGestureRecordingMode() {
          displayGestureConfirmDialog("Leave Gesture Recording Mode", "Are you sure you want to leave gesture recording mode?", "Yes", "No", new ConfirmationDialogCallback() {

             @Override
             public void onDecline() {
                 bookmarkBlockerPanel.block();
             }

             @Override
             public void onAccept() {
                 bookmarkBlockerPanel.unblock(); 
                 gestureManager.setMode(GestureMode.DEFAULT);
             }
              
          });  
        }
    
    /**
     * Displays a confirmation dialog for the bookmark gestures interface.
     * 
     * If Game Master is in full screen mode, then a dialog attached to the full screen element will be used instead
     * of the usual confirmation dialog. This is needed to make sure that the dialog shows up on top of the full screen
     * element, since dialogs attached to the body element are hidden.
     * 
     * @param title - Title for the dialog.
     * @param description - Description/message that will be displayed in the dialog to the user.
     * @param confirmLabel - (optional) The label for the 'confirm' button.  If null is specified the default of "Yes" is used.
     * @param declineLabel - (optional) The label for the 'decline' button.  If null is specified the default of "No" is used.
     * @param callback - (optional) A callback that will be triggered for the action that is taken by the user.
     */
    private void displayGestureConfirmDialog(final String title, final String description, final String confirmLabel, final String declineLabel, final ConfirmationDialogCallback callback) {
        
        if(JsniUtility.getFullscreenElement() == null) {
            
            /* The game master UI is being shown normally, so just show the dialog normally */
            UiManager.getInstance().displayConfirmDialog(title, description, confirmLabel, declineLabel, callback);
            return;
        }
        
        /* The game master UI is being focused on in full screen, so we need to show the dialog 
         * using the game master panel itself rather than body or else the dialog will be hidden. */
        logger.info("displaying confirmation dialog with title: " + title);

        if (gestureConfirmDialog.isModalShown() && gestureConfirmDialog.isSameDialog(title, description)) {
            logger.warning("Ignoring request to display the confirmation dialog.  It appears the same dialog is already being shown.");
            return;
        }

        gestureConfirmDialog.hide();

        gestureConfirmDialog.setData(title,  description, confirmLabel, declineLabel, callback);
        gestureConfirmDialog.show();
    }
    
    /**
     * Displays a detailed error dialog for the bookmark gestures interface.
     * 
     * If Game Master is in full screen mode, then a dialog attached to the full screen element will be used instead
     * of the usual error dialog. This is needed to make sure that the dialog shows up on top of the full screen
     * element, since dialogs attached to the body element are hidden.
     * 
     * @param title - The title of the dialog.
     * @param exceptionWrapper - A client-safe wrapper around a detailed exception.
     */
    protected void displayGestureDetailedErrorDialog(String title,
            DetailedExceptionSerializedWrapper detailedExceptionSerializedWrapper) {
        
        if(JsniUtility.getFullscreenElement() == null) {
            UiManager.getInstance().displayDetailedErrorDialog(title, detailedExceptionSerializedWrapper);
            return;
        }
        
        final String errorReason = detailedExceptionSerializedWrapper.getReason();
        final String errorDetails = detailedExceptionSerializedWrapper.getDetails();
        
        if (gestureErrorDialog.isModalShown() && gestureErrorDialog.isSameDialog(DialogType.DIALOG_ERROR, title, errorReason, errorDetails)) {
            logger.warning("Ignoring request to display the dialog.  It appears the same dialog is already being shown.");
            return;
        }

        gestureErrorDialog.hide();

        gestureErrorDialog.setData(DialogType.DIALOG_ERROR, title, errorReason, errorDetails, null);
        gestureErrorDialog.show();
    }

    /**
     * Displays a detailed error dialog for the bookmark gestures interface.
     * 
     * If Game Master is in full screen mode, then a dialog attached to the full screen element will be used instead
     * of the usual error dialog. This is needed to make sure that the dialog shows up on top of the full screen
     * element, since dialogs attached to the body element are hidden.
     * 
     * @param title - The title of the dialog.
     * @param description - The message for the dialog.
     */
    private void displayGestureErrorDialog(String title, String description) {
        
        if(JsniUtility.getFullscreenElement() == null) {
            UiManager.getInstance().displayErrorDialog(title, description, null);
            return;
        }
        
        if (gestureErrorDialog.isModalShown() && gestureErrorDialog.isSameDialog(DialogType.DIALOG_ERROR, title, description, null)) {
            logger.warning("Ignoring request to display the dialog.  It appears the same dialog is already being shown.");
            return;
        }

        gestureErrorDialog.hide();

        gestureErrorDialog.setData(DialogType.DIALOG_ERROR, title, description, null, null);
        gestureErrorDialog.show();
    }
    
    /**
     * Enables recording audio via touch gestures and modifes the UI as needed
     */
    private void startRecordGesture() {
        bookmarkBlockerPanel.block();
    }
    
    /**
     * The modes that a {@link GestureManager} can be switched between. Each mode has
     * a specific set of gesture inputs that it accepts.
     * 
     * @author nroberts
     */
    private enum GestureMode{
        
        /** The default mode. Allows gestures to start recording audio or create an empty bookmark */
        DEFAULT,
        
        /** Allows a gesture to stop recording audio */
        RECORDING_AUDIO,
    }
    
    /**
     * A manager that handles specific sequences of touch gestures and uses those gestures to
     * perform specific operations
     * 
     * @author nroberts
     */
    private class GestureManager {

        /** The number of milliseconds that the user must touch the screen continuously to cancel and disable gestures */
        private static final int CANCEL_TOUCH_DURATION_MS = 3000;

        /** The number of milliseconds to wait in order to receive another touch. Used to detect multi-touch. */
        private static final int MULTITOUCH_DELAY_MS = 500;

        /** Hom many times the user must touch in succession to begin recording audio */
        private static final int RECORD_AUDIO_TOUCH_COUNT = 3;

        /** Hom many times the user must touch in succession to create an empty note */
        private static final int EMPTY_NOTE_TOUCH_COUNT = 2;

        /** Hom many times the user must touch in succession to begin recording audio */
        private static final int CANCEL_RECORD_TOUCH_COUNT = 2;

        /** The last time that the user touched */
        private Long lastTouchTime = null;

        /** The number of times that the user has touched in succession*/
        private int touchCount;

        /** The mode that is currently being used to interpret touch gestures */
        private GestureMode mode = GestureMode.DEFAULT;

        /** A timer that lets the user leave and disable gestures after a delay */
        private final Timer leaveGestureTimer = new Timer() {

            @Override
            public void run() {

                if (lastTouchTime == null) {
                    return;
                }

                if (MediaInputUtil.isRecording()) {
                    
                    //if recording, simply cancel the recording
                    cancelGestureRecording();

                } else {
                    
                    //otherwise, leave the gestures interface entirely
                    leaveGestureRecordingMode();
                }

                touchCount = 0; // reset the touch counter if one touch is held too long
            }
        };
        
        /** A timer that creates an empty note afer a delay */
        private final Timer emptyNoteGestureTimer = new Timer() {
            
            @Override
            public void run() {
                
                // Create an emppty bookmark for the OC to potentially modify later
                sessionDataPanel.getHeaderPanel().getBookmarkCreator().createGlobalTextBookmark("");
            }
        };

        /**
         * Creates a new manager that handles touch gestures focused on the given element
         * 
         * @param captureWidget the widget that the user must touch in order to trigger touch
         * gestures. Cannot be null.
         */
        public GestureManager(Widget captureWidget) {
            
            if(captureWidget == null) {
                throw new IllegalArgumentException("The widget to capture touch gestures from cannot be null");
            }

            captureWidget.addDomHandler(new PointerDownHandler() {

                @Override
                public void onPointerDown(PointerDownEvent event) {
                    
                    //another touch was received, so cancel the empty note timer if it is running
                    emptyNoteGestureTimer.cancel();

                    long now = System.currentTimeMillis();

                    if (lastTouchTime == null || now - lastTouchTime > MULTITOUCH_DELAY_MS) {
                        
                        /* The time between touched has exceeded the period to be considered a 
                         * multi-touch, so reset the touch count */
                        touchCount = 1;

                    } else {

                        // Increase the multi-touch count since another touch was received
                        touchCount++;
                        
                        if (touchCount > RECORD_AUDIO_TOUCH_COUNT) {
                            touchCount = 0; // reset touch count once it exceeds the max count recognized
                        }

                        switch (mode) {

                            case DEFAULT:
    
                                if (touchCount == RECORD_AUDIO_TOUCH_COUNT) {
    
                                    // Start recording if not currently recording
                                    if (!MediaInputUtil.isRecording()) {
                                        startRecording();
                                        
                                        setMode(GestureMode.RECORDING_AUDIO);
                                    }
    
                                } else if (touchCount == EMPTY_NOTE_TOUCH_COUNT) {
    
                                    //wait for any additional touches before making an empty note
                                    emptyNoteGestureTimer.schedule(MULTITOUCH_DELAY_MS);
                                }
    
                                break;
    
                            case RECORDING_AUDIO:
    
                                if (touchCount == CANCEL_RECORD_TOUCH_COUNT) {
    
                                    // Stop recording if currently recording
                                    stopRecording();
    
                                    setMode(GestureMode.DEFAULT);
                                    
                                    touchCount = 0;
                                }
    
                                break;
                        }

                    }

                    lastTouchTime = now;

                    //start counting down in case the user touches long enough to leave
                    leaveGestureTimer.schedule(CANCEL_TOUCH_DURATION_MS);
                }

            }, PointerDownEvent.getType());

            captureWidget.addDomHandler(new PointerUpHandler() {

                @Override
                public void onPointerUp(PointerUpEvent event) {

                    //cancel leave timer, since the user did not touch long enough
                    leaveGestureTimer.cancel();
                }

            }, PointerUpEvent.getType());
            
            //initialize the mode to update the UI appropriately
            setMode(GestureMode.DEFAULT);
        }

        /**
         * Sets the current gesture mode to decide what gesture inputs should
         * be handled
         * 
         * @param mode the gesture mode. Cannot be null.
         */
        public void setMode(GestureMode mode) {
            
            if(mode == null) {
                throw new IllegalArgumentException("The gesture mode cannot be null");
            }
            
            this.mode = mode;
            
            switch(mode) {
            
                case DEFAULT:
                    
                    gestureMicIcon.setType(IconType.MICROPHONE_SLASH);
                    gestureSubText.setHTML("Triple tap to start audio recording"
                            + "<br>Double tap to make an empty note"); 
                    break;
                    
                case RECORDING_AUDIO:
                    
                    gestureMicIcon.setType(IconType.MICROPHONE);
                    gestureSubText.setText("Double tap to stop recording");
                    break;
                    
            }
        }
    }
}
