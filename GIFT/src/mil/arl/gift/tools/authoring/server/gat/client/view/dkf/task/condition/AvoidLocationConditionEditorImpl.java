/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;
import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.AreaRef;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.PointRef;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ItemListEditorEditEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestResumeEditEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.WrapButton;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The condition impl for AvoidLocation.
 */
public class AvoidLocationConditionEditorImpl extends ConditionInputPanel<AvoidLocationCondition> {

    /** The ui binder. */
    private static AvoidLocationConditionEditorUiBinder uiBinder = GWT
            .create(AvoidLocationConditionEditorUiBinder.class);

    /** Interface for handling events. */
    interface WidgetEventBinder extends EventBinder<AvoidLocationConditionEditorImpl> {
    }

    /** Create the instance of the event binder (binds the widget for events. */
    private static final WidgetEventBinder eventBinder = GWT.create(WidgetEventBinder.class);
    
    /** The event registration this editor is using to handle survey events */
    private static HandlerRegistration eventRegistration = null;

    /** The editor that edits the list of {@link PointRef} */
    @UiField(provided = true)
    protected ItemListEditor<AvoidPlaceRefWrapper> pointsEditor = new ItemListEditor<>(new AvoidPlaceRefItemEditor(this));

    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;
    
    /** the widget to set whether the learner will cause an avoid location assessment or it will be automatic */
    @UiField
    protected CheckBox assessMyLocationCheck;

    /** the wrap button used to edit points on map */
    @UiField(provided = true)
    protected WrapButton globalWrapButton = new WrapButton(false);
    
    /** The reference to the last item edited in the pointEditor */
    private static AvoidPlaceRefWrapper lastItemEdited = null;

    /** Validation for ensuring a place to avoid has been specified */
    private final WidgetValidationStatus placeToAvoidValidation;
    
    /** Validation ensuring there are assess my location learner actions */
    private final ModelValidationStatus missingLearnerAction = new ModelValidationStatus(
            "This condition requires an Assess My Location action") {
            @Override
            protected void fireJumpToEvent(Serializable modelObject) {
                ScenarioEventUtility.fireJumpToEvent(modelObject);
            }
        };

    /**
     * The Interface AvoidLocationConditionEditorUiBinder.
     */
    interface AvoidLocationConditionEditorUiBinder extends UiBinder<Widget, AvoidLocationConditionEditorImpl> {
    }

    /**
     * Default Constructor
     *
     * Required to be public for GWT UIBinder compatibility.
     */
    public AvoidLocationConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        if (eventRegistration == null) {
            eventRegistration = eventBinder.bindEventHandlers(AvoidLocationConditionEditorImpl.this, SharedResources.getInstance().getEventBus());
        }
        
        missingLearnerAction.setModelObject(ScenarioClientUtility.getAvailableLearnerActions());
        
        pointsEditor.setFields(buildItemFields());
        Widget addPlaceButton = pointsEditor.addCreateListAction("Click to add a place to avoid", new CreateListAction<AvoidPlaceRefWrapper>() {
            @Override
            public AvoidPlaceRefWrapper createDefaultItem() {
                setDirty();
                return new AvoidPlaceRefWrapper();
            }
        });

        pointsEditor.addListChangedCallback(new ListChangedCallback<AvoidPlaceRefWrapper>() {

            @Override
            public void listChanged(ListChangedEvent<AvoidPlaceRefWrapper> event) {

                setClean();

                getInput().getPointRef().clear();
                getInput().getAreaRef().clear();

                for(AvoidPlaceRefWrapper wrapper : pointsEditor.getItems()) {

                    if(wrapper.getPlaceRef() instanceof PointRef) {
                        getInput().getPointRef().add((PointRef) wrapper.getPlaceRef());

                    } else if(wrapper.getPlaceRef() instanceof AreaRef) {
                        getInput().getAreaRef().add((AreaRef) wrapper.getPlaceRef());
                    }
                }

                requestValidationAndFireDirtyEvent(getCondition(), placeToAvoidValidation);
                
                //a place of interest reference may have been changed, so update the global list of references
                ScenarioClientUtility.gatherPlacesOfInterestReferences();
            }
        });
        
        pointsEditor.addEditCancelledCallback(new EditCancelledCallback() {
            
            @Override
            public void editCancelled() {
                setClean();
            }
        });
        
        pointsEditor.setRemoveItemDialogTitle("Remove Location");
        pointsEditor.setRemoveItemStringifier(new Stringifier<AvoidPlaceRefWrapper>() {
            
            @Override
            public String stringify(AvoidPlaceRefWrapper obj) {

                String placeName = null;
                if(obj.getPlaceRef() != null){
                    if(obj.getPlaceRef() instanceof PointRef){
                        placeName = ((PointRef)obj.getPlaceRef()).getValue();
                    }else if(obj.getPlaceRef() instanceof AreaRef){
                        placeName = ((AreaRef)obj.getPlaceRef()).getValue();
                    }
                }
                
                if(StringUtils.isNotBlank(placeName)){
                    return "avoiding "+bold(placeName).asString()+" (does not delete the underlying place of interest)";
                }else{
                    return "avoiding this location (does not delete the underlying place of interest)";
                }
            }
        });

        placeToAvoidValidation = new WidgetValidationStatus(addPlaceButton, "At least one place to avoid must be defined.");
        
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
        
        assessMyLocationCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                getInput().setRequireLearnerAction(event != null && event.getValue());
                
                //add or remove the appropriate learner action as needed
                if(event != null && event.getValue()) {
                    ScenarioClientUtility.ensureAssessLocationLearnerActionExists();
                    
                } else {
                    ScenarioClientUtility.cleanUpAssessLocationLearnerAction();
                }
                
                requestValidationAndFireDirtyEvent(getCondition(), missingLearnerAction);
            }
        });
    }
    
    @Override
    protected void onEdit() {

        List<AvoidPlaceRefWrapper> wrappers = new ArrayList<>();

        for(PointRef ref : getInput().getPointRef()) {
            wrappers.add(new AvoidPlaceRefWrapper(ref));
        }

        for(AreaRef ref : getInput().getAreaRef()) {
            wrappers.add(new AvoidPlaceRefWrapper(ref));
        }

        pointsEditor.setItems(wrappers);

        // populate the scoring rules panel
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
        
        assessMyLocationCheck.setValue(getInput().isRequireLearnerAction() != null && getInput().isRequireLearnerAction());
    }
    
    /**
     * Requests revalidation when a new scenario object is created.
     *
     * @param event The event containing the object that was created.
     */
    @EventHandler
    protected void onScenarioObjectCreated(CreateScenarioObjectEvent event) {
        if (event.getScenarioObject() instanceof LearnerAction) {
            requestValidation(missingLearnerAction);
            ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
        }
    }
    
    /**
     * Requests revalidation when a scenario object has been deleted.
     *
     * @param event The event containing the object that was deleted.
     */
    @EventHandler
    protected void onScenarioObjectDeleted(DeleteScenarioObjectEvent event) {
        if (event.getScenarioObject() instanceof LearnerAction) {
            requestValidation(missingLearnerAction);
            ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
        }
    }
    
    /**
     * Removes the last item edited if it's the default row when returning from the map editor
     * 
     * @param event The event containing the place that was created or selected
     */
    @EventHandler
    protected void onPlaceOfInterestResumeEditEvent(PlaceOfInterestResumeEditEvent event) {
        if (event.getCleanupDefault() && event.getPlace() != null && lastItemEdited != null && event.getCondition().equals(getCondition())) {
            if (lastItemEdited.getPlaceRef() instanceof PointRef) {
                if (((PointRef)lastItemEdited.getPlaceRef()).getValue() == null) {
                    getInput().getPointRef().remove(0);
                    ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
                }
            } else if (lastItemEdited.getPlaceRef() instanceof AreaRef) {
                if (((AreaRef)lastItemEdited.getPlaceRef()).getValue() == null) {
                    getInput().getPointRef().remove(0);
                    ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
                }
            }
        }
    }
    
    /**
     * Saves a reference to the last item edited if it's place is unspecified
     * 
     * @param event The event containing the reference to the item edited
     */
    @EventHandler
    protected void onItemListEditorEditEvent(ItemListEditorEditEvent<?> event) {
        if (event.getItemEdited() instanceof AvoidPlaceRefWrapper) {
            AvoidPlaceRefWrapper wrapper = (AvoidPlaceRefWrapper) event.getItemEdited();
            if (wrapper.getPlaceRef() != null) {
                if (wrapper.getPlaceRef() instanceof PointRef) {
                    PointRef pointRef = (PointRef) wrapper.getPlaceRef();
                    if (pointRef.getValue() == null) {
                        lastItemEdited = wrapper;
                    }
                } else if (wrapper.getPlaceRef() instanceof AreaRef) {
                    AreaRef areaRef = (AreaRef) wrapper.getPlaceRef();
                    if (areaRef.getValue() == null) {
                        lastItemEdited = wrapper;
                    }
                }
            }
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(placeToAvoidValidation);
        validationStatuses.add(missingLearnerAction);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int pointSize;
        if (placeToAvoidValidation.equals(validationStatus)) {
            pointSize = pointsEditor.size();
            validationStatus.setValidity(pointSize > 0);
            
        } else if (missingLearnerAction.equals(validationStatus)) {
            
            if(assessMyLocationCheck.getValue() == null || !assessMyLocationCheck.getValue()) {
                validationStatus.setValid();
                
            } else {  
                
                if(ScenarioClientUtility.getLearnerActions() != null) {
                    for (LearnerAction learnerAction : ScenarioClientUtility.getLearnerActions().getLearnerAction()) {
                        
                        if(learnerAction.getType() == LearnerActionEnumType.ASSESS_MY_LOCATION) {
                            validationStatus.setValid();
                            return;
                        }
                    }
                }
                
                validationStatus.setInvalid();
            }
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(pointsEditor);
        childValidationComposites.add(teamPicker);
        childValidationComposites.add(rtaRulesPanel);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        pointsEditor.setReadonly(isReadonly);
        rtaRulesPanel.setReadonly(isReadonly);
        teamPicker.setReadonly(isReadonly);
        assessMyLocationCheck.setEnabled(!isReadonly);
    }

    /**
     * Creates the fields for the {@link #pointsEditor}.
     *
     * @return The {@link Iterable} containing each {@link ItemField}.
     */
    private Iterable<? extends ItemField<AvoidPlaceRefWrapper>> buildItemFields() {
        ItemField<AvoidPlaceRefWrapper> summaryField = new ItemField<AvoidPlaceRefWrapper>(null, "100%") {
            @Override
            public Widget getViewWidget(AvoidPlaceRefWrapper ref) {

                SafeHtml description = null;

                if(ref.getPlaceRef() instanceof PointRef) {

                    PointRef item = (PointRef) ref.getPlaceRef();

                    double distance = item.getDistance().doubleValue();
                    String unitString = distance == 1 ? " meter" : " meters";
                    SafeHtml place = item.getValue() != null ? bold(item.getValue()) : bold(color("unspecified", "red"));
                    description = new SafeHtmlBuilder().appendEscaped("Within ")
                            .append(distance).appendEscaped(unitString).appendEscaped(" of ").append(place).toSafeHtml();

                } else if(ref.getPlaceRef() instanceof AreaRef) {

                    AreaRef item = (AreaRef) ref.getPlaceRef();

                    SafeHtml place = item.getValue() != null ? bold(item.getValue()) : bold(color("unspecified", "red"));
                    description = new SafeHtmlBuilder().appendEscaped("Inside ").append(place).toSafeHtml();
                }

                return new HTML(description);
            }
        };

        return Arrays.asList(summaryField);
    }
}