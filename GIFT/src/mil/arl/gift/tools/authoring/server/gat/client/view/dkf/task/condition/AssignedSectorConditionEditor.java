/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AssignedSectorCondition;
import generated.dkf.Point;
import generated.dkf.PointRef;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ToggleButton;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * A {@link ConditionInputPanel} that is used to edit the
 * {@link AssignedSectorCondition} input.
 *
 * @author tflowers
 *
 */
public class AssignedSectorConditionEditor extends ConditionInputPanel<AssignedSectorCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AssignedSectorConditionEditor.class.getName());

    /** The binder that binds this java class to a ui.xml */
    private static AssignedSectorConditionEditorUiBinder uiBinder = GWT
            .create(AssignedSectorConditionEditorUiBinder.class);

    /** The definition of the binder that binds a java class to a ui.xml */
    interface AssignedSectorConditionEditorUiBinder extends UiBinder<Widget, AssignedSectorConditionEditor> {
    }

    /**
     * Minimum writable value for
     * {@link AssignedSectorCondition#setMaxAngle(BigDecimal)}
     */
    private static final BigDecimal MIN_ANGLE = BigDecimal.ONE;

    /**
     * Maximum writable value for
     * {@link AssignedSectorCondition#setMaxAngle(BigDecimal)}
     */
    private static final BigDecimal MAX_ANGLE = BigDecimal.valueOf(179.0);

    /**
     * The angle value to use for a condition in which it is not specified.
     */
    private static final BigDecimal DEFAULT_ANGLE = BigDecimal.valueOf(45.0);

    /** The picker that selects the team members to assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(true);

    /** The control used to author the angle threshold. */
    @UiField(provided = true)
    protected DecimalNumberSpinner angleSpinner = new DecimalNumberSpinner(DEFAULT_ANGLE, MIN_ANGLE, MAX_ANGLE);

    /** The checkbox used to optionally include a free look duration threshold. */
    @UiField
    protected ToggleButton useFreeLookDurationCheckBox;

    /** The control used to author the free look duration of the condition. */
    @UiField(provided = true)
    protected DecimalNumberSpinner freeLookDurationSpinner = new DecimalNumberSpinner(BigDecimal.ONE, BigDecimal.ZERO, null);
    
    /** Place of interest point picker for the center of the assigned sector */
    @UiField(provided = true)
    protected PlaceOfInterestPicker assignedSectorCenterPlacePicker = new PlaceOfInterestPicker(Point.class);
    
    /** The label for the free look duration control */
    @UiField
    protected Widget freeLookDurationLabel;

    /** The control used to author the custom assessment logic */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;

    /** The validation object used to validate */
    private final WidgetValidationStatus teamMemberValidation = new WidgetValidationStatus(teamPicker.getTextBoxRef(),
            "At least one team member must be chosen.");

    /**
     * The validation object used to validate constraints on the
     * {@link #angleSpinner}.
     */
    private final WidgetValidationStatus angleValidation = new WidgetValidationStatus(angleSpinner,
            "The angle must be greater than or equal to zero.");

    /**
     * The validation object used to validate constraints on the
     * {@link #freeLookDurationSpinner}.
     */
    private final WidgetValidationStatus freeLookDurationValidation = new WidgetValidationStatus(freeLookDurationSpinner,
            "The free look duration must be greater than or equal to zero.");
    
    /**
     * The validation object used to validate constraints on the
     * {@link #assignedSectorCenterPlacePicker}.
     */
    private final WidgetValidationStatus assignedSectorCenterValidation = new WidgetValidationStatus(assignedSectorCenterPlacePicker,
            "Select the center point of the assigned sector.");

    /**
     * Builds an unpopulated {@link AssignedSectorCondition}.
     */
    public AssignedSectorConditionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("AssignedSectorConditionEditor()");
        }
        
        CoordinateType[] disallowedTypes = ScenarioClientUtility.getDisallowedCoordinateTypes(generated.dkf.AssignedSectorCondition.class);
        assignedSectorCenterPlacePicker.setDisallowedTypes(disallowedTypes);

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Handles when the value of the {@link #teamPicker} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #teamPicker}. Can't be null.
     */
    @UiHandler("teamPicker")
    protected void onTeamMembersChanged(ValueChangeEvent<List<String>> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onTeamMembersChanged(" + event.toDebugString() + ")");
        }

        final List<String> teamMemberRef = getInput().getTeamMemberRefs().getTeamMemberRef();
        teamMemberRef.clear();
        teamMemberRef.addAll(event.getValue());

        requestValidationAndFireDirtyEvent(getCondition(), teamMemberValidation);
    }

    /**
     * Handles when the value of the {@link #angleSpinner} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #angleSpinner}. Can't be null.
     */
    @UiHandler("angleSpinner")
    protected void onAngleChanged(ValueChangeEvent<BigDecimal> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAngleChanged(" + event.toDebugString() + ")");
        }

        getInput().setMaxAngleFromCenter(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), angleValidation);
    }

    /**
     * Handles when the value of the {@link #useFreeLookDurationCheckBox} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #useFreeLookDurationCheckBox}. Can't be null.
     */
    @UiHandler("useFreeLookDurationCheckBox")
    protected void onUseFreeLookDurationChanged(ValueChangeEvent<Boolean> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onUseFreeLookDurationChanged(" + event.toDebugString() + ")");
        }

        final boolean isChecked = Boolean.TRUE.equals(event.getValue());
        freeLookDurationSpinner.setVisible(isChecked);
        freeLookDurationLabel.setVisible(isChecked);
        if (isChecked) {
            getInput().setFreeLookDuration(freeLookDurationSpinner.getValue());
        } else {
            getInput().setFreeLookDuration(null);
        }

        requestValidationAndFireDirtyEvent(getCondition(), freeLookDurationValidation);
    }

    /**
     * Handles when the value of the {@link #freeLookDurationSpinner} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #freeLookDurationSpinner}. Can't be null.
     */
    @UiHandler("freeLookDurationSpinner")
    protected void onFreeLookDurationChanged(ValueChangeEvent<BigDecimal> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onFreeLookDurationChanged(" + event.toDebugString() + ")");
        }

        getInput().setFreeLookDuration(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), freeLookDurationValidation);
    }
    
    /**
     * Handles when the value of the {@link #assignedSectorCenterPlacePicker} changes.
     * 
     * @param event the event containing the updated value of
     *      {@link #assignedSectorCenterPlacePicker}.  Can't be null.
     */
    @UiHandler("assignedSectorCenterPlacePicker")
    protected void onAssignedSectorCenterPlacePickerChanged(ValueChangeEvent<String> event){
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAssignedSectorCenterPlacePickerChanged(" + event.toDebugString() + ")");
        }
        
        PointRef pointRef = new PointRef();
        pointRef.setValue(assignedSectorCenterPlacePicker.getValue());
        getInput().setPointRef(pointRef);
        requestValidationAndFireDirtyEvent(getCondition(), assignedSectorCenterValidation);
        
        //a place of interest reference may have been changed, so update the global list of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getValidationStatuses(" + validationStatuses + ")");
        }

        validationStatuses.add(teamMemberValidation);
        validationStatuses.add(freeLookDurationValidation);
        validationStatuses.add(angleValidation);
        validationStatuses.add(assignedSectorCenterValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (teamMemberValidation.equals(validationStatus)) {
            final int teamRefCount = getInput().getTeamMemberRefs().getTeamMemberRef().size();
            validationStatus.setValidity(teamRefCount >= 1);
        } else if (freeLookDurationValidation.equals(validationStatus)) {
            final BigDecimal freeLookDuration = getInput().getFreeLookDuration();
            validationStatus.setValidity(freeLookDuration == null || freeLookDuration.doubleValue() >= 0);
        } else if (angleValidation.equals(validationStatus)) {
            final BigDecimal maxAngle = getInput().getMaxAngleFromCenter();
            validationStatus.setValidity(maxAngle != null && BigDecimal.ZERO.compareTo(maxAngle) < 0);
        } else if(assignedSectorCenterValidation.equals(validationStatus)){
            final String centerPointName = assignedSectorCenterPlacePicker.getValue();
            final Serializable centerPoint = ScenarioClientUtility.getPlaceOfInterestWithName(centerPointName);
            validationStatus.setValidity(centerPoint != null && centerPoint instanceof Point);
        }
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        /* Ensure that there is a value for the assessed teams. */
        TeamMemberRefs teamMemberRefs = getInput().getTeamMemberRefs();
        if (teamMemberRefs == null) {
            teamMemberRefs = new TeamMemberRefs();
            getInput().setTeamMemberRefs(teamMemberRefs);
        }

        teamPicker.setValue(teamMemberRefs.getTeamMemberRef());

        BigDecimal freeLookDuration = getInput().getFreeLookDuration();
        useFreeLookDurationCheckBox.setValue(freeLookDuration != null);
        if (freeLookDuration != null) {
            freeLookDurationSpinner.setValue(freeLookDuration);
        } else {
            freeLookDurationSpinner.setVisible(false);
            freeLookDurationLabel.setVisible(false);
        }

        freeLookDurationSpinner.setValue(freeLookDuration);

        /* Ensure that there is a value for max angle */
        BigDecimal maxAngle = getInput().getMaxAngleFromCenter();
        if (maxAngle == null) {
            maxAngle = DEFAULT_ANGLE;
            getInput().setMaxAngleFromCenter(maxAngle);
        }
        
        PointRef pointRef = getInput().getPointRef();        
        if(pointRef != null) {
            assignedSectorCenterPlacePicker.setValue(pointRef.getValue());
        }

        angleSpinner.setValue(maxAngle);

        rtaRulesPanel.populateWidget(getCondition());
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        teamPicker.setReadonly(isReadonly);
        useFreeLookDurationCheckBox.setEnabled(!isReadonly);
        freeLookDurationSpinner.setEnabled(!isReadonly);
        angleSpinner.setEnabled(!isReadonly);
        assignedSectorCenterPlacePicker.setReadonly(isReadonly);
        rtaRulesPanel.setReadonly(isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addValidationCompositeChildren(" + childValidationComposites + ")");
        }

        childValidationComposites.add(teamPicker);
        childValidationComposites.add(rtaRulesPanel);
        childValidationComposites.add(assignedSectorCenterPlacePicker);
    }
}
