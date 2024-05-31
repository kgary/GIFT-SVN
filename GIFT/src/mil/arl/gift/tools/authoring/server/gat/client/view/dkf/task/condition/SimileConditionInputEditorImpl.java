/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.SIMILEConditionInput;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchSimileConcepts;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchSimileConceptsResult;
import net.customware.gwt.dispatch.client.DispatchAsync;

/**
 * The Class SimileConditionInputEditor.
 */
public class SimileConditionInputEditorImpl extends ConditionInputPanel<SIMILEConditionInput> {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(SimileConditionInputEditorImpl.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SimileConditionInputEditorUiBinder uiBinder = GWT.create(SimileConditionInputEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SimileConditionInputEditorUiBinder extends UiBinder<Widget, SimileConditionInputEditorImpl> {
    }

    /** The panel that selects and displays the SIMILE configuration */
    @UiField
    protected DeckPanel simileConfigPanel;

    /** The panel containing the widgets used for selecting a SIMILE config */
    @UiField
    protected FlowPanel simileConfigSelectPanel;

    /** The panel containing the widgets displaying the SIMILE config */
    @UiField
    protected FlowPanel simileConfigSelectedPanel;

    /** The button used to add a configuration */
    @UiField
    protected Button addConfigButton;

    /** The button that is used to remove a configuration button. */
    @UiField
    protected Button removeConfigButton;

    /** The label that displays the name of the configuration */
    @UiField
    protected HTML configNameLabel;

    /** The panel that contains the {@link #conditionKeySelect} */
    @UiField
    protected FlowPanel conditionKeyPanel;

    /** The condition key value list box. */
    @UiField(provided = true)
    protected ValueListBox<String> conditionKeySelect = new ValueListBox<>(new Renderer<String>() {

        @Override
        public String render(String object) {
            return object != null ? object : "No condition keys defined";
        }

        @Override
        public void render(String object, Appendable appendable) throws IOException {
            appendable.append(render(object));
        }
    });

    /** The file dialog for choosing the simile config file */
    private DefaultGatFileSelectionDialog simileFileDialog = new DefaultGatFileSelectionDialog();

    /** Constant for allowed file extension of simile configuration file types */
    private static final String SIMILE_FILE_EXTENSION = ".ixs";

    /**
     * The container for showing validation messages for the condition not being
     * supported by the current JRE architecture (32-bit vs 64-bit).
     */
    private final ModelValidationStatus supportedJREValidation = new ModelValidationStatus(
            "The SIMILE condition does not support a 64-bit JRE.") {
        @Override
        protected void fireJumpToEvent(Serializable modelObject) {
            ScenarioEventUtility.fireJumpToEvent(modelObject);
        }
    };

    /**
     * The container for showing validation messages for the condition not having a simile file
     * selected.
     */
    private final WidgetValidationStatus configFileValidation;

    /**
     * The container for showing validation messages for the condition not having a condition key
     * selected.
     */
    private final WidgetValidationStatus conditionKeyValidation;

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public SimileConditionInputEditorImpl() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SimileConditionInputEditorImpl()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        supportedJREValidation.setAdditionalInstructions(
                "GIFT is currently running a 64-bit JRE. SIMILE does not support this and cannot be used at this time. Try running GIFT in 32-bit.");

        configFileValidation = new WidgetValidationStatus(addConfigButton,
                "A SIMILE configuration file is required. Please select a '" + SIMILE_FILE_EXTENSION + "' file");

        conditionKeyValidation = new WidgetValidationStatus(conditionKeySelect,
                "A SIMILE condition is required. Please select condition to be assessed.");

        // Enforce that the user can only select simile file types.
        simileFileDialog.setAllowedFileExtensions(new String[] { SIMILE_FILE_EXTENSION });

        /* Add click handler to the 'select a file' button */
        addConfigButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                simileFileDialog.center();
            }
        });

        removeConfigButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getInput().setConfigurationFile(null);
                getInput().setConditionKey(null);
                showSelectPanel();
                
                requestValidationAndFireDirtyEvent(getCondition(), configFileValidation, conditionKeyValidation);
            }
        });

        /* If the user selects a new configuration file */
        simileFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                configNameLabel.setText(event.getValue());
                getInput().setConfigurationFile(event.getValue());
                if (event.getValue() != null) {
                    showSelectedPanel();
                    fetchConditionKeys(event.getValue());
                }
                
                requestValidationAndFireDirtyEvent(getCondition(), configFileValidation, conditionKeyValidation);
            }
        });

        conditionKeySelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getInput().setConditionKey(event.getValue());
                requestValidationAndFireDirtyEvent(getCondition(), conditionKeyValidation);
            }
        });
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        String configurationFile = getInput().getConfigurationFile();
        String conditionKey = getInput().getConditionKey();

        fetchConditionKeys(configurationFile);
        conditionKeySelect.setValue(conditionKey);
        if (configurationFile != null) {
            showSelectedPanel();
        } else {
            showSelectPanel();
        }
    }

    /**
     * Populates the {@link #conditionKeySelect} with the possible keys.
     * 
     * @param configurationFile The name of the SIMILE configuration file. A null value will clear
     *        the choices within the {@link #conditionKeySelect}.
     */
    private void fetchConditionKeys(String configurationFile) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("fetchConditionKeys(" + configurationFile + ")");
        }

        if (configurationFile == null) {
            conditionKeySelect.setAcceptableValues(new ArrayList<String>());
            return;
        }

        String userName = GatClientUtility.getUserName();

        FetchSimileConcepts action = new FetchSimileConcepts();
        action.setConfigurationFilePath(DefaultGatFileSelectionDialog.getCourseFolderPath() + "/" + configurationFile);
        action.setUserName(userName);

        DispatchAsync dispatchService = SharedResources.getInstance().getDispatchService();
        dispatchService.execute(action, new AsyncCallback<FetchSimileConceptsResult>() {

            @Override
            public void onFailure(Throwable throwable) {
                WarningDialog.error("Failed to retrieve condition",
                        "There was an error retrieving the condition implementations.  Please check the log for details. ERROR "
                                + throwable.getLocalizedMessage());
            }

            @Override
            public void onSuccess(FetchSimileConceptsResult result) {

                if (result.isSuccess()) {
                    ArrayList<String> concepts = result.getConcepts();

                    /* If you don't first set a value before setting the acceptable values then NULL
                     * will appear in the list. This seems like a bad API on GWTs side and is
                     * discussed further here: http://stackoverflow.com/questions/11176626/how-
                     * to-remove-null-value-from-valuelistbox-values */
                    String conditionKey = null;
                    if (!concepts.isEmpty()) {
                        if (getInput().getConditionKey() != null && concepts.contains(getInput().getConditionKey())) {
                            conditionKey = getInput().getConditionKey();
                        } else {
                            conditionKey = concepts.get(0);
                        }
                    }

                    conditionKeySelect.setValue(conditionKey, true);
                    conditionKeySelect.setAcceptableValues(concepts);
                } else {
                    String errorMessage;
                    if (ScenarioClientUtility.isJRE64Bit()) {
                        errorMessage = "Unable to retrieve the SIMILE condition implementation because SIMILE does not support 64 bit JREs. To access SIMILE, try running GIFT in 32-bit.";
                    } else {
                        errorMessage = "There was an error retrieving the condition implementations.  Please check the log for details.";
                    }
                    WarningDialog.error("Failed to retrieve condition", errorMessage);
                }
            }
        });
    }

    /**
     * Shows the controls used to select a SIMILE configuration file.
     */
    private void showSelectPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showSelectPanel()");
        }

        simileConfigPanel.showWidget(simileConfigPanel.getWidgetIndex(simileConfigSelectPanel));
        conditionKeyPanel.setVisible(false);
    }

    /**
     * Shows the controls to display a selected SIMILE configuration file.
     */
    private void showSelectedPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showSelectedPanel()");
        }

        configNameLabel.setHTML(getInput().getConfigurationFile());
        simileConfigPanel.showWidget(simileConfigPanel.getWidgetIndex(simileConfigSelectedPanel));
        conditionKeyPanel.setVisible(true);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(supportedJREValidation);
        validationStatuses.add(configFileValidation);
        validationStatuses.add(conditionKeyValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (supportedJREValidation.equals(validationStatus)) {
            supportedJREValidation.setValidity(!ScenarioClientUtility.isJRE64Bit());
        } else if (configFileValidation.equals(validationStatus)) {
            configFileValidation.setValidity(StringUtils.isNotBlank(getInput().getConfigurationFile()));
        } else if (conditionKeyValidation.equals(validationStatus)) {
            conditionKeyValidation.setValidity(
                    !conditionKeyPanel.isVisible() || StringUtils.isNotBlank(conditionKeySelect.getValue()));
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        addConfigButton.setEnabled(!isReadonly);
        removeConfigButton.setEnabled(!isReadonly);
        conditionKeySelect.setEnabled(!isReadonly);
    }
}