/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.aar.VideoMetadata;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AddVideoModalDialog.VideoChangedCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoError;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager.LoadVideoCallback;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingPriority;

/**
 * The panel containing all the videos in the session. Each video is handled by
 * a {@link VideoManager}.
 * 
 * @author sharrison
 */
public class SessionVideoPanel extends Composite{

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionVideoPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SessionVideoPanelUiBinder uiBinder = GWT.create(SessionVideoPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SessionVideoPanelUiBinder extends UiBinder<Widget, SessionVideoPanel> {
    }

    /** 10 second timeout to load each video */
    private static final int VIDEO_LOAD_TIMEOUT = 10000;
    
    /** 
     * The interval of time that should be allowed to pass between each check to see if 
     * the video timeout has fully elapsed
     */
    private static final int LOAD_TIMEOUT_INTERVAL_DURATION = 500;

    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * The CSS applied to each row of videos.
         * 
         * @return the CSS style name
         */
        String videoRow();
    }

    /** The style from the ui.xml */
    @UiField
    protected Style style;

    /**
     * The deck panel that allows for switching between the videos and the no
     * videos label
     */
    @UiField
    protected DeckPanel mainDeck;

    /** The label to show when there are no current videos */
    @UiField
    protected Label noVideosLabel;

    /** The panel containing the video players */
    @UiField
    protected FlowPanel videoGroupPanel;

    /**
     * The sorted loaded videos that could be played during the playback session
     */
    private final List<VideoManager> videoManagers = new ArrayList<>();

    /** The comparator for the videos to sort them by start time */
    private static final Comparator<VideoMetadata> VIDEO_COMPARATOR = new Comparator<VideoMetadata>() {
        @Override
        public int compare(VideoMetadata o1, VideoMetadata o2) {
            /* Primary sort by time */
            int timeCompare = o1.getStartTime().compareTo(o2.getStartTime());
            if (timeCompare != 0) {
                return timeCompare;
            }

            /* Secondary sort by title */
            if (StringUtils.isNotBlank(o1.getTitle())) {
                if (StringUtils.isBlank(o2.getTitle())) {
                    return -1;
                } else {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            } else if (StringUtils.isNotBlank(o2.getTitle())) {
                return 1;
            }

            /* Gave it our best shot; mark as equal */
            return 0;
        }
    };

    /** Constructor */
    public SessionVideoPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        showNoVideosLabel();
    }

    /**
     * Retrieve the list of video managers that are used in this panel.
     * 
     * @return the sorted (by start time) list of video managers in the panel.
     *         Will never be null.
     */
    public List<VideoManager> getVideoManagers() {
        return videoManagers;
    }

    /**
     * Get the video manager that is responsible for the provided metadata.
     * 
     * @param videoMetadata the video metadata to find in this panel.
     * @return the associated video manager. Can be null if the metadata was not
     *         found in this panel.
     */
    public VideoManager getVideoManager(VideoMetadata videoMetadata) {
        if (videoMetadata != null) {
            for (VideoManager vManager : videoManagers) {
                if (vManager.getVideoMetadata().equals(videoMetadata)) {
                    return vManager;
                }
            }
        }

        return null;
    }

    /**
     * Check if this panel contains any videos.
     * 
     * @return true if at least one video is present; false if there are none.
     */
    public boolean hasVideos() {
        return !videoManagers.isEmpty();
    }

    /** Pause all the videos. */
    public void pauseVideos() {
        for (VideoManager vidManager : videoManagers) {
            vidManager.pause();
        }
    }

    /**
     * Seek to the provided time (in millis) for all videos.
     * 
     * @param dateMillis the time in milliseconds to seek.
     */
    public void seek(final long dateMillis) {
        for (final VideoManager vidManager : videoManagers) {
            
            if(vidManager.getVideoContainerPanel().isAttached()) {
                vidManager.seek(dateMillis);
                
            } else {
                
                /* Video panel is not yet attached, so seek when it is attached */
                final HandlerRegistration[] registration = new HandlerRegistration[1];
                registration[0] = vidManager.getVideoContainerPanel().addAttachHandler(new AttachEvent.Handler() {

                    @Override
                    public void onAttachOrDetach(AttachEvent event) {
                        
                        if(event.isAttached()) {
                            vidManager.seek(dateMillis);
                        }
                        
                        registration[0].removeHandler();
                    }
                });
            }
        }
    }

    /**
     * Show the label. This should be called when there are no videos present.
     */
    private void showNoVideosLabel() {
        mainDeck.showWidget(mainDeck.getWidgetIndex(noVideosLabel));
    }

    /**
     * Show the videos. This should be called when there is at least one video
     * present.
     */
    private void showVideoGroupPanel() {
        mainDeck.showWidget(mainDeck.getWidgetIndex(videoGroupPanel));
    }

    /**
     * Clears the panel and resets all videos.
     */
    public void reset() {
        for (VideoManager vidManager : videoManagers) {
            vidManager.resetVideo();
        }

        videoManagers.clear();
        videoGroupPanel.clear();

        showNoVideosLabel();
    }

    /**
     * Creates the video managers for the session videos
     * 
     * @param logMetadata the metadata for the playback log used to populate
     *        this panel.
     * @param videoChangedCallback handler for video updates such as changing the video properties or deleting the video.
     * Can't be null. 
     */
    public void populate(final LogMetadata logMetadata, final VideoChangedCallback videoChangedCallback) {
        reset();


        if (logMetadata == null || CollectionUtils.isEmpty(logMetadata.getVideoFiles())) {
            /* All 0 videos have been loaded */
            return;
        }

        LoadingDialogProvider.getInstance().startLoading(LoadingType.VIDEOS, LoadingPriority.HIGH, "Loading Videos",
                "Loading the videos linked to this session...");

        /* Sort the video files by start time. This is so we can append index
         * labels to the videos properly. */
        final List<VideoMetadata> sortedMetas = new ArrayList<VideoMetadata>(logMetadata.getVideoFiles());
        Collections.sort(sortedMetas, VIDEO_COMPARATOR);

        /* Create video managers */
        for (int i = 0; i < sortedMetas.size(); i++) {
            final VideoManager videoManager = new VideoManager(sortedMetas.get(i), i + 1, videoChangedCallback);
            videoManager.setUpdateSuccessfulCommand(new Command() {
                
                @Override
                public void execute() {
                    logger.info("Redrawing video panel");
                    redraw();                
                }
            });
            videoManagers.add(videoManager);
        }

        loadVideosSequentially(videoManagers.listIterator());
    }
    
    /**
     * Update the video properties in this panel for the video metadata provided
     * @param videoMetadata the video metadata that can be used to find the video manager which
     * is then updated with the latest properties.  If null or the video manager can't be found, this method does nothing.
     */
    public void updateVideo(VideoMetadata videoMetadata){
        
        if(videoMetadata == null){
            return;
        }
        
        VideoManager videoManager = getVideoManager(videoMetadata);
        if(videoManager == null){
            return;
        }
        videoManager.setVideoMetadata(videoMetadata);
    }
    
    /**
     * Remove video from the session video panel.
     * @param videoMetadata the video metadata that can be used to find the video manager which
     * is then removed from this panel.  If null or the video manager can't be found, this method does nothing.
     * @return whether the video was removed from the video panel
     */
    public boolean removeVideo(VideoMetadata videoMetadata){
        
        if(videoMetadata == null){
            return false;
        }
        
        VideoManager videoManager = getVideoManager(videoMetadata);
        if(videoManager == null){
            return false;
        }
        boolean removed = videoManagers.remove(videoManager);
        redraw();
        
        return removed;
    }

    /**
     * Add a new video to this panel. This should only be used to dynamically
     * add videos. To initialize this panel use
     * {@link #populate(LogMetadata, Command)}.
     * 
     * @param videoMetadata the video metadata used to add a new video to this
     *        panel. If null, nothing will be added.
     * @param videoLoadedCmd the command to execute when the video is loaded.
     *        Can't be null.
     * @param videoChangedCallback handler for video updates such as changing the video properties or deleting the video.
     * Can't be null. 
     */
    public void addNewVideo(VideoMetadata videoMetadata, final Command videoLoadedCmd, final VideoChangedCallback videoChangedCallback) {
        if (videoMetadata == null) {
            return;
        }

        final VideoManager videoManager = new VideoManager(videoMetadata, videoManagers.size() + 1, videoChangedCallback);
        videoManager.setUpdateSuccessfulCommand(new Command() {
            
            @Override
            public void execute() {
                redraw();                
            }
        });
        videoManagers.add(videoManager);
        loadVideo(videoManager, new Command() {
            @Override
            public void execute() {
                redraw();
                videoLoadedCmd.execute();
            }
        });
    }

    /**
     * Build the grid of videos. The grid should be as follows: <br/>
     * 1. If 1 video: 1x1 grid <br/>
     * 2. If 2 videos: 1x2 grid <br/>
     * 3. If 3-4 videos: 2x2 grid <br/>
     * 4. If > 4 videos: Rx3 grid
     */
    private void createVideoGrid() {
        final int numVideos = videoManagers.size();

        /*- 1 column if only 1 video
         *  2 per row if 2-4 videos
         *  3 per row if > 4 */
        final double numColumns;
        if (numVideos == 1) {
            numColumns = 1;
        } else if (numVideos <= 4) {
            numColumns = 2;
        } else {
            numColumns = 3;
        }

        final Iterator<VideoManager> videoItr = videoManagers.iterator();

        final double rows = Math.ceil(numVideos / numColumns);
        for (double rowIndex = 0; rowIndex < rows; rowIndex++) {
            FlowPanel row = new FlowPanel();
            row.addStyleName(style.videoRow());

            for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
                final FlowPanel videoContainerPanel;
                if (videoItr.hasNext()) {
                    VideoManager videoManager = videoItr.next();
                    videoManager.refresh();
                    videoContainerPanel = videoManager.getVideoContainerPanel();
                } else {
                    /* Stub out an empty slot */
                    videoContainerPanel = new FlowPanel();
                    videoContainerPanel.setHeight("100%");
                    videoContainerPanel.setWidth("100%");
                }

                row.add(videoContainerPanel);
            }

            videoGroupPanel.add(row);
        }

        showVideoGroupPanel();
    }

    /**
     * Redraws the video panel. This clears the panel and rebuilds it.
     */
    public void redraw() {
        videoGroupPanel.clear();
        createVideoGrid();
    }

    /**
     * Loads the videos one-by-one. This is necessary because if they are loaded
     * too quickly, then the videos will never return properly.
     * 
     * @param vidManagerItr the iterator pointing to the next video manager in
     *        the list. Can't be null.
     */
    private void loadVideosSequentially(final ListIterator<VideoManager> vidManagerItr) {
        if (vidManagerItr == null) {
            throw new IllegalArgumentException("The parameter 'vidManagerItr' cannot be null.");
        }

        if (!vidManagerItr.hasNext()) {
            createVideoGrid();
            LoadingDialogProvider.getInstance().loadingComplete(LoadingType.VIDEOS);
            return;
        }

        final VideoManager videoManager = vidManagerItr.next();
        loadVideo(videoManager, new Command() {
            @Override
            public void execute() {
                loadVideosSequentially(vidManagerItr);
            }
        });
    }

    /**
     * Loads the video.
     * 
     * @param videoManager the video manager to load. Can't be null.
     * @param command the command to execute once the video has been loaded.
     *        Can't be null.
     */
    private void loadVideo(final VideoManager videoManager, final Command command) {
        if (videoManager == null) {
            throw new IllegalArgumentException("The parameter 'videoManager' cannot be null.");
        } else if (command == null) {
            throw new IllegalArgumentException("The parameter 'command' cannot be null.");
        }

        final Timer timer = new Timer() {
            
            /* The number of times this timer has been checked*/
            private int numIterations = 0;
            
            @Override
            public void run() {
                
                if(numIterations < VIDEO_LOAD_TIMEOUT/LOAD_TIMEOUT_INTERVAL_DURATION) {
                    
                    /* The total video timeout duration has not passed, so keep iterating */
                    numIterations++;
                    
                } else {
                
                    /* The timeout has passed, so display an error dialog mentioning the timeout */
                    LoadingDialogProvider.getInstance().loadingComplete(LoadingType.VIDEOS);
                    videoManager.resetVideo();
    
                    DetailedException e = new DetailedException(
                            "A session video failed to be loaded within a reasonable amount of time.",
                            "The video file '" + videoManager.getVideoMetadata().getLocation()
                                    + "' failed to be loaded within " + VIDEO_LOAD_TIMEOUT / 1000 + " seconds.",
                            null);
    
                    UiManager.getInstance().displayDetailedErrorDialog("Failed to load the session videos.",
                            new DetailedExceptionSerializedWrapper(e));
                    }
                
                cancel();
            }
        };

        videoManager.loadVideo(true, new LoadVideoCallback() {
            @Override
            public void onLoad() {
                timer.cancel();
                command.execute();
            }

            @Override
            public void onError(VideoError error) {
                
                timer.cancel();
                
                LoadingDialogProvider.getInstance().loadingComplete(LoadingType.VIDEOS);
                videoManager.resetVideo();
            }
        });

        /* 
         * Rather than scheduling a timer for the video load timeout directly, check periodically
         * to see if the required duration has passed in actual JavaScript execution time. This
         * helps delay the timeout when JavaScript execution is blocked, which is needed if
         * execution just happens to be blocked when the video finishes loading since, otherwise,
         * related logic used to grab data from the loaded video cannot execute in time.
         */
        timer.scheduleRepeating(LOAD_TIMEOUT_INTERVAL_DURATION);
    }

    /**
     * Update the videos with the most recent timestamp (in milliseconds).
     * 
     * @param playbackTime the time in milliseconds that represent the current
     *        playback time.
     * @param isPaused true if the session is paused; false if it is live.
     */
    public void updateCurrentTime(long playbackTime, boolean isPaused) {
        for (VideoManager videoManager : videoManagers) {
            if (videoManager.updateSessionTime(playbackTime)) {
                /* Play the video if it is paused but the timeline is running */
                if (!isPaused && videoManager.isPaused()) {
                    videoManager.play();
                }
            }
        }
    }
}
