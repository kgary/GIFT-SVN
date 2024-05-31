/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

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

import generated.dkf.ImageProperties;
import generated.dkf.Media;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;

/**
 * Allows creating or editing an Image media item.
 *
 * @author sharrison
 */
public class ImageMediaWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ImageMediaWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ImageMediaWidgetUiBinder uiBinder = GWT.create(ImageMediaWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ImageMediaWidgetUiBinder extends UiBinder<Widget, ImageMediaWidget> {
    }

    /** Label to be displayed when there is no file selected */
    private static final String NO_FILE_LABEL = "No File Selected";

    /** The media title textbox */
    @UiField
    protected TextBox mediaTitleTextbox;

    /** The deck panel that allows for toggling other panels */
    @UiField
    protected DeckPanel deckPanel;

    /** The panel to be displayed when no image is selected */
    @UiField
    protected FocusPanel selectLocalImagePanel;

    /** The panel to be displayed when a image is selected */
    @UiField
    protected Widget localImageSelectedPanel;

    /** The button to remove the selected image */
    @UiField
    protected Button removeLocalImageButton;

    /** The file label to display when a image is selected */
    @UiField
    protected Label localImageFileLabel;

    /** A dialog used to select image files */
    private DefaultGatFileSelectionDialog imageFileDialog = new DefaultGatFileSelectionDialog();

    /** The media that is currently being edited */
    private Media currentMedia;

    /** The path to the course folder */
    private final String courseFolderPath;

    /** The read only flag */
    private boolean readOnly = false;

    /** Validation used to handle entering the name for a media item */
    private final WidgetValidationStatus nameValidation;

    /** Validation used to handle selecting an image */
    private WidgetValidationStatus imageValidation;

    /**
     * Creates a new editor for modifying feedback items
     */
    public ImageMediaWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        nameValidation = new WidgetValidationStatus(mediaTitleTextbox,
                "No title has been given to this media. Enter a title to be shown alongside this media.");

        // initialize validators once their UI components are ready
        imageValidation = new WidgetValidationStatus(selectLocalImagePanel,
                "No image file has been selected. Select the image file that should be presented to the learner.");

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

        initImage();
    }

    /** Initializes the panel */
    private void initImage() {

        imageFileDialog.setAllowedFileExtensions(Constants.image_supported_types);
        imageFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        imageFileDialog.setIntroMessageHTML(
                " Select an image file.<br> Supported extensions are :<b>" + Constants.image_supported_types + "</b>");

        selectLocalImagePanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                imageFileDialog.center();
            }
        });

        if (readOnly) {
            removeLocalImageButton.setVisible(false);
        }
        removeLocalImageButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (readOnly) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + localImageFileLabel.getText()
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
                                final String filePath = courseFolderPath + "/" + localImageFileLabel.getText();
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
                                    requestValidation(imageValidation);

                                    localImageFileLabel.setText("Select Image");
                                    deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalImagePanel));
                                }
                            }

                        }, "Delete Content");
            }
        });

        imageFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if (currentMedia != null) {
                    currentMedia.setUri(event.getValue());
                    deckPanel.showWidget(deckPanel.getWidgetIndex(localImageSelectedPanel));
                    requestValidation(imageValidation);

                    if (event.getValue() != null) {
                        localImageFileLabel.setText(event.getValue());
                        deckPanel.showWidget(deckPanel.getWidgetIndex(localImageSelectedPanel));

                        final String filename = event.getValue();

                        // auto fill the title textbox to help the author, they can always change it
                        if (StringUtils.isBlank(mediaTitleTextbox.getValue())) {
                            mediaTitleTextbox.setValue(filename, true);
                        }

                    } else {
                        localImageFileLabel.setText("Select Image");
                        deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalImagePanel));
                    }
                }
            }
        });
    }

    /**
     * Load the media panel for a specific type of media type.
     *
     * @param media contains the media type information to use to select the type of authoring panel
     *        to show. Can't be null. Must be of type {@link ImageProperties}.
     */
    public void edit(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        } else if (!(media.getMediaTypeProperties() instanceof ImageProperties)) {
            throw new IllegalArgumentException(
                    "The parameter 'media.getMediaTypeProperties()' must be of type 'ImageProperties'.");
        }

        currentMedia = media;

        ImageProperties properties = (ImageProperties) media.getMediaTypeProperties();
        resetPanel(properties);

        if (StringUtils.isNotBlank(media.getUri())) {
            // Populate the url text
            localImageFileLabel.setText(media.getUri());

            deckPanel.showWidget(deckPanel.getWidgetIndex(localImageSelectedPanel));
        }

        validateAll();
    }

    /**
     * Resets the panel with the provided properties
     *
     * @param properties the {@link ImageProperties}. Can't be null.
     */
    public void resetPanel(ImageProperties properties) {
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

        localImageFileLabel.setText(NO_FILE_LABEL);
        deckPanel.showWidget(deckPanel.getWidgetIndex(selectLocalImagePanel));
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(imageValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(StringUtils.isNotBlank(mediaTitleTextbox.getValue()));

        } else if (imageValidation.equals(validationStatus)) {
            boolean isCorrectPropertyType = currentMedia.getMediaTypeProperties() instanceof ImageProperties;
            imageValidation.setValidity(!isCorrectPropertyType || StringUtils.isNotBlank(currentMedia.getUri()));
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
        removeLocalImageButton.setVisible(!isReadonly);
    }
}