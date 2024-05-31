/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.HealthConditionInput;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * An editor that allows authors to modify {@link HealthConditionInput}s
 * 
 * @author nroberts
 */
public class HealthConditionEditor extends ConditionInputPanel<HealthConditionInput> {

    private static HealthConditionEditorUiBinder uiBinder = GWT.create(HealthConditionEditorUiBinder.class);

    interface HealthConditionEditorUiBinder extends UiBinder<Widget, HealthConditionEditor> {
    }
    
    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;
    
    /**
     * Creates a new health condition editor with no loaded condition data and initializes its UI handlers
     */
    public HealthConditionEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        teamPicker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                
                List<String> selectedRoles = event.getValue();
                
                if(selectedRoles != null && !selectedRoles.isEmpty()) {
                    
                    if(getInput().getTeamMemberRefs() == null) {
                        getInput().setTeamMemberRefs(new TeamMemberRefs());
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
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        //nothing to do
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        //nothing to do
    }

    @Override
    protected void onEdit() {
        
        // populate the scoring rules wrapper
        rtaRulesPanel.populateWidget(getCondition());
        
        if(ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            
            //the current training app requires team member learner IDs, so load any that are found
            if(getInput().getTeamMemberRefs() == null) {
                getInput().setTeamMemberRefs(new TeamMemberRefs());
            }
            
            List<String> targetTeamMembers = new ArrayList<>(getInput().getTeamMemberRefs().getTeamMemberRef());
            
            teamPicker.setValue(targetTeamMembers);
            
            if(!targetTeamMembers.equals(teamPicker.getValue())){
                
                //the team picker removed some invalid team member names, so update the backing data model to match
                getInput().getTeamMemberRefs().getTeamMemberRef().clear();
                
                if(teamPicker.getValue() != null) {
                    getInput().getTeamMemberRefs().getTeamMemberRef().addAll(teamPicker.getValue());
                }
            }
            
        } else {
            
            if(getInput().getTeamMemberRefs() != null) {
                getInput().setTeamMemberRefs(null);
            }
            
            teamPicker.setValue(null);
        }
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        rtaRulesPanel.setReadonly(isReadonly);
        teamPicker.setReadonly(isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(rtaRulesPanel);
        childValidationComposites.add(teamPicker);
    }

}
