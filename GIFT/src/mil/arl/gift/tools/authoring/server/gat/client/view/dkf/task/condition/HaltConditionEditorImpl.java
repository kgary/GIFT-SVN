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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.HaltConditionInput;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The condition impl for Halt.
 */
public class HaltConditionEditorImpl extends ConditionInputPanel<HaltConditionInput> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(HaltConditionEditorImpl.class.getName());

    /** The ui binder. */
    private static HaltConditionEditorUiBinder uiBinder = GWT.create(HaltConditionEditorUiBinder.class);

    /**
     * The Interface HaltConditionEditorUiBinder.
     */
    interface HaltConditionEditorUiBinder extends UiBinder<Widget, HaltConditionEditorImpl> {
    }

    /**
     * Team member picker used to determine the learner role that this condition
     * should assess
     */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public HaltConditionEditorImpl() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        teamPicker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {

                List<String> selectedMembers = event.getValue();

                if(selectedMembers != null && !selectedMembers.isEmpty()) {
                    
                    if(getInput().getTeamMemberRefs() == null) {
                        getInput().setTeamMemberRefs(new TeamMemberRefs());
                    }
                    
                    //update the backing data model with the list of referenced team members
                    getInput().getTeamMemberRefs().getTeamMemberRef().clear();
                    getInput().getTeamMemberRefs().getTeamMemberRef().addAll(selectedMembers);
                    
                }else{
                    getInput().setTeamMemberRefs(null);
                }

                /* this condition's team references may have changed, so update
                 * the global reference map */
                ScenarioClientUtility.gatherTeamReferences();

                /* Validate picker after it changed */
                teamPicker.validateAllAndFireDirtyEvent(getCondition());
            }
        });
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        if (ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            if (getInput().getTeamMemberRefs() == null) {
                getInput().setTeamMemberRefs(new TeamMemberRefs());
            }

            List<String> targetTeamMembers = new ArrayList<>(getInput().getTeamMemberRefs().getTeamMemberRef());
            teamPicker.setValue(targetTeamMembers);

            if (!targetTeamMembers.equals(teamPicker.getValue())) {

                /* the team picker removed some invalid team member names, so
                 * update the backing data model to match */
                getInput().getTeamMemberRefs().getTeamMemberRef().clear();

                if (teamPicker.getValue() != null) {
                    getInput().getTeamMemberRefs().getTeamMemberRef().addAll(teamPicker.getValue());
                }
            }
        }

        /* populate the scoring rules wrapper */
        rtaRulesPanel.populateWidget(getCondition());
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses to add
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
        teamPicker.setReadonly(isReadonly);
        rtaRulesPanel.setReadonly(isReadonly);
    }
}