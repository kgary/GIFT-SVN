/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.KnowledgeSessionListWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.KnowledgeSessionListWidget.SessionSelectedCallback;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

/**
 * A dialog used to select an assessment from a domain session log.
 * 
 * @author sharrison
 */
public class LogAssessmentPickerDialog extends ModalDialogBox implements HasValue<LogMetadata> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LogAssessmentPickerDialog.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static AssessmentPickerDialogUiBinder uiBinder = GWT.create(AssessmentPickerDialogUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AssessmentPickerDialogUiBinder extends UiBinder<Widget, LogAssessmentPickerDialog> {
    }

    /** Dialog title */
    private static final String DIALOG_TITLE = "Select Assessment";

    /** The list of assessments from the log file */
    @UiField(provided = true)
    protected KnowledgeSessionListWidget logAssessmentList = new KnowledgeSessionListWidget(null,
            new SessionSelectedCallback() {
                @Override
                protected void sessionSelected(AbstractKnowledgeSession knowledgeSession) {
                    confirmButton.setEnabled(true);
                }
            });

    /** Whether this dialog is in read only mode */
    private boolean isReadOnly = false;

    /** The confirm button */
    private Button confirmButton = new Button("Select");

    /** The cancel button */
    private Button cancelButton = new Button("Cancel");

    /** The map of knowledge sessions to their log metadata */
    private final Map<AbstractKnowledgeSession, LogMetadata> sessionToLogMetadataMap = new LinkedHashMap<>();

    /**
     * Creates a new dialog for selecting an assessment from the log options.
     */
    public LogAssessmentPickerDialog() {
        setWidget(uiBinder.createAndBindUi(this));

        setGlassEnabled(true);
        setWidth("750px");

        setText(DIALOG_TITLE);

        setEnterButton(confirmButton);

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                logAssessmentList.clearSelection();
                hide();
            }
        });

        confirmButton.setType(ButtonType.PRIMARY);
        confirmButton.setEnabled(false);
        cancelButton.setType(ButtonType.DANGER);

        FlowPanel footer = new FlowPanel();
        footer.add(confirmButton);
        footer.add(cancelButton);

        setFooterWidget(footer);

        confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isReadOnly) {
                    // in case they changed the DOM to click the button
                    return;
                }

                final LogMetadata selection = getValue();
                if (selection == null) {
                    WarningDialog.alert("Missing assessment", "Please select an assessment.");
                    return;
                }

                ValueChangeEvent.fire(LogAssessmentPickerDialog.this, selection);
                hide();
            }
        });
    }

    /**
     * Populate the options for this dialog.
     *
     * @param assessmentOptions the assessments to populate this dialog. Can't
     *        be null or empty.
     */
    public void populateOptions(List<LogMetadata> assessmentOptions) {
        if (CollectionUtils.isEmpty(assessmentOptions)) {
            throw new IllegalArgumentException("The parameter 'assessmentOptions' cannot be null or empty.");
        }

        /* Disable confirm button until an option has been selected */
        confirmButton.setEnabled(false);

        sessionToLogMetadataMap.clear();
        logAssessmentList.clearSessions();

        for (LogMetadata metadata : assessmentOptions) {
            sessionToLogMetadataMap.put(metadata.getSession(), metadata);
            logAssessmentList.addSession(metadata, null);
        }
    }

    /**
     * Checks if this dialog has been populated.
     * 
     * @return true if the dialog contains at least one option; false otherwise.
     */
    public boolean isPopulated() {
        return CollectionUtils.isNotEmpty(sessionToLogMetadataMap);
    }

    @Override
    public void center() {
        if (!isPopulated()) {
            /* Developer Error! This dialog needs to be populated with options
             * before it can be used. */
            logger.severe("Attempted to open the " + this.getClass().getSimpleName() + " before it was populated.");

            return;
        } else if (sessionToLogMetadataMap.size() == 1) {
            /* Automatically select the only option */
            setValue(sessionToLogMetadataMap.values().iterator().next(), true);
            return;
        }

        /* Show dialog */
        super.center();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<LogMetadata> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public LogMetadata getValue() {
        final AbstractKnowledgeSession selection = logAssessmentList.getSelection();
        return sessionToLogMetadataMap.get(selection);
    }

    /**
     * Set the value using the assessment name.
     * 
     * @param assessmentName the assessment name to set. If not found, the
     *        selection will be cleared.
     */
    public void setValue(String assessmentName) {
        logAssessmentList.clearSelection();

        for (AbstractKnowledgeSession session : sessionToLogMetadataMap.keySet()) {
            if (StringUtils.equalsIgnoreCase(session.getNameOfSession(), assessmentName)) {
                setValue(sessionToLogMetadataMap.get(session));
                break;
            }
        }
    }

    @Override
    public void setValue(LogMetadata value) {
        setValue(value, false);
    }

    @Override
    public void setValue(LogMetadata value, boolean fireEvents) {
        if (value == null) {
            throw new IllegalArgumentException("The parameter 'value' cannot be null.");
        }

        logAssessmentList.setSelectedItem(value.getSession());

        if (fireEvents) {
            ValueChangeEvent.fire(LogAssessmentPickerDialog.this, value);
        }
    }

    /**
     * Set whether the GAT is in read only mode.
     * 
     * @param readOnly whether the GAT is in read only mode. Causes the dialog
     *        to be changed.
     */
    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
        confirmButton.setVisible(!isReadOnly);
    }
}
