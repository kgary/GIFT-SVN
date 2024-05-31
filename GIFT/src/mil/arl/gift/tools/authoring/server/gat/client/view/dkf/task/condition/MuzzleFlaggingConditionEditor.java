/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.MuzzleFlaggingCondition;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ToggleButton;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * A {@link ConditionInputPanel} that is used to edit the
 * {@link MuzzleFlaggingCondition} input.
 *
 * @author tflowers
 *
 */
public class MuzzleFlaggingConditionEditor extends ConditionInputPanel<MuzzleFlaggingCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(MuzzleFlaggingConditionEditor.class.getName());

    /** The binder that binds this java class to a ui.xml */
    private static MuzzleFlaggingConditionEditorUiBinder uiBinder = GWT
            .create(MuzzleFlaggingConditionEditorUiBinder.class);

    /** The definition of the binder that binds a java class to a ui.xml */
    interface MuzzleFlaggingConditionEditorUiBinder extends UiBinder<Widget, MuzzleFlaggingConditionEditor> {
    }

    /**
     * Minimum writable value for
     * {@link MuzzleFlaggingCondition#setMaxDistance(BigDecimal)}
     */
    private static final BigDecimal MIN_DISTANCE = BigDecimal.ONE;

    /**
     * Maximum writable value for
     * {@link MuzzleFlaggingCondition#setMaxDistance(BigDecimal)}
     */
    private static final BigDecimal MAX_DISTANCE = BigDecimal.valueOf(1000.0);

    /**
     * Minimum writable value for
     * {@link MuzzleFlaggingCondition#setMaxAngle(BigDecimal)}
     */
    private static final BigDecimal MIN_ANGLE = BigDecimal.ONE;

    /**
     * Maximum writable value for
     * {@link MuzzleFlaggingCondition#setMaxAngle(BigDecimal)}
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

    /** The checkbox used to optionally include a max distance threshold. */
    @UiField
    protected ToggleButton useMaxDistanceCheckBox;

    /** The control used to author the maximum distance of the condition. */
    @UiField(provided = true)
    protected DecimalNumberSpinner maxDistanceSpinner = new DecimalNumberSpinner(BigDecimal.ONE, MIN_DISTANCE, MAX_DISTANCE);
    
    /** The label for the maximum distance control */
    @UiField
    protected Widget maxDistanceLabel;

    /** The control used to author the custom assessment logic */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;
    
    /** switch used to enable / disable the use of the weapon safety value in assessments */
    @UiField
    protected ToggleButton useWeaponSafetyButton;
    
    /** shown when in playback mode to let author know use weapon safety won't work during the session */
    @UiField
    protected Label weaponSafetyNotAvailableLabel;

    /** The validation object used to validate */
    private final WidgetValidationStatus teamMemberValidation = new WidgetValidationStatus(teamPicker.getTextBoxRef(),
            "At least two team members must be chosen.");

    /**
     * The validation object used to validate constraints on the
     * {@link #angleSpinner}.
     */
    private final WidgetValidationStatus angleValidation = new WidgetValidationStatus(angleSpinner,
            "The angle must be greater than or equal to zero.");

    /**
     * The validation object used to validate constraints on the
     * {@link #maxDistanceSpinner}.
     */
    private final WidgetValidationStatus maxDistanceValidation = new WidgetValidationStatus(maxDistanceSpinner,
            "The max distance must be greater than or equal to zero.");

    /**
     * Builds an unpopulated {@link MuzzleFlaggingCondition}.
     */
    public MuzzleFlaggingConditionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MuzzleFlaggingConditionEditor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        if(ScenarioClientUtility.isPlayback()){
            weaponSafetyNotAvailableLabel.setVisible(true);
            useWeaponSafetyButton.setEnabled(false);
        }else{
            weaponSafetyNotAvailableLabel.setVisible(false);
            useWeaponSafetyButton.setEnabled(true);
        }        
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

        getInput().setMaxAngle(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), angleValidation);
    }

    /**
     * Handles when the value of the {@link #useMaxDistanceCheckBox} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #useMaxDistanceCheckBox}. Can't be null.
     */
    @UiHandler("useMaxDistanceCheckBox")
    protected void onUseMaxDistanceChanged(ValueChangeEvent<Boolean> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onUseMaxDistanceChanged(" + event.toDebugString() + ")");
        }

        final boolean isChecked = Boolean.TRUE.equals(event.getValue());
        maxDistanceSpinner.setVisible(isChecked);
        maxDistanceLabel.setVisible(isChecked);
        if (isChecked) {
            getInput().setMaxDistance(maxDistanceSpinner.getValue());
        } else {
            getInput().setMaxDistance(null);
        }

        requestValidationAndFireDirtyEvent(getCondition(), maxDistanceValidation);
    }
    
    /**
     * Handles when the value of the {@link #useWeaponSafetyButton} changes.
     * 
     * @param event the event containing the updated value of {@link #useWeaponSafetyButton}.
     * Can't be null.
     */
    @UiHandler("useWeaponSafetyButton")
    protected void onUseWeaponSafetyChanged(ValueChangeEvent<Boolean> event){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onUseWeaponSafetyChanged(" + event.toDebugString() + ")");
        }
        
        final boolean isChecked = Boolean.TRUE.equals(event.getValue());
        getInput().setUseWeaponSafety(isChecked);
    }

    /**
     * Handles when the value of the {@link #maxDistanceSpinner} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #maxDistanceSpinner}. Can't be null.
     */
    @UiHandler("maxDistanceSpinner")
    protected void onMaxDistanceChanged(ValueChangeEvent<BigDecimal> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onMaxDistanceChanged(" + event.toDebugString() + ")");
        }

        getInput().setMaxDistance(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), maxDistanceValidation);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getValidationStatuses(" + validationStatuses + ")");
        }

        validationStatuses.add(teamMemberValidation);
        validationStatuses.add(maxDistanceValidation);
        validationStatuses.add(angleValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (teamMemberValidation.equals(validationStatus)) {
            final int teamRefCount = getInput().getTeamMemberRefs().getTeamMemberRef().size();
            validationStatus.setValidity(teamRefCount >= 2);
        } else if (maxDistanceValidation.equals(validationStatus)) {
            final BigDecimal maxDistance = getInput().getMaxDistance();
            validationStatus.setValidity(maxDistance == null || BigDecimal.ZERO.compareTo(maxDistance) < 0);
        } else if (angleValidation.equals(validationStatus)) {
            final BigDecimal maxAngle = getInput().getMaxAngle();
            validationStatus.setValidity(maxAngle != null && BigDecimal.ZERO.compareTo(maxAngle) < 0);
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

        BigDecimal maxDistance = getInput().getMaxDistance();
        useMaxDistanceCheckBox.setValue(maxDistance != null);
        if (maxDistance != null) {
            maxDistanceSpinner.setValue(maxDistance);
        } else {
            maxDistanceSpinner.setVisible(false);
            maxDistanceLabel.setVisible(false);
        }

        maxDistanceSpinner.setValue(maxDistance);        
        
        // default is true
        final Boolean useWeaponSafety = getInput().isUseWeaponSafety() != null ? getInput().isUseWeaponSafety() : Boolean.TRUE;
        // #5034 - for some reason the toggle button setValue has to be called this way, otherwise programmatically
        // changing the value doesn't work in this editor.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            
            @Override
            public void execute() {
                useWeaponSafetyButton.setValue(useWeaponSafety);                    
            }
        });
        
        /* Ensure that there is a value for max angle */
        BigDecimal maxAngle = getInput().getMaxAngle();
        if (maxAngle == null) {
            maxAngle = DEFAULT_ANGLE;
            getInput().setMaxAngle(maxAngle);
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
        useMaxDistanceCheckBox.setEnabled(!isReadonly);
        maxDistanceSpinner.setEnabled(!isReadonly);
        angleSpinner.setEnabled(!isReadonly);
        rtaRulesPanel.setReadonly(isReadonly);
        useWeaponSafetyButton.setEnabled(!isReadonly && !ScenarioClientUtility.isPlayback());
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addValidationCompositeChildren(" + childValidationComposites + ")");
        }

        childValidationComposites.add(teamPicker);
        childValidationComposites.add(rtaRulesPanel);
    }
}
