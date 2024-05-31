/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.io.Serializable; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Audio;
import generated.dkf.BooleanEnum;
import generated.dkf.Feedback;
import generated.dkf.Feedback.File;
import generated.dkf.MediaSemantics;
import generated.dkf.Message;
import generated.dkf.Message.DisplaySessionProperties;
import generated.dkf.StrategyStressCategory;
import generated.dkf.TeamRef;
import generated.dkf.ToObserverController;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AddMessageWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.DisclosureButton;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CanGetRootDirectory;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileRequest;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.gwt.client.widgets.file.GetRootDirectoryCallback;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.NotifyUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.LoadedFileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchAvatarKeyNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchAvatarKeyNamesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModelResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateMediaSemantics;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateMediaSemanticsResult;

/**
 * A {@link ItemEditor} that edits instructional intervention feedback items for an item list.
 *
 * @author nroberts
 */
public class AddFeedbackWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AddFeedbackWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static AddFeedbackWidgetUiBinder uiBinder = GWT.create(AddFeedbackWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AddFeedbackWidgetUiBinder extends UiBinder<Widget, AddFeedbackWidget> {
    }

    /**
     * A callback interface that is invoked when the visibility of the
     * {@link #typeChoiceRibbon} has changed.
     *
     * @author tflowers
     *
     */
    interface RibbonVisibilityChangedHandler {
        /**
         * The method that is invoked once the visibility of the
         * {@link #typeChoiceRibbon} has changed.
         *
         * @param ribbonIsShown The new visibility of the ribbon. True if the
         *        ribbon is now visible, false if the ribbon is not visible.
         */
        void onVisibilityChanged(boolean ribbonIsShown);
    }

    /** The instructions to display in the file selection dialog used for Local Webpage objects */
    private static final String LOCAL_WEBPAGE_FILE_SELECTION_INTRUCTIONS = " Select a web page file.<br> Supported extensions are :<b>"
            + Constants.html_supported_types + ".</b><br/><br/>"
            + "You can also select a <b>.zip</b> file containing a web page file and its resources (e.g. style sheets, scripts, "
            + "images, etc.) in order to load them simultaneously. This can be helpful when loading web pages with many dependencies.";
    
    /** The file extension for archive (ZIP) files */
    private static final String ZIP = ".zip";
    
    /**
     * The file extensions that should be allowed by the file selection dialog used for Local
     * Webpage objects
     */
    private static final String[] LOCAL_WEBPAGE_ALLOWED_FILE_EXTENSIONS;
    static {

        List<String> allowedExtensions = new ArrayList<String>();
        allowedExtensions.addAll(Arrays.asList(Constants.html_supported_types));
        allowedExtensions.add(ZIP);

        String[] extensionsArray = new String[allowedExtensions.size()];

        LOCAL_WEBPAGE_ALLOWED_FILE_EXTENSIONS = allowedExtensions.toArray(extensionsArray);
    }

    /** The extension for MP3 files */
    public static final String MP3_FILE_EXTENSION = ".mp3";

    /** The extension for OGG files */
    public static final String OGG_FILE_EXTENSION = ".ogg";

    /** The extension for MP3 files */
    public static final String[] AVATAR_FILE_EXTENSION = {".html", ".htm"};

    /** The message to display to the author if no file is selected */
    private static final String NO_FILE_SELECTED = "No File Selected";

    /** A modal used to track the progress of unzipping operations */
    private LoadedFileOperationProgressModal<UnzipFileResult> unzipProgressModal = new LoadedFileOperationProgressModal<UnzipFileResult>(
            ProgressType.UNZIP);

    /** The deck panel containing the ribbon, message panel, audio panel, and no-action panel */
    @UiField
    protected DeckPanel deckPanel;

    /** The panel that contains the {@link #typeChoiceRibbon} */
    @UiField
    protected FlowPanel typeChoiceRibbonPanel;

    /** The ribbon that contains the types of feedback to author */
    @UiField
    protected Ribbon typeChoiceRibbon;

    /** The panel containing the message input */
    @UiField
    protected Widget messagePanel;

    /** The panel containing the feedback file inputs */
    @UiField
    protected Widget feedbackFilePanel;

    /** The panel containing the audio feedback inputs */
    @UiField
    protected Widget audioPanel;

    /** The button used to opt-in to a delay after audio */
    @UiField
    protected DisclosureButton audioDelayButton;

    /** The control used to specify the delay after audio */
    @UiField
    protected FormattedTimeBox audioDelayTimeBox;

    /** The panel containing the avatar or no avatar panels */
    @UiField
    protected DeckPanel messageDeck;

    /** The panel containing the avatar inputs */
    @UiField
    protected Widget avatarPanel;

    /** The panel that does not contain the avatar inputs */
    @UiField
    protected Widget noAvatarPanel;

    /** The widget to author a feedback message */
    @UiField (provided = true)
    protected AddMessageWidget messageEditor = new AddMessageWidget(ScenarioClientUtility.getTrainingAppType()) {
        @Override
        protected void fireDirtyEvent(Serializable sourceObject) {
            ScenarioEventUtility.fireDirtyEditorEvent(sourceObject);
        }
    };

    /** The button to select a feedback html file */
    @UiField
    protected Button feedbackHTMLFileButton;
    
    /** The tooltip of the {@link #feedbackFileButton} */
    @UiField
    protected Tooltip feedbackFileButtonTooltip;

    /** The button to select an MP3 file */
    @UiField
    protected Button mp3FileButton;

    /** The tooltip of the {@link #mp3FileButton} */
    @UiField
    protected Tooltip mp3FileButtonTooltip;

    /** The button to select an OGG file */
    @UiField
    protected Button oggFileButton;

    /** The tooltip of {@link #oggFileButton} */
    @UiField
    protected Tooltip oggFileButtonTooltip;

    /** The button used to delete the currently selected OGG file */
    @UiField
    protected Button deleteOggFileButton;

    /** The tooltip of the {@link #deleteOggFileButton} */
    @UiField
    protected Tooltip deleteOggFileButtonTooltip;

    /** The button to select an avatar file */
    @UiField
    protected com.google.gwt.user.client.ui.Button chooseAvatarBtn;

    /** The label that shows the avatar filename */
    @UiField
    protected HTML avatarFileLabel;

    /** The dropdown containing the speech keys from the avatar file */
    @UiField
    protected ListBox speechKeyListBox;

    /** The picker for the team and its members */
    @UiField
    protected EditableTeamPicker teamPicker;
    
    /** Send message to game master controller option */
    @UiField
    protected CheckBoxButton sendToObserverControllerButton;
    
    /** panel that contains the stress category buttons */
    @UiField
    protected HorizontalPanel stressCategoryPickerPanel;
    
    /** the button for selecting the environmental type stress category */
    @UiField
    protected Button environmentalStressCategoryButton;
    
    /** the button for selecting the cognitive type stress category */
    @UiField
    protected Button cognitiveStressCategoryButton;
    
    /** the button for selecting the physiological type stress category */
    @UiField
    protected Button physiologicalStressCategoryButton;
    
    @UiField
    protected MessageDisplaySessionPropertiesWrapper requestSessionState;

    /** The dialog for selecting the local webpage (HTML) file */
    private DefaultGatFileSelectionDialog feedbackHTMLFileDialog = new DefaultGatFileSelectionDialog();

    /** The dialog for selecting the MP3 file */
    private DefaultGatFileSelectionDialog mp3FileDialog = new DefaultGatFileSelectionDialog();

    /** The dialog for selecting the OGG file */
    private DefaultGatFileSelectionDialog oggFileDialog = new DefaultGatFileSelectionDialog();

    /** The dialog for selecting the Avatar file */
    private DefaultGatFileSelectionDialog avatarFileDialog = new DefaultGatFileSelectionDialog(new CanGetRootDirectory() {

        @Override
        public void getRootDirectory(final GetRootDirectoryCallback callback) {

            AsyncCallback<FetchRootDirectoryModelResult> asyncCallback = new AsyncCallback<FetchRootDirectoryModelResult>(){

                @Override
                public void onFailure(Throwable thrown) {
                    callback.onFailure(thrown);
                }

                @Override
                public void onSuccess(FetchRootDirectoryModelResult result) {

                    if(result.isSuccess()){
                        callback.onSuccess(result.getDomainDirectoryModel().getModelFromRelativePath(
                                DefaultGatFileSelectionDialog.courseFolderPath));

                    } else {

                        if(result.getErrorMsg() != null){
                            callback.onFailure(result.getErrorMsg());

                        } else {
                            callback.onFailure("An error occurred while getting the root directory.");
                        }
                    }
                }

            };

            String userName = GatClientUtility.getUserName();

            FetchRootDirectoryModel action = new FetchRootDirectoryModel();
            action.setUserName(userName);

            SharedResources.getInstance().getDispatchService().execute(action, asyncCallback);
        }
    });

    /** A map that keeps track of which speech keys correspond to which avatar file */
    private Map<String, List<String>> avatarToKeysMap = new HashMap<>();

    /** The {@link Set} of each {@link RibbonVisibilityChangedHandler} that's registered. */
    private final Set<RibbonVisibilityChangedHandler> ribbonVisibilityChangedHandlers = new HashSet<>();

    /** The speech key that was found in the loaded schema object, if applicable */
    private String loadedSpeechKey = null;

    /** Validation used to handle selecting a local webpage file */
    private final WidgetValidationStatus feedbackFileValidation;

    /** Validation used to handle selecting an MP3 audio file */
    private final WidgetValidationStatus audioValidation;

    /** Validation used to verify the value of an audio delay */
    private final WidgetValidationStatus audioDelayValidation;

    /** Validation used to handle selecting an avatar file */
    private final WidgetValidationStatus avatarFileValidation;

    /** Validation used to handle selecting an avatar speech key */
    private final WidgetValidationStatus speechKeyValidation;

    /**
     * Creates a new editor for modifying feedback items
     */
    public AddFeedbackWidget() {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Constructing feedback widget");
        }

        initWidget(uiBinder.createAndBindUi(this));

        messageEditor.setActive(false);
        messageEditor.setInTrainingAppFeedbackVisibility(ScenarioClientUtility.doesSupportInTrainingAppFeedback());
        messageEditor.setInTutorFeedbackVisibility(true);
        teamPicker.setActive(false);

        feedbackFileValidation = new WidgetValidationStatus(feedbackHTMLFileButton,
                "No local webpage has been specified. Select a local webpage for this feedback.");

        audioValidation = new WidgetValidationStatus(mp3FileButton,
                "No MP3 file to play has been specified. Select an MP3 file for this audio feedback.");

        audioDelayValidation = new WidgetValidationStatus(audioDelayTimeBox,
                "A non zero delay must be provided for an audio feedback activity.");

        avatarFileValidation = new WidgetValidationStatus(chooseAvatarBtn,
                "No avatar file has been specified. Select an avatar file from which to play scripted actions.");

        speechKeyValidation = new WidgetValidationStatus(speechKeyListBox,
                "No speech key has been selected. Select scripted speech key to play.");

        //initialize deck panels to their default state
        showTypeChoiceRibbon();
        messageDeck.showWidget(messageDeck.getWidgetIndex(noAvatarPanel));

        typeChoiceRibbon.setTileHeight(105);

        // add handlers for each of the editor's type panels
       typeChoiceRibbon.addRibbonItem(IconType.COMMENT, "Present a Message",
                "Select this to add a message that presents text and/or dynamic speech.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        messageDeck.showWidget(messageDeck.getWidgetIndex(noAvatarPanel));
                        showDeckWidget(deckPanel.getWidgetIndex(messagePanel));

                        // validate the message editor when it is shown
                        messageEditor.setActive(true);
                        messageEditor.validateAll();
                        
                        sendToObserverControllerButton.setVisible(true);
                    }
                });

        Widget localWebpage = typeChoiceRibbon.addRibbonItem(IconType.FILE, "Local Webpage", "Select this to add a hyperlink that will open a local webpage.",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        showDeckWidget(deckPanel.getWidgetIndex(feedbackFilePanel));

                        // clear the message editor's validations when it is hidden
                        messageEditor.clearValidations();
                        messageEditor.setActive(false);

                        requestValidation(feedbackFileValidation);
                    }
                });

        Widget playAudio = typeChoiceRibbon.addRibbonItem(IconType.MUSIC, "Play Audio", "Select this to add audio feedback.",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        showDeckWidget(deckPanel.getWidgetIndex(audioPanel));

                        // clear the message editor's validations when it is hidden
                        messageEditor.clearValidations();
                        messageEditor.setActive(false);

                        sendToObserverControllerButton.setVisible(true);

                        requestValidation(audioValidation);
                    }
                });

        Widget playAvatar = typeChoiceRibbon.addRibbonItem(IconType.USER, "Play Avatar Script",
                "Select this to add a scripted avatar message.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        messageDeck.showWidget(messageDeck.getWidgetIndex(avatarPanel));
                        showDeckWidget(deckPanel.getWidgetIndex(messagePanel));

                        // clear the message editor's validations when it is hidden
                        messageEditor.clearValidations();
                        messageEditor.setActive(false);

                        requestValidation(avatarFileValidation, speechKeyValidation);
                    }
                });
        
        // If LessonLevel is set to RTA, then the widgets should be hidden.
        if(GatClientUtility.isRtaLessonLevel()){
            localWebpage.setVisible(false);
            playAudio.setVisible(false);
            playAvatar.setVisible(false);
            messageEditor.setInTutorFeedbackVisibility(false);
        }

        feedbackHTMLFileDialog.setIntroMessageHTML(LOCAL_WEBPAGE_FILE_SELECTION_INTRUCTIONS);
        feedbackHTMLFileDialog.setAllowedFileExtensions(LOCAL_WEBPAGE_ALLOWED_FILE_EXTENSIONS);
        feedbackHTMLFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                feedbackFileButtonTooltip.hide();
                feedbackHTMLFileDialog.center();
            }
        });
        
        // set up logic to allow the author to upload either plain HTML files or ZIP archives
        // containing them
        final CanHandleUploadedFile originalUploadHandler = feedbackHTMLFileDialog.getUploadHandler();

        feedbackHTMLFileDialog.setUploadHandler(new CanHandleUploadedFile() {

            @Override
            public void handleUploadedFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback) {

                if (uploadFilePath.endsWith(ZIP)) {

                    // ZIP files containing web pages should have their contents extracted to the
                    // course folder
                    UnzipFile action = new UnzipFile(GatClientUtility.getUserName(),
                            GatClientUtility.getBaseCourseFolderPath(), uploadFilePath);

                    final String filename = uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);

                    // display successful upload message in Notify UI not a dialog
                    feedbackHTMLFileDialog.setMessageDisplay(new DisplaysMessage() {

                        @Override
                        public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                            WarningDialog.warning(title, text, callback);
                        }

                        @Override
                        public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                            Notify.notify("",
                                    "The contents of '" + filename + "' have been extracted into your course.",
                                    IconType.INFO, NotifyUtil.generateDefaultSettings());
                        }

                        @Override
                        public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }

                        @Override
                        public void showDetailedErrorMessage(String text, String details, List<String> stackTrace,
                                ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }
                    });

                    copyOrUploadZip(action, null, callback);

                } else {

                    // upload HTML files normally
                    originalUploadHandler.handleUploadedFile(uploadFilePath, fileName, callback);
                }
            }

        });

        // set up logic to allow the author to copy either plain HTML files or ZIP archives
        // containing them
        final CopyFileRequest originalCopyRequest = feedbackHTMLFileDialog.getCopyFileRequest();

        feedbackHTMLFileDialog.setCopyFileRequest(new CopyFileRequest() {

            @Override
            public void asyncCopy(final FileTreeModel source, final CopyFileCallback callback) {

                final String filename = source.getFileOrDirectoryName();

                if (filename.endsWith(ZIP)) {

                    // ZIP files containing web pages should have their contents extracted to the
                    // course folder
                    UnzipFile action = new UnzipFile(GatClientUtility.getUserName(),
                            GatClientUtility.getBaseCourseFolderPath(), source.getRelativePathFromRoot());

                    // display successful upload message in Notify UI not a dialog
                    feedbackHTMLFileDialog.setMessageDisplay(new DisplaysMessage() {

                        @Override
                        public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                            WarningDialog.warning(title, text, callback);
                        }

                        @Override
                        public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                            Notify.notify("",
                                    "The contents of '" + filename + "' have been extracted into your course.",
                                    IconType.INFO, NotifyUtil.generateDefaultSettings());
                        }

                        @Override
                        public void showErrorMessage(String title, String text, ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }

                        @Override
                        public void showDetailedErrorMessage(String reason, String details, List<String> stackTrace,
                                ModalDialogCallback callback) {
                            // Let the onFailure method display ErrorDetailsDialog
                        }
                    });

                    copyOrUploadZip(action, callback, null);

                } else {
                    originalCopyRequest.asyncCopy(source, callback);
                }
            }

        });

        // set up logic to handle when the user has selected a non-ZIP file from the file selection
        // dialog (ZIP logic is separate)
        feedbackHTMLFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(final ValueChangeEvent<String> event) {

                if (event.getValue() != null) {

                    boolean isHtmlFile = false;

                    for (String extension : Constants.html_supported_types) {

                        if (event.getValue().endsWith(extension)) {
                            isHtmlFile = true;
                            break;
                        }
                    }

                    if (isHtmlFile) {

                        // HTML files should be loaded normally when the user has selected them in
                        // the file selection dialog
                        setSelectedFeedbackFileHTML(event.getValue());
                    }

                } else {
                    setSelectedFeedbackFileHTML(event.getValue());
                }
            }
        });

        mp3FileDialog.setAllowedFileExtensions(new String[]{MP3_FILE_EXTENSION});
        oggFileDialog.setAllowedFileExtensions(new String[]{OGG_FILE_EXTENSION});
        mp3FileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                mp3FileButtonTooltip.hide();
                mp3FileDialog.center();
            }
        });

        mp3FileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(event.getValue() != null){
                    setSelectedMp3File(event.getValue());
                }
            }
        });

        oggFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                oggFileButtonTooltip.hide();
                oggFileDialog.center();
            }
        });

        oggFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(event.getValue() != null){
                    setSelectedOggFile(event.getValue());
                }
            }
        });

        avatarFileDialog.setUploadEnabledIfPossible(false, null);
        avatarFileDialog.setAllowedFileExtensions(AVATAR_FILE_EXTENSION);
        avatarFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(event.getValue() != null && !event.getValue().isEmpty()){

                    validateMediaSemantics(event.getValue(), false);

                    setSelectedAvatarFile(event.getValue());
                    populateSpeechKeyNames(event.getValue());
                }
            }
        });

        chooseAvatarBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                avatarFileDialog.center();
            }

        });

        speechKeyListBox.setVisibleItemCount(1);
        
        sendToObserverControllerButton.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                /* Need to defer reading the value, since it takes a moment for
                 * the value to bubble up */
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        final boolean isSelected = Boolean.TRUE.equals(sendToObserverControllerButton.getValue());
                        sendToObserverControllerButton.setActive(isSelected);
                    }
                });
            }
        });
        
        /**
         * Disable click events on the button when the button is disabled.
         * This is to work around the issue with GWT Bootstrap CheckBoxButton class setEnabled() 
         * method which doesn't actually do what it's supposed to. It visually disables the element, 
         * but it doesn't actually stop it from receiving click events. 
         */
        sendToObserverControllerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(!sendToObserverControllerButton.isEnabled()){
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });
        
        requestSessionState.addValueChangeHandler(new ValueChangeHandler<Message.DisplaySessionProperties>() {

            @Override
            public void onValueChange(ValueChangeEvent<DisplaySessionProperties> event) {
                refreshDisplayProperties(event.getValue());
            }
        });
    }

    /**
     * Performs a request to the server to extract the contents of a .zip file. If successful, the
     * path to the base web page will be copied to the Local Webpage course objects. If more than
     * one web page is extracted from the .zip, the author will be prompted to select which one
     * should be used.
     * 
     * @param action The action to perform
     * @param copyCallback The callback to execute if an existing .zip is being copied or null if
     *        this is an upload operation.
     * @param uploadCallback The callback to execute if a new slide .zip is being uploaded or null
     *        if this is a copy operation.
     */
    private void copyOrUploadZip(final UnzipFile action, final CopyFileCallback copyCallback,
            final HandleUploadedFileCallback uploadCallback) {

        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

            @Override
            public void onFailure(Throwable caught) {

                ErrorDetailsDialog dialog = new ErrorDetailsDialog("A server error occured while unzipping the file.",
                        "The action failed on the server: " + caught.getMessage(),
                        DetailedException.getFullStackTrace(caught));
                dialog.setText("Error");
                dialog.center();

                if (copyCallback != null) {
                    copyCallback.onFailure(caught);

                } else {
                    uploadCallback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(final GatServiceResult result) {

                feedbackHTMLFileDialog.hide();

                unzipProgressModal.startPollForProgress(new AsyncCallback<LoadedProgressIndicator<UnzipFileResult>>() {

                    @Override
                    public void onSuccess(final LoadedProgressIndicator<UnzipFileResult> response) {

                        if (response.isComplete() && response.getPayload() != null) {

                            final UnzipFileResult result = response.getPayload();

                            if (result.isSuccess()) {

                                // invoke the callback of whatever operation initially called this
                                // method (copy or upload)
                                if (copyCallback != null) {
                                    copyCallback.onSuccess(result.getUnzippedFolderModel());

                                } else {
                                    uploadCallback.onSuccess(result.getUnzippedFolderModel());
                                }

                                int numHtmlFiles = 0;
                                String firstHtmlFile = null;

                                // interate through the list of files that were extracted and
                                // identify the number of HTML files
                                for (String file : result.getUnzippedFolderModel().getFileNamesUnderModel()) {

                                    for (String extension : Constants.html_supported_types) {

                                        if (file.endsWith(extension)) {

                                            numHtmlFiles++;

                                            if (firstHtmlFile == null) {
                                                firstHtmlFile = file;
                                            }

                                            break;
                                        }
                                    }
                                }

                                if (numHtmlFiles == 0) {

                                    // report a warning if no HTML files were found
                                    WarningDialog.warning("No Web Pages Found",
                                            "No web page files (e.g. .htm, .html) were extracted from the ZIP archive you selected. <br/><br/>"
                                                    + "The files that were extracted will remain in your course folder, but they will not be used by "
                                                    + "this Local Webpage object. If you wish to discard these files, you can use the "
                                                    + "Media panel to delete them.");

                                } else if (numHtmlFiles == 1) {

                                    // if only one HTML file was extracted, select it
                                    String parentPath = result.getUnzippedFolderModel().getFileOrDirectoryName();

                                    setSelectedFeedbackFileHTML(parentPath + "/" + firstHtmlFile);
                                } else {

                                    // if more than one HTML file was extracted, prompt the author
                                    // to select which one they want to use
                                    final FileSelectionDialog selectTargetDialog = new FileSelectionDialog(
                                            new CanGetRootDirectory() {

                                                @Override
                                                public void getRootDirectory(GetRootDirectoryCallback callback) {
                                                    callback.onSuccess(result.getUnzippedFolderModel());
                                                }

                                            }, DefaultMessageDisplay.includeAllMessages);

                                    selectTargetDialog.setAllowedFileExtensions(Constants.html_supported_types);
                                    selectTargetDialog.setIntroMessageHTML(
                                            "The contents of the ZIP archive you selected have been successfully "
                                                    + "extracted to this course's folder. <br/><br/>"
                                                    + "Multiple web pages were found in the extracted ZIP archive. Please select the web page "
                                                    + "file you wish to display.");

                                    selectTargetDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

                                        @Override
                                        public void onValueChange(ValueChangeEvent<String> event) {

                                            selectTargetDialog.hide();

                                            // author has selected the HTML file they want to use,
                                            // so use it
                                            String selectedHtmlFile = event.getValue();
                                            String parentPath = result.getUnzippedFolderModel()
                                                    .getFileOrDirectoryName();

                                            setSelectedFeedbackFileHTML(parentPath + "/" + selectedHtmlFile);
                                        }
                                    });

                                    selectTargetDialog.center();
                                }

                            } else {

                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "Failed to extract the archive because an error occurred on the server: "
                                                + result.getErrorMsg(),
                                        result.getErrorDetails(), result.getErrorStackTrace()

                                );
                                dialog.setText("Error");
                                dialog.center();

                                if (copyCallback != null) {
                                    copyCallback.onFailure(result.getErrorMsg());
                                } else {
                                    uploadCallback.onFailure(result.getErrorMsg());
                                }
                            }

                        } else {

                            if (response.getException() != null) {

                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "Failed to extract the archive because an error occurred on the server: "
                                                + response.getException().getReason(),
                                        response.getException().getReason(),
                                        response.getException().getErrorStackTrace());
                                dialog.setText("Error");
                                dialog.center();

                                if (copyCallback != null) {
                                    copyCallback.onFailure(response.getException().getReason());
                                } else {
                                    uploadCallback.onFailure(response.getException().getReason());
                                }

                            } else {

                                // this shouldn't be possible to hit, but handle it just in case
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "Failed to extract the archive because an unknown error occurred on the server.",
                                        "Failed to extract the archive because an unknown error occurred on the server.",
                                        null);
                                dialog.setText("Error");
                                dialog.center();

                                if (copyCallback != null) {
                                    copyCallback.onFailure(response.getException().getReason());
                                } else {
                                    uploadCallback.onFailure(response.getException().getReason());
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {

                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                "A server error occured while unzipping the file.",
                                "The action failed on the server: " + caught.getMessage(),
                                DetailedException.getFullStackTrace(caught));
                        dialog.setText("Error");
                        dialog.center();

                        if (copyCallback != null) {
                            copyCallback.onFailure(caught);

                        } else {
                            uploadCallback.onFailure(caught);
                        }
                    }
                });
            }

        });
    }    
    
    /**
     * Handles the environmentalStressCategoryButton being clicked.
     * 
     * @param event the event contain information about the click
     */
    @UiHandler("environmentalStressCategoryButton")
    protected void onEnvironmentalStressCategoryButtonToggled(ClickEvent event) {
        
        // toggle this one
        environmentalStressCategoryButton.setActive(!environmentalStressCategoryButton.isActive());
        environmentalStressCategoryButton.setFocus(environmentalStressCategoryButton.isActive());
        
        cognitiveStressCategoryButton.setActive(false);
        physiologicalStressCategoryButton.setActive(false);
    }
    
    /**
     * Handles the cognitiveStressCategoryButton being clicked.
     * 
     * @param event the event contain information about the click
     */
    @UiHandler("cognitiveStressCategoryButton")
    protected void onCognitiveStressCategoryButtonToggled(ClickEvent event) {
        environmentalStressCategoryButton.setActive(false);
        
        // toggle this one
        cognitiveStressCategoryButton.setActive(!cognitiveStressCategoryButton.isActive());
        cognitiveStressCategoryButton.setFocus(cognitiveStressCategoryButton.isActive());
        
        physiologicalStressCategoryButton.setActive(false);
    }
    
    /**
     * Handles the physiologicalStressCategoryButton being clicked.
     * 
     * @param event the event contain information about the click
     */
    @UiHandler("physiologicalStressCategoryButton")
    protected void onPhysiologicalStressCategoryButtonToggled(ClickEvent event) {
        environmentalStressCategoryButton.setActive(false);
        cognitiveStressCategoryButton.setActive(false);
        
        // toggle this one
        physiologicalStressCategoryButton.setActive(!physiologicalStressCategoryButton.isActive());
        physiologicalStressCategoryButton.setFocus(physiologicalStressCategoryButton.isActive());
    }

    /**
     * The event handler for the {@link #speechKeyListBox}. Requests for the
     * value of the list box to be reevaluated.
     *
     * @param event The event indicating that a change has taken place.
     */
    @UiHandler("speechKeyListBox")
    protected void onSpeechKeyIndexChanged(ChangeEvent event) {
        requestValidation(speechKeyValidation);
    }

    /**
     * The event handler that is fired when the delete button is clicked.
     * Deletes the OGG file that has currently been selected for an
     * {@link Audio} feedback.
     *
     * @param event The event The event that contains information about the
     *        click.
     */
    @UiHandler("deleteOggFileButton")
    protected void onDeleteOggFileClicked(ClickEvent event) {
        deleteOggFileButtonTooltip.hide();
        setSelectedOggFile(null);
    }

    /**
     * The event handler that revalidates the specified value for the delay when
     * the delay has been toggled on or off.
     *
     * @param event The event that specifies whether the button has been toggled
     *        on or off.
     */
    @UiHandler("audioDelayButton")
    protected void onDelayButtonToggled(ValueChangeEvent<Boolean> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onDelayButtonToggled(" + event.getValue() + ")");
        }

        if (!event.getValue()) {
            audioDelayButton.blur();
        }

        requestValidation(audioDelayValidation);
    }

    /**
     * The event handler that revalidates the specified value for the delay when
     * the value of the time has changed.
     *
     * @param event The event that contains the new value of the delay.
     */
    @UiHandler("audioDelayTimeBox")
    protected void onDelayTimeBoxChanged(ValueChangeEvent<Integer> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onDelayTimeBoxChanged(" + event.getValue() + ")");
        }

        requestValidation(audioDelayValidation);
    }

    /**
     * Registers a {@link RibbonVisibilityChangedHandler} to be invoked when the ribbon has
     * been shown or when the user has made a choice on the ribbon.
     *
     * @param handler The {@link RibbonVisibilityChangedHandler} to register. Can't be null.
     */
    public void addRibbonVisibilityChangedHandler(RibbonVisibilityChangedHandler handler) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addRibbonVisibilityChangedHandler(" + handler + ")");
        }

        if (handler == null) {
            throw new IllegalArgumentException("The parameter 'handler' cannot be null.");
        }

        ribbonVisibilityChangedHandlers.add(handler);
    }

    /**
     * Determines if the user has picked a feedback type based on the state of
     * the UI.
     *
     * @return True if the {@link #messagePanel} widget is currently visible
     *         (indicating the user has made a feedback type choice), false
     *         otherwise.
     */
    public boolean isFeedbackTypeSelected() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isFeedbackTypeSelected()");
        }

        return deckPanel.getVisibleWidget() != deckPanel.getWidgetIndex(typeChoiceRibbonPanel);
    }

    /**
     * Shows the provided deck widget and shows/hides the save button accordingly.
     *
     * @param deckWidget the deck widget index.
     */
    private void showDeckWidget(int deckWidget) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showDeckWidget(" + deckWidget + ")");
        }

        final int previousWidget = deckPanel.getVisibleWidget();
        final int ribbonIndex = deckPanel.getWidgetIndex(typeChoiceRibbonPanel);

        /* Change the widget that is visible */
        deckPanel.showWidget(deckWidget);

        /* Raise the ribbon visiblity changed event if necessary */
        boolean ribbonWasShown = previousWidget == ribbonIndex;
        boolean ribbonIsShown = ribbonIndex == deckWidget;
        if (ribbonIsShown || ribbonWasShown) {
            invokeRibbonVisibilityChangedHandlers(ribbonIsShown);
        }        
        
        // only show the stress category panel when the ribbon is not shown
        stressCategoryPickerPanel.setVisible(!ribbonIsShown);
        
        // only show the team picker when a feedback type has been selected
        if(ribbonIsShown) {

            teamPicker.setVisible(false);
            teamPicker.clearValidations();
            teamPicker.setActive(false);
            
        } else if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
            
            teamPicker.setVisible(true);
            teamPicker.setActive(true);
            teamPicker.validateAll();
        }
    }

    /**
     * Makes the {@link #typeChoiceRibbon} visible.
     */
    private void showTypeChoiceRibbon() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showTypeChoiceRibbon()");
        }

        final int ribbonIndex = deckPanel.getWidgetIndex(typeChoiceRibbonPanel);
        showDeckWidget(ribbonIndex);
    }

    /**
     * Invokes all available {@link RibbonVisibilityChangedHandler} handlers registered
     * with this {@link AddFeedbackWidget}.
     *
     * @param ribbonIsVisible True if the ribbon has become visible, false if
     *        the ribbon has become invisible.
     */
    private void invokeRibbonVisibilityChangedHandlers(boolean ribbonIsVisible) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("invokeRibbonVisibilityChangedHandlers(" + ribbonIsVisible + ")");
        }

        for (RibbonVisibilityChangedHandler handler : ribbonVisibilityChangedHandlers) {
            try {
                handler.onVisibilityChanged(ribbonIsVisible);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "There was an uncaught exception while invoking a "
                        + RibbonVisibilityChangedHandler.class.getName(), t);
            }
        }
    }

    /**
     * Populates the editor with a given {@link Feedback}.
     *
     * @param obj The {@link Feedback} with which to populate the editor.
     */
    protected void populateEditor(Feedback obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + obj + ")");
        }

        /* Clear fields that might have been changed by previously edited
         * objects */
        setSelectedFeedbackFileHTML(null);
        setSelectedMp3File(null);
        setSelectedOggFile(null);
        messageDeck.showWidget(messageDeck.getWidgetIndex(noAvatarPanel));
        setSelectedAvatarFile(null);
        speechKeyListBox.clear();
        messageEditor.setActive(false);
        audioDelayButton.setValue(false);
        audioDelayTimeBox.setValue(null);
        sendToObserverControllerButton.setValue(false, true);
        requestSessionState.setValue(null);

        Message message = null;

        /* Populate editor fields based on the object being edited */
        Serializable presentation = obj.getFeedbackPresentation();

        if (presentation instanceof Message) {
            sendToObserverControllerButton.setVisible(true);
            message = (Message) presentation;
            messageEditor.setActive(true);
            showDeckWidget(deckPanel.getWidgetIndex(messagePanel));
            boolean sendToController = message.getDelivery() != null && message.getDelivery().getToObserverController() != null 
                    && StringUtils.isNotBlank(message.getDelivery().getToObserverController().getValue());
            sendToObserverControllerButton.setValue(sendToController, true);
            
            requestSessionState.setValue(message.getDisplaySessionProperties());
            
            refreshDisplayProperties(message.getDisplaySessionProperties());
            
        } else if (presentation instanceof File){
            sendToObserverControllerButton.setVisible(false);
            setFeedbackFile((File) presentation);
            showDeckWidget(deckPanel.getWidgetIndex(feedbackFilePanel));
        } else if (presentation instanceof Audio) {
            Audio audio = (Audio) presentation;
            boolean sendToController = audio.getToObserverController() != null && StringUtils.isNotBlank(audio.getToObserverController().getValue());
            sendToObserverControllerButton.setVisible(true);
            sendToObserverControllerButton.setValue(sendToController, true);
            setAudio(audio);
            showDeckWidget(deckPanel.getWidgetIndex(audioPanel));
        } else if (presentation instanceof MediaSemantics) {
            sendToObserverControllerButton.setVisible(false);
            MediaSemantics avatar = (MediaSemantics) presentation;
            setMediaSemantics(avatar);

            message = avatar.getMessage();
            showDeckWidget(deckPanel.getWidgetIndex(messagePanel));
            messageDeck.showWidget(messageDeck.getWidgetIndex(avatarPanel));
        } else {
            sendToObserverControllerButton.setVisible(false);
            showTypeChoiceRibbon();
        }

        messageEditor.populateEditor(message);
        
        List<String> teamNames = new ArrayList<>();
        
        for(TeamRef ref : obj.getTeamRef()) {
            teamNames.add(ref.getValue());
        }
        
        teamPicker.setValue(teamNames);
    }

    /**
     * Returns a reference to the currently authored {@link Feedback}.
     *
     * @return The {@link Feedback} which has been authored. Can be null.
     */
    protected Feedback getFeedback() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getFeedback()");
        }

        int showing = deckPanel.getVisibleWidget();
        
        Feedback feedback = null;

        if(showing == deckPanel.getWidgetIndex(messagePanel)) {

            feedback = new Feedback();

            Message message = new Message();
            messageEditor.applyEdits(message);
            
            if (Boolean.TRUE.equals(sendToObserverControllerButton.getValue())) {
                ToObserverController toController = new ToObserverController();
                // Temporary value. Eventually it will be the id of a specific controller.
                toController.setValue("controller");
                message.getDelivery().setToObserverController(toController);
            }

            if(avatarPanel.isVisible()) {

                MediaSemantics avatar = new MediaSemantics();
                avatar.setAvatar(avatarFileLabel.getText());
                avatar.setKeyName(speechKeyListBox.getValue(speechKeyListBox.getSelectedIndex()));

                if(message.getContent() != null) {
                    avatar.setMessage(message);

                } else {
                    avatar.setMessage(null);
                }

                feedback.setFeedbackPresentation(avatar);

            } else {

                if(requestSessionState.getValue() != null 
                        && BooleanEnum.TRUE.equals(requestSessionState.getValue().getRequestUsingSessionState())) {
                    message.setDisplaySessionProperties(requestSessionState.getValue());
                }
                
                feedback.setFeedbackPresentation(message);
            }

        } else if(showing == deckPanel.getWidgetIndex(feedbackFilePanel)){
            
            feedback = new Feedback();
            feedback.setFeedbackPresentation(getFeedbackFile());
            
        } else if(showing == deckPanel.getWidgetIndex(audioPanel)) {
            
            feedback = new Feedback();
            feedback.setFeedbackPresentation(getAudio());
        }
        
        if(feedback != null) {
            
            if(teamPicker.isVisible() && teamPicker.getValue() != null) {
                
                for(String teamName : teamPicker.getValue()) {
                    
                    TeamRef ref = new TeamRef();
                    ref.setValue(teamName);
                    feedback.getTeamRef().add(ref);
                }
            }
        }
        
        return feedback;
    }
    
    /**
     * Return the optional authored stress category for this intervention.
     * 
     * @return can return null if one of the stress category buttons was not selected.
     */
    public StrategyStressCategory getStressCategory() {
        StrategyStressCategory stressCategory = null;

        if(environmentalStressCategoryButton.isActive()) {
            stressCategory = StrategyStressCategory.ENVIRONMENTAL;
        }else if(physiologicalStressCategoryButton.isActive()) {
            stressCategory = StrategyStressCategory.PHYSIOLOGICAL;
        }else if(cognitiveStressCategoryButton.isActive()) {
            stressCategory = StrategyStressCategory.COGNITIVE;
        }
        
        return stressCategory;
    }
    
    /**
     * Set the optional stress category for this intervention.  Selects the appropriate button.
     * @param category can be null which will result in no button be selected.
     */
    public void setStressCategory(StrategyStressCategory category) {        
        environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
        cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
        physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
    }

    /**
     * Gets the authored delay to wait before moving on to the next activity.
     *
     * @return The amount of time to wait in seconds.
     */
    public Integer getDelay() {
        if (deckPanel.getVisibleWidget() == deckPanel.getWidgetIndex(audioPanel)) {
            return audioDelayButton.getValue() ? audioDelayTimeBox.getValue() : null;
        } else {
            return messageEditor.getDelay();
        }
    }

    /**
     * Sets the authored delay to wait before moving on to the next activity.
     *
     * @param delayInSeconds The amount of time to wait in seconds.
     */
    public void setDelay(Integer delayInSeconds) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setDelay(" + delayInSeconds + ")");
        }

        audioDelayButton.setValue(true);
        audioDelayTimeBox.setValue(delayInSeconds);
        messageEditor.setDelay(delayInSeconds);
    }
    
    /**
     * Gets the currently authored feedback file based on the current UI state.
     * 
     * @return the (@link File} that has been authored based on the current UI
     * state.  Can't be null.
     */
    private Feedback.File getFeedbackFile(){
        Feedback.File file = new Feedback.File();
        file.setHTML(getSelectedFeedbackFileHTML());
        return file;
    }

    /**
     * Gets the currently authored audio based on the current UI state.
     *
     * @return The {@link Audio} that has been authored based on the current UI
     *         state. Can't be null.
     */
    private Audio getAudio() {
        Audio audio = new Audio();
        audio.setMP3File(getSelectedMp3File());
        audio.setOGGFile(getSelectedOggFile());

        if (Boolean.TRUE.equals(sendToObserverControllerButton.getValue())) {
            ToObserverController toController = new ToObserverController();
            // Temporary value. Eventually it will be the id of a specific controller.
            toController.setValue("controller");
            audio.setToObserverController(toController);
        }
            
        return audio;
    }

    /**
     * Sets the UI state based on a given {@link File} object.
     * 
     * @param file the {@link File} object to populate the UI with.  Can't be 
     * null.
     */
    private void setFeedbackFile(Feedback.File file){
        if (file == null) {
            throw new IllegalArgumentException("The parameter 'file' cannot be null.");
        }

        setSelectedFeedbackFileHTML(file.getHTML());
    }

    /**
     * Sets the UI state based on a given {@link Audio} object.
     *
     * @param audio The {@link Audio} object to populate the UI with. Can't be
     *        null.
     */
    private void setAudio(Audio audio) {
        if (audio == null) {
            throw new IllegalArgumentException("The parameter 'audio' cannot be null.");
        }

        setSelectedMp3File(audio.getMP3File());
        setSelectedOggFile(audio.getOGGFile());
    }
    
    /**
     * Gets the currently selected feedback file based on the current state of the
     * UI.
     *
     * @return The currently selected feedback file based on the UI. A null value
     *         indicates that no feedback file is currently selected.
     */
    private String getSelectedFeedbackFileHTML(){
        return feedbackHTMLFileButton.getText() != NO_FILE_SELECTED ? feedbackHTMLFileButton.getText() : null;
    }
    
    /**
     * Updates the UI state based on a provided feedback html file {@link String}.
     *
     * @param selectedFile The html file whose selection should now be reflected
     *        in the UI. A null value is treated as selecting nothing.
     */
    private void setSelectedFeedbackFileHTML(String selectedFile) {
        if (StringUtils.isNotBlank(selectedFile)) {
            feedbackHTMLFileButton.setText(selectedFile);
            feedbackHTMLFileButton.setIcon(null);
            feedbackFileButtonTooltip.setTitle("Click to select a different local webpage.");
        } else {
            feedbackHTMLFileButton.setText(NO_FILE_SELECTED);
            feedbackHTMLFileButton.setIcon(IconType.PLUS_CIRCLE);
            feedbackFileButtonTooltip.setTitle("Click to select a local webpage.");
        }

        requestValidation(feedbackFileValidation);
    }

    /**
     * Gets the currently selected MP3 file based on the current state of the
     * UI.
     *
     * @return The currently selected MP3 file based on the UI. A null value
     *         indicates that no MP3 file is currently selected.
     */
    private String getSelectedMp3File() {
        return mp3FileButton.getText() != NO_FILE_SELECTED ? mp3FileButton.getText() : null;
    }

    /**
     * Updates the UI state based on a provided MP3 file {@link String}.
     *
     * @param selectedFile The MP3 file whose selection should now be reflected
     *        in the UI. A null value is treated as selecting nothing.
     */
    private void setSelectedMp3File(String selectedFile) {
        if (StringUtils.isNotBlank(selectedFile)) {
            mp3FileButton.setText(selectedFile);
            mp3FileButton.setIcon(null);
            mp3FileButtonTooltip.setTitle("Click to select a different MP3 file.");
        } else {
            mp3FileButton.setText(NO_FILE_SELECTED);
            mp3FileButton.setIcon(IconType.PLUS_CIRCLE);
            mp3FileButtonTooltip.setTitle("Click to select an MP3 file.");
        }

        requestValidation(audioValidation);
    }

    /**
     * Gets the currently selected OGG file based on the current state of the
     * UI.
     *
     * @return The currently selected OGG file based on the UI. A null value
     *         indicates that no OGG file is currently selected.
     */
    private String getSelectedOggFile() {
        return oggFileButton.getText() != NO_FILE_SELECTED ? oggFileButton.getText() : null;
    }

    /**
     * Updates the UI state based on a provided OGG file {@link String}.
     *
     * @param selectedFile The OGG file whose selection should now be reflected
     *        in the UI. A null value is treated as selecting nothing.
     */
    private void setSelectedOggFile(String selectedFile) {
        if (StringUtils.isNotBlank(selectedFile)) {
            oggFileButton.setText(selectedFile);
            oggFileButton.setIcon(null);
            oggFileButtonTooltip.setTitle("Click to select a different OGG file.");

            deleteOggFileButton.setEnabled(true);
            deleteOggFileButtonTooltip.setTitle("Click to deselect the optional OGG file.");
        } else {
            oggFileButton.setText(NO_FILE_SELECTED);
            oggFileButton.setIcon(IconType.PLUS_CIRCLE);
            oggFileButtonTooltip.setTitle("Click to select an OGG file.");

            deleteOggFileButton.setEnabled(false);
            deleteOggFileButtonTooltip.setTitle(null);
        }
    }

    /**
     * Updates the UI based on a provided {@link MediaSemantics}.
     *
     * @param avatar The {@link MediaSemantics}
     */
    private void setMediaSemantics(MediaSemantics avatar) {
        if (avatar == null) {
            throw new IllegalArgumentException("The parameter 'avatar' cannot be null.");
        }

        if (avatar.getAvatar() != null && !avatar.getAvatar().isEmpty()) {
            avatarFileLabel.setText(avatar.getAvatar());

            if (avatar.getKeyName() != null && !avatar.getKeyName().isEmpty()) {

                loadedSpeechKey = avatar.getKeyName();

                List<String> speechKeys = avatarToKeysMap.get(avatar.getAvatar());
                if (speechKeys == null) {

                    /* speech keys haven't been loaded from the server, so we
                     * need to do that */
                    populateSpeechKeyNames(avatar.getAvatar());

                } else {

                    /* speech keys are already loaded, so try to find a key
                     * matching the loaded value */
                    for (String key : speechKeys) {
                        speechKeyListBox.addItem(key);
                    }

                    int selectedKeyIndex = speechKeys.indexOf(avatar.getKeyName());
                    speechKeyListBox.setSelectedIndex(selectedKeyIndex);
                }
            }
        } else {
            avatarFileLabel.setText(NO_FILE_SELECTED);
            speechKeyListBox.clear();
        }
    }

    /**
     * Gets the currently selected avatar file based on the current state of the
     * UI.
     *
     * @return The currently selected avatar file based on the UI state. Can be
     *         null if no avatar file is selected.
     */
    private String getSelectedAvatarFile() {
        return avatarFileLabel.getText() != NO_FILE_SELECTED ? avatarFileLabel.getText() : null;
    }

    /**
     * Updates the UI state based on a provided avatar file.
     *
     * @param selectedFile The avatar file whose selection should now be
     *        reflected in the UI. A null value is treated as selecting nothing.
     */
    private void setSelectedAvatarFile(String selectedFile) {
        avatarFileLabel.setText(selectedFile != null ? selectedFile : NO_FILE_SELECTED);
        requestValidation(avatarFileValidation);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(messageEditor);
        childValidationComposites.add(teamPicker);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getValidationStatuses(" + validationStatuses + ")");
        }

        validationStatuses.add(feedbackFileValidation);
        validationStatuses.add(audioValidation);
        validationStatuses.add(audioDelayValidation);
        validationStatuses.add(avatarFileValidation);
        validationStatuses.add(speechKeyValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if(audioValidation.equals(validationStatus)) {

            if(audioPanel.isVisible()) {
                boolean mp3FileIsSelected = StringUtils.isNotBlank(getSelectedMp3File());
                audioValidation.setValidity(mp3FileIsSelected);
            } else {
               audioValidation.setValid();
            }

        } else if (audioDelayValidation.equals(validationStatus)) {
            boolean isAudioPanelVisible = deckPanel.getVisibleWidget() == deckPanel.getWidgetIndex(audioPanel);
            boolean isToggledOn = audioDelayButton.getValue();
            Integer delayInSeconds = audioDelayTimeBox.getValue();
            audioDelayValidation.setValidity(!isAudioPanelVisible || !isToggledOn
                    || delayInSeconds != null && delayInSeconds > 0);
            
        } else if(feedbackFileValidation.equals(validationStatus)){
            
            if(feedbackFilePanel.isVisible()){
                boolean feedbackFileSelected = StringUtils.isNotBlank(getSelectedFeedbackFileHTML());
                feedbackFileValidation.setValidity(feedbackFileSelected);
            } else {
                feedbackFileValidation.setValid();
            }

        } else if(avatarFileValidation.equals(validationStatus)) {

            if(messagePanel.isVisible() && avatarPanel.isVisible()) {
                avatarFileValidation.setValidity(getSelectedAvatarFile() != null);
            } else {
                avatarFileValidation.setValid();
            }
        } else if(speechKeyValidation.equals(validationStatus)) {
            if(messagePanel.isVisible() && avatarPanel.isVisible()) {
                /* Determine if the value selected by the list box is not
                 * blank */
                String selectedValue = speechKeyListBox.getSelectedValue();
                boolean selectedValueIsNotBlank = StringUtils.isNotBlank(selectedValue);

                /* Determine if the speech key that was received during
                 * population is not blank. */
                boolean loadedSpeechKeyIsNotBlank = StringUtils.isNotBlank(loadedSpeechKey);

                /* The populate method is technically an asynchronous method due
                 * to the invocation of populateSpeechKeyNames. If the
                 * asynchronous method has not yet returned, the loadedSpeechKey
                 * should be used. If the asynchronous method has returned
                 * (meaning the list box has populated) then we should use the
                 * value within the list box. */
                speechKeyValidation.setValidity(loadedSpeechKeyIsNotBlank || selectedValueIsNotBlank);
            } else {
                speechKeyValidation.setValid();
            }
        }
    }

    /**
     * Populates the speech key list box.
     *
     * @param avatarFilePath The path to the avatar file.
     */
    private void populateSpeechKeyNames(final String avatarFilePath) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Initiating server method to populate speech keys");
        }

        final String filePath = DefaultGatFileSelectionDialog.courseFolderPath + "/" + avatarFilePath;
        FetchAvatarKeyNames action = new FetchAvatarKeyNames();
        action.setUserName(GatClientUtility.getUserName());
        action.setAvatar(Arrays.asList(filePath));

        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<FetchAvatarKeyNamesResult>() {

            @Override
            public void onFailure(Throwable thrown) {
                WarningDialog.error("Failed to retrieve agent keys", "Unable to retrieve the agent keys from the file because a server error occurred: " + thrown.toString());
            }

            @Override
            public void onSuccess(FetchAvatarKeyNamesResult result) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Received server result for getting speech keys");
                }

                if(result.isSuccess()) {

                    avatarToKeysMap.put(avatarFilePath.replace("/", "\\"), result.getAvatarToKeyNames().get(filePath));
                    for(String key : result.getAvatarToKeyNames().get(filePath)) {
                        speechKeyListBox.addItem(key);
                    }

                    if(loadedSpeechKey != null) {
                        speechKeyListBox.setSelectedIndex(result.getAvatarToKeyNames().get(filePath).indexOf(loadedSpeechKey));
                    }

                    requestValidation(speechKeyValidation);
                    loadedSpeechKey = null;

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Finished populating speech keys");
                    }

                } else {
                    ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
                            result.getErrorMsg(),
                            result.getErrorDetails(),
                            result.getErrorStackTrace());
                    errorDialog.setText("An Error Occurred");
                    errorDialog.center();
                }
            }
        });

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished initiating server method to populate speech keys");
        }
    }

    /**
     * Validates an Avatar.html file for compatibility with GIFT.
     *
     * @param filePath The path to the Avatar.html file
     * @param updateInvalidFiles True if any invalid files should be updated.
     */
    private void validateMediaSemantics(final String filePath, final boolean updateInvalidFiles) {

        ValidateMediaSemantics action = new ValidateMediaSemantics(
                GatClientUtility.getUserName(),
                DefaultGatFileSelectionDialog.courseFolderPath + "/" + filePath,
                updateInvalidFiles);

        BsLoadingDialogBox.display("Validating File", "Validating, please wait...");
        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<ValidateMediaSemanticsResult>() {

            @Override
            public void onFailure(Throwable thrown) {
                BsLoadingDialogBox.remove();
                WarningDialog.error("Failed to validate", "Failed to validate media semantics file because a server error occurred: " + thrown.toString());
            }

            @Override
            public void onSuccess(ValidateMediaSemanticsResult result) {
                BsLoadingDialogBox.remove();

                if(!result.isSuccess()) {
                    ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
                            result.getErrorMsg(),
                            result.getErrorDetails(),
                            result.getErrorStackTrace());
                    errorDialog.setText("An Error Occurred");
                    errorDialog.center();
                } else {

                    if(!updateInvalidFiles && !result.isValidFile()) {
                        OkayCancelDialog.show("Incompatible File", "The file you selected is not compatible with GIFT. As a result, "
                                + "ths agent may not work correctly during the course.<br/>Would you like to update the file to be "
                                + "compatible with GIFT?", "Update File", new OkayCancelCallback() {

                                    @Override
                                    public void okay() {
                                        validateMediaSemantics(filePath, true);
                                    }

                                    @Override
                                    public void cancel() {
                                        //Nothing to do
                                    }
                        });
                    } else if(updateInvalidFiles){
                        WarningDialog.info("Update Successful", "The file was updated successfully!");
                    }
                }
            }

        });
    }

    /**
     * Specifies whether or not the editor is allowed to edit the currently
     * loaded {@link Feedback}.
     *
     * @param isReadonly True if the editor should NOT be allowed to edit, false
     *        if the editor should be allowed to edit.
     */
    public void setReadonly(boolean isReadonly) {
        typeChoiceRibbon.setReadonly(isReadonly);
        messageEditor.setReadonly(isReadonly);
        mp3FileButton.setEnabled(!isReadonly);
        oggFileButton.setEnabled(!isReadonly);
        chooseAvatarBtn.setEnabled(!isReadonly);
        speechKeyListBox.setEnabled(!isReadonly);
        audioDelayButton.setReadOnly(isReadonly);
        audioDelayTimeBox.setEnabled(!isReadonly);
        feedbackHTMLFileButton.setEnabled(!isReadonly);
        teamPicker.setReadonly(isReadonly);
        sendToObserverControllerButton.setEnabled(!isReadonly);
        environmentalStressCategoryButton.setEnabled(!isReadonly);
        cognitiveStressCategoryButton.setEnabled(!isReadonly);
        physiologicalStressCategoryButton.setEnabled(!isReadonly);
    }
    
    /**
     * Refreshes the UI to match the state of the current display properties
     * 
     * @param displaySessionProperties the display properties. Can be null;
     */
    private void refreshDisplayProperties(DisplaySessionProperties displaySessionProperties) {
        
        if(displaySessionProperties != null && BooleanEnum.TRUE.equals(displaySessionProperties.getRequestUsingSessionState())){
            
            /* Disable editing the URL since the strategy provider should provide it instead */
            String providerUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.EXTERNAL_STRATEGY_PROVIDER_URL);
            if(StringUtils.isNotBlank(providerUrl)) {
                messageEditor.setImmutableMessage("This message will be provided by " + providerUrl);
                
            } else {
                messageEditor.setImmutableMessage("This message will be provided by an external strategy provider");
            }
            
        } else {
            
            /* Enable editing the message when the strategy provider is not being used */
            messageEditor.setImmutableMessage(null);
        }
    }
}