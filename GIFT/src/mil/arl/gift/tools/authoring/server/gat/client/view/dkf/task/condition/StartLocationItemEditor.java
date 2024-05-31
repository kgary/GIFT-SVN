/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.Collections;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Coordinate;
import generated.dkf.StartLocation;
import generated.dkf.TeamRef;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor.RibbonPanelChangeCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A widget used to add and edit start locations.
 * 
 * @author sharrison
 */
public class StartLocationItemEditor extends ItemEditor<TeamRefOrStartLocation> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static StartLocationItemEditorUiBinder uiBinder = GWT.create(StartLocationItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StartLocationItemEditorUiBinder extends UiBinder<Widget, StartLocationItemEditor> {
    }
    
    /** The deck used to switch between the widgets used to edit team member references and start locations */
    @UiField
    protected DeckPanel typeDeck;
    
    /** The editor used to change the team member being referenced */
    @UiField(provided = true)
    protected TeamMemberPicker teamMemberRef = new TeamMemberPicker(true);

    /** The editor used to modify coordinates for entity start locations */
    @UiField
    protected ScenarioCoordinateEditor coordinateEditor;

    /**
     * Constructor.
     */
    public StartLocationItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        coordinateEditor.addRibbonPanelChangeCallback(new RibbonPanelChangeCallback() {
            @Override
            public void ribbonVisible(boolean visible) {
                setSaveButtonVisible(!visible);
            }
        });
    }

    @Override
    protected void populateEditor(TeamRefOrStartLocation item) {
        
        if(item.getRefOrLocation() instanceof StartLocation) {
            
            teamMemberRef.setActive(false);
            
            StartLocation loc = (StartLocation) item.getRefOrLocation();
            
            if (loc.getCoordinate() == null) {
                loc.setCoordinate(new Coordinate());
            }
    
            coordinateEditor.setCoordinate(loc.getCoordinate());
            
            typeDeck.showWidget(typeDeck.getWidgetIndex(coordinateEditor));
            
        } else {
            
            teamMemberRef.setActive(true);
            
            String memberRef = null;
            
            if(item.getRefOrLocation() instanceof String) {
                memberRef = (String) item.getRefOrLocation();
            }
            
            teamMemberRef.setValue(memberRef);
            
            typeDeck.showWidget(typeDeck.getWidgetIndex(teamMemberRef));
        }
    }
    
    @Override
    protected void applyEdits(TeamRefOrStartLocation item) {
        
        if(item.getRefOrLocation() instanceof StartLocation) {
            
            //only read from the coordinate editor for legacy start locations
            coordinateEditor.updateCoordinate();
            
        } else {
            
            //save the selected team member reference when editing a team member reference or a new item
            item.setRefOrLocation(teamMemberRef.getValue());
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teamMemberRef);
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
    protected boolean validate(TeamRefOrStartLocation obj) {
        
        String errorMsg = null;
        
        if(obj.getRefOrLocation() instanceof StartLocation) {
            errorMsg = ScenarioValidatorUtility.validateStartLocation((StartLocation) obj.getRefOrLocation());
            
        } else if(obj.getRefOrLocation() instanceof String){
            
            TeamRef ref = new TeamRef();
            ref.setValue((String) obj.getRefOrLocation());
            errorMsg = ScenarioValidatorUtility.validateTeamMembersExist(Collections.singletonList(ref));
            
        } else {
            return false; //don't allow the user to save null values
        }
        
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        coordinateEditor.setReadOnly(isReadonly);
        teamMemberRef.setReadonly(isReadonly);
    }
}
