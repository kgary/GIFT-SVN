/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStartEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStartHandler;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.media.client.Video;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.VideoMetadata;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.event.FullscreenChangeEvent;
import mil.arl.gift.common.gwt.client.event.FullscreenChangeHandler;
import mil.arl.gift.common.gwt.client.event.VideoSeekHandler;
import mil.arl.gift.common.gwt.client.event.VideoSeekedEvent;
import mil.arl.gift.common.gwt.client.event.VideoSeekingEvent;
import mil.arl.gift.common.gwt.client.event.util.CrossBrowserEventUtil;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AddVideoModalDialog;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSetting;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AddVideoModalDialog.VideoChangedCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSetting.VolumeChangeHandler;

/**
 * Manages a game master video object. Including the fully built UI panel,
 * custom controls, and the video player itself.
 * 
 * @author sharrison
 */
public class VideoManager implements VideoSeekHandler, FullscreenChangeHandler, VolumeChangeHandler {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(VideoManager.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static VideoManagerUiBinder uiBinder = GWT.create(VideoManagerUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface VideoManagerUiBinder extends UiBinder<Widget, VideoManager> {
    }

    /** An HTML line break */
    private static final String HTML_LINE_BREAK = "<br/>";
    
    /**
     * The state of the video.
     * 
     * @author sharrison
     */
    public enum VideoState {
        /** Video is enabled and within the time-span */
        ACTIVE(""),
        /** Video is disabled via game master settings */
        DISABLED("Disabled"),
        /** Video has ended */
        ENDED("Ended"),
        /** Video is currently seeking */
        SEEKING("Seeking..."),
        /** Video has not started yet */
        WAITING("Waiting...");

        /** The display label for the state */
        private final String label;

        /** The states that cannot coexist with this state */
        private Set<VideoState> conflicts;

        /**
         * Constructor
         * 
         * @param label the display label
         */
        private VideoState(String label) {
            this.label = label;
        }

        /**
         * Retrieve the states that cannot coexist with this state.
         * 
         * @return the collection of conflicting states.
         */
        private Set<VideoState> getConflictingStates() {
            if (conflicts != null) {
                return conflicts;
            }

            conflicts = new HashSet<>();
            switch (this) {
            case ACTIVE:
                conflicts.add(ENDED);
                conflicts.add(WAITING);
                break;
            case ENDED:
                conflicts.add(ACTIVE);
                conflicts.add(WAITING);
                break;
            case WAITING:
                conflicts.add(ACTIVE);
                conflicts.add(ENDED);
                break;
            case DISABLED: /* intentional drop-through */
            case SEEKING: /* intentional drop-through */
            default:
                /* No conflicts */
            }

            return conflicts;
        }
    }
    
    /**
     * An enumeration of video formats that are commonly supported across most browsers. Unfortunately,
     * there isn't a way to simply ask a browser for <i>all</i> the video formats it supports, so instead, we
     * keep a common collection to use across all browsers and simply ask the browser which formats from
     * the collection it supports.
     * <br/><br/>
     * This enum is used to suggest supported video formats to the user if they attempt to load a video
     * that is not supported by their browser.
     * 
     * @author nroberts
     */
    private enum VideoWebFormat{
        
        /** Advanced Video Coding (AVC) H.264 format for MP4 files */
        H264("AVC (H.264)", "video/mp4", "avc1.42E01E"),
        
        /** Theora format for OGG files */
        OGG("OGG", "video/ogg", "theora"),
        
        /** Standard WebM format for WebM files*/
        WEBM("WebM", "video/webm", "vp8", "vorbis"),
        
        /** VP9 format for WebM files */
        VP9("VP9", "video/webm", "vp9"),
        
        /** HTTP Live Streaming (HLS) format used for live streams of MP4 data */
        HLS("HLS", "application/x-mpegURL", "avc1.42E01E");
        
        /** The display name for the video format */
        private String displayName;
        
        /** The MIME type associated with the video format (can be the same for some formats) */
        private String mimeType;
        
        /** The video codecs associated with the video format (can be the same for some formats) */
        private String[] codecs;

        /**
         * Creates a new browser video format with the given display name, MIME type, and codecs.
         * 
         * @param displayName the display name for the video format. Cannot be null.
         * @param mimeType the MIME type associated with the video format. Cannot be null.
         * @param codecs the video codecs associated with the video format. Cannot be null or empty.
         */
        private VideoWebFormat(String displayName, String mimeType, String... codecs) {
            
            if(displayName == null) {
                throw new IllegalArgumentException("The display name for a video format cannot be null");
            }
            
            if(mimeType == null) {
                throw new IllegalArgumentException("The MIME type for a video format cannot be null");
            }
            
            if(codecs == null || codecs.length == 0) {
                throw new IllegalArgumentException("There must be at least one codec for each video format");
            }
            
            this.displayName = displayName;
            this.mimeType = mimeType;
            this.codecs = codecs;
        }

        /**
         * Gets this format's display name
         * 
         * @return the display name for the format. Will not be null.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets the MIME type associated with this format. This MIME type can also be used by other formats
         * and should not be used to uniquely identify a format.
         * 
         * @return the MIME type for the format . Will not be null.
         */
        public String getMimeType() {
            return mimeType;
        }

        /**
         * Gets the codecs associated with this format. These codecs can also be used by other formats
         * and should not be used to uniquely identify a format.
         * 
         * @return the codecs for the format . Will not be null or empty.
         */
        public String[] getCodecs() {
            return codecs;
        }
        
        /**
         * Gets the full type string representing this video format. This contains both the MIME type
         * and codecs used by the format and is generally used by browsers to fully identify a specific 
         * format, particularly for operations such as 
         * <a href='https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement/canPlayType'>
         * canPlayType()
         * </a>
         * 
         * @return the full type string. Will not be null or empty.
         */
        public String getTypeString() {
            
            StringBuilder typeString = new StringBuilder(getMimeType())
                    .append("; codecs=\"");
            
            boolean first = true;
            for(String codec : getCodecs()) {
                
                if(first) {
                    first = false;
                } else {
                    typeString.append(", ");
                }
                
                typeString.append(codec);
            }
            
            typeString.append("\"");
            
            return typeString.toString();
        }
    }

    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * The CSS applied when the volume slider is being used.
         *
         * @return the CSS style name
         */
        String volumeSliderActive();

        /**
         * The CSS applied when a video is in fullscreen.
         *
         * @return the CSS style name
         */
        String fullscreenVideo();
    }

    /** The style from the ui.xml */
    @UiField
    protected Style style;

    /** The parent panel for the {@link #videoPlayer} */
    @UiField
    protected FlowPanel videoContainerPanel;

    /** A panel used to block user input and show a message to the user */
    @UiField
    protected BlockerPanel blockerPane;

    /** The label for the {@link #blockerPane} */
    @UiField
    protected Label blockerLabel;

    /** The icon for the {@link #blockerPane} */
    @UiField
    protected Icon blockerIcon;

    /** The label showing the custom channel name or the channel index */
    @UiField
    protected Label channelLabel;

    /**
     * The panel containing the {@link #leftSideControls} and
     * {@link #rightSideControls}
     */
    @UiField
    protected FlowPanel controlPanel;

    /** The panel containing the left side controls */
    @UiField
    protected FlowPanel leftSideControls;

    /** The tooltip for the {@link #volumePanel} */
    @UiField
    protected Tooltip volumePanelTooltip;

    /** The panel containing the volume controls */
    @UiField
    protected FlowPanel volumePanel;

    /** The panel containing the right side controls */
    @UiField
    protected FlowPanel rightSideControls;

    /** The tooltip for the {@link #muteButton} */
    @UiField
    protected Tooltip muteButtonTooltip;

    /** The button used to mute/unmute the video audio */
    @UiField
    protected Button muteButton;

    /** The slider to change the volume */
    @UiField
    protected Slider volumeSlider;

    /** The tooltip for the {@link #fullscreenButton} */
    @UiField
    protected Tooltip fullscreenButtonTooltip;

    /** The button used to make the video fullscreen or not */
    @UiField
    protected Button fullscreenButton;

    /** The video player */
    @UiField(provided = true)
    protected Video videoPlayer = Video.createIfSupported();

    /** The tooltip for the {@link #videoPlayer} */
    private final Tooltip videoPlayerTooltip;

    /** The video metadata */
    private VideoMetadata videoMetadata;

    /** The handler used to indicate when the video has been loaded */
    private LoadVideoCallback loadedVideoCommand;

    /** The unique 1-based channel index for this video */
    private final int channelIndex;

    /**
     * The end time of the video. Calculated by adding the start time with the
     * duration and then subtracting the offset.
     */
    private Date endTime;

    /**
     * The volume for the video between 0.0 (silent) and 1.0 (loudest) inclusive
     */
    private double previousVolume;

    /** The last known timestamp that was used for this video */
    private Long lastKnownTime;

    /** The timer to keep polling for the video metadata to be loaded */
    private final Timer metadataTimer = new Timer() {
        @Override
        public void run() {
            getEndTime();
        }
    };

    /** The set of video states that this video is currently in */
    private final Set<VideoState> videoStates = new HashSet<>();
    
    /** The detected MIME type of the loaded video. Used in error reporting. */
    private String videoMIMEType = null;
    
    /** The location of the loaded video. Used in error reporting. */
    private String videoLocation = null;
    
    /** command to execute upon successful update of a video.  can be null if the video manage is shown in the add video dialog. */
    private Command updateSuccessfulCommand = null;

    /** Whether clicking on the video should begin editing it */
    private boolean editOnClick = true;

    /**
     * Constructor
     * 
     * @param videoMetadata the video metadata. Can't be null. Must contain
     *        start time.
     * @param channelIndex the unique 1-based channel index for this video among
     *        all the other videos.
     * @param videoChangedCallback used to notify the caller loading the video of changes made to the video properties.
     * Can be null if this video is in the process of being uploaded/added for the first time.
     */
    public VideoManager(final VideoMetadata videoMetadata, int channelIndex, final VideoChangedCallback videoChangedCallback) {
        if (videoMetadata == null) {
            throw new IllegalArgumentException("The parameter 'videoMetadata' cannot be null.");
        } else if (channelIndex <= 0) {
            throw new IllegalArgumentException("The parameter 'videoIndex' must be a positive number.");
        }

        /* The createAndBindUi method will create and bind the ui.xml elements
         * for this class */
        uiBinder.createAndBindUi(this);
        
        logger.info("Creating video manager for "+videoMetadata);
        
        // use this rather than setVideoMetadata method since that method calls refresh()
        this.videoMetadata = videoMetadata;

        this.channelIndex = channelIndex;
        previousVolume = videoPlayer.getVolume();

        videoPlayer.setPreload(MediaElement.PRELOAD_AUTO);

        /* Add a listener for video seek events */
        videoPlayer.addDomHandler(this, VideoSeekedEvent.getType());
        videoPlayer.addDomHandler(this, VideoSeekingEvent.getType());
        
        // show the edit video metadata dialog when clicking on the video
        videoContainerPanel.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {  
                
                if(!editOnClick) {
                    
                    /* Editing on click is disabled, so don't open the editor */
                    return;
                }
                
                Element fullscreenElement = JsniUtility.getFullscreenElement();
                if(fullscreenElement != null && fullscreenElement.equals(videoContainerPanel.getElement())){
                    // the video player is in fullscreen, dont show metadata dialog when video is clicked on in this mode
                    return;
                }
                logger.info("Loading video property panel for editing");
                AddVideoModalDialog editMetadataDialog = new AddVideoModalDialog(getVideoMetadata(), videoChangedCallback);
                editMetadataDialog.show();  
            }
        }, ClickEvent.getType());
        
        /* Need to register an error listener to handle when the browser throws an 
         * error because the video could not be processed.
         * 
         * Oddly, we can't just add an ErrorHandler here using addDomHandler, since
         * that never seems to be hit. */
        registerErrorListener(videoPlayer.getElement());

        /* Default video to a 'waiting' state */
        updateState(VideoState.WAITING);
        
        /* update the video player label */
        channelLabel.setText(StringUtils.isNotBlank(videoMetadata.getTitle()) ? videoMetadata.getTitle()
                : Integer.toString(channelIndex));

        /* Add video tooltip */
        videoPlayerTooltip = ManagedTooltip.attachTooltip(videoContainerPanel, buildVideoTooltip());
        videoPlayerTooltip.setPlacement(Placement.BOTTOM);

        /* Hide video tooltip when over controls; restore video tooltip when
         * leaving controls */
        MouseOverHandler hideVideoTooltipHandler = new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                event.stopPropagation();
                videoPlayerTooltip.hide();
            }
        };
        MouseOutHandler showVideoTooltipHandler = new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                videoPlayerTooltip.show();
            }
        };
        leftSideControls.addDomHandler(hideVideoTooltipHandler, MouseOverEvent.getType());
        leftSideControls.addDomHandler(showVideoTooltipHandler, MouseOutEvent.getType());
        rightSideControls.addDomHandler(hideVideoTooltipHandler, MouseOverEvent.getType());
        rightSideControls.addDomHandler(showVideoTooltipHandler, MouseOutEvent.getType());

        /** The mute button */
        muteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                double newVolume;
                if (videoPlayer.isMuted()) {
                    /* Unmute to previous volume */
                    newVolume = previousVolume;
                } else {
                    /* Mute; save current volume */
                    previousVolume = volumeSlider.getValue();
                    newVolume = volumeSlider.getMin();
                }

                volumeSlider.setValue(newVolume, false);
                updateMuteButton();
                
                // don't want videoContainerPanel click handler to fire
                event.stopPropagation();
            }
        });

        volumeSlider.setValue(previousVolume);
        volumeSlider.addSlideStartHandler(new SlideStartHandler<Double>() {
            @Override
            public void onSlideStart(SlideStartEvent<Double> event) {
                /* If dragging, don't hide the slider when the mouse moves
                 * away */
                volumeSlider.addStyleName(style.volumeSliderActive());
            }
        });
        volumeSlider.addSlideStopHandler(new SlideStopHandler<Double>() {
            @Override
            public void onSlideStop(SlideStopEvent<Double> event) {
                volumeSlider.removeStyleName(style.volumeSliderActive());

                /* If dragged to the minimum volume; the default previous is the
                 * max volume. This prevents 'unmute' returning to 1 step above
                 * the minimum. */
                if (Double.compare(event.getValue(), volumeSlider.getMin()) == 0) {
                    previousVolume = volumeSlider.getMax();
                } else {
                    previousVolume = event.getValue();
                }
                updateMuteButton();
            }
        });

        /* The volume slider drops its attributes when it is detached; so update
         * it to be the same as the mute button when re-attached */
        volumeSlider.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    volumeSlider.setEnabled(muteButton.isEnabled());
                    volumeSlider.setValue(videoPlayer.getVolume());
                }
            }
        });
        
        // prevent the videoContainerPanel click handler from showing the video metadata dialog
        // when using the volume slider
        volumePanel.addHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                
            }
        }, ClickEvent.getType());

        /** The fullscreen button */
        fullscreenButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (JsniUtility.getFullscreenElement() == null) {
                    /* Show the video in full screen mode */
                    JsniUtility.requestFullscreen(videoContainerPanel.getElement());
                } else {
                    /* Exit full screen mode */
                    JsniUtility.exitFullscreen();
                }
                
                // don't want videoContainerPanel click handler to fire
                event.stopPropagation();
            }
        });

        /* Add a listener for fullscreen changes on the video container. This is
         * in case the user enters/exits fullscreen in a way other than the
         * control buttons. */
        CrossBrowserEventUtil.addFullscreenChangeListener(videoContainerPanel, this);
        
        /* Add a listener for when the setting for the video audio volume is changed */
        Dashboard.VolumeSettings.VIDEO_AUDIO.getSetting().addManagedHandler(this);

        /* The onSeeking/onSeeked methods don't work if the video is not
         * attached (e.g. seeking when the video panel is hidden); so when we
         * are re-attaching, check to see if the videos are currently
         * seeking. */
        videoContainerPanel.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    if (videoPlayer.isSeeking()) {
                        updateState(VideoState.SEEKING);
                    } else {
                        removeState(VideoState.SEEKING);
                    }
                }
            }
        });
        
        logger.info("end constructor");
    }
    
    /**
     * Set the current video metadata instance which can change upon saving an update.
     * NOTE: this will refresh the video channel label and tooltip.  Both of these elements must
     * be instantiated before calling this method.
     * @param videoMetadata the current metadata.  can't be null.
     */
    public void setVideoMetadata(VideoMetadata videoMetadata){
        if(videoMetadata == null){
            return;
        }
        logger.info("Changing video metadata from\n"+this.videoMetadata+" to\n"+videoMetadata);
        this.videoMetadata = videoMetadata;
        
        refresh();
    }
    
    /**
     * Refresh the UI elements for this video that could have changed with the latest video property changes.
     */
    public void refresh(){
        
        if(videoMetadata != null){        
            /* update the video player label */
            channelLabel.setText(StringUtils.isNotBlank(videoMetadata.getTitle()) ? videoMetadata.getTitle()
                    : Integer.toString(channelIndex));
        }

        if(videoPlayerTooltip != null){
            /* update video tooltip */
            videoPlayerTooltip.setHtml(buildVideoTooltip());
        }
    }
    
    /**
     * Set the command to execute when a video metadata is successfully updated.
     * @param updateSuccessfulCommand the command to execute once the server responds to the 
     * clients request to update a video metadata on disk.  Can be null if not needed, e.g. loadVideo logic.
     */
    public void setUpdateSuccessfulCommand(Command updateSuccessfulCommand){
        this.updateSuccessfulCommand = updateSuccessfulCommand;
    }
    
    /**
     * Registers an event listener on the given video element that will handle
     * errors thrown by it using {@link #handleVideoError(VideoError)}. This
     * should only be called once on a given video element
     * 
     * @param video the video element to add the event listener to.
     */
    private final native void registerErrorListener(Element video)/*-{
        
        var that = this;
        
        video.addEventListener('error', function(event) {
            var error = event;
        
            // Chrome v60
            if (event.path && event.path[0]) {
              error = event.path[0].error;
            }
        
            // Firefox v55
            if (event.originalTarget) {
              error = error.originalTarget.error;
            }
        
            // Report the error to any handlers
            that.@mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager::handleVideoError(Lmil/arl/gift/tools/dashboard/client/bootstrap/gamemaster/aar/VideoError;)(error)
        
          }, true);
    }-*/;

    /**
     * Loads the video.
     * 
     * @param useVideoServlet true to prepend the video file servlet to the
     *        video location; false to use the metadata location directly.
     * @param loadedVideoCommand the command to execute once the video has
     *        been loaded. Can't be null.
     */
    public void loadVideo(boolean useVideoServlet, LoadVideoCallback loadedVideoCommand) {
        if (loadedVideoCommand == null) {
            throw new IllegalArgumentException("The parameter 'loadedVideoCommand' cannot be null.");
        } else if (this.loadedVideoCommand != null) {
            /* Already loading a video */
            return;
        }

        this.loadedVideoCommand = loadedVideoCommand;
        
        videoLocation = getVideoURL(videoMetadata, useVideoServlet);

        loadVideoViaBlob(videoPlayer.getVideoElement(), videoLocation);

        /* Poll for the metadata */
        metadataTimer.scheduleRepeating(500);
    }
    
    /**
     * Returns the URL to the video so that it could be shown in a panel.
     * @param videoMetadata contains metadata about the video including its location on the server.  Can't be null.
     * @param useVideoServlet true to prepend the video file servlet to the
     *        video location; false to use the metadata location directly. 
     * @return the URL of the video file on the servlet 
     */
    public static String getVideoURL(VideoMetadata videoMetadata, boolean useVideoServlet){
        
        final String location;
        if (useVideoServlet) {
            location =  Dashboard.getInstance().getServerProperties().getSessionOutputServerPath() + videoMetadata.getLocation();
        } else {
            location = videoMetadata.getLocation();
        }
        
        return URL.encode(location);
    }

    /**
     * Natively loads the video file at the given URL into the given element by
     * storing the video data into a Blob and then feeding that data into the
     * video element. <br/>
     * <br/>
     * This is an alternative way of loading a video without relying on the
     * browser's default loading behavior (which is invoked when setting the
     * video source to a URL). Using this alternative loading process can be
     * helpful for avoiding browser-specific differences in loading behavior
     * that can sometimes prevent videos from loading, since it gives a greater
     * degree of control over how the video data is loaded into the client.
     * 
     * @param video the HTML &lt;video&gt; element to load the given URL into.
     *        Cannot be null.
     * @param url the URL of the video to load. Cannot be null.
     */
    private native void loadVideoViaBlob(Element video, String url)/*-{
        
        var that = this;
        
		//request the video file data at the given URL
		fetch(url).then(function(response) {

			//create a Blob from the given data to control how it is loaded
			response.blob().then(function(blob) {
			    
			    if(blob != null){
			        
			        // save the MIME type of the loaded blob so it can be reported in an error dialog if there is a problem
			        // with the format of the loaded video
			        that.@mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager::videoMIMEType = blob.type;
			    }
			    
				//obtain a URL pointing to the Blob that can be used by the video element
				var blobUrl = URL.createObjectURL(blob);
				
				//load the Blob's data into the video element using the appropriate URL
				video.src = blobUrl;
				video.load();
			});
		});
    }-*/;

    /**
     * The duration of the video (in seconds).
     * 
     * @return the duration of the video. Can be null if the video element
     *         metadata hasn't been loaded yet.
     */
    public Double getDuration() {
        return Double.compare(videoPlayer.getDuration(), Double.NaN) == 0 ? null : videoPlayer.getDuration();
    }

    /**
     * Get the end time of the video. This is the start time plus the duration
     * minus the offset.
     * 
     * @return the end time of the video. Will return null if the start time or
     *         duration does not exist.
     */
    public Date getEndTime() {
        if (endTime == null) {
            final Double duration = getDuration();
            if (duration == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("The metadata has not been loaded yet for video '" + videoMetadata + "'.");
                }
                return null;
            } else if (logger.isLoggable(Level.FINE)) {
                logger.fine("The metadata (duration) is loaded for '" + videoMetadata + "'.");
            }

            long endTimeMs = videoMetadata.getStartTime().getTime() + (long) (duration * 1000);
            endTime = new Date(endTimeMs);

            /* Send trigger that the metadata has been loaded */
            if (loadedVideoCommand != null) {
                /* Reset before executing the command in case they push a new
                 * video state now that the metadata is loaded */
                resetVideo();
                loadedVideoCommand.onLoad();
                loadedVideoCommand = null;
            }

            /* Update the tooltip now that we have the end time */
            videoPlayerTooltip.setHtml(buildVideoTooltip());
        }

        return endTime;
    }

    /**
     * Get the video metadata.
     * 
     * @return the video metadata. Won't be null.
     */
    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    /**
     * Get the channel label for the video.
     * 
     * @return the channel label.
     */
    public String getChannelLabel() {
        return channelLabel.getText();
    }

    /**
     * Get the panel containing the video.
     * 
     * @return the video container panel. Won't be null.
     */
    public FlowPanel getVideoContainerPanel() {
        return videoContainerPanel;
    }

    /**
     * Get the video channel index.
     * 
     * @return the video channel index.
     */
    public int getChannelIndex() {
        return channelIndex;
    }

    /**
     * Provide this manager the latest time (in milliseconds). The manager will
     * update its {@link VideoState} accordingly.
     * 
     * @param dateMillis the time in milliseconds.
     * @return true if the time is within the span of the video.
     */
    public boolean updateSessionTime(long dateMillis) {
        lastKnownTime = dateMillis;
        final VideoState state = getStateFromTime(dateMillis);
        updateState(state);

        return state == VideoState.ACTIVE;
    }

    /**
     * Checks if the given time (in milliseconds) is between the start and end
     * time of the video.
     * 
     * @param dateMillis the time in milliseconds.
     * @return true if the time is within the span of the video.
     */
    private VideoState getStateFromTime(long dateMillis) {
        final Date endTime = getEndTime();

        /* Can't calculate the video time frame without the end time */
        if (endTime == null) {
            return VideoState.WAITING;
        }

        if (dateMillis < videoMetadata.getStartTimeWithOffset()) {
            return VideoState.WAITING;
        } else if (dateMillis > endTime.getTime()) {
            return VideoState.ENDED;
        } else {
            return VideoState.ACTIVE;
        }
    }

    /**
     * Stops the video and resets it back to the starting position.
     */
    public void resetVideo() {
        if (metadataTimer.isRunning()) {
            metadataTimer.cancel();
        }

        updateState(VideoState.WAITING);
    }

    /**
     * Seeks to the relative time in the video. Calculates this time by using
     * the difference of the given time and the video start time.
     * 
     * @param dateMillis the time in milliseconds.
     */
    public void seek(long dateMillis) {
        final boolean isInTimespan = updateSessionTime(dateMillis);
        if (!isInTimespan) {
            return;
        }

        /* Find the number of seconds into the video to seek */
        long timeDiff = dateMillis - videoMetadata.getStartTime().getTime();
        double relativeSeconds = timeDiff / (double) 1000;

        /* Get the number of seconds to offset the start of the video */
        updateState(VideoState.SEEKING);
        videoPlayer.getVideoElement().setCurrentTime(relativeSeconds);
    }

    /**
     * Updates the video's volume controls to match the current volume setting for
     * video audio
     */
    public void  handleVolumeSettingChange() {
        
        boolean forceMute = Dashboard.VolumeSettings.VIDEO_AUDIO.getSetting().isMuted();
        double maxVolume = Dashboard.VolumeSettings.VIDEO_AUDIO.getSetting().getVolume();
        
        if (forceMute) {
            /* Only update values if going from unforced to forced */
            if (muteButton.isEnabled()) {
                if (Double.compare(volumeSlider.getValue(), volumeSlider.getMin()) != 0) {
                    previousVolume = volumeSlider.getValue();
                    volumeSlider.setValue(volumeSlider.getMin());
                }
            }
        } else {
            volumeSlider.setValue(Math.min(previousVolume, maxVolume));
            volumePanelTooltip.setTitle(null);
        }

        updateMuteButton();
    }

    /**
     * Update the state for this video. This will remove any conflicting states.
     * 
     * @param state the new state of the video. If null, nothing will happen.
     */
    public void updateState(VideoState state) {
        if (state == null) {
            return;
        }

        boolean added = videoStates.add(state);
        if (added) {
            /*- Actions to perform on new states:
             * Waiting: pause video and set the video to the beginning.
             * Ended: pause video and set the video to the end.
             * Disabled: pause video where it is.
             */
            if (state == VideoState.WAITING) {
                pause();
                videoPlayer.getVideoElement().setCurrentTime(videoMetadata.getOffset());
            } else if (state == VideoState.ENDED) {
                pause();
                videoPlayer.getVideoElement().setCurrentTime(getDuration());
            } else if (state == VideoState.DISABLED) {
                pause();
            }

            /* Remove contradictory states (e.g. if setting to 'ended', remove
             * 'waiting' or 'active') */
            videoStates.removeAll(state.getConflictingStates());
            updateBlockerPane();
        }
    }

    /**
     * Remove a state from the video.
     * 
     * @param state the state of the video to remove. If null, nothing will
     *        happen.
     */
    public void removeState(VideoState state) {
        if (state == null) {
            return;
        }

        boolean removed = videoStates.remove(state);
        if (removed) {
            /*- Actions to perform when removing states:
             * Disabled: seek to the location in the video based on the previous timestamp.
             */
            if (state == VideoState.DISABLED && lastKnownTime != null) {
                seek(lastKnownTime);
            }
            updateBlockerPane();
        }
    }

    /**
     * Update the {@link #blockerPane} with the current {@link #videoStates}.
     */
    private void updateBlockerPane() {
        /* Undo any icon attributes that are set below (except type) */
        blockerIcon.setSpin(false);
        blockerIcon.setVisible(false);

        /*- Hierarchy of video states:
         * 1. Disabled
         * 2. Seeking
         * 3. Waiting | Active | Ended (there can only be 1 of these at a time)
         */
        final boolean doBlock;
        if (videoStates.contains(VideoState.DISABLED)) {
            blockerLabel.setText(VideoState.DISABLED.label);
            doBlock = true;
        } else if (videoStates.contains(VideoState.SEEKING)) {
            blockerLabel.setText(VideoState.SEEKING.label);
            blockerIcon.setType(IconType.SPINNER);
            blockerIcon.setSpin(true);
            blockerIcon.setVisible(true);
            doBlock = true;
        } else if (videoStates.contains(VideoState.WAITING)) {
            blockerLabel.setText(VideoState.WAITING.label);
            doBlock = true;
        } else if (videoStates.contains(VideoState.ENDED)) {
            blockerLabel.setText(VideoState.ENDED.label);
            doBlock = true;
        } else {
            doBlock = false;
        }

        if (doBlock) {
            blockerPane.block();
        } else {
            blockerPane.unblock();
        }
    }

    /**
     * Set whether or not the channel label should be visible on the video.
     * 
     * @param visible true to make visible; false otherwise.
     */
    public void setChannelLabelVisibility(boolean visible) {
        channelLabel.setVisible(visible);
    }

    /**
     * Use the default browser controls instead of the build-in ones for this
     * widget.
     * 
     * @param useDefaultControls true to use the default video controls; false
     *        to use the ones built into this widget.
     */
    public void setUseDefaultControls(boolean useDefaultControls) {
        controlPanel.setVisible(!useDefaultControls);
        videoPlayer.setControls(useDefaultControls);
    }

    /**
     * Play the video. Will do nothing if the video is not active or if it is
     * disabled.
     */
    public void play() {
        if (!videoStates.contains(VideoState.ACTIVE) || videoStates.contains(VideoState.DISABLED)) {
            return;
        }

        videoPlayer.play();
    }

    /**
     * Pause the video.
     */
    public void pause() {
        videoPlayer.pause();
    }

    /**
     * Checks if the video is paused.
     * 
     * @return true if the video is paused; false if it is not.
     */
    public boolean isPaused() {
        return videoPlayer.isPaused();
    }

    /** Updates the mute button icon and labels */
    private void updateMuteButton() {
        boolean isMuted = Double.compare(volumeSlider.getValue(), volumeSlider.getMin()) == 0;

        videoPlayer.setMuted(isMuted);
        videoPlayer.setVolume(volumeSlider.getValue());
        if (isMuted) {
            muteButton.setIcon(IconType.VOLUME_OFF);
        } else {
            muteButton.setIcon(volumeSlider.getValue() < .5 ? IconType.VOLUME_DOWN : IconType.VOLUME_UP);
        }

        muteButtonTooltip.setTitle(isMuted ? "Unmute Video" : "Mute Video");
    }

    /**
     * Handles updating the {@link #fullscreenButton} and
     * {@link #fullscreenButtonTooltip} based on the current fullscreen state.
     */
    @Override
    public void onFullscreenChange(FullscreenChangeEvent event) {
        final boolean isFullscreen = JsniUtility.getFullscreenElement() != null;
        fullscreenButton.setIcon(isFullscreen ? IconType.COMPRESS : IconType.ARROWS_ALT);
        fullscreenButtonTooltip.setTitle(isFullscreen ? "Exit Fullscreen" : "Enter Fullscreen");

        /* Hide the left divider bar if in fullscreen */
        if (isFullscreen) {
            videoContainerPanel.addStyleName(style.fullscreenVideo());
        } else {
            videoContainerPanel.removeStyleName(style.fullscreenVideo());
        }
    }

    @Override
    public void onSeeked(VideoSeekedEvent event) {
        removeState(VideoState.SEEKING);
    }

    @Override
    public void onSeeking(VideoSeekingEvent event) {
        /* Do nothing. Handled by the seek(long) method. */
    }
    
    /**
     * Sets whether clicking on the video should begin editing it. This can be used
     * to let the user interact with an uneditable video when doing so makes sense,
     * such as when the editor itself needs to show the video.
     * 
     * @param editOnClick whether clicking on the video should begin editing it.
     * Defaults to true.
     */
    public void setEditOnClick(boolean editOnClick) {
        this.editOnClick  = editOnClick;
    }

    /**
     * Displays the video metadata in an easy to read HTML format. Only displays
     * data that exists. This is very useful for displaying the metadata to the
     * user.
     * 
     * @return the HTML representing the populated metadata fields.
     */
    private SafeHtml buildVideoTooltip() {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        if(videoMetadata == null){
            return sb.toSafeHtml();
        }
        
        sb.appendHtmlConstant("<div style='text-align: left'>");

        // don't need day of year since that should be shown elsewhere in game master
        DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss z");
        sb.append(bold("Start: "))
                .appendHtmlConstant(timeFormat.format(videoMetadata.getStartTime()));

        if (getDuration() != null) {
            final int duration = (int) Math.floor(getDuration());
            final String durationStr = FormattedTimeBox.getDisplayText(duration, true);
            sb.appendHtmlConstant(HTML_LINE_BREAK).append(bold("Duration: ")).appendHtmlConstant(durationStr);
        }

        if (videoMetadata.getOffset() != 0) {
            sb.appendHtmlConstant(HTML_LINE_BREAK).append(bold("Offset: ")).append(videoMetadata.getOffset())
                    .appendHtmlConstant("s");
        }

        sb.appendHtmlConstant("</div>");
        return sb.toSafeHtml();
    }

    @Override
    public void onVolumeChange(VolumeSetting setting) {
        if(Dashboard.VolumeSettings.VIDEO_AUDIO.getSetting().equals(setting)) {
            handleVolumeSettingChange();
        }
    }
    
    /**
     * Handles an error that was thrown from the video player while processing video data. If this method is hit,
     * then it is likely that the browser was unable to process the video file so that it could be played.
     * 
     * @param error the error that was thrown. Cannot be null.
     */
    private void handleVideoError(VideoError error) {
        
        /* Check what video formats the browser supports so we can suggest video formats */
        List<VideoWebFormat> supportedFormats = new ArrayList<>();
        for(VideoWebFormat format : VideoWebFormat.values()) {
            
            if("probably".equals(videoPlayer.canPlayType(format.getTypeString()))) {
                supportedFormats.add(format);
            }
        }
        
        /* Build a helpful error message that tells the user that their video likely was not in a format supported by
         * their browser and recommending them with alternative video formats that might work instead. */
        StringBuilder errorText = new StringBuilder()
                .append("The following video could not be fully loaded:<br/><b>")
                .append(videoLocation)
                .append("</b><br/>Is that the correct path to the file?<br/>")
                .append("If not, change the 'location' value to be the correct relative path in the vmeta.xml file:<b><br/>")
                .append(videoMetadata.getFileName())
                .append("</b><br/>Otherwise, this may indicate that your browser does not support ")
                .append("the video format used by this video, which can happen if the video uses a type of video codec that your ")
                .append("browser does not have the ability to process.")
                .append("<br/><br/>Supported Formats<ul>");
        
        for(VideoWebFormat format : supportedFormats) {
            errorText.append("<li>")
                .append(format.getDisplayName())
                .append(": ")
                .append(format.getTypeString())
                .append("</li>");
        }
        
        errorText.append("</ul>Loaded Video Format<ul><li>")
                .append(videoMIMEType)
                .append("</li></ul>")
                .append("To allow this video to play in your browser, you will likely need to convert your video file to use ")
                .append("one of the supported format and codec types listed above.")
                .append("<br/><br/>As of 04/2021, the AVC format (also known as H.264) is generally supported across most modern ")
                .append("browsers, making it a good choice for MP4 video files.<br/><br/>")
                .append("<b>The Video panel may not show any other session videos, even if properly loaded, because of this error.</b>");
        
        DetailedException e = new DetailedException(
                errorText.toString(),
                error.message(),
                null);

        /* Display the error message */
        UiManager.getInstance().displayDetailedErrorDialog("Failed to Load Video",
                new DetailedExceptionSerializedWrapper(e));
        
        logger.severe("Encountered error while processing video after loading: " + (error != null ? error.message() : error));
        
        metadataTimer.cancel();
        loadedVideoCommand.onError(error);
    }
    
    /**
     * A callback used to handle when a video manager finishes loading a video and has access to 
     * that video's metadata
     * 
     * @author nroberts
     */
    public interface LoadVideoCallback{
        
        /**
         * Handles when the video has finished loading and has access to the video's metadata
         */
        public void onLoad();
        
        /**
         * Handles when an error was thrown while processing the video.
         * 
         * @param error the error that was thrown cannot be null.
         */
        public void onError(VideoError error);
    }
}