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
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ObservedAssessmentCondition;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The condition input editor implementation for {@link ObservedAssessmentCondition}.
 */
public class ObservedAssessmentConditionInputEditorImpl extends ConditionInputPanel<ObservedAssessmentCondition> {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ObservedAssessmentConditionInputEditorImpl.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ObservedAssessmentConditionInputEditorImplUiBinder uiBinder = GWT
            .create(ObservedAssessmentConditionInputEditorImplUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ObservedAssessmentConditionInputEditorImplUiBinder extends UiBinder<Widget, ObservedAssessmentConditionInputEditorImpl> {
    }
    
    /** Deck panel used to switch editors depending on whether the team picker is needed */
    @UiField
    protected DeckPanel mainDeck;

    /** Team picker used to determine learner roles this condition should assess */
    @UiField
    protected EditableTeamPicker teamPicker;  // team member ref is optional for this condition
    
    /** A panel containing no interactive UI components that is shown when the team picker is not needed*/
    @UiField
    protected Widget emptyPanel;

    /**
     * Creates a new editor for modifying {@link ObservedAssessmentCondition}s
     */
    public ObservedAssessmentConditionInputEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
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
        
        mainDeck.showWidget(mainDeck.getWidgetIndex(emptyPanel));
    }

    @Override
    protected void onEdit() {
        
        if(ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            
            mainDeck.showWidget(mainDeck.getWidgetIndex(teamPicker));
            
            if(getInput().getTeamMemberRefs() != null){
                List<String> targetTeamMembers = new ArrayList<>(getInput().getTeamMemberRefs().getTeamMemberRef());
                
                teamPicker.setValue(targetTeamMembers);
                
                if(!CollectionUtils.equals(targetTeamMembers, teamPicker.getValue())){
                    //the team picker removed some invalid team member names, so update the backing data model to match
                    getInput().getTeamMemberRefs().getTeamMemberRef().clear();
                    
                    if(teamPicker.getValue() != null) {
                        if(logger.isLoggable(Level.INFO)){
                            logger.info("Setting team member ref data model for condition to :"+teamPicker.getValue());
                        }
                        getInput().getTeamMemberRefs().getTeamMemberRef().addAll(teamPicker.getValue());
                    }
                }
            }else{
                // the team member refs is optional for this condition UI
                teamPicker.setValue(null);
            }
            
        } else {
            
            mainDeck.showWidget(mainDeck.getWidgetIndex(emptyPanel));
            
            if(getInput().getTeamMemberRefs() != null) {
                getInput().setTeamMemberRefs(null);
            }
            
            teamPicker.setValue(null);
        }
    }
    

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teamPicker);
    }
    
    @Override
    protected void setReadonly(boolean isReadonly) {
        teamPicker.setReadonly(isReadonly);
    }
}