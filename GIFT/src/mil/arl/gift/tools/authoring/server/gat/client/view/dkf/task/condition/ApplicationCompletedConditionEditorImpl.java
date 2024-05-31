/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ApplicationCompletedCondition;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils;

/**
 * The condition impl for ApplicationCompleted.
 */
public class ApplicationCompletedConditionEditorImpl extends ConditionInputPanel<ApplicationCompletedCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ApplicationCompletedConditionEditorImpl.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ApplicationCompletedConditionEditorUiBinder uiBinder = GWT
            .create(ApplicationCompletedConditionEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ApplicationCompletedConditionEditorUiBinder
            extends UiBinder<Widget, ApplicationCompletedConditionEditorImpl> {
    }

    /** The ideal completion duration */
    @UiField
    protected FormattedTimeBox idealCompletionDuration;

    /** The panel that contains a note to the author about authoring a 0s delay */
    @UiField
    protected HTMLPanel notePanel;

    /**
     * Constructor
     */
    public ApplicationCompletedConditionEditorImpl() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ApplicationCompletedConditionEditorImpl()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        idealCompletionDuration.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                getInput().setIdealCompletionDuration(idealCompletionDuration.getValueAsText());
                notePanel.setVisible(event.getValue() == 0);
            }
        });
    }

    @Override
    protected void onEdit() {
        if (StringUtils.isBlank(getInput().getIdealCompletionDuration())) {
            getInput().setIdealCompletionDuration("00:00:00");
        }

        try {
            int timeInSeconds = FormattedTimeBox.getTimeFromString(getInput().getIdealCompletionDuration());
            idealCompletionDuration.setValue(timeInSeconds);
        } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
            // input completion duration isn't a valid integer
            idealCompletionDuration.setValue(0);
        }

        notePanel.setVisible(idealCompletionDuration.getValue() == 0);
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
        idealCompletionDuration.setEnabled(!isReadonly);
    }
}