/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Media;
import generated.dkf.WebpageProperties;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CanGetRootDirectory;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileRequest;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.gwt.client.widgets.file.GetRootDirectoryCallback;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.NotifyUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.LoadedFileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;

/**
 * Allows creating or editing a Local Webpage media item.
 * 
 * @author sharrison
 */
public class LocalWebpageMediaWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LocalWebpageMediaWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static LocalWebpageMediaWidgetUiBinder uiBinder = GWT.create(LocalWebpageMediaWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface LocalWebpageMediaWidgetUiBinder extends UiBinder<Widget, LocalWebpageMediaWidget> {
    }

    /** Label to be displayed when there is no file selected */
    private static final String NO_FILE_LABEL = "No File Selected";

    /** The file extension for archive (ZIP) files */
    private static final String ZIP = ".zip";

    /** The instructions to display in the file selection dialog used for Local Webpage objects */
    private static final String LOCAL_WEBPAGE_FILE_SELECTION_INTRUCTIONS = " Select a web page file.<br> Supported extensions are :<b>"
            + Constants.html_supported_types + ".</b><br/><br/>"
            + "You can also select a <b>.zip</b> file containing a web page file and its resources (e.g. style sheets, scripts, "
            + "images, etc.) in order to load them simultaneously. This can be helpful when loading web pages with many dependencies.";

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

    /** The media title textbox */
    @UiField
    protected TextBox mediaTitleTextbox;

    /** The deck panel that allows for toggling other panels */
    @UiField
    protected DeckPanel deckPanel;

    /** The panel to be displayed when no webpage is selected */
    @UiField
    protected FocusPanel selectLocalWebpagePanel;

    /** The panel to be displayed when a webpage is selected */
    @UiField
    protected Widget localWebpageSelectedPanel;

    /** The button to remove the selected webpage */
    @UiField
    protected Button removeLocalWebpageButton;

    /** The file label to display when a webpage is selected */
    @UiField
    protected Label localWebpageFileLabel;

    /** A dialog used to select local webpage files */
    private DefaultGatFileSelectionDialog webpageFileDialog = new DefaultGatFileSelectionDialog();
    
    /** A modal used to track the progress of unzipping operations */
    private LoadedFileOperationProgressModal<UnzipFileResult> unzipProgressModal = new LoadedFileOperationProgressModal<UnzipFileResult>(
            ProgressType.UNZIP);

    /** The path to the course folder */
    private final String courseFolderPath;

    /** The media that is currently being edited */
    private Media currentMedia;

    /** The read only flag */
    private boolean readOnly = false;

    /** Validation used to handle entering the name for a media item */
    private final WidgetValidationStatus nameValidation;

    /** Validation used to handle selecting webpage */
    private WidgetValidationStatus webpageValidation;

    /**
     * Creates a new editor for modifying feedback items
     */
    public LocalWebpageMediaWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        nameValidation = new WidgetValidationStatus(mediaTitleTextbox,
                "No title has been given to this media. Enter a title to be shown alongside this media.");

        webpageValidation = new WidgetValidationStatus(selectLocalWebpagePanel,
                "No local web page has been selected. Select the web page that should be presented to the learner.");

        mediaTitleTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String newName = event.getValue();
                currentMedia.setName(newName == null ? null : newName.trim());
                requestValidation(nameValidation);
            }
        });

        String currentCoursePath = GatClientUtility.getBaseCourseFolderPath();
        String courseName = GatClientUtility.getCourseFolderName(currentCoursePath);
        courseFolderPath = currentCoursePath.substring(0, currentCoursePath.indexOf(courseName) + courseName.length());

        initLocalWebpage();
    }

    /** Initializes the panel */
    private void initLocalWebpage() {

        // allow authors to upload HTML files or ZIP archives containing them
        webpageFileDialog.setAllowedFileExtensions(LOCAL_WEBPAGE_ALLOWED_FILE_EXTENSIONS);
        webpageFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        webpageFileDialog.setIntroMessageHTML(LOCAL_WEBPAGE_FILE_SELECTION_INTRUCTIONS);

        // add a handler to allow the webpage file selection dialog to be shown by clicking a button
        selectLocalWebpagePanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                webpageFileDialog.center();
            }
        });

        // add a handler to allow authors to delete local webpages by clicking a button
        removeLocalWebpageButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (readOnly) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + localWebpageFileLabel.getText()
                        + "' from the course or simply remove the reference to that content in this metadata object?<br><br>"
                        + "Other parts of the course will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void delete() {

                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<String>();
                                final String filePath = courseFolderPath + "/" + localWebpageFileLabel.getText();
                                filesToDelete.add(filePath);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);
                                SharedResources.getInstance().getDispatchService().execute(action,
                                        new AsyncCallback<GatServiceResult>() {

                                            @Override
                                            public void onFailure(Throwable error) {
                                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                        "Failed to delete the file.", error.getMessage(),
                                                        DetailedException.getFullStackTrace(error));
                                                dialog.setDialogTitle("Deletion Failed");
                                                dialog.center();
                                            }

                                            @Override
                                            public void onSuccess(GatServiceResult result) {

                                                if (result.isSuccess()) {
                                                    logger.warning("Successfully deleted the file '" + filePath + "'.");
                                                } else {
                                                    logger.warning("Was unable to delete the file: " + filePath
                                                            + "\nError Message: " + result.getErrorMsg());
                                                }

                                                resetUI();
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                resetUI();
                            }

                            private void resetUI() {

                                if (currentMedia != null) {
                                    currentMedia.setUri(null);
                                    requestValidation(webpageValidation);
                                }

                                localWebpageFileLabel.setText("Select Webpage");
                                deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalWebpagePanel));
                            }

                        }, "Delete Content");
            }
        });

        // set up logic to allow the author to upload either plain HTML files or ZIP archives
        // containing them
        final CanHandleUploadedFile originalUploadHandler = webpageFileDialog.getUploadHandler();

        webpageFileDialog.setUploadHandler(new CanHandleUploadedFile() {

            @Override
            public void handleUploadedFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback) {

                if (uploadFilePath.endsWith(ZIP)) {

                    // ZIP files containing web pages should have their contents extracted to the
                    // course folder
                    UnzipFile action = new UnzipFile(GatClientUtility.getUserName(),
                            GatClientUtility.getBaseCourseFolderPath(), uploadFilePath);

                    final String filename = uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);

                    // display successful upload message in Notify UI not a dialog
                    webpageFileDialog.setMessageDisplay(new DisplaysMessage() {

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
        final CopyFileRequest originalCopyRequest = webpageFileDialog.getCopyFileRequest();

        webpageFileDialog.setCopyFileRequest(new CopyFileRequest() {

            @Override
            public void asyncCopy(final FileTreeModel source, final CopyFileCallback callback) {

                final String filename = source.getFileOrDirectoryName();

                if (filename.endsWith(ZIP)) {

                    // ZIP files containing web pages should have their contents extracted to the
                    // course folder
                    UnzipFile action = new UnzipFile(GatClientUtility.getUserName(),
                            GatClientUtility.getBaseCourseFolderPath(), source.getRelativePathFromRoot());

                    // display successful upload message in Notify UI not a dialog
                    webpageFileDialog.setMessageDisplay(new DisplaysMessage() {

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
        webpageFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

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
                        selectLocalWebpage(event.getValue());
                    }

                } else {
                    selectLocalWebpage(event.getValue());
                }
            }
        });
    }

    /**
     * Load the media panel for a specific type of media type.
     * 
     * @param media contains the media type information to use to select the type of authoring panel
     *        to show. Can't be null. Must be of type {@link WebpageProperties}.
     */
    public void edit(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        } else if (!(media.getMediaTypeProperties() instanceof WebpageProperties)) {
            throw new IllegalArgumentException(
                    "The parameter 'media.getMediaTypeProperties()' must be of type 'WebpageProperties'.");
        }

        currentMedia = media;

        WebpageProperties properties = (WebpageProperties) media.getMediaTypeProperties();
        resetPanel(properties);

        String uri = currentMedia.getUri();
        if (StringUtils.isNotBlank(uri)) {
            localWebpageFileLabel.setText(uri);
            deckPanel.showWidget(deckPanel.getWidgetIndex(localWebpageSelectedPanel));
        }

        validateAll();
    }

    /**
     * Resets the panel with the provided properties
     * 
     * @param properties the {@link WebpageProperties}. Can't be null.
     */
    public void resetPanel(WebpageProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("The parameter 'properties' cannot be null.");
        }

        currentMedia.setMediaTypeProperties(properties);

        /* make sure the link text box has a useful value with the given media name */
        if (currentMedia != null && StringUtils.isNotBlank(currentMedia.getName())) {
            mediaTitleTextbox.setValue(currentMedia.getName());
        } else {
            /* otherwise the previous value will be shown which is not what we want when adding a
             * new media */
            mediaTitleTextbox.setValue(null);
        }

        localWebpageFileLabel.setText(NO_FILE_LABEL);
        deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalWebpagePanel));
    }

    /**
     * Selects the web page file with the given file name, updating both the UI and the backing
     * course objects as necessary to reference the web page
     * 
     * @param fileName the path the file from this course's folder
     */
    private void selectLocalWebpage(final String fileName) {

        // update the backing media object
        if (currentMedia != null) {
            currentMedia.setUri(fileName);
            requestValidation(webpageValidation);
        }

        // update the UI showing the file name
        localWebpageFileLabel.setText(fileName != null ? fileName : NO_FILE_LABEL);

        if (fileName != null) {

            final String simpleFileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));

            // auto fill the title textbox to help the author, they can always change it
            if (StringUtils.isBlank(mediaTitleTextbox.getValue())) {
                mediaTitleTextbox.setValue(simpleFileName, true);
            }

            deckPanel.showWidget(deckPanel.getWidgetIndex(localWebpageSelectedPanel));
        }
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

                webpageFileDialog.hide();

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

                                    selectLocalWebpage(parentPath + "/" + firstHtmlFile);
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

                                            selectLocalWebpage(parentPath + "/" + selectedHtmlFile);
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

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(webpageValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(StringUtils.isNotBlank(mediaTitleTextbox.getValue()));

        } else if (webpageValidation.equals(validationStatus)) {
            boolean isCorrectPropertyType = currentMedia.getMediaTypeProperties() instanceof WebpageProperties;
            webpageValidation.setValidity(!isCorrectPropertyType || StringUtils.isNotBlank(currentMedia.getUri()));
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        this.readOnly = isReadonly;

        mediaTitleTextbox.setEnabled(!isReadonly);
        removeLocalWebpageButton.setVisible(!isReadonly);
    }
}