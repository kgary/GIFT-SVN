/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.IdentifyPOIsCondition;
import generated.dkf.Point;
import generated.dkf.PointRef;
import generated.dkf.Pois;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The condition impl for IdentifyPois.
 */
public class IdentifyPoisConditionEditorImpl extends ConditionInputPanel<IdentifyPOIsCondition> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static IdentifyPoisConditionEditorUiBinder uiBinder = GWT.create(IdentifyPoisConditionEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface IdentifyPoisConditionEditorUiBinder extends UiBinder<Widget, IdentifyPoisConditionEditorImpl> {
    }

    /** The item list editor table for the locations */
    @UiField(provided = true)
    protected ItemListEditor<PointRef> locationListEditor;

    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;

    /**
     * Shows an error message if this condition is being used without a running
     * training application.
     */
    private final ModelValidationStatus requiresRunningTrainingApplication = new ModelValidationStatus(
            "This condition requires a running training application in order to assess properly, but this course object is configured for log file playback only.") {
        @Override
        protected void fireJumpToEvent(Serializable modelObject) {
            ScenarioEventUtility.fireJumpToEvent(modelObject);
        }
    };

    /** The container for showing validation messages for having no locations set. */
    private final WidgetValidationStatus locationValidationStatus;

    /**
     * Constructor
     */
    public IdentifyPoisConditionEditorImpl() {
        
        // must be done before initWidget
        WaypointRefItemEditor waypointEditor = new WaypointRefItemEditor(this);
        CoordinateType[] disallowedTypes = ScenarioClientUtility.getDisallowedCoordinateTypes(generated.dkf.IdentifyPOIsCondition.class);
        waypointEditor.setDisallowedTypes(disallowedTypes);
        locationListEditor = new ItemListEditor<PointRef>(waypointEditor);
        
        initWidget(uiBinder.createAndBindUi(this));

        locationListEditor.setTableLabel("Locations that the learner must see:");
        locationListEditor.setPlaceholder("No locations have been created.");
        locationListEditor.setFields(buildLocationItemFields());
        locationListEditor.setDraggable(true);
        locationListEditor.addListChangedCallback(new ListChangedCallback<PointRef>() {
            @Override
            public void listChanged(ListChangedEvent<PointRef> event) {
                setClean();
                requestValidationAndFireDirtyEvent(getCondition(), locationValidationStatus);
            }
        });
        locationListEditor.addEditCancelledCallback(new EditCancelledCallback() {
            @Override
            public void editCancelled() {
                setClean();
            }
        });
        locationListEditor.setRemoveItemDialogTitle("Remove Location to identify");
        locationListEditor.setRemoveItemStringifier(new Stringifier<PointRef>() {
            @Override
            public String stringify(PointRef obj) {
                return "the point "+bold(obj.getValue()).asString()+" from needing to be identified (does not delete the underlying place of interest)";
            }
        });
        Widget locationAddAction = locationListEditor.addCreateListAction("Click here to add a new location",
                buildLocationCreateAction());

        locationValidationStatus = new WidgetValidationStatus(locationAddAction,
                "There are no locations to see. Add a location that the learner should see.");
        requiresRunningTrainingApplication.setAdditionalInstructions(
                "Delete this Condition using the Task list panel or change the Condition using the button below.");

        teamPicker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                
                setDirty();
                
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

    /**
     * Builds the item fields for the location table.
     * 
     * @return the {@link ItemField item field columns} for the location table.
     */
    private List<ItemField<PointRef>> buildLocationItemFields() {

        ItemField<PointRef> waypointField = new ItemField<PointRef>(null, "100%") {
            @Override
            public Widget getViewWidget(PointRef item) {
                return new HTML(item.getValue());
            }
        };

        return Arrays.asList(waypointField);
    }

    /**
     * Builds the create action for the location table.
     * 
     * @return the {@link CreateListAction} for the location table.
     */
    private CreateListAction<PointRef> buildLocationCreateAction() {
        return new CreateListAction<PointRef>() {

            @Override
            public PointRef createDefaultItem() {
                
                setDirty();
                
                List<Serializable> placesOfInterest = ScenarioClientUtility.getUnmodifiablePlacesOfInterestList();

                PointRef pointRef = new PointRef();

                if (!placesOfInterest.isEmpty()) {
                    //artificially pick a point
                    for(Serializable placeOfInterest : placesOfInterest){
                        
                        if(placeOfInterest instanceof Point){
                            Point point = (Point) placeOfInterest;
                            
                            if(!ScenarioValidatorUtility.containsDisallowedCoordinate(
                                    point, ScenarioClientUtility.getDisallowedCoordinateTypes(generated.dkf.IdentifyPOIsCondition.class))){
                                pointRef.setValue(point.getName());
                                break;
                            }
                        }
                    }
                    
                }

                return pointRef;
            }
        };
    }

    @Override
    protected void onEdit() {
        if (getInput().getPois() == null) {
            getInput().setPois(new Pois());
        }

        // set list into the location list editor
        locationListEditor.setItems(getInput().getPois().getPointRef());

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
        validationStatuses.add(locationValidationStatus);
        validationStatuses.add(requiresRunningTrainingApplication);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int locationListSize;
        if (locationValidationStatus.equals(validationStatus)) {
            locationListSize = locationListEditor.size();
            locationValidationStatus.setValidity(locationListSize > 0);
        } else if (requiresRunningTrainingApplication.equals(validationStatus)) {
            requiresRunningTrainingApplication.setValidity(!ScenarioClientUtility.isPlayback());
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(locationListEditor);
        childValidationComposites.add(teamPicker);
        childValidationComposites.add(rtaRulesPanel);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        locationListEditor.setReadonly(isReadonly);
        rtaRulesPanel.setReadonly(isReadonly);
        teamPicker.setReadonly(isReadonly);
    }
}