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

import generated.dkf.TeamMemberRefs;
import generated.dkf.UseRadioCondition;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The condition input editor implementation for {@link UseRadioCondition}.
 */
public class UseRadioConditionInputEditorImpl extends ConditionInputPanel<UseRadioCondition> {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(UseRadioConditionInputEditorImpl.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static UseRadioConditionInputEditorImplUiBinder uiBinder = GWT
            .create(UseRadioConditionInputEditorImplUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface UseRadioConditionInputEditorImplUiBinder extends UiBinder<Widget, UseRadioConditionInputEditorImpl> {
    }
    
    /** Deck panel used to switch editors depending on whether the team picker is needed */
    @UiField
    protected DeckPanel mainDeck;

    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());
    
    /** A panel containing no interactive UI components that is shown when the team picker is not needed*/
    @UiField
    protected Widget emptyPanel;

    /**
     * Creates a new editor for modifying {@link UseRadioCondition}s
     */
    public UseRadioConditionInputEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        teamPicker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                
                List<String> selectedRoles = event.getValue();
                
                if(getInput().getTeamMemberRefs() == null) {
                    getInput().setTeamMemberRefs(new TeamMemberRefs());
                }
                
                if(selectedRoles != null && !selectedRoles.isEmpty()) {
                    
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
            
            //the current training app requires team member learner IDs, so load any that are found
            if(getInput().getTeamMemberRefs() == null) {
                getInput().setTeamMemberRefs(new TeamMemberRefs());
            }
            
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
                
                teamPicker.validateAllAndFireDirtyEvent(getCondition());
            }
            
        } else {
            
            mainDeck.showWidget(mainDeck.getWidgetIndex(emptyPanel));
            
            if(getInput().getTeamMemberRefs() != null) {
                getInput().setTeamMemberRefs(null);
            }
            
            teamPicker.setValue(null);
            
            teamPicker.validateAllAndFireDirtyEvent(getCondition());
        }
    }
    

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        //nothing to validate
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        //nothing to validate
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