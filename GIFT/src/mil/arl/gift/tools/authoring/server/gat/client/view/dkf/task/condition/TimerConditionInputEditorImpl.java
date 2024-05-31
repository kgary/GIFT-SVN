/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.CheckBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.BooleanEnum;
import generated.dkf.TimerConditionInput;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The condition impl for Timer.
 * 
 * @author nroberts
 */
public class TimerConditionInputEditorImpl extends ConditionInputPanel<TimerConditionInput> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static TimerConditionInputEditorImplUiBinder uiBinder = GWT
            .create(TimerConditionInputEditorImplUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface TimerConditionInputEditorImplUiBinder extends UiBinder<Widget, TimerConditionInputEditorImpl> {
    }

    /** The time boxes for the user to enter the delay length before an assessment can be given */
    @UiField
    protected FormattedTimeBox assessmentDelayTimeBox;

    /** Indicates if the countdown should repeat when it is completed */
    @UiField
    protected CheckBox repeatCheckBox;
    
    /** Team picker used to determine learner roles this condition should assess */
    @UiField
    protected EditableTeamPicker teamPicker;  // the team picker is optional input here

    /** The container for showing validation messages for having a delay length of 0 seconds. */
    private final WidgetValidationStatus delayValidationStatus;

    /**
     * Constructor
     */
    public TimerConditionInputEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        delayValidationStatus = new WidgetValidationStatus(assessmentDelayTimeBox,
                "You cannot have a delay of 0 seconds. Please enter a positive value.");

        assessmentDelayTimeBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                getInput().setInterval(BigDecimal.valueOf(event.getValue()));
                requestValidationAndFireDirtyEvent(getCondition(), delayValidationStatus);
            }
        });

        repeatCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getInput().setRepeatable(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
            }
        });
        
        teamPicker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                
                List<String> selectedRoles = event.getValue();
                
                if(selectedRoles != null && !selectedRoles.isEmpty()) {
                    
                    if(getInput().getTeamMemberRefs() == null){
                        getInput().setTeamMemberRefs(new generated.dkf.TeamMemberRefs());
                    }
                    
                    //update the backing data model with the list of referenced team members
                    getInput().getTeamMemberRefs().getTeamMemberRef().clear();
                    getInput().getTeamMemberRefs().getTeamMemberRef().addAll(selectedRoles);
                    
                }else{
                    getInput().setTeamMemberRefs(null);
                }
                
                //this condition's team references may have changed, so update the global reference map
                ScenarioClientUtility.gatherTeamReferences();

                /* Validate picker after it changed */
                teamPicker.validateAllAndFireDirtyEvent(getCondition());
            }
        });
    }

    @Override
    protected void onEdit() {
        if (getInput().getInterval() == null) {
            getInput().setInterval(BigDecimal.ONE);
        }

        if (getInput().getRepeatable() == null) {
            getInput().setRepeatable(BooleanEnum.FALSE);
        }

        assessmentDelayTimeBox.setValue(getInput().getInterval().intValue());
        repeatCheckBox.setValue(BooleanEnum.TRUE.equals(getInput().getRepeatable()));
        
        if(ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            
            teamPicker.setVisible(true);
            
            if(getInput().getTeamMemberRefs() != null){
                List<String> targetTeamMembers = new ArrayList<>(getInput().getTeamMemberRefs().getTeamMemberRef());
                
                teamPicker.setValue(targetTeamMembers);
                
                if(!targetTeamMembers.equals(teamPicker.getValue())){
                    
                    //the team picker removed some invalid team member names, so update the backing data model to match
                    getInput().getTeamMemberRefs().getTeamMemberRef().clear();
                    
                    if(teamPicker.getValue() != null) {
                        getInput().getTeamMemberRefs().getTeamMemberRef().addAll(teamPicker.getValue());
                    }
                }
            }else{
                // the team member refs is optional for this condition UI
                teamPicker.setValue(null);
            }

            
        } else {
            
            teamPicker.setVisible(false);
            
            if(getInput().getTeamMemberRefs() != null) {
                getInput().setTeamMemberRefs(null);
            }
            
            teamPicker.setValue(null);
        }
    }
    

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(delayValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (delayValidationStatus.equals(validationStatus)) {
            delayValidationStatus.setValidity(assessmentDelayTimeBox.getValue() > 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teamPicker);
    }
    
    @Override
    protected void setReadonly(boolean isReadonly) {
        assessmentDelayTimeBox.setEnabled(!isReadonly);
        repeatCheckBox.setEnabled(!isReadonly);
        teamPicker.setReadonly(isReadonly);
    }
}