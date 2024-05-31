/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.SpeedLimitCondition;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

public class SpeedLimitConditionInputPanel extends ConditionInputPanel<SpeedLimitCondition> {

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SpeedLimitConditionInputPanelUiBinder extends UiBinder<Widget, SpeedLimitConditionInputPanel> {
    }

    /** The UiBinder that combines the ui.xml with this java class */
    SpeedLimitConditionInputPanelUiBinder uiBinder = GWT.create(SpeedLimitConditionInputPanelUiBinder.class);

    /** The default value for the {@link #speedSpinner} */
    private static final BigDecimal DEFAULT_SPEED = BigDecimal.ZERO;

    /** The spinner for the speed */
    @UiField(provided = true)
    DecimalNumberSpinner speedSpinner = new DecimalNumberSpinner(DEFAULT_SPEED, DEFAULT_SPEED, null);
    
    /** The spinner for the min speed */
    @UiField(provided = true)
    DecimalNumberSpinner minSpeedSpinner = new DecimalNumberSpinner(DEFAULT_SPEED, DEFAULT_SPEED, null);

    /** The picker for the team members */
    @UiField(provided = true)
    TeamMemberPicker teamMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;
    
    /** The box used to enter how long the speed must be exceeded to trigger an assessment */
    @UiField
    protected FormattedTimeBox durationBox;

    /** Validation widget to check if the provided speed is greater than zero */
    private final WidgetValidationStatus nonZeroSpeedValidationWidget;
    
    /** Validation widget to check if the provided speed is not a negative number */
    private final WidgetValidationStatus nonNegativeMinSpeedValidationWidget;
    
    /** Validation widget to check if the provided min speed is less than the speed limit value */
    private final WidgetValidationStatus minLessThanMaxSpeedValidationWidget;

    /**
     * Constructor
     */
    public SpeedLimitConditionInputPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        nonZeroSpeedValidationWidget = new WidgetValidationStatus(speedSpinner, "The speed must be greater than 0 mph");
        nonNegativeMinSpeedValidationWidget = new WidgetValidationStatus(minSpeedSpinner, "The minimum speed must be a positive number, including 0.");
        minLessThanMaxSpeedValidationWidget = new WidgetValidationStatus(minSpeedSpinner, "The minimum speed must be less than the speed limit");
        
        durationBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                
                getInput().setMinDurationBeforeViolation(
                        event.getValue() != null 
                            ? BigInteger.valueOf(event.getValue()) 
                            : null);
            }
        });
    }

    /**
     * Catches value change events for the {@link #speedSpinner}
     * 
     * @param event the caught value change event
     */
    @UiHandler("speedSpinner")
    protected void onSpeedChanged(ValueChangeEvent<BigDecimal> event) {
        getInput().setSpeedLimit(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), nonZeroSpeedValidationWidget);
    }
    
    /**
     * Catches value change events for the {@link #minSpeedSpinner}
     * 
     * @param event the caught value change event
     */
    @UiHandler("minSpeedSpinner")
    protected void onMinSpeedChanged(ValueChangeEvent<BigDecimal> event) {
        
        if(event.getValue().compareTo(BigDecimal.ZERO) == 0){
            getInput().setMinSpeedLimit(null);
        }else{
            getInput().setMinSpeedLimit(event.getValue());
        }
        requestValidationAndFireDirtyEvent(getCondition(), nonNegativeMinSpeedValidationWidget);
        requestValidationAndFireDirtyEvent(getCondition(), minLessThanMaxSpeedValidationWidget);
    }

    /**
     * Catches value change events for the {@link #teamMemberPicker}
     * 
     * @param event the caught value change event
     */
    @UiHandler("teamMemberPicker")
    protected void onTeamReferenceChanged(ValueChangeEvent<String> event) {
        String selectedRole = event.getValue();

        /* update the backing data model with the referenced team member name */
        if (StringUtils.isNotBlank(selectedRole)) {
            getInput().setTeamMemberRef(selectedRole);
        } else {
            getInput().setTeamMemberRef(null);
        }

        /* this condition's team references may have changed, so update the
         * global reference map */
        ScenarioClientUtility.gatherTeamReferences();

        /* Validate picker after it changed */
        teamMemberPicker.validateAllAndFireDirtyEvent(getCondition());
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nonZeroSpeedValidationWidget);
        validationStatuses.add(nonNegativeMinSpeedValidationWidget);
        validationStatuses.add(minLessThanMaxSpeedValidationWidget);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (nonZeroSpeedValidationWidget.equals(validationStatus)) {
            nonZeroSpeedValidationWidget.setValidity(speedSpinner.getValue().compareTo(BigDecimal.ZERO) == 1);
        }else if(nonNegativeMinSpeedValidationWidget.equals(validationStatus)) {
            nonNegativeMinSpeedValidationWidget.setValidity(minSpeedSpinner.getValue().compareTo(BigDecimal.ZERO) != -1);            
        }else if(minLessThanMaxSpeedValidationWidget.equals(validationStatus)){
            minLessThanMaxSpeedValidationWidget.setValidity(minSpeedSpinner.getValue().doubleValue() <= speedSpinner.getValue().doubleValue());
        }
    }

    @Override
    protected void onEdit() {
        SpeedLimitCondition input = getInput();

        if (input.getSpeedLimit() != null) {
            speedSpinner.setValue(input.getSpeedLimit());
        } else {
            /* Reset spinner to default */
            speedSpinner.setValue(DEFAULT_SPEED);
        }
        
        if (input.getMinSpeedLimit() != null) {
            minSpeedSpinner.setValue(input.getMinSpeedLimit());
        } else {
            /* Reset spinner to default */
            minSpeedSpinner.setValue(DEFAULT_SPEED);
        }

        if (input.getTeamMemberRef() != null) {
            teamMemberPicker.setValue(input.getTeamMemberRef());
        }
        
        if(input.getMinDurationBeforeViolation() == null) {
            input.setMinDurationBeforeViolation(BigInteger.valueOf(5)); //default duration of 5 seconds
        }
        
        durationBox.setValue(input.getMinDurationBeforeViolation().intValue());

        /* populate the scoring rules wrapper */
        rtaRulesPanel.populateWidget(getCondition());
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        speedSpinner.setEnabled(!isReadonly);
        teamMemberPicker.setReadonly(isReadonly);
        durationBox.setReadOnly(isReadonly);
        minSpeedSpinner.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teamMemberPicker);
        childValidationComposites.add(rtaRulesPanel);
    }
}
