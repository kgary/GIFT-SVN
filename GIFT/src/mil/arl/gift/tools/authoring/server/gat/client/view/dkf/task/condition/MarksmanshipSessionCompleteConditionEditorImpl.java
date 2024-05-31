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
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.MarksmanshipSessionCompleteCondition;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.FormFieldFocusEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.HelpMap.FormFieldEnum;

/**
 * The Class MarksmanshipSessionCompleteConditionEditor.
 */
public class MarksmanshipSessionCompleteConditionEditorImpl
        extends ConditionInputPanel<MarksmanshipSessionCompleteCondition> {

    /** The ui binder. */
    private static MarksmanshipSessionCompleteConditionEditorUiBinder uiBinder = GWT
            .create(MarksmanshipSessionCompleteConditionEditorUiBinder.class);

    /** The expected number of shots text box. */
    @UiField
    protected TextBox expectedNumberOfShotsTextBox;

    /**
     * The Interface MarksmanshipSessionCompleteConditionEditorUiBinder.
     */
    interface MarksmanshipSessionCompleteConditionEditorUiBinder
            extends UiBinder<Widget, MarksmanshipSessionCompleteConditionEditorImpl> {
    }

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public MarksmanshipSessionCompleteConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        setupDirtyHandlers();
        setupHelpHandlers();

        /* If the user changes the MarksmanshipSessionCompleteCondition's exptedNumberOfShots */
        expectedNumberOfShotsTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getInput().setExpectedNumberOfShots(event.getValue());
            }
        });
    }

    /**
     * Setup dirty handlers.
     */
    protected void setupDirtyHandlers() {
        expectedNumberOfShotsTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
            }
        });
    }

    /**
     * Setup help handlers.
     */
    protected void setupHelpHandlers() {
        expectedNumberOfShotsTextBox.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent arg0) {
                SharedResources.getInstance().getEventBus().fireEvent(new FormFieldFocusEvent(
                        FormFieldEnum.MARKSMANSHIP_SESSION_COMPLETE_CONDITION_EXPECTED_NUMBER_OF_SHOTS));
            }
        });
        expectedNumberOfShotsTextBox.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent arg0) {
                SharedResources.getInstance().getEventBus().fireEvent(new FormFieldFocusEvent(
                        FormFieldEnum.MARKSMANSHIP_SESSION_COMPLETE_CONDITION_EXPECTED_NUMBER_OF_SHOTS));
            }
        });
    }

    @Override
    protected void onEdit() {
        // Set the expected number of shots
        String expectedNumberOfShots = getInput().getExpectedNumberOfShots();
        expectedNumberOfShotsTextBox.setValue(expectedNumberOfShots);
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
        expectedNumberOfShotsTextBox.setEnabled(!isReadonly);
    }
}
