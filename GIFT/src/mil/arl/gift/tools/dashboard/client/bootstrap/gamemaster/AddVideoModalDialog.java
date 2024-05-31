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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.aar.VideoMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.BsFileSelectionWidget;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.DeleteCallback;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.MIMEType;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.UploadCallback;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoError;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager.LoadVideoCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar.VideoManager.VideoState;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;

/**
 * Widget that presents a dialog for the user to add a new video into the past
 * session. Generates a video metadata.
 * 
 * @author sharrison
 */
public class AddVideoModalDialog extends Composite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AddVideoModalDialog.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static AddVideoModalDialogUiBinder uiBinder = GWT.create(AddVideoModalDialogUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AddVideoModalDialogUiBinder extends UiBinder<Widget, AddVideoModalDialog> {
    }

    /** The path to the file servlet for videos */
    private static final String VIDEO_FILE_SERVLET_PATH = "/dashboard/recorder/output/domainSessions/";

    /** 10 second timeout to load the video */
    private static final int VIDEO_TIMEOUT = 10000;
    
    /** 
     * The interval of time that should be allowed to pass between each check to see if 
     * the video timeout has fully elapsed
     */
    private static final int LOAD_TIMEOUT_INTERVAL_DURATION = 500;

    /** The modal dialog for selecting a video file */
    @UiField
    protected Modal videoFileSelectionDialog;

    /** The widget used to select the video file */
    @UiField(provided = true)
    protected BsFileSelectionWidget fileSelection = new BsFileSelectionWidget(VIDEO_FILE_SERVLET_PATH);

    /** The cancel button for the {@link #videoFileSelectionDialog} */
    @UiField
    protected Button selectionCancelButton;

    /**
     * The back button for the {@link #videoFileSelectionDialog} to return to
     * the {@link #videoMetadataDialog}
     */
    @UiField
    protected Button selectionBackButton;

    /** The modal dialog to author the video metadata */
    @UiField
    protected Modal videoMetadataDialog;

    /** The panel containing the video element */
    @UiField
    protected SimplePanel videoPanel;

    /** The label used to show the author the selected file */
    @UiField
    protected HTML filenameLabel;

    /** The button used to change the selected file */
    @UiField
    protected Button changeFileBtn;

    /** The input box for the video display name (e.g. channel label) */
    @UiField
    protected TextBox displayName;

    /** The error label when the display name is missing */
    @UiField
    protected HTML displayNameError;

    /**
     * The input for the time to start playing the video in the session timeline
     */
    @UiField
    protected FormattedTimeBox playAtTime;

    /**
     * The warning label when the play at time is before or after the video
     * start time + duration (respectively)
     */
    @UiField
    protected HTML playAtWarning;

    /** The error label when the play at time is unknown */
    @UiField
    protected HTML playAtError;

    /**
     * The offset time of the video. Determines how much of the start of the
     * video to ignore.
     */
    @UiField
    protected FormattedTimeBox startFromTime;

    /**
     * The error label when the start from time is longer than the duration of
     * the video
     */
    @UiField
    protected HTML startFromError;

    /** The select dropdown for the task or concept */
    @UiField
    protected Select taskConceptSelect;

    /**
     * The add button for the {@link #videoMetadataDialog} to upload the video
     * file and add it to the session
     */
    @UiField
    protected Button metaOkButton;
    
    /** the delete button for the {@link #videoMetadataDialog} to delete video file and
     * metadata file on the server */
    @UiField
    protected Button metaDeleteButton;

    /** The cancel button for the {@link #videoMetadataDialog} */
    @UiField
    protected Button metaCancelButton;

    /**
     * The modal dialog to show a loading icon while the video is uploading and
     * the session is updating
     */
    @UiField
    protected Modal videoFileUploadDialog;

    /** The icon to show while the video is uploading */
    @UiField
    protected BsLoadingIcon loadingIcon;

    /** The label to show while the video is uploading */
    @UiField
    protected Label uploadingLabel;

    /** The name of the selected video file */
    private String selectedFilename = null;

    /** The video manager that controls the video element */
    private VideoManager videoManager;
    
    /** the video metadata loaded into this dialog, will be null when adding video. */
    private VideoMetadata videoMetadata = null;

    /** Timer for loading the video */
    private final Timer timer = new Timer() {
        
        /* The number of times this timer has been checked*/
        private int numIterations = 0;
        
        @Override
        public void run() {
            
            if(numIterations < VIDEO_TIMEOUT/LOAD_TIMEOUT_INTERVAL_DURATION) {
                
                /* The total video timeout duration has not passed, so keep iterating */
                numIterations++;
                
            } else {
                DetailedException e = new DetailedException(
                        "The selected video failed to be loaded within a reasonable amount of time.", "The video file '"
                                + selectedFilename + "' failed to be loaded within " + VIDEO_TIMEOUT / 1000 + " seconds.",
                        null);
                hide();
    
                final DetailedExceptionSerializedWrapper eWrap = new DetailedExceptionSerializedWrapper(e);
                UiManager.getInstance().displayDetailedErrorDialog("Failed to load the selected video.", eWrap.getReason(),
                        eWrap.getDetails(), eWrap.getErrorStackTrace(), null, new ModalDialogCallback() {
                            @Override
                            public void onClose() {
                                show();
                            }
                        });
                
                cancel();
            }
        }
        
        @Override
        public void scheduleRepeating(int periodMillis) {
            
            /* Reset the count of iterations */
            numIterations = 0;
            
            super.scheduleRepeating(periodMillis);
        }
    };
    
    /**
     * Builds the default dialog components.
     */
    private AddVideoModalDialog(){
        
        initWidget(uiBinder.createAndBindUi(this));

        /* Set modal z-index to max (has to beat out blocker pane from video
         * panel) */
        videoFileSelectionDialog.getElement().getStyle().setZIndex(Integer.MAX_VALUE);
        videoMetadataDialog.getElement().getStyle().setZIndex(Integer.MAX_VALUE);
        videoMetadataDialog.getElement().getStyle().setOverflow(Overflow.AUTO);
        videoFileUploadDialog.getElement().getStyle().setZIndex(Integer.MAX_VALUE);

        fileSelection.setAllowedFileExtensions(Constants.VIDEO);
        fileSelection.setFileTableSize("100%", "220px");
        fileSelection.getFileUpload().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                final String newFilename = fileSelection.getFileUpload().getFilename();
                if (!isFileSelectionValid(newFilename)) {
                    fileSelection.reset();
                    return;
                }

                /* Get the selected file */
                final File file = getSelectedFile();
                if (file == null) {
                    return;
                }

                selectedFilename = FileTreeModel.createFromRawPath(newFilename).getFileOrDirectoryName();
                filenameLabel.setText(selectedFilename);
                if(StringUtils.isBlank(displayName.getValue())){
                    
                    String noExtName = selectedFilename;
                    if(noExtName.lastIndexOf(".") > 0){
                        noExtName = noExtName.substring(0, noExtName.lastIndexOf("."));
                    }
                    displayName.setValue(noExtName);
                }

                // dont care about video metadata changes since the user hasn't fully committed to adding this new
                // video to the game master, they can still cancel.
                loadVideo(file.getLocalUrl(), null);
            }
        });

        selectionCancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        selectionBackButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showMetadataDialog();
            }
        });

        changeFileBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                if(videoMetadata != null){
                    // prevent handling hacked button visibility, i.e. can't change when editing existing metadata
                    return;
                }
                
                videoMetadataDialog.hide();

                selectionCancelButton.setVisible(false);
                selectionBackButton.setVisible(true);

                videoFileSelectionDialog.show();
            }
        });

        /* Create the value change handlers to check if the add button should
         * become enabled. */
        final ValueChangeHandler<String> stringValueChange = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateAddButton();
            }
        };
        final ValueChangeHandler<Integer> integerValueChange = new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                updateAddButton();
            }
        };

        displayName.addValueChangeHandler(stringValueChange);
        playAtTime.addValueChangeHandler(integerValueChange);
        startFromTime.addValueChangeHandler(integerValueChange);
        taskConceptSelect.addValueChangeHandler(stringValueChange);

        /* The task/concept names is more of a placeholder to show where to add
         * video context fields, we will eventually want something more
         * sophisticated here that shows the task/concept hierarchy. */

        final AbstractKnowledgeSession session = RegisteredSessionProvider.getInstance().getRegisteredSession();
        final List<String> taskConceptNames = new ArrayList<>(session.getNodeIdToNameMap().values());
        Collections.sort(taskConceptNames);

        final OptGroup taskConceptOptGroup = new OptGroup();
        for (String name : taskConceptNames) {
            Option opt = new Option();
            opt.setText(name);
            opt.setValue(name);
            taskConceptOptGroup.add(opt);
        }

        taskConceptSelect.add(taskConceptOptGroup);
        taskConceptSelect.setPlaceholder("Choose a task/concept");
        taskConceptSelect.refresh();
        
        metaCancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
    }
    
    /**
     * Populate and show the video metadata dialog populated with the provided video metadata for possible
     * editing or deleting.
     * @param videoMetadata contains metadata about a video, can't be null.
     * @param videoChangedCallback used for notification of success or failure on the server of a video update request (e.g. delete, update metadata).
     * Can't be null.
     */
    public AddVideoModalDialog(final VideoMetadata videoMetadata, final VideoChangedCallback videoChangedCallback){
        this();
        
        if (videoMetadata == null) {
            throw new IllegalArgumentException("The parameter 'videoMetadata' cannot be null.");
        }else if(videoChangedCallback == null){
            throw new IllegalArgumentException("The parameter 'deleteCallback' cannot be null.");
        }
        
        this.videoMetadata = videoMetadata;
        this.selectedFilename = videoMetadata.getFileName();
        
        // change from 'ok' to 'save'
        metaOkButton.setText("Save");
        
        // enable delete button
        metaDeleteButton.setVisible(true);
        metaDeleteButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                // show are you sure dialog
                UiManager.getInstance().displayConfirmDialog("Delete Video?", 
                        "Are you sure you want to delete the <b>"+videoMetadata.getTitle()+"</b> video?", 
                        "Yes, Delete", "Cancel", new ConfirmationDialogCallback() {
                            
                            @Override
                            public void onDecline() {
                                // no-op
                            }
                            
                            @Override
                            public void onAccept() {
                                // delete metadata file + video file on server
                                final RecorderParams params = new RecorderParams();
                                params.setVideoMetadata(videoMetadata);
                                params.setPerformDelete(true);
                                
                                logger.info("Asking the server to delete the video and metadata file for "+videoMetadata);

                                try{
                                    /* delete the selected file */
                                    MediaInputUtil.deleteRecording(params, new DeleteCallback() {
                                        @Override
                                        public void onDeleted() {
                                            videoChangedCallback.onDeleted(videoMetadata);
                                        }
    
                                        @Override
                                        public void onDeleteFailed(String message) {                                            
                                            videoChangedCallback.onDeletedFailed(message, videoMetadata);
                                        }
                                    });
                                }catch(Throwable t){
                                    logger.log(Level.SEVERE, "Error thrown from client when trying to request to delete video", t);
                                }

                            }
                        });
            }
        });
        
        // not supporting changing the video file at this time
        changeFileBtn.setVisible(false);        
        
        loadVideo(VideoManager.getVideoURL(videoMetadata, true), videoChangedCallback);
        
        // save metadata action
        metaOkButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {                
               
                final RecorderParams params = buildRecorderParams();

                /* update metadata on server */
                MediaInputUtil.uploadRecording(params, MIMEType.MP4, Blob.create(), new UploadCallback() {
                    @Override
                    public void onUploaded(String fileRef) {
                        videoChangedCallback.onUpdated(params.getVideoMetadata());
                        hide();
                    }

                    @Override
                    public void onUploadFailed(String message) {
                        videoChangedCallback.onUpdateFailed(message, videoMetadata);
                        hide();
                    }
                });                                

            }
            
        });
    }

    /**
     * Constructor for new videos being uploaded, not editing existing video metadata.
     * 
     * @param uploadCallback the callback to invoke once the media is saved or
     *        once the save operation fails. Can't be null.
     */
    public AddVideoModalDialog(final NewVideoCallback uploadCallback) {
        this();
        
        if (uploadCallback == null) {
            throw new IllegalArgumentException("The parameter 'uploadCallback' cannot be null.");
        }

        metaOkButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!updateAddButton()) {
                    return;
                }
                
                showUploadDialog();
                
                final RecorderParams params = buildRecorderParams();

                /* Upload the selected file */
                MediaInputUtil.uploadRecording(params, MIMEType.MP4, getSelectedFile(), new UploadCallback() {
                    @Override
                    public void onUploaded(String fileRef) {
                        VideoMetadata preUploadVMetadata = params.getVideoMetadata();
                        // use the returned file ref as the vmeta.xml file location and create a new video metadata object
                        // that will then be shared on the client and used to uniquely identify this new video metadata 
                        VideoMetadata newVMetadata = new VideoMetadata(fileRef, 
                                preUploadVMetadata.getLocation(),
                                preUploadVMetadata.getStartTime(), 
                                preUploadVMetadata.getOffset(), 
                                preUploadVMetadata.getTitle(), 
                                preUploadVMetadata.getTaskConceptName());
                        logger.info("video upload successful -"+newVMetadata);
                        uploadCallback.onUploaded(newVMetadata);
                        hide();
                    }

                    @Override
                    public void onUploadFailed(String message) {
                        uploadCallback.onUploadFailed(message);
                        hide();
                    }
                });
            }
        });

    }
    
    @SuppressWarnings("deprecation")
    private RecorderParams buildRecorderParams(){
        
        /* Trim the name before saving */
        final String name = displayName.getText().trim();

        /* Need to use deprecated Date methods because Calendar is not
         * supported in gwt */
        Date playAtDate = new Date(getPlaybackTime());
        playAtDate.setHours(playAtTime.getHours());
        playAtDate.setMinutes(playAtTime.getMinutes());
        playAtDate.setSeconds(playAtTime.getSeconds());
        

        final String logFile = RegisteredSessionProvider.getInstance().getLogMetadata().getLogFile();
        FileTreeModel logFileModel = FileTreeModel.createFromRawPath(logFile);
        FileTreeModel videoModel = FileTreeModel
                .createFromRawPath(logFileModel.getParentTreeModel().getRelativePathFromRoot()
                        + Constants.FORWARD_SLASH + selectedFilename);

        final RecorderParams params = new RecorderParams();
        VideoMetadata vMetadata;
        if(videoMetadata != null){
            // use the current video metadata object to get the metadata file name
            vMetadata = new VideoMetadata(videoMetadata.getMetadataFile(), videoModel.getRelativePathFromRoot(), playAtDate,
                    (long) startFromTime.getValue(), name, taskConceptSelect.getValue());
        }else{
            vMetadata = new VideoMetadata(videoModel.getRelativePathFromRoot(), playAtDate,
                    (long) startFromTime.getValue(), name, taskConceptSelect.getValue());
        }
        params.setVideoMetadata(vMetadata);
        
        return params;
    }
    
    
    /**
     * Loads a video into the video manager.  Once loaded, asynchronously, the video metadata dialog
     * is updated appropriately and shown.
     * @param videoUrl the servlet URL of the video, used to load the video into the video manager.
     * @param videoChangedCallback used to notify the caller loading the video of changes made to the video properties.
     * Can be null if this video is in the process of being uploaded/added for the first time.
     */
    private void loadVideo(String videoUrl, final VideoChangedCallback videoChangedCallback){
        
        logger.info("Loading video from "+videoUrl);
        
        /* Stop any previous video */
        if (videoManager != null) {
            videoManager.resetVideo();
        }

        /* Mock up a video metadata so we can play the video normally */
        videoManager = new VideoManager(new VideoMetadata(videoUrl, new Date(), null, null, null), 1, videoChangedCallback);
        videoPanel.setWidget(videoManager.getVideoContainerPanel());
        videoManager.setChannelLabelVisibility(false);
        videoManager.setUseDefaultControls(true);
        videoManager.setEditOnClick(false);

        videoManager.loadVideo(false, new LoadVideoCallback() {
            @Override
            public void onLoad() {
                logger.info("Successfully loaded video");
                timer.cancel();
                videoManager.updateState(VideoState.ACTIVE);

                populateMetadata();
                showMetadataDialog();
            }

            @Override
            public void onError(VideoError error) {
                logger.severe("Failed to load video - "+error.message());
                timer.cancel();                       
                hide();
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

    /** Show the add video modal dialog */
    public void show() {
        
        if(videoMetadata == null){
            /* Hide to clear any previous data */
            hide();
    
            /* Show file selection */
            videoFileSelectionDialog.show();
        }else{
            showMetadataDialog();
        }
    }

    /** Show the metadata modal dialog */
    private void showMetadataDialog() {
        videoFileSelectionDialog.hide();
        videoFileUploadDialog.hide();
        videoMetadataDialog.show();
    }

    /** Show the upload modal dialog */
    private void showUploadDialog() {
        videoFileSelectionDialog.hide();
        videoMetadataDialog.hide();
        videoFileUploadDialog.show();

        loadingIcon.startLoading();
        uploadingLabel.setText("Uploading \"" + selectedFilename + "\" please wait...");
    }

    /** Hides the add video dialog and resets all fields for next time */
    public void hide() {
        /* Hide dialogs */
        videoFileSelectionDialog.hide();
        videoMetadataDialog.hide();
        videoFileUploadDialog.hide();
        loadingIcon.stopLoading();

        /* Clear file selection */
        fileSelection.reset();
        selectedFilename = null;
        selectionCancelButton.setVisible(true);
        selectionBackButton.setVisible(false);

        /* Clear video */
        if (videoManager != null) {
            videoManager.resetVideo();
            videoManager = null;
            videoPanel.clear();
        }

        /* Clear metadata panel */
        filenameLabel.setText(null);
        displayName.setText(null);
        playAtTime.setValue(null);
        startFromTime.setValue(null);

        /* Disable add button */
        updateAddButton();
    }

    /**
     * Populates the {@link #videoMetadataDialog} with the metadata from the
     * session and the selected video
     */
    @SuppressWarnings("deprecation")
    private void populateMetadata() {
        
        if(this.videoMetadata == null){
            /* Need to use deprecated Date methods because Calendar is not supported
             * in gwt */
            Date playAtDate = new Date(getPlaybackTime());
            Integer hourSeconds = FormattedTimeBox.convertHoursToSeconds(playAtDate.getHours());
            Integer minuteSeconds = FormattedTimeBox.convertMinutesToSeconds(playAtDate.getMinutes());
            Integer seconds = playAtDate.getSeconds();
            playAtTime.setValue(hourSeconds + minuteSeconds + seconds, true);
    
            startFromTime.setValue(0, true);
        }else{
            logger.info("populating video metadata dialog from - "+videoMetadata);
            filenameLabel.setText(videoMetadata.getFileName());
            displayName.setValue(videoMetadata.getTitle());
            
            /* Need to use deprecated Date methods because Calendar is not supported
             * in gwt */
            Date playAtDate = videoMetadata.getStartTime();
            Integer hourSeconds = FormattedTimeBox.convertHoursToSeconds(playAtDate.getHours());
            Integer minuteSeconds = FormattedTimeBox.convertMinutesToSeconds(playAtDate.getMinutes());
            Integer seconds = playAtDate.getSeconds();
            playAtTime.setValue(hourSeconds + minuteSeconds + seconds, true);
            startFromTime.setValue((int) videoMetadata.getOffset(), true);

            if(StringUtils.isNotBlank(videoMetadata.getTaskConceptName())){
                taskConceptSelect.setValue(videoMetadata.getTaskConceptName());
            }
        }
    }

    /**
     * Checks to see if the selected file is valid. Must be non-null and have a
     * supported extension.
     * 
     * @param filename the filename to check.
     * @return true if the filename passes the validation criteria; false
     *         otherwise.
     */
    private boolean isFileSelectionValid(String filename) {
        if (StringUtils.isBlank(filename)) {
            return false;
        }

        for (String ext : Constants.VIDEO) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the current playback time. If unavailable, get the log metadata's
     * start time.
     * 
     * @return the playback time, or the log start time if the playback time is
     *         null.
     */
    private long getPlaybackTime() {
        long playbackTime;
        try {
            playbackTime = TimelineProvider.getInstance().getPlaybackTime();
        } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
            /* The timeline playback time must be null. Use the log start time
             * instead. */
            playbackTime = RegisteredSessionProvider.getInstance().getLogMetadata().getStartTime();
        }

        return playbackTime;
    }

    /**
     * Gets the selected {@link File} blob from the upload dialog.
     * 
     * @return the selected file.
     */
    private File getSelectedFile() {
        /* Get only the filename, trim the path */
        /* Get the selected file */
        JavaScriptObject fileListObj = fileSelection.getFileUpload().getElement().getPropertyJSO("files");
        if (fileListObj == null) {
            return null;
        }

        final FileList fileList = fileListObj.cast();
        final List<File> selectedFiles = fileList.getFiles();

        /* Should only have 1 file */
        return selectedFiles.isEmpty() ? null : selectedFiles.get(0);
    }

    /**
     * Checks if the {@link #displayName} value is valid. This will update the
     * {@link #displayNameError} visibility as well.
     * 
     * @return true if it is valid; false otherwise.
     */
    private boolean checkDisplayName() {
        final boolean valid = StringUtils.isNotBlank(displayName.getText());
        displayNameError.setVisible(!valid);
        return valid;
    }

    /**
     * Checks if the {@link #playAtTime} value is valid. This will update the
     * {@link #playAtWarning} and {@link #playAtError} visibility as well.
     * 
     * @return true if it is valid; false otherwise.
     */
    @SuppressWarnings("deprecation")
    private boolean checkPlayAtTime() {
        boolean valid = playAtTime.getValue() != null;
        boolean showWarning = false;
        if (valid) {

            /* Need to use deprecated Date methods because Calendar is not
             * supported in gwt */
            Date playAtDate = new Date(getPlaybackTime());
            playAtDate.setHours(playAtTime.getHours());
            playAtDate.setMinutes(playAtTime.getMinutes());
            playAtDate.setSeconds(playAtTime.getSeconds());

            final LogMetadata logMeta = RegisteredSessionProvider.getInstance().getLogMetadata();
            if (playAtDate.getTime() < logMeta.getStartTime()) {
                playAtWarning.setText("The play time is set to before the session starts");
                showWarning = true;
            } else if (playAtDate.getTime() >= logMeta.getEndTime()) {
                playAtWarning.setText("The play time is set to after the session ends");
                showWarning = true;
            }
        }

        playAtWarning.setVisible(showWarning);
        playAtError.setVisible(!valid);
        return valid;
    }

    /**
     * Checks if the {@link #startFromTime} value is valid. This will update the
     * {@link #startFromError} visibility as well.
     * 
     * @return true if it is valid; false otherwise.
     */
    private boolean checkStartFromTime() {
        boolean valid = true;
        final Integer seconds = startFromTime.getValue();

        /* null is valid; no offset */
        if (seconds != null && videoManager.getDuration() != null &&
                seconds >= Math.floor(videoManager.getDuration())) {
            valid = false;
        }

        startFromError.setVisible(!valid);
        return valid;
    }

    /**
     * Updates the {@link #metaOkButton} to be enabled or disabled based on the
     * required fields in the {@link #videoMetadataDialog}.
     * 
     * @return true if the button is enabled; false if it is disabled.
     */
    private boolean updateAddButton() {
        /* Perform each check individually so any error/warning messages will be
         * updated */
        boolean enable = true;
        if(this.videoMetadata == null){
            enable &= isFileSelectionValid(selectedFilename);
        }
        enable &= checkDisplayName();
        enable &= checkPlayAtTime();
        enable &= checkStartFromTime();
        metaOkButton.setEnabled(enable);
        return enable;
    }

    /**
     * A callback used to handle the results of uploading a new video file.
     * 
     * @author sharrison
     */
    public interface NewVideoCallback {

        /**
         * Handles when a new video is uploaded
         * 
         * @param videoMetadata the metadata of the new video. Can't be null.
         */
        public void onUploaded(VideoMetadata videoMetadata);

        /**
         * Handles when an error occurs while uploading
         * 
         * @param message the error message that was reported. Can't be null.
         */
        public void onUploadFailed(String message);
    }
    
    /**
     * A callback used to handle the changing of a video file (e.g. delete, edit properties).
     * 
     * @author mhoffman
     *
     */
    public interface VideoChangedCallback{
        
        /**
         * Handles when a video is deleted
         * 
         * @param videoMetadata the metadata of the deleted video. Can't be null.
         */
        public void onDeleted(VideoMetadata videoMetadata);

        /**
         * Handles when an error occurs while deleting
         * 
         * @param message the error message that was reported. Can't be null.
         * @param videoMetadata the metadata of the video that failed to be deleted. Can't be null.
         */
        public void onDeletedFailed(String message, VideoMetadata videoMetadata);
        
        /**
         * Handles when a video metadata is updated
         * 
         * @param videoMetadata the updated metadata for a video. Can't be null.
         */
        public void onUpdated(VideoMetadata videoMetadata);

        /**
         * Handles when an error occurs while updating video metadata
         * 
         * @param message the error message that was reported. Can't be null.
         * @param videoMetadata the metadata of the video that failed to have its properties updated. Can't be null.
         */
        public void onUpdateFailed(String message, VideoMetadata videoMetadata);
    }
}
