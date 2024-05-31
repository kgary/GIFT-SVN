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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconSize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AGL;
import generated.dkf.Coordinate;
import generated.dkf.Entities;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.LifeformTargetAccuracyCondition;
import generated.dkf.StartLocation;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.RealTimeAssessmentScoringRulesPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The condition impl for LifeformTargetAccuracy.
 */
public class LifeformTargetAccuracyConditionEditorImpl extends ConditionInputPanel<LifeformTargetAccuracyCondition> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static LifeformTargetAccuracyConditionEditorUiBinder uiBinder = GWT
            .create(LifeformTargetAccuracyConditionEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface LifeformTargetAccuracyConditionEditorUiBinder
            extends UiBinder<Widget, LifeformTargetAccuracyConditionEditorImpl> {
    }

    /** The item list editor table for the start locations */
    @UiField(provided = true)
    protected ItemListEditor<TeamRefOrStartLocation> startLocationEditor = new ItemListEditor<TeamRefOrStartLocation>(
            new StartLocationItemEditor());

    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesPanel rtaRulesPanel;

    /** The container for showing validation messages for having no starting entity locations. */
    private final WidgetValidationStatus startLocationsValidationStatus;

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public LifeformTargetAccuracyConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        startLocationEditor.setFields(buildStartLocationItemFields());
        startLocationEditor.addListChangedCallback(new ListChangedCallback<TeamRefOrStartLocation>() {
            @Override
            public void listChanged(ListChangedEvent<TeamRefOrStartLocation> event) {
                
                List<String> teamRefs = new ArrayList<>();
                List<StartLocation> startLocations = new ArrayList<>();
                
                for(TeamRefOrStartLocation item : startLocationEditor.getItems()) {
                    
                    if(item.getRefOrLocation() instanceof StartLocation) {
                        startLocations.add((StartLocation) item.getRefOrLocation());
                        
                    } else if(item.getRefOrLocation() instanceof String) {
                        teamRefs.add((String) item.getRefOrLocation());
                    }
                }
                
                getInput().getEntities().getTeamMemberRef().clear();
                getInput().getEntities().getTeamMemberRef().addAll(teamRefs);
                
                getInput().getEntities().getStartLocation().clear();
                getInput().getEntities().getStartLocation().addAll(startLocations);
                
                requestValidationAndFireDirtyEvent(getCondition(), startLocationsValidationStatus);
            }
        });
        startLocationEditor.setRemoveItemDialogTitle("Remove target to shoot");
        startLocationEditor.setRemoveItemStringifier(new Stringifier<TeamRefOrStartLocation>() {
            
            @Override
            public String stringify(TeamRefOrStartLocation item) {
                
                if(item.getRefOrLocation() instanceof StartLocation) {
                    
                    StartLocation loc = (StartLocation) item.getRefOrLocation();

                    String location = ScenarioClientUtility.prettyPrintCoordinate(loc.getCoordinate());
                    
                    if(StringUtils.isNotBlank(location)){
                        return "the start location "+bold(location).asString();
                    }else{
                        return "this start location";
                    }
                
                } else {
                    
                    String location = item.getRefOrLocation() instanceof String ? (String) item.getRefOrLocation() : null;
                    
                    if(StringUtils.isNotBlank(location)){
                        return "the target entity "+bold(location).asString();
                    }else{
                        return "this target entity";
                    }
                }
            }
        });
        
        Widget hostileLocationAddAction = startLocationEditor.addCreateListAction(
                "Click here to add a new target entity", buildStartLocationCreateAction());

        startLocationsValidationStatus = new WidgetValidationStatus(hostileLocationAddAction,
                "There are no target entities to shoot. Define the location of an entity that the learner should shoot.");

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

    /**
     * Builds the item fields for the hostile entity location table.
     * 
     * @return the {@link ItemField item field columns} for the hostile entity location table.
     * @throws UnsupportedOperationException if the coordinate type is unknown
     */
    private List<ItemField<TeamRefOrStartLocation>> buildStartLocationItemFields() {

        ItemField<TeamRefOrStartLocation> startLocationDisplayField = new ItemField<TeamRefOrStartLocation>(null, "100%") {
            @Override
            public Widget getViewWidget(TeamRefOrStartLocation item) {
                
                FlowPanel flowPanel = new FlowPanel();
                
                if(item.getRefOrLocation() instanceof StartLocation) {
                    
                    StartLocation loc = (StartLocation) item.getRefOrLocation();

                    if (loc.getCoordinate() == null) {
                        loc.setCoordinate(new Coordinate());
                    }
    
                    Serializable coordinateType;
                    if (loc.getCoordinate().getType() == null) {
                        GCC gcc = new GCC();
                        gcc.setX(BigDecimal.ZERO);
                        gcc.setY(BigDecimal.ZERO);
                        gcc.setZ(BigDecimal.ZERO);
                        coordinateType = gcc;
                    } else {
                        coordinateType = loc.getCoordinate().getType();
                    }
    
                    
    
                    CoordinateType typeEnum = CoordinateType.getCoordinateTypeFromCoordinate(coordinateType);
    
                    BigDecimal xValue;
                    BigDecimal yValue;
                    BigDecimal zValue;
    
                    switch (typeEnum) {
                    case GCC:
                        GCC gcc = (GCC) coordinateType;
                        xValue = gcc.getX();
                        yValue = gcc.getY();
                        zValue = gcc.getZ();
                        break;
                    case GDC:
                        GDC gdc = (GDC) coordinateType;
                        xValue = gdc.getLongitude();
                        yValue = gdc.getLatitude();
                        zValue = gdc.getElevation();
                        break;
                    case AGL:
                        AGL agl = (AGL) coordinateType;
                        xValue = agl.getX();
                        yValue = agl.getY();
                        zValue = agl.getElevation();
                        break;
                    default:
                        throw new UnsupportedOperationException("The type '" + typeEnum + "' was unexpected.");
                    }
    
                    if (xValue == null) {
                        xValue = BigDecimal.ZERO;
                    }
    
                    if (yValue == null) {
                        yValue = BigDecimal.ZERO;
                    }
    
                    if (zValue == null) {
                        zValue = BigDecimal.ZERO;
                    }
    
                    Icon icon = new Icon(typeEnum.getIconType());
                    icon.setSize(IconSize.LARGE);
                    flowPanel.add(icon);
    
                    InlineHTML htmlLabel = new InlineHTML(typeEnum.name());
                    htmlLabel.getElement().getStyle().setPaddingLeft(5, Unit.PX);
                    htmlLabel.getElement().getStyle().setPaddingRight(5, Unit.PX);
                    flowPanel.add(htmlLabel);
    
                    flowPanel.add(new BubbleLabel(typeEnum.buildXLabel(Float.valueOf(xValue.floatValue()))));
                    flowPanel.add(new BubbleLabel(typeEnum.buildYLabel(Float.valueOf(yValue.floatValue()))));
                    flowPanel.add(new BubbleLabel(typeEnum.buildZLabel(Float.valueOf(zValue.floatValue()))));
                    
                } else if(item.getRefOrLocation() instanceof String) {
                    flowPanel.add(new Label((String) item.getRefOrLocation()));
                }

                return flowPanel;
            }
        };

        return Arrays.asList(startLocationDisplayField);
    }

    /**
     * Builds the create action for the hostile entity location table.
     * 
     * @return the {@link CreateListAction} for the hostile entity location table.
     */
    private CreateListAction<TeamRefOrStartLocation> buildStartLocationCreateAction() {
        return new CreateListAction<TeamRefOrStartLocation>() {

            @Override
            public TeamRefOrStartLocation createDefaultItem() {
                return new TeamRefOrStartLocation();
            }
        };
    }

    @Override
    protected void onEdit() {
        if (getInput().getEntities() == null) {
            getInput().setEntities(new Entities());
        }

        // set list into the hostile entity location editor
        List<TeamRefOrStartLocation> items = new ArrayList<>();
        for(String teamRef : getInput().getEntities().getTeamMemberRef()) {
            items.add(new TeamRefOrStartLocation(teamRef));
        }
        for(StartLocation loc : getInput().getEntities().getStartLocation()) {
            items.add(new TeamRefOrStartLocation(loc));
        }
        
        startLocationEditor.setItems(items);

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
        validationStatuses.add(startLocationsValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int startLocationSize;
        if (startLocationsValidationStatus.equals(validationStatus)) {
            startLocationSize = startLocationEditor.size();
            startLocationsValidationStatus.setValidity(startLocationSize > 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(startLocationEditor);
        childValidationComposites.add(teamPicker);
        childValidationComposites.add(rtaRulesPanel);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        startLocationEditor.setReadonly(isReadonly);
        rtaRulesPanel.setReadonly(isReadonly);
        teamPicker.setReadonly(isReadonly);
    }
}