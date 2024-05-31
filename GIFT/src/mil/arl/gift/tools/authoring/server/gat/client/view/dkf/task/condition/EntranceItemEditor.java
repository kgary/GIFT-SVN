/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigDecimal;
import java.util.Set;

import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Entrance;
import generated.dkf.Inside;
import generated.dkf.Outside;
import generated.dkf.Point;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A widget used to add and edit entrances.
 * 
 * @author sharrison
 */
public class EntranceItemEditor extends ItemEditor<Entrance> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static EntranceItemEditorUiBinder uiBinder = GWT.create(EntranceItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface EntranceItemEditorUiBinder extends UiBinder<Widget, EntranceItemEditor> {
    }

    /** The text value for the placeholder option in the assessment dropdown list */
    private static final String NODE_PLACEHOLDER = "placeholder_value";

    /** TextBox component for setting the entrance name */
    @UiField
    protected TextBox entranceName;

    /** Waypoint picker for the entrance waypoint */
    @UiField(provided = true)
    protected PlaceOfInterestPicker entranceWaypoint = new PlaceOfInterestPicker(Point.class);

    /** The entrance threshold (meters) */
    @UiField(provided = true)
    protected DecimalNumberSpinner entranceThreshold = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);

    /** Waypoint picker for the exit waypoint */
    @UiField(provided = true)
    protected PlaceOfInterestPicker exitWaypoint = new PlaceOfInterestPicker(Point.class);

    /** The exit threshold (meters) */
    @UiField(provided = true)
    protected DecimalNumberSpinner exitThreshold = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);

    /** The assessment dropdown */
    @UiField
    protected Select assessmentSelect;

    /** The container for showing validation messages for not having a entrance name set. */
    private WidgetValidationStatus entranceNameValidationStatus;

    /** The container for showing validation messages for not having an entrance threshold set. */
    private WidgetValidationStatus entranceThresholdValidationStatus;

    /** The container for showing validation messages for not having an exit threshold set. */
    private WidgetValidationStatus exitThresholdValidationStatus;

    /** The container for showing validation messages for not having an assessment set. */
    private WidgetValidationStatus assessmentValidationStatus;

    /**
     * Constructor.
     * 
     * @param inputPanel the {@link ConditionInputPanel} whose {@link ItemListEditor} this item editor belongs to.
     *        Can't be null.
     */
    public EntranceItemEditor(final ConditionInputPanel<?> inputPanel) {
        
        if (inputPanel == null) {
            throw new IllegalArgumentException("Input panel argument cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        entranceNameValidationStatus = new WidgetValidationStatus(entranceName, "The entrance name cannot be empty.");
        entranceThresholdValidationStatus = new WidgetValidationStatus(entranceThreshold,
                "The entrance threshold must be a positive value.");
        exitThresholdValidationStatus = new WidgetValidationStatus(exitThreshold,
                "The exit threshold must be a positive value.");
        assessmentValidationStatus = new WidgetValidationStatus(assessmentSelect, "An assessment must be selected.");

        Option placeholderOption = new Option();
        placeholderOption.setText("Select an assessment");
        placeholderOption.setValue(NODE_PLACEHOLDER);
        placeholderOption.setSelected(true);
        placeholderOption.setEnabled(false);
        placeholderOption.setHidden(true);
        assessmentSelect.add(placeholderOption);
        for (AssessmentLevelEnum assessmentLevel : AssessmentLevelEnum.VALUES()) {
            Option option = new Option();
            option.setText(assessmentLevel.getDisplayName());
            option.setValue(assessmentLevel.getName());
            assessmentSelect.add(option);
        }

        assessmentSelect.setValue(NODE_PLACEHOLDER);
        
        entranceName.addValueChangeHandler(new ValueChangeHandler<String>() { 
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                inputPanel.setDirty();
                requestValidation(entranceNameValidationStatus);
            }
        });
        
        entranceThreshold.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                inputPanel.setDirty();
                requestValidation(entranceThresholdValidationStatus);
            }
        });
        
        exitThreshold.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                inputPanel.setDirty();
                requestValidation(exitThresholdValidationStatus);
            }
        });
        
        assessmentSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                inputPanel.setDirty();
                requestValidation(assessmentValidationStatus);
            }
        });

        entranceWaypoint.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                inputPanel.setDirty();
            }
        });

        exitWaypoint.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                inputPanel.setDirty();
            }
        });
    }

    @Override
    protected void populateEditor(Entrance obj) {
        if (obj == null) {
            throw new IllegalArgumentException("The parameter 'obj' cannot be null.");
        }
        
        entranceName.setValue(obj.getName());

        if (obj.getOutside() == null) {
            obj.setOutside(new Outside());
        }
        entranceWaypoint.setValue(obj.getOutside().getPoint());

        entranceThreshold
                .setValue(obj.getOutside().getProximity() == null ? BigDecimal.ZERO : obj.getOutside().getProximity());

        if (obj.getInside() == null) {
            obj.setInside(new Inside());
        }
        exitWaypoint.setValue(obj.getInside().getPoint());

        exitThreshold
                .setValue(obj.getInside().getProximity() == null ? BigDecimal.ZERO : obj.getInside().getProximity());

        assessmentSelect.setValue(StringUtils.isBlank(obj.getAssessment()) ? NODE_PLACEHOLDER : obj.getAssessment());
    }

    @Override
    protected void applyEdits(Entrance obj) {
        obj.setName(entranceName.getValue());

        if (obj.getOutside() == null) {
            obj.setOutside(new Outside());
        }
        obj.getOutside().setPoint(entranceWaypoint.getValue());

        obj.getOutside()
                .setProximity(entranceThreshold.getValue() == null ? BigDecimal.ZERO : entranceThreshold.getValue());

        if (obj.getInside() == null) {
            obj.setInside(new Inside());
        }

        obj.getInside().setPoint(exitWaypoint.getValue());

        obj.getInside().setProximity(exitThreshold.getValue() == null ? BigDecimal.ZERO : exitThreshold.getValue());

        obj.setAssessment(assessmentSelect.getValue());
        
        //a waypoint reference may have been changed, so update the global list of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(entranceWaypoint);
        childValidationComposites.add(exitWaypoint);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(entranceNameValidationStatus);
        validationStatuses.add(entranceThresholdValidationStatus);
        validationStatuses.add(exitThresholdValidationStatus);
        validationStatuses.add(assessmentValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (entranceNameValidationStatus.equals(validationStatus)) {
            entranceNameValidationStatus.setValidity(StringUtils.isNotBlank(entranceName.getValue()));
        } else if (entranceThresholdValidationStatus.equals(validationStatus)) {
            entranceThresholdValidationStatus.setValidity(entranceThreshold.getValue() != null
                    && entranceThreshold.getValue().compareTo(BigDecimal.ZERO) > 0);
        } else if (exitThresholdValidationStatus.equals(validationStatus)) {
            exitThresholdValidationStatus.setValidity(
                    exitThreshold.getValue() != null && exitThreshold.getValue().compareTo(BigDecimal.ZERO) > 0);
        } else if (assessmentValidationStatus.equals(validationStatus)) {
            assessmentValidationStatus.setValidity(StringUtils.isNotBlank(assessmentSelect.getValue()));
        }
    }

    @Override
    protected boolean validate(Entrance entrance) {
        String errorMsg = ScenarioValidatorUtility.validateEntrance(entrance);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        entranceName.setEnabled(!isReadonly);
        entranceWaypoint.setReadonly(isReadonly);
        entranceThreshold.setEnabled(!isReadonly);
        exitWaypoint.setReadonly(isReadonly);
        exitThreshold.setEnabled(!isReadonly);
        assessmentSelect.setEnabled(!isReadonly);
    }
}
