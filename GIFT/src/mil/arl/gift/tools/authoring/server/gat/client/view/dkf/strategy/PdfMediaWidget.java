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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Media;
import generated.dkf.PDFProperties;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
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
 * Allows creating or editing a PDF media item.
 * 
 * @author sharrison
 */
public class PdfMediaWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PdfMediaWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static PdfMediaWidgetWidgetUiBinder uiBinder = GWT.create(PdfMediaWidgetWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface PdfMediaWidgetWidgetUiBinder extends UiBinder<Widget, PdfMediaWidget> {
    }

    /** The default display string for the pdf label */
    private final static String SELECT_PDF_LABEL = "Select PDF";

    /** The media title textbox */
    @UiField
    protected TextBox mediaTitleTextbox;

    /** The deck to switch between panels */
    @UiField
    protected DeckPanel deckPanel;

    /** The panel that contains components to select a PDF */
    @UiField
    protected FocusPanel selectPDFFilePanel;

    /** The label for the selected PDF */
    @UiField
    protected Label selectPDFFileLabel;

    /** The panel for when a PDF is selected */
    @UiField
    protected FlowPanel pdfSelectedPanel;

    /** The button to remove the PDF */
    @UiField
    protected Button removePDFButton;

    /** The label for the PDF file */
    @UiField
    protected Label pdfFileLabel;

    /** A dialog used to select PDF files */
    private DefaultGatFileSelectionDialog pdfFileDialog = new DefaultGatFileSelectionDialog();

    /** Read only flag */
    private boolean readOnly = false;

    /** The course folder path */
    private final String courseFolderPath;

    /** The media that is currently being edited */
    private Media currentMedia;

    /** Validation used to handle entering the name for a media item */
    private final WidgetValidationStatus nameValidation;

    /** Validation used to handle selecting a PDF */
    private final WidgetValidationStatus pdfValidation;

    /**
     * Creates a new editor for modifying feedback items
     */
    public PdfMediaWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        nameValidation = new WidgetValidationStatus(mediaTitleTextbox,
                "No title has been given to this media. Enter a title to be shown alongside this media.");

        pdfValidation = new WidgetValidationStatus(selectPDFFilePanel,
                "No PDF file has been selected. Select the PDF file that should be presented to the learner.");

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

        initPDF();
    }

    /** Initializes the panel */
    private void initPDF() {

        pdfFileDialog.setAllowedFileExtensions(new String[] { ".pdf" });
        pdfFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);

        selectPDFFilePanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                pdfFileDialog.center();
            }
        });

        removePDFButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (readOnly) {
                    // Make sure logic doesn't execute if a user modifies the DOM
                    return;
                }

                DeleteRemoveCancelDialog.show("Delete Content", "Do you wish to <b>permanently delete</b> '"
                        + pdfFileLabel.getText()
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
                                final String filePath = courseFolderPath + "/" + pdfFileLabel.getText();
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
                                    requestValidation(pdfValidation);
                                    pdfFileLabel.setText(SELECT_PDF_LABEL);
                                    deckPanel.showWidget(deckPanel.getWidgetIndex(selectPDFFilePanel));
                                }
                            }

                        }, "Delete Content");
            }
        });

        pdfFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if (currentMedia != null) {

                    currentMedia.setUri(event.getValue());
                    requestValidation(pdfValidation);
                    
                    if (event.getValue() != null) {
                        pdfFileLabel.setText(event.getValue());

                        final String filename = event.getValue();

                        // auto fill the title textbox to help the author, they can always change it
                        if (StringUtils.isBlank(mediaTitleTextbox.getValue())) {
                            mediaTitleTextbox.setValue(filename, true);
                        }

                        deckPanel.showWidget(deckPanel.getWidgetIndex(pdfSelectedPanel));

                    } else {
                        pdfFileLabel.setText(SELECT_PDF_LABEL);
                        deckPanel.showWidget(deckPanel.getWidgetIndex(selectPDFFilePanel));
                    }
                }
            }
        });

    }

    /**
     * Load the media panel for a specific type of media type.
     * 
     * @param media contains the media type information to use to select the type of authoring panel
     *        to show. Can't be null. Must be of type {@link PDFProperties}.
     */
    public void edit(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("The parameter 'media' cannot be null.");
        } else if (!(media.getMediaTypeProperties() instanceof PDFProperties)) {
            throw new IllegalArgumentException(
                    "The parameter 'media.getMediaTypeProperties()' must be of type 'PDFProperties'.");
        }

        currentMedia = media;

        resetPanel((PDFProperties) media.getMediaTypeProperties());

        if (StringUtils.isNotBlank(media.getUri())) {
            pdfFileLabel.setText(media.getUri());
            deckPanel.showWidget(deckPanel.getWidgetIndex(pdfSelectedPanel));
        }

        validateAll();
    }

    /**
     * Resets the panel with the provided properties
     * 
     * @param properties the {@link PDFProperties}. Can't be null.
     */
    public void resetPanel(PDFProperties properties) {
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

        pdfFileLabel.setText(SELECT_PDF_LABEL);
        deckPanel.showWidget(deckPanel.getWidgetIndex(selectPDFFilePanel));
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(pdfValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(StringUtils.isNotBlank(mediaTitleTextbox.getValue()));

        } else if (pdfValidation.equals(validationStatus)) {
            boolean isCorrectPropertyType = currentMedia.getMediaTypeProperties() instanceof PDFProperties;
            pdfValidation.setValidity(!isCorrectPropertyType || StringUtils.isNotBlank(currentMedia.getUri()));
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
        readOnly = isReadonly;

        mediaTitleTextbox.setEnabled(!isReadonly);
        removePDFButton.setVisible(!isReadonly);
    }
}