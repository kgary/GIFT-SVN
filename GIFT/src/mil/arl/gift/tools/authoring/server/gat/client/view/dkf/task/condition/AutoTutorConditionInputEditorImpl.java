/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ATRemoteSKO;
import generated.dkf.ATRemoteSKO.URL;
import generated.dkf.AutoTutorConditionInput;
import generated.dkf.AutoTutorSKO;
import generated.dkf.LocalSKO;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.RealTimeAssessmentPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

/**
 * The Class AutoTutorConditionInputEditor.
 */
public class AutoTutorConditionInputEditorImpl extends ConditionInputPanel<AutoTutorConditionInput> {

    /** The ui binder. */
    private static AutoTutorConditionInputEditorUiBinder uiBinder = GWT.create(AutoTutorConditionInputEditorUiBinder.class);

    /** The panel used to select a SKO file */
    @UiField
    protected RealTimeAssessmentPanel skoSelectPanel;
	
	/** The file selection dialog*/
	private DefaultGatFileSelectionDialog fileSelectionDialog = new DefaultGatFileSelectionDialog();

    /**
     * The Interface AutoTutorConditionInputEditorUiBinder.
     */
    interface AutoTutorConditionInputEditorUiBinder extends UiBinder<Widget, AutoTutorConditionInputEditorImpl> {
    }

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public AutoTutorConditionInputEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        initFileDialog();
        skoSelectPanel.hideEditButton();
        hideSkoFileLabel();
    }

    /**
     * Initializes the parameters on {@link #fileSelectionDialog} for use by the
     * {@link AutoTutorConditionInputEditorImpl}.
     */
    private void initFileDialog() {
        fileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String filePath = event.getValue();

                if (getInput().getAutoTutorSKO() == null) {
                    getInput().setAutoTutorSKO(new AutoTutorSKO());
                }

                LocalSKO localSko = (LocalSKO) getInput().getAutoTutorSKO().getScript();

                if (localSko == null) {
                    localSko = new LocalSKO();
                    getInput().getAutoTutorSKO().setScript(localSko);
                }

                localSko.setFile(filePath);

                if (filePath != null && !filePath.isEmpty()) {
                    showSkoFileLabel(filePath);
                } else {
                    hideSkoFileLabel();
                }
            }
        });

        fileSelectionDialog.getFileSelector().setAllowedFileExtensions(new String[]{AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION});
		fileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
    }
    
    /**
     * Returns the 'add' button for the real-time assessment
     * 
     * @return the add assessment button
     */
    public HasClickHandlers getSkoFileButton() {
        return skoSelectPanel.getAddAssessmentButton();
    }

    /**
     * Shows the file label
     * 
     * @param path the path of the real-time assessment
     */
    public void showSkoFileLabel(String path) {
        skoSelectPanel.setAssessment(path);
    }

    /**
     * Hides the file label
     */
    public void hideSkoFileLabel() {
        skoSelectPanel.removeAssessment();
    }

    /**
     * Returns the file selector dialog
     * 
     * @return the dialog to select the real-time assessment file
     */
    public FileSelectionView getFileSelector() {
        return fileSelectionDialog.getFileSelector();
    }

    /**
     * Sets the message to be displayed for the file selection dialog
     * 
     * @param msg the introduction message to display
     */
    public void setFileSelectionDialogIntroMessage(String msg) {
        fileSelectionDialog.setIntroMessageHTML(msg);
    }

    /**
     * Toggles the visibility of the file selection dialog
     * 
     * @param visible true to show the dialog; false to hide it
     */
    public void setFileSelectionDialogVisible(boolean visible) {
        if (visible) {
            fileSelectionDialog.center();
        } else {
            fileSelectionDialog.hide();
        }
    }

    /**
     * Gets the file selection dialog
     * 
     * @return the file selection dialog
     */
    public HasValue<String> getFileSelectionDialog() {
        return fileSelectionDialog;
    }

    /**
     * Returns the 'remove' button for the real-time assessment
     * 
     * @return the remove assessment button
     */
    public HasClickHandlers getRemoveSkoButton() {
        return skoSelectPanel.getDeleteButton();
    }

    @Override
    protected void onEdit() {
        AutoTutorSKO atSko = getInput().getAutoTutorSKO();
        if (atSko.getScript() instanceof LocalSKO) {
            LocalSKO localSko = (LocalSKO) atSko.getScript();
            String filePath = localSko.getFile();
            fileSelectionDialog.setValue(filePath);

            if (filePath != null && !filePath.isEmpty()) {
                showSkoFileLabel(filePath);
            } else {
                hideSkoFileLabel();
            }
        } else if (atSko.getScript() instanceof ATRemoteSKO) {
            ATRemoteSKO remoteSko = (ATRemoteSKO) atSko.getScript();
            URL url = remoteSko.getURL();

            if (url != null && url.getAddress() != null) {
                WarningDialog.error("Failed to load AutoTutor configuration.",
                        "It appears that this AutoTutor was originally configured with a remote SKO file hosted from the following location:"
                                + "<br/><b>"
                                + url.getAddress()
                                + "</b><br/><br/>"
                                + "For security reasons, GIFT no longer supports configuring AutoTutor with remotely hosted SKO files. To continue,"
                                + "using this SKO, please download it from its hosted location and upload it to your course so that it can be accessed"
                                + "securely.");
            } else {
                WarningDialog.error("Failed to load AutoTutor configuration.",
                        "It appears that this AutoTutor was originally configured with a remote SKO file hosted from an external location."
                                + "<br/><br/>"
                                + "For security reasons, GIFT no longer supports configuring AutoTutor with remotely hosted SKO files. To continue "
                                + "using this SKO, please download it from its hosted location and upload it to your course so that it can be accessed "
                                + "securely.");
            }

            getInput().getAutoTutorSKO().setScript(new LocalSKO());
        }
    }
    
    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }
    
    @Override
    protected void setReadonly(boolean isReadonly) {
        skoSelectPanel.setReadOnlyMode(isReadonly);
    }
}