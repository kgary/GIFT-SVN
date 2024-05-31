/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common;

import java.math.BigDecimal;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Checkpoint;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.ConditionInputPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An inline editor for an {@link ItemListEditor} that modifies a {@link Checkpoint}.
 * 
 * @author tflowers
 *
 */
public class CheckpointEditor extends ItemEditor<Checkpoint> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(CheckpointEditor.class.getName());

    /** Binder that combines this java class with the ui.xml */
    private static CheckpointEditorUiBinder uiBinder = GWT.create(CheckpointEditorUiBinder.class);

    /** Defines the binder that combines the java class with the ui.xml */
    interface CheckpointEditorUiBinder extends UiBinder<Widget, CheckpointEditor> {
    }

    /** Used to pick the place of interest for the checkpoint */
    @UiField(provided = true)
    protected PlaceOfInterestPicker placePicker = new PlaceOfInterestPicker(Point.class);

    /** Used to pick the time the learner should reach the place of interest */
    @UiField
    protected FormattedTimeBox atTimeBox;

    /** Used to pick the window of time within which the learner can arrive */
    @UiField
    protected FormattedTimeBox windowOfTimeBox;
    
    /** Validates the field used to pick the time the learner should reach the place of interest*/
    private WidgetValidationStatus atTimeValidation;

    /**
     * Creates an initialized {@link CheckpointEditor}.
     * 
     * @param inputPanel the {@link ConditionInputPanel} whose {@link ItemListEditor} this item editor belongs to.
     *        Can't be null.
     */
    public CheckpointEditor(final ConditionInputPanel<?> inputPanel) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CheckpointEditor()");
        }
        
        if (inputPanel == null) {
            throw new IllegalArgumentException("Input panel argument cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        atTimeValidation = new WidgetValidationStatus(atTimeBox, 
                "Please enter a time greater than 0 seconds for how long the learner should take to reach this place.");
        
        atTimeBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                inputPanel.setDirty();
                requestValidation(atTimeValidation);
            }
        });
        
        windowOfTimeBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> arg0) {
                inputPanel.setDirty();
            }
        });
        
        placePicker.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                inputPanel.setDirty();
            }
        });
    }

    @Override
    protected void populateEditor(Checkpoint obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + obj + ")");
        }

        placePicker.setValue(obj.getPoint());
        atTimeBox.setValue(FormattedTimeBox.getTimeFromString(obj.getAtTime()));
        windowOfTimeBox.setValue(obj.getWindowOfTime().intValue());
    }

    @Override
    protected void applyEdits(Checkpoint obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits(" + obj + ")");
        }

        obj.setPoint(placePicker.getValue());
        obj.setAtTime(atTimeBox.getValueAsText());
        obj.setWindowOfTime(new BigDecimal(windowOfTimeBox.getValue()));
        
        //a place of interest reference may have been changed, so update the global list of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(placePicker);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(atTimeValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if(atTimeValidation.equals(validationStatus)) {
            atTimeValidation.setValidity(atTimeBox.getValue() > 0);
        }
    }

    @Override
    protected boolean validate(Checkpoint checkpoint) {
        String errorMsg = ScenarioValidatorUtility.validateCheckpoint(checkpoint);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        placePicker.setReadonly(isReadonly);
        atTimeBox.setEnabled(!isReadonly);
        windowOfTimeBox.setEnabled(!isReadonly);        
    }
}