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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.BooleanEnum;
import generated.dkf.Media;
import generated.dkf.SlideShowProperties;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileRequest;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.NotifyUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.FileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.SetNameDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShow;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShowResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;

/**
 * Allows creating or editing a slide show media item.
 * 
 * @author sharrison
 */
public class SlideShowMediaWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SlideShowMediaWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SlideShowMediaWidgetUiBinder uiBinder = GWT.create(SlideShowMediaWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SlideShowMediaWidgetUiBinder extends UiBinder<Widget, SlideShowMediaWidget> {
    }

    /** the name of the folder under the course folder where slide show folders will be created */
    private static final String SLIDE_SHOW_FOLDER_NAME = "Slide Shows";

    /** The media title textbox */
    @UiField
    protected TextBox mediaTitleTextbox;

    /** The deck to switch between panels */
    @UiField
    protected DeckPanel deckPanel;

    /** The panel that contains components to select a ppt */
    @UiField
    protected FocusPanel selectPptPanel;

    /** The label for the selected ppt */
    @UiField
    protected Label selectPptLabel;

    /** The panel for when a ppt is selected */
    @UiField
    protected FlowPanel pptSelectedPanel;

    /** The label for the slide number */
    @UiField
    protected Label slideNumberLabel;

    /** The button to replace the slide show */
    @UiField
    protected Button replaceSlideShowButton;

    /** The button to remove the slide show */
    @UiField
    protected Button removePptButton;

    /** The panel contains the ppt options */
    @UiField
    protected DisclosurePanel optionsPanel;

    /** Checkbox for allowing the user to view previous slides */
    @UiField
    protected CheckBox previousCheckbox;

    /**
     * Checkbox for allowing the user to continue to the next course object without finishing the
     * ppt
     */
    @UiField
    protected CheckBox continueCheckbox;

    /** A warning to display to the author */
    @UiField
    protected HTMLPanel warning;

    /** Flag to indicate if we are replacing a slide show */
    private boolean replaceSlideShow = false;

    /** A modal used to track the progress of creating slide shows */
    private FileOperationProgressModal progressModal = new FileOperationProgressModal(ProgressType.SLIDE_SHOW);

    /** The dialog used to select a powerpoint */
    private DefaultGatFileSelectionDialog pptFileSelectionDialog = new DefaultGatFileSelectionDialog();

    /** The media that is currently being edited */
    private Media currentMedia;

    /** Validation used to handle entering the name for a media item */
    private final WidgetValidationStatus nameValidation;

    /** Validation used to handle selecting a slide show */
    private final WidgetValidationStatus slideShowValidation;

    /**
     * Creates a new editor for modifying feedback items
     */
    public SlideShowMediaWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        nameValidation = new WidgetValidationStatus(mediaTitleTextbox,
                "No title has been given to this media. Enter a title to be shown alongside this media.");

        slideShowValidation = new WidgetValidationStatus(selectPptPanel,
                "No PowerPoint show has been selected. Select the PowerPoint show that should be presented to the learner as a slide show.");

        mediaTitleTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String newName = event.getValue();
                currentMedia.setName(newName == null ? null : newName.trim());
                requestValidation(nameValidation);
            }
        });

        initSlideShow();
    }

    /** Initializes the panel */
    private void initSlideShow() {

        continueCheckbox.setValue(true);
        previousCheckbox.setValue(true);

        if (ScenarioClientUtility.isReadOnly()) {
            selectPptLabel.setText("No slide show selected");
            selectPptLabel.setTitle("Slide shows cannot be uploaded since the course is in read-only mode.");
            selectPptPanel.getElement().getStyle().setProperty("cursor", "not-allowed");
            continueCheckbox.setEnabled(false);
            previousCheckbox.setEnabled(false);
            replaceSlideShowButton.setEnabled(false);
            removePptButton.setVisible(false);
            replaceSlideShowButton.addStyleName("buttonDisabled");

        } else {

            String[] supportedPpt = {Constants.ppt_show_supported_types[0], Constants.ppt_show_supported_types[1], Constants.ppt_show_supported_types[3]};
            pptFileSelectionDialog
                    .setIntroMessageHTML("<span style=\"font-size: 15px; margin-left: 10px; margin-top: 4px;\">"
                            + "Choose a PowerPoint show file to convert to a GIFT slideshow <b>(" + supportedPpt[0]
                            + ")</b>");
            pptFileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
            pptFileSelectionDialog.getFileSelector().setAllowedFileExtensions(supportedPpt);
            pptFileSelectionDialog.setAdditionalFileExtensionInfo(
                    "<br/>You may need to do the following: <br/>" + "1. Edit the presentation in PowerPoint<br/>"
                            + "2. Click 'Save As'<br/>" + "3. Select <b>.pps</b> under 'Save as type'");

        }

        replaceSlideShowButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pptFileSelectionDialog.center();
                replaceSlideShow = true;
            }
        });

        pptFileSelectionDialog.setUploadHandler(new CanHandleUploadedFile() {

            @Override
            public void handleUploadedFile(final String uploadFilePath, final String fileName, final HandleUploadedFileCallback callback) {

                CreateSlideShow action = new CreateSlideShow();
                action.setPptFilePath(uploadFilePath);
                action.setUsername(GatClientUtility.getUserName());
                action.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
                action.setReplaceExisting(replaceSlideShow);

                if (currentMedia.getUri() != null
                        && currentMedia.getMediaTypeProperties() instanceof SlideShowProperties) {

                    // the existing media object is already a slide show, so the transition name
                    // must be the slide folder name
                    FileTreeModel firstSlideModel = FileTreeModel.createFromRawPath(currentMedia.getUri());

                    if (firstSlideModel.getParentTreeModel() != null) {
                        action.setCourseObjectName(firstSlideModel.getParentTreeModel().getFileOrDirectoryName());
                    }
                }

                if (action.getCourseObjectName() == null) {

                    // if we couldn't find the existing transition name for the slide show, generate
                    // a new name for the slides folder
                    Date date = new Date();
                    DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                    action.setCourseObjectName("LessonMaterialContent_" + format.format(date));
                }

                final String filename = uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);

                // auto fill the title textbox to help the author, they can always change it
                if (StringUtils.isBlank(mediaTitleTextbox.getValue())) {
                    mediaTitleTextbox.setValue(filename, true);
                }

                // display successful upload message in Notify UI not a dialog
                pptFileSelectionDialog.setMessageDisplay(new DisplaysMessage() {

                    @Override
                    public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                        WarningDialog.warning(title, text, callback);
                    }

                    @Override
                    public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                        Notify.notify("", "'" + filename + "' was converted to a GIFT slide show.", IconType.INFO,
                                NotifyUtil.generateDefaultSettings());
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

                copyOrUploadSlideShow(action, null, callback);
            }

        });

        pptFileSelectionDialog.setCopyFileRequest(new CopyFileRequest() {

            @Override
            public void asyncCopy(final FileTreeModel source, final CopyFileCallback callback) {

                CreateSlideShow action = new CreateSlideShow();
                action.setUsername(GatClientUtility.getUserName());
                action.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
                action.setReplaceExisting(replaceSlideShow);
                action.setPptFilePath(source.getRelativePathFromRoot());

                if (currentMedia != null) {

                    if (currentMedia.getUri() != null
                            && currentMedia.getMediaTypeProperties() instanceof SlideShowProperties) {

                        // the existing media object is already a slide show, so the transition name
                        // must be the slide folder name
                        FileTreeModel firstSlideModel = FileTreeModel.createFromRawPath(currentMedia.getUri());

                        if (firstSlideModel.getParentTreeModel() != null) {
                            action.setCourseObjectName(firstSlideModel.getParentTreeModel().getFileOrDirectoryName());
                        }
                    }
                }

                if (action.getCourseObjectName() == null) {

                    // if we couldn't find the existing transition name for the slide show, generate
                    // a new name for the slides folder
                    Date date = new Date();
                    DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                    action.setCourseObjectName("LessonMaterialContent_" + format.format(date));
                }

                final String filename = source.getFileOrDirectoryName();

                // auto fill the title textbox to help the author, they can always change it
                if (StringUtils.isBlank(mediaTitleTextbox.getValue())) {
                    mediaTitleTextbox.setValue(filename, true);
                }

                // display successful upload message in Notify UI not a dialog
                pptFileSelectionDialog.setMessageDisplay(new DisplaysMessage() {

                    @Override
                    public void showWarningMessage(String title, String text, ModalDialogCallback callback) {
                        WarningDialog.warning(title, text, callback);
                    }

                    @Override
                    public void showInfoMessage(String title, String text, ModalDialogCallback callback) {
                        Notify.notify("", "'" + filename + "' was converted to a GIFT slide show.", IconType.INFO,
                                NotifyUtil.generateDefaultSettings());
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

                copyOrUploadSlideShow(action, callback, null);
            }

        });

        selectPptPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (!ScenarioClientUtility.isReadOnly()) {

                    pptFileSelectionDialog.center();
                }
            }

        });

        removePptButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                OkayCancelDialog.show("Delete Slide Show Images",
                        "Do you wish to <b>permanently delete</b> the slide show images?", "Delete",
                        new OkayCancelCallback() {

                            @Override
                            public void okay() {
                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<String>();

                                // can't use media name to find path to specific course object
                                // slideshow folder, need to dissect image path name.
                                if (currentMedia.getMediaTypeProperties() instanceof SlideShowProperties) {

                                    SlideShowProperties props = (SlideShowProperties) currentMedia
                                            .getMediaTypeProperties();
                                    List<String> images = props.getSlideRelativePath();
                                    if (images == null || images.isEmpty()) {
                                        // nothing to delete because there are no images in the
                                        // generated object
                                        return;
                                    }

                                    String imageOne = images.get(0);
                                    int startIndex = imageOne.indexOf(SLIDE_SHOW_FOLDER_NAME);
                                    if (startIndex == -1) {
                                        // ERROR
                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                "Failed to delete slide show images.",
                                                "The file name of the first of " + images.size()
                                                        + " slide show images doesn't contain the default slide show folder name of "
                                                        + SLIDE_SHOW_FOLDER_NAME
                                                        + ".  This key is needed in order to determine the name of the folder to delete.\n\n1st image file name = "
                                                        + imageOne,
                                                null);
                                        dialog.setDialogTitle("Deletion Failed");
                                        dialog.center();
                                    }

                                    int nameIndex = startIndex + SLIDE_SHOW_FOLDER_NAME.length() + 1;
                                    int endNameIndex = imageOne.indexOf(Constants.FORWARD_SLASH, nameIndex);
                                    String slideshowFolderName = imageOne.substring(nameIndex, endNameIndex);

                                    final String filePath = GatClientUtility.getBaseCourseFolderPath()
                                            + Constants.FORWARD_SLASH + SLIDE_SHOW_FOLDER_NAME + Constants.FORWARD_SLASH
                                            + slideshowFolderName;
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
                                                    if (!result.isSuccess()) {
                                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                                "Failed to delete the file: " + filePath,
                                                                result.getErrorMsg(), result.getErrorStackTrace());
                                                        dialog.setDialogTitle("Deletion Failed");
                                                        dialog.center();
                                                    }
                                                }
                                            });

                                    SlideShowProperties properties = new SlideShowProperties();
                                    currentMedia.setMediaTypeProperties(properties);
                                    currentMedia.setUri(null);
                                    resetPanel(properties);
                                    requestValidation(slideShowValidation);
                                } else {
                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                            "Failed to delete slide show images.",
                                            "There appears to be a logic error in the course creator.  The current media object properties needs to be of type "
                                                    + SlideShowProperties.class + " but are instead of type "
                                                    + currentMedia.getMediaTypeProperties()
                                                    + ".  Therefore the path to the slide show folder to delete can't be deteremined.",
                                            null);
                                    dialog.setDialogTitle("Deletion Failed");
                                    dialog.center();
                                }
                            }

                            @Override
                            public void cancel() {
                            }
                        });
            }

        });

        previousCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue() != null && currentMedia != null
                        && currentMedia.getMediaTypeProperties() instanceof SlideShowProperties) {
                    ((SlideShowProperties) currentMedia.getMediaTypeProperties())
                            .setDisplayPreviousSlideButton((event.getValue()) ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });

        continueCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue() != null && currentMedia != null
                        && currentMedia.getMediaTypeProperties() instanceof SlideShowProperties) {
                    ((SlideShowProperties) currentMedia.getMediaTypeProperties())
                            .setKeepContinueButton((event.getValue()) ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                }
            }
        });
    }

    /**
     * Performs a request to the server to create the slide show file. If successful, the slide
     * paths are copied to the Slide Show course object properties.
     * 
     * @param action The action to perform
     * @param copyCallback The callback to execute if an existing slide show is being copied or null
     *        if this is an upload operation.
     * @param uploadCallback The callback to execute if a new slide show is being uploaded or null
     *        if this is a copy operation.
     */
    private void copyOrUploadSlideShow(final CreateSlideShow action, final CopyFileCallback copyCallback,
            final HandleUploadedFileCallback uploadCallback) {

        progressModal.startPollForProgress();
        pptFileSelectionDialog.hide();

        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<CreateSlideShowResult>() {

            @Override
            public void onFailure(Throwable caught) {

                progressModal.stopPollForProgress(true);
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "A server error occured while creating the slide show.",
                        "The action failed on the server: " + caught.getMessage(), null);
                dialog.setText("Error");
                dialog.center();

                if (copyCallback != null) {
                    copyCallback.onFailure(caught);

                } else {
                    uploadCallback.onFailure(caught);
                }

            }

            @Override
            public void onSuccess(final CreateSlideShowResult result) {

                progressModal.stopPollForProgress(!result.isSuccess());

                if (result.isSuccess()) {

                    SlideShowProperties properties = new SlideShowProperties();
                    Serializable props = currentMedia.getMediaTypeProperties();
                    if (props != null && props instanceof SlideShowProperties) {
                        // Use the existing properties to retain continue & previous checkbox values

                        properties = (SlideShowProperties) props;

                        // clear the previous in memory list of slides in the slides folder
                        // in order to re-populate the list with the latest from the server
                        properties.getSlideRelativePath().clear();

                        if (properties.getDisplayPreviousSlideButton() == null) {
                            // Set the values if they haven't been initialized already

                            properties.setDisplayPreviousSlideButton(BooleanEnum.TRUE);
                            properties.setKeepContinueButton(BooleanEnum.TRUE);
                        }
                    }

                    for (String path : result.getRelativeSlidePaths()) {
                        properties.getSlideRelativePath().add(path);
                    }

                    currentMedia.setMediaTypeProperties(properties);
                    currentMedia.setUri(result.getRelativeSlidePaths().get(0));
                    resetPanel(properties);

                    requestValidation(slideShowValidation);

                    if (copyCallback != null) {
                        copyCallback.onSuccess(result.getSlidesFolderModel());

                    } else {
                        uploadCallback.onSuccess(result.getSlidesFolderModel());
                    }
                } else if (result.getHasNameConflict()) {

                    final SetNameDialog renameDialog = new SetNameDialog("The Slide Show Already Exists",
                            "A Slide Show with the name <b>" + result.getNameConflict()
                                    + "</b> already exists. Please enter a new name for the Slide Show.",
                            "Rename Slide Show");

                    renameDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            if (event.getValue() != null && !event.getValue().isEmpty()
                                    && !event.getValue().equals(result.getNameConflict())) {
                                action.setCourseObjectName(event.getValue());
                                copyOrUploadSlideShow(action, copyCallback, uploadCallback);
                                renameDialog.hide();
                            }
                        }

                    });

                    renameDialog.center();

                } else {
                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(),
                            result.getErrorStackTrace());
                    dialog.setText("Error");
                    dialog.center();

                    if (copyCallback != null) {
                        copyCallback.onFailure(result.getErrorMsg());
                    } else {
                        uploadCallback.onFailure(result.getErrorMsg());
                    }
                }
            }

        });
    }

    /**
     * Load the media panel for a specific type of media type.
     * 
     * @param media contains the media type information to use to select the type of authoring panel
     *        to show. Can't be null. Must be of type {@link SlideShowProperties}.
     */
    public void edit(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        } else if (!(media.getMediaTypeProperties() instanceof SlideShowProperties)) {
            throw new IllegalArgumentException(
                    "The parameter 'media.getMediaTypeProperties()' must be of type 'SlideShowProperties'.");
        }

        currentMedia = media;

        resetPanel((SlideShowProperties) media.getMediaTypeProperties());
        validateAll();
    }

    /**
     * Resets the panel with the provided properties
     * 
     * @param properties the {@link SlideShowProperties}. Can't be null.
     */
    public void resetPanel(SlideShowProperties properties) {
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

        if (properties.getSlideRelativePath() != null && !properties.getSlideRelativePath().isEmpty()) {
            slideNumberLabel.setText(properties.getSlideRelativePath().size() + " Slides");
            deckPanel.showWidget(deckPanel.getWidgetIndex(pptSelectedPanel));
            warning.getElement().getStyle().setProperty("color", "rgb(68, 68, 68)");

            boolean showContinue = (properties.getKeepContinueButton() == null
                    || properties.getKeepContinueButton() == BooleanEnum.TRUE);
            boolean showPrevious = (properties.getDisplayPreviousSlideButton() == null
                    || properties.getDisplayPreviousSlideButton() == BooleanEnum.TRUE);

            continueCheckbox.setValue(showContinue, true);
            previousCheckbox.setValue(showPrevious, true);

        } else {
            deckPanel.showWidget(deckPanel.getWidgetIndex(selectPptPanel));
            warning.getElement().getStyle().setProperty("color", "#da0000");
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(slideShowValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(StringUtils.isNotBlank(mediaTitleTextbox.getValue()));

        } else if (slideShowValidation.equals(validationStatus)) {
            boolean isCorrectPropertyType = currentMedia.getMediaTypeProperties() instanceof SlideShowProperties;
            slideShowValidation.setValidity(!isCorrectPropertyType || StringUtils.isNotBlank(currentMedia.getUri()));
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
        replaceSlideShowButton.setEnabled(!isReadonly);
        removePptButton.setEnabled(!isReadonly);
        previousCheckbox.setEnabled(!isReadonly);
        continueCheckbox.setEnabled(!isReadonly);
        mediaTitleTextbox.setEnabled(!isReadonly);
    }
}