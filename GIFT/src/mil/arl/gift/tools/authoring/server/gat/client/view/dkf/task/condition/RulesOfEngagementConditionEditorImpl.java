/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.RulesOfEngagementCondition;
import generated.dkf.TeamMemberRefs;
import generated.dkf.Wcs;
import generated.dkf.WeaponControlStatusEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The condition impl for RulesOfEngagement.
 */
public class RulesOfEngagementConditionEditorImpl extends ConditionInputPanel<RulesOfEngagementCondition> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static RulesOfEngagementConditionEditorUiBinder uiBinder = GWT
            .create(RulesOfEngagementConditionEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface RulesOfEngagementConditionEditorUiBinder extends UiBinder<Widget, RulesOfEngagementConditionEditorImpl> {
    }

    /** The weapon control status list box. */
    @UiField
    protected Select engagementOrderSelect;

    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;

    /**
     * Constructor
     */
    public RulesOfEngagementConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        engagementOrderSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getInput().getWcs().setValue(WeaponControlStatusEnum.valueOf(event.getValue()));
            }
        });

        List<WeaponControlStatusEnum> acceptableValues = new ArrayList<WeaponControlStatusEnum>(
                Arrays.asList(WeaponControlStatusEnum.values()));

        // sort alphabetically
        Collections.sort(acceptableValues, new Comparator<WeaponControlStatusEnum>() {
            @Override
            public int compare(WeaponControlStatusEnum item1, WeaponControlStatusEnum item2) {
                return item1.name().compareTo(item2.name());
            }
        });

        for (WeaponControlStatusEnum engagementOrder : acceptableValues) {
            Option option = new Option();
            option.setText(engagementOrder.value());
            option.setValue(engagementOrder.name());
            engagementOrderSelect.add(option);
        }

        engagementOrderSelect.setValue(WeaponControlStatusEnum.FREE.name());

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
    protected void onEdit() {
        if (getInput().getWcs() == null) {
            getInput().setWcs(new Wcs());
        }

        engagementOrderSelect.setValue(getInput().getWcs().getValue().name());

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
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teamPicker);
        childValidationComposites.add(rtaRulesPanel);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        engagementOrderSelect.setEnabled(!isReadonly);
        rtaRulesPanel.setReadonly(isReadonly);
        teamPicker.setReadonly(isReadonly);
    }
}