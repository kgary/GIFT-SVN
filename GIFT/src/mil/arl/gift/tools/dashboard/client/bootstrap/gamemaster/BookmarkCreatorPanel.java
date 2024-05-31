/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.MIMEType;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.RecordingCallback;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.UploadCallback;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingPriority;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;

/**
 * A panel that provides UI components to create bookmarks
 * 
 * @author nroberts
 */
public class BookmarkCreatorPanel extends Composite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(BookmarkCreatorPanel.class.getName());
    
    /** The time format to use when displaying the time a bookmark was created */
    private static final DateTimeFormat BOOKMARK_TIMESTAMP_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.HOUR_MINUTE_SECOND);
    
    /** The URL host value corresponding to the local host of this client */
    private static final String LOCAL_HOST = "localhost";

    private static BookmarkCreatorPanelUiBinder uiBinder = GWT.create(BookmarkCreatorPanelUiBinder.class);

    interface BookmarkCreatorPanelUiBinder extends UiBinder<Widget, BookmarkCreatorPanel> {
    }
    
    /** The dropdown menu containing most global bookmark options. */
    @UiField
    protected DropDownMenu globalBookmarkDropdownMenu;
    
    /** The textbox for inputting bookmark comments */
    @UiField
    protected TextBox globalBookmarkCommentText;
    
    /** The floating panel containing the text box where bookmark text is entered */
    @UiField
    protected Widget bookmarkEntryPanel;
    
    /** The button to save a text bookmark. */
    @UiField
    protected Button saveGlobalBookmarkTextButton;
    
    /** The panel surrounding the Add Bookmark button */
    @UiField
    protected Button newGlobalBookmarkButton;
    
    /** button that is shown for touch screens to make it easier to enter gesture mode */
    @UiField
    protected Button enterGestureBookmarkHeaderButton;
    
    /** The anchor list item to record audio bookmark input */
    @UiField
    protected AnchorListItem recordAudioGlobalBookmarkButton;
    
    /** The anchor list item to provide global text bookmark input */
    @UiField
    protected AnchorListItem provideTextBookmarkButton;
    
    /** The anchor list item to record bookmarks using multi-touch gestures */
    @UiField
    protected AnchorListItem recordGestureBookmarkButton;
    
    /** The current mode being used by the global bookmarks button */
    private BookmarkButtonMode bookmarkMode;
    
    /** The raw data recorded from the user for audio global bookmarks*/
    private Blob recordedBlob = null;
    
    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    /** Whether the user has commanded an audio-recording to occur*/
    private boolean hasCommandedRecord;
    
    /** The knowledge session used for the bookmark data */
    private final AbstractKnowledgeSession knowledgeSession;

    /** A command to invoke when the user chooses to record using gestures*/
    private GestureCommands recordGestureCommand = null;

    /**
     * Creates a new panel to create global bookmarks for the given knowledge session
     * 
     * @param knowledgeSession the knowledge session to make bookmarks for. Cannot be null.
     */
    public BookmarkCreatorPanel(AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        this.knowledgeSession = knowledgeSession;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        //default the bookmark components to creation mode
        setBoomarkMode(BookmarkButtonMode.CREATE);
        
        globalBookmarkCommentText.addBlurHandler(new BlurHandler() {
            
            @Override
            public void onBlur(BlurEvent event) {
                setBoomarkMode(BookmarkButtonMode.CREATE); //cancel creating a bookmark if the user clicks off the text box
            }
        });
        
        globalBookmarkCommentText.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {

                boolean enterPressed = KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode();
                
                if (enterPressed){
                    
                    //save the bookmark if the user presses the Enter key
                    completeGlobalTextBookmarkInput();
                }
            }
        });
        
        newGlobalBookmarkButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                switch(bookmarkMode) {
                
                    case PROVIDE_TEXT:
                        
                        //cancel creating the bookmark
                        setBoomarkMode(BookmarkButtonMode.CREATE);
                        break;
                        
                    case RECORD_AUDIO:
                        
                        //stop recording an audio bookmark
                        setRecording(false);
                        break;
                        
                    default:
                        //just show the dropdown, which is handled automatically by the button
                        break;
                }
            }
        });
        
        recordAudioGlobalBookmarkButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //start recording audio for a bookmark when the dropdown option is clicked
                if (!getHasCommandedRecord()) {
                    setRecording(true);
                }
            }
        });
        
        provideTextBookmarkButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //show a textbox to let the user enter text for a bookmark when the dropdown option is clicked
                beginGlobalTextBookmarkInput();
            }
        });
        
        saveGlobalBookmarkTextButton.addMouseDownHandler(new MouseDownHandler() {
            
            @Override
            public void onMouseDown(MouseDownEvent event) {
                
                //save the enteted text bookmark
                completeGlobalTextBookmarkInput();
            }
        });
        
        recordGestureBookmarkButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if (!getHasCommandedRecord()) {
                   if(recordGestureCommand != null) {
                       recordGestureCommand.record();
                   }
                }
            }
        });
        
        enterGestureBookmarkHeaderButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if (!getHasCommandedRecord()) {
                    if(recordGestureCommand != null) {
                        recordGestureCommand.record();
                    }
                 }                
            }
        });
    }
    
    /**
     * Show or hide the enter gesture bookmark header button.  This button is used
     * to enter the gesture mode without having to go through the 'create note' menu.
     * @param show true to show the button on the header.
     */
    public void showEnterGestureModeHeaderButton(boolean show){
        enterGestureBookmarkHeaderButton.setVisible(show);
    }
    
    /**
     * Begin the process of creating a global text bookmark, by hiding the global bookmark dropdown menu
     * and displaying the globalBookmarkCommentText and the saveGlobalBookmarkTextButton.
     */
    public void beginGlobalTextBookmarkInput() {
        setBoomarkMode(BookmarkButtonMode.PROVIDE_TEXT);
        globalBookmarkCommentText.setFocus(true);
    }
    
    /**
     * Finishes the process of creating a global text bookmark. It creates the bookmark itself
     * with the text in globalBookmarkCommentText, then hides the text-specific UI elements and
     * displays the create bookmark menu.
     */
    public void completeGlobalTextBookmarkInput() {
        createGlobalTextBookmark(globalBookmarkCommentText.getText());
        globalBookmarkCommentText.setText("");
        setBoomarkMode(BookmarkButtonMode.CREATE);
    }
    
    /**
     * Resets the components used to create global bookmarks to their default state
     */
    public void resetBookmarkButton() {
        setBoomarkMode(BookmarkButtonMode.CREATE);
    }
    
    /**
     * Sets whether or not the global bookmark should record media from the user
     * 
     * @param recording whether to record media
     */
    public void setRecording(boolean recording) {
        
        if(recording) {
            
            if(!MediaInputUtil.hasMediaDevices()) {
                
                if(LOCAL_HOST.equals(Window.Location.getHostName())){
                    
                    //handle when browser does not support recording
                    displayErrorDialog(
                            "Recording Not Supported by Browser", 
                            "GIFT was unable to begin recording from your connected media devices because "
                            + "your browser does not support such recordings."
                            + "<br/><br/>Please use a different browser if you want to record media for GIFT.");
                
                } else if(DeploymentModeEnum.SERVER == UiManager.getInstance().getDeploymentMode()) {
                    
                    //handle when GIFT is in server mode on an insecure host that is blocked by security
                    displayErrorDialog(
                            "Recording Not Allowed by Browser", 
                            "Due to your browser's security restrictions, GIFT was unable to find any connected media devices to "
                            + "record from because your browser is connected to GIFT's webpages using an insecure (i.e. http://) host."
                            + "<br/><br/>Please contact the network administrator for the GIFT website you are using and tell"
                            + "them that GIFT cannot record from you because the server is not enforcing a secure connection.");
                    
                } else {
                    
                    //handle when GIFT is in desktop/simple mode on an insecure host that is blocked by security
                    String localHostUrl = Window.Location.getHref().replace(Window.Location.getHostName(), LOCAL_HOST);
                    
                   displayErrorDialog(
                            "Recording Not Allowed by Browser", 
                            "Due to your browser's security restrictions, GIFT was unable to find any connected media devices to "
                            + "record from because your browser is connected to GIFT's webpages using an insecure (i.e. http://) host."
                            + "<br/><br/>If you are running GIFT locally on the same machine that you are accessing this webpage from, "
                            + "try connecting to 'localhost' instead your machine's IP address using <a href='" 
                            + localHostUrl + "'>this link</a>.");
                }
                
                return;
            }
            
            hasCommandedRecord = true;
            
            notifyMsg("GIFT is recording audio");
            
            recordedBlob = null;
            
            setBoomarkMode(BookmarkButtonMode.RECORD_AUDIO);
            
            MediaInputUtil.startRecording(new RecordingCallback() {
                
                @Override
                public void onFinishedRecording(Blob recordingBlob) {
                    
                    //adjust the UI to indicate that media has been recorded
                    recordedBlob = recordingBlob;                    
                    
                    SessionMember host = knowledgeSession.getHostSessionMember();
                    RecorderParams params = new RecorderParams();
                    
                    // Save the recorded audio
                    if(host != null) {
                        params.setUserId(host.getUserSession().getUserId())
                            .setDomainSessionId(host.getDomainSessionId())
                            .setExperimentId(host.getUserSession().getExperimentId());
                    }
                    
                    saveRecording(params);
                    
                    setBoomarkMode(BookmarkButtonMode.CREATE);
                }

                @Override
                public void onRecordingFailed(String message) {
                    displayDetailedErrorDialog("Recording Failed", 
                        new DetailedExceptionSerializedWrapper(new DetailedException(
                            "GIFT was unable to record from your browser due to an unexpected error.", 
                            message, 
                            null
                    )));
                    
                    setBoomarkMode(BookmarkButtonMode.CREATE);
                }
            });
            
        } else {
            
            hasCommandedRecord = false;
            
            MediaInputUtil.stopRecording();
            
            setBoomarkMode(BookmarkButtonMode.CREATE);
        }
    }
    
    /**
     * Displays a detailed error dialog with the given detailed exception wrapper. This is handy for displaying the results of
     * RPC calls using {@link mil.arl.gift.common.gwt.client.GenericRpcResponse GenericRpcResponse}.
     *
     * @param title - The title of the dialog.
     * @param exceptionWrapper - A client-safe wrapper around a detailed exception.
     */
    private void displayDetailedErrorDialog(String title,
            DetailedExceptionSerializedWrapper detailedExceptionSerializedWrapper) {
        
        if(recordGestureCommand == null) {
            
            /* Fall back to displaying a normal dialog if there is no gesture manager */
            UiManager.getInstance().displayDetailedErrorDialog(title, detailedExceptionSerializedWrapper);
            return;
        }
        
        recordGestureCommand.displayDetailedError(title, detailedExceptionSerializedWrapper);
    }

    /**
     * Displays an error dialog with a title & message.
     *
     * @param title - The title of the dialog.
     * @param description - The message for the dialog.
     */
    private void displayErrorDialog(String title, String description) {
        
        if(recordGestureCommand == null) {
            
            /* Fall back to displaying a normal dialog if there is no gesture manager */
            UiManager.getInstance().displayErrorDialog(title, description, null);
            return;
        }
        
        recordGestureCommand.displayError(title, description);
    }

    /**
     * Saves the media recorded by this recording booth to disk
     * 
     * @param params an optional set of parameters to pass to the recorder service. Can be null. Affects how the recording is saved.
     */
    public void saveRecording(RecorderParams params) {
        
        if(recordedBlob != null) {
            MediaInputUtil.uploadRecording(params, MIMEType.WAV, recordedBlob, new UploadCallback() {
                
                @Override
                public void onUploaded(String fileRef) {
                    createGlobalMediaBookmark(fileRef);
                }

                @Override
                public void onUploadFailed(String message) {
                    notifyMsg("GIFT has failed to save the recording");
                    createGlobalMediaBookmark(null);
                }
            });
        }
    }
    
    /**
     * Gets the value of hasCommandedRecord
     * 
     * @return A boolean, whether or not user has commanded an audio-recording to occur.
     */
    public boolean getHasCommandedRecord() {
        return hasCommandedRecord;
    }
    
    /**
     * Creates a global bookmark with the specified comment.
     * @param bookmarkText The text of the bookmark's comment. Can be null.
     */
    public void createGlobalTextBookmark(String bookmarkText) {
        createGlobalBookmark(bookmarkText, null);
    }
    
    /**
     * Creates a global bookmark with the specified media link.
     * @param bookmarkMedia The text of the bookmark's media link. Can be null.
     */
    public void createGlobalMediaBookmark(String bookmarkMedia) {
        createGlobalBookmark(null, bookmarkMedia);
    }
    
    /**
     * Creates a new global bookmark with the specified comment and media
     * @param bookmarkText The text of the comment. Can be null.
     * @param bookmarkMedia The text of the media link. Can be null.
     */
    public void createGlobalBookmark(String bookmarkText, String bookmarkMedia) {      
        final EvaluatorUpdateRequest updateRequest = new EvaluatorUpdateRequest(null,
                BsGameMasterPanel.getGameMasterUserName(), System.currentTimeMillis());
        
        /* Update request with test description */
        updateRequest.setReason(bookmarkText);
        updateRequest.setMediaFile(bookmarkMedia);
        
        if (knowledgeSession.inPastSessionMode()) {
            
            LoadingDialogProvider.getInstance().startLoading(LoadingType.TIMELINE_REFRESH, LoadingPriority.HIGH, 
                    "Applying Note",
                    "Please wait while the note is applied and rendered onto the timeline...");
            
            /* Create patch for changes and apply to timeline */
            Long timestamp = TimelineProvider.getInstance().getPlaybackTime();
            dashboardService.createLogPatchForEvaluatorUpdate(
                    BrowserSession.getInstance().getBrowserSessionKey(), BsGameMasterPanel.getGameMasterUserName(),
                    timestamp, !Dashboard.getInstance().getSettings().isApplyChangesAtPlayhead(), updateRequest, false,
                    new AsyncCallback<GenericRpcResponse<String>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            logger.warning("Failed to create patch file because " + caught.getMessage());
                            
                            TimelineProvider.getInstance().reloadTimeline();
                        }
                        
                        @Override
                        public void onSuccess(GenericRpcResponse<String> result) {
                            
                            if(result.getContent() != null) {
                                if (logger.isLoggable(Level.INFO)) {
                                    logger.info("Successfully wrote patch file for bookmark update.");
                                }
                                
                                /* Update log metadata patch file name */
                                RegisteredSessionProvider.getInstance().updateLogPatchFile(result.getContent());
                                }
                            
                            if(!result.getWasSuccessful()) {
                                
                                boolean isLrsError = result.getContent() != null;
                                String title = isLrsError 
                                        ? "Unable to publish bookmark to external system"
                                        : "Failed to save bookmark";
                                
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
        } else {
        /* Send live message */
        BrowserSession.getInstance()
            .sendWebSocketMessage(new DashboardMessage(updateRequest, knowledgeSession));
        }
        
        String displayText;
        if(bookmarkMedia != null) {
            displayText = "Audio saved";
            
        } else {
            displayText = bookmarkText;
        }
        String formatTime = BOOKMARK_TIMESTAMP_FORMAT.format(new Date(System.currentTimeMillis()));
        
        notifyMsg("Note created at " + formatTime + "<br/><i>\"" + displayText +"\"</i>");
    }

    /**
     * Displays a notification message to the user. If game master is being shown in fullscreen, 
     * the notification message will be attached to the fullscreen element rather than the body element
     * so that the notification is still visible
     * 
     * @param string the message string to display in the notification. Can be null.
     */
    private void notifyMsg(String string) {
        
        NotifySettings settings = NotifySettings.newSettings();
        
        if(JsniUtility.getFullscreenElement() != null) {
            
            /* Game master is being shown in full screen mode, so attach the notification element to whatever element
             * is set to be full screen. This ensures that the notification can be seen. */
            settings.setElement(":fullscreen");
        }
        
        Notify.notify(string, settings);
    }

    /**
     * Updates the UI components used to author global bookmarks to reflect the given mode
     * 
     * @param mode the mode that the bookmark UI components should use. Cannot be null.
     */
    private void setBoomarkMode(final BookmarkButtonMode mode) {
        
        if(mode == null) {
            throw new IllegalArgumentException("The bookmark button mode cannot be null");
        }
        
        //update the mode that will be used by the click handler for the bookmark button
        this.bookmarkMode = mode;
        
        //change the text and icon of the bookmark button
        newGlobalBookmarkButton.setText(mode.getText());
        newGlobalBookmarkButton.setIcon(mode.getIcon());
        
        //show/hide the text box used to create text bookmarks
        bookmarkEntryPanel.setVisible(BookmarkButtonMode.PROVIDE_TEXT.equals(mode));
        
        /* 
         * When enabling/disabling the dropdown, we need to defer turning off the data toggle until AFTER any click
         * events have been processed. If we do not do this, then the click event gets propagated up to the
         * dropdown after the data toggle has been removed, so the dropdown remains open indefinitely
         */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            
            @Override
            public void execute() {
                newGlobalBookmarkButton.setDataToggle(BookmarkButtonMode.CREATE.equals(mode) ? Toggle.DROPDOWN : null);
            }
        });
    }
    /** 
     * Sets the command to invoke when the user chooses to record using gestures
     * 
     * @param command the command. Can be null, if nothing should happen when the user
     * chooses to record gestures.
     */
    public void setRecordGestureCommand(GestureCommands command) {
        this.recordGestureCommand = command;
    }
    
    /**
     * A set of modes that the global bookmark button can switch to to provide the user
     * with different button behavior and different UI
     * 
     * @author nroberts
     */
    private enum BookmarkButtonMode{
        
        /** Default mode. Used to start creating bookmarks.  */
        CREATE("Create Note", IconType.PLUS),
        
        /** Audio recording mode. Used to stop recording audio bookmarks */
        RECORD_AUDIO("Recording...", IconType.MICROPHONE),
        
        /** Text entry mode. Used to cancel entering bookmark text */
        PROVIDE_TEXT("Creating Note...", IconType.FONT);
        
        /** The text that the bookmark button should show */
        private String text;
        
        /** The icon that the bookmark button should show */
        private IconType icon;
        
        /**
         * Creates a new button mode with the given display text and icon
         * 
         * @param text The text that the bookmark button should show. Can be null.
         * @param icon The icon that the bookmark button should show. Can be null.
         */
        private BookmarkButtonMode(String text, IconType icon) {
            this.text = text;
            this.icon = icon;
        }

        /**
         * Gets the text that the bookmark button should show
         * 
         * @return the text. Can be null.
         */
        public String getText() {
            return text;
        }

        /**
         * Gets the icon that the bookmark button should show
         * 
         * @return the icon. Can be null.
         */
        public IconType getIcon() {
            return icon;
        }
    }
    
    /**
     * A set of commands that can be invoked on a gesture manager. Used to better interact
     * with touch screen devices.
     * 
     * @author nroberts
     */
    public static interface GestureCommands{
        
        /**
         * Notifies the gesture manager that the user has decided to record bookmarks using gestures
         */
        public void record();
        
        /**
         * Displays a detailed error dialog with the given detailed exception wrapper. This is handy for displaying the results of
         * RPC calls using {@link mil.arl.gift.common.gwt.client.GenericRpcResponse GenericRpcResponse}.
         *
         * @param title - The title of the dialog.
         * @param exceptionWrapper - A client-safe wrapper around a detailed exception.
         */
        public void displayDetailedError(String title, DetailedExceptionSerializedWrapper exceptionWrapper);
        
        /**
         * Displays an error dialog with a title & message.
         *
         * @param title - The title of the dialog.
         * @param description - The message for the dialog.
         */
        public void displayError(String title, String description);
    }
}
